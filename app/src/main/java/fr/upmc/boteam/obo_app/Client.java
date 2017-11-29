package fr.upmc.boteam.obo_app;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    private Socket socket;
    private OutputStream socketOutput;
    private BufferedReader socketInput;

    private String ip;
    private int port;
    private ClientCallback listener;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private class ConnectTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... ip) {
            socket = new Socket();
            InetSocketAddress socketAddress = new InetSocketAddress(ip[0], port);
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
            return null;
        }
    }

    void connect() {
        new ConnectTask().execute(ip);
        /*new Thread(new Runnable() {
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
        }).start();*/
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

    public void emit(String tag, String message) {
        if(socketOutput == null) {
            try {
                socketOutput = socket.getOutputStream();
                socketOutput.write((tag + "/" + message).getBytes());
                socketOutput.flush();
            } catch (IOException e) {
                if (listener != null) {
                    listener.onDisconnect(socket, e.getMessage());
                }
            }
        } else {
            try {
                socketOutput.write((tag + "/" + message).getBytes());
                socketOutput.flush();

            } catch (IOException e) {
                if (listener != null) {
                    listener.onDisconnect(socket, e.getMessage());
                }
            }
        }
    }

    public void emitBytes(String tag, byte[] message) {
        try {
            String result = tag + "/" + message.toString();
            byte[] localMessage = result.getBytes();
            socketOutput.write(localMessage);

        } catch (IOException e) {
            if (listener != null) {
                listener.onDisconnect(socket, e.getMessage());
            }
        }
    }

    public void emitVideo(byte[] buffer, int n) {
        try {
            socketOutput.write(buffer, 0, n);
            socketOutput.flush();
        } catch (IOException e) {
            if (listener != null) {
                listener.onDisconnect(socket, e.getMessage());
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public OutputStream getSocketOutput() {
        return socketOutput;
    }

    public BufferedReader getSocketInput() {
        return socketInput;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    private class ReceiveThread extends Thread implements Runnable {

        public void run() {
            String message;

            while (true) {
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
    }

    public void setClientCallback(ClientCallback listener) {
        this.listener = listener;
    }

    interface ClientCallback {
        void onMessage(String message);
        void onConnect(Socket socket);
        void onDisconnect(Socket socket, String message);
        void onConnectError(Socket socket, String message);
    }
}
