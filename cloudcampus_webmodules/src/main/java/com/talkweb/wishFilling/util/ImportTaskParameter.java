package com.talkweb.wishFilling.util;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;
import com.talkweb.accountcenter.thrift.School;

/**
 * 
 * @ClassName: ImportTaskParameter.java	
 * @version:2.0
 * @Description: 启动选课导入任务请求的参数-封装实体 v2.0增加子进程需要的参数
 * @author zhanghuihui15222@talkweb.com.cn
 * @date 2016年9月10日
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
    private School school;
    private String subjectIds;
    private int wfNum;
    private Long gradeId;
    private String isByElection;
    private String areaCode;
    public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getIsByElection() {
		return isByElection;
	}

	public void setIsByElection(String isByElection) {
		this.isByElection = isByElection;
	}

	public String getWfWay() {
		return wfWay;
	}

	public void setWfWay(String wfWay) {
		this.wfWay = wfWay;
	}

	private String wfWay;
    public int getWfNum() {
		return wfNum;
	}

	public void setWfNum(int wfNum) {
		this.wfNum = wfNum;
	}

	public Long getGradeId() {
		return gradeId;
	}

	public void setGradeId(Long gradeId) {
		this.gradeId = gradeId;
	}

	
    public String getSubjectIds() {
		return subjectIds;
	}

	public void setSubjectIds(String subjectIds) {
		this.subjectIds = subjectIds;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = school;
	}

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
    /**
     * 学年学期
     */
    private String termInfo;  
    /**
     * 填报代码
     */
    private String wfId;  
	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public String getWfId() {
		return wfId;
	}

	public void setWfId(String wfId) {
		this.wfId = wfId;
	}

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