package de.techfak.gse.fruehlemann;

import static de.techfak.gse22.player_bot.Turn.TicketType.BUS;
import static de.techfak.gse22.player_bot.Turn.TicketType.TRAIN;
import static de.techfak.gse22.player_bot.Turn.TicketType.BIKE;

import android.util.Log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.techfak.gse22.player_bot.MX;
import de.techfak.gse22.player_bot.Player;
import de.techfak.gse22.player_bot.Turn;
import de.techfak.gse22.player_bot.exceptions.NoTicketAvailableException;

public class Round {
    int amountPlayers;
    int roundnumber;
    int amountTurnsComplete;
    boolean mXTurnComplete;
    String destination = "";
    MX mx;
    Player[] players;
    Turn mxTurn = null;
    Turn[] turns;
    private PropertyChangeSupport support;

    public Round(int amountPlayers, int roundnumber, MX mx, Player[] players) {
        this.amountPlayers = amountPlayers;
        this.roundnumber = roundnumber;
        this.mx = mx;
        this.players = players;

        this.support = new PropertyChangeSupport(this);
    }

    public void startRound() throws NoTicketAvailableException {
        amountTurnsComplete = 0;
        mXTurnComplete = false;

        turns = new Turn[players.length];

        mXTurn();
        setMXTurnComplete(true);
    }

    public MX getMX() {
        return mx;
    }

    public Turn.TicketType getMXTransporttype() {
        return mxTurn.getTicketType();
    }

    public Player[] getPlayers() {
        return players;
    }

    public int getRoundnumber() {
        return roundnumber;
    }

    public void turnComplete(Turn turn) {
        this.support.firePropertyChange("TurnsComplete", amountTurnsComplete, amountTurnsComplete + 1);
        amountTurnsComplete++;

        Log.i("Spieler Verkehrsmittel:", turn.getTicketType().toString());
    }

    public void mXTurn() throws NoTicketAvailableException {
        mxTurn = mx.getTurn();
        String targetName = mxTurn.getTargetName();
        Turn.TicketType ticket = mxTurn.getTicketType();
        Log.i("M. X Zug:", mxTurn.getTicketType().toString() + ", " + mxTurn.getTargetName());
    }

    public void endPlayerTurn(String destination, String transporttype) {
        this.destination = destination;

        Turn turn = null;
        if (transporttype.equals("Siggi-Bike")) {
            turn = new Turn(BIKE, destination);
        } else if (transporttype.equals("Stadtbahn")) {
            turn = new Turn(TRAIN, destination);
        } else if (transporttype.equals("Bus")) {
            turn = new Turn(BUS, destination);
        }

        turnComplete(turn);
    }

    public void setMXTurnComplete(boolean complete) {
        support.firePropertyChange("MXTurnComplete", complete, mXTurnComplete);
        mXTurnComplete = complete;
    }



    //PropertyChangeHandler methods

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
