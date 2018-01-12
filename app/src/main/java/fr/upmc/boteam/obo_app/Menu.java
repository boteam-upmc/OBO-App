package fr.upmc.boteam.obo_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

public class Menu extends AppCompatActivity {

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
