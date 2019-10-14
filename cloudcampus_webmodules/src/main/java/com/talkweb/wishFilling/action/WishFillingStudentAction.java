package com.talkweb.wishFilling.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.service.WishFillingService;
import com.talkweb.wishFilling.service.WishFillingStudentService;
import com.talkweb.wishFilling.util.Util;


/** 
 * 新高考志愿填报-学生填报action
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return 
 * @version 2.0 2016年11月3日  author：zhh 
 */
@Controller
@RequestMapping(value = "/wishFilling/student/")
public class WishFillingStudentAction extends BaseAction{
	 @Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private WishFillingStudentService wishFillingStudentService;
	    @Autowired
	    private WishFillingService wishFillingService;
	    @Autowired
	    private ScoreManageService scoreManageService;
	    @Autowired
		private WishFillingSetDao wishFillingSetDao;
	    
		private static final Logger logger = LoggerFactory.getLogger(WishFillingStudentAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
/**
 * 五、学生填报模块
 */
		/**
		 * （1）获取待选课程
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
			JSONObject param = new JSONObject();
			JSONObject returnObj = new JSONObject();
			try{
				String wfId = request.getString("wfId");
				String schoolId = getXxdm(req);
				String termInfo = request.getString("termInfo");
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4,5);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				param.put("schoolYear", schoolYear);
				param.put("termInfoId", termInfoId);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				HttpSession sess = req.getSession();
				User user=(User)(sess.getAttribute("user"));
				
				if(user.getUserPart().getRole().equals(T_Role.Parent))
				{
					user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
				}
				//User user=allCommonDataService.getUserById(Long.valueOf(schoolId), 1001409588L);
				param.put("accountId", user.getAccountPart().getId());
				Long classId=user.getStudentPart().getClassId();
				param.put("classId", classId);
				List<Long> ids = new ArrayList<Long>();
				ids.add(classId);
				List<Classroom> cList = allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), ids, termInfo);
				if(cList!=null && cList.size()>0){
					Classroom c = cList.get(0);
					Grade g = allCommonDataService.getGradeById(Long.parseLong(schoolId), c.getGradeId(), termInfo);
				    if(g.isGraduate){
				    	response.put("data", returnObj);
				    	setPromptMessage(response, "0", "查询成功");
				    	return response;
				    }
					String useGrade = allCommonDataService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", schoolYear);
					param.put("useGrade", useGrade);
					param.put("isJudge", 1);
					returnObj = wishFillingStudentService.getStudentTb(param);
					response.put("data", returnObj);
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
		 * （2）提交填报
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/addStudentTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addStudentTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			List<JSONObject> data = new ArrayList<JSONObject>();
			try {
				String schoolId = getXxdm(req);
				String wfId = request.getString("wfId");
				String zhId = request.getString("zhId");
				String termInfo = request.getString("termInfo"); 
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4,5);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				JSONArray s = request.getJSONArray("subjectIds");
				List<Long> subList= new ArrayList<Long>();
				if(s!=null){
					for(int i=0;i<s.size();i++){
						String ss=(String) s.get(i);
						subList.add(Long.parseLong(ss));
					}
				}
				
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfo", termInfo);
				param.put("zhId", zhId);
				param.put("termInfoId", termInfoId);
				param.put("schoolYear", schoolYear);
				param.put("subList", subList);
				HttpSession sess = req.getSession();
				User user=(User)(sess.getAttribute("user"));
				
				if(user.getUserPart().getRole().equals(T_Role.Parent))
				{
					user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
				}
				//User user=allCommonDataService.getUserById(Long.valueOf(schoolId), 1001409664L);
				param.put("accountId", user.getAccountPart().getId());
				Long classId=user.getStudentPart().getClassId();
				param.put("classId", classId);
				List<Long> ids = new ArrayList<Long>();
				ids.add(classId);
				List<Classroom> cList = allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), ids, termInfo);
				if(cList!=null && cList.size()>0){
					Classroom c = cList.get(0);
					Grade g = allCommonDataService.getGradeById(Long.parseLong(schoolId), c.getGradeId(), termInfo);
				    String useGrade = allCommonDataService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", schoolYear);
					param.put("useGrade", useGrade);
				    int i = wishFillingStudentService.addStudentTb(param);
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
				}else{
					setPromptMessage(response, "-1", "当前登录用户的班级信息为空");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 *(3)获取蜘网图所需参数
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getSpiderChart", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getSpiderChart(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String schoolId = getXxdm(req);
				String termInfo = getCurXnxq(req);
				HttpSession sess = req.getSession();
				User user=(User)(sess.getAttribute("user"));
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfo", termInfo);
				JSONObject tbObj = wishFillingService.getTb(param);
				String subjectIds = tbObj.getString("subjectIds");
				String isByElection = tbObj.getString("isByElection");
				String wfWay = tbObj.getString("wfWay");
				String pycc = tbObj.getString("pycc");
				/*if("0".equals(wfWay)&&"1".equals(isByElection)){ //查询补选科目
					JSONObject tbObj1 = wishFillingService.getTb(param);
					subjectIds = tbObj1.getString("subjectIds");
				}*/
				if(user.getUserPart().getRole().equals(T_Role.Parent))
				{
					user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
				}
				//User user=allCommonDataService.getUserById(Long.valueOf(schoolId), 1001411924L);
				Long accountId = user.getAccountPart().getId();
				param.put("accountId", accountId );
				
				List<Long> fakeSubList = StringUtil.toListFromString(subjectIds);
				//转换成divideSubject
				Map<String,List<JSONObject>> subIdObjMap = new HashMap<String,List<JSONObject>>();
				List<Long> subList =new ArrayList<Long>(fakeSubList); 
				if("3".equals(pycc)){
					subList.clear();
					param.put("pycc", pycc);
					List<JSONObject> subjectDividedList =wishFillingSetDao.getDividedSubjectList(param);
					param.remove("pycc");
					for(JSONObject subDiv:subjectDividedList){
						List<JSONObject> sub = new ArrayList<JSONObject>();
						if(subIdObjMap.containsKey(subDiv.getString("ssubjectId"))){
							sub = subIdObjMap.get(subDiv.getString("ssubjectId"));
						}
						sub.add(subDiv);
						subIdObjMap.put(subDiv.getString("ssubjectId"), sub);
					}
					for(int i=0;i<fakeSubList.size();i++){
						long subId = fakeSubList.get(i);
						if(subIdObjMap.containsKey(subId+"")){
							List<JSONObject> subs = subIdObjMap.get(subId+"");
							if(subs!=null){
								for(JSONObject sub:subs){
									subList.add(sub.getLong("subjectId"));
								}
							}
						}else{
							subList.add(subId);
						}
					}
				}
				List<JSONObject> lList = wishFillingService.getDicSubjectList(schoolId,areaCode,tbObj.getString("pycc"),"1");
				Map<Long,JSONObject> lMap = new HashMap<Long,JSONObject>();
				if(lList!=null){
					for(JSONObject l:lList){
						lMap.put(l.getLong("subjectId"), l);
					}
				}
				HashMap<String, Object> map = new HashMap<String,Object>();
				map.put("schoolId", schoolId);
				map.put("studentId", accountId);
				map.put("termInfoId", termInfo);
				Map<String, List<JSONObject>> returnMap = scoreManageService.getScoreStuBZF(map);
				//科目分值=A/N
				//A：标准分A分形式=基准分 +扩大系数*标准分
				//N：学生考试个数
				int N = returnMap.size(); //获取考试次数
				//subjectId-A
				Map<Long,Float> subMap = new HashMap<Long,Float>();
				for (Map.Entry<String, List<JSONObject>> entry : returnMap.entrySet()) 
				{
					List<JSONObject> list = entry.getValue();
					for(JSONObject obj:list){
						Long kmdm = obj.getLong("kmdm");
						if(!subList.contains(kmdm)){continue;}
						Float bzf = obj.getFloat("bzf");
						if(bzf==null ||kmdm==null){continue;}
						Float B = 80+5*bzf;
						if(B>100){B=100.0F;}
						if(subMap.containsKey(kmdm)){
							Float A = subMap.get(kmdm);
							A += B;
							subMap.put(kmdm, A);
						}else{
							Float A = B;
							subMap.put(kmdm, A);
						}
					}
				}
				List<JSONObject> subjects = new ArrayList<JSONObject>();
				int kmNumber = subMap.size();
				Float kmAverageScore = 0F;
				for (Map.Entry<Long, Float> entry : subMap.entrySet()) 
				{
					Long subjectId = entry.getKey();
					Float score = entry.getValue();
					Float s = score/N;
					kmAverageScore+=s;
					JSONObject obj = new JSONObject();
					obj.put("subjectId",subjectId);
					obj.put("score",Float.parseFloat(StringUtil.formatNumber(s,2)));
					JSONObject l = lMap.get(subjectId);
					if(l!=null){
						obj.put("subjectName", l.getString("subjectName"));
					}
					subjects.add(obj);
				}
				data.put("subjects", subjects);
				Float averageMax = kmAverageScore/kmNumber+5;
				Float averageMin = kmAverageScore/kmNumber-5;
				data.put("averageMax", averageMax);
				data.put("averageMin", averageMin);
				response.put("data", data);
				setPromptMessage(response, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * （4）获取所有的选课列表(跨学年学期)
		 * 根据登录人所在的年级倒推至高一，查询所有这些年份创建的选课轮次，看该登录人是否有提交数据
		 * 时间未开放的不列出来
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getTbSelectList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getTbSelectList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
					String schoolId = getXxdm(req);
					String termInfo = getCurXnxq(req);
					param.put("schoolId", schoolId);
					param.put("termInfo", termInfo);
					School school = getSchool(req, termInfo);
					String areaCode = school.getAreaCode()+"";
					param.put("areaCode", areaCode);
					String schoolYear = termInfo.substring(0, 4);
					HttpSession sess = req.getSession();
					User user=(User)(sess.getAttribute("user"));
					
					if(user.getUserPart().getRole().equals(T_Role.Parent))
					{
						user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
					}
					
					//User user=allCommonDataService.getUserById(Long.valueOf(schoolId), 1001409588L);
					param.put("accountId", user.getAccountPart().getId());
					Long classId=user.getStudentPart().getClassId();
					param.put("classId", classId);
					List<Long> ids = new ArrayList<Long>();
					ids.add(classId);
					List<Classroom> cList = allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), ids, termInfo);
					if(cList!=null && cList.size()>0){
						Classroom c = cList.get(0);
						Grade g = allCommonDataService.getGradeById(Long.parseLong(schoolId), c.getGradeId(), termInfo);
						if(g.isGraduate){
					    	response.put("data", null);
					    	setPromptMessage(response, "0", "查询成功");
					    	response.put("data", new ArrayList<>());
					    	return response;
					    }
					    //当前年级与高一年级的时间间隔
						int interval = 0;
						if("3".equals(Util.getPycc(g.getCurrentLevel().getValue()))){
							 interval=g.getCurrentLevel().getValue()-T_GradeLevel.T_JuniorOne.getValue();
						}else{
							 interval=g.getCurrentLevel().getValue()-T_GradeLevel.T_HighOne.getValue();
						}
						String minTermInfo = (Integer.parseInt(termInfo.substring(0,4))-interval)+"1";
					    String useGrade = allCommonDataService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", schoolYear);
						param.put("wfUseGrade", useGrade);
						param.put("minTermInfo", minTermInfo);
					}
					List<JSONObject> list = wishFillingStudentService.getTbSelectList(param);
					if(list!=null){
						response.put("data", list);
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
}
 