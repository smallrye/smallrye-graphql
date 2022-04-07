# Using SmallRye Stork

SmallRye GraphQL client (both dynamic and typesafe) supports SmallRye Stork for 
service discovery and load balancing. If your client is configured to connect to a URI
with the `stork://` or `storks://` scheme, then Stork will be used to determine the
actual endpoints to connect to. Load balancing, if configured, will be applied in a way that
each HTTP request can be routed to a different service instance. For WebSocket traffic,
once a WebSocket connection is established, the client will keep using that connection
for as long as possible. If that connection is lost for any reason and needs to be
reestablished, then the load balancer can route it to a different instance.
                                            
## Using with Quarkus

The dependency on Stork is declared as optional, so if you want to use it, you might need 
to add it explicitly. If you're using Quarkus, use the Stork extension along with 
the desired service discovery implementation, for example:

```
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-stork</artifactId>
</dependency>
<dependency>
    <groupId>io.smallrye.stork</groupId>
    <artifactId>smallrye-stork-service-discovery-static-list</artifactId>
</dependency>
```

Example configuration in Quarkus `application.properties`:
```
quarkus.smallrye-graphql-client.myclient.url=stork://foo-service/graphql
quarkus.stork.foo-service.service-discovery.type=static
quarkus.stork.foo-service.service-discovery.address-list=server1.com:8080,server2.com:8080
```

## When running as a standalone application

If you're using the client in a standalone mode outside any container that handles
Stork initialization for you, you might need two additional steps to get it working:

Add a configuration provider for Stork, such as
```
<dependency>
    <groupId>io.smallrye.stork</groupId>
    <artifactId>stork-microprofile-config</artifactId>
</dependency>
```

And before initializing any GraphQL client instances, make sure that Stork is initialized 
by calling `io.smallrye.stork.Stork.initialize()` once.
