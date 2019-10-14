package com.talkweb.venueManage.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.venueManage.service.VenueManageCommonService;

/** 
 * 场馆使用-公共接口
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Controller
@RequestMapping(value = "/venueManage/common/")
public class VenueManageCommonAction extends BaseAction{
	    @Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private VenueManageCommonService venueManageCommonService;
		
		private static final Logger logger = LoggerFactory.getLogger(VenueManageCommonAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
		/**
		 * 获取所有教师列表
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAllTeacherList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAllTeacherList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String teacherName = request.getString("teacherName");
				School school = getSchool(req,null);
				String termInfoId=getCurXnxq(req);
				param.put("school", school);
				param.put("termInfoId", termInfoId);
				param.put("teacherName", teacherName);
				List<JSONObject> data = venueManageCommonService.getAllTeacherList(param);
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
		 * 获取所有场馆类别列表
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAllVenueTypeList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAllVenueTypeList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
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
		 * 获取所有设备要求列表
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getAllEquipmentRequireList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getAllEquipmentRequireList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				param.put("schoolId", schoolId);
				List<JSONObject> data = venueManageCommonService.updateAllEquipmentRequireList(param);
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
		 * 获取当前登录系统角色
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 * @return 
		 *  role:0 系统管理员 1 审核人员  2  一般老师
		 *	haveData（管理员是否填写过场馆设置信息）:0 无数据 1有数据
		 *  isEquipmentAdmin 0 不是   1 是
		 */
		@RequestMapping(value = "/getVenueRole", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueRole(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				String schoolId = getXxdm(req);
				String teacherId = String.valueOf((long)req.getSession().getAttribute("accountId"));
				boolean flag = isMoudleManager(req, "cs1022");
				param.put("schoolId", schoolId);
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
		 * 获取当前登录人信息
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 * @return 
		 */
		@RequestMapping(value = "/getCurUser", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getCurUser(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();

			try {
				User u = (User) req.getSession().getAttribute("user");
				long accountId = u.getAccountPart().getId();
				String teacherName = u.getAccountPart().getName();
				String phone=u.getAccountPart().getMobilePhone();
				if(phone==null){
					param.put("phone", "");
				}else{
					param.put("phone", phone);
				}
				param.put("teacherName", teacherName);
				param.put("accountId", accountId);
				response.put("data", param);
				setPromptMessage(response, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
}
