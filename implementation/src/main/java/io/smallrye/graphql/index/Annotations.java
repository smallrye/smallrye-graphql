package io.smallrye.graphql.index;

import javax.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.InputType;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Type;
import org.jboss.jandex.DotName;

/**
 * All the annotations we care about
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Annotations {
    public static final DotName QUERY = DotName.createSimple(Query.class.getName());
    public static final DotName MUTATION = DotName.createSimple(Mutation.class.getName());
    public static final DotName INPUTTYPE = DotName.createSimple(InputType.class.getName());
    public static final DotName TYPE = DotName.createSimple(Type.class.getName());

    public static final DotName ID = DotName.createSimple(Id.class.getName());
    public static final DotName DESCRIPTION = DotName.createSimple(Description.class.getName());

    public static final DotName JSONB_PROPERTY = DotName.createSimple(JsonbProperty.class.getName());
}
