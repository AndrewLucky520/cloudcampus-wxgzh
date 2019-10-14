package com.talkweb.notice.action;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.notice.service.NoticeService;
 

@RequestMapping("/noticeManage")
@Controller
public class NoticeAction extends BaseAction implements Serializable{
 
	private static final long serialVersionUID = -4629014240942695985L;

	@Autowired
	private NoticeService noticeService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
	
	@RequestMapping(value = "/getRole",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRole(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		boolean role = isMoudleManager(req, "cs1040");
		if(role){
			response.put("isAdmin", 1);
		}else {
			response.put("isAdmin", 0);
		} 
		 String schoolId = getXxdm(req);
		 String termInfoId = getCurXnxq(req);
		 String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		 Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
		 List<User> userList = account.getUsers();
		 if (userList!=null) {
			 for (int i = 0; i < userList.size(); i++) {
				 User user = userList.get(i);
				if (user.getUserPart().getRole() ==T_Role.Teacher ) {
					 response.put("isTeacher", 1);
				}
				if (user.getUserPart().getRole() ==T_Role.Student || user.getUserPart().getRole() ==T_Role.Parent) {
					 response.put("isParentStudent", 1);
				}
			}
		}
		 
		 if (response.getInteger("isTeacher")==null) {
			 response.put("isTeacher", 0);
		 }
		 if (response.getInteger("isParentStudent")==null) {
			 response.put("isParentStudent", 0);
		}
 
 
		setPromptMessage(response, "1", "");
		return response;
	}
	
	

	@RequestMapping(value = "/getNoticeList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getNoticeList(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		String termInfoId = getCurXnxq(req);
		request.put("schoolId", schoolId);
		request.put("accountId", accountId);
		
		
		Integer queryType = request.getInteger("queryType");
		if (queryType==1) {
			 request.put("queryType", "1");
			 Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
			 List<User> userList = account.getUsers();
			 if (userList!=null) {
				 for (int i = 0; i < userList.size(); i++) {
					 User user = userList.get(i);
					if (user.getUserPart().getRole() ==T_Role.Teacher ) {
						request.put("isTeacher", 1);
					}
					if (user.getUserPart().getRole() ==T_Role.Student || user.getUserPart().getRole() ==T_Role.Parent) {
						request.put("isParentStudent", 1);
					}
				}
			}
		}
		
		
		String startTime = request.getString("startTime");
		if (StringUtils.isNotBlank(startTime)) {
			startTime+=" 00:00:00";
		}
		request.put("startTime", startTime);
		String endTime = request.getString("endTime");
		if (StringUtils.isNotBlank(endTime)) {
			endTime+=" 23:59:59";
		}
		request.put("endTime", endTime);
		
		List<JSONObject> list = noticeService.getNoticeList(request);
 
		Set<Long> ids = new HashSet<Long>();
		if (list!=null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				JSONObject person =  list.get(i);
				ids.add(person.getLong("teacher"));
				JSONArray teachers = person.getJSONArray("teachers");
				if (teachers!=null && teachers.size() > 0) {
					for (int j = 0; j < teachers.size(); j++) {
						JSONObject object = teachers.getJSONObject(j);
						ids.add(object.getLong("teacherId"));
					}
				}
 			}
			List<Account> accList = commonDataService.getAccountBatch(Long.parseLong(schoolId), new ArrayList<Long>(ids), termInfoId);
			Map<Long, String> id2Name = new HashMap<Long, String>();
			if(CollectionUtils.isNotEmpty(accList)) {
				for(Account acc : accList) {
					id2Name.put(acc.getId(), acc.getName());
				}
			}
			
			Iterator<JSONObject> iterator = list.iterator();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			while (iterator.hasNext()) {
				 JSONObject object = iterator.next();
				 object.put("updateDate", format.format(object.getDate("updateDate")));
				 object.put("createDate", format.format(object.getDate("createDate")));
				 if (StringUtils.isNotBlank(id2Name.get(object.getLong("teacher")))) {
					 object.put("teacherName", id2Name.get(object.getLong("teacher")));
				 }else{
					 object.put("teacherName", "");
				 }
				 JSONArray teachers = object.getJSONArray("teachers");
 
				 for (int i = teachers.size() -1; i > -1 ; i --) {
					   JSONObject obj = teachers.getJSONObject(i);
					   if (StringUtils.isNotBlank(id2Name.get(object.getLong("teacherId")))) {
						   object.put("teacherName", id2Name.get(object.getLong("teacherId")));
					   }else {
						   teachers.remove(i);
					   }
				 }
 
			}

		}
 
		response.put("data", list);
	 
		setPromptMessage(response, "1", "查询成功");
		return response;
	}
	
	

	
	@RequestMapping(value = "/addNotice",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addNotice(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		String schoolId = getXxdm(req);
		request.put("teacherId", accountId);
		request.put("schoolId", schoolId);
		
		request.put("updateDate", new Date());
		String noticeId = request.getString("noticeId");
		JSONArray jsonArray = request.getJSONArray("teachers");
		List<String> list = null;
		if (jsonArray!=null && jsonArray.size() > 0) {
			list = new ArrayList<String>();
			for (int i = 0; i < jsonArray.size(); i++) {
				list.add(jsonArray.getString(i));
			}
		}
		String body = request.getString("body");
		if (StringUtils.isBlank(body)) {
			body= "";
		}else {
			body = StringEscapeUtils.unescapeHtml3(body);
		}
		request.put("body", body);
		if (StringUtils.isEmpty(noticeId)) {
			noticeId =  UUIDUtil.getUUID();
			request.put("noticeId", noticeId);
			request.put("createDate", new Date());
			request.put("clickCnt", 0);
			noticeService.insertNoticeRecord(request);
			if (list!=null && list.size() > 0) {
				List<JSONObject> list2 = new ArrayList<JSONObject>();
				for (int i = 0; i < list.size(); i++) {
					JSONObject object = new JSONObject();
					object.put("schoolId", schoolId);
					object.put("noticeId", noticeId);
					object.put("teacherId", list.get(i));
					list2.add(object);
				}
				noticeService.insertNoticePersonnel(list2);
			}
			
			setPromptMessage(response, "1", "新增通知成功");
		}else {
			noticeService.updateNotice(request);
			noticeService.delNoticePersonnel(request);
			if (list!=null && list.size() > 0) {
				List<JSONObject> list2 = new ArrayList<JSONObject>();
				for (int i = 0; i < list.size(); i++) {
					JSONObject object = new JSONObject();
					object.put("schoolId", schoolId);
					object.put("noticeId", noticeId);
					object.put("teacherId", list.get(i));
					list2.add(object);
				}
				noticeService.insertNoticePersonnel(list2);
			}
			setPromptMessage(response, "1", "更新通知成功");
		}
 
		return response;
	}
	
	
	@RequestMapping(value = "/getNoticeDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getNoticeDetail(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		String view = request.getString("view");
		String hasView = request.getString("hasView");
		request.put("teacherId", accountId);
		request.put("schoolId", schoolId);
		
		JSONObject object = noticeService.getNotice(request);
		String scope = object.getString("scope");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		object.put("createDate", format.format(object.getDate("createDate")));
		
		if ("2".equals(scope)) {
			List<JSONObject> list = noticeService.getNoticePersonnelList(request);
			Set<Long> ids = new HashSet<Long>();
			
			if (list!=null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					JSONObject person =  list.get(i);
					ids.add(person.getLong("teacherId"));
				}
			}
			
			List<Account> accList = commonDataService.getAccountBatch(Long.parseLong(schoolId), new ArrayList<Long>(ids), termInfoId);
			Map<Long, String> id2Name = new HashMap<Long, String>();
			if(CollectionUtils.isNotEmpty(accList)) {
				for(Account acc : accList) {
					id2Name.put(acc.getId(), acc.getName());
				}
			}
			
			if (list!=null && list.size() > 0) {
			
				Iterator<JSONObject> iterator = list.iterator();
				while (iterator.hasNext()) {
					JSONObject obj = iterator.next();
					if (StringUtils.isNotBlank(id2Name.get(obj.getLong("teacherId")))) {
						obj.put("teacherName", id2Name.get(obj.getLong("teacherId")));
						 
					}else {
						iterator.remove();
					}
				}

			}
			
			
			object.put("teachers", list);
		}
		response.put("data", object);
		setPromptMessage(response, "1", "查询成功");
		if (StringUtils.isNotBlank(view)) {
			noticeService.updateClickCnt(request);
		}
		if ("N".equals(hasView)) {
			noticeService.insertNoticeViewed(request);
		}
		
		
		return response;
	}
	
	@RequestMapping(value = "/delNotice",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delNotice(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			noticeService.delNotice(request);
			noticeService.delNoticePersonnel(request);
			noticeService.delNoticeViewed(request);
			setPromptMessage(response, "1", "删除成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "删除失败");
		}
		
		
		return response;
	}
	
}
