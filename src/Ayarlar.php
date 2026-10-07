<?php
declare(strict_types=1);

namespace Karaliste;

final class Ayarlar
{
    private static ?array $onbellek = null;

    public static function al(string $anahtar, ?string $varsayilan = null): ?string
    {
        if (self::$onbellek === null) {
            self::$onbellek = [];
            foreach (Veritabani::satirlar('SELECT Ayarlar_Anahtar, Ayarlar_Deger FROM dbo.Ayarlar WHERE Durum = 1') as $s) {
                self::$onbellek[$s['Ayarlar_Anahtar']] = $s['Ayarlar_Deger'];
            }
        }
        return self::$onbellek[$anahtar] ?? $varsayilan;
    }

    public static function sayi(string $anahtar, int $varsayilan): int
    {
        $deger = self::al($anahtar);
        return is_numeric($deger) ? (int) $deger : $varsayilan;
    }
}
