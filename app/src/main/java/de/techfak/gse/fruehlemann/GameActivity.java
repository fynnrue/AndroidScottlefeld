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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

public class GameActivity extends AppCompatActivity implements PropertyChangeListener {
    ParserMap parserMap;
    MapView mapView;
    IMapController mapController;
    Round round = null;
    MX mxPlayer = null;
    Player[] players = new Player[1];
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

        //show

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
    protected void onDestroy() {
        round.removeListener(this);
        super.onDestroy();
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
        String[] amountTickets = parserMap.getAmountTickets();

        for (int i = 0; i < amountPlayers; i++) {
            final int indexTrain = 2;
            final int indexBus = 4;
            final int indexBike = 0;

            Detective detective = new Detective(Integer.parseInt(amountTickets[indexTrain]),
                    Integer.parseInt(amountTickets[indexBus]), Integer.parseInt(amountTickets[indexBike]),
                    parserMap.genStartPosition());
            players[i] = detective;
        }

        try {
            final int indexTrain = 3;
            final int indexBus = 5;
            final int indexBike = 1;

            playerFactory = new PlayerFactory(jsonContent, players);

            mxPlayer = playerFactory.createMx(Integer.parseInt(amountTickets[indexTrain]),
                    Integer.parseInt(amountTickets[indexBus]), Integer.parseInt(amountTickets[indexBike]));
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
        boolean gameRunning = true;
        int roundnumber = 1;

        while (gameRunning) {
            round = new Round(players.length, roundnumber, mxPlayer, players);
            round.addListener(this);

            showRoundnumber(roundnumber);

            try {
                round.startRound();
            } catch (NoTicketAvailableException e) {
                handleException("M. X kann sich nicht Fortbewegen!");
                e.printStackTrace();
                Log.i("M. X Zug:", "keins, " + mxPlayer.getPos());
            }

            mxPlayer = round.getMX();
            players = round.getPlayers();

            roundnumber++;

            gameRunning = false;

            showMarkerNormalOnMap(mxPlayer.getPos());
        }
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
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                marker.showInfoWindow();
                showTransporttypesAndTickets(players[0].getPos(), marker.getTitle());
                return true;
            }
        });
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
            final int indexTransporttypes = 3;

            ArrayList<Transport> transporttypes = (ArrayList<Transport>) polyline[indexTransporttypes];
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
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            int colorIndex = 0;

            @Override
            public void run() {
                polyline.getOutlinePaint().setColor(colors[colorIndex]);
                colorIndex++;
                if (colorIndex >= colors.length) {
                    colorIndex = 0;
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    /**
     * Shows given Marker on MapView as normal marker, used e.g. when player or M.X was on that POI before.
     *
     * @param position Name of POI that will be changed.
     */
    public void showMarkerNormalOnMap(String position) {
        for (Marker marker : markers) {
            if (marker.getTitle().equals(position)) {
                marker.setIcon(ResourcesCompat.getDrawable(getResources(),
                        org.osmdroid.library.R.drawable.marker_default, null));
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
                marker.setIcon(ResourcesCompat.getDrawable(getResources(),
                        org.osmdroid.library.R.drawable.marker_default_focused_base, null));
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
            final int delay = 3000;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        finish();
    }

    /**
     * Shows the round number in a TextView.
     */
    public void showRoundnumber(int roundnumber) {
        TextView roundNumber = findViewById(R.id.showRoundNum);

        roundNumber.setText("Runde " + roundnumber);
    }

    public void showTransporttypesAndTickets(String position, String destination) {
        Spinner spinner = findViewById(R.id.showTransportTicket);
        String[] typeTicket;

        ArrayList<String> transporttypes = parserMap.getPossibleTransporttypes(position, destination);
        typeTicket = new String[transporttypes.size() + 1];

        typeTicket[0] = "Ticket auswählen";
        for (int i = 0; i < transporttypes.size(); i++) {
            if (transporttypes.get(i).startsWith("Siggi-Bike-Verb")) {
                typeTicket[i + 1] = "Siggi-Bike - " + players[0].getBikeTickets();
            } else if (transporttypes.get(i).startsWith("Stadtbahn-Verb")) {
                typeTicket[i + 1] = "Stadtbahn - " + players[0].getTrainTickets();
            } else if (transporttypes.get(i).startsWith("Bus-Verb")) {
                typeTicket[i + 1] = "Bus - " + players[0].getBusTickets();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, typeTicket);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


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
        Spinner spinner = findViewById(R.id.showTransportTicket);
        String ticket;

        String transporttype = spinner.getSelectedItem().toString();
        if (transporttype.startsWith("Siggi-")) {
            ticket = "BIKE";
        } else if (transporttype.startsWith("Stadtbahn-")) {
            ticket = "TRAIN";
        } else if (transporttype.startsWith("Bus-")) {
            ticket = "BUS";
        } else {
            return;
        }

        for (Marker marker : markers) {
            if (marker.isInfoWindowShown()) {
                round.endPlayerTurn(marker.getTitle(), ticket);
            }
        }
    }

    public void onShowTicketsClick(View view) {
        Intent ticketsIntent = new Intent(GameActivity.this, ShowTicktesActivity.class);
        ticketsIntent.putExtra("Siggi", String.valueOf(players[0].getBikeTickets()));
        ticketsIntent.putExtra("Train", String.valueOf(players[0].getTrainTickets()));
        ticketsIntent.putExtra("Bus", String.valueOf(players[0].getBusTickets()));
        startActivity(ticketsIntent);
    }

    //endregion

    //region Observer
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        final int mxRoundThree = 3;
        final int mxRoundEight = 3;
        final int mxRoundThirteen = 3;
        final int mxRoundEighteen = 3;

        int[] mxShowPosition = {mxRoundThree, mxRoundEight, mxRoundThirteen, mxRoundEighteen};
        int roundnumber = round.getRoundnumber();

        for (int number : mxShowPosition) {
            if (number == roundnumber) {
                showMXOnMap(mxPlayer.getPos());
            }
        }
    }

    //endRegion






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
