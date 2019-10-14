package com.talkweb.scoreManage.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.scoreManage.dao.ScoreManageConfigDao;

/****
 * 成绩管理配置DAO
 * 
 * @author guoyuanbing
 * @time 2015-05-28
 */
@Repository
public class ScoreManageConfigDaoImpl extends MyBatisBaseDaoImpl implements ScoreManageConfigDao {

	private static final String packages = "com.talkweb.scoreManage.dao.ScoreManageConfigDao.";

	@Override
	public List<JSONObject> getScoreReportNameList(Map<String, Object> map) {
		List<JSONObject> list = selectList(packages + "getScoreReportNameList", map);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public JSONObject getScoreReportConfig(Map<String, Object> map) {
		return selectOne(packages + "getScoreReportConfig", map);
	}

	@Override
	public int updateScoreReportIsShow(Map<String, Object> map) {
		return update(packages + "updateScoreReportIsShow", map);
	}

	@Override
	public int addScoreReportConfig(Map<String, Object> map) {
		return insert(packages + "addScoreReportConfig", map);
	}

	@Override
	public int updateScoreReportConfig(Map<String, Object> map) {
		return update(packages + "updateScoreReportConfig", map);
	}

	@Override
	public int selectOneScoreReportConfig(Map<String, Object> map) {
		return selectOne(packages + "selectOneScoreReportConfig", map);
	}

	@Override
	public JSONObject getScoreReportDefaultConfig(Map<String, Object> map) {
		return selectOne(packages + "getScoreReportDefaultConfig", map);
	}

	@Override
	public int updateScoreReportInfo(Map<String, Object> map) {
		return update(packages + "updateScoreReportInfo", map);
	}

	@Override
	public int addScoreReportConfigCustom(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return update(packages + "addScoreReportConfigCustom", map);
	}

	@Override
	public int updateScoreReportConfigCustom(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return update(packages + "updateScoreReportConfigCustom", map);
	}

	@Override
	public List<JSONObject> getScoreReportConfigCustom(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList(packages + "getScoreReportConfigCustom", map);
	}

	@Override
	public JSONObject getScoreReportConfigCustom(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectOne(packages + "getScoreReportConfigCustomByReportNo", map);
	}

	@Override
	public int delCustomConfig(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return delete(packages + "delCustomConfig", map);
	}

}
