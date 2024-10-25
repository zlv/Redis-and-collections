package org.redis_collections;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class RedisList extends RedisCollection implements List<String> {
    private static final String DELETED = "*DELETED*";

    public RedisList(String name, UnifiedJedis jedis) {
        super(name, jedis);
    }

    public RedisList(String name, Set<HostAndPort> nodes, String password) {
        super(name, nodes, password);
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
    public Iterator<String> iterator() {
        // TODO: implement inner RedisIterator to allow remove operation
        return getAllEntries().iterator();
    }

    private List<String> getAllEntries() {
        return jedis.lrange(name, 0, -1);
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
    public boolean add(String key) {
        if (key.equals(DELETED)) {
            throw new RuntimeException("Wrong value format");
        }
        jedis.rpush(name, key);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof String key) {
            int previous = size();
            jedis.lrem(name, 0, key);
            return previous != size();
        }
        return false;
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
    public boolean addAll(Collection<? extends String> c) {
        if (c.contains(DELETED)) {
            throw new RuntimeException("Wrong value format");
        }
        jedis.rpush(name, c.toArray(new String[0]));
        return !c.isEmpty();
    }

    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        if (c.contains(DELETED)) {
            throw new RuntimeException("Wrong value format");
        }
        if (index == 0) {
            jedis.lpush(name, c.toArray(new String[0]));
        } else if (index == size()) {
            jedis.rpush(name, c.toArray(new String[0]));
        } else {
            throw new RuntimeException("Unimplemented");
        }
        return !c.isEmpty();
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
    public void clear() {
        jedis.del(name);
    }

    @Override
    public String get(int index) {
        if (size() <= index) {
            throw new IndexOutOfBoundsException();
        }
        return jedis.lrange(name, index, index).getFirst();
    }

    @Override
    public String set(int index, String element) {
        if (element.equals(DELETED)) {
            throw new RuntimeException("Wrong value format");
        }
        String previous;
        if (index == 0) {
            previous = jedis.lpop(name);
            jedis.lpush(name, element);
        } else if (index == size() - 1) {
            previous = jedis.rpop(name);
            jedis.rpush(name, element);
        } else {
            throw new RuntimeException("Unimplemented");
        }
        return previous;
    }

    @Override
    public void add(int index, String element) {
        if (element.equals(DELETED)) {
            throw new RuntimeException("Wrong value format");
        }
        if (index == 0) {
            jedis.lpush(name, element);
        } else if (index == size() - 1) {
            jedis.rpush(name, element);
        } else {
            throw new RuntimeException("Unimplemented");
        }
    }

    @Override
    public String remove(int index) {
        String previous = get(index);
        jedis.lset(name, index, DELETED);
        jedis.lrem(name, 1, DELETED);
        return previous;
    }

    @Override
    public int indexOf(Object o) {
        return getAllEntries().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getAllEntries().lastIndexOf(o);
    }

    @Override
    public ListIterator<String> listIterator() {
        // TODO: implement inner ListIterator to allow collection write operations
        return getAllEntries().listIterator();
    }

    @Override
    public ListIterator<String> listIterator(int index) {
        // TODO: implement inner ListIterator to allow collection write operations
        return getAllEntries().listIterator(index);
    }

    @Override
    public List<String> subList(int fromIndex, int toIndex) {
        return jedis.lrange(name, fromIndex, toIndex);
    }
}
