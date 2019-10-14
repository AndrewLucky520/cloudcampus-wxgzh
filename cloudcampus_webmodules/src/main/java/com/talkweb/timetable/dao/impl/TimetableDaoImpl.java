package com.talkweb.timetable.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupInfo;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupParam;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustResult;
import com.talkweb.timetable.dao.TimetableDao;

@Repository
public class TimetableDaoImpl extends MyBatisBaseDaoImpl implements
		TimetableDao {

	@Override
	public List<JSONObject> getTimetable(Map<String, String> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTimetable", map);
	}

	@Override
	public void deleteTimetable(JSONObject object) {
		delete("com.talkweb.timetable.dao.TimetableDao.deleteTimetable", object);
	}

	@Override
	public int updateTimetable(JSONObject object) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateTimetable", object);
	}
	
	@Override
	public int updateTimetableCheck(Map<String, Object> map) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateTimetableCheck", map);
	}

	@Override
	public int addTimetable(Map<String, Object> map) {
		return insert("com.talkweb.timetable.dao.TimetableDao.addTimetable", map);
	}
	
	@Override
	public void copyTimetable(JSONObject object) {
		insert("com.talkweb.timetable.dao.TimetableDao.copyTimetable", object);
	}
	
	@Override
	public void copyTimetableTwo(JSONObject object) {
		insert("com.talkweb.timetable.dao.TimetableDao.copyTimetable_V2", object);	
	}

	@Override
	public String getTimetableXnxq(Map<String, String> map) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getTimetableXnxq", map);
	}

	@Override
	public JSONObject getTimetableSection(JSONObject object) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getTimetableSection", object);
	}
	
	@Override
	public int deleteGradeList(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteGradeList", object);
	}

	@Override
	public int addGradeList(List<JSONObject> list) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertGradeList", list);
	}

	@Override
	public List<JSONObject> getTeachingTasks(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTeachingTasks", object);
	}

	@Override
	public List<JSONObject> getSetNumList(JSONObject object) {
		return selectList("getSetNumList", object);
	}

	@Override
	public JSONObject getTotalNum(JSONObject object) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getTotalNum", object);
	}

	@Override
	public int updateTeachingTask(JSONObject object) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateTeachingTask", object);
	}

	@Override
	public int addTeachingTask(JSONObject object) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertTeachingTask",object);	
	}

	@Override
	public int deleteTeachingTask(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteTeachingTask",object);
	}

	@Override
	public int deleteTeachingTaskById(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteTeachingTaskById",object);
	}

	@Override
	public int deleteTaskTeachers(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteTaskTeachers", object);
	}

	@Override
	public List<JSONObject> getTeachingTaskImbody(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTeachingTaskImbody",object);
	}

	@Override
	public int updateTaskTeachers(List<JSONObject> list) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateTaskTeachers",list);
	}

	@Override
	public List<String> getRuleCourseIds(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleCourseIds", object);
	}

	@Override
	public List<String> getCourseRuleId(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getCourseRuleId", object);
	}

	@Override
	public int addCourseRule(JSONObject object) {
		return insert("com.talkweb.timetable.dao.TimetableDao.addCourseRule", object);
	}

	@Override
	public int deleteCourseRule(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteCourseRule", object);
	}

	@Override
	public int deleteCourseFixed(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteCourseFixed", object);
	}
	
	@Override
	public int addCourseFixed(List<JSONObject> list) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertCourseFixed", list);
	}

	@Override
	public JSONObject getCourseRule(JSONObject object) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getCourseRule", object);
	}

	@Override
	public List<String> getRuleTeacher(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleTeacher", object);
	}	

	@Override
	public List<JSONObject> getTeacherRule(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTeacherRule", object);
	}

	@Override
	public String getTeacherRuleId(JSONObject object) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getTeacherRuleId", object);
	}

	@Override
	public int updateTeacherRule(JSONObject object) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateTeacherRule", object);
	}

	@Override
	public int deleteTeacherRuleFixed(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteTeacherRuleFixed", object);
	}
	
	@Override
	public int deleteTeacherRule(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteTeacherRule", object);
	}
	
	@Override
	public int addTeacherRuleFixed(List<JSONObject> list) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertTeacherRuleFixed", list);
	}
	
	@Override
	public int updateTeacherGroup(JSONObject object) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateTeacherGroup", object);
	}

	@Override
	public int addTeacherGroupMembers(List<JSONObject> list) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertTeacherGroupMembers", list);
	}

	@Override
	public int addTeacherGroupFixed(List<JSONObject> list) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertTeacherGroupFixed", list);
	}

	@Override
	public int delTeacherGroupMembers(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.delTeacherGroupMembers", object);
	}

	@Override
	public int delTeacherGroupFixed(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.delTeacherGroupFixed", object);
	}

	@Override
	public int deleteTeacherGroup(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteTeacherGroup", object);
	}

	@Override
	public int addClassGroup(JSONObject object) {
		return insert("com.talkweb.timetable.dao.TimetableDao.addClassGroup", object);
	}

	@Override
	public List<JSONObject> getTeachingTaskCourse(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTeachingTaskCourse", object);
	}

	@Override
	public void addFixedClassGroup(List<JSONObject> list) {
		insert("com.talkweb.timetable.dao.TimetableDao.addFixedClassGroup", list);
	}

	@Override
	public List<String> getClassGroup(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getClassGroup",object);
	}

	@Override
	public List<JSONObject> getFixedClassGroup(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getFixedClassGroup", object);
	}

	@Override
	public int deleteClassGroup(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteClassGroup", object);
	}

	@Override
	public void updateClassGroupName(JSONObject object) {
		update("com.talkweb.timetable.dao.TimetableDao.updateClassGroupName",object);
	}

	@Override
	public int deleteClassFixed(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteClassFixed", object);
	}

	@Override
	public int addMonoWeek(JSONObject object) {
		return insert("com.talkweb.timetable.dao.TimetableDao.addMonoWeek",object);
	}

	@Override
	public int deleteMonoWeek(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteMonoWeek",object);
	}

	@Override
	public int updateMonoWeekClass(JSONObject object) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateMonoWeekClass",object);
	}

	@Override
	public int updateMonoWeek(List<JSONObject> list) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateMonoWeek",list);
	}

	@Override
	public List<JSONObject> getMonoWeekList(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getMonoWeekList",object);
	}

	@Override
	public List<JSONObject> getMonoTaskCourse(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getMonoTaskCourse",object);
	}

	@Override
	public List<JSONObject> getGroundRuleSum(Map<String, String> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getGroundRuleSum",map);
	}
	
	@Override
	public int insertGroundRule(JSONObject object) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertGroundRule",object);
	}

	@Override
	public int updateGroundRule(JSONObject object) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateGroundRule",object);
	}

	@Override
	public int deleteGroundRule(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteGroundRule",object);
	}
	
	@Override
	public List<JSONObject> getRoundruleCourse(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRoundruleCourse", object);
	}

	@Override
	public int deleteTimetableList(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteTimetableList", object);
	}

	@Override
	public int insertTimetableList(List<JSONObject> list) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertTimetableList", list);
	}

	@Override
	public List<JSONObject> getTimetableList(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTimetableList", object);
	}

	@Override
	public List<JSONObject> getTimetablePartList(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTimetablePartList", object);
	}

	@Override
	public List<String> getAdvanceCourses(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getAdvanceCourses",object);
	}
	
	@Override
	public List<JSONObject> getWalkthroughCourse(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getWalkthroughCourse", object);
	}

	@Override
	public List<JSONObject> getResearchWorksList(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getResearchWorksList", object);
	}

	@Override
	public int deleteResearchWork(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteResearchWork", object);
	}

	@Override
	public int deleteResearchFixed(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteResearchFixed", object);
	}

	@Override
	public int deleteResearchTeach(JSONObject object) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteResearchTeach", object);
	}

	@Override
	public int updateResearchWork(JSONObject object) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateResearchWork",object);
	}

	@Override
	public int addResearchFixed(List<JSONObject> teacherlist) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertResearchFixed", teacherlist);
	}

	@Override
	public int addResearchTeacher(List<JSONObject> fixedlist) {
		return insert("com.talkweb.timetable.dao.TimetableDao.insertResearchTeacher", fixedlist);
	}
	
	/**-----TAB页:课表APP查询-----**/
	@Override
	public List<JSONObject> getTimetableByDay(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTimetableByDay",object);
	} 
	
		@Override
	public JSONObject getArrangeTimetableInfo(Map<String, Object> map) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getArrangeTimetableInfo",map);
	}

	@Override
	public List<JSONObject> getTaskByTimetable(Map<String, Object> map) {
		//		
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTaskByTimetable", map);
	}
	
	@Override
	public List<JSONObject> getTaskTeacherByTaskId(Map<String, Object> map) {
		//		
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTaskTeacherByTaskId", map);
	}
	
	public List<JSONObject> getRuleCourseList(Map<String, Object> map) {
		//		
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleCourseList", map);
	}
	
	public List<JSONObject> getRuleCourseFixedByRuleId(Map<String, Object> map) {
		//
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleCourseFixedByRuleId", map);
	}

	@Override
	public List<JSONObject> getRuleTeacherList(Map<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleTeacherList", map);
	}

	@Override
	public List<JSONObject> getRuleTeacherFixedByRuleId(Map<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleTeacherFixedByRuleId", map);
	}

	@Override
	public List<JSONObject> getRuleClassGroupList(Map<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleClassList", map);
	}

	@Override
	public List<JSONObject> getRuleClassFixedByRuleId(Map<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleClassFixedByRuleId", map);
	}

	@Override
	public List<JSONObject> getResearchWorkList(Map<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getResearchWorkList", map);
	}

	@Override
	public List<JSONObject> getAdvanceArrangeList(Map<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getAdvanceArrangeList", map);
	}

	public boolean saveTimetableList(String schoolId, String timetableId,	List<Map<String, Object>> timeTableList, Set<String> gradeIds) {
		for (Map<String, Object> timeTableData : timeTableList) {
			this.insert("com.talkweb.timetable.dao.TimetableDao.saveTimetable", timeTableData);
		}
		
		Map<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put("schoolId", schoolId);
		mapParam.put("timetableId", timetableId);
		mapParam.put("gradeIds", gradeIds);
		//更新已发布的年级状态
		this.update("com.talkweb.timetable.dao.TimetableDao.updateGeneratedGrade", mapParam);
		
		return true;
	}
	

	public boolean saveTimetableBatch(String schoolId, String timetableId,	List<Map<String, Object>> timeTableList, Set<String> gradeIds) {
		SqlSessionTemplate sqlSessionTemplate = (SqlSessionTemplate)getSqlSession();
		SqlSession batchSqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
		try{
			List<Map<String, Object>> tempList = new ArrayList<Map<String,Object>>();
			for (Map<String, Object> timeTableData : timeTableList) {
				//每1000条提交一次
				if(tempList.size()!=0&&(tempList.size()) % 1000 == 0){
					tempList.add(timeTableData);
					batchSqlSession.insert("com.talkweb.timetable.dao.TimetableDao.saveTimetableBatch", tempList);
					batchSqlSession.commit();
					tempList.clear();
				}else{
					tempList.add(timeTableData);
				}
				
			}
			
			if(tempList.size()>0){
				batchSqlSession.insert("com.talkweb.timetable.dao.TimetableDao.saveTimetableBatch", tempList);
				batchSqlSession.commit();
			}
			
			Map<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("schoolId", schoolId);
			mapParam.put("timetableId", timetableId);
			mapParam.put("gradeIds", gradeIds);
			//更新已发布的年级状态
//			batchSqlSession.update("updateGeneratedGrade", mapParam);
			//更新课表主表为发布状态 //并更新微调校验状态为否
			batchSqlSession.update("com.talkweb.timetable.dao.TimetableDao.updateGeneratedTimetable", mapParam);
			batchSqlSession.commit();
			
			return true;
		}catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			batchSqlSession.rollback();
			return false;
		}finally{
			batchSqlSession.close();
		}
	
		
		
	}

	@Override
	public List<JSONObject> getArrangeGradeSetList(Map<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getArrangeGradeSetList", map);
	}

	@Override
	public List<JSONObject> getArrangeTimetableList(Map<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getArrangeTimetableList", map);
	}

	@Override
	public int addArrangeTimetable(Map<String, Object> map) {
		return insert("com.talkweb.timetable.dao.TimetableDao.addArrangeTimetable", map);
	}

	@Override
	public int deleteArrangeTimetable(Map<String, Object> map) {
		return delete("com.talkweb.timetable.dao.TimetableDao.deleteArrangeTimetable", map);
	}

	@Override
	public List<JSONObject> getTeacherTimetable(HashMap<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTeacherTimetable",map);
	}

	@Override
	public List<JSONObject> getMaxLessonNum(HashMap<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getMaxLessonNum",map);
	}

	@Override
	public List<JSONObject> getClassTimetable(HashMap<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getClassTimetable",map);
	}

	@Override
	public int clearTimetable(Map<String, Object> map) {
		return this.delete("com.talkweb.timetable.dao.TimetableDao.clearTimetable", map);
	}
	public int clearArrangeTimetable(Map<String, Object> map) {
		update("deleteAllTpAjust",map);
		return this.delete("com.talkweb.timetable.dao.TimetableDao.clearArrangeTimetable", map);
	}

	@Override
	public List<JSONObject> getGradeSet(HashMap<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getGradeSet",map);
	}

	@Override
	public List<JSONObject> getTimetablePie(HashMap<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTimetablePie",map);
	}

	@Override
	public List<JSONObject> getEvenOddWeekGroupList(String schoolId,
			String timetableId) {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("schoolId",schoolId);
		map.put("timetableId",timetableId);
		
		return selectList("com.talkweb.timetable.dao.TimetableDao.getEvenOddWeekGroupList",map);
	}

	@Override
	public List<JSONObject> getByTeacherTime(HashMap<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getByTeacherTime",map);
	}

	@Override
	public List<JSONObject> getTimeByTeacher(HashMap<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTimeByTeacher",map);
	}

	@Override
	public List<JSONObject> getTeacherResearch(HashMap<String, Object> map) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTeacherResearch",map);
	}

	@Override
	public List<JSONObject> getRuleTeachersInHasArrage(String schoolId,
			String timetableId) {
		HashMap<String,String> map  = new HashMap<String, String>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleTeachersInHasArrage",map);
	}
	
	@Override
	public int updateTimetablePrintSet(JSONObject object) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateTimetablePrintSet",object);
	}

	@Override
	public JSONObject getTimetablePrintSet(JSONObject object) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getTimetablePrintSet",object);
	}

	@Override
	public List<JSONObject> getTimetableGradeList(HashMap<String, Object> reqMap) {
		return  selectList("com.talkweb.timetable.dao.TimetableDao.getTimetableGradeList",reqMap);
	}

	@Override
	public List<JSONObject> getTimetableSubjectList(
			HashMap<String, Object> reqMap) {
		return  selectList("com.talkweb.timetable.dao.TimetableDao.getTimetableSubjectList",reqMap);
	}

	@Override
	public List<JSONObject> getTimetableTeacherList(
			HashMap<String, Object> reqMap) {
		return  selectList("com.talkweb.timetable.dao.TimetableDao.getTimetableTeacherList",reqMap);
	}
	
	@Override
	public List<String> getTaskClassList(JSONObject object) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTaskClassList",object);
	}

	@Override
	public JSONObject getTimetableForWeekList(HashMap<String, Object> cxMap) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getTimetableForWeekList",cxMap);
	}

	@Override
	public List<JSONObject> getTemporaryAjustList(HashMap<String, Object> cxMap) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTemporaryAjustList",cxMap);
	}

	@Override
	public <T> void insertMultiEntity(List<T> dataList) {
		if(dataList.size()>0){
			
			String cla = dataList.get(0).getClass().getSimpleName();
			update("batchInsert"+cla+"List",dataList);
		}
	}

	@Override
	public void deleteMultiTpTbRecord(
			List<TCRCATemporaryAdjustResult> needDeleteResult) {
		if(needDeleteResult.size()>0){
			
			update("com.talkweb.timetable.dao.TimetableDao.deleteMultiTpTbRecord",needDeleteResult);
		}
	}

	@Override
	public List<JSONObject> getTemporaryAjustRecord(
			HashMap<String, Object> cxMap) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTemporaryAjustRecord",cxMap);
	}

	@Override
	public void deleteMultiTmpRecord(List<String> needDelRecord) {
		if(needDelRecord.size()>0){
			
			update("com.talkweb.timetable.dao.TimetableDao.deleteMultiTmpRecord",needDelRecord);
		}
	}

	@Override
	public int updateAdjustRecordPublished(List<JSONObject> list) {
		int rs = 0;
		if(list.size()>0){
			for(JSONObject obj :list){
				rs += update("com.talkweb.timetable.dao.TimetableDao.updateAdjustRecordPublished",obj); 
			}
		}
		
		return rs;
	}
	@Override
	public int updateAdjustResultPublished(
			List<JSONObject> list) {
		// TODO Auto-generated method stub
		int rs = 0;
		if(list.size()>0){
			for(JSONObject obj :list){
				rs += update("com.talkweb.timetable.dao.TimetableDao.updateAdjustResultPublished",obj); 
			}
		}
		
		return rs;
	}

	@Override
	public void delMutilTmpAjstResult(HashMap<String, Object> cxMap) {
		update("com.talkweb.timetable.dao.TimetableDao.delMutilTmpAjstResult",cxMap);
	}

	@Override
	public void updateAutoArrangeResult(HashMap<String, Object> map) {
		update("com.talkweb.timetable.dao.TimetableDao.updateAutoArrangeResult",map);
	}

	@Override
	public int addArrangeTimetableBatch(List<JSONObject> ni) {
		
		return update("com.talkweb.timetable.dao.TimetableDao.addArrangeTimetableBatch",ni);
	}

	@Override
	public List<JSONObject> getDuplicatePubGrades(HashMap<String, Object> cxMap) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getDuplicatePubGrades",cxMap);
	}

	@Override
	public JSONObject getTimetableForAppClass(HashMap<String, String> cxMap) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getTimetableForAppClass", cxMap);
	}

	@Override
	public JSONObject getTimetableForAppClassList(HashMap<String, String> cxMap) {
		JSONObject o = selectOne("com.talkweb.timetable.dao.TimetableDao.getTimetableForAppClassList", cxMap);
		if(o!=null&&o.containsKey("TimetableId")&&cxMap.get("needClass").equalsIgnoreCase("1")){
			cxMap.put("timetableId", o.getString("TimetableId"));
			JSONObject keys = selectOne("com.talkweb.timetable.dao.TimetableDao.getTimetableForAppClassListSub",cxMap);
			o.put("classes", keys.get("classes"));
			o.put("courseIds", keys.get("courseIds"));
		}
		return o;
	}

	@Override
	public List<JSONObject> getRuleGroundList(HashMap<String, Object> cxMap) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getRuleGroundList",cxMap);
	}

	@Override
	public int deleteArrangeTimetableHigh(List<JSONObject> needDelete) {
		int num = 0;
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for(JSONObject del:needDelete){
			Map<String,Object> map= new HashMap<String, Object>();
			map.putAll(del);
			if(!del.containsKey("WalkGroupId")){
				map.put("WalkGroupId", null);
			}
			if(!del.containsKey("McGroupId")){
				map.put("McGroupId", null);
			}
			list.add(map);
			num += update("com.talkweb.timetable.dao.TimetableDao.deleteArrangeTimetableHigh",map);
		}
		int num2 = update("com.talkweb.timetable.dao.TimetableDao.insertArrangeTimetableHigh",list);
		return num;
	}

	@Override
	public int updateTimetableCheckStatus(HashMap<String, Object> cxMap) {
		return update("com.talkweb.timetable.dao.TimetableDao.updateTimetableCheckStatus",cxMap);
	}

	@Override
	public List<JSONObject> getTeacherGroupRules(String schoolId,
			String timetableId) throws Exception {
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTeacherGroupRules",cxMap);
	}

	@Override
	public List<JSONObject> getTeacherRelatedGroups(
			HashMap<String, Object> cxMap) throws Exception {		
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTeacherRelatedGroups",cxMap);
	}

	@Override
	public List<TCRCATemporaryAdjustGroupInfo> getTempAjustGroupList(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTempAjustGroupList",cxMap);
	}

	@Override
	public List<TCRCATemporaryAdjustGroupParam> getTempAjustGroupParam(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTempAjustGroupParam",cxMap);
	}

	@Override
	public TCRCATemporaryAdjustGroupInfo getSingleTempAjustGroup(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getSingleTempAjustGroup",cxMap);
	}

	@Override
	public List<TCRCATemporaryAdjustGroupParam> getSingleTempAjustGroupParam(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.timetable.dao.TimetableDao.getSingleTempAjustGroupParam",cxMap);
	}

	@Override
	public void updateCheckGroupExist(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		 update("com.talkweb.timetable.dao.TimetableDao.updateCheckGroupExist",cxMap);
	}

	@Override
	public void updateWeekAgoWeekOfEnd(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		cxMap.put("week", cxMap.get("curWeek"));
		List<TCRCATemporaryAdjustResult>  list = selectList("com.talkweb.timetable.dao.TimetableDao.getWeekAgoTb",cxMap);
		
		List<TCRCATemporaryAdjustResult> dataList = new ArrayList<TCRCATemporaryAdjustResult>();
		
		int weekOfEnd = (int) cxMap.get("groupWeekOfEnd");
		int groupCancleStep = 0;
		if(cxMap.containsKey("groupCancleStep")){
			groupCancleStep = (int) cxMap.get("groupCancleStep");
		}
		for(TCRCATemporaryAdjustResult re:list){
			String rew = re.getWeekOfEnd();
			boolean need = false;
			if(rew!=null&&rew.trim().length()>0){
				String[] ws = rew.split(",");
				if(groupCancleStep==0){
					for(int i=0;i<ws.length;i++){
						String wd = ws[i];
						if(wd!=null&&wd.trim().length()>0){
							int end = Integer.parseInt(wd);
							if(end>weekOfEnd){
								need = true;
								ws[i] = weekOfEnd+"";
							}
						}
					}
				}else{
					if(ws.length>=groupCancleStep){
						need = true;
						ws[groupCancleStep-1] = weekOfEnd+"";
					}else{
						for(int i=0;i<ws.length;i++){
							String wd = ws[i];
							if(wd!=null&&wd.trim().length()>0){
								int end = Integer.parseInt(wd);
								if(end>weekOfEnd){
									need = true;
									ws[i] = weekOfEnd+"";
								}
							}
						}
					}
				}
				if(need){
					String newEnd = "";
					for(int i=0;i<ws.length;i++){
						newEnd+= ws[i]+",";
					}
					newEnd = newEnd.substring(0,newEnd.length()-1);
					re.setWeekOfEnd(newEnd);
					dataList.add(re);
				}
			}
		}
		
		if(dataList.size()>0){
			
			update("com.talkweb.timetable.dao.TimetableDao.batchInsertTCRCATemporaryAdjustResultList",dataList);
		}
	}

	@Override
	public int updateTimetableSchedule(List<JSONObject> object) {
		// TODO Auto-generated method stub
		return update("com.talkweb.timetable.dao.TimetableDao.updateSchoolTimetableSchedule",object);
	}

	@Override
	public List<HashMap<String,Object>> queryTimetableSchedule(String timetableId,String pycc) {
		// TODO Auto-generated method stub
		JSONObject object = new JSONObject();
		object.put("timetableId", timetableId);
		object.put("pycc", pycc);
//		update("createTableTimetableSchedule");		
		return selectList("com.talkweb.timetable.dao.TimetableDao.getSchoolTimetableSchedule",object);
	}

	@Override
	public int insertTimetableSchedule(List<JSONObject> object) {
		// TODO Auto-generated method stub
		//确保插入前表已存在且不存在该数据
		//update("createTableTimetableSchedule");		
		update("com.talkweb.timetable.dao.TimetableDao.deleteSchoolTimetableSchedule",object.get(0));
		return update("com.talkweb.timetable.dao.TimetableDao.insertSchoolTimetableSchedule",object);
	}

	@Override
	public void deleteNoRecordAjst(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		update("com.talkweb.timetable.dao.TimetableDao.deleteNoRecordAjst",cxMap);
	}

	@Override
	public List<JSONObject> getTimetableByTeacherId(JSONObject object) {
		 
		return selectList("com.talkweb.timetable.dao.TimetableDao.getTimetableByTeacherId" , object);
	}

	@Override
	public List<JSONObject> getLatestTimetable(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return selectList("selectLatestPublishedTimetable", params);
	}

	@Override
	public List<JSONObject> getClassTimetableList(Map<String, Object> param) {
		return selectList("com.talkweb.timetable.dao.TimetableDao.getClassTimetableList", param);
	}

	@Override
	public JSONObject getTimetableDetailById(HashMap<String, Object> param) {
		return selectOne("com.talkweb.timetable.dao.TimetableDao.getTimetableDetailById",param);
	}
	

	@Override
	public List<JSONObject> getTimetableSections(JSONObject object) {
		return selectList("getTimetableSections", object);
	}
}