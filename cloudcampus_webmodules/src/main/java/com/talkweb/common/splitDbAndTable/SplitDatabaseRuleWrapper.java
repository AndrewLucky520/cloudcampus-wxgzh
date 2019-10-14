package com.talkweb.common.splitDbAndTable;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class SplitDatabaseRuleWrapper implements Map<Object, Object> {
	private Properties dbNameProperties;
	
	private String termInfo;
	
	public SplitDatabaseRuleWrapper(Properties dbNameProperties, String termInfo) {
		this.dbNameProperties = dbNameProperties;
		this.termInfo = termInfo;
	}

	public void setTermInfoId(String termInfoId) {
		this.termInfo = termInfoId;
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return dbNameProperties.size();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return dbNameProperties.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return dbNameProperties.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return dbNameProperties.containsValue(value);
	}

	@Override
	public Object get(Object dbNameKey) {
		// TODO Auto-generated method stub
		if(!dbNameProperties.containsKey(dbNameKey)){
			return dbNameKey;
		}
		if(StringUtils.isEmpty(termInfo)) {
			return dbNameKey;
		}
		return dbNameProperties.get(dbNameKey) + "_" + termInfo;
	}

	@Override
	public Object put(Object key, Object value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(Object key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends Object, ? extends Object> m) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Object> keySet() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Object> values() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
