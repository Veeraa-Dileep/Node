package com.node.fitness.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_measurements")
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chest: Double = 0.0,
    val arms: Double = 0.0,
    val waist: Double = 0.0,
    val thighs: Double = 0.0,
    val bodyFatPercentage: Double? = null,
    val date: Long = System.currentTimeMillis()
)
