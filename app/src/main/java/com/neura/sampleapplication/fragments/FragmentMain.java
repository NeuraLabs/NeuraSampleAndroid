package com.neura.sampleapplication.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
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

import com.neura.sampleapplication.R;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.Permission;
import com.neura.standalonesdk.util.SDKUtils;

import java.util.ArrayList;

/**
 * Created by Hadas on 9/16/2015.
 */
public class FragmentMain extends BaseFragment {

    private Button mYourNeura;
    private Button mRequestPermissions;
    private Button mSendLog;
    private Button mDisplayPermissions;
    private Button mSimulateAnEvent;
    private Button mAddDevice;
    private Button mServices;

    private ImageView mSymbolTop;
    private ImageView mSymbolBottom;
    private TextView mNeuraStatus;

    private static final int NEURA_AUTHENTICATION_REQUEST_CODE = 0;

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
        mYourNeura = (Button) view.findViewById(R.id.your_neura);
        mRequestPermissions = (Button) view.findViewById(R.id.request_permissions_btn);
        mSendLog = (Button) view.findViewById(R.id.send_log);
        mDisplayPermissions = (Button) view.findViewById(R.id.permissions_list_btn);
        mSimulateAnEvent = (Button) view.findViewById(R.id.event_simulation);
        mAddDevice = (Button) view.findViewById(R.id.add_device);
        mServices = (Button) view.findViewById(R.id.services_button);
        mNeuraStatus = (TextView) view.findViewById(R.id.neura_status);

        getMainActivity().initNeuraConnection();

        /** Copy the permissions you've declared to your application from
         * https://dev.theneura.com/console/edit/YOUR_APPLICATION - permissions section,
         * and initialize mPermissions with them.
         * for example : https://s31.postimg.org/x8phjuza3/Screen_Shot_2016_07_27_at_1.png
         */
        mPermissions = new ArrayList<>(Permission.list(new String[]{
                "userStartedDriving", "userLeftHome", "userArrivedToWork",
                "userFinishedWalking", "userStartedWorkOut", "userWokeUp",
                "userLeftGym", "userFinishedRunning", "userArrivedToGym",
                "userArrivedHome", "userStartedSleeping", "userFinishedDriving",
                "userLeftWork", "userLeftActiveZone", "userStartedRunning",
                "userArrivedAtActiveZone", "userIsOnTheWayToWork", "userIsOnTheWayHome",
                "userIsOnTheWayToActiveZone", "userIsIdle", "userStartedWalking",
                "userArrivedHomeFromWork", "userArrivedWorkFromHome", "userDetails",
                "activitySummaryPerPlace", "wellnessProfile", "dailyActivitySummary",
                "getPersonNodesSemantics", "getLocationNodesSemantics", "sleepData",
                "getDeviceNodesSemantics", "userSituation"
        }));

        mSymbolTop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setTopSymbol(SDKUtils.isConnected(getActivity(), getMainActivity().getClient()));
                mSymbolTop.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        mSymbolBottom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setBottomSymbol(SDKUtils.isConnected(getActivity(), getMainActivity().getClient()));
                mSymbolBottom.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        setUIState(SDKUtils.isConnected(getActivity(), getMainActivity().getClient()), false);

        mSendLog.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            getMainActivity().getClient().sendLog(getMainActivity());
                                        }
                                    }

        );

        mSimulateAnEvent.setOnClickListener(new View.OnClickListener()

                                            {
                                                @Override
                                                public void onClick(View v) {
                                                    getMainActivity().getClient().simulateAnEvent();
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
                setText("Version : " + getMainActivity().getClient().getSdkVersion());

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
        mDisplayPermissions.setText(getString(isConnected ?
                R.string.disconnect_from_neura : R.string.app_permissions_list));
        setEnableOnButtons(isConnected);
        mRequestPermissions.setText(getString(isConnected ?
                R.string.edit_subscriptions : R.string.connect_request_permissions));

        mDisplayPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    getMainActivity().getClient().forgetMe(true, new android.os.Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            setUIState(false, true);
                            loadProgress(false);
                            return true;
                        }
                    });
                } else {
                    FragmentPermissions frag = new FragmentPermissions();
                    Bundle bundle = new Bundle();
                    frag.setArguments(bundle);
                    getMainActivity().openFragment(frag);
                }
            }
        });

        mRequestPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected)
                    openSubscribeFragment();
                else {
                    //The response for authenticate is received on onActivityResult method in this class
                    AuthenticationRequest authenticationRequest = new AuthenticationRequest();
                    authenticationRequest.setPermissions(mPermissions);
                    getMainActivity().getClient().authenticate(NEURA_AUTHENTICATION_REQUEST_CODE,
                            authenticationRequest);
                }
            }
        });

        mYourNeura.setOnClickListener(isConnected ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().getClient().openNeuraSettingsPanel(getString(R.string.app_uid));
            }
        } : null);

        mRequestPermissions.setVisibility((isConnected && !getResources().getBoolean(R.bool.use_google)) ? View.GONE : View.VISIBLE);

        loadProgress(false);
    }

    private void setEnableOnButtons(boolean isConnected) {
        mSendLog.setAlpha(isConnected ? 1 : 0.5f);
        mSendLog.setEnabled(isConnected);
        mYourNeura.setAlpha(isConnected ? 1 : 0.5f);
        mYourNeura.setEnabled(isConnected);
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

    /**
     * When calling getMainActivity().getClient().authenticate(NEURA_AUTHENTICATION_REQUEST_CODE, mAuthenticateRequest);
     * the response is received here.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEURA_AUTHENTICATION_REQUEST_CODE && resultCode == FragmentActivity.RESULT_OK) {
            setUIState(true, true);
            /**
             * {@link com.neura.standalonesdk.service.NeuraApiClient#registerPushServerApiKey(Activity, String)}
             * isn't mandatory, only if you want Neura to handle events and let you know when
             * an event occurs. Register with your project_id.
             * Further more, please see explanation in
             * {@link FragmentSubscribe#subscribeToFromEvent(String, boolean)}
             */
            getMainActivity().getClient().registerPushServerApiKey(getMainActivity(), getString(R.string.google_api_project_number));
            Log.i(getClass().getSimpleName(), "Successfully logged in with accessToken : "
                    + SDKUtils.extractToken(data));
        } else {
            loadProgress(false);
            mRequestPermissions.setEnabled(true);
        }
    }

    @Override
    public void loadProgress(boolean enabled) {
        super.loadProgress(enabled);
        mRequestPermissions.setEnabled(!enabled);
        mDisplayPermissions.setEnabled(!enabled);
    }

    private void openSubscribeFragment() {
        FragmentSubscribe frag = new FragmentSubscribe();
        Bundle bundle = new Bundle();
//        bundle.putParcelableArrayList(MainActivity.EXTRA_PERMISSIONS_LIST, mPermissions);
        frag.setArguments(bundle);
        getMainActivity().openFragment(frag);
    }

}
