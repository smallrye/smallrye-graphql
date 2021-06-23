package io.smallrye.graphql.test.apps.scalars.api;

import static java.time.ZoneOffset.UTC;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

public class AdditionalDateScalars {

    private final Date date;
    private final java.sql.Date sqlDate;
    private final Timestamp sqlTimestamp;
    private final Time sqlTime;
    private final Instant instant;

    public AdditionalDateScalars() {
        this.instant = Instant.parse("2006-01-02T15:04:05.876Z");
        this.date = Date.from(instant);
        LocalDateTime local = LocalDateTime.ofInstant(instant, UTC);
        this.sqlDate = java.sql.Date.valueOf(local.toLocalDate());
        this.sqlTimestamp = Timestamp.valueOf(local);
        this.sqlTime = Time.valueOf(local.toLocalTime());
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
        return instant;
    }
}
