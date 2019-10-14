package com.talkweb.teacher.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.alibaba.fastjson.JSONArray;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.teacher.service.TeacherClassService;

/**
 * @version 2.0
 * @Description: 任课教师设置查询列表
 * @author 雷智
 * @date 2015年3月24日
 */
@Controller
public class TeacherClassAction {
	@Autowired
	private TeacherClassService teacherClassService;

	@RequestMapping(value = "/teacher/queryClassroomTeachersList.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> queryClassTeacherList(HttpServletRequest request) {
		// 建立param,存放传入参数
		Map<String, Object> param = new HashMap<String, Object>();
		// 建立results,封装返回结果
		Map<String, Object> results = new HashMap<String, Object>();

		String xnxq = request.getParameter("xnxq");
		String bh1 = request.getParameter("bh").trim();
		String grade1 = request.getParameter("grad");
		String[] bh = null;
		String[] grade = null;
		if (bh1 != null && !bh1.equalsIgnoreCase("")) {
			bh = bh1.split(",");
		}
		if (grade1 != null && !grade1.equalsIgnoreCase("")) {
			grade = grade1.split(",");
		}
		param.put("xnxq", xnxq);
		param.put("bh", bh);
		param.put("grade", grade);
		// 获取查询结果
		List<Map<String, Object>> list = teacherClassService.getTeacherList(param);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).get("kmdm") != null
					&& !list.get(i).get("kmdm").toString().equalsIgnoreCase("")) {
				System.out.println(list.get(i).get("kmdm"));
				String km[] = list.get(i).get("kmdm").toString().split(";");
				for (int j = 0; j < km.length; j++) {
					String[] kmxx = km[j].split(":");
					list.get(i).put(kmxx[0], kmxx[1]);
				}
				list.get(i).remove("kmdm");
			}
		}
		results.put("total", list.size());
		results.put("rows", list);
		return results;
	}

	@RequestMapping(value = "/teacher/queryOneClassroomTeachersList.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> queryClassTeacherOne(HttpServletRequest request) {
		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> results = new HashMap<String, Object>();
		String xnxq = request.getParameter("xnxq");
		String bh = request.getParameter("bh");
		param.put("xnxq", xnxq);
		param.put("bh", bh);
		List<Map<String, Object>> list = teacherClassService
				.queryTeacherOne(param);
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				String zgh = list.get(i).get("zgh").toString() + ",";
				String xm = list.get(i).get("xm").toString() + ",";
				// 遍历查询结果，若存在KMDM相同的元素，则获取单个对象的zgh和xm，组合成zgh:1,1;xm:小灰灰,小张样式
				for (int j = i + 1; j < list.size(); j++) {
					if (list.get(i).get("mkdm").equals(list.get(j).get("mkdm"))) {
						zgh += list.get(j).get("zgh").toString() + ",";
						xm += list.get(j).get("xm").toString() + ",";
						list.remove(j);
						j--;
					}
				}
				zgh = zgh.substring(0, zgh.length() - 1);
				xm = xm.substring(0, xm.length() - 1);
				list.get(i).put("zgh", zgh);
				list.get(i).put("xm", xm);
			}
		}
		List<Map<String, Object>> Lessoninfo = teacherClassService
				.querylessoninfo();
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < Lessoninfo.size(); j++) {
				if (Lessoninfo.get(j).get("mkdm")
						.equals(list.get(i).get("mkdm"))) {
					Lessoninfo.remove(j);
					Lessoninfo.add(list.get(i));
				}
			}
		}
		// 对Lessoninfo进行排序
		Collections.sort(Lessoninfo, new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				return o1.get("mkdm").toString()
						.compareTo(o2.get("mkdm").toString());
			}
		});
		results.put("total", Lessoninfo.size());
		results.put("rows", Lessoninfo);
		return results;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/teacher/saveOneClassroomTeachersList.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> saveOne(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		// 获取学年学期
		String xnxq = request.getParameter("xnxq");
		String xn = null;
		String xqm = null;
		if (xnxq != null) {
			xn = xnxq.substring(0, 4);
			xqm = xnxq.substring(4, 5);
		}
		// 获取学校代码
		TTrSchool school = (TTrSchool) request.getSession().getAttribute(
				"school");
		String xxdm = school.getXxdm();
		// 获取班号
		String bh = request.getParameter("bh");
		String hiddenStr = request.getParameter("hiddenStr");
		JSONArray jsonArr = JSON.parseArray(hiddenStr);
		// 将获取到的json数组转成list
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < jsonArr.size(); i++) {
			list.add((Map<String, Object>) jsonArr.get(i));
		}
		// 删除待维护班级任课教师信息设置
		Map<String, Object> del = new HashMap<String, Object>();
		del.put("xxdm", xxdm);
		del.put("xnxq", xnxq);
		del.put("bh", bh);
		teacherClassService.saveDelete(del);
		// 组合传入参数
		// 获取含有教师信息的字段，封装传入参数，进行保存
		for (int j = 0; j < list.size(); j++) {
			if (list.get(j).get("zgh") != null
					&& !list.get(j).get("zgh").toString().equalsIgnoreCase("")) {
				boolean info = true;
				String[] zghArr = list.get(j).get("zgh").toString().split(",");
				for (int m = 0; m < zghArr.length; m++) {
					Map<String, Object> param = new HashMap<String, Object>();
					param.put("xxdm", xxdm);
					param.put("xn", xn);
					param.put("bh", bh);
					param.put("xqm", xqm);
					param.put("zgh", zghArr[m]);
					param.put("kmdm", list.get(j).get("mkdm"));
					info = teacherClassService.saveOneClassTeacher(param);
				}
				if (info) {
					result.put("code", OutputMessage.success.getCode());
					result.put("msg", OutputMessage.updateSuccess.getDesc());
				} else {
					result.put("code", OutputMessage.updateSuccess.getCode());
					result.put("msg", OutputMessage.updateSuccess.getDesc());
				}
			}
		}

		return result;
	}
}
