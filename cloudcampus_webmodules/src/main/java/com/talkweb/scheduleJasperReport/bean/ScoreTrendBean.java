package com.talkweb.scheduleJasperReport.bean;

import java.util.List;

/**
 * 
 * 成绩打印-趋势分布图Bean
 *
*/
public class ScoreTrendBean {
   
    private String title = "";
    
    private String classTypeName = "";
    
    private double minRangeAxis; 
    private double maxRangeAxis;
       
    private String classNameOne = ""; 
    private List<TrendTableBean> trendDataOne;
    private List<TrendLine> lineListOne;
   
    private String classNameTwo ="";
    private List<TrendTableBean> trendDataTwo;
    private List<TrendLine> lineListTwo;
   
    private String classNameThree ="";
    private List<TrendTableBean> trendDataThree;
    private List<TrendLine> lineListThree;
   
    private String classNameFour ="";
    private List<TrendTableBean> trendDataFour;
    private List<TrendLine> lineListFour;
   
    private String classNameFive ="";
    private List<TrendTableBean> trendDataFive;
    private List<TrendLine> lineListFive;
    
    private String classNameSix ="";
    private List<TrendTableBean> trendDataSix;
    private List<TrendLine> lineListSix;
    
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
    
	public String getClassTypeName() {
		return classTypeName;
	}

	public void setClassTypeName(String classTypeName) {
		this.classTypeName = classTypeName;
	}

	public double getMinRangeAxis() {
		return minRangeAxis;
	}

	public void setMinRangeAxis(double minRangeAxis) {
		this.minRangeAxis = minRangeAxis;
	}

	public double getMaxRangeAxis() {
		return maxRangeAxis;
	}

	public void setMaxRangeAxis(double maxRangeAxis) {
		this.maxRangeAxis = maxRangeAxis;
	}

	public List<TrendTableBean> getTrendDataOne() {
		return trendDataOne;
	}

	public void setTrendDataOne(List<TrendTableBean> trendDataOne) {
		this.trendDataOne = trendDataOne;
	}

	public List<TrendTableBean> getTrendDataTwo() {
		return trendDataTwo;
	}

	public void setTrendDataTwo(List<TrendTableBean> trendDataTwo) {
		this.trendDataTwo = trendDataTwo;
	}

	public List<TrendTableBean> getTrendDataThree() {
		return trendDataThree;
	}

	public void setTrendDataThree(List<TrendTableBean> trendDataThree) {
		this.trendDataThree = trendDataThree;
	}

	public List<TrendTableBean> getTrendDataFour() {
		return trendDataFour;
	}

	public void setTrendDataFour(List<TrendTableBean> trendDataFour) {
		this.trendDataFour = trendDataFour;
	}

	public List<TrendTableBean> getTrendDataFive() {
		return trendDataFive;
	}

	public void setTrendDataFive(List<TrendTableBean> trendDataFive) {
		this.trendDataFive = trendDataFive;
	}

	public List<TrendTableBean> getTrendDataSix() {
		return trendDataSix;
	}

	public void setTrendDataSix(List<TrendTableBean> trendDataSix) {
		this.trendDataSix = trendDataSix;
	}

	public String getClassNameOne() {
		return classNameOne;
	}

	public void setClassNameOne(String classNameOne) {
		this.classNameOne = classNameOne;
	}

	public String getClassNameTwo() {
		return classNameTwo;
	}

	public void setClassNameTwo(String classNameTwo) {
		this.classNameTwo = classNameTwo;
	}

	public String getClassNameThree() {
		return classNameThree;
	}

	public void setClassNameThree(String classNameThree) {
		this.classNameThree = classNameThree;
	}

	public String getClassNameFour() {
		return classNameFour;
	}

	public void setClassNameFour(String classNameFour) {
		this.classNameFour = classNameFour;
	}

	public String getClassNameFive() {
		return classNameFive;
	}

	public void setClassNameFive(String classNameFive) {
		this.classNameFive = classNameFive;
	}

	public String getClassNameSix() {
		return classNameSix;
	}

	public void setClassNameSix(String classNameSix) {
		this.classNameSix = classNameSix;
	}

	public List<TrendLine> getLineListOne() {
		return lineListOne;
	}

	public void setLineListOne(List<TrendLine> lineListOne) {
		this.lineListOne = lineListOne;
	}

	public List<TrendLine> getLineListTwo() {
		return lineListTwo;
	}

	public void setLineListTwo(List<TrendLine> lineListTwo) {
		this.lineListTwo = lineListTwo;
	}

	public List<TrendLine> getLineListThree() {
		return lineListThree;
	}

	public void setLineListThree(List<TrendLine> lineListThree) {
		this.lineListThree = lineListThree;
	}

	public List<TrendLine> getLineListFour() {
		return lineListFour;
	}

	public void setLineListFour(List<TrendLine> lineListFour) {
		this.lineListFour = lineListFour;
	}

	public List<TrendLine> getLineListFive() {
		return lineListFive;
	}

	public void setLineListFive(List<TrendLine> lineListFive) {
		this.lineListFive = lineListFive;
	}

	public List<TrendLine> getLineListSix() {
		return lineListSix;
	}

	public void setLineListSix(List<TrendLine> lineListSix) {
		this.lineListSix = lineListSix;
	}
}