<?php
declare(strict_types=1);

require dirname(__DIR__, 2) . '/vendor/autoload.php';

use Karaliste\Api\HesapApi;
use Karaliste\Api\KaralisteApi;
use Karaliste\Api\SurumApi;
use Karaliste\Yanit;

ini_set('display_errors', '0');

set_exception_handler(function (Throwable $e): void {
    error_log('[Karaliste API] ' . $e);
    Yanit::hata('Sunucu hatası. Lütfen daha sonra tekrar deneyin.', 500);
});

$rotalar = [
    'POST kayit'              => [HesapApi::class, 'kayit'],
    'POST giris'              => [HesapApi::class, 'giris'],
    'POST cikis'              => [HesapApi::class, 'cikis'],
    'POST hesap-sil'          => [HesapApi::class, 'hesapSil'],
    'POST sifre-sifirla-iste' => [HesapApi::class, 'sifreSifirlaIste'],
    'POST sifre-sifirla'      => [HesapApi::class, 'sifreSifirla'],
    'GET eslesme-tipleri'     => [KaralisteApi::class, 'eslesmeTipleri'],
    'POST senkron'            => [KaralisteApi::class, 'senkron'],
    'GET surum'               => [SurumApi::class, 'bilgi'],
];

// /api/giris -> "giris"
$yol = trim((string) parse_url($_SERVER['REQUEST_URI'] ?? '', PHP_URL_PATH), '/');
$yol = preg_replace('#^api(/|$)#', '', $yol);
$anahtar = ($_SERVER['REQUEST_METHOD'] ?? 'GET') . ' ' . $yol;

if (!isset($rotalar[$anahtar])) {
    Yanit::hata('Uç nokta bulunamadı.', 404);
}

$rotalar[$anahtar]();
