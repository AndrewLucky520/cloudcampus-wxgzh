package com.talkweb.csbasedata.util;

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
	 * 
	 */
	private static final long serialVersionUID = 9070633336464368857L;
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

    private String orgType ; 
    private String fileId;
    private String [] stuTitle;
    private int [] stuTitleNeed;
    private String [] stuTitleName;
    private String Xnxq;
	/**
     * 进程ID
     */
    private String processId;  
    
    private String keyId;

    public String getXnxq() {
		return Xnxq;
	}
	public void setXnxq(String xnxq) {
		Xnxq = xnxq;
	}
	public String getKeyId() {
		return keyId;
	}
	public String getOrgType() {
		return orgType;
	}

	public void setOrgType(String orgType) {
		this.orgType = orgType;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
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
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String[] getStuTitle() {
		return stuTitle;
	}
	public void setStuTitle(String[] stuTitle) {
		this.stuTitle = stuTitle;
	}
	public int[] getStuTitleNeed() {
		return stuTitleNeed;
	}
	public void setStuTitleNeed(int[] stuTitleNeed) {
		this.stuTitleNeed = stuTitleNeed;
	}
	public String[] getStuTitleName() {
		return stuTitleName;
	}
	public void setStuTitleName(String[] stuTitleName) {
		this.stuTitleName = stuTitleName;
	}
	
}
