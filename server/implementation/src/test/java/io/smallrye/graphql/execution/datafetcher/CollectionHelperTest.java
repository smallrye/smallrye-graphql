package io.smallrye.graphql.execution.datafetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import org.junit.jupiter.api.Test;

public class CollectionHelperTest {

    private void test(Collection<?> c, Class<?> expected) {
        assertNotNull(c, "Return value is null");
        assertEquals(expected, c.getClass(), "Unexpected type returned, expected " + expected + ", found: " + c.getClass());
        assertTrue(c.isEmpty(), "Unexpected non-empty collection returned: " + c);
    }

    @Test
    public void newCollection_Set() {
        test(CollectionCreator.newCollection(Set.class.getName()), HashSet.class);
    }

    @Test
    public void newCollection_HashSet() {
        test(CollectionCreator.newCollection(HashSet.class.getName()), HashSet.class);
    }

    @Test
    public void newCollection_LinkedHashSet() {
        test(CollectionCreator.newCollection(LinkedHashSet.class.getName()), LinkedHashSet.class);
    }

    @Test
    public void newCollection_TreeSet() {
        test(CollectionCreator.newCollection(TreeSet.class.getName()), TreeSet.class);
    }

    @Test
    public void newCollection_ConcurrentSkipListSet() {
        test(CollectionCreator.newCollection(ConcurrentSkipListSet.class.getName()), ConcurrentSkipListSet.class);
    }

    @Test
    public void newCollection_CustomSet() {
        test(CollectionCreator.newCollection(CustomSet.class.getName()), HashSet.class);
    }

    @Test
    public void newCollection_EmptySet() {
        test(CollectionCreator.newCollection(Collections.EMPTY_SET.getClass().getName()), HashSet.class);
    }

    @Test
    public void newCollection_EmptySetMethod() {
        test(CollectionCreator.newCollection(Collections.emptySet().getClass().getName()), HashSet.class);
    }

    @Test
    public void newCollection_CollectionsSingleton() {
        test(CollectionCreator.newCollection(Collections.singleton("foo").getClass().getName()), HashSet.class);
    }

    @Test
    public void newCollection_Collection() {
        test(CollectionCreator.newCollection(Collection.class.getName()), ArrayList.class);
    }

    @Test
    public void newCollection_List() {
        test(CollectionCreator.newCollection(List.class.getName()), ArrayList.class);
    }

    @Test
    public void newCollection_ArrayList() {
        test(CollectionCreator.newCollection(ArrayList.class.getName()), ArrayList.class);
    }

    @Test
    public void newCollection_LinkedList() {
        test(CollectionCreator.newCollection(LinkedList.class.getName()), LinkedList.class);
    }

    @Test
    public void newCollection_Stack() {
        test(CollectionCreator.newCollection(Stack.class.getName()), Stack.class);
    }

    @Test
    public void newCollection_Vector() {
        test(CollectionCreator.newCollection(Vector.class.getName()), Vector.class);
    }

    @Test
    public void newCollection_CopyOnWriteArrayList() {
        test(CollectionCreator.newCollection(CopyOnWriteArrayList.class.getName()), CopyOnWriteArrayList.class);
    }

    @Test
    public void newCollection_CustomList() {
        test(CollectionCreator.newCollection(CustomList.class.getName()), CustomList.class);
    }

    @Test
    public void newCollection_EmptyList() {
        test(CollectionCreator.newCollection(Collections.EMPTY_LIST.getClass().getName()), ArrayList.class);
    }

    @Test
    public void newCollection_EmptyListMethod() {
        test(CollectionCreator.newCollection(Collections.emptyList().getClass().getName()), ArrayList.class);
    }

    @Test
    public void newCollection_CollectionsSingletonList() {
        test(CollectionCreator.newCollection(Collections.singletonList("foo").getClass().getName()), ArrayList.class);
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
