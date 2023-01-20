package de.techfak.gse.fruehlemann.activities;

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

import java.util.ArrayList;

import de.techfak.gse.fruehlemann.R;

public class LobbyActivity extends AppCompatActivity {
    String url;
    ArrayList<String> maps = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        url = getIntent().getStringExtra("url");

        getMapsFromServer();
    }

    /**
     * Call to get the names of all available maps from the server.
     */
    public void getMapsFromServer() {
        StringRequest request = buildGetMapRequest();

        RequestQueue queue = Volley.newRequestQueue(this);
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
        EditText textPlayerName = findViewById(R.id.textPlayerName);
        Button playMXButton = findViewById(R.id.playMXButton);
        Button createGameButton = findViewById(R.id.createGameButton);
        Spinner mapSelectSpinnerMultiplayer = findViewById(R.id.mapSelectSpinnerMultiplayer);

        textName.setVisibility(View.INVISIBLE);
        textPlayerName.setVisibility(View.INVISIBLE);
        playMXButton.setVisibility(View.INVISIBLE);
        mapSelectSpinnerMultiplayer.setVisibility(View.VISIBLE);
        createGameButton.setVisibility(View.VISIBLE);
    }
}
