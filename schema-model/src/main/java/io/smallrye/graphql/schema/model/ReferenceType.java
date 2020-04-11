package io.smallrye.graphql.schema.model;

/**
 * Type of reference
 * 
 * Because we refer to types before they might exist, we need an indication of the type
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum ReferenceType {
    INPUT,
    TYPE,
    ENUM,
    INTERFACE,
    SCALAR
}