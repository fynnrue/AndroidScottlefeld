package de.techfak.gse.fruehlemann;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class GameActivity extends AppCompatActivity {
    ParserMap parserMap;
    String position;
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        String map = getIntent().getStringExtra("map");


        String jsonContent = null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(getResources().getIdentifier(
                        map, "raw", getPackageName()))))) {
            jsonContent = br.lines().collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper om = new ObjectMapper();

        JsonNode root;

        parserMap = new ParserMap();

        try {
            root = om.readTree((jsonContent));

            parserMap.parseMap(root);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        outLinks();

        genStartPos();
        showPosition();

        //showDestinations();

        /*Spinner chooseDest = findViewById(R.id.choosePOI);
        chooseDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                showTransporttypes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });TODO löschen*/

        //initialise Osmdroid MapView
        mapView = findViewById(R.id.mapView);

        Context ctx = getApplicationContext();

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.APPLICATION_ID);
        provider.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Spiel verlassen").
                setMessage("Zur Startansicht zurückkehren?").
                setPositiveButton("Ja", (dialog, id) -> {
                    Intent mainI = new Intent(GameActivity.this, MainActivity.class);
                    startActivity(mainI);

                    finish();
                }).
                setNegativeButton("Nein", (dialog, id) -> {
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void outLinks() {
        String logType = "POI -> Verbindung";

        ArrayList<String> logOut = parserMap.outLinks();

        for (String link : logOut) {
            Log.i(logType, link);
        }
    }

    public void genStartPos() {
        position = parserMap.genStartPosition();
    }

    public void showPosition() {
        TextView showPos = findViewById(R.id.showPos);

        showPos.setText("Position: \n" + position);
    }

    /*public void showDestinations() {
        Spinner showDest = findViewById(R.id.choosePOI);

        ArrayList<String> possibleDestinations = parserMap.getPossibleDestinations(position);

        ArrayAdapter<String> adapter = new ArrayAdapter(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                possibleDestinations
        );

        showDest.setAdapter(adapter);
    }

    public void showTransporttypes() {
        Spinner showDest = findViewById(R.id.choosePOI);
        Spinner showTypes = findViewById(R.id.chooseTransptype);

        String destination = showDest.getSelectedItem().toString();
        ArrayList<String> transportTypes = parserMap.getPossibleTransporttypes(position, destination);

        ArrayAdapter<String> adapter = new ArrayAdapter(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                transportTypes
        );

        showTypes.setAdapter(adapter);
    }

    public void onEndMoveClick(View view) {
        Spinner showDest = findViewById(R.id.choosePOI);
        Spinner showTypes = findViewById(R.id.chooseTransptype);

        if (showDest.getSelectedItem() != null && showTypes.getSelectedItem() != null) {
            if (parserMap.linkExists(position, showDest.getSelectedItem().toString(),
                    showTypes.getSelectedItem().toString())) {
                position = showDest.getSelectedItem().toString();
            }

            showPosition();
            showDestinations();
            showTransporttypes();
        }
    }*/
}
