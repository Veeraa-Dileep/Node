package com.node.fitness.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_catalog")
data class ExerciseCatalog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isUserCreated: Boolean = false
)
