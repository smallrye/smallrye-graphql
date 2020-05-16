package io.smallrye.graphql.transformation;

/**
 * Transforms between char and String.
 */
public class CharTransformer implements Transformer<Character, String> {

    @Override
    public Character in(final String o) {
        return o.charAt(0);
    }

    @Override
    public String out(final Character o) {
        if (o == null) {
            return null;
        }
        return String.valueOf(o);
    }
}
