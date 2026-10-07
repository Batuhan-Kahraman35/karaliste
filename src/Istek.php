<?php
declare(strict_types=1);

namespace Karaliste;

final class Istek
{
    private static ?array $govde = null;
    private static ?string $ip = null;

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

    // İstek güvenilir bir proxy'den (dbo.GuvenilirProxyler) geldiyse CF-Connecting-IP,
    // aksi halde REMOTE_ADDR. Başlık doğrudan gelen isteklerde yok sayılır (sahte IP engeli).
    public static function ip(): string
    {
        if (self::$ip !== null) {
            return self::$ip;
        }
        $uzak = (string) ($_SERVER['REMOTE_ADDR'] ?? '');
        $cf   = (string) ($_SERVER['HTTP_CF_CONNECTING_IP'] ?? '');

        self::$ip = $uzak;
        if ($cf !== '' && filter_var($cf, FILTER_VALIDATE_IP) !== false && self::guvenilirProxy($uzak)) {
            self::$ip = $cf;
        }
        return self::$ip;
    }

    private static function guvenilirProxy(string $ip): bool
    {
        $ikili = @inet_pton($ip);
        if ($ikili === false) {
            return false;
        }
        $araliklar = Veritabani::satirlar('SELECT GuvenilirProxyler_Aralik FROM dbo.GuvenilirProxyler WHERE Durum = 1');
        foreach ($araliklar as $a) {
            [$ag, $onek] = array_pad(explode('/', $a['GuvenilirProxyler_Aralik'], 2), 2, null);
            $agIkili = @inet_pton((string) $ag);
            if ($agIkili === false || strlen($agIkili) !== strlen($ikili)) {
                continue;
            }
            $bit = $onek === null ? strlen($ikili) * 8 : (int) $onek;
            $bayt = intdiv($bit, 8);
            $kalan = $bit % 8;
            if (strncmp($ikili, $agIkili, $bayt) !== 0) {
                continue;
            }
            if ($kalan === 0) {
                return true;
            }
            $maske = (0xFF << (8 - $kalan)) & 0xFF;
            if ((ord($ikili[$bayt]) & $maske) === (ord($agIkili[$bayt]) & $maske)) {
                return true;
            }
        }
        return false;
    }
}
