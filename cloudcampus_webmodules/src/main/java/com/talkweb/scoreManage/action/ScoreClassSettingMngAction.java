package com.talkweb.scoreManage.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.scoreManage.service.ScoreManageService;

/**
 * @ClassName: ScoreManageAction.java
 * @version:1.0
 * @Description: 成绩分析设置：文理分组、班级分组、学生统计设置
 * @author 武洋 ---智慧校
 * @date 2015年3月25日
 */
@Controller
@RequestMapping(value = "/scoremanage1/setting/")
public class ScoreClassSettingMngAction extends BaseAction {
	@Autowired
	private ScoreManageService scoreService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	/**
	 * 成绩分析--设置状态与默认设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */

	@RequestMapping(value = "getExamSettingConfig", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamAnalysisSettingConfig(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		JSONObject data = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String kslcdm = request.getString("examId");
			if (StringUtils.isBlank("xnxq") || StringUtils.isBlank(kslcdm)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			String xxdm = getXxdm(req);

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", xxdm);
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);

			data = scoreService.updateInitAndGetExamAnalysisSettingConfig(params);
			int isAdmin = 0;
			if (isMoudleManager(req, "cs1002")) {
				isAdmin = 1;
			}
			data.put("isAdmin", isAdmin);
			data.put("msg", "");
			
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			data.put("msg", e.getMessage());
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			data.put("msg", "服务器异常，请联系管理员！");
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		response.put("data", data);
		return response;
	}

	/**
	 * 成绩设置--文理科设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "getASGList", method = RequestMethod.POST)
	@ResponseBody
	public Object getASGList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res)
			throws Exception {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId").trim();
			String kslcdm = request.getString("examId").trim();
			String synj = request.getString("usedGrade").trim();
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslcdm) || StringUtils.isBlank(synj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("nj", synj);
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			// 得到班级和班级类型
			response.put("data", scoreService.getASGList(params));
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response.get("data");
	}

	/**
	 * 成绩设置--保存文理科设置 需调用接口获取班级类型字典
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "saveASG", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveASG(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslcdm = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGrade");
			String classes = request.getString("classes");
			if (StringUtils.isBlank(kslcdm) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)
					|| StringUtils.isBlank(classes)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);
			params.put("xnxq", xnxq);
			params.put("classes", JSONArray.parse(request.getString("classes")));
			params.put("accessToken", req.getSession().getAttribute("accessToken"));
			scoreService.saveASG(params);

			setResponse(response, 0, "保存成功");
			response.getJSONObject("data").put("msg", "保存成功");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 成绩设置--获取班级分组列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getClassGroupList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassGroupList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId").trim();
			String kslc = request.getString("examId").trim();
			String bmfz = request.getString("asgId").trim();
			String nj = request.getString("usedGrade").trim();
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslc) || StringUtils.isBlank(bmfz)
					|| StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			params.put("bmfz", bmfz);
			
			JSONObject data = scoreService.getClassGroupList(params);
			response.putAll(data);

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 成绩设置--新增班级分组
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "addClassGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addClassGroup(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGrade");
			String kslc = request.getString("examId");
			String ssfz = request.getString("asgId");
			String fzmc = request.getString("classGroupName");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslc) || StringUtils.isBlank(nj)
					|| StringUtils.isBlank(fzmc) || StringUtils.isBlank(ssfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			List<String> classIds = StringUtil.convertToListFromStr(request.getString("classIds"), ",", String.class);
			if(classIds.size() == 0) {
				throw new CommonRunException(-1, "班级为空，请选择班级");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("ssfz", ssfz);
			params.put("fzmc", fzmc);
			scoreService.addClassGroup(params, classIds);
			
			setResponse(response, 0, "保存成功");
			response.getJSONObject("data").put("msg", "保存成功");
		}catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		}catch(Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
	

		return response;
	}

	/**
	 * 设置统计人数（按文理科分组设置）--删除班级分组
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "delClassGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delClassGroup(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xnxq = request.getString("termInfoId");
			String classGroupId = request.getString("classGroupId");
			String kslc = request.getString("examId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(classGroupId) || StringUtils.isBlank(kslc)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("bmfz", classGroupId);
			params.put("xnxq", xnxq);
			
			scoreService.delClassGroup(params);
			
			setResponse(response, 0, "删除成功！");
			response.getJSONObject("data").put("msg", "删除成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 获取统计人数默认选中的显示卡
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getStaticSelectedSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStaticSelectedSetting(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String examId = request.getString("examId");
			String usedGrade = request.getString("useGrade");
			String termInfoId = request.getString("termInfoId");
			if(StringUtils.isBlank(examId) || StringUtils.isBlank(usedGrade) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			JSONObject data = response.getJSONObject("data");
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslc", examId);
			params.put("kslcdm", examId);
			params.put("nj", usedGrade);
			params.put("xnxq", termInfoId);
			int way = scoreService.getStaticSelectedSetting(params);

			data.put("way", way);
			data.put("t", "");
			setResponse(response, 0, "成功！");
			response.getJSONObject("data").put("msg", "成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 设置统计人数（按文理科分组设置）--获取文理分组
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getASGListForStatic", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getASGListForStatic(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xnxq = request.getString("termInfoId");
			String synj = request.getString("usedGrade");
			String kslcdm = request.getString("examId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslcdm) || StringUtils.isBlank(synj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xnxq", xnxq);
			params.put("nj", synj);
			
			response.putAll(scoreService.getASGListForStatic(params));
			
			setResponse(response, 0, "");
			response.getJSONObject("data").put("msg", "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 成绩设置--保存按文理科设置的统计人数规则
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateASGStatRules", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateASGStatRules(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String staticNumGroup = request.getString("staticNumGroup");
			String examId = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if(StringUtils.isBlank(examId) || StringUtils.isBlank(staticNumGroup) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			JSONArray staticNumGroups = JSON.parseArray(staticNumGroup);
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", examId);
			params.put("kslc", examId);
			params.put("xnxq", termInfoId);
			params.put("staticNumGroups", staticNumGroups);
			
			scoreService.updateASGStatRules(params);
			
			setResponse(response, 0, "保存成功！");
			response.getJSONObject("data").put("msg", "保存成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 设置统计人数（按班级设置）
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getClassGroupListStatic", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassGroupListStatic(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xnxq = request.getString("termInfoId");
			String synj = request.getString("usedGrade");
			String kslcdm = request.getString("examId");
			String bmfz = request.getString("asgId");
			if(StringUtils.isBlank(bmfz) || StringUtils.isBlank(kslcdm) || StringUtils.isBlank(synj) 
					|| StringUtils.isBlank(xnxq)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xnxq", xnxq);
			params.put("bmfz", bmfz);
			params.put("nj", synj);
			
			response.putAll(scoreService.getClassGroupListStatic(params));
			
			setResponse(response, 0, "");
			response.getJSONObject("data").put("msg", "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		
		return response;
	}

	/**
	 * 成绩设置--保存班级设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateClassGroupStatic", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateClassGroupStatic(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String classGroups = request.getString("classGroups");
			String kslc = request.getString("examId");
			String bmfz = request.getString("asgId");
			String termInfoId = request.getString("termInfoId");
			if(StringUtils.isBlank(classGroups) || StringUtils.isBlank(kslc) || StringUtils.isBlank(bmfz)
					|| StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONArray bjfz = JSON.parseArray(classGroups);
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("bmfz", bmfz);
			params.put("bjfz", bjfz);
			scoreService.updateClassGroupStatic(params);
			
			setResponse(response, 0, "保存成功！");
			response.getJSONObject("data").put("msg", "保存成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		
		return response;
	}

	/**
	 * 成绩设置-- 获取参与/不参与统计学生列表，待确定深圳接口getaccountbatch是否返回对应的users对象
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getStatStuList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStatStuList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xnxq = request.getString("termInfoId");
			String synj = request.getString("usedGrade");
			String kslc = request.getString("examId");
			String bh = request.getString("classId").trim();
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(synj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(bh)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			String xhxm = request.getString("stdNumOrName");
			if (xhxm == null || xhxm.trim().length() == 0) {
				xhxm = null;
			}
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("xnxq", xnxq);
			params.put("nj", synj);
			params.put("bh", bh);
			params.put("xhxm", xhxm);
			response.putAll(scoreService.getStatStuList(params));
			
			setResponse(response, 0, "");
			response.getJSONObject("data").put("msg", "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 成绩设置--保存不参与统计学生设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "addStatStuList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addStatStuList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslc = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if(StringUtils.isBlank(kslc) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", termInfoId);
			
			List<String> studentIdList = StringUtil.convertToListFromStr(request.getString("studentIds"), ",", String.class);
			scoreService.addStatStuList(params, studentIdList);

			setResponse(response, 0, "保存成功！");
			response.getJSONObject("data").put("msg", "保存成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		
		return response;
	}

	/**
	 * 成绩设置--删除不参与统计学生设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "delStatStuList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delStatStuList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslc = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if(StringUtils.isBlank(kslc) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", termInfoId);
			
			List<String> studentIdList = StringUtil.convertToListFromStr(request.getString("studentIds"), ",", String.class);
			if(studentIdList != null && studentIdList.size()>0) {
				params.put("xhList", studentIdList);
			}
			scoreService.delStatStuList(params);
			
			setResponse(response, 0, "保存成功！");
			response.getJSONObject("data").put("msg", "保存成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
}
