package com.talkweb.timetable.dynamicProgram.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.timetable.dynamicProgram.entity.GridPoint;
import com.talkweb.timetable.dynamicProgram.entity.GridPointGroup;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleClass;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleDatadic;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTable;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTask;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTaskGroup;
import com.talkweb.timetable.dynamicProgram.enums.CourseLevel;

public class ScheduleRule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1382249283998318213L;
	/**
	 * 教师规则
	 */
	private Map<String, SchRuleTeacher> ruleTeachers = new HashMap<String, SchRuleTeacher>();
	/**
	 * 科目规则
	 */
	private Map<String, SchRuleCourse> ruleCourses = new HashMap<String, SchRuleCourse>();
	/**
	 * 班级_科目--天数 科目位置-记录---科目数小于天数的科目平均 当天已排不再排
	 * 还可以用于各班级课程上下午比例控制
	 */
	private Map<String, Map<Integer,List<Integer>>> classCourseDayMap = new HashMap<String, Map<Integer,List<Integer>>>();
	/**
	 * 课程上午课时记录
	 */
	private Map<String, List<String>> classCourseAmMap = new HashMap<String, List<String>>();
	/**
	 * 课程下午课时记录
	 */
	private Map<String, List<String>> classCoursePmMap = new HashMap<String, List<String>>();
	/**
	 * 场地规则
	 */
	private Map<String, SchRuleGround> ruleGrounds = new HashMap<String, SchRuleGround>();
	/**
	 * 合班规则 键为courseId_classId
	 */
	private HashMap<String, SchRuleClassGroup> ruleClassGroups = new HashMap<String, SchRuleClassGroup>();
	/**
	 * 合班规则 键为 mcGroupid
	 */
	private HashMap<String, SchRuleClassGroup> ruleClassGroupsKeyMap = new HashMap<String, SchRuleClassGroup>();
	/**
	 * 教师_上课日期节次 ---教研活动映射
	 */
	private HashMap<String, List<String>> ruleResearchMeetings = new HashMap<String, List<String>>();

	private Map<String, Integer> gradeLevelMaxAmNumMap = new HashMap<String, Integer>();
	/**
	 * 教师位置是否安排
	 */
	private Map<String, List<String>> teacherArrangePosMap = new HashMap<String, List<String>>();
	
	//教师上午课	
	private Map<String, Map<Integer,List<Integer>>> teacherAmPosMapCCkey = new HashMap<String,Map<Integer,List<Integer>>>();	
	//教师下午课	
	private Map<String, Map<Integer,List<Integer>>> teacherPmPosMapCCkey = new HashMap<String, Map<Integer,List<Integer>>>();	
	/**
	 * 是否控制教师进度 -1不控制 1控制
	 */
	private int isTeachingSync;
	/**
	 * 1教师天内集中 2教师天内分散 3:教师天内集中 半天内连上次数不超过teaSpNum
	 */
	private int lessonDistrubute;
	
	private int teaSpNum = 3;
	/**
	 * 1 尽量排完 不控制进度 2：严格控制进度
	 */
	private int isTryFinish;
	
	/**
	 * 教师 --周次-课时平均条件
	 */
	private Map<String,Map<Integer,Integer>> teacherDayNumMap = new HashMap<String, Map<Integer,Integer>>();

	public Map<String, Map<Integer, Integer>> getTeacherDayNumMap() {
		return teacherDayNumMap;
	}

	public void setTeacherDayNumMap(
			Map<String, Map<Integer, Integer>> teacherDayNumMap) {
		this.teacherDayNumMap = teacherDayNumMap;
	}
	
	/**
	 * 教师 --周次-课时平均条件--教师已安排
	 */
	private Map<String,Map<Integer,Integer>> teacherDayArNumMap = new HashMap<String, Map<Integer,Integer>>();

	public Map<String, Map<Integer, Integer>> getTeacherDayArNumMap() {
		return teacherDayArNumMap;
	}

	public void setTeacherDayArNumMap(
			Map<String, Map<Integer, Integer>> teacherDayArNumMap) {
		this.teacherDayArNumMap = teacherDayArNumMap;
	}
	public void addTeacherDayArNumMap(String teacherId,int day) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if(teacherDayArNumMap.containsKey(teacherId)){
			map = teacherDayArNumMap.get(teacherId);
			if(map.containsKey(day)){
				map.put(day, map.get((Integer) day)+1);
			}else{
				map.put(day, 1);
			}
		}else{
			teacherDayArNumMap.put(teacherId, map);
			map.put(day, 1);
		}
	}
	public void removeTeacherDayArNumMap(String teacherId,int day) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if(teacherDayArNumMap.containsKey(teacherId)){
			map = teacherDayArNumMap.get(teacherId);
			if(map.containsKey(day)){
				int num = map.get((Integer) day);
				if(num>0){
					map.put(day, num-1);
				}else{
					map.remove((Integer) day);
				}
			} 
		} 
	}
	/**
	 * 合班已增加课程记录
	 */
	private List<String> mcGroupLessonKeyList = new ArrayList<String>();
	public List<String> getMcGroupLessonKeyList() {
		return mcGroupLessonKeyList;
	}

	public void setMcGroupLessonKeyList(List<String> mcGroupLessonKeyList) {
		this.mcGroupLessonKeyList = mcGroupLessonKeyList;
	}

	/**
	 * 字典
	 * 
	 * @return
	 */
	private ScheduleDatadic scheduleDatadic = new ScheduleDatadic();

	public ScheduleDatadic getScheduleDatadic() {
		return scheduleDatadic;
	}

	public void setScheduleDatadic(ScheduleDatadic scheduleDatadic) {
		this.scheduleDatadic = scheduleDatadic;
	}

	public Map<String, List<String>> getTeacherArrangePosMap() {
		return teacherArrangePosMap;
	}

	public void setTeacherArrangePosMap(
			Map<String, List<String>> teacherArrangePosMap) {
		this.teacherArrangePosMap = teacherArrangePosMap;
	}

	public void addTeacherArrangePosMap(String teacherId, int day, int lesson,
			int courseType) {
		if (teacherArrangePosMap.containsKey(teacherId)) {
			List<String> poss = teacherArrangePosMap.get(teacherId);
			if (!poss.contains(day + "," + lesson + "," + courseType)) {
				poss.add(day + "," + lesson + "," + courseType);
			}
		} else {
			List<String> poss = new ArrayList<String>();
			poss.add(day + "," + lesson + "," + courseType);
			teacherArrangePosMap.put(teacherId, poss);
		}
	}

	public void removeTeacherArrangePosMap(String teacherId, int day,
			int lesson, int courseType) {
		if (teacherArrangePosMap.containsKey(teacherId)) {
			List<String> poss = teacherArrangePosMap.get(teacherId);
			if (poss.contains(day + "," + lesson + "," + courseType)) {
				poss.remove(day + "," + lesson + "," + courseType);
			}
		}
	}

	// 教师位置是否冲突 是否已有课程
	public boolean isTeacherTimeConflict(String teacherId, int day, int lesson,
			int courseType) {
		if (teacherArrangePosMap.containsKey(teacherId)) {
			List<String> poss = teacherArrangePosMap.get(teacherId);
			String key0 = day + "," + lesson + "," + 0;
			String key1 = day + "," + lesson + "," + 1;
			String key2 = day + "," + lesson + "," + 2;
			switch (courseType) {
			// 正课 无论单双周 都冲突
			case 0:
				if (poss.contains(key0) || poss.contains(key1)
						|| poss.contains(key2)) {
					return true;
				}
				break;
			// 单周课 与单周、正课冲突
			case 1:
				if (poss.contains(key0) || poss.contains(key1)) {
					return true;
				}
				break;
			case 2:
				if (poss.contains(key0) || poss.contains(key2)) {
					return true;
				}
				break;

			default:
				break;
			}

		}
		return false;
	}

	public Map<String, SchRuleGround> getRuleGrounds() {
		return ruleGrounds;
	}

	public void setRuleGrounds(Map<String, SchRuleGround> ruleGrounds) {
		this.ruleGrounds = ruleGrounds;
	}

	public Map<String, SchRuleTeacher> getRuleTeachers() {
		return ruleTeachers;
	}

	public void setRuleTeachers(Map<String, SchRuleTeacher> ruleTeachers) {
		this.ruleTeachers = ruleTeachers;
	}

	public Map<String, SchRuleCourse> getRuleCourses() {
		return ruleCourses;
	}

	public void setRuleCourses(Map<String, SchRuleCourse> ruleCourses) {
		this.ruleCourses = ruleCourses;
	}

	public HashMap<String, SchRuleClassGroup> getRuleClassGroups() {
		return ruleClassGroups;
	}

	public void setRuleClassGroups(List<SchRuleClassGroup> ruleClassGroupList) {
		for (SchRuleClassGroup scg : ruleClassGroupList) {
			List<String> classIds = scg.getClassIds();
			for (String cid : classIds) {

				ruleClassGroups.put(scg.getCourseId() + "_" + cid, scg);
			}
			ruleClassGroupsKeyMap.put(scg.getClassGroupId(), scg);
		}
	}

	public HashMap<String, SchRuleClassGroup> getRuleClassGroupsKeyMap() {
		return ruleClassGroupsKeyMap;
	}

	public void setRuleClassGroupsKeyMap(
			HashMap<String, SchRuleClassGroup> ruleClassGroupsKeyMap) {
		this.ruleClassGroupsKeyMap = ruleClassGroupsKeyMap;
	}

	public HashMap<String, List<String>> getRuleResearchMeetings() {
		return ruleResearchMeetings;
	}

	public void setRuleResearchMeetings(List<SchRuleResearchMeeting> rrms) {
		for (SchRuleResearchMeeting ss : rrms) {
			if (ruleResearchMeetings.containsKey(ss.getTeacherId())) {
				List<String> list = ruleResearchMeetings.get(ss.getTeacherId());
				list.add(ss.getDay() + "," + ss.getLesson());
			} else {
				List<String> list = new ArrayList<String>();
				list.add(ss.getDay() + "," + ss.getLesson());
				ruleResearchMeetings.put(ss.getTeacherId(), list);
			}
		}
	}

	public void addRuleResearchMeetings(String teacherid, int day, int lesson) {
		String key = day + "," + lesson;
		List<String> list = new ArrayList<String>();
		if (ruleResearchMeetings.containsKey(teacherid)) {
			list = ruleResearchMeetings.get(teacherid);
		}
		if (!list.contains(key)) {
			list.add(key);
		}
		ruleResearchMeetings.put(key, list);
	}

	public void setGradeLevelMaxAmNumMap(
			Map<String, Integer> gradeLevelMaxAmNumMap) {
		this.gradeLevelMaxAmNumMap = gradeLevelMaxAmNumMap;
	}

	public Map<String, Integer> getGradeLevelMaxAmNumMap() {
		return gradeLevelMaxAmNumMap;
	}

	public void addToGradeLevelMaxAmNumMap(String gradeLevlel, int amNum) {
		this.gradeLevelMaxAmNumMap.put(gradeLevlel, amNum);
	}

	// 是否为合班的课程
	public boolean isMergeCourse(String classId, String courseId) {

		if (ruleClassGroups.containsKey(courseId + "_" + classId)) {
			return ruleClassGroups.get(courseId + "_" + classId) != null;
		} else {
			return false;
		}

	}

	/**
	 * 取课程的合班信息-
	 * 
	 * @param courseId
	 * @return
	 */
	public List<String> getMergeClass(String classId, String courseId) {
		List<String> classIds = new ArrayList<String>();
		if (ruleClassGroups.containsKey(courseId + "_" + classId)) {
			SchRuleClassGroup rg = ruleClassGroups
					.get(courseId + "_" + classId);
			if (rg != null) {
				classIds.addAll(rg.getClassIds());
			}
		}

		return classIds;
	}

	/**
	 * 取课程的合班信息-
	 * 
	 * @param courseId
	 * @return
	 */
	public List<String> getMergeClassByGroupId(String McGroupId) {
		List<String> classIds = new ArrayList<String>();

		SchRuleClassGroup rg = ruleClassGroupsKeyMap.get(McGroupId);

		if (rg != null) {
			classIds.addAll(rg.getClassIds());
		}

		return classIds;
	}

	/**
	 * 取课程的合班信息-
	 * 
	 * @param courseId
	 * @return
	 */
	public String getMcGroupId(String classId, String courseId) {
		if (ruleClassGroups.containsKey(courseId + "_" + classId)) {
			SchRuleClassGroup rg = ruleClassGroups
					.get(courseId + "_" + classId);
			if (rg != null) {
				return rg.getClassGroupId();
			}
		}

		return null;

	}

	/**
	 * 教师位置冲突--必要条件
	 */
	public boolean isTeacherRuleFullFill(String teacherId, int day, int lesson,
			int courseType,double arNum,ScheduleTable table,int amNum) {
		//是否满足教师课时平均
		
		// 教师规则
		SchRuleTeacher teaRule = this.ruleTeachers.get(teacherId);
		// 教研活动
		List<String> schm = this.ruleResearchMeetings.get(teacherId);
		boolean rs = true;
		if (teaRule != null) {
			if (!teaRule.canArrangeTeacherPositions(day, lesson, arNum)) {
				rs = false;
			}
		}
		if (schm != null) {
			if (schm.contains(day + "," + lesson)) {
				rs = false;
			}
		}
		//算法增加 	处理教师规则（上午最后一节与下午最后一节冲突）
		if(lesson == (amNum-1) && !table.isDeepSwap() ){
			if (isTeacherTimeConflict(teacherId, day, lesson+1, courseType)) {
				rs = false;
			}
		}
		if(lesson == amNum && !table.isDeepSwap() ){
			if (isTeacherTimeConflict(teacherId, day, lesson-1, courseType)) {
				rs = false;
			}
		}
		if (isTeacherTimeConflict(teacherId, day, lesson, courseType)) {
			rs = false;
		}
		
		if(arNum==1&&!isTeacherCourseTimeAvg(teacherId, null, day)){
			rs = false;
		}
		//控制连排次数
		if(rs && lessonDistrubute!=1){
			rs = !isTeacherSpNumOver(amNum,teacherId,day,lesson,table);
			if(rs == false){
				System.out.println("控制教师半天次数成功！");
			}
		}
		return rs;
	}

	/**
	 * 判断 教师半天内连排次数是否超限
	 * @param amNum
	 * @param teacherId
	 * @param day
	 * @param lesson
	 * @param table 
	 * @return
	 */
	public boolean isTeacherSpNumOver(int amNum,String teacherId, int day, int lesson, ScheduleTable table) {
		// TODO Auto-generated method stub
		int maxSpNum = 2;
		if(lessonDistrubute==3){
			maxSpNum = teaSpNum;
		}
		List<Integer> poss = new ArrayList<Integer>();
		if(lesson<=amNum-1){
			if(teacherAmPosMapCCkey.containsKey(teacherId)){
				Map<Integer, List<Integer>> daymap = teacherAmPosMapCCkey.get(teacherId);
				if(daymap.containsKey(day)){
					poss = daymap.get(day);
				}
			}
		}else{
			if(teacherPmPosMapCCkey.containsKey(teacherId)){
				Map<Integer, List<Integer>> daymap = teacherPmPosMapCCkey.get(teacherId);
				if(daymap.containsKey(day)){
					poss = daymap.get(day);
				}
			}
		}
		//只有已安排位置的才去计算
		if(poss.size()>0){
			poss.add((Integer) lesson);
			Collections.sort(poss);
			
			int maxLx = 1;
			int last = -10;
			for(int les :poss){
				if(maxLx>maxSpNum){
					//超过了
					return true;
				}
				if(les==last+1){
					maxLx++;
				}else{
					maxLx =0;
				}
				last = les;
			}
			if(maxLx>maxSpNum){
				//超过了
				return true;
			}
		}
		
		return false;
	}

	/**
	 * 获取课程上午还是下午上课
	 * 
	 * @param gradeLevel
	 * @param courseId
	 * @return
	 */
	public CourseLevel getCourseLevelByGradeCourse(String gradeLevel,
			String courseId) {
		// TODO Auto-generated method stub
		CourseLevel cl = CourseLevel.AllCan;
		if (gradeLevel != null) {
			SchRuleCourse ruleCourse = this.ruleCourses.get(gradeLevel + "_"
					+ courseId);
			if (ruleCourse != null) {
				// 0：尽量上午，1：各节次均可，2:尽量下午
				int intVal = ruleCourse.getCourseLevel();
				switch (intVal) {
				case 0:
					cl = CourseLevel.AmFirst;
					break;
				case 1:
					if(Long.parseLong(courseId)<=9){
						cl = getDefaultCourseLvlByGradeLvl(gradeLevel,courseId);
//						cl = CourseLevel.AmFirst;
					}else{
						cl = CourseLevel.PmFirst;
					}
					break;
				case 2:
					cl = CourseLevel.PmFirst;
					break;
					
				case 3:
					cl = CourseLevel.AmFirst;
					break;
				default:
					cl = CourseLevel.AllCan;
					break;
				}
			}else{
				cl = getDefaultCourseLvlByGradeLvl(gradeLevel,courseId);
			}
		}
		return cl;
	}
	/**
	 * 获取原始课程上午还是下午上课(教师设置)
	 * 
	 * @param gradeLevel
	 * @param courseId
	 * @return
	 */
	public CourseLevel getOrCourseLevelByGradeCourse(String gradeLevel,
			String courseId) {
		// TODO Auto-generated method stub
		CourseLevel cl = CourseLevel.AllCan;
		if (gradeLevel != null) {
			SchRuleCourse ruleCourse = this.ruleCourses.get(gradeLevel + "_"
					+ courseId);
			if (ruleCourse != null) {
				// 0：尽量上午，1：各节次均可，2:尽量下午
				int intVal = ruleCourse.getCourseLevel();
				switch (intVal) {
				case 0:
					cl = CourseLevel.AmFirst;
					break;
				case 1:
					cl = CourseLevel.AllCan;
					break;
				case 2:
					cl = CourseLevel.PmFirst;
					break;

				case 3:
					cl = CourseLevel.AmButNotAll;
					break;
				default:
					cl = CourseLevel.AllCan;
					break;
				}
			}else{
				cl =  CourseLevel.AllCan;
			}
		}
		return cl;
	}
	/**
	 * 根据年级获取课程排上下午
	 * @param gradeLevel
	 * @param courseId
	 * @return
	 */
	public CourseLevel getDefaultCourseLvlByGradeLvl(String gradeLevel,
			String courseId) {
		// TODO Auto-generated method stub
		Long cid = Long.parseLong(courseId);
		int glv = Integer.parseInt(gradeLevel);
		int stage = 1;
		if(glv==T_GradeLevel.T_PrimaryOne.getValue()
				||glv==T_GradeLevel.T_PrimaryTwo.getValue()
				||glv==T_GradeLevel.T_PrimaryThree.getValue()
				||glv==T_GradeLevel.T_PrimaryFour.getValue()
				||glv==T_GradeLevel.T_PrimaryFive.getValue()
				||glv==T_GradeLevel.T_PrimarySix.getValue()
				){
			stage = 0;
		}
		if(stage==0){
			if(cid<=3){
				//小学语数外
				return CourseLevel.AmFirst;
			}else{
				//小学其他课程
				return CourseLevel.PmFirst;
			}
		}else{
			if(cid<=9){
				//初高中主课
				return CourseLevel.AmFirst;
			}else{
				//初高中其他课程
				return CourseLevel.PmFirst;
			}
		}
	}
	/**
	 * 上午排课最小比例
	 * @param gradeLevel
	 * @param courseId
	 * @param schTask 
	 * @return
	 */
	public double getCourseAmMinPercentByGradeCourse(String gradeLevel,
			String courseId, ScheduleTask schTask) {
		// TODO Auto-generated method stub
		CourseLevel cl = schTask.getCourseLevel();
		CourseLevel orcl = schTask.getOrCourseLevel();
		double taskNum = schTask.getTaskNum();
		int am = schTask.getClassAmNum();
		int pm = schTask.getClassPmNum();
		if(taskNum>4&&cl.equals(CourseLevel.AmFirst)&&orcl.equals(CourseLevel.AmFirst)&&!schTask.isAllAmFirst()){
			return taskNum-3;
		}
		if(cl.equals(CourseLevel.AmFirst)&&orcl.equals(CourseLevel.AllCan)){
			Long cid =Long.parseLong(courseId);
			int glv = Integer.parseInt(gradeLevel);
			if(cid<=3){
				double num = am*taskNum/(am+pm) +1;
				return taskNum-num-1;
			}else if(cid>3&&cid<=9){
				
				return 0;
			} 
		}
		// TODO Auto-generated method stub
		return taskNum;
	}
	/**
	 * 上午排课最大比例(后期更改为课时数)
	 * @param gradeLevel
	 * @param courseId
	 * @param schTask 
	 * @return
	 */
	public double getCourseAmMaxPercentByGradeCourse(String gradeLevel,
			String courseId, ScheduleTask schTask) {
		CourseLevel cl = schTask.getCourseLevel();
		CourseLevel orcl = schTask.getOrCourseLevel();
		double taskNum = schTask.getTaskNum();
		int am = schTask.getClassAmNum();
		int pm = schTask.getClassPmNum();
		if(taskNum>4&&cl.equals(CourseLevel.AmFirst)&&orcl.equals(CourseLevel.AmFirst)&&!schTask.isAllAmFirst()){
			return taskNum-1;
		}
		if(taskNum<=4&&orcl.equals(CourseLevel.AmFirst)){
			return taskNum;
		}
		if(cl.equals(CourseLevel.AmFirst)&&orcl.equals(CourseLevel.AllCan)){
			Long cid =Long.parseLong(courseId);
			int glv = Integer.parseInt(gradeLevel);
			if(cid<=3){
				double num = Math.ceil(am*taskNum/(am+pm)) +1;
				return num;
			}else if(cid>3&&cid<=9){
				if(taskNum<=3){
					return 1;
				} 
				if(taskNum>3&&taskNum<=5){
					return 2;
				}
				double num = am*taskNum/(am+pm)  ;
				if(num>1){
					num = num-1;
				}
				return num;
			} 
		}
		// TODO Auto-generated method stub
		return 0;
	}
	/**
	 * 下午排课最大节次 
	 * @param gradeLevel
	 * @param courseId
	 * @param schTask 
	 * @return
	 */
	public double getCoursePmMaxNumByGradeCourse(String gradeLevel,
			String courseId, ScheduleTask schTask) {
		CourseLevel cl = schTask.getCourseLevel();
		CourseLevel orcl = schTask.getOrCourseLevel();
		double taskNum = schTask.getTaskNum();
		int am = schTask.getClassAmNum();
		int pm = schTask.getClassPmNum();
		if(taskNum>4&&cl.equals(CourseLevel.AmFirst)&&orcl.equals(CourseLevel.AmFirst)&&!schTask.isAllAmFirst()){
			return 2;	
		}
		if(taskNum<=4&&cl.equals(CourseLevel.AmFirst)&&orcl.equals(CourseLevel.AmFirst)&&!schTask.isAllAmFirst()){
			return 1;	
		}
		if( orcl.equals(CourseLevel.AmFirst)){
			return 0;
		}
		// TODO Auto-generated method stub
		return taskNum;
	}
	/**
	 * 科目规则是否满足--必要条件
	 */
	public boolean isCourseRuleFullFill(String courseId, String classId,
			int day, int lesson,double taskNum,int maxDays,int spNum,double arNum ,ScheduleTable table) {

		String gradeLevel = scheduleDatadic.getGradeLevelByClassId(classId);
		int amNum = this.gradeLevelMaxAmNumMap.get(gradeLevel);
		SchRuleCourse ruleCourse = null;
		if (gradeLevel != null) {
			ruleCourse = this.ruleCourses.get(gradeLevel + "_"
					+ courseId);
			if (ruleCourse != null) {
				boolean rs = ruleCourse.canArrangeCourse(day, lesson, amNum);
				if (!rs) {
					return false;
				}
			}
		}
		
		
		// 单班课程数小于总天数的 控制每天不重复上课
		String cckey = classId + "_" + courseId;
		if ( classCourseDayMap.containsKey(cckey)&&classCourseDayMap.get(cckey).containsKey(day)) {
			List<Integer> lessonList = classCourseDayMap.get(cckey).get(day);
			//排单节课
			if(arNum<=1){
				//控制上课节次
				if(taskNum<=maxDays){
					//  每天不超过一节
					if(lessonList!=null&&lessonList.size()>0){
						return false;
					}
				}else {
					if(spNum==0){
						// 连排数为0 每天不超过2节
						if((taskNum<=maxDays*2)&&lessonList!=null&&lessonList.size()>1){
							return false;
						}
						
						//控制不可连排时不连排
						ScheduleTask task = table.getClassCourseTaskMap().get(cckey);
						ScheduleClass schClass = table.getScheduleClassByClassId(task.getClassId());
						GridPointGroup[][] classtable = schClass.getGridPointArr();
						if(task.getTaskNum()>maxDays&&task.getSpNum()==0){
							if(table.isOwnCourseNear(task, classtable, day, lesson)){
								return false;
							}
						}
					}else{
						// 连拍数大于0 每天不超过一节
						ScheduleTask task = table.getClassCourseTaskMap().get(cckey);
//						if( !(arNum<2&&task.getNeedSpNum()>0)&&lessonList!=null&&lessonList.size()>0
//								&&(task.getTaskNum()-spNum-task.getMaxDays()<=0)&&spNum<task.getMaxDays()){
//							return false;
//						}
						//控制课时分布均匀
						if( task.getNeedSpNum()==0&&lessonList!=null&&lessonList.size()>0
								&&(task.getTaskNum()-spNum-task.getMaxDays()<=0)&&spNum<task.getMaxDays()){
							return false;
						}
						if(arNum<2&&task.getNeedSpNum()>0&&lessonList!=null&&lessonList.size()>1
								&&(task.getTaskNum()-spNum-task.getMaxDays()<=0)&&spNum<task.getMaxDays()){
							
							return false;
						}
//						if( lessonList!=null&&lessonList.size()> 0
//								&&(task.getTaskNum()-spNum-task.getMaxDays()<=0)&&spNum<task.getMaxDays()){
//							return false;
//						}
					}
				}
				
			}else if(arNum==2){
				// 排连排课
				//  每天不超过一节
				if(lessonList!=null&&lessonList.size()>0){
					ScheduleTask task = table.getClassCourseTaskMap().get(cckey);
					//连排次数过多的 
					if(task!=null&&spNum>task.getMaxDays()){
						if(lessonList!=null&&lessonList.size()>1){
							return false;
						}
					}else{
						
						return false;
					}
				}
			}
		}
		//课程为尽量排上午课的 需要控制其上午课程数比例(后期更改为课时数的控制)
		ScheduleTask task = table.getClassCourseTaskMap().get(cckey);
//		if ( ruleCourse!=null  && task!=null&&task.getCourseLevel().equals(CourseLevel.AmFirst)&&
//				(task.getOrCourseLevel().equals(CourseLevel.AllCan)||
//						(task.getOrCourseLevel().equals(CourseLevel.AmFirst)&&!task.isAllAmFirst()))
//				  && classCourseDayMap.containsKey(cckey)){
//			//上午排课 控制上午比例
//			if(lesson<=amNum-1&&classCourseAmMap.containsKey(cckey)){
//				List<String> amdays = classCourseAmMap.get(cckey);
//				if(amdays!=null ){
//					double ammax = task.getAmMaxPercent();
//					if( amdays.size()>ammax){
//						return false;
//					}
//				}
//			} else if(task.getOrCourseLevel().equals(CourseLevel.AmFirst)
//					&&classCoursePmMap.containsKey(cckey)){
//				//下午排课 控制下午比例
//				double pmmax = task.getPmMaxNum();
//				List<String> pmdays = classCoursePmMap.get(cckey);
//				if(pmdays!=null ){
//					if( pmdays.size()>pmmax){
//						return false;
//					}
//				}
//			}
//		}
		Long cid =Long.parseLong(task.getCourseId());
		if(!table.isDeepSwap()&&taskNum==1){
			//控制理科课程不在上午最后一节和下午第一节
			amNum = task.getClassAmNum();
			//设置理科不排
			if(lesson==amNum||lesson==amNum-1){
				if(cid==2||cid==7||cid==8||cid==9){
					return false;
				}
			}
			
		}
		if(lesson == 0){
			if(cid==12||cid==14||cid==15 ){
				return false;
			}
		}
		
		//控制不可连排时不连排
		if(task.getTaskNum()>maxDays&&task.getNeedSpNum()==0){
			ScheduleClass schClass = table.getScheduleClassByClassId(task.getClassId());
			GridPointGroup[][] classtable = schClass.getGridPointArr();
			if(table.isOwnCourseNear(task, classtable, day, lesson)){
				return false;
			}
		}
		return true;

	}

	private boolean hasLpInDay(List<Integer> ll, int amNum) {
		// TODO Auto-generated method stub
		for(int i=0;i<ll.size();i++){
			if(ll.get(i)<amNum){
				if( ll.get(i)+1<amNum&&ll.contains((Integer) ll.get(i)+1)){
					return false;
				}
				if( ll.get(i)-1>=0&&ll.contains((Integer) ll.get(i)-1)){
					return false;
				}
			}else{
				if( ll.contains((Integer) ll.get(i)+1)){
					return false;
				}
				if( ll.get(i)-1>=amNum&&ll.contains((Integer) ll.get(i)-1)){
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * 是否满足场地规则
	 * 
	 * @param courseId
	 * @param classId
	 * @param day
	 * @param lesson
	 * @return
	 */
	public boolean isGroudRuleFullFill(String courseId, String classId,
			int day, int lesson,double arNum) {
		String gradeSynj = this.scheduleDatadic.getGradeSynjByClassId(classId);
		if (gradeSynj != null) {
			SchRuleGround grule = this.ruleGrounds.get(gradeSynj + "_"
					+ courseId);
			if (grule != null) {
				return !grule.isOverRuleGround(day, lesson,arNum);
			}
		}
		return true;
	}

	/**
	 * 教师课时分布是否平均
	 */
	public boolean isTeacherCourseTimeAvg(String teacherId,String courseId,int day ){
		if(teacherId!=null&&teacherDayNumMap.containsKey(teacherId) ){
			Integer max = teacherDayNumMap.get(teacherId).get(day);
			if(max!=null ){
//				if(max!=null&&!table.isRebuild()){
				int ar = 0;
				if(teacherDayArNumMap.containsKey(teacherId)&&teacherDayArNumMap.get(teacherId).containsKey(day)){
					ar = teacherDayArNumMap.get(teacherId).get(day);
				}
				if(ar+1>max){
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * 增加规则计数
	 * 
	 * @param classId
	 * @param courseId
	 * @param day
	 * @param lesson
	 * @param teacherIds
	 */
	public void addArrangedPosition(String classId, String courseId, int day,
			int lesson, List<String> teacherIds, boolean isNeedCourseNumControl,
			int courseType) {
		boolean hasArrangeMc = false;
		String mcGroupId = this.getMcGroupId(classId, courseId);
		if(mcGroupId!=null&&mcGroupId.trim().length()>0){
			String mkey = mcGroupId+","+day+","+lesson;
			if(mcGroupLessonKeyList.contains(mkey) ){
				hasArrangeMc = true;
			}else{
				mcGroupLessonKeyList.add(mkey);
			}
		}
		String gradeLev = this.scheduleDatadic.getGradeLevelByClassId(classId);
		String synj = this.scheduleDatadic.getGradeSynjByClassId(classId);
		SchRuleCourse ruleCourse = this.ruleCourses.get(gradeLev + "_"
				+ courseId);
		if (ruleCourse != null) {
			if (gradeLevelMaxAmNumMap.get(gradeLev) == null) {
				System.out.println("【课表微调】-填充教师失败，无年级上午最大数，使用年级为" + synj);
			} else if(!hasArrangeMc){
				ruleCourse.addArrangedPosition(day, lesson,
						gradeLevelMaxAmNumMap.get(gradeLev));
			}
		}
		//记录课程排课位置
		String cckey = classId + "_" + courseId;
		Map<Integer, List<Integer>> daymap = new HashMap<Integer, List<Integer>>();
		List<Integer> lessonlist = new ArrayList<Integer>();
		if (classCourseDayMap.containsKey(cckey)) {
			daymap = classCourseDayMap.get(cckey) ;
		} 
		if(daymap.containsKey(day)){
			lessonlist = daymap.get(day);
		}
		lessonlist.add(lesson);
		daymap.put(day, lessonlist);
		classCourseDayMap.put(cckey, daymap);
		//记录课程排课位置 结束
		int amNum  = 4;
		if(gradeLevelMaxAmNumMap.containsKey(gradeLev)){
			amNum = gradeLevelMaxAmNumMap.get(gradeLev);
		}
		if(lesson<=amNum-1){
			
			//记录课程上午排课位置
			List<String> lessonlistam = new ArrayList<String>();
			if (classCourseAmMap.containsKey(cckey)) {
				lessonlistam = classCourseAmMap.get(cckey) ;
			} 
			lessonlistam.add(day+","+lesson);
			classCourseAmMap.put(cckey, lessonlistam);
			//记录课程排课位置 结束
		}else{
			//记录课程下午排课位置
			List<String> lessonlistam = new ArrayList<String>();
			if (classCoursePmMap.containsKey(cckey)) {
				lessonlistam = classCoursePmMap.get(cckey) ;
			} 
			lessonlistam.add(day+","+lesson);
			classCoursePmMap.put(cckey, lessonlistam);
			//记录课程排课位置 结束
		}
		//记录教师排课位置
		if(teacherIds!=null&&!hasArrangeMc){
			for (String teacherId : teacherIds) {
				SchRuleTeacher ruleTeacher = this.ruleTeachers.get(teacherId);
				if (ruleTeacher != null) {
					ruleTeacher.addArrangedPosition(day);
				}
				this.addTeacherArrangePosMap(teacherId, day, lesson, courseType);
				this.addTeacherDayArNumMap(teacherId, day);
				Map<Integer,List<Integer>> teacherPosMap = new HashMap<Integer,List<Integer>>();
				if(lesson<=amNum-1){
					if(teacherAmPosMapCCkey.containsKey(teacherId)){
						teacherPosMap = teacherAmPosMapCCkey.get(teacherId);
					} 
					List<Integer> lessonList = new ArrayList<Integer>();
					if(teacherPosMap.containsKey(day )){
						lessonList = teacherPosMap.get(day);
					}
					if(!lessonList.contains((Integer) lesson)){
						lessonList.add(lesson);
					}
					teacherPosMap.put(day , lessonList);
					teacherAmPosMapCCkey.put(teacherId, teacherPosMap);
				}else{
					if(teacherPmPosMapCCkey.containsKey(teacherId)){
						teacherPosMap = teacherPmPosMapCCkey.get(teacherId);
					} 
					List<Integer> lessonList = new ArrayList<Integer>();
					if(teacherPosMap.containsKey(day )){
						lessonList = teacherPosMap.get(day);
					}
					if(!lessonList.contains((Integer) lesson)){
						lessonList.add(lesson);
					}
					teacherPosMap.put(day , lessonList);
					teacherPmPosMapCCkey.put(teacherId, teacherPosMap);
				}
			}
		}
		SchRuleGround ruleGround = this.ruleGrounds.get(synj + "_" + courseId);
		if (ruleGround != null&&!hasArrangeMc) {
			ruleGround.addArrangedPosition(day, lesson);
		}
	}

	/**
	 * 减少规则计数
	 * 
	 * @param classId
	 * @param courseId
	 * @param day
	 * @param lesson
	 * @param teacherIds
	 */
	public void removeArrangedPosition(String classId, String courseId,
			int day, int lesson, List<String> teacherIds,
			boolean isNeedCourseNumControl, int courseType) {
		String gradeLev = this.scheduleDatadic.getGradeLevelByClassId(classId);
		String synj = this.scheduleDatadic.getGradeSynjByClassId(classId);
		SchRuleCourse ruleCourse = this.ruleCourses.get(gradeLev + "_"
				+ courseId);
		String mcGroupId = this.getMcGroupId(classId, courseId);
		if(mcGroupId!=null&&mcGroupId.trim().length()>0){
			System.out.println("fdsss---------合班。。。");
//			return;
			String mkey = mcGroupId+","+day+","+lesson;
			if(mcGroupLessonKeyList.contains(mkey) ){
				mcGroupLessonKeyList.remove(mkey);
			} 
		}
		if (ruleCourse != null) {
			if (gradeLevelMaxAmNumMap.get(gradeLev) == null) {
				System.out.println("【课表微调】-填充教师失败，无年级上午最大数，使用年级为" + synj);
			} else {
				ruleCourse.removeArrangedPosition(day, lesson,
						gradeLevelMaxAmNumMap.get(gradeLev));
			}
		}
		
		String cckey = classId + "_" + courseId;
		if (classCourseDayMap.containsKey(cckey)
				&& classCourseDayMap.get(cckey).containsKey(day)
				&& classCourseDayMap.get(cckey).get(day).size()>0
				&& classCourseDayMap.get(cckey).get(day).contains(lesson)) {
			classCourseDayMap.get(cckey).get(day).remove((Integer) (lesson));
		}
		if(teacherIds!=null){
			
			for (String teacherId : teacherIds) {
				SchRuleTeacher ruleTeacher = this.ruleTeachers.get(teacherId);
				if (ruleTeacher != null) {
					ruleTeacher.removeArrangedPosition(day);
				}
				this.removeTeacherArrangePosMap(teacherId, day, lesson, courseType);
				this.removeTeacherDayArNumMap(teacherId, day);
				//移除教师位置控制
				if(teacherAmPosMapCCkey.containsKey(teacherId)){
					Map<Integer,List<Integer>> teacherPosMap   = teacherAmPosMapCCkey.get(teacherId);
					if(teacherPosMap.containsKey(day)){
						teacherPosMap.get(day).remove((Integer)lesson);
					}
				} 
				if(teacherPmPosMapCCkey.containsKey(teacherId)){
					Map<Integer,List<Integer>> teacherPosMap   = teacherPmPosMapCCkey.get(teacherId);
					if(teacherPosMap.containsKey(day)){
						teacherPosMap.get(day).remove((Integer)lesson);
					}
				} 
			}
		}
		int amNum  = 4;
		if(gradeLevelMaxAmNumMap.containsKey(gradeLev)){
			amNum = gradeLevelMaxAmNumMap.get(gradeLev);
		}
		if(lesson<=amNum-1){
			
			//记录课程上午排课位置
			List<String> lessonlistam = new ArrayList<String>();
			if (classCourseAmMap.containsKey(cckey)) {
				lessonlistam = classCourseAmMap.get(cckey) ;
			} 
			if(lessonlistam.contains(day+","+lesson)){
				lessonlistam.remove(day+","+lesson);
			}
			classCourseAmMap.put(cckey, lessonlistam);
			//记录课程排课位置 结束
		}else{
			//记录课程下午排课位置
			List<String> lessonlistam = new ArrayList<String>();
			if (classCoursePmMap.containsKey(cckey)) {
				lessonlistam = classCoursePmMap.get(cckey) ;
			} 
			if(lessonlistam.contains(day+","+lesson)){
				lessonlistam.remove(day+","+lesson);
			}
			classCoursePmMap.put(cckey, lessonlistam);
			//记录课程排课位置 结束
		}
		SchRuleGround ruleGround = this.ruleGrounds.get(synj + "_" + courseId);
		if (ruleGround != null) {
			ruleGround.removeArrangedPosition(day, lesson);
		}
	}

	/**
	 * 可否排课
	 * 
	 * @param task
	 * @param pday
	 * @param plesson
	 * @return
	 */
	public boolean canArrangePosition(ScheduleTask task, int day, int lesson,int courseType,ScheduleTable table,double arNum) {
		String courseId = task.getCourseId();
		String classId = task.getClassId();
		double taskNum = task.getTaskNum();
		List<String> teachers = task.getTeacherIds();
		GridPointGroup[][] classtb = table.getScheduleClassByClassId(task.getClassId()).getGridPointArr();
		if(classtb[day][lesson]!=null){
			return false;
		}
		if(!isCourseRuleFullFill(courseId , classId, day, lesson, taskNum , task.getMaxDays(), task.getSpNum(),arNum,table)){
			return false;
		}
		if(teachers!=null){
			
			for(String teacherId:teachers){
				if(isTeacherTimeConflict(teacherId, day, lesson,courseType)){
					return false;
				}
				if(!isTeacherRuleFullFill(teacherId, day, lesson, courseType,arNum, table,task.getClassAmNum())){
					return false;
				}
			}
		}
		if(!isGroudRuleFullFill(courseId, classId, day, lesson, arNum)){
			return false;
		}
		return true;
	}

	/**
	 * 班级课程对调
	 * 
	 * @param source
	 * @param target
	 * @param classId
	 * @return
	 */
	public boolean canSwapPosition(GridPointGroup source,
			GridPointGroup target, String classId,ScheduleTable table) {
		List<GridPoint> sgpList = new ArrayList<GridPoint>();
		List<GridPoint> tgpList = new ArrayList<GridPoint>();
		int day = source.getDay();
		int lesson = source.getLesson();
		int pday = target.getDay();
		int plesson = target.getLesson();
		
		for(GridPoint gpoint:source.getPointList()){
			sgpList.add(gpoint);
		}
		for(GridPoint gpoint:target.getPointList()){
			tgpList.add(gpoint);
		}
		
		//如果list中为1个课程 则走单课程交换认证
		if(sgpList.size()==1 ){
			return canSwapPosition(sgpList.get(0), target, sgpList.get(0).getDay(), sgpList.get(0).getLesson() , 
					 sgpList.get(0).getCourseType(), table, target.getDay());
		}
		if(tgpList.size()==1 ){
			return canSwapPosition(tgpList.get(0), source, tgpList.get(0).getDay(), tgpList.get(0).getLesson() , 
					tgpList.get(0).getCourseType(), table, source.getDay());
		}
		
		
		return false;
	}

	/**
	 * 按教师对调
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean canSwapPositionByTeacher(GridPoint source, GridPoint target) {
		return false;

	}

	/**
	 * 任务是否可以与已排课程交换排课
	 * @param task
	 * @param gp
	 * @param day
	 * @param lesson
	 * @param courseType
	 * @param table
	 * @return
	 */
	public boolean canSwapPosition(ScheduleTask task, GridPointGroup gp,
			int day, int lesson,int courseType,ScheduleTable table,int srcDay,boolean rebuild) {
		// TODO Auto-generated method stub
		List<GridPoint> gpList = new ArrayList<GridPoint>();
		
		List<GridPoint> copyList = new ArrayList<GridPoint>();
		for(GridPoint gpoint:gp.getPointList()){
			copyList.add(gpoint);
			gpList.add(gpoint);
		}
		for(GridPoint gpoint:copyList){
			table.removeGridPoint(gpoint);
		}
		double arNum = 1;
		for(GridPoint gpoint:gpList){
			if(isTeacherSame(task.getTeacherIds(),gpoint.getTeacherIds())&&srcDay==day){
				arNum = 2;
				break;
			}
		}
		boolean canArrangeSrc = this.canArrangePosition(task, gp.getDay(),gp.getLesson(), courseType, table,arNum);
		if(!canArrangeSrc){
			
			getFakeSwapBack(gpList,table );
			return false;
		}
		boolean arrange = true;
		for(GridPoint gpoint:gpList){
			boolean isMerge = false;
			String mcid = gpoint.getMcGroupId();
			if(mcid!=null&&mcid.trim().length()>0 ){
				isMerge = true;
			}
			if(isMerge||gpoint.isAdvance()||gpoint.isSpPoint()||gpoint.getCourseId().equalsIgnoreCase(task.getCourseId())){
				arrange = false;
				break;
			}
			boolean canArrangeTarget = this.canArrangeGpoint(gpoint, day, lesson, gpoint.getCourseType(),table);
			if(!canArrangeTarget){
				arrange = false;
				break;
			}
			if(day!=gp.getDay() ){
				//控制单班语数外科目课时平均
				ScheduleTask stask = table.getClassCourseTaskMap().get(gpoint.getClassId()+"_"+gpoint.getCourseId());
				if(stask.getTaskGroupId()==null&&stask.getTaskNum()>=stask.getMaxDays()){
					GridPointGroup[][] classtb = table.getClassIdScheduleMap().get(gpoint.getClassId()).getGridPointArr();
					if(!stask.hasClassOnDay(classtb , gp.getDay())){
						arrange = false;
						break;
					}
				}
			}
		}
		
		getFakeSwapBack(gpList,table );
		return arrange;
	}

	/**
	 * 任务是否可以与已排课程交换排课
	 * @param task
	 * @param gp
	 * @param day
	 * @param lesson
	 * @param courseType
	 * @param table
	 * @param rebuild --优化模式 部分条件宽松
	 * @return
	 */
	public boolean canSwapPosition(GridPoint point, GridPointGroup gp,
			int day, int lesson,int courseType,ScheduleTable table,int srcDay ) {
		// TODO Auto-generated method stub
		List<GridPoint> gpList = new ArrayList<GridPoint>();
		List<GridPoint> srcList = new ArrayList<GridPoint>();
		srcList.add(point);
		for(GridPoint gpoint:gp.getPointList()){
			gpList.add(gpoint);
		}
		for(GridPoint gpoint:gpList){
			table.removeGridPoint(gpoint);
		}
		table.removeGridPoint(point);
		double arNum = 1;
		for(GridPoint gpoint:gpList){
			if(isTeacherSame(point.getTeacherIds(),gpoint.getTeacherIds())&&srcDay==day){
				arNum = 2;
				break;
			}
		}
		boolean canArrangeSrc = this.canArrangeGpoint(point, gp.getDay(),gp.getLesson(), courseType, table);
		if(!canArrangeSrc){
			
			getFakeSwapBack(srcList,table );
			getFakeSwapBack(gpList, table);
			return false;
		}
		boolean arrange = true;
		for(GridPoint gpoint:gpList){
			boolean isMerge = false;
			String mcid = gpoint.getMcGroupId();
			if(mcid!=null&&mcid.trim().length()>0 ){
				isMerge = true;
			}
			if(gpoint.getCourseType()!=0||isMerge||gpoint.isAdvance()||gpoint.isSpPoint()||gpoint.getCourseId().equalsIgnoreCase(point.getCourseId())){
				arrange = false;
				break;
			}
			boolean srisMerge = false;
			String srmcid = gpoint.getMcGroupId();
			if(srmcid!=null&&srmcid.trim().length()>0 ){
				srisMerge = true;
			}
			if(point.getCourseType()!=0||srisMerge||point.isAdvance()||point.isSpPoint()||point.getCourseId().equalsIgnoreCase(gpoint.getCourseId())){
				arrange = false;
				break;
			}
			boolean canArrangeTarget = this.canArrangeGpoint(gpoint, day, lesson, gpoint.getCourseType(),table);
			if(!canArrangeTarget){
				arrange = false;
				break;
			}
			if(day!=gp.getDay() ){
				//控制单班语数外科目课时平均
				ScheduleTask stask = table.getClassCourseTaskMap().get(gpoint.getClassId()+"_"+gpoint.getCourseId());
				if(stask.getTaskGroupId()==null&&stask.getTaskNum()>=stask.getMaxDays()){
					GridPointGroup[][] classtb = table.getClassIdScheduleMap().get(gpoint.getClassId()).getGridPointArr();
					if(!stask.hasClassOnDay(classtb , gp.getDay())){
						arrange = false;
						break;
					}
				}
			}
		}
		if(day!=gp.getDay() ){
			//控制单班语数外科目课时平均
			ScheduleTask stask = table.getClassCourseTaskMap().get(point.getClassId()+"_"+point.getCourseId());
			if(stask.getTaskGroupId()==null&&stask.getTaskNum()>=stask.getMaxDays()){
				GridPointGroup[][] classtb = table.getClassIdScheduleMap().get(point.getClassId()).getGridPointArr();
				if(!stask.hasClassOnDay(classtb , gp.getDay())){
					arrange = false;
				}
			}
		}
		getFakeSwapBack(gpList,table );
		getFakeSwapBack(srcList,table );
		return arrange;
	}
	private boolean isTeacherSame(List<String> teacherIds,
			List<String> teacherIds2) {
		// TODO Auto-generated method stub
		if(teacherIds==null||teacherIds2==null){
			return false;
		}
		for(String tid:teacherIds){
			if(!teacherIds2.contains(tid)){
				return false;
			}
		}
		for(String tid:teacherIds2){
			if(!teacherIds.contains(tid)){
				return false;
			}
		}
		return true;
	}

	/**
	 * 任务是否可以与课程交换
	 * @param gpoint
	 * @param day
	 * @param lesson
	 * @param courseType
	 * @param table
	 * @param rebuild--优化模式 部分条件宽松
	 * @return
	 */
	public boolean canArrangeGpoint(GridPoint gpoint, int day, int lesson,
			int courseType, ScheduleTable table) {
		// TODO Auto-generated method stub
		String courseId = gpoint.getCourseId();
		String classId = gpoint.getClassId();
		ScheduleTask task = table.getClassCourseTaskMap().get(classId+"_"+courseId);
		double taskNum = task.getTaskNum();
		List<String> teachers = gpoint.getTeacherIds();
		if(!isCourseRuleFullFill(courseId , classId, day, lesson, taskNum , task.getMaxDays(), task.getSpNum(),1,table)){
			
			return false;
		}
		if(teachers!=null){
			
			for(String teacherId:teachers){
				
				if(isTeacherTimeConflict(teacherId, day, lesson,courseType)){
					return false;
				}
				if(!isTeacherRuleFullFill(teacherId, day, lesson, courseType, 1, table,task.getClassAmNum())){
					return false;
				}
			}
		}
		if(!isGroudRuleFullFill(courseId, classId, day, lesson, 1)){
			return false;
		}
		if(task.getTaskGroupId()!=null){
			String glvl = scheduleDatadic.getGradeLevelByClassId(task.getClassId());
			if(glvl==null||glvl.trim().length()==0){
				return true;
			}
			//如果勾选了不控制教学进度
			if( isTeachingSync == -1 ){
				return true;
			}
			ScheduleTaskGroup taskGroup = table.getScheduleTaskGroupById(task.getTaskGroupId());
			if(taskGroup!=null){
//				if( !table.isRebuild()){
					//控制尽量当天一轮模式
					boolean hasCourseInLc = getHasCourseInLc(table,taskGroup,classId,courseId, day, lesson);
					if(hasCourseInLc){	
						return false;
					}
//				}else{
					//纯计算位置模式
//					boolean isArrSync = isArrSyncRebuild(table,taskGroup,classId,courseId,day,lesson);
//				}
			}
		}
		return true;
	}

	/**
	 * 重构进度是否满足
	 * @param table
	 * @param taskGroup
	 * @param classId
	 * @param courseId
	 * @param day
	 * @param lesson
	 * @return
	 */
	private boolean isArrSyncRebuild(ScheduleTable table,
			ScheduleTaskGroup taskGroup, String classId, String courseId,
			int day, int lesson) {
		// TODO Auto-generated method stub
		
		return false;
	}

	/**
	 * 当前轮次是否已有课程安排 用于教学进度控制
	 * @param table
	 * @param taskGroup
	 * @param classId
	 * @param courseId
	 * @param day
	 * @param lesson
	 * @return
	 */
	private boolean getHasCourseInLc(ScheduleTable table,
			ScheduleTaskGroup taskGroup, String classId, String courseId,
			int day, int lesson) {
		// TODO Auto-generated method stub
		int lc = taskGroup.getLcByPosition(day, lesson);
		GridPointGroup[][] classtable = table.getScheduleClassByClassId(classId).getGridPointArr();
		List<String> poss = taskGroup.getLcPositionsMap().get(lc);
		if(poss==null){
			return false;
		}
		for(String ps:poss){
			int tday = Integer.parseInt(ps.split(",")[0]);
			int tlesson = Integer.parseInt(ps.split(",")[1]);
			GridPointGroup gp = classtable[tday][tlesson];
			if(gp!=null){
				List<GridPoint> gpList = gp.getPointList();
				for(GridPoint point:gpList){
					if(point.getClassId().equalsIgnoreCase(classId)&&point.getCourseId().equalsIgnoreCase(courseId)){
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 还原预交换排课的已排课程
	 * @param gpList
	 * @param table
	 */
	public void getFakeSwapBack(List<GridPoint> gpList, ScheduleTable table) {
		// TODO Auto-generated method stub
		for(GridPoint gp:gpList){
			table.addGridPoint(gp);
		}
	}

	public int getIsTeachingSync() {
		return isTeachingSync;
	}

	public void setIsTeachingSync(int isTeachingSync) {
		this.isTeachingSync = isTeachingSync;
	}

	public int getLessonDistrubute() {
		return lessonDistrubute;
	}

	public void setLessonDistrubute(int lessonDistrubute) {
		this.lessonDistrubute = lessonDistrubute;
	}

	public int getIsTryFinish() {
		return isTryFinish;
	}

	public void setIsTryFinish(int isTryFinish) {
		this.isTryFinish = isTryFinish;
	}

	public Map<String, List<String>> getClassCourseAmMap() {
		return classCourseAmMap;
	}

	public void setClassCourseAmMap(Map<String, List<String>> classCourseAmMap) {
		this.classCourseAmMap = classCourseAmMap;
	}

	public Map<String, List<String>> getClassCoursePmMap() {
		return classCoursePmMap;
	}

	public void setClassCoursePmMap(Map<String, List<String>> classCoursePmMap) {
		this.classCoursePmMap = classCoursePmMap;
	}


	public int getTeaSpNum() {
		return teaSpNum;
	}

	public void setTeaSpNum(int teaSpNum) {
		this.teaSpNum = teaSpNum;
	}

	public Map<String, Map<Integer, List<Integer>>> getTeacherAmPosMapCCkey() {
		return teacherAmPosMapCCkey;
	}

	public void setTeacherAmPosMapCCkey(
			Map<String, Map<Integer, List<Integer>>> teacherAmPosMapCCkey) {
		this.teacherAmPosMapCCkey = teacherAmPosMapCCkey;
	}

	public Map<String, Map<Integer, List<Integer>>> getTeacherPmPosMapCCkey() {
		return teacherPmPosMapCCkey;
	}

	public void setTeacherPmPosMapCCkey(
			Map<String, Map<Integer, List<Integer>>> teacherPmPosMapCCkey) {
		this.teacherPmPosMapCCkey = teacherPmPosMapCCkey;
	}

	 
	

	
}
