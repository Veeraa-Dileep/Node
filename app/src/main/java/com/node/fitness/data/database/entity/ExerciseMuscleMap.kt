package com.node.fitness.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exercise_muscle_map",
    primaryKeys = ["exerciseCatalogId", "muscleGroupId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseCatalog::class,
            parentColumns = ["id"],
            childColumns = ["exerciseCatalogId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["muscleGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseCatalogId"), Index("muscleGroupId")]
)
data class ExerciseMuscleMap(
    val exerciseCatalogId: Long,
    val muscleGroupId: Long
)
