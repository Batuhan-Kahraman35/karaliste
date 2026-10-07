<?php
declare(strict_types=1);

namespace Karaliste;

final class Istek
{
    private static ?array $govde = null;

    public static function govde(): array
    {
        if (self::$govde === null) {
            $ham = file_get_contents('php://input') ?: '';
            $veri = $ham === '' ? [] : json_decode($ham, true);
            if (!is_array($veri)) {
                Yanit::hata('Geçersiz JSON gövdesi.');
            }
            self::$govde = $veri;
        }
        return self::$govde;
    }

    public static function metin(string $alan, bool $zorunlu = true, int $maxUzunluk = 255): ?string
    {
        $deger = self::govde()[$alan] ?? null;
        $deger = is_scalar($deger) ? trim((string) $deger) : null;

        if ($deger === null || $deger === '') {
            if ($zorunlu) {
                Yanit::hata("'$alan' alanı zorunludur.");
            }
            return null;
        }
        if (mb_strlen($deger) > $maxUzunluk) {
            Yanit::hata("'$alan' alanı en fazla $maxUzunluk karakter olabilir.");
        }
        return $deger;
    }

    public static function token(): ?string
    {
        $baslik = $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? '';
        return preg_match('/^Bearer\s+([a-f0-9]{64})$/i', $baslik, $m) ? strtolower($m[1]) : null;
    }
}
