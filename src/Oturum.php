<?php
declare(strict_types=1);

namespace Karaliste;

final class Oturum
{
    // Düz token yalnızca istemciye bir kez döner; DB'de SHA-256 hash'i tutulur
    public static function olustur(int $kullaniciId, ?string $cihazAdi): string
    {
        $token = bin2hex(random_bytes(32));
        $gun = Ayarlar::sayi('oturum_gecerlilik_gun', 90);

        Veritabani::calistir(
            'INSERT INTO dbo.Oturumlar
                (Oturumlar_Kullanicilar_id, Oturumlar_TokenHash, Oturumlar_CihazAdi, Oturumlar_SonErisimTarihi,
                 Oturumlar_SonKullanmaTarihi, OlusturanKullanici)
             VALUES (?, ?, ?, GETDATE(), DATEADD(DAY, CAST(? AS INT), GETDATE()), ?)',
            [$kullaniciId, hash('sha256', $token), $cihazAdi, $gun, $kullaniciId]
        );

        return $token;
    }

    // Geçerli oturumun kullanıcı id'si; yoksa 401 döner
    public static function dogrula(): int
    {
        $token = Istek::token();
        if ($token === null) {
            Yanit::hata('Oturum bulunamadı.', 401);
        }

        $oturum = Veritabani::satir(
            'SELECT o.Oturumlar_id, o.Oturumlar_Kullanicilar_id
             FROM dbo.Oturumlar o
             INNER JOIN dbo.Kullanicilar k ON k.Kullanicilar_id = o.Oturumlar_Kullanicilar_id AND k.Durum = 1
             WHERE o.Oturumlar_TokenHash = ? AND o.Durum = 1 AND o.Oturumlar_SonKullanmaTarihi > GETDATE()',
            [hash('sha256', $token)]
        );
        if ($oturum === null) {
            Yanit::hata('Oturum süresi dolmuş veya geçersiz.', 401);
        }

        Veritabani::calistir(
            'UPDATE dbo.Oturumlar SET Oturumlar_SonErisimTarihi = GETDATE() WHERE Oturumlar_id = ?',
            [$oturum['Oturumlar_id']]
        );

        return (int) $oturum['Oturumlar_Kullanicilar_id'];
    }

    public static function kapat(): void
    {
        $token = Istek::token();
        if ($token !== null) {
            Veritabani::calistir(
                'UPDATE dbo.Oturumlar SET Durum = 0, GuncellemeTarihi = GETDATE() WHERE Oturumlar_TokenHash = ?',
                [hash('sha256', $token)]
            );
        }
    }

    public static function tumunuKapat(int $kullaniciId): void
    {
        Veritabani::calistir(
            'UPDATE dbo.Oturumlar SET Durum = 0, GuncellemeTarihi = GETDATE(), GuncelleyenKullanici = ?
             WHERE Oturumlar_Kullanicilar_id = ? AND Durum = 1',
            [$kullaniciId, $kullaniciId]
        );
    }
}
