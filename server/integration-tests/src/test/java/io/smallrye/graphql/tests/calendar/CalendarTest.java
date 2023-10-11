package io.smallrye.graphql.tests.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
@RunAsClient
public class CalendarTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "calendar-test.war")
                .addClasses(SomeApi.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void queryWithCalendarReturnTypeAndArgumentTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("{ someCalendar(calendar: \"2018-05-05T11:50:45.314Z\") }");
        assertThat(response).contains("{\"data\":{\"someCalendar\":\"2018-05-05T11:50:45.314Z\"}}")
                .doesNotContain("error");
    }

    @Test
    public void queryWithGregorianCalendarReturnTypeAndArgumentTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("{ someGregorianCalendar(calendar: \"2011-05-05T11:50:45.112Z\") }");
        assertThat(response).contains("{\"data\":{\"someGregorianCalendar\":\"2011-05-05T11:50:45.112Z\"}}")
                .doesNotContain("error");
    }

    @Test
    public void queryWithFormattedCalendarReturnTypeAndArgumentTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("{ someFormattedCalendar(calendar: \"2023 04 at 13 hours\") }");
        assertThat(response).contains("{\"data\":{\"someFormattedCalendar\":\"01. April 2023 at 01:00 PM\"}}")
                .doesNotContain("error");
    }

    @Test
    public void queryWithFormattedGregorianCalendarReturnTypeAndArgumentTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("{ someFormattedGregorianCalendar(calendar: \"2023 04 at 13 hours\") }");
        assertThat(response).contains("{\"data\":{\"someFormattedGregorianCalendar\":\"01. April 2023 at 01:00 PM\"}}")
                .doesNotContain("error");
    }

    @Test
    public void queryWithWrongCalendarFormatTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("{ someCalendar(calendar: \"30th of August 2000\") }");
        assertThat(response).containsIgnoringWhitespaces("{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"message\": \"argument 'calendar' with value 'StringValue{value='30th of August 2000'}' is not a valid 'DateTime'\",\n"
                +
                "      \"locations\": [\n" +
                "        {\n" +
                "          \"line\": 1,\n" +
                "          \"column\": 16\n" +
                "        }\n" +
                "      ],\n" +
                "      \"extensions\": {\n" +
                "        \"classification\": \"ValidationError\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"data\": {\n" +
                "    \"someCalendar\": null\n" +
                "  }\n" +
                "}");
    }

    @Test
    public void queryWithWrongGregorianCalendarFormatTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("{ someGregorianCalendar(calendar: \"30th of August 2000\") }");
        assertThat(response).containsIgnoringWhitespaces("{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"message\": \"argument 'calendar' with value 'StringValue{value='30th of August 2000'}' is not a valid 'DateTime'\",\n"
                +
                "      \"locations\": [\n" +
                "        {\n" +
                "          \"line\": 1,\n" +
                "          \"column\": 25\n" +
                "        }\n" +
                "      ],\n" +
                "      \"extensions\": {\n" +
                "        \"classification\": \"ValidationError\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"data\": {\n" +
                "    \"someGregorianCalendar\": null\n" +
                "  }\n" +
                "}");
    }
}
