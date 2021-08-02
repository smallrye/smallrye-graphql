package io.smallrye.graphql.test.apps.exceptionlist;

public class MyCheckedException extends Exception {

    public MyCheckedException(String errMsg) {
        super(errMsg);
    }
}
