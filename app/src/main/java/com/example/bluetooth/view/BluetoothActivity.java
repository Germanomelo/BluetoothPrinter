package com.example.bluetooth.view;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.R;
import com.example.bluetooth.controller.utils.ConstraintSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BLUETOOTH = 6;

    boolean statusBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        init();
    }

    private void init() {
        setResult(Activity.RESULT_CANCELED, null);
        statusBluetooth = false;

        // instanciar bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            toast("Bluetooth está desligado");
            statusBluetooth = false;
        } else {
            statusBluetooth = true;
        }

        // verificar se está ativo
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            statusBluetooth = false;
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BLUETOOTH);
        } else {
            statusBluetooth = true;
        }

        // listar dispositivos
        if (statusBluetooth) {
            List<BluetoothDevice> devicesAdapter;
           devicesAdapter = new ArrayList<BluetoothDevice>();

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    devicesAdapter.add(device);
                }
            }

            final RecyclerView mRecyclerView =  findViewById(R.id.devicesRecycler);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(new DevicesBluetoorhAdapter(getApplicationContext(), devicesAdapter));

            LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
            llm.setOrientation(RecyclerView.VERTICAL);
            mRecyclerView.setLayoutManager(llm);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                toast("Bluetooth ligado :D");
                statusBluetooth = true;
                init();
            } else {
                toast("Bluetooth desligado :(");
                statusBluetooth = false;
            }
        }
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private class DevicesBluetoorhAdapter extends RecyclerView.Adapter<DevicesBluetoorhAdapter.MyViewHolder> {
        private LayoutInflater mLayoutInflater;
        private List<BluetoothDevice> devices;

        private DevicesBluetoorhAdapter(Context c, List<BluetoothDevice> devices) {
            mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.devices = devices;
        }

        @NonNull
        @Override
        public DevicesBluetoorhAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = mLayoutInflater.inflate(R.layout.item_bluetooth, parent, false);
            return new DevicesBluetoorhAdapter.MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DevicesBluetoorhAdapter.MyViewHolder holder, int position) {
            holder.nameTextView.setText(devices.get(position).getName());
            holder.addressTextView.setText(devices.get(position).getAddress());
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView nameTextView;
            private TextView addressTextView;

            private MyViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                this.nameTextView = itemView.findViewById(R.id.name);
                this.addressTextView = itemView.findViewById(R.id.address);
            }

            @Override
            public void onClick(View v) {
                if (BluetoothAdapter.checkBluetoothAddress(devices.get(getAdapterPosition()).getAddress())) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(ConstraintSystem.INTENT_CODE_ADDRESS, devices.get(getAdapterPosition()).getAddress());
                    setResult(Activity.RESULT_OK, returnIntent);
                }
                onBackPressed();
            }
        }
    }
}
