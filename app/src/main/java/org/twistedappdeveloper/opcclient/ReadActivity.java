package org.twistedappdeveloper.opcclient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.TimestampsToReturn;

import java.util.ArrayList;
import java.util.List;

import OpcUtils.ConnectionThread.ThreadRead;
import OpcUtils.ManagerOPC;
import OpcUtils.SessionElement;
import tool.ui.ReadAdapter;

public class ReadActivity extends AppCompatActivity {
    ListView listRead;
    List<ReadResponse> listReadings;
    ReadAdapter adapter;
    Button btnNewRead;
    ProgressDialog progressDialog;
    int namespace;
    int nodeid;
    String nodeid_string;
    double maxAge;
    int session_position;
    ManagerOPC managerOPC;
    SessionElement sessionElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        session_position = getIntent().getIntExtra("sessionPosition", -1);
        if (session_position < 0) {
            Toast.makeText(ReadActivity.this, R.string.Errore, Toast.LENGTH_SHORT).show();
            finish();
        }

        listReadings = new ArrayList<>();

        listRead = findViewById(R.id.list_read);
        btnNewRead = findViewById(R.id.btnNewRead);

        adapter = new ReadAdapter(ReadActivity.this, R.layout.list_readings, listReadings);
        listRead.setAdapter(adapter);

        managerOPC = ManagerOPC.getIstance();
        sessionElement = managerOPC.getSessions().get(session_position);

        btnNewRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(ReadActivity.this, R.style.AppAlert);
                dialog.setContentView(R.layout.dialog_inserdataread);
                final EditText edtnamespace = dialog.findViewById(R.id.edtNamespace);
                final EditText edtnodeid = dialog.findViewById(R.id.edtNodeID);
                final RadioGroup rdGroupTimestamp = dialog.findViewById(R.id.rdgrouptimestamp);
                final EditText edtMaxAge = dialog.findViewById(R.id.edtMaxAge);
                Button btnokread = dialog.findViewById(R.id.btnOkRead);

                btnokread.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edtnamespace.getText().toString().length() == 0 || edtnodeid.getText().toString().length() == 0 || edtMaxAge.getText().toString().length() == 0) {
                            Toast.makeText(ReadActivity.this, R.string.InserisciValoriValidi, Toast.LENGTH_SHORT).show();
                        } else {

                            namespace = Integer.parseInt(edtnamespace.getText().toString());

                            if (TextUtils.isDigitsOnly(edtnodeid.getText())) {
                                nodeid = Integer.parseInt(edtnodeid.getText().toString());
                                nodeid_string = null;
                            } else {
                                nodeid_string = edtnodeid.getText().toString();
                            }
                            maxAge = Double.parseDouble(edtMaxAge.getText().toString());
                            TimestampsToReturn timestamps = TimestampsToReturn.Both;
                            switch (rdGroupTimestamp.getCheckedRadioButtonId()) {
                                case R.id.rdServer:
                                    timestamps = TimestampsToReturn.Server;
                                    break;
                                case R.id.rdSource:
                                    timestamps = TimestampsToReturn.Source;
                                    break;
                                case R.id.rdBoth:
                                    timestamps = TimestampsToReturn.Both;
                                    break;
                                case R.id.rdNeither:
                                    timestamps = TimestampsToReturn.Neither;
                                    break;
                            }

                            ThreadRead t;
                            if (nodeid_string == null)
                                t = new ThreadRead(sessionElement.getSession(), maxAge, timestamps, namespace, nodeid, Attributes.Value);
                            else
                                t = new ThreadRead(sessionElement.getSession(), maxAge, timestamps, namespace, nodeid_string, Attributes.Value);

                            progressDialog = ProgressDialog.show(ReadActivity.this, getString(R.string.TentativoDiConnessione), getString(R.string.LetturaInCorso), true);
                            @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == -1) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.LetturaFallita) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                    } else if (msg.what == -2) {
                                        Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                    } else {
                                        ReadResponse res = (ReadResponse) msg.obj;
                                        listReadings.add(res);
                                        adapter.notifyDataSetChanged();
                                        listRead.setSelection(adapter.getCount() - 1);
                                    }
                                    progressDialog.dismiss();
                                }
                            };
                            t.start(handler);
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

    }
}
