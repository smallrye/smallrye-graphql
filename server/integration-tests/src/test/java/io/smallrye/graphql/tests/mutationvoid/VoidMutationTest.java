package io.smallrye.graphql.tests.mutationvoid;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
@RunAsClient
public class VoidMutationTest {
    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "voidMutation-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Rectangle.class, RectangleService.class, RectangleResources.class);
    }

    @ArquillianResource
    URL testingURL;

    @Before
    public void resetDatabase() {
        new GraphQLAssured(testingURL).post("mutation { resetRectangles }");
    }

    @Test
    public void mutationWithVoidTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation { createRectangle ( rectangle: { width:14.42, height:4.31} ) }");
        assertThat(response).isEqualTo("{\"data\":{\"createRectangle\":null}}");

        String check = graphQLAssured
                .post("query { findAllRectangles { width height } }");
        // ------
        assertThat(check).doesNotContain("errors");
        assertThat(check).contains("\"width\":14.42");
        assertThat(check).contains("\"height\":4.31");
        // ------
    }

    @Test
    public void errorMutationWithVoidTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation { createRectangleError ( rectangle: { width:14.42, height:4.31} ) }");
        assertThat(response).contains("errors");

        String check = graphQLAssured
                .post("query { findAllRectangles { width height } }");
        // ------
        assertThat(check).doesNotContain("errors");
        assertThat(check).doesNotContain("\"width\":14.42");
        assertThat(check).doesNotContain("\"height\":4.31");
        // ------
    }

    @Test
    public void uniVoidMutationTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation { someUniMutation ( rectangle: { width:14.42, height:4.31} ) }");
        assertThat(response).isEqualTo("{\"data\":{\"someUniMutation\":null}}");

    }

    @Test
    public void errorUniVoidMutationTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation { someUniMutationThrowsError ( rectangle: { width:14.42, height:4.31} ) }");
        assertThat(response).contains("errors");
    }

    @Test
    public void mutationWithPrimitiveVoidTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation { primitiveCreateRectangle ( rectangle: { width:24.42, height:1.31} ) }");
        assertThat(response).isEqualTo("{\"data\":{\"primitiveCreateRectangle\":null}}");

        String check = graphQLAssured
                .post("query { findAllRectangles { width height } }");
        // ------
        assertThat(check).doesNotContain("errors");
        assertThat(check).contains("\"width\":24.42");
        assertThat(check).contains("\"height\":1.31");
        // ------
    }

    @Test
    public void errorMutationWithPrimitiveVoidTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation { primitiveCreateRectangleError ( rectangle: { width:16.42, height:44.31} ) }");
        assertThat(response).contains("errors");

        String check = graphQLAssured
                .post("query { findAllRectangles { width height } }");
        // ------
        assertThat(check).doesNotContain("errors");
        assertThat(check).doesNotContain("\"width\":16.42");
        assertThat(check).doesNotContain("\"height\":44.31");
        // ------
    }

    @Test
    public void mutationWithBothPrimitiveVoidAndWrapperVoidTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation { primitiveCreateRectangle ( rectangle: { width:24.42, height:1.31} ) createRectangle ( rectangle: { width:14.42, height:4.31}) }");
        assertThat(response).isEqualTo("{\"data\":{\"primitiveCreateRectangle\":null,\"createRectangle\":null}}");

        String check = graphQLAssured
                .post("query { findAllRectangles { width height } }");
        // ------
        assertThat(check).doesNotContain("errors");
        assertThat(check).contains("\"width\":24.42");
        assertThat(check).contains("\"height\":1.31");
        assertThat(check).contains("\"width\":14.42");
        assertThat(check).contains("\"height\":4.31");
        // ------
    }

}
