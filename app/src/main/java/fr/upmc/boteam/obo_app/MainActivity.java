package fr.upmc.boteam.obo_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.view.LayoutInflater;
import android.support.design.widget.Snackbar;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ClientCallback client;

    private EditText mLogin;
    private EditText mPass;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 112;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 113;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 114;
    RelativeLayout  containerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerView = this.containerView;
        checkPermission();

        mLogin = (EditText) findViewById(R.id.et_login);
        mPass = (EditText) findViewById(R.id.et_pass);

        client = new ClientCallback();

        client.connect();
        client.callback();

        Button mOk = (Button) findViewById(R.id.bt_login);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                client.setUser(
                        String.valueOf(mLogin.getText()),
                        String.valueOf(mPass.getText())
                );

                client.setRobot(UUID.randomUUID().toString());

                client.sendAssociationRequest();



                client.testVideo(getApplicationContext());

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //   client.testEmit();

                recordVideo(v);
            }
        });
    }

    public void recordVideo(View view) {
        Intent intent = new Intent(this, VideoCapture.class);
        startActivity(intent);
    }

    public void checkPermission() {
        //permission about storage

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            if(!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                explain(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);


            }else{
                askPermission( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            }
        }

        //else{
        //  askPermission( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
        //       MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);}

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)){

                explain(new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                //  Log.e("RECORD_AUDIO", "permission denied");


            }else{

                askPermission(new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        }/*else  askPermission(new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSIONS_REQUEST_RECORD_AUDIO);*/

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if(!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)){
                explain(new String[]{Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_CAMERA);
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
                askPermission(new String[]{Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
        }
    }

    /* @Override
     public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
     {
         if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA)
         {
             if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
             {
                 if (!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA))
                 {
                     displayOptions();
                 }
                 else
                 {
                     explain(new String[]{Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_CAMERA);
                 }
             }

         }

         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
     }*/
    private void explain(final String[] permissions, final int permissionCode)
    {
        Snackbar.make(containerView,"Cette permission est nécessaire",Snackbar.LENGTH_LONG).setAction("Activé", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermission(permissions,permissionCode);
            }
        }).show();


    }

    public void askPermission(String[] permissions,int permissionCode){
        ActivityCompat.requestPermissions(this,
                permissions,
                permissionCode);
    }

  /*  private void displayOptions()
    {
        Snackbar.make(containerView, "Vous avez désactivé la permission", Snackbar.LENGTH_LONG)
                .setAction("Paramètres", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        final Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }).show();
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();
    }
}
