package com.redis;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Transaction;

public class SameTamp {
	public static final Jedis jedis=new Jedis("192.168.170.129");
	public static final List<JedisShardInfo> shards = Arrays.asList(
	            new JedisShardInfo("192.168.170.129",6379),
	            new JedisShardInfo("192.168.170.129",6378));
	public static final String url="192.168.170.129";
	public static final String portone="6379";
	public static final String porttwo="6378";
	@Test
	//同步方式
	public void testInsert(){
		Long start=System.currentTimeMillis();
		for(int i=0;i<1000;i++){
			String redult=jedis.set("n"+i, "n"+i);
		}
		long end =System.currentTimeMillis();
		System.out.println("Simple SET: " + ((end - start)/1000.0) + " seconds");
		jedis.disconnect();
	}
	
	@Test
	//事务方式(Transactions)
	public void testTra(){
		long start=System.currentTimeMillis();
		Transaction tx=jedis.multi();
		for(int i=0;i<1000;i++){
			tx.set("t"+i, "t"+i);
		}
		List<Object> results=tx.exec();
		long end =System.currentTimeMillis();
		System.out.println("Transaction set: "+((end-start)/1000)+"seconds");
		jedis.disconnect();
	}
	
	@Test
	//管道(Pipelining)
	public void testPip(){
		Pipeline pipeline=jedis.pipelined();
		long start = System.currentTimeMillis();
		for(int i=0;i<1000;i++){
			pipeline.set("p"+i, "p"+i);
		}
		List<Object> results=pipeline.syncAndReturnAll();
		long end =System.currentTimeMillis();
		System.out.println("Pipelineed set: "+((end-start)/1000)+"seconds");
		jedis.disconnect();
	}
	
	
//	管道中调用事务
	@Test
	public void test4combPipelineTrans() {
	    long start = System.currentTimeMillis();
	    Pipeline pipeline = jedis.pipelined();
	    pipeline.multi();
	    for (int i = 0; i < 1000; i++) {
	        pipeline.set("" + i, "" + i);
	    }
	    pipeline.exec();
	    List<Object> results = pipeline.syncAndReturnAll();
	    long end = System.currentTimeMillis();
	    System.out.println("Pipelined transaction: " + ((end - start)/1000.0) + " seconds");
	    jedis.disconnect();
	}
	
	
	@Test
//	分布式直连同步调用
	public void test5shardNormal() {
	    ShardedJedis sharding = new ShardedJedis(shards);
	 
	    long start = System.currentTimeMillis();
	    for (int i = 0; i < 1000; i++) {
	        String result = sharding.set("sn" + i, "n" + i);
	    }
	    long end = System.currentTimeMillis();
	    System.out.println("Simple@Sharing SET: " + ((end - start)/1000.0) + " seconds");
	 
	    sharding.disconnect();
	}
	
	
//	分布式直连异步调用
	@Test
	public void test6shardpipelined() {
		 ShardedJedis sharding=new ShardedJedis(shards);
		 ShardedJedisPipeline pipeline=sharding.pipelined();
		 Long start=System.currentTimeMillis();
		 for(int i=0;i<1000;i++){
			 pipeline.set("sp"+i, "p"+i);
		 }
		 List<Object> result=pipeline.syncAndReturnAll();
		 long end=System.currentTimeMillis();
		 System.out.println("Pipelined@Sharing SET: " + ((end - start)/1000.0) + " seconds");
		 
		 sharding.disconnect();
	}
	
//	分布式连接池同步调用
	@Test
	public void test7shardSimplePool() {
	 
	    ShardedJedisPool pool = new ShardedJedisPool(new JedisPoolConfig(), shards);
	 
	    ShardedJedis one = pool.getResource();
	 
	    long start = System.currentTimeMillis();
	    for (int i = 0; i < 1000; i++) {
	        String result = one.set("spn" + i, "n" + i);
	    }
	    long end = System.currentTimeMillis();
	    pool.returnResource(one);
	    System.out.println("Simple@Pool SET: " + ((end - start)/1000.0) + " seconds");
	 
	    pool.destroy();
	}
	
//	分布式连接池异步调用
	@Test
	public void test8shardPipelinedPool() {
	 
	    ShardedJedisPool pool = new ShardedJedisPool(new JedisPoolConfig(), shards);
	 
	    ShardedJedis one = pool.getResource();
	 
	    ShardedJedisPipeline pipeline = one.pipelined();
	 
	    long start = System.currentTimeMillis();
	    for (int i = 0; i < 1000; i++) {
	        pipeline.set("sppn" + i, "n" + i);
	    }
	    List<Object> results = pipeline.syncAndReturnAll();
	    long end = System.currentTimeMillis();
	    pool.returnResource(one);
	    System.out.println("Pipelined@Pool SET: " + ((end - start)/1000.0) + " seconds");
	    pool.destroy();
	}
}












