package com.talkweb.scoreManage.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.dao.ScoreUpGradeDao;
import com.talkweb.scoreManage.service.ScoreUpGradeService;
import com.talkweb.system.domain.business.TGmScorerankstatisticsMk;
import com.talkweb.system.domain.business.TGmScorestubzf;

@Service
public class ScoreUpGradeServiceImpl implements ScoreUpGradeService {

	@Autowired
	private ScoreUpGradeDao scoreUpGradeDao;

	@Override
	public List<JSONObject> getAllKslc() {
		// TODO Auto-generated method stub
		return scoreUpGradeDao.getAllKslc();
	}

	@Override
	public List<String> getKmdmIntj(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, JSONObject> getXhBhMapByScore(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		HashMap<String, JSONObject> rs = new HashMap<String, JSONObject>();
		// xh,zf
		List<JSONObject> zflist = scoreUpGradeDao.getStuZfAndPm(cxMap);
		// xh,bh,cj
		List<JSONObject> kmlist = scoreUpGradeDao.getStuInBhAndKmScore(cxMap);

		for (JSONObject stu : zflist) {
			String xh = stu.getString("xh");
			rs.put(xh, stu);
		}
		// 组装科目成绩
		for (JSONObject stu : kmlist) {
			String xh = stu.getString("xh");
			if (rs.containsKey(xh)) {
				JSONObject stuZf = rs.get(xh);
				List<JSONObject> xskmList = new ArrayList<JSONObject>();
				String bh = stu.getString("bh");
				if (stuZf.containsKey("xskmList")) {

					xskmList = (List<JSONObject>) stuZf.get("xskmList");
				} else {
					stuZf.put("bh", bh);
				}
				xskmList.add(stu);
				stuZf.put("xskmList", xskmList);
			}
		}
		return rs;
	}

	@Override
	public HashMap<String, JSONObject> getBjBzfObj(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		List<JSONObject> list = scoreUpGradeDao.getClassZfBzfList(cxMap);
		HashMap<String, JSONObject> rs = new HashMap<String, JSONObject>();

		for (JSONObject bj : list) {
			String bh = bj.getString("bh");
			rs.put(bh, bj);
		}

		return rs;
	}

	@Override
	public HashMap<String, JSONObject> getBjKmBzfObj(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		List<JSONObject> list = scoreUpGradeDao.getClassSubjectBzfList(cxMap);
		HashMap<String, JSONObject> rs = new HashMap<String, JSONObject>();

		for (JSONObject bj : list) {
			String bh = bj.getString("bh");
			String kmdm = bj.getString("kmdm");
			rs.put(bh + "_" + kmdm, bj);
		}

		return rs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateUpGradeResult(HashMap<String, Object> cxMap) throws Exception {
		// TODO Auto-generated method stub
		// scoreUpGradeDao.deleteOneLcBzf(cxMap);
		List<TGmScorestubzf> zfList = (List<TGmScorestubzf>) cxMap.get("zfBzfList");
		if (zfList.size() > 0) {

			scoreUpGradeDao.insertMutiZfBzf(zfList);
		}
		List<TGmScorerankstatisticsMk> kmList = (List<TGmScorerankstatisticsMk>) cxMap.get("kmBzfList");
		if (kmList.size() > 0) {

			scoreUpGradeDao.insertMutiKmBzf(kmList);
		}
	}

	@Override
	public void deleteAllStuBzfRes() {
		// TODO Auto-generated method stub
		scoreUpGradeDao.deleteOneLcBzf(null);
	}

}
