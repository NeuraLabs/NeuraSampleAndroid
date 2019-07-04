package com.neura.sampleapplication.fragments;

import android.app.Fragment;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import com.neura.sampleapplication.NeuraHelper;
import com.neura.sampleapplication.SampleApplication;

public abstract class BaseFragment extends Fragment {

    protected ProgressBar mProgress;

    protected void loadProgress(boolean enable) {
        if (mProgress != null)
            mProgress.setVisibility(enable ? View.VISIBLE : View.GONE);
    }


    /**
     * @return {@link NeuraHelper} instance or NULL if getActivity() returns null.
     */
    protected NeuraHelper getNeuraHelper(){
        Context context = getActivity();
        if(context != null){
            return ((SampleApplication)context.getApplicationContext()).getCompositionRoot().getNeuraHelper();
        }
        return null;
    }
}
