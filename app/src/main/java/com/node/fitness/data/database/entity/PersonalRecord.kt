package com.node.fitness.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("sessionId"), Index("exerciseName")]
)
data class PersonalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseName: String,
    val prType: String = PR_TYPE_VOLUME,
    val maxVolume: Double,
    val weight: Double,
    val reps: Int,
    val estimated1RM: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val sessionId: Long? = null,
    val exerciseCatalogId: Long? = null
) {
    companion object {
        const val PR_TYPE_VOLUME = "VOLUME"
        const val PR_TYPE_WEIGHT = "WEIGHT"
        const val PR_TYPE_REPS = "REPS"
        const val PR_TYPE_1RM = "1RM"
    }
}
