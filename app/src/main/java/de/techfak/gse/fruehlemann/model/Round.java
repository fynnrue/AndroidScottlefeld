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
    MX mx;
    Player[] players;
    Turn mxTurn = null;
    Turn[] turns;
    private PropertyChangeSupport support;

    public Round(int amountPlayers, MX mX, Player[] players) {
        this.amountPlayers = amountPlayers;
        this.mx = mX;
        this.players = players;
    }

    public void startRound() throws NoTicketAvailableException {
        amountTurnsComplete = 0;
        mXTurnComplete = false;

        turns = new Turn[players.length];

        mXTurn();

        for (Player player : players) {
            if (player.getPos().equals(mx.getPos())) {
                gameFinished = true;
            }
        }
    }

    public MX getMX() {
        return mx;
    }

    public Turn.TicketType getMXTransporttype() {
        return mxTurn.getTicketType();
    }

    public void mXTurn() throws NoTicketAvailableException {
        mxTurn = mx.getTurn();
        Log.i("M. X Turn:", mxTurn.getTicketType().toString() + ", " + mxTurn.getTargetName());
    }

    public boolean endPlayerTurn(String destination, String transporttype) {
        this.destination = destination;

        Turn turn = null;
        if (transporttype.equals("BIKE")) {
            turn = new Turn(BIKE, destination);
        } else if (transporttype.equals("TRAIN")) {
            turn = new Turn(TRAIN, destination);
        } else if (transporttype.equals("BUS")) {
            turn = new Turn(BUS, destination);
        }

        Detective player = (Detective) players[amountTurnsComplete];
        player.setPos(destination);
        players[amountTurnsComplete] = player;

        turns[amountTurnsComplete] = turn;

        Log.i("Turn Detective " + amountTurnsComplete, transporttype);

        amountTurnsComplete++;

        if (player.getPos().equals(mx.getPos())) {
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
