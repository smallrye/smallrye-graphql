package io.smallrye.graphql.tests.json;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

/**
 * Test an application that provides its own customized `Jsonb` objects for certain model classes.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class CustomJsonbConfigTestCase {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "custom-jsonb.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset(CustomJsonbService.class.getName()),
                        "META-INF/services/io.smallrye.graphql.spi.EventingService")
                .addClasses(DateWrapper.class, CustomJsonbService.class, MyApi.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void tryInputDateWithCustomFormat() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured.post("{echo(dateWrapper: {date: \"01 01 1999 08:20 +0300\"}) {date}}");
        // for serializing the date (to return to the client), the default formatter will be used, and that translates it into UTC
        Assert.assertEquals("{\"data\":{\"echo\":{\"date\":\"1999-01-01T05:20:00\"}}}", response);
    }

}
