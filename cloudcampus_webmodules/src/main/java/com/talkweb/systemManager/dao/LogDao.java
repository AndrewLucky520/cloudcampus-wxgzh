/**
 * 
 */
package com.talkweb.systemManager.dao;

import com.talkweb.systemManager.domain.business.TWsSyslog;

/**
 * @ClassName: LogDao
 * @version:1.0
 * @Description: 操作日志DAO接口
 * @author 廖刚 ---智慧校
 * @date 2015年3月19日
 */
public interface LogDao {
	
	/*
	 * 插入日志记录
	 */
	int insertLog(TWsSyslog log);

}
