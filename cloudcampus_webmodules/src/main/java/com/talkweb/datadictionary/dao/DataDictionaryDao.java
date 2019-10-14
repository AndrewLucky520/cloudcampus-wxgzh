package com.talkweb.datadictionary.dao;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.datadictionary.domain.TDmBjlx;
import com.talkweb.datadictionary.domain.TDmByqx;
import com.talkweb.datadictionary.domain.TDmBzlb;
import com.talkweb.datadictionary.domain.TDmCjrlx;
import com.talkweb.datadictionary.domain.TDmFjyt;
import com.talkweb.datadictionary.domain.TDmGatqw;
import com.talkweb.datadictionary.domain.TDmGjdq;
import com.talkweb.datadictionary.domain.TDmGwzy;
import com.talkweb.datadictionary.domain.TDmGx;
import com.talkweb.datadictionary.domain.TDmHkxz;
import com.talkweb.datadictionary.domain.TDmHyzk;
import com.talkweb.datadictionary.domain.TDmJdfs;
import com.talkweb.datadictionary.domain.TDmJg;
import com.talkweb.datadictionary.domain.TDmJkzk;
import com.talkweb.datadictionary.domain.TDmJxsyxz;
import com.talkweb.datadictionary.domain.TDmJyjg;
import com.talkweb.datadictionary.domain.TDmJzdxz;
import com.talkweb.datadictionary.domain.TDmKmlx;
import com.talkweb.datadictionary.domain.TDmKslb;
import com.talkweb.datadictionary.domain.TDmLoglx;
import com.talkweb.datadictionary.domain.TDmLqbcdm;
import com.talkweb.datadictionary.domain.TDmMz;
import com.talkweb.datadictionary.domain.TDmMzzzx;
import com.talkweb.datadictionary.domain.TDmNj;
import com.talkweb.datadictionary.domain.TDmPycc;
import com.talkweb.datadictionary.domain.TDmRxfs;
import com.talkweb.datadictionary.domain.TDmSbjd;
import com.talkweb.datadictionary.domain.TDmSfzjlxm;
import com.talkweb.datadictionary.domain.TDmSxxfs;
import com.talkweb.datadictionary.domain.TDmSzdjjsx;
import com.talkweb.datadictionary.domain.TDmSzdlb;
import com.talkweb.datadictionary.domain.TDmWjlb;
import com.talkweb.datadictionary.domain.TDmXb;
import com.talkweb.datadictionary.domain.TDmXjzt;
import com.talkweb.datadictionary.domain.TDmXl;
import com.talkweb.datadictionary.domain.TDmXslb;
import com.talkweb.datadictionary.domain.TDmXsly;
import com.talkweb.datadictionary.domain.TDmXw;
import com.talkweb.datadictionary.domain.TDmXx;
import com.talkweb.datadictionary.domain.TDmXxbb;
import com.talkweb.datadictionary.domain.TDmXxlb;
import com.talkweb.datadictionary.domain.TDmXxly;
import com.talkweb.datadictionary.domain.TDmXyzjm;
import com.talkweb.datadictionary.domain.TDmYdlb;
import com.talkweb.datadictionary.domain.TDmZc;
import com.talkweb.datadictionary.domain.TDmZw;
import com.talkweb.datadictionary.domain.TDmZzmm;
import com.talkweb.system.domain.business.TGmScoreleveltemplatename;
/**
 * @ClassName DataDictionaryDao
 * @author Homer
 * @version 1.0
 * @Description 数据字典Dao
 * @date 2015年3月3日
 */
@Repository
public class DataDictionaryDao extends MyBatisBaseDaoImpl{

	/**
	 * @return 政区划码
	 */
	public List<TDmJg> getTDMJG() {
		
		List<TDmJg> list = selectList("getTDMJG");
		return list;
	}
	
	/**
	 * @return 地区类别码
	 */
	public List<TDmSzdlb> getTDMSZDLB() {
		List<TDmSzdlb> list = selectList("getTDMSZDLB");
		return list;
	}
	
	/**
	 * @return 经济属性码
	 */
	public List<TDmSzdjjsx> getTDMSZDJJSX() {
		List<TDmSzdjjsx> list = selectList("getTDMSZDJJSX");
		return list;
	}
	
	/**
	 * @return 民族属性
	 */
	public List<TDmMzzzx> getTDMMZZZX() {
		List<TDmMzzzx> list = selectList("getTDMMZZZX");
		return list;
	}
	
	/**
	 * @return 学校办别
	 */
	public List<TDmXxbb> getTDMXXBB() {
		List<TDmXxbb> list = selectList("getTDMXXBB");
		return list;
	}
	
	/**
	 * @return 办学类型
	 */
	public List<TDmXxlb> getTDMXXLB() {
		List<TDmXxlb> list = selectList("getTDMXXLB");
		return list;
	}
	
	/**
	 * @return 系统日志操作类型
	 */
	public List<TDmLoglx> getTDMLoglx() {
		List<TDmLoglx> list = selectList("getTDMLoglx");
		return list;
	} 
	
	/**
	 * @return 学习领域
	 */
	public List<TDmXxly> getTDMXXLY() {
		List<TDmXxly> list = selectList("getTDMXXLY");
		return list;
	} 
	
	/** 
	 * @return 科目类型
	 */
	public List<TDmKmlx> getTDMKMLX() {
		List<TDmKmlx> list = selectList("getTDMKMLX");
		return list;
	} 
	
	/**
	 * @return 建筑物使用类型
	 */
	public List<TDmFjyt> getTDMFJYT() {
		List<TDmFjyt> list = selectList("getTDMFJYT");
		return list;
	} 
	
	/**
	 * @return 教学场地使用性质
	 */
	public List<TDmJxsyxz> getTDMJXSYXZ() {
		List<TDmJxsyxz> list = selectList("getTDMJXSYXZ");
		return list;
	} 
	
	/**
	 * @return 班级类型
	 */
	public List<TDmBjlx> getTDMBJLX() {
		List<TDmBjlx> list = selectList("getTDMBJLX");
		return list;
	} 
	
	/**
	 * @return 学习阶段
	 */
	public List<TDmPycc> getTDMPYCC() {
		List<TDmPycc> list = selectList("getTDMPYCC");
		return list;
	} 
	
	/**
	 * @return 年级
	 */
	public List<TDmNj> getTDMNJ() {
		List<TDmNj> list = selectList("getTDMNJ");
		return list;
	} 
	
	/**
	 * @return 入学方式
	 */
	public List<TDmRxfs> getTDMRXFS() {
		List<TDmRxfs> list = selectList("getTDMRXFS");
		return list;
	} 
	
	/**
	 * @return 就读方式
	 */
	public List<TDmJdfs> getTDMJDFS() {
		List<TDmJdfs> list = selectList("getTDMJDFS");
		return list;
	} 
	
	/**
	 * @return 学生类别
	 */
	public List<TDmXslb> getTDMXSLB() {
		List<TDmXslb> list = selectList("getTDMXSLB");
		return list;
	} 
	
	/**
	 * @return 身份证件类型
	 */
	public List<TDmSfzjlxm> getTDMSFZJLXM() {
		List<TDmSfzjlxm> list = selectList("getTDMSFZJLXM");
		return list;
	} 
	
	/**
	 * @return 性别
	 */
	public List<TDmXb> getTDMXB() {
		List<TDmXb> list = selectList("getTDMXB");
		return list;
	} 
	
	/**
	 * @return 名族
	 */
	public List<TDmMz> getTDMMZ() {
		List<TDmMz> list = selectList("getTDMMZ");
		return list;
	} 
	
	/**
	 * @return 国籍、地区
	 */
	public List<TDmGjdq> getTDMGJDQ() {
		List<TDmGjdq> list = selectList("getTDMGJDQ");
		return list;
	} 
	
	/**
	 * @return 港澳台侨外
	 */
	public List<TDmGatqw> getTDMGATQW() {
		List<TDmGatqw> list = selectList("getTDMGATQW");
		return list;
	} 
	
	/**
	 * @return 健康状况
	 */
	public List<TDmJkzk> getTDMJKZK() {
		List<TDmJkzk> list = selectList("getTDMJKZK");
		return list;
	} 
	
	/**
	 * @return 信仰宗教码
	 */
	public List<TDmXyzjm> getTDMXYZJM() {
		List<TDmXyzjm> list = selectList("getTDMXYZJM");
		return list;
	} 
	
	/**
	 * @return 政治面貌
	 */
	public List<TDmZzmm> getTDMZZMM() {
		List<TDmZzmm> list = selectList("getTDMZZMM");
		return list;
	} 
	
	/**
	 * @return 户口性质
	 */
	public List<TDmHkxz> getTDMHKXZ() {
		List<TDmHkxz> list = selectList("getTDMHKXZ");
		return list;
	} 
	
	/**
	 * @return 上下学方式
	 */
	public List<TDmSxxfs> getTDMSXXFS() {
		List<TDmSxxfs> list = selectList("getTDMSXXFS");
		return list;
	} 
	
	/**
	 * @return 血型
	 */
	public List<TDmXx> getTDMXX() {
		List<TDmXx> list = selectList("getTDMXX");
		return list;
	} 
	
	/**
	 * @return 学生来源
	 */
	public List<TDmXsly> getTDMXSLY() {
		List<TDmXsly> list = selectList("getTDMXSLY");
		return list;
	} 
	
	/**
	 * @return 残疾类型
	 */
	public List<TDmCjrlx> getTDMCJRLX() {
		List<TDmCjrlx> list = selectList("getTDMCJRLX");
		return list;
	} 
	
	/**
	 * @return 随班就读
	 */
	public List<TDmSbjd>  getTDMSBJD() {
		List<TDmSbjd> list = selectList("getTDMSBJD");
		return list;
	} 
	
	/**
	 * @return 学籍状态
	 */
	public List<TDmXjzt> getTDMXJZT() {
		List<TDmXjzt> list = selectList("getTDMXJZT");
		return list;
	} 
	
	/**
	 * @return 生源居地性质
	 */
	public List<TDmJzdxz> getTDMJZDXZ() {
		List<TDmJzdxz> list = selectList("getTDMJZDXZ");
		return list;
	} 
	
	/**
	 * @return 家庭关系码
	 */
	public List<TDmGx> getTDMGX() {
		List<TDmGx> list = selectList("getTDMGX");
		return list;
	} 
	
	/**
	 * @return 学历
	 */
	public List<TDmXl> getTDMXL() {
		List<TDmXl> list = selectList("getTDMXL");
		return list;
	} 
	
	/**
	 * @return 异动类型
	 */
	public List<TDmYdlb> getTDMYDLB() {
		List<TDmYdlb> list = selectList("getTDMYDLB");
		return list;
	} 
	
	/**
	 * @return 结束学业码
	 */
	public List<TDmJyjg> getTDMJYJG() {
		List<TDmJyjg> list = selectList("getTDMJYJG");
		return list;
	} 
	
	/**
	 * @return 毕业去向
	 */
	public List<TDmByqx> getTDMBYQX() {
		List<TDmByqx> list = selectList("getTDMBYQX");
		return list;
	} 
	
	/**
	 * @return 录取批次
	 */
	public List<TDmLqbcdm> getTDMLQBCDM() {
		List<TDmLqbcdm> list = selectList("getTDMLQBCDM");
		return list;
	} 
	
	/**
	 * @return 婚姻状况
	 */
	public List<TDmHyzk> getTDMHYZK() {
		List<TDmHyzk> list = selectList("getTDMHYZK");
		return list;
	} 
	
	/**
	 * @return 编制类别
	 */
	public List<TDmBzlb> getTDMBZLB() {
		List<TDmBzlb> list = selectList("getTDMBZLB");
		return list;
	} 
	
	/**
	 * @return 岗位职业
	 */
	public List<TDmGwzy> getTDMGWZY() {
		List<TDmGwzy> list = selectList("getTDMGWZY");
		return list;
	} 
	
	/**
	 * @return 学位
	 */
	public List<TDmXw> getTDMXW() {
		List<TDmXw> list = selectList("getTDMXW");
		return list;
	} 
	
	/**
	 * @return 职务
	 */
	public List<TDmZw> getTDMZW() {
		List<TDmZw> list = selectList("getTDMZW");
		return list;
	} 
	
	/**
	 * @return 职称
	 */
	public List<TDmZc> getTDMZC() {
		List<TDmZc> list = selectList("getTDMZC");
		return list;
	} 
	
	/**
	 * @return 考试类别
	 */
	public List<TDmKslb> getTDMKSLB() {
		List<TDmKslb> list = selectList("getTDMKSLB");
		return list;
	} 
	
	/**
	 * @return 教学资源类别
	 */
	public List<TDmWjlb> getTDMWJLB() {
		List<TDmWjlb> list = selectList("getTDMWJLB");
		return list;
	}

    public List<HashMap<String,Object>> getTDMRJKM() {
        // TODO Auto-generated method stub
        List<HashMap<String,Object>> list = selectList("getTDMRJKM");
        return list;
    } 
    
	public List<TGmScoreleveltemplatename> getTGmScoreleveltemplatename() {
		List<TGmScoreleveltemplatename> list = selectList("getTGmScoreleveltemplatename");
		return list;
	}
    
	
}
