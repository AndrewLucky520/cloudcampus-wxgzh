package com.talkweb.scheduleJasperReport.bean;

import java.util.List;

public class GradeMiddle {
	
    private String compareTableOne = "";
	
	private List<HistogramChart> histogramOne;
	
	private List<CompareTable> tableOne;
	
    private String compareTableTwo = "";
	
	private List<HistogramChart> histogramTwo;
	
	private List<CompareTable> tableTwo;

	public String getCompareTableOne() {
		return compareTableOne;
	}

	public void setCompareTableOne(String compareTableOne) {
		this.compareTableOne = compareTableOne;
	}

	public List<HistogramChart> getHistogramOne() {
		return histogramOne;
	}

	public void setHistogramOne(List<HistogramChart> histogramOne) {
		this.histogramOne = histogramOne;
	}

	public List<CompareTable> getTableOne() {
		return tableOne;
	}

	public void setTableOne(List<CompareTable> tableOne) {
		this.tableOne = tableOne;
	}

	public String getCompareTableTwo() {
		return compareTableTwo;
	}

	public void setCompareTableTwo(String compareTableTwo) {
		this.compareTableTwo = compareTableTwo;
	}

	public List<HistogramChart> getHistogramTwo() {
		return histogramTwo;
	}

	public void setHistogramTwo(List<HistogramChart> histogramTwo) {
		this.histogramTwo = histogramTwo;
	}

	public List<CompareTable> getTableTwo() {
		return tableTwo;
	}

	public void setTableTwo(List<CompareTable> tableTwo) {
		this.tableTwo = tableTwo;
	}
	
}