package io.smallrye.graphql.schema;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

public class IndexCreator {
    public static Index index(Class<?>... clazzes) throws IOException {
        final Indexer indexer = new Indexer();
        for (Class<?> clazz : clazzes) {
            InputStream stream = IndexCreator.class.getClassLoader()
                    .getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
            indexer.index(stream);
        }
        return indexer.complete();
    }
}
