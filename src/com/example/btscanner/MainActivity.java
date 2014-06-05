package com.example.btscanner;

import java.util.ArrayList;

import com.example.btscanner.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";

    private ArrayList<BluetoothDevice> devList = null;
    private final static String DEVLIST_KEY = "com.example.btscanner.MainActivity.devList";

    private TextView scanningStatusView;
    private ProgressBar scanningProgress;
    private ListView devListView;
    private BTDevListAdapter devListAdapter;
    private Button startStopButton;

    private BluetoothAdapter btAdapter = null;
    private BroadcastReceiver btSearchReceiver = null;

    private final static int REQUEST_ENABLE_BT = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
            devList = savedInstanceState.getParcelableArrayList(DEVLIST_KEY);
        if (devList == null)
            devList = new ArrayList<BluetoothDevice>();

        setupUI();
        setupBT();
    }

    private void setupUI() {
        scanningStatusView = (TextView) findViewById(R.id.scanning_status);
        
        scanningProgress = (ProgressBar)findViewById(R.id.scanning_progress);
        scanningProgress.setIndeterminate(false);

        devListView = (ListView) findViewById(R.id.dev_list_view);
        devListAdapter = new BTDevListAdapter(this, android.R.layout.simple_list_item_2, devList);
        devListView.setAdapter(devListAdapter);
        devListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                BluetoothDevice dev = devList.get(pos);
                Log.i(TAG, "onItemClick: dev=" + dev);
                BTDeviceDialogFragment.show(MainActivity.this, dev);
            }
        });
        devListView.smoothScrollToPosition(devListAdapter.getCount());

        startStopButton = (Button) findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btAdapter.isDiscovering())
                    btAdapter.cancelDiscovery();
                else {
                    devListAdapter.clear();
                    btAdapter.startDiscovery();
                }
            }
        });
    }

    private void setupBT() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
            FatalErrorDialogFragment.show(this, R.string.no_bt_alert_message);
        else if (!btAdapter.isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT);
        else
            setupBTReceiver();
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
        case REQUEST_ENABLE_BT:
            if (resCode == Activity.RESULT_OK)
                setupBTReceiver();
            else
                FatalErrorDialogFragment.show(this, R.string.bt_needed_alert_message);
            break;
        }
    }

    private void setupBTReceiver() {
        btSearchReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(TAG, "onReceive: " + action);
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devListAdapter.add(device);
                    devListAdapter.notifyDataSetChanged();
                    devListView.smoothScrollToPosition(devListAdapter.getCount());
                }
                else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    scanningProgress.setIndeterminate(true);
                    scanningStatusView.setText(R.string.status_scanning);
                    startStopButton.setText(R.string.stop_scanning_button_label);
                }
                else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                    scanningProgress.setIndeterminate(false);
                    scanningStatusView.setText(R.string.status_stopped_scanning);
                    startStopButton.setText(R.string.start_scanning_button_label);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btSearchReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (btAdapter != null)
            btAdapter.cancelDiscovery();
        if (btSearchReceiver != null)
            unregisterReceiver(btSearchReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DEVLIST_KEY, devList);
    }
}
