package com.bivora.karaliste.veri

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Karaliste::class, EslesmeTipi::class, EngellenenArama::class], version = 4, exportSchema = false)
abstract class VeriTabani : RoomDatabase() {

    abstract fun karalisteDao(): KaralisteDao
    abstract fun engellenenAramaDao(): EngellenenAramaDao

    companion object {
        @Volatile
        private var ornek: VeriTabani? = null

        fun al(context: Context): VeriTabani =
            ornek ?: synchronized(this) {
                ornek ?: Room.databaseBuilder(
                    context.applicationContext,
                    VeriTabani::class.java,
                    "karaliste.db"
                )
                    .addMigrations(GOC_1_2, GOC_2_3, GOC_3_4)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) = eslesmeTipleriniDoldur(db)
                    })
                    .build().also { ornek = it }
            }

        private fun eslesmeTipleriniDoldur(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO EslesmeTipleri
                    (EslesmeTipleri_id, EslesmeTipleri_Ad, EslesmeTipleri_Aciklama, EslesmeTipleri_Ornek,
                     EslesmeTipleri_Sira, OlusturmaTarihi, Durum)
                VALUES
                    (${EslesmeTipi.TAM},   'Tam',      'Numaranın tamamı eşleşir',                 '0532 111 22 33', 1, strftime('%s','now') * 1000, 1),
                    (${EslesmeTipi.ONEK},  'Başlayan', 'Bu rakamlarla başlayan tüm numaralar',     '0850',           2, strftime('%s','now') * 1000, 1),
                    (${EslesmeTipi.DESEN}, 'Desen',    'Her ? (veya x, *) tek bir rakam yerine geçer', '0850 ??? 0971', 3, strftime('%s','now') * 1000, 1)
                """
            )
        }

        private val GOC_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `EslesmeTipleri` (
                        `EslesmeTipleri_id` INTEGER NOT NULL,
                        `EslesmeTipleri_Ad` TEXT NOT NULL,
                        `EslesmeTipleri_Aciklama` TEXT NOT NULL,
                        `EslesmeTipleri_Ornek` TEXT NOT NULL,
                        `EslesmeTipleri_Sira` INTEGER NOT NULL,
                        `OlusturanKullanici` INTEGER,
                        `OlusturmaTarihi` INTEGER NOT NULL,
                        `GuncelleyenKullanici` INTEGER,
                        `GuncellemeTarihi` INTEGER,
                        `Durum` INTEGER NOT NULL,
                        PRIMARY KEY(`EslesmeTipleri_id`))
                    """
                )
                eslesmeTipleriniDoldur(db)
                db.execSQL("ALTER TABLE `Karaliste` ADD COLUMN `Karaliste_EslesmeTipi_id` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("DROP INDEX IF EXISTS `index_Karaliste_Karaliste_Numara`")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_Karaliste_Karaliste_Numara_Karaliste_EslesmeTipi_id` " +
                        "ON `Karaliste` (`Karaliste_Numara`, `Karaliste_EslesmeTipi_id`)"
                )
            }
        }

        // Senkron alanları: Guid, Silindi, Senkronlandi. Tablo yeniden oluşturulur, mevcut kayıtlara guid üretilir.
        private val GOC_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Karaliste_yeni` (
                        `Karaliste_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `Karaliste_Guid` TEXT NOT NULL,
                        `Karaliste_Numara` TEXT NOT NULL,
                        `Karaliste_GorunenNumara` TEXT NOT NULL,
                        `Karaliste_EslesmeTipi_id` INTEGER NOT NULL DEFAULT 1,
                        `Karaliste_Aciklama` TEXT,
                        `Karaliste_Silindi` INTEGER NOT NULL DEFAULT 0,
                        `Karaliste_Senkronlandi` INTEGER NOT NULL DEFAULT 0,
                        `OlusturanKullanici` INTEGER,
                        `OlusturmaTarihi` INTEGER NOT NULL,
                        `GuncelleyenKullanici` INTEGER,
                        `GuncellemeTarihi` INTEGER,
                        `Durum` INTEGER NOT NULL)
                    """
                )
                db.execSQL(
                    """
                    INSERT INTO `Karaliste_yeni`
                        (Karaliste_id, Karaliste_Guid, Karaliste_Numara, Karaliste_GorunenNumara, Karaliste_EslesmeTipi_id,
                         Karaliste_Aciklama, OlusturanKullanici, OlusturmaTarihi, GuncelleyenKullanici, GuncellemeTarihi, Durum)
                    SELECT Karaliste_id,
                           lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' ||
                           substr(lower(hex(randomblob(2))), 2) || '-' || substr('89ab', 1 + (abs(random()) % 4), 1) ||
                           substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6))),
                           Karaliste_Numara, Karaliste_GorunenNumara, Karaliste_EslesmeTipi_id,
                           Karaliste_Aciklama, OlusturanKullanici, OlusturmaTarihi, GuncelleyenKullanici,
                           IFNULL(GuncellemeTarihi, OlusturmaTarihi), Durum
                    FROM `Karaliste`
                    """
                )
                db.execSQL("DROP TABLE `Karaliste`")
                db.execSQL("ALTER TABLE `Karaliste_yeni` RENAME TO `Karaliste`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Karaliste_Karaliste_Guid` ON `Karaliste` (`Karaliste_Guid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_Karaliste_Karaliste_Numara` ON `Karaliste` (`Karaliste_Numara`)")
            }
        }

        // Engellenen arama geçmişi (yalnızca cihazda)
        private val GOC_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `EngellenenAramalar` (
                        `EngellenenAramalar_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `EngellenenAramalar_Numara` TEXT NOT NULL,
                        `EngellenenAramalar_Tarih` INTEGER NOT NULL,
                        `EngellenenAramalar_KuralNumara` TEXT,
                        `EngellenenAramalar_EslesmeTipi_id` INTEGER,
                        `OlusturanKullanici` INTEGER,
                        `OlusturmaTarihi` INTEGER NOT NULL,
                        `GuncelleyenKullanici` INTEGER,
                        `GuncellemeTarihi` INTEGER,
                        `Durum` INTEGER NOT NULL)
                    """
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_EngellenenAramalar_EngellenenAramalar_Tarih` " +
                        "ON `EngellenenAramalar` (`EngellenenAramalar_Tarih`)"
                )
            }
        }
    }
}
