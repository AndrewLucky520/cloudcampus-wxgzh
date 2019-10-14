package com.talkweb.timetable.arrangement.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.timetable.arrangement.algorithm.CourseGene;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;


/**
 * 班级排课模型
 *
 */
public class ArrangeClass {
	
	private static final String unitSize = null;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String gradeId;
	private String gradeName;
	private String classId;
	private String className;
	
	private int totalMaxDays;
	private int amLessonNum;
	private int pmLessonNum;
	private int totalMaxLesson;
	
	private boolean halfAtLastDay = false;

	private RuleConflict ruleConflict;
	
	private CourseGene[][] timetable;
	
	/**
	 * 班级总课程连排数
	 */
	private int maxNearNum = 0;
	public int getMaxNearNum() {
		return maxNearNum;
	}

	public void setMaxNearNum(int maxNearNum) {
		this.maxNearNum = maxNearNum;
	}

	/**
	 * 科目固排数
	 */
	private Map<String, Double> fixedCourseCount = new HashMap<String, Double>();
	/**
	 * 周次 进度数目
	 */
	private HashMap<Integer,Double> progressMap = new HashMap<Integer, Double>();
	
	public HashMap<Integer, Double> getProgressMap() {
		int sum = totalMaxDays*totalMaxLesson;
		if(halfAtLastDay){
			sum = sum - pmLessonNum;
		}
		int sumLes = 0;
		HashMap<Integer,Integer> dayLesMap = new HashMap<Integer, Integer>();
		for(int i=0;i<timetable.length;i++){
			
			for(int j=0;j<timetable[i].length;j++){
				if(timetable[i][j]!=null){
					if(dayLesMap.isEmpty()||!dayLesMap.containsKey(i)){
						dayLesMap.put(i,1);
					}else{
						dayLesMap.put(i,dayLesMap.get(i)+1);
					}
					sumLes++;
				}
				
				
			}
		}
		
		progressMap.put(-1, (double)sumLes/sum);
		
		for(Iterator<Integer> it = dayLesMap.keySet().iterator();it.hasNext();){
			
			int num = it.next();
			int leSum = dayLesMap.get(num);
			
			progressMap.put(num,(double) leSum/totalMaxLesson);
		}
		
		return progressMap;
	}

	
	public ArrangeClass(String gradeId, String classId, int totalMaxDays, int amLessonNum, int pmLessonNum, RuleConflict ruleConflict){
		this.gradeId = gradeId;
		this.classId = classId;
		this.totalMaxDays = totalMaxDays;
		this.amLessonNum = amLessonNum;
		this.pmLessonNum = pmLessonNum;
		
		this.totalMaxLesson = amLessonNum + pmLessonNum;
		
		//每班一个课表对象
		this.initTimetable();
		
		this.ruleConflict = ruleConflict;
		maxNearNum = 0;
		
	}
	
	public void initTimetable(){
		this.timetable = new CourseGene[this.totalMaxDays][this.totalMaxLesson];
	}
	
	public int availablePositions(){
		if(this.timetable==null){
			return 0;
		}
		int available = 0;
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++){
				CourseGene courseGene = timetable[i][j];
				if(courseGene!=null){
					available ++;
				}
				
				
			}
		}
		
		return available;
		
	}
	/**
	 * 已排课程数
	 * @param courseId
	 * @return
	 */
	public double arrangedCourses(String courseId){
		if(this.timetable==null){
			return 0;
		}
		
		double count = 0;
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++){
				CourseGene courseGene = timetable[i][j];
				//获取正常节次
				if(courseGene!=null && courseGene.getCourseId().equals(courseId)){
					count += courseGene.getLessonSize();
					
				}
				//获取单双周节次
				if(courseGene!=null && courseGene.getLessonSize()==0.5 &&courseGene.getDwCourseGene()!=null&& courseGene.getDwCourseGene().getCourseId().equals(courseId)){
					count += courseGene.getLessonSize();
				}
				
				
			}
		}
		
		return count;
		
	}
	
	private void recordFixedCourseOnce(String courseId, double num){
		if(fixedCourseCount.containsKey(courseId)){
			fixedCourseCount.put(courseId, fixedCourseCount.get(courseId)+num);
		}else{
			fixedCourseCount.put(courseId, num);
		}
	}
	
	private double getFixedCourseCount(String courseId){
		if(fixedCourseCount.containsKey(courseId)){
			return fixedCourseCount.get(courseId);
		}
		
		return 0;
	}
	/**
	 * 添加固排课
	 * @param genes
	 * @param courseGene
	 * @return
	 */
	public boolean addFixedCourse(List<Gene> genes, CourseGene courseGene){
		
		int day = courseGene.getDay();
		int lesson = courseGene.getLesson();
		String courseId = courseGene.getArrangeCourse().getCourseId();
		if(timetable[day][lesson]==null){
			courseGene.setFixed(true);
			timetable[day][lesson] = courseGene;
			genes.add(courseGene);
			this.recordFixedCourseOnce(courseId,1);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 判断是否合班课
	 * @param classId
	 * @param courseId
	 * @return
	 */
	public boolean isMergeCourse(String classId, String courseId){
		
		return ruleConflict.isMergeCourse(classId, courseId);
		
	}
	/**
	 *  取课程的合班信息
	 * @param courseId
	 * @return
	 */
	public List<String> getMergeClass(String classId,String courseId){
		
		return ruleConflict.getMergeClass(classId,courseId);
		
	}
	
	/**
	 * 排课
	 * @param config
	 * @param arrangeGrid
	 * @param genes
	 * @param arrangeCourse
	 * @return
	 * @throws InvalidConfigurationException
	 */
	public boolean addCourse(Configuration config, ArrangeGrid arrangeGrid, List<Gene> genes, ArrangeCourse arrangeCourse,int tryTimes) throws InvalidConfigurationException{
				
		if(this.timetable==null){
			return false;
		}
		//控制教学进度，控制连排次数，总课时数
		String classId = arrangeCourse.getClassId();
		double taskLessons = arrangeCourse.getTaskLessons();
		// 课程的连排次数（或连排），每次连排2节
		int unitSize = arrangeCourse.getUnitSize();
		int courseLevel = arrangeCourse.getCourseLevel();
		String courseId = arrangeCourse.getCourseId();
		
		if(courseId.equals("1145")&&arrangeCourse.getClassId().equals("10029")){
			System.out.println("break1145");
		}
		
		//taskLessons -= this.getFixedCourseCount(courseId);
		//取当前课程剩余的未安排数量
		taskLessons -= this.arrangedCourses(courseId);
		
		if(taskLessons<=0){
			return true;
		}
		//已连排次数
		int nearNum = this.getNearCourseNum(arrangeCourse);
		//需要连排的次数
		nearNum = unitSize - nearNum;
		//循环 将一门课安排完
		while (taskLessons > 0) {
			boolean result = false;
//			if(classId.equals("10026"))
//			this.printTimetable();
			//非连排课
			if(nearNum<=0) {
				
				result = insertPosition(config, genes, arrangeCourse, 1, taskLessons,tryTimes);
//				
//				if(!result) {
//					arrangeGrid.addErrorInfo("班级："+classId+" 课程："+arrangeCourse.getCourseName()+" 安排未完全成功 未排课时数：" + taskLessons);
//
//				}
				
				taskLessons--;
				continue;
			}
			//连排课
			if(nearNum>0) {
				
				result = insertPosition(config, genes, arrangeCourse, 2, taskLessons,tryTimes);
//						
//				if(!result) {
//					arrangeGrid.addErrorInfo("班级："+classId+" 课程："+arrangeCourse.getCourseName()+" 安排未完全成功，未排课时数：" + taskLessons);
//					
//				}
				
				taskLessons-=2;
				nearNum--;

			}
			
		}
		
		return false;
		
	}
	
	/**
	 * 针对一个教学任务中的科目进行排课
	 * @param config
	 * @param genes
	 * @param arrangeCourse
	 * @param unitSize	连排数量
	 * @param cursor
	 * @return
	 * @throws InvalidConfigurationException
	 */
	private boolean insertPosition(Configuration config, List<Gene> genes, ArrangeCourse arrangeCourse, int unitSize, double cursor,int tryTimes) throws InvalidConfigurationException {
		boolean result = false;
		/**
		 * 课程级别  0：主课，1：普课，2：辅课
		 */
		int courseLevel = arrangeCourse.getCourseLevel();
		if(courseLevel==0 || courseLevel==1) {
			result = insertAmPosition(config, genes, arrangeCourse, unitSize, cursor,tryTimes);
		}else if(courseLevel==2) {
			//副课排在下午
			result = insertPmPosition(config, genes, arrangeCourse, unitSize, cursor,tryTimes);
		}
		
		if(!result){
			result = insertBestPosition(config, genes, arrangeCourse, unitSize, cursor,tryTimes);
		}
		
		if(unitSize==1){
			if(!result){
				result = insertAnyNoConflict(config, genes, arrangeCourse, cursor,tryTimes);
			}
			if(!result){
				result = insertAnyAvailableSync(config, genes, arrangeCourse, cursor,tryTimes);
			}
			if(!result){
				result = insertSwapPosition(config, genes, arrangeCourse, cursor,tryTimes);
			}
//			if(!result){
//				result = insertAnyAvailable(config, genes, arrangeCourse, cursor);
//			}
		}
		if(!result){
			System.out.println("未成功安排班级"+this.getClassName()+";"+arrangeCourse.getCourseName());
		}
		return result;
		
	}
	
	/**
	 * 该时段教师是否已安排课程 true为已安排 可以不判断双周的
	 * @param genes
	 * @param teacherIds
	 * @param day
	 * @param lesson
	 * @return
	 */
	public boolean hasCourseByTeacher(List<Gene> genes,ArrangeCourse arrangeCourse, int day, int lesson,int tryTimes){
		
		//测试方法
		if(!isAvgDistribute( day, lesson,  genes,arrangeCourse,tryTimes)){
			return true;
		}
		Set<String> teacherIds = arrangeCourse.getArrangeTeachers().keySet();
		for (Gene gene : genes) {
			CourseGene courseGene = (CourseGene) gene;
			if(courseGene.getDay()==day && courseGene.getLesson()==lesson){
				for(String teacherId : teacherIds){
				if(teacherId!=null&&courseGene.getArrangeCourse().getArrangeTeachers().containsKey(teacherId)){
					return true;
				}
				}
			}
		}
		return false;
	}
	/**
	 * 最佳适应位置
	 * @param arrangeCourse
	 * @param day
	 * @param lesson
	 * @return
	 */
	public boolean bestFitnessPosition(ArrangeCourse arrangeCourse, int day, int lesson){
		String courseId = arrangeCourse.getCourseId();
		if(day>0 && timetable[day-1][lesson]!=null &&  timetable[day-1][lesson].getCourseId().equals(courseId)){
			return false;
		}
		
		if(day!=this.totalMaxDays-1 && timetable[day+1][lesson]!=null &&  timetable[day+1][lesson].getCourseId().equals(courseId)){
			return false;
		}
		
		return true;
		
		
	}
	/**
	 * 获取已连排次数
	 * @param arrangeCourse
	 * @return
	 */
	public int getNearCourseNum(ArrangeCourse arrangeCourse) {
		String nearCourseCode = this.getNearCourseCode(arrangeCourse);
		int num = StringUtils.countMatches(nearCourseCode, "11");
		return num;
	}
	/**
	 * 获取连排课程排课代码
	 * @param arrangeCourse 要查的科目安排
	 * @return
	 */
	public String getNearCourseCode(ArrangeCourse arrangeCourse) {
		String classId = arrangeCourse.getClassId();
		String courseId = arrangeCourse.getCourseId();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++) {
				
				if(timetable[i][j]!=null) {
					CourseGene courseGene = timetable[i][j];
					if(courseGene.getClassId().equals(classId) && courseGene.getCourseId().equals(courseId)) {
						stringBuilder.append("1");
					}else{
						stringBuilder.append("0");
					}
				}else {
					stringBuilder.append("0");
					
				}
				
				if(j == this.amLessonNum-1 ){
					stringBuilder.append(",");
				}
				
			}
			
			stringBuilder.append("I");
			
		}
				
		return stringBuilder.toString();
		
	}
	
	/**
	 * 获取可排位置代码
	 * @param genes
	 * @param arrangeCourse
	 * @return
	 */
	public String getAvailablePositionCode(List<Gene> genes, ArrangeCourse arrangeCourse,int tryTimes) {
		String courseId = arrangeCourse.getCourseId();
		int courseLevel = arrangeCourse.getCourseLevel();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++) {
				
				if(halfAtLastDay && i==totalMaxDays-1 && j>=this.amLessonNum){
					stringBuilder.append("1");
					continue;
				}
				
				if(j<2 && courseLevel==2){
					stringBuilder.append("1");
					continue;
				}
				
				if(courseLevel==0 && j==totalMaxLesson-1){
					stringBuilder.append("1");
					continue;
				}
				
				if(timetable[i][j]==null) {
					boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse, i, j, tryTimes);
//					int canArrangeCourse = this.ruleConflict.canArrangeCourse(courseId, i, j,arrangeCourse.getGradeLev());
//					int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(arrangeCourse.getArrangeTeachers(), i, j,-1,null);
//					boolean canArrangeGround = this.ruleConflict.canArrangeGround(gradeId, courseId, i, j,-1);
//					if(hasCourseByTeacher || canArrangeCourse==0 || canArrangeTeacher ==0 || !canArrangeGround) {
//						stringBuilder.append("1");
//					}else{
//						stringBuilder.append("0");
//					}
				}else {
					stringBuilder.append("1");
					
				}
				
				if(j == this.amLessonNum-1 ){
					stringBuilder.append(",");
				}
				
			}
			
			stringBuilder.append("I");
			
		}
		
		return stringBuilder.toString();
		
	}
	


	/**
	 * 获取上午可排代码
	 * @param genes
	 * @param arrangeCourse
	 * @return
	 */
	public String getAmAvailableCode(List<Gene> genes, ArrangeCourse arrangeCourse,int tryTimes) {
		String courseId = arrangeCourse.getCourseId();
		int courseLevel = arrangeCourse.getCourseLevel();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++) {

//				if(j<2 && courseLevel==2){
//					stringBuilder.append("1");
//					continue;
//				}
				
				if(j < this.amLessonNum){
					if(timetable[i][j]==null) {
						boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , i, j,tryTimes);
//						int canArrangeCourse = this.ruleConflict.canArrangeCourse(courseId, i, j,arrangeCourse.getGradeLev());
//						int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(arrangeCourse.getArrangeTeachers(), i, j,-1,null);
//						boolean canArrangeGround = this.ruleConflict.canArrangeGround(gradeId, courseId, i, j,-1);
//						if(courseLevel==2 && j<2){
//							//辅助课程不允许排上午的前面2节
//							stringBuilder.append("1");
//						}else if(hasCourseByTeacher || canArrangeCourse==0 || canArrangeTeacher ==0||!canArrangeGround) {
//							stringBuilder.append("1");
//						}else{
//							stringBuilder.append("0");
//						}
						//
						
					}else {						
						stringBuilder.append("1");
						
					}
				}
				
			}
			//分隔周次
			stringBuilder.append("I");
			
		}
		
		return stringBuilder.toString();
		
	}
	
	public String getPmAvailableCode(List<Gene> genes, ArrangeCourse arrangeCourse,int tryTimes) {
		String courseId = arrangeCourse.getCourseId();
		int courseLevel = arrangeCourse.getCourseLevel();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++) {

				if(j >= this.amLessonNum){
					
					if(halfAtLastDay && i==totalMaxDays-1){
						stringBuilder.append("1");
						continue;
					}
					
					if(courseLevel==0 && j==totalMaxLesson-1){
						stringBuilder.append("1");
						continue;
					}
					
					if(timetable[i][j]==null) {
						boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , i, j,tryTimes);
//						int canArrangeCourse = this.ruleConflict.canArrangeCourse(courseId, i, j,arrangeCourse.getGradeLev());
//						int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(arrangeCourse.getArrangeTeachers(), i, j,-1,null);
//						boolean onlyForMergeCourse = this.ruleConflict.onlyForMergeCourse(genes, courseId, courseId, i, j);
//						boolean canArrangeGround = this.ruleConflict.canArrangeGround(gradeId, courseId, i, j,-1);
//						if(onlyForMergeCourse){
//							stringBuilder.append("1");
//						}else	if(hasCourseByTeacher || canArrangeCourse==0 || canArrangeTeacher ==0||!canArrangeGround) {
//							stringBuilder.append("1");
//						}else{
//							stringBuilder.append("0");
//						}
					}else {
						stringBuilder.append("1");
						
					}
				}
				
			}
			
			stringBuilder.append("I");
			
		}
		
		return stringBuilder.toString();
		
	}
	/**
	 * 单周返回0.5 否则返回1
	 * @param cursor
	 * @return
	 */
	public double getLessonSize(double cursor){
		return cursor==0.5 ? cursor : 1;
	}
	

	/**
	 * 尽量安排到上午的课程
	 * @param config
	 * @param genes
	 * @param arrangeCourse
	 * @param unitSize	要安排的课程数
	 * @param cursor	剩余要安排的课程数
	 * @return 是否已安排
	 * @throws InvalidConfigurationException
	 */
	public boolean insertAmPosition(Configuration config, List<Gene> genes, ArrangeCourse arrangeCourse, int unitSize, double cursor,int tryTimes) throws InvalidConfigurationException {
		String courseId = arrangeCourse.getCourseId();
		double taskLessons = arrangeCourse.getTaskLessons();
		String availableCode = this.getAmAvailableCode(genes, arrangeCourse,tryTimes);
		//int unitSize = arrangeCourse.getUnitSize();
		StringBuilder pCode = new StringBuilder();
		for(int i = 0;i<unitSize;i++){
			pCode.append("0");
		}
		String _pCod = pCode.toString();
		if(!availableCode.contains(_pCod)){
			return false;
		}
		
		//查找是否有空位置
		int day = -1;
		
		int startLesson = -1;
		String[] dayCodes = availableCode.split("I");
		for (String dayCode : dayCodes) {
			day++;
			// 连排先从周三开始
			if(tryTimes<3&&unitSize==2){
				if(day<1){
					continue;
				}
			}
			if(dayCode.contains(_pCod) && day < this.totalMaxDays ){
				//可以放置的空位
				startLesson = dayCode.indexOf(_pCod);
				if(bestFitnessPosition(arrangeCourse, day, startLesson)
						&&isTeachTaskSync(genes,arrangeCourse, day, startLesson,unitSize,cursor)
						&& !hasCourseOnDay(day, startLesson,arrangeCourse)){
					break;
				}else{
					boolean hasFinded = false;
					//无合适的空位，找下一个
					while(unitSize==1 && startLesson<this.amLessonNum-1 && startLesson < dayCode.length()-1 &&dayCode.charAt(startLesson+1) == '0'){
							
						startLesson ++;
						if(startLesson<this.amLessonNum 
								&& bestFitnessPosition(arrangeCourse, day, startLesson)
								&& !hasCourseOnDay(day, startLesson,arrangeCourse)){
							boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , day, startLesson,tryTimes);
							if(!hasCourseByTeacher){
								boolean isTeachTaskSync = this.isTeachTaskSync(genes,arrangeCourse, day, startLesson,unitSize,cursor);
								if(isTeachTaskSync){
									hasFinded = true;
									break;
								}
							}
						}
						
						
					}
					
					if(hasFinded){
						break;
					}else{
						startLesson = -1;
					}
				}
				
				
			}
		}
		
		if(day >= this.totalMaxDays){
			return false;
		}
		
		
		if(startLesson>=0) {
			for (int j = 0; j < unitSize; j++) {
				int lesson = startLesson + j;
				CourseGene courseGene = new CourseGene(config, arrangeCourse);
				courseGene.setClassId(classId);
				courseGene.setDay(day);
				courseGene.setLesson(lesson);
				courseGene.setNearNum(unitSize);
				courseGene.setLessonSize(this.getLessonSize(cursor));
				timetable[day][lesson] = courseGene;
				genes.add(courseGene);
				if(courseId.equals("1136"))
				//if((classId.equals("10026") || classId.equals("10027")) && (courseId.equals("1") || courseId.equals("2")))
				logger.debug("insertAmPosition 排课成功，ClassId：{} courseId：{} Day：{} Lesson：{} ", classId, courseId, day, lesson);
				//记录课程位置 用于同步教学任务
				arrageSyncPos(arrangeCourse, day, lesson, cursor);
			}
			
			return true;
		}
		
		
		return false;
	}
	/**
	 * 判断教学任务是否一致
	 * @param genes
	 * @param arrangeCourse
	 * @param day	安排周次
	 * @param startLesson	安排节次
	 * @param unitSize 要安排的课程数
	 * @param cursor 
	 * @return
	 */
	public boolean isTeachTaskSync(List<Gene> genes,
			ArrangeCourse arrangeCourse, int day, int startLesson, int unitSize, double cursor) {
		// TODO Auto-generated method stub
		
		
		//测试代码
//		
//		int cLev = arrangeCourse.getCourseLevel();	
		double taskLesson = arrangeCourse.getTaskLessons();
		for(int i=0;i<timetable.length;i++){
			for(int j=0;j<timetable[i].length&&timetable[i][j]!=null;j++){
				CourseGene cg = timetable[i][j];
				if(cg.getCourseId().equalsIgnoreCase(arrangeCourse.getCourseId())){
					taskLesson = taskLesson - cg.getLessonSize();
				}
			}
		}
		
		cursor = taskLesson;
//		if(taskLesson>=4.0){
//			return true;
//		}
		
		//如果是连排课 不做进度控制  
		//已连排次数
		int hasNearNum = this.getNearCourseNum(arrangeCourse);
		//需要连排的次数
//		int nearNum = unitSize - hasNearNum;
		if(unitSize==2){
			//如果班级最大连排数不大于 总天数 则控制其一天只连排一次
			if(maxNearNum<=totalMaxDays){
				CourseGene[] cday = this.timetable[day];
				for(int i=0;i<cday.length-1;i++){
					CourseGene cg = cday[i];
					CourseGene cg2 = cday[i+1];
					if(cg!=null&&cg2!=null){
						if(cg.getCourseId().equalsIgnoreCase(cg2.getCourseId())){
							return false;
						}
					}
				}
			}
			
			return true;
		}
		if(arrangeCourse.getUnitSize()>0){
			return true;
		}
		
		if(day<totalMaxDays-1){
			if(isProgressTooFast(day)){
				return false;
			}
		}
		String gradeId = arrangeCourse.getGradeId();
		String courseId = arrangeCourse.getCourseId();
		String classId = arrangeCourse.getClassId();
		for(ArrangeTeacher art : arrangeCourse.getArrangeTeachers().values()){
			if(art.getTeacherId()==null){
				continue;
			}
			float pt = art.getArragedPercent(gradeId, courseId,classId);
			if(pt>0.0){
				Map<Double, Integer[][]> map = art.getArragedPosition(gradeId, courseId,classId);
				//当前课程小于下一课程
				double key = cursor - unitSize;
				if(map.containsKey(key)&&map.get(key)!=null){
					
					Integer[][] crs = map.get(key);
					for(int i=0;i<crs.length;i++){
						Integer[] ckrs = crs[i];
						if(ckrs!=null&&ckrs.length>0&&ckrs[0]!=null){
							int otherClassDay = ckrs[0];
							int otherClassLesson = ckrs[1];
							//如果当前课程日安排大于 其它班级下一节次课程安排|| 当前课程日安排相同但节次大于等于下一节次 也不行
							if(day>otherClassDay||(day==otherClassDay&&startLesson>=otherClassLesson)){
								return false;
							}
						}
					}
				}
				//当前课程大于上一课程
				double key2 = cursor + unitSize;
				if(map.containsKey(key2)&&map.get(key2)!=null){
					
					Integer[][] crs = map.get(key2);
					for(int i=0;i<crs.length;i++){
						Integer[] ckrs = crs[i];
						if(ckrs!=null&&ckrs.length>0&&ckrs[0]!=null){
							int otherClassDay = ckrs[0];
							int otherClassLesson = ckrs[1];
							//如果当前课程日安排小于 其它班级下一节次课程安排|| 当前课程日安排相同但节次小于等于下一节次 也不行
							if(day<otherClassDay||(day==otherClassDay&&startLesson<=otherClassLesson)){
								return false;
							}
						}
					}
				}
			}else if(pt==0.0){
				double tl = arrangeCourse.getTaskLessons();
				if(tl<4){
					Map<Double, Integer[][]> map = art.getCurArragedPosition(gradeId, courseId,classId);
					//当前课程大于上一课程
					double key2 = cursor + unitSize;
					if(map.containsKey(key2)&&map.get(key2)!=null){
						Integer[][] crs = map.get(key2);
						for(Integer i=0;i<crs.length;i++){
							Integer[] ckrs = crs[i];
							if(ckrs!=null&&ckrs.length>0&&ckrs[0]!=null){
								int otherClassDay = ckrs[0];
								int otherClassLesson = ckrs[1];
								
								if((day*totalMaxLesson+startLesson)-(otherClassDay*totalMaxLesson+otherClassLesson)<totalMaxLesson){
									return false;
								}
							}
						}
					}
				}
			}
			
		}
		return true;
	}
	/**
	 * 判断班级整体排课进度 如果进度太快 则不允许排当天
	 * @param day
	 * @return
	 */
	private boolean isProgressTooFast(int day) {
		progressMap = this.getProgressMap();
		// TODO Auto-generated method stub
		double sumPro = progressMap.get(-1);
		double curPro = 0;
		if(progressMap.containsKey(day)){
			curPro = progressMap.get(day);
		}
		if(curPro<0.2||sumPro>0.7){
			return false;
		}
		if(day<4){
			if(curPro+0.2>sumPro){
				return true;
			}
		}else{
			if(curPro>sumPro+0.5){
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断教学任务是否一致
	 * @param genes
	 * @param arrangeCourse
	 * @param day	安排周次
	 * @param startLesson	安排节次
	 * @param unitSize 要安排的课程数
	 * @param cursor 
	 * @return
	 */
	public boolean isTeachTaskSync2(List<Gene> genes,
			ArrangeCourse arrangeCourse, int day, int startLesson, int unitSize, double cursor) {
		// TODO Auto-generated method stub
		//如果是连排课 不做进度控制
		int neerNum  = arrangeCourse.getUnitSize();
		if(neerNum>0){
			return true;
		}
		String gradeId = arrangeCourse.getGradeId();
		String courseId = arrangeCourse.getCourseId();
		String classId = arrangeCourse.getClassId();
		for(ArrangeTeacher art : arrangeCourse.getArrangeTeachers().values()){
			if(art.getTeacherId()==null){
				continue;
			}
			float pt = art.getArragedPercent(gradeId, courseId,classId);
			if(pt>0.0){
				Map<Double, Integer[][]> map = art.getArragedPosition(gradeId, courseId,classId);
				//当前课程小于下一课程
				double key = cursor - unitSize;
				if(map.containsKey(key)&&map.get(key)!=null){
					
					Integer[][] crs = map.get(key);
					for(int i=0;i<crs.length;i++){
						Integer[] ckrs = crs[i];
						if(ckrs!=null&&ckrs.length>0&&ckrs[0]!=null){
							int otherClassDay = ckrs[0];
							int otherClassLesson = ckrs[1];
							//如果当前课程日安排大于 其它班级下一节次课程安排|| 当前课程日安排相同但节次大于等于下一节次 也不行
							if(day>otherClassDay||(day==otherClassDay&&startLesson>=otherClassLesson)){
								return false;
							}
						}
					}
				}
				//当前课程大于上一课程
				double key2 = cursor + unitSize;
				if(map.containsKey(key2)&&map.get(key2)!=null){
					
					Integer[][] crs = map.get(key2);
					for(int i=0;i<crs.length;i++){
						Integer[] ckrs = crs[i];
						if(ckrs!=null&&ckrs.length>0&&ckrs[0]!=null){
							int otherClassDay = ckrs[0];
							int otherClassLesson = ckrs[1];
							//如果当前课程日安排小于 其它班级下一节次课程安排|| 当前课程日安排相同但节次小于等于下一节次 也不行
							if(day<otherClassDay||(day==otherClassDay&&startLesson<=otherClassLesson)){
								return false;
							}
						}
					}
				}
			}			
		}
		return true;
	}
	/**
	 * 通过重构班级教学序列来判断教学任务是否一致 -用于交换课程
	 * @param genes
	 * @param arrangeCourse
	 * @param day	安排周次
	 * @param startLesson	安排节次
	 * @param unitSize 要安排的课程数
	 * @param cursor 
	 * @return
	 */
	public boolean isTeachTaskSyncRebuild(List<Gene> genes,
			ArrangeCourse arrangeCourse, int orday, int orlesson,  int day, int startLesson, int unitSize, double cursor) {
//		if(isTeachTaskSync(genes,arrangeCourse,day,startLesson,unitSize,cursor)){
//			return true;
//		}
		if(arrangeCourse.getCourseId().equalsIgnoreCase("1145")&&arrangeCourse.getClassId().equalsIgnoreCase("10029")&&day==1&&startLesson==5){
			System.out.println("fsd");
		}
		String courseId = arrangeCourse.getCourseId();
		double taskLesson = arrangeCourse.getTaskLessons();
		//如果是连排课 不做进度控制
		int neerNum  = arrangeCourse.getUnitSize();
		if(neerNum>0){
			return true;
		}
		List<JSONObject> sortList = new ArrayList<JSONObject>();
		for(int i=0;i<timetable.length;i++){
			
			for(int j=0;j<timetable[i].length&&timetable[i][j]!=null;j++){
				CourseGene crs  = (CourseGene) timetable[i][j];
				if(crs.getCourseId().equalsIgnoreCase(courseId)
						&&!(crs.getDay()==orday&&crs.getLesson()==orlesson)){
					
					JSONObject obj = new JSONObject();
					obj.put("gen", crs);
					obj.put("isThis", false);
					obj.put("qz", crs.getDay()*totalMaxLesson+crs.getLesson());
					sortList.add(obj);
				}
			}
		}
		
		//增加当前对象到序列去判断
		JSONObject thisObj = new JSONObject();
		thisObj.put("gen", arrangeCourse);
		thisObj.put("qz", day*totalMaxLesson+startLesson);
		thisObj.put("isThis", true);
		sortList.add(thisObj);
		
		
		if(sortList.size()>0){
			ScoreUtil.sorStuScoreList(sortList, "qz", "asc", "", "");
			for(JSONObject obj :sortList){
				if(obj.getBooleanValue("isThis")){
					//当前处理移动课程
					boolean isTeachSync = isTeachTaskSync2(genes,arrangeCourse ,day,startLesson,unitSize,taskLesson);
					taskLesson -= unitSize;
					if(!isTeachSync){
						return false;
					}
				}else{
					//非当前移动课程
					CourseGene crs = (CourseGene) obj.get("gen");
					boolean isTeachSync = isTeachTaskSync2(genes,crs.getArrangeCourse(),crs.getDay(),crs.getLesson(),unitSize,taskLesson);
					
					taskLesson -= unitSize;
					
					if(!isTeachSync){
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 重置位置游标
	 * @param genes
	 * @param arrangeCourse
	 * @param day
	 * @param startLesson
	 * @param unitSize
	 * @param cursor
	 */
	public void arrageSyncPosRebuild(List<Gene> genes,
			ArrangeCourse arrangeCourse, int day, int lesson, int unitSize, double cursor) {
		String classId = arrangeCourse.getClassId();
		String courseId = arrangeCourse.getCourseId();
		double taskLesson = arrangeCourse.getTaskLessons();
//		this.ruleConflict.addArrangedPosition(classId, courseId, day, lesson, arrangeCourse.getArrangeTeachers().keySet());
		List<Gene> courseGens = new ArrayList<Gene>();
		List<JSONObject> sortList = new ArrayList<JSONObject>();
		for(int i=0;i<timetable.length;i++){
					
					for(int j=0;j<timetable[i].length&&timetable[i][j]!=null;j++){
						CourseGene crs  = (CourseGene) timetable[i][j];
						if(crs.getCourseId().equalsIgnoreCase(courseId) ){
								//&&!(crs.getDay()==day&&crs.getLesson()==startLesson)){
							
							JSONObject obj = new JSONObject();
							obj.put("gen", crs);
							obj.put("qz", crs.getDay()*totalMaxLesson+crs.getLesson());
							sortList.add(obj);
							
						}
					}
		}
		if(sortList.size()>0){
			ScoreUtil.sorStuScoreList(sortList, "qz", "asc", "", "");
			for(JSONObject obj :sortList){
				CourseGene crs = (CourseGene) obj.get("gen");
//				boolean isTeachSync = isTeachTaskSync2(genes,arrangeCourse,crs.getDay(),crs.getLesson(),unitSize,taskLesson);
				removeSyncPos(arrangeCourse,   taskLesson);
				arrageSyncPos(arrangeCourse, crs.getDay(), crs.getLesson(), taskLesson);
				
				taskLesson -= unitSize;
				
			}
		}
	}
	/**
	 * 记录课程位置
	 * @param arrangeCourse
	 * @param day
	 * @param lesson
	 * @param cursor
	 */
	public void arrageSyncPos(ArrangeCourse arrangeCourse,int day,int lesson,double cursor){
		String gradeId = arrangeCourse.getGradeId();
		String courseId = arrangeCourse.getCourseId();
//		this.ruleConflict.addArrangedPosition(classId, courseId, day, lesson, arrangeCourse.getArrangeTeachers().keySet());
		for(ArrangeTeacher art : arrangeCourse.getArrangeTeachers().values()){
			art.setArragedPosition(arrangeCourse.getClassId(),gradeId, courseId, cursor, day, lesson);
		}
	}
	/**
	 * 移除课程位置
	 * @param arrangeCourse
	 * @param day
	 * @param lesson
	 * @param cursor
	 */
	public void removeSyncPos(ArrangeCourse arrangeCourse,double cursor){
		String gradeId = arrangeCourse.getGradeId();
		String courseId = arrangeCourse.getCourseId();
		for(ArrangeTeacher art : arrangeCourse.getArrangeTeachers().values()){
//			art.setArragedPosition(arrangeCourse.getClassId(),gradeId, courseId, cursor, day, lesson);
			art.removeArragedPosition(this.getClassId(),gradeId, courseId, cursor);
		}
	}

	//尽量安排到下午的课程
	public boolean insertPmPosition(Configuration config, List<Gene> genes, ArrangeCourse arrangeCourse, int unitSize, double cursor,int tryTimes) throws InvalidConfigurationException {
		String courseId = arrangeCourse.getCourseId();
		String availableCode = this.getPmAvailableCode(genes, arrangeCourse,tryTimes);
		//int unitSize = arrangeCourse.getUnitSize();
		StringBuilder pCode = new StringBuilder();
		for(int i = 0;i<unitSize;i++){
			pCode.append("0");
		}
		String _pCod = pCode.toString();
		if(!availableCode.contains(_pCod)){
			return false;
		}
		
		//查找是否有空位置
		int day = -1;
		int startLesson = -1;
		String[] dayCodes = availableCode.split("I");
		for (String dayCode : dayCodes) {
			day++;			
			if(dayCode.contains(_pCod) && day < this.totalMaxDays ){
				//可以放置的空位
				startLesson = dayCode.indexOf(_pCod) + this.amLessonNum;
				if(bestFitnessPosition(arrangeCourse, day, startLesson)
						&&isTeachTaskSync(genes,arrangeCourse, day, startLesson,unitSize,cursor)
						&& !hasCourseOnDay(day, startLesson,arrangeCourse)){
					boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , day, startLesson,tryTimes);
					
					if(arrangeCourse.getCourseId().equalsIgnoreCase("1145")&&arrangeCourse.getClassId().equalsIgnoreCase("10029")&&day==1&&startLesson==5){
						System.out.println("fsd");
					}
					if(!hasCourseByTeacher){
						break;
					}
				}else{
					boolean hasFinded = false;
					startLesson = -1;
					//无合适的空位，找下一个
					while(unitSize==1 && startLesson<this.pmLessonNum -1 && startLesson < dayCode.length()-1  ){
						if(dayCode.charAt(startLesson+1)!= '0'){
							startLesson++;
							continue;
						}
							
						startLesson++;
						int startLesson1 = this.amLessonNum+startLesson;
						if(startLesson<this.pmLessonNum 
								&& bestFitnessPosition(arrangeCourse, day, startLesson1)
								&& !hasCourseOnDay(day, startLesson1,arrangeCourse)){
							boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , day, startLesson1,tryTimes);
							if(!hasCourseByTeacher){
								boolean isTeachTaskSync = this.isTeachTaskSync(genes,arrangeCourse, day, startLesson1,unitSize,cursor);
								if(isTeachTaskSync){
									hasFinded = true;
									break;
								}
							}
						}
						
						
					}
					
					if(hasFinded){
						break;
					}else{
						startLesson = -1;
					}
					
				}

			}
		}
		
		
		if(day >= this.totalMaxDays){
			return false;
		}
		
		
		
		if(startLesson>=0){
			for (int j = 0; j < unitSize; j++) {
				int lesson = startLesson + j;
				CourseGene courseGene = new CourseGene(config, arrangeCourse);
				courseGene.setClassId(classId);
				courseGene.setDay(day);
				courseGene.setLesson(lesson);
				courseGene.setNearNum(unitSize);
				courseGene.setLessonSize(this.getLessonSize(cursor));
				timetable[day][lesson] = courseGene;
				genes.add(courseGene);
				if(courseId.equals("1136"))
				//if((classId.equals("10026") || classId.equals("10027")) && (courseId.equals("1") || courseId.equals("2")))
				logger.debug("insertPmPosition 排课成功，ClassId：{} courseId：{} Day：{} Lesson：{} ", classId, courseId, day, lesson);
				//记录课程位置 用于同步教学任务
				arrageSyncPos(arrangeCourse, day, lesson, cursor);
			}
			
			return true;
		}
		
		
		return false;
	}
	
	/**
	 * 设法将课程安排到一个比较适合的位置
	 * @param config
	 * @param genes
	 * @param arrangeCourse
	 * @param unitSize
	 * @param cursor
	 * @return
	 * @throws InvalidConfigurationException
	 */
	public boolean insertBestPosition(Configuration config, List<Gene> genes, ArrangeCourse arrangeCourse, int unitSize, double cursor,int tryTimes) throws InvalidConfigurationException {
		String courseId = arrangeCourse.getCourseId();
		String availableCode = this.getAvailablePositionCode(genes, arrangeCourse,tryTimes);
		//int unitSize = arrangeCourse.getUnitSize();
		StringBuilder pCode = new StringBuilder();
		for(int i = 0;i<unitSize;i++){
			pCode.append("0");
		}
		String _pCod = pCode.toString();
		if(!availableCode.contains(_pCod)){
			return false;
		}
		
		
		//查找是否有空位置
		int day = -1;
		int startLesson = -1;
		String[] dayCodes = availableCode.split("I");
		for (String dayCode : dayCodes) {
			day++;
			if(dayCode.contains(_pCod) && day < this.totalMaxDays ){
				//可以放置的空位
				if(dayCode.indexOf(_pCod) < this.amLessonNum){
					//上午
					startLesson = dayCode.indexOf(_pCod);
				}else{
					//下午
					startLesson = dayCode.indexOf(_pCod)-1;
				}
				
				if(bestFitnessPosition(arrangeCourse, day, startLesson)
						&&isTeachTaskSync(genes,arrangeCourse, day, startLesson,unitSize,cursor)
						&& !hasCourseOnDay(day, startLesson,arrangeCourse)) {
					break;
				}else{
					boolean hasFinded = false;
					startLesson =0;
					//无合适的空位，找下一个
					int dt = 0;
					if(startLesson+1 >=this.amLessonNum){
						dt = 1;
					}
					while(unitSize==1 && startLesson < dayCode.length()-1 &&dayCode.charAt(startLesson+dt) == '0'){
//						if(dayCode.charAt(startLesson+dt) != '0'){
//							startLesson++;
//							continue;
//						}
						if(startLesson<=this.totalMaxLesson && bestFitnessPosition(arrangeCourse, day, startLesson-dt)){
							boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , day, startLesson-dt,tryTimes);
							if(!hasCourseByTeacher
									&&isTeachTaskSync(genes,arrangeCourse, day, startLesson-dt,unitSize,cursor)
									&& !hasCourseOnDay(day, startLesson-dt,arrangeCourse)){
								hasFinded = true;
								startLesson = startLesson-dt;
								break;
							}
						}
						startLesson ++;			
					}
					
					if(hasFinded){
						break;
					}else{
						startLesson = -1;
					}
					
				}
								
				
			}
		}
		
		if(day >= this.totalMaxDays){
			return false;
		}
		if(arrangeCourse.getCourseId().equalsIgnoreCase("1145")&&arrangeCourse.getClassId().equalsIgnoreCase("10029")&&day==1&&startLesson==5){
			System.out.println("fsd");
		}
		
		if(startLesson>=0){
			for (int j = 0; j < unitSize; j++) {
				int lesson = startLesson + j;
				CourseGene courseGene = new CourseGene(config, arrangeCourse);
				courseGene.setClassId(classId);
				courseGene.setDay(day);
				courseGene.setLesson(lesson);
				courseGene.setNearNum(unitSize);
				courseGene.setLessonSize(this.getLessonSize(cursor));
				timetable[day][lesson] = courseGene;
				genes.add(courseGene);
				if(courseId.equals("1136"))
				//if((classId.equals("10026") || classId.equals("10027")) && (courseId.equals("1") || courseId.equals("2")))
				
				//记录课程位置 用于同步教学任务
				logger.debug("insertBestPosition 排课成功，ClassId：{} courseId：{} Day：{} Lesson：{} ", classId, courseId, day, lesson);
				arrageSyncPos(arrangeCourse, day, lesson, cursor);	
			}
						
			return true;
		}
		
		
		return false;
	}
	
	
	//将课程安排到一个无冲突的可排位置
	public boolean insertAnyNoConflict(Configuration config, List<Gene> genes, ArrangeCourse arrangeCourse, double cursor,int tryTimes) throws InvalidConfigurationException {
		int courseLevel = arrangeCourse.getCourseLevel();
		String courseId = arrangeCourse.getCourseId();
		List<Integer[]> available = new ArrayList<Integer[]>();
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++) {
				if(courseLevel==2 && j<2){
					continue;
				}
				//最后一天半节课
				if(halfAtLastDay&&j>=pmLessonNum&&i==totalMaxDays-1){
					continue;
				}
				if(courseLevel==0 && j==totalMaxLesson-1){
					continue;
				}
				

				if(timetable[i][j]==null) {
					//
					boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , i, j,tryTimes);
//					int canArrangeCourse = this.ruleConflict.canArrangeCourse(courseId, i, j,arrangeCourse.getGradeLev());
//					int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(arrangeCourse.getArrangeTeachers(), i, j,-1,null);
//					boolean hasCourseOnDay = this.hasCourseOnDay(i,j, arrangeCourse);
//					boolean canArrangeGround = this.ruleConflict.canArrangeGround(gradeId, courseId, i, j,-1);
//					if(!hasCourseByTeacher && canArrangeCourse>0 && canArrangeTeacher >0 && !hasCourseOnDay
//							&&canArrangeGround) {
//						available.add(new Integer[]{i,j});
//						
//						
//					}
					
					
				}
				
			}

		}
		
		if(available.size()>0){
			for (Integer[] position : available) {
				int day = position[0];
				int lesson = position[1];
//				if(!isTeachTaskSync(genes,arrangeCourse, day, lesson,1,cursor)){
//					
//					continue;
//				}
				if((courseLevel==2 && lesson<2 )||!isTeachTaskSync(genes,arrangeCourse, day, lesson,1,cursor)){
					
					continue;
				}
				CourseGene courseGene = new CourseGene(config, arrangeCourse);
				courseGene.setClassId(classId);
				courseGene.setDay(day);
				courseGene.setLesson(lesson);
				courseGene.setNearNum(1);
				courseGene.setLessonSize(this.getLessonSize(cursor));
				timetable[day][lesson] = courseGene;
				genes.add(courseGene);
				
				if(courseId.equals("1136"))
					
				//记录课程位置 用于同步教学任务
				//if((classId.equals("10026") || classId.equals("10027")) && (courseId.equals("1") || courseId.equals("2")))
				logger.debug("insertAnyNoConflict 排课成功，ClassId：{} courseId：{} Day：{} Lesson：{} ", classId, courseId, day, lesson);
				
				arrageSyncPos(arrangeCourse, day, lesson, cursor);
				return true;
			}
			
		}
		return false;
		
	}
	
	//将课程安排到一个任意空位，有教师冲突和课程冲突的不放
	public boolean insertAnyAvailable(Configuration config, List<Gene> genes, ArrangeCourse arrangeCourse, double cursor,int tryTimes) throws InvalidConfigurationException {
		String courseId = arrangeCourse.getCourseId();
		List<Integer[]> available = new ArrayList<Integer[]>();
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++) {
				//最后一天半节课
				if(halfAtLastDay&&j>=pmLessonNum&&i==totalMaxDays-1){
					continue;
				}

				if(timetable[i][j]==null) {
					//
//					boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , i, j,tryTimes);
//					int canArrangeCourse = this.ruleConflict.canArrangeCourse(courseId, i, j,arrangeCourse.getGradeLev());
//					int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(arrangeCourse.getArrangeTeachers(), i, j,-1,null);
//					boolean canArrangeGround = this.ruleConflict.canArrangeGround(gradeId, courseId, i, j,-1);
//					if(!hasCourseByTeacher && canArrangeCourse>0 && canArrangeTeacher >0
////							if(isTeachTaskSync(genes,arrangeCourse, i, j,1,cursor)&&!hasCourseByTeacher && canArrangeCourse>0 && canArrangeTeacher >0
//							&&canArrangeGround) {
//						available.add(new Integer[]{i,j});
//					}
					
					
				}
				
			}

		}
	   if(available.size()>0){
			RandomGenerator generator = config.getRandomGenerator();
			int index = generator.nextInt(available.size());
			Integer[] position = available.get(index);
			int day = position[0];
			int lesson = position[1];
			CourseGene courseGene = new CourseGene(config, arrangeCourse);
			courseGene.setClassId(classId);
			courseGene.setDay(day);
			courseGene.setLesson(lesson);
			courseGene.setNearNum(1);
			courseGene.setLessonSize(this.getLessonSize(cursor));
			timetable[day][lesson] = courseGene;
			genes.add(courseGene);
			if(courseId.equals("1136"))
				
				
			//记录课程位置 用于同步教学任务
			//if((classId.equals("10026") || classId.equals("10027")) && (courseId.equals("1") || courseId.equals("2")))
			logger.debug("insertAnyAvailable 排课成功，ClassId：{} courseId：{} Day：{} Lesson：{} ", classId, courseId, day, lesson);
			arrageSyncPos(arrangeCourse, day, lesson, cursor);
			return true;
		}
		
		return false;
		
	}
	//将课程安排到一个任意空位，有教师冲突和课程冲突的不放
		public boolean insertAnyAvailableSync(Configuration config, List<Gene> genes, ArrangeCourse arrangeCourse, double cursor,int tryTimes) throws InvalidConfigurationException {
			String courseId = arrangeCourse.getCourseId();
			
			//最佳选择点
			List<Integer[]> bestAvb = new ArrayList<Integer[]>();
			for (int i = 0; i < totalMaxDays; i++) {
				
				for(int j = 0; j < totalMaxLesson; j++) {
					//最后一天半节课
					if(halfAtLastDay&&j>=pmLessonNum&&i==totalMaxDays-1){
						continue;
					}
					if(timetable[i][j]==null) {
						//
						boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, arrangeCourse , i, j,tryTimes);
//						int canArrangeCourse = this.ruleConflict.canArrangeCourse(courseId, i, j,arrangeCourse.getGradeLev());
//						int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(arrangeCourse.getArrangeTeachers(), i, j,-1,null);
//						boolean hasCourseOnDay = this.hasCourseOnDay(i,j, arrangeCourse);
////						if(!hasCourseByTeacher && canArrangeCourse>0 && canArrangeTeacher >0
//						boolean canArrangeGround = this.ruleConflict.canArrangeGround(gradeId, courseId, i, j,-1);
//						if(!hasCourseOnDay&&isTeachTaskSync(genes,arrangeCourse, i, j,1,cursor)&&!hasCourseByTeacher && canArrangeCourse>0 && canArrangeTeacher >0
//							&&canArrangeGround	) {
//							bestAvb.add(new Integer[]{i,j});
//						}
						
						
					}
					
				}
				
			}
			if(bestAvb.size()>0){
//				RandomGenerator generator = config.getRandomGenerator();
//				int index = generator.nextInt(bestAvb.size());
//				for(int index=0;index<bestAvb.size();index++){
					
				Integer[] position = bestAvb.get(0);
				int day = position[0];
				int lesson = position[1];
				CourseGene courseGene = new CourseGene(config, arrangeCourse);
				courseGene.setClassId(classId);
				courseGene.setDay(day);
				courseGene.setLesson(lesson);
				courseGene.setNearNum(1);
				courseGene.setLessonSize(this.getLessonSize(cursor));
				timetable[day][lesson] = courseGene;
				genes.add(courseGene);
				arrageSyncPos(arrangeCourse, day, lesson, cursor);
				
				return true;
//				}
			}
			return false;
			
		}
	/**
	 * 尝试将其它空位与已排科目交换后，再安排课程
	 * @param config
	 * @param genes
	 * @param arrangeCourse
	 * @param cursor
	 * @return
	 * @throws InvalidConfigurationException
	 */
	public boolean insertSwapPosition(Configuration config, List<Gene> genes, ArrangeCourse arrangeCourse, double cursor,int tryTimes) throws InvalidConfigurationException {
		int courseLevel = arrangeCourse.getCourseLevel();
		String courseId = arrangeCourse.getCourseId();
		
		
		List<Integer[]> available = new ArrayList<Integer[]>();
		for (int i = 0; i < totalMaxDays; i++) {
				
			for(int j = 0; j < totalMaxLesson; j++) {
				
				//最后一天半节课
				if(halfAtLastDay&&j>=pmLessonNum&&i==totalMaxDays-1){
					continue;
				}

				if(timetable[i][j]==null) {
					//
					available.add(new Integer[]{i,j});					
					
				}
				
			}

		}
		
		//相对可以移动的课程
		List<CourseGene> canSwap = new ArrayList<CourseGene>();
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++) {
				if(timetable[i][j]!=null) {
					//courseGene.getCourseLevel()==courseLevel
					CourseGene courseGene = timetable[i][j];
					//非单双周课程|非合班、非固排 && j>1 !courseGene.isOddEven()&&
					if(!courseGene.isOddEven()&&!courseGene.isFixed() && !courseGene.getCourseId().equals(courseId) && courseGene.getNearNum()==1 && j>0) {
							
						canSwap.add(courseGene);
					}
					
				}				
				
			}
			
		}
		
		
		//按课程级别排序
		Collections.sort(canSwap, new Comparator<CourseGene>(){

			@Override
			public int compare(CourseGene o1, CourseGene o2) {
				//小于-1、等于0, 大于1
				return o1.getCourseLevel() > o2.getCourseLevel() ? -1 : o1.getCourseLevel() == o2.getCourseLevel() ? 0 : 1;
			}

		});
		
		for (Integer[] position : available) {
			int pDay = position[0];
			int pLesson = position[1];
			if(available.size()>1 && pLesson<2){
				continue;
			}
		
			for (CourseGene courseGene : canSwap) {
				//目标位置
				int day = courseGene.getDay();
				int lesson = courseGene.getLesson();
				
				//被交换-被移动
				//被移动的课程
				ArrangeCourse _arrArrangeCourse = courseGene.getArrangeCourse();
				
				if(courseId.equals("1145")&&_arrArrangeCourse.getCourseId().equals("10029")
						&&pDay==1&&pLesson==5){
					System.out.println("交换测试！");
				}
				boolean hasCourseByTeacher = this.hasCourseByTeacher(genes, _arrArrangeCourse , pDay, pLesson,tryTimes);
//				int canArrangeCourse = this.ruleConflict.canArrangeCourse(_arrArrangeCourse.getCourseId(), pDay, pLesson,_arrArrangeCourse.getGradeLev());
//				int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(_arrArrangeCourse.getArrangeTeachers(), pDay, pLesson,-1,null);
//				boolean hasCourseOnDay = this.hasCourseOnDay(pDay, pLesson,_arrArrangeCourse);
//				boolean canArrangeGround = this.ruleConflict.canArrangeGround(gradeId, _arrArrangeCourse.getCourseId(), pDay, pLesson,-1);
//				double _cursor = getCursorByArrangeCourse(genes,_arrArrangeCourse, pDay, pLesson);
//				boolean isTeachTargetLess = isTeachTaskSync(genes,_arrArrangeCourse, pDay, pLesson,1,_cursor);
//				boolean isTeachTargetLess = true;
				boolean isTeachTargetLess = isTeachTaskSyncRebuild(genes,_arrArrangeCourse,day,lesson, pDay, pLesson,1, cursor);
				//要安排的课程
//				boolean isTeachSourceLess = isTeachTaskSync(genes,arrangeCourse, day, lesson,1,cursor);
				boolean isTeachSourceLess = isTeachTaskSyncRebuild(genes,arrangeCourse, pDay, pLesson, day, lesson,1,cursor);
//				boolean isTeachSourceLess = true;
//				int canArrangeCourseSrc = this.ruleConflict.canArrangeCourse(courseId, day, lesson,arrangeCourse.getGradeLev());
				boolean hasCourseByTeacherSrc = this.hasCourseByTeacher(genes, arrangeCourse , day, lesson,tryTimes);
//				int canArrangeTeacherSrc = this.ruleConflict.canArrangeTeacher(arrangeCourse.getArrangeTeachers(), day, lesson,-1,null);
//				boolean hasCourseOnDaySrc = this.hasCourseOnDay(day, lesson,arrangeCourse);
//				boolean canArrangeGroundSrc = this.ruleConflict.canArrangeGround(gradeId, arrangeCourse.getCourseId(), day, lesson,-1);
//				if(!hasCourseByTeacher&&isTeachSourceLess&&isTeachTargetLess
//						 && canArrangeCourse>0 && canArrangeTeacher >0 && (!hasCourseOnDay||(hasCourseOnDay&&pDay==day)) && !courseGene.isMerge() && !courseGene.isFixed()
//					&& !hasCourseByTeacherSrc && canArrangeCourseSrc>0 && canArrangeTeacherSrc >0 && !hasCourseOnDaySrc 
//					&&canArrangeGroundSrc &&canArrangeGround) {
//					//可以放过去，交换 被交换对象 放到空位
//					courseGene.setDay(pDay);
//					courseGene.setLesson(pLesson);
//					timetable[pDay][pLesson] = courseGene;
//					timetable[day][lesson] = null;
//					//当前查找课程 移动到被交换对象腾出的空位
//					CourseGene gene = new CourseGene(config, arrangeCourse);
//					gene.setClassId(classId);
//					gene.setDay(day);
//					gene.setLesson(lesson);
//					gene.setNearNum(1);
//					gene.setLessonSize(this.getLessonSize(cursor));
//					timetable[day][lesson] = gene;
//					genes.add(gene);
//					
//					//记录课程位置 用于同步教学任务
//					arrageSyncPosRebuild(genes,arrangeCourse, day, lesson,1,cursor);
//					arrageSyncPosRebuild(genes,_arrArrangeCourse, pDay, pLesson,1,0);
////					arrageSyncPos(arrangeCourse, day, lesson, cursor);
////					removeSyncPos(_arrArrangeCourse,   _cursor);
////					arrageSyncPos(_arrArrangeCourse, pDay, pLesson, _cursor);
//					if(courseId.equals("1136")||_arrArrangeCourse.getCourseId().equals("1136")){
//					
//						System.out.println("insertSwapPosition 排课成功:["+day+","+lesson+"]-"+arrangeCourse.getCourseName());
//						System.out.println("insertSwapPosition 被移动成功:["+pDay+","+pLesson+"]-"+_arrArrangeCourse.getCourseName());
//					}
//					//if((classId.equals("10026") || classId.equals("10027")) && (courseId.equals("1") || courseId.equals("2")))
//					logger.debug("insertSwapPosition 排课成功，ClassId：{} courseId：{} Day：{} Lesson：{} ", classId, courseId, day, lesson);
//					
//					return true;
//				}
			}
		}
		
		return false;
		
	}
	
	private double getCursorByArrangeCourse(List<Gene> genes,
			ArrangeCourse _arrArrangeCourse, int pDay, int pLesson) {
		// TODO Auto-generated method stub
		String courseId = _arrArrangeCourse.getCourseId();
		String classId = _arrArrangeCourse.getClassId();
//		String te = _arrArrangeCourse.get
		double sum = 0;
		for(Gene gen:genes){
			CourseGene cg = (CourseGene) gen;
			if(cg.getClassId().equalsIgnoreCase(classId)&&cg.getCourseId().equalsIgnoreCase(courseId)
				 	){
				sum += cg.getLessonSize();
			}
		}
		return sum;
	}

	//本班当天是否已经安排过该课程
	public boolean hasCourseOnDay(int day,int lesson, ArrangeCourse arrangeCourse){
		
		
		double taskLes = arrangeCourse.getTaskLessons();
		int unitSize = arrangeCourse.getUnitSize();
		if(taskLes>this.totalMaxDays&&unitSize==0){
				//剩余待安排课程数
				double lastLes =  taskLes - this.arrangedCourses(arrangeCourse.getCourseId());
				if(lastLes<(totalMaxDays-day)||hasTwoCourseOnDay( day, arrangeCourse)){
					return true;
				}
//			for(int j = 0; j < totalMaxLesson; j++) {
				if(lesson>0){
					
					CourseGene gene = timetable[day][lesson-1];
					if(gene!=null && gene.getArrangeCourse().getCourseId().equals(arrangeCourse.getCourseId())) {
						//
						return true;
					}	
				}
				if(lesson<this.totalMaxLesson-1){
					
					CourseGene gene = timetable[day][lesson+1];
					if(gene!=null && gene.getArrangeCourse().getCourseId().equals(arrangeCourse.getCourseId())) {
						//
						return true;
					}	
					
				}
				
//			}
			
		}else{
			
			for(int j = 0; j < totalMaxLesson; j++) {
				
				CourseGene gene = timetable[day][j];
				if(gene!=null && gene.getArrangeCourse().getCourseId().equals(arrangeCourse.getCourseId())) {
					//
					return true;
				}				
				
			}
		}
		
		return false;
		
	}
	/**
	 * 是否满足平均分配条件
	 * @param day
	 * @param lesson
	 * @param arrangeCourse
	 * @return
	 */
	private boolean isAvgDistribute(int day, int lesson,
			List<Gene> genes, ArrangeCourse arrangeCourse,int tryTimes) {
		// TODO Auto-generated method stub
		double taskLes = arrangeCourse.getTaskLessons();
		if(taskLes>=4.0||tryTimes>=3){
			//||tryTimes>=3
			return true;
		}
		Set<String> teacherIds = arrangeCourse.getArrangeTeachers().keySet();
		HashMap<String,Boolean> teaRs = new HashMap<String, Boolean>();
		HashMap<String,Double> teaNum = new HashMap<String, Double>();
		for(String tea:teacherIds){
			teaNum.put(tea, 1.0);
		}
		for(Gene gen :genes){
			CourseGene gene = (CourseGene) gen;
			int tday = gene.getDay();
			if(tday!=day){
				continue;
			}
			int tLes = gene.getLesson();
			String courseId = gene.getCourseId();
			if(!courseId.equalsIgnoreCase(arrangeCourse.getCourseId())){
				continue;
			}
			Set<String> teacherIds2 = gene.getArrangeCourse().getArrangeTeachers().keySet();
			for(String tea:teacherIds){
				if(teacherIds2.contains(tea)){
					double num = teaNum.get(tea);
					num= num+gene.getLessonSize();
					teaNum.put(tea, num);
				}
			}
			
		}
		
		for(ArrangeTeacher tea:arrangeCourse.getArrangeTeachers().values()){
			double maxAll = tea.getSumTaskLesson(arrangeCourse.getCourseId());
			int fz = totalMaxDays;
			if(halfAtLastDay){
				fz --;
			}
			double avg = maxAll/fz;
			double xx =   Math.floor(avg);
			double sx =  xx+1;
			double tarNum = 1;
			
			tarNum = teaNum.get(tea.getTeacherId() );
			if(tarNum==1||(tarNum<=sx)){
				if(day <2&&tarNum>xx&&xx!=0&&tryTimes<3){
					return false;
				}else{
					teaRs.put(tea.getTeacherId(), true) ; 
				}
			}else{
				teaRs.put(tea.getTeacherId(), false) ; 
				
			}
		}
		if(teaRs.values().contains(false)){
			
			return false;
		}else{
			return true;
		}
	}

	public boolean hasTwoCourseOnDay(int day,ArrangeCourse arrangeCourse){
		
		int num =0;
		for(int j = 0; j < totalMaxLesson; j++) {
			
			CourseGene gene = timetable[day][j];
			if(gene!=null && gene.getArrangeCourse().getCourseId().equals(arrangeCourse.getCourseId())) {
				//
				num++;
			}				
			
		}
		if(num==2){
			return true;
		}
		return false;
	}
	
	public int getAvailablePositionNum(){
		int availablePositionNum = 0;
		for (int i = 0; i < totalMaxDays; i++) {
			
			for(int j = 0; j < totalMaxLesson; j++) {
				if(timetable[i][j]==null) {
					//
					availablePositionNum++;
				}				
				
			}
			
		}
		
		return availablePositionNum;
	}
	
	private void printTimetable(){
		System.out.println("班级：" + classId);
		for (int i = 0; i < totalMaxLesson; i++) {
			
			for(int j = 0; j < totalMaxDays; j++){
				CourseGene courseGene = timetable[j][i];						
				if(courseGene!=null){
					ArrangeCourse arrangeCourse = courseGene.getArrangeCourse();
					String dw ="";
					if(courseGene.getDwCourseGene()!=null){
						dw+="/"+courseGene.getDwCourseGene().getArrangeCourse().getCourseName();
					}
					System.out.print("	" + arrangeCourse.getCourseName()+dw);
					
				}else{
					System.out.print("	(空)");
				}
				System.out.print("	");
			}
			System.out.println("");
		}
		System.out.println("");
		
	}
	
	public CourseGene getCourse(int day, int lesson){
		return timetable[day][lesson];
		
	}
	
	public void setCourse(CourseGene courseGene){
		timetable[courseGene.getDay()][courseGene.getLesson()] = courseGene;
		
	}
	
	public void removeCourse(CourseGene courseGene){
		timetable[courseGene.getDay()][courseGene.getLesson()] = null;
	}
	
	public void swapCourse(CourseGene fromCourse, CourseGene toCourse){
		timetable[fromCourse.getDay()][fromCourse.getLesson()] = toCourse;
		timetable[toCourse.getDay()][toCourse.getLesson()] = fromCourse;
		
	}
	
	
	public int getTotalMaxLesson() {
		return this.totalMaxLesson;
	}

	public String getGradeId() {
		return gradeId;
	}

	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

	public String getGradeName() {
		return gradeName;
	}

	public void setGradeName(String gradeName) {
		this.gradeName = gradeName;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getTotalMaxDays() {
		return totalMaxDays;
	}

	public void setTotalMaxDays(int totalMaxDays) {
		this.totalMaxDays = totalMaxDays;
	}

	public int getAmLessonNum() {
		return amLessonNum;
	}

	public void setAmLessonNum(int amLessonNum) {
		this.amLessonNum = amLessonNum;
	}

	public int getPmLessonNum() {
		return pmLessonNum;
	}

	public void setPmLessonNum(int pmLessonNum) {
		this.pmLessonNum = pmLessonNum;
	}

	public CourseGene[][] getTimetable() {
		return timetable;
	}

	public void setTimetable(CourseGene[][] timetable) {
		this.timetable = timetable;
	}

	public void setTotalMaxLesson(int totalMaxLesson) {
		this.totalMaxLesson = totalMaxLesson;
	}

	public boolean isHalfAtLastDay() {
		return halfAtLastDay;
	}

	public void setHalfAtLastDay(boolean halfAtLastDay) {
		this.halfAtLastDay = halfAtLastDay;
	}

	public RuleConflict getRuleConflict() {
		return ruleConflict;
	}

	public void setRuleConflict(RuleConflict ruleConflict) {
		this.ruleConflict = ruleConflict;
	}



	
	
}
