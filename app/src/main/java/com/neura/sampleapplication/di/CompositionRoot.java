package com.neura.sampleapplication.di;

import android.content.Context;

import com.neura.sampleapplication.NeuraHelper;

public class CompositionRoot {

    private NeuraHelper mNeuraHelper;

    public CompositionRoot(Context context) {
        mNeuraHelper = new NeuraHelper(context);
    }

    /**
     * Returns a {@link NeuraHelper} instance
     * @return
     */
    public NeuraHelper getNeuraHelper(){
        return mNeuraHelper;
    }
}
