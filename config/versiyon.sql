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

-- ============================================================
-- Versiyon 1.1.1 - 2026-10-07
-- Ayarlar: github_adres (sitede kaynak kod bağlantısı)
-- ============================================================
USE [karaliste_DB];
GO

IF NOT EXISTS (SELECT 1 FROM [dbo].[Ayarlar] WHERE [Ayarlar_Anahtar] = 'github_adres')
    INSERT INTO [dbo].[Ayarlar] ([Ayarlar_Anahtar], [Ayarlar_Deger], [Ayarlar_Aciklama])
    VALUES ('github_adres', N'https://github.com/Batuhan-Kahraman35/karaliste', N'Kaynak kod deposu; CHANGE_ME iken sitede bağlantı görünmez');
GO

-- ============================================================
-- Versiyon 1.2.0 - 2026-10-07
-- Hız sınırı (rate limit) ve Cloudflare gerçek IP desteği
--  * HizSinirlari (tanım): işlem bazlı IP / e-posta limiti ve pencere
--  * HizSiniriDenemeleri (hareket): deneme kayıtları, hiz_siniri_saklama_gun sonra silinir
--  * GuvenilirProxyler (tanım): Cloudflare IP aralıkları; yalnızca bunlardan gelen
--    isteklerde CF-Connecting-IP başlığına güvenilir
--  * Ayarlar: hiz_siniri_saklama_gun
--  CREATE ve başlangıç verileri: config/proje.sql (HizSinirlari, HizSiniriDenemeleri, GuvenilirProxyler)
-- ============================================================

-- ============================================================
-- Versiyon 1.2.1 - 2026-10-07
-- Uygulama içi hesap silme (POST /api/hesap-sil): hız sınırı tanımı
-- ============================================================
USE [karaliste_DB];
GO

IF NOT EXISTS (SELECT 1 FROM [dbo].[HizSinirlari] WHERE [HizSinirlari_Islem] = 'hesap-sil')
    INSERT INTO [dbo].[HizSinirlari]
        ([HizSinirlari_Islem], [HizSinirlari_IpLimit], [HizSinirlari_EpostaLimit], [HizSinirlari_PencereDakika], [HizSinirlari_SadeceHatali], [HizSinirlari_Aciklama])
    VALUES ('hesap-sil', 10, 5, 15, 1, N'Hesap silmede hatalı şifre denemeleri');
GO

-- ============================================================
-- Versiyon 1.2.2 - 2026-10-07
-- Tanıtım sitesi yeniden tasarlandı (DB değişikliği yok)
--  * Ana sayfa: ekran görüntüleri, eşleştirme deneme kutusu (EslesmeTipleri
--    örneklerini kullanır), SSS, sürüm notları (Ayarlar.uygulama_surum_notlari)
--  * Gizlilik ve indirme sayfaları yeni stile geçti (assets/site.css v4)
-- ============================================================
