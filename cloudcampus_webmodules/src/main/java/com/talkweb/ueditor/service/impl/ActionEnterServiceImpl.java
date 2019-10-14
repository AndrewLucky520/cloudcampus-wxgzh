package com.talkweb.ueditor.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.ueditor.service.ActionEnterService;
import com.talkweb.ueditor.service.ConfigManagerServer;
import com.talkweb.ueditor.service.FileManagerServer;
import com.talkweb.ueditor.service.State;
import com.talkweb.ueditor.service.UEditorService;
import com.talkweb.ueditor.service.UploaderService;
import com.talkweb.ueditor.util.ActionMap;
import com.talkweb.ueditor.util.AppInfo;
 

@Service
public class ActionEnterServiceImpl implements ActionEnterService{

	@Autowired
	private ConfigManagerServer configManager = null;
	
	@Autowired
	@Qualifier("binary") 
	UploaderService uploaderServiceBinary;
	
	@Autowired
	@Qualifier("base64") 
	UploaderService uploaderServiceBase64;
 
	
	@Autowired
	private FileManagerServer fileManager = null;
	
	@Autowired
	private ImageHunter imageHunter = null;
	
	@Autowired
	UEditorService ueditorService;
	 
	public String exec(HttpServletRequest request) {
		String actionType  =  request.getParameter("action");
		ServletContext servletContext = request.getSession().getServletContext();    
		String rootPath = servletContext.getRealPath("/");
		configManager.initEnv(rootPath, "WEB-INF/classes/config/");
		return this.invoke(request ,actionType );
	}
	
	
	private String invoke(HttpServletRequest request , String actionType) {
		State state = null;
		if ( actionType == null || !ActionMap.mapping.containsKey( actionType ) ) {
			state = new BaseState();
			state.setProperty(false, null, AppInfo.INVALID_ACTION);
			return state.toJSONString();
		}
		
		if ( this.configManager == null || !this.configManager.valid() ) {
			state = new BaseState();
			state.setProperty(false, null, AppInfo.CONFIG_ERROR);
			return state.toJSONString();
		}
		
 
		int actionCode = ActionMap.getType( actionType );
		
		Map<String, Object> conf = null;
		String rs =    request.getSession().getAttribute("xxdm")+"";
		switch ( actionCode ) {
		
			case ActionMap.CONFIG:
				return this.configManager.getAllConfig().toString();
				
			case ActionMap.UPLOAD_IMAGE:
			case ActionMap.UPLOAD_SCRAWL:
			case ActionMap.UPLOAD_VIDEO:
			case ActionMap.UPLOAD_FILE:
				conf = this.configManager.getConfig( actionCode );
				if ("true".equals(conf.get("isBase64"))) {
					state =  uploaderServiceBase64.save(request, conf);
				}else {
					state =  uploaderServiceBinary.save(request, conf);
				}
				
				JSONObject object = new JSONObject();
				object.put("fileId", UUIDUtil.getUUID());
				object.put("schoolId", rs);
				object.put("filePath", state.gettInfo("url"));
				object.put("fileName", state.gettInfo("original"));
				object.put("classfy", actionCode+"");
				object.put("fileType", state.gettInfo("type"));
				object.put("submitTime", new Date());
				
				ueditorService.insertUEditorRecord(object);
				
				break;
				
			case ActionMap.CATCH_IMAGE:
				conf = configManager.getConfig( actionCode );
				String[] list = request.getParameterValues( (String)conf.get( "fieldName" ) );
				imageHunter.setProperty(conf);
				state = imageHunter.capture( list );
				break;
				
			case ActionMap.LIST_IMAGE:
			case ActionMap.LIST_FILE:
				JSONObject obj = new JSONObject();
				if (7==actionCode) {
					obj.put("classfy", "1");
				}else {
					obj.put("classfy", "4");
				}
				obj.put("schoolId", rs);
				
				List<JSONObject> ueList = ueditorService.getUEditorList(obj);
				
				conf = configManager.getConfig( actionCode );
				String starts = request.getParameter( "start" );
				int start = this.getStartIndex(starts);
				 
				state = fileManager.listFile(ueList ,  start);

				break;
				
		}
		
		return state.toJSONString();
		
	
	 
	}
	
	public int getStartIndex (String start) {
		try {
			return Integer.parseInt( start );
		} catch ( Exception e ) {
			return 0;
		}
		
	}
	

}
