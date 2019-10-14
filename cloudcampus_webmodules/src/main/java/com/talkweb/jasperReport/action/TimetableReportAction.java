package com.talkweb.jasperReport.action;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.jasperreports.JasperReportsUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.jasperReport.service.TimetablePrinterService;
import com.talkweb.jasperReport.util.ExcelExportTool;
import com.talkweb.timetable.service.TimetableService;

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
@RequestMapping("/tablereport/")
public class TimetableReportAction extends BaseAction {
	
	@Autowired
	private TimetableService timetableService;
	
	@Autowired
	private TimetablePrinterService printService;
	
	private static HashMap<Integer,String> label;
	
	static{
		label = new HashMap<Integer,String>();
		label.put(51, "fivsix");
		label.put(52, "fivsix");
		label.put(53, "fivsix");
		label.put(54, "fivsix");
		label.put(55, "fivsix");
		
		label.put(56, "fivsix");
		label.put(57, "fivsen");
		label.put(58, "fiveig");
		label.put(59, "fivnin");
		
		label.put(61, "sixsen");
		label.put(62, "sixsen");
		label.put(63, "sixsen");
		label.put(64, "sixsen");
		label.put(65, "sixsen");
		label.put(66, "sixsen");

		
		
		label.put(67, "sixsen");
		label.put(68, "sixeig");
		label.put(69, "sixnin");
		
		label.put(71, "sevsen");
		label.put(72, "sevsen");
		label.put(73, "sevsen");
		label.put(74, "sevsen");
		label.put(75, "sevsen");
		label.put(76, "sevsen");
		
		label.put(77, "sevsen");
		label.put(78, "seveig");
		label.put(79, "sevnin");		
	}
	
	private String getLabelByNum(int number){
		String prefix = "";
		if (label.containsKey(number)){
			prefix = label.get(number);
		}
		return prefix;
	}
	
	private JSONObject getPrintSet(String timetableId,String schoolId,String type,String printStyle){
		JSONObject param = new JSONObject();
		param.put("timetableId", timetableId);
		param.put("type", type);
		param.put("schoolId", schoolId);
		return timetableService.getTimetablePrintSet(param);
	}

	/*
	 * 班级课表打印
	 */
	@RequestMapping(value = "classReport")
	@ResponseBody
	public JSONObject classReport(HttpServletRequest req,
			HttpServletResponse res) {
		String printStyle = "01";//默认一页一表
	    String type = "03"; // 班级课表
		String xxdm = getXxdm(req);
		String isShow = "";
		String timetableId = req.getParameter("timetableId");
		JSONObject setInfo = getPrintSet(timetableId,xxdm,type,printStyle);
		if ("1".equals(req.getParameter("isShowTeacher"))){
			isShow = "-show";
		}
		// 报表数据源
		JSONObject result = new JSONObject();
		try {
			// 填充报表
			@SuppressWarnings("unchecked")
			List<JSONObject> data = (List<JSONObject>) req.getSession()
					.getAttribute("report");

			if (data != null) {
				JasperReport jasperReport = null;
				JRDataSource jrDataSource=null;
 
				int[] maxDayAndLessons =  new int[2];
				for (int i = 0; i <data.size(); i++) {
					int[] maxDayAndLessonsTemp = getMaxDayAndLessons(data.get(i).getIntValue("totalMaxDays") , data.get(i).getIntValue("totalMaxLessons") );
					maxDayAndLessons[0] = maxDayAndLessons[0] > maxDayAndLessonsTemp[0]?maxDayAndLessons[0]:maxDayAndLessonsTemp[0];
					maxDayAndLessons[1] = maxDayAndLessons[1] > maxDayAndLessonsTemp[1]?maxDayAndLessons[1]:maxDayAndLessonsTemp[1];
				}
 
				int key = maxDayAndLessons[0] * 10 + maxDayAndLessons[1];
				String label = getLabelByNum(key);
				if (setInfo.getString("printStyle").equals("01")){
					String file = req.getSession().getServletContext()
							.getRealPath("/")
							+ "jasper"
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
 
				int[] maxDayAndLessons =  new int[2];
				for (int i = 0; i <data.size(); i++) {
					int[] maxDayAndLessonsTemp = getMaxDayAndLessons(data.get(i).getIntValue("totalMaxDays") , data.get(i).getIntValue("totalMaxLessons") );
					maxDayAndLessons[0] = maxDayAndLessons[0] > maxDayAndLessonsTemp[0]?maxDayAndLessons[0]:maxDayAndLessonsTemp[0];
					maxDayAndLessons[1] = maxDayAndLessons[1] > maxDayAndLessonsTemp[1]?maxDayAndLessons[1]:maxDayAndLessonsTemp[1];
				}
				 
				int key = maxDayAndLessons[0] * 10 + maxDayAndLessons[1];
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
 
				int[] maxDayAndLessons =  new int[2];
				for (int i = 0; i <data.size(); i++) {
					int[] maxDayAndLessonsTemp = getMaxDayAndLessons(data.get(i).getIntValue("totalMaxDays") , data.get(i).getIntValue("totalMaxLessons") );
					maxDayAndLessons[0] = maxDayAndLessons[0] > maxDayAndLessonsTemp[0]?maxDayAndLessons[0]:maxDayAndLessonsTemp[0];
					maxDayAndLessons[1] = maxDayAndLessons[1] > maxDayAndLessonsTemp[1]?maxDayAndLessons[1]:maxDayAndLessonsTemp[1];
				}
				setInfo.put("totalMaxDays", maxDayAndLessons[0]);
				setInfo.put("totalMaxLessons", maxDayAndLessons[1]);
				int key = maxDayAndLessons[0] * 10 + maxDayAndLessons[1];
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
 
				
				int[] maxDayAndLessons =  new int[2];
				for (int i = 0; i <data.size(); i++) {
					int[] maxDayAndLessonsTemp = getMaxDayAndLessons(data.get(i).getIntValue("totalMaxDays") , data.get(i).getIntValue("totalMaxLessons") );
					maxDayAndLessons[0] = maxDayAndLessons[0] > maxDayAndLessonsTemp[0]?maxDayAndLessons[0]:maxDayAndLessonsTemp[0];
					maxDayAndLessons[1] = maxDayAndLessons[1] > maxDayAndLessonsTemp[1]?maxDayAndLessons[1]:maxDayAndLessonsTemp[1];
				}
 
				setInfo.put("totalMaxDays", maxDayAndLessons[0]);
				setInfo.put("totalMaxLessons", maxDayAndLessons[1]);
				List<JSONObject> exportList = printService.getAllSchoolExportList(data,setInfo);
		        // 导出为excel		        
		        String fileName = req.getParameter("name").toString();
		        ExcelExportTool.exportMoreExcelWithData(exportList,fileName, req, res, isShowExtra);
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
				setInfo.put("isShowExtra", isShow);
				List<JSONObject> exportList = printService.getClassExportList(data,setInfo);
		        // 导出为excel
				String name = req.getParameter("name").toString();
				req.setCharacterEncoding("UTF-8");
				if (!isShow && exportList!=null && exportList.size() > 0 ) {
					isShow = exportList.get(0).getBooleanValue("isTimeZone");
				}
		        ExcelExportTool.exportMoreExcelWithData(exportList,name, req, res, isShow);
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
				List<JSONObject> exportList = printService.getTeacherExportList(data,setInfo);
		        // 导出为excel
				String name = req.getParameter("name").toString();
				req.setCharacterEncoding("UTF-8");
		        ExcelExportTool.exportMoreExcelWithData(exportList,name, req, res, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	private int[] getMaxDayAndLessons(int totalMaxD , int  totalMaxL) {
		int[] maxDayAndLessons=new int[2];
		if (totalMaxD < 5){
			maxDayAndLessons[0] = 5;
		}else if(totalMaxD > 7){
			maxDayAndLessons[0] = 7;
		}else{
			maxDayAndLessons[0] = totalMaxD;
		}
		if (totalMaxL < 6){
			maxDayAndLessons[1] = 6;
		}else if(totalMaxL > 9){
			maxDayAndLessons[1] = 9;
		}else{
			maxDayAndLessons[1] = totalMaxL;
		}
		 
		return maxDayAndLessons;
		
	}
	
}