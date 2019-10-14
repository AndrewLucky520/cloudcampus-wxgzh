package com.talkweb.scoreManage.action;

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.service.ScoreReportService;

/** 
* @author  Administrator
* @version 创建时间：2018年7月17日 下午7:03:35 
*  
*/

@RequestMapping("/scoreReport1")
@Controller
public class ScoreReportInterfaceAction  extends BaseAction {
	
	@Autowired
	private ScoreReportService reportService;
 
	@Value("#{settings['firstTermInfoId']}")
	 private String firstTermInfoId;// 
	
	ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	
	@Autowired
	private AllCommonDataService allCommonDataService ;
	
	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}
	
	
	@RequestMapping(value = "teacher/scoreAnalysis", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherScoreAnalysis(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String statisTypeId = request.getString("statisTypeId");
			String classId = request.getString("classId");
			String termInfoId = rbConstant.getString("currentTermInfo"); 
			Long schoolId =request.getLong("schoolId"); 
			Long userId = request.getLong("userId");
			
			System.out.println( "schoolId ==> " + schoolId );
			User user = allCommonDataService.getUserById(schoolId, userId);
			Long accountId =  user.getAccountPart().getId();
			System.out.println( "userId ==============================================> " + userId );
			System.out.println( "accountId ==============================================> " + accountId );
			if (StringUtils.isBlank(statisTypeId) ||  StringUtils.isBlank(classId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("xxdm", schoolId);//
			params.put("classId", classId);
			params.put("xnxq", termInfoId);//学年学期
			// 统计类别编号1:总平均分排名; 02:全A人数; 03:次A1B人数; 04:合格率; 05:优秀率
			params.put("statisTypeId", statisTypeId);
			params.put("accountId", accountId);
			response.put("data", reportService.getTeacherScoreAnalysis(params));
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	
	@RequestMapping(value = "student/scoreAnalysis", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentScoreAnalysis(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
		 
			String userId = request.getString("userId");
			if (StringUtils.isBlank(userId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			String termInfoId = rbConstant.getString("currentTermInfo"); 
			String schoolId = request.getString("schoolId");
			String classId =  request.getString("classId");
			String studentAccountId = request.getString("studentAccountId");
			User user = allCommonDataService.getUserById(Long.parseLong(schoolId), Long.parseLong(userId) );

			Long stuAccountId =  user.getAccountPart().getId();
			JSONObject params = new JSONObject();
			params.put("xxdm", schoolId);
			params.put("classId", classId);
			params.put("xnxq", termInfoId);
			if (StringUtils.isBlank(studentAccountId)) {
				params.put("studentId", stuAccountId);
			}else {
				params.put("studentId", studentAccountId);
			}
			
			response.put("data", reportService.getStudentScoreAnalysis(params));
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
 
}
