package org.redis_collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class RedisSetTest {
    public static final String KEY = "key";
    public static final String NAME = "set";
    @Mock
    private UnifiedJedis jedis;
    private RedisSet redisSet;

    @BeforeEach
    void setUp() {
        redisSet = new RedisSet(NAME, jedis);
    }

    @org.junit.jupiter.api.Test
    void createSet() {
        assertTrue(redisSet.isEmpty());
        Mockito.verify(jedis).smembers(NAME);
        assertFalse(redisSet.contains(KEY));
        Mockito.verify(jedis).sismember(NAME, KEY);
        assertTrue(redisSet.add(KEY));
        Mockito.when(jedis.sismember(NAME, KEY)).thenReturn(true);
        assertFalse(redisSet.add(KEY));
        Mockito.verify(jedis, Mockito.times(2)).sadd(NAME, KEY);

        redisSet.remove(KEY);
        Mockito.verify(jedis).srem(NAME, KEY);
    }

    @Test
    void containsAll() {
        Mockito.when(jedis.sismember(NAME, KEY)).thenReturn(true);
        Mockito.when(jedis.sismember(NAME, "key2")).thenReturn(true);
        Mockito.when(jedis.sismember(NAME, "key3")).thenReturn(true);
        assertTrue(redisSet.containsAll(List.of(KEY, "key2")));
        assertTrue(redisSet.containsAll(List.of(KEY, "key2", "key3")));
        assertFalse(redisSet.containsAll(List.of(KEY, "key2", "key4")));
    }

    @Test
    void retainAll() {
        Mockito.when(jedis.smembers(NAME)).thenReturn(Set.of("key1", "key2", "key3", "key4", "key5"));
        redisSet.retainAll(List.of("key1", "key3", "key4"));
        Mockito.verify(jedis).srem(NAME, "key2", "key5");
    }

    @Test
    void removeAll() {
        Mockito.when(jedis.smembers(NAME)).thenReturn(Set.of("key1", "key2", "key3", "key4", "key5"));
        redisSet.removeAll(List.of("key1", "key3", "key4"));
        Mockito.verify(jedis).srem(NAME, "key1", "key3", "key4");
    }

    @Test
    void clear() {
        Mockito.when(jedis.smembers(NAME)).thenReturn(Set.of("key1", "key2", "key3", "key4", "key5"));
        redisSet.clear();
        Mockito.verify(jedis).srem(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
}