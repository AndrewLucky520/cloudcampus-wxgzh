package com.talkweb.ueditor.service.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.talkweb.ueditor.service.State;
import com.talkweb.ueditor.util.AppInfo;
import com.talkweb.ueditor.util.MIMEType;
 
/**
 * 图片抓取器
 * @author hancong03@baidu.com
 *
 */

@Component(value = "imageHunter")
public class ImageHunter {


	private List<String> allowTypes = null;
	private long maxSize = -1;
	
	private List<String> filters = null;
	
	@Autowired
	StorageManager storageManager;
	
 
	public void setProperty(Map<String, Object> conf) {

		this.maxSize = (Long)conf.get( "maxSize" );
		this.allowTypes = Arrays.asList( (String[])conf.get( "allowFiles" ) );
		this.filters = Arrays.asList( (String[])conf.get( "filter" ) );
	}
	
 
	
	public State capture ( String[] list ) {
		State multiState = new MultiState();
		multiState.setProperty(true, null, -100);
		 
		
		for ( String source : list ) {
			
			((MultiState)multiState).addState( captureRemoteData( source ) );
		}
		
		return multiState;
		
	}

	public State captureRemoteData ( String urlStr ) {
		
		HttpURLConnection connection = null;
		URL url = null;
		String suffix = null;
		State state = null;
		try {
			url = new URL( urlStr );

			if ( !validHost( url.getHost() ) ) {
				state = new BaseState();
				state.setProperty(false, null, AppInfo.PREVENT_HOST);
				return state;
			}
			
			connection = (HttpURLConnection) url.openConnection();
		
			connection.setInstanceFollowRedirects( true );
			connection.setUseCaches( true );
		
			if ( !validContentState( connection.getResponseCode() ) ) {
				state = new BaseState();
				state.setProperty(false, null, AppInfo.CONNECTION_ERROR);
				return state;
			}
			
			suffix = MIMEType.getSuffix( connection.getContentType() );
			
			if ( !validFileType( suffix ) ) {
				state = new BaseState();
				state.setProperty(false, null, AppInfo.NOT_ALLOW_FILE_TYPE);
				return state;
			}
			
			if ( !validFileSize( connection.getContentLength() ) ) {
				state.setProperty(false, null, AppInfo.MAX_SIZE);
				return state;
			}

			state = storageManager.saveFileByInputStream( connection.getInputStream());
			
			if ( state.isSuccess() ) {
				state.putInfo( "source", urlStr );
			}
			
			return state;
			
		} catch ( Exception e ) {
			state.setProperty(false, null, AppInfo.REMOTE_FAIL);
			return state;
		}
		
	}
	
 
	
	private boolean validHost ( String hostname ) {
		
		return !filters.contains( hostname );
		
	}
	
	private boolean validContentState ( int code ) {
		
		return HttpURLConnection.HTTP_OK == code;
		
	}
	
	private boolean validFileType ( String type ) {
		
		return this.allowTypes.contains( type );
		
	}
	
	private boolean validFileSize ( int size ) {
		return size < this.maxSize;
	}
	
}
