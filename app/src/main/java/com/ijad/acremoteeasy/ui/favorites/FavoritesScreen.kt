package com.ijad.acremoteeasy.ui.favorites

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FlashOn
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.data.BrandPackLoader
import com.ijad.acremoteeasy.data.FavoriteAction
import com.ijad.acremoteeasy.ir.IrTransmitter
import com.ijad.acremoteeasy.ui.components.EmptyState
import com.ijad.acremoteeasy.ui.components.HintCard
import com.ijad.acremoteeasy.ui.components.IrUnavailableBanner
import com.ijad.acremoteeasy.ui.components.SoftCard
import com.ijad.acremoteeasy.ui.components.WideRemoteButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    repository: AppRepository,
    irTransmitter: IrTransmitter
) {
    val favorites by repository.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Favorites")
                        Text(
                            "One-tap actions you use most",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!irTransmitter.hasIrEmitter) {
                item { IrUnavailableBanner() }
            }
            if (favorites.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "No favorites yet",
                        body = "Open a remote and tap the heart on Power — or add more favorites from there later. Keep your everyday actions one tap away."
                    )
                }
                item {
                    HintCard("Favorites store the command key + brand pack, then transmit when you tap.")
                }
            } else {
                items(favorites, key = { it.id }) { fav ->
                    FavoriteRow(
                        favorite = fav,
                        enabled = irTransmitter.hasIrEmitter,
                        onSend = {
                            val pack = BrandPackLoader.load(context, fav.brandId)
                            val cmd = pack?.commands?.get(fav.commandKey)
                            if (pack == null || cmd == null) {
                                scope.launch { snackbar.showSnackbar("Missing IR pattern") }
                            } else {
                                val result = irTransmitter.transmit(pack.frequencyHz, cmd.pattern)
                                scope.launch {
                                    snackbar.showSnackbar(
                                        if (result.success) "Sent ${fav.label}" else result.message
                                    )
                                }
                            }
                        },
                        onDelete = {
                            scope.launch { repository.removeFavorite(fav.id) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: FavoriteAction,
    enabled: Boolean,
    onSend: () -> Unit,
    onDelete: () -> Unit
) {
    SoftCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(favorite.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    favorite.deviceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remove favorite")
            }
        }
        Spacer(Modifier.height(8.dp))
        WideRemoteButton(
            label = "Send now",
            icon = Icons.Outlined.FlashOn,
            onClick = onSend,
            enabled = enabled
        )
    }
}
