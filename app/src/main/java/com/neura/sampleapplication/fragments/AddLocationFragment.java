package com.neura.sampleapplication.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.neura.resources.data.PickerCallback;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;

import java.util.ArrayList;

/**
 * Created by hadas on 23/01/2017.
 */

public class AddLocationFragment extends BaseFragment {

    private ListView mLocationEventsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_location, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLocationEventsList = (ListView) view.findViewById(R.id.location_based_events_list);
        final ArrayList<String> locations = NeuraManager.getInstance().getClient().getLocationBasedEvents();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, locations);
        mLocationEventsList.setAdapter(adapter);

        mLocationEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                /**
                 * For events related to locations, if you're subscribing to the event userArrivedHome (fe),
                 * and your user is new on Neura, we don't know his/her home yet. The preferable way is to
                 * wait few days for Neura to detect it, BUT, if you need the home NOW, you can call
                 * {@link com.neura.standalonesdk.service.NeuraApiClient#getMissingDataForEvent(String, PickerCallback)}
                 * which will open a place picker for your user to select his/her home.
                 * Method is disabled in this application.
                 */
                NeuraManager.getInstance().getClient().getMissingDataForEvent(locations.get(position),
                        new PickerCallback() {
                            @Override
                            public void onResult(boolean success) {
                                Log.i(getClass().getSimpleName(), (success ?
                                        "Successfully added a place" : "Failed to add a place") +
                                        " for the event : " + locations.get(position));
                            }
                        });
            }
        });
    }
}
