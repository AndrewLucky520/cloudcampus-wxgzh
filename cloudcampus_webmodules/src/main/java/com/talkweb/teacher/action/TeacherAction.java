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

import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.teacher.domain.page.THrTeacher;
import com.talkweb.teacher.service.TeacherService;

/**
 * @version 2.0
 * @Description: 教师curd处理
 * @author 雷智
 * @date 2015年3月7日
 */
@Controller
public class TeacherAction {
	@Autowired
	private TeacherService TeacherServiceImpl;

	/**
	 * 按条件查询教师列表
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/teacher/queryTeacherList.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getTeacherList(HttpServletRequest request)
			throws Exception {
		String xmzgh = request.getParameter("xmzgh");
		String duty = request.getParameter("duty");
		if (duty.equals("")) {
			duty = null;
		}
		if (xmzgh.trim().length() == 0) {
			xmzgh = null;
		}
		Map<String, Object> limit = new HashMap<String, Object>();
		limit.put("xmzgh", xmzgh);
		limit.put("duty", duty);
		List<THrTeacher> teacherList = TeacherServiceImpl
				.queryTeacherByLimit(limit);

		Map<String, Object> datas = new HashMap<String, Object>();
		datas.put("total", teacherList.size());
		datas.put("rows", teacherList);
		return datas;
	}

	/**
	 * 增加教师信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/teacher/addTeacher.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> addTeacher(HttpServletRequest request) {
		THrTeacher teacher = new THrTeacher();
		TTrSchool school = (TTrSchool) request.getSession().getAttribute("school");
		teacher.setXxdm(school.getXxdm());
		System.out.println(teacher.getCsrq() + "hjkhjk");
		String xxdm = request.getParameter("xxdm");
		teacher.setXxdm(xxdm);
		teacher.setZgh(UUIDUtil.getUUID());
		System.out.println(teacher.getCsrq());
		String jsdm = request.getParameter("jsdm");
		teacher.setJsdm(jsdm);
		String xm = request.getParameter("xm");
		teacher.setXm(xm);
		String xmpy = request.getParameter("xmpy");
		teacher.setXmpy(xmpy);
		String cym = request.getParameter("cym");
		teacher.setCym(cym);
		String xbm = request.getParameter("xbm");
		teacher.setXbm(xbm);
		String csrq = request.getParameter("csrq");
		if (csrq.equals("")) {
			teacher.setCsrq(null);
		} else {
			teacher.setCsrq(csrq);
		}
		String sfzh = request.getParameter("sfzh");
		teacher.setSfzh(sfzh);
		String hyxkm = request.getParameter("hyxkm");
		teacher.setHyxkm(hyxkm);
		String mzm = request.getParameter("mzm");
		teacher.setMzm(mzm);
		String jgm = request.getParameter("jgm");
		teacher.setJgm(jgm);
		String jtcsm = request.getParameter("jtcsm");
		teacher.setJtcsm(jtcsm);
		String brcfm = request.getParameter("brcfm");
		teacher.setBrcfm(brcfm);
		String gzny = request.getParameter("gzny");
		if (gzny.equals("")) {
			teacher.setGzny(null);
		} else {
			teacher.setGzny(gzny);
		}
		String cjny = request.getParameter("cjny");
		if (cjny.equals("")) {
			teacher.setCjny(null);
		} else {
			teacher.setCjny(cjny);
		}
		String rxny = request.getParameter("rxny");
		if (rxny.equals("")) {
			teacher.setRxny(null);
		} else {
			teacher.setRxny(rxny);
		}
		String bzlbm = request.getParameter("bzlbm");
		teacher.setBzlbm(bzlbm);
		String gwzym = request.getParameter("gwzym");
		teacher.setGwzym(gwzym);
		String lxdh = request.getParameter("lxdh");
		teacher.setLxdh(lxdh);
		String txdz = request.getParameter("txdz");
		teacher.setTxdz(txdz);
		String yzbm = request.getParameter("yzbm");
		teacher.setYzbm(yzbm);
		String dzxx = request.getParameter("dzxx");
		teacher.setDzxx(dzxx);
		String zp = request.getParameter("zp");
		teacher.setZp(zp);
		String gwzt = request.getParameter("gwzt");
		teacher.setGwzt(gwzt);
		String xlm = request.getParameter("xlm");
		teacher.setXlm(xlm);
		String xwm = request.getParameter("xwm");
		teacher.setXwm(xwm);
		String zzmmm = request.getParameter("zzmmm");
		teacher.setZzmmm(zzmmm);
		String srkm = request.getParameter("srkm");
		teacher.setSrkm(srkm);
		String zw = request.getParameter("zw");
		teacher.setZw(zw);
		String zc = request.getParameter("zc");
		teacher.setZc(zc);
		Map<String, Object> resultinfo = new HashMap<String, Object>();
		resultinfo = TeacherServiceImpl.addTeacher(teacher);
		return resultinfo;
	}

	/**
	 * 修改教师信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/teacher/updateTeacher.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> updateTeacher(HttpServletRequest request) {
		THrTeacher teacher = new THrTeacher();
		String xxdm = request.getParameter("xxdm");
		teacher.setXxdm(xxdm);
		String zgh = request.getParameter("zgh");
		teacher.setZgh(zgh);
		String jsdm = request.getParameter("jsdm");
		teacher.setJsdm(jsdm);
		String xm = request.getParameter("xm");
		teacher.setXm(xm);
		String cym = request.getParameter("cym");
		teacher.setCym(cym);
		String xmpy = request.getParameter("xmpy");
		teacher.setXmpy(xmpy);
		String xbm = request.getParameter("xbm");
		teacher.setXbm(xbm);
		String csrq = request.getParameter("csrq");
		teacher.setCsrq(csrq);
		String sfzh = request.getParameter("sfzh");
		teacher.setSfzh(sfzh);
		String hyxkm = request.getParameter("hyxkm");
		teacher.setHyxkm(hyxkm);
		String mzm = request.getParameter("mzm");
		teacher.setMzm(mzm);
		String jgm = request.getParameter("jgm");
		teacher.setJgm(jgm);
		String jtcsm = request.getParameter("jtcsm");
		teacher.setJtcsm(jtcsm);
		String brcfm = request.getParameter("brcfm");
		teacher.setBrcfm(brcfm);
		String gzny = request.getParameter("gzny");
		teacher.setGzny(gzny);
		String cjny = request.getParameter("cjny");
		teacher.setCjny(cjny);
		String rxny = request.getParameter("rxny");
		teacher.setRxny(rxny);
		String bzlbm = request.getParameter("bzlbm");
		teacher.setBzlbm(bzlbm);
		String gwzym = request.getParameter("gwzym");
		teacher.setGwzym(gwzym);
		String lxdh = request.getParameter("lxdh");
		teacher.setLxdh(lxdh);
		String txdz = request.getParameter("txdz");
		teacher.setTxdz(txdz);
		String yzbm = request.getParameter("yzbm");
		teacher.setYzbm(yzbm);
		String dzxx = request.getParameter("dzxx");
		teacher.setDzxx(dzxx);
		String zp = request.getParameter("zp");
		teacher.setZp(zp);
		String gwzt = request.getParameter("gwzt");
		teacher.setGwzt(gwzt);
		String xlm = request.getParameter("xlm");
		teacher.setXlm(xlm);
		String xwm = request.getParameter("xwm");
		teacher.setXwm(xwm);
		String zzmmm = request.getParameter("zzmmm");
		teacher.setZzmmm(zzmmm);
		String srkm = request.getParameter("srkm");
		teacher.setSrkm(srkm);
		String zw = request.getParameter("zw");
		teacher.setZw(zw);
		String zc = request.getParameter("zc");
		teacher.setZc(zc);
		Map<String, Object> resultinfo = new HashMap<String, Object>();
		resultinfo = TeacherServiceImpl.updateTeacher(teacher);
		return resultinfo;
	}

	/**
	 * 根据id查询教师信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/teacher/queryTeacher.do", method = RequestMethod.POST)
	@ResponseBody
	public THrTeacher queryTeacher(HttpServletRequest request) {
		String zgh = request.getParameter("zgh");
		THrTeacher teacher = TeacherServiceImpl.queryTeacherById(zgh);
		return teacher;
	}

	/**
	 * 根据id字符集删除教师信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/teacher/delTeacher.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> deleteTeacherByIdList(HttpServletRequest request) {
		Map<String, Object> resultinfo = new HashMap<String, Object>();
		List<String> IdList = new ArrayList<String>();
		String[] arr = request.getParameterValues("zgh");
		for (int i = 0; i < arr.length; i++) {
			IdList.add(arr[i]);
		}
		resultinfo = TeacherServiceImpl.deleteTeacherByIdList(IdList);
		return resultinfo;
	}
}
