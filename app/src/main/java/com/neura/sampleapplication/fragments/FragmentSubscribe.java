package com.neura.sampleapplication.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.data.PickerCallback;
import com.neura.sampleapplication.NeuraEventsService;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sampleapplication.adapters.PermissionsAdapterDisplay;
import com.neura.sdk.callbacks.GetPermissionsRequestCallbacks;
import com.neura.sdk.object.AppSubscription;
import com.neura.sdk.object.Permission;
import com.neura.sdk.service.GetSubscriptionsCallbacks;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.util.NeuraUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FragmentSubscribe extends BaseFragment implements PermissionsAdapterDisplay.ISwitchChangeListener {

    private ListView mList;
    private ArrayList<PermissionStatus> mPermissions = new ArrayList<>();
    private PermissionsAdapterDisplay mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscribe, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mList = (ListView) view.findViewById(R.id.permissions_list);
        mAdapter = new PermissionsAdapterDisplay(getMainActivity(), R.layout.permission_item,
                mPermissions, this);
        mList.setAdapter(mAdapter);

        mProgress = (ProgressBar) view.findViewById(R.id.progress);

        fetchPermissions();
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
     * after {@link com.neura.standalonesdk.service.NeuraApiClient#authenticate(com.neura.sdk.object.AuthenticationRequest, AuthenticateCallback)}  is completed.
     * In this application, this is done on {@link FragmentMain#authenticateWithNeura()}.
     * make manifest adjustments and register a receiver : {@link NeuraEventsService}
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
         * received on {@link NeuraEventsService}
         * the 3rd parameter for {@link com.neura.standalonesdk.service.NeuraApiClient#subscribeToEvent(String, String, boolean, SubscriptionRequestCallbacks)}
         * and {@link com.neura.standalonesdk.service.NeuraApiClient#removeSubscription(String, String, boolean, SubscriptionRequestCallbacks)}
         * indicates whether to use push (true) or webhook(false) in order to receive an event.
         */
        if (isEnabled)
            NeuraManager.getInstance().getClient().subscribeToEvent(eventName, eventIdentifier, true, mSubscribeAddRemoveRequest);
        else
            NeuraManager.getInstance().getClient().removeSubscription(eventName, eventIdentifier, true, mSubscribeAddRemoveRequest);
    }

    private SubscriptionRequestCallbacks mSubscribeAddRemoveRequest = new SubscriptionRequestCallbacks() {
        @Override
        public void onSuccess(final String eventName, Bundle resultData, String identifier) {
            loadProgress(false);

            if (!isPermissionEnabled(eventName))
                return;
            NeuraManager.getInstance().getClient().getMissingDataForEvent(
                    eventName, new PickerCallback() {
                        @Override
                        public void onResult(boolean success) {
                            Log.i(getClass().getSimpleName(), (success ?
                                    "Successfully added data for event : " :
                                    "User canceled adding data for event : ") + eventName);
                        }
                    });
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

    protected void fetchPermissions() {
        loadProgress(true);
        NeuraManager.getInstance().getClient().getAppPermissions(new GetPermissionsRequestCallbacks() {
            @Override
            public void onSuccess(final List<Permission> permissions) throws RemoteException {
                if (getActivity() == null)
                    return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadProgress(false);
                        for (int i = 0; i < permissions.size(); i++)
                            mPermissions.add(new PermissionStatus(permissions.get(i)));
                        sortByActivePermissions();
                        setSubscriptionsState();
                    }
                });
            }

            @Override
            public void onFailure(Bundle resultData, int errorCode) throws RemoteException {
                loadProgress(false);
            }

            @Override
            public IBinder asBinder() {
                return null;
            }
        });
    }

    private void setSubscriptionsState() {
        NeuraManager.getInstance().getClient().getSubscriptions(new GetSubscriptionsCallbacks() {

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
     * Sorting permissions array by active events (meaning - all the permissions that neura supports
     * at the moment will be on top, and the non active events will be on bottom of the list.
     * FYI : when {@link Permission#isActive()} = false, there's no need to create subscription
     * to this event, since this is not an event - this is a service, and the subscription is
     * granted at once, without the need to subscribe.
     */
    private void sortByActivePermissions() {
        ArrayList<Permission> permissions = new ArrayList<>();
        if (mPermissions != null && mPermissions.isEmpty()) {
            return;
        }
        for (int i = 0; i < mPermissions.size(); i++) {
            permissions.add(mPermissions.get(i).getPermission());
        }
        permissions = NeuraManager.getInstance().getClient().getPermissionStatus(permissions);
        for (int i = 0; i < permissions.size(); i++) {
            for (int j = 0; j < mPermissions.size(); j++) {
                if (mPermissions.get(j).getPermission().getName().equals(permissions.get(i))) {
                    mPermissions.get(j).getPermission().setActive(permissions.get(i).isActive());
                    break;
                }
            }
        }
        Collections.sort(mPermissions, mComparator);
        mAdapter.notifyDataSetChanged();
    }

    private Comparator<PermissionStatus> mComparator = new Comparator<PermissionStatus>() {
        @Override
        public int compare(PermissionStatus lhs, PermissionStatus rhs) {
            Permission left = lhs.getPermission();
            Permission right = rhs.getPermission();
            if ((left.isActive() && right.isActive()) || (!left.isActive() && !right.isActive())) {
                return left.getDisplayName().compareTo(right.getDisplayName());
            } else if (left.isActive() && !right.isActive())
                return -1;
            else if (!left.isActive() && right.isActive())
                return 1;
            return 0;
        }
    };

    @Override
    public void onSwitchChange(String eventName, boolean isEnabled) {
        subscribeToFromEvent(eventName, isEnabled);
    }

    public class PermissionStatus implements Comparable {
        Permission mPermission;
        boolean mEnabled;

        public PermissionStatus(Permission permission) {
            mPermission = permission;
            mEnabled = false;
        }

        public Permission getPermission() {
            return mPermission;
        }

        public boolean isEnabled() {
            return mEnabled;
        }

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        @Override
        public int compareTo(Object another) {
            return 0;
        }
    }

}
