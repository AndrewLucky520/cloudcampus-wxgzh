package com.talkweb.leaveapply.action;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;

@Controller
@RequestMapping("/leaveManage")
public class LeaveApplicationAction  extends LeaveApplicationBaseAction{
	/**
	 * 获取登录人员的角色 0 管理员，1 审核人员，2 一般老师
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getLeaveRole" )
	@ResponseBody
	public JSONObject getLeaveRole(HttpServletRequest req, HttpServletResponse res){
		JSONObject param = new JSONObject();
		String schoolId = getXxdm(req) ;
		String termInfoId = getCurXnxq(req);
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		param.put("schoolId", schoolId);
		param.put("termInfoId", termInfoId);
		param.put("accountId", accountId);
		
 		boolean flag = isMoudleManager(req, "cs1029");
		 //boolean flag = true;
		if(flag){
			param.put("isAdmin", 1);
		}else {
			param.put("isAdmin", 0);
		}

		return getLeaveRole(param);
	}
	
	 
	//教师空间请假信息
	@RequestMapping(value = "/getLeaveApplyInfoTeacherZone" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveApplyInfoTeacherZone(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
 
		Long  schoolId = request.getLong("schoolId");
		String termInfoId = rbConstant.getString("currentTermInfo"); 
		Long userId = request.getLong("userId");
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("termInfoId", termInfoId);
		User user = commonDataService.getUserById(schoolId, userId);
		if (user == null) {
			setPromptMessage(response, "-1", "用户不存在");
			return response;
		}
		Long teacherId =  user.getAccountPart().getId() ;
		param.put("teacherId", teacherId);
 
		List<JSONObject> list = leaveApplicationService.getLeaveApplyPendingForTeacherZone(param);
		
		List<JSONObject>  typeList = leaveApplicationService.getLeaveTypeList(param);
		if(typeList==null || typeList.size() == 0){
			param.put("schoolId", "ALL");
			typeList = leaveApplicationService.getLeaveTypeList(param);
			param.put("schoolId", schoolId);
		}
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < typeList.size(); i++) {
			JSONObject object = typeList.get(i);
			map.put(object.getString("types"), object.getString("typeName"));
		}
		DateFormat simple = new SimpleDateFormat("yyyy-MM-dd");
		 
		if (list!=null &&  list.size() > 0) {//存在未完成的请假
			setPromptMessage(response, "1", "用户存在未完成请假流程");
			Set<Long> teacherSet = new HashSet<Long>();
			for (int i = 0; i < list.size(); i++) {
				JSONObject leave = list.get(i);
				leave.put("typeName", map.get(leave.getString("leaveType")));
				leave.put("applyDate", simple.format(leave.getDate("applyDate")));
				leave.put("applyAccountName", user.getAccountPart().getName());
				JSONArray  levelList = leave.getJSONArray("levelList");
				for (int j = 0; j < levelList.size(); j++) {
				   JSONArray teacherList = levelList.getJSONObject(j).getJSONArray("teacherList");
				   for (int k = 0; k < teacherList.size(); k++) {
					   JSONObject teacher = teacherList.getJSONObject(k);
					   teacherSet.add(teacher.getLong("teacherId"));
				   }
				}
			}
			
			List<Account> accounts = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(teacherSet), termInfoId);
			Map<Long , String> teacherMap = new HashMap<Long , String>();
			for (int i = 0; i < accounts.size(); i++) {
				Account account = accounts.get(i);
				teacherMap.put(account.getId(), account.getName());
			}
			for (int i = 0; i < list.size(); i++) {
				JSONArray  levelList = list.get(i).getJSONArray("levelList");
				for (int j = 0; j < levelList.size(); j++) {
				   JSONArray teacherList = levelList.getJSONObject(j).getJSONArray("teacherList");
				   for (int k = 0; k < teacherList.size(); k++) {
					   JSONObject teacher = teacherList.getJSONObject(k);
					   teacher.put("teacherName", teacherMap.get( teacher.getLong("teacherId")));
					   if (teacher.getDate("processDate") != null) {
						   teacher.put("processDate", simple.format(teacher.getDate("processDate")));
					   }
					 
				   }
				}
				
			}
			response.put("data", list);
			
			return response;
		}else {
			JSONObject result = leaveApplicationService.getLeaveApplyFinshedForTeacherZone(param);// 获取最近的一次完成的请假
			if (result!=null && result.getString("applicationId") !=null) {
				setPromptMessage(response, "2", "用户存在完成请假流程");
				Set<Long> teacherSet = new HashSet<Long>();
				result.put("typeName", map.get(result.getString("leaveType")));
				result.put("applyDate", simple.format(result.getDate("applyDate")));
				result.put("applyAccountName", user.getAccountPart().getName());
					JSONArray  levelList = result.getJSONArray("levelList");
					for (int j = 0; j < levelList.size(); j++) {
					   JSONArray teacherList = levelList.getJSONObject(j).getJSONArray("teacherList");
					   for (int k = 0; k < teacherList.size(); k++) {
						   JSONObject teacher = teacherList.getJSONObject(k);
						   teacherSet.add(teacher.getLong("teacherId"));
					   }
					}
				List<Account> accounts = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(teacherSet), termInfoId);
				Map<Long , String> teacherMap = new HashMap<Long , String>();
				for (int i = 0; i < accounts.size(); i++) {
					Account account = accounts.get(i);
					teacherMap.put(account.getId(), account.getName());
				}
 
					for (int j = 0; j < levelList.size(); j++) {
					   JSONArray teacherList = levelList.getJSONObject(j).getJSONArray("teacherList");
					   for (int k = 0; k < teacherList.size(); k++) {
						   JSONObject teacher = teacherList.getJSONObject(k);
						   teacher.put("teacherName", teacherMap.get( teacher.getLong("teacherId")));
						   if (teacher.getDate("processDate") != null) {
							   teacher.put("processDate", simple.format(teacher.getDate("processDate")));
						   }
					   }
					}
					
			    List<JSONObject> leaveList = new ArrayList<JSONObject>();
			    leaveList.add(result);
				response.put("data", leaveList);
				return response;
			}else{
				Set<Long> teacherSet = new HashSet<Long>();
				List<JSONObject> auditorList = leaveApplicationService.getLeaveApplyAuditorForTeacherZone(param);//获取到审核人列表
				if (auditorList !=null && auditorList.size() > 0) {
					setPromptMessage(response, "3", "用户存在审核人员信息");
					for (int i = 0; i < auditorList.size(); i++) {
						  JSONArray levelList =  auditorList.get(i).getJSONArray("levelList");
						  for (int j = 0; j <  levelList.size(); j++) {
							  JSONArray   teacherList = levelList.getJSONObject(j).getJSONArray("teacherList");
							  for (int k = 0; k < teacherList.size(); k++) {
								  JSONObject teacher = teacherList.getJSONObject(k);
								  teacherSet.add(teacher.getLong("teacherId"));
							 }
						  }
					}
					List<Account> accounts = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(teacherSet), termInfoId);
					Map<Long , String> teacherMap = new HashMap<Long , String>();
					for (int i = 0; i < accounts.size(); i++) {
						Account account = accounts.get(i);
						teacherMap.put(account.getId(), account.getName());
					}
					for (int i = 0; i < auditorList.size(); i++) {
						  JSONArray levelList =  auditorList.get(i).getJSONArray("levelList");
						  for (int j = 0; j <  levelList.size(); j++) {
							  JSONArray   teacherList = levelList.getJSONObject(j).getJSONArray("teacherList");
							  for (int k = 0; k < teacherList.size(); k++) {
								  JSONObject teacher = teacherList.getJSONObject(k);
								  teacher.put("teacherName",  teacherMap.get( teacher.getLong("teacherId")));
							 }
						  }
					}
					
					response.put("data", auditorList);
					return response;
				}else {
					setPromptMessage(response, "-2", "系统未查询到你的审批人信息，赶紧联系管理员设置请假组吧！");
					return response;
				}
			}
		}
	}
	
	/**
	 * 获取请假列表
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getLeaveList" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveList(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		checkApplyStartEndDate(param);
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		param.put("schoolId", getXxdm(req));
		param.put("accountId", accountId);
		String applyTeacherName = request.getString("applyTeacherName");
		int page = request.getIntValue("page");
		int pageSize = request.getIntValue("pageSize");
		page = page<=0 ? 1:page;
		int position = (page -1 ) * pageSize;
		param.put("position", position);
		param.put("pageSize", pageSize);
		
		String queryType = request.getString("queryType");
		String mystatus = request.getString("mystatus");
		
		List<JSONObject> data =new ArrayList<JSONObject>();
		String selectedSemester =getCurXnxq(req);
		School school = getSchool(req,selectedSemester);
		String[][] teacherIdNames = getTeacherIdNames(school,selectedSemester);
		if (StringUtils.isNotBlank(applyTeacherName)) {
			List<String> teacherList = new ArrayList<String>();
			for (int i = 0; i < teacherIdNames[0].length; i++) {
				System.out.println(teacherIdNames[1][i] );
				if (teacherIdNames[1][i].contains(applyTeacherName.trim())) {
					teacherList.add(teacherIdNames[0][i]);
				}
			}
			if (teacherList.size() > 0 ) {
				param.put("teacherList", teacherList);
			}else {
				response.put("data", data);
				setPromptMessage(response, "0", "查询成功");//找不到该名字的老师
				return response;
			}
		}
		
		int rowCnt = 0;
		if ("0".equals(queryType)) {
			// 获取所有的
			 rowCnt = leaveApplicationService.getAdminLeaveApplyListCnt(param);
			 data = leaveApplicationService.getAdminLeaveApplyList(param);
		}else if ("1".equals(queryType)) {
			// 获取自己跟自己审核的
			 rowCnt = leaveApplicationService.getAuditLeaveApplyListCnt(param);
			 data = leaveApplicationService.getAuditLeaveApplyList(param);
		}else {
			// 获取自己的
			 rowCnt = leaveApplicationService.getLeaveApplyListCnt(param);
			 data = leaveApplicationService.getLeaveApplyList(param);
		}
 
		response.put("page", page);
		response.put("pageSize", pageSize);
		response.put("rowCnt", rowCnt);
		if (pageSize > 0 ) {
			response.put("pageCnt", rowCnt/pageSize + (rowCnt%pageSize == 0? 0 : 1 ) );
		}
		
		List<JSONObject>  list = leaveApplicationService.getLeaveTypeList(param);
		if(list==null || list.size() == 0){
			param.put("schoolId", "ALL");
			list = leaveApplicationService.getLeaveTypeList(param);
		}
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < list.size(); i++) {
			JSONObject object = list.get(i);
			map.put(object.getString("types"), object.getString("typeName"));
		}

		Iterator<JSONObject> iterator = data.iterator();
		int row = 0;
		SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
		while (iterator.hasNext()) {
			row ++ ;
			JSONObject jsonObject = iterator.next();
			JSONArray jsonArray = jsonObject.getJSONArray("auditDetail");
			String applyUserId = jsonObject.getString("applyUserId");
			String leaveType = jsonObject.getString("leaveType");
			int status = jsonObject.getIntValue("status");
			Date applyDate = jsonObject.getDate("applyDate");
			Date leaveStartTime = jsonObject.getDate("leaveStartTime");
			Date leaveEndTime = jsonObject.getDate("leaveEndTime");
			try {
				jsonObject.put("applyDate", format.format(applyDate));
				jsonObject.put("leaveStartTime", format.format(leaveStartTime));
				jsonObject.put("leaveEndTime", format.format(leaveEndTime));
			} catch (Exception e) {
			}

			jsonObject.put("row", row);
			if (StringUtils.isNotBlank(applyUserId)) {
				int index = indexInArray(applyUserId, teacherIdNames[0]);
				if(index>-1){
					jsonObject.put("applyUserName", teacherIdNames[1][index]);
				}
			}
			jsonObject.put("leaveTypeName", map.get(leaveType));
			for (int j = 0; j < jsonArray.size(); j++) {
				JSONObject object = jsonArray.getJSONObject(j);
				JSONArray audits =  object.getJSONArray("audits");
				String auditor =  object.getString("auditor");
				int statusDetail = object.getIntValue("status");
				if (status == statusDetail) {
					if (StringUtils.isNotBlank(auditor)) {
						String auditorName = null;
							int index = indexInArray(auditor, teacherIdNames[0]);
							if(index>-1){
								auditorName =  teacherIdNames[1][index];
							}
						if (statusDetail==-1) {
							jsonObject.put("process", auditorName +"不同意");
							jsonObject.put("canDel", "1");
						}else if (statusDetail==2) {
							jsonObject.put("process", "同意");
						}
					}else {
						//无人审核
						 if (audits != null  ) {
							 if ("1".equals(queryType)&& "0".equals(mystatus)) {
								 jsonObject.put("process", "待我审核");
							}else {
								   List<String> teacherNames = new ArrayList<String>();
								   for (int k = 0; k < audits.size(); k++) {
									    JSONObject obj = audits.getJSONObject(k);
										int index = indexInArray(obj.getString("teacherId"), teacherIdNames[0]);
										if(index>-1){
											teacherNames.add(teacherIdNames[1][index]);
										}
									}
								   jsonObject.put("process",  StringUtil.toStringBySeparator(teacherNames)+"待审核");
							}
							
						}
						 if (j==0) {
							 jsonObject.put("canDel", "1");//第一个审核的没人
						}
						
					}
					break;
				}
			}
		}
 		response.put("data", data);
		setPromptMessage(response, "0", "查询成功");
		return response;
	}
	
	@RequestMapping(value = "/uploadLeaveApplyFile" )
	@ResponseBody
	public JSONObject uploadLeaveApplyFile(HttpServletRequest req, @RequestParam("fileBoby") MultipartFile file , HttpServletResponse res){
		return uploadFile(file);
	}
	
	@RequestMapping(value = "/delLeaveApplyFile" )
	@ResponseBody
	public JSONObject delLeaveApplyFile(HttpServletRequest req,  @RequestBody JSONObject request, HttpServletResponse res){
		return deleteFile(request);
	}
	
	@RequestMapping(value = "/preDownloadFile")
	public void preDownloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		downloadFile(req,res);
	}
	
	/**
	 * 获取实际请假天数
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getLeaveActualDays", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveActualDays(HttpServletRequest req, @RequestBody JSONObject request,	HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		
		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		Date leaveStartTime = request.getDate("leaveStartTime");
		Date leaveEndTime = request.getDate("leaveEndTime");
		int leaveStartTimeAMPM = request.getIntValue("leaveStartTimeAMPM");
		int leaveEndTimeAMPM = request.getIntValue("leaveEndTimeAMPM");

		param.put("leaveStartTime", leaveStartTime);
		param.put("leaveStartTimeAMPM", leaveStartTimeAMPM);
		param.put("leaveEndTime", leaveEndTime);
		param.put("leaveEndTimeAMPM", leaveEndTimeAMPM);

		if (leaveEndTime.getTime() - leaveStartTime.getTime() < 0) {
			setPromptMessage(response, "-1", "请假日期不合法");
			return response;
		}

		//设置请假天数
		if(!setLeaveDays(param,response)) {
			return response;
		} else {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("leavedays", param.get("leavedays"));
			response.put("data", jsonObject);
		}

		setPromptMessage(response, "0", "获取成功");
		return response;
	}
	
	/**
	 * 教师请假
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/addLeaveApply" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject insertLeaveApply(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String termInfo = getCurXnxq(req);
		Date leaveStartTime = request.getDate("leaveStartTime");
		Date leaveEndTime = request.getDate("leaveEndTime");
		String reason =  request.getString("reason");
		String leaveType = request.getString("leaveType");
		int leaveStartTimeAMPM = request.getIntValue("leaveStartTimeAMPM");
		int leaveEndTimeAMPM = request.getIntValue("leaveEndTimeAMPM");
 
		if(StringUtils.isNotBlank(termInfo)){
			param.put("schoolYear", termInfo.substring(0, 4));
			param.put("term", termInfo.substring(4));
		}
		String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));
		String schoolId = getXxdm(req);
		String applicationId = UUIDUtil.getUUID();
		param.put("schoolId", schoolId);
		
		param.put("applicationId", applicationId);
		param.put("applyUserId", accountId);
		param.put("teacherId", accountId);
		JSONObject obj =  leaveApplicationService.getTeacherGroup(param);
		String groupId = null;
		if (obj==null) {
			setPromptMessage(response , "-1" , "您还没有分配到请假组,请联系管理员");
			return response;
		}else {
			groupId = obj.getString("groupId");
			if (StringUtils.isEmpty(groupId)) {
				setPromptMessage(response , "-1" , "您还没有分配到请假组,请联系管理员");
				return response;
			}
		}
		
		param.put("groupId", groupId);
		param.put("leaveStartTime", leaveStartTime);
		param.put("leaveStartTimeAMPM", leaveStartTimeAMPM);
		param.put("leaveEndTime", leaveEndTime);
		param.put("leaveEndTimeAMPM", leaveEndTimeAMPM);
		param.put("leaveType", leaveType);
		param.put("reason", reason);
		param.put("status", 1);
		 
		if (leaveEndTime.getTime() - leaveStartTime.getTime() < 0 ) {
			setPromptMessage(response, "-1", "请假日期不合法");
			return response;
		}
		
		double totalDays = 0;
		//设置请假天数
		if(!setLeaveDays(param,response)) {
			return response;
		} else {
			totalDays = param.getDouble("leavedays");
		}
 
		Date date = new Date(); 
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String dateTime = df.format(date); // Formats a Date into a date/time string.
		param.put("applyDate", dateTime);

		/*
		 * 获取审核组，插入审核人
		 *  
		 */	
		//设置消息接收人列表(一级审批)
		List<JSONObject> msgReceiversArray = null;
		List<JSONObject>  list = leaveApplicationService.getAuditdaysList(param);
		JSONArray auditorObj = null;
		if (list!=null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				JSONObject object = list.get(i);
				 Float daysBegin = object.getFloat("daysBegin");
				 Float daysEnd = object.getFloat("daysEnd");
				 daysEnd = daysEnd!=null?daysEnd:30000;
				 if (totalDays>=daysBegin && totalDays< daysEnd ) {
					 groupId = object.getString("groupId");
					 auditorObj = object.getJSONArray("auditorLevel");
					 break;//找到审批等级
				}
			}
		}
		
		if (auditorObj!=null && auditorObj.size() > 0 ) {
			//1级审批列表
			msgReceiversArray = getMsgReceiversArray(1,auditorObj,schoolId,termInfo);
			if(msgReceiversArray != null) {
				logger.info("insertLeaveApply : msgReceiversArray = " + msgReceiversArray.toString());
			}
			
			JSONObject param2 = new JSONObject();
			param2.put("applicationId", applicationId);
			param2.put("groupId", groupId);
			try {
				for (int i = 0; i < auditorObj.size(); i++) {
					JSONObject jsonObject = auditorObj.getJSONObject(i);
					int levelNum = jsonObject.getIntValue("levelNum");
					param2.put("levelNum", levelNum);
					if (levelNum==1) {
						param2.put("status", 1);
					}else{
						param2.put("status", 0);
					}
					leaveApplicationService.insertProcedure(param2);
					JSONArray jsonArray = jsonObject.getJSONArray("auditors");
					if (jsonArray!=null && jsonArray.size() > 0) {
						for (int j = 0; j <jsonArray.size(); j++) {
							JSONObject object = jsonArray.getJSONObject(j);
							String teacherId = object.getString("teacherId");
							if (StringUtils.isNotBlank(teacherId)) {
								param2.put("teacherId", teacherId);
								leaveApplicationService.insertLeaveApplyProcedureMember(param2);
							}
						}
					}
				}
			} catch (Exception e) {
				setPromptMessage(response , "-1" , "设置审批流程失败！");
				return response;
			}
		}else {
			setPromptMessage(response , "-1" , "请假时长还没有设置审批人,请联系管理员！");
			return response;
		}
  
		JSONArray fileArray = param.getJSONArray("files");
		if (fileArray!=null) {
			JSONObject param2 = new JSONObject();
			try {
				for (int i = 0; i < fileArray.size(); i++) {
					JSONObject object = fileArray.getJSONObject(i);
					param2.put("applicationId",applicationId);
					param2.put("appFileName", object.getString("appFileName"));
					param2.put("fileUrl", object.getString("fileUrl"));
					leaveApplicationService.updateLeaveApplyFile(param2);//添加 申请文件
				}
			} catch (Exception e) {
				e.printStackTrace();
				setPromptMessage(response , "-1" , "保存请假文件失败！");
				return response;
			}
		}
        try {
        	leaveApplicationService.insertLeaveApply(param);// 添加请假记录
		} catch (Exception e) {
			setPromptMessage(response , "-1" , "设置失败！");
			return response;
		}
        
        //发送模板消息
  		if(msgReceiversArray != null && msgReceiversArray.size() > 0) {
  			//存在接收者列表时
  			JSONObject paramMsg = new JSONObject();
  			//设置消息接受者列表
  			paramMsg.put("receivers", msgReceiversArray);
  			paramMsg.put("first", "你收到一条新的教师请假审核单！");
  			paramMsg.put("remark", "请点击详情进行审核！");
  			/**
  			 * 请假人信息
  			 */
  			List<Long>  accountIds =  new  ArrayList<Long>();
  			accountIds.add(Long.valueOf(accountId));
  			List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfo);
  			//设置请假人姓名
  			paramMsg.put("applyName", accounts.get(0).name);
  			//设置请假类型
  			String leaveTypeName = getLeaveTypeName(leaveType);
			paramMsg.put("leaveType", leaveTypeName);
			//设置请假时间
			String startAMPM = leaveStartTimeAMPM == 0 ? "上午" : "下午";
			String endAMPM = leaveEndTimeAMPM == 0 ?  "上午" : "下午";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String leaveTime = sdf.format(leaveStartTime) + " " + startAMPM + "至" + sdf.format(leaveEndTime) + " " + endAMPM;
			paramMsg.put("leaveTime", leaveTime);
  			//设置请假天数
  			paramMsg.put("leavedays", ""+totalDays + "天");
  			//设置请假理由
  			paramMsg.put("reason", reason);
  			//创建者（自己）
  			paramMsg.put("creatorName", accounts.get(0).name);
  			//设置学校ID
  			School school = super.getSchool(req,null);
  			if(school != null) {
  				paramMsg.put("schoolId", school.getExtId());
  			}
  			//设置模板类型
  			paramMsg.put("msgTemplateType", "JSQJ1");
  			paramMsg.put("applicationId", applicationId);
  			
  			if(!sendAppMsg(paramMsg)) {
  				setPromptMessage(response, "1", "发送通知失败！");
  				return response;
  			}
  		}
		
		setPromptMessage(response , "0" , "请假成功");
		return response;
	}
	
	
	@RequestMapping(value = "/getLeaveApply" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveApply(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		param.put("schoolId", getXxdm(req));
		JSONObject jsonObject = leaveApplicationService.getLeaveApply(param);
        String applicationId = request.getString("applicationId");
        if (StringUtils.isBlank(applicationId)) {
        	setPromptMessage(response , "-1" , "参数错误,请联系管理员。");
        	return response;
		}
		String selectedSemester =getCurXnxq(req);
		School school = getSchool(req,null);
		String[][] teacherIdNames = getTeacherIdNames(school,selectedSemester);
		
		    SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
			JSONArray jsonArray = jsonObject.getJSONArray("auditDetail");
			String teacherId = null;
			for (int j = 0; j < jsonArray.size(); j++) {
				JSONObject object = jsonArray.getJSONObject(j);
				String auditor = object.getString("auditor");
				if (StringUtils.isNotBlank(auditor)) {
					
					int index = indexInArray(auditor, teacherIdNames[0]);
					if(index>-1){
						object.put("auditorName",  teacherIdNames[1][index]);
					}
					
				}
				Date processDate = jsonObject.getDate("processDate");
				try {
					if (processDate!=null) {
						jsonObject.put("processDate", format.format(processDate));
					}
				} catch (Exception e) {
					 
				}
				
				 JSONArray jArray = object.getJSONArray("audits");
				 if (jArray!=null) {
					 for (int i = 0; i < jArray.size(); i++) {
						JSONObject object2 = jArray.getJSONObject(i);
						teacherId = object2.getString("teacherId");
						if (StringUtils.isNotBlank(teacherId)) {
							int index = indexInArray(teacherId, teacherIdNames[0]);
							if(index>-1){
								object2.put("teacherName", teacherIdNames[1][index]);
							}
						}
					}
				 }
			}
		String applyUserId =  jsonObject.getString("applyUserId");
		if (StringUtils.isNotBlank(applyUserId)) {
			int index = indexInArray(applyUserId, teacherIdNames[0]);
			if(index>-1){
				jsonObject.put("applyUserName", teacherIdNames[1][index]);
			}
		}
		Date applyDate = jsonObject.getDate("applyDate");
		try {
			jsonObject.put("applyDate", format.format(applyDate));
		} catch (Exception e) {
			 
		}
		
		Date leaveStartTime = jsonObject.getDate("leaveStartTime");
		try {
			jsonObject.put("leaveStartTime", format.format(leaveStartTime));
		} catch (Exception e) {
			 
		}
		Date leaveEndTime = jsonObject.getDate("leaveEndTime");
		try {
			jsonObject.put("leaveEndTime", format.format(leaveEndTime));
		} catch (Exception e) {
			 
		}
		response.put("data", jsonObject);
		setPromptMessage(response , "0" , "成功");
		return response;
	}
	
	@RequestMapping(value = "/delLeaveApply" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delLeaveApply(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res){
		return deleteLeaveApply(request);
	}
 
	@RequestMapping(value = "/getGroupMemberAndAuditdaysList"  )
	@ResponseBody
	public JSONObject getGroupMemberAndAuditdaysList(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param =  new JSONObject();
		param.put("schoolId", getXxdm(req));
		List<JSONObject> list = leaveApplicationService.getGroupMemberAndAuditdaysList(param);

		if (list!=null && list.size() > 0 ) {
			String selectedSemester =getCurXnxq(req);
			School school = getSchool(req,null);
			String[][] teacherIdNames = getTeacherIdNames(school,selectedSemester);
			for (int i = 0; i < list.size(); i++) {
				JSONObject jsonObject = list.get(i);
				JSONArray members = jsonObject.getJSONArray("members");
				 if (members != null  ) {
					 for (int j = 0; j < members.size(); j++) {
						 JSONObject obj = members.getJSONObject(j);
							int index = indexInArray(obj.getString("teacherId"), teacherIdNames[0]);
							if(index>-1){
								obj.put("teacherName", teacherIdNames[1][index]);
							}
					 }
				 }
				 JSONArray auditflows = jsonObject.getJSONArray("auditflows");
				 if (auditflows!=null) {
					 for (int j = 0; j < auditflows.size(); j++) {
						    JSONObject obj = auditflows.getJSONObject(j);
						    JSONArray audits =  obj.getJSONArray("auditorLevel");
						    for (int k = 0; k < audits.size() ; k++) {
						    	  JSONObject auditors = audits.getJSONObject(k);
						    	  JSONArray auditorsArray = auditors.getJSONArray("auditors");
						    	  for (int k2 = 0; k2 < auditorsArray.size(); k2++) {
						    		  JSONObject auditObj = auditorsArray.getJSONObject(k2);
								    	int index = indexInArray(auditObj.getString("teacherId"), teacherIdNames[0]);
										if(index>-1){
											auditObj.put("teacherName", teacherIdNames[1][index]);
										}
								  }
							}
					 }
				}
			}
		}
		response.put("data", list);
		setPromptMessage(response , "0" , "成功");
		 
		return response;
	}
	
	@RequestMapping(value = "/getGroupMemberList" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGroupMemberList(HttpServletRequest req,  @RequestBody JSONObject request,   HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		param.put("schoolId", getXxdm(req));
		List<JSONObject> list = leaveApplicationService.getGroupMemberList(param);
		
		if (list!=null && list.size() > 0 ) {
			String selectedSemester =getCurXnxq(req);
			School school = getSchool(req,null);
			String[][] teacherIdNames = getTeacherIdNames(school,selectedSemester);
			for (int i = 0; i < list.size(); i++) {
				JSONObject jsonObject = list.get(i);
				JSONArray members = jsonObject.getJSONArray("members");
				 if (members != null  ) {
					 for (int j = 0; j < members.size(); j++) {
						 JSONObject obj = members.getJSONObject(j);
							int index = indexInArray(obj.getString("teacherId"), teacherIdNames[0]);
							if(index>-1){
								obj.put("teacherName", teacherIdNames[1][index]);
							}
						}
				 }
			}
			response.put("data", list.get(0));
		}
		setPromptMessage(response , "0" , "成功");
		return response;
	}
	
	@RequestMapping(value = "/updateGroup" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateGroup(HttpServletRequest req,  @RequestBody JSONObject request,   HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String groupId = request.getString("groupId");
		if (StringUtils.isBlank(groupId)) {
			groupId = UUIDUtil.getUUID();
		} 
		String schoolId = getXxdm(req);
		param.put("groupId", groupId);
		param.put("schoolId", schoolId);
        try {
        	leaveApplicationService.updateGroupMember(param);
		} catch (Exception e) {
			setPromptMessage(response , "-1" , "设置失败");
			return response;
		}
		
        response.put("groupId", groupId);
		setPromptMessage(response , "0" , "成功");
		return response;
	}
	
	@RequestMapping(value = "/delGroup" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delGroup(HttpServletRequest req,  @RequestBody JSONObject request,   HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		leaveApplicationService.delGroupMember(param);
		leaveApplicationService.deleteAuditMember(param);
		leaveApplicationService.delAuditdays(param);
 		setPromptMessage(response , "0" , "成功");
		return response;
	}
	
	@RequestMapping(value = "/getAuditDayGroupList" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAuditDayGroupList(HttpServletRequest req,   @RequestBody JSONObject request,  HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param =  request;
		 
		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		List<JSONObject> list = leaveApplicationService.getAuditdaysList(param);
		
		if (list!=null && list.size() > 0 ) {
			String selectedSemester =getCurXnxq(req);
			School school = getSchool(req,null);
			String[][] teacherIdNames = getTeacherIdNames(school,selectedSemester);
			
			for (int i = 0; i < list.size(); i++) {
				JSONObject jsonObject = list.get(i);

				  JSONArray audits =  jsonObject.getJSONArray("auditorLevel");
				    for (int k = 0; k < audits.size() ; k++) {
				    	  JSONObject auditors = audits.getJSONObject(k);
				    	  JSONArray auditorsArray = auditors.getJSONArray("auditors");
				    	  for (int k2 = 0; k2 < auditorsArray.size(); k2++) {
				    		  JSONObject auditObj = auditorsArray.getJSONObject(k2);
						    	int index = indexInArray(auditObj.getString("teacherId"), teacherIdNames[0]);
								if(index>-1){
									auditObj.put("teacherName", teacherIdNames[1][index]);
								}
						  }
					}
			}
		}
		response.put("data", list);
		
		setPromptMessage(response , "0" , "成功");
		return response;
	}
	
	
	@RequestMapping(value = "/updateAuditDayGroup" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateAuditDayGroup(HttpServletRequest req,   @RequestBody JSONObject request,  HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param =  request;
		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		try {
			leaveApplicationService.updateAuditdays(param);
		} catch (Exception e) {
			setPromptMessage(response , "-1" , "设置失败");
			return response;
		}

		setPromptMessage(response , "0" , "保存成功");
		return response;
	}
	
/*	@RequestMapping(value = "/delAuditDayGroup" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delAuditDayGroup(HttpServletRequest req,  @RequestBody JSONObject request,  HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param =  request;
		leaveApplicationService.delAuditdays(param);
		leaveApplicationService.deleteAuditMember(param);
		 
		setPromptMessage(response , "0" , "成功");
		return response;
	}*/
	
	/**
	 * 审核人员审核 ： 1 获取审核列表 2判断当前审核状态  不同意 就更新  同意看有没有下一层，没有也更新 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/updateProcedure" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateProcedure(HttpServletRequest req,  @RequestBody JSONObject request,  HttpServletResponse res){
		request.put("curTermInfoId", getCurXnxq(req));
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		request.put("accountId", accountId);
		request.put("schoolId", getXxdm(req));
		return super.updateProcedure(request,res);		
	}
	
	@RequestMapping(value = "/isSetProcedure" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject isSetProcedure(HttpServletRequest req, HttpServletResponse res){
		JSONObject param = new JSONObject();
		param.put("schoolId", getXxdm(req));
		return isSetProcedure(param);
	}
	
	@RequestMapping(value = "/getLeaveTypeList" ,method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveTypeList(HttpServletRequest req,  @RequestBody JSONObject request,  HttpServletResponse res){
		return getLeaveTypeList(request);
	}
	
	/**
 	 * 查询多个组下的教师列表
 	 * @param session 会话
 	 * @return 教师列表
 	 */
 	@RequestMapping(value = "getGroupsLevel", method = RequestMethod.POST)
 	@ResponseBody
 	public JSONObject getGroupsLevel(@RequestBody JSONObject params,
 			HttpServletRequest req, HttpServletResponse res) {

 		JSONObject json = new JSONObject();
 		long schoolId = Long.parseLong(getXxdm(req));
 		String groupId = params.getString("groupId");

 		Map<Long,String> teacherMap = new HashMap<Long,String>();

 		try {
 			String selectedSemester=null;
 			if(StringUtils.isEmpty(selectedSemester)|| selectedSemester==null){
 				selectedSemester=getCurXnxq(req);
 			}
 			School sch = getSchool(req,selectedSemester);
 		 
 			JSONObject parm = new JSONObject();
 			parm.put("schoolId", schoolId);
 			parm.put("groupId", groupId);
 			String selectedTeacherIds=leaveApplicationService.getSelectedTeacherIds(parm);
 			 /**声明所有的节点基础信息 */
 			 //  科室下的科室列表
 			 JSONArray data = new JSONArray();
 			 JSONObject department = new JSONObject();
 			 String departmentIds = "88881";
 			 department.put("id", departmentIds);
 			 department.put("text", "科室");
 			 //department.put("checked", "false");
 			 department.put("state", "closed");
 			 JSONArray dChildren = new JSONArray();		
 			 
 			// 年级下的年级列表			 
 			 JSONObject gradeGroup = new JSONObject();
 			 String gradeGroupIds = "88882";
 			 gradeGroup.put("id", gradeGroupIds);
 			 gradeGroup.put("text", "年级组");
 			 //gradeGroup.put("checked", "false");
 			 gradeGroup.put("state", "closed");
 			 JSONArray gChildren = new JSONArray();	
 			 
 			// 教研下的年级列表
 			 JSONObject researchGroup = new JSONObject();
 			 String researchGroupIds = "88883";
 			 researchGroup.put("id", researchGroupIds);
 			 researchGroup.put("text", "教研组");
 			 //researchGroup.put("checked", "false");
 			 researchGroup.put("state", "closed");
 			 JSONArray rChildren = new JSONArray();
 			 
 			// 备课下的年级列表			 
 			 JSONObject preparation = new JSONObject();
 			 String preparationIds = "88884";
 			 preparation.put("id", preparationIds);
 			 preparation.put("text", "备课组");
 			 //preparation.put("checked", "false");
 			 preparation.put("state", "closed");
 			 JSONArray pChildren = new JSONArray();	
 			 
 			// 各部门的责任人列表
 			 JSONObject departHead = new JSONObject();
 			 String departHeadIds = "88885";
 			 departHead.put("id", departHeadIds);
 			 departHead.put("text", "部门负责人");
 			 //departHead.put("checked", "false");
 			 departHead.put("state", "closed");
 			 JSONArray hChildren = new JSONArray();
 			// 部门责任人-科室
 			 JSONObject depHead = new JSONObject();
 			 String depHeadId = departHeadIds + "01";
 			 depHead.put("id", depHeadId);
 			 depHead.put("text", "科室负责人");
 			 //depHead.put("checked", "false");
 			 depHead.put("state", "closed");
 			 JSONArray dHeadArray = new JSONArray();	
 			// 部门责任人-年级组
 			 JSONObject gradeHead = new JSONObject();
 			 String gradeHeadId = departHeadIds + "02";
 			 gradeHead.put("id", gradeHeadId);
 			 gradeHead.put("text", "年级组负责人");
 			 //gradeHead.put("checked", "false");
 			 gradeHead.put("state", "closed");
 			 JSONArray gradeHeadArray = new JSONArray();
 			// 部门责任人-教研组
 			 JSONObject researchHead = new JSONObject();
 			 String researchHeadId = departHeadIds + "03";
 			 researchHead.put("id", researchHeadId);
 			 researchHead.put("text", "教研组负责人");
 			 //researchHead.put("checked", "false");
 			 researchHead.put("state", "closed");
 			 JSONArray researchHeadArray = new JSONArray();
 			// 部门责任人-备课组
 			 JSONObject preparationHead = new JSONObject();
 			 String preparationHeadId = departHeadIds + "04";
 			 preparationHead.put("id", preparationHeadId);
 			 preparationHead.put("text", "备课组负责人");
 			 //preparationHead.put("checked", "false");
 			 preparationHead.put("state", "closed");
 			 JSONArray preparationHeadArray = new JSONArray();
 			 
 			// 班主任列表
 			 JSONObject headmaster = new JSONObject();
 			 headmaster.put("text", "班主任");
 			 //headmaster.put("checked"H, "false");
 			 headmaster.put("state", "closed");
 			 String headmasterId = "88886";
 			 headmaster.put("id", headmasterId);    
              // 年级下的班主任列表
 			 JSONArray gradeArray = new JSONArray();
 			 
 			 // 所有的教师列表
 			 JSONObject teachers = new JSONObject();
 			 teachers.put("text", "所有教师");
 			 //teachers.put("checked", "false");	
 			 teachers.put("state", "closed");
 			 String teacherIds = "88887";
 			 teachers.put("id", teacherIds);		 			 
 			/*** 获取全校教师信息列表 */
			 //List<Account> accountList =commonDataService.getCourseTeacherList(termInfo ,sch, null);
			 List<Account> accountList =commonDataService.getAllSchoolEmployees(sch,selectedSemester,null);
			 JSONArray allTeacherArray = new JSONArray();
			 if (CollectionUtils.isNotEmpty(accountList)) {
				 for (Account account : accountList){
					  long teacherId = account.getId();
					  String teacherName = account.getName();
					  teacherMap.put(teacherId, teacherName);
					  
					  JSONObject temp = new JSONObject();
					  temp.put("id", teacherIds + teacherId);
					  temp.put("text", teacherName);
					  //temp.put("checked", "false");
					  JSONObject attibute = new JSONObject();
					  attibute.put("teacherId", teacherId+"");
					  if (StringUtils.isBlank(teacherName)) {
							 continue;
					  }
					  attibute.put("teacherName", teacherName);
					  temp.put("attributes", attibute);
					  allTeacherArray.add(temp);
				 } 
			 }	 
			
 			/** 查询学校-机构-教师,责任人 */
 			 List<OrgInfo> OrgList = commonDataService.getSchoolOrgList(sch,selectedSemester);
 			 for  (OrgInfo orgInfo : OrgList) {
 					JSONObject org = new JSONObject();
 					if (orgInfo.getOrgType() == 1) {   // 1:教研组，2:年级组，3:备课组，6:科室
 						String parentId = researchGroupIds + orgInfo.getId();					
 						// 教研组教师列表
 						org.put("text", orgInfo.getOrgName());
 						org.put("id", parentId);
 						org.put("state", "closed");
 						List<Long> accountIdList = orgInfo.getMemberAccountIds();
 						JSONObject Fteacher = getTeacherArray(parentId,accountIdList,teacherMap,selectedTeacherIds);					
 						if ("false".equals(Fteacher.getString("isEmpty"))){
 							org.put("children", Fteacher.getJSONArray("teachers"));
 							rChildren.add(org);
 						}
 						// 教研组责任人列表
 						List<Long> headIdList = orgInfo.getHeaderAccountIds();
 						if (CollectionUtils.isNotEmpty(headIdList)){
 							JSONObject Tteacher = getTeacherArray(parentId,headIdList,teacherMap,selectedTeacherIds);	
 							if ("false".equals(Tteacher.getString("isEmpty"))){
 								researchHeadArray = Tteacher.getJSONArray("teachers");
 							}
 						}											
 					} else if (orgInfo.getOrgType() == 2) {
 						String parentId = gradeGroupIds + orgInfo.getId();
 						// 年级组教师列表
 						org.put("text", orgInfo.getOrgName());
 						org.put("id", parentId);
 						//org.put("checked", "false");
 						org.put("state", "closed");
 						List<Long> accountIdList = orgInfo.getMemberAccountIds();
 						JSONObject Fteacher = getTeacherArray(parentId,accountIdList,teacherMap,selectedTeacherIds);					
 						if ("false".equals(Fteacher.getString("isEmpty"))){
 							org.put("children", Fteacher.getJSONArray("teachers"));
 							gChildren.add(org);
 						}
 						// 年级组责任人列表
 						List<Long> headIdList = orgInfo.getHeaderAccountIds();
 						if (CollectionUtils.isNotEmpty(headIdList)){
 							JSONObject Tteacher = getTeacherArray(parentId,headIdList,teacherMap,selectedTeacherIds);	
 							if ("false".equals(Tteacher.getString("isEmpty"))){
 								gradeHeadArray = Tteacher.getJSONArray("teachers");
 							}
 						}											
 					} else if (orgInfo.getOrgType() == 3) {
 						String parentId = preparationIds + orgInfo.getId();
 						// 备课组教师列表
 						org.put("text", orgInfo.getOrgName());
 						org.put("id", parentId);
 						//org.put("checked", "false");
 						org.put("state", "closed");
 						List<Long> accountIdList = orgInfo.getMemberIds();						
 						JSONObject Fteacher = getTeacherArray(parentId,accountIdList,teacherMap,selectedTeacherIds);					
 						if ("false".equals(Fteacher.getString("isEmpty"))){
 							org.put("children", Fteacher.getJSONArray("teachers"));
 							pChildren.add(org);
 						}
 						// 备课组责任人列表
 						List<Long> headIdList = orgInfo.getHeaderAccountIds();
 						if (CollectionUtils.isNotEmpty(headIdList)){
 							JSONObject Tteacher = getTeacherArray(parentId,headIdList,teacherMap,selectedTeacherIds);	
 							if ("false".equals(Tteacher.getString("isEmpty"))){
 								preparationHeadArray = Tteacher.getJSONArray("teachers");
 							}
 						}													
 					} else if (orgInfo.getOrgType() == 6) {
 						String parentId = departmentIds + orgInfo.getId();
 						// 科室组教师列表
 						org.put("text", orgInfo.getOrgName());
 						org.put("id", parentId);
 						//org.put("checked", "false");
 						org.put("state", "closed");
 						List<Long> accountIdList = orgInfo.getMemberAccountIds();
 						JSONObject Fteacher = getTeacherArray(parentId,accountIdList,teacherMap,selectedTeacherIds);					
 						if ("false".equals(Fteacher.getString("isEmpty"))){
 							org.put("children", Fteacher.getJSONArray("teachers"));
 							dChildren.add(org);
 						}
 						// 科室责任人列表
 						List<Long> headIdList = orgInfo.getHeaderAccountIds();
 						if (CollectionUtils.isNotEmpty(headIdList)){
 							JSONObject Tteacher = getTeacherArray(parentId,headIdList,teacherMap,selectedTeacherIds);	
 							if ("false".equals(Tteacher.getString("isEmpty"))){
 								dHeadArray = Tteacher.getJSONArray("teachers");
 							}
 						}									
 					}			    
 			 }	 
 			 
 			 /***获取班主任的信息列表*/
 			 List<Grade> gradeList = commonDataService.getGradeList(sch,selectedSemester);
 			 for (Grade grade : gradeList){
 				    JSONObject gObject = new JSONObject();
 				    String gradeIds = headmasterId + grade.getId();
 				    gObject.put("id", gradeIds);			    
 				String gradeName = AccountStructConstants.T_GradeLevelName
 						.get((grade.getCurrentLevel()));
 				    gObject.put("text", gradeName);
 				    //gObject.put("checked", "false");
 				    gObject.put("state", "closed");
 				    List<Long> classIds = grade.getClassIds();
 				    if (CollectionUtils.isNotEmpty(classIds)){
 				    	 HashMap<String,Object> map = new HashMap<String,Object>();
 				    	 String ids = classIds.toString();
 				    	 ids = ids.replace("[", "").replace("]", "").replace(" ", "");
 						 map.put("classId", ids);
 						 map.put("schoolId", schoolId);
 						 map.put("termInfoId", Long.parseLong(selectedSemester));
 						 List<Account> aList = commonDataService.getDeanList(map);
 						 
 						 JSONArray deanArray =  new JSONArray();
						 if (CollectionUtils.isNotEmpty(aList)) {
							 for(Account info : aList) {
								 if (selectedTeacherIds!=null && selectedTeacherIds.contains(info.getId()+"")) {
									continue;
								 }
								 JSONObject temp = new JSONObject();
								 temp.put("id", gradeIds + info.getId());
								 temp.put("text", info.getName());
								 //temp.put("checked", "false");
								 JSONObject attibute = new JSONObject();
								 attibute.put("teacherId", info.getId() + "");
								 if (StringUtils.isBlank(info.getName())) {
									 continue;
								 }
								 attibute.put("teacherName", info.getName());
								 temp.put("attributes", attibute);
								 deanArray.add(temp);
							 }
						 }
						 if (deanArray.size() > 0){
							 gObject.put("children", deanArray);
							 gradeArray.add(gObject);
						}
 				    }			    
 			 }
 			 /*** 获取全校教师信息列表 */
 			 List<Account> accountList1 = commonDataService.getAllSchoolEmployees(sch, selectedSemester, "");
 			 List<Long> accountIdList = new ArrayList<Long>();
 			 for(Account a:accountList1){
 				accountIdList.add(a.getId());
 			 }
 			 JSONObject Tteacher = getTeacherArray(teacherIds,accountIdList,teacherMap,selectedTeacherIds);
 			 if ("false".equals(Tteacher.getString("isEmpty"))){
 				 teachers.put("children", Tteacher.getJSONArray("teachers"));
 			}
 			 
 			/** 根据子节点信息添加父节点 */ 
             if (dChildren.size() > 0){
             	department.put("children", dChildren);
             	data.add(department);
             }
 			if (gChildren.size() > 0){
 				gradeGroup.put("children", gChildren);
 				data.add(gradeGroup);
 			}
 		    if (rChildren.size() > 0){
 				researchGroup.put("children", rChildren);			 			 
 				data.add(researchGroup);
 			}
 	        if (pChildren.size() > 0){
 	        	preparation.put("children", pChildren);	
 				data.add(preparation);
 	        }
 			if (dHeadArray.size() > 0){
 				depHead.put("children", dHeadArray);
 				hChildren.add(depHead);
 			}
 			if (gradeHeadArray.size() > 0){
 				gradeHead.put("children", gradeHeadArray);
 				hChildren.add(gradeHead);
 			}
 			if (researchHeadArray.size() > 0){
 				researchHead.put("children", researchHeadArray);
 				hChildren.add(researchHead);
 			}
 			if (preparationHeadArray.size() > 0){
 				preparationHead.put("children", preparationHeadArray);
 				hChildren.add(preparationHead);
 			}
 			if (hChildren.size() > 0){
 				departHead.put("children", hChildren);	
 				data.add(departHead);
 			}
 			if (gradeArray.size() > 0){
 				headmaster.put("children", gradeArray); 
 				data.add(headmaster);
 			}
 			if (teachers.containsKey("children")){
 				data.add(teachers);
 			}
 			 
 			json.put("data", data);
 			json.put("code", OutputMessage.querySuccess.getCode());
 			json.put("msg", OutputMessage.querySuccess.getDesc());
 		} catch (Exception e) {
 			e.printStackTrace();
 			json.put("msg", "查询到教师组织层次出错！！");
 			json.put("code", OutputMessage.queryDataError.getCode());
 		}
 		return json;
 	}
 	
 	@RequestMapping(value = "/getAllTeacherList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAllTeacherList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String selectedSemester = null;//request.getString("selectedSemester");
		String teacherName = request.getString("teacherName");//2016-12-06-dlm
		if(StringUtils.isEmpty(selectedSemester)){
			selectedSemester = getCurXnxq(req);
		}
		param.put("selectedSemester", selectedSemester);
		param.put("teacherName", teacherName);//2016-12-06-dlm
		
		try {
			School school = getSchool(req,selectedSemester);
			param.put("school", school);
			List<JSONObject> data = leaveApplicationService.getAllTeacherList(param);
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

 	//测试用
 	@RequestMapping(value = "/getLeaveStatisticsListH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveStatisticsListH5(@RequestBody JSONObject request, HttpServletResponse res) {
 		JSONObject response = new JSONObject();

// 		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
// 		String startTimeStr = request.getString("startTime") + " 00:00:00";
// 		String endTimeStr = request.getString("endTime") + " 23:59:59";
//		Date startTime = null;
//		Date endTime = null;
//		try {
//			startTime = format.parse(startTimeStr);
//			endTime = format.parse(endTimeStr);
//		} catch (ParseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
 		
 		Date startTime = request.getDate("startTime");
		Date endTime = request.getDate("endTime");
		 Calendar calendar = Calendar.getInstance();
		 Date date = new Date();
		if ( startTime==null  ) {
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, -100);
			startTime = calendar.getTime();
			request.put("startTime", startTime);
		}
		if (endTime==null) {
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, 100);
			endTime = calendar.getTime();
			request.put("endTime", endTime);
		}
		
		JSONObject param = request;
		String schoolId = request.getString("schoolId");
		param.put("schoolId",schoolId);
 
		String applyTeacherName = request.getString("applyTeacherName");
		int page = request.getIntValue("page");
		int pageSize = request.getIntValue("pageSize");
		page = page<=0 ? 1:page;
		pageSize = pageSize <= 0 ? 20 : pageSize;
		int position = (page -1 ) * pageSize;
		param.put("position", position);
		param.put("pageSize", pageSize);
		List<JSONObject> data =new ArrayList<JSONObject>();
		String selectedSemester = "";
//		String userId = request.getString("userId");
//		School school = getSchoolByUserId(Long.valueOf(userId));
//		String[][] teacherIdNames = getTeacherIdNames(school,selectedSemester);
//		if (StringUtils.isNotBlank(applyTeacherName)) {
//			List<String> teacherList = new ArrayList<String>();
//			for (int i = 0; i < teacherIdNames[0].length; i++) {
//				System.out.println(teacherIdNames[1][i] );
//				if (teacherIdNames[1][i].contains(applyTeacherName.trim())) {
//					teacherList.add(teacherIdNames[0][i]);
//				}
//			}
//			if (teacherList.size() > 0 ) {
//				param.put("teacherList", teacherList);
//			}else{
//				response.put("data", data);
//				setPromptMessage(response, "0", "查询成功");//找不到该名字的老师
//				return response;
//			}
//		}
		
		try {
			int rowCnt = 0;
			rowCnt = leaveApplicationService.getLeaveStatisticsCnt(param);
			response.put("page", page);
			response.put("pageSize", pageSize);
			response.put("rowCnt", rowCnt);
			if (pageSize > 0 ) {
				response.put("pageCnt", rowCnt/pageSize + (rowCnt%pageSize == 0? 0 : 1 ) );
			}
		    data = leaveApplicationService.getLeaveStatistics(param);
			if (data != null) {
				param.put("schoolId","ALL");
				List<JSONObject> festivallist = leaveApplicationService.getFestivalList(param);
				HashMap<Date, Integer> festivalMap = new HashMap<Date, Integer>();
				HashMap<Date, String> festival2Map = new HashMap<Date, String>();
				for (int i = 0; i < festivallist.size(); i++) {
					JSONObject object = festivallist.get(i);
					festivalMap.put(object.getDate("festivalDay"), object.getInteger("onduty"));
					festival2Map.put(object.getDate("festivalDay"), object.getString("ampm"));
				}
				param.put("schoolId",schoolId);
				festivallist = leaveApplicationService.getFestivalList(param);
				for (int i = 0; i < festivallist.size(); i++) {
					JSONObject object = festivallist.get(i);
					festivalMap.put(object.getDate("festivalDay"), object.getInteger("onduty"));
					festival2Map.put(object.getDate("festivalDay"), object.getString("ampm"));
				}
				
				String calNo = UUIDUtil.getUUID();
				Calendar cal = Calendar.getInstance();
				for (int i = 0; i < data.size(); i++) {
					JSONObject object = data.get(i);
					Date leaveStartTime = object.getDate("leaveStartTime");
					Date leaveEndTime = object.getDate("leaveEndTime");
					
					int leaveStartTimeAMPM = object.getIntValue("leaveStartTimeAMPM");
					int leaveEndTimeAMPM = object.getIntValue("leaveEndTimeAMPM");
                   //以最晚开始为起点
					Date tempStartDate = leaveStartTime.getTime() - startTime.getTime() > 0? leaveStartTime:startTime;
					long lStart = tempStartDate.getTime(); 
					boolean isViewStart = leaveStartTime.getTime() - startTime.getTime() >= 0? true : false;
					//以最早结束为终点
					Date tempEndDate = leaveEndTime.getTime() - endTime.getTime() > 0? endTime:leaveEndTime;
					long lEnd = tempEndDate.getTime(); 
					boolean isViewEnd =  leaveEndTime.getTime() - endTime.getTime() > 0?  false : true;
					//boolean isSame = tempEndDate.getTime() - tempStartDate.getTime() == 0 ? true : false;
					
					String uuid = UUIDUtil.getUUID();
					cal.setTime(tempStartDate);//设置当前日期为 
//					double weekend = 0 ;
					double totalDays = 0;
					
					while (true) {
						 
						if (cal.getTime().getTime() - tempEndDate.getTime() > 0 ) {// 遍历的日期超越了 最后的日期跳出
							break;
						}
						 
						Integer integer = festivalMap.get(cal.getTime());//上下班标志
						String ampm = festival2Map.get(cal.getTime());//上下午标志
						long lNow =  cal.getTime().getTime();
						
						if (integer==null) {//没有设置是否上下班
							//如果是周六日
							if( cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY  ){
								//没有上班标志，不统计
							}else {//不是周六日
								 if (lNow ==  lStart) {//当前日期等于开始时间
									 if (lNow == lEnd) {//当前日期等于结束日期
										 if (isViewStart) {// 要看开始
											if (isViewEnd) {//看结束
												if (leaveStartTimeAMPM==0) {
													if (leaveEndTimeAMPM==0) { //都是上午
														 
														totalDays = totalDays + 0.5 ;
													}else {//同一天的上下午
														 
														totalDays = totalDays + 1 ;
													}
													
												}else {
													if (leaveEndTimeAMPM==1) {// 同一天都是下午
														 
														totalDays = totalDays + 0.5 ;
													}else {
														System.err.println("不存在的情形");
													}
												}
												
											}else {//只看开始不看结束
												if (leaveStartTimeAMPM==0) {// 开始的上午
												 
													totalDays = totalDays + 1 ;
												}else {// 开始的上午
												 
													totalDays = totalDays + 0.5 ;
												}
											}
											 
										 }else {//不看开始
											 if (isViewEnd) {// 要看结束
												 if (leaveEndTimeAMPM==0) {// 结束日期的上午
													 
													 totalDays = totalDays + 0.5 ;
												}else {// 结束日期的下午
													 
													 totalDays = totalDays + 1 ;
												}	
											 }else {// 都不看
												 
												 totalDays = totalDays + 1 ;
											 }
										}
										 
									 }else {// 管当前日期 不管 结束日期
										 if (isViewStart) {
											 if (leaveStartTimeAMPM==0) {
												 
												 totalDays = totalDays + 1 ;
											}else {
												 
												 totalDays = totalDays + 0.5 ;
											}
										 }else {
											 totalDays = totalDays + 1 ;
										 }
										
									 }
									 
								 }else {
									 if (lNow == lEnd) {
										if (isViewEnd) {
											if (leaveEndTimeAMPM==0) {
												 totalDays = totalDays + 0.5 ;
											}else {
												 totalDays = totalDays + 1 ;
											}
										}else {
											 totalDays = totalDays + 1 ;
										}	
									 }else {//不是结束
										 totalDays = totalDays + 1 ;
									 }
								 }
							}
						}else if (integer==0) {// 设置 休息
							 if (lNow ==  lStart) {
								if (lNow ==  lEnd) {
									if (isViewStart) {
										if (isViewEnd) {
											if (leaveStartTimeAMPM==0) {
												if (leaveEndTimeAMPM==0) {//都是上午
													if ("0".equals(ampm)) {//全天休息
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													}else if ("1".equals(ampm)) {//上午休息
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													}else if ("2".equals(ampm)) {//上午上班没有假
														 totalDays = totalDays + 0.5 ;
													}
													
												}else {
													if ("0".equals(ampm)) {//全天休息
//														 weekend = weekend + 1;
//														 totalDays = totalDays + 1 ;
													}else if ("1".equals(ampm)) {//上午休息
//														 weekend = weekend + 0.5;
														 totalDays = totalDays + 0.5 ;
													}else if ("2".equals(ampm)) {//上午上班没有假
//														 weekend = weekend + 0.5;
														 totalDays = totalDays + 0.5 ;
													}
													
												}
											}else {
                                                if (leaveEndTimeAMPM==1) {// 都是下午
                                                	if ("0".equals(ampm)) {//全天休息
//                                                		 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													}else if ("1".equals(ampm)) {//上午休息
														 totalDays = totalDays + 0.5 ;
													}else if ("2".equals(ampm)) {//上午上班没有假
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													}
												}else {
													System.err.println("不存在的情形");
												}
											}
											
										}else {// 只看开始 不看 结束
											if (leaveStartTimeAMPM==0) {
												if ("0".equals(ampm)) {//全天休息
//													 weekend = weekend + 1;
//													 totalDays = totalDays + 1 ;
												}else if ("1".equals(ampm)) {//上午休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 1 ;
												}else if ("2".equals(ampm)) {//上午上班没有假
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												}
												
											}else {
												if ("0".equals(ampm)) {//全天休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												}else if ("1".equals(ampm)) {//上午休息
													 totalDays = totalDays + 0.5 ;
												}else if ("2".equals(ampm)) {//上午上班没有假下午休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												}
											}
										}
									}else {// 不看 开始 看结束
										if (isViewEnd) {
											if (leaveEndTimeAMPM==0) {
												if ("0".equals(ampm)) {//全天休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												}else if ("1".equals(ampm)) {//上午休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												}else if ("2".equals(ampm)) {//上午上班没有假
													 totalDays = totalDays + 0.5 ;
												}
											}else {
												if ("0".equals(ampm)) {//全天休息
//													 weekend = weekend + 1;
//													 totalDays = totalDays + 1 ;
												}else if ("1".equals(ampm)) {//上午休息
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												}else if ("2".equals(ampm)) {//上午上班没有假
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5; 
												}
											}
										}else {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 1;
//												 totalDays = totalDays + 1 ;
											}else if ("1".equals(ampm)) {//上午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5; 
											}
										}
									}
								}else {//只管开头不管结束
									if (isViewStart) {
										if (leaveStartTimeAMPM==0) {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 1;
//												 totalDays = totalDays + 1 ;
											}else if ("1".equals(ampm)) {//上午休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 1 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5; 
											}
										}else {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											}else if ("1".equals(ampm)) {//上午休息
												 totalDays = totalDays + 0.5 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5; 
											}
										}
									}else {
										if ("0".equals(ampm)) {//全天休息
//											 weekend = weekend + 1;
//											 totalDays = totalDays + 1 ;
										}else if ("1".equals(ampm)) {//上午休息
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										}else if ("2".equals(ampm)) {//上午上班没有假
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5; 
										}
									}
								}
							 }else {// 只管后面日期
								if (lNow ==  lEnd) {
									if (isViewEnd) {
										if (leaveEndTimeAMPM==1) {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 1;
//												 totalDays = totalDays + 1 ;
											}else if ("1".equals(ampm)) {//上午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5; 
											}
										}else {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											}else if ("1".equals(ampm)) {//上午休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
												 totalDays = totalDays + 0.5; 
											}
											
										}
									}else {
										
										if ("0".equals(ampm)) {//全天休息
//											 weekend = weekend + 1;
//											 totalDays = totalDays + 1 ;
										}else if ("1".equals(ampm)) {//上午休息
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										}else if ("2".equals(ampm)) {//上午上班没有假
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5; 
										}
										
									}
								}else {
									if ("0".equals(ampm)) {//全天休息
//										 weekend = weekend + 1;
//										 totalDays = totalDays + 1 ;
									}else if ("1".equals(ampm)) {//上午休息
//										 weekend = weekend + 0.5;
										 totalDays = totalDays + 0.5 ;
									}else if ("2".equals(ampm)) {//上午上班没有假
//										 weekend = weekend + 0.5;
										 totalDays = totalDays + 0.5; 
									}
								}
							 }
							
						}else {// 上班
							 if (lNow ==  lStart) {
								 if (lNow == lEnd) {
									 if (isViewStart) {
										if (isViewEnd) {
											 if (leaveStartTimeAMPM==0) {
												 if (leaveEndTimeAMPM==0) {
													 if ("0".equals(ampm)) {//全天上班
														 totalDays = totalDays + 0.5 ;
													 }else if ("1".equals(ampm)) {//上午上班下午休息
														 totalDays = totalDays + 0.5 ;
													 }else if ("2".equals(ampm)) {//上午休息下午上班
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													 }
												 }else {
													 if ("0".equals(ampm)) {//全天上班
														 totalDays = totalDays + 1 ;
													 }else if ("1".equals(ampm)) {//上午上班下午休息
//														 weekend = weekend + 0.5;
														 totalDays = totalDays + 0.5 ;
													 }else if ("2".equals(ampm)) {//上午休息下午上班
//														 weekend = weekend + 0.5;
														 totalDays = totalDays + 0.5 ;
													 }
												}
											 }else{
												 if (leaveEndTimeAMPM==1) {
													 if ("0".equals(ampm)) {//全天上班
														 totalDays = totalDays + 0.5 ;
													 }else if ("1".equals(ampm)) {//上午上班下午休息
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													 }else if ("2".equals(ampm)) {//上午休息下午上班
														 totalDays = totalDays + 0.5 ;
													 }
												}else {
													System.err.println("不存在的情形");
												}
												 
											 }
										}else{// 看开头 不看 结束
											if (leaveStartTimeAMPM==0) {
												 if ("0".equals(ampm)) {//全天上班
													 totalDays = totalDays + 1 ;
												 }else if ("1".equals(ampm)) {//上午上班下午休息
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												 }else if ("2".equals(ampm)) {//上午休息下午上班
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												 }
											}else {
												 if ("0".equals(ampm)) {//全天上班
													 totalDays = totalDays + 0.5 ;
												 }else if ("1".equals(ampm)) {//上午上班下午休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												 }else if ("2".equals(ampm)) {//上午休息下午上班
													 totalDays = totalDays + 0.5 ;
												 }
											}
										}
									 }else{// 不看开头 看 结尾
										 if (isViewEnd) {
											if (leaveEndTimeAMPM==0) {
												 if ("0".equals(ampm)) {//全天上班
													 totalDays = totalDays + 0.5 ;
												 }else if ("1".equals(ampm)) {//上午上班下午休息
													 totalDays = totalDays + 0.5 ;
												 }else if ("2".equals(ampm)) {//上午休息下午上班
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												 }
											}else {
												 if ("0".equals(ampm)) {//全天上班
													 totalDays = totalDays + 1 ;
												 }else if ("1".equals(ampm)) {//上午上班下午休息
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												 }else if ("2".equals(ampm)) {//上午休息下午上班
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												 }
											}
										 }else {// 不看开头和结束
											
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 1 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }
										}
									 }
								 }else{// 管当前日期 不管 结束日期
									 if (isViewStart) {
										if (leaveStartTimeAMPM==0) {
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 1 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }
										}else {
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 0.5 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
												 totalDays = totalDays + 0.5 ;
											 }
											
										}
									 }else {
										 if ("0".equals(ampm)) {//全天上班
											 totalDays = totalDays + 1 ;
										 }else if ("1".equals(ampm)) {//上午上班下午休息
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										 }else if ("2".equals(ampm)) {//上午休息下午上班
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										 }
									}
								}
							 }else {// 不管开始 只管结束
								 if (lNow == lEnd) {
									 if (isViewEnd) {
										 if (leaveEndTimeAMPM==0) {
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 0.5 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											 }
										 }else {
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 1 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }
										 }
									 }else {
										 
										 if ("0".equals(ampm)) {//全天上班
											 totalDays = totalDays + 1 ;
										 }else if ("1".equals(ampm)) {//上午上班下午休息
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										 }else if ("2".equals(ampm)) {//上午休息下午上班
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										 }
										
									 }
									 
								 }else {
									 
									 if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 1 ;
									 }else if ("1".equals(ampm)) {//上午上班下午休息
//										 weekend = weekend + 0.5;
										 totalDays = totalDays + 0.5 ;
									 }else if ("2".equals(ampm)) {//上午休息下午上班
//										 weekend = weekend + 0.5;
										 totalDays = totalDays + 0.5 ;
									 }
								}
							}
					    }
					    cal.add(Calendar.DATE, 1);
					}
					
                    JSONObject tempParm = new JSONObject();
                    tempParm.put("uuid", uuid);
                    tempParm.put("calNo", calNo);
                    tempParm.put("applyUserId", object.getString("applyUserId"));
                    tempParm.put("leaveType", object.getString("leaveType"));
                    tempParm.put("leavedays", totalDays);
//                    tempParm.put("actualDays", totalDays - weekend);
					 
                    leaveApplicationService.insertLeaveApplyTemp(tempParm);
					
				}
				 JSONObject tempParm = new JSONObject();
				 tempParm.put("calNo", calNo);
				
				List<JSONObject> statisticsList =  leaveApplicationService.getLeaveApplyTempStatistics(tempParm);
				leaveApplicationService.deleteLeaveApplyTemp(tempParm);
				if (statisticsList!=null && statisticsList.size() > 0 ) {
					List<JSONObject>  typeList = leaveApplicationService.getLeaveTypeList(param);
					if(typeList==null || typeList.size() == 0){
						param.put("schoolId", "ALL");
						typeList = leaveApplicationService.getLeaveTypeList(param);
					}
					HashMap<String, String> map = new HashMap<String, String>();
					for (int i = 0; i < typeList.size(); i++) {
						JSONObject object = typeList.get(i);
						map.put(object.getString("types"), object.getString("typeName"));
					}
					for (int i = 0; i < statisticsList.size(); i++) {
						JSONObject object = statisticsList.get(i);
						object.put("leaveTypeName", map.get(object.getString("leaveType")));
						object.put("row", i + 1);

//						int index = indexInArray(object.getString("applyUserId"), teacherIdNames[0]);
//						if(index>-1){
//							object.put("applyUserName", teacherIdNames[1][index]);
//						}
						
					}
				}
				
				response.put("data", statisticsList);
				setPromptMessage(response, "0", "查询成功");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
 	}
 	
 	@RequestMapping(value = "/getLeaveStatisticsList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveStatisticsList(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		Date startTime = request.getDate("startTime");
		Date endTime = request.getDate("endTime");
		 Calendar calendar = Calendar.getInstance();
		 Date date = new Date();
		if ( startTime==null  ) {
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, -100);
			startTime = calendar.getTime();
			request.put("startTime", startTime);
		}
		if (endTime==null) {
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, 100);
			endTime = calendar.getTime();
			request.put("endTime", endTime);
		}
		
		JSONObject param = request;
		String schoolId = getXxdm(req);
		param.put("schoolId",schoolId);
 
		String applyTeacherName = request.getString("applyTeacherName");
		int page = request.getIntValue("page");
		int pageSize = request.getIntValue("pageSize");
		page = page<=0 ? 1:page;
		pageSize = pageSize <= 0 ? 20 : pageSize;
		int position = (page -1 ) * pageSize;
		param.put("position", position);
		param.put("pageSize", pageSize);
		List<JSONObject> data =new ArrayList<JSONObject>();
		String selectedSemester =getCurXnxq(req);
		School school = getSchool(req,null);
		String[][] teacherIdNames = getTeacherIdNames(school,selectedSemester);
		if (StringUtils.isNotBlank(applyTeacherName)) {
			List<String> teacherList = new ArrayList<String>();
			for (int i = 0; i < teacherIdNames[0].length; i++) {
				System.out.println(teacherIdNames[1][i] );
				if (teacherIdNames[1][i].contains(applyTeacherName.trim())) {
					teacherList.add(teacherIdNames[0][i]);
				}
			}
			if (teacherList.size() > 0 ) {
				param.put("teacherList", teacherList);
			}else{
				response.put("data", data);
				setPromptMessage(response, "0", "查询成功");//找不到该名字的老师
				return response;
			}
		}
		
		try {
			int rowCnt = 0;
			rowCnt = leaveApplicationService.getLeaveStatisticsCnt(param);
			response.put("page", page);
			response.put("pageSize", pageSize);
			response.put("rowCnt", rowCnt);
			if (pageSize > 0 ) {
				response.put("pageCnt", rowCnt/pageSize + (rowCnt%pageSize == 0? 0 : 1 ) );
			}
		    data = leaveApplicationService.getLeaveStatistics(param);
			if (data != null) {
				param.put("schoolId","ALL");
				List<JSONObject> festivallist = leaveApplicationService.getFestivalList(param);
				HashMap<Date, Integer> festivalMap = new HashMap<Date, Integer>();
				HashMap<Date, String> festival2Map = new HashMap<Date, String>();
				for (int i = 0; i < festivallist.size(); i++) {
					JSONObject object = festivallist.get(i);
					festivalMap.put(object.getDate("festivalDay"), object.getInteger("onduty"));
					festival2Map.put(object.getDate("festivalDay"), object.getString("ampm"));
				}
				param.put("schoolId",schoolId);
				festivallist = leaveApplicationService.getFestivalList(param);
				for (int i = 0; i < festivallist.size(); i++) {
					JSONObject object = festivallist.get(i);
					festivalMap.put(object.getDate("festivalDay"), object.getInteger("onduty"));
					festival2Map.put(object.getDate("festivalDay"), object.getString("ampm"));
				}
				
				String calNo = UUIDUtil.getUUID();
				Calendar cal = Calendar.getInstance();
				for (int i = 0; i < data.size(); i++) {
					JSONObject object = data.get(i);
					Date leaveStartTime = object.getDate("leaveStartTime");
					Date leaveEndTime = object.getDate("leaveEndTime");
					
					int leaveStartTimeAMPM = object.getIntValue("leaveStartTimeAMPM");
					int leaveEndTimeAMPM = object.getIntValue("leaveEndTimeAMPM");
					//以最晚开始为起点
					Date tempStartDate = leaveStartTime.getTime() - startTime.getTime() > 0? leaveStartTime:startTime;
					long lStart = tempStartDate.getTime(); 
					boolean isViewStart = leaveStartTime.getTime() - startTime.getTime() >= 0? true : false;
					//以最早结束为终点
					Date tempEndDate = leaveEndTime.getTime() - endTime.getTime() > 0? endTime:leaveEndTime;
					long lEnd = tempEndDate.getTime(); 
					boolean isViewEnd =  leaveEndTime.getTime() - endTime.getTime() > 0?  false : true;
					//boolean isSame = tempEndDate.getTime() - tempStartDate.getTime() == 0 ? true : false;
					
					String uuid = UUIDUtil.getUUID();
					cal.setTime(tempStartDate);//设置当前日期为 
//					double weekend = 0 ;
					double totalDays = 0;
					
					while (true) {
						 
						if (cal.getTime().getTime() - tempEndDate.getTime() > 0 ) {// 遍历的日期超越了 最后的日期跳出
							break;
						}
						 
						Integer integer = festivalMap.get(cal.getTime());//上下班标志
						String ampm = festival2Map.get(cal.getTime());//上下午标志
						long lNow =  cal.getTime().getTime();
						
						if (integer==null) {//没有设置是否上下班
							//如果是周六日
							if( cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY  ){
								//没有上班标志，不统计
							}else {//不是周六日
								 if (lNow ==  lStart) {//当前日期等于开始时间
									 if (lNow == lEnd) {//当前日期等于结束日期
										 if (isViewStart) {// 要看开始
											if (isViewEnd) {//看结束
												if (leaveStartTimeAMPM==0) {
													if (leaveEndTimeAMPM==0) { //都是上午
														 
														totalDays = totalDays + 0.5 ;
													}else {//同一天的上下午
														 
														totalDays = totalDays + 1 ;
													}
													
												}else {
													if (leaveEndTimeAMPM==1) {// 同一天都是下午
														 
														totalDays = totalDays + 0.5 ;
													}else {
														System.err.println("不存在的情形");
													}
												}
												
											}else {//只看开始不看结束
												if (leaveStartTimeAMPM==0) {// 开始的上午
												 
													totalDays = totalDays + 1 ;
												}else {// 开始的上午
												 
													totalDays = totalDays + 0.5 ;
												}
											}
											 
										 }else {//不看开始
											 if (isViewEnd) {// 要看结束
												 if (leaveEndTimeAMPM==0) {// 结束日期的上午
													 
													 totalDays = totalDays + 0.5 ;
												}else {// 结束日期的下午
													 
													 totalDays = totalDays + 1 ;
												}	
											 }else {// 都不看
												 
												 totalDays = totalDays + 1 ;
											 }
										}
										 
									 }else {// 管当前日期 不管 结束日期
										 if (isViewStart) {
											 if (leaveStartTimeAMPM==0) {
												 
												 totalDays = totalDays + 1 ;
											}else {
												 
												 totalDays = totalDays + 0.5 ;
											}
										 }else {
											 totalDays = totalDays + 1 ;
										 }
										
									 }
									 
								 }else {
									 if (lNow == lEnd) {
										if (isViewEnd) {
											if (leaveEndTimeAMPM==0) {
												 totalDays = totalDays + 0.5 ;
											}else {
												 totalDays = totalDays + 1 ;
											}
										}else {
											 totalDays = totalDays + 1 ;
										}	
									 }else {//不是结束
										 totalDays = totalDays + 1 ;
									 }
								 }
							}
						}else if (integer==0) {// 设置 休息
							 if (lNow ==  lStart) {
								if (lNow ==  lEnd) {
									if (isViewStart) {
										if (isViewEnd) {
											if (leaveStartTimeAMPM==0) {
												if (leaveEndTimeAMPM==0) {//都是上午
													if ("0".equals(ampm)) {//全天休息
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													}else if ("1".equals(ampm)) {//上午休息
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													}else if ("2".equals(ampm)) {//上午上班没有假
														 totalDays = totalDays + 0.5 ;
													}
													
												}else {
													if ("0".equals(ampm)) {//全天休息
//														 weekend = weekend + 1;
//														 totalDays = totalDays + 1 ;
													}else if ("1".equals(ampm)) {//上午休息
//														 weekend = weekend + 0.5;
														 totalDays = totalDays + 0.5 ;
													}else if ("2".equals(ampm)) {//上午上班没有假
//														 weekend = weekend + 0.5;
														 totalDays = totalDays + 0.5 ;
													}
													
												}
											}else {
                                                if (leaveEndTimeAMPM==1) {// 都是下午
                                                	if ("0".equals(ampm)) {//全天休息
//                                                		 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													}else if ("1".equals(ampm)) {//上午休息
														 totalDays = totalDays + 0.5 ;
													}else if ("2".equals(ampm)) {//上午上班没有假
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													}
												}else {
													System.err.println("不存在的情形");
												}
											}
											
										}else {// 只看开始 不看 结束
											if (leaveStartTimeAMPM==0) {
												if ("0".equals(ampm)) {//全天休息
//													 weekend = weekend + 1;
//													 totalDays = totalDays + 1 ;
												}else if ("1".equals(ampm)) {//上午休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 1 ;
												}else if ("2".equals(ampm)) {//上午上班没有假
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												}
												
											}else {
												if ("0".equals(ampm)) {//全天休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												}else if ("1".equals(ampm)) {//上午休息
													 totalDays = totalDays + 0.5 ;
												}else if ("2".equals(ampm)) {//上午上班没有假下午休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												}
											}
										}
									}else {// 不看 开始 看结束
										if (isViewEnd) {
											if (leaveEndTimeAMPM==0) {
												if ("0".equals(ampm)) {//全天休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												}else if ("1".equals(ampm)) {//上午休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												}else if ("2".equals(ampm)) {//上午上班没有假
													 totalDays = totalDays + 0.5 ;
												}
											}else {
												if ("0".equals(ampm)) {//全天休息
//													 weekend = weekend + 1;
//													 totalDays = totalDays + 1 ;
												}else if ("1".equals(ampm)) {//上午休息
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												}else if ("2".equals(ampm)) {//上午上班没有假
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5; 
												}
											}
										}else {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 1;
//												 totalDays = totalDays + 1 ;
											}else if ("1".equals(ampm)) {//上午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5; 
											}
										}
									}
								}else {//只管开头不管结束
									if (isViewStart) {
										if (leaveStartTimeAMPM==0) {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 1;
//												 totalDays = totalDays + 1 ;
											}else if ("1".equals(ampm)) {//上午休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 1 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5; 
											}
										}else {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											}else if ("1".equals(ampm)) {//上午休息
												 totalDays = totalDays + 0.5 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5; 
											}
										}
									}else {
										if ("0".equals(ampm)) {//全天休息
//											 weekend = weekend + 1;
//											 totalDays = totalDays + 1 ;
										}else if ("1".equals(ampm)) {//上午休息
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										}else if ("2".equals(ampm)) {//上午上班没有假
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5; 
										}
									}
								}
							 }else {// 只管后面日期
								if (lNow ==  lEnd) {
									if (isViewEnd) {
										if (leaveEndTimeAMPM==1) {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 1;
//												 totalDays = totalDays + 1 ;
											}else if ("1".equals(ampm)) {//上午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5; 
											}
										}else {
											if ("0".equals(ampm)) {//全天休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											}else if ("1".equals(ampm)) {//上午休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											}else if ("2".equals(ampm)) {//上午上班没有假
												 totalDays = totalDays + 0.5; 
											}
											
										}
									}else {
										
										if ("0".equals(ampm)) {//全天休息
//											 weekend = weekend + 1;
//											 totalDays = totalDays + 1 ;
										}else if ("1".equals(ampm)) {//上午休息
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										}else if ("2".equals(ampm)) {//上午上班没有假
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5; 
										}
										
									}
								}else {
									if ("0".equals(ampm)) {//全天休息
//										 weekend = weekend + 1;
//										 totalDays = totalDays + 1 ;
									}else if ("1".equals(ampm)) {//上午休息
//										 weekend = weekend + 0.5;
										 totalDays = totalDays + 0.5 ;
									}else if ("2".equals(ampm)) {//上午上班没有假
//										 weekend = weekend + 0.5;
										 totalDays = totalDays + 0.5; 
									}
								}
							 }
							
						}else {// 上班
							 if (lNow ==  lStart) {
								 if (lNow == lEnd) {
									 if (isViewStart) {
										if (isViewEnd) {
											 if (leaveStartTimeAMPM==0) {
												 if (leaveEndTimeAMPM==0) {
													 if ("0".equals(ampm)) {//全天上班
														 totalDays = totalDays + 0.5 ;
													 }else if ("1".equals(ampm)) {//上午上班下午休息
														 totalDays = totalDays + 0.5 ;
													 }else if ("2".equals(ampm)) {//上午休息下午上班
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													 }
												 }else {
													 if ("0".equals(ampm)) {//全天上班
														 totalDays = totalDays + 1 ;
													 }else if ("1".equals(ampm)) {//上午上班下午休息
//														 weekend = weekend + 0.5;
														 totalDays = totalDays + 0.5 ;
													 }else if ("2".equals(ampm)) {//上午休息下午上班
//														 weekend = weekend + 0.5;
														 totalDays = totalDays + 0.5 ;
													 }
												}
											 }else{
												 if (leaveEndTimeAMPM==1) {
													 if ("0".equals(ampm)) {//全天上班
														 totalDays = totalDays + 0.5 ;
													 }else if ("1".equals(ampm)) {//上午上班下午休息
//														 weekend = weekend + 0.5;
//														 totalDays = totalDays + 0.5 ;
													 }else if ("2".equals(ampm)) {//上午休息下午上班
														 totalDays = totalDays + 0.5 ;
													 }
												}else {
													System.err.println("不存在的情形");
												}
												 
											 }
										}else{// 看开头 不看 结束
											if (leaveStartTimeAMPM==0) {
												 if ("0".equals(ampm)) {//全天上班
													 totalDays = totalDays + 1 ;
												 }else if ("1".equals(ampm)) {//上午上班下午休息
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												 }else if ("2".equals(ampm)) {//上午休息下午上班
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												 }
											}else {
												 if ("0".equals(ampm)) {//全天上班
													 totalDays = totalDays + 0.5 ;
												 }else if ("1".equals(ampm)) {//上午上班下午休息
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												 }else if ("2".equals(ampm)) {//上午休息下午上班
													 totalDays = totalDays + 0.5 ;
												 }
											}
										}
									 }else{// 不看开头 看 结尾
										 if (isViewEnd) {
											if (leaveEndTimeAMPM==0) {
												 if ("0".equals(ampm)) {//全天上班
													 totalDays = totalDays + 0.5 ;
												 }else if ("1".equals(ampm)) {//上午上班下午休息
													 totalDays = totalDays + 0.5 ;
												 }else if ("2".equals(ampm)) {//上午休息下午上班
//													 weekend = weekend + 0.5;
//													 totalDays = totalDays + 0.5 ;
												 }
											}else {
												 if ("0".equals(ampm)) {//全天上班
													 totalDays = totalDays + 1 ;
												 }else if ("1".equals(ampm)) {//上午上班下午休息
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												 }else if ("2".equals(ampm)) {//上午休息下午上班
//													 weekend = weekend + 0.5;
													 totalDays = totalDays + 0.5 ;
												 }
											}
										 }else {// 不看开头和结束
											
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 1 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }
										}
									 }
								 }else{// 管当前日期 不管 结束日期
									 if (isViewStart) {
										if (leaveStartTimeAMPM==0) {
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 1 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }
										}else {
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 0.5 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
												 totalDays = totalDays + 0.5 ;
											 }
											
										}
									 }else {
										 if ("0".equals(ampm)) {//全天上班
											 totalDays = totalDays + 1 ;
										 }else if ("1".equals(ampm)) {//上午上班下午休息
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										 }else if ("2".equals(ampm)) {//上午休息下午上班
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										 }
									}
								}
							 }else {// 不管开始 只管结束
								 if (lNow == lEnd) {
									 if (isViewEnd) {
										 if (leaveEndTimeAMPM==0) {
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 0.5 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
//												 weekend = weekend + 0.5;
//												 totalDays = totalDays + 0.5 ;
											 }
										 }else {
											 if ("0".equals(ampm)) {//全天上班
												 totalDays = totalDays + 1 ;
											 }else if ("1".equals(ampm)) {//上午上班下午休息
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }else if ("2".equals(ampm)) {//上午休息下午上班
//												 weekend = weekend + 0.5;
												 totalDays = totalDays + 0.5 ;
											 }
										 }
									 }else {
										 
										 if ("0".equals(ampm)) {//全天上班
											 totalDays = totalDays + 1 ;
										 }else if ("1".equals(ampm)) {//上午上班下午休息
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										 }else if ("2".equals(ampm)) {//上午休息下午上班
//											 weekend = weekend + 0.5;
											 totalDays = totalDays + 0.5 ;
										 }
										
									 }
									 
								 }else {
									 
									 if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 1 ;
									 }else if ("1".equals(ampm)) {//上午上班下午休息
//										 weekend = weekend + 0.5;
										 totalDays = totalDays + 0.5 ;
									 }else if ("2".equals(ampm)) {//上午休息下午上班
//										 weekend = weekend + 0.5;
										 totalDays = totalDays + 0.5 ;
									 }
									
								}
								 
								
							}
					    }
					    cal.add(Calendar.DATE, 1);
					}
					
                    JSONObject tempParm = new JSONObject();
                    tempParm.put("uuid", uuid);
                    tempParm.put("calNo", calNo);
                    tempParm.put("applyUserId", object.getString("applyUserId"));
                    tempParm.put("leaveType", object.getString("leaveType"));
                    tempParm.put("leavedays", totalDays);
//                    tempParm.put("actualDays", totalDays - weekend);
					 
                    leaveApplicationService.insertLeaveApplyTemp(tempParm);
					
				}
				 JSONObject tempParm = new JSONObject();
				 tempParm.put("calNo", calNo);
				
				List<JSONObject> statisticsList =  leaveApplicationService.getLeaveApplyTempStatistics(tempParm);
				leaveApplicationService.deleteLeaveApplyTemp(tempParm);
				if (statisticsList!=null && statisticsList.size() > 0 ) {
					List<JSONObject>  typeList = leaveApplicationService.getLeaveTypeList(param);
					if(typeList==null || typeList.size() == 0){
						param.put("schoolId", "ALL");
						typeList = leaveApplicationService.getLeaveTypeList(param);
					}
					HashMap<String, String> map = new HashMap<String, String>();
					for (int i = 0; i < typeList.size(); i++) {
						JSONObject object = typeList.get(i);
						map.put(object.getString("types"), object.getString("typeName"));
					}
					for (int i = 0; i < statisticsList.size(); i++) {
						JSONObject object = statisticsList.get(i);
						object.put("leaveTypeName", map.get(object.getString("leaveType")));
						object.put("row", i + 1);

						int index = indexInArray(object.getString("applyUserId"), teacherIdNames[0]);
						if(index>-1){
							object.put("applyUserName", teacherIdNames[1][index]);
						}
						
					}
				}
				
				response.put("data", statisticsList);
				setPromptMessage(response, "0", "查询成功");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
	
 	
 	@RequestMapping(value = "/getCalendarList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCalendarList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		
 		JSONObject response = new JSONObject();
 		String year = request.getString ("year");
 		String month =   request.getString("month");
 		String firstDay = year +"-" + month + "-01";
 		String schoolId = getXxdm(req);
 		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		Date dt = null;
 		try {
 			dt = format.parse(firstDay);
		} catch (ParseException e) {
			setPromptMessage(response, "-1", "日期转换失败"); 
			e.printStackTrace();
		}
 		if (dt == null ) {
			return response;
		}
 		
 		 String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
	     Calendar cal = Calendar.getInstance();
	     cal.setTime(dt);
	     int monthDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	     HashMap<Date, JSONObject> map = new LinkedHashMap<Date, JSONObject>();
	     JSONObject param = new JSONObject();
	     param.put("startTime", dt);
	     for (int i = 0; i < monthDay; i++) {
 			 int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
	 	     if (w < 0){
	 	         w = 0;
	 	     }
	 	     JSONObject obj = new JSONObject();
	 	     obj.put("date", format.format(cal.getTime()));
	 	     obj.put("xq", weekDays[w]);
	 	     map.put(cal.getTime(), obj);
	 	     cal.add(Calendar.DATE,1);
		 }
	     param.put("endTime", cal.getTime());
	     param.put("schoolId", "ALL");
 		 List<JSONObject> festivallist = leaveApplicationService.getFestivalList(param);
 		 for (int i = 0; i < festivallist.size(); i++) {
			JSONObject obj = festivallist.get(i);
			 Date date = obj.getDate("festivalDay");
			 JSONObject object = map.get(date);
			 if (object != null) {
				 object.put("festivalName", obj.getString("festivalName"));
				 object.put("onduty", obj.getString("onduty"));
				 object.put("ampm", obj.getString("ampm"));
				 object.put("sign", obj.getString("sign"));
			 }
		}
 		 param.put("schoolId", schoolId);
 		 festivallist = leaveApplicationService.getFestivalList(param);
 		 for (int i = 0; i < festivallist.size(); i++) {
			JSONObject obj = festivallist.get(i);
			 Date date = obj.getDate("festivalDay");
			 JSONObject object = map.get(date);
			 if (object != null) {
				 object.put("festivalName", obj.getString("festivalName"));
				 object.put("onduty", obj.getString("onduty"));
				 object.put("ampm", obj.getString("ampm"));
				 object.put("sign", obj.getString("sign"));
			 }
		}
 		 List<JSONObject> list = new ArrayList<JSONObject>();
 		 for (Date dt2 : map.keySet()) {
 			JSONObject object = map.get(dt2);
 			list.add(object);
 		 }
 		response.put("data", list);
 		setPromptMessage(response, "0", "成功！");
 		return response;
	}
 	
 	@RequestMapping(value = "/updateCalendarList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateCalendarList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
 		JSONObject response = new JSONObject();
 		JSONObject param = new JSONObject();
 		String year = request.getString("year");
 		String month = request.getString("month");
 		String schoolId = getXxdm(req);
 		param.put("schoolId", schoolId);
 		List<JSONObject> list = new ArrayList<JSONObject>();
 		JSONArray  jsonArray  = request.getJSONArray("calender");
 		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		Calendar cal = Calendar.getInstance();
 		Date day = null;
 		for (int i = 0; i < jsonArray.size(); i++) {
 			JSONObject object = jsonArray.getJSONObject(i);
 			if (StringUtils.isNotBlank(object.getString("onduty"))) {
 				object.put("uuid", UUIDUtil.getUUID());
 				object.put("schoolId",schoolId);
 				String onduty = object.getString("onduty");
 				day = object.getDate("date");
 				cal.setTime(day);
 				if (cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
 					if ("0".equals(onduty)) {
 						object.put("ampm", "0");
 					}
				}else {
					if ("1".equals(onduty)) {
						object.put("ampm", "0");
					}
				}
 				
 				if (  StringUtils.isBlank(object.getString("sign"))) {
 					object.put("sign", "0");
				}
 				
 				try {
					object.put("festivalDay", format.parse(object.getString("date")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
 				if (StringUtils.isBlank(object.getString("festivalName"))) {
 					object.put("festivalName", "");
				}
 				list.add(jsonArray.getJSONObject(i));
			}
		}
 		String firstDay = year +"-" + month + "-01";
 		//DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		Date dt = null;
 		try {
 			dt = format.parse(firstDay);
		} catch (ParseException e) {
			setPromptMessage(response, "-1", "日期转换失败"); 
			e.printStackTrace();
		}
	     cal.setTime(dt);
	     param.put("startTime", dt);
	     int monthDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	     cal.add(Calendar.DATE,monthDay - 1);
	     param.put("endTime",  cal.getTime() );
 		leaveApplicationService.deleteFestival(param);
 		if (list.size() > 0) {
 			leaveApplicationService.insertFestivalList(list);
		}
 		
 		setPromptMessage(response, "0", "设置成功！"); 
 		
 		return response;
 	}
}
