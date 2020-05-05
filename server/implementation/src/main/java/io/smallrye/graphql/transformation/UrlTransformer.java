package io.smallrye.graphql.transformation;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlTransformer implements Transformer {
    @Override
    public Object in(final Object o) throws MalformedURLException {
        return new URL(o.toString());
    }

    @Override
    public Object out(final Object o) {
        return o.toString();
    }
}
