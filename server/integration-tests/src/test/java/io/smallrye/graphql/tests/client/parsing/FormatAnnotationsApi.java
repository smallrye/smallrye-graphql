package io.smallrye.graphql.tests.client.parsing;

import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class FormatAnnotationsApi {

    @Query
    public ObjectWithFormattedFields something() {
        ObjectWithFormattedFields objectWithFormattedFields = new ObjectWithFormattedFields();
        Calendar calendar = Calendar.getInstance();
        // this will be formatted as
        // May 1997 13 04,20,03 May Tue
        calendar.set(1997, Calendar.MAY, 13, 4, 20, 3);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectWithFormattedFields.setDate(calendar.getTime());
        objectWithFormattedFields.setDateWithJsonbAnnotation(calendar.getTime());

        objectWithFormattedFields.setDoubleNumber(12345678.9);
        objectWithFormattedFields.setLongNumber(123456789L);
        return objectWithFormattedFields;
    }

}
