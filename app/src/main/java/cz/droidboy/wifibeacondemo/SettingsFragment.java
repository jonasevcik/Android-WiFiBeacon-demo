package cz.droidboy.wifibeacondemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by Jonáš Ševčík on 20.11.2014.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_SSID = "pref_ssid";
    public static final String KEY_PREF_MAC = "pref_mac";
    public static final String KEY_PREF_CHANNELS = "pref_channels";
    public static final String KEY_PREF_PROXIMITY = "pref_proximity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        EditTextPreference mac = (EditTextPreference) findPreference(KEY_PREF_MAC);
        mac.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                if (value.isEmpty()) {
                    newValue = null;
                    return true;
                }
                if (value.length() <= 17) {
                    return true;
                }
                return false;
            }
        });

        EditTextPreference ssid = (EditTextPreference) findPreference(KEY_PREF_SSID);
        ssid.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                if (value.isEmpty()) {
                    newValue = null;
                    return true;
                }
                if (value.length() <= 32) {
                    return true;
                }
                return false;
            }
        });

        EditTextPreference channels = (EditTextPreference) findPreference(KEY_PREF_CHANNELS);
        channels.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                if (value.isEmpty()) {
                    newValue = null;
                    return true;
                }
                if (value.matches("([\\p{XDigit}]{1,2}(,|, ){0,1})+")) {
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();
        WifiManager manager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            Intent intent = new Intent(activity, WiFiDetectionService.class);
            intent.putExtra(WiFiDetectionService.UPDATE_COMMAND_KEY, WiFiDetectionService.UPDATE_COMMAND);
            activity.startService(intent);
        }
    }
}
