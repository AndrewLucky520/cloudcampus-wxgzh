package com.talkweb.utils;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;


 

public class KafkaUtils {
	private static Logger log = Logger.getLogger(KafkaUtils.class);
	/**
	 * 发送应用消息到消息服务中心
	 * 
	 * @param msgKey
	 *            消息键，要求唯一（最好为主键），便于定位消息
	 * @param msgPayLoad
	 *            消息内容，要求一个完整的JSON的结构体
	 * @param producerId
	 *            生产者ID，唯一标识。按自身业务来命名
	 * @return
	 * @throws Exception 
	 */
	public static void sendAppMsg(String kafkaUrl,String msgKey, JSONObject msgPayLoad, String producerId,String clientId,String clientSecret) throws RuntimeException {
		String current_url = kafkaUrl + "/kafkaApi/produer/sendAppMsg";
		JSONObject param = new JSONObject();
		param.put("msgKey", msgKey);
		param.put("msgPayLoad", msgPayLoad);
		param.put("producerId", producerId);
//		JSONObject jsonObj = HttpClientUtil.doPostJsonToken(current_url, param.toString(),token);
 	    log.info("current_url:"+current_url +" " + param.toString() + " " + clientId + " " +  clientSecret);
		JSONObject jsonObj = HttpClientUtil.doPostNoToken(current_url, param.toString(),clientId,clientSecret);
		log.info("sendAppMsgKafkaRetuan:"+jsonObj);
		if(jsonObj==null){
			throw new RuntimeException("kafka接口异常！");
		}else if(jsonObj.getInteger("stateCode") != 1) {
			throw new RuntimeException(jsonObj.getString("msg"));
		}
	}
	
	
	/**
	 * 发送短信
	 * 
	 * @param msgKey
	 *            消息键，要求唯一（最好为主键），便于定位消息
	 * @param msgPayLoad
	 *            消息内容，要求一个完整的JSON的结构体
	 * @param producerId
	 *            生产者ID，唯一标识。按自身业务来命名
	 * @return
	 * @throws Exception 
	 */
	public static void sendSms(String kafkaUrl,String msgKey, String msgPayLoad, String producerId,String clientId,String clientSecret) throws RuntimeException {
		String current_url = kafkaUrl + "/kafkaApi/produer/sendSms";
		System.out.println(current_url);
		JSONObject param = new JSONObject();
		param.put("msgKey", msgKey);
		param.put("msgPayLoad", msgPayLoad);
		param.put("producerId", producerId);
//		JSONObject jsonObj = HttpClientUtil.doPostJsonToken(current_url, param.toString(),token);
		JSONObject jsonObj = HttpClientUtil.doPostNoToken(current_url, param.toString(),clientId,clientSecret);
		log.info("sendSmsKafkaRetuan:"+jsonObj);
		if(jsonObj==null){
			throw new RuntimeException("kafka接口异常！");
		}else if(jsonObj.getInteger("stateCode") != 1) {
			throw new RuntimeException(jsonObj.getString("msg"));
		}
	}
}
