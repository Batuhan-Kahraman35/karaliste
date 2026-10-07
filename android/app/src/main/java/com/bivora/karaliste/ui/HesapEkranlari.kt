package com.bivora.karaliste.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bivora.karaliste.R

private const val MIN_SIFRE = 8
private const val KOD_HANE = 6

@Composable
fun GirisEkrani(
    vm: AnaViewModel,
    geri: () -> Unit,
    kayitAc: () -> Unit,
    sifremiUnuttumAc: () -> Unit,
    girisYapildi: () -> Unit
) {
    var eposta by rememberSaveable { mutableStateOf("") }
    var sifre by rememberSaveable { mutableStateOf("") }
    var hata by remember { mutableStateOf<String?>(null) }
    val suruyor by vm.islemSuruyor.collectAsStateWithLifecycle()

    HesapIskeleti(baslik = stringResource(R.string.giris_yap), geri = geri) {
        BivoraLogosu()
        Text(stringResource(R.string.giris_aciklama), color = MaterialTheme.colorScheme.onSurfaceVariant)
        EpostaAlani(eposta) { eposta = it }
        SifreAlani(sifre, stringResource(R.string.sifre)) { sifre = it }
        HataMetni(hata)
        IslemButonu(stringResource(R.string.giris_yap), suruyor, eposta.isNotBlank() && sifre.isNotBlank()) {
            hata = null
            vm.giris(eposta.trim(), sifre) { h -> if (h == null) girisYapildi() else hata = h }
        }
        TextButton(onClick = kayitAc, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.hesap_olustur)) }
        TextButton(onClick = sifremiUnuttumAc, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.sifremi_unuttum)) }
    }
}

@Composable
fun KayitEkrani(
    vm: AnaViewModel,
    geri: () -> Unit,
    kayitYapildi: () -> Unit
) {
    var adSoyad by rememberSaveable { mutableStateOf("") }
    var eposta by rememberSaveable { mutableStateOf("") }
    var sifre by rememberSaveable { mutableStateOf("") }
    var sifreTekrar by rememberSaveable { mutableStateOf("") }
    var hata by remember { mutableStateOf<String?>(null) }
    val suruyor by vm.islemSuruyor.collectAsStateWithLifecycle()
    val kisaHata = stringResource(R.string.hata_sifre_kisa, MIN_SIFRE)
    val eslesmeHata = stringResource(R.string.hata_sifre_eslesmiyor)

    HesapIskeleti(baslik = stringResource(R.string.hesap_olustur), geri = geri) {
        Text(stringResource(R.string.kayit_aciklama), color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = adSoyad,
            onValueChange = { adSoyad = it },
            label = { Text(stringResource(R.string.ad_soyad)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        EpostaAlani(eposta) { eposta = it }
        SifreAlani(sifre, stringResource(R.string.sifre)) { sifre = it }
        SifreAlani(sifreTekrar, stringResource(R.string.sifre_tekrar)) { sifreTekrar = it }
        HataMetni(hata)
        IslemButonu(stringResource(R.string.hesap_olustur), suruyor, eposta.isNotBlank() && sifre.isNotBlank()) {
            hata = when {
                sifre.length < MIN_SIFRE -> kisaHata
                sifre != sifreTekrar -> eslesmeHata
                else -> null
            }
            if (hata == null) {
                vm.kayit(adSoyad.trim(), eposta.trim(), sifre) { h -> if (h == null) kayitYapildi() else hata = h }
            }
        }
    }
}

@Composable
fun SifremiUnuttumEkrani(
    vm: AnaViewModel,
    geri: () -> Unit,
    sifreDegisti: () -> Unit
) {
    var eposta by rememberSaveable { mutableStateOf("") }
    var kod by rememberSaveable { mutableStateOf("") }
    var sifre by rememberSaveable { mutableStateOf("") }
    var sifreTekrar by rememberSaveable { mutableStateOf("") }
    var kodGonderildi by rememberSaveable { mutableStateOf(false) }
    var bilgi by rememberSaveable { mutableStateOf<String?>(null) }
    var hata by remember { mutableStateOf<String?>(null) }
    val suruyor by vm.islemSuruyor.collectAsStateWithLifecycle()
    val kisaHata = stringResource(R.string.hata_sifre_kisa, MIN_SIFRE)
    val eslesmeHata = stringResource(R.string.hata_sifre_eslesmiyor)

    fun kodIste() {
        hata = null
        vm.sifreSifirlaIste(eposta.trim()) { h, b ->
            hata = h
            if (h == null) {
                bilgi = b
                kodGonderildi = true
            }
        }
    }

    HesapIskeleti(baslik = stringResource(R.string.sifremi_unuttum), geri = geri) {
        if (!kodGonderildi) {
            Text(stringResource(R.string.sifirlama_aciklama), color = MaterialTheme.colorScheme.onSurfaceVariant)
            EpostaAlani(eposta) { eposta = it }
            HataMetni(hata)
            IslemButonu(stringResource(R.string.kod_gonder), suruyor, eposta.isNotBlank()) { kodIste() }
        } else {
            bilgi?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            OutlinedTextField(
                value = kod,
                onValueChange = { yeni -> kod = yeni.filter(Char::isDigit).take(KOD_HANE) },
                label = { Text(stringResource(R.string.dogrulama_kodu)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            SifreAlani(sifre, stringResource(R.string.yeni_sifre)) { sifre = it }
            SifreAlani(sifreTekrar, stringResource(R.string.sifre_tekrar)) { sifreTekrar = it }
            HataMetni(hata)
            IslemButonu(stringResource(R.string.sifreyi_degistir), suruyor, kod.length == KOD_HANE && sifre.isNotBlank()) {
                hata = when {
                    sifre.length < MIN_SIFRE -> kisaHata
                    sifre != sifreTekrar -> eslesmeHata
                    else -> null
                }
                if (hata == null) {
                    vm.sifreSifirla(eposta.trim(), kod, sifre) { h -> if (h == null) sifreDegisti() else hata = h }
                }
            }
            TextButton(onClick = { kodIste() }, enabled = !suruyor, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.kodu_tekrar_gonder))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HesapIskeleti(
    baslik: String,
    geri: () -> Unit,
    icerik: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(baslik) },
                navigationIcon = {
                    IconButton(onClick = geri) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.geri))
                    }
                }
            )
        }
    ) { ic ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ic)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = icerik
        )
    }
}

@Composable
private fun BivoraLogosu() {
    Image(
        painter = painterResource(R.drawable.bivora_logo_yatay),
        contentDescription = stringResource(R.string.bivora),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(40.dp)
    )
}

@Composable
private fun EpostaAlani(deger: String, degisti: (String) -> Unit) {
    OutlinedTextField(
        value = deger,
        onValueChange = degisti,
        label = { Text(stringResource(R.string.eposta)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HesapSilmeDiyalogu(vm: AnaViewModel, kapat: () -> Unit) {
    val suruyor by vm.islemSuruyor.collectAsStateWithLifecycle()
    var sifre by rememberSaveable { mutableStateOf("") }
    var hata by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!suruyor) kapat() },
        title = { Text(stringResource(R.string.hesabi_sil)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.hesap_sil_aciklama))
                SifreAlani(sifre, stringResource(R.string.hesap_sil_sifre)) { sifre = it; hata = null }
                HataMetni(hata)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.hesapSil(sifre) { h -> if (h == null) kapat() else hata = h } },
                enabled = sifre.isNotBlank() && !suruyor,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                if (suruyor) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.hesabi_sil))
            }
        },
        dismissButton = {
            TextButton(onClick = kapat, enabled = !suruyor) { Text(stringResource(R.string.vazgec)) }
        }
    )
}

@Composable
private fun SifreAlani(deger: String, etiket: String, degisti: (String) -> Unit) {
    OutlinedTextField(
        value = deger,
        onValueChange = degisti,
        label = { Text(etiket) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HataMetni(hata: String?) {
    hata?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

@Composable
private fun IslemButonu(metin: String, suruyor: Boolean, aktif: Boolean, tikla: () -> Unit) {
    Button(onClick = tikla, enabled = aktif && !suruyor, modifier = Modifier.fillMaxWidth()) {
        if (suruyor) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        else Text(metin)
    }
}
