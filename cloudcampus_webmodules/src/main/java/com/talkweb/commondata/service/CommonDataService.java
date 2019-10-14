package com.talkweb.commondata.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.T_ClassType;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.datadictionary.domain.TDmBjlx;

/**
 * @ClassName CommonDataService
 * @author zhanghuihui
 * @version 1.0
 * @Description 公共基础数据接口
 * @date 2017年2月15日
 */
@Service
public class CommonDataService {
	private static final Logger logger = LoggerFactory
			.getLogger(CommonDataService.class);
	/**
	 * 通过年级代码检索培养层次
	 * 
	 * @param njCode
	 *            年级代码
	 * @return 培养层次
	 */
	// public String getPYCCByNJCode(String njCode){
	// String pycc = selectOne("getPYCCByNJCode", njCode);
	// return pycc;
	// }
	/**
	 * 得到培养层次通过年级代码
	 * 
	 * @param njCode
	 *            Grade的gradeLevel的值
	 * @return
	 */
	public Integer getPYCCByNJCode(long njCode) {
		if (njCode <= T_GradeLevel.T_PrimarySix.getValue()) {
			return 1;
		} else if (njCode >= T_GradeLevel.T_JuniorOne.getValue()
				&& njCode <= T_GradeLevel.T_JuniorThree.getValue()) {
			return 2;
		} else if (njCode >= T_GradeLevel.T_HighOne.getValue()
				&& njCode <= T_GradeLevel.T_HighThree.getValue()) {
			return 3;
		} else {
			logger.error("【基础数据接口】获取培养层次[为空]，方法getPYCCByNJCode，入参njCode:{}",njCode);
			return null;
		}
	}
	public String getSynjByGrade(Grade grade, String xn) {
		String njdm = grade.getCurrentLevel().getValue() + "";
		String synj =this.ConvertNJDM2SYNJ(njdm, xn);

		return synj;
	}
	/**
	 * 通过培养层次检索起始年级
	 * 
	 * @param pycc
	 *            培养层次
	 * @return 起始年级
	 */
	// public String getQSNJByPYCC(String pycc){
	// String qsnj = selectOne("getQSNJByPYCC", pycc);
	// return qsnj;
	// }

	public Integer getQSNJByPYCC(int pycc) {
		Integer qsnj = null;
		switch (pycc) {
		case 1:
			qsnj = 0;
			break;
		case 2:
			qsnj = 6;
			break;
		case 3:
			qsnj = 9;
			break;
		default:
			break;
		}
//		qsnj--;
		return qsnj;
	}

	/**
	 * 得到所有的班级类型，深圳的枚举，
	 * 
	 * @return
	 */
	public List<TDmBjlx> getClassTypeList() {
		List<TDmBjlx> list = new ArrayList<TDmBjlx>();
		TDmBjlx bjlx = new TDmBjlx();
		bjlx.setDm(T_ClassType.Arts.getValue() + "");
		bjlx.setMc("文科");
		list.add(bjlx);
		bjlx = new TDmBjlx();
		bjlx.setDm(T_ClassType.Science.getValue() + "");
		bjlx.setMc("理科");
		list.add(bjlx);
		bjlx = new TDmBjlx();
		bjlx.setDm(T_ClassType.multiple.getValue() + "");
		bjlx.setMc("综合");
		list.add(bjlx);
		return list;
	}

	/**
	 * （使用年级、学年）转入学年度
	 * 
	 * @param synj
	 *            使用年级
	 * @param xn
	 *            学年
	 * @return 入学年度
	 */
	public String ConvertSYNJ2RXND(String synj, String xn) {

		// 年级代码
		long njCode = Integer.valueOf(xn) - Integer.valueOf(synj) + 10;
		// 培养层次
		Integer pycc = getPYCCByNJCode(njCode);
		Integer qsnj = null;
		if (null != pycc) {
			// 起始年级
			qsnj = getQSNJByPYCC(pycc);
		}
		String rxnd = null;
		// 入学年度=学年-起始年级
		if (null != qsnj) {
			rxnd = String
					.valueOf(Integer.valueOf(synj) + Integer.valueOf(qsnj));
		} else {
			rxnd = "";
			logger.error("【基础数据接口】使用年级转入学年度[为空]，方法ConvertSYNJ2RXND，入参synj:{},xn{}",synj,xn);
		}

		return rxnd;
	}

	/**
	 * （入学年度、培养层次）转使用年级
	 * 
	 * @param rxnd
	 *            入学年度
	 * @param pycc
	 *            培养层次
	 * @return 使用年级
	 */
	public String ConvertRXND2SYNJ(String rxnd, String pycc) {

		// 起始年级
		Integer qsnj = getQSNJByPYCC(Integer.valueOf(pycc));
		// 使用年级
		String synj = String.valueOf(Integer.valueOf(rxnd)
				- Integer.valueOf(qsnj));

		return synj;
	}

	/**
	 * （使用年级、学年）转年级代码
	 * 
	 * @param synj
	 *            使用年级
	 * @param xn
	 *            当前学年
	 * @return 年级代码 为Grade的gradeLevel的值
	 */
	public String ConvertSYNJ2NJDM(String synj, String xn) {

		String njdm = String.valueOf(Integer.valueOf(xn)
				- Integer.valueOf(synj) + 10);
		return njdm;
	}

	/**
	 * (年级代码、学年）转使用年级
	 * 
	 * @param njdm
	 *            年级代码 Grade.getCreateLevel.getValue()的值
	 * @param xn
	 *            当前学年
	 * @return 使用年级
	 */
	public String ConvertNJDM2SYNJ(String njdm, String xn) {

		String synj = String.valueOf(Integer.valueOf(xn)
				- Integer.valueOf(njdm) + 10);
		return synj;
	}

	/**
	 * （入学年度、学年、培养层次）转年级代码
	 * 
	 * @param rxnd
	 *            入学年度
	 * @param xn
	 *            当前学年
	 * @param pycc
	 *            培养层次
	 * @return 年级代码 为Grade的gradeLevel的值
	 */
	public String ConvertRXND2NJDM(String rxnd, String xn, String pycc) {

		String synj = ConvertRXND2SYNJ(rxnd, pycc);
		String njdm = ConvertSYNJ2NJDM(synj, xn);
		return njdm;
	}

	/**
	 * （年级代码、学年）转入学年度
	 * 
	 * @param njdm
	 *            年级代码 Grade的gradeLevel的值
	 * @param xn
	 *            当前学年
	 * @return 入学年度
	 */
	public String ConvertNJDM2RXND(String njdm, String xn) {

		String synj = ConvertNJDM2SYNJ(njdm, xn);
		String rxnd = ConvertSYNJ2RXND(synj, xn);
		return rxnd;

	}

	/**
	 * （使用年级、学年）获取培养层次
	 * 
	 * @param synj
	 *            使用年级
	 * @param xn
	 *            学年
	 * @return 培养层次
	 */
	public String getPYCCBySYNJ(String synj, String xn) {

		String njdm = ConvertSYNJ2NJDM(synj, xn);
		Integer pycc = getPYCCByNJCode(Integer.valueOf(njdm));
		if (null != pycc) {
			return pycc + "";
		} else {
			
			logger.error("【基础数据接口】使用年级转培养层次[为空]，方法getPYCCBySYNJ，入参synj:{},xn{}",synj,xn);
			return "";
		}

	}


}
