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
import com.neura.sampleapplication.NeuraEventsService;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.util.NeuraUtil;
import com.neura.standalonesdk.util.SDKUtils;

import java.util.Arrays;
import java.util.List;

public class FragmentMain extends BaseFragment {

    private Button mRequestPermissions;
    private Button mDisconnect;
    private Button mSimulateAnEvent;
    private Button mAddDevice;
    private Button mServices;
    private Button mAddLocation;

    private ImageView mSymbolTop;
    private ImageView mSymbolBottom;
    private TextView mNeuraStatus;

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
        mAddLocation = (Button) view.findViewById(R.id.add_place_btn);
        mNeuraStatus = (TextView) view.findViewById(R.id.neura_status);

        mSymbolTop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setTopSymbol(NeuraManager.getInstance().getClient().isLoggedIn());
                mSymbolTop.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        mSymbolBottom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setBottomSymbol(NeuraManager.getInstance().getClient().isLoggedIn());
                mSymbolBottom.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        setUIState(NeuraManager.getInstance().getClient().isLoggedIn(), false);

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

        mAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().openFragment(new AddLocationFragment());
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
        mServices.setVisibility(isConnected ? View.VISIBLE : View.GONE);
        mDisconnect.setEnabled(isConnected);
        loadProgress(false);

        mDisconnect.setOnClickListener(isConnected ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        } : null);

        ((EditText) getView().findViewById(R.id.phone_number)).setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    private void disconnect() {
        loadProgress(true);
        NeuraManager.getInstance().getClient().forgetMe(getActivity(), true, new android.os.Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                loadProgress(false);
                if (msg.arg1 == 1)
                    setUIState(NeuraManager.getInstance().getClient().isLoggedIn(), true);
                return true;
            }
        });
    }

    /**
     * Authenticate with Neura
     * Receiving unique neuraUserId and accessToken (for external api calls : https://dev.theneura.com/docs/api/insights)
     */
    public void authenticateWithNeura() {
        AuthenticationRequest request = new AuthenticationRequest();
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

                //TODO put here a list of events that you wish to receive. Beware, that these events must be listed to your application on our dev site. https://dev.theneura.com/console/apps
                List<String> events = Arrays.asList("userArrivedHome", "userArrivedHomeFromWork",
                        "userLeftHome", "userArrivedHomeByWalking", "userArrivedHomeByRunning",
                        "userIsOnTheWayHome", "userIsIdleAtHome", "userStartedWorkOut",
                        "userFinishedRunning", "userFinishedWorkOut", "userLeftGym",
                        "userFinishedWalking", "userArrivedToGym", "userIsIdleFor2Hours",
                        "userStartedWalking", "userIsIdleFor1Hour",
                        "userStartedTransitByWalking", "userStartedRunning",
                        "userFinishedTransitByWalking", "userFinishedDriving", "userStartedDriving",
                        "userArrivedAtActiveZone", "userArrivedAtSchoolCampus",
                        "userArrivedAtAirport", "userArrivedAtClinic",
                        "userArrivedAtCafe", "userArrivedAtRestaurant", "userLeftSchoolCampus",
                        "userIsOnTheWayToActiveZone", "userLeftCafe", "userArrivedAtGroceryStore",
                        "userArrivedAtHospital", "userLeftHospital", "userLeftRestaurant",
                        "userLeftAirport", "userLeftActiveZone", "userArrivedAtPharmacy",
                        "userArrivedToWorkByRunning", "userArrivedToWork", "userArrivedWorkFromHome",
                        "userArrivedToWorkByWalking", "userLeftWork", "userIsOnTheWayToWork",
                        "userStartedSleeping", "userWokeUp", "userGotUp", "userIsAboutToGoToSleep",
                        "userStartedDriving", "userLeftHome", "userArrivedToWork",
                        "userFinishedRunning", "userArrivedToGym", "userFinishedWalking",
                        "userFinishedTransitByWalking", "userStartedWorkOut", "userWokeUp",
                        "userLeftGym", "userArrivedHome", "userStartedSleeping",
                        "userFinishedDriving", "userLeftWork", "userLeftActiveZone",
                        "userStartedRunning", "userArrivedAtActiveZone", "userIsOnTheWayToWork",
                        "userIsOnTheWayHome", "userIsOnTheWayToActiveZone", "userIsIdleFor2Hours",
                        "userIsIdleFor1Hour", "userIsIdleAtHome", "userStartedWalking",
                        "userStartedTransitByWalking", "userArrivedHomeFromWork",
                        "userArrivedWorkFromHome", "userIsAboutToGoToSleep");

                //Subscribing to events - mandatory in order to receive events.
                for (int i = 0; i < events.size(); i++) {
                    subscribeToEvent(events.get(i));
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
        mAddLocation.setEnabled(isConnected);
        mAddLocation.setAlpha(isConnected ? 1 : 0.5f);
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
        mServices.setEnabled(!enabled);
        mAddLocation.setEnabled(!enabled);
        mSimulateAnEvent.setEnabled(!enabled);
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
        NeuraManager.getInstance().getClient().subscribeToEvent(eventName, eventIdentifier, mSubscribeRequest);
    }

    private SubscriptionRequestCallbacks mSubscribeRequest = new SubscriptionRequestCallbacks() {
        @Override
        public void onSuccess(final String eventName, Bundle resultData, String identifier) {
            loadProgress(false);
        }

        @Override
        public void onFailure(String eventName, Bundle resultData, int errorCode) {
            if (getActivity() != null)
                Toast.makeText(getMainActivity(),
                        "Error: Failed to subscribe to event " + eventName + ". Error code: " +
                                NeuraUtil.errorCodeToString(errorCode), Toast.LENGTH_SHORT).show();
        }
    };

}
