package com.neura.sampleapplication.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
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
import com.neura.resources.authentication.AnonymousAuthenticationStateListener;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.resources.authentication.AuthenticationState;
import com.neura.resources.user.UserDetails;
import com.neura.resources.user.UserDetailsCallbacks;
import com.neura.sampleapplication.NeuraEventsService;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.util.NeuraUtil;
import com.neura.standalonesdk.util.SDKUtils;

import java.util.List;

public class FragmentMain extends BaseFragment {

    public Button mRequestPermissions;
    private Button mDisconnect;
    private Button mSimulateAnEvent;
    private Button mAddDevice;
    private Button mServices;

    private ImageView mSymbolTop;
    private ImageView mSymbolBottom;
    private TextView mNeuraStatus;

    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestLocationPermission();

        mSymbolTop = (ImageView) view.findViewById(R.id.neura_symbol_top);
        mSymbolBottom = (ImageView) view.findViewById(R.id.neura_symbol_bottom);

        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mRequestPermissions = (Button) view.findViewById(R.id.request_permissions_btn);
        mDisconnect = (Button) view.findViewById(R.id.disconnect);
        mSimulateAnEvent = (Button) view.findViewById(R.id.event_simulation);
        mAddDevice = (Button) view.findViewById(R.id.add_device);
        mServices = (Button) view.findViewById(R.id.services_button);
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

        mSimulateAnEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().openFragment(new FragmentEventSimulation());
            }
        });

        mAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().openFragment(new FragmentDeviceOperations());
            }
        });

        ((TextView) view.findViewById(R.id.version)).
                setText("Sdk Version : " + NeuraManager.getInstance().getClient().getSdkVersion());

        mServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().openFragment(new FragmentServices());
            }
        });
    }

    /**
     * Authenticate with Neura
     * Receiving unique neuraUserId and accessToken (for external api calls : https://dev.theneura.com/docs/api/insights)
     */
    public void authenticateByPhone() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setPhone(((EditText) getView().findViewById(R.id.phone_number)).getText().toString());
        NeuraManager.getInstance().getClient().authenticate(request, new AuthenticateCallback() {
            @Override
            public void onSuccess(AuthenticateData authenticateData) {
                Log.i(getClass().getSimpleName(), "Successfully authenticate with neura. NeuraUserId = "
                        + authenticateData.getNeuraUserId() + ". AccessToken = " + authenticateData.getAccessToken());
                userId = authenticateData.getNeuraUserId();

                boolean isConnected = true;
                boolean setSymbol = true;
                setUIState(isConnected, setSymbol);

                subscribeToPushEvents();
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. Reason : "
                        + SDKUtils.errorCodeToString(errorCode));
                boolean enabled = true;
                loadProgress(!enabled);
                mRequestPermissions.setEnabled(enabled);
            }
        });
    }

    private void subscribeToPushEvents() {
        /**
         * Go to our push notification guide for more info on how to register receiving
         * events via firebase https://dev.theneura.com/docs/guide/android/pushnotification.
         * If you're receiving a 'Token already exists error',make sure you've initiated a
         * Firebase instance like {@link com.neura.sampleapplication.activities.MainActivity#onCreate(Bundle)}
         * http://stackoverflow.com/a/38945375/5130239
         */
        NeuraManager.getInstance().getClient().
                registerFirebaseToken(FirebaseInstanceId.getInstance().getToken());

        List<String> events = NeuraManager.getEvents();
        //Subscribing to events - mandatory in order to receive events.
        for (int i = 0; i < events.size(); i++) {
            subscribeToEvent(events.get(i));
        }
    }

    //create a call back to handle authentication stages.
    AnonymousAuthenticationStateListener silentStateListener = new AnonymousAuthenticationStateListener() {
        @Override
        public void onStateChanged(AuthenticationState state) {
            switch (state) {
                case AccessTokenRequested:
                    break;
                case AuthenticatedAnonymously:
                    // successful authentication
                    NeuraManager.getInstance().getClient().unregisterAuthStateListener();
                    // do something with the user's details...
                    getUserDetails();

                    // Trigger UI changes
                    boolean isConnected = true;
                    boolean setSymbol = true;
                    setUIState(isConnected, setSymbol);

                    // Subscribe to neura moments so that you can receive push notifications
                    subscribeToPushEvents();
                    break;
                case NotAuthenticated:
                case FailedReceivingAccessToken:
                    // Authentication failed indefinitely. a good opportunity to retry the authentication flow
                    NeuraManager.getInstance().getClient().unregisterAuthStateListener();

                    // Trigger UI changes
                    boolean enabled = true;
                    loadProgress(!enabled);
                    mRequestPermissions.setEnabled(enabled);
                    break;
                default:
            }
        }
    };

    private void requestLocationPermission(){
        if (ActivityCompat.checkSelfPermission(getActivity(),
            android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION}, 1111);
            return;
        }
        else{
            // Make sure the user enables location,
            // this is needed to for Neura to work, and is not automatic when using anonymous authentication.
            // Phone based auth asks for it automatically.
        }
    }

    private void getUserDetails() {
        NeuraManager.getInstance().getClient().getUserDetails(new UserDetailsCallbacks() {
            @Override
            public void onSuccess(UserDetails userDetails) {
                if (userDetails.getData() != null) {
                    // Do something with this information
                    userId = userDetails.getData().getNeuraId();
                    NeuraManager.getInstance().getClient().getUserAccessToken();
                }
            }

            @Override
            public void onFailure(Bundle resultData, int errorCode) {
            }
        });
    }

    public void setUIState(final boolean isConnected, boolean setSymbol) {
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.alert_authenticate_dialog);
                builder.setPositiveButton(R.string.auth_phone, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        authenticateByPhone();
                    }
                });
                builder.setNeutralButton(R.string.auth_anon, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NeuraManager.authenticateAnonymously(silentStateListener);
                    }
                });

                AlertDialog popup = builder.create();
                popup.show();
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
        System.out.println(NeuraManager.getInstance().getClient().isLoggedIn());
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
        mServices.setEnabled(!enabled);
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
     * In this application, this is done on {@link FragmentMain#authenticateByPhone()}.
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
    public void subscribeToEvent(String eventName) {
        String eventIdentifier = userId + eventName;
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