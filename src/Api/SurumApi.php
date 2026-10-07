<?php
declare(strict_types=1);

namespace Karaliste\Api;

use Karaliste\Ayarlar;
use Karaliste\Yanit;

final class SurumApi
{
    /*
     * Uygulamanın güncelleme kontrolü. Yayında APK yoksa surumKodu null döner.
     * Yanıt: { surumKodu, surumAdi, minSurumKodu, notlar[], indirmeAdresi, boyut, sha256, tarih }
     */
    public static function bilgi(): never
    {
        $dosya = basename((string) Ayarlar::al('uygulama_apk_dosya', ''));
        $yol = self::apkYolu($dosya);
        $kod = Ayarlar::sayi('uygulama_surum_kodu', 0);

        if ($yol === null || $kod <= 0) {
            Yanit::basarili(['surumKodu' => null]);
        }

        $notlar = preg_split('/\R/', (string) Ayarlar::al('uygulama_surum_notlari', ''));
        $notlar = array_values(array_filter(array_map('trim', $notlar), fn($n) => $n !== ''));

        Yanit::basarili([
            'surumKodu'     => $kod,
            'surumAdi'      => Ayarlar::al('uygulama_surum'),
            'minSurumKodu'  => Ayarlar::sayi('uygulama_min_surum_kodu', 1),
            'notlar'        => $notlar,
            'indirmeAdresi' => 'https://' . $_SERVER['HTTP_HOST'] . '/indir/',
            'boyut'         => filesize($yol),
            'sha256'        => hash_file('sha256', $yol),
            'tarih'         => Ayarlar::al('uygulama_surum_tarihi'),
        ]);
    }

    // surumler/ altındaki geçerli APK'nın tam yolu; yoksa null
    public static function apkYolu(string $dosya): ?string
    {
        if ($dosya === '' || !preg_match('/^[\w.\-]+\.apk$/i', $dosya)) {
            return null;
        }
        $yol = dirname(__DIR__, 2) . '/surumler/' . $dosya;
        return is_file($yol) ? $yol : null;
    }
}
