package cz.droidboy.wifibeacon;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * @author Jonas Sevcik
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (Build.VERSION.SDK_INT >= 18) {
            if (!wifiManager.isScanAlwaysAvailable()) {
                startActivity(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE));
            }
        } else if (!wifiManager.isWifiEnabled()) {
            showWifiDialog();
        }
    }

    private void showWifiDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(ActivateWiFiDialogFragment.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = new ActivateWiFiDialogFragment();
        newFragment.show(ft, ActivateWiFiDialogFragment.TAG);
    }

}
