package io.smallrye.graphql.client.typesafe.api;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Places a parameter not on the query field but some field deeper in the request,
 * e.g. when you request a team but need to limit the number of members returned:
 *
 * <pre>
 * &#64;GraphQlClientApi
 * interface TeamsApi {
 *     Team team(String teamName, @NestedParameter("members") int limit);
 * }
 * </pre>
 * <p>
 * Generates a request:
 *
 * <pre>
 * query team($teamName: String, $limit: Int!) {
 *   team(teamName: $teamName) {
 *     headQuarter
 *     members(limit: $limit) {
 *       name
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * To nest a parameter deeper in the request, provide the path of the fields delimited with a period,
 * e.g. <code>teams.members</code>. Note that multiple parameters can be bound to the same path.
 * <p>
 * The value is an array, so the same parameter value can be used for multiple destinations,
 * e.g. for {@link Multiple} requests.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface NestedParameter {
    String[] value();
}
