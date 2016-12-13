package com.neura.sampleapplication;

import android.content.Context;
import android.widget.Toast;

import com.neura.android.statealert.SensorsManager;
import com.neura.resources.sensors.SensorType;
import com.neura.standalonesdk.util.NeuraStateAlertReceiver;

public class HandleNeuraStateAlertReceiver extends NeuraStateAlertReceiver {

    @Override
    public void onDetectedMissingPermission(Context context, String permission) {
        Toast.makeText(context, "Neura detected mission permission : " + permission, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetectedMissingPermissionAfterUserPressedNeverAskAgain(Context context, String permission) {
        Toast.makeText(context, "Neura detected mission permission BUT user already pressed 'Never ask again': "
                + permission, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorStateChanged(Context context, SensorType sensorType, boolean isEnabled) {
        Toast.makeText(context, "Neura detected that " + SensorsManager.getInstance().getSensorName(sensorType) +
                " sensor is " + (isEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        /**
         * If the sensor is disabled, you may open the settings with an intent, in an activity's context :
         */
        //if (!isEnabled)
        //startActivityForResult(new Intent(SensorsManager.getInstance().getSensorAction(sensorType), REQUEST_CODE));
    }
}
