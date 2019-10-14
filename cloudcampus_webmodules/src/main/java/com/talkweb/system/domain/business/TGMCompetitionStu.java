package com.talkweb.system.domain.business;
/**
 * T_GM_CompetitionStu竞赛组学生
 * @author zxy
 *
 */
public class TGMCompetitionStu {
private String xn;
private String xqm;
private String xxdm;
private String kslc;
private String xh;
private String kmdm;
public String getXn() {
	return xn;
}
public void setXn(String xn) {
	this.xn = xn;
}
public String getXqm() {
	return xqm;
}
public void setXqm(String xqm) {
	this.xqm = xqm;
}
public String getXxdm() {
	return xxdm;
}
public void setXxdm(String xxdm) {
	this.xxdm = xxdm;
}
public String getKslc() {
	return kslc;
}
public void setKslc(String kslc) {
	this.kslc = kslc;
}
public String getXh() {
	return xh;
}
public void setXh(String xh) {
	this.xh = xh;
}
public String getKmdm() {
	return kmdm;
}
public void setKmdm(String kmdm) {
	this.kmdm = kmdm;
}
public String toString() {
	StringBuffer sb=new StringBuffer();
	sb.append("{");
	sb.append("'xn':'"+this.getXn()+"',");
	sb.append("'xqm':'"+this.getXqm()+"',");
	sb.append("'kslc':'"+this.getKslc()+"',");
	sb.append("'xxdm':'"+this.getXxdm()+"',");	
	sb.append("'xh':'"+this.getXh()+"',");
	sb.append("'kmdm':'"+this.getKmdm()+"'");
	sb.append("}");
	return sb.toString();
}
}
