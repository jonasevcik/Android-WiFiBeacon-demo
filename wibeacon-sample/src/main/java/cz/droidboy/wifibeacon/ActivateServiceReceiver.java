package cz.droidboy.wifibeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;

/**
 * @author Jonas Sevcik
 */
public class ActivateServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled() || Build.VERSION.SDK_INT >= 18 && wifiManager.isScanAlwaysAvailable()) {
                context.startService(new Intent(context, WiFiDetectionService.class));
            }
        }
    }
}
