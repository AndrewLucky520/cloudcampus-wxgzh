package com.talkweb.weChatLogin.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.onecard.common.AESUtil;
import com.talkweb.weChatLogin.service.ZhiHuiXiaoRedirectService;
@Service
public class ZhiHuiXiaoRedirectServiceImpl implements ZhiHuiXiaoRedirectService{
	private static final Logger logger = LoggerFactory
			.getLogger(ZhiHuiXiaoRedirectServiceImpl.class);
	
	 @Value("#{settings['auth.zhihuixiaourl']}")
	 private String zhihuixiaourl;
	 
	 private static final String configFileName = "zhihuixiaoSchoolMap.json";
	 
	 private  String parentPath = null;
	@Override
	public HashMap<String,Object> getUserBySDK(HashMap<String, Object> param,HttpServletRequest request) {
		// TODO Auto-generated method stub
		
		HashMap<String,Object> syncParam=new HashMap<String, Object>();
		
		String zhxSchoolId="";
		JSONObject schoolIdMap=new JSONObject();
		try {
			schoolIdMap=this.getSchoolIdMap(request);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			logger.info("读取学校id所在json文件出错");
		}
		if(schoolIdMap.containsKey(param.get("schoolId").toString())){
			
			zhxSchoolId=schoolIdMap.getString(param.get("schoolId").toString());
		}
		
		if(zhxSchoolId.isEmpty()){
			
			syncParam.put("code", "-999");
			
		}else{
			String code="";
			String error="-1";
			try {
				String code1 = AESUtil.Encrypt(zhxSchoolId+"|"+param.get("telephone")+"|"+new Date().getTime(),"yunxiaoyuan@@key");
				
				logger.info("加密串:"+zhxSchoolId+"|"+param.get("telephone")+"|"+new Date().getTime(),"yunxiaoyuan@@key");
				
				code=URLEncoder.encode(code1,"utf-8");
				logger.info("加密code1串:"+code1);
				logger.info("加密code串:"+code);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				logger.info("加密code出错");
				e1.printStackTrace();
			}
			
		//String responseResult=updateHttpRemoteInterface(zhihuixiaourl+"?schoolId="+zhxSchoolId+"&telephone="+param.get("telephone")+"&srid=0011&code="+code,syncParam);
		  //  System.out.println(zhihuixiaourl+"?schoolId="+zhxSchoolId+"&telephone="+param.get("telephone")+"&srid=0011&code="+code);
			String responseResult=updateHttpRemoteInterface(zhihuixiaourl+"?schoolId="+zhxSchoolId+"&telephone="+param.get("telephone")+"&srid=0011&code="+code,syncParam);

			logger.info("调用远程接口:"+responseResult);
		    JSONObject json=null;
		    try{
		    	json=(JSONObject)JSONObject.parse(responseResult);
		    }catch(Exception e){
		    	logger.info("远程接口返回json格式有问题！");
				// throw new RuntimeException("远程接口返回json格式有问题！");
				}
		    
		    if(json!=null&&json.containsKey("resultCode")){
			    	String resultCode=json.getString("resultCode");
			    	if(resultCode.equals("0")){
			    		error="0";
			    	}
			    	String url=json.getString("url");
			    	syncParam.put("url", url);
		    }
		    
		    syncParam.put("code", error);
		}
		return syncParam;
	}
	
	public JSONObject getSchoolIdMap(HttpServletRequest request) throws IOException{
		
		ServletContext servletContext = request.getSession().getServletContext();    
		String rootPath = servletContext.getRealPath("/");
		this.parentPath =  rootPath + "WEB-INF/classes/constant/" ;
		String configContent = this.readFile(this.getConfigPath());
		JSONObject jsonConfig = new JSONObject();
		jsonConfig = jsonConfig.parseObject(configContent);
		return jsonConfig;
		}
		
	private String getConfigPath () {
        String path = this.getClass().getResource("/").getPath() + this.configFileName;
        if (new File(path).exists()) {
          return path;
        }else {          
          return this.parentPath + File.separator + this.configFileName;
        }
	}
	
	private String readFile ( String path ) throws IOException {
		
		StringBuilder builder = new StringBuilder();
		
		try {
			
			InputStreamReader reader = new InputStreamReader( new FileInputStream( path ), "UTF-8" );
			BufferedReader bfReader = new BufferedReader( reader );
			
			String tmpContent = null;
			
			while ( ( tmpContent = bfReader.readLine() ) != null ) {
				builder.append( tmpContent );
			}
			
			bfReader.close();
			
		} catch ( UnsupportedEncodingException e ) {
			// 忽略
		}
		
		return this.filter( builder.toString() );
		
	}
	
	// 过滤输入字符串, 剔除多行注释以及替换掉反斜杠
		private String filter ( String input ) {
			
			return input.replaceAll( "/\\*[\\s\\S]*?\\*/", "" );
			
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
