package org.twistedappdeveloper.opcclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import OpcUtils.ManagerOPC;
import tool.ui.SubscriptionAdapter;

public class ListSubscriptionActivity extends AppCompatActivity {

    ListView listSubscriptions;
    SubscriptionAdapter adapter;
    ManagerOPC manager;
    int session_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_subscription);

        manager = ManagerOPC.getIstance();
        session_position = getIntent().getIntExtra("sessionPosition", -1);
        if (session_position < 0) {
            Toast.makeText(ListSubscriptionActivity.this, "Error", Toast.LENGTH_LONG).show();
            finish();
        }

        listSubscriptions = findViewById(R.id.listSubscriptions);
        adapter = new SubscriptionAdapter(ListSubscriptionActivity.this, R.layout.list_subscriptions, manager.getSessions().get(session_position).getSubscriptions());
        listSubscriptions.setAdapter(adapter);
        listSubscriptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListSubscriptionActivity.this, SubscriptionActivity.class);
                intent.putExtra("subPosition", position);
                intent.putExtra("sessionPosition", session_position);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.notifyDataSetChanged();
    }

}
