package fr.upmc.boteam.obo_app;

import android.util.Log;

import java.net.Socket;
import java.util.HashMap;

class ClientCallback {

    private final String LOG_TAG = "ClientCallback";

    private static class Event {
        static final String LOGIN = "onLogin";
    }

    private static class Key {
        static final String USERNAME = "USERNAME";
        static final String PASSWORD = "PASSWORD";
        static final String ROBOT_ID = "ROBOT_ID";
    }

    private HashMap<String, String> messages;

    /* singleton socket */
    private Client socket;
    {
        //final String SERVER_ADDRESS = "xxx.xxx.xx.xx";
        final String LOCAL_ADDRESS = "192.168.42.91";
        final int PORT = 3000;

        socket = new Client(LOCAL_ADDRESS, PORT);
    }

    ClientCallback() {
        this.messages = new HashMap<>();
    }

    void setUser(String username, String password) {
        messages.put(Key.USERNAME, username);
        messages.put(Key.PASSWORD, password);
    }

    void setRobotId(String robotId) {
        messages.put(Key.ROBOT_ID, robotId);
    }

    void connect() {
        socket.connect();
    }

    void disconnect() {
        socket.disconnect();
    }

    void callback() {
        socket.setClientCallback(new Client.ClientCallback () {
            @Override
            public void onMessage(String message) {
                Log.i(LOG_TAG, message);
                socket.disconnect();
            }

            @Override
            public void onConnect(Socket s) {
                Log.i(LOG_TAG, "Client connected." + toJsonFormat(messages));
                socket.emit(Event.LOGIN, toJsonFormat(messages));
            }

            @Override
            public void onDisconnect(Socket socket, String message) {
                Log.i(LOG_TAG, "Client disconnected.");
            }

            @Override
            public void onConnectError(Socket socket, String message) {
                Log.i(LOG_TAG, "Connection error.");
            }
        });
    }

    private String toJsonFormat(HashMap<String, String> fields) {
        String result = "{";

        for (HashMap.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            result += "," + "\"" + key + "\":\"" + value + "\"";
        }

        return result.replaceFirst(",", "") + "}";
    }
}

