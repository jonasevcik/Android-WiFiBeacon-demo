package cz.droidboy.wibeacon.range;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.droidboy.wibeacon.ContinuousReceiver;

/**
 * @author Jonas Sevcik
 */
public class ProximityScanner {

    private static final String TAG = ProximityScanner.class.getSimpleName();

    private ScanFilter ragingFilter;
    private RangingListener rangingListener;
    private ContinuousReceiver rangingReceiver;
    private SimpleArrayMap<String, AveragedScanResult> averagedResults = new SimpleArrayMap<>();
    private Comparator<ScanResult> mScanResultComparator = new Comparator<ScanResult>() {
        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {
            return rhs.level - lhs.level;
        }
    };

    private ScanFilter monitoringFilter;
    private MonitoringListener monitoringListener;
    private ContinuousReceiver monitoringReceiver;
    private Set<String> monitoredResults = new HashSet<>();

    public ProximityScanner(@NonNull Context context) {
        rangingReceiver = new ContinuousReceiver(context, new ContinuousReceiver.ScanResultsListener() {
            @Override
            public void onScanResultsReceived(List<ScanResult> results) {
                if (rangingListener != null) {
                    List<ScanResult> processedResults = new ArrayList<>();
                    for (int i = 0, resultsSize = results.size(); i < resultsSize; i++) {
                        ScanResult result = results.get(i);
                        if (ragingFilter == null || ragingFilter.matchesStart(result)) {
                            AveragedScanResult oldResult = averagedResults.get(result.BSSID);
                            if (oldResult == null) {
                                oldResult = new AveragedScanResult(result);
                                averagedResults.put(result.BSSID, oldResult);
                            } else {
                                oldResult.averageRssi(result);
                            }
                            processedResults.add(oldResult.getScanResult());
                        }
                    }
                    Collections.sort(processedResults, mScanResultComparator);
                    rangingListener.onAPsDiscovered(processedResults);
                }
            }
        }, ContinuousReceiver.INTERVAL_IMMEDIATE);

        monitoringReceiver = new ContinuousReceiver(context, new ContinuousReceiver.ScanResultsListener() {
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
        }, ContinuousReceiver.INTERVAL_IMMEDIATE);
    }

    public void setRangingListener(RangingListener rangingListener) {
        this.rangingListener = rangingListener;
    }

    public void startRangingAPs(@Nullable ScanFilter filter) {
        ragingFilter = filter;
        rangingReceiver.startScanning(true);
    }

    public void stopRangingAPs() {
        rangingReceiver.stopScanning();
        ragingFilter = null;
        averagedResults.clear();
    }

    public void setMonitoringListener(MonitoringListener monitoringListener) {
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

    public interface RangingListener {

        /**
         * Called on every scan update, if returned results match specified ScanFilter
         *
         * @param results sorted by RSSI
         */
        void onAPsDiscovered(List<ScanResult> results);
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
