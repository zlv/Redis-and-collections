package org.redis_collections;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.util.*;
import java.util.stream.Stream;

public class RedisSet extends RedisCollection implements Set<String> {
    public RedisSet(String name, UnifiedJedis jedis) {
        super(name, jedis);
    }

    public RedisSet(String name, Set<HostAndPort> nodes, String password) {
        super(name, nodes, password);
    }

    public RedisSet(String name, HostAndPort node, String password) {
        super(name, node, password);
    }

    public static RedisSet createSet(String name, Set<HostAndPort> nodes, Properties prop) {
        if (nodes.size() == 1) {
            return new RedisSet(name, nodes.stream().findFirst().get(), (String) prop.get("password"));
        } else {
            return new RedisSet(name, nodes, (String) prop.get("password"));
        }
    }

    @Override
    public int size() {
        return jedis.smembers(name).size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof String key) {
            return jedis.sismember(name, key);
        }
        return false;
    }

    @Override
    public Iterator<String> iterator() {
        // TODO: implement inner RedisIterator to allow remove operation
        return jedis.smembers(name).iterator();
    }

    @Override
    public Object[] toArray() {
        return jedis.smembers(name).toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return jedis.smembers(name).toArray(a);
    }

    @Override
    public boolean add(String key) {
        boolean isMember = jedis.sismember(name, key);
        jedis.sadd(name, key);
        return !isMember;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof String key) {
            boolean isMember = jedis.sismember(name, key);
            jedis.srem(name, key);
            return isMember;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!(o instanceof String) || !jedis.sismember(name, (String) o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        int previouSize = size();
        jedis.sadd(name, c.toArray(new String[0]));
        return previouSize != size();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Set<String> keys = new HashSet<>(jedis.smembers(name));
        int previouSize = keys.size();
        for (Object elem: c) {
            if (elem instanceof String s) {
                keys.remove(s);
            }
        }
        jedis.srem(name, keys.toArray(new String[0]));
        return previouSize != size();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int previouSize = size();
        jedis.srem(name, c.stream().flatMap(s -> (s instanceof String key) ? Stream.of(key) : null).toArray(String[]::new));
        return previouSize != size();
    }

    @Override
    public void clear() {
        jedis.srem(name, jedis.smembers(name).toArray(new String[0]));
    }
}
