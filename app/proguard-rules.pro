# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-verbose

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve custom application classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Room
-keep class androidx.room.** { *; }
-keepclasseswithmembernames class androidx.room.** { *; }

# Keep Kotlin
-keep class kotlin.** { *; }
-keepclasseswithmembernames class kotlin.** { *; }

# Keep Google Gson
-keep class com.google.gson.** { *; }
-keepclasseswithmembernames class com.google.gson.** { *; }

# Keep ExoPlayer
-keep class androidx.media3.** { *; }
-keepclasseswithmembernames class androidx.media3.** { *; }

# Preserve enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
