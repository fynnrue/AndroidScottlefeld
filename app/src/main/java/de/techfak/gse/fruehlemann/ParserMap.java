package de.techfak.gse.fruehlemann;

import android.graphics.Point;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ParserMap {
    HashMap<PointOfInterest, ArrayList<Link>> map = new HashMap<>();
    ArrayList<Transport> transports;

    //Creates HashMap wich saves all POIs with their connections and transporttypes
    public void parseMap(JsonNode root) {
        ArrayList<PointOfInterest> pOIs;
        ArrayList<Link> links;

        pOIs = parsePOIs(root);
        transports = parseTransporttypes(root);
        links = parseLinks(root, pOIs, transports);

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
    }

    public ArrayList<PointOfInterest> parsePOIs(JsonNode root) {
        ArrayList<PointOfInterest> pOIs;
        Set<PointOfInterest> pOIsUnduped = new HashSet<>();

        for (JsonNode jn : root.get("features")) {

            String featureType = jn.get("geometry").get("type").asText();

            if (featureType.equals("Point")) {
                BigDecimal lonP = jn.get("geometry").get("coordinates").get(0).decimalValue();
                BigDecimal latP = jn.get("geometry").get("coordinates").get(1).decimalValue();

                pOIsUnduped.add(new PointOfInterest(
                        jn.get("properties").get("name").asText(),
                        (new Coordinate(latP, lonP))
                ));
            }
        }
        pOIs = new ArrayList<>(pOIsUnduped);

        return pOIs;
    }

    public ArrayList<Transport> parseTransporttypes(JsonNode root) {
        ArrayList<Transport> transports = new ArrayList<>();
        ArrayList<String[]> types = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> typesEntry = root.get("facilmap").get("types").fields();
        ArrayList<Map.Entry<String, JsonNode>> entries = new ArrayList<>();
        while (typesEntry.hasNext()) {
            Map.Entry<String, JsonNode> entry = typesEntry.next();
            entries.add(entry);
        }

        for (Map.Entry<String, JsonNode> entry : entries) {
            if (entry.getValue().get("type").asText().equals("line")) {
                String[] type = new String[2];


                type[0] = entry.getValue().get("name").asText();
                type[1] = entry.getKey();

                types.add(type);
            }
        }

        for (String[] type : types) {
            transports.add(new Transport(type[0], type[1]));
        }

        return transports;
    }

    public ArrayList<Link> parseLinks(JsonNode root, ArrayList<PointOfInterest> pOIs, ArrayList<Transport> transports) {
        ArrayList<Link> links = new ArrayList<>();

        for (JsonNode link : root.get("features")) {
            String featType = link.get("geometry").get("type").asText();

            if (featType.equals("LineString")) {
                BigDecimal lonS = link.get("geometry").get("coordinates").get(0).get(0).decimalValue();
                BigDecimal latS = link.get("geometry").get("coordinates").get(0).get(1).decimalValue();


                for (PointOfInterest start : pOIs) {
                    if (start.getCoords().getLon().equals(lonS) && start.getCoords().getLat().equals(latS)) {
                        BigDecimal lonE = link.get("geometry").get("coordinates").get(1).get(0).decimalValue();
                        BigDecimal latE = link.get("geometry").get("coordinates").get(1).get(1).decimalValue();

                        for (PointOfInterest end : pOIs) {
                            if (end.getCoords().getLon().equals(lonE) && end.getCoords().getLat().equals(latE)) {
                                final int indexStadtbahn = 0;
                                final int indexBus = 1;
                                final int indexSiggi = 2;
                                final int indexBlack = 3;

                                if (link.get("properties").get("typeId").asText().equals(
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
                                } else if (link.get("properties").get("typeId").asText().equals(
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
                                } else if (link.get("properties").get("typeId").asText().equals(
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
                                } else if (link.get("properties").get("typeId").asText().equals(
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

    public ArrayList<PointOfInterest> getPOIs() {
        return (new ArrayList<>(map.keySet()));
    }

    public ArrayList<Transport> getTransporttypes() {
        return transports;
    }

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

            String logOut = getString(
                    link.getPointOne().getName(), startLat, startLon,
                    link.getPointTwo().getName(), endLat, endLon,
                    transport.toString()
            );

            outLinks.add(logOut);
        }
        return outLinks;
    }

    private String getString(String startName, String startLat, String startLon,
                             String endName, String endLat, String endLon, String transport) {
        String logOut = String.format("%1s" + " (%2s" + ", %3s" + ") -> %4s" + " (%5s" + "  %6s" + ")%7s",
                startName, startLat, startLon, endName, endLat, endLon, transport);
        return logOut;
    }


    public String genStartPosition() {
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());

        return (pOIs.get((int) (Math.random() * pOIs.size()))).getName();
    }

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
                for (Transport transport:  link.getType()) {
                    possibleTransporttypes.add(transport.getType());
                }
            }
        }
        return possibleTransporttypes;
    }

    public boolean linkExists(String position, String destination, String transporttype) {
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());
        ArrayList<String> possibleTransporttypes = getPossibleTransporttypes(position, destination);

        for (String types : possibleTransporttypes) {
            if (types.equals(transporttype)) {
                return true;
            }
        }

        return false;
    }

    public BigDecimal[] getCoordinates(String pOIName) {
        BigDecimal[] coords = new BigDecimal[2];
        ArrayList<PointOfInterest> pOIs = new ArrayList<>(map.keySet());

        for (PointOfInterest pOI : pOIs) {
            if (pOI.getName().equals(pOIName)) {
                coords[0] = pOI.getCoords().getLat();
                coords[1] = pOI.getCoords().getLon();

                break;
            }
        }
        return coords;
    }
}
