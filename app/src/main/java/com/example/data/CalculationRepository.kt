package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val calculationDao: CalculationDao) {
    val allCalculations: Flow<List<Calculation>> = calculationDao.getAllCalculations()

    suspend fun insert(calculation: Calculation): Long {
        return calculationDao.insertCalculation(calculation)
    }

    suspend fun delete(calculation: Calculation) {
        calculationDao.deleteCalculation(calculation)
    }

    suspend fun deleteById(id: Int) {
        calculationDao.deleteCalculationById(id)
    }

    suspend fun clearAll() {
        calculationDao.clearAllCalculations()
    }
}
