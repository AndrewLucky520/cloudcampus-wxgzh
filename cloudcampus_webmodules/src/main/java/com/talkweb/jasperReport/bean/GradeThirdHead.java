package com.talkweb.jasperReport.bean;

import java.util.List;

public class GradeThirdHead {
	
    private String classViewTip = "";
	
    private String compareTableZero = "";
	
	private List<HistogramChart> histogramZero;
	
	private List<CompareTable> tableZero;

	public String getClassViewTip() {
		return classViewTip;
	}

	public void setClassViewTip(String classViewTip) {
		this.classViewTip = classViewTip;
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