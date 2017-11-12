package fr.upmc.boteam.obo_app.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import fr.upmc.boteam.obo_app.Client;
import fr.upmc.boteam.obo_app.ClientCallback;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class ServerService extends IntentService {

    private static final String LOG_TAG = "ServerService";

    public static final String ACTION_SHOW_HELLO_WORLD = "fr.upmc.boteam.obo_app.services.action.HELLO_WORLD";
    public static final String ACTION_SHOW_YOLO = "fr.upmc.boteam.obo_app.services.action.YOLO";
    public static final String ACTION_SEND_VIDEO = "fr.upmc.boteam.obo_app.services.action.SEND_VIDEO";

    public static final String EXTRA_HELLO = "fr.upmc.boteam.obo_app.services.extra.HELLO";
    public static final String EXTRA_WORLD = "fr.upmc.boteam.obo_app.services.extra.WORLD";
    public static final String EXTRA_YOLO = "fr.upmc.boteam.obo_app.services.extra.YOLO";
    public static final String EXTRA_VIDEO = "fr.upmc.boteam.obo_app.services.extra.VIDEO";



    public static final String path = "/storage/emulated/0/OBOApp/";

    private Client client = ClientCallback.socket;
    FileInputStream fis = null;
    BufferedInputStream bis = null;

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_SHOW_HELLO_WORLD.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_HELLO);
                final String param2 = intent.getStringExtra(EXTRA_WORLD);
                handleActionShowHelloWorld(param1, param2);

            } else if (ACTION_SHOW_YOLO.equals(action)) {
                final String param = intent.getStringExtra(EXTRA_YOLO);
                handleActionYolo(param);

            } else if (ACTION_SEND_VIDEO.equals(action)) {
                final String param = intent.getStringExtra(EXTRA_VIDEO);
                handleActionSendVideo(param);
                Log.i(LOG_TAG, "SUCCESS");
            }
        }
    }

    /**
     * Handle action Send Video in the provided background thread with the provided
     * parameter.
     */
    private void handleActionSendVideo(String param) {
        if(param != null) {
            String videoPath = path + param + ".mp4";
            int n;
            try {
                File file = new File(videoPath);
                byte[] buffer = new byte[(int)file.length()];
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                System.out.println("Sending " + videoPath + "(" + buffer.length + " bytes)");
                while((n = bis.read(buffer,0,buffer.length)) != -1){
                    client.emitVideo(buffer, n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle action Show Hello World in the provided background thread with the provided
     * parameters.
     */
    private void handleActionShowHelloWorld(String param1, String param2) {
        if (param1 != null && param2 != null) {
            Log.i(LOG_TAG, "handleActionShowHelloWorld : " + param1 + " " + param2);

        } else {
            Log.i(LOG_TAG, "handleActionShowHelloWorld : params are null");
            //
        }
    }

    /**
     * Handle action Yolo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionYolo(String param) {
        if (param != null) {
            Log.i(LOG_TAG, "handleActionYolo : " + param);

        } else {
            Log.i(LOG_TAG, "handleActionYolo : params are null");
        }
    }
}
