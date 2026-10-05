# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# ---- Card Battle RPG ----
# Firebase Firestore / Realtime Database map these classes by reflection
# (field names + no-arg constructors). Renaming them breaks cloud save,
# rankings, guilds and online battles in release builds.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
-keep class com.pegasus.cardbattlerpg.entity.** { *; }
-keep class com.pegasus.cardbattlerpg.model.** { *; }
-keep class com.pegasus.cardbattlerpg.online.** { *; }

# Keep readable crash stack traces in Play Console (upload mapping.txt automatically via AAB).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
