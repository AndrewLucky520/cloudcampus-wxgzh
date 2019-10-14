package com.talkweb.weChatLogin.action;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONObject;

@RequestMapping("/talkCloud/wechat")
@Controller
public class WXGZHLoginAction {

	private static final Logger logger = LoggerFactory
			.getLogger(WXGZHLoginAction.class);


	/**
	 * 登陆
	 */
	@RequestMapping(value = "/login")
	@ResponseBody
	public JSONObject login(HttpServletRequest request,
			RedirectAttributes att) throws Exception {
        JSONObject json = new JSONObject();
        json.put("zhh", "test!!");
        json.put("userId", request.getSession().getAttribute("userId"));
		return json;
	}


}