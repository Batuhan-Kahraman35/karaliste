package com.bivora.karaliste.ui

import android.telephony.PhoneNumberUtils
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bivora.karaliste.R
import com.bivora.karaliste.veri.EngellenenArama
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngellenenlerEkrani(
    vm: AnaViewModel,
    altMenu: @Composable () -> Unit
) {
    val liste by vm.engellenenler.collectAsStateWithLifecycle()
    val tipler by vm.eslesmeTipleri.collectAsStateWithLifecycle()
    val tipAdlari = remember(tipler) { tipler.associate { it.id to it.ad } }
    val snackbar = remember { SnackbarHostState() }
    var temizleOnay by remember { mutableStateOf(false) }
    val bugun = stringResource(R.string.bugun)
    val dun = stringResource(R.string.dun)

    LaunchedEffect(Unit) {
        vm.mesaj.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.engellenen_aramalar)) },
                actions = {
                    if (liste.isNotEmpty()) {
                        IconButton(onClick = { temizleOnay = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.gecmisi_temizle))
                        }
                    }
                }
            )
        },
        bottomBar = altMenu,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { ic ->
        if (liste.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ic)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.engellenen_yok),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(ic)) {
                items(liste, key = { it.id }) { kayit ->
                    EngellenenSatiri(
                        kayit = kayit,
                        tarih = tarihMetni(kayit.tarih, bugun, dun),
                        kural = kuralMetni(kayit, tipAdlari),
                        sil = { vm.engelleneniSil(kayit) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (temizleOnay) {
        AlertDialog(
            onDismissRequest = { temizleOnay = false },
            title = { Text(stringResource(R.string.gecmisi_temizle)) },
            text = { Text(stringResource(R.string.gecmisi_temizle_onay)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.engellenenleriTemizle()
                    temizleOnay = false
                }) { Text(stringResource(R.string.temizle)) }
            },
            dismissButton = {
                TextButton(onClick = { temizleOnay = false }) { Text(stringResource(R.string.vazgec)) }
            }
        )
    }
}

@Composable
private fun EngellenenSatiri(
    kayit: EngellenenArama,
    tarih: String,
    kural: String?,
    sil: () -> Unit
) {
    ListItem(
        headlineContent = { Text(PhoneNumberUtils.formatNumber(kayit.numara, "TR") ?: kayit.numara) },
        supportingContent = {
            Text(listOfNotNull(tarih, kural).joinToString(" · "))
        },
        trailingContent = {
            IconButton(onClick = sil) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.kaydi_sil))
            }
        }
    )
}

// "Başlayan: 0850" gibi; kural bilgisi yoksa null
private fun kuralMetni(kayit: EngellenenArama, tipAdlari: Map<Int, String>): String? {
    val kural = kayit.kuralNumara ?: return null
    val tip = kayit.eslesmeTipiId?.let { tipAdlari[it] }
    return if (tip != null) "$tip: $kural" else kural
}

private fun tarihMetni(zaman: Long, bugun: String, dun: String): String {
    val yerel = Locale.forLanguageTag("tr-TR")
    val saat = SimpleDateFormat("HH:mm", yerel).format(Date(zaman))
    return when {
        DateUtils.isToday(zaman) -> "$bugun $saat"
        DateUtils.isToday(zaman + DateUtils.DAY_IN_MILLIS) -> "$dun $saat"
        else -> SimpleDateFormat("d MMM yyyy HH:mm", yerel).format(Date(zaman))
    }
}
