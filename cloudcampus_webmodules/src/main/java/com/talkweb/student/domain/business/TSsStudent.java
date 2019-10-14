package com.talkweb.student.domain.business;

/**
 * @ClassName: StudentInfo.java
 * @version:1.0
 * @Description: 学生基本信息 t_ss_student
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */
public class TSsStudent {
    /** 学生代码 **/
    private String xh;
    /** 学校代码 **/
    private String xxdm;
    /** 学校学号 **/
    private String userXh;
    /** 学籍号 **/
    private String xjh;
    /** 姓名 **/
    private String xm;
    /** 曾用名 **/
    private String cym;
    /** 姓名拼音 **/
    private String xmpy;
    /** 入学年月 **/
    private Object rxny;
    /** 入学年级 **/
    private String nj;
    /** 入学方式 **/
    private String rxfs;
    /** 班级代码 **/
    private String bh;
    /** 学生类别 **/
    private String xslbm;
    /** 身份证件类型 **/
    private String sfzjlxm;
    /** 身份证件号 **/
    private String sfzh;
    /** 性别 **/
    private String xbm;
    /** 出生日期 **/
    private Object csrq;
    /** 籍贯 **/
    private String jgm;
    /** 出生地 **/
    private String csd;
    /** 民族 **/
    private String mzm;
    /** 健康状况 **/
    private String jkzkm;
    /** 残疾类型 **/
    private String cjrlx;
    /** 国籍/地区 **/
    private String gjdqm;
    /** 港澳台侨外 **/
    private String gatqwm;
    /** 政治面貌 **/
    private String zzmmm;
    /** 现住址 **/
    private String xzz;
    /** 户口所在地 **/
    private String hkszd;
    /** 户口性质 **/
    private String hkxzm;
    /** 流动人口状况 **/
    private String ldrkzk;
    /** 入团时间 **/
    private Object rtsj;
    /** 入党时间 **/
    private Object rdsj;
    /** 父亲姓名 **/
    private String fqxm;
    /** 父亲工作单位 **/
    private String fqgzdw;
    /** 父亲联系电话 **/
    private String fqlxdh;
    /** 父亲政治面貌 **/
    private String fqzzmm;
    /** 父亲出生日期 **/
    private Object fqcsny;
    /** 父亲备注 **/
    private String fqbz;
    /** 母亲姓名 **/
    private String mqxm;
    /** 母亲工作单位 **/
    private String mqgzdw;
    /** 母亲联系电话 **/
    private String mqlxdh;
    /** 母亲政治面貌 **/
    private String mqzzmm;
    /** 母亲出生日期 **/
    private Object mqcsny;
    /** 母亲备注 **/
    private String mqbz;
    /** 监护人名称 **/
    private String jfrxm;
    /** 监护人关系 **/
    private String jfrgx;
    /** 监护联系电话 **/
    private String jfrlxdh;
    /** 监护人户口所在地 **/
    private String jfrhkszd;
    /** 监护人现住址 **/
    private String jfrxzz;
    /** 通信地址 **/
    private String txdz;
    /** 邮政编码 **/
    private String yzbm;
    /** 照片 **/
    private String zp;
    /** 毕业学校 **/
    private String yxxmc;
    /** 原学号 **/
    private String czxh;
    /** 学生来源 **/
    private String lqlb;
    /** 择校生 **/
    private String zxs;
    /** 就读方式 **/
    private String zds;
    /** 随班就读 **/
    private String sbjd;
    /** 学籍状态 **/
    private String xjzt;
    /** 是否在校 **/
    private String sfzx;
    /** 培养层次 **/
    private String pycc;
    /** 特长 **/
    private String tc;
    /** 家庭地址 **/
    private String jtdz;
    /** 联系电话 **/
    private String lxdh;
    /** 电子信箱 **/
    private String dzxx;
    /** 银行 **/
    private String yh;
    /** 银行帐号 **/
    private String thzh;
    /** 卡号 **/
    private String kh;
    /** 生源居地性质 **/
    private String syjdxz;
    /** 备注 **/
    private String bz;
    /** 是否独生子女 **/
    private String dszybz;
    /** 是否受过学前教育 **/
    private String sfsgxqjy;
    /** 是否留守儿童 **/
    private String sflsqt;
    /** 是否需要申请资助 **/
    private String sfxysqzz;
    /** 是否享受一补 **/
    private String sfxsyb;
    /** 是否孤儿 **/
    private String sfge;
    /** 是否烈士或优抚子女 **/
    private String sflshyfzn;
    /** 是否进城务工人员随迁子女 **/
    private String sfjcfgrysqzn;
    /** 是否由政府购买学位 **/
    private String sfyzfgmxw;
    private String parentsysid;
    public String getParentsysid() {
        return parentsysid;
    }
    public void setParentsysid(String parentsysid) {
        this.parentsysid = parentsysid;
    }
    // method
    public String getXh() {
        return xh;
    }

    public void setXh(String xh) {
        this.xh = xh;
    }

    public String getXxdm() {
        return xxdm;
    }

    public void setXxdm(String xxdm) {
        this.xxdm = xxdm;
    }

    public String getUserXh() {
        return userXh;
    }

    public void setUserXh(String userXh) {
        this.userXh = userXh;
    }

    public String getXjh() {
        return xjh;
    }

    public void setXjh(String xjh) {
        this.xjh = xjh;
    }

    public String getXm() {
        return xm;
    }

    public void setXm(String xm) {
        this.xm = xm;
    }

    public String getCym() {
        return cym;
    }

    public void setCym(String cym) {
        this.cym = cym;
    }

    public String getXmpy() {
        return xmpy;
    }

    public void setXmpy(String xmpy) {
        this.xmpy = xmpy;
    }

    public Object getRxny() {
        return rxny;
    }

    public void setRxny(Object rxny) {
        this.rxny = rxny;
    }

    public String getNj() {
        return nj;
    }

    public void setNj(String nj) {
        this.nj = nj;
    }

    public String getRxfs() {
        return rxfs;
    }

    public void setRxfs(String rxfs) {
        this.rxfs = rxfs;
    }

    public String getBh() {
        return bh;
    }

    public void setBh(String bh) {
        this.bh = bh;
    }

    public String getXslbm() {
        return xslbm;
    }

    public void setXslbm(String xslbm) {
        this.xslbm = xslbm;
    }

    public String getSfzjlxm() {
        return sfzjlxm;
    }

    public void setSfzjlxm(String sfzjlxm) {
        this.sfzjlxm = sfzjlxm;
    }

    public String getSfzh() {
        return sfzh;
    }

    public void setSfzh(String sfzh) {
        this.sfzh = sfzh;
    }

    public String getXbm() {
        return xbm;
    }

    public void setXbm(String xbm) {
        this.xbm = xbm;
    }

    public Object getCsrq() {
        return csrq;
    }

    public void setCsrq(Object csrq) {
        this.csrq = csrq;
    }

    public String getJgm() {
        return jgm;
    }

    public void setJgm(String jgm) {
        this.jgm = jgm;
    }

    public String getCsd() {
        return csd;
    }

    public void setCsd(String csd) {
        this.csd = csd;
    }

    public String getMzm() {
        return mzm;
    }

    public void setMzm(String mzm) {
        this.mzm = mzm;
    }

    public String getJkzkm() {
        return jkzkm;
    }

    public void setJkzkm(String jkzkm) {
        this.jkzkm = jkzkm;
    }

    public String getCjrlx() {
        return cjrlx;
    }

    public void setCjrlx(String cjrlx) {
        this.cjrlx = cjrlx;
    }

    public String getGjdqm() {
        return gjdqm;
    }

    public void setGjdqm(String gjdqm) {
        this.gjdqm = gjdqm;
    }

    public String getGatqwm() {
        return gatqwm;
    }

    public void setGatqwm(String gatqwm) {
        this.gatqwm = gatqwm;
    }

    public String getZzmmm() {
        return zzmmm;
    }

    public void setZzmmm(String zzmmm) {
        this.zzmmm = zzmmm;
    }

    public String getXzz() {
        return xzz;
    }

    public void setXzz(String xzz) {
        this.xzz = xzz;
    }

    public String getHkszd() {
        return hkszd;
    }

    public void setHkszd(String hkszd) {
        this.hkszd = hkszd;
    }

    public String getHkxzm() {
        return hkxzm;
    }

    public void setHkxzm(String hkxzm) {
        this.hkxzm = hkxzm;
    }

    public String getLdrkzk() {
        return ldrkzk;
    }

    public void setLdrkzk(String ldrkzk) {
        this.ldrkzk = ldrkzk;
    }

    public Object getRtsj() {
        return rtsj;
    }

    public void setRtsj(Object rtsj) {
        this.rtsj = rtsj;
    }

    public Object getRdsj() {
        return rdsj;
    }

    public void setRdsj(Object rdsj) {
        this.rdsj = rdsj;
    }

    public String getFqxm() {
        return fqxm;
    }

    public void setFqxm(String fqxm) {
        this.fqxm = fqxm;
    }

    public String getFqgzdw() {
        return fqgzdw;
    }

    public void setFqgzdw(String fqgzdw) {
        this.fqgzdw = fqgzdw;
    }

    public String getFqlxdh() {
        return fqlxdh;
    }

    public void setFqlxdh(String fqlxdh) {
        this.fqlxdh = fqlxdh;
    }

    public String getFqzzmm() {
        return fqzzmm;
    }

    public void setFqzzmm(String fqzzmm) {
        this.fqzzmm = fqzzmm;
    }

    public Object getFqcsny() {
        return fqcsny;
    }

    public void setFqcsny(Object fqcsny) {
        this.fqcsny = fqcsny;
    }

    public String getFqbz() {
        return fqbz;
    }

    public void setFqbz(String fqbz) {
        this.fqbz = fqbz;
    }

    public String getMqxm() {
        return mqxm;
    }

    public void setMqxm(String mqxm) {
        this.mqxm = mqxm;
    }

    public String getMqgzdw() {
        return mqgzdw;
    }

    public void setMqgzdw(String mqgzdw) {
        this.mqgzdw = mqgzdw;
    }

    public String getMqlxdh() {
        return mqlxdh;
    }

    public void setMqlxdh(String mqlxdh) {
        this.mqlxdh = mqlxdh;
    }

    public String getMqzzmm() {
        return mqzzmm;
    }

    public void setMqzzmm(String mqzzmm) {
        this.mqzzmm = mqzzmm;
    }

    public Object getMqcsny() {
        return mqcsny;
    }

    public void setMqcsny(Object mqcsny) {
        this.mqcsny = mqcsny;
    }

    public String getMqbz() {
        return mqbz;
    }

    public void setMqbz(String mqbz) {
        this.mqbz = mqbz;
    }

    public String getJfrxm() {
        return jfrxm;
    }

    public void setJfrxm(String jfrxm) {
        this.jfrxm = jfrxm;
    }

    public String getJfrgx() {
        return jfrgx;
    }

    public void setJfrgx(String jfrgx) {
        this.jfrgx = jfrgx;
    }

    public String getJfrlxdh() {
        return jfrlxdh;
    }

    public void setJfrlxdh(String jfrlxdh) {
        this.jfrlxdh = jfrlxdh;
    }

    public String getJfrhkszd() {
        return jfrhkszd;
    }

    public void setJfrhkszd(String jfrhkszd) {
        this.jfrhkszd = jfrhkszd;
    }

    public String getJfrxzz() {
        return jfrxzz;
    }

    public void setJfrxzz(String jfrxzz) {
        this.jfrxzz = jfrxzz;
    }

    public String getTxdz() {
        return txdz;
    }

    public void setTxdz(String txdz) {
        this.txdz = txdz;
    }

    public String getYzbm() {
        return yzbm;
    }

    public void setYzbm(String yzbm) {
        this.yzbm = yzbm;
    }

    public String getZp() {
        return zp;
    }

    public void setZp(String zp) {
        this.zp = zp;
    }

    public String getYxxmc() {
        return yxxmc;
    }

    public void setYxxmc(String yxxmc) {
        this.yxxmc = yxxmc;
    }

    public String getCzxh() {
        return czxh;
    }

    public void setCzxh(String czxh) {
        this.czxh = czxh;
    }

    public String getLqlb() {
        return lqlb;
    }

    public void setLqlb(String lqlb) {
        this.lqlb = lqlb;
    }

    public String getZxs() {
        return zxs;
    }

    public void setZxs(String zxs) {
        this.zxs = zxs;
    }

    public String getZds() {
        return zds;
    }

    public void setZds(String zds) {
        this.zds = zds;
    }

    public String getSbjd() {
        return sbjd;
    }

    public void setSbjd(String sbjd) {
        this.sbjd = sbjd;
    }

    public String getXjzt() {
        return xjzt;
    }

    public void setXjzt(String xjzt) {
        this.xjzt = xjzt;
    }

    public String getSfzx() {
        return sfzx;
    }

    public void setSfzx(String sfzx) {
        this.sfzx = sfzx;
    }

    public String getPycc() {
        return pycc;
    }

    public void setPycc(String pycc) {
        this.pycc = pycc;
    }

    public String getTc() {
        return tc;
    }

    public void setTc(String tc) {
        this.tc = tc;
    }

    public String getJtdz() {
        return jtdz;
    }

    public void setJtdz(String jtdz) {
        this.jtdz = jtdz;
    }

    public String getLxdh() {
        return lxdh;
    }

    public void setLxdh(String lxdh) {
        this.lxdh = lxdh;
    }

    public String getDzxx() {
        return dzxx;
    }

    public void setDzxx(String dzxx) {
        this.dzxx = dzxx;
    }

    public String getYh() {
        return yh;
    }

    public void setYh(String yh) {
        this.yh = yh;
    }

    public String getThzh() {
        return thzh;
    }

    public void setThzh(String thzh) {
        this.thzh = thzh;
    }

    public String getKh() {
        return kh;
    }

    public void setKh(String kh) {
        this.kh = kh;
    }

    public String getSyjdxz() {
        return syjdxz;
    }

    public void setSyjdxz(String syjdxz) {
        this.syjdxz = syjdxz;
    }

    public String getBz() {
        return bz;
    }

    public void setBz(String bz) {
        this.bz = bz;
    }

    public String getDszybz() {
        return dszybz;
    }

    public void setDszybz(String dszybz) {
        this.dszybz = dszybz;
    }

    public String getSfsgxqjy() {
        return sfsgxqjy;
    }

    public void setSfsgxqjy(String sfsgxqjy) {
        this.sfsgxqjy = sfsgxqjy;
    }

    public String getSflsqt() {
        return sflsqt;
    }

    public void setSflsqt(String sflsqt) {
        this.sflsqt = sflsqt;
    }

    public String getSfxysqzz() {
        return sfxysqzz;
    }

    public void setSfxysqzz(String sfxysqzz) {
        this.sfxysqzz = sfxysqzz;
    }

    public String getSfxsyb() {
        return sfxsyb;
    }

    public void setSfxsyb(String sfxsyb) {
        this.sfxsyb = sfxsyb;
    }

    public String getSfge() {
        return sfge;
    }

    public void setSfge(String sfge) {
        this.sfge = sfge;
    }

    public String getSflshyfzn() {
        return sflshyfzn;
    }

    public void setSflshyfzn(String sflshyfzn) {
        this.sflshyfzn = sflshyfzn;
    }

    public String getSfjcfgrysqzn() {
        return sfjcfgrysqzn;
    }

    public void setSfjcfgrysqzn(String sfjcfgrysqzn) {
        this.sfjcfgrysqzn = sfjcfgrysqzn;
    }

    public String getSfyzfgmxw() {
        return sfyzfgmxw;
    }

    public void setSfyzfgmxw(String sfyzfgmxw) {
        this.sfyzfgmxw = sfyzfgmxw;
    }

    // override toString Method
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("'xh':'" + this.getXh() + "',");
        sb.append("'xxdm':'" + this.getXxdm() + "',");
        sb.append("'userXh':'" + this.getUserXh() + "',");
        sb.append("'xjh':'" + this.getXjh() + "',");
        sb.append("'xm':'" + this.getXm() + "',");
        sb.append("'cym':'" + this.getCym() + "',");
        sb.append("'xmpy':'" + this.getXmpy() + "',");
        sb.append("'rxny':'" + this.getRxny() + "',");
        sb.append("'nj':'" + this.getNj() + "',");
        sb.append("'rxfs':'" + this.getRxfs() + "',");
        sb.append("'bh':'" + this.getBh() + "',");
        sb.append("'xslbm':'" + this.getXslbm() + "',");
        sb.append("'sfzjlxm':'" + this.getSfzjlxm() + "',");
        sb.append("'sfzh':'" + this.getSfzh() + "',");
        sb.append("'xbm':'" + this.getXbm() + "',");
        sb.append("'csrq':'" + this.getCsrq() + "',");
        sb.append("'jgm':'" + this.getJgm() + "',");
        sb.append("'csd':'" + this.getCsd() + "',");
        sb.append("'mzm':'" + this.getMzm() + "',");
        sb.append("'jkzkm':'" + this.getJkzkm() + "',");
        sb.append("'cjrlx':'" + this.getCjrlx() + "',");
        sb.append("'gjdqm':'" + this.getGjdqm() + "',");
        sb.append("'gatqwm':'" + this.getGatqwm() + "',");
        sb.append("'zzmmm':'" + this.getZzmmm() + "',");
        sb.append("'xzz':'" + this.getXzz() + "',");
        sb.append("'hkszd':'" + this.getHkszd() + "',");
        sb.append("'hkxzm':'" + this.getHkxzm() + "',");
        sb.append("'ldrkzk':'" + this.getLdrkzk() + "',");
        sb.append("'rtsj':'" + this.getRtsj() + "',");
        sb.append("'rdsj':'" + this.getRdsj() + "',");
        sb.append("'fqxm':'" + this.getFqxm() + "',");
        sb.append("'fqgzdw':'" + this.getFqgzdw() + "',");
        sb.append("'fqlxdh':'" + this.getFqlxdh() + "',");
        sb.append("'fqzzmm':'" + this.getFqzzmm() + "',");
        sb.append("'fqcsny':'" + this.getFqcsny() + "',");
        sb.append("'fqbz':'" + this.getFqbz() + "',");
        sb.append("'mqxm':'" + this.getMqxm() + "',");
        sb.append("'mqgzdw':'" + this.getMqgzdw() + "',");
        sb.append("'mqlxdh':'" + this.getMqlxdh() + "',");
        sb.append("'mqzzmm':'" + this.getMqzzmm() + "',");
        sb.append("'mqcsny':'" + this.getMqcsny() + "',");
        sb.append("'mqbz':'" + this.getMqbz() + "',");
        sb.append("'jfrxm':'" + this.getJfrxm() + "',");
        sb.append("'jfrgx':'" + this.getJfrgx() + "',");
        sb.append("'jfrlxdh':'" + this.getJfrlxdh() + "',");
        sb.append("'jfrhkszd':'" + this.getJfrhkszd() + "',");
        sb.append("'jfrxzz':'" + this.getJfrxzz() + "',");
        sb.append("'txdz':'" + this.getTxdz() + "',");
        sb.append("'yzbm':'" + this.getYzbm() + "',");
        sb.append("'zp':'" + this.getZp() + "',");
        sb.append("'yxxmc':'" + this.getYxxmc() + "',");
        sb.append("'czxh':'" + this.getCzxh() + "',");
        sb.append("'lqlb':'" + this.getLqlb() + "',");
        sb.append("'zxs':'" + this.getZxs() + "',");
        sb.append("'zds':'" + this.getZds() + "',");
        sb.append("'sbjd':'" + this.getSbjd() + "',");
        sb.append("'xjzt':'" + this.getXjzt() + "',");
        sb.append("'sfzx':'" + this.getSfzx() + "',");
        sb.append("'pycc':'" + this.getPycc() + "',");
        sb.append("'tc':'" + this.getTc() + "',");
        sb.append("'jtdz':'" + this.getJtdz() + "',");
        sb.append("'lxdh':'" + this.getLxdh() + "',");
        sb.append("'dzxx':'" + this.getDzxx() + "',");
        sb.append("'yh':'" + this.getYh() + "',");
        sb.append("'thzh':'" + this.getThzh() + "',");
        sb.append("'kh':'" + this.getKh() + "',");
        sb.append("'syjdxz':'" + this.getSyjdxz() + "',");
        sb.append("'bz':'" + this.getBz() + "',");
        sb.append("'dszybz':'" + this.getDszybz() + "',");
        sb.append("'sfsgxqjy':'" + this.getSfsgxqjy() + "',");
        sb.append("'sflsqt':'" + this.getSflsqt() + "',");
        sb.append("'sfxysqzz':'" + this.getSfxysqzz() + "',");
        sb.append("'sfxsyb':'" + this.getSfxsyb() + "',");
        sb.append("'sfge':'" + this.getSfge() + "',");
        sb.append("'sflshyfzn':'" + this.getSflshyfzn() + "',");
        sb.append("'sfjcfgrysqzn':'" + this.getSfjcfgrysqzn() + "',");
        sb.append("'sfyzfgmxw':'" + this.getSfyzfgmxw() + "'");
        sb.append("}");
        return sb.toString();
    }

}
