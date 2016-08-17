package com.neura.sampleapplication.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.neura.resources.situation.SituationCallbacks;
import com.neura.resources.situation.SituationData;
import com.neura.sampleapplication.R;

/**
 * Created by hadas on 01/06/2016.
 */

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

        getMainActivity().getClient().getUserSituation(new SituationCallbacks() {
            @Override
            public void onSuccess(SituationData situationData) {
                Log.i(getClass().getSimpleName(), "Situation received successfully : " + situationData.toString());
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
        }, System.currentTimeMillis() - 1000 * 60 * 10);
        //Receiving situation status for 10 minutes ago. fyi - you won't always get followingSituation
        //since if the user hasn't changed its place (stayed at home for the last 10 min, there
        //won't be any followingSituation.
    }

}
