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
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GameActivity extends AppCompatActivity {
    ArrayList<PointOfInterest> pOIs;
    ArrayList<Link> links;
    ArrayList<Transport> transports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        String map = getIntent().getStringExtra("map");


        BufferedReader br = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(getResources().getIdentifier(map, "raw", getPackageName()))));
        String jsonContent = br.lines().collect(Collectors.joining());

        ObjectMapper om = new ObjectMapper();

        JsonNode root = null;

        try {
            root = om.readTree((jsonContent));

            getPOIs(root);

            getTransporttypes(root);

            getLinks(root);

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

    public void getPOIs(JsonNode root) {
        Set<PointOfInterest> pOIsUnduped = new HashSet<>();

        for (JsonNode jn : root.get("features")) {

            String featureType = jn.get("geometry").get("type").asText();

            if (featureType.equals("Point")) {
                BigDecimal latP = jn.get("geometry").get("coordinates").get(0).decimalValue();
                BigDecimal lonP = jn.get("geometry").get("coordinates").get(1).decimalValue();

                pOIsUnduped.add(new PointOfInterest(
                        jn.get("properties").get("name").asText(),
                        (new Coordinate(latP, lonP))
                ));
            }
        }
        pOIs = new ArrayList<>(pOIsUnduped);
    }

    public void getTransporttypes(JsonNode root) {
        transports = new ArrayList<>();
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

        for (String[] type : types) {
            transports.add(new Transport(type[0], type[1]));
        }
    }

    public void getLinks(JsonNode root) {
        links = new ArrayList<>();

        for (JsonNode link : root.get("features")) {
            String featType = link.get("geometry").get("type").asText();

            if (featType.equals("LineString")) {
                BigDecimal latS = link.get("geometry").get("coordinates").get(0).get(0).decimalValue();
                BigDecimal lonS = link.get("geometry").get("coordinates").get(0).get(1).decimalValue();


                for (PointOfInterest start : pOIs) {
                    if (start.getCoords().getLat().equals(latS) && start.getCoords().getLon().equals(lonS)) {
                        BigDecimal latE = link.get("geometry").get("coordinates").get(1).get(0).decimalValue();
                        BigDecimal lonE = link.get("geometry").get("coordinates").get(1).get(1).decimalValue();

                        for (PointOfInterest end : pOIs) {
                            if (end.getCoords().getLat().equals(latE) && end.getCoords().getLon().equals(lonE)) {

                                if (link.get("properties").get("typeId").asText().equals(transports.get(0).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPoint1().equals(start) && x.getPoint2().equals(end))) {
                                        for (Link linkDup : links) {
                                            if (linkDup.getPoint1().equals(start) && linkDup.getPoint2().equals(end)) {
                                                linkDup.addType(transports.get(0));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(0));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get("properties").get("typeId").asText().equals(transports.get(1).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPoint1().equals(start) && x.getPoint2().equals(end))) {
                                        if (links.stream().anyMatch(x -> x.getPoint1().equals(start) && x.getPoint2().equals(end))) {
                                            for (Link linkDup : links) {
                                                if (linkDup.getPoint1().equals(start) && linkDup.getPoint2().equals(end)) {
                                                    linkDup.addType(transports.get(1));
                                                }
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(1));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get("properties").get("typeId").asText().equals(transports.get(2).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPoint1().equals(start) && x.getPoint2().equals(end))) {
                                        if (links.stream().anyMatch(x -> x.getPoint1().equals(start) && x.getPoint2().equals(end))) {
                                            for (Link linkDup : links) {
                                                if (linkDup.getPoint1().equals(start) && linkDup.getPoint2().equals(end)) {
                                                    linkDup.addType(transports.get(2));
                                                }
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(2));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get("properties").get("typeId").asText().equals(transports.get(3).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPoint1().equals(start) && x.getPoint2().equals(end))) {
                                        if (links.stream().anyMatch(x -> x.getPoint1().equals(start) && x.getPoint2().equals(end))) {
                                            for (Link linkDup : links) {
                                                if (linkDup.getPoint1().equals(start) && linkDup.getPoint2().equals(end)) {
                                                    linkDup.addType(transports.get(3));
                                                }
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(3));
                                        links.add(new Link(start, end, type));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        links.sort(Comparator.comparing(a -> a.getPoint1().getName()));
    }

}
