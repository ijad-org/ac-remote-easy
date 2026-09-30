package com.ijad.acremoteeasy.ui.add

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ijad.acremoteeasy.MainActivity
import com.ijad.acremoteeasy.R
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.data.BrandPack
import com.ijad.acremoteeasy.data.BrandPackLoader
import com.ijad.acremoteeasy.ir.IrTransmitter
import com.ijad.acremoteeasy.ir.LgIrCodec
import com.ijad.acremoteeasy.ui.components.HintCard
import com.ijad.acremoteeasy.ui.components.IrUnavailableBanner
import com.ijad.acremoteeasy.ui.components.SoftCard
import kotlinx.coroutines.launch

/** Three-step Mi-inspired pairing: Select → Power test → Save. */
private enum class AddStep { SelectAc, TestPower, SaveDevice }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBrandScreen(
    repository: AppRepository,
    irTransmitter: IrTransmitter,
    onBack: () -> Unit,
    onDone: (deviceId: String) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val brands = remember { BrandPackLoader.loadAll(context) }
    var step by remember { mutableStateOf(AddStep.SelectAc) }
    var selected by remember { mutableStateOf<BrandPack?>(null) }
    var deviceName by remember { mutableStateOf("") }
    var testedOk by remember { mutableStateOf(false) }
    var testsTried by remember { mutableIntStateOf(0) }
    var configIndex by remember { mutableIntStateOf(0) }
    var addHomeShortcut by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lgVariants = remember { LgIrCodec.powerProbeVariants() }
    val configCount = when (selected?.id) {
        "lg" -> lgVariants.size
        else -> 1
    }.coerceAtLeast(1)

    fun sendPowerProbe() {
        val brand = selected ?: return
        if (!irTransmitter.hasIrEmitter) {
            scope.launch { snackbar.showSnackbar("No IR blaster on this device") }
            return
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val frequencyHz: Int
        val pattern: IntArray
        if (brand.id == "lg") {
            frequencyHz = LgIrCodec.FREQUENCY_HZ
            pattern = lgVariants[configIndex.coerceIn(0, lgVariants.lastIndex)]
        } else {
            val cmd = brand.commands["power"]
            if (cmd == null) {
                scope.launch { snackbar.showSnackbar("No Power pattern in this pack") }
                return
            }
            frequencyHz = brand.frequencyHz
            pattern = cmd.pattern
        }
        val result = irTransmitter.transmit(frequencyHz, pattern)
        testsTried++
        scope.launch {
            snackbar.showSnackbar(if (result.success) "Sent Power" else result.message)
        }
    }

    fun advanceConfig() {
        if (configCount <= 1) {
            scope.launch { snackbar.showSnackbar("Only one configuration for this brand") }
            return
        }
        configIndex = (configIndex + 1) % configCount
        scope.launch {
            snackbar.showSnackbar("Configuration ${configIndex + 1}/$configCount")
        }
    }

    fun requestHomeShortcut(deviceId: String, name: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            scope.launch { snackbar.showSnackbar("Home shortcuts need Android 8+") }
            return
        }
        val sm = context.getSystemService(ShortcutManager::class.java) ?: return
        if (!sm.isRequestPinShortcutSupported) {
            scope.launch { snackbar.showSnackbar("Pinning shortcuts not supported here") }
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("deviceId", deviceId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val info = ShortcutInfo.Builder(context, "ac_$deviceId")
            .setShortLabel(name.take(20).ifBlank { "AC Remote" })
            .setLongLabel(name.ifBlank { "AC Remote Easy" })
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()
        sm.requestPinShortcut(info, null)
    }

    val stepIndex = when (step) {
        AddStep.SelectAc -> 0
        AddStep.TestPower -> 1
        AddStep.SaveDevice -> 2
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            when (step) {
                                AddStep.SelectAc -> "Select AC"
                                AddStep.TestPower -> selected?.name ?: "AC"
                                AddStep.SaveDevice -> "Save device"
                            }
                        )
                        Text(
                            when (step) {
                                AddStep.SelectAc -> "Step 1 · Choose brand"
                                AddStep.TestPower -> "Step 2 · Test Power"
                                AddStep.SaveDevice -> "Step 3 · Name & save"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (step) {
                            AddStep.SelectAc -> onBack()
                            AddStep.TestPower -> step = AddStep.SelectAc
                            AddStep.SaveDevice -> step = AddStep.TestPower
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            LinearProgressIndicator(
                progress = { (stepIndex + 1) / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            if (!irTransmitter.hasIrEmitter) {
                IrUnavailableBanner(Modifier.padding(bottom = 12.dp))
            }

            when (step) {
                AddStep.SelectAc -> {
                    Text(
                        "Pick the brand on your AC (or remote).",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(brands, key = { it.id }) { brand ->
                            SoftCard(
                                modifier = Modifier.selectable(
                                    selected = selected?.id == brand.id,
                                    onClick = {
                                        selected = brand
                                        configIndex = 0
                                        if (deviceName.isBlank()) deviceName = "${brand.name} AC"
                                    },
                                    role = Role.RadioButton
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = selected?.id == brand.id,
                                        onClick = null
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(brand.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            if (brand.id == "lg") {
                                                "Classic 28-bit LG IR · GE6711 / 6711A20***"
                                            } else {
                                                "Placeholder IR · probe Power to test"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    selected?.let {
                                        if (deviceName.isBlank()) deviceName = "${it.name} AC"
                                        configIndex = 0
                                        testedOk = false
                                        testsTried = 0
                                        step = AddStep.TestPower
                                    }
                                },
                                enabled = selected != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) { Text("Continue") }
                        }
                    }
                }

                AddStep.TestPower -> {
                    val brand = selected
                    if (brand == null) {
                        LaunchedEffect(Unit) { step = AddStep.SelectAc }
                        return@Column
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Point the remote at the device and tap Power.\nMake sure the AC responds.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Checking available configurations ${configIndex + 1}/$configCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(36.dp))

                        // Power-first row: large Power + optional next-config chevron
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(Modifier.width(56.dp)) // balance chevron
                            Button(
                                onClick = { sendPowerProbe() },
                                enabled = irTransmitter.hasIrEmitter,
                                modifier = Modifier
                                    .size(128.dp)
                                    .semantics { contentDescription = "Power" },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 1.dp
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.PowerSettingsNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            IconButton(
                                onClick = { advanceConfig() },
                                enabled = configCount > 1,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .semantics { contentDescription = "Next configuration" }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            "Power",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )

                        Spacer(Modifier.height(48.dp))

                        // Bottom sheet–style respond card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Does the device respond?",
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            if (configIndex + 1 < configCount) {
                                                configIndex++
                                                scope.launch {
                                                    snackbar.showSnackbar(
                                                        "Try configuration ${configIndex + 1}/$configCount"
                                                    )
                                                }
                                            } else {
                                                scope.launch {
                                                    snackbar.showSnackbar(
                                                        "No more configurations — try another brand or skip"
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) { Text("No") }
                                    Button(
                                        onClick = {
                                            testedOk = true
                                            step = AddStep.SaveDevice
                                        },
                                        enabled = testsTried > 0 || !irTransmitter.hasIrEmitter,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) { Text("Yes") }
                                }
                                if (testsTried == 0 && irTransmitter.hasIrEmitter) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Tap Power first, then answer Yes or No.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                testedOk = false
                                step = AddStep.SaveDevice
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 16.dp)
                        ) { Text("Skip testing & save") }
                    }
                }

                AddStep.SaveDevice -> {
                    val brand = selected
                    if (brand == null) {
                        LaunchedEffect(Unit) { step = AddStep.SelectAc }
                        return@Column
                    }
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        if (testedOk) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Power responded — save this AC.",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        Text("Give this AC a friendly name.", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = deviceName,
                            onValueChange = { deviceName = it },
                            label = { Text("Device name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        HintCard("Examples: Living room, Bedroom, Office.")
                        Spacer(Modifier.height(16.dp))
                        SoftCard {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Outlined.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "Home screen shortcut",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Optional pin so you can open this remote faster.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Checkbox(
                                    checked = addHomeShortcut,
                                    onCheckedChange = { addHomeShortcut = it }
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val device = repository.addDevice(
                                        name = deviceName.ifBlank { "${brand.name} AC" },
                                        brand = brand,
                                        verified = testedOk
                                    )
                                    if (addHomeShortcut) {
                                        requestHomeShortcut(device.id, device.name)
                                    }
                                    onDone(device.id)
                                }
                            },
                            enabled = deviceName.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Save & open remote") }
                        TextButton(
                            onClick = { step = AddStep.TestPower },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Back to Power test") }
                    }
                }
            }
        }
    }
}
