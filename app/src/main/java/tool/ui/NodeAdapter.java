package tool.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.twistedappdeveloper.opcclient.R;

import java.util.List;

import OpcUtils.BrowseDataStamp;

public class NodeAdapter extends ArrayAdapter<BrowseDataStamp> {

    public NodeAdapter(Context context, int resource, List<BrowseDataStamp> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_node, null);
        TextView node = convertView.findViewById(R.id.txtNodo);
        TextView nodedata = convertView.findViewById(R.id.txtNodoData);
        BrowseDataStamp obj = getItem(position);
        node.setText(obj.name);
        String text = "Namespace: " + obj.namespace +
                "\nNodeIndex: " + obj.nodeindex +
                "\nClass: " + obj.nodeclass;
        nodedata.setText(text);
        return convertView;
    }
}
