/**
 * 
 */
package com.talkweb.base.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName: CacheExpireTime
 * @version:1.0
 * @Description: 
 * @author 廖刚 ---智慧校
 * @date 2015年4月17日
 */
@Component
public class CacheExpireTimeValues {
	
	/**
	 * 
	 */
	public CacheExpireTimeValues() {
		// TODO Auto-generated constructor stub
		
	}
	
	//@Value("#{configProperties['redis.cache.defaultExpireTime']}")
	private String defaultExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.maxExpireTime']}")
	private String maxExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.midExpireTime']}")
	private String midExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.minExpireTime']}")
	private String minExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.sessionDefaultExpireTime']}")
	private String sessionDefaultExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.sessionMaxExpireTime']}")
	private String sessionMaxExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.sessionMidExpireTime']}")
	private String sessionMidExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.sessionMinExpireTime']}")
	private String sessionMinExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.historicalDataDefaultExpireTime']}")
	private String historicalDataDefaultExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.historicalDataMaxExpireTime']}")
	private String historicalDataMaxExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.historicalDataMidExpireTime']}")
	private String historicalDataMidExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.historicalDataMinExpireTime']}")
	private String historicalDataMinExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.temporaryDataDefaultExpireTime']}")
	private String temporaryDataDefaultExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.temporaryDataMaxExpireTime']}")
	private String temporaryDataMaxExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.temporaryDataMidExpireTime']}")
	private String temporaryDataMidExpireTimeValue;
	
	//@Value("#{configProperties['redis.cache.temporaryDataMinExpireTimeTime']}")
	private String temporaryDataMinExpireTimeValue;
	

	/**
	 * @return the defaultExpireTimeValue
	 */
	public String getDefaultExpireTimeValue() {
		return defaultExpireTimeValue;
	}

	/**
	 * @param defaultExpireTimeValue the defaultExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.defaultExpireTime']}")
	public void setDefaultExpireTimeValue(String defaultExpireTimeValue) {
		this.defaultExpireTimeValue = defaultExpireTimeValue;
		CacheExpireTime.defaultExpireTime.setTimeValue(Long.parseLong(defaultExpireTimeValue));
	}

	/**
	 * @return the maxExpireTimeValue
	 */
	public String getMaxExpireTimeValue() {
		return maxExpireTimeValue;
	}

	/**
	 * @param maxExpireTimeValue the maxExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.maxExpireTime']}")
	public void setMaxExpireTimeValue(String maxExpireTimeValue) {
		this.maxExpireTimeValue = maxExpireTimeValue;
		CacheExpireTime.maxExpireTime.setTimeValue(Long.parseLong(maxExpireTimeValue));
	}

	/**
	 * @return the midExpireTimeValue
	 */
	public String getMidExpireTimeValue() {
		return midExpireTimeValue;
	}

	/**
	 * @param midExpireTimeValue the midExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.midExpireTime']}")
	public void setMidExpireTimeValue(String midExpireTimeValue) {
		this.midExpireTimeValue = midExpireTimeValue;
		CacheExpireTime.midExpireTime.setTimeValue(Long.parseLong(midExpireTimeValue));
	}

	/**
	 * @return the minExpireTimeValue
	 */
	public String getMinExpireTimeValue() {
		return minExpireTimeValue;
	}

	/**
	 * @param minExpireTimeValue the minExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.minExpireTime']}")
	public void setMinExpireTimeValue(String minExpireTimeValue) {
		this.minExpireTimeValue = minExpireTimeValue;
		CacheExpireTime.minExpireTime.setTimeValue(Long.parseLong(minExpireTimeValue));
	}

	/**
	 * @return the sessionDefaultExpireTimeValue
	 */
	public String getSessionDefaultExpireTimeValue() {
		return sessionDefaultExpireTimeValue;
	}

	/**
	 * @param sessionDefaultExpireTimeValue the sessionDefaultExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.sessionDefaultExpireTime']}")
	public void setSessionDefaultExpireTimeValue(
			String sessionDefaultExpireTimeValue) {
		this.sessionDefaultExpireTimeValue = sessionDefaultExpireTimeValue;
		CacheExpireTime.sessionDefaultExpireTime.setTimeValue(Long.parseLong(sessionDefaultExpireTimeValue));
	}

	/**
	 * @return the sessionMaxExpireTimeValue
	 */
	public String getSessionMaxExpireTimeValue() {
		return sessionMaxExpireTimeValue;
	}

	/**
	 * @param sessionMaxExpireTimeValue the sessionMaxExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.sessionMaxExpireTime']}")
	public void setSessionMaxExpireTimeValue(String sessionMaxExpireTimeValue) {
		this.sessionMaxExpireTimeValue = sessionMaxExpireTimeValue;
		CacheExpireTime.sessionMaxExpireTime.setTimeValue(Long.parseLong(sessionMaxExpireTimeValue));
	}

	/**
	 * @return the sessionMidExpireTimeValue
	 */
	public String getSessionMidExpireTimeValue() {
		return sessionMidExpireTimeValue;
	}

	/**
	 * @param sessionMidExpireTimeValue the sessionMidExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.sessionMidExpireTime']}")
	public void setSessionMidExpireTimeValue(String sessionMidExpireTimeValue) {
		this.sessionMidExpireTimeValue = sessionMidExpireTimeValue;
		CacheExpireTime.sessionMidExpireTime.setTimeValue(Long.parseLong(sessionMidExpireTimeValue));
	}

	/**
	 * @return the sessionMinExpireTimeValue
	 */
	public String getSessionMinExpireTimeValue() {
		return sessionMinExpireTimeValue;
	}

	/**
	 * @param sessionMinExpireTimeValue the sessionMinExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.sessionMinExpireTime']}")
	public void setSessionMinExpireTimeValue(String sessionMinExpireTimeValue) {
		this.sessionMinExpireTimeValue = sessionMinExpireTimeValue;
		CacheExpireTime.sessionMinExpireTime.setTimeValue(Long.parseLong(sessionMinExpireTimeValue));
	}

	/**
	 * @return the historicalDataDefaultExpireTimeValue
	 */
	public String gethistoricalDataDefaultExpireTimeValue() {
		return historicalDataDefaultExpireTimeValue;
	}

	/**
	 * @param historicalDataDefaultExpireTimeValue the historicalDataDefaultExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.historicalDataDefaultExpireTime']}")
	public void sethistoricalDataDefaultExpireTimeValue(
			String historicalDataDefaultExpireTimeValue) {
		this.historicalDataDefaultExpireTimeValue = historicalDataDefaultExpireTimeValue;
		CacheExpireTime.historicalDataDefaultExpireTime.setTimeValue(Long.parseLong(historicalDataDefaultExpireTimeValue));
	}

	/**
	 * @return the historicalDataMaxExpireTimeValue
	 */
	public String gethistoricalDataMaxExpireTimeValue() {
		return historicalDataMaxExpireTimeValue;
	}

	/**
	 * @param historicalDataMaxExpireTimeValue the historicalDataMaxExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.historicalDataMaxExpireTime']}")
	public void sethistoricalDataMaxExpireTimeValue(String historicalDataMaxExpireTimeValue) {
		this.historicalDataMaxExpireTimeValue = historicalDataMaxExpireTimeValue;
		CacheExpireTime.historicalDataMaxExpireTime.setTimeValue(Long.parseLong(historicalDataMaxExpireTimeValue));
	}

	/**
	 * @return the historicalDataMidExpireTimeValue
	 */
	public String gethistoricalDataMidExpireTimeValue() {
		return historicalDataMidExpireTimeValue;
	}

	/**
	 * @param historicalDataMidExpireTimeValue the historicalDataMidExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.historicalDataMidExpireTime']}")
	public void sethistoricalDataMidExpireTimeValue(String historicalDataMidExpireTimeValue) {
		this.historicalDataMidExpireTimeValue = historicalDataMidExpireTimeValue;
		CacheExpireTime.historicalDataMidExpireTime.setTimeValue(Long.parseLong(historicalDataMidExpireTimeValue));
	}

	/**
	 * @return the historicalDataMinExpireTimeValue
	 */
	public String gethistoricalDataMinExpireTimeValue() {
		return historicalDataMinExpireTimeValue;
	}

	/**
	 * @param historicalDataMinExpireTimeValue the historicalDataMinExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.historicalDataMinExpireTime']}")
	public void sethistoricalDataMinExpireTimeValue(String historicalDataMinExpireTimeValue) {
		this.historicalDataMinExpireTimeValue = historicalDataMinExpireTimeValue;
		CacheExpireTime.historicalDataMinExpireTime.setTimeValue(Long.parseLong(historicalDataMinExpireTimeValue));
	}

	/**
	 * @return the temporaryDataDefaultExpireTimeValue
	 */
	public String getTemporaryDataDefaultExpireTimeValue() {
		return temporaryDataDefaultExpireTimeValue;
	}

	/**
	 * @param temporaryDataDefaultExpireTimeValue the temporaryDataDefaultExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.temporaryDataDefaultExpireTime']}")
	public void setTemporaryDataDefaultExpireTimeValue(
			String temporaryDataDefaultExpireTimeValue) {
		this.temporaryDataDefaultExpireTimeValue = temporaryDataDefaultExpireTimeValue;
		CacheExpireTime.temporaryDataDefaultExpireTime.setTimeValue(Long.parseLong(temporaryDataDefaultExpireTimeValue));
	}

	/**
	 * @return the temporaryDataMaxExpireTimeValue
	 */
	public String getTemporaryDataMaxExpireTimeValue() {
		return temporaryDataMaxExpireTimeValue;
	}

	/**
	 * @param temporaryDataMaxExpireTimeValue the temporaryDataMaxExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.temporaryDataMaxExpireTime']}")
	public void setTemporaryDataMaxExpireTimeValue(
			String temporaryDataMaxExpireTimeValue) {
		this.temporaryDataMaxExpireTimeValue = temporaryDataMaxExpireTimeValue;
		CacheExpireTime.temporaryDataMaxExpireTime.setTimeValue(Long.parseLong(temporaryDataMaxExpireTimeValue));
	}

	/**
	 * @return the temporaryDataMidExpireTimeValue
	 */
	public String getTemporaryDataMidExpireTimeValue() {
		return temporaryDataMidExpireTimeValue;
	}

	/**
	 * @param temporaryDataMidExpireTimeValue the temporaryDataMidExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.temporaryDataMidExpireTime']}")
	public void setTemporaryDataMidExpireTimeValue(
			String temporaryDataMidExpireTimeValue) {
		this.temporaryDataMidExpireTimeValue = temporaryDataMidExpireTimeValue;
		CacheExpireTime.temporaryDataMidExpireTime.setTimeValue(Long.parseLong(temporaryDataMidExpireTimeValue));
	}

	/**
	 * @return the temporaryDataMinExpireTimeValue
	 */
	public String getTemporaryDataMinExpireTimeValue() {
		return temporaryDataMinExpireTimeValue;
	}

	/**
	 * @param temporaryDataMinExpireTimeValue the temporaryDataMinExpireTimeValue to set
	 */
	@Value("#{configProperties['redis.cache.temporaryDataMinExpireTimeTime']}")
	public void setTemporaryDataMinExpireTimeValue(
			String temporaryDataMinExpireTimeValue) {
		this.temporaryDataMinExpireTimeValue = temporaryDataMinExpireTimeValue;
		CacheExpireTime.temporaryDataMinExpireTime.setTimeValue(Long.parseLong(temporaryDataMinExpireTimeValue));
	}
	
}
