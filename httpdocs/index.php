<?php
declare(strict_types=1);

require dirname(__DIR__) . '/vendor/autoload.php';

use Karaliste\Web\Sablon;

ini_set('display_errors', '0');

$surum      = Sablon::ayar('uygulama_surum');
$surumTarih = Sablon::tarih(Sablon::ayar('uygulama_surum_tarihi'));
$apkVar     = Sablon::ayar('uygulama_apk_dosya') !== null && Sablon::ayar('site_indirme_acik') === '1';
$minAndroid = Sablon::ayar('uygulama_min_android') ?? '10';
$tipler     = Sablon::eslesmeTipleri();

Sablon::ust(
    'Karaliste: İstenmeyen aramaları engelleyin',
    'Karaliste, Android için ücretsiz arama engelleme uygulamasıdır. Tam numara, başlayan numara veya desenle engelleyin.'
);
?>

<section class="kahraman">
    <div class="kap">
        <img src="/img/karaliste-ikon.svg" alt="Karaliste" class="kahraman-ikon" width="96" height="96">
        <h1>İstenmeyen aramalara <span>son verin</span></h1>
        <p class="alt-baslik">Karaliste, istemediğiniz numaralardan gelen aramaları telefonunuz çalmadan sessizce engeller.</p>

        <?php if ($apkVar): ?>
            <a href="/indir/" class="buton">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12m0 0-5-5m5 5 5-5M5 21h14"/></svg>
                APK'yı indir
            </a>
            <div class="surum-bilgi">
                <?= $surum ? 'Sürüm ' . Sablon::e($surum) : '' ?>
                <?= $surum && $surumTarih ? ' · ' : '' ?>
                <?= $surumTarih ? Sablon::e($surumTarih) : '' ?>
                · Android <?= Sablon::e($minAndroid) ?> ve üzeri
            </div>
        <?php else: ?>
            <span class="buton pasif">Çok yakında</span>
            <div class="surum-bilgi">Android <?= Sablon::e($minAndroid) ?> ve üzeri</div>
        <?php endif; ?>
    </div>
</section>

<section class="bolum" id="ozellikler">
    <div class="kap">
        <h2>Üç farklı engelleme yöntemi</h2>
        <p class="bolum-aciklama">Tek bir numarayı, bir numara grubunu veya belirli bir kalıba uyan bütün numaraları engelleyin.</p>

        <div class="izgara">
            <?php foreach ($tipler as $tip): ?>
                <div class="kart">
                    <span class="kart-ikon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="m5.6 5.6 12.8 12.8"/></svg>
                    </span>
                    <h3><?= Sablon::e($tip['EslesmeTipleri_Ad']) ?></h3>
                    <p><?= Sablon::e($tip['EslesmeTipleri_Aciklama']) ?></p>
                    <span class="ornek"><?= Sablon::e($tip['EslesmeTipleri_Ornek']) ?></span>
                </div>
            <?php endforeach; ?>
        </div>
    </div>
</section>

<section class="bolum renkli">
    <div class="kap">
        <h2>Neden Karaliste?</h2>
        <p class="bolum-aciklama">Basit, hızlı ve gizliliğinize saygılı.</p>

        <div class="izgara">
            <div class="kart">
                <span class="kart-ikon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
                </span>
                <h3>Üyelik gerekmez</h3>
                <p>Uygulamayı hesap açmadan kullanabilirsiniz. Listeniz yalnızca telefonunuzda saklanır.</p>
            </div>
            <div class="kart">
                <span class="kart-ikon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 0 1-15.5 6.2M3 12a9 9 0 0 1 15.5-6.2"/><path d="M3 20v-5h5M21 4v5h-5"/></svg>
                </span>
                <h3>İsterseniz senkron</h3>
                <p>Hesap açarsanız listeniz yedeklenir ve yeni telefonunuza otomatik olarak aktarılır.</p>
            </div>
            <div class="kart">
                <span class="kart-ikon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3 4 6v6c0 4.5 3.4 8.3 8 9 4.6-.7 8-4.5 8-9V6l-8-3z"/><path d="m9 12 2 2 4-4"/></svg>
                </span>
                <h3>Aramalarınız sizde kalır</h3>
                <p>Gelen aramalar cihazınızda kontrol edilir. Hangi numaranın sizi aradığı hiçbir zaman sunucuya gönderilmez.</p>
            </div>
        </div>
    </div>
</section>

<section class="bolum" id="kurulum">
    <div class="kap">
        <h2>Kurulum</h2>
        <p class="bolum-aciklama">Karaliste Google Play'de değil, doğrudan bu siteden kurulur.</p>

        <ol class="adimlar">
            <li>
                <h3>APK dosyasını indirin</h3>
                <p>Telefonunuzdan bu sayfayı açıp <strong>APK'yı indir</strong> butonuna dokunun.</p>
            </li>
            <li>
                <h3>Kuruluma izin verin</h3>
                <p>İndirilen dosyayı açın. Telefon izin isterse <strong>Ayarlar → Bu kaynaktan izin ver</strong> seçeneğini açıp geri dönün.</p>
                <p><strong>Play Protect</strong> uyarı gösterirse <strong>Daha fazla ayrıntı → Yine de yükle</strong> seçeneğine dokunun. Uygulama Google Play'de olmadığı için bu uyarı normaldir; Karaliste aramalarınızı veya rehberinizi hiçbir yere göndermez.</p>
            </li>
            <li>
                <h3>Arama filtrelemeyi açın</h3>
                <p>Uygulamayı açın, <strong>İzin ver</strong> butonuna dokunun ve Karaliste'yi arama filtreleme uygulaması olarak seçin.</p>
            </li>
            <li>
                <h3>Numara ekleyin</h3>
                <p><strong>+</strong> butonuyla engellemek istediğiniz numarayı, başlangıcı veya deseni ekleyin.</p>
            </li>
        </ol>

        <p class="not"><strong>Not:</strong> Android, rehberinizde kayıtlı numaralardan gelen aramaları filtreleme uygulamalarına iletmez. Engellemek istediğiniz numara rehberinizdeyse önce rehberden silin.</p>
    </div>
</section>

<?php Sablon::alt(); ?>
