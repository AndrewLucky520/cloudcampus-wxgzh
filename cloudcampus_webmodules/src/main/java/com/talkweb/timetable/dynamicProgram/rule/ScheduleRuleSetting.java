package com.talkweb.timetable.dynamicProgram.rule;

import java.io.Serializable;

import com.talkweb.timetable.dynamicProgram.enums.RuleLevel;

public class ScheduleRuleSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4333235849020833797L;

	/**
	 * 教学进度是否要控制 默认是
	 */
	private RuleLevel teachingProcSyncLev = RuleLevel.Important;
	
	/**
	 * 合班
	 */
	private RuleLevel mergeClassLev = RuleLevel.Important;
	/**
	 * 教师规则
	 */
	private RuleLevel teacherRuleLev = RuleLevel.Important;
	/**
	 * 教师课程尽量平均分配-需考虑教研活动与其它未排年级课程
	 */
	private RuleLevel teacherTimeAvgRuleLev = RuleLevel.LessImportant;
	/**
	 * 科目规则 
	 */
	private RuleLevel courseRuleLev = RuleLevel.Important;
	
	/**
	 * 科目规则二 尽量上午、下午
	 */
	private RuleLevel courseRuleSubLev = RuleLevel.LessImportant;
	
	

	public RuleLevel getTeachingProcSyncLev() {
		return teachingProcSyncLev;
	}

	public void setTeachingProcSyncLev(RuleLevel teachingProcSyncLev) {
		this.teachingProcSyncLev = teachingProcSyncLev;
	}

	public RuleLevel getMergeClassLev() {
		return mergeClassLev;
	}

	public void setMergeClassLev(RuleLevel mergeClassLev) {
		this.mergeClassLev = mergeClassLev;
	}

	public RuleLevel getTeacherRuleLev() {
		return teacherRuleLev;
	}

	public void setTeacherRuleLev(RuleLevel teacherRuleLev) {
		this.teacherRuleLev = teacherRuleLev;
	}

	public RuleLevel getTeacherTimeAvgRuleLev() {
		return teacherTimeAvgRuleLev;
	}

	public void setTeacherTimeAvgRuleLev(RuleLevel teacherTimeAvgRuleLev) {
		this.teacherTimeAvgRuleLev = teacherTimeAvgRuleLev;
	}

	public RuleLevel getCourseRuleLev() {
		return courseRuleLev;
	}

	public void setCourseRuleLev(RuleLevel courseRuleLev) {
		this.courseRuleLev = courseRuleLev;
	}

	public RuleLevel getCourseRuleSubLev() {
		return courseRuleSubLev;
	}

	public void setCourseRuleSubLev(RuleLevel courseRuleSubLev) {
		this.courseRuleSubLev = courseRuleSubLev;
	}
	
	
}
