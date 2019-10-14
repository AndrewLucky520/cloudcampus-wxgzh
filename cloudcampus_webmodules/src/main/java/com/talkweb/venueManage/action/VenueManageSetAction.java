package com.talkweb.venueManage.action;

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
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.venueManage.service.VenueManageAppService;
import com.talkweb.venueManage.service.VenueManageSetService;

/** 
 * 场馆使用-相关设置
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Controller
@RequestMapping(value = "/venueManage/")
public class VenueManageSetAction extends BaseAction{
	    @Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private VenueManageSetService venueManageSetService;
	    @Autowired
	    private VenueManageAppService venueManageAppService;
		
		private static final Logger logger = LoggerFactory.getLogger(VenueManageSetAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
		/**
		 * 获取所有场馆
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getVenueSetList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueSetList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String venueName = request.getString("venueName");
				String venueTypeId = request.getString("venueTypeId");
				String useStartTime = request.getString("useStartTime");
				String useEndTime = request.getString("useEndTime");
				param.put("schoolId", schoolId);
				param.put("venueName", venueName);
				param.put("venueTypeId", venueTypeId);
				param.put("useStartTime", useStartTime);
				param.put("useEndTime", useEndTime);
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
		 * 获取所有场馆（供申请单编辑时用）
		 * 剔除了applyId的占用时间，不变灰。
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getVenueSetListForEdit", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueSetListForEdit(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String applyId = request.getString("applyId");
				String venueTypeId = request.getString("venueTypeId");
				String useStartTime = request.getString("useStartTime");
				String useEndTime = request.getString("useEndTime");
				param.put("schoolId", schoolId);
				param.put("applyIdForEdit", applyId);
				param.put("venueTypeId", venueTypeId);
				param.put("useStartTime", useStartTime);
				param.put("useEndTime", useEndTime);
				List<JSONObject> data = venueManageSetService.getVenueSetListForEdit(param);
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
		 * 新增/编辑场馆
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/addVenueSet", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addVenueSet(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String setId = request.getString("setId");
				String venueType = request.getString("venueType");
				String venueName = request.getString("venueName");
				String venueAddr = request.getString("venueAddr");
				String venueNum = request.getString("venueNum");
				String comment = request.getString("comment");
				List<JSONObject> teachers = (List<JSONObject>) request.get("teachers");
				List<JSONObject> equipmentTeachers = (List<JSONObject>) request.get("equipmentTeachers");
				String isNeedExam = request.getString("isNeedExam");
			
				param.put("schoolId", schoolId);
				param.put("setId", setId);
				param.put("venueType", venueType);
				param.put("venueName", venueName);
				param.put("venueAddr", venueAddr);
				param.put("venueNum", venueNum);
				param.put("comment", comment);
				param.put("teachers", teachers);
				param.put("equipmentTeachers", equipmentTeachers);
				param.put("isNeedExam", isNeedExam);
				
				int i = venueManageSetService.addVenueSet(param);
				if ( i>0 ) {
					setPromptMessage(response, "0", "操作成功");
				} else if( i==-2 ){
					setPromptMessage(response, "-2", "同类别下场馆名称不能相同！");
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
		 * 查询场馆详情
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getVenueSet", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueSet(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String setId = request.getString("setId");
			
				param.put("schoolId", schoolId);
				param.put("setId", setId);
				
				JSONObject data = venueManageSetService.getVenueSet(param);
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
		 * 删除场馆
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/deleteVenueSet", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject deleteVenueSet(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String setId = request.getString("setId");
				String venueTypeId = request.getString("venueTypeId");
			
				param.put("schoolId", schoolId);
				param.put("setId", setId);
				param.put("venueTypeId", venueTypeId);
				
				int i = venueManageSetService.deleteVenueSet(param);
				if ( i>0 ) {
					setPromptMessage(response, "0", "删除成功");
				} else{
					setPromptMessage(response, "-1", "删除失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "删除失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * 添加/编辑检查项目设置
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/addInspectionItemSet", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addInspectionItemSet(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId =  getXxdm(req);
				String content = request.getString("content");
				String commentId = request.getString("commentId");
				List<JSONObject> inspectionItems = (List<JSONObject>) request.get("inspectionItems");
			
				param.put("schoolId", schoolId);
				param.put("content", content);
				param.put("commentId", commentId);
				param.put("inspectionItems", inspectionItems);
				
				int i = venueManageSetService.addInspectionItemSet(param);
				if ( i>0 ) {
					setPromptMessage(response, "0", "操作成功");
				} else if( i==-2 ){
					setPromptMessage(response, "-2", "检查项目名称不能相同！");
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
		 * 删除检查项目
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/deleteInspectionItemSet", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject deleteInspectionItemSet(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId =  getXxdm(req);
				String inspectionItemId = request.getString("inspectionItemId");
			
				param.put("schoolId", schoolId);
				param.put("inspectionItemId", inspectionItemId);
				
				int i = venueManageSetService.deleteInspectionItemSet(param);
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
		 * 获取检查项目设置详情
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getInspectionItemSet", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getInspectionItemSet(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId =  getXxdm(req);
			
				param.put("schoolId", schoolId);
				
				JSONObject data = venueManageSetService.getInspectionItemSet(param);
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
		 * 新增/编辑场馆申请单
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 * @update 添加setNX控制多及并发控制2016.8.21 By：zhh
		 * @update 添加消息推动功能 2016.11.14 By:zhh
		 */
		@RequestMapping(value = "/addVenueApply", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addVenueApply(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId =  getXxdm(req);
				User u = (User) req.getSession().getAttribute("user");
				Long userId = u.getUserPart().getId();
				long teacherId = u.getAccountPart().getId();
				String teacherName = u.getAccountPart().getName();
				String applyId = request.getString("applyId");
				String useStartDate = request.getString("useStartDate");
				String useEndDate =  request.getString("useEndDate");
				String useTime =  request.getString("useTime");
				String setId = request.getString("setId");
				String applyReason = request.getString("applyReason");
				String memberStructure =  request.getString("memberStructure");
				String phone = request.getString("phone");
				boolean isRole=isMoudleManager(req, "cs1022");
				if(isRole){
					param.put("applyRole", 0);
				}else{
					param.put("applyRole", 1);
				}
				List<JSONObject> equipmentRequires = (List<JSONObject>) request.get("equipmentRequires");
			
				param.put("schoolId", schoolId);
				param.put("teacherId", teacherId);
				param.put("teacherName", teacherName);
				param.put("applyId", applyId);
				param.put("userId", userId);
				param.put("useStartDate", useStartDate);
				param.put("useEndDate", useEndDate);
				param.put("useTime",useTime);
				param.put("setId", setId);
				param.put("applyReason", applyReason);
				param.put("memberStructure", memberStructure);
				param.put("phone", phone);
				param.put("equipmentRequires", equipmentRequires);
				param.put("termInfoId", getCurXnxq(req));
				
				int i = venueManageSetService.addVenueApply(param);
				if ( i>0 ) {
					JSONObject object = venueManageSetService.getVenueSet(param);
					String exam =  object.getString("isNeedExam");
					if ("1".equals(exam) ) {
						boolean isadmin = false;
						if (isRole) {
							isadmin = isRole;
						}else{
							long teacherId2;
							List<JSONObject> list = (List<JSONObject>)object.get("teachers");
							if (list!=null && list.size() > 0) {
								for (int j = 0; j < list.size(); j++) {
									JSONObject object2 = list.get(j);
									teacherId2 = object2.getLongValue("teacherId");
									if (teacherId2 == teacherId) {
										isadmin = true;
										break;
									}
								}
							}
						}
						if (isadmin) {
							param.put("examState", "1");
							venueManageSetService.addVenueApplyExam(param);
						}
						
					}
					
					setPromptMessage(response, "0", "操作成功");
					
					JSONObject params = new JSONObject();
					params.put("applyId", param.getString("applyId"));
					params.put("schoolId", param.getLong("schoolId"));
					params.put("teacherId", param.getLong("teacherId"));
					params.put("applyReason", applyReason);
					params.put("termInfo", getCurXnxq(req));
					
					venueManageAppService.sendWxTemplateMsg(params);
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
		 * 删除场馆申请单
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/deleteVenueApply", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject deleteVenueApply(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
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
		 * 获取场馆申请单详情
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getVenueApplyDetail", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueApplyDetail(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId =  getXxdm(req);
				String applyId = request.getString("applyId");
			
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
		
				
				JSONObject data = venueManageSetService.getVenueApplyDetail(param);
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
		 * 获取所有场馆申请单列表
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getVenueApplyList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueApplyList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String termInfoId=getCurXnxq(req);
				String role = request.getString("role");
				
				String createStartDate = request.getString("createStartDate");
				if (StringUtils.isNotBlank(createStartDate)) {
					createStartDate+=" 00:00:00";
				}
				
				String createEndDate = request.getString("createEndDate");
				if (StringUtils.isNotBlank(createEndDate)) {
					createEndDate+=" 23:59:59";
				}
				
				
				String useStartDate = request.getString("useStartDate");
				if (StringUtils.isNotBlank(useStartDate)) {
					useStartDate+=" 00:00:00";
				}
				
				String useEndDate = request.getString("useEndDate");
				if (StringUtils.isNotBlank(useEndDate)) {
					useEndDate+=" 23:59:59";
				}
				
				String venueTypeId = request.getString("venueTypeId");
				String venueName = request.getString("venueName");
				String queryType = request.getString("queryType");
				String status  = request.getString("status");
				User u = (User) req.getSession().getAttribute("user");
				long teacherId = u.getAccountPart().getId();
				param.put("schoolId", schoolId);
				param.put("termInfoId", termInfoId);
				param.put("role", role);
				param.put("useStartDate", useStartDate);
				param.put("useEndDate", useEndDate);
				param.put("createStartDate", createStartDate);
				param.put("createEndDate", createEndDate);
				param.put("venueTypeId", venueTypeId);
				param.put("venueName", venueName);
				param.put("teacherId", teacherId);
				param.put("queryType", queryType);
				param.put("status", status);
 
				List<JSONObject> data = venueManageSetService.getVenueApplyList(param);
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
		 * 获取场馆申请单审核详情
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getVenueApplyExamDetail", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueApplyExamDetail(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			String termInfoId = getCurXnxq(req);
			param.put("termInfoId",termInfoId);
			logger.info("getVenueApplyExamDetail1：",request );
			System.out.println("getVenueApplyExamDetail2: "+request);
			
			try {
				String schoolId = getXxdm(req);
				String applyId = request.getString("applyId");
			
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
				
				JSONObject data = venueManageSetService.getVenueApplyExamDetail(param);
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
		 * 添加申请单审核
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/addVenueApplyExam", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addVenueApplyExam(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String applyId = request.getString("applyId");
				String examState = request.getString("examState");
				String disagreeReason = request.getString("disagreeReason");
				User u = (User) req.getSession().getAttribute("user");
				Long userId = u.getUserPart().getId();
				long teacherId =u.getAccountPart().getId();
				String teacherName = u.getAccountPart().getName();
				
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
				param.put("examState", examState);
				param.put("disagreeReason", disagreeReason);
				param.put("teacherId", teacherId);
				param.put("teacherName", teacherName);
				param.put("userId", userId);
				param.put("termInfo", getCurXnxq(req));
				
				int i = venueManageSetService.addVenueApplyExam(param);
				if ( i>0 ) {
					setPromptMessage(response, "0", "操作成功");
					venueManageAppService.sendWxTemplateMsg(param);
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
		 * 添加申请单检查
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/addVenueApplyInspection", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addVenueApplyInspection(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String applyId = request.getString("applyId");
				List<JSONObject> inspections = (List<JSONObject>) request.get("inspections");
				User u = (User) req.getSession().getAttribute("user");
				long teacherId =u.getAccountPart().getId();
				String teacherName = u.getAccountPart().getName();
				
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
		 * 获取申请单检查详情
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getVenueApplyInspectionDetail", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueApplyInspectionDetail(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String applyId = request.getString("applyId");
			
				param.put("schoolId", schoolId);
				param.put("applyId", applyId);
				
				JSONObject data = venueManageSetService.getVenueApplyInspectionDetail(param);
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
		 * 获取统计列表
		 * 包括了 普通老师、审核人员、系统管理员的统计 
		 * 需传入登录者角色
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getVenueStaticList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueStaticList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);

				String useStartDate = request.getString("useStartDate");
				if (StringUtils.isNotBlank(useStartDate)) {
					useStartDate+=" 00:00:00";
				}
				
				String useEndDate = request.getString("useEndDate");
				if (StringUtils.isNotBlank(useEndDate)) {
					useEndDate+=" 23:59:59";
				}
				
				String role = request.getString("role");
				User u = (User) req.getSession().getAttribute("user");
				long teacherId = u.getAccountPart().getId();
				
				param.put("teacherId", teacherId);
				param.put("schoolId", schoolId);
				param.put("useStartDate", useStartDate);
				param.put("useEndDate", useEndDate);
				param.put("role", role);
				
			    response = venueManageSetService.getVenueStaticList(param);
				if (response != null) {
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
		
		
		//lime-20170502
		/*
		       获取设备请求列表
		 **/
		@RequestMapping(value = "/getRequipmentrequireList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getRequipmentrequireList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {

			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String schoolId = getXxdm(req);
				String termInfoId=getCurXnxq(req);
				String role = request.getString("role");
				String useStartDate = request.getString("useStartDate");
				useStartDate+=" 00:00:00";
				String useEndDate = request.getString("useEndDate");
				useEndDate+=" 23:59:59";
				
				String venueName =  request.getString("venueName");
				String venueTypeId =  request.getString("venueTypeId");
			 
				User u = (User) req.getSession().getAttribute("user");
				long teacherId = u.getAccountPart().getId();
				String prepared = request.getString("prepared");
				if(StringUtils.isEmpty(prepared)){
					prepared = "1";
				}
			
				param.put("schoolId", schoolId);
				param.put("termInfoId", termInfoId);
				param.put("role", role);
				param.put("useStartDate", useStartDate);
				param.put("useEndDate", useEndDate);
				param.put("teacherId", teacherId);
				param.put("prepared", prepared);
				
				param.put("venueName", venueName);
				param.put("venueTypeId", venueTypeId);
			 
				
				List<JSONObject> data = venueManageSetService.getPrePareEquipMentList(param);
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
		
	 
		@RequestMapping(value = "/updateVenueApplyEquipmentExam", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject updateVenueApplyEquipmentExam(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String applyId = request.getString("applyId");
				param.put("applyId", applyId);
				String schoolId = getXxdm(req);
				param.put("schoolId", schoolId);
				List<JSONObject> equipmentRequires = (List<JSONObject>) request.get("equipmentRequires");
				String prepared = "2";
				String requireId = null;
				String teacherId = null;
				User u = (User) req.getSession().getAttribute("user");
				
				for (int i = 0; i < equipmentRequires.size(); i++) {
					JSONObject object = equipmentRequires.get(i);
					requireId = object.getString("requireId");
					param.put("requireId", requireId);
					int isSelect = object.getInteger("isSelect");
					param.put("prepared", isSelect);
					if (0 == isSelect) {
						prepared = "1";
						teacherId = "";
					}else{
						teacherId =u.getAccountPart().getId()+"";
						param.put("teacherId", teacherId);
						List<JSONObject> list = venueManageSetService.getEquipmentRequireContentList(param);
						if (list!=null && list.size() > 0) {
							JSONObject object2 = list.get(0);
							if ("0".equals(object2.getString("prepared"))) {
								venueManageSetService.updateVenueApplyEquipmentExam(param);
							}
							
						}
						
					}
					
				}
				param.put("equipmentStatus", prepared);
				venueManageSetService.updateApplyEquipmentStatus(param);
				setPromptMessage(response, "0", "更新成功");
				
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;

		}
 
}
