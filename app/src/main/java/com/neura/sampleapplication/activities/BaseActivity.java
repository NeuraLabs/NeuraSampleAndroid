package com.neura.sampleapplication.activities;

import android.app.Activity;

import com.neura.sampleapplication.NeuraHelper;
import com.neura.sampleapplication.SampleApplication;

public abstract class BaseActivity extends Activity {

    /**
     * @return {link {@link NeuraHelper}} instance.
     */
    protected NeuraHelper getNeuraHelper(){
        return ((SampleApplication)getApplicationContext()).getCompositionRoot().getNeuraHelper();
    }
}
