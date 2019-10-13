package tool.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.opcfoundation.ua.core.MonitoredItemNotification;
import org.twistedappdeveloper.opcclient.R;

import java.util.List;
import java.util.NoSuchElementException;

import OpcUtils.MonitoredItemElement;

public class SubMonitoraggioAdapter extends ArrayAdapter<MonitoredItemElement> {

    public SubMonitoraggioAdapter(Context context, int resource, List<MonitoredItemElement> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_submonitoring, null);
        MonitoredItemElement obj = getItem(position);

        TextView txtMonID = convertView.findViewById(R.id.txtMonID);
        TextView submonval = convertView.findViewById(R.id.txtsubmonval);
        TextView subsource = convertView.findViewById(R.id.txtsubsource);
        TextView subserv = convertView.findViewById(R.id.txtsubserv);
        TextView substatus = convertView.findViewById(R.id.txtsubstato);

        txtMonID.setText("Item ID: " + obj.getMonitoreditem().getResults()[0].getMonitoredItemId());

        try {
            String tmp = "none";
            MonitoredItemNotification notification = obj.getReadings().getFirst();
            submonval.setText("Value: " + notification.getValue().getValue());
            if (notification.getValue().getSourceTimestamp() != null) {
                tmp = notification.getValue().getSourceTimestamp().toString();
                tmp = tmp.substring(0, tmp.length() - 10);
            }
            subsource.setText("Source Timestamp: " + tmp);
            tmp = "none";
            if (notification.getValue().getServerTimestamp() != null) {
                tmp = notification.getValue().getServerTimestamp().toString();
                tmp = tmp.substring(0, tmp.length() - 10);
            }
            subserv.setText("Server Timestamp: " + tmp);
            substatus.setText("Status: " + notification.getValue().getStatusCode());
        } catch (NoSuchElementException e) {
            submonval.setText("Value: ");
            subsource.setText("Source Timestamp: ");
            subserv.setText("Server Timestamp: ");
            substatus.setText("Status: ");
        }
        return convertView;
    }
}
