package io.smallrye.graphql.transformation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.TransformInfo;

/**
 * Parses and formats numbers in the needed format.
 */
public class FormattedNumberTransformer implements Transformer {

    private final NumberFormat numberFormat;

    private final NumberTransformer numberTransformer;

    protected FormattedNumberTransformer(Field field) {
        this.numberTransformer = new NumberTransformer(field);
        this.numberFormat = getNumberFormat(field.getTransformInfo().orElse(null));
    }

    private NumberFormat getNumberFormat(TransformInfo formatter) {
        String format = formatter.getFormat();
        String locale = formatter.getLocale();

        if (format == null && locale == null) {
            return NumberFormat.getInstance();
        } else if (format == null) {
            return NumberFormat.getInstance(Locale.forLanguageTag(locale));
        } else if (locale == null) {
            return new DecimalFormat(format);
        } else {
            return new DecimalFormat(format,
                    DecimalFormatSymbols.getInstance(Locale.forLanguageTag(locale)));
        }
    }

    @Override
    public Object in(final Object o) throws ParseException {
        return numberTransformer.in(numberFormat.parse(o.toString()).toString());
    }

    public Object out(final Object object) {
        if (object instanceof Number) {
            Number number = (Number) object;
            return this.numberFormat.format(number);
        }
        throw new RuntimeException(String.format("[%s] is no valid number-type", object.getClass()));

    }
}
