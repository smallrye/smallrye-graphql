package io.smallrye.graphql.test;

import org.eclipse.microprofile.graphql.Interface;

@Interface
public interface UnionInterfaceOne extends UnionOfInterfaces {

    String getName();
}
