<?php
declare(strict_types=1);

namespace Karaliste\Api;

use Karaliste\Istek;
use Karaliste\Oturum;
use Karaliste\Veritabani;
use Karaliste\Yanit;

final class KaralisteApi
{
    // Android'deki EslesmeTipi.DESEN ile aynı id
    private const DESEN = 3;
    private const MAX_DEGISIKLIK = 5000;

    public static function eslesmeTipleri(): never
    {
        $tipler = Veritabani::satirlar(
            'SELECT EslesmeTipleri_id, EslesmeTipleri_Ad, EslesmeTipleri_Aciklama, EslesmeTipleri_Ornek, EslesmeTipleri_Sira
             FROM dbo.EslesmeTipleri WHERE Durum = 1 ORDER BY EslesmeTipleri_Sira'
        );

        Yanit::basarili(['tipler' => array_map(fn(array $t) => [
            'id'       => (int) $t['EslesmeTipleri_id'],
            'ad'       => $t['EslesmeTipleri_Ad'],
            'aciklama' => $t['EslesmeTipleri_Aciklama'],
            'ornek'    => $t['EslesmeTipleri_Ornek'],
            'sira'     => (int) $t['EslesmeTipleri_Sira'],
        ], $tipler)]);
    }

    /*
     * İstek:  { "sonSenkron": "2026-10-06 18:31:53.343" | null, "izinliDestegi": true,
     *           "degisiklikler": [ { guid, numara, gorunenNumara, eslesmeTipiId, aciklama, izinli?, durum, silindi } ] }
     * izinliDestegi göndermeyen eski istemciler (APK < 1.6.0) güvenilen kayıtları almaz; aksi halde
     * bunları engelleme kuralı olarak kaydederlerdi. Gönderdikleri kayıtta "izinli" yoksa mevcut değer korunur.
     * Yanıt:  { sunucuZamani, kayitlar: [...], hatalar: [ { guid, mesaj } ] }
     * İstemci bir sonraki istekte sonSenkron = sunucuZamani gönderir.
     */
    public static function senkron(): never
    {
        $kullaniciId = Oturum::dogrula();
        $govde = Istek::govde();

        $sonSenkron = $govde['sonSenkron'] ?? null;
        $izinliDestegi = ($govde['izinliDestegi'] ?? false) === true;
        if (!is_string($sonSenkron) || !preg_match('/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(\.\d{1,3})?$/', $sonSenkron)) {
            $sonSenkron = null;
        }

        $degisiklikler = $govde['degisiklikler'] ?? [];
        if (!is_array($degisiklikler)) {
            Yanit::hata("'degisiklikler' bir dizi olmalı.");
        }
        if (count($degisiklikler) > self::MAX_DEGISIKLIK) {
            Yanit::hata('Tek seferde en fazla ' . self::MAX_DEGISIKLIK . ' değişiklik gönderilebilir.');
        }

        // Zaman damgası değişiklikler yazılmadan önce alınır; bu istekte yazılanlar bir sonraki senkronda tekrar gelir (zararsız)
        $sunucuZamani = (string) Veritabani::deger('SELECT CONVERT(VARCHAR(23), GETDATE(), 121)');
        $tipler = array_map('intval', array_column(
            Veritabani::satirlar('SELECT EslesmeTipleri_id FROM dbo.EslesmeTipleri WHERE Durum = 1'),
            'EslesmeTipleri_id'
        ));

        $hatalar = [];
        $db = Veritabani::al();
        $db->beginTransaction();
        try {
            foreach ($degisiklikler as $d) {
                $kayit = self::dogrula($d, $tipler);
                if (is_string($kayit)) {
                    $hatalar[] = ['guid' => is_array($d) ? ($d['guid'] ?? null) : null, 'mesaj' => $kayit];
                    continue;
                }
                self::kaydet($kullaniciId, $kayit);
            }
            $db->commit();
        } catch (\Throwable $e) {
            $db->rollBack();
            throw $e;
        }

        $sql = 'SELECT LOWER(CONVERT(VARCHAR(36), Karaliste_Guid)) AS guid, Karaliste_Numara, Karaliste_GorunenNumara,
                       Karaliste_EslesmeTipi_id, Karaliste_Aciklama, Karaliste_Izinli, Durum, Karaliste_Silindi,
                       CONVERT(VARCHAR(23), GuncellemeTarihi, 121) AS guncellemeTarihi
                FROM dbo.Karaliste
                WHERE Karaliste_Kullanicilar_id = ?';
        $parametreler = [$kullaniciId];
        if (!$izinliDestegi) {
            $sql .= ' AND Karaliste_Izinli = 0';
        }
        if ($sonSenkron !== null) {
            $sql .= ' AND GuncellemeTarihi >= CONVERT(DATETIME, ?, 121)';
            $parametreler[] = $sonSenkron;
        }
        $sql .= ' ORDER BY GuncellemeTarihi';

        $kayitlar = array_map(fn(array $s) => [
            'guid'             => $s['guid'],
            'numara'           => $s['Karaliste_Numara'],
            'gorunenNumara'    => $s['Karaliste_GorunenNumara'],
            'eslesmeTipiId'    => (int) $s['Karaliste_EslesmeTipi_id'],
            'aciklama'         => $s['Karaliste_Aciklama'],
            'izinli'           => (bool) $s['Karaliste_Izinli'],
            'durum'            => (bool) $s['Durum'],
            'silindi'          => (bool) $s['Karaliste_Silindi'],
            'guncellemeTarihi' => $s['guncellemeTarihi'],
        ], Veritabani::satirlar($sql, $parametreler));

        Yanit::basarili([
            'sunucuZamani' => $sunucuZamani,
            'kayitlar'     => $kayitlar,
            'hatalar'      => $hatalar,
        ]);
    }

    private static function dogrula(mixed $d, array $tipler): array|string
    {
        if (!is_array($d)) {
            return 'Geçersiz kayıt.';
        }

        $guid = strtolower(trim((string) ($d['guid'] ?? '')));
        if (!preg_match('/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/', $guid)) {
            return 'Geçersiz guid.';
        }

        $tip = (int) ($d['eslesmeTipiId'] ?? 0);
        if (!in_array($tip, $tipler, true)) {
            return 'Geçersiz eşleşme tipi.';
        }

        // İstemci numarayı sadeleştirip gönderir; burada yalnızca biçim kontrol edilir
        $numara = (string) ($d['numara'] ?? '');
        $gecerli = $tip === self::DESEN
            ? preg_match('/^[0-9?]{10}$/', $numara) && str_contains($numara, '?')
            : preg_match('/^[0-9]{3,20}$/', $numara);
        if (!$gecerli) {
            return 'Geçersiz numara.';
        }

        $gorunen = trim((string) ($d['gorunenNumara'] ?? '')) ?: $numara;
        $aciklama = trim((string) ($d['aciklama'] ?? ''));

        return [
            'guid'     => $guid,
            'numara'   => $numara,
            'gorunen'  => mb_substr($gorunen, 0, 50),
            'tip'      => $tip,
            'aciklama' => $aciklama === '' ? null : mb_substr($aciklama, 0, 255),
            // null: istemci alanı göndermedi (eski sürüm), mevcut değer değişmez
            'izinli'   => array_key_exists('izinli', $d) ? (int) (bool) $d['izinli'] : null,
            'durum'    => (int) (bool) ($d['durum'] ?? true),
            'silindi'  => (int) (bool) ($d['silindi'] ?? false),
        ];
    }

    private static function kaydet(int $kullaniciId, array $k): void
    {
        $mevcut = Veritabani::satir(
            'SELECT Karaliste_id, Karaliste_Kullanicilar_id FROM dbo.Karaliste WITH (UPDLOCK, HOLDLOCK) WHERE Karaliste_Guid = ?',
            [$k['guid']]
        );
        if ($mevcut !== null && (int) $mevcut['Karaliste_Kullanicilar_id'] !== $kullaniciId) {
            return; // Başka kullanıcıya ait guid, yok sayılır
        }
        $mevcutId = $mevcut !== null ? (int) $mevcut['Karaliste_id'] : null;

        // Aynı numara + tip başka bir kayıtta zaten varsa bu kopya silinmiş olarak yazılır;
        // istemci yanıtta kendi kopyasını silinmiş, mevcut kaydı da yeni olarak alır.
        $silindi = $k['silindi'];
        if (!$silindi) {
            $sql = 'SELECT TOP 1 Karaliste_id FROM dbo.Karaliste
                    WHERE Karaliste_Kullanicilar_id = ? AND Karaliste_Numara = ? AND Karaliste_EslesmeTipi_id = ? AND Karaliste_Silindi = 0';
            $parametreler = [$kullaniciId, $k['numara'], $k['tip']];
            if ($mevcutId !== null) {
                $sql .= ' AND Karaliste_id <> ?';
                $parametreler[] = $mevcutId;
            }
            if (Veritabani::satir($sql, $parametreler) !== null) {
                $silindi = 1;
            }
        }

        if ($mevcutId !== null) {
            Veritabani::calistir(
                'UPDATE dbo.Karaliste
                 SET Karaliste_Numara = ?, Karaliste_GorunenNumara = ?, Karaliste_EslesmeTipi_id = ?, Karaliste_Aciklama = ?,
                     Karaliste_Izinli = COALESCE(CAST(? AS BIT), Karaliste_Izinli),
                     Durum = ?, Karaliste_Silindi = ?, GuncelleyenKullanici = ?, GuncellemeTarihi = GETDATE()
                 WHERE Karaliste_id = ?',
                [$k['numara'], $k['gorunen'], $k['tip'], $k['aciklama'], $k['izinli'], $k['durum'], $silindi, $kullaniciId, $mevcutId]
            );
            return;
        }

        Veritabani::calistir(
            'INSERT INTO dbo.Karaliste
                (Karaliste_Guid, Karaliste_Kullanicilar_id, Karaliste_Numara, Karaliste_GorunenNumara, Karaliste_EslesmeTipi_id,
                 Karaliste_Aciklama, Karaliste_Izinli, Durum, Karaliste_Silindi, OlusturanKullanici)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [$k['guid'], $kullaniciId, $k['numara'], $k['gorunen'], $k['tip'], $k['aciklama'], $k['izinli'] ?? 0, $k['durum'], $silindi, $kullaniciId]
        );
    }
}
