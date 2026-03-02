package com.node.fitness.data.database

import com.node.fitness.data.database.dao.ExerciseCatalogDao
import com.node.fitness.data.database.entity.ExerciseCatalog
import com.node.fitness.data.database.entity.ExerciseMuscleMap
import com.node.fitness.data.database.entity.MuscleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ExerciseSeeder {

    fun seed(dao: ExerciseCatalogDao, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val existing = dao.getAllMuscleGroupsSync()
            if (existing.isNotEmpty()) return@launch

            val groups = mapOf(
                "Chest" to 1L,
                "Back" to 2L,
                "Shoulders" to 3L,
                "Arms" to 4L,
                "Legs" to 5L,
                "Core" to 6L,
                "Full Body" to 7L
            )

            groups.forEach { (name, _) ->
                dao.insertMuscleGroup(MuscleGroup(name = name))
            }

            val allGroups = dao.getAllMuscleGroupsSync()
            val groupMap = allGroups.associateBy { it.name }

            val exercises = mapOf(
                "Chest" to listOf(
                    "Barbell Bench Press", "Incline Barbell Bench Press", "Decline Barbell Bench Press",
                    "Dumbbell Bench Press", "Incline Dumbbell Bench Press", "Dumbbell Flyes",
                    "Cable Crossover", "Chest Dip", "Push-Up", "Machine Chest Press"
                ),
                "Back" to listOf(
                    "Barbell Row", "Dumbbell Row", "Pull-Up", "Chin-Up", "Lat Pulldown",
                    "Seated Cable Row", "T-Bar Row", "Face Pull", "Deadlift", "Rack Pull"
                ),
                "Shoulders" to listOf(
                    "Overhead Press", "Dumbbell Shoulder Press", "Lateral Raise",
                    "Front Raise", "Reverse Fly", "Arnold Press", "Upright Row",
                    "Cable Lateral Raise"
                ),
                "Arms" to listOf(
                    "Barbell Curl", "Dumbbell Curl", "Hammer Curl", "Preacher Curl",
                    "Concentration Curl", "Tricep Pushdown", "Overhead Tricep Extension",
                    "Skull Crusher", "Close-Grip Bench Press", "Dip"
                ),
                "Legs" to listOf(
                    "Barbell Squat", "Front Squat", "Leg Press", "Romanian Deadlift",
                    "Leg Curl", "Leg Extension", "Bulgarian Split Squat", "Calf Raise",
                    "Hip Thrust", "Goblet Squat", "Lunge", "Hack Squat"
                ),
                "Core" to listOf(
                    "Plank", "Hanging Leg Raise", "Cable Crunch", "Ab Wheel Rollout",
                    "Russian Twist", "Decline Sit-Up"
                ),
                "Full Body" to listOf(
                    "Clean and Press", "Thruster", "Burpee", "Kettlebell Swing"
                )
            )

            exercises.forEach { (groupName, exerciseNames) ->
                val groupId = groupMap[groupName]?.id ?: return@forEach
                exerciseNames.forEach { exerciseName ->
                    val exerciseId = dao.insertExerciseCatalog(
                        ExerciseCatalog(name = exerciseName)
                    )
                    if (exerciseId > 0) {
                        dao.insertExerciseMuscleMap(
                            ExerciseMuscleMap(exerciseCatalogId = exerciseId, muscleGroupId = groupId)
                        )
                    }
                }
            }
        }
    }
}
