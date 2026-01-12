package io.smallrye.graphql.tests.client.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class TypesafeClientFormatAnnotationsTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(FormatAnnotationsApi.class, ObjectWithFormattedFields.class);
    }

    @ArquillianResource
    URL testingURL;

    protected FormatAnnotationsClientApi client;

    @BeforeEach
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(FormatAnnotationsClientApi.class);
    }

    /**
     * Test parsing a date formatted using a @DateFormat annotation
     */
    @Test
    public void testParsingDateWithCustomFormat() {
        Date parsedDate = client.something().getDate();
        Calendar calendar = toCalendar(parsedDate);
        assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        assertEquals(13, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(1997, calendar.get(Calendar.YEAR));
    }

    /**
     * Test parsing a date formatted using a @JsonbDateFormat annotation
     */
    @Test
    public void testParsingDateWithJsonbFormatAnnotation() {
        Date parsedDate = client.something().getDateWithJsonbAnnotation();
        Calendar calendar = toCalendar(parsedDate);
        assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        assertEquals(13, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(1997, calendar.get(Calendar.YEAR));
    }

    /**
     * Test parsing a double formatted using a @NumberFormat annotation
     */
    @Test
    public void testParsingDoubleWithCustomFormat() {
        Double parsedNumber = client.something().getDoubleNumber();
        assertEquals(12345678.9, parsedNumber, 0.00001);
    }

    /**
     * Test parsing a long formatted using a @NumberFormat annotation
     */
    @Test
    public void testParsingLongWithCustomFormat() {
        Long parsedNumber = client.something().getLongNumber();
        assertEquals((Long) 123456789L, parsedNumber);
    }

    private Calendar toCalendar(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return instance;
    }

}
