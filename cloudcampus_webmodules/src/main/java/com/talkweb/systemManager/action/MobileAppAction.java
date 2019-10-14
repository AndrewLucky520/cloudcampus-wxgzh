package com.talkweb.systemManager.action;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.auth.service.AuthService;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.MD5Util;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.http.service.CallRemoteInterface;
import com.talkweb.onecard.common.AESUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
@Controller(value = "mobileAction")
@RequestMapping(value = "/mobile/")
public class MobileAppAction extends BaseAction {

	private static final Logger logger = LoggerFactory
			.getLogger(MobileAppAction.class);

	@Autowired
	private CallRemoteInterface crservice;

	@Autowired
	private AuthService authService;
	
	@Autowired
	private AllCommonDataService cmdService;
	
	private static BASE64Encoder encoder = new BASE64Encoder();
	private static BASE64Decoder decoder = new BASE64Decoder();
	
	
	@Value("#{settings['app.public.login.url']}")
	private String rootUrl;

	final String enckeyBytes = "FD363AFD71CE48E56B865D0B" ;
    final String descBytes   = "DF5C58DDBF076A92C1253721" ;
	/**							
	 * 登录
	 * 
	 * @param userId
	 *            账号
	 * @param passWord
	 *            密码
	 * @param session
	 *            会话
	 * @return
	 */
	@RequestMapping(value = "userManage/login", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject login(HttpServletRequest req, @RequestBody JSONObject reqParams) {

		HttpSession session = req.getSession();
		JSONObject rs = new JSONObject();
		int code = 1;
		String msg = "登陆成功！";
		
		boolean needValidate = false;
		if(session.getAttribute("mobile.public.needValidate")!=null){
			needValidate = (boolean) session.getAttribute("mobile.public.needValidate");
		}
			
		String openId = reqParams.getString("openId");
		String sourceType = reqParams.getString("sourceType");
		String accountName = reqParams.getString("accountName").trim();
		String password = reqParams.getString("password").trim();
		String validateMsg = reqParams.getString("validateMsg");
		
		if(needValidate&&(validateMsg==null||validateMsg.trim().length()==0)){
			code = -5;
			msg = "需要输入验证码";
		}else{
			if(accountName==null||accountName.trim().length()==0||password==null||password.trim().length()==0){
				code = -2;
				msg = "用户名或密码不能为空";
			}else{
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("accountName", accountName);
				param.put("timestamp",  new Date().getTime()/1000);
				param.put("nonce",  new Date().getTime());
				try {
					rs = sendRequest("/Login/getAccountId", param);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					code = -13;
					msg = "登陆服务不可用！";
				}
				long accId = 0l;
				if(rs.containsKey("accountId")){
					accId = rs.getLongValue("accountId");
				}
				if(accId==0||rs.getIntValue("code")!=0){
					code = -1;
					msg = "账号不存在！";
				}else{
					param.put("timestamp",  new Date().getTime()/1000);
					param.put("accountId",  accId);
					param.put("nonce",  new Date().getTime());
					if(needValidate){
						
						param.put("validateSession",  session.getId());
						param.put("validateCode",  validateMsg);
					}else{
						
						param.put("validateSession", "");
						param.put("validateCode",  "");
					}
					param.put("password",  password);
					param.put("pwd",  getPwdEncry(param));
					
					try {
						JSONObject lrs =  sendRequest("/Login/login", param);
						int retcode = lrs.getIntValue("code");
						switch (retcode){
						
							case 0:
								session.setAttribute("mobile.public.loginSuccess", true);
								authService.updateMoblieLoginState(accId,openId,sourceType,1);
								List<JSONObject> loginData = getLoginData(accId);
								session.setAttribute("mobile.public.loginData", loginData);
								rs.put("data", loginData);
								if(loginData.size()>0){
									
									code = 1;
									msg = "登陆成功！";
								}else{
									code = -20;
									msg = "功能暂未向该角色开放！";
									session.setAttribute("mobile.public.loginSuccess", false);
								}
								break;
							case 1:
								session.setAttribute("mobile.public.needValidate", true);
								code = -5;
								msg = "请输入验证码！";
								break;
							case 2:
								session.setAttribute("mobile.public.needValidate", true);
								code = -10;
								msg = "验证码错误，请重新输入！";
								break;
							case 3:
								code = -3;
								msg = "用户被禁用";
								break;
							case 4:
								code = -4;
								msg = "操作过于频繁";
								break;
							case 12:
								code = -12;
								msg = "账号不存在";
								break;
							default:
								code = -20;
								msg = "登陆失败，账号或密码错误";
								break;
						}
//						return lrs;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						code = -13;
						msg = "登陆服务不可用！";
					}
				}
				
			}
		}
		rs.put("code", code);
		rs.put("msg", msg);
//		authService.
		return rs;			
	}
	
	/**
	 * 获取第三方账号登陆状态
	 * @param req
	 * @param reqParams
	 * @return
	 */
	@RequestMapping(value = "userManage/loginState", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject loginState(HttpServletRequest req, @RequestBody JSONObject reqParams) {

		JSONObject rs = new JSONObject();
		int code = 1;
		String msg = "已登录";
		
		HttpSession session = req.getSession();
		boolean loginSuccess = false;
		if(session.getAttribute("mobile.public.loginSuccess")!=null){
			loginSuccess = (boolean) session.getAttribute("mobile.public.loginSuccess");
		}
		long reqAccountId = 0;
		if( reqParams.containsKey("accountId")&&reqParams.getString("accountId").trim().length()>0){
			try{
				String tmp = AESUtil.Decrypt(reqParams.getString("accountId"), "talkweb.com@@yxy");
				
				reqAccountId = Long.parseLong(tmp);
			}catch(NumberFormatException e){
				reqAccountId = 0;
			}catch (Exception e) {
				// TODO: handle exception
				reqAccountId = 0;
			}
			
			if(reqAccountId!=0){
				Account a = cmdService.getAccountAllById(reqAccountId)
						;
				if(a!=null){
					session.setAttribute("mobile.public.loginSuccess", true);
					List<JSONObject> loginData = getLoginData(reqAccountId);
					session.setAttribute("mobile.public.loginData", loginData);
					rs.put("data", loginData);
					loginSuccess = true;
				}else{
					loginSuccess = false;
				}
			}
		}
		try{
			
			if(loginSuccess){
				List<JSONObject> logindata = (List<JSONObject>) session.getAttribute("mobile.public.loginData");
				rs.put("data", logindata);
			}else{
				String openId = reqParams.getString("openId");
				String sourceType = reqParams.getString("sourceType");
				JSONObject logjson = authService.getMoblieLoginState(   openId, sourceType);
				int state  = logjson.getIntValue("status");
				if(state == 1){
					long accountId = logjson.getLongValue("accountId");
					List<JSONObject> logindata = getLoginData(accountId);
					rs.put("data", logindata);
					
					authService.updateMoblieLoginState(accountId, openId, sourceType, 1);
				}else{
					code = 0;
				}
			}
		}catch(Exception e){
			code = -1;
			msg = "请求错误，程序处理异常";
			e.printStackTrace();
		}
		rs.put("code", code);
		if(code == 0){
			msg = "未登录";
		}
		rs.put("msg", msg);
		
//		authService.
		return rs;			
	}
	/**
	 * 获取第三方账号登陆状态
	 * @param req
	 * @param reqParams
	 * @return
	 */
	@RequestMapping(value = "userManage/logout", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject logout(HttpServletRequest req, @RequestBody JSONObject reqParams) {
		JSONObject rs = new JSONObject();
		int code = 1;
		String msg = "操作成功！";
		HttpSession session = req.getSession();
		session.setAttribute("mobile.public.loginSuccess", false);
		try{
			
			String openId = reqParams.getString("openId");
			String sourceType = reqParams.getString("sourceType");
			JSONObject logjson = authService.getMoblieLoginState(   openId, sourceType);
			int state  = logjson.getIntValue("status");
			if(state == 1&&logjson.containsKey("accountId")){
				long accountId = logjson.getLongValue("accountId");
				if(accountId!=0){
					authService.updateMoblieLoginState(accountId, openId, sourceType, 0);
				}
			}
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
	/**
	 * 获取第三方账号登陆状态
	 * @param req
	 * @param reqParams
	 * @return
	 */
	@RequestMapping(value = "userManage/refreshValiPic" )
	@ResponseBody
	public void refreshValiPic(HttpServletRequest req ,HttpServletResponse res) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("validateSession", req.getSession().getId());
		try {
			JSONObject rs = sendRequest("/Login/refreshValidate", params);
			byte[] fb = decoder.decodeBuffer( rs.getString("validatePic"));
			res.setContentType("image/png");
			OutputStream os = res.getOutputStream();
			os.write(fb);
			os.flush();
			os.close();
//			return fb;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("accountName", "19310000299");
		param.put("timestamp",  new Date().getTime());
		param.put("nonce",  new Date().getTime());
		
		
		System.out.println( );
	}
	/**
	 * 登陆处对接深圳服务组装请求
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private JSONObject sendRequest(String url,Map<String, Object> params) throws Exception{
		JSONObject rs = new JSONObject();
		int code = 1;
		String msg = "调用成功！";
		
		params.toString();
		JSONObject req = new  JSONObject();
		JSONObject headVal = new JSONObject();
		headVal.put("appId", "98006");
		headVal.put("deviceType", "1");
		headVal.put("accountId", params.containsKey("accountId")?params.get("accountId"):"");
		headVal.put("v", "1");
		headVal.put("brand", "");
		headVal.put("model", "");
		headVal.put("systemVersion", "");
		headVal.put("uuid", "");
		req.put("head", headVal);
		String body = JSONObject.toJSONString(params);
		//3DES 加密
		byte[] bodybytes = ThreeDES.encryptMode(enckeyBytes, body.getBytes( ));
		// Base64加密
		body = encoder.encode(bodybytes);
		req.put("body", body);
		String cxAccount =decodeUnicode( crservice.updateHttpRemoteInterface(rootUrl+url, req));
		if(cxAccount==null||cxAccount.trim().length()==0){
			code = -6;
			msg = "远程登陆服务不可用！";
		}else{
			JSONObject response = JSON.parseObject(cxAccount);
			if(response==null){
				code = -7;
				msg = "登陆服务出错！";
			}else{
				String resBody = response.getString("body");
				JSONObject ret = response.getJSONObject("ret");
				rs.putAll(ret);
				//base64解密
					
				byte[] resBodyBytes = decoder.decodeBuffer(resBody);
				resBody = new String(ThreeDES.decryptMode(enckeyBytes, resBodyBytes) );
				JSONObject bodyj = JSON.parseObject(resBody);
				if(bodyj!=null){
					
					rs.putAll(bodyj);
				}
				return rs;
			}
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
		
	}
	
	public static String decodeUnicode(String theString) {      
		   
	    char aChar;      
	   
	     int len = theString.length();      
	   
	    StringBuffer outBuffer = new StringBuffer(len);      
	   
	    for (int x = 0; x < len;) {      
	   
	     aChar = theString.charAt(x++);      
	   
	     if (aChar == '\\') {      
	   
	      aChar = theString.charAt(x++);      
	   
	      if (aChar == 'u') {      
	   
	       // Read the xxxx      
	   
	       int value = 0;      
	   
	       for (int i = 0; i < 4; i++) {      
	   
	        aChar = theString.charAt(x++);      
	   
	        switch (aChar) {      
	   
	        case '0':      
	   
	        case '1':      
	   
	        case '2':      
	   
	        case '3':      
	   
	       case '4':      
	   
	        case '5':      
	   
	         case '6':      
	          case '7':      
	          case '8':      
	          case '9':      
	           value = (value << 4) + aChar - '0';      
	           break;      
	          case 'a':      
	          case 'b':      
	          case 'c':      
	          case 'd':      
	          case 'e':      
	          case 'f':      
	           value = (value << 4) + 10 + aChar - 'a';      
	          break;      
	          case 'A':      
	          case 'B':      
	          case 'C':      
	          case 'D':      
	          case 'E':      
	          case 'F':      
	           value = (value << 4) + 10 + aChar - 'A';      
	           break;      
	          default:      
	           throw new IllegalArgumentException(      
	             "Malformed   \\uxxxx   encoding.");      
	          }      
	   
	        }      
	         outBuffer.append((char) value);      
	        } else {      
	         if (aChar == 't')      
	          aChar = '\t';      
	         else if (aChar == 'r')      
	          aChar = '\r';      
	   
	         else if (aChar == 'n')      
	   
	          aChar = '\n';      
	   
	         else if (aChar == 'f')      
	   
	          aChar = '\f';      
	   
	         outBuffer.append(aChar);      
	   
	        }      
	   
	       } else     
	   
	       outBuffer.append(aChar);      
	   
	      }      
	   
	      return outBuffer.toString();      
	   
	     }     
	
	/**
	 * 获取登陆成功的账号数据 规则：只取教师的userId及
	 * @param accId
	 * @return
	 */
	private List<JSONObject> getLoginData(long accId) {
		// TODO Auto-generated method stub
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		Account act = cmdService.getAccountAllById(accId);
		
		Map<Long,User> schooluserMap = new HashMap<Long, User>();
		Map<Long,School> userIdSchoolMap = new HashMap<Long, School>();
		if(act!=null&&act.getUsers()!=null){
			for(User user:act.getUsers()){
				if(user.getUserPart()==null||user.getUserPart().getRole()==null){
					continue;
				}
				long userId = user.getUserPart().getId();
				T_Role role = user.getUserPart().getRole();
				if(!role.equals(T_Role.Teacher)&&!role.equals(T_Role.SchoolManager)&&!role.equals(T_Role.Staff) ){
					continue;
				}
				School school = cmdService.getSchoolByUserId(0, userId);
				if(school==null||school.getId()==0l){
					continue;
				}
				userIdSchoolMap.put(userId, school);
				long schoolId = school.getId();
				if(schooluserMap.containsKey(schoolId)){
					User existUser = schooluserMap.get(schoolId);
					if(!existUser.getUserPart().getRole().equals(T_Role.Teacher)){
						schooluserMap.put(schoolId, user);
					}
				}else{
					schooluserMap.put(schoolId, user);
				}
			}
		}
		for(Iterator<Long> it = schooluserMap.keySet().iterator();it.hasNext();){
			long schoolId = it.next();
			User user = schooluserMap.get(schoolId);
			long userId = user.getUserPart().getId();
			School sch  = userIdSchoolMap.get(userId);
			String schoolName = sch.getName();
			String accountName = user.getAccountPart().getName();
			JSONObject obj = new JSONObject();
			
			obj.put("userId", userId);
			obj.put("schoolId", schoolId);
			obj.put("accountId", user.getAccountPart().getId());
			obj.put("schoolName", schoolName);
			obj.put("name", accountName);
			rsList.add(obj);
		}
		return rsList;
	}

	@SuppressWarnings("restriction")
	private Object getPwdEncry(Map<String, Object> param) {
		// TODO Auto-generated method stub
		String password = param.get("password").toString();
		String key = MD5Util.getMD5String(
						MD5Util.getMD5String(password).toUpperCase()+String.valueOf(param.get("accountId"))
						).toUpperCase();
		String smw = "{\"accountId\":"+param.get("accountId")+","
					+" \"timestamp\":"+param.get("timestamp")+","
					+" \"nonce\":\""+param.get("nonce")+"\"}";
		String rs = encoder.encode(ThreeDES.encryptMode(key, smw.getBytes()));
		param.remove("password");
		return rs;
	}

}
