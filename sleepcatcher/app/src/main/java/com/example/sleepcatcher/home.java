package com.example.sleepcatcher;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;

public class home extends AppCompatActivity {
    private Button btnTest;
    private Button buttonStart;
    private Button buttonStop;
    private TextView sleepText;
    private boolean testClicked = false;
    private static Vibrator v;
    private MediaPlayer r;
    private Uri notification;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private final String DEVICE_ADDRESS="20:15:05:14:17:97";
    boolean deviceConnected=false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnTest = (Button)findViewById(R.id.btnTest);
        buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        sleepText = (TextView)findViewById(R.id.sleepText);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = MediaPlayer.create(getApplicationContext(), notification);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        btnTest.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if(testClicked){
                    turnoff_wakeup();

                } else {
                    wakeup();
                }
            }
        });

    }

    private void wakeup(){
        long[] pattern = {0, 1000};
        r = MediaPlayer.create(getApplicationContext(), notification);
        r.start();
        v.vibrate(pattern, 0);
        sleepText.setText("WAKE UP!");
        btnTest.setText("Stop");
        testClicked = true;
    }

    private void turnoff_wakeup(){
        v.cancel();
        r.stop();
        sleepText.setText("Don't fall asleep!");
        btnTest.setText("BUZZ AND RING");
        testClicked = false;
    }
    public void onClickStart(View view) {
        if(BTinit())
        {
            if(BTconnect())
            {
                deviceConnected=true;
                beginListenForData();
                sleepText.setText("\nConnection Opened!\n");
                buttonStart.setVisibility(View.GONE);
                buttonStop.setVisibility(View.VISIBLE);
            }

        }
    }

    public void onClickStop(View view) throws IOException {
        stopThread = true;
        inputStream.close();
        socket.close();
        deviceConnected=false;
        sleepText.setText("\nConnection Closed!\n");
        buttonStart.setVisibility(View.VISIBLE);
        buttonStop.setVisibility(View.GONE);
    }

    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }
    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }
    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {
                                    sleepText.setText(string);
                                    if(string.equals("0")){
                                        turnoff_wakeup();
                                    } else if(string.equals("1")) {
                                        wakeup();
                                    }
                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }
}
