package com.talkweb.filter;

import java.io.IOException;
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
import com.talkweb.auth.service.AuthService;
import com.talkweb.filter.util.HttpClientToken;
import com.talkweb.oauth.service.OAUTHUserLoginService;

public class NewOAUTHUserLoginFilter implements Filter {
	private static final Integer CD_UNLOGIN = -401;
	
	private OAUTHUserLoginService loginService;
    private AuthService authServiceImpl;
	ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	ResourceBundle rb = ResourceBundle.getBundle("constant.oauthconfig" );
	private String homeUrl=  rb.getString("oauth.home.url");
	private static final Logger log = LoggerFactory.getLogger(NewOAUTHUserLoginFilter.class);

	public void init(FilterConfig filterConfig) throws ServletException {
		ServletContext sc = filterConfig.getServletContext(); 
        XmlWebApplicationContext cxt = (XmlWebApplicationContext)WebApplicationContextUtils.getWebApplicationContext(sc);
        
        if (cxt != null && cxt.getBean("casLoginService") != null 
        		&& loginService == null)
        {
        	loginService = (OAUTHUserLoginService) cxt.getBean("oauthLoginService");
        	authServiceImpl = (AuthService) cxt.getBean("authServiceImpl");
        	
        }  	
	}
	
	public void setResponse(HttpServletResponse response,int code,String msg) throws ServletException, IOException {
		JSONObject result  = new JSONObject();
 		response.setStatus(200);
 		response.setContentType("application/json;charset=UTF-8");
 		result.put("code", code);
 		result.put("msg", msg);
		response.getWriter().write(result.toJSONString());
		response.getWriter().flush();
		response.getWriter().close();
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
			try{  
				log.info("[jycloud] oauth login  filter start ....");
				HttpServletRequest request = (HttpServletRequest) servletRequest;
				HttpServletResponse response = (HttpServletResponse) servletResponse;
				HttpSession session = request.getSession();	
				String termInfoId = rbConstant.getString("currentTermInfo");      
				String accessToken = (String) session.getAttribute("accessToken");
				log.info("[jycloud]  @@termInfoId:+"+termInfoId+" token:"+accessToken);
				String accountType = "";
				//页面跳转
				if(request.getRequestURI().indexOf("getApplication")>0){
	        		String menuFlag = request.getParameter("menuFlag");
	        		String redirectUrl =  homeUrl+"#"+menuFlag;
	        		response.sendRedirect(redirectUrl);
	        		return;
	        	} 
				
				//本过滤器处理完成，处理下个过滤器
				if(request.getRequestURI().indexOf("redirectAction.do")>0 ){//如果token为空且code为空
					String authCode = request.getParameter("code");
					String redirectUrl = request.getParameter("url");
					String getAccessTokenUrl =  rb.getString("getAccessTokenUrl")+"&code="+authCode;
					log.info("[jycloud]  http  to getAccessTokenUrl:+"+getAccessTokenUrl);
					JSONObject accessTokenInfo = HttpClientToken.callHttpRemoteInterface(getAccessTokenUrl,null);
					log.info("[jycloud] http to accessTokenInfo :"+accessTokenInfo);
					if(accessTokenInfo==null){
						session.invalidate();
						setResponse(response,CD_UNLOGIN,"登录过期");
						return ;
					}
					JSONObject serverResult = (JSONObject) accessTokenInfo.get("serverResult");
					if(serverResult==null){
						setResponse(response,CD_UNLOGIN,"登录过期");
						return ;
					}
					String resultCode = serverResult.getString("resultCode");
					log.info("[jycloud] resultCode of  accessTokenInfo:"+resultCode);
					if(!"200".equals(resultCode)){
						setResponse(response,CD_UNLOGIN,"登录过期");
						return ;

					}
					JSONObject responseEntity = (JSONObject) accessTokenInfo.get("responseEntity");
					accessToken = (String) responseEntity.get("access_token");
					log.info("[jycloud] accessToken of  accessTokenInfo:"+accessToken);
					session.setAttribute("accessToken", accessToken);
					response.sendRedirect(redirectUrl);	
					return;
			 }
			
			 //@判断token是否失效
			 if(StringUtils.isBlank(accessToken)){
					setResponse(response,CD_UNLOGIN,"用户未登陆");
					return ;
			 }
			 	 
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
				if(accountInfo==null||accountInfo.get("serverResult")==null||!"200".equals(accountInfo.getJSONObject("serverResult").getString("resultCode"))){
					session.invalidate();
					setResponse(response,CD_UNLOGIN,"认证中心账号信息不存在！");
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
				 if(logoInfo==null||logoInfo.get("serverResult")==null||!"200".equals(logoInfo.getJSONObject("serverResult").getString("resultCode"))){
						session.invalidate();
						setResponse(response,CD_UNLOGIN,"认证中心用户logo信息无法取得！");
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
			if(userInfo==null||userInfo.get("serverResult")==null||!"200".equals( userInfo.getJSONObject("serverResult").getString("resultCode"))){
				session.invalidate();
				setResponse(response,CD_UNLOGIN,"认证中心用户身份信息无法取得！");
				return ;
			}
	     
	         JSONObject userResponseEntity = (JSONObject) userInfo.get("responseEntity");
	          //如果是教育局身份登录
			 accountType = userResponseEntity.getString("accountType");
			 if("1".equals(accountType)){
				 	session.invalidate();
					setResponse(response,CD_UNLOGIN,"不支持教育局身份！");
					return ;
			 }
			 
			 String stuUserExtId = userResponseEntity.getString("studentAccountId");
	         String userExtId = userResponseEntity.getString("accountId");
	         log.info("[jycloud] userExtId of userInfo:"+userExtId+" stuUserExtId:"+stuUserExtId );  
	         //判断用户角色是否跟session里的一致
	         if(session.getAttribute("userExtId")==null
	        		 ||!session.getAttribute("userExtId").equals(userExtId)
	        		 ||(session.getAttribute("userExtId").equals(userExtId)
	        				 &&!session.getAttribute("stuUserExtId").equals(stuUserExtId)
	        				 &&"4".equals(accountType))){
		         List<JSONObject> userList = new ArrayList<JSONObject>();
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
		    			setResponse(response,CD_UNLOGIN,"账号不存在！");
		            	return;
		            }
		    		long userId = userObj.getLongValue("userId");
		    		User user = loginService.getUserById(-1000,userId);
		    		if (user==null){
		    			session.invalidate();
		    			setResponse(response,CD_UNLOGIN,"账号不存在！");
		            	return;
		            }
		    		School sch = loginService.getSchoolByUserId(-1000,userId);
		    		if (sch==null||sch.getId()==0l ){
		    			session.invalidate();
						setResponse(response,CD_UNLOGIN,"学校不存在！");
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
		        	session.setAttribute("userExtId", userExtId);
		        	session.setAttribute("stuUserExtId", stuUserExtId);
		        	session.setAttribute("sunUserId", accountExtId);
		        	session.setAttribute("roleType", userObj.getString("role"));
		        	session.setAttribute("accountId", user.getAccountPart().getId());
		        	session.setAttribute("account", user.getAccountPart());
		        	session.setAttribute("curXnxq", termInfoId);
		        	session.setAttribute("isTeaching", false); 
		        	session.setAttribute("isLogin", true); 
		        	log.info("[jycloud] set session end==>"+userId);
		        	//刷新权限树
			    	if(nowRole==1){
			    		  session.removeAttribute("curRole");
						  authServiceImpl.getAllRightByParam(session,termInfoId);	 
			    	}
	         }else{
	        	 	session.invalidate();
	    			setResponse(response,CD_UNLOGIN,"账号角色信息不存在！");
	    			return;
	         }
	    	}
	    	log.info("[jycloud]  oauth login  filter end ....");
			filterChain.doFilter(request, response); 
	    } catch (Exception e) { 
	    	log.info("loginerror",e);
	    	throw e;  
	    }  
	}
	

	@Override
	public void destroy() {
		
	}
}