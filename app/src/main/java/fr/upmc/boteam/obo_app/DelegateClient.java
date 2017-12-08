package fr.upmc.boteam.obo_app;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

import fr.upmc.boteam.obo_app.services.ServerService;

import static fr.upmc.boteam.obo_app.services.ServerService.ACTION_SEND_VIDEO;
import static fr.upmc.boteam.obo_app.services.ServerService.EXTRA_VIDEO;

public class DelegateClient {

    // unique id for video
    private int counter = 0;

    // content of events
    public static class Event {
        public static final String LOGIN = "onLogin";
    }

    // content of keys
    public static class Key {
        static final String PASS = "PASS";
        static final String LOGIN = "LOGIN";
        static final String SERIAL_NUMBER = "SERIAL_NUMBER";
    }

    /* ROBOT MANAGEMENT */
    /* **************** */
    
    public void set_User_Robot(String login, String pass, String serialNumber) {
        setUser(login, pass);
        setRobot(serialNumber);
    }

    public void setRobotAccepted(boolean robotAccepted) {
        Client.isRobotAccepted = robotAccepted;
    }

    public void setRobot(String serialNumber) {
        Client.messages.put(Key.SERIAL_NUMBER, serialNumber);
    }

    /* USER MANAGEMENT */
    /* *************** */

    public void setUser(String login, String pass) {
        Client.messages.put(Key.LOGIN, login);
        Client.messages.put(Key.PASS, pass);
    }

    public void testVideo(Context c) {
        Context appContext = c;
        Intent intent = new Intent(appContext, ServerService.class);
        intent.setAction(ACTION_SEND_VIDEO);
        intent.putExtra(EXTRA_VIDEO, "VIDEO_" + counter);
        counter++;
        appContext.startService(intent);
    }

    /* ASSOCIATION REQUEST */
    /* ******************* */

    void sendAssociationRequest() {
        emit(Event.LOGIN, Client.delegate.toJsonFormat(Client.messages));
    }

    /* JSON FORMAT */
    /* *********** */

    public String toJsonFormat(HashMap<String, Object> fields) {
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


    void testEmit() { emit("onVideo", "EOF"); }

    private class Emit extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... message) {
            try {
                Client.socket.getOutputStream().write((message[0] + "/" + message[1]).getBytes());
                Client.socketOutput.flush();
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
            if(Client.socketOutput == null) { Client.socketOutput = Client.socket.getOutputStream(); }
            Client.socketOutput.write(localMessage);
            Client.socketOutput.flush();

        } catch (IOException e) {
            Log.i("EMITBYTES", e.getMessage());
        }
    }

    private class EmitVideo extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... tab) {
            try {
                if(Client.socketOutput == null) { Client.socketOutput = Client.socket.getOutputStream(); }
                Client.socketOutput.write((byte[]) tab[0], 0, (int) tab[1]);
                Client.socketOutput.flush();

            } catch (IOException e) {
                Log.i("EMITVIDEO", e.getMessage());
            }

            return null;
        }
    }

    public void emitVideo(byte[] buffer, int n) {
        new EmitVideo().execute(buffer, n);
    }
}
