package com.talkweb.system.domain.business;/******************************************************************************* * javaBeans * t_gm_classscorelevelsequnce --> TGmClassscorelevelsequnce  * <table explanation> * @author 2015-03-25 10:50:31 *  */	public class TGmClassscorelevelsequnce implements java.io.Serializable {	//field	/**  **/	private String xn;	/**  **/	private String xqm;	/**  **/	private String kslc;	/**  **/	private String xxdm;	/**  **/	private String bh;	/**  **/	private String djxllb;	/**  **/	private String djxl;	/**  **/	private int rs;	/**  **/	private int ljrs;	/**  **/	private Object bl;	/**  **/	private int pm;	//method	public String getXn() {		return xn;	}	public void setXn(String xn) {		this.xn = xn;	}	public String getXqm() {		return xqm;	}	public void setXqm(String xqm) {		this.xqm = xqm;	}	public String getKslc() {		return kslc;	}	public void setKslc(String kslc) {		this.kslc = kslc;	}	public String getXxdm() {		return xxdm;	}	public void setXxdm(String xxdm) {		this.xxdm = xxdm;	}	public String getBh() {		return bh;	}	public void setBh(String bh) {		this.bh = bh;	}	public String getDjxllb() {		return djxllb;	}	public void setDjxllb(String djxllb) {		this.djxllb = djxllb;	}	public String getDjxl() {		return djxl;	}	public void setDjxl(String djxl) {		this.djxl = djxl;	}	public int getRs() {		return rs;	}	public void setRs(int rs) {		this.rs = rs;	}	public int getLjrs() {		return ljrs;	}	public void setLjrs(int ljrs) {		this.ljrs = ljrs;	}	public Object getBl() {		return bl;	}	public void setBl(Object bl) {		this.bl = bl;	}	public int getPm() {		return pm;	}	public void setPm(int pm) {		this.pm = pm;	}	//override toString Method 	public String toString() {		StringBuffer sb=new StringBuffer();		sb.append("{");		sb.append("'xn':'"+this.getXn()+"',");		sb.append("'xqm':'"+this.getXqm()+"',");		sb.append("'kslc':'"+this.getKslc()+"',");		sb.append("'xxdm':'"+this.getXxdm()+"',");		sb.append("'bh':'"+this.getBh()+"',");		sb.append("'djxllb':'"+this.getDjxllb()+"',");		sb.append("'djxl':'"+this.getDjxl()+"',");		sb.append("'rs':'"+this.getRs()+"',");		sb.append("'ljrs':'"+this.getLjrs()+"',");		sb.append("'bl':'"+this.getBl()+"',");		sb.append("'pm':'"+this.getPm()+"'");		sb.append("}");		return sb.toString();	}}