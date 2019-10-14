package com.talkweb.teacher.action;

import java.util.ArrayList;
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
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.teacher.domain.page.TTrBmfzr;
import com.talkweb.teacher.service.DepartmentStaffService;

/**
 * @version 2.0
 * @Description: 教师机构人员设置相关处理Action
 * @author 吴安辉
 * @date 2015年3月3日
 */

@Controller
@RequestMapping(value="/teacher/")
public class DepartmentStaffAction {

	@Autowired
	private DepartmentStaffService departmentStaffService;
	
	/**
	 * 添加机构人员
	 * @return
	 */
	@RequestMapping(value="addDepartmentStaff",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> addDepartmentStaff(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String bm = request.getParameter("jgh");
		String[] zgh = request.getParameterValues("zgh");
		String jglb = request.getParameter("jglb");
		String km = request.getParameter("km");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			String xxdm = school.getXxdm();
			data = departmentStaffService.addTeadept(bm, xxdm, zgh, jglb,km);
		}
		return data;
	}
	
	/**
	 * 删除机构人员
	 * @return
	 */
	@RequestMapping(value="delDepartmentStaff",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delDepartmentStaff(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String bm = request.getParameter("jgh");
		String[] zghs = request.getParameterValues("zgh");
		data = departmentStaffService.deleteTeadept(bm, zghs);
		return data;
	}
	
	/**
	 * 添加机构负责人
	 * @return
	 */
	@RequestMapping(value="addrDepartmentHeads",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> addrDepartmentHeads(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgh = request.getParameter("jgh");
		String zgh = request.getParameter("zgh");
		TTrBmfzr bmfzr = new TTrBmfzr();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			String xxdm = school.getXxdm();
			bmfzr.setFzrgh(zgh);
			bmfzr.setJgh(jgh);
			bmfzr.setXxdm(xxdm);
			data = departmentStaffService.addBmFzr(bmfzr);
		}
		return data;
	}

	/**
	 * 删除机构负责人
	 * @return
	 */
	@RequestMapping(value="delDepartmentHeads",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delDepartmentHeads(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgh = request.getParameter("jgh");
		String zgh = request.getParameter("zgh");
		TTrBmfzr bmfzr = new TTrBmfzr();
		bmfzr.setFzrgh(zgh);
		bmfzr.setJgh(jgh);
		data = departmentStaffService.deleteBmFzr(bmfzr);
		return data;
	}
	
	/**
	 * 查询已设置人员
	 * @return
	 */
	@RequestMapping(value="queryDepartmentStaff",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryDepartmentStaff(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String bmbh = request.getParameter("jgh");
		String jglb = request.getParameter("jglb");
		String isDeparent = request.getParameter("isDeparent");
		String xm = request.getParameter("zghxm");
		String yjkm = request.getParameter("km");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			Map<String,String> param = new HashMap<String,String>();
			String xxdm = school.getXxdm();
			param.put("xxdm", xxdm);
			param.put("jgh", bmbh);
			param.put("jglb", jglb);
			param.put("isDeparent", isDeparent);
			param.put("xm", xm);
			if(yjkm == null){
				param.put("yjkm", "");
			}else{
				param.put("yjkm", yjkm);
			}
			data = departmentStaffService.queryDepartmentStaff(param);
		}
		return data;
	}
	
	/**
	 * 查询备课组已设置人员
	 * @return
	 */
	@RequestMapping(value="queryLessonDepartmentStaff",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryLessonDepartmentStaff(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String bmbh = request.getParameter("jgh");
		String xm = request.getParameter("zghxm");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			String xxdm = school.getXxdm();
			data = departmentStaffService.queryLessonDepartmentStaff(bmbh, xxdm,xm);
		}
		System.out.println(JSON.toJSONString(data));
		return data;
	}
	
	/**
	 * 查询未设置人员
	 * @return
	 */
	@RequestMapping(value="queryDepartmentNoStaff",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryDepartmentNoStaff(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String bmbh = request.getParameter("jgh");
		String jglb = request.getParameter("jglb");
		String isDeparent = request.getParameter("isDeparent");
		String xm = request.getParameter("zghxm");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			String xxdm = school.getXxdm();
			Map<String,String> param = new HashMap<String,String>();
			param.put("xxdm", xxdm);
			param.put("jgh", bmbh);
			param.put("jglb", jglb);
			param.put("isDeparent", isDeparent);
			param.put("xm", xm);
			data = departmentStaffService.queryDepartmentNoStaff(param);
		}
		System.out.println(JSON.toJSONString(data));
		return data;
	}
	
	/**
	 * 查询研究科目列表
	 * @return
	 */
	@RequestMapping(value="querySubjectByCode",method=RequestMethod.POST)
	@ResponseBody
	public List<Map<String,Object>> querySubjectByCode(HttpServletRequest request){
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		String jgh = request.getParameter("jgh");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			data = departmentStaffService.getSubjectList(jgh);
		}
		System.out.println(JSON.toJSONString(data));
		return data;
	}
}
