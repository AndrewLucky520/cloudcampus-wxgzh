package com.talkweb.system.domain.business;/******************************************************************************* * javaBeans * t_gm_scoretjmk 统计模块--> TGmScoretjmk  * <table explanation> * @author 2015-03-25 10:50:33 *  */	public class TGmScoretjmk implements java.io.Serializable {	//field	/**  **/	private String kslc;	/**  **/	private String xxdm;	/**  **/	private String nj;	/**  **/	private String fzdm;	/**  **/	private String kmdm;	//method	public String getKslc() {		return kslc;	}	public void setKslc(String kslc) {		this.kslc = kslc;	}	public String getXxdm() {		return xxdm;	}	public void setXxdm(String xxdm) {		this.xxdm = xxdm;	}	public String getNj() {		return nj;	}	public void setNj(String nj) {		this.nj = nj;	}	public String getFzdm() {		return fzdm;	}	public void setFzdm(String fzdm) {		this.fzdm = fzdm;	}	public String getKmdm() {		return kmdm;	}	public void setKmdm(String kmdm) {		this.kmdm = kmdm;	}	//override toString Method 	public String toString() {		StringBuffer sb=new StringBuffer();		sb.append("{");		sb.append("'kslc':'"+this.getKslc()+"',");		sb.append("'xxdm':'"+this.getXxdm()+"',");		sb.append("'nj':'"+this.getNj()+"',");		sb.append("'fzdm':'"+this.getFzdm()+"',");		sb.append("'kmdm':'"+this.getKmdm()+"'");		sb.append("}");		return sb.toString();	}}