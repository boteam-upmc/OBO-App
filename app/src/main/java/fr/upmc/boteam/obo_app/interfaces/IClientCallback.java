package fr.upmc.boteam.obo_app.interfaces;


public interface IClientCallback {

    void onMessage(String message);

    void onConnect();

    void onDisconnect();

    void onConnectError(String message);
}
