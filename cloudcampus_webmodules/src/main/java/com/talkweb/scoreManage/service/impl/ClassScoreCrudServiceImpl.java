package com.talkweb.scoreManage.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.api.message.utils.MessageNoticeModelEnum;
import com.talkweb.api.message.utils.MessageNoticeTypeEnum;
import com.talkweb.api.message.utils.MessageNoticeUserTypeEnum;
import com.talkweb.api.message.utils.MessageServiceEnum;
import com.talkweb.api.message.utils.MessageSmsEnum;
import com.talkweb.api.message.utils.MessageStatusEnum;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.CurCommonDataService;
import com.talkweb.commondata.service.MotanService;
import com.talkweb.creditServer.CreditCmdID;
import com.talkweb.scoreManage.action.ScoreReleaseAction;
import com.talkweb.scoreManage.dao.ClassScoreCrudDao;
import com.talkweb.scoreManage.po.ce.ClassExamInfo;
import com.talkweb.scoreManage.po.ce.ClassExamRelative;
import com.talkweb.scoreManage.po.ce.ClassExamSubjectScore;
import com.talkweb.scoreManage.po.ce.ClassTotalScoreStatistics;
import com.talkweb.scoreManage.po.ce.CustomScore;
import com.talkweb.scoreManage.po.ce.CustomScoreInfo;
import com.talkweb.scoreManage.po.ce.StudentTotalScoreStatistics;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.proc.ClassExamExcelDetail;
import com.talkweb.scoreManage.proc.ClassExamExcelTitle;
import com.talkweb.scoreManage.service.ClassScoreCrudService;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.student.domain.page.StartImportTaskParam;

@Service
public class ClassScoreCrudServiceImpl implements ClassScoreCrudService {
	private static final Logger logger = LoggerFactory.getLogger(ScoreReleaseAction.class);

	@Autowired
	private CurCommonDataService curCommonDataService;

	@Autowired
	private AllCommonDataService allCommonDataService;

	@Autowired
	private ClassScoreCrudDao classScoreCrudDao;

	@Autowired
	private ScoreManageService scoreService;

	@Value("#{settings['app.score.url']}")
	private String url;

	@Value("#{settings['app.score.switch']}")
	private String sswitch;// 是否启动同步考试状态的功能：0关闭，1启动

	@Value("#{settings['scoreReportFilePath']}")
	private String filePath;

	@Override
	public List<JSONObject> getExamList(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		return classScoreCrudDao.getExamList(termInfoId, params);
	}

	@Override
	public int createExam(JSONObject params) {
		String termInfoId = params.getString("termInfoId");

		if (classScoreCrudDao.ifExistsSameNameInClassExamInfo(termInfoId, params)) {
			throw new CommonRunException(-1, "同学年学期已存在相同名称的考试，请修改考试名称！");
		}

		ClassExamInfo classExamInfo = new ClassExamInfo();
		classExamInfo.setExamId(UUIDUtil.getUUID());
		classExamInfo.setExamName(params.getString("examName"));
		classExamInfo.setSchoolId(params.getString("schoolId"));
		classExamInfo.setTermInfoId(params.getString("termInfoId"));
		classExamInfo.setCreateUserId(params.getString("accountId"));
		classExamInfo.setCreateTime(DateUtil.getDateFormatNow());
		classExamInfo.setUpdaeTime(DateUtil.getDateFormatNow());

		return classScoreCrudDao.createExam(termInfoId, classExamInfo);
	}

	@Override
	public int updateExam(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		if (classScoreCrudDao.ifExistsSameNameInClassExamInfo(termInfoId, params)) {
			throw new CommonRunException(-1, "同学年学期已存在相同名称的考试，请修改考试名称！");
		}
		return classScoreCrudDao.updateClassExam(termInfoId, params);
	}

	@Override
	public void deleteExam(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo exam = classScoreCrudDao.getClassExamInfoById(termInfoId, params);
		if (exam == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = exam.getAutoIncr();
		classScoreCrudDao.deleteExam(termInfoId, autoIncr, params);

		Integer isImport = exam.getIsImport();
		if (isImport == 1 && "1".equals(sswitch)) {// 调用访问远程接口的http方法
			Map<String, Object> syncParam = new HashMap<String, Object>();
			syncParam.put("teacherId", params.get("accountId"));
			syncParam.put("examUuId", params.get("examId"));
			String responseResult = scoreService.updateHttpRemoteInterface(url + "/SendExam/deleteExam", syncParam);

			JSONObject json = (JSONObject) JSONObject.parse(responseResult);
			if (json == null || !"0".equals(json.getString("code")) && !"10300".equals(json.getString("code"))) {
				throw new CommonRunException(-1, "同步删除成绩失败!");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public int updateReleaseExam(JSONObject params) throws RuntimeException {
		logger.info("updateReleaseExam："+params);
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo exam = classScoreCrudDao.getClassExamInfoById(termInfoId, params);
		if (exam == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		logger.info("exam："+exam);
		Integer autoIncr = exam.getAutoIncr();

		String examId = params.getString("examId");
		Integer pub2StuFlag = params.getInteger("pub2StuFlag");// 0：取消发布，1:发布
		long accountId = params.getLongValue("accountId");
		String schoolId = params.getString("schoolId");

		int isShow = 0;// “1”：显示获取多少积分情况提示,0：不显示获取多少积分情况提示

		JSONObject updateExamParams = new JSONObject();
		updateExamParams.put("examId", examId);
		updateExamParams.put("termInfoId", termInfoId);
		updateExamParams.put("schoolId", schoolId);
		updateExamParams.put("isPublic", pub2StuFlag);
		updateExamParams.put("publicTime", params.get("time"));
		int num = classScoreCrudDao.updateClassExam(termInfoId, updateExamParams);

		if (num > 0) {
			List<Long> stuIds = new ArrayList();
			Integer examType = exam.getExamType(); // 1、系统模板，2、自定义导入
			if (examType == null) {
				examType = 1;
			}
			if (examType == 1) {// 系统导入
				// 推送消息
				if (pub2StuFlag == 1) {
					params.put("examType", examType);
					List<JSONObject> objs = classScoreCrudDao.getInfoFromClassExamSubjectScore(termInfoId, autoIncr,
							params);
					if (objs.size() > 0) {
						for (JSONObject obj : objs) {
							List<Long> ids = (List<Long>) obj.get("ids");
							stuIds.addAll(ids);
							T_Role role = (T_Role) obj.get("role");
							sendMsg(schoolId, ids, role);
						}
					}

					// 调用积分接口
					long userId = 0;
					Account account = allCommonDataService.getAccountAllById(Long.valueOf(schoolId), accountId, termInfoId);					List<User> users = account.getUsers();
					for (User user : users) {
						T_Role role = user.getUserPart().getRole();
						if (role.equals(T_Role.Teacher)) {
							if (user.getTeacherPart() != null
									&& user.getTeacherPart().getSchoolId() == Long.parseLong(schoolId)) {
								userId = user.getTeacherPart().getId();
								break;
							}
						} else if (role.equals(T_Role.Staff)) {
							if (user.getTeacherPart() != null
									&& user.getTeacherPart().getSchoolId() == Long.parseLong(schoolId)) {
								userId = user.getStaffPart().getId();
							}
						}
					}
					if (userId != 0) {
						if ("1".equals(sswitch)) {
							int res = curCommonDataService.updateUserCreditExtern(CreditCmdID.SendScoreReport, userId);
							logger.error("RRRRR：" + res);
							if (res == -1) {
								logger.error("成绩积分接口调用失败：" + accountId);
							}
							if (res == 0) {
								isShow = 1;
							}
						}
					}
				}

			} else {// 自定义导入
				if (pub2StuFlag == 1) {
					params.put("examType", examType);
					List<JSONObject> objs = classScoreCrudDao.getInfoFromClassExamSubjectScore(termInfoId, autoIncr,
							params);
					if (objs.size() > 0) {
						for (JSONObject obj : objs) {
							List<Long> ids = (List<Long>) obj.get("ids");
							T_Role role = (T_Role) obj.get("role");
							sendMsg(schoolId, ids, role);
						}
					}

					// 调用积分接口
					long userId = 0;
					Account account = allCommonDataService.getAccountAllById(Long.valueOf(schoolId), accountId, termInfoId);
					List<User> users = account.getUsers();
					for (User user : users) {
					    if (user.getUserPart()== null) {
							continue;
						}
						T_Role role = user.getUserPart().getRole();
						if (role.equals(T_Role.Teacher)) {
							if (user.getTeacherPart() != null
									&& user.getTeacherPart().getSchoolId() == Long.parseLong(schoolId)) {
								userId = user.getTeacherPart().getId();
								break;
							}
						} else if (role.equals(T_Role.Staff)) {
							if (user.getStaffPart() != null
									&& user.getStaffPart().getSchoolId() == Long.parseLong(schoolId)) {
								userId = user.getStaffPart().getId();
							}
						}
					}
					if (userId != 0) {
						if ("1".equals(sswitch)) {
							int res = curCommonDataService.updateUserCreditExtern(CreditCmdID.SendScoreReport, userId);
							logger.error("RRRRR：" + res);
							if (res == -1) {
								logger.error("成绩积分接口调用失败：" + accountId);
							}

							if (res == 0) {
								isShow = 1;
							}
						}
					}

				}
			}
			
			// 发布发送消息模板
			if(pub2StuFlag == 1 && stuIds.size() > 0) {
				DegreeInfo egreeInfo = new DegreeInfo();
				egreeInfo.setXxdm(schoolId);
				egreeInfo.setXnxq(termInfoId);
				egreeInfo.setKslcmc(exam.getExamName());
				egreeInfo.setEexamId(examId);
				
				scoreService.sendWXMsg(egreeInfo, stuIds, "班级小考", examType == 1 ? 2:3,"Parent");
				
				/*if(pub2StuFlag == 1)
					scoreService.sendWXMsg(egreeInfo, stuIds, "班级小考", examType == 1 ? 2:3,"Parent");
				else
					scoreService.sendWXMsg(egreeInfo, stuIds, "班级小考", examType == 1 ? 2:3,"Teacher");*/
			}
			
			// if ("1".equals(scoreDataType)) {
			// if ("1".equals(sswitch)) {
			// // 发布成绩
			// Map<String, Object> syncParam = new HashMap<String, Object>();
			// syncParam.put("examStatus", pub2StuFlag);
			// syncParam.put("examUuId", examId);
			// syncParam.put("accountId", accountId);
			// syncParam.put("schoolId", schoolId);
			//
			// // 调用访问远程接口的http方法
			// String responseResult = scoreService.updateHttpRemoteInterface(
			// url + "/SendExam/synExamStatus", syncParam);
			//
			// System.out.println(responseResult);
			//
			// JSONObject json = (JSONObject) JSONObject.parse(responseResult);
			//// if (json == null || (!"0".equals(json.getString("code")))) {
			// //
			//// if ("10300".equals(json.getString("code"))) {
			//// throw new RuntimeException("正在扫描成绩中，请稍后" + actionMsg
			//// + "成绩!");
			//// } else {
			//// throw new RuntimeException(actionMsg + "成绩失败!");
			//// }
			// //
			//// }
			//
			// String creditText = json == null ? "" : json
			// .getString("creditText");
			//
			// if (!StringUtil.isEmpty(creditText)) {
			// isShow = 1;
			// }
			// }
			// } else {
			// //isShow = 1;
			// }
		}
		return isShow;
	}

	/***
	 * 在线通知和离线通知接口
	 * 
	 * @param schoolId
	 *            学校代码
	 * @param userIds
	 *            用户代码集合
	 * @param accountIds
	 *            用户accountIds集合
	 * @param serviceId
	 *            业务类型代码
	 * @param optionType
	 * @param serviceType
	 * @throws Exception
	 */
	private void sendMsg(String schoolId, List<Long> ids, T_Role role) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("noticeServiceType", MessageServiceEnum.scoreManage.toInteger());
		jsonObject.put("noticeDate", new Date());
		jsonObject.put("schoolId", schoolId);
		jsonObject.put("noticeType", MessageNoticeTypeEnum.TIMELY.toInteger());
		jsonObject.put("needConfirm", "0");
		jsonObject.put("noticeUserType", MessageNoticeUserTypeEnum.PARTSTEACHER.toInteger());
		jsonObject.put("noticeModel", MessageNoticeModelEnum.DEFAULT.toInteger());
		jsonObject.put("noticeStatus", MessageStatusEnum.SUCCESS.toInteger());
		jsonObject.put("noticeSendDate", new Date());
		// noticeOperate
		jsonObject.put("sendMsgStatus", MessageSmsEnum.DEFAULT.toInteger());
		List<JSONObject> noticeDetails = new ArrayList<JSONObject>();
		if (CollectionUtils.isNotEmpty(ids) && ids.size() > 0) {
			// 批量发送
			int limit = 3000;
			int batchSize = ids.size() / limit;
			for (int batch = 0; batch <= batchSize; batch++) {
				int fromIndex = batch * limit;
				int toIndex = fromIndex + limit;
				toIndex = toIndex > ids.size() ? ids.size() : toIndex;
				for (Long id : ids.subList(fromIndex, toIndex)) {
					JSONObject noticeDetail = new JSONObject();
					noticeDetail.put("noticeDetailsContent", "您有一条新的成绩请查看");
					noticeDetail.put("userType", role.getValue());
					noticeDetail.put("schoolId", schoolId);
					noticeDetail.put("accountId", id);
					noticeDetail.put("isStatus", "0");
					noticeDetails.add(noticeDetail);
				}
				jsonObject.put("noticeDetails", noticeDetails);
				logger.info("\n ========== 开始调用RPC noticeMessage()方法============");
				String result = null;
				try {
					result = MotanService.noticeMessage(jsonObject);
				} catch (Exception e) {
					logger.error("成绩消息发送失败：" + ids);
					// throw new RuntimeException("消息发送失败，请联系管理员!");
				}
				logger.info("\n ================ 返回结果：" + result + " ==============");
				JSONObject res = JSONObject.parseObject(result);
				if ("-1".equals(res.getString("retcode"))) {
					logger.error("调用接口返回结果：" + result + "，成绩消息发送失败：" + ids);
					// throw new RuntimeException("消息发送失败，请联系管理员!");
				}
			}

		}
	}

	@Override
	public void addImport(List<ClassExamExcelDetail> successInfos, List<ClassExamExcelTitle> l_salExcel) {
		String examId = successInfos.get(0).getExamId();
		String schoolId = successInfos.get(0).getSchoolId();
		String termInfoId = successInfos.get(0).getXnxq();

		JSONObject params = new JSONObject();
		params.put("examId", examId);
		params.put("schoolId", schoolId);
		params.put("termInfoId", termInfoId);
		ClassExamInfo exam = classScoreCrudDao.getClassExamInfoById(termInfoId, params);
		if (exam == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = exam.getAutoIncr();

		// TODO Auto-generated method stub
		List<CustomScoreInfo> l_params = new ArrayList<CustomScoreInfo>();
		List<CustomScore> l_excel = new ArrayList<CustomScore>();
		List<ClassExamRelative> l_eclass = new ArrayList<ClassExamRelative>();

		Set<String> classIds = new HashSet<String>();
		for (ClassExamExcelDetail sd : successInfos) {
			for (int i = 0; i < sd.getCellIds().size(); i++) {
				CustomScoreInfo customScoreInfo = new CustomScoreInfo();
				customScoreInfo.setExamId(sd.getExamId());
				customScoreInfo.setSchoolId(sd.getSchoolId());
				customScoreInfo.setStudentId(sd.getStuId());
				customScoreInfo.setClassId(sd.getClassId());
				customScoreInfo.setProjectId(sd.getCellIds().get(i));
				customScoreInfo.setScore(sd.getCellValues().get(i));
				customScoreInfo.setTermInfoId(termInfoId);
				customScoreInfo.setInputTime(DateUtil.getDateFormatNow());
				l_params.add(customScoreInfo);

				classIds.add(sd.getClassId());
			}
		}
		for (ClassExamExcelTitle se : l_salExcel) {
			CustomScore customScore = new CustomScore();
			customScore.setExamId(se.getExamId());
			customScore.setSchoolId(se.getSchoolId());
			customScore.setProjectId(se.getCellId());
			customScore.setProjectName(se.getTitleName());
			customScore.setParentProjectId(se.getP_cellId());
			customScore.setRowSpan(se.getRowspan());
			customScore.setColSpan(se.getColspan());
			customScore.setRow(se.getIn_rowNum());
			customScore.setCol(se.getIn_colNum());
			customScore.setTitleCount(se.getHead_rowNum());
			customScore.setTermInfoId(termInfoId);
			customScore.setInputTime(DateUtil.getDateFormatNow());
			l_excel.add(customScore);
		}

		for (String classId : classIds) {
			ClassExamRelative classExamRelative = new ClassExamRelative();
			classExamRelative.setExamId(examId);
			classExamRelative.setSchoolId(schoolId);
			classExamRelative.setInputTime(DateUtil.getDateFormatNow());
			classExamRelative.setClassId(classId);
			classExamRelative.setTermInfoId(termInfoId);
			l_eclass.add(classExamRelative);
		}
		// 在重新导入之前，删除已经导入的信息
		classScoreCrudDao.deleteCustomScore(termInfoId, autoIncr, params);
		classScoreCrudDao.deleteCustomScoreInfo(termInfoId, autoIncr, params);
		classScoreCrudDao.deleteClassExamClassListTwo(termInfoId, autoIncr, params);

		classScoreCrudDao.insertClassExamRelative(termInfoId, autoIncr, l_eclass);
		classScoreCrudDao.insertCustomScore(termInfoId, autoIncr, l_excel);
		classScoreCrudDao.insertCustomScoreInfo(termInfoId, autoIncr, l_params);

		params.put("updateTime", DateUtil.getDateFormatNow());
		params.put("isImport", 1); // 是否已经导入
		params.put("examType", 2); // 1：系统格式，2：自定义考试
		params.put("scoreDataType", 2);
		classScoreCrudDao.updateClassExam(termInfoId, params);
	}

	@Override
	public void insertClassExamScoreBatch(List<ClassExamSubjectScore> scoreList, StartImportTaskParam taskParam,
			int scoreDataType) {
		String termInfoId = taskParam.getXn() + taskParam.getXq();
		String schoolId = taskParam.getXxdm();
		String examId = taskParam.getKslc();

		JSONObject params = new JSONObject();
		params.put("examId", examId);
		params.put("schoolId", schoolId);
		params.put("termInfoId", termInfoId);
		ClassExamInfo exam = classScoreCrudDao.getClassExamInfoById(termInfoId, params);
		if (exam == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = exam.getAutoIncr();

		if (scoreList.size() > 0) {
			classScoreCrudDao.insertClassExamScoreBatch(termInfoId, autoIncr, scoreList, scoreDataType);
		}

		// 添加考试所属班级，考试科目列表
		params.put("scoreDataType", scoreDataType);
		this.addClassExamRelative(termInfoId, autoIncr, params);

		if (scoreDataType == 1) {
			// 学生总分分统计
			Map<String, List<StudentTotalScoreStatistics>> classStudentToltalScore = this
					.statisticsStudentTotalSore(termInfoId, autoIncr, params);
			// 班级总分统计结果
			this.statisticsClassStudentTotalSore(termInfoId, autoIncr, classStudentToltalScore, params);
			// 生成app学生成绩报告json文件
			this.produceAppStudentScoreReport(termInfoId, autoIncr, exam.getExamName(), params);
		}
		
		params.put("updateTime", DateUtil.getDateFormatNow());
		params.put("isImport", 1); // 是否已经导入
		params.put("examType", 1); // 1：系统格式，2：自定义考试
		params.put("scoreDataType", 1);
		classScoreCrudDao.updateClassExam(termInfoId, params);
	}

	private int addClassExamRelative(String termInfoId, Integer autoIncr, JSONObject params) {
		// 设置参数
		params.put("inputTime", DateUtil.getDateFormatNow());
		params.put("isImport", 1);

		// 删除考试所属班级列表，考试科目列表
		classScoreCrudDao.deleteClassExamSubjectList(termInfoId, autoIncr, params);
		classScoreCrudDao.deleteClassExamClassList(termInfoId, autoIncr, params);

		// 添加考试所属班级列表，考试科目列表
		int addSubjectNum = classScoreCrudDao.addClassExamSubjectList(termInfoId, autoIncr, params);
		int addClassNum = classScoreCrudDao.addClassExamClassList(termInfoId, autoIncr, params);
		int updateClassExamFlag = 0;

		if (addSubjectNum > 0 && addClassNum > 0) {
			updateClassExamFlag = classScoreCrudDao.updateClassExam(termInfoId, params);
		}

		params.remove("isImport");
		params.remove("inputTime");
		return updateClassExamFlag;
	}

	/****
	 * 统计学生总分分析结果和排名
	 * 
	 * @param schoolId,
	 * @param examId
	 */
	private Map<String, List<StudentTotalScoreStatistics>> statisticsStudentTotalSore(String termInfoId,
			Integer autoIncr, JSONObject params) {
		// 学生总分列表
		List<StudentTotalScoreStatistics> subjectTotalScoreList = classScoreCrudDao
				.selectClassExamSubjectScore(termInfoId, autoIncr, params);

		// 2.计算学生总分班级排名已经击败率
		Map<String, List<StudentTotalScoreStatistics>> classStudentInfo = new HashMap<String, List<StudentTotalScoreStatistics>>();// 班级学生信息
		if (subjectTotalScoreList.size() > 0) {// 学生总分列表数据不为空的时候才予以统计分析
			// 2.1首先把学生信息放置到Map中，以班号作为键
			for (StudentTotalScoreStatistics subjectTotalScore : subjectTotalScoreList) {
				String classId = subjectTotalScore.getClassId();
				if (!classStudentInfo.containsKey(classId)) {
					classStudentInfo.put(classId, new ArrayList<StudentTotalScoreStatistics>());
				}

				classStudentInfo.get(classId).add(subjectTotalScore);
			}

			for (Map.Entry<String, List<StudentTotalScoreStatistics>> entry : classStudentInfo.entrySet()) {
				List<StudentTotalScoreStatistics> classStudentScoreList = entry.getValue();// 指定班级的成绩数据列表
				int position = 1;// 记录索引
				int currentRank = 1;// 当前名次
				float lastTotalScore = classStudentScoreList.get(0).getTotalScore(); // 上次总分

				// 相同的成绩应该是相同的名次
				for (StudentTotalScoreStatistics classStudentScore : classStudentScoreList) {
					float currentTotalScore = classStudentScore.getTotalScore();

					// 计算班级排名逻辑
					if (currentTotalScore == lastTotalScore) {
						classStudentScore.setTotalScoreRank(currentRank);
					} else { // 不相等的时候，当前学生不与上一名同学同名，表示名次要下移
						classStudentScore.setTotalScoreRank(position);
						currentRank = position;
						lastTotalScore = currentTotalScore;
					}
					position++;
				}
			}

			// 2.3计算学生击败率
			for (StudentTotalScoreStatistics subjectTotalScore : subjectTotalScoreList) {
				int i = 0;
				String classId = subjectTotalScore.getClassId();
				float currentTotalScore = subjectTotalScore.getTotalScore();

				List<StudentTotalScoreStatistics> classStudentScoreList = classStudentInfo.get(classId);// 指定班级的成绩数据列表

				// 循环比较比当前学生成绩差的学生数量
				for (StudentTotalScoreStatistics subjectTotalScoreTwo : classStudentScoreList) {
					float currentTotalScoreTwo = subjectTotalScoreTwo.getTotalScore();
					if (currentTotalScore > currentTotalScoreTwo) {
						i++;
					}
				}
				subjectTotalScore.setInputTime(DateUtil.getDateFormatNow()); // 设置时间
				subjectTotalScore.setSurpassRatio(Integer.valueOf(i * 100 / (classStudentScoreList.size()))); // 设置击败率
				subjectTotalScore.setClassStudentNum(classStudentScoreList.size()); // 设置班级人数
			}

			classScoreCrudDao.addClassExamStudentTotalScore(termInfoId, autoIncr, subjectTotalScoreList);
		}

		return classStudentInfo;
	}

	/****
	 * 统计班级学生总分的平均分，最高分，总分第一，第二，第三分析结果
	 * 
	 * @param schoolId,
	 * @param examId
	 */
	private void statisticsClassStudentTotalSore(String termInfoId, Integer autoIncr,
			Map<String, List<StudentTotalScoreStatistics>> classStudentToltalScore, JSONObject params) {
		String examId = params.getString("examId");
		String schoolId = params.getString("schoolId");

		List<ClassTotalScoreStatistics> ClassStudentTotalSoreList = new ArrayList<ClassTotalScoreStatistics>();
		// 按照班级逐一计算每个班级的总分平均分，最高分等属性
		for (Map.Entry<String, List<StudentTotalScoreStatistics>> entry : classStudentToltalScore.entrySet()) {
			// 存放每个班级的成绩分析结果
			String classId = entry.getKey();
			List<StudentTotalScoreStatistics> classStudentScoreList = entry.getValue();// 指定班级的成绩数据列表

			float classTotalScoreAverScore = 0;// 总分平均分
			float classTopTotalScore = 0;// 总分最高分
			float classTotalScore = 0;

			float topOneScore = 0;// 总分第一
			float topTwoScore = 0;// 总分第二
			float topThreeScore = 0;// 总分第三
			int topTwoSocreIndex = 0;// 总分第二索引，用来表示已经计算出第二高分
			int topThreeSocreIndex = 0;// 总分第三索引，用来表示已经计算出第三高分

			classTopTotalScore = classStudentScoreList.get(0).getTotalScore();
			topOneScore = classTopTotalScore;// 把第一高分设置总分最高分
			for (StudentTotalScoreStatistics subjectTotalScore : classStudentScoreList) {
				float currentTotalScore = subjectTotalScore.getTotalScore();
				classTotalScore += currentTotalScore;

				if (currentTotalScore < classTopTotalScore && topTwoSocreIndex < 1) {// 如果当前分数小于最高分，并且是第一次，则表示是第二高分
					topTwoScore = currentTotalScore;
					topTwoSocreIndex++;
				}

				if (currentTotalScore < topTwoScore && topTwoScore > 0 && topThreeSocreIndex < 1) {// 如果当前分数小于第二高分，并且是第一次，则表示是第三高分
					topThreeScore = currentTotalScore;
					topThreeSocreIndex++;
				}
			}

			// 计算总分的班级平均分
			classTotalScoreAverScore = classTotalScore / classStudentScoreList.size();

			ClassTotalScoreStatistics statistic = new ClassTotalScoreStatistics();
			// 设置成绩所有属性
			statistic.setExamId(examId);
			statistic.setSchoolId(schoolId);
			statistic.setTermInfoId(termInfoId);
			statistic.setClassId(classId);
			statistic.setClassStudentNum(classStudentScoreList.size());
			// 保留2位小数
			statistic.setClassTotalScoreAverScore(((float) Math.round(classTotalScoreAverScore * 100)) / 100);
			statistic.setClassTopTotalScore(classTopTotalScore);
			statistic.setTopOneScore(topOneScore);
			statistic.setTopTwoScore(topTwoScore);
			statistic.setTopThreeScore(topThreeScore);
			statistic.setInputTime(DateUtil.getDateFormatNow());

			// 把每个班级的成绩统计分析结果放置到列表集合中
			ClassStudentTotalSoreList.add(statistic);
		}

		// 保存到数据库
		if (ClassStudentTotalSoreList.size() > 0) {
			classScoreCrudDao.addClassExamClassTotalScore(termInfoId, autoIncr, ClassStudentTotalSoreList);
		}
	}

	private void produceAppStudentScoreReport(String termInfoId, Integer autoIncr, String examName, JSONObject params) {
		Long schoolId = params.getLong("schoolId");

		Map<String, String> classId2NameMap = new HashMap<String, String>();
		List<Long> classIdList = classScoreCrudDao.selectClassExamClassIdList(termInfoId, autoIncr, params);
		if (classIdList.size() > 0) {
			List<Classroom> classrooms = allCommonDataService.getClassroomBatch(schoolId, classIdList, termInfoId);
			if (CollectionUtils.isNotEmpty(classrooms)) {
				for (Classroom room : classrooms) {
					if (room == null) {
						continue;
					}
					String classId = String.valueOf(room.getId());
					if (!classId2NameMap.containsKey(classId)) {
						classId2NameMap.put(classId, room.getClassName());
					}
				}
			}
		}

		Map<String, String> subjId2NameMap = new HashMap<String, String>();
		List<Long> subjectIdList = classScoreCrudDao.selectClassExamSubjectIdList(termInfoId, autoIncr, params);
		if (subjectIdList.size() > 0) {
			List<LessonInfo> lessonInfos = allCommonDataService.getLessonInfoBatch(StringUtil.transformLong(schoolId),
					subjectIdList, termInfoId);
			if (CollectionUtils.isNotEmpty(lessonInfos)) {
				for (LessonInfo lessonInfo : lessonInfos) {
					if (lessonInfo == null) {
						continue;
					}
					String lessonId = String.valueOf(lessonInfo.getId());
					if (!subjId2NameMap.containsKey(lessonId))
						subjId2NameMap.put(lessonId, lessonInfo.getName());
				}
			}
		}

		HashMap<String, Object> studentParam = new HashMap<String, Object>();
		studentParam.put("schoolId", schoolId);
		studentParam.put("termInfoId", termInfoId);
		studentParam.put("classId", StringUtil.toStringBySeparator(classIdList, null));

		Map<String, String> studId2NameMap = new HashMap<String, String>();
		List<Account> accList = allCommonDataService.getStudentList(studentParam);
		if (CollectionUtils.isNotEmpty(accList)) {
			for (Account acc : accList) {
				if (acc == null) {
					continue;
				}
				String id = String.valueOf(acc.getId());
				if (!studId2NameMap.containsKey(id)) {
					studId2NameMap.put(id, acc.getName());
				}
			}
		}

		List<StudentTotalScoreStatistics> totalScoreList = classScoreCrudDao.selectClassExamTotalScore(termInfoId,
				autoIncr, params);
		List<ClassExamSubjectScore> everySubjectScore = classScoreCrudDao.selectClassExamEverSubjectScore(termInfoId,
				autoIncr, params);

		Map<String, JSONObject> everySubjectScoreMap = new HashMap<String, JSONObject>();
		for (ClassExamSubjectScore subjectScore : everySubjectScore) {
			if (subjectScore == null) {
				continue;
			}

			String classId = subjectScore.getClassId();
			String studentId = subjectScore.getStudentId();
			String subjectId = subjectScore.getSubjectId();

			String subjectName = subjId2NameMap.get(subjectId);
			if (subjectName == null) {
				continue;
			}

			String key = classId + studentId;
			if (!everySubjectScoreMap.containsKey(key)) {
				everySubjectScoreMap.put(key, new JSONObject());
			}
			if(subjectScore.getScore() != null) {
				everySubjectScoreMap.get(key).put(subjectName, subjectScore.getScore() * 100);
			}
		}

		JSONObject result = new JSONObject();
		result.put("subjectList", subjId2NameMap);
		result.put("examTime", System.currentTimeMillis());
		result.put("classList", classId2NameMap);

		List<JSONObject> studentList = new ArrayList<JSONObject>();
		for (StudentTotalScoreStatistics totalScore : totalScoreList) {
			String classId = totalScore.getClassId();
			String studentId = totalScore.getStudentId();

			if (!classId2NameMap.containsKey(classId)) {
				continue;
			}

			JSONObject item = new JSONObject();
			item.put("accountId", StringUtil.transformLong(studentId));
			item.put("classId", StringUtil.transformLong(classId));
			item.put("name", studId2NameMap.get(studentId));
			item.put("totalScore", totalScore.getTotalScore() * 100);
			item.put("mapScore", everySubjectScoreMap.get(classId + studentId));
			studentList.add(item);
		}

		result.put("examName", examName);
		result.put("studentList", studentList);

		List<JSONObject> list = new ArrayList<JSONObject>();
		list.add(result);

		String resultString = JSONArray.toJSONString(list);

		String examId = params.getString("examId");

		if (schoolId != null && !StringUtil.isEmpty(examId)) {
			String fileName = new StringBuffer().append(schoolId).append("_").append(examId).append("_")
					.append(System.currentTimeMillis()).append(".json").toString();

			File file = new File(filePath + File.separatorChar + fileName);
			if (!file.exists()) {
				File parentFile = file.getParentFile();
				if (!parentFile.exists()) {
					file.mkdirs();
				}
				File[] fileList = parentFile.listFiles();
				if (fileList != null && fileList.length > 0) {
					for (File f : fileList) {
						if (f.isFile()) {
							String name = f.getName();
							if (name != null && name.startsWith(schoolId + "_" + examId)) {
								f.delete();
							}
						}
					}
				}

				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			FileOutputStream output = null;
			OutputStreamWriter writer = null;
			BufferedWriter bufferedWriter = null;

			ByteArrayInputStream byteInput = null;
			InputStreamReader reader = null;
			BufferedReader bufferedReader = null;

			try {
				byteInput = new ByteArrayInputStream(resultString.getBytes());
				reader = new InputStreamReader(byteInput);
				bufferedReader = new BufferedReader(reader);

				output = new FileOutputStream(file);
				writer = new OutputStreamWriter(output);
				bufferedWriter = new BufferedWriter(writer);

				String s = "";
				while ((s = bufferedReader.readLine()) != null) {
					bufferedWriter.write(s);
				}
				bufferedWriter.flush();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (output != null) {
						output.close();
					}
					if (writer != null) {
						writer.close();
					}
					if (bufferedWriter != null) {
						bufferedWriter.close();
					}
					if (byteInput != null) {
						byteInput.close();
					}
					if (reader != null) {
						reader.close();
					}
					if (bufferedReader != null) {
						bufferedReader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	@Override
	public ClassExamInfo getClassExamInfoById(String termInfoId, Map<String, Object> map) {
		return classScoreCrudDao.getClassExamInfoById(termInfoId, map);
	}

	@Override
	public int updateClassExam(String termInfoId, Map<String, Object> map) {
		return classScoreCrudDao.updateClassExam(termInfoId, map);
	}
}
