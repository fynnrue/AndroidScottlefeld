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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import de.techfak.gse.fruehlemann.R;
import de.techfak.gse.fruehlemann.model.GameApplication;

public class LobbyActivity extends AppCompatActivity implements PropertyChangeListener {
    String url;
    String mapName;
    String playerName;
    int gameId;
    String playerToken;
    RequestQueue queue;
    GameApplication gameApplication;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        gameApplication = (GameApplication) getApplication();
        gameApplication.getServerConnection().addListener(this);

        url = getIntent().getStringExtra("url");
        queue = Volley.newRequestQueue(this);

        getMapsFromServer();
    }

    @Override
    public void onDestroy() {
        gameApplication.getServerConnection().removeListener(this);
        super.onDestroy();
    }

    /**
     * Call to get the names of all available maps from the server.
     */
    public void getMapsFromServer() {
        gameApplication.getServerConnection().getMapsFromServer();
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

        if (!mapSelectSpinnerMultiplayer.getSelectedItem().toString().equals("")) {

            mapName = Objects.requireNonNull(mapSelectSpinnerMultiplayer.getSelectedItem().toString());

            gameApplication.getServerConnection().createGameOnServer(mapName, playerName);
        }
    }

    public void refreshWaitingLobby() {
        gameApplication.getServerConnection().getWaitingRoomInfo();
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

        gameApplication.getServerConnection().getMapInfo();
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
            Toast.makeText(this, "Ein Fehler ist aufgetreten.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String propertyChangeName = propertyChangeEvent.getPropertyName();

        if (propertyChangeName.equals("maps")) {
            String mapsStatus = propertyChangeEvent.getNewValue().toString();

            if (!mapsStatus.startsWith("Fehler: ")) {
                Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);

                String[] maps = mapsStatus.split(",");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, maps);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mapSelectSpinnerMultiplayer.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Ein Fehler ist aufgetreten.\n" + mapsStatus, Toast.LENGTH_SHORT).show();
            }
        } else if (propertyChangeName.equals("gameCreate")) {
                String createGameStatus = propertyChangeEvent.getNewValue().toString();

                if (createGameStatus.equals("200")) {
                    TextView textWaitingHeader = findViewById(R.id.textWaitingHeader);
                    TextView textGameId = findViewById(R.id.textGameId);
                    TextView textMapId = findViewById(R.id.textMapId);
                    TextView textShowPlayers = findViewById(R.id.textShowPlayers);
                    TextView textSelectMap = findViewById(R.id.textSelectMap);
                    Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);
                    Button createGameButton = findViewById(R.id.createGameButton);

                    textWaitingHeader.setVisibility(View.VISIBLE);
                    textGameId.setVisibility(View.VISIBLE);
                    textMapId.setVisibility(View.VISIBLE);
                    textShowPlayers.setVisibility(View.VISIBLE);
                    textSelectMap.setVisibility(View.INVISIBLE);
                    mapSelectSpinnerMultiplayer.setVisibility(View.INVISIBLE);
                    createGameButton.setVisibility(View.INVISIBLE);

                    refreshWaitingLobby();
                } else {
                    Toast.makeText(this, "Ein Fehler ist aufgetreten.\n" + createGameStatus, Toast.LENGTH_SHORT).show();
                }
        } else if (propertyChangeName.equals("waitingLobbyGameId")) {
            String gameId = propertyChangeEvent.getNewValue().toString();

            TextView textGameId = findViewById(R.id.textGameId);

            textGameId.setText("Spiel ID:\n" + gameId);
        } else if (propertyChangeName.equals("waitingLobbyMap")) {
            String mapName = propertyChangeEvent.getNewValue().toString();

            TextView textMapId = findViewById(R.id.textMapId);

            textMapId.setText("Karten ID:\n" + mapName);
        } else if (propertyChangeName.equals("waitingLobbyPlayers")) {
                String allPlayers = propertyChangeEvent.getNewValue().toString();

                TextView textShowPlayers = findViewById(R.id.textShowPlayers);

                textShowPlayers.setText("Mitspieler:\n" + allPlayers);
        } else if (propertyChangeName.equals("waitingLobbyGameStatus")) {
            String gameStatus = propertyChangeEvent.getNewValue().toString();

            if (gameStatus.equals("RUNNING")) {
                startGame();
            }
        } else if (propertyChangeName.equals("waitingLobbyError")) {
                String waitingLobbyError = propertyChangeEvent.getNewValue().toString();

                Toast.makeText(this, "Ein Fehler ist aufgetreten.\n" + waitingLobbyError, Toast.LENGTH_SHORT).show();
        } else if (propertyChangeName.equals("mapInfo")) {
            String mapInfo = propertyChangeEvent.getNewValue().toString();

            if (!mapInfo.startsWith("Fehler: ")) {
                Intent gameI = new Intent(LobbyActivity.this, GameActivity.class);
                gameI.putExtra("map", mapName);
                gameI.putExtra("mapContent", mapInfo);
                startActivity(gameI);
            } else {
                Toast.makeText(this, "Ein Fehler ist aufgetreten.\n", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
