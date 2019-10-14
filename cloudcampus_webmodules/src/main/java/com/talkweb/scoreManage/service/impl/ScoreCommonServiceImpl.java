package com.talkweb.scoreManage.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.ScoreCommonService;

@Service
public class ScoreCommonServiceImpl implements ScoreCommonService {
	@Autowired
	private AllCommonDataService allCommonDataService;

	@Autowired
	private ScoreManageDao scoreDao;

	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;

	@Override
	public List<JSONObject> getExamGradeList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		int isAll = params.getIntValue("isAll");
		String xn = xnxq.substring(0, xnxq.length() - 1);
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "成绩数据已被删除，请刷新页面！");
		}

		List<JSONObject> data = new ArrayList<JSONObject>();

		StringBuffer all = new StringBuffer();
		List<String> list = scoreDao.getNjFromDegreeInfoNj(xnxq, params);// 得到此次考试的所有使用年级
		for (String usedGrade : list) {
			T_GradeLevel gl = T_GradeLevel
					.findByValue(Integer.parseInt(allCommonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
			JSONObject item = new JSONObject();
			item.put("text", new StringBuffer().append("[").append(allCommonDataService.ConvertSYNJ2RXND(usedGrade, xn))
					.append("]").append(njName.get(gl)).toString());
			item.put("value", usedGrade);
			data.add(item);

			all.append(usedGrade).append(",");
		}

		if (isAll > 0 && all.length() > 0) {
			all.deleteCharAt(all.length() - 1);
			JSONObject al = new JSONObject();
			al.put("text", "全部");
			al.put("value", all.toString());
			data.add(0, al);
		}

		return data;
	}

	public List<JSONObject> getClassList(JSONObject params) {
		List<JSONObject> data = new ArrayList<JSONObject>();

		String xnxq = params.getString("xnxq");
		String xn = xnxq.substring(0, xnxq.length() - 1);
		int isAll = params.getIntValue("isAll");
		Long xxdm = params.getLong("xxdm");
		List<Object> usedGradeList = params.getJSONArray("usedGradeList");

		List<T_GradeLevel> glList = new ArrayList<T_GradeLevel>();
		for (Object obj : usedGradeList) {
			String usedGrade = (String) obj;
			glList.add(
					T_GradeLevel.findByValue(Integer.parseInt(allCommonDataService.ConvertSYNJ2NJDM(usedGrade, xn))));
		}

		if (glList.size() == 0) {
			return data;
		}

		List<Grade> gradeList = allCommonDataService.getGradeByGradeLevelBatch(xxdm, glList, xnxq);
		if (CollectionUtils.isEmpty(gradeList)) {
			return data;
		}
		List<Long> classIds = new ArrayList<Long>();
		for (Grade grade : gradeList) {
			classIds.addAll(grade.getClassIds());
		}

		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(xxdm, classIds, xnxq);
		if (CollectionUtils.isEmpty(classroomList)) {
			return data;
		}
		StringBuffer all = new StringBuffer();
		for (Classroom classroom : classroomList) {
			all.append(classroom.getId()).append(",");
			JSONObject item = new JSONObject();
			item.put("text", classroom.getClassName());
			item.put("value", classroom.getId());
			data.add(item);
		}
		Collections.sort(data, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				String className1 = arg1.getString("text");
				if(className1 == null) {
					className1 = "";
				}
				String className2 = arg2.getString("text");
				if(className2 == null) {
					className2 = "";
				}
				return className1.compareTo(className2);
			}
		});
		if (isAll > 0 && all.length() > 0) {
			all.deleteCharAt(all.length() - 1);
			JSONObject item = new JSONObject();
			item.put("text", "全部");
			item.put("value", all.toString());
			data.add(0, item);
		}
		return data;
	}

	@Override
	public List<JSONObject> getExamNameList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		return scoreDao.getExamNameList(xnxq, params);
	}

	@Override
	public List<JSONObject> getwlkGroupList(JSONObject params) { // 文理科下拉分组
		int isAll = params.getIntValue("isAll");
		params.remove("isAll");
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "成绩数据已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		List<JSONObject> list = scoreDao.getwlkGroupList(xnxq, autoIncr, params);
		if (isAll > 0) {
			JSONObject item = new JSONObject();
			item.put("text", "全部");
			StringBuffer value = new StringBuffer();
			for (JSONObject ob : list) {
				value.append(ob.getString("value")).append(",");
			}
			if (value.length() > 0) {
				value.deleteCharAt(value.length() - 1);
			}
			item.put("value", value.toString());
			list.add(0, item);
		}
		return list;
	}

	@Override
	public List<JSONObject> getClassListByGroupId(JSONObject params) { // 班级下拉分组
		List<JSONObject> result = new ArrayList<JSONObject>();
		long schoolId = params.getLongValue("xxdm");
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "成绩数据已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		List<Long> classIdList = scoreDao.getClassIdListByGroupId(xnxq, autoIncr, params);
		if (classIdList.size() == 0) {
			return result;
		}
		List<Classroom> classrooms = allCommonDataService.getClassroomBatch(schoolId, classIdList, xnxq);
		if (CollectionUtils.isEmpty(classrooms)) {
			throw new CommonRunException(-1, "获取SDK数据异常，请联系管理员！");
		}
		String isAll = params.getString("isAll");
		StringBuffer allVal = new StringBuffer();
		for (Classroom classroom : classrooms) {
			JSONObject item = new JSONObject();
			item.put("text", classroom.getClassName());
			item.put("value", classroom.getId());
			allVal.append(classroom.getId()).append(",");
			result.add(item);
		}

		Collections.sort(result, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				String text1 = arg1.getString("text");
				String text2 = arg2.getString("text");
				return text1.compareTo(text2);
			}
		});

		if ("1".equals(isAll)) {
			if (allVal.length() > 0) {
				allVal.deleteCharAt(allVal.length() - 1);
			}
			JSONObject item = new JSONObject();
			item.put("value", allVal.toString());
			item.put("text", "全部");
			result.add(0, item);
		}
		return result;
	}

	public List<JSONObject> getExamSubjectDropDownList(JSONObject params) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		String xnxq = params.getString("xnxq");
		long schoolId = params.getLongValue("xxdm");
		String isAll = params.getString("isAll");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "成绩数据已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		List<Long> ids = scoreDao.getExamSubjectIdList(xnxq, autoIncr, params);
		if (ids.size() == 0) {
			return result;
		}
		List<LessonInfo> lessonInfos = allCommonDataService.getLessonInfoBatch(schoolId, ids, xnxq);
		if (CollectionUtils.isEmpty(lessonInfos)) {
			throw new CommonRunException(-1, "SDK获取数据异常，请联系管理员！");
		}

		Integer type = params.getInteger("type");
		StringBuffer allVal = new StringBuffer();
		for (LessonInfo lessonInfo : lessonInfos) {
			if(type != null && type != lessonInfo.getType()) {
				continue;
			}
			
			JSONObject item = new JSONObject();
			item.put("value", lessonInfo.getId());
			item.put("text", lessonInfo.getName());
			result.add(item);
			allVal.append(lessonInfo.getId()).append(",");
		}

		Collections.sort(result, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				return Long.compare(arg1.getLongValue("value"), arg2.getLongValue("value"));
			}
		});

		if ("1".equals(isAll)) {// 是否显示全部选项
			if (allVal.length() > 0) {
				allVal.deleteCharAt(allVal.length() - 1);
			}
			JSONObject item = new JSONObject();
			item.put("value", allVal.toString());
			item.put("text", "全部");
			result.add(0, item);
		}
		return result;
	}

	@Override
	public List<JSONObject> getExamClassDropDownList(JSONObject params) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "成绩数据已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		List<Long> classIds = scoreDao.getExamClassIdList(xnxq, autoIncr, params);
		if (classIds.size() == 0) {
			return result;
		}

		long schoolId = params.getLongValue("xxdm");
		int isAll = params.getIntValue("isAll");

		List<Classroom> classrooms = allCommonDataService.getClassroomBatchNoAccount(schoolId, classIds, xnxq);
		if (CollectionUtils.isEmpty(classrooms)) {
			throw new CommonRunException(-1, "SDK获取数据异常，请联系管理员！");
		}
		StringBuffer allVal = new StringBuffer();
		for (Classroom classroom : classrooms) {
			JSONObject item = new JSONObject();
			item.put("value", classroom.getId());
			item.put("text", classroom.getClassName());
			allVal.append(classroom.getId()).append(",");
			result.add(item);
		}
		Collections.sort(result, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String text1 = o1.getString("text");
				String text2 = o2.getString("text");
				return text1.compareTo(text2);
			}
		});
		if (isAll > 0) {// 是否显示全部选项
			if (allVal.length() > 0) {
				allVal.deleteCharAt(allVal.length() - 1);
			}
			JSONObject item = new JSONObject();
			item.put("value", allVal.toString());
			item.put("text", "全部");
			result.add(0, item);
		}
		return result;
	}

	@Override
	public List<JSONObject> getExamClassList(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		Integer autoIncr = params.getInteger("autoIncr");
		List<JSONObject> list = scoreDao.getExamClassList(termInfoId, autoIncr, params);
		return list;
	}
}
