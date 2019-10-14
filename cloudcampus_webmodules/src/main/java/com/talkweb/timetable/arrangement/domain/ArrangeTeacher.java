package com.talkweb.timetable.arrangement.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.talkweb.scoreManage.action.ScoreUtil;
/**
 * 教师任务安排
 * @author talkweb
 *
 */
public class ArrangeTeacher {

	private String teacherId;
	private String teacherName;
	
	/**
	 * 与该老师有关的教学任务
	 */
	private List<ArrangeCourse> courses = new ArrayList<ArrangeCourse>();


	public void addCourse(ArrangeCourse course){
		this.courses.add(course);
	}
	
	
	public String getTeacherId() {
		return teacherId;
	}
	public void setTeacherId(String teacherId) {
		this.teacherId = teacherId;
	}
	public String getTeacherName() {
		return teacherName;
	}
	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}


	public List<ArrangeCourse> getCourses() {
		return courses;
	}


	public void setCourses(List<ArrangeCourse> courses) {
		this.courses = courses;
	}

	/**
	 * 获取某课程已安排位置
	 * @param gradeId
	 * @param courseId
	 * @return
	 */
	public Map<Double,Integer[][]> getArragedPosition(String gradeId,String courseId ,String classId){
		 Map<Double,Integer[][]> rs2 = new HashMap<Double,Integer[][]>();
//		 int[][] crs = new int[courses.size()][2];
//		 for(ArrangeCourse arc :courses){
		 for(int i=0;i<courses.size();i++){
			 ArrangeCourse arc = courses.get(i);
			 if(!arc.getClassId().equalsIgnoreCase(classId)&&arc.getGradeId().equalsIgnoreCase(gradeId)&&arc.getCourseId().equalsIgnoreCase(courseId)){
				if(!arc.getHasArrangedPos().isEmpty()){
//					rs.put(arc.getClassId(), arc.getHasArrangedPos());
					Map<Double, Integer[]> pos = arc.getHasArrangedPos();
					for(Iterator<Double> it = pos.keySet().iterator();it.hasNext();){
						double key = it.next();
						if(rs2.containsKey(key)){
							Integer[][] crs = rs2.get(key);
							crs[i] = pos.get(key);
						}else{
							Integer[][] crs = new Integer[courses.size()][2];
							crs[i] = pos.get(key);
							rs2.put(key, crs);
						}
					}
				}
			 }
		 }
		 return rs2;
	}
	/**
	 * 获取某课程已安排位置-当前班级
	 * @param gradeId
	 * @param courseId
	 * @return
	 */
	public Map<Double,Integer[][]> getCurArragedPosition(String gradeId,String courseId ,String classId){
		Map<Double,Integer[][]> rs2 = new HashMap<Double,Integer[][]>();
//		 int[][] crs = new int[courses.size()][2];
//		 for(ArrangeCourse arc :courses){
		for(int i=0;i<courses.size();i++){
			ArrangeCourse arc = courses.get(i);
			if(arc.getClassId().equalsIgnoreCase(classId)&&arc.getGradeId().equalsIgnoreCase(gradeId)&&arc.getCourseId().equalsIgnoreCase(courseId)){
				if(!arc.getHasArrangedPos().isEmpty()){
//					rs.put(arc.getClassId(), arc.getHasArrangedPos());
					Map<Double, Integer[]> pos = arc.getHasArrangedPos();
					for(Iterator<Double> it = pos.keySet().iterator();it.hasNext();){
						double key = it.next();
						if(rs2.containsKey(key)){
							Integer[][] crs = rs2.get(key);
							crs[i] = pos.get(key);
						}else{
							Integer[][] crs = new Integer[courses.size()][2];
							crs[i] = pos.get(key);
							rs2.put(key, crs);
						}
					}
				}
			}
		}
		return rs2;
	}
	
	public void setArragedPosition(String classId,String gradeId,String courseId,double cursor,int day,int lesson){
		for(int i=0;i<courses.size();i++){
			 ArrangeCourse arc = courses.get(i);
			 if(arc.getClassId().equalsIgnoreCase(classId)&&arc.getGradeId().equalsIgnoreCase(gradeId)&&arc.getCourseId().equalsIgnoreCase(courseId)){
				 Integer[] p = new Integer[]{day,lesson};
				 arc.getHasArrangedPos().put(cursor, p);
			 }
		}
	}
	public void removeArragedPosition(String classId,String gradeId,String courseId,double cursor){
		for(int i=0;i<courses.size();i++){
			ArrangeCourse arc = courses.get(i);
			if(arc.getClassId().equalsIgnoreCase(classId)&&arc.getGradeId().equalsIgnoreCase(gradeId)&&arc.getCourseId().equalsIgnoreCase(courseId)){
//				int[] p = new int[]{day,lesson};
				arc.getHasArrangedPos().remove(cursor);
			}
		}
	}
	/**
	 * 获取某课程班级安排比例
	 * @param gradeId
	 * @param courseId
	 * @return
	 */
	public float getArragedPercent(String gradeId,String courseId,String classId){
		int sum = courses.size();
		float num = 0;
		for(ArrangeCourse arc :courses){
			 if(!arc.getClassId().equalsIgnoreCase(classId)&&arc.getGradeId().equalsIgnoreCase(gradeId)&&arc.getCourseId().equalsIgnoreCase(courseId)){
				if(!arc.getHasArrangedPos().isEmpty()){
					num++;
				}
			 }
		 }
		
		return ScoreUtil.castFloatTowPointNum((float)num/sum);
	}


	public double getSumTaskLesson(String courseId){
		double sum = 0;
		for(ArrangeCourse course:this.getCourses()){
			if(courseId.equalsIgnoreCase(course.getCourseId())){
				
				sum += course.getTaskLessons();
			}
		}
		
		return sum;
	}
}
