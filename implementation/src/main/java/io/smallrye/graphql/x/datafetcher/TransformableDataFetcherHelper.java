package io.smallrye.graphql.x.datafetcher;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.jboss.jandex.Type;

import io.smallrye.graphql.x.Annotations;
import io.smallrye.graphql.x.Classes;
import io.smallrye.graphql.x.schema.helper.CollectionHelper;
import io.smallrye.graphql.x.schema.helper.FormatHelper;

/**
 * Help with the transformation for DateFetchers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TransformableDataFetcherHelper {
    private final FormatHelper formatHelper = new FormatHelper();

    private DateTimeFormatter dateTimeFormatter = null;
    private NumberFormat numberFormat = null;

    private Type type;

    public TransformableDataFetcherHelper(Type type, Annotations annotations) {
        this.type = type;
        if (formatHelper.isDateLikeTypeOrCollectionThereOf(type)) {
            this.dateTimeFormatter = formatHelper.getDateFormat(type, annotations);
        }
        if (formatHelper.isNumberLikeTypeOrCollectionThereOf(type)) {
            this.numberFormat = formatHelper.getNumberFormat(annotations);
        }
    }

    public Object transform(Object o) {
        if (shouldTransform()) {
            return transformAny(o, type);
        }
        return o;
    }

    private Object transformAny(Object o, Type t) {
        Type.Kind kind = t.kind();
        if (kind.equals(Type.Kind.ARRAY)) {
            String arrayClass = t.name().toString();
            Type typeInArray = type.asArrayType().component();
            return transformArray(arrayClass, o, typeInArray);
        } else if (Classes.isOptional(t)) {
            return transformOptional(o);
        } else if (kind.equals(Type.Kind.PARAMETERIZED_TYPE)) {
            String collectionClass = t.name().toString();
            Type typeInCollection = t.asParameterizedType().arguments().get(0);
            return transformCollection(collectionClass, o, typeInCollection);
        } else {
            return transformObject(o);
        }
    }

    private Object transformCollection(String collectionClassName, Object o, Type typeInCollection) {
        Collection collection = Collection.class.cast(o);
        Collection transformedCollection = CollectionHelper.newCollection(collectionClassName);

        for (Object oo : collection) {
            transformedCollection.add(transformAny(oo, typeInCollection));
        }
        return transformedCollection;
    }

    private Object transformOptional(Object o) {
        Optional optional = Optional.class.cast(o);
        if (optional.isPresent()) {
            Object value = optional.get();
            return Collections.singletonList(transformObject(value));
        } else {
            return Collections.emptyList();
        }
    }

    private <T> Object transformArray(String arrayClassName, Object array, Type typeInArray) {
        Class arrayClass = Classes.loadClass(arrayClassName);
        List<Object> givenArray = Arrays.asList(array);
        List convertedList = new ArrayList();

        for (Object o : givenArray) {
            convertedList.add(transformAny(o, typeInArray));
        }

        return convertedList.toArray((T[]) Array.newInstance(arrayClass.getComponentType(), givenArray.size()));

    }

    private Object transformObject(Object o) {
        if (dateTimeFormatter != null) {
            return handleDateFormatting(o);
        } else if (numberFormat != null) {
            return handleNumberFormatting(o);
        } else {
            return o;
        }
    }

    private boolean shouldTransform() {
        return dateTimeFormatter != null || numberFormat != null;
    }

    private Object handleDateFormatting(Object o) {
        if (TemporalAccessor.class.isInstance(o)) {
            TemporalAccessor temporalAccessor = (TemporalAccessor) o;
            return dateTimeFormatter.format(temporalAccessor);
        } else if (java.sql.Date.class.isInstance(o)) {
            java.sql.Date date = ((java.sql.Date) o);
            TemporalAccessor temporalAccessor = date.toLocalDate();
            return dateTimeFormatter.format(temporalAccessor);
        } else if (java.sql.Timestamp.class.isInstance(o)) {
            java.sql.Timestamp date = ((java.sql.Timestamp) o);
            TemporalAccessor temporalAccessor = date.toLocalDateTime();
            return dateTimeFormatter.format(temporalAccessor);
        } else if (Date.class.isInstance(o)) {
            Date date = (Date) o;
            final TemporalAccessor temporalAccessor = toLocalDateTime(date);
            return handleDateFormatting(temporalAccessor);
        } else {
            return o;
        }
    }

    /*
     * @see io.smallrye.graphql.x.type.time.DateTimeScalar#toUtilDate(LocalDateTime)
     */
    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Object handleNumberFormatting(Object o) {
        if (Number.class.isInstance(o)) {
            Number number = (Number) o;
            return numberFormat.format(number);
        } else {
            return o;
        }
    }

}
