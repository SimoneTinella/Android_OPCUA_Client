package org.twistedappdeveloper.opcclient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;
import org.opcfoundation.ua.core.WriteResponse;

import OpcUtils.ConnectionThread.ThreadCreateSubscription;
import OpcUtils.ConnectionThread.ThreadWrite;
import OpcUtils.ManagerOPC;
import OpcUtils.SessionElement;

public class SessionActivity extends AppCompatActivity {

    TextView txtSessionId, txtSessionEndpoint, txtUrl;
    ManagerOPC managerOPC;
    SessionElement sessionElement;
    String Url;
    int session_position;
    Button btnRead, btnBrowse, btnSubscribe, btnWrite, btnMonitoring;
    ProgressDialog progressDialog;
    int namespace;
    int nodeid;
    String nodeid_string;
    Variant value_write;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        session_position = getIntent().getIntExtra("sessionPosition", -1);
        Url = getIntent().getStringExtra("url");

        if (session_position < 0) {
            Toast.makeText(SessionActivity.this, R.string.Errore, Toast.LENGTH_SHORT).show();
            finish();
        }

        managerOPC = ManagerOPC.getIstance();

        txtSessionEndpoint = findViewById(R.id.txtSessionEndpoint);
        txtSessionId = findViewById(R.id.txtSessionId);
        txtUrl = findViewById(R.id.txtUrl);
        btnRead = findViewById(R.id.btnRead);
        btnBrowse = findViewById(R.id.btnBrowse);
        btnSubscribe = findViewById(R.id.btnSubscribe);
        btnWrite = findViewById(R.id.btnWrite);
        btnMonitoring = findViewById(R.id.btnMonitoring);

        sessionElement = managerOPC.getSessions().get(session_position);

        txtSessionEndpoint.setText("Endpoint\n" + sessionElement.getSession().getSession().getEndpoint().getEndpointUrl());
        txtSessionId.setText("SessionID\n" + sessionElement.getSession().getSession().getName());
        txtUrl.setText("Url\n" + Url);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.btnRead:
                        Intent intent_read = new Intent(SessionActivity.this, ReadActivity.class);
                        intent_read.putExtra("sessionPosition", session_position);
                        startActivity(intent_read);
                        break;

                    case R.id.btnBrowse:
                        Intent intent = new Intent(SessionActivity.this, BrowseActivity.class);
                        intent.putExtra("sessionPosition", session_position);
                        startActivity(intent);
                        break;

                    case R.id.btnSubscribe:
                        final Dialog dialog_subscription = new Dialog(SessionActivity.this, R.style.AppAlert);
                        dialog_subscription.setContentView(R.layout.dialog_insertdatasubscription);

                        final EditText edtRequestedPublishInteval = dialog_subscription.findViewById(R.id.edtRequestedPublishingInterval);
                        final EditText edtRequestedMaxKeepAliveCount = dialog_subscription.findViewById(R.id.edtRequestedMaxKeepAliveCount);
                        final EditText edtRequestedLifetimeCount = dialog_subscription.findViewById(R.id.edtRequestedLifetimeCount);
                        final EditText edtMaxNotificationPerPublish = dialog_subscription.findViewById(R.id.edtMaxNotificationPerPublish);
                        final EditText edtPriority = dialog_subscription.findViewById(R.id.edtPriotity);
                        final CheckBox checkPublishingEnable = dialog_subscription.findViewById(R.id.checkPublishingEnable);
                        Button btnOkSubscription = dialog_subscription.findViewById(R.id.btnOkSubscription);

                        edtRequestedLifetimeCount.setHint("Ex: " + ManagerOPC.Default_RequestedLifetimeCount);
                        edtMaxNotificationPerPublish.setHint("Ex: " + ManagerOPC.Default_MaxNotificationsPerPublish);
                        edtRequestedPublishInteval.setHint("Ex: " + ManagerOPC.Default_RequestedPublishingInterval);
                        edtRequestedMaxKeepAliveCount.setHint("Ex: " + ManagerOPC.Default_RequestedMaxKeepAliveCount);
                        edtPriority.setHint("Ex: " + ManagerOPC.Default_Priority);

                        btnOkSubscription.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Double requestedPublishInterval;
                                UnsignedInteger requestedLifetimeCount;
                                UnsignedInteger requestedMaxKeepAliveCount;
                                UnsignedInteger maxNotificationPerPublish;
                                UnsignedByte priority;
                                boolean publishingEnabled;

                                if (edtRequestedLifetimeCount.getText().toString().length() == 0 ||
                                        edtMaxNotificationPerPublish.getText().toString().length() == 0 ||
                                        edtRequestedPublishInteval.getText().toString().length() == 0 ||
                                        edtRequestedMaxKeepAliveCount.getText().toString().length() == 0 ||
                                        edtPriority.getText().toString().length() == 0) {
                                    Toast.makeText(SessionActivity.this, R.string.InserisciValoriValidi, Toast.LENGTH_LONG).show();
                                } else {
                                    requestedLifetimeCount = new UnsignedInteger(edtRequestedLifetimeCount.getText().toString());
                                    maxNotificationPerPublish = new UnsignedInteger(edtMaxNotificationPerPublish.getText().toString());
                                    requestedPublishInterval = Double.parseDouble(edtRequestedPublishInteval.getText().toString());
                                    requestedMaxKeepAliveCount = new UnsignedInteger(edtRequestedMaxKeepAliveCount.getText().toString());
                                    priority = new UnsignedByte(edtPriority.getText().toString());
                                    publishingEnabled = checkPublishingEnable.isChecked();
                                    if (requestedLifetimeCount.intValue() >= 3 * requestedMaxKeepAliveCount.intValue()) {
                                        CreateSubscriptionRequest req = new CreateSubscriptionRequest(null, requestedPublishInterval, requestedLifetimeCount, requestedMaxKeepAliveCount, maxNotificationPerPublish, publishingEnabled, priority);
                                        ThreadCreateSubscription t = new ThreadCreateSubscription(sessionElement, req);
                                        progressDialog = ProgressDialog.show(SessionActivity.this, getString(R.string.TentativoDiConnessione), getString(R.string.CreazioneSottoscrizione), true);
                                        @SuppressLint("HandlerLeak") Handler handler_subscription = new Handler() {
                                            @Override
                                            public void handleMessage(Message msg) {
                                                progressDialog.dismiss();
                                                if (msg.what == -1) {
                                                    Toast.makeText(SessionActivity.this, getString(R.string.SottoscrizioneFallita) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                                } else if (msg.what == -2) {
                                                    Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                                } else {
                                                    int position = (int) msg.obj;
                                                    Intent intent = new Intent(SessionActivity.this, SubscriptionActivity.class);
                                                    intent.putExtra("subPosition", position);
                                                    intent.putExtra("sessionPosition", session_position);
                                                    startActivity(intent);
                                                }
                                            }
                                        };
                                        dialog_subscription.dismiss();
                                        t.start(handler_subscription);
                                    } else {
                                        Toast.makeText(SessionActivity.this, getString(R.string.DeviRispettareVincolo) + "\nLifetimeCount>3*MaxKeepAliveCount", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });

                        dialog_subscription.show();
                        break;
                    case R.id.btnWrite:
                        final Dialog dialog_write = new Dialog(SessionActivity.this, R.style.AppAlert);
                        dialog_write.setContentView(R.layout.dialog_inserdatawrite);
                        final EditText edtnamespace_write = dialog_write.findViewById(R.id.edtNamespaceWrite);
                        final EditText edtnodeid_write = dialog_write.findViewById(R.id.edtNodeIDWrite);
                        final EditText edtvalue_write = dialog_write.findViewById(R.id.edtValueWrite);
                        Button btnokwrite = dialog_write.findViewById(R.id.btnOkWrite);
                        final Spinner spinnerType = dialog_write.findViewById(R.id.spinnertype);
                        final ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(SessionActivity.this, R.array.WriteType, android.R.layout.simple_spinner_dropdown_item);
                        spinnerType.setAdapter(spinneradapter);

                        btnokwrite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (edtnamespace_write.getText().toString().length() == 0 || edtnodeid_write.getText().toString().length() == 0 || edtvalue_write.getText().toString().length() == 0) {
                                    Toast.makeText(SessionActivity.this, R.string.InserisciValoriValidi, Toast.LENGTH_SHORT).show();
                                } else {
                                    namespace = Integer.parseInt(edtnamespace_write.getText().toString());

                                    if (TextUtils.isDigitsOnly(edtnodeid_write.getText())) {
                                        nodeid = Integer.parseInt(edtnodeid_write.getText().toString());
                                        nodeid_string = null;
                                    } else {
                                        nodeid_string = edtnodeid_write.getText().toString();
                                    }
                                    String value = edtvalue_write.getText().toString();
                                    switch (spinnerType.getSelectedItem().toString()) {
                                        case "Double":
                                            if (TextUtils.isDigitsOnly(value)) {
                                                value_write = new Variant(Double.parseDouble(value));
                                            } else {
                                                Toast.makeText(SessionActivity.this, R.string.InserisciValoriValidi, Toast.LENGTH_LONG).show();
                                            }
                                            break;
                                        case "String":
                                            value_write = new Variant(value);
                                            break;
                                        case "Boolean":
                                            if (value.toLowerCase().compareTo("true") == 0)
                                                value_write = new Variant(true);
                                            else if (value.toLowerCase().compareTo("false") == 0)
                                                value_write = new Variant(false);
                                            else
                                                Toast.makeText(SessionActivity.this, R.string.InserisciValoriValidi, Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                    ThreadWrite t;
                                    progressDialog = ProgressDialog.show(SessionActivity.this, getString(R.string.TentativoDiConnessione), getString(R.string.WriteInCorso), true);
                                    if (nodeid_string == null)
                                        t = new ThreadWrite(sessionElement.getSession(), namespace, nodeid, Attributes.Value, value_write);
                                    else
                                        t = new ThreadWrite(sessionElement.getSession(), namespace, nodeid_string, Attributes.Value, value_write);
                                    @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            progressDialog.dismiss();
                                            if (msg.what == -1) {
                                                Toast.makeText(getApplicationContext(), getString(R.string.WriteFallita) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                            } else if (msg.what == -2) {
                                                Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                            } else {
                                                WriteResponse res = (WriteResponse) msg.obj;
                                                String response = res.getResults()[0].getDescription();
                                                if (response.length() > 0)
                                                    response = "\n" + res.getResults()[0].getDescription();
                                                Toast.makeText(getApplicationContext(), getString(R.string.ValoriInviati) + response, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    };
                                    t.start(handler);

                                }
                                dialog_write.dismiss();
                            }
                        });
                        dialog_write.show();
                        break;
                    case R.id.btnMonitoring:
                        Intent intent_monitoring = new Intent(SessionActivity.this, MonitoringActivity.class);
                        intent_monitoring.putExtra("sessionPosition", session_position);
                        startActivity(intent_monitoring);
                        break;
                }
            }
        };

        btnRead.setOnClickListener(listener);
        btnSubscribe.setOnClickListener(listener);
        btnBrowse.setOnClickListener(listener);
        btnWrite.setOnClickListener(listener);
        btnMonitoring.setOnClickListener(listener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sessionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_terminate:
                AlertDialog.Builder builder = new AlertDialog.Builder(SessionActivity.this);
                builder.setTitle(R.string.ChiudiSessione);
                builder.setMessage(R.string.EliminandoLaSessione);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                managerOPC.getSessions().remove(session_position);
                                sessionElement.getSession().closeAsync();
                                dialogInterface.dismiss();
                                finish();
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
                break;
            case R.id.action_subscriptions:
                Intent intent = new Intent(SessionActivity.this, ListSubscriptionActivity.class);
                intent.putExtra("sessionPosition", session_position);
                startActivity(intent);
                break;
        }


        return super.onOptionsItemSelected(item);
    }
}
