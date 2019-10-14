package com.talkweb.system.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.talkweb.system.domain.business.Notice;
import com.talkweb.system.domain.business.User;
import com.talkweb.system.domain.page.PLogin;
import com.talkweb.system.service.UserService;

@Controller
public class UserAction {

	@Autowired
	private UserService userServiceImpl;
	
	
	@RequestMapping(value="/getAllUsers",method=RequestMethod.GET)
	@ResponseBody	
	public List<User> getAllUsers(){
		
		List<User> userList = userServiceImpl.getAllUsers();
		return userList;
	}
	
	
	@RequestMapping(value="/login",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> Login(@RequestBody PLogin login,HttpServletRequest request){
		
		Map<String,Object> result = new HashMap<String, Object>();
		String code = "-1";
		if("123".equals(login.getUserName()) && "123".equals(login.getUserPwd())){
			code = "0";
		}
		result.put("code", code);
		result.put("token", request.getSession().getId());
		return result;
	
	}
	
	@RequestMapping(value="/getNotice",method=RequestMethod.GET)
	@ResponseBody
	public Map<String,Object> getNotice(){
		
		Map<String,Object> result = new HashMap<String, Object>();
		List<Notice> notices = new ArrayList<Notice>();
		Notice notice = new Notice(1,"开学啦","开学在汉语中解释有开设学校、启作学者、学期开始三个释义。但是在日常生活中用的比较多的是学期开始。现代汉语中开学通常是指学生们经过假期的休息之后，重新返回学校继续学习……","index.html");
		Notice notice2 = new Notice(2,"开学啦","开学在汉语中解释有开设学校、启作学者、学期开始三个释义。但是在日常生活中用的比较多的是学期开始。现代汉语中开学通常是指学生们经过假期的休息之后，重新返回学校继续学习……","index.html");
		Notice notice3 = new Notice(2,"开学啦","开学在汉语中解释有开设学校、启作学者、学期开始三个释义。但是在日常生活中用的比较多的是学期开始。现代汉语中开学通常是指学生们经过假期的休息之后，重新返回学校继续学习……","index.html");
		notices.add(notice);
		notices.add(notice2);
		notices.add(notice3);
		result.put("data", notices);
		return result;
	
	}
	
	
	
	
	
	
	
	
	
	
}
