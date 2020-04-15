package io.smallrye.graphql.scalar.number;

import io.smallrye.graphql.scalar.AbstractScalar;

/**
 * Base Scalar for Numbers.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractNumberScalar extends AbstractScalar {

    public <T> AbstractNumberScalar(String name,
            Converter converter,
            Class... supportedTypes) {

        super(name, new NumberCoercing(name, converter, supportedTypes), supportedTypes);

    }

    //    @Override
    //    public Object transform(Object input, Argument argument) {
    //        NumberFormat numberFormat = formatHelper.getNumberFormat(argument.getAnnotations());
    //        if (numberFormat != null) {
    //            try {
    //                Number number = numberFormat.parse(input.toString());
    //                return converter.fromNumber(number, argument);
    //            } catch (ParseException ex) {
    //                throw new TransformException(ex, this, argument.getName(), input.toString());
    //            }
    //        }
    //        return input;
    //
    //    }
}
