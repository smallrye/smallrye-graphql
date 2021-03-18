package io.smallrye.graphql.test.apps.scalars.api;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AdditionalDateScalars {

    private final Date date;
    private final java.sql.Date sqlDate;
    private final Timestamp sqlTimestamp;
    private final Time sqlTime;
    private final Instant now;

    public AdditionalDateScalars() {
        this.now = LocalDateTime.parse("2006-01-02T15:04:05.876").atZone(ZoneId.systemDefault()).toInstant();
        this.date = Date.from(now);
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

    public Instant getInstant() {
        return now;
    }

}
