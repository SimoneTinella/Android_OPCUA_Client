package tool.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.opcfoundation.ua.core.ReadResponse;
import org.twistedappdeveloper.opcclient.R;

import java.util.List;

public class ReadAdapter extends ArrayAdapter<ReadResponse> {

    public ReadAdapter(Context context, int resource, List<ReadResponse> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_readings, null);
        TextView endpoint = convertView.findViewById(R.id.txtReaging);
        ReadResponse obj = getItem(position);
        String text = "Value: " + obj.getResults()[0].getValue().getValue() +
                "\nStatus: " + obj.getResults()[0].getStatusCode() +
                "\nServerTimestamp: " + obj.getResults()[0].getServerTimestamp() +
                "\nSourceTimestamp: " + obj.getResults()[0].getSourceTimestamp();

        endpoint.setText(text);
        return convertView;
    }
}
