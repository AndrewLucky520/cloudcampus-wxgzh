package com.talkweb.placementtask.utils.div.dto;

public class SignalParam {
	
	/**
	 * 响应类型
	 * @author hushow
	 *
	 */
	public enum  ResponseType{
		TIMEOUT,
		FAST,
		ALL
	}
	
	/**
	 * 处理类型
	 * @author hushow
	 *
	 */
	public enum  ProcessType{
		BEST_AVG,
		BAD_AVG
	}
	
	/**
	 * 响应类型
	 */
	private ResponseType responseType;
	
	/**
	 * 处理类型
	 */
	private ProcessType processType;
	
	/**
	 * 开始时间
	 */
	private Long startTime;
	
	
	/**
	 * 超时时间
	 */
	private long timeoutSecond = 1*30;
	
	
	/**
	 * 是否强制退出
	 */
	private boolean forceOut = false;
	
	
	
	public SignalParam(ProcessType processType, ResponseType responseType) {
		startTime = System.currentTimeMillis();
		this.responseType = responseType;
		this.processType = processType;
	}
	
	
	public boolean isTimeout() {
		
		if(System.currentTimeMillis()-startTime > timeoutSecond*1000) return true;
		
		return false;
	}




	public ResponseType getResponseType() {
		return responseType;
	}


	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}


	public ProcessType getProcessType() {
		return processType;
	}


	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}


	public Long getStartTime() {
		return startTime;
	}


	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}


	public long getTimeoutSecond() {
		return timeoutSecond;
	}


	public void setTimeoutSecond(long timeoutSecond) {
		this.timeoutSecond = timeoutSecond;
	}


	public boolean isForceOut() {
		return forceOut;
	}


	public void setForceOut(boolean forceOut) {
		this.forceOut = forceOut;
	}
	
}
