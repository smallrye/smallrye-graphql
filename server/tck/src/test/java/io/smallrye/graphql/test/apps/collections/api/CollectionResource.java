package io.smallrye.graphql.test.apps.collections.api;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class CollectionResource {

    @Query
    public SortedSet<@NonNull MySortableType> sortedValues() {

        SortedSet<MySortableType> ss = new TreeSet<>();

        ss.add(new MySortableType(6, 333));
        ss.add(new MySortableType(3, 333));
        ss.add(new MySortableType(4, 999));
        ss.add(new MySortableType(8, 666));
        ss.add(new MySortableType(1, 999));
        ss.add(new MySortableType(7, 999));
        ss.add(new MySortableType(2, 666));
        ss.add(new MySortableType(5, 666));
        ss.add(new MySortableType(9, 333));

        return ss;
    }

    public static class MySortableType implements Comparable<MySortableType> {
        private final int value;
        private final int hashCode;

        public MySortableType(final int value, final int hashCode) {
            this.value = value;
            this.hashCode = hashCode;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            MySortableType that = (MySortableType) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public int compareTo(final MySortableType other) {
            return Integer.compare(value, other.value);
        }
    }

}
