package com.ijad.acremoteeasy.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.data.BrandPack
import com.ijad.acremoteeasy.data.BrandPackLoader
import com.ijad.acremoteeasy.ir.IrTransmitter
import com.ijad.acremoteeasy.ui.components.HintCard
import com.ijad.acremoteeasy.ui.components.IrUnavailableBanner
import com.ijad.acremoteeasy.ui.components.SoftCard
import com.ijad.acremoteeasy.ui.components.WideRemoteButton
import kotlinx.coroutines.launch

private enum class AddStep { PickBrand, NameDevice, TestCodes, DoneHint }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBrandScreen(
    repository: AppRepository,
    irTransmitter: IrTransmitter,
    onBack: () -> Unit,
    onDone: (deviceId: String) -> Unit
) {
    val context = LocalContext.current
    val brands = remember { BrandPackLoader.loadAll(context) }
    var step by remember { mutableStateOf(AddStep.PickBrand) }
    var selected by remember { mutableStateOf<BrandPack?>(null) }
    var deviceName by remember { mutableStateOf("") }
    var testedOk by remember { mutableStateOf(false) }
    var testsTried by remember { mutableIntStateOf(0) }
    var savedDeviceId by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val stepIndex = when (step) {
        AddStep.PickBrand -> 0
        AddStep.NameDevice -> 1
        AddStep.TestCodes -> 2
        AddStep.DoneHint -> 3
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add AC")
                        Text(
                            when (step) {
                                AddStep.PickBrand -> "Step 1 · Choose brand"
                                AddStep.NameDevice -> "Step 2 · Name your unit"
                                AddStep.TestCodes -> "Step 3 · Test a few codes"
                                AddStep.DoneHint -> "Almost done"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (step) {
                            AddStep.PickBrand -> onBack()
                            AddStep.NameDevice -> step = AddStep.PickBrand
                            AddStep.TestCodes -> step = AddStep.NameDevice
                            AddStep.DoneHint -> savedDeviceId?.let(onDone) ?: onBack()
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
                progress = { (stepIndex + 1) / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            if (!irTransmitter.hasIrEmitter) {
                IrUnavailableBanner(Modifier.padding(bottom = 12.dp))
            }

            when (step) {
                AddStep.PickBrand -> {
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
                                    onClick = { selected = brand },
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
                                            "${brand.commands.size} sample commands · placeholder IR",
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
                                        step = AddStep.NameDevice
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

                AddStep.NameDevice -> {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
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
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { step = AddStep.TestCodes },
                            enabled = deviceName.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Continue to test") }
                    }
                }

                AddStep.TestCodes -> {
                    val brand = selected
                    if (brand == null) {
                        LaunchedEffect(Unit) { step = AddStep.PickBrand }
                        return@Column
                    }
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        HintCard(
                            "Point your phone at the AC and try the normal remote buttons " +
                                "(Power, Temp, Mode, Fan, Swing). Mark “It worked” when the unit reacts."
                        )
                        Spacer(Modifier.height(16.dp))
                        listOf("power", "temp_up", "temp_down", "mode", "fan", "swing").forEach { key ->
                            val cmd = brand.commands[key] ?: return@forEach
                            WideRemoteButton(
                                label = "Send ${cmd.label}",
                                onClick = {
                                    val result = irTransmitter.transmit(brand.frequencyHz, cmd.pattern)
                                    testsTried++
                                    scope.launch {
                                        snackbar.showSnackbar(
                                            if (result.success) "Sent ${cmd.label}" else result.message
                                        )
                                    }
                                },
                                enabled = irTransmitter.hasIrEmitter,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = testedOk,
                                onClick = { testedOk = true },
                                label = { Text("It worked") },
                                leadingIcon = if (testedOk) {
                                    { Icon(Icons.Outlined.CheckCircle, null) }
                                } else null
                            )
                            FilterChip(
                                selected = !testedOk && testsTried > 0,
                                onClick = { testedOk = false },
                                label = { Text("None worked yet") }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        SoftCard {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Outlined.School, null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    "IR learn mode is coming soon. For now, keep the closest brand pack " +
                                        "and refine codes later from assets/brands.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val device = repository.addDevice(
                                        name = deviceName,
                                        brand = brand,
                                        verified = testedOk
                                    )
                                    savedDeviceId = device.id
                                    step = AddStep.DoneHint
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Save device") }
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val device = repository.addDevice(
                                        name = deviceName,
                                        brand = brand,
                                        verified = false
                                    )
                                    onDone(device.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Skip testing & save") }
                    }
                }

                AddStep.DoneHint -> {
                    val newId = savedDeviceId
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Device saved", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (testedOk) "You’re ready to use the remote."
                            else "You can keep testing codes from the remote screen.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        SoftCard {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Outlined.Lightbulb, null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    "Placeholder IR packs may not match your exact model. " +
                                        "Capture real codes before depending on daily use.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { newId?.let(onDone) },
                            enabled = newId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Open remote") }
                    }
                }
            }
        }
    }
}
