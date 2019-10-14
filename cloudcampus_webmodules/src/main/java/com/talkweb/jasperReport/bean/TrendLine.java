package com.talkweb.jasperReport.bean;

/**
 * 
 * 成绩打印-线坐标
 *
*/
public class TrendLine {
	
	private String x1 = "";
	
	private float y1;
	
	private String x2 = "";
	
	private float y2;
	
	private String name = "";
	
	private String key = "";

	public String getX1() {
		return x1;
	}

	public void setX1(String x1) {
		this.x1 = x1;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public float getY1() {
		return y1;
	}

	public void setY1(float y1) {
		this.y1 = y1;
	}

	public String getX2() {
		return x2;
	}

	public void setX2(String x2) {
		this.x2 = x2;
	}

	public String getName() {
		return name;
	}

	public float getY2() {
		return y2;
	}

	public void setY2(float y2) {
		this.y2 = y2;
	}

	public void setName(String name) {
		this.name = name;
	}	

}