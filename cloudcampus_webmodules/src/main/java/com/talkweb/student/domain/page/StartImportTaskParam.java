package com.talkweb.student.domain.page;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @ClassName: StartImportTaskParam.java	
 * @version:2.0
 * @Description: 启动学生导入任务请求的参数-封装实体 v2.0增加子进程需要的参数
 * @author 武洋 ---智慧校
 * @date 2015年3月4日
 */
public class StartImportTaskParam implements Serializable{

    /**
     * 是否需要手工匹配,1 手工匹配，0 无需匹配
     */
    private int isMatch;
    /**
     * 学校代码
     */
    private String xxdm;
    /**
     * 使用年级
     */
    private String grade;
    /**
     * 1 追加导入，2 删除导入 
     */
    private int importType;
    /**
     * 匹配结果
     */
    private JSONArray matchResult;
    
    /**
     * 培养层次
     */
    private String pycc;
    /**
     * 学期
     */
    private String xq;
    /**
     * 学年
     */
    private String xn;
    /**
     * 年级
     */
    private String nj;
    /**
     * 入学年度
     */
    private String rxnd;
    /**
     * 考试轮次代码
     */
    private String kslc;
    
    public String getXxdm() {
        return xxdm;
    }

    public void setXxdm(String xxdm) {
        this.xxdm = xxdm;
    }

    public int getIsMatch() {
        return isMatch;
    }

    public void setIsMatch(int isMatch) {
        this.isMatch = isMatch;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
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
    public String getPycc() {
        return pycc;
    }

    public void setPycc(String pycc) {
        this.pycc = pycc;
    }

    public String getXq() {
        return xq;
    }

    public void setXq(String xq) {
        this.xq = xq;
    }

    public String getXn() {
        return xn;
    }

    public void setXn(String xn) {
        this.xn = xn;
    }

    public String getNj() {
        return nj;
    }

    public void setNj(String nj) {
        this.nj = nj;
    }

    public String getRxnd() {
        return rxnd;
    }

    public void setRxnd(String rxnd) {
        this.rxnd = rxnd;
    }

	public String getKslc() {
		return kslc;
	}

	public void setKslc(String kslc) {
		this.kslc = kslc;
	}

}
