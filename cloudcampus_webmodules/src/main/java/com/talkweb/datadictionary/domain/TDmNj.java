package com.talkweb.datadictionary.domain;/******************************************************************************* * javaBeans * t_dm_nj --> TDmNj  * <table explanation> * @author 2015-03-01 17:58:18 *  */	public class TDmNj implements java.io.Serializable {	//field	/**  **/	private String dm;	/**  **/	private String userDm;	/**  **/	private String mc;	/**  **/	private String flag;	/**  **/	private String pycc;	//method	public String getDm() {		return dm;	}	public void setDm(String dm) {		this.dm = dm;	}	public String getUserDm() {		return userDm;	}	public void setUserDm(String userDm) {		this.userDm = userDm;	}	public String getMc() {		return mc;	}	public void setMc(String mc) {		this.mc = mc;	}	public String getFlag() {		return flag;	}	public void setFlag(String flag) {		this.flag = flag;	}	public String getPycc() {		return pycc;	}	public void setPycc(String pycc) {		this.pycc = pycc;	}	//override toString Method 	public String toString() {		StringBuffer sb=new StringBuffer();		sb.append("{");		sb.append("'dm':'"+this.getDm()+"',");		sb.append("'userDm':'"+this.getUserDm()+"',");		sb.append("'mc':'"+this.getMc()+"',");		sb.append("'flag':'"+this.getFlag()+"',");		sb.append("'pycc':'"+this.getPycc()+"'");		sb.append("}");		return sb.toString();	}}