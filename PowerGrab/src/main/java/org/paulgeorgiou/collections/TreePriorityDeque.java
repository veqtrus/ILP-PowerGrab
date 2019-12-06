/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

package org.paulgeorgiou.collections;

import java.util.*;

/**
 * A priority {@linkplain Deque deque} based on a {@link TreeSet}.
 * The elements of the priority queue are ordered according to their
 * {@linkplain Comparable natural ordering}, or by a {@link Comparator}
 * provided at queue construction time, depending on which constructor
 * is used. A priority queue does not permit {@code null} elements.
 * A priority queue relying on natural ordering also does not permit
 * insertion of non-comparable objects (doing so may result in
 * {@code ClassCastException}). Elements with equal priority are permitted
 * and are ordered in a similar way to a {@linkplain Deque deque}.
 *
 * @author Pavlos (Paul) Georgiou
 * @see PriorityQueue
 * @see TreeSet
 * @see Deque
 * @see Queue
 * @param <E> the type of elements held in this collection
 */
public class TreePriorityDeque<E> extends AbstractQueue<E>
    implements Deque<E>, java.io.Serializable, Cloneable {

    private class Entry implements Comparable<Entry> {
        public final E value;
        private final long index;

        Entry(E value, long index) {
            this.value = value;
            this.index = index;
        }

        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(Entry o) {
            // Use the comparator to compare elements if provided,
            // otherwise use their natural ordering.
            int compare = comparator == null
                    ? ((Comparable<? super E>) value).compareTo(o.value)
                    : comparator.compare(value, o.value);
            // If two elements are equal, compare their indices
            return compare == 0 ? Long.compare(index, o.index) : compare;
        }
    }

    private class ObjectEntry extends Entry {
        ObjectEntry(E value) {
            super(value, Long.MIN_VALUE);
        }

        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(Entry o) {
            // Use the comparator to compare elements if provided,
            // otherwise use their natural ordering.
            return comparator == null
                    ? ((Comparable<? super E>) value).compareTo(o.value)
                    : comparator.compare(value, o.value);
        }
    }

    private final class EntryIterator implements Iterator<E> {
        private final Iterator<Entry> iterator;

        EntryIterator(Iterator<Entry> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            return iterator.next().value;
        }
    }

    private long index = 0;
    private int maxSize = Integer.MAX_VALUE;
    private Comparator<? super E> comparator;
    private TreeSet<Entry> tree;

    private long getIndex() {
        if (index == Long.MAX_VALUE || index < 0) index = 0;
        return index++;
    }

    private void trim() {
        trim(maxSize);
    }

    /**
     * Creates a {@code TreePriorityDeque} that orders its elements
     * according to their {@linkplain Comparable natural ordering}.
     */
    public TreePriorityDeque() {
        this.comparator = null;
        this.tree = new TreeSet<>();
    }

    /**
     * Creates a {@code TreePriorityDeque} that orders its elements
     * according to the specified comparator.
     *
     * @param comparator the comparator that will be used to order this
     *        priority queue. If {@code null}, the {@linkplain Comparable
     *        natural ordering} of the elements will be used.
     */
    public TreePriorityDeque(Comparator<? super E> comparator) {
        this.comparator = comparator;
        this.tree = new TreeSet<>();
    }

    /**
     * Creates a {@code TreePriorityDeque} containing the elements in the
     * specified collection. If the specified collection is an instance of
     * a {@code TreePriorityDeque}, this priority queue will be ordered
     * according to the same ordering. Otherwise, this priority queue will
     * be ordered according to the {@linkplain Comparable natural ordering}
     * of its elements.
     *
     * @param c the collection whose elements are to be placed
     *        into this priority queue
     * @throws ClassCastException if elements of the specified collection
     *         cannot be compared to one another according to the priority
     *         queue's ordering
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    @SuppressWarnings("unchecked")
    public TreePriorityDeque(Collection<? extends E> c) {
        if (c instanceof TreePriorityDeque<?> && c.getClass() == TreePriorityDeque.class) {
            // c is a TreePriorityDeque and not an object which extends TreePriorityDeque
            TreePriorityDeque<? extends E> tpq = (TreePriorityDeque<? extends E>) c;
            this.tree = (TreeSet<Entry>) tpq.tree.clone();
            this.comparator = (Comparator<? super E>) tpq.comparator;
            this.index = tpq.index;
        } else {
            this.comparator = null;
            this.tree = new TreeSet<>();
            addAll(c);
        }
    }

    /**
     * Creates a {@code TreePriorityDeque} that orders its elements
     * according to the specified comparator, containing the elements
     * in the specified collection.
     *
     * @param c the collection whose elements are to be placed
     *        into this priority queue
     * @param comparator the comparator that will be used to order this
     *        priority queue.  If {@code null}, the {@linkplain Comparable
     *        natural ordering} of the elements will be used.
     * @throws ClassCastException if elements of the specified collection
     *         cannot be compared to one another according to the priority
     *         queue's ordering
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    @SuppressWarnings("unchecked")
    public TreePriorityDeque(Collection<? extends E> c, Comparator<? super E> comparator) {
        if (c instanceof TreePriorityDeque<?> && c.getClass() == TreePriorityDeque.class) {
            // c is a TreePriorityDeque and not an object which extends TreePriorityDeque
            TreePriorityDeque<? extends E> tpq = (TreePriorityDeque<? extends E>) c;
            this.tree = (TreeSet<Entry>) tpq.tree.clone();
            this.comparator = comparator;
            this.index = tpq.index;
        } else {
            this.comparator = comparator;
            this.tree = new TreeSet<>();
            addAll(c);
        }
    }

    /**
     * @return the maximum size of this priority queue
     */
    public int getMaxSize() { return maxSize; }

    /**
     * Set the maximum size of this priority queue. When the maximum size is
     * exceeded, the greatest elements are removed to meet the restriction.
     *
     * @param maxSize the maximum size of the queue
     * @throws IllegalArgumentException if the specified size is negative
     */
    public void setMaxSize(int maxSize) {
        if (maxSize < 0) throw new IllegalArgumentException();
        this.maxSize = maxSize;
        trim();
    }

    /**
     * Returns the comparator used to order the elements in this
     * queue, or {@code null} if this queue is sorted according to
     * the {@linkplain Comparable natural ordering} of its elements.
     *
     * @return the comparator used to order this queue, or
     *         {@code null} if this queue is sorted according to the
     *         natural ordering of its elements
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /**
     * Remove the greatest elements of the priority queue such that its size
     * is no greater than the maximum size specified.
     *
     * @throws IllegalArgumentException if the specified size is negative
     */
    public void trim(int maxSize) {
        if (maxSize < 0)
            throw new IllegalArgumentException();
        while (tree.size() > maxSize)
            if (tree.pollLast() == null)
                throw new IllegalStateException();
    }

    /**
     * Returns an iterator over the elements in this queue,
     * from the smallest to the largest.
     *
     * @return an iterator over the elements in this queue
     */
    @Override
    public Iterator<E> iterator() {
        return new EntryIterator(tree.iterator());
    }

    /**
     * Returns an iterator over the elements in this queue,
     * from the largest to the smallest.
     *
     * @return an iterator over the elements in this queue
     */
    @Override
    public Iterator<E> descendingIterator() {
        return new EntryIterator(tree.descendingIterator());
    }

    /**
     * Inserts the specified element into this priority queue.
     * The element is inserted at the start of the group of elements
     * which have equal priority.
     *
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public void addFirst(E e) {
        if (!offerFirst(e)) throw new IllegalStateException();
    }

    /**
     * Inserts the specified element into this priority queue.
     * The element is inserted at the end of the group of elements
     * which have equal priority.
     *
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public void addLast(E e) {
        if (!offerLast(e)) throw new IllegalStateException();
    }

    /**
     * Inserts the specified element into this priority queue.
     * The element is inserted at the start of the group of elements
     * which have equal priority.
     *
     * @return {@code true} (as specified by {@link Deque#offerFirst})
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean offerFirst(E e) {
        if (e == null) throw new NullPointerException();
        boolean result = tree.add(new Entry(e, -getIndex()));
        trim();
        return result;
    }

    /**
     * Inserts the specified element into this priority queue.
     * The element is inserted at the end of the group of elements
     * which have equal priority.
     *
     * @return {@code true} (as specified by {@link Deque#offerLast})
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean offerLast(E e) {
        if (e == null) throw new NullPointerException();
        boolean result = tree.add(new Entry(e, getIndex()));
        trim();
        return result;
    }

    /**
     * Retrieves and removes the first element of this priority queue. This method
     * differs from {@link #pollFirst pollFirst} only in that it throws an
     * exception if this priority queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E removeFirst() {
        Entry result = tree.pollFirst();
        if (result == null) throw new NoSuchElementException();
        return result.value;
    }

    /**
     * Retrieves and removes the last element of this priority queue. This method
     * differs from {@link #pollLast pollLast} only in that it throws an
     * exception if this priority queue is empty.
     *
     * @return the tail of this queue
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E removeLast() {
        Entry result = tree.pollLast();
        if (result == null) throw new NoSuchElementException();
        return result.value;
    }

    /**
     * Retrieves and removes the first element of this priority queue,
     * or returns {@code null} if this priority queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E pollFirst() {
        Entry result = tree.pollFirst();
        if (result == null) return null;
        return result.value;
    }

    /**
     * Retrieves and removes the last element of this priority queue,
     * or returns {@code null} if this priority queue is empty.
     *
     * @return the tail of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E pollLast() {
        Entry result = tree.pollLast();
        if (result == null) return null;
        return result.value;
    }

    /**
     * Retrieves, but does not remove, the first element of this priority queue.
     *
     * This method differs from {@link #peekFirst peekFirst} only in that it
     * throws an exception if this priority queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    @Override
    public E getFirst() {
        Entry result = tree.last();
        if (result == null) throw new NoSuchElementException();
        return result.value;
    }

    /**
     * Retrieves, but does not remove, the last element of this priority queue.
     * This method differs from {@link #peekLast peekLast} only in that it
     * throws an exception if this priority queue is empty.
     *
     * @return the tail of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    @Override
    public E getLast() {
        Entry result = tree.first();
        if (result == null) throw new NoSuchElementException();
        return result.value;
    }

    /**
     * Retrieves, but does not remove, the first element of this priority queue,
     * or returns {@code null} if this priority queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E peekFirst() {
        Entry result = tree.first();
        if (result == null) return null;
        return result.value;
    }

    /**
     * Retrieves, but does not remove, the last element of this priority queue,
     * or returns {@code null} if this priority queue is empty.
     *
     * @return the tail of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E peekLast() {
        Entry result = tree.last();
        if (result == null) return null;
        return result.value;
    }

    /**
     * Removes the first occurrence of the specified element from this queue.
     * If the queue does not contain the element, it is unchanged.
     * Returns {@code true} if this queue contained the specified element.
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if an element was removed as a result of this call
     * @throws ClassCastException if the specified object cannot be compared
     *         with the elements currently in this queue
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    /**
     * Removes the first occurrence of the specified element from this queue.
     * If the queue does not contain the element, it is unchanged.
     * Returns {@code true} if this queue contained the specified element.
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if an element was removed as a result of this call
     * @throws ClassCastException if the specified object cannot be compared
     *         with the elements currently in this queue
     * @throws NullPointerException if the specified element is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean removeFirstOccurrence(Object o) {
        if (o == null) throw new NullPointerException();
        Entry entryToRemove = tree.ceiling(new Entry((E) o, Long.MIN_VALUE));
        return entryToRemove != null && tree.remove(entryToRemove);
    }

    /**
     * Removes the last occurrence of the specified element from this queue.
     * If the queue does not contain the element, it is unchanged.
     * Returns {@code true} if this queue contained the specified element.
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if an element was removed as a result of this call
     * @throws ClassCastException if the specified object cannot be compared
     *         with the elements currently in this queue
     * @throws NullPointerException if the specified element is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean removeLastOccurrence(Object o) {
        if (o == null) throw new NullPointerException();
        Entry entryToRemove = tree.floor(new Entry((E) o, Long.MAX_VALUE));
        return entryToRemove != null && tree.remove(entryToRemove);
    }

    /**
     * Inserts the specified element into this priority queue.
     * The element is inserted at the end of the group of elements
     * which have equal priority.
     *
     * <p>This method is equivalent to {@link #offerLast}.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    /**
     * Retrieves and removes the first element of this priority queue,
     * or returns {@code null} if this priority queue is empty.
     *
     * <p>This method is equivalent to {@link #pollFirst()}.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E poll() {
        return pollFirst();
    }

    /**
     * Retrieves, but does not remove, the first element of this priority queue,
     * or returns {@code null} if this priority queue is empty.
     *
     * <p>This method is equivalent to {@link #peekFirst()}.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E peek() {
        return peekFirst();
    }

    /**
     * Inserts the specified element into this priority queue.
     * The element is inserted at the start of the group of elements
     * which have equal priority.
     *
     * <p>This method is equivalent to {@link #addFirst}.
     *
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public void push(E e) {
        addFirst(e);
    }

    /**
     * Retrieves and removes the first element of this priority queue. This method
     * differs from {@link #pollFirst pollFirst} only in that it throws an
     * exception if this priority queue is empty.
     *
     * <p>This method is equivalent to {@link #removeFirst()}.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E pop() {
        return removeFirst();
    }

    /**
     * Removes all of the elements from this priority queue.
     * The queue will be empty after this call returns.
     */
    @Override
    public void clear() {
        tree.clear();
        index = 0;
    }

    /**
     * @return the number of elements in this priority queue
     */
    @Override
    public int size() {
        return tree.size();
    }

    /**
     * @return {@code true} if this priority queue contains no elements
     */
    @Override
    public boolean isEmpty() {
        return tree.isEmpty();
    }

    /**
     * Returns {@code true} if this queue contains the specified element.
     *
     * @return {@code true} if this queue contains the specified element
     * @throws ClassCastException if the specified object cannot be compared
     *         with the elements currently in the queue
     * @throws NullPointerException if the specified element is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (o == null) throw new NullPointerException();
        return tree.contains(new ObjectEntry((E) o));
    }

    /**
     * Returns a shallow copy of this {@code TreePriorityDeque} instance.
     * (The elements themselves are not cloned.)
     *
     * @return a shallow copy of this queue
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        TreePriorityDeque<E> clone;
        try {
            clone = (TreePriorityDeque<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        clone.tree = (TreeSet<Entry>) tree.clone();
        clone.comparator = comparator;
        clone.index = index;
        return clone;
    }
}
