package com.talkweb.commondata.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.UpGradeService;
/**
 * 升级学年学期action
 * @author zhanghuihui
 * @date 2017.08.08
 */
@Controller
@RequestMapping(value="/upGradeAction/")
public class UpGradeAction extends BaseAction{
	@Autowired
	private UpGradeService upGradeService ; 
	
	private static final Logger logger = LoggerFactory.getLogger(UpGradeAction.class);
	@RequestMapping(value="upGradeAllSchools")
	public String upGradeAllSchools(HttpServletRequest req,HttpServletResponse res){
		JSONObject json = new JSONObject();
		String xnxq = (String) req.getParameter("xnxq");
		json.put("termInfoId", xnxq);
		try {
			upGradeService.upGradeAllSchools(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "index";
	}
	@RequestMapping(value="deGradeAllSchools")
	public String deGradeAllSchools(HttpServletRequest req,HttpServletResponse res){
		JSONObject json = new JSONObject();
		String xnxq = (String) req.getParameter("xnxq");
		json.put("termInfoId",xnxq);
		try {
			upGradeService.deGradeAllSchools(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "index";
	}
	@RequestMapping(value="upGradeBySchoolId")
	public String upGradeBySchoolId(HttpServletRequest req,HttpServletResponse res){
		String schoolId = (String) req.getParameter("schoolId");
		String xnxq = (String) req.getParameter("xnxq");
		JSONObject json = new JSONObject();
		json.put("termInfoId", xnxq);
		json.put("schoolId", schoolId);
		try {
			upGradeService.upGradeBySchoolId(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "index";
	}
	@RequestMapping(value="deGradeBySchoolId")
	public String deGradeBySchoolId(HttpServletRequest req,HttpServletResponse res){
		String schoolId = (String) req.getParameter("schoolId");
		String xnxq = (String) req.getParameter("xnxq");
		JSONObject json = new JSONObject();
		json.put("termInfoId",xnxq);
		json.put("schoolId", schoolId);
		try {
			upGradeService.deGradeBySchoolId(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "index";
	}
	@RequestMapping(value="getTermInfos")
	public String getTermInfos(HttpServletRequest req,HttpServletResponse res){
		JSONObject json = new JSONObject();
		String xnxq = (String) req.getParameter("xnxq");
		json.put("termInfoId", xnxq);
		try {
			upGradeService.getTermInfos(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "index";
	}
	
	@RequestMapping(value="getTermInfosBySchoolId")
	public String getTermInfosBySchoolId(HttpServletRequest req,HttpServletResponse res){
		String schoolId = (String) req.getParameter("schoolId");
		String xnxq = (String) req.getParameter("xnxq");
		JSONObject json = new JSONObject();
		json.put("termInfoId", xnxq);
		json.put("schoolId", schoolId);
		try {
			 upGradeService.getTermInfosBySchoolId(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "index";
	}
}
