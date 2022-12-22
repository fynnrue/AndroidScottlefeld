package de.techfak.gse.fruehlemann.model;

import com.fasterxml.jackson.databind.JsonNode;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.techfak.gse.fruehlemann.exceptions.InvalidConnectionException;

public class ParserMap {
    final int amountTicketTypes = 7;
    String[] amountTickets = new String[amountTicketTypes];
    HashMap<PointOfInterest, ArrayList<Link>> map = new HashMap<>();
    ArrayList<Transport> transports;
    ArrayList<Object[]> geoPoints = new ArrayList<>();
    ArrayList<Object[]> polylines = new ArrayList<>();

    //region String format

    String featuresPath = String.format("features");
    String geometryPath = String.format("geometry");
    String typePath = String.format("type");
    String coordinatesPath = String.format("coordinates");
    String propertiesPath = String.format("properties");
    String namePath = String.format("name");
    String facilmapPath = String.format("facilmap");
    String typesPath = String.format("types");
    String defaultPath = String.format("default");
    String fieldsPath = String.format("fields");
    String linePath = String.format("line");

    //endregion

    //region Parse map

    /**
     * Parses all information to Hashmap and GeoPoints and Polylines to work with in Controller.
     *
     * @param root JsonNode to access information from given map.
     */
    public void parseMap(JsonNode root) {
        ArrayList<PointOfInterest> pOIs;
        ArrayList<Link> links;

        pOIs = parsePOIs(root);
        transports = parseTransporttypes(root);
        links = parseLinks(root, pOIs, transports);
        amountTickets = parseTickets(root);

        for (PointOfInterest pOI : pOIs) {
            ArrayList<Link> connectedPoIs = new ArrayList<>();

            for (Link link : links) {
                if (link.getPointOne().equals(pOI)) {
                    connectedPoIs.add(link);
                } else if (link.getPointTwo().equals(pOI)) {
                    connectedPoIs.add(new Link(link.getPointTwo(), link.getPointOne(), link.getType()));
                }
            }
            map.put(pOI, connectedPoIs);
        }

        parseGeoPoints();
        parsePolylines();
    }

    /**
     * Reads out all the POIs from map.
     *
     * @param root Root of given map.
     * @return ArrayList of all existing POIs.
     */
    public ArrayList<PointOfInterest> parsePOIs(JsonNode root) {
        ArrayList<PointOfInterest> pOIs;
        Set<PointOfInterest> pOIsUnduped = new HashSet<>();

        for (JsonNode jn : root.get(featuresPath)) {

            String featureType = jn.get(geometryPath).get(typePath).asText();

            if (featureType.equals("Point")) {
                BigDecimal lonP = jn.get(geometryPath).get(coordinatesPath).get(0).decimalValue();
                BigDecimal latP = jn.get(geometryPath).get(coordinatesPath).get(1).decimalValue();

                pOIsUnduped.add(new PointOfInterest(
                        jn.get(propertiesPath).get(namePath).asText(),
                        (new Coordinate(latP, lonP))
                ));
            }
        }
        pOIs = new ArrayList<>(pOIsUnduped);

        return pOIs;
    }

    /**
     * Reads out all the transporttypes from map.
     *
     * @param root Root of given map.
     * @return ArrayList of all existing transporttypes.
     */
    public ArrayList<Transport> parseTransporttypes(JsonNode root) {
        ArrayList<Transport> transports = new ArrayList<>();
        ArrayList<String[]> types = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> typesEntry = root.get(facilmapPath).get(typesPath).fields();
        ArrayList<Map.Entry<String, JsonNode>> entries = new ArrayList<>();
        while (typesEntry.hasNext()) {
            Map.Entry<String, JsonNode> entry = typesEntry.next();
            entries.add(entry);
        }

        for (Map.Entry<String, JsonNode> entry : entries) {
            if (entry.getValue().get(typePath).asText().equals(linePath)) {
                String[] type = new String[2];


                type[0] = entry.getValue().get(namePath).asText();
                type[1] = entry.getKey();

                types.add(type);
            }
        }

        for (String[] type : types) {
            transports.add(new Transport(type[0], type[1]));
        }

        return transports;
    }

    /**
     * Reads out all the connections from map with corresponding transporttypes.
     *
     * @param root       Root of given map.
     * @param pOIs       All POIs in map.
     * @param transports All transporttypes in map.
     * @return ArrayList of all existing connections between POIs.
     */
    public ArrayList<Link> parseLinks(JsonNode root, ArrayList<PointOfInterest> pOIs, ArrayList<Transport> transports) {
        ArrayList<Link> links = new ArrayList<>();

        for (JsonNode link : root.get(featuresPath)) {
            String featType = link.get(geometryPath).get(typePath).asText();

            if (featType.equals("LineString")) {
                BigDecimal lonS = link.get(geometryPath).get(coordinatesPath).get(0).get(0).decimalValue();
                BigDecimal latS = link.get(geometryPath).get(coordinatesPath).get(0).get(1).decimalValue();


                for (PointOfInterest start : pOIs) {
                    final String typeID = "typeId";

                    if (start.getCoords().getLon().equals(lonS) && start.getCoords().getLat().equals(latS)) {
                        BigDecimal lonE =
                                link.get(geometryPath).get(coordinatesPath).get(1).get(0).decimalValue();
                        BigDecimal latE =
                                link.get(geometryPath).get(coordinatesPath).get(1).get(1).decimalValue();

                        for (PointOfInterest end : pOIs) {
                            if (end.getCoords().getLon().equals(lonE) && end.getCoords().getLat().equals(latE)) {
                                final int indexStadtbahn = 0;
                                final int indexBus = 1;
                                final int indexSiggi = 2;
                                final int indexBlack = 3;

                                if (link.get(propertiesPath).get(typeID).asText().equals(
                                        transports.get(indexStadtbahn).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPointOne().equals(start)
                                            && x.getPointTwo().equals(end))
                                            || links.stream().anyMatch(x -> x.getPointTwo().equals(start)
                                            && x.getPointOne().equals(end))) {
                                        for (Link linkDup : links) {
                                            if ((linkDup.getPointOne().equals(start)
                                                    && linkDup.getPointTwo().equals(end))
                                                    || (linkDup.getPointTwo().equals(start)
                                                    && linkDup.getPointOne().equals(end))) {
                                                linkDup.addType(transports.get(indexStadtbahn));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(indexStadtbahn));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get(propertiesPath).get(typeID).asText().equals(
                                        transports.get(indexBus).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPointOne().equals(start)
                                            && x.getPointTwo().equals(end))
                                            || links.stream().anyMatch(x -> x.getPointTwo().equals(start)
                                            && x.getPointOne().equals(end))) {
                                        for (Link linkDup : links) {
                                            if ((linkDup.getPointOne().equals(start)
                                                    && linkDup.getPointTwo().equals(end))
                                                    || (linkDup.getPointTwo().equals(start)
                                                    && linkDup.getPointOne().equals(end))) {
                                                linkDup.addType(transports.get(indexBus));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(indexBus));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get(propertiesPath).get(typeID).asText().equals(
                                        transports.get(indexSiggi).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPointOne().equals(start)
                                            && x.getPointTwo().equals(end))
                                            || links.stream().anyMatch(x -> x.getPointTwo().equals(start)
                                            && x.getPointOne().equals(end))) {
                                        for (Link linkDup : links) {
                                            if ((linkDup.getPointOne().equals(start)
                                                    && linkDup.getPointTwo().equals(end))
                                                    || (linkDup.getPointTwo().equals(start)
                                                    && linkDup.getPointOne().equals(end))) {
                                                linkDup.addType(transports.get(indexSiggi));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(indexSiggi));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get(propertiesPath).get(typeID).asText().equals(
                                        transports.get(indexBlack).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPointOne().equals(start)
                                            && x.getPointTwo().equals(end))
                                            || links.stream().anyMatch(x -> x.getPointTwo().equals(start)
                                            && x.getPointOne().equals(end))) {
                                        for (Link linkDup : links) {
                                            if ((linkDup.getPointOne().equals(start)
                                                    && linkDup.getPointTwo().equals(end))
                                                    || (linkDup.getPointTwo().equals(start)
                                                    && linkDup.getPointOne().equals(end))) {
                                                linkDup.addType(transports.get(indexBlack));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(indexBlack));
                                        links.add(new Link(start, end, type));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        links.sort(Comparator.comparing(a -> a.getPointOne().getName()));

        return links;
    }

    /**
     * Reads out the amount of Tickets the Detectives and M. X have at the start of the game.
     *
     * @param root Root of map.
     * @return Array of the amount of Tickets for each transporttype per player.
     */
    public String[] parseTickets(JsonNode root) {
        String[] amountTickets = new String[amountTicketTypes];

        Iterator<Map.Entry<String, JsonNode>> typesEntry = root.get(facilmapPath).get(typesPath).fields();
        ArrayList<Map.Entry<String, JsonNode>> entries = new ArrayList<>();
        while (typesEntry.hasNext()) {
            Map.Entry<String, JsonNode> entry = typesEntry.next();
            entries.add(entry);
        }

        int ticketIndex = 1;
        final int amntTickets = 5;
        for (Map.Entry<String, JsonNode> entry : entries) {
            if (entry.getValue().get(typePath).asText().equals(linePath)) {
                String amountOne = entry.getValue().get(fieldsPath).get(0).get(defaultPath).textValue();
                amountTickets[ticketIndex - 1] = amountOne;
                if (ticketIndex <= amntTickets) {
                    String amountTwo = entry.getValue().get(fieldsPath).get(1).get(defaultPath).textValue();
                    amountTickets[ticketIndex] = amountTwo;
                }
                ticketIndex += 2;
            }
        }
        return amountTickets;
    }

    /**
     * Parsing all POIs into GeoPoints in form of Object Arrays (GeoPoint, Name of POI)
     * and storing them in global ArrayList Variable geoPoints.
     */
    public void parseGeoPoints() {
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());

        for (PointOfInterest pOI : pOIs) {
            Object[] geoName = new Object[2];

            geoName[0] = new GeoPoint(pOI.getCoords().getLat().doubleValue(), pOI.getCoords().getLon().doubleValue());
            geoName[1] = pOI.getName();

            geoPoints.add(geoName);
        }
    }

    /**
     * Parsing all connections between POIs in form of Object Arrays (Polyline, GeoPoint1, GeoPoint2, transporttypes)
     * and storing them in global ArrayList Variable polylines.
     */
    public void parsePolylines() {
        ArrayList<Link> links = getLinks();
        ArrayList<Link> linksDuplicate = getLinks();

        for (int i = 0; i < linksDuplicate.size(); i++) {
            for (int j = 0; j < linksDuplicate.size(); j++) {
                if (j > i) {
                    if (linksDuplicate.get(i).getPointOne().equals(linksDuplicate.get(j).getPointTwo())
                            && linksDuplicate.get(i).getPointTwo().equals(linksDuplicate.get(j).getPointOne())) {
                        links.remove(linksDuplicate.get(j));
                    }
                }
            }
        }

        for (Link link : links) {
            final int polylineAttributes = 4;
            PointOfInterest pOIFirst = link.getPointOne();
            PointOfInterest pOISecond = link.getPointTwo();
            ArrayList<String> transporttypes = new ArrayList<>();

            ArrayList<GeoPoint> points = new ArrayList<>();
            for (Object[] geoPoint : geoPoints) {
                if (pOIFirst.getName().equals(geoPoint[1])) {
                    points.add((GeoPoint) geoPoint[0]);
                } else if (pOISecond.getName().equals(geoPoint[1])) {
                    points.add((GeoPoint) geoPoint[0]);
                }
            }
            for (Transport type : link.getType()) {
                transporttypes.add(type.getType());
            }
            Polyline line = new Polyline();
            line.setPoints(points);

            final int indexTransporttypes = 3;

            Object[] newPolyline = new Object[polylineAttributes];
            newPolyline[0] = line;
            newPolyline[1] = points.get(0);
            newPolyline[2] = points.get(1);
            newPolyline[indexTransporttypes] = transporttypes;

            polylines.add(newPolyline);
        }
    }

    //endregion

    //region get methods

    /**
     * All connections between POIs read out from map.
     *
     * @return ArrayList of all connections existing between POIs on map.
     */
    public ArrayList<Link> getLinks() {
        ArrayList<Link> links = new ArrayList<>();
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());

        for (PointOfInterest pOI : pOIs) {
            for (Link link : map.get(pOI)) {
                links.add(link);
            }
        }
        return links;
    }

    /**
     * All avialable transporttypes read out from map.
     *
     * @return ArrayList of all transporttypes existing on map.
     */
    public ArrayList<Transport> getTransporttypes() {
        return transports;
    }

    /**
     * All amounts of ticktes read out from map.
     *
     * @return ArrayList of amount of tickets read out from map.
     */
    public String[] getAmountTickets() {
        return amountTickets;
    }

    /**
     * Gives back all GeoPoints parsed from map.
     *
     * @return ArrayList of Objects (GeoPoint POI, Name of POI) representing GeoPoints.
     */
    public ArrayList<Object[]> getGeoPoints() {
        return geoPoints;
    }

    /**
     * Gives back all Polylines parsed from map.
     *
     * @return ArrayList of Objects (Polyline, GeoPoint1, GeoPoint2, transporttypes) representing Polylines.
     */
    public ArrayList<Object[]> getPolylines() {
        return polylines;
    }

    /**
     * Gives back GeoPoint of POI.
     *
     * @param name Name of POI.
     * @return GeoPoint from corresponding POI.
     */
    public GeoPoint getGeoPoint(String name) {
        for (Object[] point : geoPoints) {
            if (name.equals(point[1])) {
                return (GeoPoint) point[0];
            }
        }
        return null;
    }

    /**
     * Gives back all the POIs that are possible to visit from given POI.
     *
     * @param position Name of current position/POI.
     * @return ArrayList of Strings of all names of visitable POIs.
     */
    public ArrayList<String> getPossibleDestinations(String position) {
        ArrayList<String> possibleDestinations = new ArrayList<>();
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());
        PointOfInterest pOIPosition = null;

        for (PointOfInterest pOI : pOIs) {
            if (pOI.getName().equals(position)) {
                pOIPosition = pOI;
                break;
            }
        }

        ArrayList<Link> links = map.get(pOIPosition);

        for (Link link : links) {
            possibleDestinations.add(link.getPointTwo().getName());
        }
        return possibleDestinations;
    }

    /**
     * Gives Back all the possible transporttypes between two POIs.
     *
     * @param position    Name of current position/POI.
     * @param destination Name of destination POI.
     * @return ArrayList of all possible transporttypes between the two POIs.
     */
    public ArrayList<String> getPossibleTransporttypes(String position, String destination) {
        ArrayList<String> possibleTransporttypes = new ArrayList<>();
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());
        PointOfInterest positionPOI = new PointOfInterest(null, null);
        PointOfInterest destinationPOI = new PointOfInterest(null, null);

        for (PointOfInterest pOI : pOIs) {
            if (pOI.getName().equals(position)) {
                positionPOI = pOI;
            }
            if (pOI.getName().equals(destination)) {
                destinationPOI = pOI;
            }
        }

        ArrayList<Link> links = map.get(positionPOI);

        for (Link link : links) {
            if (link.getPointTwo().equals(destinationPOI)) {
                for (Transport transport : link.getType()) {
                    possibleTransporttypes.add(transport.getType());
                }
            }
        }
        return possibleTransporttypes;
    }

    //endregion

    //region different methods

    /**
     * All connections between POIs corresponding Coordinates and transporttypes as Strings
     * to print out in Logging-System.
     *
     * @return ArrayList of Strings of connections.
     */
    public ArrayList<String> outLinks() {
        ArrayList<String> outLinks = new ArrayList<>();
        ArrayList<Link> links = getLinks();

        for (Link link : links) {
            StringBuilder transport = new StringBuilder();

            for (Transport trans : link.getType()) {
                transport.append(", ").append(trans.getType());
            }

            String startLat = String.valueOf(link.getPointOne().getCoords().getLat());
            String startLon = String.valueOf(link.getPointOne().getCoords().getLon());
            String endLat = String.valueOf(link.getPointTwo().getCoords().getLat());
            String endLon = String.valueOf(link.getPointTwo().getCoords().getLon());

            String logOut = outLinkHelp(
                    link.getPointOne().getName(), startLat, startLon,
                    link.getPointTwo().getName(), endLat, endLon,
                    transport.toString()
            );

            outLinks.add(logOut);
        }
        return outLinks;
    }

    /**
     * Supporting method for outLinks() that creates a String with a String builder to keep Code clear.
     *
     * @param startName Name of start POI.
     * @param startLat  Lat coordinate of start POI.
     * @param startLon  Lon coordinate of start POI.
     * @param endName   Name of end POI.
     * @param endLat    Lat coordinate of end POI.
     * @param endLon    Lon coordinate of end POI.
     * @param transport Transporttypes between POIs.
     * @return Return String with information about connection to print out in Logging-System.
     */
    private String outLinkHelp(String startName, String startLat, String startLon,
                               String endName, String endLat, String endLon, String transport) {
        String logOut = String.format("%1s" + " (%2s" + ", %3s" + ") -> %4s" + " (%5s" + "  %6s" + ")%7s",
                startName, startLat, startLon, endName, endLat, endLon, transport);
        return logOut;
    }

    /**
     * Generates random position from all POIs from map.
     *
     * @return Name of random POI.
     */
    public String genStartPosition() {
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());

        return (pOIs.get((int) (Math.random() * pOIs.size()))).getName();
    }

    /**
     * Checks if connection between two POIs exist with given transporttype.
     *
     * @param position      Name of current position/POI.
     * @param destination   Name of destination POI.
     * @param transporttype Name of chosen transporttype.
     * @return true when connection between two POIs exist with transporttype, else false.
     */
    public boolean checkLinkExists(String position, String destination, String transporttype)
            throws InvalidConnectionException {
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());
        ArrayList<String> possibleTransporttypes = getPossibleTransporttypes(position, destination);

        for (String types : possibleTransporttypes) {
            if (types.equals(transporttype)) {
                return true;
            }
        }

        throw new InvalidConnectionException("No connection between " + position
                + "and " + destination + " (" + transporttype + ")");
    }

    //endregion

}
