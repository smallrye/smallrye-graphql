package io.smallrye.graphql.execution.datafetcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CollectionHelperTest {

    private void test(Collection<?> c, Class<?> expected) {
        assertNotNull(c, "Return value is null");
        assertTrue(expected.isAssignableFrom(c.getClass()),
                "Unexpected type returned, expected subtype of " + expected + ", found: " + c.getClass());
        assertTrue(c.isEmpty(), "Unexpected non-empty collection returned: " + c);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Collection.class,

            Set.class,
            HashSet.class,
            LinkedHashSet.class,
            TreeSet.class,
            ConcurrentSkipListSet.class,
            CustomSet.class,
            NavigableSet.class,
            SortedSet.class,

            List.class,
            ArrayList.class,
            LinkedList.class,
            Stack.class,
            Vector.class,
            CopyOnWriteArrayList.class,
            CustomList.class,
            Queue.class,
            Deque.class,
    })

    public void newCollection(Class<? extends Collection<?>> clazz) {
        test(CollectionCreator.newCollection(clazz.getName(), 0), clazz);
    }

    static class CustomSet implements Set<Object> {

        public CustomSet() {
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
