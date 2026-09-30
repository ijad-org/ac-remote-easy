package com.ijad.acremoteeasy.ui.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ijad.acremoteeasy.data.AcMode
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.data.BrandPackLoader
import com.ijad.acremoteeasy.data.FanSpeed
import com.ijad.acremoteeasy.ir.IrTransmitter
import com.ijad.acremoteeasy.ir.LgIrCodec
import com.ijad.acremoteeasy.ui.components.GridRemoteCell
import com.ijad.acremoteeasy.ui.components.IrUnavailableBanner
import com.ijad.acremoteeasy.ui.components.LcdStatusStrip
import com.ijad.acremoteeasy.ui.components.PowerHeroButton
import com.ijad.acremoteeasy.ui.components.WideRemoteButton
import kotlinx.coroutines.launch

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
    var powerPulse by remember { mutableIntStateOf(0) }
    var moreOpen by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val canSend = irTransmitter.hasIrEmitter && brand != null

    fun send(key: String, feedbackLabel: String = key) {
        val pack = brand ?: return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val frequencyHz: Int
        val pattern: IntArray
        if (pack.id == "lg") {
            frequencyHz = LgIrCodec.FREQUENCY_HZ
            pattern = when (key) {
                "swing" -> LgIrCodec.swingPattern()
                "direction" -> LgIrCodec.swingHorizontalPattern()
                "power" -> LgIrCodec.patternFor(poweredOn, mode, temperature, fan)
                else -> {
                    if (!poweredOn) poweredOn = true
                    LgIrCodec.patternFor(true, mode, temperature, fan)
                }
            }
        } else {
            val mapped = when (key) {
                "direction" -> "swing"
                "speed" -> "fan"
                else -> key
            }
            val cmd = pack.commands[mapped] ?: pack.commands[key]
            if (cmd == null) {
                scope.launch { snackbar.showSnackbar("No pattern for $feedbackLabel in this pack") }
                return
            }
            frequencyHz = pack.frequencyHz
            pattern = cmd.pattern
        }
        val result = irTransmitter.transmit(frequencyHz, pattern)
        if (key == "power") powerPulse++
        scope.launch {
            snackbar.showSnackbar(if (result.success) feedbackLabel else result.message)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (device == null || brand == null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Device not found", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                WideRemoteButton(label = "Go back", onClick = onBack)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp)
                .padding(top = 48.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!irTransmitter.hasIrEmitter) {
                IrUnavailableBanner()
            }

            // Upper status / display (~top third)
            LcdStatusStrip(
                brandName = device.brandName,
                deviceName = device.name,
                poweredOn = poweredOn,
                modeLabel = mode.label.uppercase(),
                temperature = temperature,
                fanLabel = fan.label.uppercase(),
                expanded = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.34f)
            )

            // Row 1: Power | Mode (equal cells)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.18f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 3.dp
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PowerHeroButton(
                            onClick = {
                                poweredOn = !poweredOn
                                send("power", if (poweredOn) "Power On" else "Power Off")
                            },
                            enabled = canSend,
                            poweredOn = poweredOn,
                            size = 88.dp,
                            icon = Icons.Outlined.PowerSettingsNew,
                            pulseKey = powerPulse
                        )
                    }
                }
                GridRemoteCell(
                    label = "Mode",
                    subtitle = mode.label,
                    onClick = {
                        val next = AcMode.entries[(mode.ordinal + 1) % AcMode.entries.size]
                        mode = next
                        send("mode", "Mode ${next.label}")
                    },
                    enabled = canSend,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
            }

            // Row 2: Speed | Direction | Swing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.14f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GridRemoteCell(
                    label = "Speed",
                    subtitle = fan.label,
                    onClick = {
                        val next = FanSpeed.entries[(fan.ordinal + 1) % FanSpeed.entries.size]
                        fan = next
                        send("speed", "Fan ${next.label}")
                    },
                    enabled = canSend,
                    modifier = Modifier.weight(1f).fillMaxSize()
                )
                GridRemoteCell(
                    label = "Direction",
                    icon = Icons.Outlined.SwapHoriz,
                    onClick = { send("direction", "Direction") },
                    enabled = canSend,
                    modifier = Modifier.weight(1f).fillMaxSize()
                )
                GridRemoteCell(
                    label = "Swing",
                    icon = Icons.Outlined.SwapVert,
                    onClick = { send("swing", "Swing") },
                    enabled = canSend,
                    modifier = Modifier.weight(1f).fillMaxSize()
                )
            }

            // Row 3: − Temp +
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.14f),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            temperature = (temperature - 1).coerceIn(16, 30)
                            send("temp_down", "Temp $temperature°")
                        },
                        enabled = canSend,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Text("−", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$temperature°",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Temp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = {
                            temperature = (temperature + 1).coerceIn(16, 30)
                            send("temp_up", "Temp $temperature°")
                        },
                        enabled = canSend,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Row 4: Timer | Sleep | more (…)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.14f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GridRemoteCell(
                    label = "Timer",
                    icon = Icons.Outlined.Timer,
                    onClick = {
                        scope.launch {
                            snackbar.showSnackbar("Timers: open the Timers tab from Home")
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxSize()
                )
                GridRemoteCell(
                    label = "Sleep",
                    icon = Icons.Outlined.Bedtime,
                    onClick = {
                        scope.launch {
                            snackbar.showSnackbar("Sleep IR not in this pack yet")
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxSize()
                )
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    GridRemoteCell(
                        label = "…",
                        icon = Icons.Outlined.MoreHoriz,
                        onClick = { moreOpen = true },
                        modifier = Modifier.fillMaxSize()
                    )
                    DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Add Power to favorites") },
                            onClick = {
                                moreOpen = false
                                scope.launch {
                                    repository.addFavorite(device, "power", "Power")
                                    snackbar.showSnackbar("Added Power to favorites")
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove device") },
                            onClick = {
                                moreOpen = false
                                scope.launch {
                                    repository.removeDevice(device.id)
                                    onBack()
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }

            Text(
                "Not affiliated with ${device.brandName} or other AC brands.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Edge back + light title (not a mini app bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, tonalElevation = 2.dp, shadowElevation = 2.dp) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
            Text(
                "${device.brandName} AC",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.size(48.dp)) // balance back button
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}
