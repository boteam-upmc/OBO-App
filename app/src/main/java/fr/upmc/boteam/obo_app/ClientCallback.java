package fr.upmc.boteam.obo_app;

import android.util.Log;
import android.widget.Toast;

import java.net.Socket;
import java.util.HashMap;

class ClientCallback {

    private final String LOG_TAG = "ClientCallback";

    private static class Event {
        static final String LOGIN = "onLogin";
    }

    private static class Key {
        static final String PASS = "PASS";
        static final String LOGIN = "LOGIN";
        static final String SERIAL_NUMBER = "SERIAL_NUMBER";
    }

    private HashMap<String, Object> messages;
    private boolean isRobotAccepted;

    /* singleton socket */
    private Client socket;
    {
        //final String SERVER_ADDRESS = "xxx.xxx.xx.xx";
        final String LOCAL_ADDRESS = "192.168.42.91";
        final int PORT = 3000;

        this.socket = new Client(LOCAL_ADDRESS, PORT);
    }

    ClientCallback() {
        this.messages = new HashMap<>();
        this.isRobotAccepted = false;
    }

    void setUser(String login, String pass) {
        messages.put(Key.LOGIN, login);
        messages.put(Key.PASS, pass);
    }

    void setRobot(String serialNumber) {
        messages.put(Key.SERIAL_NUMBER, serialNumber);
    }

    void connect() {
        socket.connect();
    }

    void disconnect() {
        socket.disconnect();
    }

    boolean isRobotAccepted() {
        return isRobotAccepted;
    }

    void sendAssociationRequest() {
        socket.emit(Event.LOGIN, toJsonFormat(messages));
    }

    void callback() {
        socket.setClientCallback(new Client.ClientCallback () {
            @Override
            public void onMessage(String message) {

                if (message.startsWith("ANDROID/")) {
                    Log.i(LOG_TAG, message);

                } else if (message.startsWith("VALID/")) {

                    if (message.contains("T")) {
                        isRobotAccepted = true;
                        Log.i(LOG_TAG, "isRobotAccepted=" + true);

                    } else if (message.contains("F")) {
                        isRobotAccepted = false;
                        Log.i(LOG_TAG, "isRobotAccepted=" + false);

                    } else {
                        isRobotAccepted = false;
                        Log.i(LOG_TAG, "Unknown : " + message);
                    }
                }
            }

            @Override
            public void onConnect(Socket s) {
                Log.i(LOG_TAG, "Client connected.");
            }

            @Override
            public void onDisconnect(Socket socket, String message) {
                Log.i(LOG_TAG, "Client disconnected.");
                disconnect();
            }

            @Override
            public void onConnectError(Socket socket, String message) {
                Log.i(LOG_TAG, "Connection error.");
                disconnect();
            }
        });
    }

    private String toJsonFormat(HashMap<String, Object> fields) {
        String result = "{";

        for (HashMap.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                result += "," + "\"" + key + "\":\"" + value + "\"";

            } else if (value instanceof Integer) {
                result += "," + "\"" + key + "\":" + value;
            }
        }

        return result.replaceFirst(",", "") + "}";
    }
}

