package org.twistedappdeveloper.opcclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import OpcUtils.ManagerOPC;
import OpcUtils.SessionElement;
import tool.ui.MonitoringAdapter;

public class MonitoringActivity extends AppCompatActivity {

    TextView txtSessionMonitoring;
    ListView listMonitoring;
    int session_position;
    ManagerOPC managerOPC;
    SessionElement sessionElement;
    MonitoringAdapter adapter;
    static boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        listMonitoring = findViewById(R.id.listMonitoraggio);
        txtSessionMonitoring = findViewById(R.id.txtSessionMonitoring);

        managerOPC = ManagerOPC.getIstance();

        session_position = getIntent().getIntExtra("sessionPosition", -1);
        if (session_position < 0) {
            Toast.makeText(MonitoringActivity.this, R.string.ErroreLetturaSessione, Toast.LENGTH_LONG).show();
            finish();
        }

        sessionElement = managerOPC.getSessions().get(session_position);

        txtSessionMonitoring.setText(getString(R.string.sessionid) + "\n" + sessionElement.getSession().getSession().getName());

        adapter = new MonitoringAdapter(MonitoringActivity.this, R.layout.list_monitoring, sessionElement.getSubscriptions());
        listMonitoring.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setRunning(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getRunning()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    try {
                        Thread.sleep(MonitoredItemActivity.refreshrate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        running = false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.monitoreditemmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                setRunning(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (getRunning()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            try {
                                Thread.sleep(MonitoredItemActivity.refreshrate);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                Toast.makeText(MonitoringActivity.this, R.string.AggiornamentoAttivo, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_pause:
                Toast.makeText(MonitoringActivity.this, R.string.AggiornamentoInPausa, Toast.LENGTH_SHORT).show();
                setRunning(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static synchronized void setRunning(boolean running) {
        MonitoredItemActivity.running = running;
    }

    public static synchronized boolean getRunning() {
        return MonitoredItemActivity.running;
    }

}
