/**
 * 
 */
package com.talkweb.systemManager.service;

import com.talkweb.systemManager.domain.business.TWsSyslog;

/**
 * @ClassName: LogService
 * @version:1.0
 * @Description: 操作日志Service
 * @author 廖刚 ---智慧校
 * @date 2015年3月19日
 */
public interface LogService {
	
	/**
	 * @Description 插入操作日志记录
	 * @param log 日志
	 * @return 影响记录数
	 **/
	public int insertLog(TWsSyslog log);

}
