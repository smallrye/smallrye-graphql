package io.smallrye.graphql.schema.directiveswithenumvalues;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.DirectiveLocation;

@Retention(RetentionPolicy.RUNTIME)
@Directive(on = DirectiveLocation.FIELD_DEFINITION)
public @interface MyEnumValueDirective {

    @NonNull
    MyEnum value();

}
