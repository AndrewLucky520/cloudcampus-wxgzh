package com.talkweb.jasperReport.bean;

import java.util.List;
import java.util.Map;

public class HistogramTable {
	
    private List<HistogramChart> histogram;
	
	private List<CompareTable> table;
	
    private String compareTable = "";
    
    private Map<String,String[]> sortMap;

	public List<HistogramChart> getHistogram() {
		return histogram;
	}

	public void setHistogram(List<HistogramChart> histogram) {
		this.histogram = histogram;
	}

	public List<CompareTable> getTable() {
		return table;
	}

	public void setTable(List<CompareTable> table) {
		this.table = table;
	}

	public String getCompareTable() {
		return compareTable;
	}

	public void setCompareTable(String compareTable) {
		this.compareTable = compareTable;
	}

	public Map<String, String[]> getSortMap() {
		return sortMap;
	}

	public void setSortMap(Map<String, String[]> sortMap) {
		this.sortMap = sortMap;
	}

}