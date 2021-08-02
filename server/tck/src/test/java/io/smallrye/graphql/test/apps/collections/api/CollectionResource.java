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

        ss.add(new MySortableType(6, 333L));
        ss.add(new MySortableType(3, 333L));
        ss.add(new MySortableType(4, 999L));
        ss.add(new MySortableType(8, 666L));
        ss.add(new MySortableType(1, 999L));
        ss.add(new MySortableType(7, 999L));
        ss.add(new MySortableType(2, 666L));
        ss.add(new MySortableType(5, 666L));
        ss.add(new MySortableType(9, 333L));

        return ss;
    }

    public class MySortableType implements Comparable<MySortableType> {

        private Integer id;
        private Long anotherNumber;

        public MySortableType() {
        }

        public MySortableType(int id, Long anotherNumber) {
            this.id = id;
            this.anotherNumber = anotherNumber;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Long getAnotherNumber() {
            return anotherNumber;
        }

        public void setAnotherNumber(Long anotherNumber) {
            this.anotherNumber = anotherNumber;
        }

        @Override
        public int compareTo(MySortableType t) {
            return this.id.compareTo(t.id);
        }

    }

}
