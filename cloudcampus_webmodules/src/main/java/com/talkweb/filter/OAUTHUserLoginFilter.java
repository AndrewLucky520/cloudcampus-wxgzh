package com.talkweb.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.filter.util.HttpClientToken;
import com.talkweb.oauth.service.OAUTHUserLoginService;


public class OAUTHUserLoginFilter implements Filter {
	
	private OAUTHUserLoginService loginService;
	ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	ResourceBundle rb = ResourceBundle.getBundle("constant.oauthconfig" );
	
	private static final Logger log = LoggerFactory.getLogger(OAUTHUserLoginFilter.class);

	public void init(FilterConfig filterConfig) throws ServletException {
		ServletContext sc = filterConfig.getServletContext(); 
        XmlWebApplicationContext cxt = (XmlWebApplicationContext)WebApplicationContextUtils.getWebApplicationContext(sc);
        
        if (cxt != null && cxt.getBean("casLoginService") != null 
        		&& loginService == null)
        {
        	loginService = (OAUTHUserLoginService) cxt.getBean("oauthLoginService");
        }  	
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		try{  
			log.info("[jycloud] oauth login  filter start ....");
			
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			HttpSession session = request.getSession(false);	
			String accountType = "";
			if(session==null){
				log.info("[jycloud]  session is null ....");
				session = request.getSession(true);
			}
			String serverName = request.getServerName();
			
			/*log.info("[jycloud]  loginServerName:"+rb.getString("loginServerName")+" trueloginServerName:"+serverName);
			if(rb.getString("loginServerName").equals(serverName)){
				session.invalidate();
				log.info("[jycloud]  session is invalidate ....");
			}*/
			String firstUrl = (String) session.getAttribute("firstUrl");
			boolean isLogin = false;
			if(session.getAttribute("isLogin")!=null){
				isLogin = (boolean) session.getAttribute("isLogin");
			}
			log.info("[jycloud]  session is null ..isLogin.."+isLogin);
			/*if( isLogin ){
				    log.info("[jycloud]  session is null ..isLogin ==true");
				    session.removeAttribute("firstUrl");
				    if(StringUtils.isNotBlank(firstUrl)){
				    	response.sendRedirect(firstUrl);	
				    }
					filterChain.doFilter(request, response); 
					return;
			}*/
			String termInfoId = rbConstant.getString("currentTermInfo");      
			String accessToken = (String) session.getAttribute("accessToken");
			 log.info("[jycloud]  @@termInfoId:+"+termInfoId+" token:"+accessToken);
			 
			String url="http://" + request.getServerName();   
			url += ":"+rb.getString("qdPort");
	        url += request.getRequestURI();  
	        String urlSuffix = "";
	        if(StringUtils.isNotBlank(request.getQueryString())){
	        	urlSuffix = "?"+request.getQueryString();
	        }
	        String trueUrl = url+urlSuffix;
	        log.info("[jycloud]  trueUrl:+"+trueUrl);
	        
			 //@判断token是否失效
			 if(accessToken!=null){
				 String checkAccessToken = rb.getString("checkAccessToken");
				 checkAccessToken+= accessToken;
				 JSONObject checkAccessTokenInfo = HttpClientToken.callHttpRemoteInterface(checkAccessToken,null);
				 log.info("[jycloud]  @@checkAccessTokenUrl:+"+checkAccessToken+" checkAccessTokenInfo:"+checkAccessTokenInfo.toJSONString());
				 if(checkAccessTokenInfo!=null){
					 JSONObject checkServerResult = checkAccessTokenInfo.getJSONObject("serverResult");
					 if(checkServerResult!=null && !"200".equals(checkServerResult.getString("resultCode"))){
						 log.info("[jycloud]  @@checkServerResult.getString(resultCode):+"+checkServerResult.getString("resultCode"));
						session.invalidate();
						
						if(trueUrl.indexOf("&code=")>=0 || trueUrl.indexOf("?code=")>=0){ //免登录的情况 ，每一次登录都会将前面的token失效重新得到code
							session = request.getSession(true);
							if(session.getAttribute("isLogin")!=null){
								isLogin = (boolean) session.getAttribute("isLogin");
							}
							log.info("[jycloud]nolanding  session is null ..isLogin.."+isLogin);
							 accessToken = (String) session.getAttribute("accessToken");
							 log.info("[jycloud]nolanding  @@termInfoId:+"+termInfoId+" token:"+accessToken);
							 
						}else{
							String getAuCodeUrl = rb.getString("getAuCodeUrl");
							getAuCodeUrl += URLEncoder.encode(trueUrl,"UTF-8");
							getAuCodeUrl+=rb.getString("buttomIndex");
							log.info("[jycloud]  sendRedirect to getAuCodeUrl:+"+getAuCodeUrl);
							response.sendRedirect(getAuCodeUrl);	
							return ;
						}
					 }
				 }
			 }
			 
			//本过滤器处理完成，处理下个过滤器
			
			if(StringUtils.isBlank(accessToken) && (trueUrl.indexOf("&code=")<0 && trueUrl.indexOf("?code=")<0)){//如果token为空且code为空
			
				//1.重定向到登录页面   
				boolean isSendForGetAuCode = false;
				if( session.getAttribute("isSendForGetAuCode")!=null){
				   isSendForGetAuCode = (boolean) session.getAttribute("isSendForGetAuCode");
				}
				 log.info("[jycloud]  isSendForGetAuCode:+"+isSendForGetAuCode);
				if(!isSendForGetAuCode){
					String getAuCodeUrl = rb.getString("getAuCodeUrl");
					session.setAttribute("isSendForGetAuCode", true);
					getAuCodeUrl += URLEncoder.encode(trueUrl,"UTF-8");
					getAuCodeUrl+=rb.getString("buttomIndex");
					log.info("[jycloud]  sendRedirect to getAuCodeUrl:+"+getAuCodeUrl);
					response.sendRedirect(getAuCodeUrl);	
					return ;
				}
			}
			if(StringUtils.isBlank(accessToken) ){ //如果token为空 
				//2.通过URL的code，得到token
				if(trueUrl.indexOf("&code=")<0 && trueUrl.indexOf("?code=")<0){
					session.removeAttribute("isSendForGetAuCode");
				    log.info("[jycloud]  sendRedirect to trueUrl:+"+trueUrl);
					response.sendRedirect(trueUrl);
					return ;
				}
				if(trueUrl.indexOf("?code=")>=0){
			    	int index = trueUrl.indexOf("?");
			    	trueUrl=  trueUrl.substring(0,index)+"&"+trueUrl.substring(index+1,index+38);
			    }
				String getAccessTokenUrl = rb.getString("getAccessTokenUrl");
				getAccessTokenUrl += trueUrl;
				log.info("[jycloud]  http  to getAccessTokenUrl:+"+getAccessTokenUrl);
				JSONObject accessTokenInfo = HttpClientToken.callHttpRemoteInterface(getAccessTokenUrl,null);
				log.info("[jycloud] http to accessTokenInfo :"+accessTokenInfo);
				if(accessTokenInfo==null){
					log.info("[jycloud] accessTokenInfo is null");
					session.invalidate();
					request.getRequestDispatcher("/cas_error.html").forward(request, response);
					return ;
				}
				JSONObject serverResult = (JSONObject) accessTokenInfo.get("serverResult");
				if(serverResult==null){
					log.info("[jycloud] serverResult is null");
					session.invalidate();
					request.getRequestDispatcher("/cas_error.html").forward(request, response);
					return ;
				}
				String resultCode = serverResult.getString("resultCode");
				log.info("[jycloud] resultCode of  accessTokenInfo:"+resultCode);
				if(!"200".equals(resultCode)){
					session.invalidate();
					request.getRequestDispatcher("/cas_error.html").forward(request, response);
					return ;
				}
				JSONObject responseEntity = (JSONObject) accessTokenInfo.get("responseEntity");
				accessToken = (String) responseEntity.get("access_token");
				log.info("[jycloud] accessToken of  accessTokenInfo:"+accessToken);
				session.setAttribute("accessToken", accessToken);
		   }
			if (StringUtils.isNotBlank(accessToken)){ //token不为空
				//请求用户信息
				log.info("[jycloud] accessToken  :"+accessToken);
				String  accountExtId = (String) session.getAttribute("accountExtId");
				log.info("[jycloud] @@accountExtId  :"+accountExtId);
				//3.通过accessToken信息获取用户信息
				if(StringUtils.isBlank(accountExtId)){
					String getUserInfoURL = rb.getString("getUserInfoUrl");
					getUserInfoURL += "/"+accessToken;
					log.info("[jycloud] http to   getUserInfoURL:"+getUserInfoURL);
					JSONObject accountInfo = HttpClientToken.callHttpRemoteInterface(getUserInfoURL,null);
					log.info("[jycloud] http to   accountInfo:"+accountInfo);
					if(accountInfo==null){
						session.invalidate();
						request.getRequestDispatcher("/cas_error.html").forward(request, response);
						return ;
					}
					JSONObject uServerResult = (JSONObject) accountInfo.get("serverResult");
					if(uServerResult==null){
						session.invalidate();
						request.getRequestDispatcher("/cas_error.html").forward(request, response);
						return ;
					}
					//将得到的登录信息保存至session
					String uResultCode = uServerResult.getString("resultCode");
					log.info("[jycloud] resultCode of  accountInfo:"+uResultCode);
					if(!"200".equals(uResultCode)){
						if("104".equals(uResultCode)){//token过期
							log.info("[jycloud] resultCode of  accountInfo:"+uResultCode+"so session invalidata...");
							session.invalidate();
						}
						session.invalidate();
						request.getRequestDispatcher("/cas_error.html").forward(request, response);
						return ;
					}
					JSONObject uResponseEntity = (JSONObject) accountInfo.get("responseEntity");
					accountExtId = uResponseEntity.getString("userId");
					String name = uResponseEntity.getString("userName");
					session.setAttribute("accountExtId", accountExtId);
					session.setAttribute("userName", name);
					
					log.info("[jycloud] accountExtId:"+accountExtId+" name:"+name);
					//学校logo
					String schoolLogo=rb.getString("getUserLogo")+accountExtId;
					 JSONObject logoInfo = HttpClientToken.callHttpRemoteInterface(schoolLogo,accessToken);
					 log.info("[jycloud] http to logoInfo:"+logoInfo );
					 if(logoInfo==null){
						 session.invalidate();
							request.getRequestDispatcher("/cas_error.html").forward(request, response);
							return ;
					 }
					 JSONObject logoServerResult = (JSONObject) logoInfo.get("serverResult");
					 if(logoServerResult==null){
						 session.invalidate();
							request.getRequestDispatcher("/cas_error.html").forward(request, response);
							return ;
					 }
			         String logoResultCode = logoServerResult.getString("resultCode");
			         log.info("[jycloud] logoResultCode of logoInfo:"+logoResultCode );
			         if(!"200".equals(logoResultCode)){
			        	 session.invalidate();
						 request.getRequestDispatcher("/cas_error.html").forward(request, response);
						 return ;
			         }	
			         JSONObject logoResponseEntity = (JSONObject) logoInfo.get("responseEntity");
			         JSONObject topNav = (JSONObject) logoResponseEntity.get("topNav");
			         String logoFileName = topNav.getString("logoFileName");
			         
			         if(logoFileName!=null && logoFileName.endsWith("default.png")) { //是否为默认
			        	 schoolLogo = "";
			         }else {
			        	 schoolLogo = logoFileName;
			         }
			         session.setAttribute("logoUrl", schoolLogo);
			         log.info("[jycloud] logoFileName:"+logoFileName+" schoolLogo:"+schoolLogo );
				}
				//4.获取当前登录的用户身份信息
				String getSwitchUserUrl = rb.getString("getSwitchUserUrl");
				getSwitchUserUrl += "/"+accountExtId;
				log.info("[jycloud] http to getSwitchUserUrl:"+getSwitchUserUrl+" accessToken:"+accessToken);
				JSONObject userInfo = HttpClientToken.callHttpRemoteInterface(getSwitchUserUrl,accessToken);
				log.info("[jycloud] http to userInfo:"+userInfo );
				if(userInfo==null){
					session.invalidate();
					request.getRequestDispatcher("/cas_error.html").forward(request, response);
					return ;
				}
				JSONObject userServerResult = (JSONObject) userInfo.get("serverResult");
				if(userServerResult==null){
					session.invalidate();
					request.getRequestDispatcher("/cas_error.html").forward(request, response);
					return ;
				}
		         String userResultCode = userServerResult.getString("resultCode");
		         log.info("[jycloud] userResultCode of userInfo:"+userResultCode );
		         if(!"200".equals(userResultCode)){
		        	    session.invalidate();
		        		request.getRequestDispatcher("/cas_error.html").forward(request, response);
						return ;
		         }
		         JSONObject userResponseEntity = (JSONObject) userInfo.get("responseEntity");
		          //如果是教育局身份登录
				 accountType = userResponseEntity.getString("accountType");
				 if("1".equals(accountType)){
						log.info("[jycloud] redirect to cas_error_noUser");
						session.invalidate();
						request.getRequestDispatcher("/cas_error_noUser.html").forward(request, response);
						return ;
				 }
				 String stuUserExtId = userResponseEntity.getString("studentAccountId");
		         String userExtId = userResponseEntity.getString("accountId");
		         log.info("[jycloud] userExtId of userInfo:"+userExtId+" stuUserExtId:"+stuUserExtId );
		         // if (StringUtils.isNotEmpty(stuUserExtId))userExtId = stuUserExtId; 
				   
		         boolean isFlag = false;
		         List<JSONObject> userList = new ArrayList<JSONObject>();
		         JSONObject userObject = new JSONObject();
		          /* if ("007b818d85904237b33cebd7fba6b473".equals(accountExtId)){//admin 
		            	userObject.put("userId", 1010221347l);
		            	userObject.put("role", "0"); //徐恒燕家长 徐金灵学生
		            	userList.add(userObject);
		            	 isFlag=true;
		            }else if("5073c0aed35311e79100fa163ed3019d".equals(accountExtId)){
		            	userObject.put("userId", 1010221348L);
		            	userObject.put("role", "2"); //李想学生
		            	userList.add(userObject);
		            	 isFlag=true;
		            }else if ("a1d7a13e886f4840921b4168e3ec5cd0".equals(accountExtId)){
		            	userObject.put("userId", 1010225361l);
		            	userObject.put("role", "4"); //管理员
		            	userList.add(userObject);
		            	 isFlag=true;
		            }else if("10663a2091e34c149d203161bf35de27".equals(accountExtId)){
		            	userObject.put("userId", 1010225360L);
		            	userObject.put("role", "1"); //老师
		            	userList.add(userObject);
		            	 isFlag=true;
		            }else{
		            	userObject.put("userId", 1010221338L);
		            	userObject.put("role", "2"); //王蓉学生
		            	userList.add(userObject);
		            	 isFlag=true;
		            } */
		            if( isFlag==false){
		            	int nowRole= 1;
		            	if("1".equals(accountType)||"2".equals(accountType)){
		            		nowRole=1;//教育局或老师视为老师
		            	}else if("3".equals(accountType)){ //学生
		            		nowRole=2;
		            	}else if("4".equals(accountType)){ //家长
		            		nowRole=0;
		            	}
		            	log.info("[jycloud] userList:"+userList+"入参:userExtId:"+userExtId+" termInfoId:"+termInfoId+" nowRole:"+nowRole+" accountType:"+accountType);
		                userList = loginService.getUserIdByExtIdRole(userExtId,termInfoId,nowRole);
		            }
		            log.info("[jycloud] cs userList" +userList );
		            //获取所有的学校关联平台表数据
			    	if (CollectionUtils.isNotEmpty(userList)){
			    		JSONObject userObj = null;
			    		if("4".equals(accountType)){ //家长
			    			for(JSONObject pObj:userList){
			    				String extStudentId = pObj.getString("extStudentId"); //学生的extId
			    				if(StringUtils.isBlank(extStudentId)){
			    					continue;
			    				}
			    				if(extStudentId.equals(stuUserExtId)){
			    					userObj=pObj;
			    					break;
			    				}
			    			}
			    		}else{
			    			userObj = userList.get(0);
			    		}
			    		if (userObj==null){
			    			log.info("[jycloud] userObj is null");
			            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
			            	return;
			            }
			    		long userId = userObj.getLongValue("userId");
			    		User user = loginService.getUserById(-1000,userId);
			    		if (user==null){
			    			log.info("[jycloud] user is null");
			            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
			            	return;
			            }
			    		School sch = loginService.getSchoolByUserId(-1000,userId);
			    		if (sch==null||sch.getId()==0l ){
			    			log.info("[jycloud] sch is null");
			            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
			            	return;
			            }
			    		long schoolId = sch.getId();
			    		//如果身份不是000 就 获取学校代码
			    		log.info("[jycloud] set session start==>"+user.getAccountPart().getName());
			    		session.setAttribute("xxdm", schoolId+"");
			    		session.setAttribute("user", user);
			    		session.setAttribute("school", sch);
			    		//设置session有效期10小时
			    		session.setMaxInactiveInterval(3600*8);	
			    		
			        	session.setAttribute("userId", userId);
			        	session.setAttribute("sunUserId", accountExtId);
			        	session.setAttribute("roleType", userObj.getString("role"));
			        	session.setAttribute("accountId", user.getAccountPart().getId());
			        	session.setAttribute("account", user.getAccountPart());
			        	session.setAttribute("curXnxq", termInfoId);
			        	session.setAttribute("isTeaching", false); 
			        	session.setAttribute("isLogin", true); 
			        	log.info("[jycloud] set session end==>"+userId);
			    	}
			    	if (StringUtils.isNotEmpty(urlSuffix)) {
			    		url=constructServiceurl(request, response);
			    		session.setAttribute("firstUrl", url);
			    		response.sendRedirect(url);	
			    		return;
					}
			    	log.info("[jycloud]  oauth login  filter end ....");
			    	
			}
			filterChain.doFilter(request, response); 
	    } catch (Exception e) { 
	    	log.info("loginerror",e);
	    	throw e;  
	    }  
	}



	
	
	/** 处理前端请求的跳转链接地址，替换为指定的格式 */
	private String constructServiceurl(HttpServletRequest request,
			HttpServletResponse response) {
		StringBuilder buffer = new StringBuilder();			
		String urlPrefix ="http://" + request.getServerName();
		urlPrefix += ":"+rb.getString("qdPort");
		urlPrefix += request.getRequestURI();  
		String urlSuffix = request.getQueryString();
		if (urlPrefix.contains("talkCloud/getApplication")){
			urlPrefix = urlPrefix.replace("getApplication", "homePage.html");		
		}else if(urlPrefix.contains("talkCloud/getBaseDataManage")){
			urlPrefix = urlPrefix.replace("getBaseDataManage", "embedIndex.html");
		}
		urlSuffix = urlSuffix.replace("menuFlag=", "#");
		int index =urlSuffix.length();
		if(urlPrefix.contains("&code=")){
			index = urlSuffix.indexOf("&code=");
		}
		if(urlSuffix.contains("&code=")){
			index = urlSuffix.indexOf("&code=");
		}
		if(index>=0){
			urlSuffix = urlSuffix.substring(0, index);
		}
		String[] urlPrefixArray = urlPrefix.split(";jsessionid");
		buffer.append(urlPrefixArray[0]);
		buffer.append(urlSuffix);
		String returnValue = response.encodeURL(buffer.toString());
		if (log.isDebugEnabled())
			log.debug((new StringBuilder()).append("redirectUrl generated: ")
					.append(returnValue).toString());
		return returnValue;	
	}
	
	

	@Override
	public void destroy() {
		
	}
}