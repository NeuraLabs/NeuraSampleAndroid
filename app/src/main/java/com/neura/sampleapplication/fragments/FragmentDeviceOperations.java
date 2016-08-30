package com.neura.sampleapplication.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.neura.resources.data.PickerCallback;
import com.neura.resources.device.Capability;
import com.neura.resources.device.DevicesRequestCallback;
import com.neura.resources.device.DevicesResponseData;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;

import java.util.ArrayList;
import java.util.Arrays;

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
                NeuraManager.getInstance().getClient().getKnownDevices(new DevicesRequestCallback() {
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
                ArrayList<Capability> data = NeuraManager.getInstance().getClient().getKnownCapabilities();
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
                        String message = "User " + (NeuraManager.getInstance().getClient().hasDeviceWithCapability(capability) ?
                                "has " : "doesn't have ") + "device with capability " + capability;
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        Log.i(getClass().getSimpleName(), message);
                    }
                });
    }

    private void setGeneralAddDevice() {
        getView().findViewById(R.id.add_device_general).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NeuraManager.getInstance().getClient().addDevice(mPickerCallback);
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
                NeuraManager.getInstance().getClient().addDevice(capabilities, mPickerCallback);
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
                        NeuraManager.getInstance().getClient().addDevice(text, mPickerCallback);
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Device id isn't a number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private PickerCallback mPickerCallback = new PickerCallback() {
        @Override
        public void onResult(boolean success) {
            Log.i(getClass().getSimpleName(), success ?
                    "Add device completed successfully" : "Add device wasn't completed");
        }
    };

}
