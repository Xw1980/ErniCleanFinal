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

# ============================================
# REGLAS PARA EMAIL Y PDF
# ============================================

# Reglas para iText7 (PDF)
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Reglas para JavaMail
-keep class com.sun.mail.** { *; }
-keep class javax.mail.** { *; }
-keep class javax.activation.** { *; }
-dontwarn com.sun.mail.**
-dontwarn javax.mail.**
-dontwarn javax.activation.**

# Reglas para clases personalizadas de email
-keep class com.example.erniclean.EmailConfig { *; }
-keep class com.example.erniclean.EmailSender { *; }
-keep class com.example.erniclean.PdfGenerator { *; }
-keep class com.example.erniclean.AppointmentConfirmationService { *; }

# Reglas para Firebase (por si acaso)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**