package de.techfak.gse.fruehlemann;

public class Transport {
    private String type;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Transport(String type, String id) {
        this.type = type;
        this.id = id;
    }
}
