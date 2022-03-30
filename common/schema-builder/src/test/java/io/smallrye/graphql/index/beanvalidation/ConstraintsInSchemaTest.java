package io.smallrye.graphql.index.beanvalidation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.IndexCreator;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.schema.model.Schema;

public class ConstraintsInSchemaTest {

    @GraphQLApi
    public static class ConstraintsApi {

        @Query
        public String query(FieldConstraints constraints) {
            return null;
        }

        @Query
        public String queryWithConstrainedArgument(@Size(max = 123) String constrainedArgument) {
            return null;
        }
    }

    public static class FieldConstraints {

        @Size(min = 5, max = 10)
        public String stringWithMinMax;

        @Size(min = 7)
        public String stringWithMinOnly;

        @Email
        public String email;

        @Max(3500)
        public Long numberWithMax;

        @Min(2000)
        public Long numberWithMin;

        @Pattern(regexp = ".*")
        public String stringWithPattern;

    }

    @Test
    public void testFieldConstraints() {
        Index index = IndexCreator.index(ConstraintsApi.class, FieldConstraints.class);
        Schema schema = SchemaBuilder.build(index);
        InputType inputType = schema.getInputs().get("FieldConstraintsInput");

        Field stringWithMinMax = inputType.getFields().get("stringWithMinMax");
        assertHasValue(stringWithMinMax, "minLength", 5);
        assertHasValue(stringWithMinMax, "maxLength", 10);

        Field stringWithMinOnly = inputType.getFields().get("stringWithMinOnly");
        assertHasValue(stringWithMinOnly, "minLength", 7);
        assertHasValue(stringWithMinOnly, "maxLength", null);

        Field email = inputType.getFields().get("email");
        assertHasValue(email, "format", "email");

        Field numberWithMinMax = inputType.getFields().get("numberWithMax");
        assertHasValue(numberWithMinMax, "max", 3500L);

        Field numberWithMin = inputType.getFields().get("numberWithMin");
        assertHasValue(numberWithMin, "min", 2000L);

        Field stringWithPattern = inputType.getFields().get("stringWithPattern");
        assertHasValue(stringWithPattern, "pattern", ".*");
    }

    @Test
    public void testArgumentConstraints() {
        Index index = IndexCreator.index(ConstraintsApi.class, FieldConstraints.class);
        Schema schema = SchemaBuilder.build(index);
        Argument argument = schema.getQueries().stream()
                .filter(q -> q.getName().equals("queryWithConstrainedArgument"))
                .findFirst().get()
                .getArguments().get(0);
        Assertions.assertEquals(123, argument.getDirectiveInstances().get(0).getValue("maxLength"));
    }

    private void assertHasValue(Field field, String constraintName, Object value) {
        List<DirectiveInstance> directiveInstances = field.getDirectiveInstances();
        assertEquals(1, directiveInstances.size());
        DirectiveInstance constraintDirective = directiveInstances.get(0);
        assertEquals("constraint", constraintDirective.getType().getName());
        assertEquals(value, constraintDirective.getValue(constraintName),
                "Directive values: " + constraintDirective.getValues());
    }
}
