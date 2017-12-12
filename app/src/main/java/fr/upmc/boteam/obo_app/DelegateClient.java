package fr.upmc.boteam.obo_app;

import android.content.Context;
import android.content.Intent;
import java.util.HashMap;

import fr.upmc.boteam.obo_app.services.ServerService;

/* SendMessage */
/* *********** */
import static fr.upmc.boteam.obo_app.services.ServerService.ACTION_SEND_MESSAGE;
import static fr.upmc.boteam.obo_app.services.ServerService.EXTRA_MESSAGE;
/* SendVideo */
/* ********* */
import static fr.upmc.boteam.obo_app.services.ServerService.ACTION_SEND_VIDEO;
import static fr.upmc.boteam.obo_app.services.ServerService.EXTRA_VIDEO;

class DelegateClient {

    // unique id for video
    private int counter = 0;

    // content of events
    public static class Event {
        static final String LOGIN = "onLogin";
    }

    // content of keys
    public static class Key {
        static final String PASS = "PASS";
        static final String LOGIN = "LOGIN";
        static final String SERIAL_NUMBER = "SERIAL_NUMBER";
    }

    /* ROBOT MANAGEMENT */
    /* **************** */

    void set_User_Robot(String login, String pass, String serialNumber) {
        setUser(login, pass);
        setRobot(serialNumber);
    }

    void setRobotAccepted(boolean robotAccepted) {
        Client.isRobotAccepted = robotAccepted;
    }

    private void setRobot(String serialNumber) {
        Client.messages.put(Key.SERIAL_NUMBER, serialNumber);
    }

    /* USER MANAGEMENT */
    /* *************** */

    private void setUser(String login, String pass) {
        Client.messages.put(Key.LOGIN, login);
        Client.messages.put(Key.PASS, pass);
    }

    /* ASSOCIATION REQUEST */
    /* ******************* */

    void sendAssociationRequest(Context c) {
        sendMessage(c, Event.LOGIN, Client.delegate.toJsonFormat(Client.messages));
    }

    /* SERVERSERVICE FONCTION */
    /* ********************** */

    void sendVideo(Context appContext) {
        Intent intent = new Intent(appContext, ServerService.class);
        intent.setAction(ACTION_SEND_VIDEO);
        intent.putExtra(EXTRA_VIDEO, "VIDEO_" + counter);
        counter++;
        appContext.startService(intent);
    }

    void sendMessage(Context appContext, String tag, String message) {
        String[] s = {tag, message};
        Intent intent = new Intent(appContext, ServerService.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE, s);
        appContext.startService(intent);
    }

    /* JSON FORMAT */
    /* *********** */

    private String toJsonFormat(HashMap<String, Object> fields) {
        StringBuilder result = new StringBuilder("{");

        for (HashMap.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                result.append("," + "\"").append(key).append("\":\"").append(value).append("\"");

            } else if (value instanceof Integer) {
                result.append("," + "\"").append(key).append("\":").append(value);
            }
        }

        return result.toString().replaceFirst(",", "") + "}";
    }
}
