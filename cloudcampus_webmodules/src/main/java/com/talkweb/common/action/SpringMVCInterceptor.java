package com.talkweb.common.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.auth.service.AuthService;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.filter.util.HttpClientToken;
import com.talkweb.oauth.service.OAUTHUserLoginService;


public class SpringMVCInterceptor implements HandlerInterceptor {
	@Autowired
	private OAUTHUserLoginService loginService;
	@Autowired
    private AuthService authServiceImpl;
	
	static ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	static ResourceBundle rb = ResourceBundle.getBundle("constant.oauthconfig" );
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	private static final Logger log = LoggerFactory.getLogger(SpringMVCInterceptor.class);

	private static final Integer CD_UNLOGIN = 401;
	private static final Integer CD_SYSERR = 601;

	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
		// TODO Auto-generated method stub

	}
	
	public void setResponse(HttpServletResponse response,int code,String msg) throws ServletException, IOException {
		JSONObject result  = new JSONObject();
 		response.setStatus(code);
 		response.setContentType("application/json;charset=UTF-8");
 		result.put("code", code);
 		result.put("msg", msg);
		response.getWriter().write(result.toJSONString());
		response.getWriter().flush();
		response.getWriter().close();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object arg2) throws Exception {
		// TODO Auto-generated method stub
			try{  
				log.debug("[jycloud] oauth login  filter start ....");
				HttpSession session = request.getSession();	
				String termInfoId = rbConstant.getString("currentTermInfo");      
				
				log.debug(String.format("[jycloud]  @@termInfoId:%s  token:%s",termInfoId,session.getAttribute("accessToken")));
				String accountType = "";
			 //@判断token是否失效
			 if(session.getAttribute("accessToken")==null){
				 	session.invalidate();
					setResponse(response,CD_UNLOGIN,"用户未登陆");
					return false;
			 }
			 
			 if(session.getAttribute("accountExtId")==null){
				 	session.invalidate();
					setResponse(response,CD_SYSERR,"用户ExtId为空");
					return false;
			 }
		
		     if(session.getAttribute("accountExtId")==null){
				 	session.invalidate();
					setResponse(response,CD_SYSERR,"用户ID不存在");
					return false;
			 }
			 String accessToken = (String) session.getAttribute("accessToken");
			 String  accountExtId = (String) session.getAttribute("accountExtId");
			//缓存key
			 String cacheResultKey = String.format("common.%s.00.userRoleInfo",accessToken);
			//获取当前登录的用户身份信息
			String getSwitchUserUrl = rb.getString("getSwitchUserUrl");
			getSwitchUserUrl += "/"+accountExtId;
			log.debug("[jycloud] http to getSwitchUserUrl:"+getSwitchUserUrl+" accessToken:"+accessToken);
			JSONObject userInfo = HttpClientToken.callHttpRemoteInterface(getSwitchUserUrl,accessToken);
			log.debug("[jycloud] http to userInfo:"+userInfo );
			if(userInfo==null||userInfo.get("serverResult")==null||!"200".equals( userInfo.getJSONObject("serverResult").getString("resultCode"))){
				session.invalidate();
				 //清理缓存
	        	redisOperationDAO.del(cacheResultKey);
				setResponse(response,CD_UNLOGIN,"认证中心用户身份信息无法取得！");
				return false;
			}
	     
	         JSONObject userResponseEntity = (JSONObject) userInfo.get("responseEntity");
	          //如果是教育局身份登录
			 accountType = userResponseEntity.getString("accountType");
			 if("1".equals(accountType)){
				 	session.invalidate();
					setResponse(response,CD_SYSERR,"不支持教育局身份！");
					return false;
			 }
			 
			 String stuUserExtId = userResponseEntity.getString("studentAccountId");
	         String userExtId = userResponseEntity.getString("accountId");
	         log.debug("[jycloud] userExtId of userInfo:"+userExtId+" stuUserExtId:"+stuUserExtId );  
	         //判断用户角色是否跟session里的一致
	         if(session.getAttribute("userExtId")==null
	        		 ||!session.getAttribute("userExtId").equals(userExtId)
	        		 ||(session.getAttribute("userExtId").equals(userExtId)
	        				 &&!session.getAttribute("stuUserExtId").equals(stuUserExtId)
	        				 &&"4".equals(accountType))){
	        	 //清理缓存
	        	redisOperationDAO.del(cacheResultKey);
		        List<JSONObject> userList = new ArrayList<JSONObject>();
	        	int nowRole= 1;
	        	if("1".equals(accountType)||"2".equals(accountType)){
	        		nowRole=1;//教育局或老师视为老师
	        	}else if("3".equals(accountType)){ //学生
	        		nowRole=2;
	        	}else if("4".equals(accountType)){ //家长
	        		nowRole=0;
	        	}
	        	log.debug(String.format("[jycloud] userList:%s 入参:userExtId:%s  termInfoId:%s nowRole:%s accountType:%s",userList,userExtId,termInfoId,nowRole,accountType));
	            userList = loginService.getUserIdByExtIdRole(userExtId,termInfoId,nowRole);
	        	log.debug(String.format("[jycloud] cs userList: %s" ,userList));
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
		    			setResponse(response,CD_SYSERR,"账号不存在！");
		            	return false;
		            }
		    		long userId = userObj.getLongValue("userId");
		    		User user = loginService.getUserById(-1000,userId);
		    		if (user==null){
		    			session.invalidate();
		    			setResponse(response,CD_SYSERR,"账号不存在！");
		            	return false;
		            }
		    		School sch = loginService.getSchoolByUserId(-1000,userId);
		    		if (sch==null||sch.getId()==0l ){
		    			session.invalidate();
						setResponse(response,CD_SYSERR,"学校不存在！");
		            	return false;
		            }
		    		long schoolId = sch.getId();
		    		//如果身份不是000 就 获取学校代码
		    		log.debug("[jycloud] set session start==>"+user.getAccountPart().getName());
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
		        	log.debug("[jycloud] set session end==>"+userId);
		        	//刷新权限树
			    	if(nowRole==1){
			    		//查看是否为超级管理员
			    		String getRoleByUserId = rb.getString("getRoleByUserId");
						getRoleByUserId+="/"+accountExtId;
						JSONObject roleByUserId = null;
						try {
							roleByUserId = HttpClientToken.sendGetNoToken(getRoleByUserId, new HashMap());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						boolean isSuperManager  = false;
						if(roleByUserId!=null){
							JSONObject serverResult = (JSONObject) roleByUserId.get("serverResult");
							Log.debug(String.format("[jycloud]serverResult:%s", serverResult.toJSONString()));
							String resultCode = serverResult.getString("resultCode");
							if("200".equals(resultCode)){
								JSONObject pageInfo = (JSONObject) roleByUserId.get("pageInfo");
								if(pageInfo.containsKey("list")){
									List<JSONObject> list = (List<JSONObject>) pageInfo.get("list");
									for(JSONObject responseEntity:list){
										if("SCHOOL_MANAGER".equals(responseEntity.getString("roleCode"))&&responseEntity.getJSONArray("orgIdList").contains(sch.getExtId())){
											isSuperManager=true;
											break;
										}
									}
								}
							}
						}
						String isSuperManangerKey = String.format("common.%s.%s.00.isSuperManager",schoolId,userId);
						redisOperationDAO.set(isSuperManangerKey,isSuperManager);
			    		session.removeAttribute("curRole");
						authServiceImpl.getAllRightByParam(session,termInfoId);	 
			    	}
	         }else{
	        	 	session.invalidate();
	    			setResponse(response,CD_SYSERR,"账号角色信息不存在！");
	    			return false;
	         }
	    	}
	    	log.debug("[jycloud]  oauth login  filter end ....");
	    	return true;
	    } catch (Exception e) { 
	    	log.error("loginerror",e);
	    	return false;
	  
	    }	
	}

}
