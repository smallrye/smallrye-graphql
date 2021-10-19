package io.smallrye.graphql.test.apps.enumlist.api;

import java.util.EnumSet;
import java.util.Set;

public class ObjectWithEnumList {
    public Set<Pet> nopets = EnumSet.noneOf(Pet.class);
    public Set<Pet> allpets = EnumSet.allOf(Pet.class);
}
