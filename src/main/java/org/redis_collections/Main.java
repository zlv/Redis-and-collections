package org.redis_collections;


import redis.clients.jedis.HostAndPort;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {
        FileInputStream propsInput = new FileInputStream("src/main/resources/app.properties");

        Properties prop = new Properties();

        prop.load(propsInput);

        Set<HostAndPort> nodes = Stream.of(((String) prop.get("nodes")).split(",")).map(String::strip).map(node -> node.split(":")).map(node -> new HostAndPort(node[0], Integer.parseInt(node[1]))).collect(Collectors.toSet());

        RedisMap redisMap = RedisMap.createMap("map", nodes, prop);
        redisMap.put("key4", 123);

        Set<Map.Entry<String, Integer>> m = redisMap.entrySet();
        redisMap.put("key24", 123);
        redisMap.put("key34", 123);
        boolean c = redisMap.containsKey("key23");
        boolean c2 = redisMap.containsKey("key24");
        boolean c3 = redisMap.containsValue(123);
        boolean c4 = redisMap.containsValue(12345);
        Integer elem = redisMap.get("key24");
        System.out.println(m + " " + c + " " + c2 + " " + c3 + " " + c4 + " " + elem);
        redisMap.put("key24", 1234);
        redisMap.remove("key23");
        redisMap.remove("key24");
        Integer elem2 = redisMap.get("key24");
        Set<Map.Entry<String, Integer>> elems = redisMap.entrySet();
        System.out.println(elems + " " + elem2);

        String password = (String) prop.get("password");
        RedisSortedMap redisSortedMap = new RedisSortedMap("smap", nodes, password);
        redisSortedMap.put("key4", 123);

        Set<Map.Entry<String, Integer>> ms = redisSortedMap.entrySet();
        redisSortedMap.put("key24", 123);
        redisSortedMap.put("key34", 123);
        Integer elem3 = redisSortedMap.get("key24");
        redisSortedMap.put("key24", 1234);
        Integer elem4 = redisSortedMap.get("key24");
        Set<Map.Entry<String, Integer>> elems2 = redisSortedMap.entrySet();
        SortedMap<String, Integer> elems3 = redisSortedMap.subMap("key34", "key4");
        System.out.println(ms + " " + elem3 + " " + elem4 + " " + elems2 + " " + elems3);

        RedisSet redisSet = RedisSet.createSet("set2", nodes, prop);
        redisSet.add("key34");
        redisSet.add("key24");
        redisSet.add("key14");
        redisSet.add("key54");
        Object[] a = redisSet.toArray();
        List<String> strings = new ArrayList<>(redisSet);
        System.out.println(Arrays.toString(a) + " " + strings);

        RedisSortedSet redisSortedSet = new RedisSortedSet("sset1", nodes, password);
        redisSortedSet.add("key24");
        redisSortedSet.add("key14");
        redisSortedSet.add("key54");
        Object[] as = redisSortedSet.toArray();
        List<String> sstrings = new ArrayList<>(redisSortedSet);
        System.out.println(Arrays.toString(as) + " " + sstrings);

        RedisQueue redisQueue = new RedisQueue("q", nodes, password);
        redisQueue.add("key1");
        redisQueue.add("key2");
        String t = redisQueue.get(1);
        String t1 = redisQueue.poll();
        String t2 = redisQueue.peek();
        String t4 = redisQueue.poll();
        String t5 = redisQueue.poll();
        System.out.println(t + " " + t1 + " " + t2 + " " + t4 + " " + t5);

        RedisDeque redisDeque = new RedisDeque("deq", nodes, password);
        redisDeque.add("key1");
        redisDeque.add("key2");
        String t6 = redisDeque.getFirst();
        String t7 = redisDeque.poll();
        String t8 = redisDeque.peek();
        String t9 = redisDeque.peekFirst();
        String t10 = redisDeque.peekLast();
        System.out.println(t6 + " " + t7 + " " + t8 + " " + t9 + " " + t10);

        RedisList redisList = new RedisList("list", nodes, password);
        redisList.add("key1");
        redisList.add("key1");
        redisList.add("key2");
        int sz = redisList.size();
        redisList.remove(2);
        String[] arr = redisList.toArray(new String[1]);
        System.out.println(sz + " " + Arrays.toString(arr));
    }
}