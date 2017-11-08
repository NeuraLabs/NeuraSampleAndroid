package com.neura.sampleapplication;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;
import com.neura.resources.authentication.AnonymousAuthenticateCallBack;
import com.neura.resources.authentication.AnonymousAuthenticateData;
import com.neura.resources.authentication.AnonymousAuthenticationStateListener;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.resources.authentication.AuthenticationState;
import com.neura.sampleapplication.fragments.FragmentMain;
import com.neura.sdk.object.AnonymousAuthenticationRequest;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;
import com.neura.standalonesdk.util.SDKUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Singleton class for interacting with NeuraApiClient
 */
public class NeuraManager {
    private static final String TAG = NeuraManager.class.getSimpleName();
    private static NeuraManager sInstance;

    private NeuraApiClient mNeuraApiClient;

    public static NeuraManager getInstance() {
        if (sInstance == null)
            sInstance = new NeuraManager();
        return sInstance;
    }

    private NeuraManager() {

    }

    public NeuraApiClient getClient() {
        return mNeuraApiClient;
    }

    /**
     * Initiation Neura's api client. Please notice that there are mandatory and optional fields :
     * 1. Mandatory : {@link NeuraApiClient#setAppUid(String)} : add your appUid, as listed in the
     * dev site https://s32.postimg.org/s1zya5zph/Screen_Shot_2016_07_07_at_10_02_32_AM.png
     * 2. Mandatory : {@link NeuraApiClient#setAppSecret(String)} : add your appSecret, as listed
     * in the dev site https://s32.postimg.org/s1zya5zph/Screen_Shot_2016_07_07_at_10_02_32_AM.png
     * 3. Mandatory : {@link NeuraApiClient#connect()} : connecting to the neura api client
     * 4. Optional : {@link NeuraApiClient#enableNeuraHandingStateAlertMessages(boolean)} : Default - true.
     * View full notification
     * Neura uses multiply sensors and permission, we're tracking when :
     * - (On Marshmallow os and above) A permission isn't granted by your user, and is required by
     * Neura to work. Fyi this applies only for permissions that are critical - location.
     * - Sensors are disabled by the user(location/wifi/bluetooth/network).
     * <b>Fyi</b> This only means that the sensors are disabled, not when there's no wifi available fe.
     * <br>We'll alert for the disabled sensors whenever Neura sdk might need it.
     * the settings). Fyi this only means that the sensors are disabled, not when there's no wifi available fe.
     */
    public void initNeuraConnection(Context context) {
        if (!isMinVersion()) {
            return;
        }

        Builder builder = new Builder(context);
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(context.getResources().getString(R.string.app_uid));
        mNeuraApiClient.setAppSecret(context.getResources().getString(R.string.app_secret));
        mNeuraApiClient.connect();
    }

    //create a call back to handle authentication stages.
    private static AnonymousAuthenticationStateListener silentStateListener = new AnonymousAuthenticationStateListener() {
        @Override
        public void onStateChanged(AuthenticationState state) {

            switch (state) {
                case AccessTokenRequested:
                    break;
                case AuthenticatedAnonymously:
                    // successful authentication
                    NeuraManager.getInstance().getClient().unregisterAuthStateListener();

//                    NeuraManager.getInstance().getClient().getUserDetails(new UserDetailsCallbacks() {
//                        @Override
//                        public void onSuccess(UserDetails userDetails) {
//                            if (getActivity() != null) {
//                                userDetails.getData().getNeuraId();
//                                NeuraManager.getInstance().getClient().getUserAccessToken();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Bundle resultData, int errorCode) {
//
//                        }
//                    });
                    break;
                case NotAuthenticated:
                case FailedReceivingAccessToken:
                    // Authentication failed indefinitely. a good opportunity to retry the authentication flow
                    NeuraManager.getInstance().getClient().unregisterAuthStateListener();
                    break;
                default:
            }
        }
    };

    public static void authenticateAnonymously(final FragmentMain context) {
        if (!isMinVersion()) {
            return;
        }

        if (NeuraManager.getInstance().getClient().isLoggedIn()) {
            return;
        }

        //Get the FireBase Instance ID, we will use it to instantiate AnonymousAuthenticationRequest
        String pushToken = FirebaseInstanceId.getInstance().getToken();

        //Instantiate AnonymousAuthenticationRequest instance.
        AnonymousAuthenticationRequest request = new AnonymousAuthenticationRequest(pushToken);

        //Pass the AnonymousAuthenticationRequest instance and register a call back for success and failure events.
        NeuraManager.getInstance().getClient().authenticate(request, new AnonymousAuthenticateCallBack() {
            @Override
            public void onSuccess(AnonymousAuthenticateData data) {
                NeuraManager.getInstance().getClient().registerAuthStateListener(silentStateListener);
                Log.i(TAG, "Successfully requested authentication with neura. ");
                context.setUIState(true, true);
            }

            @Override
            public void onFailure(int errorCode) {
                NeuraManager.getInstance().getClient().unregisterAuthStateListener();
                Log.e(TAG, "Failed to authenticate with neura. " + "Reason : " + SDKUtils.errorCodeToString(errorCode));
                context.loadProgress(false);
                context.mRequestPermissions.setEnabled(true);
            }
        });
    }


    /**
     * Authenticate with Neura
     * Receiving unique neuraUserId and accessToken (for external api calls : https://dev.theneura.com/docs/api/insights)
     */
    public static void authenticateByPhone(String phoneNumber, final FragmentMain context) {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setPhone(phoneNumber);
        NeuraManager.getInstance().getClient().authenticate(request, new AuthenticateCallback() {
            @Override
            public void onSuccess(AuthenticateData authenticateData) {
                Log.i(getClass().getSimpleName(), "Successfully authenticate with neura. NeuraUserId = "
                        + authenticateData.getNeuraUserId() + ". AccessToken = " + authenticateData.getAccessToken());
                context.setUIState(true, true);

                /**
                 * Go to our push notification guide for more info on how to register receiving
                 * events via firebase https://dev.theneura.com/docs/guide/android/pushnotification.
                 * If you're receiving a 'Token already exists error',make sure you've initiated a
                 * Firebase instance like {@link com.neura.sampleapplication.activities.MainActivity#onCreate(Bundle)}
                 * http://stackoverflow.com/a/38945375/5130239
                 */
                NeuraManager.getInstance().getClient().registerFirebaseToken(context.getActivity(),
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
                    context.subscribeToEvent(events.get(i));
                }
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. Reason : "
                        + SDKUtils.errorCodeToString(errorCode));
                context.loadProgress(false);
                context.mRequestPermissions.setEnabled(true);
            }
        });
    }

    private static boolean isMinVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

}