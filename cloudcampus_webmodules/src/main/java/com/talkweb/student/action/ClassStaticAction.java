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

import com.talkweb.student.service.ClassStatic;
import com.talkweb.systemManager.domain.business.TTrSchool;

/**
 * @Version 2.0
 * @Description 班级信息维护
 * @author 雷智
 * @Data 2015-03-13
 */
@Controller
public class ClassStaticAction {
	@Autowired
	private ClassStatic classStatic;

	@RequestMapping(value = "/student/queryStudentStatistics.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getClassStatic(HttpServletRequest request) {
		//获取传入参数map
		Map<String, Object> param = new HashMap<String, Object>();
		//返回数据map
		Map<String, Object> result = new HashMap<String, Object>();
		//获取学校代码
		TTrSchool school = (TTrSchool) request.getSession().getAttribute("school");
		String xxdm = school.getXxdm();
		String xnxq = request.getParameter("xnxq");
//		String nj = request.getParameter("nj");
		String[] njStr = request.getParameter("nj").split(",");
		String nj="";
		String sql="";
		if(njStr.length==1){
			nj=njStr[0];
			sql=null;
		}
		else{
			for(int i=0;i<njStr.length;i++){
				sql+=" "+"or"+" "+"synj="+" "+njStr[i];
				nj=null;
			}
		}
		param.put("xxdm", xxdm);
		param.put("xnxq", xnxq);
		param.put("nj", nj);
		param.put("sql", sql);
		List<Map<String,Object>> res= new ArrayList<Map<String,Object>>();
		//执行查询操作
		res = classStatic.getStatic(param);
		result.put("total",res.size());
		result.put("rows", res);
		return result;
	}

}
