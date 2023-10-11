package io.smallrye.graphql.tests.client.typesafe.calendar;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeCalendarTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "typesafe-calendar.war")
                .addClasses(SomeApi.class);
    }

    @ArquillianResource
    URL testingURL;

    private ClientSomeApi client;

    @Before
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(ClientSomeApi.class);
    }

    @Test
    public void queryWithCalendarReturnTypeAndArgumentTest() {
        Calendar calendar = new Calendar.Builder()
                .setDate(2000, 12, 30)
                .setTimeOfDay(4, 20, 45, 314)
                .build();

        Calendar resultCalendar = client.someCalendar(calendar);
        assertEquals(calendar, resultCalendar);
    }

    @Test
    public void queryWithGregorianCalendarReturnTypeAndArgumentTest() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(2000,
                31,
                12,
                3,
                4,
                5);
        gregorianCalendar.setTimeInMillis(128); // for some reason the constructor is not public
        GregorianCalendar resultGregorianCalendar = client.someGregorianCalendar(gregorianCalendar);
        assertEquals(gregorianCalendar, resultGregorianCalendar);
    }
}
