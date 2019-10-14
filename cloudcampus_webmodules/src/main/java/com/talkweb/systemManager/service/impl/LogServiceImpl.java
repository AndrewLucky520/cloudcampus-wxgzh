/**
 * 
 */
package com.talkweb.systemManager.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.talkweb.systemManager.dao.LogDao;
import com.talkweb.systemManager.domain.business.TWsSyslog;
import com.talkweb.systemManager.service.LogService;

/**
 * @ClassName: LogServiceImpl
 * @version:1.0
 * @Description: 操作日志Service
 * @author 廖刚 ---智慧校
 * @date 2015年3月19日
 */
@Service(value="logServiceImpl")
public class LogServiceImpl implements LogService {
	
	@Resource(name="logDaoImpl")
	private LogDao logDao;

	/**
	 * 
	 */
	public LogServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.talkweb.systemManager.service.LogService#insertLog(com.talkweb.systemManager.domain.business.TWsSyslog)
	 */
	@Override
	public int insertLog(TWsSyslog log) {
		// TODO Auto-generated method stub
		return logDao.insertLog(log);
	}

}
