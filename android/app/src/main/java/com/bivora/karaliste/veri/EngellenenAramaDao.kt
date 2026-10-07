package com.bivora.karaliste.veri

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EngellenenAramaDao {

    @Query("SELECT * FROM EngellenenAramalar ORDER BY EngellenenAramalar_Tarih DESC")
    fun tumu(): Flow<List<EngellenenArama>>

    @Insert
    suspend fun ekle(kayit: EngellenenArama): Long

    // En yeni :sinir kayıt dışındakileri siler
    @Query(
        """
        DELETE FROM EngellenenAramalar
        WHERE EngellenenAramalar_id NOT IN (
            SELECT EngellenenAramalar_id FROM EngellenenAramalar
            ORDER BY EngellenenAramalar_Tarih DESC LIMIT :sinir
        )
        """
    )
    suspend fun eskileriTemizle(sinir: Int)

    @Query("DELETE FROM EngellenenAramalar WHERE EngellenenAramalar_id = :id")
    suspend fun sil(id: Long)

    @Query("DELETE FROM EngellenenAramalar")
    suspend fun tumunuSil()
}
