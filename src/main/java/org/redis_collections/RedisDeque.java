package org.redis_collections;

import redis.clients.jedis.HostAndPort;

import java.util.*;

public class RedisDeque extends RedisCollection implements Deque<String> {
    public RedisDeque(String name, Set<HostAndPort> nodes, String password) {
        super(name, nodes, password);
    }

    @Override
    public void addFirst(String s) {
        jedis.lpush(name, s);
    }

    @Override
    public void addLast(String s) {
        jedis.rpush(name, s);
    }

    @Override
    public boolean offerFirst(String s) {
        addFirst(s);
        return true;
    }

    @Override
    public boolean offerLast(String s) {
        addLast(s);
        return true;
    }

    @Override
    public String removeFirst() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return jedis.lpop(name);
    }

    @Override
    public String removeLast() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return jedis.rpop(name);
    }

    @Override
    public String pollFirst() {
        if (size() == 0) {
            return null;
        }
        return jedis.lpop(name);
    }

    @Override
    public String pollLast() {
        if (size() == 0) {
            return null;
        }
        return jedis.rpop(name);
    }

    @Override
    public String getFirst() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return getElement(0);
    }

    private String getElement(int i) {
        return jedis.lrange(name, i, i).getFirst();
    }

    @Override
    public String getLast() {
        int size = size();
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return getElement(size - 1);
    }

    @Override
    public String peekFirst() {
        if (size() == 0) {
            return null;
        }
        return getElement(0);
    }

    @Override
    public String peekLast() {
        int size = size();
        if (size == 0) {
            return null;
        }
        return getElement(size - 1);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        int previouSize = size();
        if (o instanceof String key) {
            jedis.lrem(name, 1, key);
        }
        return previouSize != size();
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        int previouSize = size();
        if (o instanceof String key) {
            jedis.lrem(name, -1, key);
        }
        return previouSize != size();
    }

    @Override
    public boolean add(String s) {
        jedis.rpush(name, s);
        return true;
    }

    @Override
    public boolean offer(String s) {
        return offerLast(s);
    }

    @Override
    public String remove() {
        return removeFirst();
    }

    @Override
    public String poll() {
        return pollFirst();
    }

    @Override
    public String element() {
        return getFirst();
    }

    @Override
    public String peek() {
        return peekFirst();
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        jedis.rpush(name, c.toArray(new String[1]));
        return !c.isEmpty();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int previouSize = size();
        for (Object entry: c) {
            if (entry instanceof String key) {
                jedis.lrem(name, 0, key);
            }
        }
        return previouSize != size();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int previouSize = size();
        List<String> entries = new ArrayList<>(getAllEntries());
        entries.removeAll(c);
        for (String entry: entries) {
            jedis.lrem(name, 0, entry);
        }
        return previouSize != size();
    }

    private List<String> getAllEntries() {
        return jedis.lrange(name, 0, size());
    }

    @Override
    public void clear() {
        jedis.del(name);
    }

    @Override
    public void push(String s) {
        addFirst(s);
    }

    @Override
    public String pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        List<String> entries = getAllEntries();
        for (Object o: c) {
            if (!(o instanceof String) || !entries.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof String) {
            for (String value: getAllEntries()) {
                if (value.equals(o)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int size() {
        return (int) jedis.llen(name);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<String> iterator() {
        // TODO: implement inner RedisIterator to allow remove operation
        return getAllEntries().iterator();
    }

    @Override
    public Object[] toArray() {
        return getAllEntries().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getAllEntries().toArray(a);
    }

    @Override
    public Iterator<String> descendingIterator() {
        return new Iterator<>() {
            int index = size() - 1;

            @Override
            public boolean hasNext() {
                return index >= 0;
            }

            @Override
            public String next() {
                return getElement(index--);
            }
        };
    }
}
