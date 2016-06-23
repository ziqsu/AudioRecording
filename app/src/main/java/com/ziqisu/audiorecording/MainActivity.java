package com.ziqisu.audiorecording;

//import android.hardware.SensorManager;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.graphics.Color;
import android.widget.EditText;
import android.util.Log;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.content.Context;
import android.os.PowerManager.WakeLock;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AllPermission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.NullPointerException;


public class MainActivity extends AppCompatActivity{

    private AudioRecord mRecorder = null;

    private boolean changecolor = true;

    String inputstring;
    private static final int SampleRate = 16000;
    private static final int Channel = AudioFormat.CHANNEL_IN_MONO;
    private static final int AudioEncode = AudioFormat.ENCODING_PCM_16BIT;
    private Thread recordingThread = null;
    int BufferElements2Rec = 1024;
    int BytesPerElement = 2;
    private boolean isRecording = false;

    //variable need to request permission at runtime
    final private int RequestCodeAskPermission = 124;
    private static final int WritePermission = 0x11;
    private static final int AudioPermission = 0x12;
    private static final int WakePermission = 0x13;
    private static final int AllPermission = WritePermission+AudioPermission+WakePermission;
    private static String[] Permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.RECORD_AUDIO,Manifest.permission.WAKE_LOCK};
    WakeLock wl;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        setupButton();

        boolean granted = true;
        for(String permission: Permissions){
            granted = granted && ContextCompat.checkSelfPermission(this,permission)==
                    PackageManager.PERMISSION_GRANTED;
        }
        if(!granted){
            Log.i("request permission:","request permissions");
            ActivityCompat.requestPermissions(this,Permissions, AllPermission);
        }

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"wakelock");
        wl.acquire();


    }


    protected void onDestroy(){
        super.onDestroy();
        wl.release();
    }


    public void onRequestPermissionsResult(int requestCode, String[] Permissions, int[] grantedResults){
        switch(requestCode){
            case AllPermission:{
                if(grantedResults.length>0
                        && grantedResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantedResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantedResults[2] == PackageManager.PERMISSION_GRANTED){
                    Log.i("activity:","All is granted");
                }else{
                    Log.i("activity:","Access denied");
                }
            }
        }
    }





    private void setupButton() {
        //get a reference to the button
        final Button startbutton = (Button)findViewById(R.id.button);
        final EditText input = (EditText) findViewById(R.id.input);
        //set the click listener to run my code
        startbutton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //onCheckedChanged(startbutton,true);
                        inputstring = input.getText().toString();
                        Log.i("input string is:",inputstring);
                        if(changecolor){
                            try {
                                startRecording();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            changecolor = !changecolor;
                            startbutton.setBackgroundColor(Color.RED);
                            startbutton.setText("Press button to stop recording.");

                        }else{
                            stopRcording();
                            changecolor = !changecolor;
                            startbutton.setBackgroundColor(Color.GREEN);
                            startbutton.setText("Press button to start recording");

                        }
                    }
                }
        );
    }



    private void startRecording() throws IOException {

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SampleRate,Channel,AudioEncode,
                BufferElements2Rec*BytesPerElement);
        mRecorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");
        recordingThread.start();
    }

    private void writeAudioDataToFile(){

        String state;
        state = Environment.getExternalStorageState();

        // to check whether or not we have external storage
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // get the directory and create a folder named AccData
            File Root = Environment.getExternalStorageDirectory();
            File Dir = new File(Root.getAbsolutePath() + "/AudioRecording");
            // if the folder does not exist, we create the folder
            if (!Dir.exists()) {
                Dir.mkdir();
            }
            // create file name according to data and time
            DateFormat df = new SimpleDateFormat("ddMMyyyy,HH:mm");
            String date = df.format(Calendar.getInstance().getTime());
            date = date+"+"+inputstring + ".pcm";
            File file = new File(Dir, date);
            try{
                DataOutputStream steam = new DataOutputStream(new FileOutputStream(file));
                final StringBuilder sb = new StringBuilder();
                while(isRecording){
                    //use stringbuilder to create a line of data
                    short sData[] = new short[BufferElements2Rec];
                    mRecorder.read(sData, 0, BufferElements2Rec);
                    //System.out.println("short writing to file" + sData.toString());
                    byte bData[] = shortToByte(sData);
                    steam.write(bData);
                }
                //close file
                steam.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    private void stopRcording(){
        if(mRecorder != null){
            isRecording = false;
            mRecorder.stop();
            mRecorder.release();
            mRecorder= null;
            recordingThread= null;
        }
    }

    private byte[] shortToByte(short[] Data){
        int arraysize = Data.length;
        byte[] byteArray = new byte[arraysize*2];
        for(int i = 0;i<arraysize;i++){
            byteArray[i*2] = (byte) (Data[i] & 0x00FF);
            byteArray[i*2+1] =(byte)(Data[i]>>8);
        }
        return byteArray;
    }

    //function to touch outside the keyboard to hide keyboard
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            //get the location of edittext
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

}
