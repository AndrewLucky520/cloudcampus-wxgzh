package com.talkweb.system.domain.business;/******************************************************************************* * javaBeans * t_gm_scorerankstatistics_mk_range --> TGmScorerankstatisticsMkRange  * <table explanation> * @author 2015-03-25 10:50:33 *  */	public class TGmScorerankstatisticsMkRange implements java.io.Serializable {	//field	/**  **/	private String xn;	/**  **/	private String xqm;	/**  **/	private String kslc;	/**  **/	private String xxdm;	/**  **/	private String bh;	/**  **/	private String kmdm;	/**  **/	private String pmfbdm;	/**  **/	private int rs;	/**  **/	private int ljrs;	//method	public String getXn() {		return xn;	}	public void setXn(String xn) {		this.xn = xn;	}	public String getXqm() {		return xqm;	}	public void setXqm(String xqm) {		this.xqm = xqm;	}	public String getKslc() {		return kslc;	}	public void setKslc(String kslc) {		this.kslc = kslc;	}	public String getXxdm() {		return xxdm;	}	public void setXxdm(String xxdm) {		this.xxdm = xxdm;	}	public String getBh() {		return bh;	}	public void setBh(String bh) {		this.bh = bh;	}	public String getKmdm() {		return kmdm;	}	public void setKmdm(String kmdm) {		this.kmdm = kmdm;	}	public String getPmfbdm() {		return pmfbdm;	}	public void setPmfbdm(String pmfbdm) {		this.pmfbdm = pmfbdm;	}	public int getRs() {		return rs;	}	public void setRs(int rs) {		this.rs = rs;	}	public int getLjrs() {		return ljrs;	}	public void setLjrs(int ljrs) {		this.ljrs = ljrs;	}	//override toString Method 	public String toString() {		StringBuffer sb=new StringBuffer();		sb.append("{");		sb.append("'xn':'"+this.getXn()+"',");		sb.append("'xqm':'"+this.getXqm()+"',");		sb.append("'kslc':'"+this.getKslc()+"',");		sb.append("'xxdm':'"+this.getXxdm()+"',");		sb.append("'bh':'"+this.getBh()+"',");		sb.append("'kmdm':'"+this.getKmdm()+"',");		sb.append("'pmfbdm':'"+this.getPmfbdm()+"',");		sb.append("'rs':'"+this.getRs()+"',");		sb.append("'ljrs':'"+this.getLjrs()+"'");		sb.append("}");		return sb.toString();	}}