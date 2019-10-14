package com.talkweb.questionnaire.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.questionnaire.service.AppQuestionnaireService;
import com.talkweb.questionnaire.service.QuestionnaireService;

@Controller
@RequestMapping("/questionnaire/app")
public class AppQuestionnaireAction extends BaseAction {
	Logger logger = LoggerFactory.getLogger(AppQuestionnaireAction.class);
 

	@Autowired
	private AppQuestionnaireService appQuestionnaireService;
	
	@Autowired
	private QuestionnaireService questionnaireService;
 
    /*
     *如果questionId 为空就出所有 当前时间未介绍
     * 如果有questionId  ,就出当前具体信息
     * */
	@RequestMapping(value = "/queryQuestionnaireByUser", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryQuestionnaireByUser(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String userId = request.getString("userId");
			String schoolId = request.getString("schoolId");
			String lastIndex = request.getString("lastIndex");
			String questionId = request.getString("questionId");
			logger.info("userId===" + userId);
			if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(schoolId)) {
				response.put("code", -1);
				response.put("msg", "schoolId或userId参数为空");
				return response;
			}

			if (StringUtil.isEmpty(lastIndex)) {
				request.put("lastIndex", 0);
				request.put("isRequestFirstPageData", true); // 是否请求第一页的数据
			} else {
				request.put("isRequestFirstPageData", false);
			}
           
			JSONObject data = appQuestionnaireService.queryQuestionnaireByUser(request);
			if (StringUtil.isEmpty(questionId)) { //未传questionId 情况
				response.put("code",1);
			}else { //已传questionId
				JSONObject question = data.getJSONObject("question");
				if (question==null) {
					request.remove("schoolId");// 多学校切换了身份 所以把学校去掉
					List<JSONObject> list = questionnaireService.queryQuestionList(request);
					if (list.size() > 0) {
						response.put("code",2);
					}else {
						response.put("code",3);
					}
				}else {
					if (StringUtil.isEmpty(lastIndex)) {
						Date questionStartDate = question.getDate("questionStartDate");
						Date questionEndDate =  question.getDate("questionEndDate");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						//1、正常显示  2、当前身份没有这次数据  3、当前数据已删除   4、当前数据已结束  5.当前数据未开始   
							String s = sdf.format(new Date());
							try {
								Date date =  sdf.parse(s);
								if (questionStartDate.getTime() > date.getTime()) {
									response.put("code",5);
								}else if (questionEndDate.getTime() < date.getTime()) {
									response.put("code",4);
								}else{
									response.put("code",1);
								}
							} catch (Exception e) {
							   e.printStackTrace();
							}
					}else { // 后面页数都是返回1
						response.put("code",1);
					}
				}
				
				
			}
			response.put("data", data);
			response.put("msg", "success");
		} catch (RuntimeException e) {
			String msg = e.getMessage();
			response.put("code", -1);
			response.put("msg", msg);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.put("code", -1);
			response.put("msg", "后台错误，请联系管理员");
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/updateForSpecificTarget", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateForSpecificTarget(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String userId = request.getString("userId");
			String schoolId = request.getString("schoolId");
			String questionId = request.getString("questionId");
			if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(schoolId) || StringUtil.isEmpty(questionId)) {
				response.put("code", -1);
				response.put("msg", "参数为空");
				return response;
			}
			appQuestionnaireService.updateForSpecificTarget(request);
			response.put("code", 0);
			response.put("msg", "success");
		} catch (RuntimeException e) {
			String msg = e.getMessage();
			response.put("code", -1);
			response.put("msg", msg);
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.put("code", -1);
			response.put("msg", "后台错误，请联系管理员");
		}
		return response;
	}

	@RequestMapping(value = "/updateForTableTarget", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateForTableTarget(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String userId = request.getString("userId");
			String schoolId = request.getString("schoolId");
			String questionId = request.getString("questionId");
			String indexRow = request.getString("indexRow");
			String tableRuleId = request.getString("tableRuleId");
			if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(schoolId) || StringUtil.isEmpty(questionId)
					|| StringUtil.isEmpty(indexRow) || StringUtil.isEmpty(tableRuleId)) {
				response.put("code", -1);
				response.put("msg", "参数为空");
				return response;
			}

			appQuestionnaireService.updateForTableTarget(request);

			response.put("code", 0);
			response.put("msg", "success");
		} catch (RuntimeException e) {
			String msg = e.getMessage();
			response.put("code", -1);
			response.put("msg", msg);
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.put("code", -1);
			response.put("msg", "后台错误，请联系管理员");
		}
		return response;
	}
}