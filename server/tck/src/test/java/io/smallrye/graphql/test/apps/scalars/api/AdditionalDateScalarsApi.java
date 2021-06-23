package io.smallrye.graphql.test.apps.scalars.api;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class AdditionalDateScalarsApi {

    @Query
    public AdditionalDateScalars additionalDateScalars() {
        return new AdditionalDateScalars();
    }

    public java.time.Instant instantInput(@Source AdditionalDateScalars additionalDateScalars, java.time.Instant instant) {
        return instant;
    }

    public java.util.Date dateInput(@Source AdditionalDateScalars additionalDateScalars, java.util.Date date) {
        return date;
    }

    public java.sql.Date sqlDateInput(@Source AdditionalDateScalars additionalDateScalars, java.sql.Date date) {
        return date;
    }

    public java.sql.Timestamp sqlTimestampInput(@Source AdditionalDateScalars additionalDateScalars,
            java.sql.Timestamp timestamp) {
        return timestamp;
    }

    public java.sql.Time sqlTimeInput(@Source AdditionalDateScalars additionalDateScalars, java.sql.Time time) {
        return time;
    }

    public java.time.Instant instantDefault(@Source AdditionalDateScalars additionalDateScalars,
            @DefaultValue("2006-01-02T15:04:05.876Z") java.time.Instant instant) {
        return instant;
    }

    public java.util.Date dateDefault(@Source AdditionalDateScalars additionalDateScalars,
            @DefaultValue("2006-01-02T15:04:05.876") java.util.Date date) {
        return date;
    }

    public java.sql.Date sqlDateDefault(@Source AdditionalDateScalars additionalDateScalars,
            @DefaultValue("2006-01-02") java.sql.Date date) {
        return date;
    }

    public java.sql.Timestamp sqlTimestampDefault(@Source AdditionalDateScalars additionalDateScalars,
            @DefaultValue("2006-01-02T15:04:05.876") java.sql.Timestamp timestamp) {
        return timestamp;
    }

    public java.sql.Time sqlTimeDefault(@Source AdditionalDateScalars additionalDateScalars,
            @DefaultValue("15:04:05") java.sql.Time time) {
        return time;
    }
}
