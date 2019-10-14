package com.talkweb.scoreManage.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_ClassType;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.api.message.utils.MessageNoticeModelEnum;
import com.talkweb.api.message.utils.MessageNoticeTypeEnum;
import com.talkweb.api.message.utils.MessageNoticeUserTypeEnum;
import com.talkweb.api.message.utils.MessageServiceEnum;
import com.talkweb.api.message.utils.MessageSmsEnum;
import com.talkweb.api.message.utils.MessageStatusEnum;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.TermUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.CsCurCommonDataService;
import com.talkweb.commondata.service.CurCommonDataService;
import com.talkweb.commondata.service.MotanService;
import com.talkweb.creditServer.CreditCmdID;
import com.talkweb.datadictionary.domain.TDmBjlx;
import com.talkweb.scoreManage.action.ScoreReleaseAction;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.dao.ScoreReportDao;
import com.talkweb.scoreManage.po.gm.CompetitionStu;
import com.talkweb.scoreManage.po.gm.Dbkslc;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.po.gm.DegreeInfoNj;
import com.talkweb.scoreManage.po.gm.JXPGScoreSection;
import com.talkweb.scoreManage.po.gm.ScoreInfo;
import com.talkweb.scoreManage.po.gm.ScoreLevelTemplate;
import com.talkweb.scoreManage.po.gm.ScoreMf;
import com.talkweb.scoreManage.po.gm.ScoreRankDistribute;
import com.talkweb.scoreManage.po.gm.ScoreStatTerm;
import com.talkweb.scoreManage.po.gm.ScoreTjmk;
import com.talkweb.scoreManage.po.gm.SynthScore;
import com.talkweb.scoreManage.po.gm.TopGroup;
import com.talkweb.scoreManage.po.gm.TopGroupBj;
import com.talkweb.scoreManage.po.gm.Xscjbtj;
import com.talkweb.scoreManage.po.gm.Zfqjsz;
import com.talkweb.scoreManage.po.sar.SettingBJ;
import com.talkweb.scoreManage.po.sar.SettingStu;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.student.domain.page.StartImportTaskParam;
import com.talkweb.utils.KafkaWXmsgThread;

@Service
public class ScoreManageServiceImpl implements ScoreManageService {

	@Autowired
	private CurCommonDataService curCommonDataService;

	@Autowired
	private ScoreManageDao scoreDao;
	
	@Autowired
	private ScoreReportDao scoreReportDao;

	@Autowired
	private AllCommonDataService allCommonDataService;
	
	@Autowired
	private CsCurCommonDataService curCommonService;

	@Value("#{settings['clientId']}")
	private String clientId;

	@Value("#{settings['clientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
 
	@Value("#{settings['score.msgUrlPc']}")
	private String msgUrlPc="https://pre.yunxiaoyuan.com/talkCloud/homePage.html#newNEMT/examResults";
	
	@Value("#{settings['score.msgUrlApp']}")
	private String msgUrlApp="https://pre.yunxiaoyuan.com/apph5/openH5/scoreManage/?cur=1&type=%d&examid=%s&termInfoId=%s&role=%s";
	
	private static final String MSG_TYPE_CODE = "XQFX";

	private static final Logger logger = LoggerFactory.getLogger(ScoreReleaseAction.class);

	@Value("#{settings['scoreReportFilePath']}")
	private String filePath;

	@Value("#{settings['app.score.url']}")
	private String url;

	@Value("#{settings['app.score.switch']}")
	private String sswitch;

	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;

	private static final String ERR_MSG_3 = "没有查询到相应的考试信息，请刷新页面！";

	@Override
	public DegreeInfo getDegreeInfoById(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		return degreeInfo;
	}

	@Override
	public List<JSONObject> getDegreeInfoList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		String xn = xnxq.substring(0, xnxq.length() - 1);

		List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(xnxq, params);

		Map<String, StringBuffer> id2GradeNames = new HashMap<String, StringBuffer>();
		if (degreeInfoList.size() > 0) {
			params.put("degreeInfoList", degreeInfoList);
			List<DegreeInfoNj> degreeInfoNjList = scoreDao.getDegreeInfoNjList(xnxq, params);
			for (DegreeInfoNj degreeInfoNj : degreeInfoNjList) {
				String kslcdm = degreeInfoNj.getKslcdm();
				if (!id2GradeNames.containsKey(kslcdm)) {
					id2GradeNames.put(kslcdm, new StringBuffer());
				}
				String usedGrade = degreeInfoNj.getNj();
				T_GradeLevel gl = T_GradeLevel
						.findByValue(Integer.parseInt(allCommonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
				id2GradeNames.get(kslcdm).append(njName.get(gl)).append("，");
			}
		}

		List<JSONObject> data = new ArrayList<JSONObject>();
		for (DegreeInfo degreeInfo : degreeInfoList) {
			JSONObject json = new JSONObject();
			String examId = degreeInfo.getKslcdm();
			String impStatus = degreeInfo.getDrflag();

			json.put("examId", examId);
			json.put("impStatus", impStatus);
			json.put("examName", degreeInfo.getKslcmc());
			json.put("alzStatus", degreeInfo.getFxflag());
			json.put("pub2StuStatus", degreeInfo.getFbflag());
			json.put("pub2TeaStatus", degreeInfo.getFbteaflag());
			if ("1".equals(impStatus)) {
				StringBuffer gradeNames = id2GradeNames.get(examId);
				if (gradeNames == null || gradeNames.length() == 0) {
					json.put("gradeNames", "");
				} else {
					json.put("gradeNames", gradeNames.deleteCharAt(gradeNames.length() - 1).toString());
				}
			} else {
				json.put("gradeNames", "");
			}
			data.add(json);
		}
		return data;
	}

	public JSONObject createExam(JSONObject params) {
		JSONObject res = new JSONObject();
		String kslcdm = UUIDUtil.getUUID();
		res.put("kslcdm", kslcdm);
		res.put("examId", params.getString("examId"));
		Long lrr = params.getLong("lrr");
		String kslcmc = params.getString("kslcmc");
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		List<String> usedGradeList = StringUtil.convertToListFromStr(params.getString("usedGradeId"), ",",
				String.class);
		
		String examId = params.getString("examId");//考网数据

		DegreeInfo degreeInfo = new DegreeInfo();
		degreeInfo.setKslcdm(kslcdm);
		degreeInfo.setXxdm(xxdm);
		degreeInfo.setKslcmc(kslcmc);
		degreeInfo.setLrr(String.valueOf(lrr));
		degreeInfo.setCdate(new Date());
		degreeInfo.setXnxq(xnxq);

		if (scoreDao.hasSameNameInDegreeInfo(xnxq, degreeInfo)) {
			if (StringUtils.isNotBlank(examId)) {
				degreeInfo.setKslcmc(kslcmc + examId); //考网数据重名加编号
			}else {
				throw new CommonRunException(-2, "已存在相同名称的考试，请输入不同的考试名称！");
			}
		
		}

		List<DegreeInfoNj> degreeInfoNjList = new ArrayList<DegreeInfoNj>(usedGradeList.size());
		for (String usedGrade : usedGradeList) {
			DegreeInfoNj degreeInfoNj = new DegreeInfoNj();
			degreeInfoNj.setKslcdm(kslcdm);
			degreeInfoNj.setXxdm(xxdm);
			degreeInfoNj.setNj(usedGrade);
			degreeInfoNj.setXnxq(xnxq);
			degreeInfoNjList.add(degreeInfoNj);
		}

		scoreDao.insertDegreeInfoNjBatch(xnxq, degreeInfoNjList);
		scoreDao.insertDegreeInfo(xnxq, degreeInfo);
		return res;
	}

	@Override
	public int updatetExamName(JSONObject params) {
		String xnxq = params.getString("xnxq");

		DegreeInfo degreeInfo = new DegreeInfo();
		degreeInfo.setKslcdm(params.getString("kslcdm"));
		degreeInfo.setXxdm(params.getString("xxdm"));
		degreeInfo.setKslcmc(params.getString("kslcmc"));
		degreeInfo.setXnxq(xnxq);

		if (scoreDao.hasSameNameInDegreeInfo(xnxq, degreeInfo)) {
			throw new CommonRunException(-2, "已存在相同名称的考试，请输入不同的考试名称！");
		}

		return scoreDao.updatetDegreeInfo(xnxq, degreeInfo);
	}

	@Override
	public void deleteExam(JSONObject params) throws RuntimeException {
		String kslcdm = params.getString("kslcdm");
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		// 先删除数据库中数据，再远程删除，这样做的好处是远程删除失败会回滚
		scoreDao.deleteDegreeInfo(xnxq, params);
		scoreDao.deleteDegreeInfoNj(xnxq, params);
		scoreDao.deleteScoreInfo(xnxq, autoIncr, params);

		if ("1".equals(degreeInfo.getFxflag()) && "1".equals(sswitch)) {
			Map<String, Object> syncParam = new HashMap<String, Object>();
			syncParam.put("teacherId", degreeInfo.getLrr());
			syncParam.put("examUuId", kslcdm);

			// 调用访问远程接口的http方法
			String responseResult = updateHttpRemoteInterface(url + "/SendExam/deleteExam", syncParam);
			JSONObject response = JSONObject.parseObject(responseResult);
			if (response == null
					|| (!"0".equals(response.getString("code")) && !"10300".equals(response.getString("code")))) {
				throw new RuntimeException("删除考试失败!");
			}
		}
	}

	@Override
	public JSONObject getScoreInfoList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		Long xxdm = params.getLong("xxdm");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}

		Integer autoIncr = degreeInfo.getAutoIncr();

		String bhs = params.getString("bhs"); // 班级
		List<Long> bhList = StringUtil.convertToListFromStr(bhs, ",", Long.class);
		params.put("bhList", bhList);
		params.remove("bhs");
		List<Long> kmdmList = StringUtil.convertToListFromStr(params.getString("kmdms"), ",", Long.class);
		params.put("kmdmList", kmdmList);
		params.remove("kmdms");
		String stdNumOrName = params.getString("stdNumOrName");
		params.remove("stdNumOrName");

		if (StringUtils.isNotBlank(stdNumOrName)) {
			// 获取学生列表
			HashMap<String, Object> studentParam = new HashMap<String, Object>();
			studentParam.put("schoolId", String.valueOf(xxdm));
			studentParam.put("termInfoId", xnxq);
			studentParam.put("keyword", stdNumOrName);
			studentParam.put("classId", bhs);
			List<Account> accounts = allCommonDataService.getStudentList(studentParam);
			List<String> accIds = new ArrayList<String>();
			for (Account acc : accounts) {
				accIds.add(String.valueOf(acc.getId()));
			}
			if (CollectionUtils.isEmpty(accIds)) {
				JSONObject data = new JSONObject();
				data.put("total", 0);
				data.put("rows", new Object[0]);
				return data;
			}
			params.put("xhList", accIds);
		}

		List<ScoreInfo> scoreInfoList = scoreDao.getScoreInfo(xnxq, autoIncr, params);
		Set<Long> accIdSet = new HashSet<Long>();
		Set<Long> subjectIdSet = new HashSet<Long>();
		for (ScoreInfo scoreInfo : scoreInfoList) {
			accIdSet.add(Long.valueOf(scoreInfo.getXh()));
			subjectIdSet.add(Long.valueOf(scoreInfo.getKmdm()));
		}

		List<LessonInfo> lessonInfoList = allCommonDataService.getLessonInfoBatch(xxdm,
				new ArrayList<Long>(subjectIdSet), xnxq);
		List<Account> accountList = allCommonDataService.getAccountBatch(xxdm, new ArrayList<Long>(accIdSet), xnxq);
		Map<Long, Account> id2Obj = new HashMap<Long, Account>();
		for (Account acc : accountList) {
			id2Obj.put(acc.getId(), acc);
		}

		// 5.组织表头
		EasyUIDatagridHead head[][] = new EasyUIDatagridHead[1][lessonInfoList.size()];
		for (int i = 0; i < lessonInfoList.size(); i++) {
			LessonInfo lessonInfo = lessonInfoList.get(i);
			if (lessonInfo != null) {
				head[0][i] = new EasyUIDatagridHead(lessonInfo.getId() + "_score", lessonInfo.getName(), "center", 60,
						1, 1, true);
			}
		}

		Map<Long, JSONObject> scoreInfoMap = new HashMap<Long, JSONObject>(); // 学生成绩记录
		for (ScoreInfo scoreInfo : scoreInfoList) {
			Long xh = Long.valueOf(scoreInfo.getXh());
			Account acc = id2Obj.get(xh);
			if (acc == null) {
				continue; 
			}
			if (!scoreInfoMap.containsKey(xh)) {
				scoreInfoMap.put(xh, new JSONObject());
			}
			JSONObject json = scoreInfoMap.get(xh);
			
			String stdNumber = "";
			for (User user : acc.getUsers()) {
				if (T_Role.Student.equals(user.getUserPart().getRole())) {
					stdNumber = user.getStudentPart().getSchoolNumber();
					break;
				}
			}
			json.put("stdNumber", stdNumber);
			json.put("name", acc.getName());
			json.put("stdId", scoreInfo.getXh());
			json.put(scoreInfo.getKmdm() + "_score", scoreInfo.getCj());
			json.put(scoreInfo.getKmdm() + "_specialCase", scoreInfo.getTsqk() == null ? 0 : scoreInfo.getTsqk());
		}

		JSONObject data = new JSONObject();
		data.put("columns", head);
		data.put("total", scoreInfoMap.size());
		data.put("rows", scoreInfoMap.values());
		return data;
	}

	@Override
	public JSONObject getAllStuByParam(JSONObject params) {
		Map<String, JSONObject> xhMap = new HashMap<String, JSONObject>(); // 学号
		Map<String, Classroom> bjMap = new HashMap<String, Classroom>(); // 班级
		Map<String, JSONObject> xjhMap = new HashMap<String, JSONObject>(); // 学籍号
		Map<String, List<JSONObject>> xmbjMap = new HashMap<String, List<JSONObject>>(); // 姓名班级
		Map<String, List<JSONObject>> xmMap = new HashMap<String, List<JSONObject>>(); // 姓名或者手机信息

		String termInfoId = params.getString("xnxq");
		String xn = params.getString("xn");
		long schoolId = params.getLongValue("xxdm");

		School sch = allCommonDataService.getSchoolById(schoolId, termInfoId);
		List<Account> stuList = allCommonDataService.getAllStudent(sch, termInfoId);

		// 获取学生
		List<JSONObject> list = new ArrayList<JSONObject>();
		Set<Long> classIdSet = new HashSet<Long>();
		for (Account acc : stuList) {
			if (acc == null || acc.getUsers() == null) {
				continue;
			}

			for (User u : acc.getUsers()) {
				if (u == null || !T_Role.Student.equals(u.getUserPart().getRole())) {
					continue;
				}

				StudentPart sp = u.getStudentPart();
				if (sp == null) {
					continue;
				}
				classIdSet.add(sp.getClassId());
				JSONObject o = new JSONObject();
				o.put("xh", acc.getId()); // accId
				o.put("userId", acc.getId()); // accId
				o.put("userXh", sp.getSchoolNumber()); // 学号
				o.put("xjh", sp.getStdNumber()); // 学籍号
				o.put("xm", acc.getName()); // 姓名
				o.put("bh", sp.getClassId()); // 班级id
				o.put("phone", acc.getMobilePhone());	// 手机号
				o.put("idcard", sp.getStdNumber());	// 身份证
				list.add(o);
				break;
			}
		}

		List<Classroom> classroomList = allCommonDataService.getSimpleClassBatch(schoolId,
				new ArrayList<Long>(classIdSet), termInfoId);

		Map<Long, Classroom> classId2Obj = new HashMap<Long, Classroom>();
		Set<Long> gradeIdSet = new HashSet<Long>();
		for (Classroom classroom : classroomList) {
			classId2Obj.put(classroom.getId(), classroom);
			gradeIdSet.add(classroom.getGradeId());
			bjMap.put(classroom.getClassName(), classroom);
		}

		// 批量获取年级
		List<Grade> gList = allCommonDataService.getGradeBatch(schoolId, new ArrayList<Long>(gradeIdSet), termInfoId);
		Map<Long, String> gradeMap = new HashMap<Long, String>();
		for (Grade g : gList) {
			gradeMap.put(g.getId(), allCommonDataService.ConvertNJDM2SYNJ((g.getCurrentLevel().getValue()) + "", xn));
		}

		for (JSONObject stu : list) {
			Classroom classroom = classId2Obj.get(stu.getLong("bh"));
			if (classroom == null) {
				continue;
			}
			stu.put("bjmc", classroom.getClassName());
			stu.put("nj", gradeMap.get(classroom.getGradeId()));

			if (StringUtils.isNotBlank(stu.getString("userXh"))) { // 学号
				xhMap.put(stu.getString("userXh"), stu);
			}

			if (StringUtils.isNotBlank(stu.getString("userId"))) { // accId
				xjhMap.put(stu.getString("userId"), stu);
			}

			if (StringUtils.isNotBlank(stu.getString("xm")) && StringUtils.isNotBlank(stu.getString("bjmc"))) {
				 // 姓名 + 班级名称或者手机班级
				String key = stu.getString("xm") + stu.getString("bjmc");
				if (!xmbjMap.containsKey(key)) {
					xmbjMap.put(key, new ArrayList<JSONObject>());
				}
				xmbjMap.get(key).add(stu);
				
				key = stu.getString("phone") + stu.getString("bjmc");
				if(!xmbjMap.containsKey(key)) {
					xmbjMap.put(key, new ArrayList<JSONObject>());
				}
				xmbjMap.get(key).add(stu);
			}

			if (StringUtils.isNotBlank(stu.getString("xm"))) {
				String xm = stu.getString("xm");
				if (!xmMap.containsKey(xm)) {
					xmMap.put(xm, new ArrayList<JSONObject>());
				}
				xmMap.get(xm).add(stu);
				
				String sjh = stu.getString("phone");	// 手机号
				if(StringUtils.isNotEmpty(sjh)) {
					if(!xmMap.containsKey(sjh)) {
						xmMap.put(sjh, new ArrayList<JSONObject>());
					}
					xmMap.get(sjh).add(stu);
				}
				
				String sfz = stu.getString("idcard");	// 身份证
				if(StringUtils.isNotEmpty(sfz)) {
					if(!xmMap.containsKey(sfz)) {
						xmMap.put(sfz, new ArrayList<JSONObject>());
					}
					xmMap.get(sfz).add(stu);
				}
			}
		}

		JSONObject obj = new JSONObject();
		obj.put("xhMap", xhMap);
		obj.put("xjhMap", xjhMap);
		obj.put("xmbjMap", xmbjMap);
		obj.put("xmMap", xmMap);
		obj.put("bjMap", bjMap);

		return obj;
	}

	@Override
	public void insertScoreInfoBatch(List<ScoreInfo> scoreList, StartImportTaskParam taskParam) {
		String xnxq = taskParam.getXn() + taskParam.getXq();
		String xxdm = taskParam.getXxdm();
		String kslc = taskParam.getKslc();
		JSONObject params = new JSONObject();
		params.put("xxdm", xxdm);
		params.put("kslcdm", kslc);
		params.put("kslc", kslc);
		params.put("xnxq", xnxq);
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1001, "成绩信息已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		if (scoreList.size() > 0) {
			scoreDao.deleteScoreInfo(xnxq, autoIncr, params);// 删除老数据
			scoreDao.insertScoreInfoBatch(xnxq, autoIncr, scoreList);
		}
		
		List<String> njList = scoreDao.getNjByScoreInfo(xnxq, autoIncr, params);
		if (njList.size() > 0) {
			params.put("excludeNjList", njList);
			scoreDao.deleteDegreeInfoNj(xnxq, params);
			params.remove("excludeNjList");
		}

		Set<String> njSet = new HashSet<String>();
		for (ScoreInfo scoreInfo : scoreList) {
			String nj = scoreInfo.getNj();
			if (!njSet.contains(nj)) {
				njSet.add(nj);
			}
		}

		if (njSet.size() > 0) {
			List<DegreeInfoNj> degreeInfoNjList = new ArrayList<DegreeInfoNj>();
			for(String nj : njSet) {
				DegreeInfoNj degreeInfoNj = new DegreeInfoNj();
				degreeInfoNj.setKslcdm(kslc);
				degreeInfoNj.setMrszflag("0");
				degreeInfoNj.setNj(nj);
				degreeInfoNj.setXnxq(xnxq);
				degreeInfoNj.setXxdm(xxdm);
				degreeInfoNjList.add(degreeInfoNj);
			}
			scoreDao.insertDegreeInfoNjBatch(xnxq, degreeInfoNjList);
		}

		degreeInfo.setDrflag("1");
		scoreDao.updatetDegreeInfo(xnxq, degreeInfo);
	}

	@Override
	public void updateScore(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1001, "成绩信息已被删除，请刷新页面！");
		}

		Integer autoIncr = degreeInfo.getAutoIncr();
		String kslc = params.getString("kslc");
		String xxdm = params.getString("xxdm");

		JSONArray scores = params.getJSONArray("scores");
		for (int i = 0, len = scores.size(); i < len; i++) {
			JSONObject score = scores.getJSONObject(i);

			JSONObject param = new JSONObject();
			param.put("xxdm", xxdm);
			param.put("kslc", kslc);
			param.put("xnxq", xnxq);
			param.put("xh", score.get("stdId"));

			for (Map.Entry<String, Object> entry : score.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().toString();
				if (key != null && key.endsWith("_score")) {
					String subjectId = key.replaceAll("_score", "");
					param.put("kmdm", subjectId);
					if ("".equals(value)) {
						param.put("cj", -1);
					} else if(NumberUtils.isNumber(value)){
						param.put("cj", value);
					} else {
						throw new CommonRunException(-1, "修改的成绩值不为数值类型，请检查相关输入！");
					}
					param.put("tsqk", param.get(subjectId + "_specialCase"));
					scoreDao.updateScoreInfo(xnxq, autoIncr, param);
				}
			}
		}
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
	public JSONObject updateInitAndGetExamAnalysisSettingConfig(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1001, "成绩信息已被删除，请刷新页面！");
		}

		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);
		String xn = xnxq.substring(0, xnxq.length() - 1);

		JSONObject data = new JSONObject();

		List<String> pycc = new ArrayList<String>(); // 培养层次代码
		// 从t_gm_degreeinfo_nj表中获取考试所属年级
		List<String> userGradeList = scoreDao.getNjFromDegreeInfoNj(xnxq, params);
		if (CollectionUtils.isNotEmpty(userGradeList)) {
			for (String usedGrade : userGradeList) {
				String gradeCode = allCommonDataService.ConvertSYNJ2NJDM(usedGrade, xn); // 年级代码
				int educationLevel = allCommonDataService.getPYCCByNJCode(Long.valueOf(gradeCode));// 培养层次
				pycc.add(String.valueOf(educationLevel));
			}
		}
		Set<String> setItemSet = new HashSet<String>(); // 设置项集合
		if (CollectionUtils.isNotEmpty(pycc)) {
			// 从t_gm_scoreanalysisconfig中通过pycc获取setdm，setdm对应分析中的选项代码，即
			// 通过培养层次，从数据库取出各个培养层次的设置项
			params.put("pyccList", pycc);
			setItemSet.addAll(scoreDao.getExamAnalysisConfig(params));// 从数据库取出设置项
			params.remove("pyccList");
		}

		// 对分析代码配置进行默认设置判断，并进行默认设置
		updateExamDefaultSet(params, degreeInfo, setItemSet);

		StringBuffer setItemId = new StringBuffer();
		if (CollectionUtils.isNotEmpty(setItemSet)) {
			for (String item : setItemSet) {
				setItemId.append(item).append(",");
			}
			if (setItemId.length() > 0) {
				setItemId.deleteCharAt(setItemId.length() - 1);
			}
		}
		data.put("setedItemId", setItemId.toString());

		// 获取设置状态
		List<JSONObject> list = scoreDao.getExamDefaultSetting(params, autoIncr, setItemSet);
		// 修改
		params.remove("nj");
		params.put("lb", "01");

		List<TopGroup> allgroup = scoreDao.getTopGroup(xnxq, autoIncr, params);
		Set<String> lxs = new HashSet<String>();
		if (CollectionUtils.isNotEmpty(allgroup)) {
			List<String> ssfzList = new ArrayList<String>(); // 所属分组
			for (TopGroup group : allgroup) {
				ssfzList.add(group.getBmfz()); // 分组代码
				lxs.add(group.getBjlxm()); // 班级类型代码
			}

			if (setItemSet.contains("02")) { // 班级分组
				boolean isset = true;
				params.put("ssfzList", ssfzList);
				Set<String> ssfzSet = new HashSet<String>(scoreDao.getSsfzFromTopGroup(xnxq, autoIncr, params));

				for (String ss : ssfzList) {
					if (!ssfzSet.contains(ss)) {
						isset = false;
						break;
					}
				}

				for (JSONObject sefo : list) {
					if (sefo.getString("setItemId").equals("02")) {
						sefo.put("isSeted", isset);
						break;
					}

				}
			}

			if (setItemSet.contains("05")) {
				boolean isset = true;
				for (TopGroup group : allgroup) {
					params.put("bmfz", group.getBmfz());
					int num = 0;
					List<JSONObject> stlist = scoreDao.getSubjectStatusList(xnxq, autoIncr, params);
					if (stlist != null && !stlist.isEmpty()) {
						for (JSONObject sj : stlist) {
							if (sj.getInteger("inStaticSubject") == 1) {
								num++;
							}
						}
						if (num == 0) {
							isset = false;
							break;
						}
					}
				}
				for (JSONObject sefo : list) {
					if (sefo.getString("setItemId").equals("05")) {
						sefo.put("isSeted", isset);
						break;
					}
				}
			}

			if (setItemSet.contains("08")) {// 排名区间
				boolean isset = true;
				params.put("ssfzList", ssfzList);
				Set<String> ssfzSet = new HashSet<String>(
						scoreDao.getFzdmFromScoreRankDistribute(xnxq, autoIncr, params));

				for (String ss : ssfzList) {
					if (!ssfzSet.contains(ss)) {
						isset = false;
						break;
					}
				}

				for (JSONObject sefo : list) {
					if (sefo.getString("setItemId").equals("08")) {
						sefo.put("isSeted", isset);
						break;
					}
				}
			}

			// if (setItemSet.contains("09")) { // 分数段
			// List<TopGroup> needlist = new ArrayList<TopGroup>();
			// params.put("ssfzList", ssfzList);
			// // 总分区间设置表 中获取fzdm
			// Set<String> ssfzSet = new
			// HashSet<String>(scoreDao.getFzdmFromZfqjsz(xnxq, autoIncr,
			// params));
			// for (TopGroup group : allgroup) {
			// if (ssfzSet.contains(group.getBmfz())) {
			// needlist.add(group);
			// }
			// }
			//
			// Map<String, List<TopGroup>> allgroupmap = new HashMap<String,
			// List<TopGroup>>();
			// for (TopGroup group : needlist) {
			// String nj = group.getNj();
			// if (!allgroupmap.containsKey(nj)) {
			// allgroupmap.put(nj, new ArrayList<TopGroup>());
			// }
			// allgroupmap.get(nj).add(group);
			// }
			//
			// for (Map.Entry<String, List<TopGroup>> entry :
			// allgroupmap.entrySet()) {
			// params.put("nj", entry.getKey());
			// params.put("lb", "01");
			//
			// DegreeInfo prevDegreeInfo =
			// scoreDao.getOldKslcAndWlfz(degreeInfo.getCdate(), params, lxs);//
			// 获取以往考试极其分组
			//
			// Map<String, String> lx2BmfzMap = new HashMap<String, String>();
			// for (TopGroup group : entry.getValue()) {
			// lx2BmfzMap.put(group.getBjlxm(), group.getBmfz());
			// }
			//
			// setScoreDistribute(params, lx2BmfzMap, prevDegreeInfo);
			// }
			// }
		}

		data.put("defaultSet", list);
		return data;
	}

	private int updateExamDefaultSet(JSONObject params, DegreeInfo degreeInfo, Set<String> setItemSet) {
		// TODO Auto-generated method stub
		Integer autoIncr = degreeInfo.getAutoIncr();
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		String kslc = params.getString("kslc");

		HashMap<String, Object> hashmap = new HashMap<String, Object>();
		hashmap.put("termInfoId", xnxq);
		hashmap.put("schoolId", xxdm);

		Set<String> isSetted = new HashSet<String>();// 如果已经默认设置，检查下面四项设置是否有对应的设置，没有则需进行设置
		// 4个必须设置的选项，可以在前台页面中看到，是否有数据
		// 查询lb=01（文理分组）的t_gm_topgroup表记录数
		if (scoreDao.ifExistsAnalysisConfig01(xnxq, autoIncr, params)) {
			isSetted.add("01");
		}
		// 查询lb=02（班级分组）的t_gm_topgroup表记录数
		if (scoreDao.ifExistsAnalysisConfig02(xnxq, autoIncr, params)) {
			isSetted.add("02");
		}
		// 统计科目与满分，查询t_gm_scoretjmk（成绩统计模块）记录数
		if (scoreDao.ifExistsAnalysisConfig05(xnxq, autoIncr, params)) {
			isSetted.add("05");
		}
		// 统计t_gm_zfqjsz（总分成绩分布）表记录数
		if (scoreDao.ifExistsAnalysisConfig09(xnxq, autoIncr, params)) {
			isSetted.add("09");
		}

		// t_gm_degreeinfo_nj获取所有的数据
		List<DegreeInfoNj> njList = scoreDao.getDegreeInfoNjList(xnxq, params);
		for (DegreeInfoNj degreeInfoNj : njList) {
			// mrszflag不等于1，即此年级的分析数据没有被设置；isSetted.size()不等于4，表示必设置选项的数据不完整
			if ("1".equals(degreeInfoNj.getMrszflag())) {
				continue;
			}
			if(isSetted.size() == 4) {
				continue;
			}
			
			String synj = degreeInfoNj.getNj(); // 获取使用年级

			params.put("nj", synj);
			// 删除所有成绩设置，t_gm_topgroupbj(班级分组设置表),t_gm_topgroup(年级分组表),t_gm_scoretjmk(成绩统计模块),
			// t_gm_scorerankdistribute(年级排名分布设置),t_gm_zfqjsz(总分成绩分布),
			// t_gm_scoremf(考试满分),t_gm_synthscore(综合成绩设置表),t_gm_scoreleveltemplate(成绩等级模板表),
			// t_gm_scorestustatisticsrank_mk(学生单科统计结果)
			scoreDao.deleteAllExamAnalysisConfig(xnxq, autoIncr, params);

			hashmap.put("usedGradeId", synj);
			List<Account> accList = allCommonDataService.getStudentList(hashmap); // 取得该年级学生
			if (CollectionUtils.isEmpty(accList)) {
				throw new CommonRunException(-1, "无法从SDK中获取的学生数据，请联系管理员！");
			}

			Set<String> studIdSet = new HashSet<String>();
			for (Account acc : accList) {
				studIdSet.add(String.valueOf(acc.getId()));
			}

			params.put("studentIds", studIdSet);
			// 删除所有此年级不参与统计学生
			scoreDao.delXSCJBTJ(xnxq, autoIncr, params);

			// 统计参数-优秀合格率默认设置
			if (setItemSet.contains("07")) {
				// t_gm_jxpgscoresection，优秀率合格率参数
				if (!scoreDao.ifExistsJXPGScoreSection(params)) {
					List<JXPGScoreSection> jxpgScoreSectionList = new ArrayList<JXPGScoreSection>();
					JXPGScoreSection tsc = new JXPGScoreSection();
					tsc.setDm("01");
					tsc.setFlag("1");
					tsc.setFs("1"); // 设置方式
					tsc.setFzbfb(80f); // 总分/人数百分比
					tsc.setMc("优秀率"); // 名称
					tsc.setXxdm(xxdm);
					jxpgScoreSectionList.add(tsc);
					tsc = new JXPGScoreSection();
					tsc.setDm("02");
					tsc.setFlag("1");
					tsc.setFs("1");
					tsc.setFzbfb(60f);
					tsc.setMc("合格率");
					tsc.setXxdm(xxdm);
					jxpgScoreSectionList.add(tsc);
					tsc = new JXPGScoreSection();
					tsc.setDm("03");
					tsc.setFlag("1");
					tsc.setFs("1");
					tsc.setFzbfb(40f);
					tsc.setMc("低分率");
					tsc.setXxdm(xxdm);
					jxpgScoreSectionList.add(tsc);
					tsc = new JXPGScoreSection();
					tsc.setDm("04");
					tsc.setFlag("1");
					tsc.setFs("1");
					tsc.setFzbfb(90f);
					tsc.setMc("尖子生");
					tsc.setXxdm(xxdm);
					jxpgScoreSectionList.add(tsc);
					tsc = new JXPGScoreSection();
					tsc.setDm("05");
					tsc.setFlag("1");
					tsc.setFs("1");
					tsc.setFzbfb(50f);
					tsc.setMc("潜能生");
					tsc.setXxdm(xxdm);
					jxpgScoreSectionList.add(tsc);
					scoreDao.insertJXPGScoreSectionBatch(jxpgScoreSectionList);
					scoreDao.initScoreStatTerm(xxdm);
				}
			}

			if (setItemSet.contains("13")) {
				// t_sar_setting_bj（班级告报参数表）
				if (!scoreDao.ifExistsSettingBj(xxdm)) {
					List<SettingBJ> tbjInsert = new ArrayList<SettingBJ>();
					SettingBJ tbj = new SettingBJ();
					tbj.setXxdm(xxdm);
					tbj.setDm("01"); // 代码
					tbj.setSzz(3); // 设置值
					tbjInsert.add(tbj);
					tbj = new SettingBJ();
					tbj.setXxdm(xxdm);
					tbj.setDm("02");
					tbj.setSzz(3);
					tbjInsert.add(tbj);
					scoreDao.insertClassReportParamBatch(tbjInsert);
				}
			}

			if (setItemSet.contains("14")) {
				// 设置学生报告参数 t_sar_setting_stu（学生告报参数表）
				if (!scoreDao.ifExistsSettingStu(xxdm)) {
					List<SettingStu> tStuInsert = new ArrayList<SettingStu>();
					SettingStu tbj = new SettingStu();
					tbj.setXxdm(xxdm);
					tbj.setDm("01");
					tbj.setTsydm("01"); // 提示语代码
					tbj.setSzz(5); // 设置值
					tStuInsert.add(tbj);
					tbj = new SettingStu();
					tbj.setXxdm(xxdm);
					tbj.setDm("01");
					tbj.setTsydm("02");
					tbj.setSzz(3);
					tStuInsert.add(tbj);
					tbj = new SettingStu();
					tbj.setXxdm(xxdm);
					tbj.setDm("01");
					tbj.setTsydm("03");
					tbj.setSzz(1);
					tStuInsert.add(tbj);
					tbj = new SettingStu();
					tbj.setXxdm(xxdm);
					tbj.setDm("02");
					tbj.setTsydm("01");
					tbj.setSzz(1);
					tStuInsert.add(tbj);
					tbj = new SettingStu();
					tbj.setXxdm(xxdm);
					tbj.setDm("02");
					tbj.setTsydm("02");
					tbj.setSzz(3);
					tStuInsert.add(tbj);
					tbj = new SettingStu();
					tbj.setXxdm(xxdm);
					tbj.setDm("02");
					tbj.setTsydm("03");
					tbj.setSzz(5);
					tStuInsert.add(tbj);
					tbj = new SettingStu();
					tbj.setXxdm(xxdm);
					tbj.setDm("03");
					tbj.setTsydm("01");
					tbj.setSzz(5);
					tStuInsert.add(tbj);
					tbj = new SettingStu();
					tbj.setXxdm(xxdm);
					tbj.setDm("03");
					tbj.setTsydm("02");
					tbj.setSzz(5);
					tStuInsert.add(tbj);
					scoreDao.insertStudentReportParamBatch(tStuInsert);
				}
			}

			// 获取该年级所有对比考试轮次
			// 前提：假如当前考试包含了1/2/3/4/5个班级的考试数据
			// 获取对比考试数据：首先找出包含此这几个班级的考试轮次（不需要全部包含），但要覆盖所有的班级，例如考试1包含1/3班，考试2包含1/2/4班，考试3包含5班，
			// 那么对比考试数据则是考试1/2/3。
			if (setItemSet.contains("10")) {
				// 此次成绩的t_gm_scoreinfo（成绩表）班级代码
				List<Long> classIdList = scoreDao.getExamClassIdList(xnxq, autoIncr, params); // 班级

				// t_gm_scoreclassstatistics（班级总分统计），此次成绩之前的成绩统计值
				List<Dbkslc> dbKslcList = scoreDao.getPrevKslcList(degreeInfo.getCdate(), params,
						new HashSet<Long>(classIdList));

				// 保存对比考试轮次
				if (dbKslcList.size() > 0) { // 先查询 后插入
					scoreDao.insertDbkslcBatch(xnxq, autoIncr, dbKslcList);
				}
			}

			Map<String, JSONArray> lx2ArrayMap = new HashMap<String, JSONArray>(); // 班级类型映射TopGroupBj对象列表

			// 先默认分组 按班级类型分组
			List<TDmBjlx> TDmBjlxList = allCommonDataService.getClassTypeList();
			Map<String, String> classType2NameMap = new HashMap<String, String>(); // 类型代码映射类型名称
			for (TDmBjlx lx : TDmBjlxList) {
				classType2NameMap.put(lx.getDm(), lx.getMc());
			}

			// T_GradeLevel gl = T_GradeLevel
			// .findByValue(Integer.valueOf(allCommonDataService.ConvertSYNJ2NJDM(synj,
			// xn)));

			// boolean needWlfzGrade = false; // true表示年级为高中，需要文理分组
			// if (gl.equals(T_GradeLevel.T_HighOne) ||
			// gl.equals(T_GradeLevel.T_HighThree)
			// || gl.equals(T_GradeLevel.T_HighTwo)) {
			// needWlfzGrade = true;
			// }

			hashmap.put("schoolId", Long.valueOf(xxdm));
			List<Classroom> classList = allCommonDataService.getClassList(hashmap);// 从深圳调接口得到该年级的所有班级
			Map<String, String> classId2TypeMap = new HashMap<String, String>();// 存放班级，班级对应的类型代码

			List<TopGroupBj> topGroupBjList = new ArrayList<TopGroupBj>(); // 分组列表
			for (Classroom c : classList) {
				if (c == null) {
					continue;
				}
				String classId = String.valueOf(c.getId());

				String classType = "0";
				if (c.getClassType() != null /* && needWlfzGrade */) {
					classType = String.valueOf(c.getClassType().getValue());// 班级类型码
				}
				classId2TypeMap.put(classId, classType);

				TopGroupBj groupBj = new TopGroupBj();
				groupBj.setKslc(kslc);
				groupBj.setXxdm(xxdm);
				groupBj.setBh(String.valueOf(c.getId()));
				groupBj.setXnxq(xnxq);
				groupBj.getTopGroup().setBjlxm(classType);
				topGroupBjList.add(groupBj);

				if (!lx2ArrayMap.containsKey(classType)) {
					lx2ArrayMap.put(classType, new JSONArray());
				}
				lx2ArrayMap.get(classType).add(groupBj);
			}

			Set<String> classTypeDmSet = lx2ArrayMap.keySet(); // 类型代码

			params.put("lb", "01");

			Map<String, String> lx2BmfzMap = new HashMap<String, String>(); // 类型代码映射分组代码
			List<TopGroup> topGroupList = new ArrayList<TopGroup>();
			// 设置年级分组-文理
			for (String key : classTypeDmSet) {
				String bmfz = UUIDUtil.getUUID();

				TopGroup tgm = new TopGroup();
				tgm.setBjlxm(key.toString());
				tgm.setBmfz(bmfz);
				tgm.setFzmc(classType2NameMap.get(key));
				tgm.setKslc(kslc);
				tgm.setLb("01");
				tgm.setNj(synj);
				tgm.setXnxq(xnxq);
				tgm.setXxdm(xxdm);

				topGroupList.add(tgm);
				lx2BmfzMap.put(key.toString(), bmfz);
			}

			for (TopGroupBj groupBj : topGroupBjList) {
				String bjlxm = (String) groupBj.getTopGroup().getBjlxm();
				groupBj.setBmfz(lx2BmfzMap.get(bjlxm));
			}

			// 年级分组表
			if (topGroupList.size() > 0) {
				scoreDao.insertTopGroupBatch(xnxq, autoIncr, topGroupList);
			}
			// 年级分组设置表
			if (topGroupBjList.size() > 0) {
				scoreDao.insertTopGroupBjBatch(xnxq, autoIncr, topGroupBjList);
			}

			DegreeInfo prevDegreeInfo = null;
			if (!params.containsKey("tlNull")) {
				prevDegreeInfo = scoreDao.getOldKslcAndWlfz(degreeInfo.getCdate(), params, classTypeDmSet); // 获取以往考试及其分组
			}

			params.put("autoIncr", autoIncr);
			// 设置统计科目和满分
			setTjkmAndMf(params, lx2BmfzMap, classId2TypeMap, topGroupBjList);
			// 班级设置
			setBjsz(prevDegreeInfo, topGroupList, classId2TypeMap.keySet(), topGroupBjList, params, lx2BmfzMap);
			// 设置分数段
			setScoreDistribute(params, lx2BmfzMap, prevDegreeInfo);
			params.remove("autoIncr");

			if (setItemSet.contains("08")) {
				// 开始排名区间设置
				List<ScoreRankDistribute> tgsRankList = new ArrayList<ScoreRankDistribute>();
				if (prevDegreeInfo != null) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("kslc", prevDegreeInfo.getKslcdm());
					map.put("xnxq", prevDegreeInfo.getXnxq());
					map.put("xxdm", xxdm);
					map.put("nj", synj);
					List<ScoreRankDistribute> scoreRankDistributeList = scoreDao
							.getRankListByPrevKslc(prevDegreeInfo.getXnxq(), prevDegreeInfo.getAutoIncr(), map);
                    
					Iterator<ScoreRankDistribute> iterator = scoreRankDistributeList.iterator();
					while (iterator.hasNext()) {
						ScoreRankDistribute rank = iterator.next();
						if (lx2BmfzMap.get(String.valueOf(rank.getOtherData()))!=null) {
							rank.setKslc(kslc);
							rank.setFzdm(lx2BmfzMap.get(String.valueOf(rank.getOtherData())));
							rank.setXnxq(xnxq);
							tgsRankList.add(rank);
						}else {
							iterator.remove();
						}
						
					}
					/*for (ScoreRankDistribute rank : scoreRankDistributeList) {
						rank.setKslc(kslc);
						rank.setFzdm(lx2BmfzMap.get(String.valueOf(rank.getOtherData())));
						rank.setXnxq(xnxq);
						tgsRankList.add(rank);
					}*/

				} else {
					// 文理类型 -映射文理分组代码
					for (String key : lx2BmfzMap.keySet()) {
						for (int j = 0; j < 3; j++) {
							ScoreRankDistribute trk = new ScoreRankDistribute();
							trk.setFzdm(lx2BmfzMap.get(key));
							trk.setKslc(kslc);
							trk.setNj(synj);
							trk.setPmfbdm("0" + (j + 1));
							trk.setXxdm(xxdm);
							trk.setXnxq(xnxq);
							if (j == 0) {
								trk.setPmfbsx(50);
								trk.setPmfbxx(1);
							} else if (j == 1) {
								trk.setPmfbsx(100);
								trk.setPmfbxx(50);
							} else if (j == 2) {
								trk.setPmfbsx(200);
								trk.setPmfbxx(100);
							} else if (j == 3) {
								trk.setPmfbsx(500);
								trk.setPmfbxx(200);
							}
							tgsRankList.add(trk);
						}
					}
				}
				// 保存排名区间设置
				if (tgsRankList.size() > 0) {
					scoreDao.insertScoreRankDistributeBatch(xnxq, autoIncr, tgsRankList);
				}
			}

			if (prevDegreeInfo != null) {
				// 竞赛
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("kslc", prevDegreeInfo.getKslcdm());
				map.put("xxdm", xxdm);
				map.put("xnxq", prevDegreeInfo.getXnxq());
				map.put("nj", synj);
				List<CompetitionStu> compStuListPrivKslc = scoreDao.getCompetitionStu(prevDegreeInfo.getXnxq(),
						prevDegreeInfo.getAutoIncr(), map);

				List<CompetitionStu> compStuList = new ArrayList<CompetitionStu>();
				for (CompetitionStu stu : compStuListPrivKslc) {
					if (studIdSet.contains(stu.getXh())) {
						stu.setKslc(kslc);
						stu.setXnxq(xnxq);
						compStuList.add(stu);
					}
				}
				compStuListPrivKslc = null;

				scoreDao.deleteCompetitionStud(xnxq, autoIncr, params);
				if (compStuList.size() > 0) {
					scoreDao.insertCompetitionStuBatch(xnxq, autoIncr, compStuList);
				}

				// 获取合并科目列表
				List<SynthScore> synthScoreList = scoreDao.getSynthScoreByPrevKslc(prevDegreeInfo.getXnxq(),
						prevDegreeInfo.getAutoIncr(), map);
				
				Iterator<SynthScore> iterator = synthScoreList.iterator();
				while (iterator.hasNext()) {
					SynthScore synthScore = iterator.next();
					if (lx2BmfzMap.get(String.valueOf(synthScore.getOtherData()))!=null) {
						synthScore.setKslc(kslc);
						synthScore.setFzdm(lx2BmfzMap.get(String.valueOf(synthScore.getOtherData())));
						synthScore.setXnxq(xnxq);
					}else {
						iterator.remove();
					}
					
				}
				
				/*for (SynthScore synthScore : synthScoreList) {
					synthScore.setKslc(kslc);
					synthScore.setFzdm(lx2BmfzMap.get(String.valueOf(synthScore.getOtherData())));
					synthScore.setXnxq(xnxq);
				}*/
				if (setItemSet.contains("06")) {
					if (synthScoreList.size() > 0) {
						scoreDao.insertSynthScoreBatch(xnxq, autoIncr, synthScoreList);
					}
				}

				if (setItemSet.contains("11")) {
					// 设置等第值
					map.put("kmdm", "");
					List<ScoreLevelTemplate> scoreLevelTemplateList = scoreDao
							.getScoreLevelTemplate(prevDegreeInfo.getXnxq(), prevDegreeInfo.getAutoIncr(), map);
					map.remove("kmdm");

					// 当前的成绩数据
					List<JSONObject> cjList = scoreDao.getCjListForDdSet(xnxq, autoIncr, params);

					for (ScoreLevelTemplate template : scoreLevelTemplateList) {
						template.setKslc(kslc);
						template.setXnxq(xnxq);
					}

					// 统计计算最终得到插入的数据list
					List<ScoreLevelTemplate> sltpList2 = ScoreUtil.getDJInfo(params, scoreLevelTemplateList,
							synthScoreList, cjList);
					sltpList2.addAll(scoreLevelTemplateList);
					if (sltpList2.size() > 0) {
						scoreDao.insertScoreLevelTemplateBatch(xnxq, autoIncr, sltpList2);
					}
				}
			}

			params.put("mrszflag", "1");
			scoreDao.updateDegreeInfoNj(xnxq, params);
		}

		return 1;
	}

	/**
	 * 班级设置
	 * 
	 * @param nextLc
	 *            下一轮次代码
	 * @param needInsertGroup
	 * @param wlkBj
	 *            分组类型 ：代码类型--代码值
	 * @param map
	 * @param lx2BmfzMap
	 * @return
	 */
	private void setBjsz(DegreeInfo prevDegreeInfo, List<TopGroup> topGroupList, final Set<String> classIdSet,
			List<TopGroupBj> bjList, JSONObject params, Map<String, String> lx2BmfzMap) {
		// 开始班级分组
		// 旧版 映射新版
		String kslc = params.getString("kslc");
		String xnxq = params.getString("xnxq");
		String xxdm = params.getString("xxdm");
		String nj = params.getString("nj");
		Integer autoIncr = params.getInteger("autoIncr");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nj", nj);
		map.put("xxdm", xxdm);

		List<JSONObject> needGroup = null;
		List<TopGroupBj> wlkBj = null;

		if (prevDegreeInfo != null) {
			// 查询上次并进行设置
			map.put("lb", "02");
			map.put("xnxq", prevDegreeInfo.getXnxq());
			map.put("kslc", prevDegreeInfo.getKslcdm());
			needGroup = scoreDao.getTopGroupByPrevKslc(prevDegreeInfo.getXnxq(), prevDegreeInfo.getAutoIncr(), map);// 查询上一次考试的班级分组

			wlkBj = scoreDao.getTopGroupBj(prevDegreeInfo.getXnxq(), prevDegreeInfo.getAutoIncr(), map);
		} else {
			needGroup = new ArrayList<JSONObject>();
			wlkBj = new ArrayList<TopGroupBj>();
		}

		boolean bjlistex = true;
		if (wlkBj.isEmpty() || wlkBj.size() == 0) {
			wlkBj = bjList;
			bjlistex = false;
		}

		Map<String, String> lxBmMap2 = new HashMap<String, String>();
		if (prevDegreeInfo != null && needGroup.size() > 0 && bjlistex) {
			topGroupList.clear();
			for (JSONObject obj : needGroup) {
				String bmfz = UUIDUtil.getUUID();
				String oldBmfz = obj.getString("bmfz");
				String oldLx = obj.getString("wlfzlx");

				TopGroup g = new TopGroup();
				g.setXnxq(xnxq);
				g.setLb("02");
				g.setXxdm(xxdm);
				g.setKslc(kslc);
				g.setNj(nj);
				g.setFzmc(obj.getString("fzmc"));
				g.setBmfz(bmfz);
				g.setSsfz(lx2BmfzMap.get(oldLx));
				topGroupList.add(g);

				lxBmMap2.put(oldBmfz, bmfz);
			}
		} else {
			for (int j = 0; j < topGroupList.size(); j++) {
				TopGroup tgm = topGroupList.get(j);
				String bmfz = UUIDUtil.getUUID();
				lxBmMap2.put(tgm.getBjlxm(), bmfz);
				tgm.setBmfz(bmfz);
				// 设置班级分组
				tgm.setSsfz(lx2BmfzMap.get(tgm.getBjlxm()));
				tgm.setLb("02");
				tgm.setKslc(kslc);
				tgm.setXnxq(xnxq);
			}
		}

		List<TopGroupBj> newBJ = new ArrayList<TopGroupBj>();
		for (TopGroupBj bj : wlkBj) {
			String bjlxm = (String) bj.getTopGroup().getBjlxm();
			String bmfz = bj.getBmfz();
			if (prevDegreeInfo != null && bjlistex) {
				bmfz = lxBmMap2.get(bmfz) == null ? "" : lxBmMap2.get(bmfz);
			} else {
				bmfz = lxBmMap2.get(bjlxm) == null ? "" : lxBmMap2.get(bjlxm);
			}
			bj.setBmfz(bmfz);
			bj.setKslc(kslc);
			bj.setXnxq(xnxq);

			if (classIdSet.contains(bj.getBh())) {// 过滤班级不存在的数据
				newBJ.add(bj);
			}
		}

		// 保存班级分组设置
		if (topGroupList.size() > 0) {
			scoreDao.insertTopGroupBatch(xnxq, autoIncr, topGroupList);
		}
		if (newBJ.size() > 0) {
			scoreDao.insertTopGroupBjBatch(xnxq, autoIncr, newBJ);
		}
	}

	/**
	 * 设置统计科目和满分05
	 * 
	 * @param map
	 * @param lx2BmfzMap
	 *            分组类型 ：代码类型--代码值
	 * @param kslc
	 * @param synj
	 * @param xxdm
	 */
	private void setTjkmAndMf(JSONObject params, Map<String, String> lx2BmfzMap, Map<String, String> classId2TypeMap,
			List<TopGroupBj> groupBjList) {
		// 开始设置科目统计
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		Integer autoIncr = params.getInteger("autoIncr");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("kslc", kslc);
		map.put("nj", nj);
		map.put("xxdm", xxdm);
		map.put("xnxq", xnxq);

		Map<String, List<String>> bmfz2BhList = new HashMap<String, List<String>>();
		for (TopGroupBj groupBj : groupBjList) {
			String bmfz = groupBj.getBmfz();
			String bh = groupBj.getBh();
			if (!bmfz2BhList.containsKey(bmfz)) {
				bmfz2BhList.put(bmfz, new ArrayList<String>());
			}
			bmfz2BhList.get(bmfz).add(bh);
		}

		List<ScoreTjmk> jmkList = new ArrayList<ScoreTjmk>();
		List<ScoreMf> mfsz = new ArrayList<ScoreMf>();

		for (Map.Entry<String, List<String>> entry : bmfz2BhList.entrySet()) {

			String key = entry.getKey();
			map.put("bhlist", entry.getValue());

			List<JSONObject> kmfz = scoreDao.getKmfzList(xnxq, autoIncr, map);
			for (JSONObject o : kmfz) {
				ScoreTjmk jmk = new ScoreTjmk();
				jmk.setFzdm(key);// 从深圳接口班级获取班级类型
				jmk.setKmdm(o.getString("kmdm"));
				jmk.setKslc(kslc);
				jmk.setNj(nj);
				jmk.setXxdm(xxdm);
				jmk.setXnxq(xnxq);
				jmkList.add(jmk);

				ScoreMf cmf = new ScoreMf();
				cmf.setFzdm(key);// 从深圳接口班级获取班级类型
				cmf.setKmdm(o.getString("kmdm"));
				cmf.setKslc(kslc);
				cmf.setNj(nj);
				cmf.setXxdm(xxdm);
				cmf.setXnxq(xnxq);
				float mcj = o.getFloatValue("mcj");
				int mf = getMfByMaxCj(mcj);
				cmf.setMf(mf);
				o.put("kmmf", mf);
				mfsz.add(cmf);
			}

		}
		if (jmkList.size() > 0) {
			scoreDao.insertScoreTjmkBatch(xnxq, autoIncr, jmkList);
		}

		// 设置统计科目结束 开始设计成绩满分
		if (mfsz.size() > 0) {
			scoreDao.insertScoreMfBatch(xnxq, autoIncr, mfsz);
		}
	}

	private int getMfByMaxCj(float mcj) {
		// TODO Auto-generated method stub
		if (mcj <= 100) {
			return 100;
		}
		if (mcj <= 120) {
			return 120;
		}
		if (mcj > 120) {
			return 150;
		}
		return 100;
	}

	/**
	 * 设置分数段09
	 * 
	 * @param map
	 * @param lx2BmfzMap
	 *            分组类型 ：代码类型--代码值
	 * @param kslc
	 * @param nextLc
	 * @param synj
	 * @param xxdm
	 */
	private void setScoreDistribute(JSONObject params, Map<String, String> lx2BmfzMap, DegreeInfo prevDegreeInfo) {
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		Integer autoIncr = params.getInteger("autoIncr");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("xxdm", xxdm);
		map.put("nj", nj);

		// 设置总分分布设置 -分数段区间（交互已修改）
		Set<String> isClassTypeSet = new HashSet<String>();// 是否存在班级类型的默认设置
		List<Zfqjsz> zfqjList = new ArrayList<Zfqjsz>();
		if (prevDegreeInfo != null) {
			map.put("kslc", prevDegreeInfo.getKslcdm());
			map.put("xnxq", prevDegreeInfo.getXnxq());

			zfqjList = scoreDao.getZfqjsz(prevDegreeInfo.getXnxq(), prevDegreeInfo.getAutoIncr(), map);
			Iterator<Zfqjsz> iterator = zfqjList.iterator();
			while (iterator.hasNext()) {
				Zfqjsz qj = (Zfqjsz) iterator.next();
				String bjlxm = String.valueOf(qj.getOtherData());
				if (lx2BmfzMap.get(bjlxm)!=null) {
					qj.setKslc(kslc);
					qj.setFzdm(lx2BmfzMap.get(bjlxm));
					qj.setXnxq(xnxq);
					isClassTypeSet.add(bjlxm);
				}else {
					iterator.remove();
				}
				
			}
			/*for (Zfqjsz qj : zfqjList) {
				String bjlxm = String.valueOf(qj.getOtherData());

				qj.setKslc(kslc);
				qj.setFzdm(lx2BmfzMap.get(bjlxm));
				qj.setXnxq(xnxq);

				isClassTypeSet.add(bjlxm);
			}*/
		}

		// 默认检测所有班级分组类有没有继承上次考试的分数段设置，没有就进行默认设置
		for (String key : lx2BmfzMap.keySet()) {
			if (!isClassTypeSet.contains(key)) {
				Zfqjsz zfqjsz = new Zfqjsz();
				zfqjsz.setKslc(kslc);
				zfqjsz.setXxdm(xxdm);
				zfqjsz.setNj(nj);
				zfqjsz.setFzdm(lx2BmfzMap.get(key));
				zfqjsz.setFs("01");
				zfqjsz.setBl(10);
				zfqjsz.setZhqjbl(40);
				zfqjsz.setFz(100);
				zfqjsz.setZhqjfz(360);
				zfqjsz.setDkbl(10);
				zfqjsz.setDkzhqjbl(40);
				zfqjsz.setZdytext("760,740,690,590,490,290,0");
				zfqjsz.setXnxq(xnxq);
				zfqjList.add(zfqjsz);
			}
		}

		if (zfqjList.size() > 0) {
			scoreDao.insertZfqjszBatch(xnxq, autoIncr, zfqjList);
		}
	}

	@Override
	public List<JSONObject> getScoreReleaseList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		List<JSONObject> ksList = scoreDao.getScoreReleaseList(xnxq, params);
		if (CollectionUtils.isEmpty(ksList)) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}

		List<JSONObject> ksListnew = new ArrayList<JSONObject>();
		for (JSONObject ob : ksList) {
			JSONObject ne = (JSONObject) ob.clone();
			if (ob.get("alzFlag") != null) {
				if (ob.get("alzFlag").equals("1")) {
					ne.put("alzFlagName", "已分析");
				} else {
					ne.put("alzFlagName", "未分析");
				}
			} else {
				ne.put("alzFlag", "0");
				ne.put("alzFlagName", "未分析");
			}
			if (ob.get("pub2StuFlag") != null) {
				if (ob.get("pub2StuFlag").equals("1")) {
					ne.put("pub2StuFlagName", "已发布");
				} else {
					ne.put("pub2StuFlagName", "未发布");
				}
			} else {
				ne.put("pub2StuFlag", "0");
				ne.put("pub2StuFlagName", "未发布");
			}
			if (ob.get("pub2TeaFlag") != null) {
				if (ob.get("pub2TeaFlag").equals("1")) {
					ne.put("pub2TeaFlagName", "已发布");
				} else {
					ne.put("pub2TeaFlagName", "未发布");
				}
			} else {
				ne.put("pub2TeaFlag", "0");
				ne.put("pub2TeaFlagName", "未发布");
			}
			ksListnew.add(ne);
		}
		return ksListnew;
	}

	@Override
	public JSONObject updateScoreRelease(JSONObject params) {
//		logger.info("updateScoreRelease:"+params);
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		params.put("fbtime", new Date());

		String xxdm = params.getString("xxdm");
		Long schoolId = params.getLong("xxdm");
		String fbflag = params.getString("fbflag");

		int line = scoreDao.updatetDegreeInfo(xnxq, params);

		String actionMsg = "1".equals(fbflag) ? "发布" : "取消发布";

		int code = -1;
		String msg = actionMsg + "成绩失败!";

		int isShow = 0; // “1”：显示获取多少积分情况提示,0：不显示获取多少积分情况提示
		
		if (line > 0) {
			boolean flag = false;
			// 推送消息
			if ("1".equals(params.getString("fbteaflag"))) {
				flag = true;
				List<Long> ids = scoreDao.getTeacherIdFromScoreInfo(xnxq, autoIncr, params);
				/*
				 * if (ids.size() > 0) { sendMsg(xxdm, ids, T_Role.Teacher); }
				 */
				
				sendWXMsg(degreeInfo, ids, "校考成绩", 1, "Teacher");
			}
			if ("1".equals(params.getString("fbflag"))) {
				flag = true;
				List<Long> ids = scoreDao.getStudentIdFromScoreInfo(xnxq, autoIncr, params);
				/*
				 * if (ids.size() > 0) { sendMsg(xxdm, ids, T_Role.Student); }
				 */
				sendWXMsg(degreeInfo, ids, "校考成绩", 1,"Parent");
			}
			if (flag) {
				// 调用积分接口
				long accountId = params.getLong("accountId");
				long userId = 0;
				Account account = allCommonDataService.getAccountAllById(schoolId, accountId, xnxq);
				List<User> users = account.getUsers();
				for (User user : users) {
					T_Role role = user.getUserPart().getRole();
					if (role.equals(T_Role.Teacher)) {
						if (user.getTeacherPart() != null && user.getTeacherPart().getSchoolId() == schoolId) {
							userId = user.getTeacherPart().getId();
							break;
						}
					} else if (role.equals(T_Role.Staff)) {
						if (user.getStaffPart() != null && user.getStaffPart().getSchoolId() == schoolId) {
							userId = user.getStaffPart().getId();
						}
					}
				}
				if (userId != 0) {
					if ("1".equals(sswitch)) {
						int res = curCommonDataService.updateUserCreditExtern(CreditCmdID.SendScoreReport, userId);
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
		if (line > 0) {
			code = 0;
			msg = actionMsg + "成绩成功!";
		}

		JSONObject result = new JSONObject();
		result.put("code", code);
		result.put("msg", msg);
		result.put("pub2StuFlag", isShow);
		return result;
	}

	@Override
	public List<JSONObject> getASGList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		Long schoolId = params.getLong("xxdm");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		List<JSONObject> data = new ArrayList<JSONObject>();

		params.put("lb", "01"); // 文理分组
		List<TopGroupBj> list = scoreDao.getTopGroupBj(xnxq, autoIncr, params);

		// 已设班级、类别
		Map<Long, String> classTypeMap = new HashMap<Long, String>();
		List<Long> ids = new ArrayList<Long>();

		for (TopGroupBj groupBj : list) {
			long classId = Long.parseLong(groupBj.getBh());
			String classTypeId = groupBj.getTopGroup().getBjlxm();
			ids.add(classId);
			classTypeMap.put(classId, classTypeId);
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("termInfoId", xnxq);
		map.put("usedGradeId", params.getString("nj"));
		List<Classroom> classroomList = allCommonDataService.getClassList(map);

		if (CollectionUtils.isEmpty(classroomList)) {
			throw new CommonRunException(-1, "无法从基础数据接口获取班级信息，请联系管理员！");
		}

		for (Classroom classroom : classroomList) {
			JSONObject item = new JSONObject();
			long classId = classroom.getId();
			String classTypeId = classTypeMap.get(classId);

			if (classTypeId == null) {
				classTypeId = String.valueOf(classroom.getClassType().getValue());
			}

			item.put("classTypeId", classroom.getClassType().getValue());
			item.put("classId", String.valueOf(classId));
			String className = classroom.getClassName();
			item.put("className", className == null ? "" : className);
			data.add(item);
		}
		Collections.sort(data, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				String className1 = arg1.getString("className");
				String className2 = arg2.getString("className");
				return className1.compareTo(className2);
			}
		});
		return data;
	}

	@Override
	public void saveASG(JSONObject params) {
		JSONArray classes = params.getJSONArray("classes");
		params.remove("classes");
		String xnxq = params.getString("xnxq");
		String kslc = params.getString("kslc");
		Long schoolId = params.getLong("xxdm");
		String xxdm = params.getString("xxdm");
		String nj = params.getString("nj");

		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		// 需调用接口获取班级类型字典
		List<TDmBjlx> bjlxs = allCommonDataService.getClassTypeList();
		Map<String, String> sdkBjlxm2McMap = new HashMap<String, String>();
		for (TDmBjlx bj : bjlxs) {
			sdkBjlxm2McMap.put(bj.getDm(), bj.getMc());
		}

		// 先准备班级类型数据与当前考试 的所有班级数据
		params.put("lb", "01");
		List<TopGroup> groupList = scoreDao.getTopGroup(xnxq, autoIncr, params);

		Map<String, String> dbBjlxm2BmfzMap = new HashMap<String, String>();
		Map<String, Integer> dbBjlxm2NumMap = new HashMap<String, Integer>();
		for (TopGroup group : groupList) {
			dbBjlxm2BmfzMap.put(group.getBjlxm(), group.getBmfz());
			dbBjlxm2NumMap.put(group.getBjlxm(), 0);
		}

		List<TopGroup> insertGroupList = new ArrayList<TopGroup>();
		List<TopGroupBj> updateGroupBjList = new ArrayList<TopGroupBj>();

		List<Long> cids = new ArrayList<Long>();

		Map<Long, Integer> clId2BjlxmMap = new HashMap<Long, Integer>();
		// 循环班级列表 凑数据
		for (int i = 0; i < classes.size(); i++) {
			JSONObject o = classes.getJSONObject(i);
			Long classId = o.getLong("classId");
			String bjlxm = o.getString("classTypeId");

			TopGroupBj groupBj = new TopGroupBj();
			groupBj.setXnxq(xnxq);
			groupBj.setBh(String.valueOf(classId));
			groupBj.setKslc(kslc);
			groupBj.setXxdm(xxdm);

			String bmfz = dbBjlxm2BmfzMap.get(bjlxm);
			if (bmfz == null) {
				bmfz = UUIDUtil.getUUID();
				TopGroup group = new TopGroup();
				group.setBjlxm(bjlxm);
				group.setBmfz(bmfz);
				String fzmc = sdkBjlxm2McMap.get(bjlxm);
				group.setFzmc(fzmc);
				group.setKslc(kslc);
				group.setLb("01");
				group.setNj(nj);
				group.setXnxq(xnxq);
				group.setXxdm(xxdm);
				insertGroupList.add(group);

				dbBjlxm2BmfzMap.put(bjlxm, bmfz);
				dbBjlxm2NumMap.put(bjlxm, 0);
			}

			groupBj.setBmfz(bmfz);
			dbBjlxm2NumMap.put(bjlxm, dbBjlxm2NumMap.get(bjlxm) + 1);

			clId2BjlxmMap.put(classId, Integer.valueOf(bjlxm));

			updateGroupBjList.add(groupBj);
			cids.add(classId);
		}

		List<String> delBmfzList = new ArrayList<String>();
		for (Map.Entry<String, String> entry : dbBjlxm2BmfzMap.entrySet()) {
			String bjlxm = entry.getKey();
			String bmfz = entry.getValue();
			if (0 == dbBjlxm2NumMap.get(bjlxm)) {
				delBmfzList.add(bmfz);
			}
		}

		params.put("bhList", cids);
		scoreDao.deleteTopGroupBj(xnxq, autoIncr, params);
		params.remove("bhList");

		// 删掉此次设置数据库多余的文理分组
		if (delBmfzList.size() > 0) {
			params.put("lb", "01");
			params.put("bmfzList", delBmfzList);
			scoreDao.deleteTopGroup(xnxq, autoIncr, params);
			params.remove("bmfzList");
		}

		// 保存新增的文理分组
		if (insertGroupList.size() > 0) {
			scoreDao.insertTopGroupBatch(xnxq, autoIncr, insertGroupList);
		}
		// 保存 分组班级
		scoreDao.insertTopGroupBjBatch(xnxq, autoIncr, updateGroupBjList);
		
		boolean isChangeClassType = false;
		// 更新班级类型代码
		List<Classroom> classroomList = allCommonDataService.getClassroomBatchNoAccount(schoolId, cids, xnxq);
		if (CollectionUtils.isNotEmpty(classroomList)) {
			for (Classroom classroom : classroomList) {
				if (classroom == null) {
					continue;
				}

				long classId = classroom.getId();

				Integer type = clId2BjlxmMap.get(classId);
				if (type == null) {
					type = 0;
				}
				int ctype = 0;
				if (classroom.getClassType() != null) {
					ctype = classroom.getClassType().getValue();
				}
				if (ctype != type) {
					isChangeClassType = true;
					Classroom c = allCommonDataService.getClassById(schoolId, classId, xnxq);
					c.setClassType(T_ClassType.findByValue(type));
					allCommonDataService.updateClassroom(schoolId, c, xnxq);
				}
			}
		}
		
		if(isChangeClassType) {
			params.put("lb", "02");
			// 先删除班级分组详细
			scoreDao.deleteTopGroupBj(xnxq, autoIncr, params);
			// 再删除分组
			scoreDao.deleteTopGroup(xnxq, autoIncr, params);
			
			params.put("lb", "01");
			params.put("fw", "null");
			scoreDao.updateTopGroup(xnxq, autoIncr, params);
		}
	}

	@Override
	public List<JSONObject> getBjfzList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		List<JSONObject> result = scoreDao.getBjfzList(xnxq, autoIncr, params);
		int isAll = params.getIntValue("isAll");
		if (isAll > 0) {
			JSONObject item = new JSONObject();
			item.put("text", "全部");
			StringBuffer allVal = new StringBuffer();
			for (JSONObject json : result) {
				allVal.append(json.getString("value")).append(",");
			}
			if (allVal.length() > 0) {
				allVal.deleteCharAt(allVal.length() - 1);
			}
			item.put("value", allVal.toString());
			result.add(0, item);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getClassGroupList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		Long schoolId = params.getLong("xxdm");

		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		List<Long> classIds = new ArrayList<Long>();
		List<JSONObject> groupedClassList = scoreDao.getYfzList(xnxq, autoIncr, params);
		for (JSONObject json : groupedClassList) {
			String bhStr = json.getString("bhStr");
			json.remove("bhStr");
			List<Long> ids = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
			classIds.addAll(ids);
			json.put("bhList", ids);
		}

		if (classIds.size() > 0) {
			params.put("noBhList", classIds);
		}
		List<Long> bhList = scoreDao.getWfzList(xnxq, autoIncr, params);
		classIds.addAll(bhList);

		List<Classroom> classroomList = allCommonDataService.getClassroomBatchNoAccount(schoolId, classIds, xnxq);
		if (CollectionUtils.isEmpty(classroomList)) {
			throw new CommonRunException(-1, "无法从SDK获取基础数据，请联系管理员！");
		}

		Map<Long, String> classId2NameMap = new HashMap<Long, String>();
		for (Classroom classroom : classroomList) {
			classId2NameMap.put(classroom.getId(), classroom.getClassName());
		}

		for (JSONObject json : groupedClassList) {
			List<Long> ids = (List<Long>) json.get("bhList");
			json.remove("bhList");
			StringBuffer strbuf = new StringBuffer();
			for (Long classId : ids) {
				String className = classId2NameMap.get(classId);
				if (className == null) {
					continue;
				}
				strbuf.append(className).append(",");
			}

			if (strbuf.length() > 0) {
				strbuf.deleteCharAt(strbuf.length() - 1);
			}
			json.put("classNamelist", strbuf.toString());
		}

		List<JSONObject> nogroupedClassList = new ArrayList<JSONObject>();
		for (Long classId : bhList) {
			String className = classId2NameMap.get(classId);
			if (className == null) {
				continue;
			}
			JSONObject json = new JSONObject();
			json.put("classId", classId);
			json.put("className", className);
			nogroupedClassList.add(json);
		}

		JSONObject data = new JSONObject();
		data.put("noGroupClass", nogroupedClassList);
		JSONObject groupedClass = new JSONObject();
		groupedClass.put("total", groupedClassList.size());
		groupedClass.put("rows", groupedClassList);
		data.put("groupedClass", groupedClass);

		return data;
	}

	@Override
	public void addClassGroup(JSONObject params, List<String> classIds) {
		String xnxq = params.getString("xnxq");

		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		params.put("lb", "02");
		if (scoreDao.ifExistsSameTopGroup(xnxq, autoIncr, params)) {
			throw new CommonRunException(-1, "存在相同的分组名称，请修改分组名称！");
		}

		String kslc = params.getString("kslc");
		String xxdm = params.getString("xxdm");
		String nj = params.getString("nj");
		String ssfz = params.getString("ssfz");
		String fzmc = params.getString("fzmc");

		// 新增班级分组
		TopGroup group = new TopGroup();
		String bmfz = UUIDUtil.getUUID();
		group.setBmfz(bmfz);
		group.setFzmc(fzmc);
		group.setKslc(kslc);
		group.setNj(nj);
		group.setLb("02");
		group.setSsfz(ssfz);
		group.setXnxq(xnxq);
		group.setXxdm(xxdm);
		List<TopGroup> groupList = new ArrayList<TopGroup>();
		groupList.add(group);

		List<TopGroupBj> groupBjList = new ArrayList<TopGroupBj>();
		// 新增班级分组关联表
		for (String classId : classIds) {
			TopGroupBj groupBj = new TopGroupBj();
			groupBj.setBh(classId);
			groupBj.setBmfz(bmfz);
			groupBj.setKslc(kslc);
			groupBj.setXnxq(xnxq);
			groupBj.setXxdm(xxdm);
			groupBjList.add(groupBj);
		}

		scoreDao.insertTopGroupBjBatch(xnxq, autoIncr, groupBjList);
		scoreDao.insertTopGroupBatch(xnxq, autoIncr, groupList);
	}

	@Override
	public void delClassGroup(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		scoreDao.deleteTopGroupBj(xnxq, autoIncr, params);
		scoreDao.deleteTopGroup(xnxq, autoIncr, params);
	}

	@Override
	public JSONObject getASGListForStatic(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		List<JSONObject> list = scoreDao.getASGListForStatic(xnxq, autoIncr, params);
		if (CollectionUtils.isNotEmpty(list)) {
			List<JSONObject> groupNumList = scoreDao.getSubjectGroupNumById(xnxq, autoIncr, params);
			Map<String, Integer> groupNumMap = new HashMap<String, Integer>();
			for (JSONObject json : groupNumList) {
				String agsId = json.getString("agsId");
				Integer num = json.getInteger("groupNum");

				groupNumMap.put(agsId, num);
			}

			for (JSONObject obj : list) {
				if (obj == null)
					continue;
				String agsId = obj.getString("asgId");
				Integer groupNum = groupNumMap.get(agsId);
				if (groupNum != null) {
					obj.put("asgNum", groupNum);
				} else {
					obj.put("asgNum", "");
				}
			}
		}

		JSONObject data = new JSONObject();
		data.put("total", list.size());
		data.put("rows", list);
		return data;
	}

	@Override
	public void updateASGStatRules(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String xxdm = params.getString("xxdm");
		String kslc = params.getString("kslc");

		JSONArray staticNumGroups = params.getJSONArray("staticNumGroups");
		for (Object obj : staticNumGroups) {
			JSONObject json = (JSONObject) obj;
			json.put("xxdm", xxdm);
			json.put("xnxq", xnxq);
			json.put("kslc", kslc);

			if (json.getString("range").equals("")) {
				json.put("range", null);
			}
		}

		scoreDao.updateASGStatRules(xnxq, autoIncr, staticNumGroups);
	}

	@Override
	public JSONObject getClassGroupListStatic(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		JSONObject data = new JSONObject();

		long schoolId = params.getLongValue("xxdm");

		List<JSONObject> list = scoreDao.getClassGroupListStatic(xnxq, autoIncr, params);
		if (list.size() == 0) {
			data.put("total", list.size());
			data.put("rows", list);
			return data;
		}

		// 调用接口获取班级信息
		List<Long> cids = new ArrayList<Long>();
		for (JSONObject j : list) {
			cids.add(j.getLongValue("classId"));
		}

		// 班级
		Map<Long, String> classId2NameMap = new HashMap<Long, String>();
		List<Classroom> classList = allCommonDataService.getClassroomBatchNoAccount(schoolId, cids, xnxq);
		if (CollectionUtils.isEmpty(classList)) {
			throw new CommonRunException(-1, "无法从SDK获取基础数据，请联系管理员！");
		}
		for (Classroom c : classList) {
			classId2NameMap.put(c.getId(), c.getClassName());
		}

		for (JSONObject json : list) {
			json.put("className", classId2NameMap.get(json.getLong("classId")));
		}

		data.put("total", list.size());
		data.put("rows", list);

		return data;
	}

	@Override
	public JSONObject getStatStuList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		long schoolId = params.getLongValue("xxdm");
		String bh = params.getString("bh");
		params.remove("bh");
		String xhxm = params.getString("xhxm");
		params.remove("xhxm");

		// 调用接口获取班级信息
		HashMap<String, Object> studentParam = new HashMap<String, Object>();
		studentParam.put("termInfoId", xnxq);
		studentParam.put("schoolId", params.getString("xxdm"));
		studentParam.put("classId", bh);
		studentParam.put("keyword", xhxm);
		List<Account> allStu = allCommonDataService.getStudentList(studentParam);// 获取所有学生，需从深圳调用接口拼接

		List<Long> cIds = StringUtil.convertToListFromStr(bh, ",", Long.class);

		List<Classroom> classroomList = allCommonDataService.getClassroomBatchNoAccount(schoolId, cIds, xnxq);

		Map<Long, String> classId2NameMap = new HashMap<Long, String>();
		for (Classroom classroom : classroomList) {
			classId2NameMap.put(classroom.getId(), classroom.getClassName());
		}

		List<JSONObject> cyxs = new ArrayList<JSONObject>(); // 参与统计的学生
		List<JSONObject> bcyxs = new ArrayList<JSONObject>(); // 不参与统计的学生

		Set<Long> bcyXhSet = scoreDao.getBcytjXhList(xnxq, autoIncr, params);// 不参与统计的学号

		for (Account acc : allStu) {
			long accId = acc.getId();
			if (null == acc.getUsers()) {
				continue;
			}

			for (User u : acc.getUsers()) {
				if (u.getUserPart() == null) {
					continue;
				}
				if (T_Role.Student.equals(u.getUserPart().getRole())) {
					StudentPart sp = u.getStudentPart();

					JSONObject stud = new JSONObject();
					stud.put("studentId", accId);
					stud.put("studentNo", sp.getSchoolNumber());
					stud.put("studentName", acc.getName());
					stud.put("className",
							classId2NameMap.containsKey(sp.getClassId()) ? classId2NameMap.get(sp.getClassId()) : "");

					if (bcyXhSet.contains(accId)) {
						bcyxs.add(stud);
					} else {
						cyxs.add(stud);
					}
					break;
				}
			}
		}

		Collections.sort(cyxs, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				String className1 = arg1.getString("className");
				String className2 = arg2.getString("className");
				return className1.compareTo(className2);
			}
		});

		Collections.sort(bcyxs, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				String className1 = arg1.getString("className");
				String className2 = arg2.getString("className");
				return className1.compareTo(className2);
			}
		});

		JSONObject cytjxs = new JSONObject();
		cytjxs.put("total", cyxs.size());
		cytjxs.put("rows", cyxs);
		JSONObject bcytjxs = new JSONObject();
		bcytjxs.put("total", bcyxs.size());
		bcytjxs.put("rows", bcyxs);

		JSONObject data = new JSONObject();
		data.put("inStaticStd", cytjxs);
		data.put("outStaticStd", bcytjxs);
		return data;
	}

	@Override
	public void updateClassGroupStatic(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String xxdm = params.getString("xxdm");
		String kslc = params.getString("kslc");
		String bmfz = params.getString("bmfz");
		JSONArray bjfz = params.getJSONArray("bjfz");

		for (Object obj : bjfz) {
			JSONObject json = (JSONObject) obj;
			if ("".equals(json.getString("range"))) {
				json.put("range", null);
			}
			json.put("kslc", kslc);
			json.put("bmfz", bmfz);
			json.put("xxdm", xxdm);
			json.put("xnxq", xnxq);
			scoreDao.updateClassGroupStatic(xnxq, autoIncr, json);
		}
	}

	@Override
	public void addStatStuList(JSONObject params, List<String> studentIds) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String xxdm = params.getString("xxdm");
		String kslc = params.getString("kslc");

		List<Xscjbtj> needInsert = new ArrayList<Xscjbtj>();
		for (String studentId : studentIds) {
			Xscjbtj btj = new Xscjbtj();
			btj.setKslc(kslc);
			btj.setXh(studentId);
			btj.setXxdm(xxdm);
			btj.setXnxq(xnxq);
			needInsert.add(btj);
		}

		scoreDao.insertXscjbtjBatch(xnxq, autoIncr, needInsert);
	}

	@Override
	public void delStatStuList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		scoreDao.deleteXscjbtj(xnxq, autoIncr, params);
	}

	@Override
	public JSONObject getStatSubjectList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		School school = (School) params.get("school");
		params.remove("school");

		List<LessonInfo> kmList = allCommonDataService.getLessonInfoList(school, xnxq);// 从深圳调接口得到科目
		Map<String, String> kmMap = new HashMap<String, String>();
		for (int i = 0; i < kmList.size(); i++) {
			LessonInfo j = kmList.get(i);
			kmMap.put(j.getId() + "", j.getName());
		}

		List<JSONObject> list = scoreDao.getSubjectStatusList(xnxq, autoIncr, params);
		for (JSONObject json : list) {
			json.put("subjectName", kmMap.get(json.get("subjectId")));
			float maxScore = json.getFloatValue("maxScore");
			if (!json.containsKey("fullScore")) {
				json.put("fullScore", getMfByMaxCj(maxScore));
			}
		}

		JSONObject data = new JSONObject();
		data.put("total", list.size());
		data.put("rows", list);
		return data;
	}

	@Override
	public void updateStatSubjectList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String xxdm = params.getString("xxdm");
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");
		String bmfz = params.getString("bmfz");

		JSONArray bjfzList = params.getJSONArray("bjfzList");

		List<ScoreTjmk> scoreTjmkList = new ArrayList<ScoreTjmk>(bjfzList.size());
		List<ScoreMf> scoreMfList = new ArrayList<ScoreMf>(bjfzList.size());
		for (Object obj : bjfzList) {
			JSONObject json = (JSONObject) obj;

			String kmdm = json.getString("subjectId");
			Integer mf = json.getInteger("fullScore");

			ScoreTjmk scoreTjmk = new ScoreTjmk();
			scoreTjmk.setFzdm(bmfz);
			scoreTjmk.setKmdm(kmdm);
			scoreTjmk.setKslc(kslc);
			scoreTjmk.setNj(nj);
			scoreTjmk.setXnxq(xnxq);
			scoreTjmk.setXxdm(xxdm);
			scoreTjmkList.add(scoreTjmk);

			ScoreMf scoreMf = new ScoreMf();
			scoreMf.setFzdm(bmfz);
			scoreMf.setKmdm(kmdm);
			scoreMf.setKslc(kslc);
			scoreMf.setMf(mf);
			scoreMf.setNj(nj);
			scoreMf.setXnxq(xnxq);
			scoreMf.setXxdm(xxdm);
			scoreMfList.add(scoreMf);
		}

		if (bjfzList.size() > 0) {
			scoreDao.deleteScoretjmk(xnxq, autoIncr, params);
			scoreDao.deleteScoreMF(xnxq, autoIncr, params);

			scoreDao.insertScoreTjmkBatch(xnxq, autoIncr, scoreTjmkList);
			scoreDao.insertScoreMfBatch(xnxq, autoIncr, scoreMfList);
		}
	}

	@Override
	public JSONObject getMergedSubjectList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		School school = (School) params.get("school");

		List<LessonInfo> allkmlist = allCommonDataService.getLessonInfoList(school, xnxq);
		Map<Long, LessonInfo> subjId2ObjMap = new HashMap<Long, LessonInfo>();
		for (LessonInfo lessonInfo : allkmlist) {
			subjId2ObjMap.put(lessonInfo.getId(), lessonInfo);
		}

		List<JSONObject> noMergedList = new ArrayList<JSONObject>();
		List<Long> subjectIds = scoreDao.getSubjectIdsFromScoreInfo(xnxq, autoIncr, params);
		for (Long subjectId : subjectIds) {
			LessonInfo lessonInfo = subjId2ObjMap.get(subjectId);
			if (lessonInfo.getType() == 0) {
				JSONObject oj = new JSONObject();
				oj.put("subjectId", lessonInfo.getId());
				oj.put("subjectName", lessonInfo.getName());
				noMergedList.add(oj);
			}
		}

		List<JSONObject> klist = new ArrayList<JSONObject>();
		// 得到合并科目
		params.put("kclx", "01");
		List<JSONObject> listZH = scoreDao.getMergerSubjectList(xnxq, autoIncr, params);
		if (listZH.size() > 0) {
			for (JSONObject json : listZH) {
				Long chsId = json.getLong("chsId");
				List<Long> kmdmList = StringUtil.convertToListFromStr(json.getString("kmlist"), ",", Long.class);

				if (kmdmList.size() == 0) {
					continue;
				}

				StringBuffer strbuf = new StringBuffer();
				for (Long kmdm : kmdmList) {
					if (kmdm == null) {
						continue;
					}
					LessonInfo lessonInfo = subjId2ObjMap.get(kmdm);
					if (lessonInfo != null) {
						strbuf.append(lessonInfo.getName()).append(",");
					}
				}
				if (strbuf.length() > 0) {
					strbuf.deleteCharAt(strbuf.length() - 1);
				}
				json.put("chsName", subjId2ObjMap.containsKey(chsId) ? subjId2ObjMap.get(chsId).getName() : "");
				json.put("subjectList", strbuf.toString());
				klist.add(json);
			}
		}

		JSONObject data = new JSONObject();
		JSONObject objZH = new JSONObject();
		objZH.put("total", klist.size());
		objZH.put("rows", klist);
		data.put("chsSubjects", objZH);
		data.put("noChsSubjects", noMergedList);// 所有非综合科目
		return data;
	}

	@Override
	public void addMergerSubject(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		List<JSONObject> list = new ArrayList<JSONObject>();
		list.add(params);
		scoreDao.insertSynthScoreBatch(xnxq, autoIncr, list);
	}

	@Override
	public void delMergerSubject(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		scoreDao.deleteSynthScore(xnxq, autoIncr, params);
	}

	@Override
	public JSONObject getStatisticalParameters(String xxdm) {
		JSONObject data = new JSONObject();

		List<JXPGScoreSection> list = scoreDao.getJxpgScoreSection(xxdm);
		if (list.size() > 0) {
			for (JXPGScoreSection scoreSection : list) {
				String dm = scoreSection.getDm();
				data.put(getDmName(dm), scoreSection.getFzbfb());
			}
			data.put("way", list.get(0).getFs()); // 方式
		}

		ScoreStatTerm term = scoreDao.getScoreStatTerm(xxdm);
		if (null != term) {
			data.put("absent", term.getQk());
			data.put("cheat", term.getWb());
			data.put("zero", term.getLf());
		}
		return data;
	}

	/**
	 * 根据分类标号返回对应的参数名称给前台使用
	 * 
	 * @param dm
	 * @return
	 */
	private String getDmName(String dm) {
		String d = "";
		switch (dm) {
		case "01":
			d = "excellentPer";
			break;
		case "02":
			d = "passPer";
			break;
		case "03":
			d = "lowPer";
			break;
		case "04":
			d = "topPer";
			break;
		case "05":
			d = "potentialPer";
			break;
		default:
			d = "";
		}
		return d;
	}

	@Override
	public void updateStatisticalParameters(JSONObject params) {
		String fs = params.getString("way");
		Float yxlbfb = params.getFloat("excellentPer");
		Float hglbfb = params.getFloat("passPer");
		Float dflbfb = params.getFloat("lowPer");
		Float jzsbfb = params.getFloat("topPer");
		Float qnsbfb = params.getFloat("potentialPer");
		String qk = params.getString("absent");
		String wb = params.getString("cheat");
		String lf = params.getString("zero");
		String xxdm = params.getString("xxdm");

		List<JXPGScoreSection> list = new ArrayList<JXPGScoreSection>();

		JXPGScoreSection jxpg = new JXPGScoreSection();
		jxpg.setFs(fs);
		jxpg.setXxdm(xxdm);
		jxpg.setDm("01");
		jxpg.setFzbfb(yxlbfb);
		jxpg.setMc(getStaName("01"));
		list.add(jxpg);

		jxpg = new JXPGScoreSection();
		jxpg.setFs(fs);
		jxpg.setXxdm(xxdm);
		jxpg.setDm("02");
		jxpg.setFzbfb(hglbfb);
		jxpg.setMc(getStaName("02"));
		list.add(jxpg);

		jxpg = new JXPGScoreSection();
		jxpg.setFs(fs);
		jxpg.setXxdm(xxdm);
		jxpg.setDm("03");
		jxpg.setFzbfb(dflbfb);
		jxpg.setMc(getStaName("03"));
		list.add(jxpg);

		jxpg = new JXPGScoreSection();
		jxpg.setFs(fs);
		jxpg.setXxdm(xxdm);
		jxpg.setDm("04");
		jxpg.setFzbfb(jzsbfb);
		jxpg.setMc(getStaName("04"));
		list.add(jxpg);

		jxpg = new JXPGScoreSection();
		jxpg.setFs(fs);
		jxpg.setXxdm(xxdm);
		jxpg.setDm("05");
		jxpg.setFzbfb(qnsbfb);
		jxpg.setMc(getStaName("05"));
		list.add(jxpg);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("qk", qk);
		map.put("wb", wb);
		map.put("lf", lf);
		map.put("xxdm", xxdm);

		scoreDao.deleteStatisticalParameters(xxdm);
		scoreDao.insertScoreStatTerm(map);
		scoreDao.insertJXPGScoreSectionBatch(list);
	}

	/**
	 * 根据分类标号返回对应的参数名称给数据库使用
	 * 
	 * @param dm
	 * @return
	 */
	private String getStaName(String dm) {
		String d = "";
		switch (dm) {
		case "01":
			d = "优秀率";
			break;
		case "02":
			d = "合格率";
			break;
		case "03":
			d = "低分率";
			break;
		case "04":
			d = "尖子生";
			break;
		case "05":
			d = "潜能生";
			break;
		default:
			d = "";
		}
		return d;
	}

	@Override
	public JSONObject getIntervalList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		List<JSONObject> list = scoreDao.getIntervalList(xnxq, autoIncr, params);

		JSONObject data = new JSONObject();
		data.put("total", list.size());
		data.put("rows", list);
		return data;
	}

	@Override
	public void updateInterval(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		JSONArray pmfbs = params.getJSONArray("pmfbs");
		params.remove("pmfbs");

		String xxdm = params.getString("xxdm");
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");
		String fzdm = params.getString("fzdm");

		List<ScoreRankDistribute> list = new ArrayList<ScoreRankDistribute>();
		String format = "%02d";
		for (int i = 0; i < pmfbs.size(); i++) {
			JSONObject json = pmfbs.getJSONObject(i);
			Integer upLimit = json.getInteger("upLimit");
			Integer underLimit = json.getInteger("underLimit");

			ScoreRankDistribute rankDist = new ScoreRankDistribute();
			rankDist.setKslc(kslc);
			rankDist.setXxdm(xxdm);
			rankDist.setXnxq(xnxq);
			rankDist.setNj(nj);
			rankDist.setFzdm(fzdm);
			rankDist.setPmfbdm(String.format(format, i + 1));
			rankDist.setPmfbsx(upLimit);
			rankDist.setPmfbxx(underLimit);
			list.add(rankDist);
		}

		if (list.size() > 0) {
			scoreDao.deleteScoreRankDistribute(xnxq, autoIncr, params);
			scoreDao.insertScoreRankDistributeBatch(xnxq, autoIncr, list);
		}
	}

	@Override
	public JSONObject getTotalScoreSection(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		JSONObject data = scoreDao.getTotalScoreSection(xnxq, autoIncr, params);
		if (data == null) {
			data = new JSONObject();
		}
		return data;
	}

	@Override
	public void updateTotalScoreSection(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		scoreDao.insertZfqjsz(xnxq, autoIncr, params.get("zfqjsz"));
	}

	@Override
	public JSONObject getContrastExamList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String xxdm = params.getString("xxdm");

		List<String> njList = scoreDao.getNjFromDegreeInfoNj(xnxq, params);

		List<Dbkslc> selectedDbKslcList = scoreDao.getDbkslcList(xnxq, autoIncr, params);
		Map<String, Set<String>> xnxq2SelectedKslcdmMap = new HashMap<String, Set<String>>();
		for (Dbkslc dbkslc : selectedDbKslcList) {
			String dbxnxq = dbkslc.getDbxnxq();
			if (!xnxq2SelectedKslcdmMap.containsKey(dbxnxq)) {
				xnxq2SelectedKslcdmMap.put(dbxnxq, new HashSet<String>());
			}
			xnxq2SelectedKslcdmMap.get(dbxnxq).add(dbkslc.getDbkslc());
		}

		Integer minNj = Integer.valueOf(njList.get(0));
		for (String nj : njList) {
			Integer synj = Integer.valueOf(nj);
			if (synj < minNj) {
				minNj = synj;
			}
		}

		List<String> xnxqList = TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(allCommonDataService,
				String.valueOf(minNj), xnxq);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("xxdm", xxdm);
		map.put("ltCdate", degreeInfo.getCdate());
		map.put("njList", njList);

		List<JSONObject> list = new ArrayList<JSONObject>();
		for (String termInfoId : xnxqList) {
			map.put("xnxq", termInfoId);
			String termInfoName = TermUtil.formatTerm(termInfoId);
			Set<String> dbkslcdmSet = xnxq2SelectedKslcdmMap.get(termInfoId);

			List<DegreeInfo> dbExamList = scoreDao.getDegreeInfoList(termInfoId, map);
			for (DegreeInfo dbExam : dbExamList) {
				String kslcdm = dbExam.getKslcdm();

				JSONObject json = new JSONObject();
				json.put("termInfoName", termInfoName);
				json.put("examName", dbExam.getKslcmc());
				json.put("examId", kslcdm);
				json.put("termInfoId", termInfoId);

				if (dbkslcdmSet != null && dbkslcdmSet.contains(kslcdm)) {
					json.put("contrastExamId", kslcdm);
				}
				list.add(json);
			}
		}

		JSONObject data = new JSONObject();
		data.put("total", list.size());
		data.put("rows", list);
		return data;
	}

	@Override
	public void updateContrastExamList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		String kslc = params.getString("kslcdm");
		String xxdm = params.getString("xxdm");

		scoreDao.deleteContrastExam(xnxq, autoIncr, params);

		JSONArray data = params.getJSONArray("data");
		List<Dbkslc> list = new ArrayList<Dbkslc>();
		for (int i = 0; i < data.size(); i++) {
			JSONObject json = data.getJSONObject(i);

			Dbkslc dbkslc = new Dbkslc();
			dbkslc.setKslc(kslc);
			dbkslc.setXxdm(xxdm);
			dbkslc.setXnxq(xnxq);
			dbkslc.setDbkslc(json.getString("dbkslc"));
			dbkslc.setDbxnxq(json.getString("dbxnxq"));
			list.add(dbkslc);
		}

		scoreDao.insertDbkslcBatch(xnxq, autoIncr, list);
	}

	@Override
	public JSONObject getRankingsValueList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		// 设置等第值没有调通
		Long schoolId = params.getLong("xxdm");

		List<JSONObject> settingList = new ArrayList<JSONObject>();
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		Set<Long> subjIdSet = new HashSet<Long>();
		// 得到等第值，第一行
		List<JSONObject> list = scoreDao.getRankingsByKMDM(xnxq, autoIncr, params);
		for (JSONObject json : list) {
			String subjectId = json.getString("subjectId");
			if (StringUtils.isEmpty(subjectId)) {
				settingList.add(json);
			} else {
				dataList.add(json);
				subjIdSet.add(Long.valueOf(subjectId));
			}
		}

		if (settingList.size() == 0) {
			settingList = scoreDao.getAllDJList(xnxq, autoIncr, params);
		}

		Collections.sort(settingList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				int dm1 = arg1.getIntValue("levelId");
				int dm2 = arg2.getIntValue("levelId");
				return Integer.compare(dm1, dm2);
			}
		});

		List<List<JSONObject>> columns = new ArrayList<List<JSONObject>>();
		List<JSONObject> titleList = new ArrayList<JSONObject>();
		for (JSONObject json : settingList) {
			JSONObject item = new JSONObject();
			item.put("field", "s" + json.getString("levelId"));
			item.put("title", json.getString("levelName"));
			item.put("align", "center");
			item.put("width", 120);
			item.put("rowspan", 1);
			item.put("colspan", 1);
			item.put("sortable", true);
			titleList.add(item);
		}
		columns.add(titleList);

		List<LessonInfo> kmList = allCommonDataService.getLessonInfoBatch(schoolId, new ArrayList<Long>(subjIdSet),
				xnxq);
		Map<Long, String> subjId2NameMap = new HashMap<Long, String>();
		for (LessonInfo le : kmList) {
			subjId2NameMap.put(le.getId(), le.getName());
		}

		Map<Long, JSONObject> dataMap = new HashMap<Long, JSONObject>();
		for (JSONObject json : dataList) {
			Long subjectId = json.getLong("subjectId");

			if (!dataMap.containsKey(subjectId)) {
				dataMap.put(subjectId, new JSONObject());
			}

			JSONObject item = dataMap.get(subjectId);
			item.put("subjectId", subjectId);
			item.put("subjectName", subjId2NameMap.get(subjectId));
			String levelId = json.getString("levelId");
			String levelName = json.getString("levelName");
			String percent = json.getString("percent");

			if (levelId.equals("05")) {
				String text = "-1," + json.get("stdNum") + "," + levelId + "," + levelName + "," + percent;
				item.put("s" + json.getString("levelId"), text);
			} else {
				String text = json.get("convertScore") + "," + json.get("stdNum") + "," + levelId + "," + levelName
						+ "," + percent;
				item.put("s" + json.getString("levelId"), text);
			}
		}

		JSONObject jsonKM = new JSONObject();
		jsonKM.put("total", dataMap.size());
		jsonKM.put("rows", dataMap.values());
		jsonKM.put("columns", columns);

		JSONObject data = new JSONObject();
		data.put("levelSetting", settingList);
		data.put("subjectLevelSetting", jsonKM);
		return data;
	}

	@Override
	public List<JSONObject> getRankingsByKMDM(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		return scoreDao.getRankingsByKMDM(xnxq, autoIncr, params);
	}

	@Override
	public void updateBillDiviLevelSetting(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String kmdm = params.getString("kmdm");
		String xxdm = params.getString("xxdm");
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");
		JSONArray djsz = params.getJSONArray("levelSetting");

		List<ScoreLevelTemplate> list = new ArrayList<ScoreLevelTemplate>();
		for (int i = 0; i < djsz.size(); i++) {
			JSONObject js = djsz.getJSONObject(i);

			ScoreLevelTemplate level = new ScoreLevelTemplate();
			level.setKslc(kslc);
			level.setXnxq(xnxq);
			level.setXxdm(xxdm);
			level.setNj(nj);
			level.setKmdm(kmdm);
			level.setBl(js.getFloatValue("percent"));
			level.setCj(js.getFloatValue("convertScore"));
			level.setDjmc(js.getString("levelName"));
			level.setDm(js.getString("levelId"));
			level.setRs(js.getIntValue("stdNum"));

			list.add(level);
		}

		scoreDao.insertScoreLevelTemplateBatch(xnxq, autoIncr, list);
	}

	@Override
	public void delBillDiviLevelSetting(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		scoreDao.deleteScoreLevelTemplate(xnxq, autoIncr, params);
	}

	@Override
	public void updateRankingsValue(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String xxdm = params.getString("xxdm");
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");
		JSONArray djsz = params.getJSONArray("djsz");
		params.remove("djsz");

		// 删除当前年级等级设置
		scoreDao.deleteScoreLevelTemplate(xnxq, autoIncr, params);

		// 设置等第值
		List<ScoreLevelTemplate> sltpList = new ArrayList<ScoreLevelTemplate>();
		for (int i = 0; i < djsz.size(); i++) {
			JSONObject json = djsz.getJSONObject(i);
			ScoreLevelTemplate tl = new ScoreLevelTemplate();
			tl.setKslc(kslc);
			tl.setXnxq(xnxq);
			tl.setXxdm(xxdm);

			tl.setNj(nj);
			tl.setKmdm("");
			tl.setDjmc(json.getString("levelName"));
			tl.setDm(json.getString("levelId"));
			tl.setBl(json.getFloatValue("percent"));
			tl.setRs(json.getIntValue("stdNum"));
			tl.setCj(0f);
			sltpList.add(tl);
		}

		// 获取合并科目列表
		List<SynthScore> tsynthList = scoreDao.getSynthScoreList(xnxq, autoIncr, params);
		// 获取成绩并分析
		List<JSONObject> cjList = scoreDao.selectCjListForDdSet(xnxq, autoIncr, params);
		// 统计计算最终得到插入的数据list
		List<ScoreLevelTemplate> sltpList2 = ScoreUtil.getDJInfo(params, sltpList, tsynthList, cjList);
		if (sltpList2.size() > 0) {
			scoreDao.insertScoreLevelTemplateBatch(xnxq, autoIncr, sltpList2);
		}
	}

	@Override
	public List<JSONObject> getSingleSubjectGrade(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String xxdm = params.getString("xxdm");
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");
		JSONArray djsz = params.getJSONArray("djsz");

		// 设置等第值
		List<ScoreLevelTemplate> sltpList = new ArrayList<ScoreLevelTemplate>();
		for (int i = 0; i < djsz.size(); i++) {
			JSONObject json = (JSONObject) djsz.get(i);
			ScoreLevelTemplate tl = new ScoreLevelTemplate();
			tl.setXnxq(xnxq);
			tl.setXxdm(xxdm);
			tl.setNj(nj);
			tl.setKmdm("");
			tl.setKslc(kslc);
			tl.setDjmc(json.getString("levelName"));
			tl.setDm(json.getString("levelId"));
			tl.setBl(json.getFloatValue("percent"));
			tl.setRs(json.getIntValue("stdNum"));
			tl.setCj(json.getFloat("convertScore"));
			sltpList.add(tl);
		}

		// 获取合并科目列表
		List<SynthScore> tsynthList = scoreDao.getSynthScoreList(xnxq, autoIncr, params);

		// 获取成绩并分析
		List<JSONObject> cjList = scoreDao.selectCjListForDdSet(xnxq, autoIncr, params);
		// 统计计算最终得到数据list
		List<ScoreLevelTemplate> sltpList2 = ScoreUtil.getSingleDJInfo(params, sltpList, tsynthList, cjList);

		List<JSONObject> data = new ArrayList<JSONObject>();
		for (ScoreLevelTemplate tth : sltpList2) {
			JSONObject json = new JSONObject();
			json.put("levelId", tth.getDm());
			json.put("levelName", tth.getDjmc());
			json.put("percent", null == tth.getBl() ? 100 : tth.getBl());
			json.put("convertScore", null == tth.getCj() ? 0 : tth.getCj());
			json.put("stdNum", tth.getRs());
			data.add(json);
		}
		return data;
	}

	@Override
	public JSONObject getClassReportParam(String xxdm) {
		List<JSONObject> list = scoreDao.getClassReportParam(xxdm);

		JSONObject data = new JSONObject();
		for (JSONObject j : list) {
			if (j.getString("dm").equals("01")) {
				data.put("progressParam", j.getIntValue("szz"));
			} else {
				data.put("backwardParam", j.getIntValue("szz"));
			}
		}

		return data;
	}

	@Override
	public void updateClassReportParam(JSONObject params) {
		String xxdm = params.getString("xxdm");
		int progressParam = params.getIntValue("progressParam");// 进步之星参数
		int backwardParam = params.getIntValue("backwardParam");// 异常学生参数

		List<SettingBJ> list = new LinkedList<SettingBJ>();
		SettingBJ settingBj = new SettingBJ();
		settingBj.setXxdm(xxdm);
		settingBj.setSzz(progressParam);
		settingBj.setDm("01");
		list.add(settingBj);

		settingBj = new SettingBJ();
		settingBj.setXxdm(xxdm);
		settingBj.setSzz(backwardParam);
		settingBj.setDm("02");
		list.add(settingBj);

		scoreDao.insertClassReportParamBatch(list);
	}

	@Override
	public JSONObject getStudentReportParam(String xxdm) {
		List<SettingStu> list = scoreDao.getSettingStuList(xxdm);

		JSONObject data = new JSONObject();
		for (SettingStu j : list) {
			String dm = j.getDm();
			String tsydm = j.getTsydm();
			Integer szz = j.getSzz();
			// 进步
			if (dm.equals("01")) {
				if (tsydm.equals("01")) {
					data.put("highProgressParam", szz);
				} else if (tsydm.equals("02")) {
					data.put("midProgressParam", szz);
				} else {
					data.put("lowProgressParam", szz);
				}
			} else if (dm.equals("02")) {
				if (tsydm.equals("01")) {
					data.put("lowBackwardParam", szz);
				} else if (tsydm.equals("02")) {
					data.put("midBackwardParam", szz);
				} else {
					data.put("highBackwardParam", szz);
				}
			} else {
				if (tsydm.equals("01")) {
					data.put("betterOthClaParam", szz);
				} else {
					data.put("worseOthClaParam", szz);
				}
			}
		}
		return data;
	}

	@Override
	public void updateStudentReportParam(JSONObject params) {
		List<SettingStu> list = new LinkedList<SettingStu>();

		String xxdm = params.getString("xxdm");
		int highProgressParam = params.getIntValue("highProgressParam");
		int midProgressParam = params.getIntValue("midProgressParam");
		int lowProgressParam = params.getIntValue("lowProgressParam");
		int lowBackwardParam = params.getIntValue("lowBackwardParam");
		int midBackwardParam = params.getIntValue("midBackwardParam");
		int highBackwardParam = params.getIntValue("highBackwardParam");
		int betterOthClaParam = params.getIntValue("betterOthClaParam");
		int worseOthClaParam = params.getIntValue("worseOthClaParam");
		// 进步
		SettingStu stu = new SettingStu();
		stu.setXxdm(xxdm);
		stu.setDm("01");
		stu.setTsydm("01");
		stu.setSzz(highProgressParam);
		list.add(stu);

		stu = new SettingStu();
		stu.setXxdm(xxdm);
		stu.setDm("01");
		stu.setTsydm("02");
		stu.setSzz(midProgressParam);
		list.add(stu);

		stu = new SettingStu();
		stu.setXxdm(xxdm);
		stu.setDm("01");
		stu.setTsydm("03");
		stu.setSzz(lowProgressParam);
		list.add(stu);

		stu = new SettingStu();
		stu.setXxdm(xxdm);
		stu.setDm("02");
		stu.setTsydm("01");
		stu.setSzz(lowBackwardParam);
		list.add(stu);
		// 退步
		stu = new SettingStu();
		stu.setXxdm(xxdm);
		stu.setDm("02");
		stu.setTsydm("02");
		stu.setSzz(midBackwardParam);
		list.add(stu);

		stu = new SettingStu();
		stu.setXxdm(xxdm);
		stu.setDm("02");
		stu.setTsydm("03");
		stu.setSzz(highBackwardParam);
		list.add(stu);

		stu = new SettingStu();
		stu.setXxdm(xxdm);
		stu.setDm("03");
		stu.setTsydm("01");
		stu.setSzz(betterOthClaParam);
		list.add(stu);

		// 年级比较
		stu = new SettingStu();
		stu.setXxdm(xxdm);
		stu.setDm("03");
		stu.setTsydm("03");
		stu.setSzz(worseOthClaParam);
		list.add(stu);

		scoreDao.insertStudentReportParamBatch(list);
	}

	@Override
	public List<JSONObject> getCompStdList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		Long schoolId = params.getLong("xxdm");

		String kmdms = params.getString("kmdms");
		params.remove("kmdms");
		params.put("kmdmList", StringUtil.convertToListFromStr(kmdms, ",", String.class));

		List<CompetitionStu> list = scoreDao.getCompetitionStu(xnxq, autoIncr, params);
		Set<Long> subjectIds = new HashSet<Long>();
		Set<Long> accIds = new HashSet<Long>();
		for (CompetitionStu stu : list) {
			subjectIds.add(Long.valueOf(stu.getKmdm()));
			accIds.add(Long.valueOf(stu.getXh()));
		}

		List<LessonInfo> kmList = allCommonDataService.getLessonInfoBatch(schoolId, new ArrayList<Long>(subjectIds),
				xnxq);
		Map<String, String> kmdm2McMap = new HashMap<String, String>();
		for (LessonInfo lessonInfo : kmList) {
			kmdm2McMap.put(String.valueOf(lessonInfo.getId()), lessonInfo.getName());
		}

		List<Account> accountList = allCommonDataService.getAccountBatch(schoolId, new ArrayList<Long>(accIds), xnxq);

		Set<Long> classIds = new HashSet<Long>();
		Map<String, JSONObject> accId2JsonMap = new HashMap<String, JSONObject>();
		for (Account acc : accountList) {
			if (acc.getUsers() == null) {
				continue;
			}

			for (User u : acc.getUsers()) {
				if (!T_Role.Student.equals(u.getUserPart().getRole()) || u.getStudentPart() == null) {
					continue;
				}
				StudentPart studentPart = u.getStudentPart();
				long classId = studentPart.getClassId();

				classIds.add(classId);

				JSONObject obj = new JSONObject();
				obj.put("stuName", acc.getName());
				obj.put("schoolNum",
						null == u.getStudentPart().getSchoolNumber() ? "" : u.getStudentPart().getSchoolNumber());
				obj.put("classId", classId);
				accId2JsonMap.put(String.valueOf(acc.getId()), obj);
				break;
			}
		}

		List<Classroom> classList = allCommonDataService.getClassroomBatch(schoolId, new ArrayList<Long>(classIds),
				xnxq);
		Map<Long, String> classId2NameMap = new HashMap<Long, String>();
		for (Classroom c : classList) {
			classId2NameMap.put(c.getId(), c.getClassName());
		}

		List<JSONObject> data = new ArrayList<JSONObject>(list.size());
		// 需从深圳得到科目名称、学生姓名、班级名称
		for (CompetitionStu stu : list) {
			JSONObject item = new JSONObject();
			String xh = stu.getXh();
			String kmdm = stu.getKmdm();

			item.put("xh", xh);
			item.put("kmdm", kmdm);

			JSONObject json = accId2JsonMap.get(xh);
			Long classId = json.getLong("classId");
			String stuName = json.getString("stuName");
			String schoolNum = json.getString("schoolNum");

			item.put("bh", classId);
			item.put("studentName", stuName);
			item.put("studentNum", schoolNum); // 学号
			item.put("className", classId2NameMap.get(classId));
			item.put("staticSubject", kmdm2McMap.get(kmdm));

			data.add(item);
		}

		return data;
	}

	@Override
	public void insertCompStuBatch(JSONObject params, List<CompetitionStu> list) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		scoreDao.deleteCompetitionStud(xnxq, autoIncr, params);
		scoreDao.insertCompetitionStuBatch(xnxq, autoIncr, list);
	}

	@Override
	public List<Long> getExamSubjectIdList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1001, "成绩信息已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		return scoreDao.getExamSubjectIdList(xnxq, autoIncr, params);
	}

	@Override
	public void delCompStdListBatch(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1001, "成绩信息已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		JSONArray stdList = params.getJSONArray("stdList");
		if (stdList.size() == 0) {
			return;
		}

		for (Object obj : stdList) {
			JSONObject json = (JSONObject) obj;
			String xh = json.getString("studentNum");
			String kmdm = json.getString("kmdm");
			params.put("xh", xh);
			params.put("kmdm", kmdm);
			scoreDao.deleteCompetitionStud(xnxq, autoIncr, params);
		}
	}

	/**
	 * 调用远程接口
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	@Override
	public String updateHttpRemoteInterface(String url, Map<String, Object> param) {
		CloseableHttpClient client = HttpClients.createDefault();
		String reponseResult = "";

		HttpPost post = new HttpPost(url);

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (String key : param.keySet()) {
			pairs.add(new BasicNameValuePair(key, StringUtil.transformString(param.get(key))));
		}

		try {
			UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(pairs, "UTF-8");
			post.setEntity(urlEntity);
			CloseableHttpResponse response = client.execute(post);
			HttpEntity responseEntity = response.getEntity();
			reponseResult = EntityUtils.toString(responseEntity);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return reponseResult;
	}

	@Override
	public List<Long> getClassIdListByGroupId(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		return scoreDao.getClassIdListByGroupId(xnxq, autoIncr, params);
	}

	@Override
	public int getStaticSelectedSetting(JSONObject params) {
		String xnxq = params.getString("xnxq");

		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		int way = 1;

		if (scoreDao.getAsgGroupSetFlag(xnxq, autoIncr, params)) {
			way = 1;
		} // 按照文理分组设置数量

		if (scoreDao.getClassGroupSetFlag(xnxq, autoIncr, params)) {
			way = 2;
		} // 按照班级设置不为空记录数量

		return way;
	}

	/**
	 * 提供新高考志愿填报使用 参数：studentId，schoolId 返回map=key：examid value：list
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public Map<String, List<JSONObject>> getScoreStuBZF(Map<String, Object> map) {
		if (map.get("studentId") == null) {
			throw new CommonRunException(-1, "参数异常，无法获取到学生的账户代码！");
		}
		if (map.get("schoolId") == null) {
			throw new CommonRunException(-1, "参数异常，无法获取到学校代码！");
		}
		if (map.get("termInfoId") == null) {
			throw new CommonRunException(-1, "参数异常，无法获取到学年学期代码！");
		}
		Long accId = Long.valueOf(map.get("studentId").toString());
		Long schoolId = Long.valueOf(map.get("schoolId").toString());
		String termInfoId = map.get("termInfoId").toString();

		List<String> termInfoIds = TermInfoIdUtils.getUserAllTermInfoIdsByAccId(allCommonDataService, schoolId, accId,
				termInfoId);

		List<JSONObject> list = new ArrayList<JSONObject>();
		map.put("xxdm", String.valueOf(schoolId));
		map.put("xh", String.valueOf(accId));
		for (String xnxq : termInfoIds) {
			map.put("xnxq", xnxq);
			List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(xnxq, map);
			for (DegreeInfo degreeInfo : degreeInfoList) {
				Integer autoIncr = degreeInfo.getAutoIncr();
				String kslc = degreeInfo.getKslcdm();
				map.put("kslc", kslc);
				list.addAll(scoreDao.getScoreStuBZF(xnxq, autoIncr, map));
			}
		}

		Map<String, List<JSONObject>> datamap = new HashMap<String, List<JSONObject>>();
		for (JSONObject o : list) {
			String kslc = o.getString("kslc");
			if (!datamap.containsKey(kslc)) {
				datamap.put(kslc, new ArrayList<JSONObject>());
			}
			datamap.get(kslc).add(o);
		}

		logger.info(JSONObject.toJSONString(datamap));
		return datamap;
	}

	@Override
	public JSONObject getAllScoreList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		Object xnxq = map.get("xnxq");
		String rtMsg = "返回成功！";
		try {
			String termInfoId = null;
			Integer autoIncr = null;
			
			try {
				logger.info("查询成绩分库分表：\n");
				logger.info("termInfo:"+xnxq+"map:"+map.toString());
				DegreeInfo degreeInfo = scoreDao.getDegreeInfoById((String)xnxq, map);
				
				termInfoId = degreeInfo.getXxdm();
				autoIncr = degreeInfo.getAutoIncr();
			} catch (Exception e) {
				rtMsg = "查询成绩分库分表出错！";
				throw new Exception();
			}
			
			try{
				logger.info("查询学生成绩列表：\n");
				logger.info("termInfoId:"+termInfoId+"autoIncr:"+autoIncr+"map:"+map.toString());				
				List<JSONObject> data = scoreDao.getStuScore(termInfoId, autoIncr, map);
				
				
				
			}catch (Exception e) {
				rtMsg = "查询成绩列表出错！";	
				throw new Exception();
			}
		} catch (Exception e) {
			logger.info(rtMsg);
		}
		
		return result;
	}

	@Override
	public JSONObject getStuScoreDetail(Map<String, Object> map) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		
		return result;
	}

	@Override
	public List<Long> getSynthscoreSubject(Map<String,Object> map) {
		String termInfoId = map.get("termInfoId").toString();
		List<Long> data = scoreDao.getSynthscoreSubject(termInfoId, null, map);
		return data;
	}
	
	
	@Override
	public JSONObject getDegreeinfoRelate(Map<String, Object> map) {
		String termInfoId = map.get("xnxq").toString();
		return scoreDao.getDegreeinfoRelate(termInfoId, null, map);
	}

	@Override
	public int deleteDegreeinfoRelate(Map<String, Object> map) {
		String termInfoId = map.get("xnxq").toString();
		return scoreDao.deleteDegreeinfoRelate(termInfoId , null,map);
	}

	@Override
	public int insertDegreeinfoRelate(Map<String, Object> map) {
		String termInfoId = map.get("xnxq").toString();
		return scoreDao.insertDegreeinfoRelate(termInfoId , null,map);
	}

	@Override
	public int insertDegreeinfoError(String termInfoId , List<JSONObject> list) {
		return scoreDao.insertDegreeinfoError(termInfoId , null , list);
	}
	
	@Override
	public void sendWXMsg(DegreeInfo egreeInfo,List<Long> ids,String examTypeName,int examType,String role) {
		logger.info("sendWXMsg:");
		
		JSONObject json = new JSONObject();
		json.put("studentIds", ids);
		json.put("termInfoId", egreeInfo.getXnxq());
		
		List<JSONObject> accountIdList = curCommonDataService.getAccountByIds(json);
		if(accountIdList == null | accountIdList.size() == 0) {
			logger.error("accountIdList获取失败");
			return;
		}
		
		// 组装接收者信息
		JSONArray receivers = new JSONArray();
		for(JSONObject user : accountIdList) {
			JSONObject msgCenterReceiver = new JSONObject();
			msgCenterReceiver.put("userId", user.getString("extId"));
			msgCenterReceiver.put("userName", user.getString("name"));
			receivers.add(msgCenterReceiver);
		}
		
		try {
			// School Info
			School school = allCommonDataService.getSchoolById(Long.parseLong(egreeInfo.getXxdm()), egreeInfo.getXnxq());
			if(null == school)
				return;
			
			// 家长信息
			if("Parent".equals(role)) {
				// 获取学生所有家长信息
				List<JSONObject> result = curCommonService.getSimpleParentByStuMsg(ids, egreeInfo.getXnxq(),school.getId());
				if(result != null && !result.isEmpty()) {
					
					for(JSONObject obj : result) {
						JSONObject  receive= new JSONObject();
						receive.put("userId", obj.getString("extUserId"));
						receive.put("userName",obj.getString("name"));
						
						receivers.add(receive); 
					}
				}
			}
			
			String examId = egreeInfo.getEexamId();
			if(StringUtils.isBlank(examId))
				 examId = egreeInfo.getKslcdm();
			
			// 4. 发送消息
			JSONObject msg = new JSONObject();
		    String msgId = UUIDUtil.getUUID().replace("-", "");
			msg.put("msgId", msgId);
			msg.put("msgTitle", "你收到一份新的成绩分析报告！");
			msg.put("msgContent", "你收到一份新的成绩分析报告!");
			msg.put("msgUrlPc", "" + msgUrlPc);
			msg.put("msgUrlApp", String.format(msgUrlApp, examType,examId,egreeInfo.getXnxq(), role)); 
			msg.put("msgOrigin", "成绩分析报告提醒");
			msg.put("msgTypeCode", MSG_TYPE_CODE);
			msg.put("msgTemplateType", MSG_TYPE_CODE);
			msg.put("schoolId", school.getExtId());
			msg.put("creatorName", "");
		    
			// 标题
			JSONObject first = new JSONObject();
			first.put("value","你收到一份新的成绩分析报告！");

			// 考试名称
			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", egreeInfo.getKslcmc());
			
			// 考试类型：
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", examTypeName);
			// 详情按钮提示
			JSONObject remark = new JSONObject();
			remark.put("value", "点击此消息查看详情！");
			
			JSONObject data = new JSONObject();
			data.put("first", first);
			data.put("keyword1", keyword1);
			data.put("keyword2", keyword2);
			data.put("remark", remark);
			data.put("url", String.format(msgUrlApp, examType,examId,egreeInfo.getXnxq(),role) );
			msg.put("msgWxJson", data);
			
			JSONObject msgBody = new JSONObject();
			msgBody.put("msg", msg);
			msgBody.put("receivers", receivers); 
			logger.info("msgBody=====>" + msgBody.toString());
			
			KafkaWXmsgThread kafka = new KafkaWXmsgThread(kafkaUrl, msgId, msgBody, MSG_TYPE_CODE, clientId, clientSecret);
			kafka.start();
		} catch (Exception e) {
		}
	}
	
	@Override
	public void sendWxMsgLimitCount(DegreeInfo egreeInfo,List<JSONObject> receivers,int examType,String examTypeName,String role) {
		try {
			// School Info
			School school = allCommonDataService.getSchoolById(Long.parseLong(egreeInfo.getXxdm()), egreeInfo.getXnxq());
//			logger.info("school："+school);
			if(null == school)
				return;
			
			String examId = egreeInfo.getEexamId();
			if(StringUtils.isBlank(examId)) {
				examId = egreeInfo.getKslcdm();
			}
			
			// 4. 发送消息
			JSONObject msg = new JSONObject();
		    String msgId = UUIDUtil.getUUID().replace("-", "");
			msg.put("msgId", msgId);
			msg.put("msgTitle", "你收到一份新的成绩分析报告！");
			msg.put("msgContent", "你收到一份新的成绩分析报告!");
			msg.put("msgUrlPc", "" + msgUrlPc);
			msg.put("msgUrlApp", String.format(msgUrlApp, examType,examId,egreeInfo.getXnxq(),role)); // &type=?&examid=?&termInfoId=?
			msg.put("msgOrigin", "成绩分析报告提醒");
			msg.put("msgTypeCode", MSG_TYPE_CODE);
			msg.put("msgTemplateType", MSG_TYPE_CODE);
			msg.put("schoolId", school.getExtId());
			msg.put("creatorName", "");
		    
			// 标题
			JSONObject first = new JSONObject();
			first.put("value","你收到一份新的成绩分析报告！");

			// 考试名称
			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", egreeInfo.getKslcmc());
			
			// 考试类型：
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", examTypeName);
			
			// 详情按钮提示
			JSONObject remark = new JSONObject();
			remark.put("value", "点击此消息查看详情！");

			JSONObject data = new JSONObject();
			data.put("first", first);
			data.put("keyword1", keyword1);
			data.put("keyword2", keyword2);
			data.put("remark", remark);
			data.put("url", String.format(msgUrlApp, examType,examId,egreeInfo.getXnxq(),role) );
			msg.put("msgWxJson", data);
			
			JSONObject msgBody = new JSONObject();
			msgBody.put("msg", msg);
			msgBody.put("receivers", receivers); 
			logger.info("msgBody=====>" + msgBody.toString());
			
			KafkaWXmsgThread kafka = new KafkaWXmsgThread(kafkaUrl, msgId, msgBody, MSG_TYPE_CODE, clientId, clientSecret);
			kafka.start();
		} catch (Exception e) {
		}
	}

	@Override
	public void updatetDegreeInfo(String xxdm, String kslcdm, String xnxq,int counter) {
		JSONObject params = new JSONObject();
		params.put("xnxq", xnxq);
		params.put("kslcdm", kslcdm);
		params.put("xxdm", xxdm);
		params.put("counter", counter);
		
		DegreeInfo degreeInfo = new DegreeInfo();
		degreeInfo.setKslcdm(kslcdm);
		degreeInfo.setXxdm(xxdm);
		degreeInfo.setXnxq(xnxq);
		degreeInfo.setCounter(counter);
		scoreDao.updatetDegreeInfo(xnxq, degreeInfo);
	}
}
