package com.node.fitness.di

import android.content.Context
import androidx.room.Room
import com.node.fitness.data.database.ExerciseSeeder
import com.node.fitness.data.database.NodeDatabase
import com.node.fitness.data.database.dao.BodyDataDao
import com.node.fitness.data.database.dao.ExerciseCatalogDao
import com.node.fitness.data.database.dao.PersonalRecordDao
import com.node.fitness.data.database.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NodeDatabase {
        val db = Room.databaseBuilder(
            context,
            NodeDatabase::class.java,
            "node_fitness.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        ExerciseSeeder.seed(
            db.exerciseCatalogDao(),
            CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )

        return db
    }

    @Provides
    fun provideWorkoutDao(database: NodeDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun providePersonalRecordDao(database: NodeDatabase): PersonalRecordDao = database.personalRecordDao()

    @Provides
    fun provideBodyDataDao(database: NodeDatabase): BodyDataDao = database.bodyDataDao()

    @Provides
    fun provideExerciseCatalogDao(database: NodeDatabase): ExerciseCatalogDao = database.exerciseCatalogDao()
}
