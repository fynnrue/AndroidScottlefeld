package de.techfak.gse.fruehlemann.model;

import com.android.volley.RequestQueue;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

public class ServerConnection {
    String url;
    ArrayList<String> maps;
    String mapName;
    String playerName;
    int gameId;
    String playerToken;
    RequestQueue queue;
    ScheduledExecutorService executorService;

    public ServerConnection(String url) {
        this.url = url;
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

    public RequestQueue getQueue() {
        return queue;
    }
}
