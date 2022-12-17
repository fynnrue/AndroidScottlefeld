package de.techfak.gse.fruehlemann;

import de.techfak.gse22.player_bot.Player;

public class Detective implements Player {
    int amountBusTickets;
    int amountBikeTickets;
    int amountTrainTickets;
    String position;

    public Detective(int amountBusTickets, int amountBikeTickets, int amountTrainTickets, String position) {
        this.amountBusTickets = amountBusTickets;
        this.amountBikeTickets = amountBikeTickets;
        this.amountTrainTickets = amountTrainTickets;
        this.position = position;
    }

    @Override
    public int getBusTickets() {
        return amountBusTickets;
    }

    @Override
    public int getBikeTickets() {
        return amountBikeTickets;
    }

    @Override
    public int getTrainTickets() {
        return amountTrainTickets;
    }

    @Override
    public String getPos() {
        return position;
    }

    public void setPos(String position) {
        this.position = position;
    }
}
