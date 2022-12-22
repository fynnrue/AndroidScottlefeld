package de.techfak.gse.fruehlemann.model;

import static de.techfak.gse22.player_bot.Turn.TicketType.BUS;
import static de.techfak.gse22.player_bot.Turn.TicketType.TRAIN;
import static de.techfak.gse22.player_bot.Turn.TicketType.BIKE;

import android.util.Log;

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
    boolean turnValid = false;
    String exceptionType;
    MX mX;
    Player[] players;
    Turn mxTurn = null;
    Turn[] turns;
    ParserMap parserMap;

    String bike = String.format("BIKE");
    String bus = String.format("BUS");
    String train = String.format("TRAIN");

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
    }

    public void mXTurn() throws NoTicketAvailableException {
        mxTurn = mX.getTurn();
        Log.i("M. X Turn:", mxTurn.getTicketType().toString() + ", " + mxTurn.getTargetName());
    }

    public boolean endPlayerTurn(String destination, String transporttype) {
        if (checkIfTurnValid(destination, transporttype)) {

            Detective player = (Detective) players[amountTurnsComplete];

            Turn turn = null;
            if (transporttype.equals(bike)) {
                turn = new Turn(BIKE, destination);
                player.decreaseTicket(bike);
                mX.giveBikeTicket();
            } else if (transporttype.equals(train)) {
                turn = new Turn(TRAIN, destination);
                player.decreaseTicket(train);
                mX.giveTrainTicket();
            } else if (transporttype.equals(bus)) {
                turn = new Turn(BUS, destination);
                player.decreaseTicket(bus);
                mX.giveTrainTicket();
            }

            player.setPos(destination);
            players[amountTurnsComplete] = player;

            turns[amountTurnsComplete] = turn;

            Log.i("Turn Detective " + amountTurnsComplete, transporttype);

            amountTurnsComplete++;

            if (amountTurnsComplete >= amountPlayers) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTurnValidGame() {
        if (turnValid) {
            turnValid = false;
            return true;
        }
        return false;
    }

    public boolean checkIfTurnValid(String destination, String transporttype) {
        String exceptionLog = "Exception:";
        if (destination.equals(getPlayerPosition())) {
            return false;
        }
        try {
            if (transporttype.equals(bike)) {
                parserMap.checkLinkExists(getPlayerPosition(), destination, "Siggi-Bike-Verbindung");

                checkIfPlayerHasTicket(bike);
            } else if (transporttype.equals(bus)) {
                parserMap.checkLinkExists(getPlayerPosition(), destination, "Bus-Verbindung");

                checkIfPlayerHasTicket(bus);
            } else if (transporttype.equals(train)) {
                parserMap.checkLinkExists(getPlayerPosition(), destination, "Stadtbahn-Verbindung");

                checkIfPlayerHasTicket(train);
            }
        } catch (InvalidConnectionException invalidConnection) {
            Log.i(exceptionLog, "Chosen Connection does not exist.");
            exceptionType = "Invalid Connection";
            invalidConnection.printStackTrace();
            return false;
        } catch (ZeroTicketException zeroTicketException) {
            Log.i(exceptionLog, "No Ticket available for chosen transporttype.");
            exceptionType = "No Ticket";
            zeroTicketException.printStackTrace();
            return false;
        }

        turnValid = true;
        return true;
    }

    public void checkIfPlayerHasTicket(String tickettype) throws ZeroTicketException {
        if (tickettype.equals(bike)) {
            if (getPlayer().getBikeTickets() < 1) {
                throw new ZeroTicketException("No Ticket for Siggi-Bike");
            }
        } else if (tickettype.equals(bus)) {
            if (getPlayer().getBusTickets() < 1) {
                throw new ZeroTicketException("No Ticket for Bus");
            }
        } else if (tickettype.equals(train)) {
            if (getPlayer().getTrainTickets() < 1) {
                throw new ZeroTicketException("No Ticket for Train");
            }
        }
    }

    public String getExceptionType() {
        String tempExceptionType = exceptionType;
        exceptionType = "";
        return tempExceptionType;
    }

    public MX getMX() {
        return mX;
    }

    public Turn.TicketType getMXTransporttype() {
        return mxTurn.getTicketType();
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getPlayer() {
        return players[amountTurnsComplete];
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

}
