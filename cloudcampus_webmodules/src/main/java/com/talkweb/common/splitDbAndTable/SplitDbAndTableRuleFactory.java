package com.talkweb.common.splitDbAndTable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SplitDbAndTableRuleFactory {
	private Map<String, Map<String, Integer>> splitTableRule = new HashMap<String, Map<String, Integer>>();
	Logger logger = LoggerFactory.getLogger(SplitDbAndTableRuleFactory.class);
	@Autowired
	private Properties dbNameProperties;

	@Autowired
	private void initMethod(Properties splitTableRuleProperties, Properties settings) {
		if (splitTableRuleProperties == null || settings == null) {
			throw new UnsupportedOperationException("没有获取配置文件中的分表规则！");
		}
		
		String firstTermInfoId = settings.getProperty("firstTermInfoId");	// 设置第一个学年学期
		if(firstTermInfoId != null) {
			TermInfoIdUtils.FIRST_TERMINFOID = firstTermInfoId;
		}
		
		for (Object keyObj : splitTableRuleProperties.keySet()) {
			String key = (String) keyObj;
			Integer value = Integer.valueOf(splitTableRuleProperties.getProperty(key));
			if(value == null || value == 1) {
				continue;
			}
			String[] tmp = key.split("\\.");
			String termInfo = tmp[0];
			String tableName = tmp[1];
			if (!splitTableRule.containsKey(termInfo)) {
				splitTableRule.put(termInfo, new HashMap<String, Integer>());
			}
			splitTableRule.get(termInfo).put(tableName, value);
		}
	}

	public SplitDbAndTableRule getRule(String termInfo, Integer autoIncr, Object data) {
		if(StringUtils.isEmpty(termInfo)) {
			throw new RuntimeException("分库分表操作，学年学期为空！");
		}
		
		//logger.info("splitTableRule:"+splitTableRule);
		Map<String, Integer> rule = splitTableRule.get(termInfo);
		//logger.info("rule："+rule);
//		rule.forEach( (k,v) -> {logger.info(k+"-"+v);});
		if (rule == null) {
			rule = new HashMap<String, Integer>();
		}
		
		SplitTableRuleWrapper tableRule = new SplitTableRuleWrapper(Collections.unmodifiableMap(rule), autoIncr);
		//logger.info("tableRule:"+tableRule.get(""));
		SplitDatabaseRuleWrapper dbRule = new SplitDatabaseRuleWrapper(dbNameProperties, termInfo);
		return new SplitDbAndTableRule(tableRule, dbRule, data);
	}
}
