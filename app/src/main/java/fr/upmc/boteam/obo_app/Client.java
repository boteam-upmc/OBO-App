package fr.upmc.boteam.obo_app;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import fr.upmc.boteam.obo_app.interfaces.IClientCallback;
import fr.upmc.boteam.obo_app.services.ServerService;

/**
 *
 */
public class Client implements IClientCallback {

    public static DatagramSocket s;
    public MainActivity mainActivity;
    public Context mainContext;
    public static Socket socket;
    private static BufferedReader socketInput;

    private String ip;
    private int port;
    static boolean isServerRechable=true;
    private static final String LOG_TAG = "CLIENT";

    static HashMap<String, Object> messages = new HashMap<>();
    static boolean isRobotAccepted = false;
    static boolean isUserIdentified=false;
    public static DelegateClient delegate;

    public static String id_user;
    public static String id_robot;

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

    /**
     * We user login we save the ip adress of the server
     * when something is wrong with the adresse we notice that
     * the server is unreachable
     *
     */
    public void connect() {
        Thread t=  new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    socket = new Socket(InetAddress.getByName(ip), port);
                    socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    new ReceiveThread().start();
                    Log.i(LOG_TAG, "Client connected.");


                } catch (Exception e) {
                    Client.isServerRechable=false;
                    System.out.println("CLIENT_ ERROR connect" + e.getMessage());
                }
            }
        });

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onConnect() {
        Log.i(LOG_TAG, "Client connected.");
    }

    /**
     *
     * We parse an reserving message from the server
     * @param message
     */
    public void onMessage(String message) {

        if (message.startsWith("ANDROID/")) {
            Log.i(LOG_TAG, message);

        } else if(message.startsWith("DATA/")) {
            Log.i("TEST", message);
            String[] split = message.split("/");
            Client.id_user = split[1];
            Client.id_robot = split[2];
            Log.i("SPLIT : ", Client.id_user + "/" + Client.id_robot);

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
        } else if (message.equals("identified")) {
            //delegate.setRobotAccepted(false);
            isUserIdentified=true;
            mainActivity.menu(isUserIdentified,mainContext);
           // mainActivity.recordVideo(isUserIdentified);
            Log.i(LOG_TAG, "identified=");
        } else if (message.equals("Notidentified")) {
            //delegate.setRobotAccepted(false);
            isUserIdentified=false;
            mainActivity.menu(isUserIdentified,mainContext);
            Log.i(LOG_TAG, "identified=");
        }else {
            Log.i(LOG_TAG, "NOT HANDLED MESSAGE : " + message);
        }
    }
    /**
     * Delecting all video capture during application activity
     */
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

    /**
     * A thread for geting all data received from server
     */
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
