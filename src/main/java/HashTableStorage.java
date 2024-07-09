import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class HashTableStorage<K, V> {
    private static final int DEFAULT_INIT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private final float loadFactor;
    private int initCapacity;
    private final List<HashTableEntry<K, V>> tables = new ArrayList<>();
    private static final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public HashTableStorage() {
        loadFactor = DEFAULT_LOAD_FACTOR;
        initCapacity = DEFAULT_INIT_CAPACITY;
        initNewTable();
    }

    public HashTableStorage(int initCapacity, float loadFactor) {
        this.initCapacity = initCapacity;
        this.loadFactor = loadFactor;
        initNewTable();
    }

    private HashTableEntry<K, V> initNewTable() {
        final HashTableEntry<K, V> table = new HashTableEntry<>(initCapacity);
        initCapacity = initCapacity * 2;
        tables.add(table);
        return table;
    }

    private HashTableEntry<K, V> getNotFilledTable() {
        for (final HashTableEntry<K, V> table : tables) {
            if (!isFilled(table)) {
                return table;
            }
        }
        return initNewTable();
    }

    private boolean isFilled(final HashTableEntry<K, V> table) {
        return table.getSize() > table.getCapacity() * loadFactor;
    }

    public void put(K key, V value) {
        HashTableEntry<K, V> table = forkJoinPool.invoke(new ContainedInTask(tables, key, 0, tables.size()));
        if (table == null) {
            table = getNotFilledTable();
        }
        table.put(key, value);
    }

    public V get(K key) {
        return forkJoinPool.invoke(new GetTask(tables, key, 0, tables.size()));
    }

    public void remove(K key) {
        forkJoinPool.invoke(new RemoveTask(tables, key, 0, tables.size()));
    }

    private static class HashTableEntry<K, V> {
        private final LinkedList<Entry<K, V>>[] table;
        private int size = 0;

        HashTableEntry(int initCapacity) {
            table = new LinkedList[initCapacity];
            for (int index = 0; index < initCapacity; index++) {
                table[index] = new LinkedList<>();
            }
        }

        public int getCapacity() {
            return this.table.length;
        }

        public int getSize() {
            return size;
        }

        public void put(K key, V value) {
            final int index = hash(key);
            final LinkedList<Entry<K, V>> bucket = table[index];
            for (Entry<K, V> entry : bucket) {
                if (entry.key.equals(key)) {
                    entry.value = value;
                    return;
                }
            }
            bucket.add(new Entry<>(key, value));
            size++;
        }

        public V get(K key) {
            final int index = hash(key);
            final LinkedList<Entry<K, V>> bucket = table[index];
            for (Entry<K, V> entry : bucket) {
                if (entry.key.equals(key)) {
                    return entry.value;
                }
            }
            return null;
        }

        private int hash(K key) {
            return Math.abs(key.hashCode()) % table.length;
        }

        public void remove(K key) {
            final int index = hash(key);
            final LinkedList<Entry<K, V>> bucket = table[index];
            for (Entry<K, V> entry : bucket) {
                if (entry.key.equals(key)) {
                    bucket.remove(entry);
//                    table[index] = bucket.stream().filter(record -> !record.key.equals(key)).collect(Collectors.());
                    size--;
                }
            }
        }

        private static final class Entry<K, V> {
            K key;
            V value;

            Entry(K key, V value) {
                this.key = key;
                this.value = value;
            }
        }
    }

    private class GetTask extends RecursiveTask<V> {
        private final List<HashTableEntry<K, V>> tables;
        private final K key;
        private final int start;
        private final int end;

        GetTask(List<HashTableEntry<K, V>> tables, K key, int start, int end) {
            this.tables = tables;
            this.key = key;
            this.start = start;
            this.end = end;
        }

        @Override
        public V compute() {
            if (end - start <= 1) {
                return tables.get(start).get(key);
            } else {
                final int mid = (start + end) / 2;
                final GetTask leftTask = new GetTask(tables, key, start, mid);
                final GetTask rightTask = new GetTask(tables, key, mid, end);
                leftTask.fork();
                V rightResult = rightTask.compute();
                V leftResult = leftTask.join();
                return leftResult != null ? leftResult : rightResult;
            }
        }
    }

    private class ContainedInTask extends RecursiveTask<HashTableEntry<K, V>> {
        private final List<HashTableEntry<K, V>> tables;
        private final K key;
        private final int start;
        private final int end;

        ContainedInTask(List<HashTableEntry<K, V>> tables, K key, int start, int end) {
            this.tables = tables;
            this.key = key;
            this.start = start;
            this.end = end;
        }

        @Override
        public HashTableEntry<K, V> compute() {
            if (end - start <= 1) {
                final HashTableEntry<K, V> table = tables.get(start);
                return table.get(key) == null ? null : table;
            } else {
                final int mid = (start + end) / 2;
                final ContainedInTask leftTask = new ContainedInTask(tables, key, start, mid);
                final ContainedInTask rightTask = new ContainedInTask(tables, key, mid, end);
                leftTask.fork();
                HashTableEntry<K, V> rightResult = rightTask.compute();
                HashTableEntry<K, V> leftResult = leftTask.join();
                return leftResult != null ? leftResult : rightResult;
            }
        }
    }

    private class RemoveTask extends RecursiveAction {
        private final List<HashTableEntry<K, V>> tables;
        private final K key;
        private final int start;
        private final int end;

        RemoveTask(List<HashTableEntry<K, V>> tables, K key, int start, int end) {
            this.tables = tables;
            this.key = key;
            this.start = start;
            this.end = end;
        }

        @Override
        public void compute() {
            if (end - start <= 1) {
                tables.get(start).remove(key);
            } else {
                final int mid = (start + end) / 2;
                final RemoveTask leftTask = new RemoveTask(tables, key, start, mid);
                final RemoveTask rightTask = new RemoveTask(tables, key, mid, end);
                invokeAll(leftTask, rightTask);
            }
        }
    }
}