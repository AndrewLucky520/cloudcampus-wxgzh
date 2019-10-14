package com.talkweb.http.service.impl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.http.service.CallRemoteInterface;
@Service
public class CallRemoteInterfaceImpl implements CallRemoteInterface{
	
	@Override
	public String updateHttpRemoteInterface(String url,Map<String, Object> param) {
		CloseableHttpClient client=HttpClients.createDefault();
		String reponseResult="";
		List<NameValuePair> list = new ArrayList <NameValuePair>();
		
		if(param!=null&&param.size()>0)
		{
			Set<String> keys=param.keySet();
			for(String key:keys)
			{
				NameValuePair value=new BasicNameValuePair(key,StringUtil.transformString(param.get(key)));
				list.add(value);
			}
		}
		
		HttpPost post=new HttpPost(url);
		
		try {
			 post.setEntity(new UrlEncodedFormEntity(list));
			 CloseableHttpResponse response = client.execute(post);
			 HttpEntity responseEntity = response.getEntity();
			 reponseResult=EntityUtils.toString(responseEntity);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
		return reponseResult;
	}

	@SuppressWarnings("resource")
	@Override
	public String updateHttpRemoteInterface(String url, JSONObject jsonData) {
		// TODO Auto-generated method stub
		CloseableHttpClient client = null;
		try {
			client = new SSLClient();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String reponseResult="";
		HttpPost post=new HttpPost(url);
		StringEntity entity = new StringEntity(jsonData.toString(),"utf-8");//解决中文乱码问题    
        entity.setContentEncoding("UTF-8");    
        entity.setContentType("application/json");    
        post.setEntity(entity); 
		try {
			 CloseableHttpResponse response = client.execute(post);
			 HttpEntity responseEntity = response.getEntity();
			 reponseResult=EntityUtils.toString(responseEntity);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
		return reponseResult;
	}
	
	@Override
	 public String HttpGet(String url ){
		 HttpGet  httppost = new HttpGet(url);
		 CloseableHttpResponse response = null;
	     CloseableHttpClient httpclient = HttpClients.createDefault();
	     String result = "";
	     try {
	    	    response = httpclient.execute(httppost);
			    int status = response.getStatusLine().getStatusCode();
			    if (status == 200) {
			    	result = EntityUtils.toString(response.getEntity() , "utf-8");
			    }
		 } catch (Exception e) {
		    e.printStackTrace();
		}finally {
			if (response != null) {
                try {
                	response.close();
                } catch (Exception e) {
                }
            }
        	if (httpclient != null) {
                try {
                	httpclient.close();
                } catch (Exception e) {
                }
            }
		}
	   return result;	
	}
	
	
	
}
