package de.techfak.gse.fruehlemann.model;

public class PointOfInterest {
    private String name;
    private Coordinate coords;

    public PointOfInterest(String name, Coordinate coords) {
        this.name = name;
        this.coords = coords;
    }

    public Coordinate getCoords() {
        return coords;
    }

    public void setCoords(Coordinate coords) {
        this.coords = coords;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
