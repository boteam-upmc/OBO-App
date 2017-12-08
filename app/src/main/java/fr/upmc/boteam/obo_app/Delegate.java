package fr.upmc.boteam.obo_app;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;

import fr.upmc.boteam.obo_app.services.ServerService;

import static fr.upmc.boteam.obo_app.services.ServerService.ACTION_SEND_VIDEO;
import static fr.upmc.boteam.obo_app.services.ServerService.EXTRA_VIDEO;

public class Delegate {

    String LOG_TAG = "Delegate";

    private HashMap<String, Object> messages = new HashMap<>();

    private boolean isRobotAccepted = false;

    private int counter = 0;


    public static class Event {
        public static final String LOGIN = "onLogin";
    }

    public static class Key {
        static final String PASS = "PASS";
        static final String LOGIN = "LOGIN";
        static final String SERIAL_NUMBER = "SERIAL_NUMBER";
    }

    public String getLOG_TAG() {
        return LOG_TAG;
    }

    public HashMap<String, Object> getMessages() {
        return messages;
    }

    public void setRobotAccepted(boolean robotAccepted) {
        isRobotAccepted = robotAccepted;
    }

    public void testVideo(Context c) {
        Context appContext = c;
        Intent intent = new Intent(appContext, ServerService.class);
        intent.setAction(ACTION_SEND_VIDEO);
        intent.putExtra(EXTRA_VIDEO, "VIDEO_" + counter);
        counter++;
        appContext.startService(intent);
    }

    public void setRobot(String serialNumber) {
        messages.put(Key.SERIAL_NUMBER, serialNumber);
    }

    public void setUser(String login, String pass) {
        messages.put(Key.LOGIN, login);
        messages.put(Key.PASS, pass);
    }

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
}
