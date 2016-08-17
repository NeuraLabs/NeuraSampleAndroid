package com.neura.sampleapplication.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.neura.resources.device.Capability;
import com.neura.resources.device.DevicesRequestCallback;
import com.neura.resources.device.DevicesResponseData;
import com.neura.sampleapplication.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Hadas on 4/20/2016.
 */
public class FragmentDeviceOperations extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_operations, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setGetDevices();
        setGetCapabilities();
        setHasDeviceWithCapability();
        setGeneralAddDevice();
        setDeviceCapabilities();
        setSpecificDevice();
    }

    private void setGetDevices() {
        getView().findViewById(R.id.get_devices_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().getClient().getKnownDevices(new DevicesRequestCallback() {
                    @Override
                    public void onSuccess(DevicesResponseData data) {
                        String message = "Successfully received " + (data.getDevices() != null ?
                                data.getDevices().size() : 0) + " devices";
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        Log.i(getClass().getSimpleName(), message);
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        String message = "Failed to receive devices list";
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        Log.i(getClass().getSimpleName(), message);
                    }
                });
            }
        });
    }

    private void setGetCapabilities() {
        getView().findViewById(R.id.get_capabilities_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Capability> data = getMainActivity().getClient().getKnownCapabilities();
                String message = "Successfully received " + (data != null ? data.size() : 0) + " capabilities";
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                Log.i(getClass().getSimpleName(), message);
            }
        });
    }

    private void setHasDeviceWithCapability() {
        getView().findViewById(R.id.has_device_with_capability_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String capability = ((EditText) getView().findViewById
                                (R.id.has_device_with_capability_edit)).getText().toString();
                        String message = "User " + (getMainActivity().getClient().hasDeviceWithCapability(capability) ?
                                "has " : "doesn't have ") + "device with capability " + capability;
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        Log.i(getClass().getSimpleName(), message);
                    }
                });
    }

    /**
     * When returning from NEURA_SDK_REQUEST_CODE, we'll be returning the device that was added.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEURA_SDK_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK)
                Log.i(getClass().getSimpleName(), "Device was added successfully");
            else {
                Log.e(getClass().getSimpleName(), "Failed to add a device");
            }
        }
    }

    private void setGeneralAddDevice() {
        getView().findViewById(R.id.add_device_general).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().getClient().addDevice(getMainActivity(), getString(R.string.app_uid),
                        NEURA_SDK_REQUEST_CODE);
            }
        });
    }

    private void setDeviceCapabilities() {
        getView().findViewById(R.id.add_device_capabilities).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ((EditText) getView().findViewById
                        (R.id.add_device_capabilities_capabilities_list)).getText().toString();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(getActivity(), "Can't add device, capabilities are blank",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                text = text.replace(" ", "");
                ArrayList<String> capabilities = new ArrayList<>(Arrays.asList(text.split(",")));
                getMainActivity().getClient().addDevice(getMainActivity(), getString(R.string.app_uid),
                        NEURA_SDK_REQUEST_CODE, capabilities);
            }
        });
    }

    private void setSpecificDevice() {
        getView().findViewById(R.id.add_device_specific).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String text = ((EditText) getView().findViewById(R.id.add_device_specific_id)).getText().toString();
                    if (TextUtils.isEmpty(text))
                        Toast.makeText(getActivity(), "Please set a device name", Toast.LENGTH_SHORT).show();
                    else
                        getMainActivity().getClient().addDevice(getMainActivity(), getString(R.string.app_uid),
                                NEURA_SDK_REQUEST_CODE, text);
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Device id isn't a number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
