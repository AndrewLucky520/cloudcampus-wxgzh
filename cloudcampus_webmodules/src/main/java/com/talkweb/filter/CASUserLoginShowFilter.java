package com.talkweb.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.http.service.impl.SSLClient;


public class CASUserLoginShowFilter implements Filter {
	
	private AllCommonDataService commonDataService;
	
	private static final Logger logger = LoggerFactory.getLogger(CASUserLoginFilter.class);
	
	ResourceBundle rb = ResourceBundle.getBundle("constant.casconfig" );
	
	protected final Log log = LogFactory.getLog(getClass());

	public void init(FilterConfig filterConfig) throws ServletException {
		ServletContext sc = filterConfig.getServletContext(); 
        XmlWebApplicationContext cxt = (XmlWebApplicationContext)WebApplicationContextUtils.getWebApplicationContext(sc);
        
        if (cxt != null && cxt.getBean("allCommonDataService") != null 
        		&& commonDataService == null)
        {
        	commonDataService = (AllCommonDataService) cxt.getBean("allCommonDataService");
        }  	
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		try{  
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			HttpSession session = request.getSession(false);
		
	        //加上自己应用的逻辑，例如构造用户的信息和权限等 
            AttributePrincipal principal = (AttributePrincipal)request.getUserPrincipal();
            Map<String,Object> attributes = principal.getAttributes();
            String userName = attributes .get("name").toString();
            String accountId = attributes .get("userId").toString();
            logger.info("[jycloud] accountId is "+accountId);
            if (StringUtils.isEmpty(accountId)){
            	logger.info("[jycloud] accountId is Empty");
            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
            	return;
            }
            // 通过阳光平台的userId获取对应角色的extId 
            JSONObject param_first = new JSONObject();
            param_first.put("appId", rb.getString("appId"));
            String tokenUrl = rb.getString("getTokenUrl");
            String key = rb.getString("secret");
            param_first.put("secret", encryption(key.getBytes(),key));                    
            JSONObject access_token =  sendRequestForToken(tokenUrl, param_first); 
            logger.info("[jycloud] access_token is "+access_token," 入参tokenUrl:"+tokenUrl+" param_first:"+param_first.toJSONString());
            if (null == access_token){
            	logger.info("[jycloud] access_token is null");
            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
            	return;
            }
            String token = access_token.getString("accessToken");
            JSONObject param_second = new JSONObject();
            param_second.put("userId", accountId);
            String roleUrl = rb.getString("getRoleUrl");
            JSONObject role =  sendRequest(roleUrl + token, param_second);
            logger.info("[jycloud] role is "+role+"入参:roleUrl + token"+roleUrl + token+"  param_second:"+param_second.toJSONString());
            if (null == role){
            	logger.info("[jycloud] role is null");
            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
            	return;
            }
            String stuAccountId = role.getString("studentAccountId");
            String extId = role.getString("accountId");
            if (StringUtils.isNotBlank(stuAccountId) )extId = stuAccountId;  
            String getLogoUrl = rb.getString("getLogo");
            JSONObject logoObj =  sendRequest(getLogoUrl + token, param_second);
            String logoUrl = "";
            String logoImg = "";
            logger.info("[jycloud] logoObj is "+logoObj);
            if (null != logoObj ){
            	 logoUrl = logoObj.getString("logoLink");
            	 logoImg = logoObj.getString("logoFileName");
            }
            
            String termInfoId = rb.getString("currentTermInfo");          
            List<JSONObject> userList = new ArrayList<JSONObject>();
            boolean isFlag = false;
               /* JSONObject userObject = new JSONObject();
                if ("e21130a5df1346edaefdad804e76b205".equals(accountId)) {
                  userObject.put("userId", Long.valueOf(100000005461L));
                  userObject.put("role", "2");
                  userList.add(userObject);
                  isFlag=true;
                } else if ("991b6dc861724c339b6a8c446c10d330".equals(accountId)) {
                  userObject.put("userId", Long.valueOf(100000005624L));
                  userObject.put("role", "2");
                  userList.add(userObject);
                  isFlag=true;
                }
                else if("4c3b624f60f0408082815d2af9935a53".equals(accountId)){
                  userObject.put("userId", Long.valueOf(100000005350L));
                  userObject.put("role", "2");
                  userList.add(userObject);
                  isFlag=true;
                }*/
            JSONObject userObject = new JSONObject();
            if ("b105715d763844cdb1c37a482d89cb8f".equals(accountId)){
            	userObject.put("userId", 1010221347l);
            	userObject.put("role", "0");
            	userList.add(userObject);
            	 isFlag=true;
            }else if("e21130a5df1346edaefdad804e76b205".equals(accountId)){
            	userObject.put("userId", 1010221347l);
            	userObject.put("role", "2");
            	userList.add(userObject);
            	 isFlag=true;
            }else{
            	userObject.put("userId", 1010225361l);
            	userObject.put("role", "4");
            	userList.add(userObject);
            	 isFlag=true;
            }
            if( isFlag==false){
            	logger.info("[jycloud] userList:"+userList+"入参:extId:"+extId+" termInfoId:"+termInfoId);
            	userList = commonDataService.getUserIdByExtId(extId,termInfoId);
            }
            //获取所有的学校关联平台表数据
	    	if (CollectionUtils.isNotEmpty(userList)){
	    		JSONObject userObj = userList.get(0);
	    		if (userObj==null){
	    			logger.info("[jycloud] userObj is null");
	            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
	            	return;
	            }
	    		long userId = userObj.getLongValue("userId");
	    		User user = commonDataService.getUserById(-1000,userId);
	    		if (user==null){
	    			logger.info("[jycloud] user is null");
	            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
	            	return;
	            }
	    		School sch = commonDataService.getSchoolByUserId(-1000,userId);
	    		if (sch==null||sch.getId()==0l ){
	    			logger.info("[jycloud] sch is null");
	            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
	            	return;
	            }
	    		long schoolId = sch.getId();
	    		//如果身份不是000 就 获取学校代码
	    		session.setAttribute("xxdm", schoolId+"");
	    		session.setAttribute("user", user);
	    		session.setAttribute("school", sch);
	    		//设置session有效期10小时
	    		session.setMaxInactiveInterval(3600*10);
	     	
	        	session.setAttribute("userId", userId);
	        	session.setAttribute("sunUserId", accountId);
	        	session.setAttribute("userName", userName);
	        	session.setAttribute("roleType", userObj.getString("role"));
	        	session.setAttribute("accountId", user.getAccountPart().getId());
	        	session.setAttribute("account", user.getAccountPart());
	        	session.setAttribute("curXnxq", termInfoId);
	        	session.setAttribute("isTeaching", false);
	        	
	        	//logo
	        	session.setAttribute("logoUrl",logoUrl+"");
	        	session.setAttribute("logoImg",logoImg+"");
	    	}
	    	if (StringUtils.isNotEmpty(request.getQueryString())) {
				response.sendRedirect(constructServiceurl(request, response));		
				return;
			}
	        //本过滤器处理完成，处理下个过滤器  
	        filterChain.doFilter(request, response);  
	    } catch (Exception e) {  
	    	throw new ServletException(e);  
	    }  
	}

	/** 通过appId和secret获取临时的access_token信息 */
	private JSONObject sendRequestForToken(String tokenUrl, JSONObject param) {
		JSONObject target = JSON.parseObject(callHttpRemoteInterface(tokenUrl, param.toString()));
		int status = target.getIntValue("status");
		if (status == 1){
			return target.getJSONObject("result");
		}else{
			return null;
		}		
	}

	private JSONObject sendRequest(String url, JSONObject param) {
		String keyStr = rb.getString("secret");
		String body = encryption(param.toString().getBytes(),keyStr);
		JSONObject response = JSON.parseObject(callHttpRemoteInterface(url, body));
		int status = response.getIntValue("status");
		if (status == 1){
			String result = response.getString("result");
			byte[] decs = decryption(result,keyStr);
			return JSON.parseObject(new String(decs));
		}else{
			return null;
		}		
	}
	
	/** Base64加密算法 */
	private static String encodeBase64(byte[]input) throws Exception{
		Class<?> clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method mainMethod= clazz.getMethod("encode", byte[].class);
		mainMethod.setAccessible(true);
		Object retObj=mainMethod.invoke(null, new Object[]{input});
		return (String)retObj;
	}

	/** Base64解密算法 */
	private static byte[] decodeBase64(String input) throws Exception{
		Class<?> clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method mainMethod= clazz.getMethod("decode", String.class);
		mainMethod.setAccessible(true);
		Object retObj=mainMethod.invoke(null, input);
		return (byte[])retObj;
	}

	/** 3DES加密算法 */
	private static String encryption(byte[] bytes, String keyStr) {
        try {
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(), "DESede");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return encodeBase64(cipher.doFinal(bytes));
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }

	/** 3DES解密算法 */
	private static byte[] decryption(String bytes, String keyStr) {
        try {
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(), "DESede");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(decodeBase64(bytes));
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
	
	@SuppressWarnings("resource")
	private String callHttpRemoteInterface(String url, String content) {
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

	public void destroy() {}
	
	/** 处理前端请求的跳转链接地址，替换为指定的格式 */
	private String constructServiceurl(HttpServletRequest request,
			HttpServletResponse response) {
		StringBuilder buffer = new StringBuilder();			
		String urlPrefix = request.getRequestURL().toString();		
		String urlSuffix = request.getQueryString();
		if (urlPrefix.contains("talkCloud/getApplication")){
			urlPrefix = urlPrefix.replace("getApplication", "homePage.html");		
		}else if(urlPrefix.contains("talkCloud/getBaseDataManage")){
			urlPrefix = urlPrefix.replace("getBaseDataManage", "embedIndex.html");
		}
		urlSuffix = urlSuffix.replace("menuFlag=", "#");
		String[] urlPrefixArray = urlPrefix.split(";jsessionid");
		buffer.append(urlPrefixArray[0]);
		buffer.append(urlSuffix);
		String returnValue = response.encodeURL(buffer.toString());
		if (log.isDebugEnabled())
			log.debug((new StringBuilder()).append("redirectUrl generated: ")
					.append(returnValue).toString());
		return returnValue;	
	}

}