package io.smallrye.graphql.schema.helper;

/**
 * Naming strategy for type
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum TypeAutoNameStrategy {
    Default, // Spec compliant
    MergeInnerClass, // Inner class prefix parent name
    Full // Use fully qualified name
}
