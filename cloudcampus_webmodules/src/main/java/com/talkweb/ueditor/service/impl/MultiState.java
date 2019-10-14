package com.talkweb.ueditor.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.talkweb.common.tools.StringUtil;
import com.talkweb.ueditor.service.State;
import com.talkweb.ueditor.util.AppInfo;
import com.talkweb.ueditor.util.Encoder;

 
  
public class MultiState implements State {
	
 
	private boolean state = false;
	private String info = null;
	private Map<String, Long> intMap = new HashMap<String, Long>();
	private Map<String, String> infoMap = new HashMap<String, String>();
	private List<String> stateList = new ArrayList<String>();
	
 
	public MultiState(){
		this.state = true ;
	}
 
	
 
	
	@Override
	public boolean isSuccess() {
		return this.state;
	}
	
	public void addState ( State state ) {
		stateList.add( state.toJSONString() );
	}
	
	public void reMoveAllState ( ) {
		stateList.clear();
	}
	

	/**
	 * 该方法调用无效果
	 */
	@Override
	public void putInfo(String name, String val) {
		this.infoMap.put(name, val);
	}

	@Override
	public String toJSONString() {
		
		String stateVal = this.isSuccess() ? AppInfo.getStateInfo( AppInfo.SUCCESS ) : this.info;
		
		StringBuilder builder = new StringBuilder();
		
		builder.append( "{\"state\": \"" + stateVal + "\"" );
		
		// 数字转换
		Iterator<String> iterator = this.intMap.keySet().iterator();
		
		while ( iterator.hasNext() ) {
			
			stateVal = iterator.next();
			
			builder.append( ",\""+ stateVal +"\": " + this.intMap.get( stateVal ) );
			
		}
		
		iterator = this.infoMap.keySet().iterator();
		
		while ( iterator.hasNext() ) {
			
			stateVal = iterator.next();
			
			builder.append( ",\""+ stateVal +"\": \"" + this.infoMap.get( stateVal ) + "\"" );
			
		}
		
		builder.append( ", list: [" );
		
		
		iterator = this.stateList.iterator();
		
		while ( iterator.hasNext() ) {
			
			builder.append( iterator.next() + "," );
			
		}
		
		if ( this.stateList.size() > 0 ) {
			builder.deleteCharAt( builder.length() - 1 );
		}
		
		builder.append( " ]}" );

		return Encoder.toUnicode( builder.toString() );

	}

	@Override
	public void putInfo(String name, long val) {
		this.intMap.put( name, val );
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
