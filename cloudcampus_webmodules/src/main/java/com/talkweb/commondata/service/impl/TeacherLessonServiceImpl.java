package com.talkweb.commondata.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.StringUtils;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.TeacherLesson;
import com.talkweb.auth.dao.AuthDao;
import com.talkweb.common.tools.AESUtil;
import com.talkweb.commondata.dao.CsCurCommonDataDao;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.TeacherLessonService;
import com.talkweb.commondata.util.TeachingRelationShipUtil;

@Service("teacherLessonService")
public class TeacherLessonServiceImpl implements TeacherLessonService {
	@Autowired
	private AllCommonDataService allCommonDataService;
	
	@Autowired
	private AuthDao authDao;
	
	ResourceBundle rb = ResourceBundle.getBundle("constant.constant" );
	
	@Autowired
	private CsCurCommonDataDao csCurCommonDataDao;
	
	public static void main(String[] args) {
		String accessToken = "";
		try {
			accessToken = AESUtil.Encrypt("2b952354-2f1b-42d4-9101-e19aa92746e4"+"&"+(new Date()).getTime(), "talkweb.com@zhjx"); //clientId为每个业务手工分配的id
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("jiami:"+accessToken);
		try {
			accessToken = AESUtil.Decrypt(accessToken,  "talkweb.com@zhjx"); //clientId为每个业务手工分配的id
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] accessTokenStr = accessToken.split("&");
		System.out.println("jiemi:"+accessTokenStr[0]);
	}
	
	
	@Override
	public int updateTeacherLesson(String extSchoolId,String extTeacherId, List<JSONObject> classLessons, String accessToken,String clientId)
			throws Exception {
		//验证入参
		if(StringUtils.isEmptyOrWhitespaceOnly(extSchoolId)|| StringUtils.isEmptyOrWhitespaceOnly(extTeacherId)
				||StringUtils.isEmptyOrWhitespaceOnly(accessToken)||classLessons==null || StringUtils.isEmptyOrWhitespaceOnly(clientId)){
			return -1;
		}
		//解密accessToken
		String aesDecrypt = rb.getString("aes.decrypt");
		try {
			accessToken = AESUtil.Decrypt(accessToken, aesDecrypt); //clientId为每个业务手工分配的id
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(accessToken)){
			return -2;
		}
		String[] accessTokenStr = accessToken.split("&");
		//验证accessToken
		String token =authDao.getTokenByClientId(clientId);
		if(!StringUtils.isEmptyOrWhitespaceOnly(token) && !token.equals(accessTokenStr[0])  ){
			return -2;
		}
		
		String termInfoId = rb.getString("currentTermInfo");
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		//查老师teacherId
		List<String> ids = new ArrayList<String>();
		ids.add(extTeacherId);
		param.put("ids", ids);
		long teacherId=0;
		List<JSONObject> userIds = csCurCommonDataDao.getUserIdsByExtId(param);
		if(userIds==null || userIds.size()<1 || StringUtils.isNullOrEmpty( userIds.get(0).getString("userId")) ){
			return -1;
		}
		teacherId = userIds.get(0).getLongValue("userId");
		List<String> extClassIds= new ArrayList<String>();
		for(JSONObject classLesson:classLessons){
			if(!StringUtils.isEmptyOrWhitespaceOnly(classLesson.getString("extClassId"))  &&  !extClassIds.contains(classLesson.getString("extClassId"))){
			     extClassIds.add(classLesson.getString("extClassId"));
			}
		}
		//查classId
		param.put("ids", extClassIds);
		List<JSONObject> classIds = csCurCommonDataDao.getClassIdByExtclassId(param);
		Map<String,Long> classMap = new HashMap<String,Long>();
		for(JSONObject classObj :classIds){
			long classId=classObj.getLongValue("classId");
			String extId=classObj.getString("extId");
			if(StringUtils.isNullOrEmpty(extId)||0==classId){
				continue;
			}
			classMap.put(extId, classId);
		}
		//查询学校
		String schoolId =  allCommonDataService.getSchoolIdByExtId(extSchoolId,termInfoId);
		if(StringUtils.isEmptyOrWhitespaceOnly(schoolId)){
			return -1;
		}
		List<Course> courseIds = new ArrayList<Course>();
		for(JSONObject classLesson:classLessons){
			 long classId =classMap.get( classLesson.getString("extClassId"));
			 String extLessonName = classLesson.getString("extLessonName");
			 if(StringUtils.isEmptyOrWhitespaceOnly(extLessonName)||!TeachingRelationShipUtil.teachingRelationshipTwoMap.containsKey(extLessonName)||0==classId){
				 continue;
			 }
			 long lessonId = TeachingRelationShipUtil.teachingRelationshipTwoMap.get(extLessonName); 
			 Course c = new Course();
			 c.setClassId(classId);
			 c.setLessonId(lessonId);
			 courseIds.add(c);
		}
		allCommonDataService.updateTeacherLesson(Long.parseLong(schoolId), teacherId, courseIds, termInfoId);
		return 0;
	}

	@Override
	public int updateTeacherLessonBatch(String extSchoolId,List<JSONObject> teacherClassLessons, String accessToken,String clientId) throws Exception {
		//验证入参
		if(StringUtils.isEmptyOrWhitespaceOnly(extSchoolId)|| StringUtils.isEmptyOrWhitespaceOnly(clientId)
				||StringUtils.isEmptyOrWhitespaceOnly(accessToken)||teacherClassLessons==null){
			return -1;
		}
		//解密accessToken
		String aesDecrypt = rb.getString("aes.decrypt");
		try {
			accessToken = AESUtil.Decrypt(accessToken, aesDecrypt); //clientId为每个业务手工分配的id
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(accessToken)){
			return -2;
		}
		String[] accessTokenStr = accessToken.split("&");
		//验证accessToken
		String token =authDao.getTokenByClientId(clientId);
		if(!StringUtils.isEmptyOrWhitespaceOnly(token) && !token.equals(accessTokenStr[0])  ){
			return -2;
		}
		
		String termInfoId = rb.getString("currentTermInfo");
		//查询班级id和老师id
		List<String> extTeacherIds = new ArrayList<String>();
		List<String> extClassIds = new ArrayList<String>();
		for(JSONObject teacherClassLesson:teacherClassLessons){
			String extTeacherId = teacherClassLesson.getString("extTeacherId");
			if(org.apache.commons.lang.StringUtils.isNotBlank(extTeacherId) &&  !extTeacherIds.contains(extTeacherId)){
				extTeacherIds.add(extTeacherId);
			}
			List<JSONObject> classLessons = (List<JSONObject>) teacherClassLesson.get("classLessons");
			if(classLessons!=null){
				for(JSONObject classLesson :classLessons){
					String extClassId = classLesson.getString("extClassId");
					if(org.apache.commons.lang.StringUtils.isNotBlank(extClassId) &&  !extClassIds.contains(extClassId)){
						extClassIds.add(extClassId);
					}
				}
			}
		}
		JSONObject param = new JSONObject();
		param.put("ids", extClassIds);
		param.put("termInfoId", termInfoId);
		List<JSONObject> classIds = csCurCommonDataDao.getClassIdByExtclassId(param);
		Map<String,Long> classMap = new HashMap<String,Long>();
		for(JSONObject classObj :classIds){
			long classId=classObj.getLongValue("classId");
			String extId=classObj.getString("extId");
			if(StringUtils.isNullOrEmpty(extId)||0==classId){
				continue;
			}
			classMap.put(extId, classId);
		}
		
		param.put("ids", extTeacherIds);
		List<JSONObject> userIds = csCurCommonDataDao.getUserIdsByExtId(param);
		Map<String,Long> teacherMap = new HashMap<String,Long>();
		for(JSONObject userObj : userIds){
			long userId = userObj.getLongValue("userId");
			String extId = userObj.getString("extId");
			teacherMap.put(extId, userId);
		}
		Map<Long, List<Course>> teacherCourseMap = new HashMap<Long, List<Course>>(); 
		for(JSONObject teacherClassLesson:teacherClassLessons){
			String extTeacherId = teacherClassLesson.getString("extTeacherId");
			long userId = teacherMap.get(extTeacherId);
			if(0==userId){
				continue;
			}
			List<Course> courseList = new ArrayList<Course>();
			
			List<JSONObject> classLessons = (List<JSONObject>) teacherClassLesson.get("classLessons");
			if(classLessons!=null){
				for(JSONObject classLesson :classLessons){
					long classId = classMap.get(classLesson.getString("extClassId"));
					String extLessonName = classLesson.getString("extLessonName");
					if(classId==0 ||  !TeachingRelationShipUtil.teachingRelationshipTwoMap.containsKey(extLessonName)  ){
						continue;
					}
					long lessonId = TeachingRelationShipUtil.teachingRelationshipTwoMap.get(extLessonName);
					if(lessonId==0){
						continue;
					}
					Course c = new Course();
					c.setClassId(classId);
					c.setLessonId(lessonId);
					courseList.add(c);
				}
			}
			teacherCourseMap.put(userId, courseList);
		}
		
		//查询学校
		String schoolId =  allCommonDataService.getSchoolIdByExtId(extSchoolId,termInfoId);
		if(StringUtils.isEmptyOrWhitespaceOnly(schoolId)){
			return -1;
		}
		allCommonDataService.updateTeacherLessonBatch(Long.parseLong(schoolId), teacherCourseMap, termInfoId);
		return 0;
	}

	@Override
	public int updateClassroom(String extSchoolId,String extClassId, String extHeadClassTeacherId, List<JSONObject> teacherLessons,
			String accessToken,String clientId) throws Exception {
		//验证入参
		if(StringUtils.isEmptyOrWhitespaceOnly(extSchoolId)|| StringUtils.isEmptyOrWhitespaceOnly(extClassId)|| StringUtils.isEmptyOrWhitespaceOnly(extHeadClassTeacherId)|| StringUtils.isEmptyOrWhitespaceOnly(clientId)
				||StringUtils.isEmptyOrWhitespaceOnly(accessToken)||teacherLessons==null){
			return -1;
		}
		//解密accessToken
		String aesDecrypt = rb.getString("aes.decrypt");
		try {
			accessToken = AESUtil.Decrypt(accessToken, aesDecrypt); //clientId为每个业务手工分配的id
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(accessToken)){
			return -2;
		}
		String[] accessTokenStr = accessToken.split("&");
		//验证accessToken
		String token =authDao.getTokenByClientId(clientId);
		if(!StringUtils.isEmptyOrWhitespaceOnly(token) && !token.equals(accessTokenStr[0])  ){
			return -2;
		}
		
		String termInfoId = rb.getString("currentTermInfo");
		//查询学校
		String schoolId =  allCommonDataService.getSchoolIdByExtId(extSchoolId,termInfoId);
		if(StringUtils.isEmptyOrWhitespaceOnly(schoolId)){
			return -1;
		}
		List<String> extTeacherIds = new ArrayList<String>();
		for(JSONObject tl:teacherLessons){
			String extTeacherId = tl.getString("extTeacherId");
			if(!StringUtils.isEmptyOrWhitespaceOnly(extTeacherId) && !extTeacherIds.contains(extTeacherId)){
				extTeacherIds.add(extTeacherId);
			}
		}
		if(!StringUtils.isEmptyOrWhitespaceOnly(extHeadClassTeacherId) && !extTeacherIds.contains(extHeadClassTeacherId)){
			extTeacherIds.add(extHeadClassTeacherId);
		}
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		param.put("ids", extTeacherIds);
		List<JSONObject> userIds = csCurCommonDataDao.getUserIdsByExtId(param);
		Map<String,Long> teacherMap = new HashMap<String,Long>();
		for(JSONObject userObj : userIds){
			long userId = userObj.getLongValue("userId");
			String extId = userObj.getString("extId");
			teacherMap.put(extId, userId);
		}
		List<String> extClassIds = new ArrayList<String>();
		extClassIds.add(extClassId);
		param.put("ids", extClassIds);
		List<JSONObject> classIds = csCurCommonDataDao.getClassIdByExtclassId(param);
		long classId = 0;
		if(classIds!=null && classIds.size()>0 &&  classIds.get(0).containsKey("classId") ){
			 classId = classIds.get(0).getLongValue("classId");
		}
		 if(classId==0){
			 return -1;
		 }
		if(0==teacherMap.get(extHeadClassTeacherId)){
			return -1;
		}
		Classroom c = new Classroom();
		c.setId(classId);
		c.setTeacherId(teacherMap.get(extHeadClassTeacherId));
		List<TeacherLesson> tlList = new ArrayList<TeacherLesson>();
		for(JSONObject tlObj:teacherLessons){
			long teacherId = teacherMap.get(tlObj.getString("extTeacherId"));
			String extLessonName = tlObj.getString("extLessonName");
			if(teacherId==0 ||  !TeachingRelationShipUtil.teachingRelationshipTwoMap.containsKey(extLessonName)  ){
				continue;
			}
			long lessonId = TeachingRelationShipUtil.teachingRelationshipTwoMap.get(extLessonName);
			if(lessonId==0){
				continue;
			}
			TeacherLesson tl = new TeacherLesson();
			tl.setLessonId(lessonId);
			tl.setTeacherId(teacherId);
			tlList.add(tl);
		}
		c.setTeacherLessons(tlList);
		allCommonDataService.updateClassroom(Long.parseLong(schoolId), c, termInfoId);
		return 0;
	}

}
