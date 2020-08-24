package io.smallrye.graphql.test.apps.profile.api;

import java.time.LocalDateTime;
import java.time.Month;

import org.eclipse.microprofile.graphql.DateFormat;

public class Timestamp {

    @DateFormat("dd MMMM yyyy 'at' HH:mm:ss")
    private LocalDateTime dateTime;

    public Timestamp() {
        dateTime = LocalDateTime.of(1978, Month.JULY, 3, 3, 15, 45);
    }

    public Timestamp(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
