// Setting Spinner options from: https://stackoverflow.com/a/5241720

package de.techfak.gse.fruehlemann.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.Objects;

import de.techfak.gse.fruehlemann.R;
import de.techfak.gse.fruehlemann.exceptions.NoMapSelectedException;
import de.techfak.gse.fruehlemann.GameApplication;
import de.techfak.gse.fruehlemann.model.ServerConnection;

public class MainActivity extends AppCompatActivity implements PropertyChangeListener {
    Spinner dropdown;
    String noSelection;
    String[] singleplayerMapNames;
    String connection = "";

    GameApplication gameApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameApplication = (GameApplication) getApplication();
        gameApplication.setServerConnection(new ServerConnection("", this));
        gameApplication.getServerConnection().addListener(this);

        dropdown = findViewById(R.id.mapSpinner);
        noSelection = "Karte auswählen";

        Field[] maps = R.raw.class.getFields();
        singleplayerMapNames = new String[maps.length + 1];
        singleplayerMapNames[0] = noSelection;
        for (int i = 0; i < maps.length; i++) {
            singleplayerMapNames[i + 1] = maps[i].getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, singleplayerMapNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        gameApplication.getServerConnection().removeListener(this);
        super.onDestroy();
    }

    public void onStartClick(View view) {
        if (isMapSelected()) {
            String selectedMap = dropdown.getSelectedItem().toString();

            Snackbar.make(view, "Spiel startet", Snackbar.LENGTH_SHORT).show();

            Intent gameI = new Intent(MainActivity.this, GameActivity.class);
            gameI.putExtra("map", selectedMap);
            gameI.putExtra("mode", "singleplayer");
            startActivity(gameI);

            Log.i("Ausgewählte Karte", selectedMap);
        } else {
            Snackbar.make(view, "Keine Karte ausgewählt!", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void onSingleplayerClick(View view) {
        Button singleButton = findViewById(R.id.singleplayerGameButton);
        Button multiButton = findViewById(R.id.multiplayerGameButton);
        Button startButton = findViewById(R.id.start);

        singleButton.setVisibility(View.INVISIBLE);
        multiButton.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.VISIBLE);
        dropdown.setVisibility(View.VISIBLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, singleplayerMapNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }

    public void onMultiplayerClick(View view) {
        Button singleButton = findViewById(R.id.singleplayerGameButton);
        Button multiButton = findViewById(R.id.multiplayerGameButton);
        Button connectButton = findViewById(R.id.connectServerButton);
        TextView urlHeader = findViewById(R.id.textUrlHeader);
        TextView hostnameHeader = findViewById(R.id.textHostnameHeader);
        EditText hostnameContent = findViewById(R.id.textHostname);
        TextView portHeader = findViewById(R.id.textPortHeader);
        EditText portContent = findViewById(R.id.textPort);
        TextView welcomeMessage = findViewById(R.id.header);


        singleButton.setVisibility(View.INVISIBLE);
        multiButton.setVisibility(View.INVISIBLE);
        welcomeMessage.setVisibility(View.INVISIBLE);
        urlHeader.setVisibility(View.VISIBLE);
        hostnameHeader.setVisibility(View.VISIBLE);
        hostnameContent.setVisibility(View.VISIBLE);
        portHeader.setVisibility(View.VISIBLE);
        portContent.setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);
    }

    public void onConnectClick(View view) {
        EditText hostnameInput = findViewById(R.id.textHostname);
        EditText portInput = findViewById(R.id.textPort);

        String hostname = Objects.requireNonNull(hostnameInput.getText()).toString();
        String port = Objects.requireNonNull(portInput.getText()).toString();

        String url = hostname + ":" + port;

        gameApplication.getServerConnection().setUrl(url);
        gameApplication.getServerConnection().connectToServer();
    }

    private boolean isMapSelected() {
        try {
            checkSelected();
            return true;
        } catch (NoMapSelectedException exception) {
            Log.i("Exception", "No map selected when tried to start game!");
            exception.printStackTrace();
        }
        return false;
    }

    private void checkSelected() throws NoMapSelectedException {
        if (dropdown.getSelectedItem().equals(noSelection)) {
            throw new NoMapSelectedException("No map Selected");
        }
    }

    //PropertyChange
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String url = gameApplication.getServerConnection().getUrl();

        if (propertyChangeEvent.getPropertyName().equals("connection")) {
            String statusCode = propertyChangeEvent.getNewValue().toString();

            if (statusCode.equals("200")) {
                Toast.makeText(this, "Verbunden mit " + url, Toast.LENGTH_SHORT).show();

                Intent lobbyI = new Intent(MainActivity.this, LobbyActivity.class);
                lobbyI.putExtra("url", url);
                startActivity(lobbyI);
            } else if (statusCode.equals("201")) {
                Toast.makeText(this, "Verbunden aber " + url + "ist kein Spielserver.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Keine Verbindung zu " + url + " möglich. \nFehler: " + statusCode, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
