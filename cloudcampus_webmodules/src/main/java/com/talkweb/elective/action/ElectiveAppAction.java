package com.talkweb.elective.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_Gender;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.elective.service.ElectiveAppService;
import com.talkweb.elective.service.ElectiveService;
/**
 * APPH5学生功能
 * @time 2016.12.19
 * @author zhanghuihui
 *
 */
@Controller
@RequestMapping(value = "/elective/")
public class ElectiveAppAction extends BaseAction{
	@Autowired
	private ElectiveAppService electiveAppService;
	@Autowired
	private ElectiveService electiveService;
    @Autowired
    private AllCommonDataService allCommonDataService;
    private static final Logger logger = LoggerFactory.getLogger(ElectiveAppAction.class);
    private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
    
    @RequestMapping(value = "studentApp/hasOpenTimeElectiveApp")
    @ResponseBody
    public JSONObject hasOpenTimeElectiveApp(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject returnObj = new JSONObject();
        JSONObject param=new JSONObject();
    	try {
    		String schoolId=requestParams.getString("schoolId");
    		String electiveId=requestParams.getString("electiveId");
			String termInfo= allCommonDataService.getCurTermInfoId( Long.parseLong(schoolId) );
			Long userId=requestParams.getLong("userId");
			User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,termInfo);
			if(user==null){
				setPromptMessage(returnObj, "-2", "系统异常,获取失败");
				return  returnObj;
			}
			T_Role role = user.getUserPart().getRole();
			if(role==null){
				setPromptMessage(returnObj, "-2", "系统异常,获取失败");
				return  returnObj;
			}
			if(role.getValue()!=T_Role.Student.getValue() && role.getValue()!=T_Role.Parent.getValue()){
				setPromptMessage(returnObj, "2", "当前用户没有学生/家长身份，请用学生/家长账号登录");
				return  returnObj;
			}
			if(role.getValue()!=T_Role.Student.getValue()){
				user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
			}
			if(user!=null && user.getStudentPart()!=null){
				Long classId = user.getStudentPart().getClassId();
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				param.put("classId",classId);
				if(StringUtils.isNotBlank(electiveId)){
					param.put("eleId",electiveId);
				}
				//param.put("isOpen", "1");
				//查询是否有 与当前登录用户相关且正在开放的选课
				logger.info("elective param:"+param.toJSONString());
				List<String> data = electiveAppService.getOpenTimeElectiveApp(param);
				logger.info("elective data:"+data );
				if(data!=null && data.size()>0){
					if(StringUtils.isNotBlank(electiveId)){
						 param.put("electiveId", data.get(0));
						 JSONObject obj = electiveAppService.getElectiveTimeByIdApp(param);
						 if(obj!=null ){
							 logger.info("elective obj:"+obj.toJSONString() );
						 }else{
							 logger.info("elective obj is null "  );
						 }
						 if(obj==null){
							setPromptMessage(returnObj, "3", "选课已删除");
						}else{
							Date startTime =  obj.getDate("startTime");
							Date endTime =  obj.getDate("endTime");
							if(startTime==null || endTime==null ){
								setPromptMessage(returnObj, "-2", "系统异常,获取失败");
							}
							Date nowDate = new Date();
							if(nowDate.before(startTime)){
								setPromptMessage(returnObj, "5", "选课未开始");
							}else if(nowDate.after(endTime)){
								setPromptMessage(returnObj, "4", "选课已结束");
							}else if (nowDate.after(startTime)&& nowDate.before(endTime)){
								setPromptMessage(returnObj, "1", "获取成功");
							}else{
								setPromptMessage(returnObj, "-2", "系统异常");
							}
						}
					}else{
						setPromptMessage(returnObj, "1", "获取成功"); //有正在开放的选课
					}
				}else if(data!=null && data.size()==0){
					if(StringUtils.isBlank(electiveId)){
						setPromptMessage(returnObj, "-2", "获取成功,无正在开放的选课"); //无正在开放的选课
					}else{
						setPromptMessage(returnObj, "3", "选课已删除"); //无正在开放的选课
					}
				}else{
					setPromptMessage(returnObj, "-2", "系统异常,获取失败");
				}
			}else{
				setPromptMessage(returnObj, "-2", "系统异常,获取失败");
			}
		} catch (Exception e) {
			logger.error("/elective/studentApp/hasOpenTimeElectiveApp",e);
			e.printStackTrace();
			setPromptMessage(returnObj, "-2", "系统异常,获取失败");
		}
		
		return returnObj;
    }
    @RequestMapping(value = "studentApp/getResultElectiveCourseApp")
    @ResponseBody
    public JSONObject getResultElectiveCourseApp(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject returnObj = new JSONObject();
        JSONObject param=new JSONObject();
    	try {
			String schoolId=requestParams.getString("schoolId");
			String termInfo= allCommonDataService.getCurTermInfoId( Long.parseLong(schoolId) );
			String schoolYear = termInfo.substring(0, 4);
			String term = termInfo.substring(4,5);
			Long userId=requestParams.getLong("userId");
			User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,termInfo);
			T_Role role = user.getUserPart().getRole();
			if(role.getValue()!=T_Role.Student.getValue()){
				user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
			}
			if(user!=null && user.getStudentPart()!=null){
				Long classId = user.getStudentPart().getClassId();
				Long accountId = user.getAccountPart().getId();
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				param.put("classId",classId);
				param.put("studentId", accountId);
				param.put("term", term);
				param.put("schoolYear", schoolYear);
				param.put("isOpen", "1"); //查询开放中的选课  ，如果没有开放中的 则查询本学期最近一次的选课轮次
				List<String> eList = electiveAppService.getOpenTimeElectiveApp(param);
				if(eList==null || eList.size()==0){
					param.remove("isOpen");
					eList = electiveAppService.getOpenTimeElectiveApp(param);
				}
				if(eList!=null && eList.size()>0){
					String electiveId = eList.get(0);
					param.put("electiveId", electiveId);//取最近一次的选课的结果
					JSONObject data = electiveAppService.getResultElectiveCourseApp(param);
					if(data!=null){
						data.put("termInfo", termInfo);
						returnObj.put("data", data);
						setPromptMessage(returnObj, "0", "获取成功"); //有正在开放的选课
					}else{
						returnObj.put("data", new JSONObject());
						setPromptMessage(returnObj, "-1", "获取失败");
					}
				}else{
					if(eList.size()==0){
						returnObj.put("data", new JSONObject());
						setPromptMessage(returnObj, "0", "获取成功"); 
					}else{
						returnObj.put("data", new JSONObject());
						setPromptMessage(returnObj, "-1", "获取失败");
					}
				}
			}else{
				returnObj.put("data", new JSONObject());
				setPromptMessage(returnObj, "-1", "获取失败");
			}
		} catch (Exception e) {
			logger.error("/elective/studentApp/getResultElectiveCourseApp",e);
			e.printStackTrace();
			returnObj.put("data", new JSONObject());
			setPromptMessage(returnObj, "-1", "获取失败");
		}
		
		return returnObj;
    }
    @RequestMapping(value = "studentApp/getElectiveCourseDetailApp")
    @ResponseBody
    public JSONObject getElectiveCourseDetailApp(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject returnObj = new JSONObject();
        JSONObject param=new JSONObject();
    	try {
			String schoolId=requestParams.getString("schoolId");
			String termInfo= allCommonDataService.getCurTermInfoId( Long.parseLong(schoolId) );
			String schoolYear = termInfo.substring(0, 4);
			String term = termInfo.substring(4,5);
			String electiveId=requestParams.getString("electiveId");
			String courseId=requestParams.getString("courseId");
		    param.put("schoolYear", schoolYear);
			param.put("term", term);
			param.put("termInfo", termInfo);
			param.put("schoolId", schoolId);
			param.put("electiveId", electiveId);
			param.put("courseId", courseId);
			Long userId=requestParams.getLong("userId");
			User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,termInfo);
			T_Role role = user.getUserPart().getRole();
			if(role.getValue()!=T_Role.Student.getValue()){
				user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
			}
			if(user!=null && user.getStudentPart()!=null){
				Long classId = user.getStudentPart().getClassId();
				param.put("classId", classId);
				JSONObject data = electiveAppService.getElectiveCourseDetailApp(param);
				if(data!=null){
					data.put("termInfo", termInfo);
					returnObj.put("data", data);
					setPromptMessage(returnObj, "0", "获取成功");
				}else{
					returnObj.put("data", new JSONObject());
					setPromptMessage(returnObj, "-1", "获取失败");
				}
			}else{
				returnObj.put("data", new JSONObject());
				setPromptMessage(returnObj, "-1", "获取失败");
			}
		} catch (Exception e) {
			logger.error("/elective/studentApp/getElectiveCourseDetailApp",e);
			e.printStackTrace();
			returnObj.put("data", new JSONObject());
			setPromptMessage(returnObj, "-1", "获取失败");
		}
		
		return returnObj;
    }
    @RequestMapping(value = "studentApp/getElectiveCourseListApp")
    @ResponseBody
    public JSONObject getElectiveCourseListApp(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject returnObj = new JSONObject();
        JSONObject param=new JSONObject();
    	try {
			String schoolId=requestParams.getString("schoolId");
			String termInfo= allCommonDataService.getCurTermInfoId( Long.parseLong(schoolId) );
			String schoolYear = termInfo.substring(0, 4);
			String term = termInfo.substring(4,5);
			Long userId=requestParams.getLong("userId");
			param.put("termInfo", termInfo);
			param.put("schoolYear", schoolYear);
			param.put("term", term);
			param.put("schoolId", schoolId);
			param.put("userId", userId);	
			User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,termInfo);
			T_Role role = user.getUserPart().getRole();
			if(role.getValue()!=T_Role.Student.getValue()){
				user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
			}
			if(user!=null && user.getStudentPart()!=null){
				Long classId = user.getStudentPart().getClassId();
				Long studentId = user.getAccountPart().getId();
				T_Gender  gender=user.getAccountPart().getGender();
				if(null!=gender)
				{
					param.put("adaptSex", gender.getValue());
				}
				param.put("classId", classId);
				param.put("isOpen", "1");
				param.put("studentId", studentId);
				List<String> eList = electiveAppService.getOpenTimeElectiveApp(param);
				if(eList!=null && eList.size()>0){
					String electiveId = eList.get(0);
					param.put("electiveId", electiveId);//取最近一次的选课的结果
				    JSONObject data = electiveAppService.getElectiveCourseListApp(param);
					if(data!=null){
						data.put("termInfo", termInfo);
						returnObj.put("data", data);
						setPromptMessage(returnObj, "0", "获取成功");
					}else{
						returnObj.put("data", new JSONObject());
						setPromptMessage(returnObj, "-1", "获取失败");
					}
				}else{
					if(eList.size()==0){
						returnObj.put("data", new JSONObject());
						setPromptMessage(returnObj, "0", "获取成功");
					}else{
						returnObj.put("data", new JSONObject());
						setPromptMessage(returnObj, "-1", "获取失败");
					}
				}
			}else{
				returnObj.put("data", new JSONObject());
				setPromptMessage(returnObj, "-1", "获取失败");
			}
    	} catch (Exception e) {
			logger.error("/elective/studentApp/getElectiveCourseListApp",e);
			e.printStackTrace();
			returnObj.put("data", new JSONObject());
			setPromptMessage(returnObj, "-1", "获取失败");
		}
		
		return returnObj;
    }
    @RequestMapping(value = "studentApp/addElectiveApp")
    @ResponseBody
    public  JSONObject addElectiveApp(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
            JSONObject json=new JSONObject();
    		String appMsg = "选课成功！";
    		int code = 0;
    		String electiveId="";
    		long classId=-1;
    		long studentId=-1;
    		long userId = -1;
    		List<String> courseIdList=new ArrayList<String>();
        	try {
        			String selectedSemester="";//requestParams.getString("selectedSemester");
        			/*if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
        				selectedSemester=getCurXnxq(req);
        			}*/
	    			String schoolId=requestParams.getString("schoolId");
	    			electiveId=requestParams.getString("electiveId");
	    			userId=requestParams.getLong("userId");
	    			String courseIds=requestParams.getString("courseIds");
	    			courseIdList = Arrays.asList(courseIds.split(","));
	    			HashMap<String,Object> nameMap=new HashMap<String,Object>();
	    			nameMap.put("schoolId", schoolId);
	    			nameMap.put("electiveId", electiveId);
	    			nameMap.put("courseIdList",courseIdList);
	    			logger.info("【elective】params：schoolId:{} electiveId:{} courseIdList：{}",schoolId,electiveId,courseIdList);
	    			List<JSONObject> courseList = electiveService.getCourseNameById(nameMap);
	    			HashMap<String,String> nameIdMap = new HashMap<String,String>();
	    			logger.info("【elective】getCourseNameById begin");
	    			for(JSONObject course:courseList){
	    				nameIdMap.put(course.getString("courseId"), course.getString("courseName"));
	    			}
	    			if(StringUtils.isNotEmpty(schoolId)&&StringUtils.isNotEmpty(electiveId)&&StringUtils.isNotEmpty(courseIds)&& null!=courseIdList && courseIdList.size()>0)
	    			{
	    				logger.info("【elective】judge the open elective time begin");
	    				//根据electiveId找到学年学期，作为allCommonDataService的参数
	    				HashMap<String, Object> selectedSemesterMap = new HashMap<String, Object>();
	    				selectedSemesterMap.put("schoolId", schoolId);
	    				selectedSemesterMap.put("electiveId", electiveId);
	    				JSONObject selectedSemesterObj=electiveService.getElectiveXnxqById(selectedSemesterMap);
	    				if(selectedSemesterObj!=null){
	    					String startTime = selectedSemesterObj.getString("startTime");
	    					String endTime = selectedSemesterObj.getString("endTime");
	    					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    					Date startDate = null;
	    					Date endDate = null;
	    					try {
	    						startDate = format.parse(startTime);
	    						endDate = format.parse(endTime);
	    						Date now = new Date();
	    						if(now.getTime()<startDate.getTime()){
	    							json.put("code", -1);
	    			        		json.put("appMsg", "失败！选课还未开放");
	    			        		json.put("success", new ArrayList<String>());
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
	    			        		obj.put("reason", "失败！选课还未开放");
	    			        		appMsgList.add(obj);
	    			        		json.put("appMsgList", appMsgList) ;
	    			        		return json;
	    						}
	    						
	    						if(now.getTime()>endDate.getTime()){
	    							json.put("code", -1);
	    							json.put("appMsg", "失败！选课已经结束");
	    							json.put("success", new ArrayList<String>());
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
	    			        		obj.put("reason", "失败！选课已经结束");
	    			        		appMsgList.add(obj);
	    			        		json.put("appMsgList", appMsgList) ;
	    							return json;
	    						}
	    						
	    					}catch(Exception e){
	    						e.printStackTrace();
	    					}
	    				}
	    				if(selectedSemesterObj!=null){
	    					String schoolYear = selectedSemesterObj.getString("schoolYear");
	    					String term = selectedSemesterObj.getString("term");
	    					selectedSemester=schoolYear+term;
		    				User user=allCommonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
		    				if(user.getUserPart().getRole().equals(T_Role.Parent))
		    				{
		    					user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),selectedSemester);
		    				}
	//	    				User user=commonDataService.getUserById(Long.valueOf(schoolId), 104514l);
		    				studentId=user.getAccountPart().getId();
		    			    classId=user.getStudentPart().getClassId();
		    				HashMap<String, Object> map = new HashMap<String,Object>();
		    				map.put("courseIds", courseIds);
		    				map.put("courseIdList", courseIdList);
		    				map.put("schoolId", schoolId);
		    				map.put("electiveId", electiveId);
		    				map.put("classId", classId);
		    				map.put("studentId", studentId);
		    				map.put("nameIdMap", nameIdMap);
		    				map.put("schoolYear", schoolYear);
		    				map.put("term", term);
		    				map.put("termInfo", selectedSemester);
		    				//判断该学生，是否已经选了某个开课时间相冲突的课程（两个浏览器 登录同一个账户 同时选）
		    				JSONObject conflictObj = electiveService.isConflict(map);
		    				List<String> conflictCourseIdList = (List<String>) conflictObj.get("conflictCourseIdList");
		    				int i=(int) conflictObj.get("returnFlag");
		    				/*//某个学生已选 政治地理，同一时间家长也提交政治地理生物 则将政治地理的courseId去掉
		    				 * List<String> courseIdList1 = (List<String>) conflictObj.get("courseIdList");
		    				String courseIds1 = (String) conflictObj.get("courseIds");
		    				if(StringUtils.isBlank(courseIds1) && StringUtils.isNotBlank(courseIds)){
		    					json.put("code", -1);
			            		json.put("appMsg", "失败!您已选择了该课程");
			            		json.put("success", new ArrayList<String>());
			            		return json;
		    				}*/
		    				/*if(i==-1){ //开课时间
		    					json.put("code", -1);
			            		json.put("appMsg", "失败！您已选择了该开课时间下的课程");
			            		json.put("success", new ArrayList<String>());
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
    			        		obj.put("reason", "失败！您已选择了该开课时间下的课程");
    			        		appMsgList.add(obj);
    			        		json.put("appMsgList", appMsgList) ;
			            		return json;
		    				}else */
		    				if(i==-2){ //课程类别冲突判断
		    					json.put("code", -1);
			            		json.put("appMsg", "失败！您选择的类别数量不在可选范围之内");
			            		json.put("success", new ArrayList<String>());
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
    			        		obj.put("reason", "失败！您选择的类别数量不在可选范围之内");
    			        		appMsgList.add(obj);
    			        		json.put("appMsgList", appMsgList) ;
			            		return json;
		    				}else if(i==-3){//课程数量和课时冲突判断
		    					json.put("code", -1);
			            		json.put("appMsg", "失败！您选择的课程数量不在可选范围之内");
			            		json.put("success", new ArrayList<String>());
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
    			        		obj.put("reason", "失败！您选择的课程数量不在可选范围之内");
    			        		appMsgList.add(obj);
    			        		json.put("appMsgList", appMsgList) ;
			            		return json;
		    				}else if(i==-4){
		    					json.put("code", -1);
			            		json.put("appMsg", "失败！您选择的课程课时不在可选范围之内");
			            		json.put("success", new ArrayList<String>());
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
    			        		obj.put("reason", "失败！您选择的课程课时不在可选范围之内");
    			        		appMsgList.add(obj);
    			        		json.put("appMsgList", appMsgList) ;
			            		return json;
		    				}
		    				//学生所在班级所有选课对应的人数
		    				long time0=new Date().getTime();
		    				logger.info("#########【elective】student elective begin：courseids:{} studentAccountId:{}  electiveId:{} #################",courseIds,studentId,electiveId);
		    				 //map.put("courseIdList", courseIdList1);
		    				if(conflictCourseIdList!=null && conflictCourseIdList.size()>0){
		    					List<String> addCourseIdList= new ArrayList<String>();
		    					for(String cId:courseIdList){
		    						if(!conflictCourseIdList.contains(cId)){
		    							addCourseIdList.add(cId);	
		    						}
		    					}
		    					//courseIdList.removeAll(conflictCourseIdList);
		    					map.put("courseIdList",addCourseIdList);
		    					map.put("conflictCourseIdList", conflictCourseIdList);
		    				}
		    				json = electiveService.addElective(map);
		    				long time1=new Date().getTime();
		    				logger.info("#####################【elective】student elective end:need times:{}###############",(time1-time0));
	    			  } //end of selectedSemesterObj!=null
	    			}//end of courseIds is not empty
	    			else
	    			{
	        			code = -1;
	        			appMsg = "失败！参数异常";
	        			json.put("code", code);
	            		json.put("appMsg", appMsg);
	            		json.put("success", new ArrayList<String>());
	    			}
    			} catch (Exception e) {
        			// TODO Auto-generated catch block
    				logger.error("/elective/student/addElective",e);
        			e.printStackTrace();
        			code = -1;
        			appMsg = "失败！网络超时";
        			json.put("code", code);
            		json.put("appMsg", appMsg);
            		json.put("success", new ArrayList<String>());
        		}
        		logger.info("【elective】(4)addElective：elective：{} json：{} courseIds:{} classId:{} studentId:{}",electiveId,json,courseIdList,classId,studentId);
        		return json;
            }
    @RequestMapping(value = "studentApp/meetElectiveApp")
    @ResponseBody
    public JSONObject meetElectiveApp(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject returnObj = new JSONObject();
        JSONObject param=new JSONObject();
    	try {
			String schoolId=requestParams.getString("schoolId");
			String termInfo= allCommonDataService.getCurTermInfoId( Long.parseLong(schoolId) );
			String schoolYear = termInfo.substring(0, 4);
			String term = termInfo.substring(4,5);
			String electiveId=requestParams.getString("electiveId");
			Long userId=requestParams.getLong("userId");
			param.put("termInfo", termInfo);
			param.put("schoolYear", schoolYear);
			param.put("term", term);
			param.put("schoolId", schoolId);
			param.put("electiveId", electiveId);
			param.put("userId", userId);	
			User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,termInfo);
			T_Role role = user.getUserPart().getRole();
			if(role.getValue()!=T_Role.Student.getValue()){
				user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
			}
			if(user!=null && user.getStudentPart()!=null){
				Long classId = user.getStudentPart().getClassId();
				Long studentId = user.getAccountPart().getId();
				param.put("classId", classId);
				param.put("studentId", studentId);
			    int isMeet = electiveAppService.meetElectiveApp(param);
				returnObj.put("isMeet", isMeet);
				setPromptMessage(returnObj, "0", "获取成功");
			}else{
				setPromptMessage(returnObj, "-1", "获取失败");
			}
    	} catch (Exception e) {
			logger.error("/elective/studentApp/meetElectiveApp",e);
			e.printStackTrace();
			setPromptMessage(returnObj, "-1", "获取失败");
		}
		
		return returnObj;
    }
}
