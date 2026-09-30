package com.ijad.acremoteeasy.ui.timer

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ijad.acremoteeasy.data.AcDevice
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.data.TimerStub
import com.ijad.acremoteeasy.ui.components.EmptyState
import com.ijad.acremoteeasy.ui.components.HintCard
import com.ijad.acremoteeasy.ui.components.SoftCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(repository: AppRepository) {
    val timers by repository.timers.collectAsStateWithLifecycle(initialValue = emptyList())
    val devices by repository.devices.collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    var selectedDevice by remember { mutableStateOf<AcDevice?>(null) }
    var minutes by remember { mutableIntStateOf(30) }
    var action by remember { mutableStateOf("power") }
    var deviceMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Timers")
                        Text(
                            "Schedule power or comfort actions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SoftCard {
                    Text("New timer", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "v1 stores timer stubs in the app. Background exact-alarm firing ships later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    if (devices.isEmpty()) {
                        Text(
                            "Add a device first to create a timer.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = deviceMenuOpen,
                            onExpandedChange = { deviceMenuOpen = it }
                        ) {
                            OutlinedTextField(
                                value = selectedDevice?.name ?: "Choose device",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Device") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviceMenuOpen)
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = deviceMenuOpen,
                                onDismissRequest = { deviceMenuOpen = false }
                            ) {
                                devices.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d.name) },
                                        onClick = {
                                            selectedDevice = d
                                            deviceMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text("In how many minutes?", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(15, 30, 60, 120).forEach { m ->
                                FilterChip(
                                    selected = minutes == m,
                                    onClick = { minutes = m },
                                    label = { Text("${m}m") }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Action", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("power" to "Power", "temp_down" to "Cooler", "mode" to "Mode").forEach { (key, label) ->
                                FilterChip(
                                    selected = action == key,
                                    onClick = { action = key },
                                    label = { Text(label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val device = selectedDevice ?: return@Button
                                scope.launch {
                                    repository.addTimer(
                                        device = device,
                                        label = "$action in ${minutes}m",
                                        minutesFromNow = minutes,
                                        commandKey = action
                                    )
                                }
                            },
                            enabled = selectedDevice != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null)
                                Text("Add timer stub")
                            }
                        }
                    }
                }
            }

            if (timers.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Timer,
                        title = "No timers yet",
                        body = "Create a stub above — for example “Power off in 30 minutes”. Exact background alarms are planned; the UI is ready now."
                    )
                }
                item {
                    HintCard("Polish tip: keep timers short and labeled by room so they’re easy to scan.")
                }
            } else {
                item {
                    Text(
                        "Scheduled",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(timers, key = { it.id }) { timer ->
                    TimerRow(
                        timer = timer,
                        onToggle = { scope.launch { repository.toggleTimer(timer.id) } },
                        onDelete = { scope.launch { repository.removeTimer(timer.id) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerRow(
    timer: TimerStub,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    SoftCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(timer.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${timer.deviceName} · ${timer.minutesFromNow} min · ${timer.commandKey}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = timer.enabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete timer")
            }
        }
    }
}
