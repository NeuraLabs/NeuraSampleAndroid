package com.neura.sampleapplication.fragments;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.Permission;
import com.neura.standalonesdk.util.SDKUtils;

import java.util.ArrayList;

public class FragmentMain extends BaseFragment {

    private Button mRequestPermissions;
    private Button mDisconnect;
    private Button mSimulateAnEvent;
    private Button mAddDevice;
    private Button mServices;

    private ImageView mSymbolTop;
    private ImageView mSymbolBottom;
    private TextView mNeuraStatus;

    private ArrayList<Permission> mPermissions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSymbolTop = (ImageView) view.findViewById(R.id.neura_symbol_top);
        mSymbolBottom = (ImageView) view.findViewById(R.id.neura_symbol_bottom);

        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mRequestPermissions = (Button) view.findViewById(R.id.request_permissions_btn);
        mDisconnect = (Button) view.findViewById(R.id.disconnect);
        mSimulateAnEvent = (Button) view.findViewById(R.id.event_simulation);
        mAddDevice = (Button) view.findViewById(R.id.add_device);
        mServices = (Button) view.findViewById(R.id.services_button);
        mNeuraStatus = (TextView) view.findViewById(R.id.neura_status);

        /** Copy the permissions you've declared to your application from
         * https://dev.theneura.com/console/edit/YOUR_APPLICATION - permissions section,
         * and initialize mPermissions with them.
         * for example : https://s31.postimg.org/x8phjuza3/Screen_Shot_2016_07_27_at_1.png
         */
        mPermissions = new ArrayList<>(Permission.list(new String[]{
                "userStartedDriving", "userLeftHome", "userArrivedToWork", "userFinishedWalking",
                "userStartedWorkOut", "userWokeUp", "userLeftGym", "userFinishedRunning",
                "userArrivedToGym", "userArrivedHome", "userStartedSleeping", "userFinishedDriving",
                "userLeftWork", "userLeftActiveZone", "userStartedRunning",
                "userArrivedAtActiveZone", "userIsOnTheWayToWork", "userIsOnTheWayHome",
                "userIsOnTheWayToActiveZone", "userIsIdle", "userStartedWalking",
                "userArrivedHomeFromWork", "userArrivedWorkFromHome", "userDetails",
                "activitySummaryPerPlace", "wellnessProfile", "dailyActivitySummary",
                "getPersonNodesSemantics", "getLocationNodesSemantics", "sleepData",
                "getDeviceNodesSemantics", "userSituation", "userPhoneNumber"}));

        mSymbolTop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setTopSymbol(SDKUtils.isConnected(getActivity(), NeuraManager.getInstance().getClient()));
                mSymbolTop.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        mSymbolBottom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setBottomSymbol(SDKUtils.isConnected(getActivity(), NeuraManager.getInstance().getClient()));
                mSymbolBottom.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        setUIState(SDKUtils.isConnected(getActivity(), NeuraManager.getInstance().getClient()), false);

        mSimulateAnEvent.setOnClickListener(new View.OnClickListener()

                                            {
                                                @Override
                                                public void onClick(View v) {
                                                    NeuraManager.getInstance().getClient().simulateAnEvent();
                                                }
                                            }
        );

        mAddDevice.setOnClickListener(new View.OnClickListener()

                                      {
                                          @Override
                                          public void onClick(View v) {
                                              getMainActivity().openFragment(new FragmentDeviceOperations());
                                          }
                                      }
        );

        ((TextView) view.findViewById(R.id.version)).
                setText("Sdk Version : " + NeuraManager.getInstance().getClient().getSdkVersion());

        mServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().openFragment(new FragmentServices());
            }
        });
    }

    private void setUIState(final boolean isConnected, boolean setSymbol) {
        if (setSymbol)
            setSymbols(isConnected);
        loadProgress(!isConnected);
        mNeuraStatus.setText(getString(isConnected ? R.string.neura_status_connected : R.string.neura_status_disconnected));
        mNeuraStatus.setTextColor(getResources().getColor(isConnected ? R.color.green_connected : R.color.red_disconnected));
        setEnableOnButtons(isConnected);
        mRequestPermissions.setText(getString(isConnected ?
                R.string.edit_subscriptions : R.string.connect_request_permissions));

        mRequestPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected)
                    openSubscribeFragment();
                else {
                    authenticateWithNeura();
                }
            }
        });

        mRequestPermissions.setVisibility((isConnected && !getResources().getBoolean(R.bool.use_google)) ? View.GONE : View.VISIBLE);
        mDisconnect.setEnabled(isConnected);
        loadProgress(false);

        mDisconnect.setOnClickListener(isConnected ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NeuraManager.getInstance().getClient().forgetMe(getActivity(), true, new android.os.Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        setUIState(false, true);
                        loadProgress(false);
                        return true;
                    }
                });
            }
        } : null);
    }

    /**
     * Authenticate with Neura
     * Receiving unique neuraUserId and accessToken (for external api calls : https://dev.theneura.com/docs/api/insights)
     */
    private void authenticateWithNeura() {
        AuthenticationRequest request = new AuthenticationRequest(mPermissions);
        NeuraManager.getInstance().getClient().authenticate(request, new AuthenticateCallback() {
            @Override
            public void onSuccess(AuthenticateData authenticateData) {
                Log.i(getClass().getSimpleName(), "Successfully authenticate with neura. NeuraUserId = "
                        + authenticateData.getNeuraUserId() + ". AccessToken = " + authenticateData.getAccessToken());
                setUIState(true, true);
                NeuraManager.getInstance().getClient().registerPushServerApiKey(getMainActivity(), getString(R.string.google_api_project_number));
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. Reason : "
                        + SDKUtils.errorCodeToString(errorCode));
                loadProgress(false);
                mRequestPermissions.setEnabled(true);
            }
        });
    }

    private void setEnableOnButtons(boolean isConnected) {
        mSimulateAnEvent.setAlpha(isConnected ? 1 : 0.5f);
        mSimulateAnEvent.setEnabled(isConnected);
        mAddDevice.setEnabled(isConnected);
        mAddDevice.setAlpha(isConnected ? 1 : 0.5f);
        mServices.setEnabled(isConnected);
        mServices.setAlpha(isConnected ? 1 : 0.5f);
    }

    private void setTopSymbol(boolean isConnected) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSymbolTop.getLayoutParams();
        params.rightMargin = isConnected ? 0 : (int) (mSymbolTop.getWidth() / 1.4);
        mSymbolTop.setLayoutParams(params);
    }

    private void setBottomSymbol(boolean isConnected) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSymbolBottom.getLayoutParams();
        params.leftMargin = isConnected ? 0 : (int) (mSymbolBottom.getWidth() / 1.4);
        mSymbolBottom.setLayoutParams(params);
    }

    private void setSymbols(final boolean isConnected) {
        setTopSymbol(isConnected);
        setBottomSymbol(isConnected);
    }

    @Override
    public void loadProgress(boolean enabled) {
        super.loadProgress(enabled);
        mRequestPermissions.setEnabled(!enabled);
        mDisconnect.setEnabled(!enabled);
    }

    private void openSubscribeFragment() {
        FragmentSubscribe frag = new FragmentSubscribe();
        Bundle bundle = new Bundle();
        frag.setArguments(bundle);
        getMainActivity().openFragment(frag);
    }

}
