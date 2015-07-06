package diffprocessor;

import sun.plugin.dom.exception.InvalidStateException;

import java.util.List;

/**
 * Created by VavilauA on 6/19/2015.
 */
public class SortedLimitedList<T extends Comparable<T>> {
    public class Entry {
        private T value;
        private Entry previous;
        private Entry next;
        private SortedLimitedList list;

        public T getValue() {
            return value;
        }

        public Entry getPrevious() {
            return previous;
        }

        public Entry getNext() {
            return next;
        }

        public SortedLimitedList getList() {
            return list;
        }
    }

    Entry freePool;
    Entry first;
    Entry last;
    int count;
    int limit;
    int performedOperations;

    private Entry allocate() {
        if (freePool == null)
            throw new OutOfMemoryError();
        Entry result = freePool;
        freePool = freePool.next;

        return result;
    }

    protected void free(Entry entry) {
        entry.next = freePool;
        freePool = entry;
    }

    protected void check(Entry entry) {
        Entry before = entry.previous;
        if (before != null && before.value.compareTo(entry.value) > 0)
            throw new InvalidStateException("List not sorted");

        Entry after = entry.next;
        if (after != null && after.value.compareTo(entry.value) < 0)
            throw new InvalidStateException("List not sorted");
    }

    public SortedLimitedList(int limit) {
        first = null;
        last = null;
        freePool = null;
        this.limit = limit;
        this.performedOperations = 0;
        this.count = 0;
        for (int i = 0; i < limit; ++i) {
            Entry current = new Entry();
            current.list = this;
            free(current);
        }
    }

    public void fromArray(T[] array)
    {
        clear();
        for (int i = 0; i < array.length; ++i)
            addLast(array[i]);
        performedOperations = 0;
    }

    public void fromList(List<T> array)
    {
        clear();
        for (int i = 0; i < array.size(); ++i)
            addLast(array.get(i));
        performedOperations = 0;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        Entry e = first;
        while (e != null)
        {
            builder.append(e.value);
            if (e.next != null)
            {
                builder.append(", ");
            }
            e = e.next;
        }

        return builder.toString();
    }

    public void addAfter(Entry after, T value)
    {
        if (after == null)
            addLast(value);
        else
        {
            Entry entry = allocate();

            entry.value = value;
            entry.previous = after;
            entry.next = after.next;
            ++count;

            if (after.next != null)
                after.next.previous = entry;
            else
                last = entry;

            after.next = entry;
            ++performedOperations;
            check(entry);
        }
    }

    public void addBefore(Entry before, T value)
    {
        if (before == null)
            addFirst(value);
        else
        {
            Entry entry = allocate();

            entry.value = value;
            entry.previous = before.previous;
            entry.next = before;
            ++count;

            if (before.previous != null)
                before.previous.next = entry;
            else
                first = entry;

            before.previous = entry;
            ++performedOperations;
            check(entry);
        }
    }

    public void addLast(T value)
    {
        Entry entry = allocate();

        entry.value = value;
        entry.previous = last;
        entry.next = null;
        ++count;

        if (last != null)
            last.next = entry;
        else
            first = entry;

        last = entry;
        ++performedOperations;
        check(entry);
    }

    public void addFirst(T value)
    {
        Entry entry = allocate();

        entry.value = value;
        entry.previous = null;
        entry.next = first;
        ++count;

        if (first != null)
            first.previous = entry;
        else
            last = entry;

        first = entry;
        ++performedOperations;
        check(entry);
    }

    public void remove(Entry entry)
    {
        if (entry.previous != null)
            entry.previous.next = entry.next;
        else
            first = entry.next;

        if (entry.next != null)
            entry.next.previous = entry.previous;
        else
            last = entry.previous;

        ++performedOperations;
        --count;
        free(entry);
    }

    public void clear()
    {
        Entry entry = first, next;
        while (entry != null)
        {
            next = entry.next;
            remove(entry);
            entry = next;
        }
        performedOperations = 0;
    }

    public boolean equals(SortedLimitedList<T> list)
    {
        Entry e1 = first;
        Entry e2 = list.first;
        while (e1 != null && e2 != null)
        {
            if (e1.value.compareTo(e2.value) != 0)
                return false;
            e1 = e1.next;
            e2 = e2.next;
        }

        return e1 == null && e2 == null;
    }

    public Entry getFirst() {
        return first;
    }

    public Entry getLast() {
        return last;
    }

    public int getCount() {
        return count;
    }

    public int getLimit() {
        return limit;
    }

    public int getPerformedOperations() {
        return performedOperations;
    }
}
