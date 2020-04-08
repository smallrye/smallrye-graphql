package io.smallrye.graphql.bootstrap.schema;

/**
 * When a method parameter is not found.
 */
public class MethodParameterNotFoundException extends RuntimeException {

    public MethodParameterNotFoundException() {
    }

    public MethodParameterNotFoundException(String string) {
        super(string);
    }

    public MethodParameterNotFoundException(String string, Throwable cause) {
        super(string, cause);
    }

    public MethodParameterNotFoundException(Throwable cause) {
        super(cause);
    }

    public MethodParameterNotFoundException(String string, Throwable cause, boolean bln, boolean bln1) {
        super(string, cause, bln, bln1);
    }

}
