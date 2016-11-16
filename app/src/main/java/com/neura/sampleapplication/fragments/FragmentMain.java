package com.neura.sampleapplication.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.resources.data.PickerCallback;
import com.neura.sampleapplication.NeuraEventsService;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.Permission;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.util.NeuraUtil;
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
        getView().findViewById(R.id.phone_injection_layout).setVisibility(isConnected ? View.GONE : View.VISIBLE);
        mRequestPermissions.setText(R.string.connect_request_permissions);

        mRequestPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getMainActivity().requestSmsPermission())
                    authenticateWithNeura();
            }
        });

        mRequestPermissions.setVisibility(isConnected ? View.GONE : View.VISIBLE);
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

        ((EditText) getView().findViewById(R.id.phone_number)).setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    /**
     * Authenticate with Neura
     * Receiving unique neuraUserId and accessToken (for external api calls : https://dev.theneura.com/docs/api/insights)
     */
    public void authenticateWithNeura() {
        AuthenticationRequest request = new AuthenticationRequest(mPermissions);
        request.setPhone(((EditText) getView().findViewById(R.id.phone_number)).getText().toString());
        NeuraManager.getInstance().getClient().authenticate(request, new AuthenticateCallback() {
            @Override
            public void onSuccess(AuthenticateData authenticateData) {
                Log.i(getClass().getSimpleName(), "Successfully authenticate with neura. NeuraUserId = "
                        + authenticateData.getNeuraUserId() + ". AccessToken = " + authenticateData.getAccessToken());
                setUIState(true, true);

                /**
                 * Go to our push notification guide for more info on how to register receiving
                 * events via firebase https://dev.theneura.com/docs/guide/android/pushnotification.
                 * If you're receiving a 'Token already exists error',make sure you've initiated a
                 * Firebase instance like {@link com.neura.sampleapplication.activities.MainActivity#onCreate(Bundle)}
                 * http://stackoverflow.com/a/38945375/5130239
                 */
                NeuraManager.getInstance().getClient().registerFirebaseToken(getActivity(),
                        FirebaseInstanceId.getInstance().getToken());

                //Subscribing to events - mandatory in order to receive events.
                for (int i = 0; i < mPermissions.size(); i++) {
                    subscribeToEvent(mPermissions.get(i).getName());
                }
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

    /**
     * Subscribe / unsubscribe to receive events from Neura.
     * <br>Neura will continue notifying this event, until
     * {@link com.neura.standalonesdk.service.NeuraApiClient#removeSubscription(String, String, boolean, SubscriptionRequestCallbacks)}
     * will be called.
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
     * An important note :
     * ------------------------
     * EventIdentifier should be unique for each event, for example,
     * subscribeToEvent(userStartedDriving, iden1, mSubscribeRequest) - success
     * subscribeToEvent(userFinishedDriving, iden1, mSubscribeRequest) - fail since event
     * userStartedDriving already used iden1.
     *
     * @param eventName event to notify
     */
    private void subscribeToEvent(String eventName) {
        String eventIdentifier = "YourEventIdentifier_" + eventName;
        NeuraManager.getInstance().getClient().subscribeToEvent(eventName, eventIdentifier, true, mSubscribeRequest);
    }

    private SubscriptionRequestCallbacks mSubscribeRequest = new SubscriptionRequestCallbacks() {
        @Override
        public void onSuccess(final String eventName, Bundle resultData, String identifier) {
            loadProgress(false);

            /**
             * For events related to locations, if you're subscribing to the event userArrivedHome (fe),
             * and your user is new on Neura, we don't know his/her home yet. The preferable way is to
             * wait few days for Neura to detect it, BUT, if you need the home NOW, you can call
             * {@link com.neura.standalonesdk.service.NeuraApiClient#getMissingDataForEvent(String, PickerCallback)}
             * which will open a place picker for your user to select his/her home.
             * Method is disabled in this application.
             */
//            NeuraManager.getInstance().getClient().getMissingDataForEvent(
//                    eventName, new PickerCallback() {
//                        @Override
//                        public void onResult(boolean success) {
//                            Log.i(getClass().getSimpleName(), (success ?
//                                    "Successfully added data for event : " :
//                                    "User canceled adding data for event : ") + eventName);
//                        }
//                    });
        }

        @Override
        public void onFailure(String eventName, Bundle resultData, int errorCode) {
            Toast.makeText(getMainActivity(),
                    "Error: Failed to subscribe to event " + eventName + ". Error code: " +
                            NeuraUtil.errorCodeToString(errorCode), Toast.LENGTH_SHORT).show();
        }
    };

}
