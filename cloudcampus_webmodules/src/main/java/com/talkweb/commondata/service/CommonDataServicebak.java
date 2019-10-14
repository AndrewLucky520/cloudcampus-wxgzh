package com.talkweb.commondata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.core.Listener;
import com.talkweb.accountcenter.core.ParallelListener;
import com.talkweb.accountcenter.core.SDKClient;
import com.talkweb.accountcenter.core.SDKHeader;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.ClassStudentCon;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.ClassroomList;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.CurrentTermRsp;
import com.talkweb.accountcenter.thrift.DeanCon;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.GradeList;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.LessonList;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.SchoolClassCon;
import com.talkweb.accountcenter.thrift.SchoolOrgList;
import com.talkweb.accountcenter.thrift.SchoolStudentCon;
import com.talkweb.accountcenter.thrift.SchoolTeacherCon;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.SubjectTeacherCon;
import com.talkweb.accountcenter.thrift.T_ClassType;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_LessonType;
import com.talkweb.accountcenter.thrift.T_OrgType;
import com.talkweb.accountcenter.thrift.T_PermissionLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.T_SchoolType;
import com.talkweb.accountcenter.thrift.TermInfo;
import com.talkweb.accountcenter.thrift.TermInfoList;
import com.talkweb.accountcenter.thrift.UpdateTeacherLessonBatchRsp;
import com.talkweb.accountcenter.thrift.UpdateTeacherLessonRsp;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserAndAccount;
import com.talkweb.accountcenter.thrift.UserPermissions;
import com.talkweb.commondata.dao.CommonDataDao;
import com.talkweb.datadictionary.domain.TDmBjlx;
import com.talkweb.scoreManage.action.ScoreUtil;

/**
 * @ClassName CommonDataDao
 * @author Homer
 * @version 1.0
 * @Description 封装深圳基础数据接口，大部分返回的是深圳那边的对象
 * @date 2015年3月3日
 */
/**
 * @author talkweb
 *
 */
/**
 * @author talkweb
 *
 */
@SuppressWarnings({ "rawtypes" })
//@Service
public class CommonDataServicebak {

	@Autowired
	private CommonDataDao commonDataDao;

	private static final Logger logger = LoggerFactory
			.getLogger(CommonDataServicebak.class);
	private static SDKHeader sdkHeader = new SDKHeader();
	// private static String schoolId;
	// public static String getSchoolId() {
	// return schoolId;
	// }
	// public static void setSchoolId(String schoolId) {
	// CommonDataService.schoolId = schoolId;
	// }
	/**
	 * 学校权限等级名称
	 */
	public static final Map<T_PermissionLevel, String> SchoolPermissionLevelName = AccountStructConstants.SchoolPermissionLevelName;
	/**
	 * 平台权限等级名称
	 */
	public static final Map<T_PermissionLevel, String> PlatePermissionLevelName = AccountStructConstants.PlatePermissionLevelName;
	/**
	 * 学校类型
	 */
	public static final Map<T_SchoolType, String> SchoolTypeNames = AccountStructConstants.SchoolTypeNames;
	/**
	 * 年级名称
	 */
	public static final Map<T_GradeLevel, String> T_GradeLevelName = AccountStructConstants.T_GradeLevelName;
	/**
	 * 机构类型
	 */
	public static final Map<T_OrgType, String> DefaultOrgTypeNames = AccountStructConstants.DefaultOrgTypeNames;
	/**
	 * 科目类型
	 */
	public static final Map<T_LessonType, String> DefaultLessonTypeNames = AccountStructConstants.DefaultLessonTypeNames;

	static {
		/**
		 * 线上环境--阿里云
		 */
//		SDKClient.changedServerInfo("121.40.144.157", 12030);
		/**
		 * 线上环境--预发布平台
		 */
		SDKClient.changedServerInfo("121.40.226.180", 12030);
		
		//121内网
//		SDKClient.changedServerInfo("10.168.123.11", 12030);
		SDKClient.setSocketPoolSize(100);
		
	}

	public List<Map<String, Object>> getSystemVersion() {
		return commonDataDao.getSystemVersion();
	}

	
	/**
	 * 获取当前学年学期
	 * 
	 * @param schoolId
	 *            学校id
	 * @return CurrentTermRsp
	 */
	// @Cacheable
	public CurrentTermRsp getCurTermInfo(long schoolId) {
		sdkHeader.setSchoolId(schoolId);

		try {
			final CurrentTermRsp[] c = new CurrentTermRsp[1];
			SDKClient.getCurrentTerm(new Listener<CurrentTermRsp>() {
				@Override
				public void onResponse(CurrentTermRsp response) {
					if (response != null) {
						c[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, schoolId, sdkHeader);
			if (c[0] == null) {
				logger.error("【基础数据接口】获取当前学年学期[为空]，方法getCurrentTerm，入参schoolId："
						+ schoolId+"出参："+c[0]);
			}else{
				logger.info("【基础数据接口】获取当前学年学期 [正常]，方法getCurrentTerm，入参schoolId："
						+ schoolId);
				
			}
			return c[0];
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("【基础数据接口】获取当前学年学期报错，方法getCurrentTerm，参数schoolId："
					+ schoolId);
			return null;
		}
	}

	/**
	 * 获取学年学期
	 * 
	 * @param termId
	 *            学年学期ID
	 * @return TermInfo
	 */
	// @Cacheable
	public TermInfo getTermInfo(long termId, long schoolId) {
		sdkHeader.setSchoolId(schoolId);
		final TermInfo[] t = new TermInfo[1];

		try {
			Listener<TermInfo> listener = new Listener<TermInfo>() {
				@Override
				public void onResponse(TermInfo response) {
					if (response != null) {
						t[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			};
			SDKClient.getTermInfo(listener, termId, sdkHeader);

			if (t[0] == null) {
				logger.error("【基础数据接口】获取学年学期[为空]，方法getTermInfo，入参termId:{},schoolId： {},",termId,schoolId);
			}else{
				logger.info("【基础数据接口】获取学年学期[正常]，方法getTermInfo，入参termId:{},schoolId： {},出参：{}",termId,schoolId,t[0]);
				
			}

			return t[0];
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("【基础数据接口】获取学年学期[为空]，方法getTermInfo，入参termId:{},schoolId： {},",termId,schoolId);
			return null;
		}
	}
	/**
	 * 获取格式为 20151 格式的学年学期
	 * @param School	学校实例
	 * @return
	 */
	public String getCurrentXnxq(School sch){
		String xnxq = "";
		
		long curTermId = getCurTermInfo(sch.getId()).getTermId();
		
		List<TermInfo> terms = this.getTermInfoBatch(sch.getTermIds(), sch.getId());
		
		List<JSONObject> sorList = new ArrayList<JSONObject>();
		int minxn = 10000;
		for(TermInfo ter:terms){
			JSONObject obj = (JSONObject) JSON.toJSON(ter);
			sorList.add(obj);
			Timestamp d  = new Timestamp(ter.getStartTime()*1000);
			int now = d.getYear()+1900;
			if(now<minxn){
				minxn = now;
			}
		}
		
		ScoreUtil.sorStuScoreList(sorList, "endTime", "asc", "", "");
		int xq = -1;
		for(JSONObject obj:sorList){
			long tid = obj.getLongValue("id");
			if(tid==curTermId){
				xq = obj.getIntValue("pm");
			}
		}
		if(minxn!=10000&&xq!=-1){
			xnxq = minxn + "" + xq;
		}
		return xnxq;
	}
	/**
	 * 获取学期列表
	 * 
	 * @param termId
	 *            学年学期ID
	 * @return TermInfo
	 */
	// @Cacheable
	public List<TermInfo> getTermInfoBatch(List<Long> termIds, long schoolId) {
		sdkHeader.setSchoolId(schoolId);
		final TermInfoList[] t = new TermInfoList[1];

		try {
			Listener<TermInfoList> listener = new Listener<TermInfoList>() {
				@Override
				public void onResponse(TermInfoList response) {
					if (response != null) {
						t[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			};
			SDKClient.getTermInfoBatch(listener, termIds, sdkHeader);

			if (t[0] == null) {
				logger.error("【基础数据接口】批量获取学年学期[为空]，方法getTermInfo，入参termId:{},schoolId： {},",termIds,schoolId);
			}else{
				logger.info("【基础数据接口】批量获取学年学期[正常]，方法getTermInfo，入参termId:{},schoolId： {},出参：{}",termIds,schoolId,t[0]);
				
			}

			List<TermInfo> terms = new ArrayList<TermInfo>();
			terms.addAll(t[0].getTermInfos());
			return terms;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("【基础数据接口】批量获取学年学期[为空]，方法getTermInfo，入参termId:{},schoolId： {},",termIds,schoolId);
			return null;
		}
	}
	

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

	/**
	 * 查询年级组和教研组下面的教师信息
	 * 
	 * @param schoolId
	 *            学校ID（ 必传）
	 * @param gradeId
	 *            年级组ID（ 必传，多个用逗号分隔）
	 * @param researchId
	 *            教研组ID （必传，多个用逗号分隔）
	 * @param name
	 *            教师姓名 （可以[为空]）
	 * @return
	 */
	// @Cacheable
	public List<Account> getAllTeacherList(long schoolId, String gradeId,
			String researchId, String name) {
		// 年级组所有教师
		List<Account> gradeList = new ArrayList<Account>();
		if (StringUtils.isNotEmpty(gradeId)) {
			List<Long> gIds = new ArrayList<Long>();
			for (String id : gradeId.split(",")) {
				gIds.add(Long.valueOf(id));
			}
			List<OrgInfo> orglist = this.getSchoolOrgBatch(schoolId, gIds);
			List<Long> aIds = new ArrayList<Long>();
			for (OrgInfo o : orglist) {
				List<Long> ids = o.getMemberAccountIds();
				if (null != ids) {
					aIds.addAll(ids);
				}
			}
			gradeList = this.getAccountBatch(schoolId, aIds);
		}
		// 教研组所有教师
		List<Account> researchList = new ArrayList<Account>();
		if (StringUtils.isNotEmpty(researchId)) {
			List<Long> gIds = new ArrayList<Long>();
			for (String id : researchId.split(",")) {
				gIds.add(Long.valueOf(id));
			}
			List<OrgInfo> orglist = this.getSchoolOrgBatch(schoolId, gIds);
			List<Long> aIds = new ArrayList<Long>();
			for (OrgInfo o : orglist) {
				List<Long> ids = o.getMemberAccountIds();
				if (null != ids) {
					aIds.addAll(ids);
				}
			}
			researchList = this.getAccountBatch(schoolId, aIds);
		}
		// 如果年级组、教研组都有，则取它们的交集,
		return this.getListIntersection(gradeList, researchList, name);
	}

	/**
	 * 查询多个机构下面的教师信息
	 * 
	 * @param schoolId
	 *            学校ID（ 必传）
	 * @param orgId
	 *            机构id(多个使用逗号分隔)
	 * @param name
	 *            教师姓名(可以[为空])
	 * @return
	 */
	// @Cacheable
	public List<Account> getAllTeacherList(long schoolId, String orgId,
			String name) {
		// 年级组所有教师
		List<Account> userList = new ArrayList<Account>();

		List<Long> gIds = new ArrayList<Long>();
		for (String id : orgId.split(",")) {
			gIds.add(Long.valueOf(id));
		}
		List<OrgInfo> orglist = this.getSchoolOrgBatch(schoolId, gIds);
		List<Long> aIds = new ArrayList<Long>();
		for (OrgInfo o : orglist) {
			List<Long> ids = o.getMemberAccountIds();
			if (null != ids) {
				aIds.addAll(ids);
			}
		}
		userList = this.getAccountBatch(schoolId, aIds);
		// 去重
		return this.getListIntersection(userList, null, name);
	}

	/**
	 * 查询某种机构(年级组/教研组/备课组/科室)下面的教师信息
	 * 
	 * @param school
	 *            学校
	 * @param type
	 *            机构类型 1:教研组，2:年级组，3:备课组，6:科室
	 * @param name
	 *            教师姓名(可以[为空])
	 * @return List<User>
	 */
	// @Cacheable
	public List<Account> getAllTeacherList(School school, int type, String name) {
		// sdkHeader.setSchoolId(school.getId());
		// 年级组所有教师
		List<Account> userList = new ArrayList<Account>();
		List<OrgInfo> orglist = this.getSchoolOrgList(school, type);
		List<Long> aIds = new ArrayList<Long>();
		for (OrgInfo o : orglist) {
			List<Long> ids = o.getMemberAccountIds();
			if (null != ids) {
				aIds.addAll(ids);
			}
		}
		userList = this.getAccountBatch(school.getId(), aIds);
		// 去重
		return this.getListIntersection(userList, null, name);
	}

	/**
	 * 查询学校教师信息
	 * 
	 * @param school
	 *            学校对象
	 * @param name
	 *            教师姓名(可以[为空])
	 * @return List<User>
	 */
	// @Cacheable
	public List<Account> getAllTeacherList(School school, String name) {
		sdkHeader.setSchoolId(school.getId());
		final List<Account> list = new ArrayList<Account>();
		if (StringUtils.isNotEmpty(name)) {
			try {
				SchoolTeacherCon condition = new SchoolTeacherCon();
				condition.setKeyword(name);
				condition.setSchoolId(school.getId());
				SDKClient.getSchoolTeacher(new Listener<UserAndAccount>() {
					@Override
					public void onResponse(UserAndAccount response) {
						if (response != null && null!=response.getAccounts()) {
							list.addAll(response.getAccounts());
						}
					}

					@Override
					public void onErrorResponse(String msg, int retCode) {
						logger.error(msg + ", retCode:" + retCode);
					}
				}, condition, sdkHeader);
				
				logger.info("【基础数据接口】根据名称查询教师[正常]，方法getSchoolTeacher，入参school:{},name{}",school,name);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				logger.error("【基础数据接口】根据名称查询教师【出错】，方法getSchoolTeacher，入参school:{},name{}",school,name);
			}
		} else {
			// School school=this.getSchoolById(schoolId);
			List<Long> ids = school.getTeacherAccountIds();
			if (null != ids && ids.size() > 0) {
				return this.getAccountBatch(school.getId(), ids);
			}else{
				
				logger.error("【基础数据接口】学校教师账号列表为空，方法school.getTeacherAccountIds，入参school:{} ,出参：{}",school ,list);
			}

		}
		logger.info("【基础数据接口】根据名称查询教师[正常]，方法getSchoolTeacher，入参school:{},name{},出参：{}",school,name,list);
		return list;
	}

	/**
	 * 获取任课老师信息
	 * 
	 * @param map
	 *            schoolId 学校ID（必传） lessonId 科目ID，多个使用逗号分隔 classId
	 *            班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件） termInfoId 学年学期（必传）
	 *            usedGradeId 使用年级[可以多个逗号隔开] name 教师姓名
	 * @return List<Account>
	 */
	// @Cacheable
	public List<Account> getTeacherList(HashMap<String, Object> map) {
		final List<Account> userList = new ArrayList<Account>();
		SubjectTeacherCon condition = new SubjectTeacherCon();
		long schoolId = Long.valueOf(map.get("schoolId").toString());
		condition.setSchoolId(schoolId);
		sdkHeader.setSchoolId(schoolId);
		String kmdm = map.get("lessonId") == null ? "" : map.get("lessonId")
				.toString();
		if (StringUtils.isNotEmpty(kmdm)) {
			Set<Long> lessonIds = new HashSet<Long>();
			for (String s : Arrays.asList(kmdm.split(","))) {
				lessonIds.add(Long.valueOf(s));
			}
			condition.setLessonIds(lessonIds);
		}
		String bj = map.get("classId") == null ? "" : map.get("classId")
				.toString();
		String synj = map.get("usedGradeId") == null ? "" : map.get(
				"usedGradeId").toString();
		if (StringUtils.isNotEmpty(bj)) {
			Set<Long> classIds = new HashSet<Long>();
			for (String s : Arrays.asList(bj.split(","))) {
				classIds.add(Long.valueOf(s));
			}
			condition.setClassIds(classIds);
		} else if (StringUtils.isNotEmpty(synj)) {
			Set<T_GradeLevel> set = new HashSet<T_GradeLevel>();
			String xnxq = map.get("termInfoId").toString();
			String xn = xnxq.substring(0, 4);
			for (String s : Arrays.asList(synj.split(","))) {
				T_GradeLevel gl = T_GradeLevel.findByValue(Integer
						.valueOf(ConvertSYNJ2NJDM(s, xn)));
				set.add(gl);
			}
			condition.setGradeLevels(set);
		}
		final String name = map.get("name") == null ? "" : map.get("name")
				.toString();// 姓名

		try {
			SDKClient.getSubjectTeacher(new Listener<UserAndAccount>() {
				@Override
				public void onResponse(UserAndAccount response) {
					if (response != null && null!=response.getAccounts()) {
						if (StringUtils.isNotEmpty(name)) {
							for (Account u : response.getAccounts()) {
								if (u.getName().contains(name)) {
									userList.add(u);
								}
							}
						} else {
							userList.addAll(response.getAccounts());
						}

					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, condition, sdkHeader);
			
			logger.info("【基础数据接口】查询任课教师列表[正常]，方法getSubjectTeacher，入参map:{} ,出参:{}",map,userList);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询任课教师列表【出错】，方法getSubjectTeacher，入参map:{} ",map);
		}
		return userList;
	}

	/**
	 * 获取班主任信息
	 * 
	 * @param map
	 *            schoolId 学校ID（必传） classId 班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件）
	 *            termInfoId 学年学期（必传） usedGradeId 使用年级[可以多个逗号隔开] name 教师姓名
	 * @return List<Account>
	 */
	// @Cacheable
	public List<Account> getDeanList(HashMap<String, Object> map) {
		final List<Account> userList = new ArrayList<Account>();
		DeanCon condition = new DeanCon();
		long schoolId = Long.valueOf(map.get("schoolId").toString());
		condition.setSchoolId(schoolId);
		sdkHeader.setSchoolId(schoolId);
		String bj = map.get("classId") == null ? "" : map.get("classId")
				.toString();
		String synj = map.get("usedGradeId") == null ? "" : map.get(
				"usedGradeId").toString();
		if (StringUtils.isNotEmpty(bj)) {
			Set<Long> classIds = new HashSet<Long>();
			for (String s : Arrays.asList(bj.split(","))) {
				classIds.add(Long.valueOf(s));
			}
			condition.setClassIds(classIds);
		} else if (StringUtils.isNotEmpty(synj)) {
			Set<T_GradeLevel> set = new HashSet<T_GradeLevel>();
			String xnxq = map.get("termInfoId").toString();
			String xn = xnxq.substring(0, 4);
			for (String s : Arrays.asList(synj.split(","))) {
				T_GradeLevel gl = T_GradeLevel.findByValue(Integer
						.valueOf(ConvertSYNJ2NJDM(s, xn)));
				set.add(gl);
			}
			condition.setGradeLevels(set);
		}
		final String name = map.get("name") == null ? "" : map.get("name")
				.toString();// 姓名

		try {
			SDKClient.getClassDean(new Listener<UserAndAccount>() {
				@Override
				public void onResponse(UserAndAccount response) {
					if (response != null && null!=response.getAccounts()) {
						if (StringUtils.isNotEmpty(name)) {
							for (Account u : response.getAccounts()) {
								if (u.getName().contains(name)) {
									userList.add(u);
								}
							}
						} else {
							userList.addAll(response.getAccounts());
						}

					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, condition, sdkHeader);
			
			logger.info("【基础数据接口】查询班主任列表[正常]，方法getClassDean，入参name:{},出参:{} ",name,userList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询班主任列表【出错】，方法getClassDean，入参name:{} ",name);
		}
		List<Account> delList = new ArrayList<Account>();
		for(Account a:userList){
			if(a.getId()==0){
				delList.add(a);
			}
		}
		userList.removeAll(delList);
		return userList;
	}

	/**
	 * 供根据学校得到所有的年级
	 * 
	 * @param sch
	 *            学校对象
	 * @return
	 */
	// @Cacheable
	public List<Grade> getGradeList(School sch) {
		// School sch = this.getSchoolById(schoolId);
		sdkHeader.setSchoolId(sch.getId());
		List<Long> gradeIds = sch.getGrades();
		final List<Grade> glist = new ArrayList<Grade>();
		if(gradeIds==null||gradeIds.size()==0){
			logger.error("【基础数据接口】查询年级列表[错误]，所选学校年级为空，学校为【{}】",sch.getName());
			return glist;
		}
		try {
			SDKClient.getGradeBatch(new Listener<GradeList>() {
				@Override
				public void onResponse(GradeList response) {
					if (response != null && null!=response.getGrades()) {
						glist.addAll(response.getGrades());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, gradeIds, sdkHeader);
			logger.info("【基础数据接口】查询年级列表[正常]，方法getGradeBatch，入参School:{},出参:{} ",sch,glist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("【基础数据接口】查询年级列表【出错】，方法getGradeBatch，入参School:{} ",sch);
		}
		List<Grade> list = new ArrayList<Grade>();
		for(Grade g:glist){
			if(g.getCurrentLevel()!=null&&!g.isGraduate){
				list.add(g);
			}
		}
		//
		if(list.size()>0){
			logger.info("【基础数据接口】过滤毕业查询年级列表[正常]，方法getGradeBatch，入参School:{},出参Gradelist：{} ",sch,list);
		}
		return list;
	}

	/**
	 * 批量调用 通过多个年级ID得到年级对象List<LessonInfo>
	 * 
	 * @param schoolId
	 *            学校id
	 * @param ids
	 *            年级ids
	 * @return
	 */
	// @Cacheable
	public List<Grade> getGradeBatch(long schoolId, List<Long> ids) {
		sdkHeader.setSchoolId(schoolId);
		final List<Grade> list = new ArrayList<Grade>();
		if(null==ids||ids.size()==0)
		{
			return list;
		}
		try {
			SDKClient.getGradeBatch(new Listener<GradeList>() {
				@Override
				public void onResponse(GradeList response) {
					if (response != null && null!=response.getGrades()) {
						list.addAll(response.getGrades());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, ids, sdkHeader);
			
			logger.info("【基础数据接口】查询年级列表[正常]，方法getGradeBatch，入参schoolId:{},gradeIds:{},出参:{}",schoolId,ids,list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询年级列表【出错】，方法getGradeBatch，入参schoolId:{},gradeIds:{}",schoolId,ids);
		}
		return list;
	}

	/**
	 * 获取班级 (查询多个年级或单个年级下面的班级)
	 * 
	 * @param map
	 *            schoolId 学校ID（必传） classTypeId 班级类型 0综合 1文科 2理科 termInfoId
	 *            学年学期（必传） usedGradeId 使用年级[可以多个逗号隔开]
	 * @return List<Classroom>
	 */
	// @Cacheable
	public List<Classroom> getClassList(HashMap<String, Object> map) {
		final List<Classroom> list = new ArrayList<Classroom>();
		SchoolClassCon condition = new SchoolClassCon();
		long schoolId = Long.valueOf(map.get("schoolId").toString());
		condition.setSchoolId(schoolId);
		sdkHeader.setSchoolId(schoolId);
		if (map.containsKey("classTypeId")) {
			Set<T_ClassType> set = new HashSet<T_ClassType>();
			set.add(T_ClassType.findByValue(Integer.valueOf(map.get(
					"classTypeId").toString())));
			condition.setClassTypes(set);
		}

		String synj = map.get("usedGradeId") == null ? "" : map.get(
				"usedGradeId").toString();
		if (StringUtils.isNotEmpty(synj)) {
			Set<T_GradeLevel> set = new HashSet<T_GradeLevel>();
			String xnxq = map.get("termInfoId").toString();
			String xn = xnxq.substring(0, 4);
			for (String s : Arrays.asList(synj.split(","))) {
				T_GradeLevel gl = T_GradeLevel.findByValue(Integer
						.valueOf(ConvertSYNJ2NJDM(s, xn)));
				set.add(gl);
			}
			condition.setGradeLevels(set);
		}
		try {
			SDKClient.getSchoolClass(new Listener<ClassroomList>() {
				@Override
				public void onResponse(ClassroomList response) {
					if (response != null && null!=response.getClasses()) {
						list.addAll(response.getClasses());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, condition, sdkHeader);
			logger.info("【基础数据接口】查询班级列表[正常]，方法getSchoolClass，入参condition:{},出参:{} ",map,list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询班级列表【出错】，方法getSchoolClass，入参condition:{} ",map);
		}
		return list;
	}

	
	/**
	 * 获取全校所有班级-简版【仅含name，id，gradeId】
	 * @param sch
	 * @param grade	可以为空
	 * @return
	 */
	public List<Classroom> getSimpleClassList(School sch,List<Grade> grade ) {
		SchoolClassCon condition = new SchoolClassCon();
		long schoolId =sch.getId();
		condition.setSchoolId(schoolId);
		sdkHeader.setSchoolId(schoolId);
		List<Grade> grades = new ArrayList<Grade>();
		if(grade==null){
			grades = this.getGradeBatch(schoolId, sch.getGrades()) ;
		}else{
			grades = grade;
		}
		
		List<Long> cls = new ArrayList<Long>();
		
		for(Grade gd:grades){
			if(!gd.isGraduate&&gd.getClassIds()!=null){
				cls.addAll(gd.getClassIds());
			}
		}
		
		return this.getSimpleClassBatch(schoolId, cls);
	}

	/**
	 * 批量调用 通过多个班级ID得到班级对象List<Classroom>
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return
	 */
	// @Cacheable
	public List<Classroom> getClassroomBatch(long schoolId, List<Long> ids) {
		sdkHeader.setSchoolId(schoolId);
		final List<Classroom> list = new ArrayList<Classroom>();
		if(ids.size()==0){
			logger.error("【基础数据接口】入参学校代码:{},班级[为空]:{}",schoolId,ids);
			return list;
		}
		try {
			SDKClient.getClassroomBatch(new Listener<ClassroomList>() {
				@Override
				public void onResponse(ClassroomList response) {
					if (response != null && null!=response.getClasses()) {
						list.addAll(response.getClasses());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, ids, sdkHeader);
			if(list.size()==0){
				logger.error("【基础数据接口】查询班级列表[为空]，方法getClassroomBatch，入参schoolId:{},classIds:{} ",schoolId,ids);
			}else{
				
				logger.info("【基础数据接口】查询班级列表[正常]，方法getClassroomBatch，入参schoolId:{},classIds:{},出参:{} ",schoolId,ids,list);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询班级列表【出错】，方法getClassroomBatch，入参schoolId:{},classIds:{} ",schoolId,ids);
		}
		return list;
	}


	/**
	 * 批量调用 通过多个班级ID得到班级对象List<Classroom>
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return
	 */
	// @Cacheable
	public List<Classroom> getSimpleClassBatch(long schoolId, List<Long> ids) {
		sdkHeader.setSchoolId(schoolId);
		final List<Classroom> list = new ArrayList<Classroom>();
		if(ids.size()==0){
			logger.error("【基础数据接口】入参学校代码:{},班级[为空]:{}",schoolId,ids);
			return list;
		}
		try {
			SDKClient.getSimpleClassBatch(new Listener<ClassroomList>() {
				@Override
				public void onResponse(ClassroomList response) {
					if (response != null && null!=response.getClasses()) {
						list.addAll(response.getClasses());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, ids, sdkHeader);
			if(list.size()==0){
				logger.error("【基础数据接口】查询简版班级列表[为空]，方法getClassroomBatch，入参schoolId:{},classIds:{} ",schoolId,ids);
			}else{
				
				logger.info("【基础数据接口】查询简版班级列表[正常]，方法getClassroomBatch，入参schoolId:{},classIds:{},出参:{} ",schoolId,ids,list);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询班级列表【出错】，方法getClassroomBatch，入参schoolId:{},classIds:{} ",schoolId,ids);
		}
		return list;
	}

	/**
	 * 批量调用 通过多个科目ID得到科目对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 *            科目ids
	 * @return List<LessonInfo>
	 */
	// @Cacheable
	public List<LessonInfo> getLessonInfoBatch(long schoolId, List<Long> ids) {
		sdkHeader.setSchoolId(schoolId);
		final List<LessonInfo> list = new ArrayList<LessonInfo>();
		if(null==ids||ids.size()==0)
		{
			return list;
		}
		try {
			SDKClient.getLessonInfoBatch(new Listener<LessonList>() {
				@Override
				public void onResponse(LessonList response) {
					if (response != null && null!=response.getLessons()) {
						list.addAll(response.getLessons());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, ids, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询课程信息列表【出错】，方法g etLessonInfoBatch，入参schoolId:{},lessonIds:{} ",schoolId,ids);
		}

		if(list.size()==0){
			
			logger.error("【基础数据接口】查询课程信息列表[为空]，方法getLessonInfoBatch，入参schoolId:{},lessonIds:{} ",schoolId,ids);
		}else{
			
			logger.info("【基础数据接口】查询课程信息列表[正常]，方法getLessonInfoBatch，入参schoolId:{},lessonIds:{},出参list:{} ",schoolId,ids,list);
		}
		return list;
	}

	/**
	 * 根据学校获取学校所有科目
	 * 
	 * @param school
	 *            学校
	 * @return List<LessonInfo>
	 */
	// @Cacheable
	public List<LessonInfo> getLessonInfoList(School school) {
		List<LessonInfo> list = new ArrayList<LessonInfo>();
		// School school=getSchoolById(schoolId);
		if (null != school) {
			list = getLessonInfoBatch(school.getId(), school.getLessonInfoIds());
		}

		return list;
	}

	/**
	 * 获取某种类型科目
	 * 
	 * @param school
	 *            学校
	 * @param type
	 *            0普通 1综合 2活动
	 * @return
	 */
	// @Cacheable
	public List<LessonInfo> getLessonInfoByType(School school, int type) {
		List<LessonInfo> list = this.getLessonInfoList(school);
		List<LessonInfo> clist = new ArrayList<LessonInfo>();
		for (LessonInfo le : list) {
			if (le.getType() == type && null != le.getName()) {
				clist.add(le);
			}
		}
		return clist;
	}

	/**
	 * （全校的）根据学号/学籍号/姓名查询学生
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param termInfoId
	 *            学年学期ID
	 * @param keyword
	 *            学号/学籍号/姓名
	 * @return
	 */
	// @Cacheable
	public List<Account> getStudentList(long schoolId, String termInfoId,
			String keyword) {
		sdkHeader.setSchoolId(schoolId);
		final List<Account> list = new ArrayList<Account>();
		SchoolStudentCon condition = new SchoolStudentCon();
		if (StringUtils.isNotEmpty(keyword)) {
			condition.setKeyword(keyword);
		}
		condition.setSchoolId(schoolId);
		try {
			SDKClient.getSchoolStudent(new Listener<UserAndAccount>() {
				@Override
				public void onResponse(UserAndAccount response) {
					if (response != null && null!=response.getAccounts()) {
						list.addAll(response.getAccounts());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, condition, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询学生信息列表【出错】，方法getSchoolStudent，入参schoolId:{},termInfoId:{},keyword:{} ",schoolId,termInfoId,keyword);
		}
		if(list.size()==0){
			
			logger.error("【基础数据接口】查询学生信息列表[为空]，方法getSchoolStudent，入参schoolId:{},termInfoId:{},keyword:{} ",schoolId,termInfoId,keyword);
		}else{
			//用户增加学校条件过滤
			List<Grade> gradeList=this.getGradeList(this.getSchoolById(schoolId));
			List<Long> cid=new ArrayList<Long>();
			for(Grade g:gradeList)
			{
				if(g==null||g.getClassIds()==null){
					continue;
				}
				cid.addAll(g.getClassIds());
			}
			
			for(Account act:list){
				if(act!=null&&act.getUsers()!=null){
					List<User> uses = act.getUsers();
					List<User> needRemove = new ArrayList<User>();
					if(uses.size()>0){
						for(User u:uses){
							if(u!=null&&!cid.contains(u.getStudentPart().getClassId())){
								needRemove.add(u);
							}
						}
					}
					uses.removeAll(needRemove);
					act.setUsers(uses);
				}
			}
			logger.info("【基础数据接口】查询学生信息列表[正常]，方法getSchoolStudent，入参schoolId:{},termInfoId:{},keyword:{},出参:{} ",schoolId,termInfoId,keyword,list);
		}
		return list;
	}

	/**
	 * 获取学生 （年级的）年级、班级类型的
	 * 
	 * @param map
	 *            schoolId 学校代码 （必传） classTypeId 班级类型0:综合，1：文科，2： 理科， classId
	 *            班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件） termInfoId 学年学期（必传）
	 *            usedGradeId 使用年级[可以多个逗号隔开] keyword 姓名/学号/学籍号 可以[为空]
	 * @return
	 */
	// @Cacheable
	public List<Account> getStudentList(HashMap<String, Object> map) {
		final List<Account> list = new ArrayList<Account>();
		ClassStudentCon condition = new ClassStudentCon();
		long schoolId = Long.valueOf(map.get("schoolId").toString());
		condition.setSchoolId(schoolId);
		sdkHeader.setSchoolId(schoolId);
		long d1 = new Date().getTime();
		if (map.containsKey("classTypeId")) {
			Set<T_ClassType> set = new HashSet<T_ClassType>();
			set.add(T_ClassType.findByValue(Integer.valueOf(map.get(
					"classTypeId").toString())));
			condition.setClassTypes(set);
		}
		String bj = map.get("classId") == null ? "" : map.get("classId")
				.toString();
		String synj = map.get("usedGradeId") == null ? "" : map.get(
				"usedGradeId").toString();
		if (StringUtils.isNotEmpty(bj)) {
			Set<Long> classIds = new HashSet<Long>();
			for (String s : Arrays.asList(bj.split(","))) {
				classIds.add(Long.valueOf(s));
			}
			condition.setClassIds(classIds);
		} else if (StringUtils.isNotEmpty(synj)) {
			Set<T_GradeLevel> set = new HashSet<T_GradeLevel>();
			String xnxq = map.get("termInfoId").toString();
			String xn = xnxq.substring(0, 4);
			for (String s : Arrays.asList(synj.split(","))) {
				T_GradeLevel gl = T_GradeLevel.findByValue(Integer
						.valueOf(ConvertSYNJ2NJDM(s, xn)));
				set.add(gl);
			}
			condition.setGradeLevels(set);
		} else if (null != map.get("gradeLevel")) {
			Set<T_GradeLevel> set = new HashSet<T_GradeLevel>();
			set.add((T_GradeLevel) map.get("gradeLevel"));
			condition.setGradeLevels(set);
		}

		final String keyword = map.get("keyword") == null ? "" : map.get(
				"keyword").toString();
		// 姓名/学号/学籍号
		try {
			SDKClient.getClassStudent(new Listener<UserAndAccount>() {
				@Override
				public void onResponse(UserAndAccount response) {
					if (response != null&&null!=response.getAccounts()) {
						if (StringUtils.isNotEmpty(keyword)) {
							for (Account a : response.getAccounts()) {
			    				 if(a.getUsers()==null){
			      		    		continue;
			      		    	}
								for (User u : a.getUsers()) {
			         				if(u==null||u.getUserPart()==null||u.getUserPart().getRole()==null||u.getStudentPart()==null){
			         					continue;
			         				}
									if (u.getUserPart().getRole().equals(T_Role.Student)) {
										StudentPart sp=u.getStudentPart();
										if (a.getName().contains(keyword)|| (null!=sp.getStdNumber()&&sp.getStdNumber().contains(keyword))
												|| (null!=sp.getSchoolNumber()&&sp.getSchoolNumber().contains(keyword))) {
											list.add(a);
										}
										break;
									}
								}

							}

						} else {
							list.addAll(response.getAccounts());
						}
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					System.out.println("error:" + msg + ", retCode:" + retCode);
				}
			}, condition, sdkHeader);
			
			logger.info("【基础数据接口】查询学生信息列表[正常]，方法getClassStudent，入参map{},出参:{} ",map,list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询学生信息列表【出错】，方法getClassStudent，入参map{} ",map);
		}

		long d2 = new Date().getTime();
		logger.debug("【基础数据】获取学生列表并组装耗时:" + (d2 - d1));
		
		//用户增加学校条件过滤
		List<Grade> gradeList=this.getGradeList(this.getSchoolById(schoolId));
		List<Long> cid=new ArrayList<Long>();
		for(Grade g:gradeList)
		{
			if(g==null||g.getClassIds()==null){
				continue;
			}
			cid.addAll(g.getClassIds());
		}
		
		for(Account act:list){
			if(act!=null&&act.getUsers()!=null){
				List<User> uses = act.getUsers();
				List<User> needRemove = new ArrayList<User>();
				if(uses.size()>0){
					for(User u:uses){
						if(u!=null&&u.getStudentPart()!=null
								&&!cid.contains(u.getStudentPart().getClassId())){
							needRemove.add(u);
						}
					}
				}
				uses.removeAll(needRemove);
				act.setUsers(uses);
			}
		}
		
		return list;
	}

	/**
	 * 获取机构信息（年级组，教研组，备课组，科室）
	 * 
	 * @param map
	 *            school 学校 gradeId 年级组Id，多个使用逗号分隔
	 * @param type
	 *            1:教研组，2:年级组，3:备课组，6:科室
	 * @return List<OrgInfo>
	 */
	// @Cacheable
	public List<OrgInfo> getSchoolOrgList(School school, int type) {
		List<OrgInfo> list = new ArrayList<OrgInfo>();
		// School school=this.getSchoolById(schoolId);
		for (OrgInfo o : this.getSchoolOrgBatch(school.getId(),
				school.getOrgIds())) {
			if (o.getOrgType() == type) {
				list.add(o);
			}
		}
		return list;
	}

	/**
	 * 通过学校获取机构信息
	 * 
	 * @param school
	 *            学校对象
	 * @return
	 */
	// @Cacheable
	public List<OrgInfo> getSchoolOrgList(School school) {
		List<OrgInfo> list = new ArrayList<OrgInfo>();
		// School school = this.getSchoolById(schoolId);
		if (null != school) {
			list = this.getSchoolOrgBatch(school.getId(), school.getOrgIds());
		}
		return list;
	}

	/**
	 * 通过机构ID获取机构详细信息
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param orgId
	 * @return
	 */
	public OrgInfo getSchoolOrgAllById(long schoolId, long orgId) {
		sdkHeader.setSchoolId(schoolId);
		final OrgInfo[] org = new OrgInfo[1];
		try {
			SDKClient.getSchoolOrgAllById(new Listener<OrgInfo>() {
				@Override
				public void onResponse(OrgInfo response) {
					if (response != null) {
						org[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, orgId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询学校机构【出错】，方法getSchoolOrgAllById，入参schoolId:{},orgId:{} ",schoolId,orgId);
		}
		if(org[0]==null){
			
			logger.error("【基础数据接口】查询学校机构[为空]，方法getSchoolOrgAllById，入参schoolId:{},orgId:{} ",schoolId,orgId);
		}else{
			
			logger.info("【基础数据接口】查询学校机构[正常]，方法getSchoolOrgAllById，入参schoolId:{},orgId:{},出参:{} ",schoolId,orgId,org[0]);
		}
		return org[0];
	}

	/**
	 * 批量接口 通过机构ID获取机构详细信息
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return List<OrgInfo>
	 */
	public List<OrgInfo> getSchoolOrgBatch(long schoolId, List<Long> ids) {
		sdkHeader.setSchoolId(schoolId);
		final List<OrgInfo> list = new ArrayList<OrgInfo>();
		if(null==ids||ids.size()==0)
		{
			return list;
		}
		try {
			SDKClient.getSchoolOrgBatch(new Listener<SchoolOrgList>() {
				@Override
				public void onResponse(SchoolOrgList response) {
					if (response != null && null!=response.getOrgs()) {
						list.addAll(response.getOrgs());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, ids, sdkHeader);
			logger.info("【基础数据接口】查询学校机构列表[正常]，方法getSchoolOrgBatch，入参schoolId:{},orgIds:{},出参:{} ",schoolId,ids,list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】查询学校机构列表【出错】，方法getSchoolOrgBatch，入参schoolId:{},orgIds:{} ",schoolId,ids);
		}
		return list;
	}

//	/**
//	 * 课程设置回写教师、班级、科目信息
//	 * 
//	 * @param schoolId
//	 *            学校ID
//	 * @param classId
//	 *            班级id
//	 * @param teacherLessons
//	 *            教师ids和科目ids
//	 */
//	public void replaceTeacherCourseByClassId(long schoolId, long classId,
//			List<TeacherLesson> teacherLessons) {
//		sdkHeader.setSchoolId(schoolId);
//		try {
//			SDKClient.replaceTeacherCourseByClassId(
//					new Listener<ReplaceTeacherCourseByClassIdRsp>() {
//						@Override
//						public void onResponse(
//								ReplaceTeacherCourseByClassIdRsp response) {
//							if (response != null) {
//
//							}
//						}
//
//						@Override
//						public void onErrorResponse(String msg, int retCode) {
//							logger.error(msg + ", retCode:" + retCode);
//						}
//					}, classId, teacherLessons, sdkHeader);
//			logger.info("【基础数据接口】回写教师、班级、科目[正常]，方法replaceTeacherCourseByClassId，入参schoolId:{},classId:{}，teacherLessons{} ",schoolId,classId,teacherLessons);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//			logger.error("【基础数据接口】回写教师、班级、科目【出错】，方法replaceTeacherCourseByClassId，入参schoolId:{},classId:{}，teacherLessons{} ",schoolId,classId,teacherLessons);
//		}
//	}

	/**
	 * 通过深圳那边的年级代码获取对应的年级代码
	 * 
	 * @param gradeId
	 * @return
	 */
	public int getNewGradeIdByGradeLevel(int gradeLevel) {
		return gradeLevel - 10;

	}

	/*		*//**
	 * 通过年级ID、学年得到入学年度,（深圳的年级对应的createLevel代码从10开始,调用方法之前减去10）
	 * 入学年度=学年-年级代码+起始年级
	 * 
	 * @param gradeId
	 * @return
	 */
	/*
	 * public long getEnrolYearByTermAndGId(long xn, long gradeId) { // 小学 if
	 * (gradeId < 6) { return xn - gradeId; } //初中 else if (6 <= gradeId &&
	 * gradeId <= 8) { return xn - gradeId + 6; }//高中 else if (9 <= gradeId &&
	 * gradeId <= 11) { return xn - gradeId + 9; } else { return -1; } }
	 */

	/**
	 * 通过年级ID得到年级名称
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param gradeId
	 * @return
	 */
	public String getGradeNameById(long schoolId, long gradeId) {
		sdkHeader.setSchoolId(schoolId);
		final String[] name = new String[1];
		try {
			SDKClient.getGradeById(new Listener<Grade>() {

				@Override
				public void onResponse(Grade response) {
					if (response != null && null!=response.getCurrentLevel()) {
						name[0] = T_GradeLevelName.get(response.getCurrentLevel());
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, gradeId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("【基础数据接口】根据年级id获取名称[【出错】]，方法getGradeNameById，入参schoolId:{},gradeId:{} ",schoolId,gradeId);
		}
		if(name[0]==null){
			
			logger.error("【基础数据接口】根据年级id获取名称[[为空]]，方法getGradeNameById，入参schoolId:{},gradeId:{} ",schoolId,gradeId);
		}else{
			
			logger.info("【基础数据接口】根据年级id获取名称[[正常]]，方法getGradeNameById，入参schoolId:{},gradeId:{},出参:{} ",schoolId,gradeId,name[0]);
		}
		
		return name[0];
	}

	/**
	 * 通过年级id获取年级对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param gradeId
	 *            年级id
	 * @return
	 */
	public Grade getGradeById(long schoolId, long gradeId) {
		sdkHeader.setSchoolId(schoolId);
		final Grade[] grade = new Grade[1];
		try {
			SDKClient.getGradeById(new Listener<Grade>() {

				@Override
				public void onResponse(Grade response) {
					if (response != null) {
						grade[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, gradeId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据年级id获取年级[【出错】]，方法getGradeById，入参schoolId:{},gradeId:{} ",schoolId,gradeId);
		}
		if(grade[0]==null){
			
			logger.error("【基础数据接口】根据年级id获取年级[[为空]]，方法getGradeById，入参schoolId:{},gradeId:{} ",schoolId,gradeId);
		}else{
			
			logger.info("【基础数据接口】根据年级id获取年级[[正常]]，方法getGradeById，入参schoolId:{},gradeId:{},出参:{} ",schoolId,gradeId,grade[0]);
		}
		return grade[0];
	}
	
	/**
	 * 通过年级代码gradeLevel获取年级对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param gradeId
	 *            年级id
	 * @return
	 */
	public Grade getGradeByGradeLevel(long schoolId, T_GradeLevel gradeLevel) {
		sdkHeader.setSchoolId(schoolId);
		final Grade[] grade = new Grade[1];
		try {
			SDKClient.getGradeByGradeLevel(new Listener<Grade>() {

				@Override
				public void onResponse(Grade response) {
					if (response != null) {
						grade[0]=response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, schoolId,gradeLevel, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据gradeLevel获取年级[【出错】]，方法getGradeByGradeLevel，入参schoolId:{},gradeLevel:{} ",schoolId,gradeLevel);
		}
		if(grade[0]==null){
			
			logger.error("【基础数据接口】根据gradeLevel获取年级[[为空]]，方法getGradeByGradeLevel，入参schoolId:{},gradeLevel:{} ",schoolId,gradeLevel);
		}else{
			
			logger.info("【基础数据接口】根据gradeLevel获取年级[[正常]]，方法getGradeByGradeLevel，入参schoolId:{},gradeLevel:{},出参:{} ",schoolId,gradeLevel,grade[0]);
		}
		return grade[0];
	}
	

	/**
	 * 通过班级ID得到班级
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param gradeId
	 * @return
	 */
	public Classroom getClassById(long schoolId, long classId) {
		sdkHeader.setSchoolId(schoolId);
		final Classroom[] c = new Classroom[1];
		try {
			SDKClient.getClassroomById(new Listener<Classroom>() {

				@Override
				public void onResponse(Classroom response) {
					if (response != null) {
						c[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, classId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据班级id获取班级信息[【出错】]，方法getClassroomById，入参schoolId:{},classId:{} ",schoolId,classId);

		}
		if(c[0]==null){
			
			logger.error("【基础数据接口】根据班级id获取班级信息[[为空]]，方法getClassroomById，入参schoolId:{},classId:{} ",schoolId,classId);
		}else{
			
			logger.info("【基础数据接口】根据班级id获取班级信息[[正常]]，方法getClassroomById，入参schoolId:{},classId:{} ",schoolId,classId,c[0]);
		}
		return c[0];
	}

	/**
	 * 批量调用 通过多个用户（教师/学生/其他user）ID得到用户对象List<User>
	 * 调用并发接口getUserBatchParallel
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return
	 */
	public List<User> getUserBatch(long schoolId, List<Long> ids) {
		sdkHeader.setSchoolId(schoolId);
		final List<User> list = new ArrayList<User>();
		if(ids.size()==0){
			logger.error("【基础数据接口】入参学校代码:{},userids[为空]:{}",schoolId,ids);
			return list;
		}
		try {
			SDKClient.getUserBatchParallel(new ParallelListener<List<User>>() {
				@Override
				public void onResponse(List<User> response) {
					if (response != null) {
						list.addAll(response);
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, ids, sdkHeader);
			logger.info("【基础数据接口】根据用户id获取用户列表[[正常]]，方法getUserBatchParallel，入参{}，出参{}",ids,list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			logger.error("【基础数据接口】根据用户id获取用户列表[【出错】]，方法getUserBatchParallel，入参userIds:{}",ids);
		}
		
		return list;
	}
	
	
	/**
	 * 异步批量调用 通过多个用户（教师/学生/其他user）ID得到用户对象List<User>
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return
	 */
//	public List<User> getUserBatchAsync(long schoolId, List<Long> ids) {
////		long time0=new Date().getTime();
//		sdkHeader.setSchoolId(schoolId);
//		final List<User> list = new ArrayList<User>();
//		try {
//			SDKClient.getUserBatchAsync(new Listener<UserList>() {
//				@Override
//				public void onResponse(UserList response) {
//					if (response != null) {
//						list.addAll(response.getUsers());
//					}
//				}
//
//				@Override
//				public void onErrorResponse(String msg, int retCode) {
//					logger.error(msg + ", retCode:" + retCode);
//				}
//			}, ids, sdkHeader);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//			logger.error("【基础数据接口】根据用户id异步获取用户列表[【出错】]，方法getUserBatchAsync，入参userIds:{}  ",ids);
//		}
//		
//		if(list.size()==0 ){
//			logger.error("【基础数据接口】根据用户id异步获取用户列表[[为空]]，方法getUserBatchAsync，入参userIds:{}  ",ids);
//		}else{
//			logger.info("【基础数据接口】根据用户id异步获取用户列表[[正常]]，方法getUserBatchAsync，入参userIds:{} ,出参:{} ",ids,list);
//		}
////		long time1=new Date().getTime();
////		System.out.println(Thread.currentThread().getName()+"    getUserBatch:"+(time1-time0));
//		return list;
//	}

	/**
	 * 通过用户ID得到用户对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param userId
	 *            教师/学生/用户ID
	 * @return
	 */
	public User getUserById(long schoolId, long userId) {
		sdkHeader.setSchoolId(schoolId);
		final User[] c = new User[1];
		try {
			SDKClient.getUserById(new Listener<User>() {

				@Override
				public void onResponse(User response) {
					if (response != null) {
						c[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, userId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据用户id获取用户信息[【出错】]，方法getUserById，入参schoolId:{},userId{} ",schoolId,userId);
		}
		if(c[0]==null){
			logger.error("【基础数据接口】根据用户id获取用户信息[[为空]]，方法getUserById，入参schoolId:{},userId{} ",schoolId,userId);
			
		}else{
			
			logger.info("【基础数据接口】根据用户id获取用户信息[[正常]]，方法getUserById，入参schoolId:{},userId{},出参:{} ",schoolId,userId,c[0]);
		}
		return c[0];
	}

	/**
	 * 通过账号获取账号信息，包括账号下所有User的详细信息
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param account
	 *            账号
	 * @return
	 */
	public Account getAccountAllByAccount(long schoolId, String account) {
		sdkHeader.setSchoolId(schoolId);
		final Account[] c = new Account[1];
		try {
			SDKClient.getAccountAllByName(new Listener<Account>() {

				@Override
				public void onResponse(Account response) {
					if (response != null) {
						c[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, account, sdkHeader);
			logger.info("【基础数据接口】根据账号获取账户[[正常]]，方法getAccountAllByName，入参schoolId:{},account{},出参:{} ",schoolId,account,c[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据账号获取账户[【出错】]，方法getAccountAllByName，入参schoolId:{},account{} ",schoolId,account);
		}
		return c[0];
	}

	/**
	 * 通过学校ID获取学校
	 * 
	 * @param schoolId
	 *            学校ID
	 * @return School
	 */
	public School getSchoolById(long schoolId) {
		sdkHeader.setSchoolId(schoolId);
		final School[] c = new School[1];
		try {
			SDKClient.getSchoolById(new Listener<School>() {

				@Override
				public void onResponse(School response) {
					if (response != null) {
						c[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, schoolId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据学校id获取学校[【出错】]，方法getSchoolById，入参schoolId:{}  ",schoolId);
		}
		if(c[0]!=null ){
			
			logger.info("【基础数据接口】根据学校id获取学校[[正常]]，方法getSchoolById，入参schoolId:{},出参:{}  ",schoolId,c[0]);
		}else{
			logger.error("【基础数据接口】根据学校id获取学校[[为空]]，方法getSchoolById，入参schoolId:{}  ",schoolId);
			
		}	
		return c[0];
	}

	/**
	 * 通过用户ID得到学校对象,,
	 * 
	 * @param schoolId
	 *            学校ID 此参数设置为0
	 * @param userId
	 *            教师/学生/用户ID
	 * @return School
	 */
	public School getSchoolByUserId(long schoolId, long userId) {
		sdkHeader.setSchoolId(schoolId);
		final School[] c = new School[1];
		try {
			SDKClient.getSchoolByUserId(new Listener<School>() {
				@Override
				public void onResponse(School response) {
					if (response != null) {
						c[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, userId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据用户id获取学校[【出错】]，方法getSchoolByUserId，入参schoolId:{},userId:{}  ",schoolId,userId);
		}
		if(c[0]==null){
			
			logger.error("【基础数据接口】根据用户id获取学校[[为空]]，方法getSchoolByUserId，入参schoolId:{} ,userId:{} ",schoolId,userId);
		}else{
			
			logger.info("【基础数据接口】根据用户id获取学校[[正常]]，方法getSchoolByUserId，入参schoolId:{} ,userId:{},出参:{} ",schoolId,userId,c[0]);
		}
		return c[0];
	}

	/**
	 * 通过用户账号得到账号对象
	 * 
	 * @param schoolId
	 *            学校id
	 * @param accountId
	 *            账号
	 * @return Account
	 */
	public Account getAccountAllById(long schoolId, long accountId) {
		sdkHeader.setSchoolId(schoolId);
		final Account[] c = new Account[1];
		try {
			SDKClient.getAccountAllById(new Listener<Account>() {
				@Override
				public void onResponse(Account response) {
					if (response != null) {
						c[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, accountId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据账户id获取所有账户信息[【出错】]，方法getAccountAllById，入参schoolId:{},accountId:{}  ",schoolId,accountId);
		}
		if( c[0]==null){
			
			logger.error("【基础数据接口】根据账户id获取所有账户信息[[为空]]，方法getAccountAllById，入参schoolId:{},accountId:{}  ",schoolId,accountId);
		}else{
			
			logger.info("【基础数据接口】根据账户id获取所有账户信息[[正常]]，方法getAccountAllById，入参schoolId:{},accountId:{} ,出参:{} ",schoolId,accountId,c[0]);
		}
		//增加学校条件过滤，后续待深圳增加基础数据学校条件过滤后删掉
		List<User> uList=new ArrayList<User>();
		if(null!=c[0].getUsers())
		{
			//一个Account下面有多个user对象的时候才进行判断
			if(c[0].getUsers().size()>1)
			{
				for(User u:c[0].getUsers())
				{
					if(u.getUserPart().getRole().equals(T_Role.Student)&&null!=u.getStudentPart())
					{
						if(this.getSchoolByUserId(schoolId, u.getUserPart().getId()).getId()!=schoolId)
						{
							continue;
						}
						uList.add(u);
					}
				}
			}
		}
		if(!uList.isEmpty())
		{
	        c[0].setUsers(uList);
		}

		return c[0];
	}

	/**
	 * 通过账号ID得到用户账号名,,
	 * 
	 * @param schoolId
	 *            学校id
	 * @param accountId
	 *            账号ID
	 * @return
	 */
	public Account getAccountById(long schoolId, long accountId) {
		sdkHeader.setSchoolId(schoolId);
		final Account[] s = new Account[1];
		try {
			SDKClient.getAccountById(new Listener<Account>() {
				@Override
				public void onResponse(Account response) {
					if (response != null) {
						s[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, accountId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据账户id获取账户信息[【出错】]，方法getAccountById，入参schoolId:{},accountId:{}  ",schoolId,accountId);
		}
		if( s[0]==null){
			
			logger.error("【基础数据接口】根据账户id获取账户信息[[为空]]，方法getAccountById，入参schoolId:{},accountId:{}  ",schoolId,accountId);
		}else{
			
			logger.info("【基础数据接口】根据账户id获取账户信息[[正常]]，方法getAccountById，入参schoolId:{},accountId:{} ,出参:{} ",schoolId,accountId,s[0]);
		}
		return s[0];
	}

	/**
	 * 批量获取账号用户,调用并发接口getAccountBatchParallel
	 * 
	 * @param schoolId
	 *            学校id
	 * @param accountIds
	 *            账号ids
	 * @return
	 */
	public List<Account> getAccountBatch(long schoolId, List<Long> accountIds) {
		sdkHeader.setSchoolId(schoolId);
		final List<Account> s = new ArrayList<Account>();
		if(null==accountIds||accountIds.size()==0)
		{
			return s;
		}
		if (null != accountIds && accountIds.size() > 0) {
			try {
				SDKClient.getAccountBatchParallel(new ParallelListener<List<Account>>() {
					@Override
					public void onResponse(List<Account> response) {
						if (response != null) {
							s.addAll(response);
						}
					}

					@Override
					public void onErrorResponse(String msg, int retCode) {
						System.out.println("error:" + msg + ", retCode:" + retCode);
					}
				}, accountIds, sdkHeader);
				logger.info("【基础数据接口】根据账户id获取账户列表[[正常]]，方法getAccountBatchParallel");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				logger.error("【基础数据接口】根据账户id获取账户列表[【出错】]，方法getAccountBatchParallel，入参schoolId:{},accountIds:{}  ",schoolId,accountIds);
			}
		}
		HashMap<Long, Account> userAccountIdMap = new HashMap<Long, Account>();
		List<Long> aids=new ArrayList<Long>();
		List<Long> batchUid = new ArrayList<Long>();
		for (Account a : s) {
			if (null == a.getUsers() && null != a.getUserIds()) {
				List<Long> userIds = a.getUserIds();
				for (Long uid : userIds) {
					userAccountIdMap.put(uid, a);
					batchUid.add(uid);
				}

			}
		}
		List<User> users = this.getUserBatch(schoolId, batchUid);
		//用户增加学校条件过滤
		List<Grade> gradeList=this.getGradeList(this.getSchoolById(schoolId));
		List<Long> cid=new ArrayList<Long>();
		for(Grade g:gradeList)
		{
			if(g==null||g.getClassIds()==null){
				continue;
			}
			cid.addAll(g.getClassIds());
		}
		
		if(users.size()>=1)
		{
			for (User u : users) {
				if(u.getUserPart().getRole().equals(T_Role.Student)&&null!=u.getStudentPart() && !cid.contains(u.getStudentPart().getClassId()))
				{
					aids.add(u.getAccountPart().getId());
					continue;
				}
				if(u.getUserPart().getRole().equals(T_Role.Teacher)&& null!=u.getTeacherPart()&&u.getTeacherPart().getSchoolId()!=schoolId)
				{
					aids.add(u.getAccountPart().getId());
					continue;
				}
				if(u.getUserPart().getRole().equals(T_Role.Staff)&& null!=u.getStaffPart()&&u.getStaffPart().getSchoolId()!=schoolId)
				{
					aids.add(u.getAccountPart().getId());
					continue;
				}
			   
				Account act = userAccountIdMap.get(u.getUserPart().getId());
				List<User> uList = new ArrayList<User>();
				if (act.getUsers() != null) {
					uList = act.getUsers();
				}
				uList.add(u);
				act.setUsers(uList);
			}
		}
		
		List<Account> delList = new ArrayList<Account>();
		for(Account a:s){
			if(a.getId()==0){
				delList.add(a);
			}
		}
		s.removeAll(delList);
		return s;
	}
	
	/**
	 * 获取全校所有教职工
	 * @param school
	 * @return
	 */
	public List<Account> getAllSchoolEmployees(School school) {
		List<Long> aids = new ArrayList<Long>();
		if(school.getTeacherAccountIds()!=null){
			aids.addAll(school.getTeacherAccountIds());
		}
		List<Long> t1 = school.getManagerAccountIds();
		if(t1==null){
			t1 = new ArrayList<Long>();
		}
		if(school.getStaffAccountIds()!=null){
			t1.addAll(school.getStaffAccountIds());
		}
		
		for(Long id:t1){
			if(!aids.contains(id)){
				aids.add(id);
			}
		}
		
		return getAccountBatch(school.getId(), aids);
	}

	/**
	 * 批量异步获取账号用户，Account只包含userIds
	 * 
	 * @param schoolId
	 *            学校id
	 * @param accountIds
	 *            账号ids
	 * @return
	 */
//	public List<Account> getAccountBatchAsync(long schoolId, List<Long> accountIds) {
//		sdkHeader.setSchoolId(schoolId);
//		final List<Account> s = new ArrayList<Account>();
//		if (null != accountIds && accountIds.size() > 0) {
//			try {
//				SDKClient.getAccountBatchAsync(new Listener<AccountList>() {
//					@Override
//					public void onResponse(AccountList response) {
//						if (response != null && null != response.getAccounts()) {
//							s.addAll(response.getAccounts());
//						}
//					}
//
//					@Override
//					public void onErrorResponse(String msg, int retCode) {
//						logger.error(msg + ", retCode:" + retCode);
//					}
//				}, accountIds, sdkHeader);
//				logger.info("【基础数据接口】根据账户id异步获取账户列表[[正常]]，方法getAccountBatchAsync，入参schoolId:{},accountIds:{} ,出参:{} ",schoolId,accountIds,s);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				
//				logger.error("【基础数据接口】根据账户id异步获取账户列表[【出错】]，方法getAccountBatchAsync，入参schoolId:{},accountIds:{}  ",schoolId,accountIds);
//			}
//		}
////		long time1=new Date().getTime();
////		System.out.println(Thread.currentThread().getName()+"    getAccountBatch:"+(time1-time0));
//		return s;
//	}

	/**
	 * 获取用户权限 勿删
	 * 
	 * @param schoolId
	 *            学校id
	 * @param userId
	 * @return
	 */
	public UserPermissions getUserPermissionById(long schoolId, long userId) {
		sdkHeader.setSchoolId(schoolId);
		final UserPermissions[] up = new UserPermissions[1];

		try {
			SDKClient.getUserPermissionsAllById(new Listener<UserPermissions>() {
				@Override
				public void onResponse(UserPermissions response) {
					if (response != null) {
						up[0] = response;
					}
				}

				@Override
				public void onErrorResponse(String msg, int retCode) {
					logger.error(msg + ", retCode:" + retCode);
				}
			}, userId, sdkHeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("【基础数据接口】根据用户id获取用户权限[【出错】]，方法getUserPermissionsAllById，入参schoolId:{},userId:{}  ",schoolId,userId);
		}

		if( up[0]==null){
			logger.error("【基础数据接口】根据用户id获取用户权限[[为空]]，方法getUserPermissionsAllById，入参schoolId:{},userId:{}  ",schoolId,userId);
			
		}else{
			
			logger.info("【基础数据接口】根据用户id获取用户权限[[正常]]，方法getUserPermissionsAllById，入参schoolId:{},userId:{},出参:{}  ",schoolId,userId,up[0]);
		}
		return up[0];

	}

	/**
	 * 更新班级对象
	 * 
	 * @param schoolId
	 *            学校id
	 * @param c
	 * @return
	 */
	public void updateClassroom(long schoolId, Classroom c) {
		if (null != c) {
			sdkHeader.setSchoolId(schoolId);
			try {
				SDKClient.updateClassroom(new Listener<Classroom>() {
					@Override
					public void onResponse(Classroom response) {
						if (response != null) {
						}
					}

					@Override
					public void onErrorResponse(String msg, int retCode) {
						logger.error(msg + ", retCode:" + retCode);
					}
				}, c, sdkHeader);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				logger.error("【基础数据接口】回写班级信息[【出错】]，方法updateClassroom，入参schoolId:{},Classroom:{}  ",schoolId,c);
			}
		}

	}
	
/**
 * 更新任课教师的课程
 * @param schoolId 学校id
 * @param teacherId 任课教师的accountId
 * @param courseIds 课程List<Course>
 */
	public void updateTeacherLesson(long schoolId,long teacherId, List<Course> courseIds) {
		if (null != courseIds) {
			sdkHeader.setSchoolId(schoolId);
			try {
				SDKClient.updateTeacherLesson(new Listener<UpdateTeacherLessonRsp>() {
					@Override
					public void onErrorResponse(String msg, int retCode) {
						logger.error(msg + ", retCode:" + retCode);
					}

					@Override
					public void onResponse(UpdateTeacherLessonRsp response) {					
					}
				}, teacherId, courseIds, schoolId, sdkHeader);
				
				logger.info("【基础数据接口】回写任课教师的课程[[正常]]，方法updateTeacherLessonRequest，入参schoolId:{},teacherId:{},courseIds:{}  ",schoolId,teacherId,courseIds);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				logger.error("【基础数据接口】回写任课教师课程信息[【出错】]，方法updateTeacherLessonRequest，入参schoolId:{},teacherId:{},courseIds:{}  ",schoolId,teacherId,courseIds);
			}
			
		}
	}
	
	/**
	 * 批量更新任课教师的课程
	 * @param schoolId 学校id
	 * @param teacherCourseMap 任课教师课程map
	 */
		public void updateTeacherLessonBatch(long schoolId,Map<Long,List<Course>> teacherCourseMap) {
			if (null != teacherCourseMap) {
				sdkHeader.setSchoolId(schoolId);
				try {
					SDKClient.updateTeacherLessonBatch(new Listener<UpdateTeacherLessonBatchRsp>(){
						@Override
						public void onErrorResponse(String msg, int retCode) {
							logger.error(msg + ", retCode:" + retCode);
						}
						@Override
						public void onResponse(UpdateTeacherLessonBatchRsp response) {
							// TODO Auto-generated method stub
							
						}
					}, teacherCourseMap, schoolId, sdkHeader);
					
					logger.info("【基础数据接口】批量回写任课教师的课程[[正常]]，方法updateTeacherLessonBatch，入参schoolId:{},teacherCourseMap:{}",schoolId,teacherCourseMap);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					logger.error("【基础数据接口】批量回写任课教师课程信息[【出错】]，方法updateTeacherLessonBatch，入参schoolId:{},teacherCourseMap:{}",schoolId,teacherCourseMap);
				}
				
			}
		}

	/**
	 * 获取两个获取单个list集合的交集（去重）
	 * @param gradeList 年级组
	 * @param researchList 教研组
	 * @param name 教师姓名
	 * @return
	 */
    public List<Account> getListIntersection(List<Account> gradeList, List<Account> researchList,String name)
    {
		HashMap<Account,String> userMap=new HashMap<Account,String>();
		List<Account> list=new ArrayList<Account>();
		if(null!=gradeList&&gradeList.size()>0 && null!=researchList&&researchList.size()>0)
		{
				for(Account u:gradeList)
				{
					if(!userMap.containsKey(u)&&researchList.contains(u))
					{
						if(StringUtils.isNotEmpty(name))
						{
							String n=u.getName();
							if(n.contains(name))
							{
								userMap.put(u, "1");
								list.add(u);
							}
						}
						else
						{
							userMap.put(u, "1");
							list.add(u);
						}
					}
				}
		}//获取单种类型机构教师
		else if(null!=gradeList&&gradeList.size()>0&&(researchList==null||researchList.size()==0))
		{
			for(Account u:gradeList)
			{
				if(!userMap.containsKey(u))
				{
					if(StringUtils.isNotEmpty(name))
					{
						String n=u.getName();
						if(n.contains(name))
						{
							userMap.put(u, "1");
							list.add(u);
						}
					}
					else
					{
						userMap.put(u, "1");
						list.add(u);
					}
				}
			}
		}else if((gradeList==null||gradeList.size()==0)&&null!=researchList&&researchList.size()>0){
			for(Account u:researchList)
			{
				if(!userMap.containsKey(u))
				{
					if(StringUtils.isNotEmpty(name))
					{
						String n=u.getName();
						if(n.contains(name))
						{
							userMap.put(u, "1");
							list.add(u);
						}
					}
					else
					{
						userMap.put(u, "1");
						list.add(u);
					}
				}
			}
		}
		return list;
    }
    

	public String getSynjByGrade(Grade grade, String xn) {
		String njdm = grade.getCurrentLevel().getValue() + "";
		String synj = this.ConvertNJDM2SYNJ(njdm, xn);

		return synj;
	}

	
	
	

	public static SDKHeader getSdkHeader() {
		return sdkHeader;
	}

	public static void setSdkHeader(SDKHeader sdkHeader) {
		CommonDataServicebak.sdkHeader = sdkHeader;
	}
	
	

}
