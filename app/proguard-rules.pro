# =============================================================================
# PROGUARD RULES — Titik Jemput Mitra
# Generated based on libs.versions.toml dependencies
# =============================================================================

# -----------------------------------------------------------------------------
# ANDROID DEFAULT
# -----------------------------------------------------------------------------
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Jangan obfuscate nama class yang muncul di stack trace (untuk Crashlytics)
-renamesourcefileattribute SourceFile

# -----------------------------------------------------------------------------
# KOTLIN
# -----------------------------------------------------------------------------
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Kotlin Parcelize — wajib untuk @Parcelize seperti DataIdentity
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# -----------------------------------------------------------------------------
# RETROFIT 2
# -----------------------------------------------------------------------------
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# -----------------------------------------------------------------------------
# OKHTTP3 + LOGGING INTERCEPTOR
# -----------------------------------------------------------------------------
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okio.** { *; }

# -----------------------------------------------------------------------------
# GSON + CONVERTER GSON
# — Wajib agar @SerializedName dan DataClass tidak di-strip
# -----------------------------------------------------------------------------
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keepattributes *Annotation*

# Jaga semua data class response agar field-nya tidak di-rename oleh obfuscator
# Ini sangat penting untuk DataIdentity, IdentityResponse, dll
-keep class com.id.titikjemputmitra.data.response.** { *; }
-keep class com.id.titikjemputmitra.data.request.** { *; }

# Rule generik untuk semua class yang punya @SerializedName
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-dontwarn com.google.gson.**

# -----------------------------------------------------------------------------
# GLIDE
# -----------------------------------------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}
-dontwarn com.bumptech.glide.**

# -----------------------------------------------------------------------------
# FIREBASE (BOM: Analytics, Crashlytics, Auth, Messaging)
# -----------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Crashlytics — jaga mapping agar stack trace terbaca
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }

# Firebase Auth + Google Sign-In
-keep class com.google.android.libraries.identity.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# -----------------------------------------------------------------------------
# PUSHER
# -----------------------------------------------------------------------------
-keep class com.pusher.** { *; }
-keep interface com.pusher.** { *; }
-keepclassmembers class com.pusher.** { *; }
-dontwarn com.pusher.**

# Pusher menggunakan Gson untuk serialisasi event
-keep class com.google.gson.** { *; }

# -----------------------------------------------------------------------------
# ZXING (QR Code Scanner)
# -----------------------------------------------------------------------------
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }
-dontwarn com.google.zxing.**
-dontwarn com.journeyapps.**

# -----------------------------------------------------------------------------
# CIRCLE IMAGE VIEW
# -----------------------------------------------------------------------------
-keep class de.hdodenhof.circleimageview.** { *; }
-dontwarn de.hdodenhof.circleimageview.**

# -----------------------------------------------------------------------------
# EXIF INTERFACE
# -----------------------------------------------------------------------------
-keep class androidx.exifinterface.** { *; }
-dontwarn androidx.exifinterface.**

# -----------------------------------------------------------------------------
# PLAY SERVICES LOCATION (FusedLocationProvider)
# -----------------------------------------------------------------------------
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# -----------------------------------------------------------------------------
# ANDROID NAVIGATION COMPONENT
# -----------------------------------------------------------------------------
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# -----------------------------------------------------------------------------
# MATERIAL COMPONENTS
# -----------------------------------------------------------------------------
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# -----------------------------------------------------------------------------
# LIFECYCLE (ViewModel, LiveData)
# -----------------------------------------------------------------------------
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <methods>;
}

# -----------------------------------------------------------------------------
# SWIPE REFRESH LAYOUT
# -----------------------------------------------------------------------------
-keep class androidx.swiperefreshlayout.** { *; }
-dontwarn androidx.swiperefreshlayout.**

# -----------------------------------------------------------------------------
# APLIKASI — Jaga semua package utama
# -----------------------------------------------------------------------------
# Sesuaikan package name jika berbeda
-keep class com.id.titikjemputmitra.** { *; }

# Jaga semua Activity, Fragment, Service, BroadcastReceiver
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.fragment.app.Fragment

# Jaga View custom agar XML inflation tidak crash
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    void set*(***);
    *** get*();
}

# -----------------------------------------------------------------------------
# SUPPRESS WARNING UMUM
# -----------------------------------------------------------------------------
-dontwarn javax.annotation.**
-dontwarn sun.misc.**
-dontwarn java.lang.invoke.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Suppress SLF4J warnings (Generated by R8)
-dontwarn org.slf4j.impl.StaticLoggerBinder