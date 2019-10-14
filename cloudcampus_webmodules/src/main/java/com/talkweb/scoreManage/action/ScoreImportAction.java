package com.talkweb.scoreManage.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.scoreManage.po.gm.ScoreInfo;
import com.talkweb.scoreManage.proc.ExcelErrorInfo;
import com.talkweb.scoreManage.proc.ExcelTitleInfo;
import com.talkweb.scoreManage.proc.ProgressBar;
import com.talkweb.scoreManage.proc.ScoreProgressProc;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.student.domain.page.StartImportTaskParam;

@Controller
@RequestMapping("/scoremanage1/import")
public class ScoreImportAction extends BaseAction {

	@Autowired
	private ScoreManageService scoreService;
	@Autowired
	private AllCommonDataService allCommonDataService;
	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	@Autowired
	private FileServer fileServerImplFastDFS;

	private static final Logger logger = LoggerFactory.getLogger(ScoreImportAction.class);

	@Autowired
	private FileImportInfoService fileImportInfoService;
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['tempFilePath']}")
	private String tempFilePath;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	private Object getRedisData(String key) throws Exception {
		redisOperationDAO.expire(key, CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
		return redisOperationDAO.get(key);
	}

	/**
	 * Excel上传接口
	 * 
	 * @param file
	 * @param req
	 * @param res
	 */
	@RequestMapping(value = "/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(@RequestParam("excelBody") MultipartFile file, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		setResponse(response, 1, "");

		Workbook workbook = null;
		try {
			String termInfoId = StringUtil.transformString(req.getParameter("termInfoId"));// 学年学期
			if (StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1002, "参数传递错误，请联系管理员！");
			}
			String schoolId = getXxdm(req);

			HttpSession session = req.getSession();
			String sessionId = session.getId();

			School school = getSchool(req, termInfoId);
			List<Map<String, Object>> kms = allCommonDataService.getAdminKM(school, termInfoId);

			String key = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.km").toString();
			session.setAttribute(key, kms);

			List<ExcelTitleInfo> excelTitleInfoList = new ArrayList<ExcelTitleInfo>(3 + kms.size());
			excelTitleInfoList.add(new ExcelTitleInfo("姓名", "xm", 1, -3L));
			excelTitleInfoList.add(new ExcelTitleInfo("学号", "userXh", 1, -2L));
			excelTitleInfoList.add(new ExcelTitleInfo("班级", "bjmc", 1, -1L));

			for (Map<String, Object> map : kms) {
				excelTitleInfoList.add(new ExcelTitleInfo((String) map.get("zwmc"), (String) map.get("kmdm"), 0,
						Long.parseLong(String.valueOf(map.get("kmdm")))));
			}

			Collections.sort(excelTitleInfoList, new Comparator<ExcelTitleInfo>() {
				@Override
				public int compare(ExcelTitleInfo e1, ExcelTitleInfo e2) {
					return Long.compare(e1.getSort(), e2.getSort());
				}
			});

			String excelTitleInfoKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.excelTitleInfo").toString();
			redisOperationDAO.set(excelTitleInfoKey, excelTitleInfoList,
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			// 获取源文件后缀名
			String fileName = file.getOriginalFilename();
			// 扩展名并转小写
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			// 文件名后缀转小写
			fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + prefix;
			byte[] bytes = file.getBytes();
			InputStream inputStream = new ByteArrayInputStream(bytes);

			String keyId = StringUtil.transformString(session.getAttribute("keyId")); // 文件主键值
			if (StringUtils.isEmpty(keyId)) {
				keyId = UUIDUtil.getUUID();
				session.setAttribute("keyId", keyId);
			}

			String fileId = fileImportInfoService.getFileBy(schoolId, keyId);
			if (StringUtils.isNotEmpty(fileId)) {
				fileServerImplFastDFS.deleteFile(fileId);
				fileImportInfoService.deleteFile(schoolId, keyId);
			}

			fileId = fileServerImplFastDFS.uploadFile(bytes, fileName);
			logger.info("fileId==>" + fileId);
			session.setAttribute("fileId", fileId);
			if (StringUtils.isNotEmpty(fileId)) {
				fileImportInfoService.addFile(schoolId, keyId, fileId);
			}

			if (prefix.equalsIgnoreCase("xls")) {
				workbook = new HSSFWorkbook(inputStream);
			} else if (prefix.equalsIgnoreCase("xlsx")) {
				workbook = new XSSFWorkbook(inputStream);
			} else {
				throw new CommonRunException(-2, "文件不是excel格式！");
			}

			Sheet sheet = workbook.getSheetAt(0);
			int rows = sheet.getPhysicalNumberOfRows();

			if (rows > 1) { // 第一行为表头，因此row应该大于1才有数据
				Row row = sheet.getRow(0);
				int cellNum = row.getPhysicalNumberOfCells();
				String[] excelTitleArray = new String[cellNum];

				// 判断是否需要进行手工字段匹配
				for (int i = 0; i < cellNum; i++) {
					String excelVal = row.getCell(i).getStringCellValue().trim();

					if (getExcelTitleInfoByName(excelVal, excelTitleInfoList) == null) {
						setResponse(response, 2, "文件格式正确，字段需要匹配！");
					}
					
					// 放置临时保存目录
					excelTitleArray[i] = excelVal;
				}

				Object excelTitleKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
						.append(".importScore.excelTitle").toString();
				redisOperationDAO.set(excelTitleKey, excelTitleArray,
						CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
			} else {
				throw new CommonRunException(-2102, "Excel里面没有数据，请重新导入excel文件！");
			}
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			setResponse(response, -2102, OutputMessage.getDescByCode(-2101 + ""));
			e.printStackTrace();
		} finally {
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return response;
	}

	private ExcelTitleInfo getExcelTitleInfoByName(String str, List<ExcelTitleInfo> list) {
		if (str == null) {
			return null;
		}
		for (ExcelTitleInfo excelTitleInfo : list) {
			if (excelTitleInfo.getSysTitleName() != null && excelTitleInfo.getSysTitleName().equalsIgnoreCase(str)) {
				return excelTitleInfo;
			}
		}
		return null;
	}

	/**
	 * 字符串在数组中的索引
	 * 
	 * @param string
	 * @param stutitle2
	 * @return
	 */
	private int strIndexInArray(String string, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != null && arr[i].equalsIgnoreCase(string)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获取Excel表头和系统字段
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelMatch(HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			JSONObject excelHead = new JSONObject();
			// 获取session中保存的临时表头
			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			String excelTitleInfoKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.excelTitleInfo").toString();
			List<ExcelTitleInfo> excelTitleInfoList = (List<ExcelTitleInfo>) redisOperationDAO.get(excelTitleInfoKey);

			String excelTitleKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.excelTitle").toString();
			String[] excelTitleArray = (String[]) redisOperationDAO.get(excelTitleKey);

			if (excelTitleArray == null || excelTitleInfoList == null) {
				throw new CommonRunException(-1, "缓存数据因超时已被删除，请重新导入excel数据！");
			}

			if (excelTitleArray != null) {
				// 开始拼装返回数据结构
				excelHead.put("total", excelTitleArray.length);
				JSONArray rows = new JSONArray();
				excelHead.put("rows", rows);
				for (int i = 0; i < excelTitleArray.length; i++) {
					JSONObject obj = new JSONObject();
					obj.put("field", excelTitleArray[i]);
					rows.add(obj);
				}
			}

			JSONObject moduleField = new JSONObject();
			// 直接使用系统表头 系统表头是否必填数组开始拼装返回数据结构
			moduleField.put("total", excelTitleInfoList.size());
			JSONArray rows = new JSONArray();
			moduleField.put("rows", rows);
			for (ExcelTitleInfo excelTitleInfo : excelTitleInfoList) {
				JSONObject obj = new JSONObject();
				obj.put("field", excelTitleInfo.getSysTitleName());
				obj.put("sysfield", excelTitleInfo.getSysTitleNeed());
				rows.add(obj);
			}

			response.put("excelHead", excelHead);
			response.put("moduleField", moduleField);
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1001, e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/startImportTask")
	@ResponseBody
	public synchronized JSONObject startImportTask(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) throws Exception {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("termInfoId");
			Integer isMatch = request.getInteger("isMatch");
			if (StringUtils.isBlank(termInfoId) || isMatch == null) {
				throw new CommonRunException(-1002, "参数传递错误，请联系管理员！");
			}

			String schoolId = getXxdm(req);
			request.put("school", allCommonDataService.getSchoolById(Long.parseLong(schoolId), termInfoId));
			String sessionId = req.getSession().getId();

			StartImportTaskParam taskParam = new StartImportTaskParam();
			taskParam.setIsMatch(isMatch);
			if (taskParam.getIsMatch() == 1) {
				taskParam.setMatchResult(request.getJSONArray("matchResult"));
			}

			int len = termInfoId.length() - 1;
			String xn = termInfoId.substring(0, len);
			String xqm = termInfoId.substring(len);
			// 根据使用年级、学年获取入学年度
			taskParam.setXn(xn);
			taskParam.setXq(xqm);
			taskParam.setXxdm(schoolId);
			taskParam.setKslc(request.getString("examId"));

			String taskParamKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.taskParam").toString();
			redisOperationDAO.set(taskParamKey, taskParam,
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			ProgressBar progressBar = new ProgressBar();
			progressBar.setCode(0);
			progressBar.setProgress(0);
			progressBar.setMsg("");
			String progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.progress").toString();
			redisOperationDAO.set(progressKey, progressBar,
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			// 设置获取的参数
			JSONObject prepData = new JSONObject();

			Object prepDataKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.prepData").toString();
			redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			Object excelTitleKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.excelTitle").toString();
			redisOperationDAO.expire(excelTitleKey, CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			JSONObject data = new JSONObject();
			data.put("progress", 0);
			response.put("data", data);

			String key = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.km").toString();
			List<Map<String, Object>> kms = (List<Map<String, Object>>) req.getSession().getAttribute(key);

			String keyId = StringUtil.transformString(req.getSession().getAttribute("keyId"));// 文件主键值

			new SubProcess(sessionId, 0, null, schoolId, kms, keyId,request,req,res).start();

			/*if(progressBar.getProgress()==50){
				request.put("progressBar", progressBar);
				new ScoreAlzAction().startResultAnalysisTask(request, req, res);
			}*/
			data.put("msg", "正常启动任务");
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "启动任务异常，请联系管理员！");

			JSONObject data = new JSONObject();
			data.put("progress", 0);
			data.put("msg", "启动任务失败");
			response.put("data", data);
		}

		return response;
	}

	/**
	 * 继续导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/continueImport")
	@ResponseBody
	public synchronized JSONObject continueImport(HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String xxdm = getXxdm(req);
		try {
			String sessionId = req.getSession().getId();

			String key = new StringBuffer().append("scoreManage.").append(getXxdm(req)).append(sessionId)
					.append(".importScore.km").toString();
			
			String progressKey = new StringBuffer("scoreManage.").append(xxdm).append(sessionId)
					.append(".importScore.progress").toString();
			ProgressBar progressBar = new ProgressBar();
			progressBar.setProgressInfo(1, 0, "继续导入！");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

			List<Map<String, Object>> kms = (List<Map<String, Object>>) req.getSession().getAttribute(key);
			String keyId = StringUtil.transformString(req.getSession().getAttribute("keyId"));// 文件主键值

			new SubProcess(sessionId, 1, null, getXxdm(req), kms, keyId,null,req,null).start();

			setResponse(response, 1, "正常启动！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "启动任务异常，请联系管理员！");
		}
		return response;

	}

	/**
	 * 导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/singleDataCheck")
	@ResponseBody
	public synchronized JSONObject singleDataCheck(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) throws Exception {
		JSONObject rs = new JSONObject();
		JSONObject data = new JSONObject();
		String sessionId = req.getSession().getId();
		String xxdm = getXxdm(req);
		try {
			int row = request.getIntValue("row");
			rs.put("rowNum", row);
			JSONArray mrows = request.getJSONArray("mrows");

			String progressKey = new StringBuffer("scoreManage.").append(xxdm).append(sessionId)
					.append(".importScore.progress").toString();
			ProgressBar progressBar = (ProgressBar) redisOperationDAO.get(progressKey);

			String prepDataKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
					.append(".importScore.prepData").toString();
			JSONObject prepData = (JSONObject) getRedisData(prepDataKey);

			if (null == progressBar || null == prepData) {
				progressBar.setCode(-50);
				progressBar.setMsg("由于长时间未操作，数据已过期，请重新导入!");
				progressBar.setProgress(100);
				redisOperationDAO.set(progressKey, progressBar,
						CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

				data.put("progress", progressBar.getProgress());
				data.put("msg", progressBar.getMsg());
				rs.put("code", progressBar.getCode());
				rs.put("data", data);
				return rs;
			}

			Map<Integer, JSONObject> rowDatas = (Map<Integer, JSONObject>) prepData.get("rowDatas");
			Map<String, String> titleEnMap = (Map<String, String>) prepData.get("titleEnMap");

			int code = request.getIntValue("code");
			if (code == -1) {
				rowDatas.remove(row);
				prepData.put("rowDatas", rowDatas);
				redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				
				rs.put("code", 1);
			} else if (code == 1) {
				String taskParamKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
						.append(".importScore.taskParam").toString();
				StartImportTaskParam sp = (StartImportTaskParam) getRedisData(taskParamKey);

				int isMatch = sp.getIsMatch();
				JSONArray mrs = sp.getMatchResult();

				Map<Integer, JSONObject> pureDatas = new HashMap<Integer, JSONObject>();
				JSONObject sd = rowDatas.get(row);
				for (int i = 0; i < mrows.size(); i++) {
					JSONObject o = mrows.getJSONObject(i);
					sd.put(titleEnMap.get(o.getString("title")), o.getString("value"));
				}

				pureDatas.put(row, sd);
				rowDatas.put(row, sd);
				prepData.put("rowDatas", rowDatas);

				redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

				String key = new StringBuffer().append("scoreManage.").append(getXxdm(req)).append(sessionId)
						.append(".importScore.km").toString();
				List<Map<String, Object>> kms = (List<Map<String, Object>>) req.getSession().getAttribute(key);
				JSONObject cr = checkImpData(pureDatas, mrs, isMatch, sessionId, sp, kms, true);

				if (cr.getBooleanValue("ckRs")) {
					rs.put("code", 1);
					data.put("msg", "校验通过！");
					rs.put("data", data);
				} else {
					rs.put("code", -1);
					data.put("msg", "校验不通过！");
					rs.put("data", data);
					rs.put("mrows", cr.getJSONArray("wrongMsg").getJSONObject(0).get("mrows"));
				}
			}
			
		} catch (CommonRunException e) {
			rs.put("code", e.getCode());
			data.put("msg", e.getMessage());
			rs.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			rs.put("code", -1);
			data.put("msg", "后台系统异常，请联系管理员！");
			rs.put("data", data);
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
	public ProgressBar importProgress(HttpServletRequest req, HttpServletResponse res) throws Exception {
		ProgressBar progressBar = new ProgressBar();
		progressBar.setProgressInfo(1, 0, "");

		String schoolId = getXxdm(req);
		String sessionId = req.getSession().getId();

		try {
			String progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.progress").toString();

			progressBar = (ProgressBar) getRedisData(progressKey);

			if (null == progressBar) {
				progressBar = new ProgressBar();
				progressBar.setProgressInfo(-50, 100, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
			}

			if (-50 == progressBar.getCode()) {
				String keyId = req.getSession().getAttribute("keyId").toString();
				if (StringUtils.isNotBlank(keyId)) {
					String fileId = fileImportInfoService.getFileBy(schoolId, keyId);
					if (StringUtils.isNotBlank(fileId)) {
						fileServerImplFastDFS.deleteFile(fileId);
					}
					fileImportInfoService.deleteFile(schoolId, keyId);
				}
			}
		} catch (Exception e) {
			progressBar.setMsg("后台异常，请联系管理员！");
			progressBar.setCode(-1);
			e.printStackTrace();
		}

		return progressBar;
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
		ExcelTool.exportExcelWithData(excelData, excelHeads, "错误信息", null, req, res);
	}

	class SubProcess extends Thread {
		private String sessionId = null;
		private String xxdm = null;
		List<Map<String, Object>> kms = null;
		private int impType; // 0：初次执行，1：由于有错误，修改后继续执行
		private String keyId = null;

		private JSONObject singleData;

		private String progressKey = null;
		private String prepDataKey = null;

		private String taskParamKey = null;

		private JSONObject request;
		private HttpServletRequest req;
		private HttpServletResponse res;
		private String fileId ;
		//session.setAttribute("fileId", fileId);
		public SubProcess(String sessionId, int impType, JSONObject singleData, String xxdm,
				List<Map<String, Object>> kms, String keyId, JSONObject request, HttpServletRequest req, HttpServletResponse res) {
			this.sessionId = sessionId;
			this.impType = impType;
			this.singleData = singleData;
			this.kms = kms;
			this.xxdm = xxdm;
			this.keyId = keyId;

			this.req=req;
			this.request=request;
			this.res=res;
			
			progressKey = new StringBuffer("scoreManage.").append(xxdm).append(sessionId)
					.append(".importScore.progress").toString();

			prepDataKey = new StringBuffer("scoreManage.").append(xxdm).append(sessionId)
					.append(".importScore.prepData").toString();

			taskParamKey = new StringBuffer("scoreManage.").append(xxdm).append(sessionId)
					.append(".importScore.taskParam").toString();
			
 
			fileId =(String)req.getSession().getAttribute("fileId");// 这个不能删  continue 继续导入的时候会清掉数据
		}

		@Override
		public void run() {
			ProgressBar progressBar = new ProgressBar();

			String fileIdTmp = fileImportInfoService.getFileBy(xxdm, keyId);
			if (fileIdTmp!=null) {
				fileId = fileIdTmp;
			}
			logger.info(" fileId==> " + fileId);
			try {
				StartImportTaskParam taskParam = (StartImportTaskParam) getRedisData(taskParamKey);
				progressBar = (ProgressBar) getRedisData(progressKey);
				if (progressBar == null || taskParam == null) {
					throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
				}

				int isMatch = taskParam.getIsMatch();

				progressBar.setProgressInfo(1, 0, "正在准备导入任务");
				redisOperationDAO.set(progressKey, progressBar,
						CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

				JSONArray datas = new JSONArray();
				if (impType == 0) {
					datas = readExcelToData(fileId);
					if (CollectionUtils.isEmpty(datas)) {
						throw new CommonRunException(-1, "excel中没有数据，请重新上传文件！");
					}
				}

				// 开始校验
				JSONObject ckrs = new JSONObject();
				ckrs.put("ckRs", true);
				if (impType == 0) {	// impType：0：第一次导入数据，1：继续导入数据
					progressBar.setProgressInfo(1, 5, "正在校验excel数据");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					JSONArray matchResult = taskParam.getMatchResult();
					// 组装数据
					Map<Integer, JSONObject> rowDatas = changeData(datas, matchResult, isMatch, sessionId, xxdm, kms);
					// 检测数据
					ckrs = checkImpData(rowDatas, matchResult, isMatch, sessionId, taskParam, kms, false);
				}

				JSONObject prepData = (JSONObject) getRedisData(prepDataKey);
				if (ckrs.getBooleanValue("ckRs") || impType == 1) { // 校验通过，或者由于错误修复之后继续执行
					// 开始入库
					progressBar.setProgressInfo(1, 40, "正在保存数据！");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					List<ScoreInfo> scoreList = getInsertEntityByCkrs(taskParam, sessionId);

					ScoreProgressProc spThread = null;
					if (scoreList.size() > 0) {
						spThread = new ScoreProgressProc("正在保存数据！", 1, 6, 5, 40, progressKey);
						spThread.start();

						scoreService.insertScoreInfoBatch(scoreList, taskParam);
					}

					String[] userKms = (String[]) prepData.get("userKms");
					int nums = datas.size() - 1;
					Set<String> stuXhSet = new HashSet<String>();
					if (impType == 1) {
						for (ScoreInfo sc : scoreList) {
							stuXhSet.add(sc.getXh());
						}
					}

					int a = stuXhSet.isEmpty() ? nums : stuXhSet.size();

					progressBar.setProgressInfo(2, 100, "导入成功，共计导入" + a + "位学生*" + userKms.length + "个科目成绩数据！");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					/*// 开始入库  邓志华注释掉
					progressBar.setProgressInfo(1, 40/2, "正在保存数据！");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					List<ScoreInfo> scoreList = getInsertEntityByCkrs(taskParam, sessionId);

					ScoreProgressProc spThread = null;
					if (scoreList.size() > 0) {
						spThread = new ScoreProgressProc("正在保存数据！", 1, 6, 5, 40/2, progressKey);
						spThread.start();

						scoreService.insertScoreInfoBatch(scoreList, taskParam);
					}

					String[] userKms = (String[]) prepData.get("userKms");
					int nums = datas.size() - 1;
					Set<String> stuXhSet = new HashSet<String>();
					if (impType == 1) {
						for (ScoreInfo sc : scoreList) {
							stuXhSet.add(sc.getXh());
						}
					}

					int a = stuXhSet.isEmpty() ? nums : stuXhSet.size();

					progressBar.setProgressInfo(1, 100/2, "导入成功，共计导入" + a + "位学生*" + userKms.length + "个科目成绩数据！");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					
					request.put("progressBar", progressBar);
					request.put("sessionId", sessionId);
					request.put("xxdm", xxdm);
					request.put("redisOperationDAO", redisOperationDAO);
					//request.put("school", request.get("school"));
					new ScoreAlzAction().startResultAnalysisTask(request, req, res);*/
				} else {
					progressBar.setProgressInfo(-2, 100, "Excel数据校验不通过!");
					if (impType == 0) {
						progressBar.setOtherData("total", datas.size() - 1);
					} else {
						progressBar.setOtherData("total", 1);
					}

					JSONObject validateMsg = new JSONObject();
					validateMsg.put("total", ckrs.getJSONArray("wrongMsg").size());
					validateMsg.put("rows", ckrs.getJSONArray("wrongMsg"));
					progressBar.setOtherData("validateMsg", validateMsg);
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					redisOperationDAO.set(prepDataKey, prepData,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
				}
			} catch (CommonRunException e) {
				progressBar.setCode(e.getCode());
				progressBar.setMsg(e.getMessage());
				progressBar.setProgress(100);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
				progressBar.setCode(-1);
				progressBar.setMsg("导入失败，错误未知，请稍后重试或联系管理员!");
				progressBar.setProgress(100);
			} finally {
				// excel导入处理结束
				try {
					redisOperationDAO.expire(progressKey,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
					redisOperationDAO.expire(prepDataKey,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

					String excelTitleKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
							.append(".importScore.excelTitle").toString();
					redisOperationDAO.expire(excelTitleKey,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

					if (fileId != null) {
						fileServerImplFastDFS.deleteFile(fileId);
						fileImportInfoService.deleteFileByFileId(fileId);
					}
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
	 * @param sessionId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<ScoreInfo> getInsertEntityByCkrs(StartImportTaskParam sp, String sessionId) throws Exception {
		// TODO Auto-generated method stub
		String xq = sp.getXq();
		String xn = sp.getXn();
		String xxdm = sp.getXxdm();
		// 使用为考试轮次代码
		String kslcdm = sp.getKslc();

		String prepDataKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
				.append(".importScore.prepData").toString();

		JSONObject prepData = (JSONObject) getRedisData(prepDataKey);
		if (null == prepData) {
			throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
		}

		Map<Integer, List<ScoreInfo>> rows = (Map<Integer, List<ScoreInfo>>) prepData.get("rowcolMap");

		List<ScoreInfo> scoreList = new ArrayList<ScoreInfo>();
		for (Map.Entry<Integer, List<ScoreInfo>> entry : rows.entrySet()) {
			List<ScoreInfo> rowList = entry.getValue();
			for (ScoreInfo row : rowList) {
				row.setKslc(kslcdm);
				row.setXnxq(xn + xq);
				row.setXxdm(xxdm);
				scoreList.add(row);
			}
		}

		return scoreList;
	}

	/**
	 * 根据映射字段映射生成数据
	 * 
	 * @param datas
	 * @param mrs
	 * @param isMatch
	 * @param sessionId2
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer, JSONObject> changeData(JSONArray datas, JSONArray matchResult, int isMatch, String sessionId,
			String xxdm, List<Map<String, Object>> kms) throws Exception {
		String excelTitleInfoKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
				.append(".importScore.excelTitleInfo").toString();
		List<ExcelTitleInfo> excelTitleInfoList = (List<ExcelTitleInfo>) redisOperationDAO.get(excelTitleInfoKey);

		if (excelTitleInfoKey == null) {
			throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
		}

		List<String> kmdms = new ArrayList<String>();
		for (Map<String, Object> map : kms) {
			kmdms.add((String) map.get("kmdm"));
		}

		String[] excelTitles = (String[]) datas.get(0);
		// 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射实体字段[3]
		String[][] arr = new String[4][excelTitles.length];
		// 处理表头数据
		Map<String, String> titleEnMap = new HashMap<String, String>();
		for (int i = 0; i < excelTitles.length; i++) {
			arr[0][i] = excelTitles[i]; // excel的表头的一个单元

			String sysTit = null;
			if (isMatch == 0) { // 无需手工匹配，系统自动匹配好了
				sysTit = excelTitles[i]; // 已经和系统匹配成功了
			} else { // 需要手工匹配的，根据匹配关系封装表头
				for (int j = 0; j < matchResult.size(); j++) {
					JSONObject obj = matchResult.getJSONObject(j);
					if (excelTitles[i] != null && excelTitles[i].equalsIgnoreCase(obj.getString("excelField"))) {
						sysTit = obj.getString("sysField"); // 手动匹配的关系
						break;
					}
				}
			}

			ExcelTitleInfo excelTitleInfo = getExcelTitleInfoByName(sysTit, excelTitleInfoList);
			if (excelTitleInfo != null) {
				// 在系统字段中能找到
				arr[1][i] = excelTitleInfo.getSysTitleNeed() + "";
				arr[2][i] = excelTitleInfo.getSysTitleName();
				arr[3][i] = excelTitleInfo.getSysTitleCode();

				titleEnMap.put(arr[0][i], arr[3][i]);
			} else {
				// 在系统字段中找不到 标记为不录入
				arr[1][i] = "-1";
				arr[2][i] = "none";
				arr[0][i] = null;
			}
		}

		// 处理excel行数据
		Map<Integer, JSONObject> rowDatas = new HashMap<Integer, JSONObject>();
		for (int i = 1; i < datas.size(); i++) {
			JSONObject d = new JSONObject();
			String[] cell = (String[]) datas.get(i);
			for (int j = 0; j < arr[0].length - 1; j++) {
				int isNeed = Integer.parseInt(arr[1][j]); // 0：不是必须的，1：必须的，-1：不存在title
				if (isNeed != -1) {
					// 放入数据 （excel映射json）
					String cellVal = cell[j];
					cellVal = cellVal != null ? cellVal.replaceAll(" ", "") : null;
					d.put(arr[3][j], cellVal); // 科目代码对应分数
					d.put(arr[3][j] + "Name", arr[0][j]); // 科目代码对应科目名称
				}
			}

			int rowNum = Integer.parseInt(cell[arr[0].length - 1]) + 1; // 行号
			d.put("rowNum", rowNum);
			rowDatas.put(rowNum, d);
		}

		List<String> userKms = new ArrayList<String>();
		for (int i = 0; i < arr[3].length; i++) {
			if (kmdms.contains(arr[3][i])) {
				userKms.add(arr[3][i]);
			}
		}

		int xmIndex = strIndexInArray("xm", arr[3]); // 姓名
		int xjhIndex = strIndexInArray("xjh", arr[3]); // 学籍号
		int xhIndex = strIndexInArray("userXh", arr[3]); // 学号

		String bs = ""; // 标识
		int bsix = 0;
		if (xjhIndex != -1) { // 学籍号
			bs = "xjh";
			bsix = xjhIndex;
		} else if (xhIndex != -1) { // 学号
			bs = "userXh";
			bsix = xhIndex;
		} else if (xmIndex != -1) { // 姓名
			bs = "xm";
			bsix = xmIndex;
		}
		JSONObject rs = new JSONObject();
		rs.put("rowDatas", rowDatas);
		rs.put("titleEnMap", titleEnMap);
		rs.put("userKms", userKms.toArray(new String[0]));
		rs.put("bs", bs); // 标识
		rs.put("bsix", bsix);
		rs.put("impHead", arr[3]);

		String prepDataKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
				.append(".importScore.prepData").toString();
		redisOperationDAO.set(prepDataKey, rs, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		return rowDatas;
	}

	/**
	 * 将excel读取为数据
	 * 
	 * @param impFrc
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	private JSONArray readExcelToData(String fileId) throws IOException, Exception {
		// TODO Auto-generated method stub
		JSONArray datas = new JSONArray();
		// 解析excel 封装对象
		byte[] bytes = fileServerImplFastDFS.downloadFile(fileId);
		if (bytes == null || bytes.length == 0) {
			throw new CommonRunException(-1, "excel文件已被删除，请重新上传并导入数据！");
		}
		
		Workbook workbook = null;
		if (fileId.endsWith("xls")) {
			workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes));
		} else if (fileId.endsWith("xlsx")) {
			workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
		} else {
			workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes));
		}

		Sheet sheet = workbook.getSheetAt(0);
		int rows = sheet.getLastRowNum();
		DecimalFormat df = new DecimalFormat("0.0");

		if (rows > 0) {
			int cols = sheet.getRow(0).getPhysicalNumberOfCells();
			for (int i = 0; i <= rows; i++) {
				if (sheet.getRow(i) == null) {
					continue;
				}

				boolean isTrueNull = true;
				String[] temp = new String[cols + 1];
				for (int j = 0; j < cols; j++) {
					if (sheet.getRow(i).getCell(j) != null
							&& sheet.getRow(i).getCell(j).getCellType() != HSSFCell.CELL_TYPE_BLANK) {
						Cell cell = sheet.getRow(i).getCell(j);
						switch (cell.getCellType()) {
						case HSSFCell.CELL_TYPE_NUMERIC:
							temp[j] = df.format(cell.getNumericCellValue());
							if (df.format(cell.getNumericCellValue()).endsWith(".0")) {
								temp[j] = df.format(cell.getNumericCellValue()).split("\\.")[0];
							}
							break;
						case HSSFCell.CELL_TYPE_STRING:
							temp[j] = cell.getRichStringCellValue().getString().trim();
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							try {
								temp[j] = String.valueOf(cell.getStringCellValue());
							} catch (IllegalStateException e) {
								temp[j] = String.valueOf(cell.getNumericCellValue());
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

		workbook.close();
		return datas;
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
	@SuppressWarnings("unchecked")
	private JSONObject checkImpData(Map<Integer, JSONObject> rowDatas, JSONArray matchResult, int isMatch,
			String sessionId, StartImportTaskParam sp, List<Map<String, Object>> kms, boolean isSingleDataCheck)
			throws Exception {
		String schoolId = sp.getXxdm();

		String progressKey = new StringBuffer("scoreManage.").append(schoolId).append(sessionId)
				.append(".importScore.progress").toString();

		String prepDataKey = new StringBuffer("scoreManage.").append(schoolId).append(sessionId)
				.append(".importScore.prepData").toString();

		ProgressBar progressBar = (ProgressBar) redisOperationDAO.get(progressKey);
		JSONObject prepData = (JSONObject) redisOperationDAO.get(prepDataKey);
		if (progressBar == null || prepData == null) {
			throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
		}

		String xq = sp.getXq();
		String xn = sp.getXn();

		// excel验证部分
		JSONObject rs = new JSONObject();

		Set<String> kmdms = new HashSet<String>();
		for (int i = 0; i < kms.size(); i++) {
			kmdms.add((String) kms.get(i).get("kmdm"));
		}

		progressBar.setProgressInfo(1, "正在校验excel数据,匹配表头完成,校验数据");
		redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

		// 学号映射表
		Map<String, JSONObject> xhMap = new HashMap<String, JSONObject>();
		if (prepData.containsKey("xhMap")) {
			xhMap = (Map<String, JSONObject>) prepData.get("xhMap");
		}
		// 学籍号映射表
		Set<String> xjhSet = new HashSet<String>();
		if (prepData.containsKey("xjhSet")) {
			xjhSet = (Set<String>) prepData.get("xjhSet");
		}
		// 姓名班级对应的映射表
		Map<String, JSONObject> xmbjMap = new HashMap<String, JSONObject>();
		if (prepData.containsKey("xmbjMap")) {
			xmbjMap = (Map<String, JSONObject>) prepData.get("xmbjMap");
		}

		// 对比
		JSONObject dbckMap = new JSONObject();
		if (prepData.containsKey("dbckMap")) {
			dbckMap = prepData.getJSONObject("dbckMap");
		} else {
			JSONObject map = new JSONObject();
			map.put("xxdm", schoolId);
			map.put("xn", xn);
			map.put("xnxq", xn + xq);

			ScoreProgressProc spThread = new ScoreProgressProc("正在查询学生数据...", 1, 5, 5, 6, progressKey);
			spThread.start();

			dbckMap = scoreService.getAllStuByParam(map);
			prepData.put("dbckMap", dbckMap);
		}

		progressBar.setProgressInfo(1, 32, "获取对比数据完成，正在校验excel数据 ");
		redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

		Map<String, JSONObject> dbxhMap = (Map<String, JSONObject>) dbckMap.get("xhMap"); // 学号映射表
		Map<String, JSONObject> dbxjhMap = (Map<String, JSONObject>) dbckMap.get("xjhMap"); // accId映射表
		Map<String, List<JSONObject>> dbxmbjMap = (Map<String, List<JSONObject>>) dbckMap.get("xmbjMap"); // 姓名班级映射表
		Map<String, Classroom> bjMap = (Map<String, Classroom>) dbckMap.get("bjMap"); // 班级名称映射表
		Map<String, List<JSONObject>> dbxmMap = (Map<String, List<JSONObject>>) dbckMap.get("xmMap"); // 姓名映射表

		// ["bjmc","xm","1","3","2","4","5","7","8",null]
		String[] impHead = (String[]) prepData.get("impHead");
		String bs = prepData.getString("bs"); // 标识代码，能够识别唯一的学生，例如：xm、userXh

		Map<Integer, List<ScoreInfo>> rowcolMap = null;
		if (prepData.containsKey("rowcolMap")) {
			rowcolMap = (Map<Integer, List<ScoreInfo>>) prepData.get("rowcolMap");
		} else {
			rowcolMap = new HashMap<Integer, List<ScoreInfo>>();
			prepData.put("rowcolMap", rowcolMap);
		}

		JSONArray wrongMsg = new JSONArray();
		int index = 0;
		for (Map.Entry<Integer, JSONObject> entry : rowDatas.entrySet()) {
			index++;
			int rowNum = entry.getKey();
			JSONObject pd = entry.getValue();

			// 单条时直接覆盖 批量时也不会重复 行号不重复的话
			List<ScoreInfo> etyList = new ArrayList<ScoreInfo>();
			rowcolMap.put(rowNum, etyList);

			JSONObject wmsg = new JSONObject();
			wmsg.put("row", rowNum); // 行号
			wmsg.put("bs", pd.getString(bs + "Name")); // 标识，表头名称，例如：学号、学籍号
			wmsg.put("mrows", new JSONArray());

			// 姓名
			String xm = pd.getString("xm");
			xm = xm == null ? "" : xm;
			// 学号
			String userXh = pd.getString("userXh");
			// 学籍号
			String xjh = pd.getString("xjh");
			// 班级名称
			String bjmc = pd.getString("bjmc");
			bjmc = bjmc == null ? "" : bjmc;

			// 关键字段对应的内容，例如：学号、学籍号、姓名
			String bsContent = pd.getString(bs);
			String bsTitle = pd.containsKey(bs + "Name") ? pd.getString(bs + "Name") : "";

			if (StringUtils.isBlank(bsContent)) {
				JSONArray mrows = wmsg.getJSONArray("mrows");
				mrows.add(new ExcelErrorInfo(bsTitle, bs, bsContent, "标识不能为空！"));
				wrongMsg.add(wmsg);
				continue;
			}

			// 学号重复
			boolean isXhCfBug = false;
			// 姓名重复
			boolean isXmBjCfBug = false;

			String accId = null;

			// 校验标识，userXh标识，xm标识
			if (bs.equalsIgnoreCase("userXh")) {
				// 标识为学号
				JSONObject result = checkIdentifierXh(xm, userXh, bjmc, bsTitle, pd, xhMap, dbxhMap, wmsg);
				if (result.containsKey("isXhCfBug")) {
					isXhCfBug = result.getBooleanValue("isXhCfBug");
				}
				if (result.containsKey("accId")) {
					accId = result.getString("accId");
				}
				if (!isSingleDataCheck && result.containsKey("wmsg")) {
					// 正常导入流程，将会把全部的excel重复数据显示出来，单数据监测则只检测单次数据是否合法
					wrongMsg.add(result.get("wmsg"));
				}
			} else if (bs.equalsIgnoreCase("xm")) { // 标识为姓名
				JSONObject result = checkIdentifierXm(xm, userXh, bjmc, bsTitle, bs, pd, xmbjMap, dbxmbjMap, bjMap,
						dbxmMap, wmsg);
				if (result.containsKey("isXmCfBug")) {
					isXmBjCfBug = result.getBooleanValue("isXmBjCfBug");
				}
				if (result.containsKey("accId")) {
					accId = result.getString("accId");
				}
				if (!isSingleDataCheck && result.containsKey("wmsg")) {
					// 正常导入流程，将会把全部的excel重复数据显示出来，单数据监测则只检测单次数据是否合法
					wrongMsg.add(result.get("wmsg"));
				}
			}

			for (int j = 0; j < impHead.length; j++) {
				if (impHead[j] == null || !kmdms.contains(impHead[j])) {
					continue;
				}

				String titleCode = impHead[j];
				String val = pd.containsKey(titleCode) ? pd.getString(titleCode) : "";
				if (titleCode.equalsIgnoreCase("xm")) {
					val = val.replaceAll(" ", "");
				}

				String titleName = pd.getString(titleCode + "Name");
				// 成绩字段
				Float cj = null;
				String tsqk = null;

				if (val.equals("舞弊") || val.equals("缺考")) {
					tsqk = val;
				} else {
					try {
						if (StringUtils.isNotBlank(val)) {
							cj = ScoreUtil.castFloatTowPointNum(Float.parseFloat(val));
							if(cj < 0) {
								wmsg.getJSONArray("mrows").add(new ExcelErrorInfo(titleName, titleCode, val, "成绩不能小于0！"));
								continue;
							}
						}
					} catch (NumberFormatException e) {
						wmsg.getJSONArray("mrows").add(new ExcelErrorInfo(titleName, titleCode, val, "分值输入不合法！"));
						continue;
					}
				}

				if (wmsg.getJSONArray("mrows").size() == 0 && accId.trim().length() > 0) {
					JSONObject stu = dbxjhMap.get(accId);

					ScoreInfo scoreInfo = new ScoreInfo();
					scoreInfo.setTsqk(tsqk); // 特殊情况，缺考，舞弊
					scoreInfo.setCj(cj); // 成绩
					scoreInfo.setXh(stu.getString("xh")); // 学号
					scoreInfo.setNj(stu.getString("nj")); // 年级
					scoreInfo.setBh(stu.getString("bh")); // 班号
					scoreInfo.setKmdm(titleCode); // 科目代码

					etyList.add(scoreInfo);
				}
			}

			if (wmsg.getJSONArray("mrows").size() > 0) {
				if (!isSingleDataCheck && !isXhCfBug) {
					// 正常导入流程，将会把全部的excel重复数据显示出来，单数据监测则只检测单次数据是否合法
					xhMap.remove(userXh);
				}
				if (!isSingleDataCheck && !isXmBjCfBug) {
					// 正常导入流程，将会把全部的excel重复数据显示出来，单数据监测则只检测单次数据是否合法
					xmbjMap.remove(xm + bjmc);
				}
				wrongMsg.add(wmsg);
			}

			progressBar.setProgressInfo(1, 32 + (int) ((float) 20 * index / rowDatas.keySet().size()),
					"正在校验excel数据,匹配表头完成,校验数据");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		}

		prepData.put("rowcolMap", rowcolMap);
		prepData.put("xmbjMap", xmbjMap);
		prepData.put("xhMap", xhMap);

		redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		if (CollectionUtils.isNotEmpty(wrongMsg)) {
			rs.put("ckRs", false);
			Collections.sort(wrongMsg, new Comparator<Object>() {
				@Override
				public int compare(Object arg0, Object arg1) {
					int rowNum0 = ((JSONObject) arg0).getIntValue("row");
					int rowNum1 = ((JSONObject) arg1).getIntValue("row");
					return Integer.compare(rowNum0, rowNum1);
				}
			});
			rs.put("wrongMsg", wrongMsg);
		} else {
			rs.put("ckRs", true);
		}

		JSONArray exDatas = new JSONArray();
		rs.put("exDatas", exDatas);

		return rs;
	}

	private JSONObject checkIdentifierXh(String xm, String userXh, String bjmc, String bsTitle, JSONObject pd,
			Map<String, JSONObject> xhMap, Map<String, JSONObject> dbxhMap, JSONObject wmsg) {
		JSONObject result = new JSONObject();
		JSONArray mrows = wmsg.getJSONArray("mrows");

		if (StringUtils.isBlank(userXh)) {
			result.put("isXhCfBug", true);
			mrows.add(new ExcelErrorInfo(bsTitle, userXh, "不能为空！"));
			return result;
		}

		if (xhMap.containsKey(userXh)) { // 如果xhSet中已经存在userXh
			result.put("isXhCfBug", true);
			mrows.add(new ExcelErrorInfo(bsTitle, userXh, "excel中重复！"));

			JSONObject wmsg1 = xhMap.get(userXh);
			wmsg1.getJSONArray("mrows").add(new ExcelErrorInfo(bsTitle, userXh, "excel中重复！"));
			result.put("wmsg", wmsg1);

			return result;
		}
		xhMap.put(userXh, wmsg); // 不存在则放入，继续判断

		if (!dbxhMap.containsKey(userXh)) {
			mrows.add(new ExcelErrorInfo(bsTitle, userXh, "无匹配记录！"));
			return result;
		}

		JSONObject stuInfo = dbxhMap.get(userXh);

		if (StringUtils.isNotEmpty(xm) && !xm.equalsIgnoreCase(stuInfo.getString("xm"))) {
			mrows.add(new ExcelErrorInfo(pd.getString("xmName"), xm, "与系统不匹配！"));
			return result;
		}

		if (StringUtils.isNotEmpty(bjmc) && !bjmc.equalsIgnoreCase(stuInfo.getString("bjmc"))) {
			mrows.add(new ExcelErrorInfo(pd.getString("bjmcName"), bjmc, "与系统不匹配！"));
			return result;
		}

		result.put("accId", stuInfo.getString("userId"));
		return result;
	}

	private JSONObject checkIdentifierXm(String xm, String userXh, String bjmc, String bsTitle, String bs,
			JSONObject pd, Map<String, JSONObject> xmbjMap, Map<String, List<JSONObject>> dbxmbjMap,
			Map<String, Classroom> bjMap, Map<String, List<JSONObject>> dbxmMap, JSONObject wmsg) {
		JSONObject result = new JSONObject();

		JSONArray mrows = wmsg.getJSONArray("mrows");

		if (StringUtils.isBlank(xm)) {
			result.put("isXmBjCfBug", true);
			mrows.add(new ExcelErrorInfo(bs, xm, "不能为空！"));
			return result;
		}

		String key = xm + bjmc;
		if (xmbjMap.containsKey(key)) { // 如果
			result.put("isXmBjCfBug", true);
			mrows.add(new ExcelErrorInfo(bsTitle, xm, "excel中重复！"));
			mrows.add(new ExcelErrorInfo(pd.getString("bjmcName"), bjmc, "excel中重复！"));

			JSONObject wmsg1 = xmbjMap.get(key);
			wmsg1.getJSONArray("mrows").add(new ExcelErrorInfo(bsTitle, xm, "excel中重复！"));
			wmsg1.getJSONArray("mrows").add(new ExcelErrorInfo(pd.getString("bjmcName"), bjmc, "excel中重复！"));
			result.put("wmsg", wmsg1);

			return result;
		}
		xmbjMap.put(key, wmsg);

		if (StringUtils.isBlank(bjmc)) {
			// 如果班级为空字符串，则只通过姓名获取数据
			List<JSONObject> stuList = dbxmMap.get(xm);
			if (stuList == null) {
				mrows.add(new ExcelErrorInfo(pd.getString("xmName"), pd.getString("xm"), "无匹配记录！"));
				return result;
			}

			if (stuList.size() != 1) {
				mrows.add(new ExcelErrorInfo(pd.getString("xmName"), pd.getString("xm"), "匹配到多条记录！"));
				return result;
			}

			result.put("accId", stuList.get(0).getString("userId"));
			return result;
		}

		if (dbxmMap.containsKey(xm) && dbxmbjMap.containsKey(xm + bjmc)) {
			List<JSONObject> sysObj = dbxmbjMap.get(xm + bjmc);
			if (sysObj.size() > 1) { // 同一个班级拥有两个相同名字的学生，提示错误
				mrows.add(new ExcelErrorInfo(pd.getString("xmName"), pd.getString("xm"), "系统中存在多条该信息！"));
				return result;
			}

			if (userXh == null) { // 判断是否有学号字段，没有则获取accId
				result.put("accId", sysObj.get(0).getString("userId"));
				return result;
			}

			// 存有学号，判断学号是否和数据库中的学号相同，不相同提示错误
			if (!userXh.equalsIgnoreCase(sysObj.get(0).getString("userXh"))) {
				mrows.add(new ExcelErrorInfo(pd.getString("userXhName"), pd.getString("userXh"), "与系统不匹配！"));
				return result;
			}

			result.put("accId", sysObj.get(0).getString("userId"));
		} else if (dbxmMap.containsKey(xm) && !dbxmbjMap.containsKey(xm + bjmc)) {
			mrows.add(new ExcelErrorInfo(pd.getString("bjmcName"), pd.getString("bjmc"), "与系统不匹配！"));
		} else if (!dbxmMap.containsKey(xm) && bjMap.containsKey(bjmc)) {
			// xm不存在，那么xm + bjmc一定不存在，因此这里需要判断bjmc是否存在
			mrows.add(new ExcelErrorInfo(pd.getString("xmName"), pd.getString("xm"), "与系统不匹配！"));
		} else if (!dbxmMap.containsKey(xm) && !bjMap.containsKey(bjmc)) {
			// xm不存在，那么xm + bjmc一定不存在，因此这里需要判断bjmc是否存在
			mrows.add(new ExcelErrorInfo(pd.getString("bjmcName"), pd.getString("bjmc"), "与系统不匹配！"));
			mrows.add(new ExcelErrorInfo(pd.getString("xmName"), pd.getString("xm"), "与系统不匹配！"));
		} else {
			mrows.add(new ExcelErrorInfo(pd.getString("xmName"), pd.getString("xm"), "无匹配记录！"));
		}
		return result;
	}
}