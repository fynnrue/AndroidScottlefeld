package de.techfak.gse.fruehlemann;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ParserMap {
    ArrayList<PointOfInterest> pOIs;
    ArrayList<Transport> transports;
    ArrayList<Link> links;


    public ArrayList<PointOfInterest> getPOIs(JsonNode root) {
        Set<PointOfInterest> pOIsUnduped = new HashSet<>();

        for (JsonNode jn : root.get("features")) {

            String featureType = jn.get("geometry").get("type").asText();

            if (featureType.equals("Point")) {
                BigDecimal latP = jn.get("geometry").get("coordinates").get(0).decimalValue();
                BigDecimal lonP = jn.get("geometry").get("coordinates").get(1).decimalValue();

                pOIsUnduped.add(new PointOfInterest(
                        jn.get("properties").get("name").asText(),
                        (new Coordinate(latP, lonP))
                ));
            }
        }
        pOIs = new ArrayList<>(pOIsUnduped);

        return pOIs;
    }

    public ArrayList<Transport> getTransporttypes(JsonNode root) {
        transports = new ArrayList<>();
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

    public ArrayList<Link> getLinks(JsonNode root) {
        links = new ArrayList<>();

        for (JsonNode link : root.get("features")) {
            String featType = link.get("geometry").get("type").asText();

            if (featType.equals("LineString")) {
                BigDecimal latS = link.get("geometry").get("coordinates").get(0).get(0).decimalValue();
                BigDecimal lonS = link.get("geometry").get("coordinates").get(0).get(1).decimalValue();


                for (PointOfInterest start : pOIs) {
                    if (start.getCoords().getLat().equals(latS) && start.getCoords().getLon().equals(lonS)) {
                        BigDecimal latE = link.get("geometry").get("coordinates").get(1).get(0).decimalValue();
                        BigDecimal lonE = link.get("geometry").get("coordinates").get(1).get(1).decimalValue();

                        for (PointOfInterest end : pOIs) {
                            if (end.getCoords().getLat().equals(latE) && end.getCoords().getLon().equals(lonE)) {

                                if (link.get("properties").get("typeId").asText().equals(
                                        transports.get(0).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPoint1().equals(start)
                                            && x.getPoint2().equals(end))) {
                                        for (Link linkDup : links) {
                                            if (linkDup.getPoint1().equals(start) && linkDup.getPoint2().equals(end)) {
                                                linkDup.addType(transports.get(0));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(0));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get("properties").get("typeId").asText().equals(
                                        transports.get(1).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPoint1().equals(start)
                                            && x.getPoint2().equals(end))) {
                                        for (Link linkDup : links) {
                                            if (linkDup.getPoint1().equals(start) && linkDup.getPoint2().equals(end)) {
                                                linkDup.addType(transports.get(1));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(1));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get("properties").get("typeId").asText().equals(
                                        transports.get(2).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPoint1().equals(start)
                                            && x.getPoint2().equals(end))) {
                                        for (Link linkDup : links) {
                                            if (linkDup.getPoint1().equals(start) && linkDup.getPoint2().equals(end)) {
                                                linkDup.addType(transports.get(2));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(2));
                                        links.add(new Link(start, end, type));
                                    }
                                } else if (link.get("properties").get("typeId").asText().equals(
                                        transports.get(3).getId())) {
                                    if (links.stream().anyMatch(x -> x.getPoint1().equals(start)
                                            && x.getPoint2().equals(end))) {
                                        for (Link linkDup : links) {
                                            if (linkDup.getPoint1().equals(start) && linkDup.getPoint2().equals(end)) {
                                                linkDup.addType(transports.get(3));
                                            }
                                        }
                                    } else {
                                        ArrayList<Transport> type = new ArrayList<>();
                                        type.add(transports.get(3));
                                        links.add(new Link(start, end, type));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        links.sort(Comparator.comparing(a -> a.getPoint1().getName()));

        return links;
    }
}
