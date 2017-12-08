package fr.upmc.boteam.obo_app;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;

import fr.upmc.boteam.obo_app.interfaces.IClientCallback;

public class Client implements IClientCallback {

    static Socket socket;
    static OutputStream socketOutput;
    static BufferedReader socketInput;

    private String ip;
    private int port;

    private static final String LOG_TAG = "Client";
    static HashMap<String, Object> messages = new HashMap<>();
    static boolean isRobotAccepted = false;

    public static DelegateClient delegate;

    Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
        Client.delegate = new DelegateClient();
    }

    void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Client.socket = new Socket();
                    SocketAddress sockAddr = new InetSocketAddress(ip, port);
                    socket.connect(sockAddr);
                    socketOutput = socket.getOutputStream();
                    socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    new ReceiveThread().start();


                } catch (IOException e) {
                    System.out.println("CLIENT_ ERROR connect" + e.getMessage());
                }
            }
        }).start();
    }

    public void onConnect() {
        Log.i(this.LOG_TAG, "Client connected.");
    }

    public void onMessage(String message) {

        if (message.startsWith("ANDROID/")) {
            Log.i(this.LOG_TAG, message);

        } else if (message.startsWith("VALID/")) {

            if (message.contains("T")) {
                delegate.setRobotAccepted(true);
                Log.i(this.LOG_TAG, "isRobotAccepted=" + true);

            } else if (message.contains("F")) {
                delegate.setRobotAccepted(false);
                Log.i(this.LOG_TAG, "isRobotAccepted=" + false);

            } else {
                delegate.setRobotAccepted(false);
                Log.i(this.LOG_TAG, "Unknown : " + message);
            }
        } else {
            Log.i(this.LOG_TAG, "NOT HANDLED MESSAGE : " + message);
        }
    }

    public void onDisconnect() {
        Log.i(this.LOG_TAG, "Client disconnected.");
        try {
            if(socket != null) { socket.close(); }

        } catch (IOException e) {
            Log.i("DISCONNECT", e.getMessage());
        }
    }

    public void onConnectError(String message) {
        Log.i(this.LOG_TAG, "Connection error. " + message);
        onDisconnect();
    }

    private class ReceiveThread extends Thread implements Runnable {

        public void run() {
            String message;

            while (true) {
                try {
                    // !!! each line must end with a \n to be received !!!
                    while ((message = socketInput.readLine()) != null) { onMessage(message); }
                } catch (IOException e) {
                    Log.i("RECEIVEDTHREAD", e.getMessage());
                }
            }
        }
    }
}
