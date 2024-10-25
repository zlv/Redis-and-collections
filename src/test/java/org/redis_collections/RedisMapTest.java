package org.redis_collections;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class RedisMapTest {

    public static final String KEY = "key";
    public static final String NAME = "map";
    @Mock
    private UnifiedJedis jedis;
    private RedisMap redisMap;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        redisMap = new RedisMap(NAME, jedis);
    }

    @org.junit.jupiter.api.Test
    void createMap() {
        assertTrue(redisMap.isEmpty());
        assertNull(redisMap.put(KEY, 123));
        Mockito.verify(jedis).hset(NAME, KEY, "123");
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

    @org.junit.jupiter.api.Test
    void putAll() {
        redisMap.putAll(Map.of(KEY, 123, "key2", 1234));
        Mockito.verify(jedis).hset(NAME, Map.of(KEY, "123", "key2", "1234"));
    }

    @org.junit.jupiter.api.Test
    void clear() {
        Mockito.reset(jedis);
        Mockito.when(jedis.hkeys(NAME)).thenReturn(Set.of(KEY, "key2"));
        redisMap.clear();
        Mockito.verify(jedis).hdel(anyString(), anyString(), anyString());
    }

    @org.junit.jupiter.api.Test
    void values() {
        Mockito.when(jedis.hvals(NAME)).thenReturn(List.of("123", "1234"));
        assertArrayEquals(List.of(123, 1234).toArray(), redisMap.values().toArray());
    }

    @org.junit.jupiter.api.Test
    void entrySet() {
        Mockito.when(jedis.hgetAll(NAME)).thenReturn(Map.of(KEY, "123", "key2", "1234"));
        Set<Map.Entry<String, Integer>> entries = redisMap.entrySet();
        assertEquals(2, entries.size());
        Map<String, Integer> expectedResult = Map.of(KEY, 123, "key2", 1234);
        for (Map.Entry<String, Integer> entry: entries) {
            assertEquals(expectedResult.get(entry.getKey()), entry.getValue());
        }
    }
}