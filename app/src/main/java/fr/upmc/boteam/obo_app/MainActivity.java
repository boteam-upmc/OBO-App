package fr.upmc.boteam.obo_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    ClientCallback client;

    /*private EditText mFirstName;
    private EditText mSecondName;
    private EditText mEmail;
    private EditText mAlpha;*/
    private EditText mSerialNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*mFirstName = (EditText) findViewById(R.id.et_first_name);
        mSecondName = (EditText) findViewById(R.id.et_second_name);
        mEmail = (EditText) findViewById(R.id.et_email);
        mAlpha = (EditText) findViewById(R.id.et_alpha);*/
        mSerialNumber = (EditText) findViewById(R.id.et_serial_number);

        client = new ClientCallback();

        client.connect();
        client.callback();

        Button mOk = (Button) findViewById(R.id.bt_ok);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*client.setUser(
                        String.valueOf(mFirstName.getText()),
                        String.valueOf(mSecondName.getText()),
                        String.valueOf(mEmail.getText()),
                        Integer.valueOf(String.valueOf(mAlpha.getText()))
                );*/

                client.setRobot(String.valueOf(mSerialNumber.getText()));

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
