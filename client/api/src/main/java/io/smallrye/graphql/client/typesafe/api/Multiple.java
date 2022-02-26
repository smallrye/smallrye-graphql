package io.smallrye.graphql.client.typesafe.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 'Hide' or 'unwrap' the return type of the annotated method from GraphQL,
 * i.e. request the fields contained within the class directly.
 * The nested fields can optionally be parameterized with {@link NestedParameter} annotations.
 * <p>
 * E.g.
 *
 * <pre>
 * &#64;GraphQlClientApi
 * interface FooAndBarApi {
 *     FooAndBar fooAndBar(&#64;NestedParameter("bar") String id);
 * }
 *
 * &#64;Multiple
 * static class FooAndBar {
 *     Foo foo;
 *     Bar bar;
 * }
 * </pre>
 * <p>
 * Creates a query for the two fields <code>foo</code> and <code>bar</code>:
 *
 * <pre>
 * query fooAndBar($id: String!) {
 *   foo {
 *     name
 *   }
 *   bar(id: $id) {
 *     name
 *   }
 * }
 * </pre>
 * <p>
 * In this way, you can also issue multiple mutations with a single request.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Multiple {
}
