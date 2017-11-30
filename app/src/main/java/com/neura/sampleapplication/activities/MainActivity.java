package com.neura.sampleapplication.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sampleapplication.fragments.BaseFragment;
import com.neura.sampleapplication.fragments.FragmentMain;

import java.util.Arrays;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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