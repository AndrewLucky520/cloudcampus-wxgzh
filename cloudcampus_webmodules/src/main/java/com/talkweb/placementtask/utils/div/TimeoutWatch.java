package com.talkweb.placementtask.utils.div;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutWatch {
	
	Logger logger = LoggerFactory.getLogger(TimeoutWatch.class);
	
	private Long timeOutSeconds;

	private long startTimeMillis;

	public TimeoutWatch(Long timeOutSeconds) {
		this.startTimeMillis = System.currentTimeMillis();
		this.timeOutSeconds = timeOutSeconds;
	}
	
	public double getRunTimeSeconds() throws IllegalStateException {
		long lastTime = System.currentTimeMillis() - this.startTimeMillis;
		return lastTime / 1000.0;
	}
	
	
	public boolean isTimeout() {
		double end = this.getRunTimeSeconds();
		boolean flag = end >= this.timeOutSeconds;
		if(flag) {
			logger.info("TimeoutWatch is timeout timeOutSeconds:{}, RunTimeSeconds:", this.timeOutSeconds, end);
		}
		return flag;
	}


	public Long getTimeOutSeconds() {
		return timeOutSeconds;
	}

	public void setTimeOutSeconds(Long timeOutSeconds) {
		this.timeOutSeconds = timeOutSeconds;
	}
	
	
	
}
