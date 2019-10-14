package com.talkweb.systemManager.action;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.SchoolPlateService;
import com.talkweb.filter.util.HttpClientToken;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Controller
@RequestMapping(value = "/talkCloud/")
public class CASUserAction extends BaseAction {
	
	@Autowired
	private SchoolPlateService schoolPlateService;
	ResourceBundle rb = ResourceBundle.getBundle("constant.oauthconfig" );
	ResourceBundle constantRb = ResourceBundle.getBundle("constant.constant" );
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	@Autowired
    private AuthService authServiceImpl;
	
	@Autowired
	private AllCommonDataService allCommonDataService;
		
    private static final Logger logger = LoggerFactory.getLogger(CASUserAction.class);
    

    /**
     * 后续配置化
     */
    private static String JWT_SECRET = "10YnMCJdJk75L5qLNoUlsrcG7N4p8F3/lysPIaXvTxc=";
    private static String ERR_PAGE = "/talkCloud/cas_error.html";
    private static void toErrPage(HttpServletResponse response,String msg) throws IOException{
    	String path = ERR_PAGE;
    	try {
    		path = path+"?msg="+URLEncoder.encode(msg,"utf-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
    	response.sendRedirect(path);
    	return;
    }
    @RequestMapping(value = "/redirectAction", method = RequestMethod.GET)
    public void redirectAction(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException { 
    	 String accessToken = null;
    	 HttpSession session = request.getSession();	
			//本过滤器处理完成，处理下个过滤器
		String authCode = request.getParameter("code");
		String redirectUrl = request.getParameter("url");
		String getAccessTokenUrl =  rb.getString("getAccessTokenUrl")+"&code="+authCode;
		logger.info("[jycloud]  http  to getAccessTokenUrl:+"+getAccessTokenUrl);
		JSONObject accessTokenInfo = HttpClientToken.callHttpRemoteInterface(getAccessTokenUrl,null);
		logger.info("[jycloud] http to accessTokenInfo :"+accessTokenInfo);
		if(accessTokenInfo==null){
			session.invalidate();
			toErrPage(response,"accessToken接口调用失败");
			return;
		}
		JSONObject serverResult = (JSONObject) accessTokenInfo.get("serverResult");
		if(serverResult==null){
			session.invalidate();
			toErrPage(response,"accessToken接口调用失败");
			return;
		}
		String resultCode = serverResult.getString("resultCode");
		logger.info("[jycloud] resultCode of  accessTokenInfo:"+resultCode);
		if(!"200".equals(resultCode)){
			session.invalidate();
			toErrPage(response,"accessToken接口调用失败");
			return;

		}
		JSONObject responseEntity = (JSONObject) accessTokenInfo.get("responseEntity");
		accessToken = (String) responseEntity.get("access_token");
		logger.info("[jycloud] accessToken of  accessTokenInfo:"+accessToken);
		session.setAttribute("accessToken", accessToken);

		//通过accessToken信息获取用户信息
		String getUserInfoURL = rb.getString("getUserInfoUrl");
		getUserInfoURL += "/"+accessToken;
		logger.info("[jycloud] http to   getUserInfoURL:"+getUserInfoURL);
		JSONObject accountInfo = HttpClientToken.callHttpRemoteInterface(getUserInfoURL,null);
		logger.info("[jycloud] http to   accountInfo:"+accountInfo);
		if(accountInfo==null||accountInfo.get("serverResult")==null||!"200".equals(accountInfo.getJSONObject("serverResult").getString("resultCode"))){
			session.invalidate();
			toErrPage(response,"用户信息接口调用失败");
			return;
		}

		JSONObject uResponseEntity = (JSONObject) accountInfo.get("responseEntity");
		String accountExtId = uResponseEntity.getString("userId");
		String name = uResponseEntity.getString("userName");
		session.setAttribute("accountExtId", accountExtId);
		session.setAttribute("userName", name);
		
		logger.info("[jycloud] accountExtId:"+accountExtId+" name:"+name);
		//学校logo
		String schoolLogo=rb.getString("getUserLogo")+accountExtId;
		 JSONObject logoInfo = HttpClientToken.callHttpRemoteInterface(schoolLogo,accessToken);
		 logger.info("[jycloud] http to logoInfo:"+logoInfo );
		 if(logoInfo==null||logoInfo.get("serverResult")==null||!"200".equals(logoInfo.getJSONObject("serverResult").getString("resultCode"))){
				session.invalidate();
				toErrPage(response,"学校logo获取失败");
				return;
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
         logger.info(String.format("%s [jycloud] logoFileName: %s schoolLogo:%s redirectUrl:%s",new Date().toString(), logoFileName,schoolLogo ,redirectUrl));
         response.sendRedirect(redirectUrl);	
         return;
	
		
    }

    /**
     * 登录(126的login  注释By:zhh)
     * @param userId 账号
     * @param passWord 密码
     * @param session 会话
     * @return
     */
    @RequestMapping(value = "/getUserRole", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject getUserRole(HttpServletRequest req) { 
    	long start=new Date().getTime();
    	logger.debug("[login times ]"+"start.....");
    	HttpSession session = req.getSession();  	 	
    	String termInfoId = (String) session.getAttribute("curXnxq");
    	String accessToken = (String) session.getAttribute("accessToken");
    	String roleType = (String)session.getAttribute("roleType");
    	String userName = (String)session.getAttribute("userName");
    	String userExtId = (String)session.getAttribute("sunUserId");
    	long userId = (long)session.getAttribute("userId");
    	long accountId = (long)session.getAttribute("accountId");
    	String xnxq = (String)session.getAttribute("curXnxq");
    	String logoUrl = (String) session.getAttribute("logoUrl");
    	String accountExtId = (String) session.getAttribute("accountExtId");
    	String logoImg = rb.getString("getUserPhoto")+accountExtId;
    	logger.info("[jycloud]nowuId:%s logoUrl:%s roleType:%s accessToken:%s",session.getAttribute("userId"),logoUrl,roleType,accessToken);
    	if(accessToken==null){
    		accessToken = userId+"";
    	}
    	//如果是云平台环境则走redis缓存	
    	String cacheResultKey = String.format("common.%s.00.userRoleInfo",accessToken);
    	String from = constantRb.getString("from");
    	if("1".equals(from)){
	    	try {
		    	Object cacheResult = redisOperationDAO.get(cacheResultKey);
		    	if(cacheResult!=null){
		    		JSONObject dataCacheResult = ((JSONObject)cacheResult).getJSONObject("data");
		    		long cacheUserId = dataCacheResult.getLongValue("userId");
		    		if(cacheUserId==userId){
		    			return (JSONObject)cacheResult;
		    		}
		    	}
	    	} catch (Exception e1) {
	 			e1.printStackTrace();
	 		}
    	}
    	// 初始化REDIS中保存的学校信息
    	Long csUserId = (Long)session.getAttribute("userId");
		String sessionId = req.getSession().getId();
 		Map<String,String> paramMap = new HashMap<String,String>();
 		School s = (School) session.getAttribute("school");
 	    long schoolId= s.getId();
 		String schoolName = s.getName();

	    //获取所有的学校关联平台表数据
    	JSONObject result = new JSONObject();
    	String code = "1",msg = "获取用户角色成功!";
    	JSONObject data = new JSONObject();
    	data.put("logoUrl", logoUrl);
		data.put("logoImg", logoImg);
		data.put("userId", userExtId);
		data.put("userName", userName);	
		if (StringUtils.isNotEmpty(roleType)){
			int role = Integer.parseInt(roleType);
			switch (role) {	
			  case 0://家长
					data.put("role", "Parent");
					result.put("data", data);
					logger.info("[jycloud]parent:"+data.toJSONString());
					break;
			  case 1://教师
					data.put("role", "Teacher"); 
					result.put("data", data);
				    logger.info("[jycloud]teacher:"+data.toJSONString());
					break;
			  case 2://学生 
					data.put("role", "Student");
					result.put("data", data);
					logger.info("[jycloud]student:"+data.toJSONString());
					break;			  
			  case 4://管理员  
					data.put("role", "SchoolManager");
					logger.info("[jycloud]systemanager:"+data.toJSONString());
					break;
			  default:
				  	logger.info("[jycloud]no role:");
					code = "-1";
					msg = "未知的用户角色！";
					break;		
			}	
		}else{
			code = "-2";
	        msg = "不包含用户角色信息！";
		}
		List<Long> accounts = new ArrayList<Long>();
		accounts.add(accountId);
		List<Account> as= allCommonDataService.getAccountBatch(schoolId,accounts,termInfoId);
		long end21=new Date().getTime();
		List<JSONObject> roleList = new ArrayList<JSONObject>();
		List<JSONObject> schoolManagerUsers = new ArrayList<JSONObject>();
		List<JSONObject> teacherUsers = new ArrayList<JSONObject>();
		List<JSONObject> studentUsers = new ArrayList<JSONObject>();
		List<JSONObject> parentUsers = new ArrayList<JSONObject>();
		List<User> list = as.get(0).getUsers();
		userName = "";
		for(User u :list){
			if(u.getAccountPart()==null || u.getAccountPart().getId()==0 || u.getUserPart()==null || u.getUserPart().getId()==0||u.getUserPart().getRole()==null){
				continue;
			}
			long uId = u.getUserPart().getId();
			//获取extUserId和extAccountId 
			JSONObject extObj = authServiceImpl.getExtIdByUserId(uId,termInfoId);
			String extUserId = extObj.getString("extUserId");
			String extAccountId = extObj.getString("extAccountId");
			JSONObject roleObj = new JSONObject();
			roleObj.put("isSelect", 0);
			roleObj.put("accountId", u.getAccountPart().getId());
			roleObj.put("userId", u.getUserPart().getId() );
			roleObj.put("extAccountId", extAccountId);
			roleObj.put("extUserId", extUserId);
			roleObj.put("extStudentAccountId", "");
			roleObj.put("extStudentUserId", "");
			String roleName = u.getUserPart().getRole().name();
			logger.debug("getUserRole u.getParentPart().getStudentName() :" + u.toString());
			if("Parent".equals(roleName)){
				String parentUserName = "";
				if(!StringUtils.isBlank(u.getParentPart().getStudentName())){
					 parentUserName =u.getParentPart().getStudentName() +"的家长"+"("+schoolName+")";
				}
				roleObj.put("userName", parentUserName);
				if(userId==uId){
					userName = parentUserName;
					roleObj.put("isSelect", 1);
				}
				//获取家长对应的孩子的extStudentUserId 和extStudentAccountId
				JSONObject extStudentObj = authServiceImpl.getExtStudentByParentId(uId,termInfoId);
				if(extStudentObj==null){
					continue;
				}
				String extStudentAccountId= extStudentObj.getString("extStudentAccountId");
				String extStudentUserId= extStudentObj.getString("extStudentUserId");
				roleObj.put("extStudentUserId", extStudentUserId);
				roleObj.put("extStudentAccountId", extStudentAccountId);
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
						aName+="老师"+"("+schoolName+")";
					}else if("SchoolManager".equals(roleName)){
						aName+="管理员"+"("+schoolName+")";
					}else{
						aName+="同学"+"("+schoolName+")";
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
		roleList.addAll(schoolManagerUsers);
		roleList.addAll(teacherUsers);
		roleList.addAll(studentUsers);
		roleList.addAll(parentUsers);
        result.put("msg", msg);
        result.put("code", code); 
        result.put("xnxq", xnxq); 
        result.put("roleList", roleList);
        data = (JSONObject) result.get("data");
        if(data!=null){
        	data.put("userName", userName);
        	data.put("userId", userId);
        	data.put("schoolName", schoolName);
        	data.put("schoolId", schoolId);
        	//签名数据
        	Key key =new SecretKeySpec(JWT_SECRET.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        	
        	String jwtToken = Jwts.builder().setSubject("NewNEMT")
        									.claim("accountId", accountId)
        									.claim("userId", userId)
        									.claim("schoolId", schoolId).signWith(key).compact();
        	data.put("jwtToken", jwtToken);
        	
        }
        req.getSession().setAttribute(sessionId+"_"+csUserId, true);
        //云平台的环境下做redis缓存
        if("1".equals(from)){
	        try {
				redisOperationDAO.set(cacheResultKey,result,CacheExpireTime.temporaryDataMidExpireTime.getTimeValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    	logger.debug("[login times ]"+"end.....");
        return result;
    }
    /**
	 * ***角色切换***
	 * @param req
	 * @param reqParams
	 * @return
	 */
	@RequestMapping(value = "/switchUser", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject switchUser(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		logger.info("[jycloud]  switchUser...start");
		JSONObject response = new JSONObject();
		String extAccountId =param.getString("extAccountId");
		String role = param.getString("role");
		String extUserId = param.getString("extUserId");
		String extStudentUserId = param.getString("extStudentUserId");
		String extStudentAccountId  = param.getString("extStudentAccountId");
		int roleNum=2;
		if("Parent".equals(role)){
			roleNum=4;
		}else if ("Student".equals(role)){
			roleNum=3;
		}
		logger.info("[jycloud]  switchUser...param:"+param.toJSONString());
		//调用教育云的接口实现切换
		JSONObject remoteJSON = new JSONObject();
		remoteJSON.put("userId", extUserId);
		remoteJSON.put("accountId", extAccountId);
		remoteJSON.put("accountType", roleNum);
		remoteJSON.put("studentAccountId", extStudentAccountId);
		remoteJSON.put("studentUserId", extStudentUserId);
		
		String updateUserSwitchUrl = rb.getString("updateUserSwitchUrl");

		String accessToken = (String) req.getSession().getAttribute("accessToken");
		JSONObject serverResultObj = HttpClientToken.callHttpRemoteInterfacePost(updateUserSwitchUrl,accessToken,remoteJSON);
		logger.info("[jycloud] switchUser...serverResultObj:"+serverResultObj+"  accessToken:"+accessToken+"  updateUserSwitchUrl:"+updateUserSwitchUrl+"  remoteJSON:"+remoteJSON.toJSONString());
		JSONObject serverResult = new JSONObject();
		String resultCode="0";
		if(serverResultObj!=null){
			serverResult = (JSONObject) serverResultObj.get("serverResult");
			resultCode = serverResult.getString("resultCode");
		}
		if("200".equals(resultCode)){
			setPromptMessage(response, "1", "切换成功");
		}else{
			if(serverResult!=null&&StringUtils.isNotBlank(serverResult.getString("resultMsg")) ){
				setPromptMessage(response, "-1",  serverResult.getString("resultMsg"));
			}else{
			setPromptMessage(response, "-1", "参数错误");
			}
		}
		logger.info("[jycloud]  switchUser...end");
		return response;
	}

	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
}