package io.smallrye.graphql.test.apps.exceptionlist;

public class MyUncheckedException extends Exception {

    public MyUncheckedException(String errMsg) {
        super(errMsg);
    }
}
