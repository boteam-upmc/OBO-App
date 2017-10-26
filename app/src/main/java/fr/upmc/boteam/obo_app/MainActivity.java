package fr.upmc.boteam.obo_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    final String LOGIN = "user1";
    final String PASSWORD = "pass1";
    final String ROBOT_ID = "robot1";

    ClientCallback client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new ClientCallback();
        client.setUser(LOGIN, PASSWORD);
        client.setRobotId(ROBOT_ID);

        client.connect();
        client.callback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        client.disconnect();
    }
}
