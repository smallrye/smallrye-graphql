package io.smallrye.graphql.bootstrap;

public enum LogPayloadOption {

    OFF("off"),
    TRUE("withoutvariable"),
    FALSE("off"),
    WITHOUTVARIABLE("withoutvariable"),
    WITHVARIABLE("withvariable");

    private String payload;

    LogPayloadOption(String payload) {
        this.payload = payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
