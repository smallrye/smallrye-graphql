package io.smallrye.graphql.schema.model;

/**
 * Represent an wrapper type in the Schema.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum WrapperType {
    OPTIONAL,
    COLLECTION,
    MAP,
    ARRAY,
    UNKNOWN // Could be a plugged in type, or normal generics
}