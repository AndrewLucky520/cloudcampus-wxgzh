package com.talkweb.teacher.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.domain.TEdTerminfo;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.teacher.domain.page.TTrBmxx;
import com.talkweb.teacher.service.DepartmentService;

/**
 * @version 2.0
 * @Description: 教师机构（科室）设置处理Action
 * @author 吴安辉
 * @date 2015年3月3日
 */

@Controller
@RequestMapping(value="/teacher/")
public class DepartmentAction {
	
	@Autowired
	private DepartmentService departmentService;

	/**
	 * 获取科室列表
	 * @return
	 */
	@RequestMapping(value="queryDepartmentList",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> getDepartmentList(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setXxdm(school.getXxdm());
			data = departmentService.getKsList(bmxx);
		}
		return data;
	}
	
	/**
	 * 查询单个科室
	 * @return
	 */
	@RequestMapping(value="queryOneDepartment",method=RequestMethod.POST)
	@ResponseBody
	public TTrBmxx queryOneDepartment(HttpServletRequest request){
		TTrBmxx data = null;
		TTrBmxx bmxx = new TTrBmxx();
		String jgh = request.getParameter("jgh");
		System.out.println(jgh);
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJglb("0");
			bmxx.setJgh(jgh);
			bmxx.setXxdm(school.getXxdm());
			data = departmentService.getOneKsInfo(bmxx);
		}
		return data;
	}
	
	/**
	 * 新增科室信息
	 * @return
	 */
	@RequestMapping(value="addDepartment",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> addDepartment(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgmc = request.getParameter("jgmc");
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJgmc(jgmc);
			bmxx.setJglb("0");
			bmxx.setXxdm(school.getXxdm());
			bmxx.setJgh(UUIDUtil.getUUID());
			data = departmentService.addKsInfo(bmxx);
		}
		return data;
	}
	
	/**
	 * 修改科室信息
	 * @return
	 */
	@RequestMapping(value="updateDepartment",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> updateDepartment(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgmc = request.getParameter("jgmc");
		String jgh = request.getParameter("jgh");
		TTrBmxx bmxx = new TTrBmxx();
		bmxx.setJgmc(jgmc);
		bmxx.setJgh(jgh);
		data = departmentService.updateKsInfo(bmxx);
		return data;
	}
	
	/**
	 * 删除科室
	 * @return
	 */
	@RequestMapping(value="delDepartment",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delDepartment(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String[] bmbh = request.getParameterValues("jgh");	
		data = departmentService.deleteKsInfo(bmbh);
		return data;
	}
	
	/**
	 * 获取年级组列表
	 * @return
	 */
	@RequestMapping(value="queryGradeList",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryGradeList(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setXxdm(school.getXxdm());
			data = departmentService.getGradeList(bmxx);
		}
		return data;
	}
	
	/**
	 * 新增年级组信息
	 * @return
	 */
	@RequestMapping(value="addGrade",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> addGrade(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgmc = request.getParameter("jgmc");
		String njdm = request.getParameter("njdm");
		TTrBmxx bmxx = new TTrBmxx();
		TEdTerminfo info = (TEdTerminfo)request.getSession().getAttribute("currentXNXQ");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJgmc(jgmc);
			bmxx.setJglb("1");
			bmxx.setXxdm(school.getXxdm());
			bmxx.setJgh(UUIDUtil.getUUID());
			data = departmentService.addGrade(bmxx, njdm,info.getXn());
		}
		return data;
	}
	

	/**
	 * 修改年级组信息
	 * @return
	 */
	@RequestMapping(value="updateGrade",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> updateGrade(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgh = request.getParameter("jgh");
		String jgmc = request.getParameter("jgmc");
		///String njdm = request.getParameter("njdm");
		TTrBmxx bmxx = new TTrBmxx();
		bmxx.setJgmc(jgmc);
		bmxx.setJglb("1");
		bmxx.setJgh(jgh);
		data = departmentService.updateGrade(bmxx);
		return data;
	}
	
	/**
	 * 查询单个年级组信息
	 * @return
	 */
	@RequestMapping(value="queryOneGrade",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryOneGrade(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgh = request.getParameter("jgh");
		TEdTerminfo info = (TEdTerminfo)request.getSession().getAttribute("currentXNXQ");
		TTrBmxx bmxx = new TTrBmxx();
		bmxx.setJglb("1");
		bmxx.setJgh(jgh);
		data = departmentService.getOneGrade(bmxx,info.getXn());
		return data;
	}
	
	/**
	 * 删除年级组信息
	 * @return
	 */
	@RequestMapping(value="delGrade",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delGrade(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String[] grades = request.getParameterValues("jgh");
		data = departmentService.deleteGrade(grades);
		return data;
	}
	
	/**
	 * 获取教研组列表
	 * @return
	 */
	@RequestMapping(value="queryResearchGroupList",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryResearchGroupList(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setXxdm(school.getXxdm());
			data = departmentService.getResearchGradeList(bmxx);
		}
		return data;
	}
	
	/**
	 * 新增教研组信息
	 * @return
	 */
	@RequestMapping(value="addResearchGroup",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> addResearchGroup(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgmc = request.getParameter("jgmc");
		String[] jyzdm = request.getParameterValues("jyzdm");
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJgmc(jgmc);
			bmxx.setJglb("2");
			bmxx.setXxdm(school.getXxdm());
			bmxx.setJgh(UUIDUtil.getUUID());
			data = departmentService.addResearchGrade(bmxx, jyzdm);
		}
		return data;
	}
	
	/**
	 * 修改教研组信息
	 * @return
	 */
	@RequestMapping(value="updateResearchGroup",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> updateResearchGroup(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgh = request.getParameter("jgh");
		String jgmc = request.getParameter("jgmc");
		String[] jyzdm = request.getParameterValues("jyzdm");
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJgmc(jgmc);
			bmxx.setJglb("2");
			bmxx.setXxdm(school.getXxdm());
			bmxx.setJgh(jgh);
			data = departmentService.updateResearchGrade(bmxx, jyzdm);
		}
		return data;
	}
	
	
	/**
	 * 查询单个教研组信息
	 * @return
	 */
	@RequestMapping(value="queryOneResearchGroup",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryOneResearchGroup(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgh = request.getParameter("jgh");
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJglb("2");
			bmxx.setXxdm(school.getXxdm());
			bmxx.setJgh(jgh);
			data = departmentService.getOneResearchGrade(bmxx);
		}
		return data;
	}
	
	/**
	 * 删除教研组信息
	 * @return
	 */
	@RequestMapping(value="delResearchGroup",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delResearchGroup(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String[] kmdms = request.getParameterValues("jgh");
		data = departmentService.deleteResearchGrade(kmdms);
		return data;
	}
	
	/**
	 * 查询备课组信息列表
	 * @return
	 */
	@RequestMapping(value="queryLessonPlanningGroupList",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryLessonPlanningGroupList(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJglb("3");
			bmxx.setXxdm(school.getXxdm());
			data = departmentService.getLessonPlanningGroupList(bmxx);
		}
		return data;
	}
	
	/**
	 * 新增备课组信息
	 * @return
	 */
	@RequestMapping(value="addLessonPlanningGroup",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> addLessonPlanningGroup(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgmc = request.getParameter("jgmc");
		String[] jyzdm = request.getParameterValues("jyzdm");
		String[] njdm = request.getParameterValues("njdm");
		TTrBmxx bmxx = new TTrBmxx();
		TEdTerminfo info = (TEdTerminfo)request.getSession().getAttribute("currentXNXQ");
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJglb("3");
			bmxx.setJgmc(jgmc);
			bmxx.setJgh(UUIDUtil.getUUID());
			bmxx.setXxdm(school.getXxdm());
			data = departmentService.addLessonPlanningGroup(bmxx, jyzdm, njdm,info.getXn());
		}
		return data;
	}
	
	/**
	 * 修改备课组信息
	 * @return
	 */
	@RequestMapping(value="updateLessonPlanningGroup",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> updateLessonPlanningGroup(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgh = request.getParameter("jgh");
		String jgmc = request.getParameter("jgmc");
		String[] jyzdm = request.getParameterValues("jyzdm");
		String[] njdm = request.getParameterValues("njdm");
		TEdTerminfo info = (TEdTerminfo)request.getSession().getAttribute("currentXNXQ");
		TTrBmxx bmxx = new TTrBmxx();
		TTrSchool school = (TTrSchool)request.getSession().getAttribute("school");
		if(school != null){
			bmxx.setJglb("3");
			bmxx.setJgmc(jgmc);
			bmxx.setJgh(jgh);
			bmxx.setXxdm(school.getXxdm());
			
			data = departmentService.updateLessonPlanningGroup(bmxx, jyzdm, njdm,info.getXn());
		}
		return data;
	}
	
	/**
	 * 查询单个备课组信息
	 * @return
	 */
	@RequestMapping(value="queryOneLessonPlanningGroup",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> queryOneLessonPlanningGroup(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String jgh = request.getParameter("jgh");
		TEdTerminfo info = (TEdTerminfo)request.getSession().getAttribute("currentXNXQ");
		TTrBmxx bmxx = new TTrBmxx();
		bmxx.setJglb("3");
		bmxx.setJgh(jgh);
		data = departmentService.getOneLessonPlanningGroup(bmxx,info.getXn());
		return data;
	}
	
	/**
	 * 删除备课组信息
	 * @return
	 */
	@RequestMapping(value="delLessonPlanningGroup",method=RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delLessonPlanningGroup(HttpServletRequest request){
		Map<String,Object> data = new HashMap<String,Object>();
		String[] jghs = request.getParameterValues("jgh");
		data = departmentService.deleteLessonPlanningGroup(jghs);
		return data;
	}
	
}
