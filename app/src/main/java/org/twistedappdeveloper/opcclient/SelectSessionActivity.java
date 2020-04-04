package org.twistedappdeveloper.opcclient;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import OpcUtils.ManagerOPC;
import tool.ui.SessionsAdapter;

public class SelectSessionActivity extends AppCompatActivity {

    ListView listSessions;
    ManagerOPC managerOPC;
    SessionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_session);

        managerOPC = ManagerOPC.getIstance();

        listSessions = findViewById(R.id.ListSessions);

        adapter = new SessionsAdapter(this, R.layout.list_session, managerOPC.getSessions());

        listSessions.setAdapter(adapter);
        listSessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SelectSessionActivity.this, SessionActivity.class);
                intent.putExtra("sessionPosition", position);
                intent.putExtra("url", managerOPC.getSessions().get(position).getUrl());
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
