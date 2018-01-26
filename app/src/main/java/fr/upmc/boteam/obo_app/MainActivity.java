package fr.upmc.boteam.obo_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    String SERVER_ADDRESS = "134.157.252.168";
    int SERVER_PORT = 3000;

    public static Client client;

    private EditText mLogin;
    private EditText mPass;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 112;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 113;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 114;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        mLogin = (EditText) findViewById(R.id.et_login);
        mPass = (EditText) findViewById(R.id.et_pass);

        client = new Client(SERVER_ADDRESS, SERVER_PORT,this);

        client.connect();
        Log.i("sidi","CLIENT_ MainActivity "+client.isServerRechable);

        Button mOk = findViewById(R.id.bt_login);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(client.isServerRechable){
                    Client.delegate.set_User_Robot(
                            String.valueOf(mLogin.getText()),
                            String.valueOf(mPass.getText()),
                            UUID.randomUUID().toString());

                    Client.delegate.sendAssociationRequest(getApplicationContext());

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("sidi","CLIENT_ MainActivity ISidentified "+client.isUserIdentified);
                   // recordVideo();
                }else{

                    Toast.makeText(getApplicationContext(), "Server unreachable",
                               Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void testEmit() { Client.delegate.sendMessage(getApplicationContext(),"onVideo", "EOF"); }

    public void recordVideo(boolean isUserIdentified) {
        if(isUserIdentified){
            Intent intent = new Intent(this, VideoCapture.class);
            startActivity(intent);
        }//else{
          //  Toast.makeText(this, "Wrong password or Log in",
            //        Toast.LENGTH_LONG).show();
       // }

    }

    public void checkPermission() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Log.e("WRITE_EXTERNAL_STORAGE", "permission denied");
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)){
                Log.e("RECORD_AUDIO", "permission denied");
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)){
                Log.e("CAMERA", "permission denied");
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.onDisconnect();
    }
}
