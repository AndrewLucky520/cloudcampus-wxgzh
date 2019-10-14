package com.talkweb.notice.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

 
public interface NoticeDao {

	public List<JSONObject> getNoticeList(JSONObject param);
	public JSONObject getNotice(JSONObject param);
	public int insertNoticeRecord(JSONObject  param);
	public int insertNoticePersonnel(List<JSONObject>  list);
	public List<JSONObject> getNoticePersonnelList(JSONObject param);
	public int delNoticePersonnel(JSONObject  param);
	public int delNoticeViewed(JSONObject  param);
	
	public int updateNotice(JSONObject  param);
	public int updateClickCnt(JSONObject  param);	
	public int delNotice(JSONObject  param);
	public int insertNoticeViewed(JSONObject param);
	
}
