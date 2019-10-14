package com.talkweb.weekWork.action;
import java.io.BufferedOutputStream;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.utils.KafkaUtils;
import com.talkweb.weekWork.service.WeekWorkService;

/**
 * 周工作
 */
@Controller
@RequestMapping("/weekWork")
public class WeekWorkAction extends BaseAction {
	
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
	private static final String PROMPT_PARAM_ERROR 		= "参数错误,请联系管理员。";

	@Autowired
	private WeekWorkService weekWorkService;
	@Autowired
	private AllCommonDataService commonDataService;

	@Value("#{settings['kafkaClientId']}")
	private String clientId;

	@Value("#{settings['kafkaClientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
	
	@Value("#{settings['weekwork.msgUrlPc']}")
	private String msgUrlPc;
	
	@Value("#{settings['weekwork.msgUrlApp']}")
	private String msgUrlApp;
	
	Logger logger = LoggerFactory.getLogger(WeekWorkAction.class);
	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
	@RequestMapping(value = "/isFillMan",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject isFillMan(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));
			String schoolId = getXxdm(req);
			param.put("schoolId", schoolId);
			param.put("teacherId", accountId);
			if(weekWorkService.getFillManInfo(param)) {
				response.put("isFillMan", "1");	
			} else {
				response.put("isFillMan", "0");	
			}
			setPromptMessage(response, "0", "");
			boolean role = isMoudleManager(req, "cs1020");
			if(role){
				response.put("role", 1);
			}else{
				response.put("role", 0);
			}
		} catch(CommonRunException e) {
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getTerminfoAndStartWeek",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTerminfoAndStartWeek(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			String termInfo = request.getString("termInfo");
			if(StringUtils.isBlank(termInfo)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员!");
			}
			int len = termInfo.length() - 1;
			param.put("schoolYear", termInfo.substring(0, len));
			param.put("term", termInfo.substring(len));
			param.put("schoolId", getXxdm(req));
			
			response.put("weekStartTime", weekWorkService.createOrGetStartWeekByTermInfo(param));
			setPromptMessage(response, "0", "查询成功");
			
		} catch(CommonRunException e) {
			setPromptMessage(response, "-1", e.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "系统错误");
		}
		return response;
	}
	
	@RequestMapping(value = "/getCurrentTermWeek",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCurrentTermWeek(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			String termInfo = request.getString("termInfo");
			if(StringUtils.isBlank(termInfo)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			int subLen = termInfo.length() - 1;
			String schoolYear = termInfo.substring(0, subLen);
			String term = termInfo.substring(subLen);
			
			JSONObject param = new JSONObject();
			param.put("schoolId", getXxdm(req));
			param.put("teacherId", req.getSession().getAttribute("accountId"));
			param.put("schoolYear", schoolYear);
			param.put("term", term);
			
			Calendar cstart = Calendar.getInstance();
			Date startTimeTemp = weekWorkService.createOrGetStartWeekByTermInfo(param);
			cstart.setTime(startTimeTemp);
			int diffDay = cstart.get(Calendar.DAY_OF_WEEK); 
			if(diffDay==0){
				cstart.add(Calendar.DAY_OF_MONTH, -6); 
			}else{
				cstart.add(Calendar.DAY_OF_MONTH, 1-diffDay);  
			}
			
			Double diffTemp = Math.ceil((new Date().getTime()-cstart.getTime().getTime())/((float)1000*3600*24*7));
			param.put("noWeekNum", diffTemp + 1); //去除下一周
			List<JSONObject> maxweekObj = weekWorkService.getMaxWeekFromRecord(param); //不包括下一周的
			Integer maxweek2 = -1;
			int nextFlag = 0;
			for(JSONObject obj : maxweekObj) {
				if(obj.get("week") == null) {
					continue;
				}
				
				String content = obj.getString("content");
				if(StringUtils.isNotBlank(content)){
					maxweek2 = obj.getInteger("week");
					break;
				}
			}
			
			Integer maxweek3 = -1;
			maxweekObj = weekWorkService.getMaxWeekFromRecord2(param); //不包括下一周的  new version 
			for(JSONObject obj:maxweekObj){
				if(obj.get("week") == null) {
					continue;
				}
				
				String content=obj.getString("content");
				if(StringUtils.isNotBlank(content)) {
					maxweek3 = obj.getInteger("week");
					if (maxweek3 > maxweek2 ) {
						maxweek2 = maxweek3;
					}
					break;
				}
			}
			
			boolean isFillMan = weekWorkService.getFillManInfo(param);
			int loopNum = 0;
			if (diffTemp > 21) {
				loopNum = 21;
			} else {
				if (isFillMan) {
					loopNum = diffTemp.intValue();
				} else {
					// 没有权限，则如果填写的周报中 最大周 > 当前周-1 则按当前周显示 14(填写的周次)>14-1
					// 则按当前周4显示
					if (maxweek2 != null && maxweek2 > diffTemp.intValue() - 1) {
						loopNum = diffTemp.intValue();
					} else { // 没有权限，如果 最大周<= 当前周-1则按当前周-1显示 3（填写的周次）>4-1
								// 则按当前周4-1显示
						loopNum = diffTemp.intValue();
						nextFlag = 1; // 显示下一周
					}
				}
			}
			List<JSONObject> dataTemp = new ArrayList<JSONObject>();
			int addNum = 0;
			if ((loopNum < 21 && loopNum > 0) || loopNum <= 0) { // 起始周时间的周一<当前时间，则默认显示空，下拉可选起始周时间对应的第一周时间
				if (loopNum <= 0) {
					loopNum = 0;
				}
				addNum = 1; // 能够操作下一周
			}
			int temp = 0;
			for (int i = 1; i <= loopNum + addNum; i++) {
				JSONObject o = new JSONObject();
				int firstMonth = cstart.get(Calendar.MONTH) + 1;
				int firstDay = cstart.get(Calendar.DAY_OF_MONTH);
				temp =  cstart.getActualMaximum(Calendar.DAY_OF_MONTH);
				cstart.add(Calendar.DAY_OF_MONTH, 7);
				int lastMonth = cstart.get(Calendar.MONTH) + 1;
				int lastDay = cstart.get(Calendar.DAY_OF_MONTH) - 1;
				if (lastDay == 0) {
					lastMonth = firstMonth;
					lastDay = temp;
				}
				o.put("value", i);
				o.put("text", "第" + i + "周(" + firstMonth + "." + firstDay + " - " + lastMonth + "." + lastDay + ")");
				if (isFillMan) {
					if (loopNum == i) {
						o.put("selected", true);
					} else {
						o.put("selected", false);
					}
				} else {
					if ((nextFlag == 1 || diffTemp > 21) && maxweek2 != -1) {
						if (maxweek2 == i) {
							o.put("selected", true);
						} else {
							o.put("selected", false);
						}
					} else {
						if (loopNum == i) {
							o.put("selected", true);
						} else {
							o.put("selected", false);
						}
					}
				}
				dataTemp.add(o);
			}
			
			List<JSONObject> data = new ArrayList<JSONObject>();
			for (int j = dataTemp.size(); j > 0; j--) {
				data.add(dataTemp.get(j - 1));
			}
			response.put("data", data);
			setPromptMessage(response, "0", "");
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "系统错误");
		}
		return response;
	}
	
	@RequestMapping(value = "/getWeeklyRecordList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getWeeklyRecordList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			Integer weekNum = request.getInteger("weekNum");
			if(weekNum == null){  // 下拉框默认为空的情况。
				setPromptMessage(response, "0", "查询成功");
				return response;
			}
			
			String termInfoId = request.getString("termInfo");
			if(StringUtils.isBlank(termInfoId)){
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			
			int len = termInfoId.length() - 1;
			param.put("termInfoId", termInfoId);
			param.put("schoolYear", termInfoId.substring(0, len));
			param.put("term", termInfoId.substring(len));
			
			param.put("schoolId", getXxdm(req));
			param.put("accountId", req.getSession().getAttribute("accountId"));
			param.put("weekNum", weekNum);
			
			boolean isMoudleManager = isMoudleManager(req, "cs1020");
			param.put("isMoudleManager", isMoudleManager);
			List<JSONObject> data = weekWorkService.getWeeklyRecordList(param);
			response.put("data", data);
			setPromptMessage(response, "0", "查询成功");
		} catch(CommonRunException e) {
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "系统错误");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getWeeklyRecordDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getWeeklyRecordDetail(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			String termInfo = request.getString("termInfo");
			Integer weekNum = request.getInteger("weekNum");
			String type = request.getString("type");
			String departmentId = request.getString("departmentId");
			if(StringUtils.isBlank(termInfo) || weekNum == null 
					|| StringUtils.isBlank(departmentId)){
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			int len = termInfo.length() - 1;
			param.put("schoolYear", termInfo.substring(0, len));
			param.put("term", termInfo.substring(len));
			
			param.put("schoolId", getXxdm(req));
			param.put("weekNum", weekNum);
			
			param.put("departmentId", departmentId);
			param.put("type", type);
			
			JSONObject data = weekWorkService.updateOrGetWeeklyRecordDetail(param);
			
			if ("1".equals(data.getString("version"))) {
				response.put("version", "1");
			}else {
				response.put("version", "0");
			}
			response.put("data", data);
			setPromptMessage(response, "0", "查询成功");
		} catch(CommonRunException e) {
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/updateWeeklyRecordDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateWeeklyRecordDetail(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String msgId = UUIDUtil.getUUID().replace("-", "");
		try {
			String weekname = request.getString("departmentName");
			weekname = weekname == null ? "" : weekname;
			
			String termInfo = request.getString("termInfo");
			Integer weekNum = request.getInteger("weekNum");
			Integer version = request.getInteger("version");
			String departmentId = request.getString("departmentId");
			if(StringUtils.isBlank(termInfo) || weekNum == null || version == null
					|| StringUtils.isBlank(departmentId)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
		    String schoolId=  getXxdm(req);
			int len = termInfo.length() - 1;
			request.put("schoolYear", termInfo.substring(0, len));
			request.put("term", termInfo.substring(len));
			request.put("schoolId", schoolId);
			Long accountId =  (Long)req.getSession().getAttribute("accountId");
			request.put("accountId", accountId);
			weekWorkService.updateWeeklyRecordDetail(request);
			setPromptMessage(response, "0", "保存成功");
 
			School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
		 
			Account account = commonDataService.getAccountById(Long.parseLong(schoolId), accountId, termInfo);
			List<Long> moduleList =  commonDataService.getModuleManagers("cs1020", termInfo, Long.parseLong(schoolId));
			List<User>  userList = commonDataService.getUserBatch(Long.parseLong(schoolId), moduleList, termInfo);
			List<Long> accountIdList = new ArrayList<Long>();
			for (int i = 0; i < userList.size(); i++) {
				User user = userList.get(i);
				accountIdList.add(user.getAccountPart().getId());
			}
			List<Account> accountList = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIdList, termInfo);
			 
			JSONObject departMent = weekWorkService.getDepartmentById(request);
			
			List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>(); 
			for (int i = 0; i < accountList.size(); i++) {
				Account account2 = accountList.get(i);
				JSONObject msgCenterReceiver = new JSONObject();
				msgCenterReceiver.put("userId", account2.getExtId());
				msgCenterReceiver.put("userName",account2.getName());
				msgCenterReceiversArray.add(msgCenterReceiver);
			}
			
			accountIdList.clear();
			accountIdList.add(accountId);
			List<Account> accountList2 = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIdList, termInfo);
			Account account2 = accountList2.get(0);

			
			JSONObject msgCenterPayLoad = new JSONObject();
			JSONObject msg = new JSONObject();
			msg.put("msgId", msgId);
			msg.put("msgTitle", "周工作通知");
			msg.put("msgContent", account.getName() + "提交了工作周报");
 
			msg.put("msgUrlPc", ""+msgUrlPc);
			msg.put("msgUrlApp", ""+msgUrlApp.replace("#weeknum#", String.valueOf(weekNum)).replace("#department#", departmentId).replace("#weekname#", weekname));
			msg.put("msgOrigin", "周工作通知");
			msg.put("msgTypeCode", "ZGZ");
			msg.put("msgTemplateType", "ZGZ");
			
			msg.put("schoolId", school.getExtId());
			
			msg.put("creatorName", account2.getName());
 
			JSONObject first = new JSONObject();
			first.put("value", "您收到了新的工作汇报");

			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", account.getName());

			JSONObject keyword2 = new JSONObject();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			keyword2.put("value", format.format(new Date()));//日期

			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", departMent.getString("departmentName") + "第"+weekNum+"周工作报表");

			JSONObject remark = new JSONObject();
			remark.put("value", "请及时查阅。");
			
			JSONObject data = new JSONObject();
			data.put("first", first);
			data.put("keyword1", keyword1);
			data.put("keyword2", keyword2);
			data.put("keyword3", keyword3);
			data.put("remark", remark);
			data.put("url", msg.get("msgUrlApp"));
			msg.put("msgWxJson", data);
			
			msgCenterPayLoad.put("msg", msg);
			msgCenterPayLoad.put("receivers", msgCenterReceiversArray);
			KafkaUtils.sendAppMsg(kafkaUrl, msgId, msgCenterPayLoad, "ZGZ", clientId, clientSecret);
		} catch(CommonRunException e) {
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			logger.info("updateWeeklyRecordDetail => exception : " + e.toString());
			setPromptMessage(response, "-2", "系统错误");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/delWeeklyRecordDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delWeeklyRecordDetail(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String termInfo = param.getString("termInfo");
		if(StringUtils.isNotBlank(termInfo)){
			param.put("schoolYear", termInfo.substring(0, 4));
			param.put("term", termInfo.substring(4));
		}
		param.put("schoolId", getXxdm(req));
		try {
			weekWorkService.delWeeklyRecordDetail(param);
			setPromptMessage(response, "0", "删除成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "系统错误");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 获取部门
	 * @param req
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value = "/getDepartmentInfoByDataBase",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDepartmentInfoByDataBase(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
			param.put("teacherId", accountId);
			param.put("schoolId", getXxdm(req));
			List<JSONObject> data = weekWorkService.getDepartmentInfoByDataBase(param);
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		} catch(CommonRunException e){
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		
		return response;
	}
	/**
	 * 获取所有部门
	 * @param req
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value = "/getDepartmentList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDepartmentList(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		param.put("schoolId", getXxdm(req));
		try {
			List<JSONObject> data = weekWorkService.getDepartmentList(param);
			setPromptMessage(response, "0", "查询成功");
			response.put("data", data);
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		
		return response;
	}
	
	@RequestMapping(value = "/insertTerminfoAndStartWeek",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject insertTerminfoAndStartWeek(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			String termInfo = request.getString("termInfo");
			Date weekStartTime = request.getDate("weekStartTime");
			if(StringUtils.isBlank(termInfo) || weekStartTime == null) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			int len = termInfo.length() - 1;
			JSONObject param = new JSONObject();
			param.put("schoolYear", termInfo.substring(0, len));
			param.put("term", termInfo.substring(len));
			param.put("schoolId", getXxdm(req));
			param.put("weekStartTime", weekStartTime);
			weekWorkService.insertTerminfoAndStartWeek(param);
			setPromptMessage(response, "0", "保存成功");
		} catch (CommonRunException e) {
			setPromptMessage(response, "-1", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "系统错误");
		}
		return response;
	}
	/**
	 * 获取所有填报人员
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value = "/getReportingPersonList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getReportingPersonList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			String selectedSemester = request.getString("selectedSemester");
			if(StringUtils.isBlank(selectedSemester)){
				selectedSemester = getCurXnxq(req);
			}
			JSONObject param = new JSONObject();
			param.put("schoolId", getXxdm(req));
			param.put("termInfoId", selectedSemester);
			List<JSONObject> data = weekWorkService.getReportingPersonList(param);
			response.put("data", data);
			
			setPromptMessage(response, "0", "查询成功");
		} catch(CommonRunException e) {
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "查询失败");
		}
		return response;
	}
	/**
	 * 获取所有教师列表
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value = "/getAllTeacherList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAllTeacherList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String selectedSemester = request.getString("selectedSemester");
			if(StringUtils.isEmpty(selectedSemester)){
				selectedSemester = getCurXnxq(req);
			}
			
			JSONObject param = new JSONObject();
			param.put("selectedSemester", selectedSemester);
			param.put("teacherName", request.getString("teacherName"));//2016-12-06-dlm
			param.put("schoolId", getXxdm(req));
		
//			School school = getSchool(req,selectedSemester);
//			param.put("school", school);
			List<JSONObject> data = weekWorkService.getAllTeacherList(param);
			if (data != null) {
				response.put("data", data);
				setPromptMessage(response, "0", "查询成功");
			} else {
				setPromptMessage(response, "-1", "查询失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 更新填报人员
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value = "/updateReportPerson",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateReportPerson(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			param.put("teacherIds", request.getJSONArray("teacherId"));
			param.put("departmentName", request.getString("departmentName"));
			param.put("departmentId", request.getString("departmentId"));
			param.put("schoolId", getXxdm(req));
			param.put("content", StringEscapeUtils.unescapeHtml3(request.getString("content")));
			weekWorkService.updateReportPerson(param);
			
			setPromptMessage(response, "0", "保存成功");
		} catch(CommonRunException e) {
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "设置失败");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 删除填报人员
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value = "/deleteReportPerson",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteReportPerson(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			param.put("schoolId", getXxdm(req));
			param.put("departmentId", request.getString("departmentId"));
			weekWorkService.deleteReportPerson(param);
			
			setPromptMessage(response, "0", "删除成功");
		} catch(CommonRunException e) {
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "删除失败");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/insertReportStyle", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject insertReportStyle(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();

		try {
			String schoolId = getXxdm(req);
			List<JSONObject> weekWorkDetails = (List<JSONObject>) request.get("weekWorkDetails");
			param.put("weekWorkDetails", weekWorkDetails);
			param.put("schoolId", schoolId);
			param.put("departmentId", request.getString("departmentId"));
			int i = weekWorkService.insertReportStyle(param);
			if (i > 0) {
				setPromptMessage(response, "0", "设置成功");
			} else if(i==-2){
				setPromptMessage(response, "-2", "设置类别或列头名称不能相同！");
			}else{
				setPromptMessage(response, "-1", "设置失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "设置失败");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getReportStyle", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getReportStyle(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			param.put("schoolId", getXxdm(req));
			param.put("departmentId", request.getString("departmentId"));
			response.put("data", weekWorkService.getDepartmentById(param));
			setPromptMessage(response, "0", "");
		} catch(CommonRunException e){
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "设置失败");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/deleteReportStyle",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteReportStyle(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			JSONObject param = new JSONObject();
			param.put("schoolId", getXxdm(req));
			String departmentId = request.getString("departmentId");
			param.put("departmentId", departmentId);
			param.put("sortId", request.getString("sortId"));
			weekWorkService.deleteReportStyle(param);
			setPromptMessage(response, "0", "删除成功");
		} catch (CommonRunException e){
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "删除失败");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getAppCurrentTermWeek",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAppCurrentTermWeek(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		List<JSONObject> data = new ArrayList<JSONObject>();
		JSONObject param = new JSONObject();
		Long schoolId = request.getLong("schoolId");
		param.put("schoolId", schoolId);
		String selectedSemester = commonDataService.getCurTermInfoId(schoolId);
		School school = commonDataService.getSchoolById(schoolId,selectedSemester);
		String termInfo = commonDataService.getCurrentXnxq(school);
		if(StringUtils.isNotBlank(termInfo)){
			int year = Integer.parseInt(termInfo.substring(0, 4));
			int term = Integer.parseInt(termInfo.substring(4));
			param.put("schoolYear", termInfo.substring(0, 4));
			param.put("term", termInfo.substring(4));
			String termZH = "";
			if(term==1)termZH = "一";
			if(term==2)termZH = "二";
			response.put("termInfo", year+"-"+(year+1)+"学年第"+termZH+"学期");
		}
		Long userId = request.getLong("userId");
		User user = commonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
		String teacherId ="";
		if(user!=null){
			teacherId = String.valueOf(user.getAccountPart().getId());
		}
		param.put("teacherId", teacherId);
		try {
			int num = 0;//weekWorkService.getFillManInfo(param);
			 
			Date current = new Date();
			String weekStartTime = weekWorkService.getCurrentTermWeek(param);
			if(StringUtils.isBlank(weekStartTime)){
				setPromptMessage(response, "-1", "请设置起始周时间");
			}else{
				setPromptMessage(response, "0", "保存成功");
				Calendar cstart = Calendar.getInstance();
				Date startTimeTemp = DateUtil.parseDateDayFormat(weekStartTime);
				cstart.setTime(startTimeTemp);
				int diffDay = cstart.get(Calendar.DAY_OF_WEEK);
				if(diffDay==0){
					cstart.add(Calendar.DAY_OF_MONTH, -6);
				}else{
					cstart.add(Calendar.DAY_OF_MONTH, 1-diffDay);
				}
				Double diffTemp = Math.ceil((current.getTime()-cstart.getTime().getTime())/((float)1000*3600*24*7));
				param.put("noWeekNum", diffTemp+1); //去除下一周
				List<JSONObject> maxweekObj = weekWorkService.getMaxWeekFromRecord(param); //不包括下一周的
				Integer maxweek2=-1;
				for(JSONObject obj:maxweekObj){
					if(obj.containsKey("week")&& obj.get("week")!=null){
						String content=obj.getString("content");
						if(content!=null&& StringUtils.isNotBlank(content)){
							maxweek2=obj.getInteger("week");
							break;
						}
					}else{
						continue;
					}
				}
				
				Integer maxweek3=-1;
				maxweekObj = weekWorkService.getMaxWeekFromRecord2(param); //不包括下一周的  new version 
				for(JSONObject obj:maxweekObj){
					if(obj.containsKey("week")&& obj.get("week")!=null){
						String content=obj.getString("content");
						if(content!=null&& StringUtils.isNotBlank(content)){
							maxweek3=obj.getInteger("week");
							if (maxweek3 > maxweek2 ) {
								maxweek2 = maxweek3;
							}
							break;
						}
					}else{
						continue;
					}
				}
				
				List<JSONObject> dataTemp = new ArrayList<JSONObject>();
				int loopNum = 0;
				int nextFlag=0;
				if(diffTemp>21){
					loopNum = 21;
				}else{ 
					if(num>0){
						loopNum=diffTemp.intValue();
						}else{
							//没有权限，则如果填写的周报中  最大周 > 当前周-1  则按当前周显示 14(填写的周次)>14-1  则按当前周4显示
							if(maxweek2!=null&&maxweek2>diffTemp.intValue()-1){
								loopNum=diffTemp.intValue();
							}else{ //没有权限，如果 最大周<= 当前周-1则按当前周-1显示    3（填写的周次）>4-1  则按当前周4-1显示
								loopNum=diffTemp.intValue();
								nextFlag=1; //显示下一周
						}
					}
				}
				int addNum=0;
				if( (loopNum<21 && loopNum>0) || loopNum<=0){ //起始周时间的周一<当前时间，则默认显示空，下拉可选起始周时间对应的第一周时间
					if(loopNum<=0){
						loopNum=0;
					}
					addNum=1;  //能够操作下一周
				}
				for(int i=1;i<=loopNum+addNum;i++){
					JSONObject o = new JSONObject();
					int firstMonth = cstart.get(Calendar.MONTH)+1;
					int firstDay = cstart.get(Calendar.DAY_OF_MONTH);
					cstart.add(Calendar.DAY_OF_MONTH, 7);
					int lastMonth = cstart.get(Calendar.MONTH)+1;
					int lastDay = cstart.get(Calendar.DAY_OF_MONTH)-1;
					if (lastDay==0) {
						lastMonth = Math.abs(12 - (lastMonth - 1) );
						lastDay=cstart.getActualMaximum(Calendar.DAY_OF_MONTH);
					}
					
					o.put("value", i);
					o.put("text0", "第"+i+"周");
					o.put("text1", "("+firstMonth+"."+firstDay+"-"+lastMonth+"."+lastDay+")");
					if(num>0){
						if(loopNum==i){
							o.put("selected", true);
						}else{
							o.put("selected", false);
						}
					}else{
						if((nextFlag==1||diffTemp>21) && maxweek2!=-1){
							if(maxweek2==i){
								o.put("selected", true);
							}else{
								o.put("selected", false);
							}
						}else{
							if(loopNum==i){
								o.put("selected", true);
							}else{
								o.put("selected", false);
							}
						}
					}
					dataTemp.add(o);
				}
				for(int j=dataTemp.size();j>0;j--){
					data.add(dataTemp.get(j-1));
				}
			}
			response.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "系统错误");
		}
		return response;
	}
	
	@RequestMapping(value = "/getAppRecordList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAppRecordList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		Long schoolId = request.getLong("schoolId");
		String selectedSemester = commonDataService.getCurTermInfoId(schoolId);
		School school = commonDataService.getSchoolById(schoolId,selectedSemester);
		if(school!=null){
			String termInfo = commonDataService.getCurrentXnxq(school);
			if(StringUtils.isNotBlank(termInfo)){
				param.put("schoolYear", termInfo.substring(0, 4));
				param.put("term", termInfo.substring(4));
			}
			param.put("termInfoId", termInfo);
		}
		param.put("schoolId", schoolId);
		param.put("weekNum", request.getString("weekNum"));
		try {
			List<JSONObject> data = weekWorkService.getWeeklyRecordList(param);
			List<JSONObject> filter = new ArrayList<JSONObject>();
			for (int i = 0; i < data.size(); i++) {
				JSONObject object = data.get(i);
				 Date date = object.getDate("submitTime");
				 if (date!=null) {
					 filter.add(object);
				}
			}
			setPromptMessage(response, "0", "查询成功");
			 
			response.put("data", filter);
		} catch (Exception e) {
			setPromptMessage(response, "-1", "系统错误");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getAppRecordDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAppRecordDetail(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		logger.info("getAppRecordDetail is call");
		try {
			JSONObject param = new JSONObject();
			String userId = request.getString("userId");
			if (userId == null || userId.length() == 0) {
				setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
				return response;
			}
			Long schoolId = request.getLong("schoolId");
			/**
			 * 判断身份
			 */
			User user = commonDataService.getUserById(Long.valueOf(schoolId), Long.valueOf(userId));
			if(user.getUserPart().getRole() == T_Role.Parent ||
				user.getUserPart().getRole() == T_Role.Student) {
				setPromptMessage(response, ERRCODE_NO_PERMISSION, PROMPT_NO_PERMISSION);
				return response;
			}
			
			String termInfo = commonDataService.getCurTermInfoId(schoolId);
//			List<Long> moduleList =  commonDataService.getModuleManagers("cs1020", termInfo, schoolId);
//			List<User>  userList = commonDataService.getUserBatch(schoolId, moduleList, termInfo);
//			boolean hasPermission = false;
//			if(userList != null && userList.size()>0) {
//				for(User eachUser : userList) {
//					if(eachUser.getUserPart().getId() == Long.valueOf(userId)) {
//						hasPermission = true;
//						break;
//					}
//				}
//			}
//			if(!hasPermission) {
//				setPromptMessage(response, ERRCODE_NO_PERMISSION, PROMPT_NO_PERMISSION);
//				return response;
//			}
			 
			Integer weekNum = request.getInteger("weekNum");
			String departmentId = request.getString("departmentId");
			if(schoolId == null || weekNum == null || StringUtils.isBlank(departmentId)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			
			int len = termInfo.length() - 1;
			param.put("schoolYear", termInfo.substring(0, len));
			param.put("term", termInfo.substring(len));
			param.put("schoolId", schoolId);
			param.put("weekNum", weekNum);
			param.put("departmentId", departmentId);
			
			JSONObject data = weekWorkService.getAppRecordDetail(param);
			if(data != null) {
				if ("1".equals(data.getString("version"))) {
					response.put("version", "1");
				}else {
					response.put("version", "0");
				}
			} else {
				setPromptMessage(response, ERRCODE_DELETED, PROMPT_DELETED);
				return response;
			}
			
			response.put("data", data);
			setPromptMessage(response, "0", "查询成功");
		} catch(CommonRunException e) {
			setPromptMessage(response, String.valueOf(e.getCode()), e.getMessage());
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		
		return response;
	}
	/**
	 * 获取所有部门下的所有类别、列头和周报内容
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 * @author zhh
	 */
	@RequestMapping(value = "/getWeeklyRecordDetailAllDepartment", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getWeeklyRecordDetailAllDepartment(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		
		try {
			String termInfo = request.getString("termInfo");
			String weekNum = request.getString("weekNum");
			String schoolId = getXxdm(req);
			if(StringUtils.isNotBlank(termInfo)){
				param.put("schoolYear", termInfo.substring(0, 4));
				param.put("term", termInfo.substring(4));
			}
			String weekDate = request.getString("weekDate");
			param.put("weekDate", weekDate);
			param.put("weekNum", weekNum);
			param.put("schoolId", schoolId);
			param.put("version", "1");
			
			JSONObject data = weekWorkService.getWeeklyRecordDetailAllDepartment(param);
			if (data != null && data.size()>0) {
				if(data.containsKey("weekWorkTable")&&data.get("weekWorkTable")!=null && ((List<JSONObject>)data.get("weekWorkTable")).size()>0){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "暂无周工作报表记录");
				}
			} else {
				setPromptMessage(response, "-1", "暂无周工作报表记录");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-2", "系统错误");
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * html字符串导出成word
	 * @param request
	 * @param response
	 * @author zhh
	 */
	@RequestMapping(value="/exportWordByHtml")
    @ResponseBody   
    public void exportWordByHtml(HttpServletRequest request,HttpServletResponse response)throws Exception{
		 java.io.BufferedOutputStream bos = null;
	     java.io.BufferedInputStream bis = null;  
		try {
			 response.setContentType("text/html;charset=UTF-8");   
		     request.setCharacterEncoding("UTF-8"); 
		     byte[] bytes = null;   
		    
	         String fileN = (request.getParameter("fileName")+".doc").trim().replace(" ", "");
			 String fileName = URLDecoder.decode(fileN,"UTF-8");
			 //前端要加 <meta charset="utf-8">
			 String weekWorkAllDepartmentHtml= request.getParameter("weekWorkAllDepartmentHtml");
			 /*POIFSFileSystem poifs = new POIFSFileSystem();    
			 DirectoryEntry directory = poifs.getRoot();    
			 DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);*/    
		
			    response.reset();  
			    String downLoadName = new String(fileName.getBytes("gbk"), "iso8859-1"); 
			    response.addHeader("Content-Type", "text/html; charset=UTF-8");  
			    response.setHeader("Content-Disposition",  
				         "attachment;filename=" +downLoadName);
				response.setContentType("application/msword;charset=UTF-8");
				bytes = weekWorkAllDepartmentHtml.getBytes("UTF-8");
				bos = new BufferedOutputStream(response.getOutputStream());   
	            bos.write(bytes);
	            /*while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {   
	                bos.write(buff, 0, bytesRead);   
	            }  */ 
				//poifs.writeFilesystem(ostream);    
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {   
            if (bis != null)   
                bis.close();   
            if (bos != null)   
                bos.close();   
        }   
	}
	
	
	@RequestMapping(value="/noticeWeekWork")
    @ResponseBody   
    public JSONObject noticeWeekWork(@RequestBody JSONObject request, HttpServletRequest req,HttpServletResponse resp){
		JSONObject response = new JSONObject();
		Integer weekNum = request.getInteger("weekNum"); 
		String schoolId =  getXxdm(req);
		String termInfo = request.getString("termInfo");
 
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("schoolYear", termInfo.substring(0, 4));
		param.put("term", termInfo.substring(4, 5));
		param.put("weekNum", weekNum);

		List<JSONObject>  list = weekWorkService.getNoticePerson(param);
		List<Long> accountList = new ArrayList<Long>();
		logger.info("noticeWeekWork==> notice person list = " + list); 
		for (int i = 0; i < list.size(); i++) {
			JSONObject teacher = list.get(i);
			accountList.add(teacher.getLong("teacherId"));
		}
		School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
		List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountList, termInfo);
		
		Long accountId =  (Long)req.getSession().getAttribute("accountId");
		accountList.clear();
		accountList.add(accountId);
		List<Account> accounts2 = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountList, termInfo);
	    Account account2 = accounts2.get(0);
		
		for (int i = 0; i <accounts.size(); i++) {
			List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
			Account account = accounts.get(i);
			JSONObject  msgCenterReceiver = new JSONObject();
			msgCenterReceiver.put("userId", account.getExtId());
			msgCenterReceiver.put("userName", account.getName());
			msgCenterReceiversArray.add(msgCenterReceiver);
			String msgId = UUIDUtil.getUUID().replace("-", "");
			JSONObject msgCenterPayLoad = new JSONObject();
			JSONObject msg = new JSONObject();
			msg.put("msgId", msgId);
			msg.put("msgTitle", "周工作催交提醒");
			msg.put("msgContent",  "请及时提交周报。");

			msg.put("msgUrlPc", ""+msgId);
			msg.put("msgUrlApp", ""+msgId);
			msg.put("msgOrigin", "周工作催交提醒");
			msg.put("msgTypeCode", "ZGZ");
			msg.put("msgTemplateType", "ZGZ");
			msg.put("schoolId", school.getExtId());
			msg.put("creatorName", account2.getName());

			JSONObject first = new JSONObject();
			first.put("value", "您收到了新的周报催交提醒");

			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", account.getName());

			JSONObject keyword2 = new JSONObject();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			keyword2.put("value", format.format(new Date()));//日期

			JSONObject keyword3 = new JSONObject();
			keyword3.put("value",  "第"+weekNum+"周工作报表"  );

			JSONObject remark = new JSONObject();
			remark.put("value", "请及时填报。");
			
			JSONObject data = new JSONObject();
			data.put("first", first);
			data.put("keyword1", keyword1);
			data.put("keyword2", keyword2);
			data.put("keyword3", keyword3);
			data.put("remark", remark);
			msg.put("msgWxJson", data);
			msgCenterPayLoad.put("msg", msg);
			msgCenterPayLoad.put("receivers", msgCenterReceiversArray);// 按照组来发送催收消息
			try {
				KafkaUtils.sendAppMsg(kafkaUrl, msgId, msgCenterPayLoad, "ZGZ", clientId, clientSecret);
				setPromptMessage(response, "200", "通知成功!");
			} catch (Exception e) {
				e.printStackTrace();
				setPromptMessage(response, "-1", "通知失败!");
			}
		}
 
		return response;
	}
	
	
	
	public String[][] getTeacherIdNames(School school,String selectedSemester){
		List<Account> ll = commonDataService.getAllSchoolEmployees(school,selectedSemester,"");
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
 
	
 
}
