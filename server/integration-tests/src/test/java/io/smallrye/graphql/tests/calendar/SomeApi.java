package io.smallrye.graphql.tests.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class SomeApi {
    @Query
    public Calendar someCalendar(Calendar calendar) {
        return calendar;
    }

    @Query
    public GregorianCalendar someGregorianCalendar(GregorianCalendar calendar) {
        return calendar;
    }

    @Query
    @DateFormat("dd'.' MMMM yyyy 'at' hh':'mm a")
    public Calendar someFormattedCalendar(@DateFormat("yyyy MM 'at' HH 'hours'") Calendar calendar) {
        return calendar;
    }

    @Query
    @DateFormat("dd'.' MMMM yyyy 'at' hh':'mm a")
    public GregorianCalendar someFormattedGregorianCalendar(@DateFormat("yyyy MM 'at' HH 'hours'") GregorianCalendar calendar) {
        return calendar;
    }
}
