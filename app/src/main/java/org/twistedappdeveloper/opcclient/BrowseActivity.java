package org.twistedappdeveloper.opcclient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.ReferenceDescription;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import OpcUtils.BackListener;
import OpcUtils.ConnectionThread.ThreadBrowse;
import OpcUtils.ManagerOPC;

public class BrowseActivity extends AppCompatActivity {

    int session_position;
    ProgressDialog dialog;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        session_position = getIntent().getIntExtra("sessionPosition", -1);
        if (session_position < 0) {
            Toast.makeText(BrowseActivity.this, R.string.Errore, Toast.LENGTH_LONG).show();
            finish();
        }

        ManagerOPC.getIstance().initStack();


        ArrayList<String> list_basicnodes_name = new ArrayList<>();
        ArrayList<String> list_basicnodes_namespace = new ArrayList<>();
        ArrayList<String> list_basicnodes_nodeindex = new ArrayList<>();
        ArrayList<String> list_basicnodes_class = new ArrayList<>();

        list_basicnodes_name.add("Root");
        list_basicnodes_name.add("Objects");
        list_basicnodes_name.add("Types");
        list_basicnodes_name.add("Views");


        list_basicnodes_namespace.add(Identifiers.RootFolder.getNamespaceIndex() + "");
        list_basicnodes_namespace.add(Identifiers.ObjectsFolder.getNamespaceIndex() + "");
        list_basicnodes_namespace.add(Identifiers.TypesFolder.getNamespaceIndex() + "");
        list_basicnodes_namespace.add(Identifiers.ViewsFolder.getNamespaceIndex() + "");


        list_basicnodes_nodeindex.add(Identifiers.RootFolder.getValue().toString());
        list_basicnodes_nodeindex.add(Identifiers.ObjectsFolder.getValue().toString());
        list_basicnodes_nodeindex.add(Identifiers.TypesFolder.getValue().toString());
        list_basicnodes_nodeindex.add(Identifiers.ViewsFolder.getValue().toString());


        list_basicnodes_class.add("Object");
        list_basicnodes_class.add("Object");
        list_basicnodes_class.add("Object");
        list_basicnodes_class.add("Object");

        Bundle nodi = new Bundle();
        nodi.putStringArrayList("nodes", list_basicnodes_name);
        nodi.putStringArrayList("namespace", list_basicnodes_namespace);
        nodi.putStringArrayList("nodeindex", list_basicnodes_nodeindex);
        nodi.putStringArrayList("nodeclass", list_basicnodes_class);
        nodi.putInt("sessionPosition", session_position);

        BrowseFragment fragmentbase = new BrowseFragment();
        fragmentbase.setArguments(nodi);

        fragmentManager = getSupportFragmentManager();

        fragmentManager.addOnBackStackChangedListener(new BackListener(fragmentManager));

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, fragmentbase);
        fragmentTransaction.commit();

    }

    public void browseToPosition(int position) {
        ThreadBrowse t = new ThreadBrowse(session_position, position);
        dialog = ProgressDialog.show(BrowseActivity.this, getString(R.string.TentativoDiConnessione), getString(R.string.RichiestaBrowse), true);
        @SuppressLint("HandlerLeak") Handler handler_browse = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                dialog.dismiss();
                if (msg.what == -1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.BrowseFallita) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                } else if (msg.what == -2) {
                    Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                } else {
                    BrowseResponse res = (BrowseResponse) msg.obj;
                    ArrayList<String> tmp_name = new ArrayList<>();
                    ArrayList<String> tmp_namespace = new ArrayList<>();
                    ArrayList<String> tmp_nodeindex = new ArrayList<>();
                    ArrayList<String> tmp_class = new ArrayList<>();
                    for (int i = 0; i < res.getResults().length; i++) {
                        if (res.getResults()[i].getReferences() != null) {
                            for (int j = 0; j < res.getResults()[i].getReferences().length; j++) {
                                ReferenceDescription ref = res.getResults()[i].getReferences()[j];
                                tmp_name.add(ref.getDisplayName().getText());
                                tmp_namespace.add(ref.getNodeId().getNamespaceIndex() + "");
                                tmp_nodeindex.add(ref.getNodeId().getValue().toString());
                                tmp_class.add(ref.getNodeClass().toString());
                            }
                        }
                    }
                    if (tmp_name.size() > 0) {
                        Bundle nodi = new Bundle();
                        nodi.putStringArrayList("nodes", tmp_name);
                        nodi.putStringArrayList("namespace", tmp_namespace);
                        nodi.putStringArrayList("nodeindex", tmp_nodeindex);
                        nodi.putStringArrayList("nodeclass", tmp_class);

                        BrowseFragment fragmentbase = new BrowseFragment();
                        fragmentbase.setArguments(nodi);

                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container, fragmentbase);
                        fragmentTransaction.addToBackStack("fragment");
                        fragmentTransaction.commit();
                    } else {
                        Toast.makeText(BrowseActivity.this, R.string.NonCiSonoNodi, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        t.start(handler_browse);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browsemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_close:
                finish();
                break;
            case R.id.action_infoBrowse:
                Dialog dialog = new Dialog(BrowseActivity.this, R.style.AppAlert);
                dialog.setContentView(R.layout.dialog_gif);
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
