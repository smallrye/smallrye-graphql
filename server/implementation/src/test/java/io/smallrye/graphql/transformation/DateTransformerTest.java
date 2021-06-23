package io.smallrye.graphql.transformation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Transformation;
import io.smallrye.graphql.schema.model.Transformation.Type;

class DateTransformerTest {
    Field field = new Field("foo", "bar", "baz", null);

    @Nested
    class Default {
        @Test
        void shouldTransformLocalDate() {
            shouldTransform(LocalDate::parse, "2007-12-03");
        }

        @Test
        void shouldTransformLocalTime() {
            shouldTransform(LocalTime::parse, "10:15:31");
        }

        @Test
        void shouldTransformLocalDateTime() {
            shouldTransform(LocalDateTime::parse, "2007-12-03T10:15:31");
        }

        @Test
        void shouldTransformOffsetTime() {
            shouldTransform(OffsetTime::parse, "10:15:31+01:00");
        }

        @Test
        void shouldTransformOffsetDateTime() {
            shouldTransform(OffsetDateTime::parse, "2007-12-03T10:15:30+01:00");
        }

        @Test
        void shouldTransformZonedDateTime() {
            shouldTransform(ZonedDateTime::parse, "2007-12-03T10:15:31+01:00[Europe/Paris]");
        }

        @Test
        void shouldTransformInstant() {
            shouldTransform(Instant::parse, "2007-12-03T10:15:31.231Z");
        }

        private void shouldTransform(Function<String, ? extends Temporal> parse, String value) {
            Temporal expected = parse.apply(value);
            DateTransformer transformer = new DateTransformer(field, expected.getClass().getName());

            Temporal in = transformer.in(value);
            assertThat(in).isEqualTo(expected);

            String out = transformer.out(in);
            assertThat(out).isEqualTo(value);
        }
    }

    @Nested
    class WithDateFormat {
        @Test
        void shouldTransformLocalDateWithDateFormat() {
            shouldTransformWithDateFormat(LocalDate::parse, "03.12.2007", "dd.MM.yyyy");
        }

        @Test
        void shouldTransformLocalTimeWithDateFormat() {
            shouldTransformWithDateFormat(LocalTime::parse, "31*15*10", "ss*mm*HH");
        }

        @Test
        void shouldTransformLocalDateTimeWithDateFormat() {
            shouldTransformWithDateFormat(LocalDateTime::parse, "03.12.2007 31*15*10", "dd.MM.yyyy ss*mm*HH");
        }

        @Test
        void shouldTransformOffsetTimeWithDateFormat() {
            shouldTransformWithDateFormat(OffsetTime::parse, "31*15*10+0100", "ss*mm*HHZ");
        }

        @Test
        void shouldTransformOffsetDateTimeWithDateFormat() {
            shouldTransformWithDateFormat(OffsetDateTime::parse, "03.12.2007 31*15*10 +01", "dd.MM.yyyy ss*mm*HH x");
        }

        @Test
        void shouldTransformZonedDateTimeWithDateFormat() {
            shouldTransformWithDateFormat(ZonedDateTime::parse, "03.12.2007 31*15*10 +01[Europe/Paris]",
                    "dd.MM.yyyy ss*mm*HH X'['VV']'");
        }

        @Test
        void shouldTransformInstantWithDateFormat() {
            String text = "03.12.2007 231*31*15*10";
            String format = "dd.MM.yyyy SSS*ss*mm*HH";
            Instant expected = Instant.parse("2007-12-03T10:15:31.231Z");
            shouldTransformWithDateFormat(text, format, expected);
        }

        private void shouldTransformWithDateFormat(BiFunction<String, DateTimeFormatter, ? extends Temporal> parse,
                String value, String format) {
            shouldTransformWithDateFormat(value, format, parse.apply(value, DateTimeFormatter.ofPattern(format)));
        }

        private void shouldTransformWithDateFormat(String text, String format, Temporal expected) {
            field.setTransformation(new Transformation(Type.DATE, format, null, false));
            DateTransformer transformer = new DateTransformer(field, expected.getClass().getName());

            Temporal in = transformer.in(text);
            assertThat(in).isEqualTo(expected);

            String out = transformer.out(in);
            assertThat(out).isEqualTo(text);
        }
    }
}
