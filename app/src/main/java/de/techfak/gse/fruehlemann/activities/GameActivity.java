package de.techfak.gse.fruehlemann.activities;

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
import android.widget.Button;
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

import de.techfak.gse.fruehlemann.BuildConfig;
import de.techfak.gse.fruehlemann.model.Game;
import de.techfak.gse.fruehlemann.model.ParserMap;
import de.techfak.gse.fruehlemann.R;

public class GameActivity extends AppCompatActivity implements PropertyChangeListener {
    ParserMap parserMap;
    MapView mapView;
    IMapController mapController;
    Game game;
    ArrayList<Marker> markers = new ArrayList<>();
    String markerName = "";

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
            br.close();
        } catch (IOException e) {
            Log.i("Exception", "Failed to read File.");
            showSnackbarOnScreen("Karte konnte nicht gelesen werden.");
            e.printStackTrace();
            endActivity();
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
            Log.i("Exception", "Failed to parse Map from Json.");
            showSnackbarOnScreen("Fehler beim Parsen der Karte.");
            e.printStackTrace();
            endActivity();
        }

        //Log all POIs and Connections
        outLinks();

        //Creating Game
        createGame(1, jsonContent);

        //Initialising Osmdroid MapView
        initialiseOsmdroid();

        //Show POIs and Connections on map
        //After Player initialisations to avoid null-pointer-exception
        showAllLinks();
        showAllPOIs();

        //Starting Game
        startGame();
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
        game.removeListener(this);
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

    /**
     * Ends the Activity after 3 Seconds.
     */
    public void endActivity() {
        try {
            final int delay = 3000;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Log.i("Exception", "Failed to end Activity.");
            showSnackbarOnScreen("Fehler beim Beenden der Ansicht.");
            e.printStackTrace();
        }
        finish();
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
     * Starts the game and administrates Rounds.
     */
    public void createGame(int amountPlayers, String jsonContent) {
        game = new Game(amountPlayers, parserMap, jsonContent);
        game.addListener(this);
    }

    public void startGame() {
        game.startGame();
        showPlayerOnMap(game.getPlayerPosition());
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
                showTransporttypesAndTickets(game.getPlayerPosition(), marker.getTitle());
                return true;
            }
        });
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

            ArrayList<String> transporttypes = (ArrayList<String>) polyline[indexTransporttypes];
            Polyline line = (Polyline) polyline[0];
            int[] colors = {};

            for (String type : transporttypes) {
                if (type.equals("Siggi-Bike-Verbindung")) {
                    int[] tempColor = new int[colors.length + 1];
                    for (int i = 0; i < colors.length; i++) {
                        tempColor[i] = colors[i];
                    }
                    tempColor[colors.length] = Color.RED;

                    colors = tempColor;
                } else if (type.equals("Bus-Verbindung")) {
                    int[] tempColor = new int[colors.length + 1];
                    for (int i = 0; i < colors.length; i++) {
                        tempColor[i] = colors[i];
                    }
                    tempColor[colors.length] = Color.GREEN;

                    colors = tempColor;
                } else if (type.equals("Stadtbahn-Verbindung")) {
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
                markerName = marker.getTitle();
            }
        }
    }

    public void showPlayerOnMap(String position) {
        if (!game.isFinished()) {
            for (Marker marker : markers) {
                if (marker.getTitle().equals(position)) {
                    centerMap(parserMap.getGeoPoint(marker.getTitle()));
                    marker.setIcon(ResourcesCompat.getDrawable(getResources(),
                            org.osmdroid.library.R.drawable.person, null));
                }
            }
        }
    }

    public void showMXNormal() {
        for (Marker marker : markers) {
            if (marker.getTitle().equals(markerName)) {
                marker.setIcon(ResourcesCompat.getDrawable(getResources(),
                        org.osmdroid.library.R.drawable.marker_default, null));
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
    public void showSnackbarOnScreen(String textSnackbar) {
        Snackbar.make(findViewById(android.R.id.content).getRootView(), textSnackbar, Snackbar.LENGTH_SHORT).show();
    }

    public void showTransporttypesAndTickets(String position, String destination) {
        String[] typeTicket;

        ArrayList<String> transporttypes = parserMap.getPossibleTransporttypes(position, destination);
        typeTicket = new String[transporttypes.size() + 1];

        typeTicket[0] = "Ticket auswählen";
        for (int i = 0; i < transporttypes.size(); i++) {
            if (transporttypes.get(i).startsWith("Siggi-Bike-Verb")) {
                int amountBikeTickets = game.playerGetBikeTickets();

                if (amountBikeTickets > 0) {
                    typeTicket[i + 1] = "Siggi-Bike - " + amountBikeTickets;
                }
            } else if (transporttypes.get(i).startsWith("Stadtbahn-Verb")) {
                int amountTrainTickets = game.playerGetTrainTickets();

                if (amountTrainTickets > 0) {
                    typeTicket[i + 1] = "Stadtbahn - " + amountTrainTickets;
                }
            } else if (transporttypes.get(i).startsWith("Bus-Verb")) {
                int amountBusTickets = game.playerGetBusTickets();

                if (amountBusTickets > 0) {
                    typeTicket[i + 1] = "Bus - " + amountBusTickets;
                }
            }
        }
        ArrayList<String> transportTicketList = new ArrayList<>();
        for (String ticket : typeTicket) {
            if (ticket != null) {
                transportTicketList.add(ticket);
            }
        }

        setTransportSpinner(transportTicketList);
    }

    public void setTransportSpinner(ArrayList<String> transportTicket) {
        Spinner spinner = findViewById(R.id.showTransportTicket);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, transportTicket);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Calls method to center map when Button is button to center is clicked.
     *
     * @param view Current View.
     */
    public void onCenterMapClick(View view) {
        centerMap(parserMap.getGeoPoint(game.getPlayerPosition()));
    }

    /**
     * Ends Turn of player.
     *
     * @param view Current View.
     */
    public void onEndMoveClick(View view) {
        Spinner spinner = findViewById(R.id.showTransportTicket);
        String ticket;

        if (spinner.getSelectedItem() == null) {
            return;
        }
        String transporttype = spinner.getSelectedItem().toString();
        if (transporttype.startsWith("Siggi-")) {
            ticket = "BIKE";
        } else if (transporttype.startsWith("Stadtbahn -")) {
            ticket = "TRAIN";
        } else if (transporttype.startsWith("Bus -")) {
            ticket = "BUS";
        } else {
            return;
        }

        for (Marker marker : markers) {
            if (marker.isInfoWindowShown()) {
                showMarkerNormalOnMap(game.getPlayerPosition());
                showPlayerOnMap(marker.getTitle());
                game.endPlayerTurn(marker.getTitle(), ticket);
            }
        }

        ArrayList<String> emptySpinner = new ArrayList<>();
        emptySpinner.add("Ziel auswählen");
        setTransportSpinner(emptySpinner);
    }

    public void onShowTicketsClick(View view) {
        Intent ticketsIntent = new Intent(GameActivity.this, ShowTicktesActivity.class);
        ticketsIntent.putExtra("Siggi", String.valueOf(game.playerGetBikeTickets()));
        ticketsIntent.putExtra("Train", String.valueOf(game.playerGetTrainTickets()));
        ticketsIntent.putExtra("Bus", String.valueOf(game.playerGetBusTickets()));
        startActivity(ticketsIntent);
    }

    public void onEndGameClick(View view) {
        finish();
    }

    //endregion

    //region Observer
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String event = propertyChangeEvent.getPropertyName();

        if (event.equals("NextRound")) {
            TextView round = findViewById(R.id.showRoundText);

            int roundnumber = game.getRoundnumber();

            round.setText("Runde " + roundnumber);

        } else if (event.equals("MXTurn")) {
            final int mxShowRoundThree = 3;
            final int mxShowRoundEight = 8;
            final int mxShowRoundThirteen = 13;
            final int mxShowRoundEighteen = 18;

            int[] mxShowPosition = {mxShowRoundThree, mxShowRoundEight, mxShowRoundThirteen, mxShowRoundEighteen};
            int roundnumber = game.getRoundnumber();

            for (int number : mxShowPosition) {
                if (number == roundnumber) {
                    showMXOnMap(game.getMXPos());
                } else if ((number + 1) == roundnumber) {
                    showMXNormal();
                }
            }
        } else if (event.equals("GameEnded")) {
            TextView winnerText = findViewById(R.id.showWinnerText);
            TextView round = findViewById(R.id.showRoundText);
            Spinner transportTickets = findViewById(R.id.showTransportTicket);
            Button tickets = findViewById(R.id.showTicketsButton);
            Button endMove = findViewById(R.id.endMoveButton);
            Button center = findViewById(R.id.centerButton);
            Button endGame = findViewById(R.id.endGameButton);

            showMXOnMap(game.getMXPos());
            centerMap(parserMap.getGeoPoint(game.getMXPos()));

            round.setVisibility(View.INVISIBLE);
            transportTickets.setVisibility(View.INVISIBLE);
            tickets.setVisibility(View.INVISIBLE);
            endMove.setVisibility(View.INVISIBLE);
            center.setVisibility(View.INVISIBLE);

            winnerText.setVisibility(View.VISIBLE);
            winnerText.setText("Sieger: " + game.getWinner());
            endGame.setVisibility(View.VISIBLE);
        } else if (event.equals("ValidTurns")) {
            TextView noValidTurnsText = findViewById(R.id.noValidTurnsText);

            noValidTurnsText.setVisibility(View.VISIBLE);
            noValidTurnsText.setText("Keine validen Spielzüge mehr möglich!");
        } else if (event.equals("Exception")) {
            String exceptionType = game.getExceptionType();

            if (exceptionType.equals("Invalid Connection")) {
                showSnackbarOnScreen("Unzulässige Verbindung!");
            } else if (exceptionType.equals("No Ticket")) {
                showSnackbarOnScreen("Kein Ticket für ausgewählten Transporttyp!");
            } else if (exceptionType.equals("No Ticket M. X")) {
                showSnackbarOnScreen("M. X besitzt aktuell kein gültiges Ticket.");
            }
        }
    }

    //endregion

}
