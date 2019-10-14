package com.talkweb.exammanagement.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.CommonDataDao;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.datadictionary.dao.DataDictionaryDao;
import com.talkweb.exammanagement.domain.ScoreProgressProc;
import com.talkweb.exammanagement.service.ExamManagementExamPlaceService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.student.domain.page.StartImportTaskParam;

@Controller
@RequestMapping("/examManagement/examPlaceImport")
public class ExamManagementImportAction extends BaseAction {

	@Autowired
	private ExamManagementExamPlaceService examManagementExamPlaceService;
	@Autowired
	private AllCommonDataService authService;
	@Autowired
	private DataDictionaryDao dataDictionaryDao;

	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	@Autowired
	private CommonDataDao cmdDao;

	@Autowired
	private FileServer fileServerImplFastDFS;

	private static final Logger logger = LoggerFactory
			.getLogger(ExamManagementImportAction.class);

	@Autowired
	private FileImportInfoService fileImportInfoService;
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['tempFilePath']}")
	private String tempFilePath;



	/**
	 * Excel上传接口
	 * 
	 * @param file
	 * @param req
	 * @param res
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value = "/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(
			@RequestParam("excelBody") MultipartFile file,
			HttpServletRequest req, HttpServletResponse res) {
		JSONObject obj = new JSONObject();
		HttpSession session = req.getSession();
		String termInfoId = StringUtil.transformString(req
				.getParameter("termInfoId"));// 学年学期
		System.out.println("导入sessionID:" + req.getSession().getId());
		System.out.println("获取学校代码：" + getXxdm(req));
		// if (stuTitle == null) {
		String[] stuTitle = new String[7];
		stuTitle[0] = "考场编号";
		stuTitle[1] = "考场名称";
		stuTitle[2] = "计划考生数";
		stuTitle[3] = "监考老师数";
		stuTitle[4] = "楼房名称";
		stuTitle[5] = "楼层数";
		stuTitle[6] = "教室名称";
		String[] stuTitleName = new String[stuTitle.length];
		stuTitleName[0] = "examPlaceCode";
		stuTitleName[1] = "examPlaceName";
		stuTitleName[2] = "numOfExaminee";
		stuTitleName[3] = "numOfTeacher";
		stuTitleName[4] = "buildingName";
		stuTitleName[5] = "floor";
		stuTitleName[6] = "roomName";
		int[] stuTitleNeed = new int[stuTitle.length];
		stuTitleNeed[0] = 1;
		stuTitleNeed[1] = 1;
		stuTitleNeed[2] = 1;
		stuTitleNeed[3] = 0;
		stuTitleNeed[4] = 0;
		stuTitleNeed[5] = 0;
		stuTitleNeed[6] = 0;

		// }
		int code = 1;
		String msg = "";
		// 临时文件保存目录
		File dir = new File(tempFilePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		File df = null;
		try {
			// 初步解析上传的excel，判断字段是否符合原始系统字段
			String s = UUID.randomUUID().toString();
			// 获取源文件后缀名
			String fileName = file.getOriginalFilename();
			// 扩展名并转小写
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1)
					.toLowerCase();
			// 文件名后缀转小写
			fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1)
					+ prefix;
			df = new File(tempFilePath + "/" + s + "." + prefix);
			logger.info("目标目录：" + tempFilePath + "/" + s + "." + prefix);
			file.transferTo(df);
			
			logger.info("df size："  + df.length());

			String fileId = "";
			String keyId = StringUtil.transformString(session
					.getAttribute("keyId"));// 文件主键值
			
			logger.info("fileId  ："  + fileId);
			if (StringUtils.isEmpty(keyId)) {
				keyId = UUID.randomUUID().toString();
				session.setAttribute("keyId", keyId);
			}

			fileId = fileImportInfoService.getFileBy(getXxdm(req), keyId);
			logger.info("fileId2  ："  + fileId);
			if (StringUtils.isNotEmpty(fileId)) {
				fileServerImplFastDFS.deleteFile(fileId);
				fileImportInfoService.deleteFile(getXxdm(req), keyId);
			}

			fileId = fileServerImplFastDFS.uploadFile(df, fileName);
			logger.info("fileId3  ："  + fileId);
			if (StringUtils.isNotEmpty(fileId)) {
				fileImportInfoService.addFile(getXxdm(req), keyId, fileId);
				req.getSession().setAttribute("fileId", fileId);
			}

			String processId = req.getSession().getId();
			Hashtable<String, String> tempFileMap = new Hashtable<String, String>();
			tempFileMap.put(processId, fileId);
			Object tempFileMapKey = "examManage." + getXxdm(req) + processId
					+ ".import.tempFileMap";
			logger.info("LLL:XFQ记录时tfM的tempFileMapKey为{},", tempFileMapKey);
			redisOperationDAO.set(tempFileMapKey, tempFileMap,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());
			logger.info("LLL:记录时tfM的key为:{},", tempFileMap.keySet());
			tempFileMap = (Hashtable<String, String>) redisOperationDAO
					.get(tempFileMapKey);
			logger.info("LLL:保存后tfM的key为:{},", tempFileMap.keySet());

			Object stuTitleMapKey = "examManage." + getXxdm(req) + processId
					+ ".import.stuTitle";
			Object stuTitleNameMapKey = "examManage." + getXxdm(req)
					+ processId + ".import.stuTitleName";
			Object stuTitleNeedMapKey = "examManage." + getXxdm(req)
					+ processId + ".import.stuTitleNeed";
			redisOperationDAO.set(stuTitleMapKey, stuTitle,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());
			redisOperationDAO.set(stuTitleNameMapKey, stuTitleName,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());
			redisOperationDAO.set(stuTitleNeedMapKey, stuTitleNeed,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());

			Workbook workbook = null;
			if (prefix.equalsIgnoreCase("xls")) {
				workbook = new HSSFWorkbook(new FileInputStream(df));
			} else if (prefix.equalsIgnoreCase("xlsx")) {
				workbook = new XSSFWorkbook(new FileInputStream(df));
			} else {
				code = -2;
				msg = "文件不是excel格式！";
			}
			Sheet sheet = workbook.getSheetAt(0);
			int rows = sheet.getPhysicalNumberOfRows();

			if (rows > 0) {// 有数据时才处理

				Row row = sheet.getRow(0);
				int cellNum = row.getPhysicalNumberOfCells();
				//
				String[] tempStuImpExcTitle = new String[cellNum];
				// 判断是否需要进行手工字段匹配
				for (int i = 0; i < cellNum; i++) {
					String excelVal = row.getCell(i).getStringCellValue()
							.trim();
					if (!isInArray(excelVal, stuTitle)) {
						code = 2;
						msg = "文件格式正确,字段需要匹配";
					}
					// 放置临时保存目录
					tempStuImpExcTitle[i] = excelVal;
				}
				if (code == 1) {
					if (isInArray("考场编号", tempStuImpExcTitle)
							&& isInArray("考场名称", tempStuImpExcTitle)) {
						code = 1;
					} else {
						code = -4;
						msg = "考场编号和考场名称为必填字段";
					}
				}
				Hashtable<String, String[]> excelTitleMap = new Hashtable<String, String[]>();
				excelTitleMap.put(processId, tempStuImpExcTitle);
				Object excelTitleMapKey = "examManage." + getXxdm(req)
						+ processId + ".import.excelTitleMap";
				redisOperationDAO.set(excelTitleMapKey, excelTitleMap,
						CacheExpireTime.temporaryDataDefaultExpireTime
								.getTimeValue());
			} else {
				code = -2102;
				msg = OutputMessage.getDescByCode(code + "");
			}
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			code = -2101;
			msg = "文件格式错误或无法读取！";
			msg = OutputMessage.getDescByCode(code + "");
		} finally {
			if (null != df) {
				df.delete();
			}
		}
		// 封装返回结果
		obj.put("code", code);
		obj.put("msg", msg);
		return obj;
	}

	/**
	 * 判断字符串是否在字符串数组内
	 * 
	 * @param string
	 * @param arr
	 * @return
	 */
	private boolean isInArray(String string, String[] arr) {
		// TODO Auto-generated method stubr
		boolean rs = false;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equalsIgnoreCase(string)) {
				rs = true;
				return rs;
			}
		}
		return rs;
	}

	/**
	 * 字符串在数组中的索引
	 * 
	 * @param string
	 * @param stutitle2
	 * @return
	 */
	private int strIndexInArray(String string, String[] arr) {
		// TODO Auto-generated method stub
		int rs = -1;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != null && arr[i].equalsIgnoreCase(string)) {
				rs = i;
				return rs;
			}
		}
		return rs;
	}

	/**
	 * 获取Excel表头和系统字段
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelHead(HttpServletRequest req) throws Exception {
		JSONObject excelHead = new JSONObject();
		// 获取session中保存的临时表头
		String processId = req.getSession().getId();

		Hashtable<String, String[]> excelTitleMap = new Hashtable<String, String[]>();
		Object excelTitleMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.excelTitleMap";
		Object prepDataMapObj = redisOperationDAO.get(excelTitleMapKey);

		String key = "examManage." + getXxdm(req) + processId + ".import.km";

		Object stuTitleMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.stuTitle";
		Object stuTitleNameMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.stuTitleName";
		Object stuTitleNeedMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.stuTitleNeed";

		String[] stuTitle = new String[0];
		String[] stuTitleName = new String[0];
		int[] stuTitleNeed = new int[0];
		Object titleo = redisOperationDAO.get(stuTitleMapKey);
		Object titlenameo = redisOperationDAO.get(stuTitleNameMapKey);
		Object titleneedo = redisOperationDAO.get(stuTitleNeedMapKey);
		if (null != titleo) {
			stuTitle = (String[]) titleo;
		}
		if (null != titlenameo) {
			stuTitleName = (String[]) titlenameo;
		}
		if (null != titleneedo) {
			stuTitleNeed = (int[]) titleneedo;
		}

		if (null != prepDataMapObj) {
			excelTitleMap = (Hashtable<String, String[]>) prepDataMapObj;
		}

		String[] tmpTit = (String[]) excelTitleMap.get(processId);
		if (tmpTit != null) {
			// 开始拼装返回数据结构
			excelHead.put("total", tmpTit.length);
			JSONArray rows = new JSONArray();
			excelHead.put("rows", rows);
			for (int i = 0; i < tmpTit.length; i++) {
				JSONObject obj = new JSONObject();
				obj.put("field", tmpTit[i]);
				rows.add(obj);
			}
		} else {
			excelHead.put("total", 0);
		}

		JSONObject moduleField = new JSONObject();

		// 直接使用系统表头 系统表头是否必填数组开始拼装返回数据结构
		moduleField.put("total", stuTitle.length);
		JSONArray rows = new JSONArray();
		moduleField.put("rows", rows);
		for (int i = 0; i < stuTitle.length; i++) {
			JSONObject obj = new JSONObject();
			obj.put("field", stuTitle[i]);
			obj.put("sysfield", stuTitleNeed[i]);
			rows.add(obj);
		}
		JSONObject rs = new JSONObject();

		rs.put("excelHead", excelHead);
		rs.put("moduleField", moduleField);
		return rs;

	}

	/**
	 * 导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/startImportTask")
	@ResponseBody
	public synchronized JSONObject startImportTask(HttpServletRequest req,
			@RequestBody Map mappost, HttpServletResponse res) throws Exception {
		StartImportTaskParam stt = new StartImportTaskParam();
		stt.setIsMatch(Integer.parseInt(mappost.get("isMatch").toString()));
		if (stt.getIsMatch() == 1) {
			stt.setMatchResult(JSON.parseArray(mappost.get("matchResult")
					.toString()));
		}
		JSONObject obj = new JSONObject();

		// TTrSchool tcSchool = (TTrSchool) req.getSession()
		// .getAttribute("school");

		// int impType = Integer.parseInt(req.getParameter("importType"));
		String termInfoId = mappost.get("termInfoId").toString();
		String xn = "", xqm = null;
		if (termInfoId != null) {
			xn = termInfoId.substring(0, termInfoId.length() - 1);
			xqm = termInfoId.substring(termInfoId.length() - 1);
		}
		JSONObject procObj = new JSONObject();
		// 根据使用年级、学年获取入学年度
		procObj.put("taskParam", stt);
		stt.setXn(xn);
		stt.setXq(xqm);
		stt.setXxdm(getXxdm(req));
		stt.setRxnd(mappost.get("examManagementId").toString());
		// 设置获取的参数
		// procObj.put("school", tcSchool);
		String processId = req.getSession().getId();
		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();

		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();

		Hashtable<String, String[]> excelTitleMap = new Hashtable<String, String[]>();

		Object prepDataMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.prepDataMap";
		Object progressMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.progressMap";
		Object excelTitleMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.excelTitleMap";
		try {

			redisOperationDAO.set(prepDataMapKey, prepDataMap,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());
			redisOperationDAO.set(excelTitleMapKey, excelTitleMap,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());
		} catch (Exception e) {
			e.printStackTrace();

			JSONObject data = new JSONObject();
			data.put("progress", 0);
			data.put("msg", "启动任务失败");

			procObj.put("code", -1);
			procObj.put("data", data);
			System.out.println("主线程结束！");

			obj.put("code", -1);
			obj.put("data", data);
			return obj;
		}

		obj.put("code", 0);
		JSONObject data = new JSONObject();
		obj.put("data", data);
		data.put("progress", 0);
		procObj.put("code", 0);
		procObj.put("data", data);

		progressMap.put(processId, procObj);
		redisOperationDAO.set(progressMapKey, progressMap,
				CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
		String keyId = StringUtil.transformString(req.getSession()
				.getAttribute("keyId"));// 文件主键值
		StartImportTaskParam spm = (StartImportTaskParam) progressMap.get(
				processId).get("taskParam");
		String fileId = (String)req.getSession().getAttribute("fileId");
		SubProcess sp = new SubProcess(processId, 0, null, getXxdm(req), keyId,
				spm , fileId);
		sp.start();
		data.put("msg", "正常启动任务");
		System.out.println("主线程结束！");
		return obj;

	}

	/**
	 * 继续导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/continueImport")
	@ResponseBody
	public synchronized JSONObject continueImport(HttpServletRequest req,
			HttpServletResponse res) {
		String processId = req.getSession().getId();
		int code = 1;
		String msg = "正常启动！";
		JSONObject obj = new JSONObject();
		String termInfoId = StringUtil.transformString(req
				.getParameter("termInfoId"));// 学年学期

		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();

		Object progressMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.progressMap";
		try {

			progressMap = (Hashtable<String, JSONObject>) redisOperationDAO
					.get(progressMapKey);
		} catch (Exception e) {
			e.printStackTrace();
			code = -1;
			msg = "启动进程异常！";
			obj.put("code", code);
			JSONObject data = new JSONObject();
			data.put("msg", msg);
			obj.put("data", data);
			System.out.println("继续导入-主线程结束！");
			return obj;
		}

		try {
			String keyId = StringUtil.transformString(req.getSession()
					.getAttribute("keyId"));// 文件主键值

			StartImportTaskParam spm = (StartImportTaskParam) progressMap.get(
					processId).get("taskParam");
			String fileId = (String)req.getSession().getAttribute("fileId");
			SubProcess sp = new SubProcess(processId, 1, null, getXxdm(req),
					keyId, spm , fileId);
			sp.start();
		} catch (Exception e) {
			code = -1;
			msg = "启动进程异常！";

		}
		obj.put("code", code);
		JSONObject data = new JSONObject();
		data.put("msg", msg);
		obj.put("data", data);
		System.out.println("继续导入-主线程结束！");
		return obj;

	}

	/**
	 * 导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/singleDataCheck")
	@ResponseBody
	public synchronized JSONObject singleDataCheck(HttpServletRequest req,
			@RequestBody JSONObject post, HttpServletResponse res)
			throws Exception {
		JSONObject rs = new JSONObject();
		String processId = req.getSession().getId();
		JSONObject data = new JSONObject();
		int row = post.getIntValue("row");
		rs.put("rowNum", row);

		JSONArray mrows = post.getJSONArray("mrows");

		int code = post.getIntValue("code");

		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		Object progressMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.progressMap";
		Object progressMapObj = redisOperationDAO.get(progressMapKey);
		if (null != progressMapObj) {
			progressMap = (Hashtable<String, JSONObject>) progressMapObj;
		}
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Object prepDataMapKey = "examManage." + getXxdm(req) + processId
				+ ".import.prepDataMap";
		Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
		if (null != prepDataMapObj) {
			prepDataMap = (Hashtable<String, JSONObject>) prepDataMapObj;
		}
		if (null == progressMapObj || null == prepDataMapObj) {
			data.put("progress", 100);
			data.put("msg", "由于长时间未操作，请重新导入");
			rs.put("code", -50);
			rs.put("data", data);
			progressMap.put(processId, rs);
			redisOperationDAO.set(progressMapKey, progressMap,
					CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			return rs;
		}

		HashMap<Integer, JSONObject> rowDatas = (HashMap<Integer, JSONObject>) prepDataMap
				.get(processId).get("rowDatas");

		HashMap<String, String> titleEnMap = (HashMap<String, String>) prepDataMap
				.get(processId).get("titleEnMap");

		String termInfoId = StringUtil.transformString(req
				.getParameter("termInfoId"));// 学年学期
		if (code == -1) {
			rowDatas.remove(row);
			rs.put("code", 1);
		} else if (code == 1) {
			StartImportTaskParam sp = (StartImportTaskParam) progressMap.get(
					processId).get("taskParam");
			int isMatch = sp.getIsMatch();
			JSONArray mrs = sp.getMatchResult();

			HashMap<Integer, JSONObject> pureDatas = new HashMap<Integer, JSONObject>();

			JSONObject sd = rowDatas.get(row);
			for (int i = 0; i < mrows.size(); i++) {
				JSONObject o = mrows.getJSONObject(i);
				sd.put(titleEnMap.get(o.getString("title")),
						o.getString("value"));
			}

			pureDatas.put(row, sd);
			rowDatas.put(row, sd);
			prepDataMap.get(processId).put("rowDatas", rowDatas);
			try {
				redisOperationDAO.set(prepDataMapKey, prepDataMap,
						CacheExpireTime.temporaryDataMaxExpireTime
								.getTimeValue());

			} catch (Exception e) {
				e.printStackTrace();
			}

			String key = "examManage." + getXxdm(req) + processId
					+ ".import.km";
			List<Map<String, Object>> kms = (List<Map<String, Object>>) req
					.getSession().getAttribute(key);
			JSONObject cr = checkImpData(pureDatas, mrs, isMatch, processId, sp);

			if (cr.getBooleanValue("ckRs")) {
				rs.put("code", 1);
				data.put("msg", "校验通过！");
				rs.put("data", data);
			} else {

				rs.put("code", -1);

				data.put("msg", "校验不通过！");
				rs.put("data", data);
				rs.put("mrows", cr.getJSONArray("wrongMsg").getJSONObject(0)
						.get("mrows"));
			}
		}

		return rs;
	}

	/**
	 * 获取导入进度
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/importProgress")
	@ResponseBody
	public JSONObject importProgress(HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		// TUcUser tcUser = (TUcUser) req.getSession().getAttribute("user");
		JSONObject rs = new JSONObject();

		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		Object progressMapKey = "examManage." + getXxdm(req)
				+ req.getSession().getId() + ".import.progressMap";
		Object progressMapObj = redisOperationDAO.get(progressMapKey);
		if (null != progressMapObj) {
			progressMap = (Hashtable<String, JSONObject>) progressMapObj;
		} else {
			JSONObject data = new JSONObject();
			data.put("progress", 100);
			data.put("msg", "由于长时间未操作，请重新导入");
			rs.put("code", -50);
			rs.put("data", data);
			progressMap.put(req.getSession().getId(), rs);
		}
		JSONObject obj = progressMap.get(req.getSession().getId());
		if (progressMap != null && obj != null) {
			if (obj.getIntValue("code") == -50) {
				String schoolId = this.getXxdm(req);
				String keyId = req.getSession().getAttribute("keyId")
						.toString();
				String fileId = fileImportInfoService
						.getFileBy(schoolId, keyId);

				if (null != fileId && StringUtils.isNotEmpty(fileId)) {
					fileServerImplFastDFS.deleteFile(fileId);
					logger.info("LLL:导入删除文件id22:" + fileId);
				}
				if (null != keyId && StringUtils.isNotEmpty(keyId)) {
					fileImportInfoService.deleteFile(schoolId, keyId);
				}
			}
			rs.put("code", obj.get("code"));
			rs.put("data", obj.get("data"));
		} else {
			rs.put("code", -100);
			rs.put("data", "用户身份信息已失效，请重新登陆！");

			System.out.println("用户身份信息已失效，请重新登陆！");
		}
		redisOperationDAO.set(progressMapKey, progressMap,
				CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		// rs.put("progress", obj.get("progress"));
		// rs.put("msg", obj.get("msg"));
		return rs;
	}

	/**
	 * 导出验证结果
	 * 
	 * @return
	 */
	@RequestMapping(value = "/exportWrongMsg")
	@ResponseBody
	public void exportWrongMsg(HttpServletRequest req, HttpServletResponse res) {

		JSONArray arr = JSONArray.parseArray(req.getParameter("param"));
		JSONArray excelHeads = new JSONArray();
		JSONArray line = new JSONArray();
		JSONObject col = new JSONObject();
		col.put("field", "rowNum");
		col.put("title", "行号");
		line.add(col);

		col = new JSONObject();
		col.put("field", "msg");
		col.put("title", "错误描述");
		line.add(col);
		excelHeads.add(line);

		JSONArray excelData = new JSONArray();
		for (int i = 0; i < arr.size(); i++) {
			JSONObject o = arr.getJSONObject(i);
			JSONObject row = new JSONObject();
			row.put("rowNum", o.get("row"));
			JSONArray cols = o.getJSONArray("mrows");
			String msg = "";
			for (int j = 0; j < cols.size(); j++) {
				JSONObject co = cols.getJSONObject(j);
				msg += co.getString("title") + "：" + co.getString("err") + "；";
			}

			row.put("msg", msg);

			excelData.add(row);
		}
		ExcelTool.exportExcelWithData(excelData, excelHeads, "错误信息", null, req,
				res);
	}

	class SubProcess extends Thread {
		private String processId;
		private String xxdm;
		private int impType;
		private String keyId;
		private JSONObject singleData;
		private StartImportTaskParam sp;
		private String fileId;
		public SubProcess(String processId, int impType, JSONObject singleData,
				String xxdm, String keyId, StartImportTaskParam sp , String fileId) {
			this.processId = processId;
			this.impType = impType;
			this.singleData = singleData;
			this.xxdm = xxdm;
			this.keyId = keyId;
			this.sp = sp;
			// excel导入处理结束
            this.fileId = fileId;
		}

		@Override
		public void run() {
			long t1 = (new Date()).getTime();
			// excel导入处理开始
			String fileId1 = null;

			Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			Hashtable<String, String[]> excelTitleMap = new Hashtable<String, String[]>();
			Hashtable<String, String> tempFileMap = new Hashtable<String, String>();

			Object prepDataMapKey = "examManage." + xxdm + processId
					+ ".import.prepDataMap";
			Object progressMapKey = "examManage." + xxdm + processId
					+ ".import.progressMap";
			Object excelTitleMapKey = "examManage." + xxdm + processId
					+ ".import.excelTitleMap";
			Object tempFileMapKey = "examManage." + xxdm + processId
					+ ".import.tempFileMap";
			try {

				progressMap = (Hashtable<String, JSONObject>) redisOperationDAO
						.get(progressMapKey);

				excelTitleMap = (Hashtable<String, String[]>) redisOperationDAO
						.get(excelTitleMapKey);
				tempFileMap = (Hashtable<String, String>) redisOperationDAO
						.get(tempFileMapKey);
				logger.info("LLL:tempFileMap获取是否为空 :{},keySet:{}",
						tempFileMap == null, tempFileMap.keySet());
			} catch (Exception e) {
				JSONObject toFront = new JSONObject();
				toFront.put("code", -50);
				JSONObject data = new JSONObject();
				data.put("progress", 100);
				data.put("msg", "操作失败，请重新导入!");
				toFront.put("data", data);

				progressMap.put(processId, toFront);

				e.printStackTrace();
			}
			JSONObject o = progressMap.get(processId);
			System.out.println(o.get("taskParam"));
			StartImportTaskParam sp = (StartImportTaskParam) o.get("taskParam");
			int isMatch = sp.getIsMatch();
			JSONArray mrs = sp.getMatchResult();
			JSONObject toFront = progressMap.get(processId);
			JSONObject data = new JSONObject();
			data.put("progress", 0);
			data.put("msg", "正在准备导入任务");
			toFront.put("data", data);
			toFront.put("code", 1);

			try {
				progressMap.put(processId, toFront);
				redisOperationDAO.set(progressMapKey, progressMap,
						CacheExpireTime.temporaryDataMaxExpireTime
								.getTimeValue());
				logger.info("LLL:tempFileMap获取是否为空222 :{},keySet:{}",
						tempFileMap == null, tempFileMap.keySet());
				logger.info("LLL读取文件854行kid:{},xxdm:{}", keyId, xxdm);
				String fileIdTmp = fileImportInfoService.getFileBy(xxdm, keyId);
				if (fileIdTmp!=null) {
					fileId = fileIdTmp;
				}
				logger.info("LLL读取文件856行fileId:{}", fileId);
				// String fileId = tempFileMap.get(processId);
				// String prefix = fileId.substring(fileId.lastIndexOf(".") +
				// 1);
				// String impFrc = tempFilePath + "/temp." + prefix;
				// fileServerImplFastDFS.downloadFile(tempFileMap.get(processId),
				// impFrc);
				String[] ckExcTt = null;
				JSONObject readRs = new JSONObject();
				readRs.put("code", 1);
				JSONArray datas = new JSONArray();
				if (impType == 0) {
					logger.info("LLL读取文件id" + fileId);
					readRs = readExcelToData(fileId);
					datas = readRs.getJSONArray("datas");
					try {
						ckExcTt = (String[]) datas.get(0);
					} catch (Exception e) {
						toFront.put("code", -1);
						data.put("progress", 100);
						data.put("msg", "excel中没有数据,请重新上传");
						progressMap.put(processId, toFront);
						redisOperationDAO.set(progressMapKey, progressMap,
								CacheExpireTime.temporaryDataMaxExpireTime
										.getTimeValue());
						e.printStackTrace();

					}
				}
				int readCode = readRs.getIntValue("code");
				if ((readCode == 1 && datas.size() > 0) || impType == 1) {
					// 开始校验
					// toFront.put("code", value);
					JSONObject ckrs = new JSONObject();
					ckrs.put("ckRs", true);
					if (impType == 0) {

						data.put("progress", 5);
						data.put("msg", "正在校验excel数据");
						toFront.put("data", data);
						progressMap.put(processId, toFront);
						redisOperationDAO.set(progressMapKey, progressMap,
								CacheExpireTime.temporaryDataMaxExpireTime
										.getTimeValue());
						JSONObject preDatas = changeData(datas, mrs, isMatch,
								processId, xxdm);
						HashMap<Integer, JSONObject> pureDatas = (HashMap<Integer, JSONObject>) preDatas
								.get("rowDatas");
						ckrs = checkImpData(pureDatas, mrs, isMatch, processId,
								sp);
					}
					System.out.println("查询结果:" + ckrs);
					prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO
							.get(prepDataMapKey);
					if (ckrs.getBooleanValue("ckRs") || impType == 1) {
						// 开始入库
						data.put("progress", 40);
						data.put("msg", "正在保存数据！");
						toFront.put("data", data);
						progressMap.put(processId, toFront);
						redisOperationDAO.set(progressMapKey, progressMap,
								CacheExpireTime.temporaryDataMaxExpireTime
										.getTimeValue());

						JSONObject needInsert = getInsertEntityByCkrs(sp,
								processId);
						List<JSONObject> newlist = (List<JSONObject>) needInsert
								.get("newlist");
						List<JSONObject> coverlist = (List<JSONObject>) needInsert
								.get("coverlist");

						if (newlist.size() > 0 || coverlist.size() > 0) {
							try {

								// autoArrangeProcess( "正在保存数据！", processId, 1,
								// 6, 5, 40);
								ScoreProgressProc spThread = new ScoreProgressProc(
										"正在保存数据！", processId, 1, 6, 5, 40,
										progressMap, xxdm,
										progressMapKey.toString());

								spThread.start();
								Map<String, Object> param = new HashMap<String, Object>();
								param.put("list", newlist);
								param.put("coverlist", coverlist);
								param.put("schoolId", sp.getXxdm());
								param.put("termInfo", sp.getXn() + sp.getXq());
								param.put("examManagementId", sp.getRxnd());
								examManagementExamPlaceService
										.saveExamPlace(param);
							} catch (Exception e) {
								toFront.put("code", -1);
								data.put("progress", 100);
								data.put("msg", "保存数据时发生系统故障!");
								progressMap.put(processId, toFront);
								e.printStackTrace();
							}
						}

						toFront.put("code", 2);
						data.put("progress", 100);
						// int nums = datas.size() - 1;
						int a = newlist.size() + coverlist.size();
						data.put("msg", "导入成功，共计导入" + a + "个考场数据！");
						toFront.put("data", data);
						progressMap.put(processId, toFront);
						redisOperationDAO.set(progressMapKey, progressMap,
								CacheExpireTime.temporaryDataMaxExpireTime
										.getTimeValue());

					} else {
						toFront.put("code", -2);
						data.put("progress", 100);
						data.put("msg", "Excel数据校验不通过!");
						if (impType == 0) {

							data.put("total", datas.size() - 1);
						} else {
							data.put("total", 1);
						}
						JSONObject validateMsg = new JSONObject();
						validateMsg.put("total", ckrs.getJSONArray("wrongMsg")
								.size());
						validateMsg.put("rows", ckrs.getJSONArray("wrongMsg"));
						data.put("validateMsg", validateMsg);
						toFront.put("data", data);
						progressMap.put(processId, toFront);
						redisOperationDAO.set(progressMapKey, progressMap,
								CacheExpireTime.temporaryDataMaxExpireTime
										.getTimeValue());
						redisOperationDAO.set(prepDataMapKey, prepDataMap,
								CacheExpireTime.temporaryDataDefaultExpireTime
										.getTimeValue());
					}
				} else {
					if (readCode == -10) {
						toFront.put("code", -1);
						data.put("progress", 100);
						data.put("msg", "服务器出错，上传的文件不存在!");
						progressMap.put(processId, toFront);
						redisOperationDAO.set(progressMapKey, progressMap,
								CacheExpireTime.temporaryDataMaxExpireTime
										.getTimeValue());
					} else if (readCode == -20) {
						toFront.put("code", -1);
						data.put("progress", 100);
						data.put("msg", "解析Excel时出错，可能为文件格式问题!");
						progressMap.put(processId, toFront);
						redisOperationDAO.set(progressMapKey, progressMap,
								CacheExpireTime.temporaryDataMaxExpireTime
										.getTimeValue());
					} else if (readCode == -30) {
						toFront.put("code", -1);
						data.put("progress", 100);
						data.put("msg", "解析Excel时出错，请检查Excel格式及内容或联系管理员!");
						progressMap.put(processId, toFront);
						redisOperationDAO.set(progressMapKey, progressMap,
								CacheExpireTime.temporaryDataMaxExpireTime
										.getTimeValue());
					}
				}

			} catch (NullPointerException e) {
				toFront.put("code", -1);
				data.put("progress", 100);
				data.put("msg", "导入失败，错误未知，请稍后重试或联系管理员!");
				toFront.put("data", data);
				progressMap.put(processId, toFront);
				e.printStackTrace();
				logger.error(e.getMessage());
			} catch (Exception e) {
				toFront.put("code", -1);
				data.put("progress", 100);
				data.put("msg", "导入失败，错误未知，请稍后重试或联系管理员!");
				toFront.put("data", data);

				progressMap.put(processId, toFront);
				e.printStackTrace();
				logger.error(e.getMessage());
			} finally {
				// excel导入处理结束
				try {
					redisOperationDAO.set(progressMapKey, progressMap,
							CacheExpireTime.temporaryDataDefaultExpireTime
									.getTimeValue());
					redisOperationDAO.set(prepDataMapKey, prepDataMap,
							CacheExpireTime.temporaryDataDefaultExpireTime
									.getTimeValue());
					redisOperationDAO.set(excelTitleMapKey, excelTitleMap,
							CacheExpireTime.temporaryDataDefaultExpireTime
									.getTimeValue());
					redisOperationDAO.set(tempFileMapKey, tempFileMap,
							CacheExpireTime.temporaryDataDefaultExpireTime
									.getTimeValue());
					long t2 = (new Date()).getTime();
					System.out.println("导入子线程结束,耗时：" + (t2 - t1));
					System.out.println("开始删除临时excel");
					fileServerImplFastDFS
							.deleteFile(tempFileMap.get(processId));

					logger.info("LLL:导入删除文件id:{},seesionID:{}",
							tempFileMap.get(processId), processId);
					fileImportInfoService.deleteFileByFileId(tempFileMap
							.get(processId));

				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage());
				}

			}
		}

	}

	/**
	 * 根据参数生成需插入数据
	 * 
	 * @param sp
	 * @param processId
	 * @return
	 * @throws Exception
	 */
	private JSONObject getInsertEntityByCkrs(StartImportTaskParam sp,
			String processId) throws Exception {
		// TODO Auto-generated method stub
		// 使用为考试轮次名称
		String xq = sp.getXq();
		String xn = sp.getXn();
		String xxdm = sp.getXxdm();
		// 使用为考试轮次代码
		String kslcdm = sp.getRxnd();

		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Object prepDataMapKey = "examManage." + xxdm + processId
				+ ".import.prepDataMap";
		Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
		if (null != prepDataMapObj) {
			prepDataMap = (Hashtable<String, JSONObject>) prepDataMapObj;
		}

		HashMap<Integer, List<JSONObject>> rows = (HashMap<Integer, List<JSONObject>>) prepDataMap
				.get(processId).get("rowcolMap");

		JSONObject rs = new JSONObject();
		List<JSONObject> newlist = new ArrayList<JSONObject>();
		List<JSONObject> coverlist = new ArrayList<JSONObject>();
		for (Iterator<Integer> it = rows.keySet().iterator(); it.hasNext();) {
			int key = it.next();
			JSONObject rowdata = (JSONObject) rows.get(key);
			List<JSONObject> rowList = (List<JSONObject>) rowdata
					.get("newlist");
			for (JSONObject row : rowList) {

				// TGmScoreinfo pa = (TGmScoreinfo) JSON.toJavaObject(row,
				// TGmScoreinfo.class);
				row.put("examManagementId", kslcdm);
				row.put("termInfo", xn + xq);
				row.put("schoolId", xxdm);
				newlist.add(row);
			}
			List<JSONObject> covlist = (List<JSONObject>) rowdata
					.get("coverlist");
			for (JSONObject row : covlist) {

				// TGmScoreinfo pa = (TGmScoreinfo) JSON.toJavaObject(row,
				// TGmScoreinfo.class);
				row.put("examManagementId", kslcdm);
				row.put("termInfo", xn + xq);
				row.put("schoolId", xxdm);
				coverlist.add(row);
			}
		}

		rs.put("newlist", newlist);
		rs.put("coverlist", coverlist);
		return rs;
	}

	/**
	 * 根据映射字段映射生成数据
	 * 
	 * @param datas
	 * @param mrs
	 * @param isMatch
	 * @param processId2
	 * @return
	 * @throws Exception
	 */
	private JSONObject changeData(JSONArray datas, JSONArray mrs, int isMatch,
			String processId, String xxdm) throws Exception {
		JSONArray exDatas = new JSONArray();
		HashMap<Integer, JSONObject> rowDatas = new HashMap<Integer, JSONObject>();
		HashMap<String, String> titleEnMap = new HashMap<String, String>();
		// List<Map<String, Object>> kms = authService.getAdminKM(s,
		// xnxq);

		Object stuTitleMapKey = "examManage." + xxdm + processId
				+ ".import.stuTitle";
		Object stuTitleNameMapKey = "examManage." + xxdm + processId
				+ ".import.stuTitleName";
		Object stuTitleNeedMapKey = "examManage." + xxdm + processId
				+ ".import.stuTitleNeed";

		String[] stuTitle = new String[0];
		String[] stuTitleName = new String[0];
		int[] stuTitleNeed = new int[0];
		Object titleo = redisOperationDAO.get(stuTitleMapKey);
		Object titlenameo = redisOperationDAO.get(stuTitleNameMapKey);
		Object titleneedo = redisOperationDAO.get(stuTitleNeedMapKey);
		if (null != titleo) {
			stuTitle = (String[]) titleo;
		}
		if (null != titlenameo) {
			stuTitleName = (String[]) titlenameo;
		}
		if (null != titleneedo) {
			stuTitleNeed = (int[]) titleneedo;
		}

		String[] titles = (String[]) datas.get(0);
		// 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射实体字段[3]
		String[][] arr = new String[4][titles.length];
		// 无需手工匹配
		if (isMatch == 0) {
			// 封装表头
			for (int i = 0; i < titles.length; i++) {
				arr[0][i] = titles[i];
				int needIndex = strIndexInArray(titles[i], stuTitle);
				if (needIndex >= 0) {
					// 在系统字段中能找到
					arr[1][i] = stuTitleNeed[needIndex] + "";
					arr[2][i] = stuTitle[needIndex];
					arr[3][i] = stuTitleName[needIndex];
				} else {
					// 在系统字段中找不到 标记为不录入
					arr[1][i] = "-1";
					arr[2][i] = "none";
				}
			}
		} else {
			// 需要手工匹配的 根据匹配关系封装表头
			// 封装表头
			for (int i = 0; i < titles.length - 1; i++) {
				String sysTit = "none";
				for (int j = 0; j < mrs.size(); j++) {
					JSONObject obj = mrs.getJSONObject(j);
					if (titles[i] != null
							&& titles[i].equalsIgnoreCase(obj
									.getString("excelField"))) {
						sysTit = obj.getString("sysField");
						continue;
					}
				}
				arr[0][i] = titles[i];
				int needIndex = strIndexInArray(sysTit, stuTitle);
				if (needIndex >= 0) {
					// 在系统字段中能找到
					arr[1][i] = stuTitleNeed[needIndex] + "";
					arr[2][i] = stuTitle[needIndex];
					arr[3][i] = stuTitleName[needIndex];
				} else {
					// 在系统字段中找不到 标记为不录入
					arr[1][i] = "-1";
					arr[2][i] = "none";
					arr[0][i] = null;
				}
			}
		}
		for (int i = 1; i < datas.size(); i++) {
			JSONObject d = new JSONObject();
			String[] cell = (String[]) datas.get(i);
			for (int j = 0; j < arr[0].length - 1; j++) {
				int isIn = Integer.parseInt(arr[1][j]);
				if (isIn != -1) {
					// 放入数据 （excel映射json）
					String cellVal = cell[j];
					d.put(arr[3][j], cellVal);
					d.put(arr[3][j] + "Name", arr[0][j]);

					titleEnMap.put(arr[0][j], arr[3][j]);
				}
			}
			int rowNum = Integer.parseInt(cell[arr[0].length - 1]) + 1;
			d.put("rowNum", rowNum);
			rowDatas.put(rowNum, d);
			exDatas.add(d);
		}

		JSONObject rs = new JSONObject();
		// rs.put("exDatas", exDatas);
		rs.put("rowDatas", rowDatas);
		rs.put("titleEnMap", titleEnMap);
		rs.put("impHead", arr[3]);

		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		prepDataMap.put(processId, rs);
		Object prepDataMapKey = "examManage." + xxdm + processId
				+ ".import.prepDataMap";
		redisOperationDAO.set(prepDataMapKey, prepDataMap,
				CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		prepDataMap.put(processId, rs);
		return rs;
	}

	/**
	 * 将excel读取为数据
	 * 
	 * @param impFrc
	 * @return
	 * @throws IOException 
	 */
	private JSONObject readExcelToData(String fileId) throws IOException {
		// TODO Auto-generated method stub
		JSONObject rs = new JSONObject();
		int code = 1;
		JSONArray datas = new JSONArray();
		// 解析excel 封装对象
		Workbook workbook = null;
		File file = null;
		FileInputStream fi=null;
		try {
			String impFrc = UUID.randomUUID().toString();
			fileServerImplFastDFS.downloadFile(fileId, impFrc);
			file = new File(impFrc);
			logger.info("LLL文件名称" + impFrc);
			workbook = WorkbookFactory.create(file);
			fi=new FileInputStream(file);
			if (impFrc.endsWith("xls")) {
				workbook = new HSSFWorkbook(fi);
			} else if (impFrc.endsWith("xlsx")) {
				workbook = new XSSFWorkbook(fi);
			}
			Sheet sheet = workbook.getSheetAt(0);
			int rows = sheet.getLastRowNum();
			// 转换器 一般poi取数字格式需转换
			DecimalFormat df = new DecimalFormat("0.0");

			if (rows > 0) {
				int cols = sheet.getRow(0).getPhysicalNumberOfCells();
				datas = new JSONArray();
				for (int i = 0; i <= rows; i++) {
					if (sheet.getRow(i) == null) {
						continue;
					}
					String[] temp = new String[cols + 1];
					boolean isTrueNull = true;
					for (int j = 0; j < cols; j++) {
						if (sheet.getRow(i).getCell(j) != null
								&& sheet.getRow(i).getCell(j).getCellType() != HSSFCell.CELL_TYPE_BLANK) {
							Cell cell = sheet.getRow(i).getCell(j);
							switch (cell.getCellType()) {
							case HSSFCell.CELL_TYPE_NUMERIC:
								temp[j] = df.format(cell.getNumericCellValue());
								if (df.format(cell.getNumericCellValue())
										.endsWith(".0")) {
									temp[j] = df.format(
											cell.getNumericCellValue()).split(
											"\\.")[0];

								}
								break;
							case HSSFCell.CELL_TYPE_STRING:
								temp[j] = cell.getRichStringCellValue()
										.getString().trim();
								break;
							case HSSFCell.CELL_TYPE_FORMULA:
								try {
									temp[j] = String.valueOf(cell
											.getStringCellValue());
								} catch (IllegalStateException e) {
									temp[j] = String.valueOf(cell
											.getNumericCellValue());
								}
								break;

							}
							isTrueNull = false;
						} else {
							temp[j] = "";
						}
					}
					if (!isTrueNull) {
						if (i != 0) {
							temp[cols] = i + "";
						}
						datas.add(temp);
					}
				}
			}
		} catch (FileNotFoundException e) {
			code = -10;
		} catch (IOException e) {
			code = -20;
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			code = -30;
			e.printStackTrace();
		} finally {
			if(null!=fi){
				fi.close();
			}
			workbook.close();
			if (null != file) {
				file.delete();
				logger.info("fff删除否---"+file.delete());
//				while(!file.delete()){
//					logger.info("删除中。。。");
//					file.delete();
//				}
			}
		}

		rs.put("code", code);
		rs.put("datas", datas);
		return rs;

	}

	/**
	 * 校验表格数据
	 * 
	 * @param pureDatas
	 * @param mrs
	 * @param isMatch
	 * @param sp
	 * @return
	 * @throws Exception
	 */
	private JSONObject checkImpData(HashMap<Integer, JSONObject> pureDatas,
			JSONArray mrs, int isMatch, String processId,
			StartImportTaskParam sp) throws Exception {

		// 数据库验证部分
		// 使用为考试轮次名称
		String xq = sp.getXq();
		String xn = sp.getXn();
		String xxdm = sp.getXxdm();
		// 使用为考试轮次代
		String kslcdm = sp.getRxnd();

		List<String> titles = new ArrayList<String>();
		titles.add("numOfExaminee");
		titles.add("numOfTeacher");
		titles.add("buildingName");
		titles.add("floor");
		titles.add("roomName");

		// excel验证部分
		JSONObject rs = new JSONObject();
		// 导入进度
		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		JSONObject toFront = new JSONObject();
		JSONObject data = new JSONObject();
		Object progressMapKey = "examManage." + xxdm + processId
				+ ".import.progressMap";
		Object progressMapObj = redisOperationDAO.get(progressMapKey);
		// String schoolId=sp.getSchoolId();
		if (null != progressMapObj) {
			progressMap = (Hashtable<String, JSONObject>) progressMapObj;
			toFront = progressMap.get(processId);
			data.put("progress", 9);
			data.put("msg", "正在校验excel数据,匹配表头完成,校验数据");
			toFront.put("data", data);
			progressMap.put(processId, toFront);

			redisOperationDAO.set(progressMapKey, progressMap,
					CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		}
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Object prepDataMapKey = "examManage." + xxdm + processId
				+ ".import.prepDataMap";
		Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
		if (null != prepDataMapObj) {
			prepDataMap = (Hashtable<String, JSONObject>) prepDataMapObj;
		}
		if (null == progressMapObj || null == prepDataMapObj) {
			toFront.put("code", -50);
			data.put("progress", 100);
			data.put("msg", "由于长时间未操作，请重新导入");
			toFront.put("data", data);
			progressMap.put(processId, toFront);
			redisOperationDAO.set(progressMapKey, progressMap,
					CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			rs.put("ckRs", false);
			rs.put("errorCode", -50);
			return rs;
		}

		data.put("progress", 10);
		data.put("msg", "匹配表头完成,正在获取考场信息进行对比");
		toFront.put("data", data);
		progressMap.put(processId, toFront);

		redisOperationDAO.set(progressMapKey, progressMap,
				CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		// progNum = 6;
		List<JSONObject> wrongMsg = new ArrayList<JSONObject>();
		JSONArray exDatas = new JSONArray();
		JSONObject preData = prepDataMap.get(processId);

		HashMap<String, String> examPlaceCodeMap = new HashMap<String, String>();
		if (preData.containsKey("examPlaceCodeMap")) {
			examPlaceCodeMap = (HashMap<String, String>) preData
					.get("examPlaceCodeMap");
		}
		HashMap<String, String> examPlaceNameMap = new HashMap<String, String>();
		if (preData.containsKey("examPlaceNameMap")) {
			examPlaceNameMap = (HashMap<String, String>) preData
					.get("examPlaceNameMap");
		}
		JSONObject dbckMap = new JSONObject();
		if (preData.containsKey("dbckMap")) {
			dbckMap = preData.getJSONObject("dbckMap");
		} else {
			HashMap map = new HashMap();
			map.put("schoolId", xxdm);
			map.put("examManagementId", kslcdm);
			map.put("termInfo", xn + xq);

			ScoreProgressProc spThread = new ScoreProgressProc("正在查询考场数据...",
					processId, 1, 5, 5, 6, progressMap, xxdm,
					progressMapKey.toString());

			spThread.start();
			dbckMap = examManagementExamPlaceService.getExamPlaceListMap(map);
			preData.put("dbckMap", dbckMap);
		}
		data.put("progress", 32);
		data.put("msg", "获取对比数据完成，正在校验excel数据 ");
		toFront.put("data", data);
		progressMap.put(processId, toFront);

		redisOperationDAO.set(progressMapKey, progressMap,
				CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		HashMap<String, JSONObject> dbexamPlaceCodeMap = (HashMap<String, JSONObject>) dbckMap
				.get("examPlaceCodeMap"); // 学号与教师代码(工号)映射表
		HashMap<String, JSONObject> dbexamPlaceNameMap = (HashMap<String, JSONObject>) dbckMap
				.get("examPlaceNameMap"); // 学号与教师代码(工号)映射表
		String[] impHead = (String[]) preData.get("impHead");

		int bsix = preData.getIntValue("bsix");
		String bs = preData.getString("bs");

		HashMap<Integer, JSONObject> rowcolMap;
		if (preData.containsKey("rowcolMap")) {
			rowcolMap = (HashMap<Integer, JSONObject>) preData.get("rowcolMap");
		} else {
			rowcolMap = new HashMap<Integer, JSONObject>();
			preData.put("rowcolMap", rowcolMap);
		}
		int index = 0;
		for (Iterator<Integer> it = pureDatas.keySet().iterator(); it.hasNext();) {
			index++;
			int rowNum = it.next();
			// 单条时直接覆盖 批量时也不会重复 行号不重复的话
			List<JSONObject> newlist = new ArrayList<JSONObject>();
			List<JSONObject> coverlist = new ArrayList<JSONObject>();
			JSONObject row = new JSONObject();
			row.put("newlist", newlist);
			row.put("coverlist", coverlist);
			rowcolMap.put(rowNum, row);

			JSONObject pd = pureDatas.get(rowNum);
			JSONObject wmsg = new JSONObject();
			wmsg.put("row", rowNum);
			wmsg.put("bs", pd.getString(impHead[bsix] + "Name"));
			JSONArray mrows = new JSONArray();
			wmsg.put("mrows", mrows);
			// excel重复性验证

			String sysexamPlaceCode = "";
			String sysexamPlaceName = "";

			String examPlaceCode = pd.containsKey("examPlaceCode") ? pd
					.getString("examPlaceCode").replaceAll(" ", "") : null;
			String examPlaceName = pd.containsKey("examPlaceName") ? pd
					.getString("examPlaceName") : null;

			// excel重复性验证

			boolean isnameCfBug = false;
			boolean iscodefBug = false;
			boolean iscover = false;
			HashMap<String,JSONObject> wmsgmap=new HashMap<String, JSONObject>();
			for(Object o:wrongMsg){
				JSONObject rowda=(JSONObject) o;
				wmsgmap.put(rowda.getString("row"),rowda);
			}
			if (examPlaceCode != null && examPlaceCode.trim().length() > 0) {
				if (examPlaceCodeMap.containsKey(examPlaceCode)&&!examPlaceCodeMap.get(examPlaceCode).equals(rowNum+"")) {
					JSONObject wsg = new JSONObject();
					wsg.put("title", pd.get("examPlaceCodeName"));
					wsg.put("oldValue", examPlaceCode);
					wsg.put("err", "excel中重复！");
					mrows.add(wsg);
						if(wmsgmap.containsKey(examPlaceCodeMap.get(examPlaceCode).toString())){
							JSONObject rowda=wmsgmap.get(examPlaceCodeMap.get(examPlaceCode).toString());
								JSONArray roway=(JSONArray) rowda.get("mrows");
								JSONArray roway1=new JSONArray();
								for(Object ro:roway){
									JSONObject rowj=(JSONObject) ro;
									if(rowj.getString("title").equals( pd.get("examPlaceCodeName"))&&!rowj.getString("err").equals("excel中重复！")){
										JSONObject wsg1 = new JSONObject();
										wsg1.put("title", pd.get("examPlaceCodeName"));
										wsg1.put("oldValue", examPlaceCode);
										wsg1.put("err", "excel中重复！");
										roway.add(wsg1);
								}else if(!rowj.getString("title").equals( pd.get("examPlaceNameName"))){
									JSONObject wsg1 = new JSONObject();
									wsg1.put("title", pd.get("examPlaceNameName"));
									wsg1.put("oldValue", examPlaceName);
									wsg1.put("err", "excel中重复！");
									roway1.add(wsg1);
								}
							}
								roway.addAll(roway1);
						}else{
							String rownum1=examPlaceCodeMap.get(examPlaceCode).toString();
							JSONObject wsg1 = new JSONObject();
							wsg1.put("title", pd.get("examPlaceCodeName"));
							wsg1.put("oldValue", examPlaceCode);
							wsg1.put("err", "excel中重复！");
							JSONObject wmsg1 = new JSONObject();
							wmsg1.put("row", rownum1);
							wmsg1.put("bs", pd.getString(impHead[bsix] + "Name"));
							JSONArray mrows1 = new JSONArray();
							mrows1.add(wsg1);
							wmsg1.put("mrows", mrows1);
							wrongMsg.add(wmsg1);
						}
						
				}else if(!StringUtils.isNumeric(examPlaceCode)){
					JSONObject wsg = new JSONObject();
					wsg.put("title", pd.get("examPlaceCodeName"));
					wsg.put("oldValue", examPlaceCode);
					wsg.put("err", "必须是整数");
					mrows.add(wsg);
				}else {
					if (!dbexamPlaceCodeMap.containsKey(examPlaceCode)) {

						sysexamPlaceCode = examPlaceCode;
						iscodefBug = true;
					} else {
						// JSONObject wsg = new JSONObject();
						// wsg.put("title", pd.get("examPlaceCodeName") );
						// wsg.put("oldValue", examPlaceCode);
						// wsg.put("err", "系统中存在重复数据");
						// mrows.add(wsg);
						sysexamPlaceCode = examPlaceCode;
						iscodefBug = true;
						iscover = true;
					}
				}
			} else {
				JSONObject wsg = new JSONObject();
				wsg.put("title", pd.get("examPlaceCodeName"));
				wsg.put("oldValue", examPlaceCode);
				wsg.put("err", "不能为空！");
				mrows.add(wsg);
			}

			if (examPlaceName != null && examPlaceName.trim().length() > 0) {
				if (examPlaceNameMap.containsKey(examPlaceName)&&!examPlaceNameMap.get(examPlaceName).equals(rowNum+"")) {
					JSONObject wsg = new JSONObject();
					wsg.put("title", pd.get("examPlaceNameName"));
					wsg.put("oldValue", examPlaceName);
					wsg.put("err", "excel中重复！");
					mrows.add(wsg);
					
					if(wmsgmap.containsKey(examPlaceNameMap.get(examPlaceName).toString())){
						JSONObject rowda=wmsgmap.get(examPlaceNameMap.get(examPlaceName).toString());
							JSONArray roway=(JSONArray) rowda.get("mrows");
							JSONArray roway1=new JSONArray();
							for(Object ro:roway){
								JSONObject rowj=(JSONObject) ro;
								if(rowj.getString("title").equals( pd.get("examPlaceNameName"))&&!rowj.getString("err").equals("excel中重复！")){
									JSONObject wsg1 = new JSONObject();
									wsg1.put("title", pd.get("examPlaceNameName"));
									wsg1.put("oldValue", examPlaceName);
									wsg1.put("err", "excel中重复！");
									roway.add(wsg1);
							}else if(!rowj.getString("title").equals( pd.get("examPlaceNameName"))){
								JSONObject wsg1 = new JSONObject();
								wsg1.put("title", pd.get("examPlaceNameName"));
								wsg1.put("oldValue", examPlaceName);
								wsg1.put("err", "excel中重复！");
								roway1.add(wsg1);
							}
						}
							roway.addAll(roway1);
					}else{
						String rownum1=examPlaceNameMap.get(examPlaceName).toString();
						JSONObject wsg1 = new JSONObject();
						wsg1.put("title", pd.get("examPlaceNameName"));
						wsg1.put("oldValue", examPlaceName);
						wsg1.put("err", "excel中重复！");
						JSONObject wmsg1 = new JSONObject();
						wmsg1.put("row", rownum1);
						wmsg1.put("bs", pd.getString(impHead[bsix] + "Name"));
						JSONArray mrows1 = new JSONArray();
						mrows1.add(wsg1);
						wmsg1.put("mrows", mrows1);
						wrongMsg.add(wmsg1);
					}
					
				} else {
					if (!dbexamPlaceNameMap.containsKey(examPlaceName)) {
						sysexamPlaceName = examPlaceName;
						isnameCfBug = true;
					} else if (dbexamPlaceNameMap.containsKey(examPlaceName)&&dbexamPlaceNameMap.get(examPlaceName).getString("examPlaceCode").equals(examPlaceCode)) {
						sysexamPlaceName = examPlaceName;
						isnameCfBug = true;
					} else {
						JSONObject wsg = new JSONObject();
						wsg.put("title", pd.get("examPlaceNameName"));
						wsg.put("oldValue", examPlaceName);
						wsg.put("err", "系统中存在重复数据");
						mrows.add(wsg);
					}
				}
			} else {
				JSONObject wsg = new JSONObject();
				wsg.put("title", pd.get("examPlaceNameName"));
				wsg.put("oldValue", examPlaceName);
				wsg.put("err", "不能为空！");
				mrows.add(wsg);
			}
			// if (mrows.size() == 0) {

			JSONObject scr = new JSONObject();
			scr.put("numOfExaminee", "");
			scr.put("numOfTeacher", "");
			scr.put("buildingName", "");
			scr.put("floor", "");
			scr.put("roomName", "");
			for (int j = 0; j < impHead.length; j++) {

				if (impHead[j] == null || !titles.contains(impHead[j])) {
					continue;
				}
				String tit = impHead[j];
				String val = pd.containsKey(tit) ? pd.getString(tit) : "";
				// if (tit.equalsIgnoreCase("xm")) {
				// val = val.replaceAll(" ", "");
				// }
				String titName = pd.getString(tit + "Name");
				JSONObject wsg = new JSONObject();
				wsg.put("title", titName);
				wsg.put("titleEnName", tit);
				wsg.put("oldValue", val);
				String err = "";
				int num = 0;
				boolean noRecord = false;
				String tsqk = null;
					if (tit.equals("numOfExaminee")) {
						if(val==null||val.trim().equals("")){
							err += "该项为必填！";
						}else{
							try {
								num = Integer.valueOf(val);
								if (num <= 0) {
									err += "数字必须大于0！";
								} else {
									scr.put("numOfExaminee", num);
								}

							} catch (NumberFormatException e) {
								err += "数字输入不合法！";
							}
						}
						
					} else if (tit.equals("numOfTeacher")) {
						try {
							if(StringUtils.isNotEmpty(val)) {
								num = Integer.valueOf(val);
								if (num <= 0) {
									err += "数字必须大于0！";
								} else {
									scr.put("numOfTeacher", num);
								}
							}else {
								scr.put("numOfTeacher", null);
							}
						} catch (NumberFormatException e) {
							err += "数字输入不合法！";
						}
					} else if (tit.equals("buildingName")) {
						scr.put("buildingName", val);
					} else if (tit.equals("floor")) {
						scr.put("floor", val);
					} else if (tit.equals("roomName")) {
						scr.put("roomName", val);
					}
				if (err.length() > 0) {

					wsg.put("err", err);
					mrows.add(wsg);
				} else {
					wsg = null;
				}
			}

			if (isnameCfBug) {
				examPlaceNameMap.put(examPlaceName, rowNum+"");
			}
			if (iscodefBug) {
				examPlaceCodeMap.put(examPlaceCode, rowNum+"");

			}
			if (mrows.size() == 0) {

				if (iscover) {
					JSONObject dbdata = dbexamPlaceCodeMap.get(examPlaceCode);
					scr.put("examPlaceId", dbdata.getString("examPlaceId"));
				} else {
					scr.put("examPlaceId", UUIDUtil.getUUID());
				}
				scr.put("examPlaceCode", sysexamPlaceCode);

				scr.put("isup", "1");
				scr.put("examPlaceName", sysexamPlaceName);

				if (iscover) {
					coverlist.add(scr);
				} else {
					newlist.add(scr);
				}
			}

			// }
			if (mrows.size() > 0) {
				// 学号重复导致的校验不通过 不移除该学号键
				wrongMsg.add(wmsg);
			} else {
				wmsg = null;
			}

			data.put("progress", 20 + (int) ((float) 20 * index / pureDatas
					.keySet().size()));
			toFront.put("data", data);
		}

		preData.put("rowcolMap", rowcolMap);
		preData.put("examPlaceCodeMap", examPlaceCodeMap);
		preData.put("examPlaceNameMap", examPlaceNameMap);
		// Object prepDataMapKey = "examManage." + getXxdm(req)
		// + ".import.prepDataMap";
		redisOperationDAO.set(prepDataMapKey, prepDataMap,
				CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		if (wrongMsg != null && wrongMsg.size() > 0) {
			Collections.sort(wrongMsg,new Comparator<JSONObject>(){
	            public int compare(JSONObject arg0, JSONObject arg1) {
					return 	arg0.getIntValue("row")==arg1.getIntValue("row")?0:arg0.getIntValue("row")<arg1.getIntValue("row")?-1:1;
	            }
			});
			rs.put("ckRs", false);
			rs.put("wrongMsg", wrongMsg);
		} else {
			rs.put("ckRs", true);
		}
		rs.put("exDatas", exDatas);
		return rs;
	}

}