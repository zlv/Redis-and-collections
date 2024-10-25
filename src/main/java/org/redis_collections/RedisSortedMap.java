package org.redis_collections;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.util.*;
import java.util.stream.Collectors;

public class RedisSortedMap extends RedisCollection implements SortedMap<String, Integer> {

    public static final String SORTED_SET = "_sorted_set_";

    public RedisSortedMap(String name, Set<HostAndPort> nodes, String password) {
        super(name, nodes, password);
    }

    public RedisSortedMap(String name, UnifiedJedis jedis) {
        super(name, jedis);
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
        jedis.zadd(SORTED_SET + name, 0., key);
        return previous == null ? null : Integer.valueOf(previous);
    }

    @Override
    public Integer remove(Object o) {
        if (o instanceof String key) {
            String previous = jedis.hget(name, key);
            jedis.hdel(name, key);
            jedis.zrem(SORTED_SET + name, key);
            return previous == null ? null : Integer.valueOf(previous);
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Integer> m) {
        jedis.hset(name, m.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString())));
        for (String key: m.keySet()) {
            jedis.zadd(SORTED_SET + name, 0., key);
        }
    }

    private List<String> getMembers() {
        return jedis.zrange(SORTED_SET + name, 0, -1);
    }

    @Override
    public void clear() {
        String[] keys = jedis.hkeys(name).toArray(new String[0]);
        jedis.hdel(name, keys);
        jedis.zrem(SORTED_SET + name, keys);
    }

    @Override
    public Comparator<? super String> comparator() {
        return Comparator.naturalOrder();
    }

    @Override
    public SortedMap<String, Integer> subMap(String fromKey, String toKey) {
        List<String> members = getMembers();
        return subSortedMap(members.indexOf(fromKey), members.indexOf(toKey) + 1, members);
    }

    private TreeMap<String, Integer> subSortedMap(int fromIndex, int toIndex, List<String> members) {
        Map<String, String> m = jedis.hgetAll(name);
        return members.subList(fromIndex, toIndex).stream().collect(Collectors.toMap(key -> key, key -> Integer.parseInt(m.get(key)), (v1, v2) -> v1, TreeMap<String, Integer>::new));
    }

    @Override
    public SortedMap<String, Integer> headMap(String toKey) {
        List<String> members = getMembers();
        return subSortedMap(0, members.indexOf(toKey) + 1, members);
    }

    @Override
    public SortedMap<String, Integer> tailMap(String fromKey) {
        List<String> members = getMembers();
        return subSortedMap(members.indexOf(fromKey), size(), members);
    }

    @Override
    public String firstKey() {
        return getMembers().getFirst();
    }

    @Override
    public String lastKey() {
        return getMembers().getLast();
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
        return jedis.hgetAll(name).entrySet().stream().map(entry -> new RedisSortedMap.RedisEntry(entry.getKey(), Integer.valueOf(entry.getValue()))).collect(Collectors.toCollection(() -> new TreeSet<>(Entry.comparingByKey())));
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
