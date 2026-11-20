# Proguard rules for Hajiri Book
-keep class com.pkstudy.hajiribook.data.local.entity.** { *; }
-keep class com.pkstudy.hajiribook.data.remote.dto.** { *; }
-keep class com.pkstudy.hajiribook.domain.model.** { *; }
-keep class org.tensorflow.lite.** { *; }
-keepattributes *Annotation*
-dontwarn org.tensorflow.lite.**
