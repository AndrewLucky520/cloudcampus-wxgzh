package com.talkweb.dayDayUpLogin.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.http.service.impl.SSLClient;

/**
 * 接入天天向上CAS
 * @author zhanghuihui
 * @date 2017-08-30
 */
@Controller
@RequestMapping(value="/casToDayDayUpAction/")
public class CasToDayDayUpAction {
	
	private static final Logger logger = LoggerFactory.getLogger(CasToDayDayUpAction.class);
	
	@Autowired
	private AllCommonDataService allCommonDataService;
	 
	@Value("#{settings['clientId']}")
	private String clientId;
	@Value("#{settings['secret']}")
	private String secret;
	@Value("#{settings['url.getAccessToken']}")
	private String getAccessTokenURL; 
	@Value("#{settings['url.createLoginToken']}")
	private String createLoginTokenURL; 
	@Value("#{settings['url.redirect.loginByToken']}")
	private String loginByTokenURL; 
	@Value("#{settings['url.redirect']}")
	private String redirectURL; 
	
	@SuppressWarnings("resource")
	@RequestMapping(value = "/redirectToDayDayUp", method = RequestMethod.GET)
	public void redirectToDayDayUp(HttpServletRequest req,HttpServletResponse res){
		/*JSONObject param = new JSONObject();
		param.put("clientId", clientId);
		param.put("secret", secret); */
		String getAccessTokenURLTrue = getAccessTokenURL+"?clientId="+clientId+"&secret="+secret;
		JSONObject accessTokenTarget = JSON.parseObject(callHttpRemoteInterface(getAccessTokenURLTrue));
		logger.info("[daydayup-switch remote getAccessToken "+accessTokenTarget+"  ]");
		boolean success = accessTokenTarget.getBooleanValue("success");
		if(success==true){
			String accessToken = accessTokenTarget.getString("result");
			//User user = allCommonDataService.getUserById( 44898, 1001421439 , "20171");
			User user = (User)req.getSession().getAttribute("user");
			logger.info("[daydayup-switch login user  is "+user+"  ]");
			if(user!=null && user.getUserPart()!=null && user.getUserPart().getRole()!=null){
				String mobilePhone = ""; //家长、管理员、老师手机号
				String stdNumber ="";//学生学籍号
				T_Role role = user.getUserPart().getRole();
				
				switch (role) {
				case Teacher:
				case Parent:
				case SchoolManager:
					logger.info("[daydayup-switch login user  accountPart is  "+user.getAccountPart()+"  ]");
					if(user.getAccountPart()!=null){
					    mobilePhone = user.getAccountPart().getMobilePhone();
					    //mobilePhone="13300000001";
					}
					logger.info("[daydayup-switch login user  mobilePhone is  "+mobilePhone+"  ]");
					break;
				case Student:
					logger.info("[daydayup-switch login user  studentPart is  "+user.getStudentPart()+"  ]");
					if(user.getStudentPart()!=null){
						stdNumber = user.getStudentPart().getStdNumber();
						//stdNumber = "2222222222";
					}
					logger.info("[daydayup-switch login user  stdNumber is  "+stdNumber+"  ]");
					break;
				default:
					logger.info("[daydayup-switch default error,can not find login user role]");
					break;
				}
				if(StringUtils.isNotBlank(stdNumber) ||StringUtils.isNotBlank(mobilePhone)){
					/*param.clear();
					param.put("accessToken", accessToken);*/
					String createLoginTokenURLTrue = createLoginTokenURL;
					if(StringUtils.isNotBlank(mobilePhone)){
						//param.put("phone", mobilePhone);
						 createLoginTokenURLTrue += "?accessToken="+accessToken+"&phone="+mobilePhone;
					}else{
						//param.put("areaStudentCode", stdNumber);
						 createLoginTokenURLTrue += "?accessToken="+accessToken+"&areaStudentCode="+stdNumber;
					}
					
					JSONObject loginTokenTarget = JSON.parseObject(callHttpRemoteInterface(createLoginTokenURLTrue));
					logger.info("[daydayup-switch remote loginToken "+loginTokenTarget+"  ]");
					boolean loginSuccess = loginTokenTarget.getBooleanValue("success");
					if(loginSuccess==true){
						String loginToken = loginTokenTarget.getString("result");
						//param.clear();
						String loginByTokenURLTrue = loginByTokenURL+"?loginToken="+loginToken+"&redirectUri="+ redirectURL;
						logger.info("[daydayup-switch remote loginByTokenURLTrue "+loginByTokenURLTrue+"  ]");
						try {
							res.sendRedirect(loginByTokenURLTrue);
						}   catch (IOException e) {
							e.printStackTrace();
						}
					}else{
						try {
							PrintWriter out =  res.getWriter();
							String msg="登录失败";
							out.print("<html><head><meta charset='UTF-8'></head>");
							out.println("<script>");
							out.println("alert('" + msg + "');");
							out.println("</script>");
							return ;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}else{
					try {
						PrintWriter out =  res.getWriter();
						String msg="用户信息获取失败";
						out.print("<html><head><meta charset='UTF-8'></head>");
						out.println("<script>");
						out.println("alert('" + msg + "');");
						out.println("</script>");
						return ;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else{
				try {
					PrintWriter out =  res.getWriter();
					String msg="用户信息获取失败";
					out.print("<html><head><meta charset='UTF-8'></head>");
					out.println("<script>");
					out.println("alert('" + msg + "');");
					out.println("</script>");
					return ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}else{
			String msg = accessTokenTarget.getString("message");
			try {
				PrintWriter out =  res.getWriter();
				out.print("<html><head><meta charset='UTF-8'></head>");
				out.println("<script>");
				out.println("alert('" + msg + "');");
				out.println("</script>");
				return ;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	

	/** 发送http请求 @author zhanghuihui **/
	private String callHttpRemoteInterface(String url) {
		CloseableHttpClient client = null;
		try {
			client = new SSLClient();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		String reponseResult="";
		HttpPost post=new HttpPost(url);
				
		//StringEntity entity = new StringEntity(content,"utf-8");
        //entity.setContentEncoding("UTF-8");    
        //entity.setContentType("application/json");    
        //post.setEntity(entity); 
		try {
			 CloseableHttpResponse response = client.execute(post);
			 HttpEntity responseEntity = response.getEntity();
			 reponseResult=EntityUtils.toString(responseEntity);			
		} catch (Exception e) {
			e.printStackTrace();
		}	    
		return reponseResult;
	}
}
