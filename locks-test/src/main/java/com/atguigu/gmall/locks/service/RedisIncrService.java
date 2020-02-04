package com.atguigu.gmall.locks.service;

import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 synchronized 和 ReentrantLock
 在单机内好用 但是分布式不行

锁的设置：
    1）自旋：
        自旋次数
        自旋超时
    2）锁设置
        锁的粒度 细 记录级别
            1）各自服务各自锁
            2）分析好粒度 不要锁住无关数据 一种数据一种锁 一条数据一个锁
    3）锁类型
        读写锁

 查询商品详情 进缓存-->击穿 穿透 雪崩
    查商品
 public Product productInfo(String productId){
    Product cache = jedis.get(productId);
    if(cache!=null){
        return cache;
    }else{
        // 各自服务各自锁
        String lock = jedis.set("lock", token, SetParams.setParams().ex(3).nx());
        if(lock!=null){
            //查数据库
            Product product = getFromDb();
            jedis.set(productId,product);
        }else{
            return productInfo(productId); // 自旋
        }
     }
 }


 */


@Service
public class RedisIncrService {
    @Autowired
    StringRedisTemplate redisTemplate;

//    // 方法加锁 多线程 锁得住
//    public synchronized void incr() {
//        // redisTemplate.opsForValue().increment("num");
//        ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
//        String num = stringStringValueOperations.get("num");
//        if (num != null) {
//            Integer i = Integer.parseInt(num);
//            i = i + 1;
//            stringStringValueOperations.set("num", i.toString());
//        }
//    }

//    // 方法不加锁 内容加锁 效果同上
//    public void incr() {
//        synchronized (this) {
//            // redisTemplate.opsForValue().increment("num");
//            ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
//            String num = stringStringValueOperations.get("num");
//            if (num != null) {
//                Integer i = Integer.parseInt(num);
//                i = i + 1;
//                stringStringValueOperations.set("num", i.toString());
//            }
//        }
//    }



    ReentrantLock lock = new ReentrantLock(); // 共用一个lock对象 锁得住
    public void incr() {
        // ReentrantLock lock = new ReentrantLock(); 锁不住
        lock.lock();
        ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
        String num = stringStringValueOperations.get("num");
        if (num != null) {
            Integer i = Integer.parseInt(num);
            i = i + 1;
            stringStringValueOperations.set("num", i.toString());
        }
        lock.unlock();
    }

    /**
     问题：分布式如何使用同一把锁
    1: redis占坑
    1）：先判断没有 2）再给里面放值
    public void hello(){
        String lock = getFromRedis("lock");
        if(lock==null){
            set("lock",1);
            //执行业务
            del("lock")
        }else{
            Thread.sleep(3000);
            hello() // 自旋
        }
    }

     //问题 ：多个用户get("lock")为null的时候 多个用户会同时set("lock",1) 分布式锁就会失效
     解决：redis中 setnx("lock",1) 多用户访问 只能有一个生效 返回1设置成功 返回0设置失败
     2：代码第二阶段：
     public void hello(){
         Integer lock = setnx("lock",1);
        if(lock!=0){
            //执行业务
            del("lock")
        }else{
            // 等待重试
            Thread.sleep(3000);
            hello() // 自旋
        }
     }

     //3问题 ：由于特殊情况（未捕获的异常 断电）导致锁未释放  其他用户永远获取不了锁
     解决：加个过期时间 expire key time(s)
     public void hello(){
        Integer lock = setnx("lock",1);
        if(lock!=0){
            expire("lock",10s);
            // 执行业务逻辑
            // 释放锁
            del("lock")
        }else{
             // 等待重试
             Thread.sleep(3000);
             hello() // 自旋
        }
     }

    // 4问题 刚拿到锁 就爆炸 没来的及设置超时时间
     解决：加锁加超时也必须原子性（同时做） setnxex("lock",10s);
     public void hello(){
         String result = setnxex("lock",1,10s);
        if(result=="ok"){ // 加锁成功
            // 执行业务逻辑
            del("lock") // 释放锁
        }else{
         // 等待重试
            Thread.sleep(3000);
            hello() // 自旋
        }
     }


     // 5问题 如果业务逻辑超时 导致锁自动删除 业务执行完又删除一次 至少会有两个人获取锁
     解决： 只删除自己的锁 不是自己的不删
     public void hello(){
        String token = UUID;
        String result = setnxex("lock",UUID,10s);
        if(result=="ok"){  // 加锁成功
            // 执行业务逻辑
            // 释放自己的锁
            if(get("lock")==token){
                del("lock")
            }
        }else{
            // 等待重试
            Thread.sleep(3000);
            hello() // 自旋
        }
     }

     // 6问题：释放自己的锁的时候get("lock")中 当返回正是自己的token的时候 下一步del还没有执行之前 锁被别人获取 这个时候自己仍然去删除 但是此时删除的锁已经不是自己了 也会导致多个人获取到锁
     解决： Lua脚本 扔给redis执行（redis是单线程的） 实现解锁的原子性
     解锁
     String script =
        "if redis.call('get', KEYS[1]) == ARGV[1] then
            return redis.call('del', KEYS[1])
        else
            return 0
        end";
     jedis.eval(script, Collections.singletonList(key), Collections.singletonList(token));


     1) 分布式锁的核心：（保证原子性）
        1）加锁。占坑一定是原子性的
            避免当多个用户get("lock")为null的时候 多个用户会同时set("lock",1) 分布式锁就会失效 所以必须保证 只有一个用户set成功（占坑成功）
        2）锁要自动超时
        3）解锁也要原子

     最终的分布式代码：
     public void hello(){
        String token = UUID;
        String lock = redis.setnxex("lock","token",10s);
        if(lock=="ok"){
            // 执行业务逻辑
            // 脚本删除锁
        }else{
            hello(); // 自旋
        }
     }

     将其使用AOP

     */
    public void incrDistribute() {
        String token = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.SECONDS );
        if(lock){
            ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
            String num = stringStringValueOperations.get("num");
            if (num != null) {
                Integer i = Integer.parseInt(num)+ 1;
                stringStringValueOperations.set("num", i.toString());
            }

            //删除锁
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> script1 = new DefaultRedisScript<Long>(script);
            script1.setResultType(Long.class);
            List<String> keys = new ArrayList<>();
            keys.add("lock");
            redisTemplate.execute(script1, keys,token);
            System.out.println("删除锁完成2----");
        }else{
            try {
                Thread.sleep(1000);
                incrDistribute(); // 自旋
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @Autowired
    JedisPool jedisPool;


    /**
     * jedis使用
     * 1：导入依赖
     * 2：配置 添加JedisPool到容器
     */
    public void incrDistribute2() {
        Jedis jedis = jedisPool.getResource();
        try {
            String token = UUID.randomUUID().toString();
            String lock = jedis.set("lock", token, SetParams.setParams().ex(3).nx());
            if(lock != null && lock.equalsIgnoreCase("OK")){
                String num = jedis.get("num");
                if (num != null) {
                    jedis.set("num", String.valueOf(Integer.parseInt(num)+ 1));
                }

                //删锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList("lock"), Collections.singletonList(token));
                System.out.println("删除锁3完成----");
            }else{
                try {
                    Thread.sleep(1000);
                    incrDistribute2(); // 自旋
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }finally {
            jedis.close();
        }
    }

    @Autowired
    RedissonClient redisson;

    // use Redission For Lock
    public void incrDistribute4() {
        RLock lock = redisson.getLock("lock");
        try {
            // lock.lock() // 一直等待
            // 感知别人删锁  发布订阅模式（实施感知）  不会自旋占用资源 但是仍然会阻塞 等待redis的解锁通知
            // lock监听redis redis一旦删锁 赶紧尝试加锁
            // 每个线程的锁一定是这个线程解

            // lock.lock() //默认是阻塞的
            // lock.tryLock() // 是非阻塞的 尝试一次 不行就算了
            // lock.tryLock(100,10,TimeUnit=SECONDS); // 尝试一次 等待100s 不行就算了 成功的话锁的有效期是10s
            lock.lock();
            ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
            String num = stringStringValueOperations.get("num");
            if (num != null) {
                Integer i = Integer.parseInt(num) + 1;
                stringStringValueOperations.set("num", i.toString());
            }
        } finally {
            lock.unlock(); //redisson解锁具有原子性
        }
    }


    /**
     * 写锁是个排他锁（独占锁）
     * 读锁是一个共享锁
     *
     * 有写锁 写锁以后的读都不可以 只有写锁释放才能读
     */
    private String hello = "hello";
    public String read() {
        RReadWriteLock helloValue = redisson.getReadWriteLock("helloValue");
        RLock rLock = helloValue.readLock();
        rLock.lock();
        String a = hello;
        rLock.unlock();
        return a;
    }

    public String write() throws InterruptedException {
        RReadWriteLock helloValue = redisson.getReadWriteLock("helloValue");
        RLock rLock = helloValue.writeLock();
        rLock.lock();
        Thread.sleep(3000);
        String a = UUID.randomUUID().toString();
        rLock.unlock();
        return a;
    }

    /**
     * 错误的场景
     * 1、两个服务及俩个服务以上操作相同的数据 如果涉及到读写
     *      独加读锁 写加写锁
     *
     * 对于一个服务内读写  直接一个synchronized搞定
     */
    public void unlock(){
        RReadWriteLock helloValue = redisson.getReadWriteLock("helloValue");
        RLock rLock = helloValue.readLock();
        RLock rLock1 = helloValue.writeLock();

        //修改一号记录
        rLock1.lock();
        // 修改

        //读操作
        rLock.lock();


    }

/**
 * 缓存
 *问题：
 * 	查询频率高，数据变化率不是太快的 我们进缓存 缓存数据库同步----
 *
 *
 * # 我们读取数据的时候：
 *
 * If(readLock){
 * 	Data data = readFromCache();
 * 	If(data==null){
 * 		data = readFromDb();
 * 		setToCache(data);
 * }
 * }
 *
 * # 写数据的时候：
 * If(writeLock){
 * 	    Data data = xxxx;
 * 	    updateDataToDb(data);
 *  	setToCache(data);
 * }
 *
 * 双写+读写锁 保证一致性
 * 下单 查价格 需要看最新的价格 我们使用读锁
 * 其他的（商品详情页）查看价格 直接去缓存获取 不需要使用读锁 即使数据不是最新的也无所谓
 */
}
