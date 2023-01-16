// Setting Spinner options from: https://stackoverflow.com/a/5241720

package de.techfak.gse.fruehlemann.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Objects;

import de.techfak.gse.fruehlemann.R;
import de.techfak.gse.fruehlemann.exceptions.NoMapSelectedException;

public class MainActivity extends AppCompatActivity {
    Spinner dropdown;
    String noSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dropdown = findViewById(R.id.mapSpinner);
        noSelection = "Karte auswählen";

        Field[] maps = R.raw.class.getFields();
        String[] mapNames = new String[maps.length + 1];
        mapNames[0] = noSelection;
        for (int i = 0; i < maps.length; i++) {
            mapNames[i + 1] = maps[i].getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mapNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }

    public void onStartClick(View view) {
        if (isMapSelected()) {
            String selectedMap = dropdown.getSelectedItem().toString();

            Snackbar.make(view, "Spiel startet", Snackbar.LENGTH_SHORT).show();

            Intent gameI = new Intent(MainActivity.this, GameActivity.class);
            gameI.putExtra("map", selectedMap);
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
        Spinner mapSpinner = findViewById(R.id.mapSpinner);

        singleButton.setVisibility(View.INVISIBLE);
        multiButton.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.VISIBLE);
        mapSpinner.setVisibility(View.VISIBLE);
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
        StringRequest request = buildRequest(url);

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public StringRequest buildRequest(String url) {
        url += "/";
        // url somit "http://127.0.0.1:8080/"
        Response.Listener<String> onResponse = response -> {
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
        };
        final Response.ErrorListener onError = error -> {
            Toast.makeText(this, new String(error.networkResponse.data), Toast.LENGTH_SHORT).show();
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, onResponse, onError);
        // request.getBody() liefert = null

        return request;
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

}
