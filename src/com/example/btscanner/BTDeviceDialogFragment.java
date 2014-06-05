package com.example.btscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;

public class BTDeviceDialogFragment extends DialogFragment {
    public static void show(Activity activity, BluetoothDevice device) {
        BTDeviceDialogFragment frag = new BTDeviceDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("device", device);
        frag.setArguments(args);
        frag.setCancelable(true);
        frag.show(activity.getFragmentManager(), "BTDeviceDialogFragment");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        BluetoothDevice device = getArguments().getParcelable("device");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.bt_device_dialog_title);
        String message = "Name: " + device.getName() + "\nPaired: "
                + (device.getBondState() == BluetoothDevice.BOND_BONDED ? "Yes" : "No")
                + "\nAddress: " + device.getAddress();
        builder.setMessage(message);
        builder.setPositiveButton(R.string.dialog_ok_button_label,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
        return builder.create();
    }
}
