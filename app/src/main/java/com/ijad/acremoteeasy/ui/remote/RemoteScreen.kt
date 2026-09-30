package com.ijad.acremoteeasy.ui.remote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ijad.acremoteeasy.data.AcMode
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.data.BrandPackLoader
import com.ijad.acremoteeasy.data.FanSpeed
import com.ijad.acremoteeasy.ir.IrTransmitter
import com.ijad.acremoteeasy.ui.components.HintCard
import com.ijad.acremoteeasy.ui.components.IrUnavailableBanner
import com.ijad.acremoteeasy.ui.components.LcdStatusStrip
import com.ijad.acremoteeasy.ui.components.PillRemoteButton
import com.ijad.acremoteeasy.ui.components.PowerRemoteButton
import com.ijad.acremoteeasy.ui.components.RemoteHandsetBody
import com.ijad.acremoteeasy.ui.components.RoundRemoteButton
import com.ijad.acremoteeasy.ui.components.TempControlRow
import com.ijad.acremoteeasy.ui.components.WideRemoteButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(
    deviceId: String,
    repository: AppRepository,
    irTransmitter: IrTransmitter,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val devices by repository.devices.collectAsStateWithLifecycle(initialValue = emptyList())
    val device = devices.firstOrNull { it.id == deviceId }
    val brand = remember(device?.brandId) {
        device?.brandId?.let { BrandPackLoader.load(context, it) }
    }
    var temperature by remember { mutableIntStateOf(24) }
    var mode by remember { mutableStateOf(AcMode.Cool) }
    var fan by remember { mutableStateOf(FanSpeed.Auto) }
    var poweredOn by remember { mutableStateOf(true) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val canSend = irTransmitter.hasIrEmitter && brand != null

    fun send(key: String, feedbackLabel: String = key) {
        val pack = brand ?: return
        val cmd = pack.commands[key]
        if (cmd == null) {
            scope.launch { snackbar.showSnackbar("No pattern for $key in this pack") }
            return
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val result = irTransmitter.transmit(pack.frequencyHz, cmd.pattern)
        scope.launch {
            snackbar.showSnackbar(if (result.success) feedbackLabel else result.message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(device?.name ?: "Remote")
                        Text(
                            device?.brandName ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (device != null && brand != null) {
                        IconButton(onClick = {
                            scope.launch {
                                repository.addFavorite(device, "power", "Power")
                                snackbar.showSnackbar("Added Power to favorites")
                            }
                        }) {
                            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Favorite power")
                        }
                        IconButton(onClick = {
                            scope.launch {
                                repository.removeDevice(device.id)
                                onBack()
                            }
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove device")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (device == null || brand == null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Device not found", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                WideRemoteButton(label = "Go back", onClick = onBack)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!irTransmitter.hasIrEmitter) {
                IrUnavailableBanner()
            }

            RemoteHandsetBody {
                LcdStatusStrip(
                    brandName = device.brandName,
                    deviceName = device.name,
                    poweredOn = poweredOn,
                    modeLabel = mode.label.uppercase(),
                    temperature = temperature,
                    fanLabel = fan.label.uppercase()
                )

                PowerRemoteButton(
                    poweredOn = poweredOn,
                    onClick = {
                        poweredOn = !poweredOn
                        send("power", if (poweredOn) "Power On" else "Power Off")
                    },
                    enabled = canSend,
                    icon = Icons.Outlined.PowerSettingsNew
                )

                Text(
                    if (device.verified) "Tested pack" else "Untested pack",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TempControlRow(
                    temperature = temperature,
                    onDown = {
                        temperature = (temperature - 1).coerceIn(16, 30)
                        send("temp_down", "Temp $temperature°")
                    },
                    onUp = {
                        temperature = (temperature + 1).coerceIn(16, 30)
                        send("temp_up", "Temp $temperature°")
                    },
                    enabled = canSend
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PillRemoteButton(
                        label = "Mode",
                        subtitle = mode.label,
                        icon = Icons.Outlined.Tune,
                        onClick = {
                            val next = AcMode.entries[(mode.ordinal + 1) % AcMode.entries.size]
                            mode = next
                            send("mode", "Mode ${next.label}")
                        },
                        enabled = canSend,
                        modifier = Modifier.weight(1f)
                    )
                    PillRemoteButton(
                        label = "Fan",
                        subtitle = fan.label,
                        icon = Icons.Outlined.Air,
                        onClick = {
                            val next = FanSpeed.entries[(fan.ordinal + 1) % FanSpeed.entries.size]
                            fan = next
                            send("fan", "Fan ${next.label}")
                        },
                        enabled = canSend,
                        modifier = Modifier.weight(1f)
                    )
                    PillRemoteButton(
                        label = "Swing",
                        icon = Icons.Outlined.SwapVert,
                        onClick = { send("swing", "Swing") },
                        enabled = canSend,
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Test codes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("test_1", "test_2", "test_3").forEachIndexed { index, key ->
                            RoundRemoteButton(
                                label = "T${index + 1}",
                                onClick = { send(key, "Test ${index + 1}") },
                                enabled = canSend,
                                size = 64.dp
                            )
                        }
                    }
                }
            }

            HintCard(
                "Optional IR learn: capture your original remote later. " +
                    "v1 ships placeholder timings under assets/brands — expand with measured codes."
            )
            Text(
                "Not affiliated with ${device.brandName} or other AC brands.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
