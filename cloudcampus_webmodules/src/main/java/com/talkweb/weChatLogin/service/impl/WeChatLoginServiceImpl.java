package com.talkweb.weChatLogin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.weChatLogin.service.WeChatLoginService;

@Service
public class WeChatLoginServiceImpl implements WeChatLoginService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(WeChatLoginServiceImpl.class);
	@Autowired
	private AllCommonDataService dataService;
	 
	 @Value("#{settings['auth.getUserUrl']}")
	 private String authurl;
	 
	@Override
	public HashMap<String,Object> getUserBySDK(HashMap<String,Object> param) {
		// TODO Auto-generated method stub
		Map<String,Object> syncParam=new HashMap<String, Object>();
		String responseResult=updateHttpRemoteInterface(authurl+"?param="+param.get("openId")+"&paramType=1",syncParam);
		    logger.info("调用远程接口:"+responseResult);
		    JSONObject json=null;
		    try{
		    	json=(JSONObject)JSONObject.parse(responseResult);
		    }catch(Exception e){
		    	logger.info("远程接口返回json格式有问题！");
				// throw new RuntimeException("远程接口返回json格式有问题！");
				}
		    
			if(json==null||(!"1".equals(json.getString("status"))))
			{	
				logger.info("远程接口返回无数据，或者status不为1");
			    // throw new RuntimeException("远程接口返回无数据，或者status不为1");
	        }else{
	        	try{
	        	JSONArray users=(JSONArray) json.get("result");
	        	if(users!=null&&users.size()>0){
	        	Long schoolId=Long.valueOf(param.get("schoolId").toString());
	        	JSONObject user=users.getJSONObject(0);
	        	String account=user.getString("userLoginName");
	        	Account a=dataService.getAccountAllByAccount(schoolId, account);
	        	if(a!=null){
	        	param.put("name", a.getName()!=null?a.getName():"");
	        	param.put("accountId", a.getId());
	        	List<User> userss=a.getUsers();
	        	if(userss!=null){
	        	for(User u:userss){
	        		if(u!=null&&u.getUserPart().getRole().equals(T_Role.Teacher)){
	        			param.put("userId", u.getUserPart().getId());
	        			TeacherPart t=u.getTeacherPart();
	        			if(t!=null){
	        				param.put("schoolId",t.getSchoolId());
	        				break;
	        			}
	        			//param.put("role", teacher)  //预留
	        		}
	        	}
	        	}
	        	}
	        	}
	        	}catch(Exception e){
	        		logger.info("解析并从深圳接口获取数据有问题");
	        	}
	        }
		    
		return param;
	}
	
	public String updateHttpRemoteInterface(String url,Map<String, Object> param)  {
		CloseableHttpClient client=HttpClients.createDefault();
		String reponseResult="";
		
		HttpPost post=new HttpPost(url);
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>(); 
		for(String key:param.keySet())
		{
			pairs.add(new BasicNameValuePair(key,StringUtil.transformString(param.get(key))));
		}
      
		try {
			 UrlEncodedFormEntity urlEntity= new UrlEncodedFormEntity(pairs, "UTF-8"); 
			 post.setEntity(urlEntity);
			 CloseableHttpResponse response = client.execute(post);
			 HttpEntity responseEntity = response.getEntity();
			 reponseResult=EntityUtils.toString(responseEntity);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
		return reponseResult;
}

}
