package de.techfak.gse.fruehlemann.model;

import static de.techfak.gse22.player_bot.Turn.TicketType.BUS;
import static de.techfak.gse22.player_bot.Turn.TicketType.TRAIN;
import static de.techfak.gse22.player_bot.Turn.TicketType.BIKE;

import android.util.Log;

import java.beans.PropertyChangeSupport;

import de.techfak.gse22.player_bot.MX;
import de.techfak.gse22.player_bot.Player;
import de.techfak.gse22.player_bot.Turn;
import de.techfak.gse22.player_bot.exceptions.NoTicketAvailableException;

public class Round {
    int amountPlayers;
    int amountTurnsComplete;
    boolean mXTurnComplete;
    boolean gameFinished = false;
    String destination = "";
    MX mX;
    Player[] players;
    Turn mxTurn = null;
    Turn[] turns;
    private PropertyChangeSupport support;

    public Round(int amountPlayers, MX mX, Player[] players) {
        this.amountPlayers = amountPlayers;
        this.mX = mX;
        this.players = players;
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

        this.destination = destination;
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

        return false;
    }

    public Player[] getPlayers() {
        return players;
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
}
