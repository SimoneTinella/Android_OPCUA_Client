package org.twistedappdeveloper.opcclient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.DataChangeFilter;
import org.opcfoundation.ua.core.DataChangeTrigger;
import org.opcfoundation.ua.core.DeadbandType;
import org.opcfoundation.ua.core.MonitoredItemCreateRequest;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.MonitoringParameters;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.WriteResponse;

import java.util.ArrayList;

import OpcUtils.BrowseDataStamp;
import OpcUtils.ConnectionThread.ThreadCreateMonitoredItem;
import OpcUtils.ConnectionThread.ThreadRead;
import OpcUtils.ConnectionThread.ThreadWrite;
import OpcUtils.ManagerOPC;
import OpcUtils.SessionElement;
import tool.ui.NodeAdapter;

public class BrowseFragment extends Fragment {
    Bundle bundle;
    ListView listNode;
    NodeAdapter adapter;
    ArrayList<String> nodes, namespace, nodeindex, classi;
    int sessionPosition;
    SessionElement sessionElement;
    ArrayList<BrowseDataStamp> dati;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        bundle = getArguments();
        listNode = view.findViewById(R.id.listNode);

        nodes = bundle.getStringArrayList("nodes");
        namespace = bundle.getStringArrayList("namespace");
        nodeindex = bundle.getStringArrayList("nodeindex");
        classi = bundle.getStringArrayList("nodeclass");
        sessionPosition = bundle.getInt("sessionPosition");

        sessionElement = ManagerOPC.getIstance().getSessions().get(sessionPosition);

        dati = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            BrowseDataStamp tmp = new BrowseDataStamp(nodes.get(i), namespace.get(i), nodeindex.get(i), classi.get(i));
            dati.add(tmp);
        }

        adapter = new NodeAdapter(getContext(), R.layout.list_node, dati);
        listNode.setAdapter(adapter);

        listNode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((BrowseActivity) getActivity()).browseToPosition(position);
            }
        });

        listNode.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                String[] colors = {"Read", "Write", "Browse", "Monitor this node"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.menu_azioni));
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                readNode(position);
                                break;
                            case 1:
                                writeNode(position);
                                break;
                            case 2:
                                ((BrowseActivity) getActivity()).browseToPosition(position);
                                break;
                            case 3:
                                createMonItem(position);
                                break;
                            default:
                                dialog.cancel();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });

        return view;
    }

    private void createMonItem(int position){

        final MonitoredItemCreateRequest[] monitoredItems = new MonitoredItemCreateRequest[1];
        monitoredItems[0] = new MonitoredItemCreateRequest();

        final Dialog dialog = new Dialog(getContext(), R.style.AppAlert);
        dialog.setContentView(R.layout.dialog_createmonitoreditem);
        final Spinner timestamps = dialog.findViewById(R.id.spinnerTimestamp);
        ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(getContext(), R.array.timestamps, android.R.layout.simple_spinner_dropdown_item);
        timestamps.setAdapter(spinneradapter);

        final EditText edtMonitoredNamespace = dialog.findViewById(R.id.edtMonitoredNamespace);
        final EditText edtMonitoredNodeID = dialog.findViewById(R.id.edtMonitoredNodeID);
        final EditText edtMonitoredSampling = dialog.findViewById(R.id.edtSamplingInterval);
        final EditText edtMonitoredQueue = dialog.findViewById(R.id.edtQueueSize);
        final CheckBox checkDiscardOldest = dialog.findViewById(R.id.checkDiscardOldest);
        final RadioGroup rdgroupfiltro = dialog.findViewById(R.id.rdgroupDeadband);
        final EditText edtValDeadband = dialog.findViewById(R.id.edtValDeadband);

        edtMonitoredNamespace.setText(dati.get(position).namespace);
        edtMonitoredNodeID.setText(dati.get(position).nodeindex);
        edtMonitoredNamespace.setEnabled(false);
        edtMonitoredNodeID.setEnabled(false);
        edtMonitoredNamespace.setFocusable(false);
        edtMonitoredNodeID.setFocusable(false);

        edtMonitoredSampling.setHint("Ex: " + ManagerOPC.Default_SamplingInterval);
        edtMonitoredQueue.setHint("Ex: " + ManagerOPC.Default_QueueSize + "");
        edtValDeadband.setHint("Ex: " + ManagerOPC.Default_AbsoluteDeadBand + "");

        Button btnOkMonitored = dialog.findViewById(R.id.btnOkMonitoredItem);

        btnOkMonitored.setOnClickListener(new View.OnClickListener() {

            int namespace, nodeid;
            String nodeid_String;
            double sampling_interval,deadband;
            UnsignedInteger queue_size;
            boolean discard_oldest;

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
                    reqParams.setClientHandle(new UnsignedInteger(SubscriptionActivity.idchandle++));
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
                    mi.setSubscriptionId(sessionElement.getSubscriptions().get(0).getSubscription().getSubscriptionId()); //FIXME gestire creazione e ricerca sottoscrizioni
                    mi.setTimestampsToReturn(timestamp);
                    mi.setItemsToCreate(monitoredItems);

                    ThreadCreateMonitoredItem t = new ThreadCreateMonitoredItem(sessionElement.getSubscriptions().get(0), mi); //FIXME gestire creazione e ricerca sottoscrizioni
                    final ProgressDialog progressDialog = ProgressDialog.show( getContext(), getString(R.string.TentativoDiConnessione), getString(R.string.CreazioneMonItemInCorso), true);
                    @SuppressLint("HandlerLeak") Handler handler_monitoreditem = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            progressDialog.dismiss();
                            if (msg.what == -1) {
                                Toast.makeText(getContext(), getString(R.string.ErroreSconosciuto) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                            } else if (msg.what == -2) {
                                Toast.makeText(getContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                            } else if (msg.what == -3) {
                                Toast.makeText(getContext(), getString(R.string.ErroreToast) + msg.obj.toString(), Toast.LENGTH_LONG).show();
                            } else {
                                //TODO gestire creazione con successo
                                Toast.makeText(getContext(), "Creato con successo", Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    t.start(handler_monitoreditem);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), R.string.InserisciValoriValidi, Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();
    }

    private void readNode(int position) {
        final Dialog dialogRead = new Dialog(getContext(), R.style.AppAlert);
        dialogRead.setContentView(R.layout.dialog_inserdataread);
        final EditText edtnamespace = dialogRead.findViewById(R.id.edtNamespace);
        final EditText edtnodeid = dialogRead.findViewById(R.id.edtNodeID);
        final RadioGroup rdGroupTimestamp = dialogRead.findViewById(R.id.rdgrouptimestamp);
        final EditText edtMaxAge = dialogRead.findViewById(R.id.edtMaxAge);
        Button btnokread = dialogRead.findViewById(R.id.btnOkRead);

        edtnamespace.setText(dati.get(position).namespace);
        edtnodeid.setText(dati.get(position).nodeindex);
        edtnamespace.setFocusable(false);
        edtnodeid.setFocusable(false);
        edtnamespace.setEnabled(false);
        edtnodeid.setEnabled(false);

        btnokread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int namespace, nodeid;
                String nodeid_string;
                double maxAge;
                if (edtnamespace.getText().toString().length() == 0 || edtnodeid.getText().toString().length() == 0 || edtMaxAge.getText().toString().length() == 0) {
                    Toast.makeText(getContext(), R.string.InserisciValoriValidi, Toast.LENGTH_SHORT).show();
                } else {

                    namespace = Integer.parseInt(edtnamespace.getText().toString());

                    if (TextUtils.isDigitsOnly(edtnodeid.getText())) {
                        nodeid = Integer.parseInt(edtnodeid.getText().toString());
                        nodeid_string = null;
                    } else {
                        nodeid = -1;
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

                    final ProgressDialog progressDialog = ProgressDialog.show(getContext(), getString(R.string.TentativoDiConnessione), getString(R.string.LetturaInCorso), true);
                    @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == -1) {
                                Toast.makeText(getContext(), getString(R.string.LetturaFallita) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                            } else if (msg.what == -2) {
                                Toast.makeText(getContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                            } else {
                                ReadResponse res = (ReadResponse) msg.obj;
                                String text = "Value: " + res.getResults()[0].getValue().getValue() +
                                        "\nStatus: " + res.getResults()[0].getStatusCode() +
                                        "\nServerTimestamp: " + res.getResults()[0].getServerTimestamp() +
                                        "\nSourceTimestamp: " + res.getResults()[0].getSourceTimestamp();
                                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                alertDialog.setTitle(getString(R.string.result));
                                alertDialog.setMessage(text);
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                            progressDialog.dismiss();
                        }
                    };
                    t.start(handler);
                    dialogRead.dismiss();
                }
            }
        });
        dialogRead.show();
    }

    private void writeNode(int position) {
        final Dialog dialog_write = new Dialog(getContext(), R.style.AppAlert);
        dialog_write.setContentView(R.layout.dialog_inserdatawrite);
        final EditText edtnamespace_write = dialog_write.findViewById(R.id.edtNamespaceWrite);
        final EditText edtnodeid_write = dialog_write.findViewById(R.id.edtNodeIDWrite);
        final EditText edtvalue_write = dialog_write.findViewById(R.id.edtValueWrite);
        Button btnokwrite = dialog_write.findViewById(R.id.btnOkWrite);

        edtnamespace_write.setText(dati.get(position).namespace);
        edtnodeid_write.setText(dati.get(position).nodeindex);
        edtnamespace_write.setFocusable(false);
        edtnodeid_write.setFocusable(false);
        edtnamespace_write.setEnabled(false);
        edtnodeid_write.setEnabled(false);

        final Spinner spinnerType = dialog_write.findViewById(R.id.spinnertype);
        final ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(getContext(), R.array.WriteType, android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spinneradapter);

        btnokwrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int namespace, nodeid;
                String nodeid_string;
                Variant value_write= null;

                if (edtnamespace_write.getText().toString().length() == 0 || edtnodeid_write.getText().toString().length() == 0 || edtvalue_write.getText().toString().length() == 0) {
                    Toast.makeText(getContext(), R.string.InserisciValoriValidi, Toast.LENGTH_SHORT).show();
                } else {
                    namespace = Integer.parseInt(edtnamespace_write.getText().toString());

                    if (TextUtils.isDigitsOnly(edtnodeid_write.getText())) {
                        nodeid = Integer.parseInt(edtnodeid_write.getText().toString());
                        nodeid_string = null;
                    } else {
                        nodeid = -1;
                        nodeid_string = edtnodeid_write.getText().toString();
                    }
                    String value = edtvalue_write.getText().toString();
                    switch (spinnerType.getSelectedItem().toString()) {
                        case "Double":
                            if (TextUtils.isDigitsOnly(value)) {
                                value_write = new Variant(Double.parseDouble(value));
                            } else {
                                Toast.makeText(getContext(), R.string.InserisciValoriValidi, Toast.LENGTH_LONG).show();
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
                                Toast.makeText(getContext(), R.string.InserisciValoriValidi, Toast.LENGTH_LONG).show();
                            break;
                    }
                    ThreadWrite t;

                    final ProgressDialog progressDialog = ProgressDialog.show(getContext(), getString(R.string.TentativoDiConnessione), getString(R.string.WriteInCorso), true);
                    if (nodeid_string == null)
                        t = new ThreadWrite(sessionElement.getSession(), namespace, nodeid, Attributes.Value, value_write);
                    else
                        t = new ThreadWrite(sessionElement.getSession(), namespace, nodeid_string, Attributes.Value, value_write);
                    @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            progressDialog.dismiss();
                            if (msg.what == -1) {
                                Toast.makeText(getContext(), getString(R.string.WriteFallita) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                            } else if (msg.what == -2) {
                                Toast.makeText(getContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                            } else {
                                WriteResponse res = (WriteResponse) msg.obj;
                                String response = res.getResults()[0].getDescription();
                                if (response.length() > 0)
                                    response = "\n" + res.getResults()[0].getDescription();
                                Toast.makeText(getContext(), getString(R.string.ValoriInviati) + response, Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    t.start(handler);

                }
                dialog_write.dismiss();
            }
        });
        dialog_write.show();
    }

}
