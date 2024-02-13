package io.smallrye.graphql.schema.link;

import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;

import org.eclipse.microprofile.graphql.Name;

import io.smallrye.graphql.api.Directive;

@Directive(on = { OBJECT })
@Name("custom")
public @interface CustomDirective {
}
