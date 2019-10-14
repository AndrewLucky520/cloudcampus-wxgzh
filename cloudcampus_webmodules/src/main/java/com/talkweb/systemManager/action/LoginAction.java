package com.talkweb.systemManager.action;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.auth.service.AuthService;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.SchoolPlateService;
import com.talkweb.csbasedata.service.CommonManageService;
import com.talkweb.systemManager.service.LoginService;


/**
 * 云校园登录Action
 * @author zhanghuihui
 * @date 2017.09.05
 */
@Controller
@RequestMapping(value="/login/")
public class LoginAction {
	private static final Logger logger = LoggerFactory.getLogger(LoginAction.class);
	
	@Autowired
	private SchoolPlateService schoolPlateService;
	@Autowired
	private AuthService authServiceImpl;
	@Autowired
	private AllCommonDataService allCommonDataService;
	@Autowired
	private CommonManageService commonManageService;
	@Autowired
	private LoginService loginService;
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	@Value("#{settings['currentTermInfo']}")
	private String termInfoId;
	@Value("#{settings['url.loginout']}")
	private String loginouturl;
	@Value("#{settings['url.newNEMT.loginout']}")
	private String loginouturl2;
	 /**
	  * 切换用户
	  * @param requestParams
	  * @param req
	  * @param res
	  * @return
	  * @author zhanghuihui
	  */
	@SuppressWarnings("resource")
	@RequestMapping(value = "/switchUser", method = RequestMethod.POST)
    @ResponseBody
	public JSONObject switchUser(@RequestBody JSONObject requestParams,HttpServletRequest req,HttpServletResponse res){
		JSONObject json  = new JSONObject();
		int code = 0;
		String msg ="success";
		//String accountId = requestParams.getString("accountId");
		String userId = requestParams.getString("userId");
		//调用登录重写session
		 //重写SESSION
		JSONObject loginReturn = new JSONObject();
		try {
			loginReturn = loginService.setSessionAndGetLoginStatus(req.getSession(), Long.parseLong(userId));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 if(loginReturn!=null ){
			 String code1 = loginReturn.getString("code");
			 if(!"1".equals(code1)){
				 json.put("code", "-1");
				 json.put("msg", "系统错误"); //系统错误
				 return json;
			 }else{
				 json.put("code", code);
				 json.put("msg", msg);
				 return json;
			 }
		 }else{
			 json.put("code", "-1");
			 json.put("msg", "系统错误"); //系统错误
			 return json;
		 }
	}
	/**
	 * 获取登录状态
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 * @author zhanghuihui
	 */
	@SuppressWarnings("resource")
	@RequestMapping(value = "/getSession", method = RequestMethod.POST)
    @ResponseBody
	public JSONObject getSession(@RequestBody JSONObject requestParams,HttpServletRequest req,HttpServletResponse res){
		JSONObject json = new JSONObject();
		//User user = allCommonDataService.getUserById(50513 , 1010221347l , "20171");
		User user = (User) req.getSession().getAttribute("user");
		long userId = user.getUserPart().getId();
		String sessionId = req.getSession().getId();
		
		json.put("url", loginouturl);
		json.put("url2", loginouturl2);
		if(user==null){
			json.put("code", 0);
			json.put("msg", "session失效");
			return json;
		}
		if(user.getUserPart()==null || user.getUserPart().getId()==0 
			|| user.getUserPart().getRole()==null	){
			json.put("code", -1);
			json.put("msg", "系统错误");
			return json;
		}
		req.getSession().setAttribute("common.00.isJYcloud","1");
		logger.info("getSession forbidden repeat request start..."+(sessionId+"_"+userId));
		if(req.getSession().getAttribute(sessionId+"_"+userId)==null){
			logger.info("getSession into  getAllRightByParam request...");
			authServiceImpl.getAllRightByParam(req.getSession(),termInfoId);
		}
		logger.info("getSession forbidden repeat request end...");
		String role = user.getUserPart().getRole().name();
		String avatar = user.getUserPart().getAvatar();
		School s = allCommonDataService.getSchoolByUserId(-1000, userId);
		if(s==null || s.getId()==0){
			json.put("code", -1);
			json.put("msg", "系统错误");
			return json;
		}
		Long schoolId = s.getId();
		String schoolName = s.getName();
		if(StringUtils.isBlank(schoolName)){
			schoolName="";
		}else{
			schoolName="（"+schoolName;
			schoolName+="）";
		}
		if(user.getAccountPart()==null || user.getAccountPart().getId()==0){
			json.put("code", -1);
			json.put("msg", "系统错误");
			return json;
		}
		/*String userName = user.getAccountPart().getName();
		if("Parent".equals(user.getUserPart().getRole().toString())){
			if( StringUtils.isNotBlank(user.getParentPart().getStudentName())){
				userName = user.getParentPart().getStudentName()+"家长";
			}else{
				userName="";
			}
		}
		if(StringUtils.isBlank(userName)){
			userName="";
		}*/
		Long accountId = user.getAccountPart().getId();
		List<Long> accounts = new ArrayList<Long>();
		accounts.add(accountId);
		List<Account> as= allCommonDataService.getAccountBatch(schoolId,accounts,termInfoId);
		if(as==null){
			json.put("code", -1);
			json.put("msg", "系统错误");
			return json;
		}
		List<JSONObject> roleList = new ArrayList<JSONObject>();
		List<JSONObject> schoolManagerUsers = new ArrayList<JSONObject>();
		List<JSONObject> teacherUsers = new ArrayList<JSONObject>();
		List<JSONObject> studentUsers = new ArrayList<JSONObject>();
		List<JSONObject> parentUsers = new ArrayList<JSONObject>();
		List<User> list = as.get(0).getUsers();
		String userName = "";
		for(User u :list){
			if(u.getAccountPart()==null || u.getAccountPart().getId()==0 || u.getUserPart()==null || u.getUserPart().getId()==0||u.getUserPart().getRole()==null){
				continue;
			}
			long uId = u.getUserPart().getId();
			
			JSONObject roleObj = new JSONObject();
			roleObj.put("isSelect", 0);
			roleObj.put("accountId", u.getAccountPart().getId());
			roleObj.put("userId", u.getUserPart().getId() );
			String roleName = u.getUserPart().getRole().name();
			if("Parent".equals(roleName)){
				String parentUserName = "";
				if(!StringUtils.isBlank(u.getParentPart().getStudentName())){
					 parentUserName =u.getParentPart().getStudentName() +"家长"+schoolName;
				}
				roleObj.put("userName", parentUserName);
				if(userId==uId){
					userName = parentUserName;
					roleObj.put("isSelect", 1);
				}
			}else{
				if(StringUtils.isBlank(u.getAccountPart().getName())){
					roleObj.put("userName", "");
					if(userId==uId){
						userName = "";
						roleObj.put("isSelect", 1);
					}
				}else{
					String aName = u.getAccountPart().getName();
					if("Teacher".equals(roleName)){
						aName+="老师"+schoolName;
					}else if("SchoolManager".equals(roleName)){
						aName+="管理员"+schoolName;
					}
					roleObj.put("userName", aName);
					if(userId==uId){
						userName =  aName;
						roleObj.put("isSelect", 1);
					}
				}
			}
			
			roleObj.put("role", roleName);
			if(StringUtils.isBlank( u.getUserPart().getAvatar())){
				roleObj.put("avatar", "");
			}else{
				roleObj.put("avatar", u.getUserPart().getAvatar());
			}
			switch (roleName) {
			case "Teacher":
				teacherUsers.add(roleObj);
				break;
			case "Parent":
				parentUsers.add(roleObj);
				break;
			case "Student":
				studentUsers.add(roleObj);
				break;
			case "SchoolManager":
				schoolManagerUsers.add(roleObj);
				break;
			default:
				break;
			}
		}
		JSONObject activeObj = new JSONObject();
		activeObj.put("accountId", accountId);
		try {
			commonManageService.toActive(activeObj);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("login getSession==> toActive error !");
		}
		json.put("role", role);
		json.put("avatar", avatar);
		userName = userName.replaceAll(schoolName, "");
		json.put("userName", userName);
		roleList.addAll(schoolManagerUsers);
		roleList.addAll(teacherUsers);
		roleList.addAll(studentUsers);
		roleList.addAll(parentUsers);
		json.put("roleList", roleList);
		JSONObject returnObj = new JSONObject();
		returnObj.put("data", json);
		returnObj.put("code", 1);
		returnObj.put("msg", "success" );
		req.getSession().setAttribute(sessionId+"_"+userId, true);
		return returnObj;
	}
	/**
	 * 登出
	 * @param req
	 * @param reqParams
	 * @return
	 * @author zhanghuihui
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject logout(HttpServletRequest req, @RequestBody JSONObject reqParams,HttpServletResponse res) {
		JSONObject rs = new JSONObject();
		int code = 1;
		String msg = "操作成功！";
		HttpSession session = req.getSession();
		try{
			session.invalidate();
		}catch (Exception e){
			e.printStackTrace();
			code = -1;
			msg = "操作失败！";
		}

		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
}
