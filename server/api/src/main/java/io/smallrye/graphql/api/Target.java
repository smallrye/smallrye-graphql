package io.smallrye.graphql.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;

import io.smallrye.common.annotation.Experimental;

/**
 * Extends a GraphQL input type by adding an externally defined input field.
 * <br>
 * <br>
 * The GraphQL input type which is extended is the GraphQL input type corresponding to the Java type of the annotated
 * method parameter.
 * <br>
 * <br>
 * At runtime, when a mutation receives input containing the added field, the method declaring the annotated parameter
 * is invoked for that field. This allows application code to handle values that are supplied through the extended input
 * model.
 * <br>
 * <br>
 * By default, the name of the added input field is the name of the method.
 *
 * <br>
 * <br>
 * For example, a user might annotate a method's parameter as such:
 *
 * <pre>
 * public class CharacterService {
 *     public Character addTwitterHandle(
 *                          {@literal @}Target Character character,
 *                          String twitterHandle) {
 *         character.setTwitterHandle(twitterHandle);
 *         return character;
 *     }
 * }
 * </pre>
 * <p>
 * Schema generation of this would result in a stanza such as:
 *
 * <pre>
 * input CharacterInput {
 *    # Other fields ...
 *    addTwitterHandle: String
 * }
 * </pre>
 */
@java.lang.annotation.Target(ElementType.PARAMETER)
@Retention(RUNTIME)
@Documented
@Experimental("Marks a method parameter as the input object extended by an externally defined input field")
public @interface Target {
}
