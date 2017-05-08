package com.neura.sampleapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.neura.resources.data.PickerCallback;
import com.neura.resources.place.AddPlaceCallback;
import com.neura.resources.place.PlaceNode;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;

import java.util.ArrayList;

/**
 * Created by hadas on 23/01/2017.
 */

public class AddLocationFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_location, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAddPlace(view);
        setAddMissingData(view);
    }

    private void setAddPlace(View view) {
        final EditText label = (EditText) view.findViewById(R.id.place_label);
        final EditText latitude = (EditText) view.findViewById(R.id.place_latitude);
        final EditText longitude = (EditText) view.findViewById(R.id.place_longitude);
        final EditText address = (EditText) view.findViewById(R.id.place_address);
        final EditText name = (EditText) view.findViewById(R.id.place_name);
        Button addPlace = (Button) view.findViewById(R.id.place_button);

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitudeValue = TextUtils.isEmpty(latitude.getText().toString()) ? 0 :
                        Double.valueOf(latitude.getText().toString());
                double longitudeValue = TextUtils.isEmpty(longitude.getText().toString()) ? 0 :
                        Double.valueOf(longitude.getText().toString());

                closeKeyboard(v);

                NeuraManager.getInstance().getClient().addPlace(label.getText().toString(),
                        latitudeValue, longitudeValue, address.getText().toString(),
                        name.getText().toString(), new AddPlaceCallback() {
                            @Override
                            public void onSuccess(PlaceNode place) {
                                Log.e(getClass().getSimpleName(), "Successfully add place : "
                                        + place.toJson().toString());
                            }

                            @Override
                            public void onFailure() {
                                Log.e(getClass().getSimpleName(), "Failed to add place");
                            }
                        });
            }
        });
    }

    private void setAddMissingData(View view) {
        ListView locationEventsList = (ListView) view.findViewById(R.id.location_based_events_list);
        final ArrayList<String> locations = NeuraManager.getInstance().getClient().getLocationBasedEvents();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, locations);
        locationEventsList.setAdapter(adapter);

        /**
         * For events related to locations, if you're subscribing to the event userArrivedHome (fe),
         * and your user is new on Neura, we don't know his/her home yet. The preferable way is to
         * wait few days for Neura to detect it, BUT, if you need the home NOW, you can call
         * {@link com.neura.standalonesdk.service.NeuraApiClient#getMissingDataForEvent(String, PickerCallback)}
         * which will open a place picker for your user to select his/her home.
         * Method is disabled in this application.
         */
        locationEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
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

    private void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
