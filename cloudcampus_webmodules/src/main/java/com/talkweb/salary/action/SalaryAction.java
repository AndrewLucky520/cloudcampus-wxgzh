package com.talkweb.salary.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.springframework.beans.factory.annotation.Value;
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
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.auth.service.AuthService;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.salary.domain.SalDetail;
import com.talkweb.salary.domain.SalExcel;
import com.talkweb.salary.domain.SalItems;
import com.talkweb.salary.service.SalaryService;
import com.talkweb.utils.KafkaWXmsgThread;

@Controller
@RequestMapping("/salaryManage")
public class SalaryAction extends BaseAction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Logger logger = LoggerFactory.getLogger(SalaryAction.class);
	
	@Autowired
	private SalaryService salaryService;
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	@Autowired
	private FileImportInfoService fileImportInfoService;
	@Autowired
	private FileServer fileServerImplFastDFS;
    private final String SYSFIELD_TEACHER = "教师姓名";
    
	@Autowired
	private AuthService authServiceImpl;
	
	@Value("#{settings['clientId']}")
	private String clientId;

	@Value("#{settings['clientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
	
	
	@Value("#{settings['salary.msgUrlPc']}")
	private String msgUrlPc;
	
	@Value("#{settings['salary.msgUrlApp']}")
	private String msgUrlApp;
	
    
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	/**
	 * 获取登录人员是否是工资管理员
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/lookup/getRole",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRole(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		boolean role = isMoudleManager(req, "cs1011");
		if(role){
			response.put("role", 1);
		}else{
			response.put("role", 0);
		}
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		JSONObject object = salaryService.getCjSchool(param);
		if (object!=null) {
			response.put("cj", 1);
		}else {
			response.put("cj", 0);
		}
	 
		
		setPromptMessage(response, "0", "");
		return response;
	}
	/**
	 * 获取工资单列表信息
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/lookup/getSalaryList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSalaryList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String year = request.getString("year");
		JSONObject param = new JSONObject();
		param.put("year", year);
		param.put("schoolId", getXxdm(req));
		List<JSONObject> data = salaryService.getSalaryList(param);
		if(!CollectionUtils.isEmpty(data)){
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		}else{
			setPromptMessage(response, "0", "没有查询到相关数据");
		}
		return response;
	}
	/**
	 * 新增一条工资单
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/lookup/addSalary",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addSalary(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String year = request.getString("year");
		String month = request.getString("month");
		String salaryName = request.getString("salaryName");
		if(!StringUtils.isEmpty(year)&&!StringUtils.isEmpty(month)&&!StringUtils.isEmpty(salaryName)){
			String salaryId = UUID.randomUUID().toString();
			param.put("year", year);
			param.put("month", month);
			param.put("salaryId", salaryId);
			param.put("salaryName", salaryName);
			param.put("schoolId", getXxdm(req));
			param.put("createDate", new Date());
			param.put("isImported", 0);
			param.put("isPublished", 0);
			
			int result = salaryService.addSalary(param);
			if(result > 0){
				setPromptMessage(response, "0", "创建工资成功");
			}else{
				setPromptMessage(response, "1", "创建工资异常");
			}
		}else{
			setPromptMessage(response, "1", "工资名称不能为空");
		}
		return response;
	}
	/**
	 * 更新一条工资单信息
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/lookup/updateSalary",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateSalary(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String salaryId = request.getString("salaryId");
		String salaryName = request.getString("salaryName");
		String month = request.getString("month");
		if(!StringUtils.isEmpty(salaryId)&&!StringUtils.isEmpty(salaryName)&&!StringUtils.isEmpty(salaryName)){
			param.put("salaryId", salaryId);
			param.put("salaryName", salaryName);
			param.put("month", month);
			int result = salaryService.updateSalary(param);
			if(result > 0){
				setPromptMessage(response, "0", "更新工资成功");
			}else{
				setPromptMessage(response, "1", "更新工资失败");
			}
		}else{
			setPromptMessage(response, "1", "更新工资失败");
		}
		return response;
	}
	/**
	 * 更新工资单的发布状态
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/lookup/updateSalaryPublished",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateSalaryPublished(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String salaryId = request.getString("salaryId");
		Integer isPublished = request.getIntValue("isPublished");
		if(!StringUtils.isEmpty(salaryId)&&isPublished!=null){
			param.put("salaryId", salaryId);
			param.put("isPublished", isPublished);
			int result = salaryService.updateSalaryPublished(param);
			if(result > 0){
				setPromptMessage(response, "0", "更新发布状态成功");
				if (isPublished == 1) {
					String schoolId =  getXxdm(req);
					String termInfo = getCurXnxq(req);
					param.put("schoolId", schoolId);
					List<JSONObject> list = salaryService.getTeacherBySalaryId(param);
					Long accountId = (Long)req.getSession().getAttribute("accountId");
					Set<Long>  accountIds =  new  HashSet<Long>();
					accountIds.add(accountId);
					List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId),new ArrayList<>(accountIds) , termInfo);
					Account account =accounts.get(0);
					String creatorName = account.getName();
					String title = "";
					accountIds.clear();
					String year = null;
					String month = null;
					for (int i = 0; i < list.size(); i++) {
						JSONObject object = list.get(i);
						accountIds.add(object.getLong("TeacherId"));
						title = object.getString("SalaryName");
						year = object.getString("Year");
						month = object.getString("Month");
					}
					accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), new ArrayList<>(accountIds), termInfo);
					School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
					List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
					for (int i = 0; i < accounts.size(); i++) {
						account = accounts.get(i);
						JSONObject msgCenterReceiver = new JSONObject();
						msgCenterReceiver.put("userId", account.getExtId());
						msgCenterReceiver.put("userName", account.getName());
						

						// 针对每个老师进行显示
						JSONObject values = new JSONObject();
						values.put("keyword1", account.getName());
						msgCenterReceiver.put("keyValues", values);

						msgCenterReceiversArray.add(msgCenterReceiver);
					}
 
						JSONObject msgCenterPayLoad = new JSONObject();
						JSONObject msg = new JSONObject();
					    String msgId = UUIDUtil.getUUID().replace("-", "");
						msg.put("msgId", msgId);
						msg.put("msgTitle", "您收到工资提醒！");
						msg.put("msgContent", "您收到工资提醒!");
						msg.put("msgUrlPc",  msgUrlPc);
						msg.put("msgUrlApp", msgUrlApp +"?month="+month+"&year=" + year);
						msg.put("msgOrigin", "工资提醒");
						msg.put("msgTypeCode", "GZT");
						msg.put("schoolId", school.getExtId());
						msg.put("creatorName", creatorName);

						JSONObject first = new JSONObject();
						first.put("value", "你好，本月工资条已下发");

						JSONObject keyword1 = new JSONObject();
						keyword1.put("value", "" );
						
						JSONObject keyword2 = new JSONObject();
						keyword2.put("value", title);
 
						JSONObject remark = new JSONObject();
						remark.put("value", "请进入系统核实工资详情，如有异议请与学校财务部门联系。");
						
						JSONObject data = new JSONObject();
						data.put("first", first);
						data.put("keyword1", keyword1);
						data.put("keyword2", keyword2);
						data.put("remark", remark);
						data.put("url",  msgUrlApp +"?month="+month+"&year=" + year);
						msg.put("msgWxJson", data);
					
						msgCenterPayLoad.put("msg", msg);
						msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
						logger.info("msgCenterPayLoad=====>" + msgCenterPayLoad.toString());
						KafkaWXmsgThread thread = new KafkaWXmsgThread(kafkaUrl, msgId, msgCenterPayLoad, "GZT", clientId, clientSecret);
						thread.start();
					}
 
			}else{
				setPromptMessage(response, "1", "更新发布状态失败");
			}
		}else{
			setPromptMessage(response, "1", "更新发布状态失败");
		}
		return response;
	}
	/**
	 * 删除一条工资单信息
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/lookup/deleteSalary",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteSalary(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String salaryId = request.getString("salaryId");
		if(!StringUtils.isEmpty(salaryId)){
			param.put("salaryId", salaryId);
			int result = salaryService.deleteSalary(param);
			if(result > 0){
				setPromptMessage(response, "0", "删除工资成功");
			}else{
				setPromptMessage(response, "1", "删除工资失败");
			}
		}else{
			setPromptMessage(response, "1", "删除工资失败");
		}
		return response;
	}
	/**
	 * 管理员查看导入老师的工资详情
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/lookup/getSalaries",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSalaries(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String gradeGroupId = request.getString("gradeGroupId");
		String researchGroupId = request.getString("researchGroupId");
		String teacherName = request.getString("teacherName");
		String salaryId = request.getString("salaryId");
		List<Account> la = new ArrayList<Account>();
		List<Account> accoutList = commonDataService.getAllSchoolEmployees(getSchool(req,null), getCurXnxq(req), "");
		if(StringUtils.isNotEmpty(gradeGroupId) && StringUtils.isNotEmpty(researchGroupId)){
			la = commonDataService.getOrgTeacherListIntersect(getCurXnxq(req), Long.valueOf(getXxdm(req)), gradeGroupId, researchGroupId, "");
		}else if(StringUtils.isEmpty(gradeGroupId) && StringUtils.isEmpty(researchGroupId)){
			la=accoutList;
		}else if(StringUtils.isNotEmpty(gradeGroupId) && StringUtils.isEmpty(researchGroupId)){
			la = commonDataService.getOrgTeacherList(getCurXnxq(req), Long.valueOf(getXxdm(req)), gradeGroupId, "");
		}else{
			la = commonDataService.getOrgTeacherList(getCurXnxq(req), Long.valueOf(getXxdm(req)), researchGroupId, "");
		}
		String[] groupIds = new String[la.size()];
		for(int i = 0;i<la.size();i++){
			groupIds[i] = String.valueOf(la.get(i).getId());
		}
		
		String[] t_Name = new String[accoutList.size()];
		String[] t_Id = new String[accoutList.size()];
		for (int i = 0; i < accoutList.size(); i++) {
			t_Name[i] = accoutList.get(i).getName();
			t_Id[i] = String.valueOf(accoutList.get(i).getId());
		}
		if(!StringUtils.isEmpty(salaryId)){
			param.put("salaryId", salaryId);
			if(groupIds.length>0){
				param.put("groupIds", groupIds);
			}else{
				List<String> list = new ArrayList<String>();
				list.add("-1");
				param.put("groupIds",list);
			}
			if(StringUtils.isEmpty(teacherName)){
				param.put("teacherId", null);
			} else {
				List<Integer> t_index = indexLikeInArray(teacherName, t_Name);
				List<String> s_index = new ArrayList<String>();
				for(Integer i:t_index){
					s_index.add(t_Id[i]);
				}
				if(s_index.size()==0){
					s_index.add("TEACHERID");
				}
					param.put("teacherId", s_index);
			}
			JSONObject data = salaryService.getSalaries(param);
			if(!CollectionUtils.isEmpty(data)){
				setPromptMessage(response, "0", "查询成功");
				JSONArray array = data.getJSONArray("rows");
				for(int i=0;i<array.size();i++){
					String tid = array.getJSONObject(i).getString("r0c0");
					int nameNum = indexInArray(tid, t_Id);
					if(nameNum>-1){
						array.getJSONObject(i).put("r0c0", t_Name[nameNum]);
					}
				}
				response.put("data", data);
			}else{
				setPromptMessage(response, "0", "没有查询到相关数据");
			}
		}else{
			setPromptMessage(response, "1", "参数异常");
		}
		return response;
	}
	/**
	 * 查看个人工资单信息
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/lookup/getPersonalSalary",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getPersonalSalary(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String year = request.getString("year");
		String month = request.getString("month");
		String teacherId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		if(!StringUtils.isEmpty(teacherId)){
			if(StringUtils.isNotEmpty(year)&&StringUtils.isNotEmpty(month)){
				param.put("year", year);
				param.put("month", month);
			}else{
				response.put("message", "第一次查询");
			}
			param.put("teacherId", teacherId);
			param.put("schoolId", getXxdm(req));
			
			List<JSONObject> data = salaryService.getPersonalSalary(param);
			if(data.size()!=0){
				setPromptMessage(response, "0", "查询成功");
				response.put("data", data);
			}else{
				setPromptMessage(response, "0", "没有查询到相关数据");
			}
		}else{
			setPromptMessage(response, "1", "参数异常");
		}
		return response;
	}
	/**
	 * APP端查看个人工资详情
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/lookup/getAppPersonalSalary",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAppPersonalSalary(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String year = request.getString("year");
		String month = request.getString("month");
		Long userId = request.getLong("userId");
		String schoolId = request.getString("schoolId");
		String selectedSemester = commonDataService.getCurTermInfoId(Long.parseLong(schoolId));
		User user = commonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
		String teacherId ="";
		if(user!=null){
			teacherId = String.valueOf(user.getAccountPart().getId());
		}
		param.put("teacherId", teacherId);
		param.put("schoolId", schoolId);
		if(StringUtils.isNotBlank(teacherId)&&StringUtils.isNotBlank(schoolId)){
			if(StringUtils.isNotBlank(year)&&StringUtils.isNotBlank(month)){
				param.put("year", year);
				param.put("month", month);
			}else{
				setPromptMessage(response, "2", "第一次查询");
			}
			List<JSONObject> data = salaryService.getAppPersonalSalary(param);
			if(data.size()!=0){
				setPromptMessage(response, "1", "查询成功");
				response.put("data", data);
			}else{
				setPromptMessage(response, "3", "没有查询到相关数据");
			}
		}else{
			setPromptMessage(response, "-1", "参数异常");
		}
		return response;
	}
	/**
	 * 导入工资
	 * @param file
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping("/importSalary/importSalary")
	@ResponseBody
	public JSONObject importSalary(@RequestParam("importFile") MultipartFile file, 
			HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		 HttpSession session = req.getSession();
		 //String salaryId = req.getParameter("salaryId");
		 String schoolId = getXxdm(req);
		 SalItems si = new SalItems();
		 si.setSessionId(session.getId());
		 Object salKey = "salary."+session.getId()+".salItems";
		// 获取教师列表
		 List<Account> accoutList = commonDataService.getAllSchoolEmployees(getSchool(req,null), getCurXnxq(req), "");
       // List<Account> accoutList = commonDataService.getCourseTeacherList(getCurXnxq(req), getSchool(req), "");
        String[] teacherTitleName = new String[accoutList.size()]; 
        String[] teacherTitleId = new String[accoutList.size()]; 
    	for(int i=0; i < accoutList.size(); i++){
            teacherTitleName[i] = accoutList.get(i).getName();
            teacherTitleId[i] = String.valueOf(accoutList.get(i).getId());
    	}
    	si.setTeacherTitleId(teacherTitleId);
    	si.setTeacherTitleName(teacherTitleName);
    	File df = null;
		try {
				si.setHeadRowNum(Integer.parseInt(req.getParameter("headRowNum")));
				String fileName = file.getOriginalFilename();
				String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
				fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +suffix;
				String tempName0 = UUID.randomUUID().toString()+"."+suffix;
				df = new File(tempName0);
				file.transferTo(df);
				String keyId = UUIDUtil.getUUID();
				Object tempFileIdKey = "salary."+session.getId()+".fileId";
				String fileId = fileServerImplFastDFS.uploadFile(df,tempName0);
				fileImportInfoService.addFile(schoolId, keyId, fileId);
				redisOperationDAO.set(tempFileIdKey, fileId);
				/*Object tempFileKey = "salary."+session.getId()+".tempFile";
				redisOperationDAO.set(tempFileKey, fileId, CacheExpireTime.sessionMaxExpireTime.getTimeValue());*/
				if(suffix.equalsIgnoreCase("xls")||suffix.equalsIgnoreCase("xlsx")){
					setPromptMessage(response, "0", "文件格式正确");
				}else{
					setPromptMessage(response, "-1", "文件不是Excel格式");
					return response;
				}
				Workbook workbook = WorkbookFactory.create(df);

				Sheet sheet = workbook.getSheetAt(0);
				int maxRow = sheet.getPhysicalNumberOfRows();
				Row row = null;
				int headRowNum = si.getHeadRowNum();
				if(maxRow > headRowNum){
					row = sheet.getRow(headRowNum-1);
					int cellNum = row.getLastCellNum();
					String[] headTitleName = new String[cellNum];
					for(int i = 0;i < cellNum;i++){
						headTitleName[i] = getMergedRegionValue(sheet, headRowNum-1, i);
					}
					si.setHeadTitleName(headTitleName);
					int index = indexInArray(SYSFIELD_TEACHER, headTitleName);
					if(index==0){
						setPromptMessage(response, "0", "文件格式正确");
						si.setHeadNameIndex(index);
					}else{
						setPromptMessage(response, "-1", "第一列必须为教师姓名");
					}
				}else{
					setPromptMessage(response, "-2", "Excel没有数据！！");
				}
				for(int j=headRowNum;j<maxRow;j++){
					if(sheet.getRow(j)!=null){
						for(int k=0;k<sheet.getRow(j).getLastCellNum();k++){
							if(isMerged(sheet, j,k)||StringUtils.isEmpty(getCellValue(sheet.getRow(j).getCell(0)))){
								setPromptMessage(response, "-100", "表头行输入错误或教师姓名列有空值");
								break;
							}
							
						}
					}
				}
				workbook.close();
				redisOperationDAO.set(salKey, si,CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
			} catch (Exception e) {
				setPromptMessage(response, "-3", "Excel出现未可知错误");
				e.printStackTrace();
			}finally{
			if(df!=null)df.delete();
		}
		return response;
	}
		
	/**
	 * 设置教师公共参数
	 * @param req
	 * @author zhh
	 */
	private int setTeacherNameMap(HttpServletRequest req)
	{
		int code=1;
		try {
			School school =this.getSchool(req,null); 
			code = 1;
			// 获取教师列表
			List<Account> accoutList = commonDataService.getAllSchoolEmployees(school, getCurXnxq(req), "");
			String [] teacherName = new String[accoutList.size()];
			HashMap<String,JSONObject> teacherMap = new HashMap<String,JSONObject>();
			for(int i=0; i < accoutList.size(); i++){
				String key = accoutList.get(i).getName();
			    teacherName[i] = key;
			    Account account=accoutList.get(i);
			    JSONObject obj=new JSONObject();
			    obj.put("teacherId", account.getId());
			    obj.put("teacherName", account.getName());
				if(teacherMap.containsKey(key)){
					JSONObject obj1 = teacherMap.get(key);
					int count = obj1.getIntValue("count");
					count ++;
					obj1.put("count", count);
							
				}else{
					obj.put("count", 1);
					teacherMap.put(key, obj);
				}
			}
			
			req.getSession().setAttribute("teacherName", teacherName);
			req.getSession().setAttribute("teacherMap", teacherMap);
		} catch (Exception e) {
			code=-50;
			e.printStackTrace();
		}
    	
    	return code;
	}
	/**
     * 导入任务启动接口
     * 
     * @param req
     * @param res
     * @return
     */
    @RequestMapping(value = "/importSalary/startImportTask")
    @ResponseBody
    public synchronized JSONObject startImportTask(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res) {
    	JSONObject response = new JSONObject();
        String salaryId = request.getString("salaryId");
        HttpSession session = req.getSession();
        String schoolId = getXxdm(req);
        JSONObject toFront = new JSONObject();
		JSONObject data = new JSONObject();
		//***获取teacherNameMap信息  并存入session 以便run中使用
		int code = setTeacherNameMap(req);
		if(code<0){
				JSONObject obj = new JSONObject();
				obj.put("code", -50); //code=-50为redis读取异常
		        obj.put("msg", "出现异常，导入失败！");
		        return obj;
		}
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		JSONObject prepData=new JSONObject();
		prepDataMap.put(session.getId(), prepData);
		
		data.put("progress", 0);
		data.put("msg", "开始启动导入程序...");
		toFront.put("data", data);
		toFront.put("code", 0);
		Object progressKey = "salary."+session.getId()+".progress";
		Object prepDataMapKey = "salary." + getXxdm(req) +session.getId()
				+ ".prepDataMap";
		try {
			redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(prepDataMapKey, prepDataMap,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
        SubProcess sp = new SubProcess(schoolId,salaryId,session.getId(),session);
        sp.start();
        logger.info("主线程结束！");
        setPromptMessage(response, "0", "正常启动任务");
        return response;
    }
    public  boolean isDigits(String str) { 
        return str.matches("^[-\\+]?\\d+(\\.\\d+)?$");
    }
	class SubProcess extends Thread{
		private String schoolId;
		private String salaryId;
		private String sessionId;
		private HttpSession ses;
		public SubProcess(String schoolId,String salaryId,String sessionId,HttpSession ses){
			this.schoolId = schoolId;
			this.salaryId = salaryId;
			this.sessionId = sessionId;
			this.ses = ses;
		}
		@Override
		public void run() {
			super.run();
			long t1 = (new Date()).getTime();
			
			JSONObject toFront = new JSONObject();
			JSONObject data = new JSONObject();
			Object salKey = "salary."+sessionId+".salItems";
			Object prepDataMapKey = "salary." + schoolId  +sessionId+ ".prepDataMap";
			Object progressKey = "salary."+sessionId+".progress";
			//Object tempFileKey = "salary."+sessionId+".tempFile";
			File temfile = null;
			Workbook workbook = null;
			SalItems si = null;
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			try {
				
				Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
				if (null != prepDataMapObj) {
					prepDataMap = (Hashtable<String, JSONObject>) prepDataMapObj;
				}
				si = (SalItems) redisOperationDAO.get(salKey);
				if(si==null|| null == prepDataMapObj){
					toFront.put("code", -100);
					data.put("progress", 100);
					data.put("msg", "由于长时间未操作，请重新导入");
					toFront.put("data", data);
					try {
						redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
				Object tempFileIdKey = "salary."+sessionId+".fileId";
				String fileID = (String) redisOperationDAO.get(tempFileIdKey);
				String localFilenName = UUID.randomUUID().toString();
				fileServerImplFastDFS.downloadFile(fileID, localFilenName);
				//在删除表中的记录
				fileImportInfoService.deleteFileByFileId(fileID);
				fileServerImplFastDFS.deleteFile(fileID);
				temfile = new File(localFilenName);
				workbook = WorkbookFactory.create(temfile);
			} catch(Exception e) {
				e.printStackTrace();
			}
			Sheet sheet = workbook.getSheetAt(0);
			int maxRow = sheet.getPhysicalNumberOfRows();
			//读取Excel中的数据
			data.put("progress", 5);
			data.put("msg", "正在读取校验excel数据");
			toFront.put("data", data);
			toFront.put("code", 1);
			
			
			try {
				redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
			HashMap<String,JSONObject> teacherMap = (HashMap<String, JSONObject>) ses.getAttribute("teacherMap");
			si.setL_salExcel(getSalExcels(sheet, si.getHeadRowNum(),salaryId,schoolId));
			String[] excelTeachers = new String[maxRow-si.getHeadRowNum()];
			
			Map<String,String> semap=new HashMap<String, String>();
			for(SalExcel se:si.getL_salExcel()){
				semap.put(se.getEn_sal(), se.getZh_sal());
			}
			List<SalDetail> successInfos = new ArrayList<SalDetail>();
			List<JSONObject> errorInfos = new ArrayList<JSONObject>();
			for(int i = si.getHeadRowNum();i<maxRow;i++){
				String teacherName = getCellValue(sheet.getRow(i).getCell(si.getHeadNameIndex()));
				if(isDigits(teacherName)){
					excelTeachers[i-si.getHeadRowNum()] =(int)Double.parseDouble(teacherName)+"";
				}else{
					excelTeachers[i-si.getHeadRowNum()] = teacherName;
				}
			}
			JSONObject preData = prepDataMap.get(sessionId);
			JSONObject rowdata=new JSONObject();
			//判断ecxel表中的重复数据
			for(int i = 0;i<excelTeachers.length;i++){
				SalDetail sd = null;
				boolean flag2 = false;
				String[] excelTeacherTemp = excelTeachers.clone();
				excelTeacherTemp[i]="nul";
				int result = indexInArray(excelTeachers[i], si.getTeacherTitleName());
					//教师姓名在基础数据中重复
					JSONObject tObj = teacherMap.get(excelTeachers[i]);
					Map<String,String> colmap=new HashMap<String, String>();
					JSONObject rowd=new JSONObject();
					rowd.put("xm", excelTeachers[i]);
					rowd.put("colmap", colmap);
					rowdata.put("semap", semap);
					rowdata.put(i+si.getHeadRowNum()+1+"", rowd);
					if(tObj!=null){
						int count = tObj.getIntValue("count");
						if(count>1){
							flag2=true;
							JSONObject object = new JSONObject();
							object.put("rowNum", i+si.getHeadRowNum()+1);
							object.put("teacherName", excelTeachers[i]);
							object.put("err", "系统中存在多条该信息！");
							String teacher_title = getMergedRegionValue(sheet, si.getHeadRowNum()-1, si.getHeadNameIndex());
							object.put("teacher_title", teacher_title);
							try{
								sd = getRowSalDetail(sheet, si.getHeadRowNum()+i, si.getHeadNameIndex(),si.getHeadRowNum());
							}catch(IllegalStateException e){
								object.put("dataErr", e.getMessage());
								errorInfos.add(object);
							}
							sd.setSalaryId(salaryId);
							sd.setSchoolId(schoolId);
							object.put("salDetail", sd);
							errorInfos.add(object);
							
							if(!sd.getErrorlist().isEmpty()){
								for(JSONObject s:sd.getErrorlist()){
									JSONObject object1 = new JSONObject();
									String projectName="";
									if(semap.containsKey(s.getString("projectId"))){
										projectName=semap.get(s.getString("projectId"));
									}
									if(s.getString("value").length()>250){
									object1.put("rowNum",i+si.getHeadRowNum()+1);
									object1.put("teacherName",s.getString("value"));
									object1.put("err", "内容文字超过250字");
									object1.put("teacher_title", projectName);
									object1.put("col", s.getString("projectId"));
									sd.setSchoolId(schoolId);
									object1.put("salDetail", sd);
									errorInfos.add(object1);
									}else{
										object1.put("rowNum",i+si.getHeadRowNum()+1);
										object1.put("teacherName",s.getString("value"));
										object1.put("err", "内容文字有异常");
										object1.put("teacher_title", projectName);
										object1.put("col", s.getString("projectId"));
										sd.setSchoolId(schoolId);
										object1.put("salDetail", sd);
										errorInfos.add(object1);
									}
									
									colmap.put(s.getString("projectId"),s.getString("value"));
								}
							}
						}
					}
					
				 if(flag2==false&&(indexInArray(excelTeachers[i], excelTeacherTemp)>-1)){
					JSONObject object = new JSONObject();
					object.put("rowNum", i+si.getHeadRowNum()+1);
					object.put("teacherName", excelTeachers[i]);
					String teacher_title = getMergedRegionValue(sheet, si.getHeadRowNum()-1, si.getHeadNameIndex());
					object.put("teacher_title", teacher_title);
					object.put("err", "教师姓名重复！");
					try{
						sd = getRowSalDetail(sheet, si.getHeadRowNum()+i, si.getHeadNameIndex(),si.getHeadRowNum());
					}catch(IllegalStateException e){
						object.put("dataErr", e.getMessage());
						errorInfos.add(object);
					}
					sd.setSalaryId(salaryId);
					sd.setSchoolId(schoolId);
					//String teacher_title = getCellValue(sheet.getRow(headRowNum-1).getCell(headNameIndex));
					object.put("salDetail", sd);
					errorInfos.add(object);
					
					if(!sd.getErrorlist().isEmpty()){
						for(JSONObject s:sd.getErrorlist()){
							JSONObject object1 = new JSONObject();
							String projectName="";
							if(semap.containsKey(s.getString("projectId"))){
								projectName=semap.get(s.getString("projectId"));
							}
							if(s.getString("value").length()>250){
							object1.put("rowNum",i+si.getHeadRowNum()+1);
							object1.put("teacherName",s.getString("value"));
							object1.put("err", "内容文字超过250字");
							object1.put("teacher_title", projectName);
							object1.put("col", s.getString("projectId"));
							sd.setSchoolId(schoolId);
							object1.put("salDetail", sd);
							errorInfos.add(object1);
							}else{
								object1.put("rowNum",i+si.getHeadRowNum()+1);
								object1.put("teacherName",s.getString("value"));
								object1.put("err", "内容文字有异常");
								object1.put("teacher_title", projectName);
								object1.put("col", s.getString("projectId"));
								sd.setSchoolId(schoolId);
								object1.put("salDetail", sd);
								errorInfos.add(object1);
							}
							
							colmap.put(s.getString("projectId"),s.getString("value"));
						}
					}
				}else if(flag2==false&&result>=0){
					boolean flag=true;
					try{
						sd = getRowSalDetail(sheet, si.getHeadRowNum()+i,si.getHeadNameIndex(),si.getHeadRowNum());

						if(!sd.getErrorlist().isEmpty()){
							for(JSONObject s:sd.getErrorlist()){
								JSONObject object1 = new JSONObject();
								String projectName="";
								if(semap.containsKey(s.getString("projectId"))){
									projectName=semap.get(s.getString("projectId"));
								}
								if(s.getString("value").length()>250){
								object1.put("rowNum",i+si.getHeadRowNum()+1);
								object1.put("teacherName",s.getString("value"));
								object1.put("err", "内容文字超过250字");
								object1.put("teacher_title", projectName);
								object1.put("col", s.getString("projectId"));
								sd.setSchoolId(schoolId);
								object1.put("salDetail", sd);
								errorInfos.add(object1);
								}else{
									object1.put("rowNum",i+si.getHeadRowNum()+1);
									object1.put("teacherName",s.getString("value"));
									object1.put("err", "内容文字有异常");
									object1.put("teacher_title", projectName);
									object1.put("col", s.getString("projectId"));
									sd.setSchoolId(schoolId);
									object1.put("salDetail", sd);
									errorInfos.add(object1);
								}
								
								colmap.put(s.getString("projectId"),s.getString("value"));
							}
							
							flag=false;
						}
					}catch(IllegalStateException e){
						JSONObject object = new JSONObject();
						object.put("rowNum", i+si.getHeadRowNum()+1);
						object.put("teacherName", excelTeachers[i]);
						String teacher_title = getMergedRegionValue(sheet, si.getHeadRowNum()-1, si.getHeadNameIndex());
						object.put("teacher_title", teacher_title);
						object.put("dataErr", e.getMessage());
						errorInfos.add(object);
					}
					sd.setTeacherId(si.getTeacherTitleId()[result]);
					sd.setSalaryId(salaryId);
					sd.setSchoolId(schoolId);
					if(flag){
						successInfos.add(sd);
						}
					
				}else if(flag2==false&&result<0){
					JSONObject object = new JSONObject();
					object.put("rowNum", i+si.getHeadRowNum()+1);
					object.put("teacherName", excelTeachers[i]);
					String teacher_title = getMergedRegionValue(sheet, si.getHeadRowNum()-1, si.getHeadNameIndex());
					object.put("teacher_title", teacher_title);
					object.put("err", "无匹配记录！");
					try{
						sd = getRowSalDetail(sheet, si.getHeadRowNum()+i, si.getHeadNameIndex(),si.getHeadRowNum());
					}catch(IllegalStateException e){
						object.put("dataErr", e.getMessage());
						errorInfos.add(object);
					}
					sd.setSalaryId(salaryId);
					sd.setSchoolId(schoolId);
					//String teacher_title = getCellValue(sheet.getRow(headRowNum-1).getCell(headNameIndex));
					object.put("salDetail", sd);
					errorInfos.add(object);
					
					if(!sd.getErrorlist().isEmpty()){
						for(JSONObject s:sd.getErrorlist()){
							JSONObject object1 = new JSONObject();
							String projectName="";
							if(semap.containsKey(s.getString("projectId"))){
								projectName=semap.get(s.getString("projectId"));
							}
							if(s.getString("value").length()>250){
							object1.put("rowNum",i+si.getHeadRowNum()+1);
							object1.put("teacherName",s.getString("value"));
							object1.put("err", "内容文字超过250字");
							object1.put("teacher_title", projectName);
							object1.put("col", s.getString("projectId"));
							sd.setSchoolId(schoolId);
							object1.put("salDetail", sd);
							errorInfos.add(object1);
							}else{
								object1.put("rowNum",i+si.getHeadRowNum()+1);
								object1.put("teacherName",s.getString("value"));
								object1.put("err", "内容文字有异常");
								object1.put("teacher_title", projectName);
								object1.put("col", s.getString("projectId"));
								sd.setSchoolId(schoolId);
								object1.put("salDetail", sd);
								errorInfos.add(object1);
							}
							
							colmap.put(s.getString("projectId"),s.getString("value"));
						}
					}
				}
			}
			preData.put("rowdatas", rowdata);
			if(errorInfos.size()>0){
				toFront.put("code", -2);
				data.put("progress", 100);
				JSONObject res=getValidateMsg(errorInfos);
				List<JSONObject> siz=(List<JSONObject>) res.get("rows");
				int s=siz!=null?siz.size():0;
				data.put("msg", "共"+(s+successInfos.size())+"条记录："+successInfos.size()+"条导入成功,"+s+"条导入失败");
				data.put("total", successInfos.size());
				data.put("validateMsg",res);
				toFront.put("data", data);
				try {
					redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
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
							redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
						salaryService.addImportSalary(successInfos, si.getL_salExcel());
						toFront.put("code", 2);
						data.put("progress", 100);
						data.put("msg", "恭喜，导入工资表结束，共导入" + (successInfos.size())
								+ "位教师工资！");
						toFront.put("data", data);
						try {
							redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						toFront.put("code", -1);
						data.put("progress", 100);
						data.put("msg", "服务器出错,导入失败!");
						try {
							redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
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
						redisOperationDAO.set(progressKey, toFront, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			si.setSuccessInfos(successInfos);
			si.setErrorInfos(errorInfos);
			si.setExcelTeachers(excelTeachers);
			try {
				redisOperationDAO.set(salKey, si,CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
				redisOperationDAO.set(prepDataMapKey, prepDataMap,CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
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
	/**
	 * 获取导入进度
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/importSalary/importProgress",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject importProgress(HttpServletRequest req,
			HttpServletResponse res) {
		String sessionId = req.getSession().getId();
		Object progressKey = "salary."+sessionId+".progress";
		JSONObject obj;
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
	/**
	 * 导入数据单条验证
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/importSalary/singleDataCheck",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject singleDataCheck(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject response = new JSONObject(); 
		int row = request.getIntValue("row");
		int code = request.getIntValue("code");
		JSONObject toFront = new JSONObject();
		JSONObject data = new JSONObject();
		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		int errorIndex = -1;
		Object salKey = "salary."+req.getSession().getId()+".salItems";
		SalItems si = null;
		try {
			si = (SalItems) redisOperationDAO.get(salKey);
			if(si==null){
				setPromptMessage(response, "-50", "由于长时间未操作，请重新导入");
				return response;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<JSONObject> errorInfos = si.getErrorInfos();
		List<SalDetail> successInfos = si.getSuccessInfos();
		logger.info("***********************singleDataCheckSalaryerrorInfo********************:"+errorInfos);
		for(int i = 0;i<errorInfos.size();i++){
			if(errorInfos.get(i).getInteger("rowNum")==row){
				errorIndex = i;
				break;
			}
		}
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		//忽略错误信息
		if(code==-1){
			errorInfos.remove(errorIndex);
			setPromptMessage(response, "1", "通过");
		}else{
			JSONArray j_rows = request.getJSONArray("mrows");
			Object prepDataMapKey = "salary." + getXxdm(req)  +req.getSession().getId()+ ".prepDataMap";
			try{
			Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
			if (null != prepDataMapObj) {
				prepDataMap = (Hashtable<String, JSONObject>) prepDataMapObj;
			}
			if ( null == prepDataMapObj) {
				setPromptMessage(response, "-50", "由于长时间未操作，请重新导入");
				return response;
			}
			} catch (Exception e) {
				e.printStackTrace();
			}
			JSONObject preData = prepDataMap.get(req.getSession().getId());
			JSONObject rowdatas=preData.getJSONObject("rowdatas");
			JSONObject rowdata=rowdatas.getJSONObject(row+"");
			Map<String,String> semap=(Map<String, String>) rowdatas.get("semap");
			String value=rowdata.getString("xm");
			Map<String,String> colmap=(Map<String, String>) rowdata.get("colmap");
			List<JSONObject> lj = new ArrayList<JSONObject>();
			String title="";
			for (int i = 0; i < j_rows.size(); i++) {
				JSONObject mrows = j_rows.getJSONObject(i);
				title = mrows.getString("title");
				String value2 = mrows.getString("value");
				if(title.equals("教师姓名")){
					value=value2;
				}
				
			}
				int position = indexInArray(value, si.getTeacherTitleName());
				if(position >= 0){
					boolean flag = false;
					for(SalDetail sd:successInfos){
						if(si.getTeacherTitleId()[position].equals(sd.getTeacherId())){
							flag = true;break;
						}
					}
					boolean flag2= false;
					if(flag){
						setPromptMessage(response, "-1", "校验数据失败");
						JSONObject result = new JSONObject();
						result.put("title", "教师姓名");
						result.put("oldValue", value);
						result.put("err", "教师姓名重复！");
						lj.add(result);
						response.put("mrows", lj);
					}else{
						//判断基础数据是否有多条记录
						HashMap<String,JSONObject> teacherMap = (HashMap<String,JSONObject>) req.getSession().getAttribute("teacherMap");
						//教师姓名在基础数据中重复
						if(!value.isEmpty()){
						JSONObject tObj = teacherMap.get(value);
						if(tObj!=null){
							int count = tObj.getIntValue("count");
							if(count>1){
								setPromptMessage(response, "-1", "校验数据失败");
								JSONObject result = new JSONObject();
								result.put("title",  "教师姓名");
								result.put("oldValue", value);
								result.put("err", "系统中存在多条该信息！");
								lj.add(result);
								flag2=true;
								response.put("mrows", lj);
							}
						}
						}
					}
					
					for (int i = 0; i < j_rows.size(); i++) {
						JSONObject mrows = j_rows.getJSONObject(i);
					if(mrows.containsKey("col")){
						String value1=mrows.getString("value");
						if(value1.length()>250){
							JSONObject result = new JSONObject();
							String projectName="";
							if(semap.containsKey(mrows.getString("col"))){
								projectName=semap.get(mrows.getString("col"));
							}
							result.put("title", projectName);
							result.put("oldValue", value1);
							result.put("err", "内容文字超过250字");
							result.put("col", mrows.getString("col"));
							lj.add(result);
							response.put("mrows", lj);
							flag=true;
						}
						colmap.put(mrows.getString("col"),value1);
					}
					}
					
					rowdata.put("xm", value);
					rowdata.put("colmap", colmap);
				    if(flag==false && flag2==false){
						setPromptMessage(response, "1", "校验通过");
						SalDetail sd = (SalDetail) errorInfos.get(errorIndex).get("salDetail");
						sd.setTeacherId(si.getTeacherTitleId()[position]);
						 Iterator<Entry<String, String>> it1 = colmap.entrySet().iterator();
						  while (it1.hasNext()) {
						   Entry<String, String> entry = it1.next();
							sd.getSalComponentIds().add(entry.getKey());
							sd.getSalNums().add(entry.getValue());
						  }
						successInfos.add(sd);
						errorInfos.remove(errorIndex);
				    }
				
				}else{
					setPromptMessage(response, "-1", "校验数据失败");
					JSONObject result = new JSONObject();
					result.put("title",  "教师姓名");
					result.put("oldValue", value);
					result.put("err", "无匹配记录！");
					lj.add(result);
					response.put("mrows", lj);
					
					for (int i = 0; i < j_rows.size(); i++) {
						JSONObject mrows = j_rows.getJSONObject(i);
					if(mrows.containsKey("col")){
						String value1=mrows.getString("value");
						if(value1.length()>250){
							JSONObject result1 = new JSONObject();
							String projectName="";
							if(semap.containsKey(mrows.getString("col"))){
								projectName=semap.get(mrows.getString("col"));
							}
							result1.put("title", projectName);
							result1.put("oldValue", value1);
							result1.put("err", "内容文字超过250字");
							result1.put("col", mrows.getString("col"));
							lj.add(result1);
							response.put("mrows", lj);
						}
						colmap.put(mrows.getString("col"),value1);
					}
					}
					rowdata.put("xm", value);
					rowdata.put("colmap", colmap);
				}
			try {
				redisOperationDAO.set(salKey, si,CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
				redisOperationDAO.set(prepDataMapKey, prepDataMap,CacheExpireTime.temporaryDataSessionExpireTime.getTimeValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return response;
	}
	/**
	 * 验证通过后继续导入
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/importSalary/continueImport")
	@ResponseBody
	public JSONObject continueImport(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject response = new JSONObject();
		int code = -1;
		String msg = null;
		Object salKey = "salary."+req.getSession().getId()+".salItems";
		SalItems si = null;
		try {
			si = (SalItems) redisOperationDAO.get(salKey);
			if(si==null){
				setPromptMessage(response, "-50", "由于长时间未操作，请重新导入");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<SalDetail> successInfos = si.getSuccessInfos();
		if(successInfos.size()>0){
			int result = salaryService.addImportSalary(successInfos, si.getL_salExcel());
			if(result > 0){
				code = 1;
				msg = "恭喜，导入工资表结束，共导入"+ (successInfos.size()) +"位教师工资！";
			}else{
				msg = "启动进程异常！";
			}
		}else{
			code =1;
			msg = "恭喜，导入工资表结束，共导入0位教师工资！";
		}
		response.put("code",code);
		response.put("msg",msg);
		return response;
	}
	/**
	 * 导出异常信息
	 * @param req
	 * @param res
	 */
	@RequestMapping(value = "/importSalary/exportWrongMsg")
	public void exportWrongMsg(HttpServletRequest req,HttpServletResponse res){

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
	/**
	 * 返回字符串在数组中的位置
	 * @param string
	 * @param arr
	 * @return -1 不在数组中
	 */
	private int indexInArray(String string, String[] arr) {
		String source = string.replace(" ", "");				
		int rs = -1;
		for (int i = 0; i < arr.length; i++) {
			String target = (arr[i] == null?"":arr[i]);
			target = target.replace(" ", "");
			if (target.equalsIgnoreCase(source)) {
				rs = i; break;
			}
		}
		return rs;
	}
	/**
	 * 返回相似字符串在数组中的位置集合
	 * @param string
	 * @param arr
	 * @return List<Integer> 索引位置集合
	 */
	private List<Integer> indexLikeInArray(String string, String[] arr) {
		String source = string.replace(" ", "");				
		List<Integer> rs = new ArrayList<Integer>();
		for (int i = 0; i < arr.length; i++) {
			String target = (arr[i] == null?"":arr[i]);
			target = target.replace(" ", "");
			if (target.indexOf(source)>=0) {
				rs.add(i);
			}
		}
		return rs;
	}
	
	public JSONObject getValidateMsg(List<JSONObject> errorInfos) {
		JSONObject result = new JSONObject();
		List<JSONObject> l_object = new ArrayList<JSONObject>();
		Map<String,List<JSONObject>> mrowmap=new LinkedHashMap<String, List<JSONObject>>();
		if(errorInfos.size()>0){
			result.put("total", errorInfos.size());
			for(int i = 0;i<errorInfos.size();i++){
				int row = errorInfos.get(i).getInteger("rowNum");
				if(mrowmap.containsKey(row+"")){
					List<JSONObject> mrows=mrowmap.get(row+"");
					JSONObject detail = new JSONObject();
					detail.put("title", errorInfos.get(i).getString("teacher_title"));
					detail.put("oldValue", errorInfos.get(i).getString("teacherName"));
					detail.put("err", errorInfos.get(i).getString("err"));
					if(errorInfos.get(i).containsKey("col")){
					detail.put("col", errorInfos.get(i).getString("col"));
					}
					mrows.add(detail);
					mrowmap.put(row+"", mrows);
				}else{
					List<JSONObject> mrows = new ArrayList<JSONObject>();
					JSONObject detail = new JSONObject();
					detail.put("title", errorInfos.get(i).getString("teacher_title"));
					detail.put("oldValue", errorInfos.get(i).getString("teacherName"));
					detail.put("err", errorInfos.get(i).getString("err"));
					if(errorInfos.get(i).containsKey("col")){
						detail.put("col", errorInfos.get(i).getString("col"));
						}
					mrows.add(detail);
					mrowmap.put(row+"", mrows);
				}
				
			}
			 Iterator<Entry<String, List<JSONObject>>> it = mrowmap.entrySet().iterator();
			 
			  while (it.hasNext()) {
				  JSONObject rowInfo = new JSONObject();
			  Entry<String, List<JSONObject>> entry = it.next();
			  String row= entry.getKey();
			  List<JSONObject> mrows=entry.getValue();
			  rowInfo.put("row", row);
			  rowInfo.put("mrows", mrows);
			  l_object.add(rowInfo);
			  }
			result.put("rows", l_object);
		}
		return result;
	}

	/**
	 * 获取合并单元格的值
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public String getMergedRegionValue(Sheet sheet, int row,int col){
		int sheetMergeCount = sheet.getNumMergedRegions();
		for(int i=0;i<sheetMergeCount;i++){
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if((row >= firstRow && row <= lastRow)){
				if(col >= firstCol && col <= lastCol){
					Cell fCell = sheet.getRow(firstRow).getCell(firstCol);
					return getCellValue(fCell);
				}
			}
		}
		return getCellValue(sheet.getRow(row).getCell(col));
	}
	
	/**
	 * 判断制定的单元格是否是合并单元格
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public boolean isMerged(Sheet sheet,int row,int col){
		int sheetMergeCount = sheet.getNumMergedRegions();
		for(int i=0;i<sheetMergeCount;i++){
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if((row >= firstRow && row <= lastRow)){
				if((col >= firstCol && col <= lastCol)){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 得到指定单元格或合并单元格的序号
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public String getRowId(Sheet sheet, int row, int col){
		int sheetMergeCount = sheet.getNumMergedRegions();
		for(int i=0;i<sheetMergeCount;i++){
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if((row >= firstRow && row <= lastRow)){
				if((col >= firstCol && col <= lastCol)){
					return "r"+firstRow+"c"+firstCol;
				}
			}
		}
		return "r"+row+"c"+col;
	}
	/**
	 * 得到指定合并单元格的行的跨度
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public int getRowdiff(Sheet sheet, int row, int col){
		int sheetMergeCount = sheet.getNumMergedRegions();
		for(int i=0;i<sheetMergeCount;i++){
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if((row >= firstRow && row <= lastRow)){
				if(col >= firstCol && col <= lastCol){
					return lastRow-firstRow;
				}
			}
		}
		return 0;
	}
	/**
	 * 得到指定合并单元格的列的跨度
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public int getColdiff(Sheet sheet, int row, int col){
		int sheetMergeCount = sheet.getNumMergedRegions();
		for(int i=0;i<sheetMergeCount;i++){
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			int firstCol = range.getFirstColumn();
			int lastCol = range.getLastColumn();
			if((row >= firstRow && row <= lastRow)){
				if(col >= firstCol && col <= lastCol){
					return lastCol-firstCol;
				}
			}
		}
		return 0;
	}
	/**
	 * 合并指定的单元格
	 * @param sheet
	 * @param firstRow
	 * @param firstCol
	 * @param lastRow
	 * @param lastCol
	 */
	public void mergeRegion(Sheet sheet, int firstRow, int firstCol,int lastRow, int lastCol){
		CellRangeAddress range = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		sheet.addMergedRegion(range);
	}
	
	/**
	 * 获取单元格的值
	 * @param cell
	 * @return
	 */
	public String getCellValue(Cell cell){
		if(cell==null)return "";
		switch(cell.getCellType()){
			case Cell.CELL_TYPE_STRING:
				String str = null;
				String temStr = cell.getRichStringCellValue().getString().trim();
				if(isNumber(temStr)){
					double d = Double.valueOf(temStr);
					if(d%1>0){
						BigDecimal b = new BigDecimal(d);
						str = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
					}else{
						str = temStr;
					}
				}else{
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
					if(d%1>0){
						BigDecimal b = new BigDecimal(d);
						temp = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
					}else{
						temp = String.valueOf(d);
					}
				}
				return temp;
			case Cell.CELL_TYPE_NUMERIC:
				double d = cell.getNumericCellValue();
				String s="";
				if(d%1>0){
					BigDecimal b = new BigDecimal(d);
					s = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
				}else{
					s=String.valueOf(d);
				}
				return s;
		}
		return "";
	}
	
	public boolean isNumber(String str){
		Pattern patter = Pattern.compile("^[-\\+]?[0-9]+(\\.[0-9]+)?$");
		return patter.matcher(str).matches();
	}
	/**
	 * 封装excel表头
	 * @param sheet
	 * @param rowNum
	 * @return
	 */
	public List<SalExcel> getSalExcels(Sheet sheet,int rowNum,String salaryId,String schoolId){
		List<SalExcel> l_salExcels = new ArrayList<SalExcel>();
		Row row = sheet.getRow(0);
		for(int k= 0;k < rowNum; k++ ){
			for(int i=0;i<row.getLastCellNum();){
				SalExcel se = new SalExcel();
				se.setSalaryId(salaryId);
				se.setSchoolId(schoolId);
				se.setHead_rowNum(rowNum);
				String rowId = getRowId(sheet, k, i);
				int r_index = rowId.indexOf("r");
				int c_index = rowId.indexOf("c");
				int curRow = Integer.parseInt(rowId.substring(r_index+1,c_index));
				int curCol = Integer.parseInt(rowId.substring(c_index+1));
				if(isMerged(sheet, k, i)){
					int coldiff = getColdiff(sheet, k, i);
					int rowdiff = getRowdiff(sheet, k, i);
					se.setColspan(coldiff+1);
					se.setEn_sal(rowId);
					se.setZh_sal(getMergedRegionValue(sheet, k, i));
					se.setIn_rowNum(curRow);
					se.setIn_colNum(curCol);
					if(k==0){
						se.setPen_sal(null);
					}else{
						if(rowdiff==0){
							se.setPen_sal(getRowId(sheet, curRow-1, i));
						}else{
							if(curRow==0){
								se.setPen_sal(null);
							}else{
								se.setPen_sal(getRowId(sheet, curRow-1, i));
							}
						}
					}
					se.setRowspan(rowdiff+1);
					i=i+coldiff+1;
				}else{
					se.setColspan(1);
					se.setRowspan(1);
					se.setZh_sal(getCellValue(sheet.getRow(k).getCell(i)));
					se.setEn_sal(rowId);
					se.setIn_rowNum(curRow);
					se.setIn_colNum(curCol);
					if(curRow==0){
						se.setPen_sal(null);
					}else{
						se.setPen_sal(getRowId(sheet, k-1, i));
					}
					i++;
				}
				l_salExcels.add(se);
			}
		}
		//去掉重复元素
		Set<SalExcel> ss = new HashSet<SalExcel>(l_salExcels);	
		return new ArrayList<SalExcel>(ss);
	}
	
	/**
	 * 十进制转26进制
	 * @param num
	 * @return
	 */
	private String toNumberSystem26(int num){
    	num += 1;
        StringBuffer s = new StringBuffer();
        while (num > 0){
            int m = num % 26;
            if (m == 0) m = 26;
            s.append((char)(m + 64));
            num = (num - m) / 26;
        }
        return s.reverse().toString();
    } 
	/**
	 * 得到一行的工资信息
	 * @param sheet
	 * @param row
	 * @param titleIndex
	 * @return
	 */
	public SalDetail getRowSalDetail(Sheet sheet,int row,int titleIndex,int headRowNumber){
		SalDetail sd = new SalDetail();
		List<String> salComponentIds = new ArrayList<String>();
		List<String> salNums = new ArrayList<String>();
		List<JSONObject> errorlist = new ArrayList<JSONObject>();
		StringBuffer errInfo = new StringBuffer();
		for(int j = 0 ;j<sheet.getRow(row).getLastCellNum();j++){
			try{
				if(j == titleIndex)continue;
				String salNum = getCellValue(sheet.getRow(row).getCell(j));
				if(salNum.length()>250){
					JSONObject s=new JSONObject();
					s.put("num", toNumberSystem26(j));
					s.put("projectId",getRowId(sheet, headRowNumber-1, j));
					s.put("value", salNum);
					errorlist.add(s);
				}else{
					salComponentIds.add(getRowId(sheet, headRowNumber-1, j));
					salNums.add(salNum);
				}
			}catch(IllegalStateException e){
				JSONObject s=new JSONObject();
				s.put("num", toNumberSystem26(j));
				s.put("projectId",getRowId(sheet, headRowNumber-1, j));
		  		s.put("value", "");
				errorlist.add(s);
				errInfo.append("第").append(toNumberSystem26(j)).append("列、");
			}
		}
		if(errInfo.length() > 0){
			errInfo.deleteCharAt(errInfo.length() - 1);
			//throw new IllegalStateException(errInfo.toString());
		}
		sd.setErrorlist(errorlist);
		sd.setSalComponentIds(salComponentIds);
		sd.setSalNums(salNums);
		return sd;
	}
	
	/**
	 * 得到所有的工资表详细信息（不包含teacherId 和teacherName）
	 * @param sheet
	 * @param rowNum 表头行数
	 * @return
	 */
	public List<SalDetail> getSalDetails(Sheet sheet,int rowNum){
		List<SalDetail> l_sd = new ArrayList<SalDetail>();
		Row row = sheet.getRow(0);
		int maxRowNum = sheet.getPhysicalNumberOfRows();
		for(int i = rowNum;i < maxRowNum;i++){
			SalDetail sd = new SalDetail();
			List<String> salComponentIds = new ArrayList<String>();
			List<String> salNums = new ArrayList<String>();
			for(int j = 1 ;j<row.getLastCellNum();j++){
				salComponentIds.add(getRowId(sheet, rowNum-1, j));
				String salNum = getCellValue(sheet.getRow(i).getCell(j));
				salNums.add(salNum);
			}
			sd.setSalComponentIds(salComponentIds);
			sd.setSalNums(salNums);
			l_sd.add(sd);
		}
		return l_sd;
	}
	
 
	@RequestMapping(value = "/lookup/getCjSalary",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCjSalary(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String teacherId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		System.setProperty("sun.net.client.defaultConnectTimeout", "16000"); 
		System.setProperty("sun.net.client.defaultReadTimeout", "16000");  
		JSONObject param = new JSONObject();
		param.put("schoolId", getXxdm(req));
		param.put("teacherId", teacherId);
		JSONObject object = salaryService.getCjSalaryAccount(param);
		String url =null;
		String parm =null;
		boolean validate = false;
		if (object != null) {
			String adminAccount = object.getString("adminAccount");
			String adminPass = object.getString("adminPass");
			String bankAccount = object.getString("bankAccount");
			String bankPass = object.getString("bankPass");
			String digitalAccount = object.getString("digitalAccount");
			String digitalPass = object.getString("digitalPass");
			  url = "http://gz.changjun.com.cn/index.aspx";
			  parm ="__EVENTTARGET=DropDownList1&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=%2FwEPDwULLTIxMTU1NDc4MzcPZBYCAgEPZBYKAgEPEGRkFgECAWQCAw8PFgIeBFRleHQFD%2BaVsOWtl%2BW4kOWPt%2B%2B8mmRkAgcPDxYCHwAFD%2BeZu%2BW9leWvhuegge%2B8mmRkAgkPDxYCHgRNb2RlCyolU3lzdGVtLldlYi5VSS5XZWJDb250cm9scy5UZXh0Qm94TW9kZQJkZAIPDxYCHwAFSkNvcHlyaWdodCAyMDA3LTIwMTIg5rmW5Y2X6ZW%2F6YOh5Lqn5Lia5Y%2BR5bGV5pyJ6ZmQ5YWs5Y%2B4LiBUZWw6MDczMS04NzE4NzQ4ZBgBBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WAgUMSW1hZ2VCdXR0b24xBQxJbWFnZUJ1dHRvbjJfx9RQQfOJjfU%2FZjif%2B00wr%2FGB3w%3D%3D&__VIEWSTATEGENERATOR=90059987&__EVENTVALIDATION=%2FwEWCQK8v5qzCAKd5I%2FlCgKNi6WLBgKSi6WLBgKTi6WLBgLokv37AgLyveCRDwLSwpnTCALSwtXkAmupw%2F3sOo07yc9yZ3%2FAnPW7IbXV&ImageButton1.x=38&ImageButton1.y=19";
			if (StringUtils.isNotBlank(adminAccount) && StringUtils.isNotBlank(adminPass)) {
				try {
					adminAccount =  URLEncoder.encode(adminAccount, "utf-8");
					adminPass =  URLEncoder.encode(adminPass, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				parm = parm + "&DropDownList1=2&passid="+adminAccount+"&password=" + adminPass;
				validate = sendGet (url , parm);
			}
			
			if (!validate && StringUtils.isNotBlank(digitalAccount) && StringUtils.isNotBlank(digitalPass)) {
				try {
					digitalAccount =  URLEncoder.encode(digitalAccount, "utf-8");
					digitalPass =  URLEncoder.encode(digitalPass, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				parm = parm + "&DropDownList1=1&passid="+digitalAccount+"&password=" + digitalPass;
				validate = sendGet (url , parm);
			 }
			
			if (!validate && StringUtils.isNotBlank(bankAccount) && StringUtils.isNotBlank(bankPass)) {
				try {
					bankPass =  URLEncoder.encode(bankPass, "utf-8");
					bankAccount =  URLEncoder.encode(bankAccount, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				parm = parm + "&DropDownList1=0&passid="+bankAccount+"&password=" + bankPass;
				validate = sendGet (url , parm);
			}
		}
		  
		if (validate) {
			setPromptMessage(response, "0", "成功");
			response.put("url", url +"?" + parm);
		}else{
			setPromptMessage(response, "-1", "用户名或密码错误");
		}
 
		return response;
	}
	
	@RequestMapping(value = "/lookup/updateCjSalaryAccount",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateCjSalaryAccount(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String DropDownList1 = request.getString("DropDownList1");
		String passid = request.getString("passid");
		String password = request.getString("password");
		passid= passid!=null?passid.trim():"";
		password= password!=null?password.trim():"";
		
		String encodePassid = null;
		String encodePassword = null;
		try {
			encodePassid =  URLEncoder.encode(passid, "utf-8");
			encodePassword =  URLEncoder.encode(password, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String url = "http://gz.changjun.com.cn/index.aspx";
		String parm ="__EVENTTARGET=DropDownList1&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=%2FwEPDwULLTIxMTU1NDc4MzcPZBYCAgEPZBYKAgEPEGRkFgECAWQCAw8PFgIeBFRleHQFD%2BaVsOWtl%2BW4kOWPt%2B%2B8mmRkAgcPDxYCHwAFD%2BeZu%2BW9leWvhuegge%2B8mmRkAgkPDxYCHgRNb2RlCyolU3lzdGVtLldlYi5VSS5XZWJDb250cm9scy5UZXh0Qm94TW9kZQJkZAIPDxYCHwAFSkNvcHlyaWdodCAyMDA3LTIwMTIg5rmW5Y2X6ZW%2F6YOh5Lqn5Lia5Y%2BR5bGV5pyJ6ZmQ5YWs5Y%2B4LiBUZWw6MDczMS04NzE4NzQ4ZBgBBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WAgUMSW1hZ2VCdXR0b24xBQxJbWFnZUJ1dHRvbjJfx9RQQfOJjfU%2FZjif%2B00wr%2FGB3w%3D%3D&__VIEWSTATEGENERATOR=90059987&__EVENTVALIDATION=%2FwEWCQK8v5qzCAKd5I%2FlCgKNi6WLBgKSi6WLBgKTi6WLBgLokv37AgLyveCRDwLSwpnTCALSwtXkAmupw%2F3sOo07yc9yZ3%2FAnPW7IbXV&ImageButton1.x=38&ImageButton1.y=19";
		parm = parm + "&DropDownList1="+DropDownList1+"&passid="+encodePassid+"&password=" + encodePassword;
		boolean validate = false;
		validate = sendGet (url , parm);
		 
		if (validate) {
			setPromptMessage(response, "0", "成功");
			response.put("url", url +"?" + parm);
			
			String teacherId = String.valueOf((long)req.getSession().getAttribute("accountId"));
			JSONObject param = new JSONObject();
			param.put("schoolId", getXxdm(req));
			param.put("teacherId", teacherId);
			param.put("DropDownList1", DropDownList1);
			param.put("passid", passid);
			param.put("password", password);
			salaryService.updateCjSalaryAccount(param);
		}else {
			setPromptMessage(response, "-1", "用户名或密码错误");
		}
		return response;
	}
	
	
	@RequestMapping(value = "/lookup/getAppCjSalary",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAppCjSalary(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res){

		JSONObject response = new JSONObject();
	 
		String teacherId = null;
		Long schoolId = request.getLong("schoolId");
		Long userId = request.getLong("userId");
		String selectedSemester =commonDataService.getCurTermInfoId(schoolId);
		User user = commonDataService.getUserById( schoolId , userId,selectedSemester);
		teacherId = user.getAccountPart().getId()+"";
		System.setProperty("sun.net.client.defaultConnectTimeout", "16000"); 
		System.setProperty("sun.net.client.defaultReadTimeout", "16000");  
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("teacherId", teacherId);
		JSONObject object = salaryService.getCjSalaryAccount(param);
		String url =null;
		String parm =null;
		boolean validate = false;
		if (object != null) {
			String adminAccount = object.getString("adminAccount");
			String adminPass = object.getString("adminPass");
			String bankAccount = object.getString("bankAccount");
			String bankPass = object.getString("bankPass");
			String digitalAccount = object.getString("digitalAccount");
			String digitalPass = object.getString("digitalPass");
			  url = "http://gz.changjun.com.cn/index.aspx";
			  parm ="__EVENTTARGET=DropDownList1&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=%2FwEPDwULLTIxMTU1NDc4MzcPZBYCAgEPZBYKAgEPEGRkFgECAWQCAw8PFgIeBFRleHQFD%2BaVsOWtl%2BW4kOWPt%2B%2B8mmRkAgcPDxYCHwAFD%2BeZu%2BW9leWvhuegge%2B8mmRkAgkPDxYCHgRNb2RlCyolU3lzdGVtLldlYi5VSS5XZWJDb250cm9scy5UZXh0Qm94TW9kZQJkZAIPDxYCHwAFSkNvcHlyaWdodCAyMDA3LTIwMTIg5rmW5Y2X6ZW%2F6YOh5Lqn5Lia5Y%2BR5bGV5pyJ6ZmQ5YWs5Y%2B4LiBUZWw6MDczMS04NzE4NzQ4ZBgBBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WAgUMSW1hZ2VCdXR0b24xBQxJbWFnZUJ1dHRvbjJfx9RQQfOJjfU%2FZjif%2B00wr%2FGB3w%3D%3D&__VIEWSTATEGENERATOR=90059987&__EVENTVALIDATION=%2FwEWCQK8v5qzCAKd5I%2FlCgKNi6WLBgKSi6WLBgKTi6WLBgLokv37AgLyveCRDwLSwpnTCALSwtXkAmupw%2F3sOo07yc9yZ3%2FAnPW7IbXV&ImageButton1.x=38&ImageButton1.y=19";
			if (StringUtils.isNotBlank(adminAccount) && StringUtils.isNotBlank(adminPass)) {
				try {
					adminAccount =  URLEncoder.encode(adminAccount, "utf-8");
					adminPass =  URLEncoder.encode(adminPass, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				parm = parm + "&DropDownList1=2&passid="+adminAccount+"&password=" + adminPass;
				validate = sendGet (url , parm);
			}
			
			if (!validate && StringUtils.isNotBlank(digitalAccount) && StringUtils.isNotBlank(digitalPass)) {
				try {
					digitalAccount =  URLEncoder.encode(digitalAccount, "utf-8");
					digitalPass =  URLEncoder.encode(digitalPass, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				parm = parm + "&DropDownList1=1&passid="+digitalAccount+"&password=" + digitalPass;
				validate = sendGet (url , parm);
			 }
			
			if (!validate && StringUtils.isNotBlank(bankAccount) && StringUtils.isNotBlank(bankPass)) {
				try {
					bankPass =  URLEncoder.encode(bankPass, "utf-8");
					bankAccount =  URLEncoder.encode(bankAccount, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				parm = parm + "&DropDownList1=0&passid="+bankAccount+"&password=" + bankPass;
				validate = sendGet (url , parm);
			}
		}
		  
		if (validate) {
			setPromptMessage(response, "0", "成功");
			response.put("url", url +"?" + parm);
		}else{
			setPromptMessage(response, "-1", "用户名或密码错误");
		}
 
		return response;
 
	}
	
	
	@RequestMapping(value = "/lookup/updateAppCjSalaryAccount",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateAppCjSalaryAccount(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String DropDownList1 = request.getString("DropDownList1");
		String passid = request.getString("passid");
		String password = request.getString("password");
		String teacherId = null;
		Long schoolId = request.getLong("schoolId");
		Long userId = request.getLong("userId");
		String selectedSemester =commonDataService.getCurTermInfoId(schoolId);
		User user = commonDataService.getUserById( schoolId , userId,selectedSemester);
		teacherId = user.getAccountPart().getId()+"";
			
			
		
		passid= passid!=null?passid.trim():"";
		password= password!=null?password.trim():"";
		
		String encodePassid = null;
		String encodePassword = null;
		try {
			encodePassid =  URLEncoder.encode(passid, "utf-8");
			encodePassword =  URLEncoder.encode(password, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String url = "http://gz.changjun.com.cn/index.aspx";
		String parm ="__EVENTTARGET=DropDownList1&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=%2FwEPDwULLTIxMTU1NDc4MzcPZBYCAgEPZBYKAgEPEGRkFgECAWQCAw8PFgIeBFRleHQFD%2BaVsOWtl%2BW4kOWPt%2B%2B8mmRkAgcPDxYCHwAFD%2BeZu%2BW9leWvhuegge%2B8mmRkAgkPDxYCHgRNb2RlCyolU3lzdGVtLldlYi5VSS5XZWJDb250cm9scy5UZXh0Qm94TW9kZQJkZAIPDxYCHwAFSkNvcHlyaWdodCAyMDA3LTIwMTIg5rmW5Y2X6ZW%2F6YOh5Lqn5Lia5Y%2BR5bGV5pyJ6ZmQ5YWs5Y%2B4LiBUZWw6MDczMS04NzE4NzQ4ZBgBBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WAgUMSW1hZ2VCdXR0b24xBQxJbWFnZUJ1dHRvbjJfx9RQQfOJjfU%2FZjif%2B00wr%2FGB3w%3D%3D&__VIEWSTATEGENERATOR=90059987&__EVENTVALIDATION=%2FwEWCQK8v5qzCAKd5I%2FlCgKNi6WLBgKSi6WLBgKTi6WLBgLokv37AgLyveCRDwLSwpnTCALSwtXkAmupw%2F3sOo07yc9yZ3%2FAnPW7IbXV&ImageButton1.x=38&ImageButton1.y=19";
		parm = parm + "&DropDownList1="+DropDownList1+"&passid="+encodePassid+"&password=" + encodePassword;
		boolean validate = false;
		validate = sendGet (url , parm);
		 
		if (validate) {
			setPromptMessage(response, "0", "成功");
			response.put("url", url +"?" + parm);
			
			 
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("teacherId", teacherId);
			param.put("DropDownList1", DropDownList1);
			param.put("passid", passid);
			param.put("password", password);
			salaryService.updateCjSalaryAccount(param);
		}else {
			setPromptMessage(response, "-1", "用户名或密码错误！");
		}
		return response;
	}
	
	
	@RequestMapping(value = "/lookup/getAppRole",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAppRole(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		 JSONObject response = new JSONObject();
		 Long	schoolId = request.getLong("schoolId");
		 Long userId = request.getLong("userId");
		String selectedSemester =commonDataService.getCurTermInfoId(schoolId);
		User user = commonDataService.getUserById( schoolId , userId,selectedSemester);
		Long accountId = user.getAccountPart().getId();
		boolean isManager=authServiceImpl.getIsMoudleManagerByAccountId(schoolId, accountId, "cs1011",selectedSemester);
		if(isManager){
			response.put("role", 1);
		}else{
			response.put("role", 0);
		}

		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		JSONObject object = salaryService.getCjSchool(param);
		if (object!=null) {
			response.put("cj", 1);
		}else {
			response.put("cj", 0);
		}
 
		setPromptMessage(response, "0", "");
		return response;
	}
	
	
	public static boolean sendGet(String url, String param) {
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" +param ;
            HttpURLConnection  conn = null;
            String reUrl= null;
             for (int i = 0; i < 50; i++) {
        	    URL realUrl = new URL(urlNameString);
           	    conn = (HttpURLConnection)realUrl.openConnection();
 	            conn.setRequestProperty("Accept", "*/*");
 	            conn.setRequestProperty("Connection", "Keep-Alive");
 	            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
 	            conn.setRequestProperty("Proxy-Connection", "keep-alive");
 	            conn.setRequestProperty("Referer", "http://gz.changjun.com.cn/index.aspx");
 	            conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
 	            conn.setRequestProperty("Charset", "UTF-8");   
  	            conn.setRequestProperty("Content-Type","text/html");
 	            conn.setRequestMethod("GET");
 	            conn.setDoInput(true);
  	            try {
 	            	 conn.connect();
 	            	 in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				} catch (Exception e) {
					 continue;
				}finally {
					 conn.disconnect();
				} 
  	          reUrl = conn.getURL().toString();
  	           if (reUrl.indexOf("pay.aspx") > 0 ) {
				   return true;
			   }else if(url.equals(reUrl.trim())){
				   return true;
			   }else {
				   return false;
			   }
		   }

        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    
        return false;
    }
	
	
}
