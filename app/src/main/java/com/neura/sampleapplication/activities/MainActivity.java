package com.neura.sampleapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.neura.sampleapplication.R;
import com.neura.sampleapplication.fragments.BaseFragment;
import com.neura.sampleapplication.fragments.FragmentMain;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;

/**
 * Created by Hadas on 9/16/2015.
 */
public class MainActivity extends Activity {

    /**
     * SDK variables
     */
    private NeuraApiClient mNeuraApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openFragment(new FragmentMain());
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
     * 5. Optional : {@link NeuraApiClient#enableSettingsSystemMessages(boolean)} : Default - false.
     * By enabling the settings system messages you're allowing neura sdk to present system messages
     * to your user. Fe https://s32.postimg.org/bhfw2ni9x/location_settings.png
     * 6. Optional : {@link NeuraApiClient#enableLogFile(boolean)} : Default - false.
     * By enabling the log file we'll be able to track errors your user might have.
     * This isn't mandatory, but it'll help us detect any issues you might have with neura.
     * By default - log file IS NOT enabled.
     * 7. Optional : {@link NeuraApiClient#enableAutomaticallySyncLogs(boolean)} : Default - false.
     * The log collected in {@link NeuraApiClient#enableLogFile(boolean)} will be sent to our
     * servers, in order to track issues your user might be having.
     */
    public void initNeuraConnection() {
        Builder builder = new Builder(this);
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(getResources().getString(R.string.app_uid));
        mNeuraApiClient.setAppSecret(getResources().getString(R.string.app_secret));
        mNeuraApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mNeuraApiClient != null)
            mNeuraApiClient.disconnect();
    }

    public void openFragment(BaseFragment newFragment) {
        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        int backStackCount = getFragmentManager().getBackStackEntryCount();
        if (backStackCount == 1) {
            finish();
        } else {
            getFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getFragmentManager().findFragmentById(R.id.fragment_container)
                .onActivityResult(requestCode, resultCode, data);
    }

}
