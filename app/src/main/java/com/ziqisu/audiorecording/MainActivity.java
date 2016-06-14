package com.ziqisu.audiorecording;

//import android.hardware.SensorManager;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.content.Context;
import android.content.Intent;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.NullPointerException;


public class MainActivity extends AppCompatActivity{

    private AudioRecord mRecorder = null;

    private boolean changecolor = true;
    //EditText input = (EditText) findViewById(R.id.input);
    String inputstring;
    private static final int SampleRate = 8000;
    private static final int Channel = AudioFormat.CHANNEL_IN_MONO;
    private static final int AudioEncode = AudioFormat.ENCODING_PCM_16BIT;
    private Thread recordingThread = null;
    int BufferElements2Rec = 1024;
    int BytesPerElement = 2;
    private boolean isRecording = false;

    //chanelConfiguration = AudioFormat.ENCODING


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //EditText input = (EditText) findViewById(R.id.input);
        setupButton();
        setupInputButton();

    }

    private void setupInputButton(){
        final EditText input = (EditText) findViewById(R.id.input);
        final Button inputbutton = (Button)findViewById(R.id.inputbutton);
        inputbutton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        inputstring = input.getText().toString();
                        Log.i("input string is:",inputstring);
                    }
                }
        );
    }




    private void setupButton() {
        //get a reference to the button
        final Button startbutton = (Button)findViewById(R.id.button);
        //set the click listener to run my code
        startbutton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //onCheckedChanged(startbutton,true);

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
            date = date+inputstring + ".pcm";
            File file = new File(Dir, date);
            try{
                DataOutputStream steam = new DataOutputStream(new FileOutputStream(file));
                final StringBuilder sb = new StringBuilder();
                while(isRecording){
                    //use stringbuilder to create a line of data
                    short sData[] = new short[BufferElements2Rec];
                    mRecorder.read(sData, 0, BufferElements2Rec);
                    System.out.println("short writing to file" + sData.toString());
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




    /*public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                Toast.makeText(this,"OK, clicked", Toast.LENGTH_SHORT).show();
                Log.i("I clicked the button:","run at this");
                String input = textUserInput.getText().toString();
                Log.i("the input is:",input);
                if(input == null){
                    Log.i("the input is null","why?");
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                Toast.makeText(this,"Cancel, clicked", Toast.LENGTH_SHORT).show();
                break;
        }
    }*/
}
