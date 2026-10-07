<?php
declare(strict_types=1);

namespace Karaliste;

use PDO;

final class Veritabani
{
    private static ?PDO $baglanti = null;

    public static function al(): PDO
    {
        if (self::$baglanti === null) {
            $ayar = require dirname(__DIR__) . '/config/veritabani.php';

            self::$baglanti = new PDO(
                sprintf('sqlsrv:Server=%s;Database=%s;TrustServerCertificate=1', $ayar['sunucu'], $ayar['veritabani']),
                $ayar['kullanici'],
                $ayar['sifre'],
                [
                    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::SQLSRV_ATTR_FETCHES_NUMERIC_TYPE => true,
                ]
            );
        }
        return self::$baglanti;
    }

    public static function satir(string $sql, array $parametreler = []): ?array
    {
        $sorgu = self::al()->prepare($sql);
        $sorgu->execute($parametreler);
        $sonuc = $sorgu->fetch();
        return $sonuc === false ? null : $sonuc;
    }

    public static function satirlar(string $sql, array $parametreler = []): array
    {
        $sorgu = self::al()->prepare($sql);
        $sorgu->execute($parametreler);
        return $sorgu->fetchAll();
    }

    public static function calistir(string $sql, array $parametreler = []): int
    {
        $sorgu = self::al()->prepare($sql);
        $sorgu->execute($parametreler);
        return $sorgu->rowCount();
    }

    // INSERT ... OUTPUT INSERTED.<id> ile kullanılır
    public static function deger(string $sql, array $parametreler = []): mixed
    {
        $sorgu = self::al()->prepare($sql);
        $sorgu->execute($parametreler);
        $sonuc = $sorgu->fetchColumn();
        return $sonuc === false ? null : $sonuc;
    }
}
