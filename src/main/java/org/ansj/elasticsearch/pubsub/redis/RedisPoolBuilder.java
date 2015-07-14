package org.ansj.elasticsearch.pubsub.redis;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPoolBuilder {
	
	public static ESLogger log = Loggers.getLogger("ansj-redis-pool");
	
	private int maxActive=20;
	private int maxIdle=10;
	private int maxWait=1000;
	private boolean testOnBorrow=true;
	
	private String ipAddress="master.redis.yao.com:6379";
	private int port=6379;
	
	public int getMaxActive() {
		return maxActive;
	}

	public RedisPoolBuilder setMaxActive(int maxActive) {
		this.maxActive = maxActive;
		return this;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public RedisPoolBuilder setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
		return this;
	}
	public int getMaxWait() {
		return maxWait;
	}
	public RedisPoolBuilder setMaxWait(int maxWait) {
		this.maxWait = maxWait;
		return this;
	}
	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}
	public RedisPoolBuilder setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
		return this;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public RedisPoolBuilder setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		return this;
	}
	public int getPort() {
		return port;
	}
	public RedisPoolBuilder setPort(int port) {
		this.port = port;
		return this;
	}
	
	public JedisPool jedisPool(){
		final JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(getMaxActive());
		config.setMaxIdle(getMaxIdle());
		config.setMaxWaitMillis(getMaxWait());
		config.setTestOnBorrow(isTestOnBorrow());
		final String[] ipAndPort = getIpAddress().split(":");
		String ip="";
		int port=0;
		if(ipAndPort.length==1){
			ip=ipAndPort[0];
			port = getPort();
		}else{
			ip=ipAndPort[0];
			port = Integer.valueOf(ipAndPort[1]);
		}
		log.info(ip + ":" + port);
		return new JedisPool(config, ip, port);
	}
}
