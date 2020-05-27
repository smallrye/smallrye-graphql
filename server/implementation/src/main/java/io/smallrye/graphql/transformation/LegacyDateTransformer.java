package io.smallrye.graphql.transformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.schema.model.Field;

/**
 * Handles legacy-date-formats (which aren't required by spec).
 */
public class LegacyDateTransformer implements Transformer {

    /**
     * Mappings between Legacy-Date-Type and java.time-Type.
     */
    private static final Map<String, String> CLASS_MAPPINGS = createClassMappings();

    public static final Set<String> SUPPORTED_TYPES = Collections.unmodifiableSet(CLASS_MAPPINGS.keySet());

    private final DateTransformer dateTransformer;

    private final String targetClassName;

    public LegacyDateTransformer(final Field field) {
        this.targetClassName = field.getReference().getClassName();
        this.dateTransformer = new DateTransformer(field, CLASS_MAPPINGS.get(targetClassName));
    }

    @Override
    public Object in(final Object o) throws Exception {
        if (targetClassName.equals(java.sql.Date.class.getName())) {
            LocalDate localdate = (LocalDate) dateTransformer.in(o);
            return java.sql.Date.valueOf(localdate);
        } else if (targetClassName.equals(java.sql.Time.class.getName())) {
            LocalTime localtime = (LocalTime) dateTransformer.in(o);
            return java.sql.Time.valueOf(localtime);
        } else if (targetClassName.equals(java.sql.Timestamp.class.getName())) {
            LocalDateTime localdatetime = (LocalDateTime) dateTransformer.in(o);
            return java.sql.Timestamp.valueOf(localdatetime);
        } else if (targetClassName.equals(Date.class.getName())) {
            LocalDateTime localdatetime = (LocalDateTime) dateTransformer.in(o);
            return Date.from(localdatetime.atZone(ZoneId.systemDefault()).toInstant());
        }
        throw SmallRyeGraphQLServerMessages.msg.cantParseDate(o.getClass().getName(), targetClassName);
    }

    @Override
    public Object out(final Object dateType) {
        if (dateType instanceof java.sql.Date) {
            java.sql.Date casted = (java.sql.Date) dateType;
            return dateTransformer.out(casted.toLocalDate());
        } else if (dateType instanceof java.sql.Time) {
            java.sql.Time casted = (java.sql.Time) dateType;
            return dateTransformer.out(casted.toLocalTime());
        } else if (dateType instanceof java.sql.Timestamp) {
            java.sql.Timestamp casted = (java.sql.Timestamp) dateType;
            return dateTransformer.out(casted.toLocalDateTime());
        } else if (dateType instanceof Date) {
            Date casted = (Date) dateType;
            return dateTransformer.out(casted.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        throw SmallRyeGraphQLServerMessages.msg.cantParseDate(dateType.getClass().getName(), targetClassName);
    }

    private static Map<String, String> createClassMappings() {
        final HashMap<String, String> stringStringHashMap = new HashMap<>();

        stringStringHashMap.put(java.sql.Date.class.getName(), LocalDate.class.getName());
        stringStringHashMap.put(java.sql.Time.class.getName(), LocalTime.class.getName());
        stringStringHashMap.put(java.sql.Timestamp.class.getName(), LocalDateTime.class.getName());
        stringStringHashMap.put(Date.class.getName(), LocalDateTime.class.getName());

        return stringStringHashMap;
    }

}
