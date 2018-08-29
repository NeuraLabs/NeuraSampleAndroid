package com.neura.sampleapplication.activities;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sampleapplication.fragments.BaseFragment;
import com.neura.sampleapplication.fragments.FragmentMain;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
        setContentView(R.layout.activity_main);

        //http://stackoverflow.com/a/38945375/5130239
        try {
            FirebaseApp.getInstance();
        } catch (IllegalStateException ex) {
            FirebaseApp.initializeApp(this, FirebaseOptions.fromResource(this));
        }

        NeuraManager.getInstance().initNeuraConnection(getApplicationContext());
        openFragment(new FragmentMain());
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        NeuraManager.getInstance().getClient().disconnect();
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
}