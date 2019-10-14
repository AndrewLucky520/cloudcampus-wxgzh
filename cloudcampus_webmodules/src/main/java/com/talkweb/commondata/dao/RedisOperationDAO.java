/**
 * 
 */
package com.talkweb.commondata.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: RedisOperationDAO
 * @version:1.0
 * @Description: redis操作接口
 * @author 廖刚 ---智慧校
 * @date 2015年5月12日
 */
public interface RedisOperationDAO {
	/**
	 * <p> 查看redis是否含有此key </p>
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws Exception
	 */
	public Boolean hasKey(Object key) throws Exception;
	
	/**
	 * <p> 设置key过期时间 </p>
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws Exception
	 */
	public Boolean expire(Object key, long timeout, TimeUnit unit) throws Exception;
	
	/**
	 * <p> 设置key过期时间 </p>
	 * @param key
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Boolean expire(Object key, long timeout) throws Exception;
	
	/**
	 *  <p> 设置key过期时间 </p>
	 * @param key
	 * @param date
	 * @return
	 */
	public Boolean expireAt(Object key, Date date);	

	
	/** 
     * <p> 获取单个值，通过key获取储存在redis中的value，并释放连接</p> 
     * @param key
     * @return 成功返回value 失败返回null
     * @throws Exception
     */
	public Object get(Object key) throws Exception;
	
	/** 
     * <p> 获取单个值，通过key获取储存在redis中的value加一并返回，并释放连接</p> 
     * @param key
     * @return 成功返回value 失败返回null
     * @throws Exception
     */
	public Long increment(Object key, long delta) throws Exception;
	
	/**
	 *  <p> 获取多个值，通过key获取储存在redis中的value，并释放连接</p> 
	 * @param paramCollection
	 * @return
	 * @throws Exception
	 */
	public List<?> multiGet(Collection<?> paramCollection) throws Exception;

	
	/** 
     * <p>设置单个值，向redis存入key和value，并释放连接资源；如果key已经存在 则覆盖</p> 
     * @param key 
     * @param value
     * @throws Exception
     */	
	public void set(Object key, Object Value) throws Exception;
	
	/** 
     * <p>设置单个值，向redis存入key和value，并释放连接资源；如果key已经存在 则覆盖</p> 
     * @param key 
     * @param value 
     * @param expireTime 原则上值为CacheExpireTime
     * @throws Exception
     */	
	public void set(Object key, Object Value, long expireTime) throws Exception;
	
	/** 
     * <p>设置单个值，向redis存入key和value，并释放连接资源；如果key已经存在 则覆盖</p> 
     * @param key 
     * @param value 
     * @param expireTime 原则上值为CacheExpireTime
     * @param timeUnit
     * @throws Exception
     */	
	public void set(Object key, Object Value, long expireTime, TimeUnit timeUnit) throws Exception;
	
	/**
	 *  <p>设置多个值，向redis存入key和value，并释放连接资源；如果key已经存在 则覆盖</p> 
	 * @param paramMap
	 * @throws Exception
	 */
	public void multiSet(Map<?, ?> paramMap) throws Exception;
	
	/**
	 * <p>设置单个值，向redis存入key和value，如果key已经存在则不设置</p>
	 * @param key
	 * @param value
	 * @return 成功返回true 失败返回false
	 * @throws Exception
	 */
	public Boolean setNX(Object key, Object value) throws Exception;
	
	/**
	 * <p>设置多个值，向redis存入key和value，如果key已经存在则不设置</p>
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Boolean multiSetIfAbsent(Map<?, ?> paramMap) throws Exception;

	
	/**
	 * <p>删除给定的一个或多个 key;不存在的 key 会被忽略。</p>
	 * @param keys
	 * @throws Exception 
	 */
	public void del(Object... keys) throws Exception;	
	public void delete(Collection<?> keys) throws Exception;
	
}
