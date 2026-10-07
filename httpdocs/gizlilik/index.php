<?php
declare(strict_types=1);

require dirname(__DIR__, 2) . '/vendor/autoload.php';

use Karaliste\Web\Sablon;

ini_set('display_errors', '0');

$veriSorumlusu = Sablon::ayar('veri_sorumlusu') ?? 'Bivora';
$iletisim      = Sablon::ayar('iletisim_eposta');
$guncelleme    = Sablon::tarih(Sablon::ayar('gizlilik_guncelleme_tarihi'));

Sablon::ust('Gizlilik Politikası · Karaliste', 'Karaliste uygulamasının kişisel verileri nasıl işlediğine dair gizlilik politikası.');
?>

<article class="kap metin">
    <h1>Gizlilik Politikası</h1>
    <?php if ($guncelleme): ?>
        <p class="guncelleme">Son güncelleme: <?= Sablon::e($guncelleme) ?></p>
    <?php endif; ?>

    <p>Bu metin, Karaliste Android uygulamasını ve <strong>karaliste.bivora.com.tr</strong> sunucusunu kullanırken kişisel verilerinizin 6698 sayılı Kişisel Verilerin Korunması Kanunu (KVKK) kapsamında nasıl işlendiğini açıklar.</p>

    <h2>1. Veri sorumlusu</h2>
    <p>Kişisel verileriniz veri sorumlusu sıfatıyla <strong><?= Sablon::e($veriSorumlusu) ?></strong> tarafından işlenir.</p>

    <h2>2. Üye olmadan kullanım</h2>
    <p>Uygulamayı hesap açmadan kullandığınızda karalistenize eklediğiniz numaralar ve açıklamalar <strong>yalnızca telefonunuzda</strong> saklanır. Sunucuya hiçbir veri gönderilmez.</p>

    <h2>3. Gelen aramalar</h2>
    <p>Telefonunuza gelen bir aramanın numarası, karalistenizle eşleşip eşleşmediğinin kontrolü için <strong>yalnızca cihaz üzerinde</strong> kullanılır. Gelen arama numaraları, arama geçmişiniz ve rehberiniz hiçbir koşulda sunucuya gönderilmez ve kaydedilmez.</p>
    <p>Engellenen aramaların listesi (numara, tarih ve eşleşen kural) yalnızca telefonunuzda tutulur, en fazla son 1000 kayıtla sınırlıdır ve uygulama içinden istediğiniz zaman silinebilir.</p>

    <h2>4. Üyelikte işlenen veriler</h2>
    <p>Hesap oluşturmanız halinde aşağıdaki veriler sunucuda saklanır:</p>
    <ul>
        <li><strong>Hesap bilgileri:</strong> e-posta adresi, isteğe bağlı ad soyad, şifrenizin geri döndürülemez özeti (şifrenin kendisi saklanmaz).</li>
        <li><strong>Karaliste:</strong> karalisteye eklediğiniz numaralar, numara başlangıçları ve desenler, açıklamalarınız, açık/kapalı durumları.</li>
        <li><strong>Oturum bilgileri:</strong> giriş yaptığınız cihazın marka ve modeli, giriş ve son erişim zamanları.</li>
        <li><strong>Şifre sıfırlama:</strong> e-postanıza gönderilen doğrulama kodunun geri döndürülemez özeti ve geçerlilik süresi.</li>
    </ul>

    <h2>5. İşleme amaçları ve hukuki sebep</h2>
    <p>Bu veriler; hesabınızın oluşturulması, kimliğinizin doğrulanması, karalistenizin yedeklenmesi ve cihazlarınız arasında senkronize edilmesi ile şifre sıfırlama e-postalarının gönderilmesi amacıyla, KVKK madde 5/2-c (sözleşmenin kurulması ve ifası) hukuki sebebine dayanarak işlenir.</p>

    <h2>6. Aktarım</h2>
    <p>Kişisel verileriniz üçüncü kişilerle paylaşılmaz, satılmaz ve reklam amacıyla kullanılmaz.</p>

    <h2>7. Güvenlik</h2>
    <ul>
        <li>Uygulama ile sunucu arasındaki tüm iletişim HTTPS ile şifrelenir.</li>
        <li>Şifreler ve doğrulama kodları tek yönlü (bcrypt) özet olarak saklanır.</li>
        <li>Giriş anahtarınız telefonunuzda şifreli olarak saklanır; sunucuda yalnızca özeti tutulur.</li>
    </ul>

    <h2>8. Saklama süresi</h2>
    <p>Verileriniz hesabınız açık kaldığı sürece saklanır. Hesabınızın silinmesini talep ettiğinizde hesabınıza ait tüm veriler kalıcı olarak silinir. Uygulamada oturumu kapatmanız sunucudaki verileri silmez.</p>

    <h2>9. Haklarınız</h2>
    <p>KVKK madde 11 uyarınca; verilerinizin işlenip işlenmediğini öğrenme, bilgi talep etme, düzeltilmesini veya silinmesini isteme ve işlemeye itiraz etme haklarına sahipsiniz.</p>

    <h2>10. İletişim ve hesap silme</h2>
    <?php if ($iletisim): ?>
        <p>Haklarınızı kullanmak veya hesabınızın silinmesini talep etmek için kayıtlı e-posta adresinizden <a href="mailto:<?= Sablon::e($iletisim) ?>"><?= Sablon::e($iletisim) ?></a> adresine yazabilirsiniz.</p>
    <?php else: ?>
        <p>Haklarınızı kullanmak veya hesabınızın silinmesini talep etmek için kayıtlı e-posta adresinizden bizimle iletişime geçebilirsiniz.</p>
    <?php endif; ?>
</article>

<?php Sablon::alt(); ?>
