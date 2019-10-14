package com.talkweb.venueManage.action;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.auth.service.AuthService;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.venueManage.service.VenueManageAppService;
import com.talkweb.venueManage.service.VenueManageCommonService;
import com.talkweb.venueManage.service.VenueManageSetService;

/** 
 * 场馆使用-app接口
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年4月15日 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Controller
@RequestMapping(value = "/venueManage/app/")
public class VenueManageAppAction extends BaseAction{
	private static final Logger logger = LoggerFactory.getLogger(VenueManageAppAction.class);
	
	 @Autowired
	 private AllCommonDataService allCommonDataService;
	 
	 @Autowired
	 private VenueManageSetService venueManageSetService;
	 
	 @Autowired
	 private VenueManageCommonService venueManageCommonService;
	 
	 @Autowired
	 private VenueManageAppService venueManageAppService;
	 
	 @Autowired
	 private AuthService authServiceImpl;
	 
	 private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
	 }
	 /**
		 * 删除场馆申请单（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/deleteAppVenueApply", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject deleteAppVenueApply(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId =  request.getString("schoolId");
				String applyId = request.getString("applyId");
			
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
				
				int i = venueManageSetService.deleteVenueApply(param);
				if ( i>0 ) {
					setPromptMessage(response, "0", "刪除成功");
				}else{
					setPromptMessage(response, "-1", "刪除失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "刪除失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * 获取所有场馆（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAppVenueSetList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAppVenueSetList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId =  request.getString("schoolId");
				String venueTypeId = request.getString("venueTypeId");
				String useDate = request.getString("useDate");
				String useStartDate = request.getString("useStartDate");
				String useEndDate = request.getString("useEndDate");
				param.put("schoolId", schoolId);
				param.put("venueTypeId", venueTypeId);
				if(StringUtils.isNotEmpty(useDate)&& StringUtils.isNotEmpty(useStartDate)&& StringUtils.isNotEmpty(useEndDate)){
					param.put("useStartTime", useDate+" "+useStartDate+":00");
					param.put("useEndTime", useDate+" "+useEndDate+":59");
				}
				List<JSONObject> data = venueManageSetService.getVenueSetList(param);
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
		 * 获取所有场馆类别列表（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAppAllVenueTypeList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAppAllVenueTypeList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId =  request.getString("schoolId");
				param.put("schoolId", schoolId);
				List<JSONObject> data = venueManageCommonService.getAllVenueTypeList(param);
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
		 * 获取所有场馆申请单列表（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAppVenueApplyList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAppVenueApplyList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = request.getString("schoolId");
				
				Long userId = request.getLong("userId");
				String selectedSemester = allCommonDataService.getCurTermInfoId(Long.parseLong(schoolId));
				User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
				String teacherId ="";
				if(user!=null){
					teacherId = String.valueOf(user.getAccountPart().getId());
				}
				//   teacherId = "100984739"; //“1303”
			      
			    //teacherId = "100984779";//"艾芳芳"
			     
			    //   teacherId = "100985011"; //“陈佳”
			      
			       boolean isManager=authServiceImpl.getIsMoudleManagerByAccountId(Long.parseLong(schoolId), Long.parseLong(teacherId), "cs1022",selectedSemester);
					if(isManager){
						param.put("role", 0);
					}else{
						param.put("role", 1);
					}
				param.put("schoolId", schoolId);
				param.put("teacherId", teacherId);
				
				List<JSONObject> data = venueManageAppService.getVenueApplyList(param);
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
		 * 新增场馆申请单（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 * @update 添加setNX控制多及并发控制2016.8.21 By：zhh
		 * @update 添加消息推送功能 2016.11.14 By:zhh
		 */
		@RequestMapping(value = "/addAppVenueApply", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addAppVenueApply(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			boolean isSendMsg = false;
			try {
				String schoolId = request.getString("schoolId");
				Long userId = request.getLong("userId");
				String selectedSemester = allCommonDataService.getCurTermInfoId(Long.parseLong(schoolId));
				User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
				String teacherId="";
				String teacherName = "";
				if(user!=null){
					teacherId = String.valueOf(user.getAccountPart().getId());
			        teacherName = user.getAccountPart().getName();
		        /*teacherId = "100984739";
		        teacherName = "1303";*/
		       /* teacherId = "100984779";
		        teacherName = "艾芳芳";*/
		       /* teacherId = "100985011";
		        teacherName = "陈佳";*/
				}
				
				String useDate = request.getString("useDate");
				String useStartDate = request.getString("useStartDate");
				String useEndDate =  request.getString("useEndDate");
				String useTime =  request.getString("useTime");
				String setId = request.getString("setId");
				String applyReason = request.getString("applyReason");
				String memberStructure =  request.getString("memberStructure");
				String comment = request.getString("comment");
				boolean isManager=authServiceImpl.getIsMoudleManagerByAccountId(Long.parseLong(schoolId), Long.parseLong(teacherId), "cs1022",selectedSemester);
				if(isManager){
					param.put("applyRole", 0);
				}else{
					param.put("applyRole", 1);
				}
			
				param.put("schoolId", schoolId);
				param.put("teacherId", teacherId);
				param.put("teacherName", teacherName);
				
				param.put("useStartDate", useDate+" "+useStartDate+":00");
				param.put("useEndDate", useDate+" "+useEndDate+":00");
				
				param.put("useTime",useTime);
				param.put("setId", setId);
				param.put("applyReason", applyReason);
				param.put("memberStructure", memberStructure);
				param.put("comment", comment);
				param.put("userId",userId);
				param.put("termInfo", selectedSemester);
				int i = venueManageAppService.addAppVenueApply(param);
				if ( i>0 ) {
					//
					JSONObject object = venueManageSetService.getVenueSet(param);
					String exam =  object.getString("isNeedExam");
					if ("1".equals(exam) ) {
						boolean isadmin = false;
						if (isManager) {
							isadmin = isManager;
						}else{
							long teacherId2;
							List<JSONObject> list = (List<JSONObject>)object.get("teachers");
							if (list!=null && list.size() > 0) {
								for (int j = 0; j < list.size(); j++) {
									JSONObject object2 = list.get(j);
									teacherId2 = object2.getLongValue("teacherId");
									if (teacherId2 == Long.parseLong(teacherId)) {
										isadmin = true;
										break;
									}
								}
							}
						}
						if (isadmin) {
							isSendMsg = true;
							param.put("examState", "1");
							venueManageSetService.addVenueApplyExam(param);
						}
					}
					//
					setPromptMessage(response, "0", "操作成功");
					
					param.put("applyReason", request.getString("applyReason"));
					venueManageAppService.sendWxTemplateMsg(param);
				}else if(i==-2){
					setPromptMessage(response, "-2", "同一时间段该场馆已有人申请！");
				}else if(i==-3){
					setPromptMessage(response, "-3", "管理员设置项目未设置！");
				}else if(i==-4){
					setPromptMessage(response, "-4", "请求超时，请稍后重试！");
				}else{
					setPromptMessage(response, "-1", "操作失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * 获取场馆申请单审核列表（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAppVenueApplyExamList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAppVenueApplyExamList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
		
			try {
				//获取当前时间和当时时间的前15天 createStartDate和createEndDate
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
				String createEndDate = sf.format(new Date())+" 23:59:59"; 
			    Calendar cal = Calendar.getInstance();
			    cal.add(Calendar.DATE, -15);  
				Date d1 = cal.getTime();
				String createStartDate = sf.format(d1)+" 00:00:00"; 
				param.put("createStartDate", createStartDate);
				param.put("createEndDate", createEndDate);
				long schoolId = request.getLong("schoolId");
				Long userId = request.getLong("userId");
				String selectedSemester = allCommonDataService.getCurTermInfoId(schoolId);
				User user = allCommonDataService.getUserById(schoolId, userId,selectedSemester);
				String teacherId="";
				String teacherName = "";
				if(user!=null){
					teacherId = String.valueOf(user.getAccountPart().getId());
			        teacherName = user.getAccountPart().getName();
		       /* teacherId = "100984739";
		        teacherName = "1303";*/
		        /*teacherId = "100984779";
		        teacherName = "艾芳芳";*/
		       /* teacherId = "100985011";
		        teacherName = "陈佳";*/
				}
				String role="";
				boolean isManager=authServiceImpl.getIsMoudleManagerByAccountId(schoolId, Long.parseLong(teacherId), "cs1022",selectedSemester);
				if(isManager){
					role="0";
				}else{
					role="1";
				}
				param.put("schoolId", schoolId);
				param.put("teacherId", teacherId);
				param.put("teacherName", teacherName);
				param.put("role", role);
				List<JSONObject> data=venueManageAppService.getAppVenueApplyExamList(param);
				if (data != null ) {
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				} else {
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return response;
		}
		
		/**
		 * 获取场馆申请单审核详情（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAppVenueApplyExamDetail", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAppVenueApplyExamDetail(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			logger.info("getAppVenueApplyExamDetailParams:"+request);
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				long schoolId = request.getLongValue("schoolId");
				String applyId = request.getString("applyId");
				
				// 判断用户信息
				long fontUserId = Long.parseLong(request.getString("userId"));
				// 获取AccountId
				User user = allCommonDataService.getUserById(schoolId, fontUserId);
				if(user == null) {
					setPromptMessage(response, "2", "当前身份没有这次数据");
					return response;
				}
				long teacherId = user.getAccountPart().getId();
				
				JSONObject params = new JSONObject();
				params.put("applyId", applyId);
				params.put("schoolId", schoolId);
				
				 // 获取审核列表
				List<JSONObject> ExamApplyList = venueManageSetService.getExamApplyList(params);
				logger.info("getExamApplyList:"+JSONObject.toJSONString(ExamApplyList));
				if(ExamApplyList == null || ExamApplyList.size() == 0) { // 未审核，只有管理员收到模板消息，
					// 获取审批人员
					params.put("teacherId", teacherId);
					List<JSONObject> venueManagerList = venueManageAppService.getVenueManagerList(params);
					logger.info("venueManagerList:"+JSONObject.toJSONString(venueManagerList));
					if(venueManagerList == null || venueManagerList.size() == 0) {
						setPromptMessage(response, "2", "当前身份没有这次数据");
						return response;
					}
				}else { // 审核通过，普通老师收到模板消息
					boolean isAdmin = false;	// 管理员
					boolean isAdminAndAproveler = false; // 管理员兼申请人
					int isApprovaled = request.getIntValue("isApprovaled");	// 1审批完成，0未审批
					
					List<Long> applyTeacherIds = new ArrayList<Long>();
					ExamApplyList.stream().forEach(obj -> {
						if(obj.getLongValue("examTeacherId") == teacherId) {
							applyTeacherIds.add(teacherId);
						}
					});
					
					if(applyTeacherIds.contains(teacherId)) {
						isAdmin = true;
					}
					
					if(isAdmin && request.getIntValue("isAdminAndAproveler") == 1) {
						isAdminAndAproveler = true;
					}
					
					// 获取申请使用场馆信息
					params.put("teacherId", teacherId);
					JSONObject venueDetail = venueManageAppService.getVenueAndAprovel(params);
					logger.info("ExamApplyList：{}", JSONObject.toJSONString(ExamApplyList));
					logger.info("venueDetail:{}", JSONObject.toJSONString(venueDetail));
					logger.info("pass:{}", isAdmin);
					logger.info("isAdminAndAproveler:{}", isAdminAndAproveler);
					logger.info("isApprovaled:{}", isApprovaled);
					if (isApprovaled == 1 && (venueDetail == null || venueDetail.isEmpty())) { // 普通老师，点了其他人的消息
						setPromptMessage(response, "2", "当前身份没有这次数据");
						return response;
					}else if(isAdminAndAproveler && (venueDetail == null || venueDetail.isEmpty())) { // 审批人&申请人，但没非该场馆的申请人
						setPromptMessage(response, "2", "当前身份没有这次数据");
						return response;
					}else if(!isAdmin && isApprovaled == 0) { // 普通老师，未审批的消息
						setPromptMessage(response, "2", "当前身份没有这次数据");
						return response;
					}
				}
				
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
				
				JSONObject venueSetData = venueManageAppService.getVenueAndAprovel(param);
				JSONObject data = venueManageAppService.getAppVenueApplyExamDetail(param);
				if (data != null && venueSetData != data) {
					// 1 需要 2不需要（否）
					int isNeedExam = venueSetData.getIntValue("isNeedExam");
					// 审核 1 同意 2不同意
					int examState = 0;
					if(StringUtils.isNotBlank(data.getString("examState"))) {
						examState = Integer.parseInt(data.getString("examState"));
					}
					
					// 1 同意 2不同意
					int applyState = data.getIntValue("applyState");
					if(1 == isNeedExam && examState == 1) {
						data.put("applyState", "3");
					}else if(1 == isNeedExam && examState == 2) { // 无需审核
						data.put("applyState", "4");
					}else if(1 == isNeedExam && applyState == 1) { // 待审核
						data.put("applyState", "2");
					}else if(2 == isNeedExam) { // 待审核
						data.put("applyState", "1");
					}
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				} else {
					setPromptMessage(response, "3", "当前数据已删除");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * 添加申请单审核（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/addAppVenueApplyExam", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addAppVenueApplyExam(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = request.getString("schoolId");
				String selectedSemester = allCommonDataService.getCurTermInfoId(Long.parseLong(schoolId));
				Long userId = request.getLong("userId");
				String applyId = request.getString("applyId");
				String examState = request.getString("examState");
				String disagreeReason = request.getString("disagreeReason");
				User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
				String teacherId="";
				String teacherName = "";
				if(user!=null){
					teacherId = String.valueOf(user.getAccountPart().getId());
			       teacherName = user.getAccountPart().getName();
		       /* teacherId = "100984739";
		        teacherName = "1303";*/
		       /* teacherId = "100984779";
		        teacherName = "艾芳芳";*/
		       /* teacherId = "100985011";
		        teacherName = "陈佳";*/
				}
				
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
				param.put("examState", examState);
				param.put("disagreeReason", disagreeReason);
				param.put("teacherId", teacherId);
				param.put("teacherName", teacherName);
				param.put("userId", userId);
				param.put("termInfo", selectedSemester);
				int i = venueManageSetService.addVenueApplyExam(param);
				if ( i>0 ) {
					setPromptMessage(response, "0", "操作成功");
					
					try {
						request.put("status", 0);
				    	venueManageAppService.sendWxTemplateMsg(request);
					} catch (Exception e) {
						logger.info("场馆申请失败!"+e.getMessage());
					}
					
				}else if(i==-2) {
					setPromptMessage(response, "-1", "时间冲突，该申请单的使用时间下已有人申请成功");
				}else{
					setPromptMessage(response, "-1", "操作失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * 场馆检查查看详情（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAppVenueApplyInspectionDetail", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAppVenueApplyInspectionDetail(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String schoolId = request.getString("schoolId");
				String applyId = request.getString("applyId");
				
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
				
				JSONObject data = venueManageAppService.getAppVenueApplyInspectionDetail(param);
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
		 * 添加申请单检查（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/addAppVenueApplyInspection", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addAppVenueApplyInspection(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = request.getString("schoolId");
				String selectedSemester = allCommonDataService.getCurTermInfoId(Long.parseLong(schoolId));
				Long userId = request.getLong("userId");
				String applyId = request.getString("applyId");
				List<JSONObject> inspections = (List<JSONObject>) request.get("inspections");
				User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
				String teacherId="";
				String teacherName = "";
				if(user!=null){
					teacherId = String.valueOf(user.getAccountPart().getId());
			        teacherName = user.getAccountPart().getName();
		       /* teacherId = "100984739";
		        teacherName = "1303";*/
		      /*  teacherId = "100984779";
		        teacherName = "艾芳芳";*/
		       /* teacherId = "100985011";
		        teacherName = "陈佳";*/
				}
				
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
				param.put("inspections", inspections);
				param.put("teacherId", teacherId);
				param.put("teacherName", teacherName);
				
				int i = venueManageSetService.addVenueApplyInspection(param);
				if ( i>0 ) {
					setPromptMessage(response, "0", "操作成功");
				}else{
					setPromptMessage(response, "-1", "操作失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * 获取当前登录系统角色（App端）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 * @return 
		 *  role:0 系统管理员 1 审核人员  2  一般老师
		 */
		@RequestMapping(value = "/getAppVenueRole", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAppVenueRole(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = request.getString("schoolId");
				String selectedSemester = allCommonDataService.getCurTermInfoId(Long.parseLong(schoolId));
				Long userId = request.getLong("userId");
				User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,selectedSemester);
				Long teacherId = user.getAccountPart().getId();
				boolean flag=authServiceImpl.getIsMoudleManagerByAccountId(Long.parseLong(schoolId), teacherId, "cs1022",selectedSemester);
				param.put("schoolId", schoolId);
				//param.put("teacherId",  "100984779"); //艾芳芳
				//param.put("teacherId",  "100985011"); //陈佳
				param.put("teacherId",  teacherId); 
				param.put("flag", flag);
				JSONObject data = venueManageCommonService.getVenueRole(param);
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
		 * ZHXY-334
		 * @param req
		 * @param request
		 * @param res
		 * @return
		 */
		@RequestMapping(value = "/getApplyList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getApplyList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			List<JSONObject> result = venueManageCommonService.getApplyList(request);
			response.put("data", result);
			setPromptMessage(response, "0", "查询成功");
			return response;
		}
}
