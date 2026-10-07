# Karaliste

Yerel karaliste ve isteğe bağlı üyelik tabanlı senkronizasyon ile Android arama engelleme uygulaması.

[English](README.md)

## Özellikler

- Karalistedeki numaralardan gelen aramaları engeller (`CallScreeningService`, Android 10+)
- Üyelik olmadan çevrimdışı çalışır; liste cihazda tutulur
- Üye olunursa liste sunucu ile senkronize edilir

## Yapı

```text
android/    Kotlin + Jetpack Compose uygulaması
httpdocs/   PHP 8.3 API (web kök dizini)
config/     proje.sql, versiyon.sql
```

## Gereksinimler

- Android 10 (API 29) veya üzeri
- APK olarak dağıtılır (Google Play'de yok)

## Lisans

[MIT](LICENSE)
