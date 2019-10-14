package com.talkweb.repairManage.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.api.message.utils.MessageNoticeModelEnum;
import com.talkweb.api.message.utils.MessageNoticeTypeEnum;
import com.talkweb.api.message.utils.MessageNoticeUserTypeEnum;
import com.talkweb.api.message.utils.MessageServiceEnum;
import com.talkweb.api.message.utils.MessageSmsEnum;
import com.talkweb.api.message.utils.MessageStatusEnum;
import com.talkweb.auth.service.AuthService;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.MotanService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.filter.util.HttpClientToken;
import com.talkweb.repairManage.service.RepairManageService;
import com.talkweb.utils.KafkaUtils;

import net.coobird.thumbnailator.Thumbnails;

/**
 * 设备报修
 */

@Controller
@RequestMapping(value="/repairManage")
public class RepairManageAction extends BaseAction{
	Logger logger = LoggerFactory.getLogger(RepairManageAction.class);
	
	@Autowired
	private RepairManageService repairManageService;

	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private AuthService authServiceImpl;
	
	@Autowired
	private FileServer fileServerImplFastDFS;
	
	@Value("#{settings['kafkaClientId']}")
	private String clientId;

	@Value("#{settings['kafkaClientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
	
	@Value("#{settings['repairmanage.msgUrlPc']}")
	private String msgUrlPc;
	
	@Value("#{settings['repairmanage.msgUrlApp']}")
	private String msgUrlApp;
	
 
	@Value("#{settings['baselocal.url']}")
 
	private String baseUrl;
	
	
	
	ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	
	/**
	 * 模板消息关联接口调用时，返回码
	 */
	private static final String ERRCODE_SUCCESS 			= "1";//正常显示
	private static final String ERRCODE_NO_PERMISSION	= "2";//当前身份没有这次数据  
	private static final String ERRCODE_DELETED 			= "3";//当前数据已删除!
	private static final String ERRCODE_EXPIRED 			= "4";//当前数据已结束
	private static final String ERRCODE_NOT_START 		= "5";//当前数据未开始   
	private static final String ERRCODE_ERROR 			= "-1";//其它错误
	
	/**
	 * 模板消息关联接口调用时，返回提示信息
	 */
	private static final String PROMPT_SUCCESS 			= "正常显示";
	private static final String PROMPT_NO_PERMISSION 	= "当前身份无法查看信息！";
	private static final String PROMPT_DELETED 			= "当前数据已删除!";
	private static final String PROMPT_EXPIRED 			= "此条数据已失效";
	private static final String PROMPT_NOT_START			= "还未到达开始时间，请在规定时间内查看！";
//	private static final String PROMPT_NO_DATA			= "暂时还没有相关数据哦！！";
	private static final String PROMPT_PARAM_ERROR 		= "参数错误,请联系管理员。";
	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	/**
	 * 获取登录人员的角色 0 管理员，1 审核人员，2 一般老师
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getRepairRole",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairRole(HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		JSONObject data = new JSONObject();
		int role = 2;
		param.put("schoolId", getXxdm(req));
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		List<String> selpersons = repairManageService.getSelPersons(param);
		int count = repairManageService.getRepairTypeCount(param);
		if(count>0){
			data.put("haveData", 1);//已经设置了维修类别
		}else{
			data.put("haveData", 0);//未设置维修类别
		}
		for(String s:selpersons){
			if(s.indexOf(accountId)>-1){
				role = 1;//审核人员
			}
		}
		boolean flag = isMoudleManager(req, "cs1019");
		if(flag){
			role = 0;
		}
		data.put("role", role);
		setPromptMessage(response, "0", "查询成功");
		response.put("data", data);
		logger.info("【getRepairRole】入参：schoolId:{} 出参角色role:{}",getXxdm(req),role);
		return response;
	}
	
	@RequestMapping(value = "/getRepairManageDetailPlus" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairManageDetailPlus(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) throws Exception{
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String userId = request.getString("userId");
		String schoolId = request.getString("schoolId");
		User user = commonDataService.getUserById(Long.parseLong(schoolId), Long.parseLong(userId));
		long accountId = user.getAccountPart().getId();
	
		param.put("accountId", accountId);
		param.put("schoolId", schoolId);
		String termInfoId = rbConstant.getString("currentTermInfo"); 
		
		List<JSONObject> data =new ArrayList<JSONObject>();
		data = repairManageService.getRepairInfoListPlus(param);
		
		JSONObject paramList = request;
		paramList.put("schoolId", schoolId);
		if(data.isEmpty() && data.size()==0){		// 用户不存在任何设备报修记录，则显示审批人页面
			List<JSONObject> list = repairManageService.getRepairTypeList(paramList);
			if(list!=null && !list.isEmpty() && list.size()>0){
				Map<String,String> mapList = null;
				for(JSONObject o:list){
					String checkPersonIdstr = o.getString("checkPersons");
					mapList = new HashMap<String,String>();
					if(checkPersonIdstr!=null && !"".equals(checkPersonIdstr)){
						String checkPersonId[] = checkPersonIdstr.split(",");
						for(String str:checkPersonId){
							Account account = commonDataService.getAccountById(Long.valueOf(schoolId), Long.valueOf(str), termInfoId);
							mapList.put(str, account.getName());
						}
					}
					String checkPersons = "";
					if (mapList!=null) {
						for (String key: mapList.keySet()) {
							String name = mapList.get(key);
							if (!StringUtils.isEmpty(name)) {
								checkPersons = checkPersons + name + ",";
							}
						}
					}
					if (checkPersons.endsWith(",")) {
						checkPersons = checkPersons.substring(0, checkPersons.length() - 1);
					}
					o.put("checkPersons", checkPersons);
				}
			}else{
				response.put("code", -1);
				response.put("data", null);
				response.put("msg", "系统未查询到你的审批人信息，赶紧联系管理员设置请假组吧！");
				return response;
			}
			response.put("code", 1);
			response.put("data", list);
			response.put("size", list.size());
			response.put("msg", "用户不存在任何设备报修记录，此时显示审批人页面");
			return response;
		}else{
			// 用户有设备报修信息：1、存在未完成设备报修流程则显示所有未完成流程；2、只有已完成设备报修流程则显示最近一条已完成流程。
			int flag = 0;								// 若flag值一直为0，则表示没有未完成的设备报修流程
			List<JSONObject> reslist = new ArrayList<JSONObject>();
			
			for(JSONObject obj : data){
				if(obj.getInteger("repairState")==0){		// status状态0表示未完成，1表示已完成
					flag = 1;
					reslist.add(obj);
				}else continue;
			}
			if(flag == 1){			// 存在未完成设备报修流程则显示所有未完成流程
				
				Map<String,String> mapList = null;
				for(JSONObject o:reslist){
					String checkPersonIdstr = o.getString("checkPersons");
					if(checkPersonIdstr!=null && !"".equals(checkPersonIdstr)){
						String checkPersonId[] = checkPersonIdstr.split(",");
						mapList = new HashMap<String,String>();
						for(String str:checkPersonId){
							Account account = commonDataService.getAccountById(Long.valueOf(schoolId), Long.valueOf(str), termInfoId);
							mapList.put(str , account.getName());
						}
					}
					String checkPersons = "";
					if (mapList!=null) {
						for (String key: mapList.keySet()) {
							String name = mapList.get(key);
							if (!StringUtils.isEmpty(name)) {
								checkPersons = checkPersons + name + ",";
							}
						}
					}
					if (checkPersons.endsWith(",")) {
						checkPersons = checkPersons.substring(0, checkPersons.length() - 1);
					}
					o.put("checkPersons", checkPersons);
				}
				
				response.put("code", 2);
				response.put("data", reslist);
				response.put("size", reslist.size());
				response.put("msg", "存在未完成设备报修流程,显示所有未完成流程");
				return response;
			}else if(flag == 0){	// 只有已完成设备报修流程则显示最近一条已完成流程
				Collections.sort(data,new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						return o2.getDate("askDate").compareTo(o1.getDate("askDate"));
					}
				});
				response.put("code", 3);
				List<JSONObject> list = new ArrayList<JSONObject>();
				JSONObject o = data.get(0);
				Map<String,String> mapList = null;

				String checkPersonIdstr = o.getString("checkPersons");
				if(checkPersonIdstr!=null && !"".equals(checkPersonIdstr)){
					String checkPersonId[] = checkPersonIdstr.split(",");
					mapList = new HashMap<String,String>();
					for(String str:checkPersonId){
						Account account = commonDataService.getAccountById(Long.valueOf(schoolId), Long.valueOf(str), termInfoId);
						mapList.put(str , account.getName());
					}
				}
				String checkPersons = "";
				if (mapList!=null) {
					for (String key: mapList.keySet()) {
						String name = mapList.get(key);
						if (!StringUtils.isEmpty(name)) {
							checkPersons = checkPersons + name + ",";
						}
					}
				}
				if (checkPersons.endsWith(",")) {
					checkPersons = checkPersons.substring(0, checkPersons.length() - 1);
				}
				o.put("checkPersons", checkPersons);
				list.add(o);
				response.put("data", list);
				response.put("msg", "只有已完成设备报修流程，显示最近一条已完成流程");
				return response;
			}
		}
		return response;
	}
	
	// 设备报修审批细节
	@RequestMapping(value = "/getRepairManageDetail" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairManageDetail(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) throws Exception{
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("accountId", accountId);
		
		List<JSONObject> data =new ArrayList<JSONObject>();
		data = repairManageService.getRepairInfoListPlus(param);
		
		for(JSONObject obj : data){
			String checkPersonIdstr = obj.getString("checkPersons");
			List<Long> checkPersonIds =StringUtil.toListFromString(checkPersonIdstr);
			List<String> checkPersonNames = new ArrayList<String>();
			String checkPersons = "";
			String[][] teacherNameMap = getTeacherIdNames(req,Long.valueOf(getXxdm(req)),checkPersonIds);
			if(CollectionUtils.isNotEmpty(checkPersonIds)){
				for(Long s:checkPersonIds){
					for(int j = 0;j<teacherNameMap[0].length;j++){
						if(teacherNameMap[0][j].equals(String.valueOf(s))){
							checkPersonNames.add(teacherNameMap[1][j]);
							break;
						}
					}
				}
			}
			if(checkPersonNames.size()>0){
				checkPersons = StringUtil.toStringBySeparator(checkPersonNames);
			}
			obj.put("checkPersonsName", checkPersons);
		}
		
		JSONObject paramList = request;
		paramList.put("schoolId", schoolId);
		if(data.isEmpty() && data.size()==0){		// 用户不存在任何设备报修记录，则显示审批人页面
			List<JSONObject> list = repairManageService.getRepairTypeList(paramList);
			if(list!=null && !list.isEmpty() && list.size()>0){
				for(JSONObject o:list){
					String checkPersonIdstr = o.getString("checkPersons");
					List<Long> checkPersonIds =StringUtil.toListFromString(checkPersonIdstr);
					List<String> checkPersonNames = new ArrayList<String>();
					String checkPersons = "";
					String[][] teacherNameMap = getTeacherIdNames(req,Long.valueOf(getXxdm(req)),checkPersonIds);
					
					if(CollectionUtils.isNotEmpty(checkPersonIds)){
						for(Long s:checkPersonIds){
							for(int j = 0;j<teacherNameMap[0].length;j++){
								if(teacherNameMap[0][j].equals(String.valueOf(s))){
									checkPersonNames.add(teacherNameMap[1][j]);
									break;
								}
							}
						}
					}
					if(checkPersonNames.size()>0){
						checkPersons = StringUtil.toStringBySeparator(checkPersonNames);
					}
					o.put("checkPersons", checkPersons);
				}
			}else{
				response.put("data", -1);
				response.put("data", null);
				response.put("msg", "系统未查询到你的审批人信息，赶紧联系管理员设置请假组吧！");
				return response;
			}
			response.put("data", 1);
			response.put("data", list);
			response.put("size", list.size());
			response.put("msg", "用户不存在任何设备报修记录，此时显示审批人页面");
			return response;
		}else{
			// 用户有设备报修信息：1、存在未完成设备报修流程则显示所有未完成流程；2、只有已完成设备报修流程则显示最近一条已完成流程。
			int flag = 0;								// 若flag值一直为0，则表示没有未完成的设备报修流程
			List<JSONObject> reslist = new ArrayList<JSONObject>();
			for(JSONObject obj : data){
				if(obj.getInteger("repairState")==0){		// status状态0表示未完成，1表示已完成
					flag = 1;
					reslist.add(obj);
				}else continue;
			}
			if(flag == 1){			// 存在未完成设备报修流程则显示所有未完成流程
				response.put("data", 2);
				response.put("data", reslist);
				response.put("size", reslist.size());
				response.put("msg", "存在未完成设备报修流程,显示所有未完成流程");
				return response;
			}else if(flag == 0){	// 只有已完成设备报修流程则显示最近一条已完成流程
				Collections.sort(data,new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						return o2.getDate("askDate").compareTo(o1.getDate("askDate"));
					}
				});
				response.put("data", 3);
				response.put("data", data.get(0));
				response.put("msg", "只有已完成设备报修流程，显示最近一条已完成流程");
				return response;
			}
		}
		return response;
	}
	
	/**
	 * 获取维修单列表信息
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getRepairInfoList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairInfoList(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		Date startDate = request.getDate("startDate");
		Date endDate = request.getDate("endDate");
		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		String role = request.getString("role");
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
//		String isEvaled = request.getString("isEvaled");
		String repairState = request.getString("repairState");
		String repairTypeId=request.getString("repairTypeId");
		String deviceName = request.getString("deviceName");
		if("2".equals(repairState)){
			repairState = null;
		}
		if(repairTypeId.isEmpty()){
			repairTypeId=null;
		}
		List<Long> accountIds = new ArrayList<Long>();
		param.put("startDate", startDate);
		param.put("endDate", cal.getTime());
		param.put("repairState",repairState);
		param.put("deviceName", deviceName);
		param.put("schoolId", getXxdm(req));
		param.put("accountId", accountId);
		param.put("repairTypeId", repairTypeId);
		
		try{
			List<JSONObject> data =new ArrayList<JSONObject>();
			if("1".equals(role)){
				data = repairManageService.getRepairInfoList(param);
			}else{
				data = repairManageService.getAllRepairInfoList(param);
			}
			for(JSONObject json:data){
				Long personId = json.getLong("askPerson");
				if(personId == null){
					continue;
				}
				//repairIds.add(json.getString("repairId"));
				accountIds.add(personId);
			}
			
			//param.put("repairIds", repairIds);
			
			//List<JSONObject> persondata=repairManageService.getRepairPersonByIds(param);
//			for(JSONObject json:persondata){
//				permap.put(json.getString("repairId"), json);
//			}
			//String[][] teacherIdNames = getTeacherIdNames(req,Long.valueOf(getXxdm(req)),accountIds);
			
			List<Account> ll = commonDataService.getAccountBatch(Long.valueOf(getXxdm(req)), accountIds,getCurXnxq(req));
			
			HashMap<String, Account> accmap=new HashMap<String, Account>();
			for(Account a:ll){
				accmap.put(a.getId()+"", a);
			}
			
			for(JSONObject o:data){
//				String phone="";
//				if(permap.containsKey(o.getString("repairId"))){
//					phone=permap.get(o.getString("repairId")).getString("phoneNum");
//				}
//				o.put("phone", phone);
				String personId = o.getString("askPerson");
				String isCheckconfig = o.getString("isCheckconfig");
				if(StringUtils.isBlank(isCheckconfig)){
					o.put("isCheckconfig", 0);
				}else{
					o.put("isCheckconfig", 1);
				}
			//	int index = indexInArray(personId, teacherIdNames[0]);
				String name=accmap.containsKey(personId)?accmap.get(personId).getName():"";
				o.put("askPerson",name);
				
				if(personId.equals(accountId)){
					if(!StringUtils.isBlank(isCheckconfig)&&isCheckconfig.indexOf(accountId)>=0){
						o.put("isOwn", 2);
					}else{
						o.put("isOwn", 1);
					}
				}else{
					o.put("isOwn", 0);
				}
			}
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		}catch(Exception e){
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
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
	 * 获取自己填报的维修单列表
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getOwnRepairInfoList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getOwnRepairInfoList(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		Date startDate = request.getDate("startDate");
		Date endDate = request.getDate("endDate");
		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		String repairState = request.getString("repairState");
		String deviceName = request.getString("deviceName");
		if("2".equals(repairState)){
			repairState = null;
		}
		param.put("startDate", startDate);
		param.put("endDate", cal.getTime());
		param.put("repairState",repairState);
		param.put("deviceName", deviceName);
		param.put("schoolId", getXxdm(req));
		param.put("accountId", String.valueOf((long)req.getSession().getAttribute("accountId")));
		try{
			List<JSONObject> data = repairManageService.getOwnRepairInfoList(param);
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		}catch(Exception e){
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 删除维修单
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/deleteRepairInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteRepairInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		param.put("repairId", repairId);
		try {
			repairManageService.deleteRepairInfo(param);
			setPromptMessage(response, "0", "删除成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "删除失败");
			e.printStackTrace();
		}
		
		return response;
	}
	/**
	 * 增加维修反馈
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/addFeedbackInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addFeedbackInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		String feedbackInfo = request.getString("feedbackInfo");
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		String role = request.getString("role");
		
		if("0".equals(role)){
			param.put("personType", "管理员");
		}else if("1".equals(role)){
			param.put("personType", "审核人员");
		}else{
			param.put("personType", "申报人");
		}
		param.put("feedbackPerson", accountId);
		param.put("repairId", repairId);
		param.put("feedbackTime", new Date());
		param.put("feedbackInfo", feedbackInfo);
		try {
			repairManageService.addFeedbackInfo(param);
			setPromptMessage(response, "0", "添加成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "添加失败");
			e.printStackTrace();
		}
		
		return response;
	}
	/**
	 * 添加维修评价
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/addRepairEvalInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addRepairEvalInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		int isEvaled = request.getIntValue("isEvaled");
		param.put("repairId", repairId);
		param.put("isEvaled", isEvaled);
		param.put("repairState", 1);
		try {
			repairManageService.addRepairEvalInfo(param);
			
		 
			String schoolId =  getXxdm(req);
			String termInfo = getCurXnxq(req);
			JSONObject repair = repairManageService.getRepairInfo(request); 
			Long  accountId = repair.getLong("askPerson");
			School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
			
			List<Long> accountIds = new ArrayList<Long>();
			accountIds.add(accountId);
			List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfo);
			Account account =accounts.get(0);
			//JSONObject reparePerson = repairManageService.getAPPRepairDetail(param);
			//JSONArray repairPersons =  reparePerson.getJSONArray("repairPersons");


			List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
			JSONObject msgCenterReceiver = new JSONObject();
			msgCenterReceiver.put("userId", account.getExtId());
			msgCenterReceiver.put("userName", account.getName());
			msgCenterReceiversArray.add(msgCenterReceiver);

			JSONObject msgCenterPayLoad = new JSONObject();
			JSONObject msg = new JSONObject();
			String msgId = UUIDUtil.getUUID().replace("-", "");
			msg.put("msgId", msgId);
			msg.put("msgTitle", "您提交的设备报修单有了新进度");
			msg.put("msgContent", account.getName() + "您提交的设备报修单有了新进度！!");
			msg.put("msgUrlPc", ""+repairId);
			msg.put("msgUrlApp", ""+repairId);
			msg.put("msgOrigin", "设备报修");
			msg.put("msgTypeCode", "SBBX");
			msg.put("msgTemplateType", "SBBX");
			msg.put("schoolId", school.getExtId());
			Long accountId2 = (Long)req.getSession().getAttribute("accountId") ;
			Account account2 = commonDataService.getAccountById(Long.parseLong(schoolId), accountId2, termInfo);
			msg.put("creatorName", account2.getName());

			JSONObject first = new JSONObject();
			first.put("value", "您提交的设备报修单已经维修完成");

			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", account.getName());
			
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", repair.getString("askPhone"));

			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", repair.getString("repairAddr"));

			JSONObject keyword4 = new JSONObject();
			keyword4.put("value", repair.getString("deviceName") +"【" + repair.getString("bugDisplay") + "】");

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			JSONObject keyword5 = new JSONObject();
			keyword5.put("value", format.format(repair.getDate("askDate")));
 
			JSONObject remark = new JSONObject();
			remark.put("value", "请及时查看。");
			
			JSONObject data = new JSONObject();
			data.put("first", first);
			data.put("keyword1", keyword1);
			data.put("keyword2", keyword2);
			data.put("keyword3", keyword3);
			data.put("keyword4", keyword4);
			data.put("keyword5", keyword5);
			data.put("remark",   remark);
			msg.put("msgWxJson", data);
			msgCenterPayLoad.put("msg", msg);
			msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
			logger.info("msgCenterPayLoad=====>" + msgCenterPayLoad.toString());
//			try {
//				SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
//		        sp.start();     
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			setPromptMessage(response, "0", "添加成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "添加失败");
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * 更新维修状态(审核完成)
	 */
	@RequestMapping(value="/updateRepairEvalState",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateRepairEvalState(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		int repairState = request.getIntValue("repairState");
		param.put("repairId", repairId);
		param.put("repairState", repairState);
		try {
			repairManageService.updateRepairEvalState(param);
			setPromptMessage(response, "0", "更新成功");
			if (repairState == 1) {// 1表示已维修
				String schoolId =  getXxdm(req);
				String termInfo = getCurXnxq(req);
				JSONObject repairDetail = repairManageService.getRepairInfo(request);
				if(repairDetail == null) {
					setPromptMessage(response, "-1", "维修信息为空！");
					return response;
				}
				Long  accountId = repairDetail.getLong("askPerson");
				School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
				
				List<Long> accountIds = new ArrayList<Long>();
				accountIds.add(accountId);
				List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfo);
				Account account =accounts.get(0);
				//JSONObject reparePerson = repairManageService.getAPPRepairDetail(param);
				//JSONArray repairPersons =  reparePerson.getJSONArray("repairPersons");


				List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
				JSONObject msgCenterReceiver = new JSONObject();
				msgCenterReceiver.put("userId", account.getExtId());
				msgCenterReceiver.put("userName", account.getName());
				msgCenterReceiversArray.add(msgCenterReceiver);

				JSONObject msgCenterPayLoad = new JSONObject();
				JSONObject msg = new JSONObject();
				String msgId = UUIDUtil.getUUID().replace("-", "");
				msg.put("msgId", msgId);
				msg.put("msgTitle", "您提交的设备报修单有了新进度");
				msg.put("msgContent", account.getName() + "您提交的设备报修单有了新进度！!");
				msg.put("msgUrlPc", ""+msgUrlPc);
				msg.put("msgUrlApp", ""+msgUrlApp.replace("#id#", repairId).replace("#type#", "1"));
				msg.put("msgOrigin", "设备报修");
				msg.put("msgTypeCode", "SBBX");
				msg.put("msgTemplateType", "SBBX");
				msg.put("schoolId", school.getExtId());
				Long accountId2 = (Long)req.getSession().getAttribute("accountId") ;
				Account account2 = commonDataService.getAccountById(Long.parseLong(schoolId), accountId2, termInfo);
				msg.put("creatorName", account2.getName());

				JSONObject first = new JSONObject();
				first.put("value", "您提交的设备报修单已经维修完成");

				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", account.getName());
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", repairDetail.getString("askPhone"));

				JSONObject keyword3 = new JSONObject();
				keyword3.put("value", repairDetail.getString("repairAddr"));
 
				JSONObject keyword4 = new JSONObject();
				keyword4.put("value", repairDetail.getString("deviceName") +"【" + repairDetail.getString("bugDisplay") + "】");
 
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				JSONObject keyword5 = new JSONObject();
				keyword5.put("value", format.format(repairDetail.getDate("askDate")));
	 
				JSONObject remark = new JSONObject();
				remark.put("value", "请及时查看。");
				
				JSONObject data = new JSONObject();
				data.put("first", first);
				data.put("keyword1", keyword1);
				data.put("keyword2", keyword2);
				data.put("keyword3", keyword3);
				data.put("keyword4", keyword4);
				data.put("keyword5", keyword5);
				data.put("remark",   remark);
				data.put("url", msg.get("msgUrlApp"));
				msg.put("msgWxJson", data);
				msgCenterPayLoad.put("msg", msg);
				msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
				logger.info("msgCenterPayLoad=====>" + msgCenterPayLoad.toString());
				sendAppMsg(msgId,msgCenterPayLoad);
//				try {
//					SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
//			        sp.start();     
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "更新失败");
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * 获取一个维修单所有的反馈信息
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getFeedbackInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getFeedbackInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		Long schoolId = Long.valueOf(getXxdm(req));
		String repairId = request.getString("repairId");
		param.put("repairId", repairId);
		try {
			List<JSONObject> data = repairManageService.getFeedbackInfo(param);
			List<Long> accountIds = new ArrayList<Long>();
			for(JSONObject o:data){
				Long accountId = o.getLong("feedbackPerson");
				accountIds.add(accountId);
			}
			Map<String,String> teacherIdName = getTeacherIdNameMap(req,schoolId, accountIds);
			for(JSONObject json:data){
				String feedbackPerson = json.getString("feedbackPerson");
				String feedbackPersonName = teacherIdName.get(feedbackPerson);
				String personType = json.getString("personType");
				json.put("feedbackPerson", personType+": "+feedbackPersonName);
			}
			response.put("data", data);
			setPromptMessage(response, "0", "查询成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		
		return response;
	}
	/**
	 * 获取申请维修人员信息
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getRepairAskpersonInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairAskpersonInfo(HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject data = new JSONObject();
		User user = (User) req.getSession().getAttribute("user");
		if(user!=null){
			String askPerson = user.getAccountPart().getName();
			String askPhone = user.getAccountPart().getMobilePhone();
			//user.getUserPart().getOrgIds();
			data.put("askPerson", askPerson);
			data.put("askPhone", askPhone);
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		}else{
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	/**
	 * 新增维修单填报(审核中)
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/addRepairInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addRepairInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		
		logger.info("addRepairInfo=> request: " + request.toString());
		User user = (User)req.getSession().getAttribute("user");
		JSONObject param = request;
		String repairId =UUIDUtil.getUUID();
		Long accountId = user.getAccountPart().getId();
		param.put("repairId", repairId);
		param.put("askPerson", accountId);
		param.put("askDate", new Date());
		param.put("schoolId", getXxdm(req));
		String repairTypeId = param.getString("repairTypeId");
		String isCheckPerson = repairManageService.getIsCheckPerson(repairTypeId);
		String askPhone =  request.getString("askPhone");
		String repairAddr = request.getString("repairAddr");
		String bugDisplay = request.getString("bugDisplay");
		String deviceName = request.getString("deviceName");
		try {
			repairManageService.addRepairInfo(param);
			setPromptMessage(response, "0", "添加维修单成功");
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "添加维修单失败");
		}
		
		String schoolId =  getXxdm(req);
		String termInfo = getCurXnxq(req);
		//申请人
		Account account = commonDataService.getAccountAllById(Long.parseLong(schoolId), accountId, termInfo);
		
		School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
		List<Long> accountIds = new ArrayList<Long>();
		//接收人account列表
		List<Account> accounts;
		//消息接收者列表
		List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
		if(StringUtils.isBlank(isCheckPerson)){
			//没有设置审核人，直接发送短信
			 // 发送微信给 模块 管理员 ？？？
			/**
			 * 如果无需审核的申请单，直接消息推送给维修人员
			 */
			//获取维修人员
			JSONObject result = repairManageService.getRepairDetail(param);
			List<String> repairPersons = (List<String>) result.get("repairPersons");
			if(repairPersons != null && repairPersons.size() > 0) {
				param.put("repairTypeId", repairTypeId);
				List<JSONObject> repairPersonInfos = repairManageService.getRepairPersonInfo(param);
				if(repairPersonInfos != null && repairPersonInfos.size() > 0) {
					
				}
			}
		}else{
			String[] split = isCheckPerson.split(",");
			for (String string : split) {
				if(StringUtils.isNotBlank(string)){
					accountIds.add(Long.valueOf(string));  //拿 extid
				}
			}
			accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfo);
			
			for (int i = 0; i < accounts.size(); i++) {
				Account account2 = accounts.get(i);
				JSONObject msgCenterReceiver = new JSONObject();
				msgCenterReceiver.put("userId", account2.getExtId());
				msgCenterReceiver.put("userName", account2.getName());
				msgCenterReceiversArray.add(msgCenterReceiver);
			}
		}
		
		if(msgCenterReceiversArray.size() > 0) {
			JSONObject msgCenterPayLoad = new JSONObject();
			JSONObject msg = new JSONObject();
			String msgId = UUIDUtil.getUUID().replace("-", "");
			msg.put("msgId", msgId);
			msg.put("msgTitle", "您有一条新设备报修审核单");
			msg.put("msgContent", "您有一条新设备报修审核单");
	
			msg.put("msgUrlPc", ""+msgUrlPc);
			msg.put("msgUrlApp", ""+msgUrlApp.replace("#id#", repairId).replace("#type#", "0"));
			msg.put("msgOrigin", "设备报修");
			msg.put("msgTypeCode", "SBBX");
			msg.put("msgTemplateType", "SBBX");
			msg.put("schoolId", school.getExtId());
			msg.put("creatorName", account.getName());
	
			JSONObject first = new JSONObject();
			first.put("value", "您有一条新设备报修审核单");
	
			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", account.getName());
			
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", askPhone);
	
	
			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", repairAddr);
			
			
			JSONObject keyword4 = new JSONObject();
			keyword4.put("value", deviceName +"【" + bugDisplay + "】");
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			JSONObject keyword5 = new JSONObject();
			keyword5.put("value", format.format(new Date()));
	
			JSONObject remark = new JSONObject();
			remark.put("value", "请及时审核。");
			
			JSONObject data = new JSONObject();
			data.put("first", first);
			data.put("keyword1", keyword1);
			data.put("keyword2", keyword2);
			data.put("keyword3", keyword3);
			data.put("keyword4", keyword4);
			data.put("keyword5", keyword5);
			data.put("remark", remark);
			data.put("url", msg.get("msgUrlApp"));
			msg.put("msgWxJson", data);
			msgCenterPayLoad.put("msg", msg);
			msgCenterPayLoad.put("receivers",msgCenterReceiversArray);
			sendAppMsg(msgId,msgCenterPayLoad);
	//		try {
	//			SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
	//	        sp.start();     
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
			
			
			//设置了审核人，发送通知给审核人
			
			
			//        下面的 注释掉， 没有再用MotanService 服务 
			/*
			try {
				List<Long> accountIds = new ArrayList<Long>();
				String[] split = isCheckPerson.split(",");
				for (String string : split) {
					if(StringUtils.isNotBlank(string)){
						accountIds.add(Long.valueOf(string));
					}
				}
				List<String> ids = new ArrayList<String>();
				List<Account> accountBatch = commonDataService.getAccountBatch(Long.valueOf(getXxdm(req)), accountIds, commonDataService.getCurrentXnxq(getSchool(req,null)));
				if(CollectionUtils.isNotEmpty(accountBatch) && accountBatch.size() > 0){
					for (Account account : accountBatch) {
						List<User> users = account.getUsers();
						if(CollectionUtils.isNotEmpty(users)){
							for (User user2 : users) {
								if(user2.getUserPart().getRole().getValue() == 1){
									long accountId = user2.getAccountPart().getId();
									long userId = user2.getUserPart().getId();
									ids.add(accountId + ":" + userId);
								}
							}
						}
					}
				}
				sendMsg(getXxdm(req),ids,String.valueOf(user.getUserPart().getId()),repairTypeId,MessageNoticeModelEnum.DEFAULT,null);
			} catch (Exception e) {
				e.printStackTrace();
			}*/
		}
		return response;
	}
	/**
	 * 获取维修类别信息
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getRepairTypeInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairTypeInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String schoolId = getXxdm(req);
//		String schoolId = "10006";
		param.put("schoolId", schoolId);
		
		String isAll=request.getString("isAll")==null?"1":request.getString("isAll");
		try {
			List<JSONObject> data = repairManageService.getRepairTypeInfo(param);
			for(JSONObject json:data){
				String repairType = json.getString("repairType");
				String repairDetail = json.getString("repairDetail");
				if(isAll.equals("0")){
					json.put("repairType", repairType);
				}else{
					json.put("repairType", repairType+"("+repairDetail+")");
				}
				
			}
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	/**
	 * 获取维修人员信息
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getRepairPersonInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairPersonInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairTypeId = request.getString("repairTypeId");
		param.put("repairTypeId", repairTypeId);
		param.put("schoolId", getXxdm(req));
//		param.put("schoolId", "10006");
		try {
			List<JSONObject> data = repairManageService.getRepairPersonInfo(param);
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	/**
	 * 维修单统计列表
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getRepairStatistics",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairStatistics(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String schoolId = this.getXxdm(req);
		Long teacherId = (Long) req.getSession().getAttribute("accountId");
		param.put("schoolId", schoolId);
		Date startDate = request.getDate("startDate");
		Date endDate = request.getDate("endDate");
		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		param.put("startDate", startDate);
		param.put("endDate", cal.getTime());
		param.put("teacherId", teacherId);
		param.put("role", request.getString("role"));
		List<Long> accountIds = new ArrayList<Long>();
		try {
			List<JSONObject> data = repairManageService.getRepairStatistics(param);
			for(JSONObject json:data){
				Long personId = json.getLong("askPerson");
				if(personId == null){
					continue;
				}
				//repairIds.add(json.getString("repairId"));
				accountIds.add(personId);
			}
			
			
			List<Account> ll = commonDataService.getAccountBatch(Long.valueOf(getXxdm(req)), accountIds,getCurXnxq(req));
			
			HashMap<String, Account> accmap=new HashMap<String, Account>();
			for(Account a:ll){
				accmap.put(a.getId()+"", a);
			}
			
			for(JSONObject o:data){
				String personId = o.getString("askPerson");
				String name=accmap.containsKey(personId)?accmap.get(personId).getName():"";
				o.put("askPerson",name);
			}
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	/**
	 * 获取维修单详情
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getRepairDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairDetail(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		String schoolId = this.getXxdm(req);
//		String schoolId = "10006";
		param.put("repairId", repairId);
		param.put("schoolId", schoolId);
		try {
			List<Long> accountIds = new ArrayList<Long>();
			JSONObject data = repairManageService.getRepairDetail(param);
			String personId = data.getString("askPerson");
			if(personId!=null&&!personId.isEmpty()){
				accountIds.add(Long.valueOf(personId));
			}
			//String[][] teacherIdNames = getTeacherIdNames(req,Long.valueOf(schoolId),accountIds);
			//int index = indexInArray(personId, teacherIdNames[0]);
			List<Account> ll = commonDataService.getAccountBatch(Long.valueOf(getXxdm(req)), accountIds,getCurXnxq(req));
			
			HashMap<String, Account> accmap=new HashMap<String, Account>();
			for(Account a:ll){
				accmap.put(a.getId()+"", a);
			}
			String name=accmap.containsKey(personId)?accmap.get(personId).getName():"";
			data.put("askPerson", name);
			data.put("isCheckconfig", request.getString("isCheckconfig"));
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	/**
	 * 更新维修单信息（）
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/updateRepairInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateRepairInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String isCheckconfig = param.getString("isCheckconfig");
		String role = param.getString("role");
		String repairId = request.getString("repairId");
		try {
			if("1".equals(isCheckconfig)){
				User user = (User) req.getSession().getAttribute("user");
				String checkPerson = user.getAccountPart().getName();
				String accountId = String.valueOf(user.getAccountPart().getId());
				long userId = user.getUserPart().getId();
				String schoolId = getXxdm(req);
				param.put("isCheck", 1);
				param.put("checkPerson", checkPerson);
				String repairTypeId = param.getString("repairTypeId");
				String checkpsons = repairManageService.getPersonsByRepairId(param);
				String deviceName = param.getString("deviceName");
				logger.info("checkpsons==>" + checkpsons + "accountId===>" + accountId + "role===" + role);
				if(checkpsons.indexOf(accountId)>-1||"0".equals(role)){
					//需要审核，保存审核人，发送短信
					int updateCheckNum = repairManageService.updateCheckRepairInfo(param);
					//发送短信
					/*jsonObject {
						 * noticeServiceType:通知业务类型
						 * noticeDate:通知时间
						 * schoolId:学校代码
						 * userId:创建用户
						 * noticeType:通知类型
						 * needConfirm:是否需要确认
						 * noticeUserType:通知用户类型
						 * noticeModel:通知模型
						 * noticeStartus:通知状态
						 * noticeSendDate:通知发送时间
						 * serviceId:业务轮次代码
						 * sendMsgStartus:短信开关
						 * noticeDetails:[
						 * 	  {
						 * 		noticeDetailsContent：通知消息体，
						 * 		userId：用户代码，
						 * 		classId：班级代码，
						 * 		userType：用户类型，
						 * 		schoolId：学校代码，
						 * 		isStartus：是否有效,
						 * 		family:父母[accountId:userId,accountId:userId]
						 *    }
						 * ] 通知详细情况
						 * }*/
					
					
					List<String>ids = new ArrayList<String>();
					ids.add(accountId+":"+userId);
					String termInfo = getCurXnxq(req);
					School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
					JSONObject repair = repairManageService.getRepairInfo(request); 
					Long askPerson = repair.getLong("askPerson");
					List<Long> accountIds = new ArrayList<Long>();
					accountIds.add(askPerson);
					List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfo);
					Account account = accounts.get(0);
					List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
					JSONObject msgCenterReceiver = new JSONObject();
					msgCenterReceiver.put("userId", account.getExtId());
					msgCenterReceiver.put("userName", account.getName());
					msgCenterReceiversArray.add(msgCenterReceiver);
					
					//JSONObject persons = repairManageService.getAPPRepairDetail(request);
					//JSONArray repairPersons = persons.getJSONArray("repairPersons");


					JSONObject msgCenterPayLoad = new JSONObject();
					JSONObject msg = new JSONObject();
				    String msgId = UUIDUtil.getUUID().replace("-", "");
					msg.put("msgId", msgId);
					msg.put("msgTitle", "您提交的设备报修单有了新进度");
					msg.put("msgContent", account.getName() + "您提交的设备报修单有了新进度！!");
					msg.put("msgUrlPc", "" + msgUrlPc);
					msg.put("msgUrlApp", msgUrlApp.replace("#id#", repairId).replace("#type#", "1"));
					msg.put("msgOrigin", "设备报修");
					msg.put("msgTypeCode", "SBBX");
					msg.put("msgTemplateType", "SBBX");
					msg.put("schoolId", school.getExtId());
					
					Account account2 = commonDataService.getAccountById(Long.parseLong(schoolId), Long.parseLong(accountId), termInfo);
					msg.put("creatorName", account2.getName());

					JSONObject first = new JSONObject();
					first.put("value", "您提交的设备报修单有了新进度");

					JSONObject keyword1 = new JSONObject();
					keyword1.put("value", account.getName());
					
					JSONObject keyword2 = new JSONObject();
					keyword2.put("value", repair.getString("askPhone"));

					JSONObject keyword3 = new JSONObject();
					keyword3.put("value", repair.getString("repairAddr"));
	 
					JSONObject keyword4 = new JSONObject();
					keyword4.put("value",repair.getString("deviceName") +"【" + repair.getString("bugDisplay") + "】");
	                
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					JSONObject keyword5 = new JSONObject();
					keyword5.put("value", format.format(repair.getDate("askDate")));
		 
					JSONObject remark = new JSONObject();
					remark.put("value", "请及时查看。");
					
					JSONObject data = new JSONObject();
					data.put("first", first);
					data.put("keyword1", keyword1);
					data.put("keyword2", keyword2);
					data.put("keyword3", keyword3);
					data.put("keyword4", keyword4);
					data.put("keyword5", keyword5);
					data.put("remark", remark);
					data.put("url", msg.get("msgUrlApp"));
					msg.put("msgWxJson", data);
					
					msgCenterPayLoad.put("msg", msg);
					msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
					logger.info("msgCenterPayLoad======>" + msgCenterPayLoad);
					sendAppMsg(msgId,msgCenterPayLoad);
//					try {
//						SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
//					        sp.start();     
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
 
					logger.info("+++++++++++++++ 发送审核通过通知  ++++++++++");
					String result = sendMsg(schoolId, ids, null, repairTypeId, MessageNoticeModelEnum.DEFAULT, deviceName);
					logger.info("+++++++++++++++ 发送审核通过通知结果：="+ result +" ++++++++++");
				}
					//返回数据
				setPromptMessage(response, "0", "添加维修单成功");
			}else{
				repairManageService.updateRepairInfo(param);
				setPromptMessage(response, "0", "添加维修单成功");
			}
		 
		} catch (Exception e) {
			setPromptMessage(response, "-1", "添加维修单失败");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 获取维修类别列表
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getRepairTypeList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRepairTypeList(HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		param.put("schoolId", getXxdm(req));
		List<JSONObject> data = repairManageService.getRepairTypeList(param);
		for(JSONObject o:data){
			String checkPersonIdstr = o.getString("checkPersons");
			List<Long> checkPersonIds =StringUtil.toListFromString(checkPersonIdstr);
			List<String> checkPersonNames = new ArrayList<String>();
			String checkPersons = "";
			String[][] teacherNameMap = getTeacherIdNames(req,Long.valueOf(getXxdm(req)),checkPersonIds);
			
			if(CollectionUtils.isNotEmpty(checkPersonIds)){
				for(Long s:checkPersonIds){
					for(int j = 0;j<teacherNameMap[0].length;j++){
						if(teacherNameMap[0][j].equals(String.valueOf(s))){
							checkPersonNames.add(teacherNameMap[1][j]);
							break;
						}
					}
				}
			}
			if(checkPersonNames.size()>0){
				checkPersons = StringUtil.toStringBySeparator(checkPersonNames);
			}
			o.put("checkPersons", checkPersons);
		}
		if(data.size()>0){
			response.put("data", data);
			setPromptMessage(response, "0", "查询成功");
		}else{
			setPromptMessage(response, "-1", "未查询到相关数据");
		}
		return response;
	}
	/**
	 * 删除维修类别信息
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/deleteRepairTypeInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteRepairTypeInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairTypeId");
		param.put("repairTypeId", repairId);
		try {
			repairManageService.deleteRepairTypeInfo(param);
			setPromptMessage(response, "0", "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "删除失败");
		}
		
		return response;
	}
	/**
	 * 获取维修类别信息
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getEditRepairType",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getEditRepairType(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		param.put("repairTypeId", request.getString("repairTypeId"));
		JSONObject data = repairManageService.getEditRepairType(param);
		if(data.size()>0){
			response.put("data", data);
			setPromptMessage(response, "0", "查询成功");
		}else{
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	/**
	 * 获取审核人员列表
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getCheckPersonList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCheckPersonList(HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		School school = this.getSchool(req,null);
		List<JSONObject> data = getTeacherNameMap(req,school);
		if(data.size()>0){
			response.put("data", data);
			setPromptMessage(response, "0", "查询成功");
		}else{
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	
	public List<JSONObject> getTeacherNameMap(HttpServletRequest req,School school){
		List<Account> ll = commonDataService.getAllSchoolEmployees(school,getCurXnxq(req),"");
		List<JSONObject> teacherIdName = new ArrayList<JSONObject>();
		for(int i = 0;i<ll.size();i++){
			JSONObject o = new JSONObject();
			String teacherId = String.valueOf(ll.get(i).getId());
			String teacherName = ll.get(i).getName();
			o.put("teacherId", teacherId);
			o.put("teacherName", teacherName);
			teacherIdName.add(o);
		}
		return teacherIdName;
	}
	
	public Map<String,String> getTeacherIdNameMap(HttpServletRequest req,Long schoolId,List<Long> accountIds){
		List<Account> ll = commonDataService.getAccountBatch(schoolId, accountIds,getCurXnxq(req));
		Map<String,String>  teacherIdName = new HashMap<String,String> ();
		for(int i = 0;i<ll.size();i++){
			String teacherId = String.valueOf(ll.get(i).getId());
			String teacherName = ll.get(i).getName();
			teacherIdName.put(teacherId, teacherName);
		}
		return teacherIdName;
	}
	public Map<String,String> getAllTeacherNameMap(HttpServletRequest req,School school){
		List<Account> ll = commonDataService.getAllSchoolEmployees(school,getCurXnxq(req),"");
		Map<String,String>  teacherIdName = new HashMap<String,String> ();
		for(int i = 0;i<ll.size();i++){
			String teacherId = String.valueOf(ll.get(i).getId());
			String teacherName = ll.get(i).getName();
			teacherIdName.put(teacherId, teacherName);
		}
		return teacherIdName;
	}
	public String[][] getTeacherIdNames(HttpServletRequest req,Long schoolId,List<Long> accountIds){
		List<Account> ll = commonDataService.getAccountBatch(schoolId, accountIds,getCurXnxq(req));
		String[][] teacherIdName = new String[2][ll.size()];
		for(int i = 0;i<ll.size();i++){
			String teacherId = String.valueOf(ll.get(i).getId());
			String teacherName = ll.get(i).getName();
			teacherIdName[0][i] = teacherId;
			teacherIdName[1][i] = teacherName;
		}
		return teacherIdName;
	}
	/**
	 * 新增维修类别信息
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/addRepairTypeInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addRepairTypeInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		param.put("schoolId", getXxdm(req));
//		param.put("schoolId", "10006");
		param.put("repairTypeId", UUID.randomUUID().toString());
		param.put("repairDate", new Date());
		try {
			repairManageService.addRepairTypeInfo(param);
			setPromptMessage(response, "0", "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "添加失败");
		}
		return response;
	}
	/**
	 * 更新维修类别信息
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/updateRepairTypeInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateRepairTypeInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		try {
			param.put("schoolId", getXxdm(req));
			repairManageService.updateRepairTypeInfo(param);
			setPromptMessage(response, "0", "更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "更新失败");
		}
		return response;
	}
	
	/***************************************APP接口******************************************/
	@RequestMapping(value="/getAPPRepairInfoList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPRepairInfoList(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String schoolId = request.getString("schoolId");
		Long userId = request.getLong("userId");
		String selectedSemester = commonDataService.getCurTermInfoId(Long.parseLong(schoolId));
		User user = commonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
		String teacherId ="";
		if(user!=null && user.getAccountPart() != null){
			teacherId = String.valueOf(user.getAccountPart().getId());
		}
		param.put("schoolId", schoolId);
		param.put("teacherId", teacherId);
		try {
			List<JSONObject> rdata = repairManageService.getAPPRepairInfoList(param);
			List<JSONObject> data = new ArrayList<>();
			List<Long> askPersonList = new ArrayList<Long>();
			for(JSONObject obj:rdata){
				String askPerson = obj.getString("askPerson");
				askPersonList.add(obj.getLong("askPerson"));
				if(teacherId.equals(askPerson)){
					String isCheckconfig=obj.getString("isCheckconfig");
					if(StringUtils.isBlank(isCheckconfig)){
						obj.put("isCheck", 1);
					}
				   data.add(obj);
				}
			}
			List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), askPersonList, selectedSemester);
			Map<Long, String> accountMap = new HashMap<Long, String>();
			for (int i = 0; i < accounts.size(); i++) {
				Account account = accounts.get(i);
				accountMap.put(account.getId(), account.getName());
			}
			for(JSONObject obj:rdata){
				obj.put("askPersonName", accountMap.get(obj.getLong("askPerson")));
			}
			
			
			response.put("data", data);
			setPromptMessage(response, "0", "更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "更新失败");
		}
		return response;
	}
	
	/**
	 * 报修申报
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/addAPPRepairInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addAPPRepairInfo(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String schoolId = request.getString("schoolId");
		Long userId = request.getLong("userId");
		String selectedSemester = commonDataService.getCurTermInfoId(Long.parseLong(schoolId));
		User user = commonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
		String teacherId ="";
		if(user!=null && user.getAccountPart() != null){
			teacherId = String.valueOf(user.getAccountPart().getId());
		} else {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}
		String deviceName = request.getString("deviceName");
		String repairAddr = request.getString("repairAddr");
		String repairTypeId = request.getString("repairTypeId");
		JSONArray repairPersons = request.getJSONArray("repairPersons");
		String bugDisplay = request.getString("bugDisplay");
		String askDepartment = request.getString("askDepartment");
		String askPhone = request.getString("askPhone");
		JSONArray attachmentIds = request.getJSONArray("attachmentIds");
		String repairId = UUIDUtil.getUUID();
		param.put("repairId", repairId);
		param.put("deviceName", deviceName);
		param.put("repairAddr", repairAddr);
		param.put("repairTypeId", repairTypeId);
		param.put("bugDisplay", bugDisplay);
		param.put("askDate", new Date());
		param.put("askDepartment", askDepartment);
		param.put("askPerson", teacherId);
		param.put("askPhone", askPhone);
		param.put("repairPersons", repairPersons);
		param.put("schoolId", schoolId);
		param.put("isSendMsg", 1);
		param.put("attachmentIds", attachmentIds);
		try {
			repairManageService.addRepairInfo(param);
			setPromptMessage(response, "0", "添加成功");
			response.put("repairId", repairId);
			
			String isCheckPerson = repairManageService.getIsCheckPerson(repairTypeId);
			if(StringUtils.isBlank(isCheckPerson)){
				 logger.info("addAPPRepairInfo => 没有审核人");
			}else{
				List<Long> accountIds = new ArrayList<Long>();
				String[] split = isCheckPerson.split(",");
				for (String string : split) {
					if(StringUtils.isNotBlank(string)){
						accountIds.add(Long.valueOf(string));  //拿 extid
					}
				}
 
				School school = commonDataService.getSchoolById(Long.parseLong(schoolId), selectedSemester);
				List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, selectedSemester);
				Account account = commonDataService.getAccountAllById(Long.parseLong(schoolId), Long.parseLong(teacherId), selectedSemester);
				List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
				for (int i = 0; i < accounts.size(); i++) {
					Account account2 = accounts.get(i);
					JSONObject msgCenterReceiver = new JSONObject();
					msgCenterReceiver.put("userId", account2.getExtId());
					msgCenterReceiver.put("userName", account2.getName());
					msgCenterReceiversArray.add(msgCenterReceiver);
				}
				JSONObject msgCenterPayLoad = new JSONObject();
				JSONObject msg = new JSONObject();
				String msgId = UUIDUtil.getUUID().replace("-", "");
				msg.put("msgId", msgId);
				msg.put("msgTitle", "您有一条新设备报修审核单");
				msg.put("msgContent", "您有一条新设备报修审核单");
				msg.put("msgUrlPc", msgUrlPc);
				msg.put("msgUrlApp", msgUrlApp.replace("#id#", repairId).replace("#type#", "0"));
				msg.put("msgOrigin", "设备报修");
				msg.put("msgTypeCode", "SBBX");
				msg.put("msgTemplateType", "SBBX");
				msg.put("schoolId", school.getExtId());
				msg.put("creatorName", account.getName());

				JSONObject first = new JSONObject();
				first.put("value", "您有一条新设备报修审核单");

				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", account.getName());
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", askPhone);
	 

				JSONObject keyword3 = new JSONObject();
				keyword3.put("value", repairAddr);
				
				
				JSONObject keyword4 = new JSONObject();
				keyword4.put("value", deviceName +"【" + bugDisplay + "】");
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				JSONObject keyword5 = new JSONObject();
				keyword5.put("value", format.format(new Date()));
	 
				JSONObject remark = new JSONObject();
				remark.put("value", "请及时审核。");
				
				JSONObject data = new JSONObject();
				data.put("first", first);
				data.put("keyword1", keyword1);
				data.put("keyword2", keyword2);
				data.put("keyword3", keyword3);
				data.put("keyword4", keyword4);
				data.put("keyword5", keyword5);
				data.put("remark", remark);
				data.put("url", msg.get("msgUrlApp"));
				msg.put("msgWxJson", data);
				msgCenterPayLoad.put("msg", msg);
				msgCenterPayLoad.put("receivers",msgCenterReceiversArray);
				sendAppMsg(msgId,msgCenterPayLoad);
//				try {
//					SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
//			        sp.start();     
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("addAPPRepairInfo => exception : " + e.toString());
			setPromptMessage(response, "-1", "添加失败");
		}
		return response;
	}
	
	@RequestMapping(value="/getAPPRepairTypeInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPRepairTypeInfo(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String schoolId = request.getString("schoolId");
		param.put("schoolId", schoolId);
		try {
			List<JSONObject> data = repairManageService.getRepairTypeInfo(param);
			for(JSONObject json:data){
				String repairType = json.getString("repairType");
				//String repairDetail = json.getString("repairDetail");
				json.put("repairType", repairType);
			}
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	
	@RequestMapping(value="/getAPPRepairPersonInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPRepairPersonInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairTypeId = request.getString("repairTypeId");
		String schoolId = request.getString("schoolId");
		param.put("repairTypeId", repairTypeId);
		param.put("schoolId", schoolId);
		try {
			List<JSONObject> data = repairManageService.getRepairPersonInfo(param);
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	
	/**
	 * APP报修详情
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/getAPPRepairDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPRepairDetail(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		String schoolId = request.getString("schoolId");
		String userId = request.getString("userId");
		
		if (userId == null || userId.length() == 0) {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}
		/**
		 * 判断身份
		 */
		 User user = commonDataService.getUserById(Long.valueOf(schoolId), Long.valueOf(userId));
		 if(user.getUserPart().getRole() == T_Role.Parent ||
			user.getUserPart().getRole() == T_Role.Student) {
			setPromptMessage(response, ERRCODE_NO_PERMISSION, PROMPT_NO_PERMISSION);
			return response;
		 }
		
		 param.put("repairId", repairId);
		 param.put("schoolId", schoolId);
		 String selectedSemester = commonDataService.getCurTermInfoId(Long.parseLong(schoolId));
		 try {
			 JSONObject data = repairManageService.getAPPRepairDetail(param);
			 if(data != null) {
				 boolean hasPermission = false;
				 String personId = data.getString("askPerson");
				 if(String.valueOf(user.getAccountPart().getId()).equals(personId)) {
					 //是自己
					 hasPermission = true;
				 } else {
					 String userAccountId = String.valueOf(user.getAccountPart().getId());
					 hasPermission = isPermission(userAccountId,data.getString("repairTypeId"));
				 }
				 
				 if(hasPermission) {
					 Account account = commonDataService.getAccountById(Long.parseLong(schoolId), Long.parseLong(personId),selectedSemester);
					 data.put("askPerson", account.getName());
					 setPromptMessage(response, ERRCODE_SUCCESS, PROMPT_SUCCESS);
					 response.put("data", data);
				 } else {
					 setPromptMessage(response, ERRCODE_NO_PERMISSION, PROMPT_NO_PERMISSION); 
				 }
			 } else {
				 setPromptMessage(response, ERRCODE_DELETED, PROMPT_DELETED);
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
			 setPromptMessage(response, ERRCODE_ERROR, "查询失败");
		 }
		 return response;
	}
	
	/**
	 * 检查是否为审查者
	 * @param accountId
	 * @param repairTypeId
	 * @return
	 */
	private boolean isPermission(String accountId,String repairTypeId) {
		String isCheckPerson = repairManageService.getIsCheckPerson(repairTypeId);
		if(StringUtils.isNotBlank(isCheckPerson)){
			String[] split = isCheckPerson.split(",");
			for (String eachAccount : split) {
				if(StringUtils.isNotBlank(eachAccount) && eachAccount.equals(accountId)){
					return true;
				}
			}
		}
		return false;
	}
	
	@RequestMapping(value="/addAPPFeedbackInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addAPPFeedbackInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		String feedbackInfo = request.getString("feedbackInfo");
		Long schoolId = request.getLong("schoolId");
		Long userId = request.getLong("userId");
		String selectedSemester = commonDataService.getCurTermInfoId(schoolId);
		User user = commonDataService.getUserById(schoolId, userId,selectedSemester);
		String teacherId ="";
		if(user!=null && user.getAccountPart() != null){
			teacherId = String.valueOf(user.getAccountPart().getId());
		}
		String role="";
		param.put("repairId", repairId);
		boolean isManager=authServiceImpl.getIsMoudleManagerByAccountId(schoolId, Long.parseLong(teacherId), "cs1019",selectedSemester);
		//如果是多种角色则优先出"申报人",PC端这块由前段控制的,呵呵哒~
		/*JSONObject repairObj = repairManageService.getAPPRepairDetail(param);
		if(repairObj==null || StringUtils.isBlank(repairObj.getString("repairTypeId"))){
					return response;
		}
		JSONObject typeParam = new JSONObject();
		typeParam.put("repairTypeId", repairObj.get("repairTypeId"));
		String checkPersons=repairManageService.getPersonsByRepairId(typeParam);
		if(StringUtils.isNotBlank(checkPersons)&& checkPersons.contains(teacherId)){
				role="2";
		}*/
		if(isManager){
			role="0";
		}
		//判断是否为申报人
		JSONObject repairObj = repairManageService.getAPPRepairDetail(param);
		String askPerson = repairObj.getString("askPerson");
		if(teacherId.equals(askPerson)){
			role="2";
		}else{
			if(!"0".equals(role)){
			 role="1";
			}
		}
		if("0".equals(role)){
			param.put("personType", "管理员");
		}else if("1".equals(role)){
			param.put("personType", "审核人员");
		}else{
			param.put("personType", "申报人");
		}
		param.put("feedbackPerson", teacherId);
		param.put("feedbackTime", new Date());
		param.put("feedbackInfo", feedbackInfo);
		try {
			repairManageService.addFeedbackInfo(param);
			setPromptMessage(response, "0", "添加成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "添加失败");
			e.printStackTrace();
		}
		
		return response;
	}
	
	@RequestMapping(value="/getAPPFeedbackInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPFeedbackInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		Long schoolId = request.getLong("schoolId");
		String repairId = request.getString("repairId");
		String selectedSemester =commonDataService.getCurTermInfoId(schoolId);
		param.put("repairId", repairId);
		try {
			List<JSONObject> data = repairManageService.getFeedbackInfo(param);
			List<Long> accountIds = new ArrayList<Long>();
			for(JSONObject o:data){
				Long accountId = o.getLong("feedbackPerson");
				accountIds.add(accountId);
			}
			List<Account> ll = commonDataService.getAccountBatch(schoolId, accountIds,selectedSemester);
			Map<String,String>  teacherIdName = new HashMap<String,String> ();
			for(int i = 0;i<ll.size();i++){
				String teacherId = String.valueOf(ll.get(i).getId());
				String teacherName = ll.get(i).getName();
				teacherIdName.put(teacherId, teacherName);
			}
		
			for(JSONObject json:data){
				String feedbackPerson = json.getString("feedbackPerson");
				String feedbackPersonName = teacherIdName.get(feedbackPerson);
				String personType = json.getString("personType");
				json.put("feedbackPerson", personType+": "+feedbackPersonName);
			}
			response.put("data", data);
			setPromptMessage(response, "0", "查询成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value="/getAPPRepairAskpersonInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPRepairAskpersonInfo(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject response = new JSONObject();
		Long schoolId = request.getLong("schoolId");
		Long userId = request.getLong("userId");
		String selectedSemester = commonDataService.getCurTermInfoId(schoolId);
		User user = commonDataService.getUserById(schoolId, userId,selectedSemester);
		if(user!=null && user.getAccountPart() != null){
			String askPerson = user.getAccountPart().getName();
			String askPhone = user.getAccountPart().getMobilePhone();
			JSONObject data = new JSONObject();
			data.put("askPerson", askPerson);
			data.put("askPhone", askPhone);
			data.put("askDepartment","");
			response.put("data", data);
			setPromptMessage(response, "0", "获取成功");
		}else{
			setPromptMessage(response, "-1", "获取失败");
		}
		return response;
	}
	
	/**
	 * 维修评价
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/addAPPRepairEvalInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addAPPRepairEvalInfo(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		int isEvaled = request.getIntValue("isEvaled");
		String name = request.getString("name");
		param.put("repairId", repairId);
		param.put("isEvaled", isEvaled);
		param.put("repairState", 1);
		try {
			repairManageService.addRepairEvalInfo(param);
			setPromptMessage(response, "0", "添加成功");
			JSONObject rd = repairManageService.getRepairDetail(param);
			Long schoolId = rd.getLong("schoolId");
			String termInfo =  commonDataService.getCurTermInfoId(schoolId);
			JSONObject repair = repairManageService.getRepairInfo(request); 
			Long  accountId = repair.getLong("askPerson");
			School school = commonDataService.getSchoolById(schoolId, termInfo);
			
			List<Long> accountIds = new ArrayList<Long>();
			accountIds.add(accountId);
			List<Account> accounts = commonDataService.getAccountBatch(schoolId, accountIds, termInfo);
			Account account =accounts.get(0);
			List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
			JSONObject msgCenterReceiver = new JSONObject();
			msgCenterReceiver.put("userId", account.getExtId());
			msgCenterReceiver.put("userName", account.getName());
			msgCenterReceiversArray.add(msgCenterReceiver);

			JSONObject msgCenterPayLoad = new JSONObject();
			JSONObject msg = new JSONObject();
		    String msgId = UUIDUtil.getUUID().replace("-", "");
			msg.put("msgId", msgId);
			msg.put("msgTitle", "您提交的设备报修单有了新进度");
			msg.put("msgContent", account.getName() + "您提交的设备报修单有了新进度！!");
			msg.put("msgUrlPc", ""+repairId);
			msg.put("msgUrlApp", ""+repairId);
			msg.put("msgOrigin", "设备报修");
			msg.put("msgTypeCode", "SBBX");
			msg.put("msgTemplateType", "SBBX");
			msg.put("schoolId", school.getExtId());
			msg.put("creatorName", name );

			JSONObject first = new JSONObject();
			first.put("value", "您提交的设备报修单已经维修完成");

			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", account.getName());
			
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", repair.getString("askPhone"));

			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", repair.getString("repairAddr"));

			JSONObject keyword4 = new JSONObject();
			keyword4.put("value", repair.getString("deviceName") +"【" + repair.getString("bugDisplay") + "】");

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			JSONObject keyword5 = new JSONObject();
			keyword5.put("value", format.format(repair.getDate("askDate")));
 
			JSONObject remark = new JSONObject();
			remark.put("value", "请及时查看。");
			
			JSONObject data = new JSONObject();
			data.put("first", first);
			data.put("keyword1", keyword1);
			data.put("keyword2", keyword2);
			data.put("keyword3", keyword3);
			data.put("keyword4", keyword4);
			data.put("keyword5", keyword5);
			data.put("remark",   remark);
			msg.put("msgWxJson", data);
			msgCenterPayLoad.put("msg", msg);
			msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
			logger.info("msgCenterPayLoad=====>" + msgCenterPayLoad.toString());
//			try {
//				SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
//		        sp.start();     
//			} catch (Exception e) {
//				logger.info("setPromptMessage1=====>" + e);
//				e.printStackTrace();
//			}
			
		} catch (Exception e) {
			setPromptMessage(response, "-1", "添加失败");
			logger.info("setPromptMessage=====>" + e);
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * 维修状态
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/addAPPRepairEvalState",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addAPPRepairEvalState(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String repairId = request.getString("repairId");
		int repairState = request.getIntValue("repairState");
		param.put("repairId", repairId);
		param.put("repairState", repairState);
		logger.info("addAPPRepairEvalState => param :" + param.toString());
		try {
			repairManageService.updateRepairEvalState(param);
			setPromptMessage(response, "0", "添加成功");
			logger.info("addAPPRepairEvalState : repairState " + repairState);
			if (repairState == 1) {
				// 1表示已维修
				String userId = request.getString("userId");
				if(userId == null || userId.length() == 0) {
					logger.info("addAPPRepairEvalState => userId为空！");
					setPromptMessage(response, "-1", "userId为空！");
					return response;
				}
				
				JSONObject repairDetail = repairManageService.getRepairDetail(param);
				if(repairDetail == null) {
					logger.info("addAPPRepairEvalState => 维修信息为空！");
					setPromptMessage(response, "-1", "维修信息为空！");
					return response;
				}
				Long schoolId = repairDetail.getLong("schoolId");
				String termInfo = commonDataService.getCurTermInfoId(schoolId);
				JSONObject repair = repairManageService.getRepairInfo(request); 
				Long  askPersonAccountId = repair.getLong("askPerson");
				School school = commonDataService.getSchoolById(schoolId, termInfo);
				if(school == null) {
					logger.info("addAPPRepairEvalState => school为空！");
					setPromptMessage(response, "-1", "school为空！");
					return response;
				}
				
				//获取当前用户
				User user = commonDataService.getUserById(schoolId,Long.valueOf(userId),termInfo);
				if(user == null || user.getUserPart() == null) {
					logger.info("addAPPRepairEvalState => user为空！");
					setPromptMessage(response, "-1", "user为空！");
					return response;
				}
				//当前用户accountId
				long accountId = user.getUserPart().getAccountId();
				
				List<Long> accountIds = new ArrayList<Long>();
				accountIds.add(askPersonAccountId);
				List<Account> accounts = commonDataService.getAccountBatch(schoolId, accountIds, termInfo);
				if(accounts == null || accounts.size() == 0) {
					logger.info("addAPPRepairEvalState => accounts为空！");
					setPromptMessage(response, "-1", "accounts为空！");
					return response;
				}
				Account account =accounts.get(0);

				List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
				JSONObject msgCenterReceiver = new JSONObject();
				msgCenterReceiver.put("userId", account.getExtId());
				msgCenterReceiver.put("userName", account.getName());
				msgCenterReceiversArray.add(msgCenterReceiver);

				JSONObject msgCenterPayLoad = new JSONObject();
				JSONObject msg = new JSONObject();
				String msgId = UUIDUtil.getUUID().replace("-", "");
				msg.put("msgId", msgId);
				msg.put("msgTitle", "您提交的设备报修单有了新进度");
				msg.put("msgContent", account.getName() + "您提交的设备报修单有了新进度！!");
				msg.put("msgUrlPc", msgUrlPc);
				msg.put("msgUrlApp", msgUrlApp.replace("#id#", repairId).replace("#type#", "1"));
				msg.put("msgOrigin", "设备报修");
				msg.put("msgTypeCode", "SBBX");
				msg.put("msgTemplateType", "SBBX");
				msg.put("schoolId", school.getExtId());
//				Long accountId2 = (Long)req.getSession().getAttribute("accountId") ;
				Account account2 = commonDataService.getAccountById(schoolId, accountId, termInfo);
				msg.put("creatorName", account2.getName());

				JSONObject first = new JSONObject();
				first.put("value", "您提交的设备报修单已经维修完成");

				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", account.getName());
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", repair.getString("askPhone"));

				JSONObject keyword3 = new JSONObject();
				keyword3.put("value", repair.getString("repairAddr"));
 
				JSONObject keyword4 = new JSONObject();
				keyword4.put("value", repair.getString("deviceName") +"【" + repair.getString("bugDisplay") + "】");
 
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				JSONObject keyword5 = new JSONObject();
				keyword5.put("value", format.format(repair.getDate("askDate")));
	 
				JSONObject remark = new JSONObject();
				remark.put("value", "请及时查看。");
				
				JSONObject data = new JSONObject();
				data.put("first", first);
				data.put("keyword1", keyword1);
				data.put("keyword2", keyword2);
				data.put("keyword3", keyword3);
				data.put("keyword4", keyword4);
				data.put("keyword5", keyword5);
				data.put("remark",   remark);
				data.put("url", msg.get("msgUrlApp"));
				msg.put("msgWxJson", data);
				msgCenterPayLoad.put("msg", msg);
				msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
				logger.info("addAPPRepairEvalState ：msgCenterPayLoad=====>" + msgCenterPayLoad.toString());
				sendAppMsg(msgId,msgCenterPayLoad);
//				try {
//					SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
//			        sp.start();     
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
			
		} catch (Exception e) {
			logger.info("addAPPRepairEvalState exception :" + e.toString());
			setPromptMessage(response, "-1", "添加失败");
			e.printStackTrace();
		}
		
		return response;
	}
	//----------------------------@author：zhh[设备报修App端审核功能]
	/**
	 * 维修审核新增（H5）
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value="/addAPPRepairInfoCheck",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addAPPRepairInfoCheck(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		try {
			//获取原先保存的askPhone和askDepartment
			Long schoolId = param.getLong("schoolId");
			JSONObject rd = repairManageService.getRepairDetail(param);
			param.put("askPhone", rd.get("askPhone"));
			param.put("askDepartment", rd.get("askDepartment"));
			long userId=request.getLong("userId");
			String selectedSemester =commonDataService.getCurTermInfoId(schoolId);
			User user = commonDataService.getUserById( schoolId , userId,selectedSemester);
			Long accountId = user.getAccountPart().getId();
			String checkPerson =  user.getAccountPart().getName();
			String checkpsons = repairManageService.getPersonsByRepairId(param);
			String role="";
			boolean isManager=authServiceImpl.getIsMoudleManagerByAccountId(schoolId, accountId, "cs1019",selectedSemester);
			if(isManager){
				role="0";
			}else{
				role="1";
			}
			param.put("isCheck", 1);
			param.put("checkPerson", checkPerson);
			param.put("isSendMsg", 0);
			if(checkpsons.indexOf(accountId+"")>-1||"0".equals(role)){
				// 更新 isCheck 和checkPerson等信息
				repairManageService.updateCheckRepairInfo(param);
				setPromptMessage(response, "0", "添加维修单审核成功");
				
                String repairId = param.getString("repairId");
				String termInfo = selectedSemester;
				School school = commonDataService.getSchoolById(schoolId, termInfo);
				JSONObject repair = repairManageService.getRepairInfo(request); 
				Long askPerson = repair.getLong("askPerson");
				List<Long> accountIds = new ArrayList<Long>();
				accountIds.add(askPerson);
				List<Account> accounts = commonDataService.getAccountBatch(schoolId, accountIds, termInfo);
				Account account = accounts.get(0);
				List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
				JSONObject msgCenterReceiver = new JSONObject();
				msgCenterReceiver.put("userId", account.getExtId());
				msgCenterReceiver.put("userName", account.getName());
				msgCenterReceiversArray.add(msgCenterReceiver);
				
 
				JSONObject msgCenterPayLoad = new JSONObject();
				JSONObject msg = new JSONObject();
			    String msgId = UUIDUtil.getUUID().replace("-", "");
				msg.put("msgId", msgId);
				msg.put("msgTitle", "您提交的设备报修单有了新进度");
				msg.put("msgContent", account.getName() + "您提交的设备报修单有了新进度！!");
				msg.put("msgUrlPc", msgUrlPc);
				msg.put("msgUrlApp", msgUrlApp.replace("#id#", repairId).replace("#type#", "1"));
				msg.put("msgOrigin", "设备报修");
				msg.put("msgTypeCode", "SBBX");
				msg.put("msgTemplateType", "SBBX");
				msg.put("schoolId", school.getExtId());
				
				Account account2 = commonDataService.getAccountById(schoolId, accountId, termInfo);
				msg.put("creatorName", account2.getName());

				JSONObject first = new JSONObject();
				first.put("value", "您提交的设备报修单有了新进度");

				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", account.getName());
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", repair.getString("askPhone"));

				JSONObject keyword3 = new JSONObject();
				keyword3.put("value", repair.getString("repairAddr"));
 
				JSONObject keyword4 = new JSONObject();
				keyword4.put("value",repair.getString("deviceName") +"【" + repair.getString("bugDisplay") + "】");
                
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				JSONObject keyword5 = new JSONObject();
				keyword5.put("value", format.format(repair.getDate("askDate")));
	 
				JSONObject remark = new JSONObject();
				remark.put("value", "请及时查看。");
				
				JSONObject data = new JSONObject();
				data.put("first", first);
				data.put("keyword1", keyword1);
				data.put("keyword2", keyword2);
				data.put("keyword3", keyword3);
				data.put("keyword4", keyword4);
				data.put("keyword5", keyword5);
				data.put("remark", remark);
				data.put("url", msg.get("msgUrlApp"));
				msg.put("msgWxJson", data);
				
				msgCenterPayLoad.put("msg", msg);
				msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
				logger.info("msgCenterPayLoad======>" + msgCenterPayLoad);
				sendAppMsg(msgId,msgCenterPayLoad);
//				try {
//					SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
//			        sp.start();     
//				} catch (Exception e) {
//					e.printStackTrace();
//				}				
			}else{
				// 更新不包括 isCheck 和checkPerson等信息
				repairManageService.updateRepairInfo(param);
				setPromptMessage(response, "0", "添加维修单审核成功");
			}

		} catch (Exception e) {
			setPromptMessage(response, "-1", "添加维修单审核失败");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 获取登录人员的角色 0 管理员，1 审核人员，2 一般老师（H5）
	 * @param req
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value="/getAPPRepairRole",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPRepairRole(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		JSONObject data = new JSONObject();
		try {
			int role = 2;
			long schoolId=request.getLong("schoolId");
			long userId=request.getLong("userId");
			String selectedSemester = commonDataService.getCurTermInfoId(schoolId);
			User user = commonDataService.getUserById( schoolId , userId,selectedSemester);
			Long accountId = user.getAccountPart().getId();
			param.put("schoolId", schoolId);
			List<String> selpersons = repairManageService.getSelPersons(param);
			int count = repairManageService.getRepairTypeCount(param);
			if(count>0){
				data.put("haveData", 1);//已经设置了维修类别
			}else{
				data.put("haveData", 0);//未设置维修类别
			}
			for(String s:selpersons){
				if(s.indexOf(accountId+"")>-1){
					role = 1;//审核人员
				}
			}
			boolean flag=false;
			if(!"1".equals(role)){
				flag=authServiceImpl.getIsMoudleManagerByAccountId(schoolId, accountId, "cs1019",selectedSemester);
				if(flag){
					role = 0;
				}
			}
			data.put("role", role);
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
			logger.info("【getAPPRepairRole】入参：schoolId:{}userId:{} 出参角色role:{} isMoudleManager:{}",schoolId,userId,role,flag);
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 维修审核查看列表（H5）
	 * @param req
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value="/getAPPRepairInfoCheckList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPRepairInfoCheckList(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();

		try {
			long schoolId = request.getLong("schoolId");
			long userId = request.getLong("userId");
			
			String selectedSemester = commonDataService.getCurTermInfoId( schoolId );
			User user = commonDataService.getUserById( schoolId , userId,selectedSemester);
			String accountId="";
			if(user!=null && user.getAccountPart() != null){
				accountId = String.valueOf(user.getAccountPart().getId());
	       /* teacherId = "100984739";
	        teacherName = "1303";*/
	       // accountId = "100984779";
	        //teacherName = "艾芳芳";
	       /* teacherId = "100985011";
	        teacherName = "陈佳";*/
			}
	        String role="";
			boolean isManager=authServiceImpl.getIsMoudleManagerByAccountId(schoolId, Long.parseLong(accountId), "cs1019",selectedSemester);
			if(isManager){
				role="0";
			}else{
				role="1";
			}
			param.put("schoolId", schoolId);
			param.put("accountId", accountId);
			param.put("role", role);
			
			List<JSONObject>data= new ArrayList<JSONObject>();
			List<JSONObject> rdata=repairManageService.getAPPRepairInfoList(param);
			
			if (rdata != null ) {
				List<Long> askPersonList = new ArrayList<Long>();
				for(JSONObject obj:rdata){
					String checkPersons = obj.getString("isCheckconfig");
					askPersonList.add(obj.getLong("askPerson"));
					if((StringUtils.isNotBlank(checkPersons)&&checkPersons.contains(accountId))||"0".equals(role)){
						if(StringUtils.isBlank(checkPersons)){
							obj.put("isCheck", 2);
						}
						data.add(obj);
					}
				}
				List<Account> accounts = commonDataService.getAccountBatch(schoolId, askPersonList, selectedSemester);
				Map<Long, String> accountMap = new HashMap<Long, String>();
				for (int i = 0; i < accounts.size(); i++) {
					Account account = accounts.get(i);
					accountMap.put(account.getId(), account.getName());
				}
				for(JSONObject obj:rdata){
					obj.put("askPersonName", accountMap.get(obj.getLong("askPerson")));
				}
				response.put("data", data);
				setPromptMessage(response, "0", "查询成功");
			} else {
				setPromptMessage(response, "-1", "查询失败");
			}
			setPromptMessage(response, "0", "查询成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 维修审核查看详情（H5）
	 * @param request
	 * @param req
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value="/getAPPRepairCheckDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAPPRepairCheckDetail(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		 try {
			 String repairId = request.getString("repairId");
			 Long schoolId = request.getLong("schoolId");
			 Long userId = request.getLong("userId");
			 
			 if (userId == null) {
				setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
				return response;
			 }
			 /**
			 * 判断身份
			 */
			 User user = commonDataService.getUserById(schoolId, userId);
			 if(user.getUserPart().getRole() == T_Role.Parent ||
				user.getUserPart().getRole() == T_Role.Student) {
				setPromptMessage(response, ERRCODE_NO_PERMISSION, PROMPT_NO_PERMISSION);
				return response;
			 }
			 
			 param.put("schoolId", schoolId);
			 param.put("repairId", repairId);
			 
			 JSONObject data = repairManageService.getAPPRepairCheckDetail(param);
			 if(data != null){
				 boolean hasPermission = false;
				 Long personId = data.getLong("askPerson");
				 
				 if(user.getAccountPart().getId() == personId) {
					 //是自己
					 hasPermission = true;
				 } else {
					// JSONObject accessTokenInfo = HttpClientToken.callHttpRemoteInterface(getAccessTokenUrl,null);
					 String userAccountId = String.valueOf(user.getAccountPart().getId());
					 hasPermission = isPermission(userAccountId,data.getString("repairTypeId"));
					 if (!hasPermission) {
						    logger.info("baseUrl==" + baseUrl);
							String getAccessTokenUrl = baseUrl + "/basedataApi/BaseDataService/isMoudleManager?menuId=cs1019&userId=" +userId;
							JSONObject accessTokenInfo = HttpClientToken.callHttpRemoteInterface(getAccessTokenUrl,null);
							logger.info("accessTokenInfo==" + accessTokenInfo);
							hasPermission = accessTokenInfo.getBooleanValue("isMoudleManager");
					 }
				 }
				 
				 if(hasPermission) {
					 String termInfo= commonDataService.getCurTermInfoId(schoolId);
					 Account account = commonDataService.getAccountById(schoolId, personId, termInfo);
					 
					 if(account!=null){
						 data.put("askPerson", account.getName()); 
					 }else{
						 data.put("askPerson", ""); 
					 }
					 response.put("data", data);
					 setPromptMessage(response, ERRCODE_SUCCESS, PROMPT_SUCCESS);
				 } else {
					 setPromptMessage(response, ERRCODE_NO_PERMISSION, PROMPT_NO_PERMISSION); 
				 }
			 }else{
				 setPromptMessage(response, ERRCODE_DELETED, PROMPT_DELETED);
			 }
		} catch (Exception e) {
			setPromptMessage(response, ERRCODE_ERROR, "查询失败");
			e.printStackTrace();
		}
	    return response;	
	}
	/***
	 * 在线通知和离线通知接口
	 * @param schoolId 学校代码
	 * @param userIds 用户代码集合
	 * @param accountIds 用户accountIds集合
	 * @param serviceId 业务类型代码
	 * @param optionType 
	 * @param serviceType
	 * @throws Exception
	 */
	public String sendMsg(String schoolId,List<String> ids,String userId,
			String serviceId,MessageNoticeModelEnum noticeModel,String deviceName){
		/*noticeServiceType:通知业务类型,noticeDate:通知时间, schoolId:学校代码
		userId:创建用户,noticeType:通知类型,needConfirm:是否需要确认
		noticeUserType:通知用户类型,noticeModel:通知模型,noticeStatus:通知状态
		noticeSendDate:通知发送时间,serviceId:业务轮次代码,sendMsgStatus:短信开关
		noticeDetails:[{noticeDetailsContent：通知消息体,userId：用户代码,
		classId：班级代码,userType：用户类型,schoolId：学校代码,isStatus：是否有效,
		family:父母[accountId:userId,accountId:userId]}] */
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("noticeServiceType", MessageServiceEnum.repairManage.toInteger());
		jsonObject.put("noticeDate", new Date());
		jsonObject.put("schoolId", schoolId);
		jsonObject.put("userId", userId);
		jsonObject.put("noticeType", MessageNoticeTypeEnum.TIMELY.toInteger());
		jsonObject.put("needConfirm", "0");
		jsonObject.put("noticeUserType", MessageNoticeUserTypeEnum.PARTSTEACHER.toInteger());
		jsonObject.put("noticeModel", noticeModel.toInteger());
		jsonObject.put("noticeStatus", MessageStatusEnum.SUCCESS.toInteger());
		jsonObject.put("noticeSendDate", new Date());
		jsonObject.put("serviceId", serviceId);
		//noticeOperate
		jsonObject.put("sendMsgStatus", MessageSmsEnum.DEFAULT.toInteger());
		List<JSONObject> noticeDetails = new ArrayList<JSONObject>();	
		
		String messageTemplet = "";
		try {
			logger.info("\n ========== 开始调用RPC messageTemplet()方法 ============");
			String result =messageTemplet;// messageTemplet = MotanService.messageTemplet(MessageTempletEnum.MESSAGE, "repairManage.message.info.A");
			logger.info("\n ================ 返回结果："+ result+" ============== ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(CollectionUtils.isNotEmpty(ids) && ids.size() > 0){
			for (String id : ids) {
				String[] split = id.split(":");
				JSONObject noticeDetail = new JSONObject();
				noticeDetail.put("noticeDetailsContent", messageTemplet);
				noticeDetail.put("userId", split[1]);
				noticeDetail.put("classId", "");
				noticeDetail.put("userType", "3");
				noticeDetail.put("schoolId", schoolId);
				noticeDetail.put("accountId", split[0]);
				noticeDetail.put("isStatus", "0");
				noticeDetail.put("family", "");
				if(null!=deviceName){
					noticeDetail.put("contentParams", deviceName);
				}
				noticeDetails.add(noticeDetail);
			}
		}
		
		jsonObject.put("noticeDetails", noticeDetails);
		String result="";
		try {
			logger.info("\n ========== 开始调用RPC noticeMessage()方法============");
			result = MotanService.noticeMessage(jsonObject);
			logger.info("\n ================ 返回结果："+ result+" ==============");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value="/deleteRepairpicture",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteRepairpicture(@RequestBody JSONObject request,HttpServletRequest req,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		int result = 0;
		if (StringUtils.isNotBlank(param.getString("picUrl"))) {
			try {
				fileServerImplFastDFS.deleteFile(param.getString("picUrl"));
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			List<JSONObject> list = repairManageService.selectRepairpictures(param);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					try {
						fileServerImplFastDFS.deleteFile(list.get(i).getString("picUrl"));
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		result = repairManageService.deleteRepairpicture(param);
		if (result > 0) {
			setPromptMessage(response, "0", "删除照片成功！");
		}else {
			setPromptMessage(response, "-1", "删除照片失败");
		}
		
		return response;
	}
	
	

	@RequestMapping(value = "/addAPPUploadRepairpicture" )
	@ResponseBody
	public JSONObject uploadRepairpicture( HttpServletRequest req, @RequestParam("fileBoby") MultipartFile file ,   HttpServletResponse res){
		String repairId = req.getParameter("repairId");
		JSONObject response = new JSONObject();
		if (file!=null) {
			 File df = null;
			 File df1 = null;
				try {
					String fileName = file.getOriginalFilename();
					if ("blob".equals(fileName)) {
						fileName = req.getParameter("filename");
						if (StringUtils.isBlank(fileName)) {
							fileName = "blob.jpg";
						}
					}
					String fileId = null;
					if (file.getSize() > 1048576) {// 如果文件大等于1Mb 
						fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +"jpg";
						String tempName0 = UUID.randomUUID().toString() + ".jpg";
						df = new File(tempName0);
						file.transferTo(df);
						String tempNam1 = UUID.randomUUID().toString()+".jpg";
						df1 = new File(tempNam1);
						Thumbnails.of(df).scale(1f).outputQuality(0.25f).outputFormat("jpg").toFile(df1);  
						 fileId = fileServerImplFastDFS.uploadFile(df1,tempNam1);
					}else {
						String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
						fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +suffix;
						String tempName0 = UUID.randomUUID().toString()+"."+suffix;
						df = new File(tempName0);
						file.transferTo(df);
						fileId = fileServerImplFastDFS.uploadFile(df,tempName0);
					}

					if (StringUtils.isNotBlank(fileId)) {
						response.put("picName", fileName);
						response.put("picUrl", fileId);
						JSONObject param = new JSONObject();
						param.put("picName", fileName);
						param.put("picUrl", fileId);
						param.put("repairId", repairId);
						repairManageService.insertRepairpicture(param);
						setPromptMessage(response, "0", "上传成功");
					}else{
						setPromptMessage(response, "-1", "文件上传出现问题,请联系管理员!");
					}
				} catch (Exception e) {
					setPromptMessage(response, "-1", "文件上传出现问题,请联系管理员!");
					e.printStackTrace();
				}finally {
					if(df!=null)df.delete();
					if (df1!=null) df1.delete();
				}
		}
 
		return response;
	}
	
	
	@RequestMapping(value = "/getAPPPreDownloadFile" ,method = RequestMethod.GET)
	public void preDownloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		String urlTemp = req.getParameter("picUrl");
		String suffix =urlTemp.substring(urlTemp.lastIndexOf("."));
		String originTemp = UUIDUtil.getUUID() + suffix;
		String url = URLDecoder.decode(urlTemp, "UTF-8");
		ByteArrayInputStream bis = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis1 = null;
		File temp = null;
		try {
				String fileName = originTemp;
				fileServerImplFastDFS.downloadFile(url, fileName);
				temp = new File(fileName);
				// 写入数据结束
				res.setContentType("octets/stream");
				String downLoadName = fileName;
				res.addHeader("Content-Disposition", "attachment;filename=".concat(downLoadName ));
				bis1 = new BufferedInputStream(new FileInputStream(temp));
				bos = new BufferedOutputStream(res.getOutputStream());
				byte[] buff = new byte[2048];
				int bytesRead;
				while (-1 != (bytesRead = bis1.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
				bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bis1 != null)
				try {
					bis1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(temp!=null){
				temp.delete();
			}
		}

	}
	
	@RequestMapping(value = "/preDownloadFile" ,method = RequestMethod.GET)
	public void preDownloadFile2(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		preDownloadFile(req , res);

	}
	
//	//测试用，之后删除
//	class SubProcess extends Thread {
//        private String kafkaUrl;
//        private String msgId;
//        private JSONObject msgCenterPayLoad;
//        private String busi;
//        private String clientId;
//        private String clientSecret;
//		
//		public  SubProcess(String kafkaUrl , String msgId , JSONObject msgCenterPayLoad , String busi , String clientId  , String clientSecret){
//	        this.kafkaUrl = kafkaUrl;
//	        this.msgId = msgId;
//	        this.msgCenterPayLoad = msgCenterPayLoad;
//	        this.busi = busi;
//	        this.clientId = clientId;
//	        this.clientSecret = clientId;
//		}
//		
//		@Override
//		public void run() {
//			try {
//				KafkaUtils.sendAppMsg(kafkaUrl, msgId, msgCenterPayLoad,  busi, clientId, clientSecret);
//			} catch (Exception e) {
//				e.printStackTrace();
//				logger.info("repairManage => sendAppMsg exception : " + e.getMessage());
//			}
//		}
//	}
	
	private void sendAppMsg(String msgId,JSONObject msgPayLoad) {
		try {
			KafkaUtils.sendAppMsg(kafkaUrl, msgId, msgPayLoad,  "SBBX", clientId, clientSecret);
		}catch (Exception e) {
			e.printStackTrace();
			logger.info("repairManage => sendAppMsg exception : " + e.getMessage());
		}
	}
	
	/**
	 * 报修申报(测试用，之后删除)
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value="/addAPPRepairInfoTest",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addAPPRepairInfoTest(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject response = new JSONObject();
		logger.info("addAPPRepairInfo => request: " + request.toString());
		JSONObject param = new JSONObject();
		String schoolId = request.getString("schoolId");
		Long userId = request.getLong("userId");
		String selectedSemester = "20182";//commonDataService.getCurTermInfoId(Long.parseLong(schoolId));
		User user = commonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
		String teacherId ="";
		if(user!=null && user.getAccountPart() != null){
			teacherId = String.valueOf(user.getAccountPart().getId());
		}
		String deviceName = request.getString("deviceName");
		String repairAddr = request.getString("repairAddr");
		String repairTypeId = request.getString("repairTypeId");
		JSONArray repairPersons = request.getJSONArray("repairPersons");
		String bugDisplay = request.getString("bugDisplay");
		String askDepartment = request.getString("askDepartment");
		String askPhone = request.getString("askPhone");
		JSONArray attachmentIds = request.getJSONArray("attachmentIds");
		String repairId = UUIDUtil.getUUID();
		param.put("repairId", repairId);
		param.put("deviceName", deviceName);
		param.put("repairAddr", repairAddr);
		param.put("repairTypeId", repairTypeId);
		param.put("bugDisplay", bugDisplay);
		param.put("askDate", new Date());
		param.put("askDepartment", askDepartment);
		param.put("askPerson", teacherId);
		param.put("askPhone", askPhone);
		param.put("repairPersons", repairPersons);
		param.put("schoolId", schoolId);
		param.put("isSendMsg", 1);
		param.put("attachmentIds", attachmentIds);
		try {
			repairManageService.addRepairInfo(param);
			setPromptMessage(response, "0", "添加成功");
			response.put("repairId", repairId);
			
			String isCheckPerson = repairManageService.getIsCheckPerson(repairTypeId);
			if(StringUtils.isBlank(isCheckPerson)){
				 logger.info("addAPPRepairInfo => 没有审核人");
			}else{
				List<Long> accountIds = new ArrayList<Long>();
				String[] split = isCheckPerson.split(",");
				for (String string : split) {
					if(StringUtils.isNotBlank(string)){
						accountIds.add(Long.valueOf(string));  //拿 extid
					}
				}
 
				String termInfo = selectedSemester;
			 
				
				School school = null;//commonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
				List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfo);
				Account account = null;
				if(teacherId != null && teacherId.length() > 0) {
					account = commonDataService.getAccountAllById(Long.parseLong(schoolId), Long.parseLong(teacherId), termInfo);
				}
				
				List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
				for (int i = 0; i < accounts.size(); i++) {
					Account account2 = accounts.get(i);
					JSONObject msgCenterReceiver = new JSONObject();
					msgCenterReceiver.put("userId", account2.getExtId());
					msgCenterReceiver.put("userName", account2.getName());
					msgCenterReceiversArray.add(msgCenterReceiver);
				}
				JSONObject msgCenterPayLoad = new JSONObject();
				JSONObject msg = new JSONObject();
				String msgId = UUIDUtil.getUUID().replace("-", "");
				msg.put("msgId", msgId);
				msg.put("msgTitle", "您有一条新设备报修审核单");
				msg.put("msgContent", "您有一条新设备报修审核单");

				msg.put("msgUrlPc", msgUrlPc);
				msg.put("msgUrlApp", msgUrlApp.replace("#id#", repairId).replace("#type#", "0"));
				msg.put("msgOrigin", "设备报修");
				msg.put("msgTypeCode", "SBBX");
				msg.put("msgTemplateType", "SBBX");
				msg.put("schoolId", school != null ? school.getExtId() : schoolId);
				msg.put("creatorName", account != null ? account.getName() : "");

				JSONObject first = new JSONObject();
				first.put("value", "您有一条新设备报修审核单");

				JSONObject keyword1 = new JSONObject();
				keyword1.put("value", account != null ? account.getName() : "");
				
				JSONObject keyword2 = new JSONObject();
				keyword2.put("value", askPhone);
	 

				JSONObject keyword3 = new JSONObject();
				keyword3.put("value", repairAddr);
				
				
				JSONObject keyword4 = new JSONObject();
				keyword4.put("value", deviceName +"【" + bugDisplay + "】");
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				JSONObject keyword5 = new JSONObject();
				keyword5.put("value", format.format(new Date()));
	 
				JSONObject remark = new JSONObject();
				remark.put("value", "请及时审核。");
				
				JSONObject data = new JSONObject();
				data.put("first", first);
				data.put("keyword1", keyword1);
				data.put("keyword2", keyword2);
				data.put("keyword3", keyword3);
				data.put("keyword4", keyword4);
				data.put("keyword5", keyword5);
				data.put("remark", remark);
				data.put("url", msg.get("msgUrlApp"));
				msg.put("msgWxJson", data);
				msgCenterPayLoad.put("msg", msg);
				msgCenterPayLoad.put("receivers",msgCenterReceiversArray);
				sendAppMsg(msgId,msgCenterPayLoad);
//				try {
//					SubProcess sp = new SubProcess(kafkaUrl, msgId, msgCenterPayLoad, "SBBX", clientId, clientSecret);
//			        sp.start();     
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "添加失败");
		}
		return response;
	}
	
	public static void main(String[] args) {
		String getAccessTokenUrl = "https://pre.yunxiaoyuan.com/basedataApi/BaseDataService/isMoudleManager?menuId=cs1019&userId=233";
		
		JSONObject accessTokenInfo = HttpClientToken.callHttpRemoteInterface(getAccessTokenUrl,null);
		System.out.println( accessTokenInfo );
	}
	
}
 
