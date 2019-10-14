package com.talkweb.timetable.dynamicProgram.core;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.commondata.dao.RedisOperationDAO;

public class DynamicPorcessProgressProc extends Thread {
	
	
	private String msg;
	private String processId;
	private int code;
	private int ms;
	private int eachPercent;
	private int startPercent;

	/**
	 * @param msg 信息
	 * @param processId
	 * @param code code
	 * @param ms 多少秒
	 * @param eachPercent 每秒进度
	 * @param startPercent 起始进度
	 */
	public DynamicPorcessProgressProc(String msg,String processId,int code,int ms,int eachPercent,int startPercent ) {
		this.processId = processId;
		this.code = code;
		this.eachPercent = eachPercent;
		this.ms = ms;
		this.msg = msg;
		this.startPercent = startPercent;
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
            		
            	Object arrangeKey = "timetable." + processId
        				+ ".courseSmtArrange.progress";
            	JSONObject rs  =null;
            	try {
					rs = (JSONObject) redisOperationDAO.get(arrangeKey);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	if(rs!=null){
            		int pg = rs.getIntValue("arrangeProgress");
            		if(p<pg){
            			return;
            		}
            	}
            	updateArrangeProgress(msg,p,processId,code,   redisOperationDAO,null);
				
//				updateProgress(msg1,st+(int)(float)msL*st/10,pd,1);
				DynamicPorcessProgressProc.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void updateArrangeProgress(String msg,int progress,String processId,int code,RedisOperationDAO redisOperationDAO, String progressMapKey) {

		JSONObject rs = new JSONObject();
		rs.put("arrangeCode", code);
		rs.put("arrangeProgress", progress);
		rs.put("arrangeMsg", msg);

		Object arrangeKey = "timetable." + processId
				+ ".courseSmtArrange.progress";
		try {
			redisOperationDAO.set(arrangeKey, rs,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
