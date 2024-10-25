package org.redis_collections;

import redis.clients.jedis.HostAndPort;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

public class RedisQueue extends RedisList implements Queue<String> {
    public RedisQueue(String name, Set<HostAndPort> nodes, String password) {
        super(name, nodes, password);
    }

    @Override
    public boolean offer(String s) {
        return add(s);
    }

    @Override
    public String remove() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return jedis.lpop(name);
    }

    @Override
    public String poll() {
        if (size() == 0) {
            return null;
        }
        return jedis.lpop(name);
    }

    @Override
    public String element() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return get(0);
    }

    @Override
    public String peek() {
        if (size() == 0) {
            return null;
        }
        return get(0);
    }
}
