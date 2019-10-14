package com.talkweb.cas.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.http.service.impl.SSLClient;

public class CASHttpClient {
	
	@SuppressWarnings("resource")
	public static final String callHttpRemoteInterface(String url, String content) {
		CloseableHttpClient client = null;
		try {
			client = new SSLClient();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		String reponseResult="";
		HttpPost post=new HttpPost(url);
				
		StringEntity entity = new StringEntity(content,"utf-8");
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
	
	@SuppressWarnings("resource")
	public static final String callHttpRemoteInterface(String url, JSONObject content) {
		CloseableHttpClient client = null;
		try {
			client = new SSLClient();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		String reponseResult="";
		HttpPost post=new HttpPost(url);
			
		try {
			 List<NameValuePair> params = new ArrayList<NameValuePair>();
		     params.add(new BasicNameValuePair("clientId", content.getString("clientId")));
		     params.add(new BasicNameValuePair("token", content.getString("token")));
		     params.add(new BasicNameValuePair("clientSecret", content.getString("clientSecret")));
		     UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params,"utf-8");
			 entity.setContentEncoding("UTF-8");    
			 entity.setContentType("application/x-www-form-urlencoded");    
			 post.setEntity(entity); 
		
			 CloseableHttpResponse response = client.execute(post);
			 HttpEntity responseEntity = response.getEntity();
			 reponseResult=EntityUtils.toString(responseEntity);			
		} catch (Exception e) {
			e.printStackTrace();
		}	    
		return reponseResult;
	}

}