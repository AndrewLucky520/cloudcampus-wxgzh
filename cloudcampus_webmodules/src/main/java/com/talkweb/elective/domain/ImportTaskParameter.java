package com.talkweb.elective.domain;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @ClassName: ImportTaskParameter.java	
 * @version:2.0
 * @Description: 启动选课导入任务请求的参数-封装实体 v2.0增加子进程需要的参数
 * @author 智慧校
 * @date 2015年3月4日
 */
public class ImportTaskParameter implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -4763258576371636400L;
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
     * 课表代码
     */
	private String electiveId;
	
    /**
     * 进程ID
     */
    private String processId;  
    /**
     * 学期
     */
    private String schoolYear;
    /**
     * 学年
     */
    private String term;

    
    public String getElectiveId() {
		return electiveId;
	}

	public void setElectiveId(String electiveId) {
		this.electiveId = electiveId;
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

	public String getSchoolYear() {
		return schoolYear;
	}

	public void setSchoolYear(String schoolYear) {
		this.schoolYear = schoolYear;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	
  
}