package com.neura.sampleapplication.fragments;

import android.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;

import com.neura.sampleapplication.activities.MainActivity;

/**
 * Created by Hadas on 9/16/2015.
 */
public abstract class BaseFragment extends Fragment {

    protected static final int NEURA_SDK_REQUEST_CODE = 1;

    protected ProgressBar mProgress;

    protected void loadProgress(boolean enable) {
        if (mProgress != null)
            mProgress.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
