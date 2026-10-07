package com.bivora.karaliste.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bivora.karaliste.R

enum class Ekran { Liste, Engellenenler, Giris, Kayit, SifremiUnuttum }

@Composable
fun Uygulama(
    vm: AnaViewModel,
    rolVar: Boolean,
    rolIste: () -> Unit,
    guncelle: () -> Unit
) {
    var ekran by rememberSaveable { mutableStateOf(Ekran.Liste) }
    val zorunlu by vm.zorunluGuncelleme.collectAsStateWithLifecycle()
    val guncellemeDurumu by vm.guncellemeDurumu.collectAsStateWithLifecycle()

    // Desteklenmeyen sürümde başka ekran gösterilmez
    if (zorunlu) {
        ZorunluGuncellemeEkrani(guncellemeDurumu, guncelle)
        return
    }

    // Kayıt ve şifre sıfırlamadan geri: giriş; giriş ve engellenenlerden geri: liste
    BackHandler(enabled = ekran != Ekran.Liste) {
        ekran = when (ekran) {
            Ekran.Kayit, Ekran.SifremiUnuttum -> Ekran.Giris
            else -> Ekran.Liste
        }
    }

    val altMenu: @Composable () -> Unit = { AltMenu(secili = ekran, sec = { ekran = it }) }

    when (ekran) {
        Ekran.Liste -> AnaEkran(
            vm = vm,
            rolVar = rolVar,
            rolIste = rolIste,
            girisAc = { ekran = Ekran.Giris },
            guncelle = guncelle,
            altMenu = altMenu
        )
        Ekran.Engellenenler -> EngellenenlerEkrani(vm = vm, altMenu = altMenu)
        Ekran.Giris -> GirisEkrani(
            vm = vm,
            geri = { ekran = Ekran.Liste },
            kayitAc = { ekran = Ekran.Kayit },
            sifremiUnuttumAc = { ekran = Ekran.SifremiUnuttum },
            girisYapildi = { ekran = Ekran.Liste }
        )
        Ekran.Kayit -> KayitEkrani(
            vm = vm,
            geri = { ekran = Ekran.Giris },
            kayitYapildi = { ekran = Ekran.Liste }
        )
        Ekran.SifremiUnuttum -> SifremiUnuttumEkrani(
            vm = vm,
            geri = { ekran = Ekran.Giris },
            sifreDegisti = { ekran = Ekran.Giris }
        )
    }
}

@Composable
private fun AltMenu(secili: Ekran, sec: (Ekran) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = secili == Ekran.Liste,
            onClick = { sec(Ekran.Liste) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.menu_karaliste)) }
        )
        NavigationBarItem(
            selected = secili == Ekran.Engellenenler,
            onClick = { sec(Ekran.Engellenenler) },
            icon = { Icon(Icons.Default.Phone, contentDescription = null) },
            label = { Text(stringResource(R.string.menu_engellenenler)) }
        )
    }
}
