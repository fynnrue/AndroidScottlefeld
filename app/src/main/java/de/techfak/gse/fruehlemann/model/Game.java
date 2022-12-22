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
    boolean validTurnsPossible = true;
    String winner = "M. X";
    String exceptionType = "";
    ParserMap parserMap;
    Round round;
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
        round = new Round(amountPlayers, mX, players, parserMap);

        try {
            round.startRound();
        } catch (NoTicketAvailableException e) {
            Log.i("Exception: M. X Turn:", "none, " + mX.getPos());
            e.printStackTrace();
            alarmException("No Ticket M. X");
        }

        mX = round.getMX();
        mXTurnFinished();

        checkIfGameEnds();
    }

    /**
     * Initialising all players including Detectives and M. X.
     *
     * @param amountPlayers Amount of players that are playing Detective.
     * @param jsonContent   Information about map that the game is played on.
     */
    public void initialisePlayers(int amountPlayers, String jsonContent) {
        String exceptionLog = "Exceptiom:";
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
            Log.i(exceptionLog, "Failed to initiate M. X");
            e.printStackTrace();
            alarmException("Initiation M. X Failure");
            return;
        } catch (NoFreePositionException e) {
            Log.i(exceptionLog, "No free position on map to initialise M. X");
            e.printStackTrace();
            alarmException("No place M. X");
            return;
        }

        Log.i("M. X Position", mX.getPos());
    }

    public void endPlayerTurn(String destination, String ticket) {
        boolean roundEnded = round.endPlayerTurn(destination, ticket);

        if (round.checkTurnValidGame()) {

            checkIfGameEnds();

            players = round.getPlayers();
            mX = round.getMX();
            checkIfGameEnds();

            if (roundEnded) {
                final int maxRound = 22;

                if (roundnumber >= maxRound) {
                    gameFinished(true);
                }
                increaseRoundNumber();
                startRound();
            }
        } else {
            alarmException(round.getExceptionType());
        }
    }

    public void gameFinished(boolean bool) {
        support.firePropertyChange("GameEnded", gameFinished, bool);
        gameFinished = bool;
    }

    public void checkIfGameEnds() {
        for (Player player : players) {
            if (player.getPos().equals(mX.getPos())) {
                winner = "Detective";
                gameFinished(true);
            }
            if (player.getBusTickets() == 0 && player.getTrainTickets() == 0 && playerGetBikeTickets() == 0) {
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
                    if (player.getBikeTickets() > 0 && transporttype.equals("Siggi-Bike-Verbindung")) {
                        return;
                    }
                }
                gameFinished(true);
                setValidTurnsPossible(false);
            }
        }
    }

    public void increaseRoundNumber() {
        roundnumber++;
        support.firePropertyChange("NextRound", (roundnumber - 1), roundnumber);
    }

    public void alarmException(String exception) {
        support.firePropertyChange("Exception", exceptionType, exception);
        exceptionType = exception;
    }

    public void setValidTurnsPossible(boolean bool) {
        support.firePropertyChange("ValidTurns", validTurnsPossible, bool);
        validTurnsPossible = bool;
    }

    public void mXTurnFinished() {
        support.firePropertyChange("MXTurn", 0, 1);
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

    public String getWinner() {
        return winner;
    }

    public String getExceptionType() {
        String tempExceptionType = exceptionType;
        exceptionType = "";
        return tempExceptionType;
    }

    //PropertyChangeHandler methods

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
