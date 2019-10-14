/**
 * 
 */
package com.talkweb.commondata.dao.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.commondata.dao.RedisOperationDAO;

/**
 * @ClassName: RedisOperationDAOImpl
 * @version:1.0
 * @Description: redis操作接口
 * @author 廖刚 ---智慧校
 * @date 2015年5月12日
 */
@Repository(value="redisOperationDAOSDRCacheDataImpl")
public class RedisOperationDAOSDRCacheDataImpl implements RedisOperationDAO {
	
	@Resource(name="redisSentinelCacheDataTemplate")
	private RedisTemplate redisTemplate;
	
	@Override
	public Object get(Object key) throws Exception {
		return redisTemplate.opsForValue().get(key);
	}

	@Override
	public Long increment(Object key, long delta) throws Exception {
		return redisTemplate.opsForValue().increment(key, delta);
	}
	
	@Override
	public Boolean hasKey(Object key) throws Exception {
		return redisTemplate.hasKey(key);
	}
	
	@Override
	public void set(Object key, Object Value, long expireTime,
			TimeUnit timeUnit) throws Exception {
		redisTemplate.opsForValue().set(key, Value, expireTime, timeUnit);
	}
	
	@Override
	public void set(Object key, Object Value,long expireTime) throws Exception {
		redisTemplate.opsForValue().set(key, Value, expireTime, TimeUnit.SECONDS);
	}
	
	@Override
	public void set(Object key, Object Value) throws Exception {
		redisTemplate.opsForValue().set(key, Value, CacheExpireTime.defaultExpireTime.getTimeValue(), TimeUnit.SECONDS);
	}


	@Override
	public void del(Object... keys) throws Exception {
		for (Object key:keys) {
			redisTemplate.delete(key);
		}
		
	}

	@Override
	public Boolean setNX(Object key, Object value) throws Exception {
		return redisTemplate.opsForValue().setIfAbsent(key, value);
	}
	
	@Override
	public Boolean expire(Object key, long timeout, TimeUnit unit)
			throws Exception {
		return redisTemplate.expire(key, timeout, unit);
	}

	@Override
	public Boolean expire(Object key, long timeout) throws Exception {
		return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
	}
	@Override
	public Boolean expireAt(Object key, Date date) {
		return redisTemplate.expireAt(key, date);
	}


	@Override
	public List<?> multiGet(Collection<?> paramCollection) throws Exception {
		return redisTemplate.opsForValue().multiGet(paramCollection);
	}

	@Override
	public void multiSet(Map<?, ?> paramMap) throws Exception {
		redisTemplate.opsForValue().multiSet(paramMap);
	}
	
	@Override
	public Boolean multiSetIfAbsent(Map<?, ?> paramMap) throws Exception {
		return redisTemplate.opsForValue().multiSetIfAbsent(paramMap);
	}

	@Override
	public void delete(Collection<?> keys) throws Exception {
		 redisTemplate.delete(keys);
	}

}
