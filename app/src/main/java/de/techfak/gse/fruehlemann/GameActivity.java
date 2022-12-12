package de.techfak.gse.fruehlemann;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class GameActivity extends AppCompatActivity {
    ArrayList<PointOfInterest> pOIs;
    ArrayList<Link> links;
    ArrayList<Transport> transports;
    PointOfInterest position;

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

        ParserMap parserMap = new ParserMap();

        try {
            root = om.readTree((jsonContent));

            pOIs = parserMap.getPOIs(root);

            transports = parserMap.getTransporttypes(root);

            links = parserMap.getLinks(root);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        outLinks();

        genStartPos();
        showPosition();

        showDestinations();

        Spinner chooseDest = findViewById(R.id.choosePOI);
        chooseDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                showTransporttypes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
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
        for (Link link : links) {
            StringBuilder transport = new StringBuilder();
            String logType = "POI -> Verbindung";

            for (Transport trans : link.getType()) {
                transport.append(", ").append(trans.getType());
            }

            String startLat = String.valueOf(link.getPointOne().getCoords().getLat());
            String startLon = String.valueOf(link.getPointOne().getCoords().getLon());
            String endLat = String.valueOf(link.getPointTwo().getCoords().getLat());
            String endLon = String.valueOf(link.getPointTwo().getCoords().getLon());

            String logOut = getString(
                    link.getPointOne().getName(), startLat, startLon,
                    link.getPointTwo().getName(), endLat, endLon,
                    transport.toString()
            );

            Log.i(logType, logOut);

            startLat = String.valueOf(link.getPointTwo().getCoords().getLat());
            startLon = String.valueOf(link.getPointTwo().getCoords().getLon());
            endLat = String.valueOf(link.getPointOne().getCoords().getLat());
            endLon = String.valueOf(link.getPointOne().getCoords().getLon());


            logOut = getString(
                    link.getPointTwo().getName(), startLat, startLon,
                    link.getPointOne().getName(), endLat, endLon,
                    transport.toString()
            );

            Log.i(logType, logOut);
        }

    }


    private String getString(String startName, String startLat, String startLon,
                            String endName, String endLat, String endLon, String transport) {
        String logOut = String.format("%1s" + " (%2s" + ", %3s" + ") -> %4s" + " (%5s" + "  %6s" + ")%7s",
                startName, startLat, startLon, endName, endLat, endLon, transport);
        return logOut;
    }

    public void genStartPos() {
        int amPOIs = pOIs.size();

        position = pOIs.get((int) (Math.random() * amPOIs));
    }

    public void showPosition() {
        TextView showPos = findViewById(R.id.showPos);

        showPos.setText("Position: \n" + position.getName());
    }

    public void showDestinations() {
        Spinner showDest = findViewById(R.id.choosePOI);

        ArrayList<String> pOIDest = new ArrayList<>();
        for (PointOfInterest poi : pOIs) {
            if (!poi.equals(position)) {
                for (Link link : links) {
                    if ((link.getPointOne().equals(poi)
                            && link.getPointTwo().equals(position))
                            || (link.getPointTwo().equals(poi)
                            && link.getPointOne().equals(position))) {
                        pOIDest.add(poi.getName());
                    }
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                pOIDest
        );

        showDest.setAdapter(adapter);
    }

    public void showTransporttypes() {
        Spinner showDest = findViewById(R.id.choosePOI);
        Spinner showTypes = findViewById(R.id.chooseTransptype);

        String dest = showDest.getSelectedItem().toString();
        ArrayList<Transport> transpTypes = new ArrayList<>();

        for (PointOfInterest poi : pOIs) {
            if (poi.getName().equals(dest)) {
                for (Link link : links) {
                    if ((link.getPointOne().equals(position) && link.getPointTwo().equals(poi))
                            || (link.getPointOne().equals(poi) && link.getPointTwo().equals(position))) {
                        transpTypes.addAll(link.getType());
                        break;
                    }
                }
            }
        }

        ArrayList<String> stringTypes = new ArrayList<>();
        for (Transport transp : transpTypes) {
            stringTypes.add(transp.getType());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                stringTypes
        );

        showTypes.setAdapter(adapter);
    }

    public void onEndMoveClick(View view) {
        Spinner showDest = findViewById(R.id.choosePOI);
        Spinner showTypes = findViewById(R.id.chooseTransptype);

        if (showDest.getSelectedItem() != null && showTypes.getSelectedItem() != null) {
            for (PointOfInterest poi : pOIs) {
                if (showDest.getSelectedItem().toString().equals(poi.getName())) {
                    position = poi;
                    break;
                }
            }
            showPosition();
            showDestinations();
            showTransporttypes();
        }
    }
}
