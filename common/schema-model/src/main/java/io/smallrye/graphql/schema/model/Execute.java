package io.smallrye.graphql.schema.model;

/**
 * Execution type
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum Execute {
    BLOCKING,
    NON_BLOCKING,
    DEFAULT,
    RUN_ON_VIRTUAL_THREAD
}
