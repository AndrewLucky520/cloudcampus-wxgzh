package com.talkweb.common.splitDbAndTable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplitTableRuleWrapper implements Map<String, String> {
	private Map<String, Integer> splitTableRule;
	Logger logger = LoggerFactory.getLogger(SplitTableRuleWrapper.class);

	private Integer autoIncr;

	public SplitTableRuleWrapper(Map<String, Integer> splitTableRule, Integer autoIncr) {
		this.splitTableRule = splitTableRule;
		this.autoIncr = autoIncr;
	}
	
	public void setAutoIncr(Integer autoIncr) {
		this.autoIncr = autoIncr;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return splitTableRule.size();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return splitTableRule.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return splitTableRule.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return splitTableRule.containsValue(value);
	}

	@Override
	public String get(Object tableName) {
		// TODO Auto-generated method stub
		//logger.info("tableName:"+tableName);
		Integer splitTableNum = splitTableRule.get(tableName);
		//logger.info("splitTableNum:"+splitTableNum);
		//logger.info("autoIncr"+autoIncr);
		if (splitTableNum == null || autoIncr == null) {
			return (String) tableName;
		}
		return tableName + "_" + (autoIncr % splitTableNum);
	}

	@Override
	public String put(String key, String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String remove(Object key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> values() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
