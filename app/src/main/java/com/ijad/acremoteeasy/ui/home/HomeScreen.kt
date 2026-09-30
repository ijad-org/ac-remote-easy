package com.ijad.acremoteeasy.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ijad.acremoteeasy.data.AcDevice
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.ui.components.EmptyState
import com.ijad.acremoteeasy.ui.components.HintCard
import com.ijad.acremoteeasy.ui.components.IrUnavailableBanner
import com.ijad.acremoteeasy.ui.components.SoftCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: AppRepository,
    hasIr: Boolean,
    onAddDevice: () -> Unit,
    onOpenDevice: (String) -> Unit
) {
    val devices by repository.devices.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AC Remote Easy")
                        Text(
                            "Calm control for your air conditioner",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDevice,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add device")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!hasIr) {
                item { IrUnavailableBanner() }
            }
            if (devices.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.DevicesOther,
                        title = "No devices yet",
                        body = "Add your AC brand, then test a few codes pointed at the unit. It only takes a minute.",
                        actionLabel = "Add your first AC",
                        onAction = onAddDevice
                    )
                }
                item {
                    HintCard(
                        "Tip: Point the top of your phone at the AC from about 1–3 meters. " +
                            "Placeholder brand packs ship for structure — expand real IR captures before relying on them."
                    )
                }
            } else {
                item {
                    Text(
                        "My devices",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(devices, key = { it.id }) { device ->
                    DeviceRow(device = device, onClick = { onOpenDevice(device.id) })
                }
                item {
                    Spacer(Modifier.height(72.dp))
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: AcDevice, onClick: () -> Unit) {
    SoftCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                Icons.Outlined.Air,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        append(device.brandName)
                        if (device.verified) append(" · Tested") else append(" · Needs testing")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
