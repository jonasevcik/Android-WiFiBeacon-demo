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
public class ProximityScanner implements ContinuousReceiver.ScanResultsListener {

    private static final String TAG = ProximityScanner.class.getSimpleName();

    private ScanFilter ragingFilter;
    private RangingListener rangingListener;
    private ContinuousReceiver rangingReceiver;
    private SimpleArrayMap<String, AveragedScanResult> averagedResults = new SimpleArrayMap<>();
    private static final Comparator<ScanResult> SCAN_RESULT_COMPARATOR = new Comparator<ScanResult>() {
        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {
            return rhs.level - lhs.level;
        }
    };

    public ProximityScanner(@NonNull Context context) {
        rangingReceiver = new ContinuousReceiver(context, this, ContinuousReceiver.INTERVAL_IMMEDIATE);
    }

    public void setRangingListener(@NonNull RangingListener rangingListener) {
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
            Collections.sort(processedResults, SCAN_RESULT_COMPARATOR);
            rangingListener.onAPsDiscovered(processedResults);
        }
    }

    public interface RangingListener {

        /**
         * Called on every scan update, if returned results match specified ScanFilter
         *
         * @param results sorted by RSSI
         */
        void onAPsDiscovered(List<ScanResult> results);
    }
}
