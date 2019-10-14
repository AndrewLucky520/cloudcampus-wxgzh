package com.talkweb.commondata.service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.api.templet.utils.MessageTempletEnum;
/**
 * @ClassName MotanService.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2016年6月28日 下午4:05:37
 */
/*public class MotanService {
	
	private static ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"classpath:spring/app-message-push-client.xml"});
	private static Logger logger = LoggerFactory.getLogger(MotanService.class);
	
	public static String noticeMessage(JSONObject jsonObject) throws Exception {
		if(null==ctx){
			logger.info("\n ===================获取 RPC 上下文失败 =================");
			return "RPC context retrieve failed";
		}
		String result = "";
		try {
			MessagePlatform service = (MessagePlatform) ctx.getBean("messagePlatform");
			result = service.noticeMessage(jsonObject);
		} catch (Exception e) {
			logger.info("\n ===================调用 RPC noticeMessage() 接口失败 =================");
			result="{\"message\":\"called failed\",\"retcode\":-1}";
		}
		return result;
	}
	
	public static String messageTemplet(MessageTempletEnum messageTempletEnum,String messageKey,Object... objects) throws Exception {		
		if(null==ctx){
			logger.info("\n ===================获取 RPC 上下文失败 =================");
			return "RPC context retrieve failed";
		}
		String result = "";
		try {
			MessageTemplet service = (MessageTemplet) ctx.getBean("messageTemplet");
			result = service.message(messageTempletEnum,messageKey,objects);
		} catch (Exception e) {
			logger.info("\n ===================调用 RPC noticeMessage() 接口失败 ================="); 
			result="{\"message\":\"called failed\",\"retcode\":-1}";
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		//MotanService.service("messageTempletReferer", "message", "salary.message.info", 
    	//		"1","5300","5000");
		    @SuppressWarnings("resource")
			ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"classpath:spring/spring-motan.xml"});
	        MessageTemplet service = (MessageTemplet) ctx.getBean("messageTempletReferer");
	        try {
	        	String message = service.message(
	        			MessageTempletEnum.MESSAGE, 
	        			"salary.message.info", 
	        			"1","5300","5000");
	        	System.out.println(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
	        System.out.println("motan demo is finish.");
	        System.exit(0);
	}
}*/

public class MotanService
{
  public static String noticeMessage(JSONObject jsonObject)
    throws Exception
  {
    return "{\"message\":\"铜仁环境屏蔽此方法\",\"retcode\":1}";
  }

  public static String messageTemplet(MessageTempletEnum messageTempletEnum, String messageKey, Object[] objects)
    throws Exception
  {
    return "{\"message\":\"铜仁环境屏蔽此方法\",\"retcode\":1}";
  }

  public static void main(String[] args)
    throws Exception
  {
  }
}
