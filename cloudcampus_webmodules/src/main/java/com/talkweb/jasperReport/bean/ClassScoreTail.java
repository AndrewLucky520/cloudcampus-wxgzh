package com.talkweb.jasperReport.bean;

import java.util.List;

/**
 * 
 * 班级报告打印-尾页bean
 *
*/
public class ClassScoreTail {
	
    private List<ScorePlacesSet> scoreStudentCount;
	
	private List<ScoreSectionSet> sectionCount;
	
	private List<TrendLine> scoreTracking;	
	
	private String trackingExplanation = "";

	public List<ScorePlacesSet> getScoreStudentCount() {
		return scoreStudentCount;
	}

	public void setScoreStudentCount(List<ScorePlacesSet> scoreStudentCount) {
		this.scoreStudentCount = scoreStudentCount;
	}

	public List<ScoreSectionSet> getSectionCount() {
		return sectionCount;
	}

	public void setSectionCount(List<ScoreSectionSet> sectionCount) {
		this.sectionCount = sectionCount;
	}

	public List<TrendLine> getScoreTracking() {
		return scoreTracking;
	}

	public void setScoreTracking(List<TrendLine> scoreTracking) {
		this.scoreTracking = scoreTracking;
	}

	public String getTrackingExplanation() {
		return trackingExplanation;
	}

	public void setTrackingExplanation(String trackingExplanation) {
		this.trackingExplanation = trackingExplanation;
	}

}