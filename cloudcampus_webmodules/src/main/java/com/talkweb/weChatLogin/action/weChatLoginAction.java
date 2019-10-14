package com.talkweb.weChatLogin.action;

import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.talkweb.auth.entity.Permisson2AppID;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.weChatLogin.service.WeChatLoginService;

@RequestMapping("/wechat/login")
@Controller
public class weChatLoginAction {

	private static final Logger logger = LoggerFactory
			.getLogger(weChatLoginAction.class);

	@Autowired
	private WeChatLoginService weChatLoginService;

	@Value("#{settings['redirct.menuid.cs1002']}")
	private String cs1002;// 成绩
	@Value("#{settings['redirect.baseurl']}")
	private String baseUrl;
	@Value("#{settings['redirct.menuid.cs1008']}")
	private String cs1008;// 课表
	@Value("#{settings['redirct.menuid.cs1019']}")
	private String cs1019;// 保修
	@Value("#{settings['redirct.menuid.cs1020']}")
	private String cs1020;// 周工作
	@Value("#{settings['redirct.menuid.cs1011']}")
	private String cs1011;// 工资
	@Value("#{settings['redirct.menuid.cs1022']}")
	private String cs1022;// 场馆
	@Value("#{settings['redirct.menuid.cs1023']}")
	private String cs1023;// 考勤

	/**
	 * 登陆,根据模块id跳转
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/authredirect")
	@ResponseBody
	public ModelAndView authredirect(HttpServletRequest request,
			RedirectAttributes att) throws Exception {
		String openId = StringUtil.transformString(request
				.getParameter("user_openid"));
		String menuId = StringUtil.transformString(request
				.getParameter("menuId"));
		//String schoolId = StringUtil.transformString(request
		//		.getParameter("schoolId"));
		logger.info("WWW----weixinOPENID:"+openId);
		ModelAndView view = new ModelAndView();
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("openId", openId);
		param.put("schoolId", 0);//没有学校  默认
		if (!openId.isEmpty() && !menuId.isEmpty()) {
			Integer menuIds = Integer.valueOf(menuId);
			HashMap<Integer, String> appMap = Permisson2AppID.perAppIDMap;
			HashMap<String, Object> result = weChatLoginService
					.getUserBySDK(param);
			att.addAttribute("schoolId", result.get("schoolId"));
			att.addAttribute("userId", result.get("userId"));
			att.addAttribute("name", result.get("name"));
			att.addAttribute("accountId", result.get("accountId"));
			if (appMap.get(menuIds).toString().equals("cs1002")) {
				view.setViewName("redirect:" + baseUrl + cs1002);
			} else if (appMap.get(menuIds).toString().equals("cs1019")) {
				view.setViewName("redirect:" + baseUrl + cs1019);
			} else if (appMap.get(menuIds).toString().equals("cs1020")) {
				view.setViewName("redirect:" + baseUrl + cs1020);
			} else if (appMap.get(menuIds).toString().equals("cs1011")) {
				view.setViewName("redirect:" + baseUrl + cs1011);
			} else if (appMap.get(menuIds).toString().equals("cs1022")) {
				view.setViewName("redirect:" + baseUrl + cs1022);
			} else if (appMap.get(menuIds).toString().equals("cs1023")) {
				view.setViewName("redirect:" + baseUrl + cs1023);
			}else if (appMap.get(menuIds).toString().equals("cs1008")) {
				view.setViewName("redirect:" + baseUrl + cs1008);
			}

		}
		// logger.debug("examUrl:{},baseUrl:{}",examUrl,baseUrl);

		return view;
	}

	/**
	 * 登陆,根据模块id跳转
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/authlogin")
	@ResponseBody
	public ModelAndView authlogin(HttpServletRequest request,
			RedirectAttributes att) throws Exception {
		String wx_mp_id = StringUtil.transformString(request
				.getParameter("wx_mp_id"));
		String menuId = StringUtil.transformString(request
				.getParameter("menuId"));
		// String
		// schoolId=StringUtil.transformString(request.getParameter("schoolId"));
//		String returnUrl = URLEncoder
//				.encode("http://192.168.26.15:8080/cloudcampus_webmodules/wechat/login/authredirect.do?menuId="
//						+ menuId);
		String returnUrl = URLEncoder
				.encode(baseUrl+"cloudcampus_webmodules/wechat/login/authredirect.do?menuId="
						+ menuId);
		logger.info("WWW----weixinloginURL"+returnUrl);
		ModelAndView view = new ModelAndView();
		HashMap<String, Object> param = new HashMap<String, Object>();

		att.addAttribute("returnUrl", returnUrl);
		att.addAttribute("wx_mp_id", 7018);//岳麓教育 写死

		view.setViewName("redirect:http://test.web.wxres.talkedu.cn/OAuthHandler.ashx");

		return view;
	}

}