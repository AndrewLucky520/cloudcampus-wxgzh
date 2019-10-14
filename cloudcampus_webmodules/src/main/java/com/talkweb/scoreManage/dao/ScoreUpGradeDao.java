package com.talkweb.scoreManage.dao;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.system.domain.business.TGmScorerankstatisticsMk;
import com.talkweb.system.domain.business.TGmScorestubzf;

public interface ScoreUpGradeDao {

	/**
	 * 获取全部成绩
	 * 
	 * @return
	 */
	public List<JSONObject> getAllKslc();

	public List<JSONObject> getStuZfAndPm(HashMap<String, Object> cxMap);

	/**
	 * 获取学生单科成绩以及所在班级
	 */
	public List<JSONObject> getStuInBhAndKmScore(HashMap<String, Object> cxMap);

	/**
	 * 获取班级总分标准分列表
	 * 
	 * @param cxMap
	 * @return
	 */
	public List<JSONObject> getClassZfBzfList(HashMap<String, Object> cxMap);

	/**
	 * 获取班级单科标准分列表
	 * 
	 * @param cxMap
	 * @return
	 */
	public List<JSONObject> getClassSubjectBzfList(HashMap<String, Object> cxMap);

	/**
	 * 删除一次考试的全部学生标准分数据
	 * 
	 * @param cxMap
	 * @return
	 */
	public int deleteOneLcBzf(HashMap<String, Object> cxMap);

	/**
	 * 插入一次考试的全部学生标准分数据(总分)
	 * 
	 * TGmScorestubzf 总分标准分列表
	 * 
	 * @return
	 */
	public int insertMutiZfBzf(List<TGmScorestubzf> zfList);

	/**
	 * 插入一次考试的全部学生标准分数据(单科)
	 * 
	 * TGmScorestubzf 单科标准分列表
	 * 
	 * @return
	 */
	public int insertMutiKmBzf(List<TGmScorerankstatisticsMk> kmList);

}
