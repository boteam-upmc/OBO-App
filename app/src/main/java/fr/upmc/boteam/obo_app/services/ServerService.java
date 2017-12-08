package fr.upmc.boteam.obo_app.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import fr.upmc.boteam.obo_app.Client;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class ServerService extends IntentService {

    private static final String LOG_TAG = "ServerService";

    public static final String ACTION_SEND_VIDEO = "fr.upmc.boteam.obo_app.services.action.SEND_VIDEO";

    public static final String EXTRA_VIDEO = "fr.upmc.boteam.obo_app.services.extra.VIDEO";



    public static final String path = "/storage/emulated/0/OBOApp/";

    FileInputStream fis = null;
    BufferedInputStream bis = null;

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            switch (action) {
                case ACTION_SEND_VIDEO:
                    final String param = intent.getStringExtra(EXTRA_VIDEO);
                    handleActionSendVideo(param);
                    Log.i(LOG_TAG, "SUCCESS");
                    break;
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
                    Client.delegate.emitVideo(buffer, n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
