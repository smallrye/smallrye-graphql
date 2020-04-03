package io.smallrye.graphql.schema.helper;

import org.jboss.jandex.Type;

/**
 * When the type used is not supported
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InvalidTypeException extends RuntimeException {

    public InvalidTypeException(Type type) {
        this("Not a valid Type [" + type.name().toString() + "]");
    }

    public InvalidTypeException(String string) {
        super(string);
    }

    public InvalidTypeException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public InvalidTypeException(Throwable thrwbl) {
        super(thrwbl);
    }

    public InvalidTypeException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
