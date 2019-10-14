package com.talkweb.timetable.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.timetable.arrangement.service.ArrangeDataService;
import com.talkweb.timetable.arrangement.service.SmartArrangeService;
import com.talkweb.timetable.service.TimetableService;

/**
 * @author talkweb
 *
 *2015年9月23日
 */
@Controller
@RequestMapping(value = "/timetableManage/common/")
public class TimetableCommonAction extends BaseAction {

	@Autowired
	private TimetableService timetableService;
	@Autowired
	private SmartArrangeService smartArrangeService;
	@Autowired
	private ArrangeDataService arrangeDataService;
	@Autowired
	private AllCommonDataService commonDataService;
	/** -----查询课表年级列表----- **/
	@RequestMapping(value = "getTimetableGradeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetableGradeList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject rs = new JSONObject();
		String schoolId = getXxdm(req );
		String timetableId = request.getString("timetableId");
		int isAll  = request.getIntValue("isAll");
		String xnxq = request.getString("selectedSemester");
		if(xnxq==null||xnxq.trim().length()==0){
			xnxq = getCurXnxq(req);
		}
		String xn = xnxq.substring(0,4);
		HashMap<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("schoolId",schoolId);
		reqMap.put("timetableId",timetableId);
		List<JSONObject> list = timetableService.getTimetableGradeList(reqMap);
		Map<String, Grade> gradesDic = this.arrangeDataService.getGradesDic( xn,getSchool(req,xnxq),xnxq.substring(4,5));
		String allGrades = "";
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		for(JSONObject obj:list){
			String gradeId = obj.getString("gradeId");
			
			Grade grade = gradesDic.get(gradeId);
			if(grade==null){
				continue;
			}
			String gradeName = this.arrangeDataService.getGradeName(grade);
			allGrades+= gradeId+",";
			
			JSONObject v = new JSONObject();
			v.put("value", gradeId);
			v.put("text", gradeName);
			
			rsList.add(v);
		}
		ScoreUtil.sorStuScoreList(rsList, "value", "desc", "", "");
		if(isAll==1){
			JSONObject v = new JSONObject();
			if(allGrades.trim().length()>0){
				allGrades = allGrades.substring(0,allGrades.length()-1);
			}
			v.put("value", allGrades);
			v.put("text", "全部");
			
			rsList.add(0,v);
		}
		int code = -1;
		String msg = "无可用数据";
		if(list.size()!=0 ){
			code =1;
			rs.put("data", rsList);
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	/** -----查询课表下有任教关系的年级列表----- **/
	@RequestMapping(value = "getTimetableGradeListWithTeaching", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetableGradeListWithTeaching(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject rs = new JSONObject();
		String schoolId = getXxdm(req );
		String timetableId = request.getString("timetableId");
		int isAll  = request.getIntValue("isAll");
		String xnxq = request.getString("selectedSemester");
		if(xnxq==null||xnxq.trim().length()==0){
			xnxq = getCurXnxq(req);
		}
		String xn = xnxq.substring(0,4);
		HashMap<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("schoolId",schoolId);
		reqMap.put("timetableId",timetableId);
		
		List<JSONObject> list = timetableService.getTimetableGradeList(reqMap);
		List<JSONObject> taskList = timetableService.getTaskByTimetable(schoolId, timetableId);
		List<Long> cids = new ArrayList<Long>();
		for(JSONObject task :taskList){
			String teachers = task.getString("TeacherId");
			if(teachers==null||teachers.trim().length()==0){
				continue;
			}
			if(task.containsKey("ClassId")){
				long classId = Long.parseLong(task.getString("ClassId"));
				if(!cids.contains(classId)){
					cids.add(classId);
				}
			}
		}
		//开始过滤年级
		List<Long> gradeIds = new ArrayList<Long>();
		List<Classroom> classlist = commonDataService.getSimpleClassBatch(Long.parseLong(schoolId), cids, xnxq);
		if(classlist!=null&&classlist.size()>0){
			for(Classroom cls:classlist){
				if(cls!=null&&cls.getGradeId()!=0){
					gradeIds.add(cls.getGradeId());
				}
			}
		}
		Map<String, Grade> gradesDic = this.arrangeDataService.getGradesDic( xn,getSchool(req,xnxq),xnxq.substring(4,5));
		String allGrades = "";
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		for(JSONObject obj:list){
			String gradeId = obj.getString("gradeId");
			
			Grade grade = gradesDic.get(gradeId);
			if(grade==null||!gradeIds.contains(grade.getId())){
				continue;
			}
			String gradeName = this.arrangeDataService.getGradeName(grade);
			allGrades+= gradeId+",";
			
			JSONObject v = new JSONObject();
			v.put("value", gradeId);
			v.put("text", gradeName);
			
			rsList.add(v);
		}
		ScoreUtil.sorStuScoreList(rsList, "value", "desc", "", "");
		if(isAll==1){
			JSONObject v = new JSONObject();
			if(allGrades.trim().length()>0){
				allGrades = allGrades.substring(0,allGrades.length()-1);
			}
			v.put("value", allGrades);
			v.put("text", "全部");
			
			rsList.add(0,v);
		}
		int code = -1;
		String msg = "无可用数据";
		if(list.size()!=0 ){
			code =1;
			rs.put("data", rsList);
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	/** -----查询课表科目列表----- **/
	@RequestMapping(value = "getTimetableSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetableSubjectList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject rs = new JSONObject();
		String schoolId = getXxdm(req );
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		int isAll  = request.getIntValue("isAll");
		String termInfoId = request.getString("selectedSemester");
		String xn = termInfoId.substring(0,4);

		School sch = getSchool(req,termInfoId);
		
		List<Long> classIds = new ArrayList<Long>();
		if(gradeId.indexOf(",")==-1){
			
			int njdm  = Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(gradeId, xn));
			
			List<Grade> gds = commonDataService.getGradeList(sch,termInfoId);
			Grade gd = null;
			for(Grade g:gds){
				if(g.getCurrentLevel().getValue()==njdm){
					gd = g;
				}
			}
			if(gd!=null&&gd.getClassIds()!=null){
				
				classIds = gd.getClassIds();
			}
		}else{
			String[] synjs = gradeId.split(",");
			List<Grade> gds = commonDataService.getGradeList(sch,termInfoId);
			for(int i=0;i<synjs.length;i++){

				int njdm  = Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(synjs[i], xn));
				
				Grade gd = null;
				for(Grade g:gds){
					if(g.getCurrentLevel().getValue()==njdm){
						gd = g;
					}
				}
				if(gd!=null&&gd.getClassIds()!=null){
					
					classIds.addAll(gd.getClassIds());
				}
			}
		}
		HashMap<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("schoolId",schoolId);
		reqMap.put("timetableId",timetableId);
		if(classIds.size()>0){
			
			reqMap.put("classIds",classIds);
		}
		List<JSONObject> list = timetableService.getTimetableSubjectList(reqMap);
		
		List<LessonInfo> lessons = commonDataService.getLessonInfoList(sch, termInfoId);
		HashMap<Long,LessonInfo> lessMap = new HashMap<Long, LessonInfo>();
		for(LessonInfo les:lessons){
			lessMap.put(les.getId(), les);
		}
		String allLes = "";
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		for(JSONObject obj:list){
			String lessId = obj.getString("courseId");
			String lessName = lessMap.get(Long.parseLong(lessId)).getName();
//			allLes+= lessId+",";	 全部科目为空
			
			JSONObject v = new JSONObject();
			v.put("value", lessId);
			v.put("text", lessName);
			
			rsList.add(v);
		}
		ScoreUtil.sorStuScoreList(rsList, "value", "asc", "", "");
		if(isAll==1){
			JSONObject v = new JSONObject();
			if(allLes.trim().length()>0){
				allLes = allLes.substring(0,allLes.length()-1);
			}
			v.put("value", allLes);
			v.put("text", "全部");
			
			rsList.add(0,v);
		}
		int code = -1;
		String msg = "无可用数据";
		if(rsList.size()!=0 ){
			code =1;
			rs.put("data", rsList);
		}
		
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	/** -----查询课表科目列表----- **/
	@RequestMapping(value = "getTimetableSubjectListWithTeaching", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetableSubjectListWithTeaching(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject rs = new JSONObject();
		String schoolId = getXxdm(req );
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		int isAll  = request.getIntValue("isAll");
		String termInfoId = request.getString("selectedSemester");
		String xn = termInfoId.substring(0,4);
		
		School sch = getSchool(req,termInfoId);
		
		List<String> classIds = new ArrayList<String>();
		if(gradeId.indexOf(",")==-1){
			
			int njdm  = Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(gradeId, xn));
			
			List<Grade> gds = commonDataService.getGradeList(sch,termInfoId);
			Grade gd = null;
			for(Grade g:gds){
				if(g.getCurrentLevel().getValue()==njdm){
					gd = g;
				}
			}
			if(gd!=null&&gd.getClassIds()!=null){
				for(Long cid:gd.getClassIds()){
					classIds.add(String.valueOf(cid));
				}
			}
		}else{
			String[] synjs = gradeId.split(",");
			List<Grade> gds = commonDataService.getGradeList(sch,termInfoId);
			for(int i=0;i<synjs.length;i++){
				
				int njdm  = Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(synjs[i], xn));
				
				Grade gd = null;
				for(Grade g:gds){
					if(g.getCurrentLevel().getValue()==njdm){
						gd = g;
					}
				}
				if(gd!=null&&gd.getClassIds()!=null){
					
					for(Long cid:gd.getClassIds()){
						classIds.add(String.valueOf(cid));
					}
				}
			}
		}
		HashMap<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("schoolId",schoolId);
		reqMap.put("timetableId",timetableId);
		if(classIds.size()>0){
			
			reqMap.put("classIds",classIds);
		}
//		List<JSONObject> list = timetableService.getTimetableSubjectList(reqMap);
		List<String> courseIdlist = new ArrayList<String>();
		List<JSONObject> taskList = timetableService.getTaskByTimetable(schoolId, timetableId,classIds);
		for(JSONObject task :taskList){
			String teachers = task.getString("TeacherId");
			if(teachers==null||teachers.trim().length()==0){
				continue;
			}
			if(task.containsKey("CourseId")){
				String CourseId = task.getString("CourseId");
				if(!courseIdlist.contains(CourseId)){
					courseIdlist.add(CourseId);
				}
			}
		}
		//开始过滤科目
		
		List<LessonInfo> lessons = commonDataService.getLessonInfoList(sch, termInfoId);
		HashMap<Long,LessonInfo> lessMap = new HashMap<Long, LessonInfo>();
		for(LessonInfo les:lessons){
			lessMap.put(les.getId(), les);
		}
		String allLes = "";
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		for(String lessId:courseIdlist){
//			String lessId = obj.getString("courseId");
			long lid = Long.parseLong(lessId);
			if(lid==0||!lessMap.containsKey(lid)){
				continue;
			}
			String lessName = lessMap.get(Long.parseLong(lessId)).getName();
//			allLes+= lessId+",";	 全部科目为空
			if(lessName==null||lessName.trim().length()==0){
				continue;
			}
			JSONObject v = new JSONObject();
			v.put("value", lessId);
			v.put("text", lessName);
			
			rsList.add(v);
		}
		ScoreUtil.sorStuScoreList(rsList, "value", "asc", "", "");
		if(isAll==1){
			JSONObject v = new JSONObject();
			if(allLes.trim().length()>0){
				allLes = allLes.substring(0,allLes.length()-1);
			}
			v.put("value", allLes);
			v.put("text", "全部");
			
			rsList.add(0,v);
		}
		int code = -1;
		String msg = "无可用数据";
		if(rsList.size()!=0 ){
			code =1;
			rs.put("data", rsList);
		}
		
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	
	
	/** -----查询课表教师列表----- **/
	@RequestMapping(value = "getTimetableTeacherList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetableTeacherList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject rs = new JSONObject();
		String schoolId = getXxdm(req );
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		String subjectId = request.getString("subjectId");
		int isAll  = request.getIntValue("isAll");
		String termInfoId = request.getString("selectedSemester");
		String xn = termInfoId.substring(0,4);

		School sch = getSchool(req,termInfoId);
		List<Long> classIds = new ArrayList<Long>();
		if(gradeId.indexOf(",")==-1){
			
			int njdm  = Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(gradeId, xn));
			
			List<Grade> gds = commonDataService.getGradeList(sch,termInfoId);
			Grade gd = null;
			for(Grade g:gds){
				if(g.getCurrentLevel().getValue()==njdm){
					gd = g;
				}
			}
			if(gd!=null&&gd.getClassIds()!=null){
				
				classIds = gd.getClassIds();
			}
		}else{
			String[] synjs = gradeId.split(",");
			List<Grade> gds = commonDataService.getGradeList(sch, termInfoId);
			for(int i=0;i<synjs.length;i++){

				int njdm  = Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(synjs[i], xn));
				
				Grade gd = null;
				for(Grade g:gds){
					if(g.getCurrentLevel().getValue()==njdm){
						gd = g;
					}
				}
				if(gd!=null&&gd.getClassIds()!=null){
					
					classIds.addAll(gd.getClassIds());
				}
			}
		}
		HashMap<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("schoolId",schoolId);
		reqMap.put("timetableId",timetableId);
		if(classIds.size()>0){
			
			reqMap.put("classIds",classIds);
		}
		if(subjectId!=null&&subjectId.trim().length()>0 ){
			reqMap.put("subjectId",subjectId);
			
		}
		long d1 = new Date().getTime();
		List<JSONObject> list = timetableService.getTimetableTeacherList(reqMap);
		List<Long> teaList = new ArrayList<Long>();
		for(JSONObject obj:list){
			String lessId = obj.getString("TeacherId");
			teaList.add(Long.valueOf(lessId));
		}
		long d2 = new Date().getTime();
		System.out.println("【课表公共接口】查询教学任务中的教师耗时"+(d2-d1));
		List<Account> teas = commonDataService.getAccountBatch(Long.parseLong(schoolId), teaList, termInfoId);
		long d3 = new Date().getTime();
		System.out.println("【课表公共接口】查询教师耗时"+(d3-d2));
		HashMap<String,String> lessMap = new HashMap<String,String>();
		for(Account les:teas){
			
			if(les!=null&&les.getUsers()!=null){
				 List<User> us = les.getUsers();
				 for(User u :us){
					 if(u.getTeacherPart()!=null&&u.getUserPart().getRole().equals(T_Role.Teacher)){
						 
						 lessMap.put(les.getId()+"", les.getName());
						 break;
					 }
				 }
			}
			if(!lessMap.containsKey(les.getId()+"")){
				lessMap.put(les.getId()+"", les.getName());
			}
		}
		String allLes = "";
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		for(JSONObject obj:list){
			String lessId = obj.getString("TeacherId");
			String lessName = lessMap.get( lessId) ;
			if(lessName==null||lessName.trim().length()==0){
				continue;
			}
			allLes+= lessId+",";
			
			JSONObject v = new JSONObject();
			v.put("value", lessId);
			v.put("text", lessName);
			
			rsList.add(v);
		}
		try {
			long t1 = new Date().getTime();
			rsList = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, rsList, "text");
			long t2 = new Date().getTime();
			System.out.println("[教师排序耗时]"+(t2-t1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(isAll==1){
			JSONObject v = new JSONObject();
			if(allLes.trim().length()>0){
				allLes = allLes.substring(0,allLes.length()-1);
			}
			v.put("value", allLes);
			v.put("text", "全部");
			
			rsList.add(0,v);
		}
		int code = -1;
		String msg = "无可用数据";
		if(rsList.size()!=0 ){
			code =1;
			rs.put("data", rsList);
			msg = "查询成功！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	/** -----查询任务下的班级列表----- **/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getTeachingTaskClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeachingTaskClassList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject rs = new JSONObject();	
		String schoolId = getXxdm(req );
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		int isAll  = request.getIntValue("isAll");
		String subjectId = request.getString("subjectId");	
		String termInfoId = request.getString("selectedSemester");
		/** ---查询班级信息--- */
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("usedGradeId", gradeId);
		map.put("termInfoId", termInfoId);
		List<Classroom> list = commonDataService.getClassList(map);
		List<String> classIdList = new ArrayList<String>();
		HashMap<String,String> classMap = new HashMap<String,String>();
		for (Classroom room : list){
			 classIdList.add(room.getId()+"");
			 classMap.put(room.getId()+"",room.getClassName());
		}
		/** ---查询本科目存在教学任务的班级--- */
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("timetableId", timetableId);
		if (StringUtils.isNotEmpty(subjectId)){
			param.put("subjectId", subjectId);
		}
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		if (CollectionUtils.isNotEmpty(classIdList)){
			param.put("classIdList", classIdList);
			List<String> cList = timetableService.getTaskClassList(param);
			for(String classId : cList){
				if (classMap.containsKey(classId)){
					JSONObject v = new JSONObject();
					v.put("value", classId);
					v.put("text", classMap.get(classId));
					rsList.add(v);
				}	
			}		
			try {
				rsList = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, rsList, "text");
			} catch (Exception e) {
				e.printStackTrace();
			}
			String allLes = "";
			if (isAll==1){
				JSONObject v = new JSONObject();
				if(allLes.trim().length()>0){
					allLes = allLes.substring(0,allLes.length()-1);
				}
				v.put("value", allLes);
				v.put("text", "全部");				
				rsList.add(0,v);
			}	
		}
		rs.put("code", 1);
		rs.put("data", rsList);
		rs.put("msg", "班级列表");
		return rs;
	}
	
	/** -----查询任务下的班级列表----- **/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getTeachingTaskGradeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeachingTaskGradeList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject rs = new JSONObject();
		String schoolId = getXxdm(req );
		String timetableId = request.getString("timetableId");
		int isAll  = request.getIntValue("isAll");
		String xnxq = request.getString("selectedSemester");
		if(xnxq==null||xnxq.trim().length()==0){
			xnxq = getCurXnxq(req);
		}
		String xn = xnxq.substring(0,4);
		HashMap<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("schoolId",schoolId);
		reqMap.put("timetableId",timetableId);
		
		List<JSONObject> list = timetableService.getTimetableGradeList(reqMap);
		List<JSONObject> taskList = timetableService.getTaskByTimetable(schoolId, timetableId);
		List<Long> cids = new ArrayList<Long>();
		for(JSONObject task :taskList){
//			String teachers = task.getString("TeacherId");
//			if(teachers==null||teachers.trim().length()==0){
//				continue;
//			}
			if(task.containsKey("ClassId")){
				long classId = Long.parseLong(task.getString("ClassId"));
				if(!cids.contains(classId)){
					cids.add(classId);
				}
			}
		}
		//开始过滤年级
		List<Long> gradeIds = new ArrayList<Long>();
		List<Classroom> classlist = commonDataService.getSimpleClassBatch(Long.parseLong(schoolId), cids, xnxq);
		if(classlist!=null&&classlist.size()>0){
			for(Classroom cls:classlist){
				if(cls!=null&&cls.getGradeId()!=0){
					gradeIds.add(cls.getGradeId());
				}
			}
		}
		Map<String, Grade> gradesDic = this.arrangeDataService.getGradesDic( xn,getSchool(req,xnxq),xnxq.substring(4,5));
		String allGrades = "";
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		for(JSONObject obj:list){
			String gradeId = obj.getString("gradeId");
			
			Grade grade = gradesDic.get(gradeId);
			if(grade==null||!gradeIds.contains(grade.getId())){
				continue;
			}
			String gradeName = this.arrangeDataService.getGradeName(grade);
			allGrades+= gradeId+",";
			
			JSONObject v = new JSONObject();
			v.put("value", gradeId);
			v.put("text", gradeName);
			
			rsList.add(v);
		}
		ScoreUtil.sorStuScoreList(rsList, "value", "desc", "", "");
		if(isAll==1){
			JSONObject v = new JSONObject();
			if(allGrades.trim().length()>0){
				allGrades = allGrades.substring(0,allGrades.length()-1);
			}
			v.put("value", allGrades);
			v.put("text", "全部");
			
			rsList.add(0,v);
		}
		int code = -1;
		String msg = "无可用数据";
		if(list.size()!=0 ){
			code =1;
			rs.put("data", rsList);
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
}
