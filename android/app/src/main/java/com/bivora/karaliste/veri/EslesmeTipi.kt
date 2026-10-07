package com.bivora.karaliste.veri

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "EslesmeTipleri")
data class EslesmeTipi(
    @PrimaryKey
    @ColumnInfo(name = "EslesmeTipleri_id") val id: Int,
    @ColumnInfo(name = "EslesmeTipleri_Ad") val ad: String,
    @ColumnInfo(name = "EslesmeTipleri_Aciklama") val aciklama: String,
    @ColumnInfo(name = "EslesmeTipleri_Ornek") val ornek: String,
    @ColumnInfo(name = "EslesmeTipleri_Sira") val sira: Int,
    @ColumnInfo(name = "OlusturanKullanici") val olusturanKullanici: Int? = null,
    @ColumnInfo(name = "OlusturmaTarihi") val olusturmaTarihi: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "GuncelleyenKullanici") val guncelleyenKullanici: Int? = null,
    @ColumnInfo(name = "GuncellemeTarihi") val guncellemeTarihi: Long? = null,
    @ColumnInfo(name = "Durum") val durum: Boolean = true
) {
    // Eşleştirme mantığı bu id'lere bağlı; ad/açıklama/örnek tablodan gelir
    companion object {
        const val TAM = 1
        const val ONEK = 2
        const val DESEN = 3
    }
}
