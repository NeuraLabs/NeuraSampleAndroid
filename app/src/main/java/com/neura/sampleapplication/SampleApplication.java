package com.neura.sampleapplication;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.neura.sampleapplication.di.CompositionRoot;

/**
 * Global Application class, here you should instantiate your NeuraApiClient
 * In this Implementation we have used pure dependencies injection - just of the example sake.
 * Feel free to implement your own dependency injection implementation - using Dagger2 for instance.
 */
public class SampleApplication extends Application {

    private CompositionRoot mCompositionRoot;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        // Neura init takes place inside the CompositionRoot
        mCompositionRoot = new CompositionRoot(this);
        try {
            FirebaseApp.getInstance();
        } catch (IllegalStateException ex) {
            FirebaseApp.initializeApp(this, FirebaseOptions.fromResource(this));
        }
    }

    public CompositionRoot getCompositionRoot() {
        return mCompositionRoot;
    }
}
