package com.bivora.karaliste.veri

object NumaraYardimci {

    const val MIN_HANE = 3
    const val DESEN_HANE = 10
    const val JOKER = '?'
    private val JOKER_KARSILIKLARI = setOf('?', 'x', 'X', '*')

    // "+90 532 111 22 33", "0532 111 2233" ve "5321112233" -> "5321112233"
    fun sadelestir(numara: String): String =
        ulusalHale(numara.filter { it.isDigit() })

    // "0850 ??? 0971" veya "0850 xxx 0971" -> "850???0971"
    fun desenSadelestir(desen: String): String =
        ulusalHale(
            desen.mapNotNull {
                when {
                    it.isDigit() -> it
                    it in JOKER_KARSILIKLARI -> JOKER
                    else -> null
                }
            }.joinToString("")
        )

    // Uluslararası (00), yurt içi (0) ve TR ülke kodu (90) öneklerini atar.
    // TR ulusal numaraları 9 ile başlamadığı için baştaki 90 her zaman ülke kodudur.
    private fun ulusalHale(rakamlar: String): String {
        var sonuc = when {
            rakamlar.startsWith("00") -> rakamlar.drop(2)
            rakamlar.startsWith("0") -> rakamlar.drop(1)
            else -> rakamlar
        }
        if (sonuc.startsWith("90") && sonuc.length > 2) sonuc = sonuc.drop(2)
        return sonuc
    }
}
