package com.bivora.karaliste.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bivora.karaliste.R
import com.bivora.karaliste.guncelleme.GuncellemeDurumu
import com.bivora.karaliste.guncelleme.SurumBilgisi

// Ana ekranın üstündeki kart; güncelleme yoksa hiçbir şey çizmez
@Composable
fun GuncellemeKarti(durum: GuncellemeDurumu, guncelle: () -> Unit) {
    val bilgi = durum.bilgiVeyaNull() ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                Text(
                    text = stringResource(R.string.guncelleme_baslik, bilgi.surumAdi),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            SurumNotlari(bilgi)
            GuncellemeIslemi(durum, guncelle)
        }
    }
}

// Desteklenmeyen sürümde kapatılamayan tam ekran
@Composable
fun ZorunluGuncellemeEkrani(durum: GuncellemeDurumu, guncelle: () -> Unit) {
    Scaffold { ic ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ic)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.zorunlu_baslik),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.zorunlu_aciklama),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            durum.bilgiVeyaNull()?.let { SurumNotlari(it) }
            Spacer(Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                GuncellemeIslemi(durum, guncelle)
            }
        }
    }
}

@Composable
private fun SurumNotlari(bilgi: SurumBilgisi) {
    bilgi.notlar.forEach { not ->
        Text("• $not", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun GuncellemeIslemi(durum: GuncellemeDurumu, guncelle: () -> Unit) {
    when (durum) {
        is GuncellemeDurumu.Indiriliyor -> {
            Text(stringResource(R.string.guncelleme_indiriliyor, durum.yuzde), style = MaterialTheme.typography.bodySmall)
            LinearProgressIndicator(
                progress = { durum.yuzde / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }
        is GuncellemeDurumu.Kuruluyor -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(stringResource(R.string.guncelleme_kuruluyor), style = MaterialTheme.typography.bodySmall)
            }
        }
        is GuncellemeDurumu.Hata -> {
            Text(durum.mesaj, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Button(onClick = guncelle, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.tekrar_dene))
            }
        }
        is GuncellemeDurumu.Var -> {
            Button(onClick = guncelle, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.simdi_guncelle))
            }
        }
        GuncellemeDurumu.Yok -> Unit
    }
}

private fun GuncellemeDurumu.bilgiVeyaNull(): SurumBilgisi? = when (this) {
    is GuncellemeDurumu.Var -> bilgi
    is GuncellemeDurumu.Indiriliyor -> bilgi
    is GuncellemeDurumu.Kuruluyor -> bilgi
    is GuncellemeDurumu.Hata -> bilgi
    GuncellemeDurumu.Yok -> null
}
