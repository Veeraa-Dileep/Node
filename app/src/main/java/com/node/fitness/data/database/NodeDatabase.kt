package com.node.fitness.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.node.fitness.data.database.dao.BodyDataDao
import com.node.fitness.data.database.dao.ExerciseCatalogDao
import com.node.fitness.data.database.dao.PersonalRecordDao
import com.node.fitness.data.database.dao.WorkoutDao
import com.node.fitness.data.database.entity.*

@Database(
    entities = [
        WorkoutSession::class,
        Exercise::class,
        SetEntry::class,
        PersonalRecord::class,
        BodyWeight::class,
        BodyMeasurement::class,
        ProgressPhoto::class,
        MuscleGroup::class,
        ExerciseCatalog::class,
        ExerciseMuscleMap::class
    ],
    version = 2,
    exportSchema = true
)
abstract class NodeDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun bodyDataDao(): BodyDataDao
    abstract fun exerciseCatalogDao(): ExerciseCatalogDao
}
