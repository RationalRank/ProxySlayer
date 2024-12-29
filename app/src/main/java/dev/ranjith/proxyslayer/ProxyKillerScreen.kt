package dev.ranjith.proxyslayer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.ranjith.proxyslayer.ProxyManager.disableProxy
import dev.ranjith.proxyslayer.ProxyManager.getProxyStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun ProxyKillerScreen(modifier: Modifier = Modifier) {
    var hasWriteSettingsPermission by remember { mutableStateOf(false) }
    var hasSecureSettingsPermission by remember { mutableStateOf(false) }
    var proxyStatus by remember { mutableStateOf("") }
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        hasWriteSettingsPermission = Settings.System.canWrite(context)
        hasSecureSettingsPermission = ProxyManager.hasRequiredPermission(context)

        // Start polling if we have permissions
        if (hasWriteSettingsPermission) {
            val job = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    hasWriteSettingsPermission = Settings.System.canWrite(context)
                    hasSecureSettingsPermission = ProxyManager.hasRequiredPermission(context)
                    // Only check proxy status if we have both permissions
                    if (hasWriteSettingsPermission && hasSecureSettingsPermission) {
                        proxyStatus = getProxyStatus(context)
                    }
                    delay(3.seconds.inWholeMilliseconds)
                }
            }

            onPauseOrDispose {
                job.cancel()
            }
        } else {
            onPauseOrDispose {
                // no-op if we never started polling
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            !hasWriteSettingsPermission -> {
                WriteSettingsPermissionCard(context)
            }

            !hasSecureSettingsPermission -> {
                SecureSettingsPermissionCard(context.packageName)
            }

            else -> {
                ProxyControlsContent(
                    proxyStatus = proxyStatus,
                    onDisableProxy = {
                        disableProxy(context)
                        proxyStatus = getProxyStatus(context)
                    }
                )
            }
        }
    }
}

@Composable
private fun WriteSettingsPermissionCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "System Settings Permission Required",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This app needs permission to modify system settings. Please grant the permission in the next screen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:" + context.packageName)
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun SecureSettingsPermissionCard(packageName: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showCopiedToast by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Permission Required",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This app requires special permission to modify proxy settings. Please run this command from your computer:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))

            val adbCommand =
                "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SelectionContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 48.dp) // Make space for the button
                ) {
                    Text(
                        text = adbCommand,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(adbCommand))
                        showCopiedToast = true
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy to clipboard",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    if (showCopiedToast) {
        Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
        showCopiedToast = false
    }
}

@Composable
private fun ProxyControlsContent(
    proxyStatus: String,
    onDisableProxy: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = "Warning",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(48.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "ProxyKiller",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Proxy Status:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = proxyStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onDisableProxy,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text("Disable Proxy")
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "This will remove all global proxy settings from your device",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
