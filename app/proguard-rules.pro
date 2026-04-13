# Add project specific ProGuard rules here.
# Keep Shizuku classes
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }

# Keep app classes
-keep class com.eightball.pool.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
