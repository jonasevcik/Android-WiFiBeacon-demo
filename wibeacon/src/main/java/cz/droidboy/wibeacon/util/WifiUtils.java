package cz.droidboy.wibeacon.util;

/**
 * @author Jonas Sevcik
 */
public final class WifiUtils {

    private WifiUtils(){}

    /**
     * Converts frequency to channel number.
     *
     * @param freq frequency obtained via {@link android.net.wifi.ScanResult}
     * @return channel number, -1 if the frequency is out of range
     */
    public static int toChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }

}
