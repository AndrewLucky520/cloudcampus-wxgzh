package com.talkweb.placementtask.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_Gender;

public class StudentInfo implements Serializable {
	private static final long serialVersionUID = 9191538471006745547L;

	private Long schoolId;
	private String placementId;
	private String teachingClassId;
	private String openClassInfoId = "";
	private String openClassTaskId = "";
	private Float sortField;
	private String termInfo;
	private Integer type;	// 1:表示拥有自愿自动分入班级，2：表示拥有自愿，手动分入班级；3：表示没有自愿手动分配班级
	
	private Long classId;	// 班级id
	private Long accountId;	// 账户id
	
	private String name;	// 姓名
	private String nameSpelling;	// 姓名拼写

	private T_Gender gender;	// 性别

	private String subjectIdsStr;	// 志愿科目组合

	private Map<Long, Float> scoreDetailMap = new HashMap<Long, Float>();
	
	// 所属教学班级
	private TeachingClassInfo teachingClassInfo;
	private OpenClassTask openClassTask;

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public String getPlacementId() {
		return placementId;
	}

	public void setPlacementId(String placementId) {
		this.placementId = placementId;
	}

	public String getTeachingClassId() {
		return teachingClassId;
	}

	public void setTeachingClassId(String teachingClassId) {
		this.teachingClassId = teachingClassId;
	}

	public String getOpenClassInfoId() {
		return openClassInfoId;
	}

	public void setOpenClassInfoId(String openClassInfoId) {
		this.openClassInfoId = openClassInfoId;
	}

	public String getOpenClassTaskId() {
		return openClassTaskId;
	}

	public void setOpenClassTaskId(String openClassTaskId) {
		this.openClassTaskId = openClassTaskId;
	}

	public Float getSortField() {
		return sortField;
	}

	public void setSortField(Float sortField) {
		this.sortField = sortField;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getClassId() {
		return classId;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameSpelling() {
		return nameSpelling;
	}

	public void setNameSpelling(String nameSpelling) {
		this.nameSpelling = nameSpelling;
	}

	public T_Gender getGender() {
		return gender;
	}

	public void setGender(T_Gender gender) {
		this.gender = gender;
	}

	public String getSubjectIdsStr() {
		return subjectIdsStr;
	}

	public void setSubjectIdsStr(String subjectIdsStr) {
		this.subjectIdsStr = subjectIdsStr;
	}

	public void putScore(long subjectId, float score) {
		scoreDetailMap.put(subjectId, score);
	}
	
	public Map<Long, Float> getScoreAll(){
		return scoreDetailMap;
	}
	
	public void putScoreAll(Map<Long, Float> map) {
		scoreDetailMap.putAll(map);
	}
	
	public float getTotalScore() {
		float totalScore = 0f;
		for (Float score : scoreDetailMap.values()) {
			totalScore += score;
		}
		return totalScore;
	}

	public float getScore(Long subjectId) {
		Float score = scoreDetailMap.get(subjectId);
		if (score == null) {
			return 0;
		}
		return score;
	}

	public float getScore(Long[] subjectIds) {
		if (subjectIds == null || subjectIds.length == 0) {
			return 0f;
		}
		float totalScore = 0f;
		for (Long subjectId : subjectIds) {
			Float score = scoreDetailMap.get(subjectId);
			if (score != null) {
				totalScore += score;
			}
		}
		return totalScore;
	}

	public static Map<Long, StudentInfo> convertToStudentScoreMap(List<JSONObject> scoreInfos) {
		if (CollectionUtils.isEmpty(scoreInfos)) {
			return new HashMap<Long, StudentInfo>();
		}
		Map<Long, StudentInfo> map = new HashMap<Long, StudentInfo>();
		for (JSONObject scoreInfo : scoreInfos) {
			Long accountId = scoreInfo.getLong("accountId");
			if (accountId == null) {
				continue;
			}
			if (!map.containsKey(accountId)) {
				StudentInfo studInfo = new StudentInfo();
				studInfo.setAccountId(accountId);
				studInfo.setClassId(scoreInfo.getLong("classId"));
				map.put(accountId, studInfo);
			}
			StudentInfo studInfo = map.get(accountId);
			Long subjectId = scoreInfo.getLong("subjectId");
			if (subjectId == null) {
				continue;
			}
			Float score = scoreInfo.getFloat("score");
			if (score == null) {
				score = 0f;
			}
			studInfo.putScore(subjectId, score);
		}
		return map;
	}

	public static List<StudentInfo> convertToStudentScoreList(Map<Long, StudentInfo> map) {
		return new ArrayList<StudentInfo>(map.values());
	}

	public static List<StudentInfo> convertToStudentScoreList(List<JSONObject> scoreInfos) {
		return new ArrayList<StudentInfo>(convertToStudentScoreMap(scoreInfos).values());
	}

	public TeachingClassInfo getTeachingClassInfo() {
		return teachingClassInfo;
	}

	public void setTeachingClassInfo(TeachingClassInfo teachingClassInfo) {
		this.teachingClassInfo = teachingClassInfo;
	}

	public OpenClassTask getOpenClassTask() {
		return openClassTask;
	}

	public void setOpenClassTask(OpenClassTask openClassTask) {
		this.openClassTask = openClassTask;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
