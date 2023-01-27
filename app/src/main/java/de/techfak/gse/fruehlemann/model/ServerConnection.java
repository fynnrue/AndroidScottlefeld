package de.techfak.gse.fruehlemann.model;

import android.content.Context;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
    final String statusSuccessfull = "200";
    final String splitComma = ",";
    final String splitColon = ":";
    final String endRequestbody = "\"}";
    final String replaceQuote = "\"";
    final String urlGames = "/games/";
    final String requestBodyType = "application/json";

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

    /**
     * Called by LobbyActivity to connect with server.
     */
    public void connectToServer() {
        StringRequest request = buildConnectionRequest(url);

        queue.add(request);
    }

    /**
     * Builds request to connect with server. Depending on the server answer a different PropertyChange gets fired:
     *
     * Statuscode 200 and Text "Scottlefeld": 200
     * Statuscode 200 and Text not "Scottlefeld": 201
     * Statuscode not 200 and TimeoutError/NoConnectionError: Errormessage
     * Statuscode not 200: Errormessage from Server
     *
     * @param url url from server
     * @return answer from server to request as string
     */
    public StringRequest buildConnectionRequest(String url) {
        String checkConnectionUrl = url + "/";
        String propertyChange = "connection";

        Response.Listener<String> onResponse = response -> {
            setUrl(url);
            if (response.equals("Scottlefeld")) {
                connectionStatus = statusSuccessfull;
            } else {
                connectionStatus = "201";
            }
            this.support.firePropertyChange(propertyChange, "", connectionStatus);
        };
        Response.ErrorListener onError = error -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                this.support.firePropertyChange(propertyChange, "", "Server antwortet nicht beim Verbindung abrufen.");
            } else {
                connectionStatus = error.getCause().getMessage();
                setUrl(url);
                this.support.firePropertyChange(propertyChange, "", connectionStatus);
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, checkConnectionUrl, onResponse, onError);

        return request;
    }

    //Requests player plays M. X
    /**
     * Called by LobbyActivity get available maps from server.
     */
    public void getMapsFromServer() {
        StringRequest request = buildGetMapsRequest();

        queue.add(request);
    }

    /**
     * Builds request to get available maps server.
     * Depending on the server answer a different PropertyChange gets fired:
     *
     * Statuscode 200: String that contains all maps on server
     * Statuscode not 200 and TimeoutError/NoConnectionError: Errormessage
     * Statuscode not 200: Errormessage from Server
     */
    public StringRequest buildGetMapsRequest() {
        String getMapUrl = url + "/maps";
        String propertyChange = "maps";

        Response.Listener<String> onResponse = response -> {
            maps = new ArrayList<>();

            response = response.substring(1, response.length() - 1);
            String[] responseSplit = response.split(splitComma);
            for (String map : responseSplit) {
                maps.add(map.replace(replaceQuote, ""));
            }

            String mapsString = "";
            for (int i = 0; i < maps.size(); i++) {
                if (i != 0) {
                    mapsString += splitComma + maps.get(i);
                } else {
                    mapsString = maps.get(i);
                }
            }
            this.support.firePropertyChange(propertyChange, "", mapsString);
        };
        Response.ErrorListener onError = error -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                this.support.firePropertyChange(propertyChange, "",
                        "Fehler: Server antwortet nicht beim Abfragen der Karten.");
            } else {
                String mapsStatus = error.getCause().getMessage();
                this.support.firePropertyChange(propertyChange, "", "Fehler: MapAbfrage: " + mapsStatus);
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, getMapUrl, onResponse, onError);

        return request;
    }


    /**
     * Called by LobbyActivity create game on server.
     *
     * @param mapName map chosen in LobbyActivity
     * @param playerName name chosen in LobbyActivity
     */
    public void createGameOnServer(String mapName, String playerName) {
        this.mapName = mapName;
        this.playerName = playerName;

        StringRequest request = buildCreateGameRequest();

        queue.add(request);
    }

    /**
     * Builds request to create game on server. Depending on the server answer a different PropertyChange gets fired:
     *
     * Statuscode 200: 200
     * Statuscode not 200 and TimeoutError/NoConnectionError: Errormessage
     * Statuscode not 200: Errormessage from Server
     */
    public StringRequest buildCreateGameRequest() {
        String createGameUrl = url + "/games";
        String propertyChange = "gameCreate";

        Response.Listener<String> onResponse = response -> {
            final int indexPlayerToken = 16;

            String[] responseSplit = response.split(splitComma);
            String[] gameIdSplit = responseSplit[0].split(splitColon);
            String[] playerTokenSplit = responseSplit[indexPlayerToken].split(splitColon);

            gameId = Integer.parseInt(gameIdSplit[1].replace(replaceQuote, ""));
            playerToken = playerTokenSplit[1].replace(replaceQuote, "");

            this.support.firePropertyChange(propertyChange, "", statusSuccessfull);
        };
        Response.ErrorListener onError = error -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                this.support.firePropertyChange(propertyChange, "", "Server antwortet nicht beim Spiel erstellen.");
            } else {
                String createGameStatus = error.getCause().getMessage();
                this.support.firePropertyChange(propertyChange, "", createGameStatus);
            }
        };

        StringRequest request = new StringRequest(Request.Method.POST, createGameUrl, onResponse, onError) {
            @Override
            public byte[] getBody() {
                try {
                    final String encodedMapName = URLEncoder.encode(mapName, getParamsEncoding());
                    final String encodedPlayerName = URLEncoder.encode(playerName, getParamsEncoding());
                    final String body = "{\"mapName\":\"" + encodedMapName + "\",\"playerName\":\""
                            + encodedPlayerName + endRequestbody;
                    return body.getBytes(getParamsEncoding());
                } catch (UnsupportedEncodingException e) {
                    Log.i("Exception create", "Error when encoding player or map name to create a game.");
                    support.firePropertyChange("exceptionCreate", "", "createGameRequest");
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return requestBodyType;
            }
        };

        return request;
    }


    /**
     * Called by LobbyActivity to get waiting room information from server with delay of 1 sec.
     */
    public void getWaitingRoomInfo() {
        executorService = Executors.newScheduledThreadPool(1);

        StringRequest request = buildGetWaitinglobbyInfoRequest();

        executorService.scheduleWithFixedDelay(() -> queue.add(request), 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Builds request to get waitinglobby info from server.
     * Depending on the server answer a different PropertyChange gets fired:
     *
     * Statuscode 200: Multiple propertyChanges containing gameId, mapName, players and game status
     * Statuscode not 200 and TimeoutError/NoConnectionError: Errormessage
     * Statuscode not 200: Errormessage from Server
     */
    public StringRequest buildGetWaitinglobbyInfoRequest() {
        String getGameInfosUrl = url + urlGames + gameId;

        Response.Listener<String> onResponse = response -> {
            response = response.substring(1, response.length() - 1);
            String[] responseSplit = response.split(splitComma);

            String[] gameIdSplit = responseSplit[0].split(splitColon);
            String[] mapIdSplit = responseSplit[1].split(splitColon);
            String[] gameStatusSplit = responseSplit[2].split(splitColon);
            ArrayList<String> allPlayers = new ArrayList<>();

            for (int i = 0; i < responseSplit.length; i++) {
                if (responseSplit[i].startsWith("\"name\":")) {
                    int findPlayerRole = i;
                    while (!responseSplit[findPlayerRole].startsWith("\"type\":")) {
                        findPlayerRole++;
                    }
                    String[] playerNameSplit = responseSplit[i].split(splitColon);
                    String[] playerRoleSplit = responseSplit[findPlayerRole].split(splitColon);

                    String playerInfo = playerNameSplit[1].replace(replaceQuote, "") + " ("
                            + playerRoleSplit[1].replace(replaceQuote, "") + ")";
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

            mapName = mapIdSplit[1].replace(replaceQuote, "");
            gameId = Integer.valueOf(gameIdSplit[1].replace(replaceQuote, ""));
            gameStatus = gameStatusSplit[1].replace(replaceQuote, "");

            if (gameStatus.equals("RUNNING")) {
                executorService.shutdown();
            }

            this.support.firePropertyChange("waitingLobbyGameId", "", gameId);
            this.support.firePropertyChange("waitingLobbyMap", "", mapName);
            this.support.firePropertyChange("waitingLobbyPlayers", "", playersString);
            this.support.firePropertyChange("waitingLobbyGameStatus", "", gameStatus);
        };
        Response.ErrorListener onError = error -> {
            String propertyChange = "waitingLobbyError";

            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                this.support.firePropertyChange(propertyChange, "",
                        "Fehler: Server antwortet nicht beim Abfragen der Wartelobby Informationen.");
            } else {
                String waitingStatus = error.getCause().getMessage();
                this.support.firePropertyChange(propertyChange, "", "Fehler: Wartelobby: " + waitingStatus);
            }
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
    /**
     * Called by LobbyActivity to connect to game on server.
     */
    public void connectToGame(int gameId, String playerName) {
        this.gameId = gameId;
        this.playerName = playerName;

        StringRequest request = buildConnectGameRequest();

        queue.add(request);
    }

    /**
     * Builds request to connect to game on server.
     * Depending on the server answer a different PropertyChange gets fired:
     *
     * Statuscode 200: 200
     * Statuscode not 200 and TimeoutError/NoConnectionError: Errormessage
     * Statuscode not 200: Errormessage from Server
     */
    public StringRequest buildConnectGameRequest() {
        String connectUrl = url + urlGames + gameId + "/players";
        String propertyChange = "connectGame";

        Response.Listener<String> onResponse = response -> {
            final int indexPlayerToken = 6;

            String[] responseSplit = response.split(splitComma);
            String[] playerTokenSplit = responseSplit[indexPlayerToken].split(splitColon);

            playerToken = playerTokenSplit[1].replace(replaceQuote, "");

            this.support.firePropertyChange(propertyChange, "", statusSuccessfull);
        };
        Response.ErrorListener onError = error -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                this.support.firePropertyChange(propertyChange, "",
                        "Server antwortet nicht beim Verbinden als Detective.");
            } else {
                String createGameStatus = error.getCause().getMessage();
                this.support.firePropertyChange(propertyChange, "", createGameStatus);
            }
        };

        StringRequest request = new StringRequest(Request.Method.POST, connectUrl, onResponse, onError) {
            @Override
            public byte[] getBody() {
                try {
                    final String encodedPlayerName = URLEncoder.encode(playerName, getParamsEncoding());
                    final String body = "{\"playerName\":\"" + encodedPlayerName + endRequestbody;
                    return body.getBytes(getParamsEncoding());
                } catch (UnsupportedEncodingException e) {
                    Log.i("Exception connect", "Error when encoding player name to connect to game.");
                    support.firePropertyChange("exceptionJoin", "", "connectRequest");
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return requestBodyType;
            }
        };

        return request;
    }



    //Requests from all players
    /**
     * Called by LobbyActivity to get map content from server.
     */
    public void getMapInfo() {
        StringRequest request = buildGetMapInfoRequest();

        queue.add(request);
    }

    /**
     * Builds request to get map content server. Depending on the server answer a different PropertyChange gets fired:
     *
     * Statuscode 200: Content of chosen map
     * Statuscode not 200 and TimeoutError/NoConnectionError: Errormessage
     * Statuscode not 200: Errormessage from Server
     */
    public StringRequest buildGetMapInfoRequest() {
        String getMapInfosUrl = url + "/maps/" + mapName;
        String propertyChange = "mapInfo";

        Response.Listener<String> onResponse = response -> {
            String mapContent = response;

            this.support.firePropertyChange(propertyChange, "", mapContent);
        };
        Response.ErrorListener onError = error -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                this.support.firePropertyChange(propertyChange, "",
                        "Fehler: Server antwortet nicht beim Abfragen der Map Infos.");
            } else {
                String mapInfoStatus = error.getCause().getMessage();
                this.support.firePropertyChange(propertyChange, "", "Fehler: Map Info parse: " + mapInfoStatus);
            }
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
