package com.talkweb.filter.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.csource.common.MyException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.http.service.impl.SSLClient;

/**
 * HttpGET获取token
 * @author zhanghuihui
 *
 */
public class HttpClientToken {
	private static final String CLIENT_SECRET;
	private static final String CLIENT_ID;
	static{
		ResourceBundle rb = ResourceBundle.getBundle("constant.oauthconfig" );
		CLIENT_ID = rb.getString("clientId");
		CLIENT_SECRET = rb.getString("clientSecret");
	}
 
	/** 发送http请求 @author zhanghuihui **/
	public static JSONObject callHttpRemoteInterface(String url,String access_token) {
			 CloseableHttpClient httpclient= HttpClientBuilder.create().setDefaultRequestConfig(null).build();
			 String reponseResult="";
			 HttpGet httpget = new HttpGet(url);
			 if(access_token!=null){
				 httpget.addHeader("Access-Token", access_token);
			 }
			try {
				 CloseableHttpResponse response= httpclient.execute(httpget);
				 HttpEntity responseEntity = response.getEntity();
				 reponseResult=EntityUtils.toString(responseEntity);	
			} catch (IOException e) {
				e.printStackTrace();
			}
			return  JSON.parseObject(reponseResult);
	}
	/** 发送http请求 @author zhanghuihui **/
	public static JSONObject callHttpRemoteInterfacePost(String url,String access_token, JSONObject jsonData) {
		CloseableHttpClient client = null;
		
		try {
			client = new SSLClient();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		String reponseResult="";
		HttpPost post=new HttpPost(url);
		 if(access_token!=null){
			 post.addHeader("Access-Token", access_token);
		 }
		
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
	    
		return  JSON.parseObject(reponseResult);
	}
	/** 发送http请求 @author zhanghuihui **/
	public static JSONObject callHttpRemoteInterfacePost1(String url,String access_token, JSONArray jsonData) {
		CloseableHttpClient client = null;
		
		try {
			client = new SSLClient();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		String reponseResult="";
		HttpPost post=new HttpPost(url);
		 if(access_token!=null){
			 post.addHeader("Access-Token", access_token);
		 }
		
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
	    
		return  JSON.parseObject(reponseResult);
	}
	
	
	/** 
     * 发送GET请求 
     *  
     * @param url 
     *            目的地址 
     * @param parameters 
     *            请求参数，Map类型。 
     * @return 远程响应结果 
     * @throws Exception 
     */  
    public static JSONObject sendGetNoToken(String url, Map<String, String> parameters) throws Exception { 
        String result="";
        BufferedReader in = null;// 读取响应输入流  
        StringBuffer sb = new StringBuffer();// 存储参数  
        String params = "";// 编码之后的参数
        try {
            // 编码请求参数  
            if(parameters.size()==1){
                for(String name:parameters.keySet()){
                    sb.append(name).append("=").append(
                            java.net.URLEncoder.encode(parameters.get(name),  
                            "UTF-8"));
                }
                params=sb.toString();
            }else if(parameters.size() > 1){
                for (String name : parameters.keySet()) {  
                    sb.append(name).append("=").append(  
                            java.net.URLEncoder.encode(parameters.get(name),  
                                    "UTF-8")).append("&");  
                }  
                String temp_params = sb.toString();  
                params = temp_params.substring(0, temp_params.length() - 1);  
            }
            String full_url = url;
            if(params.trim().length() !=0){
            	full_url = full_url + "?" + params;
            }
        
            System.out.println(full_url);
            int times = 3;
            while("".equals(result)&&times>=0){
            	times--;
	            // 创建URL对象  
	            java.net.URL connURL = new java.net.URL(full_url);  
	            // 打开URL连接  
	            java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL 
	                    .openConnection();  
	            // 设置通用属性  
	            httpConn.setRequestProperty("Accept", "*/*");  
	            httpConn.setRequestProperty("Connection", "Keep-Alive");  
	            httpConn.setRequestProperty("User-Agent",  
	                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
	            httpConn.setRequestProperty("Client-Id", CLIENT_ID);
	            httpConn.setRequestProperty("Client-Secret", CLIENT_SECRET);
	            // 建立实际的连接  
	            httpConn.connect();  
	            // 响应头部获取  
	            Map<String, List<String>> headers = httpConn.getHeaderFields();  
	            // 遍历所有的响应头字段  
	            for (String key : headers.keySet()) {  
	                System.out.println(key + "\t：\t" + headers.get(key));  
	            }  
	            // 定义BufferedReader输入流来读取URL的响应,并设置编码方式  
	            in = new BufferedReader(new InputStreamReader(httpConn  
	                    .getInputStream(), "UTF-8"));  
	            String line;  
	            // 读取返回的内容  
	            while ((line = in.readLine()) != null) {  
	                result += line;  
	            } 
            }
        } catch (Exception e) {
        	   e.printStackTrace(); 
        }finally{
            try {  
                if (in != null) {  
                    in.close();  
                }  
            } catch (IOException ex) {  
                ex.printStackTrace();  
            }  
        }
        return JSONObject.parseObject(result) ;
    }  
  
    /** 
     * 发送POST请求 
     *  
     * @param url 
     *            目的地址 
     * @param requestBody 
     *            请求参数，Map类型。 
     * @return 远程响应结果 
     * @throws MyException 
     */  
    public static JSONObject sendPostNoToken(String url, Object requestBody) throws Exception {  
        String result = "";// 返回的结果  
        BufferedReader in = null;// 读取响应输入流  
        DataOutputStream out = null ;  
        StringBuffer sb = new StringBuffer();// 处理请求参数  
        try { 
        	  int times = 3;
        	  while("".equals(result)&&times>=0){
		            times--;
		            // 创建URL对象  
		            java.net.URL connURL = new java.net.URL(url);  
		            // 打开URL连接  
		            java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL  
		                    .openConnection(); 
		            httpConn.setRequestMethod("POST");
		            // 设置通用属性  
		            httpConn.setRequestProperty("Accept", "*/*");
		            httpConn.setRequestProperty("Connection", "Keep-Alive");  
		            httpConn.setRequestProperty("User-Agent",  
		                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");  
		            httpConn.setRequestProperty("Content-Type", "application/json");
		            httpConn.setRequestProperty("Client-Id", CLIENT_ID);
		            httpConn.setRequestProperty("Client-Secret", CLIENT_SECRET);
		            // 设置POST方式  
		            httpConn.setDoInput(true);  
		            httpConn.setDoOutput(true);  
		            // 获取HttpURLConnection对象对应的输出流  
		            out = new DataOutputStream(
		            		httpConn.getOutputStream());
		            out.writeBytes(requestBody.toString());
		            out.flush();
		            out.close();
		            // 定义BufferedReader输入流来读取URL的响应，设置编码方式  
		            in = new BufferedReader(new InputStreamReader(httpConn  
		                    .getInputStream(), "UTF-8"));  
		            String line;  
		            // 读取返回的内容  
		            while ((line = in.readLine()) != null) {  
		                result += line;  
		            } 
        	  }
        } catch (Exception e) {  
            e.printStackTrace(); 
        } finally {  
            try {  
                if (out != null) {  
                    out.close();  
                }  
                if (in != null) {  
                    in.close();  
                }  
            } catch (IOException ex) {  
                ex.printStackTrace();  
            }  
        }  
        
        return JSONObject.parseObject(result);  
    }  
  
}
