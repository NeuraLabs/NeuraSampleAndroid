package com.neura.sampleapplication.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.text.Layout;
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

import java.util.Arrays;
import java.util.List;

/**
 * Created by Jack on 11/28/2017.
 */

public class FragmentEventSimulation extends BaseFragment {
    List<String> events = Arrays.asList("userLeftHome", "userArrivedHome",
            "userStartedWalking", "userStartedRunning",
            "userArrivedToWork", "userLeftWork",
            "userFinishedRunning", "userFinishedWalking",
            "userFinishedDriving", "userStartedDriving");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_simulation, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView eventListView = (ListView) getView().findViewById(R.id.event_list_view);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, events);
        eventListView.setAdapter(adapter);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NeuraManager.getInstance().getClient().simulateAnEvent(events.get(position), new SimulateEventCallBack() {
                            @Override
                            public void onSuccess(String s) {
                                Log.i(getClass().getSimpleName(), "Successfully simulated");
                            }

                            @Override
                            public void onFailure(String s, String s1) {
                                Log.i(getClass().getSimpleName(), "Not successfully simulated");
                            }
                        }
                );
            }
        });
    }
}
