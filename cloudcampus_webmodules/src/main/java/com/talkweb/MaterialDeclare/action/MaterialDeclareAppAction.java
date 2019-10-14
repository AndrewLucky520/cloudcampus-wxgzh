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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.MaterialDeclare.service.MaterialDeclareService;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.PermissionTemplateIns;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserPermissions;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.utils.Result;

/** 
* @author  Administrator
* @version 创建时间：2019年1月10日 上午10:11:42 
* 
*/

@RequestMapping("/materialDeclareManage/app")
@Controller
public class MaterialDeclareAppAction {
	
	private static final String ERROR_MSG = "操作异常，请联系管理员！";
	private static final Logger logger = LoggerFactory.getLogger(MaterialDeclareAppAction.class);

	@Autowired
	private MaterialDeclareService materialDeclareService;
	@Autowired
	private AllCommonDataService commonDataService;
	
	
 
	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
	@RequestMapping(value = "/test" )
	@ResponseBody
	public JSONObject test() {
		JSONObject res = new JSONObject() ;
		res.put("code", 1);
		return res;
	}
	
	@RequestMapping(value = "/getRole" )
	@ResponseBody
	public JSONObject getMaterialDeclareRole(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		Long schoolId = request.getLong("schoolId");
		Long teacherId = request.getLong("teacherId");// 前端传来的是userId
	 
		if (schoolId == null || schoolId == 0 || teacherId == null || teacherId == 0) {
			setPromptMessage(response, "-1", "参数异常!");
			return response;
		}
		User user = commonDataService.getUserById(schoolId, teacherId);
		param.put("teacherId",  user.getAccountPart().getId());
		param.put("schoolId", schoolId); 
		response.put("isAdmin", 0);
		UserPermissions upers = commonDataService.getUserPermissionById(schoolId, teacherId, null);
		List<PermissionTemplateIns> ps = upers
				.getPermissionTemplateInss();
		String perLevel = "";
		boolean permission = false;
		for (PermissionTemplateIns p : ps) {
			if (p == null || p.getLevelv2() == 0) {
				continue;
			}
			perLevel = "cs"+p.getLevelv2();
			if(perLevel.equals("cs1041")){
				permission = true;
				break;
			}
	    }
 
		if(permission){
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
		setPromptMessage(response, "1", "查询成功");
		return response;
	}
	
	
	@RequestMapping(value = "/getMaterialDeclareDepartments" )
	@ResponseBody
	public JSONObject getMaterialDeclareDepartments(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		try {
			List<JSONObject> list =materialDeclareService.getMaterialDeclareDepartment(request);
			response.put("data", list);
			setPromptMessage(response, "1", "查询成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", ERROR_MSG);
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return response;
	}
	
	
	@RequestMapping(value = "/insertMaterialDeclareDepartment" )
	@ResponseBody
	public JSONObject insertMaterialDeclareDepartment(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
 
		try {
			int result = materialDeclareService.insertMaterialDeclareDepartment(request);
			if (result > 0) {
				setPromptMessage(response, "1", "新增部门成功");
			}else {
				setPromptMessage(response, "-1", "新增部门失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", ERROR_MSG); 
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	
	@RequestMapping(value = "/getMaterialDeclareList" )
	@ResponseBody
	public JSONObject getMaterialDeclareList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		String queryType = request.getString("queryType");
		int page = request.getIntValue("page");
		int pageSize = request.getIntValue("pageSize");
		page = page<=0 ? 1:page;
		int position = (page -1 ) * pageSize;
		param.put("position", position);
		param.put("pageSize", pageSize);   
        Long schoolId = request.getLong("schoolId");
		String  termInfoId = commonDataService.getCurTermInfoId(schoolId);
		Long teacherId = request.getLong("teacherId");
		User user = commonDataService.getUserById(schoolId, teacherId);
		request.put("teacherId", user.getAccountPart().getId());
		
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
    			response.put("pageCnt", rowCnt/pageSize + (rowCnt%pageSize == 0? 0 : 1));
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
         	List<Account> accList = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(ids), termInfoId);
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
			setPromptMessage(response, "-1", ERROR_MSG);
			e.printStackTrace();
			logger.error(e.getMessage());
		}
 
		return response;
	}
	
	
	@RequestMapping(value = "/getMaterialDeclareDetail" )
	@ResponseBody
	public JSONObject getMaterialDeclareDetail(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		System.out.println("getMaterialDeclareDetail:"+request);
		JSONObject response = new JSONObject();
		Long schoolId = request.getLong("schoolId");
		JSONObject param = request;
		param.put("schoolId", schoolId);
		String termInfoId = commonDataService.getCurTermInfoId(schoolId);
		try {
			JSONObject object = materialDeclareService.getMaterialDeclareDetail(param);
			if(object == null || object.isEmpty()) {
				setPromptMessage(response, "3", "当前数据已删除");
				return response;
			}
			
			String currentTeacherId = request.getString("userId");
			String applyTeacherId = object.getLong("teacherId").toString();
			// 判断当前用户是否为申请人
			
			logger.info("currentTeacherId："+currentTeacherId);
			logger.info("applyTeacherId："+applyTeacherId);
			logger.info("request："+request);
			
			// userId -> accountId
			User user = commonDataService.getUserById(schoolId, Long.parseLong(currentTeacherId));
			request.put("teacherId", user.getAccountPart().getId()+"");
			JSONObject currentTeacherMaterialDetial = materialDeclareService.getMaterialDeclareDetail(request);
			logger.info("currentTeacherMaterialDetial："+currentTeacherMaterialDetial);
			// 缺少 当前用户不在审核员列表内
			if(currentTeacherMaterialDetial == null || currentTeacherMaterialDetial.isEmpty()) {
				// 查询审核员列表
				List<JSONObject> auditMembers = materialDeclareService.getProcedureMember(request.getString("applicationId"));
				auditMembers.stream().forEach(memObj -> logger.info("auditMembers=>"+memObj));
				
				StringBuilder sb = new StringBuilder();
				auditMembers.stream().forEach(memObj -> sb.append(memObj.getString("teacherId")));
				if(sb.indexOf(user.getAccountPart().getId()+"") == -1) {
					setPromptMessage(response, "2", "当前身份没有这次数据");
					return response;
				}
			}
			
			Set<Long> ids = new HashSet<Long>();
			ids.add(object.getLong("teacherId"));
			JSONArray jsonArray = object.getJSONArray("auditDetail");
			if (jsonArray!=null && jsonArray.size() > 0) {
				 for (int i = 0; i < jsonArray.size(); i++) {
					 JSONObject obj = jsonArray.getJSONObject(i);
					 if (obj.containsKey("auditor")) {
						 ids.add(obj.getLong("auditor"));
					 }
					JSONArray  members_ = obj.getJSONArray("members");
					if (members_!=null ) {
						for (int j = 0; j < members_.size(); j++) {
							ids.add(members_.getJSONObject(j).getLong("member"));
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
			
         	List<Account> accList = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(ids), termInfoId);
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
			setPromptMessage(response, "-1", ERROR_MSG);
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return response;
	}
	
	
	@RequestMapping(value = "/insertMaterialDeclare" )
	@ResponseBody
	public JSONObject insertMaterialDeclare(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){

		JSONObject response = new JSONObject();
		request.put("applyDate",  new Date());
		Long teacherId = request.getLong("teacherId");
		Long schoolId =  request.getLong("schoolId");
		User user = commonDataService.getUserById(schoolId, teacherId);
		request.put("teacherId", user.getAccountPart().getId());
		try {
			int result = materialDeclareService.insertMaterialDeclare(request);
			if (result > 0) {
				setPromptMessage(response, "1", "新增物资申请成功");
			}else {
				setPromptMessage(response, "-1", "新增物资申请失败");
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", ERROR_MSG);
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return response;
	}
	
	
	
	
	@RequestMapping(value = "/updateMaterialDeclareProcedure" )
	@ResponseBody
	public JSONObject updateMaterialDeclareProcedure(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		Long teacherId = request.getLong("teacherId");
		Long schoolId = request.getLong("schoolId");
		User user = commonDataService.getUserById(schoolId, teacherId);
		param.put("teacherId", user.getAccountPart().getId());
		param.put("schoolId", schoolId);
		try {
			// int result =  materialDeclareService.updateMaterialDeclareProcedure(param);
			JSONObject result_ = materialDeclareService.updateMaterialDeclareProcedureNew(param);
			response = result_;
			/*if (result <= 1) {
				setPromptMessage(response, "1", "审核成功");
			}else {
				setPromptMessage(response, "1", "审核失败");
			}*/
		} catch (Exception e) {
			setPromptMessage(response, "-1", "审核失败");
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
	
	/**
	 * 一键催办
	* @Title: noticeAuditor 
	* @Description: TODO
	* @param @param req
	* @param @return
	* @return JSONObject
	* @throws 
	 */
	@RequestMapping(value = "/notice")
	@ResponseBody
	public JSONObject noticeAuditor(HttpServletRequest req,@RequestBody JSONObject request) {
		JSONObject response = new JSONObject();
		try {
			String applicationId = request.getString("applicationId");
			 if (StringUtils.isBlank(applicationId)) {
				 setPromptMessage(response, "-1", "参数异常"); 
				 return response;
			}
			 
			 JSONObject param = new JSONObject();
			 param.put("applicationId", request.getString("applicationId"));
			 JSONObject apply = materialDeclareService.getApplicationById(applicationId);
			 int count = apply.getIntValue("count");
			 if (count > 0) {
					param.put("status", 1);
					param.put("desc", apply.getString("reason"));
				    materialDeclareService.sendMsg(param, new Result<JSONObject>());
				    materialDeclareService.updateMaterialDeclareCount(param);
					setPromptMessage(response, "1", "提醒成功"); 
					return response;
			 }else{
				 setPromptMessage(response, "-1", "已达最大提醒次数"); 
				 return response;
			 }
 
		} catch (Exception e) {
			setPromptMessage(response, "-1", "系统异常");
			e.printStackTrace();
		}
		return response;
	}
 
	
}
