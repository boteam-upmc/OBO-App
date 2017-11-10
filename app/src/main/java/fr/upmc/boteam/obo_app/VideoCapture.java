package fr.upmc.boteam.obo_app;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED;
import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN;

public class VideoCapture extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback , MediaRecorder.OnInfoListener {

    private static final String LOG_TAG = "VideoCapture";

    int recordsCounter;
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording;
    {
        recordsCounter = 0;
        recording = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        recorder = new MediaRecorder();
        recorder.setOnInfoListener(this);
        initRecorder("rec" + recordsCounter);
        setContentView(R.layout.activity_video_capture);

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.sv_camera);
        holder = cameraView.getHolder();
        holder.addCallback(this);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
    }

    private void initRecorder(String recordName) {
        final String EXTENSION = ".mp4";
        final String SEPARATOR = File.separator;
        final int QUALITY = CamcorderProfile.QUALITY_HIGH;
        final int MAX_DURATION = 3000; // 50000 = 50 seconds
        //final int MAX_FILE_SIZE = 5000000; // Approximately 5 megabytes

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile.get(QUALITY);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile(getDirectory() + SEPARATOR + recordName + EXTENSION);
        recorder.setMaxDuration(MAX_DURATION);
        //recorder.setMaxFileSize(MAX_FILE_SIZE);
    }

    private String getDirectory() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/OBOApp");

        if (!folder.exists()) {
            boolean success = folder.mkdir();
            Log.i(LOG_TAG, "file created? " + success);
        }

        return folder.getPath();
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();

        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            recording = false;

            // Let's initRecorder so we can record again
            initRecorder("rec1");
            prepareRecorder();

        } else {
            recording = true;
            recorder.start();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        /*if (recording) {
            recorder.stop();
            recording = false;
        }*/

        //recorder.release();
        //finish();
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

        switch(what) {
            case MEDIA_RECORDER_INFO_UNKNOWN :
                Log.i(LOG_TAG, "INFO_UNKNOWN");
                break;

            //case MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING :
            case MEDIA_RECORDER_INFO_MAX_DURATION_REACHED :
                Log.i(LOG_TAG, "MAX_DURATION_REACHED");
                recordsCounter++;

                if (recordsCounter % 2 == 0) {

                    Log.i(LOG_TAG, "cpt=" + recordsCounter);
                    //recorder.stop();
                    recorder.release();

                    initRecorder("rec" + recordsCounter);
                    prepareRecorder();

                    recorder.start();
                    //finish();
                }

                /*initRecorder("rec" + recordsCounter);
                prepareRecorder();
                recorder.reset();*/
                break;

            case MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED :
                Log.i(LOG_TAG, "MAX_FILESIZE_REACHED");
                break;
        }
    }
}
