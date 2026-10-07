# Karaliste

Android call blocker with a local blacklist and optional account-based cloud sync.

[Türkçe](README.tr.md)

## Features

- Blocks incoming calls from blacklisted numbers (`CallScreeningService`, Android 10+)
- Works offline without an account; the blacklist is stored locally
- Optional membership syncs the blacklist with the server

## Structure

```text
android/    Kotlin + Jetpack Compose app
httpdocs/   PHP 8.3 API (web root)
config/     proje.sql, versiyon.sql
```

## Requirements

- Android 10 (API 29) or later
- Distributed as APK (not on Google Play)

## License

[MIT](LICENSE)
