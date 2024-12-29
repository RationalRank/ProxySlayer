# ProxyKiller
Quickly disable Android proxy settings when you can't access your computer. Essential for when you're on the move and need to restore direct internet access.

## Prerequisites
Before using the app, you need to:

1. Grant system settings permission:
    - Install the app
    - Open the app
    - Tap "Grant Permission" when prompted
    - Enable the permission in Android settings

2. Grant WRITE_SECURE_SETTINGS permission via ADB (the app will show you the exact command):
```bash
adb shell pm grant dev.ranjith.proxyslayer android.permission.WRITE_SECURE_SETTINGS
```

## Usage
1. Open the app
2. Once both permissions are granted, you'll see the current proxy status
3. Tap "Disable Proxy" button to remove proxy settings

## Developer Notes
- The app requires two permissions:
    1. System settings permission (granted through Android settings)
    2. WRITE_SECURE_SETTINGS permission (granted via ADB)
- The app automatically refreshes proxy status every 3 seconds
- No root access required

## ProxyMan Helper Functions
For developers using ProxyMan, you can add these helper bash functions to quickly enable/disable proxy:
<script src="https://gist.github.com/RationalRank/380017672f2bec6a19c0e5e89de565ab.js"></script>

Add these functions to your .zshrc or .bashrc to use commands like:
`enable_proxy` - Turn on ProxyMan proxy
`disable_proxy` - Turn off proxy
`toggle_proxy` - Toggle between on/off states