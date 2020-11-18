package io.smallrye.graphql.schema.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.AsyncApi;
import io.smallrye.graphql.schema.IndexCreator;
import io.smallrye.graphql.schema.model.Transformation;

public class FormatHelperTest {

    @Test
    public void testFormattedLocalDate() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("formattedLocalDate");
        Type type = nonNullString.returnType();

        Annotations annotations = Annotations.getAnnotationsForMethod(nonNullString);

        Optional<Transformation> format = FormatHelper.getFormat(type, annotations);

        Transformation transformInfo = format.get();
        assertEquals("yyyy-MM-dd", transformInfo.getFormat());
    }

    @Test
    public void testFormattedCompletionStage() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("formattedCompletionStage");
        Type type = nonNullString.returnType();

        Annotations annotations = Annotations.getAnnotationsForMethod(nonNullString);

        Optional<Transformation> format = FormatHelper.getFormat(type, annotations);

        Transformation transformInfo = format.get();
        assertEquals("yyyy-MM-dd", transformInfo.getFormat());
    }
}
