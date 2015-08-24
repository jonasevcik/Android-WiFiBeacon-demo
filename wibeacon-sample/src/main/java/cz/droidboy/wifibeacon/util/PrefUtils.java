package cz.droidboy.wifibeacon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import cz.droidboy.wibeacon.range.Proximity;
import cz.droidboy.wibeacon.range.ScanFilter;
import cz.droidboy.wifibeacon.SettingsFragment;

/**
 * @author Jonas Sevcik
 */
public class PrefUtils {

    @Nullable
    public static ScanFilter prepareFilter(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String mac = prefs.getString(SettingsFragment.KEY_PREF_MAC, null);
        String ssid = prefs.getString(SettingsFragment.KEY_PREF_SSID, null);
        String[] parsedChannels = prefs.getString(SettingsFragment.KEY_PREF_CHANNELS, "").split("[,|, ]");
        Set<Integer> channels = null;
        if (parsedChannels.length > 0 && !parsedChannels[0].isEmpty()) {
            channels = new HashSet<>();
            for (String parsedChannel : parsedChannels) {
                parsedChannel = parsedChannel.trim();
                if (!parsedChannel.isEmpty()) {
                    channels.add(Integer.parseInt(parsedChannel));
                }
            }
        }
        Proximity proximity = Proximity.values()[Integer.parseInt(prefs.getString(SettingsFragment.KEY_PREF_PROXIMITY, "0"))];
        if (proximity == Proximity.UNKNOWN) {
            proximity = null;
        }
        if (mac != null && mac.isEmpty()) {
            mac = null;
        }
        if (ssid != null && ssid.isEmpty()) {
            ssid = null;
        }
        if (mac == null && ssid == null && channels == null && proximity == null) {
            return null;
        } else {
            return new ScanFilter.Builder()
                    .setMac(mac)
                    .setSsid(ssid)
                    .setChannels(channels)
                    .setProximity(proximity)
                    .build();
        }
    }

}
