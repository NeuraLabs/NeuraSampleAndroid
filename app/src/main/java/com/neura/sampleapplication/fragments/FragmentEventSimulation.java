package com.neura.sampleapplication.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sdk.service.SimulateEventCallBack;

import java.util.List;

/**
 * Created by Jack on 11/28/2017.
 */

public class FragmentEventSimulation extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_simulation, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final List<String> events = NeuraManager.getEvents();

        ListView eventListView = (ListView) getView().findViewById(R.id.event_list_view);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), R.layout.textview_for_listview_eventsimulation, events);
        eventListView.setAdapter(adapter);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NeuraManager.getInstance().getClient().simulateAnEvent(events.get(position), new SimulateEventCallBack() {
                            @Override
                            public void onSuccess(String s) {
                                Log.i(getClass().getSimpleName(), "Successfully simulated: " + s);
                            }

                            @Override
                            public void onFailure(String s, String s1) {
                                Log.i(getClass().getSimpleName(), "Not successfully simulated: " + s);
                            }
                        }
                );
            }
        });
    }
}
