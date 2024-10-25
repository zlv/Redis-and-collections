package org.redis_collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class RedisSortedMapTest {
    public static final String KEY = "key";
    public static final String NAME = "map";
    public static final String SORTED_SET = "_sorted_set_";
    @Mock
    private UnifiedJedis jedis;
    private RedisSortedMap redisMap;

    @BeforeEach
    void setUp() {
        redisMap = new RedisSortedMap(NAME, jedis);
    }

    @org.junit.jupiter.api.Test
    void createMap() {
        assertTrue(redisMap.isEmpty());
        Mockito.verify(jedis).hlen(NAME);
        assertEquals(0, redisMap.size());
        assertFalse(redisMap.containsKey(KEY));
        Mockito.verify(jedis).hexists(NAME, KEY);
        assertFalse(redisMap.containsValue(123));
        Mockito.verify(jedis).hvals(NAME);
        assertNull(redisMap.put(KEY, 1234));
        Mockito.verify(jedis).hset(NAME, KEY, "1234");
        Mockito.verify(jedis, Mockito.times(1)).zadd(SORTED_SET + NAME, 0., KEY);
        Mockito.when(jedis.hget(NAME, KEY)).thenReturn("1234");
        assertNotNull(redisMap.put(KEY, 123));
        Mockito.verify(jedis).hset(NAME, KEY, "123");
        Mockito.verify(jedis, Mockito.times(2)).zadd(SORTED_SET + NAME, 0., KEY);
        Mockito.when(jedis.hexists(NAME, KEY)).thenReturn(true);
        assertTrue(redisMap.containsKey(KEY));
        Mockito.when(jedis.hvals(NAME)).thenReturn(List.of("123"));
        assertTrue(redisMap.containsValue(123));
        Mockito.when(jedis.hget(NAME, KEY)).thenReturn("123");
        assertEquals(123, redisMap.get(KEY));
        Mockito.when(jedis.hlen(NAME)).thenReturn(1L);
        assertEquals(1, redisMap.size());

        redisMap.remove(KEY);
    }

    @Test
    void putAll() {
        redisMap.putAll(Map.of(KEY, 123, "key2", 1234));
        Mockito.verify(jedis).hset(NAME, Map.of(KEY, "123", "key2", "1234"));
        Mockito.verify(jedis).zadd(SORTED_SET + NAME, 0., KEY);
        Mockito.verify(jedis).zadd(SORTED_SET + NAME, 0., "key2");
    }

    @org.junit.jupiter.api.Test
    void clear() {
        Mockito.reset(jedis);
        Mockito.when(jedis.hkeys(NAME)).thenReturn(Set.of(KEY, "key2"));
        redisMap.clear();
        Mockito.verify(jedis).hdel(anyString(), anyString(), anyString());
        Mockito.verify(jedis).zrem(anyString(), anyString(), anyString());
    }

    @Test
    void subMap() {
        Mockito.when(jedis.hgetAll(NAME)).thenReturn(Map.of("key1", "1", "key3", "1", "key4", "1", "key2", "2", "key5", "2"));
        Mockito.when(jedis.zrange(SORTED_SET + NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        SortedMap<String, Integer> subMap = redisMap.subMap("key2", "key4");
        assertTrue(subMap.containsKey("key2") && subMap.containsKey("key3") && subMap.containsKey("key4"));
        assertEquals(3, subMap.size());
        assertEquals(2, subMap.get("key2"));
        assertEquals(1, subMap.get("key3"));
        assertEquals(1, subMap.get("key4"));
    }

    @Test
    void headMap() {
        Mockito.when(jedis.hgetAll(NAME)).thenReturn(Map.of("key1", "1", "key3", "1", "key4", "1", "key2", "2", "key5", "2"));
        Mockito.when(jedis.zrange(SORTED_SET + NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        SortedMap<String, Integer> subMap = redisMap.headMap("key4");
        assertTrue(subMap.containsKey("key1") && subMap.containsKey("key2") && subMap.containsKey("key3") && subMap.containsKey("key4"));
        assertEquals(4, subMap.size());
        assertEquals(1, subMap.get("key1"));
        assertEquals(2, subMap.get("key2"));
        assertEquals(1, subMap.get("key3"));
        assertEquals(1, subMap.get("key4"));
    }

    @Test
    void tailMap() {
        Mockito.when(jedis.hgetAll(NAME)).thenReturn(Map.of("key1", "1", "key3", "1", "key4", "1", "key2", "2", "key5", "2"));
        Mockito.when(jedis.zrange(SORTED_SET + NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        Mockito.when(jedis.hlen(NAME)).thenReturn(5L);
        SortedMap<String, Integer> subMap = redisMap.tailMap("key2");
        assertTrue(subMap.containsKey("key2") && subMap.containsKey("key3") && subMap.containsKey("key4") && subMap.containsKey("key5"));
        assertEquals(4, subMap.size());
        assertEquals(2, subMap.get("key2"));
        assertEquals(1, subMap.get("key3"));
        assertEquals(1, subMap.get("key4"));
        assertEquals(2, subMap.get("key5"));
    }

    @Test
    void firstKey() {
        Mockito.when(jedis.zrange(SORTED_SET + NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        assertEquals("key1", redisMap.firstKey());
    }

    @Test
    void lastKey() {
        Mockito.when(jedis.zrange(SORTED_SET + NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        assertEquals("key5", redisMap.lastKey());
    }
}