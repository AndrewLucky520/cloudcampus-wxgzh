package com.talkweb.elective.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.StringNumTool;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.elective.dao.ElectiveAppDao;
import com.talkweb.elective.dao.ElectiveDao;
import com.talkweb.elective.service.ElectiveAppService;

@Service
public class ElectiveAppServiceImpl implements ElectiveAppService{
	@Autowired
	private ElectiveDao electiveDao;
	@Autowired
	private ElectiveAppDao electiveAppDao;
   
    @Autowired
    private AllCommonDataService allCommonDataService;
    
    private static final Logger logger = LoggerFactory.getLogger(ElectiveAppServiceImpl.class);
    /**
  	 * redis
  	 */
  	@Resource(name="redisOperationDAOSDRTempDataImpl")
   	private RedisOperationDAO redisOperationDAO;
	@Override
	public List<String> getOpenTimeElectiveApp(JSONObject param) throws Exception {
		return electiveAppDao.getOpenTimeElectiveApp(param);
	}
	@Override
	public JSONObject getResultElectiveCourseApp(JSONObject param) throws Exception {
		JSONObject returnObj = electiveAppDao.getElectiveApp(param);
		List<JSONObject> courseList = electiveAppDao.getSelectedElectiveCourseApp(param);
		
		//判断没有图片的课程是出 默认还是直接不显示
		  //依据是这个轮次是否有一个课程有图片数据
		JSONObject json = new JSONObject();
		json.put("electiveId", param.getString("electiveId"));
		json.put("schoolId", param.getString("schoolId"));
		json.put("termInfo", param.getString("termInfo"));
		List<JSONObject> attachmentList = electiveDao.getAttachment(json);
		int attachmentWay = 0; //默认不显示   0不显示 1显示
		if(attachmentList.size()>0){
			attachmentWay=1;
		}
		Map<String,String> attMap = new HashMap<String,String>();
		for(JSONObject attObj:attachmentList){
			attMap.put(attObj.getString("courseId"), attObj.getString("attachmentId"));
		}
		
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", param.getString("schoolId"));
		map.put("termInfo", param.getString("termInfo"));
		map.put("electiveId", param.getString("electiveId"));
		List<JSONObject> timeList=electiveDao.getElectiveCourseSchoolTime(map);
		HashMap<String,String> timeMap=new HashMap<String,String>();
		HashMap<String,Object> dayOfWeekMap=new HashMap<String,Object>();
		HashMap<String,Integer> courseWeekType=new HashMap<String,Integer>();//周次
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
					text+=";"+StringNumTool.getDayOfWeek(dayOfWeek)+" "+lessonOfDay;
					dayOfWeekMap.put(courseId+dayOfWeek, "in");
				}
				timeMap.put(courseId, text);
			}
			else
			{
				String text=StringNumTool.getDayOfWeek(dayOfWeek)+" "+lessonOfDay;
				dayOfWeekMap.put(courseId+dayOfWeek, "in");
				timeMap.put(courseId, text);
			}
		}
		
		for(JSONObject cObj:courseList){
			String courseId = cObj.getString("courseId");
			String attId = attMap.get(courseId);
			if(StringUtils.isNotBlank(attId)){
				cObj.put("attachmentId", attId);
			}else{
				if(attachmentWay==1){
					cObj.put("attachmentId", "");
				}
			}
			cObj.put("schoolTime", StringNumTool.getSchoolTimeText(timeMap.get(cObj.getString("courseId")), courseWeekType.get(cObj.getString("courseId"))));
		}
		returnObj.put("courseList", courseList);
		return returnObj;
	}
	@Override
	public JSONObject getElectiveCourseDetailApp(JSONObject param) throws Exception {
		JSONObject returnObj = electiveAppDao.getSingleElectiveCourseApp(param);
		//获取开课时间
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", param.getString("schoolId"));
		map.put("termInfo", param.getString("termInfo"));
		map.put("electiveId", param.getString("electiveId"));
		map.put("courseId", param.getString("courseId"));
		List<JSONObject> timeList=electiveDao.getElectiveCourseSchoolTime(map);
		returnObj.put("schoolTime", timeList);
		HashMap<String,String> timeMap=new HashMap<String,String>();
		HashMap<String,Object> dayOfWeekMap=new HashMap<String,Object>();
		HashMap<String,Integer> courseWeekType=new HashMap<String,Integer>();//周次
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
					text+=";"+StringNumTool.getDayOfWeek(dayOfWeek)+" "+lessonOfDay;
					dayOfWeekMap.put(courseId+dayOfWeek, "in");
				}
				timeMap.put(courseId, text);
			}
			else
			{
				String text=StringNumTool.getDayOfWeek(dayOfWeek)+" "+lessonOfDay;
				dayOfWeekMap.put(courseId+dayOfWeek, "in");
				timeMap.put(courseId, text);
			}
		}
		returnObj.put("schoolTimeText", StringNumTool.getSchoolTimeText(timeMap.get(param.getString("courseId")), courseWeekType.get(param.getString("courseId"))));
		//获取附件
		List<JSONObject> attList = electiveDao.getAttachment(param);
		if(attList!=null && attList.size()>0){
			returnObj.put("attachmentId", attList.get(0).getString("attachmentId"));
		}else{
			returnObj.put("attachmentId", "");
		}
		//获取课程已选人数
		returnObj.put("selectedNum", "0");
		JSONObject obj = electiveAppDao.getSingleSelectedCourseStudent(map);
		if(obj!=null&& StringUtils.isNotBlank(obj.getString("selectedNum"))){
			returnObj.put("selectedNum", obj.getString("selectedNum"));
		}
		//获取班内已选人数
		returnObj.put("classSelectedNum", "0");
		map.put("classId", param.getString("classId"));
		List<JSONObject> courseClassNumList = electiveDao.getCourseClassNum(map);
		if(courseClassNumList!=null && courseClassNumList.size()>0){
			JSONObject courseClassNum = courseClassNumList.get(0);
			String selectedNum = courseClassNum.getString("selectedNum");
			returnObj.put("classSelectedNum", selectedNum);
		}
		return returnObj;
	}
	@Override
	public JSONObject getElectiveCourseListApp(JSONObject param) throws Exception {
		String schoolId = param.getString("schoolId");
		String electiveId = param.getString("electiveId");
		String classId = param.getString("classId");
		String termInfo = param.getString("termInfo");
		String studentId = param.getString("studentId");
		String adaptSex = param.getString("adaptSex");
		JSONObject returnObj = electiveAppDao.getSingleElectiveApp(param);
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("electiveId", electiveId);
		map.put("classId", classId);
		map.put("termInfo", termInfo);
		map.put("studentId", studentId);
		map.put("adaptSex",adaptSex);
		//班级选课数量、课时要求
		List<JSONObject> requireClass = electiveDao.getElectiveCourseRequire(map);
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
		returnObj.put("classElectiveRequire", classElectiveRequire); 
		//获取课程类别要求
		List<JSONObject> requireTypeClass = electiveDao.getCourseTypeNumList(map);
		returnObj.put("courseTypeRequire", requireTypeClass); 
		map.put("isApp", true);
		List<JSONObject> courseList=electiveDao.getCurrentCourseAllInfo(map);
		
		//判断没有图片的课程是出 默认还是直接不显示
		  //依据是这个轮次是否有一个课程有图片数据
		JSONObject json = new JSONObject();
		json.put("electiveId", param.getString("electiveId"));
		json.put("schoolId", param.getString("schoolId"));
		json.put("termInfo", param.getString("termInfo"));
		List<JSONObject> attachmentList = electiveDao.getAttachment(json);
		if(attachmentList.size()<=0){
			for(JSONObject courseObj : courseList){
				courseObj.remove("attachmentId");
			}
		}
		returnObj.put("courseList", courseList);  
		return returnObj;
	}
	@Override
	public int meetElectiveApp(JSONObject param) throws Exception {
		// 0 不满足选课要求   1满足选课要求
		int result = 1; //默认
		String schoolId = param.getString("schoolId");
		String electiveId = param.getString("electiveId");
		String termInfo = param.getString("termInfo");
		String classId = param.getString("classId");
		String studentId = param.getString("studentId");
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("electiveId", electiveId);
		map.put("classId", classId);
		map.put("termInfo", termInfo);
		//班级限制
		List<JSONObject> requireClass = electiveDao.getElectiveCourseRequire(map);
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
		//类别限制
		List<JSONObject> requireTypeClass = electiveDao.getCourseTypeNumList(map);
		
		//获取自己已提交的课程
		map.put("studentId", studentId);
		List<JSONObject> stuCourseList=electiveDao.getStudentCourseIds(map);
		String courseIds="";
		List<String> courseIdList = new ArrayList<String>();
		if(null!=stuCourseList && stuCourseList.size()>0)
		{
			courseIds=stuCourseList.get(0).getString("courseIds");
		}
		if(StringUtils.isNotBlank(courseIds)){
			courseIdList = Arrays.asList(courseIds.split(","));
		}
		//获取所有课程列表
		List<JSONObject> courseList = electiveDao.getCurrentCourse(map);
		//courseTypeId-已选改type的数量
		int courseHourNum = 0;
		  //已选的课程类别 的数量
		Map<String,Integer> typeMap = new HashMap<String,Integer>();
		for(JSONObject courseObj:courseList){
			  String courseId = courseObj.getString("courseId");
			  if(!courseIdList.contains(courseId)){
				  continue;	
		      }
			  //是当前用户选的课程
			   //计算课程类别数量
			  String courseTypeId=courseObj.getString("courseTypeId");
			  if(typeMap.containsKey(courseTypeId)&&typeMap.get(courseTypeId)!=null){
				  Integer num = typeMap.get(courseTypeId);
				  typeMap.put(courseTypeId, ++num);
			  }else{
				  typeMap.put(courseTypeId, 1);
			  }
		}
		//获取
		List<JSONObject> timeList=electiveDao.getSchoolTimeByClassID(map);
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
		double classHour = 0.0; //课时（已选+将选）
		for(String s:courseIdList){ //已选的课程
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
		
		//-------------------------------------
		//班级课时判断
		Double classhourUpperLimit = classElectiveRequire.getDouble("classhourUpperLimit");
		Double classhourLowerLimit = classElectiveRequire.getDouble("classhourLowerLimit");
		if(classhourUpperLimit==null){
			classhourUpperLimit=Double.MAX_VALUE;
		}
		if(classhourLowerLimit==null){
			classhourLowerLimit=-1.0D;
		}
		/*if(!(classHour<=classhourUpperLimit && classHour>= classhourLowerLimit)){
			result=0;
		}*/
		if(!(classHour>= classhourLowerLimit)){
			result=0;
		}
		//班级门数判断
		int classCount = courseIdList.size();
		Integer courseLowerLimit = classElectiveRequire.getInteger("courseLowerLimit");
		Integer  courseUpperLimit= classElectiveRequire.getInteger("courseUpperLimit");
		  if(courseLowerLimit==null ){
			  courseLowerLimit = Integer.MIN_VALUE;
		  }
		  if(courseUpperLimit==null){
			  courseUpperLimit=Integer.MAX_VALUE;
		  }
		 /*if(!(classCount<=courseUpperLimit && classCount>=courseLowerLimit)){
			 result=0;
		 }*/
		 if(!(classCount>=courseLowerLimit)){
			 result=0;
		 }
		//类别门数判断
		 for(JSONObject type:requireTypeClass){
			 if(result==0){break;}
			 String courseTypeId = type.getString("courseTypeId");
			 Integer cLowerLimit = type.getInteger("courseLowerLimit");
			 Integer  cUpperLimit= type.getInteger("courseUpperLimit");
			 if(cLowerLimit==null ){
				  cLowerLimit = Integer.MIN_VALUE;
			 }
			 if(cUpperLimit==null){
				  cUpperLimit=Integer.MAX_VALUE;
			 }
			 Integer sortCount = typeMap.get(courseTypeId);
			 if(sortCount==null){sortCount=0;}
			/* if(!(sortCount<=courseUpperLimit && sortCount>=courseLowerLimit)){
				 result=0;
			 }*/
			 if(!(sortCount>=cLowerLimit)){
				 result=0;
			 }
		 }
		return result;
	}
	@Override
	public JSONObject getElectiveTimeByIdApp(JSONObject param) {
		return electiveAppDao.getElectiveTimeByIdApp(param);
	}
}
