package com.talkweb.notice.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.notice.dao.NoticeDao;

/** 
* @author  Administrator
* @version 创建时间：2017年6月19日 下午2:34:42 
* 程序的简单说明
*/

@Repository
public class NoticeDaoImpl extends MyBatisBaseDaoImpl  implements NoticeDao {

	@Override
	public List<JSONObject> getNoticeList(JSONObject param) {
		 
		return selectList("getNoticeList", param);
	}

	@Override
	public JSONObject getNotice(JSONObject param) {
		 
		return selectOne("getNotice", param);
	}

	@Override
	public int insertNoticeRecord(JSONObject param) {
 
		return insert("insertNoticeRecord", param);
	}

	@Override
	public int updateNotice(JSONObject param) {
 
		return update("updateNotice", param);
	}

	@Override
	public int delNotice(JSONObject param) {
		 
		return delete("delNotice", param);
	}

	@Override
	public int updateClickCnt(JSONObject param) {
	 
		return  update("updateClickCnt", param);
	}

	@Override
	public int insertNoticePersonnel(List<JSONObject>  list) {
		 
		return insert("insertNoticePersonnel", list);
	}

	@Override
	public List<JSONObject> getNoticePersonnelList(JSONObject param) {
		
		return selectList("getNoticePersonnelList", param);
	}

	@Override
	public int delNoticePersonnel(JSONObject param) {
		 
		return delete("delNoticePersonnel", param);
	}

	@Override
	public int insertNoticeViewed(JSONObject param) {
		 
		return insert("insertNoticeViewed", param);
	}

	@Override
	public int delNoticeViewed(JSONObject param) {
		 
		return delete("delNoticeViewed", param);
	}

 
}
