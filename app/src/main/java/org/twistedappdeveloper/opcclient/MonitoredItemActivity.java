package org.twistedappdeveloper.opcclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.SubscriptionAcknowledgement;

import OpcUtils.ManagerOPC;
import OpcUtils.MonitoredItemElement;
import OpcUtils.SessionElement;
import tool.ui.ReadMonitoredAdapter;

public class MonitoredItemActivity extends AppCompatActivity {
    public final static int refreshrate = 100;

    TextView txtData;
    SessionElement sessionElement;
    int sub_pos;
    int mon_pos;
    SubscriptionAcknowledgement subAck;
    UnsignedInteger LastSeqNumber;
    ListView listMonRead;
    ReadMonitoredAdapter adapter;
    static boolean running = false;
    TextView txtBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitored_item);
        txtData = findViewById(R.id.txtMonitoredDati);
        listMonRead = findViewById(R.id.listMonitoredRead);
        txtBuffer = findViewById(R.id.txtBufferLetture);
        LastSeqNumber = new UnsignedInteger(0);

        txtBuffer.setText(getString(R.string.Ultime) + MonitoredItemElement.buffersize + getString(R.string.letture));
        int session_pos = getIntent().getIntExtra("sessionPosition", -1);
        sub_pos = getIntent().getIntExtra("subPosition", -1);
        mon_pos = getIntent().getIntExtra("monPosition", -1);

        sessionElement = ManagerOPC.getIstance().getSessions().get(session_pos);

        adapter = new ReadMonitoredAdapter(MonitoredItemActivity.this, R.layout.list_monitoredreadings, sessionElement.getSubscriptions().get(sub_pos).getMonitoreditems().get(mon_pos).getReadings());
        listMonRead.setAdapter(adapter);

        CreateMonitoredItemsResponse mi = sessionElement.getSubscriptions().get(sub_pos).getMonitoreditems().get(mon_pos).getMonitoreditem();
        String text = "Monitored Item ID: " + mi.getResults()[0].getMonitoredItemId() +
                "\nSampling Interval: " + mi.getResults()[0].getRevisedSamplingInterval() +
                "\nQueue Size: " + mi.getResults()[0].getRevisedQueueSize();
        txtData.setText(text);

        subAck = new SubscriptionAcknowledgement();
        subAck.setSubscriptionId(new UnsignedInteger(sessionElement.getSubscriptions().get(sub_pos).getSubscription().getSubscriptionId()));


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
                        Thread.sleep(refreshrate);
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
        setRunning(false);
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
                                Thread.sleep(refreshrate);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                Toast.makeText(MonitoredItemActivity.this, R.string.AggiornamentoAttivo, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_pause:
                Toast.makeText(MonitoredItemActivity.this, R.string.AggiornamentoInPausa, Toast.LENGTH_SHORT).show();
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
