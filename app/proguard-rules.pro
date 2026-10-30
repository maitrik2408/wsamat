# ProGuard / R8 Shrinking & Optimization Rules for WSAMat Personal Safety Hub

# Keep WSAMat data models, config, receivers, services, and custom views
-keep class com.kirtiam.wsamat.data.** { *; }
-keep class com.kirtiam.wsamat.config.** { *; }
-keep class com.kirtiam.wsamat.receiver.** { *; }
-keep class com.kirtiam.wsamat.service.** { *; }
-keep class com.kirtiam.wsamat.views.** { *; }

# Keep Play Services Location components
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**
