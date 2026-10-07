-- Proje: Karaliste
-- Veritabanı değişiklik geçmişi (X.Y.Z)

-- ============================================================
-- Versiyon 1.0.0 - 2026-10-06
-- Proje iskeleti oluşturuldu (henüz DB değişikliği yok)
-- ============================================================

-- ============================================================
-- Versiyon 1.1.0 - 2026-10-07
-- İlk veritabanı şeması (Veritabanı: karaliste_DB, .\MSSQLSERVER2022)
--  * Tablolar: Ayarlar, Kullanicilar, Oturumlar, SifreSifirlama,
--    EslesmeTipleri, Karaliste (CREATE betikleri: config/proje.sql)
--  * Karaliste: filtreli UNIQUE index (kullanıcı + numara + tip, silinenler hariç),
--    senkron için (kullanıcı, GuncellemeTarihi DESC) index'i
--  * EslesmeTipleri sabit verileri: 1 Tam, 2 Başlayan, 3 Desen
--  * Ayarlar: SMTP ve güvenlik anahtarları
--  * Ayarlar: uygulama içi güncelleme ve site anahtarları (aşağıda)
-- ============================================================
USE [karaliste_DB];
GO

INSERT INTO [dbo].[Ayarlar] ([Ayarlar_Anahtar], [Ayarlar_Deger], [Ayarlar_Aciklama])
SELECT y.[Anahtar], y.[Deger], y.[Aciklama]
FROM (VALUES
    ('uygulama_surum',             N'1.4.0',      N'Sitede gösterilen APK sürümü'),
    ('uygulama_surum_tarihi',      N'2026-10-07', N'APK çıkış tarihi (YYYY-AA-GG)'),
    ('uygulama_apk_dosya',         N'CHANGE_ME',  N'surumler/ klasöründeki APK dosya adı; CHANGE_ME iken buton "Çok yakında" görünür'),
    ('uygulama_min_android',       N'10',         N'Desteklenen en düşük Android sürümü'),
    ('gizlilik_guncelleme_tarihi', N'2026-10-07', N'Gizlilik politikası son güncelleme tarihi'),
    ('veri_sorumlusu',             N'CHANGE_ME',  N'KVKK veri sorumlusu unvanı'),
    ('iletisim_eposta',            N'CHANGE_ME',  N'KVKK ve hesap silme talepleri için e-posta'),
    ('uygulama_surum_kodu',        N'5',          N'Yayındaki APK versionCode; uygulama bundan küçükse güncelleme önerir'),
    ('uygulama_min_surum_kodu',    N'1',          N'Bunun altındaki sürümler zorunlu güncelleme ekranı görür'),
    ('uygulama_surum_notlari',     N'',           N'Sürüm notları; her satır bir madde'),
    ('site_indirme_acik',          N'0',          N'1 ise sitede APK indirme butonu görünür')
) AS y ([Anahtar], [Deger], [Aciklama])
WHERE NOT EXISTS (SELECT 1 FROM [dbo].[Ayarlar] a WHERE a.[Ayarlar_Anahtar] = y.[Anahtar]);
GO
