package com.talkweb.elective.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringNumTool;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.elective.service.ElectiveService;

/** 
 * @author  作者 E-mail: zhuxiaoyue@talkweb.com.cn
 * @date 创建时间：2015年7月21日 下午6:24:34 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Controller
@RequestMapping(value = "/elective/static/")
public class ElectiveStaticAction extends BaseAction{
	
	@Autowired
	private ElectiveService electiveService;
    @Autowired
    private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(ElectiveStaticAction.class);
    
	/**
	 * 选课人数统计
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "staticSelectedNum")
    @ResponseBody
    public JSONObject staticSelectedNum(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	    JSONObject data = new JSONObject();
            JSONObject json=new JSONObject();
    		String msg = "";
    		int code = 0;
        	try {
    			String schoolId=getXxdm(req);
    			long sid=Long.valueOf(schoolId);
    			String selectedSemester=requestParams.getString("selectedSemester");
    			if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
    				selectedSemester=getCurXnxq(req);
    			}
    			String electiveId=requestParams.getString("electiveId");
    			HashMap<String,Object> map=new HashMap<String,Object>();
    			map.put("schoolId", schoolId);
    			map.put("electiveId", electiveId);
    			map.put("termInfo", selectedSemester);
    			List<JSONObject> courseList=electiveService.getElectiveCourse(map);
    			List<JSONObject> timeList=electiveService.getElectiveCourseSchoolTime(map);
    			/*School school = allCommonDataService.getSchoolById(Long.parseLong(schoolId), selectedSemester);
				List<Account> aList = allCommonDataService.getAllStudent(school, selectedSemester);
				List<Long> studentIdList= new ArrayList<Long>(); 
				for(Account a:aList){
					studentIdList.add(a.getId());
				}
				map.put("studentIdList", studentIdList);*/
    			List<JSONObject> selectedList=electiveService.getSelectedCourseNum(map);
    			HashMap<String,Integer> selectedMap=new HashMap<String,Integer>();
    			for(JSONObject o:selectedList)
    			{
    				selectedMap.put(o.getString("courseId"), o.getIntValue("selectedNum"));
    			}
    			List<Long> shouldClassList=electiveService.getShouldSelectedCourseNum(map);
    			List<Classroom>  cList=allCommonDataService.getClassroomBatch(sid,shouldClassList,selectedSemester);
    			int shouldSum=0;//应选课人数
    			Set<Long> removeIds = new HashSet<Long>();
    			for(Classroom c:cList)
    			{
    				
    				List<Long> aids=c.getStudentAccountIds();
    				if(null!=aids)
    				{
    					shouldSum+=c.getStudentAccountIds().size();
    					removeIds.addAll(aids);
    				}    				
    			}
    			shouldSum=removeIds.size();
    			int totalSum= electiveService.getTotalSubmittedNum(map);//已提交学生数
    			
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
    						text+=";"+StringNumTool.getDayOfWeek(dayOfWeek)+lessonOfDay;
    						dayOfWeekMap.put(courseId+dayOfWeek, "in");
    					}
    					timeMap.put(courseId, text);
    				}
    				else
    				{
    					String text=StringNumTool.getDayOfWeek(dayOfWeek)+lessonOfDay;
    					dayOfWeekMap.put(courseId+dayOfWeek, "in");
    					timeMap.put(courseId, text);
    				}
    			}
    			
    			for(JSONObject j:courseList)
    			{
    				 if(null==timeMap.get(j.getString("courseId"))||null==courseWeekType.get(j.getString("courseId")))
    				 {
    					 j.put("schoolTime", "");
    				 }
    				 else
    				 {
    					 j.put("schoolTime", StringNumTool.getSchoolTimeText(timeMap.get(j.getString("courseId")),courseWeekType.get(j.getString("courseId"))));
    				 }
    		         j.put("adaptSex", StringNumTool.getSex(j.getIntValue("adaptSex")));
    		         String courseId=j.getString("courseId");
    		         if(selectedMap.containsKey(courseId))
    		         {
        		         j.put("selectedNum", selectedMap.get(j.getString("courseId"))); 
    		         }
    		         else
    		         {
        		         j.put("selectedNum", 0);
    		         }
    		         j.put("offerGrade",StringNumTool.getOfferGradeText(JSONArray.parseArray(j.getString("offerGrade"))));
    		         
    			}
    			data.put("totalCourse", courseList.size());
    			data.put("selectedCourseNum", shouldSum);
    			data.put("submittedNum", totalSum);
    			data.put("noSubmittedNum", shouldSum-totalSum);    			
    			data.put("rows", courseList);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			logger.error("/elective/static/staticSelectedNum:",e);
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
	 * 选课人数统计
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "getNoSelectedStudentList")
    @ResponseBody
    public JSONObject getNoSelectedStudentList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	    JSONObject data = new JSONObject();
            JSONObject json=new JSONObject();
    		String msg = "";
    		int code = 0;
        	try {
    			String schoolId=getXxdm(req);
    			String selectedSemester= requestParams.getString("selectedSemester");
    			if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
    				selectedSemester= getCurXnxq(req);
    			}
    			long sid=Long.valueOf(schoolId);
    			String electiveId= requestParams.getString("electiveId");
    			String useGrade= requestParams.getString("useGrade");
    			String classIds= requestParams.getString("classIds");
    			HashMap<String,Object> map=new HashMap<String,Object>();
    			map.put("schoolId", schoolId);
    			map.put("electiveId", electiveId);
    			map.put("useGrade", useGrade);
    			map.put("classIds", Arrays.asList(classIds.split(",")));
    			List<JSONObject> list=new ArrayList<JSONObject>();
    			List<Long> shouldClassList=electiveService.getShouldSelectedCourseNum(map);
    			logger.info("shouldClassList:"+shouldClassList);
    			List<Classroom>  cList= new ArrayList<Classroom>();
    			if(shouldClassList!=null && shouldClassList.size()>0){
    				  cList=allCommonDataService.getClassroomBatch(sid,shouldClassList,selectedSemester);
    			}
    			logger.info("cList:"+cList.size());
    			/*School school = allCommonDataService.getSchoolById(Long.parseLong(schoolId), selectedSemester);
				List<Account> aList = allCommonDataService.getAllStudent(school, selectedSemester);
				List<Long> studentIdList= new ArrayList<Long>(); 
				for(Account a:aList){
					studentIdList.add(a.getId());
				}
				map.put("studentIdList", studentIdList);*/
    			map.put("termInfo", selectedSemester);
    			List<Long> selectedStudentIds=electiveService.getSubmittedStudentIds(map);               
    			List<Long> shouldStudentIds=new ArrayList<Long>();
                HashMap<String,String> classMap=new HashMap<String,String>();
    			for(Classroom c:cList)
    			{
    				List<Long> ids=c.getStudentAccountIds();
    				if(null!=ids && ids.size()>0)
    				{
        				shouldStudentIds.addAll(ids);
    				}
    				classMap.put(c.getId()+"", c.getClassName());	
    			}
//    			List<Long> noSeletedIds=new ArrayList<Long>();
//    			for(Long l:shouldStudentIds)
//    			{
//    				if(!selectedStudentIds.contains(l))
//    				{
//    					noSeletedIds.add(l);
//    				}
//    			}
    			logger.info("shouldStudentIds:"+shouldStudentIds+" selectedStudentIds:"+selectedStudentIds);
    			if(shouldStudentIds.size()>0){
    				shouldStudentIds.removeAll(selectedStudentIds);
    			}    			
    			logger.info("shouldStudentIds:"+shouldStudentIds);
    			if(shouldStudentIds.size()>0)
    			{
        			List<Account> accountList=allCommonDataService.getAccountBatch(sid,shouldStudentIds,selectedSemester);
        		    for(Account a:accountList)
                    {
        		    	if(a.getUsers()==null){
        		    		continue;
        		    	}
            			for(User u:a.getUsers())
                    	{
            				if(u==null||u.getUserPart()==null||u.getUserPart().getRole()==null||u.getStudentPart()==null){
            					continue;
            				}
            				StudentPart sp=u.getStudentPart();
	                		if(u.getUserPart().getRole().equals(T_Role.Student)&&classMap.containsKey(sp.getClassId()+""))
	                		{	                			
	                			JSONObject obj=new JSONObject();
	                			obj.put("studentId", a.getId());
	                			obj.put("studentName", a.getName());
	                			obj.put("studentNum", sp.getSchoolNumber());
	                			obj.put("className", classMap.get(sp.getClassId()+""));
	                			list.add(obj);
	                			break;
	                		}
	                	}
                    }
    			}
    			//list=(List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, list, "className");
    			Collections.sort(list, new Comparator<JSONObject>(){
					@Override
					public int compare(JSONObject s1, JSONObject s2) {
						return s1.getString("className").compareTo( s2.getString("className"));
					}
				});
    			data.put("rows", list);
    			data.put("noSubmittedNum", list.size());

    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			logger.error("/elective/static/getNoSelectedStudentList:",e);
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
	 * 按课程查看
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "staticByCourse")
    @ResponseBody
    public JSONObject staticByCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	    JSONArray arr=new JSONArray();
            JSONObject json=new JSONObject();
    		String msg = "";
    		int code = 0;
        	try {
        		String selectedSemester=requestParams.getString("selectedSemester");
        		if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
    				selectedSemester=getCurXnxq(req);
    			}
        		String schoolId=getXxdm(req);
    			long sid=Long.valueOf(schoolId);
    			String electiveId=requestParams.getString("electiveId");
    			String courseIds=requestParams.getString("courseIds");
    			String useGrade=requestParams.getString("useGrade");
    			HashMap<String,Object> map=new HashMap<String,Object>();
    			map.put("schoolId", schoolId);
    			map.put("electiveId", electiveId);
    			map.put("termInfo", selectedSemester);
    			if(StringUtils.isNotEmpty(courseIds))
    			{
    				/*School school = allCommonDataService.getSchoolById(Long.parseLong(schoolId), selectedSemester);
	    			List<Account> aList = allCommonDataService.getAllStudent(school, selectedSemester);
					List<Long> studentIdList= new ArrayList<Long>(); 
					for(Account a:aList){
						studentIdList.add(a.getId());
					}
					map.put("studentIdList", studentIdList);*/
    				if(StringUtils.isNotEmpty(useGrade))
    				{
    					map.put("gradeList", Arrays.asList(useGrade.split(",")));
    				}    				
    				List<String> courseIdList = new ArrayList<String>();  
    				courseIdList.addAll(Arrays.asList(courseIds.split(",")));
    				if(courseIdList.size()==1)
    				{
            			map.put("courseIdList", courseIdList);
    				}
    	    		//课程+班级代码-班级课程已选人数
        			List<JSONObject> selectedNumList=electiveService.getCourseClassNum(map);
        			HashMap<String,Integer> selectedNumMap=new HashMap<String,Integer>();
        			for(JSONObject o:selectedNumList)
        			{
        				selectedNumMap.put(o.getString("courseId")+o.getString("classId"), o.getIntValue("selectedNum"));
        			}
        			//班级代码-班级名称
        			List<Long> selectedCids=electiveService.getSubmittedClassIds(map);
        			HashMap<String,String> classMap=new HashMap<String,String>();
        			List<Classroom> classList=allCommonDataService.getClassroomBatch(sid,selectedCids,selectedSemester);
        			for(Classroom o:classList)
        			{
        				classMap.put(o.getId()+"", o.getClassName());
        			}
        			//学生代码-json
        			List<Long> selectedStudentIds=electiveService.getSubmittedStudentIds(map);
//        			List<Account> accountList=new ArrayList<Account>();
//    				for (int i = 0; i < selectedStudentIds.size(); i += 500) {
//    					int b = i + 500;
//    					if (b > selectedStudentIds.size()) {
//    						b = selectedStudentIds.size();
//    					}
//    					accountList.addAll(commonDataService.getAccountBatch(sid,selectedStudentIds.subList(i, b)));
//    				}
        			List<Account> accountList=new ArrayList<Account>();
        			if(selectedStudentIds.size()>0){
        				accountList=allCommonDataService.getAccountBatch(sid,selectedStudentIds,selectedSemester);
        			}
        			HashMap<String,JSONObject> studentMap=new HashMap<String,JSONObject>();
        		    for(Account a:accountList)
                    {
        		    	if(a.getUsers()==null){
        		    		continue;
        		    	}
            			for(User u:a.getUsers())
                    	{
            				if(u==null||u.getUserPart()==null||u.getUserPart().getRole()==null||u.getStudentPart()==null){
            					continue;
            				}
            				StudentPart sp=u.getStudentPart();
                    		if(u.getUserPart().getRole().equals(T_Role.Student)&&classMap.containsKey(sp.getClassId()+""))
                    		{
                    			
                    			JSONObject obj=new JSONObject();
                    			obj.put("studentId", a.getId());
                    			obj.put("studentName", a.getName());
                    			obj.put("studentNum", sp.getSchoolNumber());
                    			obj.put("className", classMap.get(sp.getClassId()+""));
                    			obj.put("studentSex", null==a.getGender()?"":a.getGender().getValue()==1?"男":"女");
                    			studentMap.put(a.getId()+"", obj);
                    			break;
                    		}
                    	}
                    }
        		    
        			//所有课程：课程代码--课程详细内容
        			List<JSONObject> detailCourseList=electiveService.getDetailCourseText(map);
        			HashMap<String,String> detailCourseMap=new HashMap<String,String>();
        			for(JSONObject o:detailCourseList)
        			{
        				StringBuffer text=new StringBuffer();
        				text.append(o.getString("courseName")).append(";").append("选课学生人数 ").append(o.getIntValue("selectedNum")).append("人").append(";")
        				.append("任课教师 ").append(o.getString("teachers")).append(";").append("教学场地 ").append(o.getString("classroom"));
        				detailCourseMap.put(o.getString("courseId"), text.toString());
        			}
        		    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        			//所有课程选课记录
    	    		List<JSONObject> selectedList=electiveService.getAjustElectiveList(map);
    	    		
    	    		List<JSONObject> returnList= new ArrayList<JSONObject>();
    	    		logger.info("ElectiveStaticAction selectedList "+selectedList.toString());
    	    		for(JSONObject j:selectedList)
    	    		{
    	    			String courseId=j.getString("courseId");
    	    			JSONObject s=studentMap.get(j.getString("studentId"));
//    	    			long t = j.getLong("electiveTime");
    	    			
    	    			if(detailCourseMap.get(courseId)==null){
    	    				j.put("courseNameText", "[已删除]");	
    	    			}else{
    	    				j.put("courseNameText", detailCourseMap.get(courseId));	
    	    			}
    	    			j.put("selectedNum", selectedNumMap.get(courseId+j.getString("classId")));
    	    			if(s!=null){
    	    				logger.info("ElectiveStaticAction  s!=null "+ j.getString("studentId"));
    	    				j.putAll(s);
    	    				logger.info("ElectiveStaticAction  s!=null end"+ j.getString("studentId"));
    	    			}else{
    	    				logger.info("ElectiveStaticAction  s==null " + j.getString("studentId"));
    	    				JSONObject obj=new JSONObject();
    	    				Long cId=j.getLong("classId");
    	    				List<Long> cIds= new ArrayList<Long>();
    	    				cIds.add(cId);
    	    				List<Classroom> cList=allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), cIds, selectedSemester);
                			if(cList!=null && cList.size()>0){
                				Classroom  c= cList.get(0);
                				String name=c.getClassName();
                				if(StringUtils.isBlank(name)){
                					obj.put("className", "[已删除]");
                				}else{
                					obj.put("className", name);
                				}
                			}
    	    				//obj.put("studentId","");
                			obj.put("studentName", "[已删除]");
                			obj.put("studentNum", "[已删除]");
                			
                			obj.put("studentSex", "[已删除]");
                			j.putAll(obj);
                			logger.info("ElectiveStaticAction  s==null end"+ j.getString("studentId"));
    	    			}
    	    			//j.put("studentNum", j.get("electiveTime"));
    	    			courseIdList.remove(courseId);
    	    		}
    	    		logger.info("end of selectedList " +selectedList.toString());
    	    		if(courseIdList.size()>0)
    	    		{
    	    			for(String s:courseIdList)
    	    			{
    	    				JSONObject j=new JSONObject();
    	    				j.put("courseNameText", detailCourseMap.get(s));
    	    				j.put("className", "");
    	    				j.put("selectedNum", 0);
    	    				j.put("studentNum", "");
    	    				j.put("studentName", "");
    	    				j.put("studentSex", "");
    	    				j.put("courseId", s);
    	    				selectedList.add(j);
    	    			}
    	    		}
//    	    		selectedList=(List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, selectedList, "courseNameText,className");
//                   暂不排序
    	    		for(JSONObject j:selectedList){
    	    			String courseId=j.getString("courseId");
    	    			if(courseIds.contains(courseId)){
    	    				returnList.add(j);
    	    			}
    	    		}
    	    		logger.info("beign of arr " +selectedList.toString());
    	    		String s = "";
    	    		for(JSONObject obj:returnList){
    	    			s +=obj.getString("studentId")+",";
    	    		}
    	    		arr = (JSONArray) JSON.toJSON(returnList);
    	    		logger.info("sstudentId:"+s);
    			}
    			
    		} catch (Exception e) {
    			logger.error("/elective/static/staticByCourse:",e);
    			e.printStackTrace();
    			code = -1;
    			msg = "获取失败！";
    		}
        	
    		json.put("code", code);
    		json.put("msg", msg);
    		json.put("data", arr);
    		req.getSession().setAttribute("exportStaticByCourse", arr);
    		return json;
        }
    
	/**
	 * 按班级查看
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "staticByClass")
    @ResponseBody
    public JSONObject staticByClass(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	    List<JSONObject> list=new ArrayList<JSONObject>();
            JSONObject json=new JSONObject();
    		String msg = "";
    		int code = 0;
        	try {
        		
        		String selectedSemester=requestParams.getString("selectedSemester");
        		if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
    				selectedSemester=getCurXnxq(req);
    			}
        		String schoolId=getXxdm(req);
    			long sid=Long.valueOf(schoolId);
    			String electiveId=requestParams.getString("electiveId");
//    			String useGrade=requestParams.getString("useGrade");
    			String classId=requestParams.getString("classIds");
    			HashMap<String,Object> map=new HashMap<String,Object>();
    			map.put("schoolId", schoolId);
    			map.put("electiveId", electiveId);
    			map.put("classIdList", Arrays.asList(classId.split(",")));
    			
    			//已选课程：课程代码--上课时间
    			List<JSONObject> timeList=electiveService.getStudentCourseSchoolTime(map);
    			HashMap<String,String> timeMap=new HashMap<String,String>();
    			HashMap<String,Object> dayOfWeekMap=new HashMap<String,Object>();
    			for(JSONObject j:timeList)
    			{
    				String courseId=j.getString("courseId");
    				int dayOfWeek=j.getIntValue("dayOfWeek");
    				int lessonOfDay=j.getIntValue("lessonOfDay")+1;				
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
    				}
    				else
    				{
    					String text=StringNumTool.getDayOfWeek(dayOfWeek)+lessonOfDay;
    					dayOfWeekMap.put(courseId+dayOfWeek, "in");
    					timeMap.put(courseId, text);
    				}
    			}
    			
    			//已选课程：课程代码--课程详细内容
    			List<JSONObject> detailCourseList=electiveService.getStudentCourseText(map);
    			HashMap<String,String> detailCourseMap=new HashMap<String,String>();
    			for(JSONObject o:detailCourseList)
    			{
    				StringBuffer text=new StringBuffer();
    				String t="";
    				String cN = "[已删除]";
    				String cT = "[已删除]";
    				if(o.getString("courseName")!=null){
    					cN=o.getString("courseName");
    				}
    				if(o.getString("teachers")!=null){
    					cT=o.getString("teachers");
    				}
    				text.append(cN).append("(");
    				if(StringUtils.isNotEmpty(o.getString("teachers")))
    				{
    					text.append(cT).append(";");
    				}
    				
    				text.append(timeMap.get(o.getString("courseId"))).append(";");
    				if(StringUtils.isNotEmpty(o.getString("classroom")))
    				{
    					text.append(o.getString("classroom"));
    				}
    				
    				t=text.substring(0, text.lastIndexOf(";"))+")";
    				detailCourseMap.put(o.getString("courseId"), t);
    			}
    			
    			List<JSONObject> studentCourseIds=electiveService.getStudentCourseIds(map);
    			HashMap<String,String> studentCourseMap=new HashMap<String,String>();
    			Set<String> sIds= new HashSet<String>();
    			HashMap<String,String> scIdMap=new HashMap<String,String>();
    			for(JSONObject j:studentCourseIds)
    			{
    				sIds.add(j.getString("studentId"));
    				scIdMap.put(j.getString("studentId"), j.getString("classId"));
    				studentCourseMap.put(j.getString("studentId"), j.getString("courseIds"));
    			}
    			List<Long> ids=new ArrayList<Long>();
    			Set<Long> removeIds = new HashSet<Long	>();
    			for(String s:classId.split(","))
    			{
    				ids.add(Long.valueOf(s));
    			}
    			Collections.sort(ids);
    			HashMap<Long,String> classMap=new HashMap<Long,String>();
    			List<Classroom> classList=allCommonDataService.getClassroomBatch(sid,ids,selectedSemester);
    			ids.clear();
    			for(Classroom o:classList)
    			{
    				classMap.put(o.getId(), o.getClassName()); 
    				List<Long> sids=o.getStudentAccountIds();
    				if(null!=sids)
    				{
        				removeIds.addAll(sids);
    				}
    			} 
    			ids.addAll(removeIds);
//    			List<Account> studentList=new ArrayList<Account>();
//				for (int i = 0; i < ids.size(); i += 500) {
//					int b = i + 500;
//					if (b > ids.size()) {
//						b = ids.size();
//					}
//					studentList.addAll(commonDataService.getUserBatch(sid,ids.subList(i, b)));
//				}
    			List<Account> studentList=new ArrayList<Account>();
    			if(ids.size()>0){
    				studentList=allCommonDataService.getAccountBatch(sid, ids,selectedSemester);//根据查询的班级得到所有的学生
    			} 
    			Map<String,Account> aMap = new HashMap<String,Account>();
    			for(Account a: studentList){
    				aMap.put(a.getId()+"", a);
    			}
    			for(String s:sIds)
                 {
    				
    				 Account a=aMap.get(s);
    				 if(a!=null){
	    				 if(a.getUsers()==null){
	     		    		continue;
	     		    	}
	         			for(User u:a.getUsers())
	                 	{
	         				if(u==null||u.getUserPart()==null||u.getUserPart().getRole()==null||u.getStudentPart()==null){
	         					continue;
	         				}
	         				StudentPart sp = u.getStudentPart();	
	                 		if(u.getUserPart().getRole().equals(T_Role.Student)&&classMap.containsKey(sp.getClassId()))
	                 		{
														
								JSONObject obj = new JSONObject();
								obj.put("studentId", a.getId());
								obj.put("studentName", a.getName());
								obj.put("studentNum", sp.getSchoolNumber());
								obj.put("className", classMap.get(sp.getClassId()));
								String text = "";
								if (studentCourseMap.containsKey(a.getId() + "")) {
									String cid = studentCourseMap.get(a.getId() + "");
									for (String c : cid.split(",")) {
										text += detailCourseMap.get(c) + ";";
									}
									obj.put("courseNameText", text.substring(0, text.length()-1));
								}
								else
								{
									obj.put("courseNameText", "");
								}
								
								list.add(obj);
								break;
	                 		}
	                 	}
                    }else{
                    	JSONObject obj = new JSONObject();
						obj.put("studentId", "[已删除]");
						obj.put("studentName", "[已删除]");
						obj.put("studentNum","[已删除]");
						String cId=scIdMap.get(s);
						List<Long> cIds= new ArrayList<Long>();
						cIds.add(Long.parseLong(cId));
						List<Classroom> cList=allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), cIds, selectedSemester);
						if(cList!=null&& cList.size()>0){
							Classroom c= cList.get(0);
							String name=c.getClassName();
							if(StringUtils.isBlank(name)){
								obj.put("className", "[已删除]");
							}else{
								obj.put("className", name);	
							}
							
						}else{
							obj.put("className", "");
						}
						String text = "";
						if (studentCourseMap.containsKey(s)) {
							String cid = studentCourseMap.get(s);
							for (String c : cid.split(",")) {
								text += detailCourseMap.get(c) + ";";
							}
							obj.put("courseNameText", text.substring(0, text.length()-1));
						}
						else
						{
							obj.put("courseNameText", "");
						}
						
						list.add(obj);
                    }
				}
    			//出所有未选择的学生
    			for(Account a:studentList){
    				if(!sIds.contains(a.getId()+"")){
    					 if(a.getUsers()==null){
 	     		    		continue;
 	     		    	}
 	         			for(User u:a.getUsers())
 	                 	{
 	         				if(u==null||u.getUserPart()==null||u.getUserPart().getRole()==null||u.getStudentPart()==null){
 	         					continue;
 	         				}
 	         				StudentPart sp = u.getStudentPart();	
 	                 		if(u.getUserPart().getRole().equals(T_Role.Student)&&classMap.containsKey(sp.getClassId()))
 	                 		{
 														
 								JSONObject obj = new JSONObject();
 								obj.put("studentId", a.getId());
 								obj.put("studentName", a.getName());
 								obj.put("studentNum", sp.getSchoolNumber());
 								obj.put("className", classMap.get(sp.getClassId()));
 								String text = "";
 								if (studentCourseMap.containsKey(a.getId() + "")) {
 									String cid = studentCourseMap.get(a.getId() + "");
 									for (String c : cid.split(",")) {
 										text += detailCourseMap.get(c) + ";";
 									}
 									obj.put("courseNameText", text.substring(0, text.length()-1));
 								}
 								else
 								{
 									obj.put("courseNameText", "");
 								}
 								
 								list.add(obj);
 								break;
 	                 		}
 	                 	}
    				}
    			}
    			 list=(List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, list, "className");

    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			logger.error("/elective/static/staticByClass:",e);
    			e.printStackTrace();
    			code = -1;
    			msg = "获取失败！";
    		}
        	
    		json.put("code", code);
    		json.put("msg", msg);
    		json.put("data", list);
    		return json;
        }
}
