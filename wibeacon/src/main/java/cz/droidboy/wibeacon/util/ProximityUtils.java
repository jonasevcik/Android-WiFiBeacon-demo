package cz.droidboy.wibeacon.util;

import cz.droidboy.wibeacon.range.Proximity;

/**
 * @author Jonas Sevcik
 */
public final class ProximityUtils {

    private ProximityUtils(){}

    /**
     * Calculates distance using Free-space path loss. Constant -27.55 is used for calculations, where frequency is in MHz and distance in meters.
     * FSPL(dB) = 20 log(d) + 20 log(f) - 27.55; d distance from the transmitter [m], f signal frequency [MHz]
     *
     * @param level measured RSSI [dBm]
     * @param freq  WiFi frequency [MHz]
     * @return distance from AP [m]
     */
    public static double calculateDistance(double level, double freq) {
        double exp = (27.55 - (20 * Math.log10(freq)) + Math.abs(level)) / 20.0;
        return Math.pow(10.0, exp);
    }

    public static double calculateIndoorDistance(double level, double freq) {
        double exp = (27.55d - 40d * Math.log10(freq) + 6.7d - level) / 20.0d;
        return Math.pow(10.0d, exp);
    }

    /**
     * Estimates proximity to AP.
     * <p>
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
