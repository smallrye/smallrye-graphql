package io.smallrye.graphql.execution.resolver;

/**
 * There is an interface with no concrete implementation
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ConcreteImplementationNotFoundException extends RuntimeException {

    public ConcreteImplementationNotFoundException() {
    }

    public ConcreteImplementationNotFoundException(String string) {
        super(string);
    }

    public ConcreteImplementationNotFoundException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public ConcreteImplementationNotFoundException(Throwable thrwbl) {
        super(thrwbl);
    }

    public ConcreteImplementationNotFoundException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
