package com.talkweb.questionnaire.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.talkweb.common.action.BaseAction;
import com.talkweb.questionnaire.service.UpdateQuestionnaireService;

@Controller
@RequestMapping("/questionnaire")
public class UpdateQuestionnaireAction extends BaseAction{
	
	Logger logger = LoggerFactory.getLogger(UpdateQuestionnaireAction.class);
	
	@Autowired
	private UpdateQuestionnaireService updateQuestionnaireService;
	
//	@RequestMapping(value = "/update1002269", method = RequestMethod.GET)
//	@ResponseBody
	public String queryQuestionList(HttpServletRequest req, HttpServletResponse res){
		try{
			updateQuestionnaireService.update1002269();
			logger.info("update1002269 : 更新成功！");
			return "更新成功！";
		}catch(Exception e){
			e.printStackTrace();
			logger.info("update1002269 : 更新失败\n" + e.getMessage());
			return "更新失败：\n" + e.getMessage();
		}
	}
}
