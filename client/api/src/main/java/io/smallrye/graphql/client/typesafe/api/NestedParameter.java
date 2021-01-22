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
 * <code>
 * &#64;GraphQlClientApi
 * interface TeamsApi {
 *     Team team(String teamName, @NestedParameter("members") int limit);
 * }
 * </code>
 * </pre>
 *
 * Generates a request:
 *
 * <pre>
 * <code>
 * query team($teamName: String, $limit: Int!) {
 *   team(teamName: $teamName) {
 *     headQuarter
 *     members(limit: $limit) {
 *       name
 *     }
 *   }
 * }
 * </code>
 * </pre>
 *
 * To nest a parameter deeper in the request, provide the path of the fields delimited with a period,
 * e.g. <code>teams.members</code>. Note that multiple parameters can be bound to the same path.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface NestedParameter {
    String value();
}
