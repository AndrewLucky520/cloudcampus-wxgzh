package com.talkweb.common.threadprogress;

import java.util.HashMap;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.commondata.dao.RedisOperationDAO;

public class CommonRedisProgressUpdate implements Runnable {

	private String msg;
	private int code;
	private int ms;
	private int eachPercent;
	private int startPercent;
	private String dataKey;
	private Object data;

	public CommonRedisProgressUpdate(int code, String msg, int ms,
			int eachPercent, int startPercent, String dataKey, Object data) {
		this.code = code;
		this.eachPercent = eachPercent;
		this.ms = ms;
		this.msg = msg;
		this.startPercent = startPercent;
		this.dataKey = dataKey;
		this.data = data;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int msL = 1;
		ms = ms * 10;
		while (ms > 0) {
			ms--;
			msL++;
			try {
				int p = startPercent + (int) (float) msL * eachPercent / 10;
				RedisOperationDAO redisOperationDAO;
				WebApplicationContext wac = ContextLoader
						.getCurrentWebApplicationContext();
				redisOperationDAO = (RedisOperationDAO) wac
						.getBean("redisOperationDAOSDRTempDataImpl");

				updateProgress(code, msg, p, redisOperationDAO, dataKey, data);

				// updateProgress(msg1,st+(int)(float)msL*st/10,pd,1);
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@SuppressWarnings({ "null", "unchecked" })
	public void updateProgress(int code, String msg, int progress,
			RedisOperationDAO redisOperationDAO, String dataKey, Object data) {
		if (progress > 100)
			progress = 100;
		if (redisOperationDAO == null) {
			WebApplicationContext wac = ContextLoader
					.getCurrentWebApplicationContext();
			redisOperationDAO = (RedisOperationDAO) wac
					.getBean("redisOperationDAOSDRTempDataImpl");
		}
		Object pckey = dataKey;
		HashMap<String, Object> progressMap = null;
		try {
			progressMap = (HashMap<String, Object>) redisOperationDAO
					.get(pckey);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (progressMap == null) {
			progressMap = new HashMap<String, Object>();
		} else {
			if (progressMap.containsKey("progress")) {
				int pIn = (int) progressMap.get("progress");
				if (progress <= pIn&&progress!=0) {
					this.ms = 0;
					return;
				}

			}
		}
		progressMap.put("code", code);
		progressMap.put("data", data);
		progressMap.put("msg", msg);
		progressMap.put("progress", progress);
		try {

			redisOperationDAO.set(pckey, progressMap,
					CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
