package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<Calculation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: Calculation): Long

    @Delete
    suspend fun deleteCalculation(calculation: Calculation)

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteCalculationById(id: Int)

    @Query("DELETE FROM calculations")
    suspend fun clearAllCalculations()
}
