package com.talkweb.system.domain.business;/******************************************************************************* * javaBeans * t_gm_zfqjsz --> TGmZfqjsz  * <table explanation> * @author 2015-03-25 10:50:33 *  */	public class TGmZfqjsz implements java.io.Serializable {	//field	/**  **/	private String kslc;	/**  **/	private String xxdm;	/**  **/	private String nj;	/**  **/	private String fzdm;	/**  **/	private String fs;	/**  **/	private int bl;	private int zhqjbl;	/**  **/	private int fz;	private int zhqjfz;	/**  **/	private String zdytext;	private int dkbl;	private int dkzhqjbl;	//method	public String getKslc() {		return kslc;	}	public void setKslc(String kslc) {		this.kslc = kslc;	}	public String getXxdm() {		return xxdm;	}	public void setXxdm(String xxdm) {		this.xxdm = xxdm;	}	public String getNj() {		return nj;	}	public void setNj(String nj) {		this.nj = nj;	}	public String getFzdm() {		return fzdm;	}	public void setFzdm(String fzdm) {		this.fzdm = fzdm;	}	public String getFs() {		return fs;	}	public void setFs(String fs) {		this.fs = fs;	}	public int getBl() {		return bl;	}	public void setBl(int bl) {		this.bl = bl;	}	public int getFz() {		return fz;	}	public void setFz(int fz) {		this.fz = fz;	}	public String getZdytext() {		return zdytext;	}	public void setZdytext(String zdytext) {		this.zdytext = zdytext;	}			public int getZhqjbl() {		return zhqjbl;	}	public void setZhqjbl(int zhqjbl) {		this.zhqjbl = zhqjbl;	}	public int getZhqjfz() {		return zhqjfz;	}	public void setZhqjfz(int zhqjfz) {		this.zhqjfz = zhqjfz;	}	public int getDkbl() {		return dkbl;	}	public void setDkbl(int dkbl) {		this.dkbl = dkbl;	}	public int getDkzhqjbl() {		return dkzhqjbl;	}	public void setDkzhqjbl(int dkzhqjbl) {		this.dkzhqjbl = dkzhqjbl;	}	//override toString Method 	public String toString() {		StringBuffer sb=new StringBuffer();		sb.append("{");		sb.append("'kslc':'"+this.getKslc()+"',");		sb.append("'xxdm':'"+this.getXxdm()+"',");		sb.append("'nj':'"+this.getNj()+"',");		sb.append("'fzdm':'"+this.getFzdm()+"',");		sb.append("'fs':'"+this.getFs()+"',");		sb.append("'bl':'"+this.getBl()+"',");		sb.append("'zhqjbl':'"+this.getZhqjbl()+"',");		sb.append("'zhqjfz':'"+this.getZhqjfz()+"',");		sb.append("'fz':'"+this.getFz()+"',");		sb.append("'zdytext':'"+this.getZdytext()+"',");		sb.append("'dkbl':'"+this.getDkbl()+"',");		sb.append("'dkzhqjbl':'"+this.getDkzhqjbl()+"'");		sb.append("}");		return sb.toString();	}}