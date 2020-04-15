package io.smallrye.graphql.execution.datafetcher.helper;

import org.eclipse.microprofile.graphql.GraphQLException;
import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.datafetcher.Transformer;
import io.smallrye.graphql.schema.model.Field;

/**
 * Help with the field response
 * 
 * Here we need to transform (if needed) the response field
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FieldHelper extends AbstractHelper {
    private static final Logger LOG = Logger.getLogger(FieldHelper.class.getName());

    private final Field field;

    /**
     * We need the modeled field to create the correct value
     * 
     * @param field the field
     * 
     */
    public FieldHelper(Field field) {
        this.field = field;
    }

    public Object transformResponse(Object argumentValue) throws GraphQLException {
        return super.recursiveTransform(argumentValue, field);
    }

    /**
     * By now this is a 'leaf' value, i.e not a collection of array, so we just transform if needed.
     * 
     * @param argumentValue the value to transform
     * @param field the field as created while scanning
     * @return transformed value
     */
    @Override
    Object singleTransform(Object argumentValue, Field field) {
        if (shouldTransform(field)) {
            Transformer transformer = Transformer.transformer(field);
            return argumentValue = transformer.formatOutput(argumentValue);
        } else {
            return argumentValue;
        }
    }

    @Override
    protected Object afterRecursiveTransform(Object fieldValue, Field field) throws GraphQLException {
        return fieldValue;
    }

}
