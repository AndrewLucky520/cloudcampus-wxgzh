package com.talkweb.placementtask.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.placementtask.dao.PlacementTaskDao;
import com.talkweb.placementtask.domain.OpenClassInfo;
import com.talkweb.placementtask.domain.OpenClassTask;
import com.talkweb.placementtask.domain.PlacementRule;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.StudentInfo;
import com.talkweb.placementtask.domain.TPlConfIndexSubs;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezyClassgroup;
import com.talkweb.placementtask.domain.TPlDezySettings;
import com.talkweb.placementtask.domain.TPlDezySubjectSet;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezySubjectgroup;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.domain.TPlMediumClasslevel;
import com.talkweb.placementtask.domain.TPlMediumSettings;
import com.talkweb.placementtask.domain.TPlMediumSubjectSet;
import com.talkweb.placementtask.domain.TPlMediumZhSet;
import com.talkweb.placementtask.domain.TeachingClassInfo;

@Repository
public class PlacementTaskDaoImpl extends MyBatisBaseDaoImpl implements PlacementTaskDao {
	Logger logger = LoggerFactory.getLogger(PlacementTaskDaoImpl.class);
	
	private static int INSERT_LIMIT = 1000;

	@Override
	public List<PlacementTask> queryPlacementTaskList(Map<String, Object> map){
		List<PlacementTask> list = selectList("queryPlacementTaskList", map);
		if(CollectionUtils.isEmpty(list)){
			return new ArrayList<PlacementTask>();
		} else {
			return list;
		}
	}
	
	@Override
	public PlacementTask queryPlacementTaskById(Map<String, Object> map){
		return selectOne("queryPlacementTaskById", map);
	}
	
	@Override
	public boolean ifExistsSamePlacementName(Map<String, Object> map){
		Integer result = selectOne("ifExistsSamePlacementName", map);
		if(result != null && result == 1){
			return true;
		}
		return false;
	}
	
	@Override
	public int insertOrUpdatePlacementTask(PlacementTask placementTask){
		return insert("insertOrUpdatePlacementTask", placementTask);
	}
	
	@Override
	public int updatePlacementTaskById(PlacementTask placementTask){
		return update("updatePlacementTaskById", placementTask);
	}
	
	@Override
	public int deletePlacementTask(Map<String, Object> map){
		return delete("deletePlacementTask", map);
	}
	
	@Override
	public List<OpenClassTask> queryOpenClassTasksWithInfo(Map<String, Object> map){
		List<OpenClassInfo> openClassInfos = queryOpenClassInfo(map);
		if(openClassInfos.size() == 0){
			return new ArrayList<OpenClassTask>();
		}
		Map<String, OpenClassInfo> infoId2Obj = new HashMap<String, OpenClassInfo>();
		for(OpenClassInfo opClassInfo : openClassInfos){
			infoId2Obj.put(opClassInfo.getOpenClassInfoId(), opClassInfo);
		}
		if(infoId2Obj.size() == 0){
			return new ArrayList<OpenClassTask>();
		}
		map.put("openClassInfos", openClassInfos);
		List<OpenClassTask> openClassTasks = queryOpenClassTask(map);
		map.remove("openClassInfos");
		for(OpenClassTask opClassTask : openClassTasks){
			String openClassInfoId = opClassTask.getOpenClassInfoId();
			if(!infoId2Obj.containsKey(openClassInfoId)){
				continue;
			}
			OpenClassInfo opClassInfo = infoId2Obj.get(openClassInfoId);
			opClassTask.setOpenClassInfo(opClassInfo);
			opClassInfo.getOpenClassTasks().add(opClassTask);
		}
		return openClassTasks;
	}
	
	@Override
	public List<OpenClassInfo> queryOpenClassInfosWithTask(Map<String, Object> map){
		List<OpenClassInfo> openClassInfos = queryOpenClassInfo(map);
		if(openClassInfos.size() == 0){
			return openClassInfos;
		}
		Map<String, OpenClassInfo> infoId2Obj = new HashMap<String, OpenClassInfo>();
		for(OpenClassInfo opClassInfo : openClassInfos){
			infoId2Obj.put(opClassInfo.getOpenClassInfoId(), opClassInfo);
		}
		if(infoId2Obj.size() == 0){
			return new ArrayList<OpenClassInfo>();
		}
		
		map.put("openClassInfos", openClassInfos);
		List<OpenClassTask> openClassTasks = queryOpenClassTask(map);
		map.remove("openClassInfos");
		for(OpenClassTask opClassTask : openClassTasks){
			String openClassInfoId = opClassTask.getOpenClassInfoId();
			if(!infoId2Obj.containsKey(openClassInfoId)){
				continue;
			}
			OpenClassInfo opClassInfo = infoId2Obj.get(openClassInfoId);
			if(opClassInfo.getOpenClassTasks() == null){
				opClassInfo.setOpenClassTasks(new ArrayList<OpenClassTask>());
			}
			opClassInfo.getOpenClassTasks().add(opClassTask);
			opClassTask.setOpenClassInfo(opClassInfo);
		}
		return openClassInfos;
	}
	
	@Override
	public List<OpenClassInfo> queryOpenClassInfo(Map<String, Object> map){
		List<OpenClassInfo> result = selectList("queryOpenClassInfo", map);
		if(result == null){
			return new ArrayList<OpenClassInfo>();
		}else{
			return result;
		}
	}
	
	@Override
	public List<OpenClassTask> queryOpenClassTask(Map<String, Object> map){
		List<OpenClassTask> result = selectList("queryOpenClassTask", map);
		if(result == null){
			return new ArrayList<OpenClassTask>();
		}else{
			return result;
		}
	}
	
	@Override
	public int deleteOpenClassInfo(Map<String, Object> map){
		return delete("deleteOpenClassInfo", map);
	}
	
	@Override
	public int deleteOpenClassTask(Map<String, Object> map){
		return delete("deleteOpenClassTask", map);
	}
	
	@Override
	public int insertOpenClassInfo(OpenClassInfo openClassInfo){
		return insert("insertOpenClassInfo", openClassInfo);
	}
	
	@Override
	public int insertOpenClassInfoBatch(List<OpenClassInfo> openClassInfos, String termInfo){
		int result = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("termInfo", termInfo);
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = openClassInfos.size() / INSERT_LIMIT;
		for(int batch = 0; batch <= batchSize; batch ++){
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > openClassInfos.size() ? openClassInfos.size() : toIndex;
			map.put("openClassInfos", openClassInfos.subList(fromIndex, toIndex));
			result += insert("insertOpenClassInfoBatch", map);
		}
		return result;
	}
	
	@Override
	public int insertOpenClassTask(OpenClassTask openClassTask){
		return insert("insertOpenClassTask", openClassTask);
	}
	
	@Override
	public int insertOpenClassTaskBatch(List<OpenClassTask> openClassTasks, String termInfo){
		int result = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("termInfo", termInfo);
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = openClassTasks.size() / INSERT_LIMIT;
		for(int batch = 0; batch <= batchSize; batch ++){
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > openClassTasks.size() ? openClassTasks.size() : toIndex;
			map.put("openClassTasks", openClassTasks.subList(fromIndex, toIndex));
			result += insert("insertOpenClassTaskBatch", map);
		}
		return result;
	}
	
	@Override
	public int updateOpenClassInfo(OpenClassInfo openClassInfo){
		return update("updateOpenClassInfo", openClassInfo);
	}
	
	@Override
	public int updateOpenClassTask(OpenClassTask openClassTask){
		return update("updateOpenClassTask", openClassTask);
	}
	
	@Override
	public List<PlacementRule> queryRuleInfo(Map<String, Object> map){
		List<PlacementRule> result = selectList("queryRuleInfo", map);
		if(result == null){
			return new ArrayList<PlacementRule>();
		}else{
			return result;
		}
	}
	
	@Override
	public int insertPlacementRule(PlacementRule placementRule){
		return insert("insertPlacementRule", placementRule);
	}
	
	@Override
	public int deletePlacementRule(Map<String, Object> map){
		return delete("deletePlacementRule", map);
	}
	
	@Override
	public List<TeachingClassInfo> queryTeachingClassInfo(Map<String, Object> map) {
		int placementType = (int) map.get("placementType");
		List<TeachingClassInfo> tClassInfos = null;
		if(placementType==4||placementType==3){
			tClassInfos = new ArrayList<TeachingClassInfo>();
			List<TPlDezyClass> tplist = this.getDezyClassList(map);
			for(TPlDezyClass dc:tplist){
				TeachingClassInfo tc = new TeachingClassInfo();
				tc.setNumOfStuds(dc.getTclassNum());
				tc.setPlacementId(dc.getPlacementId());
				tc.setSchoolId(Long.valueOf( dc.getSchoolId()));
				tc.setTeachingClassId(dc.getTclassId());
				tc.setTeachingClassName(dc.getTclassName());
				tc.setUsedGrade(dc.getUsedGrade());
				tClassInfos.add(tc);
			}
		}else{
			tClassInfos = selectList("queryTeachingClassInfo", map);
		}
		
		if(tClassInfos == null) {
			tClassInfos = new ArrayList<TeachingClassInfo>();
		}
		return tClassInfos;
	}
	
	@Override
	public List<TeachingClassInfo> queryTeachingClassInfoWithAll(Map<String, Object> map) {
		List<OpenClassTask> opClassTaskList = queryOpenClassTasksWithInfo(map);
		Map<String, OpenClassTask> opClassTaskId2Obj = new HashMap<String, OpenClassTask>();
		for(OpenClassTask opClassTask : opClassTaskList) {
			opClassTaskId2Obj.put(opClassTask.getOpenClassTaskId(), opClassTask);
		}
		if(opClassTaskId2Obj.size() == 0){
			return new ArrayList<TeachingClassInfo>();
		}
		
		if(!map.containsKey("openClassTaskIds")){
			map.put("openClassTaskIds", opClassTaskId2Obj.keySet());
		}
		List<TeachingClassInfo> tClassInfoList = queryTeachingClassInfo(map);
		for(TeachingClassInfo tClassInfo : tClassInfoList) {
			OpenClassTask opClassTask = opClassTaskId2Obj.get(tClassInfo.getOpenClassTaskId());
			tClassInfo.setOpenClassTask(opClassTask);
		}
		map.remove("openClassTaskIds");
		return tClassInfoList;
	}

	@Override
	public Boolean ifExistsSameTClassName(Map<String, Object> map){
		Integer result = selectOne("ifExistsSameTClassName", map);
		if(result == null) {
			return false;
		}
		return true;
	}

	@Override
	public int updateTeachingClassInfo(Map<String, Object> map){
		return update("updateTeachingClassInfo", map);
	}
	
	@Override
	public int insertTeachingClassInfoBatch(List<TeachingClassInfo> teachingClassInfos, String termInfo) {
		int result = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("termInfo", termInfo);
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = teachingClassInfos.size() / INSERT_LIMIT;
		for(int batch = 0; batch <= batchSize; batch ++){
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > teachingClassInfos.size() ? teachingClassInfos.size() : toIndex;
			map.put("teachingClassInfos", teachingClassInfos.subList(fromIndex, toIndex));
			result += insert("insertTeachingClassInfoBatch", map);
		}
		return result;
	}
	
	@Override
	public int deleteTeachingClassInfo(Map<String, Object> map) {
		return delete("deleteTeachingClassInfo", map);
	}
	
	@Override
	public List<StudentInfo> queryStudentInfo(Map<String, Object> map){
		List<StudentInfo> list = selectList("queryStudentInfo", map);
		if(list == null){
			list = new ArrayList<StudentInfo>();
		}
		return list;
	}
	
	@Override
	public List<StudentInfo> queryStudentInfoWithAll(Map<String, Object> map){
		List<TeachingClassInfo> tClassInfoList = queryTeachingClassInfo(map);
		Map<String, TeachingClassInfo> tClassInfoId2Obj = new HashMap<String, TeachingClassInfo>();
		for(TeachingClassInfo tClassInfo : tClassInfoList) {
			tClassInfoId2Obj.put(tClassInfo.getTeachingClassId(), tClassInfo);
		}
		if(tClassInfoId2Obj.size() == 0){
			return new ArrayList<StudentInfo>();
		}
		
		map.put("teachingClassIds", tClassInfoId2Obj.keySet());
		List<StudentInfo> studInfoList = queryStudentInfo(map);
		for(StudentInfo studInfo : studInfoList) {
			studInfo.setTeachingClassInfo(tClassInfoId2Obj.get(studInfo.getTeachingClassId()));
			studInfo.setOpenClassTask(studInfo.getTeachingClassInfo().getOpenClassTask());
		}
		map.remove("teachingClassIds");
		return studInfoList;
	}
	
	@Override
	public int insertStudentInfoBatch(List<StudentInfo> studentInfos, String termInfo){
		int result = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("termInfo", termInfo);
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = studentInfos.size() / INSERT_LIMIT;
		for(int batch = 0; batch <= batchSize; batch ++){
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > studentInfos.size() ? studentInfos.size() : toIndex;
			map.put("studentInfos", studentInfos.subList(fromIndex, toIndex));
			result += insert("insertStudentInfoBatch", map);
		}
		return result;
	}
	
	@Override
	public int deleteStudentInfo(Map<String, Object> map){
		return delete("deleteStudentInfo", map);
	}
	
	@Override
	public List<Float> validateScoreLayerLarge(Map<String, Object> map) {
		return selectList("validateScoreLayerLarge", map);
	}
	
	@Override
	public List<StudentInfo> queryStudentInfoWaitForPlacement(Map<String, Object> map){
		List<StudentInfo> list = selectList("queryStudentInfoWaitForPlacement", map);
		if(list == null){
			list = new ArrayList<StudentInfo>();
		}
		return list;
	}
	
	@Override
	public List<StudentInfo> queryStudentInfoWaitForPlacementWithAll(Map<String, Object> map) {
		List<OpenClassTask> opClassTaskList = queryOpenClassTask(map);
		Map<String, OpenClassTask> opClassTaskId2Obj = new HashMap<String, OpenClassTask>();
		for(OpenClassTask opClassTask : opClassTaskList) {
			opClassTaskId2Obj.put(opClassTask.getOpenClassTaskId(), opClassTask);
		}
		if(opClassTaskId2Obj.size() == 0){
			return new ArrayList<StudentInfo>();
		}
		
		map.put("openClassTaskIds", opClassTaskId2Obj.keySet());
		List<StudentInfo> studInfoList = queryStudentInfoWaitForPlacement(map);
		for(StudentInfo studInfo : studInfoList) {
			studInfo.setOpenClassTask(opClassTaskId2Obj.get(studInfo.getOpenClassTaskId()));
		}
		map.remove("openClassTaskIds");
		return studInfoList;
	}
	
	@Override
	public int insertStudentInfoWaitForPlacementBatch(List<StudentInfo> studentInfos, String termInfo){
		int result = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("termInfo", termInfo);
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = studentInfos.size() / INSERT_LIMIT;
		for(int batch = 0; batch <= batchSize; batch ++){
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > studentInfos.size() ? studentInfos.size() : toIndex;
			map.put("studentInfos", studentInfos.subList(fromIndex, toIndex));
			result += insert("insertStudentInfoWaitForPlacementBatch", map);
		}
		return result;
	}
	
	@Override
	public int deleteStudentInfoWaitForPlacement(Map<String, Object> map){
		return delete("deleteStudentInfoWaitForPlacement", map);
	}
	
	
	
	
	
	@Override
	public Set<String> getPlacementTaskSchemas(String prefixSchema) {
		List<String> result = selectList("getPlacementTaskSchemas", prefixSchema);
		if(result == null){
			return new HashSet<String>();
		}
		return new HashSet<String>(result);
	}
	
	@Override
	public List<JSONObject> getPlacementTaskDropDownList(Map<String, Object> map){
		List<JSONObject> list = selectList("getPlacementTaskDropDownList", map);
		if(list == null){
			list = new ArrayList<JSONObject>();
		}
		return list;
	}

	
	
	@Override
	public TPlDezySettings getTPlDezySet(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectOne("com.talkweb.placementtask.dao.getTPlDezySet",map);
	}

	@Override
	public List<TPlDezySubjectgroup> getSubjectGroupList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getSubjectGroupList",map);
	}

	@Override
	public List<TPlDezySubjectSet> getSubjectSetList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getSubjectSetList",map);
	}

	@Override
	public List<String> getSubjectGroupIDList(Map<String, Object> map){
		return selectList("com.talkweb.placementtask.dao.getSubjectGroupIDList",map);
	}
	
	@Override
	public List<TPlDezyClassgroup> getDezyClassGroupList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getDezyClassGroupList",map);
	}

	@Override
	public List<TPlDezyClass> getDezyClassList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getDezyClassList",map);
	}

	@Override
	public List<TPlDezySubjectcomp> getSubjectcompList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getSubjectcompList",map);
	}

	@Override
	public List<TPlDezySubjectcompStudent> getSubjectcompStuList(
			Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getSubjectcompStuList",map);
	}

	@Override
	public List<TPlDezyTclassSubcomp> getTclassSubcompList(
			Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getTclassSubcompList",map);
	}

	@Override
	public List<TPlDezyTclassfrom> getTclassFromList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getTclassFromList",map);
	}

/*	@Override
	public List<TPlDezyTclassfrom> getStusubjectCompList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getTclassFromList",map);
	}*/
	@Override
	public void updateDezyResult(HashMap<String,Object> cxmap) throws Exception{
		update("com.talkweb.placementtask.dao.deleteDezyResult",cxmap);
	}

	@Override
	public int updateDivProc(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return update("com.talkweb.placementtask.dao.updateDivProc", map);
	}

	@Override
	public List<TPlConfIndexSubs> getConfIndexSubsList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getConfIndexSubsList",map);
	}

	@Override
	public List<String> selectUsedWfId(Map<String,Object> map) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.selectUsedWfId",map);
	}
	
	@Override
	public List<JSONObject> getByZhStudentToThirdByAccountId(Map<String,String> map) {
		return selectList("com.talkweb.placementtask.dao.getByZhStudentToThirdByAccountId",map);
	}
	
	@Override
	public List<JSONObject> getZhStudentToThirdByAccountId(Map<String,String> map) {
		return selectList("com.talkweb.placementtask.dao.getZhStudentToThirdByAccountId",map);
	}
	
	@Override
	public String getWfInfo(Map<String,Object> map){
		return selectOne("com.talkweb.placementtask.dao.getWfInfo",map);
	}
	
	@Override
	public List<JSONObject> getStudentClassInfo(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getStudentClassInfo",map);
	}
	
	@Override
	public List<JSONObject> getOldClassInfo(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getOldClassInfo",map);
	}
	
	@Override
	public List<String> getplacementInfoByWfid(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getplacementInfoByWfid",map);
	}
	
	@Override
	public List<JSONObject> getSubjectcompInfoByAccountId(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getSubjectcompInfoByAccountId",map);
	}
	
	@Override
	public List<TPlDezyClass> getDezyClassListByClassIds(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getDezyClassListByClassIds",map);
	}
	
	@Override
	public int updateTPlDezyClassNum(Map<String,Object> map){
		return update("com.talkweb.placementtask.dao.updateTPlDezyClassNum", map);
	}
	
	@Override
	public int deleteTPlDezyClass(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlDezyClass", map);
	}
	
	@Override
	public int deleteTPlDezyTclassfrom(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlDezyTclassfrom", map);
	}
	
	@Override
	public int deleteTPlDezyclassfrom(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlDezyclassfrom", map);
	}
	
	@Override
	public int deleteTPlDezySubjectcomp(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlDezySubjectcomp", map);
	}
	
	@Override
	public int deleteTPlDezyTclassSubcomp(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlDezyTclassSubcomp", map);
	}
	
	@Override
	public int updateTPlDezySubjectcomp(Map<String,Object> map){
		return update("com.talkweb.placementtask.dao.updateTPlDezySubjectcomp",map);
	}
	
	@Override
	public int deleteTPlDezySubjectcompStudentById(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlDezySubjectcompStudentById",map);
	}
	
	@Override
	public String getCurrenTermInfo(){
		return selectOne("com.talkweb.placementtask.dao.getCurrenTermInfo");
	}
	
	@Override
	public List<JSONObject> countIdByWfid(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.countIdByWfid",map);
	}
	
	@Override
	public List<JSONObject> countByIdByWfid(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.countByIdByWfid",map);
	}
	
	@Override
	public void updateTPlDezySubjectSet(Map<String,Object> map){
		update("com.talkweb.placementtask.dao.updateTPlDezySubjectSet",map);
	}
	
	@Override
	public JSONObject getOpenClassByAccountId(Map<String,Object> map){
		return selectOne("com.talkweb.placementtask.dao.getOpenClassByAccountId",map);
	}
	
	@Override
	public int deleteTPlOpenclasstask(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlOpenclasstask",map);
	}
	
	@Override
	public int deleteTPlOpenclassinfo(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlOpenclassinfo",map);
	}
	
	@Override
	public int updateTPlOpenclasstask(Map<String,Object> map){
		return update("com.talkweb.placementtask.dao.updateTPlOpenclasstask",map);
	}
	
	@Override
	public int deleteTPlTeachingclassinfo(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlTeachingclassinfo",map);
	}
	
	@Override
	public int deleteTPlStudentinfo(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlStudentinfo",map);
	}
	
	@Override
	public int updateTPlTeachingclassinfo(Map<String,Object> map){
		return update("com.talkweb.placementtask.dao.updateTPlTeachingclassinfo",map);
	}
	
	@Override
	public String queryGender(Map<String,Object> map){
		return selectOne("com.talkweb.placementtask.dao.queryGender",map);
	}
	
	@Override
	public List<String> getplacementIdByWfid(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getplacementIdByWfid",map);
	}
	
	@Override
	public String getopenClassTaskId(Map<String,Object> map){
		return selectOne("com.talkweb.placementtask.dao.getopenClassTaskId",map);
	}
	
	@Override
	public void deleteTPlStudentinfowaitforplacement(Map<String,Object> map){
		delete("com.talkweb.placementtask.dao.deleteTPlStudentinfowaitforplacement",map);
	}
	
	@Override
	public int insertTPlStudentinfowaitforplacement(Map<String,Object> map){
		return insert("com.talkweb.placementtask.dao.insertTPlStudentinfowaitforplacement",map);
	}
	
	@Override
	public List<JSONObject> getClassToTClassRelation(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getClassToTClassRelation",map);
	}
	
	@Override
	public List<TPlDezyClass> getTPlDezyClassBySubjectCompId(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getTPlDezyClassBySubjectCompId",map);
	}
	
	@Override
	public List<TPlMediumSettings> getTPlMediumSettings(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getTPlMediumSettings",map);
	}
	
	@Override
	public List<TPlMediumZhSet> getTPlMediumZhSet(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getTPlMediumZhSet",map);
	}
	
	@Override
	public List<TPlMediumClasslevel> getTPlMediumClasslevel(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getTPlMediumClasslevel",map);
	}
	
	@Override
	public List<TPlMediumSubjectSet> getTPlMediumSubjectSet(Map<String,Object> map){
		return selectList("com.talkweb.placementtask.dao.getTPlMediumSubjectSet",map);
	}
	
	@Override
	public int deleteTPlMediumZhSet(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlMediumZhSet",map);
	}
	
	@Override
	public int insertTPlMediumZhSet(String termInfo, List<TPlMediumZhSet> list){
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.insertTPlMediumZhSet", params);
	}
	
	@Override
	public int updateTPlMediumSettings(TPlMediumSettings TtPlMediumSettings){
		return update("com.talkweb.placementtask.dao.updateTPlMediumSettings", TtPlMediumSettings);
	}
	
	@Override
	public int insertTPlMediumSettings(TPlMediumSettings TtPlMediumSettings){
		return insert("com.talkweb.placementtask.dao.insertTPlMediumSettings", TtPlMediumSettings);
	}
	
	@Override
	public int deleteTPlMediumClasslevel(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlMediumClasslevel",map);
	}
	
	@Override
	public int insertTPlMediumClasslevel(String termInfo, List<TPlMediumClasslevel> list){
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.insertTPlMediumClasslevel", params);
	}
	
	@Override
	public int deleteTPlMediumSubjectSet(Map<String,Object> map){
		return delete("com.talkweb.placementtask.dao.deleteTPlMediumSubjectSet",map);
	}
	
	@Override
	public int insertTPlMediumSubjectSet(String termInfo, List<TPlMediumSubjectSet> list){
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.insertTPlMediumSubjectSet", params);
	}
}
