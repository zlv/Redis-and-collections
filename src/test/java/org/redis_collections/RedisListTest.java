package org.redis_collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisListTest {

    public static final String KEY = "key";
    public static final String NAME = "list";
    @Mock
    private UnifiedJedis jedis;
    private RedisList redisList;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        redisList = new RedisList(NAME, jedis);
    }

    @org.junit.jupiter.api.Test
    void createList() {
        assertTrue(redisList.isEmpty());
        Mockito.verify(jedis).llen(NAME);
        assertFalse(redisList.contains(KEY));
        Mockito.verify(jedis).lrange(NAME, 0, -1);
        assertTrue(redisList.add(KEY));
        Mockito.verify(jedis).rpush(NAME, KEY);

        redisList.remove(KEY);
        Mockito.verify(jedis).lrem(NAME, 0, KEY);
    }

    @Test
    void contains() {
        Mockito.when(jedis.lrange(NAME, 0, -1)).thenReturn(List.of(KEY));
        assertTrue(redisList.contains(KEY));
    }

    @Test
    void containsAll() {
        Mockito.when(jedis.lrange(NAME, 0, -1)).thenReturn(List.of(KEY, "key2"));
        assertTrue(redisList.containsAll(List.of(KEY, "key2")));
        assertTrue(redisList.contains(KEY));
        assertFalse(redisList.containsAll(List.of(KEY, "key3", "key2")));
    }

    @Test
    void retainAll() {
        Mockito.when(jedis.lrange(NAME, 0, -1)).thenReturn(List.of("key1", "key2", "key3", "key4", "key5"));
        redisList.retainAll(List.of("key1", "key3", "key4"));
        Mockito.verify(jedis).lrem(NAME, 0, "key2");
        Mockito.verify(jedis).lrem(NAME, 0, "key5");
    }

    @Test
    void removeAll() {
        redisList.removeAll(List.of("key1", "key3", "key4"));
        Mockito.verify(jedis).lrem(NAME, 0, "key1");
        Mockito.verify(jedis).lrem(NAME, 0, "key3");
        Mockito.verify(jedis).lrem(NAME, 0, "key4");
    }

    @Test
    void set() {
        redisList.set(0, "key1");
        Mockito.verify(jedis).lpop(NAME);
        Mockito.verify(jedis).lpush(NAME, "key1");
        Mockito.when(jedis.llen(NAME)).thenReturn(2L);
        redisList.set(1, "key2");
        Mockito.verify(jedis).rpop(NAME);
        Mockito.verify(jedis).rpush(NAME, "key2");
    }

    @Test
    void add() {
        redisList.set(0, "key1");
        Mockito.verify(jedis).lpush(NAME, "key1");
        Mockito.when(jedis.llen(NAME)).thenReturn(2L);
        redisList.set(1, "key2");
        Mockito.verify(jedis).rpush(NAME, "key2");
    }

    @Test
    void remove() {
        Mockito.when(jedis.llen(NAME)).thenReturn(1L);
        Mockito.when(jedis.lrange(NAME, 0, 0)).thenReturn(List.of(KEY));
        redisList.removeFirst();
        Mockito.verify(jedis).lset(NAME, 0, "*DELETED*");
        Mockito.verify(jedis).lrem(NAME, 1, "*DELETED*");
    }
}