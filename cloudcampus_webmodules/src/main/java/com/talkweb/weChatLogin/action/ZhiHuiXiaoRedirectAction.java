package com.talkweb.weChatLogin.action;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.weChatLogin.service.ZhiHuiXiaoRedirectService;

@RequestMapping("/zhx/redirect")
@Controller
public class ZhiHuiXiaoRedirectAction extends BaseAction{

	
	private static final Logger logger = LoggerFactory
			.getLogger(ZhiHuiXiaoRedirectAction.class);
	
	@Autowired
	private ZhiHuiXiaoRedirectService zhiHuiXiaoRedirectService;
	/**
	 * 登陆,根据模块id跳转
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/authredirect")
	public ModelAndView authredirect(HttpServletRequest request,HttpServletResponse response){
			
			try{
				PrintWriter out = null;
				out=response.getWriter();
				// 通过getWriter方法获取PrintWriter对象
					response.setContentType("text/html;charset=utf-8");
					out = response.getWriter();
				
				  HttpSession sess = request.getSession();
				  
				  String xxdm=(String) sess.getAttribute("xxdm");
				  
				  User u=(User) sess.getAttribute("user");
				  
				  String phone= u.getAccountPart()!=null? u.getAccountPart().getMobilePhone():"";
				  
				  HashMap<String, Object> param=new HashMap<String,Object>();
				  
				  param.put("schoolId", xxdm);
				  
				  param.put("telephone", phone);
				  
				  if(phone==null||phone.trim().isEmpty()){
					  this.printErrorPage(out,"该帐号还未开通短信中心，请联系管理员添加！");
				  }else{
					  HashMap<String,Object> data=zhiHuiXiaoRedirectService.getUserBySDK(param, request);
					  if(data.get("code").toString().equals("0")){
						  String url=data.get("url").toString();
						  ModelAndView view = new ModelAndView();
						  view.setViewName("redirect:"+url);
						  return view;
					  }else   if(data.get("code").toString().equals("-1")){
						  this.printErrorPage(out,"该帐号还未开通短信中心，请联系管理员添加！");
					  }else   if(data.get("code").toString().equals("-999")){
						  this.printErrorPage(out,"没有权限");
					  }
				  }
			}catch(Exception e){
				e.printStackTrace();
			}
		  return null;
	}
	
	  private void printErrorPage(PrintWriter out,String message){
	    	out.flush();//清空缓存
	    	//输出script标签    	
	        out.write("<script language=\"JavaScript\" type=\"text/JavaScript\">");
	        //js语句：输出alert语句
	        out.write("alert('"+message+"');");
	        out.write("window.close();");
	        //输出网页回退语句
	        //out.println("history.back();");
	        out.write("</script>");//输出script结尾标签
	    }
}
