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
import com.fasterxml.jackson.databind.JsonMappingException;
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


        BufferedReader br = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(getResources().getIdentifier(
                        map, "raw", getPackageName()))));
        String jsonContent = br.lines().collect(Collectors.joining());

        ObjectMapper om = new ObjectMapper();

        JsonNode root = null;

        ParserMap parserMap = new ParserMap();

        try {
            root = om.readTree((jsonContent));

            pOIs = parserMap.getPOIs(root);

            transports = parserMap.getTransporttypes(root);

            links = parserMap.getLinks(root);

        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        outLinks();

        genStartPos();
        showPosition();

        showDestinations();

        Spinner chooseDest = findViewById(R.id.choosePOI);
        chooseDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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
            String transport = "";
            for (Transport trans : link.getType()) {
                transport += (", " + trans.getType());
            }

            Log.i("POI -> Verbindung", link.getPoint1().getName() + " (" + link.getPoint1().getCoords().getLat()
                    + ", " + link.getPoint1().getCoords().getLon() + ") -> "
                    + link.getPoint2().getName() + " (" + link.getPoint2().getCoords().getLat()
                    + " " + link.getPoint2().getCoords().getLon() + ")"
                    + transport);


            Log.i("POI -> Verbindung", link.getPoint2().getName() + " (" + link.getPoint2().getCoords().getLat()
                    + ", " + link.getPoint2().getCoords().getLon() + ") -> "
                    + link.getPoint1().getName() + " (" + link.getPoint1().getCoords().getLat()
                    + " " + link.getPoint1().getCoords().getLon() + ")"
                    + transport);
        }
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
                    if ((link.getPoint1().equals(poi)
                            && link.getPoint2().equals(position))
                            || (link.getPoint2().equals(poi)
                            && link.getPoint1().equals(position))) {
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
                    if ((link.getPoint1().equals(position) && link.getPoint2().equals(poi)) ||
                            (link.getPoint1().equals(poi) && link.getPoint2().equals(position))) {
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
