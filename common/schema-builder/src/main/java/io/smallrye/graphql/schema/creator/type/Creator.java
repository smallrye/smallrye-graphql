package io.smallrye.graphql.schema.creator.type;

import org.jboss.jandex.ClassInfo;

/**
 * Something that can create object types on the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <T> the created type
 */
public interface Creator<T> {

    public T create(ClassInfo classInfo);
}
