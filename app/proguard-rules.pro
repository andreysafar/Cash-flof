# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ru.cashflow.statement.model.** {
    *** Companion;
}
-keepclasseswithmembers class ru.cashflow.statement.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class ru.cashflow.statement.model.**$$serializer { *; }

# VK Ad SDK (myTarget)
-keep class com.my.target.** { *; }
