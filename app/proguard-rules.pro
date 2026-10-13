# Proguard rules for Scroll-Meter

# Keep Hilt generated classes
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.internal.GeneratedComponent

# Keep Room entities and schemas
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Timber
-dontwarn com.jakewharton.timber.**
