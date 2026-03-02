# Add project specific ProGuard rules here.
-keepattributes *Annotation*

# Room — keep database, entities, and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep class com.node.fitness.data.database.entity.** { *; }
-keep class com.node.fitness.data.database.dao.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Gson — keep entity field names for JSON serialization (backup/restore)
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Vico charts
-keep class com.patrykandpatrick.vico.** { *; }

# Keep backup data model for reflection
-keep class com.node.fitness.data.backup.** { *; }
