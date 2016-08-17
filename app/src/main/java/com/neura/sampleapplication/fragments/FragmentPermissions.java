package com.neura.sampleapplication.fragments;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.neura.sampleapplication.R;
import com.neura.sampleapplication.adapters.PermissionsAdapterDisplay;
import com.neura.sdk.callbacks.GetPermissionsRequestCallbacks;
import com.neura.sdk.object.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hadas on 10/8/2015.
 */
public class FragmentPermissions extends BaseFragment implements PermissionsAdapterDisplay.ISwitchChangeListener {

    protected ListView mList;
    protected ArrayList<PermissionStatus> mPermissions = new ArrayList<>();
    protected PermissionsAdapterDisplay mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permissions, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mList = (ListView) view.findViewById(R.id.permissions_list);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);

        mAdapter = new PermissionsAdapterDisplay(getMainActivity(), R.layout.permission_item,
                mPermissions, shouldShowExtendedToggle(), this);
        mList.setAdapter(mAdapter);

        fetchPermissions();
    }

    public boolean shouldShowExtendedToggle() {
        return false;
    }

    @Override
    public void onSwitchChange(String eventName, boolean isEnabled) {

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

    /**
     * Getting permissions for specific application, followed by -
     * Authenticate with Neura, where this demo app launches authorization in
     * the Neura app -- the user will see a Neura screen Request from Neura an
     * accessToken for this user for the requested permissions; the callback is
     * onActivityResult These permissions must be a subset of permissions you
     * declared on Neura's developer website, https://dev.theneura.com/#/manage
     */

    protected void fetchPermissions() {
        loadProgress(true);
        getMainActivity().getClient().getAppPermissions(new GetPermissionsRequestCallbacks() {
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
        permissions = getMainActivity().getClient().getPermissionStatus(permissions);
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
                return right.getDisplayName().compareTo(left.getDisplayName());
            } else if (left.isActive() && !right.isActive())
                return -1;
            else if (!left.isActive() && right.isActive())
                return 1;
            return 0;
        }
    };

    /**
     * Override this on {@link FragmentSubscribe} in order to fetch each subscription's state
     */
    protected void setSubscriptionsState() {

    }
}
