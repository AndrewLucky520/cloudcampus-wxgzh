package com.talkweb.ueditor.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.ueditor.service.ConfigManagerServer;
import com.talkweb.ueditor.util.ActionMap;

 
@Component(value = "configManager")
public  class ConfigManagerServerImpl implements ConfigManagerServer{

	private static final String configFileName = "config.json";
	private JSONObject jsonConfig = null;
	// 涂鸦上传filename定义
	private final static String SCRAWL_FILE_NAME = "scrawl";
	// 远程图片抓取filename定义
	private final static String REMOTE_FILE_NAME = "remote";
 
	private  String parentPath = null;
	
	
 
	
	 
	public void initEnv(String rootPath, String uri) {
		rootPath = rootPath.replace( "\\", "/" );
		this.parentPath =  rootPath + uri ;
		try{
			String configContent = this.readFile( this.getConfigPath() );
			JSONObject jsonConfig = new JSONObject(  );
			jsonConfig = jsonConfig.parseObject(configContent);
			this.jsonConfig = jsonConfig;
		} catch ( Exception e ) {
			this.jsonConfig = null;
		}
		
		
	}
 
 
 
	public Map<String, Object> getConfig(int type) {

		Map<String, Object> conf = new HashMap<String, Object>();
 
		
		switch ( type ) {
		
			case ActionMap.UPLOAD_FILE:
				conf.put( "isBase64", "false" );
				conf.put( "maxSize", this.jsonConfig.getLong( "fileMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "fileAllowFiles" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "fileFieldName" ) );
				 
				break;
				
			case ActionMap.UPLOAD_IMAGE:
				conf.put( "isBase64", "false" );
				conf.put( "maxSize", this.jsonConfig.getLong( "imageMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "imageAllowFiles" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "imageFieldName" ) );
				 
				break;
				
			case ActionMap.UPLOAD_VIDEO:
				conf.put( "maxSize", this.jsonConfig.getLong( "videoMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "videoAllowFiles" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "videoFieldName" ) );
				 
				break;
				
			case ActionMap.UPLOAD_SCRAWL:
				conf.put( "filename", this.SCRAWL_FILE_NAME );
				conf.put( "maxSize", this.jsonConfig.getLong( "scrawlMaxSize" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "scrawlFieldName" ) );
				conf.put( "isBase64", "true" );
				 
				break;
				
			case ActionMap.CATCH_IMAGE:
				conf.put( "filename", this.REMOTE_FILE_NAME );
				conf.put( "filter", this.getArray( "catcherLocalDomain" ) );
				conf.put( "maxSize", this.jsonConfig.getLong( "catcherMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "catcherAllowFiles" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "catcherFieldName" ) + "[]" );
				 
				break;
				
			case ActionMap.LIST_IMAGE:
				conf.put( "allowFiles", this.getArray( "imageManagerAllowFiles" ) );
				conf.put( "dir", this.jsonConfig.getString( "imageManagerListPath" ) );
				conf.put( "count", this.jsonConfig.getInteger( "imageManagerListSize" ) );
				break;
				
			case ActionMap.LIST_FILE:
				conf.put( "allowFiles", this.getArray( "fileManagerAllowFiles" ) );
				conf.put( "dir", this.jsonConfig.getString( "fileManagerListPath" ) );
				conf.put( "count", this.jsonConfig.getInteger( "fileManagerListSize" ) );
				break;
				
		}
 
		conf.put( "baseUrl", this.jsonConfig.getString( "baseUrl" ));
		
		return conf;
		
	
	}

	 
	public JSONObject getAllConfig() {
		return this.jsonConfig;
	}
	
	
	
	private String[] getArray ( String key ) {
		
		JSONArray jsonArray = this.jsonConfig.getJSONArray( key );
		String[] result = new String[ jsonArray.size() ];
		
		for ( int i = 0, len = jsonArray.size(); i < len; i++ ) {
			result[i] = jsonArray.getString( i );
		}
		
		return result;
		
	}
	
	
	private String getConfigPath () {
        String path = this.getClass().getResource("/").getPath() + this.configFileName;
        if (new File(path).exists()) {
          return path;
        }else {          
          return this.parentPath + File.separator + this.configFileName;
        }
	}
	
	
	private String readFile ( String path ) throws IOException {
		
		StringBuilder builder = new StringBuilder();
		
		try {
			
			InputStreamReader reader = new InputStreamReader( new FileInputStream( path ), "UTF-8" );
			BufferedReader bfReader = new BufferedReader( reader );
			
			String tmpContent = null;
			
			while ( ( tmpContent = bfReader.readLine() ) != null ) {
				builder.append( tmpContent );
			}
			
			bfReader.close();
			
		} catch ( UnsupportedEncodingException e ) {
			// 忽略
		}
		
		return this.filter( builder.toString() );
		
	}
	
	
	// 过滤输入字符串, 剔除多行注释以及替换掉反斜杠
	private String filter ( String input ) {
		
		return input.replaceAll( "/\\*[\\s\\S]*?\\*/", "" );
		
	}


	 
	public boolean valid() {
		return this.jsonConfig != null;
	}

	
	
    public static String getRootPath(HttpServletRequest request, Map<String, Object> conf) {
        Object rootPath = request.getAttribute("rootPath");
        if (rootPath != null) {
            return rootPath + "" + File.separatorChar;
        } else {
            return conf.get("rootPath") + "";
        }
    }

	
}
