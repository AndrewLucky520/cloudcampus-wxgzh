package com.talkweb.scoreManage.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_StageType;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.scoreManage.service.ScoreManageConfigService;

/****
 * 成绩管理配置
 * 
 * @author guoyuanbing
 * @time 2015-05-28
 */
@RequestMapping("/scoremanage1/ScoreReportSet/")
@Controller
public class ScoreManageConfigAction extends BaseAction {

	@Autowired
	private ScoreManageConfigService configService;

	@RequestMapping(value = "getScoreReportNameList", method = RequestMethod.POST)
	@ResponseBody
	public JSONArray getScoreReportNameList(HttpServletRequest request) {
		String schooId = getXxdm(request);
		School school = getSchool(request, null);
		List<T_StageType> stageList = school == null ? null : school.getStage();
		// 2.循环找到每个学校的培养层次，并且保证数字少的层次排在前面。
		StringBuffer stateType = new StringBuffer();
		if (stageList != null) {
			for (T_StageType state : stageList) {
				if (state.getValue() >= T_StageType.Primary.getValue()) {// 从小学开始计算
					stateType.append(state.getValue() - 1).append(",");
				}
			}
		}
		if (stateType.length() > 0) {
			stateType = stateType.deleteCharAt(stateType.length() - 1);
		}
		return configService.getScoreReportNameList(schooId, stateType.toString(), null);
	}

	@RequestMapping(value = "getReportConfigs", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getReportConfigs(@RequestBody JSONObject param, HttpServletRequest request) {
		String schoolId = getXxdm(request);// 学校编号
		String reportNo = param.getString("reportNo");
		if (reportNo == null) {
			reportNo = "";
		}
		return configService.getReportConfigs(reportNo, schoolId);
	}

	@RequestMapping(value = "saveReportConfigs", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> saveReportConfigs(@RequestBody JSONObject param, HttpServletRequest request) {
		// 1.对参数处理
		String schoolId = getXxdm(request);// 学校编号
		String reportNo = param.getString("reportNo"); // 报表代码
		if (reportNo == null) {
			reportNo = "";
		}
		String reportName = param.getString("reportName"); // 报表名称
		if (reportName == null) {
			reportName = "";
		}
		String config = param.getString("config");// 字段配置
		if (config == null) {
			config = "";
		}
		String availableRole = param.getString("availableRole");// 有权限查看的角色

		if ("".equals(availableRole)) {
			availableRole = "**";// 表示无角色能查看
		}
		return configService.saveReportConfigs(schoolId, reportNo, reportName, config, availableRole);
	}

	@RequestMapping(value = "setReportHidden", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> setReportHidden(@RequestBody JSONObject param, HttpServletRequest request) {
		// 1.对参数处理
		String reportNo = param.getString("reportNo");
		if (reportNo == null) {
			reportNo = "";
		}
		String reportTypeNo = param.getString("reportTypeNo");
		if (reportTypeNo == null) {
			reportTypeNo = "";
		}
		String schoolId = getXxdm(request);// 学校编号
		if (reportTypeNo.equals("004")) { // 自定义报表（未做完，不清楚是否取消）
			return configService.delCustomConfig(reportNo, schoolId);
		} else {
			List<String> reportNoList = (reportNo == null ? null : Arrays.asList(reportNo.split(",")));// 报表代码编号列表
			// 2.循环找到每个学校的培养层次，并且保证数字少的层次排在前面。
			School school = getSchool(request, null);
			List<T_StageType> stageList = school == null ? null : school.getStage();
			StringBuffer stateType = new StringBuffer();
			if (stageList != null) {
				for (T_StageType state : stageList) {
					if (state.getValue() >= T_StageType.Primary.getValue()) {// 从小学开始计算
						stateType.append(state.getValue() - 1).append(",");
					}
				}
				if (stateType.length() > 0) {
					stateType.deleteCharAt(stateType.length() - 1);
				}
			} else {
				stateType.append("1,2,3");
			}
			// 3.调用服务返回数据
			return configService.updateScoreReportIsShow(reportNoList, schoolId, stateType.toString(), 0);
		}
	}

	@RequestMapping(value = "setReportShow", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> setReportShow(@RequestBody JSONObject param, HttpServletRequest request) {
		// 1.对参数处理
		String reportNo = param.getString("reportNo");
		List<String> reportNoList = reportNo == null ? null : Arrays.asList(reportNo.split(","));// 报表代码编号列表
		String schoolId = getXxdm(request);// 学校编号
		// 2.循环找到每个学校的培养层次，并且保证数字少的层次排在前面。
		School school = getSchool(request, null);
		List<T_StageType> stageList = school == null ? null : school.getStage();
		StringBuffer stateType = new StringBuffer();
		if (stageList != null) {
			for (T_StageType state : stageList) {
				if (state.getValue() >= T_StageType.Primary.getValue()) {// 从小学开始计算
					stateType.append(state.getValue() - 1).append(",");
				}
			}
			if (stateType.length() > 0) {
				stateType.deleteCharAt(stateType.length() - 1);
			}
		} else {
			stateType.append("1,2,3");
		}
		// 3.调用服务返回数据
		return configService.updateScoreReportIsShow(reportNoList, schoolId, stateType.toString(), 1);
	}

	@RequestMapping(value = "saveCustomReportConfigs", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> saveCustomReportConfigs(@RequestBody Map<String, Object> param,
			HttpServletRequest request) {
		// 1.对参数处理
		String schoolId = getXxdm(request);// 学校编号
		String rptdm = StringUtil.transformString(param.get("rptdm"));// 基础报表代码
		String reportNo = StringUtil.transformString(param.get("reportNo"));// 报表代码
		String reportName = StringUtil.transformString(param.get("reportName"));// 报表名称
		String config = StringUtil.transformString(param.get("config"));// 字段配置
		String availableRole = StringUtil.transformString(param.get("availableRole"));// 有权限查看的角色

		return configService.saveReportConfigsCustom(rptdm, schoolId, reportNo, reportName, config, availableRole);
	}

	@RequestMapping(value = "getCustomReportConfigs", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getCustomReportConfigs(@RequestBody Map<String, Object> param,
			HttpServletRequest request) {
		// 1.对参数处理
		String schoolId = getXxdm(request);// 学校编号
		String reportNo = StringUtil.transformString(param.get("reportNo"));// 报表代码

		return configService.getReportConfigsCustom(reportNo, schoolId);
	}

}
