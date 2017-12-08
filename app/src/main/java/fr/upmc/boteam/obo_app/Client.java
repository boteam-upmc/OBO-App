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

import static fr.upmc.boteam.obo_app.Delegate.*;

public class Client {

    private Socket socket;
    private OutputStream socketOutput;
    private BufferedReader socketInput;

    public static Delegate delegate;

    private String ip;
    private int port;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.delegate = new Delegate();
    }

    void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    System.out.println("CLIENT_ before addr");
                    socket = new Socket();
                    SocketAddress sockAddr = new InetSocketAddress(ip, port);
                    System.out.println("CLIENT_ before .connect");
                    socket.connect(sockAddr);
                    System.out.println("CLIENT_ after .connect");
                    socketOutput = socket.getOutputStream();
                    socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    new ReceiveThread().start();


                } catch (IOException e) {
                    System.out.println("CLIENT_ ERROR connect" + e.getMessage());
                }
                System.out.println("CLIENT_ end connect");
            }
        }).start();
    }

    public void set_User_Robot(String login, String pass, String serialNumber) {
        delegate.setUser(login, pass);
        delegate.setRobot(serialNumber);
    }

    public void receivedMessage(String message) {

        if (message.startsWith("ANDROID/")) {
            Log.i(delegate.getLOG_TAG(), message);

        } else if (message.startsWith("VALID/")) {

            if (message.contains("T")) {
                delegate.setRobotAccepted(true);
                Log.i(delegate.getLOG_TAG(), "isRobotAccepted=" + true);

            } else if (message.contains("F")) {
                delegate.setRobotAccepted(false);
                Log.i(delegate.getLOG_TAG(), "isRobotAccepted=" + false);

            } else {
                delegate.setRobotAccepted(false);
                Log.i(delegate.getLOG_TAG(), "Unknown : " + message);
            }
        } else {
            Log.i(delegate.getLOG_TAG(), "NOT HANDLED MESSAGE : " + message);
        }
    }

    public void disconnect() {
        try {
            if(socket != null) { socket.close(); }

        } catch (IOException e) {
            Log.i("DISCONNECT", e.getMessage());
        }
    }

    void testEmit() { emit("onVideo", "EOF"); }

    private class Emit extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... message) {
            try {
                if (socketOutput == null) {
                    socketOutput = socket.getOutputStream();
                }
                socketOutput.write((message[0] + "/" + message[1]).getBytes());
                socketOutput.flush();
            } catch (IOException e) {
                Log.i("EMIT", e.getMessage());
            }

            return null;
        }
    }

    public void emit(String tag, String message) {
        new Emit().execute(tag, message);
    }

    public void emitBytes(String tag, byte[] message) {
        try {
            String result = tag + "/" + message.toString();
            byte[] localMessage = result.getBytes();
            if(socketOutput == null) { socketOutput = socket.getOutputStream(); }
            socketOutput.write(localMessage);
            socketOutput.flush();

        } catch (IOException e) {
            Log.i("EMITBYTES", e.getMessage());
        }
    }

    private class EmitVideo extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... tab) {
            try {
                if(socketOutput == null) { socketOutput = socket.getOutputStream(); }
                socketOutput.write((byte[]) tab[0], 0, (int) tab[1]);
                socketOutput.flush();

            } catch (IOException e) {
                Log.i("EMITVIDEO", e.getMessage());
            }

            return null;
        }
    }

    public void emitVideo(byte[] buffer, int n) {
        new EmitVideo().execute(buffer, n);
    }

    void sendAssociationRequest() {
        emit(Event.LOGIN, delegate.toJsonFormat(delegate.getMessages()));
    }

    private class ReceiveThread extends Thread implements Runnable {

        public void run() {
            String message;

            while (true) {
                try {
                    // !!! each line must end with a \n to be received !!!
                    while ((message = socketInput.readLine()) != null) { receivedMessage(message); }
                } catch (IOException e) {
                    Log.i("RECEIVEDTHREAD", e.getMessage());
                }
            }
        }
    }

    public void setClientCallback(ClientCallback l) {
        ClientCallback listener = l;
    }

    interface ClientCallback {
        void onMessage(String message);
        void onConnect();
        void onDisconnect(String message);
        void onConnectError(String message);
    }
}
