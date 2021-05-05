package io.smallrye.graphql.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an injection point that should receive a named GraphQL client instance.
 * Configuration of the client needs to be provided through config properties.
 *
 * Example of injecting a dynamic client:
 *
 * `@Inject`
 * `@NamedClient("countries")`
 * DynamicGraphQLClient countriesClient;
 *
 * Definition of the client via system properties:
 * smallrye.graphql.client.countries/url=https://countries.trevorblades.com/
 * Passing a HTTP header:
 * smallrye.graphql.client.countries/header/Authorization=Bearer 123456
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface NamedClient {

    String value();

}
