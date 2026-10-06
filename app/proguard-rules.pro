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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# --- Atributos necesarios para anotaciones, genéricos y serialización ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations

# --- kotlinx.serialization: las claves de navegación (NavKey) se guardan y restauran por
# serializador, también tras morir el proceso ---
-keepclassmembers class com.mypeals.** {
    *** Companion;
}
-keepclasseswithmembers class com.mypeals.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mypeals.**$$serializer { *; }
-keepnames class com.mypeals.ui.** implements androidx.navigation3.runtime.NavKey

# --- AppFunctions (Gemini): el sistema y la librería localizan las clases generadas por nombre ---
-keep class com.mypeals.appfunctions.** { *; }
-keep class androidx.appfunctions.internal.** { *; }
-keep class androidx.appfunctions.service.internal.** { *; }

# --- Receivers de alarmas: los instancia el sistema desde el manifest ---
-keep class com.mypeals.core.data.reminders.*Receiver { *; }

# --- Room: los enums se guardan por nombre (valueOf) ---
-keepclassmembers enum com.mypeals.core.domain.model.* {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
