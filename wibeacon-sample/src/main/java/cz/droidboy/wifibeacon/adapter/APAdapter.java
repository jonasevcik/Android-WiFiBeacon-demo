package cz.droidboy.wifibeacon.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cz.droidboy.wibeacon.util.ProximityUtils;
import cz.droidboy.wifibeacon.R;

/**
 * @author Jonas Sevcik
 */
public class APAdapter extends BaseAdapter {

    private Context context;
    private List<ScanResult> data;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public APAdapter(Context context) {
        this.context = context;
        this.data = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ap_row, parent, false);
            holder = new ViewHolder();
            holder.ssid = (TextView) convertView.findViewById(R.id.ssid);
            holder.rssi = (TextView) convertView.findViewById(R.id.rss);
            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScanResult scan = data.get(position);
        holder.ssid.setText(scan.SSID);
        holder.rssi.setText(ProximityUtils.getProximity(scan.level, scan.frequency).name());
        return convertView;
    }

    public void replaceData(List<ScanResult> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView ssid;
        TextView rssi;
        TextView distance;
    }

}
