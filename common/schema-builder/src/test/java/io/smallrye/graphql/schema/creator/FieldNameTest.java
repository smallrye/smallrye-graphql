package io.smallrye.graphql.schema.creator;

import static io.smallrye.graphql.index.SchemaBuilderTest.indexDirectory;
import static org.junit.Assert.assertEquals;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.junit.Test;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.creator.fieldnameapp.SomeObjectAnnotatedGetters;
import io.smallrye.graphql.schema.helper.Direction;

public class FieldNameTest {
    @Test
    public void testFieldNamePriority() throws Exception {
        Indexer indexer = new Indexer();
        indexDirectory(indexer, "io/smallrye/graphql/schema/creator/fieldnameapp");
        IndexView index = indexer.complete();

        ClassInfo classInfo = index.getClassByName(DotName.createSimple(SomeObjectAnnotatedGetters.class.getName()));
        // @Name
        MethodInfo methodInfo = classInfo.method("getName");
        Annotations annotations = Annotations.getAnnotationsForMethod(methodInfo);
        assertEquals("a", FieldCreator.getFieldName(Direction.OUT, annotations, "name"));

        // @Query
        methodInfo = classInfo.method("getQuery");
        annotations = Annotations.getAnnotationsForMethod(methodInfo);
        assertEquals("d", FieldCreator.getFieldName(Direction.OUT, annotations, "query"));

        // @JsonbProperty
        methodInfo = classInfo.method("getJsonbProperty");
        annotations = Annotations.getAnnotationsForMethod(methodInfo);
        assertEquals("f", FieldCreator.getFieldName(Direction.OUT, annotations, "jsonbProperty"));

        // no annotation
        methodInfo = classInfo.method("getFieldName");
        annotations = Annotations.getAnnotationsForMethod(methodInfo);
        assertEquals("fieldName", FieldCreator.getFieldName(Direction.OUT, annotations, "fieldName"));
    }
}