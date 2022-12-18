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
import de.techfak.gse22.player_bot.exceptions.NoTicketAvailableException;

public class GameActivity extends AppCompatActivity {
    ParserMap parserMap;
    MapView mapView;
    IMapController mapController;
    MX mxPlayer = null;
    Player[] players = new Player[1];
    int roundnumber = 1;
    ArrayList<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        /**
         * Read map.
         */
        String map = getIntent().getStringExtra("map");

        String jsonContent = null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(getResources().getIdentifier(
                        map, "raw", getPackageName()))))) {
            jsonContent = br.lines().collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Parse all needed information before game start.
         */
        ObjectMapper om = new ObjectMapper();

        JsonNode root;

        parserMap = new ParserMap();

        try {
            root = om.readTree(jsonContent);

            parserMap.parseMap(root);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        //Log all POIs and Connections
        outLinks();

        //Initialising Osmdroid MapView
        initialiseOsmdroid();

        //Initialising Players
        initialisePlayers(1, jsonContent);

        //Show POIs and Connections on map
        //After Player initialisations to avoid null-pointer-exception
        showAllLinks();
        showAllPOIs();

        //Initialising Game
        startGameRounds();

    }

    //region Android belonging Methods

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

    //endregion

    //region Initialisation of Activity

    /**
     * Initialising MapView.
     */
    public void initialiseOsmdroid() {
        mapView = findViewById(R.id.mapView);

        Context ctx = getApplicationContext();

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.APPLICATION_ID);
        provider.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);


        mapController = mapView.getController();

        final int zoomfactor = 16;
        mapController.setZoom(zoomfactor);
    }

    /**
     * Initialising all players including Detectives and M. X.
     *
     * @param amountPlayers Amount of players that are playing Detective.
     * @param jsonContent   Information about map that the game is played on.
     */
    public void initialisePlayers(int amountPlayers, String jsonContent) {
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
    }

    /**
     * Starts the game and administrates Rounds.
     */
    public void startGameRounds() {
        // while (true) {
        int[] mxShowPosition = {3, 8, 13, 18};
        boolean showMXRound = false;
        Round round = new Round(players.length, roundnumber, mxPlayer, players);

        showRoundnumber();
        try {
            round.startRound();
        } catch (NoTicketAvailableException e) {
            handleException("M. X kann sich nicht Fortbewegen!");
            e.printStackTrace();
            Log.i("M. X Zug:", "keins, " + mxPlayer.getPos());
        }

        mxPlayer = round.getMX();
        players = round.getPlayers();


        for (int roundnumber : mxShowPosition) {
            if (roundnumber == roundnumber) {
                showMXRound = true;
            }
        }

            /*MVC Pattern

            if (showMXRound == true) {
                showMXOnMap(mxPlayer.getPos());
            }*/

        if (showMXRound == true) {
            showMarkerNormalOnMap(mxPlayer.getPos());
        }

        roundnumber++;
        // }
    }

    /**
     * Prints out POIs and connections in Android-Log when activity is started.
     */
    public void outLinks() {
        String logType = "POI -> Verbindung";

        ArrayList<String> logOut = parserMap.outLinks();

        for (String link : logOut) {
            Log.i(logType, link);
        }
    }

    //endregion

    //region Map Interaction

    /**
     * Goes through every POI and starts method to show on MapView.
     */
    public void showAllPOIs() {
        ArrayList<Object[]> geoPoints = parserMap.getGeoPoints();

        for (Object[] geoPoint : geoPoints) {
            showPositionOnMap(geoPoint);
        }
    }

    /**
     * Adds Marker on MapView for given POI consisting of GeoPoints and Name.
     *
     * @param point Information about GeoPoints and name of POI.
     */
    public void showPositionOnMap(Object[] point) {
        Marker marker = new Marker(mapView);

        marker.setPosition((GeoPoint) point[0]);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle((String) point[1]);
        if (point[1].equals(players[0].getPos())) {
            centerMap((GeoPoint) point[0]);
            marker.setIcon(ResourcesCompat.getDrawable(getResources(), org.osmdroid.library.R.drawable.person, null));
        }
        mapView.getOverlays().add(marker);

        markers.add(marker);
    }

    /**
     * Centers MapView on GeoPoint.
     *
     * @param point GeoPoint for centering.
     */
    public void centerMap(GeoPoint point) {
        mapController.setCenter(point);
    }

    /**
     * For every Polyline received from parserMap it will be checked if the connection contains the specific
     * transporttype and adds a color to the facette, which will be taken to show the different colors on the mapView.
     * Every Polyline gets added to the MapView and Method polylineChangeColor will be calles with the colors.
     */
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
            mapView.getOverlays().add(line);

            polylineChangeColor(line, new Handler(Looper.getMainLooper()), colors);
        }
    }

    /**
     * At runtime the given polyline will change colors which represent the different transporttypes between
     * the POIs it connects.
     *
     * @param polyline Polyline that will change colors.
     * @param handler  Performs actions on different thread than main game.
     * @param colors   Colors which represent the differen transporttypes.
     */
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

    /**
     * Shows given Marker on MapView as normal marker, used e.g. when player or M.X was on that POI before.
     *
     * @param position Name of POI that will be changed.
     */
    public void showMarkerNormalOnMap(String position) {
        for (Marker marker : markers) {
            if (marker.getTitle().equals(position)) {
                marker.setIcon(ResourcesCompat.getDrawable(getResources(), org.osmdroid.library.R.drawable.marker_default, null));
            }
        }
    }

    /**
     * Shows given Marker on MapView with a different look to show M.X. position
     *
     * @param position Name of POI that M. X is at.
     */
    public void showMXOnMap(String position) {
        for (Marker marker : markers) {
            if (marker.getTitle().equals(position)) {
                marker.setIcon(ResourcesCompat.getDrawable(getResources(), org.osmdroid.library.R.drawable.marker_default_focused_base, null));
            }
        }
    }

    //endregion

    //region Non-Map Interaction

    /**
     * Shows Snackbar on View with given text. Called on Exceptions.
     *
     * @param textSnackbar Fitting text to Exception which gets shown to User in a Snackbar.
     */
    public void handleException(String textSnackbar) {
        Snackbar.make(findViewById(android.R.id.content).getRootView(), textSnackbar, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Ends the Activity after 3 Seconds.
     */
    public void endActivity() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        finish();
    }

    /**
     * Shows the round number in a TextView.
     */
    public void showRoundnumber() {
        TextView roundNumber = findViewById(R.id.showRoundNum);

        roundNumber.setText("Runde " + roundnumber);
    }

    /**
     * Calls method to center map when Button is button to center is clicked.
     *
     * @param view Current View.
     */
    public void onCenterMapClick(View view) {
        centerMap(parserMap.getGeoPoint(players[0].getPos()));
    }

    /**
     * Ends Turn of player.
     *
     * @param view Current View.
     */
    public void onEndMoveClick(View view) {
      /*  Spinner showDest = findViewById(R.id.choosePOI);
        Spinner showTypes = findViewById(R.id.chooseTransptype);

        if (showDest.getSelectedItem() != null && showTypes.getSelectedItem() != null) {
            if (parserMap.linkExists(position, showDest.getSelectedItem().toString(),
                    showTypes.getSelectedItem().toString())) {
                position = showDest.getSelectedItem().toString();
            }

            showPosition();
            showDestinations();
            showTransporttypes();
        }*/
    }

    //endregion




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
    }*/


}
