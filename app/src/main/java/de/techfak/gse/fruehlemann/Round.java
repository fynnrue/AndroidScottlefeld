package de.techfak.gse.fruehlemann;

import android.util.Log;

import de.techfak.gse22.player_bot.MX;
import de.techfak.gse22.player_bot.Player;
import de.techfak.gse22.player_bot.Turn;
import de.techfak.gse22.player_bot.exceptions.NoTicketAvailableException;

public class Round {
    int amountPlayers;
    int roundnumber;
    MX mx;
    Player[] players;
    Turn mxTurn = null;

    public Round(int amountPlayers, int roundnumber, MX mx, Player[] players) {
        this.amountPlayers = amountPlayers;
        this.roundnumber = roundnumber;
        this.mx = mx;
        this.players = players;
    }

    public void startRound() throws NoTicketAvailableException {
         mxTurn = mx.getTurn();
         Log.i("M. X Zug:", mxTurn.getTicketType().toString() + ", " + mxTurn.getTargetName());

        for (Player player : players) {
            Turn turn = null;
            Turn.TicketType tickettype = null;

            Log.i("Detective Zug: ", tickettype.toString());
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
}
