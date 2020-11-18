package io.smallrye.graphql.transformation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Transformation;

/**
 * Parses and formats numbers in the needed format.
 */
public class FormattedNumberTransformer implements Transformer<Number, String> {

    private final DecimalFormat numberFormat;

    private final NumberTransformer numberTransformer;

    protected FormattedNumberTransformer(Field field) {
        this.numberTransformer = new NumberTransformer(field);
        this.numberFormat = getNumberFormat(field.getTransformation());
        numberFormat.setParseBigDecimal(true);
    }

    private DecimalFormat getNumberFormat(Transformation formatter) {
        String format = formatter.getFormat();
        String locale = formatter.getLocale();

        if (format == null && locale == null) {
            return new DecimalFormat();
        } else if (format == null) {
            //Should work: https://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html
            return (DecimalFormat) NumberFormat.getInstance(Locale.forLanguageTag(locale));
        } else if (locale == null) {
            return new DecimalFormat(format);
        } else {
            return new DecimalFormat(format,
                    DecimalFormatSymbols.getInstance(Locale.forLanguageTag(locale)));
        }
    }

    @Override
    public Number in(final String o) throws ParseException {
        Number parsed = numberFormat.parse(o);
        return numberTransformer.in(parsed);
    }

    public String out(final Number object) {
        return this.numberFormat.format(object);
    }
}
