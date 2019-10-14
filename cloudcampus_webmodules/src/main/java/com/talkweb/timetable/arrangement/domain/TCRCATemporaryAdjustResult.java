package com.talkweb.timetable.arrangement.domain;

	/**
	*此类由MySQLToBean工具自动生成
	*备注(数据表的comment字段)：无备注信息
	*@wy 
2015-09-23 12:02:08
	*/

	public class TCRCATemporaryAdjustResult{
	private String SchoolId;
	private String TimetableId;
	private int Week;
	private String ClassId;
	private String SubjectId;
	private int DayOfWeek;
	private int LessonOfDay;
	private String Teachers;
	private String CourseType; 
	private String McGroupId;
	private int WeekOfStart;
	private String WeekOfEnd;
	private String FromTeachers;
	private String FromSubjectId;
	private int FromDayOfWeek;
	private int FromLessonOfDay;
	private String AdjustType;//1：对调，2：教师代课，3：自习，4：公开课
	private String Published;
	private String GradeId;
	private String GroupId;
	private String PublishTeachers;
	public String getGradeId() {
		return GradeId;
	}
	public void setGradeId(String gradeId) {
		GradeId = gradeId;
	}
	public String getSchoolId(){
		return this.SchoolId;
	}
	public void setSchoolId(String SchoolId){
		this.SchoolId=SchoolId;
	}
	public String getTimetableId(){
		return this.TimetableId;
	}
	public void setTimetableId(String TimetableId){
		this.TimetableId=TimetableId;
	}
	public int getWeek(){
		return this.Week;
	}
	public void setWeek(int Week){
		this.Week=Week;
	}
	public String getClassId(){
		return this.ClassId;
	}
	public void setClassId(String ClassId){
		this.ClassId=ClassId;
	}
	public String getSubjectId(){
		return this.SubjectId;
	}
	public void setSubjectId(String SubjectId){
		this.SubjectId=SubjectId;
	}
	public int getDayOfWeek(){
		return this.DayOfWeek;
	}
	public void setDayOfWeek(int DayOfWeek){
		this.DayOfWeek=DayOfWeek;
	}
	public int getLessonOfDay(){
		return this.LessonOfDay;
	}
	public void setLessonOfDay(int LessonOfDay){
		this.LessonOfDay=LessonOfDay;
	}
	public String getTeachers(){
		return this.Teachers;
	}
	public void setTeachers(String Teachers){
		this.Teachers=Teachers;
	}
	public int getWeekOfStart(){
		return this.WeekOfStart;
	}
	public void setWeekOfStart(int WeekOfStart){
		this.WeekOfStart=WeekOfStart;
	}
	public String getWeekOfEnd(){
		return this.WeekOfEnd;
	}
	public void setWeekOfEnd(String WeekOfEnd){
		this.WeekOfEnd=WeekOfEnd;
	}
	public String getFromTeachers(){
		return this.FromTeachers;
	}
	public void setFromTeachers(String FromTeachers){
		this.FromTeachers=FromTeachers;
	}
	public String getFromSubjectId(){
		return this.FromSubjectId;
	}
	public void setFromSubjectId(String FromSubjectId){
		this.FromSubjectId=FromSubjectId;
	}
	public int getFromDayOfWeek(){
		return this.FromDayOfWeek;
	}
	public void setFromDayOfWeek(int FromDayOfWeek){
		this.FromDayOfWeek=FromDayOfWeek;
	}
	public int getFromLessonOfDay(){
		return this.FromLessonOfDay;
	}
	public void setFromLessonOfDay(int FromLessonOfDay){
		this.FromLessonOfDay=FromLessonOfDay;
	}
	public String getAdjustType(){
		return this.AdjustType;
	}
	public void setAdjustType(String AdjustType){
		this.AdjustType=AdjustType;
	}
	public String getPublished(){
		return this.Published;
	}
	public void setPublished(String Published){
		this.Published=Published;
	}
	public String getCourseType() {
		return CourseType;
	}
	public void setCourseType(String courseType) {
		CourseType = courseType;
	}
	public String getMcGroupId() {
		return McGroupId;
	}
	public void setMcGroupId(String mcGroupId) {
		McGroupId = mcGroupId;
	}
	
	public String getGroupId() {
		return GroupId;
	}
	public void setGroupId(String groupId) {
		GroupId = groupId;
	}
	public String getPublishTeachers() {
		return PublishTeachers;
	}
	public void setPublishTeachers(String publishTeachers) {
		PublishTeachers = publishTeachers;
	}
}