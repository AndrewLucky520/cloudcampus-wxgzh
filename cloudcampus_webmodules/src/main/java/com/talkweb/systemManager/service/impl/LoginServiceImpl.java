package com.talkweb.systemManager.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.auth.service.AuthService;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.SchoolPlateService;
import com.talkweb.systemManager.service.LoginService;
/**
 * 登录服务端
 * @author zhanghuihui
 *
 */
@Service(value="loginServiceImpl")
public class LoginServiceImpl implements LoginService {
	private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
	
	@Autowired
	private SchoolPlateService schoolPlateService;
	@Autowired
	private AuthService authServiceImpl;
	@Autowired
	private AllCommonDataService allCommonDataService;
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	@Value("#{settings['currentTermInfo']}")
	private String termInfoId;
	
	/**
	 * 获取登录状态
	 * @param session
	 * @param userId
	 * @return
	 * @author zhanghuihui
	 */
	@Override
	public JSONObject setSessionAndGetLoginStatus(HttpSession session, long userId) throws Exception {
    	JSONObject result = new JSONObject();
    	logger.info("登陆sessionID::"+session.getId());
    	logger.info("登陆userID::"+userId);
		int code = 1;
	    String msg = "登陆成功！";
	    //获取所有的学校关联平台表数据
	    JSONObject param = new JSONObject();
	    List<JSONObject> schoolList = schoolPlateService.getSchoolPlateListBy(param);
		Map<String,String> paramMap = new HashMap<String,String>();
	    for(JSONObject schoolObj : schoolList){
			String schoolId = schoolObj.getString("schoolId");
			String schoolPlateKey = "common."+schoolId+".00.schoolPlate";
			paramMap.put(schoolPlateKey, "1");
	    }
	    try {
			redisOperationDAO.multiSet(paramMap);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	if(userId!=0l){
    		User user = allCommonDataService.getUserById(-1000,userId);
    		if(user==null){
    			result.put("code",-1);
    			msg = "未查询到用户！";
		        result.put("msg",msg);
		        result.put("sessionID", session.getId());
		        logger.error(msg);
		        return result;
    		}
    		School sch = allCommonDataService.getSchoolByUserId(-1000,userId);
    		if(sch==null||sch.getId()==0l ){
    			result.put("code",-2);
    			msg = "未查询到用户所在学校！";
		        result.put("msg",msg);
		        result.put("sessionID", session.getId());
		        return result;
    		}
    		long schoolId = sch.getId();
    		
    		//如果身份不是000 就 获取学校代码
    		session.setAttribute("xxdm", schoolId+"");
    		session.setAttribute("user", user);
    		logger.info("【登录】获取当前用户为：{}",user);
    		session.setAttribute("school", sch);
    		//设置session有效期10小时
    		session.setMaxInactiveInterval(3600*10);
//    		SubProcess sub = new SubProcess(schoolId, session,xnxq, userId, user);
    		
    		int roleType = user.getUserPart().getRole().getValue();
        	
        	session.setAttribute("userId", userId);
        	session.setAttribute("accountId", user.getAccountPart().getId());
        	session.setAttribute("account", user.getAccountPart());
        	session.setAttribute("curXnxq", termInfoId);
        	session.setAttribute("isTeaching", false);
        	//工作台首页要用的是否为管理员字段
        	int isManagerInt = user.getUserPart().getRole().getValue();
        	if(isManagerInt==4){
        		session.setAttribute("isManager", true);
        	}else{
        		session.setAttribute("isManager", false);
        	}
        	//将学校当前学年学期放入redis
    		Object key = "common."+schoolId+".00.currentTermInfo";
    		
    		try {
    			logger.info("loginPlus:----key:{},value:{}",key,termInfoId);
				redisOperationDAO.set(key, termInfoId, CacheExpireTime.sessionMaxExpireTime.getTimeValue());
				String rs = (String) redisOperationDAO.get(key);
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("loginPlus捕获异常为：{}",e.getCause());
				logger.info("loginPlus捕获异常为：{}",e.getMessage());
				logger.info("loginPlus捕获异常为：{}",e.getStackTrace());
			}
    		boolean needControlMenu = true;
    		boolean isPureManager = false;
    		if(roleType==T_Role.Student.getValue()||roleType==T_Role.Parent.getValue()){
    			needControlMenu = false;
    		}else{
    			long d1 = new Date().getTime();
    			try{
    				session.setAttribute("common.00.isJYcloud","1");
    				authServiceImpl.getAllRightByParam(session,termInfoId);
    				
    				if(session.getAttribute("isTeaching")==null||!(boolean)session.getAttribute("isTeaching")){
    					isPureManager = true;
    				}
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    			long d2 = new Date().getTime();
    			
    			logger.debug("【登陆】-【处理权限】耗时："+(d2-d1));
    			System.out.println("【登陆】-【处理权限】耗时："+(d2-d1));
    		}
    		
    	}else{
    		code = -1;
    		msg = "登陆失败，未查询到用户！";
    	}
        
        result.put("code",code);
        result.put("msg",msg);
        result.put("sessionID", session.getId());
        return result;
	}

}
