package com.talkweb.notice.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.notice.dao.NoticeDao;
import com.talkweb.notice.service.NoticeService;

/** 
* @author  Administrator
* @version 创建时间：2017年6月19日 下午2:34:30 
* 程序的简单说明
*/

@Service
public class NoticeServiceImpl implements NoticeService{

	@Autowired
	private NoticeDao noticeDao;
	
	@Override
	public List<JSONObject> getNoticeList(JSONObject param) {
	 
		return noticeDao.getNoticeList(param);
	}

	@Override
	public JSONObject getNotice(JSONObject param) {
	 
		return noticeDao.getNotice(param);
	}

	@Override
	public int insertNoticeRecord(JSONObject param) {
		 
		return noticeDao.insertNoticeRecord(param);
	}

	@Override
	public int updateNotice(JSONObject param) {
		 
		return noticeDao.updateNotice(param);
	}

	@Override
	public int delNotice(JSONObject param) {
 
		return noticeDao.delNotice(param);
	}

	@Override
	public int updateClickCnt(JSONObject param) {
	 
		return noticeDao.updateClickCnt(param);
	}

	@Override
	public int insertNoticePersonnel(List<JSONObject>  list) {
	 
		return noticeDao.insertNoticePersonnel(list);
	}

	@Override
	public List<JSONObject> getNoticePersonnelList(JSONObject param) {
		return noticeDao.getNoticePersonnelList(param);
	}

	@Override
	public int delNoticePersonnel(JSONObject param) {
		 
		return noticeDao.delNoticePersonnel(param);
	}

	@Override
	public int insertNoticeViewed(JSONObject param) {
		 
		return noticeDao.insertNoticeViewed(param);
	}

	@Override
	public int delNoticeViewed(JSONObject param) {
		 
		return noticeDao.delNoticeViewed(param);
	}
 
}
