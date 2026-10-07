<?php
declare(strict_types=1);

namespace Karaliste\Api;

use Karaliste\Ayarlar;
use Karaliste\Eposta;
use Karaliste\HizSiniri;
use Karaliste\Istek;
use Karaliste\Oturum;
use Karaliste\Veritabani;
use Karaliste\Yanit;

final class HesapApi
{
    private const MIN_SIFRE = 8;
    private const KOD_TEKRAR_SANIYE = 60;
    private const SIFIRLAMA_MESAJI = 'Bu e-posta adresi kayıtlıysa sıfırlama kodu gönderildi.';
    private const KOD_HATA_MESAJI = 'Kod hatalı veya süresi dolmuş.';
    // Rastgele bir değerin hash'i; hiçbir şifreyle eşleşmez
    private const SAHTE_HASH = '$2y$10$1nQl09WNRetG0hnL7.Z2QeZB.vZ2tDQdHr0a.FOux/z1.rrkVnH22';

    public static function kayit(): never
    {
        $eposta  = self::eposta();
        $sifre   = self::yeniSifre('sifre');
        $adSoyad = Istek::metin('adSoyad', false, 150);
        $cihaz   = Istek::metin('cihazAdi', false, 150);

        HizSiniri::kontrol('kayit', $eposta);
        HizSiniri::kaydet('kayit', $eposta, false);

        if (self::kullanici($eposta) !== null) {
            Yanit::hata('Bu e-posta adresiyle zaten bir hesap var.', 409);
        }

        try {
            $id = (int) Veritabani::deger(
                'INSERT INTO dbo.Kullanicilar (Kullanicilar_Eposta, Kullanicilar_SifreHash, Kullanicilar_AdSoyad, Kullanicilar_SonGirisTarihi)
                 OUTPUT INSERTED.Kullanicilar_id
                 VALUES (?, ?, ?, GETDATE())',
                [$eposta, password_hash($sifre, PASSWORD_DEFAULT), $adSoyad]
            );
        } catch (\PDOException $e) {
            // Aynı anda iki kayıt isteği: UQ_Kullanicilar_Eposta
            if (in_array((int) ($e->errorInfo[1] ?? 0), [2601, 2627], true)) {
                Yanit::hata('Bu e-posta adresiyle zaten bir hesap var.', 409);
            }
            throw $e;
        }

        Veritabani::calistir('UPDATE dbo.Kullanicilar SET OlusturanKullanici = ? WHERE Kullanicilar_id = ?', [$id, $id]);

        Yanit::basarili([
            'token'     => Oturum::olustur($id, $cihaz),
            'kullanici' => self::bilgi($id),
        ], 'Hesabınız oluşturuldu.');
    }

    public static function giris(): never
    {
        $eposta = self::eposta();
        $sifre  = (string) Istek::metin('sifre', true, 255);
        $cihaz  = Istek::metin('cihazAdi', false, 150);

        HizSiniri::kontrol('giris', $eposta);

        $k = self::kullanici($eposta);
        // Kullanıcı yoksa da hash doğrulaması yapılır; yanıt süresi e-postanın varlığını ele vermez
        $hash = $k['Kullanicilar_SifreHash'] ?? self::SAHTE_HASH;
        if (!password_verify($sifre, $hash) || $k === null) {
            HizSiniri::kaydet('giris', $eposta, false);
            Yanit::hata('E-posta veya şifre hatalı.', 401);
        }
        HizSiniri::kaydet('giris', $eposta, true);
        if (!$k['Durum']) {
            Yanit::hata('Hesabınız pasif durumda.', 403);
        }

        $id = (int) $k['Kullanicilar_id'];
        if (password_needs_rehash($hash, PASSWORD_DEFAULT)) {
            Veritabani::calistir(
                'UPDATE dbo.Kullanicilar SET Kullanicilar_SifreHash = ? WHERE Kullanicilar_id = ?',
                [password_hash($sifre, PASSWORD_DEFAULT), $id]
            );
        }
        Veritabani::calistir('UPDATE dbo.Kullanicilar SET Kullanicilar_SonGirisTarihi = GETDATE() WHERE Kullanicilar_id = ?', [$id]);

        Yanit::basarili([
            'token'     => Oturum::olustur($id, $cihaz),
            'kullanici' => self::bilgi($id),
        ]);
    }

    public static function cikis(): never
    {
        Oturum::dogrula();
        Oturum::kapat();
        Yanit::basarili([], 'Çıkış yapıldı.');
    }

    public static function sifreSifirlaIste(): never
    {
        $eposta = self::eposta();
        HizSiniri::kontrol('sifre-sifirla-iste', $eposta);
        HizSiniri::kaydet('sifre-sifirla-iste', $eposta, true);
        $k = self::kullanici($eposta);

        // Kayıtlı olmayan e-posta için de aynı yanıt döner
        if ($k === null || !$k['Durum']) {
            Yanit::basarili([], self::SIFIRLAMA_MESAJI);
        }
        $id = (int) $k['Kullanicilar_id'];

        $yeniKodVar = Veritabani::satir(
            'SELECT TOP 1 SifreSifirlama_id FROM dbo.SifreSifirlama
             WHERE SifreSifirlama_Kullanicilar_id = ? AND OlusturmaTarihi > DATEADD(SECOND, CAST(? AS INT), GETDATE())',
            [$id, -self::KOD_TEKRAR_SANIYE]
        );
        if ($yeniKodVar !== null) {
            Yanit::basarili([], self::SIFIRLAMA_MESAJI);
        }

        Veritabani::calistir(
            'UPDATE dbo.SifreSifirlama SET SifreSifirlama_KullanildiMi = 1, GuncellemeTarihi = GETDATE()
             WHERE SifreSifirlama_Kullanicilar_id = ? AND SifreSifirlama_KullanildiMi = 0',
            [$id]
        );

        $kod = str_pad((string) random_int(0, 999999), 6, '0', STR_PAD_LEFT);
        $dakika = Ayarlar::sayi('sifirlama_kod_dakika', 15);

        Veritabani::calistir(
            'INSERT INTO dbo.SifreSifirlama
                (SifreSifirlama_Kullanicilar_id, SifreSifirlama_KodHash, SifreSifirlama_SonKullanmaTarihi, OlusturanKullanici)
             VALUES (?, ?, DATEADD(MINUTE, CAST(? AS INT), GETDATE()), ?)',
            [$id, password_hash($kod, PASSWORD_DEFAULT), $dakika, $id]
        );

        try {
            Eposta::gonder(
                $eposta,
                'Karaliste şifre sıfırlama kodu',
                '<p>Merhaba,</p>'
                . '<p>Karaliste hesabınızın şifre sıfırlama kodu:</p>'
                . '<p style="font-size:28px;font-weight:bold;letter-spacing:6px">' . $kod . '</p>'
                . '<p>Kod ' . $dakika . ' dakika geçerlidir. Bu isteği siz yapmadıysanız bu e-postayı dikkate almayın.</p>'
            );
        } catch (\Throwable $e) {
            error_log('[Karaliste API] E-posta gönderilemedi: ' . $e->getMessage());
            Yanit::hata('E-posta gönderilemedi. Lütfen daha sonra tekrar deneyin.', 503);
        }

        Yanit::basarili([], self::SIFIRLAMA_MESAJI);
    }

    public static function sifreSifirla(): never
    {
        $eposta = self::eposta();
        $kod    = (string) Istek::metin('kod', true, 6);
        $yeni   = self::yeniSifre('yeniSifre');

        HizSiniri::kontrol('sifre-sifirla', $eposta);

        $k = self::kullanici($eposta);
        if ($k === null || !$k['Durum']) {
            HizSiniri::kaydet('sifre-sifirla', $eposta, false);
            Yanit::hata(self::KOD_HATA_MESAJI);
        }
        $id = (int) $k['Kullanicilar_id'];

        $s = Veritabani::satir(
            'SELECT TOP 1 SifreSifirlama_id, SifreSifirlama_KodHash, SifreSifirlama_DenemeSayisi
             FROM dbo.SifreSifirlama
             WHERE SifreSifirlama_Kullanicilar_id = ? AND SifreSifirlama_KullanildiMi = 0 AND Durum = 1
               AND SifreSifirlama_SonKullanmaTarihi > GETDATE()
             ORDER BY SifreSifirlama_id DESC',
            [$id]
        );
        if ($s === null) {
            HizSiniri::kaydet('sifre-sifirla', $eposta, false);
            Yanit::hata(self::KOD_HATA_MESAJI);
        }

        if ($s['SifreSifirlama_DenemeSayisi'] >= Ayarlar::sayi('sifirlama_max_deneme', 5)) {
            Yanit::hata('Çok fazla hatalı deneme yapıldı. Lütfen yeni kod isteyin.', 429);
        }

        if (!password_verify($kod, $s['SifreSifirlama_KodHash'])) {
            Veritabani::calistir(
                'UPDATE dbo.SifreSifirlama SET SifreSifirlama_DenemeSayisi = SifreSifirlama_DenemeSayisi + 1, GuncellemeTarihi = GETDATE()
                 WHERE SifreSifirlama_id = ?',
                [$s['SifreSifirlama_id']]
            );
            HizSiniri::kaydet('sifre-sifirla', $eposta, false);
            Yanit::hata(self::KOD_HATA_MESAJI);
        }

        $db = Veritabani::al();
        $db->beginTransaction();
        try {
            Veritabani::calistir(
                'UPDATE dbo.Kullanicilar SET Kullanicilar_SifreHash = ?, GuncelleyenKullanici = ?, GuncellemeTarihi = GETDATE()
                 WHERE Kullanicilar_id = ?',
                [password_hash($yeni, PASSWORD_DEFAULT), $id, $id]
            );
            Veritabani::calistir(
                'UPDATE dbo.SifreSifirlama SET SifreSifirlama_KullanildiMi = 1, GuncelleyenKullanici = ?, GuncellemeTarihi = GETDATE()
                 WHERE SifreSifirlama_id = ?',
                [$id, $s['SifreSifirlama_id']]
            );
            // Şifre değişince tüm cihazlardaki oturumlar kapanır
            Oturum::tumunuKapat($id);
            $db->commit();
        } catch (\Throwable $e) {
            $db->rollBack();
            throw $e;
        }

        HizSiniri::kaydet('sifre-sifirla', $eposta, true);
        Yanit::basarili([], 'Şifreniz değiştirildi. Yeni şifrenizle giriş yapabilirsiniz.');
    }

    private static function eposta(): string
    {
        $eposta = mb_strtolower((string) Istek::metin('eposta', true, 255));
        if (filter_var($eposta, FILTER_VALIDATE_EMAIL) === false) {
            Yanit::hata('Geçerli bir e-posta adresi girin.');
        }
        return $eposta;
    }

    private static function yeniSifre(string $alan): string
    {
        $sifre = (string) Istek::metin($alan, true, 255);
        if (mb_strlen($sifre) < self::MIN_SIFRE) {
            Yanit::hata('Şifre en az ' . self::MIN_SIFRE . ' karakter olmalı.');
        }
        return $sifre;
    }

    private static function kullanici(string $eposta): ?array
    {
        return Veritabani::satir(
            'SELECT Kullanicilar_id, Kullanicilar_SifreHash, Durum FROM dbo.Kullanicilar WHERE Kullanicilar_Eposta = ?',
            [$eposta]
        );
    }

    private static function bilgi(int $id): array
    {
        $k = Veritabani::satir(
            'SELECT Kullanicilar_id, Kullanicilar_Eposta, Kullanicilar_AdSoyad FROM dbo.Kullanicilar WHERE Kullanicilar_id = ?',
            [$id]
        );
        return [
            'id'      => (int) $k['Kullanicilar_id'],
            'eposta'  => $k['Kullanicilar_Eposta'],
            'adSoyad' => $k['Kullanicilar_AdSoyad'],
        ];
    }
}
