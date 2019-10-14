package com.talkweb.elective.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.elective.service.ElectiveService;

/** 
 * @author  作者 E-mail: zhuxiaoyue@talkweb.com.cn
 * @date 创建时间：2015年7月23日 下午3:40:08 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @update 添加多服务器并发处理， 2016.08.10 By:zhh
 */
@Controller
@RequestMapping(value = "/elective/student/")
public class ElectiveStudentAction extends BaseAction{
	@Autowired
	private ElectiveService electiveService;
    @Autowired
    private AllCommonDataService allCommonDataService;
    /**
	 * redis
	 */
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	private static final Logger logger = LoggerFactory.getLogger(ElectiveStudentAction.class);
    
	/**
	 * 选课人数统计
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getElectiveList")
    @ResponseBody
    public JSONObject getElectiveList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	    JSONObject data = new JSONObject();
            JSONObject json=new JSONObject();
    		String msg = "";
    		int code = 0;
        	try {
        		String selectedSemester="";//requestParams.getString("selectedSemester");
        		/*if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
        			selectedSemester=getCurXnxq(req);
        		}*/
    			String schoolId=getXxdm(req);
    			List<JSONObject> list=electiveService.getCurrentElective(schoolId);
    			if(null!=list && list.size()>0)
    			{
    				JSONObject electiveInfo=list.get(0);
    				selectedSemester=electiveInfo.getString("schoolYear")+electiveInfo.getString("term");
    				String electiveId=electiveInfo.getString("electiveId");
    				data.put("electiveId", electiveId);
    				data.put("electiveName", electiveInfo.getString("electiveName"));
    				data.put("startTime", electiveInfo.getString("startTime").substring(0,19));
    				data.put("endTime", electiveInfo.getString("endTime").substring(0,19));
    				HttpSession sess = req.getSession();
    				User user=(User)(sess.getAttribute("user"));
    				
    				if(user.getUserPart().getRole().equals(T_Role.Parent))
    				{
    					user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),selectedSemester);
    				}
//    				User user=commonDataService.getUserById(Long.valueOf(schoolId), 104514l);
    				data.put("studentName", user.getAccountPart().getName());
    				Long classId=user.getStudentPart().getClassId();
    				data.put("className", allCommonDataService.getClassById(Long.valueOf(schoolId),classId,selectedSemester).getClassName());
    				
    				HashMap<String,Object> map=new HashMap<String,Object>();
    				map.put("schoolId", schoolId);
    				map.put("electiveId", electiveId);
    				map.put("classId", classId);
    				map.put("termInfo", selectedSemester);
    				//班级选课数量、课时要求
    				List<JSONObject> requireClass=   electiveService.getElectiveCourseRequire(map);
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
    				
    				data.put("classElectiveRequire", classElectiveRequire); 
    				
    				List<JSONObject> requireTypeClass=  electiveService.getCourseTypeNumList(map);
    				data.put("courseTypeRequire", requireTypeClass); 
    				//只筛选和学生性别符合的课程
    				T_Gender  gender=user.getAccountPart().getGender();
    				if(null!=gender)
    				{
    					map.put("adaptSex", gender.getValue());
    				}
    				map.put("studentId", user.getAccountPart().getId());
    				/*School school = allCommonDataService.getSchoolById(Long.parseLong(schoolId), selectedSemester);
    				List<Account> aList = allCommonDataService.getAllStudent(school, selectedSemester);
    				List<Long> studentIdList= new ArrayList<Long>(); 
    				for(Account a:aList){
    					studentIdList.add(a.getId());
    				}
    				map.put("studentIdList", studentIdList);
    				*/
    				List<JSONObject> courseList=electiveService.getCurrentCourseAllInfo(map);
    				data.put("rows", courseList); 
    				data.put("termInfo", selectedSemester);
    			}//end of list !null or >0
    			
        	} catch (Exception e) {
    			// TODO Auto-generated catch block
        		logger.error("/elective/student/getElectiveList",e);
    			e.printStackTrace();
    			code = -1;
    			msg = "获取失败！";
    		}
    		json.put("code", code);
    		json.put("msg", msg);
    		json.put("data", data);
    		return json;
        }
    
	/**
	 * 学生选课
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 * @update 2016.8.9 添加多机并发处理 By：zhh
	 * @update 2016.8.13 加选课模块移入service层做事务控制 By：zhh
	 * @update 添加后端冲突判断控制  2016.09.06
	 */
    @RequestMapping(value = "addElective")
    @ResponseBody
    public  JSONObject addElective(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
            JSONObject json=new JSONObject();
    		String appMsg = "选课成功！";
    		int code = 0;
    		String electiveId="";
    		long classId=-1;
    		long studentId=-1;
    		List<String> courseIdList=new ArrayList<String>();
        	try {
        			String selectedSemester="";//requestParams.getString("selectedSemester");
        			/*if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
        				selectedSemester=getCurXnxq(req);
        			}*/
	    			String schoolId=getXxdm(req);
	    			electiveId=requestParams.getString("electiveId");
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
	    			        		json.put("msg", "失败！选课还未开放");
	    			        		json.put("success", new ArrayList<String>());
	    			        		return json;
	    						}
	    						
	    						if(now.getTime()>endDate.getTime()){
	    							json.put("code", -1);
	    							json.put("appMsg", "失败！选课已经结束");
	    							json.put("msg", "失败！选课已经结束");
	    							json.put("success", new ArrayList<String>());
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
		    				HttpSession sess = req.getSession();
		    				User user=(User)(sess.getAttribute("user"));
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
		    				/*List<String> courseIdList1 = (List<String>) conflictObj.get("courseIdList");
		    				String courseIds1 = (String) conflictObj.get("courseIds");
		    				if(StringUtils.isBlank(courseIds1) && StringUtils.isNotBlank(courseIds)){
		    					json.put("code", -1);
			            		json.put("msg", "选课失败，您已选择了该课程");
			            		return json;
		    				}*/
		    				/*if(i==-1){ //开课时间
		    					json.put("code", -1);
			            		json.put("appMsg", "失败!您已选择了该开课时间下的课程");
			            		json.put("msg", "失败!您已选择了该开课时间下的课程");
			            		json.put("success", new ArrayList<String>());
			            		return json;
		    				}else */
		    				if(i==-2){ //课程类别冲突判断
		    					json.put("code", -1);
			            		json.put("appMsg", "失败!您选择的类别数量不在可选范围之内");
			            		json.put("msg", "失败!您选择的类别数量不在可选范围之内");
			            		json.put("success", new ArrayList<String>());
			            		return json;
		    				}else if(i==-3){//课程数量和课时冲突判断
		    					json.put("code", -1);
			            		json.put("appMsg", "失败!您选择的课程数量不在可选范围之内");
			            		json.put("msg", "失败!您选择的课程数量不在可选范围之内");
			            		json.put("success", new ArrayList<String>());
			            		return json;
		    				}else if(i==-4){
		    					json.put("code", -1);
			            		json.put("appMsg", "失败!您选择的课程课时不在可选范围之内");
			            		json.put("msg", "失败!您选择的课程课时不在可选范围之内");
			            		json.put("success", new ArrayList<String>());
			            		return json;
		    				}
		    				//学生所在班级所有选课对应的人数
		    				long time0=new Date().getTime();
		    				logger.info("#########【elective】student elective begin：courseids:{} studentAccountId:{}  electiveId:{} #################",courseIds,studentId,electiveId);
		    			   /* map.put("courseIdList", courseIdList1);
		    			    map.put("courseIds", courseIds1);*/
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
	        			appMsg = "失败!参数异常";
	        			json.put("code", code);
	            		json.put("appMsg", appMsg);
	            		json.put("success", new ArrayList<String>());
	    			}
    			} catch (Exception e) {
        			// TODO Auto-generated catch block
    				logger.error("/elective/student/addElective",e);
        			e.printStackTrace();
        			code = -1;
        			appMsg = "失败!网络超时";
        			json.put("code", code);
            		json.put("appMsg", appMsg);
            		json.put("success", new ArrayList<String>());
        		}
        		logger.info("【elective】(4)addElective：elective：{} json：{} courseIds:{} classId:{} studentId:{}",electiveId,json,courseIdList,classId,studentId);
        		return json;
            }
}
