package de.techfak.gse.fruehlemann;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

import de.techfak.gse22.player_bot.MX;
import de.techfak.gse22.player_bot.Player;
import de.techfak.gse22.player_bot.PlayerFactory;
import de.techfak.gse22.player_bot.exceptions.JSONParseException;
import de.techfak.gse22.player_bot.exceptions.NoFreePositionException;

public class GameActivity extends AppCompatActivity {
    ParserMap parserMap;
    MapView mapView;
    IMapController mapController;
    MX mxPlayer = null;
    Player[] players = new Player[1];

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

        //showPosition();

        //initialise Osmdroid MapView
        mapView = findViewById(R.id.mapView);

        Context ctx = getApplicationContext();

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.APPLICATION_ID);
        provider.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);


        mapController = mapView.getController();

        //Initialising Players
        PlayerFactory playerFactory = null;

        Detective detective = new Detective(8, 10, 4, parserMap.genStartPosition());
        detective.setPos(parserMap.genStartPosition());
        players[0] = detective;

        try {
            playerFactory = new PlayerFactory(jsonContent, players);

            mxPlayer = playerFactory.createMx(3, 4, 3);
        } catch (JSONParseException e) {
            handleException("Fehler beim Verarbeiten der GeoJson!");
            e.printStackTrace();
            endActivity();
        } catch (NoFreePositionException e) {
            handleException("Keine freie Position auf der Karte für M. X");
            e.printStackTrace();
            endActivity();
        }

        Log.i("M. X Position", mxPlayer.getPos());

        //Show POIs and Connections on map
        //After Player initialisations to avoid null-pointer-exception
        showAllLinks();
        showAllPOIs();
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

    public void showAllPOIs() {
        ArrayList<Object[]> geoPoints = parserMap.getGeoPoints();

        for (Object[] geoPoint : geoPoints) {
            showPositionOnMap(geoPoint);
        }
    }

    public void showPositionOnMap(Object[] point) {
        //center mapView
        final int zoomfactor = 16;
        mapController.setZoom(zoomfactor);

        //show position on map
        Marker marker = new Marker(mapView);

        marker.setPosition((GeoPoint) point[0]);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle((String) point[1]);
        if (point[1].equals(players[0].getPos())) {
            centerMap((GeoPoint) point[0]);
            marker.setIcon(ResourcesCompat.getDrawable(getResources(), org.osmdroid.library.R.drawable.person, null));
        }
        mapView.getOverlays().add(marker);
    }

    public void centerMap(GeoPoint point) {
        mapController.setCenter(point);
    }

    public void showAllLinks() {
        ArrayList<Object[]> polylines = parserMap.getPolylines();

        for (Object[] polyline : polylines) {
            ArrayList<Transport> transporttypes = (ArrayList<Transport>) polyline[3];
            Polyline line = (Polyline) polyline[0];
            int[] colors = {};

            for (Transport type : transporttypes) {
                if (type.getType().equals("Siggi-Bike-Verbindung")) {
                    int[] tempColor = new int[colors.length + 1];
                    for (int i = 0; i < colors.length; i++) {
                        tempColor[i] = colors[i];
                    }
                    tempColor[colors.length] = Color.RED;

                    colors = tempColor;
                } else if (type.getType().equals("Bus-Verbindung")) {
                    int[] tempColor = new int[colors.length + 1];
                    for (int i = 0; i < colors.length; i++) {
                        tempColor[i] = colors[i];
                    }
                    tempColor[colors.length] = Color.GREEN;

                    colors = tempColor;
                } else if (type.getType().equals("Stadtbahn-Verbindung")) {
                    int[] tempColor = new int[colors.length + 1];
                    for (int i = 0; i < colors.length; i++) {
                        tempColor[i] = colors[i];
                    }
                    tempColor[colors.length] = Color.BLUE;

                    colors = tempColor;
                }
            }
            showPolylineOnMap(line);

            polylineChangeColor(line, new Handler(Looper.getMainLooper()), colors);
        }
    }

    public void showPolylineOnMap(Polyline line) {
        mapView.getOverlays().add(line);
    }

    //Information on how to change Polylines while running from: https://stackoverflow.com/a/72381497
    public void polylineChangeColor(final Polyline polyline, final Handler handler, int[] colors) {
        handler.postDelayed(new Runnable() {
            int colorIndex = 0;

            @Override
            public void run() {
                polyline.getOutlinePaint().setColor(colors[colorIndex]);
                colorIndex++;
                if (colorIndex >= colors.length) {
                    colorIndex = 0;
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
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

    public void onCenterMapClick(View view) {
        centerMap(parserMap.getGeoPoint(players[0].getPos()));
    }

    public void outLinks() {
        String logType = "POI -> Verbindung";

        ArrayList<String> logOut = parserMap.outLinks();

        for (String link : logOut) {
            Log.i(logType, link);
        }
    }

    public void showPosition() {
        TextView showPos = findViewById(R.id.showPos);

        showPos.setText("Position: \n" + players[0].getPos());
    }

    public void handleException(String textSnackbar) {
        Snackbar.make(findViewById(android.R.id.content).getRootView(), textSnackbar, Snackbar.LENGTH_SHORT).show();
    }

    public void endActivity() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        finish();
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
