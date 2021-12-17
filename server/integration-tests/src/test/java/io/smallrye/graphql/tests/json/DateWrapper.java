package io.smallrye.graphql.tests.json;

import java.util.Date;

public class DateWrapper {

    // For deserializing this will use a custom date format: MM dd yyyy HH:mm Z
    // thanks to the logic in `CustomJsonbService`.
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
