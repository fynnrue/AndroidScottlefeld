package de.techfak.gse.fruehlemann;


import java.util.ArrayList;

public class Link {
    private PointOfInterest pointOne;
    private PointOfInterest pointTwo;
    private ArrayList<Transport> type;

    public Link(PointOfInterest pointOne, PointOfInterest pointTwo, ArrayList<Transport> type) {
        this.pointOne = pointOne;
        this.pointTwo = pointTwo;
        this.type = type;
    }

    public PointOfInterest getPointOne() {
        return pointOne;
    }

    public void setPointOne(PointOfInterest pointOne) {
        this.pointOne = pointOne;
    }

    public PointOfInterest getPointTwo() {
        return pointTwo;
    }

    public void setPointTwo(PointOfInterest pointTwo) {
        this.pointTwo = pointTwo;
    }

    public ArrayList<Transport> getType() {
        return type;
    }

    public void addType(Transport type) {
        this.type.add(type);
    }
}
