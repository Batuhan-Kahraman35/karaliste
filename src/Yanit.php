<?php
declare(strict_types=1);

namespace Karaliste;

final class Yanit
{
    public static function basarili(array $veri = [], ?string $mesaj = null): never
    {
        self::gonder(200, array_filter(['basarili' => true, 'mesaj' => $mesaj, 'veri' => $veri ?: null], fn($d) => $d !== null));
    }

    public static function hata(string $mesaj, int $kod = 400, array $ek = []): never
    {
        self::gonder($kod, ['basarili' => false, 'mesaj' => $mesaj] + $ek);
    }

    private static function gonder(int $kod, array $govde): never
    {
        http_response_code($kod);
        header('Content-Type: application/json; charset=utf-8');
        header('Cache-Control: no-store');
        echo json_encode($govde, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        exit;
    }
}
