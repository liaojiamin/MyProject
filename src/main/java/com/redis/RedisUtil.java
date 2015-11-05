package com.redis;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Response;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

public class RedisUtil {

	public static final Jedis jedis=new Jedis("192.168.170.129");
	public static final List<JedisShardInfo> shards = Arrays.asList(
	            new JedisShardInfo("192.168.170.129",6379),
	            new JedisShardInfo("192.168.170.129",6378));
	
	public ShardedJedisPool getSJP(){
		ShardedJedisPool pool = new ShardedJedisPool(new JedisPoolConfig(), shards);
	    return pool;
	}
	public ShardedJedisPipeline getSJPL(){
		ShardedJedisPool pool=this.getSJP();
		ShardedJedis one=pool.getResource();
		ShardedJedisPipeline pipeline=one.pipelined();
		return pipeline;
	}
	
	public void closePool( ShardedJedisPool pool){
		pool.destroy();
	}
	
	public void setRedis(String key,String value){
		ShardedJedisPool pool=this.getSJP();
		ShardedJedis one=pool.getResource();
		ShardedJedisPipeline pipeline=this.getSJPL();
		pipeline.set(key, value);
		pipeline.syncAndReturnAll();
		pool.returnResource(one);
		this.closePool(pool);
	}
	
	public void delRedis(String key){
		ShardedJedisPool pool=this.getSJP();
		ShardedJedis one=pool.getResource();
		ShardedJedisPipeline pipeline=one.pipelined();
		pipeline.del(key);
		pipeline.syncAndReturnAll();
		pool.returnResource(one);
		this.closePool(pool);
	}
	
	public String getRedis(String key){
		ShardedJedisPool pool=this.getSJP();
		ShardedJedis one=pool.getResource();
		ShardedJedisPipeline pipeline=one.pipelined();
		Response<String> value=pipeline.get(key);
		pipeline.syncAndReturnAll();
		pool.returnResource(one);
		this.closePool(pool);
		return value.get();
	}
	
	
	@Test
	public void setTest(){
		this.setRedis("1", "2");
		this.delRedis("1");
		System.out.println(this.getRedis("1"));
	}
	
	
	
	
}















