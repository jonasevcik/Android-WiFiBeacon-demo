package cz.droidboy.wifibeacondemo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

import cz.droidboy.wifibeacondemo.SettingsFragment;
import cz.droidboy.wifibeacondemo.wifi.range.Proximity;
import cz.droidboy.wifibeacondemo.wifi.range.ScanFilter;

/**
 * @author Jonas Sevcik
 */
public class ProximityUtils {

    public static ScanFilter prepareFilter(Context context) {
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
            return new ScanFilter(mac, ssid, channels, proximity);
        }
    }

    /**
     * Calculates distance using Free-space path loss. Constant -27.55 is used for calculations, where frequency is in MHz and distance in meters
     *
     * @param level measured RSSI [dBm]
     * @param freq  WiFi frequency [MHz]
     * @return distance from AP [m]
     */
    public static double calculateDistance(double level, double freq) {
        double exp = (27.55 - (20 * Math.log10(freq)) + Math.abs(level)) / 20.0;
        return Math.pow(10.0, exp);
    }

    /**
     * Estimates proximity to AP.
     * <p/>
     * IMMEDIATE - Less than half a meter away
     * NEAR - More than half a meter away, but less than four meters away
     * FAR - More than four meters away
     * UNKNOWN - No distance estimate was possible due to a bad RSS value or measured TX power
     *
     * @param rssi      measured RSSI [dBm]
     * @param frequency [MHz]
     * @return estimated proximity to AP
     */
    public static Proximity getProximity(int rssi, int frequency) {
        double distance = calculateDistance(rssi, frequency);
        if (distance < 0) {
            return Proximity.UNKNOWN;
        } else if (distance < 0.5) {
            return Proximity.IMMEDIATE;
        } else if (distance <= 4.0) {
            return Proximity.NEAR;
        } else {
            return Proximity.FAR;
        }
    }
}
