-keep class com.bloodbridge.app.data.api.models.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
