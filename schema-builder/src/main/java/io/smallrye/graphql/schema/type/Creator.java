package io.smallrye.graphql.schema.type;

import org.jboss.jandex.ClassInfo;

/**
 * Create elements on the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <T> the created type
 */
public interface Creator<T> {

    public T create(ClassInfo classInfo);
}
