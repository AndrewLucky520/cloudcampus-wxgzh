package com.talkweb.common.splitDbAndTable;

public class SplitDbAndTableRule {
	private SplitTableRuleWrapper tableRule;
	
	private SplitDatabaseRuleWrapper dbRule;

	private Object data;

	public SplitDbAndTableRule(SplitTableRuleWrapper tableRule, SplitDatabaseRuleWrapper dbRule, Object data) {
		this.tableRule = tableRule;
		this.dbRule = dbRule;
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public SplitTableRuleWrapper getTableRule() {
		return tableRule;
	}

	public void setTermInfoId(String termInfoId) {
		this.dbRule.setTermInfoId(termInfoId);
	}
	
	public SplitDatabaseRuleWrapper getDbRule() {
		return dbRule;
	}
	
	public void setAutoIncr(Integer autoIncr) {
		this.tableRule.setAutoIncr(autoIncr);
	}
}
