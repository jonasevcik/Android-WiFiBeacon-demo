package cz.droidboy.wifibeacondemo;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;

import cz.droidboy.wifibeacondemo.adapter.APAdapter;
import cz.droidboy.wifibeacondemo.util.ProximityUtils;
import cz.droidboy.wifibeacondemo.wifi.range.ProximityScanner;
import cz.droidboy.wifibeacondemo.wifi.range.ScanFilter;

/**
 * @author Jonas Sevcik
 */
public class MainActivity extends BaseActivity implements ProximityScanner.RangingListener {

    private ProximityScanner scanner;
    private APAdapter adapter;
    private ScanFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setEmptyView(findViewById(android.R.id.empty));
        adapter = new APAdapter(this);
        listView.setAdapter(adapter);

        scanner = new ProximityScanner(this);
        scanner.setRangingListener(this);

        startService(new Intent(MainActivity.this, WiFiDetectionService.class));

    }

    @Override
    protected void onResume() {
        super.onResume();
        filter = ProximityUtils.prepareFilter(this);
        scanner.startRangingAPs(filter);
    }

    @Override
    protected void onPause() {
        scanner.stopRangingAPs();
        super.onPause();
    }

    @Override
    public void onAPsDiscovered(List<ScanResult> results) {
        adapter.replaceData(results);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
