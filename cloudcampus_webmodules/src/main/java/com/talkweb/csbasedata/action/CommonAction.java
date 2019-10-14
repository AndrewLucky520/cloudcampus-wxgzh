package com.talkweb.csbasedata.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.service.CommonManageService;

@Controller
@RequestMapping(value = "/common/")
public class CommonAction extends BaseAction{
	
	@Autowired
	private CommonManageService commonManageService;
		
	/** -----查询年级信息列表----- **/
	@RequestMapping(value = "getGradeFromSchool", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGradeFromSchool(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();		
		String xnxq = this.getCurXnxq(req);
		if (StringUtils.isNotEmpty(xnxq) && xnxq.length()>=4)
		{
			param.put("schoolId", getXxdm(req));
			param.put("xn", xnxq.substring(0, 4));
			List<JSONObject> gradeList = commonManageService.getGradeFromSchool(param);
			if (gradeList.size() > 0) {
				response.put("data", gradeList);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryEmpty.getCode(),
						OutputMessage.queryEmpty.getDesc());
			}			
		}else{
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
	    return response;
	}
	/** -----查询年级level信息列表----- **/
	@RequestMapping(value = "getCurrentLevelFromSchool", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCurrentLevelFromSchool(HttpServletRequest req,
			HttpServletResponse res,@RequestBody JSONObject reqObj) {
		JSONObject response = new JSONObject();		
		String xnxq = this.getCurXnxq(req);
		if (StringUtils.isNotEmpty(xnxq) && xnxq.length()>=4)
		{
			String isAll = reqObj.getString("isAll");
			JSONObject param = new JSONObject();
			param.put("schoolId", getXxdm(req));
			param.put("xn", xnxq.substring(0, 4));
			param.put("isAll", isAll);
			List<JSONObject> gradeList = commonManageService.getCurrentLevelFromSchool(param);
			if (gradeList.size() > 0) {
				response.put("data", gradeList);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryEmpty.getCode(),
						OutputMessage.queryEmpty.getDesc());
			}			
		}else{
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}	
	    return response;
	}
	/** -----获取年级下的班级----- **/
	@RequestMapping(value = "getClassFromGrade", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassFromGrade(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String gradeId = param.getString("gradeId");
		if (StringUtils.isNotEmpty(gradeId))
		{
			param.put("schoolId", getXxdm(req));
			List<JSONObject> classList = commonManageService
					.getClassFromGrade(param);
			if (classList.size() > 0) {
				response.put("data", classList);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, 
						OutputMessage.queryEmpty.getCode(),
						OutputMessage.queryEmpty.getDesc());
			}
		}else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}	
	    return response;
	}
	
	/** -----获取学校下的科目----- **/
	@RequestMapping(value = "getLessonFromSchool", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLessonFromSchool(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		param.put("schoolId", getXxdm(req));
		List<JSONObject> lessonList = commonManageService.getLessonFromSchool(param);
		if (lessonList.size() > 0) {
			response.put("data", lessonList);
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.queryEmpty.getCode(),
					OutputMessage.queryEmpty.getDesc());
		}
	    return response;
	}
	
	/** -----根据教师姓名查询教师信息----- **/
	@RequestMapping(value = "getTeacherByName", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherByName(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		param.put("schoolId", getXxdm(req));
		List<JSONObject> teacherList = commonManageService
				.getTeacherByName(param);
		if (teacherList.size() >= 0) {
			response.put("data", teacherList);
			setPromptMessage(response,
					OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} /*else {
			setPromptMessage(response, 
					OutputMessage.queryEmpty.getCode(),
					OutputMessage.queryEmpty.getDesc());
		}	*/		
	    return response;
	}
	/** -----根据教师姓名查询教师信息----- **/
	@RequestMapping(value = "getTeacherAccountByName", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherAccountByName(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		param.put("schoolId", getXxdm(req));
		List<JSONObject> teacherList = commonManageService
				.getTeacherAccountByName(param);
		if (teacherList.size() > 0) {
			response.put("data", teacherList);
			setPromptMessage(response,
					OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, 
					OutputMessage.queryEmpty.getCode(),
					OutputMessage.queryEmpty.getDesc());
		}			
	    return response;
	}
    /**
     * 公共下载HTML表格方法使用示例
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/excel/exportExcelByData")
    @ResponseBody  
    public void demoForDownExcelByHTMLTable(HttpServletRequest req,HttpServletResponse res) throws Exception{      
        /**excelHead 前台传过来的datagrid表头
         * 示例3 用于合并表头
        String datas = req.getParameter("data");
        System.out.println("前台参数："+datas);
        JSONArray excelHead = JSON.parseArray("[[{field:'itemid',title:'Item ID',rowspan:3,width:80,sortable:true},{field:'productid',title:'Product ID',rowspan:2,colspan:2,width:80,sortable:true},{title:'Item Details',colspan:6}],"
                + "[{field:'listprice',title:'List Price',colspan:2,width:80,align:'right',sortable:true},{field:'unitcost',colspan:2,title:'Unit Cost',width:80,align:'right',sortable:true},{field:'attr1',title:'Attribute',width:100},{field:'status',title:'Status',width:60}]"
                + ",["
                + "{field:'star3',title:'star3',width:80,align:'right',sortable:true},"
                + "{field:'start3',title:'start3',width:80,align:'right',sortable:true},"
                + "{field:'listprice1',title:'listprice1',width:100},"
                + "{field:'listp2',title:'listp2',width:100},"
                + "{field:'u1',title:'u1',width:100},"
                + "{field:'u2',title:'u2',width:100},"
                + "{field:'a1',title:'a1',width:100},"
                + "{field:'s2',title:'s2',width:100}"
                + "]"
                + "]");
        //封装数据 json格式 键名需与表头中的field字段一致
        JSONArray data = new JSONArray();
        JSONObject obj = new JSONObject();
        data.add(obj);
        obj.put("itemid", "abc");
        obj = new JSONObject();
        data.add(obj);
        obj.put("itemid", "abc");
        obj = new JSONObject();
        data.add(obj);
        String[] needMerg = {"itemid"};
        ExcelTool.exportExcelWithData(data , excelHead,"导出文件", needMerg, req, res);
        *  
        */
        JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
        JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));
        String fileName  = req.getParameter("fileName");
        ExcelTool.exportExcelWithData(excelData , excelHeads,fileName, null, req, res);
    }
	
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
}