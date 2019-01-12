package org.twistedappdeveloper.opcclient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import OpcUtils.BrowseDataStamp;
import tool.ui.NodeAdapter;
public class BrowseFragment extends Fragment {
    Bundle bundle;
    ListView listNode;
    NodeAdapter adapter;
    ArrayList<String> nodes,namespace,nodeindex,classi;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_browse, container, false);

        bundle=getArguments();
        listNode =  view.findViewById(R.id.listNode);

        nodes = bundle.getStringArrayList("Nodi");
        namespace= bundle.getStringArrayList("namespace");
        nodeindex= bundle.getStringArrayList("nodeindex");
        classi= bundle.getStringArrayList("nodeclass");

        ArrayList<BrowseDataStamp> dati=new ArrayList<>();
        for(int i = 0; i< nodes.size(); i++){
            BrowseDataStamp tmp= new BrowseDataStamp(nodes.get(i),namespace.get(i),nodeindex.get(i),classi.get(i));
            dati.add(tmp);
        }

        adapter= new NodeAdapter(getContext(),R.layout.list_node,dati);
        listNode.setAdapter(adapter);

                listNode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((BrowseActivity)getActivity()).BrowseDaRadice(position);
            }
        });

        return view;
    }

}
