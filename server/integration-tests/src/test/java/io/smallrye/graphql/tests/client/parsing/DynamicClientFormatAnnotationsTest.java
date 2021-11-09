package io.smallrye.graphql.tests.client.parsing;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;

@RunWith(Arquillian.class)
@RunAsClient
public class DynamicClientFormatAnnotationsTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(FormatAnnotationsApi.class, ObjectWithFormattedFields.class);
    }

    @ArquillianResource
    URL testingURL;

    private static VertxDynamicGraphQLClient client;

    @Before
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    @After
    public void cleanup() {
        client.close();
    }

    /**
     * Test parsing a date formatted using a @DateFormat annotation
     */
    @Test
    public void testParsingDateWithCustomFormat() throws ExecutionException, InterruptedException {
        Document document = document(operation(
                field("something",
                        field("date"))));
        Response response = client.executeSync(document);

        // a sanity check to make sure that the server really returned the custom format
        // because if not, then this test would not be actually verifying anything
        assertEquals("Sanity check failed: the server did not return the date in the desired custom format",
                "May 1997 13 04,20,03 May Tue",
                response.getData().getJsonObject("something").getString("date"));

        ObjectWithFormattedFields objectWithFormattedFields = response.getObject(ObjectWithFormattedFields.class, "something");
        Date parsedDate = objectWithFormattedFields.getDate();
        Calendar calendar = toCalendar(parsedDate);
        assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        assertEquals(13, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(1997, calendar.get(Calendar.YEAR));
    }

    /**
     * Test parsing a date formatted using a @JsonbDateFormat annotation
     */
    @Test
    public void testParsingDateWithJsonbFormatAnnotation() throws ExecutionException, InterruptedException {
        Document document = document(operation(
                field("something",
                        field("dateWithJsonbAnnotation"))));
        Response response = client.executeSync(document);

        ObjectWithFormattedFields objectWithFormattedFields = response.getObject(ObjectWithFormattedFields.class, "something");
        Date parsedDate = objectWithFormattedFields.getDateWithJsonbAnnotation();
        Calendar calendar = toCalendar(parsedDate);
        assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        assertEquals(13, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(1997, calendar.get(Calendar.YEAR));
    }

    /**
     * Test parsing a double formatted using a @NumberFormat annotation
     */
    @Test
    public void testParsingDoubleWithCustomFormat() throws ExecutionException, InterruptedException {
        Document document = document(operation(
                field("something",
                        field("doubleNumber"))));
        Response response = client.executeSync(document);

        assertEquals("Sanity check failed: the server did not return the number in the desired custom format",
                "12.345.678,9",
                response.getData().getJsonObject("something").getString("doubleNumber"));

        ObjectWithFormattedFields objectWithFormattedFields = response.getObject(ObjectWithFormattedFields.class, "something");
        Double parsedNumber = objectWithFormattedFields.getDoubleNumber();
        assertEquals(12345678.9, parsedNumber, 0.00001);
    }

    /**
     * Test parsing a long formatted using a @NumberFormat annotation
     */
    @Test
    public void testParsingLongWithCustomFormat() throws ExecutionException, InterruptedException {
        Document document = document(operation(
                field("something",
                        field("longNumber"))));
        Response response = client.executeSync(document);
        ObjectWithFormattedFields objectWithFormattedFields = response.getObject(ObjectWithFormattedFields.class, "something");
        Long parsedNumber = objectWithFormattedFields.getLongNumber();
        assertEquals((Long) 123456789L, parsedNumber);
    }

    private Calendar toCalendar(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return instance;
    }

}
