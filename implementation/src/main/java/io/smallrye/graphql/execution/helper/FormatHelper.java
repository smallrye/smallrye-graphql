package io.smallrye.graphql.execution.helper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import io.smallrye.graphql.schema.model.Formatter;

/**
 * Helping with formats of dates and Numbers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FormatHelper {

    private FormatHelper() {
    }

    public static NumberFormat getNumberFormat(Formatter formatter) {
        if (formatter != null) {
            String format = formatter.getFormat();
            String locale = formatter.getLocale();

            if (format == null && locale == null) {
                return null;
            } else if (format == null) {
                return NumberFormat.getInstance(Locale.forLanguageTag(locale));
            } else if (locale == null) {
                return new DecimalFormat(format);
            } else {
                return new DecimalFormat(format,
                        DecimalFormatSymbols.getInstance(Locale.forLanguageTag(locale)));
            }
        }
        return null;
    }

    public static DateTimeFormatter getDateFormat(Formatter formatter) {
        if (formatter != null) {
            String format = formatter.getFormat();
            String locale = formatter.getLocale();
            if (format == null && locale == null) {
                return null;//getDefaultDateTimeFormatter(type);
            } else if (format == null) {
                return null;//getDefaultDateTimeFormatter(type).withLocale(Locale.forLanguageTag(locale.asString()));
            } else if (locale == null) {
                return DateTimeFormatter.ofPattern(format);
            } else {
                return DateTimeFormatter.ofPattern(format).withLocale(Locale.forLanguageTag(locale));
            }
        }
        return null;
    }
}
