package dev.ranjith.proxyslayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dev.ranjith.proxyslayer.ui.theme.ProxySlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProxySlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProxyKillerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}