package cz.droidboy.wifibeacon;

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
 * @author Jonas Sevcik
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
                return value.isEmpty() || value.length() <= 17;
            }
        });

        EditTextPreference ssid = (EditTextPreference) findPreference(KEY_PREF_SSID);
        ssid.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                return value.isEmpty() || value.length() <= 32;
            }
        });

        EditTextPreference channels = (EditTextPreference) findPreference(KEY_PREF_CHANNELS);
        channels.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                return value.isEmpty() || value.matches("([\\p{XDigit}]{1,2}(,|, )?)+");
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
