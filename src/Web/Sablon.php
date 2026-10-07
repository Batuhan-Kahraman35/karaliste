<?php
declare(strict_types=1);

namespace Karaliste\Web;

use Karaliste\Ayarlar;
use Karaliste\Veritabani;

// Tanıtım sitesi için ortak başlık/alt bilgi ve güvenli ayar okuma
final class Sablon
{
    public static function e(?string $metin): string
    {
        return htmlspecialchars((string) $metin, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
    }

    // DB'ye ulaşılamazsa veya değer doldurulmamışsa null döner; sayfa yine açılır
    public static function ayar(string $anahtar): ?string
    {
        try {
            $deger = Ayarlar::al($anahtar);
        } catch (\Throwable $e) {
            error_log('[Karaliste Site] ' . $e->getMessage());
            return null;
        }
        return ($deger === null || $deger === '' || $deger === 'CHANGE_ME') ? null : $deger;
    }

    public static function eslesmeTipleri(): array
    {
        try {
            return Veritabani::satirlar(
                'SELECT EslesmeTipleri_Ad, EslesmeTipleri_Aciklama, EslesmeTipleri_Ornek
                 FROM dbo.EslesmeTipleri WHERE Durum = 1 ORDER BY EslesmeTipleri_Sira'
            );
        } catch (\Throwable $e) {
            error_log('[Karaliste Site] ' . $e->getMessage());
            return [];
        }
    }

    public static function tarih(?string $isoTarih): ?string
    {
        if ($isoTarih === null || !preg_match('/^(\d{4})-(\d{2})-(\d{2})/', $isoTarih, $m)) {
            return null;
        }
        return "$m[3].$m[2].$m[1]";
    }

    public static function ust(string $baslik, string $aciklama): void
    {
        ?><!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= self::e($baslik) ?></title>
    <meta name="description" content="<?= self::e($aciklama) ?>">
    <meta name="theme-color" content="#4B2AE8">
    <link rel="icon" href="/img/karaliste-ikon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="/assets/site.css?v=2">
</head>
<body>
<header class="ust">
    <div class="kap ust-ic">
        <a href="/" class="marka">
            <img src="/img/karaliste-ikon.svg" alt="" width="32" height="32">
            <span>Karaliste</span>
        </a>
        <nav>
            <a href="/#ozellikler">Özellikler</a>
            <a href="/#kurulum">Kurulum</a>
            <a href="/gizlilik/">Gizlilik</a>
        </nav>
    </div>
</header>
<main>
<?php
    }

    public static function alt(): void
    {
        ?>
</main>
<footer class="alt">
    <div class="kap alt-ic">
        <picture>
            <source srcset="/img/bivora-logo-koyu.svg" media="(prefers-color-scheme: dark)">
            <img src="/img/bivora-logo.svg" alt="Bivora" class="bivora-logo" width="140" height="34">
        </picture>
        <div class="alt-baglantilar">
            <a href="/gizlilik/">Gizlilik politikası</a>
            <span>© <?= date('Y') ?> Bivora</span>
        </div>
    </div>
</footer>
</body>
</html>
<?php
    }
}
