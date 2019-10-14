package com.talkweb.scoreManage.proc;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.commondata.dao.RedisOperationDAO;

public class ScoreProgressProc extends Thread {

	private String msg;
	private int code;
	private int ms;
	private int eachPercent;
	private int startPercent;
	private String progressKey;

	public ScoreProgressProc(String msg, int code, int ms, int eachPercent, int startPercent, String progressKey) {
		this.code = code;
		this.eachPercent = eachPercent;
		this.ms = ms;
		this.msg = msg;
		this.startPercent = startPercent;
		this.progressKey = progressKey;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int msL = 1;
		ms = ms * 10;
		while (ms > 0 && !isInterrupted()) {
			ms--;
			msL++;
			try {
				int p = startPercent + (int) (float) msL * eachPercent / 10;
				RedisOperationDAO redisOperationDAO;
				WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
				redisOperationDAO = (RedisOperationDAO) wac.getBean("redisOperationDAOSDRTempDataImpl");

				updateProgress(p, redisOperationDAO);

				ScoreProgressProc.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private void updateProgress(int progress, RedisOperationDAO redisOperationDAO) throws Exception {
		ProgressBar progressProc = (ProgressBar) redisOperationDAO.get(progressKey);
		if (progressProc.getProgress() < progress) {
			progressProc.setProgressInfo(code, progress, msg);
			redisOperationDAO.set(progressKey, progressProc, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
		}
	}
}
