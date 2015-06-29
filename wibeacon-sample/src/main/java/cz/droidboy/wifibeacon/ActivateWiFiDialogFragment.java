package cz.droidboy.wifibeacon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;

/**
 * @author Jonas Sevcik
 */
public class ActivateWiFiDialogFragment extends DialogFragment {

    public static final String TAG = ActivateWiFiDialogFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.alert_dialog_message))
                .setPositiveButton(getString(R.string.alert_dialog_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                                wifiManager.setWifiEnabled(true);
                            }
                        }
                )
                .setNegativeButton(getString(R.string.alert_dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                )
                .create();
    }
}
