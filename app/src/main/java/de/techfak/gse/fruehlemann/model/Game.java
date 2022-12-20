package de.techfak.gse.fruehlemann.model;

import android.util.Log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.techfak.gse22.player_bot.MX;
import de.techfak.gse22.player_bot.Player;
import de.techfak.gse22.player_bot.PlayerFactory;
import de.techfak.gse22.player_bot.Turn;
import de.techfak.gse22.player_bot.exceptions.JSONParseException;
import de.techfak.gse22.player_bot.exceptions.NoFreePositionException;
import de.techfak.gse22.player_bot.exceptions.NoTicketAvailableException;

public class Game {
    int amountPlayers;
    int roundnumber = 1;
    boolean gameFinished = false;
    String winner = "M. X";
    ParserMap parserMap;
    Round round;
    PlayerFactory playerFactory;
    MX mX;
    Player[] players;
    Turn[] turns;
    private PropertyChangeSupport support;


    public Game(int amountPlayers, ParserMap map, String jsonContent) {
        this.amountPlayers = amountPlayers;
        parserMap = map;
        turns = new Turn[amountPlayers];

        initialisePlayers(amountPlayers, jsonContent);

        this.support = new PropertyChangeSupport(this);
    }

    public void startGame() {
        startRound();
    }

    public void startRound() {
        round = new Round(amountPlayers, mX, players);

        try {
            round.startRound();
        } catch (NoTicketAvailableException e) {
            //handleException("M. X kann sich nicht Fortbewegen!");
            e.printStackTrace();
            //Log.i("M. X Zug:", "none, " + mxPlayer.getPos());
        }

        mX = round.getMX();

        checkIfGameEnds();
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
        players = new Player[amountPlayers];

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

            mX = playerFactory.createMx(Integer.parseInt(amountTickets[indexTrain]),
                    Integer.parseInt(amountTickets[indexBus]), Integer.parseInt(amountTickets[indexBike]));
        } catch (JSONParseException e) {
            //handleException("Fehler beim Verarbeiten der GeoJson!");
            e.printStackTrace();
            //endActivity();
        } catch (NoFreePositionException e) {
            //handleException("Keine freie Position auf der Karte für M. X");
            e.printStackTrace();
            //endActivity();
        }

        Log.i("M. X Position", mX.getPos());
    }

    public void endPlayerTurn(String destination, String ticket) {
        boolean roundEnded = round.endPlayerTurn(destination, ticket);

        checkIfGameEnds();

        players = round.getPlayers();
        checkIfGameEnds();

        if (roundEnded) {
            increaseRoundNumber();
            startRound();
        }
    }

    public void gameFinished(boolean bool) {
        support.firePropertyChange("GameEnded", gameFinished, bool);
        gameFinished = bool;
    }

    public boolean isFinished() {
        return gameFinished;
    }

    public int getRoundnumber() {
        return roundnumber;
    }

    public String getMXPos() {
        return mX.getPos();
    }

    public void increaseRoundNumber() {
        this.support.firePropertyChange("NextRound", roundnumber, roundnumber + 1);
        roundnumber++;
    }

    public String getPlayerPosition() {
        return round.getPlayerPosition();
    }

    public int playerGetBusTickets() {
        return round.playerGetBusTickets();
    }

    public int playerGetTrainTickets() {
        return round.playerGetTrainTickets();
    }

    public int playerGetBikeTickets() {
        return round.playerGetBikeTickets();
    }

    public void checkIfGameEnds() {
        for (Player player : players) {
            if (player.getPos().equals(mX.getPos())) {
                winner = "Detective";
                gameFinished(true);
            }
            if (player.getBusTickets() == 0 && player.getTrainTickets() == 0 && playerGetBikeTickets() == 0)
                gameFinished(true);
            if (roundnumber >= 23) {
                gameFinished(true);
            }
            for (String destinations : parserMap.getPossibleDestinations(player.getPos())) {
                for (String transporttype : parserMap.getPossibleTransporttypes(player.getPos(), destinations)) {
                    if (player.getTrainTickets() > 0 && transporttype.equals("Stadtbahn-Verbindung")) {
                        return;
                    }
                    if (player.getBusTickets() > 0 && transporttype.equals("Bus-Verbindung")) {
                        return;
                    }
                    if (player.getTrainTickets() > 0 && transporttype.equals("Siggi-Bike-Verbindung")) {
                        return;
                    }
                    gameFinished(true);
                }
            }
        }
    }

    public String getWinner() {
        return winner;
    }

    //PropertyChangeHandler methods

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
