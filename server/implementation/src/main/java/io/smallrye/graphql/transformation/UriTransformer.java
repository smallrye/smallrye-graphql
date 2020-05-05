package io.smallrye.graphql.transformation;

import java.net.URI;
import java.net.URISyntaxException;

public class UriTransformer implements Transformer {
    @Override
    public Object in(final Object o) throws URISyntaxException {
        return new URI(o.toString());
    }

    @Override
    public Object out(final Object o) {
        return o.toString();
    }
}
