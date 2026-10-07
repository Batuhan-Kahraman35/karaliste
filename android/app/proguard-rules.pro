# Proje özel ProGuard kuralları

# security-crypto (Tink): yalnızca derleme zamanı anotasyonları, çalışma zamanında yok
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
