package com.talkweb.student.action;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.domain.TEdTerminfo;
import com.talkweb.student.domain.business.TSsStudent;
import com.talkweb.student.service.StudentManageService;
import com.talkweb.systemManager.domain.business.TTrSchool;


/**
 * 
 * @ClassName: StudentManageAction.java
 * @version:2.0
 * @Description: 学生信息维护管理Action
 * @author 吴安辉
 * @date 2015年3月9日
 */

@Controller
@RequestMapping(value="/student/")
public class StudentManageAction {

	@Autowired
	private StudentManageService studentService;
	
	/**
	 * 查询学生列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="queryStudentList",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject queryStudentList(HttpServletRequest request){
	    JSONObject data = new JSONObject();
		Map<String,Object> param = new HashMap<String,Object>();
		String xnxq = request.getParameter("xnxq");
		String synj = request.getParameter("synj");
		String bh = request.getParameter("bh");
		String xjhxm = request.getParameter("xjhxm");
		String sfzx = request.getParameter("sfzx");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		TEdTerminfo info = (TEdTerminfo)request.getSession().getAttribute("currentXNXQ");
		long d0 = (new Date()).getTime();
		if(school != null){
			param.put("xxdm", school.getXxdm());
			param.put("sfzx", sfzx);
			param.put("bh", bh);
			if(bh.indexOf(",") > 0){
				List<String> bhList = Arrays.asList(bh.split(","));
				param.put("bhList", bhList);
			}
			String xn = xnxq.substring(0, 4);
			String xqm = xnxq.substring(4);
			param.put("xn", xn);
			param.put("xqm", xqm);
			param.put("synj", synj);
			if(synj.indexOf(",") > 0){
				List<String> njList = Arrays.asList(synj.split(","));
				param.put("njList", njList);
			}
			param.put("xm", xjhxm);
			data = studentService.getStudentList(param,info.getXn());
		}
		long d1 = (new Date()).getTime();
		System.out.println("查询学生列表耗时："+(d1-d0));
		return data;
	}
	
	
	/**
	 * 查询单个学生信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="queryOneStudent",method=RequestMethod.POST)
	@ResponseBody
	public TSsStudent queryOneStudent(HttpServletRequest request){
		String xh = request.getParameter("xh");
		TSsStudent data = studentService.getStudentById(xh);
		return data;
	}
	
	/**
	 * 修改学生信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="updateStudent",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> updateStudent(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		//学生编号（主键）
		String xh = request.getParameter("xh");
		//学号
		String xjh = request.getParameter("xjh");
		//学籍号
		String userXh = request.getParameter("user_xh");
		//姓名
		String xm = request.getParameter("xm");
		//身份证号
		String sfzh = request.getParameter("sfzh");
		//出生日期
		String csrq = request.getParameter("csrq");
		//性别码
		String xbm = request.getParameter("xbm");
		//入学年级
		String nj = request.getParameter("nj");
		//所属班级
		String bh = request.getParameter("bh");
		//父亲联系电话
		String fqlxdh = request.getParameter("fqlxdh");
		//母亲联系电话
		String mqlxdh = request.getParameter("mqlxdh");
		//在校状态
		String sfzx = request.getParameter("sfzx");
		TSsStudent student = new TSsStudent();
		TEdTerminfo info = (TEdTerminfo)request.getSession().getAttribute("currentXNXQ");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(info != null && school!= null){
			student.setXxdm(school.getXxdm());
			student.setXh(xh);
			student.setXjh(xjh);
			student.setUserXh(userXh);
			student.setXm(xm);
			student.setSfzh(sfzh);
			if(csrq.equals("")){
				student.setCsrq(null);
			}else{
				student.setCsrq(csrq);
			}
			student.setXbm(xbm);
			student.setNj(nj);
			student.setBh(bh);
			student.setFqlxdh(fqlxdh);
			student.setMqlxdh(mqlxdh);
			student.setSfzx(sfzx);
			String xn = info.getXn();
			//String xqm = info.getXqm();
			data = studentService.updateStudent(student, xn);
		}
		System.out.println(JSON.toJSONString(data));
		return data;
	}
	
	/**
	 * 添加学生信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="addStudent",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> addStudent(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String xjh = request.getParameter("xjh");
		String userXh = request.getParameter("user_xh");
		String xm = request.getParameter("xm");
		String sfzh = request.getParameter("sfzh");
		String csrq = request.getParameter("csrq");
		String xbm = request.getParameter("xbm");
		String nj = request.getParameter("nj");
		String bh = request.getParameter("bh");
		String fqlxdh = request.getParameter("fqlxdh");
		String mqlxdh = request.getParameter("mqlxdh");
		//String sfzx = request.getParameter("sfzx");
		TSsStudent student = new TSsStudent();
		TEdTerminfo info = (TEdTerminfo)request.getSession().getAttribute("currentXNXQ");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(info != null && school!= null){
			student.setXxdm(school.getXxdm());
			student.setXh(UUIDUtil.getUUID());
			student.setParentsysid(UUIDUtil.getUUID());
			student.setXjh(xjh);
			student.setUserXh(userXh);
			student.setXm(xm);
			student.setSfzh(sfzh);
			if(csrq.equals("")){
				student.setCsrq(null);
			}else{
				student.setCsrq(csrq);
			}
			student.setXbm(xbm);
			student.setNj(nj);
			student.setBh(bh);
			student.setFqlxdh(fqlxdh);
			student.setMqlxdh(mqlxdh);
			student.setSfzx("1");
			String xn = info.getXn();
			String xqm = info.getXqm();
			data = studentService.addStudent(student, xn, xqm);
		}
		return data;
	}
	
	/**
	 * 删除学生信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="delStudent",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delStudent(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String[] xhs = request.getParameterValues("xh");
		data = studentService.deleteStudent(xhs);
		return data;
	}
}
