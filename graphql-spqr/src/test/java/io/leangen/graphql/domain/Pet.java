package io.leangen.graphql.domain;

import org.eclipse.microprofile.graphql.Type;

import io.leangen.graphql.annotations.types.Interface;

@Type("Pet")
@Interface(implementationAutoDiscovery = true)
public interface Pet {
    String getSound();

    Human getOwner();
}
