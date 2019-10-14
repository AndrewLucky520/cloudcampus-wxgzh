package com.talkweb.scoreManage.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.CsCurCommonDataService;
import com.talkweb.scoreManage.po.ce.ClassExamInfo;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.AppScoreReportService;
import com.talkweb.scoreManage.service.ClassScoreCrudService;
import com.talkweb.scoreManage.service.ScoreManageService;

import io.jsonwebtoken.lang.Collections;

@RequestMapping("/scoreRport1/app/")
@Controller
public class AppScoreReportAction extends BaseAction {

	@Autowired
	private AppScoreReportService appService;
	
	@Autowired
	private ScoreManageService scoreManageService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private CsCurCommonDataService curCommonService;
	
	@Autowired
	private ClassScoreCrudService classScoreCrudService;
	
	private static final Logger logger = LoggerFactory.getLogger(AppScoreReportAction.class);


	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	@RequestMapping(value = "getExamList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getExamList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			Long userId = request.getLong("userId");// 用户编号
			Long schoolId = request.getLong("schoolId");// 学校编号
			if (userId == null || schoolId == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = appService.getStudentInfo(null, schoolId, userId, 1);
			params.put("xxdm", String.valueOf(schoolId));

			response.put("data", appService.getExamList(params));

			setResponse(response, 0, "成功");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "getSchoolExamStudentScoreReport", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSchoolExamStudentScoreReport(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			Long userId = request.getLong("userId");// 用户编号
			Long schoolId = request.getLong("schoolId");// 学校编号
			String examId = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if (userId == null || schoolId == null || StringUtils.isBlank(examId) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			
			JSONObject params = appService.getStudentInfo(termInfoId, schoolId, userId, 1);
			params.put("kslc", examId);
			params.put("kslcdm", examId);
			params.put("xnxq", termInfoId);
			
			DegreeInfo degreeInfo = scoreManageService.getDegreeInfoById(params);
//			logger.info("degreeInfo:"+JSONObject.toJSONString(degreeInfo));
			
			if("0".equals(degreeInfo.getFbflag())) {
				setResponse(response, 4, "该数据已失效!");
				return response;
			}
			
			response.putAll(appService.getSchoolExamStudentScoreReport(params));
			
			params.put("kslc", examId);
			params.put("kslcdm", examId);
			params.put("xnxq", termInfoId);
			appService.insertExamViewByExamType(1, params);
			response.remove("autoIncr");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "getClassExamStudentScoreReport", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassExamStudentScoreReport(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			Long userId = request.getLong("userId");// 用户编号
			Long schoolId = request.getLong("schoolId");// 学校编号
			String examId = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if (userId == null || schoolId == null || StringUtils.isBlank(examId) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = appService.getStudentInfo(termInfoId, schoolId, userId, 2);
			params.put("examId", examId);
			params.put("termInfoId", termInfoId);
			response.putAll(appService.getClassExamStudentScoreReport(params));
			
			params.put("examId", examId);
			params.put("termInfoId", termInfoId);
			appService.insertExamViewByExamType(2, params);
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 自定义考试成绩报告(学生)
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getCustomExamStudentScoreReport", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCustomExamStudentScoreReport(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			Long userId = request.getLong("userId");// 用户编号
			Long schoolId = request.getLong("schoolId");// 学校编号
			String examId = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if (userId == null || schoolId == null || StringUtils.isBlank(examId) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = appService.getStudentInfo(termInfoId, schoolId, userId, 3);
			params.put("examId", examId);
			params.put("termInfoId", termInfoId);
			response.putAll(appService.getCustomExamStudentScoreReport(params));
			
			params.put("examId", examId);
			params.put("termInfoId", termInfoId);
			appService.insertExamViewByExamType(3, params);

		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * App班级报告（老师用户）学生考试列表
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getClassReportExamList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassReportExamList(@RequestBody JSONObject request, HttpServletRequest req) {
//		logger.info("getClassReportExamLis-params: "+JSONObject.toJSONString(request));
		JSONObject response = new JSONObject();
		try {
			Long userId = request.getLong("userId");// 用户编号
			Long schoolId = request.getLong("schoolId");// 学校编号
			if (userId == null || schoolId == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = appService.getTeacherInfo(null, schoolId, userId, 1);
			response.putAll(appService.getClassReportExamList(params));

		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 校内考试班级成绩报告(老师)
	 * 
	 * @param param
	 * @param request
	 * @return
	 */

	@RequestMapping(value = "getSchoolExamClassScoreReport", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSchoolExamClassScoreReport(@RequestBody JSONObject request, HttpServletRequest req) {
		logger.info("getSchoolExamClassScoreReport:"+request);
		JSONObject response = new JSONObject();
		try {
			String classId = request.getString("classId");// 用户编号
			String schoolId = request.getString("schoolId");// 学校编号
			String examId = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if (StringUtils.isBlank(classId) || StringUtils.isBlank(schoolId) || StringUtils.isBlank(examId) 
					|| StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xxdm", schoolId);
			params.put("bh", classId);
			params.put("xnxq", termInfoId);
			params.put("kslcdm", examId);
			params.put("kslc", examId);
			
			DegreeInfo degreeeInfo = scoreManageService.getDegreeInfoById(params);
			
			if("0".equals(degreeeInfo.getFbteaflag())){
				setResponse(response, 4, "该成绩已失效");
				return response;
			}
			response.put("data", appService.getSchoolExamClassScoreReport(params));

			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 班级家长查阅成绩名单(老师)
	 * 
	 * @param param
	 * @param request
	 * @return
	 */

	@RequestMapping(value = "getViewScoreParentList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getViewScoreParentList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String examId = request.getString("examId");
			String schoolId = request.getString("schoolId");// 学校编号
			String classId = request.getString("classId");
			String termInfoId = request.getString("termInfoId");
			Integer examType = request.getInteger("examType");
			if (StringUtils.isBlank(classId) || StringUtils.isBlank(schoolId) || StringUtils.isBlank(examId)
					|| StringUtils.isBlank(termInfoId) || examType == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("examId", examId);
			params.put("kslcdm", examId);
			params.put("kslc", examId);
			params.put("schoolId", schoolId);
			params.put("xxdm", schoolId);
			params.put("classId", classId);
			params.put("bh", classId);
			params.put("termInfoId", termInfoId);
			params.put("xnxq", termInfoId);
			params.put("examType", examType);

			response.put("data", appService.getViewScoreParentList(params));
			
			DegreeInfo degreeInfo = appService.getDegreeInfoById(termInfoId, params);
			if(degreeInfo == null)
				response.put("counter",3);
			else
				response.put("counter",degreeInfo.getCounter());
			
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 4. 班内测试班级成绩报告(老师)
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getClassExamClassScoreReport", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassExamClassScoreReport(@RequestBody JSONObject request, HttpServletRequest req) {

		JSONObject response = new JSONObject();
		try {
			String examId = request.getString("examId");
			String schoolId = request.getString("schoolId");// 学校编号
			String classId = request.getString("classId");
			String termInfoId = request.getString("termInfoId");
			if (StringUtils.isBlank(classId) || StringUtils.isBlank(schoolId) || StringUtils.isBlank(examId)
					|| StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("examId", examId);
			params.put("schoolId", schoolId);
			params.put("classId", classId);
			params.put("termInfoId", termInfoId);

			response.put("data", appService.getClassExamClassScoreReport(params));

			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
	
	
	// 获取最近一次考试信息
	@RequestMapping(value = "gradeExamScore", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject gradeExamScore(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		String classId = request.getString("classId");
		String schoolId = request.getString("schoolId");
		if (classId == null || schoolId == null) {
			throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
		}
		try {
			JSONObject data = appService.getGradeExamScore(request);
			response.put("data", data);
			setResponse(response, 1, "查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, e.getMessage());
		}
		return response;
	}
	
	
	// 获取考试的学生名单
	@RequestMapping(value = "getStudentList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		String classId = request.getString("classId");
		String examId = request.getString("examId");
		String schoolId = request.getString("schoolId");
		String termInfoId  = request.getString("termInfoId");
		if (classId == null || examId == null || schoolId == null || termInfoId ==null) {
			throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
		}
		Classroom classroom = commonDataService.getClassById(Long.parseLong(schoolId), Long.parseLong(classId), termInfoId);
		JSONObject data = appService.getStudentList(request);
		data.put("classId", classId);
		data.put("className", classroom.getClassName());
		response.put("data", data);
		
		JSONObject degreeParam = new JSONObject();
		degreeParam.put("xxdm", schoolId);
		degreeParam.put("kslcdm", examId);
		degreeParam.put("xnxq", termInfoId);
		
		DegreeInfo degreeInfo = appService.getDegreeInfoById(termInfoId, degreeParam);
		logger.info("degreeInfo："+degreeInfo);
		if(degreeInfo != null) {
			response.put("counter", degreeInfo.getCounter());
		}else {
			response.put("counter", 3);
		}
		
		setResponse(response, 1, "查询成功");
		return response;
	}
	
	
	// 获取学生考试信息详情
	@RequestMapping(value = "getExamStudentScoreReport", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamStudentScoreReport(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		String schoolId = request.getString("schoolId");
		String studentId = request.getString("studentId");// 传递来的是userId
		if (schoolId == null || studentId == null ) {
			throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
		}
		try {
			User user = commonDataService.getUserById(Long.parseLong(schoolId), Long.parseLong(studentId));
			if (user!=null) {
				Long classId = user.getStudentPart().getClassId();
				request.put("classId", classId);
			}
			Long lStudentId = user.getUserPart().getAccountId();
			request.put("studentId", lStudentId);
			logger.info("lStudentId" + lStudentId);
			JSONObject data = appService.getExamStudentScoreReport(request);
			response.put("data", data);
			setResponse(response, 1, "查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, e.getMessage());
		}
		return response;
	}
	
	@RequestMapping(value = "sendWxMsg", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject sendWxMsg(@RequestBody JSONObject request, HttpServletRequest req) {
		logger.info("sendWxMsg:"+request);
		
		JSONObject response = new JSONObject();
		try {
			String examId = request.getString("examId");
			String schoolId = request.getString("schoolId");// 学校编号
			String classId = request.getString("classId");
			String termInfoId = request.getString("termInfoId");
			Integer examType = request.getInteger("examType");
			if (StringUtils.isBlank(classId) || StringUtils.isBlank(schoolId) || StringUtils.isBlank(examId)
					|| StringUtils.isBlank(termInfoId) || examType == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("examId", examId);
			params.put("kslcdm", examId);
			params.put("kslc", examId);
			params.put("schoolId", schoolId);
			params.put("xxdm", schoolId);
			params.put("classId", classId);
			params.put("bh", classId);
			params.put("termInfoId", termInfoId);
			params.put("xnxq", termInfoId);
			params.put("examType", examType);

			List<JSONObject> data = appService.getViewScoreParentList(params);
			List<Long> accountIds = new ArrayList();
			if(data != null && data.size()>0) {
//				data.stream().forEach(obj -> accountIds.add(obj.getLong("studentId")));
				for(JSONObject obj : data) {
					if(obj.getInteger("isView") == 0) {
						accountIds.add(obj.getLong("studentId"));
					}
				}
			}
			
			logger.info("data:"+data);
			List<JSONObject> receives = new ArrayList();
			if(accountIds != null && accountIds.size()>0) {
				List<JSONObject> result = curCommonService.getAccountBatch(accountIds, termInfoId);
				logger.info("result："+result);
				for(JSONObject obj : result) {
					JSONObject receive = new JSONObject();
					
					if(obj == null)
						continue;
					
					receive.put("userId", obj.getString("extUserId"));
					receive.put("userName", obj.getString("name"));
					receives.add(receive);
				}
				
				// 家长信息
				result = curCommonService.getParentByStudentIds(accountIds,termInfoId);
				if(!Collections.isEmpty(result)) {
					List<Long> parentUserIds = new ArrayList();
					result.stream().forEach(obj -> parentUserIds.add(obj.getLong("parentId")) );
					
					List<User> parents = curCommonService.getUserBatch(Long.parseLong(schoolId), parentUserIds, termInfoId);
					logger.info("parent："+ JSONObject.toJSONString(parents));
					for(User p : parents) {
						JSONObject receive = new JSONObject();
						
						receive.put("userId", p.getAccountPart().getExtId());
						receive.put("userName",p.getAccountPart().getName());
						
						receives.add(receive);
					}
					
				}
				
				String examTypeName = "";
				DegreeInfo degreeInfo = null;
				if("1".equals(examType)) {
					examTypeName = "校考成绩";
					degreeInfo = appService.getDegreeInfoById(termInfoId, params);
					
					synchronized (this) {
						int counter = degreeInfo.getCounter();
						if(counter <3) {
							try {
								response.put("counter", counter);
								scoreManageService.updatetDegreeInfo(schoolId, examId, termInfoId, counter+1);
							} catch (Exception e) {
								setResponse(response, -1, "counter更新失败！");
								e.printStackTrace();
							}
						}
					}
				}else {
					examTypeName = "班级小考";
					
					Map<String,Object> map = new HashMap<>();
					map.put("examId", examId);
					map.put("schoolId", schoolId);
					map.put("termInfoId", termInfoId);
					ClassExamInfo examInfo = classScoreCrudService.getClassExamInfoById(termInfoId, map);
					if(examInfo != null) {
						degreeInfo = new DegreeInfo();
						degreeInfo.setXxdm(schoolId);
						degreeInfo.setXnxq(termInfoId);
						degreeInfo.setEexamId(examId);
						degreeInfo.setKslcmc(examInfo.getExamName());
					}
					
					synchronized (this) {
						int counter = examInfo.getCounter();
						if(counter <3) {
							try {
								response.put("counter", counter);
								map.put("counter", counter+1);
								classScoreCrudService.updateClassExam(termInfoId, map);
							} catch (Exception e) {
								setResponse(response, -1, "counter更新失败！");
								e.printStackTrace();
							}
						}
					}
				}
				
				logger.info("degreeInfo："+JSONObject.toJSONString(degreeInfo)+",receivesSize:"+JSONObject.toJSONString(receives));
				if(degreeInfo != null) {
					scoreManageService.sendWxMsgLimitCount(degreeInfo, receives, examType, examTypeName, "Parent");
				}
				
				setResponse(response, 1, "查询成功");
			}
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
	
}
