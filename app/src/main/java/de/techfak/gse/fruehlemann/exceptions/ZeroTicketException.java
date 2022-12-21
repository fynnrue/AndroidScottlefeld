package de.techfak.gse.fruehlemann.exceptions;

public class ZeroTicketException extends Exception {

    public ZeroTicketException(String errorMessage) {
        super(errorMessage);
    }
}
