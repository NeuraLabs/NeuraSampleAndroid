package com.neura.sampleapplication;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.neura.resources.authentication.AnonymousAuthenticateCallBack;
import com.neura.resources.authentication.AnonymousAuthenticateData;
import com.neura.resources.authentication.AnonymousAuthenticationStateListener;
import com.neura.sdk.object.AnonymousAuthenticationRequest;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.SDKUtils;

/**
 * Singleton class for interacting with NeuraApiClient
 */
public class NeuraHelper {

    private static final String TAG = NeuraHelper.class.getSimpleName();

    private NeuraApiClient mNeuraApiClient;

    public NeuraHelper(Context context) {
        // Replace Place holders with your own Neura APP_ID and Neura APP_SECRET
        mNeuraApiClient = NeuraApiClient.getClient(context, "[APP_ID]", "[APP_SECRET]");
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
     * We'll alert for the disabled sensors whenever Neura sdk might need it.
     */

    public void authenticateAnonymously(final AnonymousAuthenticationStateListener silentStateListener) {
        if (!isMinVersion()) {
            return;
        }

        if (mNeuraApiClient.isLoggedIn()) {
            return;
        }

        //Get the FireBase Instance ID, we will use it to instantiate AnonymousAuthenticationRequest
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        if (task.getResult() != null) {
                            String pushToken = task.getResult().getToken();

                            //Instantiate AnonymousAuthenticationRequest instance.
                            AnonymousAuthenticationRequest request = new AnonymousAuthenticationRequest(pushToken);

                            //Pass the AnonymousAuthenticationRequest instance and register a call back for success and failure events.
                            mNeuraApiClient.authenticate(request, new AnonymousAuthenticateCallBack() {
                                @Override
                                public void onSuccess(AnonymousAuthenticateData data) {
                                    mNeuraApiClient.registerAuthStateListener(silentStateListener);
                                    Log.i(TAG, "Successfully requested authentication with neura. ");
                                }

                                @Override
                                public void onFailure(int errorCode) {
                                    mNeuraApiClient.unregisterAuthStateListener();
                                    Log.e(TAG, "Failed to authenticate with neura. " + "Reason : " + SDKUtils.errorCodeToString(errorCode));
                                }
                            });
                        } else {
                            Log.e(TAG, "Firebase task returned without result, cannot proceed with Authentication flow.");
                        }
                    }
                });

    }

    private static boolean isMinVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
