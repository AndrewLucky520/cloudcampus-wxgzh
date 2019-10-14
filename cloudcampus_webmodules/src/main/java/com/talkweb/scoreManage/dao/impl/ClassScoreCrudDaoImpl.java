package com.talkweb.scoreManage.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountLesson;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.ClassScoreCrudDao;
import com.talkweb.scoreManage.po.ce.ClassExamInfo;
import com.talkweb.scoreManage.po.ce.ClassExamRelative;
import com.talkweb.scoreManage.po.ce.ClassExamSubjectScore;
import com.talkweb.scoreManage.po.ce.ClassTotalScoreStatistics;
import com.talkweb.scoreManage.po.ce.CustomScore;
import com.talkweb.scoreManage.po.ce.CustomScoreInfo;
import com.talkweb.scoreManage.po.ce.StudentTotalScoreStatistics;

@Repository
public class ClassScoreCrudDaoImpl extends MyBatisBaseDaoImpl implements ClassScoreCrudDao {
	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;

	private static int INSERT_LIMIT = 1000;

	private int insertBatch(List<?> list, SplitDbAndTableRule splitDbAndTableRule, String key) {
		int result = 0;
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = list.size() % INSERT_LIMIT == 0 ? list.size() / INSERT_LIMIT - 1 : list.size() / INSERT_LIMIT;
		for (int batch = 0; batch <= batchSize; batch++) {
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > list.size() ? list.size() : toIndex;
			splitDbAndTableRule.setData(list.subList(fromIndex, toIndex));
			result += insert(MYBATIS_PREFIX + key, splitDbAndTableRule);
		}
		return result;
	}

	private static final String MYBATIS_PREFIX = "com.talkweb.scoreReport.dao.ClassScoreCrudDao.";

	@Override
	public List<JSONObject> getExamList(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getExamList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public boolean ifExistsSameNameInClassExamInfo(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsSameNameInClassExamInfo", splitDbAndTableRule);
		if (result != null) {
			return true;
		}
		return false;
	}

	@Override
	public int createExam(String termInfoId, Object obj) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, obj);
		return insert(MYBATIS_PREFIX + "createExam", splitDbAndTableRule);
	}

	@Override
	public int updateClassExam(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		return update(MYBATIS_PREFIX + "updateClassExam", splitDbAndTableRule);
	}

	@Override
	public ClassExamInfo getClassExamInfoById(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		return selectOne(MYBATIS_PREFIX + "getClassExamInfoById", splitDbAndTableRule);
	}

	@Override
	public int deleteExam(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		int i = 0;
		i += delete(MYBATIS_PREFIX + "deleteExam", splitDbAndTableRule);
		i += delete(MYBATIS_PREFIX + "deleteClassExamSubject", splitDbAndTableRule);
		i += delete(MYBATIS_PREFIX + "deleteClassExamClassListTwo", splitDbAndTableRule);
		i += delete(MYBATIS_PREFIX + "deleteClassExamSubjectScoreListTwo", splitDbAndTableRule);
		i += delete(MYBATIS_PREFIX + "deleteClassExamClassTotalScoreListTwo", splitDbAndTableRule);
		i += delete(MYBATIS_PREFIX + "deleteClassExamStudentTotalScoreListTwo", splitDbAndTableRule);

		return i;
	}

	@Override
	public void deleteClassExamClassListTwo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		delete(MYBATIS_PREFIX + "deleteClassExamClassListTwo", splitDbAndTableRule);
	}

	@Override
	public void insertCustomScore(String termInfoId, Integer autoIncr, List<CustomScore> l_excel) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		insertBatch(l_excel, splitDbAndTableRule, "insertCustomScore");
	}

	@Override
	public int insertCustomScoreInfo(String termInfoId, Integer autoIncr, List<CustomScoreInfo> l_params) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(l_params, splitDbAndTableRule, "insertCustomScoreInfo");
	}

	@Override
	public void deleteCustomScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		delete(MYBATIS_PREFIX + "deleteCustomScore", splitDbAndTableRule);
	}

	@Override
	public void deleteCustomScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		delete(MYBATIS_PREFIX + "deleteCustomScoreInfo", splitDbAndTableRule);
	}

	@Override
	public void insertClassExamRelative(String termInfoId, Integer autoIncr, List<ClassExamRelative> l_eclass) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		insertBatch(l_eclass, splitDbAndTableRule, "insertClassExamRelative");
	}

	@Override
	public List<JSONObject> getInfoFromClassExamSubjectScore(String termInfoId, Integer autoIncr, JSONObject params) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, params);

		List<JSONObject> result = new ArrayList<JSONObject>();

		long schoolId = params.getLongValue("schoolId");
		if (params.getInteger("examType") == 1) { // 系统格式
			List<ClassExamSubjectScore> list = selectList(MYBATIS_PREFIX + "getInfoFromClassExamSubjectScore",
					splitDbAndTableRule);
			if (CollectionUtils.isEmpty(list)) {
				return result;
			}

			Map<Long, Set<Long>> classId2SubjectIdMap = new HashMap<Long, Set<Long>>();
			Set<Long> studentIdSet = new HashSet<Long>();
			for (ClassExamSubjectScore obj : list) {
				Long studentId = Long.valueOf(obj.getStudentId());
				if (studentId != null) {
					studentIdSet.add(studentId);
				}
				Long classId = Long.valueOf(obj.getClassId());
				Long subjectId = Long.valueOf(obj.getSubjectId());
				if (classId == null || subjectId == null) {
					continue;
				}
				if (!classId2SubjectIdMap.containsKey(classId)) {
					classId2SubjectIdMap.put(classId, new HashSet<Long>());
				}
				classId2SubjectIdMap.get(classId).add(subjectId);
			}
			if (studentIdSet.size() > 0) {
				JSONObject obj = new JSONObject();
				obj.put("role", T_Role.Student);
				obj.put("ids", new ArrayList<Long>(studentIdSet));
				result.add(obj);
			}

			Set<Long> teacherIdSet = new HashSet<Long>();
			List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId,
					new ArrayList<Long>(classId2SubjectIdMap.keySet()), termInfoId);
			if (CollectionUtils.isNotEmpty(classrooms)) {
				for (Classroom classroom : classrooms) {
					Long deanId = classroom.getDeanAccountId();
					if (deanId != null) { // 添加班主任
						teacherIdSet.add(deanId);
					}

					// 添加对应的任课教师
					List<AccountLesson> als = classroom.getAccountLessons();
					if (CollectionUtils.isNotEmpty(als)) {
						long classId = classroom.getId();
						Set<Long> set = classId2SubjectIdMap.get(classId);
						for (AccountLesson al : als) {
							Long subjectId = al.getLessonId();
							Long teacherId = al.getAccountId();
							if (subjectId != null && teacherId != null && set.contains(subjectId)) {
								teacherIdSet.add(teacherId);
							}
						}
					}
				}
			}
			if (teacherIdSet.size() > 0) {
				JSONObject obj = new JSONObject();
				obj.put("role", T_Role.Teacher);
				obj.put("ids", new ArrayList<Long>(teacherIdSet));
				result.add(obj);
			}
		} else {
			List<Long> studentIds = selectList(MYBATIS_PREFIX + "getCustomScoreInfo", splitDbAndTableRule);
			if (CollectionUtils.isNotEmpty(studentIds)) {
				JSONObject obj = new JSONObject();
				obj.put("role", T_Role.Student);
				obj.put("ids", studentIds);
				result.add(obj);
			}
		}

		return result;
	}

	@Override
	public int insertClassExamScoreBatch(String termInfoId, Integer autoIncr, List<ClassExamSubjectScore> scoreList,
			int type) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		if (type == 1) {
			return insertBatch(scoreList, splitDbAndTableRule, "updateClassExamScore2");
		} else {
			return insertBatch(scoreList, splitDbAndTableRule, "updateClassExamScore3");
		}
	}

	@Override
	public int deleteClassExamSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteClassExamSubjectList2", splitDbAndTableRule);
	}

	@Override
	public int deleteClassExamClassList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteClassExamClassList2", splitDbAndTableRule);
	}

	@Override
	public int addClassExamSubjectList(String termInfoId, Integer autoIncr, Object obj) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "addClassExamSubjectList2", splitDbAndTableRule);
	}

	@Override
	public int addClassExamClassList(String termInfoId, Integer autoIncr, Object obj) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "addClassExamClassList2", splitDbAndTableRule);
	}

	@Override
	public List<StudentTotalScoreStatistics> selectClassExamSubjectScore(String termInfoId, Integer autoIncr,
			Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<StudentTotalScoreStatistics> list = selectList(MYBATIS_PREFIX + "selectClassExamSubjectScore2",
				splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<StudentTotalScoreStatistics>();
		}
		return list;
	}

	@Override
	public int addClassExamStudentTotalScore(String termInfoId, Integer autoIncr,
			List<StudentTotalScoreStatistics> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "addClassExamStudentTotalScore2");
	}

	@Override
	public int addClassExamClassTotalScore(String termInfoId, Integer autoIncr, List<ClassTotalScoreStatistics> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "addClassExamClassTotalScore2");
	}

	@Override
	public List<Long> selectClassExamClassIdList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "selectClassExamClassIdList2", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<Long> selectClassExamSubjectIdList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "selectClassExamSubjectIdList2", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<StudentTotalScoreStatistics> selectClassExamTotalScore(String termInfoId, Integer autoIncr,
			Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<StudentTotalScoreStatistics> list = selectList(MYBATIS_PREFIX + "selectClassExamTotalScore2",
				splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<StudentTotalScoreStatistics>();
		}
		return list;
	}

	@Override
	public List<ClassExamSubjectScore> selectClassExamEverSubjectScore(String termInfoId, Integer autoIncr,
			Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<ClassExamSubjectScore> list = selectList(MYBATIS_PREFIX + "selectClassExamEverSubjectScore2",
				splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<ClassExamSubjectScore>();
		}
		return list;
	}
}
