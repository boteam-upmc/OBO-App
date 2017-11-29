package fr.upmc.boteam.obo_app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED;
import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN;

public class VideoCapture extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback , MediaRecorder.OnInfoListener {

    private static final String LOG_TAG = "VideoCapture";
    public static final String MY_PREFERENCES = "fr.upmc.boteam.obo_app.services.extra.MY_PREFERENCES";

    public static final String KEY_RECORDS_COUNTER = "fr.upmc.boteam.obo_app.services.extra.KEY_RECORDS_COUNTER";

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 112;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 113;

    private SharedPreferences sharedPref;

    private Context mContext;

    private int recordsCounter;
    private SurfaceView cameraView;
    private MediaRecorder recorder;
    private SurfaceHolder holder;
    private boolean recording;
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

        checkPermission();

        recorder = new MediaRecorder();
        recorder.setOnInfoListener(this);
        initRecorder("VIDEO_" + recordsCounter);
        setContentView(R.layout.activity_video_capture);

        cameraView = findViewById(R.id.sv_camera);
        holder = cameraView.getHolder();
        holder.addCallback(this);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);

        sharedPref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        mContext = getApplicationContext();
    }

    private void initRecorder(String recordName) {
        final String EXTENSION = ".mp4";
        final String SEPARATOR = File.separator;
        final int QUALITY = CamcorderProfile.QUALITY_HIGH;
        final int MAX_DURATION = 5000; // 50000 = 50 seconds
        //final int MAX_FILE_SIZE = 5000000; // Approximately 5 megabytes

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

        }
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile.get(QUALITY);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile(getDirectory() + SEPARATOR + recordName + EXTENSION);
        recorder.setMaxDuration(MAX_DURATION);
        //recorder.setMaxFileSize(MAX_FILE_SIZE);
    }

    @NonNull
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
            Log.i(LOG_TAG, "STOP");
            recorder.stop();
            recording = false;

            // Let's initRecorder so we can record again
            initRecorder("VIDEO_" + recordsCounter / 2);
            prepareRecorder();

        } else {
            Log.i(LOG_TAG, "START");
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
    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

        switch(what) {
            case MEDIA_RECORDER_INFO_UNKNOWN :
                Log.i(LOG_TAG, "INFO_UNKNOWN");
                break;

            case MEDIA_RECORDER_INFO_MAX_DURATION_REACHED :
                recordsCounter++;

                if (recordsCounter % 2 == 0) {
                    Log.i(LOG_TAG, "Record n°" + ((recordsCounter / 2) - 1) + " saved.");
                    cameraView.performClick();
                    cameraView.performClick();

                    String path = Environment.getExternalStorageDirectory() + "/OBOApp/VIDEO_" + ((recordsCounter / 2) - 1) + ".mp4";
                    Log.i(LOG_TAG, path);

                    //createVideoThumbnail(mContext, Uri.fromFile(new File(path)));
                }
                break;

            case MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED :
                Log.i(LOG_TAG, "MAX_FILESIZE_REACHED");
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        storeInSharedPreferences(KEY_RECORDS_COUNTER, (recordsCounter / 2) - 1);

        /*if (recording) {
            recorder.stop();
            recording = false;
        }

        recorder.release();
        finish();*/
    }

    @Override
    public void onResume() {
        super.onResume();

        restoreFromSharedPreferences();
    }

    private void storeInSharedPreferences(String key, int intValue) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, intValue);
        editor.apply();

        Log.i(LOG_TAG, "intValue=" + intValue);
    }

    private void restoreFromSharedPreferences() {
        recordsCounter = sharedPref.getInt(KEY_RECORDS_COUNTER, 0);

        Log.i(LOG_TAG, "recordsCounter=" + recordsCounter);
    }

    public Bitmap createVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime(-1);

        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        return bitmap;
    }

    public void checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
