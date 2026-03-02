package com.node.fitness.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val startTimeMillis: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val notes: String = "",
    val isCompleted: Boolean = false
)
