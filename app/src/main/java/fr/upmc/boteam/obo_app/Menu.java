package fr.upmc.boteam.obo_app;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

import fr.upmc.boteam.obo_app.services.LogoutDialog;

public class Menu extends AppCompatActivity{

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

                Client.delegate.sendAssociationRequest(getApplicationContext());

                Toast.makeText(getApplicationContext(), "Sending association request", Toast.LENGTH_SHORT).show();
            }
        });

        final Button record_video_button = findViewById(R.id.button_record_video);
        record_video_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recordVideo(v);
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

    /* ACTIVITY VIDEOCAPTURE */
    /* ********************* */
    public void recordVideo(View view) {
        Intent intent = new Intent(this, VideoCapture.class);
        startActivity(intent);
    }
}
