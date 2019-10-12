package tool.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.twistedappdeveloper.opcclient.R;

import java.util.List;

import OpcUtils.SubscriptionElement;

public class SubscriptionAdapter extends ArrayAdapter<SubscriptionElement> {

    public SubscriptionAdapter(Context context, int resource, List<SubscriptionElement> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_readings, null);
        TextView endpoint = convertView.findViewById(R.id.txtReaging);
        SubscriptionElement obj = getItem(position);
        String text = "Subscription ID: " + obj.getSubscription().getSubscriptionId() +
                "\nSessionID:" + obj.getSession().getSession().getName() +
                "\nPublishInterval: " + obj.getSubscription().getRevisedPublishingInterval() +
                "\nLifetimeCount: " + obj.getSubscription().getRevisedLifetimeCount() +
                "\nMaxKeepAliveCount: " + obj.getSubscription().getRevisedMaxKeepAliveCount();
        endpoint.setText(text);
        return convertView;
    }
}
