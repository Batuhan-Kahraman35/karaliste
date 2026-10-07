package com.bivora.karaliste.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bivora.karaliste.R
import com.bivora.karaliste.ag.OturumBilgisi
import com.bivora.karaliste.veri.EslesmeTipi
import com.bivora.karaliste.veri.Karaliste

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaEkran(
    vm: AnaViewModel,
    rolVar: Boolean,
    rolIste: () -> Unit,
    girisAc: () -> Unit,
    guncelle: () -> Unit,
    altMenu: @Composable () -> Unit
) {
    val liste by vm.liste.collectAsStateWithLifecycle()
    val guncellemeDurumu by vm.guncellemeDurumu.collectAsStateWithLifecycle()
    val tipler by vm.eslesmeTipleri.collectAsStateWithLifecycle()
    val oturum by vm.oturum.collectAsStateWithLifecycle()
    val senkronCalisiyor by vm.senkronCalisiyor.collectAsStateWithLifecycle()
    val tipAdlari = remember(tipler) { tipler.associate { it.id to it.ad } }
    val snackbar = remember { SnackbarHostState() }
    var arama by rememberSaveable { mutableStateOf("") }
    var eklemeAcik by rememberSaveable { mutableStateOf(false) }
    var silinecek by remember { mutableStateOf<Karaliste?>(null) }

    LaunchedEffect(Unit) {
        vm.mesaj.collect { snackbar.showSnackbar(it) }
    }

    val filtreli = remember(liste, arama) {
        if (arama.isBlank()) liste
        else liste.filter {
            it.gorunenNumara.contains(arama, ignoreCase = true) ||
                it.numara.contains(arama.filter(Char::isDigit).ifEmpty { arama }) ||
                (it.aciklama?.contains(arama, ignoreCase = true) ?: false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    if (oturum != null) {
                        IconButton(onClick = { vm.senkronIste(elle = true) }, enabled = !senkronCalisiyor) {
                            if (senkronCalisiyor) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.senkronize_et))
                        }
                    }
                    HesapMenusu(oturum = oturum, girisAc = girisAc, cikis = vm::cikis)
                }
            )
        },
        bottomBar = altMenu,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { eklemeAcik = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.numara_ekle))
            }
        }
    ) { ic ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ic)
        ) {
            GuncellemeKarti(guncellemeDurumu, guncelle)

            if (!rolVar) {
                RolUyarisi(rolIste)
            }

            OutlinedTextField(
                value = arama,
                onValueChange = { arama = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.ara)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (filtreli.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(if (liste.isEmpty()) R.string.liste_bos else R.string.sonuc_yok),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(filtreli, key = { it.id }) { kayit ->
                        KaralisteSatiri(
                            kayit = kayit,
                            tipAdi = tipAdlari[kayit.eslesmeTipiId].orEmpty(),
                            durumDegistir = { vm.durumDegistir(kayit, it) },
                            sil = { silinecek = kayit }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (eklemeAcik) {
        EklemeDiyalogu(
            tipler = tipler,
            kapat = { eklemeAcik = false },
            kaydet = { numara, aciklama, tipId ->
                vm.ekle(numara, aciklama, tipId)
                eklemeAcik = false
            }
        )
    }

    silinecek?.let { kayit ->
        AlertDialog(
            onDismissRequest = { silinecek = null },
            title = { Text(stringResource(R.string.sil_baslik)) },
            text = { Text(stringResource(R.string.sil_onay, kayit.gorunenNumara)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.sil(kayit)
                    silinecek = null
                }) { Text(stringResource(R.string.sil)) }
            },
            dismissButton = {
                TextButton(onClick = { silinecek = null }) { Text(stringResource(R.string.vazgec)) }
            }
        )
    }
}

@Composable
private fun HesapMenusu(
    oturum: OturumBilgisi?,
    girisAc: () -> Unit,
    cikis: () -> Unit
) {
    var acik by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { if (oturum == null) girisAc() else acik = true }) {
            Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.hesap))
        }
        if (oturum != null) {
            DropdownMenu(expanded = acik, onDismissRequest = { acik = false }) {
                DropdownMenuItem(
                    text = {
                        Column {
                            oturum.adSoyad?.let { Text(it, style = MaterialTheme.typography.titleSmall) }
                            Text(oturum.eposta, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    onClick = {},
                    enabled = false
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.cikis_yap)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    onClick = {
                        acik = false
                        cikis()
                    }
                )
            }
        }
    }
}

@Composable
private fun RolUyarisi(rolIste: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Text(
                    text = stringResource(R.string.rol_baslik),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(stringResource(R.string.rol_aciklama))
            Button(onClick = rolIste) { Text(stringResource(R.string.rol_izin_ver)) }
        }
    }
}

@Composable
private fun KaralisteSatiri(
    kayit: Karaliste,
    tipAdi: String,
    durumDegistir: (Boolean) -> Unit,
    sil: () -> Unit
) {
    val altBilgi = listOfNotNull(tipAdi.ifBlank { null }, kayit.aciklama).joinToString(" · ")

    ListItem(
        headlineContent = { Text(kayit.gorunenNumara) },
        supportingContent = altBilgi.ifBlank { null }?.let { { Text(it) } },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = kayit.durum, onCheckedChange = durumDegistir)
                IconButton(onClick = sil) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.sil))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EklemeDiyalogu(
    tipler: List<EslesmeTipi>,
    kapat: () -> Unit,
    kaydet: (String, String, Int) -> Unit
) {
    var numara by rememberSaveable { mutableStateOf("") }
    var aciklama by rememberSaveable { mutableStateOf("") }
    var tipId by rememberSaveable { mutableIntStateOf(tipler.firstOrNull()?.id ?: EslesmeTipi.TAM) }
    val secili = tipler.firstOrNull { it.id == tipId }

    AlertDialog(
        onDismissRequest = kapat,
        title = { Text(stringResource(R.string.numara_ekle)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    tipler.forEachIndexed { sira, tip ->
                        SegmentedButton(
                            selected = tip.id == tipId,
                            onClick = { tipId = tip.id },
                            shape = SegmentedButtonDefaults.itemShape(sira, tipler.size)
                        ) { Text(tip.ad) }
                    }
                }
                OutlinedTextField(
                    value = numara,
                    onValueChange = { numara = it },
                    label = { Text(stringResource(R.string.numara)) },
                    placeholder = { secili?.let { Text(it.ornek) } },
                    supportingText = { secili?.let { Text(it.aciklama) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (tipId == EslesmeTipi.DESEN) KeyboardType.Ascii else KeyboardType.Phone
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = aciklama,
                    onValueChange = { aciklama = it },
                    label = { Text(stringResource(R.string.aciklama)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { kaydet(numara, aciklama, tipId) },
                enabled = numara.isNotBlank()
            ) { Text(stringResource(R.string.kaydet)) }
        },
        dismissButton = {
            TextButton(onClick = kapat) { Text(stringResource(R.string.vazgec)) }
        }
    )
}
