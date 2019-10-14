package com.talkweb.system.domain.business;
/**
 * 班级报告参数表
 * @author zxy
 *
 */
public class TSARSettingBJ {
private String xxdm;
private String dm;
private int szz;
public String getXxdm() {
	return xxdm;
}
public void setXxdm(String xxdm) {
	this.xxdm = xxdm;
}
public String getDm() {
	return dm;
}
public void setDm(String dm) {
	this.dm = dm;
}
public int getSzz() {
	return szz;
}
public void setSzz(int szz) {
	this.szz = szz;
}
@Override
	public String toString() {
	StringBuffer sb=new StringBuffer();
	sb.append("{");
	sb.append("'xxdm':'"+this.getXxdm()+"',");
	sb.append("'dm':'"+this.getDm()+"',");
	sb.append("'szz':'"+this.getSzz()+"'");
	sb.append("}");
	return sb.toString();
	}
}
