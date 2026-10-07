<?php
declare(strict_types=1);

namespace Karaliste;

use PHPMailer\PHPMailer\PHPMailer;

final class Eposta
{
    public static function gonder(string $alici, string $konu, string $html): void
    {
        $posta = new PHPMailer(true);
        $posta->isSMTP();
        $posta->CharSet  = PHPMailer::CHARSET_UTF8;
        $posta->Host     = (string) Ayarlar::al('smtp_sunucu');
        $posta->Port     = Ayarlar::sayi('smtp_port', 465);
        $posta->SMTPAuth = true;
        $posta->Username = (string) Ayarlar::al('smtp_kullanici');
        $posta->Password = (string) Ayarlar::al('smtp_sifre');
        $posta->Timeout  = 15;

        $posta->SMTPSecure = match (Ayarlar::al('smtp_guvenlik')) {
            'ssl'   => PHPMailer::ENCRYPTION_SMTPS,
            'tls'   => PHPMailer::ENCRYPTION_STARTTLS,
            default => '',
        };

        $posta->setFrom((string) Ayarlar::al('smtp_gonderen'), (string) Ayarlar::al('smtp_gonderen_ad', 'Karaliste'));
        $posta->addAddress($alici);
        $posta->isHTML(true);
        $posta->Subject = $konu;
        $posta->Body    = $html;
        $posta->AltBody = trim(strip_tags(str_replace(['<br>', '</p>'], "\n", $html)));

        $posta->send();
    }
}
