package org.acastronovo.tesi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class CustomAdapterView extends ArrayAdapter<DevicesScannedModel>{

     Context mContext;
     ArrayList<DevicesScannedModel> deviceList;

     CustomAdapterView(@NonNull Context context, int resource, @NonNull ArrayList<DevicesScannedModel> object) {
        super(context, resource, object);
        mContext = context;
        deviceList = object;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View customView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_view_layout, parent, false);

        TextView customText1 = customView.findViewById(R.id.textView_name);
        TextView customText2 = customView.findViewById(R.id.textView_address);
        TextView customText3 = customView.findViewById(R.id.textView_rssi);
        TextView customText4 = customView.findViewById(R.id.textView_bondInfo);

        DevicesScannedModel device = getItem(position);
        customText1.setText(device.getDeviceName());
        customText1.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
        customText2.setText(device.getBleAddress());
        customText2.setTypeface(Typeface.SANS_SERIF);
        customText3.append(Integer.toString(device.getRssi()));
        customText3.setTypeface(Typeface.SANS_SERIF);
        if(device.getBondState() == 12){
            customText4.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
            customText4.setText("Bonded");
        } else {
            customText4.setText("NOT BONDED");
        }


        return customView;
    }

    @Nullable
    @Override
    public DevicesScannedModel getItem(int position) {
        return deviceList.get(position);
    }
}

