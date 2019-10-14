package com.talkweb.jasperReport.bean;

import java.util.List;

/**
 * 
 * 班级报告打印-首页bean
 *
*/
public class ClassScoreHead {
	
	private String reportTitle = "";

	private List<ExamOverview> examOverview;
	
	private String examExplanation = "";
		
	private String totalScoreItem = "";
	private String totalUpName = "";
	private List<StudentRankVary> totalScoreUp;
	private String totalDownName = "";
	private List<StudentRankVary> totalScoreDown;
	
	public String getReportTitle() {
		return reportTitle;
	}
	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}
	public List<ExamOverview> getExamOverview() {
		return examOverview;
	}
	public void setExamOverview(List<ExamOverview> examOverview) {
		this.examOverview = examOverview;
	}
	public String getExamExplanation() {
		return examExplanation;
	}
	public void setExamExplanation(String examExplanation) {
		this.examExplanation = examExplanation;
	}
	public String getTotalScoreItem() {
		return totalScoreItem;
	}
	public void setTotalScoreItem(String totalScoreItem) {
		this.totalScoreItem = totalScoreItem;
	}
	public String getTotalUpName() {
		return totalUpName;
	}
	public void setTotalUpName(String totalUpName) {
		this.totalUpName = totalUpName;
	}
	public List<StudentRankVary> getTotalScoreUp() {
		return totalScoreUp;
	}
	public void setTotalScoreUp(List<StudentRankVary> totalScoreUp) {
		this.totalScoreUp = totalScoreUp;
	}
	public String getTotalDownName() {
		return totalDownName;
	}
	public void setTotalDownName(String totalDownName) {
		this.totalDownName = totalDownName;
	}
	public List<StudentRankVary> getTotalScoreDown() {
		return totalScoreDown;
	}
	public void setTotalScoreDown(List<StudentRankVary> totalScoreDown) {
		this.totalScoreDown = totalScoreDown;
	}

}