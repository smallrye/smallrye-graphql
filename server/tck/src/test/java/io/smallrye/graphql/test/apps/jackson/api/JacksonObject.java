package io.smallrye.graphql.test.apps.jackson.api;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JacksonObject {

    @JsonProperty("name")
    private String names;
    @JsonIgnore
    private String secret;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy MM dd HH:mm")
    private LocalDateTime time;

    public JacksonObject() {
    }

    public JacksonObject(String names, String secret, LocalDateTime time) {
        this.names = names;
        this.secret = secret;
        this.time = time;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

}
