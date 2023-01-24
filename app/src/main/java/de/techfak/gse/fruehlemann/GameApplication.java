package de.techfak.gse.fruehlemann;

import android.app.Application;

import de.techfak.gse.fruehlemann.model.ServerConnection;

public class GameApplication  extends Application {
    ServerConnection server;

    public GameApplication() {
    }

    public ServerConnection getServerConnection() {
        return server;
    }

    public void setServerConnection(ServerConnection server) {
        this.server = server;
    }
}
