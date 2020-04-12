package io.smallrye.graphql.bootstrap;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.x.*;
import io.smallrye.graphql.x.type.number.BigDecimalScalar;
import io.smallrye.graphql.x.type.number.BigIntegerScalar;
import io.smallrye.graphql.x.type.number.FloatScalar;
import io.smallrye.graphql.x.type.number.IntegerScalar;
import io.smallrye.graphql.x.type.time.DateScalar;
import io.smallrye.graphql.x.type.time.DateTimeScalar;
import io.smallrye.graphql.x.type.time.TimeScalar;

/**
 * Here we keep all the graphql-java scalars
 * mapped by classname
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLScalarTypes {

    private GraphQLScalarTypes() {
    }

    public static Map<String, GraphQLScalarType> getScalarMap() {
        return SCALAR_MAP;
    }

    // Scalar map we can just create now.
    private static final Map<String, GraphQLScalarType> SCALAR_MAP = new HashMap<>();
    private static final String ID = "ID";

    static {
        SCALAR_MAP.put(ID, Scalars.GraphQLID);
        SCALAR_MAP.put(char.class.getName(), Scalars.GraphQLString);
        SCALAR_MAP.put(Character.class.getName(), Scalars.GraphQLString);

        SCALAR_MAP.put(String.class.getName(), Scalars.GraphQLString);
        SCALAR_MAP.put(UUID.class.getName(), Scalars.GraphQLString);
        SCALAR_MAP.put(URL.class.getName(), Scalars.GraphQLString);
        SCALAR_MAP.put(URI.class.getName(), Scalars.GraphQLString);

        SCALAR_MAP.put(Boolean.class.getName(), Scalars.GraphQLBoolean);
        SCALAR_MAP.put(boolean.class.getName(), Scalars.GraphQLBoolean);

        mapType(new IntegerScalar());
        mapType(new FloatScalar());
        mapType(new BigIntegerScalar());
        mapType(new BigDecimalScalar());
        mapType(new DateScalar());
        mapType(new TimeScalar());
        mapType(new DateTimeScalar());
    }

    private static void mapType(Transformable transformable) {
        for (Class c : transformable.getSupportedClasses()) {
            SCALAR_MAP.put(c.getName(), (GraphQLScalarType) transformable);
        }
    }
}
