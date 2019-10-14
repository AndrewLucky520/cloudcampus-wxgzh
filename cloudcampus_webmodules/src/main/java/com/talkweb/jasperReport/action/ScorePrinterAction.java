package com.talkweb.jasperReport.action;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.jasperreports.JasperReportsUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.jasperReport.bean.ClassScoreHead;
import com.talkweb.jasperReport.bean.ClassScoreMiddle;
import com.talkweb.jasperReport.bean.ClassScoreTail;
import com.talkweb.jasperReport.bean.GradeFirstHead;
import com.talkweb.jasperReport.bean.GradeMiddle;
import com.talkweb.jasperReport.bean.GradeSecondHead;
import com.talkweb.jasperReport.bean.GradeThirdHead;
import com.talkweb.jasperReport.service.impl.ScorePrinterServiceImpl;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * 成绩报表打印Action
*/
@SuppressWarnings("deprecation")
@Controller
@RequestMapping("/scorereport/")
public class ScorePrinterAction extends BaseAction {
	
	@Autowired
	private ScorePrinterServiceImpl scoreService;
	
	/*
	 * 班级趋势图打印
	*/
	@RequestMapping(value = "resultsTrendReport")
	@ResponseBody
	public JSONObject resultsTrendReport(HttpServletRequest req,
			HttpServletResponse res) {		
		JSONObject result = new JSONObject();
		try {
			// 填充报表			
			String form = req.getParameter("hiddenData");
			String head = req.getParameter("hiddenTitle");
			String[] titles = head.trim().replace("\"", "").split(",");
			String type = "";
			if (titles.length > 1){
	            type = titles[titles.length-1];						
			}
			List<JSONObject> data = JSON.parseArray(form, JSONObject.class);
			if (CollectionUtils.isNotEmpty(data)) {
				JasperReport jasperReport = null;
				JRDataSource jrDataSource = null;
				
				String file = req.getSession().getServletContext()
						.getRealPath("/")
						+ "jasper"
						+ System.getProperty("file.separator")
						+ "score"
						+ System.getProperty("file.separator")
						+ "scoretrendreport.jasper";
				jasperReport = (JasperReport) JRLoader
						.loadObjectFromFile(file);				
				jrDataSource = new JRBeanCollectionDataSource(
						scoreService.getResultsTrendList(data,head,type));
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
	
	/**
	 * 班级报告打印
	*/
	@RequestMapping(value = "classScoreReport")
	@ResponseBody
	public JSONObject classScoreReport(HttpServletRequest req,
			HttpServletResponse res) {		
		JSONObject result = new JSONObject();
		try {		
			String form = req.getParameter("hiddenData");
			JSONObject data = JSON.parseObject(form);
			String prefix = req.getSession().getServletContext()
					.getRealPath("/")
					+ "jasper"
					+ System.getProperty("file.separator")
					+ "score"
					+ System.getProperty("file.separator");	
			if (null != data && !data.isEmpty()) {		
				List<JasperPrint> jasperPrintList = new ArrayList<JasperPrint>();
				// -----班级报告打印-首页-----
				List<ClassScoreHead> head = scoreService.getClassScoreHeadList(data);
				String headFile = prefix + "classreporthead.jasper";
				JasperReport headReport = (JasperReport) JRLoader
						.loadObjectFromFile(headFile);		
				JRDataSource headDataSource = new JRBeanCollectionDataSource(
						head);
            	JasperPrint jasperHeadPrint = JasperFillManager.fillReport(
            			headReport, null, headDataSource);
            	jasperPrintList.add(jasperHeadPrint);
            	//** -----班级报告打印-学生变化情况-----
				List<ClassScoreMiddle> middle = scoreService.getClassScoreMiddleList(data);
				if (CollectionUtils.isNotEmpty(middle)){
					String middleFile = prefix + "classreportmiddle.jasper";
					JasperReport middleReport = (JasperReport) JRLoader
							.loadObjectFromFile(middleFile);			
					JRDataSource middleDataSource = new JRBeanCollectionDataSource(
							middle);
	            	JasperPrint jasperMiddlePrint = JasperFillManager.fillReport(
	            			middleReport, null, middleDataSource);
	            	jasperPrintList.add(jasperMiddlePrint);
				}
				//** -----班级报告打印-尾页-----
				List<ClassScoreTail> tail = scoreService.getClassScoreTailList(data); 
				String tailFile = prefix + "classreporttail.jasper";
				JasperReport tailReport = (JasperReport) JRLoader
						.loadObjectFromFile(tailFile);
				
				JRDataSource tailDataSource = new JRBeanCollectionDataSource(
						tail);
            	JasperPrint jasperTailPrint = JasperFillManager.fillReport(
            			tailReport, null, tailDataSource);
            	jasperPrintList.add(jasperTailPrint);
            	//** -----打印报告-----
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	JRPdfExporter exporter = new JRPdfExporter();
            	exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST,jasperPrintList);
            	exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
            	exporter.exportReport();
            	byte[] bytes= baos.toByteArray();//得到这个流
            	res.setContentLength(bytes.length);
				res.reset();
				res.setHeader("Content-Disposition",
						"inline;filename=class.pdf");
				OutputStream os = res.getOutputStream();
				os.write(bytes, 0, bytes.length);
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
	
	/**
	 * 年级报告打印
	*/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "gradeScoreReport")
	@ResponseBody
	public JSONObject gradeScoreReport(HttpServletRequest req,
			HttpServletResponse res) {		
		JSONObject result = new JSONObject();
		try {		
			String form = req.getParameter("hiddenData");
			JSONObject data = JSON.parseObject(form);
			/**
			String useGrade = req.getParameter("hiddenGradeid").replace("\"", "");
			String xnxq = req.getParameter("hiddenXnxq").replace("\"", "");
			long schoolId = Long.parseLong(getXxdm(req));
			String label = "V";
			if (StringUtils.isNotEmpty(useGrade)
					&&StringUtils.isNotEmpty(xnxq)){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("schoolId", schoolId);
				map.put("usedGradeId", useGrade);
				map.put("termInfoId", xnxq);
				List<Classroom> classList = commonService.getClassList(map);
                if (classList.size() > 20)label = "H";
			}*/			
			String prefix = req.getSession().getServletContext()
					.getRealPath("/")
					+ "jasper"
					+ System.getProperty("file.separator")
					+ "score"
					+ System.getProperty("file.separator");	
			if (null != data && !data.isEmpty()) {	
				List<JasperPrint> jasperPrintList = new ArrayList<JasperPrint>();
				// -----模版数据源-----
				Map<String,List<?>> resultMap = scoreService.getGradeReportList(data);
                List<Integer> lenList = (List<Integer>)resultMap.get("classList");
				int classLength = Collections.max(lenList);
				String label = "V";
				if (classLength > 20) label = "H";				
				// -----班级角度-首页-----
				List<GradeFirstHead> headFirst = (List<GradeFirstHead>)resultMap.get("firstHead");
				String headFirstFile = prefix + "gradereport"+label+"1head.jasper";
				JasperReport headFirstReport = (JasperReport) JRLoader
						.loadObjectFromFile(headFirstFile);		
				JRDataSource headFirstDs = new JRBeanCollectionDataSource(
						headFirst);
            	JasperPrint jasperHeadFirPrint = JasperFillManager.fillReport(
            			headFirstReport, null, headFirstDs);
            	jasperPrintList.add(jasperHeadFirPrint);
            	//** -----班级角度-对比表-----	
            	List<GradeMiddle> middleFirst = (List<GradeMiddle>)resultMap.get("firstMiddle");
				if (CollectionUtils.isNotEmpty(middleFirst)){
					String middleFirstFile = prefix + "gradereport"+label+"1middle.jasper";
					JasperReport middleFirstReport = (JasperReport) JRLoader
							.loadObjectFromFile(middleFirstFile);			
					JRDataSource middleFirstDs = new JRBeanCollectionDataSource(
							middleFirst);
	            	JasperPrint jasperMiddleFirPrint = JasperFillManager.fillReport(
	            			middleFirstReport, null, middleFirstDs);
	            	jasperPrintList.add(jasperMiddleFirPrint);
				}
				//** -----班级角度-对比表-----	
				List<GradeMiddle> tailFirst = (List<GradeMiddle>)resultMap.get("firstTail");
				if (CollectionUtils.isNotEmpty(tailFirst)){
					String tailFirstFile = prefix + "gradereport"+label+"1tail.jasper";
					JasperReport tailFirstReport = (JasperReport) JRLoader
							.loadObjectFromFile(tailFirstFile);			
					JRDataSource tailFirstDs = new JRBeanCollectionDataSource(
							tailFirst);
	            	JasperPrint jaspertailFirPrint = JasperFillManager.fillReport(
	            			tailFirstReport, null, tailFirstDs);
	            	jasperPrintList.add(jaspertailFirPrint);
				}	
				// -----学科角度-首页-----	
				List<GradeSecondHead> headSecond = (List<GradeSecondHead>)resultMap.get("secondHead");
				String headSecondFile = prefix + "gradereport"+label+"2head.jasper";
				JasperReport headSecondReport = (JasperReport) JRLoader
						.loadObjectFromFile(headSecondFile);		
				JRDataSource headSecondDs = new JRBeanCollectionDataSource(
						headSecond);
            	JasperPrint jasperHeadSecPrint = JasperFillManager.fillReport(
            			headSecondReport, null, headSecondDs);
            	jasperPrintList.add(jasperHeadSecPrint);        	
            	//** -----学科角度-分布表-----
            	List<GradeMiddle> middleSecond = (List<GradeMiddle>)resultMap.get("secondMiddle");
				if (CollectionUtils.isNotEmpty(middleSecond)){
					String middleSecondFile = prefix + "gradereport"+label+"2middle.jasper";
					JasperReport middleSecondReport = (JasperReport) JRLoader
							.loadObjectFromFile(middleSecondFile);			
					JRDataSource middleSecondDs = new JRBeanCollectionDataSource(
							middleSecond);
	            	JasperPrint jasperMiddleSecPrint = JasperFillManager.fillReport(
	            			middleSecondReport, null, middleSecondDs);
	            	jasperPrintList.add(jasperMiddleSecPrint);
				} 			
				// -----教师角度-首页-----	
				List<GradeThirdHead> headThird = (List<GradeThirdHead>)resultMap.get("thirdHead");
				String headThirdFile = prefix + "gradereport"+label+"3head.jasper";
				JasperReport headThirdReport = (JasperReport) JRLoader
						.loadObjectFromFile(headThirdFile);		
				JRDataSource headThirdDs = new JRBeanCollectionDataSource(
						headThird);
            	JasperPrint jasperHeadThrPrint = JasperFillManager.fillReport(
            			headThirdReport, null, headThirdDs);
            	jasperPrintList.add(jasperHeadThrPrint);         	
            	//** -----教师角度-学科分布表-----		
            	List<GradeMiddle> middleThird = (List<GradeMiddle>)resultMap.get("thirdMiddle");
				if (CollectionUtils.isNotEmpty(middleThird)){
					String middleThirdFile = prefix + "gradereport"+label+"3middle.jasper";
					JasperReport middleThirdReport = (JasperReport) JRLoader
							.loadObjectFromFile(middleThirdFile);			
					JRDataSource middleThirdDs = new JRBeanCollectionDataSource(
							middleThird);
	            	JasperPrint jasperMiddleThrPrint = JasperFillManager.fillReport(
	            			middleThirdReport, null, middleThirdDs);
	            	jasperPrintList.add(jasperMiddleThrPrint);
				} 
            	//** -----打印报告-----
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	JRPdfExporter exporter = new JRPdfExporter();
            	exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST,jasperPrintList);
            	exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
            	exporter.exportReport();
            	byte[] bytes= baos.toByteArray();//得到这个流
            	res.setContentLength(bytes.length);
				res.reset();
				res.setHeader("Content-Disposition",
						"inline;filename=class.pdf");
				OutputStream os = res.getOutputStream();
				os.write(bytes, 0, bytes.length);
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
	
}