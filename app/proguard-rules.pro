# ProGuard / R8 Shrinking & Optimization Rules for WSAMat Personal Safety Hub

# Keep WSAMat data models, config, receivers, services, and custom views
-keep class com.example.wsamat.data.** { *; }
-keep class com.example.wsamat.config.** { *; }
-keep class com.example.wsamat.receiver.** { *; }
-keep class com.example.wsamat.service.** { *; }
-keep class com.example.wsamat.views.** { *; }

# Keep Play Services Location components
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**
