package io.smallrye.graphql.test.apps.exceptionlist;

public class MyUncheckedException extends RuntimeException {

    public MyUncheckedException(String errMsg) {
        super(errMsg);
    }
}
