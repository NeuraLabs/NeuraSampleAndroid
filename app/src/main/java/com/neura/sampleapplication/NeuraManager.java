package com.neura.sampleapplication;

import android.content.Context;

import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;

/**
 * Singleton class for interacting with NeuraApiClient
 */
public class NeuraManager {

    private static NeuraManager sInstance = new NeuraManager();

    private NeuraApiClient mNeuraApiClient;

    public static NeuraManager getInstance() {
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
        Builder builder = new Builder(context);
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(context.getResources().getString(R.string.app_uid));
        mNeuraApiClient.setAppSecret(context.getResources().getString(R.string.app_secret));
        mNeuraApiClient.connect();
    }
}
