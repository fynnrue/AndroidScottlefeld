package de.techfak.gse.fruehlemann.model;

public class Transport {
    private String type;
    private String id;

    public Transport(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
