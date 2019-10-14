package com.talkweb.timetable.arrangement.domain;

	/**
	*此类由MySQLToBean工具自动生成
	*备注(数据表的comment字段)：无备注信息
	*@wy 
2015-09-23 12:03:15
	*/

	public class TCRCATimetablePrintSet{
	private String SchoolId;
	private String TimetableId;
	private String type;//01：年级课表，02：教师课表，03：班级课表
	private String title;
	private String BottomNote1;
	private String BottomNote2;
	private String BottomNote3;
	private String PrintStyle;//01：一页一表，02：一页两表
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
	public String getType(){
		return this.type;
	}
	public void setType(String type){
		this.type=type;
	}
	public String getTitle(){
		return this.title;
	}
	public void setTitle(String title){
		this.title=title;
	}
	public String getBottomNote1(){
		return this.BottomNote1;
	}
	public void setBottomNote1(String BottomNote1){
		this.BottomNote1=BottomNote1;
	}
	public String getBottomNote2(){
		return this.BottomNote2;
	}
	public void setBottomNote2(String BottomNote2){
		this.BottomNote2=BottomNote2;
	}
	public String getBottomNote3(){
		return this.BottomNote3;
	}
	public void setBottomNote3(String BottomNote3){
		this.BottomNote3=BottomNote3;
	}
	public String getPrintStyle(){
		return this.PrintStyle;
	}
	public void setPrintStyle(String PrintStyle){
		this.PrintStyle=PrintStyle;
	}

}