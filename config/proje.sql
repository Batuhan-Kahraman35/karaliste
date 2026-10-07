-- Proje: Karaliste
-- Açıklama: Projenin tüm SQL yapısı (tablolar, view'lar vb.). Manuel güncellenir.

-- Veritabanı: karaliste_DB  (Sunucu: .\MSSQLSERVER2022)
USE [karaliste_DB];
GO

/* ===================== Ayarlar ===================== */
CREATE TABLE [dbo].[Ayarlar] (
    [Ayarlar_id]          INT IDENTITY(1,1) NOT NULL CONSTRAINT [PK_Ayarlar] PRIMARY KEY,
    [Ayarlar_Anahtar]     VARCHAR(100)      NOT NULL,
    [Ayarlar_Deger]       NVARCHAR(MAX)     NULL,
    [Ayarlar_Aciklama]    NVARCHAR(255)     NULL,
    [OlusturanKullanici]  INT               NULL,
    [OlusturmaTarihi]     DATETIME          NOT NULL CONSTRAINT [DF_Ayarlar_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici] INT              NULL,
    [GuncellemeTarihi]    DATETIME          NULL,
    [Durum]               BIT               NOT NULL CONSTRAINT [DF_Ayarlar_Durum] DEFAULT (1),
    CONSTRAINT [UQ_Ayarlar_Anahtar] UNIQUE ([Ayarlar_Anahtar])
);
GO

/* ===================== Kullanicilar ===================== */
CREATE TABLE [dbo].[Kullanicilar] (
    [Kullanicilar_id]             INT IDENTITY(1,1) NOT NULL CONSTRAINT [PK_Kullanicilar] PRIMARY KEY,
    [Kullanicilar_Eposta]         NVARCHAR(255)     NOT NULL,
    [Kullanicilar_SifreHash]      VARCHAR(255)      NOT NULL,
    [Kullanicilar_AdSoyad]        NVARCHAR(150)     NULL,
    [Kullanicilar_SonGirisTarihi] DATETIME          NULL,
    [OlusturanKullanici]          INT               NULL,
    [OlusturmaTarihi]             DATETIME          NOT NULL CONSTRAINT [DF_Kullanicilar_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici]        INT               NULL,
    [GuncellemeTarihi]            DATETIME          NULL,
    [Durum]                       BIT               NOT NULL CONSTRAINT [DF_Kullanicilar_Durum] DEFAULT (1),
    CONSTRAINT [UQ_Kullanicilar_Eposta] UNIQUE ([Kullanicilar_Eposta])
);
GO

/* ===================== Oturumlar ===================== */
CREATE TABLE [dbo].[Oturumlar] (
    [Oturumlar_id]                INT IDENTITY(1,1) NOT NULL CONSTRAINT [PK_Oturumlar] PRIMARY KEY,
    [Oturumlar_Kullanicilar_id]   INT               NOT NULL,
    [Oturumlar_TokenHash]         CHAR(64)          NOT NULL,   -- SHA-256 (hex)
    [Oturumlar_CihazAdi]          NVARCHAR(150)     NULL,
    [Oturumlar_SonErisimTarihi]   DATETIME          NULL,
    [Oturumlar_SonKullanmaTarihi] DATETIME          NOT NULL,
    [OlusturanKullanici]          INT               NULL,
    [OlusturmaTarihi]             DATETIME          NOT NULL CONSTRAINT [DF_Oturumlar_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici]        INT               NULL,
    [GuncellemeTarihi]            DATETIME          NULL,
    [Durum]                       BIT               NOT NULL CONSTRAINT [DF_Oturumlar_Durum] DEFAULT (1),
    CONSTRAINT [UQ_Oturumlar_TokenHash] UNIQUE ([Oturumlar_TokenHash]),
    CONSTRAINT [FK_Oturumlar_Kullanicilar] FOREIGN KEY ([Oturumlar_Kullanicilar_id])
        REFERENCES [dbo].[Kullanicilar] ([Kullanicilar_id])
);
CREATE INDEX [IX_Oturumlar_Kullanicilar_id] ON [dbo].[Oturumlar] ([Oturumlar_Kullanicilar_id]);
GO

/* ===================== SifreSifirlama ===================== */
CREATE TABLE [dbo].[SifreSifirlama] (
    [SifreSifirlama_id]                INT IDENTITY(1,1) NOT NULL CONSTRAINT [PK_SifreSifirlama] PRIMARY KEY,
    [SifreSifirlama_Kullanicilar_id]   INT               NOT NULL,
    [SifreSifirlama_KodHash]           VARCHAR(255)      NOT NULL,
    [SifreSifirlama_DenemeSayisi]      INT               NOT NULL CONSTRAINT [DF_SifreSifirlama_DenemeSayisi] DEFAULT (0),
    [SifreSifirlama_KullanildiMi]      BIT               NOT NULL CONSTRAINT [DF_SifreSifirlama_KullanildiMi] DEFAULT (0),
    [SifreSifirlama_SonKullanmaTarihi] DATETIME          NOT NULL,
    [OlusturanKullanici]               INT               NULL,
    [OlusturmaTarihi]                  DATETIME          NOT NULL CONSTRAINT [DF_SifreSifirlama_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici]             INT               NULL,
    [GuncellemeTarihi]                 DATETIME          NULL,
    [Durum]                            BIT               NOT NULL CONSTRAINT [DF_SifreSifirlama_Durum] DEFAULT (1),
    CONSTRAINT [FK_SifreSifirlama_Kullanicilar] FOREIGN KEY ([SifreSifirlama_Kullanicilar_id])
        REFERENCES [dbo].[Kullanicilar] ([Kullanicilar_id])
);
CREATE INDEX [IX_SifreSifirlama_Kullanicilar_id] ON [dbo].[SifreSifirlama] ([SifreSifirlama_Kullanicilar_id]);
GO

/* ===================== EslesmeTipleri ===================== */
CREATE TABLE [dbo].[EslesmeTipleri] (
    [EslesmeTipleri_id]       INT              NOT NULL CONSTRAINT [PK_EslesmeTipleri] PRIMARY KEY,
    [EslesmeTipleri_Ad]       NVARCHAR(50)     NOT NULL,
    [EslesmeTipleri_Aciklama] NVARCHAR(255)    NOT NULL,
    [EslesmeTipleri_Ornek]    NVARCHAR(50)     NOT NULL,
    [EslesmeTipleri_Sira]     INT              NOT NULL,
    [OlusturanKullanici]      INT              NULL,
    [OlusturmaTarihi]         DATETIME         NOT NULL CONSTRAINT [DF_EslesmeTipleri_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici]    INT              NULL,
    [GuncellemeTarihi]        DATETIME         NULL,
    [Durum]                   BIT              NOT NULL CONSTRAINT [DF_EslesmeTipleri_Durum] DEFAULT (1)
);
GO

/* ===================== Karaliste ===================== */
CREATE TABLE [dbo].[Karaliste] (
    [Karaliste_id]                INT IDENTITY(1,1)  NOT NULL CONSTRAINT [PK_Karaliste] PRIMARY KEY,
    [Karaliste_Guid]              UNIQUEIDENTIFIER   NOT NULL,   -- cihazda üretilir
    [Karaliste_Kullanicilar_id]   INT                NOT NULL,
    [Karaliste_Numara]            VARCHAR(20)        NOT NULL,   -- sadeleştirilmiş numara / önek / desen
    [Karaliste_GorunenNumara]     NVARCHAR(50)       NOT NULL,
    [Karaliste_EslesmeTipi_id]    INT                NOT NULL CONSTRAINT [DF_Karaliste_EslesmeTipi_id] DEFAULT (1),
    [Karaliste_Aciklama]          NVARCHAR(255)      NULL,
    [Karaliste_Silindi]           BIT                NOT NULL CONSTRAINT [DF_Karaliste_Silindi] DEFAULT (0),
    [OlusturanKullanici]          INT                NULL,
    [OlusturmaTarihi]             DATETIME           NOT NULL CONSTRAINT [DF_Karaliste_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici]        INT                NULL,
    [GuncellemeTarihi]            DATETIME           NOT NULL CONSTRAINT [DF_Karaliste_GuncellemeTarihi] DEFAULT (GETDATE()),
    [Durum]                       BIT                NOT NULL CONSTRAINT [DF_Karaliste_Durum] DEFAULT (1),
    CONSTRAINT [UQ_Karaliste_Guid] UNIQUE ([Karaliste_Guid]),
    CONSTRAINT [FK_Karaliste_Kullanicilar] FOREIGN KEY ([Karaliste_Kullanicilar_id])
        REFERENCES [dbo].[Kullanicilar] ([Kullanicilar_id]),
    CONSTRAINT [FK_Karaliste_EslesmeTipleri] FOREIGN KEY ([Karaliste_EslesmeTipi_id])
        REFERENCES [dbo].[EslesmeTipleri] ([EslesmeTipleri_id])
);
-- Aynı kullanıcıda aynı numara + tip tekrar edemez (silinenler hariç)
CREATE UNIQUE INDEX [UX_Karaliste_Kullanici_Numara_Tip]
    ON [dbo].[Karaliste] ([Karaliste_Kullanicilar_id], [Karaliste_Numara], [Karaliste_EslesmeTipi_id])
    WHERE [Karaliste_Silindi] = 0;
-- Senkron: "şu tarihten sonra değişenler"
CREATE INDEX [IX_Karaliste_Kullanici_Guncelleme]
    ON [dbo].[Karaliste] ([Karaliste_Kullanicilar_id], [GuncellemeTarihi] DESC) INCLUDE ([Karaliste_id]);
CREATE INDEX [IX_Karaliste_EslesmeTipi_id] ON [dbo].[Karaliste] ([Karaliste_EslesmeTipi_id]);
GO

/* ===================== HizSinirlari ===================== */
CREATE TABLE [dbo].[HizSinirlari] (
    [HizSinirlari_id]            INT IDENTITY(1,1) NOT NULL CONSTRAINT [PK_HizSinirlari] PRIMARY KEY,
    [HizSinirlari_Islem]         VARCHAR(50)       NOT NULL,
    [HizSinirlari_IpLimit]       INT               NOT NULL,
    [HizSinirlari_EpostaLimit]   INT               NULL,       -- NULL: e-posta bazlı sınır yok
    [HizSinirlari_PencereDakika] INT               NOT NULL,
    [HizSinirlari_SadeceHatali]  BIT               NOT NULL CONSTRAINT [DF_HizSinirlari_SadeceHatali] DEFAULT (0),
    [HizSinirlari_Aciklama]      NVARCHAR(255)     NULL,
    [OlusturanKullanici]         INT               NULL,
    [OlusturmaTarihi]            DATETIME          NOT NULL CONSTRAINT [DF_HizSinirlari_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici]       INT               NULL,
    [GuncellemeTarihi]           DATETIME          NULL,
    [Durum]                      BIT               NOT NULL CONSTRAINT [DF_HizSinirlari_Durum] DEFAULT (1),
    CONSTRAINT [UQ_HizSinirlari_Islem] UNIQUE ([HizSinirlari_Islem])
);
GO

/* ===================== HizSiniriDenemeleri ===================== */
CREATE TABLE [dbo].[HizSiniriDenemeleri] (
    [HizSiniriDenemeleri_id]       BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT [PK_HizSiniriDenemeleri] PRIMARY KEY,
    [HizSiniriDenemeleri_Islem]    VARCHAR(50)          NOT NULL,
    [HizSiniriDenemeleri_Ip]       VARCHAR(45)          NOT NULL,
    [HizSiniriDenemeleri_Eposta]   NVARCHAR(255)        NULL,
    [HizSiniriDenemeleri_Basarili] BIT                  NOT NULL CONSTRAINT [DF_HizSiniriDenemeleri_Basarili] DEFAULT (0),
    [OlusturanKullanici]           INT                  NULL,
    [OlusturmaTarihi]              DATETIME             NOT NULL CONSTRAINT [DF_HizSiniriDenemeleri_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici]         INT                  NULL,
    [GuncellemeTarihi]             DATETIME             NULL,
    [Durum]                        BIT                  NOT NULL CONSTRAINT [DF_HizSiniriDenemeleri_Durum] DEFAULT (1)
);
CREATE INDEX [IX_HizSiniriDenemeleri_Islem_Ip]
    ON [dbo].[HizSiniriDenemeleri] ([HizSiniriDenemeleri_Islem], [HizSiniriDenemeleri_Ip], [OlusturmaTarihi] DESC)
    INCLUDE ([HizSiniriDenemeleri_Basarili]);
CREATE INDEX [IX_HizSiniriDenemeleri_Islem_Eposta]
    ON [dbo].[HizSiniriDenemeleri] ([HizSiniriDenemeleri_Islem], [HizSiniriDenemeleri_Eposta], [OlusturmaTarihi] DESC)
    INCLUDE ([HizSiniriDenemeleri_Basarili]);
CREATE INDEX [IX_HizSiniriDenemeleri_OlusturmaTarihi]
    ON [dbo].[HizSiniriDenemeleri] ([OlusturmaTarihi]);
GO

/* ===================== GuvenilirProxyler ===================== */
-- Bu aralıklardan gelen isteklerde gerçek IP CF-Connecting-IP başlığından okunur
CREATE TABLE [dbo].[GuvenilirProxyler] (
    [GuvenilirProxyler_id]        INT IDENTITY(1,1) NOT NULL CONSTRAINT [PK_GuvenilirProxyler] PRIMARY KEY,
    [GuvenilirProxyler_Aralik]    VARCHAR(50)       NOT NULL,   -- CIDR (IPv4 / IPv6)
    [GuvenilirProxyler_Saglayici] NVARCHAR(50)      NOT NULL,
    [OlusturanKullanici]          INT               NULL,
    [OlusturmaTarihi]             DATETIME          NOT NULL CONSTRAINT [DF_GuvenilirProxyler_OlusturmaTarihi] DEFAULT (GETDATE()),
    [GuncelleyenKullanici]        INT               NULL,
    [GuncellemeTarihi]            DATETIME          NULL,
    [Durum]                       BIT               NOT NULL CONSTRAINT [DF_GuvenilirProxyler_Durum] DEFAULT (1),
    CONSTRAINT [UQ_GuvenilirProxyler_Aralik] UNIQUE ([GuvenilirProxyler_Aralik])
);
GO

/* ===================== Sabit veriler ===================== */
INSERT INTO [dbo].[HizSinirlari]
    ([HizSinirlari_Islem], [HizSinirlari_IpLimit], [HizSinirlari_EpostaLimit], [HizSinirlari_PencereDakika], [HizSinirlari_SadeceHatali], [HizSinirlari_Aciklama])
VALUES
    ('giris',              20, 5,    15, 1, N'Hatalı giriş denemeleri'),
    ('kayit',               5, NULL, 60, 0, N'Yeni hesap oluşturma'),
    ('sifre-sifirla-iste',  5, 3,    60, 0, N'Şifre sıfırlama kodu isteği (e-posta gönderir)'),
    ('sifre-sifirla',      10, 10,   15, 1, N'Hatalı sıfırlama kodu denemeleri');

-- Kaynak: https://www.cloudflare.com/ips-v4 ve /ips-v6 (2026-10-07)
INSERT INTO [dbo].[GuvenilirProxyler] ([GuvenilirProxyler_Aralik], [GuvenilirProxyler_Saglayici])
VALUES
    ('173.245.48.0/20', N'Cloudflare'), ('103.21.244.0/22', N'Cloudflare'), ('103.22.200.0/22', N'Cloudflare'),
    ('103.31.4.0/22',   N'Cloudflare'), ('141.101.64.0/18', N'Cloudflare'), ('108.162.192.0/18', N'Cloudflare'),
    ('190.93.240.0/20', N'Cloudflare'), ('188.114.96.0/20', N'Cloudflare'), ('197.234.240.0/22', N'Cloudflare'),
    ('198.41.128.0/17', N'Cloudflare'), ('162.158.0.0/15',  N'Cloudflare'), ('104.16.0.0/13',    N'Cloudflare'),
    ('104.24.0.0/14',   N'Cloudflare'), ('172.64.0.0/13',   N'Cloudflare'), ('131.0.72.0/22',    N'Cloudflare'),
    ('2400:cb00::/32',  N'Cloudflare'), ('2606:4700::/32',  N'Cloudflare'), ('2803:f800::/32',   N'Cloudflare'),
    ('2405:b500::/32',  N'Cloudflare'), ('2405:8100::/32',  N'Cloudflare'), ('2a06:98c0::/29',   N'Cloudflare'),
    ('2c0f:f248::/32',  N'Cloudflare');

INSERT INTO [dbo].[EslesmeTipleri]
    ([EslesmeTipleri_id], [EslesmeTipleri_Ad], [EslesmeTipleri_Aciklama], [EslesmeTipleri_Ornek], [EslesmeTipleri_Sira])
VALUES
    (1, N'Tam',      N'Numaranın tamamı eşleşir',                     N'0532 111 22 33', 1),
    (2, N'Başlayan', N'Bu rakamlarla başlayan tüm numaralar',         N'0850',           2),
    (3, N'Desen',    N'Her ? (veya x, *) tek bir rakam yerine geçer', N'0850 ??? 0971',  3);

INSERT INTO [dbo].[Ayarlar] ([Ayarlar_Anahtar], [Ayarlar_Deger], [Ayarlar_Aciklama])
VALUES
    ('smtp_sunucu',                N'bivora.com.tr',         N'SMTP sunucu adresi'),
    ('smtp_port',                  N'465',                   N'SMTP portu'),
    ('smtp_guvenlik',              N'ssl',                   N'tls / ssl / bos'),
    ('smtp_kullanici',             N'noreply@bivora.com.tr', N'SMTP kullanıcı adı'),
    ('smtp_sifre',                 N'CHANGE_ME',             N'SMTP şifresi'),
    ('smtp_gonderen',              N'noreply@bivora.com.tr', N'Gönderen e-posta adresi'),
    ('smtp_gonderen_ad',           N'Karaliste',             N'Gönderen adı'),
    ('oturum_gecerlilik_gun',      N'90',                    N'Giriş token geçerlilik süresi (gün)'),
    ('sifirlama_kod_dakika',       N'15',                    N'Şifre sıfırlama kodu geçerlilik süresi (dakika)'),
    ('sifirlama_max_deneme',       N'5',                     N'Bir kod için en fazla hatalı deneme'),
    ('uygulama_surum',             N'1.4.0',                 N'Sitede gösterilen APK sürümü'),
    ('uygulama_surum_tarihi',      N'2026-10-07',            N'APK çıkış tarihi (YYYY-AA-GG)'),
    ('uygulama_apk_dosya',         N'CHANGE_ME',             N'surumler/ klasöründeki APK dosya adı; CHANGE_ME iken buton "Çok yakında" görünür'),
    ('uygulama_min_android',       N'10',                    N'Desteklenen en düşük Android sürümü'),
    ('gizlilik_guncelleme_tarihi', N'2026-10-07',            N'Gizlilik politikası son güncelleme tarihi'),
    ('veri_sorumlusu',             N'CHANGE_ME',             N'KVKK veri sorumlusu unvanı'),
    ('iletisim_eposta',            N'CHANGE_ME',             N'KVKK ve hesap silme talepleri için e-posta'),
    ('uygulama_surum_kodu',        N'5',                     N'Yayındaki APK versionCode; uygulama bundan küçükse güncelleme önerir'),
    ('uygulama_min_surum_kodu',    N'1',                     N'Bunun altındaki sürümler zorunlu güncelleme ekranı görür'),
    ('uygulama_surum_notlari',     N'',                      N'Sürüm notları; her satır bir madde'),
    ('site_indirme_acik',          N'0',                     N'1 ise sitede APK indirme butonu görünür'),
    ('github_adres',               N'CHANGE_ME',             N'Kaynak kod deposu; CHANGE_ME iken sitede bağlantı görünmez'),
    ('hiz_siniri_saklama_gun',     N'7',                     N'Hız sınırı deneme kayıtlarının saklanma süresi (gün)');
GO

