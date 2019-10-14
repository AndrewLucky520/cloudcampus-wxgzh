package com.talkweb.scoreManage.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.scoreManage.dao.ScoreManageConfigDao;
import com.talkweb.scoreManage.service.ScoreManageConfigService;

/****
 * 服务实现
 * 
 * @author guoyuanbing
 * @time 2015-05-28
 */
@Service
public class ScoreManageConfigServiceImpl implements ScoreManageConfigService {

	@Autowired
	private ScoreManageConfigDao configDao;

	@Override
	public JSONArray getScoreReportNameList(String schoolId, String studyState, String curRole) {

		// 1.获取参数，定义变量
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("studyStage", StringUtil.toListFromString(studyState));// 设置学习阶段
		param.put("shcoolId", schoolId);// 设置学校编号

		JSONArray result = new JSONArray();// 返回结果json数组

		// 1.读取数据库，得到记录结果
		List<JSONObject> reportConfigList = configDao.getScoreReportNameList(param); // 报告配置数据
		// List<JSONObject>
		// costomlist=configDao.getScoreReportConfigCustom(param);//自定义报表
		result.add(this.extractPointTypeReportTypeJson("01", "分数报表", curRole, reportConfigList));
		result.add(this.extractPointTypeReportTypeJson("02", "等第报表", curRole, reportConfigList));
		result.add(this.extractPointTypeReportTypeJson("03", "综合报表", curRole, reportConfigList));
		// result.add(this.extractPointTypeReportTypeJson("04", "自定义报表",
		// curRole, costomlist));
		// 2.返回结果

		return result;
	}

	/****
	 * 返回属于某一个报表类型的所有报表信息集合
	 * 
	 * @param reportType
	 *            报表类型
	 * @param reportTypeName
	 *            报表名称
	 * @param curRole
	 *            当前角色
	 * @param reportConfigs
	 *            报表配置数据记录列表
	 * @return
	 */
	private JSONObject extractPointTypeReportTypeJson(String reportType, String reportTypeName, String curRole,
			List<JSONObject> reportConfigs) {
		// 1.定义返回的json的对象
		JSONObject data = new JSONObject();
		JSONArray reprotArray = new JSONArray();
		data.put("reportTypeNo", reportType);
		data.put("reportTypeName", reportTypeName);

		// 2.查找指定的报表类型的数据
		for (JSONObject item : reportConfigs) {// 循环所有数据记录，把数据记录分类存储
			String type = item.getString("reportType");
			if (reportType != null && reportType.equals(type)) {
				// 每个报表信息json
				JSONObject report = new JSONObject();
				report.put("reportNo", item.getString("reportNo"));// 报表代码
				report.put("reportName", item.getString("reportName"));// 报表名称
				String flag = item.getString("flag");
				report.put("isUsed", flag);// 0(不可用)，1（可用）
				reprotArray.add(report);
			}
		}

		// 3.返回结果
		if (reprotArray.size() > 0) {// 如果有属于该类型的报表
			data.put("report", reprotArray);
			return data;
		} else {
			return null;
		}
	}

	@Override
	public Map<String, Object> updateScoreReportIsShow(List<String> reprotNoList, String schoolId, String stateType,
			int flag) {
		// 1.组织参数，循环调用DAO方法更新数据
		Map<String, Object> map = new HashMap<String, Object>();// 参数集合
		map.put("schoolId", schoolId);
		map.put("flag", flag);

		int number = 0;// 更新记录
		for (String reportNo : reprotNoList) {
			map.put("reportNo", reportNo);
			// 判断数据是否存在，不存在则添加一条记录，否则修改更新记录数据
			Map<String, Object> selectParam = new HashMap<String, Object>();// 选择查询记录参数
			selectParam.put("schoolId", schoolId);
			selectParam.put("reportNo", reportNo);

			int reportConfigNum = configDao.selectOneScoreReportConfig(selectParam);// 报表配置记录
			if (reportConfigNum < 1) {
				// 插入改报表的配置。
				Map<String, Object> insertParam = new HashMap<String, Object>();// 添加数据记录参数
				insertParam.put("schoolId", schoolId);
				insertParam.put("reportNo", reportNo);

				// 获取默认报表配置，通过报表编号
				Map<String, Object> selectDefaultConfigParam = new HashMap<String, Object>();
				selectDefaultConfigParam.put("reportNo", reportNo);
				selectDefaultConfigParam.put("stateType", stateType);
				JSONObject defaultConfig = configDao.getScoreReportDefaultConfig(selectDefaultConfigParam);
				if (defaultConfig != null) {
					insertParam.put("reportName", defaultConfig.get("reportName"));
					insertParam.put("config", defaultConfig.get("config"));
					insertParam.put("availableRole", defaultConfig.get("role"));
				}

				insertParam.put("flag", flag);
				number += configDao.addScoreReportConfig(insertParam);
			} else {
				// 更新报表的配置
				number += configDao.updateScoreReportIsShow(map);
			}
		}

		// 2.返回结果，判断成功与否
		Map<String, Object> result = new HashMap<String, Object>();
		if (number > 0) {
			result.put("code", 0);
			result.put("msg", "成功");
		} else {
			result.put("code", -1);
			result.put("msg", "失败");
		}
		return result;
	}

	@Override
	public JSONObject getReportConfigs(String reportNo, String schoolId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("reportNo", reportNo);
		param.put("schoolId", schoolId);
		// 获取配置信息列表，所有系统角色列表
		JSONObject json = configDao.getScoreReportConfig(param);
		// 把list中的config参数转换成json
		if (json != null && json.containsKey("config")) {
			String config = json.getString("config");
			if (config != null && !"".equals(config)) {
				json.put("config", JSONObject.parse(config));
			}
		}
		List<Map<String, Object>> roleList = this.getRoleList();
		if (json != null) {
			json.put("roleList", roleList);
		}
		return json;
	}

	@Override
	public JSONObject delCustomConfig(String reportNo, String schoolId) {
		// 1.参数Map
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("costomId", reportNo);
		param.put("schoolId", schoolId);

		// 2.获取配置信息列表，所有系统角色列表
		int number = configDao.delCustomConfig(param);
		JSONObject result = new JSONObject();
		if (number > 0) {
			result.put("code", 0);
			result.put("msg", "成功");
		} else {
			result.put("code", -1);
			result.put("msg", "失败");
		}
		return result;
	}

	@Override
	public JSONObject getReportConfigsCustom(String reportNo, String schoolId) {

		// 1.参数Map
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("reportNo", reportNo);
		param.put("schoolId", schoolId);

		// 2.获取配置信息列表，所有系统角色列表
		JSONObject list = configDao.getScoreReportConfigCustom(param);

		// 把list中的config参数转换成json
		if (list != null && list.containsKey("config")) {
			String config = list.getString("config");

			if (config != null && !"".equals(config)) {
				list.put("config", JSONObject.parse(config));
			}

		}

		List<Map<String, Object>> roleList = this.getRoleList();

		if (list != null)
			list.put("roleList", roleList);

		return list;
	}

	/***
	 * 获取所有系统角色列表
	 * 
	 * @return
	 */
	private List<Map<String, Object>> getRoleList() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 6; i++) {
			Map<String, Object> roleList = new HashMap<String, Object>();
			switch (i) {
			case 0:
				roleList.put("roleId", "01");
				roleList.put("roleName", "任课老师");
				break;
			case 1:
				roleList.put("roleId", "02");
				roleList.put("roleName", "班主任");
				break;
			case 2:
				roleList.put("roleId", "03");
				roleList.put("roleName", "年级组长");
				break;
			case 3:
				roleList.put("roleId", "04");
				roleList.put("roleName", "教研组长");
				break;
			case 4:
				roleList.put("roleId", "05");
				roleList.put("roleName", "备课组长");
				break;
			case 5:
				roleList.put("roleId", "06");
				roleList.put("roleName", "校领导");
				break;
			default:
				break;
			}
			list.add(roleList);
		}
		return list;
	}

	/****
	 * 保存报表配置数据
	 */
	@Override
	public Map<String, Object> saveReportConfigs(String schoolId, String reportNo, String reportName, String config,
			String availableRole) {
		int addNumber = 0;// 添加记录数
		int updateNumber = 0;// 更新记录数

		// 判断数据是否存在，不存在则添加一条记录，否则修改更新记录数据
		Map<String, Object> selectParam = new HashMap<String, Object>();// 选择查询记录参数
		selectParam.put("schoolId", schoolId);
		selectParam.put("reportNo", reportNo);
		int reportConfigNum = configDao.selectOneScoreReportConfig(selectParam);// 报表配置记录

		if (reportConfigNum < 1) {
			Map<String, Object> insertParam = new HashMap<String, Object>();// 添加数据记录参数
			insertParam.put("schoolId", schoolId);
			insertParam.put("reportNo", reportNo);
			insertParam.put("reportName", reportName);
			insertParam.put("config", config);
			insertParam.put("flag", 1);
			insertParam.put("availableRole", availableRole);
			addNumber = configDao.addScoreReportConfig(insertParam);
		} else {
			Map<String, Object> updateParam = new HashMap<String, Object>();// 更新记录参数
			updateParam.put("schoolId", schoolId);
			updateParam.put("reportNo", reportNo);
			updateParam.put("reportName", reportName);
			updateParam.put("config", config);
			updateParam.put("availableRole", availableRole);
			updateNumber = configDao.updateScoreReportConfig(updateParam);
			updateNumber += configDao.updateScoreReportInfo(updateParam);
		}

		// 3.生成返回结果，更具前面的更新记录或是添加记录判断更新操作结果
		Map<String, Object> result = new HashMap<String, Object>();
		if (addNumber > 0 || updateNumber > 0) {
			result.put("code", 0);
			result.put("msg", "成功!");
		} else {
			result.put("code", -1);
			result.put("msg", "失败!");
		}

		return result;
	}

	/****
	 * 保存报表配置数据
	 */
	@Override
	public Map<String, Object> saveReportConfigsCustom(String rptdm, String schoolId, String reportNo,
			String reportName, String config, String availableRole) {

		// 1.组织参数
		Map<String, Object> insertParam = new HashMap<String, Object>();// 添加数据记录参数
		insertParam.put("schoolId", schoolId);
		insertParam.put("reportNo", rptdm);
		insertParam.put("reportName", reportName);
		insertParam.put("config", config);
		insertParam.put("flag", 1);
		insertParam.put("availableRole", availableRole);

		Map<String, Object> updateParam = new HashMap<String, Object>();// 更新记录参数
		updateParam.put("schoolId", schoolId);
		updateParam.put("customId", reportNo);
		updateParam.put("reportName", reportName);
		updateParam.put("config", config);
		updateParam.put("availableRole", availableRole);

		int addNumber = 0;// 添加记录数
		int updateNumber = 0;// 更新记录数

		if (reportNo.isEmpty()) {
			String uuid = UUIDUtil.getUUID();
			insertParam.put("customId", uuid);
			addNumber = configDao.addScoreReportConfigCustom(insertParam);
		} else {
			updateNumber = configDao.updateScoreReportConfigCustom(updateParam);
		}

		// 3.生成返回结果，更具前面的更新记录或是添加记录判断更新操作结果
		Map<String, Object> result = new HashMap<String, Object>();
		if (addNumber > 0 || updateNumber > 0) {
			result.put("code", 0);
			result.put("msg", "成功!");
		} else {
			result.put("code", -1);
			result.put("msg", "失败!");
		}

		return result;
	}

	/****
	 * 获取本次报表的字段查看权限。
	 */
	@Override
	public JSONObject getReportFieldAuths(String schoolId, String reportNo) {
		// 1.参数Map
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("reportNo", reportNo);
		param.put("schoolId", schoolId);

		// 2.获取配置信息列表，所有系统角色列表
		JSONObject obj = configDao.getScoreReportConfig(param);
		return obj;
	}

}
