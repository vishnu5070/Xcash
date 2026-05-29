package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Calculation
import com.example.data.getDenominationsMap
import com.example.ui.components.DenominationRow
import com.example.ui.components.FintechButton
import com.example.ui.components.GlassCard
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SleekTextField
import com.example.ui.theme.*
import com.example.ui.viewmodel.CashXViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(viewModel: CashXViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val denominations by viewModel.denominations.collectAsStateWithLifecycle()
    val coinsInput by viewModel.coinsInput.collectAsStateWithLifecycle()
    val subtractInput by viewModel.subtractInput.collectAsStateWithLifecycle()
    val totalCash by viewModel.totalCash.collectAsStateWithLifecycle()
    val totalNotes by viewModel.totalNotes.collectAsStateWithLifecycle()
    val remainingBalance by viewModel.remainingBalance.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredCalculations.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Counter, 1: History

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_main_scaffold"),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Top Header Component with Toggle Theme & Outlined Pill "Reset All"
            HeaderSection(
                isDarkMode = isDarkMode,
                onThemeToggle = { viewModel.toggleTheme() },
                onResetClick = {
                    viewModel.clearAllInputs()
                    Toast.makeText(context, "All values reset", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Custom Dual Tab Segment Switcher
            TabSegmentControl(
                selectedTab = activeTab,
                onTabSelected = {
                    activeTab = it
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Screen content cross-fade swap
            Box(modifier = Modifier.weight(1f)) {
                if (activeTab == 0) {
                    CounterTabScreen(
                        denominations = denominations,
                        coinsInput = coinsInput,
                        subtractInput = subtractInput,
                        totalCash = totalCash,
                        totalNotes = totalNotes,
                        remainingBalance = remainingBalance,
                        onCountChange = { denom, count ->
                            viewModel.updateCount(denom, count)
                        },
                        onCoinsChange = { viewModel.updateCoinsAmount(it) },
                        onSubtractChange = { viewModel.updateSubtractAmount(it) },
                        onSaveClick = {
                            if (totalCash == 0.0 && (subtractInput.toDoubleOrNull() ?: 0.0) == 0.0) {
                                Toast.makeText(context, "Nothing to save yet", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveCurrentCalculation {
                                    Toast.makeText(context, "Calculation recorded in Ledger", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onClearClick = {
                            viewModel.clearAllInputs()
                            Toast.makeText(context, "All values cleared", Toast.LENGTH_SHORT).show()
                        },
                        onShareClick = {
                            val summary = formatShareSummary(
                                denominations = denominations,
                                totalCash = totalCash,
                                totalNotes = totalNotes,
                                subtract = subtractInput.toDoubleOrNull() ?: 0.0,
                                remaining = remainingBalance,
                                note = ""
                            )
                            shareText(context, summary)
                        }
                    )
                } else {
                    HistoryTabScreen(
                        historyList = filteredHistory,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onDeleteClick = { id ->
                            viewModel.deleteHistoryItem(id)
                            Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                        },
                        onClearAllHistoryClick = {
                            viewModel.clearHistory()
                            Toast.makeText(context, "Ledger fully cleared", Toast.LENGTH_SHORT).show()
                        },
                        onShareIndividualClick = { calc ->
                            val map = calc.getDenominationsMap()
                            val summary = formatShareSummary(
                                denominations = map,
                                totalCash = calc.totalCash,
                                totalNotes = calc.totalNotes,
                                subtract = calc.subtractAmount,
                                remaining = calc.remainingBalance,
                                note = calc.noteMemo,
                                timestamp = calc.timestamp
                            )
                            shareText(context, summary)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onResetClick: () -> Unit
) {
    val currentDateStr = remember {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date()).uppercase()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Xcash",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = currentDateStr,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme toggle button with dynamic LightMode / DarkMode icons
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .size(34.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Outlined pill-style Reset All button
            Button(
                onClick = onResetClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("reset_all_button")
            ) {
                Text(
                    text = "Reset All",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun TabSegmentControl(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val inactiveColor = if (isLight) LightTextSecondary else TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(onBgColor.copy(alpha = 0.03f))
            .border(1.dp, onBgColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        val countWeight = if (selectedTab == 0) 1f else 1f
        val historyWeight = if (selectedTab == 1) 1f else 1f

        // Tab item 1
        Box(
            modifier = Modifier
                .weight(countWeight)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onTabSelected(0) }
                .testTag("tab_counter"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Calculator",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else inactiveColor
            )
        }

        // Tab item 2
        Box(
            modifier = Modifier
                .weight(historyWeight)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onTabSelected(1) }
                .testTag("tab_history"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "History",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else inactiveColor
            )
        }
    }
}

@Composable
fun CounterTabScreen(
    denominations: Map<Int, Int>,
    coinsInput: String,
    subtractInput: String,
    totalCash: Double,
    totalNotes: Int,
    remainingBalance: Double,
    onCountChange: (Int, Int) -> Unit,
    onCoinsChange: (String) -> Unit,
    onSubtractChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onClearClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val denominationKeys = remember { denominations.keys.toList() }

    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White
    val buttonContentColor = if (isLight) Color.White else Color.Black
    val secondaryContentColor = if (isLight) Color.Black else Color.White
    val inactiveColor = if (isLight) LightTextSecondary else TextSecondary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("counter_screen_scroll"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main glass Total Amount Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Absolute backdrop glow effect: -inset-1 blur-xl opacity-20 bg-white/20
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(baseColor.copy(alpha = 0.12f), Color.Transparent),
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                )

                // Relative Glass Card with 32.dp rounding and thin border line
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grand_total_card"),
                    alpha = 0.06f,
                    borderAlpha = 0.08f,
                    cornerRadius = 32.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "TOTAL BALANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = baseColor.copy(alpha = 0.4f),
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = formatCurrency(remainingBalance),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Light,
                            color = baseColor,
                            letterSpacing = (-1.5).sp,
                            modifier = Modifier.testTag("total_balance_text")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(color = baseColor.copy(alpha = 0.10f))

                        Spacer(modifier = Modifier.height(14.dp))

                        // Unified horizontally centered row with border dividers matching HTML
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gross Column
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "GROSS",
                                    fontSize = 10.sp,
                                    color = baseColor.copy(alpha = 0.40f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatCurrency(totalCash),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = baseColor
                                )
                            }

                            // Vertical divider 1
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(baseColor.copy(alpha = 0.10f))
                            )

                            // Notes Column
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "NOTES",
                                    fontSize = 10.sp,
                                    color = baseColor.copy(alpha = 0.40f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalNotes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = baseColor
                                )
                            }

                            // Vertical divider 2
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(baseColor.copy(alpha = 0.10f))
                            )

                            // Expense Column
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "EXPENSE",
                                    fontSize = 10.sp,
                                    color = baseColor.copy(alpha = 0.40f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val subAmt = subtractInput.toDoubleOrNull() ?: 0.0
                                Text(
                                    text = formatCurrency(subAmt),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = baseColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section header for Notes/Coins
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COUNTER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = baseColor.copy(alpha = 0.40f),
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Enter quantities directly",
                    fontSize = 10.sp,
                    color = if (isLight) LightTextTertiary else TextTertiary
                )
            }
        }

        // Denominations grid/rows representation
        items(denominationKeys) { denomination ->
            val count = denominations[denomination] ?: 0
            DenominationRow(
                denomination = denomination,
                count = count,
                onCountChange = { newCount -> onCountChange(denomination, newCount) },
                modifier = Modifier.testTag("denom_row_$denomination")
            )
        }

        // Dedicated Unified row for "Total Coins" directly below the note fields
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(baseColor.copy(alpha = 0.02f))
                    .border(1.dp, baseColor.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Coins",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = inactiveColor,
                    modifier = Modifier.width(120.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Custom single-line direct type field for Coins (matches DenominationQuantityInput shape exactly)
                com.example.ui.components.DenominationQuantityInput(
                    value = coinsInput,
                    onValueChange = onCoinsChange,
                    keyboardType = KeyboardType.Decimal,
                    isDecimal = true,
                    modifier = Modifier
                        .width(76.dp)
                        .height(38.dp)
                        .testTag("coins_input_field")
                )

                Spacer(modifier = Modifier.weight(1f))

                val coinValDouble = coinsInput.toDoubleOrNull() ?: 0.0
                Text(
                    text = formatCurrency(coinValDouble),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (coinValDouble > 0) baseColor else inactiveColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(80.dp)
                )
            }
        }

        // Subtract expense section (Memo input fully removed)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth().testTag("subtract_card"),
                alpha = 0.03f,
                borderAlpha = 0.06f
            ) {
                Text(
                    text = "SUBTRACT EXPENSE AMOUNT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = baseColor.copy(alpha = 0.40f),
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                SleekTextField(
                    value = subtractInput,
                    onValueChange = onSubtractChange,
                    placeholder = "Subtract Expense (e.g. 150)",
                    keyboardType = KeyboardType.Decimal,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = "Minus Icon",
                            tint = inactiveColor,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.testTag("subtract_input_field")
                )
            }
        }

        // Bottom CTAs
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FintechButton(
                    text = "Save Calculation",
                    onClick = onSaveClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Icon",
                            tint = buttonContentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_ledger_button")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SecondaryButton(
                        text = "Share Summary",
                        onClick = onShareClick,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Icon",
                                tint = secondaryContentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("share_summary_button")
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    SecondaryButton(
                        text = "Clear",
                        onClick = onClearClick,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.RotateLeft,
                                contentDescription = "Clear Icon",
                                tint = secondaryContentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("clear_all_button")
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryTabScreen(
    historyList: List<Calculation>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onClearAllHistoryClick: () -> Unit,
    onShareIndividualClick: (Calculation) -> Unit
) {
    var expandedItemId by remember { mutableStateOf<Int?>(null) }
    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White
    val inactiveColor = if (isLight) LightTextSecondary else TextSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen_root")
    ) {
        // Historical search bar
        SleekTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Search notes, amount, or date...",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search icon",
                    tint = inactiveColor,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = baseColor
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (historyList.isEmpty()) {
            // Elegant Empty state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Folder Empty",
                        tint = if (isLight) LightTextTertiary else TextTertiary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No saved ledgers found",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = inactiveColor
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try refining your words" else "Count cash and save to register active cards",
                        fontSize = 11.sp,
                        color = if (isLight) LightTextTertiary else TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            // Dynamic Header Row with Ledger Count & Reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RECORDED LOGS (${historyList.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = inactiveColor,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = "Clear All Logs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = baseColor,
                    modifier = Modifier
                        .clickable { onClearAllHistoryClick() }
                        .testTag("clear_all_history_btn")
                )
            }

            // Scrollable Log entries
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("history_items_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(historyList, key = { it.id }) { calc ->
                    val isExpanded = expandedItemId == calc.id
                    HistoryItemCard(
                        calculation = calc,
                        isExpanded = isExpanded,
                        onExpandToggle = {
                            expandedItemId = if (isExpanded) null else calc.id
                        },
                        onDelete = { onDeleteClick(calc.id) },
                        onShare = { onShareIndividualClick(calc) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    calculation: Calculation,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val dateStr = remember(calculation.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(calculation.timestamp))
    }
    val isLight = MaterialTheme.colorScheme.background == AbsoluteWhite || MaterialTheme.colorScheme.background == Color.White
    val baseColor = if (isLight) Color.Black else Color.White
    val inactiveColor = if (isLight) LightTextSecondary else TextSecondary
    val secondaryContentColor = if (isLight) Color.Black else Color.White

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${calculation.id}"),
        alpha = if (isExpanded) 0.08f else 0.04f,
        borderAlpha = if (isExpanded) 0.18f else 0.06f,
        onClick = onExpandToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = inactiveColor,
                    letterSpacing = 0.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = calculation.noteMemo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = baseColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "BAL",
                    fontSize = 9.sp,
                    color = inactiveColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = formatCurrency(calculation.remainingBalance),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = baseColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand Indicator",
                tint = inactiveColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // Expanded view detail list representation
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                HorizontalDivider(color = baseColor.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                val denominationsMap = remember { calculation.getDenominationsMap() }
                val nonZeroDenominations = remember {
                    denominationsMap.entries
                        .filter { it.value > 0 }
                        .sortedByDescending { it.key }
                }

                if (nonZeroDenominations.isEmpty()) {
                    Text(
                        text = "No denominations counted (only expense/memo saved)",
                        fontSize = 12.sp,
                        color = if (isLight) LightTextTertiary else TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "DENOMINATIONS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = inactiveColor,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    nonZeroDenominations.forEach { (denom, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (denom == 0) "Total Coins" else "₹$denom × $count",
                                fontSize = 13.sp,
                                color = if (isLight) LightTextPrimary else TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (denom == 0) "₹$count" else "₹${denom * count}",
                                fontSize = 13.sp,
                                color = if (isLight) LightTextPrimary else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = baseColor.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                // Numerical detailed rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Cash Val:", fontSize = 12.sp, color = inactiveColor)
                    Text(text = formatCurrency(calculation.totalCash), fontSize = 12.sp, color = baseColor, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Bills Counted:", fontSize = 12.sp, color = inactiveColor)
                    Text(text = "${calculation.totalNotes} pcs", fontSize = 12.sp, color = baseColor, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Expense Subtract:", fontSize = 12.sp, color = inactiveColor)
                    Text(text = "-${formatCurrency(calculation.subtractAmount)}", fontSize = 12.sp, color = baseColor, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card actions: share or delete inside history card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    SecondaryButton(
                        text = "Share entry",
                        onClick = onShare,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share entry",
                                tint = secondaryContentColor,
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        modifier = Modifier.height(38.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, Color(0xFFCC3333).copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .background(Color(0xFFCC3333).copy(alpha = 0.08f), RoundedCornerShape(32.dp)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete entry",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Global Help formatting & sharing utilities
fun formatCurrency(amount: Double): String {
    if (amount.isNaN() || amount.isInfinite()) return "₹0"
    
    val isNegative = amount < 0
    val absAmount = Math.abs(amount)
    
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    var formatted = formatter.format(absAmount)
    
    // Clean up en_IN defaults if they add trailing .00 safely
    formatted = formatted.removeSuffix(".00")
    
    // Make sure we have the pure Rupee sign symbol
    if (!formatted.startsWith("₹")) {
        // Strip out existing generic currency sign and prepend ours
        formatted = "₹" + formatted.replace(Regex("[^0-9,.]"), "").trim()
    }
    
    return if (isNegative) "-$formatted" else formatted
}

fun formatShareSummary(
    denominations: Map<Int, Int>,
    totalCash: Double,
    totalNotes: Int,
    subtract: Double,
    remaining: Double,
    note: String,
    timestamp: Long = System.currentTimeMillis()
): String {
    val dateStr = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(timestamp))
    val sb = java.lang.StringBuilder()
    sb.append("⚡ XCASH TRANSACTION REPORT ⚡\n")
    sb.append("Date: $dateStr\n")
    if (note.isNotBlank()) {
        sb.append("Memo: $note\n")
    }
    sb.append("----------------------------\n")

    denominations.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.key }
        .forEach { (denom, count) ->
            if (denom == 0) {
                sb.append("Total Coins: ₹$count\n")
            } else {
                sb.append("₹$denom × $count = ₹${denom * count}\n")
            }
        }

    sb.append("----------------------------\n")
    sb.append("Gross Cash Total : ${formatCurrency(totalCash)} ($totalNotes bills)\n")
    if (subtract > 0) {
        sb.append("Expense Subtract : -${formatCurrency(subtract)}\n")
    }
    sb.append("Net Ledger Balance: ${formatCurrency(remaining)}\n")
    sb.append("----------------------------\n")
    sb.append("Smart Cash Counting Made Simple • Xcash")
    return sb.toString()
}

fun shareText(context: Context, text: String) {
    try {
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "Xcash Summary Report")
        shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show()
    }
}
