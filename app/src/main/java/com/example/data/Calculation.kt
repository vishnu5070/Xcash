package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class Calculation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val denominationsJson: String, // format: "500:2,200:1,100:3"
    val subtractAmount: Double,
    val totalNotes: Int,
    val totalCash: Double,
    val remainingBalance: Double,
    val noteMemo: String = ""
)

fun Calculation.getDenominationsMap(): Map<Int, Int> {
    if (denominationsJson.isBlank()) return emptyMap()
    return try {
        denominationsJson.split(",")
            .filter { it.contains(":") }
            .associate {
                val parts = it.split(":")
                parts[0].toInt() to parts[1].toInt()
            }
    } catch (e: Exception) {
        emptyMap()
    }
}

fun Map<Int, Int>.toDenominationsJson(): String {
    return this.entries.joinToString(",") { "${it.key}:${it.value}" }
}
