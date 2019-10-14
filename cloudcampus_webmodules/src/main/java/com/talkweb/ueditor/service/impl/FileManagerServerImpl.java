package com.talkweb.ueditor.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.ueditor.service.FileManagerServer;
import com.talkweb.ueditor.service.State;

 
 
@Component(value = "fileManagerServer")
public class FileManagerServerImpl implements FileManagerServer{
 
 
	@Override
	public State listFile ( List<JSONObject> list , int index ) {
		State multiState = new MultiState();
		State state = null;
		if ( index < 0 || index > list.size() ) {
			multiState.setProperty(true, null, -100);
			state = multiState;
		} else {
			state = this.getState( list );
		}
		
		state.putInfo( "start", index );
		state.putInfo( "total", list.size() );
		
		return state;
		
	}
	
	private State getState ( List<JSONObject> list ) {
		State multiState = new MultiState();
		State state = null;
		multiState.setProperty(true, null, -100);
 		MultiState multiState2 = (MultiState) multiState;

		for ( JSONObject obj : list ) {
			if ( obj == null ) {
				continue;
			}
			state = new BaseState();
			state.setProperty(true, null, -100);
			state.putInfo( "url", obj.getString("filePath"));
			 
			multiState2.addState( state );
		}
		
		return multiState2;
		
	}
	
 
	
 
	
}
