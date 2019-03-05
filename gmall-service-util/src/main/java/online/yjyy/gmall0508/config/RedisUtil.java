package online.yjyy.gmall0508.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    // 配置连接池的相应参数，有连接池的对象
    private JedisPool jedisPool;
    // 配置jedisPoolConfig

    /**
     *
     * @param host ip 地址
     * @param port 端口号
     * @param database 指定的哪个库
     */
    public void initJedisPool(String host,int port,int database){
        // 创建JedisPoolConfig
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 总数
        jedisPoolConfig.setMaxTotal(200);
        // 获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);
        // 如果到最大数，设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 在获取连接时，检查是否有效
        jedisPoolConfig.setTestOnBorrow(true);
        //
        jedisPool = new JedisPool(jedisPoolConfig,host,port,20*1000);
    }
    // 创建一个方法，获取Jedis对象
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

}
