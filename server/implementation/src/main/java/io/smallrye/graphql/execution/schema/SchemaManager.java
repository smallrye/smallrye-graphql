package io.smallrye.graphql.execution.schema;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import io.smallrye.graphql.schema.model.Schema;

/**
 * Make it easy to manage the schema
 * 
 * TODO: Change to use Scopes rather than map with classloader ?
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaManager {

    // If support for multiple deployments is enabled, schemas stored in this map are keyed by
    // each application's context class loader. If there is only a single app in this process,
    // the map will contain only one entry and it will be under a null key
    // One of the reasons is that Quarkus initializes this class at build time, so keying by
    // class loader is not possible, because class loader at runtime will be different.
    // allowMultipleDeployments MUST always be false in native mode.
    // FIXME: this approach could use some improvements, perhaps we could instead use an application-scoped bean
    // to separate things belonging to different apps?
    private static final Map<ClassLoader, Schema> schemas = Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile boolean allowMultipleDeployments = false;

    private SchemaManager() {
    }

    public static void setSchema(Schema schema, boolean allowMultipleDeployments) {
        if (allowMultipleDeployments) {
            SchemaManager.allowMultipleDeployments = true;
            schemas.put(Thread.currentThread().getContextClassLoader(), schema);
        } else {
            schemas.put(null, schema);
        }
    }

    public static Schema getSchema() {
        ClassLoader key = allowMultipleDeployments ? Thread.currentThread().getContextClassLoader() : null;
        return schemas.get(key);
    }
}
