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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import de.techfak.gse.fruehlemann.R;
import de.techfak.gse.fruehlemann.GameApplication;

public class LobbyActivity extends AppCompatActivity implements PropertyChangeListener {
    String url;
    GameApplication gameApplication;

    final String statusSuccessfull = "200";
    final String statusErrorOccured = "Fehler: ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        gameApplication = (GameApplication) getApplication();
        gameApplication.getServerConnection().addListener(this);

        url = getIntent().getStringExtra("url");

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

    /**
     * When play M. X button is pressed and chosen name is not empty, view changes to select map and create game.
     *
     * @param view current view
     */
    public void onPlayMXClick(View view) {
        TextView textName = findViewById(R.id.textName);
        TextView textSelectMap = findViewById(R.id.textSelectMap);
        EditText textPlayerName = findViewById(R.id.textPlayerName);
        Button playMXButton = findViewById(R.id.playMXButton);
        Button playDetectiveButton = findViewById(R.id.playDetectiveButton);
        Button createGameButton = findViewById(R.id.createGameButton);
        Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);

        if (!textPlayerName.getText().toString().equals("")) {
            textName.setVisibility(View.INVISIBLE);
            textPlayerName.setVisibility(View.INVISIBLE);
            playMXButton.setVisibility(View.INVISIBLE);
            playDetectiveButton.setVisibility(View.INVISIBLE);
            mapSelectSpinnerMultiplayer.setVisibility(View.VISIBLE);
            createGameButton.setVisibility(View.VISIBLE);
            textSelectMap.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When create game button is pressed and a map is selected,
     * server gets asked to create game through ServerConnection.
     *
     * @param view current view
     */
    public void onCreateGameClick(View view) {
        Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);
        EditText textPlayerName = findViewById(R.id.textPlayerName);

        if (!mapSelectSpinnerMultiplayer.getSelectedItem().toString().equals("")) {

            String mapName = Objects.requireNonNull(mapSelectSpinnerMultiplayer.getSelectedItem().toString());
            String playerName = Objects.requireNonNull(textPlayerName.getText().toString());

            gameApplication.getServerConnection().createGameOnServer(mapName, playerName);
        }
    }

    /**
     * Server gets asked to send information about lobby through ServerConnection.
     */
    public void refreshWaitingLobby() {
        gameApplication.getServerConnection().getWaitingRoomInfo();
    }

    /**
     * View changes when game starts to hide unnecessary information.
     */
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

    /**
     * When play detective button is pressed and a player name is entered,
     * view changes to input gameId and connect to game.
     *
     * @param view current view
     */
    public void onPlayDetectiveClick(View view) {
        TextView textName = findViewById(R.id.textName);
        TextView textInputGameIdHeader = findViewById(R.id.textInputGameIdHeader);
        EditText textInputGameId = findViewById(R.id.textInputGameId);
        EditText textPlayerName = findViewById(R.id.textPlayerName);
        Button playMXButton = findViewById(R.id.playMXButton);
        Button playDetectiveButton = findViewById(R.id.playDetectiveButton);
        Button connectButton = findViewById(R.id.connectButton);

        if (!textPlayerName.getText().toString().equals("")) {
            textName.setVisibility(View.INVISIBLE);
            textPlayerName.setVisibility(View.INVISIBLE);
            playMXButton.setVisibility(View.INVISIBLE);
            playDetectiveButton.setVisibility(View.INVISIBLE);
            textInputGameIdHeader.setVisibility(View.VISIBLE);
            textInputGameId.setVisibility(View.VISIBLE);
            connectButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When connect button is pressed and a gameId is entered,
     * server gets asked to create new detective player and puts him into the game through ServerConnection.
     *
     * @param view current view
     */
    public void onConnectClick(View view) {
        EditText textInputGameId = findViewById(R.id.textInputGameId);
        EditText textPlayerName = findViewById(R.id.textPlayerName);

        if (!textInputGameId.getText().toString().equals("")) {

            int gameId = Integer.valueOf(textInputGameId.getText().toString());
            String playerName = textPlayerName.getText().toString();

            gameApplication.getServerConnection().connectToGame(gameId, playerName);
        }
    }

    /**
     * Depending on the propertyChangeEvent the view changes and/or methods are called.
     */
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String propertyChangeName = propertyChangeEvent.getPropertyName();

        if (propertyChangeName.equals("maps")) {
            String mapsStatus = propertyChangeEvent.getNewValue().toString();

            if (!mapsStatus.startsWith(statusErrorOccured)) {
                Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);

                String[] maps = mapsStatus.split(",");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, maps);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mapSelectSpinnerMultiplayer.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Ein Fehler beim Auslesen der Karten ist aufgetreten.\n" + mapsStatus,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (propertyChangeName.equals("gameCreate")) {
                String createGameStatus = propertyChangeEvent.getNewValue().toString();

                if (createGameStatus.equals(statusSuccessfull)) {
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
                    Toast.makeText(this, "Ein Fehler beim Erstellen des Spiels ist aufgetreten.\n" + createGameStatus,
                            Toast.LENGTH_SHORT).show();
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

                Toast.makeText(this, "Ein Fehler beim Laden der Lobby Informationen ist aufgetreten.\n"
                        + waitingLobbyError, Toast.LENGTH_SHORT).show();
        } else if (propertyChangeName.equals("mapInfo")) {
            String mapInfo = propertyChangeEvent.getNewValue().toString();

            if (!mapInfo.startsWith(statusErrorOccured)) {
                Intent gameI = new Intent(LobbyActivity.this, GameActivity.class);
                gameI.putExtra("mapName", gameApplication.getServerConnection().getMapName());
                gameI.putExtra("mapContent", mapInfo);
                gameI.putExtra("mode", "multiplayer");
                startActivity(gameI);
            } else {
                Toast.makeText(this, "Ein Fehler ist beim Parsen der Karte ausgewählten aufgetreten.\n" + mapInfo,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (propertyChangeName.equals("connectGame")) {
            String connectGameStatus = propertyChangeEvent.getNewValue().toString();

            if (connectGameStatus.equals(statusSuccessfull)) {
                TextView textGameId = findViewById(R.id.textGameId);
                TextView textMapId = findViewById(R.id.textMapId);
                TextView textShowPlayers = findViewById(R.id.textShowPlayers);
                TextView textWaitingHeader = findViewById(R.id.textWaitingHeader);
                TextView textInputGameIdHeader = findViewById(R.id.textInputGameIdHeader);
                EditText textInputGameId = findViewById(R.id.textInputGameId);
                Button connectButton = findViewById(R.id.connectButton);

                textWaitingHeader.setVisibility(View.VISIBLE);
                textGameId.setVisibility(View.VISIBLE);
                textMapId.setVisibility(View.VISIBLE);
                textShowPlayers.setVisibility(View.VISIBLE);
                textInputGameIdHeader.setVisibility(View.INVISIBLE);
                textInputGameId.setVisibility(View.INVISIBLE);
                connectButton.setVisibility(View.INVISIBLE);

                refreshWaitingLobby();
            } else {
                Toast.makeText(this, "Ein Fehler ist aufgetreten.\n" + connectGameStatus, Toast.LENGTH_SHORT).show();
            }
        } else if (propertyChangeName.equals("exceptionCreate")) {
            Toast.makeText(this, "Fehler beim erstellen des Spiels. (parsen fehlgeschlagen)",
                        Toast.LENGTH_SHORT).show();
        } else if (propertyChangeName.equals("exceptionJoin")) {
            Toast.makeText(this, "Fehler beim erstellen betreten Spiels. (parsen fehlgeschlagen)",
                        Toast.LENGTH_SHORT).show();
        }
    }

}
