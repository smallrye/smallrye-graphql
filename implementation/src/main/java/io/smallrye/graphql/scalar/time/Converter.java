package io.smallrye.graphql.scalar.time;

/**
 * Convert to the correct Type
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Converter {

    public Object fromString(String value);

}
