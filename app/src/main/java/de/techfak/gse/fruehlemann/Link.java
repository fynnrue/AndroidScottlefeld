package de.techfak.gse.fruehlemann;


public class Link {
    private PointOfInterest point1, point2;
    private Transport type;

    public Link(PointOfInterest point1, PointOfInterest point2, Transport type) {
        this.point1 = point1;
        this.point2 = point2;
        this.type = type;
    }

    public PointOfInterest getPoint1() {
        return point1;
    }

    public void setPoint1(PointOfInterest point1) {
        this.point1 = point1;
    }

    public PointOfInterest getPoint2() {
        return point2;
    }

    public void setPoint2(PointOfInterest point2) {
        this.point2 = point2;
    }

    public Transport getType() {
        return type;
    }

    public void setType(Transport type) {
        this.type = type;
    }
}
