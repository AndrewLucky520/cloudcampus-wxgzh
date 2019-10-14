package com.talkweb.leaveapply.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;

/**
 * 教师请假应用H5
 * 
 * @author 90732
 */
@Controller
@RequestMapping("/leaveManage")
public class LeaveApplicationActionH5 extends LeaveApplicationBaseAction {
	@Value("#{settings['currentTermInfo']}")
	protected String curTermInfoId;

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
	
	/**
	 * 获取登录人员的角色 0 管理员，1 审核人员，2 一般老师
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getLeaveRoleH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveRole(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject param = new JSONObject();
		String schoolId = request.getString("schoolId");
		if (schoolId == null || schoolId.length() == 0) {
			JSONObject response = new JSONObject();
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}
		String termInfoId = curTermInfoId;
		String accountId = request.getString("accountId");
		param.put("schoolId", schoolId);
		param.put("termInfoId", termInfoId);
		param.put("accountId", accountId);

		String teacherUserId = request.getString("userId");
		boolean flag = leaveApplicationService.isMoudleManager("1029", teacherUserId);
		if (flag) {
			param.put("isAdmin", 1);
		} else {
			param.put("isAdmin", 0);
		}

		return getLeaveRole(param);
	}

	/**
	 * 获取请假列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getLeaveListH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveList(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		checkApplyStartEndDate(param);
		String accountId = request.getString("accountId");
		String schoolId = request.getString("schoolId");
		if (schoolId == null || schoolId.length() == 0) {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}
		param.put("schoolId", schoolId);
		param.put("accountId", accountId);
		String applyTeacherName = request.getString("applyTeacherName");
		int page = request.getIntValue("page");
		int pageSize = request.getIntValue("pageSize");
		page = page <= 0 ? 1 : page;
		pageSize = pageSize <= 0 ? 20 : pageSize;
		int position = (page - 1) * pageSize;
		param.put("position", position);
		param.put("pageSize", pageSize);

		String queryType = request.getString("queryType");
		String mystatus = request.getString("mystatus");
		param.put("leaveType", request.getString("leaveType"));

		List<JSONObject> data = new ArrayList<JSONObject>();
		String selectedSemester = curTermInfoId;
		String userId = request.getString("userId");
		if (userId == null || userId.length() == 0) {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}
		School school = getSchoolByUserId(Long.valueOf(userId));
		String[][] teacherIdNames = null;
		if(school != null) {
			teacherIdNames = getTeacherIdNames(school, selectedSemester);
		}
		if (StringUtils.isNotBlank(applyTeacherName)) {
			List<String> teacherList = new ArrayList<String>();
			if(teacherIdNames != null && teacherIdNames.length > 0) {
				for (int i = 0; i < teacherIdNames[0].length; i++) {
					System.out.println(teacherIdNames[1][i]);
					if (teacherIdNames[1][i].contains(applyTeacherName.trim())) {
						teacherList.add(teacherIdNames[0][i]);
					}
				}
			}
			if (teacherList.size() > 0) {
				param.put("teacherList", teacherList);
			} else {
				response.put("data", data);
				setPromptMessage(response, ERRCODE_SUCCESS, "查询成功");// 找不到该名字的老师
				return response;
			}
		}

		int rowCnt = 0;
		if ("0".equals(queryType)) {
			// 获取所有的
			rowCnt = leaveApplicationService.getAdminLeaveApplyListCnt(param);
			data = leaveApplicationService.getAdminLeaveApplyList(param);
		} else if ("1".equals(queryType)) {
			// 获取自己跟自己审核的
			rowCnt = leaveApplicationService.getAuditLeaveApplyListCnt(param);
			data = leaveApplicationService.getAuditLeaveApplyList(param);
		} else {
			// 获取自己的
			rowCnt = leaveApplicationService.getLeaveApplyListCnt(param);
			data = leaveApplicationService.getLeaveApplyList(param);
		}

		response.put("page", page);
		response.put("pageSize", pageSize);
		response.put("rowCnt", rowCnt);
		if (pageSize > 0) {
			response.put("pageCnt", rowCnt / pageSize + (rowCnt % pageSize == 0 ? 0 : 1));
		}

		List<JSONObject> list = leaveApplicationService.getLeaveTypeList(param);
		if (list == null || list.size() == 0) {
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
			row++;
			JSONObject jsonObject = iterator.next();
			JSONArray jsonArray = jsonObject.getJSONArray("auditDetail");
			String applyUserId = jsonObject.getString("applyUserId");
			String leaveType = jsonObject.getString("leaveType");
			int status = jsonObject.getIntValue("status");
			String reason = jsonObject.getString("reason");
//			if (reason != null && reason.length() > 12) {
//				reason = reason.substring(0, 12) + "...";
//			}
			jsonObject.put("reason", reason);
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
			if (StringUtils.isNotBlank(applyUserId) && teacherIdNames != null && teacherIdNames.length > 0) {
				int index = indexInArray(applyUserId, teacherIdNames[0]);
				if (index > -1) {
					jsonObject.put("applyUserName", teacherIdNames[1][index]);
				}
			}
			jsonObject.put("leaveTypeName", map.get(leaveType));
			for (int j = 0; j < jsonArray.size(); j++) {
				JSONObject object = jsonArray.getJSONObject(j);
				JSONArray audits = object.getJSONArray("audits");
				String auditor = object.getString("auditor");
				int statusDetail = object.getIntValue("status");
				if (status == statusDetail) {
					if (StringUtils.isNotBlank(auditor) && teacherIdNames != null && teacherIdNames.length > 0) {
						String auditorName = null;
						int index = indexInArray(auditor, teacherIdNames[0]);
						if (index > -1) {
							auditorName = teacherIdNames[1][index];
						}
						if (statusDetail == -1) {
							jsonObject.put("process", auditorName + "不同意");
							jsonObject.put("canDel", "1");
						} else if (statusDetail == 2) {
							jsonObject.put("process", "同意");
						}
					} else {
						// 无人审核
						if (audits != null) {
							if ("1".equals(queryType) && "0".equals(mystatus)) {
								jsonObject.put("process", "待我审核");
							} else {
								List<String> teacherNames = new ArrayList<String>();
								for (int k = 0; k < audits.size(); k++) {
									JSONObject obj = audits.getJSONObject(k);
									if(teacherIdNames != null && teacherIdNames.length > 0) {
										int index = indexInArray(obj.getString("teacherId"), teacherIdNames[0]);
										if (index > -1) {
											teacherNames.add(teacherIdNames[1][index]);
										}
									}
								}
								jsonObject.put("process", StringUtil.toStringBySeparator(teacherNames) + "待审核");
							}

						}
						if (j == 0) {
							jsonObject.put("canDel", "1");// 第一个审核的没人
						}

					}
					break;
				}
			}
		}
		response.put("data", data);
		setPromptMessage(response, ERRCODE_SUCCESS, "查询成功");
		return response;
	}

	@RequestMapping(value = "/isSetProcedureH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject isSetProcedure(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject param = new JSONObject();
		param.put("schoolId", request.getString("schoolId"));
		return isSetProcedure(param);
	}

	@RequestMapping(value = "/getLeaveTypeListH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveTypeList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		return getLeaveTypeList(request);
	}
	
	/**
	 * 判断老师是否已分配到请假组
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/checkTeacherInGroupH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject checkTeacherInGroup(HttpServletRequest req, @RequestBody JSONObject request,	HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		
		String schoolId = request.getString("schoolId");
		if (schoolId == null || schoolId.length() == 0) {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}
		
		String accountId = request.getString("accountId");
		param.put("teacherId", accountId);
		param.put("schoolId", schoolId);
		
		JSONObject obj = leaveApplicationService.getTeacherGroup(param);
		if (obj == null) {
			setPromptMessage(response, ERRCODE_ERROR, "您还没有分配到请假组,请联系管理员");
			return response;
		}
		
		setPromptMessage(response, ERRCODE_SUCCESS, "成功：已分配到请假组");
		return response;
	}
	
	/**
	 * 获取实际请假天数
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getLeaveActualDaysH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveActualDays(HttpServletRequest req, @RequestBody JSONObject request,	HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		
		String schoolId = request.getString("schoolId");
		if (schoolId == null || schoolId.length() == 0) {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}

		Date leaveStartTime = request.getDate("leaveStartTime");
		Date leaveEndTime = request.getDate("leaveEndTime");
		int leaveStartTimeAMPM = request.getIntValue("leaveStartTimeAMPM");
		int leaveEndTimeAMPM = request.getIntValue("leaveEndTimeAMPM");

		param.put("leaveStartTime", leaveStartTime);
		param.put("leaveStartTimeAMPM", leaveStartTimeAMPM);
		param.put("leaveEndTime", leaveEndTime);
		param.put("leaveEndTimeAMPM", leaveEndTimeAMPM);

		if (leaveEndTime.getTime() - leaveStartTime.getTime() < 0) {
			setPromptMessage(response, ERRCODE_ERROR, "请假日期不合法");
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

		setPromptMessage(response, ERRCODE_SUCCESS, "获取成功");
		return response;
	}

	/**
	 * 教师请假
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/addLeaveApplyH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject insertLeaveApply(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;

		String schoolId = request.getString("schoolId");
		if (schoolId == null || schoolId.length() == 0) {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}

		String termInfoId = curTermInfoId;
		Date leaveStartTime = request.getDate("leaveStartTime");
		Date leaveEndTime = request.getDate("leaveEndTime");
		String reason = request.getString("reason");
		String leaveType = request.getString("leaveType");
		int leaveStartTimeAMPM = request.getIntValue("leaveStartTimeAMPM");
		int leaveEndTimeAMPM = request.getIntValue("leaveEndTimeAMPM");

		if (StringUtils.isNotBlank(termInfoId)) {
			param.put("schoolYear", termInfoId.substring(0, 4));
			param.put("term", termInfoId.substring(4));
		}
		String accountId = request.getString("accountId");
		String applicationId = UUIDUtil.getUUID();
		param.put("schoolId", schoolId);

		param.put("applicationId", applicationId);
		param.put("applyUserId", accountId);
		param.put("teacherId", accountId);
		JSONObject obj = leaveApplicationService.getTeacherGroup(param);
		String groupId = null;
		if (obj == null) {
			setPromptMessage(response, ERRCODE_ERROR, "您还没有分配到请假组,请联系管理员");
			return response;
		} else {
			groupId = obj.getString("groupId");
			if (StringUtils.isEmpty(groupId)) {
				setPromptMessage(response, ERRCODE_ERROR, "您还没有分配到请假组,请联系管理员");
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

		if (leaveEndTime.getTime() - leaveStartTime.getTime() < 0) {
			setPromptMessage(response, ERRCODE_ERROR, "请假日期不合法");
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
		
		List<JSONObject> list = leaveApplicationService.getAuditdaysList(param);
		JSONArray auditorObj = null;
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				JSONObject object = list.get(i);
				 Float daysBegin = object.getFloat("daysBegin");
				 Float daysEnd = object.getFloat("daysEnd");
				daysEnd = daysEnd != null ? daysEnd : 30000;
				if (totalDays >= daysBegin && totalDays < daysEnd) {
					groupId = object.getString("groupId");
					auditorObj = object.getJSONArray("auditorLevel");
					break;// 找到审批等级
				}
			}
		}

		if (auditorObj != null && auditorObj.size() > 0) {
			//1级审批列表
			msgReceiversArray = getMsgReceiversArray(1,auditorObj,schoolId,curTermInfoId);
			
			JSONObject param2 = new JSONObject();
			param2.put("applicationId", applicationId);
			param2.put("groupId", groupId);
			try {
				for (int i = 0; i < auditorObj.size(); i++) {
					JSONObject jsonObject = auditorObj.getJSONObject(i);
					int levelNum = jsonObject.getIntValue("levelNum");
					param2.put("levelNum", levelNum);
					if (levelNum == 1) {
						param2.put("status", 1);
					} else {
						param2.put("status", 0);
					}
					leaveApplicationService.insertProcedure(param2);
					JSONArray jsonArray = jsonObject.getJSONArray("auditors");
					if (jsonArray != null && jsonArray.size() > 0) {
						for (int j = 0; j < jsonArray.size(); j++) {
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
				setPromptMessage(response, ERRCODE_ERROR, "设置审批流程失败！");
				return response;
			}
		} else {
			setPromptMessage(response, ERRCODE_ERROR, "请假时长还没有设置审批人,请联系管理员！");
			return response;
		}

		JSONArray fileArray = param.getJSONArray("files");
		if (fileArray != null) {
			JSONObject param2 = new JSONObject();
			try {
				for (int i = 0; i < fileArray.size(); i++) {
					JSONObject object = fileArray.getJSONObject(i);
					param2.put("applicationId", applicationId);
					param2.put("appFileName", object.getString("appFileName"));
					param2.put("fileUrl", object.getString("fileUrl"));
					leaveApplicationService.updateLeaveApplyFile(param2);// 添加 申请文件
				}
			} catch (Exception e) {
				e.printStackTrace();
				setPromptMessage(response, ERRCODE_ERROR, "保存请假文件失败！");
				return response;
			}
		}
		try {
			leaveApplicationService.insertLeaveApply(param);// 添加请假记录
		} catch (Exception e) {
			setPromptMessage(response, ERRCODE_ERROR, "设置失败！");
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
			List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfoId);
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
			School school = commonDataService.getSchoolById(Long.valueOf(schoolId), curTermInfoId);
			if(school != null) {
				paramMsg.put("schoolId", school.getExtId());
			}
			//设置模板类型
			paramMsg.put("msgTemplateType", "JSQJ1");
			paramMsg.put("applicationId", applicationId);
			
			if(!sendAppMsg(paramMsg)) {
				setPromptMessage(response, ERRCODE_ERROR, "发送通知失败！");
				return response;
			}
		}
		
		setPromptMessage(response, ERRCODE_SUCCESS, "请假成功");
		return response;
	}

	@RequestMapping(value = "/uploadLeaveApplyFileH5")
	@ResponseBody
	public JSONObject uploadLeaveApplyFile(HttpServletRequest req, @RequestParam("fileBoby") MultipartFile file,
			HttpServletResponse res) {
		return uploadFile(file);
	}

	@RequestMapping(value = "/delLeaveApplyFileH5")
	@ResponseBody
	public JSONObject delLeaveApplyFile(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		return deleteFile(request);
	}

//	@RequestMapping(value = "/preDownloadFileH5")
//	public void preDownloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
//		downloadFile(req, res);
//	}

	@RequestMapping(value = "/getLeaveApplyH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeaveApply(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String schoolId = request.getString("schoolId");
		if (schoolId == null || schoolId.length() == 0) {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}

		param.put("schoolId", schoolId);
		String applicationId = request.getString("applicationId");
		if (StringUtils.isBlank(applicationId)) {
			setPromptMessage(response, ERRCODE_ERROR, "参数错误,请联系管理员。");
			return response;
		}
		String selectedSemester = curTermInfoId;
		String userId = request.getString("userId");
		if (userId == null || userId.length() == 0) {
			setPromptMessage(response, ERRCODE_ERROR, PROMPT_PARAM_ERROR);
			return response;
		}
		
		/**
		 * 判断身份
		 */
		User user = commonDataService.getUserById(Long.parseLong(schoolId), Long.parseLong(userId));
		if(user.getUserPart().getRole() == T_Role.Parent ||
			user.getUserPart().getRole() == T_Role.Student) {
			setPromptMessage(response, ERRCODE_NO_PERMISSION, PROMPT_NO_PERMISSION);
			return response;
		}
		
		boolean hasPermission = false;
		
		JSONObject jsonObject = leaveApplicationService.getLeaveApply(param);
		if(jsonObject == null) {
			setPromptMessage(response, ERRCODE_DELETED, PROMPT_DELETED);
			return response;
		} else {
			/**
			 * 用户权限检查
			 */
			String applyUserId = jsonObject.getString("applyUserId");
			if(String.valueOf(user.getAccountPart().getId()).equals(applyUserId)) {
				//是自己
				hasPermission = true;
			} else {
				/**
				 * 判断是否是审批人
				 */
				String accountId = String.valueOf(user.getAccountPart().getId());
				logger.info("getLeaveApplyH5 => accountId:" + accountId);
				JSONArray auditDetail = jsonObject.getJSONArray("auditDetail");
				if(auditDetail != null) {
					logger.info("getLeaveApplyH5 => auditDetail:" + auditDetail);
					for (int i = 0; i < auditDetail.size(); i++) {
						JSONObject object = auditDetail.getJSONObject(i);
						JSONArray audits = object.getJSONArray("audits");
						if (audits != null) {
							for (int j = 0; j < audits.size(); j++) {
								JSONObject object2 = audits.getJSONObject(j);
								String teacherId = object2.getString("teacherId");
								if (StringUtils.isNotBlank(teacherId)) {
									if(teacherId.equals(accountId)) {
										hasPermission = true;
										break;
									}
								}
							}
						}
						if(hasPermission) {
							break;
						}
					}
				} else {
					logger.info("getLeaveApplyH5 => auditDetail is null");
				}
			}
		}
		
		if(!hasPermission) {
			setPromptMessage(response, ERRCODE_NO_PERMISSION, PROMPT_NO_PERMISSION);
			return response;
		}
		
		School school = getSchoolByUserId(Long.valueOf(userId));

		String[][] teacherIdNames = getTeacherIdNames(school, selectedSemester);
		SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
		JSONArray jsonArray = jsonObject.getJSONArray("auditDetail");
		String teacherId = null;
		for (int j = 0; j < jsonArray.size(); j++) {
			JSONObject object = jsonArray.getJSONObject(j);
			String auditor = object.getString("auditor");
			if (StringUtils.isNotBlank(auditor)) {

				int index = indexInArray(auditor, teacherIdNames[0]);
				if (index > -1) {
					object.put("auditorName", teacherIdNames[1][index]);
				}
			}
			Date processDate = jsonObject.getDate("processDate");
			try {
				if (processDate != null) {
					jsonObject.put("processDate", format.format(processDate));
				}
			} catch (Exception e) {

			}

			JSONArray jArray = object.getJSONArray("audits");
			if (jArray != null) {
				for (int i = 0; i < jArray.size(); i++) {
					JSONObject object2 = jArray.getJSONObject(i);
					teacherId = object2.getString("teacherId");
					if (StringUtils.isNotBlank(teacherId)) {
						int index = indexInArray(teacherId, teacherIdNames[0]);
						if (index > -1) {
							object2.put("teacherName", teacherIdNames[1][index]);
						}
					}
				}
			}
		}
		String applyUserId = jsonObject.getString("applyUserId");
		if (StringUtils.isNotBlank(applyUserId)) {
			int index = indexInArray(applyUserId, teacherIdNames[0]);
			if (index > -1) {
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
		
		/**
		 * 设置返回状态
		 * 1、正常显示  2、当前身份没有这次数据  3、当前数据已删除   4、当前数据已结束  5.当前数据未开始
		 */
		String retCode = ERRCODE_SUCCESS;
		String prompt = PROMPT_SUCCESS;
//		Date now = new Date();
//		if(now.getTime() < leaveStartTime.getTime()) {
//			retCode = ERRCODE_NOT_START;
//			prompt = PROMPT_NOT_START;
//		} else if(now.getTime()>leaveEndTime.getTime()) {
//			retCode = ERRCODE_EXPIRED;
//			prompt = PROMPT_EXPIRED;
//		}
		
		setPromptMessage(response, retCode, prompt);
		return response;
	}

	@RequestMapping(value = "/delLeaveApplyH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delLeaveApply(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		return deleteLeaveApply(request);
	}

	/**
	 * 审核人员审核 ： 1 获取审核列表 2判断当前审核状态 不同意 就更新 同意看有没有下一层，没有也更新
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/updateProcedureH5", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateProcedure(HttpServletRequest req, @RequestBody JSONObject request,HttpServletResponse res) {
		request.put("curTermInfoId", curTermInfoId);
		return super.updateProcedure(request,res);
	}
}
