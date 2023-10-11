package io.smallrye.graphql.tests.client.typesafe.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.microprofile.graphql.Query;

public interface ClientSomeApi {
    @Query
    Calendar someCalendar(Calendar calendar);

    @Query
    GregorianCalendar someGregorianCalendar(GregorianCalendar calendar);

}
