package address.util.collections;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.text.Collator;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Unmodifiable view of an observable list
 */
public class UnmodifiableObservableList<E> implements ObservableList<E> {

    public static final String MUTATION_OP_EXCEPTION_MESSAGE = "Attempted to modify an unmodifiable view";

    private final ObservableList<? extends E> backingList;


    public UnmodifiableObservableList(ObservableList<? extends E> backingList) {
        if (backingList == null) {
            throw new NullPointerException();
        }
        this.backingList = backingList;
    }

    
    @Override
    public void addListener(ListChangeListener<? super E> listener) {
        backingList.addListener(listener);
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listener) {
        backingList.removeListener(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        backingList.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        backingList.removeListener(listener);
    }

    
    @Override
    public boolean addAll(Object... elements) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean setAll(Object... elements) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean setAll(Collection<? extends E> col) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }
    
    @Override
    public boolean removeAll(Object... elements) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }
    
    @Override
    public boolean retainAll(Object... elements) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public void remove(int from, int to) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    
    @Override
    public FilteredList<E> filtered(Predicate<E> predicate) {
        return new FilteredList<>(this, predicate);
    }

    @Override
    public SortedList<E> sorted(Comparator<E> comparator) {
        return new SortedList<>(this, comparator);
    }
    
    @Override
    public SortedList<E> sorted() {
        return sorted(Comparator.nullsFirst((o1, o2) -> {
            if (o1 instanceof Comparable) {
                return ((Comparable) o1).compareTo(o2);
            }
            return Collator.getInstance().compare(o1.toString(), o2.toString());
        }));
    }

    
    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean isEmpty() {
        return backingList.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return backingList.contains(o);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<? extends E> i = backingList.iterator();

            public boolean hasNext() {return i.hasNext();}
            public E next()          {return i.next();}
            public void remove() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                // Use backing collection version
                i.forEachRemaining(action);
            }
        };
    }
    
    @Override
    public Object[] toArray() {
        return backingList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return backingList.toArray(a);
    }

    
    @Override
    public boolean add(E o) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return backingList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    
    @Override
    public boolean equals(Object o) {
        return o == this || backingList.equals(o);
    }

    @Override
    public int hashCode() {
        return backingList.hashCode();
    }

    
    @Override
    public E get(int index) {
        return backingList.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }
    
    @Override
    public int indexOf(Object o) {
        return backingList.indexOf(o);
    }
    
    @Override
    public int lastIndexOf(Object o) {
        return backingList.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }
    
    @Override
    public ListIterator<E> listIterator(int index) {
        return new ListIterator<E>() {
            private final ListIterator<? extends E> i = backingList.listIterator(index);

            public boolean hasNext()     {return i.hasNext();}
            public E next()              {return i.next();}
            public boolean hasPrevious() {return i.hasPrevious();}
            public E previous()          {return i.previous();}
            public int nextIndex()       {return i.nextIndex();}
            public int previousIndex()   {return i.previousIndex();}

            public void remove() {
                throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
            }
            public void set(E e) {
                throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
            }
            public void add(E e) {
                throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
            }

            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                i.forEachRemaining(action);
            }
        };
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return Collections.unmodifiableList(backingList.subList(fromIndex, toIndex));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<E> spliterator() {
        return (Spliterator<E>) backingList.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException(MUTATION_OP_EXCEPTION_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<E> stream() {
        return (Stream<E>) backingList.stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<E> parallelStream() {
        return (Stream<E>) backingList.parallelStream();
    }
    
    @Override
    public void forEach(Consumer<? super E> action) {
        backingList.forEach(action);
    }
}
