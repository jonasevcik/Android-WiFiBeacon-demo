package cz.droidboy.wibeacon.range;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.droidboy.wibeacon.ContinuousReceiver;

/**
 * @author Jonas Sevcik
 */
public class ProximityMonitor implements ContinuousReceiver.ScanResultsListener {

    private ScanFilter monitoringFilter;
    private MonitoringListener monitoringListener;
    private ContinuousReceiver monitoringReceiver;
    private Set<String> monitoredResults = new HashSet<>();

    /**
     * @param context
     * @param monitoringListener for publishing monitoring events
     * @throws NullPointerException if {@code context} or {@code monitoringListener} is null
     */
    public ProximityMonitor(@NonNull Context context, @NonNull MonitoringListener monitoringListener) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        if (monitoringListener == null) {
            throw new NullPointerException("monitoringListener == null");
        }
        monitoringReceiver = new ContinuousReceiver(context, this, ContinuousReceiver.INTERVAL_IMMEDIATE);
        this.monitoringListener = monitoringListener;
    }

    public void setMonitoringListener(@NonNull MonitoringListener monitoringListener) {
        this.monitoringListener = monitoringListener;
    }

    /**
     * Initiates monitoring of surrounding APs, specified by the filter.
     * Don't forget to call {@link #stopMonitoringAPs()} when done.
     *
     * Note that the {@link android.net.wifi.ScanResult} updates may be faster than this rate if another app is receiving
     * updates at a faster rate, or slower than this rate, or there may be no updates at all
     * (if the device has no connectivity, for example).
     *
     * @param filter for filtering results; if null, no filtering is applied
     * @param scanInterval preferred delay between scans [millis]; cannot be negative
     */
    public void startMonitoringAPs(@Nullable ScanFilter filter, int scanInterval) {
        monitoringFilter = filter;
        monitoringReceiver.changeScanInterval(scanInterval);
        monitoringReceiver.startScanning(true);
    }

    public void stopMonitoringAPs() {
        monitoringReceiver.stopScanning();
        monitoredResults.clear();
        monitoringFilter = null;
    }

    @Override
    public void onScanResultsReceived(List<ScanResult> results) {
        if (monitoringListener != null) {
            List<ScanResult> enteredResults = new ArrayList<>();
            Set<String> enteredMacs = new HashSet<>();
            boolean matchedPreviousResults = true;
            for (int i = 0, resultsSize = results.size(); i < resultsSize; i++) {
                ScanResult result = results.get(i);
                if (monitoringFilter == null || monitoringFilter.matchesStart(result)) {
                    enteredResults.add(result);
                    enteredMacs.add(result.BSSID);
                    if (matchedPreviousResults && !monitoredResults.contains(result.BSSID)) {
                        matchedPreviousResults = false;
                    }
                }
            }
            if (!enteredResults.isEmpty()) {
                if (matchedPreviousResults) {
                    monitoringListener.onDwellRegion(enteredResults);
                } else {
                    monitoringListener.onEnterRegion(enteredResults);
                }
            } else if (!monitoredResults.isEmpty()) {
                monitoringListener.onExitRegion(monitoringFilter);
            }

            monitoredResults = enteredMacs;
        }
    }

    public interface MonitoringListener {
        /**
         * Called when at least one AP matching ScanFilter is found
         *
         * @param results filtered ScanResults
         */
        void onEnterRegion(List<ScanResult> results);

        /**
         * Called when no results changed
         *
         * @param results filtered ScanResults
         */
        void onDwellRegion(List<ScanResult> results);

        /**
         * Called when no APs matching ScanFilter are found
         *
         * @param filter used for filtering ScanResults
         */
        void onExitRegion(ScanFilter filter);
    }
}
