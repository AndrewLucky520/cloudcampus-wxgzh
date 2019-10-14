package com.talkweb.document.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.document.service.DocumentService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.ueditor.action.UEditorAction;

@RequestMapping("/documentManage")
@Controller
public class DocumentAction extends BaseAction implements Serializable{
//	@Value("#{settings['clientId']}")
//	private String clientId;
//
//	@Value("#{settings['clientSecret']}")
//	private String clientSecret;
//
//	@Value("#{settings['kafkaUrl']}")
//	private String kafkaUrl;
//	
//	private static final String PRODUCER_ID = "";//生产者ID，唯一标识。按自身业务来命名 (公文)
//	private static final String MSG_TYPE_CODE = "";
	
	private static final long serialVersionUID = 5725730760409806559L;
	Logger logger = LoggerFactory.getLogger(DocumentAction.class);
	
	@Value("#{settings['currentTermInfo']}")
	protected String curTermInfoId;
	
	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private FileServer fileServerImplFastDFS;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	/**
	 * redis
	 */
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
	
	@RequestMapping(value = "/getRole",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRole(HttpServletRequest req, HttpServletResponse res){
		JSONObject response = new JSONObject();
		boolean role = isMoudleManager(req, "cs1039");
		if(role){
			response.put("role", 0);
		}else{
			String schoolId = getXxdm(req);
			String teacherId = String.valueOf((long)req.getSession().getAttribute("accountId"));
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("teacherId", teacherId);
			List<JSONObject> list = documentService.getDocumentManager(param);
			if (list!=null && list.size() > 0) {
				response.put("role", 1);
			}else{
				response.put("role", 2);
			}
		}
		setPromptMessage(response, "1", "");
		return response;
	}
	
	@RequestMapping(value = "/getGroupsLevel", method = RequestMethod.POST)
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
 			String selectedTeacherIds= null;
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
 			 //headmaster.put("checked", "false");
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
 			 List<Account> accountList1 = commonDataService.getCourseTeacherList(selectedSemester,sch, "");
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
	
	@RequestMapping(value = "/getDocumentListH5",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDocumentListH5(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		request.put("clientType", "WX");
		return getDocumentList(req,request,res);
	}
	
	@RequestMapping(value = "/getDocumentList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDocumentList(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		String schoolId;
		String termInfoId;
		String accountId;
		
//		boolean test = false;
//		if(test) {
//			JSONArray array = request.getJSONArray("orgs");
//			School school = commonDataService.getSchoolById(Long.parseLong("820000000318"), "20182");
//			List<OrgInfo> orgList = commonDataService.getSchoolOrgList(school, "20182");
//			for(OrgInfo each : orgList) {
//				if(each.getId() == 82 || each.getId() == 1609) {
//					logger.info("getDocumentList : orgInfo=====> " + each.toString());
//				}
//			}
//			
//			List<Account> teacherAccounts;
//			//获取所有教职工
//			teacherAccounts = commonDataService.getAllSchoolEmployees(school, "20182", null);
//			logger.info("getDocumentList : 获取所有教职工=====> " + teacherAccounts.toString());
//			//获取机构教师
//			teacherAccounts.clear();
//			teacherAccounts = commonDataService.getOrgTeacherList("20182",Long.parseLong("820000000318"),"82,1609",null);
//			logger.info("getDocumentList : 获取机构教师=====> " + teacherAccounts.toString());
//		}
		
		String clientType = request.getString("clientType"); 
		if("WX".equals(clientType)) {
			//从H5微信调用时
			schoolId = request.getString("schoolId");
			termInfoId = curTermInfoId;
			accountId = request.getString("accountId");
		} else {
			schoolId = getXxdm(req);
			termInfoId = getCurXnxq(req);
			request.put("schoolId", schoolId);
			accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		}
		
		String queryType = request.getString("queryType");
		if ("1".equals(queryType)) {
			Map<String,Map<String,List<Object>>> map;
			Object keyOrgNameMap;
			if("WX".equals(clientType)) {
				keyOrgNameMap = request.get("keyOrgNameMap");
			} else {
				keyOrgNameMap = "auth."+schoolId+req.getSession().getId()+".00.orgNameMap";
			}
			map = new HashMap<String, Map<String, List<Object>>>();
			try {
				map = (Map<String, Map<String, List<Object>>>) redisOperationDAO.get(keyOrgNameMap);
				if(map != null) {
					logger.info("getDocumentList : orgNameMap=====> " + map.toString());
				} else {
					logger.info("getDocumentList : orgNameMap=====> " + null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//Map<String,Map<String,List<Object>>> map = (Map<String,Map<String,List<Object>>> )req.getSession().getAttribute("auth.orgNameMap");
			if (map!=null) {
				 Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
				 if(account != null) {
					 logger.info("getDocumentList : account=====> " + account.toString());
				 } else {
					 logger.info("getDocumentList : account=====> " + null);
				 }
				 Map<String,List<Object>> map2 =  map.get( account.getName());
				 logger.info("getDocumentList : account.getName() => " + account.getName());
				 if(map2 != null) {
					 logger.info("getDocumentList : account.getName() => map2=====> " + map2.toString());
				 } else {
					 logger.info("getDocumentList : account.getName() => map2=====> " + null);
				 }
				 List<Long> list = new ArrayList<Long>();
				 if(map2!=null){
					  for (String in : map2.keySet()) {
						  if (in.indexOf("8") > -1) {
							  list.add(Long.parseLong(in));
						  }else {
							List<Object> list2 =  map2.get(in);
							for (int i = 0; i < list2.size(); i++) {
								Object obj = list2.get(i);
								if (obj instanceof OrgInfo) {
									OrgInfo orgInfo = (OrgInfo)obj;
									list.add(orgInfo.getId());
								}else if (obj instanceof JSONObject) {
									JSONObject jsonObject = (JSONObject)obj;
									list.add(jsonObject.getLong("gradeId"));
								}
							}
						  }
					  }
				 }
				if (list.size() > 0) {
					logger.info("getDocumentList : list=====> " + list.toString());
					request.put("orgIdList", list);
				} else {
					logger.info("getDocumentList : list=====> null");
				}
			}
		}
		
		int page = request.getIntValue("page");
		int pageSize = request.getIntValue("pageSize");
		page = page<=0 ? 1:page;
		pageSize = pageSize<=0 ? 20 : pageSize;
		int position = (page -1 ) * pageSize;
//		String selectedSemester = commonDataService.getCurTermInfoId(Long.parseLong(schoolId));
		request.put("position", position);
		request.put("pageSize", pageSize);
		request.put("teacherId", accountId);
		request.put("termInfoId", termInfoId);
		
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

		logger.info("getDocumentList : request=====>" + request.toString());
		List<JSONObject> list = documentService.getDocumentList(request);
		
		 JSONObject param = new JSONObject();
		 param.put("schoolId", schoolId);
		 List<JSONObject> typeList = documentService.getDocumentTypeList(param);
		 Map<String, String> map = new HashMap<String, String>();
		 if (typeList==null || typeList.size() == 0) {
			 param.put("schoolId", "ALL");
			 typeList = documentService.getDocumentTypeList(param);
		 } 
		 
		 JSONObject obj = null;
		 for (int i = 0; i < typeList.size(); i++) {
			 obj = typeList.get(i);
			 map.put(obj.getString("categoryId"), obj.getString("categoryName"));
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < list.size(); i++) {
			JSONObject object =list.get(i);
			object.put("index", i +1 );
			object.put("createDate", format.format(object.getDate("createDate")));
			object.put("categoryName", map.get(object.getString("categoryId")));
		}
		response.put("data", list);
		logger.info("getDocumentList : 查询成功 data=====> " + list.toString());
		setPromptMessage(response, "1", "查询成功");
		return response;
	}
	
	@RequestMapping(value = "/uploadDocument",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject uploadDocument(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String accountId = String.valueOf((long)req.getSession().getAttribute("accountId"));
		String schoolId = getXxdm(req);
		request.put("teacherId", accountId);
		request.put("schoolId", schoolId);
		request.put("createDate", new Date());
		request.put("documentId", UUIDUtil.getUUID());
		documentService.uploadDocument(request);
		int viewType = request.getIntValue("viewType");
		JSONArray array =null;
		if (viewType==1) {
			array = request.getJSONArray("orgs");
		}else if (viewType==2) {
			array = request.getJSONArray("teachers");
		}
		List<JSONObject> list = new ArrayList<JSONObject>();
		if (array!=null) {
			for (int i = 0; i < array.size(); i++) {
				String id = array.getString(i);
				JSONObject object = new JSONObject();
				object.put("schoolId", schoolId);
				object.put("documentId", request.getString("documentId"));
				object.put("teacherGroupId", id);
				list.add(object);
			}
		}
		
		if (list.size() > 0) {
			documentService.addDocumentReview(list);
		}
		
		/**
		 * 模板消息
		 */
//		if(TEST) {
//			JSONObject param = new JSONObject();
//			if(!sendAppMsg(param)) {
//				setPromptMessage(response, "1", "发送公文通知失败！");
//				return response;
//			}
//		}
		setPromptMessage(response, "1", "新增公文成功");
		return response;
	}
	
//	/**
//	 * 发送Kafka消息通知
//	 */
//	private boolean sendAppMsg(JSONObject param) {
//		//消息内容，要求一个完整的JSON的结构体
//		JSONObject msgCenterPayLoad = new JSONObject();
//		JSONObject msg = new JSONObject();
//	    String msgId = UUIDUtil.getUUID().replace("-", "");
//		msg.put("msgId", msgId);
//		msg.put("msgTitle", "你收到一份新的公文！");
//		msg.put("msgContent", param.getString("type") + " " + param.getString("number"));
//		msg.put("msgUrlPc", "" + "");//暂定
//		msg.put("msgUrlApp", "" + "" );//暂定
//		msg.put("msgOrigin", "公文提醒");
//		msg.put("msgTypeCode", MSG_TYPE_CODE);
//		msg.put("schoolId", param.getString("schoolId"));
//		msg.put("creatorName", param.getString("creatorName"));
//
//		/**
//		 * 根据产品需求文档，设置数据
//		 * first,keyword1,keyword2,...,remark
//		 */
//		JSONObject first = new JSONObject();
//		first.put("value", "你收到一份新的公文！");
//
//		JSONObject keyword1 = new JSONObject();
//		keyword1.put("value", "任务标题:" + param.getString("title"));
//		
//		JSONObject keyword2 = new JSONObject();
//		keyword2.put("value", "公文类型:" + param.getString("type"));
//		
//		JSONObject keyword3 = new JSONObject();
//		keyword3.put("value", "公文编号:" + param.getString("number"));
//
//		JSONObject remark = new JSONObject();
//		remark.put("value", "点击此消息查看详情！");
//		
//		JSONObject data = new JSONObject();
//		data.put("first", first);
//		data.put("keyword1", keyword1);
//		data.put("keyword2", keyword2);
//		data.put("keyword3", keyword3);
//		data.put("remark", remark);
//		msg.put("msgWxJson", data);
//		
//		//消息接收者列表
//		List<JSONObject> msgCenterReceiversArray = (List<JSONObject>) param.get("receivers");
//		
//		msgCenterPayLoad.put("msg", msg);
//		msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
//		logger.info("msgCenterPayLoad=====>" + msgCenterPayLoad.toString());
//		try {
//			KafkaUtils.sendAppMsg(kafkaUrl, msgId, msgCenterPayLoad, PRODUCER_ID, clientId, clientSecret);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}

	@RequestMapping(value = "/getDocumentDetailH5",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDocumentDetailH5(HttpServletRequest req,  @RequestBody JSONObject request, HttpServletResponse res) {
		request.put("clientType", "WX");
		return getDocumentDetail(req,request,res);
	}
	
	@RequestMapping(value = "/getDocumentDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDocumentDetail(HttpServletRequest req,  @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		String schoolId;
		String termInfoId;
		
		String clientType = request.getString("clientType"); 
		if("WX".equals(clientType)) {
			schoolId = request.getString("schoolId");
			termInfoId = curTermInfoId;
		} else {
			schoolId = getXxdm(req);
			termInfoId = getCurXnxq(req);
		}
		
		request.put("schoolId", schoolId);
		JSONObject object = documentService.getDocumentDetail(request);
		
		 JSONObject param = new JSONObject();
		 param.put("schoolId", schoolId);
		 List<JSONObject> typeList = documentService.getDocumentTypeList(param);
		 Map<String, String> map = new HashMap<String, String>();
		 if (typeList==null || typeList.size() == 0) {
			 param.put("schoolId", "ALL");
			 typeList = documentService.getDocumentTypeList(param);
		 } 
		 
		 JSONObject obj = null;
		 for (int i = 0; i < typeList.size(); i++) {
			 obj = typeList.get(i);
			 map.put(obj.getString("categoryId"), obj.getString("categoryName"));
		}
		
 
		if (object!=null) {
			String viewType = object.getString("viewType");
			object.put("categoryName", map.get(object.getString("categoryId")));
			if ("1".equals(viewType) || "2".equals(viewType)) {
				List<JSONObject> list =  documentService.getReviewList(request);
				Map<Long, String> id2Name = new HashMap<Long, String>();
				if ("2".equals(viewType)) {
					Set<Long> ids = new HashSet<Long>();
					for (int i = 0; i < list.size(); i++) {
						ids.add(list.get(i).getLong("teacherGroupId"));
					}
					List<Account> accList = commonDataService.getAccountBatch(Long.parseLong(schoolId), new ArrayList<Long>(ids), termInfoId);
					
					if(CollectionUtils.isNotEmpty(accList)) {
						for(Account acc : accList) {
							id2Name.put(acc.getId(), acc.getName());
						}
					}
				}
				Map<Long, String>  id2OrgName = new HashMap<Long, String>();
				if ("1".equals(viewType)) {
					School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfoId);
					List<OrgInfo> orgs = commonDataService.getSchoolOrgList(school, termInfoId);
					 List<Grade> gradeList = commonDataService.getGradeList(school,termInfoId);
					for (int i = 0; i < orgs.size(); i++) {
						OrgInfo orgInfo = orgs.get(i);
						id2OrgName.put(orgInfo.getId(), orgInfo.getOrgName());
					}
					
					for (int i = 0; i < gradeList.size(); i++) {
						Grade grade = gradeList.get(i);
						String gradeName = AccountStructConstants.T_GradeLevelName
		 						.get((grade.getCurrentLevel()));
						id2OrgName.put(grade.getId(), gradeName);
					}
				}
				id2OrgName.put(86L, "科室负责人");
				id2OrgName.put(82L, "年级组负责人");
				id2OrgName.put(81L, "教研组负责人");
				id2OrgName.put(83L, "备课组负责人");
				
				if (list!=null) {
					Iterator<JSONObject> iterator = list.iterator();
					while (iterator.hasNext()) {
						JSONObject object2 = iterator.next();
						if ("1".equals(viewType)) {
							object2.put("orgId", object2.getString("teacherGroupId"));
							if (StringUtils.isBlank( id2OrgName.get(object2.getLong("orgId")))) {
								iterator.remove();
							}else {
								object2.put("orgName", id2OrgName.get(object2.getLong("orgId")));
							}
							
						}else if ("2".equals(viewType)) {
							object2.put("teacherId", object2.getString("teacherGroupId"));
							if (StringUtils.isBlank( id2Name.get(object2.getLong("teacherId")))) {
								iterator.remove();
							}else {
								object2.put("teacherName", id2Name.get(object2.getLong("teacherId")));
							}
							
						}
					}
					
				}
				
 
				if ("1".equals(viewType)) {
					object.put("orgs", list);
				}else if ("2".equals(viewType)) {
					object.put("teachers", list);
				}
			}

		}
		 
		response.put("data", object);
		setPromptMessage(response, "1", "查询成功！");
		return response;
	}
	
	@RequestMapping(value = "/delDocumentH5",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delDocumentH5(HttpServletRequest req,  @RequestBody JSONObject request, HttpServletResponse res) {
		request.put("clientType", "WX");
		return delDocument(req,request,res);
	}
	
	@RequestMapping(value = "/delDocument",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delDocument(HttpServletRequest req,  @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = request;
		
		String schoolId;
		String clientType = request.getString("clientType"); 
		if("WX".equals(clientType)) {
			schoolId = request.getString("schoolId");
		} else {
			schoolId = getXxdm(req);
		}
		
		param.put("schoolId", schoolId);
		
		List<JSONObject> list = documentService.getDocumentFileList(param);
		for (int i = 0; i < list.size(); i++) {
			String url = list.get(i).getString("fileUrl");
			try {
				if (StringUtils.isNotBlank(url)) {
					fileServerImplFastDFS.deleteFile(url);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		documentService.delDocumentFile(param);
		documentService.delDocument(param);
		documentService.delDocumentReview(param);
		setPromptMessage(response, "1", "删除成功！");
		return response;
	}
	
	
	@RequestMapping(value = "/uploadDocumentFile",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject uploadDocumentFile(HttpServletRequest req, @RequestParam("fileBoby") MultipartFile file ,  HttpServletResponse res) {
		JSONObject response = new JSONObject();
		 
		if (file!=null) {
			 File df = null;
				try {
					String fileName = file.getOriginalFilename();
					String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
					fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +suffix;
					String tempName0 = UUID.randomUUID().toString()+"."+suffix;
					df = new File(tempName0);
					file.transferTo(df);
					String fileId = fileServerImplFastDFS.uploadFile(df,tempName0);
					if (StringUtils.isNotBlank(fileId)) {
						response.put("fileName", fileName);
						response.put("fileUrl", fileId);
						setPromptMessage(response, "1", "上传成功");
					}else{
						setPromptMessage(response, "-3", "文件上传出现问题,请联系管理员!");
					}
				} catch (Exception e) {
					setPromptMessage(response, "-3", "文件上传出现问题,请联系管理员!");
					e.printStackTrace();
				}finally {
					if(df!=null)df.delete();
				}
		}
 
		return response;
	}
	
	@RequestMapping(value = "/delDocumentFile",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delDocumentFile(HttpServletRequest req,  @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String url = request.getString("fileUrl");
		try {
			if (StringUtils.isNotBlank(url)) {
				fileServerImplFastDFS.deleteFile(url);
				setPromptMessage(response, "1", "删除成功!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "删除失败!");
		}
		
		return response;
	 
	}
	
	@RequestMapping(value = "/preDownloadDocumentFile")
	@ResponseBody
	public void preDownloadDocumentFile(HttpServletRequest req,HttpServletResponse res) {
		String urlTemp;
		String originTemp;
		urlTemp = req.getParameter("fileUrl");
		originTemp = req.getParameter("fileName");	
		
		String url = null;
		try {
			url = URLDecoder.decode(urlTemp, "UTF-8");
			if (originTemp!=null) {
				String encode = UEditorAction.getEncoding(originTemp); 
				originTemp = new String(originTemp.getBytes(encode),"UTF-8");
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
 
		ByteArrayInputStream bis = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis1 = null;
		File temp = null;
		try {
				String fileName = originTemp;
				fileName = fileName.replace(" ", "");
				int i = fileServerImplFastDFS.downloadFile(url, fileName);
				System.out.println( i);
				temp = new File(fileName);
				// 写入数据结束
				res.setContentType("octets/stream");
				String downLoadName = String.valueOf(URLEncoder.encode(fileName, "UTF-8"));
				downLoadName = replaceFileName(downLoadName);
				System.out.println(downLoadName);
				res.addHeader("Content-Disposition", "attachment;filename=".concat(downLoadName ));
				bis1 = new BufferedInputStream(new FileInputStream(temp));
				bos = new BufferedOutputStream(res.getOutputStream());
				byte[] buff = new byte[2048];
				int bytesRead;
				while (-1 != (bytesRead = bis1.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
				bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bis1 != null)
				try {
					bis1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(temp!=null){
				temp.delete();
			}
		}
	}
	
	
	@RequestMapping(value = "/setDocumentTypeAndManager",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject setDocumentTypeAndManager(HttpServletRequest req, @RequestBody JSONObject request,  HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		JSONArray managerList = request.getJSONArray("manager");
		JSONArray categorylist = request.getJSONArray("category");
		String schoolId = getXxdm(req);
		 JSONObject param = new JSONObject();
		 param.put("schoolId", schoolId);
		 documentService.delDocumentManager(param);
		 List<JSONObject> list = new ArrayList<JSONObject>();
		 if (managerList!=null && managerList.size() > 0) {
			 for (int i = 0; i < managerList.size(); i++) {
				 JSONObject object = managerList.getJSONObject(i);
				 object.put("schoolId", schoolId);
				 list.add(object);
			 } 
			 documentService.addDocumentManager(list);
		 }
 
		 List<JSONObject> cateList = new ArrayList<JSONObject>();
		 documentService.delDocumentType(param);
		 if (categorylist!=null && categorylist.size() > 0) {
			 String categoryId = null;
			 for (int i = 0; i < categorylist.size(); i++) {
				JSONObject object = categorylist.getJSONObject(i);
				categoryId = object.getString("categoryId");
				if (StringUtils.isNotBlank(categoryId)) {
					object.put("categoryId", categoryId);
				}else {
					object.put("categoryId", UUIDUtil.getUUID());
				}
				object.put("schoolId", schoolId);
				object.put("seq", i);
				cateList.add(object);
			}
			 documentService.addDocumentType(cateList);
		}else {
			JSONObject object = new JSONObject();
			object.put("categoryId", " ");
			object.put("categoryName", " ");
			object.put("schoolId", schoolId);
			object.put("seq", 0);
			cateList.add(object);
			documentService.addDocumentType(cateList);//添加一个为空的，表明不用默认的 上级来文校内发文
		}
		
		setPromptMessage(response, "1", "更新成功！");
		
		return response;
	}
	
	
	@RequestMapping(value = "/getDocumentTypeAndManager",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDocumentTypeAndManager(HttpServletRequest req, @RequestBody JSONObject request,  HttpServletResponse res) {
		JSONObject response = new JSONObject();
		 String schoolId = getXxdm(req);
		 JSONObject param = new JSONObject();
		 String termInfoId = getCurXnxq(req);
		 param.put("schoolId", schoolId);
		 List<JSONObject> typeList = documentService.getDocumentTypeList(param);
		 if (typeList!=null && typeList.size()==0) {
			 param.put("schoolId", "ALL");
			 typeList = documentService.getDocumentTypeList(param);
		 }else if (typeList!=null && typeList.size()==1) {
			 if (typeList.get(0).getString("categoryId").trim().length()==0) {//空的不存在
				 typeList = new ArrayList<JSONObject>();
			 } 
		 }
		 param.put("schoolId", schoolId);
		 List<JSONObject> managerList  = documentService.getDocumentManager(param);
		 if (managerList!=null) {
			 Set<Long> ids = new HashSet<Long>();
			 for (int i = 0; i < managerList.size(); i++) {
				 JSONObject object = managerList.get(i);
				 ids.add(object.getLong("teacherId"));
			 }
			 List<Account> accList = commonDataService.getAccountBatch(Long.parseLong(schoolId), new ArrayList<Long>(ids), termInfoId);
			 Map<Long, String> id2Name = new HashMap<Long, String>();
			 for (int i = 0; i < accList.size(); i++) {
				 for(Account acc : accList) {
						id2Name.put(acc.getId(), acc.getName());
					}
			 }
	 
			 Iterator<JSONObject> iterator = managerList.iterator();
			 while (iterator.hasNext()) {
				 JSONObject object = iterator.next();
				 if (StringUtils.isBlank(id2Name.get(object.getLong("teacherId")))) {
					 iterator.remove();
				 }else {
					 object.put("teacherName", id2Name.get(object.getLong("teacherId")));
				}
			}
			 
		 }
		
		 
		 Map<String, List<JSONObject>> map = new HashMap<String, List<JSONObject>>();
		 map.put("manager", managerList);
		 map.put("category", typeList);
		 setPromptMessage(response, "1", "查询成功！");
		 response.put("data", map);
		return response;
	}
	
	
	@RequestMapping(value = "/getDocumentTypeList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDocumentTypeList(HttpServletRequest req, @RequestBody JSONObject request,  HttpServletResponse res) {
		JSONObject response = new JSONObject();
		 JSONObject param = new JSONObject();
		 String schoolId = getXxdm(req);
		 param.put("schoolId", schoolId);
		 List<JSONObject> typeList = documentService.getDocumentTypeList(param);
		 if (typeList==null|| typeList.size()==0) {
			 param.put("schoolId", "ALL");
			 typeList = documentService.getDocumentTypeList(param);
		 }else if (typeList!=null && typeList.size()==1) {
			 if (typeList.get(0).getString("categoryId").trim().length()==0) {//空的不存在
				 typeList = new ArrayList<JSONObject>();
			 } 
		 }
		 response.put("data", typeList);
		 setPromptMessage(response, "1", "查询成功！");
		return response;
		
	}
	
	private String replaceFileName(String fileName) {
		fileName = fileName.replace("%28", "(");
		fileName = fileName.replace("%29", ")");
		fileName = fileName.replace("%7E", "~");
		fileName = fileName.replace("%21", "!");
		fileName = fileName.replace("%40", "@");
		return fileName;
	}
	 
	private JSONObject getTeacherArray(String parentId,
 			List<Long> accountIdList,Map<Long,String> teacherMap,String selectedTeacherIds) {
 		JSONArray teacherArray = new JSONArray();
 		if (CollectionUtils.isNotEmpty(accountIdList)) {
 			for (long accountId : accountIdList) {
 				if(!StringUtils.isEmpty(selectedTeacherIds)&&selectedTeacherIds.contains(accountId+""))
 				{
 					continue;
 				}
 				 String teacherName = "";
				 if (teacherMap.containsKey(accountId)){
					 teacherName = teacherMap.get(accountId); 
				 }
				 JSONObject temp = new JSONObject();
				 temp.put("id", parentId + accountId);
				 temp.put("text", teacherName);
 	
 				//temp.put("checked", "false");
 				JSONObject attibute = new JSONObject();
 				attibute.put("teacherId", accountId + "");
 				attibute.put("teacherName", teacherName);
 				temp.put("attributes", attibute);
 				teacherArray.add(temp);
 			}
 		}
 		JSONObject result = new JSONObject();
 		if (teacherArray.size() > 0) {
 			result.put("isEmpty", "false");
 			result.put("teachers", teacherArray);
 		} else {
 			result.put("isEmpty", "true");
 		}
 		return result;
 	}
}
