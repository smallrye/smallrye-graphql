# Customizing JSON-B serializers/deserializers

If your application needs finer-grained control over JSON serialization/deserialization than you can achieve via
formatting annotations like `@JsonbDateFormat`, you may plug in your own custom instances of the `Jsonb` class
for each input type that your GraphQL application exposes.

To do this, you'll need to create an implementation of `io.smallrye.graphql.spi.EventingService`
and implement its `overrideJsonbConfig` method. 
An example that plugs in a custom date format for a particular class that is used as input:

```
package org.acme.custom.json.config;

public class CustomJsonbService implements EventingService {

    @Override
    public String getConfigKey() {
        return null; // activate this service always regardless of the configuration 
    }

    @Override
    public Map<String, Jsonb> overrideJsonbConfig() {
        JsonbConfig config = new JsonbConfig().withDateFormat("MM dd yyyy HH:mm Z", null).withDeserializers(OBJECTID_DESERIALIZER).withSerializers(
            OBJECTID_SERIALIZER);
        return Collections.singletonMap("MyModelClass.class.getName()", JsonbBuilder.create(config));
    }
}
```

We can customize the JsonbConfig as per our need in above example we have registered additional serializer and date formatting for the class MyModelClass.

As the discovery of eventing services uses the ServiceLoader mechanism, don't forget to add a 
`META-INF/services/io.smallrye.graphql.spi.EventingService` file that contains the fully qualified 
name of your implementation.
Example `org.acme.custom.json.config.CustomJsonbService`