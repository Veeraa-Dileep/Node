package com.node.fitness.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val REMINDER_WEIGHT_KEY = booleanPreferencesKey("reminder_weight")
        val REMINDER_MEASUREMENTS_KEY = booleanPreferencesKey("reminder_measurements")
        val REMINDER_PHOTOS_KEY = booleanPreferencesKey("reminder_photos")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: true  // Default to dark mode
    }

    val reminderWeightFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_WEIGHT_KEY] ?: true
    }

    val reminderMeasurementsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_MEASUREMENTS_KEY] ?: true
    }

    val reminderPhotosFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_PHOTOS_KEY] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setReminderWeight(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_WEIGHT_KEY] = enabled
        }
    }

    suspend fun setReminderMeasurements(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_MEASUREMENTS_KEY] = enabled
        }
    }

    suspend fun setReminderPhotos(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_PHOTOS_KEY] = enabled
        }
    }
}
