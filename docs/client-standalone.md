# Bootstraping GraphQL clients

In general, it is recommended to use SmallRye GraphQL client through the
[Quarkus extension](https://quarkus.io/guides/smallrye-graphql-client),
or the [WildFly feature pack](https://github.com/wildfly-extras/wildfly-graphql-feature-pack/wiki/GraphQL-client-guide),
each of them having their own documentation about how to set up dependencies and bootstrap a client.
It is however also possible to use SmallRye GraphQL Client in a standalone application outside any container.
The single dependency that you need is `io.smallrye:smallrye-graphql-client-implementation-vertx`, as the
Vert.x based implementation is currently the only supported one.

If you're using the client as a script and notice that the JVM doesn't exit even after you close the client instance,
it's probably because the client is maintaining a `Vertx` instance that it created automatically. To solve this
issue, you might need to create a `Vertx` instance on your own, pass it to the client builder, and then close it after
closing the client. See the following JBang snippet for an example.

## Logging with a standalone (Java SE) application using the SmallRye GraphQL Client

If you want to properly configure logging in a standalone application, you
will also need to add a logging implementation to the classpath. The client
code uses the JBoss Logging facade, so you can, for example, use the JBoss
Logmanager. Add the `org.jboss.logmanager:jboss-logmanager` dependency to
your project(it needs to be on the runtime classpath). You will also need to
run the application with this system property declared:
`-Djava.util.logging.manager=org.jboss.logmanager.LogManager`.

To customize logging levels etc, it picks up the `logging.properties` file
from the root of the runtime classpath, so, for example if using Maven and
the exec plugin to start the application, you would place the file into
`src/main/resources/logging.properties`. For an example of the contents of
this file, see
https://github.com/jamezp/jboss-logging-example/blob/master/src/main/resources/logging.properties.


## Using with JBang

This is a full script runnable directly with [JBang](https://www.jbang.dev/) that
uses a dynamic client for connecting to [countries.trevorblades.com](https://countries.trevorblades.com)
to obtain a list of countries from its database.

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.smallrye:smallrye-graphql-client-implementation-vertx:RELEASE

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;

import io.vertx.core.Vertx;

// Has a multiline string literal, requires Java 15+!
class Client {
    public static void main(String... args) throws Exception {
        Vertx vertx = Vertx.vertx();
        DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
            .url("https://countries.trevorblades.com")
            .vertx(vertx)
            .build();
        try {
            Response response = client.executeSync("""
                query {
                  countries {
                    name
                  }
                }
                """);
            System.out.println(response);
        } finally {
            client.close();
            vertx.close();
        }
    }
}
```

Save this file as `Client.java` and execute with `jbang Client.java`.


