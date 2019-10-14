package com.talkweb.datadictionary.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.talkweb.datadictionary.dao.DataDictionaryDao;
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

/**
 * @ClassName DataDictionaryAction
 * @author Homer
 * @version 1.0
 * @Description 数据字典Action
 * @date 2015年3月3日
 */
@Controller
@RequestMapping(value="/datadictionary/")
public class DataDictionaryAction {

	@Autowired
	private DataDictionaryDao dataDictionaryDao;
	
	/**
	 * @return 政区划码
	 */
	@RequestMapping(value="getTDMJG")
	@ResponseBody
	public List<TDmJg> getTDMJG() {
		return dataDictionaryDao.getTDMJG();
	}
	
	/**
	 * @return 地区类别码
	 */
	@RequestMapping(value="getTDMSZDLB")
	@ResponseBody
	public List<TDmSzdlb> getTDMSZDLB() {
		return dataDictionaryDao.getTDMSZDLB();
	}
	
	/**
	 * @return 经济属性码
	 */
	@RequestMapping(value="getTDMSZDJJSX")
	@ResponseBody
	public List<TDmSzdjjsx> getTDMSZDJJSX() {
		return dataDictionaryDao.getTDMSZDJJSX();
	}
	
	/**
	 * @return 民族属性
	 */
	@RequestMapping(value="getTDMMZZZX")
	@ResponseBody
	public List<TDmMzzzx> getTDMMZZZX() {
		return dataDictionaryDao.getTDMMZZZX();
	}
	
	/**
	 * @return 学校办别
	 */
	@RequestMapping(value="getTDMXXBB")
	@ResponseBody
	public List<TDmXxbb> getTDMXXBB() {
		return dataDictionaryDao.getTDMXXBB();
	}
	
	/**
	 * @return 办学类型
	 */
	@RequestMapping(value="getTDMXXLB")
	@ResponseBody
	public List<TDmXxlb> getTDMXXLB() {
		return dataDictionaryDao.getTDMXXLB();
	}
	
	/**
	 * @return 系统日志操作类型
	 */
	@RequestMapping(value="getTDMLoglx")
	@ResponseBody
	public List<TDmLoglx> getTDMLoglx() {
		return dataDictionaryDao.getTDMLoglx();
	} 
	
	/**
	 * @return 学习领域
	 */
	@RequestMapping(value="getTDMXXLY")
	@ResponseBody
	public List<TDmXxly> getTDMXXLY() {
		return dataDictionaryDao.getTDMXXLY();
	} 
	
	/** 
	 * @return 科目类型
	 */
	@RequestMapping(value="getTDMKMLX")
	@ResponseBody
	public List<TDmKmlx> getTDMKMLX() {
		return dataDictionaryDao.getTDMKMLX();
	} 
	
	/**
	 * @return 建筑物使用类型
	 */
	@RequestMapping(value="getTDMFJYT")
	@ResponseBody
	public List<TDmFjyt> getTDMFJYT() {
		return dataDictionaryDao.getTDMFJYT();
	} 
	
	/**
	 * @return 教学场地使用性质
	 */
	@RequestMapping(value="getTDMJXSYXZ")
	@ResponseBody
	public List<TDmJxsyxz> getTDMJXSYXZ() {
		return dataDictionaryDao.getTDMJXSYXZ();
	} 
	
	/**
	 * @return 班级类型
	 */
	@RequestMapping(value="getTDMBJLX")
	@ResponseBody
	public List<TDmBjlx> getTDMBJLX() {
		return dataDictionaryDao.getTDMBJLX();
	} 
	
	/**
	 * @return 学习阶段
	 */
	@RequestMapping(value="getTDMPYCC")
	@ResponseBody
	public List<TDmPycc> getTDMPYCC() {
		return dataDictionaryDao.getTDMPYCC();
	} 
	
	/**
	 * @return 年级
	 */
	@RequestMapping(value="getTDMNJ")
	@ResponseBody
	public List<TDmNj> getTDMNJ() {
		return dataDictionaryDao.getTDMNJ();
	} 
	
	/**
	 * @return 入学方式
	 */
	@RequestMapping(value="getTDMRXFS")
	@ResponseBody
	public List<TDmRxfs> getTDMRXFS() {
		return dataDictionaryDao.getTDMRXFS();
	} 
	
	/**
	 * @return 就读方式
	 */
	@RequestMapping(value="getTDMJDFS")
	@ResponseBody
	public List<TDmJdfs> getTDMJDFS() {
		return dataDictionaryDao.getTDMJDFS();
	} 
	
	/**
	 * @return 学生类别
	 */
	@RequestMapping(value="getTDMXSLB")
	@ResponseBody
	public List<TDmXslb> getTDMXSLB() {
		return dataDictionaryDao.getTDMXSLB();
	} 
	
	/**
	 * @return 身份证件类型
	 */
	@RequestMapping(value="getTDMSFZJLXM")
	@ResponseBody
	public List<TDmSfzjlxm> getTDMSFZJLXM() {
		return dataDictionaryDao.getTDMSFZJLXM();
	} 
	
	/**
	 * @return 性别
	 */
	@RequestMapping(value="getTDMXB")
	@ResponseBody
	public List<TDmXb> getTDMXB() {
		return dataDictionaryDao.getTDMXB();
	} 
	
	/**
	 * @return 名族
	 */
	@RequestMapping(value="getTDMMZ")
	@ResponseBody
	public List<TDmMz> getTDMMZ() {
		return dataDictionaryDao.getTDMMZ();
	} 
	
	/**
	 * @return 国籍、地区
	 */
	@RequestMapping(value="getTDMGJDQ")
	@ResponseBody
	public List<TDmGjdq> getTDMGJDQ() {
		return dataDictionaryDao.getTDMGJDQ();
	} 
	
	/**
	 * @return 港澳台侨外
	 */
	@RequestMapping(value="getTDMGATQW")
	@ResponseBody
	public List<TDmGatqw> getTDMGATQW() {
		return dataDictionaryDao.getTDMGATQW();
	} 
	
	/**
	 * @return 健康状况
	 */
	@RequestMapping(value="getTDMJKZK")
	@ResponseBody
	public List<TDmJkzk> getTDMJKZK() {
		return dataDictionaryDao.getTDMJKZK();
	} 
	
	/**
	 * @return 信仰宗教码
	 */
	@RequestMapping(value="getTDMXYZJM")
	@ResponseBody
	public List<TDmXyzjm> getTDMXYZJM() {
		return dataDictionaryDao.getTDMXYZJM();
	} 
	
	/**
	 * @return 政治面貌
	 */
	@RequestMapping(value="getTDMZZMM")
	@ResponseBody
	public List<TDmZzmm> getTDMZZMM() {
		return dataDictionaryDao.getTDMZZMM();
	} 
	
	/**
	 * @return 户口性质
	 */
	@RequestMapping(value="getTDMHKXZ")
	@ResponseBody
	public List<TDmHkxz> getTDMHKXZ() {
		return dataDictionaryDao.getTDMHKXZ();
	} 
	
	/**
	 * @return 上下学方式
	 */
	@RequestMapping(value="getTDMSXXFS")
	@ResponseBody
	public List<TDmSxxfs> getTDMSXXFS() {
		return dataDictionaryDao.getTDMSXXFS();
	} 
	
	/**
	 * @return 血型
	 */
	@RequestMapping(value="getTDMXX")
	@ResponseBody
	public List<TDmXx> getTDMXX() {
		return dataDictionaryDao.getTDMXX();
	} 
	
	/**
	 * @return 学生来源
	 */
	@RequestMapping(value="getTDMXSLY")
	@ResponseBody
	public List<TDmXsly> getTDMXSLY() {
		return dataDictionaryDao.getTDMXSLY();
	} 
	
	/**
	 * @return 残疾类型
	 */
	@RequestMapping(value="getTDMCJRLX")
	@ResponseBody
	public List<TDmCjrlx> getTDMCJRLX() {
		return dataDictionaryDao.getTDMCJRLX();
	} 
	
	/**
	 * @return 随班就读
	 */
	@RequestMapping(value="getTDMSBJD")
	@ResponseBody
	public List<TDmSbjd> getTDMSBJD() {
		return dataDictionaryDao.getTDMSBJD();
	} 
	
	/**
	 * @return 学籍状态
	 */
	@RequestMapping(value="getTDMXJZT")
	@ResponseBody
	public List<TDmXjzt> getTDMXJZT() {
		return dataDictionaryDao.getTDMXJZT();
	} 
	
	/**
	 * @return 生源居地性质
	 */
	@RequestMapping(value="getTDMJZDXZ")
	@ResponseBody
	public List<TDmJzdxz> getTDMJZDXZ() {
		return dataDictionaryDao.getTDMJZDXZ();
	} 
	
	/**
	 * @return 家庭关系码
	 */
	@RequestMapping(value="getTDMGX")
	@ResponseBody
	public List<TDmGx> getTDMGX() {
		return dataDictionaryDao.getTDMGX();
	} 
	
	/**
	 * @return 学历
	 */
	@RequestMapping(value="getTDMXL")
	@ResponseBody
	public List<TDmXl> getTDMXL() {
		return dataDictionaryDao.getTDMXL();
	} 
	
	/**
	 * @return 异动类型
	 */
	@RequestMapping(value="getTDMYDLB")
	@ResponseBody
	public List<TDmYdlb> getTDMYDLB() {
		return dataDictionaryDao.getTDMYDLB();
	} 
	
	/**
	 * @return 结束学业码
	 */
	@RequestMapping(value="getTDMJYJG")
	@ResponseBody
	public List<TDmJyjg> getTDMJYJG() {
		return dataDictionaryDao.getTDMJYJG();
	} 
	
	/**
	 * @return 毕业去向
	 */
	@RequestMapping(value="getTDMBYQX")
	@ResponseBody
	public List<TDmByqx> getTDMBYQX() {
		return dataDictionaryDao.getTDMBYQX();
	} 
	
	/**
	 * @return 录取批次
	 */
	@RequestMapping(value="getTDMLQBCDM")
	@ResponseBody
	public List<TDmLqbcdm> getTDMLQBCDM() {
		return dataDictionaryDao.getTDMLQBCDM();
	} 
	
	/**
	 * @return 婚姻状况
	 */
	@RequestMapping(value="getTDMHYZK")
	@ResponseBody
	public List<TDmHyzk> getTDMHYZK() {
		return dataDictionaryDao.getTDMHYZK();
	} 
	
	/**
	 * @return 编制类别
	 */
	@RequestMapping(value="getTDMBZLB")
	@ResponseBody
	public List<TDmBzlb> getTDMBZLB() {
		return dataDictionaryDao.getTDMBZLB();
	} 
	
	/**
	 * @return 岗位职业
	 */
	@RequestMapping(value="getTDMGWZY")
	@ResponseBody
	public List<TDmGwzy> getTDMGWZY() {
		return dataDictionaryDao.getTDMGWZY();
	} 
	
	/**
	 * @return 学位
	 */
	@RequestMapping(value="getTDMXW")
	@ResponseBody
	public List<TDmXw> getTDMXW() {
		return dataDictionaryDao.getTDMXW();
	} 
	
	/**
	 * @return 职务
	 */
	@RequestMapping(value="getTDMZW")
	@ResponseBody
	public List<TDmZw> getTDMZW() {
		return dataDictionaryDao.getTDMZW();
	} 
	
	/**
	 * @return 职称
	 */
	@RequestMapping(value="getTDMZC")
	@ResponseBody
	public List<TDmZc> getTDMZC() {
		return dataDictionaryDao.getTDMZC();
	} 
	
	/**
	 * @return 考试类别
	 */
	@RequestMapping(value="getTDMKSLB")
	@ResponseBody
	public List<TDmKslb> getTDMKSLB() {
		return dataDictionaryDao.getTDMKSLB();
	} 
	
	/**
	 * @return 教学资源类别
	 */
	@RequestMapping(value="getTDMWJLB")
	@ResponseBody
	public List<TDmWjlb> getTDMWJLB() {
		return dataDictionaryDao.getTDMWJLB();
	} 
}
