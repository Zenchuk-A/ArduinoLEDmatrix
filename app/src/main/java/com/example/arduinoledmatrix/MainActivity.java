package com.example.arduinoledmatrix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import com.example.arduinoledmatrix.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;

import yuku.ambilwarna.AmbilWarnaDialog;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ArduinoLEDmatrix";
    private ConstraintLayout linearLayout;
    private final int LEDcnt = 16;
    private boolean getFL = false;
    private int initialColor = Color.parseColor("#FF0000");
    private final int[] btnClrArr = new int[LEDcnt*LEDcnt];
    private final ImageView[][] btn = new ImageView[LEDcnt][LEDcnt];
    Button btnColor;
    Button btnDropColor;
    Button btnGetColor;
    ImageView curClrV;
    Switch swchSend;
    Button setMatrix;
    TextView tvDbg;
    Button btnA;
    Button btnB;
    Button btnC;
    Button btnD;
    Button btnE;
    Button btnF;
    static final int REQUEST_ENABLE_BT=1;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice curBTdevice;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;

    // SPP UUID сервиса
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-адрес Bluetooth модуля
    private static String address = "00:00:00:00:00:00";//"98:D3:61:F9:3E:F9";

    @Override
    public void onResume() {
        super.onResume();
        connectBTdevice();
        Log.d(TAG, "onResume - попытка соединения");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            //errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        bluetoothAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "Соединяемся");
        try {
            btSocket.connect();
            Log.d(TAG, "Соединение установлено и готово к передачи данных");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "Создание Socket");

        try {
            outStream = btSocket.getOutputStream();
            inStream = btSocket.getInputStream();
        } catch (IOException e) {
            //errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = (ConstraintLayout) findViewById(R.id.linearLayout);
        btnColor = (Button) findViewById(R.id.btnColor);
        btnDropColor = (Button) findViewById(R.id.btnDropColor);
        btnGetColor = (Button) findViewById(R.id.btnGetColor);
        curClrV = (ImageView) findViewById(R.id.curClrV);
        curClrV.setBackgroundColor(initialColor);
        swchSend = (Switch) findViewById(R.id.swchSend);
        setMatrix = (Button) findViewById(R.id.setMatrix);
        tvDbg = (TextView) findViewById(R.id.tvDbg);
        btnA = (Button) findViewById(R.id.btnA);
        btnB = (Button) findViewById(R.id.btnB);
        btnC = (Button) findViewById(R.id.btnC);
        btnD = (Button) findViewById(R.id.btnD);
        btnE = (Button) findViewById(R.id.btnE);
        btnF = (Button) findViewById(R.id.btnF);
        Display display = getWindowManager().getDefaultDisplay();
        Arrays.fill(btnClrArr,Color.parseColor("#C0C0C0"));
        Point size = new Point();
        display.getSize(size);
        int btnWidth = (size.x - 60)/LEDcnt;

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(false);
            }

        });

        btnDropColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialColor = Color.parseColor("#C0C0C0");
                curClrV.setBackgroundColor(initialColor);
            }
        });

        btnDropColor.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int tmpColor = Color.parseColor("#C0C0C0");
                for (int i = 0; i < LEDcnt; i++) {
                    for (int j = 0; j < LEDcnt; j++) {
                        btn[i][j].setBackgroundColor(tmpColor);
                        btnClrArr[i*LEDcnt + j] = tmpColor;
                    }
                }
                send2BT("g");
                return true;
            }
        });

        btnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resStr="";
                send2BT("a");
                clearData();
                btnA.setEnabled(false);
                do{resStr = readData();}while (resStr == "");
                tvDbg.setText(resStr);
                btnA.setEnabled(true);
            }
        });

        btnB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resStr="";
                send2BT("b");
                clearData();
                btnB.setEnabled(false);
                do{
                    resStr = readData();
                }while (resStr == "");
                tvDbg.setText(resStr);
                btnB.setEnabled(true);
            }
        });

        btnC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resStr="";
                send2BT("c");
                clearData();
                btnC.setEnabled(false);
                do{resStr = readData();}while (resStr == "");
                tvDbg.setText(resStr);
                btnC.setEnabled(true);
            }
        });

        btnD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send2BT("d");
                clearData();
            }
        });

        btnE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send2BT("e");
                clearData();
            }
        });

        btnF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send2BT("f");
                clearData();
            }
        });

        btnGetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curClrV.setBackgroundColor(Color.alpha(0));
                getFL = true;
                btnColor.setEnabled(false);
                btnDropColor.setEnabled(false);
                setMatrix.setEnabled(false);
                swchSend.setEnabled(false);
            }
        });

        swchSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setMatrix.setEnabled(false);
                }else{
                    setMatrix.setEnabled(true);
                }
            }
        });

        ColorButton[] Cbtn = new ColorButton[LEDcnt*LEDcnt];
        for (int i = 0; i < LEDcnt; i++) {
            for (int j = 0; j < LEDcnt; j++) {
                btn[i][j]= new ImageView(this);
                btn[i][j].setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
                btn[i][j].setMinimumWidth(btnWidth);
                btn[i][j].setMinimumHeight(btnWidth);
                btn[i][j].setY(i * (btnWidth + 3) + 45);
                btn[i][j].setX(j * (btnWidth + 3) + 20);
                btn[i][j].setBackgroundColor(Color.parseColor("#C0C0C0"));//Comment for debug
                //btn[i][j].setBackgroundColor(Color.parseColor("#F0F0F0"));//Uncomment for debug
                int id = i*LEDcnt + j + 1;//View.generateViewId();
                btn[i][j].setId(id);
                btn[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getFL){
                            ColorDrawable drw = (ColorDrawable) v.getBackground();
                            initialColor = drw.getColor();
                            curClrV.setBackgroundColor(initialColor);
                            btnColor.setEnabled(true);
                            btnDropColor.setEnabled(true);
                            setMatrix.setEnabled(true);
                            swchSend.setEnabled(true);
                            getFL = false;

                        }else{
                            v.setBackgroundColor(initialColor);
                            int curID = v.getId();
                            btnClrArr[curID-1] = initialColor;
                            if(swchSend.isChecked()){
                                try{
                                    int mls = 40;
                                    int rowX =(int) Math.floor((curID-1)/LEDcnt);//+ 1;
                                    int colY = (curID%LEDcnt);
                                    int red = 0;
                                    int green = 0;
                                    int blue = 0;

                                    if(initialColor != Color.parseColor("#C0C0C0")){
                                        //int alpha = (initialColor >> 24) & 0xff;
                                        red = (initialColor >> 16) & 0xff;
                                        green = (initialColor >> 8) & 0xff;
                                        blue = (initialColor) & 0xff;
                                    }
                                    String outStr="";
                                    if(colY == 0){colY=16;}
                                    colY--;
                                    outStr=String.format("%02d",rowX)+"-"+String.format("%02d",colY)+"-"+
                                           String.format("%03d",red)+"-"+String.format("%03d",green)+"-"+
                                           String.format("%03d",blue);
                                    tvDbg.setText(outStr);
                                    send2BT(String.format("%02d",rowX));
                                    Thread.sleep(mls);
                                    send2BT(String.format("%02d",colY));
                                    Thread.sleep(mls);
                                    send2BT(String.format("%03d",red));
                                    Thread.sleep(mls);
                                    send2BT(String.format("%03d",green));
                                    Thread.sleep(mls);
                                    send2BT(String.format("%03d",blue));/**/
                                }catch(Exception e){
                                    tvDbg.setText(e.getMessage());
                                }
                            }
                        }
                    }
                });
                // слой, к которому кнопку хотите прикрепить
                linearLayout.addView(btn[i][j]);
            }
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        // Register for broadcasts when a device is discovered.
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter1);

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            setMatrix.setEnabled(false);
            swchSend.setEnabled(false);
        }else{
            if (!bluetoothAdapter.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),REQUEST_ENABLE_BT);
            }else{
                setMatrix.setEnabled(true);
                swchSend.setEnabled(true);
                connectBTdevice();
            }
        }
    }

    private void connectBTdevice(){
        tvDbg.setText("");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        curBTdevice = null;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                curBTdevice = device;
                String BTname = device.getName();
                String BTaddr = device.getAddress();
                if(BTname.equals("HC-06")){
                    address=BTaddr;
                    tvDbg.setText("HC-06 - "+BTaddr);
                }
            }
        }else{
            //Here we need to find bluetooth devices around our device
        }
    };

    private void send2BT(String inStr){
        if(curBTdevice!=null){
            sendData(inStr);
        } else {
            tvDbg.setText("I can not send data!");
        }

    }
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                setMatrix.setEnabled(true);
                swchSend.setEnabled(true);
            }else
            if(resultCode == RESULT_CANCELED){
                setMatrix.setEnabled(false);
                swchSend.setEnabled(false);
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        tvDbg.setText("Bluetooth off");
                        setMatrix.setEnabled(false);
                        swchSend.setEnabled(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        tvDbg.setText("Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        tvDbg.setText("Bluetooth on");
                        setMatrix.setEnabled(true);
                        swchSend.setEnabled(true);
                        connectBTdevice();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        tvDbg.setText("Turning Bluetooth on...");
                        break;
                }
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                tvDbg.setText(deviceName+" - "+deviceHardwareAddress+";");
            }
        }
    };

    private void openDialog(boolean supportAlpha){
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, initialColor, supportAlpha,new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
                initialColor = color;
                curClrV.setBackgroundColor(initialColor);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        dialog.show();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "Посылаем данные: " + message + ";");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            tvDbg.setText(e.getMessage());
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nВ переменной address у вас прописан 00:00:00:00:00:00, вам необходимо прописать реальный MAC-адрес Bluetooth модуля";
            msg = msg +  ".\n\nПроверьте поддержку SPP UUID: " + MY_UUID.toString() + " на Bluetooth модуле, к которому вы подключаетесь.\n\n";

            //errorExit("Fatal Error", msg);
        }
    }

    private String readData() {
        byte[] buffer = new byte[256];  // buffer store for the stream
        int bytes; // bytes returned from read()
        String outStr="";
        try {
            // Read from the InputStream
            bytes = inStream.read(buffer);        // Получаем кол-во байт и само собщение в байтовый массив "buffer"
            //tvDbg.setText(bytes);
            Log.d(TAG, "Получили: " + bytes + ";");
            for(int rj=0; rj < bytes; rj++) {
                if((buffer[rj] != 10)&(buffer[rj] != 13)) {
                    Log.d(TAG, "Получили: " + (char)buffer[rj] + ";");
                    outStr += (char)buffer[rj];
                }
            }

        } catch (IOException e) {
            Log.d(TAG, "Ошибочка вышла: " + e.getMessage() + ";");
            //break;
            outStr = "Error";
        }
        return outStr;
    }

    private void clearData() {
        byte[] buffer = new byte[256];  // buffer store for the stream
        int bytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs
        for(int ci=0; ci<2; ci++) {
            try {
                // Read from the InputStream
                bytes = inStream.read(buffer);        // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                //tvDbg.setText(bytes);
                Log.d(TAG, "Было: " + bytes + ";");
                for (int rj = 0; rj < bytes; rj++) {
                    if ((buffer[rj] != 10) & (buffer[rj] != 13)) {
                        Log.d(TAG, "Было: " + (char) buffer[rj] + ";");
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "Ошибочка вышла: " + e.getMessage() + ";");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /* ... */

        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}
