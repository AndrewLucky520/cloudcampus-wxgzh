package com.talkweb.scoreManage.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.scoreManage.po.ce.ClassExamSubjectScore;
import com.talkweb.scoreManage.proc.ExcelErrorInfo;
import com.talkweb.scoreManage.proc.ExcelTitleInfo;
import com.talkweb.scoreManage.proc.ProgressBar;
import com.talkweb.scoreManage.proc.ScoreProgressProc;
import com.talkweb.scoreManage.service.ClassScoreCrudService;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.student.domain.page.StartImportTaskParam;

@Controller
@RequestMapping("/scoreReport1/classScoreImport")
public class ImportClassExamScoreAction extends BaseAction {

	private static final Logger logger = LoggerFactory.getLogger(ImportClassExamScoreAction.class);
	@Autowired
	private ScoreManageService scoreService;

	@Autowired
	private ClassScoreCrudService classScoreCrudService;

	@Autowired
	private AllCommonDataService allDataService;

	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	@Autowired
	private FileServer fileServerImplFastDFS;

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

	/**
	 * Excel上传接口
	 * 
	 * @param file
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	@RequestMapping(value = "/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(@RequestParam("excelBody") MultipartFile file, HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		JSONObject response = new JSONObject();
		setResponse(response, 1, "");

		Workbook workbook = null;
		try {
			String termInfoId = req.getParameter("termInfoId"); // 学年学期
			if (StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1002, "参数传递异常，请联系管理员！");
			}
			int scoreDataType = 1; // 新版本，所有系统格式只有数字类型，自定义格式是字符类型；以前的版本做过区分，1：数字类型，2：字符类型

			HttpSession session = req.getSession();
			session.setAttribute("scoreDataType", scoreDataType);
			String sessionId = session.getId();
			String schoolId = getXxdm(req);

			List<Map<String, Object>> kms = allDataService.getAdminKM(getSchool(req, termInfoId), termInfoId);
			String key = new StringBuffer().append("scoreManage.").append(schoolId).append(".importClassScore.km")
					.toString();
			req.getSession().setAttribute(key, kms);

			List<ExcelTitleInfo> titleInfoList = new ArrayList<ExcelTitleInfo>(3 + kms.size());
			titleInfoList.add(new ExcelTitleInfo("姓名", "xm", 1, -3L));
			titleInfoList.add(new ExcelTitleInfo("学号", "userXh", 1, -2L));
			titleInfoList.add(new ExcelTitleInfo("班级", "bjmc", 1, -1L));

			for (Map<String, Object> map : kms) {
				titleInfoList.add(new ExcelTitleInfo((String) map.get("zwmc"), (String) map.get("kmdm"), 0,
						Long.parseLong(map.get("kmdm").toString())));
			}

			Collections.sort(titleInfoList, new Comparator<ExcelTitleInfo>() {
				@Override
				public int compare(ExcelTitleInfo e1, ExcelTitleInfo e2) {
					return Long.compare(e1.getSort(), e2.getSort());
				}
			});

			String titleInfoKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.titleInfoList").toString();

			redisOperationDAO.set(titleInfoKey, titleInfoList,
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			// 获取源文件后缀名
			String fileName = file.getOriginalFilename();
			// 扩展名并转小写
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			// 文件名后缀转小写
			fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + prefix;

			byte[] fileBytes = file.getBytes();

			String fileId = null;
			String keyId = StringUtil.transformString(session.getAttribute("keyId"));// 文件主键值
			if (StringUtils.isEmpty(keyId)) {
				keyId = UUIDUtil.getUUID();
				session.setAttribute("keyId", keyId);
			}

			fileId = fileImportInfoService.getFileBy(schoolId, keyId);
			if (StringUtils.isNotEmpty(fileId)) { // 先删除相同的记录
				fileServerImplFastDFS.deleteFile(fileId);
				fileImportInfoService.deleteFile(schoolId, keyId);
			}

			fileId = fileServerImplFastDFS.uploadFile(fileBytes, fileName); // 上传文件倒文件服务器
			req.getSession().setAttribute("fileId", fileId);
			if (StringUtils.isNotEmpty(fileId)) {
				fileImportInfoService.addFile(schoolId, keyId, fileId); // 记录文件位置
			}

			ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

			if (prefix.equalsIgnoreCase("xls")) {
				workbook = new HSSFWorkbook(inputStream);
			} else if (prefix.equalsIgnoreCase("xlsx")) {
				workbook = new XSSFWorkbook(inputStream);
			} else {
				throw new CommonRunException(-2, "文件不是excel格式！");
			}

			if (workbook != null) {
				Sheet sheet = workbook.getSheetAt(0);
				int rows = sheet.getPhysicalNumberOfRows();
				if (rows > 0) {// 有数据时才处理
					Row row = sheet.getRow(0);
					int cellNum = row.getPhysicalNumberOfCells();
					//
					String[] excelTitleArray = new String[cellNum];
					// 判断是否需要进行手工字段匹配
					for (int i = 0; i < cellNum; i++) {
						String excelVal = row.getCell(i).getStringCellValue();
						excelVal = StringUtil.isEmpty(excelVal) ? "" : excelVal.trim();
						if (getExcelTitleInfoByName(excelVal, titleInfoList) == null) {
							setResponse(response, 2, "文件格式正确，字段需要匹配！");
						}
						// 放置临时保存目录
						excelTitleArray[i] = excelVal;
					}

					String excelTitleKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
							.append(".importClassScore.excelTitle").toString();
					redisOperationDAO.set(excelTitleKey, excelTitleArray,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
				} else {
					throw new CommonRunException(-2102, "Excel里面没有数据，请重新导入excel文件！");
				}
			}
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				workbook.close();
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
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelHead(HttpServletRequest req) throws Exception {
		JSONObject response = new JSONObject();
		try {
			JSONObject excelHead = new JSONObject();
			// 获取session中保存的临时表头
			String schoolId = getXxdm(req);
			String sessionId = req.getSession().getId();

			String excelTitleKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.excelTitle").toString();
			String[] excelTitleArray = (String[]) redisOperationDAO.get(excelTitleKey);

			String titleInfoKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.titleInfoList").toString();
			List<ExcelTitleInfo> titleInfoList = (List<ExcelTitleInfo>) redisOperationDAO.get(titleInfoKey);

			if (excelTitleArray == null || titleInfoList == null) {
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
			moduleField.put("total", titleInfoList.size());
			JSONArray rows = new JSONArray();
			moduleField.put("rows", rows);
			for (ExcelTitleInfo excelTitleInfo : titleInfoList) {
				JSONObject obj = new JSONObject();
				obj.put("field", excelTitleInfo.getSysTitleName());
				obj.put("sysfield", excelTitleInfo.getSysTitleNeed());
				rows.add(obj);
			}
			response.put("excelHead", excelHead);
			response.put("moduleField", moduleField);
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1001, "服务器异常，请联系管理员！");
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
	public synchronized ProgressBar startImportTask(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) throws Exception {
		ProgressBar progressBar = new ProgressBar();
		try {
			Integer isMatch = request.getInteger("isMatch");
			String termInfoId = request.getString("termInfoId");
			String examId = request.getString("examId");
			if (StringUtils.isBlank(termInfoId) || isMatch == null || StringUtils.isBlank(examId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			StartImportTaskParam stt = new StartImportTaskParam();
			stt.setIsMatch(isMatch);
			JSONArray matchResult = request.getJSONArray("matchResult");
			if (stt.getIsMatch() == 1) {
				if (matchResult == null) {
					throw new CommonRunException(-1, "参数传递异常，请联系管理员(matchResult is null)！");
				}
				stt.setMatchResult(matchResult);
			}

			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			int len = termInfoId.length() - 1;
			stt.setXn(termInfoId.substring(0, len));
			stt.setXq(termInfoId.substring(len));
			stt.setXxdm(schoolId);
			stt.setKslc(examId);

			progressBar.setOtherData("taskParam", stt);
			progressBar.setProgressInfo(0, 0, "导入任务已启动！");

			String progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.progress").toString();

			redisOperationDAO.set(progressKey, progressBar,
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			String prepDataKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.prepData").toString();

			redisOperationDAO.set(prepDataKey, new JSONObject(),
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			String key = new StringBuffer().append("scoreManage.").append(schoolId).append(".importClassScore.km")
					.toString();

			List<Map<String, Object>> kms = (List<Map<String, Object>>) req.getSession().getAttribute(key);

			int type = (int) req.getSession().getAttribute("scoreDataType");

			String keyId = StringUtil.transformString(req.getSession().getAttribute("keyId"));// 文件主键值
            String fileId = (String)req.getSession().getAttribute("fileId") ;
			SubProcess sp = new SubProcess(sessionId, 0, null, getXxdm(req), kms, type, keyId , fileId);
			sp.start();
		} catch (CommonRunException e) {
			progressBar.setProgressInfo(-1, 0, e.getMessage());
		} catch (Exception e) {
			progressBar.setProgressInfo(-1, 0, "服务器异常，启动任务失败，请联系管理员!");
			e.printStackTrace();
		}
		return progressBar;

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
	public synchronized ProgressBar continueImport(HttpServletRequest req, HttpServletResponse res) {
		ProgressBar progressBar = null;

		try {
			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			String progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.progress").toString();
			progressBar = (ProgressBar)redisOperationDAO.get(progressKey);
			progressBar.setProgressInfo(1, 35, "正在导入数据！");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			
			String key = new StringBuffer().append("scoreManage.").append(schoolId).append(".importClassScore.km")
					.toString();
			List<Map<String, Object>> kms = (List<Map<String, Object>>) req.getSession().getAttribute(key);

			int type = (int) req.getSession().getAttribute("scoreDataType");
			String keyId = StringUtil.transformString(req.getSession().getAttribute("keyId"));// 文件主键值
			 String fileId = (String)req.getSession().getAttribute("fileId") ;
			SubProcess sp = new SubProcess(sessionId, 1, null, schoolId, kms, type, keyId , fileId);
			sp.start();

			progressBar.setProgressInfo(1, "正在导入数据！");
		} catch (CommonRunException e) {
			progressBar.setProgressInfo(-1, e.getMessage());
		} catch (Exception e) {
			progressBar.setProgressInfo(-1, "服务器异常，继续导入失败，请联系管理员!");
			e.printStackTrace();
		}
		return progressBar;

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
	public synchronized ProgressBar singleDataCheck(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) throws Exception {
		ProgressBar progressBar = null;
		String progressKey = null;
		try {
			JSONArray mrows = request.getJSONArray("mrows");
			int row = request.getIntValue("row");
			int code = request.getIntValue("code");

			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			String prepDataKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.prepData").toString();
			progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.progress").toString();

			progressBar = (ProgressBar) redisOperationDAO.get(progressKey);
			JSONObject prepData = (JSONObject) redisOperationDAO.get(prepDataKey);
			progressBar.put("rowNum", row);

			if (null == progressBar || null == prepData) {
				progressBar.setProgressInfo(-50, 100, "由于长时间未操作，请重新导入");
				redisOperationDAO.set(progressKey, progressBar,
						CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				throw new CommonRunException(-50, "由于长时间未操作，请重新导入");
			}

			Map<Integer, JSONObject> rowDatas = (Map<Integer, JSONObject>) prepData.get("rowDatas");
			Map<String, String> titleEnMap = (Map<String, String>) prepData.get("titleEnMap");

			if (code == -1) {
				rowDatas.remove(row);
				prepData.put("rowDatas", rowDatas);
				redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				progressBar.setProgressInfo(1, "正在保存数据！");
				progressBar.setCode(1);
			} else if (code == 1) {
				StartImportTaskParam sp = (StartImportTaskParam) progressBar.getOtherData("taskParam");

				int isMatch = sp.getIsMatch();
				JSONArray mrs = sp.getMatchResult();

				JSONObject sd = rowDatas.get(row);
				for (int i = 0; i < mrows.size(); i++) {
					JSONObject o = mrows.getJSONObject(i);
					sd.put(titleEnMap.get(o.getString("title")), o.getString("value"));
				}

				Map<Integer, JSONObject> pureDatas = new HashMap<Integer, JSONObject>();
				pureDatas.put(row, sd);
				rowDatas.put(row, sd);
				prepData.put("rowDatas", rowDatas);

				redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

				String key = new StringBuffer().append("scoreManage.").append(schoolId).append(".importClassScore.km")
						.toString();

				List<Map<String, Object>> kms = (List<Map<String, Object>>) req.getSession().getAttribute(key);
				int type = (int) req.getSession().getAttribute("scoreDataType");

				JSONObject cr = checkImpData(pureDatas, mrs, isMatch, sessionId, sp, kms, type, true);
				if (cr.getBooleanValue("ckRs")) {
					progressBar.setProgressInfo(1, "校验通过！");
				} else {
					progressBar.setProgressInfo(-1, "校验不通过！");
					progressBar.put("mrows", cr.getJSONArray("wrongMsg").getJSONObject(0).get("mrows"));
				}
			}
		} catch (CommonRunException e) {
			progressBar.setProgressInfo(e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			progressBar.setProgressInfo(-1, "后台系统异常，请联系管理员！");
		}
		redisOperationDAO.set(progressKey, progressBar,
				CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		 
		return progressBar;
	}

	/**
	 * 全部忽略验证错误
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/ignoreAllError")
	@ResponseBody
	public synchronized ProgressBar ignoreAllError(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) throws Exception {
		ProgressBar response = new ProgressBar();

		try {
			int code = request.getIntValue("code");

			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			String progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.progress").toString();
			ProgressBar progressBar = (ProgressBar) redisOperationDAO.get(progressKey);

			String prepDataKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.prepData").toString();
			JSONObject prepData = (JSONObject) redisOperationDAO.get(prepDataKey);

			if (null == progressBar || null == prepData) {
				progressBar = new ProgressBar();
				progressBar.setProgressInfo(-50, 100, "由于长时间未操作，请重新导入");
				redisOperationDAO.set(progressKey, progressBar,
						CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				throw new CommonRunException(-50, "由于长时间未操作，请重新导入");
			}

			Map<Integer, JSONObject> rowDatas = (Map<Integer, JSONObject>) prepData.get("rowDatas");
			if (code == -1) {
				JSONObject jsonObject = (JSONObject) progressBar.get("data");
				JSONObject validateMsg = jsonObject == null ? null : jsonObject.getJSONObject("validateMsg");
				JSONArray mrows = validateMsg == null ? null : validateMsg.getJSONArray("rows");

				if (CollectionUtils.isNotEmpty(mrows)) {
					for (int i = 0; i < mrows.size(); i++) {
						JSONObject o = mrows.getJSONObject(i);
						int row = o.getIntValue("row");
						rowDatas.remove(row);
					}
					
					redisOperationDAO.set(prepDataKey, prepData,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					
					progressBar.setProgressInfo(1, "正在保存数据！");
					
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					
					response.setProgressInfo(0, "忽略成功!");
					response.put("msg", "忽略成功!");
				} else {
					response.setProgressInfo(-1, "需要忽略的校验错误为空!");
					response.put("msg", "需要忽略的校验错误为空!");
				}
			}
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.setProgressInfo(e.getCode(), e.getMessage());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "后台系统异常，请联系管理员！");
			response.put("msg", "后台系统异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 获取导入进度
	 * 
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	@RequestMapping(value = "/importProgress")
	@ResponseBody
	public ProgressBar importProgress(HttpServletRequest req, HttpServletResponse res) throws IOException, Exception {
		ProgressBar progressBar = new ProgressBar();

		String schoolId = getXxdm(req);
		String sessionId = req.getSession().getId();

		try {
			String progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importClassScore.progress").toString();

			progressBar = (ProgressBar) redisOperationDAO.get(progressKey);
			if (progressBar == null) {
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
			progressBar.setProgressInfo(-1, "后台异常，请联系管理员！");
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
		private String sessionId;
		private String xxdm;
		private int impType; // 0：开始导入，1：继续导入
		List<Map<String, Object>> kms;
		private JSONObject singleData;
		private String keyId;
		int scoreDataType;

		private String progressKey = null;
		private String prepDataKey = null;
		private String excelTitleKey = null;
		private String fileId = null;

		public SubProcess(String sessionId, int impType, JSONObject singleData, String xxdm,
				List<Map<String, Object>> kms, int scoreDataType, String keyId , String fileId) {
			this.sessionId = sessionId;
			this.impType = impType;
			this.singleData = singleData;
			this.xxdm = xxdm;
			this.kms = kms;
			this.scoreDataType = scoreDataType;
			this.keyId = keyId;

			this.progressKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
					.append(".importClassScore.progress").toString();
			this.prepDataKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
					.append(".importClassScore.prepData").toString();
			this.excelTitleKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
					.append(".importClassScore.excelTitle").toString();
			
			this.fileId = fileId;
		}

		@Override
		public void run() {
			ProgressBar progressBar = null;
			JSONObject prepData = new JSONObject();
			// excel导入处理开始
			String fileIdTmp = null;
			try {
				progressBar = (ProgressBar) redisOperationDAO.get(progressKey);
				prepData = (JSONObject) redisOperationDAO.get(prepDataKey);
				String[] excelTitle = (String[]) redisOperationDAO.get(excelTitleKey);

				if (progressBar == null || prepData == null || excelTitle == null) {
					throw new CommonRunException(-50, "由于长时间未操作，数据已过期，请重新导入!");
				}

				StartImportTaskParam taskParam = (StartImportTaskParam) progressBar.getOtherData("taskParam");
				fileIdTmp = fileImportInfoService.getFileBy(xxdm, keyId);
				if (fileIdTmp!=null) {
					fileId = fileIdTmp;
				}

				int isMatch = taskParam.getIsMatch();
				JSONArray matchResult = taskParam.getMatchResult();

				progressBar.setProgressInfo(1, 0, "正在准备导入任务");
				redisOperationDAO.set(progressKey, progressBar,
						CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

				JSONArray datas = new JSONArray();
				if (impType == 0) { // 0：为开始导入，1：继续导入
					datas = readExcelToData(fileId);
					if (CollectionUtils.isEmpty(datas)) {
						throw new CommonRunException(-1, "excel中没有数据，请重新上传文件！");
					}
				}

				// 开始校验
				JSONObject ckrs = new JSONObject();
				ckrs.put("ckRs", true); 
				if (impType == 0) {
					progressBar.setProgressInfo(1, 5, "正在校验excel数据");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					Map<Integer, JSONObject> rowDatas = changeData(datas, matchResult, isMatch, sessionId, xxdm, kms);
					ckrs = checkImpData(rowDatas, matchResult, isMatch, sessionId, taskParam, kms, scoreDataType, false);
				}

				prepData = (JSONObject) redisOperationDAO.get(prepDataKey);
				if (ckrs.getBooleanValue("ckRs") || impType == 1) {
					// 开始入库
					progressBar.setProgressInfo(1, 30, "正在保存数据！");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					List<ClassExamSubjectScore> scoreList = getInsertEntityByCkrs(taskParam, sessionId);

					if (scoreList.size() > 0) {
						// 添加学生单科成绩
						classScoreCrudService.insertClassExamScoreBatch(scoreList, taskParam, scoreDataType);
					}

					String[] userKms = (String[]) prepData.get("userKms");
					int nums = datas.size() - 1;
					Set<String> set = new HashSet<String>();
					if (impType == 1) {
						for (ClassExamSubjectScore sc : scoreList) {
							if (!set.contains(sc.getStudentId())) {
								set.add(sc.getStudentId());
							}
						}
					}
					int a = set.isEmpty() ? nums : set.size();

					progressBar.setProgressInfo(2, 100, "导入成功，共计导入" + a + "位学生*" + userKms.length + "个科目成绩数据！");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
				} else {
					progressBar.setProgressInfo(-2, 32, "Excel数据校验不通过!");
					if (impType == 0) {
						progressBar.setOtherData("total", datas.size() - 1);
					} else {
						progressBar.setOtherData("total", 1);
					}

					JSONObject validateMsg = new JSONObject();
					validateMsg.put("total", ckrs.getJSONArray("wrongMsg").size());
					validateMsg.put("rows", ckrs.getJSONArray("wrongMsg"));
					progressBar.setOtherData("validateMsg", validateMsg);
				}
			} catch (CommonRunException e) {
				progressBar.setProgressInfo(e.getCode(), 100, e.getMessage());
			} catch (Exception e) {
				progressBar.setProgressInfo(-1, 100, "服务器异常，请稍后重试或联系管理员!");
				e.printStackTrace();
			} finally {
				try {
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
					redisOperationDAO.set(prepDataKey, prepData,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
					redisOperationDAO.expire(excelTitleKey,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

					// excel导入处理结束
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
	private List<ClassExamSubjectScore> getInsertEntityByCkrs(StartImportTaskParam sp, String sessionId)
			throws Exception {
		// TODO Auto-generated method stub
		String xq = sp.getXq();
		String xn = sp.getXn();
		String termInfoId = xn + xq;
		String xxdm = sp.getXxdm();
		// 使用为考试轮次代码
		String kslcdm = sp.getKslc();

		String prepDataKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
				.append(".importClassScore.prepData").toString();

		JSONObject prepData = (JSONObject) redisOperationDAO.get(prepDataKey);
		if (null == prepData) {
			throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
		}

		Map<Integer, List<ClassExamSubjectScore>> rows = (Map<Integer, List<ClassExamSubjectScore>>) prepData
				.get("rowcolMap");

		List<ClassExamSubjectScore> scoreList = new ArrayList<ClassExamSubjectScore>();
		for (Map.Entry<Integer, List<ClassExamSubjectScore>> entry : rows.entrySet()) {
			List<ClassExamSubjectScore> rowList = entry.getValue();
			for (ClassExamSubjectScore row : rowList) {
				row.setExamId(kslcdm);
				row.setTermInfoId(termInfoId);
				row.setSchoolId(xxdm);
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
	 * @param processId2
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer, JSONObject> changeData(JSONArray datas, JSONArray matchResult, int isMatch, String sessionId,
			String xxdm, List<Map<String, Object>> kms) throws Exception {
		String titleInfoKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
				.append(".importClassScore.titleInfoList").toString();
		List<ExcelTitleInfo> titleInfoList = (List<ExcelTitleInfo>) redisOperationDAO.get(titleInfoKey);

		if (titleInfoList == null) {
			throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
		}

		List<String> kmdms = new ArrayList<String>();
		for (int i = 0; i < kms.size(); i++) {// 重新去科目 放入
			kmdms.add((String) kms.get(i).get("kmdm"));
		}

		String[] excelTitles = (String[]) datas.get(0);
		// 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射实体字段[3]
		String[][] arr = new String[4][excelTitles.length];

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

			ExcelTitleInfo excelTitleInfo = getExcelTitleInfoByName(sysTit, titleInfoList);
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

		int xmIndex = strIndexInArray("xm", arr[3]);
		int xjhIndex = strIndexInArray("xjh", arr[3]);
		int xhIndex = strIndexInArray("userXh", arr[3]);

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
		JSONObject prepData = new JSONObject();
		prepData.put("rowDatas", rowDatas);
		prepData.put("titleEnMap", titleEnMap);
		prepData.put("userKms", userKms.toArray(new String[0]));
		prepData.put("bs", bs);
		prepData.put("bsix", bsix);
		prepData.put("impHead", arr[3]);

		String prepDataKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
				.append(".importClassScore.prepData").toString();
		redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
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
		// 转换器 一般poi取数字格式需转换
		DecimalFormat df = new DecimalFormat("0.0");
		if (rows > 0) {
			int cols = sheet.getRow(0).getPhysicalNumberOfCells();
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
	private JSONObject checkImpData(Map<Integer, JSONObject> rowDatas, JSONArray mrs, int isMatch, String sessionId,
			StartImportTaskParam sp, List<Map<String, Object>> kms, int scoreDataType, boolean isSingleDataCheck)
			throws Exception {
		String schoolId = sp.getXxdm();

		String progressKey = new StringBuffer("scoreManage.").append(schoolId).append(sessionId)
				.append(".importClassScore.progress").toString();

		String prepDataKey = new StringBuffer("scoreManage.").append(schoolId).append(sessionId)
				.append(".importClassScore.prepData").toString();

		ProgressBar progressBar = (ProgressBar) redisOperationDAO.get(progressKey);
		JSONObject prepData = (JSONObject) redisOperationDAO.get(prepDataKey);
		if (progressBar == null || prepData == null) {
			throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
		}

		String xq = sp.getXq();
		String xn = sp.getXn();

		// excel验证部分
		JSONObject rs = new JSONObject();

		List<String> kmdms = new ArrayList<String>();
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
		String bs = prepData.getString("bs");

		Map<Integer, List<ClassExamSubjectScore>> rowcolMap = null;
		if (prepData.containsKey("rowcolMap")) {
			rowcolMap = (Map<Integer, List<ClassExamSubjectScore>>) prepData.get("rowcolMap");
		} else {
			rowcolMap = new HashMap<Integer, List<ClassExamSubjectScore>>();
			prepData.put("rowcolMap", rowcolMap);
		}

		JSONArray wrongMsg = new JSONArray();
		int index = 0;
		for (Map.Entry<Integer, JSONObject> entry : rowDatas.entrySet()) {
			index++;
			int rowNum = entry.getKey();
			JSONObject pd = entry.getValue();

			// 单条时直接覆盖 批量时也不会重复 行号不重复的话
			List<ClassExamSubjectScore> etyList = new ArrayList<ClassExamSubjectScore>();
			rowcolMap.put(rowNum, etyList);

			JSONObject wmsg = new JSONObject();
			wmsg.put("row", rowNum);
			wmsg.put("bs", pd.getString(bs + "Name"));
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
				wmsg.getJSONArray("mrows").add(new ExcelErrorInfo(bsTitle, bs, bsContent, "标识不能为空！"));
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
				String cj2 = "";

				if (val.equals("舞弊") || val.equals("缺考")) {
					cj = 0f;
					cj2 = val;
				} else {
					if (scoreDataType == 1) {
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
					} else {
						cj2 = val;
					}
				}

				if (wmsg.getJSONArray("mrows").size() == 0 && accId.trim().length() > 0) {
					JSONObject stu = dbxjhMap.get(accId);

					ClassExamSubjectScore scoreInfo = new ClassExamSubjectScore();
					scoreInfo.setScore(cj);
					scoreInfo.setScore2(cj2);
					scoreInfo.setStudentId(stu.getString("xh"));
					scoreInfo.setClassId(stu.getString("bh"));
					scoreInfo.setSubjectId(titleCode);

					etyList.add(scoreInfo);
				}
			}

			if (wmsg.getJSONArray("mrows").size() > 0) {
				// 学号重复导致的校验不通过 不移除该学号键
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

			progressBar.setProgressInfo(1, 10 + (int) ((float) 20 * index / rowDatas.keySet().size()),
					"正在校验excel数据,匹配表头完成,校验数据");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		}

		prepData.put("rowcolMap", rowcolMap);
		prepData.put("xmbjMap", xmbjMap);
		prepData.put("xhMap", xhMap);
		redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		if (wrongMsg != null && wrongMsg.size() > 0) {
			rs.put("ckRs", false);
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

		xhMap.put(userXh, wmsg); // 不存在则放入，继续判断
		result.put("accId", stuInfo.getString("userId"));
		return result;
	}

	private JSONObject checkIdentifierXm(String xm, String userXh, String bjmc, String bsTitle, String bs,
			JSONObject pd, Map<String, JSONObject> xmbjMap, Map<String, List<JSONObject>> dbxmbjMap,
			Map<String, Classroom> bjMap, Map<String, List<JSONObject>> dbxmMap, JSONObject wmsg) {
		JSONObject result = new JSONObject();
		JSONArray mrows = wmsg.getJSONArray("mrows");

		if (StringUtils.isBlank(xm)) {
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
				mrows.add(new ExcelErrorInfo(pd.getString("xmName"), pd.getString("xm"), "系统中存同名学生，请使用身份证或手机号替换名字！"));
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

			xmbjMap.put(key, wmsg);
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