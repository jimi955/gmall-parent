package com.atguigu.gmall.portal.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置当前系统的线程池信息
 */
@Configuration
public class ThreadPoolConfig {
    @Value("${gmall.pool.coreSize}")
    private Integer coreSize;
    @Value("${gmall.pool.maximumPoolSize}")
    private Integer maximumPoolSize;
    @Value("${gmall.pool.queueSize}")
    private Integer queueSize;


    /**
     * 核心业务线程池
     * @param poolProperties
     * @return
     */
    @Bean("mainThreadPoolExecutor")
    public ThreadPoolExecutor mainThreadPoolExecutor(PoolProperties poolProperties){
        /**
         *   public ThreadPoolExecutor(int corePoolSize,
         *                               int maximumPoolSize,
         *                               long keepAliveTime,
         *                               TimeUnit unit,
         *                               BlockingQueue<Runnable> workQueue,
         *                               RejectedExecutionHandler handler) {
         */
        LinkedBlockingDeque<Runnable> deque = new LinkedBlockingDeque<>(poolProperties.getQueueSize());

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolProperties.getCoreSize(),
                poolProperties.getMaximumPoolSize(), 10,
                TimeUnit.MINUTES, deque);

        return threadPoolExecutor;
    }

    /**
     * 非核心业务线程池
     * @param poolProperties
     * @return
     */
    @Bean("otherThreadPoolExecutor")
    public ThreadPoolExecutor otherThreadPoolExecutor(PoolProperties poolProperties){
        LinkedBlockingDeque<Runnable> deque = new LinkedBlockingDeque<>(poolProperties.getQueueSize());

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolProperties.getCoreSize(),
                poolProperties.getMaximumPoolSize(), 10,
                TimeUnit.MINUTES, deque);

        return threadPoolExecutor;
    }


}
