package com.talkweb.wishFilling.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.scoreManage.service.ScoreReportService;

/*
 * 个人中心-学科分析 与第三方对接的Action 
 * @zhanghuihui15222@talkweb.com.cn
 */
@RequestMapping("/lessonAnalysis/") // Auth 单点登录web.xml url 过滤
@Controller
public class WishFillingOtherAction extends BaseAction{
    @Autowired
    private ScoreManageService scoreManageService;
	@Autowired
	private AllCommonDataService allCommonDataService;
	@Autowired
	private ScoreReportService reportService;
	
	ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	 
	@RequestMapping(value = "getStudentSpiderChart", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentSpiderChart(@RequestBody JSONObject request, HttpServletRequest req ,HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject data = new JSONObject();
		try {
			long schoolId = request.getLongValue("schoolId"); //820000000316L;
			String accountId = request.getString("studentAccountId"); //"100000165366";
			String termInfoId = rbConstant.getString("currentTermInfo"); 
			HashMap<String, Object> map = new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("studentId", accountId);
			map.put("termInfoId",termInfoId );
			Log.info("[wishFilling]getStudentSpiderChart getScoreStuBZF:"+map.toString());
			Map<String, List<JSONObject>> returnMap = scoreManageService.getScoreStuBZF(map);
			List<Long> subjectList = scoreManageService.getSynthscoreSubject(map);
			
			//获取所有科目列表
			Map<String,String> lMap = new HashMap<String,String>();
			School school = allCommonDataService.getSchoolById(schoolId, termInfoId);
			List<LessonInfo> lList = allCommonDataService.getLessonInfoList(school, termInfoId);
			for(LessonInfo l:lList){
				if(l.getId()!=0&& StringUtils.isNotBlank(l.getName())){
					lMap.put(l.getId()+"", l.getName());
				}
			}
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
				if (subjectList!=null && subjectList.contains(subjectId)) {
					continue;
				}
				Float score = entry.getValue();
				Float s = score/N;
				kmAverageScore+=s;
				JSONObject obj = new JSONObject();
				obj.put("subjectId",subjectId);
				obj.put("score",Float.parseFloat(StringUtil.formatNumber(s,2)));
				String subjectName = lMap.get(subjectId+"");
				if(StringUtils.isNotBlank(subjectName)){
					obj.put("subjectName", subjectName);
				}
				subjects.add(obj);
			}
			if(subjects!=null && subjects.size()>0){
				data.put("subjects", subjects);
				Float averageMax = kmAverageScore/kmNumber+5;
				Float averageMin = kmAverageScore/kmNumber-5;
				data.put("averageMax", averageMax);
				data.put("averageMin", averageMin);
				response.put("data", data);
			}
			setPromptMessage(response, "0", "查询成功");
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
	  	
	}
	
	public static void main(String[] args) {
 
	}
	
	
	@RequestMapping(value = "getClassSpiderChart", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassSpiderChart(@RequestBody JSONObject request, HttpServletRequest req ,HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject data = new JSONObject();
		String termInfoId = rbConstant.getString("currentTermInfo");
		String schoolId = request.getString("schoolId"); //"820000000316";
		String classId =  request.getString("classId"); //"100000002173";
		JSONObject param = new JSONObject();
		param.put("schoolId",schoolId );
		param.put("classId",classId );
		param.put("termInfoId" ,termInfoId );
		//param.put("firstTermInfo" ,rbConstant.getString("firstTermInfoId") );
		Log.info("[wishFilling]getClassSpiderChart getAllHisSubjectfullScore getAllHisSubjectAverageScore:"+param.toString());
		long now1 = new Date().getTime();
		Map<String,List<JSONObject>> fullScoreMap = reportService.getAllHisSubjectfullScore(param) ;
		long now2 = new Date().getTime();
		System.out.println("now1 - now2 =========" +  (now2 - now1)  );
		Map<String,List<JSONObject>> averScoreMap = reportService.getAllHisSubjectAverageScore(param);
		long now3 = new Date().getTime();
		System.out.println("now3 - now2 =========" +  (now3 - now2)  );
		
		int N = averScoreMap.size(); //获取考试次数
		
 
		
		//获取所有科目列表
		Map<String,String> lMap = new HashMap<String,String>();
		School school = allCommonDataService.getSchoolById(Long.parseLong(schoolId), termInfoId);
		List<LessonInfo> lList = allCommonDataService.getLessonInfoList(school, termInfoId);
 
	 
		Log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@wishFilling:lList"+lList.toString());
		for(LessonInfo l:lList){
			if(l.getId()!=0&& StringUtils.isNotBlank(l.getName())){
				lMap.put(l.getId()+"", l.getName());
			}
		}
		
 
		Map<Long,HashMap<String,Float>> fullScoreSubMap = new HashMap<Long,HashMap<String,Float>>();
		for (Map.Entry<String, List<JSONObject>> entry : fullScoreMap.entrySet()) 
		{
			List<JSONObject> list = entry.getValue();
			for(JSONObject obj:list){
				Long kmdm = obj.getLong("kmdm");
				if(!lMap.containsKey(kmdm+"")){
					continue;
				}
				String kslc = obj.getString("kslc");
				Float mf = obj.getFloat("mf");
				HashMap<String,Float> mfMap = new HashMap<String,Float>();
				if(fullScoreSubMap.containsKey(kmdm)){
					mfMap = fullScoreSubMap.get(kmdm);
				}
				mfMap.put(kslc, mf);
				fullScoreSubMap.put(kmdm, mfMap);
			}
		}
		
		Map<Long,Float> subMap = new HashMap<Long,Float>();
		for (Map.Entry<String, List<JSONObject>> entry : averScoreMap.entrySet()) 
		{
			List<JSONObject> list = entry.getValue();
			for(JSONObject obj:list){
				Long kmdm = obj.getLong("kmdm");
				if(!lMap.containsKey(kmdm+"")){
					continue;
				}
				String kslc = obj.getString("kslc");
				Float pjf = obj.getFloat("pjf");
				
				HashMap<String,Float> mfMap = fullScoreSubMap.get(kmdm);
				if(mfMap==null){
					continue;
				}
				Float mf = mfMap.get(kslc);
				if(mf==null){
					continue;
				}
				Float X = pjf /mf ;
				Float B = 20+80*X;
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
			String subjectName = lMap.get(subjectId+"");
			if(StringUtils.isNotBlank(subjectName)){
				obj.put("subjectName", subjectName);
			}
			subjects.add(obj);
		}
		if(subjects!=null && subjects.size()>0){
			data.put("subjects", subjects);
			Float averageMax = kmAverageScore/kmNumber+5;
			Float averageMin = kmAverageScore/kmNumber-5;
			data.put("averageMax", averageMax);
			data.put("averageMin", averageMin);
			response.put("data", data);
		}
		setPromptMessage(response, "0", "查询成功");
		
	 
		
		return response;
	}
	
	
	
	
	
	
	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
 

}
