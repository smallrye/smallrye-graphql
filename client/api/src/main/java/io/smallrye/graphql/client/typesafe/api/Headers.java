package io.smallrye.graphql.client.typesafe.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Headers {
    Header[] value();
}
