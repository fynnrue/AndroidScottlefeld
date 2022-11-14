package de.techfak.gse.fruehlemann;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public class GameActivity extends AppCompatActivity {

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

            for (JsonNode jn : root.get("features")) {

                String featureType = jn.get("geometry").get("type").asText();
                if (featureType.equals("Point")) {
                    BigDecimal lonP = jn.get("geometry").get("coordinates").get(0).decimalValue();
                    BigDecimal latP = jn.get("geometry").get("coordinates").get(1).decimalValue();

                    for (JsonNode link : root.get("features")) {
                        String featType = link.get("geometry").get("type").asText();
                        if (featType.equals("LineString")) {
                            BigDecimal lonL = link.get("geometry").get("coordinates").get(0).get(0).decimalValue();
                            BigDecimal latL = link.get("geometry").get("coordinates").get(0).get(1).decimalValue();

                            if (lonP.equals(lonL) && latP.equals(latL)) {
                                BigDecimal lonPoint = link.get("geometry").get("coordinates").get(1).get(0).decimalValue();
                                BigDecimal latPoint = link.get("geometry").get("coordinates").get(1).get(1).decimalValue();

                                Log.i("POIs mit Verbindung", jn.get("properties").get("name").asText() + " ("
                                        + lonP + ", " + latP + ") -> " + searchPoint(root, lonPoint, latPoint) + " ("
                                        + lonPoint + ", " + latPoint + "), " + "Verkehrsmittel?"
                                );
                            }

                            lonL = link.get("geometry").get("coordinates").get(1).get(0).decimalValue();
                            latL = link.get("geometry").get("coordinates").get(1).get(1).decimalValue();

                            if (lonP.equals(lonL) && latP.equals(latL)) {
                                BigDecimal lonPoint = link.get("geometry").get("coordinates").get(0).get(0).decimalValue();
                                BigDecimal latPoint = link.get("geometry").get("coordinates").get(0).get(1).decimalValue();

                                Log.i("POIs mit Verbindung", jn.get("properties").get("name").asText() + " ("
                                        + lonP + ", " + latP + ") -> " + searchPoint(root, lonPoint, latPoint) + " ("
                                        + latPoint + ", " + lonPoint + "), " + "Verkehrsmittel?"
                                );
                            }
                        }
                    }
                }
            }
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

    private String searchPoint(JsonNode root, BigDecimal lon, BigDecimal lat) {
        for (JsonNode jn : root.get("features")) {
            if (jn.get("geometry").get("type").asText().equals("Point")) {
                if (jn.get("geometry").get("coordinates").get(0).decimalValue().equals(lon)
                        && jn.get("geometry").get("coordinates").get(1).decimalValue().equals(lat)) {
                    return jn.get("properties").get("name").asText();
                }
            }
        }
        return null;
    }
}
