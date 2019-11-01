package io.smallrye.graphql.scalar;

import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

/**
 * Coercing that take Annotated values into account
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotatedCoercing implements Coercing {
    private final CustomScalar customScalar;
    private final Map<DotName, AnnotationInstance> annotations;

    public AnnotatedCoercing(CustomScalar customScalar, Map<DotName, AnnotationInstance> annotations) {
        this.customScalar = customScalar;
        this.annotations = annotations;
    }

    @Override
    public Object serialize(Object o) throws CoercingSerializeException {
        return customScalar.serialize(o, annotations);
    }

    @Override
    public Object parseValue(Object o) throws CoercingParseValueException {
        return customScalar.deserialize(o, annotations);
    }

    @Override
    public Object parseLiteral(Object o) throws CoercingParseLiteralException {
        if (o.getClass().getName().equals(StringValue.class.getName())) {
            StringValue stringValue = StringValue.class.cast(o);
            String value = stringValue.getValue();
            return parseLiteral(value);
        } // TODO: Other types ?
        return customScalar.deserialize(o, annotations);
    }

}
