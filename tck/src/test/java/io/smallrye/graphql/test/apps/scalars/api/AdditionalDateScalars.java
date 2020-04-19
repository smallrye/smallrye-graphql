package io.smallrye.graphql.test.apps.scalars.api;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AdditionalDateScalars {

    private Date date;
    private java.sql.Date sqlDate;
    private Timestamp sqlTimestamp;
    private Time sqlTime;

    public AdditionalDateScalars() {
        this.date = Date.from(LocalDateTime.parse("2006-01-02T15:04:05.800").atZone(ZoneId.systemDefault()).toInstant());
        this.sqlDate = new java.sql.Date(date.getTime());
        this.sqlTimestamp = new Timestamp(date.getTime());
        this.sqlTime = new Time(date.getTime());
    }

    public Date getDate() {
        return date;
    }

    public java.sql.Date getSqlDate() {
        return sqlDate;
    }

    public Timestamp getSqlTimestamp() {
        return sqlTimestamp;
    }

    public Time getSqlTime() {
        return sqlTime;
    }
}
