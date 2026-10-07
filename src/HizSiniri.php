<?php
declare(strict_types=1);

namespace Karaliste;

// Kaba kuvvet ve kötüye kullanıma karşı işlem bazlı deneme sınırı (limitler: dbo.HizSinirlari)
final class HizSiniri
{
    private const TEMIZLIK_OLASILIGI = 100;   // ~her 100 istekte bir eski kayıtlar silinir
    private const TEMIZLIK_PARTI = 1000;

    // Limit aşılmışsa 429 döner ve istek sonlanır.
    // Sınır altyapısında hata olursa istek engellenmez (giriş/kayıt çalışmaya devam eder), hata loglanır.
    public static function kontrol(string $islem, ?string $eposta = null): void
    {
        try {
            $bekle = self::asim($islem, $eposta);
        } catch (\PDOException $e) {
            error_log('[Karaliste HizSiniri] ' . $e->getMessage());
            return;
        }

        if ($bekle !== null) {
            $dakika = max(1, (int) ceil($bekle / 60));
            header('Retry-After: ' . max(1, $bekle));
            Yanit::hata("Çok fazla deneme yapıldı. Lütfen $dakika dakika sonra tekrar deneyin.", 429);
        }
    }

    public static function kaydet(string $islem, ?string $eposta, bool $basarili): void
    {
        try {
            self::ekle($islem, $eposta, $basarili);
        } catch (\PDOException $e) {
            error_log('[Karaliste HizSiniri] ' . $e->getMessage());
        }
    }

    // Limit aşılmışsa beklenecek saniye, aşılmamışsa null
    private static function asim(string $islem, ?string $eposta): ?int
    {
        $sinir = Veritabani::satir(
            'SELECT HizSinirlari_IpLimit, HizSinirlari_EpostaLimit, HizSinirlari_PencereDakika, HizSinirlari_SadeceHatali
             FROM dbo.HizSinirlari WHERE HizSinirlari_Islem = ? AND Durum = 1',
            [$islem]
        );
        if ($sinir === null) {
            return null;
        }

        $pencere = (int) $sinir['HizSinirlari_PencereDakika'];
        $sadeceHatali = (bool) $sinir['HizSinirlari_SadeceHatali'];

        $bekle = self::bekleme($islem, 'HizSiniriDenemeleri_Ip', Istek::ip(), (int) $sinir['HizSinirlari_IpLimit'], $pencere, $sadeceHatali);
        if ($bekle === null && $eposta !== null && $sinir['HizSinirlari_EpostaLimit'] !== null) {
            $bekle = self::bekleme($islem, 'HizSiniriDenemeleri_Eposta', $eposta, (int) $sinir['HizSinirlari_EpostaLimit'], $pencere, $sadeceHatali);
        }
        return $bekle;
    }

    private static function ekle(string $islem, ?string $eposta, bool $basarili): void
    {
        Veritabani::calistir(
            'INSERT INTO dbo.HizSiniriDenemeleri
                (HizSiniriDenemeleri_Islem, HizSiniriDenemeleri_Ip, HizSiniriDenemeleri_Eposta, HizSiniriDenemeleri_Basarili)
             VALUES (?, ?, ?, ?)',
            [$islem, Istek::ip(), $eposta, $basarili ? 1 : 0]
        );

        if (random_int(1, self::TEMIZLIK_OLASILIGI) === 1) {
            Veritabani::calistir(
                'DELETE TOP (' . self::TEMIZLIK_PARTI . ') FROM dbo.HizSiniriDenemeleri
                 WHERE OlusturmaTarihi < DATEADD(DAY, -CAST(? AS INT), GETDATE())',
                [Ayarlar::sayi('hiz_siniri_saklama_gun', 7)]
            );
        }
    }

    // Limit dolmuşsa en eski denemenin pencereden çıkmasına kalan saniye, dolmamışsa null
    private static function bekleme(string $islem, string $kolon, string $deger, int $limit, int $pencere, bool $sadeceHatali): ?int
    {
        $s = Veritabani::satir(
            "SELECT COUNT(*) AS Sayi,
                    DATEDIFF(SECOND, GETDATE(), DATEADD(MINUTE, CAST(? AS INT), MIN(OlusturmaTarihi))) AS Kalan
             FROM dbo.HizSiniriDenemeleri
             WHERE HizSiniriDenemeleri_Islem = ? AND $kolon = ?
               AND OlusturmaTarihi > DATEADD(MINUTE, -CAST(? AS INT), GETDATE())"
               . ($sadeceHatali ? ' AND HizSiniriDenemeleri_Basarili = 0' : ''),
            [$pencere, $islem, $deger, $pencere]
        );
        return ((int) $s['Sayi'] >= $limit) ? (int) $s['Kalan'] : null;
    }
}
