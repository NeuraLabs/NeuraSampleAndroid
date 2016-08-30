package com.neura.sampleapplication.activities;

import android.app.Activity;
import android.os.Bundle;

import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;
import com.neura.sampleapplication.fragments.BaseFragment;
import com.neura.sampleapplication.fragments.FragmentMain;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
