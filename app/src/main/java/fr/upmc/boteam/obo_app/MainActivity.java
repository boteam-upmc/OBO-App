package fr.upmc.boteam.obo_app;

import android.Manifest;
import android.content.Context;
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

import fr.upmc.boteam.obo_app.services.ServerService;

public class MainActivity extends AppCompatActivity {

    public static String SERVER_ADDRESS = "192.168.1.91";
    //public static String SERVER_ADDRESS = "192.168.1.89";
    //public static String SERVER_ADDRESS = "192.168.1.34";
    //public static String SERVER_ADDRESS = "192.168.1.91";

    public static int UDP_PORT = 3001;
    int SERVER_PORT = 3000;

    public static Client client;

    public static EditText mLogin;
    public static EditText mPass;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 112;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 113;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 114;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        mLogin = findViewById(R.id.et_login);
        mPass = findViewById(R.id.et_pass);

        client = new Client(SERVER_ADDRESS, SERVER_PORT);
        client.mainActivity=this;
        client.mainContext=this;


        Button mOk = findViewById(R.id.bt_login);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.connect();
                if(Client.isServerRechable){
                    Client.delegate.set_User_Robot(
                            String.valueOf(mLogin.getText()),
                            String.valueOf(mPass.getText()),
                            UUID.randomUUID().toString());
                    ServerService.isOnlogin=true;
                    Client.delegate.sendLoginRequest(getApplicationContext());

                 }else{

                Toast.makeText(getApplicationContext(), "Server unreachable",
                        Toast.LENGTH_LONG).show();
            }
            }
        });
    }

    /* RESTART ACTIVITY ON NEW INTENT */
    /* ***************************** */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if ("ExitApp".equals(intent.getAction())) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.onDisconnect();
    }

    /**
     *  ACTIVITY MENU */
    /* ************* */
    public void menu(boolean isUserIdentified, Context c) {
        if(isUserIdentified) {
            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        }else {
            Toast.makeText(c,"Wrong login or psw",Toast.LENGTH_LONG).show();
        }
    }

    /**
     *  We ask user all permission when we lauch the app */
    /* ******************** */
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
}
