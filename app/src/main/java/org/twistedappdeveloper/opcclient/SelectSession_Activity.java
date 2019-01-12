package org.twistedappdeveloper.opcclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import OpcUtils.ManagerOPC;
import tool.ui.SessionsAdapter;

public class SelectSession_Activity extends AppCompatActivity {

    ListView listSessions;
    ManagerOPC managerOPC;
    SessionsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_session);

        managerOPC = ManagerOPC.getIstance();

        listSessions = findViewById(R.id.ListSessions);

        adapter = new SessionsAdapter(this,R.layout.list_session, managerOPC.getSessions());

        listSessions.setAdapter(adapter);
        listSessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent= new Intent(SelectSession_Activity.this,SessionActivity.class);
                intent.putExtra("SessionPosition",position);
                intent.putExtra("Url", managerOPC.getSessions().get(position).getUrl());
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
