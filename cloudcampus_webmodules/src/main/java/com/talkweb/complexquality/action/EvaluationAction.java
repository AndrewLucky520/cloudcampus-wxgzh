package com.talkweb.complexquality.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.utils.Base64Util;
import com.talkweb.common.utils.BcryptHashing;
import com.talkweb.complexquality.service.EvalutionService;
import com.talkweb.http.service.CallRemoteInterface;
import com.talkweb.scoreManage.service.ScoreManageService;

/******
 * 综合素质评价控制器
 * @author administator
 * @time 2015-11-17
 */
@RequestMapping("/ComplexQuality/Evaluation/")
@Controller
public class EvaluationAction extends BaseAction{
	
	@Autowired
	private  EvalutionService evalutionService;
	
	@Autowired
	private ScoreManageService scoreManageService;
	
	@Autowired
	private CallRemoteInterface callRemoteInterface;//调用远程接口的服务类
	
	@Value("#{settings['renrentong.evalutionurl']}")
	private String url;//评价页面
	
	@Value("#{settings['renrentong.checkurl']}")
	 private String url1;//绑定action地址
	
   
	@RequestMapping(value="checkStu",method=RequestMethod.POST)
	@ResponseBody
	public  Map<String,Object> checkStu(HttpServletRequest request)
	{
		//1.获取学生账号编号，学校编号。并且设置访问数据库的参数。
	    HttpSession session=request.getSession();
	    Account account=(Account)(session.getAttribute("account"));//账号编号
	    String accountId=account==null?"":account.getId()+"";//账号编号
	    String studentName=account==null?"":account.getName();//学生姓名
	    String schoolId=getXxdm(request);//学校编号
	    String actionUrl=url;
	    
	    //参数，访问数据库方法的参数
	    Map<String, Object> param=new HashMap<String, Object>();
	    param.put("accountId", accountId);
	    param.put("schoolId", schoolId);
	    
	    //返回结果信息存放map
	    Map<String,Object> result=new HashMap<String, Object>();
	    
	    //2.从数据库中读取绑定关系列表，并且更加绑定关系处理，如果返回的列表为空表示没有绑定，否则表示已经绑定了学籍号。
		List<Map<String,Object>> studentRelative=evalutionService.getStudentReative(param);//学生与账号绑定关心
		
		if(CollectionUtils.isEmpty(studentRelative))
		{//如果列表为空，表示没有绑定学籍号
			result.put("result", -1);
			result.put("msg", "学生没有绑定学籍号!");
			result.put("name",studentName);
			result.put("pname","");
			result.put("password","");
			result.put("url", "");
			return result;
		}
		
		if(CollectionUtils.isNotEmpty(studentRelative))
		{//如果列表中的第一条数据是空值表示没有绑定学籍号
		  Map<String,Object> relative=studentRelative.get(0);
		  String studentCode=relative==null?"":StringUtil.transformString(relative.get("studentCode"));
		  
		  if(StringUtil.isEmpty(studentCode))
		  {
			  result.put("result", -1);
			  result.put("msg", "学生没有绑定学籍号!");
			  result.put("name",studentName);
			  result.put("pname","");
			  result.put("password","");
			  result.put("url", "");
			  return result;
		  }
		}
		
		
		//3.访问远程接口，获取评价页面地址
		
		String code=StringUtil.transformString(studentRelative.get(0).get("studentCode"));//学籍号
		String name=StringUtil.transformString(studentRelative.get(0).get("studentName"));//姓名
		
		if(!StringUtil.isEmpty(code))  code=code.trim();
		if(!StringUtil.isEmpty(name))  name=name.trim();
		
		String password=BcryptHashing.encrypt(code);
		
		try {
			//组装url
			code=Base64Util.encrypt(code);
			name=Base64Util.encrypt(name);
			
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", -2);
			result.put("msg", "编码错误!");
			result.put("name",studentName);
			result.put("pname","");
			result.put("password","");
			result.put("url", "");
			
			return result;
		}
		
		result.put("result", 0);
		result.put("msg", "成功!");
		result.put("code",code);
		result.put("name",studentName);
		result.put("pname",name);
		result.put("password",password);
		result.put("url", actionUrl);
		return result;
	}
	
	@RequestMapping(value="bindingStu",method=RequestMethod.POST)
	@ResponseBody
	public  Map<String,Object> bindingStu(@RequestBody Map<String, Object> param,HttpServletRequest request)
	{
		Map<String,Object> reponse=new HashMap<String, Object>();
		String result="";
		String code=StringUtil.transformString(param.get("code"));
		String name=StringUtil.transformString(param.get("name"));
		String password="";
		String actionUrl=url;
		if(StringUtil.isEmpty(code))
		{
			reponse.put("code", "-1");
			reponse.put("msg","输入 的学籍号是空值!");
			reponse.put("url","");
			return reponse;
		}
		
		if(!StringUtil.isEmpty(code))  code=code.trim();
		if(!StringUtil.isEmpty(name))  name=name.trim();
		
		try {
			Map<String, Object> urlParam=new  HashMap<String, Object>();
			password=BcryptHashing.encrypt(code);
			code=Base64Util.encrypt(code);
			name=Base64Util.encrypt(name);
			urlParam.put("code",code);
			urlParam.put("name",name);
			//访问第三方接口，获取校验结果
		    result=callRemoteInterface.updateHttpRemoteInterface(url1,urlParam);
		} catch (Exception e) {
			e.printStackTrace();
			reponse.put("code", "-1");
			reponse.put("msg","编码错误!");
			reponse.put("url","");  
			return reponse;
		}
	
		
		if(!StringUtil.isEmpty(result))
		{
		  JSONObject jsonResult=(JSONObject) JSONObject.parse(result);
		  String resultCode=jsonResult==null?"":StringUtil.transformString(jsonResult.get("resultCode"));//结果代码
		  String remark=jsonResult==null?"":StringUtil.transformString(jsonResult.get("remark"));//结果说明
		  
		  if(resultCode.equals("0"))
		  {//0：验证通过;
			  HttpSession session=request.getSession();
			  String accountId=StringUtil.transformString(session.getAttribute("accountId"));//账号编号
			  
			//保存学生与学籍号的关系
			  param.put("accountId", accountId);
			  param.put("schoolId", getXxdm(request));
			  String code1=StringUtil.transformString(param.get("code"));
			  String name1=StringUtil.transformString(param.get("name"));
			  if(!StringUtil.isEmpty(code1))  code1=code1.trim();
			  if(!StringUtil.isEmpty(name1))  name1=name1.trim();
			  param.put("studentCode",code1);
			  param.put("studentName",name1);
			  int  num=evalutionService.addStudentReative(param);
			  
			  if(num<0)
			  {
				  reponse.put("code", "-1");
				  reponse.put("msg","保存数据失败");
				  reponse.put("url",""); 
				  
				  return reponse;
			  }
			  
		  }
		
		  
		  reponse.put("code", resultCode);
		  reponse.put("msg",remark);
		  reponse.put("url",actionUrl);  
		  reponse.put("pcode",code);
		  reponse.put("pname",name);
		  reponse.put("password",password);
		  return reponse;
		  
		}
		
		reponse.put("code",-1);
	    reponse.put("msg","绑定学籍号失败!");
	    reponse.put("url","");
		  
	    return reponse;
		
	}
}
