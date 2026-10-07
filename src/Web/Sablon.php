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

    // $css: yeni tasarım denemeleri için farklı stil dosyası; $gizli: arama motorlarına kapalı sayfa
    public static function ust(string $baslik, string $aciklama, string $css = '/assets/site.css?v=4', bool $gizli = false): void
    {
        ?><!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= self::e($baslik) ?></title>
    <meta name="description" content="<?= self::e($aciklama) ?>">
    <?php if ($gizli): ?><meta name="robots" content="noindex, nofollow"><?php endif; ?>
    <meta name="theme-color" content="#4B2AE8">
    <link rel="icon" href="/img/karaliste-ikon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="<?= self::e($css) ?>">
</head>
<body>
<header class="ust">
    <div class="kap ust-ic">
        <a href="/" class="marka">
            <img src="/img/karaliste-ikon.svg" alt="" width="32" height="32">
            <span>Karaliste</span>
        </a>
        <nav>
            <a href="/#dene">Dene</a>
            <a href="/#kurulum">Kurulum</a>
            <a href="/#sss">SSS</a>
            <a href="/gizlilik/">Gizlilik</a>
        </nav>
    </div>
</header>
<main>
<?php
    }

    public static function alt(): void
    {
        $github = self::ayar('github_adres');
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
            <?php if ($github): ?>
                <a href="<?= self::e($github) ?>" class="github-baglanti" target="_blank" rel="noopener">
                    <svg viewBox="0 0 16 16" width="16" height="16" fill="currentColor" aria-hidden="true"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0 0 16 8c0-4.42-3.58-8-8-8z"/></svg>
                    GitHub
                </a>
            <?php endif; ?>
            <span>© <?= date('Y') ?> Bivora</span>
        </div>
    </div>
</footer>
</body>
</html>
<?php
    }
}
