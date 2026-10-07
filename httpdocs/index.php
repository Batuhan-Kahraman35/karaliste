<?php
declare(strict_types=1);

require dirname(__DIR__) . '/vendor/autoload.php';

use Karaliste\Api\SurumApi;
use Karaliste\Web\Sablon;

ini_set('display_errors', '0');


$surum      = Sablon::ayar('uygulama_surum');
$surumTarih = Sablon::tarih(Sablon::ayar('uygulama_surum_tarihi'));
$apkDosya   = Sablon::ayar('uygulama_apk_dosya');
$apkVar     = $apkDosya !== null && Sablon::ayar('site_indirme_acik') === '1';
$minAndroid = Sablon::ayar('uygulama_min_android') ?? '10';
$github     = Sablon::ayar('github_adres');
$tipler     = Sablon::eslesmeTipleri();
$notlar     = array_values(array_filter(array_map('trim', preg_split('/\R/', (string) Sablon::ayar('uygulama_surum_notlari')))));

$apkYol   = $apkDosya ? SurumApi::apkYolu(basename($apkDosya)) : null;
$apkBoyut = $apkYol ? number_format(filesize($apkYol) / 1048576, 1, ',', '') . ' MB' : null;

Sablon::ust(
    'Karaliste: Android için arama engelleyici',
    'Tek numarayı, 0850 gibi bir başlangıcı veya 0850 ??? 0971 gibi bir kalıbı engelleyin. Ücretsiz, reklamsız, açık kaynak.'
);
?>

<section class="giris-bolum">
    <div class="kap giris-izgara">
        <div>
            <p class="ust-etiket">Android için arama engelleyici</p>
            <h1>Arayıp duran numaraları bir kez ekleyin, telefonunuz <mark>bir daha çalmasın.</mark></h1>
            <p class="giris-metin">
                Tek bir numarayı, <code>0850</code> ile başlayan bütün numaraları ya da <code>0850 ??? 0971</code> gibi
                bir kalıbı engelleyebilirsiniz. Engellenen arama telefonu hiç çaldırmaz, yalnızca uygulamadaki
                listeye düşer.
            </p>

            <div class="indir-satir">
                <?php if ($apkVar): ?>
                    <a href="/indir/" class="buton">APK'yı indir<?= $apkBoyut ? ' <small>(' . Sablon::e($apkBoyut) . ')</small>' : '' ?></a>
                <?php else: ?>
                    <span class="buton pasif">Çok yakında</span>
                <?php endif; ?>
                <?php if ($github): ?>
                    <a href="<?= Sablon::e($github) ?>" class="ikincil" target="_blank" rel="noopener">Kaynak kodu GitHub'da</a>
                <?php endif; ?>
            </div>
            <p class="kucuk-not">
                <?= $surum ? 'Sürüm ' . Sablon::e($surum) : '' ?><?= $surumTarih ? ', ' . Sablon::e($surumTarih) : '' ?>
                · Android <?= Sablon::e($minAndroid) ?> ve üzeri · Ücretsiz, reklam yok
            </p>
        </div>

        <div class="giris-gorsel">
            <figure class="telefon telefon-buyuk">
                <img src="/img/ekran/numara-ekle.jpg" alt="Karaliste uygulamasında numara ekleme penceresi: Tam, Başlayan ve Desen seçenekleri" width="540" height="1038">
            </figure>
            <div class="bildirim bildirim-1" aria-hidden="true">
                <span class="bildirim-ikon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="m5.6 5.6 12.8 12.8"/></svg></span>
                <span><b>0850 333 12 12</b><small>Engellendi · Başlayan: 0850 · 14:02</small></span>
            </div>
            <div class="bildirim bildirim-2" aria-hidden="true">
                <span class="bildirim-ikon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="m5.6 5.6 12.8 12.8"/></svg></span>
                <span><b>0216 999 40 40</b><small>Engellendi · Tam eşleşme · Dün</small></span>
            </div>
        </div>
    </div>
</section>

<div class="serit" aria-hidden="true">
    <div class="serit-ic">
        <?php for ($tekrar = 0; $tekrar < 2; $tekrar++): ?>
            <span><s>0850 333 12 12</s> engellendi</span>
            <span><s>0212 444 90 90</s> engellendi</span>
            <span><s>0850 711 0971</s> engellendi</span>
            <span><s>0544 000 11 22</s> engellendi</span>
            <span><s>0850 255 0971</s> engellendi</span>
            <span><s>0216 999 40 40</s> engellendi</span>
        <?php endfor; ?>
    </div>
</div>

<section class="bolum" id="dene">
    <div class="kap">
        <h2>Hangi arama engellenir?</h2>
        <p class="bolum-metin">Aşağıdaki örnek listeyle bir numara deneyin. Kutu, uygulamadaki eşleştirmenin aynısını kullanır: boşluklar, baştaki 0 ve +90 dikkate alınmaz.</p>

        <div class="deneme panel">
            <div class="deneme-liste">
                <h3>Örnek karaliste</h3>
                <ul id="kurallar">
                    <?php foreach ($tipler as $tip): ?>
                        <li data-tip="<?= Sablon::e($tip['EslesmeTipleri_Ad']) ?>" data-kural="<?= Sablon::e($tip['EslesmeTipleri_Ornek']) ?>">
                            <span class="kural"><?= Sablon::e($tip['EslesmeTipleri_Ornek']) ?></span>
                            <span class="tip"><?= Sablon::e($tip['EslesmeTipleri_Ad']) ?></span>
                            <span class="aciklama"><?= Sablon::e($tip['EslesmeTipleri_Aciklama']) ?></span>
                        </li>
                    <?php endforeach; ?>
                </ul>
            </div>

            <div class="deneme-giris">
                <label for="gelen">Sizi arayan numara</label>
                <input id="gelen" type="tel" inputmode="tel" autocomplete="off" placeholder="0850 222 0971">
                <div class="ornekler">
                    <span>Örnek:</span>
                    <button type="button" data-numara="0850 222 0971">0850 222 0971</button>
                    <button type="button" data-numara="+90 532 111 22 33">+90 532 111 22 33</button>
                    <button type="button" data-numara="0212 555 44 33">0212 555 44 33</button>
                </div>
                <p id="sonuc" class="sonuc" aria-live="polite">Bir numara yazın.</p>
            </div>
        </div>
    </div>
</section>

<section class="bolum ekranlar-bolum">
    <div class="kap">
        <h2>Uygulamanın içi</h2>
        <div class="ekranlar">
            <figure class="telefon">
                <img src="/img/ekran/liste.jpg" alt="Karaliste ana ekranı" width="540" height="1038" loading="lazy">
                <figcaption><strong>Liste.</strong> Eklediğiniz numaraları tek dokunuşla kapatıp açabilir, açıklamaya göre arayabilirsiniz.</figcaption>
            </figure>
            <figure class="telefon">
                <img src="/img/ekran/engellenenler.jpg" alt="Engellenen aramalar ekranı" width="540" height="1038" loading="lazy">
                <figcaption><strong>Engellenenler.</strong> Hangi numaranın ne zaman aradığı ve hangi kurala takıldığı. Bu kayıt telefonunuzdan çıkmaz.</figcaption>
            </figure>
            <figure class="telefon">
                <img src="/img/ekran/giris.jpg" alt="İsteğe bağlı hesap giriş ekranı" width="540" height="1038" loading="lazy">
                <figcaption><strong>Hesap, isterseniz.</strong> Listenizi yedekler ve yeni telefona taşır. Kullanmak için şart değil.</figcaption>
            </figure>
        </div>
    </div>
</section>

<section class="bolum" id="kurulum">
    <div class="kap dar">
        <h2>Kurulum</h2>
        <p class="bolum-metin">Karaliste Google Play'de yok, dosyayı bu siteden indirip kuruyorsunuz. Güncellemeler sonra uygulamanın içinden gelir.</p>
        <ol class="adimlar">
            <li><strong>İndirin.</strong> Telefonunuzdan bu sayfayı açıp <em>APK'yı indir</em>'e dokunun.</li>
            <li><strong>Kurun.</strong> İndirilen dosyayı açın. Telefon izin isterse <em>Ayarlar → Bu kaynaktan izin ver</em>'i açıp geri dönün.
                Play Protect uyarı verirse <em>Daha fazla ayrıntı → Yine de yükle</em>'yi seçin.</li>
            <li><strong>İzin verin.</strong> Uygulamayı açın, <em>İzin ver</em>'e dokunun ve Karaliste'yi arama filtreleme uygulaması olarak seçin.</li>
            <li><strong>Numara ekleyin.</strong> Sağ alttaki <em>+</em> ile engellemek istediğiniz numarayı, başlangıcı veya kalıbı yazın.</li>
        </ol>
    </div>
</section>

<section class="bolum" id="sss">
    <div class="kap dar">
        <h2>Sık sorulanlar</h2>
        <details>
            <summary>Rehberimdeki bir numarayı engelleyemiyorum, neden?</summary>
            <p>Android, rehberde kayıtlı numaralardan gelen aramaları filtreleme uygulamalarına hiç göstermez. Bu numarayı engellemek için önce rehberden silmeniz gerekiyor.</p>
        </details>
        <details>
            <summary>Play Protect "güvenli olmayabilir" diyor.</summary>
            <p>Play Protect, Google Play dışından gelen ve tanımadığı her uygulama için bu uyarıyı gösterir. Uygulamanın kaynak kodu açık<?= $github ? ' (<a href="' . Sablon::e($github) . '" target="_blank" rel="noopener">GitHub</a>)' : '' ?>; neyi okuyup neyi göndermediğini kendiniz inceleyebilirsiniz.</p>
        </details>
        <details>
            <summary>Üye olmam gerekiyor mu?</summary>
            <p>Hayır. Üye olmadan listeniz yalnızca telefonunuzda durur. Hesap açarsanız liste sunucuda yedeklenir; hesabınızı istediğiniz zaman uygulamadan silebilirsiniz.</p>
        </details>
        <details>
            <summary>Arama geçmişim veya rehberim bir yere gönderiliyor mu?</summary>
            <p>Hayır. Gelen numara yalnızca telefonda listenizle karşılaştırılır. Sunucuya giden tek şey, üye olduysanız kendi eklediğiniz karaliste kayıtlarıdır. Ayrıntılar <a href="/gizlilik/">gizlilik politikasında</a>.</p>
        </details>
        <details>
            <summary>SMS de engelliyor mu?</summary>
            <p>Hayır, yalnızca aramaları. SMS engellemek için uygulamanın varsayılan mesajlaşma uygulaması olması gerekir; bunu bilinçli olarak yapmadık.</p>
        </details>
    </div>
</section>

<?php if ($surum && $notlar): ?>
<section class="bolum">
    <div class="kap dar">
        <h2><?= Sablon::e($surum) ?> sürümünde</h2>
        <ul class="notlar">
            <?php foreach ($notlar as $not): ?>
                <li><?= Sablon::e($not) ?></li>
            <?php endforeach; ?>
        </ul>
        <?php if ($github): ?>
            <p class="kucuk-not">Önceki değişiklikler: <a href="<?= Sablon::e($github) ?>/commits/main" target="_blank" rel="noopener">GitHub'daki geçmiş</a></p>
        <?php endif; ?>
    </div>
</section>
<?php endif; ?>

<script>
(() => {
    const giris = document.getElementById('gelen');
    const sonuc = document.getElementById('sonuc');
    const satirlar = [...document.querySelectorAll('#kurallar li')];

    // Android NumaraYardimci ile aynı: 00, baştaki 0 ve 90 ülke kodu atılır
    const ulusal = r => {
        r = r.startsWith('00') ? r.slice(2) : r.startsWith('0') ? r.slice(1) : r;
        return r.startsWith('90') && r.length > 2 ? r.slice(2) : r;
    };
    const sade = s => ulusal(s.replace(/\D/g, ''));
    const desenSade = s => ulusal([...s].map(c => /\d/.test(c) ? c : /[?xX*]/.test(c) ? '?' : '').join(''));

    // Tip adları DB'den gelir; sıra uygulamadaki id sırasıyla aynı (1 Tam, 2 Başlayan, 3 Desen)
    const eslesir = (sira, kural, numara) => {
        if (sira === 0) return numara === sade(kural);
        if (sira === 1) return numara.startsWith(sade(kural));
        const d = desenSade(kural);
        return d.length === numara.length && [...d].every((c, i) => c === '?' || c === numara[i]);
    };

    const guncelle = () => {
        const numara = sade(giris.value);
        satirlar.forEach(li => li.classList.remove('eslesti'));
        sonuc.className = 'sonuc';
        if (numara.length < 3) { sonuc.textContent = 'Bir numara yazın.'; return; }

        const bulunan = satirlar.filter((li, i) => eslesir(i, li.dataset.kural, numara));
        if (bulunan.length) {
            bulunan.forEach(li => li.classList.add('eslesti'));
            const kurallar = bulunan.map(li => `"${li.dataset.kural}" (${li.dataset.tip})`).join(' ve ');
            sonuc.classList.add('engel');
            sonuc.textContent = `Engellenir: ${kurallar} kuralına uyuyor. Telefonunuz çalmaz.`;
        } else {
            sonuc.classList.add('gecer');
            sonuc.textContent = 'Engellenmez: listedeki hiçbir kurala uymuyor. Arama normal şekilde gelir.';
        }
    };

    giris.addEventListener('input', guncelle);
    document.querySelectorAll('[data-numara]').forEach(b => b.addEventListener('click', () => {
        giris.value = b.dataset.numara;
        guncelle();
    }));
})();
</script>

<?php Sablon::alt(); ?>
