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
