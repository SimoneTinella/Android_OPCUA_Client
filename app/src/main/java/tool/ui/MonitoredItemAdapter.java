package tool.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.twistedappdeveloper.opcclient.R;

import java.util.List;

import OpcUtils.MonitoredItemElement;

public class MonitoredItemAdapter extends ArrayAdapter<MonitoredItemElement> {

    public MonitoredItemAdapter(Context context, int resource, List<MonitoredItemElement> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_monitored, null);
        TextView monitored = convertView.findViewById(R.id.txtMonitored);
        MonitoredItemElement obj = getItem(position);
        String text = "Monitored Item ID: " + obj.getMonitoreditem().getResults()[0].getMonitoredItemId() +
                "\nSampling Interval: " + obj.getMonitoreditem().getResults()[0].getRevisedSamplingInterval() +
                "\nQueue Size: " + obj.getMonitoreditem().getResults()[0].getRevisedQueueSize();
        monitored.setText(text);
        return convertView;
    }
}
