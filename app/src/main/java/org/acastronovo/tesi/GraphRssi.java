package org.acastronovo.tesi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.jjoe64.graphview.CursorMode;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;

/**
 *@author Cristian D'Ortona / Andrea Castronovo / Alberto Iantorni
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */


public class GraphRssi extends AppCompatActivity {

    static final String TAG = "GraphRssi";

    //Toolbar
    Toolbar toolbar;

    //Graph
    LineGraphSeries<DataPoint> series;
    double xAxis = 0d;
    Handler mHandler;

    //BLE
    BluetoothManager manager;
    BluetoothAdapter adapter;
    //I need to instantiate a scanner object which I'll use to start a new scan
    //The phone will constantly scan for nearby devices with the sole purpose of gathering RSSI info of the selected device
    BluetoothLeScanner bluetoothLeScanner;
    String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_rssi);

        //Toolbar
        toolbar = findViewById(R.id.toolbar_graph_rssi);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Graph
        mHandler = new Handler();
        GraphView graph = findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        series.setAnimated(true);
        series.setDrawBackground(true);
        series.setColor(Color.BLUE);
        series.setBackgroundColor(R.color.design_default_color_primary);
        graph.addSeries(series);

        GridLabelRenderer gridLabelRenderer = graph.getGridLabelRenderer();
        gridLabelRenderer.setHighlightZeroLines(false);
        gridLabelRenderer.setVerticalAxisTitle("RSSI(dBm)");
        gridLabelRenderer.setVerticalAxisTitleColor(Color.BLACK);
        gridLabelRenderer.setVerticalLabelsColor(R.color.lightPrimaryColor);
        gridLabelRenderer.setHorizontalLabelsColor(R.color.lightPrimaryColor);
        gridLabelRenderer.setNumVerticalLabels(10);
        gridLabelRenderer.setNumHorizontalLabels(6);
        gridLabelRenderer.setPadding(35);
        gridLabelRenderer.setGridStyle(GridLabelRenderer.GridStyle.BOTH);

        Viewport viewport = graph.getViewport();
        viewport.setScrollable(true);
        viewport.setScalable(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(10);
        viewport.setMinY(0);

        //BLE
        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        Intent intent = getIntent();
        deviceAddress = intent.getStringExtra(StaticResources.EXTRA_CHOOSEN_ADDRESS);
        bluetoothLeScanner = adapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(leScanCallBack);
    }

    //callBack for the scanning process
    ScanCallback leScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            valueToGraph(new DevicesScannedModel(deviceAddress, result.getRssi()));
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.w(TAG, "Scan failed with errorCode: " + errorCode);
        }
    };

    void valueToGraph(final DevicesScannedModel scannedDevice){
        Log.d(TAG, "Result from scan: " + scannedDevice.getBleAddress() + ", chosen address: " + deviceAddress);
        if(scannedDevice.getBleAddress().equals(deviceAddress)) {
            Log.d(TAG, "Device found: " + scannedDevice.getBleAddress() + ", updating RSSI");
            //anonymous inner class with interface, the anonymous class implements the abstract class Runnable and has to override the abstract method run
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    xAxis += 1d;
                    series.appendData(new DataPoint(xAxis, scannedDevice.getRssi()), true, 100);
                }
            }, 2500);
        }
    }

    //Toolbar
    public boolean onSupportNavigateUp() {
        //this is called when the activity detects the user pressed the up button in the toolbar
        onBackPressed();
        return true;
    }

    //I'm overriding this since I want to stop the scan as the user navigates up
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        bluetoothLeScanner.stopScan(leScanCallBack);
    }
}
