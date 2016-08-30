package com.neura.sampleapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.neura.sampleapplication.R;
import com.neura.sampleapplication.fragments.FragmentSubscribe;

import java.util.ArrayList;

public class PermissionsAdapterDisplay extends ArrayAdapter<FragmentSubscribe.PermissionStatus> {

    private ISwitchChangeListener mListener;

    public PermissionsAdapterDisplay(Context context, int resource,
                                     ArrayList<FragmentSubscribe.PermissionStatus> permissions,
                                     ISwitchChangeListener listener) {
        super(context, resource, permissions);
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        FragmentSubscribe.PermissionStatus permission = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.permission_item, parent, false);
            viewHolder.mEventName = (TextView) convertView.findViewById(R.id.permission_event);
            viewHolder.mSwitch = (Switch) convertView.findViewById(R.id.permission_switch);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mSwitch.setChecked(permission.isEnabled());

        viewHolder.mEventName.setText(permission.getPermission().getDisplayName());

        boolean isActive = permission.getPermission().isActive();
        viewHolder.mEventName.setTextColor(getContext().getResources().getColor(isActive ? R.color.light_black : R.color.grey));
        viewHolder.mSwitch.setEnabled(isActive);
        viewHolder.mSwitch.setClickable(isActive);
        viewHolder.mSwitch.setOnClickListener(isActive ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getItem(position).setEnabled(viewHolder.mSwitch.isChecked());
                //Distinguishing between changing the switch's state programmatically and manually
                //by the user, since we want to call the callback only after a user action,
                //and not after viewHolder.mSwitch.setChecked (Init the switch's state above).
                if (v.isShown())
                    mListener.onSwitchChange(getItem(position).getPermission().getName(), viewHolder.mSwitch.isChecked());
            }
        } : null);

        return convertView;
    }

    private static final class ViewHolder {
        TextView mEventName;
        Switch mSwitch;
    }

    public interface ISwitchChangeListener {
        void onSwitchChange(String eventName, boolean isEnabled);
    }

}
