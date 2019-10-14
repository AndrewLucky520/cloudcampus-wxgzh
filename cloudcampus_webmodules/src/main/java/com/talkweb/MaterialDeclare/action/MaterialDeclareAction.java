package com.talkweb.MaterialDeclare.action;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.MaterialDeclare.service.MaterialDeclareService;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.jasperReport.util.MaterialDeclareExcelExportTool;


@RequestMapping("/materialDeclareManage")
@Controller
public class MaterialDeclareAction extends BaseAction {

	@Autowired
	private MaterialDeclareService materialDeclareService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
	@RequestMapping(value = "/getRole" )
	@ResponseBody
	public JSONObject getMaterialDeclareRole(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String schoolId = getXxdm(req) ;
		String termInfoId = getCurXnxq(req);
		param.put("schoolId", schoolId);
		String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));

		param.put("teacherId", accountId);
 		boolean flag = isMoudleManager(req, "cs1041");
		if(flag){
			response.put("isAdmin", 1);
		}else {
			response.put("isAdmin", 0);
		}
		JSONObject userobj =materialDeclareService.getAuditMaterialDeclare(param); 
		if(userobj != null){
			response.put("isAuditor", 1);
		}else {
			response.put("isAuditor", 0);
		}

		 Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
		 String userName = account.getName();
		 response.put("userName", userName);
		 List<User> userList = account.getUsers();
		 if (userList!=null) {
			 for (int i = 0; i < userList.size(); i++) {
				 User user = userList.get(i);
				if (user.getUserPart().getRole() ==T_Role.Teacher ) {
					 response.put("isTeacher", 1);
				}
			}
		}
		 
		 if (response.getInteger("isTeacher")==null) {
			 response.put("isTeacher", 0);
		 }
 
		
		
		setPromptMessage(response, "1", "查询成功");
		return response;
	}
	
	
	
	
	
	
	@RequestMapping(value = "/getMaterialDeclareList" )
	@ResponseBody
	public JSONObject getMaterialDeclareList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));
		param.put("teacherId", accountId);
		String schoolId = getXxdm(req) ;
		param.put("schoolId", schoolId);
		String termInfoId = getCurXnxq(req);
		String queryType = request.getString("queryType");
		String applyUser = request.getString("applyUser");
		int page = request.getIntValue("page");
		int pageSize = request.getIntValue("pageSize");
		page = page<=0 ? 1:page;
		int position = (page -1 ) * pageSize;
		param.put("position", position);
		param.put("pageSize", pageSize);
 
		String selectedSemester =getCurXnxq(req);
		School school = getSchool(req,selectedSemester);
		
		if (StringUtils.isNotBlank(applyUser)) {
			String[][] teacherIdNames = getTeacherIdNames(school,selectedSemester , applyUser);
			List<String> teacherList = new ArrayList<String>();
			if (teacherIdNames[0].length > 0) {
				for (int i = 0; i < teacherIdNames[0].length; i++) {
					teacherList.add(teacherIdNames[0][i]);
				}
			}else {
				response.put("data", new ArrayList<JSONObject>());
				setPromptMessage(response, "0", "查询成功");//
				return response;
			}
			param.put("teacherList", teacherList);
		}
 
		
        try {
        	List<JSONObject> list = null;
        	int rowCnt = 0;
        	if ("0".equals(queryType)) {//0 我申请的 1 待我审核 2 我已审核
        		rowCnt = materialDeclareService.getApplayMaterialDeclareCnt(param);
        		list = materialDeclareService.getApplayMaterialDeclareList(param);
        	}else if ("1".equals(queryType)) {
        		rowCnt = materialDeclareService.getAuditMaterialDeclareCnt(param);
        		list =   materialDeclareService.getAuditMaterialDeclareList(param);
			}else if ("2".equals(queryType)) {
				rowCnt = materialDeclareService.getAuditedMaterialDeclareCnt(param);
				list =   materialDeclareService.getAuditedMaterialDeclareList(param);
			}else if ("3".equals(queryType)) {
				rowCnt = materialDeclareService.getAdminMaterialDeclareCnt(param);
				list =   materialDeclareService.getAdminMaterialDeclareList(param);
			}
        	
        	
    		response.put("page", page);
    		response.put("pageSize", pageSize);
    		response.put("rowCnt", rowCnt);
    		if (pageSize > 0 ) {
    			response.put("pageCnt", rowCnt/pageSize + (rowCnt%pageSize == 0? 0 : 1 ) );
    		}
        	
        	
        	Set<Long> ids = new HashSet<Long>();
        	int status = 0;
        	  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            
        	for (int i = 0; i < list.size(); i++) {
        		JSONObject object = list.get(i);
        		ids.add(object.getLong("teacherId"));
        		status =  object.getIntValue("status");
        		if (status == 1) {
        			JSONArray jsonArray = object.getJSONArray("auditDetail");
        			 if (jsonArray !=null) {
						for (int j = 0; j < jsonArray.size(); j++) {
							JSONObject obj = jsonArray.getJSONObject(j);
							if (obj.getInteger("checkStatus")==1) {
								JSONArray members = obj.getJSONArray("members");
								if (members!=null) {
									for (int k = 0; k < members.size(); k++) {
										ids.add(members.getJSONObject(k).getLong("member"));
									}
								}
								break;
							}
						}
					 }
				}else if (status == 3) {
				 JSONArray jsonArray = object.getJSONArray("auditDetail");
       			 if (jsonArray !=null) {
						for (int j = 0; j < jsonArray.size(); j++) {
							JSONObject obj = jsonArray.getJSONObject(j);
							if (obj.getInteger("checkStatus")==3) {
								ids.add(obj.getLong("auditor"));
								break;
							}
						}
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
    		String teacherName = null;
    		
    		for (int i = 0; i < list.size(); i++) {
    			JSONObject object = list.get(i);
    			  try {
    				  object.put("applyDate",format.format(object.getDate("applyDate")));
				  } catch (Exception e) {
					  e.printStackTrace();
				  }
     			teacherName = id2Name.get(object.getLong("teacherId"));
     			teacherName = teacherName==null?"":teacherName;
     			object.put("teacherName", teacherName);
    			 status =  object.getIntValue("status");
    			 if (status==2) {
    				 object.put("statusDescribe", "同意");
				 }else if (status==3) {
					 JSONArray array = object.getJSONArray("auditDetail");
					 if (array !=null) {
							for (int j = 0; j < array.size(); j++) {
								JSONObject obj = array.getJSONObject(j);
								if (obj.getInteger("checkStatus")==3) {
									teacherName = id2Name.get(obj.getLong("auditor"));
									teacherName = teacherName==null?"":teacherName;
									 object.put("statusDescribe", teacherName + "不同意");
								}
							}
						 }
				}else if (status == 1) {
					 JSONArray jsonArray = object.getJSONArray("auditDetail");
					 teacherName = "";
        			 if (jsonArray !=null) {
        				 if (jsonArray.getJSONObject(0).getInteger("checkStatus")==1) {
        					 object.put("status", "0");
						 }
						for (int j = 0; j < jsonArray.size(); j++) {
							JSONObject obj = jsonArray.getJSONObject(j);
							if (obj.getInteger("checkStatus")==1) {
								JSONArray members = obj.getJSONArray("members");
								if (members!=null) {
									for (int k = 0; k < members.size(); k++) {
										if (id2Name.get(members.getJSONObject(k).getLong("member")) !=null) {
											teacherName =teacherName + id2Name.get(members.getJSONObject(k).getLong("member")) +",";
										}
									}
								}
								break;
							}
						}
					 }
        			 if (teacherName.length() > 0) {
        				 teacherName = teacherName.substring(0 , teacherName.length() -1 );
					}
        			 object.put("statusDescribe", teacherName + "待审核");
				}
    			 
    			
			}
    		
        	response.put("data", list);
        	setPromptMessage(response, "1", "查询成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
 
		return response;
	}
	
	
	@RequestMapping(value = "/insertMaterialDeclareDepartment" )
	@ResponseBody
	public JSONObject insertMaterialDeclareDepartment(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req) ;
		JSONObject param = request;
		param.put("schoolId", schoolId);
		try {
			int result = materialDeclareService.insertMaterialDeclareDepartment(param);
			if (result > 0) {
				setPromptMessage(response, "1", "新增部门成功");
			}else {
				setPromptMessage(response, "-1", "新增部门失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "新增部门失败"); 
			e.printStackTrace();
		}
		return response;
	}
	 

	@RequestMapping(value = "/deleteMaterialDeclareDepartment" )
	@ResponseBody
	public JSONObject deleteMaterialDeclareDepartment(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req) ;
		JSONObject param = request;
		param.put("schoolId", schoolId);
		try {
			int result = materialDeclareService.deleteMaterialDeclareDepartment(request);
			if (result > 0) {
				setPromptMessage(response, "1", "删除部门成功");
			}else {
				setPromptMessage(response, "-1", "删除部门失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "删除部门失败"); 
			e.printStackTrace();
		}
		return response;
	}
 
	@RequestMapping(value = "/getMaterialDeclareDepartments" )
	@ResponseBody
	public JSONObject getMaterialDeclareDepartments(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req) ;
		JSONObject param = request;
		param.put("schoolId", schoolId);
		try {
			List<JSONObject> list =materialDeclareService.getMaterialDeclareDepartment(request);
			response.put("data", list);
			setPromptMessage(response, "1", "查询成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
 
	@RequestMapping(value = "/getMaterialDeclareDetail" )
	@ResponseBody
	public JSONObject getMaterialDeclareDetail(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		System.out.println("getMaterialDeclareDetail:"+request);
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req) ;
		JSONObject param = request;
		param.put("schoolId", schoolId);
		String termInfoId = getCurXnxq(req);
		try {
			
			JSONObject object = materialDeclareService.getMaterialDeclareDetail(param);
			
			Set<Long> ids = new HashSet<Long>();
			ids.add(object.getLong("teacherId"));
			JSONArray jsonArray = object.getJSONArray("auditDetail");
			if (jsonArray!=null && jsonArray.size() > 0) {
				 for (int i = 0; i < jsonArray.size(); i++) {
					 JSONObject obj = jsonArray.getJSONObject(i);
					 if (obj.containsKey("auditor")) {
						 ids.add(obj.getLong("auditor"));
					 }
					JSONArray  members = obj.getJSONArray("members");
					if (members!=null ) {
						for (int j = 0; j < members.size(); j++) {
							ids.add(members.getJSONObject(j).getLong("member"));
						}
					}
					
					 
				}
				
			}
			
			JSONArray itemsSet = object.getJSONArray("items");
			if (itemsSet!=null && itemsSet.size() > 0) {
				for (int i = 0; i < itemsSet.size(); i++) {
					 JSONObject obj = itemsSet.getJSONObject(i);
					 BigDecimal aBigDecimal = new BigDecimal(obj.getString("cnt"));
					 BigDecimal bBigDecimal = new BigDecimal(obj.getString("unitPrice"));
					 BigDecimal cBigDecimal = aBigDecimal.multiply(bBigDecimal);
					 obj.put("sum", cBigDecimal.doubleValue());
				}
			}
			
         	List<Account> accList = commonDataService.getAccountBatch(Long.parseLong(schoolId), new ArrayList<Long>(ids), termInfoId);
    		Map<Long, String> id2Name = new HashMap<Long, String>();
    		if(CollectionUtils.isNotEmpty(accList)) {
    			for(Account acc : accList) {
    				id2Name.put(acc.getId(), acc.getName());
    			}
    		}
    		
    		
    		 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    		object.put("teacherName", id2Name.get(object.getLong("teacherId")));
			jsonArray = object.getJSONArray("auditDetail");
			if (jsonArray!=null && jsonArray.size() > 0) {
				 for (int i = 0; i < jsonArray.size(); i++) {
					 JSONObject obj = jsonArray.getJSONObject(i);
					 obj.getDate("processDate");
					 if (obj.getDate("processDate")!=null) {
						 obj.put("processDate", format.format(obj.getDate("processDate")));
					}
					
					 if (obj.containsKey("auditor")) {
						 obj.put("auditorName", id2Name.get(obj.getLong("auditor")));
					 }
					JSONArray  members = obj.getJSONArray("members");
					if (members!=null ) {
						for (int j = 0; j < members.size(); j++) {
							 JSONObject menberObj = members.getJSONObject(j);
							 menberObj.put("memberName",   id2Name.get(menberObj.getLong("member")));
						}
					}
				}
				
			}
           
             try {
				  object.put("applyDate",format.format(object.getDate("applyDate")));
			  } catch (Exception e) {
				  e.printStackTrace();
			  }
			response.put("data", object);
			setPromptMessage(response, "1", "查询成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/insertMaterialDeclare" )
	@ResponseBody
	public JSONObject insertMaterialDeclare(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req) ;
		JSONObject param = request;
		param.put("schoolId", schoolId);
		param.put("applyDate", new Date());
		String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));
		param.put("teacherId", accountId);
		try {
			int result = materialDeclareService.insertMaterialDeclare(request);
			if (result > 0) {
				setPromptMessage(response, "1", "新增物资申请成功");
			}else {
				setPromptMessage(response, "-1", "新增物资申请失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "新增物资申请失败");
			e.printStackTrace();
		}
	
		
	 
		return response;
	}
	
	@RequestMapping(value = "/deleteMaterialDeclare" )
	@ResponseBody
	public JSONObject deleteMaterialDeclare(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			int result = materialDeclareService.deleteMaterialDeclare(request);
			if (result > 0) {
				setPromptMessage(response, "1", "删除物资申请成功");
			}else {
				setPromptMessage(response, "-1", "删除物资申请失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "删除物资申请失败");
			e.printStackTrace();
		}
		
		return response;
	}
 
	@RequestMapping(value = "/insertMaterialDeclareAuditMenber" )
	@ResponseBody
	public JSONObject insertMaterialDeclareAuditMenber(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req) ;
		JSONObject param = request;
		param.put("schoolId", schoolId);

		JSONArray auditflows = param.getJSONArray("auditflows");
		List<JSONObject> list = new ArrayList<JSONObject>();
		if (auditflows !=null && auditflows.size() > 0) {
			 for (int i = 0; i < auditflows.size(); i++) {
				 JSONObject elem  = auditflows.getJSONObject(i);
				 JSONArray  auditorLevel = elem.getJSONArray("auditorLevel");
				 if (auditorLevel!= null && auditorLevel.size() > 0) {
					 for (int j = 0; j < auditorLevel.size(); j++) {
					    JSONObject level = auditorLevel.getJSONObject(j);
					    JSONArray auditors = level.getJSONArray("auditors");
					    if (auditors!=null && auditors.size() > 0) {
					    	   for (int k = 0; k < auditors.size(); k++) {
					    		   JSONObject obj  =auditors.getJSONObject(k);
					    		   JSONObject object = new JSONObject();
									object.put("schoolId", schoolId);
									object.put("moneyBegin", elem.getDouble("moneyBegin"));
									object.put("moneyEnd", elem.getDouble("moneyEnd"));
									object.put("levelNum", level.getInteger("levelNum"));
									object.put("teacherId",obj.getString("teacherId"));
									list.add(object);
								}
						}
					 
						
					}
				 }
				 
			}
		}
		
		if (list.size() == 0) {
			setPromptMessage(response, "-1", "新增物资审核人员失败");
			return response;
		}
		materialDeclareService.deleteMaterialDeclareAuditMenber(param);
		int result =  materialDeclareService.insertMaterialDeclareAuditMenber(list);
		if (result > 0) {
			setPromptMessage(response, "1", "新增物资审核人员成功");
		}else {
			setPromptMessage(response, "-1", "新增物资审核人员失败");
		}
		return response;
	} 
	
	 
		@RequestMapping(value = "/deleteMaterialDeclareAuditMenber" )
		@ResponseBody
		public JSONObject deleteMaterialDeclareAuditMenber(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
			JSONObject response = new JSONObject();
			String schoolId = getXxdm(req) ;
			request.put("schoolId", schoolId);
			try {
				int result = materialDeclareService.deleteMaterialDeclareAuditMenber(request);
				if (result > 0) {
					setPromptMessage(response, "1", "删除审核人成功");
				}else {
					setPromptMessage(response, "-1", "删除审核人失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "删除审核人失败");
				e.printStackTrace();
			}

			return response;
		} 
	 
		@RequestMapping(value = "/getMaterialDeclareAuditMenber" )
		@ResponseBody
		public JSONObject getMaterialDeclareAuditMenber(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
			JSONObject response = new JSONObject();
			String schoolId = getXxdm(req) ;
			JSONObject param = request;
			String termInfoId = getCurXnxq(req);
			
			param.put("schoolId", schoolId);
			try {
				List<JSONObject> list =  materialDeclareService.getMaterialDeclareAuditMenber(request);
				
				if (list!=null ) {
					Set<Long> ids = new HashSet<Long>();
					for (int i = 0; i < list.size(); i++) {
						JSONObject object = list.get(i);
						 JSONArray  auditorLevel = object.getJSONArray("auditorLevel");
						 if (auditorLevel!=null) {
							 for (int j = 0; j < auditorLevel.size(); j++) {
								JSONObject teachers = auditorLevel.getJSONObject(j);
								if (teachers!=null) {
									JSONArray auditors =  teachers.getJSONArray("auditors");
									if (auditors!=null) {
										for (int k = 0; k < auditors.size(); k++) {
											JSONObject obj = auditors.getJSONObject(k);
											ids.add(obj.getLong("teacherId"));
										}
									}
									
								}
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
		    		String teacherName = null;
					for (int i = 0; i < list.size(); i++) {
						JSONObject object = list.get(i);
						 JSONArray  auditorLevel = object.getJSONArray("auditorLevel");
						 if (auditorLevel!=null) {
							 for (int j = 0; j < auditorLevel.size(); j++) {
								 JSONObject teachers = auditorLevel.getJSONObject(j);
								if (teachers!=null) {
									
									JSONArray auditors =  teachers.getJSONArray("auditors");
									if (auditors!=null) {
										for (int k = 0; k < auditors.size(); k++) {
											JSONObject obj = auditors.getJSONObject(k);
											teacherName = id2Name.get(obj.getLong("teacherId"));
											if (StringUtils.isNotBlank(teacherName)) {
												obj.put("teacherName", teacherName);
											}else {
												obj.put("teacherName", "");
											}
										}
									}
									
								 
								}
							}
						}
						
					}
		    		
					
					
				}
				
				
				response.put("data", list);
				setPromptMessage(response, "1", "获取审核人成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "获取审核人失败");
				e.printStackTrace();
			}
			return response;
		} 
 
		@RequestMapping(value = "/updateMaterialDeclareProcedure" )
		@ResponseBody
		public JSONObject updateMaterialDeclareProcedure(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
			JSONObject response = new JSONObject();
			JSONObject param = request;
			String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));
			param.put("teacherId", accountId);
			try {
				int result =  materialDeclareService.updateMaterialDeclareProcedure(param);
				if (result > 0) {
					setPromptMessage(response, "1", "审核成功");
				}else {
					setPromptMessage(response, "1", "审核失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "审核失败");
				e.printStackTrace();
			}
			return response;
		} 
 
	
		@RequestMapping(value = "/getMaterialDeclareStatistics" )
		@ResponseBody
		public JSONObject getMaterialDeclareStatistics(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
			JSONObject response = new JSONObject();
			String schoolId = getXxdm(req) ;
			JSONObject param = request;
			param.put("schoolId", schoolId);
			try {
				List<JSONObject> list=  materialDeclareService.getMaterialDeclareStatistics(param);
				req.getSession().setAttribute("materialDeclareStatistics", list);
				response.put("data", list);
				setPromptMessage(response, "1", "获取统计数据成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "获取统计数据失败");
				e.printStackTrace();
			}
			return response;
		} 
		
		
 
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
				param.put("teacherName", request.getString("teacherName"));
				param.put("schoolId", getXxdm(req));
				List<JSONObject> data = materialDeclareService.getAllTeacherList(param);
				if (data != null) {
					response.put("data", data);
					setPromptMessage(response, "1", "查询成功");
				} else {
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		
		@RequestMapping(value = "/getHasSetMember", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getHasSetMember(HttpServletRequest req,
			 HttpServletResponse res) {
			JSONObject response = new JSONObject();
			try {
				JSONObject param = new JSONObject();
				param.put("schoolId", getXxdm(req));
				Integer cnt = materialDeclareService.getHasSetMember(param);
				if (cnt > 0) {
					response.put("hasSet", "Y");
				}else {
					response.put("hasSet", "N");
				}
				setPromptMessage(response, "1", "查询成功"); 
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		
		
		
		@RequestMapping(value = "/exportStatistics")
		@ResponseBody
		public JSONObject exportStatistics( HttpServletRequest req,
			 HttpServletResponse res) {
			List<JSONObject> data=  (List<JSONObject>)req.getSession().getAttribute("materialDeclareStatistics");
            if (data!=null) {
				try {
					String name = req.getParameter("name");
					if (StringUtils.isBlank(name)) {
						name = "物资申报统计";
					}
					req.setCharacterEncoding("UTF-8");
					List<JSONObject> exportList = materialDeclareService.getMaterialDeclareExportList(data);
			        MaterialDeclareExcelExportTool.exportMoreExcelWithData(exportList,name, req, res, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		
		private  String[][] getTeacherIdNames(School school,String selectedSemester ,String teacherName){
			List<Account> ll = commonDataService.getAllSchoolEmployees(school,selectedSemester, teacherName);
			String[][] teacherIdName = new String[2][ll.size()];
			for(int i = 0;i<ll.size();i++){
				String teacherId = String.valueOf(ll.get(i).getId());
				teacherIdName[0][i] = teacherId;
				teacherIdName[1][i] =ll.get(i).getName();
			}
			return teacherIdName;
		}
	
}
