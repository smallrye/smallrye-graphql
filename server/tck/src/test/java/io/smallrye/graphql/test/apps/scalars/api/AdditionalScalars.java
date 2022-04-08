package io.smallrye.graphql.test.apps.scalars.api;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.bson.types.ObjectId;

public class AdditionalScalars {
    private URI uri;
    private URL url;
    private UUID uuid = UUID.fromString("037f4ba2-6d74-4686-a4ea-90cbd86007c3");
    private ObjectId objectId = new ObjectId("624fbb9213b47a7f7afe1f89");

    public AdditionalScalars() {
        try {
            uri = new URI("https://example.com");
            url = new URL("https://example.com");
        } catch (MalformedURLException | URISyntaxException e) {
            throw new AssertionError();//should never happen
        }
    }

    public URI getUri() {
        return uri;
    }

    public URL getUrl() {
        return url;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ObjectId getObjectId() {
        return objectId;
    }
}
