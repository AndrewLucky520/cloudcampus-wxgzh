package com.talkweb.timetable.domain;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;
import com.talkweb.accountcenter.thrift.School;

/**
 * 
 * @ClassName: ImportTaskParameter.java	
 * @version:2.0
 * @Description: 启动学生导入任务请求的参数-封装实体 v2.0增加子进程需要的参数
 * @author 智慧校
 * @date 2015年3月4日
 */
public class ImportTaskParameter implements Serializable{

	private static final long serialVersionUID = 6889550452189854741L;
	/**
     * 是否需要手工匹配,1 手工匹配，0 无需匹配
     */
    private int isMatch;
    
    private String placementType;
    /**
     * 学校代码
     */
    private String xxdm;
    
    public String getPlacementType() {
		return placementType;
	}

	public void setPlacementType(String placementType) {
		this.placementType = placementType;
	}

	private School school;
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
	private String timetableId;
	
    /**
     * 进程ID
     */
    private String processId;
    
    /**
     * 学年学期
     */
    private String termInfo;
    
    private String keyId;

    
    public String getTimetableId() {
		return timetableId;
	}

	public void setTimetableId(String timetableId) {
		this.timetableId = timetableId;
	}
    
    public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getXxdm() {
        return xxdm;
    }

    public void setXxdm(String xxdm) {
        this.xxdm = xxdm;
    }

    public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = school;
	}
	
	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public int getIsMatch() {
        return isMatch;
    }

    public void setIsMatch(int isMatch) {
        this.isMatch = isMatch;
    }

    public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
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
  
}