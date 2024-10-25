package org.redis_collections;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisMap extends RedisCollection implements Map<String, Integer> {
    public RedisMap(String name, Set<HostAndPort> nodes, String password) {
        super(name, nodes, password);
    }

    public RedisMap(String name, HostAndPort node, String password) {
        super(name, node, password);
    }

    public RedisMap(String name, UnifiedJedis jedis) {
        super(name, jedis);
    }

    public static RedisMap createMap(String map, Set<HostAndPort> nodes, Properties prop) {
        if (nodes.size() == 1) {
            return new RedisMap(map, nodes.stream().findFirst().get(), (String) prop.get("password"));
        } else {
            return new RedisMap(map, nodes, (String) prop.get("password"));
        }
    }

    @Override
    public int size() {
        return (int) jedis.hlen(name);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object o) {
        if (o instanceof String key) {
            return jedis.hexists(name, key);
        }
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        if (o instanceof Integer value) {
            return jedis.hvals(name).contains(value.toString());
        }
        return false;
    }

    @Override
    public Integer get(Object o) {
        if (o instanceof String key) {
            String result = jedis.hget(name, key);
            return result == null ? null : Integer.valueOf(result);
        }
        return null;
    }

    @Override
    public Integer put(String key, Integer value) {
        String previous = jedis.hget(name, key);
        jedis.hset(name, key, value.toString());
        return previous == null ? null : Integer.valueOf(previous);
    }

    @Override
    public Integer remove(Object o) {
        if (o instanceof String key) {
            String previous = jedis.hget(name, key);
            jedis.hdel(name, key);
            return previous == null ? null : Integer.valueOf(previous);
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Integer> map) {
        jedis.hset(name, map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString())));
    }

    @Override
    public void clear() {
        jedis.hdel(name, jedis.hkeys(name).toArray(new String[0]));
    }

    @Override
    public Set<String> keySet() {
        // TODO: implement inner RedisSet to allow collection write operations
        return jedis.hkeys(name);
    }

    @Override
    public Collection<Integer> values() {
        // TODO: implement inner RedisCollection to allow collection write operations
        return jedis.hvals(name).stream().map(Integer::valueOf).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, Integer>> entrySet() {
        // TODO: implement inner RedisEntrySet to allow collection write operations
        return jedis.hgetAll(name).entrySet().stream().map(entry -> new RedisEntry(entry.getKey(), Integer.valueOf(entry.getValue()))).collect(Collectors.toSet());
    }

    private class RedisEntry implements Entry<String, Integer> {
        private final String key;
        private final Integer value;

        public RedisEntry(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(Integer value) {
            return put(key, value);
        }
    }
}
