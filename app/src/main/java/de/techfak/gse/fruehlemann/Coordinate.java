package de.techfak.gse.fruehlemann;

import java.math.BigDecimal;
import java.util.Objects;

public class Coordinate {
    private BigDecimal lon;
    private BigDecimal lat;

    public Coordinate(BigDecimal lon, BigDecimal lat) {
        this.lon = lon;
        this.lat = lat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return lon.equals(that.lon) && lat.equals(that.lat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lon, lat);
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public String getCoords() {
        return lon + ", " + lat;

    }
}
