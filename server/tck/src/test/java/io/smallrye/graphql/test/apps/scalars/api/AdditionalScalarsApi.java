package io.smallrye.graphql.test.apps.scalars.api;

import java.net.URI;
import java.net.URL;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class AdditionalScalarsApi {

    @Query
    public AdditionalScalars additionalScalars() {
        return new AdditionalScalars();
    }

    public URL urlInput(@Source AdditionalScalars additionalScalars, URL url) {
        return url;
    }

    public URI uriInput(@Source AdditionalScalars additionalScalars, URI uri) {
        return uri;
    }

    public UUID uuidInput(@Source AdditionalScalars additionalScalars, UUID uuid) {
        return uuid;
    }

    public ObjectId objectIdInput(@Source AdditionalScalars additionalScalars, ObjectId objectId) {
        return objectId;
    }

    public URL urlDefault(@Source AdditionalScalars additionalScalars, @DefaultValue("https://example.com") URL url) {
        return url;
    }

    public URI uriDefault(@Source AdditionalScalars additionalScalars, @DefaultValue("https://example.com") URI uri) {
        return uri;
    }

    public UUID uuidDefault(@Source AdditionalScalars additionalScalars,
            @DefaultValue("037f4ba2-6d74-4686-a4ea-90cbd86007c3") UUID uuid) {
        return uuid;
    }
}
