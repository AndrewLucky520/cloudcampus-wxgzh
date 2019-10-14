package com.talkweb.scheduleJasperReport.action;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.jasperreports.JasperReportsUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.schedule.service.ScheduleLookUpService;
import com.talkweb.scheduleJasperReport.bean.Timetable;
import com.talkweb.scheduleJasperReport.service.impl.newTimetablePrinterServiceImpl;
import com.talkweb.scheduleJasperReport.util.ExcelExportTool;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * @author XFQ
 */
@Controller
@RequestMapping("/schedule/tablereport/")
public class newTimetableReportAction extends BaseAction {
	Logger logger = LoggerFactory.getLogger(newTimetableReportAction.class);
	@Autowired
	private ScheduleLookUpService scheduleLookUpService;
	
	@Autowired
	private newTimetablePrinterServiceImpl printService;
	
	private static HashMap<Integer,String> label;
	
	private static HashMap<String,String> Folder;
	
	// 01：年级课表，02：教师课表，03：班级课表,04:学生课表，05：教室课表
	static{
		label = new HashMap<Integer,String>();
		label.put(56, "fivsix");
		label.put(57, "fivsen");
		label.put(58, "fiveig");
		label.put(59, "fivnin");
		label.put(67, "sixsen");
		label.put(68, "sixeig");
		label.put(69, "sixnin");
		label.put(77, "sevsen");
		label.put(78, "seveig");
		label.put(79, "sevnin");
		label.put(01, "grade");
		label.put(02, "teacher");
		label.put(03, "class");
		label.put(04, "student");
		label.put(05, "classroom");
	}
	
	static{
		Folder = new HashMap<String,String>();
		Folder.put("01", "grade");
		Folder.put("02", "teacher");
		Folder.put("03", "class");
		Folder.put("04", "student");
		Folder.put("05", "classroom");
	}
	
	private String getLabelByNum(int number){
		String prefix = "";
		if (label.containsKey(number)){
			prefix = label.get(number);
		}
		return prefix;
	}
	
	private String getFolderByNum(String number){
		String prefix = "";
		if (Folder.containsKey(number)){
			prefix = Folder.get(number);
		}
		return prefix;
	}
	private JSONObject getPrintSet(String scheduleId,String schoolId,String type,String printStyle){
		JSONObject param = new JSONObject();
		
		param.put("scheduleId", scheduleId);
		
		param.put("schoolId", schoolId);
		
		param.put("type", type);
		
		return scheduleLookUpService.getNewTimetablePrintSet(param);
	}
	
	/*
	 * 课表打印
	 */
	@RequestMapping(value = "PrintReport")
	@ResponseBody
	public JSONObject PrintReport(HttpServletRequest req,
			HttpServletResponse res) throws UnsupportedEncodingException {
		String printStyle = "01";//默认一页一表
		req.setCharacterEncoding("UTF-8");
	    String type = req.getParameter("type"); //01：年级课表，02：教师课表，03：班级课表,04:学生课表，05：教室课表
		String xxdm = getXxdm(req);
		String isShow = "";
		String name= URLDecoder.decode(req.getParameter("name"),"UTF-8");
		String scheduleId = req.getParameter("scheduleId");
		JSONObject setInfo = getPrintSet(scheduleId,xxdm,type,printStyle);
		if ("1".equals(req.getParameter("isShowTeacher"))&&!"1".equals(req.getParameter("isShowClass"))){
			isShow = "-showTeacher";
		}
		else if ("1".equals(req.getParameter("isShowClass"))&&!"1".equals(req.getParameter("isShowTeacher"))){
			isShow = "-showClass";
		}
		else if("1".equals(req.getParameter("isShowClass"))&&"1".equals(req.getParameter("isShowTeacher"))){
			isShow="-all";
		}
		// 报表数据源
		JSONObject result = new JSONObject();
		try {
			// 填充报表
			@SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("schedulereport");

			if (data != null) {
				JasperReport jasperReport = null;
				JRDataSource jrDataSource=null;
				int totalMaxDays = 0;
				int totalMaxLessons = 0;
				int totalMaxD = Integer.valueOf(data.get(0).getString(
						"totalMaxDays"));
				int totalMaxL = Integer.valueOf(data.get(0).getString(
						"totalMaxLessons"));							
				if (totalMaxD < 5){
					totalMaxDays = 5;
				}else if(totalMaxD > 7){
					totalMaxDays = 7;
				}else{
					totalMaxDays = totalMaxD;
				}	
				if (totalMaxL < 6){
					totalMaxLessons = 6;
				}else if(totalMaxL > 9){
					totalMaxLessons = 9;
				}else{
					totalMaxLessons = totalMaxL;
				}
				if(setInfo==null){
					setInfo=new JSONObject();
					setInfo.put("printStyle", printStyle);
					setInfo.put("bottomNote1", "");
					setInfo.put("bottomNote2", "");
					setInfo.put("bottomNote3", "");
					setInfo.put("title", name);
				}
				setInfo.put("totalMaxDays", totalMaxDays);
				setInfo.put("totalMaxLessons", totalMaxLessons);
				setInfo.put("isShow", isShow);
				int key = totalMaxDays * 10 + totalMaxLessons;
				String label = getLabelByNum(key);
				
				
				
				if (setInfo.getString("printStyle").equals("01")){
					//01：年级课表，02：教师课表，03：班级课表,04:学生课表，05：教室课表
					List<Timetable> datalist=new ArrayList<Timetable>();
					switch (type) {
					case "01":
						//datalist=printService.getClassList(data, setInfo);
						break;
					case "02":
						datalist=printService.getTeacherList(data, setInfo);
						break;
					case "03":
						datalist=printService.getClassList(data, setInfo);
						break;
					case "04":
						datalist=printService.getStudenList(data, setInfo);
						break;
					case "05":
						datalist=printService.getClassroomList(data, setInfo);
						break;
					}
					
					String file = req.getSession().getServletContext()
							.getRealPath("/")
							+ "newjasper"
							+ System.getProperty("file.separator")
							+  getFolderByNum(type) + isShow
							+ System.getProperty("file.separator")
							+ getFolderByNum(type) + label + ".jasper";
					jasperReport = (JasperReport) JRLoader
							.loadObjectFromFile(file);
					jrDataSource = new JRBeanCollectionDataSource(
							datalist);
				}else{
					//01：年级课表，02：教师课表，03：班级课表,04:学生课表，05：教室课表
					List<Timetable> datalist=new ArrayList<Timetable>();
					switch (type) {
					case "01":
						//datalist=printService.getClassList(data, setInfo);
						break;
					case "02":
						datalist=printService.getTwoTeacherList(data, setInfo);
						break;
					case "03":
						datalist=printService.getTwoClassList(data, setInfo);
						break;
					case "04":
						datalist=printService.getTwoStudentList(data, setInfo);
						break;
					case "05":
						datalist=printService.getTwoClassroomList(data, setInfo);
						break;
					}
					
					String file = req.getSession().getServletContext()
							.getRealPath("/")
							+ "newjasper"
							+ System.getProperty("file.separator")
							+ getFolderByNum(type) + isShow
							+ System.getProperty("file.separator")
							+ "two" +getFolderByNum(type)+ label + ".jasper";
					jasperReport = (JasperReport) JRLoader
							.loadObjectFromFile(file);
					jrDataSource = new JRBeanCollectionDataSource(
							datalist);
				}			
            	JasperPrint jasperPrint = JasperFillManager.fillReport(
						jasperReport, null, jrDataSource);
				OutputStream os = res.getOutputStream();
				JRPdfExporter exporter = new JRPdfExporter();
				res.reset();
				res.setHeader("Content-Disposition",
						"inline;filename=class.pdf");
				JasperReportsUtils.render(exporter, jasperPrint, os);
				os.flush();
				os.close();       
			} else {
				result.put("code", 1);
				result.put("msg", "没有可打印的数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * 课表导出
	 */
	@RequestMapping(value = "ExportReport")
	@ResponseBody
	public JSONObject ExportReport(HttpServletRequest req,
			HttpServletResponse res) throws UnsupportedEncodingException {
		String printStyle = "01";//默认一页一表
	    String type = req.getParameter("type"); // 01：年级课表，02：教师课表，03：班级课表,04:学生课表，05：教室课表
		String xxdm = getXxdm(req);
		String isShow = "";
		String scheduleId = req.getParameter("scheduleId");
		String name = URLDecoder.decode(req.getParameter("name"),"UTF-8");
		String viewType = req.getParameter("viewType"); //1固定上课，2走班上课   年级课表使用
		boolean isAllName =  StringUtil.transformString(req.getParameter("isAllName")).equals("1");
		String courseKey = "";
		JSONObject setInfo = getPrintSet(scheduleId,xxdm,type,printStyle);
		boolean isGrade=false;
		if(viewType!=null&&viewType.equals("2")){
			isGrade=true;
		}
		if ("1".equals(req.getParameter("isShowTeacher"))&&!"1".equals(req.getParameter("isShowClass"))){
			isShow = "-showTeacher";
		}
		else if ("1".equals(req.getParameter("isShowClass"))&&!"1".equals(req.getParameter("isShowTeacher"))){
			isShow = "-showClass";
		}
		else if("1".equals(req.getParameter("isShowClass"))&&"1".equals(req.getParameter("isShowTeacher"))){
			isShow="-all";
		}
		if (isAllName){
			courseKey = "courseName";
		}else{
			courseKey = "courseSimpleName";
		}
		// 报表数据源
		JSONObject result = new JSONObject();
		try {
			// 填充报表
			@SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("schedulereport");

			if (data != null) {
				int totalMaxDays = 0;
				int totalMaxLessons = 0;
				int totalMaxD=0;
				int totalMaxL=0;
				if(!type.equals("06")){
				totalMaxD = Integer.valueOf(data.get(0).getString(
						"totalMaxDays"));
				totalMaxL = Integer.valueOf(data.get(0).getString(
						"totalMaxLessons"));	
				}
				if (totalMaxD < 5){
					totalMaxDays = 5;
				}else if(totalMaxD > 7){
					totalMaxDays = 7;
				}else{
					totalMaxDays = totalMaxD;
				}	
				if (totalMaxL < 6){
					totalMaxLessons = 6;
				}else if(totalMaxL > 9){
					totalMaxLessons = 9;
				}else{
					totalMaxLessons = totalMaxL;
				}
				if(setInfo==null){
					setInfo=new JSONObject();
					setInfo.put("printStyle", printStyle);
					setInfo.put("bottomNote1", "");
					setInfo.put("bottomNote2", "");
					setInfo.put("bottomNote3", "");
					setInfo.put("title", name);
				}
				setInfo.put("totalMaxDays", totalMaxDays);
				setInfo.put("totalMaxLessons", totalMaxLessons);
				setInfo.put("isShow", isShow);
				setInfo.put("viewType", viewType);
				setInfo.put("courseKey", courseKey);
				
				
				List<JSONObject> exportList=new ArrayList<JSONObject>();
				logger.info("\n\n query type:"+ type);
				logger.info("\n\n data:"+data+"\n\n setInfo"+setInfo);
				switch (type) {//01：年级课表，02：教师课表，03：班级课表,04:学生课表，05：教室课表,06:学生分班
				case "01":
					exportList=printService.getGradeExportList(data, setInfo); 
					break;
				case "02":
					exportList=printService.getTeacherExportList(data, setInfo);
					isShow="-all";
					break;
				case "03":
					exportList=printService.getClassExportList(data,setInfo);
					break;
				case "04":
					exportList=printService.getStudentExportList(data, setInfo);
					isShow="-all";
					break;
				case "05":
					exportList=printService.getClassroomExportList(data, setInfo);
					isShow="-all";
					break;
				case "06":
					exportList=printService.getStudentPlacementExportList(data, setInfo);
					isShow="";
					isGrade=true;
					break;
				}
				req.setCharacterEncoding("UTF-8");
		        ExcelExportTool.exportMoreExcelWithData(exportList,name, req, res, true,isShow,isGrade);
			} else {
				result.put("code", 1);
				result.put("msg", "没有可打印的数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/*
	 * 班级课表打印
	 */
	@RequestMapping(value = "classReport")
	@ResponseBody
	public JSONObject classReport(HttpServletRequest req,
			HttpServletResponse res) {
		String printStyle = "01";//默认一页一表
	    String type = "01"; // 班级课表
		String xxdm = getXxdm(req);
		String isShow = "";
		String scheduleId = req.getParameter("scheduleId");
		JSONObject setInfo = getPrintSet(scheduleId,xxdm,type,printStyle);
		if ("1".equals(req.getParameter("isShowTeacher"))&&!"1".equals(req.getParameter("isShowClass"))){
			isShow = "-showTeacher";
		}
		else if ("1".equals(req.getParameter("isShowClass"))&&!"1".equals(req.getParameter("isShowTeacher"))){
			isShow = "-showClass";
		}
		else if("1".equals(req.getParameter("isShowClass"))&&"1".equals(req.getParameter("isShowTeacher"))){
			isShow="-all";
		}
		// 报表数据源
		JSONObject result = new JSONObject();
		try {
			// 填充报表
			@SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("schedulereport");

			if (data != null) {
				JasperReport jasperReport = null;
				JRDataSource jrDataSource=null;
				int totalMaxDays = 0;
				int totalMaxLessons = 0;
				int totalMaxD = Integer.valueOf(data.get(0).getString(
						"totalMaxDays"));
				int totalMaxL = Integer.valueOf(data.get(0).getString(
						"totalMaxLessons"));							
				if (totalMaxD < 5){
					totalMaxDays = 5;
				}else if(totalMaxD > 7){
					totalMaxDays = 7;
				}else{
					totalMaxDays = totalMaxD;
				}	
				if (totalMaxL < 6){
					totalMaxLessons = 6;
				}else if(totalMaxL > 9){
					totalMaxLessons = 9;
				}else{
					totalMaxLessons = totalMaxL;
				}
				setInfo.put("totalMaxDays", totalMaxDays);
				setInfo.put("totalMaxLessons", totalMaxLessons);
				int key = totalMaxDays * 10 + totalMaxLessons;
				String label = getLabelByNum(key);
				if (setInfo.getString("printStyle").equals("01")){
					String file = req.getSession().getServletContext()
							.getRealPath("/")
							+ "newjasper"
							+ System.getProperty("file.separator")
							+ "class" + isShow
							+ System.getProperty("file.separator")
							+ "class" + label + ".jasper";
					jasperReport = (JasperReport) JRLoader
							.loadObjectFromFile(file);
					jrDataSource = new JRBeanCollectionDataSource(
							printService.getClassList(data, setInfo));
				}else{
					String file = req.getSession().getServletContext()
							.getRealPath("/")
							+ "jasper"
							+ System.getProperty("file.separator")
							+ "class" + isShow
							+ System.getProperty("file.separator")
							+ "twoclass" + label + ".jasper";
					jasperReport = (JasperReport) JRLoader
							.loadObjectFromFile(file);
					jrDataSource = new JRBeanCollectionDataSource(
							printService.getTwoClassList(data, setInfo));
				}			
            	JasperPrint jasperPrint = JasperFillManager.fillReport(
						jasperReport, null, jrDataSource);
				OutputStream os = res.getOutputStream();
				JRPdfExporter exporter = new JRPdfExporter();
				res.reset();
				res.setHeader("Content-Disposition",
						"inline;filename=class.pdf");
				JasperReportsUtils.render(exporter, jasperPrint, os);
				os.flush();
				os.close();       
			} else {
				result.put("code", 1);
				result.put("msg", "没有可打印的数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 老师课表打印
	 */
	@RequestMapping(value = "/teacherReport")
	@ResponseBody
	public JSONObject teacherReport(Model model, HttpServletRequest req,
			HttpServletResponse res) {
		String printStyle = "01";//默认一页一表
		String type = "02"; // 教师课表
		String xxdm = getXxdm(req);
		String timetableId = req.getParameter("timetableId");
		JSONObject setInfo = getPrintSet(timetableId,xxdm,type,printStyle);
		// 报表数据源
		JSONObject result = new JSONObject();
		try {

			// 填充报表
			@SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("report");
			if (data != null) {
				JasperReport jasperReport = null;
				JRDataSource jrDataSource = null;
				int totalMaxDays = 0;
				int totalMaxLessons = 0;
				int totalMaxD = Integer.valueOf(data.get(0).getString(
						"totalMaxDays"));
				int totalMaxL = Integer.valueOf(data.get(0).getString(
						"totalMaxLessons"));							
				if (totalMaxD < 5){
					totalMaxDays = 5;
				}else if(totalMaxD > 7){
					totalMaxDays = 7;
				}else{
					totalMaxDays = totalMaxD;
				}	
				if (totalMaxL < 6){
					totalMaxLessons = 6;
				}else if(totalMaxL > 9){
					totalMaxLessons = 9;
				}else{
					totalMaxLessons = totalMaxL;
				}
				setInfo.put("totalMaxDays", totalMaxDays);
				setInfo.put("totalMaxLessons", totalMaxLessons);
				int key = totalMaxDays * 10 + totalMaxLessons;
				String label = getLabelByNum(key);				
				if (setInfo.getString("printStyle").equals("01")){
					String file = req.getSession().getServletContext()
							.getRealPath("/")
							+ "jasper"
							+ System.getProperty("file.separator")
							+ "teacher"
							+ System.getProperty("file.separator")
							+ "teacher" + label + ".jasper";
					jasperReport = (JasperReport) JRLoader
							.loadObjectFromFile(file);
					jrDataSource = new JRBeanCollectionDataSource(
							printService.getTeacherList(data, setInfo));
				}else{
					String file = req.getSession().getServletContext()
							.getRealPath("/")
							+ "jasper"
							+ System.getProperty("file.separator")
							+ "teacher"
							+ System.getProperty("file.separator")
							+ "twoteacher" + label + ".jasper";
					jasperReport = (JasperReport) JRLoader
							.loadObjectFromFile(file);
					jrDataSource = new JRBeanCollectionDataSource(
							printService.getTwoTeacherList(data, setInfo));
				}	
				JasperPrint jasperPrint = JasperFillManager.fillReport(
						jasperReport, null, jrDataSource);
				OutputStream os = res.getOutputStream();
				JRPdfExporter exporter = new JRPdfExporter();
				res.reset();
				res.setHeader("Content-Disposition",
						"inline;filename=teacher.pdf");
				JasperReportsUtils.render(exporter, jasperPrint, os);
				os.flush();
				os.close();
			} else {
				result.put("code", 1);
				result.put("msg", "没有可打印的数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 全校总课表打印
	 */
	@RequestMapping(value = "/schoolReport")
	@ResponseBody
	public JSONObject schoolReport(Model model, HttpServletRequest req,
			HttpServletResponse res) {
		String viewType = req.getParameter("viewType"); //1.班级总课表
		String printStyle = "01";//一页一表
		String type = "01"; // 全校课表
		String xxdm = getXxdm(req);
		String timetableId = req.getParameter("timetableId");
		JSONObject setInfo = getPrintSet(timetableId,xxdm,type,printStyle);
		// 报表数据源
		JSONObject result = new JSONObject();
		try {
			// 填充报表
			@SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("report");
			if (data != null) {
				JasperReport jasperReport = null;
				JRDataSource jrDataSource = null;
				int totalMaxDays = 0;
				int totalMaxLessons = 0;
				int totalMaxD = Integer.valueOf(data.get(0).getString(
						"totalMaxDays"));
				int totalMaxL = Integer.valueOf(data.get(0).getString(
						"totalMaxLessons"));							
				if (totalMaxD < 5){
					totalMaxDays = 5;
				}else if(totalMaxD > 7){
					totalMaxDays = 7;
				}else{
					totalMaxDays = totalMaxD;
				}	
				if (totalMaxL < 6){
					totalMaxLessons = 6;
				}else if(totalMaxL > 9){
					totalMaxLessons = 9;
				}else{
					totalMaxLessons = totalMaxL;
				}
				setInfo.put("totalMaxDays", totalMaxDays);
				setInfo.put("totalMaxLessons", totalMaxLessons);
				int key = totalMaxDays * 10 + totalMaxLessons;
				String label = getLabelByNum(key);				
				String file = req.getSession().getServletContext()
						.getRealPath("/")
						+ "jasper"
						+ System.getProperty("file.separator")
						+ "school"
						+ System.getProperty("file.separator")
						+ "school" + label + ".jasper";
				jasperReport = (JasperReport) JRLoader
						.loadObjectFromFile(file);	
				setInfo.put("courseShowName", "courseName");
				if (viewType.equals("1")) {//班级		
					jrDataSource = new JRBeanCollectionDataSource(
							printService.getSchoolClassList(data,setInfo));
				} else {	
					jrDataSource = new JRBeanCollectionDataSource(
							printService.getSchoolTeacherList(data,setInfo));
				}				
				JasperPrint jasperPrint = JasperFillManager.fillReport(
						jasperReport, null, jrDataSource);
				OutputStream os = res.getOutputStream();
				JRPdfExporter exporter = new JRPdfExporter();
				res.reset();
				res.setHeader("Content-Disposition",
						"inline;filename=school.pdf");
				JasperReportsUtils.render(exporter, jasperPrint, os);
				os.flush();
				os.close();
			} else {
				result.put("code", 1);
				result.put("msg", "没有可打印的数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 
	 * 全校课表导出
	 * 
	 * @param res
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/schoolExport")
	@ResponseBody
	public JSONObject schoolExport(HttpServletResponse res,
			HttpServletRequest req) {
		// 报表数据源
		String viewType = req.getParameter("type"); //1.班级总课表
		String xxdm = getXxdm(req);
		String timetableId = req.getParameter("timetableId");
		JSONObject setInfo = getPrintSet(timetableId,xxdm,"01","01");
		boolean isAllName = req.getParameter("isAllName").equals("1");
		boolean isShowExtra = req.getParameter("show").equals("1");
		String courseKey = "",rowId = "", courseExtra = "",row = "";
		if (isAllName){
			courseKey = "courseAllName";
		}else{
			courseKey = "courseName";
		}
		if (viewType.equals("1")){
			rowId = "ClassId";
			row = "className";
			courseExtra = "teacherName";
		}else{
			rowId = "TeacherId"; 
			row = "teacherName";
			courseExtra = "className";
		}
		setInfo.put("courseShowName",courseKey);
		setInfo.put("rowId",rowId);
		setInfo.put("row",row);
		setInfo.put("courseExtra",courseExtra);
		setInfo.put("isShowExtra",isShowExtra);
		try{
	        @SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("report");	        
	        if (data != null) {
				int totalMaxDays = 0;
				int totalMaxLessons = 0;
				int totalMaxD = Integer.valueOf(data.get(0).getString(
						"totalMaxDays"));
				int totalMaxL = Integer.valueOf(data.get(0).getString(
						"totalMaxLessons"));							
				if (totalMaxD < 5){
					totalMaxDays = 5;
				}else if(totalMaxD > 7){
					totalMaxDays = 7;
				}else{
					totalMaxDays = totalMaxD;
				}	
				if (totalMaxL < 6){
					totalMaxLessons = 6;
				}else if(totalMaxL > 9){
					totalMaxLessons = 9;
				}else{
					totalMaxLessons = totalMaxL;
				}
				setInfo.put("totalMaxDays", totalMaxDays);
				setInfo.put("totalMaxLessons", totalMaxLessons);
				List<JSONObject> exportList = printService.getAllSchoolExportList(data,setInfo);
		        // 导出为excel		        
		        String fileName = req.getParameter("name").toString();
		        ExcelExportTool.exportMoreExcelWithData(exportList,fileName, req, res, isShowExtra,"",false);
	        }	        			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping(value = "/classExport")
	@ResponseBody
	public JSONObject classExport(HttpServletResponse res,
			HttpServletRequest req) {
		String xxdm = getXxdm(req);
		String timetableId = req.getParameter("timetableId");
		boolean isShow = req.getParameter("isShowTeacher").equals("1");
		JSONObject setInfo = getPrintSet(timetableId,xxdm,"03","01");
		try{			
			@SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("report");
			if (data != null) {
				int totalMaxDays = 0;
				int totalMaxLessons = 0;
				int totalMaxD = Integer.valueOf(data.get(0).getString(
						"totalMaxDays"));
				int totalMaxL = Integer.valueOf(data.get(0).getString(
						"totalMaxLessons"));							
				if (totalMaxD < 5){
					totalMaxDays = 5;
				}else if(totalMaxD > 7){
					totalMaxDays = 7;
				}else{
					totalMaxDays = totalMaxD;
				}
				if (totalMaxL < 6){
					totalMaxLessons = 6;
				}else if(totalMaxL > 9){
					totalMaxLessons = 9;
				}else{
					totalMaxLessons = totalMaxL;
				}
				setInfo.put("totalMaxDays", totalMaxDays);
				setInfo.put("isShowExtra", isShow);
				setInfo.put("totalMaxLessons", totalMaxLessons);
				
				List<JSONObject> exportList = printService.getClassExportList(data,setInfo);
		        // 导出为excel
				String name = req.getParameter("name").toString();
				req.setCharacterEncoding("UTF-8");
		        ExcelExportTool.exportMoreExcelWithData(exportList,name, req, res, isShow,"",false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping(value = "/teacherExport")
	@ResponseBody
	public JSONObject teacherExport(HttpServletResponse res,
			HttpServletRequest req) {
		String xxdm = getXxdm(req);
		String timetableId = req.getParameter("timetableId");
		JSONObject setInfo = getPrintSet(timetableId,xxdm,"02","01");
		try{
			@SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("report");
			if (data != null) {
				int totalMaxDays = 0;
				int totalMaxLessons = 0;
				int totalMaxD = Integer.valueOf(data.get(0).getString(
						"totalMaxDays"));
				int totalMaxL = Integer.valueOf(data.get(0).getString(
						"totalMaxLessons"));							
				if (totalMaxD < 5){
					totalMaxDays = 5;
				}else if(totalMaxD > 7){
					totalMaxDays = 7;
				}else{
					totalMaxDays = totalMaxD;
				}
				if (totalMaxL < 6){
					totalMaxLessons = 6;
				}else if(totalMaxL > 9){
					totalMaxLessons = 9;
				}else{
					totalMaxLessons = totalMaxL;
				}
				setInfo.put("totalMaxDays", totalMaxDays);
				setInfo.put("totalMaxLessons", totalMaxLessons);
				List<JSONObject> exportList = printService.getTeacherExportList(data,setInfo);
		        // 导出为excel
				String name = req.getParameter("name").toString();
				req.setCharacterEncoding("UTF-8");
		        ExcelExportTool.exportMoreExcelWithData(exportList,name, req, res, true,"",false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}