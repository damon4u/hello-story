package com.damon4u.story.cache;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.util.*;

public class JedisTemplate {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String SCRIPT_HSET_KEY_EXIST = "local ex = redis.call('exists',KEYS[1])\n" +
            "local res = -1\n" +
            "if ex == 1 then\n" +
            "	res = redis.call('hset',KEYS[1],ARGV[1],ARGV[2])\n" +
            "end\n" +
            "return res";
	
	private Pool<Jedis> jedisPool;

	public void setJedisPool(Pool<Jedis> jedisPool) {
		this.jedisPool = jedisPool;
	}

	public <T> T execute(JedisAction<T> jedisAction) throws JedisException{
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			return jedisAction.action(jedis);
		} catch (JedisConnectionException e) {
			logger.error(e.getMessage(), e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}
	
	public void execute(JedisActionNoResult jedisActionNoResult) throws JedisException{
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			jedisActionNoResult.action(jedis);
		} catch (JedisConnectionException e) {
			logger.error(e.getMessage(), e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}
	
	private void closeResource(Jedis jedis, boolean connectionBroken) {
		if (jedis != null) {
			if (connectionBroken) {
				jedisPool.returnBrokenResource(jedis);
			} else {
				jedisPool.returnResource(jedis);
			}
		}
	}

	public static interface JedisAction<T>{
		T action(Jedis jedis);
	}
	
	public static interface JedisActionNoResult{
		void action(Jedis jedis);
	}

	/************************ String *************************/

    /**
     * Get the value of the specified key.
     * If the key does not exist null is returned.
     * If the value stored at key is not a string an error is returned because GET can only handle string values.
     *
     * @param key key
     * @return value
     */
    public String get(final String key){
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				return jedis.get(key);
			}
			
		});
	}

	public String set(final String key, final String value){
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				return jedis.set(key, value);
			}
			
		});
	}
	
	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
	 * @param key
	 * @param value
	 * @return key不存在 或  key存在但不是字符串类型时，返回null
	 */
	public String getSet(final String key, final String value) {
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				return jedis.getSet(key, value);
			}
			
		});
	}

	public String setex(final String key, final int seconds, final String val){
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				return jedis.setex(key, seconds, val);
			}
			
		});
	}

	public byte[] get(final byte[] key){
		return execute(new JedisAction<byte[]>(){

			@Override
			public byte[] action(Jedis jedis) {
				return jedis.get(key);
			}

		});
	}

	public String set(final byte[] key, final byte[] value, final int seconds){
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				return jedis.setex(key, seconds, value);
			}

		});
	}

    public List<String> setListString(final List<String> keys, final List<String> values, final int seconds){
		if(keys.size() != values.size() || keys.isEmpty()) {
			return Lists.newArrayList();
		}
		return execute(new JedisAction<List<String>>(){

            @Override
            public List<String> action(Jedis jedis) {
                int keySize = keys.size();
                List<Response<String>> responses = Lists.newArrayListWithCapacity(keySize);
                List<String> result = Lists.newArrayListWithCapacity(keySize);
                Pipeline pipelined = jedis.pipelined();
                for(int i = 0; i < keySize; ++i) {
                    responses.add(pipelined.setex(keys.get(i), seconds, values.get(i)));
                }
                pipelined.sync();
                for (Response<String> response : responses) {
                    result.add(response.get());
                }
                return result;
            }

        });
    }

    public List<String> setList(final List<byte[]> keys, final List<byte[]> values, final int seconds){
        if(keys.size() != values.size() || keys.isEmpty()) {
            return Lists.newArrayList();
        }
        return execute(new JedisAction<List<String>>(){

            @Override
            public List<String> action(Jedis jedis) {
                int keySize = keys.size();
                List<Response<String>> responses = Lists.newArrayListWithCapacity(keySize);
                List<String> result = Lists.newArrayListWithCapacity(keySize);
                Pipeline pipelined = jedis.pipelined();
                for(int i = 0; i < keySize; ++i) {
                    responses.add(pipelined.setex(keys.get(i), seconds, values.get(i)));
                }
                pipelined.sync();
                for (Response<String> response : responses) {
                    result.add(response.get());
                }
                return result;
            }

        });
    }

    public List<String> getListString(final List<String> keys){
        if (CollectionUtils.isEmpty(keys)) {
            return Lists.newArrayList();
        }

        return execute(new JedisAction<List<String>>(){

            @Override
            public List<String> action(Jedis jedis) {
                String[] keysArray = new String[keys.size()];
                keysArray = keys.toArray(keysArray);
                return jedis.mget(keysArray);
            }

        });
    }

    public List<byte[]> mget(final List<byte[]> keys){
        if (CollectionUtils.isEmpty(keys)) {
            return Lists.newArrayList();
        }
        return execute(new JedisAction<List<byte[]>>(){

            @Override
            public List<byte[]> action(Jedis jedis) {
                byte[][] keysArray = new byte[keys.size()][];
                keysArray = keys.toArray(keysArray);
                return jedis.mget(keysArray);
            }

        });
    }

	/**
	 * like ++i
	 * @param key
	 * @return value after increment, or 1 if key not exist
	 */
	public Long incr(final String key){
		return execute(new JedisAction<Long>(){

			@Override
			public Long action(Jedis jedis) {
				return jedis.incr(key);
			}
			
		});
	}

	public Long incrBy(final String key, final long integer){
		return execute(new JedisAction<Long>(){

			@Override
			public Long action(Jedis jedis) {
				return jedis.incrBy(key, integer);
			}

		});
	}

	public String setnx(final String key, final int seconds, final String val){
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				Long result = jedis.setnx(key, val);
				if (seconds > 0) {
					jedis.expire(key, seconds);
				}
				return result.toString();
			}
			
		});
	}

	public Long del(final String key){
		return execute(new JedisAction<Long>(){

			@Override
			public Long action(Jedis jedis) {
				return jedis.del(key);
			}
			
		});
	}

	public Long del(final byte[] key){
		return execute(new JedisAction<Long>(){

			@Override
			public Long action(Jedis jedis) {
				return jedis.del(key);
			}

		});
	}

	public Boolean exists(final String key){
		return execute(new JedisAction<Boolean>(){

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.exists(key);
			}
			
		});
	}

    public Boolean exists(final byte[] key){
        return execute(new JedisAction<Boolean>(){

            @Override
            public Boolean action(Jedis jedis) {
                return jedis.exists(key);
            }

        });
    }

	public Long expire(final String key, final int seconds) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.expire(key, seconds);
			}

		});
	}

	/*************************** Hash ******************************/

    public Long hset(final String key, final String field, final String value){
		return execute(new JedisAction<Long>(){

			@Override
			public Long action(Jedis jedis) {
				return jedis.hset(key, field, value);
			}

		});
	}

	public Long hset(final byte[] key, final byte[] field, final byte[] value){
		return execute(new JedisAction<Long>(){

			@Override
			public Long action(Jedis jedis) {
				return jedis.hset(key, field, value);
			}

		});
	}

	public String hget(final String key, final String field){
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				return jedis.hget(key, field);
			}

		});
	}

	public byte[] hget(final byte[] key, final byte[] field){
		return execute(new JedisAction<byte[]>(){

			@Override
			public byte[] action(Jedis jedis) {
				return jedis.hget(key, field);
			}

		});
	}

	public Map<String,String> hgetAll(final String key){
		return execute(new JedisAction<Map<String,String>>(){

			@Override
			public Map<String,String> action(Jedis jedis) {
				return jedis.hgetAll(key);
			}
			
		});
	}

	public Map<byte[],byte[]> hgetAll(final byte[] key){
		return execute(new JedisAction<Map<byte[],byte[]>>(){

			@Override
			public Map<byte[],byte[]> action(Jedis jedis) {
				return jedis.hgetAll(key);
			}

		});
	}

	public boolean hexists(final String key, final String field){
	    return execute(new JedisAction<Boolean>(){

            @Override
            public Boolean action(Jedis jedis) {
                return jedis.hexists(key, field);
            }
	        
	    });
	}
	
	public String hmSet(final byte[] key, final Map<byte[],byte[]> values){
	    return execute(new JedisAction<String>(){

            @Override
            public String action(Jedis jedis) {
                return jedis.hmset(key, values);
            }
	        
	    });
	}

	public String hmSet(final String key, final Map<String,String> values){
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				return jedis.hmset(key, values);
			}

		});
	}

	public String hmSetEx(final String key, final int seconds, final Map<String,String> values){
		return execute(new JedisAction<String>(){

			@Override
			public String action(Jedis jedis) {
				String hmSetResult = jedis.hmset(key, values);
				jedis.expire(key, seconds);
				return hmSetResult;
			}

		});
	}

    public List<String> hmSetListString(final List<String> keys, final List<Map<String,String>> values){
        if(keys.size() != values.size() || keys.isEmpty()) {
            return Lists.newArrayList();
        }
        return execute(new JedisAction<List<String>>(){

            @Override
            public List<String> action(Jedis jedis) {
                int keySize = keys.size();
                List<Response<String>> responses = Lists.newArrayListWithCapacity(keySize);
                List<String> result = Lists.newArrayListWithCapacity(keySize);
                Pipeline pipelined = jedis.pipelined();
                for(int i = 0; i < keySize; ++i) {
                    responses.add(pipelined.hmset(keys.get(i), values.get(i)));
                }
                pipelined.sync();
                for (Response<String> response : responses) {
                    result.add(response.get());
                }
                return result;
            }

        });
    }

	public List<String> hmSetListStringEx(final List<String> keys, final int seconds, final List<Map<String,String>> values){
		if(keys.size() != values.size() || keys.isEmpty()) {
			return Lists.newArrayList();
		}
		return execute(new JedisAction<List<String>>(){

			@Override
			public List<String> action(Jedis jedis) {
				int keySize = keys.size();
				List<Response<String>> responses = Lists.newArrayListWithCapacity(keySize);
				List<String> result = Lists.newArrayListWithCapacity(keySize);
				Pipeline pipelined = jedis.pipelined();
				for(int i = 0; i < keySize; ++i) {
					responses.add(pipelined.hmset(keys.get(i), values.get(i)));
					pipelined.expire(keys.get(i), seconds);
				}
				pipelined.sync();
				for (Response<String> response : responses) {
					result.add(response.get());
				}
				return result;
			}

		});
	}

    public List<Map<String,String>> hgetAllList(final List<String> keys){
        if (CollectionUtils.isEmpty(keys)) {
            return Lists.newArrayList();
        }
        return execute(new JedisAction<List<Map<String,String>>>(){

            @Override
            public List<Map<String,String>> action(Jedis jedis) {
                int keySize = keys.size();
                List<Response<Map<String,String>>> responses = Lists.newArrayListWithCapacity(keySize);
                List<Map<String,String>> result = Lists.newArrayListWithCapacity(keySize);
                Pipeline pipelined = jedis.pipelined();
                for(int i = 0; i < keySize; ++i) {
                    responses.add(pipelined.hgetAll(keys.get(i)));
                }
                pipelined.sync();
                for (Response<Map<String, String>> response : responses) {
                    result.add(response.get());
                }
                return result;
            }

        });
    }

	public Long hdel(final String key, final String field){
		return execute(new JedisAction<Long>(){

			@Override
			public Long action(Jedis jedis) {
				return jedis.hdel(key, field);
			}

		});
	}

	public Long hdel(final byte[] key, final byte[] field){
		return execute(new JedisAction<Long>(){

			@Override
			public Long action(Jedis jedis) {
				return jedis.hdel(key, field);
			}

		});
	}

    public Long hincBy(final String key, final String field, final long value){
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.hincrBy(key, field, value);
			}
		});
	}

    /**
     *  Return the number of items in a hash.
     *
     * @param key key
     * @return The number of entries (fields) contained in the hash stored at key. If the specified
     *         key does not exist, 0 is returned assuming an empty hash.
     */
    public Long hlen(final String key){
        return execute(new JedisAction<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.hlen(key);
            }
        });
    }

	public Integer hsetKeyExist(final String key, final String field, final String value){
		return execute(new JedisAction<Integer>() {
			@Override
			public Integer action(Jedis jedis) {
				List<String> keys = new ArrayList<String>(1);
				keys.add(key);
				List<String> args = new ArrayList<>(2);
				args.add(field);
				args.add(value);
				Object o = jedis.eval(SCRIPT_HSET_KEY_EXIST, keys, args);
				if(o != null){
					return Integer.valueOf(o.toString());
				}
				return -1;
			}
		});
	}

	/************************** Set **************************/

	public Set<String> smembers(final String key){
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.smembers(key);
			}
		});
	}

	public Long scard(final String key){
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.scard(key);
			}

		});
	}

	public Long sadd(final String key, final String... members) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.sadd(key, members);
			}
		});
	}

	public Long srem(final String key, final String... members){
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.srem(key, members);
			}
		});
	}

	public Boolean sisMember(final String key, final String member){
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.sismember(key, member);
			}
		});
	}

    public String spop(final String key){
        return execute(new JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.spop(key);
            }
        });
    }

	public String srandmember(final String key){
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.srandmember(key);
			}
		});
	}

	/************************** List **************************/

	public Long rpush(final String key, final String... vals) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.rpush(key, vals);
			}
		});
	}

	public Long llen(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.llen(key);
			}
		});
	}

	public Long lrem(final String key, final long count, final String value) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.lrem(key, count, value);
			}
		});
	}

	public String lpop(final String key) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.lpop(key);
			}
		});
	}

	public List<String> lrange(final String key, final long start, final long end) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.lrange(key, start, end);
			}
		});
	}

	/************************** Sorted Set **************************/

	public Double zincrby(final String key, final double score, final String member) {
		return execute(new JedisAction<Double>() {
			@Override
			public Double action(Jedis jedis) {
				return jedis.zincrby(key, score, member);
			}
		});
	}

	public Long zadd(final String key, final double score, final String member) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.zadd(key, score, member);
			}
		});
	}

	public Set<String> zrange(final String key, final long start, final long end) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.zrange(key, start, end);
			}
		});
	}

	public Set<String> zrevrange(final String key, final long start, final long end) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.zrevrange(key, start, end);
			}
		});
	}

	public Set<String> zrevrangeByScore(final String key, final long min, final long max) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}
		});
	}

	public Double zscore(final String key, final String member) {
		return execute(new JedisAction<Double>() {
			@Override
			public Double action(Jedis jedis) {
				return jedis.zscore(key, member);
			}
		});
	}
	
	public Long zcard(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.zcard(key);
			}
		});
	}

	public Long zrem(final String key, final String member) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.zrem(key, member);
			}
		});
	}

	public Long zrank(final String key, final String member) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.zrank(key, member);
			}
		});
	}

	/**
	 * 分批删除大sorted set
	 * @param key key
	 * @param count 一次删除多少个元素
	 * @return 删除总数
	 */
	public Long deleteLargeZSet(final String key, final int count) {

		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				long totalDelete = 0L;
				while (zcard(key) > 0) {
					totalDelete += jedis.zremrangeByRank(key, 0, count - 1);
				}
				logger.info("deleteLargeZSet {}, totalDelete: {}", key, totalDelete);
				del(key);
				return totalDelete;
			}
		});
	}

}
