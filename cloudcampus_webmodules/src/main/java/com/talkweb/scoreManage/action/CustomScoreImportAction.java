package com.talkweb.scoreManage.action;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.scoreManage.business.CustomItems;
import com.talkweb.scoreManage.proc.ClassExamExcelDetail;
import com.talkweb.scoreManage.proc.ClassExamExcelTitle;
import com.talkweb.scoreManage.proc.ProgressBar;
import com.talkweb.scoreManage.proc.ScoreProgressProc;
import com.talkweb.scoreManage.service.ClassScoreCrudService;
import com.talkweb.scoreManage.service.ScoreManageService;

@Controller
@RequestMapping("/scoreReport1/customScoreImport")
public class CustomScoreImportAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(CustomScoreImportAction.class);

	@Autowired
	private ScoreManageService scoreService;

	@Autowired
	private ClassScoreCrudService classScoreCrudService;

	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	@Autowired
	private FileServer fileServerImplFastDFS;

	@Autowired
	private FileImportInfoService fileImportInfoService;

	private final String SYSFIELD_CLASS = "班级";
	private final String SYSFIELD_STUDENT = "姓名";
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
	 * @throws Exception
	 * 			@throws
	 */
	@RequestMapping(value = "/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(@RequestParam("excelBody") MultipartFile file, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		JSONObject response = new JSONObject();

		Workbook workbook = null;
		String fileId = null;
		String keyId = UUIDUtil.getUUID();
		try {
			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			byte[] fileBytes = file.getBytes(); // 二进制内容

			CustomItems si = new CustomItems();
			si.setSessionId(sessionId);
			si.setHeadRowNum(Integer.parseInt(req.getParameter("headRowNum")));

			String fileName = file.getOriginalFilename(); // 原始文件名称
			String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(); // 后缀转小写
			if (suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx")) {
				setPromptMessage(response, 1, "文件格式正确");
			} else {
				throw new CommonRunException(-1, "文件不是Excel格式");
			}

			fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + suffix;
			String tempName0 = keyId + "." + suffix;

			fileId = fileServerImplFastDFS.uploadFile(fileBytes, tempName0); // 上传文件倒文件服务器
			fileImportInfoService.addFile(schoolId, keyId, fileId); // 记录文件服务器代码

			String tempFileIdKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".fileId").toString();
			redisOperationDAO.set(tempFileIdKey, fileId);

			ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
			workbook = WorkbookFactory.create(inputStream);

			Sheet sheet = workbook.getSheetAt(0);
			int maxRow = sheet.getPhysicalNumberOfRows(); // 获取excel最大行数
			int headRowNum = si.getHeadRowNum(); // 获取从前台传过来的表头所占的行数
			if (maxRow > headRowNum) {
				boolean hasStudNameItem = false;
				boolean hasClassNameItem = false;
				for (int i = 0; i < headRowNum; i++) {
					Row row = sheet.getRow(i);
					for (int j = 0, len = row.getLastCellNum(); j < len; j++) {
						String value = getCellValue(row.getCell(j));
						if (StringUtils.isBlank(value)) {
							continue;
						}
						switch (j) {
						case 0:
							if (SYSFIELD_STUDENT.equals(value)) {
								hasStudNameItem = true;
								si.setHeadNameIndex(j);
							}
							break;
						case 1:
							if (SYSFIELD_CLASS.equals(value)) {
								hasClassNameItem = true;
								si.setHeadClassNameIndex(j);
							}
							break;
						default:
							break;
						}
					}
				}

				if (!hasStudNameItem) {
					throw new CommonRunException(-1, "第一列必须为姓名");
				}
				if (!hasClassNameItem) {
					throw new CommonRunException(-1, "第二列必须为班级");
				}
			} else {
				throw new CommonRunException(-2, "Excel没有数据！！");
			}

			for (int j = headRowNum; j < maxRow; j++) {
				if (sheet.getRow(j) != null) {
					for (int k = 0; k < sheet.getRow(j).getLastCellNum(); k++) {
						if (isMerged(sheet, j, k) || StringUtils.isEmpty(getCellValue(sheet.getRow(j).getCell(0)))) {
							setPromptMessage(response, -100, "表头行输入错误或学生姓名列有空值");
							break;
						}
						if (isMerged(sheet, j, k) || StringUtils.isEmpty(getCellValue(sheet.getRow(j).getCell(1)))) {
							setPromptMessage(response, -100, "表头行输入错误或班级名称列有空值");
							break;
						}
					}
				}
			}

			String salKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".customItems").toString();
			redisOperationDAO.set(salKey, si, CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
		} catch (CommonRunException e) {
			setPromptMessage(response, e.getCode(), e.getMessage());
			if (fileId != null) {
				fileServerImplFastDFS.deleteFile(fileId);
				fileImportInfoService.deleteFileByFileId(fileId);
			}
		} catch (Exception e) {
			setPromptMessage(response, -3, "Excel格式有问题,请联系管理员!");
			e.printStackTrace();
			if (fileId != null) {
				fileServerImplFastDFS.deleteFile(fileId);
				fileImportInfoService.deleteFileByFileId(fileId);
			}
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}
		return response;
	}

	private void setPromptMessage(JSONObject object, int code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}

	/**
	 * 导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/startImportTask")
	@ResponseBody
	public synchronized JSONObject startImportTask(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String examId = request.getString("examId");
			String termInfoId = request.get("termInfoId").toString();
			if (StringUtils.isBlank(examId) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			HttpSession session = req.getSession();
			String sessionId = session.getId();
			String schoolId = getXxdm(req);
			// ***获取teacherNameMap信息 并存入session 以便run中使用
			ProgressBar progress = new ProgressBar();
			progress.setProgressInfo(0, 0, "开始启动导入程序...");
			String progressKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".progress").toString();
			redisOperationDAO.set(progressKey, progress, CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			String prepDataKey = new StringBuffer().append("customScore.").append(getXxdm(req)).append(sessionId)
					.append(".importScore.prepData").toString();
			JSONObject prepData = new JSONObject();
			redisOperationDAO.set(prepDataKey, prepData, CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());

			SubProcess sp = new SubProcess(schoolId, examId, sessionId, termInfoId);
			sp.start();

			setPromptMessage(response, 0, "正常启动任务");
		} catch (CommonRunException e) {
			setPromptMessage(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, -2, "服务器异常，请联系管理员！");
		}
		return response;
	}

	public boolean isDigits(String str) {
		return str.matches("^[-\\+]?\\d+(\\.\\d+)?$");
	}

	class SubProcess extends Thread {
		private String schoolId;
		private String examId;
		private String termInfoId;
		private String xnxq;

		private ProgressBar progress = new ProgressBar();
		private String salKey = null;
		private String progressKey = null;
		private String prepDataKey = null;
		private String fileIdKey = null;

		public SubProcess(String schoolId, String examId, String sessionId, String termInfoId) {
			this.schoolId = schoolId;
			this.examId = examId;
			this.termInfoId = termInfoId;
			this.xnxq = termInfoId;

			this.salKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".customItems").toString();
			this.progressKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".progress").toString();
			this.prepDataKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".importScore.prepData").toString();
			this.fileIdKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".fileId").toString();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Workbook workbook = null;
			CustomItems si = null;
			try {
				si = (CustomItems) redisOperationDAO.get(salKey);
				if (si == null) {
					progress.setProgressInfo(-50, 100, "由于长时间未操作，请重新导入数据！");
					redisOperationDAO.set(progressKey, progress,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					return;
				}
				String fileID = (String) redisOperationDAO.get(fileIdKey);

				byte[] bytes = fileServerImplFastDFS.downloadFile(fileID);
				if (bytes == null || bytes.length == 0) {
					throw new CommonRunException(-1, "excel文件已被删除，请重新上传并导入数据！");
				}

				if (fileID.endsWith("xls")) {
					workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes));
				} else if (fileID.endsWith("xlsx")) {
					workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
				} else {
					workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes));
				}

				fileImportInfoService.deleteFileByFileId(fileID);
				fileServerImplFastDFS.deleteFile(fileID);

				Sheet sheet = workbook.getSheetAt(0);
				int maxRow = sheet.getPhysicalNumberOfRows();
				// 读取Excel中的数据
				progress.setProgressInfo(1, 5, "正在读取校验excel数据");
				redisOperationDAO.set(progressKey, progress, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

				JSONObject preData = (JSONObject) redisOperationDAO.get(prepDataKey);
				if (null == preData) {
					progress.setProgressInfo(-50, 100, "由于长时间未操作，请重新导入");
					redisOperationDAO.set(progressKey, progress,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				}

				JSONObject dbckMap = preData.getJSONObject("dbckMap");
				if (dbckMap == null) {
					JSONObject map = new JSONObject();
					map.put("xxdm", schoolId);
					map.put("xnxq", termInfoId);
					map.put("xn", xnxq.substring(0, xnxq.length() - 1));
					ScoreProgressProc spThread = new ScoreProgressProc("正在查询学生数据...", 1, 5, 5, 6,
							progressKey.toString());
					spThread.start();
					dbckMap = scoreService.getAllStuByParam(map);
					preData.put("dbckMap", dbckMap);
				}

				progress.setProgressInfo(1, 32, "获取对比数据完成，正在校验excel数据");
				redisOperationDAO.set(progressKey, progress, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

				Map<String, List<JSONObject>> dbxmbjMap = (Map<String, List<JSONObject>>) dbckMap.get("xmbjMap");
				Map<String, List<JSONObject>> dbxmMap = (Map<String, List<JSONObject>>) dbckMap.get("xmMap"); // 学号与教师代码(工号)映射表

				// 设置Excel表头
				si.setL_ClassExamExcelTitle(getClassExamExcelTitles(sheet, si.getHeadRowNum(), examId, schoolId, xnxq));

				String[] excelTeachers = new String[maxRow - si.getHeadRowNum()];
				List<ClassExamExcelDetail> successInfos = new ArrayList<ClassExamExcelDetail>();
				List<JSONObject> errorInfos = new ArrayList<JSONObject>();

				JSONObject rowdata = new JSONObject();
				Map<String, String> semap = new HashMap<String, String>();
				for (ClassExamExcelTitle se : si.getL_ClassExamExcelTitle()) {
					semap.put(se.getCellId(), se.getTitleName());
				}
				rowdata.put("semap", semap);

				if (!preData.containsKey("xmbjMap")) {
					preData.put("xmbjMap", new HashMap<String, JSONObject>());
				}
				Map<String, JSONObject> xmbjMap = (Map<String, JSONObject>) preData.get("xmbjMap");

				for (int i = si.getHeadRowNum(); i < maxRow; i++) {
					Row row = sheet.getRow(i);

					String bjtmp = getCellValue(row.getCell(si.getHeadClassNameIndex()));
					String bj = "";
					if (!bjtmp.isEmpty()) {
						if (bjtmp.indexOf(".") > -1) {
							bj = bjtmp.substring(0, bjtmp.indexOf("."));
						} else {
							bj = bjtmp;
						}
					}
					String xm = getCellValue(row.getCell(si.getHeadNameIndex())); // 姓名或者手机号

					String rowNum = String.valueOf(i + 1);

					rowdata.put(rowNum, new JSONObject());
					JSONObject rowd = rowdata.getJSONObject(rowNum);
					rowd.put("xm", xm);
					rowd.put("bj", bj);
					Map<String, String> colmap = new HashMap<String, String>();
					rowd.put("colmap", colmap);

					ClassExamExcelDetail sd = getRowClassExamExcelDetail(sheet, i, si);
					sd.setExamId(examId);
					sd.setSchoolId(schoolId);
					sd.setXnxq(xnxq);

					List<JSONObject> errInfoList = checkToGetErrInfoList(xm, bj, rowNum, sd, xmbjMap, dbxmMap,
							dbxmbjMap, 1);
					
					errorInfos.addAll(errInfoList);
					
					if (!sd.getErrorlist().isEmpty()) {
						for (JSONObject errObj : sd.getErrorlist()) {
							String cellId = errObj.getString("cellId");
							String value = errObj.getString("value");

							String projectName = "";
							if (semap.containsKey(cellId)) {
								projectName = semap.get(cellId);
							}

							JSONObject err = new JSONObject();
							err.put("rowNum", rowNum);
							err.put("oldValue", value);
							err.put("err", errObj.get("err"));
							err.put("title", projectName);
							err.put("col", cellId);
							err.put("ClassExamExcelDetail", sd);
							errorInfos.add(err);

							colmap.put(cellId, value);
						}
					}
					
					if (errInfoList.size() > 0) {
						continue;
					}

					String xmbj = xm + bj;
					List<JSONObject> stuList = dbxmbjMap.get(xmbj);
					String sysXh = stuList.get(0).getString("userId");
					String sysBh = stuList.get(0).getString("bh");
					sd.setStuId(sysXh);
					sd.setClassId(sysBh);
					sd.setStuName(xm);
					sd.setClassName(bj);

					successInfos.add(sd);

					JSONObject stu = new JSONObject();
					stu.put("rowNum", rowNum);
					stu.put("xm", xm);
					stu.put("bj", bj);
					stu.put("count", 1);
					stu.put("sd", sd);
					xmbjMap.put(xmbj, stu);
				}

				for (String xmbj : new ArrayList<String>(xmbjMap.keySet())) {
					JSONObject stu = xmbjMap.get(xmbj);
					if (stu.getIntValue("count") > 1) {
						xmbjMap.remove(xmbj);
						List<ClassExamExcelDetail> list = new ArrayList<ClassExamExcelDetail>();
						for(ClassExamExcelDetail detail : successInfos) {
							String xm = detail.getStuName();
							String bj = detail.getClassName();
							if(xmbj.equals(xm + bj)) {
								list.add(detail);
							}
						}
						successInfos.removeAll(list);
					}
				}
				
				preData.put("rowdatas", rowdata);

				si.setSuccessInfos(successInfos);
				si.setErrorInfos(errorInfos);
				si.setExcelTeachers(excelTeachers);
				redisOperationDAO.set(salKey, si, CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());

				preData.put("xmbjMap", xmbjMap);
				redisOperationDAO.set(prepDataKey, preData,
						CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());

				if (errorInfos.size() > 0) {
					JSONObject res = getValidateMsg(errorInfos);
					List<JSONObject> siz = (List<JSONObject>) res.get("rows");
					int errNum = siz != null ? siz.size() : 0;
					progress.setProgressInfo(-2, 100, "共" + (errNum + successInfos.size()) + "条记录："
							+ successInfos.size() + "条导入成功," + errNum + "条导入失败");
					progress.setOtherData("total", successInfos.size());
					progress.setOtherData("validateMsg", res);
					redisOperationDAO.set(progressKey, progress,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} else {
					progress.setProgressInfo(1, 30, "正在保存数据！");
					redisOperationDAO.set(progressKey, progress,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

					classScoreCrudService.addImport(successInfos, si.getL_ClassExamExcelTitle());
					progress.setProgressInfo(2, 100, "恭喜，导入成绩结束，共导入" + (successInfos.size()) + "条成绩！");

					redisOperationDAO.set(progressKey, progress,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				}
			} catch (Exception e) {
				e.printStackTrace();
				progress.setProgressInfo(-50, 100, "服务器异常，请联系管理员！");
				try {
					redisOperationDAO.set(progressKey, progress,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private List<JSONObject> checkToGetErrInfoList(String xm, String bjmc, String rowNum, ClassExamExcelDetail sd,
			Map<String, JSONObject> xmbjMap, Map<String, List<JSONObject>> dbxmMap,
			Map<String, List<JSONObject>> dbxmbjMap, int type) {
		List<JSONObject> errInfoList = new ArrayList<JSONObject>();

		boolean checkPass = true;
		if (StringUtils.isBlank(xm)) {
			JSONObject err = new JSONObject();
			err.put("rowNum", rowNum);
			err.put("oldValue", xm);
			err.put("err", "姓名不能为空！");
			err.put("title", "姓名");
			err.put("ClassExamExcelDetail", sd);
			errInfoList.add(err);
			checkPass = false;
		}

		if (StringUtils.isBlank(bjmc)) {
			JSONObject err = new JSONObject();
			err.put("rowNum", rowNum);
			err.put("oldValue", bjmc);
			err.put("err", "班级不能为空！");
			err.put("title", "班级");
			err.put("ClassExamExcelDetail", sd);
			errInfoList.add(err);
			checkPass = false;
		}

		if (!checkPass) {
			return errInfoList;
		}

		String xmbj = xm + bjmc;
		if (xmbjMap.containsKey(xmbj)) {
			JSONObject stu = xmbjMap.get(xmbj);
			int count = stu.getIntValue("count");
			stu.put("count", count + 1); // 冲突数

			JSONObject err = null;
			if(type == 1) {
				err = new JSONObject();
				err.put("rowNum", stu.get("rowNum"));
				err.put("oldValue", stu.get("xm"));
				err.put("err", "姓名班级同第" + rowNum + "行重复！");
				err.put("title", "姓名");
				err.put("ClassExamExcelDetail", stu.get("sd"));
				errInfoList.add(err);

				err = new JSONObject();
				err.put("rowNum", stu.get("rowNum"));
				err.put("oldValue", stu.get("bj"));
				err.put("err", "姓名班级同第" + rowNum + "行重复！");
				err.put("title", "班级");
				err.put("ClassExamExcelDetail", stu.get("sd"));
				errInfoList.add(err);
			}

			err = new JSONObject();
			err.put("rowNum", rowNum);
			err.put("oldValue", xm);
			err.put("err", "姓名班级同第" + stu.get("rowNum") + "行重复！");
			err.put("title", "姓名");
			err.put("ClassExamExcelDetail", sd);
			errInfoList.add(err);

			err = new JSONObject();
			err.put("rowNum", rowNum);
			err.put("oldValue", bjmc);
			err.put("err", "姓名班级同第" + stu.get("rowNum") + "行重复！");
			err.put("title", "班级");
			err.put("ClassExamExcelDetail", sd);
			errInfoList.add(err);

			checkPass = false;
		}

		if (!checkPass) {
			return errInfoList;
		}

		List<JSONObject>  stuList = dbxmbjMap.get(xmbj);
		if (stuList == null) {
			JSONObject err = new JSONObject();
			err.put("rowNum", rowNum);
			err.put("oldValue", xm);
			err.put("err", "与系统不匹配！");
			err.put("title", "姓名");
			err.put("ClassExamExcelDetail", sd);
			errInfoList.add(err);

			err = new JSONObject();
			err.put("rowNum", rowNum);
			err.put("oldValue", bjmc);
			err.put("err", "与系统不匹配！");
			err.put("title", "班级");
			err.put("ClassExamExcelDetail", sd);
			errInfoList.add(err);

			checkPass = false;
		}

		if (stuList != null && stuList.size() > 1) {
			JSONObject err = new JSONObject();
			err.put("rowNum", rowNum);
			err.put("oldValue", xm);
			err.put("err", "系统中存在同名学生，请使用身份证或手机号码代替名字！");
			err.put("title", "姓名");
			err.put("ClassExamExcelDetail", sd);
			errInfoList.add(err);

//			err = new JSONObject();
//			err.put("rowNum", rowNum);
//			err.put("oldValue", bjmc);
//			err.put("err", "系统中存在多条该信息！");
//			err.put("title", "班级");
//			err.put("ClassExamExcelDetail", sd);
//			errInfoList.add(err);

			checkPass = false;
		}

		return errInfoList;
	}

	/**
	 * 获取导入进度
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/importProgress", method = RequestMethod.POST)
	@ResponseBody
	public ProgressBar importProgress(HttpServletRequest req, HttpServletResponse res) throws Exception {
		String sessionId = req.getSession().getId();
		String progressKey = new StringBuffer().append("customScore.").append(getXxdm(req)).append(sessionId)
				.append(".progress").toString();
		ProgressBar progress = (ProgressBar) redisOperationDAO.get(progressKey);
		if (null == progress) {
			progress = new ProgressBar();
			progress.setProgressInfo(-50, 100, "由于长时间未操作，请重新导入");
		}
		return progress;
	}

	/**
	 * 导入数据单条验证
	 * 
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/singleDataCheck", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject singleDataCheck(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String rowNum = request.getString("row");
			int code = request.getIntValue("code");
			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			String salKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".customItems").toString();
			String progressKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".progress").toString();
			String prepDataKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".importScore.prepData").toString();

			CustomItems si = (CustomItems) redisOperationDAO.get(salKey);
			if (si == null) {
				throw new CommonRunException(-50, "由于长时间未操作，请重新导入");
			}

			List<JSONObject> errorInfos = si.getErrorInfos();
			List<ClassExamExcelDetail> successInfos = si.getSuccessInfos();

			logger.info("***********************singleDataCheckSalaryerrorInfo********************:" + errorInfos);
			int errorIndex = -1;
			for (int i = 0; i < errorInfos.size(); i++) {
				if (rowNum.equals(errorInfos.get(i).getString("rowNum"))) {
					errorIndex = i;
					break;
				}
			}
			ClassExamExcelDetail sd = (ClassExamExcelDetail) errorInfos.get(errorIndex).get("ClassExamExcelDetail");
			
			ProgressBar progress = (ProgressBar) redisOperationDAO.get(progressKey);
			// 忽略错误信息
			if (code == -1) {
				errorInfos.remove(errorIndex);
				si.setErrorInfos(errorInfos);
				redisOperationDAO.set(salKey, si, CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
				setPromptMessage(response, 1, "通过");
				
			} else {
				JSONArray j_rows = request.getJSONArray("mrows");
				JSONObject preData = (JSONObject) redisOperationDAO.get(prepDataKey);
				if (null == progress || null == preData) {
					progress.setProgressInfo(-50, 100, "由于长时间未操作，请重新导入");
					redisOperationDAO.set(progressKey, progress,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				}

				JSONObject dbckMap = preData.getJSONObject("dbckMap");
				JSONObject rowdatas = preData.getJSONObject("rowdatas");
				Map<String, List<JSONObject>> dbxmbjMap = (Map<String, List<JSONObject>>) dbckMap.get("xmbjMap");
				Map<String, List<JSONObject>> dbxmMap = (Map<String, List<JSONObject>>) dbckMap.get("xmMap");

				if (!preData.containsKey("xmbjMap")) {
					preData.put("xmbjMap", new HashMap<String, JSONObject>());
				}
				Map<String, JSONObject> xmbjMap = (Map<String, JSONObject>) preData.get("xmbjMap");

				JSONObject rowdata = rowdatas.getJSONObject(rowNum);
				Map<String, String> semap = (Map<String, String>) rowdatas.get("semap");
				String xm = rowdata.getString("xm");
				String bj = rowdata.getString("bj");
				Map<String, String> colmap = (Map<String, String>) rowdata.get("colmap");

				for (int i = 0; i < j_rows.size(); i++) {
					JSONObject mrows = j_rows.getJSONObject(i);
					String title = mrows.getString("title");
					if (title.equals("姓名")) {
						xm = mrows.getString("value");
					} else if (title.equals("班级")) {
						bj = mrows.getString("value");
					}
				}
				
				List<JSONObject> errInfoList = checkToGetErrInfoList(xm, bj, rowNum, sd, xmbjMap, dbxmMap,
						dbxmbjMap, 2);
				
				if(errInfoList.size() == 0) {
					rowdata.put("xm", xm);
					rowdata.put("bj", bj);
					redisOperationDAO.set(prepDataKey, preData,
							CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
				}
				
				for (int i = 0; i < j_rows.size(); i++) {
					JSONObject mrows = j_rows.getJSONObject(i);
					if (mrows.containsKey("col")) {
						String value = mrows.getString("value");
						if (value.length() > 50) {
							String projectName = "";
							if (semap.containsKey(mrows.getString("col"))) {
								projectName = semap.get(mrows.getString("col"));
							}
							JSONObject err = new JSONObject();
							err.put("title", projectName);
							err.put("oldValue", value);
							err.put("err", "内容文字超过50字");
							err.put("col", mrows.getString("col"));
							errInfoList.add(err);
						}
						colmap.put(mrows.getString("col"), value);
						redisOperationDAO.set(prepDataKey, preData,
								CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
					}
				}
				
				if (errInfoList.size() > 0) {
					response.put("mrows", errInfoList);
					setPromptMessage(response, -1, "校验数据失败");
					return response;
				}
				
				rowdata.put("xm", xm);
				rowdata.put("bj", bj);
				rowdata.put("colmap", colmap);

				setPromptMessage(response, 1, "校验通过");
				
				String xmbj = xm + bj;
				JSONObject stu = new JSONObject();
				stu.put("rowNum", rowNum);
				stu.put("xm", xm);
				stu.put("bj", bj);
				stu.put("count", 1);
				xmbjMap.put(xmbj, stu);
				
				List<JSONObject> stuList = dbxmbjMap.get(xmbj);
				sd.setStuId(stuList.get(0).getString("userId"));
				sd.setClassId(stuList.get(0).getString("bh"));

				for (Map.Entry<String, String> entry : colmap.entrySet()) {
					sd.getCellIds().add(entry.getKey());
					sd.getCellValues().add(entry.getValue());
				}

				successInfos.add(sd);
				errorInfos.remove(errorIndex);

				redisOperationDAO.set(salKey, si, CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
				redisOperationDAO.set(prepDataKey, preData,
						CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
			}
			
		} catch (CommonRunException e) {
			setPromptMessage(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 验证通过后继续导入
	 * 
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/continueImport")
	@ResponseBody
	public JSONObject continueImport(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String schoolId = getXxdm(req);
			HttpSession session = req.getSession();
			String sessionId = session.getId();

			String salKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".customItems").toString();
			String progressKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".progress").toString();

			ProgressBar progress = new ProgressBar();

			CustomItems si = (CustomItems) redisOperationDAO.get(salKey);
			if (si == null) {
				setPromptMessage(response, -50, "由于长时间未操作，请重新导入");
			}

			List<ClassExamExcelDetail> successInfos = si.getSuccessInfos();
			if (CollectionUtils.isEmpty(successInfos)) {
				successInfos = new ArrayList<ClassExamExcelDetail>();
			}
			if (successInfos.size() > 0) {
				classScoreCrudService.addImport(successInfos, si.getL_ClassExamExcelTitle());
			}
			String msg = "恭喜，导入成绩结束，共导入" + (successInfos.size()) + "条成绩！";
			progress.setProgressInfo(2, 100, msg);
			redisOperationDAO.set(progressKey, progress, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			setPromptMessage(response, 1, msg);
		} catch (CommonRunException e) {
			setPromptMessage(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 导出异常信息
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(value = "/exportWrongMsg")
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

	public JSONObject getValidateMsg(List<JSONObject> errorInfos) {
		Map<String, List<JSONObject>> mrowmap = new LinkedHashMap<String, List<JSONObject>>();

		for (JSONObject errInfo : errorInfos) {
			String rowNum = errInfo.getString("rowNum");
			errInfo.remove("rowNum");
			if (!mrowmap.containsKey(rowNum)) {
				mrowmap.put(rowNum, new ArrayList<JSONObject>());
			}
			mrowmap.get(rowNum).add(errInfo);
		}

		List<JSONObject> l_object = new ArrayList<JSONObject>();
		for (Map.Entry<String, List<JSONObject>> entry : mrowmap.entrySet()) {
			String rowNum = entry.getKey();
			List<JSONObject> mrows = entry.getValue();

			JSONObject rowInfo = new JSONObject();
			rowInfo.put("row", rowNum);
			rowInfo.put("mrows", mrows);
			l_object.add(rowInfo);
		}

		JSONObject result = new JSONObject();
		result.put("total", errorInfos.size());
		result.put("rows", l_object);
		return result;
	}

	/**
	 * 获取合并单元格的值
	 * 
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public String getMergedRegionValue(Sheet sheet, int row, int col) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if ((row >= firstRow && row <= lastRow)) {
				if (col >= firstCol && col <= lastCol) {
					Cell fCell = sheet.getRow(firstRow).getCell(firstCol);
					return getCellValue(fCell);
				}
			}
		}
		return getCellValue(sheet.getRow(row).getCell(col));
	}

	/**
	 * 判断制定的单元格是否是合并单元格
	 * 
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	private boolean isMerged(Sheet sheet, int row, int col) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if ((row >= firstRow && row <= lastRow)) {
				if ((col >= firstCol && col <= lastCol)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 得到指定单元格或合并单元格的序号
	 * 
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public String getCellId(Sheet sheet, int row, int col) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if ((row >= firstRow && row <= lastRow)) {
				if ((col >= firstCol && col <= lastCol)) {
					return "r" + firstRow + "c" + firstCol;
				}
			}
		}
		return "r" + row + "c" + col;
	}

	/**
	 * 得到指定合并单元格的行的跨度
	 * 
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public int getRowdiff(Sheet sheet, int row, int col) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if ((row >= firstRow && row <= lastRow)) {
				if (col >= firstCol && col <= lastCol) {
					return lastRow - firstRow;
				}
			}
		}
		return 0;
	}

	/**
	 * 得到指定合并单元格的列的跨度
	 * 
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public int getColdiff(Sheet sheet, int row, int col) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if ((row >= firstRow && row <= lastRow)) {
				if (col >= firstCol && col <= lastCol) {
					return lastCol - firstCol;
				}
			}
		}
		return 0;
	}

	/**
	 * 合并指定的单元格
	 * 
	 * @param sheet
	 * @param firstRow
	 * @param firstCol
	 * @param lastRow
	 * @param lastCol
	 */
	public void mergeRegion(Sheet sheet, int firstRow, int firstCol, int lastRow, int lastCol) {
		CellRangeAddress range = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		sheet.addMergedRegion(range);
	}

	/**
	 * 获取单元格的值
	 * 
	 * @param cell
	 * @return
	 */
	public String getCellValue(Cell cell) {
		if (cell == null)
			return "";

		String result = null;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			result = cell.getRichStringCellValue().getString().trim();
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			result = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			try {
				result = String.valueOf(cell.getStringCellValue());
			} catch (IllegalStateException e) {
				String ss = String.valueOf(cell.getNumericCellValue());
				if (ss.indexOf(".") > -1) {
					result = ss.substring(0, ss.indexOf("."));
				} else {
					result = ss;
				}
			}
			break;
		case Cell.CELL_TYPE_NUMERIC:
			double d = cell.getNumericCellValue();
			if (d % 1 > 0) {
				BigDecimal b = new BigDecimal(d);
				result = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
			} else {
				String ss = String.valueOf(d);
				if (ss.indexOf(".") > -1) {
					result = ss.substring(0, ss.indexOf("."));
				} else {
					result = ss;
				}
			}
			break;
		}
		if (result == null) {
			result = "";
		}
		return result;
	}

	public boolean isNumber(String str) {
		Pattern patter = Pattern.compile("^[-\\+]?[0-9]+(\\.[0-9]+)?$");
		return patter.matcher(str).matches();
	}

	/**
	 * 封装excel表头
	 * 
	 * @param sheet
	 * @param rowNum
	 * @return
	 */
	public List<ClassExamExcelTitle> getClassExamExcelTitles(Sheet sheet, int rowNum, String examId, String schoolId,
			String xnxq) {
		List<ClassExamExcelTitle> l_ClassExamExcelTitles = new ArrayList<ClassExamExcelTitle>();
		Set<String> cellIdSet = new HashSet<String>();
		for (int k = 0; k < rowNum; k++) {
			Row row = sheet.getRow(k);

			int colspan = 1; // 列跨度
			for (int i = 0, len = row.getLastCellNum(); i < len; i = i + colspan) {
				String cellId = getCellId(sheet, k, i);
				colspan = getColdiff(sheet, k, i) + 1;
				if (cellIdSet.contains(cellId)) {
					continue;
				}
				cellIdSet.add(cellId);

				int rowspan = getRowdiff(sheet, k, i) + 1; // 行跨度

				int r_index = cellId.indexOf("r");
				int c_index = cellId.indexOf("c");
				int curRow = Integer.parseInt(cellId.substring(r_index + 1, c_index));
				int curCol = Integer.parseInt(cellId.substring(c_index + 1));

				ClassExamExcelTitle se = new ClassExamExcelTitle();
				se.setExamId(examId);
				se.setSchoolId(schoolId);
				se.setHead_rowNum(rowNum); // 表头行数
				se.setXnxq(xnxq);

				se.setCellId(cellId);
				se.setIn_colNum(curCol);
				se.setIn_rowNum(curRow);

				se.setRowspan(rowspan);
				se.setColspan(colspan);
				se.setTitleName(getCellValue(sheet.getRow(k).getCell(i)));

				if (curRow == 0) {
					se.setP_cellId(null);
				} else {
					se.setP_cellId(getCellId(sheet, curRow - 1, i));
				}
				l_ClassExamExcelTitles.add(se);
			}
		}
		// 去掉重复元素
		Set<ClassExamExcelTitle> ss = new HashSet<ClassExamExcelTitle>(l_ClassExamExcelTitles);
		return new ArrayList<ClassExamExcelTitle>(ss);
	}

	/**
	 * 十进制转26进制
	 * 
	 * @param num
	 * @return
	 */
	private String toNumberSystem26(int num) {
		num += 1;
		StringBuffer s = new StringBuffer();
		while (num > 0) {
			int m = num % 26;
			if (m == 0)
				m = 26;
			s.append((char) (m + 64));
			num = (num - m) / 26;
		}
		return s.reverse().toString();
	}

	/**
	 * 得到一行的工资信息
	 * 
	 * @param sheet
	 * @param row
	 * @param titleIndex
	 * @return
	 */
	public ClassExamExcelDetail getRowClassExamExcelDetail(Sheet sheet, int row, CustomItems si) {
		int headRowNumber = si.getHeadRowNum();

		ClassExamExcelDetail sd = new ClassExamExcelDetail();
		List<String> cellIds = sd.getCellIds();
		List<String> cellValues = sd.getCellValues();
		List<JSONObject> errorlist = sd.getErrorlist();

		for (int j = 0, total = sheet.getRow(row).getLastCellNum(); j < total; j++) {
			try {
				if (j == si.getHeadNameIndex() || j == si.getHeadClassNameIndex()
						|| (si.getHeadPhoneIndex() != null && j == si.getHeadPhoneIndex())
						|| (si.getHeadIDCardIndex() != null && j == si.getHeadIDCardIndex())) {
					continue;
				}

				String value = getCellValue(sheet.getRow(row).getCell(j));
				if (value.length() > 50) {
					JSONObject err = new JSONObject();
					err.put("num", toNumberSystem26(j));
					err.put("cellId", getCellId(sheet, headRowNumber - 1, j));
					err.put("value", value);
					err.put("err", "内容文字超过50字");
					errorlist.add(err);
				} else {
					cellIds.add(getCellId(sheet, headRowNumber - 1, j));
					cellValues.add(value);
				}
			} catch (IllegalStateException e) {
				JSONObject err = new JSONObject();
				err.put("num", toNumberSystem26(j));
				err.put("cellId", getCellId(sheet, headRowNumber - 1, j));
				err.put("value", "");
				err.put("err", "内容文字有异常");
				errorlist.add(err);
			}
		}
		return sd;
	}

	/**
	 * 得到所有的工资表详细信息（不包含teacherId 和teacherName）
	 * 
	 * @param sheet
	 * @param rowNum
	 *            表头行数
	 * @return
	 */
	public List<ClassExamExcelDetail> getClassExamExcelDetails(Sheet sheet, int rowNum) {
		List<ClassExamExcelDetail> l_sd = new ArrayList<ClassExamExcelDetail>();
		Row row = sheet.getRow(0);
		int maxRowNum = sheet.getPhysicalNumberOfRows();
		for (int i = rowNum; i < maxRowNum; i++) {
			ClassExamExcelDetail sd = new ClassExamExcelDetail();
			List<String> cellIds = new ArrayList<String>();
			List<String> cellValues = new ArrayList<String>();
			for (int j = 1; j < row.getLastCellNum(); j++) {
				cellIds.add(getCellId(sheet, rowNum - 1, j));
				String salNum = getCellValue(sheet.getRow(i).getCell(j));
				cellValues.add(salNum);
			}
			sd.setCellIds(cellIds);
			sd.setCellValues(cellValues);
			l_sd.add(sd);
		}
		return l_sd;
	}

	/**
	 * 全部忽略验证错误
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/ignoreAllError")
	@ResponseBody
	public synchronized JSONObject ignoreAllError(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) throws Exception {
		JSONObject response = new JSONObject();
		try {
			String sessionId = req.getSession().getId();
			String schoolId = getXxdm(req);

			String salKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".customItems").toString();
			String progressKey = new StringBuffer().append("customScore.").append(schoolId).append(sessionId)
					.append(".progress").toString();

			int code = request.getIntValue("code");

			CustomItems si = (CustomItems) redisOperationDAO.get(salKey);
			if (si == null) {
				setPromptMessage(response, -50, "由于长时间未操作，请重新导入");
				return response;
			}

			List<JSONObject> errorInfos = si.getErrorInfos();
			ProgressBar progress = (ProgressBar) redisOperationDAO.get(progressKey);
			if (progress == null) {
				setPromptMessage(response, -50, "由于长时间未操作，请重新导入");
				return response;
			}
			if (code == -1) {
				JSONObject validateMsg = (JSONObject) progress.getOtherData("validateMsg");
				JSONArray mrows = validateMsg == null ? null : validateMsg.getJSONArray("rows");
				if (CollectionUtils.isNotEmpty(mrows)) {
					errorInfos.clear();
					si.setErrorInfos(errorInfos);

					progress.setProgressInfo(1, 35, "正在保存数据");

					redisOperationDAO.set(salKey, si, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					redisOperationDAO.set(progressKey, progress,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					setPromptMessage(response, 0, "忽略成功!");
				} else {
					setPromptMessage(response, -1, "需要忽略的校验错误为空!");
				}
			}
		} catch (CommonRunException e) {
			setPromptMessage(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}
}
