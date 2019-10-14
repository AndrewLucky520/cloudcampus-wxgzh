/**
 * 
 */
package com.talkweb.base.common;

/**
 * @ClassName: CacheExpireTime
 * @version:1.0
 * @Description: 
 * @author 廖刚 ---智慧校
 * @date 2015年6月17日
 */
public enum CacheExpireTime {
	
	defaultExpireTime(86400, "缺省缓存时间：一天"),
	maxExpireTime(604800, "最大缓存时间：七天"),
	midExpireTime(86400, "一般缓存时间：一天"),
	minExpireTime(3600, "最小缓存时间:一小时"),
	//session缓存
	sessionDefaultExpireTime(86400, "缺省缓存时间：一天"),
	sessionMaxExpireTime(604800, "最大缓存时间：七天"),
	sessionMidExpireTime(86400, "一般缓存时间：一天"),
	sessionMinExpireTime(3600, "最小缓存时间:一小时"),
	//历史数据缓存
	historicalDataDefaultExpireTime(86400, "缺省缓存时间：一天"),
	historicalDataMaxExpireTime(604800, "最大缓存时间：七天"),
	historicalDataMidExpireTime(86400, "一般缓存时间：一天"),
	historicalDataMinExpireTime(3600, "最小缓存时间:一小时"),
	//临时数据缓存
	temporaryDataDefaultExpireTime(1800, "缺省缓存时间：30分钟"),
	temporaryDataMaxExpireTime(3600, "最大缓存时间：一小时"),
	temporaryDataMidExpireTime(1800, "一般缓存时间：30分钟"),
	temporaryDataMinExpireTime(900, "最小缓存时间:15分钟"),
	temporaryDataSessionExpireTime(3600, "最大缓存时间：一小时");
	
	
	
	
	/**
	 * @param timeValue
	 * @param desc
	 */
	private CacheExpireTime(long timeValue, String desc) {
		this.timeValue = timeValue;
		this.desc = desc;
	}
	
	private long timeValue;	//缓存时间
	private String desc;	//缓存枚举描述
	
	
	
	/**
	 * @return the timeValue
	 */
	public long getTimeValue() {
		return timeValue;
	}
	/**
	 * @param timeValue the timeValue to set
	 */
	public void setTimeValue(Long timeValue) {
		this.timeValue = timeValue;
	}
	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}
	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	
	/*
     * 获取错误描述代码
     */
    public static long getCodeByDesc(String desc) {
    	CacheExpireTime[] values = values();
        for (CacheExpireTime value : values) {
            if (desc != null) {
                if (desc.contains(value.getDesc())) {
                    return value.getTimeValue();
                }
            }
        }

        return Long.parseLong(OutputMessage.unknowSeviceErrorDesc.getCode());
    }   
       
    /*
     * 获取错误描述信息
     */
    public static String getDescByCode(String code) {
    	CacheExpireTime[] values = values();
        for (CacheExpireTime value : values) {
            if (code != null) {
                if (code.equals(value.getTimeValue())) {
                    return value.getDesc();
                }
            }
        }
        return OutputMessage.unknowSeviceErrorDesc.getDesc();
    }
    
	

}
