package com.example.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bluetooth.controller.BluetoothManager;
import com.example.bluetooth.controller.utils.ConstraintSystem;
import com.example.bluetooth.controller.utils.PrinterCommands;
import com.example.bluetooth.view.BluetoothActivity;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private BluetoothManager mBluetoothFactory;
    private MyViewHolder myViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){
        this.myViewHolder = new MyViewHolder();

        this.myViewHolder.btSend.setEnabled(false);

        this.myViewHolder.btListDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListBluetooth();
            }
        });

        this.myViewHolder.btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothFactory.isConnected()) {
                    String msg = myViewHolder.etMessage.getText().toString();

                    mBluetoothFactory.setConfig(PrinterCommands.getFormatBold());
                    boolean isPrint = mBluetoothFactory.sendData(msg.getBytes());

//                    mBluetoothFactory.setConfig(PrinterCommands.getFormatHeigth());
//                    boolean isPrint = mBluetoothFactory.sendData(msg.getBytes());
//
//                    mBluetoothFactory.setConfig(PrinterCommands.getFormatUnderline());
//                    boolean isPrint = mBluetoothFactory.sendData(msg.getBytes());
//
//                    mBluetoothFactory.setConfig(PrinterCommands.getFormatWidth());
//                    boolean isPrint = mBluetoothFactory.sendData(msg.getBytes());
//
//                    mBluetoothFactory.setConfig(PrinterCommands.getFormatSmall());
//                    boolean isPrint = mBluetoothFactory.sendData(msg.getBytes());

                    mBluetoothFactory.jumpLine(4);

                    if(isPrint){
                        Toast.makeText(getApplicationContext(),"Mensagem enviada com sucesso",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Erro no envio da mensagem",Toast.LENGTH_LONG).show();
                    }
                }else{
                    myViewHolder.btSend.setEnabled(false);
                    Toast.makeText(getApplicationContext(),"Bluetooth não conectado",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void onClickListBluetooth() {
        Intent intent = new Intent(getApplicationContext(), BluetoothActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            this.mBluetoothFactory = new BluetoothManager();
            String address = data.getStringExtra(ConstraintSystem.INTENT_CODE_ADDRESS);
            this.mBluetoothFactory.connect(address);

            if(this.mBluetoothFactory.isConnected()){
                this.myViewHolder.btSend.setEnabled(true);
            }

        } else if (resultCode == RESULT_CANCELED) {
            toast("Conexao cancelada");
        }
    }


    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private class MyViewHolder{
        private Button btListDevices;
        private Button btSend;
        private EditText etMessage;

        MyViewHolder(){
            this.btListDevices = findViewById(R.id.button_list);
            this.btSend = findViewById(R.id.button_send);
            this.etMessage = findViewById(R.id.text_msg);
        }
    }
}
