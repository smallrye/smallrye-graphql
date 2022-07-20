package io.smallrye.graphql.test;

import org.eclipse.microprofile.graphql.Interface;

@Interface
public interface UnionInterfaceTwo extends UnionOfInterfaces {

    String getColor();
}
