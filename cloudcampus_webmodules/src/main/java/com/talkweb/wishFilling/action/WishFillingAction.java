package com.talkweb.wishFilling.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.service.PlacementTaskService;
import com.talkweb.wishFilling.service.WishFillingCommonService;
import com.talkweb.wishFilling.service.WishFillingService;
import com.talkweb.wishFilling.service.WishFillingStudentService;


/** 
 * 新高考志愿填报-管理员设置action
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0  2016.11.3  author:zhh
 */
@Controller
@RequestMapping(value = "/wishFilling/")
public class WishFillingAction extends BaseAction{
	 	@Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private WishFillingService wishFillingService;
	    @Autowired
	    private WishFillingCommonAction  wishFillingCommonAction;
	    @Autowired
	    private WishFillingCommonService wishFillingCommonService;
	    @Autowired
	    private WishFillingStudentService wishFillingStudentService;
	    @Autowired
	    private PlacementTaskService placementTaskService;
		private static final Logger logger = LoggerFactory.getLogger(WishFillingAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
/***
 *  二、管理员设置模块
 */
		/**
		 * (1)获取填报列表
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getTbList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getTbList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			List<JSONObject> data = new ArrayList<JSONObject>();
			try {
				String schoolId = getXxdm(req);
				String termInfo = request.getString("termInfo");
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				data = wishFillingService.getTbList(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * (2)创建填报
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/createTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject createTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfName = request.getString("wfName");
				String schoolId = getXxdm(req);
				String termInfo = request.getString("termInfo");
				String xn = termInfo.substring(0,4);
				String xqm = termInfo.substring(4,termInfo.length());
				String selectWayToJunior = request.getString("selectWayToJunior");
				param.put("wfId", UUIDUtil.getUUID());
				param.put("wfName", wfName);
				param.put("schoolId", schoolId);
				param.put("xn", xn);
				param.put("xqm", xqm);
				param.put("termInfo", termInfo);
				param.put("selectWayToJunior", selectWayToJunior);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				if(StringUtils.isBlank(wfName)){
					setPromptMessage(response, "-2", "选科名称不能为空");
					return response;
				}
				//判断重名（同一学年学期下不可重名）
				List<JSONObject> list = wishFillingService.getTbNameList(param);
				for(JSONObject obj:list){
					String wfN = obj.getString("wfName");
					if(wfName.equals(wfN)){
						setPromptMessage(response, "-1", "选科名称不能重复");
						return response;
					}
				}
				wishFillingService.createTb(param);
				setPromptMessage(response, "0", "操作成功");
			} catch (Exception e) {
				setPromptMessage(response, "-2", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * (3)修改填报名称
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/updateTbName", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject updateTbName(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfName = request.getString("wfName");
				String termInfo = request.getString("termInfo");
				String wfId = request.getString("wfId");
				String schoolId = getXxdm(req);
				param.put("wfId", wfId);
				param.put("wfName", wfName);
				param.put("termInfo", termInfo);
				param.put("schoolId", schoolId);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				if(StringUtils.isBlank(wfName)){
					setPromptMessage(response, "-2", "选科名称不能为空");
					return response;
				}
				//判断重名（同一学年学期下不可重名）
				List<JSONObject> list = wishFillingService.getTbNameList(param);
				for(JSONObject obj:list){
					String wfId2=obj.getString("wfId");
					if(wfId.equals(wfId2)){continue;}
					String wfN = obj.getString("wfName");
					if(wfName.equals(wfN)){
						setPromptMessage(response, "-1", "选科名称不能重复");
						return response;
					}
				}
				wishFillingService.updateTbName(param);
				setPromptMessage(response, "0", "操作成功");
			} catch (Exception e) {
				setPromptMessage(response, "-2", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * (4)获取填报详情
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
			    data = wishFillingService.getTb(param);
				if(data!=null){
					data.put("wfId", wfId);//前端需要
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * (6)删除填报
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/deleteTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject deleteTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String flag = request.getString("flag");
				String schoolId = getXxdm(req);
				
				param.put("wfId", wfId);
				param.put("termInfo", termInfo);
				param.put("schoolId", schoolId);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				//分班是否已经引用该轮次，已引用则不允许删除
				if("1".equals(flag)){
					List<String> usedIds = placementTaskService.getAllUsedWfId(termInfo);
					if(usedIds!=null && usedIds.contains(wfId)){
						setPromptMessage(response, "-1", "已有分班数据不允许删除！");
						return response;
					}
				}
				//查询
				if(!"1".equals(flag)){
					JSONObject tbObj = wishFillingService.getTb(param);
					String isByElection = tbObj.getString("isByElection");
					String wfWay = tbObj.getString("wfWay");
					int hasNum = 0;
					if("0".equals(isByElection)){
						hasNum = wishFillingService.getTotalStudentCount(param);
					}else{
						hasNum = wishFillingService.getByTotalStudentCount(param);
					}
					response.put("code",0);
					if(hasNum>0){
						response.put("hasNum",1);
					}else{
						response.put("hasNum",0);
					}
					return response;
				}
				//删除
				wishFillingService.deleteTb(param);
				setPromptMessage(response, "0", "操作成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * (5)编辑填报详情
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/updateTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject updateTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String wfStartTime = request.getString("wfStartTime");
				String wfEndTime = request.getString("wfEndTime");
				//String wfNum = request.getString("wfNum");
				String wfWay = request.getString("wfWay");
				String wfGradeId = request.getString("wfGradeId");
				param.put("wfStartTime", wfStartTime);
				param.put("wfEndTime", wfEndTime);
				param.put("wfId", wfId);
				//param.put("wfNum", wfNum);
				param.put("wfWay", wfWay);
				param.put("wfGradeId", wfGradeId);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				
				if("0".equals(wfWay)){ //单科
					String s = request.getString("subjectIds");
					List<Long> subList= StringUtil.toListFromString(s);
					param.put("subList", subList);
					param.put("wfNum", 3);
				}else{//组合
					List<JSONObject> zhs = (List<JSONObject>) request.get("zhs");
					param.put("zhs", zhs);
					param.put("wfNum", 1);
				}
				param.put("schoolId", getXxdm(req));
				
				String xn = termInfo.substring(0, 4);
				String xqm = termInfo.substring(4,termInfo.length());
				String wfUseGrade=request.getString("wfGradeId");
				param.put("wfUseGrade", wfUseGrade);
				param.put("termInfo", termInfo);
				param.put("xn", xn);
				param.put("xqm", xqm);
				int i=wishFillingService.updateTb(param);
				if(i>0){
					setPromptMessage(response, "0", "操作成功");
				}else if(i==-2){
					setPromptMessage(response, "-1", "补选已设置，不可编辑正选设置项");
				}else if (i==-3){
					setPromptMessage(response, "-2",  "已有分班数据不允许修改！");
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
		 * (7)查看进度(单科)
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getProgressTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getProgressTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4, 5);
				param.put("schoolYear", schoolYear);
				param.put("termInfoId", termInfoId);
			     data = wishFillingService.getProgressTb(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * (8)查看进度(组合)
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getProgressTbByZh", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getProgressTbByZh(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4, 5);
				param.put("schoolYear", schoolYear);
				param.put("termInfoId", termInfoId);
			     data = wishFillingService.getProgressTbByZh(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * (9)设置补选
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/updateByElection", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject updateByElection(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String byStartTime = request.getString("byStartTime");
				String byEndTime = request.getString("byEndTime");
				String termInfo = request.getString("termInfo");
				List<JSONObject> zhs = (List<JSONObject>) request.get("zhs");
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				if(zhs.size()<=0){ //取消补选
					param.put("byStartTime", "");
					param.put("byEndTime", "");
					param.put("isByElection", 0);
					param.put("wfNum", 3);//将wfNum由1更新成3
				}else{
					param.put("byStartTime", byStartTime);
					param.put("byEndTime", byEndTime);
					param.put("isByElection", 1);
					param.put("wfNum", 1); //将wfNum由3更新成1
				}
				param.put("zhs", zhs);
				param.put("byStartTime", byStartTime);
				param.put("byEndTime", byEndTime);
				
			    int i = wishFillingService.updateByElection(param);
				if(i>0){
					setPromptMessage(response, "0", "操作成功");
				}else if (i==-2){
					setPromptMessage(response, "-2", "正选还未结束，不能设置补选项");
				}else if(i==-3){
					setPromptMessage(response, "-3", "组合设置下不能补选");
				}else if (i==-4){
					setPromptMessage(response, "-4", "已有分班数据不允许修改！");
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
		 * (10)设置补选
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getByElection", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getByElection(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				
			    data = wishFillingService.getByElection(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
	 
		/**
		 * (11)给未选科的学生/家长发送模板消息
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/sendWx", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject sendWx(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			int i =1;
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String xn = termInfo.substring(0, 4);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				JSONObject tb = wishFillingService.getTb(param);
				 
				String wfStartTimeD = tb.getString("wfStartTime") ;
				String wfEndTimeD = tb.getString("wfEndTime") ;
				String byStartTimeD = tb.getString("byStartTime") ;
				String byEndTimeD = tb.getString("byEndTime") ;
				logger.info("wf:"+tb);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				Date wfStartTimeDate = null;
				Date wfEndTimeDate = null;
				Date byStartTimeDate = null;
				Date byEndTimeDate = null;
				if(StringUtils.isNotBlank(wfStartTimeD)) {
				  wfStartTimeDate = sdf.parse(wfStartTimeD);
				}
				if(StringUtils.isNotBlank(wfEndTimeD)) {
				  wfEndTimeDate = sdf.parse(wfEndTimeD);
				}
				if(StringUtils.isNotBlank(byStartTimeD)) {
				  byStartTimeDate = sdf.parse(byStartTimeD);
				}
				if(StringUtils.isNotBlank(byEndTimeD)) {
				  byEndTimeDate = sdf.parse(byEndTimeD);
				}
				String wfName = tb.getString("wfName");
				String isByElection = tb.getString("isByElection");
				String gradeId = tb.getString("wfGradeId");
				
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");
				String wfStartTime="";
				String wfEndTime ="";
				String byStartTime ="";
				String byEndTime="";
				if(wfStartTimeDate!=null) {
				  wfStartTime  = formatter.format(wfStartTimeDate);
				}
				if(wfEndTimeDate!=null) {
					 wfEndTime   = formatter.format(wfEndTimeDate);
				}
				if(byStartTimeDate!=null) {
				  byStartTime    = formatter.format(byStartTimeDate);
				}
				if(byEndTimeDate!=null) {
				  byEndTime     = formatter.format(byEndTimeDate);
				}
				request.put("isRelatedTeacher", 0);
				request.put("isAll", 1);
				request.put("gradeId", gradeId);
				JSONObject classObj = wishFillingCommonAction.getClassList(req, request, res);
				List<JSONObject> data = (List<JSONObject>) classObj.get("data");
				List<String> classIds = (List<String>) data.get(0).get("value");
				String classId = this.listToString(classIds);
				
				param.put("classId", classId);
				param.put("isByElection", isByElection);
				param.put("wfStartTime", wfStartTime);
				param.put("wfEndTime", wfEndTime);
				param.put("byStartTime", byStartTime);
				param.put("byEndTime", byEndTime);
				param.put("wfName", wfName);
				param.put("schoolExtId", school.getExtId());
				param.put("schoolName", school.getName());
				param.put("gradeId", gradeId);
				Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
				String njCode = allCommonDataService.ConvertSYNJ2NJDM(gradeId, xn);
				logger.info("wf:gradeId "+gradeId+" xn:"+xn+" njcode "+njCode+" gradename" +njName.get(njCode));
				
				param.put("gradeName", njName.get(T_GradeLevel.findByValue(Integer.parseInt(njCode))));
				i  = wishFillingService.sendWx(param);
				if(i>0){
					setPromptMessage(response, "0", "发送成功");
				}else{
					setPromptMessage(response, "-1", "发送失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "发送失败");
				e.printStackTrace();
			}
			return response;
		}
	 
		
		public  <E> String listToString(Collection<E> list) {
			return listToString(list, ",");
		}

		public  <E> String listToString(Collection<E> list, String sep) {
			String str = "";
			if (list != null) {
				for (Object obj : list) {
					if (obj != null) {
						if (str.length() > 0)
							str += sep;
						str += obj.toString();
					}
				}
			}
			return str;
		}
		public static void main(String[] args) {
			Date currentTime = new Date();
			 SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");
			  String dateString = formatter.format(currentTime);
			  System.out.println(dateString);
			  
			  String wfStartTimeD = "2019-07-16 00:00:00";
			  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        try {
					System.out.println(sdf.parse(wfStartTimeD));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
		}
		
		
/***
 *  三、管理员统计模块
 */
		/**
		 *(1)按学生查看结果
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getStaticListByStudent", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getStaticListByStudent(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String classId = request.getString("classId");
				String name = request.getString("name");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				String schoolYear = termInfo.substring(0,4);
				String termInfoId = termInfo.substring(4,5);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("name", name);
				param.put("classId", classId);
				param.put("termInfo", termInfo);
				param.put("schoolYear", schoolYear);
				param.put("termInfoId", termInfoId);
				JSONObject wf = wishFillingService.getTb(param);
				String useGrade = wf.getString("wfGradeId");
				T_GradeLevel currentLevel = T_GradeLevel.findByValue(Integer.parseInt(allCommonDataService.ConvertSYNJ2NJDM(useGrade, termInfo.substring(0, 4))));
				Grade g = allCommonDataService.getGradeByGradeLevel(Long.parseLong(schoolId), currentLevel, termInfo);
				List<Grade> gList= new ArrayList<Grade>();
				if(!g.isGraduate){
					gList.add(g);
				}
				List<Classroom> cList = allCommonDataService.getSimpleClassList(school, gList, termInfo);
				List<Long> classIds = new ArrayList<Long>();
				String allClassIds ="";
				for(Classroom c:cList){
					Long cId = c.getId();
					classIds.add(cId);
					allClassIds+=cId+",";
				}
				if(classIds.size()>0){
					param.put("allClassIdList", classIds);
				}else{
					Long cId= -1l;
					classIds.add(cId);
					param.put("allClassIdList", classIds);
				}
				if(StringUtils.isNotBlank(allClassIds)){
					allClassIds=allClassIds.substring(0,allClassIds.length()-1);
				}else{
					allClassIds="-1";
				}
				param.put("allClassIds", allClassIds);
				
			    data = wishFillingService.getStaticListByStudent(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 *(2)按学生查看结果-未提交学生名单
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getNoselectedStudentList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getNoselectedStudentList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String classId = request.getString("classId");
				String schoolId = getXxdm(req);
				String termInfo = request.getString("termInfo");
				String schoolYear = termInfo.substring(0,4);
				String termInfoId = termInfo.substring(4,5);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("classId", classId);
				param.put("wfId", wfId);
				param.put("schoolYear", schoolYear);
				param.put("termInfoId", termInfoId);
				param.put("termInfo", termInfo);
				
			    data = wishFillingService.getNoselectedStudentList(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 *(3)按学生查看结果-调整学生填报结果查看
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getStudentTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getStudentTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject  data = new JSONObject();
			JSONObject  returnObj = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String accountId = request.getString("accountId");
				String classId = request.getString("classId");
				String useGrade = request.getString("gradeId");
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				String schoolId = getXxdm(req);
				param.put("schoolId", schoolId);
				param.put("classId", classId);
				param.put("wfId", wfId);
				param.put("useGrade", useGrade);
				param.put("accountId", accountId);
				param.put("termInfo", termInfo);
				
				data = wishFillingService.getStudentTb(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 *(4)已删除学生删除接口
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/deleteStudentTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject deleteStudentTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject  data = new JSONObject();
			JSONObject  returnObj = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String accountId = request.getString("accountId");
				String classId = request.getString("classId");
				String useGrade = request.getString("gradeId");
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("classId", classId);
				param.put("wfId", wfId);
				param.put("useGrade", useGrade);
				param.put("accountId", accountId);
				param.put("termInfo", termInfo);
				
				int i = wishFillingService.deleteStudentTb(param);
				if(i>0){
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
		 *(5)按学生查看结果-调整学生填报结果更新
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/updateStudentTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject updateStudentTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String accountId = request.getString("accountId");
				String classId = request.getString("classId");
				String useGrade = request.getString("gradeId");
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String schoolYear = termInfo.substring(0,4);
				String termInfoId = termInfo.substring(4,5);
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				JSONArray s = request.getJSONArray("subjectIds");
				List<Long> subList= new ArrayList<Long>();
				for(int i=0;i<s.size();i++){
					String ss=(String) s.get(i);
					subList.add(Long.parseLong(ss));
				}
				String zhId = request.getString("zhId");
				
				param.put("subList", subList);
				param.put("schoolId", schoolId);
				param.put("classId", classId);
				param.put("wfId", wfId);
				param.put("useGrade", useGrade);
				param.put("accountId", accountId);
				param.put("zhId", zhId);
				
				param.put("termInfoId", termInfoId);
				param.put("schoolYear", schoolYear);
				param.put("termInfo", termInfo);
				param.put("isManager", 1);
				int i=wishFillingStudentService.addStudentTb(param);
				if(i>0){
					setPromptMessage(response, "0", "操作成功");
				}else if(i==-2){
					setPromptMessage(response, "-1", "选科时间未开放！");
				}else if(i==-3){
					setPromptMessage(response, "-1", "您选择的组合已删除");
				}else if(i==-4){
					setPromptMessage(response, "-1", "提交的填报科目个数错误");
				}else if(i==-5){ 
					setPromptMessage(response, "-1", "补选已开始，请刷新后重新提交");
				}else if(i==-6){
					setPromptMessage(response, "-1", "您不能修改填报的科目组合");
				}else if(i==-7){
					setPromptMessage(response, "-1", "补选还未开始，请刷新后重新提交");
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
		 *(6)按科目查看结果
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getStaticListBySubject", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getStaticListBySubject(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String subjectId = request.getString("subjectId");
				String termInfo = request.getString("termInfo");
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4,5);
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("subjectId", subjectId);
				param.put("wfId", wfId);
				param.put("termInfoId", termInfoId);
				param.put("schoolYear", schoolYear);
				param.put("termInfo", termInfo);
				List<JSONObject> data = wishFillingService.getStaticListBySubject(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 *(7)按组合查看结果
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getStaticListByZh", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getStaticListByZh(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4, 5);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				String classId = request.getString("classId");
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfoId", termInfoId);
				param.put("schoolYear", schoolYear);
				param.put("termInfo", termInfo);
				param.put("classId", classId);
				List<JSONObject> data = wishFillingService.getStaticListByZh(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 *(8)按组合查看-查看组合名单
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getStudentZh", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getStudentZh(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String zhId = request.getString("zhId");
				String classId = request.getString("classId");
				String termInfo = request.getString("termInfo");
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4, 5);
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("zhId", zhId);
				param.put("classId", classId);
				param.put("termInfoId", termInfoId);
				param.put("schoolYear", schoolYear);
				param.put("termInfo", termInfo);
				
				List<JSONObject> data = wishFillingService.getStudentZh(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 *(9)按组合查看-批量导出名单
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/exportStudentZh", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject exportStudentZh(HttpServletRequest req,
				 HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = req.getParameter("wfId");
				String termInfo = req.getParameter("termInfo");
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4, 5);
				String schoolId = getXxdm(req);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfoId", termInfoId);
				param.put("schoolYear", schoolYear);
				param.put("termInfo", termInfo);
				JSONArray excelData = wishFillingService.exportStudentZh(param);
				JSONArray excelHeads = new JSONArray();
				JSONArray line = new JSONArray();
				JSONObject col = new JSONObject();
				col.put("field", "zhName");
				col.put("title", "所选组合");
				line.add(col);
				
				col = new JSONObject();
				col.put("field", "accountName");
				col.put("title", "姓名");
				line.add(col);
				
				col = new JSONObject();
				col.put("field", "className");
				col.put("title", "所属行政班级");
				line.add(col);
				
				excelHeads.add(line);
				
				ExcelTool.exportExcelWithData(excelData, excelHeads, "选科导出学生组合查看结果", null, req,res);
				setPromptMessage(response, "0", "操作成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		
	
}
