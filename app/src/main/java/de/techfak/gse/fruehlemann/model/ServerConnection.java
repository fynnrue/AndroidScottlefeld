package de.techfak.gse.fruehlemann.model;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

public class ServerConnection {
    String url;
    ArrayList<String> maps;
    String mapName;
    String playerName;
    int gameId;
    String playerToken;
    String connectionStatus;
    RequestQueue queue;
    ScheduledExecutorService executorService;
    Context context;
    private PropertyChangeSupport support;

    public ServerConnection(String url, Context context) {
        this.url = url;
        this.context = context;

        queue = Volley.newRequestQueue(this.context);
        this.support = new PropertyChangeSupport(this);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<String> getMaps() {
        return maps;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public RequestQueue getQueue() {
        return queue;
    }


    // Requests to server
    public void connectToServer() {
        StringRequest request = buildConnectionRequest(url);

        queue.add(request);
    }

    public StringRequest buildConnectionRequest(String url) {
        String checkConnectionUrl = url + "/";
        Response.Listener<String> onResponse = response -> {
            setUrl(url);
            if (response.equals("Scottlefeld")) {
                connectionStatus = "200";
            } else {
                connectionStatus = "201";
            }
            this.support.firePropertyChange("connection", "", connectionStatus);
        };
        Response.ErrorListener onError = error -> {
            connectionStatus = error.getCause().getMessage();
            setUrl(url);
            this.support.firePropertyChange("connection", "", connectionStatus);
        };

        StringRequest request = new StringRequest(Request.Method.GET, checkConnectionUrl, onResponse, onError);

        return request;
    }

    //PropertyChange
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
