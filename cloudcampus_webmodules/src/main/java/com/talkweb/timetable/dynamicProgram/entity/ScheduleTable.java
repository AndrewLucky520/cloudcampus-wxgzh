
package com.talkweb.timetable.dynamicProgram.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.timetable.dynamicProgram.core.DynamicPorcessProgressProc;
import com.talkweb.timetable.dynamicProgram.core.GridPointLessonCompare;
import com.talkweb.timetable.dynamicProgram.enums.CourseLevel;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleClassGroup;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleCourse;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleTeacher;
import com.talkweb.timetable.dynamicProgram.rule.ScheduleRule;

public class ScheduleTable implements Serializable {

	/**
	 * 版本id
	 */
	private static final long serialVersionUID = -6033901525395929097L;

	private String schoolId;

	private String timetableId;
	
	private int maxDays;

	private boolean halfAtLastDay = false;
	
	private String processId;
	
	private List<String> errorInfos = new ArrayList<String>();

	private HashMap<String, ScheduleClass> classIdScheduleMap = new HashMap<String, ScheduleClass>();

	private HashMap<Integer, HashMap<Integer, GridPointGroup>> groupPointMap = new HashMap<Integer, HashMap<Integer, GridPointGroup>>();

	private List<GridPoint> finishLessons = new ArrayList<GridPoint>();
	
	private HashMap<String,List<GridPoint>> finishGroupPoints = new HashMap<String, List<GridPoint>>();
	
	/**
	 * 教师已完成
	 */
	private HashMap<String,List<GridPoint>> finishTeacherLessons = new HashMap<String,List<GridPoint>>();

	
	private List<ScheduleTaskGroup> unfinishTaskGroups = new ArrayList<ScheduleTaskGroup>();
	/**
	 * 已完成
	 */
	private HashMap<String,ScheduleTaskGroup> finishTaskGroupMap = new HashMap<String, ScheduleTaskGroup>();
	/**
	 * 所有
	 */
	private HashMap<String,ScheduleTaskGroup> allTaskGroupMap = new HashMap<String, ScheduleTaskGroup>();

	/**
	 * class_course--task
	 */
	private HashMap<String,ScheduleTask> classCourseTaskMap = new HashMap<String, ScheduleTask>();
	/**
	 * 教师map
	 */
	private HashMap<String,List<String>> classTaskTeacherMap = new HashMap<String, List<String>>();
	
	private List<ScheduleTask> unfinishTasks = new ArrayList<ScheduleTask>();

	private ScheduleRule scheduleRule;
	
	private ScheduleDatadic scheduleDatadic;
	
	private double programScore;
	
	private double programProgress;
	
	private int retryTimes = 5 ;
	
	private boolean rebuild = false;
	
	private double totalPreNum = 0;
	//深度交换 耗时较长
	private boolean deepSwap = false;
	
	private GridPointLessonCompare gpLessonCompare = new  GridPointLessonCompare();
	// 单双周课程map map1存单周映射 map2存双周映射
	private JSONObject SingleDoubleCourseMap = new JSONObject();
	
	/**
	 * 已排课眼-教师为键
	 * @return
	 */
	public HashMap<String, List<GridPoint>> getFinishTeacherLessons() {
		return finishTeacherLessons;
	}

	public void setFinishTeacherLessons(
			HashMap<String, List<GridPoint>> finishTeacherLessons) {
		this.finishTeacherLessons = finishTeacherLessons;
	}
	/**
	 * 课表是否编排完成
	 * @return
	 */
	public boolean isFinished() {
		return isTaskGroupsFinished() && isTasksFinished();
	}

	public boolean isTaskGroupsFinished() {
		return this.unfinishTaskGroups.size() == 0;
	}

	public boolean isTasksFinished() {
		return this.unfinishTasks.size() == 0;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getTimetableId() {
		return timetableId;
	}

	public void setTimetableId(String timetableId) {
		this.timetableId = timetableId;
	}

	public int getMaxDays() {
		return maxDays;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public void setMaxDays(int maxDays) {
		this.maxDays = maxDays;
	}

	public boolean isHalfAtLastDay() {
		return halfAtLastDay;
	}

	public void setHalfAtLastDay(boolean halfAtLastDay) {
		this.halfAtLastDay = halfAtLastDay;
	}

	public List<String> getErrorInfos() {
		
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		for(ScheduleTaskGroup unfitg:unfinishTaskGroups){
			List<ScheduleTask> tasks = unfitg.getChildTasks();
			for(ScheduleTask task:tasks){
				if(!task.isFinish()){
					// 高二:2016985:1404:英语
					String gName = njName.get( scheduleDatadic.getGradeBySynj(task.getGradeId()).getCurrentLevel());
					String className = scheduleDatadic.getClassNameById(task.getClassId());
					errorInfos.add(gName+":"+task.getClassId()+":"+className+":"+task.getCourseId()+":"+task.getGradeLevel());
				}
			}
		}
		for(ScheduleTask task:unfinishTasks){
			if(!task.isFinish()){
				String gName = njName.get( scheduleDatadic.getGradeBySynj(task.getGradeId()).getCurrentLevel());
				String className = scheduleDatadic.getClassNameById(task.getClassId());
				errorInfos.add(gName+":"+task.getClassId()+":"+className+":"+task.getCourseId()+":"+task.getGradeLevel());
			}
		}
		return errorInfos;
	}

	public void setErrorInfos(List<String> errorInfos) {
		this.errorInfos = errorInfos;
	}

	public HashMap<String, ScheduleClass> getClassIdScheduleMap() {
		return classIdScheduleMap;
	}
	
	public ScheduleClass getScheduleClassByClassId(String classId){
		
		return classIdScheduleMap.get(classId);
	}

	public void setClassIdScheduleMap(
			HashMap<String, ScheduleClass> classIdScheduleMap) {
		this.classIdScheduleMap = classIdScheduleMap;
	}

	public void addClassIdScheduleMap(ScheduleClass scheduleClass){
		classIdScheduleMap.put(scheduleClass.getClassId(), scheduleClass);
	}
	public HashMap<Integer, HashMap<Integer, GridPointGroup>> getGroupPointMap() {
		return groupPointMap;
	}

	public void setGroupPointMap(
			HashMap<Integer, HashMap<Integer, GridPointGroup>> groupPointMap) {
		this.groupPointMap = groupPointMap;
	}

	public List<GridPoint> getFinishLessons() {
		return finishLessons;
	}

	public void setFinishLessons(List<GridPoint> finishLessons) {
		this.finishLessons = finishLessons;
	}

	public HashMap<String, List<GridPoint>> getFinishGroupPoints() {
		return finishGroupPoints;
	}

	public void setFinishGroupPoints(
			HashMap<String, List<GridPoint>> finishGroupPoints) {
		this.finishGroupPoints = finishGroupPoints;
	}

	public List<ScheduleTaskGroup> getUnfinishTaskGroups() {
		return unfinishTaskGroups;
	}

	public void setUnfinishTaskGroups(List<ScheduleTaskGroup> unfinishTaskGroups) {
		if(this.getAllTaskGroupMap().size()==0){
			for(ScheduleTaskGroup stg:unfinishTaskGroups){
				allTaskGroupMap.put(stg.getTaskGroupId(), stg);
			}
		}
		this.unfinishTaskGroups = unfinishTaskGroups;
	}
	public ScheduleTaskGroup getScheduleTaskGroupById(String taskGroupId){
		
		return this.allTaskGroupMap.get(taskGroupId);
	}
	public List<ScheduleTask> getUnfinishTasks() {
		return unfinishTasks;
	}

	public HashMap<String, List<String>> getClassTaskTeacherMap() {
		return classTaskTeacherMap;
	}

	public void setClassTaskTeacherMap(
			HashMap<String, List<String>> classTaskTeacherMap) {
		this.classTaskTeacherMap = classTaskTeacherMap;
	}

	public void setUnfinishTasks(List<ScheduleTask> unfinishTasks) {
		this.unfinishTasks = unfinishTasks;
	}

	public ScheduleRule getScheduleRule() {
		return scheduleRule;
	}

	public void setScheduleRule(ScheduleRule scheduleRule) {
		this.scheduleRule = scheduleRule;
	}

	public ScheduleDatadic getScheduleDatadic() {
		return scheduleDatadic;
	}

	public void setScheduleDatadic(ScheduleDatadic scheduleDatadic) {
		this.scheduleDatadic = scheduleDatadic;
	}

	public double getProgramScore() {
		return programScore;
	}

	public void setProgramScore(double programScore) {
		this.programScore = programScore;
	}

	public double getProgramProgress() {
		double allTaskNum = 0;
		double finishNum =0;
		for(ScheduleClass schclass: classIdScheduleMap.values()){
			allTaskNum+= schclass.getClassTaskNum();
			finishNum += schclass.getHasArrangedNum();
		}
		finishNum = finishNum-this.getTotalPreNum();
		return finishNum/allTaskNum;
	}

	public void setProgramProgress(double programProgress) {
		this.programProgress = programProgress;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public HashMap<String, ScheduleTaskGroup> getFinishTaskGroupMap() {
		return finishTaskGroupMap;
	}

	public void setFinishTaskGroupMap(
			HashMap<String, ScheduleTaskGroup> finishTaskGroupMap) {
		this.finishTaskGroupMap = finishTaskGroupMap;
	}

	public HashMap<String, ScheduleTask> getClassCourseTaskMap() {
		return classCourseTaskMap;
	}

	public void setClassCourseTaskMap(
			HashMap<String, ScheduleTask> classCourseTaskMap,ScheduleRule schRule) {
		this.classCourseTaskMap = classCourseTaskMap;
		
		for(ScheduleTask task:classCourseTaskMap.values() ){
			ScheduleClass schClass = this.getScheduleClassByClassId(task.getClassId());
			schClass.addInitTask(task);
		}
		List<String> commonposs = new ArrayList<String>();
		HashMap<String,Integer> posNumMap = new HashMap<String, Integer>();
		//课程不排位置规则
		List<SchRuleCourse> rcs = new ArrayList<SchRuleCourse>();
		rcs.addAll( schRule.getRuleCourses().values());
		for(SchRuleCourse rc:rcs){
			if(rc!=null&&rc.getPositions()!=null){
				for(String pos:rc.getPositions().keySet()){
					if(posNumMap.containsKey(pos)){
						posNumMap.put(pos, posNumMap.get(pos)+1);
					}else{
						posNumMap.put(pos,1);
					}
				}
			}
		}
		//教师规则
		Collection<SchRuleTeacher> rts = schRule.getRuleTeachers().values();
		for(SchRuleTeacher rc:rts){
			if(rc!=null&&rc.getPositions()!=null){
				for(String pos:rc.getPositions().keySet()){
					if(posNumMap.containsKey(pos)){
						posNumMap.put(pos, posNumMap.get(pos)+1);
					}else{
						posNumMap.put(pos,1);
					}
				}
			}
		}
		//教研活动
		Collection<List<String>> rgs = schRule.getRuleResearchMeetings().values();
		for(List<String> rg:rgs){
			if(rg!=null&&rg.size()>0){
				for(String pos:rg){
					if(posNumMap.containsKey(pos)){
						posNumMap.put(pos, posNumMap.get(pos)+1);
					}else{
						posNumMap.put(pos,1);
					}
				}
			}
		}
		for(String pos:posNumMap.keySet()){
			if(pos!=null&&posNumMap.get(pos)!=null){
				if(posNumMap.get(pos)>=4){
					commonposs.add(pos);
				}
			}
		}
		for( ScheduleClass schClass:classIdScheduleMap.values()){
			schClass.init(schRule,commonposs, this);
		}
	}

	public HashMap<String, ScheduleTaskGroup> getAllTaskGroupMap() {
		return allTaskGroupMap;
	}

	public void setAllTaskGroupMap(
			HashMap<String, ScheduleTaskGroup> allTaskGroupMap) {
		this.allTaskGroupMap = allTaskGroupMap;
	}

	public void init( ){
		
	}
	
	public Object deepClone() throws IOException, ClassNotFoundException {
		// 序列化
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);

		oos.writeObject(this);

		// 反序列化
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);

		return ois.readObject();
	}
	/**
	 * 安排预排、单双周、合班
	 * @param advanceArrangeList
	 * @param courseGroup
	 * @param classTaskTeacherMap 班级、课程--教课老师
	 */
	public void preArrange(List<JSONObject> advanceArrangeList,
			JSONObject courseGroup, HashMap<String,List<String>> classTaskTeacherMap ) {
		// TODO Auto-generated method stub
		if (advanceArrangeList == null) {
			return;
		}
		// 预排科目
		List<GridPoint> fixedCourses = new ArrayList<GridPoint>();
		preArrangeFixedCourse(advanceArrangeList, classTaskTeacherMap,
				fixedCourses);
		//合班课
		preArrangeMergeClassCourse();
	}
	
	/**
	 * 单双周课程
	 * @param courseGroup
	 */
	public void preArrangeSdWeekCourse(JSONObject courseGroup) {
		// TODO Auto-generated method stub
		HashMap<String,String> map1 = (HashMap<String, String>) courseGroup.get("map1");
		int times = 0;
		for(Iterator<String> it = map1.keySet().iterator();it.hasNext();){
			String ckey = it.next();
			times++;
			String course2Id = map1.get(ckey);
			
			ScheduleTask task = classCourseTaskMap.get(ckey);
			if(task==null){
				continue;
			}
			int amnum = task.getClassAmNum();
			int pmNum = task.getClassPmNum();
			ScheduleTask dtask = classCourseTaskMap.get(task.getClassId()+"_"+course2Id);
			if(task!=null&&dtask!=null&&!task.isFinish()&&!dtask.isFinish()){
				List<int[]> canArrangePos = new ArrayList<int[]>();
				for(int i=0;i<maxDays;i++){
					for(int j=amnum;j<amnum+pmNum;j++){
						if(scheduleRule.canArrangePosition(task, i, j,1,this,0.5)&&
								scheduleRule.canArrangePosition(dtask, i, j,2,this,0.5)){
							int[] ps = new int[2];
							ps[0] = i;ps[1] = j;
							canArrangePos.add(ps);
						}
					}
				}
				if(canArrangePos.size()==0){
					for(int i=0;i<maxDays;i++){
						for(int j=0;j<amnum ;j++){
							if(scheduleRule.canArrangePosition(task, i, j,1,this,0.5)&&
									scheduleRule.canArrangePosition(dtask, i, j,2,this,0.5)){
								int[] ps = new int[2];
								ps[0] = i;ps[1] = j;
								canArrangePos.add(ps);
							}
						}
					}
				}
				if(canArrangePos.size()==0){
					continue;
				}
				int[] selected = canArrangePos.get((int)(Math.random()*canArrangePos.size()));
				int day = selected[0];
				int lesson = selected[1];
				//单周课
				GridPoint gridPoint = new GridPoint();
				gridPoint.setAdvance(false);
				gridPoint.setArrangedNum(0.5);
				gridPoint.setClassId(task.getClassId());
				gridPoint.setCourseId(task.getCourseId());
				gridPoint.setCourseType(1);
				gridPoint.setDay(day);
				
				gridPoint.setGradeId(task.getGradeId());
				gridPoint.setGradeLevel(task.getGradeLevel());
				gridPoint.setLesson(lesson);
				gridPoint.setNeedCourseNumControl(false);
				gridPoint.setTaskGroupId(task.getTaskGroupId());
				List<String> teacherIds = classTaskTeacherMap.get(ckey);
				gridPoint.setTeacherIds(teacherIds );
				
				this.addGridPoint(gridPoint);
				
				//单周课
				GridPoint dgridPoint = new GridPoint();
				dgridPoint.setAdvance(false);
				dgridPoint.setArrangedNum(0.5);
				dgridPoint.setClassId(task.getClassId());
				dgridPoint.setCourseId(dtask.getCourseId());
				dgridPoint.setCourseType(2);
				dgridPoint.setDay(day);
				
				dgridPoint.setGradeId(task.getGradeId());
				dgridPoint.setGradeLevel(task.getGradeLevel());
				dgridPoint.setLesson(lesson);
				dgridPoint.setNeedCourseNumControl(false);
				dgridPoint.setTaskGroupId(dtask.getTaskGroupId());
				List<String> dteacherIds = classTaskTeacherMap.get(dtask.getClassId()+"_"+dtask.getCourseId());
				dgridPoint.setTeacherIds(dteacherIds );
				
				this.addGridPoint(dgridPoint);
				task.setArrangedTaskNum(task.getArrangedTaskNum()+0.5);
				dtask.setArrangedTaskNum(dtask.getArrangedTaskNum()+0.5);
			}
			
		}
		
		System.out.println("LLLLLLLLLLLLLLLLLLLLLLL:"+times);
	}

	private void preArrangeMergeClassCourse() {
		
		HashMap<String, SchRuleClassGroup> classgroups = scheduleRule.getRuleClassGroupsKeyMap();
		
		for(Iterator<String> mkey = classgroups.keySet().iterator();mkey.hasNext();){
			String mcgId = mkey.next();
			SchRuleClassGroup classGroup = classgroups.get(mcgId);
			String courseId = classGroup.getCourseId();
			if(classGroup!=null&&classGroup.getClassIds()!=null&&classGroup.getClassIds().size()>0){
				String scid = classGroup.getClassIds().get(0);
				ArrayList<String> poss = new ArrayList<String>();
				ScheduleClass schClass = this.getScheduleClassByClassId(scid);
				int i =0;
				while (schClass==null&&i<classGroup.getClassIds().size()){
					schClass = getScheduleClassByClassId(classGroup.getClassIds().get(i));
					i++;
				}
				if(schClass==null){
					continue;
				}
				GridPointGroup[][] classtable = schClass.getGridPointArr();
				for(int day=0;day<classtable.length;day++){
					for(int lesson=0;lesson<classtable[day].length;lesson++){
						if(schClass.isHalfAtLast()&&day==maxDays-1){
							if(lesson>=schClass.getAmNum()){
								continue;
							}
						}
						poss.add(day+","+lesson);
					}
				}
				
				for(String classId :classGroup.getClassIds()){
					String cckey = classId+"_"+courseId;
					ScheduleTask schTask = this.getClassCourseTaskMap().get(cckey);
					if(schTask!=null  ){
						schTask.setMergeClassCourse(true);
						if(schTask.isFinish()){
							continue;
						}
						double taskNum = schTask.getTaskNum();
						int index = (int) Math.ceil(taskNum);
						double arNum = 1;
						if(index>taskNum){
							arNum = 0.5;
						}
						int rets = 0;
						while(index>0&&rets<=retryTimes*10){
							rets++;
							if(schTask.getSpNum()-schTask.getHasSpNum()>0){
								arNum = 2;
							}
							
							boolean rs = arrangeMergeClassChild(poss,classGroup.getClassIds(),courseId,schTask.getCourseLevel(),schTask.getClassAmNum(),arNum,mcgId);
							if(rs){
								if(arNum==2){
									index -- ;
								}
								arNum = 1;
								index --;
							}
						}
						
					}
				}
				
			}
		}
		
		return;
		// TODO Auto-generated method stub
//		for(ScheduleTaskGroup tg:unfinishTaskGroups){
//			String courseId = tg.getCourseId();
//			
//			List<ScheduleTask> children = tg.getChildTasks();
//			
//			List<String> arrangedMCIds = new ArrayList<String>();
//			for(ScheduleTask child:children){
//				if(child.isFinish()){
//					continue;
//				}
//				String classId = child.getClassId();
//				if(scheduleRule.isMergeCourse(classId, courseId)){
//					
//					child.setMergeClassCourse(true); 
//					String mcid = scheduleRule.getMcGroupId(classId, courseId);
//					if(!arrangedMCIds.contains(mcid)){
//						List<String> classes = scheduleRule.getMergeClass(classId, courseId);
//						double taskNum = child.getTaskNum();
//						int index = (int) Math.ceil(taskNum);
//						double arNum = 1;
//						if(index>taskNum){
//							arNum = 0.5;
//						}
//						int rets = 0;
//						while(index>0&&rets<=retryTimes){
//							rets++;
//							if(child.getSpNum()-child.getHasSpNum()>0){
//								arNum = 2;
//							}
//							List<String> poss = tg.getLcPositionsMap().get(index);
//							if(poss==null){
//								poss = new ArrayList<String>();
//								ScheduleClass schClass = this.getScheduleClassByClassId(classId);
//								if(schClass==null){
//									continue;
//								}
//								GridPointGroup[][] classtable = schClass.getGridPointArr();
//								for(int day=0;day<classtable.length;day++){
//									for(int lesson=0;lesson<classtable[day].length;lesson++){
//										if(schClass.isHalfAtLast()&&day==maxDays-1){
//											if(lesson>=schClass.getAmNum()){
//												continue;
//											}
//										}
//										poss.add(day+","+lesson);
//									}
//								}
//							}
//							boolean rs = arrangeMergeClassChild(poss,classes,courseId,tg.getCourseLevel(),tg.getClassAmNum(),arNum,mcid);
//							if(rs){
//								if(arNum==2){
//									index -- ;
//								}
//								arNum = 1;
//								index --;
//							}
//						}
//						
//						arrangedMCIds.add(mcid);
//					}
//				}
//			}
//		}
//		for(ScheduleTask  child:unfinishTasks){
//			String courseId = child.getCourseId();
//			
//			
//				if(child.isFinish()){
//					continue;
//				}
//				String classId = child.getClassId();
//				if(scheduleRule.isMergeCourse(classId, courseId)){
//					
//					child.setMergeClassCourse(true); 
//					String mcid = scheduleRule.getMcGroupId(classId, courseId);
//					List<String> classes = scheduleRule.getMergeClass(classId, courseId);
//					double taskNum = child.getTaskNum();
//					int index = (int) Math.ceil(taskNum);
//					double arNum = 1;
//					if(index>taskNum){
//						arNum = 0.5;
//					}
//					int rets = 0;
//					while(index>0&&rets<=retryTimes){
//						rets++;
//						if(child.getSpNum()-child.getHasSpNum()>0){
//							arNum = 2;
//						}
//						List<String> poss = null;
//						if(poss==null){
//							poss = new ArrayList<String>();
//							ScheduleClass schClass = this.getScheduleClassByClassId(classId);
//							if(schClass==null){
//								continue;
//							}
//							GridPointGroup[][] classtable = schClass.getGridPointArr();
//							for(int day=0;day<classtable.length;day++){
//								for(int lesson=0;lesson<classtable[day].length;lesson++){
//									if(schClass.isHalfAtLast()&&day==maxDays-1){
//										if(lesson>=schClass.getAmNum()){
//											continue;
//										}
//									}
//									poss.add(day+","+lesson);
//								}
//							}
//						}
//						boolean rs = arrangeMergeClassChild(poss,classes,courseId,child.getCourseLevel(),child.getClassAmNum(),arNum,mcid);
//						if(rs){
//							if(arNum==2){
//								index -- ;
//							}
//							arNum = 1;
//							index --;
//						}
//						
//				}
//			}
//		}
	}

	private boolean arrangeMergeClassChild(List<String> poss,
			List<String> classes, String courseId, CourseLevel courseLevel, 
			int amNum, double arNum, String mcid) {
		// TODO Auto-generated method stub
		List<int[]> canArrangePos  = new ArrayList<int[]>();
		List<int[]> bestArrangePos  = new ArrayList<int[]>();
		for(String pos:poss ){
			int day = Integer.parseInt(pos.split(",")[0]);
			int lesson = Integer.parseInt(pos.split(",")[1]);
			boolean best  = true;
			if(courseLevel.equals(CourseLevel.AmFirst)&&lesson>=amNum){
				best = false;
			}
			if(courseLevel.equals(CourseLevel.PmFirst)&&lesson<amNum){
				best = false;
			}
			boolean allcan = true;
			for(String classId:classes){
				
				ScheduleTask task = this.classCourseTaskMap.get(classId+"_"+courseId);
				if(task==null ){
					continue;
				}
				if(task.isFinish()){
					allcan = false;
				}
				ScheduleClass schClass = this.getScheduleClassByClassId(task.getClassId());
				GridPointGroup[][] classtable = schClass.getGridPointArr();
				if(classtable[day][lesson]!=null ){
					boolean isMerge = false;
					GridPointGroup gp = classtable[day][lesson];
					if(gp.getPointList()!=null){
						for(GridPoint gpt:gp.getPointList()){
							
							if(gpt.getMcGroupId()!=null&&gpt.getMcGroupId().trim().length()>0){
								isMerge = true;
								break;
							}
						}
					}
					if(isMerge){
						allcan = false;
						break;
					}
					if(task.getTeacherIds()!=null&&task.getTeacherIds().size()>0){
						for(String teacherId:task.getTeacherIds()){
							int ctype = 0;
							if(arNum>=1){
								ctype = 0;
							}
							boolean isconf =  scheduleRule.isTeacherTimeConflict(teacherId, day, lesson, ctype);
							if(isconf){
								allcan = false;
								break;
							}
						}
					}
				}
				
				if(!allcan){
					continue;
				}
				
			}
			for(String classId:classes){
				
				ScheduleTask task = this.classCourseTaskMap.get(classId+"_"+courseId);
				if(task==null ){
					continue;
				}
				if(task.isFinish()){
					allcan = false;
				}
				boolean t = scheduleRule.canArrangePosition(task , day, lesson,0,this,arNum);
				if(!t){
					allcan = false;	
					break;
				}
			}
			if(!allcan){
				continue;
			}
			int[] ipos = new int[]{day,lesson};
			if(best){
				bestArrangePos.add(ipos);
			}else{
				canArrangePos.add(ipos);
			}
		}
		if(canArrangePos.size()==0){
			return false;
		}
		boolean rs = false;
		if(bestArrangePos.size()>0){
			 rs = insertMergePoss(classes, courseId, amNum, arNum, mcid,
						bestArrangePos);
		}else{
			rs = insertMergePoss(classes, courseId, amNum, arNum, mcid,
					canArrangePos);
		}
		
		
		return rs;
		
		
	}

	private boolean insertMergePoss(List<String> classes, String courseId,
			int amNum, double arNum, String mcid, List<int[]> canArrangePos) {
		boolean rs = false;
		int retryT = 0;
		while(retryT<30){
			int[] selected = canArrangePos.get((int)(Math.random()*canArrangePos.size()));
			int day = selected[0];
			int lesson = selected[1];
			if(arNum==2){
				int[][] spPosition = getSpPositions(canArrangePos, amNum, classes.get(0));
				for(String classId:classes){
					ScheduleClass schClass = this.getScheduleClassByClassId(classId);
					if(schClass==null){
						continue;
					}
					ScheduleTask task = this.classCourseTaskMap.get(classId+"_"+courseId);
					if(task.isFinish()){
						continue;
					}
					int spday = 0;
					if(spPosition!=null&&spPosition.length>0){
						for(int i=0;i<spPosition.length;i++){
							int[] pos = spPosition[i];
							int sday = pos[0];
							spday = sday;
							int slesson = pos[1];
							GridPoint gridPoint = new GridPoint();
							gridPoint.setAdvance(false);
							gridPoint.setArrangedNum(1);
							gridPoint.setClassId(task.getClassId());
							gridPoint.setCourseId(task.getCourseId());
							gridPoint.setCourseType(0);
							gridPoint.setDay(sday);
							gridPoint.setGradeId(task.getGradeId());
							gridPoint.setGradeLevel(task.getGradeLevel());
							gridPoint.setLesson(slesson);
							gridPoint.setNeedCourseNumControl(false);
							gridPoint.setTaskGroupId(task.getTaskGroupId());
							gridPoint.setMcGroupId(mcid);
							gridPoint.setSpPoint(true);
							List<String> teacherIds = classTaskTeacherMap.get(task.getClassId()+"_"+task.getCourseId());
							gridPoint.setTeacherIds(teacherIds );
							this.addGridPoint(gridPoint);
						}
						schClass .addSpNumOnDay(spday);
					}

					
					//..
					task.setArrangedTaskNum(task.getArrangedTaskNum()+arNum);
					task.setHasSpNum(task.getHasSpNum()+1);
				}	
			}else{
				for(String classId:classes){
					ScheduleClass schClass = this.getScheduleClassByClassId(classId);
					if(schClass==null){
						continue;
					}
					ScheduleTask task = this.classCourseTaskMap.get(classId+"_"+courseId);
					if(task==null){
						continue;
					}
					if(task.isFinish()){
						continue;
					}
					GridPoint gridPoint = new GridPoint();
					gridPoint.setAdvance(false);
					gridPoint.setArrangedNum(1);
					gridPoint.setClassId(classId);
					gridPoint.setCourseId(courseId);
					gridPoint.setCourseType(0);
					gridPoint.setDay(day);
					
					gridPoint.setGradeId(task.getGradeId());
					gridPoint.setGradeLevel(task.getGradeLevel());
					gridPoint.setLesson(lesson);
					gridPoint.setNeedCourseNumControl(false);
					gridPoint.setTaskGroupId(task.getTaskGroupId());
					gridPoint.setMcGroupId(mcid);
					String taskCourseKey = classId + "_" + courseId;
					List<String> teacherIds = classTaskTeacherMap.get(taskCourseKey);
					gridPoint.setTeacherIds(teacherIds );
					this.addGridPoint(gridPoint);
					task.setArrangedTaskNum(task.getArrangedTaskNum()+arNum);
				}	
			}
			return true;
		}
		return rs;
	}

	/**
	 * 预排课安排
	 * @param advanceArrangeList
	 * @param classTaskTeacherMap
	 * @param fixedCourses
	 */
	private void preArrangeFixedCourse(List<JSONObject> advanceArrangeList,
			HashMap<String, List<String>> classTaskTeacherMap,
			List<GridPoint> fixedCourses) {
		HashMap<String,List<String>> taskKeySpRecord = new HashMap<String, List<String>>();
		double totalPreNum = 0;
		List<ScheduleTask> needRecountTasks = new ArrayList<ScheduleTask>();
		for (JSONObject advanceArrange : advanceArrangeList) {

			String classId = advanceArrange.getString("ClassId");
			String courseId = advanceArrange.getString("CourseId");
			ScheduleTask task = classCourseTaskMap.get(classId+"_"+courseId);
			if(task==null){
				continue;
			}
			int day = advanceArrange.getIntValue("DayOfWeek");
			int lesson = advanceArrange.getIntValue("LessonOfDay");
			int courseType = advanceArrange.getIntValue("CourseType");
			String taskCourseKey = classId + "_" + courseId;
			double arrangedNum =courseType==0?1:0.5;
			totalPreNum += arrangedNum;
			
			task.setArrangedTaskNum(task.getArrangedTaskNum()+arrangedNum);
			needRecountTasks.add(task);
			if(recordAndJugeHasSp(taskKeySpRecord,taskCourseKey,task,day,lesson)){
				task.setHasSpNum(task.getHasSpNum()+1);
				ScheduleClass classtask = this.getScheduleClassByClassId(classId);
				if(classtask==null){
					continue;
				}
				classtask.addSpNumOnDay(day);
			}
			
			String gradeId = this.scheduleDatadic.getGradeSynjByClassId(classId);
			String gradeLevel = this.scheduleDatadic.getGradeLevelByClassId(classId);
			GridPoint gridPoint = new GridPoint();
			gridPoint.setAdvance(true);
			gridPoint.setArrangedNum(arrangedNum);
			gridPoint.setClassId(classId);
			gridPoint.setCourseId(courseId);
			gridPoint.setCourseType(courseType);
			gridPoint.setDay(day);
			
			gridPoint.setGradeId(gradeId);
			gridPoint.setGradeLevel(gradeLevel);
			gridPoint.setLesson(lesson);
			gridPoint.setNeedCourseNumControl(false);
			gridPoint.setTaskGroupId(null);
			
			List<String> teacherIds = classTaskTeacherMap.get(taskCourseKey);
			gridPoint.setTeacherIds(teacherIds );
			
			this.addGridPoint(gridPoint);

			fixedCourses.add(gridPoint);

			task.resetUnfinishLc(this);
		}
		setTotalPreNum(totalPreNum);
		for(ScheduleTask task:needRecountTasks){
			task.rebuildTrueTaskNum();
		}
	}
	
	/**
	 * 查询并记录预排课是否有连排
	 * @param taskKeySpRecord
	 * @param taskCourseKey
	 * @param task
	 * @param day
	 * @param lesson
	 * @return
	 */
	private boolean recordAndJugeHasSp(
			HashMap<String, List<String>> taskKeySpRecord,
			String taskCourseKey, ScheduleTask task, int day, int lesson) {
		// TODO Auto-generated method stub
		if(taskKeySpRecord.containsKey(taskCourseKey)){
			List<String> strss = taskKeySpRecord.get(taskCourseKey);
			for(String str:strss){
				int pday = Integer.parseInt(str.split(",")[0]);
				int plesson = Integer.parseInt(str.split(",")[1]);
				if(pday==day&&Math.abs(lesson-plesson)==1){
					if(lesson<task.getClassAmNum()&&plesson<task.getClassAmNum() ){
						strss.add(day+","+lesson);
						return true;
					}
					if(lesson>=task.getClassAmNum()&&plesson>=task.getClassAmNum()){
						strss.add(day+","+lesson);
						return true;
					}
				}
			}
			strss.add(day+","+lesson);
		}else{
			List<String> strs = new ArrayList<String>();
			strs.add(day+","+lesson );
			taskKeySpRecord.put(taskCourseKey, strs);
		}
		return false;
	}

	public void startArrange(){
		// 先排连排课
		int tgroupRetryTimes = 0;
		//先排优先排课位置
		while(unfinishTaskGroups.size()>0&&tgroupRetryTimes<=retryTimes*3){
			tgroupRetryTimes++;
			for(ScheduleTaskGroup unfiTaskGroup:unfinishTaskGroups){
				List<ScheduleTask> childTasks = unfiTaskGroup.getChildTasks();
				for(ScheduleTask task:childTasks){
					if(task.getNeedSpNum()==0&&task.getPreSingleLcs()!=null&&task.getPreSingleLcs().size()>0){
						List<Integer> lcs = task.getPreSingleLcs();
						for(int lc:lcs){
							ArrangeSingleTask(task, unfiTaskGroup, lc, 1);
						}
						task.resetUnfinishLc(this);
					}
				}
			}
		}
		DynamicPorcessProgressProc spThread = new DynamicPorcessProgressProc(
				"正在智能排课...", processId, 1, 30, 2, 40 );

		spThread.start();
//		tgroupRetryTimes=0;
//		while(unfinishTasks.size()>0&&tgroupRetryTimes<retryTimes*3){
//			tgroupRetryTimes++;
//			for(ScheduleTask  task :unfinishTasks){
//				if(task.getPreSingleLcs()!=null&&task.getPreSingleLcs().size()>0){
//					List<Integer> lcs = task.getPreSingleLcs();
//					for(int lc:lcs){
//						ArrangeSingleTask(task, null, lc, 1);
//					}
//					task.resetUnfinishLc(this);
//				}
//			}
//		}
		rebuildTasksUperMath();
		//再排带连排课的
		tgroupRetryTimes = 0;
		while(unfinishTaskGroups.size()>0&&tgroupRetryTimes<=retryTimes*10){
			tgroupRetryTimes++;
			for(ScheduleTaskGroup unfiTaskGroup:unfinishTaskGroups){
				if(unfiTaskGroup.isFinish()||unfiTaskGroup.getGroupMaxSpNum()==0){
					continue;
				}
				ArrangeTaskGroupWithSp(unfiTaskGroup);
			}
		}
		tgroupRetryTimes=0;
		while(unfinishTasks.size()>0&&tgroupRetryTimes<retryTimes*10){
			tgroupRetryTimes++;
			for(ScheduleTask  unfiTask :unfinishTasks){
				if(unfiTask.isFinish()||unfiTask.getNeedSpNum()==0){
					continue;
				}
				ArrangeSingleTaskSp(unfiTask, null);
			}
		}
		// 再排带连排课的主课
		int zgroupRetryTimes = 0;
		while(unfinishTaskGroups.size()>0&&zgroupRetryTimes<=retryTimes){
			zgroupRetryTimes++;
			for(ScheduleTaskGroup unfiTaskGroup:unfinishTaskGroups){
				if(unfiTaskGroup.isFinish()||unfiTaskGroup.getGroupMaxSpNum()==0){
					continue;
				}
				ArrangeTaskGroup(unfiTaskGroup);
				if(tgroupRetryTimes==retryTimes&&!unfiTaskGroup.isFinish()){
					System.out.println("----------未完成主课:"+unfiTaskGroup.getCourseId());
				}
			}
		}
		//先排单班超过天数的课程
		//排其它课程
		int singleTryTimes = 0;
		while(unfinishTasks.size()>0&&singleTryTimes<retryTimes){
			singleTryTimes++;
			List<ScheduleTask> needRemove = new ArrayList<ScheduleTask>();
			for(ScheduleTask task:unfinishTasks){
				if(task.isFinish()){
					needRemove.add(task);
					continue;
				}
				if(task.getTaskNum()>=task.getMaxDays()||task.getSpNum()>0){
					
					boolean finish = ArrangeTask(task, null);
					if(finish){
						needRemove.add(task);
					}
				}
			}
			
			unfinishTasks.removeAll(needRemove);
			Collections.reverse(unfinishTasks);
		}
		
		//然后排合组课程
		startArrangeNormalUnfinish();
		DynamicPorcessProgressProc spThread2 = new DynamicPorcessProgressProc(
				"正在优化排课结果... ", processId, 1, 25, 1, 70 );
		spThread2.start();
		if(unfinishTaskGroups.size()!=0||unfinishTasks.size()!=0){
			System.out.println("--------------开始优化课表------------此时进度比例："+this.getProgramProgress()+"%");
			
			growUpForCourseRule();
			startArrangeNormalUnfinish();
			ranomEvolution();
			System.out.println("--------------优化课表完成------------此时进度比例："+this.getProgramProgress()+"%");
		}else{
			growUpForCourseRule();
			growUpForCourseRuleTwo();
		}
		int isTryFinish = scheduleRule.getIsTryFinish();
		if(isTryFinish>0){
			if(unfinishTaskGroups.size()!=0||unfinishTasks.size()!=0){
				scheduleRule.setIsTeachingSync(-1);
				startArrangeNormalUnfinish();
				System.out.println("-------------突破规则排课表完成------------此时进度比例："+this.getProgramProgress()+"%");
			
			}
			if(unfinishTaskGroups.size()!=0||unfinishTasks.size()!=0){
				setDeepSwap(true);
				System.out.println("-------------深度交换突破规则排课表完成------------此时进度比例："+this.getProgramProgress()+"%");
				
			}
//			for(int i=0;i<3;i++){
				long dd0 = new Date().getTime();
				
				startArrangeNormalUnfinish();
				long dd1 = new Date().getTime();
				System.out.println("--------------优化阶段 0000111耗时"+(dd1-dd0)+"--------");
//				growUpForTeacher();
				growUpForCourseRule();
				long dd2 = new Date().getTime();
				System.out.println("--------------优化阶段 00002222耗时"+(dd2-dd0)+"--------");
				growUpForCourseRuleTwo();
				long dd3 = new Date().getTime();
				System.out.println("--------------优化阶段 00003333耗时"+(dd3-dd0)+"--------");
//			}
//			if((unfinishTaskGroups.size()!=0||unfinishTasks.size()!=0) ){
//				ranomEvolution();
//				System.out.println("-------------深度交换再次尝试突破规则排课表完成------------此时进度比例："+this.getProgramProgress()+"%");
//				
//			}
		}
		
	}

	//校验课时、单双周等是否合规
	public  JSONObject preCheckValid() {
		// TODO Auto-generated method stub
		String rsMsg = "";
		int code = 1;
		JSONObject rsObj = new JSONObject();
		for(ScheduleClass schClass:this.getClassIdScheduleMap().values()){
			String classId = schClass.getClassId();
			String className = scheduleDatadic.getClassNameById(classId);
			
			//先校验单双周是否配对
			HashMap<String,String> singleMap = (HashMap<String, String>) SingleDoubleCourseMap.get("map1");
			HashMap<String,String> doubleMap = (HashMap<String, String>) SingleDoubleCourseMap.get("map2");
			
			int allPositions = 0;
			double allTaskNum = schClass.getClassTaskNum();
			int dayNum = schClass.getAmNum()+schClass.getPmNum();
			//可排位置
			for(int i=0;i<maxDays;i++){
				for(int j=0;j<dayNum;j++){
					if(halfAtLastDay&&i==(maxDays-1)&&j>=schClass.getAmNum()){
						continue;
					}
					GridPointGroup[][] tb = schClass.getGridPointArr();
					if(tb[i][j]!=null){
						boolean fixPre = false;
						GridPointGroup tg = tb[i][j] ;
						List<GridPoint> tglist = tg.getPointList();
						if(tglist!=null&&tglist.size()>0){
							for(GridPoint t:tglist){
								if(t.isAdvance()){
									fixPre = true;
									break;
								}
							}
						}
						if(fixPre){
							
							continue;
						}
					}
					allPositions++;
				}
			}
			if(allTaskNum>allPositions){
				code = -1;
				rsMsg =  "存在班级教学任务课时超出可排位置,请回到设置课表或教学任务中进行修改，再进行智能编排！";
				break;
			}
			//未绑定的单双周计数
			int unBindSdTasks = 0;
			StringBuffer sdCourses = new StringBuffer();
			for(ScheduleTask task:schClass.getClassTasks()){
				double needArNum = task.getTaskNum()-task.getArrangedTaskNum();
				if(needArNum*10%10==5 ){
					String ccKey = task.getClassId()+"_"+task.getCourseId();
					if(singleMap!=null&&singleMap.containsKey(ccKey) ){
						continue;
					}
					if(doubleMap!=null&&doubleMap.containsKey(ccKey) ){
						continue;
					}
					//未找到绑定
					unBindSdTasks++;
					String courseName = scheduleDatadic.getCourseNameById(task.getCourseId());
					sdCourses.append(courseName);
					sdCourses.append(";");
				}
			}
			//两门及以上的单双周未做绑定
			if(unBindSdTasks>1){
				rsMsg  = "未绑定单双周科目，请到设置排课规则—单双周页面进行绑定后，再进行智能编排！";
				code = -1;
				break;
			}
		}
		rsObj.put("rsMsg", rsMsg);
		rsObj.put("code", code);
		return rsObj;
	}
	//优先排数学课
	private void rebuildTasksUperMath() {
		// TODO Auto-generated method stub
		List<ScheduleTaskGroup> mathTgs = new ArrayList<ScheduleTaskGroup>();
		List<ScheduleTaskGroup> otherTgs = new ArrayList<ScheduleTaskGroup>();
		for(ScheduleTaskGroup tg:unfinishTaskGroups){
			if(tg.getCourseId().equals("2")){
				mathTgs.add(tg);
			}else{
				otherTgs.add(tg);
			}
		}
		unfinishTaskGroups = new ArrayList<ScheduleTaskGroup>();
		unfinishTaskGroups.addAll(mathTgs);
		unfinishTaskGroups.addAll(otherTgs);
		
		List<ScheduleTask> mathTks = new ArrayList<ScheduleTask>();
		List<ScheduleTask> otherTks = new ArrayList<ScheduleTask>();
		for(ScheduleTask tg:unfinishTasks){
			if(tg.getCourseId().equals("2")){
				mathTks.add(tg);
			}else{
				otherTks.add(tg);
			}
		}
		unfinishTasks = new ArrayList<ScheduleTask>();
		unfinishTasks.addAll(mathTks);
		unfinishTasks.addAll(otherTks);
	}

	/**
	 * 优化上下午排课比例
	 */
	private void growUpForCourseRule() {
		// TODO Auto-generated method stub
		for(Iterator<String> tkey = classCourseTaskMap.keySet().iterator();tkey.hasNext();){
			String cckey = tkey.next();
			ScheduleTask task = classCourseTaskMap.get(cckey);
			if(true)
			{
				double amMax = task.getAmMaxPercent();
				double pmMax = task.getPmMaxNum();
				double nowam = 100;
				double nowpm = 100;
				int retryt = 0;
				while((nowpm>pmMax||nowam>amMax)){
					if(retryt>retryTimes*5 ){
						break;
					}
					retryt++;
					Map<String, List<String>> ammap = scheduleRule.getClassCourseAmMap();
					if(ammap!=null&&ammap.containsKey(cckey)){
						nowam = ammap.get(cckey).size();
						//上午课下调
						if(nowam>amMax+1){
							List<String> tmps = ammap.get(cckey);
							List<String> copys = new ArrayList<String>();
							for(String tp:tmps){
								copys.add(tp);
							}
							for(String daystr:copys){
								
								int day = Integer.parseInt(daystr.split(",")[0]);
								int lesson = Integer.parseInt(daystr.split(",")[1]);
								boolean rs = growUpForCourseRuleChild(task,day,lesson,1);
								if(rs){
									break;
								}
							}
//							for(int j=0;j<5;j++){
//								
//								String daystr = tmps.get((int)Math.random()*tmps.size());
//								int day = Integer.parseInt(daystr.split(",")[0]);
//								int lesson = Integer.parseInt(daystr.split(",")[1]);
//								boolean rs = growUpForCourseRuleChild(task,day,lesson);
//								if(rs){
//									break;
//								}
//							}
						}
					}
					Map<String, List<String>> pmmap = scheduleRule.getClassCoursePmMap();
					if(pmmap!=null&&pmmap.containsKey(cckey)){
						nowpm = pmmap.get(cckey).size();
						//下午课上调
						if(nowpm>pmMax){
//							System.out.println("----需要下午上调"+task.getCourseId()+"---"+pmMax);
							List<String> tmps = pmmap.get(cckey);
							List<String> copys = new ArrayList<String>();
							for(String tp:tmps){
								copys.add(tp);
							}
							for(String daystr:copys){
								
								int day = Integer.parseInt(daystr.split(",")[0]);
								int lesson = Integer.parseInt(daystr.split(",")[1]);
								boolean rs = growUpForCourseRuleChild(task,day,lesson,1);
								if(rs){
									break;
								}
							}
//							for(int j=0;j<5;j++){
//								
//								String daystr = tmps.get((int)Math.random()*tmps.size());
//								int day = Integer.parseInt(daystr.split(",")[0]);
//								int lesson = Integer.parseInt(daystr.split(",")[1]);
//								boolean rs = growUpForCourseRuleChild(task,day,lesson);
//								if(rs){
//									break;
//								}
//							}
						}
					}
					
				}
			}
		}
		
	}
	/**
	 * 平衡上下午比例 子进程
	 * @param task
	 * @param day
	 * @param lesson
	 * @param type 1:上下午对调；2：上午与上午 ，下午与下午
	 */
	private boolean growUpForCourseRuleChild(ScheduleTask task, int day, int lesson, int type) {
		// TODO Auto-generated method stub
		ScheduleClass schclass = this.getScheduleClassByClassId(task.getClassId());
		if(schclass!=null){
			int amNum = schclass.getAmNum();
			GridPointGroup[] dayTbs = schclass.getGridPointArr()[day];
			if(lesson<=amNum-1){
				//上午课需下调
				dayTbs = schclass.getGridPointArr()[day];
				GridPointGroup sourcegp = dayTbs[lesson];
				List<Integer> canswap = new ArrayList<Integer>();
				if(type==1){
					for(int i=amNum;i<dayTbs.length;i++){
						canswap.add(i);
					}
				}else{
					for(int i=0;i<dayTbs.length ;i++){
						if(i==lesson){
							continue;
						}
						canswap.add(i);
					}
				}
				for(int i:canswap){
					GridPointGroup tgp = dayTbs[i];
					sourcegp = dayTbs[lesson];
					if(sourcegp==null||tgp==null ){
						continue ;
					}
					String cid = tgp.getPointList().get(0).getCourseId();
					ScheduleTask tarTask = this.classCourseTaskMap.get(task.getClassId()+"_"+cid);
					if(type==1&&!tarTask.getCourseLevel().equals(CourseLevel.PmFirst)){
						continue;
					}
					boolean isMerge = false;
					for(GridPoint gp: tgp.getPointList()){
						if(gp.getMcGroupId()!=null&&gp.getMcGroupId().trim().length()>0){
							isMerge = true;
						}
					}
					if(isMerge){
						continue;
					}
					if(sourcegp.getPointList().size()==1){
						GridPoint srcGpoint = sourcegp.getPointList().get(0);
						if(srcGpoint.isSpPoint()||(srcGpoint.getMcGroupId()!=null&&srcGpoint.getMcGroupId().trim().length()>0)){
							return false;
						}
						List<GridPoint> glist = new ArrayList<GridPoint>();
						for(GridPoint gp:tgp.getPointList()){
							glist.add(gp);
						}
						if(tgp!=null){
							boolean canSwap = scheduleRule.canSwapPosition(srcGpoint, tgp, day, lesson, srcGpoint.getCourseType(), this, day);
							if(canSwap){
								for(GridPoint gp:glist){
									this.removeGridPoint(gp);
								}
								this.removeGridPoint(srcGpoint);
								for(GridPoint gp:glist){
									gp.setDay(srcGpoint.getDay());
									gp.setLesson(srcGpoint.getLesson());
									this.addGridPoint(gp);
								}
								srcGpoint.setDay(day);
								srcGpoint.setLesson(i);
								this.addGridPoint(srcGpoint);
								System.out.println("优化上午到下午-----:--源"+task.getCourseId()+"---目标"+glist.get(0).getCourseId()+"mcgId:"+glist.get(0).getMcGroupId());
								return true;
							}
						}
					}
				}
			}else{
				//下午克上调
				dayTbs = schclass.getGridPointArr()[day];
				GridPointGroup sourcegp = dayTbs[lesson];
				List<Integer> canswap = new ArrayList<Integer>();
				if(type==2){
					for(int i=0;i<dayTbs.length ;i++){
						if(i==lesson){
							continue;
						}
						canswap.add(i);
					}
				}else{
					for(int i=0;i<amNum&&(i!=lesson);i++){
						canswap.add(i);
					}
				}
				for(int i:canswap){
					GridPointGroup tgp = dayTbs[i];
					sourcegp = dayTbs[lesson];
					if(sourcegp==null||tgp==null ){
						continue ;
					}
					boolean isMerge = false;
					for(GridPoint gp: tgp.getPointList()){
						if(gp.getMcGroupId()!=null&&gp.getMcGroupId().trim().length()>0){
							isMerge = true;
						}
					}
					if(isMerge){
						continue;
					}
					if(sourcegp.getPointList().size()==1){
						
						GridPoint srcGpoint = sourcegp.getPointList().get(0);
						if(srcGpoint.isSpPoint()||(srcGpoint.getMcGroupId()!=null&&srcGpoint.getMcGroupId().trim().length()>0)){
							return false;
						}
						List<GridPoint> glist = new ArrayList<GridPoint>();
						for(GridPoint gp:tgp.getPointList()){
							glist.add(gp);
						}
						
						String cid = tgp.getPointList().get(0).getCourseId();
						if(Long.parseLong(cid)<=3&&type==1){
							continue;
						}
						if(tgp!=null){
							boolean canSwap = scheduleRule.canSwapPosition(srcGpoint, tgp, day, lesson, srcGpoint.getCourseType(), this, day);
							if(canSwap){
								for(GridPoint gp:glist){
									this.removeGridPoint(gp);
								}
								this.removeGridPoint(srcGpoint);
								for(GridPoint gp:glist){
									gp.setDay(srcGpoint.getDay());
									gp.setLesson(srcGpoint.getLesson());
									this.addGridPoint(gp);
								}
								srcGpoint.setDay(day);
								srcGpoint.setLesson(i);
								this.addGridPoint(srcGpoint);
								System.out.println("优化下午到上午-----:--源"+task.getCourseId()+"---目标"+glist.get(0).getCourseId()+"mcgId:"+glist.get(0).getMcGroupId());
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 控制科目不出现直线排课
	 */
	private void growUpForCourseRuleTwo() {
		// TODO Auto-generated method stub
		for(Iterator<String> tkey = classCourseTaskMap.keySet().iterator();tkey.hasNext();){
			String cckey = tkey.next();
			ScheduleTask task = classCourseTaskMap.get(cckey);
			Map<String, List<String>> ammap = scheduleRule.getClassCourseAmMap();
			Map<String, List<String>> pmmap = scheduleRule.getClassCoursePmMap();
			if(ammap!=null&&ammap.containsKey(cckey)){
				List<String> amList = ammap.get(cckey);
				if(amList.size()>=3){
					List<String> needDisList = isThereLineInTb(amList);
					if(needDisList.size()>0){
						String poss = "";
						for(String str:needDisList){
							poss+= str+";";
						}
							
						System.out.println("----需要打破上午直线---"+task.getCourseId()+"--"+task.getClassId()+"--"+poss);
						
						for(String daystr:needDisList){
							int day = Integer.parseInt(daystr.split(",")[0]);
							int lesson = Integer.parseInt(daystr.split(",")[1]);
							boolean rs = growUpForCourseRuleChild(task,day,lesson,2);
							if(rs){
								System.out.println("----打破上午直线---"+task.getCourseId()+"--"+task.getClassId());
								break;
							}
						}
					}
				}
			}
			if(pmmap!=null&&pmmap.containsKey(cckey)){
				List<String> pmList = pmmap.get(cckey);
				if(pmList.size()>=3){
					List<String> needDisList = isThereLineInTb(pmList);
					if(needDisList.size()>0){
						String poss = "";
						for(String str:needDisList){
							poss+= str+";";
						}
							
						System.out.println("----需要打破上午直线---"+task.getCourseId()+"--"+task.getClassId()+"--"+poss);
						for(String daystr:needDisList){
							int day = Integer.parseInt(daystr.split(",")[0]);
							int lesson = Integer.parseInt(daystr.split(",")[1]);
							boolean rs = growUpForCourseRuleChild(task,day,lesson,2);
							if(rs){
								System.out.println("----打破下午直线---"+task.getCourseId()+"--"+task.getClassId());
								break;
							}
						}
					}
				}
			}
		}
	}
	/**
	 * 判断是否有连续直线
	 * @param pmList
	 * @return
	 */
	private List<String> isThereLineInTb(List<String> pmList) {
		// TODO Auto-generated method stub
		List<String> rs = new ArrayList<String>();
		HashMap<Integer,List<Integer>> lessonDayMap = new HashMap<Integer, List<Integer>>();
		for(String daystr:pmList){
			int day = Integer.parseInt(daystr.split(",")[0]);
			int lesson = Integer.parseInt(daystr.split(",")[1]);
			if(lessonDayMap.containsKey(lesson)){
				lessonDayMap.get(lesson).add(day);
			}else{
				List<Integer> days = new ArrayList<Integer>();
				days.add(day);
				lessonDayMap.put(lesson, days);
			}
		}
		for(Iterator<Integer> it = lessonDayMap.keySet().iterator();it.hasNext();){
			int lesson = it.next();
			List<Integer> days = lessonDayMap.get(lesson);
			if(days.size()<3){
				continue;
			}
			Collections.sort(days);
			int maxLx = 0;
			int last = -10;
			for(int day:days){
				if(maxLx==2){
					break;
				}
				if(day==last+1){
					maxLx++;
				}else{
					maxLx =0;
				}
				last = day;
			}
			if(maxLx==2){
				rs.add(last+","+lesson);
				rs.add((last-1)+","+lesson);
				rs.add((last-2)+","+lesson);
				return rs;
			}
		}
		return rs;
	}

	private void swapGridPoints(ScheduleTask task, int day, int lesson,
			GridPointGroup sourcegp, int tarDay, int tarLesson, GridPointGroup tgp) {
		
			//交换B与C
			List<GridPoint> srcList = new ArrayList<GridPoint>();
			List<GridPoint> tarList = new ArrayList<GridPoint>();
			for(GridPoint gpoint:sourcegp.getPointList()){
				srcList.add(gpoint);
			}
			for(GridPoint gpoint:tgp.getPointList()){
				tarList.add(gpoint);
			}
			for(GridPoint gpoint:srcList){
				removeGridPoint(gpoint);
			}
			for(GridPoint gpoint:srcList){
				gpoint.setDay(tarDay);
				gpoint.setLesson(tarLesson);
				addGridPoint(gpoint);
			}
			for(GridPoint gpoint:tarList){
				removeGridPoint(gpoint);
			}
			for(GridPoint gpoint:tarList){
				gpoint.setDay(day);
				gpoint.setLesson(lesson);
				addGridPoint(gpoint);
			}
	}

	/**
	 * 
	 */
	private void startArrangeNormalUnfinish() {
		int groupRetryTimes = 0;
		while(unfinishTaskGroups.size()>0&&groupRetryTimes<retryTimes){
			groupRetryTimes++;
			List<ScheduleTaskGroup> fiTaskGroup = new ArrayList<ScheduleTaskGroup>();
			for(ScheduleTaskGroup unfiTaskGroup:unfinishTaskGroups){
				if(unfiTaskGroup.isFinish()){
					fiTaskGroup.add(unfiTaskGroup);
					continue;
				}
				boolean finish  = ArrangeTaskGroup(unfiTaskGroup);
				if(finish){
					fiTaskGroup.add(unfiTaskGroup);
				}
			}
			
			unfinishTaskGroups.removeAll(fiTaskGroup);
		}
		//排其它课程
		int taskRetryTimes = 0;
		
		while(unfinishTasks.size()>0&&taskRetryTimes<retryTimes){
			taskRetryTimes++;
			ArrayList<ScheduleTask> needRemove = new ArrayList<ScheduleTask>();
			for(ScheduleTask task:unfinishTasks){
				if(task.isFinish()){
					needRemove.add(task);
					continue;
				}
				boolean finish = ArrangeTask(task, null);
				if(finish){
					needRemove.add(task);
				}
			}
			
			unfinishTasks.removeAll(needRemove);
			Collections.reverse(unfinishTasks);
		}
	}

	public void ranomEvolution() {
		// TODO Auto-generated method stub
		//开启强排模式
		rebuild = true;
		int loop = 0;
		int maxLoop = 30;
		if(deepSwap){
			maxLoop = 2;
		}
		//控制循环时间不超过1分钟
		long maxTime = 60000;
		long dstart = new Date().getTime();
		while(this.getProgramProgress()<100&&loop<maxLoop&&(new Date().getTime()-dstart)<maxTime){
			loop++;
			randomChild(loop);
			startArrangeNormalUnfinish();
//			growUpForTeacher();
		}
	}


	/**
	 * 教师课程集中/分散优化算法
	 */
	public void growUpForTeacher() {
		// TODO Auto-generated method stub
			
//		for(Iterator<String> it = scheduleRule.getTeacherAmPosMapCCkey().keySet().iterator();it.hasNext();){
//			String teacherId  = it.next();
//			 
//			List<String> teacherPoss = new ArrayList<String>();
//			
//			Map<String, String> teacherPosCCkeyMap = scheduleRule.getTeacherAmPosMapCCkey().get(teacherId);
//			
//			teacherPoss.addAll(teacherPosCCkeyMap.keySet());
//			
//		}
	}

	/**
	 * @param glist
	 * @param amNum
	 * @return
	 */
	public boolean isTeacherPointAllNear(List<GridPoint> glist, int amNum) {
		boolean teaAllNear = true;
		int amPmFlag = -1;
		for(int i=0;i<glist.size()-1;i++){
			int left = glist.get(i).getLesson();
			int right = glist.get(i+1).getLesson();
			int lamPmFlag =0;
			if(left<amNum){
				lamPmFlag  = 0;
			}else{
				lamPmFlag = 1;
			}
			int ramPmFlag =0;
			if(right<amNum){
				ramPmFlag  = 0;
			}else{
				ramPmFlag = 1;
			}
			if(right-left>1||(lamPmFlag!=ramPmFlag)
					||(amPmFlag!=-1&&lamPmFlag!=amPmFlag)
					||(amPmFlag!=-1&&ramPmFlag!=amPmFlag)){
				teaAllNear = false;
				break;
			}
		}
		return teaAllNear;
	}

	public boolean isRebuild() {
		return rebuild;
	}

	public void setRebuild(boolean rebuild) {
		this.rebuild = rebuild;
	}

	private void randomChild(int tts) {
		// TODO Auto-generated method stub
		List<String> unfinishClasses = getUnfinishClasses() ;
//		if(tts>5){
//			unfinishClasses = new ArrayList<String>();
//			unfinishClasses.addAll( classIdScheduleMap.keySet());
//		}
		for(String classId:unfinishClasses){
			ScheduleClass schClass = this.getScheduleClassByClassId(classId);
			if(schClass==null){
				continue;
			}
			GridPointGroup[][] classtb = schClass.getGridPointArr();
			List<GridPoint> classfinishLessons = schClass.getFinishLessons();
			if(classtb==null||classfinishLessons.size()==0){
				continue;
			}
			int loop = 0;
			while(loop<5 ){
				classtb = schClass.getGridPointArr();
				classfinishLessons = schClass.getFinishLessons();
				loop++;
				int src = (int)(Math.random()*classfinishLessons.size()) ;
				GridPoint srcGpoint = classfinishLessons.get(src);
				boolean isMerge = false;
				String mcid = srcGpoint.getMcGroupId();
				if(mcid!=null&&mcid.trim().length()>0 ){
					isMerge = true;
				}
				if(srcGpoint.getCourseType()!=0||isMerge||srcGpoint.isAdvance()||srcGpoint.isSpPoint()  ){
					continue;
				}
				ScheduleTask srctask =  getClassCourseTaskMap().get(classId+"_"+srcGpoint.getCourseId());
				ScheduleTaskGroup srctaskGroup =  getScheduleTaskGroupById(srctask.getTaskGroupId());
				if(srctaskGroup!=null){
					int lc = srctaskGroup.getLcByPosition(srcGpoint.getDay(),srcGpoint.getLesson());
					List<String> lcposs = srctaskGroup.getLcPositionsMap().get(lc);
					if(lcposs!=null&&lcposs.size()>0){
						for(int i=0;i<lcposs.size();i++){
							String daystr = lcposs.get(i);
							int tarday = Integer.parseInt(daystr.split(",")[0]);
							int tarlesson = Integer.parseInt(daystr.split(",")[1]);
							GridPointGroup gps = classtb[tarday][tarlesson];
							if(tarday==srcGpoint.getDay()&&tarlesson==srcGpoint.getLesson()){
								continue;
							}
							if(gps==null||gps.getPointList().size()==0){
								continue;
							}
							boolean isMerget = false;
							for(GridPoint gp: gps.getPointList()){
								if(gp.getMcGroupId()!=null&&gp.getMcGroupId().trim().length()>0){
									isMerget = true;
								}
							}
							if(isMerget){
								continue;
							}
							List<GridPoint> glist = new ArrayList<GridPoint>();
							for(GridPoint gp:gps.getPointList()){
								glist.add(gp);
							}
							if(gps!=null){
								boolean canSwap = scheduleRule.canSwapPosition(srcGpoint, gps, srcGpoint.getDay(), srcGpoint.getLesson(), srcGpoint.getCourseType(), this, tarday);
								if(canSwap){
									for(GridPoint gp:glist){
										this.removeGridPoint(gp);
									}
									this.removeGridPoint(srcGpoint);
									for(GridPoint gp:glist){
										gp.setDay(srcGpoint.getDay());
										gp.setLesson(srcGpoint.getLesson());
										this.addGridPoint(gp);
									}
									srcGpoint.setDay(tarday);
									srcGpoint.setLesson(tarlesson);
									this.addGridPoint(srcGpoint);
									String cName = this.getScheduleDatadic().getClassNameById(classId);
									String srcCourse = this.getScheduleDatadic().getCourseNameById(srcGpoint.getCourseId());
									System.out.println("【交换课眼排课】-成功："+ cName+":"+srcCourse+":"+tarday+","+tarlesson);
									break;
								}
							}
						}	
					}
				}else{
					for(int lesson=0;lesson<srctask.getClassAmNum()+srctask.getClassPmNum();lesson++){
						if(lesson==srcGpoint.getLesson()){
							continue;
						}
						int srcDay = srcGpoint.getDay();
						int tarday = srcDay;
						int tarlesson = lesson;
						GridPointGroup gps = classtb[tarday][tarlesson];
						if(gps==null||gps.getPointList().size()==0){
							continue;
						}
						List<GridPoint> glist = new ArrayList<GridPoint>();
						for(GridPoint gp:gps.getPointList()){
							glist.add(gp);
						}
						if(gps!=null){
							boolean canSwap = scheduleRule.canSwapPosition(srcGpoint, gps, srcGpoint.getDay(), srcGpoint.getLesson(), srcGpoint.getCourseType(), this, tarday);
							if(canSwap){
								
								for(GridPoint gp:glist){
									this.removeGridPoint(gp);
								}
								this.removeGridPoint(srcGpoint);
								for(GridPoint gp:glist){
									gp.setDay(srcGpoint.getDay());
									gp.setLesson(srcGpoint.getLesson());
									this.addGridPoint(gp);
								}
								srcGpoint.setDay(tarday);
								srcGpoint.setLesson(tarlesson);
								this.addGridPoint(srcGpoint);
//								System.out.println("【交换课眼排课】-成功：");
								String cName = this.getScheduleDatadic().getClassNameById(classId);
								String srcCourse = this.getScheduleDatadic().getCourseNameById(srcGpoint.getCourseId());
								System.out.println("【交换课眼排课】-成功："+ cName+":"+srcCourse+":"+tarday+","+tarlesson);
								break;
							}
						}
					}
				}
			}
		}
	}

	public List<String> getUnfinishClasses(){
		List<String> rs = new ArrayList<String>();
		if(unfinishTaskGroups.size()>0){
			for(ScheduleTaskGroup tg:unfinishTaskGroups){
				List<ScheduleTask> childtks = tg.getChildTasks();
				for(ScheduleTask task:childtks ){
					if(!rs.contains(task.getClassId())){
						rs.add(task.getClassId());
					}
				}
			}
		}
		if(unfinishTasks.size()>0){
			for(ScheduleTask task:unfinishTasks){
				if(!rs.contains(task.getClassId())){
					rs.add(task.getClassId());
				}
			}
		}
		
		return rs;
	}
	private boolean ArrangeTaskGroup(ScheduleTaskGroup unfiTaskGroup) {
		// TODO Auto-generated method stub
		List<ScheduleTask> children = unfiTaskGroup.getChildTasks();
		for(ScheduleTask task:children){
			int childRetryTimes = 0;
			while(!task.isFinish()&&childRetryTimes<=retryTimes){
				childRetryTimes++;
				boolean childfinish = ArrangeTask(task,unfiTaskGroup);
			}
		}
		return unfiTaskGroup.isFinish();
	}

	private boolean ArrangeTaskGroupWithSp(ScheduleTaskGroup unfiTaskGroup) {
		// TODO Auto-generated method stub
		List<ScheduleTask> children = unfiTaskGroup.getChildTasks();
		if(children.size()>=2){
			int arnum = unfiTaskGroup.getHasArrangeSpNum();
			int maxnum= unfiTaskGroup.getGroupMaxSpNum();
			
			int spretryTimes = 0;
			while(arnum<maxnum&&spretryTimes<retryTimes){
				spretryTimes++;
				boolean gfinish = ArrangeTaskGroupSp(unfiTaskGroup);
				if(gfinish){
					arnum++;	
				}
			}	
			
		}else{
			for(ScheduleTask task:children){
				int childRetryTimes = 0;
				while(!task.isFinish()&&childRetryTimes<=retryTimes&&task.getNeedSpNum()>0){
					childRetryTimes++;
					ArrangeSingleTaskSp(task,unfiTaskGroup);
				}
			}
		}
		return unfiTaskGroup.isFinish();
	}

	/**
	 * 按组排连排课
	 * @param unfiTaskGroup
	 * @return
	 */
	private boolean ArrangeTaskGroupSp(ScheduleTaskGroup unfiTaskGroup) {
		// TODO Auto-generated method stub
		
		List<ScheduleTask> tasks = unfiTaskGroup.getChildTasks();
		List<Integer> yxlcs = new ArrayList<Integer>();
		HashMap<Integer,Integer> lcNumMap = new HashMap<Integer, Integer>();
		for(ScheduleTask task:tasks){
			if(task.getFirstArLcs().size()>0){
				for(int lc:task.getFirstArLcs()){
					if(lcNumMap.containsKey(lc)){
						lcNumMap.put(lc, lcNumMap.get(lc)+1);
					}else{
						lcNumMap.put(lc,1);
					}
				}
			}
		}
		for(int lc :lcNumMap.keySet()){
			if(lcNumMap.get(lc)==tasks.size()){
				yxlcs.add(lc);
			}
		}
		int retryTimes = 0;
		while (retryTimes <10){
			
			retryTimes++;
			int index = (int)(Math.random()*unfiTaskGroup.getAvgTaskNum())+1;
			if(retryTimes<5&&yxlcs.size()>0){
				index = yxlcs.get((int)(Math.random()*yxlcs.size())) ;
			}
			boolean rightCan = true;
			if(index>1){
				JSONObject finish = tryTempSp(unfiTaskGroup, tasks, index, index-1);
				rightCan = finish.getBooleanValue("rightCan");
				if(rightCan){
					for(ScheduleTask tk:tasks){
						tk.getFirstArLcs().remove((Integer) index);
					}
					return true;
				}
			}
			if(index<unfiTaskGroup.getMaxDays()){
				JSONObject finish = tryTempSp(unfiTaskGroup, tasks, index, index+1);
				rightCan = finish.getBooleanValue("rightCan");
				if(rightCan){
					for(ScheduleTask tk:tasks){
						tk.getFirstArLcs().remove((Integer) index);
					}
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * 尝试连排
	 * @param unfiTaskGroup
	 * @param tasks
	 * @param index
	 * @param rightCan
	 */
	public JSONObject tryTempSp(ScheduleTaskGroup unfiTaskGroup,
			List<ScheduleTask> tasks, int index,int nextIndex ) {
		
		//记录完成的连排任务
		HashMap<Integer,ScheduleTask> finishTemp = new HashMap<Integer, ScheduleTask>();
		
		boolean rightCan = true;
		
		for(ScheduleTask task:tasks){
			if(task.getNeedSpNum()>0&&(task.getTaskNum()-task.getArrangedTaskNum())>=2){
				
				int lc = index;
				boolean sfinish = ArrangeSingleTask(task, unfiTaskGroup, index, 2);
				
				if(!sfinish){
					sfinish = ArrangeSingleTask(task, unfiTaskGroup, nextIndex, 2);
					lc = nextIndex;
				}
				if(!sfinish){
					rightCan = false;
					break;
				}else{
					finishTemp.put(lc, task);
				}
			}
		}
		
		if(!rightCan){
			rollBackFinish(finishTemp);
		} 
		
		JSONObject rs = new JSONObject();
		
		rs.put("rightCan", rightCan);
		rs.put("finishTemp", finishTemp);
		
		return rs;
	}
	/**
	 * 用于尝试连排失败的回滚
	 * @param finishTemp
	 */
	private void rollBackFinish(HashMap<Integer, ScheduleTask> finishTemp) {
		// TODO Auto-generated method stub
		for(Iterator<Integer> it = finishTemp.keySet().iterator();it.hasNext();){
			int lc = it.next();
			ScheduleTask task = finishTemp.get(lc);
			ScheduleTaskGroup tg = allTaskGroupMap.get(task.getTaskGroupId());
			if(tg!=null){
				List<String> daylist = tg.getLcPositionsMap().get(lc);
				for(String poss:daylist){
					String[] pos = poss.split(",");
					int day = Integer.parseInt(pos[0]);
					int lesson = Integer.parseInt(pos[1]);
					GridPointGroup[][] tb = this.getClassIdScheduleMap().get(task.getClassId()).getGridPointArr();
					GridPointGroup ggp = tb[day][lesson];
					if(ggp!=null&&ggp.getPointList()!=null){
						List<GridPoint> gplist = ggp.getPointList();
						List<GridPoint> needRemove = new ArrayList<GridPoint>();
						for(GridPoint gp:gplist){
							if(gp.isSpPoint()&&gp.getCourseId().equalsIgnoreCase(task.getCourseId())){
								needRemove.add(gp);
							}
						}
						for(GridPoint gp:needRemove){
							this.removeGridPoint(gp);
						}
					}
				}
				if(task.getHasSpNum()>0){
					List<Integer> lcs = task.getUnfinishLc();
					lcs.add(lc);
					task.setHasSpNum(task.getHasSpNum()-1);
				}
				double arrangedTaskNum = task.getArrangedTaskNum();
				if(arrangedTaskNum>=2){
					task.setArrangedTaskNum(arrangedTaskNum-2);
				}
			}
		}
	}

	private boolean ArrangeSingleTaskSp(ScheduleTask task,
			ScheduleTaskGroup unfiTaskGroup) {
		// TODO Auto-generated method stub
		List<Integer> unfinishLc = new ArrayList<Integer>();
		if(task.getNeedSpNum()>0){
			for(int i=1;i<task.getTaskNum()+1;i++){
				unfinishLc.add((Integer) i);
			}
		}
		boolean needFir = true;
		if((task.getTaskNum()>maxDays&&task.getHasSpNum()> 1)||
				(task.getTaskNum()<=maxDays&&task.getArrangedTaskNum()>1)){
			  needFir = false;
		}
		int spretryTimes = 0;
		while(task.getNeedSpNum()>0&&spretryTimes<retryTimes*10){
			spretryTimes++;
			int index=0 ;
			if(needFir&&task.getFirstArLcs().size()>0){
				 index = task.getFirstArLcs().get((int)(Math.random()*task.getFirstArLcs().size())) ;
			}else {
				index = (int)(Math.random()*task.getTaskNum())+1;
			}
			boolean sfinish = ArrangeSingleTask(task, unfiTaskGroup, index, 2);
			if(sfinish){
				if(unfinishLc.contains((Integer) index)){
					unfinishLc.remove((Integer) index);
				}
				task.getFirstArLcs().remove((Integer) index);
			}else{
				if(!unfinishLc.contains((Integer) index)){
					
					unfinishLc.add((Integer) index);
				}
			}
		}
		task.setUnfinishLc(unfinishLc);
		task.resetUnfinishLc(this);
		return true;
	}

	/**
	 * 排单个教学任务-全部课时
	 * @param task
	 * @param unfiTaskGroup
	 * @return
	 */
	private boolean ArrangeTask(ScheduleTask task, ScheduleTaskGroup unfiTaskGroup) {
		// TODO Auto-generated method stub
		boolean taskf = true;
		if(!task.isFinish()){
			int spNum = task.getNeedSpNum();
			List<Integer> unfinishlc = task.getUnfinishLc();
			//用来记录排课失败的轮次【仅需要控制教学进度的需要】
			List<Integer> recordUnfinishlc = new ArrayList<Integer>();
			List<Integer> recordfinishlc = new ArrayList<Integer>();
			if(unfinishlc.size()!=0  ){
				for(int lc:unfinishlc){
					boolean single= true;
					if(spNum>0){
						spNum --;
						single = ArrangeSingleTask(task,unfiTaskGroup,lc,2);
					}else{
						single = ArrangeSingleTask(task,unfiTaskGroup,lc,1);
					}
					if(!single){
						taskf = false;
						if(!recordUnfinishlc.contains((Integer) lc)){
							recordUnfinishlc.add((Integer)lc);
						}
					}else{
						if(recordUnfinishlc.contains((Integer)lc)){
							recordfinishlc.remove((Integer)lc);
						}
					}
				}
			}else{
				double taskNum = task.getTaskNum() ;
				int index = (int) Math.ceil(taskNum);
				while(index>0){
					boolean single = true;
					if(spNum>0){
						for(int fr =index;fr>0;fr--){
							//排连排课 
							single = ArrangeSingleTask(task,unfiTaskGroup,fr,2);
							if(single){
								spNum --;
								index--;
								break;
							}else if(fr!=1){
								taskf = false;
								if(!recordUnfinishlc.contains((Integer)fr)){
									recordUnfinishlc.add((Integer)fr);
								}
							}
						}
					}else{
						single = ArrangeSingleTask(task,unfiTaskGroup,index,1);
					}
					if(!single){
						taskf = false;
						if(!recordUnfinishlc.contains((Integer)index)){
							recordUnfinishlc.add((Integer)index);
						}
					}else{
						if(recordUnfinishlc.contains((Integer)index)){
							recordfinishlc.remove((Integer)index);
						}
					}
					index--;
				}
			}
//			unfinishlc.removeAll(recordfinishlc);
//			task.setUnfinishLc(unfinishlc);
			task.resetUnfinishLc(this);
//			if(unfiTaskGroup!=null){
//				task.setUnfinishLc(recordUnfinishlc);
//			}
		}
		return taskf;
		
	}
	/**
	 * 排单个教学任务-单个课时
	 * @param task
	 * @param unfiTaskGroup
	 * @param	tasknum:安排课时数
	 * @return
	 */
	private boolean ArrangeSingleTask(ScheduleTask task,
			ScheduleTaskGroup unfiTaskGroup,Integer lc,double tasknum) {
		// TODO Auto-generated method stub
		if(task.isMergeClassCourse()){
			return false;
		}
		if(task.isFinish() ){
			return true;
		}
		
		if(tasknum==2&&task.getNeedSpNum()*2>(task.getTaskNum()-task.getArrangedTaskNum())){
			return ArrangeSingleTaskPairSp(  task,
					  unfiTaskGroup,  lc,  tasknum);	
		}
		CourseLevel courseLevel = task.getCourseLevel();
		int amNum = task.getClassAmNum();
		boolean rs = true;
		List<int[]> bestPoss = new ArrayList<int[]>();
		List<int[]> bestPossLv2 = new ArrayList<int[]>();
		List<int[]> canArrangePoss = new ArrayList<int[]>();
		List<int[]> canSwapPoss = new ArrayList<int[]>();
		ScheduleClass schClass = this.getScheduleClassByClassId(task.getClassId());
		GridPointGroup[][] classtable = schClass.getGridPointArr();
		List<String> poss = new ArrayList<String>();
		if(unfiTaskGroup!=null&& unfiTaskGroup.getLcPositionsMap().get(lc)!=null
				&&scheduleRule.getIsTeachingSync()==1
				&&!deepSwap){
			poss = unfiTaskGroup.getLcPositionsMap().get(lc);
		}else{
			for(int day=0;day<classtable.length;day++){
				for(int lesson=0;lesson<classtable[day].length;lesson++){
					if(schClass.isHalfAtLast()&&day==maxDays-1){
						if(lesson>=amNum){
							continue;
						}
					}
					poss.add(day+","+lesson);
				}
			}
		}
		for(String pos:poss){
			int day = Integer.parseInt(pos.split(",")[0]);
			int lesson = Integer.parseInt(pos.split(",")[1]);
			
			if(task.getTaskNum()>maxDays&&task.getSpNum()==0){
				if(isOwnCourseNear(task, classtable, day, lesson)){
					continue;
				}
			}
			if(classtable[day][lesson]!=null ){
				boolean isMerge = false;
				GridPointGroup gp = classtable[day][lesson];
				if(gp.getPointList()!=null){
					for(GridPoint gpt:gp.getPointList()){
						
						if(gpt.getMcGroupId()!=null&&gpt.getMcGroupId().trim().length()>0){
							isMerge = true;
							break;
						}
					}
				}
				if(isMerge){
					continue;
				}
			}
//			boolean canar = scheduleRule.canArrangePosition(task, day, lesson,0,this,tasknum);
			boolean canar = true;
			boolean levFit = true;
			if(task.getOrCourseLevel().equals(CourseLevel.AmFirst)&&lesson>=amNum){
				levFit = false;
			}
			if((courseLevel.equals(CourseLevel.PmFirst)||courseLevel.equals(CourseLevel.AllCan))&&lesson<amNum){
				levFit = false;
			}
			//设置理科不排
			Long cid = Long.parseLong(task.getCourseId());
			if(lesson==amNum||lesson==amNum-1){
				if(!this.isRebuild()&&(cid==2||cid==7||cid==8||cid==9)){
					levFit =  false;
				}
			}
			
			if(lesson == 0){
				if(cid==12||cid==14||cid==15 ){
					levFit = false;
				}
			}
			boolean hasClass = false;	
			if(classtable[day][lesson]!=null ){
				hasClass = true;
			}
			int[] posi = new int[]{day,lesson};
			boolean hasCourseNearBy = false;
			
			if(hasCourseNearBy(task.getCourseId(),classtable,day,lesson)){
				hasCourseNearBy = true;
			}
			boolean teacherNearBy = true;
			if(!isTeacherNearBy(task,day,lesson)){
				teacherNearBy = false;
			}
			boolean srcTeacherNearBy = teacherNearBy;
			// 1集中 2分散
			int distrubute = scheduleRule.getLessonDistrubute();
			
			if(distrubute==2){
				teacherNearBy = !teacherNearBy;
			}
			
			
			if( task.getTaskNum()>=task.getMaxDays()&&task.getSpNum()==0 ){
				teacherNearBy = !srcTeacherNearBy;
			}
			//优先排课位置
			boolean firstAr = false;
			//是否还需要优先排课位置
			boolean needFir = false;
//			if(tasknum==2){
				
				if(task.getFirstArPoss()!=null&&task.getFirstArPoss().size()>0){
					needFir = true;
					if(task.getFirstArPoss().contains(day+","+lesson)){
						firstAr = true;
					}
					//上午课
					if(lesson<amNum){
						if(lesson-1>=0){
							if(task.getFirstArPoss().contains(day+","+(lesson-1))){
								firstAr = true;
							}
						}
						if(lesson+1<amNum){
							if(task.getFirstArPoss().contains(day+","+(lesson+1))){
								firstAr = true;
							}
						}
					}
					//下午课
					if(lesson>=amNum){
						if(lesson-1>=amNum){
							if(task.getFirstArPoss().contains(day+","+(lesson-1))){
								firstAr = true;
							}
						}
						if(lesson+1<amNum+task.getClassPmNum()){
							if(task.getFirstArPoss().contains(day+","+(lesson+1))){
								firstAr = true;
							}
						}
					}
				}else{
					firstAr= true;
				}
				if((task.getTaskNum()>maxDays&&task.getHasSpNum()>1)||
						(task.getTaskNum()<=maxDays&&task.getArrangedTaskNum()>1)){
					needFir = false;
				}
//			}
			if(needFir){
				//符合尽量排、当前课眼无课、相邻无课 、优先排课 则为最佳位置
				if(tasknum==2){
					
				}
				if(canar&&firstAr&&  !hasClass   && teacherNearBy){
					bestPoss.add(posi);
				}
				if( canar&&firstAr  &&!hasClass  ){
					bestPossLv2.add(posi);
				}
			}else{
				//符合尽量排、当前课眼无课、相邻无课 则为最佳位置
				if(canar&&levFit &&!hasClass&&!hasCourseNearBy  && teacherNearBy ){
					bestPoss.add(posi);
				}
				if( canar&&levFit &&!hasClass  && teacherNearBy   ){
					bestPossLv2.add(posi);
				}
			}
			if(!hasClass ){
				canArrangePoss.add(posi);
			}
			if(hasClass ){
				canSwapPoss.add(posi);
			}
			
		}
		double needArNum = task.getTaskNum()-task.getArrangedTaskNum();
		if(needArNum*10%10==5 ){
			tasknum = 0.5;
		}
		boolean tp = insertPosition(task,bestPoss, tasknum,amNum);
		if(!tp){
			tp = insertPosition(task,bestPossLv2, tasknum,amNum);
		}
		if(!tp){
			tp = insertPosition(task,canArrangePoss, tasknum,amNum);
		}
		if(!tp&&tasknum==1){
			tp = insertSwapPosition(task,canSwapPoss, bestPoss, tasknum,amNum);
		}
		if(!tp&&tasknum==1){
			tp = insertSwapPosition(task,canSwapPoss, bestPossLv2, tasknum,amNum);
		}
		if(!tp&&tasknum==1){
			tp = insertSwapPosition(task,canSwapPoss, canArrangePoss, tasknum,amNum);
		}
		if(!tp&&tasknum==1&&deepSwap){
			tp = insertDeepSwapPosition(task,canSwapPoss, canArrangePoss, tasknum,amNum);
		}
		if(tp){
			//增加已排记录
			task.setArrangedTaskNum(task.getArrangedTaskNum()+tasknum);
			if(tasknum==2){
				task.setHasSpNum(task.getHasSpNum()+1);
			}
		}
		return tp;
	}

	/**
	 * 对已有排课的且课时不够仍需连排的课程凑连排
	 * @param task
	 * @param unfiTaskGroup
	 * @param lc
	 * @param tasknum
	 * @return
	 */
	public boolean ArrangeSingleTaskPairSp(ScheduleTask task,
			ScheduleTaskGroup unfiTaskGroup, Integer lc, double tasknum) {
		// TODO Auto-generated method stub
		if(task.isFinish() ){
			return true;
		}
		CourseLevel courseLevel = task.getCourseLevel();
		int amNum = task.getClassAmNum();
		boolean rs = true;
		List<int[]> bestPoss = new ArrayList<int[]>();
		ScheduleClass schClass = this.getScheduleClassByClassId(task.getClassId());
		GridPointGroup[][] classtable = schClass.getGridPointArr();
		List<String> poss = new ArrayList<String>();
		if(unfiTaskGroup!=null&& unfiTaskGroup.getLcPositionsMap().get(lc)!=null
				&&scheduleRule.getIsTeachingSync()==1
				&&!deepSwap){
			poss = unfiTaskGroup.getLcPositionsMap().get(lc);
		}else{
			for(int day=0;day<classtable.length;day++){
				for(int lesson=0;lesson<classtable[day].length;lesson++){
					if(schClass.isHalfAtLast()&&day==maxDays-1){
						if(lesson>=amNum){
							continue;
						}
					}
					poss.add(day+","+lesson);
				}
			}
		}
		for(String pos:poss){
			int day = Integer.parseInt(pos.split(",")[0]);
			int lesson = Integer.parseInt(pos.split(",")[1]);
			
			if(classtable[day][lesson]!=null ){
				boolean isMerge = false;
				GridPointGroup gp = classtable[day][lesson];
				if(gp.getPointList()!=null){
					for(GridPoint gpt:gp.getPointList()){
						
						if(gpt.getMcGroupId()!=null&&gpt.getMcGroupId().trim().length()>0){
							isMerge = true;
							break;
						}
					}
				}
				if(isMerge){
					continue;
				}
			}
			int[] posi = new int[]{day,lesson};
			
			if(schClass.hasCourseSpOnDay(task.getCourseId(), day)){
				continue;
			}
			boolean isOwnCourseNear = isOwnCourseNear(task,classtable,day,lesson);
			
			if(isOwnCourseNear){
				bestPoss.add(posi);
				continue;
			}
			
		}
		
		double needArNum = task.getTaskNum()-task.getArrangedTaskNum();
		if(needArNum*10%10==5 ){
			tasknum = 0.5;
		}else{
			tasknum =1;
		}
		
		boolean tp = insertPositionPairSp(task, bestPoss, tasknum, amNum);
		return tp;
	}

	/**
	 * 本班该节次前后是否有本课程 用于控制连排或者不可连排
	 * @param task
	 * @param classtable
	 * @param day
	 * @param lesson
	 * @return
	 */
	public boolean isOwnCourseNear(ScheduleTask task,
			GridPointGroup[][] classtable, int day, int lesson) {
		// TODO Auto-generated method stub
		boolean rs = false;
		boolean courseNear = false;
		
		int  amNum = task.getClassAmNum();
		int  pmNum = task.getClassPmNum();
		int dayNum = amNum+pmNum;
		//上午课
		if(lesson<=amNum-1){
			if(lesson<amNum-1){
				GridPointGroup gp  = classtable[day][lesson+1];
				if(gp!=null&& gp.getPointList()!=null){
					for(GridPoint gpt:gp.getPointList()){
						if(gpt.getCourseId().equals(task.getCourseId())){
							return true;
						}
					}
				}
			}
			if(lesson>0){
				GridPointGroup gp  =  classtable[day][lesson-1];
				if( gp!=null&&gp.getPointList()!=null){
					for(GridPoint gpt:gp.getPointList()){
						if(gpt.getCourseId().equals(task.getCourseId())){
							return true;
						}
					}
				}
				
			}
		}
		//下午课
		if(lesson>amNum-1){
			if(lesson<dayNum-1){
				GridPointGroup gp  = classtable[day][lesson+1];
				if( gp!=null&&gp.getPointList()!=null){
					for(GridPoint gpt:gp.getPointList()){
						if(gpt.getCourseId().equals(task.getCourseId())){
							return true;
						}
					}
				}
			}
			if(lesson>amNum){
				GridPointGroup gp  = classtable[day][lesson-1];
				if( gp!=null&&gp.getPointList()!=null){
					for(GridPoint gpt:gp.getPointList()){
						if(gpt.getCourseId().equals(task.getCourseId())){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	public boolean isTeacherNearBy(ScheduleTask task, int day, int lesson) {
		// TODO Auto-generated method stub
		List<String> teachers = task.getTeacherIds();
		if(teachers==null||teachers.size()==0 ){
			return true;
		}
		int amNum = task.getClassAmNum();
		int pmNum = task.getClassPmNum();
		int dayNum = amNum+pmNum;
//		ScheduleClass schClass = this.getScheduleClassByClassId(task.getClassId());
//		groupPointMap()
//		GridPointGroup[][] classtable = schClass.getGridPointArr();
		if(!groupPointMap.containsKey(day)){
			return true;
		}
		for(String tid:teachers){
			if(finishTeacherLessons.get(tid+"_"+day)==null){
				return true;
			}
		}
		//上午课
		if(lesson<=amNum-1){
			if(lesson<amNum-1){
				GridPointGroup gptp = groupPointMap.get(day).get(lesson+1);
				if(gptp!=null&&isGroupPointsTeacherContainAll(teachers,gptp)){
					return true;
				}
			}
			if(lesson>0){
				GridPointGroup gptp2 = groupPointMap.get(day).get(lesson-1);
				if(gptp2!=null&&isGroupPointsTeacherContainAll(teachers,gptp2)){
					return true;
				}
				
			}
		}
		//下午课
		if(lesson>amNum-1){
			if(lesson<dayNum-1){
				GridPointGroup gptp =groupPointMap.get(day).get(lesson+1);
				if(gptp!=null&&isGroupPointsTeacherContainAll(teachers,gptp)){
					return true;
				}
			}
			if(lesson>amNum){
				GridPointGroup gptp = groupPointMap.get(day).get(lesson-1);
				if(gptp!=null&&isGroupPointsTeacherContainAll(teachers,gptp)){
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * 教师为已排位置子集
	 * @param teachers
	 * @param gptp2
	 * @return
	 */
	public boolean isGroupPointsTeacherContainAll(List<String> teachers,
			GridPointGroup gptp) {
		// TODO Auto-generated method stub
		List<GridPoint> gpList = gptp.getPointList();
		if(gpList==null||gpList.size()==0){
			return false;
		}
		List<String> gpTeachers = new ArrayList<String>();
		for(GridPoint gp:gpList){
			if(gp.getTeacherIds()!=null){
				gpTeachers.addAll(gp.getTeacherIds());
			}
		}
		for(String tid:teachers){
			if(!gpTeachers.contains(tid)){
				return false;
			}
		}
		return true;
	}

	/**
	 * 交换排课
	 * @param task
	 * @param canSwapPoss	有课的位置
	 * @param bestSwapPoss	空位 
	 * @param taskNum
	 * @param amNum
	 * @return
	 */
	public boolean insertSwapPosition(ScheduleTask task,
			List<int[]> canSwapPoss,List<int[]> bestSwapPoss,
			double taskNum, int amNum) {
		// TODO Auto-generated method stub
		ScheduleClass schClass = this.getScheduleClassByClassId(task.getClassId());
		GridPointGroup[][] classtable = schClass.getGridPointArr();
		for(int[] spos:bestSwapPoss){
			int sday = spos[0];
			int slesson = spos[1];
			
			for(int[] pos:canSwapPoss){
				int day = pos[0];
				int lesson = pos[1];
				
				GridPointGroup gp = classtable[day][lesson];
				List<GridPoint> gpointlist = gp.getPointList();
				List<GridPoint> copyList = new ArrayList<GridPoint>();
				for(GridPoint gpoint:gpointlist){
					copyList.add(gpoint);
				}
				if(gp!=null&&taskNum<=1
						&&scheduleRule.canSwapPosition(task, gp, sday,slesson,0,this, day, false)){
					//先移除课程
					for(GridPoint gpoint:copyList){
						this.removeGridPoint(gpoint);
						gpoint.setDay(sday);
						gpoint.setLesson(slesson);
						this.addGridPoint(gpoint);
					}
					//非连排课
					GridPoint gridPoint = new GridPoint();
					gridPoint.setAdvance(false);
					gridPoint.setArrangedNum(1);
					gridPoint.setClassId(task.getClassId());
					gridPoint.setCourseId(task.getCourseId());
					int ctype = 0;
					if(taskNum==0.5){
						ctype = 1;
					}
					gridPoint.setCourseType(ctype);
					gridPoint.setDay(day);
					gridPoint.setGradeId(task.getGradeId());
					gridPoint.setGradeLevel(task.getGradeLevel());
					gridPoint.setLesson(lesson);
					gridPoint.setNeedCourseNumControl(false);
					gridPoint.setTaskGroupId(task.getTaskGroupId());
					List<String> teacherIds = classTaskTeacherMap.get(task.getClassId()+"_"+task.getCourseId());
					gridPoint.setTeacherIds(teacherIds );
					this.addGridPoint(gridPoint);
					
					System.out.println("-------"+task.getClassId()+"POS:"+day+","+lesson+":"+copyList.get(0).getCourseId()+"与"
							+sday+","+slesson+":"+task.getCourseId()+"交换成功");
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * 深度交换排课
	 * @param task
	 * @param canSwapPoss	有课的位置
	 * @param bestSwapPoss	空位 
	 * @param tasknum
	 * @param amNum
	 * @return
	 */
	public boolean insertDeepSwapPosition(ScheduleTask task,
			List<int[]> canSwapPoss,List<int[]> bestSwapPoss,
			double tasknum, int amNum) {
		// TODO Auto-generated method stub
		ScheduleClass schClass = this.getScheduleClassByClassId(task.getClassId());
		GridPointGroup[][] classtable = schClass.getGridPointArr();
		for(int[] spos:bestSwapPoss){
			int sday = spos[0];
			int slesson = spos[1];
			
			for(int[] pos:canSwapPoss){
				int day = pos[0];
				int lesson = pos[1];
				schClass = this.getScheduleClassByClassId(task.getClassId());
				classtable = schClass.getGridPointArr();
				//交换要交换的B
				GridPointGroup sourcegp = classtable[day][lesson];
				if(sourcegp==null||sourcegp.getPointList().size()==0 ){
					continue;
				}
				boolean isMergeB = false;
				for(GridPoint gp:sourcegp.getPointList()){
					if(gp.getMcGroupId()!=null&&gp.getMcGroupId().trim().length()>0){
						isMergeB = true;
						break;
					}
				}
				if(isMergeB){
					continue;
				}
				List<String> poss = new ArrayList<String>();
//				for(GridPoint gpoint:sourcegp.getPointList()){
//					ScheduleTaskGroup taskGroup =  getScheduleTaskGroupById(gpoint.getTaskGroupId());
//					if(taskGroup!=null){
//						int lc = taskGroup.getLcByPosition(day, lesson);
//						if(lc!=0&&taskGroup.getLcPositionsMap().containsKey(lc)){
//							List<String> add = taskGroup.getLcPositionsMap().get((Object) lc);
//							if(add!=null){
//								for(String p:add){
//									if(!poss.contains(p)){
//										poss.add(p);
//									}
//								}
//							}
//						}
//					}
//				}
				if(poss.size()==0){
					for(int i=0;i<classtable.length;i++){
						GridPointGroup[] dayarr = classtable[i];
						if(dayarr!=null){
							for(int j=0;j<classtable[i].length;j++){
								GridPointGroup targp = dayarr[j];
								if(targp!=null){
									boolean isMergeC = false;
									for(GridPoint gp:targp.getPointList()){
										if(gp.getMcGroupId()!=null&&gp.getMcGroupId().trim().length()>0){
											isMergeC = true;
											break;
										}
									}
									if(isMergeC){
										continue;
									}
									poss.add(i+","+j);
								}
							}
						}
					}
				}
				
				//B与第三方C尝试交换
				boolean canswap = false;
				
				for(String po:poss){
					int tday = Integer.parseInt(po.split(",")[0]);
					int tlesson = Integer.parseInt(po.split(",")[1]);
					if(tday==day&&tlesson==lesson){
						continue;
					}
					schClass = this.getScheduleClassByClassId(task.getClassId());
					classtable = schClass.getGridPointArr();
					GridPointGroup tgp = classtable[tday][tlesson];
					sourcegp = classtable[day][lesson];
					if(tgp!=null&&tgp.getPointList().size()!=0){
						if(scheduleRule.canSwapPosition(sourcegp, tgp, task.getClassId(), this)){
							//交换B与C
							tgp = classtable[tday][tlesson];
							sourcegp = classtable[day][lesson];
							List<GridPoint> srcList = new ArrayList<GridPoint>();
							List<GridPoint> tarList = new ArrayList<GridPoint>();
							for(GridPoint gpoint:sourcegp.getPointList()){
								srcList.add(gpoint);
							}
							for(GridPoint gpoint:tgp.getPointList()){
								tarList.add(gpoint);
							}
							for(GridPoint gpoint:srcList){
								removeGridPoint(gpoint);
							}
							for(GridPoint gpoint:srcList){
								gpoint.setDay(tday);
								gpoint.setLesson(tlesson);
								addGridPoint(gpoint);
							}
							for(GridPoint gpoint:tarList){
								removeGridPoint(gpoint);
							}
							for(GridPoint gpoint:tarList){
								gpoint.setDay(day);
								gpoint.setLesson(lesson);
								addGridPoint(gpoint);
							}
							schClass = this.getScheduleClassByClassId(task.getClassId());
							classtable = schClass.getGridPointArr();
							//交换A与C
							List<GridPoint> copyList = new ArrayList<GridPoint>();
							GridPointGroup gp =  classtable[day][lesson];
							List<GridPoint> gpointlist = gp .getPointList();
							for(GridPoint gpoint:gpointlist){
								copyList.add(gpoint);
							}
							
							if(gp!=null&&tasknum==1
									&&scheduleRule.canSwapPosition(task, gp, sday,slesson,0,this, day, false)){
								//先移除课程
								for(GridPoint gpoint:copyList){
									this.removeGridPoint(gpoint);
								}
								for(GridPoint gpoint:copyList){
									gpoint.setDay(sday);
									gpoint.setLesson(slesson);
									this.addGridPoint(gpoint);
								}
								
								//非连排课
								GridPoint gridPoint = new GridPoint();
								gridPoint.setAdvance(false);
								gridPoint.setArrangedNum(1);
								gridPoint.setClassId(task.getClassId());
								gridPoint.setCourseId(task.getCourseId());
								gridPoint.setCourseType(0);
								gridPoint.setDay(day);
								gridPoint.setGradeId(task.getGradeId());
								gridPoint.setGradeLevel(task.getGradeLevel());
								gridPoint.setLesson(lesson);
								gridPoint.setNeedCourseNumControl(false);
								gridPoint.setTaskGroupId(task.getTaskGroupId());
								List<String> teacherIds = classTaskTeacherMap.get(task.getClassId()+"_"+task.getCourseId());
								gridPoint.setTeacherIds(teacherIds );
								this.addGridPoint(gridPoint);
								
								System.out.println("-------"+task.getClassId()+"POS:"+day+","+lesson+":"+copyList.get(0).getCourseId()+"与"
										+sday+","+slesson+":"+task.getCourseId()+"【深度】交换成功");
								
								return true;
							}else{
								//B与C换回位置
								for(GridPoint gpoint:srcList){
									removeGridPoint(gpoint);
								}
								for(GridPoint gpoint:srcList){
									gpoint.setDay(day);
									gpoint.setLesson(lesson);
									addGridPoint(gpoint);
								}
								for(GridPoint gpoint:tarList){
									removeGridPoint(gpoint);
								}
								for(GridPoint gpoint:tarList){
									gpoint.setDay(tday);
									gpoint.setLesson(tlesson);
									addGridPoint(gpoint);
								}
								schClass = this.getScheduleClassByClassId(task.getClassId());
								classtable = schClass.getGridPointArr();
							}
						}
					}
					
					
					//ABC交换结束
				}
				
			}
		}
		
		return false;
	}
	/**
	 * 插入位置排课
	 * @param task
	 * @param bestPoss
	 * @param taskNum
	 * @param amNum
	 * @return
	 */
	public boolean insertPosition(ScheduleTask task, List<int[]> bestPoss,double taskNum, int amNum) {
		// TODO Auto-generated method stub
		
		if(taskNum==2){
			List<int[]> canArrangePos = new ArrayList<int[]>();
			for(int[] pos:bestPoss){
				int day = pos[0];
				int lesson = pos[1];
				if(taskNum==2&&scheduleRule.canArrangePosition(task, day, lesson,0,this,2)){
					canArrangePos.add(pos);
				}
			}
			String classId = task.getClassId();
			int[][] spPosition = getSpPositions(canArrangePos,amNum,classId );
			int spday = 0;
			if(spPosition!=null&&spPosition.length>0){
				for(int i=0;i<spPosition.length;i++){
					int[] pos = spPosition[i];
					int day = pos[0];
					spday = day;
					int lesson = pos[1];
					GridPoint gridPoint = new GridPoint();
					gridPoint.setAdvance(false);
					gridPoint.setArrangedNum(1);
					gridPoint.setClassId(task.getClassId());
					gridPoint.setCourseId(task.getCourseId());
					gridPoint.setCourseType(0);
					gridPoint.setDay(day);
					gridPoint.setGradeId(task.getGradeId());
					gridPoint.setGradeLevel(task.getGradeLevel());
					gridPoint.setLesson(lesson);
					gridPoint.setNeedCourseNumControl(false);
					gridPoint.setTaskGroupId(task.getTaskGroupId());
					gridPoint.setSpPoint(true);
					List<String> teacherIds = classTaskTeacherMap.get(task.getClassId()+"_"+task.getCourseId());
					gridPoint.setTeacherIds(teacherIds );
					this.addGridPoint(gridPoint);
				}
				ScheduleClass schClass = this.getScheduleClassByClassId(classId);
				schClass.addSpNumOnDay(spday);
				return true;
			}
		}else{
			if(bestPoss.size()==0){
				return false;
			}
			int[] pos = bestPoss.get((int)(Math.random()*bestPoss.size()));
			int day = pos[0];
			int lesson = pos[1];
			if((taskNum==1||taskNum==0.5)&&scheduleRule.canArrangePosition(task, day, lesson,0,this,taskNum)){
				//非连排课
				GridPoint gridPoint = new GridPoint();
				gridPoint.setAdvance(false);
				gridPoint.setArrangedNum(taskNum);
				gridPoint.setClassId(task.getClassId());
				gridPoint.setCourseId(task.getCourseId());
				int ctype = 0;
				if(taskNum==0.5){
					ctype = 1;
				}
				gridPoint.setCourseType(ctype);
				gridPoint.setDay(day);
				gridPoint.setGradeId(task.getGradeId());
				gridPoint.setGradeLevel(task.getGradeLevel());
				gridPoint.setLesson(lesson);
				gridPoint.setNeedCourseNumControl(false);
				gridPoint.setTaskGroupId(task.getTaskGroupId());
				List<String> teacherIds = classTaskTeacherMap.get(task.getClassId()+"_"+task.getCourseId());
				
				gridPoint.setTeacherIds(teacherIds );
				this.addGridPoint(gridPoint);
				return true;
			}
		}
		return false;
	}
	/**
	 * 插入位置排课--凑连排
	 * @param task
	 * @param bestPoss
	 * @param taskNum
	 * @param amNum
	 * @return
	 */
	public boolean insertPositionPairSp(ScheduleTask task, List<int[]> bestPoss,double taskNum, int amNum) {
		// TODO Auto-generated method stub
		
		if(bestPoss.size()==0){
			return false;
		}
		int[] pos = bestPoss.get((int)(Math.random()*bestPoss.size()));
		int day = pos[0];
		int lesson = pos[1];
		if((taskNum==1||taskNum==0.5)&&scheduleRule.canArrangePosition(task, day, lesson,0,this,taskNum)){
			//非连排课
			GridPoint gridPoint = new GridPoint();
			gridPoint.setAdvance(false);
			gridPoint.setArrangedNum(taskNum);
			gridPoint.setClassId(task.getClassId());
			gridPoint.setCourseId(task.getCourseId());
			int ctype = 0;
			if(taskNum==0.5){
				ctype = 1;
			}
			gridPoint.setCourseType(ctype);
			gridPoint.setDay(day);
			gridPoint.setGradeId(task.getGradeId());
			gridPoint.setGradeLevel(task.getGradeLevel());
			gridPoint.setLesson(lesson);
			gridPoint.setNeedCourseNumControl(false);
			gridPoint.setTaskGroupId(task.getTaskGroupId());
			gridPoint.setSpPoint(true);
			List<String> teacherIds = classTaskTeacherMap.get(task.getClassId()+"_"+task.getCourseId());
			gridPoint.setTeacherIds(teacherIds );
			this.addGridPoint(gridPoint);
			ScheduleClass schClass = this.getScheduleClassByClassId(task.getClassId());
			schClass.addSpNumOnDay(day);
			
			task.setHasSpNum(task.getHasSpNum()+1);
			task.setArrangedTaskNum(task.getArrangedTaskNum()+1);
			return true;
		}
		return false;
	}

	/**
	 * 根据可排位置获取连排位置
	 * @param canArrangePos
	 * @param amNum
	 * @param classId 
	 * @return
	 */
	private int[][] getSpPositions(List<int[]> canArrangePos, int amNum, String classId) {
		// TODO Auto-generated method stub
		ScheduleClass schClass = this.getScheduleClassByClassId(classId);
		
		if(canArrangePos.size()>1&&schClass!=null){
			int[][] sp = new int[2][2];
			for(int i=0;i<canArrangePos.size()-1;i++){
				int[] posNow = canArrangePos.get(i);
				int[] posNext = canArrangePos.get(i+1);
				if(posNow[0]==posNext[0]&&schClass.canArrangeSpOnDay(posNow[0])){
					int nowl = posNow[1];
					int nown = posNext[1];
					if((nowl<amNum&&nown<amNum&&nowl==nown-1)||(nowl>=amNum&&nown>=amNum&&nowl==nown-1)){
						sp[0] = posNow;
						sp[1] = posNext;
						return sp;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 相邻是否有课程
	 * @param courseId
	 * @param classtable
	 * @param day
	 * @param lesson
	 * @return
	 */
	private boolean hasCourseNearBy(String courseId,
			GridPointGroup[][] classtable, int day, int lesson) {
		// TODO Auto-generated method stub
		boolean has = false;
		if(day>0){
			GridPointGroup gp = classtable[day-1][lesson];
			if(gp!=null){
				List<GridPoint> courses = gp.getPointList();
				if(courses!=null&&courses.size()>0){
					for(GridPoint p:courses){
						if(p.getCourseId().equalsIgnoreCase(courseId)){
							return true;
						}
					}
				}
			}
		}
		if(day<maxDays-1){
			GridPointGroup gp = classtable[day+1][lesson];
			if(gp!=null){
				List<GridPoint> courses = gp.getPointList();
				if(courses!=null&&courses.size()>0){
					for(GridPoint p:courses){
						if(p.getCourseId().equalsIgnoreCase(courseId)){
							return true;
						}
					}
				}
			}
		}
		
		return has;
	}

	/**
	 * 加课 记录相关信息
	 * @param gridPoint
	 */
	public void addGridPoint(GridPoint gridPoint){
		
		if(gridPoint.getTeacherIds()!=null){
			for(String tid:gridPoint.getTeacherIds()){
				String tkey = tid +"_" +gridPoint.getDay();
				if(finishTeacherLessons.containsKey(tkey)){
					finishTeacherLessons.get(tkey).add(gridPoint);
				}else{
					List<GridPoint> list = new ArrayList<GridPoint>();
					list.add(gridPoint);
					finishTeacherLessons.put(tkey, list);
				}
			}
		}
		
		finishLessons.add(gridPoint);
		HashMap<Integer, GridPointGroup> dayMap = groupPointMap.get(gridPoint.getDay());
		if(dayMap==null){
			dayMap= new HashMap<Integer, GridPointGroup>();
			groupPointMap.put(gridPoint.getDay(),dayMap);
		}
		GridPointGroup gp = dayMap.get(gridPoint.getLesson());
		if(gp==null){
			gp = new GridPointGroup();
			gp.setDay(gridPoint.getDay());
			gp.setGradeId(gridPoint.getGradeId());
			gp.setGradeLevel(gridPoint.getGradeLevel());
			gp.setLesson(gridPoint.getLesson());
			dayMap.put(gridPoint.getLesson(), gp);
		}
		gp.addClassPointMap(gridPoint);
		
		List<GridPoint> finilist = finishGroupPoints.get(gridPoint.getClassId());
		if(finilist==null){
			finilist = new ArrayList<GridPoint>();
			finishGroupPoints.put(gridPoint.getClassId(), finilist);
		}
		finilist.add(gridPoint);
		
		//开始移除班级内的课程
		ScheduleClass schClass = this.getScheduleClassByClassId(gridPoint.getClassId());
		if(schClass!=null){
			schClass.addGridPoint(gridPoint);
		}
		
		scheduleRule.addArrangedPosition(gridPoint.getClassId(), gridPoint.getCourseId(),
				gridPoint.getDay(), gridPoint.getLesson(), gridPoint.getTeacherIds(), 
				true, gridPoint.getCourseType());
	}
	/**
	 * 移除课程 记录相关信息
	 * @param gridPoint
	 */
	public void removeGridPoint(GridPoint gridPoint){
		// 
		if(gridPoint.getTeacherIds()!=null){
			for(String tid:gridPoint.getTeacherIds()){
				String tkey = tid +"_" +gridPoint.getDay();
				if(finishTeacherLessons.containsKey(tkey)){
					finishTeacherLessons.get(tkey).remove(gridPoint);
				}
			}
		}
		finishLessons.remove(gridPoint);
		Collections.reverse(finishLessons);
		HashMap<Integer, GridPointGroup> dayMap = groupPointMap.get(gridPoint.getDay());
		if(dayMap!=null){
			GridPointGroup gp = dayMap.get(gridPoint.getLesson());
			if(gp!=null){
				gp.removeClassPointMap(gridPoint);
				if(gp.getPointList().size()==0){
					dayMap.remove(gridPoint.getLesson());
				}
			}
		}
		//开始移除班级内的课程
		ScheduleClass schClass = this.getScheduleClassByClassId(gridPoint.getClassId());
		if(schClass!=null){
			schClass.removeGridPoint(gridPoint);
		}
		List<GridPoint> finilist = finishGroupPoints.get(gridPoint.getClassId());
		if(finilist!=null){
			finilist.remove(gridPoint);
			Collections.reverse(finilist);
		}
		
		scheduleRule.removeArrangedPosition(gridPoint.getClassId(), gridPoint.getCourseId(),
				gridPoint.getDay(), gridPoint.getLesson(), gridPoint.getTeacherIds(), 
				true, gridPoint.getCourseType());
	}

	public double getTotalPreNum() {
		return totalPreNum;
	}

	public void setTotalPreNum(double totalPreNum) {
		this.totalPreNum = totalPreNum;
	}

	public boolean isDeepSwap() {
		return deepSwap;
	}

	public void setDeepSwap(boolean deepSwap) {
		this.deepSwap = deepSwap;
	}

	public JSONObject getSingleDoubleCourseMap() {
		return SingleDoubleCourseMap;
	}

	public void setSingleDoubleCourseMap(JSONObject singleDoubleCourseMap) {
		SingleDoubleCourseMap = singleDoubleCourseMap;
	}
}
