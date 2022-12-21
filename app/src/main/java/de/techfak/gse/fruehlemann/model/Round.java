package de.techfak.gse.fruehlemann.model;

import static de.techfak.gse22.player_bot.Turn.TicketType.BUS;
import static de.techfak.gse22.player_bot.Turn.TicketType.TRAIN;
import static de.techfak.gse22.player_bot.Turn.TicketType.BIKE;

import android.util.Log;

import java.beans.PropertyChangeSupport;

import de.techfak.gse.fruehlemann.exceptions.InvalidConnectionException;
import de.techfak.gse.fruehlemann.exceptions.ZeroTicketException;
import de.techfak.gse22.player_bot.MX;
import de.techfak.gse22.player_bot.Player;
import de.techfak.gse22.player_bot.Turn;
import de.techfak.gse22.player_bot.exceptions.NoTicketAvailableException;

public class Round {
    int amountPlayers;
    int amountTurnsComplete;
    boolean mXTurnComplete;
    boolean gameFinished = false;
    boolean turnValid = false;
    String exceptionType;
    MX mX;
    Player[] players;
    Turn mxTurn = null;
    Turn[] turns;
    ParserMap parserMap;
    private PropertyChangeSupport support;

    public Round(int amountPlayers, MX mX, Player[] players, ParserMap parserMap) {
        this.amountPlayers = amountPlayers;
        this.mX = mX;
        this.players = players;
        this.parserMap = parserMap;
    }

    public void startRound() throws NoTicketAvailableException {
        amountTurnsComplete = 0;
        mXTurnComplete = false;

        turns = new Turn[players.length];

        mXTurn();

        for (Player player : players) {
            if (player.getPos().equals(mX.getPos())) {
                gameFinished = true;
            }
        }
    }

    public MX getMX() {
        return mX;
    }

    public Turn.TicketType getMXTransporttype() {
        return mxTurn.getTicketType();
    }

    public void mXTurn() throws NoTicketAvailableException {
        mxTurn = mX.getTurn();
        Log.i("M. X Turn:", mxTurn.getTicketType().toString() + ", " + mxTurn.getTargetName());
    }

    public boolean endPlayerTurn(String destination, String transporttype) {
        final String typeBike = "BIKE";
        final String typeTrain = "TRAIN";
        final String typeBus = "BUS";

        if (checkIfTurnValid(destination, transporttype)) {

            Detective player = (Detective) players[amountTurnsComplete];

            Turn turn = null;
            if (transporttype.equals(typeBike)) {
                turn = new Turn(BIKE, destination);
                player.decreaseTicket(typeBike);
                mX.giveBikeTicket();
            } else if (transporttype.equals(typeTrain)) {
                turn = new Turn(TRAIN, destination);
                player.decreaseTicket(typeTrain);
                mX.giveTrainTicket();
            } else if (transporttype.equals(typeBus)) {
                turn = new Turn(BUS, destination);
                player.decreaseTicket(typeBus);
                mX.giveTrainTicket();
            }

            player.setPos(destination);
            players[amountTurnsComplete] = player;

            turns[amountTurnsComplete] = turn;

            Log.i("Turn Detective " + amountTurnsComplete, transporttype);

            amountTurnsComplete++;

            if (player.getPos().equals(mX.getPos())) {
                gameFinished = true;
            }

            if (amountTurnsComplete >= amountPlayers) {
                return true;
            }
        }
        return false;
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getPlayer() {
        return players[amountTurnsComplete];
    }

    public boolean gameFinished() {
        return gameFinished;
    }

    public String getPlayerPosition() {
        return players[amountTurnsComplete].getPos();
    }

    public int playerGetBusTickets() {
        return players[amountTurnsComplete].getBusTickets();
    }

    public int playerGetTrainTickets() {
        return players[amountTurnsComplete].getTrainTickets();
    }

    public int playerGetBikeTickets() {
        return players[amountTurnsComplete].getBikeTickets();
    }

    public String getExceptionType() {
        String tempExceptionType = exceptionType;
        exceptionType = "";
        return tempExceptionType;
    }

    public boolean checkTurnValidGame() {
        if (turnValid) {
            turnValid = false;
            return true;
        }
        return false;
    }

    public boolean checkIfTurnValid(String destination, String transporttype) {
        if (destination.equals(getPlayerPosition())) {
            return false;
        }
        try {
            if (transporttype.startsWith("BI")) {
                parserMap.checkLinkExists(getPlayerPosition(), destination, "Siggi-Bike-Verbindung");

                checkIfPlayerHasTicket("siggi");
            } else if (transporttype.startsWith("BU")) {
                parserMap.checkLinkExists(getPlayerPosition(), destination, "Bus-Verbindung");

                checkIfPlayerHasTicket("bus");
            } else if (transporttype.startsWith("TRA")) {
                parserMap.checkLinkExists(getPlayerPosition(), destination, "Stadtbahn-Verbindung");

                checkIfPlayerHasTicket("train");
            }
        } catch (InvalidConnectionException invalidConnection) {
            exceptionType = "Invalid Connection";
            invalidConnection.printStackTrace();
        } catch (ZeroTicketException zeroTicketException) {
            exceptionType = "No Ticket";
            zeroTicketException.printStackTrace();
        }

        turnValid = true;
        return true;
    }

    public void checkIfPlayerHasTicket(String tickettype) throws ZeroTicketException {
        if (tickettype.startsWith("sig")) {
            if (getPlayer().getBikeTickets() < 1) {
                throw new ZeroTicketException("No Ticket for Siggi-Bike");
            }
        } else if (tickettype.startsWith("bu")) {
            if (getPlayer().getBusTickets() < 1) {
                throw new ZeroTicketException("No Ticket for Bus");
            }
        } else if (tickettype.startsWith("tra")) {
            if (getPlayer().getTrainTickets() < 1) {
                throw new ZeroTicketException("No Ticket for Train");
            }
        }
    }
}
