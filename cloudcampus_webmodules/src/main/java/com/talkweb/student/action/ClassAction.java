package com.talkweb.student.action;

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

import com.talkweb.common.tools.StringNumTool;
import com.talkweb.common.tools.TermUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.student.domain.business.TSsClass;
import com.talkweb.student.domain.business.TSsClassenrol;
import com.talkweb.student.service.ClassService;
import com.talkweb.student.service.StudentImportService;
import com.talkweb.systemManager.domain.business.TTrSchool;

/**
 * @Version 2.0
 * @Description 班级信息维护
 * @author 雷智
 * @Data 2015-03-13
 */
@Controller
public class ClassAction {
	@Autowired
	private ClassService classServiceImpl;
	@Autowired
	private StudentImportService studentService;
	@Autowired
	private AllCommonDataService commonDataService;

	/**
	 * 按条件查询班级列表
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/student/queryClassList.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getClassList(HttpServletRequest request) {
		Map<String, Object> limit = new HashMap<String, Object>();
		String nj = request.getParameter("xnxq");
		String bjmc = request.getParameter("bjmc");
		String[] synj = request.getParameter("synj").split(",");
		String sql = "";
		if(synj.length==1){
			sql=" "+"and"+" "+"a.synj="+synj[0];
		}
		for (int i = 0; i < synj.length; i++) {
			sql += " " + "or" + " " + "a.synj=" + synj[i];
		}
		limit.put("sql", sql);
		limit.put("bjmc", bjmc);
		limit.put("xnxq", nj);
		String xq = TermUtil.formatTerm(nj);
		List<Map<String, Object>> rows = classServiceImpl.queryClassList(limit);
		for (int i = 0; i < rows.size(); i++) {
			rows.get(i).put("xq", xq);
		}
		Map<String, Object> datas = new HashMap<String, Object>();
		datas.put("total", rows.size());
		datas.put("rows", rows);
		return datas;
	}

	/**
	 * 修改查询接口
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/student/queryClassOne.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getClassOne(HttpServletRequest request) {
		Map<String, Object> param = new HashMap<String, Object>();
		String bh = request.getParameter("bh");
		String xnxq = request.getParameter("xnxq");
		param.put("bh", bh);
		param.put("xnxq", xnxq);
		System.out.println(classServiceImpl.queryClassOne(param));
		return classServiceImpl.queryClassOne(param);
	}

	/**
	 * 新增接口
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/student/addClass.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> addClass(HttpServletRequest request)
			throws Exception {
		TSsClass sclass = new TSsClass();
		Map<String, Object> map = new HashMap<String, Object>();
		TTrSchool school = (TTrSchool) request.getSession().getAttribute(
				"school");
		map.put("xxdm", school.getXxdm());
		sclass.setBh(UUIDUtil.getUUID());
		map.put("bh", sclass.getBh());
		String xnxq = request.getParameter("xnxq");
		map.put("xnxq", xnxq);
		String synj = request.getParameter("nj");
		map.put("synj", synj);
		String bjlxm = request.getParameter("bjlxm");
		map.put("bjlxm", bjlxm);
		String bzrzgh = request.getParameter("bzrzgh");
		map.put("bzrzgh", bzrzgh);
		String bjmc = request.getParameter("bjmc");
		map.put("bjmc", bjmc);
		String xn = xnxq.substring(0, 4);
		String rxnj = commonDataService.ConvertSYNJ2RXND(synj, xn);
		map.put("nj", rxnj);
		String pycc = commonDataService.getPYCCBySYNJ(synj, xn);
		map.put("pycc", pycc);
		int xz = 3;
		if (pycc.equalsIgnoreCase("2") || pycc.equalsIgnoreCase("3")) {

		} else {
			xz = 6;
		}
		map.put("xz", xz + "");
		List<TSsClassenrol> needInsertClassEnrol = new ArrayList<TSsClassenrol>();
		for (int k = Integer.parseInt(rxnj); k <= Integer.parseInt(rxnj) + xz
				- 1; k++) {
			for (int l = 1; l <= 2; l++) {
				TSsClassenrol arg0 = new TSsClassenrol();
				arg0.setBh(sclass.getBh());
				arg0.setXxdm(school.getXxdm());
				arg0.setBjlxm("01");
				arg0.setSfcx("0");
				arg0.setXn(k + "");
				arg0.setXqm(l + "");
				arg0.setXxdm(sclass.getXxdm());
				arg0.setBzrzgh(bzrzgh);
				needInsertClassEnrol.add(arg0);
			}
		}
		if (needInsertClassEnrol.size() > 0) {

			try {
				studentService.insertClassEnrolList(needInsertClassEnrol);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return classServiceImpl.addClass(map);
	}

	/**
	 * 批量添加班级接口
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/student/addClassMany.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> addBatch(HttpServletRequest request) {
		List<TSsClass> bathClass = new ArrayList<TSsClass>();
		TTrSchool school = (TTrSchool) request.getSession().getAttribute(
				"school");
		String bjlxm = request.getParameter("bjlxm");
		String synj = request.getParameter("nj");
		String cjgs = request.getParameter("cjgs");
		String bh = request.getParameter("sbjmc");
		String xnxq = request.getParameter("xnxq");
		String xn = xnxq.substring(0, 4);
		String rxnj = commonDataService.ConvertSYNJ2RXND(synj, xn);
		String pycc = commonDataService.getPYCCBySYNJ(synj, xn);
		int m = Integer.parseInt(cjgs);
		int xz = 3;
		if (pycc.equalsIgnoreCase("2") || pycc.equalsIgnoreCase("3")) {

		} else {
			xz = 6;
		}
		List<TSsClassenrol> needInsertClassEnrol = new ArrayList<TSsClassenrol>();
		for (int i = 0; i < m; i++) {
			TSsClass bclass = new TSsClass();
			bclass.setBh(UUIDUtil.getUUID());
			bclass.setXxdm(school.getXxdm());
			bclass.setBjlxm(bjlxm);
			bclass.setXz(xz);
			bclass.setSynj(synj);
			bclass.setNj(rxnj);
			bclass.setPycc(pycc);
			bclass.setBjmc(bh);
			bathClass.add(bclass);
			bh = StringNumTool.getLastNumAdd1(bh);

			for (int k = Integer.parseInt(rxnj); k <= Integer.parseInt(rxnj)
					+ xz - 1; k++) {
				for (int l = 1; l <= 2; l++) {
					TSsClassenrol arg0 = new TSsClassenrol();
					arg0.setBh(bclass.getBh());
					arg0.setXxdm(school.getXxdm());
					arg0.setBjlxm("01");
					arg0.setSfcx("0");
					arg0.setXn(k + "");
					arg0.setXqm(l + "");
					arg0.setXxdm(bclass.getXxdm());
					arg0.setBzrzgh(bclass.getBzrzgh());
					needInsertClassEnrol.add(arg0);
				}
			}
		}
		if (needInsertClassEnrol.size() > 0) {

			try {
				studentService.insertClassEnrolList(needInsertClassEnrol);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return classServiceImpl.addBatchClass(bathClass);
	}

	/**
	 * 修改接口
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/student/updateClass.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> updateClass(HttpServletRequest request) {
		Map<String, Object> updatas = new HashMap<String, Object>();
		String bh = request.getParameter("bh");
		String xnxq = request.getParameter("xnxq");
		String bjmc = request.getParameter("bjmc");
		String bjlxm = request.getParameter("bjlxm");
		String bzrzgh = request.getParameter("bzrzgh");
		updatas.put("bh", bh);
		updatas.put("xnxq", xnxq);
		updatas.put("bjmc", bjmc);
		updatas.put("bjlxm", bjlxm);
		updatas.put("bzrzgh", bzrzgh);
		return classServiceImpl.updateClass(updatas);
	}

	/**
	 * 删除接口
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/student/delClass.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> delClass(HttpServletRequest request) {
		String[] rdels = request.getParameterValues("bh");
		List<String> dels = new ArrayList<String>();
		for (int i = 0; i < rdels.length; i++) {
			dels.add(rdels[i]);
		}
		return classServiceImpl.delClass(dels);
	}
}
