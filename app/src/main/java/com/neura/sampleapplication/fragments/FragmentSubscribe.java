package com.neura.sampleapplication.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.neura.sampleapplication.R;
import com.neura.sdk.object.AppSubscription;
import com.neura.sdk.service.GetSubscriptionsCallbacks;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.util.NeuraUtil;
import com.neura.standalonesdk.util.SDKUtils;

import java.util.List;

/**
 * Created by Hadas on 9/16/2015.
 */
public class FragmentSubscribe extends FragmentPermissions {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscribe, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgress = (ProgressBar) view.findViewById(R.id.progress);
    }

    @Override
    public boolean shouldShowExtendedToggle() {
        return true;
    }

    @Override
    protected void setSubscriptionsState() {
        getMainActivity().getClient().getSubscriptions(new GetSubscriptionsCallbacks() {

            @Override
            public void onSuccess(List<AppSubscription> subscriptions) {
                for (AppSubscription subscription : subscriptions) {
                    updateSubscribe(subscription.getEventName());
                }

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Bundle resultData, int errorCode) {
                Toast.makeText(getMainActivity(),
                        "Error: Failed to receive event subscription list. Error code: "
                                + NeuraUtil.errorCodeToString(errorCode),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Subscribe / unsubscribe to receive events from Neura.
     * <br>Neura will continue notifying this event, until removeSubscription will be called.
     * <p>
     * The second parameter for subscribeToEvent/removeSubscription methods is the eventIdentifier
     * The event identifier will be part of the data sent to your webhook when Neura identifies
     * an event for this subscription. It's there for you to attach a user identification, for example.
     * There are 2 ways you can receive events from Neura :
     * ----------------------------------------------------
     * 1. webhook : define a webhood when creating the application on our devsite.
     * 2. push : Neura will send you a push on event, to your declared receiver on the manifest.
     * In this case, make sure to call {@link com.neura.standalonesdk.service.NeuraApiClient#registerPushServerApiKey(Activity, String)}
     * after {@link com.neura.standalonesdk.service.NeuraApiClient#authenticate(int, com.neura.sdk.object.AuthenticationRequest)} is completed,
     * (called onActivityResult in {@link FragmentMain})
     * make manifest adjustments and register a receiver : {@link com.neura.sampleapplication.NeuraEventsBroadcastReceiver}
     * 3 very important notes :
     * ------------------------
     * - eventIdentifier should be unique for each event, for example,
     * subscribeToEvent(userStartedDriving, iden1, mSubscribeAddRemoveRequest) - success
     * subscribeToEvent(userFinishedDriving, iden1, mSubscribeAddRemoveRequest) - fail since event
     * userStartedDriving already used iden1.
     * - when subscribing and un subscribing from event X, the identifier should be the same.
     * - In the permission adapter, the services Neura provides are disabled on
     * {@link #sortByActivePermissions()}, since there's no need to create subscription
     * to this, because this is not an event - this is a service, and the service is granted
     * at once, without the need to subscribe.
     *
     * @param eventName event to notify
     * @param isEnabled true - subscribing to event, false - un subscribing from event.
     */
    private void subscribeToFromEvent(String eventName, boolean isEnabled) {
        loadProgress(true);

        String eventIdentifier = "YourEventIdentifier_" + eventName;
        /**
         * In this sample, events will be send from Neura directly to the sample, and will be
         * received on {@link com.neura.sampleapplication.NeuraEventsBroadcastReceiver}
         * the 3rd parameter for {@link com.neura.standalonesdk.service.NeuraApiClient#subscribeToEvent(String, String, boolean, SubscriptionRequestCallbacks)}
         * and {@link com.neura.standalonesdk.service.NeuraApiClient#removeSubscription(String, String, boolean, SubscriptionRequestCallbacks)}
         * indicates whether to use push (true) or webhook(false) in order to receive an event.
         */
        if (isEnabled)
            getMainActivity().getClient().subscribeToEvent(eventName, eventIdentifier, true, mSubscribeAddRemoveRequest);
        else
            getMainActivity().getClient().removeSubscription(eventName, eventIdentifier, true, mSubscribeAddRemoveRequest);
    }

    private SubscriptionRequestCallbacks mSubscribeAddRemoveRequest = new SubscriptionRequestCallbacks() {
        @Override
        public void onSuccess(String eventName, Bundle resultData, String identifier) {
            loadProgress(false);

            if (!isPermissionEnabled(eventName))
                return;
            //Please notice that since the missing data flow is asynchronous,
            //the return will be onActivityResult on this class.
            if (getMainActivity() != null)
                getMainActivity().getClient().getMissingDataForEvent(getMainActivity(),
                        getString(R.string.app_uid), eventName,
                        NEURA_SDK_REQUEST_CODE);
        }

        @Override
        public void onFailure(String eventName, Bundle resultData, int errorCode) {
            boolean isEnabled = revertSubscribe(eventName);
            Toast.makeText(getMainActivity(),
                    "Error: Failed to " + (isEnabled ? "subscribe to event " : "un subscribe from event ")
                            + eventName + ". Error code: " + NeuraUtil.errorCodeToString(errorCode),
                    Toast.LENGTH_SHORT).show();
            loadProgress(false);
        }
    };

    /**
     * Reverting user's subscription / un subscription since we've received an error from the server.
     *
     * @param eventName
     * @return true if we've tried subscribing to an event, false otherwise.
     */
    private boolean revertSubscribe(String eventName) {
        for (PermissionStatus permission : mPermissions) {
            if (eventName != null && eventName.equals(permission.getPermission().getName())) {
                permission.setEnabled(!permission.isEnabled());
                mAdapter.notifyDataSetChanged();
                return !permission.isEnabled();
            }
        }
        return false;
    }

    private boolean isPermissionEnabled(String eventName) {
        for (PermissionStatus permission : mPermissions) {
            if (eventName != null && eventName.equals(permission.getPermission().getName())) {
                return permission.isEnabled();
            }
        }
        return false;
    }

    private void updateSubscribe(String eventName) {
        for (PermissionStatus permission : mPermissions) {
            if (eventName != null && eventName.equals(permission.getPermission().getName())) {
                permission.setEnabled(true);
                break;
            }
        }
    }

    @Override
    public void onSwitchChange(String eventName, boolean isEnabled) {
        subscribeToFromEvent(eventName, isEnabled);
    }

    /**
     * When returning from getMissingDataForEven, we'll return if the process of missing data
     * failed or finished successfully, and upon successful return - we'll provide  the label that
     * was handed.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEURA_SDK_REQUEST_CODE) {
            String label = SDKUtils.extractSelectedLabel(data);
            Log.i(getClass().getSimpleName(), "Missing data flow finished " +
                    (resultCode == FragmentActivity.RESULT_OK ?
                            "successfully, user selected " : "unsuccessfully, user didn't select ") + label);
        }
    }
}
