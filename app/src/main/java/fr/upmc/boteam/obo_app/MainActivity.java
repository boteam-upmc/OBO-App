package fr.upmc.boteam.obo_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Client socket = new Client("192.168.42.91", 3000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socket.setClientCallback(new Client.ClientCallback () {
            @Override
            public void onMessage(String message) {
                Log.i("MainActivity", message);
                socket.disconnect();
            }

            @Override
            public void onConnect(Socket s) {
                socket.send("ggg");
            }

            @Override
            public void onDisconnect(Socket socket, String message) {

            }

            @Override
            public void onConnectError(Socket socket, String message) {

            }
        });

        socket.connect();
    }
}
