package de.techfak.gse.fruehlemann;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ArrayList<PointOfInterest> pOIs;
        ArrayList<Link> links = new ArrayList<>();
        String map = getIntent().getStringExtra("map");


        BufferedReader br = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(getResources().getIdentifier(map, "raw", getPackageName()))));
        String jsonContent = br.lines().collect(Collectors.joining());

        ObjectMapper om = new ObjectMapper();


        JsonNode root = null;
        Set<PointOfInterest> pOIsUnduped = new HashSet<>();

        try {
            root = om.readTree((jsonContent));

            for (JsonNode jn : root.get("features")) {

                String featureType = jn.get("geometry").get("type").asText();

                if (featureType.equals("Point")) {
                    BigDecimal lonP = jn.get("geometry").get("coordinates").get(0).decimalValue();
                    BigDecimal latP = jn.get("geometry").get("coordinates").get(1).decimalValue();

                    pOIsUnduped.add(new PointOfInterest(
                            jn.get("properties").get("name").asText(),
                            (new Coordinate(lonP, latP))
                    ));
                }
            }
            pOIs = new ArrayList<>(pOIsUnduped);


            ArrayList<String[]> types = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> typesEntry = root.get("facilmap").get("types").fields();
            ArrayList<Map.Entry<String, JsonNode>> entries = new ArrayList<>();
            while (typesEntry.hasNext()) {
                Map.Entry<String, JsonNode> entry = typesEntry.next();
                entries.add(entry);
            }

            for (Map.Entry<String, JsonNode> entry : entries) {
                if (entry.getValue().get("type").asText().equals("line")) {
                    String[] type = new String[2];


                    type[0] = entry.getValue().get("name").asText();
                    type[1] = entry.getKey();

                    types.add(type);
                }
            }

            ArrayList<Transport> transports = new ArrayList<>();
            for (String[] type : types) {
                transports.add(new Transport(type[0], type[1]));
            }

            for (JsonNode link : root.get("features")) {
                String featType = link.get("geometry").get("type").asText();

                if (featType.equals("LineString")) {
                    BigDecimal lonS = link.get("geometry").get("coordinates").get(0).get(0).decimalValue();
                    BigDecimal latS = link.get("geometry").get("coordinates").get(0).get(1).decimalValue();


                    for (PointOfInterest start : pOIs) {
                        if (start.getCoords().getLon().equals(lonS) && start.getCoords().getLat().equals(latS)) {
                            BigDecimal lonE = link.get("geometry").get("coordinates").get(1).get(0).decimalValue();
                            BigDecimal latE = link.get("geometry").get("coordinates").get(1).get(1).decimalValue();

                            for (PointOfInterest end : pOIs) {
                                if (end.getCoords().getLon().equals(lonE) && end.getCoords().getLat().equals(latE)) {

                                    if (link.get("properties").get("typeId").asText().equals(transports.get(0).getId())) {
                                        links.add(new Link(start, end, transports.get(0)));
                                    } else if (link.get("properties").get("typeId").asText().equals(transports.get(1).getId())) {
                                        links.add(new Link(start, end, transports.get(1)));
                                    } else if (link.get("properties").get("typeId").asText().equals(transports.get(2).getId())) {
                                        links.add(new Link(start, end, transports.get(2)));
                                    } else if (link.get("properties").get("typeId").asText().equals(transports.get(3).getId())) {
                                        links.add(new Link(start, end, transports.get(3)));

                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
}
