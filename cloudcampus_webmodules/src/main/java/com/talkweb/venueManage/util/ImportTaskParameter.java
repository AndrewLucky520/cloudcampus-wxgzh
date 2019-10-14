package com.talkweb.venueManage.util;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @ClassName: ImportTaskParameter.java	
 * @version:2.0
 * @Description: 启动选课导入任务请求的参数-封装实体 v2.0增加子进程需要的参数
 * @author zhanghuihui15222@talkweb.com.cn
 * @date 2015年12月3日
 */
public class ImportTaskParameter implements Serializable {

    /**
     * 是否需要手工匹配,1 手工匹配，0 无需匹配
     */
    private int isMatch;
    /**
     * 学校代码
     */
    private String schoolId;
    /**
     * 1 追加导入，2 删除导入 
     */
    private int importType;
    /**
     * 匹配结果
     */
    private JSONArray matchResult;
    /**
     * 登录用户
     */
    private String accountId;

	/**
     * 进程ID
     */
    private String processId;  

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

    public int getIsMatch() {
        return isMatch;
    }

    public void setIsMatch(int isMatch) {
        this.isMatch = isMatch;
    }

    public int getImportType() {
        return importType;
    }

    public void setImportType(int importType) {
        this.importType = importType;
    }

    public JSONArray getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(JSONArray matchResult) {
        this.matchResult = matchResult;
    }

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
	 public String getAccountId() {
			return accountId;
	}

	public void setAccountId(String accountId) {
			this.accountId = accountId;
	}
}