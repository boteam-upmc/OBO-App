package fr.upmc.boteam.obo_app;

import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.util.List;
import java.util.UUID;

import fr.upmc.boteam.obo_app.services.LogoutDialog;
import fr.upmc.boteam.obo_app.services.ServerService;

/**
 *
 */
public class Menu extends AppCompatActivity{

    /** Useful console log tag. */
    private static final String LOG_TAG = "Menu";

    public static final String EXTRA_USER = "fr.upmc.boteam.obo_app.Menu.extra_user";
    public static final String EXTRA_ROBOT = "fr.upmc.boteam.obo_app.Menu.extra_robot";

    /** Used for video processing. */
    private FFmpeg ffmpeg;
    private boolean canSplitVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button mSendAssociation = findViewById(R.id.button_send_association);
        Button mControlRobot = findViewById(R.id.button_control_robot);
        Button mrecordVideo = findViewById(R.id.button_record_video);
        Button sendVideo = findViewById(R.id.button_send_video);

        mSendAssociation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Client.delegate.set_User_Robot(
                        String.valueOf(MainActivity.mLogin.getText()),
                        String.valueOf(MainActivity.mPass.getText()),
                        UUID.randomUUID().toString());
                ServerService.isOnlogin=false;
                Client.delegate.sendAssociationRequest(getApplicationContext());

                Toast.makeText(getApplicationContext(), "Sending association request", Toast.LENGTH_SHORT).show();
            }
        });

        mControlRobot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RobotControlActivity.class);
                intent.putExtra(EXTRA_USER, Client.id_user);
                intent.putExtra(EXTRA_ROBOT, Client.id_robot);
                startActivity(intent);
            }
        });

        mrecordVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recordVideo();
            }
        });

        sendVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendVideos();
            }
        });

        // Initialise video processing
        this.canSplitVideo = false;
        this.ffmpeg = FFmpeg.getInstance(getApplicationContext());
        initSplitVideo();
    }

    /** DOUBLE CLICK TO QUIT */
    /* ******************** */
    long SystemTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis()- SystemTime < 1000 && SystemTime != 0){
                List<File> videos= Client.delegate.getVideoListFiles(new File(ServerService.videoDirectory));
                if (videos.size() > 0) {
                    DialogFragment dialogFragment = LogoutDialog.newInstance("ExitApp", "You will loose all of your videos.");
                    dialogFragment.show(this.getSupportFragmentManager(), "dialog");
                }else {
                    Intent intent = new Intent();
                    intent.setAction("ExitApp");
                    intent.setClass(this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }else{
                SystemTime = System.currentTimeMillis();
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    /** SHOW ITEM TO THE APP BAR */
    /* ************************ */
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * handling click events
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_logout:
                 List<File> videos= Client.delegate.getVideoListFiles(new File(ServerService.videoDirectory));
                if (videos.size() > 0){
                    DialogFragment dialogFragment = LogoutDialog.newInstance("logout", "You will loose all of your videos.");
                    dialogFragment.show(this.getSupportFragmentManager(), "dialog");
                }else {
                    Intent intent = new Intent();
                    intent.setClass(this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.client.onDisconnect();
    }

    /**
     * Start recording video.
     */
    public void recordVideo() {
        final int CAMERA_RQ = 6969;
        final int QUALITY = MaterialCamera.QUALITY_480P;

        new MaterialCamera(this)
                .saveDir(getFolder())
                .allowRetry(false) // Whether or not 'Retry' is visible during playback
                .showPortraitWarning(false) // Whether or not a warning is displayed if the user presses record in portrait orientation
                //.retryExits(true)
                .qualityProfile(QUALITY)
                .start(CAMERA_RQ);
    }

    /**
     * Send a new video to the server after splitting it into parts
     * and also send old video if they exists.
     */
    private void sendVideos() {
        if (getFolder().list().length <= 0) {
            Toast.makeText(getApplicationContext(), "There is no video to send. " +
                    "Try to record a new one.", Toast.LENGTH_SHORT).show();

        } else {
            if (existNewVideo()) {
                chooseAndSplitVideo();

            } else {
                Log.i(LOG_TAG, "There is no new video");
                // send old videos if they exists
                Client.delegate.sendingAllVideos(getApplicationContext());
            }
        }
    }

    /**
     * Initialise video processing by loading FFmpeg binary.
     */
    private void initSplitVideo() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {
                    canSplitVideo = true;
                }

                @Override
                public void onFinish() {}
            });

        } catch (FFmpegNotSupportedException e) {
            Log.i(LOG_TAG, "FFmpeg is not supported by your device");
        }
    }

    /**
     * Ask the user to choose a video and split it.
     */
    private void chooseAndSplitVideo() {
        new ChooserDialog()
                .with(this)
                // ignore trimmed videos
                .withFilterRegex(false, false, ".*VID.*\\.mp4")
                .withStartFile(getPath())
                .withResources(R.string.title_choose_dict_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        splitAndSendVideo(pathFile);
                    }
                })
                .build()
                .show();

    }

    /**
     * Check if a file starts with 'VID' exists in a directory.
     * @return true if a file starts with 'VID' exists, false otherwise.
     */
    private boolean existNewVideo() {
        File dir = new File(getDirectory());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().startsWith("VID")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Split a video into 10min trims then remove it.
     */
    private void splitAndSendVideo(final File pathFile) {
        if (canSplitVideo) {
            String fileName = pathFile.getName();
            try {
                String[] cmd = {
                        "-i",
                        getPath() + File.separator + fileName,
                        "-c",
                        "copy",
                        "-map",
                        "0",
                        "-segment_time",
                        "600", // duration in seconds. Example : 60 = 1min
                        "-f",
                        "segment",
                        getPath() + File.separator + fileName.replace("VID", "TMP") + "_" + "%03d" +".mp4"
                };

                ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                    ProgressBar mSplitVideo = findViewById(R.id.pb_split_video);

                    @Override
                    public void onStart() {
                        mSplitVideo.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onProgress(String message) {
                        Log.i(LOG_TAG, "onProgress : " + message);
                    }

                    @Override
                    public void onFailure(String message) {
                        Log.i(LOG_TAG, "onFailure : " + message);
                    }

                    @Override
                    public void onSuccess(String message) {
                        Log.i(LOG_TAG, "onSuccess : " + message);

                        boolean isDeleted = pathFile.delete();
                        Log.i(LOG_TAG, "File deleted : " + isDeleted);
                        mSplitVideo.setVisibility(View.INVISIBLE);

                        // send video parts to the server
                        Client.delegate.sendingAllVideos(getApplicationContext());
                    }

                    @Override
                    public void onFinish() {}
                });

            } catch (FFmpegCommandAlreadyRunningException e) {
                Log.i(LOG_TAG, "FFmpeg is already running");
            }
        }
    }

    /**
     * Create or find an existing external storage folder.
     * @return An external storage folder.
     */
    private File getFolder() {
        File folder = new File(getDirectory());

        if (!folder.exists()) {
            boolean success = folder.mkdir();
            Log.i(LOG_TAG, "file created? " + success);
        }

        return folder;
    }

    /**
     * Get the existing external storage path.
     * @return An external storage path.
     */
    private String getPath() {
        return getFolder().getPath();
    }

    /**
     * Get the external storage directory.
     * @return An external storage directory.
     */
    private String getDirectory() {
        return Environment.getExternalStorageDirectory() + "/OBOApp";
    }
}
