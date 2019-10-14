package com.talkweb.scoreManage.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.scoreManage.po.gm.CompetitionStu;
import com.talkweb.scoreManage.proc.ExcelErrorInfo;
import com.talkweb.scoreManage.proc.ExcelTitleInfo;
import com.talkweb.scoreManage.proc.ProgressBar;
import com.talkweb.scoreManage.proc.ScoreProgressProc;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.student.domain.page.StartImportTaskParam;

/**
 * 竞赛组学生
 *
 */
@Controller
@RequestMapping("/competitionGroup1")
public class CompetitionGroupAction extends BaseAction {

	@Autowired
	private ScoreManageService scoreService;

	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	@Autowired
	private FileServer fileServerImplFastDFS;

	@Autowired
	private FileImportInfoService fileImportInfoService;

	@Autowired
	private AllCommonDataService commonDataService;
	private static final Logger logger = LoggerFactory.getLogger(CompetitionGroupAction.class);

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
	@RequestMapping(value = "/import/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(@RequestParam("excelBody") MultipartFile file, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		// 封装返回结果
		setResponse(response, 1, "");

		Workbook workbook = null;
		try {
			HttpSession session = req.getSession();

			List<ExcelTitleInfo> excelTitleInfoList = new ArrayList<ExcelTitleInfo>(4);
			excelTitleInfoList.add(new ExcelTitleInfo("学号", "userXh", 1, 1L));
			excelTitleInfoList.add(new ExcelTitleInfo("姓名", "xm", 1, 2L));
			excelTitleInfoList.add(new ExcelTitleInfo("班级", "bjmc", 1, 3L));
			excelTitleInfoList.add(new ExcelTitleInfo("统计科目", "tjkm", 1, 4L));

			// 获取源文件后缀名
			String fileName = file.getOriginalFilename();
			// 扩展名并转小写
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			// 文件名后缀转小写
			fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + prefix;

			String xxdm = getXxdm(req);
			byte[] fileBytes = file.getBytes();
			String sessionId = session.getId();

			String keyId = StringUtil.transformString(session.getAttribute("keyId"));// 文件主键值
			if (StringUtils.isEmpty(keyId)) {
				keyId = UUID.randomUUID().toString();
				session.setAttribute("keyId", keyId);
			}

			String fileId = fileImportInfoService.getFileBy(xxdm, keyId);
			if (StringUtils.isNotEmpty(fileId)) {
				fileServerImplFastDFS.deleteFile(fileId);
				fileImportInfoService.deleteFile(xxdm, keyId);
			}

			fileId = fileServerImplFastDFS.uploadFile(fileBytes, fileName);
			if (StringUtils.isNotEmpty(fileId)) {
				fileImportInfoService.addFile(getXxdm(req), keyId, fileId);
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

				// 有数据时才处理
				if (rows > 1) {
					Row row = sheet.getRow(0);
					int cellNum = row.getPhysicalNumberOfCells();
					String[] excelTitleArray = new String[cellNum];
					// 判断是否需要进行手工字段匹配
					for (int i = 0; i < cellNum; i++) {
						String excelVal = row.getCell(i).getStringCellValue();
						excelVal = StringUtil.isEmpty(excelVal) ? "" : excelVal.trim();
						if (getExcelTitleInfoByName(excelVal, excelTitleInfoList) == null) {
							setResponse(response, 2, "文件格式正确，字段需要匹配！");
						}
						// 放置临时保存目录
						excelTitleArray[i] = excelVal;
					}

					String excelTitleKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
							.append(".importCompetitionStudent.excelTitle").toString();
					redisOperationDAO.set(excelTitleKey, excelTitleArray,
							CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

					String excelTitleInfoKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
							.append(".importCompetitionStudent.excelTitleInfo").toString();
					redisOperationDAO.set(excelTitleInfoKey, excelTitleInfoList,
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
	@RequestMapping(value = "/import/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelMatch(HttpServletRequest req) throws Exception {
		JSONObject response = new JSONObject();
		try {
			JSONObject excelHead = new JSONObject();
			// 获取session中保存的临时表头
			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			String excelTitleInfoKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.excelTitleInfo").toString();

			List<ExcelTitleInfo> excelTitleInfoList = (List<ExcelTitleInfo>) redisOperationDAO.get(excelTitleInfoKey);

			String excelTitleKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.excelTitle").toString();

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
	@RequestMapping(value = "/import/startImportTask")
	@ResponseBody
	public synchronized JSONObject startImportTask(HttpServletRequest req, @RequestBody JSONObject mappost,
			HttpServletResponse res) throws Exception {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = mappost.getString("termInfoId");
			String kslc = mappost.getString("examId");

			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(kslc)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			String schoolId = getXxdm(req);
			String sessionId = req.getSession().getId();

			String prepDataKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.prepData").toString();
			String progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.progress").toString();
			String excelTitleKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.excelTitle").toString();
			String taskParamKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.taskParam").toString();

			StartImportTaskParam taskParam = new StartImportTaskParam();
			// 设置获取的参数
			taskParam.setIsMatch(mappost.getIntValue("isMatch"));// 是否需要手工匹配,1
																	// 手工匹配，0
			// 无需匹配
			if (taskParam.getIsMatch() == 1) {
				taskParam.setMatchResult(mappost.getJSONArray("matchResult"));// 匹配结果
			}

			String schoolYear = termInfoId.substring(0, termInfoId.length() - 1);
			String term = termInfoId.substring(termInfoId.length() - 1);

			taskParam.setKslc(kslc);
			taskParam.setXn(schoolYear);
			taskParam.setXq(term);
			taskParam.setXxdm(schoolId);

			// 考试下的科目
			JSONObject params = new JSONObject();
			params.put("xxdm", schoolId);
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", termInfoId);
			List<Long> subjectIds = scoreService.getExamSubjectIdList(params);

			List<String> kmmcs = new ArrayList<String>();
			List<String> kmdms = new ArrayList<String>();
			if (subjectIds.size() > 0) {
				List<LessonInfo> list = commonDataService.getLessonInfoBatch(Long.valueOf(schoolId), subjectIds,
						termInfoId);
				if (CollectionUtils.isNotEmpty(list)) {
					for (LessonInfo c : list) {
						kmmcs.add(c.getName());
						kmdms.add(String.valueOf(c.getId()));
					}
				}
			}

			redisOperationDAO.set(taskParamKey, taskParam,
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			ProgressBar progressBar = new ProgressBar();
			progressBar.setCode(0);
			progressBar.setProgress(0);
			progressBar.setMsg("正常启动任务");
			redisOperationDAO.set(progressKey, progressBar,
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			redisOperationDAO.set(prepDataKey, new JSONObject(),
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			redisOperationDAO.expire(excelTitleKey, CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			String kmmcsKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.kmmcs").toString();
			String kmdmskey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.kmdms").toString();
			redisOperationDAO.set(kmmcsKey, kmmcs, CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
			redisOperationDAO.set(kmdmskey, kmdms, CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			String keyId = req.getSession().getAttribute("keyId").toString();
			SubProcess sp = new SubProcess(sessionId, 0, schoolId, kmdms, kmmcs, keyId);
			sp.start();

			setResponse(response, 0, "正常启动任务");
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
	 * 获取导入进度
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/import/importProgress")
	@ResponseBody
	public JSONObject importProgress(HttpServletRequest req, HttpServletResponse res) throws Exception {
		ProgressBar progressBar = new ProgressBar();
		progressBar.setProgressInfo(1, 0, "");

		String schoolId = getXxdm(req);
		String sessionId = req.getSession().getId();

		try {
			String progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.progress").toString();

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

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/import/singleDataCheck")
	@ResponseBody
	public synchronized JSONObject singleDataCheck(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) throws Exception {
		JSONObject rs = new JSONObject();
		JSONObject data = new JSONObject();
		String sessionId = req.getSession().getId();
		String schoolId = getXxdm(req);
		try {
			int row = request.getIntValue("row");
			rs.put("rowNum", row);
			JSONArray mrows = request.getJSONArray("mrows");

			String progressKey = new StringBuffer("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.progress").toString();
			ProgressBar progressBar = (ProgressBar) redisOperationDAO.get(progressKey);

			String prepDataKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
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
				String taskParamKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
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

				String kmmcsKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
						.append(".importCompetitionStudent.kmmcs").toString();
				String kmdmskey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
						.append(".importCompetitionStudent.kmdms").toString();

				List<String> kmmcs = (List<String>) redisOperationDAO.get(kmmcsKey); // 科目名称
				List<String> kmdms = (List<String>) redisOperationDAO.get(kmdmskey); // 科目代码

				JSONObject cr = checkImpData(pureDatas, mrs, isMatch, sessionId, sp, kmmcs, kmdms, true);

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
			rs.put("code", -1);
			data.put("msg", "后台系统异常，请联系管理员！");
			rs.put("data", data);
		}
		return rs;
	}

	/**
	 * 继续导入(验证通过后自动触发的下一步)
	 * 
	 * @param sessionId
	 * @param arr
	 * @param sp
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/import/continueImport")
	@ResponseBody
	public synchronized JSONObject continueImport(HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);
			
			String progressKey = new StringBuffer("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.progress").toString();
			ProgressBar progressBar = new ProgressBar();
			progressBar.setProgressInfo(1, 0, "继续导入！");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

			String kmmcsKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.kmmcs").toString();
			String kmdmskey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.kmdms").toString();

			List<String> kmmcs = (List<String>) redisOperationDAO.get(kmmcsKey);
			List<String> kmdms = (List<String>) redisOperationDAO.get(kmdmskey);

			String keyId = StringUtil.transformString(req.getSession().getAttribute("keyId"));// 文件主键值

			SubProcess sp = new SubProcess(sessionId, 1, getXxdm(req), kmdms, kmmcs, keyId);
			sp.start();

			setResponse(response, 1, "继续导入！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "启动任务异常，请联系管理员！");
		}
		return response;
	}

	class SubProcess extends Thread {
		private String sessionId;
		/**
		 * 学校代码
		 */
		private String schoolId;
		/**
		 * 导入类型0：第一次导入，1：校验后继续导入
		 */
		private int impType;

		private String progressKey;
		private String prepDataKey;
		private String taskParamKey;

		private String keyId;

		List<String> kmdms;
		List<String> kmmcs;

		public SubProcess(String sessionId, int impType, String schoolId, List<String> kmdms, List<String> kmmcs,
				String keyId) {
			this.sessionId = sessionId;
			this.impType = impType;
			this.schoolId = schoolId;
			this.kmdms = kmdms;
			this.kmmcs = kmmcs;
			this.keyId = keyId;

			this.prepDataKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.prepData").toString();
			this.progressKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importCompetitionStudent.progress").toString();
			this.taskParamKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
					.append(".importScore.taskParam").toString();
		}

		@Override
		public void run() {
			ProgressBar progressBar = new ProgressBar();
			String fileId = fileImportInfoService.getFileBy(schoolId, keyId);
			// excel导入处理开始
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

				if (impType == 0) {
					progressBar.setProgressInfo(1, 10, "正在校验excel数据");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					JSONArray matchResult = taskParam.getMatchResult();

					Map<Integer, JSONObject> rowDatas = changeData(datas, matchResult, isMatch, sessionId, schoolId);
					ckrs = checkImpData(rowDatas, matchResult, isMatch, sessionId, taskParam, kmmcs, kmdms, false);

				}

				JSONObject prepData = (JSONObject) getRedisData(prepDataKey);
				if (ckrs.getBooleanValue("ckRs") || impType == 1) { // 校验通过，或者由于错误修复之后继续执行
					// 开始入库
					progressBar.setProgressInfo(1, 40, "正在保存数据！");
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					List<CompetitionStu> compStudList = getInsertEntityByCkrs(taskParam, sessionId);

					ScoreProgressProc spThread = null;
					if (compStudList.size() > 0) {
						spThread = new ScoreProgressProc("正在保存数据！", 1, 6, 5, 40, progressKey);
						spThread.start();

						JSONObject params = new JSONObject();
						params.put("kslcdm", taskParam.getKslc());
						params.put("kslc", taskParam.getKslc());
						params.put("xnxq", taskParam.getXn() + taskParam.getXq());
						params.put("xxdm", schoolId);
						scoreService.insertCompStuBatch(params, compStudList);

						progressBar.setProgressInfo(2, 100, "导入成功，共计导入" + compStudList.size() + "条信息记录！");
						redisOperationDAO.set(progressKey, progressBar,
								CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					}
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

					String excelTitleKey = new StringBuffer().append("scoreManage.").append(schoolId).append(sessionId)
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
		Workbook workbook = null;
		byte[] bytes = fileServerImplFastDFS.downloadFile(fileId);
		if (bytes == null || bytes.length == 0) {
			throw new CommonRunException(-1, "excel文件已被删除，请重新上传并导入数据！");
		}

		InputStream inputStream = new ByteArrayInputStream(bytes);

		if (fileId.endsWith("xls")) {
			workbook = new HSSFWorkbook(inputStream);
		} else if (fileId.endsWith("xlsx")) {
			workbook = new XSSFWorkbook(inputStream);
		} else {
			workbook = WorkbookFactory.create(inputStream);
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
			String sessionId, StartImportTaskParam sp, List<String> kmmcs, List<String> kmdms,
			boolean isSingleDataCheck) throws Exception {
		JSONObject rs = new JSONObject();

		String schoolId = sp.getXxdm();

		String progressKey = new StringBuffer("scoreManage.").append(schoolId).append(sessionId)
				.append(".importCompetitionStudent.progress").toString();

		String prepDataKey = new StringBuffer("scoreManage.").append(schoolId).append(sessionId)
				.append(".importScore.prepData").toString();

		ProgressBar progressBar = (ProgressBar) redisOperationDAO.get(progressKey);
		JSONObject prepData = (JSONObject) redisOperationDAO.get(prepDataKey);
		if (progressBar == null || prepData == null) {
			throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
		}

		String xq = sp.getXq();
		String xn = sp.getXn();

		progressBar.setProgressInfo(1, 9, "正在校验excel数据,匹配表头完成,校验数据");
		redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

		// 数据库验证部分
		// 姓名科目对应的集合
		Map<String, JSONObject> bjxmkmMap = new HashMap<String, JSONObject>();
		if (prepData.containsKey("bjxmkmMap")) {
			bjxmkmMap = (Map<String, JSONObject>) prepData.get("bjxmkmMap");
		}
		// 学号科目对应的集合
		Map<String, JSONObject> xhkmMap = new HashMap<String, JSONObject>();
		if (prepData.containsKey("xhkmMap")) {
			xhkmMap = (Map<String, JSONObject>) prepData.get("xhkmMap");
		}

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
		Map<String, List<JSONObject>> dbxmbjMap = (Map<String, List<JSONObject>>) dbckMap.get("xmbjMap"); // 姓名班级映射表
		Map<String, Classroom> bjMap = (Map<String, Classroom>) dbckMap.get("bjMap"); // 班级名称映射表
		Map<String, List<JSONObject>> dbxmMap = (Map<String, List<JSONObject>>) dbckMap.get("xmMap"); // 姓名映射表

		String bs = prepData.getString("bs"); // 标识代码，能够识别唯一的学生，例如：xm、userXh

		Map<Integer, CompetitionStu> rowcolMap = null;
		if (prepData.containsKey("rowcolMap")) {
			rowcolMap = (Map<Integer, CompetitionStu>) prepData.get("rowcolMap");
		} else {
			rowcolMap = new HashMap<Integer, CompetitionStu>();
			prepData.put("rowcolMap", rowcolMap);
		}

		JSONArray wrongMsg = new JSONArray();

		int index = 0;
		for (Map.Entry<Integer, JSONObject> entry : rowDatas.entrySet()) {
			index++;

			int rowNum = entry.getKey();
			JSONObject pd = entry.getValue();

			// 单条时直接覆盖 批量时也不会重复 行号不重复的话
			JSONObject wmsg = new JSONObject();
			wmsg.put("row", rowNum); // 行号
			wmsg.put("bs", pd.getString(bs + "Name")); // 标识，表头名称，例如：学号、学籍号
			wmsg.put("mrows", new JSONArray());

			// 姓名
			String xm = pd.getString("xm");
			xm = xm == null ? "" : xm;
			// 学号
			String userXh = pd.getString("userXh");
			// 班级名称
			String bjmc = pd.getString("bjmc");
			bjmc = bjmc == null ? "" : bjmc;

			String tjkm = pd.getString("tjkm"); // 统计科目

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
				JSONObject result = checkIdentifierXh(xm, userXh, bjmc, tjkm, bsTitle, pd, kmmcs, xhkmMap, dbxhMap,
						wmsg);
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
				JSONObject result = checkIdentifierXm(xm, userXh, bjmc, tjkm, bsTitle, bs, pd, kmmcs, bjxmkmMap,
						dbxmbjMap, bjMap, dbxmMap, wmsg);
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

			if (wmsg.getJSONArray("mrows").size() > 0) {
				if (!isSingleDataCheck && !isXhCfBug) {
					// 单次测试异常，则不移除测试信息
					xhkmMap.remove(userXh + tjkm);
				}
				if (!isSingleDataCheck && !isXmBjCfBug) {
					// 单次测试异常，则不移除测试信息
					bjxmkmMap.remove(bjmc + xm + tjkm);
				}
				wrongMsg.add(wmsg);
			} else {
				// 单条时直接覆盖 批量时也不会重复 行号不重复的话
				CompetitionStu compStu = new CompetitionStu();
				compStu.setXh(accId);
				int idx = kmmcs.indexOf(tjkm);
				compStu.setKmdm(kmdms.get(idx));
				rowcolMap.put(rowNum, compStu);
			}

			progressBar.setProgressInfo(1, 32 + (int) ((float) 20 * index / rowDatas.keySet().size()),
					"正在校验excel数据,匹配表头完成,校验数据");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		}

		prepData.put("rowcolMap", rowcolMap);
		prepData.put("xhkmMap", xhkmMap);
		prepData.put("bjxmkmMap", bjxmkmMap);

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

		return rs;
	}

	private JSONObject checkIdentifierXh(String xm, String userXh, String bjmc, String tjkm, String bsTitle,
			JSONObject pd, List<String> kmmcs, Map<String, JSONObject> xhkmMap, Map<String, JSONObject> dbxhMap,
			JSONObject wmsg) {
		JSONObject result = new JSONObject();
		JSONArray mrows = wmsg.getJSONArray("mrows");

		if (StringUtils.isBlank(userXh)) {
			result.put("isXhCfBug", true);
			mrows.add(new ExcelErrorInfo(bsTitle, userXh, "不能为空！"));
			return result;
		}

		if (!dbxhMap.containsKey(userXh)) {
			mrows.add(new ExcelErrorInfo(bsTitle, userXh, "无匹配记录！"));
			return result;
		}

		if (StringUtils.isBlank(tjkm)) {
			result.put("isXhCfBug", true);
			mrows.add(new ExcelErrorInfo(pd.getString("tjkmName"), pd.getString("tjkm"), "不能为空！"));
			return result;
		}

		if (!kmmcs.contains(tjkm)) {
			result.put("isXhCfBug", true);
			mrows.add(new ExcelErrorInfo(pd.getString("tjkmName"), pd.getString("tjkm"), "无匹配记录！"));
			return result;
		}

		String xhkm = userXh + tjkm;
		if (xhkmMap.containsKey(xhkm)) {
			result.put("isXhCfBug", true);
			mrows.add(new ExcelErrorInfo(pd.getString("tjkmName"), pd.getString("tjkm"), "excel中统计科目重复！"));

			JSONObject wmsg1 = xhkmMap.get(xhkm);
			wmsg1.getJSONArray("mrows").add(new ExcelErrorInfo(pd.getString("tjkmName"), pd.getString("tjkm"), "excel中重复！"));
			result.put("wmsg", wmsg1);

			return result;
		}
		xhkmMap.put(xhkm, wmsg);

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

	private JSONObject checkIdentifierXm(String xm, String userXh, String bjmc, String tjkm, String bsTitle, String bs,
			JSONObject pd, List<String> kmmcs, Map<String, JSONObject> bjxmkmMap,
			Map<String, List<JSONObject>> dbxmbjMap, Map<String, Classroom> bjMap, Map<String, List<JSONObject>> dbxmMap,
			JSONObject wmsg) {
		JSONObject result = new JSONObject();
		JSONArray mrows = wmsg.getJSONArray("mrows");

		if (StringUtils.isBlank(xm)) {
			mrows.add(new ExcelErrorInfo(bs, xm, "不能为空！"));
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

		if (StringUtils.isBlank(tjkm)) {
			result.put("isXhCfBug", true);
			mrows.add(new ExcelErrorInfo(pd.getString("tjkmName"), pd.getString("tjkm"), "不能为空！"));
			return result;
		}

		if (!kmmcs.contains(tjkm)) {
			result.put("isXhCfBug", true);
			mrows.add(new ExcelErrorInfo(pd.getString("tjkmName"), pd.getString("tjkm"), "无匹配记录！"));
			return result;
		}

		String bjxmkm = bjmc + xm + tjkm;
		if (bjxmkmMap.containsKey(bjxmkm)) {
			result.put("isXmBjCfBug", true);
			mrows.add(new ExcelErrorInfo(pd.getString("tjkmName"), pd.getString("tjkm"), "excel中重复！"));

			JSONObject wmsg1 = bjxmkmMap.get(bjxmkm);
			wmsg1.getJSONArray("mrows").add(new ExcelErrorInfo(pd.getString("tjkmName"), pd.getString("tjkm"), "excel中重复！"));
			result.put("wmsg", wmsg1);

			return result;
		}
		bjxmkmMap.put(bjxmkm, wmsg);

		String xmbj = xm + bjmc;
		if (dbxmMap.containsKey(xm) && dbxmbjMap.containsKey(xmbj)) {
			List<JSONObject> sysObj = dbxmbjMap.get(xmbj);
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

	/**
	 * 根据参数生成需插入数据
	 * 
	 * @param sp
	 * @param sessionId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<CompetitionStu> getInsertEntityByCkrs(StartImportTaskParam sp, String sessionId) throws Exception {
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

		Map<Integer, CompetitionStu> rows = (Map<Integer, CompetitionStu>) prepData.get("rowcolMap");

		List<CompetitionStu> compStuList = new ArrayList<CompetitionStu>();
		for (Map.Entry<Integer, CompetitionStu> entry : rows.entrySet()) {
			CompetitionStu row = entry.getValue();
			row.setKslc(kslcdm);
			row.setXxdm(xxdm);
			row.setXnxq(xn + xq);
			compStuList.add(row);
		}

		return compStuList;
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
			String xxdm) throws Exception {
		String excelTitleInfoKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
				.append(".importCompetitionStudent.excelTitleInfo").toString();
		List<ExcelTitleInfo> excelTitleInfoList = (List<ExcelTitleInfo>) redisOperationDAO.get(excelTitleInfoKey);

		if (excelTitleInfoKey == null) {
			throw new CommonRunException(-50, "由于长时间未操作或会话失效导致数据过期，请重新导入数据！");
		}

		String[] excelTitles = (String[]) datas.get(0);

		// 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射实体字段[3]
		String[][] arr = new String[4][excelTitles.length];
		// 无需手工匹配
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

		Map<Integer, JSONObject> rowDatas = new HashMap<Integer, JSONObject>();
		// 开始循环excel数据 第二行开始
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
		}

		int xmIndex = strIndexInArray("xm", arr[3]);
		int xhIndex = strIndexInArray("userXh", arr[3]);

		String bs = "";
		int bsix = 0;
		if (xhIndex != -1) {
			bs = "userXh";// 学号
			bsix = xhIndex;
		} else if (xmIndex != -1) {
			bs = "xm";// 姓名
			bsix = xmIndex;
		}

		JSONObject rs = new JSONObject();
		rs.put("rowDatas", rowDatas);
		rs.put("titleEnMap", titleEnMap);
		rs.put("bs", bs); // 标识
		rs.put("bsix", bsix);
		rs.put("impHead", arr[3]);

		String prepDataKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
				.append(".importScore.prepData").toString();
		redisOperationDAO.set(prepDataKey, rs, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		return rowDatas;
	}

	/**
	 * 获取竞赛组学生列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/curd/getCompStdList")
	@ResponseBody
	public JSONObject getCompStdList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		try {
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String kmdms = request.getString("subjectId");
			if (StringUtils.isBlank(kslc) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(kmdms)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			String xxdm = getXxdm(req);

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", xxdm);
			params.put("xnxq", xnxq);
			params.put("kmdms", kmdms);

			response.put("data", scoreService.getCompStdList(params));// xh,kmdm,bh

			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 清空竞赛学生
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/curd/delCompStdList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delCompStdList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		try {
			String kslc = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if (StringUtils.isBlank(kslc) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);
			params.put("stdList", request.get("stdList"));

			scoreService.delCompStdListBatch(params);

			setResponse(response, 0, "删除成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
}