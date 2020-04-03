package io.smallrye.graphql.schema;

/**
 * Exception when we could not create a type (input or output) while building the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CreationException extends RuntimeException {

    public CreationException() {
    }

    public CreationException(String string) {
        super(string);
    }

    public CreationException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public CreationException(Throwable thrwbl) {
        super(thrwbl);
    }

    public CreationException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
