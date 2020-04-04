package org.twistedappdeveloper.opcclient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.DataChangeFilter;
import org.opcfoundation.ua.core.DataChangeTrigger;
import org.opcfoundation.ua.core.DeadbandType;
import org.opcfoundation.ua.core.MonitoredItemCreateRequest;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.MonitoringParameters;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;

import OpcUtils.ConnectionThread.ThreadCreateMonitoredItem;
import OpcUtils.ConnectionThread.ThreadDeleteSubscription;
import OpcUtils.ManagerOPC;
import OpcUtils.SubscriptionElement;
import tool.ui.MonitoredItemAdapter;

public class SubscriptionActivity extends AppCompatActivity {

    public static int idchandle = 0;
    ManagerOPC managerOPC;
    SubscriptionElement subscriptionElement;
    TextView txtInfoSubscription, txtSubscriptionParameters;
    Button btnNewMonitoredItem;
    ListView listMonitoredItem;
    ProgressDialog progressDialog;
    MonitoredItemAdapter adapter;

    int namespace;
    int nodeid;
    String nodeid_String;
    double sampling_interval;
    UnsignedInteger queue_size;
    boolean discard_oldest;
    double deadband;
    int session_position;
    int sub_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        managerOPC = ManagerOPC.getIstance();
        session_position = getIntent().getIntExtra("sessionPosition", -1);
        sub_position = getIntent().getIntExtra("subPosition", -1);
        if (session_position < 0 || sub_position < 0) {
            Toast.makeText(SubscriptionActivity.this, R.string.ErroreLetturaSottoscrizione, Toast.LENGTH_LONG).show();
            finish();
        }

        subscriptionElement = managerOPC.getSessions().get(session_position).getSubscriptions().get(sub_position);

        txtInfoSubscription = findViewById(R.id.txtInfoSottoscrizione);
        txtSubscriptionParameters = findViewById(R.id.txtParametriSottoscrizione);
        btnNewMonitoredItem = findViewById(R.id.btnCreateMonitoredItem);
        listMonitoredItem = findViewById(R.id.listMonitoredItem);

        adapter = new MonitoredItemAdapter(SubscriptionActivity.this, R.id.listMonitoredItem, subscriptionElement.getMonitoreditems());
        listMonitoredItem.setAdapter(adapter);

        String testo = "Endpoint\n" + subscriptionElement.getSession().getSession().getEndpoint().getEndpointUrl() +
                "\nSessionID\n" + subscriptionElement.getSession().getSession().getName() +
                "\nSubscriptionID\n" + subscriptionElement.getSubscription().getSubscriptionId();
        txtInfoSubscription.setText(testo);

        testo = getString(R.string.ParametriRestituiti) +
                "\nPublishInterval: " + subscriptionElement.getSubscription().getRevisedPublishingInterval() +
                "\nLifetimeCount: " + subscriptionElement.getSubscription().getRevisedLifetimeCount() +
                "\nMaxKeepAliveCount: " + subscriptionElement.getSubscription().getRevisedMaxKeepAliveCount();
        txtSubscriptionParameters.setText(testo);

        btnNewMonitoredItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MonitoredItemCreateRequest[] monitoredItems = new MonitoredItemCreateRequest[1];
                monitoredItems[0] = new MonitoredItemCreateRequest();


                final Dialog dialog = new Dialog(SubscriptionActivity.this, R.style.AppAlert);
                dialog.setContentView(R.layout.dialog_createmonitoreditem);
                final Spinner timestamps = dialog.findViewById(R.id.spinnerTimestamp);
                ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(SubscriptionActivity.this, R.array.timestamps, android.R.layout.simple_spinner_dropdown_item);
                timestamps.setAdapter(spinneradapter);

                final EditText edtMonitoredNamespace = dialog.findViewById(R.id.edtMonitoredNamespace);
                final EditText edtMonitoredNodeID = dialog.findViewById(R.id.edtMonitoredNodeID);
                final EditText edtMonitoredSampling = dialog.findViewById(R.id.edtSamplingInterval);
                final EditText edtMonitoredQueue = dialog.findViewById(R.id.edtQueueSize);
                final CheckBox checkDiscardOldest = dialog.findViewById(R.id.checkDiscardOldest);
                final RadioGroup rdgroupfiltro = dialog.findViewById(R.id.rdgroupDeadband);
                final EditText edtValDeadband = dialog.findViewById(R.id.edtValDeadband);

                edtMonitoredSampling.setHint("Ex: " + ManagerOPC.Default_SamplingInterval);
                edtMonitoredQueue.setHint("Ex: " + ManagerOPC.Default_QueueSize + "");
                edtValDeadband.setHint("Ex: " + ManagerOPC.Default_AbsoluteDeadBand + "");


                Button btnOkMonitored = dialog.findViewById(R.id.btnOkMonitoredItem);

                btnOkMonitored.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TimestampsToReturn timestamp = null;
                        switch (timestamps.getSelectedItem().toString()) {
                            case "Server":
                                timestamp = TimestampsToReturn.Server;
                                break;
                            case "Source":
                                timestamp = TimestampsToReturn.Source;
                                break;
                            case "Both":
                                timestamp = TimestampsToReturn.Both;
                                break;
                            case "Neither":
                                timestamp = TimestampsToReturn.Neither;
                                break;
                        }


                        if (edtMonitoredNamespace.getText().toString().length() != 0 && edtMonitoredNodeID.getText().toString().length() != 0 && edtMonitoredSampling.getText().toString().length() != 0
                                && edtMonitoredQueue.getText().toString().length() != 0 && edtValDeadband.getText().toString().length() != 0) {
                            namespace = Integer.parseInt(edtMonitoredNamespace.getText().toString());
                            try {
                                nodeid = Integer.parseInt(edtMonitoredNodeID.getText().toString());
                                nodeid_String = null;
                            } catch (Exception e) {
                                nodeid_String = edtMonitoredNodeID.getText().toString();
                            }
                            sampling_interval = Double.parseDouble(edtMonitoredSampling.getText().toString());
                            queue_size = new UnsignedInteger(edtMonitoredQueue.getText().toString());
                            discard_oldest = checkDiscardOldest.isChecked();
                            DeadbandType deadbandType = null;
                            switch (rdgroupfiltro.getCheckedRadioButtonId()) {
                                case R.id.rdAbsolute:
                                    deadbandType = DeadbandType.Absolute;
                                    break;
                                case R.id.rdPercentage:
                                    deadbandType = DeadbandType.Percent;
                                    break;
                            }
                            deadband = Double.parseDouble(edtValDeadband.getText().toString());

                            DataChangeFilter filter = new DataChangeFilter();
                            filter.setTrigger(DataChangeTrigger.StatusValue);
                            filter.setDeadbandType(new UnsignedInteger(deadbandType.getValue()));
                            filter.setDeadbandValue(deadband);
                            ExtensionObject fil = new ExtensionObject(filter);

                            MonitoringParameters reqParams = new MonitoringParameters();
                            reqParams.setClientHandle(new UnsignedInteger(idchandle++));
                            reqParams.setSamplingInterval(sampling_interval);
                            reqParams.setQueueSize(queue_size);
                            reqParams.setDiscardOldest(discard_oldest);
                            reqParams.setFilter(fil);
                            monitoredItems[0].setRequestedParameters(reqParams);
                            monitoredItems[0].setMonitoringMode(MonitoringMode.Reporting);
                            NodeId nodeId;
                            if (nodeid_String == null)
                                nodeId = new NodeId(namespace, nodeid);
                            else
                                nodeId = new NodeId(namespace, nodeid_String);
                            monitoredItems[0].setItemToMonitor(new ReadValueId(nodeId, Attributes.Value, null, null));

                            final CreateMonitoredItemsRequest mi = new CreateMonitoredItemsRequest();
                            mi.setSubscriptionId(subscriptionElement.getSubscription().getSubscriptionId());
                            mi.setTimestampsToReturn(timestamp);
                            mi.setItemsToCreate(monitoredItems);

                            ThreadCreateMonitoredItem t = new ThreadCreateMonitoredItem(subscriptionElement, mi);
                            progressDialog = ProgressDialog.show(SubscriptionActivity.this, getString(R.string.TentativoDiConnessione), getString(R.string.CreazioneMonItemInCorso), true);
                            @SuppressLint("HandlerLeak") Handler handler_monitoreditem = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    progressDialog.dismiss();
                                    if (msg.what == -1) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.ErroreSconosciuto) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                    } else if (msg.what == -2) {
                                        Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                    } else if (msg.what == -3) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.ErroreToast) + msg.obj.toString(), Toast.LENGTH_LONG).show();
                                    } else {
                                        adapter.notifyDataSetChanged();
                                        listMonitoredItem.setSelection(adapter.getCount() - 1);
                                    }
                                }
                            };
                            t.start(handler_monitoreditem);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(SubscriptionActivity.this, R.string.InserisciValoriValidi, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                dialog.show();
            }

        });

        listMonitoredItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SubscriptionActivity.this, MonitoredItemActivity.class);
                intent.putExtra("sessionPosition", session_position);
                intent.putExtra("subPosition", sub_position);
                intent.putExtra("monPosition", position);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subscriptionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SubscriptionActivity.this);
        builder.setTitle(R.string.ChiudiSottoscrizione);
        builder.setMessage(R.string.ChiudendoSottoscrizione);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ThreadDeleteSubscription t = new ThreadDeleteSubscription(subscriptionElement.getSession(), subscriptionElement.getSubscription().getSubscriptionId());
                        progressDialog = ProgressDialog.show(SubscriptionActivity.this, getString(R.string.TentativoDiConnessione), getString(R.string.CencellazioneSottoscrizioneInCorso), true);
                        @SuppressLint("HandlerLeak") Handler handler_delete_subscription = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                progressDialog.dismiss();
                                if (msg.what == -1) {
                                    Toast.makeText(SubscriptionActivity.this, getString(R.string.EliminationeSottoscrizioneFallita) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                } else if (msg.what == -2) {
                                    Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                } else {
                                    managerOPC.getSessions().get(session_position).getSubscriptions().remove(sub_position);
                                    finish();
                                }
                            }
                        };
                        t.start(handler_delete_subscription);
                        dialogInterface.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialogInterface.dismiss();
                        break;
                }
            }
        };
        builder.setPositiveButton(android.R.string.yes, listener);
        builder.setNegativeButton(android.R.string.no, listener);
        builder.setCancelable(false);
        Dialog g = builder.create();
        g.show();

        return super.onOptionsItemSelected(item);
    }
}
