/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.graphql.bootstrap.schema.helper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionHelperTest {

    private void test(Collection<?> c, Class<?> expected) {
        assertThat(c).isNotNull();
        assertThat(c.getClass()).isEqualTo(expected);
        assertThat(c).isEmpty();
    }

    @Test void newCollection_Set() {
        test(CollectionHelper.newCollection(Set.class), HashSet.class);
    }

    @Test void newCollection_HashSet() {
        test(CollectionHelper.newCollection(HashSet.class), HashSet.class);
    }

    @Test void newCollection_LinkedHashSet() {
        test(CollectionHelper.newCollection(LinkedHashSet.class), LinkedHashSet.class);
    }

    @Test void newCollection_TreeSet() {
        test(CollectionHelper.newCollection(TreeSet.class), TreeSet.class);
    }

    @Test void newCollection_ConcurrentSkipListSet() {
        test(CollectionHelper.newCollection(ConcurrentSkipListSet.class), ConcurrentSkipListSet.class);
    }

    @Test void newCollection_CustomSet() {
        test(CollectionHelper.newCollection(CustomSet.class), HashSet.class);
    }

    @Test void newCollection_EmptySet() {
        test(CollectionHelper.newCollection(Collections.EMPTY_SET.getClass()), HashSet.class);
    }

    @Test void newCollection_EmptySetMethod() {
        test(CollectionHelper.newCollection(Collections.emptySet().getClass()), HashSet.class);
    }

    @Test void newCollection_CollectionsSingleton() {
        test(CollectionHelper.newCollection(Collections.singleton("foo").getClass()), HashSet.class);
    }

    @Test void newCollection_Collection() {
        test(CollectionHelper.newCollection(Collection.class), ArrayList.class);
    }

    @Test void newCollection_List() {
        test(CollectionHelper.newCollection(List.class), ArrayList.class);
    }

    @Test void newCollection_ArrayList() {
        test(CollectionHelper.newCollection(ArrayList.class), ArrayList.class);
    }

    @Test void newCollection_LinkedList() {
        test(CollectionHelper.newCollection(LinkedList.class), LinkedList.class);
    }

    @Test void newCollection_Stack() {
        test(CollectionHelper.newCollection(Stack.class), Stack.class);
    }

    @Test void newCollection_Vector() {
        test(CollectionHelper.newCollection(Vector.class), Vector.class);
    }

    @Test void newCollection_CopyOnWriteArrayList() {
        test(CollectionHelper.newCollection(CopyOnWriteArrayList.class), CopyOnWriteArrayList.class);
    }

    @Test void newCollection_CustomList() {
        test(CollectionHelper.newCollection(CustomList.class), CustomList.class);
    }

    @Test void newCollection_EmptyList() {
        test(CollectionHelper.newCollection(Collections.EMPTY_LIST.getClass()), ArrayList.class);
    }

    @Test void newCollection_EmptyListMethod() {
        test(CollectionHelper.newCollection(Collections.emptyList().getClass()), ArrayList.class);
    }

    @Test void newCollection_CollectionsSingletonList() {
        test(CollectionHelper.newCollection(Collections.singletonList("foo").getClass()), ArrayList.class);
    }

    static class CustomSet implements Set<Object> {

        private CustomSet(String someString) {
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<Object> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return null;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(Object e) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Object> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {
        }
    }

    static class CustomList implements List<Object> {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<Object> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return null;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(Object e) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Object> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Object> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {
        }

        @Override
        public Object get(int index) {
            return null;
        }

        @Override
        public Object set(int index, Object element) {
            return null;
        }

        @Override
        public void add(int index, Object element) {
        }

        @Override
        public Object remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @Override
        public ListIterator<Object> listIterator() {
            return null;
        }

        @Override
        public ListIterator<Object> listIterator(int index) {
            return null;
        }

        @Override
        public List<Object> subList(int fromIndex, int toIndex) {
            return null;
        }
    }
}
