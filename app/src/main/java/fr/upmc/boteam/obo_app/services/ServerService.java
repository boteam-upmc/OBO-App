package fr.upmc.boteam.obo_app.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;

import fr.upmc.boteam.obo_app.Client;
import fr.upmc.boteam.obo_app.MainActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class ServerService extends IntentService {

    private static final String LOG_TAG = "ServerService";

    public static final String ACTION_SEND_VIDEO = "fr.upmc.boteam.obo_app.services.action.SEND_VIDEO";
    public static final String ACTION_SEND_MESSAGE = "fr.upmc.boteam.obo_app.services.action.SEND_MESSAGE";

    public static final String EXTRA_VIDEO = "fr.upmc.boteam.obo_app.services.extra.VIDEO";
    public static final String EXTRA_MESSAGE = "fr.upmc.boteam.obo_app.services.extra.MESSAGE";

    public static final String videoDirectory = "/storage/emulated/0/OBOApp/";

    FileInputStream fis = null;
    BufferedInputStream bis = null;

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final String action = intent.getAction();

            assert action != null;
            switch (action) {
                case ACTION_SEND_VIDEO:
                    handleActionSendVideo(intent.getStringExtra(EXTRA_VIDEO));
                    Log.i(LOG_TAG, "ACTION_SEND_VIDEO");
                    //Log.i("DC_SS : ", intent.getStringExtra(EXTRA_VIDEO));
                    break;
                case ACTION_SEND_MESSAGE:
                    handleActionSendMessage(intent.getStringArrayExtra(EXTRA_MESSAGE));
                    Log.i(LOG_TAG, "ACTION_SEND_MESSAGE");
                    break;
            }
        }
    }

    /**
     * Handle action Send Video in the provided background thread with the provided
     * parameter.
     */
    private void handleActionSendVideo(String param) {
        final String videoName = param;
        String videoPath = videoDirectory + param;
        int BUFFER_LENGHT = 65000;
        int n;
        File videoFile = new File(videoPath);

        try {

            byte[] buffer = new byte[BUFFER_LENGHT];
            //int bytesRead  = 0;
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(videoPath));
            DatagramPacket packet;

            while ((/*bytesRead = */in.read(buffer, 0, BUFFER_LENGHT)) != -1)
            {
                packet = new DatagramPacket(buffer, BUFFER_LENGHT, InetAddress.getByName(MainActivity.SERVER_ADDRESS), MainActivity.UDP_PORT);
                Client.s.send(packet);
                packet = new DatagramPacket(buffer, BUFFER_LENGHT);
                Client.s.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                Log.i("ServerService : ", received);
            }
            buffer = "EOF".getBytes();
            packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(MainActivity.SERVER_ADDRESS), MainActivity.UDP_PORT);
            Client.s.send(packet);
            packet = new DatagramPacket(buffer, buffer.length);
            Client.s.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            Log.i("ServerService : ", received);
            if(received.equals("VWR")) {

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), videoName + " well inserted in the database", Toast.LENGTH_SHORT).show();
                    }
                });

                videoFile.delete();

            } else {

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), videoName + " NOT inserted in the database", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle action Send Message in the provided background thread with the provided
     * parameter.
     */
    private void handleActionSendMessage(String[] message) {


        try {
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(Client.socket.getOutputStream())));
            out.print(message[0] + "/" + message[1]);
            out.flush();

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Association request sent", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.i(LOG_TAG, "SENDMESSAGE " + e.getMessage());
        }
    }
}
