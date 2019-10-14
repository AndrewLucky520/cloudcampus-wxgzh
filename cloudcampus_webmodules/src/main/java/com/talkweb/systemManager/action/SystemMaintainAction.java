package com.talkweb.systemManager.action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.talkweb.common.tools.GenerateRandomPassword;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.systemManager.exception.BusinessDataExistsException;
import com.talkweb.systemManager.exception.DeleteOperationException;
import com.talkweb.systemManager.exception.InsertOperationException;
import com.talkweb.systemManager.service.SystemMaintainService;

/**
 * @ClassName SystemMaintainAction
 * @author Homer
 * @version 1.0
 * @Description 学校维护控制类
 * @date 2015年3月19日
 */
@Controller
@RequestMapping(value = "/maintenance/")
public class SystemMaintainAction {

	@Autowired
	private SystemMaintainService systemMaintainServiceImpl;
	
	@RequestMapping(value = "querySchoolList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> querySchoolList(HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();
		String xzqhm = request.getParameter("xzqhm");
		String xxmc = request.getParameter("xxmc");
		
		List<Map<String, Object>> schoolList = systemMaintainServiceImpl.querySchoolList(xzqhm, xxmc);
		result.put("total", schoolList.size());
		result.put("rows", schoolList);
		return result;
	}
	
	
	@RequestMapping(value = "addSchool", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> addSchool(HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();
		String code = "0";
		String msg = "";
		String xzqhm = request.getParameter("xzqhm");
		//系统自动生成
		String xxdm = UUIDUtil.getUUID();
		String xxmc = request.getParameter("xxmc");
		String xxlb = request.getParameter("xxlb");
		String dmid = request.getParameter("dmid");
		String[] pycc = request.getParameterValues("pycc");
		String userId = request.getParameter("userId");
		String oriPassword = request.getParameter("oriPassword");
		List<String> pyccList = Arrays.asList(pycc);
		try {
		   systemMaintainServiceImpl.addSchool(xzqhm, xxdm, xxmc, xxlb,dmid,pyccList, userId, oriPassword);
		} catch (InsertOperationException e) {
		   code = "-1";
		   msg = e.getMessage();
		}
		
		result.put("code", code);
		result.put("msg", msg);
		return result;
	}
	
	
	@RequestMapping(value = "delSchool", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delSchool(HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();
		String code = "0";
		String msg = "";
		String[] xxdm = request.getParameterValues("xxdm");

		List<String> xxdmList = Arrays.asList(xxdm);
			
		try {
			systemMaintainServiceImpl.deleteSchool(xxdmList);
		} catch (BusinessDataExistsException e) {
			code = "-1";
			msg = e.getMessage();
		} catch (DeleteOperationException e) {
			code = "-1";
			msg = e.getMessage();
		}
		
		
		result.put("msg", msg);
		result.put("code", code);
		return result;
		
	}
	
	
	@RequestMapping(value = "queryTeacherAccountList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryTeacherAccountList(HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();
		String xzqhm = request.getParameter("xzqhm");
		String xxdm = request.getParameter("xxdm");
		String zghxm = request.getParameter("zghxm");
		
		List<Map<String, Object>> accounts = systemMaintainServiceImpl.queryTeacherAccountList(xzqhm, xxdm, zghxm);
		result.put("total", accounts.size());
		result.put("rows", accounts);
		return result;
	}
	
	
	@RequestMapping(value = "queryStudentAccountList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryStudentAccountList(HttpServletRequest request){

		Map<String,Object> result = new HashMap<String, Object>();
		String xxdm = request.getParameter("xxdm");
		String xzqhm = request.getParameter("xzqhm");
		String xnxq = request.getParameter("xnxq");
		String xn = xnxq.substring(0,4);
		String xqm = xnxq.substring(4,5);
		String xjhxm = request.getParameter("xjhxm");
		String grade = request.getParameter("grade");
		String bh = request.getParameter("bh");
		List<String> gradeList = null;
		List<String> bhList = null;
		if(null != grade){
			String[] gradeArray = grade.split(",");
			gradeList = Arrays.asList(gradeArray);
		}
		if(null != bh){
			String[] bhArray = bh.split(",");
			bhList = Arrays.asList(bhArray);
		}	
		
		List<Map<String, Object>>  accounts = systemMaintainServiceImpl.queryStudentAccountList(xzqhm, xxdm, xn, xqm, gradeList, bhList, xjhxm);
		
		result.put("total", accounts.size());
		result.put("rows", accounts);
		return result;
	}
	
	
	@RequestMapping(value = "queryParentAccountList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryParentAccountList(HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();
		String xxdm = request.getParameter("xxdm");
		String xzqhm = request.getParameter("xzqhm");
		String xnxq = request.getParameter("xnxq");
		String xn = xnxq.substring(0,4);
		String xqm = xnxq.substring(4,5);
		String grade = request.getParameter("grade");
		String bh = request.getParameter("bh");
		String xjhxm = request.getParameter("xjhxm");
		List<String> gradeList = null;
		List<String> bhList = null;
		if(null != grade){
			String[] gradeArray = grade.split(",");
			gradeList = Arrays.asList(gradeArray);
		}
		if(null != bh){
			String[] bhArray = bh.split(",");
			bhList = Arrays.asList(bhArray);
		}	
		
		List<Map<String, Object>> accounts = systemMaintainServiceImpl.queryParentAccountList(xzqhm, xxdm, xn, xqm, gradeList, bhList, xjhxm);
		result.put("total", accounts.size());
		result.put("rows", accounts);
		return result;
	}
	
	
	@RequestMapping(value = "updatePwds", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> updatePwds(HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();
		Map<String,Object> data = new HashMap<String, Object>();
		String code = "0";
		String msg = "";
		int effectRow = 0;
		String[] zgh = request.getParameter("zgh").split(",");
		List<String> zghList = Arrays.asList(zgh);
		String style = request.getParameter("style");
		String oriPassWord = request.getParameter("oriPassWord");
		
		if("1".equals(style)){
			//帐号与密码相同
			for (Iterator<String> iterator = zghList.iterator(); iterator.hasNext();) {
				String zghL = (String) iterator.next();
				effectRow = systemMaintainServiceImpl.updatePwdsEqualsAccount(zghL);
				if(effectRow == 0){
					code = "-1";
					msg = "修改密码失败";
				}
			}
				
		}else if("2".equals(style)){
			//随机密码
			String randPass = GenerateRandomPassword.generateRandomPassword();
			effectRow = systemMaintainServiceImpl.updatePwds(zghList, randPass, randPass);
			if(effectRow == 0){
				code = "-1";
				msg = "修改密码失败";
			}
		}else{
			//指定密码
			if(null == oriPassWord || "".equals(oriPassWord.trim())){
				String randPass = GenerateRandomPassword.generateRandomPassword();
				effectRow = systemMaintainServiceImpl.updatePwds(zghList, randPass, randPass);
			    if(effectRow == 0){
					code = "-1";
					msg = "修改密码失败";
				}
			}else{
				effectRow = systemMaintainServiceImpl.updatePwds(zghList, oriPassWord, oriPassWord);
				if(effectRow == 0){
					code = "-1";
					msg = "修改密码失败";
				}
			}
		}
		
		
		result.put("code",code);
		data.put("msg", msg);
		result.put("data", data);
		return result;
	}
	
	
	@RequestMapping(value = "querySchoolByAreaCode", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String, Object>> querySchoolByAreaCode(HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();	
		String xzqhm = request.getParameter("xzqhm");
		List<Map<String, Object>> schools = systemMaintainServiceImpl.querySchoolByAreaCode(xzqhm);
	
		return schools;
	}
	
	@RequestMapping(value = "getNJByXXXNXQ", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String,Object>> getNJByXXXNXQ(HttpServletRequest request){
			
		Map<String,Object> result = new HashMap<String, Object>();	
		String xxdm = request.getParameter("xxdm");
		String xnxq = request.getParameter("xnxq");
		String xn = xnxq.substring(0, 4);
		
		List<Map<String,Object>> synj = systemMaintainServiceImpl.getNJByXXXNXQ(xxdm, xn);
		result.put("total", synj.size());
		result.put("rows", synj);
		return synj;
	}
	
	
	@RequestMapping(value = "getClassByNJXXDM", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String,Object>> getClassByNJXXDM(HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();	
		String xxdm = request.getParameter("xxdm");
		String xnxq = request.getParameter("xnxq");
		String synj = request.getParameter("synj");
		String xn = xnxq.substring(0, 4);
		String xqm = xnxq.substring(4);
		
		String[] synjArray = synj.split(",");
		List<String> synjList = Arrays.asList(synjArray);
		
		
		List<Map<String,Object>> bj = systemMaintainServiceImpl.getClassByNJXXDM(xxdm, xn, xqm, synjList);
		result.put("total", bj.size());
		result.put("rows", bj);
		return bj;
		
		
	}
	
}
