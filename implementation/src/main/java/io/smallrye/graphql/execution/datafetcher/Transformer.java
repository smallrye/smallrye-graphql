package io.smallrye.graphql.execution.datafetcher;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.helper.FormatHelper;
import io.smallrye.graphql.schema.model.Format;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Transform data (for now on the way out)
 * 
 * TODO: Consider some sort of decorator pattern ? Or java.lang.reflect.Proxy ?
 * TODO: Add Metrics back
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Transformer {
    private static final Logger LOG = Logger.getLogger(Transformer.class.getName());

    private DateTimeFormatter dateTimeFormatter = null;
    private NumberFormat numberFormat = null;

    private final Operation operation;

    private boolean shouldTransformResponse = false;

    public Transformer(Operation operation) {
        this.operation = operation;

        if (operation.getFormat().isPresent()) {
            Format format = operation.getFormat().get();

            if (format.getType().equals(Format.Type.NUMBER)) {
                this.numberFormat = FormatHelper.getNumberFormat(format);
                this.shouldTransformResponse = true;
            } else if (format.getType().equals(Format.Type.DATE)) {
                this.dateTimeFormatter = FormatHelper.getDateFormat(format);
                this.shouldTransformResponse = true;
            }
        }
    }

    public Object transform(Object input) {
        if (shouldTransformResponse) {
            return transform(input, operation);
        } else {
            return input;
        }
    }

    private Object transform(Object input, Operation operation) {

        if (operation.getArray().isPresent()) {
            LOG.warn("TODO: Transform [" + input.toString() + "] that is a [" + operation.getClassName() + "]");
            // TODO: Call this until o.getClass equals field type
        }

        if (input.getClass().getName().equals(operation.getReference().getClassName())) {
            return transformObject(input);
        } else {
            // Throw an exception ?
            LOG.warn("Can not transform type [" + input.getClass().getName() + "] - expecting ["
                    + operation.getReference().getClassName() + "]");
            return input;
        }
    }

    private Object transformObject(Object input) {
        if (dateTimeFormatter != null) {
            return handleDateFormatting(input);
        } else if (numberFormat != null) {
            return handleNumberFormatting(input);
        } else {
            return input;
        }
    }

    private Object handleDateFormatting(Object input) {
        if (TemporalAccessor.class.isInstance(input)) {
            TemporalAccessor temporalAccessor = (TemporalAccessor) input;
            return dateTimeFormatter.format(temporalAccessor);
        } else {
            return input;
        }
    }

    private Object handleNumberFormatting(Object input) {
        if (Number.class.isInstance(input)) {
            Number number = (Number) input;
            return numberFormat.format(number);
        } else {
            return input;
        }
    }

}
