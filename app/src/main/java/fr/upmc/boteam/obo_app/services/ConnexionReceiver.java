package fr.upmc.boteam.obo_app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.widget.Toast;


public class ConnexionReceiver extends BroadcastReceiver {

    private String tag = "ConnexionReceiver";
    NetworkInfo networkInfo;
    State state;
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {

        //Intent intente = new Intent(VideoCapture.class);


        this.networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        this.context=context;
        if (networkInfo != null) {
            State state = networkInfo.getState();
            String typeName = networkInfo.getTypeName();
            networkType();
            networkState();

            //context.getApplicationContext().startActivity(intent);

         /*   Toast.makeText(context.getApplicationContext(), "type: "+typeName+"state: "+state,
                    Toast.LENGTH_LONG).show();
            Log.d(tag, "connexion data change NetworkInfo:" + networkInfo);
            Log.d(tag, "connexion data change NetworkInfo::State: " + state);
            Log.d(tag, "connexion data change NetworkInfo::TypeName: " + typeName);*/
        }
    }
    public State networkState() {

        //  if (networkInfo != null) {
        State state = networkInfo.getState();

        if(state== State.DISCONNECTED){
            String typeName = networkInfo.getTypeName();
            Toast.makeText(context.getApplicationContext(), "Pas d'accé internet",
                    Toast.LENGTH_LONG).show();
            return State.DISCONNECTED;
        }else return State.CONNECTED;

        // }
    }
    public State networkType() {

        //  if (net;workInfo != null) {
        String type = networkInfo.getTypeName();

        if(type.equals("MOBILE")){
            String typeName = networkInfo.getTypeName();
            Toast.makeText(context.getApplicationContext(), "Attention type de connexion Mobile ",
                    Toast.LENGTH_LONG).show();
        }
        return State.CONNECTED;

        // }
    }

}
