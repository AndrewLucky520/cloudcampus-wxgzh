package com.talkweb.timetable.arrangement.domain;

/**
	*此类由MySQLToBean工具自动生成
	*备注(数据表的comment字段)：无备注信息
	*@wy 
2015-09-23 10:24:31
	*/

	public class TCRCATemporaryAdjustRecord{
	private String SchoolId;
	private String TimetableId;
	private String RecordId;
	private String RelatedRecordIds;
	private int WeekOfStart;
	private int WeekOfEnd;
	private String ClassId;
	private String OriginalSubject;
	private String TargetSubject;
	private int OriginalDay;
	private int OriginalLesson;
	private int TargetDay;
	private int TargetLesson;
	private String OriginalTeachers;
	private String TargetTeachers;
	private String AdjustType;//1：对调，2：教师代课，3：自习，4：公开课
	private String RecordTime;
	private String GradeId;
	private int Week;//当前周
	private String GroupId;
	private int Step;
	private String PublishTeachers;
	private String Published;

	public int getWeek() {
		return Week;
	}
	public void setWeek(int week) {
		this.Week = week;
	}
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
	public String getRecordId(){
		return this.RecordId;
	}
	public void setRecordId(String RecordId){
		this.RecordId=RecordId;
	}
	public String getRelatedRecordIds() {
		return RelatedRecordIds;
	}
	public void setRelatedRecordIds(String relatedRecordIds) {
		RelatedRecordIds = relatedRecordIds;
	}
	public int getWeekOfStart(){
		return this.WeekOfStart;
	}
	public void setWeekOfStart(int WeekOfStart){
		this.WeekOfStart=WeekOfStart;
	}
	public int getWeekOfEnd(){
		return this.WeekOfEnd;
	}
	public void setWeekOfEnd(int WeekOfEnd){
		this.WeekOfEnd=WeekOfEnd;
	}
	public String getClassId(){
		return this.ClassId;
	}
	public void setClassId(String ClassId){
		this.ClassId=ClassId;
	}
	public String getOriginalSubject(){
		return this.OriginalSubject;
	}
	public void setOriginalSubject(String OriginalSubject){
		this.OriginalSubject=OriginalSubject;
	}
	public String getTargetSubject(){
		return this.TargetSubject;
	}
	public void setTargetSubject(String TargetSubject){
		this.TargetSubject=TargetSubject;
	}
	public int getOriginalDay(){
		return this.OriginalDay;
	}
	public void setOriginalDay(int OriginalDay){
		this.OriginalDay=OriginalDay;
	}
	public int getOriginalLesson(){
		return this.OriginalLesson;
	}
	public void setOriginalLesson(int OriginalLesson){
		this.OriginalLesson=OriginalLesson;
	}
	public int getTargetDay(){
		return this.TargetDay;
	}
	public void setTargetDay(int TargetDay){
		this.TargetDay=TargetDay;
	}
	public int getTargetLesson(){
		return this.TargetLesson;
	}
	public String getGroupId() {
		return GroupId;
	}
	public void setGroupId(String groupId) {
		GroupId = groupId;
	}
	public int getStep() {
		return Step;
	}
	public void setStep(int step) {
		Step = step;
	}
	public void setTargetLesson(int TargetLesson){
		this.TargetLesson=TargetLesson;
	}
	public String getOriginalTeachers(){
		return this.OriginalTeachers;
	}
	public void setOriginalTeachers(String OriginalTeachers){
		this.OriginalTeachers=OriginalTeachers;
	}
	public String getTargetTeachers(){
		return this.TargetTeachers;
	}
	public void setTargetTeachers(String TargetTeachers){
		this.TargetTeachers=TargetTeachers;
	}
	public String getAdjustType(){
		return this.AdjustType;
	}
	public void setAdjustType(String AdjustType){
		this.AdjustType=AdjustType;
	}
	public String getRecordTime(){
		return this.RecordTime;
	}
	public String getPublished() {
		return Published;
	}
	public void setPublished(String published) {
		Published = published;
	}
	public void setRecordTime(String RecordTime){
		this.RecordTime=RecordTime;
	}
	public String getPublishTeachers() {
		return PublishTeachers;
	}
	public void setPublishTeachers(String publishTeachers) {
		PublishTeachers = publishTeachers;
	}

}