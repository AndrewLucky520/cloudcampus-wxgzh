package com.talkweb.ueditor.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.talkweb.common.tools.StringUtil;
import com.talkweb.ueditor.service.State;
import com.talkweb.ueditor.util.AppInfo;
import com.talkweb.ueditor.util.Encoder;
 
public class BaseState implements State {

	private boolean state = false;
	private String info = null;
	
	private Map<String, String> infoMap = new HashMap<String, String>();
	
 
	public BaseState () {
		this.state = true;
	}
	
 
 
	public boolean isSuccess () {
		return this.state;
	}
	
	public void setState ( boolean state ) {
		this.state = state;
	}
	
	public void setInfo ( String info ) {
		this.info = info;
	}
	
	public void setInfo ( int infoCode ) {
		this.info = AppInfo.getStateInfo( infoCode );
	}
	
	@Override
	public String toJSONString() {
		return this.toString();
	}
	
	public String toString () {
		
		String key = null;
		String stateVal = this.isSuccess() ? AppInfo.getStateInfo( AppInfo.SUCCESS ) : this.info;
		
		StringBuilder builder = new StringBuilder();
		
		builder.append( "{\"state\": \"" + stateVal + "\"" );
		
		Iterator<String> iterator = this.infoMap.keySet().iterator();
		
		while ( iterator.hasNext() ) {
			
			key = iterator.next();
			
			builder.append( ",\"" + key + "\": \"" + this.infoMap.get(key) + "\"" );
			
		}
		
		builder.append( "}" );

		return Encoder.toUnicode( builder.toString() );

	}

	@Override
	public void putInfo(String name, String val) {
		this.infoMap.put(name, val);
	}

	@Override
	public void putInfo(String name, long val) {
		this.putInfo(name, val+"");
	}

	@Override
	public String gettInfo(String name) {
		return this.infoMap.get(name);
	}



	@Override
	public void setProperty(boolean status, String info, int infoCode) {
		this.state = status;
		if (! StringUtil.isEmpty(info)) {
			this.info = info;
		}else if(infoCode != -100){
			this.info = AppInfo.getStateInfo( infoCode );
		}
		
	}
 

}
