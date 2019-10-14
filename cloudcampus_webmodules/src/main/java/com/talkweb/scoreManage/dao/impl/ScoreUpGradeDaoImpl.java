package com.talkweb.scoreManage.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.scoreManage.dao.ScoreUpGradeDao;
import com.talkweb.system.domain.business.TGmScorerankstatisticsMk;
import com.talkweb.system.domain.business.TGmScorestubzf;

@Repository
public class ScoreUpGradeDaoImpl extends MyBatisBaseDaoImpl implements ScoreUpGradeDao {

	private static final String packages = "com.talkweb.scoreManage.dao.ScoreUpGradeDao.";

	@Override
	public List<JSONObject> getAllKslc() {
		return selectList(packages + "getAllKslc");
	}

	@Override
	public List<JSONObject> getStuZfAndPm(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectList(packages + "getStuZfAndPm", cxMap);
	}

	@Override
	public List<JSONObject> getStuInBhAndKmScore(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectList(packages + "getStuInBhAndKmScore", cxMap);
	}

	@Override
	public List<JSONObject> getClassZfBzfList(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectList(packages + "getClassZfBzfList", cxMap);
	}

	@Override
	public List<JSONObject> getClassSubjectBzfList(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectList(packages + "getClassSubjectBzfList", cxMap);
	}

	@Override
	public int deleteOneLcBzf(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		int x = update(packages + "deleteStuZfBzf");
		x += update(packages + "deleteStuKmBzf");
		return x;
	}

	@Override
	public int insertMutiZfBzf(List<TGmScorestubzf> zfList) {
		// TODO Auto-generated method stub
		return update(packages + "batchInsertTGmScorestubzfList2", zfList);
	}

	@Override
	public int insertMutiKmBzf(List<TGmScorerankstatisticsMk> kmList) {
		// TODO Auto-generated method stub
		return update(packages + "batchInsertTGmScorestubzfMkList2", kmList);
	}

}
