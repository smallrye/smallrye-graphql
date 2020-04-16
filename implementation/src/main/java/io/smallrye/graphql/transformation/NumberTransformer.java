package io.smallrye.graphql.transformation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Optional;

import org.jboss.logging.Logger;

/**
 * Help with number creation.
 * This is not pretty. But it works.
 * 
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class NumberTransformer {
    private static final Logger LOG = Logger.getLogger(NumberTransformer.class.getName());

    private final Optional<NumberFormat> numberFormat;

    /**
     * Get a NumberTransformer that will use the default values
     * 
     * @return instance of NumberTransformer
     */
    public static NumberTransformer transformer() {
        return new NumberTransformer(Optional.empty());
    }

    /**
     * Get a NumberTransformer
     * 
     * @param numberFormat the formatter to use
     * @return instance of NumberTransformer
     */
    public static NumberTransformer transformer(NumberFormat numberFormat) {
        return new NumberTransformer(Optional.of(numberFormat));
    }

    private NumberTransformer(Optional<NumberFormat> numberFormat) {
        this.numberFormat = numberFormat;
    }

    /**
     * Create Numbers from a String (using the default format)
     * 
     * @param input the number string
     * @param typeClassName the number type name
     * @return some number type object, maybe
     */
    public Optional<Object> stringToNumberType(String input, String typeClassName) {

        if (numberFormat.isPresent()) {
            try {
                input = numberFormat.get().parse(input).toString();
            } catch (ParseException ex) {
                LOG.warn("Could not parse [" + input.toString() + "]", ex);
            }
        }

        // Integer
        if (typeClassName.equals(int.class.getName())) {
            return Optional.of(Integer.parseInt(input));
        } else if (typeClassName.equals(Integer.class.getName())) {
            return Optional.of(Integer.valueOf(input));
        } else if (typeClassName.equals(short.class.getName())) {
            return Optional.of(Short.parseShort(input));
        } else if (typeClassName.equals(Short.class.getName())) {
            return Optional.of(Short.valueOf(input));
        } else if (typeClassName.equals(byte.class.getName())) {
            return Optional.of(Byte.parseByte(input));
        } else if (typeClassName.equals(Byte.class.getName())) {
            return Optional.of(Byte.valueOf(input));

            // Float
        } else if (typeClassName.equals(float.class.getName())) {
            return Optional.of(Float.parseFloat(input));
        } else if (typeClassName.equals(Float.class.getName())) {
            return Optional.of(Float.valueOf(input));
        } else if (typeClassName.equals(double.class.getName())) {
            return Optional.of(Double.parseDouble(input));
        } else if (typeClassName.equals(Double.class.getName())) {
            return Optional.of(Double.valueOf(input));

            // BigInteger
        } else if (typeClassName.equals(BigInteger.class.getName())) {
            return Optional.of(new BigInteger(input));
        } else if (typeClassName.equals(long.class.getName())) {
            return Optional.of(Long.parseLong(input));
        } else if (typeClassName.equals(Long.class.getName())) {
            return Optional.of(Long.valueOf(input));

            // BigDecimal
        } else if (typeClassName.equals(BigDecimal.class.getName())) {
            return Optional.of(new BigDecimal(input));

            // Not a number    
        } else {
            return Optional.empty();
        }
    }

}
