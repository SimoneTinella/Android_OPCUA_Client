package tool.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.twistedappdeveloper.opcclient.R;

import java.util.List;

import OpcUtils.SessionElement;

public class SessionsAdapter extends ArrayAdapter<SessionElement> {

    public SessionsAdapter(Context context, int resource, List<SessionElement> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_session, null);
        TextView endpoint = convertView.findViewById(R.id.txtSession);
        SessionElement obj = getItem(position);
        String text = "Url: " + obj.getUrl() + "\nSessionID: " + obj.getSession().getSession().getName() + "\nEndpoint: " + obj.getSession().getSession().getEndpoint().getEndpointUrl();
        endpoint.setText(text);
        return convertView;
    }
}
