package com.talkweb.discipline.action;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.action.ListComparetor;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.discipline.domain.DisciplineDetail;
import com.talkweb.discipline.domain.DisciplineExcel;
import com.talkweb.discipline.domain.DisciplineItems;
import com.talkweb.discipline.service.DisciplineService;
import com.talkweb.filemanager.service.FileServer;

@RequestMapping("/discipline")
@Controller
public class DisciplineAction extends BaseAction {

	Logger logger = LoggerFactory.getLogger(DisciplineAction.class);
	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private DisciplineService disciplineService;

	@Autowired
	private FileServer fileServerImplFastDFS;
	@Autowired
	private FileImportInfoService fileImportInfoService;

	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	private final String SYSFIELD_CLASS = "班级";

	private static ListComparetor comparetor = new ListComparetor();

	static ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant");

	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}

	// 教师档案：cs1042 班级班风cs1043 教师考评 cs1017

	@RequestMapping(value = "/getRole", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDisciplineRole(HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("isAdmin", 0);
		User user = (User) req.getSession().getAttribute("user");
		if (user != null) {
			if (user.getUserPart().getRole() == T_Role.Teacher) {
				response.put("isTeacher", 1);
				boolean flag = isMoudleManager(req, "cs1043");
				if (flag) {
					response.put("isAdmin", 1);
				}
			} else if (user.getUserPart().getRole() == T_Role.Parent
					|| user.getUserPart().getRole() == T_Role.Student) {
				response.put("isParent", 1);
			}
		}
		if (response.getInteger("isTeacher") == null) {
			response.put("isTeacher", 0);
		}
		if (response.getInteger("isParent") == null) {
			response.put("isParent", 0);
		}
		setPromptMessage(response, "1", "查询成功");
		return response;
	}

	@RequestMapping(value = "/getDisciplineList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDisciplineList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		boolean flag = isMoudleManager(req, "cs1043");
		if (!flag) {
			User user = (User) req.getSession().getAttribute("user");
			if (user != null) {
				if (user.getUserPart().getRole() == T_Role.Teacher) {
					param.put("publishTeacherFlag", 1);
				} else if (user.getUserPart().getRole() == T_Role.Parent
						|| user.getUserPart().getRole() == T_Role.Student) {
					param.put("publishFlag", 1);
				}
			}
		}

		List<JSONObject> list = disciplineService.getDisciplineList(param);
		setPromptMessage(response, "1", "查询成功");
		response.put("data", list);
		return response;
	}

	@RequestMapping(value = "/getAPPDisciplineList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPDisciplineList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String schoolId = param.getString("schoolId");
		String period = param.getString("period");
		String xnxq = param.getString("xnxq");
		if (schoolId == null || period == null || xnxq == null) {
			setPromptMessage(response, "-1", "参数不能为空");
			return response;
		}
		boolean flag = isMoudleManager(req, "cs1043");
		if (!flag) {
			String role = param.getString("Role");
			if (role != null) {
				if (role.equals("Teacher") || role.equals("SystemManager")) {
					param.put("publishTeacherFlag", 1);
				} else if (role.equals("Parent") || role.equals("Student")) {
					param.put("publishFlag", 1);
				}
			}
		}
		List<JSONObject> list = disciplineService.getDisciplineList(param);
		setPromptMessage(response, "1", "查询成功");
		response.put("data", list);
		return response;
	}

	@RequestMapping(value = "/addDiscipline", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addDiscipline(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String xnxq = request.getString("xnxq");
		String disciplineName = request.getString("disciplineName");
		if (!StringUtils.isEmpty(xnxq) && !StringUtils.isEmpty(disciplineName)) {
			String disciplineId = UUIDUtil.getUUID();
			param.put("xnxq", xnxq);
			param.put("disciplineId", disciplineId);
			param.put("disciplineName", disciplineName);
			param.put("schoolId", getXxdm(req));
			param.put("createDate", new Date());
			param.put("isImported", 0);
			param.put("isPublished", 0);
			param.put("publishFlag", 0);
			param.put("publishTeacherFlag", 0);
			int result = disciplineService.addDiscipline(param);
			if (result > 0) {
				setPromptMessage(response, "1", "创建班纪班风成功");
			} else {
				setPromptMessage(response, "-1", "创建班纪班风异常");
			}
		} else {
			setPromptMessage(response, "-1", "班纪班风名称不能为空");
		}
		return response;

	}

	@RequestMapping(value = "/updateDiscipline", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateDiscipline(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String disciplineName = request.getString("disciplineName");
		String disciplineId = request.getString("disciplineId");
		if (!StringUtils.isEmpty(disciplineId) && !StringUtils.isEmpty(disciplineName)) {
			param.put("disciplineId", disciplineId);
			param.put("disciplineName", disciplineName);
			int result = disciplineService.updateDiscipline(param);
			if (result > 0) {
				setPromptMessage(response, "1", "创建班纪班风成功");
			} else {
				setPromptMessage(response, "-1", "创建班纪班风异常");
			}
		} else {
			setPromptMessage(response, "-1", "班纪班风名称不能为空");
		}
		return response;

	}

	@RequestMapping(value = "/getDisciplineWeek", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDisciplineWeek(HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		List<JSONObject> data = new ArrayList<JSONObject>();
		for (int i = 1; i <= 20; i++) {
			JSONObject o = new JSONObject();
			o.put("value", i);
			o.put("text", "第" + i + "周");
			data.add(o);
		}
		response.put("data", data);
		setPromptMessage(response, "1", "");
		return response;
	}

	@RequestMapping(value = "/updateDisciplinePublished", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateDisciplinePublished(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String disciplineId = request.getString("disciplineId");
		Integer publishFlag = request.getIntValue("publishFlag");
		Integer publishTeacherFlag = request.getIntValue("publishTeacherFlag");
		param.put("publishFlag", publishFlag);
		param.put("publishTeacherFlag", publishTeacherFlag);
		if ((publishFlag != null && publishFlag == 1) || (publishTeacherFlag != null && publishTeacherFlag == 1)) {
			param.put("isPublished", 1);
		}
		if (!StringUtils.isEmpty(disciplineId)) {
			JSONObject query = new JSONObject();
			query.put("disciplineId", disciplineId);
			JSONObject oldOne = disciplineService.getDiscipline(query);
			Integer oldPublishFlag = oldOne.getInteger("publishFlag");
			Integer oldPublishTeacherFlag = oldOne.getInteger("publishTeacherFlag");
			// 本次发布是否修改家长发布状态
			boolean flag1 = false;
			if (publishFlag != null && publishFlag == 1) {
				if (oldPublishFlag == null) {
					flag1 = true;
				} else if (oldPublishFlag != publishFlag) {
					flag1 = true;
				} else {
					flag1 = false;
				}
			}
			// 本次发布是否修改老师发布状态
			boolean flag2 = false;
			if (publishTeacherFlag != null && publishTeacherFlag == 1) {
				if (oldPublishTeacherFlag == null) {
					flag2 = true;
				} else if (publishTeacherFlag != oldPublishTeacherFlag) {
					flag2 = true;
				} else {
					flag2 = false;
				}
			}
			param.put("disciplineId", disciplineId);
			int result = disciplineService.updateDisciplinePublished(param);
			if (result > 0) {
				setPromptMessage(response, "1", "更新发布状态成功");
			} else {
				setPromptMessage(response, "-1", "更新发布状态失败");
			}
			// 推送
			/*JSONObject recordInfo = disciplineService.getRecordInfo(query);
			if (recordInfo != null) {
				Integer tchRecrd = recordInfo.getInteger("tchRecord");
				Integer parRecrd = recordInfo.getInteger("parRecord");
				if (tchRecrd != null && tchRecrd == 1) {
					flag2 = false;
				}
				if (parRecrd != null && parRecrd == 1) {
					flag1 = false;
				}
			} else {
				disciplineService.addRecordInfo(query);
			}*/
			String termInfoId = rbConstant.getString("currentTermInfo");
			query.put("termInfo", termInfoId);
			List<JSONObject> clssList = disciplineService.getAPPClassList(query);
			List<String> queyList = new ArrayList<>();
			for (JSONObject obj : clssList) {
				queyList.add(obj.getString("classId"));
			}
			JSONObject q2 = new JSONObject();
			String schoolId = getXxdm(req);
			q2.put("schoolId", schoolId);//根据ZHXY-440添加
			q2.put("termInfoId", termInfoId);//根据ZHXY-440添加
			q2.put("classList", queyList);
			if (flag1) {
				List<JSONObject> parList = disciplineService.queryParentInfos(q2);
				logger.info("updateDisciplinePublished parList : " + parList);
				JSONArray msgCenterReceiversArray = new JSONArray();
				for (JSONObject obj : parList) {
					JSONObject msgCenterReceiver = new JSONObject();
					msgCenterReceiver.put("userType", 4);
					msgCenterReceiver.put("userId", obj.getString("extUserId"));//根据ZHXY-440修改
					msgCenterReceiver.put("userName", obj.getString("name"));
					msgCenterReceiversArray.add(msgCenterReceiver);
				}
				disciplineService.sendKafka(oldOne, false, msgCenterReceiversArray);
				query.put("parRecord", 1);
				disciplineService.updateRecordInfo(query);
			}
			if (flag2) {
				List<JSONObject> tchList = disciplineService.queryTeacherInfos(q2);
				logger.info("updateDisciplinePublished tchList : " + tchList);
				JSONArray msgCenterReceiversArray = new JSONArray();
				for (JSONObject obj : tchList) {
					JSONObject msgCenterReceiver = new JSONObject();
					msgCenterReceiver.put("userType", 2);
					msgCenterReceiver.put("userId", obj.getString("extId"));
					msgCenterReceiver.put("userName", obj.getString("name"));
					msgCenterReceiversArray.add(msgCenterReceiver);
				}
				disciplineService.sendKafka(oldOne, true, msgCenterReceiversArray);
				query.put("tchRecord", 1);
				disciplineService.updateRecordInfo(query);
			}
		} else {
			setPromptMessage(response, "-1", "更新发布状态失败");
		}
		return response;
	}

	@RequestMapping(value = "/deleteDiscipline", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteDiscipline(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		try {
			int result = disciplineService.deleteDiscipline(param);
			if (result > 0) {
				setPromptMessage(response, "1", "删除班纪班风成功");
			} else {
				setPromptMessage(response, "-1", "删除班纪班风失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "删除班纪班风异常");
		}
		return response;

	}

	@RequestMapping(value = "/getDiscipline", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDiscipline(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;

		String gradeId = request.getString("gradeId");
		String classId = request.getString("classId");
		String xnxq = getCurXnxq(req);
		String schoolId = getXxdm(req);
		if (StringUtils.isNotEmpty(gradeId)) {
			List<Long> classIds = new ArrayList<Long>();
			if (StringUtils.isNotEmpty(classId)) {
				classIds.add(Long.parseLong(classId));
				param.put("classIds", classIds);
			} else {
				School sch = commonDataService.getSchoolById(Long.parseLong(schoolId), xnxq);
				List<Grade> grades = commonDataService.getGradeList(sch, xnxq);
				for (Grade gd : grades) {
					if (gd.getId() == 0 || gd.isGraduate || gd.getCurrentLevel() == null || gd.getClassIds() == null
							|| gd.getClassIds().size() == 0) {
						continue;
					}
					String xn = xnxq.substring(0, 4);
					int njdm = gd.getCurrentLevel().getValue();
					String synj = commonDataService.ConvertNJDM2SYNJ(njdm + "", xn);
					if (gradeId.equals(synj)) {
						classIds = gd.getClassIds();
						param.put("classIds", classIds);
						break;
					}
				}
			}
		}

		JSONObject data = disciplineService.getDisciplines(param);
		List<Classroom> classroomList = commonDataService.getAllClass(getSchool(req, null), getCurXnxq(req));
		Classroom classroom = null;
		HashMap<String, String> classRoom = new HashMap<String, String>();
		for (int i = 0; i < classroomList.size(); i++) {
			classroom = classroomList.get(i);
			classRoom.put(classroom.getId() + "", classroom.getClassName());
		}

		if (!CollectionUtils.isEmpty(data)) {
			setPromptMessage(response, "1", "获取班纪班风成功");
			JSONArray array = data.getJSONArray("rows");
			for (int i = 0; i < array.size(); i++) {
				String tid = array.getJSONObject(i).getString("r0c0");
				array.getJSONObject(i).put("r0c0", classRoom.get(tid));
			}
			response.put("data", data);
		} else {
			setPromptMessage(response, "0", "没有查询到相关数据");
		}

		return response;
	}

	@RequestMapping(value = "/getAPPDiscipline", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPDiscipline(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String role = request.getString("role");
		String classId = request.getString("classId");
		Integer termInfo = request.getInteger("termInfo");
		String disciplineId = request.getString("disciplineId");
		List<Long> classIds = new ArrayList<Long>();
		if (StringUtils.isNotEmpty(classId)) {
			classIds.add(Long.parseLong(classId));
			param.put("classIds", classIds);
		}
		if (termInfo == null || disciplineId == null) {
			setPromptMessage(response, "-1", "参数不能为空");
			return response;
		}
		JSONObject dis  = disciplineService.getDiscipline(param);
		if(dis==null){
			setPromptMessage(response, "3", "没有查询到相关数据");
			return response;
		}
		String  publishFlag = dis.getString("publishFlag");
		String  publishTeacherFlag = dis.getString("publishTeacherFlag");
		 
		logger.info("getAPPDiscipline:role:"+role+" publishFlag:"+publishFlag+" publishTeacherFlag:"+publishTeacherFlag);
		if(("Parent".equals(role)||"Student".equals(role)) && !"1".equals(publishFlag) ){
			setPromptMessage(response, "4", "未发布");
			return response;
		}
		if("Teacher".equals(role) && !"1".equals(publishTeacherFlag) ){
			setPromptMessage(response, "4", "未发布");
			return response;
		}
		JSONObject data = disciplineService.getDisciplines(param);
		List<JSONObject> classResult = disciplineService.getAPPClassList(request);
		HashMap<String, String> classRoom = new HashMap<String, String>();
		for (int i = 0; i < classResult.size(); i++) {
			classRoom.put(classResult.get(i).getString("classId"), classResult.get(i).getString("className"));
		}
		if (!CollectionUtils.isEmpty(data)) {
			setPromptMessage(response, "1", "获取班纪班风成功");
			JSONArray array = data.getJSONArray("rows");
			for (int i = 0; i < array.size(); i++) {
				String tid = array.getJSONObject(i).getString("r0c0");
				array.getJSONObject(i).put("r0c0", classRoom.get(tid));
			}
			response.put("data", data);
		} else {
			setPromptMessage(response, "0", "没有查询到相关数据");
		}

		return response;
	}

	@RequestMapping(value = "/getPersonalDiscipline", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getPersonalDiscipline(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		String accountId = String.valueOf((Long) req.getSession().getAttribute("accountId"));
		Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId),
				termInfoId);
		JSONObject param = request;
		List<User> usersList = account.getUsers();
		List<Long> classIdList = null;
		if (usersList != null && usersList.size() > 0) {
			for (int i = 0; i < usersList.size(); i++) {
				User user = usersList.get(i);
				if (user.getUserPart().getRole() == T_Role.Teacher) {
					classIdList = user.getTeacherPart().getDeanOfClassIds();
					break;
				} else if (user.getUserPart().getRole() == T_Role.Parent) {
					classIdList = new ArrayList<Long>();
					Long classId = user.getParentPart().getClassId();
					classIdList.add(classId);
					break;
				}
			}
		}
		param.put("classIds", classIdList);
		String xnxq = request.getString("semesterCode");
		if (StringUtils.isEmpty(xnxq)) {
			xnxq = getCurXnxq(req);
		}
		param.put("xnxq", xnxq);

		JSONObject data = disciplineService.getPersonalDiscipline(param);
		List<Classroom> classroomList = commonDataService.getAllClass(getSchool(req, null), getCurXnxq(req));
		Classroom classroom = null;
		HashMap<String, String> classRoom = new HashMap<String, String>();
		for (int i = 0; i < classroomList.size(); i++) {
			classroom = classroomList.get(i);
			classRoom.put(classroom.getId() + "", classroom.getClassName());
		}

		if (!CollectionUtils.isEmpty(data)) {
			setPromptMessage(response, "1", "获取班纪班风成功");
			JSONArray array = data.getJSONArray("rows");
			for (int i = 0; i < array.size(); i++) {
				String tid = array.getJSONObject(i).getString("r0c0");
				array.getJSONObject(i).put("r0c0", classRoom.get(tid));
			}
			response.put("data", data);
		} else {
			setPromptMessage(response, "0", "没有查询到相关数据");
		}
		return response;

	}

	@RequestMapping(value = "/importDiscipline")
	@ResponseBody
	public JSONObject importDiscipline(@RequestParam("importFile") MultipartFile file, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		HttpSession session = req.getSession();
		String schoolId = getXxdm(req);
		DisciplineItems si = new DisciplineItems();
		si.setSessionId(session.getId());
		Object disciKey = "discipline." + session.getId() + ".disciplineItems";

		List<Classroom> classroomList = commonDataService.getAllClass(getSchool(req, null), getCurXnxq(req));

		String[] classTitleName = new String[classroomList.size()];
		String[] classTitleId = new String[classroomList.size()];
		Classroom classroom = null;
		for (int i = 0; i < classroomList.size(); i++) {
			classroom = classroomList.get(i);
			classTitleName[i] = classroom.getClassName();
			classTitleId[i] = String.valueOf(classroom.getId());
		}
		si.setClassTitleId(classTitleId);
		si.setClassTitleName(classTitleName);
		File df = null;
		try {
			si.setHeadRowNum(Integer.parseInt(req.getParameter("headRowNum")));
			String fileName = file.getOriginalFilename();
			String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + suffix;
			String tempName0 = UUID.randomUUID().toString() + "." + suffix;
			df = new File(tempName0);
			file.transferTo(df);
			String keyId = UUIDUtil.getUUID();
			Object tempFileIdKey = "discipline." + session.getId() + ".fileId";
			String fileId = fileServerImplFastDFS.uploadFile(df, tempName0);
			fileImportInfoService.addFile(schoolId, keyId, fileId);
			redisOperationDAO.set(tempFileIdKey, fileId);
			if (suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx")) {
				setPromptMessage(response, "0", "文件格式正确");
			} else {
				setPromptMessage(response, "-1", "文件不是Excel格式");
				return response;
			}
			Workbook workbook = WorkbookFactory.create(df);

			Sheet sheet = workbook.getSheetAt(0);
			int maxRow = sheet.getPhysicalNumberOfRows();
			Row row = null;
			int headRowNum = si.getHeadRowNum();
			if (maxRow > headRowNum) {
				row = sheet.getRow(headRowNum - 1);
				int cellNum = row.getLastCellNum();
				String[] headTitleName = new String[cellNum];
				for (int i = 0; i < cellNum; i++) {
					headTitleName[i] = getMergedRegionValue(sheet, headRowNum - 1, i);
				}
				si.setHeadTitleName(headTitleName);
				int index = indexInArray(SYSFIELD_CLASS, headTitleName);
				if (index == 0) {
					setPromptMessage(response, "0", "文件格式正确");
					si.setHeadNameIndex(index);
				} else {
					setPromptMessage(response, "-1", "第一列必须为班级");
				}
				if (cellNum <= 1) {
					setPromptMessage(response, "-2", "Excel表中的数据不符合要求！");
				}

			} else {
				setPromptMessage(response, "-2", "Excel没有数据！！");
			}

			for (int j = headRowNum; j < maxRow; j++) {
				if (sheet.getRow(j) != null) {
					for (int k = 0; k < sheet.getRow(j).getLastCellNum(); k++) {
						if (isMerged(sheet, j, k) || StringUtils.isEmpty(getCellValue(sheet.getRow(j).getCell(0)))) {
							setPromptMessage(response, "-100", "表头行输入错误或班级列有空值");
							break;
						}
					}
				}
			}
			workbook.close();
			redisOperationDAO.set(disciKey, si, CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
		} catch (Exception e) {
			setPromptMessage(response, "-3", "Excel出现未可知错误");
			e.printStackTrace();
		} finally {
			if (df != null)
				df.delete();
		}
		return response;
	}

	@RequestMapping(value = "/startImportTask")
	@ResponseBody
	public synchronized JSONObject startImportTask(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String disciplineId = request.getString("disciplineId");
		HttpSession session = req.getSession();
		String schoolId = getXxdm(req);
		JSONObject toFront = new JSONObject();
		JSONObject data = new JSONObject();
		int code = setClassNameMap(req);
		if (code < 0) {
			JSONObject obj = new JSONObject();
			obj.put("code", -50); // code=-50为redis读取异常
			obj.put("msg", "出现异常，导入失败！");
			return obj;
		}
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		JSONObject prepData = new JSONObject();
		prepDataMap.put(session.getId(), prepData);

		data.put("progress", 0);
		data.put("msg", "开始启动导入程序...");
		toFront.put("data", data);
		toFront.put("code", 0);

		Object progressKey = "discipline." + session.getId() + ".progress";
		Object prepDataMapKey = "discipline." + getXxdm(req) + session.getId() + ".prepDataMap";
		try {
			redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(prepDataMapKey, prepDataMap,
					CacheExpireTime.temporaryDataDefaultExpireTime.getTimeValue());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SubProcess sp = new SubProcess(schoolId, disciplineId, session.getId(), session);
		sp.start();
		logger.info("主线程结束！");
		setPromptMessage(response, "0", "正常启动任务");
		return response;
	}

	@RequestMapping(value = "/importProgress")
	@ResponseBody
	public JSONObject importProgress(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {
		String sessionId = req.getSession().getId();
		Object progressKey = "discipline." + sessionId + ".progress";
		JSONObject obj = null;
		JSONObject rs = new JSONObject();
		try {
			obj = (JSONObject) redisOperationDAO.get(progressKey);
			rs.put("code", obj.get("code"));
			rs.put("data", obj.get("data"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	@RequestMapping(value = "/singleDataCheck")
	@ResponseBody
	public JSONObject singleDataCheck(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {

		JSONObject response = new JSONObject();
		int row = request.getIntValue("row");
		int code = request.getIntValue("code");
		int errorIndex = -1;
		Object disciKey = "discipline." + req.getSession().getId() + ".disciplineItems";
		DisciplineItems si = null;
		try {
			si = (DisciplineItems) redisOperationDAO.get(disciKey);
			if (si == null) {
				setPromptMessage(response, "-50", "由于长时间未操作，请重新导入");
				return response;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<JSONObject> errorInfos = si.getErrorInfos();
		List<DisciplineDetail> successInfos = si.getSuccessInfos();
		logger.info("***********************singleDataCheckDiscimateerrorInfo********************:" + errorInfos);
		for (int i = 0; i < errorInfos.size(); i++) {
			if (errorInfos.get(i).getInteger("rowNum") == row) {
				errorIndex = i;
				break;
			}
		}
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		// 忽略错误信息
		if (code == -1) {
			errorInfos.remove(errorIndex);
			setPromptMessage(response, "1", "通过");
		} else {
			JSONArray j_rows = request.getJSONArray("mrows");
			Object prepDataMapKey = "discipline." + getXxdm(req) + req.getSession().getId() + ".prepDataMap";

			try {
				Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
				if (null != prepDataMapObj) {
					prepDataMap = (Hashtable<String, JSONObject>) prepDataMapObj;
				}
				if (null == prepDataMapObj) {
					setPromptMessage(response, "-50", "由于长时间未操作，请重新导入");
					return response;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			JSONObject preData = prepDataMap.get(req.getSession().getId());
			JSONObject rowdatas = preData.getJSONObject("rowdatas");
			JSONObject rowdata = rowdatas.getJSONObject(row + "");
			Map<String, String> semap = (Map<String, String>) rowdatas.get("semap");
			String value = rowdata.getString("xm");
			Map<String, String> colmap = (Map<String, String>) rowdata.get("colmap");
			List<JSONObject> lj = new ArrayList<JSONObject>();
			String title = "";
			for (int i = 0; i < j_rows.size(); i++) {
				JSONObject mrows = j_rows.getJSONObject(i);
				title = mrows.getString("title");
				String value2 = mrows.getString("value");
				if (title.equals("班级")) {
					value = value2;
				}

			}
			int position = indexInArray(value, si.getClassTitleName());
			if (position >= 0) {

				boolean flag2 = false;

				// 判断基础数据是否有多条记录
				HashMap<String, JSONObject> classMap = (HashMap<String, JSONObject>) req.getSession()
						.getAttribute("classMap");
				// 班级在基础数据中重复
				if (!value.isEmpty()) {
					JSONObject tObj = classMap.get(value);
					if (tObj != null) {
						int count = tObj.getIntValue("count");
						if (count > 1) {
							setPromptMessage(response, "-1", "校验数据失败");
							JSONObject result = new JSONObject();
							result.put("title", "班级");
							result.put("oldValue", value);
							result.put("err", "系统中存在多条该信息！");
							lj.add(result);
							flag2 = true;
							response.put("mrows", lj);
						}
					}
				}

				for (int i = 0; i < j_rows.size(); i++) {
					JSONObject mrows = j_rows.getJSONObject(i);
					if (mrows.containsKey("col")) {
						String value1 = mrows.getString("value");
						if (value1.length() > 250) {
							JSONObject result = new JSONObject();
							String projectName = "";
							if (semap.containsKey(mrows.getString("col"))) {
								projectName = semap.get(mrows.getString("col"));
							}
							result.put("title", projectName);
							result.put("oldValue", value1);
							result.put("err", "内容文字超过250字");
							result.put("col", mrows.getString("col"));
							lj.add(result);
							response.put("mrows", lj);
							flag2 = true;
						}
						colmap.put(mrows.getString("col"), value1);
					}
				}

				rowdata.put("xm", value);
				rowdata.put("colmap", colmap);
				if (flag2 == false) {
					setPromptMessage(response, "1", "校验通过");
					DisciplineDetail sd = (DisciplineDetail) errorInfos.get(errorIndex).get("disciplineDetail");
					sd.setClassId(si.getClassTitleId()[position]);
					Iterator<Entry<String, String>> it1 = colmap.entrySet().iterator();
					while (it1.hasNext()) {
						Entry<String, String> entry = it1.next();
						sd.getDisciComponentIds().add(entry.getKey());
						sd.getScores().add(entry.getValue());
					}
					successInfos.add(sd);
					errorInfos.remove(errorIndex);
				}

			} else {
				setPromptMessage(response, "-1", "校验数据失败");
				JSONObject result = new JSONObject();
				result.put("title", "班级");
				result.put("oldValue", value);
				result.put("err", "无匹配记录！");
				lj.add(result);
				response.put("mrows", lj);

				for (int i = 0; i < j_rows.size(); i++) {
					JSONObject mrows = j_rows.getJSONObject(i);
					if (mrows.containsKey("col")) {
						String value1 = mrows.getString("value");
						if (value1.length() > 250) {
							JSONObject result1 = new JSONObject();
							String projectName = "";
							if (semap.containsKey(mrows.getString("col"))) {
								projectName = semap.get(mrows.getString("col"));
							}
							result1.put("title", projectName);
							result1.put("oldValue", value1);
							result1.put("err", "内容文字超过250字");
							result1.put("col", mrows.getString("col"));
							lj.add(result1);
							response.put("mrows", lj);
						}
						colmap.put(mrows.getString("col"), value1);
					}
				}
				rowdata.put("xm", value);
				rowdata.put("colmap", colmap);
			}
			try {
				redisOperationDAO.set(disciKey, si, CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
				redisOperationDAO.set(prepDataMapKey, prepDataMap,
						CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return response;
	}

	@RequestMapping(value = "/continueImport")
	@ResponseBody
	public JSONObject continueImport(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		int code = -1;
		String msg = null;
		Object disciKey = "discipline." + req.getSession().getId() + ".disciplineItems";
		DisciplineItems si = null;
		try {
			si = (DisciplineItems) redisOperationDAO.get(disciKey);
			if (si == null) {
				setPromptMessage(response, "-50", "由于长时间未操作，请重新导入");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<DisciplineDetail> successInfos = si.getSuccessInfos();
		if (successInfos.size() > 0) {
			int result = disciplineService.addImportDiscipline(successInfos, si.getL_disciExcel());
			if (result > 0) {
				code = 1;
				msg = "恭喜，导入班纪班风表结束，共导入" + (successInfos.size()) + "条班纪班风！";
			} else {
				msg = "启动进程异常！";
			}
		} else {
			code = 1;
			msg = "恭喜，导入教学考评表结束，共导入0条班纪班风！";
		}
		response.put("code", code);
		response.put("msg", msg);
		return response;
	}

	@RequestMapping(value = "/exportWrongMsg")
	@ResponseBody
	public void exportWrongMsg(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {

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

	@RequestMapping(value = "/getGradeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGradeList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String schoolId = getXxdm(req);
		String xnxq = getCurXnxq(req);
		param.put("schoolId", schoolId);
		School sch = commonDataService.getSchoolById(Long.parseLong(schoolId), xnxq);
		List<Grade> grades = commonDataService.getGradeList(sch, xnxq);
		HashMap<String, JSONObject> njList = new HashMap<String, JSONObject>();
		List<JSONObject> rs = new ArrayList<JSONObject>();
		if (grades != null) {
			Grade gd = null;
			Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
			for (int i = 0; i < grades.size(); i++) {
				gd = grades.get(i);

				if (gd.getId() == 0 || gd.isGraduate || gd.getCurrentLevel() == null || gd.getClassIds() == null
						|| gd.getClassIds().size() == 0) {
					continue;
				}
				String xn = xnxq.substring(0, 4);
				int njdm = gd.getCurrentLevel().getValue();
				String synj = commonDataService.ConvertNJDM2SYNJ(njdm + "", xn);

				String gradeName = "[" + commonDataService.ConvertSYNJ2RXND(synj, xn) + "]"
						+ njName.get(gd.getCurrentLevel());

				JSONObject go = new JSONObject();
				go.put("gradeName", gradeName);
				go.put("synj", synj);
				go.put("gradeLevel", gd.getCurrentLevel());
				njList.put(synj, go);

				JSONObject j = new JSONObject();
				j.put("value", synj);
				j.put("text", gradeName);
				rs.add(j);
			}

			Collections.sort(rs, comparetor);
			Collections.reverse(rs);

		}

		setPromptMessage(response, "1", "查询成功");
		response.put("data", rs);
		return response;
	}

	@RequestMapping(value = "/getClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String synj = request.getString("gradeId");
		String xnxq = getCurXnxq(req);

		School sch = commonDataService.getSchoolById(Long.parseLong(schoolId), xnxq);
		List<Grade> grades = commonDataService.getGradeBatch(sch.getId(), sch.getGrades(), xnxq);
		List<Long> classList = new ArrayList<Long>();
		List<JSONObject> rs = new ArrayList<JSONObject>();

		for (Grade gd : grades) {
			if (gd != null && !gd.isGraduate && gd.getClassIds() != null && gd.getClassIds().size() > 0
					&& gd.getCurrentLevel() != null) {
				String gs = commonDataService.ConvertNJDM2SYNJ(gd.getCurrentLevel().getValue() + "",
						xnxq.substring(0, 4));
				if (synj.indexOf(gs) != -1) {
					classList.addAll(gd.getClassIds());
				}
			}
		}

		if (classList.size() > 0) {
			List<Classroom> classRoomList = commonDataService.getClassroomBatch(Long.parseLong(schoolId), classList,
					xnxq);
			if (classRoomList != null) {
				Classroom classroom = null;
				for (int i = 0; i < classRoomList.size(); i++) {
					classroom = classRoomList.get(i);
					JSONObject j = new JSONObject();
					j.put("value", classroom.getId());
					j.put("text", classroom.getClassName());
					rs.add(j);
				}
			}
		}
		setPromptMessage(response, "1", "查询成功");
		response.put("data", rs);
		return response;
	}

	@RequestMapping(value = "/getAPPClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPClassList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String disciplineId = request.getString("disciplineId");
		Integer termInfo = request.getInteger("termInfo");
		if (termInfo == null || disciplineId == null) {
			setPromptMessage(response, "-1", "参数不能为空");
			return response;
		}
		List<JSONObject> result = disciplineService.getAPPClassList(request);
		setPromptMessage(response, "1", "查询成功");
		response.put("data", result);
		return response;
	}

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

	private int indexInArray(String string, String[] arr) {
		String source = string.replace(" ", "");
		int rs = -1;
		for (int i = 0; i < arr.length; i++) {
			String target = (arr[i] == null ? "" : arr[i]);
			target = target.replace(" ", "");
			if (target.equalsIgnoreCase(source)) {
				rs = i;
				break;
			}
		}
		return rs;
	}

	public String getCellValue(Cell cell) {
		if (cell == null)
			return "";
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			String str = null;
			String temStr = cell.getRichStringCellValue().getString().trim();
			if (isNumber(temStr)) {
				double d = Double.valueOf(temStr);
				if (d % 1 > 0) {
					BigDecimal b = new BigDecimal(d);
					str = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
				} else {
					str = temStr;
				}
			} else {
				str = temStr;
			}
			return str;
		case Cell.CELL_TYPE_BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case Cell.CELL_TYPE_FORMULA:
			String temp = null;
			try {
				temp = String.valueOf(cell.getStringCellValue());
			} catch (IllegalStateException e) {
				double d = cell.getNumericCellValue();
				temp = String.valueOf(d);
				if (temp.endsWith("0") && temp.indexOf(".") > -1) {
					if (temp.endsWith(".0")) {
						temp = temp.substring(0, temp.length() - 2);
					} else {
						temp = temp.substring(0, temp.length() - 1);
					}
				}
			}
			return temp;
		case Cell.CELL_TYPE_NUMERIC:
			double d = cell.getNumericCellValue();
			String s = String.valueOf(d);
			if (s.endsWith("0") && s.indexOf(".") > -1) {
				if (s.endsWith(".0")) {
					s = s.substring(0, s.length() - 2);
				} else {
					s = s.substring(0, s.length() - 1);
				}
			}
			return s;
		}
		return "";
	}

	public boolean isNumber(String str) {
		Pattern patter = Pattern.compile("^[-\\+]?[0-9]+(\\.[0-9]+)?$");
		return patter.matcher(str).matches();
	}

	public boolean isMerged(Sheet sheet, int row, int col) {
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

	private int setClassNameMap(HttpServletRequest req) {
		int code = 1;
		try {
			School school = this.getSchool(req, null);
			code = 1;
			// 获取班级列表
			List<Classroom> classroomList = commonDataService.getAllClass(school, getCurXnxq(req));

			String[] className = new String[classroomList.size()];
			HashMap<String, JSONObject> classMap = new HashMap<String, JSONObject>();
			for (int i = 0; i < classroomList.size(); i++) {
				Classroom classroom = classroomList.get(i);
				String key = classroom.getClassName();
				className[i] = key;
				JSONObject obj = new JSONObject();
				obj.put("classId", classroom.getId());
				obj.put("className", classroom.getClassName());
				if (classMap.containsKey(key)) {
					JSONObject obj1 = classMap.get(key);
					int count = obj1.getIntValue("count");
					count++;
					obj1.put("count", count);

				} else {
					obj.put("count", 1);
					classMap.put(key, obj);
				}
			}

			req.getSession().setAttribute("className", className);
			req.getSession().setAttribute("classMap", classMap);
		} catch (Exception e) {
			code = -50;
			e.printStackTrace();
		}

		return code;
	}

	class SubProcess extends Thread {
		private String schoolId;
		private String disciplineId;
		private String sessionId;
		private HttpSession ses;

		public SubProcess(String schoolId, String disciplineId, String sessionId, HttpSession ses) {
			this.schoolId = schoolId;
			this.disciplineId = disciplineId;
			this.sessionId = sessionId;
			this.ses = ses;
		}

		@Override
		public void run() {
			super.run();
			long t1 = (new Date()).getTime();

			JSONObject toFront = new JSONObject();
			JSONObject data = new JSONObject();
			Object disciKey = "discipline." + sessionId + ".disciplineItems";

			Object prepDataMapKey = "discipline." + schoolId + sessionId + ".prepDataMap";
			Object progressKey = "discipline." + sessionId + ".progress";

			File temfile = null;
			Workbook workbook = null;
			DisciplineItems si = null;
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			try {
				Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
				if (null != prepDataMapObj) {
					prepDataMap = (Hashtable<String, JSONObject>) prepDataMapObj;
				}
				si = (DisciplineItems) redisOperationDAO.get(disciKey);
				if (si == null || null == prepDataMapObj) {
					toFront.put("code", -100);
					data.put("progress", 100);
					data.put("msg", "由于长时间未操作，请重新导入");
					toFront.put("data", data);
					try {
						redisOperationDAO.set(progressKey, toFront,
								CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
				Object tempFileIdKey = "discipline." + sessionId + ".fileId";

				String fileID = (String) redisOperationDAO.get(tempFileIdKey);
				String localFilenName = UUID.randomUUID().toString();
				fileServerImplFastDFS.downloadFile(fileID, localFilenName);
				// 在删除表中的记录
				fileImportInfoService.deleteFileByFileId(fileID);
				fileServerImplFastDFS.deleteFile(fileID);
				temfile = new File(localFilenName);
				workbook = WorkbookFactory.create(temfile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Sheet sheet = workbook.getSheetAt(0);
			int maxRow = sheet.getPhysicalNumberOfRows();
			// 读取Excel中的数据
			data.put("progress", 5);
			data.put("msg", "正在读取校验excel数据");
			toFront.put("data", data);
			toFront.put("code", 1);

			try {
				redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
			HashMap<String, JSONObject> classMap = (HashMap<String, JSONObject>) ses.getAttribute("classMap");
			si.setL_disciExcel(getDisciExcels(sheet, si.getHeadRowNum(), disciplineId, schoolId));

			String[] excelClasses = new String[maxRow - si.getHeadRowNum()];

			Map<String, String> semap = new HashMap<String, String>();
			for (DisciplineExcel se : si.getL_disciExcel()) {
				semap.put(se.getEn_disci(), se.getZh_disci());
			}
			List<DisciplineDetail> successInfos = new ArrayList<DisciplineDetail>();
			List<JSONObject> errorInfos = new ArrayList<JSONObject>();
			for (int i = si.getHeadRowNum(); i < maxRow; i++) {
				String className = getCellValue(sheet.getRow(i).getCell(si.getHeadNameIndex()));
				if (isDigits(className)) {
					excelClasses[i - si.getHeadRowNum()] = (int) Double.parseDouble(className) + "";
				} else {
					excelClasses[i - si.getHeadRowNum()] = className;
				}
			}
			JSONObject preData = prepDataMap.get(sessionId);
			JSONObject rowdata = new JSONObject();
			// 判断ecxel表中的重复数据
			for (int i = 0; i < excelClasses.length; i++) {
				DisciplineDetail sd = null;
				boolean flag2 = false;
				String[] excelClassTemp = excelClasses.clone();
				excelClassTemp[i] = "nul";
				int result = indexInArray(excelClasses[i], si.getClassTitleName());
				// 教师姓名在基础数据中重复
				JSONObject tObj = classMap.get(excelClasses[i]);
				Map<String, String> colmap = new HashMap<String, String>();
				JSONObject rowd = new JSONObject();
				rowd.put("xm", excelClasses[i]);
				rowd.put("colmap", colmap);
				rowdata.put("semap", semap);
				rowdata.put(i + si.getHeadRowNum() + 1 + "", rowd);
				if (tObj != null) {
					int count = tObj.getIntValue("count");
					if (count > 1) {
						flag2 = true;
						JSONObject object = new JSONObject();
						object.put("rowNum", i + si.getHeadRowNum() + 1);
						object.put("className", excelClasses[i]);
						object.put("err", "系统中存在多条该信息！");
						String class_title = getMergedRegionValue(sheet, si.getHeadRowNum() - 1, si.getHeadNameIndex());
						object.put("class_title", class_title);
						try {
							sd = getRowesDisciDetail(sheet, si.getHeadRowNum() + i, si.getHeadNameIndex(),
									si.getHeadRowNum());
						} catch (IllegalStateException e) {
							object.put("dataErr", e.getMessage());
							errorInfos.add(object);
						}
						sd.setDisciplineId(disciplineId);
						sd.setSchoolId(schoolId);
						object.put("disciplineDetail", sd);
						errorInfos.add(object);

						if (!sd.getErrorlist().isEmpty()) {
							for (JSONObject s : sd.getErrorlist()) {
								JSONObject object1 = new JSONObject();
								String projectName = "";
								if (semap.containsKey(s.getString("projectId"))) {
									projectName = semap.get(s.getString("projectId"));
								}
								if (s.getString("value").length() > 250) {
									object1.put("rowNum", i + si.getHeadRowNum() + 1);
									object1.put("className", s.getString("value"));
									object1.put("err", "内容文字超过250字");
									object1.put("class_title", projectName);
									object1.put("col", s.getString("projectId"));
									sd.setSchoolId(schoolId);
									object1.put("disciplineDetail", sd);
									errorInfos.add(object1);
								} else {
									object1.put("rowNum", i + si.getHeadRowNum() + 1);
									object1.put("className", s.getString("value"));
									object1.put("err", "内容文字有异常");
									object1.put("class_title", projectName);
									object1.put("col", s.getString("projectId"));
									sd.setSchoolId(schoolId);
									object1.put("disciplineDetail", sd);
									errorInfos.add(object1);
								}

								colmap.put(s.getString("projectId"), s.getString("value"));
							}
						}
					}
				}

				if (flag2 == false && result >= 0) {
					boolean flag = true;
					try {
						sd = getRowesDisciDetail(sheet, si.getHeadRowNum() + i, si.getHeadNameIndex(),
								si.getHeadRowNum());

						if (!sd.getErrorlist().isEmpty()) {
							for (JSONObject s : sd.getErrorlist()) {
								JSONObject object1 = new JSONObject();
								String projectName = "";
								if (semap.containsKey(s.getString("projectId"))) {
									projectName = semap.get(s.getString("projectId"));
								}
								if (s.getString("value").length() > 250) {
									object1.put("rowNum", i + si.getHeadRowNum() + 1);
									object1.put("className", s.getString("value"));
									object1.put("err", "内容文字超过250字");
									object1.put("class_title", projectName);
									object1.put("col", s.getString("projectId"));
									sd.setSchoolId(schoolId);
									object1.put("disciplineDetail", sd);
									errorInfos.add(object1);
								} else {
									object1.put("rowNum", i + si.getHeadRowNum() + 1);
									object1.put("className", s.getString("value"));
									object1.put("err", "内容文字有异常");
									object1.put("class_title", projectName);
									object1.put("col", s.getString("projectId"));
									sd.setSchoolId(schoolId);
									object1.put("disciplineDetail", sd);
									errorInfos.add(object1);
								}

								colmap.put(s.getString("projectId"), s.getString("value"));
							}

							flag = false;
						}
					} catch (IllegalStateException e) {
						JSONObject object = new JSONObject();
						object.put("rowNum", i + si.getHeadRowNum() + 1);
						object.put("className", excelClasses[i]);
						String class_title = getMergedRegionValue(sheet, si.getHeadRowNum() - 1, si.getHeadNameIndex());
						object.put("class_title", class_title);
						object.put("dataErr", e.getMessage());
						errorInfos.add(object);
					}
					sd.setClassId(si.getClassTitleId()[result]);
					sd.setDisciplineId(disciplineId);
					sd.setSchoolId(schoolId);
					if (flag) {
						successInfos.add(sd);
					}

				} else if (flag2 == false && result < 0) {
					JSONObject object = new JSONObject();
					object.put("rowNum", i + si.getHeadRowNum() + 1);
					object.put("className", excelClasses[i]);
					String class_title = getMergedRegionValue(sheet, si.getHeadRowNum() - 1, si.getHeadNameIndex());
					object.put("class_title", class_title);
					object.put("err", "无匹配记录！");
					try {
						sd = getRowesDisciDetail(sheet, si.getHeadRowNum() + i, si.getHeadNameIndex(),
								si.getHeadRowNum());
					} catch (IllegalStateException e) {
						object.put("dataErr", e.getMessage());
						errorInfos.add(object);
					}
					sd.setDisciplineId(disciplineId);
					sd.setSchoolId(schoolId);

					object.put("disciplineDetail", sd);
					errorInfos.add(object);

					if (!sd.getErrorlist().isEmpty()) {
						for (JSONObject s : sd.getErrorlist()) {
							JSONObject object1 = new JSONObject();
							String projectName = "";
							if (semap.containsKey(s.getString("projectId"))) {
								projectName = semap.get(s.getString("projectId"));
							}
							if (s.getString("value").length() > 250) {
								object1.put("rowNum", i + si.getHeadRowNum() + 1);
								object1.put("className", s.getString("value"));
								object1.put("err", "内容文字超过250字");
								object1.put("class_title", projectName);
								object1.put("col", s.getString("projectId"));
								sd.setSchoolId(schoolId);
								object1.put("disciplineDetail", sd);
								errorInfos.add(object1);
							} else {
								object1.put("rowNum", i + si.getHeadRowNum() + 1);
								object1.put("className", s.getString("value"));
								object1.put("err", "内容文字有异常");
								object1.put("class_title", projectName);
								object1.put("col", s.getString("projectId"));
								sd.setSchoolId(schoolId);
								object1.put("disciplineDetail", sd);
								errorInfos.add(object1);
							}

							colmap.put(s.getString("projectId"), s.getString("value"));
						}
					}
				}
			}
			preData.put("rowdatas", rowdata);
			if (errorInfos.size() > 0) {
				toFront.put("code", -2);
				data.put("progress", 100);
				JSONObject res = getValidateMsg(errorInfos);
				List<JSONObject> siz = (List<JSONObject>) res.get("rows");
				int s = siz != null ? siz.size() : 0;
				data.put("msg",
						"共" + (s + successInfos.size()) + "条记录：" + successInfos.size() + "条导入成功," + s + "条导入失败");
				data.put("total", successInfos.size());
				data.put("validateMsg", res);
				toFront.put("data", data);
				try {
					redisOperationDAO.set(progressKey, toFront,
							CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				if (successInfos.size() > 0) {
					try {
						// 保存数据
						toFront.put("code", 1);
						data.put("msg", "正在保存数据！");
						data.put("progress", 30);
						toFront.put("data", data);
						try {
							redisOperationDAO.set(progressKey, toFront,
									CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
						disciplineService.addImportDiscipline(successInfos, si.getL_disciExcel());
						toFront.put("code", 2);
						data.put("progress", 100);
						data.put("msg", "恭喜，导入班纪班风表结束，共导入" + (successInfos.size()) + "条班纪班风！");
						toFront.put("data", data);
						try {
							redisOperationDAO.set(progressKey, toFront,
									CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						toFront.put("code", -1);
						data.put("progress", 100);
						data.put("msg", "服务器出错,导入失败!");
						try {
							redisOperationDAO.set(progressKey, toFront,
									CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
						} catch (Exception ex) {
							e.printStackTrace();
						}
						e.printStackTrace();
					}
				} else {
					toFront.put("code", -2);
					data.put("progress", 100);
					data.put("msg", "Excel数据校验不通过!");
					data.put("total", successInfos.size());
					data.put("validateMsg", getValidateMsg(errorInfos));
					toFront.put("data", data);
					try {
						redisOperationDAO.set(progressKey, toFront,
								CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			si.setSuccessInfos(successInfos);
			si.setErrorInfos(errorInfos);
			si.setExcelclasses(excelClasses);
			try {
				redisOperationDAO.set(disciKey, si, CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
				redisOperationDAO.set(prepDataMapKey, prepDataMap,
						CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// excel导入处理结束
			long t2 = (new Date()).getTime();
			logger.info("导入子线程结束,耗时：" + (t2 - t1));
			logger.info("开始删除临时excel");
			temfile.delete();
		}

	}

	public List<DisciplineExcel> getDisciExcels(Sheet sheet, int rowNum, String discimateId, String schoolId) {
		List<DisciplineExcel> l_disciExcels = new ArrayList<DisciplineExcel>();
		Row row = sheet.getRow(0);
		for (int k = 0; k < rowNum; k++) {
			for (int i = 0; i < row.getLastCellNum();) {
				DisciplineExcel se = new DisciplineExcel();
				se.setDisciplineId(discimateId);
				se.setSchoolId(schoolId);
				se.setHead_rowNum(rowNum);
				String rowId = getRowId(sheet, k, i);
				int r_index = rowId.indexOf("r");
				int c_index = rowId.indexOf("c");
				int curRow = Integer.parseInt(rowId.substring(r_index + 1, c_index));
				int curCol = Integer.parseInt(rowId.substring(c_index + 1));
				if (isMerged(sheet, k, i)) {
					int coldiff = getColdiff(sheet, k, i);
					int rowdiff = getRowdiff(sheet, k, i);
					se.setColspan(coldiff + 1);
					se.setEn_disci(rowId);
					se.setZh_disci(getMergedRegionValue(sheet, k, i));
					se.setIn_rowNum(curRow);
					se.setIn_colNum(curCol);
					if (k == 0) {
						se.setPen_disci(null);
					} else {
						if (rowdiff == 0) {
							se.setPen_disci(getRowId(sheet, curRow - 1, i));
						} else {
							if (curRow == 0) {
								se.setPen_disci(null);
							} else {
								se.setPen_disci(getRowId(sheet, curRow - 1, i));
							}
						}
					}
					se.setRowspan(rowdiff + 1);
					i = i + coldiff + 1;
				} else {
					se.setColspan(1);
					se.setRowspan(1);
					se.setZh_disci(getCellValue(sheet.getRow(k).getCell(i)));
					se.setEn_disci(rowId);
					se.setIn_rowNum(curRow);
					se.setIn_colNum(curCol);
					if (curRow == 0) {
						se.setPen_disci(null);
					} else {
						se.setPen_disci(getRowId(sheet, k - 1, i));
					}
					i++;
				}
				l_disciExcels.add(se);
			}
		}
		// 去掉重复元素
		Set<DisciplineExcel> ss = new HashSet<DisciplineExcel>(l_disciExcels);
		return new ArrayList<DisciplineExcel>(ss);
	}

	public String getRowId(Sheet sheet, int row, int col) {
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

	public JSONObject getValidateMsg(List<JSONObject> errorInfos) {
		JSONObject result = new JSONObject();
		List<JSONObject> l_object = new ArrayList<JSONObject>();
		Map<String, List<JSONObject>> mrowmap = new LinkedHashMap<String, List<JSONObject>>();
		if (errorInfos.size() > 0) {
			result.put("total", errorInfos.size());
			for (int i = 0; i < errorInfos.size(); i++) {
				int row = errorInfos.get(i).getInteger("rowNum");
				if (mrowmap.containsKey(row + "")) {
					List<JSONObject> mrows = mrowmap.get(row + "");
					JSONObject detail = new JSONObject();
					detail.put("title", errorInfos.get(i).getString("class_title"));
					detail.put("oldValue", errorInfos.get(i).getString("className"));
					detail.put("err", errorInfos.get(i).getString("err"));
					if (errorInfos.get(i).containsKey("col")) {
						detail.put("col", errorInfos.get(i).getString("col"));
					}
					mrows.add(detail);
					mrowmap.put(row + "", mrows);
				} else {
					List<JSONObject> mrows = new ArrayList<JSONObject>();
					JSONObject detail = new JSONObject();
					detail.put("title", errorInfos.get(i).getString("class_title"));
					detail.put("oldValue", errorInfos.get(i).getString("className"));
					detail.put("err", errorInfos.get(i).getString("err"));
					if (errorInfos.get(i).containsKey("col")) {
						detail.put("col", errorInfos.get(i).getString("col"));
					}
					mrows.add(detail);
					mrowmap.put(row + "", mrows);
				}

			}
			Iterator<Entry<String, List<JSONObject>>> it = mrowmap.entrySet().iterator();

			while (it.hasNext()) {
				JSONObject rowInfo = new JSONObject();
				Entry<String, List<JSONObject>> entry = it.next();
				String row = entry.getKey();
				List<JSONObject> mrows = entry.getValue();
				rowInfo.put("row", row);
				rowInfo.put("mrows", mrows);
				l_object.add(rowInfo);
			}
			result.put("rows", l_object);
		}
		return result;
	}

	public boolean isDigits(String str) {
		return str.matches("^[-\\+]?\\d+(\\.\\d+)?$");
	}

	public DisciplineDetail getRowesDisciDetail(Sheet sheet, int row, int titleIndex, int headRowNumber) {
		DisciplineDetail sd = new DisciplineDetail();
		List<String> disciComponentIds = new ArrayList<String>();
		List<String> scores = new ArrayList<String>();
		List<JSONObject> errorlist = new ArrayList<JSONObject>();
		StringBuffer errInfo = new StringBuffer();
		for (int j = 0; j < sheet.getRow(row).getLastCellNum(); j++) {
			try {
				if (j == titleIndex)
					continue;
				String score = getCellValue(sheet.getRow(row).getCell(j));
				if (score.length() > 250) {
					JSONObject s = new JSONObject();
					s.put("num", toNumberSystem26(j));
					s.put("projectId", getRowId(sheet, headRowNumber - 1, j));
					s.put("value", score);
					errorlist.add(s);
				} else {
					disciComponentIds.add(getRowId(sheet, headRowNumber - 1, j));
					scores.add(score);
				}
			} catch (IllegalStateException e) {
				JSONObject s = new JSONObject();
				s.put("num", toNumberSystem26(j));
				s.put("projectId", getRowId(sheet, headRowNumber - 1, j));
				s.put("value", "");
				errorlist.add(s);
				errInfo.append("第").append(toNumberSystem26(j)).append("列、");
			}
		}
		if (errInfo.length() > 0) {
			errInfo.deleteCharAt(errInfo.length() - 1);
			// throw new IllegalStateException(errInfo.toString());
		}
		sd.setErrorlist(errorlist);
		sd.setDisciComponentIds(disciComponentIds);
		sd.setScores(scores);
		sd.setRowNum(row);
		return sd;
	}

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

}

