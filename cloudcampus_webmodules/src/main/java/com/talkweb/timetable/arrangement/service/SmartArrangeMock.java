package com.talkweb.timetable.arrangement.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;

import com.talkweb.timetable.arrangement.algorithm.CourseGene;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;
import com.talkweb.timetable.arrangement.domain.ArrangeClass;
import com.talkweb.timetable.arrangement.domain.ArrangeCourse;
import com.talkweb.timetable.arrangement.domain.ArrangeGrid;
import com.talkweb.timetable.arrangement.domain.ArrangeTeacher;
import com.talkweb.timetable.arrangement.domain.RuleClassGroup;
import com.talkweb.timetable.arrangement.domain.RuleCourse;
import com.talkweb.timetable.arrangement.domain.RulePosition;
import com.talkweb.timetable.arrangement.domain.RuleResearchMeeting;
import com.talkweb.timetable.arrangement.domain.RuleTeacher;
import com.talkweb.timetable.arrangement.service.impl.SmartArrangeServiceImpl;

public class SmartArrangeMock {

	
	public static void main(String[] args) {
		
		//测试智能排课算法

		RuleConflict ruleConflict = new RuleConflict();
		
		//加载科目规则
		Map<String, RuleCourse> ruleCourses = new HashMap<String, RuleCourse>();
		
		RuleCourse ruleCourse = new RuleCourse();
		ruleCourse.setCourseId("C006");
		ruleCourse.setMaxClassNum(2);
		ruleCourse.setMaxAMNum(2);
		ruleCourse.setMaxPMNum(2);
		
		List<RulePosition> positions = new ArrayList<RulePosition>();
		
		RulePosition rulePosition = new RulePosition();
		//0：不排位置，1：必排位置
		rulePosition.setRuleType(0);
		rulePosition.setDay(0);
		rulePosition.setLesson(0);
		positions.add(rulePosition);
		
		rulePosition = new RulePosition();
		rulePosition.setRuleType(0);
		rulePosition.setDay(0);
		rulePosition.setLesson(5);
		positions.add(rulePosition);
		
		ruleCourse.setPositions(positions);
		
		ruleCourses.put(ruleCourse.getCourseId(), ruleCourse);
		
		
		//加载教师规则
		Map<String, RuleTeacher> ruleTeachers = new HashMap<String, RuleTeacher>();

		RuleTeacher ruleTeacher = new RuleTeacher();
		ruleTeacher.setTeacherId("T0003");
		ruleTeacher.setMaxPerDay(2);
		
		positions = new ArrayList<RulePosition>();
		
		rulePosition = new RulePosition();
		rulePosition.setRuleType(0);
		rulePosition.setDay(0);
		rulePosition.setLesson(0);
		positions.add(rulePosition);
		
		rulePosition = new RulePosition();
		rulePosition.setRuleType(1);
		rulePosition.setDay(0);
		rulePosition.setLesson(1);
		positions.add(rulePosition);
		
		ruleTeacher.setPositions(positions);
		
		ruleTeachers.put(ruleTeacher.getTeacherId(), ruleTeacher);
		
		
		//加载合班规则
		List<RuleClassGroup> ruleClassGroups = new ArrayList<RuleClassGroup>();
		
		RuleClassGroup ruleClassGroup = new RuleClassGroup();
		ruleClassGroup.setClassGroupId("G0001");
		ruleClassGroup.setClassGroupName("体育合班");
		ruleClassGroup.setCourseId("C006");
		ruleClassGroup.addClassGroup("101");
		ruleClassGroup.addClassGroup("102");
		
		ruleClassGroups.add(ruleClassGroup);
		
		//教师的教研活动
		List<RuleResearchMeeting> ruleResearchMeetings = new ArrayList<RuleResearchMeeting>();
		
		RuleResearchMeeting ruleResearchMeeting = new RuleResearchMeeting();
		ruleResearchMeeting.setTeacherId("T0001");
		ruleResearchMeeting.setTeacherName("李老师");
		ruleResearchMeeting.setDay(0);
		ruleResearchMeeting.setLesson(5);				
		ruleResearchMeetings.add(ruleResearchMeeting);
		
		ruleResearchMeeting = new RuleResearchMeeting();
		ruleResearchMeeting.setTeacherId("T0002");
		ruleResearchMeeting.setTeacherName("张老师");
		ruleResearchMeeting.setDay(0);
		ruleResearchMeeting.setLesson(5);
		ruleResearchMeetings.add(ruleResearchMeeting);
		
		//
		ruleConflict.setRuleCourses(ruleCourses);
		ruleConflict.setRuleTeachers(ruleTeachers);
		ruleConflict.setRuleClassGroups(ruleClassGroups);
		ruleConflict.setRuleResearchMeetings(ruleResearchMeetings);
		
		
		//按年级生成班级课表的表格
		ArrangeGrid arrangeGrid = new ArrangeGrid();
		for (int i = 1; i <= 1; i++) {
			String gradeId = "NJ" + i;
			
			for(int c = 1; c <= 2; c++) {
				String classId = i + "0" + c;
				
				ArrangeClass arrangeClass = new ArrangeClass(gradeId, classId, 5, 4, 2, ruleConflict);
				arrangeClass.setGradeId(gradeId);
				arrangeClass.setGradeName("");
				
				arrangeClass.setClassId(classId);
				arrangeClass.setClassName("");

				arrangeGrid.addArrangeClass(arrangeClass);
				
			}
			
			
		}
		
		
		//加载教学任务
		List<ArrangeTeacher> arrangeTeachers = new ArrayList<ArrangeTeacher>();
		
		List<ArrangeCourse> courses = new ArrayList<ArrangeCourse>();
		
		ArrangeTeacher teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0001");
		teacher.setTeacherName("李老师");
		
		arrangeTeachers.add(teacher);
		
		
		ArrangeCourse arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(1);
		arrangeCourse.setCourseId("C001");
		arrangeCourse.setCourseName("语文");
		//0：主课，1：普课，2：辅课
		arrangeCourse.setCourseLevel(0);
		arrangeCourse.setTaskLessons(6);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);


		//++++++++++++++++++++++++++++	
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(1);
		arrangeCourse.setCourseId("C001");
		arrangeCourse.setCourseName("语文");
		//0：主课，1：普课，2：辅课
		arrangeCourse.setCourseLevel(0);
		arrangeCourse.setTaskLessons(6);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		

		teacher.setCourses(courses);
		
		//-------------------------------------------------------------------------------
		teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0002");
		teacher.setTeacherName("张老师");
		
		arrangeTeachers.add(teacher);
		
		courses = new ArrayList<ArrangeCourse>();
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(1);
		arrangeCourse.setCourseId("C002");
		arrangeCourse.setCourseName("数学");
		arrangeCourse.setCourseLevel(0);
		arrangeCourse.setTaskLessons(6);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
				
		
		//++++++++++++++++++++++++++++
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(1);
		arrangeCourse.setCourseId("C002");
		arrangeCourse.setCourseName("数学");
		arrangeCourse.setCourseLevel(0);
		arrangeCourse.setTaskLessons(6);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		teacher.setCourses(courses);
		
		//-------------------------------------------------------------------------------
		teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0003");
		teacher.setTeacherName("武老师");
		
		arrangeTeachers.add(teacher);
		

		courses = new ArrayList<ArrangeCourse>();		
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C004");
		arrangeCourse.setCourseName("化学");
		arrangeCourse.setCourseLevel(1);
		arrangeCourse.setTaskLessons(4);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		
		//++++++++++++++++++++++++++++
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C004");
		arrangeCourse.setCourseName("化学");
		arrangeCourse.setCourseLevel(1);
		arrangeCourse.setTaskLessons(4);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		teacher.setCourses(courses);
		
		//-------------------------------------------------------------------------------
		teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0004");
		teacher.setTeacherName("刘老师");
		
		arrangeTeachers.add(teacher);
		
				
		courses = new ArrayList<ArrangeCourse>();		

		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(1);
		arrangeCourse.setCourseId("C003");
		arrangeCourse.setCourseName("英语");
		arrangeCourse.setCourseLevel(0);
		arrangeCourse.setTaskLessons(4);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		
		//++++++++++++++++++++++++++++
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(1);
		arrangeCourse.setCourseId("C003");
		arrangeCourse.setCourseName("英语");
		arrangeCourse.setCourseLevel(0);
		arrangeCourse.setTaskLessons(4);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		teacher.setCourses(courses);
		
		//-------------------------------------------------------------------------------
		teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0007");
		teacher.setTeacherName("严老师");
		
		arrangeTeachers.add(teacher);
		
		courses = new ArrayList<ArrangeCourse>();
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C007");
		arrangeCourse.setCourseName("美术");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		
		//++++++++++++++++++++++++++++
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C007");
		arrangeCourse.setCourseName("美术");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		teacher.setCourses(courses);
		
		//-------------------------------------------------------------------------------
		teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0008");
		teacher.setTeacherName("宋老师");
		
		arrangeTeachers.add(teacher);
		
				
		courses = new ArrayList<ArrangeCourse>();
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C008");
		arrangeCourse.setCourseName("计算");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		
		//++++++++++++++++++++++++++++
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C008");
		arrangeCourse.setCourseName("计算");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		teacher.setCourses(courses);
				
		//-------------------------------------------------------------------------------
		teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0009");
		teacher.setTeacherName("唐老师");
		
		arrangeTeachers.add(teacher);
				
		courses = new ArrayList<ArrangeCourse>();
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C009");
		arrangeCourse.setCourseName("花卉");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2.5);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		//++++++++++++++++++++++++++++
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C009");
		arrangeCourse.setCourseName("花卉");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		teacher.setCourses(courses);
		
		//-------------------------------------------------------------------------------
		teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0010");
		teacher.setTeacherName("范老师");
		
		arrangeTeachers.add(teacher);
		
				
		courses = new ArrayList<ArrangeCourse>();

		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C005");
		arrangeCourse.setCourseName("音乐");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		
		//++++++++++++++++++++++++++++
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C005");
		arrangeCourse.setCourseName("音乐");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(1.5);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		teacher.setCourses(courses);
		
		//-------------------------------------------------------------------------------
		teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0011");
		teacher.setTeacherName("强老师");
		
		arrangeTeachers.add(teacher);
		
				
		courses = new ArrayList<ArrangeCourse>();
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C006");
		arrangeCourse.setCourseName("体育");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		
		//++++++++++++++++++++++++++++
		
		arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("101");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C006");
		arrangeCourse.setCourseName("体育");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		courses.add(arrangeCourse);
		
		teacher.setCourses(courses);
		
		
		//设置预排课		
		
		
		//智能排课
		SmartArrangeServiceImpl smartArrangeService = new SmartArrangeServiceImpl();
		try {
//			smartArrangeService.startArrangeTimeTable(null, arrangeGrid, arrangeTeachers, ruleConflict, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		smartArrangeService.print(arrangeGrid,arrangeTeachers);
		
		//校验课表完美程度
		

		System.out.println("------------------结束------------------");
		
	}
	
	
	private void mockArrangeFixedCourses(Configuration config, ArrangeGrid arrangeGrid, List<Gene> genes) throws InvalidConfigurationException{
		//模拟数据库中的固排数据
		List<CourseGene> fixedCourses = new ArrayList<CourseGene>();
		
		ArrangeTeacher teacher = new ArrangeTeacher();
		teacher.setTeacherId("T0007");
		teacher.setTeacherName("严老师");
		
		
		ArrangeCourse arrangeCourse = new ArrangeCourse();
		arrangeCourse.setClassId("102");
		arrangeCourse.setUnitSize(0);
		arrangeCourse.setCourseId("C007");
		arrangeCourse.setCourseName("美术");
		arrangeCourse.setCourseLevel(2);
		arrangeCourse.setTaskLessons(2);
		arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);
		
		CourseGene courseGene = new CourseGene(config, arrangeCourse);
		courseGene.setClassId("102");
		courseGene.setDay(0);
		courseGene.setLesson(4);
		courseGene.setFixed(true);

		fixedCourses.add(courseGene);
		
		for (CourseGene gene : fixedCourses) {
			String classId = gene.getClassId();
			ArrangeClass arrangeClass = arrangeGrid.getArrangeClass(classId);
			arrangeClass.addFixedCourse(genes, gene);
			
		}
		
		
	}
	
	
}
