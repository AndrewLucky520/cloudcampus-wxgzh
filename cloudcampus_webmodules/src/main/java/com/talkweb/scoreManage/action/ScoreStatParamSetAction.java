package com.talkweb.scoreManage.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
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
import com.talkweb.scoreManage.po.gm.Zfqjsz;
import com.talkweb.scoreManage.service.ScoreManageService;

/**
 * @ClassName ScoreStaticParamSetAction
 * @author zxy
 * @Desc 成绩分析 -设置统计参数
 * @date 2015年4月13日
 */
@Controller
@RequestMapping("/scoremanage1/setting/")
public class ScoreStatParamSetAction extends BaseAction {
	@Autowired
	private ScoreManageService scoreService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	/**
	 * 获取统计参数
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getStaticParameters", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStaticParameters(HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		String xxdm = getXxdm(req);
		response.putAll(scoreService.getStatisticalParameters(xxdm));
		return response;
	}

	/**
	 * 成绩分析--保存统计参数
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateStatisticalParameters", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateStatisticalParameters(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			request.put("xxdm", getXxdm(req));

			scoreService.updateStatisticalParameters(request);

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
	 * 获取排名区间列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getIntervalList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getIntervalList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {

			String xnxq = request.getString("termInfoId");
			String kslcdm = request.getString("examId");
			String synj = request.getString("usedGrade");
			String bmfz = request.getString("asgId");

			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslcdm) || StringUtils.isBlank(synj)
					|| StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xnxq", xnxq);
			params.put("nj", synj);
			params.put("xxdm", getXxdm(req));
			params.put("bmfz", bmfz);

			response.putAll(scoreService.getIntervalList(params));

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
	 * 成绩分析--保存排名区间
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateInterval", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateInterval(@RequestBody JSONObject request, HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslcdm = request.getString("examId");
			String synj = request.getString("usedGrade");
			String bmfz = request.getString("asgId");
			String termInfoId = request.getString("termInfoId");
			JSONArray pmfbs = request.getJSONArray("intervalList");
			if (CollectionUtils.isEmpty(pmfbs) || StringUtils.isBlank(termInfoId) || StringUtils.isBlank(termInfoId)
					|| StringUtils.isBlank(bmfz) || StringUtils.isBlank(synj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);
			params.put("nj", synj);
			params.put("fzdm", bmfz);
			params.put("pmfbs", pmfbs);
			scoreService.updateInterval(params);

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
	 * 获取总分分数段设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getTotalScoreSection", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTotalScoreSection(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {

			String xnxq = request.getString("termInfoId");
			String kslcdm = request.getString("examId");
			String synj = request.getString("usedGrade");
			String bmfz = request.getString("asgId");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslcdm) || StringUtils.isBlank(synj)
					|| StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("nj", synj);
			params.put("bmfz", bmfz);

			response.putAll(scoreService.getTotalScoreSection(params));

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
	 * 成绩分析--保存总分分数段设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateTotalScoreSection", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTotalScoreSection(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslcdm = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			String synj = request.getString("usedGrade");
			String bmfz = request.getString("asgId");

			if (StringUtils.isBlank(kslcdm) || StringUtils.isBlank(termInfoId) || StringUtils.isBlank(synj)
					|| StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			String szfs = request.getString("setWay");
			int bl = StringUtils.isNotBlank(request.getString("percent")) ? request.getIntValue("percent") : 0;
			int zhqjbl = StringUtils.isNotBlank(request.getString("finalSecPer")) ? request.getIntValue("finalSecPer")
					: 0;
			int fz = StringUtils.isNotBlank(request.getString("secValue")) ? request.getIntValue("secValue") : 0;
			int zhqjfz = StringUtils.isNotBlank(request.getString("finalSecValue"))
					? request.getIntValue("finalSecValue") : 0;
			String zdytext = request.getString("customSecValue");
			int dkbl = StringUtils.isNotBlank(request.getString("ssPercent")) ? request.getIntValue("ssPercent") : 0;
			int dkzhqjbl = StringUtils.isNotBlank(request.getString("ssFinalSecPer"))
					? request.getIntValue("ssFinalSecPer") : 0;

			String xxdm = getXxdm(req);

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("xnxq", termInfoId);
			params.put("xxdm", xxdm);

			Zfqjsz zfqjsz = new Zfqjsz();
			zfqjsz.setKslc(kslcdm);
			zfqjsz.setXxdm(xxdm);
			zfqjsz.setXnxq(termInfoId);
			zfqjsz.setNj(synj);
			zfqjsz.setFzdm(bmfz);
			zfqjsz.setFs(szfs);
			zfqjsz.setBl(bl);
			zfqjsz.setZhqjbl(zhqjbl);
			zfqjsz.setFz(fz);
			zfqjsz.setZhqjfz(zhqjfz);
			zfqjsz.setZdytext(zdytext);
			zfqjsz.setDkbl(dkbl);
			zfqjsz.setDkzhqjbl(dkzhqjbl);

			params.put("zfqjsz", zfqjsz);

			scoreService.updateTotalScoreSection(params);

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
	 * 获取考试列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getContrastExamList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getContrastExamList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslcdm = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if (StringUtils.isBlank(kslcdm) || StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);

			response.putAll(scoreService.getContrastExamList(params));

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
	 * 成绩分析--保存对比考试
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateContrastExamList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateContrastExamList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslcdm = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			if (StringUtils.isBlank(kslcdm) || StringUtils.isBlank(xnxq)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("data", request.get("data"));

			scoreService.updateContrastExamList(params);

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
	 * 获取等第设置列表 no需从深圳接口获取科目名称
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getRankingsValueList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRankingsValueList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGrade");
			if (StringUtils.isBlank(kslc) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("nj", nj);

			response.putAll(scoreService.getRankingsValueList(params));

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
	 * 获取单科等级设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getBillDiviLevelSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONArray getBillDiviLevelSetting(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		try {
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGrade");
			if (StringUtils.isBlank(kslc) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			params.put("kmdm", request.get("subjectId"));
			
			response.put("data", scoreService.getRankingsByKMDM(params));
			
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		
		return response.getJSONArray("data");
	}

	/**
	 * 成绩分析--保存单科等级设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateBillDiviLevelSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateBillDiviLevelSetting(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGrade");
			if (StringUtils.isBlank(kslc) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kmdm", request.get("subjectId"));
			params.put("nj", nj);
			params.put("levelSetting", request.get("levelSetting"));

			scoreService.updateBillDiviLevelSetting(params);

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
	 * 成绩分析--清空等级设置
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "delBillDiviLevelSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delBillDiviLevelSetting(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGrade");
			if (StringUtils.isBlank(kslc) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("nj", nj);

			scoreService.delBillDiviLevelSetting(params);

			setResponse(response, 0, "清空成功！");
			response.getJSONObject("data").put("msg", "清空成功！");
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
	 * 成绩分析-等级生成
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateRankingsValue", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateRankingsValue(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGrade");
			if (StringUtils.isBlank(kslc) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONArray djsz = JSON.parseArray(request.getString("levelSetting"));

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			params.put("djsz", djsz);

			scoreService.updateRankingsValue(params);

			setResponse(response, 0, "生成成功！");
			response.getJSONObject("data").put("msg", "生成成功！");
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
	 * 成绩分析-获取单科等级人数 no 需从深圳接口获取科目名称
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getSingleSubjectGrade", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSingleSubjectGrade(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		try {
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGrade");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslc) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONArray djsz = JSON.parseArray(request.getString("levelSetting"));

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);
			params.put("kmdm", request.get("subjectId"));
			params.put("djsz", djsz);

			response.put("data", scoreService.getSingleSubjectGrade(params));

			setResponse(response, 0, "生成成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
}
