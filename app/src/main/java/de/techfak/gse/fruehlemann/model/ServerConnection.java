package de.techfak.gse.fruehlemann.model;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerConnection {
    String url;
    ArrayList<String> maps;
    String mapName;
    String playerName;
    int gameId;
    String playerToken;
    String connectionStatus;
    String gameStatus;
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

    public String getGameStatus() {
        return gameStatus;
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

    //Requests player plays M. X
    public void getMapsFromServer() {
        StringRequest request = buildGetMapsRequest();

        queue.add(request);
    }

    public StringRequest buildGetMapsRequest() {
        String getMapUrl = url + "/maps";

        Response.Listener<String> onResponse = response -> {
            maps = new ArrayList<>();

            response = response.substring(1, response.length()-1);
            String[] responseSplit = response.split(",");
            for (String map : responseSplit) {
                maps.add(map.replace("\"", ""));
            }

            String mapsString = "";
            for (int i = 0; i < maps.size(); i++) {
                if (i != 0) {
                    mapsString += "," + maps.get(i);
                } else {
                    mapsString = maps.get(i);
                }
            }
            this.support.firePropertyChange("maps", "", mapsString);
        };
        Response.ErrorListener onError = error -> {
            String mapsStatus = error.getCause().getMessage();
            this.support.firePropertyChange("connection", "", "Fehler: " + mapsStatus);
        };

        StringRequest request = new StringRequest(Request.Method.GET, getMapUrl, onResponse, onError);

        return request;
    }


    public void createGameOnServer(String mapName, String playerName) {
        this.mapName = mapName;
        this.playerName = playerName;

        StringRequest request = buildCreateGameRequest();

        queue.add(request);
    }

    public StringRequest buildCreateGameRequest() {
        String createGameUrl = url + "/games";

        Response.Listener<String> onResponse = response -> {
            String[] responseSplit = response.split(",");
            String[] gameIdSplit = responseSplit[0].split(":");
            String[] playerTokenSplit = responseSplit[7].split(":");

            gameId = Integer.parseInt(gameIdSplit[1].replace("\"", ""));
            playerToken = playerTokenSplit[1].replace("\"", "");

            this.support.firePropertyChange("gameCreate", "", "200");
        };
        Response.ErrorListener onError = error -> {
            String createGameStatus = error.getCause().getMessage();
            this.support.firePropertyChange("gameCreate", "", createGameStatus);
        };

        StringRequest request = new StringRequest(Request.Method.POST, createGameUrl, onResponse, onError) {
            @Override
            public byte[] getBody() {
                try {
                    final String encodedMapName = URLEncoder.encode(mapName, getParamsEncoding());
                    final String encodedPlayerName = URLEncoder.encode(playerName, getParamsEncoding());
                    final String body = "{\"mapName\":\"" + encodedMapName + "\",\"playerName\":\"" + encodedPlayerName + "\"}";
                    return body.getBytes(getParamsEncoding());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        return request;
    }


    public void getWaitingRoomInfo() {
        executorService = Executors.newScheduledThreadPool(1);

        StringRequest request = buildGetWaitinglobbyInfoRequest();

        executorService.scheduleWithFixedDelay(() -> queue.add(request), 0, 1, TimeUnit.SECONDS);
    }

    public StringRequest buildGetWaitinglobbyInfoRequest() {
        String getGameInfosUrl = url + "/games/" + gameId;

        Response.Listener<String> onResponse = response -> {
            response = response.substring(1, response.length()-1);
            String[] responseSplit = response.split(",");

            String[] gameIdSplit = responseSplit[0].split(":");
            String[] mapIdSplit = responseSplit[1].split(":");
            String[] gameStatusSplit = responseSplit[2].split(":");
            ArrayList<String> allPlayers = new ArrayList<>();

            for (int i = 0; i < responseSplit.length; i++) {
                if (responseSplit[i].startsWith("\"name\":")) {
                    int j = i;
                    while (!responseSplit[j].startsWith("\"type\":")) {
                        j++;
                    }
                    String[] playerNameSplit = responseSplit[i].split(":");
                    String[] playerRoleSplit = responseSplit[j].split(":");

                    String playerInfo = playerNameSplit[1].replace("\"", "") + " (" + playerRoleSplit[1].replace("\"", "") + ")";
                    allPlayers.add(playerInfo);
                }
            }

            String playersString = "";
            for (int i = 0; i < allPlayers.size(); i++) {
                if (i != 0) {
                    playersString += "\n" + allPlayers.get(i);
                } else {
                    playersString = allPlayers.get(i);
                }
            }

            mapName = mapIdSplit[1].replace("\"", "");
            gameId = Integer.valueOf(gameIdSplit[1].replace("\"", ""));
            gameStatus = gameStatusSplit[1].replace("\"", "");

            if (gameStatus.equals("RUNNING")) {
                executorService.shutdown();
            }

            this.support.firePropertyChange("waitingLobbyGameId", "", gameId);
            this.support.firePropertyChange("waitingLobbyMap", "", mapName);
            this.support.firePropertyChange("waitingLobbyPlayers", "", playersString);
            this.support.firePropertyChange("waitingLobbyGameStatus", "", gameStatus);
        };
        Response.ErrorListener onError = error -> {
            String waitingStatus = error.getCause().getMessage();
            this.support.firePropertyChange("waitingLobbyError", "", "Fehler: " + waitingStatus);
        };

        StringRequest request = new StringRequest(Request.Method.GET, getGameInfosUrl, onResponse, onError) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-PLAYER-TOKEN", playerToken);
                return headers;
            }
        };

        return request;
    }


    //Requests player plays Detective
    public void connectToGame(int gameId, String playerName) {
        this.gameId = gameId;
        this.playerName = playerName;

        StringRequest request = buildConnectGameRequest();

        queue.add(request);
    }

    public StringRequest buildConnectGameRequest() {
        String connectUrl = url + "/games/" + gameId + "/players";

        Response.Listener<String> onResponse = response -> {
            String[] responseSplit = response.split(",");
            String[] playerTokenSplit = responseSplit[3].split(":");

            playerToken = playerTokenSplit[1].replace("\"", "");

            this.support.firePropertyChange("connectGame", "", "200");
        };
        Response.ErrorListener onError = error -> {
            String createGameStatus = error.getCause().getMessage();
            this.support.firePropertyChange("connectGame", "", createGameStatus);
        };

        StringRequest request = new StringRequest(Request.Method.POST, connectUrl, onResponse, onError) {
            @Override
            public byte[] getBody() {
                try {
                    final String encodedPlayerName = URLEncoder.encode(playerName, getParamsEncoding());
                    final String body = "{\"playerName\":\"" + encodedPlayerName + "\"}";
                    return body.getBytes(getParamsEncoding());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        return request;
    }



    //Requests from all players
    public void getMapInfo() {
        StringRequest request = buildGetMapInfoRequest();

        queue.add(request);
    }

    public StringRequest buildGetMapInfoRequest() {
        String getMapInfosUrl = url + "/maps/" + mapName;

        Response.Listener<String> onResponse = response -> {
            String mapContent = response;

            this.support.firePropertyChange("mapInfo", "", mapContent);
        };
        Response.ErrorListener onError = error -> {
            String mapInfoStatus = error.getCause().getMessage();
            this.support.firePropertyChange("mapInfo", "", "Fehler: " + mapInfoStatus);
        };

        StringRequest request = new StringRequest(Request.Method.GET, getMapInfosUrl, onResponse, onError);

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
