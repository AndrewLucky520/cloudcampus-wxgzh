package com.talkweb.system.domain.business;/******************************************************************************* * javaBeans * t1 --> T1  * <table explanation> * @author 2015-03-01 17:58:17 *  */	public class T1 implements java.io.Serializable {	//field	/**  **/	private int a;	/**  **/	private String b;	//method	public int getA() {		return a;	}	public void setA(int a) {		this.a = a;	}	public String getB() {		return b;	}	public void setB(String b) {		this.b = b;	}	//override toString Method 	public String toString() {		StringBuffer sb=new StringBuffer();		sb.append("{");		sb.append("'a':'"+this.getA()+"',");		sb.append("'b':'"+this.getB()+"'");		sb.append("}");		return sb.toString();	}}