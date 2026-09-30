package com.ijad.acremoteeasy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.ir.IrTransmitter
import com.ijad.acremoteeasy.ui.add.AddBrandScreen
import com.ijad.acremoteeasy.ui.favorites.FavoritesScreen
import com.ijad.acremoteeasy.ui.home.HomeScreen
import com.ijad.acremoteeasy.ui.remote.RemoteScreen
import com.ijad.acremoteeasy.ui.timer.TimerScreen

object Routes {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val TIMERS = "timers"
    const val ADD = "add"
    const val REMOTE = "remote/{deviceId}"

    fun remote(deviceId: String) = "remote/$deviceId"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AcRemoteNavHost(
    repository: AppRepository,
    irTransmitter: IrTransmitter
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBottomBar = current in setOf(Routes.HOME, Routes.FAVORITES, Routes.TIMERS)

    val tabs = listOf(
        Tab(Routes.HOME, "Home", Icons.Outlined.Home),
        Tab(Routes.FAVORITES, "Favorites", Icons.Outlined.FavoriteBorder),
        Tab(Routes.TIMERS, "Timers", Icons.Outlined.Timer)
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    repository = repository,
                    hasIr = irTransmitter.hasIrEmitter,
                    onAddDevice = { navController.navigate(Routes.ADD) },
                    onOpenDevice = { id -> navController.navigate(Routes.remote(id)) }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    repository = repository,
                    irTransmitter = irTransmitter
                )
            }
            composable(Routes.TIMERS) {
                TimerScreen(repository = repository)
            }
            composable(Routes.ADD) {
                AddBrandScreen(
                    repository = repository,
                    irTransmitter = irTransmitter,
                    onBack = { navController.popBackStack() },
                    onDone = { deviceId ->
                        navController.popBackStack()
                        navController.navigate(Routes.remote(deviceId))
                    }
                )
            }
            composable(
                route = Routes.REMOTE,
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { entry ->
                val deviceId = entry.arguments?.getString("deviceId").orEmpty()
                RemoteScreen(
                    deviceId = deviceId,
                    repository = repository,
                    irTransmitter = irTransmitter,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
