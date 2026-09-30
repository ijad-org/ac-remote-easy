package com.ijad.acremoteeasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ijad.acremoteeasy.data.AppRepository
import com.ijad.acremoteeasy.ir.IrTransmitter
import com.ijad.acremoteeasy.ui.navigation.AcRemoteNavHost
import com.ijad.acremoteeasy.ui.theme.AcRemoteEasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcRemoteEasyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val repository = remember { AppRepository(context.applicationContext) }
                    val irTransmitter = remember { IrTransmitter(context.applicationContext) }
                    AcRemoteNavHost(
                        repository = repository,
                        irTransmitter = irTransmitter
                    )
                }
            }
        }
    }
}
