package cz.droidboy.wibeacon.range;

import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * @author Jonas Sevcik
 */
public class AveragedScanResult {

    private static final int MAX_AGE = 30_000; //30s

    private ScanResult scanResult;
    /**
     * Timestamp representing date when this result was last averaged, in milliseconds from 1970
     */
    private long seen;

    public AveragedScanResult(@NonNull ScanResult scanResult) {
        this.scanResult = scanResult;
        seen = System.currentTimeMillis();
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void averageRssi(@NonNull ScanResult newResult) {
        if (Build.VERSION.SDK_INT >= 17 && newResult.timestamp == scanResult.timestamp) {
            return;
        }
        int newRssi = newResult.level;
        newResult.level = scanResult.level;
        scanResult = newResult;
        long nowSeen = System.currentTimeMillis();
        long age = nowSeen - seen;

        if (seen > 0 && age > 0 && age < MAX_AGE / 2) {
            // Average the RSSI with previously seen instances of this scan result
            double alpha = 0.5 - (double) age / (double) MAX_AGE;
            scanResult.level = (int) ((double) newRssi * (1 - alpha) + (double) scanResult.level * alpha);
        } else {
            scanResult.level = newRssi;
        }
        seen = nowSeen;
    }

}
