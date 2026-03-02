package com.node.fitness.data.backup

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.node.fitness.data.database.NodeDatabase
import com.node.fitness.data.database.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class BackupResult(
    val success: Boolean,
    val message: String,
    val counts: Map<String, Int> = emptyMap()
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: NodeDatabase
) {
    private val gson = Gson()

    companion object {
        private const val TAG = "BackupManager"
        private const val SCHEMA_VERSION = 2
    }

    suspend fun exportToStream(outputStream: OutputStream): BackupResult = withContext(Dispatchers.IO) {
        try {
            val data = JsonObject()
            data.addProperty("schema_version", SCHEMA_VERSION)

            val workoutDao = database.workoutDao()
            val prDao = database.personalRecordDao()
            val bodyDao = database.bodyDataDao()
            val catalogDao = database.exerciseCatalogDao()

            val sessions = workoutDao.getAllSessionsSync()
            val exercises = workoutDao.getAllExercisesSync()
            val sets = workoutDao.getAllSetsSync()
            val prs = prDao.getAllRecordsSync()
            val weights = bodyDao.getAllWeightsSync()
            val measurements = bodyDao.getAllMeasurementsSync()
            val photos = bodyDao.getAllPhotosSync()
            val muscleGroups = catalogDao.getAllMuscleGroupsSync()
            val catalog = catalogDao.getAllExercisesSync()
            val mappings = catalogDao.getAllMappingsSync()

            data.add("workout_sessions", gson.toJsonTree(sessions))
            data.add("exercises", gson.toJsonTree(exercises))
            data.add("sets", gson.toJsonTree(sets))
            data.add("personal_records", gson.toJsonTree(prs))
            data.add("body_weight", gson.toJsonTree(weights))
            data.add("body_measurements", gson.toJsonTree(measurements))
            data.add("progress_photos", gson.toJsonTree(photos))
            data.add("muscle_groups", gson.toJsonTree(muscleGroups))
            data.add("exercise_catalog", gson.toJsonTree(catalog))
            data.add("exercise_muscle_map", gson.toJsonTree(mappings))

            outputStream.bufferedWriter().use { writer ->
                gson.toJson(data, writer)
            }

            val counts = mapOf(
                "sessions" to sessions.size,
                "exercises" to exercises.size,
                "sets" to sets.size,
                "prs" to prs.size
            )
            Log.i(TAG, "Export complete: $counts")
            BackupResult(true, "Exported successfully", counts)
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            BackupResult(false, "Export failed: ${e.localizedMessage}")
        }
    }

    suspend fun importFromStream(inputStream: InputStream): BackupResult = withContext(Dispatchers.IO) {
        // Phase 1: Parse and validate JSON BEFORE touching the database
        val data: JsonObject
        try {
            data = inputStream.bufferedReader().use { reader ->
                gson.fromJson(reader, JsonObject::class.java)
            } ?: return@withContext BackupResult(false, "File is empty or invalid JSON")
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Import parse failed", e)
            return@withContext BackupResult(false, "Invalid JSON format: ${e.localizedMessage}")
        }

        // Validate required fields exist
        if (!data.has("workout_sessions")) {
            return@withContext BackupResult(false, "Invalid backup file: missing workout_sessions")
        }

        // Phase 2: Deserialize all entities (still no DB writes)
        try {
            val sessions = parseArray<WorkoutSession>(data, "workout_sessions")
            val exercises = parseArray<Exercise>(data, "exercises")
            val sets = parseArray<SetEntry>(data, "sets")
            val prs = parseArray<PersonalRecord>(data, "personal_records")
            val weights = parseArray<BodyWeight>(data, "body_weight")
            val measurements = parseArray<BodyMeasurement>(data, "body_measurements")
            val photos = parseArray<ProgressPhoto>(data, "progress_photos")
            val muscleGroups = parseArray<MuscleGroup>(data, "muscle_groups")
            val catalog = parseArray<ExerciseCatalog>(data, "exercise_catalog")
            val mappings = parseArray<ExerciseMuscleMap>(data, "exercise_muscle_map")

            // Phase 3: Clear and re-insert in a transaction
            database.runInTransaction {
                database.clearAllTables()

                val workoutDao = database.workoutDao()
                val prDao = database.personalRecordDao()
                val bodyDao = database.bodyDataDao()
                val catalogDao = database.exerciseCatalogDao()

                // Insert in FK-safe order
                kotlinx.coroutines.runBlocking {
                    muscleGroups.forEach { catalogDao.insertMuscleGroup(it) }
                    catalog.forEach { catalogDao.insertExerciseCatalog(it) }
                    mappings.forEach { catalogDao.insertExerciseMuscleMap(it) }
                    sessions.forEach { workoutDao.insertSessionRecord(it) }
                    exercises.forEach { workoutDao.insertExerciseRecord(it) }
                    sets.forEach { workoutDao.insertSetRecord(it) }
                    prs.forEach { prDao.insertRecord(it) }
                    weights.forEach { bodyDao.insertWeightRecord(it) }
                    measurements.forEach { bodyDao.insertMeasurementRecord(it) }
                    photos.forEach { bodyDao.insertPhotoRecord(it) }
                }
            }

            val counts = mapOf(
                "sessions" to sessions.size,
                "exercises" to exercises.size,
                "sets" to sets.size,
                "prs" to prs.size
            )
            Log.i(TAG, "Import complete: $counts")
            BackupResult(true, "Imported successfully", counts)
        } catch (e: Exception) {
            Log.e(TAG, "Import failed during DB write", e)
            BackupResult(false, "Import failed: ${e.localizedMessage}")
        }
    }

    private inline fun <reified T> parseArray(data: JsonObject, key: String): List<T> {
        val element = data.get(key) ?: return emptyList()
        return try {
            gson.fromJson(element, Array<T>::class.java)?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse $key, skipping", e)
            emptyList()
        }
    }
}
