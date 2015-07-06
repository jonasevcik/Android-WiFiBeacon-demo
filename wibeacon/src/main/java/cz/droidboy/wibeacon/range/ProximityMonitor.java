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

    public ProximityMonitor(@NonNull Context context) {
        monitoringReceiver = new ContinuousReceiver(context, this, ContinuousReceiver.INTERVAL_IMMEDIATE);
    }

    public void setMonitoringListener(@NonNull MonitoringListener monitoringListener) {
        this.monitoringListener = monitoringListener;
    }

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
