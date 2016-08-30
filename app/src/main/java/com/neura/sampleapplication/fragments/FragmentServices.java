package com.neura.sampleapplication.fragments;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.neura.resources.situation.SituationCallbacks;
import com.neura.resources.situation.SituationData;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;

public class FragmentServices extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_services, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mProgress.setVisibility(View.VISIBLE);

        NeuraManager.getInstance().getClient().getUserSituation(new SituationCallbacks() {
            @Override
            public void onSuccess(SituationData situationData) {
                Log.i(getClass().getSimpleName(), "Situation received successfully : " + situationData.toString());
                ((TextView) getView().findViewById(R.id.situation_results_text)).setMovementMethod(new ScrollingMovementMethod());
                ((TextView) getView().findViewById(R.id.situation_results_text))
                        .setText(situationData.toString());
                //SituationData class overrides to string, so you'll be displayed with the results.
                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Bundle bundle, int i) {
                Log.e(getClass().getSimpleName(), "Failed to receive situation");
                mProgress.setVisibility(View.GONE);
            }
        }, System.currentTimeMillis() - 1000 * 60 * 30);
        //Receiving situation status for 30 minutes ago. fyi - you won't always get followingSituation
        //since if the user hasn't changed its place (stayed at home for the last 30 min,
        //followingSituation will be offline.
    }

}
