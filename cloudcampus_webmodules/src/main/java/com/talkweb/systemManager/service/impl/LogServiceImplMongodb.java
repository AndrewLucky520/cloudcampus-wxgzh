/**
 * 
 */
package com.talkweb.systemManager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.talkweb.systemManager.domain.business.TWsSyslog;
import com.talkweb.systemManager.service.LogService;

/**
 * @ClassName: LogServiceImplMongodb
 * @version:1.0
 * @Description: 
 * @author 廖刚 ---智慧校
 * @date 2015年4月1日
 */
//@Service(value="logServiceImplMongodb")
public class LogServiceImplMongodb implements LogService {
	
	private static String USER_COLLECTION = "zhx";
	
	@Autowired
	MongoTemplate mongoTemplate;
	/**
	 * 
	 */
	public LogServiceImplMongodb() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.talkweb.systemManager.service.LogService#insertLog(com.talkweb.systemManager.domain.business.TWsSyslog)
	 */
	@Override
	public int insertLog(TWsSyslog log) {
		// TODO Auto-generated method stub
		mongoTemplate.save(log, USER_COLLECTION);
		return 1;
	}

}
