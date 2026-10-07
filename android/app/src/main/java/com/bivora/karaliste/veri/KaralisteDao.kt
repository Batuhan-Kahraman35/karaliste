package com.bivora.karaliste.veri

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface KaralisteDao {

    @Query("SELECT * FROM Karaliste WHERE Karaliste_Silindi = 0 AND Karaliste_Izinli = :izinli ORDER BY OlusturmaTarihi DESC")
    fun tumu(izinli: Boolean): Flow<List<Karaliste>>

    @Query("SELECT * FROM EslesmeTipleri WHERE Durum = 1 ORDER BY EslesmeTipleri_Sira")
    fun eslesmeTipleri(): Flow<List<EslesmeTipi>>

    @Upsert
    suspend fun eslesmeTipleriniKaydet(tipler: List<EslesmeTipi>)

    // TAM: birebir, ONEK: numara kayıtla başlar, DESEN: GLOB ile ? = tek rakam.
    // Birden fazla kural eşleşirse en belirgini (önce tam numara) döner. izinli: güvenilenler mi, karaliste mi.
    @Query(
        """
        SELECT * FROM Karaliste
        WHERE Durum = 1 AND Karaliste_Silindi = 0 AND Karaliste_Izinli = :izinli AND (
               (Karaliste_EslesmeTipi_id = ${EslesmeTipi.TAM}   AND Karaliste_Numara = :numara)
            OR (Karaliste_EslesmeTipi_id = ${EslesmeTipi.ONEK}  AND substr(:numara, 1, length(Karaliste_Numara)) = Karaliste_Numara)
            OR (Karaliste_EslesmeTipi_id = ${EslesmeTipi.DESEN} AND :numara GLOB Karaliste_Numara)
        )
        ORDER BY Karaliste_EslesmeTipi_id
        LIMIT 1
        """
    )
    suspend fun eslesenKural(numara: String, izinli: Boolean): Karaliste?

    @Query("SELECT EXISTS(SELECT 1 FROM Karaliste WHERE Karaliste_Numara = :numara AND Karaliste_EslesmeTipi_id = :tipId AND Karaliste_Silindi = 0)")
    suspend fun ayniVarMi(numara: String, tipId: Int): Boolean

    @Insert
    suspend fun ekle(kayit: Karaliste): Long

    @Update
    suspend fun guncelle(kayit: Karaliste)

    @Query("UPDATE Karaliste SET Durum = :durum, Karaliste_Senkronlandi = 0, GuncellemeTarihi = :tarih WHERE Karaliste_id = :id")
    suspend fun durumGuncelle(id: Long, durum: Boolean, tarih: Long)

    @Query("DELETE FROM Karaliste WHERE Karaliste_id = :id")
    suspend fun sil(id: Long)

    @Query("UPDATE Karaliste SET Karaliste_Silindi = 1, Karaliste_Senkronlandi = 0, GuncellemeTarihi = :tarih WHERE Karaliste_id = :id")
    suspend fun yumusakSil(id: Long, tarih: Long)

    // ---- Senkron ----

    @Query("SELECT * FROM Karaliste WHERE Karaliste_Senkronlandi = 0")
    suspend fun senkronlanmamislar(): List<Karaliste>

    @Query("SELECT * FROM Karaliste WHERE Karaliste_Guid = :guid")
    suspend fun guidIle(guid: String): Karaliste?

    // İstek sürerken kayıt yeniden değiştiyse işaretlenmez, bir sonraki senkronda tekrar gider
    @Query("UPDATE Karaliste SET Karaliste_Senkronlandi = 1 WHERE Karaliste_Guid = :guid AND IFNULL(GuncellemeTarihi, 0) = :tarih")
    suspend fun senkronlandiIsaretle(guid: String, tarih: Long)

    @Query("UPDATE Karaliste SET Karaliste_Senkronlandi = 1 WHERE Karaliste_Guid = :guid")
    suspend fun senkronlandiIsaretleGuid(guid: String)

    @Query("DELETE FROM Karaliste WHERE Karaliste_Guid = :guid")
    suspend fun guidSil(guid: String)

    @Query("DELETE FROM Karaliste WHERE Karaliste_Silindi = 1 AND Karaliste_Senkronlandi = 1")
    suspend fun senkronlanmisSilinmisleriTemizle()

    @Query("DELETE FROM Karaliste WHERE Karaliste_Silindi = 1")
    suspend fun silinmisleriTemizle()

    // Giriş yapınca yerel listenin tamamı hesaba yüklenir
    @Query("UPDATE Karaliste SET Karaliste_Senkronlandi = 0")
    suspend fun tumunuSenkronlanmadiYap()
}
