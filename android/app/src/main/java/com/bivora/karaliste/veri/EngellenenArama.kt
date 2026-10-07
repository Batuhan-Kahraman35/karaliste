package com.bivora.karaliste.veri

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Yalnızca cihazda tutulur, sunucuya gönderilmez
@Entity(
    tableName = "EngellenenAramalar",
    indices = [Index(value = ["EngellenenAramalar_Tarih"])]
)
data class EngellenenArama(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "EngellenenAramalar_id") val id: Long = 0,
    // Aramanın geldiği haliyle numara
    @ColumnInfo(name = "EngellenenAramalar_Numara") val numara: String,
    @ColumnInfo(name = "EngellenenAramalar_Tarih") val tarih: Long = System.currentTimeMillis(),
    // Eşleşen kuralın o anki görünümü; kural sonradan silinse de geçmişte kalır
    @ColumnInfo(name = "EngellenenAramalar_KuralNumara") val kuralNumara: String?,
    @ColumnInfo(name = "EngellenenAramalar_EslesmeTipi_id") val eslesmeTipiId: Int?,
    @ColumnInfo(name = "OlusturanKullanici") val olusturanKullanici: Int? = null,
    @ColumnInfo(name = "OlusturmaTarihi") val olusturmaTarihi: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "GuncelleyenKullanici") val guncelleyenKullanici: Int? = null,
    @ColumnInfo(name = "GuncellemeTarihi") val guncellemeTarihi: Long? = null,
    @ColumnInfo(name = "Durum") val durum: Boolean = true
)
