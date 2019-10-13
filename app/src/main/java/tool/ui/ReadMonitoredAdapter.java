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

public class ReadMonitoredAdapter extends ArrayAdapter<MonitoredItemNotification> {

    public ReadMonitoredAdapter(Context context, int resource, List<MonitoredItemNotification> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_monitoredreadings, null);
        TextView monitored = convertView.findViewById(R.id.txtLetturaMonitored);
        MonitoredItemNotification obj = getItem(position);
        String text = "Value:" + obj.getValue().getValue() +
                "\nServer Timestamp:" + obj.getValue().getServerTimestamp() +
                "\nSource Timestamp:" + obj.getValue().getSourceTimestamp() +
                "\nStatus:" + obj.getValue().getStatusCode();
        monitored.setText(text);
        return convertView;
    }
}
