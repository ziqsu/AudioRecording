# AudioRecording

This is app use AudioRecord in the android to record and store raw audio file into our android phone.
Please enter the name first and press OK, then press green button to start recording. If you want to stop recording, press the same button, it will stop recording.
After recording, you can find your file under /sdcard/AudioRecording directory and the format of your file name is: data+time+the name you just input. 

If you want to replay the raw data that you recorded, you can download Audacity and import the raw data, change dafault sample rate to 16000 and format to 16 BIT-PCM, then you will hear your original audio.

update: I add requesting permission at runtime so that android marshmallow users can use it without errors.(The app will ask for permission when you first open the app.)
