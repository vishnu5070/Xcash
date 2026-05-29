package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Calculation
import com.example.data.CalculationRepository
import com.example.data.toDenominationsJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CashXViewModel(
    private val repository: CalculationRepository,
    private val context: Context
) : ViewModel() {

    // Indian standard denominations (Notes only)
    private val standardDenominations = listOf(500, 200, 100, 50, 20, 10)

    private val _denominations = MutableStateFlow(standardDenominations.associateWith { 0 })
    val denominations: StateFlow<Map<Int, Int>> = _denominations.asStateFlow()

    private val _coinsInput = MutableStateFlow("")
    val coinsInput: StateFlow<String> = _coinsInput.asStateFlow()

    private val _subtractInput = MutableStateFlow("")
    val subtractInput: StateFlow<String> = _subtractInput.asStateFlow()

    private val _noteMemoInput = MutableStateFlow("") // Unused but kept for model compatibility
    val noteMemoInput: StateFlow<String> = _noteMemoInput.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow<String?>(null) // Format: "yyyy-MM-dd" or null
    val selectedDateFilter: StateFlow<String?> = _selectedDateFilter.asStateFlow()

    // Persistent Theme preference
    private val sharedPrefs = context.getSharedPreferences("xcash_settings", Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleTheme() {
        val nextMode = !_isDarkMode.value
        _isDarkMode.value = nextMode
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sharedPrefs.edit().putBoolean("is_dark_mode", nextMode).commit()
            }
        }
    }

    // Real-time calculation derivations on Dispatchers.Default CPU-bound thread pool
    val totalCash: StateFlow<Double> = combine(_denominations, _coinsInput) { map, coinsStr ->
        withContext(Dispatchers.Default) {
            val notesSum = map.entries.sumOf { (denom, count) -> (denom * count).toDouble() }
            val coinsVal = coinsStr.toDoubleOrNull() ?: 0.0
            notesSum + coinsVal
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalNotes: StateFlow<Int> = _denominations.map { map ->
        withContext(Dispatchers.Default) {
            map.values.sum()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val remainingBalance: StateFlow<Double> = combine(totalCash, _subtractInput) { cash, subStr ->
        withContext(Dispatchers.Default) {
            val subValue = subStr.toDoubleOrNull() ?: 0.0
            (cash - subValue).coerceAtLeast(0.0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Historical calculations
    val allCalculations: StateFlow<List<Calculation>> = repository.allCalculations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Filter list of history
    val filteredCalculations: StateFlow<List<Calculation>> = combine(
        allCalculations, searchQuery, selectedDateFilter
    ) { list, query, dateFilter ->
        list.filter { calc ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                calc.noteMemo.contains(query, ignoreCase = true) ||
                calc.totalCash.toString().contains(query) ||
                calc.remainingBalance.toString().contains(query) ||
                getFormattedDate(calc.timestamp).contains(query, ignoreCase = true)
            }
            matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun updateCount(denomination: Int, count: Int) {
        val current = _denominations.value.toMutableMap()
        current[denomination] = count.coerceAtLeast(0)
        _denominations.value = current
    }

    fun incrementCount(denomination: Int) {
        val currentVal = _denominations.value[denomination] ?: 0
        updateCount(denomination, currentVal + 1)
    }

    fun decrementCount(denomination: Int) {
        val currentVal = _denominations.value[denomination] ?: 0
        if (currentVal > 0) {
            updateCount(denomination, currentVal - 1)
        }
    }

    fun updateCoinsAmount(coins: String) {
        if (coins.length <= 8 && (coins.isEmpty() || coins.matches(Regex("^\\d*\\.?\\d*$")))) {
            _coinsInput.value = coins
        }
    }

    fun updateSubtractAmount(amount: String) {
        // Enforce numeric/decimal structure
        if (amount.length <= 8 && (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$")))) {
            _subtractInput.value = amount
        }
    }

    fun updateNoteMemo(note: String) {
        _noteMemoInput.value = note
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearAllInputs() {
        _denominations.value = standardDenominations.associateWith { 0 }
        _coinsInput.value = ""
        _subtractInput.value = ""
        _noteMemoInput.value = ""
    }

    fun saveCurrentCalculation(onSuccess: () -> Unit = {}) {
        val cash = totalCash.value
        val notes = totalNotes.value
        val coinsVal = (_coinsInput.value.toDoubleOrNull() ?: 0.0).toInt()
        val subAmount = _subtractInput.value.toDoubleOrNull() ?: 0.0
        val remaining = remainingBalance.value

        if (cash == 0.0 && subAmount == 0.0) {
            // Nothing to save
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Include coins value under denomination 0 in denominationsJson
                val finalDenominationsMap = _denominations.value.toMutableMap()
                if (coinsVal > 0) {
                    finalDenominationsMap[0] = coinsVal
                }
                val calc = Calculation(
                    denominationsJson = finalDenominationsMap.toDenominationsJson(),
                    subtractAmount = subAmount,
                    totalNotes = notes,
                    totalCash = cash,
                    remainingBalance = remaining,
                    noteMemo = "Cash count"
                )
                repository.insert(calc)
            }
            clearAllInputs()
            onSuccess()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteById(id)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearAll()
            }
        }
    }

    // Date formatting helper
    private fun getFormattedDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}

class CashXViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CashXViewModel::class.java)) {
            val db = AppDatabase.getInstance(context)
            val repo = CalculationRepository(db.calculationDao())
            @Suppress("UNCHECKED_CAST")
            return CashXViewModel(repo, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
