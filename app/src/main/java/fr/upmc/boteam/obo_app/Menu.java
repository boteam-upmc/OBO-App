package fr.upmc.boteam.obo_app;

import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;

import java.io.File;
import java.util.UUID;

import fr.upmc.boteam.obo_app.services.LogoutDialog;
import fr.upmc.boteam.obo_app.services.ServerService;

public class Menu extends AppCompatActivity{

    private static final String LOG_TAG = "Menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final Button send_association_button = findViewById(R.id.button_send_association);
        send_association_button.setOnClickListener(new View.OnClickListener() {
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

        final Button record_video_button = findViewById(R.id.button_record_video);
        record_video_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recordVideo();
            }
        });

        final Button send_video_button = findViewById(R.id.button_send_video);
        send_video_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Client.delegate.sendingAllVideos(getApplicationContext());
            }
        });
    }

    /* DOUBLE CLICK TO QUIT */
    /* ******************** */
    long SystemTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis()- SystemTime < 1000 && SystemTime != 0){
                DialogFragment dialogFragment = LogoutDialog.newInstance("ExitApp");
                dialogFragment.show(this.getSupportFragmentManager(), "dialog");
            }else{
                SystemTime = System.currentTimeMillis();
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    /* SHOW ITEM TO THE APP BAR */
    /* ************************ */
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //handling click events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_logout:
                DialogFragment dialogFragment = LogoutDialog.newInstance("logout");
                dialogFragment.show(this.getSupportFragmentManager(), "dialog");
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
     * Start recording video
     */
    public void recordVideo() {
        /*Intent intent = new Intent(this, VideoCapture.class);
        startActivity(intent);*/

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
     *
     * @return
     */
    private File getFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/OBOApp");

        if (!folder.exists()) {
            boolean success = folder.mkdir();
            Log.i(LOG_TAG, "file created? " + success);
        }

        return folder;
    }

    /**
     *
     * @return
     */
    private String getPath() {
        return getFolder().getPath();
    }
}
