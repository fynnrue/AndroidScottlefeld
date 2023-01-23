package de.techfak.gse.fruehlemann.model;

import android.app.Application;

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
