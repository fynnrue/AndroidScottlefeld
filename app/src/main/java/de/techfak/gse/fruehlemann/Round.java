package de.techfak.gse.fruehlemann;

import android.util.Log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.techfak.gse22.player_bot.MX;
import de.techfak.gse22.player_bot.Player;
import de.techfak.gse22.player_bot.Turn;
import de.techfak.gse22.player_bot.exceptions.NoTicketAvailableException;

public class Round {
    private PropertyChangeSupport support;
    int amountPlayers;
    int roundnumber;
    int amountTurnComplete;
    boolean mXTurnComplete;
    MX mx;
    Player[] players;
    Turn mxTurn = null;

    public Round(int amountPlayers, int roundnumber, MX mx, Player[] players) {
        this.amountPlayers = amountPlayers;
        this.roundnumber = roundnumber;
        this.mx = mx;
        this.players = players;

        this.support = new PropertyChangeSupport(this);
    }

    public void startRound() throws NoTicketAvailableException {
        amountTurnComplete = 0;
        mXTurnComplete = false;

        mXTurn();
        setMXTurnComplete(true);

        for (Player player : players) {
            Turn turn = null;
            Turn.TicketType tickettype = null;

            Log.i("Detective Zug: ", "test");//tickettype.toString());

            turnComplete();
        }
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

    public void turnComplete() {
        this.support.firePropertyChange("TurnsComplete", amountTurnComplete, amountTurnComplete + 1);
        amountTurnComplete++;
    }

    public void mXTurn() throws NoTicketAvailableException {
        mxTurn = mx.getTurn();
        String targetName = mxTurn.getTargetName();
        Turn.TicketType ticket = mxTurn.getTicketType();
        Log.i("M. X Zug:", mxTurn.getTicketType().toString() + ", " + mxTurn.getTargetName());
    }

    public void setMXTurnComplete(boolean complete) {
        support.firePropertyChange("MXTurnComplete", complete, mXTurnComplete);
        mXTurnComplete = complete;
    }

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
