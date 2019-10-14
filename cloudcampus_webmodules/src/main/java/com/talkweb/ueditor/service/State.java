package com.talkweb.ueditor.service;

 
public interface State {
	
	public boolean isSuccess ();
	
	public void putInfo( String name, String val );
	
	public void putInfo ( String name, long val );
	
	public String gettInfo( String name );
	
	public void setProperty(boolean status ,  String info , int infoCode);
	
 
	
	public String toJSONString ();

}
