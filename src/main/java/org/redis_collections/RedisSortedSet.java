package org.redis_collections;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.util.*;
import java.util.stream.Stream;

public class RedisSortedSet extends RedisCollection implements SortedSet<String> {
    public RedisSortedSet(String name, UnifiedJedis jedis) {
        super(name, jedis);
    }

    public RedisSortedSet(String name, Set<HostAndPort> nodes, String password) {
        super(name, nodes, password);
    }

    @Override
    public int size() {
        return getMembers().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof String key) {
            return isContains(key);
        }
        return false;
    }

    private List<String> getMembers() {
        return jedis.zrange(name, 0, -1);
    }

    @Override
    public Iterator<String> iterator() {
        // TODO: implement inner RedisIterator to allow remove operation
        return getMembers().iterator();
    }

    @Override
    public Object[] toArray() {
        return getMembers().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getMembers().toArray(a);
    }

    @Override
    public boolean add(String key) {
        boolean isMember = isContains(key);
        jedis.zadd(name, 0., key);
        return !isMember;
    }

    private boolean isContains(String key) {
        return getMembers().contains(key);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof String key) {
            boolean isMember = isContains(key);
            jedis.zrem(name, key);
            return isMember;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!(o instanceof String) || !isContains((String) o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        int previouSize = size();
        for (String key: c) {
            jedis.zadd(name, 0., key);
        }
        return previouSize != size();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<String> keys = new ArrayList<>(getMembers());
        int previouSize = keys.size();
        for (Object elem: c) {
            if (elem instanceof String s) {
                keys.remove(s);
            }
        }
        jedis.zrem(name, keys.toArray(new String[0]));
        return previouSize != size();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int previouSize = size();
        jedis.zrem(name, c.stream().flatMap(s -> (s instanceof String key) ? Stream.of(key) : null).toArray(String[]::new));
        return previouSize != size();
    }

    @Override
    public void clear() {
        jedis.zrem(name, getMembers().toArray(new String[0]));
    }

    @Override
    public Comparator<? super String> comparator() {
        return Comparator.naturalOrder();
    }

    @Override
    public SortedSet<String> subSet(String fromElement, String toElement) {
        List<String> members = getMembers();
        return new TreeSet<>(members.subList(members.indexOf(fromElement), members.indexOf(toElement) + 1));
    }

    @Override
    public SortedSet<String> headSet(String toElement) {
        List<String> members = getMembers();
        return new TreeSet<>(members.subList(0, members.indexOf(toElement) + 1));
    }

    @Override
    public SortedSet<String> tailSet(String fromElement) {
        List<String> members = getMembers();
        return new TreeSet<>(members.subList(members.indexOf(fromElement), size()));
    }

    @Override
    public String first() {
        return getMembers().getFirst();
    }

    @Override
    public String last() {
        return getMembers().getLast();
    }
}
