package io.smallrye.graphql.transformation;

/**
 * Just lets values pass through, used as default-transformer.
 */
public class PassThroughTransformer implements Transformer {
    @Override
    public Object in(final Object o) {
        return o;
    }

    @Override
    public Object out(final Object o) {
        return o;
    }
}
