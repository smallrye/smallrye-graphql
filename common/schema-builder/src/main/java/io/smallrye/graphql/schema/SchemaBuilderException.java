package io.smallrye.graphql.schema;

/**
 * Thrown when we can't build a proper schema due to invalid
 * declarations
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaBuilderException extends RuntimeException {

    public SchemaBuilderException() {
    }

    public SchemaBuilderException(String string) {
        super(string);
    }

    public SchemaBuilderException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public SchemaBuilderException(Throwable thrwbl) {
        super(thrwbl);
    }

    public SchemaBuilderException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
