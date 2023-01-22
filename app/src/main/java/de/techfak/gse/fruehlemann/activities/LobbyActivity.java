package de.techfak.gse.fruehlemann.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.techfak.gse.fruehlemann.R;

public class LobbyActivity extends AppCompatActivity {
    String url;
    ArrayList<String> maps = new ArrayList<>();
    String mapName;
    String playerName;
    int gameId;
    String playerToken;
    RequestQueue queue;
    ScheduledExecutorService executorService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        url = getIntent().getStringExtra("url");
        queue = Volley.newRequestQueue(this);

        getMapsFromServer();
    }

    /**
     * Call to get the names of all available maps from the server.
     */
    public void getMapsFromServer() {
        StringRequest request = buildGetMapRequest();

        queue.add(request);
    }

    /**
     * Gets all the names of available maps from the server and saves them in global variable maps.
     * @return StringRequest object of Request to the server.
     */
    public StringRequest buildGetMapRequest() {
        Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);
        String getMapUrl = url + "/maps";

        Response.Listener<String> onResponse = response -> {
            response = response.substring(1, response.length()-1);
            String[] responseSplit = response.split(",");
            for (String map : responseSplit) {
                maps.add(map.replace("\"", ""));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, maps);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mapSelectSpinnerMultiplayer.setAdapter(adapter);
        };
        Response.ErrorListener onError = error -> {
            Toast.makeText(this, "Ein Fehler ist aufgetreten", Toast.LENGTH_SHORT).show();
        };

        StringRequest request = new StringRequest(Request.Method.GET, getMapUrl, onResponse, onError);

        return request;
    }

    public void onPlayMXClick(View view) {
        TextView textName = findViewById(R.id.textName);
        TextView textSelectMap = findViewById(R.id.textSelectMap);
        EditText textPlayerName = findViewById(R.id.textPlayerName);
        Button playMXButton = findViewById(R.id.playMXButton);
        Button playDetectiveButton = findViewById(R.id.playDetectiveButton);
        Button createGameButton = findViewById(R.id.createGameButton);
        Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);

        if (!textPlayerName.getText().toString().equals("")) {

            playerName = Objects.requireNonNull(textPlayerName.getText().toString());

            textName.setVisibility(View.INVISIBLE);
            textPlayerName.setVisibility(View.INVISIBLE);
            playMXButton.setVisibility(View.INVISIBLE);
            playDetectiveButton.setVisibility(View.INVISIBLE);
            mapSelectSpinnerMultiplayer.setVisibility(View.VISIBLE);
            createGameButton.setVisibility(View.VISIBLE);
            textSelectMap.setVisibility(View.VISIBLE);
        }
    }

    public void onCreateGameClick(View view) {
        Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);
        Button createGameButton = findViewById(R.id.createGameButton);

        mapName = Objects.requireNonNull(mapSelectSpinnerMultiplayer.getSelectedItem().toString());

        StringRequest request = buildCreateGameRequest(mapName, playerName);

        queue.add(request);

        createGameButton.setVisibility(View.INVISIBLE);
    }

    public StringRequest buildCreateGameRequest(String mapName, String playerName) {
        String createGameUrl = url + "/games";

        Response.Listener<String> onResponse = response -> {
            TextView textWaitingHeader = findViewById(R.id.textWaitingHeader);
            TextView textGameId = findViewById(R.id.textGameId);
            TextView textMapId = findViewById(R.id.textMapId);
            TextView textShowPlayers = findViewById(R.id.textShowPlayers);
            TextView textSelectMap = findViewById(R.id.textSelectMap);
            Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);

            String[] responseSplit = response.split(",");
            String[] gameIdSplit = responseSplit[0].split(":");
            String[] playerTokenSplit = responseSplit[12].split(":");

            gameId = Integer.parseInt(gameIdSplit[1]);
            playerToken = playerTokenSplit[1].replace("\"", "");

            textWaitingHeader.setVisibility(View.VISIBLE);
            textGameId.setVisibility(View.VISIBLE);
            textMapId.setVisibility(View.VISIBLE);
            textShowPlayers.setVisibility(View.VISIBLE);
            textSelectMap.setVisibility(View.INVISIBLE);
            mapSelectSpinnerMultiplayer.setVisibility(View.INVISIBLE);


            refreshWaitingLobby();
        };
        Response.ErrorListener onError = error -> {
            Toast.makeText(this, "Ein Fehler ist aufgetreten", Toast.LENGTH_SHORT).show();
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

    public void refreshWaitingLobby() {
        executorService = Executors.newScheduledThreadPool(1);

        StringRequest request = buildGetWaitinglobbyInfo();

        executorService.scheduleWithFixedDelay(() -> queue.add(request), 0, 1, TimeUnit.SECONDS);
    }

    public StringRequest buildGetWaitinglobbyInfo() {
        TextView textGameId = findViewById(R.id.textGameId);
        TextView textMapId = findViewById(R.id.textMapId);
        TextView textShowPlayers = findViewById(R.id.textShowPlayers);
        String getGameInfosUrl = url + "/games/" + gameId;

        Response.Listener<String> onResponse = response -> {
            response = response.substring(1, response.length()-1);
            String[] responseSplit = response.split(",");

            String[] gameIdSplit = responseSplit[0].split(":");
            String[] mapIdSplit = responseSplit[1].split(":");
            String[] gameStatus = responseSplit[2].split(":");
            ArrayList<String[]> allPlayers = new ArrayList<>();
            for (int i = 0; i < responseSplit.length; i++) {
                if (responseSplit[i].startsWith("\"name\":")) {
                    int j = 0;
                    while (!responseSplit[j].startsWith("\"type\":")) {
                        j++;
                    }
                    String[] playerNameSplit = responseSplit[i].split(":");
                    String[] playerRoleSplit = responseSplit[j].split(":");

                    String[] playerInfo = {playerNameSplit[1].replace("\"", ""), playerRoleSplit[1].replace("\"", "")};
                    allPlayers.add(playerInfo);
                }
            }

            textGameId.setText("Spiel ID:\n" + gameIdSplit[1]);
            textMapId.setText("Karten ID:\n" + mapIdSplit[1].replace("\"", ""));
            mapName = mapIdSplit[1].replace("\"", "");

            textShowPlayers.setText("Mitspieler");
            for (String[] playerInfo : allPlayers) {
                textShowPlayers.setText(textShowPlayers.getText().toString() + "\n" + playerInfo[0] + " (" + playerInfo[1] + ")");
            }

            if (gameStatus[1].equals("\"RUNNING\"")) {
                startGame();
            }
        };
        Response.ErrorListener onError = error -> {
            Toast.makeText(this, "Ein Fehler ist aufgetreten", Toast.LENGTH_SHORT).show();
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

    public void startGame() {
        TextView textWaitingHeader = findViewById(R.id.textWaitingHeader);
        TextView textGameId = findViewById(R.id.textGameId);
        TextView textMapId = findViewById(R.id.textMapId);
        TextView textShowPlayers = findViewById(R.id.textShowPlayers);

        textWaitingHeader.setVisibility(View.INVISIBLE);
        textGameId.setVisibility(View.INVISIBLE);
        textMapId.setVisibility(View.INVISIBLE);
        textShowPlayers.setVisibility(View.INVISIBLE);

        executorService.shutdown();

        StringRequest request = buildGetMapInfo();

        queue.add(request);
    }

    public StringRequest buildGetMapInfo() {
        String getMapInfosUrl = url + "/maps/" + mapName;

        Response.Listener<String> onResponse = response -> {
            String mapContent = response;

            Intent gameI = new Intent(LobbyActivity.this, GameActivity.class);
            gameI.putExtra("map", mapName);
            gameI.putExtra("mapContent", mapContent);
            startActivity(gameI);
        };
        Response.ErrorListener onError = error -> {
            Toast.makeText(this, "Ein Fehler ist aufgetreten", Toast.LENGTH_SHORT).show();
        };

        StringRequest request = new StringRequest(Request.Method.GET, getMapInfosUrl, onResponse, onError);

        return request;
    }

    public void onPlayDetectiveClick(View view) {
        TextView textName = findViewById(R.id.textName);
        TextView textInputGameIdHeader = findViewById(R.id.textInputGameIdHeader);
        EditText textInputGameId = findViewById(R.id.textInputGameId);
        EditText textPlayerName = findViewById(R.id.textPlayerName);
        Button playMXButton = findViewById(R.id.playMXButton);
        Button playDetectiveButton = findViewById(R.id.playDetectiveButton);
        Button connectButton = findViewById(R.id.connectButton);

        if (!textPlayerName.getText().toString().equals("")) {

            playerName = Objects.requireNonNull(textPlayerName.getText().toString());

            textName.setVisibility(View.INVISIBLE);
            textPlayerName.setVisibility(View.INVISIBLE);
            playMXButton.setVisibility(View.INVISIBLE);
            playDetectiveButton.setVisibility(View.INVISIBLE);
            textInputGameIdHeader.setVisibility(View.VISIBLE);
            textInputGameId.setVisibility(View.VISIBLE);
            connectButton.setVisibility(View.VISIBLE);
        }
    }

    public void onConnectClick(View view) {
        EditText textInputGameId = findViewById(R.id.textInputGameId);

        if (!textInputGameId.getText().toString().equals("")) {

            gameId = Integer.valueOf(textInputGameId.getText().toString());

            StringRequest request = buildConnectRequest();

            queue.add(request);
        }
    }

    public StringRequest buildConnectRequest() {
        String connectUrl = url + "/games/" + gameId + "/players";

        Response.Listener<String> onResponse = response -> {
            TextView textGameId = findViewById(R.id.textGameId);
            TextView textMapId = findViewById(R.id.textMapId);
            TextView textShowPlayers = findViewById(R.id.textShowPlayers);
            TextView textWaitingHeader = findViewById(R.id.textWaitingHeader);
            TextView textInputGameIdHeader = findViewById(R.id.textInputGameIdHeader);
            EditText textInputGameId = findViewById(R.id.textInputGameId);
            Button connectButton = findViewById(R.id.connectButton);

            String[] responseSplit = response.split(",");
            String[] gameIdSplit = responseSplit[1].split(":");
            String[] playerTokenSplit = responseSplit[3].split(":");

            playerToken = playerTokenSplit[1].replace("\"", "");

            textWaitingHeader.setVisibility(View.VISIBLE);
            textGameId.setVisibility(View.VISIBLE);
            textMapId.setVisibility(View.VISIBLE);
            textShowPlayers.setVisibility(View.VISIBLE);
            textInputGameIdHeader.setVisibility(View.INVISIBLE);
            textInputGameId.setVisibility(View.INVISIBLE);
            connectButton.setVisibility(View.INVISIBLE);

            refreshWaitingLobby();
        };
        Response.ErrorListener onError = error -> {
            Toast.makeText(this, "Ein Fehler ist aufgetreten", Toast.LENGTH_SHORT).show();
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
}
