package com.talkweb.elective.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface ElectiveService {
	//公共接口
	/**获取课程类型列表*/
    List<JSONObject> getCourseTypeList(HashMap<String,Object> map);
    /**获取年级下面的选修课程列表*/
    List<JSONObject> getElectiveListByGrade(HashMap<String,Object> map);
    /**获取课程下的班级列表*/
    String getClassListByCourse(HashMap<String,Object> map);
    
    //管理员选课首页
    /**获取管理员选课列表*/
    List<JSONObject> getAdminElectiveList(HashMap<String,Object> map);
    /**创建选课*/
    int createElective(HashMap<String,Object> map);
    /**修改选课名称*/
    int updateElective(HashMap<String,Object> map);
    /**编辑选课时间*/
    int updateElectiveTime(HashMap<String,Object> map);
    /**删除选课*/
    int deleteElective(HashMap<String,Object> map);
    List<JSONObject> getCourseSelectedNum(HashMap<String,Object> map);

    //选修课程设置
    /**获取选修课程*/
    List<JSONObject> getElectiveCourse(HashMap<String,Object> map);
    /**获取选修课程上课时间*/
    List<JSONObject> getElectiveCourseSchoolTime(HashMap<String,Object> map);
    /**创建选修课程*/
    JSONObject createElectiveCourse(HashMap<String,Object> map,List<JSONObject> classList,List<JSONObject> teacherList,List<JSONObject> schoolTimeList)throws Exception;
    /**批量创建选修课程*/
    int insertBatchElectiveCourse(HashMap<String,Object> map)throws Exception;
    /**获取某个课程的任课教师ids*/
    String getElectiveCourseTeacher(HashMap<String,Object> map);
    /**获取某个课程的班级ids*/
    String getElectiveCourseClass(HashMap<String,Object> map);
    /**修改选修课程*/
    JSONObject updateElectiveCourse(HashMap<String,Object> map,List<JSONObject> classList,List<JSONObject> teacherList,List<JSONObject> schoolTimeList)throws Exception;
    /**删除选修课程*/
    int deleteElectiveCourse(HashMap<String,Object> map);
    /**根据选课名称模糊查询选课**/
    List<JSONObject> getCourseByName(HashMap<String,Object> map);
    /**冻结某个课程**/
    void freezeElectiveCourse(HashMap<String,Object> map);
    /**导出选修课程和人数**/
    JSONArray getCourseToExport(HashMap<String,Object> map);
    //选课数量要求设置
    /**获取选课数量*/
    List<JSONObject> getElectiveCourseRequire(HashMap<String,Object> map);
    /**删除选课数量要求*/
    int updateElectiveCourseRequire(HashMap<String,Object> map);
    /**删除选课数量要求*/
    int deleteElectiveCourseRequire(HashMap<String,Object> map);
    /**批量设置选课数量*/
    int batchUpdateElectiveCourseRequire(List<JSONObject> list);
    /**清空选课数量要求*/
    int batchDeleteElectiveCourseRequire(HashMap<String,Object> map);
    
    //课程类别设置
    /**获取课程类别下面 课程名称*/
//    List<JSONObject> getCourseNameByCourseSortId(HashMap<String,Object> map);
    /**获取课程类别*/
    List<JSONObject> getCourseSort(HashMap<String,Object> map);
    /**批量更新开设课程的类别id*/
    int updateElectiveCourseType(HashMap<String,Object> map);
    /**新增或者修改课程类别*/
    int insertElectiveCourseType(HashMap<String,Object> map);
    /**获取选修课程 是否已设置类别*/
    List<JSONObject> getElectiveCourseList(HashMap<String,Object> map);
    /**清空已设置类别*/
    int clearElectiveCourseType(HashMap<String,Object> map);
    /**删除课程类别*/
    int deleteElectiveCourseType(HashMap<String,Object> map);
//    /**删除选课类别 对应的选课要求*/
//    int deleteCourseTypeRequire(HashMap<String,Object> map);
    
    //课程类别要求设置
    /**获取课程类别要求*/
    List<JSONObject> getCourseTypeNumList(HashMap<String,Object> map);
    /**批量设置选课列表要求*/
    int batchCreateCourseTypeNum(List<JSONObject> list);
    /**修改单个课程类别要求*/
    int updateSingeCourseTypeNum(HashMap<String,Object> map);
    /**删除单个课程类别要求*/
    int deleteCourseTypeNum(HashMap<String,Object> map);
    
    //调整学生选课
    /**获取学生选课列表*/
    List<JSONObject> getAjustElectiveList(HashMap<String,Object> map);
    /**获取班级下未选此课程的学生*/
    List<JSONObject> getNoSelectedCourseStudentList(HashMap<String,Object> map);
    /**新增选课学生*/
    int insertElectiveStudent(List<JSONObject> list,HashMap<String,Object> map) throws Exception;
    /**删除选课学生*/
    int deleteElectiveStudent(HashMap<String,Object> map) throws Exception;
    
    //选课人数统计
    /**得到各个课程的选课人数*/
    List<JSONObject> getSelectedCourseNum(HashMap<String,Object> map);
    /**得到已提交学生数*/
    int getTotalSubmittedNum(HashMap<String,Object> map);
    /**应选课人数classids*/
    List<Long> getShouldSelectedCourseNum(HashMap<String,Object> map);
    
    //按课程查看
    /**已提交学生ids*/
    List<Long> getSubmittedStudentIds(HashMap<String,Object> map);
    /**已提交班级ids*/
    List<Long> getSubmittedClassIds(HashMap<String,Object> map);
    /**得到各个课程的详细的内容*/
    List<JSONObject> getDetailCourseText(HashMap<String,Object> map);
    /**得到各个课程各个班级的统计人数*/
    List<JSONObject> getCourseClassNum(HashMap<String,Object> map);
    
    //按班级查看
    /**得到各个学生的选课ids*/
    List<JSONObject> getStudentCourseIds(HashMap<String,Object> map);
    /**得到已选的课程的具体内容*/
    List<JSONObject> getStudentCourseText(HashMap<String,Object> map);
    /**得到已选的课程的具体上课时间*/
    List<JSONObject> getStudentCourseSchoolTime(HashMap<String,Object> map);
    
    //学生选课
    /**得到当前选课*/
    List<JSONObject> getCurrentElective(String schoolId);
    /**获取学生所在班级可以选的课程*/
    List<JSONObject> getCurrentCourse(HashMap<String,Object> map);
    /**获取学生所在班级可以选的课程所有信息*/
    List<JSONObject> getCurrentCourseAllInfo(HashMap<String,Object> map);
    /**获取学生已选课程类别所有信息*/
    List<JSONObject> getSelectedCoureRequire(HashMap<String,Object> map);
    /**获取选修课程上课时间*/
    List<JSONObject> getSchoolTimeByClassID(HashMap<String,Object> map);
    List<JSONObject> getCourseNameById(HashMap<String,Object> map);
    /**学生选课提交**/ 
    JSONObject addElective(HashMap<String,Object> map) throws Exception;
 
    //导入选修课程
    /**获取所有选修课程*/
    List<JSONObject> getAllCourse(HashMap<String,Object> map); 
    /**获取本次选课  班级、教师*/
    public JSONObject getImportCompareData(HashMap<String,Object> map);
    /** 根据课程名称得到对应的课程id*/
    List<String> getCourseIdsByName(HashMap<String,Object> map);
    
    
    //授课教师首页
    /**获取所有选课列表*/
    List<JSONObject> getElectiveListByTermInfo(HashMap<String,Object> map);
    /** 根据选课id得到课程ids*/
    String getElectiveCourseIds(HashMap<String,Object> map);
    //历史学年学期
    JSONObject getElectiveXnxqById(HashMap<String,Object> map);
    
    JSONObject isConflict(HashMap<String,Object> map)throws Exception;
	
    JSONObject uploadFile(JSONObject param, File saveFile) throws Exception;
	List<JSONObject> getAttachmentById(JSONObject param) throws Exception;
	void deleteFileById(JSONObject param) throws Exception;
	void updateAttachment(JSONObject param)throws Exception;
	void deleteAttachmentByElectiveId(JSONObject param)throws Exception;
}
