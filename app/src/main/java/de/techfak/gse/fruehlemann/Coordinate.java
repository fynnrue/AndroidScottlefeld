package de.techfak.gse.fruehlemann;

import java.math.BigDecimal;
import java.util.Objects;

public class Coordinate {
    private BigDecimal lat, lon;

    public Coordinate(BigDecimal lat, BigDecimal lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return lat.equals(that.lat) && lon.equals(that.lon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
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
        return lat + ", " + lon;

    }
}
