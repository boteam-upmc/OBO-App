package fr.upmc.boteam.obo_app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

class Client {
    private Socket socket;
    private OutputStream socketOutput;
    private BufferedReader socketInput;

    private String ip;
    private int port;
    private ClientCallback listener;

    Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket = new Socket();
                InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
                try {
                    socket.connect(socketAddress);
                    socketOutput = socket.getOutputStream();
                    socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    new ReceiveThread().start();

                    if (listener != null) {
                        listener.onConnect(socket);
                    }
                } catch (IOException e) {
                    if (listener != null) {
                        listener.onConnectError(socket, e.getMessage());
                    }
                }
            }
        }).start();
    }

    void disconnect() {
        try {
            socket.close();

        } catch (IOException e) {
            if (listener != null) {
                listener.onDisconnect(socket, e.getMessage());
            }
        }
    }

    void emit(String tag, String message) {
        try {
            String localMessage = tag + "/" + message;
            socketOutput.write(localMessage.getBytes());

        } catch (IOException e) {
            if (listener != null) {
                listener.onDisconnect(socket, e.getMessage());
            }
        }
    }

    private class ReceiveThread extends Thread implements Runnable {

        public void run() {
            String message;

            try {
                // !!! each line must end with a \n to be received !!!
                while ((message = socketInput.readLine()) != null) {
                    if (listener != null) {
                        listener.onMessage(message);
                    }
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onDisconnect(socket, e.getMessage());
                }
            }
        }
    }

    void setClientCallback(ClientCallback listener) {
        this.listener = listener;
    }

    interface ClientCallback {
        void onMessage(String message);
        void onConnect(Socket socket);
        void onDisconnect(Socket socket, String message);
        void onConnectError(Socket socket, String message);
    }
}
