package org.acastronovo.tesi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class ScanningActivity extends AppCompatActivity {

    private String TAG = "ScanningActivity";

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_FINE_LOCATION = 2;

    BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning = false;
    ArrayList<DevicesScannedModel> devicesScannedList;

    //this end the scan every 10 seconds
    //it's very important as in a LE application we want to reduce battery-intensive tasks
    private static final long SCAN_PERIOD = 5000;

    //noBlueooth Icon
    ImageView noBluetoothIcon;

    //ListView
    CustomAdapterView customAdapter;

    //Toolbar
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI setup
        noBluetoothIcon = findViewById(R.id.noBluetooth);

        //initialize bluetooth adapter
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //initialize scan variables
        devicesScannedList = new ArrayList<>();

        checkBleStatus();
        grantLocationPermissions();

        //ListView used to print on screen a list of all the scanned devices
        customAdapter = new CustomAdapterView(this, R.layout.adapter_view_layout, devicesScannedList);
        ListView listView = findViewById(R.id.list_view);
        //this is what happens whenever an item of the ListView is pressed
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final Intent intent = new Intent(ScanningActivity.this, SensorsInfo.class);
                intent.putExtra(StaticResources.EXTRA_CHOOSEN_ADDRESS, devicesScannedList.get(position).getBleAddress());
                intent.putExtra(StaticResources.EXTRA_CHOOSEN_NAME, devicesScannedList.get(position).getDeviceName());
                setResult(StaticResources.REQUEST_CODE_SCAN_ACTIVITY, intent);
                finish();

            }
        });
        listView.setAdapter(customAdapter);

        //Toolbar
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    //this lets me add the menu resource to the toolbar
    //from api 11 and higher items from the option menu are automatically added to the toolbar if present
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu_main, menu);
        return true;
    }

    public boolean onSupportNavigateUp() {
        //this is called when the activity detects the user pressed the back button
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(bluetoothLeScanner != null)
            stopScan();
    }

    //this method is called whenever the user select an item from the toolbar menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_scan):
                //once the system calls onCreateOptionMenu, the menu won't be changed from the one specified
                //in order to change it at run-time I call the following method
                //whose response is handled by onPrepareOptionsMenu
                invalidateOptionsMenu();
                startScan();
                return true;
            case (R.id.action_stopScan):
                invalidateOptionsMenu();
                stopScan();
                return true;
            case (R.id.action_adapter_info):
                Intent adapterInfo = new Intent(ScanningActivity.this, AdapterInfo.class);
                startActivity(adapterInfo);
                return true;
            default:
                //item selected not recognized
                Log.e(TAG, "error, selected item not recognized");
                return super.onOptionsItemSelected(item);
        }
    }

    //this callBack takes as parameter the menu that was initialized
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       if(scanning) {
           menu.findItem(R.id.action_scan).setVisible(false);
           menu.findItem(R.id.action_stopScan).setVisible(true);
           return true;
       }
       else if(!scanning){
           menu.findItem(R.id.action_scan).setVisible(true);
           menu.findItem(R.id.action_stopScan).setVisible(false);
           return true;
       }
       return super.onPrepareOptionsMenu(menu);
    }


    @Override
    protected  void onResume(){
        super.onResume();

        //this checks on whether the BLE adapter is integrated in the device or not
        //the app won't work with classic Bluetooth
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //method used to check on whether the BLE adapter is enabled or not
    //if it's not enabled then a new implicit intent, whose action is a BLE activation request, is instantiated
    //this'll prompt a window where the user'll be able to either agree or not on the activation of the BLE
    //the REQUEST_ENABLE_BT will be returned in OoActivityResult() if greater than 0
    private void checkBleStatus(){
        if(bluetoothAdapter == null){
            Log.w(TAG, "device has no Bluetooth radio");
        } else {
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult throws the following exception
                try{
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Log.d("request to turn BLE on", "Request to turn BLE sent");
                } catch (ActivityNotFoundException e){
                    Log.e("request to turn BLE on", "the activity wasn't found");
                }
            }
        }
    }

    //in order to use BLE I have to make sure fine_location permissions are enabled
    //it's considered a dangerous permission so I have to ask for it at run-time
    private void grantLocationPermissions(){
        if(!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            Log.d("request fine_location", "request sent");
            //add builder eventually
        }
    }

    //callback method to catch the result of startActivityForResult
    //this method receives the response of the user if the activity that was launched exists
    //The requestCode used to lunch the activity as well as the resultCode are returned, the requestCode is used to identify the who this result come from
    //The resultCode will be RESULT_CANCELED if the activity explicitly returned that, didn't return any result, or crashed during its operation
    protected void onActivityResult(int requestCode, int resultCode, Intent data ){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                Toast.makeText(this,"Bluetooth Enabled", Toast.LENGTH_LONG).show();
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this,"Bluetooth wasn't enabled", Toast.LENGTH_LONG).show();
                //I disable the bluetooth as stated by the user
            }
        }
    }

    //this callback method is called to catch the result of the method requestPermissions launched in order to grand fine_location permissions
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location services enabled", Toast.LENGTH_SHORT).show();
                Log.d("result location request", "fine location has been granted");
            } else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(this, "Location services must be enabled to use the app", Toast.LENGTH_LONG).show();
                Log.d("result location request", "fine location wasn't granted");
                finish();
            }
        }
    }

    //this starts the scan of the BLE advertiser nearby
    public void startScan(){
        //I declare the variable here so it doesn't crash when I start the scan after asking the user to enable the bluetooth
        //If I declared it in the onCreate, it'd be null if the user didn't activate the bluetooth beforehand
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if(!scanning && bluetoothAdapter.isEnabled()) {


            noBluetoothIcon.setVisibility(View.INVISIBLE);

            Toast.makeText(this, "Scanning for nearby devices", Toast.LENGTH_SHORT ).show();

            //this is used to flush the entries in the viewList whenever a new scan occurs
            customAdapter.clear();

            //the handler is used to schedule an event to happen at some point in the future
            //in this case the method postDelayed causes the runnable to be added to the message queue
            // and it will be executed(run) after a given time SCAN_PERIOD
            //Handler(Runnable runnable, long delay)
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    //this's going to stop the scan if the user doesn't stop it manually before SCAN_PERIOD
                    stopScan();
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            scanning = true;

            //filter for the scan activity
            UUID[] serviceUUIDs = new UUID[]{UUID.fromString(StaticResources.ESP32_BME280_SERVICE)};
            List<ScanFilter> filters = null;
            filters = new ArrayList<>();
            for (UUID serviceUUID : serviceUUIDs) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid(new ParcelUuid(serviceUUID))
                        .build();
                filters.add(filter);
            }
            //settings for the scan activity
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                    .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT)
                    .setReportDelay(0)
                    .build();
            //this is executed during the x time before the thread is activated
            bluetoothLeScanner.startScan(filters, scanSettings, leScanCallBack);
            Log.d("startScan", "scan has started");
        }
    }

    //this stops the scan before the time elapses
    // if the button is pressed
    public void stopScan(){
        scanning = false;
        bluetoothLeScanner.stopScan(leScanCallBack);
        Log.d("stopScan", "Scanner stopped");
    }

    //callBack method used to catch the result of startScan()
    //this is an abstract class
    ScanCallback leScanCallBack = new ScanCallback() {
        @Override
        //ScanResult is the result of the BLE scan
        //its method getDevice() permits to retrieve a BluetoothDevice object
        // which I use to gain info about the BLE advertisers scanned
        //callBackType determines how this callback was triggered
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            customAdapter.add(new DevicesScannedModel(device.getName(), device.getAddress(), result.getRssi(), device.getBondState()));
            customAdapter.notifyDataSetChanged();
            Log.d("onScanResult", "Device name: " + device.getName());
        }
        public void onScanFailed(int errorCode) {
            Toast.makeText(getBaseContext(), "scan has failed, please retry again", Toast.LENGTH_SHORT).show();
            Log.e("leScanCallBack" ,"scan call back has failed with errorCode: " + errorCode);
        }
    };
}