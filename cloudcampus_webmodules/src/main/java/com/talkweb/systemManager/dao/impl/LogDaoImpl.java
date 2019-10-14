/**
 * 
 */
package com.talkweb.systemManager.dao.impl;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.systemManager.dao.LogDao;
import com.talkweb.systemManager.domain.business.TWsSyslog;

/**
 * @ClassName: LogDaoImpl
 * @version:1.0
 * @Description: 操作日志DAO实现
 * @author 廖刚 ---智慧校
 * @date 2015年3月19日
 */
@Repository(value="logDaoImpl")
public class LogDaoImpl extends MyBatisBaseDaoImpl implements LogDao {

	/**
	 * 
	 */
	public LogDaoImpl() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.talkweb.systemManager.dao.LogDao#insertLog(com.talkweb.systemManager.domain.business.TWsSyslog)
	 */
	@Override
	public int insertLog(TWsSyslog log) {
		// TODO Auto-generated method stub
		return insert("insertSysLog", log);
	}

}
