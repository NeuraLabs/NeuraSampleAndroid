package com.neura.sampleapplication.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neura.resources.authentication.AnonymousAuthenticationStateListener;
import com.neura.resources.authentication.AuthenticationState;
import com.neura.resources.user.UserDetails;
import com.neura.resources.user.UserDetailsCallbacks;
import com.neura.sampleapplication.NeuraManager;
import com.neura.sampleapplication.R;

public class FragmentMain extends BaseFragment {

    public Button mRequestPermissions;
    private Button mDisconnect;

    private ImageView mSymbolTop;
    private ImageView mSymbolBottom;
    private TextView mNeuraStatus;

    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestLocationPermission();

        mSymbolTop = (ImageView) view.findViewById(R.id.neura_symbol_top);
        mSymbolBottom = (ImageView) view.findViewById(R.id.neura_symbol_bottom);

        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mRequestPermissions = (Button) view.findViewById(R.id.request_permissions_btn);
        mDisconnect = (Button) view.findViewById(R.id.disconnect);
        mNeuraStatus = (TextView) view.findViewById(R.id.neura_status);

        mSymbolTop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setTopSymbol(NeuraManager.getInstance().getClient().isLoggedIn());
                mSymbolTop.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        mSymbolBottom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setBottomSymbol(NeuraManager.getInstance().getClient().isLoggedIn());
                mSymbolBottom.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        setUIState(NeuraManager.getInstance().getClient().isLoggedIn());
        loadProgress(false);

        ((TextView) view.findViewById(R.id.version)).
                setText("Sdk Version : " + NeuraManager.getInstance().getClient().getSdkVersion());
    }

    //create a call back to handle authentication stages.
    AnonymousAuthenticationStateListener silentStateListener = new AnonymousAuthenticationStateListener() {
        @Override
        public void onStateChanged(AuthenticationState state) {
            switch (state) {
                case AccessTokenRequested:
                    loadProgress(true);
                    break;
                case AuthenticatedAnonymously:
                    // successful authentication
                    NeuraManager.getInstance().getClient().unregisterAuthStateListener();
                    // do something with the user's details...
                    getUserDetails();
                    // Trigger UI changes
                    setUIState(true);

                    break;
                case NotAuthenticated:
                case FailedReceivingAccessToken:
                    // Authentication failed indefinitely. a good opportunity to retry the authentication flow
                    NeuraManager.getInstance().getClient().unregisterAuthStateListener();

                    // Trigger UI changes
                    setUIState(false);
                    break;
                default:
            }
        }
    };

    private void requestLocationPermission(){
        if (ActivityCompat.checkSelfPermission(getActivity(),
            android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION}, 1111);
            return;
        }
        else{
            // Make sure the user enables location,
            // this is needed to for Neura to work, and is not automatic when using anonymous authentication.
            // Phone based auth asks for it automatically.
        }
    }

    private void getUserDetails() {
        NeuraManager.getInstance().getClient().getUserDetails(new UserDetailsCallbacks() {
            @Override
            public void onSuccess(UserDetails userDetails) {
                if (userDetails.getData() != null) {
                    // Do something with this information
                    userId = userDetails.getData().getNeuraId();
                    NeuraManager.getInstance().getClient().getUserAccessToken();
                }
            }

            @Override
            public void onFailure(Bundle resultData, int errorCode) {
            }
        });
    }

    public void setUIState(final boolean isConnected) {
        setSymbols(isConnected);
        mNeuraStatus.setText(getString(isConnected ? R.string.neura_status_connected : R.string.neura_status_disconnected));
        mNeuraStatus.setTextColor(getResources().getColor(isConnected ? R.color.green_connected : R.color.red_disconnected));
        setEnableOnButtons(isConnected);
        mRequestPermissions.setText(R.string.connect_request_permissions);

        mRequestPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProgress(true);
                NeuraManager.authenticateAnonymously(silentStateListener);
            }
        });

        mDisconnect.setOnClickListener(isConnected ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProgress(true);
                disconnect();
            }
        } : null);
    }

    private void disconnect() {
        setUIState(false);
        System.out.println(NeuraManager.getInstance().getClient().isLoggedIn());
        NeuraManager.getInstance().getClient().forgetMe(getActivity(), true, new android.os.Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.arg1 == 1) {
                    setUIState(NeuraManager.getInstance().getClient().isLoggedIn());

                }
                loadProgress(false);
                return true;
            }
        });
    }

    private void setEnableOnButtons(boolean isConnected) {
        mDisconnect.setEnabled(isConnected);
        mDisconnect.setAlpha(isConnected ? 1 : 0.5f);
        mRequestPermissions.setEnabled(!isConnected);
        mRequestPermissions.setAlpha(isConnected ? 0.5f : 1);
    }

    private void setTopSymbol(boolean isConnected) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSymbolTop.getLayoutParams();
        params.rightMargin = isConnected ? 0 : (int) (mSymbolTop.getWidth() / 1.4);
        mSymbolTop.setLayoutParams(params);
    }

    private void setBottomSymbol(boolean isConnected) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSymbolBottom.getLayoutParams();
        params.leftMargin = isConnected ? 0 : (int) (mSymbolBottom.getWidth() / 1.4);
        mSymbolBottom.setLayoutParams(params);
    }

    private void setSymbols(final boolean isConnected) {
        setTopSymbol(isConnected);
        setBottomSymbol(isConnected);
    }

    @Override
    public void loadProgress(boolean enabled) {
        super.loadProgress(enabled);
        mRequestPermissions.setEnabled(!enabled);
        mDisconnect.setEnabled(!enabled);
    }
}