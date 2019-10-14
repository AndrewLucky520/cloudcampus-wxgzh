package com.talkweb.elective.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.tools.StringNumTool;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.elective.dao.ElectiveDao;
@Repository
public class ElectiveDaoImpl extends MyBatisBaseDaoImpl implements ElectiveDao{
	 @Autowired
	 private AllCommonDataService allCommonDataService;
	@Override
	public List<JSONObject> getCourseTypeList(HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getCourseTypeList",map);
        return list;
	}

	@Override
	public List<JSONObject> getElectiveListByGrade(HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getElectiveListByGrade",map);
        return list;
	}

	@Override
	public String getClassListByCourse(HashMap<String, Object> map) {
		String classIds = selectOne("getClassListByCourse",map);
        return classIds;
	}

	@Override
	public List<JSONObject> getAdminElectiveList(HashMap<String, Object> map) {
		 List<JSONObject> list1 = selectList("getCountSetCourse",map);
		 List<JSONObject> list2 = selectList("getCountSetCourseNum",map);
		 List<JSONObject> list3 = selectList("getCountSetCourseType",map);
		 HashMap<String,Integer> map1=new HashMap<String,Integer>();
		 HashMap<String,Integer> map2=new HashMap<String,Integer>();
		 HashMap<String,Integer> map3=new HashMap<String,Integer>();
		 if(null!=list1&&list1.size()>0)
		 {
			 for(JSONObject obj:list1)
			 {
				 map1.put(obj.getString("electiveId"), obj.getInteger("counts"));
			 }
		 }
		 if(null!=list2&&list2.size()>0)
		 {
			 for(JSONObject obj:list2)
			 {
				 map2.put(obj.getString("electiveId"), obj.getInteger("counts"));
			 }
		 }
		 if(null!=list3&&list3.size()>0)
		 {
			 for(JSONObject obj:list3)
			 {
				 map3.put(obj.getString("electiveId"), obj.getInteger("counts"));
			 }
		 }
		
         List<JSONObject> list = selectList("getAdminElectiveList",map);
         for(JSONObject obj:list)
         {
        	 obj.put("isDelete", 1);
        	 Date s = null;
        	 Date e = null;
        	 if(StringUtils.isNotBlank(obj.getString("startTime"))){
        		  s=obj.getDate("startTime");
        	 }
        	 if(StringUtils.isNotBlank(obj.getString("endTime"))){
        		  e = obj.getDate("endTime");
        	 }
        	 String electiveId=obj.getString("electiveId");
				String isSet="";
				if(map1.containsKey(electiveId)&&map1.get(electiveId)>0)
				{
					isSet+="01,";
					Date d = new Date();
					if(d!=null && s!=null &&d.after(s)&& d.before(e)){
						obj.put("isDelete", 0);
					}
				}
				if((map2.containsKey(electiveId)&&map2.get(electiveId)>0) || (map3.containsKey(electiveId)&&map3.get(electiveId)>0))
				{
					isSet+="02,";
				}
				if(StringUtils.isNotEmpty(isSet))
				{
					obj.put("isSet", isSet.substring(0, isSet.lastIndexOf(",")));
				}
				else
				{
					obj.put("isSet", "");
				}
         }      
        
        return list;
	}

	@Override
	public int createElective(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("createElective", map);
	}

	@Override
	public int updateElective(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("updateElective", map);
	}

	@Override
	public int updateElectiveTime(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("updateElectiveTime", map);
	}

	@Override
	public int deleteElective(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("deleteElective", map);
	}

	@Override
	public List<JSONObject> getElectiveCourse(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
        List<JSONObject> list = selectList("getElectiveCourse",map);
        return list;
	}

	@Override
	public List<JSONObject> getElectiveCourseSchoolTime(
			HashMap<String, Object> map) {
		// TODO Auto-generated method stub
        List<JSONObject> list = selectList("getElectiveCourseSchoolTime",map);
        return list;
	}

	@Override
	public int createElectiveCourse(HashMap<String, Object> map,List<JSONObject> classList,List<JSONObject> teacherList,List<JSONObject> schoolTimeList) {
		int num1=update("createElectiveCourse", map);
		if(null!=classList&&classList.size()>0)
		{
			update("insertElectiveCourseClass", classList);
		}

		if(null!=teacherList&&teacherList.size()>0)
		{
			update("insertCoursesTeacher", teacherList);
		}
		if(null!=schoolTimeList&&schoolTimeList.size()>0)
		{
			update("insertSchoolTime", schoolTimeList);
		}
		return num1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String,Object> batchInsertElectiveCourse(HashMap<String, Object> map) throws Exception{
		List<JSONObject> courseList=(List<JSONObject>) map.get("courseList");
		List<JSONObject> classList=(List<JSONObject>)map.get("classList");
		List<JSONObject> teacherList=(List<JSONObject>)map.get("teacherList");
		List<JSONObject> schoolTimeList=(List<JSONObject>)map.get("schoolTimeList");
		HashMap<String, Object> delMap=(HashMap<String, Object>)map.get("delMap");
		List<String> courseIds=(List<String>) map.get("courseIds");
		//先删除重名的课程
		/*if(map.containsKey("delMap"))
		{
		
			 for(int i=0;i<courseIds.size();i+=20)
			 {
				 int b = i+20;
				 if(b>courseIds.size())
				 {
				   b = courseIds.size();
				 }
				 List<String> cids=courseIds.subList(i,b);
	            StringBuffer sb=new StringBuffer();
	            for(String s:cids)
	            {
	            	sb.append("'").append(s).append("'").append(",");
	            }
	            delMap.put("courseId", sb.substring(0,sb.length()-1));
			   
			 }	
			delMap.put("courseIds", courseIds);
			this.deleteElectiveCourseSingle(delMap);	
			this.delete
		}*/
		//插入课程，如果课程与前面的已提交数据重名，则更新该课程
		if(courseList.size()>0){
			update("batchInsertElectiveCourse", courseList);
		}
        HashMap<String,Object> cMap = new HashMap<String,Object>();
        cMap.put("courseIds", courseIds);
        cMap.put("schoolId", map.get("schoolId"));
        cMap.put("electiveId", map.get("electiveId"));
		if(courseIds!=null && courseIds.size()>0 ){
			//删除课程下的班级
			update("batchDeleteElectiveCourse",cMap);
			//删除课程下上课时间
			update("batchDeleteSchoolTime",cMap);
			//删除课程下的授课老师
			update("batchDeleteCoursesTeacher",cMap);
		}
		//新增课程下的班级
		update("insertElectiveCourseClass", classList);
		
		//新增课程下上课时间
		update("insertSchoolTime", schoolTimeList);
		
		if(null!=teacherList&&teacherList.size()>0)
		{	//新增课程下的授课老师
			update("insertCoursesTeacher", teacherList);
		}
		Set<String> cList= new HashSet<String>();
		for(JSONObject c:courseList){
			cList.add(c.getString("courseId"));
		}
		Map<String,Object> returnMap = new HashMap<String,Object>();
		returnMap.put("cList", cList);
		returnMap.put("num", courseList.size());
		return returnMap;
	}


	@Override
	public int updateElectiveCourse(HashMap<String, Object> map,List<JSONObject> classList,List<JSONObject> teacherList,List<JSONObject> schoolTimeList) {
		update("deleteElectiveCourseClass", map);
		update("deleteCoursesTeacher", map);
		update("deleteSchoolTime", map);
		int num4=update("updateElectiveCourse", map);
		if(null!=classList&&classList.size()>0)
		{
			update("insertElectiveCourseClass", classList);
		}

		if(null!=teacherList&&teacherList.size()>0)
		{
			update("insertCoursesTeacher", teacherList);
		}
		if(null!=schoolTimeList&&schoolTimeList.size()>0)
		{
			update("insertSchoolTime", schoolTimeList);
		}

		return num4;
	}

	@Override
	public int deleteElectiveCourse(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("deleteElectiveCourse", map);
	}
	@Override
	public int deleteElectiveCourseSingle(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("deleteElectiveCourseSingle", map);
	}
	@Override
	public String getElectiveCourseTeacher(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectOne("getElectiveCourseTeacher",map);
	}

	@Override
	public String getElectiveCourseClass(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectOne("getElectiveCourseClass",map);
	}

	@Override
	public List<JSONObject> getElectiveCourseRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getElectiveCourseRequire",map);
	}

	@Override
	public int updateElectiveCourseRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("updateElectiveCourseRequire", map);
	}

	@Override
	public int deleteElectiveCourseRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("deleteElectiveCourseRequire", map);
	}

	@Override
	public int batchUpdateElectiveCourseRequire(List<JSONObject> list) {
		// TODO Auto-generated method stub
		return update("batchUpdateElectiveCourseRequire", list);
	}

	@Override
	public int batchDeleteElectiveCourseRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("batchDeleteElectiveCourseRequire", map);
	}

//	@Override
//	public List<JSONObject> getCourseNameByCourseSortId(
//			HashMap<String, Object> map) {
//		// TODO Auto-generated method stub
//		return selectList("getCourseNameByCourseSortId",map);
//	}

	@Override
	public List<JSONObject> getCourseSort(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getCourseSort",map);
	}

	@Override
	public int updateElectiveCourseType(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("updateElectiveCourseType", map);
	}

	@Override
	public int insertElectiveCourseType(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("insertElectiveCourseType", map);
	}

	@Override
	public List<JSONObject> getElectiveCourseList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getElectiveCourseList",map);
	}

	@Override
	public int clearElectiveCourseType(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("clearElectiveCourseType", map);
	}

	@Override
	public int deleteElectiveCourseType(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("deleteElectiveCourseType", map)+update("deleteCourseTypeRequire", map);
	}

	@Override
	public List<JSONObject> getCourseTypeNumList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
        List<JSONObject> list = selectList("getCourseTypeNumList",map);
        return list;
	}

	@Override
	public int batchCreateCourseTypeNum(List<JSONObject> list) {
		// TODO Auto-generated method stub
		return update("batchCreateCourseTypeNum", list);
	}

	@Override
	public int updateSingeCourseTypeNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("updateSingeCourseTypeNum", map);
	}

	@Override
	public int deleteCourseTypeNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("deleteCourseTypeNum", map);
	}

	@Override
	public List<JSONObject> getAjustElectiveList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
        List<JSONObject> list = selectList("getAjustElectiveList",map);
	   return list;
	}

	@Override
	public List<JSONObject> getNoSelectedCourseStudentList(
			HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getNoSelectedCourseStudentList",map);
        return list;
	}

	@Override
	public int insertElectiveStudent(List<JSONObject> list) {
		// TODO Auto-generated method stub
		return update("insertElectiveStudent", list);
	}

	@Override
	public int deleteElectiveStudent(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return update("deleteElectiveStudent", map);
	}

	@Override
	public List<JSONObject> getSelectedCourseNum(HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getSelectedCourseNum",map);
        return list;
	}

	@Override
	public int getTotalSubmittedNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
	
		List<JSONObject> studentObjIds = selectList("getTotalSubmittedNum", map);
		if(studentObjIds!=null && studentObjIds.size()>0)return studentObjIds.size();
		else return 0;
	}

	@Override
	public List<Long> getShouldSelectedCourseNum(
			HashMap<String, Object> map) {
        List<Long> list = selectList("getShouldSelectedCourseNum",map);
        return list;
	}

	@Override
	public List<Long> getSubmittedStudentIds(HashMap<String, Object> map) {
        List<Long> list = selectList("getSubmittedStudentIds",map);
		return list;
	}

	@Override
	public List<Long> getSubmittedClassIds(HashMap<String, Object> map) {
        List<Long> list = selectList("getSubmittedClassIds",map);
        return list;
	}

	@Override
	public List<JSONObject> getDetailCourseText(HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getDetailCourseText",map);
        return list;
	}

	@Override
	public List<JSONObject> getCourseClassNum(HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getCourseClassNum",map);
        return list;
	}

	@Override
	public List<JSONObject> getStudentCourseIds(HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getStudentCourseIds",map);
        return list;
	}

	@Override
	public List<JSONObject> getStudentCourseText(HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getStudentCourseText",map);
        return list;
	}

	@Override
	public List<JSONObject> getStudentCourseSchoolTime(
			HashMap<String, Object> map) {
        List<JSONObject> list = selectList("getStudentCourseSchoolTime",map);
        return list;
	}

	@Override
	public List<JSONObject> getCurrentElective(String schoolId) {
		 List<JSONObject> list = selectList("getCurrentElective",schoolId);
	     return list;
	}
	@Override
	public List<JSONObject> getCourseSelectedNum(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		List<JSONObject> list = selectList("getCourseSelectedNum",map);
		return list;
	}
	@Override
	public List<JSONObject> getCurrentCourse(HashMap<String,Object> map) {
        List<JSONObject> list = selectList("getCurrentCourse",map);
	     return list;
	}
	
	@Override
	public List<JSONObject> getCurrentCourseAllInfo(HashMap<String,Object> map) {
		Boolean isApp = (Boolean) map.get("isApp");   
		List<JSONObject> courseList = this.getCurrentCourse(map);
			List<JSONObject> timeList=this.getSchoolTimeByClassID(map);
			
			HashMap<String,String> timeMap=new HashMap<String,String>();
			HashMap<String,Object> dayOfWeekMap=new HashMap<String,Object>();
			HashMap<String,List<JSONObject>> schoolTimeMap= new HashMap<String,List<JSONObject>>();
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
						if(isApp!=null && isApp==true){
							text+=";"+StringNumTool.getDayOfWeek(dayOfWeek)+" "+lessonOfDay;
						}else{
							text+=";"+StringNumTool.getDayOfWeek(dayOfWeek)+lessonOfDay;
						}
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
					String text ="";
					if(isApp!=null && isApp==true){
						text=StringNumTool.getDayOfWeek(dayOfWeek)+" "+lessonOfDay;
					}else{
					    text=StringNumTool.getDayOfWeek(dayOfWeek)+lessonOfDay;
					}
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
			//学生所在班级所有选课对应的人数
			List<JSONObject> classSelecteNumList=this.getCourseClassNum(map);
			Map<String,Integer> classSelecteNumMap = new HashMap<String,Integer>();
			for(JSONObject j:classSelecteNumList)
			{
				classSelecteNumMap.put(j.getString("courseId"),j.getIntValue("selectedNum"));
			}
			
			//该学生所选全部课程
//			map.put("studentId", user.getAccountPart().getId());
			List<JSONObject> stuCourseList=this.getStudentCourseIds(map);
			String courseIds="";
			if(null!=stuCourseList && stuCourseList.size()>0)
			{
				courseIds=stuCourseList.get(0).getString("courseIds");
			}
			
			for(JSONObject j:courseList)
			{
				 String courseId=j.getString("courseId");
		         j.put("schoolTimeText", StringNumTool.getSchoolTimeText(timeMap.get(courseId),courseWeekType.get(courseId)));
		         j.put("schoolTime", schoolTimeMap.get(courseId));
		         j.put("classSelectedNum", null==classSelecteNumMap.get(courseId)?0:classSelecteNumMap.get(courseId));
		         if(courseIds.contains(courseId))
		         {
		        	 j.put("isSelected", 1);
		         }
		         else
		         {
		        	 j.put("isSelected", 0);
		         }
		         j.put("weekType", courseWeekType.get(courseId));
//		         j.put("adaptSex", getSex(j.getIntValue("adaptSex")));
			}
	     return courseList;
	}

	@Override
	public List<JSONObject> getSchoolTimeByClassID(HashMap<String,Object> map) {
		 List<JSONObject> list = selectList("getSchoolTimeByClassID",map);
	     return list;
	}

	@Override
	public List<JSONObject> getAllCourse(HashMap<String, Object> map) {
		 List<JSONObject> list = selectList("getAllCourse",map);
	     return list;
	}

	@Override
	public List<String> getCourseIdsByName(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getCourseIdsByName", map);
	}

	@Override
	public List<JSONObject> getElectiveListByTermInfo(
			HashMap<String, Object> map) {
		 List<JSONObject> list = selectList("getElectiveListByTermInfo",map);
	     return list;
	}

	@Override
	public String getElectiveCourseIds(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectOne("getElectiveCourseIds", map);
	}

	@Override
	public List<JSONObject> getSelectedCoureRequire(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		 List<JSONObject> list = selectList("getSelectedCoureRequire",map);
	     return list;
	}

	@Override
	public JSONObject getElectiveXnxqById(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectOne("getElectiveXnxqById",map);
	}

	@Override
	public List<JSONObject> getCourseNameById(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getCourseNameById",map);
	}

	@Override
	public List<JSONObject> getCourseNumForInit(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getCourseNumForInit",map);
	}

	@Override
	public List<JSONObject> getSelectedCourseList(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getSelectedCourseList",map);
	}

	

	@Override
	public JSONObject getOneCourse(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectOne("getOneCourse",map);
	}

	@Override
	public List<JSONObject> getCourseByName(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList("getCourseByName",map);
	}

	@Override
	public void freezeElectiveCourse(HashMap<String, Object> map) {
		update("freezeElectiveCourse",map);
		
	}

	@Override
	public  JSONObject  getCourseToExport(HashMap<String, Object> map) {
		return selectOne("getCourseToExport",map);
	}

	@Override
	public List<JSONObject> getStudentToExport(HashMap<String, Object> map) {
		return selectList("getStudentToExport",map);
	}

	@Override
	public void insertAttachment(JSONObject param) {
		update("insertAttachmentEL",param);
	}

	@Override
	public void deleteAttachment(JSONObject param) {
		update("deleteAttachmentEL",param);
	}

	@Override
	public List<JSONObject> getAttachment(JSONObject param) {
		return selectList("getAttachmentEL",param);
	}

	@Override
	public void updateAttachment(JSONObject param) {
		update("updateAttachmentEL",param);
	}

	@Override
	public void deleteAttachmentByElectiveId(JSONObject param) {
		update("deleteAttachmentByElectiveIdEL",param);
		
	}


//	@Override
//	public int deleteCourseTypeRequire(HashMap<String, Object> map) {
//		// TODO Auto-generated method stub
//		return update("deleteCourseTypeRequire", map);
//	}

}
