package com.example.btscanner;

import java.util.ArrayList;

import com.example.btscanner.R;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
    private final static String TAG = "MainActivity";

    private ArrayList<BluetoothDevice> devList = null;
    private final static String DEVLIST_KEY = "com.example.btscanner.MainActivity.devList";

    private ArrayAdapter<BluetoothDevice> devListAdapter;

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

        devListAdapter = new ArrayAdapter<BluetoothDevice>(this, 0, devList) {
            @Override
            public View getView(int pos, View view, ViewGroup parent) {
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
                }
                BluetoothDevice device = getItem(pos);
                String bonded = device.getBondState() == BluetoothDevice.BOND_BONDED ? " *" : "";
                ((TextView) view.findViewById(android.R.id.text2)).setText(device.getAddress());
                ((TextView) view.findViewById(android.R.id.text1)).setText(device.getName()
                        + bonded);
                return view;
            }
        };
        setListAdapter(devListAdapter);

        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        if (btAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_is_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if (!btAdapter.isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT);
        else
            setupBT();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!btAdapter.isDiscovering()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_scan:
            devListAdapter.clear();
            btAdapter.startDiscovery();
            break;
        case R.id.menu_stop:
            btAdapter.cancelDiscovery();
            break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DEVLIST_KEY, devList);
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
        case REQUEST_ENABLE_BT:
            if (resCode == Activity.RESULT_OK)
                setupBT();
            else {
                Toast.makeText(this, R.string.error_bluetooth_must_be_turned_on, Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
            break;
        }
        super.onActivityResult(reqCode, resCode, data);
    }

    protected void onListItemClick(ListView listView, View view, int pos, long id) {
        BluetoothDevice device = devList.get(pos);
        Log.i(TAG, "onListItemClick: device=" + device);
        BTDeviceDialogFragment.show(this, device);
    }

    private void setupBT() {
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
                }
                else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    invalidateOptionsMenu();
                }
                else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                    invalidateOptionsMenu();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btSearchReceiver, filter);
    }

}
