package com.ijad.acremoteeasy.ui.add

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.ijad.acremoteeasy.ui.components.PowerHeroButton
import com.ijad.acremoteeasy.ui.components.SoftCard
import kotlinx.coroutines.launch

/** Three-step pairing: Select → Power test → Save. */
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
    var powerPulse by remember { mutableIntStateOf(0) }
    var showRespondPrompt by remember { mutableStateOf(false) }
    var configIndex by remember { mutableIntStateOf(0) }
    var addHomeShortcut by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lgVariants = remember { LgIrCodec.powerProbeVariants() }
    val configCount = when (selected?.id) {
        "lg" -> lgVariants.size
        else -> 1
    }.coerceAtLeast(1)

    fun openPowerTest(brand: BrandPack) {
        selected = brand
        deviceName = "${brand.name} AC"
        configIndex = 0
        testedOk = false
        testsTried = 0
        powerPulse = 0
        showRespondPrompt = false
        step = AddStep.TestPower
    }

    fun sendPowerProbe() {
        val brand = selected ?: return
        if (!irTransmitter.hasIrEmitter) {
            // Still reveal Yes/No so pairing can proceed on emulators / no-IR phones
            testsTried++
            powerPulse++
            showRespondPrompt = true
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
        powerPulse++
        showRespondPrompt = true
        scope.launch {
            snackbar.showSnackbar(if (result.success) "Sent Power" else result.message)
        }
    }

    fun advanceConfig() {
        if (configCount <= 1) {
            showRespondPrompt = false
            scope.launch { snackbar.showSnackbar("Only one configuration for this brand") }
            return
        }
        configIndex = (configIndex + 1) % configCount
        showRespondPrompt = false
        scope.launch {
            snackbar.showSnackbar("Configuration ${configIndex + 1}/$configCount")
        }
    }

    fun retreatConfig() {
        if (configCount <= 1) {
            scope.launch { snackbar.showSnackbar("Only one configuration for this brand") }
            return
        }
        configIndex = (configIndex - 1 + configCount) % configCount
        showRespondPrompt = false
        scope.launch {
            snackbar.showSnackbar("Configuration ${configIndex + 1}/$configCount")
        }
    }

    fun saveAndOpenRemote(verified: Boolean) {
        val brand = selected ?: return
        scope.launch {
            val device = repository.addDevice(
                name = deviceName.ifBlank { "${brand.name} AC" },
                brand = brand,
                verified = verified
            )
            onDone(device.id)
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

    // Immersive Power-test: hint above Power; Yes/No at bottom; tick saves
    if (step == AddStep.TestPower) {
        val brand = selected
        if (brand == null) {
            LaunchedEffect(Unit) { step = AddStep.SelectAc }
            return
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 56.dp, bottom = if (showRespondPrompt) 200.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    brand.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (configCount > 1) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Configuration ${configIndex + 1}/$configCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        "Point the remote at AC and tap the button. Make sure AC responds.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (configCount > 1) {
                        if (configIndex > 0) {
                            IconButton(
                                onClick = { retreatConfig() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .semantics { contentDescription = "Previous configuration" }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Spacer(Modifier.size(48.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    PowerHeroButton(
                        onClick = { sendPowerProbe() },
                        enabled = true,
                        poweredOn = true,
                        size = 148.dp,
                        icon = Icons.Outlined.PowerSettingsNew,
                        pulseKey = powerPulse
                    )
                    if (configCount > 1) {
                        Spacer(Modifier.width(12.dp))
                        IconButton(
                            onClick = { advanceConfig() },
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
                }
            }

            // Floating back (top-start)
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                IconButton(onClick = { step = AddStep.SelectAc }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            }

            // Tick at top-end: save & open remote (verified if Yes already set testedOk)
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(12.dp)
                    .align(Alignment.TopEnd),
                shape = CircleShape,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                IconButton(
                    onClick = { saveAndOpenRemote(verified = testedOk) },
                    modifier = Modifier.semantics { contentDescription = "Save and open remote" }
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Yes/No at bottom after Power tap
            if (showRespondPrompt) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 6.dp
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
                                onClick = { advanceConfig() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("No") }
                            Button(
                                onClick = {
                                    testedOk = true
                                    saveAndOpenRemote(verified = true)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Yes") }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 56.dp)
            )
        }
        return
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
                            AddStep.SaveDevice -> {
                                showRespondPrompt = testsTried > 0
                                step = AddStep.TestPower
                            }
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
                        "Tap a brand to start the Power test.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(brands, key = { it.id }) { brand ->
                            BrandSelectCard(
                                brand = brand,
                                onClick = { openPowerTest(brand) }
                            )
                        }
                    }
                }

                AddStep.TestPower -> { /* immersive branch above */ }

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
                            onClick = {
                                showRespondPrompt = testsTried > 0
                                step = AddStep.TestPower
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Back to Power test") }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandSelectCard(
    brand: BrandPack,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .semantics { contentDescription = brand.name }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandLettermark(brandId = brand.id, brandName = brand.name, size = 72.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                brand.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (brand.id == "lg") "Documented IR" else "Sample IR",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
