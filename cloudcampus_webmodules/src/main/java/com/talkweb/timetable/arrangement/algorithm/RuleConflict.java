package com.talkweb.timetable.arrangement.algorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.jgap.Gene;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.timetable.arrangement.domain.ArrangeTeacher;
import com.talkweb.timetable.arrangement.domain.RuleClassGroup;
import com.talkweb.timetable.arrangement.domain.RuleCourse;
import com.talkweb.timetable.arrangement.domain.RuleGround;
import com.talkweb.timetable.arrangement.domain.RulePosition;
import com.talkweb.timetable.arrangement.domain.RuleResearchMeeting;
import com.talkweb.timetable.arrangement.domain.RuleTeacher;

/**
 * 检查冲突
 * 
 * @author Li xi yuan
 *
 */
public class RuleConflict implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -638218879687593872L;
	private String schoolYear;
	private String termName;

	/**
	 * 教学进度控制
	 */
	private Map<String,RuleTeachProc> ruleTeachProc = new HashMap<String, RuleTeachProc>();
	/**
	 * 教师规则
	 */
	private Map<String, RuleTeacher> ruleTeachers = new HashMap<String, RuleTeacher>();
	/**
	 * 教师规则
	 */
	private Map<String, RuleTeacher> otherGradeTeachers = new HashMap<String, RuleTeacher>();

	/**
	 * 科目规则
	 */
	private Map<String, RuleCourse> ruleCourses = new HashMap<String, RuleCourse>();
	/**
	 * 场地规则
	 */
	private Map<String, RuleGround> ruleGrounds = new HashMap<String, RuleGround>();

	public Map<String, RuleGround> getRuleGrounds() {
		return ruleGrounds;
	}

	public void setRuleGrounds(Map<String, RuleGround> ruleGrounds) {
		this.ruleGrounds = ruleGrounds;
	}

	private List<RuleClassGroup> ruleClassGroups = new ArrayList<RuleClassGroup>();

	private List<RuleResearchMeeting> ruleResearchMeetings = new ArrayList<RuleResearchMeeting>();

	
	public Map<String, Integer> getGradeLevelMaxAmNumMap() {
		return gradeLevelMaxAmNumMap;
	}

	public void addToGradeLevelMaxAmNumMap(String gradeLevlel,int amNum) {
		this.gradeLevelMaxAmNumMap.put(gradeLevlel, amNum);
	}

	private Map<String,Integer> gradeLevelMaxAmNumMap = new HashMap<String, Integer>();
	
	public Map<String, Integer> getGradeLevelMaxPmNumMap() {
		return gradeLevelMaxPmNumMap;
	}
	
	public void addToGradeLevelMaxPmNumMap(String gradeLevlel,int pmNum) {
		this.gradeLevelMaxPmNumMap.put(gradeLevlel, pmNum);
	}
	
	private Map<String,Integer> gradeLevelMaxPmNumMap = new HashMap<String, Integer>();

	/**
	 * 使用年级-年级
	 */
	private Map<String, Grade> gradesDic = new HashMap<String, Grade>();
	
	/**
	 * 年级id-使用年级
	 */
	private Map<Long,String> gradeIdSynjMap = new HashMap<Long, String>();

	public Map<Long, String> getGradeIdSynjMap() {
		return gradeIdSynjMap;
	}

	private Map<String, Grade> gradesDicSrc = new HashMap<String, Grade>();
	/**
	 * 年级id-年级
	 */
	private Map<Long, Grade> gradeIdGradeDic = new HashMap<Long, Grade>();
	
	private Map<Integer, Grade> gradeLevGradeDic = new HashMap<Integer, Grade>();

	private Map<String, Classroom> classroomsDic = new HashMap<String, Classroom>();

	private Map<String, Account> teachersDic = new HashMap<String, Account>();

	private Map<String, LessonInfo> coursesDic = new HashMap<String, LessonInfo>();

	// 取年级信息
	public Grade findGradeInfo(String gradeId) {
		return this.gradesDic.get(gradeId);
	}

	// 取年级信息（深圳接口原始ID）
	public Grade findGradeInfoFromSrc(String gradeId) {
		return this.gradesDicSrc.get(gradeId);
	}

	// 取班级信息
	public Classroom findClassInfo(String classId) {
		return this.classroomsDic.get(classId);
	}

	// 取教师信息
	public Account findTeacherInfo(String teacherId) {
		return this.teachersDic.get(teacherId);
	}

	// 取科目信息
	public LessonInfo findCourseInfo(String courseId) {
		return this.coursesDic.get(courseId);
	}

	// 是否为合班的课程
	public boolean isMergeCourse(String classId, String courseId) {
		for (RuleClassGroup ruleClassGroup : ruleClassGroups) {
			if (ruleClassGroup.getCourseId().equals(courseId)) {
				for (String _classId : ruleClassGroup.getClassIds()) {
					if (_classId.equals(classId)) {
						return true;
					}

				}
			}
		}

		return false;
	}

	/**
	 *  取课程的合班信息- 
	 * @param courseId
	 * @return
	 */
	public List<String> getMergeClass(String classId,String courseId) {
		List<String> classIds = new ArrayList<String>();
		for (RuleClassGroup ruleClassGroup : ruleClassGroups) {
			if (ruleClassGroup.getCourseId().equals(courseId)&&ruleClassGroup.getClassIds().contains(classId)) {
				classIds.addAll(ruleClassGroup.getClassIds());
			}
		}

		return classIds;
	}
	
	/**
	 *  取课程的合班信息- 
	 * @param courseId
	 * @return
	 */
	public List<String> getMergeClassByGroupId(String groupId ) {
		List<String> classIds = new ArrayList<String>();
		for (RuleClassGroup ruleClassGroup : ruleClassGroups) {
			if (ruleClassGroup.getClassGroupId().equals(groupId)) {
				classIds.addAll(ruleClassGroup.getClassIds());
			}
		}
		
		return classIds;
	}
	/**
	 *  取课程的合班信息- 
	 * @param courseId
	 * @return
	 */
	public  String  getMcGroupId(String classId,String courseId) {
		for (RuleClassGroup ruleClassGroup : ruleClassGroups) {
			if (ruleClassGroup.getCourseId().equals(courseId)&&ruleClassGroup.getClassIds().contains(classId)) {
				
				return (ruleClassGroup.getClassGroupId());
			}
		}
		
		return null;
		
	}

	/**
	 * 判断老师是否排到指定的位置
	 * 
	 * @param teacherId
	 *            老师ID
	 * @param day
	 *            周期
	 * @param lesson
	 *            节次
	 * @param srcTeacherId 
	 * @return 0：不排位置，1：必排位置，2：未限制
	 */
	private int canArrangeTeacher(String teacherId, int day, int lesson,int srcday, Set<String> srcTeacherId,int courseType) {
//		if(teacherId.equalsIgnoreCase("102805139")&&day==0&&lesson==2){
//			System.out.println("fsdfds");
//		}
		RuleTeacher ruleTeacher = this.ruleTeachers.get(teacherId);
		if (ruleTeacher == null) {
			return 2;
		}

		List<RulePosition> positions = ruleTeacher.getPositions();
		int rType = 2;
		for (RulePosition rulePosition : positions) {
			if (rulePosition.getDay() == day
					&& rulePosition.getLesson() == lesson) {
				rType =  rulePosition.getRuleType();
				break;
			}
		}
		if(srcday!=-1){
			
			if(ruleTeacher.isTeacherOverNum(day,srcday,courseType)
					&&(srcTeacherId==null||!srcTeacherId.contains(teacherId)||courseType!=0)){
				return 0;
			}else{
				return rType;
			}
		}else{
			if(ruleTeacher.isTeacherOverNum(day,srcday,courseType)){
				return 0;
			}else{
				return rType;
			}
			
		}

	}

	/**
	 * 判断教师在某个位置是否有教研活动安排
	 * 
	 * @param teacherId
	 * @param day
	 * @param lesson
	 * @return 0：不排位置，2：未限制
	 */
	private int canArrangeTeacherByResearchMeeting(String teacherId, int day,
			int lesson) {

		for (RuleResearchMeeting ruleResearchMeeting : ruleResearchMeetings) {
			int _day = ruleResearchMeeting.getDay();
			int _lesson = ruleResearchMeeting.getLesson();
			String _teacherId = ruleResearchMeeting.getTeacherId();
			if (day == _day && lesson == _lesson
					&& _teacherId.equals(teacherId)) {
				return 0;
			}

		}

		return 2;

	}

	// 是否可以安排老师的课到指定的位置
	public int canArrangeTeacher(Map<String, ArrangeTeacher> arrangeTeachers,
			int day, int lesson,int srcday,  HashMap<String, List<JSONObject>> arrangeMap,int courseType) {
		
		return this.canArrangeTeacher(arrangeTeachers.keySet(), day, lesson,  srcday,arrangeMap, courseType);

	}

	// 是否可以安排老师的课到指定的位置
	public int canArrangeTeacher(Set<String> teacherIds, int day, int lesson,int srcday, HashMap<String, List<JSONObject>> arrangeMap,int courseType) {
		List<Integer> result = new ArrayList<Integer>();
		Set<String> srcTeacherIds = null;
		HashMap<String,String> teaMaps = new HashMap<String, String>();
		if(arrangeMap!=null){
			List<JSONObject> arrangeList = arrangeMap.get(day + "," + lesson);
			if (arrangeList!=null&&arrangeList.size()>0) {
				for (JSONObject arrange : arrangeList) {
					Map<String, String> teachers = (Map<String, String>) arrange
							.get("teachers");
					if(teachers!=null){
						
						for(String tea:teachers.keySet()){
							if(tea!=null){
								teaMaps.put(tea, tea);
							}
						}
					}
				}
			}
		}
			
		if(teaMaps.size()>0){
			srcTeacherIds = teaMaps.keySet();
		}
			
		for (String teacherId : teacherIds) {
			int code = this.canArrangeTeacher(teacherId, day, lesson,srcday,srcTeacherIds ,  courseType);
			//
			if (code != 0) {
				// 检查教师是否科研活动
				code = this.canArrangeTeacherByResearchMeeting(teacherId, day,
						lesson);
			}
			result.add(code);
		}
		int[] array = new int[result.size()];
		for (int i = 0; i < result.size(); i++) {
			array[i] = result.get(i);
		}

		if (ArrayUtils.contains(array, 0)) {
			// 只要其中的任何一个教师不能安排在这个位置，就不可以
			return 0;
		}

		if (ArrayUtils.contains(array, 1)) {
			//
			return 1;
		}

		return 2;

	}

	/**
	 * 判断是否可以安排科目到指定的位置
	 * 
	 * @param courseId
	 *            科目ID
	 * @param day
	 *            周期
	 * @param lesson
	 *            节次
	 * @param arrangeMap 
	 * @param classId 
	 * @return 0：不排位置，1：必排位置，2：未限制
	 */
	public int canArrangeCourse(String courseId, int day, int lesson,String gradeLev,
			String classId, HashMap<String, List<JSONObject>> arrangeMap ) {
		
		RuleCourse ruleCourse = this.ruleCourses.get(gradeLev+"_"+courseId);
		if (ruleCourse == null) {
			return 2;
		}

		List<RulePosition> positions = ruleCourse.getPositions();
		int ruleType = 2;
		for (RulePosition rulePosition : positions) {
			if (rulePosition.getDay() == day
					&& rulePosition.getLesson() == lesson) {
				ruleType = rulePosition.getRuleType();
				break;
			}
		}
		if(ruleCourse.isAmNumOrPmNumOver(day, lesson, gradeLevelMaxAmNumMap.get(gradeLev))){
			return 0;
		}else{
			return ruleType;
		}
	}
	/**
	 * 是否满足场地规则
	 * @param gradeId
	 * @param courseId
	 * @param day
	 * @param lesson
	 * @return
	 */
	public boolean canArrangeGround(String gradeId,String courseId,int day,int lesson,int srcday,int courseType){
		String key = gradeId +"_" +courseId;
		RuleGround rg = this.ruleGrounds.get(key);
		if(rg!=null&&rg.isOverRuleGround(day, lesson, srcday ,courseType)){
			return false;
		}else{
			return true;
		}
		
	}

	/**
	 *  判断一个位置是否只能安排合班科目
	 * @param genes
	 * @param classId
	 * @param courseId
	 * @param day	
	 * @param lesson
	 * @return
	 */
	public boolean onlyForMergeCourse(List<Gene> genes, String classId,
			String courseId, int day, int lesson) {

		if (this.isMergeCourse(classId, courseId)) {

			List<String> classIds = this.getMergeClass(classId,courseId);
			for (String _classId : classIds) {
				for (Gene gene : genes) {
					CourseGene courseGene = (CourseGene) gene;
					if (courseGene.getClassId().equals(_classId)
							&& courseGene.getCourseId().equals(courseId)
							&& courseGene.getDay() == day
							&& courseGene.getLesson() == lesson) {
						return true;
					}

				}
			}

		}

		return false;
	}

	// 判断某天是否已经安排指定的课程
	public boolean hasCourseOnDay(CourseGene[][] timetable, int totalMaxLesson,
			int day, String courseId) {

		for (int j = 0; j < totalMaxLesson; j++) {

			CourseGene gene = timetable[day][j];
			if (gene != null && gene.getCourseId().equals(courseId)) {
				//
				return true;
			}

		}

		return false;

	}

	// 判断指定的位置是否已有老师的课
	public boolean hasCourseByTeacher(Gene[] genes, Set<String> teacherIds,
			int day, int lesson) {
		for (Gene gene : genes) {
			CourseGene courseGene = (CourseGene) gene;
			if (courseGene.getDay() == day && courseGene.getLesson() == lesson) {
				for (String teacherId : teacherIds) {
					if (courseGene.getArrangeCourse().getArrangeTeachers()
							.containsKey(teacherId)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	// 判断指定的位置是否已有老师的课
	public boolean hasCourseByTeacher(List<Gene> genes, Set<String> teacherIds,
			int day, int lesson) {
		for (Gene gene : genes) {
			CourseGene courseGene = (CourseGene) gene;
			if (courseGene.getDay() == day && courseGene.getLesson() == lesson) {
				for (String teacherId : teacherIds) {
					if (courseGene.getArrangeCourse().getArrangeTeachers()
							.containsKey(teacherId)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public Map<String, RuleTeacher> getRuleTeachers() {
		return ruleTeachers;
	}

	public void setRuleTeachers(Map<String, RuleTeacher> ruleTeachers) {
		this.ruleTeachers = ruleTeachers;
	}

	public Map<String, RuleCourse> getRuleCourses() {
		return ruleCourses;
	}

	public void setRuleCourses(Map<String, RuleCourse> ruleCourses) {
		this.ruleCourses = ruleCourses;
	}

	public List<RuleClassGroup> getRuleClassGroups() {
		return ruleClassGroups;
	}

	public void setRuleClassGroups(List<RuleClassGroup> ruleClassGroups) {
		this.ruleClassGroups = ruleClassGroups;
	}

	public List<RuleResearchMeeting> getRuleResearchMeetings() {
		return ruleResearchMeetings;
	}

	public void setRuleResearchMeetings(
			List<RuleResearchMeeting> ruleResearchMeetings) {
		this.ruleResearchMeetings = ruleResearchMeetings;
	}

	public Map<String, Grade> getGradesDic() {
		return gradesDic;
	}

	public void setGradesDic(Map<String, Grade> gradesDic) {
		this.gradesDic = gradesDic;

		for(Iterator<String> it = gradesDic.keySet().iterator();it.hasNext();){
			String synj = it.next();
			Grade grade = gradesDic.get(synj);
			
			this.gradeIdSynjMap.put(grade.getId(), synj);
			
			this.gradesDicSrc.put(String.valueOf(grade.getId()), grade);
			
			this.gradeIdGradeDic.put(grade.getId(), grade);
			
			this.gradeLevGradeDic.put(grade.getCurrentLevel().getValue(), grade);
		}
	}

	public Map<String, Classroom> getClassroomsDic() {
		return classroomsDic;
	}

	public void setClassroomsDic(Map<String, Classroom> classroomsDic) {
		this.classroomsDic = classroomsDic;
	}

	public Map<String, Account> getTeachersDic() {
		return teachersDic;
	}

	public void setTeachersDic(Map<String, Account> teachersDic) {
		this.teachersDic = teachersDic;
	}

	public Map<String, LessonInfo> getCoursesDic() {
		return coursesDic;
	}

	public void setCoursesDic(Map<String, LessonInfo> coursesDic) {
		this.coursesDic = coursesDic;
	}

	public String getSchoolYear() {
		return schoolYear;
	}

	public void setSchoolYear(String schoolYear) {
		this.schoolYear = schoolYear;
	}

	public String getTermName() {
		return termName;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}

	public Map<String, RuleTeachProc> getRuleTeachProc() {
		return ruleTeachProc;
	}

	public void setRuleTeachProc(Map<String, RuleTeachProc> ruleTeachProc) {
		this.ruleTeachProc = ruleTeachProc;
	}

	public Map<Long, Grade> getGradeIdGradeDic() {
		return gradeIdGradeDic;
	}

	public Grade getGradeByLevel(int lev){
		return this.gradeLevGradeDic.get(lev);
	}

	public Map<String, RuleTeacher> getOtherGradeTeachers() {
		return otherGradeTeachers;
	}

	public void setOtherGradeTeachers(Map<String, RuleTeacher> otherGradeTeachers) {
		this.otherGradeTeachers = otherGradeTeachers;
	}
	
	public void addArrangedPosition(String classId, String courseId,
			int day,int lesson,Set<String> teacherIds,int courseType){
		if( this.classroomsDic.get(classId)==null){
			return;
		}
		long gradekey = this.classroomsDic.get(classId).getGradeId();
		Grade gd = this.gradeIdGradeDic.get(gradekey);
		if(gd==null||gd.isGraduate||gd.getCurrentLevel()==null){
			return;
		}
		String gradeLev = gd.getCurrentLevel().getValue()+"";
		String gradeId = this.gradeIdSynjMap.get(gradekey);
		RuleCourse ruleCourse = this.ruleCourses.get(gradeLev+"_"+courseId);
		if(ruleCourse!=null){
			if(gradeLevelMaxAmNumMap.get(gradeLev)==null){
				System.out.println("【课表微调】-填充教师失败，无年级上午最大数，使用年级为"+gradeId);
				return;
			}
			ruleCourse.addArrangedPosition(day, lesson, gradeLevelMaxAmNumMap.get(gradeLev));
		}
		for(String teacherId:teacherIds){
			RuleTeacher ruleTeacher = this.ruleTeachers.get(teacherId);
			if(ruleTeacher!=null){
				ruleTeacher.addArrangedPosition(day,  courseType);
			}
		}
		RuleGround ruleGround = this.ruleGrounds.get(gradeId+"_"+courseId);
		if(ruleGround!=null){
			ruleGround.addArrangedPosition(day, lesson,   courseType);
		}
		
	}
	/**
	 * 清除记录的位置
	 */
	public void clearArrangedPosition(){
		for(RuleCourse rc:ruleCourses.values()){
			rc.clearArrangedPosition();
		}
		for(RuleTeacher rt:ruleTeachers.values()){
			rt.clearArrangedPosition();
		}
		for(RuleGround rg:ruleGrounds.values()){
			rg.clearArrangedPosition();
		}
	}

}
