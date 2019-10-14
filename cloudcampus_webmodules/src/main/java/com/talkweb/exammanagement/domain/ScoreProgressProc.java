package com.talkweb.exammanagement.domain;

import java.util.Hashtable;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.commondata.dao.RedisOperationDAO;

public class ScoreProgressProc extends Thread {
	
	
	private String msg;
	private String processId;
	private int code;
	private int ms;
	private int eachPercent;
	private int startPercent;
	private Hashtable<String, JSONObject> progressMap;
	private String xxdm;
	private String progressMapKey;

	public ScoreProgressProc(String msg,String processId,int code,int ms,int eachPercent,int startPercent,Hashtable<String, JSONObject> progressMap,String xxdm ,String progressMapKey) {
		this.processId = processId;
		this.code = code;
		this.eachPercent = eachPercent;
		this.ms = ms;
		this.msg = msg;
		this.progressMap = progressMap;
		this.startPercent = startPercent;
		this.xxdm = xxdm;
		this.progressMapKey = progressMapKey;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int msL = 1;
		ms = ms*10;
		while(ms>0){
			ms --;
			 msL++;
			try {
				int p = startPercent+(int)(float)msL*eachPercent/10;
				RedisOperationDAO redisOperationDAO;
				WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            	redisOperationDAO = (RedisOperationDAO) wac.getBean("redisOperationDAOSDRTempDataImpl");
            		
            	updateProgress(msg,p,processId,code,   redisOperationDAO,progressMapKey);
				
//				updateProgress(msg1,st+(int)(float)msL*st/10,pd,1);
				ScoreProgressProc.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private void updateProgress(String msg,int progress,String processId,int code,RedisOperationDAO redisOperationDAO, String progressMapKey){
		JSONObject data = new JSONObject();
		JSONObject toFront = new JSONObject();
		Object pckey = progressMapKey;
    	Hashtable<String, JSONObject> progressMap = null;
		try {
			progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(pckey);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(progressMap.containsKey(processId)){
			toFront = progressMap.get(processId);
		}
		if(toFront.containsKey("data")){
			data  = toFront.getJSONObject("data");
		}
		if(data.containsKey("progress")&&data.getIntValue("progress")>progress){
			
		}else{
			
			data.put("progress", progress);
			data.put("msg", msg);
			toFront.put("data", data);
			toFront.put("code", code);
			progressMap.put(processId, toFront);
			 progressMap.put(processId, toFront);
             try {
            	
				redisOperationDAO.set(pckey, progressMap,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}
