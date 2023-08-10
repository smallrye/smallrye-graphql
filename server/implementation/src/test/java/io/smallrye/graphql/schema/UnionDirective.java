package io.smallrye.graphql.schema;

import static io.smallrye.graphql.api.DirectiveLocation.UNION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Retention(RUNTIME)
@Directive(on = { UNION })
@Repeatable(UnionDirective.UnionDirectives.class)
public @interface UnionDirective {
    String value();

    @Retention(RUNTIME)
    @interface UnionDirectives {
        UnionDirective[] value();
    }

}
