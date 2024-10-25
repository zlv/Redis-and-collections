package org.redis_collections;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Connection;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;

import java.util.Set;

public class RedisCollection {
    protected final String name;
    protected final UnifiedJedis jedis;

    public RedisCollection(String name, Set<HostAndPort> nodes, String password) {
        this.name = name;
        GenericObjectPoolConfig<Connection> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10000);
        config.setMaxIdle(500);
        if (password != null) {
            jedis = new JedisCluster(nodes, 1000, 1000, 1000, password, config);
        } else {
            jedis = new JedisCluster(nodes, 1000, 1000, 1000, null, config);
        }
    }

    public RedisCollection(String name, UnifiedJedis jedis) {
        this.name = name;
        this.jedis = jedis;
    }

    public RedisCollection(String name, HostAndPort node, String password) {
        this.name = name;
        jedis = new JedisPooled(node, DefaultJedisClientConfig.builder().password(password).build());
    }

}
