package fr.upmc.boteam.obo_app;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;

import fr.upmc.boteam.obo_app.interfaces.IClientCallback;
import fr.upmc.boteam.obo_app.services.ServerService;

public class Client implements IClientCallback {

    public static DatagramSocket s;

    public static Socket socket;
    private static BufferedReader socketInput;

    private String ip;
    private int port;

    private static final String LOG_TAG = "CLIENT";

    static HashMap<String, Object> messages = new HashMap<>();
    static boolean isRobotAccepted = false;

    static DelegateClient delegate;

    Client(String ip, int port) {
        try {
            s = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.ip = ip;
        this.port = port;
        Client.delegate = new DelegateClient();
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    socket = new Socket(InetAddress.getByName(ip), port);
                    socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    new ReceiveThread().start();
                    Log.i(LOG_TAG, "Client connected.");


                } catch (Exception e) {
                    System.out.println("CLIENT_ ERROR connect" + e.getMessage());
                }
            }
        }).start();
    }

    public void onConnect() {
        Log.i(LOG_TAG, "Client connected.");
    }

    public void onMessage(String message) {

        if (message.startsWith("ANDROID/")) {
            Log.i(LOG_TAG, message);

        } else if (message.startsWith("VALID/")) {

            if (message.contains("T")) {
                delegate.setRobotAccepted(true);
                Log.i(LOG_TAG, "isRobotAccepted=" + true);

            } else if (message.contains("F")) {
                delegate.setRobotAccepted(false);
                Log.i(LOG_TAG, "isRobotAccepted=" + false);

            } else {
                delegate.setRobotAccepted(false);
                Log.i(LOG_TAG, "Unknown : " + message);
            }
        } else {
            Log.i(LOG_TAG, "NOT HANDLED MESSAGE : " + message);
        }
    }

    public void onDisconnect() {
        Log.i(LOG_TAG, "Client disconnected.");

        List<File> videoListFiles = DelegateClient.getVideoListFiles(new File(ServerService.videoDirectory));
        for(int i = 0; i < videoListFiles.size(); i++) {
            videoListFiles.get(i).delete();
        }

        try {
            socket.close();

        } catch (Exception e) {
            Log.i("DISCONNECT", e.getMessage());
        }
    }

    public void onConnectError(String message) {
        Log.i(LOG_TAG, "Connection error. " + message);
        onDisconnect();
    }

    public boolean isConnected() {
        return Client.socket != null;
    }

    private class ReceiveThread extends Thread implements Runnable {

        public void run() {
            String message;

            try {
                // !!! each line must end with a \n to be received !!!
                while ((message = socketInput.readLine()) != null || isConnected()) { onMessage(message); }
            } catch (Exception e) {
                Log.i("RECEIVEDTHREAD", e.getMessage());
            }
        }
    }
}
