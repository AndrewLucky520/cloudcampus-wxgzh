package com.talkweb.venueManage.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.venueManage.service.VenueManageSetService;

/** 
 * 场馆使用-老师接口
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Controller
@RequestMapping(value = "/venueManage/teacher/")
public class VenueManageTeacherAction extends BaseAction {
	    @Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private VenueManageSetService venueManageSetService;
	
		private static final Logger logger = LoggerFactory.getLogger(VenueManageTeacherAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
		
		@RequestMapping(value = "/getVenueManageDetailPlus" ,method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueManageDetailPlus(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) throws Exception{
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			String userId = request.getString("userId");
			String schoolId = request.getString("schoolId");
			User user = allCommonDataService.getUserById(Long.parseLong(schoolId), Long.parseLong(userId));
			long accountId = user.getAccountPart().getId();
			param.put("accountId", accountId);
			param.put("schoolId", schoolId);
			List<JSONObject> data =venueManageSetService.getVenueApplyListPlus(param);
			
			JSONObject paramList = request;
			paramList.put("schoolId", schoolId);
			if(data.isEmpty() && data.size()==0){		// 用户不存在任何场馆使用记录，则显示审批人页面
				List<JSONObject> list = venueManageSetService.getVenueSetListPlus(paramList);
				if(list!=null && !list.isEmpty() && list.size()>0){
					response.put("code", 1);
					response.put("data", list);
					response.put("size", list.size());
					response.put("msg", "用户不存在任何场馆使用记录，此时显示审批人页面");
					return response;
				}else{
					response.put("code", -1);
					response.put("data", null);
					response.put("msg", "系统未查询到你的审批人信息，赶紧联系管理员设置请假组吧！");
					return response;
				}
			}else{
  
				// 用户有场馆使用信息：1、存在未完成设备场馆使用则显示所有未完成流程；2、只有已完成场馆使用流程则显示最近一条已完成流程。
				int flag = 0;								// 若flag值一直为0，则表示没有未完成的场馆使用流程
				List<JSONObject> reslist = new ArrayList<JSONObject>();
				for(JSONObject obj : data){					// status状态1 同意,2不同意
					if("1".equals(obj.getString("checkState"))){ // 没有审核并且需要审核 obj.getString("examState")==null && "1".equals(obj.getString("isNeedExam"))
						flag = 1;
						reslist.add(obj);
					}else continue;
				}
				if(flag == 1){			// 存在未完成场馆使用流程则显示所有未完成流程
					response.put("code", 2);
					response.put("data", reslist);
					response.put("size", reslist.size());
					response.put("msg", "存在未完成场馆使用流程,显示所有未完成流程");
					return response;
				}else if(flag == 0){	// 只有已完成场馆使用流程则显示最近一条已完成流程
					Collections.sort(data,new Comparator<JSONObject>() {
						@Override
						public int compare(JSONObject o1, JSONObject o2) {
							return o2.getDate("createDate").compareTo(o1.getDate("createDate"));
						}
					});
					List<JSONObject> list = new ArrayList<JSONObject>();
					JSONObject result = data.get(0);
					Date date = result.getDate("createDate");
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					result.put("createDate", format.format(date));
					list.add(data.get(0));
					response.put("code", 3);
					response.put("data", list);
					response.put("msg", "只有已完成场馆使用流程，显示最近一条已完成流程");
					return response;
				}
			}
			return response;
		}
		
		// 场馆使用审批细节
		@RequestMapping(value = "/getVenueManageDetail" ,method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getVenueManageDetail(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) throws Exception{
			JSONObject response = new JSONObject();
			JSONObject param = request;
			String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
			String schoolId = getXxdm(req);
			param.put("schoolId", schoolId);
			param.put("accountId", accountId);
			List<JSONObject> data =venueManageSetService.getVenueApplyListPlus(param);
			
			JSONObject paramList = request;
			paramList.put("schoolId", schoolId);
			if(data.isEmpty() && data.size()==0){		// 用户不存在任何场馆使用记录，则显示审批人页面
				List<JSONObject> list = venueManageSetService.getVenueSetListPlus(paramList);
				if(list!=null && !list.isEmpty() && list.size()>0){
					response.put("data", 1);
					response.put("data", list);
					response.put("size", list.size());
					response.put("msg", "用户不存在任何场馆使用记录，此时显示审批人页面");
					return response;
				}else{
					response.put("data", -1);
					response.put("data", null);
					response.put("msg", "系统未查询到你的审批人信息，赶紧联系管理员设置请假组吧！");
					return response;
				}
			}else{
				// 用户有场馆使用信息：1、存在未完成设备场馆使用则显示所有未完成流程；2、只有已完成场馆使用流程则显示最近一条已完成流程。
				int flag = 0;								// 若flag值一直为0，则表示没有未完成的场馆使用流程
				List<JSONObject> reslist = new ArrayList<JSONObject>();
				for(JSONObject obj : data){					// status状态1表示未完成，2表示已完成
					if(obj.getInteger("examState")==1 && obj.getDate("useEndDate").before(new Date())){
						flag = 1;
						reslist.add(obj);
					}else continue;
				}
				if(flag == 1){			// 存在未完成场馆使用流程则显示所有未完成流程
					response.put("data", 2);
					response.put("data", reslist);
					response.put("size", reslist.size());
					response.put("msg", "存在未完成场馆使用流程,显示所有未完成流程");
					return response;
				}else if(flag == 0){	// 只有已完成场馆使用流程则显示最近一条已完成流程
					Collections.sort(data,new Comparator<JSONObject>() {
						@Override
						public int compare(JSONObject o1, JSONObject o2) {
							return o2.getDate("createDate").compareTo(o1.getDate("createDate"));
						}
					});
					response.put("data", 3);
					response.put("data", data.get(0));
					response.put("msg", "只有已完成场馆使用流程，显示最近一条已完成流程");
					return response;
				}
			}
			return response;
		}
		
		/**
		 * 老师获取场馆申请单列表
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
				if (StringUtils.isEmpty(queryType)) {
					queryType = "0";
				}
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
}
