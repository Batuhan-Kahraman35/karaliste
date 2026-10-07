package com.bivora.karaliste.veri

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(
    tableName = "Karaliste",
    indices = [
        Index(value = ["Karaliste_Guid"], unique = true),
        Index(value = ["Karaliste_Numara"])
    ]
)
data class Karaliste(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Karaliste_id") val id: Long = 0,
    // Cihaz ve sunucuda aynı kaydı eşleştirir
    @ColumnInfo(name = "Karaliste_Guid") val guid: String,
    // Karşılaştırma için sadeleştirilmiş numara, önek veya desen (? = tek rakam)
    @ColumnInfo(name = "Karaliste_Numara") val numara: String,
    // Kullanıcının girdiği haliyle numara
    @ColumnInfo(name = "Karaliste_GorunenNumara") val gorunenNumara: String,
    @ColumnInfo(name = "Karaliste_EslesmeTipi_id", defaultValue = "1") val eslesmeTipiId: Int = EslesmeTipi.TAM,
    @ColumnInfo(name = "Karaliste_Aciklama") val aciklama: String? = null,
    // Üyelikte silinen kayıt sunucuya iletilene kadar işaretli tutulur
    @ColumnInfo(name = "Karaliste_Silindi", defaultValue = "0") val silindi: Boolean = false,
    @ColumnInfo(name = "Karaliste_Senkronlandi", defaultValue = "0") val senkronlandi: Boolean = false,
    @ColumnInfo(name = "OlusturanKullanici") val olusturanKullanici: Int? = null,
    @ColumnInfo(name = "OlusturmaTarihi") val olusturmaTarihi: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "GuncelleyenKullanici") val guncelleyenKullanici: Int? = null,
    @ColumnInfo(name = "GuncellemeTarihi") val guncellemeTarihi: Long? = System.currentTimeMillis(),
    @ColumnInfo(name = "Durum") val durum: Boolean = true
) {
    fun json(): JSONObject = JSONObject()
        .put("guid", guid)
        .put("numara", numara)
        .put("gorunenNumara", gorunenNumara)
        .put("eslesmeTipiId", eslesmeTipiId)
        .put("aciklama", aciklama ?: JSONObject.NULL)
        .put("durum", durum)
        .put("silindi", silindi)
}
