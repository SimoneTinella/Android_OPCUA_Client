package tool.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.twistedappdeveloper.opcclient.R;

import java.util.List;

import OpcUtils.SubscriptionElement;

public class MonitoringAdapter extends ArrayAdapter<SubscriptionElement> {

    SubMonitoraggioAdapter adapter;

    public MonitoringAdapter(Context context, int resource, List<SubscriptionElement> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_monitoring, null);
        SubscriptionElement obj = getItem(position);

        TextView txtSubID = convertView.findViewById(R.id.txtSubID);
        ListView listSub = convertView.findViewById(R.id.listSubMonitored);
        TextView txtpubinterval = convertView.findViewById(R.id.txtSubPubInterval);

        ViewGroup.LayoutParams l = listSub.getLayoutParams();
        l.height = (int) getContext().getResources().getDimension(R.dimen.dim) * obj.getMonitoreditems().size();
        listSub.setLayoutParams(l);

        txtSubID.setText("Subscription ID: " + obj.getSubscription().getSubscriptionId());
        txtpubinterval.setText("Publishing interval: " + obj.getSubscription().getRevisedPublishingInterval().toString() + " ms");
        adapter = new SubMonitoraggioAdapter(getContext(), R.layout.list_submonitoring, obj.getMonitoreditems());

        listSub.setAdapter(adapter);

        return convertView;
    }
}
