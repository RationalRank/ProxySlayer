package dev.ranjith.proxyslayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast

object ProxyManager {

    fun hasRequiredPermission(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun disableProxy(context: Context) {
        try {
            Settings.Global.putString(
                context.contentResolver,
                Settings.Global.HTTP_PROXY,
                ":0"
            )
            Settings.Global.putString(context.contentResolver, "https_proxy", "")
            Toast.makeText(context, "Proxy disabled successfully", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Error: Requires WRITE_SECURE_SETTINGS permission",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getProxyStatus(context: Context): String {
        return try {
            val proxy =
                Settings.Global.getString(context.contentResolver, Settings.Global.HTTP_PROXY)
            if (proxy.isNullOrEmpty() || proxy == ":0") "No proxy configured"
            else "Enabled: $proxy"
        } catch (e: SecurityException) {
            "Unable to read proxy settings"
        }
    }
}