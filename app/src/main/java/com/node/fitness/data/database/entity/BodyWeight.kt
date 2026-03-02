package com.node.fitness.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_weight")
data class BodyWeight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Double,
    val date: Long = System.currentTimeMillis()
)
