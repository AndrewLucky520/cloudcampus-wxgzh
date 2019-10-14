package com.talkweb.wishFilling.vo;

import java.util.Date;

public class WfInfoVo {
    private String wfid;

    private String schoolid;

    private String schoolyear;

    private String terminfoid;

    private String wfname;

    private Date wfstarttime;

    private Date wfendtime;

    private String wfnum;

    private String wfusegrade;

    private Date createdate;

    private String hasstudenttb;

    private String isshowright;

    private String isbyelection;

    private Date bystarttime;

    private Date byendtime;

    private String wfway;

    private Long wfindex;

    private Integer pycc;

    public String getWfid() {
        return wfid;
    }

    public void setWfid(String wfid) {
        this.wfid = wfid;
    }

    public String getSchoolid() {
        return schoolid;
    }

    public void setSchoolid(String schoolid) {
        this.schoolid = schoolid;
    }

    public String getSchoolyear() {
        return schoolyear;
    }

    public void setSchoolyear(String schoolyear) {
        this.schoolyear = schoolyear;
    }

    public String getTerminfoid() {
        return terminfoid;
    }

    public void setTerminfoid(String terminfoid) {
        this.terminfoid = terminfoid;
    }

    public String getWfname() {
        return wfname;
    }

    public void setWfname(String wfname) {
        this.wfname = wfname;
    }

    public Date getWfstarttime() {
        return wfstarttime;
    }

    public void setWfstarttime(Date wfstarttime) {
        this.wfstarttime = wfstarttime;
    }

    public Date getWfendtime() {
        return wfendtime;
    }

    public void setWfendtime(Date wfendtime) {
        this.wfendtime = wfendtime;
    }

    public String getWfnum() {
        return wfnum;
    }

    public void setWfnum(String wfnum) {
        this.wfnum = wfnum;
    }

    public String getWfusegrade() {
        return wfusegrade;
    }

    public void setWfusegrade(String wfusegrade) {
        this.wfusegrade = wfusegrade;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }

    public String getHasstudenttb() {
        return hasstudenttb;
    }

    public void setHasstudenttb(String hasstudenttb) {
        this.hasstudenttb = hasstudenttb;
    }

    public String getIsshowright() {
        return isshowright;
    }

    public void setIsshowright(String isshowright) {
        this.isshowright = isshowright;
    }

    public String getIsbyelection() {
        return isbyelection;
    }

    public void setIsbyelection(String isbyelection) {
        this.isbyelection = isbyelection;
    }

    public Date getBystarttime() {
        return bystarttime;
    }

    public void setBystarttime(Date bystarttime) {
        this.bystarttime = bystarttime;
    }

    public Date getByendtime() {
        return byendtime;
    }

    public void setByendtime(Date byendtime) {
        this.byendtime = byendtime;
    }

    public String getWfway() {
        return wfway;
    }

    public void setWfway(String wfway) {
        this.wfway = wfway;
    }

    public Long getWfindex() {
        return wfindex;
    }

    public void setWfindex(Long wfindex) {
        this.wfindex = wfindex;
    }

    public Integer getPycc() {
        return pycc;
    }

    public void setPycc(Integer pycc) {
        this.pycc = pycc;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        WfInfoVo other = (WfInfoVo) that;
        return (this.getWfid() == null ? other.getWfid() == null : this.getWfid().equals(other.getWfid()))
            && (this.getSchoolid() == null ? other.getSchoolid() == null : this.getSchoolid().equals(other.getSchoolid()))
            && (this.getSchoolyear() == null ? other.getSchoolyear() == null : this.getSchoolyear().equals(other.getSchoolyear()))
            && (this.getTerminfoid() == null ? other.getTerminfoid() == null : this.getTerminfoid().equals(other.getTerminfoid()))
            && (this.getWfname() == null ? other.getWfname() == null : this.getWfname().equals(other.getWfname()))
            && (this.getWfstarttime() == null ? other.getWfstarttime() == null : this.getWfstarttime().equals(other.getWfstarttime()))
            && (this.getWfendtime() == null ? other.getWfendtime() == null : this.getWfendtime().equals(other.getWfendtime()))
            && (this.getWfnum() == null ? other.getWfnum() == null : this.getWfnum().equals(other.getWfnum()))
            && (this.getWfusegrade() == null ? other.getWfusegrade() == null : this.getWfusegrade().equals(other.getWfusegrade()))
            && (this.getCreatedate() == null ? other.getCreatedate() == null : this.getCreatedate().equals(other.getCreatedate()))
            && (this.getHasstudenttb() == null ? other.getHasstudenttb() == null : this.getHasstudenttb().equals(other.getHasstudenttb()))
            && (this.getIsshowright() == null ? other.getIsshowright() == null : this.getIsshowright().equals(other.getIsshowright()))
            && (this.getIsbyelection() == null ? other.getIsbyelection() == null : this.getIsbyelection().equals(other.getIsbyelection()))
            && (this.getBystarttime() == null ? other.getBystarttime() == null : this.getBystarttime().equals(other.getBystarttime()))
            && (this.getByendtime() == null ? other.getByendtime() == null : this.getByendtime().equals(other.getByendtime()))
            && (this.getWfway() == null ? other.getWfway() == null : this.getWfway().equals(other.getWfway()))
            && (this.getWfindex() == null ? other.getWfindex() == null : this.getWfindex().equals(other.getWfindex()))
            && (this.getPycc() == null ? other.getPycc() == null : this.getPycc().equals(other.getPycc()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getWfid() == null) ? 0 : getWfid().hashCode());
        result = prime * result + ((getSchoolid() == null) ? 0 : getSchoolid().hashCode());
        result = prime * result + ((getSchoolyear() == null) ? 0 : getSchoolyear().hashCode());
        result = prime * result + ((getTerminfoid() == null) ? 0 : getTerminfoid().hashCode());
        result = prime * result + ((getWfname() == null) ? 0 : getWfname().hashCode());
        result = prime * result + ((getWfstarttime() == null) ? 0 : getWfstarttime().hashCode());
        result = prime * result + ((getWfendtime() == null) ? 0 : getWfendtime().hashCode());
        result = prime * result + ((getWfnum() == null) ? 0 : getWfnum().hashCode());
        result = prime * result + ((getWfusegrade() == null) ? 0 : getWfusegrade().hashCode());
        result = prime * result + ((getCreatedate() == null) ? 0 : getCreatedate().hashCode());
        result = prime * result + ((getHasstudenttb() == null) ? 0 : getHasstudenttb().hashCode());
        result = prime * result + ((getIsshowright() == null) ? 0 : getIsshowright().hashCode());
        result = prime * result + ((getIsbyelection() == null) ? 0 : getIsbyelection().hashCode());
        result = prime * result + ((getBystarttime() == null) ? 0 : getBystarttime().hashCode());
        result = prime * result + ((getByendtime() == null) ? 0 : getByendtime().hashCode());
        result = prime * result + ((getWfway() == null) ? 0 : getWfway().hashCode());
        result = prime * result + ((getWfindex() == null) ? 0 : getWfindex().hashCode());
        result = prime * result + ((getPycc() == null) ? 0 : getPycc().hashCode());
        return result;
    }
}