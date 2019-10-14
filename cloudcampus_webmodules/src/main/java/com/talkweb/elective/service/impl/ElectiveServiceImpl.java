package com.talkweb.elective.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringNumTool;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.elective.dao.ElectiveDao;
import com.talkweb.elective.service.ElectiveService;
import com.talkweb.filemanager.service.FileServer;
@Service
public class ElectiveServiceImpl implements ElectiveService{

	@Autowired
	private ElectiveDao electiveDao;
   
    @Autowired
    private AllCommonDataService allCommonDataService;
    
    @Autowired
	private FileServer fileServerImplFastDFS;
    
	private static final Logger logger = LoggerFactory.getLogger(ElectiveServiceImpl.class);
    
    /**
	 * redis
	 */
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	@Override
	public List<JSONObject> getCourseTypeList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCourseTypeList(map);
	}

	@Override
	public List<JSONObject> getElectiveListByGrade(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveListByGrade(map);
	}

	@Override
	public String getClassListByCourse(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getClassListByCourse(map);
	}

	@Override
	public List<JSONObject> getAdminElectiveList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getAdminElectiveList(map);
	}

	@Override
	public int createElective(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.createElective(map);
	}

	@Override
	public int updateElective(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.updateElective(map);
	}

	@Override
	public int updateElectiveTime(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.updateElectiveTime(map);
	}

	@Override
	public int deleteElective(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.deleteElective(map);
	}

	@Override
	public List<JSONObject> getElectiveCourse(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveCourse(map);
	}

	@Override
	public List<JSONObject> getElectiveCourseSchoolTime(
			HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveCourseSchoolTime(map);
	}

	@Override
	public JSONObject createElectiveCourse(HashMap<String, Object> map,
			List<JSONObject> classList, List<JSONObject> teacherList,
			List<JSONObject> schoolTimeList) throws Exception {
		JSONObject json = new JSONObject();
		json.put("msg", "保存成功");
		json.put("code", 0);
		// TODO Auto-generated method stub
		String schoolId=(String) map.get("schoolId");
		String electiveId=(String) map.get("electiveId");
		String courseId=(String) map.get("courseId");
		String maxNum=(String) map.get("maxNum");
		String classMaxNum=(String) map.get("classMaxNum");
		int num=electiveDao.createElectiveCourse(map,classList,teacherList,schoolTimeList);
		if(num>0){
			//redis缓存修改
			String isInitKey="elective."+schoolId+"."+electiveId+".isInit";
			String keyNX = "elective." + schoolId + "." + electiveId + "." + courseId;
			Object isInitObj = redisOperationDAO.get(isInitKey);
			Boolean hasSuccessUpdateRedis=false;
			if(isInitObj!=null){
				logger.info("[elective]update course isInitObj={} isInitKey={} keyNX={} courseId={} eleciveId={}",isInitObj,isInitKey,keyNX,courseId,electiveId);
				for(int i=0;i<10;i++){
					Boolean isSet = redisOperationDAO.setNX(keyNX, 1);
					if (!isSet) {
						Thread.sleep(10);
						logger.info("[elective]update course，wait 10ms");
						continue;
					}
					try {
						//得到资源后
						redisOperationDAO.set(keyNX, 1,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
						
						//redis中有数据则修改对应的班级 和课程人数上限
						HashMap<String,Integer> redisMap = new HashMap<String,Integer>();
						for(JSONObject cObj:classList){
							// 课程人数上限
							String courseKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + cObj.getString("courseId") + ".MaxValue";
							// 班级课程人数上限
							String courseClassKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + cObj.getString("courseId") + "." + cObj.getString("classId")+ ".MaxValue";
							redisMap.put(courseKeyMaxValue,Integer.valueOf(maxNum) );
							if(StringUtils.isBlank(classMaxNum)){
								redisMap.put(courseClassKeyMaxValue, Integer.MAX_VALUE);
							}else{
								redisMap.put(courseClassKeyMaxValue, Integer.valueOf(classMaxNum));
							}
						}
						logger.info("[elective]update course get lock and redis update  redisMap={} electiveId={}",redisMap,electiveId);
						redisOperationDAO.multiSet(redisMap);
						hasSuccessUpdateRedis=true;
						break;
					}finally {
						logger.info("[elective]update course:del lock");
						redisOperationDAO.del(keyNX);
					}
					
					
				}
				
				
			}
			if(isInitObj!=null &&!hasSuccessUpdateRedis){
				//json.put("code", -1);
				//json.put("msg", "有学生正在选课中，设置失败，请稍候重试！");
				logger.info("[elective]update course isInitObj={} hasSuccessUpdateRedis={} electiveId={}",isInitObj,hasSuccessUpdateRedis,electiveId);
				throw new  Exception("lock over time");
				//return json;
			}
		}
		else if(num<=0)
        {
			json.put("code", -1);
			json.put("msg", "保存失败！");	
		}
		return json;
	}

	@Override
	public JSONObject updateElectiveCourse(HashMap<String, Object> map,
			List<JSONObject> classList, List<JSONObject> teacherList,
			List<JSONObject> schoolTimeList)throws Exception {
		JSONObject json = new JSONObject();
		json.put("msg", "保存成功");
		json.put("code", 0);
		String schoolId=(String) map.get("schoolId");
		String electiveId=(String) map.get("electiveId");
		String courseId=(String) map.get("courseId");
		String maxNum=(String) map.get("maxNum");
		String classMaxNum=(String) map.get("classMaxNum");
		int num= electiveDao.updateElectiveCourse(map, classList, teacherList, schoolTimeList);
		if(num>0){
			//redis缓存修改
			String isInitKey="elective."+schoolId+"."+electiveId+".isInit";
			String keyNX = "elective." + schoolId + "." + electiveId + "." + courseId;
			Object isInitObj = redisOperationDAO.get(isInitKey);
			Boolean hasSuccessUpdateRedis=false;
			if(isInitObj!=null){
				logger.info("[elective]update course-redis is update：isInitObj={} isInitKey={} keyNX={} courseId={} eleciveId={}",isInitObj,isInitKey,keyNX,courseId,electiveId);
				for(int i=0;i<10;i++){
					Boolean isSet = redisOperationDAO.setNX(keyNX, 1);
					if (!isSet) {
						Thread.sleep(10);
						logger.info("[elective]has not lock,wait 10ms");
						continue;
					}
					try {
						//得到资源后
						redisOperationDAO.set(keyNX, 1,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			
						//redis中有数据则修改对应的班级 和课程人数上限
						HashMap<String,Integer> redisMap = new HashMap<String,Integer>();
						for(JSONObject cObj:classList){
							// 课程人数上限
							String courseKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + cObj.getString("courseId") + ".MaxValue";
							// 班级课程人数上限
							String courseClassKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + cObj.getString("courseId") + "." + cObj.getString("classId")+ ".MaxValue";
							redisMap.put(courseKeyMaxValue,Integer.valueOf(maxNum) );
							if(StringUtils.isBlank(classMaxNum)){
								redisMap.put(courseClassKeyMaxValue, Integer.MAX_VALUE);
							}else{
								redisMap.put(courseClassKeyMaxValue, Integer.valueOf(classMaxNum));
							}
						}
						logger.info("[elective]update course，get lock and redis update, electiveId={} redisMap={} ",electiveId,redisMap);
						redisOperationDAO.multiSet(redisMap);
						hasSuccessUpdateRedis=true;
						break;
					}finally {
						logger.info("[elective]update course del lock ");
						redisOperationDAO.del(keyNX);
					}
					
					
				}
				
				
			}
			if(isInitObj!=null &&!hasSuccessUpdateRedis){
				//json.put("code", -1);
				//json.put("msg", "有学生正在选课中，设置失败，请稍候重试！");
				logger.info("[elective]update course isInitObj={} hasSuccessUpdateRedis={} electiveId={}",isInitObj,hasSuccessUpdateRedis,electiveId);
				throw new  Exception("lock over time");
				//return json;
			}
		}
		else if(num<=0)
        {
			json.put("code", -1);
			json.put("msg", "保存失败！");	
		}
		return json;
	}

	@Override
	public int deleteElectiveCourse(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.deleteElectiveCourse(map);
	}

	@Override
	public String getElectiveCourseTeacher(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveCourseTeacher(map);
	}

	@Override
	public String getElectiveCourseClass(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveCourseClass(map);
	}

	@Override
	public List<JSONObject> getElectiveCourseRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveCourseRequire(map);
	}

	@Override
	public int updateElectiveCourseRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.updateElectiveCourseRequire(map);
	}

	@Override
	public int deleteElectiveCourseRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.deleteElectiveCourseRequire(map);
	}

	@Override
	public int batchUpdateElectiveCourseRequire(List<JSONObject> list) {
		// TODO Auto-generated method stub
		return electiveDao.batchUpdateElectiveCourseRequire(list);
	}

	@Override
	public int batchDeleteElectiveCourseRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.batchDeleteElectiveCourseRequire(map);
	}

//	@Override
//	public List<JSONObject> getCourseNameByCourseSortId(
//			HashMap<String, Object> map) {
//		// TODO Auto-generated method stub
//		return electiveDao.getCourseNameByCourseSortId(map);
//	}

	@Override
	public List<JSONObject> getCourseSort(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCourseSort(map);
	}

	@Override
	public int updateElectiveCourseType(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.updateElectiveCourseType(map);
	}

	@Override
	public int insertElectiveCourseType(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.insertElectiveCourseType(map);
	}

	@Override
	public List<JSONObject> getElectiveCourseList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveCourseList(map);
	}

	@Override
	public int clearElectiveCourseType(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.clearElectiveCourseType(map);
	}

	@Override
	public int deleteElectiveCourseType(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.deleteElectiveCourseType(map);
	}

//	@Override
//	public int deleteCourseTypeRequire(HashMap<String, Object> map) {
//		// TODO Auto-generated method stub
//		return electiveDao.deleteCourseTypeRequire(map);
//	}
	@Override
	public List<JSONObject> getCourseTypeNumList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCourseTypeNumList(map);
	}

	@Override
	public int batchCreateCourseTypeNum(List<JSONObject> list) {
		// TODO Auto-generated method stub
		return electiveDao.batchCreateCourseTypeNum(list);
	}

	@Override
	public int updateSingeCourseTypeNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.updateSingeCourseTypeNum(map);
	}

	@Override
	public int deleteCourseTypeNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.deleteCourseTypeNum(map);
	}

	@Override
	public List<JSONObject> getAjustElectiveList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getAjustElectiveList(map);
	}

	@Override
	public List<JSONObject> getNoSelectedCourseStudentList(
			HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getNoSelectedCourseStudentList(map);
	}

	@Override
	public int insertElectiveStudent(List<JSONObject> list,HashMap<String,Object> map)throws Exception {
		String schoolId = (String) map.get("schoolId");
		String electiveId = (String) map.get("electiveId");
		String courseId = (String) map.get("courseId");
		String termInfo= (String) map.get("termInfo");
		//HashMap<String,String> scMap=(HashMap<String, String>) map.get("scMap");
		//List<String> studentIdList = (List<String>) map.get("studentIdList");
		int returnInt = electiveDao.insertElectiveStudent(list);
		//操作redis的已选课人数
    	//redis缓存修改
		if(returnInt>0){
			String isInitKey="elective."+schoolId+"."+electiveId+".isInit";
			String keyNX = "elective." + schoolId + "." + electiveId + "." + courseId;
			Object isInitObj = redisOperationDAO.get(isInitKey);
			Boolean hasSuccessUpdateRedis=false;
			if(isInitObj!=null){
				logger.info("[elective]update course-redis is update：isInitObj={} isInitKey={} keyNX={} courseId={} eleciveId={}",isInitObj,isInitKey,keyNX,courseId,electiveId);
				for(int i=0;i<10;i++){
					Boolean isSet = redisOperationDAO.setNX(keyNX, 1);
					if (!isSet) {
						Thread.sleep(10);
						logger.info("[elective]has not lock,wait 10ms");
						continue;
					}
					try {
						//得到资源后
						redisOperationDAO.set(keyNX, 1,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			
						//redis中有数据则修改对应的班级 和课程人数上限
						HashMap<String,Integer> redisMap = new HashMap<String,Integer>();
						for(JSONObject obj:list){
							String classId = obj.getString("classId");
							if(StringUtils.isBlank(schoolId) || StringUtils.isBlank(electiveId) || StringUtils.isBlank(courseId) ||  StringUtils.isBlank( obj.getString("classId"))){
								continue;
							}
							// 课程人数上限
							String courseKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + courseId + ".MaxValue";
							// 班级课程人数上限
							String courseClassKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + courseId + "." +  classId+ ".MaxValue";
							// 已选课程人数
							String courseKeyValue = "elective." + schoolId + "." + electiveId + "." + courseId + ".Value";
							// 已选班级课程人数
							String courseClassKeyValue = "elective." + schoolId + "." + electiveId + "." + courseId + "." +  classId+ ".Value";
							
							HashMap<String, Object> courseMap = new HashMap<String, Object>();
							courseMap.put("schoolId", schoolId);
							courseMap.put("electiveId", electiveId);
							courseMap.put("classId", classId);
							courseMap.put("courseId", courseId);
							courseMap.put("termInfo", termInfo);
							logger.info("[elective]#########courseMap:{}",courseMap);
							List<JSONObject> maxList=electiveDao.getCourseNumForInit(courseMap);
							List<JSONObject> ccvList = electiveDao.getCourseClassNum(courseMap);
							List<JSONObject> cvList=electiveDao.getCourseSelectedNum(courseMap);
							if(null!=maxList&& maxList.size()>0){
								JSONObject c =maxList.get(0);
								redisMap.put(courseKeyMaxValue, c.getIntValue("upperLimit"));
								redisMap.put(courseKeyValue, 0); //初始化为0
								redisMap.put(courseClassKeyValue,0);//初始化为0
								String classMaxNum = c.getString("classMaxNum");
								if (null != classMaxNum && !"不限".equals(classMaxNum)) {
									redisMap.put(courseClassKeyMaxValue, Integer.valueOf(classMaxNum));
								} else {
									redisMap.put(courseClassKeyMaxValue,  Integer.MAX_VALUE);
								}
							}
							if(null!=ccvList&& ccvList.size()>0){
								JSONObject c=ccvList.get(0);
								redisMap.put(courseClassKeyValue, c.getIntValue("selectedNum"));
							}
							if(null!=cvList&& cvList.size()>0){
								JSONObject c=cvList.get(0);
								redisMap.put(courseKeyValue,c.getIntValue("selectedNum"));
							}
						}
						logger.info("[elective]update course，get lock and redis update, electiveId={} redisMap={} ",electiveId,redisMap);
						redisOperationDAO.multiSet(redisMap);
						hasSuccessUpdateRedis=true;
						break;
					}finally {
						logger.info("[elective]update course del lock ");
						redisOperationDAO.del(keyNX);
					}
				}
				if(isInitObj!=null &&!hasSuccessUpdateRedis){
					throw new  Exception("lock over time");
				}
			}
		}
		return returnInt;
	}

	@Override
	public int deleteElectiveStudent(HashMap<String, Object> map) throws Exception {
		String schoolId = (String) map.get("schoolId");
		String electiveId = (String) map.get("electiveId");
		String courseId = (String) map.get("courseId");
		String termInfo = (String) map.get("termInfo");
		HashMap<String,String> scMap=(HashMap<String, String>) map.get("scMap");
		List<String> studentIdList = (List<String>) map.get("studentIdList");
		
		int returnInt = electiveDao.deleteElectiveStudent(map);
		//操作redis的已选课人数
    	//redis缓存修改
		if(returnInt>0){
			String isInitKey="elective."+schoolId+"."+electiveId+".isInit";
			String keyNX = "elective." + schoolId + "." + electiveId + "." + courseId;
			Object isInitObj = redisOperationDAO.get(isInitKey);
			Boolean hasSuccessUpdateRedis=false;
			if(isInitObj!=null){
				logger.info("[elective]update course-redis is update：isInitObj={} isInitKey={} keyNX={} courseId={} eleciveId={}",isInitObj,isInitKey,keyNX,courseId,electiveId);
				for(int i=0;i<10;i++){
					Boolean isSet = redisOperationDAO.setNX(keyNX, 1);
					if (!isSet) {
						Thread.sleep(10);
						logger.info("[elective]has not lock,wait 10ms");
						continue;
					}
					try {
						//得到资源后
						redisOperationDAO.set(keyNX, 1,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			
						//redis中有数据则修改对应的班级 和课程人数上限
						HashMap<String,Integer> redisMap = new HashMap<String,Integer>();
						for(String studentId:studentIdList){
							if(StringUtils.isBlank(schoolId) || StringUtils.isBlank(electiveId) || StringUtils.isBlank(courseId) ||  StringUtils.isBlank( scMap.get(studentId))){
								continue;
							}
							// 课程人数上限
							String courseKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + courseId + ".MaxValue";
							// 班级课程人数上限
							String courseClassKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + courseId + "." +  scMap.get(studentId)+ ".MaxValue";
							// 已选课程人数
							String courseKeyValue = "elective." + schoolId + "." + electiveId + "." + courseId + ".Value";
							// 已选班级课程人数
							String courseClassKeyValue = "elective." + schoolId + "." + electiveId + "." + courseId + "." +  scMap.get(studentId)+ ".Value";
							
							HashMap<String, Object> courseMap = new HashMap<String, Object>();
							courseMap.put("schoolId", schoolId);
							courseMap.put("electiveId", electiveId);
							courseMap.put("classId", scMap.get(studentId));
							courseMap.put("courseId", courseId);
							courseMap.put("termInfo", termInfo);
							List<JSONObject> maxList=electiveDao.getCourseNumForInit(courseMap);
							List<JSONObject> ccvList = electiveDao.getCourseClassNum(courseMap);
							List<JSONObject> cvList=electiveDao.getCourseSelectedNum(courseMap);
							if(null!=maxList&& maxList.size()>0){
								JSONObject c =maxList.get(0);
								redisMap.put(courseKeyMaxValue, c.getIntValue("upperLimit"));
								redisMap.put(courseKeyValue, 0); //初始化为0
								redisMap.put(courseClassKeyValue,0);//初始化为0
								String classMaxNum = c.getString("classMaxNum");
								if (null != classMaxNum && !"不限".equals(classMaxNum)) {
									redisMap.put(courseClassKeyMaxValue, Integer.valueOf(classMaxNum));
								} else {
									redisMap.put(courseClassKeyMaxValue,  Integer.MAX_VALUE);
								}
							}
							if(null!=ccvList&& ccvList.size()>0){
								JSONObject c=ccvList.get(0);
								redisMap.put(courseClassKeyValue, c.getIntValue("selectedNum"));
							}
							if(null!=cvList&& cvList.size()>0){
								JSONObject c=cvList.get(0);
								redisMap.put(courseKeyValue,c.getIntValue("selectedNum"));
							}
						}
						logger.info("[elective]update course，get lock and redis update, electiveId={} redisMap={} ",electiveId,redisMap);
						redisOperationDAO.multiSet(redisMap);
						hasSuccessUpdateRedis=true;
						break;
					}finally {
						logger.info("[elective]update course del lock ");
						redisOperationDAO.del(keyNX);
					}
				}
				if(isInitObj!=null &&!hasSuccessUpdateRedis){
					throw new  Exception("lock over time");
				}
			}
		}
		return returnInt;
	}

	@Override
	public List<JSONObject> getSelectedCourseNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getSelectedCourseNum(map);
	}

	@Override
	public int getTotalSubmittedNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getTotalSubmittedNum(map);
	}

	@Override
	public List<Long> getShouldSelectedCourseNum(
			HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getShouldSelectedCourseNum(map);
	}

	@Override
	public List<Long> getSubmittedStudentIds(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getSubmittedStudentIds(map);
	}

	@Override
	public List<Long> getSubmittedClassIds(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getSubmittedClassIds(map);
	}

	@Override
	public List<JSONObject> getDetailCourseText(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getDetailCourseText(map);
	}

	@Override
	public List<JSONObject> getCourseClassNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCourseClassNum(map);
	}

	@Override
	public List<JSONObject> getStudentCourseIds(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getStudentCourseIds(map);
	}

	@Override
	public List<JSONObject> getStudentCourseText(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getStudentCourseText(map);
	}

	@Override
	public List<JSONObject> getStudentCourseSchoolTime(
			HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getStudentCourseSchoolTime(map);
	}

	@Override
	public List<JSONObject> getCurrentElective(String schoolId) {
		// TODO Auto-generated method stub
		return electiveDao.getCurrentElective(schoolId);
	}

	@Override
	public List<JSONObject> getCurrentCourse(HashMap<String,Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCurrentCourse(map);
	}

	@Override
	public List<JSONObject> getSchoolTimeByClassID(HashMap<String,Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getSchoolTimeByClassID(map);
	}

	@Override
	public int insertBatchElectiveCourse(HashMap<String, Object> map) throws Exception{
		//#####
		String termInfo = (String) map.get("termInfo");
		String schoolId = (String) map.get("schoolId");
		String electiveId = (String) map.get("electiveId");
		Map<String,Object> returnMap =electiveDao.batchInsertElectiveCourse(map);
		int num = (int) returnMap.get("num"); //导入的条数
		Set<String> cList = (HashSet<String>) returnMap.get("cList");//course id list 去重映射
		map.put("courseIds", cList);
		List<JSONObject> courseClassMaxList = electiveDao.getCourseNumForInit(map);
		if(num>0){ //redis数据刷新（针对导入的课程代码）
			
			//redis缓存修改
			String isInitKey="elective."+schoolId+"."+electiveId+".isInit";
			Object isInitObj = redisOperationDAO.get(isInitKey);
			for(JSONObject obj:courseClassMaxList){
				String courseId = obj.getString("courseId");
				String classId = obj.getString("classId");
				String keyNX = "elective." + schoolId + "." + electiveId + "." + courseId;
				
				Boolean hasSuccessUpdateRedis=false;
				if(isInitObj!=null){
					logger.info("[elective]update course-redis is update：isInitObj={} isInitKey={} keyNX={} courseId={} eleciveId={}",isInitObj,isInitKey,keyNX,courseId,electiveId);
					for(int i=0;i<10;i++){
						Boolean isSet = redisOperationDAO.setNX(keyNX, 1);
						if (!isSet) {
							Thread.sleep(10);
							logger.info("[elective]has not lock,wait 10ms");
							continue;
						}
						try {
							//得到资源后
							redisOperationDAO.set(keyNX, 1,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
				
							//redis中有数据则修改对应的班级 和课程人数上限
							HashMap<String,Integer> redisMap = new HashMap<String,Integer>();
						
							// 课程人数上限
							String courseKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + courseId + ".MaxValue";
							// 班级课程人数上限
							String courseClassKeyMaxValue = "elective." + schoolId + "." + electiveId + "." + courseId + "." + classId+ ".MaxValue";
							int maxNum = obj.getIntValue("upperLimit");
							redisMap.put(courseKeyMaxValue,Integer.valueOf(maxNum) );
							String classMaxNum = obj.getString("classMaxNum");
							if (null != classMaxNum && !"不限".equals(classMaxNum)) {
								redisMap.put(courseClassKeyMaxValue, Integer.valueOf(classMaxNum));
							} else {
								redisMap.put(courseClassKeyMaxValue,  Integer.MAX_VALUE);
							}
						
							logger.info("[elective]update course，get lock and redis update, electiveId={} redisMap={} ",electiveId,redisMap);
							redisOperationDAO.multiSet(redisMap);
							hasSuccessUpdateRedis=true;
							break;
						}finally {
							logger.info("[elective]update course del lock ");
							redisOperationDAO.del(keyNX);
						}
						
						
					}
					
					
				}
				if(isInitObj!=null &&!hasSuccessUpdateRedis){
					logger.info("[elective]update course isInitObj={} hasSuccessUpdateRedis={} electiveId={}",isInitObj,hasSuccessUpdateRedis,electiveId);
					throw new  Exception("lock over time");
				}
			}
		}
		return num;
	}

	@Override
	public List<JSONObject> getAllCourse(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getAllCourse(map);
	}

	@Override
	public JSONObject getImportCompareData(HashMap<String, Object> map) {
          return null;
	}

	@Override
	public List<String> getCourseIdsByName(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCourseIdsByName(map);
	}

	@Override
	public List<JSONObject> getElectiveListByTermInfo(
			HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveListByTermInfo(map);
	}

	@Override
	public String getElectiveCourseIds(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveCourseIds(map);
	}

	@Override
	public List<JSONObject> getCurrentCourseAllInfo(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCurrentCourseAllInfo(map);
	}

	@Override
	public List<JSONObject> getSelectedCoureRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getSelectedCoureRequire(map);
	}

	@Override
	public JSONObject getElectiveXnxqById(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getElectiveXnxqById(map);
	}

	@Override
	public List<JSONObject> getCourseNameById(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCourseNameById(map);
	}
	/**
	 * 学生选课
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 * @update 2016.8.9 添加多机并发处理 By：zhh
	 * @update 2016.8.13  将选课模块移入service层做事务控制 By：zhh
	 * @update 添加后端冲突判断控制  2016.09.06
	 * @update 修改提交选课后的反馈信息 2017.1.12 By:zhh
	 */
	@Override
	public JSONObject addElective(HashMap<String, Object> map) throws Exception {
        JSONObject json =new JSONObject();
       List<String>  successList = new ArrayList<String>();
        String msg = "选课成功！";
 		int code = 0;
 		String schoolId=(String) map.get("schoolId");
		String electiveId = (String) map.get("electiveId");
 		Long classId = (Long) map.get("classId");
		Long studentId = (Long) map.get("studentId");
		String term = (String) map.get("term");
		String schoolYear = (String) map.get("schoolYear");
		String termInfo = (String) map.get("termInfo");
		HashMap<String,String> nameIdMap = (HashMap<String, String>) map.get("nameIdMap");
 		List<String> courseIdList = (List<String>) map.get("courseIdList");
 		List<String> conflictCourseIdList = (List<String>) map.get("conflictCourseIdList");
		//学生所在班级所有选课对应的人数
		long time0=new Date().getTime();
		List<JSONObject> currentCourseList=new ArrayList<JSONObject>();
		int num=0;
		//-------------------------------------------------------开始多机并发处理
		
		//(1)redis是否有数据，若无则一次性从mysql中load过来 【需setNX】
		String keyNXForInit = "elective."+schoolId+"."+electiveId+".keyNXForInit";
		String courseId = courseIdList.get(0);
		Set<String> redisSet = new LinkedHashSet<String>();
		
		Boolean isSuccessForInit=false;
		Boolean hasInit =false;
		List<Integer> redisResult =new  ArrayList<Integer>();
		String isInitKey="elective."+schoolId+"."+electiveId+".isInit";
		Object isInitObj=null;
		for(int i=0;i<1000;i++ ) {
		
			isInitObj = redisOperationDAO.get(isInitKey);
			logger.info("[elective](1)isInitKey={} isInitObj={} keyNXForInit={}",isInitKey,isInitObj,keyNXForInit);
			// 表示已经load了数据，则跳出循环
			if( isInitObj!=null ) {
					hasInit=true;
					break;
			}
			// 没有初始化则setNXForInit然后初始化redis
			Boolean isSetForInit = redisOperationDAO.setNX(keyNXForInit, 1);
		
			// 没有得到资源则休眠10毫秒，继续下一轮的竞争锁
			if (!isSetForInit) {
				logger.info("[elective](1)has not get init lock ,wait 10 ms");
				Thread.sleep(10);
				continue;
			}
			// 得到了资源
			redisOperationDAO.set(keyNXForInit, 1,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			logger.info("[elective](1)has get init lock");
			
		    isInitObj = redisOperationDAO.get(isInitKey);
		    logger.info("[elective](1)get init lock before isInitObj={} ",isInitObj);
			// 这里一定要再次判断！！
			if (isInitObj!=null) {
				hasInit=true;
				break;
			}
			try {
				// 执行读mysql操作 然后存入redis
				HashMap<String, Object> courseMap = new HashMap<String, Object>();
				courseMap.put("schoolId", schoolId);
				courseMap.put("electiveId", electiveId);
				courseMap.put("termInfo", termInfo);
				//获取课程相关人数
				currentCourseList=electiveDao.getCourseNumForInit(courseMap);
				HashMap<String,Integer> redisMap = new HashMap<String,Integer>(); 
				for(JSONObject c:currentCourseList){
					if(c.getString("courseId")==null || c.getString("classId")==null){
						continue;
					}
					// 课程人数上限
					String courseKeyMaxValue1 = "elective." + schoolId + "." + electiveId + "." + c.getString("courseId") + ".MaxValue";
					// 已选课程人数
					String courseKeyValue1 = "elective." + schoolId + "." + electiveId + "." + c.getString("courseId") + ".Value";
					// 班级课程人数上限
					String courseClassKeyMaxValue1 = "elective." + schoolId + "." + electiveId + "." + c.getString("courseId") + "." + c.getString("classId")
							+ ".MaxValue";
					// 已选班级课程人数
					String courseClassKeyValue1 = "elective." + schoolId + "." + electiveId + "." + c.getString("courseId") + "." + c.getString("classId")
							+ ".Value";
					redisMap.put(courseKeyMaxValue1, c.getIntValue("upperLimit"));
					redisMap.put(courseKeyValue1, 0); //初始化为0
					redisMap.put(courseClassKeyValue1, 0);//初始化为0
					String classMaxNum = c.getString("classMaxNum");
					if (null != classMaxNum && !"不限".equals(classMaxNum)) {
						redisMap.put(courseClassKeyMaxValue1, Integer.valueOf(classMaxNum));
					} else {
						redisMap.put(courseClassKeyMaxValue1,  Integer.MAX_VALUE);
					}
				}
				List<JSONObject> courseClassList = electiveDao.getCourseClassNum(courseMap);
				for(JSONObject c:courseClassList){
					// 已选班级课程人数
					String courseClassKeyValue1 = "elective." + schoolId + "." + electiveId + "." + c.getString("courseId") + "." + c.getString("classId")
							+ ".Value";
					redisMap.put(courseClassKeyValue1, c.getIntValue("selectedNum"));
				}
				List<JSONObject> courseList = electiveDao.getCourseSelectedNum(courseMap);
				for(JSONObject c:courseList){
					// 已选课程人数
					String courseKeyValue1 = "elective." + schoolId + "." + electiveId + "." + c.getString("courseId") + ".Value";
					redisMap.put(courseKeyValue1, c.getIntValue("selectedNum"));
				}
				logger.info("[elective](1)get init lock and init soon after ,redisMap={} redisMap.size()={}",redisMap,redisMap.size());

				redisOperationDAO.multiSet(redisMap);//只有全部set成功了才set isInit标志位
				redisOperationDAO.set(isInitKey, 1);//是否初始化标志位,这里为选课设置处用的
				isSuccessForInit=true;
				hasInit=true;
				logger.info("[elective](1)get init lock and init success ,redisMap={}",redisMap);
				break;
				
			} finally {
				//释放锁
				logger.info("[elective](1)del init lock");
				redisOperationDAO.del(keyNXForInit);
			}
		}
		//看初始化成功否
		logger.info("[elective](1)init over.hasInit={} isSuccessForInit={}",hasInit,isSuccessForInit);
		if(!hasInit && !isSuccessForInit ) throw new Exception("redis init failed！");
		
		//(2)拦截判断 【无需setNX】
		Set<String> reloadCourseId = new HashSet<String>();
	    int isInterceptedNum=0;
		for(String cId:courseIdList){
			String courseKeyMaxValue2 = "elective." + schoolId + "." + electiveId + "." + cId + ".MaxValue";
			// 已选课程人数
			String courseKeyValue2 = "elective." + schoolId + "." + electiveId + "." + cId + ".Value";
			// 班级课程人数上限
			String courseClassKeyMaxValue2 = "elective." + schoolId + "." + electiveId + "." + cId + "." + classId
					+ ".MaxValue";
			// 已选班级课程人数
			String courseClassKeyValue2 = "elective." + schoolId + "." + electiveId + "." + cId + "." + classId
					+ ".Value";
			
			redisSet.clear();
			redisSet.add(courseKeyMaxValue2);
			redisSet.add(courseKeyValue2);
			redisSet.add(courseClassKeyMaxValue2);
			redisSet.add(courseClassKeyValue2);
			redisResult=(List<Integer>) redisOperationDAO.multiGet(redisSet);
			logger.info("[elective](2)intercept electiveId={} courseId={} classId={} redisResult={} cmvKey={} cvKey={} ccmvKey={} ccvKey={} ",
					electiveId,cId,classId,redisResult,courseKeyMaxValue2,courseKeyValue2,courseClassKeyMaxValue2,courseClassKeyValue2);
			if(redisResult.size()!=4 || redisResult.contains(null)){
				logger.error("[elective](2)intercept electiveId={} courseId={} classId={} redisResult={} cmvKey={} cvKey={} ccmvKey={} ccvKey={} ",
						electiveId,cId,classId,redisResult,courseKeyMaxValue2,courseKeyValue2,courseClassKeyMaxValue2,courseClassKeyValue2);
				//throw new Exception("redis is null ");//改成不抛出 而是在插入之前，将mysql数据insert到redis
				
			}
			//是否有mysql选课数据与redis不一致
			HashMap<String, Object> courseMap = new HashMap<String, Object>();
			courseMap.put("schoolId", schoolId);
			courseMap.put("electiveId", electiveId);
			courseMap.put("classId", classId);
			courseMap.put("courseId", cId);
			courseMap.put("termInfo", termInfo);
			List<JSONObject> ccvList = electiveDao.getCourseClassNum(courseMap);
			List<JSONObject> cvList=electiveDao.getCourseSelectedNum(courseMap);
			if(null!=ccvList&& ccvList.size()>0){
				JSONObject c=ccvList.get(0);
				 int sn=c.getIntValue("selectedNum");
				 if(sn!=redisResult.get(3)){
					 reloadCourseId.add(cId);
				 }
			}
			if(null!=cvList&& cvList.size()>0){
				JSONObject c=cvList.get(0);
				int sn=c.getIntValue("selectedNum");
				 if(sn!=redisResult.get(1)){
					 reloadCourseId.add(cId);
				 }
			}
			if(reloadCourseId.contains(cId))continue; //如果有则不考虑该课程（严格来说没有这种情况，但不能保证并发导致数据不一致不存在）
			if(redisResult.size()==4 && !redisResult.contains(null)){
				if(redisResult.get(0)<=redisResult.get(1) || redisResult.get(2)<= redisResult.get(3)){
					isInterceptedNum++;
				}
		    }
		}
		if(isInterceptedNum==courseIdList.size()){
			json.put("code", -1);
			json.put("success", new ArrayList<String>());
			json.put("appMsg", "失败！选课人数已达上限");
			json.put("msg", "失败！选课人数已达上限");
			List<JSONObject> appMsgList = new ArrayList<JSONObject>();
    		String s = "";
    		for(String cId:courseIdList){
    			s+=cId+",";
    		}
    		if(StringUtils.isNotBlank(s)){
    			s=s.substring(0,s.length()-1);
    		}
    		JSONObject obj = new JSONObject();
    		obj.put("courseId", s);
    		obj.put("reason", "失败！选课人数已达上限");
    		appMsgList.add(obj);
    		json.put("appMsgList", appMsgList) ;
			return json;
		}
		//（3）选课操作 【需setNX】
		List<JSONObject> insertList = new ArrayList<JSONObject>();
		//获取该学生已选课程
		String failure="";   //已选该课程 
		String failToMax = ""; //已达人数上限选择该课程
		String failToNX=""; //未争取到资源
		String failureToDel="";
		String failureToFreeze="";
		String failureToDelForApp=""; //已删除失败
		String failureToFreezeForApp=""; //已冻结失败
		String failureForApp="";   //已选该课程 
		String failToMaxForApp = ""; //已达人数上限选择该课程
		String failToNXForApp=""; //未争取到资源
		
		Set<String> selectCourseSet = new HashSet<String>();
		//获取当前登录人所选的所有课程id
		List<JSONObject> selectedCourseList = electiveDao.getSelectedCourseList(map);
		for(JSONObject course:selectedCourseList){
			selectCourseSet.add(course.getString("courseId"));
		}
		logger.info("[elective](3)student has elective courseIds. studendId={} selectCourseSet={}",electiveId,studentId,selectCourseSet);
		for (String cId : courseIdList) {
			String keyNX = "elective." + schoolId + "." + electiveId + "." + cId;
			String courseKeyMaxValue2 = "elective." + schoolId + "." + electiveId + "." + cId + ".MaxValue";
			// 已选课程人数
			String courseKeyValue2 = "elective." + schoolId + "." + electiveId + "." + cId + ".Value";
			// 班级课程人数上限
			String courseClassKeyMaxValue2 = "elective." + schoolId + "." + electiveId + "." + cId + "." + classId
					+ ".MaxValue";
			// 已选班级课程人数
			String courseClassKeyValue2 = "elective." + schoolId + "." + electiveId + "." + cId + "." + classId
					+ ".Value";
			
			int i=0;
			boolean isWait=false;
			for (i = 0; i < 300; i++) {  //一个课程请求300次 若没有得到资源
				    int returnNum=0;
					Boolean isSet = redisOperationDAO.setNX(keyNX, 1);
					if (!isSet) {
						Thread.sleep(10);
						logger.info("[elective](3)has not get lock wait 10ms");
						isWait=true;
						continue;
					}
					try {
						//得到资源后
						logger.info("[elective](3)has get lock keyNX:{}",keyNX);
						isWait=false;
						redisOperationDAO.set(keyNX, 1,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
						//检查是否已有选该课数据
						if (selectCourseSet.contains(cId)) {
							failure += cId +",";
							logger.info("[elective](3)student has elective, elective={} selectCourseSet={} cId={} ",electiveId,selectCourseSet,cId);
							break; //进行该请求的下一轮课程选择
						}
						JSONObject sj = new JSONObject();
						sj.put("schoolYear", schoolYear);
						sj.put("term", term);
						sj.put("electiveId", electiveId);
						sj.put("schoolId", schoolId);
						sj.put("studentId", studentId);
						sj.put("courseId", cId);
						sj.put("electiveTime", DateUtil.getDateFormatNow());
						sj.put("electiveWay", 1);
						sj.put("classId", classId);
						insertList.clear(); //此处是为了一次性插入一条学生选课记录
						insertList.add(sj);
						
						redisSet.clear();
						redisSet.add(courseKeyMaxValue2);
						redisSet.add(courseKeyValue2);
						redisSet.add(courseClassKeyMaxValue2);
						redisSet.add(courseClassKeyValue2);
						redisResult = (List<Integer>) redisOperationDAO.multiGet(redisSet);
						boolean reloadFlag= false;
						List<JSONObject> ccvList = new ArrayList<JSONObject>();
						List<JSONObject> cvList = new ArrayList<JSONObject>();
						HashMap<String, Object> courseMap = new HashMap<String, Object>();
						courseMap.put("schoolId", schoolId);
						courseMap.put("electiveId", electiveId);
						courseMap.put("classId", classId);
						courseMap.put("courseId", cId);
						courseMap.put("termInfo", termInfo);
						if(redisResult.size()==4 && !redisResult.contains(null)){
							//判断基础数据是否删除学生，是否需要刷新redis
							ccvList = electiveDao.getCourseClassNum(courseMap);
							cvList=electiveDao.getCourseSelectedNum(courseMap);
							if(null!=ccvList&& ccvList.size()>0){
								JSONObject c=ccvList.get(0);
								 int sn=c.getIntValue("selectedNum");
								 if(sn!=redisResult.get(3)){
									 reloadFlag=true;
								 }
							}
							if(null!=cvList&& cvList.size()>0){
								JSONObject c=cvList.get(0);
								int sn=c.getIntValue("selectedNum");
								 if(sn!=redisResult.get(1)){
									 reloadFlag=true;
								 }
							}
						}
						//判断是否为空（reloadFlag 严格来说不存在，但不能保证并发过程中数据不一致一定不会发生）
						if(redisResult.size()!=4 || redisResult.contains(null)||reloadFlag){
							//为空则重新取mysql存入redis
							//获取课程相关人数
							Map<String,Integer> redisMap = new HashMap<String,Integer>();
							JSONObject courseObj = electiveDao.getOneCourse(courseMap);
							if(courseObj==null){
								failureToDel += cId+",";
								break;
							}
							if(courseObj!=null){
								String courseName=courseObj.getString("courseName");
								String isFreezed = courseObj.getString("isFreezed");
								if("1".equals(isFreezed)){
							        failureToFreeze += cId + ",";
							        break;
								}
							}
							List<JSONObject> maxList=electiveDao.getCourseNumForInit(courseMap);
							
							if(null!=maxList&& maxList.size()>0){
								JSONObject c =maxList.get(0);
								redisMap.put(courseKeyMaxValue2, c.getIntValue("upperLimit"));
								redisMap.put(courseKeyValue2, 0); //初始化为0
								redisMap.put(courseClassKeyValue2,0);//初始化为0
								String classMaxNum = c.getString("classMaxNum");
								if (null != classMaxNum && !"不限".equals(classMaxNum)) {
									redisMap.put(courseClassKeyMaxValue2, Integer.valueOf(classMaxNum));
								} else {
									redisMap.put(courseClassKeyMaxValue2,  Integer.MAX_VALUE);
								}
							}
							if(null!=ccvList&& ccvList.size()>0){
								JSONObject c=ccvList.get(0);
								redisMap.put(courseClassKeyValue2, c.getIntValue("selectedNum"));
							}
							if(null!=cvList&& cvList.size()>0){
								JSONObject c=cvList.get(0);
								redisMap.put(courseKeyValue2,c.getIntValue("selectedNum"));
							}
							redisOperationDAO.multiSet(redisMap);
							redisResult = (List<Integer>) redisOperationDAO.multiGet(redisSet);
						}
						int courseMaxNum1 = redisResult.get(0);
						int courseNum1 =  redisResult.get(1);
						int courseClassMaxNum1 = redisResult.get(2);
						int courseClassNum1 = redisResult.get(3);
						if(courseMaxNum1 <= courseNum1 || courseClassMaxNum1 <= courseClassNum1){
							logger.info("[elective](3)insert before judge max electiveId={} courseId={} studentId={} courseMaxNum={}courseNum={}  courseClassMaxNum={} courseClassNum={} redisSetKey={}",
									electiveId,cId,studentId,courseMaxNum1,courseNum1,courseClassMaxNum1,courseClassNum1,redisSet);
							failToMax +=cId + ",";
							break;
						}
						logger.info("[elective](3) electiveId={} get lock and redisResult={} redisSetKey={} is fit for insert",electiveId,redisResult,redisSet);
						//判断该课程是否存在
						HashMap<String,Object>cMap= new HashMap<String,Object>();
						cMap.put("schoolId", schoolId);
						cMap.put("electiveId", electiveId);
						cMap.put("courseId", cId);
						JSONObject courseObj = electiveDao.getOneCourse(cMap);
						if(courseObj==null){
							failureToDel += cId+ ",";
							break;
						}
						if(courseObj!=null){
							String courseName = courseObj.getString("courseName");
							String isFreezed = courseObj.getString("isFreezed");
							if("1".equals(isFreezed)){
						    	failureToFreeze += cId + ",";
						    	break;
							}
						}
						
							returnNum = electiveDao.insertElectiveStudent(insertList);
							if (returnNum > 0) {
								num += returnNum;
								redisOperationDAO.set(courseKeyValue2, ++courseNum1);
								redisOperationDAO.set(courseClassKeyValue2, ++courseClassNum1);
								successList.add(cId);
								logger.info("[elective](3)insert success,courseId={}  studentId={} electiveId={}", courseId, studentId,
										electiveId);
								break;//只有插入操作执行成功了才进行下一门的判断，不然每个课都有300次机会
							}
						
						
					}finally {
						logger.info("[elective](3)del lock");
						redisOperationDAO.del(keyNX);
					}
						
				}//end of for  10 loop
			if(isWait){ //该课程从没争取到资源过
				failToNX +=nameIdMap.get(cId) + ",";
			}
			}//end of for courseIdList
		
		//(4)返回提示信息
		List<JSONObject> appMsgList = new ArrayList<JSONObject>();
		if(StringUtils.isNotEmpty(failure))
		{
			failureForApp = failure.substring(0,failure.length()-1);
			JSONObject obj = new JSONObject();
			obj.put("courseId", failureForApp);
			obj.put("reason", "失败！您已选择了该门课程");
			msg = "失败！您已选择了该门课程";
			appMsgList.add(obj);
		}
		if(StringUtils.isNotEmpty(failToMax)){
			failToMaxForApp = failToMax.substring(0,failToMax.length()-1);
			JSONObject obj = new JSONObject();
			obj.put("courseId", failToMaxForApp);
			obj.put("reason", "失败！选课人数已达上限");
			msg = "失败！选课人数已达上限";
			appMsgList.add(obj);
		}
		if(StringUtils.isNotEmpty(failToNX)){
			failToNXForApp = failToNX.substring(0,failToNX.length()-1);
			JSONObject obj = new JSONObject();
			obj.put("courseId", failToNXForApp);
			obj.put("reason", "失败！网络超时");
			msg = "失败！网络超时";
			appMsgList.add(obj);
		}
		if(StringUtils.isNotEmpty(failureToDel)){
			failureToDelForApp = failureToDel.substring(0,failureToDel.length()-1);
			JSONObject obj = new JSONObject();
			obj.put("courseId", failureToDelForApp);
			obj.put("reason", "失败！此课程已被删除");
			msg = "失败！此课程已被删除";
			appMsgList.add(obj);
		}
		if(StringUtils.isNotEmpty(failureToFreeze)){
			failureToFreezeForApp = failureToFreeze.substring(0,failureToFreeze.length()-1);
			JSONObject obj = new JSONObject();
			obj.put("courseId", failureToFreezeForApp);
			obj.put("reason", "失败！课程正在删除中，请稍后重试");
			msg = "失败！课程正在删除中，请稍后重试";
			appMsgList.add(obj);
		}
		int conflictNum = 0;
		if(conflictCourseIdList!=null && conflictCourseIdList.size()>0){
			JSONObject obj = new JSONObject();
			conflictNum=conflictCourseIdList.size();
			String failureToConflictForApp = "";
			for(String s:conflictCourseIdList){
				failureToConflictForApp+=s+",";
			}
			if(StringUtils.isNotBlank(failureToConflictForApp)){
				failureToConflictForApp=failureToConflictForApp.substring(0, failureToConflictForApp.length()-1);
			}
	        obj.put("courseId", failureToConflictForApp);
			obj.put("reason", "失败！您已选择了该开课时间下的课程");
			msg = "失败！您已选择了该开课时间下的课程";
			appMsgList.add(obj);
		}
		if(num==0){
			code=-1;
	     }
		else if(num<courseIdList.size()+conflictNum){
			code=1;
		}
			
        json.put("code", code);
        json.put("appMsg", "");
        json.put("msg", msg);
        json.put("success", successList);
        //拼接appMsgList
        if(appMsgList.size()>0){
        	json.put("appMsgList", appMsgList);
        }
		return json;
	}

	@Override
	public List<JSONObject> getCourseSelectedNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCourseSelectedNum(map);
	}

	@Override
	public List<JSONObject> getCourseByName(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return electiveDao.getCourseByName(map);
	}

	@Override
	public JSONObject isConflict(HashMap<String, Object> map) throws Exception {
		JSONObject returnObj = new JSONObject();
		int returnFlag=1;
		String courseIds = "";
		Long classId = (Long) map.get("classId");
		List<String> courseIdList1 = (List<String>) map.get("courseIdList");
		List<String> courseIdList = new ArrayList<String>(courseIdList1); 
		List<JSONObject> timeList=this.getSchoolTimeByClassID(map);
		HashMap<String,Integer> courseWeekType=new HashMap<String,Integer>();//周次
		HashMap<String,String> timeMap=new HashMap<String,String>();
		HashMap<String,Object> dayOfWeekMap=new HashMap<String,Object>();
		//判断开放时间冲突
		HashMap<String,List<JSONObject>> schoolTimeMap= new HashMap<String,List<JSONObject>>();
		for(JSONObject j:timeList)
		{
			String courseId=j.getString("courseId");
			int dayOfWeek=j.getIntValue("dayOfWeek");
			int lessonOfDay=j.getIntValue("lessonOfDay")+1;	
			if(!courseWeekType.containsKey(courseId))
			{
				courseWeekType.put(courseId, j.getIntValue("weekType"));
			}
			if(timeMap.containsKey(courseId))
			{
				String text=timeMap.get(courseId);
				if(dayOfWeekMap.containsKey(courseId+dayOfWeek))
				{
					text+=","+lessonOfDay;
				}
				else
				{
					text+=";"+StringNumTool.getDayOfWeek(dayOfWeek)+lessonOfDay;
					dayOfWeekMap.put(courseId+dayOfWeek, "in");
				}
				timeMap.put(courseId, text);
				JSONObject tj=new JSONObject();
				tj.put("dayOfWeek", j.getIntValue("dayOfWeek"));
				tj.put("lessonOfDay", j.getIntValue("lessonOfDay"));
				tj.put("weekType", j.getIntValue("weekType"));
				List<JSONObject> tlist=schoolTimeMap.get(courseId);
				tlist.add(tj);
				schoolTimeMap.put(courseId, tlist); 
			}
			else
			{
				String text=StringNumTool.getDayOfWeek(dayOfWeek)+lessonOfDay;
				dayOfWeekMap.put(courseId+dayOfWeek, "in");
				timeMap.put(courseId, text);
				List<JSONObject> tlist=new ArrayList<JSONObject>();
				JSONObject tj=new JSONObject();
				tj.put("dayOfWeek", j.getIntValue("dayOfWeek"));
				tj.put("lessonOfDay", j.getIntValue("lessonOfDay"));
				tj.put("weekType", j.getIntValue("weekType"));
				tlist.add(tj);
				schoolTimeMap.put(courseId, tlist);
			}
		}
		//schoolTimeMap 所有课程的开课时间
		
		List<String> hasSelectedCourseIdList = new ArrayList<String>();
		  //获取该人全部已选课程
		List<JSONObject> eList = electiveDao.getSelectedCourseList(map);
		for(JSONObject e:eList){
			String courseId = e.getString("courseId");
			List<JSONObject> sList = schoolTimeMap.get(courseId);
			hasSelectedCourseIdList.add(courseId); //去除已选的课程
			
		}
		courseIdList.removeAll(hasSelectedCourseIdList);
		for(String cId:courseIdList){
			courseIds+=cId+",";
		}
		if(StringUtils.isNotBlank(courseIds)){
			courseIds=courseIds.substring(0, courseIds.length()-1);
		}
		boolean isReturn = false;
		List<String> conflictCourseIdList = new ArrayList<String>();
		for(String s:courseIdList){ //将选则的课程
			List<JSONObject> toBeList = schoolTimeMap.get(s); //将选课程的开课时间列表
			for(JSONObject e:eList){ //已选课的课程
				String courseId = e.getString("courseId");
				List<JSONObject> hasSelectedList = schoolTimeMap.get(courseId);//得到已选课程的开课时间列表
				if(toBeList!=null && hasSelectedList!=null){
					for(JSONObject toBeObj:toBeList){ //判断将选课程是否与已选的每个课程有冲突
						int dayOfWeek = toBeObj.getIntValue("dayOfWeek");
						int lessonOfDay = toBeObj.getIntValue("lessonOfDay");
						int weekType = toBeObj.getIntValue("weekType");
						for(JSONObject hasSelectedObj:hasSelectedList){
							int dayOfWeek1 = hasSelectedObj.getIntValue("dayOfWeek");
							int lessonOfDay1 = hasSelectedObj.getIntValue("lessonOfDay");
							int weekType1 = hasSelectedObj.getIntValue("weekType");
							if(dayOfWeek==dayOfWeek1 && lessonOfDay==lessonOfDay1 && weekType==weekType1){
								isReturn=true;
								if(!conflictCourseIdList.contains(s)){
									conflictCourseIdList.add(s);
								}
							}
						}
					}
			    }
			}
		}
		if(isReturn){
			/*returnObj.put("returnFlag", -1);
			returnObj.put("courseIdList",courseIdList );
			returnObj.put("courseIds",courseIds );*/
			returnObj.put("conflictCourseIdList", conflictCourseIdList);
			//return returnObj;
		}
		//班级选课数量、课时要求
		List<JSONObject> requireClass=   electiveDao.getElectiveCourseRequire(map);
		JSONObject classElectiveRequire=new JSONObject();
		if(null!=requireClass&&requireClass.size()>0)
		{
			classElectiveRequire=requireClass.get(0);    					
		}
		else
		{
			classElectiveRequire.put("courseUpperLimit", "");
			classElectiveRequire.put("courseLowerLimit", "");
			classElectiveRequire.put("classhourUpperLimit", "");
			classElectiveRequire.put("classhourLowerLimit", "");
		}
		//classElectiveRequire
		//{"classId":"2017015","courseLowerLimit":"1","courseUpperLimit":"3","classhourUpperLimit":"3","classhourLowerLimit":"1"}
		//课程数量
		Integer courseUpperLimit = classElectiveRequire.getInteger("courseUpperLimit");
		Integer courseLowerLimit = classElectiveRequire.getInteger("courseLowerLimit");
		if(courseUpperLimit==null){
			courseUpperLimit=Integer.MAX_VALUE;
		}
			
		if(courseLowerLimit==null){
			courseLowerLimit=Integer.MIN_VALUE;
		}
		int sCount = eList.size()+courseIdList.size();//课程个数（已选+将选）
		if(!(sCount<=courseUpperLimit && sCount>=courseLowerLimit)){
			returnObj.put("returnFlag", -3);
			returnObj.put("courseIdList",courseIdList );
			returnObj.put("courseIds",courseIds );
			return returnObj;
		}
		
		//课时
		Double classhourUpperLimit = classElectiveRequire.getDouble("classhourUpperLimit");
		Double classhourLowerLimit = classElectiveRequire.getDouble("classhourLowerLimit");
		if(classhourUpperLimit==null){
			classhourUpperLimit=Double.MAX_VALUE;
		}
		if(classhourLowerLimit==null){
			classhourLowerLimit=-1.0D;
		}
		double classHour = 0.0; //课时（已选+将选）
		for(String s:courseIdList){ //将选则的课程
			List<JSONObject> toBeList = schoolTimeMap.get(s); //将选课程的开课时间列表
			if(toBeList!=null){
				for(JSONObject toBeObj:toBeList){ //判断将选课程是否与已选的每个课程有冲突
					int dayOfWeek = toBeObj.getIntValue("dayOfWeek");
					int lessonOfDay = toBeObj.getIntValue("lessonOfDay");
					int weekType = toBeObj.getIntValue("weekType");
					if(weekType==0){
						classHour+=1.0;
					}else{
						classHour+=0.5;
					}
				}
			}
		}
	
		for(JSONObject e:eList){ //已选课的课程
			String courseId = e.getString("courseId");
			List<JSONObject> hasSelectedList = schoolTimeMap.get(courseId);//得到已选课程的开课时间列表
			if(hasSelectedList!=null){
				for(JSONObject hasSelectedObj:hasSelectedList){
					int dayOfWeek1 = hasSelectedObj.getIntValue("dayOfWeek");
					int lessonOfDay1 = hasSelectedObj.getIntValue("lessonOfDay");
					int weekType1 = hasSelectedObj.getIntValue("weekType");
					if(weekType1==0){
						classHour+=1.0;
					}else{
						classHour+=0.5;
					}
				}
			}
		}
		if(!(classHour<=classhourUpperLimit && classHour>= classhourLowerLimit)){
			returnObj.put("returnFlag", -4);
			returnObj.put("courseIdList",courseIdList );
			returnObj.put("courseIds",courseIds );
			return returnObj;
		}
	
		//获取课程类别要求
		List<JSONObject> requireTypeClass=  electiveDao.getCourseTypeNumList(map);
		Map<String,JSONObject> sortMaxMap = new HashMap<String,JSONObject>();
		for(JSONObject obj:requireTypeClass){
			sortMaxMap.put(obj.getString("courseTypeId"), obj);
		}
		//所有类别-课程Ids
		List<JSONObject> csList = electiveDao.getCourseSort(map);
		//sortId - （将/已）选择的个数
		Map<String,Integer> sortMap = new HashMap<String,Integer>();
		for(String s:courseIdList){ //将选则的课程
			for(JSONObject csObj:csList){
				String cIds = csObj.getString("courseIds");
				if(StringUtils.isBlank(cIds)){continue;}
				if(cIds.contains(s)){ //找到将选的课程对应的课程类别
					String courseTypeId=csObj.getString("courseTypeId");
					if(sortMap.containsKey(courseTypeId)){
						int count = sortMap.get(courseTypeId);
						count++;
						sortMap.put(courseTypeId, count);
					}else{
						sortMap.put(courseTypeId, 1);
					}
					
				}
			}
		}
		for(JSONObject e:eList){ //已选课的课程
			String s = e.getString("courseId");
			for(JSONObject csObj:csList){
				String cIds = csObj.getString("courseIds");
				if(StringUtils.isBlank(cIds)){continue;}
				if(cIds.contains(s)){ //找到将选的课程对应的课程类别
					String courseTypeId=csObj.getString("courseTypeId");
					if(sortMap.containsKey(courseTypeId)){
						int count = sortMap.get(courseTypeId);
						count++;
						sortMap.put(courseTypeId, count);
					}else{
						sortMap.put(courseTypeId, 1);
					}
					
				}
			}
		}
		//sortMap循环 同时判断是否在范围之内
		for (Map.Entry<String, Integer> entry : sortMap.entrySet()) 
		{
//			 System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			 String sortId = entry.getKey();
			 int count = entry.getValue();
			 JSONObject obj = sortMaxMap.get(sortId);
			 if(obj!=null){
				  courseLowerLimit = obj.getInteger("courseLowerLimit");
				  courseUpperLimit= obj.getInteger("courseUpperLimit");
				  if(courseLowerLimit==null ){
					  courseLowerLimit = Integer.MIN_VALUE;
				  }
				  if(courseUpperLimit==null){
					  courseUpperLimit=Integer.MAX_VALUE;
				  }
				 if(!(count<=courseUpperLimit && count>=courseLowerLimit)){
					 returnObj.put("returnFlag", -2);
						returnObj.put("courseIdList",courseIdList );
						returnObj.put("courseIds",courseIds );
						return returnObj;
				 }
				  
			 }
		}
		returnObj.put("returnFlag", returnFlag);
		returnObj.put("courseIdList",courseIdList );
		returnObj.put("courseIds",courseIds );
		return returnObj;
	}

	@Override
	public void freezeElectiveCourse(HashMap<String, Object> map) {
		electiveDao.freezeElectiveCourse(map);
	}

	@Override
	public JSONArray getCourseToExport(HashMap<String, Object> map) {
		String schoolId = (String) map.get("schoolId");
		String termInfo = (String) map.get("termInfo");
		JSONObject returnObj = electiveDao.getCourseToExport(map);
		List<JSONObject> returnList = electiveDao.getStudentToExport(map);
		Set<Long> studentSet = new HashSet<Long>();
		Set<Long> classSet = new HashSet<Long>();
		Map<Long,Integer> classNumberMap = new HashMap<Long,Integer>();
		for(JSONObject obj :returnList){
			Long classId= obj.getLong("classId");
			Long studentId = obj.getLong("studentId");
			
			classSet.add(classId);
			if(!studentSet.contains(studentId)){
				if(classNumberMap.containsKey(classId)){
					Integer num = classNumberMap.get(classId);
					classNumberMap.put(classId, ++num);
				}else{
						classNumberMap.put(classId, 1);
				}
			}
			studentSet.add(studentId);
		}
		List<Classroom> cList = allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), new ArrayList<Long>(classSet), termInfo);
		Map<Long,Classroom> cMap = new HashMap<Long,Classroom>();
		for(Classroom c:cList){
			cMap.put(c.getId(), c);
		}
		Map<Long,Account> aMap = new HashMap<Long,Account>();
		List<Account> aList = allCommonDataService.getAccountBatch(Long.parseLong(schoolId), new ArrayList<Long>(studentSet), termInfo);
		for(Account a:aList){
			aMap.put(a.getId(), a);
		}
		JSONArray returnArr = new JSONArray();
		for(JSONObject obj :returnList){
			Long classId= obj.getLong("classId");
			Long studentId = obj.getLong("studentId");
			Classroom c = cMap.get(classId);
			if(c!=null){
				obj.put("className", c.getClassName());
			}else{
				obj.put("className", "[已删除]");
			}
			Account a = aMap.get(studentId);
			if(a!=null){
				obj.put("name",  a.getName());
				if(a.getGender()!=null){
					obj.put("sex",  (1==a.getGender().getValue())?"男":"女");
				}else{
					obj.put("sex", "");
				}
				obj.put("schoolNumber","");
				List<User> users = a.getUsers();
				if(users!=null){
					for(User u:users){
						if(u.getUserPart()!=null && u.getUserPart().getRole()==T_Role.Student){
							StudentPart sp=u.getStudentPart();
							if(sp!=null){
								obj.put("schoolNumber",sp.getSchoolNumber());
								break;
							}
						}
					}
				}
			}else{
				obj.put("sex",  "[已删除]");
				obj.put("schoolNumber",  "[已删除]");
				obj.put("name",  "[已删除]");
			}
			Integer hasSelectedNum = classNumberMap.get(classId);
			if(hasSelectedNum!=null){
				obj.put("hasSelectedNum", hasSelectedNum);
			}else{
				obj.put("hasSelectedNum", 0);
			}
			obj.putAll(returnObj);
			returnArr.add(obj);
		}
		return returnArr;
	}

	@Override
	public JSONObject uploadFile(JSONObject param, File ratioFile) throws Exception {
		JSONObject json = new JSONObject();
		String url = fileServerImplFastDFS.uploadFile(ratioFile);
		String attachmentId = UUIDUtil.getUUID();
		param.put("attachmentId", attachmentId);
		param.put("attachmentAddr", url);
		electiveDao.insertAttachment(param);
		json.put("attachmentId", attachmentId);
		json.put("url", url);
		return json;
	}

	@Override
	public List<JSONObject> getAttachmentById(JSONObject param) throws Exception {
		return electiveDao.getAttachment(param);
	}

	@Override
	public void deleteFileById(JSONObject param) throws Exception {
		param.put("isNotNull", 1);
		List<JSONObject> jsonList = electiveDao.getAttachment(param);
		if(jsonList!=null && jsonList.size()>0){
			JSONObject json = jsonList.get(0);
			String url = json.getString("attachmentAddr");
			fileServerImplFastDFS.deleteFile(url);
		    electiveDao.deleteAttachment(param);
		}
	}

	@Override
	public void updateAttachment(JSONObject param) throws Exception {
		electiveDao.updateAttachment(param);
	}

	@Override
	public void deleteAttachmentByElectiveId(JSONObject param) throws Exception {
		electiveDao.deleteAttachmentByElectiveId(param);
	}

	
}
