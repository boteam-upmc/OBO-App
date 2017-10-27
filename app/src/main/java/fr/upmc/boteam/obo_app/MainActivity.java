package fr.upmc.boteam.obo_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ClientCallback client;

    private EditText mLogin;
    private EditText mPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();
    }
}
