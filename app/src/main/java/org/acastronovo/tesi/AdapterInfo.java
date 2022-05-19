package org.acastronovo.tesi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.widget.TextView;

import java.util.Objects;

/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class AdapterInfo extends AppCompatActivity {

    //Toolbar
    Toolbar toolbar;

    //UI
    TextView version;
    TextView manufacturer;
    TextView model;
    TextView hasBLE;
    TextView bleAddress;

    //BLE
    BluetoothManager manager;
    BluetoothAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_info);

        //Toolbar
        toolbar = findViewById(R.id.toolbar_device_info);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //BLE
        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        String bleSupport = (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))? "Yes" : "no";
        String bleMac = adapter.getAddress();

        //UI
        version = findViewById(R.id.android_version);
        manufacturer = findViewById(R.id.manufacturer);
        model = findViewById(R.id.model);
        hasBLE = findViewById(R.id.has_ble);
        bleAddress = findViewById(R.id.ble_address);

        version.append(Html.fromHtml("<font color=#757575>" + Build.VERSION.RELEASE + "</font>"));
        manufacturer.append(Html.fromHtml("<font color=#757575>" + Build.MANUFACTURER + "</font>"));
        model.append(Html.fromHtml("<font color=#757575>" + Build.MODEL + "</font>"));
        hasBLE.append(Html.fromHtml("<font color=#757575>" + bleSupport + "</font>"));
        bleAddress.append(Html.fromHtml("<font color=#757575>" + bleMac + "</font>"));


    }

    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }


}
