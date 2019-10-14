package com.talkweb.utils;

import com.alibaba.fastjson.JSONObject;

public class KafkaWXmsgThread extends Thread{

	private String kafkaUrl;
	private String msgKey;
	private JSONObject msgPayLoad;
	private String producerId;
	private String clientId;
	private String clientSecret;

	public KafkaWXmsgThread(String kafkaUrl,String msgKey, JSONObject msgPayLoad, String producerId,String clientId,String clientSecret) {
		this.kafkaUrl = kafkaUrl;
		this.msgKey = msgKey;
		this.msgPayLoad = msgPayLoad;
		this.producerId = producerId;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}
	
	
	@Override
	public void run() {
		try {
			KafkaUtils.sendAppMsg(kafkaUrl, msgKey, msgPayLoad, producerId , clientId, clientSecret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	
}
