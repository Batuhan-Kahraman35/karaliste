package com.bivora.karaliste.veri

import org.junit.Assert.assertEquals
import org.junit.Test

class NumaraYardimciTest {

    @Test
    fun tamNumaraFarkliBicimlerAyniSonucuVerir() {
        listOf("+90 532 111 22 33", "0532 111 2233", "5321112233", "0090 532 111 22 33", "90(532)111-22-33")
            .forEach { assertEquals(it, "5321112233", NumaraYardimci.sadelestir(it)) }
    }

    @Test
    fun onekSadelestirilir() {
        assertEquals("850", NumaraYardimci.sadelestir("0850"))
        assertEquals("850", NumaraYardimci.sadelestir("+90 850"))
        assertEquals("212", NumaraYardimci.sadelestir("0212"))
    }

    @Test
    fun desenSadelestirilir() {
        assertEquals("850???0971", NumaraYardimci.desenSadelestir("0850 ??? 0971"))
        assertEquals("850???0971", NumaraYardimci.desenSadelestir("0850 xxx 0971"))
        assertEquals("850???0971", NumaraYardimci.desenSadelestir("+90 850 *** 0971"))
    }

    @Test
    fun yabanciNumaraKorunur() {
        assertEquals("442071234567", NumaraYardimci.sadelestir("+44 20 7123 4567"))
    }
}
