package com.talkweb.elective.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.StringNumTool;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.elective.service.ElectiveService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.utils.KafkaUtils;

@Controller
@RequestMapping(value = "/elective/")
public class ElectiveAction extends BaseAction{
	@Value("#{settings['clientId']}")
	private String clientId;

	@Value("#{settings['clientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
	
	@Value("#{settings['elective.msgUrlApp']}")
	private String msgUrlApp;
	
	@Value("#{settings['elective.msgUrlPc']}")
	private String msgUrlPc;
	
	@Autowired
	private ElectiveService electiveService;
	
	@Autowired
	private FileServer fileServerImplFastDFS;
	
    @Autowired
    private AllCommonDataService allCommonDataService;
    
	private static final Logger logger = LoggerFactory.getLogger(ElectiveAction.class);
	 /**
		 * redis
		 */
		@Resource(name="redisOperationDAOSDRTempDataImpl")
	 	private RedisOperationDAO redisOperationDAO;
/**管理员选课首页 ***/
	/**
	 * 获取管理员选课列表
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getElectiveList")
    @ResponseBody
    public JSONObject getElectiveList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
			String termInfo=requestParams.getString("termInfo");
			String schoolId=getXxdm(req);
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("termInfo", termInfo);
			List<JSONObject> list=electiveService.getAdminElectiveList(map);
			for(JSONObject obj:list)
			{
				String isSet = obj.getString("isSet");
				Date endTime  = obj.getDate("endTime");
				Date nowDate = new Date();
				obj.put("isShowSendBtn", 0); //不出显示按钮
				logger.info("isSet:"+isSet+"endTime:"+endTime+" nowDate:"+nowDate);
				if(isSet!=null && isSet.contains("01") &&  endTime!=null && nowDate.before(endTime)){ //设置了并且未到结束时间
					obj.put("isShowSendBtn", 1);//出显示按钮
				}
    	    }
 
			arr = (JSONArray) JSON.toJSON(list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("/elective/getElectiveList:",e);
			e.printStackTrace();
			code = -1;
			msg = "获取失败！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", arr);
		return json;
    }
    
	/**
	 * 创建选课
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "createElective")
    @ResponseBody
    public JSONObject createElective(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String msg = "保存成功";
		int code = 0;
		try {
			String termInfo=requestParams.getString("termInfo");
//			String selectedSemester=requestParams.getString("selectedSemester");
			String electiveName=requestParams.getString("electiveName");
			String schoolId=getXxdm(req);
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("schoolYear", termInfo.substring(0, 4));
			map.put("term", termInfo.substring(4, 5));
			map.put("electiveId", UUIDUtil.getUUID());
			map.put("electiveName", electiveName);
			map.put("createTime", DateUtil.getDateFormatNow());
			
			int num=electiveService.createElective(map);
			if(num<=0)
			{
				code = -1;
				msg = "保存失败！";	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("/elective/createElective:",e);
			e.printStackTrace();
			code = -1;
			msg = "保存失败！";
		}
		json.put("code", code);
		json.put("msg", msg);
		return json;
    }
	/**
	 * 修改选课名称
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "updateElective")
    @ResponseBody
    public JSONObject updateElective(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String electiveId=requestParams.getString("electiveId");
		String electiveName=requestParams.getString("electiveName");
//		String selectedSemester=requestParams.getString("selectedSemester");
		String schoolId=getXxdm(req);
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("electiveName", electiveName);
		map.put("electiveId", electiveId);
		
		int num=electiveService.updateElective(map);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","保存成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","保存失败");	
		}
		return json;
    }
	/**
	 * 编辑选课时间
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "updateElectiveTime")
    @ResponseBody
    public JSONObject updateElectiveTime(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String electiveId=requestParams.getString("electiveId");
		String startTime=requestParams.getString("startTime");
//		String selectedSemester=requestParams.getString("selectedSemester");
		String endTime=requestParams.getString("endTime");
		Date startDate = null;
		if (StringUtils.isNotBlank(startTime)) {
			startDate = DateUtil.parseDateSecondFormat(startTime);
		}
		Date endDate = null;
		if (StringUtils.isNotBlank(endTime)) {
			endDate = DateUtil.parseDateSecondFormat(endTime);
		}
		String schoolId=getXxdm(req);
		String termInfo = getCurXnxq(req);
		HashMap<String, Object> objMap =new HashMap<String, Object>();
		objMap.put("schoolId",schoolId);
		objMap.put("termInfo",termInfo);
		List<JSONObject> eList = electiveService.getAdminElectiveList(objMap);
		for(JSONObject e:eList){
			if( electiveId.equals(e.getString("electiveId"))){
				continue;
			}
			String useStartApply = e.getString("startTime");
			String useEndApply = e.getString("endTime");
			Date useStartDateApply = DateUtil.parseDateSecondFormat(useStartApply);
			Date useEndDateApply = DateUtil.parseDateSecondFormat(useEndApply);
			if (useStartDateApply != null && useEndDateApply != null) {
				boolean overlap = ((useStartDateApply.getTime() >= startDate.getTime())
						&& useStartDateApply.getTime() < endDate.getTime())
						|| ((useStartDateApply.getTime() > startDate.getTime())
								&& useStartDateApply.getTime() <= endDate.getTime())
						|| ((startDate.getTime() >= useStartDateApply.getTime())
								&& startDate.getTime() < useEndDateApply.getTime())
						|| ((startDate.getTime() > useStartDateApply.getTime())
								&& startDate.getTime() <= useEndDateApply.getTime());
				if (overlap) {
					json.put("code",-1);
					json.put("msg","选课开放时间冲突,请重新输入！");
					return json;
				}
			}
		}
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
		if(startDate.getTime()>endDate.getTime()){
			json.put("code",-1);
			json.put("msg","结束时间不能小于开始时间,请重新输入！");
			return json;
		}
		map.put("electiveId", electiveId);
		int num=electiveService.updateElectiveTime(map);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","保存成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","保存失败");	
		}
		return json;
    }
    
	/**
	 * 删除选课
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "deleteElective")
    @ResponseBody
    public JSONObject deleteElective(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject  json = new JSONObject();
		try {
		
			String electiveId=requestParams.getString("electiveId");
			
			String flag=requestParams.getString("flag");
			String schoolId=getXxdm(req);
//		String selectedSemester=requestParams.getString("selectedSemester");
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("electiveId", electiveId);
			JSONObject obj = electiveService.getElectiveXnxqById(map);
			String schoolYear=obj.getString("schoolYear");
			String term = obj.getString("term");
			map.put("termInfo", schoolYear+term);
			if(!"1".equals(flag)){
				int hasNum = electiveService.getTotalSubmittedNum(map);
				/*if(hasNum==0){
					electiveService.deleteElective(map);
				     json.put("msg","删除成功");
				}*/
					 json.put("code",0);
					 json.put("hasNum",hasNum);
					 return json;
			}
			electiveService.deleteElective(map);
			json.put("code",0);
			json.put("msg","删除成功");
			//删除对应的附件
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("electiveId", electiveId);
			param.put("termInfo", schoolYear+term);
			param.put("isNotNull", "1");
			List<JSONObject> aList = electiveService.getAttachmentById(param);
			if(aList!=null && aList.size()>0){
				electiveService.deleteAttachmentByElectiveId(param);
				for(JSONObject attObj:aList){
					 String url = attObj.getString("attachmentAddr");
					 fileServerImplFastDFS.deleteFile(url);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			json.put("code",-1);
			json.put("msg","删除失败");
			e.printStackTrace();
		}
        
		return json;
    }
/**选修课程设置 ***/    
	/**
	 * 获取选修课程
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getElectiveCourse")
    @ResponseBody
    public JSONObject getElectiveCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
			String termInfo=requestParams.getString("termInfo");
			String schoolId=getXxdm(req);
//			String selectedSemester=requestParams.getString("selectedSemester");
			String electiveId=requestParams.getString("electiveId");
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("termInfo", termInfo);
			map.put("electiveId", electiveId);
			List<JSONObject> courseList=electiveService.getElectiveCourse(map);
			List<JSONObject> timeList=electiveService.getElectiveCourseSchoolTime(map);
			
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
		         j.put("schoolTime", StringNumTool.getSchoolTimeText(timeMap.get(j.getString("courseId")), courseWeekType.get(j.getString("courseId"))));
		         j.put("adaptSex", StringNumTool.getSex(j.getIntValue("adaptSex")));
		         j.put("offerGrade",StringNumTool.getOfferGradeText(JSONArray.parseArray(j.getString("offerGrade"))));
			}
			arr = (JSONArray) JSON.toJSON(courseList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("/elective/getElectiveCourse:",e);
			code = -1;
			msg = "获取失败！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", arr);
		return json;
    }
    /**导出课程列表 ***/    
	/**
	 * 导出课程列表
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "exportElectiveCourse")
    @ResponseBody
    public JSONObject exportElectiveCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
    		 String fileName  = requestParams.getString("fileName");
    		 String width  = requestParams.getString("width");
    		 String align  = requestParams.getString("align");
    		 String boxWidth  = requestParams.getString("boxWidth");
    		 String deltaWidth  = requestParams.getString("deltaWidth");
			String termInfo=requestParams.getString("termInfo");
			String schoolId=requestParams.getString("schoolId");//getXxdm(req);
			String electiveId=requestParams.getString("electiveId");
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("termInfo", termInfo);
			map.put("electiveId", electiveId);
			List<JSONObject> courseList=electiveService.getElectiveCourse(map);
			List<JSONObject> timeList=electiveService.getElectiveCourseSchoolTime(map);
			
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
		      	JSONArray excelData =new JSONArray();
		        JSONArray newExcelHeads=new JSONArray();
		        JSONArray newHead=new JSONArray();
		        
		        JSONObject head0=new JSONObject();
		        head0.put("field", "courseName");
		        head0.put("title", "课程名称");
		        head0.put("width", width);
		        head0.put("align", align);
		        head0.put("boxWidth", boxWidth);
		        head0.put("deltaWidth", deltaWidth);
		        JSONObject head1=new JSONObject();
		        head1.put("title", "人数上限");
		        head1.put("field",  "upperLimit");
		        head1.put("width", width);
		        head1.put("align", align);
		        head1.put("boxWidth", boxWidth);
		        head1.put("deltaWidth", deltaWidth);
		        JSONObject head2=new JSONObject();
		        head2.put("title", "开课时间");
		        head2.put("field", "schoolTime");
		        head2.put("width", width);
		        head2.put("align", align);
		        head2.put("boxWidth", boxWidth);
		        head2.put("deltaWidth", deltaWidth);
		        JSONObject head3=new JSONObject();
		        head3.put("title", "选课年级");
		        head3.put("field", "offerGrade");
		        head3.put("width", width);
		        head3.put("align", align);
		        head3.put("boxWidth", boxWidth);
		        head3.put("deltaWidth", deltaWidth);
		        JSONObject head4=new JSONObject();
		        head4.put("title", "班级最多可选人数");
		        head4.put("field", "classMaxNum");
		        head4.put("width", width);
		        head4.put("align", align);
		        head4.put("boxWidth", boxWidth);
		        head4.put("deltaWidth", deltaWidth);
		        JSONObject head5=new JSONObject();
		        head5.put("title", "适应性别");
		        head5.put("field", "adaptSex");
		        head5.put("width", width);
		        head5.put("align", align);
		        head5.put("boxWidth", boxWidth);
		        head5.put("deltaWidth", deltaWidth);
		        JSONObject head6=new JSONObject();
		        head6.put("title", "授课教师");
		        head6.put("field", "teachers");
		        head6.put("width", width);
		        head6.put("align", align);
		        head6.put("boxWidth", boxWidth);
		        head6.put("deltaWidth", deltaWidth);
		        JSONObject head7=new JSONObject();
		        head7.put("title", "教学场地");
		        head7.put("field", "classroom");
		        head7.put("width", width);
		        head7.put("align", align);
		        head7.put("boxWidth", boxWidth);
		        head7.put("deltaWidth", deltaWidth);
		        newHead.add(head0);
		        newHead.add(head1);
		        newHead.add(head2);
		        newHead.add(head3);
		        newHead.add(head4);
		        newHead.add(head5);
		        newHead.add(head6);
		        newHead.add(head7);
		        
		        newExcelHeads.add(newHead);
	        
		        for(JSONObject j:courseList)
				{
			         j.put("schoolTime", StringNumTool.getSchoolTimeText(timeMap.get(j.getString("courseId")), courseWeekType.get(j.getString("courseId"))));
			         j.put("adaptSex", StringNumTool.getSex(j.getIntValue("adaptSex")));
			         j.put("offerGrade",StringNumTool.getOfferGradeText(JSONArray.parseArray(j.getString("offerGrade"))));
				}
				if(courseList.size()>0){
					ExcelTool.exportExcelWithData( (JSONArray) JSON.toJSON(courseList) , newExcelHeads,fileName, null, req, res);
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("/elective/exportElectiveCourse:",e);
			code = -1;
			msg = "获取失败！";
		}
		json.put("code", code);
		json.put("msg", msg);
		return json;
    }
	/**
	 * 获取单个选修课程
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getSingeElectiveCourse")
    @ResponseBody
    public JSONObject getSingeElectiveCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
		JSONObject data=new JSONObject();
    	try {
			String termInfo=requestParams.getString("termInfo");
//			String selectedSemester=requestParams.getString("selectedSemester");
			String schoolId=getXxdm(req);
			String electiveId=requestParams.getString("electiveId");
			String courseId=requestParams.getString("courseId");
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("termInfo", termInfo);
			map.put("electiveId", electiveId);
			map.put("courseId", courseId);
			JSONObject json1 = new JSONObject();
			json1.put("termInfo", termInfo);
			json1.put("schoolId", schoolId);
			json1.put("electiveId", electiveId);
			json1.put("courseId", courseId);
			JSONObject att = new JSONObject();
			List<JSONObject> attList = electiveService.getAttachmentById(json1);
			if(attList!=null && attList.size()>0){
				att=attList.get(0);
				if(StringUtils.isNotBlank(att.getString("attachmentId"))){
					data.put("attachmentId", att.getString("attachmentId"));
				}else{
					data.put("attachmentId", "");
				}
			}
			List<JSONObject> courseList=electiveService.getElectiveCourse(map);			

			if(null!=courseList&&courseList.size()>0)
			{
				List<JSONObject> timeList=electiveService.getElectiveCourseSchoolTime(map);	
				String teacherIds=electiveService.getElectiveCourseTeacher(map);
				String classIds=electiveService.getElectiveCourseClass(map);
				JSONObject j=courseList.get(0);
				j.put("offerGradeText", JSONArray.parseArray(j.getString("offerGrade")));
				j.put("offerGrade", classIds);
				j.put("teachers", teacherIds);
				j.put("schoolTime", timeList);
				if("不限".equals(j.get("classMaxNum")+""))
				{
					j.put("classMaxNum", null);
				}
				data=j;
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("/elective/getSingeElectiveCourse:",e);
			code = -1;
			msg = "获取失败！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", data);
		return json;
    }
    
	/**
	 * 新增选修课程
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 * @update 添加人数上限的同步控制20160818  By： zhh
	 */
    @RequestMapping(value = "createElectiveCourse")
    @ResponseBody
    public JSONObject createElectiveCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
        String electiveId="";
    	try {
    		String courseId=UUIDUtil.getUUID();
    		String attachmentId=requestParams.getString("attachmentId");
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String termInfo=requestParams.getString("termInfo");
			String schoolId=getXxdm(req);
			electiveId=requestParams.getString("electiveId");
			String courseName=requestParams.getString("courseName");
			String courseDesc=requestParams.getString("courseDesc");
			String maxNum=requestParams.getString("upperLimit");
			String teachers=requestParams.getString("teachers");
			String teachersName=requestParams.getString("teachersName");
			String classMaxNum=requestParams.getString("classMaxNum");
			String adaptSex=requestParams.getString("adaptSex");
			String classroom=requestParams.getString("classroom");
			String offerGrade=requestParams.getString("offerGrade");
			String offerGradeText=requestParams.getString("offerGradeText");
			String schoolTime=requestParams.getString("schoolTime");
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("schoolYear", termInfo.substring(0, 4));
			map.put("term", termInfo.substring(4, 5));
			map.put("electiveId", electiveId);
			map.put("courseId", courseId);
			map.put("courseName", courseName);
			map.put("courseDesc", courseDesc);
			map.put("maxNum", maxNum);
			map.put("classMaxNum", StringUtils.isEmpty(classMaxNum)==true?null:classMaxNum);
			map.put("adaptSex", adaptSex);
			List<JSONObject> eList = electiveService.getCourseByName(map);
			if(eList!=null && eList.size()>0){ //全部查出来是因为mysql不能区分大小写
				boolean isRepeat=false;
				for(JSONObject e:eList){
					String cName = e.getString("courseName");
					if(courseName.equals(cName)){
						isRepeat=true;
						break;
					}
				}
				if(isRepeat){
					json.put("code", -1);
	 		 		json.put("msg", "课程名称不能重复！");
	 		 		return json;
				}
			}
			if(StringUtils.isNotEmpty(teachersName)&&teachersName.endsWith(","))
			{
				teachersName=teachersName.substring(0,teachersName.length()-1);
			}
			map.put("teachers", teachersName);
			map.put("classroom", classroom);
			map.put("offerGrade", offerGradeText);
			map.put("createTime", DateUtil.getDateFormatNow());
			
			List<JSONObject> classList=new ArrayList<JSONObject>();
			if(StringUtils.isNotEmpty(offerGrade))
			{
				JSONArray offerGrades = JSON.parseArray(offerGrade); 
				for(int i=0;i<offerGrades.size();i++)
				{
					JSONObject j=offerGrades.getJSONObject(i);
					String useGrade=j.getString("useGrade");
					String classIds=j.getString("classIds");
					if(StringUtils.isNotEmpty(classIds))
					{
						String[] classes=classIds.split(",");
						for(String id:classes)
						{
							JSONObject obj=new JSONObject();
							obj.put("schoolYear", termInfo.substring(0, 4));
							obj.put("term", termInfo.substring(4, 5));
							obj.put("electiveId", electiveId);
							obj.put("schoolId", schoolId);
							obj.put("courseId", courseId);
							obj.put("classId", id);
							obj.put("useGrade", useGrade);
							classList.add(obj);
						}
					}
				}
			}
			
			List<JSONObject> teacherList=new ArrayList<JSONObject>();
			if(StringUtils.isNotEmpty(teachers))
			{
				String[] teacherIds=teachers.split(",");
				for(String id:teacherIds)
				{
					JSONObject obj=new JSONObject();
					obj.put("schoolYear", termInfo.substring(0, 4));
					obj.put("term", termInfo.substring(4, 5));
					obj.put("electiveId", electiveId);
					obj.put("schoolId", schoolId);
					obj.put("courseId", courseId);
					obj.put("teacherId", id);
					teacherList.add(obj);
				}
			}

			List<JSONObject> schoolTimeList=new ArrayList<JSONObject>();
			if(StringUtils.isNotEmpty(schoolTime))
			{
				JSONArray schoolTimes = JSON.parseArray(schoolTime); 
				for(int i=0;i<schoolTimes.size();i++)
				{
					JSONObject obj=schoolTimes.getJSONObject(i);
					obj.put("schoolYear", termInfo.substring(0, 4));
					obj.put("term", termInfo.substring(4, 5));
					obj.put("electiveId", electiveId);
					obj.put("schoolId", schoolId);
					obj.put("courseId", courseId);
					schoolTimeList.add(obj);
				}
			}
			json=electiveService.createElectiveCourse(map, classList, teacherList, schoolTimeList);
			//将附件和课程绑定到一起
			JSONObject json1 = new JSONObject();
			json1.put("schoolId", schoolId);
			json1.put("electiveId", electiveId);
			json1.put("attachmentId", attachmentId);
			json1.put("courseId", courseId);
			electiveService.updateAttachment(json1);
			
    	} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			logger.error("/elective/createElectiveCourse:",e);
 			
 			if(json.get("msg")==null || StringUtils.isEmpty(json.getString("msg"))){
 				json.put("code", -1);
 		 		json.put("msg", "保存失败");
 			}
 		}
    	logger.info("【选课】最后返回createElectiveCourse：elective:{} json：{}",electiveId,json);
 		return json;
    }
    
	/**
	 * 修改选修课程
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 * @update 添加人数上限的同步控制20160818  By： zhh
	 */
    @RequestMapping(value = "updateElectiveCourse")
    @ResponseBody
    public JSONObject updateElectiveCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
        String electiveId="";
    	try {
    		String attachmentId=requestParams.getString("attachmentId");
    		String courseId=requestParams.getString("courseId");
    		String termInfo=requestParams.getString("termInfo");
//    		String selectedSemester=requestParams.getString("selectedSemester");
			String schoolId=getXxdm(req);
			electiveId=requestParams.getString("electiveId");
			String courseName=requestParams.getString("courseName");
			String courseDesc=requestParams.getString("courseDesc");
			String maxNum=requestParams.getString("upperLimit");
			String teachers=requestParams.getString("teachers");
			String teachersName=requestParams.getString("teachersName");
			String classMaxNum=requestParams.getString("classMaxNum");
			String adaptSex=requestParams.getString("adaptSex");
			String classroom=requestParams.getString("classroom");
			String offerGrade=requestParams.getString("offerGrade");
			String offerGradeText=requestParams.getString("offerGradeText");
			String schoolTime=requestParams.getString("schoolTime");
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("schoolYear", termInfo.substring(0, 4));
			map.put("term", termInfo.substring(4, 5));
			map.put("electiveId", electiveId);
			map.put("courseId", courseId);
			map.put("courseName", courseName);
			map.put("courseDesc", courseDesc);
			map.put("maxNum", maxNum);
			map.put("classMaxNum", StringUtils.isEmpty(classMaxNum)==true?null:classMaxNum);
			map.put("adaptSex", adaptSex);
			List<JSONObject> eList = electiveService.getCourseByName(map);
			if(eList!=null && eList.size()>0){ //全部查出来是因为mysql不能区分大小写
				boolean isRepeat=false;
				for(JSONObject e:eList){
					String cName = e.getString("courseName");
					if(courseName.equals(cName)){
						isRepeat=true;
						break;
					}
				}
				if(isRepeat){
					json.put("code", -1);
	 		 		json.put("msg", "课程名称不能重复！");
	 		 		return json;
				}
			}
			if(StringUtils.isNotEmpty(teachersName)&&teachersName.endsWith(","))
			{
				teachersName=teachersName.substring(0,teachersName.length()-1);
			}
			map.put("teachers", teachersName);
			map.put("classroom", classroom);
			map.put("offerGrade", offerGradeText);
//			map.put("createTime", DateUtil.getDateFormatNow());
			
			List<JSONObject> classList=new ArrayList<JSONObject>();
			if(StringUtils.isNotEmpty(offerGrade))
			{
				JSONArray offerGrades = JSON.parseArray(offerGrade); 
				for(int i=0;i<offerGrades.size();i++)
				{
					JSONObject j=offerGrades.getJSONObject(i);
					String useGrade=j.getString("useGrade");
					String classIds=j.getString("classIds");
					if(StringUtils.isNotEmpty(classIds))
					{
						String[] classes=classIds.split(",");
						for(String id:classes)
						{
							JSONObject obj=new JSONObject();
							obj.put("schoolYear", termInfo.substring(0, 4));
							obj.put("term", termInfo.substring(4, 5));
							obj.put("electiveId", electiveId);
							obj.put("schoolId", schoolId);
							obj.put("courseId", courseId);
							obj.put("classId", id);
							obj.put("useGrade", useGrade);
							classList.add(obj);
						}
					}
				}
			}
			
			List<JSONObject> teacherList=new ArrayList<JSONObject>();
			if(StringUtils.isNotEmpty(teachers))
			{
				String[] teacherIds=teachers.split(",");
				for(String id:teacherIds)
				{
					JSONObject obj=new JSONObject();
					obj.put("schoolYear", termInfo.substring(0, 4));
					obj.put("term", termInfo.substring(4, 5));
					obj.put("electiveId", electiveId);
					obj.put("schoolId", schoolId);
					obj.put("courseId", courseId);
					obj.put("teacherId", id);
					teacherList.add(obj);
				}
			}

			List<JSONObject> schoolTimeList=new ArrayList<JSONObject>();
			if(StringUtils.isNotEmpty(schoolTime))
			{
				JSONArray schoolTimes = JSON.parseArray(schoolTime); 
				for(int i=0;i<schoolTimes.size();i++)
				{
					JSONObject obj=schoolTimes.getJSONObject(i);
					obj.put("schoolYear", termInfo.substring(0, 4));
					obj.put("term", termInfo.substring(4, 5));
					obj.put("electiveId", electiveId);
					obj.put("schoolId", schoolId);
					obj.put("courseId", courseId);
					schoolTimeList.add(obj);
				}
			}
		    json=electiveService.updateElectiveCourse(map, classList, teacherList, schoolTimeList);
		  //将附件和课程绑定到一起
		    if(StringUtils.isNotBlank(attachmentId)){ //管理员没删除附件 或者只是更改附件
				JSONObject json1 = new JSONObject();
				json1.put("schoolId", schoolId);
				json1.put("electiveId", electiveId);
				json1.put("attachmentId", attachmentId);
				json1.put("courseId", courseId);
				electiveService.updateAttachment(json1);
			}
    	} catch (Exception e) {
 			// TODO Auto-generated catch block
    		logger.error("/elective/updateElectiveCourse:",e);
 			e.printStackTrace();
 			if(json.get("msg")==null || StringUtils.isEmpty(json.getString("msg"))){
 				json.put("code", -1);
 		 		json.put("msg", "保存失败");
 			}
 		}
      	logger.info("【选课】最后返回updateElectiveCourse：electiveId:{} json：{}",electiveId,json);
 		return json;
    }
	/**
	 * 解冻某个选修课程
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "unFreezeElectiveCourse")
    @ResponseBody
    public JSONObject unFreezeElectiveCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		try {
			String electiveId=requestParams.getString("electiveId");
			String courseId=requestParams.getString("courseId");
//		String selectedSemester=requestParams.getString("selectedSemester");
			String schoolId=getXxdm(req);
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("electiveId", electiveId);
			JSONObject eObj = electiveService.getElectiveXnxqById(map);
			if(eObj!=null){
				String schoolYear=eObj.getString("schoolYear");
				String term = eObj.getString("term");
				map.put("termInfo", schoolYear+term);
				map.put("courseId", courseId);
				map.put("isFreezed", 0);
				electiveService.freezeElectiveCourse(map);
			}
			json.put("code", 0);
			json.put("msg", "冻结成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			json.put("code", -1);
			json.put("msg", "冻结失败");
			e.printStackTrace();
		}
		
		return json;
    }
    /**
	 * 导出某课程的已选课学生名单
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "exportElectiveCourseList")
    @ResponseBody
    public JSONObject exportElectiveCourseList(HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		try {
			String electiveId=req.getParameter("electiveId");
			String courseId=req.getParameter("courseId");
//		String selectedSemester=requestParams.getString("selectedSemester");
			String schoolId=getXxdm(req);
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("electiveId", electiveId);
			JSONObject eObj = electiveService.getElectiveXnxqById(map);
			String schoolYear=eObj.getString("schoolYear");
			String term = eObj.getString("term");
			map.put("termInfo", schoolYear+term);
			map.put("courseId", courseId);
			List<String> courseIdList = new ArrayList<String>();
			courseIdList.add(courseId);
			map.put("courseIdList", courseIdList);
			List<JSONObject> list = electiveService.getCourseNameById(map);
			JSONObject courseObj = list.get(0);
			String courseName = courseObj.getString("courseName");
			//某个courseId的已选学生名单
			String fileName = courseName+"课程选课结果";
			JSONArray excelHeads = new JSONArray();
			JSONArray line = new JSONArray();
			JSONObject col = new JSONObject();
			col.put("field", "courseName");
			col.put("title", "课程名称");
			line.add(col);
			
			col = new JSONObject();
			col.put("field", "hasSelectedTotalNum");
			col.put("title", "选课学生人数");
			line.add(col);
			
			col = new JSONObject();
			col.put("field", "teachers");
			col.put("title", "任课教师");
			line.add(col);
			
			col = new JSONObject();
			col.put("field", "classroom");
			col.put("title", "教学场地");
			line.add(col);
			
			col = new JSONObject();
			col.put("field", "className");
			col.put("title", "班级");
			line.add(col);
			
			col = new JSONObject();
			col.put("field", "hasSelectedNum");
			col.put("title", "班级选课人数");
			line.add(col);
			
			col = new JSONObject();
			col.put("field", "schoolNumber");
			col.put("title", "学号");
			line.add(col);
			
			col = new JSONObject();
			col.put("field", "name");
			col.put("title", "姓名");
			line.add(col);
			
			col = new JSONObject();
			col.put("field", "sex");
			col.put("title", "性别");
			line.add(col);
			excelHeads.add(line);
			
			JSONArray excelData = electiveService.getCourseToExport(map);
			
			String[] needMerg = {"courseName","hasSelectedTotalNum","teachers","classroom","hasSelectedNum"};
			
			ExcelTool.exportExcelWithData(excelData, excelHeads, fileName, null, req,res);
			json.put("code", 0);
			json.put("msg", "操作成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			json.put("code", -1);
			json.put("msg", "操作失败");
			e.printStackTrace();
		}
		
		return json;
    }
	/**
	 * 删除选修课程
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "deleteElectiveCourse")
    @ResponseBody
    public JSONObject deleteElectiveCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		try {
			String electiveId=requestParams.getString("electiveId");
			String courseId=requestParams.getString("courseId");
			String flag=requestParams.getString("flag");
    		//String selectedSemester=requestParams.getString("selectedSemester");
			String schoolId=getXxdm(req);
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("electiveId", electiveId);
			JSONObject eObj = electiveService.getElectiveXnxqById(map);
			String schoolYear=eObj.getString("schoolYear");
			String term = eObj.getString("term");
			map.put("termInfo", schoolYear+term);
			map.put("courseId", courseId);
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("electiveId", electiveId);
			param.put("courseId", courseId);
			param.put("termInfo", schoolYear+term);
			param.put("isNotNull", "1");
			List<JSONObject> aList = electiveService.getAttachmentById(param);
			JSONObject attObj = new JSONObject();
			if(aList!=null && aList.size()>0){
				attObj = aList.get(0);
			}
			if(!"1".equals(flag)){
				List<JSONObject> list = electiveService.getCourseSelectedNum(map);
				if(list!=null && list.size()>0){
					JSONObject obj=list.get(0);
					int selectedNum = obj.getIntValue("selectedNum");
					if(selectedNum==0){
						map.put("courseId", "'"+courseId+"'");
						electiveService.deleteElectiveCourse(map);
						json.put("msg", "删除成功");
				        //删除对应的附件
						String attachmentId = attObj.getString("attachmentId");
						if(StringUtils.isNotBlank(attachmentId)){
							param.put("attachmentId", attachmentId);
							electiveService.deleteFileById(param);
						}
					}
					//如果有已选数据，则冻结该课程（学生端不可选）
					map.put("isFreezed", 1);
					electiveService.freezeElectiveCourse(map);
					json.put("code", 0);
					json.put("hasNum", selectedNum);
					return json;
				}else{
					json.put("hasNum", 0);
				}
			}
			map.put("courseId", "'"+courseId+"'");
			electiveService.deleteElectiveCourse(map);
			json.put("code", 0);
			json.put("msg", "删除成功");
			 //删除对应的附件
			String attachmentId = attObj.getString("attachmentId");
			if(StringUtils.isNotBlank(attachmentId)){
				param.put("attachmentId", attachmentId);
				electiveService.deleteFileById(param);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			json.put("code", -1);
			json.put("msg", "删除失败");
			e.printStackTrace();
		}
		
		return json;
    }
    
/**选修课程数量要求设置 ***/    
	/**
	 * 获取选修课程数量
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "getElectiveCourseRequire")
    @ResponseBody
    public JSONObject getElectiveCourseRequire(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
    		String electiveId=requestParams.getString("electiveId");
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String useGrade=requestParams.getString("useGrade");
    		String termInfo=requestParams.getString("termInfo");
    		String schoolId=getXxdm(req);
    		HashMap<String,Object> map=new HashMap<String,Object>();
    		map.put("schoolId", Long.parseLong(schoolId));
    		map.put("electiveId", electiveId);
    		map.put("useGradeList", Arrays.asList(useGrade.split(",")));
    		List<JSONObject> requireList=electiveService.getElectiveCourseRequire(map);
    		map.put("termInfoId", termInfo);
    		map.put("useGradeId", useGrade);
    		if(null!=requireList && requireList.size()>0)
    		{
        		List<Classroom> classList=allCommonDataService.getClassList(map);
        		HashMap<String,String> classMap=new HashMap<String,String>();
        		for(Classroom c:classList)
        		{
        			classMap.put(c.getId()+"", c.getClassName());
        		}
        		for(JSONObject j:requireList)
        		{
        			j.put("className", classMap.get(j.getString("classId")));
        		}
        		requireList= (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, requireList, "className");
        		arr = (JSONArray) JSON.toJSON(requireList);
    		}

    	} catch (Exception e) {
			// TODO Auto-generated catch block
    		logger.error("/elective/getElectiveCourseRequire:",e);
			e.printStackTrace();
			code = -1;
			msg = "获取失败！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", arr);
		return json;
    }
    
	/**
	 * 修改班级选课数量
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "updateElectiveCourseRequire")
    @ResponseBody
    public JSONObject updateElectiveCourseRequire(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String electiveId=requestParams.getString("electiveId");
//		String selectedSemester=requestParams.getString("selectedSemester");
		String classId=requestParams.getString("classId");
		String coursesUpperLimit=requestParams.getString("courseUpperLimit");
		String coursesLowerLimit=requestParams.getString("courseLowerLimit");
		String classhourUpperLimit=requestParams.getString("classhourUpperLimit");
		String classhourLowerLimit=requestParams.getString("classhourLowerLimit");
		if(StringUtils.isEmpty(coursesUpperLimit))
		{
			coursesUpperLimit=null;
		}
		if(StringUtils.isEmpty(coursesLowerLimit))
		{
			coursesLowerLimit=null;
		}
		if(StringUtils.isEmpty(classhourUpperLimit))
		{
			classhourUpperLimit=null;
		}
		if(StringUtils.isEmpty(classhourLowerLimit))
		{
			classhourLowerLimit=null;
		}
		String schoolId=getXxdm(req);
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("classId", classId);
		map.put("electiveId", electiveId);
		map.put("coursesUpperLimit", coursesUpperLimit);
		map.put("coursesLowerLimit", coursesLowerLimit);
		map.put("classhourUpperLimit", classhourUpperLimit);
		map.put("classhourLowerLimit", classhourLowerLimit);

		int num=electiveService.updateElectiveCourseRequire(map);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","保存成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","保存失败");	
		}
		return json;
    }
    
	/**
	 * 删除班级选课数量
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "deleteElectiveCourseRequire")
    @ResponseBody
    public JSONObject deleteElectiveCourseRequire(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String electiveId=requestParams.getString("electiveId");
//		String selectedSemester=requestParams.getString("selectedSemester");
		String classId=requestParams.getString("classId");
		String schoolId=getXxdm(req);
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("classId", classId);
		map.put("electiveId", electiveId);
		int num=electiveService.deleteElectiveCourseRequire(map);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","删除成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","删除失败");	
		}
		return json;
    }
    
	/**
	 * 批量设置班级选课数量
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "batchUpdateElectiveCourseRequire")
    @ResponseBody
    public JSONObject batchUpdateElectiveCourseRequire(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String electiveId=requestParams.getString("electiveId");
//		String selectedSemester=requestParams.getString("selectedSemester");
		String classIds=requestParams.getString("classIds");
		String useGrade=requestParams.getString("useGrade");
		String termInfo=requestParams.getString("termInfo");
		String coursesUpperLimit=requestParams.getString("courseUpperLimit");
		String coursesLowerLimit=requestParams.getString("courseLowerLimit");
		String classhourUpperLimit=requestParams.getString("classhourUpperLimit");
		String classhourLowerLimit=requestParams.getString("classhourLowerLimit");
		if(StringUtils.isEmpty(coursesUpperLimit))
		{
			coursesUpperLimit=null;
		}
		if(StringUtils.isEmpty(coursesLowerLimit))
		{
			coursesLowerLimit=null;
		}
		if(StringUtils.isEmpty(classhourUpperLimit))
		{
			classhourUpperLimit=null;
		}
		if(StringUtils.isEmpty(classhourLowerLimit))
		{
			classhourLowerLimit=null;
		}
		String schoolId=getXxdm(req);

		List<JSONObject> list=new ArrayList<JSONObject>();
		if(StringUtils.isNotEmpty(classIds))
		{
			String[] classId = classIds.split(","); 
			for(int i=0;i<classId.length;i++)
			{
				JSONObject obj=new JSONObject();
				obj.put("schoolYear", termInfo.substring(0, 4));
				obj.put("term", termInfo.substring(4, 5));
				obj.put("classId", classId[i]);
				obj.put("schoolId", schoolId);
				obj.put("electiveId", electiveId);
				obj.put("coursesUpperLimit", coursesUpperLimit);
				obj.put("coursesLowerLimit", coursesLowerLimit);
				obj.put("classhourUpperLimit", classhourUpperLimit);
				obj.put("classhourLowerLimit", classhourLowerLimit);
				obj.put("useGrade", useGrade);
				list.add(obj);
			}
		}
		int num=electiveService.batchUpdateElectiveCourseRequire(list);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","保存成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","保存失败");	
		}
		return json;
    }
	/**
	 * 清空选课数量要求
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "batchDeleteElectiveCourseRequire")
    @ResponseBody
    public JSONObject batchDeleteElectiveCourseRequire(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
//        String selectedSemester=requestParams.getString("selectedSemester");
		String electiveId=requestParams.getString("electiveId");
		String useGrade=requestParams.getString("useGrade");
		String schoolId=getXxdm(req);
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("electiveId", electiveId);
//		map.put("useGrade", useGrade);
		map.put("useGradeList", Arrays.asList(useGrade.split(",")));
		int num=electiveService.batchDeleteElectiveCourseRequire(map);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","删除成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","删除失败");	
		}
		return json;
    }

/**课程类别设置**/    
	/**
	 * 获取选修课程类别
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getCourseTypeList")
    @ResponseBody
    public JSONObject getCourseTypeList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String electiveId=requestParams.getString("electiveId");
    		String schoolId=getXxdm(req);
    		HashMap<String,Object> map=new HashMap<String,Object>();
    		map.put("schoolId", schoolId);
    		map.put("electiveId", electiveId);
     		List<JSONObject> courseType=electiveService.getCourseSort(map);
//    		if(null!=courseType&& courseType.size()>0)
//    		{
//    	   		List<JSONObject> courseNameList=electiveService.getCourseNameByCourseSortId(map); 
//    	   		HashMap<String,String> courseMap=new HashMap<String,String>();
//    	   		for(JSONObject obj:courseNameList)
//    	   		{
//    	   			courseMap.put(obj.getString("courseSortId"), obj.getString("electiveCourseName"));
//    	   		}
//                for(JSONObject obj:courseType)
//                {
//                	obj.put("electiveCourseName", courseMap.get(obj.getString("courseSortId")));
//                }
//                
//    		}
    		arr = (JSONArray) JSON.toJSON(courseType); 
       	} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			logger.error("/elective/getCourseTypeList:",e);
    			code = -1;
    			msg = "获取失败！";
    		}
    		json.put("code", code);
    		json.put("msg", msg);
    		json.put("data", arr);
    		return json;
        }
	/**
	 * 设置课程类别
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "createElectiveCourseType")
    @ResponseBody
    public JSONObject createElectiveCourseType(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String msg = "保存成功";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String courseSortId=UUIDUtil.getUUID();
    		String termInfo=requestParams.getString("termInfo");
			String schoolId=getXxdm(req);
			String electiveId=requestParams.getString("electiveId");
			String courseId=requestParams.getString("courseId");
			String courseSortName=requestParams.getString("courseTypeName").trim();
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("electiveId", electiveId);
			map.put("courseSortId", courseSortId);
			map.put("courseSortName", courseSortName);
			
			List<JSONObject>  typeList=electiveService.getCourseTypeList(map);
			if(null!=typeList && typeList.size()>0)
			{
	        	code=-1;
	        	msg="保存失败,该课程类别已存在！";	
			}
			else
			{
				if(StringUtils.isNotEmpty(courseId))
				{
					map.put("courseIdList", Arrays.asList(courseId.split(",")));
				}
				int num=electiveService.updateElectiveCourseType(map);
				map.put("schoolYear", termInfo.substring(0, 4));
				map.put("term", termInfo.substring(4, 5));
				map.put("courseSortName", courseSortName);
				map.put("createTime", DateUtil.getDateFormatNow());
				num=electiveService.insertElectiveCourseType(map);
		        if(num<=0)
		        {
		        	code=-1;
		        	msg="保存失败";	
				}
			}		

    	} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			logger.error("/elective/createElectiveCourseType:",e);
 			code = -1;
 			msg = "保存失败";
 		}
 		json.put("code", code);
 		json.put("msg", msg);
 		return json;
    }
	/**
	 * 获取未设置的选修课程(如果courseTypeId为空则列出所有未设置的课程，如果不为空则列出当前类别下已设置的课程和未设置的课程)
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getNoSetElectiveCourse")
    @ResponseBody
    public JSONObject getNoSetElectiveCourse(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String electiveId=requestParams.getString("electiveId");
    		String courseSortId=requestParams.getString("courseTypeId");
    		String schoolId=getXxdm(req);
    		HashMap<String,Object> map=new HashMap<String,Object>();
    		map.put("schoolId", schoolId);
    		map.put("electiveId", electiveId);
    		map.put("courseSortId", courseSortId);
     		List<JSONObject> courseType=electiveService.getElectiveCourseList(map);    		
            arr = (JSONArray) JSON.toJSON(courseType); 
    	
       	} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			logger.error("/elective/getNoSetElectiveCourse:",e);
    			code = -1;
    			msg = "获取失败！";
    		}
    		json.put("code", code);
    		json.put("msg", msg);
    		json.put("data", arr);
    		return json;
        }
    
	/**
	 * 修改课程类别
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "updateElectiveCourseType")
    @ResponseBody
    public JSONObject updateElectiveCourseType(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String msg = "保存成功";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String courseSortId=requestParams.getString("courseTypeId");
    		String termInfo=requestParams.getString("termInfo");
			String schoolId=getXxdm(req);
			String electiveId=requestParams.getString("electiveId");
			String courseId=requestParams.getString("courseId");
			String courseSortName=requestParams.getString("courseTypeName").trim();
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("electiveId", electiveId);
			map.put("courseSortId", courseSortId);
			map.put("courseSortName", courseSortName);
			
			List<JSONObject>  typeList=electiveService.getCourseTypeList(map);
			if(null!=typeList && typeList.size()>0)
			{
	        	code=-1;
	        	msg="保存失败,该课程类别已存在！";	
			}
			else
			{
				//修改之前先清空之前这个类别的课程类别id
				int num=electiveService.clearElectiveCourseType(map);
				
				if(StringUtils.isNotEmpty(courseId))
				{
					map.put("courseIdList", Arrays.asList(courseId.split(",")));
				}
				num=electiveService.updateElectiveCourseType(map);
				map.put("schoolYear", termInfo.substring(0, 4));
				map.put("term", termInfo.substring(4, 5));
				map.put("courseSortName", courseSortName);
				num=electiveService.insertElectiveCourseType(map);
		        if(num<=0)
		        {
		        	code=-1;
		        	msg="保存失败";	
				}
			}			

    	} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			logger.error("/elective/updateElectiveCourseType:",e);
 			code = -1;
 			msg = "保存失败";
 		}
 		json.put("code", code);
 		json.put("msg", msg);
 		return json;
    } 
	/**
	 * 删除课程类别
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "deleteElectiveCourseType")
    @ResponseBody
    public JSONObject deleteElectiveCourseType(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String msg = "删除成功";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String courseSortId=requestParams.getString("courseTypeId");    		
			String schoolId=getXxdm(req);
			String electiveId=requestParams.getString("electiveId");
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("electiveId", electiveId);
			map.put("courseSortId", courseSortId);
			
			//修改之前先清空之前这个类别的课程类别id
			int num=electiveService.clearElectiveCourseType(map);
			num=electiveService.deleteElectiveCourseType(map);
	        if(num<=0)
	        {
	        	code=-1;
	        	msg="删除失败";	
			}
    	} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			logger.error("/elective/deleteElectiveCourseType:",e);
 			code = -1;
 			msg = "删除失败";
 		}
 		json.put("code", code);
 		json.put("msg", msg);
 		return json;
    } 
    
    /**课程类别要求设置**/    
	@SuppressWarnings("unchecked")
	/**
	 * 获取选修课程类别要求
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getCourseTypeNumList")
    @ResponseBody
    public JSONObject getCourseTypeNumList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String electiveId=requestParams.getString("electiveId");
    		String useGrade=requestParams.getString("useGrade");
    		String courseTypeId=requestParams.getString("courseTypeId");
    		String termInfo =requestParams.getString("termInfo");
    		String schoolId=getXxdm(req);
    		HashMap<String,Object> map=new HashMap<String,Object>();
    		map.put("schoolId", Long.parseLong(schoolId));
    		map.put("electiveId", electiveId);
    		map.put("useGrade", useGrade);
    		if(StringUtils.isNotEmpty(courseTypeId))
    		{
                map.put("courseSortIdList", Arrays.asList(courseTypeId.split(",")));
    		}

     		List<JSONObject> courseTypeNumList=electiveService.getCourseTypeNumList(map);
    		if(null!=courseTypeNumList&& courseTypeNumList.size()>0)
    		{
        		map.put("termInfoId", termInfo);
        		map.put("useGradeId", useGrade);
        		List<Classroom> classList=allCommonDataService.getClassList(map);
        		HashMap<String,String> classMap=new HashMap<String,String>();
        		for(Classroom c:classList)
        		{
        			classMap.put(c.getId()+"", c.getClassName());
        		}
        		for(JSONObject j:courseTypeNumList)
        		{
        			j.put("className", classMap.get(j.getString("classId")));
        		}
        		courseTypeNumList=(List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, courseTypeNumList, "className");
        		arr = (JSONArray) JSON.toJSON(courseTypeNumList);} 
    		
       	} catch (Exception e) {
    			// TODO Auto-generated catch block
       		    logger.error("/elective/getCourseTypeNumList:",e);
    			e.printStackTrace();
    			code = -1;
    			msg = "获取失败！";
    		}
    		json.put("code", code);
    		json.put("msg", msg);
    		json.put("data", arr);
    		return json;
        }
	/**
	 * 批量设置课程类别要求
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "batchCreateCourseTypeNum")
    @ResponseBody
    public JSONObject batchCreateCourseTypeNum(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
//        String selectedSemester=requestParams.getString("selectedSemester");
		String electiveId=requestParams.getString("electiveId");
		String classIds=requestParams.getString("classIds");
		String useGrade=requestParams.getString("useGrade");
		String termInfo=requestParams.getString("termInfo");
		String coursesUpperLimit=requestParams.getString("courseUpperLimit");
		String coursesLowerLimit=requestParams.getString("courseLowerLimit");
		String courseSortId=requestParams.getString("courseTypeId");
		String schoolId=getXxdm(req);

		if(StringUtils.isEmpty(coursesUpperLimit))
		{
			coursesUpperLimit=null;
		}
		if(StringUtils.isEmpty(coursesLowerLimit))
		{
			coursesLowerLimit=null;
		}

		List<JSONObject> list=new ArrayList<JSONObject>();
		if(StringUtils.isNotEmpty(classIds))
		{
			String[] classId = classIds.split(","); 
			for(int i=0;i<classId.length;i++)
			{
				JSONObject obj=new JSONObject();
				obj.put("schoolYear", termInfo.substring(0, 4));
				obj.put("term", termInfo.substring(4, 5));
				obj.put("classId", classId[i]);
				obj.put("schoolId", schoolId);
				obj.put("electiveId", electiveId);
				obj.put("coursesUpperLimit", coursesUpperLimit);
				obj.put("coursesLowerLimit", coursesLowerLimit);
				obj.put("courseSortId", courseSortId);
				obj.put("useGrade", useGrade);
				list.add(obj);
			}
		}
		int num=electiveService.batchCreateCourseTypeNum(list);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","保存成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","保存失败");	
		}
		return json;
    }
    
	/**
	 * 修改单个课程类别要求
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "updateSingeCourseTypeNum")
    @ResponseBody
    public JSONObject updateSingeCourseTypeNum(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
//        String selectedSemester=requestParams.getString("selectedSemester");
		String electiveId=requestParams.getString("electiveId");
		String classId=requestParams.getString("classId");
		String coursesUpperLimit=requestParams.getString("courseUpperLimit");
		String coursesLowerLimit=requestParams.getString("courseLowerLimit");
		String schoolId=getXxdm(req);
		String courseTypeId=requestParams.getString("courseTypeId");
		if(StringUtils.isEmpty(coursesUpperLimit))
		{
			coursesUpperLimit=null;
		}
		if(StringUtils.isEmpty(coursesLowerLimit))
		{
			coursesLowerLimit=null;
		}

		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("classId", classId);
		map.put("electiveId", electiveId);
		map.put("coursesUpperLimit", coursesUpperLimit);
		map.put("coursesLowerLimit", coursesLowerLimit);
		map.put("courseSortId", courseTypeId);
		int num=electiveService.updateSingeCourseTypeNum(map);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","保存成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","保存失败");	
		}
		return json;
    }
    
	/**
	 * 删除单个课程类别要求
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "deleteCourseTypeNum")
    @ResponseBody
    public JSONObject deleteCourseTypeNum(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
//        String selectedSemester=requestParams.getString("selectedSemester");
		String electiveId=requestParams.getString("electiveId");
		String classId=requestParams.getString("classId");
		String courseTypeId=requestParams.getString("courseTypeId");
		String schoolId=getXxdm(req);
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("classId", classId);
		map.put("electiveId", electiveId);
		map.put("courseSortId", courseTypeId);
		int num=electiveService.deleteCourseTypeNum(map);
        if(num>0)
        {
        	json.put("code",0);
        	json.put("msg","删除成功");
        }
        else 
        {
        	json.put("code",-1);
        	json.put("msg","删除失败");	
		}
		return json;
    }
    
/**调整选课**/    
	/**
	 * 获取学生选课列表
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "getAjustElectiveList")
    @ResponseBody
    public JSONObject getAjustElectiveList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	JSONObject data = new JSONObject();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
		int upperLimit=0;
    	try {
    		String selectedSemester=requestParams.getString("selectedSemester");
			if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
				selectedSemester=getCurXnxq(req);
			}
    		List<JSONObject> selectedStudents=new ArrayList<JSONObject>();
			List<JSONObject> notSelectedStudents=new ArrayList<JSONObject>();
    		String electiveId=requestParams.getString("electiveId");
    		String courseId=requestParams.getString("courseId");
    		String classIds=requestParams.getString("classIds");
    		String stuNameOrNum =requestParams.getString("stuNameOrNum");
    		String selectedCourseNum =requestParams.getString("selectedCourseNum");
//    		StringUtils.isNotEmpty(requestParams.getString("selectedCourseNum"))==true?requestParams.getIntValue("selectedCourseNum"):0;
    		String schoolId=getXxdm(req);
    		long sid=Long.valueOf(schoolId);
    		if(StringUtils.isNotEmpty(classIds))
    		{
        		HashMap<String,Object> map=new HashMap<String,Object>();
        		map.put("schoolId", schoolId);   		
	    		map.put("electiveId", electiveId);
	    		map.put("courseId", courseId);
	    		map.put("termInfo", selectedSemester);
	    		List<JSONObject> selectedList=electiveService.getAjustElectiveList(map);
	    		upperLimit=electiveService.getElectiveCourse(map).get(0).getIntValue("upperLimit");
	    		HashMap<String,JSONObject> selectedMap=new HashMap<String,JSONObject>();
	    		HashMap<String,String> scMap=new HashMap<String,String>();
	    		List<Long> stuids=new ArrayList<Long>();
	    		List<Long> selectClassIds=new ArrayList<Long>();
	    		for(JSONObject j:selectedList)
	    		{
	    			selectedMap.put(j.getString("studentId"), j);
	    			stuids.add(j.getLongValue("studentId"));
	    			scMap.put(j.getString("studentId"), j.getString("classId"));
	    			if(!selectClassIds.contains(j.getLongValue("classId")))
	    			{
		    			selectClassIds.add(j.getLongValue("classId"));
	    			}
	    		}
    			map.put("classIdList", Arrays.asList(classIds.split(",")));
	    		List<JSONObject> noSelectedList=electiveService.getNoSelectedCourseStudentList(map);
	    		HashMap<String,Integer> noSelectedMap=new HashMap<String,Integer>();
	    		for(JSONObject j:noSelectedList)
	    		{
	    			noSelectedMap.put(j.getString("studentId"), j.getIntValue("selectedNum"));
	    		}
	    		
	    		List<Long> cids=new ArrayList<Long>();
	    		for(String s:classIds.split(","))
	    		{
	    			cids.add(Long.valueOf(s));
	    		}
//	    		map.put("classId", classIds);
//	    		map.put("termInfoId", termInfo);
//	    		map.put("keyword ", stuNameOrNum);
//	    		List<Account> accountList=commonDataService.getStudentList(map);
	    		List<Classroom> classList=allCommonDataService.getClassroomBatch(sid,cids,selectedSemester);
	    		HashMap<String,String> classMap=new HashMap<String,String>();
	    		List<Long> stuids2=new ArrayList<Long>();
	    		for(Classroom c:classList)
	    		{
	    			List<Long> sids=c.getStudentAccountIds();
	    			if(null!=sids)
	    			{
	    				stuids2.addAll(sids);
	    			}
	    			classMap.put(c.getId()+"", c.getClassName());	    			
	    		}
	    		
	    		//得到已选择的班级名称
	    		List<Classroom> selectCclassList=allCommonDataService.getClassroomBatch(sid,selectClassIds,selectedSemester);
	    		for(Classroom c:selectCclassList)
	    		{
	    			classMap.put(c.getId()+"", c.getClassName());	    			
	    		}
	    		
	    		int size=stuids.size()-stuids2.size();
	    		if(size>0)
	    		{
	    			//stuids.retainAll(stuids2);
	    			
	    		}
	    		List<Long> list=StringNumTool.removeListRepeat(stuids,stuids2);
	    		List<Account> accountList=allCommonDataService.getAccountBatch(sid,list,selectedSemester);
	    		Map<Long,Account> accountMap = new HashMap<Long,Account>();
	    		if(null!=accountList && accountList.size()>0)
	    		{
	    			for(Account a:accountList){
	    				accountMap.put(a.getId(), a);
	    			}
                    for(Long l:list)
                    {
                    	Account a = accountMap.get(l);
                    	Boolean isInsertList = false;
                    	if(null!=a){
	    	    			if(null!=a.getUsers())
	    	    			{
	                        	for(User u:a.getUsers())
	                        	{
	                        		if(u.getUserPart().getRole().equals(T_Role.Student))
	                        		{
	                        			StudentPart sp=u.getStudentPart();
	                        			String stuNum=(null==sp.getSchoolNumber())?"":sp.getSchoolNumber();
	                        			if(StringUtils.isNotEmpty(stuNameOrNum))
	                	    			{
	                        				if(a.getName().contains(stuNameOrNum)||(stuNum.contains(stuNameOrNum)))
	                	    				{
	                	    				}
	                        				else
	                        				{
	                        					break;
	                        				}
	                	    			}
	                        			JSONObject obj=new JSONObject();
	                        			obj.put("studentId", a.getId());
	                        			obj.put("studentName", a.getName());
	                        			obj.put("studentSex", null==a.getGender()?"":a.getGender().getValue()==1?"男":"女");
	                        			obj.put("studentNum", stuNum);
	                        			obj.put("className", classMap.get(sp.getClassId()+""));
	                        			if(selectedMap.containsKey(a.getId()+""))
	                        			{                       				
	                        				selectedStudents.add(obj);
	                        				isInsertList=true;
	                        			}
	                        			else
	                        			{
	                        				int num=noSelectedMap.containsKey(a.getId()+"")==true?noSelectedMap.get(a.getId()+""):0;
	                        				obj.put("selectedNum", noSelectedMap.containsKey(a.getId()+"")==true?noSelectedMap.get(a.getId()+""):0);                        				
	                        				if(StringUtils.isNotEmpty(selectedCourseNum))
	                        				{
	                        					if(num==Integer.valueOf(selectedCourseNum))
	                        					{
	                        						notSelectedStudents.add(obj);
	                        						isInsertList=true;
	                        					}
	                        				}
	                        				else
	                        				{                            				
	                            				notSelectedStudents.add(obj);
	                            				isInsertList=true;
	                        				}
	                        			}
	                        			break;
	                        		}
	                        	}
	    	    			}
                    	}else{
                    		JSONObject obj=new JSONObject();
                    		String cId=scMap.get(l+"");
                    		List<Long> cIds= new ArrayList<Long>();
                    		if(StringUtils.isNotBlank(cId)){
                    			cIds.add(Long.parseLong(cId));
                    		}
                    		List<Classroom> cList=allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), cIds, selectedSemester);
                			if(cList!=null && cList.size()>0){
                				Classroom c=cList.get(0);
                				if(StringUtils.isNotBlank(c.getClassName())){
                					obj.put("className", c.getClassName());
                				}else{
                					obj.put("className", "[已删除]");
                				}
                				
                			}
                    		obj.put("studentId", l);
                			obj.put("studentName", "[已删除]");
                			obj.put("studentSex","[已删除]");
                			obj.put("studentNum", "[已删除]");
                			
                			if(stuids.contains(l)){
                				selectedStudents.add(obj);
                			}else {
                				notSelectedStudents.add(obj);
                			}
    	    				
                    	}

                    }
                    logger.info("[elective] ###################################selectedStudents={}",selectedStudents);
                   // selectedStudents=(List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, selectedStudents, "className");
                    
	    		}
    		}
    		
    		data.put("upperLimit", upperLimit);
    		data.put("selectedNum", selectedStudents.size());
    		data.put("selectedStudents", selectedStudents);
    		data.put("notSelectedStudents", notSelectedStudents);
		
   	 } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("/elective/getAjustElectiveList:",e);
			code = -1;
			msg = "获取失败！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", data);
		return json;
    }
    
	/**
	 * 添加/删除选课学生
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 * @update 添加并发控制   @author zhh
	 */
    @RequestMapping(value = "updateElectiveStudent")
    @ResponseBody
    public JSONObject updateElectiveStudent(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
        String selectedSemester=requestParams.getString("selectedSemester");
        if(selectedSemester==null ||StringUtils.isBlank(selectedSemester)){
        	selectedSemester=getCurXnxq(req);
        }
		String electiveId=requestParams.getString("electiveId");
		String studentIds=requestParams.getString("studentIds");
		int type=requestParams.getIntValue("type");
		String termInfo=requestParams.getString("termInfo");
		String courseId=requestParams.getString("courseId");
		String schoolId=getXxdm(req);
		if(StringUtils.isNotEmpty(studentIds))
		{

			String[] studentId = studentIds.split(","); 
			List<Long> aids=new ArrayList<Long>();
    		for(String s:studentId)
    		{
    			aids.add(Long.valueOf(s));
    		}
			List<Account> alist=allCommonDataService.getAccountBatch(Long.valueOf(schoolId),aids,selectedSemester);				
			HashMap<String,String> scMap=new HashMap<String,String>();
			for(Account a:alist)
			{
				if(null!=a.getUsers())
				{
					for(User u:a.getUsers())
                	{
                		if(u.getUserPart().getRole().equals(T_Role.Student))
                		{
                			scMap.put(a.getId()+"", u.getStudentPart().getClassId()+"");
                		}
                	}
				}
				else
				{
					System.out.println("users为空："+a.toString());
				}

			}
			if(type==0)
			{
				HashMap<String,Object> map=new HashMap<String,Object>();
				map.put("schoolId", schoolId);
				map.put("studentIdList", Arrays.asList(studentIds.split(",")));
				map.put("electiveId", electiveId);
				map.put("courseId", courseId);
				map.put("termInfo", selectedSemester);
				map.put("scMap", scMap);
				int num=0;
				try {
					num = electiveService.deleteElectiveStudent(map);
					 if(num>0)
				     {
				        json.put("code",0);
				        json.put("msg","删除成功");
				     }
				     else 
				     {
				        json.put("code",-1);
				        json.put("msg","删除失败");	
					 }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		       
				return json;
			}
			else
			{
				   	List<JSONObject> list=new ArrayList<JSONObject>();
					HashMap<String,Object> map=new HashMap<String,Object>();
					map.put("schoolId", schoolId);
					map.put("studentIdList", Arrays.asList(studentIds.split(",")));
					map.put("electiveId", electiveId);
					map.put("courseId", courseId);
					map.put("termInfo", selectedSemester);
					//map.put("scMap", scMap);
					for(int i=0;i<studentId.length;i++)
					{
						JSONObject obj=new JSONObject();
						obj.put("schoolYear", termInfo.substring(0, 4));
						obj.put("term", termInfo.substring(4, 5));
						obj.put("studentId", studentId[i]);
						obj.put("classId", scMap.get(studentId[i]));
						obj.put("schoolId", schoolId);
						obj.put("electiveId", electiveId);
						obj.put("courseId", courseId);
						obj.put("electiveWay", 2);
						obj.put("electiveTime", DateUtil.getDateFormatNow());
						list.add(obj);
					}
				
				int num=0;
				try {
					num = electiveService.insertElectiveStudent(list,map);
					if(num>0)
			        {
			        	json.put("code",0);
			        	json.put("msg","保存成功");
			        }
			        else 
			        {
			        	json.put("code",-1);
			        	json.put("msg","保存失败");	
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return json;
			}
		}
		return json;
    }
    
	/**
	 * 获取授课教师选课列表
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "teacher/getElectiveCourseList")
    @ResponseBody
    public JSONObject getElectiveCourseList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	JSONObject data = new JSONObject();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
			String termInfo=requestParams.getString("termInfo");
			String schoolId=getXxdm(req);
			data.put("termInfo", termInfo);
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("termInfo", termInfo);
			List<JSONObject> teacherList=new ArrayList<JSONObject>();
			List<JSONObject> list=electiveService.getElectiveListByTermInfo(map);
			if(StringUtils.isEmpty(termInfo)&&null!=list&&list.size()>0)
			{
				termInfo=list.get(0).getString("termInfo");
				data.put("termInfo", termInfo);
				map.put("termInfo", termInfo);
				list=electiveService.getElectiveListByTermInfo(map);
			}
			
			if(StringUtils.isNotEmpty(termInfo))
			{
				HttpSession sess = req.getSession();
				User user=(User)(sess.getAttribute("user"));
//				User user=commonDataService.getUserById(Long.valueOf(schoolId), 105478l);//13723758498
				String teacherName=user.getAccountPart().getName();
				map.put("teachers", teacherName);
	    		for(int i=0;i<list.size();i++)
	    		{
	    			JSONObject obj=list.get(i);
	    			map.put("electiveId", obj.getString("electiveId"));    			
	    			List<JSONObject> courseList = electiveService.getElectiveCourse(map);// 只得到该老师授课的课程
	    			if(null==courseList||courseList.size()==0)
	    			{
	    				continue;
	    			}
	    			else
	    			{
	    				teacherList.add(obj);
	    			}
	        		String courseIds=electiveService.getElectiveCourseIds(map);
	        		if(null!=courseIds&& StringUtils.isNotEmpty(courseIds))
	        		{
	        			map.put("courseIdList", Arrays.asList(courseIds.split(",")));
	        		}
	        		else
	        		{
	        			map.put("courseIdList",null);
	        		}
	    			List<JSONObject> timeList=electiveService.getElectiveCourseSchoolTime(map);    			
	    			List<JSONObject> selectedList=electiveService.getSelectedCourseNum(map);
	    			HashMap<String,Integer> selectedMap=new HashMap<String,Integer>();
	    			for(JSONObject o:selectedList)
	    			{
	    				selectedMap.put(o.getString("courseId"), o.getIntValue("selectedNum"));
	    			}    			
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
	    						text+=",第"+lessonOfDay+"节";
	    					}
	    					else
	    					{
	    						text+="; "+StringNumTool.getDayOfWeek(dayOfWeek)+" 第"+lessonOfDay+"节";
	    						dayOfWeekMap.put(courseId+dayOfWeek, "in");
	    					}
	    					timeMap.put(courseId, text);
	    				}
	    				else
	    				{
	    					String text=StringNumTool.getDayOfWeek(dayOfWeek)+" 第"+lessonOfDay+"节";
	    					dayOfWeekMap.put(courseId+dayOfWeek, "in");
	    					timeMap.put(courseId, text);
	    				}
	    			}
	    			
	    			for(JSONObject j:courseList)
	    			{
	    		         j.put("schoolTime", StringNumTool.getSchoolTimeText2(timeMap.get(j.getString("courseId")), courseWeekType.get(j.getString("courseId"))));
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
	    			obj.put("courseList", courseList);
	    		}
	    		
			}

			data.put("rows", teacherList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("/elective/teacher/getElectiveCourseList:",e);
			code = -1;
			msg = "获取失败！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", data);
		return json;
    }
    /**
	 * 发送短信
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "sendMsg")
    @ResponseBody
    public JSONObject sendMsg(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json=new JSONObject();
		String msgStr = "";
		int code = 0;
    	try {
    		Date startTime  = requestParams.getDate("startTime");
    		Date endTime  = requestParams.getDate("endTime");
    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    		String startTimeStr = "";
    		String endTimeStr = "";
    		if(startTime!=null){
    			 startTimeStr = formatter.format(startTime);
    		}
    		if(endTime!=null){
    			 endTimeStr = formatter.format(endTime);
    		}
     		String termInfo=requestParams.getString("selectedSemester");
     		String electiveId=requestParams.getString("electiveId");
     		String electiveName=requestParams.getString("electiveName");
			String schoolId=getXxdm(req);//requestParams.getString("schoolId");//
			long sid=Long.valueOf(schoolId);
			School school = allCommonDataService.getSchoolById(sid, termInfo);
			 
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("termInfo", termInfo);
			map.put("electiveId", electiveId);
			//得到年级、分割 
			String gradeStr = "";
			List<String> gStr = new ArrayList<String>();
			List<JSONObject> courseList=electiveService.getElectiveCourse(map);
			logger.info("courseList:"+courseList.toString());
			if(courseList!=null ){
				for(JSONObject course :courseList){
					JSONArray offerGrade = JSONArray.parseArray(course.getString("offerGrade"));
					for (int i = 0; i < offerGrade.size(); i++) {
						JSONObject obj = offerGrade.getJSONObject(i);
						String useGradeText = obj.getString("useGradeText");
						if(gStr.contains( useGradeText)){
							continue;
						}
						gradeStr += useGradeText + "、";
						gStr.add(useGradeText);
					}
				}
			}
			logger.info("gradeStr:"+gradeStr);
			if("、".equals(gradeStr.substring(gradeStr.length()-1,gradeStr.length()))){
				gradeStr = gradeStr.substring(0,gradeStr.length()-1);
			}
			//应选课班级
			List<Long> shouldClassList=electiveService.getShouldSelectedCourseNum(map);
			logger.info("shouldClassList:"+shouldClassList);
			List<Classroom>  cList= new ArrayList<Classroom>();
			if(shouldClassList!=null && shouldClassList.size()>0){
				  cList=allCommonDataService.getClassroomBatch(sid,shouldClassList,termInfo);
			}
			logger.info("cList:"+cList.size());
			Set<Long> shouldStudentIds=new HashSet<Long>();
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
			//得到已选择的学生id
			List<Long> selectedStudentIds=electiveService.getSubmittedStudentIds(map); 
			//得到未选的学生accountId 去重
			shouldStudentIds.removeAll(selectedStudentIds);
			logger.info("shouldStudentIds:"+shouldStudentIds.size());
			logger.info("shouldStudentIds:"+shouldStudentIds.toString());
			//得到未选的学生accountList
			List<Account> studentAccountList = allCommonDataService.getAccountBatch(sid, new ArrayList<Long>(shouldStudentIds), termInfo);
			List<JSONObject> listParent = allCommonDataService.getSimpleParentByStuMsg(new ArrayList<Long>(shouldStudentIds), termInfo,Long.parseLong(schoolId)  ); 
			if(listParent==null||listParent.size()<1) {
				logger.info("listParent:"+null);
			}else {
				logger.info("listParent:"+listParent.toString());
			}
			
			List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
			List<String> parentNoRepeat = new ArrayList<String>();
			if(listParent!=null) {
				for(JSONObject parentObj:listParent) {
					JSONObject parent = new JSONObject();
					String studentName = parentObj.getString("studentName");
					String parentName = parentObj.getString("name");
					String parentUserId= parentObj.getString("extUserId");
						if(!parentNoRepeat.contains(parentUserId)){
							parent.put("userId", parentUserId );
							parent.put("userName", parentName);
							parent.put("userType", 4);			//  kafka中4为家长
							parent.put("userStudentName", studentName );
							msgCenterReceiversArray.add(parent);
							parentNoRepeat.add(parentUserId);
						}
				}
			}

			/*	//获取家长信息--去重
			List<Long> parentUserIds = new ArrayList<Long>();
			if(studentAccountList!=null){
				for(Account studentAccount :studentAccountList){
					if(studentAccount.getUsers()==null || studentAccount.getUsers().get(0)==null || studentAccount.getUsers().get(0).getStudentPart()==null){
						continue;
					}
					Long parentId = studentAccount.getUsers().get(0).getStudentPart().getParentId();
					if(!parentUserIds.contains(parentId)){
						parentUserIds.add(parentId);
					}
				}
			}
			List<User> parentUserList = allCommonDataService.getUserBatch(Long.parseLong(schoolId), parentUserIds, termInfo );
			//parentUserId-ExtParentUserId
			Map<Long,String> parentMap =new HashMap<Long,String>();
			//parentUserId-name
			Map<Long,String> parentnameMap =new HashMap<Long,String>();
			if(parentUserList!=null) {
				for(User pUser :parentUserList) {
					if(pUser==null || pUser.getAccountPart()==null || pUser.getUserPart()==null) {
						continue;
					}
					String name =pUser.getAccountPart().getName();
					String extUserId = pUser.getAccountPart().getExtId();
					long parentUserId=pUser.getUserPart().getId();
					parentnameMap.put(parentUserId, name);
					parentMap.put(parentUserId, extUserId);
				}
			}
			logger.info("parentMap:"+parentMap.toString());
			logger.info("parentUserList:"+parentUserList.toString());*/
			
			//一、填接收者
		
			
			for (int i = 0; i < studentAccountList.size(); i++) {
				Account account = studentAccountList.get(i);
				//1.填学生接收者
				JSONObject student = new JSONObject();
				student.put("userId", account.getExtId());
				student.put("userName", account.getName());
				student.put("userType", 3);		//  kafka中3为学生
				msgCenterReceiversArray.add(student);
				/*//2.填家长接收者
				if(account==null || account.getUsers()==null || account.getUsers().get(0)==null || account.getUsers().get(0).getStudentPart()==null  ) {
					logger.info("sendHomeworkMsgfailure account:"+account.toString());
					continue;
				}
				Long parentId = account.getUsers().get(0).getStudentPart().getParentId();
				
				logger.info("parentId===>" + parentId );
				String parentUserId= parentMap.get(parentId);
				if(StringUtils.isBlank(parentUserId)) {
					logger.info("sendHomeworkMsgfailure account:{}, parentId:{},parentUserId:{}",account.toString(),parentId,parentUserId);
					continue;
				}
				JSONObject parent = new JSONObject();
				if (StringUtils.isNotBlank(parentUserId)) {
					if(!parentNoRepeat.contains(parentUserId)){
						parent.put("userId", parentUserId.replace("\"", "").trim());
						parent.put("userName", parentnameMap.get(parentId));
						parent.put("userType", 4);			//  kafka中4为家长
						parent.put("userStudentName", account.getName() );
						msgCenterReceiversArray.add(parent);
						parentNoRepeat.add(parentUserId);
					}
				}*/
			}
			//二、填消息体
			 if(msgCenterReceiversArray.size()>0) {
				JSONObject msg = new JSONObject();
			    String msgId = UUIDUtil.getUUID().replace("-", "");
				msg.put("msgId", msgId);
				msg.put("msgTemplateType", "XSXK");
				msg.put("msgTitle",electiveName);
				msg.put("msgContent",  electiveName);
				msg.put("msgUrlPc", msgUrlPc+"?electiveId="+electiveId );
				msg.put("msgUrlApp",  msgUrlApp+"?electiveId="+electiveId );
				msg.put("msgOrigin", "学生选课"); //业务模块名称
				msg.put("msgTypeCode", "XSXK");
				msg.put("schoolId", school.getExtId());
				msg.put("creatorName", school.getName());
				
				JSONObject first = new JSONObject();
				first.put("value", "你好，学校已经创建了选课任务！");

				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", startTimeStr );
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", endTimeStr);
				
				JSONObject keyword3 = new JSONObject();
				keyword3.put("value", gradeStr);
				
				
				JSONObject remark = new JSONObject();
				remark.put("value", "请尽快完成选课，如有疑问请联系老师。");
				
				JSONObject data = new JSONObject();
				data.put("first", first);
				data.put("keyword1", keyword1);
				data.put("keyword2", keyword2);
				data.put("keyword3", keyword3);
				data.put("url", msgUrlApp+"?electiveId="+electiveId);
				data.put("remark", remark);
				msg.put("msgWxJson", data);
				
				JSONObject msgCenterPayLoad = new JSONObject();
				msgCenterPayLoad.put("msg", msg);
				msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
				logger.info("msg=====>" + msg.toString());
				logger.info("msgCenterReceiversArray=====>" + msgCenterReceiversArray.toString());
				logger.info("msgCenterReceiversArray size=====>" + msgCenterReceiversArray.size());
				logger.info("msgCenterPayLoad=====>" + msgCenterPayLoad.toString());
				// 发送消息操作
				KafkaUtils.sendAppMsg(kafkaUrl,electiveId, msgCenterPayLoad , "XSXK", clientId,clientSecret);
			 }
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("/elective/sendMsg:",e);
			code = -1;
			msgStr = "获取失败！";
		}
		json.put("code", code);
		json.put("msg", msgStr);
		return json;
    }

 
}