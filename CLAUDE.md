# Karaliste

Android arama engelleme uygulaması + PHP API + tanıtım sitesi. APK olarak dağıtılır (Google Play'de değil).
Sunucuya özel yollar ve komutlar: `CLAUDE.local.md` (git dışı).

- Site: https://karaliste.bivora.com.tr
- GitHub: https://github.com/Batuhan-Kahraman35/karaliste (public, remote `origin`)
- Android paket adı: `com.bivora.karaliste` (değiştirilemez)

## Yapı

```text
android/          Kotlin + Jetpack Compose uygulaması (Room, WorkManager, security-crypto)
httpdocs/         Web kök dizini (Plesk belge kökü = karaliste.bivora.com.tr/httpdocs)
  index.php       Tanıtım sayfası
  gizlilik/       KVKK gizlilik politikası
  indir/          APK indirme (dosyayı surumler/ altından okur)
  api/            JSON API yönlendiricisi (index.php + URL Rewrite web.config)
  assets/, img/   site.css, Bivora logoları, uygulama ikonu SVG
src/              PHP sınıfları (PSR-4 Karaliste\), web'den erişilemez
  Api/            HesapApi, KaralisteApi, SurumApi
  Web/Sablon.php  Site şablonu ve güvenli ayar okuma
config/           proje.sql, versiyon.sql, veritabani.php (git dışı), veritabani.example.php
surumler/         Dağıtılan APK'lar (git dışı, web dışı)
temp/             Geçici SQL/test dosyaları
```

## Temel kararlar

- **Kapsam:** Yalnızca arama engelleme. SMS engelleme bilinçli olarak yapılmadı (varsayılan SMS uygulaması olmayı gerektirir).
- **Engelleme:** `CallScreeningService` + `ROLE_CALL_SCREENING`, minSdk 29. Varsayılan arama uygulaması olmadığı için Android rehberdeki numaraları servise iletmez.
- **Eşleştirme tipleri** (`EslesmeTipleri` tablosu, id'ler kodda sabit): 1 Tam, 2 Başlayan (önek), 3 Desen (`?` = tek rakam; `x` ve `*` da kabul edilir, desen 10 hane olmalı).
- **Numara sadeleştirme** (`NumaraYardimci`): `00`, baştaki `0` ve `90` ülke kodu atılır → TR numarası 10 hane. Desen eşleşmesi SQLite `GLOB` ile.
- **Üyelik isteğe bağlı:** Üye değilse liste yalnızca cihazda. Giriş yapınca yerel liste hesaba yüklenir, çıkışta liste cihazda kalır.
- **Senkron:** Kayıtlar `Guid` ile eşleşir. Silme `Silindi` işaretiyle (tombstone) yapılır. İstemci yalnızca `Senkronlandi = 0` olanları gönderir. Sunucu `sonSenkron` sonrası değişenleri döner, `sunucuZamani` değişiklikler yazılmadan önce alınır. Aynı numara+tip başka bir guid ile gelirse sunucu kopyayı `silindi` olarak yazar ve istemci kendi kopyasını siler.
- **Engellenen aramalar** yalnızca cihazda tutulur (son 1000 kayıt), sunucuya asla gönderilmez. Gizlilik metni buna dayanıyor.
- **Kimlik doğrulama:** Bearer token (64 hex). DB'de yalnızca SHA-256 hash'i tutulur. Şifreler ve sıfırlama kodları bcrypt ile saklanır. Token cihazda `EncryptedSharedPreferences` ile şifreli tutulur.
- **Uygulama içi güncelleme:** `/api/surum` → APK indirilir → SHA-256 doğrulanır → `PackageInstaller` oturumu (Android 12+ için `USER_ACTION_NOT_REQUIRED`). Günlük WorkManager kontrolü ve bildirim. `uygulama_min_surum_kodu` altındaki sürümlerde zorunlu güncelleme ekranı.
- **Tema:** Bivora "03 Orbit" renkleri (`#4B2AE8` → `#FF2E93`, vurgu `#FFC93C`). Dinamik renk kapalı.
- **Tarih/saat:** DB `GETDATE()` kullanılır (sunucu +03:00). PHP saat dilimi `httpdocs/.user.ini` içinde.
- **Hız sınırı** (`HizSiniri`): `kayit`, `giris`, `sifre-sifirla-iste`, `sifre-sifirla` için IP ve e-posta bazlı; limitler `HizSinirlari` tablosunda, aşımda 429 + `Retry-After`. Altyapı hatasında istek engellenmez (fail-open, loglanır).
- **Cloudflare:** Site proxy arkasında, SSL modu **Full (strict)**. Gerçek IP `Istek::ip()`: `REMOTE_ADDR` `GuvenilirProxyler` aralığındaysa `CF-Connecting-IP`, değilse `REMOTE_ADDR`.

## Veritabanı (MSSQL, `karaliste_DB`)

Tablolar: `Ayarlar`, `Kullanicilar`, `Oturumlar`, `SifreSifirlama`, `EslesmeTipleri`, `Karaliste`, `HizSinirlari`, `HizSiniriDenemeleri`, `GuvenilirProxyler`. Şema: `config/proje.sql`.

`Ayarlar` anahtarları:
- SMTP: `smtp_sunucu`, `smtp_port`, `smtp_guvenlik`, `smtp_kullanici`, `smtp_sifre`, `smtp_gonderen`, `smtp_gonderen_ad`
- Güvenlik: `oturum_gecerlilik_gun`, `sifirlama_kod_dakika`, `sifirlama_max_deneme`, `hiz_siniri_saklama_gun`
- Sürüm: `uygulama_surum`, `uygulama_surum_kodu`, `uygulama_min_surum_kodu`, `uygulama_surum_notlari` (satır başına bir madde), `uygulama_surum_tarihi`, `uygulama_apk_dosya`, `uygulama_min_android`
- Site: `site_indirme_acik` (1 = sitede indirme butonu), `github_adres` (alt bilgi ve gizlilik sayfasındaki kaynak kod bağlantısı), `gizlilik_guncelleme_tarihi`, `veri_sorumlusu`, `iletisim_eposta`
- Değeri `CHANGE_ME` olan ayar sitede doldurulmamış sayılır.

Android yerel Room DB sürüm 4: `Karaliste`, `EslesmeTipleri`, `EngellenenAramalar`. Geçişler `VeriTabani.kt` içinde (1→2→3→4). Şema değişirse yeni `Migration` yazılmalı, `fallbackToDestructiveMigration` kullanılmaz.

## API (`/api/...`, JSON `{basarili, mesaj?, veri?}`)

`POST kayit`, `POST giris`, `POST cikis`, `POST sifre-sifirla-iste`, `POST sifre-sifirla`, `GET eslesme-tipleri`, `POST senkron` (token gerekir), `GET surum`.

## Yeni sürüm yayınlama

1. `android/app/build.gradle.kts` içinde `versionCode` +1 ve `versionName` güncellenir.
2. `gradlew assembleRelease` çalıştırılır (imza `android/keystore.properties` üzerinden).
3. APK `surumler/karaliste-<sürüm>.apk` olarak kopyalanır.
4. `Ayarlar` tablosunda `uygulama_surum_kodu`, `uygulama_surum`, `uygulama_apk_dosya`, `uygulama_surum_tarihi` ve `uygulama_surum_notlari` güncellenir.
5. `GET /api/surum` ile SHA-256 değerinin indirilen dosyayla aynı olduğu kontrol edilir.

İmza anahtarı `android/karaliste-release.jks` (+ `keystore.properties`) kaybolursa mevcut kullanıcılara güncelleme gönderilemez. İki dosya da git dışında; ayrıca yedekte olmalı.

## Bilinen tuzaklar

- PDO `sqlsrv` parametreleri nvarchar gönderir: `DATEADD` gibi sayı bekleyen yerlerde `CAST(? AS INT)` kullan. Tarih parametrelerinde `CONVERT(DATETIME, ?, 121)` kullan (dil ayarına göre gün/ay karışır).
- `sqlcmd` ile filtreli index'li tablolara yazarken `-I` (QUOTED_IDENTIFIER) şart.
- PowerShell'de `sqlcmd -Q` içinde çift tırnak argümanı bozar; sorguyu `temp/` altında dosyaya yazıp `-i` ile çalıştır.
- R8: Tink için `-dontwarn com.google.errorprone.annotations.**` ve `javax.annotation.**` kuralları gerekli (`proguard-rules.pro`).
- IIS varsayılan belge sırası `index.html` dosyasını `index.php`'den önce açar; `httpdocs` içinde `index.html` olmamalı.
- Android `org.json` null değerini `"null"` metni olarak döndürür: `metinVeyaNull()` yardımcısını kullan.
- Proje klasörünün sahibi Plesk kullanıcısı: git için `safe.directory` tanımlı.
- Cloudflare SSL modu **Flexible** yapılırsa Plesk'in HTTP→HTTPS yönlendirmesi sonsuz döngüye girer (site kapanır). Mod zone geneli: tüm bivora.com.tr alt alan adlarını etkiler.
- Sunucu kendi alan adını Cloudflare yerine kendi IP'sine çözer; sunucudan Cloudflare üzerinden test için `curl --resolve karaliste.bivora.com.tr:443:188.114.96.7` kullan.
- Cloudflare IP aralıkları değişirse `GuvenilirProxyler` güncellenmeli (kaynak: cloudflare.com/ips-v4, /ips-v6); yoksa o aralıktan gelen tüm kullanıcılar tek IP sayılır.

## Açık işler

- [ ] Uygulama içi güncelleme cihazda çalıştı (1.4.1 yayında, site 2026-10-07'de yayına alındı). Sessiz kurulum (onaysız 2. güncelleme) henüz doğrulanmadı.
- [ ] `surumler/` altındaki eski APK'lar (1.2.0, 1.3.0) silinebilir
- [ ] Değerlendirilecek: Play Protect inceleme başvurusu, Android geliştirici doğrulaması, hesap silme ekranı, yönetim paneli
