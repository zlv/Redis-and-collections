package org.redis_collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RedisSortedSetTest {
    public static final String KEY = "key";
    public static final String NAME = "set";
    @Mock
    private UnifiedJedis jedis;
    private RedisSortedSet redisSet;

    @BeforeEach
    void setUp() {
        redisSet = new RedisSortedSet(NAME, jedis);
    }

    @org.junit.jupiter.api.Test
    void createSet() {
        assertTrue(redisSet.isEmpty());
        Mockito.verify(jedis, times(1)).zrange(NAME, 0, -1);
        assertFalse(redisSet.contains(KEY));
        Mockito.verify(jedis, times(2)).zrange(NAME, 0, -1);
        assertTrue(redisSet.add(KEY));
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of(KEY));
        assertFalse(redisSet.add(KEY));
        Mockito.verify(jedis, times(2)).zadd(NAME, 0., KEY);

        redisSet.remove(KEY);
        Mockito.verify(jedis).zrem(NAME, KEY);
    }

    @Test
    void containsAll() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3"));
        assertFalse(redisSet.containsAll(List.of(KEY, "key2")));
        assertTrue(redisSet.containsAll(List.of("key1", "key2")));
        assertFalse(redisSet.containsAll(List.of(KEY, "key2", "key3")));
        assertTrue(redisSet.containsAll(List.of("key1", "key2", "key3")));
        assertFalse(redisSet.containsAll(List.of(KEY, "key2", "key4")));
        assertFalse(redisSet.containsAll(List.of("key1", "key2", "key4")));
    }

    @Test
    void addAll() {
       redisSet.addAll(List.of("key1", "key2", "key3"));
       Mockito.verify(jedis, times(3)).zadd(anyString(), anyDouble(), anyString());
    }

    @Test
    void retainAll() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        redisSet.retainAll(List.of("key1", "key3", "key4"));
        Mockito.verify(jedis).zrem(NAME, "key2", "key5");
    }

    @Test
    void removeAll() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        redisSet.removeAll(List.of("key1", "key3", "key4"));
        Mockito.verify(jedis).zrem(NAME, "key1", "key3", "key4");
    }

    @Test
    void clear() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        redisSet.clear();
        Mockito.verify(jedis).zrem(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }


    @Test
    void subSet() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        SortedSet<String> subSet = redisSet.subSet("key2", "key4");
        assertTrue(subSet.contains("key2") && subSet.contains("key3") && subSet.contains("key4"));
        assertEquals(3, subSet.size());
    }

    @Test
    void headSet() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        SortedSet<String> subSet = redisSet.headSet("key4");
        assertTrue(subSet.contains("key1") && subSet.contains("key2") && subSet.contains("key3") && subSet.contains("key4"));
        assertEquals(4, subSet.size());
    }

    @Test
    void tailSet() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        SortedSet<String> subSet = redisSet.tailSet("key2");
        assertTrue(subSet.contains("key2") && subSet.contains("key3") && subSet.contains("key4") && subSet.contains("key5"));
        assertEquals(4, subSet.size());
    }

    @Test
    void firstKey() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        assertEquals("key1", redisSet.first());
    }

    @Test
    void lastKey() {
        Mockito.when(jedis.zrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        assertEquals("key5", redisSet.last());
    }
}