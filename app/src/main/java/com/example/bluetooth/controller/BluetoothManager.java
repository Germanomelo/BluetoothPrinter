package com.example.bluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;

    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread thread;

    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    public boolean connect(String address) {
        boolean status = find(address);

        if (status) {
            try {
                openConnection();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                disconnect();
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean find(String address) {
        boolean statusConnection = false;
        if (!isConnected()) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            ArrayList<BluetoothDevice> mArrayAdapter;
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    if (device.getAddress().equals(address)) {
                        bluetoothDevice = device;
                    }
                }
            }

            statusConnection = BluetoothAdapter.checkBluetoothAddress(address);
            return statusConnection;
        }
        return false;

    }

    public boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    private void openConnection() throws IOException {
        //Standard uuid from string //
        UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
        bluetoothSocket.connect();
        outputStream = bluetoothSocket.getOutputStream();
        inputStream = bluetoothSocket.getInputStream();

        initThreadConnected();
    }

    private void initThreadConnected() {

        final Handler handler = new Handler();
        final byte delimiter = 10;
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int byteAvailable = inputStream.available();
                        if (byteAvailable > 0) {
                            byte[] packetByte = new byte[byteAvailable];
                            inputStream.read(packetByte);

                            for (int i = 0; i < byteAvailable; i++) {
                                byte b = packetByte[i];
                                if (b == delimiter) {
                                    byte[] encodedByte = new byte[readBufferPosition];
                                    System.arraycopy(
                                            readBuffer, 0,
                                            encodedByte, 0,
                                            encodedByte.length
                                    );
                                    final String data = new String(encodedByte, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        stopWorker = true;
                    }
                }

            }
        });

        thread.start();
    }

    public void setConfig(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendData(byte[] bytes) {
        try {
            outputStream.write(bytes);
            outputStream.write("\n".getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write("\n".getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void jumpLine(int line) {
        try {
            for(int i = 0; i < line; i++ ) {
                outputStream.write("\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            stopWorker = true;
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
