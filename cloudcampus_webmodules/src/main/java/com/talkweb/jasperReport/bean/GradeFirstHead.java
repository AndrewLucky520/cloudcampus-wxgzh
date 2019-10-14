package com.talkweb.jasperReport.bean;

import java.util.List;

public class GradeFirstHead {
	
	private String reportTitle = "";
	
	private String averageViewTip = "";
	
	private String passrateViewTip = "";
	
	private String finerateViewTip = "";
	
	private String ladderViewTip = "";
	
	private String sectionViewTip = "";
	
	private String compareTableZero = "";
	
	private List<HistogramChart> histogramZero;
	
	private List<CompareTable> tableZero;

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

	public String getAverageViewTip() {
		return averageViewTip;
	}

	public void setAverageViewTip(String averageViewTip) {
		this.averageViewTip = averageViewTip;
	}

	public String getPassrateViewTip() {
		return passrateViewTip;
	}

	public void setPassrateViewTip(String passrateViewTip) {
		this.passrateViewTip = passrateViewTip;
	}

	public String getFinerateViewTip() {
		return finerateViewTip;
	}

	public void setFinerateViewTip(String finerateViewTip) {
		this.finerateViewTip = finerateViewTip;
	}

	public String getLadderViewTip() {
		return ladderViewTip;
	}

	public void setLadderViewTip(String ladderViewTip) {
		this.ladderViewTip = ladderViewTip;
	}

	public String getSectionViewTip() {
		return sectionViewTip;
	}

	public void setSectionViewTip(String sectionViewTip) {
		this.sectionViewTip = sectionViewTip;
	}

	public String getCompareTableZero() {
		return compareTableZero;
	}

	public void setCompareTableZero(String compareTableZero) {
		this.compareTableZero = compareTableZero;
	}

	public List<HistogramChart> getHistogramZero() {
		return histogramZero;
	}

	public void setHistogramZero(List<HistogramChart> histogramZero) {
		this.histogramZero = histogramZero;
	}

	public List<CompareTable> getTableZero() {
		return tableZero;
	}

	public void setTableZero(List<CompareTable> tableZero) {
		this.tableZero = tableZero;
	}	
}