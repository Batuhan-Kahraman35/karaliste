<?php
declare(strict_types=1);

require dirname(__DIR__, 2) . '/vendor/autoload.php';

use Karaliste\Api\SurumApi;
use Karaliste\Web\Sablon;

ini_set('display_errors', '0');

// APK'lar web kök dizini dışındaki surumler/ klasöründe tutulur; dosya adı Ayarlar tablosundan gelir.
// Uygulama içi güncelleme de bu adresi kullanır; sitedeki buton site_indirme_acik ile ayrıca açılır.
$dosyaAdi = basename((string) Sablon::ayar('uygulama_apk_dosya'));
$yol = SurumApi::apkYolu($dosyaAdi);

if ($yol === null) {
    http_response_code(404);
    Sablon::ust('İndirme bulunamadı · Karaliste', 'Karaliste APK dosyası bulunamadı.');
    ?>
    <section class="kahraman">
        <div class="kap">
            <h1>Dosya şu an hazır değil</h1>
            <p class="alt-baslik">Uygulamanın kurulum dosyası henüz yüklenmedi. Lütfen daha sonra tekrar deneyin.</p>
            <a href="/" class="buton">Ana sayfaya dön</a>
        </div>
    </section>
    <?php
    Sablon::alt();
    exit;
}

header('Content-Type: application/vnd.android.package-archive');
header('Content-Disposition: attachment; filename="' . $dosyaAdi . '"');
header('Content-Length: ' . filesize($yol));
header('Cache-Control: no-cache');
header('X-Content-Type-Options: nosniff');
readfile($yol);
