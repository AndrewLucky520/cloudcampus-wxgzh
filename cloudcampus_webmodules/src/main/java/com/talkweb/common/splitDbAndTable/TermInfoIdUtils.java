package com.talkweb.common.splitDbAndTable;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.commondata.service.AllCommonDataService;

public class TermInfoIdUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(TermInfoIdUtils.class);
	
	static ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	public static  String FIRST_TERMINFOID =  rbConstant.getString("firstTermInfoId");
	 
	
	public static String increaseTermInfo(String termInfo) {
		if (!StringUtils.isNumeric(termInfo)) {
			throw new CommonRunException(-1, "学年学期传递错误，请联系管理员！");
		}
		int termInfoInt = Integer.parseInt(termInfo);
		int xn = termInfoInt / 10;
		int xq = termInfoInt % 10;
		if (xq == 1) {
			return String.valueOf(xn) + String.valueOf(++xq);
		} else {
			return String.valueOf(++xn) + String.valueOf(--xq);
		}
	}

	public static String decreaseTermInfo(String termInfo) {
		if (!StringUtils.isNumeric(termInfo)) {
			throw new CommonRunException(-1, "学年学期传递错误，请联系管理员！");
		}
		int termInfoInt = Integer.parseInt(termInfo);
		int xn = termInfoInt / 10;
		int xq = termInfoInt % 10;
		if (xq == 1) {
			return String.valueOf(--xn) + String.valueOf(++xq);
		} else {
			return String.valueOf(xn) + String.valueOf(--xq);
		}
	}

	public static List<String> getAllTermInfoIds(String curTermInfoId) {
		return getAllTermInfoIds(curTermInfoId, FIRST_TERMINFOID);
	}

	public static List<String> getAllTermInfoIds(String curTermInfoId, String first_db_termInfoId) {
		List<String> termInfoIdList = new ArrayList<String>();
		String termInfoId = curTermInfoId;
		if (compare(termInfoId, first_db_termInfoId) < 0) {
			return termInfoIdList;
		}
		while (true) {
			termInfoIdList.add(termInfoId);
			termInfoId = decreaseTermInfo(termInfoId);
			if (compare(termInfoId, first_db_termInfoId) < 0) {
				break;
			}
		}
		return termInfoIdList;
	}

	public static List<String> getUserAllTermInfoIdsByClassId(AllCommonDataService commonDataService, Long schoolId,
			Long classId, String termInfoId) {
		return getUserAllTermInfoIdsByClassId(commonDataService, schoolId, classId, termInfoId, FIRST_TERMINFOID);
	}

	/**
	 * 通过班级代码当前学生的获取所有学年学期
	 * 
	 * @param commonDataService
	 * @param schoolId
	 *            学校代码
	 * @param classId
	 *            学生所在班级代码
	 * @param termInfoId
	 *            当前学年学期
	 * @param first_db_termInfoId
	 *            按学年学期分库后第一个学年学期
	 * @return
	 */
	public static List<String> getUserAllTermInfoIdsByClassId(AllCommonDataService commonDataService, Long schoolId,
			Long classId, String termInfoId, String first_db_termInfoId) {
		List<String> termInfoIds = new ArrayList<String>();

		if (compare(termInfoId, first_db_termInfoId) < 0) {
			return termInfoIds;
		}

		Classroom classroom = commonDataService.getClassById(schoolId, classId, termInfoId);
		if (classroom == null) {
			throw new CommonRunException(-1, "SDK异常，无法从基础数据获取到班级信息！");
		}
		Long gradeId = classroom.getGradeId();
		Grade grade = commonDataService.getGradeById(schoolId, gradeId, termInfoId);
		if (grade == null) {
			throw new CommonRunException(-1, "SDK异常，无法从基础数据获取到年级信息！");
		}
		String xn = termInfoId.substring(0, termInfoId.length() - 1);
		// 获取入学年度，入学的那年
		String rxnd = commonDataService.ConvertNJDM2RXND(String.valueOf(grade.getCurrentLevel().getValue()), xn);
		// 入学的学年学期
		String firstTermInfoId = rxnd + "1";
		if (compare(firstTermInfoId, first_db_termInfoId) < 0) {
			firstTermInfoId = first_db_termInfoId;
		}
		while (true) {
			termInfoIds.add(termInfoId);
			termInfoId = decreaseTermInfo(termInfoId);
			if (compare(termInfoId, firstTermInfoId) < 0) {
				break;
			}
		}
		return termInfoIds;
	}

	public static List<String> getUserAllTermInfoIdsByUsedGrade(AllCommonDataService commonDataService,
			String usedGrade, String termInfoId) {
		
			logger.debug("=========" + FIRST_TERMINFOID);
		 
		   ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
		  String FIRST_TERMINFOID2 =  rbConstant.getString("firstTermInfoId");
		  logger.debug("=========" + FIRST_TERMINFOID2);
			
		 List<String> list = getUserAllTermInfoIdsByUsedGrade(commonDataService, usedGrade, termInfoId, FIRST_TERMINFOID2);
		 logger.debug("======list" + list);
		return list ;//getUserAllTermInfoIdsByUsedGrade(commonDataService, usedGrade, termInfoId, FIRST_TERMINFOID);
	}
	
	public static List<String> getUserAllTermInfoIdsByUsedGrade(AllCommonDataService commonDataService,
			String usedGrade, String termInfoId, String first_db_termInfoId) {
		List<String> termInfoIds = new ArrayList<String>();

		if (compare(termInfoId, first_db_termInfoId) < 0) {
			return termInfoIds;
		}

		String xn = termInfoId.substring(0, termInfoId.length() - 1);
		// 获取入学年度，入学的那年
		String rxnd = commonDataService.ConvertSYNJ2RXND(usedGrade, xn);
		// 入学的学年学期
		String firstTermInfoId = rxnd + "1";
		if (compare(firstTermInfoId, first_db_termInfoId) < 0) {
			firstTermInfoId = first_db_termInfoId;
		}
		while (true) {
			termInfoIds.add(termInfoId);
			termInfoId = decreaseTermInfo(termInfoId);
			if (compare(termInfoId, firstTermInfoId) < 0) {
				break;
			}
		}
		return termInfoIds;
	}

	public static List<String> getUserAllTermInfoIdsByAccId(AllCommonDataService commonDataService, Long schoolId,
			Long accId, String termInfoId) {
		return getUserAllTermInfoIdsByAccId(commonDataService, schoolId, accId, termInfoId, FIRST_TERMINFOID);
	}

	/**
	 * @param commonDataService
	 * @param schoolId
	 *            学校代码
	 * @param accId
	 *            学生代码
	 * @param termInfoId
	 *            当前学年学期
	 * @param first_db_termInfoId
	 *            按学年学期分库后第一个学年学期
	 * @return
	 */
	public static List<String> getUserAllTermInfoIdsByAccId(AllCommonDataService commonDataService, Long schoolId,
			Long accId, String termInfoId, String first_db_termInfoId) {
		List<Long> accIds = new ArrayList<Long>(1);
		accIds.add(accId);
		List<Account> accList = commonDataService.getAccountBatch(schoolId, accIds, termInfoId);
		if(CollectionUtils.isEmpty(accList)) {
			throw new CommonRunException(-1, "SDK异常，无法从基础数据获取到学生信息！");
		}
		Account acc = accList.get(0);
		Long classId = null;
		for (User user : acc.getUsers()) {
			if (T_Role.Student.equals(user.getUserPart().getRole())) {
				classId = user.getStudentPart().getClassId();
				break;
			}
		}
		return getUserAllTermInfoIdsByClassId(commonDataService, schoolId, classId, termInfoId, first_db_termInfoId);
	}

	public static int compare(String termInfo1, String termInfo2) {
		if (!StringUtils.isNumeric(termInfo1) || !StringUtils.isNumeric(termInfo2)) {
			throw new CommonRunException(-1, "学年学期传递错误，请联系管理员！");
		}
		return Integer.compare(Integer.parseInt(termInfo1), Integer.parseInt(termInfo2));
	}
}
