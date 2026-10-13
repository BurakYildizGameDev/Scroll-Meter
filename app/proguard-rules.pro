# ===================================================================
# SCROLL-METER ENTERPRISE R8 / PROGUARD OPTIMIZATION & RULES (2026)
# ===================================================================

# 1. Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-dontwarn androidx.compose.**

# 2. Dagger-Hilt & Injections
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class androidx.hilt.work.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    @dagger.assisted.AssistedInject <init>(...);
}

# 3. Room Database & SQLite
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# 4. Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses,Signature
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class com.scrollmeter.app.presentation.navigation.** { *; }

# 5. Jetpack DataStore Preferences
-keep class androidx.datastore.preferences.protobuf.** { *; }
-dontwarn androidx.datastore.**

# 6. Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# 7. Timber Logging & Strip Debug Logs in Release
-dontwarn com.jakewharton.timber.**
-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
}
