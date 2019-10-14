package com.talkweb.placementtask.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.base.common.PlacementConstant.PlacementType;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.BeanTool;
import com.talkweb.common.tools.ListSortUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.CurCommonDataService;
import com.talkweb.placementtask.dao.DezyPlacementTaskDao;
import com.talkweb.placementtask.dao.PlacementTaskDao;
import com.talkweb.placementtask.domain.ClassInfo;
import com.talkweb.placementtask.domain.DezyDisTclassParams;
import com.talkweb.placementtask.domain.DezyDisTclassRunParams;
import com.talkweb.placementtask.domain.OpenClassInfo;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.TPlDezyAdvancedOpt;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezyClassgroup;
import com.talkweb.placementtask.domain.TPlDezySettings;
import com.talkweb.placementtask.domain.TPlDezySubjectSet;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezySubjectgroup;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.domain.TPlDzbClassLevel;
import com.talkweb.placementtask.service.DezyPlacementTaskService;
import com.talkweb.placementtask.service.PlacementTaskService;
import com.talkweb.placementtask.utils.DezyGenClassAlg;
import com.talkweb.placementtask.utils.DezyGenClassAlg.MapEntry;
import com.talkweb.placementtask.utils.TmpUtil;
import com.talkweb.placementtask.utils.div.dezy.DezyDivOperation;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.schedule.dao.ScheduleCommonDao;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.wishFilling.service.WishFillingThirdService;

@Service
public class DezyPlacementTaskServiceImpl implements DezyPlacementTaskService {
	Logger logger = LoggerFactory.getLogger(DezyPlacementTaskServiceImpl.class);

	// private Executor executor =
	@Value("#{configProperties['db_name_placementtask']}")
	private String prefixSchema;

	@Value("#{settings['placement.alg']}")
	private String placementAlgType;

	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private CurCommonDataService curDataService;

	@Autowired
	private DezyPlacementTaskDao dezyPlacementTaskDao;

	@Autowired
	private PlacementTaskDao placementTaskDao;

	@Autowired
	private PlacementTaskService placementTaskService;

	@Autowired
	private WishFillingThirdService wishFillingThirdService;

	@Resource
	private ScheduleCommonDao scheduleCommonDao;
	
	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	private ExecutorService executor = Executors.newFixedThreadPool(10);

	private final Map<T_GradeLevel, String> njName = Collections
			.unmodifiableMap(AccountStructConstants.T_GradeLevelName);

	@Override
	/**
	 * result = 3 正在排课
	 */
	public int insertDezyPreSettings(JSONObject obj) {
		// TODO Auto-generated method stub

		String placementId = obj.getString("placementId");
		String progressMapKey = "dezy." + placementId + ".progressMap";
		try {
			Object progressMapObj = redisOperationDAO.get(progressMapKey);
			if (null != progressMapObj) {
				JSONObject progressJSON = (JSONObject)progressMapObj;
				if(progressJSON.getFloat("progress")!=100){
					return 3;
				}else{
					redisOperationDAO.del(progressMapKey);
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		if(StringUtils.isNotEmpty(placementId)){
			String oldPlacementId = scheduleCommonDao.selectPlacementIdInSchedule(placementId);
			if(!StringUtils.isEmpty(oldPlacementId)){
				return 2;
			}
		}
		
		
		obj.put("placementAlgType", placementAlgType);

		String wfId = obj.getString("wfId");
		String wfTermInfo = obj.getString("termInfo");
		Long schoolId = obj.getLong("schoolId");
		String usedGrade = obj.getString("useGrade");
		if (StringUtils.isEmpty(usedGrade)) {
			PlacementTask pl = placementTaskDao.queryPlacementTaskById(obj);
			usedGrade = pl.getUsedGrade();
		}
		obj.put("usedGrade", usedGrade);
		Map<String, Object> settingParams = new HashMap<String, Object>();

		// 获取学生志愿及人数
		
		try {
			// int rePlacement = obj.getInteger("rePlacement");
			// 不重新分班,获取最新一次的数据

			List<JSONObject> zhData = wishFillingThirdService.getZhStudentNumToThird(wfId, wfTermInfo, schoolId);
			settingParams.put("wishings", zhData);
			settingParams.put("reqParams", obj);

			// UUID.randomUUID().toString();
			if (zhData.size() == 0) {
				setDivProc(placementId, "100", "分班失败（学生没有选考）！", -1);
				return -1;
			}
			short isTechExist = -1;
			int totalStuInClass = 0;
			String techId = getTechId(obj);

			for (JSONObject wishing : zhData) {
				String subjectIds = wishing.getString("subjectIds");
				totalStuInClass += wishing.getIntValue("studentNum");
				if (techId != null && subjectIds.contains(techId)) {// 判断是否包含技术选科
					isTechExist = 1;
					// break;
				}
			}

			obj.put("isTechExist", isTechExist);

			// 设置班级阈值(及算法班级容量上下限参数)
			Integer classNum = obj.getIntValue("classNum");
			Integer maxClassNum = obj.getInteger("maxStuInClass");
			int classRoundNum = 5;
			// int classRoundNum = (maxClassNum -
			// classNum)/2;//obj.getIntValue("classRoundNum")>2?
			// obj.getIntValue("classRoundNum"):3;
			if (null == classNum || classNum == 0) {
				classNum = 1;
			}
			int maxStuInClass = totalStuInClass / classNum + classRoundNum;
			int minStuInClass = maxStuInClass - 2 * classRoundNum;
			long shredshold = Math.round(totalStuInClass / (double) classNum + 0.5);
			DezyGenClassAlg divAlg = new DezyGenClassAlg(maxStuInClass, minStuInClass, classRoundNum, shredshold);
			divAlg.setTargetDivClassNum(classNum);
			divAlg.setTechId(techId);

			obj.put("divAlg", divAlg);
			int wishingGroups = zhData.size();
			switch (wishingGroups / 10) {
			case 0: // 1-9组(放宽班级人数限制)
				if (wishingGroups <= 6) {
					// maxStuInClass+=classRoundNum;
					minStuInClass = 0;
				} else {
					maxStuInClass += classRoundNum;
					minStuInClass -= classRoundNum;
				}
				break;
			case 1:// 10-19
					// maxStuInClass-=classRoundNum;
				minStuInClass -= classRoundNum;
				break;
			}
			obj.put("thresholdStuInClass", maxStuInClass - classRoundNum);

			int isLearnExist = -1;
			// 学生选科数（肯定是3）* 选科中最多的学时+学生最多学科数(4/3?)*学科里面最多的学时，得到的值（A）
			// 和走班能安排的格子数（B）对比，A<=B的时候用高三的类型分班；A>B用高二第一学期的类型分班
			JSONArray subjectGroups = obj.getJSONArray("subjectGroup");
			int optLesson = 0, proLesson = 0;

			List<String> proLessons = new ArrayList<String>();
			for (Object object : subjectGroups) { // 两个科目组
				JSONObject subjectGroup = (JSONObject) object;
				optLesson = subjectGroup.getIntValue("optLesson") > optLesson ? subjectGroup.getIntValue("optLesson")
						: optLesson;
				for (Object o : subjectGroup.getJSONArray("subject")) {
					JSONObject subject = (JSONObject) (o);
					if (1 == subject.getIntValue("isProExist")) {
						proLesson = subjectGroup.getIntValue("proLesson") > proLesson
								? subjectGroup.getIntValue("proLesson") : proLesson;
						isLearnExist = 1;
						proLessons.add(subject.getString("subjectId"));
						// break;
					}
				}
			}
			obj.put("isLearnExist", isLearnExist);

			// 选出学生中最多的学考数
			int stuMaxProLesson = 0, preMax = 0;

			maxLabel: for (JSONObject wish : zhData) {
				String subjectIds = wish.getString("subjectIds");
				stuMaxProLesson = 0;
				for (String subId : subjectIds.split(",")) {
					if (proLessons.contains(subId)) {
						if (++stuMaxProLesson >= 3) {
							preMax = stuMaxProLesson;
							break maxLabel;
						}
					}

				}
				preMax = preMax >= stuMaxProLesson ? preMax : stuMaxProLesson;
			}

			// 1:不跨组方案；2：跨组方案
			int availableSpace = obj.getIntValue("gradeSumLesson") - (Integer) obj.get("fixedSumLesson");
			int maxOpt = 3 * optLesson;
			int maxPro = (preMax) * proLesson;
			int placeAlgMethod = ((maxOpt + maxPro) <= availableSpace) ? 2 : 1;
			obj.put("placeAlgMethod", placeAlgMethod);
			divAlg.setPlaceAlgMethod(Integer.parseInt(placementAlgType));
			// divAlg.setPlaceAlgMethod(placeAlgMethod);
			// obj.put("placementId", placementId);

			/*
			 * 填充部分公共数据
			 */
			// 加入分班设置实体-TPlDezySettings
			TPlDezySettings dezySetting = new TPlDezySettings();
			dezySetting.setClassNum(classNum);
			dezySetting.setMaxClassNum(obj.getIntValue("maxStuInClass"));
			dezySetting.setClassRoundNum(classRoundNum);
			dezySetting.setFixedSumLesson(obj.getIntValue("fixedSumLesson"));
			dezySetting.setGradeSumLesson(obj.getIntValue("gradeSumLesson"));
			dezySetting.setIsLearnExist(isLearnExist);
			dezySetting.setIsTechExist(isTechExist);
			dezySetting.setMinTeacherNum(obj.getIntValue("minTeacherNum"));
			dezySetting.setPlaceAlgMethod(placeAlgMethod);
			dezySetting.setPlacementId(placementId);
			dezySetting.setRePlacement(obj.getIntValue("rePlacement"));
			dezySetting.setSchoolId(obj.getString("schoolId"));
			dezySetting.setUsedGrade(usedGrade);
			dezySetting.setWfId(wfId);

			// 技术单独设置
			JSONObject techSettings = obj.getJSONObject("techSettings");
			int goClass=-1;
			if(null!=techSettings){							
				if(null==techSettings.getInteger("")){
					techSettings.put("goClass", -1);
				}
				goClass = techSettings.getIntValue("goClass");
				if (null != techSettings) {
					dezySetting.setFormOfTech(goClass);
					/**
					 * goClass:走班（0：走班，1：行政班开班） classNum: 选考班级数 isProExist:是否开设学考
					 */
					if (0 == goClass) {
						Map<String, String> courseMap = getSubjectNameMap(obj);
						techSettings.put("subjectId", techId);
						techSettings.put("subjectName", courseMap.get(techId));
						Integer techClsNum = techSettings.getInteger("classNum");
						if (null == techClsNum) {
							techClsNum = 0;
						}
						techSettings.put("classLimit", techClsNum);
					}
				}
			}else{
				//没有技术科目
				techSettings = new JSONObject();
				techSettings.put("goClass", -1);
				obj.put("techSettings", techSettings);
			}
			settingParams.put("dezySetting", dezySetting);

			// 加入科目组实体-TPlDezySubjectgroup
			List<TPlDezySubjectgroup> subjectGroupList = new ArrayList<TPlDezySubjectgroup>();
			List<TPlDezySubjectSet> subjectSetList = new ArrayList<TPlDezySubjectSet>();
			JSONArray advancedOptions = obj.getJSONArray("advancedOptions");
			// 选考
			Map<String, Integer> subClassNumMap = new HashMap<String, Integer>();
			Map<String, Integer> subStuNumMap = new HashMap<String, Integer>();
			// 学考
			Map<String, Integer> subProClassNumMap = new HashMap<String, Integer>();
			Map<String, Integer> subProStuNumMap = new HashMap<String, Integer>();
			// 老师
			Map<String, Integer> subTeacherNumMap = new HashMap<String, Integer>();

			if (null != advancedOptions) {
				for (Object tmp : advancedOptions) {
					JSONObject subSet = (JSONObject) tmp;
					int subClassNum = -1, proClassNum = -1, teacherNum = -1;
					if (subSet.getInteger("classNum") != null) {
						subClassNum = subSet.getIntValue("classNum");
					}
					if (subSet.getInteger("proClassNum") != null) {
						proClassNum = subSet.getInteger("proClassNum");
					}
					if (subSet.getInteger("teacherNum") != null) {
						teacherNum = subSet.getInteger("teacherNum");
					}

					subClassNumMap.put(subSet.getString("subjectId"), subClassNum);
					subStuNumMap.put(subSet.getString("subjectId"), subSet.getInteger("stuNum"));

					subProClassNumMap.put(subSet.getString("subjectId"), proClassNum);
					subProStuNumMap.put(subSet.getString("subjectId"), subSet.getInteger("stuNumOfPro"));

					subTeacherNumMap.put(subSet.getString("subjectId"), teacherNum);
				}
			}
			int techStuNum = 0;
			for (JSONObject wishing : zhData) {
				String subjectIds = wishing.getString("subjectIds");
				if (null != subjectIds && Arrays.asList(subjectIds.split(",")).contains(techId)) {
					techStuNum += wishing.getIntValue("studentNum");
				}
			}
			techSettings.put("stuNum", techStuNum);

			for (Object group : obj.getJSONArray("subjectGroup")) {
				JSONObject subGroup = (JSONObject) group;
				String subjectGroupId = subGroup.getString(
						"subjectGroupId");/* UUID.randomUUID().toString(); */
				// 技术组
				if (subGroup.getJSONArray("subject").size() < 2) {
					// continue;
				}

				TPlDezySubjectgroup subjectGroup = new TPlDezySubjectgroup();
				if (StringUtils.isEmpty(subjectGroupId)) {
					subjectGroupId = UUID.randomUUID().toString();
				}
				subGroup.put("subjectGroupId", subjectGroupId);
				subjectGroup.subjectGroupId(subjectGroupId);
				// subjectGroup.setSubjectGroupId(subjectGroupId);
				proLesson = subGroup.getIntValue("proLesson");
				subjectGroup.setIsProExist(proLesson == 0 ? -1 : 1);
				subjectGroup.setGroupName(subGroup.getString("groupName"));
				subjectGroup.setPlacementId(placementId);
				subjectGroup.setOptLesson(subGroup.getIntValue("optLesson"));
				subjectGroup.setProLesson(subGroup.getIntValue("proLesson"));
				subjectGroup.setSchoolId(String.valueOf(schoolId));
				subjectGroup.setUsedGrade(usedGrade);
				subjectGroup.setSubjectIds(subGroup.getString("subjectIds"));

				if (null != subGroup.getString("subjectIds") && !techId.equals(subGroup.getString("subjectIds"))) {
					subjectGroupList.add(subjectGroup);
				}
				// 加入科目设置实体-TPlDezySubjectSet
				for (Object set : subGroup.getJSONArray("subject")) {
					JSONObject subjects = (JSONObject) set;
					TPlDezySubjectSet subjectSet = new TPlDezySubjectSet();
					subjectSet.setUsedGrade(usedGrade);
					String subjectId = subjects.getString("subjectId");

					// ++++++++++++++++++++++++ 10-20 deleted
					// ++++++++++++++++++++
					// 补充高级选项
					subjects.put("classLimit", subClassNumMap.get(subjectId));
					subjects.put("stuNum", subStuNumMap.get(subjectId));
					subjectSet.setSubjectGroupId(subjectGroupId);

					subjects.put("proClassLimit", subProClassNumMap.get(subjectId));
					subjects.put("stuNumOfPro", subProStuNumMap.get(subjectId));

					subjects.put("teacherNum", subTeacherNumMap.get(subjectId));
					// +++++++++++++++++++++++++++++ end +++++++++++++++++++++

					// ++++++++++++++++++++++++ 10-20 added ++++++++++++++++++++
					if (null != subjects.getInteger("classNum")) {
						subjectSet.setClassLimit(subjects.getIntValue("classNum"));
					}
					if (null != subjects.getInteger("proClassLimit")) {
						subjectSet.setProClassLimit(subjects.getIntValue("proClassLimit"));
					}
					if (null != subjects.getInteger("stuNumOfPro")) {
						subjectSet.setStuNumOfPro(subjects.getIntValue("stuNumOfPro"));
					}
					if (null != subjects.getInteger("stuNum")) {
						subjectSet.setStuNum(subjects.getIntValue("stuNum"));
					}
					if (null != subjects.getInteger("teacherNum")) {
						subjectSet.setTeacherNum(subjects.getIntValue("teacherNum"));
					}
					// +++++++++++++++++++++++++++++ end +++++++++++++++++++++

					subjectSet.setSubjectId(subjectId);
					int isProExist = subjects.getIntValue("isProExist");
					subjectSet.setIsProExist(isProExist);
					int proLessonOfSub = (isProExist == 0) ? isProExist : subGroup.getIntValue("proLesson");
					subjectSet.setOptLesson(subGroup.getIntValue("optLesson"));
					subjectSet.setProLesson(proLessonOfSub);
					subjectSet.setSubjectGroupId(subjectGroupId);
					subjectSet.setPlacementId(placementId);
					subjectSet.setSchoolId(String.valueOf(schoolId));

					// 高级设置项（科目下的教学班数）
					subjectSetList.add(subjectSet);
				}
			}
			settingParams.put("subjectGroupList", subjectGroupList);
			settingParams.put("subjectSetList", subjectSetList);

			//不重新分班的情况
			obj.put("queryClassType", "6");//行政班
			List<TPlDezyClass> dezyClassList = placementTaskDao.getDezyClassList(obj);
			if(0==obj.getIntValue("rePlacement") && CollectionUtils.isNotEmpty(dezyClassList)){
				settingParams.put("dezyClassList", dezyClassList);
				settingParams.put("subjectCompList", placementTaskDao.getSubjectcompList(obj));
				settingParams.put("tclassSubcompList", placementTaskDao.getTclassSubcompList(obj));
				settingParams.put("subjectCompStuList", placementTaskDao.getSubjectcompStuList(obj));
				settingParams.put("classGroupList", placementTaskDao.getDezyClassGroupList(obj));				
			}
			//清除上次数据
			String plcId = obj.getString("placementId");
			String termInfo = obj.getString("termInfo");
			placementTaskService.updateAllDezyResult(String.valueOf(schoolId),plcId,termInfo);			
			
			try {
				obj.put("classRoundNum", classRoundNum);
				obj.put("avgStuInClass", maxStuInClass-classRoundNum);
				obj.put("minTeacherNum", -1);
				//obj.put("formOfTech", -1);
				
				dezyPlacementTaskDao.insertDezyPreSetting(obj);
				dezyPlacementTaskDao.insertDezySubjectGroup(obj);

				// 技术
				if (goClass == 0) {
					TPlDezySubjectSet techSet = new TPlDezySubjectSet();

					JSONArray subGrps = obj.getJSONArray("subjectGroup");
					JSONObject subGrp = new JSONObject();
					subGrp.put("subjectGroupId", techId);
					// (#{placementId},#{schoolId},#{usedGrade},'${group.subjectGroupId}',${item.subjectId},
					// ${group.optLesson},${group.proLesson},${item.isProExist},${item.classLimit},${item.stuNum})
					JSONArray subjects = new JSONArray();
					JSONObject subSettings = new JSONObject();
					for (String key : techSettings.keySet()) {
						subSettings.put(new String(key), new String(techSettings.getString(key)));
					}
					subSettings.put("proClassLimit",
							/* subProClassNumMap.get(techId) */"-1");
					subSettings.put("stuNumOfPro",
							/* subProStuNumMap.get(techId) */"-1");

					subSettings.put("teacherNum",
							/* subTeacherNumMap.get(techId) */"-1");

					subjects.add(subSettings);
					subGrp.put("subject", subjects);
					subGrp.put("proLesson", "-1");
					subGrp.put("optLesson", "-1");
					subGrp.put("groupName", "技术");
					subGrp.put("subjectIds", techId);
					subGrp.put("isProExist", "1");

					// 外传接口
					techSet.setSubjectGroupId("-999");
					techSet.setPlacementId(placementId);
					techSet.setSchoolId(String.valueOf(schoolId));
					techSet.setUsedGrade(usedGrade);
					techSet.setSubjectId(techId);
					Integer techClsNum = techSettings.getInteger("classNum") == null ? 0
							: techSettings.getInteger("classNum");
					techSet.setClassLimit(techClsNum);
					techSet.setIsProExist(techSettings.getIntValue("isProExist"));
					techSet.setOptLesson(-1);
					techSet.setStuNum(techSettings.getIntValue("stuNum"));
					techSet.setProLesson(0);

					subjectSetList.add(techSet);
					subGrps.add(subGrp);

					dezyPlacementTaskDao.insertDezySubjectSet(obj);
					// 还原
					subGrps.remove(subGrp);
				} else {
					dezyPlacementTaskDao.insertDezySubjectSet(obj);
				}

				// 实验班高级设置
				JSONArray expClassSettings = obj.getJSONArray("expClassSettings");
				if (!CollectionUtils.isEmpty(expClassSettings)) {
					List<TPlDezyAdvancedOpt> dataList = new ArrayList<TPlDezyAdvancedOpt>();
					int clsNum = 1;
					for (Object object : expClassSettings) {
						TPlDezyAdvancedOpt setting = new TPlDezyAdvancedOpt();
						JSONObject expClassSetting = (JSONObject) object;
						setting.setClassId(expClassSetting.getString("classId"));
						setting.setClassName(expClassSetting.getString("className"));
						setting.setExpClassId(UUID.randomUUID().toString());
						setting.setExpClassName("实验" + (clsNum++) + "班");
						setting.setSubjectIds(expClassSetting.getString("subjectIds"));
						setting.setPlacementId(placementId);
						setting.setUsedGrade(usedGrade);
						setting.setSchoolId(String.valueOf(schoolId));

						dataList.add(setting);
					}
					obj.put("DezyAdvancedOptList", dataList);
					dezyPlacementTaskDao.updateBatchInsertEntity(obj.getString("termInfo"), dataList);
				}

			} catch (Exception e) {
				e.printStackTrace();
				// 设置进度,保存进redis(设置入库完成)
				setDivProc(placementId, "100", "初始化设置失败！", -1);
				return -1;
			}

			// 设置进度,保存进redis(设置入库完成)
			setDivProc(placementId, "10", "设置完成->开始分班！", 0);
			
			// 启用分班线程
			executor.submit(new GenDezyClassPrc(settingParams));
			System.out.println("\n 分班任务已提交!");
		} catch (Exception e) {
			e.printStackTrace();
			// 设置进度,保存进redis(设置入库完成)
			setDivProc(placementId, "100", "初始化设置失败！", -1);
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	@Override
	public JSONObject getDezyPreSettings(JSONObject obj) {
		// TODO Auto-generated method stub
		JSONObject result = null;
		try {
			// obj.put("termInfo", obj.get("termInfoId"));
			result = dezyPlacementTaskDao.queryDezyPreSetting(obj);
			int code = 0;
			String msg = "返回设置成功！";
			Map<String, String> courseMap = getSubjectNameMap(obj);
			
			
			
			// 获取原行政班
			String termInfoId = obj.getString("termInfo");
			/*
			 * School sch =
			 * commonDataService.getSchoolById(obj.getLongValue("schoolId"),
			 * termInfoId); List<Classroom> classroomList =
			 * commonDataService.getSimpleClassList(sch, null, termInfoId);
			 */
			// 原行政班中的实验班
			List<Long> classIds = new ArrayList<Long>();
			String techId = null;
			if (null == result || result.size() == 0) {				
				PlacementTask pl = placementTaskDao.queryPlacementTaskById(obj);
				String gradeId = pl.getUsedGrade();

				result = new JSONObject();
				result.put("usedGrade", gradeId);
			
				//七选三标志位
				result.put("hasTech", 0);
				
				// 设置默认科目组
				code = 2;
				JSONArray subGroups = new JSONArray();
				JSONObject subGroup1 = new JSONObject();
				subGroup1.put("subjectGroupId", UUID.randomUUID().toString());
				subGroup1.put("subjectGroupName", "物化生");

				JSONObject subGroup2 = new JSONObject();
				subGroup2.put("subjectGroupId", UUID.randomUUID().toString());
				subGroup2.put("subjectGroupName", "政史地");

				JSONArray subGroup1Detail = new JSONArray();
				JSONArray subGroup2Detail = new JSONArray();
				for (Entry<String, String> entry : courseMap.entrySet()) {
					switch (entry.getValue()) {
					case "物理":
						JSONObject subject1 = new JSONObject();
						subject1.put("subjectName", "物理");
						subject1.put("subjectId", entry.getKey());
						subGroup1Detail.add(subject1);
						break;
					case "化学":
						JSONObject subject2 = new JSONObject();
						subject2.put("subjectName", "化学");
						subject2.put("subjectId", entry.getKey());

						subGroup1Detail.add(subject2);
						break;
					case "生物":
						JSONObject subject3 = new JSONObject();
						subject3.put("subjectName", "生物");
						subject3.put("subjectId", entry.getKey());

						subGroup1Detail.add(subject3);
						break;
					case "政治":
						JSONObject subject4 = new JSONObject();
						subject4.put("subjectName", "政治");
						subject4.put("subjectId", entry.getKey());

						subGroup2Detail.add(subject4);
						break;
					case "地理":
						JSONObject subject5 = new JSONObject();
						subject5.put("subjectName", "地理");
						subject5.put("subjectId", entry.getKey());

						subGroup2Detail.add(subject5);
						break;
					case "历史":
						JSONObject subject6 = new JSONObject();
						subject6.put("subjectName", "历史");
						subject6.put("subjectId", entry.getKey());

						subGroup2Detail.add(subject6);
						break;
				/*	case "技术":
						result.put("hasTech", 1);*/
					default:
						continue;
					}

				}
				subGroup1.put("subject", subGroup1Detail);
				subGroup2.put("subject", subGroup2Detail);
				subGroups.add(subGroup1);
				subGroups.add(subGroup2);
				result.put("subjectGroup", subGroups);
			} else {// 补全科目名称
				//七选三标志位
				result.put("hasTech", 0);
				
				JSONArray subGrps = result.getJSONArray("subjectGroup");
				techId = getTechId(obj);
				Iterator<Object> subGrpsIter = subGrps.iterator();
				int isTechProExist = 0;
				while (subGrpsIter.hasNext()) {
					JSONObject subGroup = (JSONObject) subGrpsIter.next();
					JSONArray subjects = subGroup.getJSONArray("subject");

					Iterator<Object> subIter = subjects.iterator();
					while (subIter.hasNext()) {
						JSONObject subject = (JSONObject) subIter.next();
						String subjectId = subject.getString("subjectId");
						if (techId.equals(subjectId)) {
							isTechProExist = subject.getIntValue("isProExist");
							subIter.remove();
							
							//七选三标志位
							result.put("hasTech", 1);
						}
						subject.put("subjectName", courseMap.get(subjectId));
					}
					subGroup.put("subject", subjects);
				}

				// advancedOptions
				int techClsLimit = 0;
				JSONArray advancedOptions = result.getJSONArray("advancedOptions");
				Iterator<Object> advanceOptIter = advancedOptions.iterator();
				while (advanceOptIter.hasNext()) {
					JSONObject subOptions = (JSONObject) advanceOptIter.next();
					String subjectId = subOptions.getString("subjectId");
					subOptions.put("subjectName", courseMap.get(subjectId));

					Integer subClsLimit = subOptions.getInteger("classNum");
					if (null != subClsLimit && 0 == subClsLimit || subjectId.equals(techId)) {
						advanceOptIter.remove();
						if (subjectId.equals(techId)) {
							techClsLimit = subClsLimit;
						}
					}

				}
				result.put("advancedOptions", advancedOptions);

				// techSettings
				JSONObject techSettings = result.getJSONObject("techSettings");
				if (null != techSettings && 0 == techSettings.getIntValue("goClass")) {
					techSettings.put("isProExist", isTechProExist);
					techSettings.put("classNum", techClsLimit);
				} else {// 默认行政班开班
					techSettings = new JSONObject();
					techSettings.put("goClass", "1");
					result.put("techSettings", techSettings);
				}

				// expClassSettings
				List<TPlDezyAdvancedOpt> expClassSettingList = dezyPlacementTaskDao.queryDezyAdvancedOpt(obj);
				if (!CollectionUtils.isEmpty(expClassSettingList)) {
					JSONArray expClassSettings = new JSONArray();
					for (TPlDezyAdvancedOpt expClass : expClassSettingList) {
						JSONObject expClassSetting = new JSONObject();
						expClassSetting.put("classId", expClass.getClassId());
						classIds.add(Long.parseLong(expClass.getClassId()));

						expClassSetting.put("className", expClass.getClassName());
						expClassSetting.put("expClassId", expClass.getExpClassId());
						expClassSetting.put("expClassName", expClass.getExpClassName());
						String subjectIds = expClass.getSubjectIds();
						expClassSetting.put("subjectIds", subjectIds);
						expClassSetting.put("className", expClass.getClassName());
						expClassSetting.put("type", "1");
						StringBuffer sb = new StringBuffer();
						for (String subId : subjectIds.split(",")) {
							sb.append(courseMap.get(subId));
							sb.append(",");
						}
						expClassSetting.put("subjectNames", sb.substring(0, sb.length() - 1));
						expClassSettings.add(expClassSetting);
					}
					result.put("expClassSettings", expClassSettings);

				}
			}
			
			try {					
				List<JSONObject> wishings = wishFillingThirdService.getZhStudentNumToThird(result.getString("wfId"), termInfoId, obj.getLong("schoolId"));
				if(CollectionUtils.isNotEmpty(wishings)){
					for(JSONObject wishing : wishings){
						String subjectIds = wishing.getString("subjectIds");
						if(StringUtils.isNotBlank(techId) && StringUtils.isNotEmpty(subjectIds) && subjectIds.contains(techId)){
							result.put("hasTech", 1);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("查询志愿出错");
			}

			// 补全实验班高级设置
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", obj.getLong("schoolId"));
			map.put("termInfoId", termInfoId);
			map.put("usedGradeId ", result.getString("usedGrade"));
			map.put("classTypeId ", 0);
			String gLevel = commonDataService.ConvertSYNJ2NJDM(result.getString("usedGrade"),
					termInfoId.substring(0, 4));

			Grade useGrade = commonDataService.getGradeByGradeLevel(obj.getLong("schoolId"),
					T_GradeLevel.findByValue(Integer.parseInt(gLevel)), termInfoId);
			List<Classroom> classroomList = commonDataService.getSimpleClassBatch(obj.getLong("schoolId"), useGrade.classIds,
					termInfoId);
			if (!CollectionUtils.isEmpty(classroomList)) {
				JSONArray expClassSettings = result.getJSONArray("expClassSettings");
				// 第一次设置
				if (CollectionUtils.isEmpty(expClassSettings)) {
					expClassSettings = new JSONArray();
					result.put("expClassSettings", expClassSettings);
				}

				Grade gradenj = null;
				long curGradeId = 0l;
				// long preGradeId = 0l;
				for (Classroom clsRoom : classroomList) {
					long gradeId = clsRoom.getGradeId();
					if (curGradeId != 0l && curGradeId != gradeId) {
						continue;
					}
					if (curGradeId == 0l) {
						if (null == gradenj) {
							gradenj = commonDataService.getGradeById(obj.getLongValue("schoolId"), gradeId, termInfoId);
						}
						T_GradeLevel currentLevel = gradenj.getCurrentLevel();
						String grade = commonDataService.ConvertNJDM2SYNJ(String.valueOf(currentLevel.getValue()),
								termInfoId.substring(0, 4));
						if (gradenj != null && !grade.equals(result.getString("usedGrade"))) {
							gradenj = null;
							// preGradeId = gradeId;
							continue;
						} else {
							curGradeId = gradeId;
						}
					}
					// 剔选同年级原行政班
					if (!classIds.contains(clsRoom.getClassNo())) {
						JSONObject expClassSetting = new JSONObject();
						expClassSetting.put("classId", clsRoom.getId());
						expClassSetting.put("className", clsRoom.getClassName());
						expClassSetting.put("type", 0);
						expClassSettings.add(expClassSetting);
					}
				}
			}

			result.put("msg", msg);
			result.put("code", code);
			return result;
		} catch (Exception e) {
			result = new JSONObject();
			result.put("msg", "（数据库查询）内部错误！");
			result.put("code", -1);
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getClassInfo(String placementId, String schoolId, String gradeId) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		return result;
	}

	
	private int divFixTwoGoOneClass(Map<String, Object> settingParams) {
		
		String placementId = "";
		
		try {
			
			JSONObject reqParams = (JSONObject) settingParams.get("reqParams");
			
			Integer classNum = reqParams.getIntValue("classNum");
			
			Integer maxClassSize = reqParams.getInteger("maxStuInClass");
			
			String usedGrade = reqParams.getString("usedGrade");
			
			String schoolId = reqParams.getString("schoolId");
							
			String termInfo = reqParams.getString("termInfo");
			
			TPlDezySettings dezySettings = (TPlDezySettings) settingParams.get("dezySetting");
			
			placementId = dezySettings.getPlacementId();
			
			// 获取学生选科
			List<JSONObject> wishStudList = wishFillingThirdService.getZhStudentListToThird(reqParams.getString("wfId"),
					reqParams.getString("termInfo"), Long.valueOf(dezySettings.getSchoolId()));
			
			//Long globalAgvClassSize = Math.round(wishStudList.size()/(double) classNum + 0.5);
			
			List<Student> studentList = new ArrayList<>();
			for (JSONObject jsonObject : wishStudList) {
				String subjectIds = jsonObject.getString("subjectIds");
				String id = jsonObject.getString("accountId");
				String zhName = jsonObject.getString("zhName");
				String sids[] = subjectIds.split(",");
				TreeSet<String> subjectIdSet = new TreeSet<>();
				for (String subjectId : sids) {
					subjectIdSet.add(subjectId);
				}
				studentList.add(new Student(id, id,  Joiner.on(",").skipNulls().join(subjectIdSet), zhName));
			}
			List<ClassInfo> classList = DezyDivOperation.excuteDiv(maxClassSize, classNum, studentList);
			
			classList = placementTaskService.arrangeClassGroud(schoolId, termInfo, usedGrade, placementId, classList);
			
			
			placementTaskService.dataConversion(schoolId, termInfo, usedGrade, placementId, classList);
			
			setDivProc(placementId, "100", "完成分班！", 0);
			
		} catch(Exception e) {
			setDivProc(placementId, "100", "分班失败！", -1);
			e.printStackTrace();
		}

		return 0;
		
	}

	/**
	 * 
	 * @return 0-成功,1-行政班出错,2-行政班分组出错,3-调班出错,4-生成教学班出错
	 */
	@SuppressWarnings("unchecked")
	private int divClass(Map<String, Object> settingParams) {
		// TODO Auto-generated method stub
		TPlDezySettings dezySettings = (TPlDezySettings) settingParams.get("dezySetting");
		String placementId = dezySettings.getPlacementId();
		try {
			Map<String, Object> calcParams = new HashMap<String, Object>();
			List<JSONObject> wishingList = (List<JSONObject>) settingParams.get("wishings");
			JSONObject reqParams = (JSONObject) settingParams.get("reqParams");

			// 划分行政班
			List<Entry<String, Integer>> wishingMapList = new ArrayList<Map.Entry<String, Integer>>();
			DezyGenClassAlg divAlg = (DezyGenClassAlg) reqParams.get("divAlg");

			if (null == divAlg) {
				logger.info("分班算法设置失败,请重新分班！");
				return -1;
			}
			wishingMapList = divAlg.genWishingList(wishingList, getSubjectNameMap(reqParams),
					getSubjectNameMap(reqParams, true));
			divAlg.sort(wishingMapList, true);
			Map<String, Integer> overNumbersWishings = divAlg.getClassWisthSingleWishings(wishingMapList);

			// 行政班总数
			int classNum = 0;
			List<Map<String, Integer>> commonGroupWishings = null;
			List<Map<String, Integer>> expClsWishings = new ArrayList<Map<String, Integer>>();

			// 获取学生选科
			List<JSONObject> wishStudList = wishFillingThirdService.getZhStudentListToThird(reqParams.getString("wfId"),
					reqParams.getString("termInfo"), Long.valueOf(dezySettings.getSchoolId()));

			// 是否重新分班
			Integer rePlacement = reqParams.getInteger("rePlacement");

			Map<Long, String> clsIdNameMap = new HashMap<Long, String>();
			// 原行政班与志愿映射关系
			Map<Long, Map<String, Integer>> classIdClsMap = new HashMap<Long, Map<String, Integer>>();
			Map<Long, Map<String, Integer>> classIdWishingMap = new HashMap<Long, Map<String, Integer>>();

			for (JSONObject wishStud : wishStudList) {
				Long classId = wishStud.getLong("classId");
				Map<String, Integer> cls = classIdClsMap.get(classId);
				Map<String, Integer> wishingGrp = classIdWishingMap.get(classId);
				if (cls == null) {
					cls = new HashMap<String, Integer>();
					wishingGrp = new HashMap<String, Integer>();

					classIdClsMap.put(classId, cls);
					classIdWishingMap.put(classId, wishingGrp);
				}
				String subjectIds = wishStud.getString("subjectIds");
				String wishingId = divAlg.getWishingIdBySubjectIds(subjectIds);
				int preStuNum = (null == cls.get(wishingId)) ? 0 : cls.get(wishingId);

				cls.put(wishingId, ++preStuNum);
				wishingGrp.put(subjectIds, preStuNum);
			}
			
			// 填充行政班部分数据(沿用基础数据库行政班)
			List<TPlDezyClass> dezyClassList = (List<TPlDezyClass>) settingParams.get("dezyClassList");//placementTaskDao.getDezyClassList(reqParams);
			boolean useOriClass = false;
			if (null != rePlacement && 0 == rePlacement && CollectionUtils.isEmpty(dezyClassList)) {
				//沿用原行政班			
				useOriClass = true;
				List<Long> clsIds = new ArrayList<Long>();
				for (Long clsId : classIdClsMap.keySet()) {
					clsIds.add(clsId);
				}
				List<Classroom> clsRoomList = commonDataService.getSimpleClassBatch(reqParams.getLong("schoolId"),
						clsIds, reqParams.getString("termInfo"));
				for (Classroom cr : clsRoomList) {
					clsIdNameMap.put(cr.getId(), cr.getClassName());
				}
				commonGroupWishings = new ArrayList<Map<String, Integer>>();
				commonGroupWishings.addAll(classIdClsMap.values());
			} else if(rePlacement==1){
				// 实验班
				List<TPlDezyAdvancedOpt> dezyAdvancedOptList = (List<TPlDezyAdvancedOpt>) reqParams
						.get("DezyAdvancedOptList");
				if (null != dezyAdvancedOptList) {
					List<Entry<String, Map<String, Integer>>> expSubWishingList = new ArrayList<Entry<String, Map<String, Integer>>>();

					JSONArray expClassSettings = reqParams.getJSONArray("expClassSettings");
					for (Object obj : expClassSettings) {
						JSONObject expClsSetting = (JSONObject) obj;
						if (1 != expClsSetting.getIntValue("type")) {
							continue;
						}
						long clsId = expClsSetting.getLongValue("classId");
						String expSubs = expClsSetting.getString("subjectIds");

						expSubWishingList.add(divAlg.new MapEntry<String, Map<String, Integer>>(expSubs,
								classIdWishingMap.get(clsId)));
						// classIdWishingMap.get(clsId);
						// expSubWishingList.add(e)
						// expSubWishingMap.put(expSubs,
						// classIdWishingMap.get(clsId));
					}

					divAlg.setExpClass(expSubWishingList, expSubWishingList.size());
					List<MapEntry<String, Map<String, Integer>>> expClsEntryList = divAlg.getExpClassList();
					if (expClsEntryList != null) {
						for (Entry<String, Map<String, Integer>> expClsEntry : expClsEntryList) {
							expClsWishings.add(expClsEntry.getValue());
						}
					}
				}
				// 平行班
				for (int i = 0; i < 100; i++) {
					commonGroupWishings = divAlg.divClass();
					if (CollectionUtils.isNotEmpty(commonGroupWishings)) {
						break;
					}
				}
			}
			if (commonGroupWishings == null && (rePlacement!=0 || CollectionUtils.isEmpty(dezyClassList))) {
				setDivProc(placementId, "100", "分班失败,请重新设置", -1);
				return -1;
			}else{
				if(CollectionUtils.isNotEmpty(dezyClassList)){
					classNum = dezyClassList.size();
				}else{
					classNum = commonGroupWishings.size() + expClsWishings.size();
				}
				if (classNum != 0 && rePlacement == 0) {
					reqParams.put("thresholdStuInClass", reqParams.getIntValue("thresholdStuInClass") / classNum);
				}
				setDivProc(placementId, "50", "行政班划分成功", 0);
				calcParams.put("classNum", classNum);
				System.out.println(overNumbersWishings);
				System.out.println(commonGroupWishings);
			}
			// 获取入学年度(用于行政班命名)
			Calendar cal = Calendar.getInstance();
			String rxnd = commonDataService.ConvertSYNJ2RXND(dezySettings.getUsedGrade(),
					String.valueOf(cal.get(Calendar.YEAR)));
			calcParams.put("rxnd", rxnd);

			// 最小老师数（行政班组内班级数）
			int minTeacherNum = dezySettings.getMinTeacherNum();

			// 分班方案(1:跨组,2:不跨组)
			int placeAlgMethod = dezySettings.getPlaceAlgMethod();
			
			// 行政班总数(向上取整)
			int groupNum = 1;
			if (1 == placeAlgMethod) {// 跨组-只生成一个行政班
				groupNum = 3;
			}
			calcParams.put("groupNum", groupNum);

			/*
			 * 填充部分公共数据
			 */
			// 填充科目组
			List<TPlDezySubjectgroup> subjectGroupList = (List<TPlDezySubjectgroup>) settingParams
					.get("subjectGroupList");
			
			// 填充行政班下科目组合
			List<TPlDezySubjectcomp> subjectCompList = new ArrayList<TPlDezySubjectcomp>();
			// 填充行政班下科目组合构成
			List<TPlDezyTclassSubcomp> tclassSubcompList = new ArrayList<TPlDezyTclassSubcomp>();
			// 填充部分行政班组数据
			List<TPlDezyClassgroup> classGroupList = new ArrayList<TPlDezyClassgroup>();
			// 填充学生志愿列表
			List<TPlDezySubjectcompStudent> subjectCompStuList = new ArrayList<TPlDezySubjectcompStudent>();
			
			boolean hasPreDivData = false;
			if (CollectionUtils.isEmpty(dezyClassList)) {
				dezyClassList = new ArrayList<TPlDezyClass>();

				// 科目组
				List<String> subjectGroupIdList = new ArrayList<String>();
				for (TPlDezySubjectgroup subjectGroup : subjectGroupList) {
					String subjectGroupId = subjectGroup.getSubjectGroupId();
					subjectGroupIdList.add(subjectGroupId);
				}
				Iterator<String> wishingIterator = overNumbersWishings.keySet().iterator();
				Iterator<Map<String, Integer>> GroupWishingIterator = commonGroupWishings.iterator();
				Iterator<Long> oriClsIds = classIdClsMap.keySet().iterator();
				Iterator<Map<String, Integer>> expClsIter = expClsWishings.iterator();

				int classInGroup = 0;
				String wishingIdInGroup = null;
				if (wishingIterator.hasNext()) {
					wishingIdInGroup = wishingIterator.next();
					classInGroup = overNumbersWishings.get(wishingIdInGroup);
				}

				// 所有学生志愿映射(key:hafcode,value:number)
				Map<String, Integer> WishingMapAll = new HashMap<String, Integer>();
				Map<String, List<String>> hafCodeWishingIdMap = new HashMap<String, List<String>>();
				int expNum = 0;
				// 班级与志愿的映射
				Map<String, Map<String, String>> clsIdSubCompIdMap = new HashMap<String, Map<String, String>>();
				Map<String, List<String>> subCompIdClsListMap = new HashMap<String, List<String>>();

				// 行政班
				for (int i = 0; i < classNum; i++) {
					Map<String, Integer> wishingMap = new HashMap<String, Integer>();
					String oriClsId = null;
					String oriClsName = null;
					// 超人数（单）志愿组合
					String wisingIdOfSingleComp = null;
					boolean isExpCls = false;
					if (classNum - commonGroupWishings.size() - expClsWishings.size() > i) {
						if (classInGroup-- == 0 && wishingIterator.hasNext()) {
							wishingIdInGroup = wishingIterator.next();
							classInGroup = overNumbersWishings.get(wishingIdInGroup) - 1;
						}
						// hafcode-人数
						wishingMap.put(wishingIdInGroup,divAlg.getLargeWishingOfStu());
						wisingIdOfSingleComp = UUID.randomUUID().toString();
					} else {// 多志愿组合
						if (useOriClass && oriClsIds.hasNext()) { // 使用原来的行政班
							Long clsId = oriClsIds.next();
							oriClsId = String.valueOf(clsId);
							oriClsName = clsIdNameMap.get(clsId);
							wishingMap = classIdClsMap.get(clsId);
						} else if (GroupWishingIterator.hasNext()) { // 重新分班,使用新的行政班
							wishingMap = GroupWishingIterator.next();
						} else if (expClsIter.hasNext()) { // 重新分班,实验班
							wishingMap = expClsIter.next();
							expNum++;
							isExpCls = true;
						}
					}
					// WishingMapAll.putAll(wishingMap);
					// 行政班代码(填充行政班数据)
					String classId = (useOriClass) ? oriClsId : UUID.randomUUID().toString();
					Map<String, String> subjectIdsWishingIdMap = new HashMap<String, String>();
					clsIdSubCompIdMap.put(classId, subjectIdsWishingIdMap);
					TPlDezyClass dezyClass = new TPlDezyClass();
					dezyClass.setTclassId(classId);
					dezyClass.setPlacementId(dezySettings.getPlacementId());
					dezyClass.setSchoolId(dezySettings.getSchoolId());
					dezyClass.setTclassLevel(0); // 行政班
					dezyClass.setTclassType(6); // 行政班
					dezyClass.setUsedGrade(dezySettings.getUsedGrade());
					dezyClass.setSubjectGroupId("-999"/* subjectGroupId */); // 设置科目组
					dezyClass.setSubjectId("-999"); // 行政班
					String groundId = UUID.randomUUID().toString();
					dezyClass.setGroundId(groundId);

					// 0-平行班,1-实验班
					// dezySettings.ge
					int expCls = (isExpCls) ? 1 : 0;
					dezyClass.setIsExpClass(expCls);
					// 设置班级名称
					String className = "";
					if (null != oriClsName || isExpCls) {// 沿用原行政班名称或实验班
						if (isExpCls) {
							className = "实验" + expNum + "班";
						} else {
							className = oriClsName;
						}
					} else {
						if (rxnd != null && rxnd.trim().length() > 2) {
							className = String.format("G%.2s%02d班", rxnd.substring(2), i + 1);
						} else {
							className = String.format("G%02d班", i + 1);
						}
					}
					dezyClass.setTclassName(className);
					dezyClass.setGroundName(className + "教室");

					dezyClassList.add(dezyClass);

					// }

					// 行政班下(志愿科目组合以及志愿科目)
					int stuNumInclass = 0;
					for (String wishingId : wishingMap.keySet()) {
						// 行政班下-志愿科目组合表
						TPlDezySubjectcomp subjectComp = new TPlDezySubjectcomp();
						subjectComp.setSchoolId(dezySettings.getSchoolId());
						subjectComp.setPlacementId(dezySettings.getPlacementId());
						subjectComp.setUsedGrade(dezySettings.getUsedGrade());
						subjectComp.setClassId(classId);

						String wishingIds = divAlg.getWishingIds(wishingId);
						subjectComp.setCompFrom(divAlg.getWishingIds(wishingId)); // 科目Id,以逗号分隔

						String subjectCompId = (null == wisingIdOfSingleComp) ? divAlg.getWishingIdByCode(wishingId)
								: wisingIdOfSingleComp;
						// if(rePlacement==0){
						subjectCompId = UUID.randomUUID().toString();
						// }
						subjectIdsWishingIdMap.put(wishingIds, subjectCompId);

						subjectComp.setSubjectCompId(subjectCompId); // 科目组合Id
						subjectComp.setCompName(divAlg.getsortedWisingName(wishingId, commonGroupWishings)); // 志愿科目组合名称
						int stuInWishing = wishingMap.get(wishingId);
						subjectComp.setCompNum(stuInWishing); // 志愿人数
						stuNumInclass += stuInWishing;

						// 志愿下的人数（已拆分志愿）
						WishingMapAll.put(subjectCompId, stuInWishing);

						List<String> subCompIds = hafCodeWishingIdMap.containsKey(wishingId)
								? hafCodeWishingIdMap.get(wishingId) : new ArrayList<String>();
						subCompIds.add(subjectCompId);
						hafCodeWishingIdMap.put(wishingId, subCompIds);

						subjectCompList.add(subjectComp);

						// 行政班下-科目组合构成
						TPlDezyTclassSubcomp tclassSubComp = new TPlDezyTclassSubcomp();
						tclassSubComp.setPlacementId(dezySettings.getPlacementId());
						tclassSubComp.setSchoolId(dezySettings.getSchoolId());
						tclassSubComp.setUsedGrade(dezySettings.getUsedGrade());
						tclassSubComp.setTclassId(classId);
						tclassSubComp.setSubjectCompId(subjectCompId);

						tclassSubcompList.add(tclassSubComp);
					}
					// stuInWishingNum+=stuNumInclass;
					dezyClass.setTclassNum(stuNumInclass);
				}

				//// 填充学生志愿 & 选出实验班学生
				JSONArray expClassSettings = reqParams.getJSONArray("expClassSettings");
				List<String> expClassIds = new ArrayList<String>();
				if (!CollectionUtils.isEmpty(expClassSettings)) {
					List<String> expSubInClass = new ArrayList<String>();
					for (Object tmp : expClassSettings) {
						JSONObject expClassSetting = (JSONObject) tmp;
						expClassIds.add(expClassSetting.getString("classId")); // 实验班-班级代码
						expSubInClass.add(expClassSetting.getString("subjectIds")); // 实验班-开设重点科目
					}
				}
				// 实验班下的学生<classId,<studentId,wishingId>>
				Map<String, Map<String, String>> expClassMap = new HashMap<String, Map<String, String>>();

				for (JSONObject wishStud : wishStudList) {
					String subjectIds = wishStud.getString("subjectIds");
					String hafCode = divAlg.getWishingIdBySubjectIds(subjectIds);
					String stuId = wishStud.getString("accountId");

					// 添加实验班学生
					String classId = wishStud.getString("classId");

					if (expClassIds.contains(classId)) {
						Map<String, String> cls = expClassMap.get(classId);
						if (cls == null) {
							cls = new HashMap<String, String>();
							expClassMap.put(classId, cls);
						}
						cls.put(stuId, hafCode);
					}

					// 添加学生志愿
					TPlDezySubjectcompStudent stuSubComp = new TPlDezySubjectcompStudent();
					stuSubComp.setPlacementId(placementId);
					stuSubComp.setSchoolId(reqParams.getString("schoolId"));
					stuSubComp.setStudentId(stuId);

					List<String> subCompIds = hafCodeWishingIdMap.get(hafCode);
					if (null == subCompIds) {
						continue;
					}

					Map<String, String> subIdsWishingIdMap = clsIdSubCompIdMap.get(classId);

					String subCompId = subCompIds.get(0);
					int stuNum = WishingMapAll.get(subCompId);
					WishingMapAll.put(subCompId, --stuNum);
					if (stuNum <= 0) {
						subCompIds.remove(subCompId);
						if (0 == subCompIds.size()) {
							hafCodeWishingIdMap.remove(hafCode);
						}
					}
					// added 还需要判断实验班
					if (useOriClass) {
						subCompId = subIdsWishingIdMap.get(subjectIds);
					}
					stuSubComp.setSubjectCompId(subCompId);
					stuSubComp.setUsedGrade(wishStud.getString("gradeId"));

					subjectCompStuList.add(stuSubComp);
				}

				for (int i = 0; i < groupNum; i++) {
					String classGroupId = UUID.randomUUID().toString();
					for (int n = 0; n < subjectGroupIdList.size(); n++) {
						TPlDezyClassgroup classGroup = new TPlDezyClassgroup();
						classGroup.setSchoolId(dezySettings.getSchoolId());
						classGroup.setPlacementId(dezySettings.getPlacementId());
						classGroup.setUsedGrade(dezySettings.getUsedGrade());
						classGroup.setSubjectGroupId(subjectGroupIdList.get(n));
						classGroup.setClasstGroupId(classGroupId);
						classGroup.setClassGroupName("组" + (i + 1));

						classGroupList.add(classGroup);
					}
				}			
			}else if(null != rePlacement && 0 == rePlacement){	//行政班不重新分班，且以前分过行政班的情况
				hasPreDivData = true;
				/*subjectCompList = placementTaskDao.getSubjectcompList(reqParams);
				tclassSubcompList = placementTaskDao.getTclassSubcompList(reqParams);
				subjectCompStuList = placementTaskDao.getSubjectcompStuList(reqParams);
				classGroupList = placementTaskDao.getDezyClassGroupList(reqParams);
*/			}
			if(!hasPreDivData){
				settingParams.put("dezyClassList", dezyClassList);
				settingParams.put("subjectCompList", subjectCompList);
				settingParams.put("tclassSubcompList", tclassSubcompList);
				settingParams.put("subjectCompStuList", subjectCompStuList);
				settingParams.put("classGroupList", classGroupList);
			}

			// 设置进度,保存进redis(行政班生成完成)
			setDivProc(placementId, "15", "行政班分班完成", 0);

			int tryTimes = 0;
			double score = -100000;

			Map<String,Object>  bestResult = null;
			float per = (float)40/100;
			HashMap<String,List<TPlDezySubjectcomp>> classSubcompMap = TmpUtil.convertToClassSpMap(tclassSubcompList,subjectCompList);
			
			Map<String,Object> clsGroup = genGenDezyClassGroup(settingParams,classSubcompMap, reqParams.getIntValue("thresholdStuInClass"), 0.5);
			long dStart = new Date().getTime();
			while(score<10000&&tryTimes<800 ){
				tryTimes++;
				long dEnd = new Date().getTime();
				if(dEnd-dStart>120000){
					tryTimes = 800;
				}
				setDivProc(placementId, ""+(ScoreUtil.castFloatTowPointNum( tryTimes/10)+15), "正在划分教学班...", 0);

				long d1 = new Date().getTime();
				if (placeAlgMethod == 1) {
					clsGroup = genGenDezyClassGroup(settingParams, classSubcompMap,
							reqParams.getIntValue("thresholdStuInClass"), 0.5);
				}

				long d2 = new Date().getTime();
				Map<String, Object> tmp = genDezyTclass(clsGroup, reqParams, (float) (0.26));
				long d3 = new Date().getTime();
				int scoret = (int) tmp.get("score");
				if (scoret > score) {
					bestResult = TmpUtil.deepCopy(tmp);
					score = scoret;
				}
				System.out.println("-----行政班分组耗时----：" + (d2 - d1));
				System.out.println("-----分教学班耗时----：" + (d3 - d2));
			}
			System.out.println("best--score:" + score);
			bestResult.put("termInfo", reqParams.get("termInfo"));
			setDivProc(placementId, "95", "分班结果选优成功！", 0);
			saveResult(bestResult, dezySettings);

			// 更新分班状态
			reqParams.put("status", 100);
			reqParams.put("createDate", new Date());
			placementTaskDao.updateDivProc(reqParams);
			setDivProc(placementId, "100", "完成分班！", 0);
		} catch (Exception e) {
			setDivProc(placementId, "100", "分班失败！", -1);
			e.printStackTrace();
		}

		// executor.execute(new GenDezyClassPrc(settingParams));
		// new GenDezyClassPrc(settingParams);

		return 0;
	}

	/**
	 * 保存结果集
	 * 
	 * @param dezySettings
	 * @param clsGroup
	 * @return key:classGroupList,value:List<TPlDezyClassgroup><br>
	 *         key:dezyClassList,value:List<TPlDezyClass><br>
	 *         key:subjectCompList,value:List<TPlDezySubjectcomp><br>
	 *         key:tclassFromList,value:List<TPlDezyTclassfrom>
	 *         key:subjectCompStuList,value:List<TPlDezySubjectcompStudent><br>
	 *         key:tclassSubcompList,value:List<TPlDezyTclassSubcomp>
	 */
	private void saveResult(Map<String, Object> bestResult, TPlDezySettings dezySettings) throws Exception {
		// TODO Auto-generated method stub
		try {

			List<TPlDezyClassgroup> classGroupList = (List<TPlDezyClassgroup>) bestResult.get("classGroupList");
			List<TPlDezyClass> dezyClassList = (List<TPlDezyClass>) bestResult.get("dezyClassList");
			List<TPlDezySubjectcomp> subjectCompList = (List<TPlDezySubjectcomp>) bestResult.get("subjectCompList");
			List<TPlDezyTclassfrom> tclassFromList = (List<TPlDezyTclassfrom>) bestResult.get("tclassFromList");
			List<TPlDezySubjectcompStudent> subjectCompStuList = (List<TPlDezySubjectcompStudent>) bestResult
					.get("subjectCompStuList");
			List<TPlDezyTclassSubcomp> tclassSubcompList = (List<TPlDezyTclassSubcomp>) bestResult
					.get("tclassSubcompList");
			String termInfo = (String) bestResult.get("termInfo");

			for (TPlDezyClass tcl : dezyClassList) {
				if (tcl.getClassGroupId() == null || tcl.getClassGroupId().trim().length() == 0) {
					System.out.println("ffdsdfdsf");
				}
				
				//备份班级名称
				tcl.setOriClassName(tcl.getTclassName());
			}
			dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, classGroupList);
			dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, dezyClassList);
			dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, subjectCompList);
			dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, tclassFromList);
			dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, subjectCompStuList);
			dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, tclassSubcompList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommonRunException(-2, "保存数据出错！");
		}
	}

	/**
	 * 返回N个实体列表:<br>
	 * key:dezySetting,value:TPlDezySettings<br>
	 * key:subjectGroupList,value:List&lt;TPlDezySubjectgroup&gt;<br>
	 * key:subjectSetList,value:List&lt;TPlDezySubjectSet&gt;<br>
	 * key:classGroupList,value:List&lt;TPlDezyClassgroup&gt;<br>
	 * key:dezyClassList,value:List&lt;TPlDezyClass&gt;<br>
	 * key:subjectCompList,value:List&lt;TPlDezySubjectcomp&gt;<br>
	 * key:tclassSubcompList,value:List&lt;TPlDezyTclassSubcomp&gt;<br>
	 * key:subjectCompStuList,value:List&lt;TPlDezySubjectcompStudent&gt;
	 * 
	 * @param classSubcompMap
	 * @param settingParams,calcParams
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> genGenDezyClassGroup(Map<String, Object> settingParams,
			HashMap<String, List<TPlDezySubjectcomp>> classSubcompMap, int classMidNum, double xs) {
		settingParams = genDezyTclassGroupChild(settingParams);

		TPlDezySettings dezySetting = (TPlDezySettings) settingParams.get("dezySetting");
		List<TPlDezyClass> dezyClassList = (List<TPlDezyClass>) settingParams.get("dezyClassList");
		List<TPlDezyClass> needRemoveList = new ArrayList<TPlDezyClass>();
		for (TPlDezyClass tcl : dezyClassList) {
			if (tcl.getTclassType() == 7) {
				needRemoveList.add(tcl);
			}
		}
		dezyClassList.removeAll(needRemoveList);
		Map<String, Object> bestRs = settingParams;
		if (dezySetting.getPlaceAlgMethod() == 1) {
			int score = 0;
			int tryTimes = 0;
			while (score < 100 && tryTimes < 100) {
				tryTimes++;
				settingParams = genDezyTclassGroupChild(settingParams);
				int scoret = TmpUtil.getScoreByDezyClassGroup(settingParams, classSubcompMap, classMidNum, xs);

				if (scoret > score) {
					score = scoret;
					bestRs = (settingParams);
				}
			}

		}
		return bestRs;
	}

	public Map<String, Object> genDezyTclassGroupChild(Map<String, Object> settingParams) {
		List<TPlDezyClass> dezyClassList = (List<TPlDezyClass>) settingParams.get("dezyClassList");
		List<TPlDezyClassgroup> classGroupList = (List<TPlDezyClassgroup>) settingParams.get("classGroupList");

		int groupNum = classGroupList.size() / 2;

		// 随机进入行政班组
		Collections.shuffle(dezyClassList);

		// 班级组
		List<String> classGroupIds = new ArrayList<String>();
		for (TPlDezyClassgroup classGroup : classGroupList) {
			String classGroupId = classGroup.getClasstGroupId();
			if (!classGroupIds.contains(classGroupId)) {
				classGroupIds.add(classGroupId);
			}
		}

		// 分配班级给班级组
		Map<String, String> classGroupIdClassIdsMap = new HashMap<String, String>();
		Iterator<TPlDezyClass> classIte = dezyClassList.iterator();
		int subCount = 0;
		while (classIte.hasNext()) {
			TPlDezyClass dezyClass = classIte.next();
			String groupId = classGroupIds.get(subCount++ % groupNum);
			StringBuffer sb = new StringBuffer(dezyClass.getTclassId());
			;
			if (classGroupIdClassIdsMap.containsKey(groupId)) {// 已有班级所属该行政班组
				sb.append(",");
				sb.append(classGroupIdClassIdsMap.get(groupId));
			}
			classGroupIdClassIdsMap.put(groupId, sb.toString());

			// 补充开班表
			dezyClass.setClassGroupId(groupId);
		}

		// 补全行政班组表
		for (TPlDezyClassgroup classGroup : classGroupList) {
			String classGroupId = classGroup.getClasstGroupId();
			// 设置行政班组classIds字段
			classGroup.setClassIds(classGroupIdClassIdsMap.get(classGroupId));
		}

		// 2017年4月22日-武洋添加--部分班级未分配行政班组
		for (TPlDezyClass tclass : dezyClassList) {
			if (tclass.getClassGroupId() == null) {
				tclass.setClassGroupId("-99");
			}
		}

		return settingParams;
	}

	/**
	 * 生成教学班结果集
	 * 
	 * @param clsGroup
	 * @param reqParams
	 * @return key:score,value:本次教学班结果得分
	 *         key:dezySetting,value:TPlDezySettings<br>
	 *         key:subjectGroupList,value:List<TPlDezySubjectgroup><br>
	 *         key:subjectSetList,value:List<TPlDezySubjectSet><br>
	 *         key:classGroupList,value:List<TPlDezyClassgroup><br>
	 *         key:dezyClassList,value:List<TPlDezyClass><br>
	 *         key:subjectCompList,value:List<TPlDezySubjectcomp><br>
	 *         key:tclassFromList,value:List<TPlDezyTclassfrom>
	 *         key:subjectCompStuList,value:List<TPlDezySubjectcompStudent><br>
	 *         key:tclassSubcompList,value:List<TPlDezyTclassSubcomp>
	 */
	private Map<String, Object> genDezyTclass(Map<String, Object> clsGroup, JSONObject reqParams, float round) {

		Map<String, Object> rs = new HashMap<String, Object>();
		rs.putAll(clsGroup);
		// 设置部分
		TPlDezySettings dezySetting = (TPlDezySettings) clsGroup.get("dezySetting");
		List<TPlDezySubjectgroup> subjectGroupList = (List<TPlDezySubjectgroup>) clsGroup.get("subjectGroupList");
		List<TPlDezySubjectSet> subjectSetList = (List<TPlDezySubjectSet>) clsGroup.get("subjectSetList");
		// 由分班生成 可能会增加教学班的列表
		List<TPlDezyClassgroup> classGroupList = (List<TPlDezyClassgroup>) clsGroup.get("classGroupList");
		List<TPlDezyClass> dezyClassList = (List<TPlDezyClass>) clsGroup.get("dezyClassList");
		HashMap<String, List<TPlDezyClass>> optionTclassMap = new HashMap<String, List<TPlDezyClass>>();
		List<TPlDezySubjectcomp> subjectCompList = (List<TPlDezySubjectcomp>) clsGroup.get("subjectCompList");
		List<TPlDezyTclassSubcomp> tclassSubcompList = (List<TPlDezyTclassSubcomp>) clsGroup.get("tclassSubcompList");

		Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap = new HashMap<String, List<TPlDezyTclassSubcomp>>();
		// 移除本方法生成的教学班
		List<String> rTclassIds = new ArrayList<String>();
		List<String> lTclassIds = new ArrayList<String>();
		List<TPlDezyClass> rTclassList = new ArrayList<TPlDezyClass>();
		List<TPlDezyTclassSubcomp> rSubcompList = new ArrayList<TPlDezyTclassSubcomp>();
		for (TPlDezyClass tcl : dezyClassList) {
			if (tcl.getTclassType() == 7) {
				rTclassIds.add(tcl.getTclassId());
				rTclassList.add(tcl);
			} else {
				lTclassIds.add(tcl.getTclassId());
			}

		}
		for (TPlDezyTclassSubcomp tsub : tclassSubcompList) {
			String tclId = tsub.getTclassId();
			if (!lTclassIds.contains(tclId)) {
				rSubcompList.add(tsub);
			}
		}
		dezyClassList.removeAll(rTclassList);
		tclassSubcompList.removeAll(rSubcompList);
		// 由此处生成
		List<TPlDezyTclassfrom> tclassFromList = new ArrayList<TPlDezyTclassfrom>();
		int thresholdStuInClass = reqParams.getIntValue("thresholdStuInClass");
		int placementAlgType = reqParams.getIntValue("placementAlgType");
		// 班级合适的人数
		int classNumMax = thresholdStuInClass + dezySetting.getClassRoundNum();
		int classNumMin = thresholdStuInClass - dezySetting.getClassRoundNum();
		int classMidNum = thresholdStuInClass;
		// 开始构建生成教学班需要的数据结构
		int placeAlgMethod = dezySetting.getPlaceAlgMethod();

		JSONObject cxkmObj = new JSONObject();
		cxkmObj.put("schoolId", dezySetting.getSchoolId());
		cxkmObj.put("termInfo", reqParams.get("termInfo"));
		Map<String, String> kmMcMap = getSubjectNameMap(cxkmObj);
		DezyDisTclassParams disTclassParams = TmpUtil.initDezySettingParams(dezySetting, subjectSetList, classNumMax,
				classNumMin, classMidNum, placeAlgMethod, subjectCompList, kmMcMap);

		// 科目id_等级--序号map
		// Map<String,Integer> kmbjxhMap = new HashMap<String, Integer>();
		// 科目设置map
		HashMap<String, TPlDezySubjectSet> subjectSetMap = disTclassParams.getSubjectSetMap();
		// 志愿组合map
		HashMap<String, TPlDezySubjectcomp> subjectCompMap = new HashMap<String, TPlDezySubjectcomp>();
		// 志愿的行政班map
		HashMap<String, String> subcompClassMap = new HashMap<String, String>();
		for (TPlDezySubjectcomp subcomp : subjectCompList) {
			subjectCompMap.put(subcomp.getSubjectCompId(), subcomp);
			subcompClassMap.put(subcomp.getSubjectCompId(), subcomp.getClassId());
		}
		// 行政班组合构成map
		HashMap<String, List<TPlDezySubjectcomp>> classSubcompMap = new HashMap<String, List<TPlDezySubjectcomp>>();
		for (TPlDezyTclassSubcomp tclassSubcomp : tclassSubcompList) {
			String classId = tclassSubcomp.getTclassId();
			String subjectCompId = tclassSubcomp.getSubjectCompId();
			TPlDezySubjectcomp subcomp = subjectCompMap.get(subjectCompId);
			List<TPlDezySubjectcomp> subplist = null;
			if (classSubcompMap.containsKey(classId)) {
				subplist = classSubcompMap.get(classId);
			} else {
				subplist = new ArrayList<TPlDezySubjectcomp>();
			}
			subplist.add(subcomp);
			classSubcompMap.put(classId, subplist);
		}
		// 组合人数排序
		ListSortUtil<TPlDezySubjectcomp> compSort = new ListSortUtil<TPlDezySubjectcomp>();
		// 初始化定二走一分班运行时参数
		DezyDisTclassRunParams disTclassRunParams = new DezyDisTclassRunParams();
		disTclassRunParams.setClassSubcompMap(classSubcompMap);
		disTclassRunParams.setSubject2GroupMap(subjectGroupList);
		disTclassRunParams.setXzclassMap(dezyClassList);
		disTclassRunParams.setClassGroupList(classGroupList);
		disTclassRunParams.setTclassFromList(tclassFromList);
		// 场地空闲map subgroup_tclassId_classseq--using
		ConcurrentHashMap<String, String> xzgroudMap = new ConcurrentHashMap<String, String>();
		HashMap<String, String> xzgroud2xzClassIdMap = new HashMap<String, String>();
		// 辅助教室占用map
		ConcurrentHashMap<String, List<Integer>> fzgroudMap = new ConcurrentHashMap<String, List<Integer>>();
		// 辅助教室列表 json：groundId,groundName
		Vector<JSONObject> fzgroudList = new Vector<JSONObject>();
		HashMap<String, TPlDezyClass> xzclassMap = new HashMap<String, TPlDezyClass>();
		for (TPlDezyClass xzclass : dezyClassList) {
			xzclassMap.put(xzclass.getTclassId(), xzclass);
			xzgroud2xzClassIdMap.put(xzclass.getGroundId(), xzclass.getTclassId());
		}
		// subjectGroupList
		HashMap<String, TPlDezySubjectgroup> subject2GroupMap = new HashMap<String, TPlDezySubjectgroup>();
		for (TPlDezySubjectgroup sub : subjectGroupList) {
			String[] subIds = sub.getSubjectIds().split(",");
			for (int i = 0; i < subIds.length; i++) {
				subject2GroupMap.put(subIds[i], sub);
			}
		}
		// subject2GroupMap.s("19", subjectGroupList.get(0));
		int unfinishSubs = 0;
		//开始分班
		if (placeAlgMethod == 1) {
			// 不跨组生成教学班 科目组分开排课
			// 单科最大重复开班数
			unfinishSubs += TmpUtil.divTclassNoneCross(subjectGroupList, classGroupList, dezyClassList, optionTclassMap,
					tclassSubMap, tclassFromList, classNumMax, classNumMin, placeAlgMethod, kmMcMap, subjectSetMap,
					subjectCompMap, classSubcompMap, compSort, xzgroudMap, fzgroudMap, fzgroudList, xzclassMap);

		} else {
			int isTechExist = reqParams.getIntValue("isTechExist");
			unfinishSubs = TmpUtil.divTclassCrossGroup(dezySetting, subjectSetList, classGroupList, dezyClassList,
					optionTclassMap, tclassSubMap, tclassFromList, classNumMax, classNumMin, placeAlgMethod, kmMcMap,
					subjectCompMap, classSubcompMap, compSort, xzgroudMap, fzgroudMap, fzgroudList, xzclassMap,
					subject2GroupMap, placementAlgType, isTechExist);
		}

		// 生成教学班结束 优化教学班
		int tclNumScore = 0;

		int xzMaxRs = 48;
		for (int i = 0; i <= 20; i++) {
			List<TPlDezyClass> rmList = new ArrayList<TPlDezyClass>();
			tclNumScore = TmpUtil.adjustTclassResult(dezyClassList, tclassSubMap, optionTclassMap, classMidNum, 0.5,
					subjectCompMap, xzclassMap, xzgroudMap, fzgroudMap, fzgroudList, kmMcMap, subjectSetMap,
					placeAlgMethod, xzMaxRs);

			for (TPlDezyClass tcl : dezyClassList) {
				if (tcl.getTclassNum() == 0) {
					rmList.add(tcl);
					tclassSubMap.remove(tcl.getTclassId());
					String subgId = "";
					if (!tcl.getSubjectGroupId().equals("-999") && placeAlgMethod != 2) {
						subgId = tcl.getSubjectGroupId();
					}
					if (xzgroud2xzClassIdMap.containsKey(tcl.getGroundId())) {
						// 移除行政班场地占用
						xzgroudMap.remove(
								subgId + "_" + xzgroud2xzClassIdMap.get(tcl.getGroundId()) + "_" + tcl.getClassSeq());
					} else {
						// 移除辅助场地占用

						for (int j = 0; j < fzgroudList.size(); j++) {
							JSONObject fzground = fzgroudList.get(j);
							String gid = fzground.getString("groundId");
							if (gid.equals(tcl.getGroundId())) {
								List<Integer> seqList = fzgroudMap.get(subgId + "_" + j);
								if (seqList != null) {
									seqList.remove((Integer) tcl.getClassSeq());
								}
							}
						}
					}
				}
			}

			dezyClassList.removeAll(rmList);
		}

		// 班级生成完毕 生成需要的结果
		Map<String, List<String>> tclass2classIdMap = new HashMap<String, List<String>>();
		Map<String, String> tclass2MaxRsClassMap = new HashMap<String, String>();
		for (String tclId : tclassSubMap.keySet()) {
			List<TPlDezyTclassSubcomp> tclSpList = tclassSubMap.get(tclId);
			List<TPlDezySubjectcomp> spList = new ArrayList<TPlDezySubjectcomp>();

			List<String> xzcids = new ArrayList<String>();
			for (TPlDezyTclassSubcomp subcomp : tclSpList) {
				String spId = subcomp.getSubjectCompId();
				TPlDezySubjectcomp osubcomp = subjectCompMap.get(spId);
				spList.add(osubcomp);
				String xzclassid = subcompClassMap.get(spId);
				if (!xzcids.contains(xzclassid)) {
					xzcids.add(xzclassid);
				}
			}
			compSort.sort(subjectCompList, "compNum", "desc");
			tclass2MaxRsClassMap.put(tclId, subjectCompList.get(0).getClassId());
			tclass2classIdMap.put(tclId, xzcids);
			tclassSubcompList.addAll(tclSpList);
		}
		TmpUtil.adjustGrounds(xzgroudMap, fzgroudMap, fzgroudList, dezyClassList, xzgroud2xzClassIdMap, xzclassMap,
				kmMcMap, placeAlgMethod, tclass2MaxRsClassMap, tclass2classIdMap, xzMaxRs);
		for (int i = 0; i < 3; i++) {

			TmpUtil.adjustGrounds2(xzgroudMap, fzgroudMap, fzgroudList, dezyClassList, xzgroud2xzClassIdMap, xzclassMap,
					kmMcMap, placeAlgMethod);
		}

		HashMap<String, TPlDezyClass> tclassMap = new HashMap<String, TPlDezyClass>();
		for (TPlDezyClass dtcl : dezyClassList) {
			if (dtcl == null || dtcl.getTclassId() == null) {
				System.out.println("fdsf");
			}
			tclassMap.put(dtcl.getTclassId(), dtcl);
		}
		for (String tclId : tclass2classIdMap.keySet()) {

			List<String> xzcids = tclass2classIdMap.get(tclId);
			TPlDezyClass newTclass = tclassMap.get(tclId);
			for (String xzId : xzcids) {

				TPlDezyTclassfrom re = new TPlDezyTclassfrom();
				re.setClassGroupId(newTclass.getClassGroupId());
				re.setClassId(xzId);
				re.setPlacementId(newTclass.getPlacementId());
				re.setSchoolId(newTclass.getSchoolId());
				re.setSubjectGroupId(newTclass.getSubjectGroupId());
				re.setTclassId(newTclass.getTclassId());
				re.setUsedGrade(newTclass.getUsedGrade());

				tclassFromList.add(re);
			}
		}
		rs.put("tclassFromList", tclassFromList);
		int score = 10000;

		int minus = 0;

		HashMap<String, Integer> subjectLvlseqNumMap = new HashMap<String, Integer>();
		for (TPlDezyClass dtcl : dezyClassList) {
			score = (int) (score - Math.pow(classMidNum - dtcl.getTclassNum(), 4) / 10000);
			if (dtcl.getTclassNum() < classMidNum * (1 - round) || dtcl.getTclassNum() > classMidNum * (1 + round)) {
				minus += 10;
			}
			String slsKey = dtcl.getSubjectId() + "," + dtcl.getTclassLevel() + "," + dtcl.getClassSeq();
			if (!subjectLvlseqNumMap.containsKey(slsKey)) {
				subjectLvlseqNumMap.put(slsKey, 1);
			} else {
				subjectLvlseqNumMap.put(slsKey, subjectLvlseqNumMap.get(slsKey) + 1);
			}
		}
		HashMap<String, Integer> subjectLvlseqMaxNumMap = new HashMap<String, Integer>();
		for (String ssKey : subjectLvlseqNumMap.keySet()) {
			int nowNum = subjectLvlseqNumMap.get(ssKey);
			if (subjectLvlseqMaxNumMap.containsKey(ssKey)) {
				int existNum = subjectLvlseqMaxNumMap.get(ssKey);
				if (nowNum > existNum) {
					subjectLvlseqMaxNumMap.put(ssKey, nowNum);
				}
			} else {
				subjectLvlseqMaxNumMap.put(ssKey, nowNum);
			}
		}

		//
		int teaScore = 0;
		if (placeAlgMethod == 2 && placementAlgType == 2) {
			int xls = 3;
			for (int bs : subjectLvlseqMaxNumMap.values()) {
				if (bs > xls) {
					teaScore -= 20 * (bs - xls);
				}
			}
		}
		if (minus > 0) {
			score = 5000 - minus + tclNumScore + teaScore;
			// score = 5000 - minus + tclNumScore ;
		}
		if (unfinishSubs > 0) {
			score = -9999;
		}
		// score-= unfinishSubs*10;
		// classMidNum
		rs.put("score", score);
		return rs;
	}

	/**
	 * 定二走一分班总流程
	 * 
	 * @author dengzhihua
	 *
	 */
	class GenDezyClassPrc implements Callable<Integer> {
		private Map<String, Object> settingParams;

		GenDezyClassPrc(Map<String, Object> settingParams) {
			this.settingParams = settingParams;
		}

		@Override
		public Integer call() throws Exception {
			// TODO Auto-generated method stub
			return divFixTwoGoOneClass(settingParams);
		}
	}
	
	@Override
	public JSONObject getGenDezyClassProc(String placementId,Float currentProgress) {
		String progressMapKey = "dezy." + placementId + ".progressMap";
		try {
			JSONObject progressJSON = null;
			Object progressMapObj = redisOperationDAO.get(progressMapKey);
			if (null == progressMapObj) {
				progressJSON = new JSONObject();
				progressJSON.put("progress", 0);
				progressJSON.put("msg", "开始分班！");
				progressJSON.put("code", 0);
				redisOperationDAO.set(progressMapKey, progressJSON);
			}else{
				progressJSON = (JSONObject) progressMapObj;
				Float nextProgress = progressJSON.getFloat("nextProgress");
				Float progress = progressJSON.getFloat("progress");
				if(nextProgress!=null&&currentProgress!=null){
					if(currentProgress<progress){
						progressJSON.put("progress", progress);
					}else if(currentProgress+1<nextProgress){
						progressJSON.put("progress", currentProgress+1);
					}
					
				}
			}
			return progressJSON;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 1.4 查看分班结果预览
	 * obj:{keys:schoolId,placementId,subjectId/subjectGroupId,type}
	 */
	@Override
	public List<JSONObject> getTclassPreview(JSONObject obj) {
		try {
			JSONObject preSettings = dezyPlacementTaskDao.queryDezyPreSetting(obj);
			Map<String, String> courseMap = getSubjectNameMap(obj);
			Long schoolId = obj.getLong("schoolId");
			String termInfoId = obj.getString("termInfo");// commonDataService.getCurTermInfoId(schoolId);

			obj.put("termInfo", termInfoId);

			/*
			 * int replacement = preSettings.getIntValue("rePlacement");
			 * if(replacement==0){ Map<String, Object> latestDivSetting =
			 * dezyPlacementTaskDao.queryLatestDivData(obj); String placementId
			 * = (String)latestDivSetting.get("placementId"); Long usedGrade =
			 * (Long)latestDivSetting.get("usedGrade"); }
			 */

			// 确定查询科目所属科目组
			int type = obj.getIntValue("type");
			String subjectGroupId = null;
			String querySubjectId = null;

			List<TPlDezySubjectgroup> subGroupList = placementTaskDao.getSubjectGroupList(obj);
			switch (type) {
			case 0:
				subjectGroupId = obj.getString("id");
				break;
			case 1:
				try {
					querySubjectId = obj.getString("id");
					for (TPlDezySubjectgroup subGroup : subGroupList) {
						String[] subjectIds = subGroup.getSubjectIds().split(",");
						if (Arrays.asList(subjectIds).contains(querySubjectId)) {
							subjectGroupId = subGroup.getSubjectGroupId();
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.info("不能找到科目所属组!");
				}
				break;
			default:
				return null;
			}

			// 获取行政班与走班班级列表
			List<JSONObject> data = new ArrayList<JSONObject>();
			List<TPlDezyClass> dezyClassList = placementTaskDao.getDezyClassList(obj);
			Map<String, TPlDezyClass> tclassMap = new HashMap<String, TPlDezyClass>();
			Map<String, TPlDezyClass> classMap = new HashMap<String, TPlDezyClass>();

			// 技术单独处理
			String techId = getTechId(obj);
			if (null == subjectGroupId && techId.equals(querySubjectId)) {
				// techId.equals(subjectGroupId
				subjectGroupId = querySubjectId;
			}

			Iterator<TPlDezyClass> classIterator = dezyClassList.iterator();
			while (classIterator.hasNext()) {
				TPlDezyClass dezyClass = classIterator.next();

				int classType = dezyClass.getTclassType();
				switch (classType) {
				case 6:// 行政班
					classMap.put(dezyClass.getTclassId(), dezyClass);
					break;
				case 7:// 走班班级
						// 剔选走班班级所属科目组
					String subGroupId = dezyClass.getSubjectGroupId();
					if (!subjectGroupId.equals(subGroupId)
							|| (techId != null && techId.equals(dezyClass.getSubjectId()))) {
						// continue;
					}

					tclassMap.put(dezyClass.getTclassId(), dezyClass);
					break;
				default:
					continue;
				}
			}

			// 获取走班教学班下的志愿组合(包含行政班下志愿组合)以及志愿下的教学班
			List<TPlDezyTclassSubcomp> tclassSubComp = placementTaskDao.getTclassSubcompList(obj);
			Map<String, String> tclassSubCompMap = new HashMap<String, String>();
			Map<String, List<String>> tclassInWishingMap = new HashMap<String, List<String>>();
			for (TPlDezyTclassSubcomp subComp : tclassSubComp) {
				String tclassId = subComp.getTclassId();
				// 过滤行政班
				if (classMap.containsKey(tclassId)) {
					continue;
				}

				String subjectIds = subComp.getSubjectCompId();

				// 教学班映射已存在
				if (tclassSubCompMap.containsKey(tclassId)) {
					StringBuffer sb = new StringBuffer(subjectIds);
					sb.append("," + tclassSubCompMap.get(tclassId));
					tclassSubCompMap.put(tclassId, sb.toString());
				} else {
					tclassSubCompMap.put(tclassId, subjectIds);
				}

				// 添加志愿-教学班映射
				List<String> tclassList = tclassInWishingMap.containsKey(subjectIds)
						? tclassInWishingMap.get(subjectIds) : new ArrayList<String>();

				tclassList.add(tclassId);
				tclassInWishingMap.put(subjectIds, tclassList);
			}

			// 获取科目所属行政班下的教学班(走班班级)
			List<TPlDezyTclassfrom> tclassFromList = placementTaskDao.getTclassFromList(obj);
			Map<String, JSONArray> subjectInClassMap = new HashMap<String, JSONArray>();

			// 获取行政班下的志愿组合
			List<TPlDezySubjectcomp> subCompList = placementTaskDao.getSubjectcompList(obj);
			Map<String, JSONArray> subInClassMap = new HashMap<String, JSONArray>();
			Iterator<TPlDezySubjectcomp> subCompIter = subCompList.iterator();
			int n = 0;
			while (subCompIter.hasNext()) {
				TPlDezySubjectcomp subComp = subCompIter.next();
				JSONObject subject = new JSONObject();

				String classId = subComp.getClassId();
				JSONArray subjctComp = null;
				if (subInClassMap.containsKey(classId)) {
					subjctComp = subInClassMap.get(classId);
				} else {
					subjctComp = new JSONArray();
					subInClassMap.put(classId, subjctComp);
				}
				subject.put("wishingId", subComp.getSubjectCompId());
				subject.put("wishingName", subComp.getCompName());
				subject.put("wishingCount", subComp.getCompNum());
				subject.put("wishingSeq", n++);

				subjctComp.add(subject);
			}

			// ++++++++++++补全志愿所属行政班下的教学班+++++++++++++
			try {
				empty: // 行政班下没有教学班的情况
				for (String classId : subInClassMap.keySet()) {
					// 获取行政班下的志愿数
					JSONArray subjectComp = subInClassMap.get(classId);
					List<String> wishingIdList = new ArrayList<String>();
					for (Object object : subjectComp) {
						JSONObject wishing = (JSONObject) object;
						wishingIdList.add(new String(wishing.getString("wishingId")));
					}

					// 行政班下的科目组(单科列表的形式)
					JSONArray subjectInClass = null;
					if (subjectInClassMap.containsKey(classId)) {
						subjectInClass = subjectInClassMap.get(classId);
					} else {
						subjectInClass = new JSONArray();
						subjectInClassMap.put(classId, subjectInClass);
					}
					Map<String, JSONObject> subjectGroupMap = new HashMap<String, JSONObject>();

					// 查询科目组

					// 补全志愿下的教学班（保证每个志愿都有对应的教学班）
					for (String wishingId : wishingIdList) {
						// 志愿下的教学班
						List<String> tclassIds = tclassInWishingMap.get(wishingId);
						label: for (String tclassId : tclassIds) {
							JSONObject subjectGroup = null;
							TPlDezyClass dezyTclass = tclassMap.get(tclassId);
							String subjectId = dezyTclass.getSubjectId();

							// 过滤科目组
							if (!subjectGroupId.equals(dezyTclass.getSubjectGroupId())
									&& !techId.equals(subjectGroupId)) {
								continue;
							}
							// 过滤科目
							if (null != querySubjectId && !querySubjectId.equals(subjectId)) {
								continue;
							}

							if (subjectGroupMap.containsKey(subjectId)) {
								subjectGroup = subjectGroupMap.get(subjectId);
							} else {
								subjectGroup = new JSONObject();
								subjectGroup.put("subjectName", courseMap.get(subjectId));
								subjectGroup.put("subjectSeq", dezyTclass.getClassSeq());
								subjectGroup.put("subjectId", subjectId);
								subjectGroup.put("tclass", new JSONArray());

								subjectGroupMap.put(subjectId, subjectGroup);
							}
							JSONArray tclass = subjectGroup.getJSONArray("tclass");

							// 过滤可能存在的重复教学班
							for (Object object : tclass) {
								JSONObject dezyTcls = (JSONObject) object;
								String tclsId = dezyTcls.getString("tclassId");
								if (tclsId.equals(tclassId)) {
									continue label;
								}
							}

							JSONObject tcls = new JSONObject();

							tcls.put("tclassName", dezyTclass.getTclassName());
							tcls.put("tclassId", tclassId);
							tcls.put("groundName", dezyTclass.getGroundName());
							tcls.put("tclassCount", dezyTclass.getTclassNum());

							if (dezyTclass.getTclassType() == 0) {
								tcls.put("wishingIds", wishingId);
							} else {
								tcls.put("wishingIds", tclassSubCompMap.get(tclassId));
							}

							// 确定走班、选考和学考
							int tclassType = dezyTclass.getClassSeq() == 3 ? 0 : dezyTclass.getTclassLevel();
							tcls.put("type", tclassType);// 走班教学
							tclass.add(tcls);
						}
					}

					// 行政班下不存在教学班的情况（补全科目）
					// 过滤科目
					/*
					 * List<String> querySubs = new ArrayList<String>();
					 * if(null!=querySubjectId){ querySubs.add(querySubjectId);
					 * }else{ //过滤科目组 if(null!=subjectGroupId){
					 * for(TPlDezySubjectgroup subGroup : subGroupList){
					 * if(subjectGroupId.equals(subGroup.getSubjectGroupId())){
					 * querySubs =
					 * Arrays.asList(subGroup.getSubjectIds().split(","));
					 * break; } } } } if(null==subjectGroupMap ||
					 * subjectGroupMap.size()<querySubs.size()){ querySub:
					 * for(String subjectId : querySubs){ for(Object object :
					 * subjectInClass){ JSONObject subGroup =
					 * (JSONObject)object; String subId =
					 * subGroup.getString("subjectId"); if(null!=subId &&
					 * subId.equals(subjectId)){ continue querySub; } }
					 * JSONObject subjectGroup = new JSONObject();
					 * subjectGroup.put("subjectName",
					 * courseMap.get(subjectId)); subjectGroup.put("subjectId",
					 * subjectId);
					 * 
					 * subjectGroupMap.put(subjectId, subjectGroup); } }
					 */

					subjectInClass.addAll(subjectGroupMap.values());

					for (Object object : subjectInClass) {
						// （单科）科目组
						JSONObject subjectGroup = (JSONObject) object;
						String subjectId = subjectGroup.getString("subjectId");
						JSONArray tclass = subjectGroup.getJSONArray("tclass");

						if (null == tclass) {// 行政班下没有教学班
							continue;
						}
						for (Object tmp : tclass) {
							JSONObject tcls = (JSONObject) tmp;
							String[] wishingIds = tcls.getString("wishingIds").split(",");
							wishingIdList.removeAll(Arrays.asList(wishingIds));
						}

						// 补全志愿下的教学班（保证每个志愿都有对应的教学班）
						for (String wishingId : wishingIdList) {

							List<String> tclassIds = tclassInWishingMap.get(wishingId);
							// 志愿下的教学班
							for (String tclassId : tclassIds) {
								JSONObject tcls = new JSONObject();
								TPlDezyClass dezyTclass = tclassMap.get(tclassId);
								String tclassInSubId = dezyTclass.getSubjectId();

								// 剔除不同科目下的教学班
								if (!subjectId.equals(tclassInSubId)) {
									continue;
								}

								tcls.put("tclassName", dezyTclass.getTclassName());
								tcls.put("tclassId", tclassId);
								tcls.put("groundName", dezyTclass.getGroundName());
								tcls.put("tclassCount", dezyTclass.getTclassNum());
								tcls.put("wishingIds", tclassSubCompMap.get(tclassId));

								// 确定走班、选考和学考
								int tclassType = dezyTclass.getClassSeq() == 3 ? 0 : dezyTclass.getTclassLevel();
								tcls.put("type",
										tclassType/*
													 * dezyTclass.getTclassLevel()
													 */);// 走班教学班
								tclass.add(tcls);
							}

						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			// ++++++++++++ end +++++++++++++

			// 行政班组
			List<TPlDezyClassgroup> dezyClassGroupList = placementTaskDao.getDezyClassGroupList(obj);
			Iterator<TPlDezyClassgroup> clsGroupIter = dezyClassGroupList.iterator();
			String techSubGroup = null;
			while (clsGroupIter.hasNext()) {
				TPlDezyClassgroup dezyClassGroup = clsGroupIter.next();
				JSONObject clsGroup = new JSONObject();

				// 过滤科目组
				String subGroupId = dezyClassGroup.getSubjectGroupId();
				if (!subjectGroupId.equals(subGroupId) && !subjectGroupId.equals(techId)) {
					continue;
				}

				// 技术科目特别处理
				if (null == techSubGroup) {
					techSubGroup = subGroupId;
				}
				if (!techSubGroup.equals(subGroupId)) {
					continue;
				}

				String classGroupName = dezyClassGroup.getClassGroupName();
				String classGroupId = dezyClassGroup.getClasstGroupId();
				clsGroup.put("classGroupId", classGroupId);
				clsGroup.put("classGroupName", classGroupName);
				// 班级组下的行政班
				String classList = dezyClassGroup.getClassIds();
				if (null != classList) {
					JSONArray clsList = new JSONArray();
					for (String classId : classList.split(",")) {
						JSONObject cls = new JSONObject();
						TPlDezyClass dezyClass = classMap.get(classId);
						if(null==dezyClass){
							continue;
						}
						cls.put("classId", dezyClass.getTclassId());
						cls.put("className", dezyClass.getTclassName());
						cls.put("classNum", dezyClass.getTclassNum());

						// 添加行政班下的志愿列表
						JSONArray wishings = subInClassMap.get(classId);
						cls.put("wishings", wishings);

						// +++++++++ 剔除不在当前行政班下的志愿（用于合并单元格） +++++++++++++
						/*
						 * List<String> wishingList = new ArrayList<String>();
						 * for(Object o : wishings){ JSONObject wish =
						 * (JSONObject)o;
						 * wishingList.add(wish.getString("wishingId")); }
						 */
						// ++++++++++ end +++++++++++++++

						// 添加行政班下的科目组
						JSONArray subjectGroup = new JSONArray();
						try {
							for (Object object : subjectInClassMap.get(classId)) {
								JSONObject subject = BeanTool.castBeanToFirstLowerKey((JSONObject) object);

								// +++++++++ 剔除不在当前行政班下的志愿（用于合并单元格） +++++++++++++
								/*
								 * JSONArray tclassArray =
								 * subject.getJSONArray("tclass"); for(Object tmp :
								 * tclassArray){ JSONObject tclass =
								 * (JSONObject)tmp; String wishingsAll =
								 * tclass.getString("wishingIds"); StringBuffer sb =
								 * new StringBuffer(); for(String wish :
								 * wishingsAll.split(",")){
								 * if(wishingList.contains(wish)){
								 * sb.append(wish+","); } }
								 * tclass.remove("wishingIds");
								 * tclass.put("wishingIds", sb.substring(0,
								 * sb.length()-1)); }
								 */
								// ++++++++++ end +++++++++++++++

								subjectGroup.add(subject);
							}
						}catch (Exception e) {
							e.printStackTrace();
							throw e;
						}

						subjectGroup = (JSONArray) subjectGroup.clone();
						cls.put("subjectGroup", subjectGroup);

						clsList.add(cls);
					}
					clsGroup.put("class", clsList);
				}

				// 组排序
				boolean alreadyInserted = false;
				if (data != null) {
					int insertPos = 0;
					int clsGroupNum = classGroupName.charAt(1);
					for (JSONObject group : data) {
						String groupName = group.getString("classGroupName");
						int groupNum = groupName.charAt(1);
						if (clsGroupNum < groupNum) {
							data.add(insertPos, clsGroup);
							alreadyInserted = true;
							break;
						}
						insertPos++;
					}
				}
				if (!alreadyInserted) {
					// 组内行政班排序
					List<JSONObject> cls = (List<JSONObject>) clsGroup.get("class");
					Collections.sort(cls, new Comparator<JSONObject>() {

						@Override
						public int compare(JSONObject o1, JSONObject o2) {
							// TODO Auto-generated method stub
							return o1.getString("className").compareTo(o2.getString("className"));
						}
					});

					data.add(data.size(), clsGroup);
				}

			}

			// 补全表头科目
			// 查询科目（表头科目）
			List<String> querySubs = new ArrayList<String>();
			if (null != querySubjectId) {
				querySubs.add(querySubjectId);
			} else {
				// 过滤科目组
				if (null != subjectGroupId) {
					for (TPlDezySubjectgroup subGroup : subGroupList) {
						if (subjectGroupId.equals(subGroup.getSubjectGroupId())) {
							querySubs = Arrays.asList(subGroup.getSubjectIds().split(","));
							break;
						}
					}
				}
			}
			for (JSONObject classGroup : data) {
				JSONArray clsArray = classGroup.getJSONArray("class");
				for (Object object : clsArray) {
					JSONObject cls = (JSONObject) object;
					JSONArray subjectGroup = cls.getJSONArray("subjectGroup");

					List<String> alreadyInSubs = new ArrayList<String>();
					for (Object tmp : subjectGroup) {
						JSONObject subject = (JSONObject) tmp;
						String subjectId = subject.getString("subjectId");
						alreadyInSubs.add(new String(subjectId));
					}
					for (String querySub : querySubs) {
						if (!alreadyInSubs.contains(querySub)) {
							JSONObject subject = new JSONObject();
							subject.put("subjectName", courseMap.get(querySub));
							subject.put("subjectId", querySub);
							subjectGroup.add(subject);
						}
					}
				}
			}
			sortDivResult(data);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 分班结果排序 & 志愿显示排序
	 * 
	 * @param data
	 */
	private void sortDivResult(List<JSONObject> data) {
		for (JSONObject group : data) {
			JSONArray clsArray = group.getJSONArray("class");
			for (Object obj : clsArray) {
				JSONObject cls = (JSONObject) obj;
				JSONArray wishings = cls.getJSONArray("wishings");
				List<String> subjectNames = new ArrayList<String>();
				for (Object tmp : wishings) {
					JSONObject wishing = (JSONObject) tmp;
					subjectNames.add(new String(wishing.getString("wishingName")));
				}

				List<String> subNames = new ArrayList<String>();
				for (String sub : Arrays.asList(subjectNames.get(0).split(","))) {
					subNames.add(new String(sub));
				}

				for (int i = 1; i < subjectNames.size(); i++) {
					List<String> subNames1 = Arrays.asList(subjectNames.get(i).split(","));
					for (String subName : Arrays.asList(subjectNames.get(0).split(","))) {
						if (!subNames1.contains(subName)) {
							subNames.remove(subName);
						}
					}
				}

				for (Object tmp : wishings) {
					JSONObject wishing = (JSONObject) tmp;
					String wishingName = wishing.getString("wishingName");
					List<String> subjectNameLs = new ArrayList<String>();
					for (String subject : Arrays.asList(wishingName.split(","))) {
						subjectNameLs.add(new String(subject));
					}
					StringBuffer sb = new StringBuffer();
					for (String sub : subNames) {
						sb.append(sub);
						sb.append(",");
						subjectNameLs.remove(sub);
					}
					for (int i = 0; i < subjectNameLs.size(); i++) {
						sb.append(subjectNameLs.get(i));
						sb.append(",");
					}
					wishing.put("wishingName", sb.subSequence(0, sb.length() - 1));
				}

			}
		}
	}

	/**
	 * 设置分班进度
	 */
	public static String rtMsg = "";
	@Override
	public void setDivProc(String placementId, String percentage, String msg, int code, String... rsMsg) {		
		
		String progressMapKey = "dezy." + placementId + ".progressMap";
		try {
			Object progressMapObj = redisOperationDAO.get(progressMapKey);
			if (null == progressMapObj) {
				JSONObject data = new JSONObject();
				progressMapObj = data;
				redisOperationDAO.set(progressMapKey, data, 300);
			}
			JSONObject data = (JSONObject) progressMapObj;
			if(Float.parseFloat(percentage)<70 && Float.parseFloat(percentage)>20
					&& data.containsKey("progress")
					&& Float.parseFloat(percentage)<data.getFloatValue("progress")) {
				
			}else{
				data.put("progress", percentage);
				data.put("msg", msg);
				data.put("code", code);
			}
			if (null != rsMsg && rsMsg.length > 0) {
				data.put("resultMsg", rsMsg[0]);
			}
			redisOperationDAO.set(progressMapKey, data, 300);
			if(Float.parseFloat(percentage)==100){
				rtMsg = msg;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setDivProc(String placementId, String percentage, String nextPercentage,String msg, int code, String... rsMsg) {		
		
		String progressMapKey = "dezy." + placementId + ".progressMap";
		try {
			JSONObject data = null;
			Object progressMapObj = redisOperationDAO.get(progressMapKey);
			if (progressMapObj !=null){
				data  = (JSONObject) progressMapObj;
				if(Float.parseFloat(percentage) < data.getFloat("progress") ){
					return;
				}
			}else {
				data = new JSONObject();
			}
			data.put("progress", percentage);
			data.put("nextProgress", nextPercentage);
			data.put("msg", msg);
			data.put("code", code);
			if (null != rsMsg && rsMsg.length > 0) {
				data.put("resultMsg", rsMsg[0]);
			}
			redisOperationDAO.set(progressMapKey, data, 300);
			if(Float.parseFloat(percentage)==100){
				rtMsg = msg;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int recoveryClassName(JSONObject obj) {

		return dezyPlacementTaskDao.recoverClassName(obj);
	}

	@Override
	public JSONArray getQuerySubList(JSONObject obj) {
		// 获取科目Id与科目名称映射
		Map<String, String> courseMap = getSubjectNameMap(obj);

		JSONArray data = new JSONArray();
		List<TPlDezySubjectSet> subSets = placementTaskDao.getSubjectSetList(obj);
		for (TPlDezySubjectSet sub : subSets) {
			JSONObject subSet = new JSONObject();
			subSet.put("id", sub.getSubjectId());
			subSet.put("text", courseMap.get(sub.getSubjectId()));
			subSet.put("type", "1");

			data.add(subSet);
		}
		Collections.sort(data,new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				JSONObject sub1 = (JSONObject) o1;
				JSONObject sub2 = (JSONObject) o2;
				Integer subId1 = sub1.getInteger("id");
				Integer subId2 = sub2.getInteger("id");
				
				if(null!=subId1 && subId2!=null){					
					return subId1-subId2;
				}
				return 0;
			}
		});
		// 技术
		/*
		 * String techId = getTechId(obj); if(null!=techId){ JSONObject groupSet
		 * = new JSONObject(); groupSet.put("id", techId); groupSet.put("text",
		 * "技术"); groupSet.put("type", "0"); data.add(groupSet); }
		 */

		List<TPlDezySubjectgroup> subGroups = placementTaskDao.getSubjectGroupList(obj);
		for (TPlDezySubjectgroup subGroup : subGroups) {
			JSONObject groupSet = new JSONObject();
			groupSet.put("id", subGroup.getSubjectGroupId());
			groupSet.put("text", subGroup.getGroupName());
			groupSet.put("type", "0");
			groupSet.put("subjectIds", subGroup.getSubjectIds());
			data.add(groupSet);
		}
		return data;
	}

	@Override
	public int modifyClassName(JSONObject obj) {
		List<TPlDezyClass> classList = new ArrayList<TPlDezyClass>();
		String termInfo = obj.getString("termInfo");
		JSONArray data = obj.getJSONArray("data");

		String gradeId = obj.getString("useGrade");
		if (StringUtils.isEmpty(gradeId)) {
			PlacementTask pl = placementTaskDao.queryPlacementTaskById(obj);
			gradeId = pl.getUsedGrade();
		}
		for (Object object : data) {
			JSONObject cls = (JSONObject) object;
			TPlDezyClass dezyClass = new TPlDezyClass();
			dezyClass.setClassGroupId(cls.getString("classGroupId"));
			dezyClass.setPlacementId(obj.getString("placementId"));
			dezyClass.setSubjectGroupId(cls.getString("subjectGroupId"));
			dezyClass.setUsedGrade(gradeId);
			int clsType = cls.getIntValue("tclassType");
			dezyClass.setTclassType(clsType);
			String subjectId = (clsType == 6) ? "-999" : cls.getString("subjectId");
			dezyClass.setSubjectId(subjectId);
			dezyClass.setTclassId(cls.getString("tclassId"));
			dezyClass.setTclassName(cls.getString("tclassName"));
			dezyClass.setSchoolId(obj.getString("schoolId"));

			classList.add(dezyClass);
		}

		try {

			dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, classList);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	public List<JSONObject> getClassAll(JSONObject obj) {
		List<JSONObject> clsList = new ArrayList<JSONObject>();
		List<TPlDezyClass> classAll = placementTaskDao.getDezyClassList(obj);
		String subjectIds = obj.getString("subjectId");
		int isNeedSubShef = 0;
		if(obj.containsKey("isNeedSubShef")){
			isNeedSubShef = obj.getIntValue("isNeedSubShef");
		}
		List<String> subList = null;
		if (null != subjectIds) {
			subList = Arrays.asList(subjectIds.split(","));
		}

		for (TPlDezyClass dezyClass : classAll) {
			JSONObject cls = new JSONObject();
			cls.put("classGroupId", dezyClass.getClassGroupId());
			cls.put("subjectGroupId", dezyClass.getSubjectGroupId());
			cls.put("tclassId", dezyClass.getTclassId());
			cls.put("tclassType", dezyClass.getTclassType());
			cls.put("tclassName", dezyClass.getTclassName());
			cls.put("usedGrade", dezyClass.getUsedGrade());
			cls.put("subjectId", dezyClass.getSubjectId());
			if(isNeedSubShef==0){
				
				if (null == subList || !subList.contains(dezyClass.getSubjectId())) {
					continue;
				}
			}

			clsList.add(cls);
		}

		return clsList;
	}

	@Override
	public JSONObject getWishingDetail(JSONObject obj) {
		JSONObject result = new JSONObject();
		String wfId = obj.getString("wfId");
		String wfTermInfo = obj.getString("termInfo");
		Long schoolId = obj.getLong("schoolId");
		String usedGrade = obj.getString("usedGrade");
		String xn = wfTermInfo.substring(0, wfTermInfo.length() - 1);
		String placementId = obj.getString("placementId");
		// 获取学生志愿及人数
		try {
			// 获取年级的所有人数
			Integer numOfGradeStuds = 0;
			T_GradeLevel gl = T_GradeLevel
					.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
			String gradeName = njName.get(gl);
			// data.put("gradeName", gradeName);
			Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl, wfTermInfo);
			if (grade == null) {
				throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
			}
			List<Classroom> crs = commonDataService.getClassroomBatch(schoolId, grade.getClassIds(), wfTermInfo);
			if (crs == null) {
				throw new CommonRunException(-1, "无法获取年级下的班级信息，请联系管理员！");
			}
			for (Classroom cr : crs) {
				numOfGradeStuds += cr.getStudentAccountIdsSize();
			}

			Integer numOfWfStuds = 0;

			List<JSONObject> zhData = wishFillingThirdService.getZhStudentNumToThird(wfId, wfTermInfo, schoolId);
			if (null == zhData)
				return null;
			
			PlacementTask plaObj = null;
			if(placementId!=null){
				Map<String, Object> cxMap = new HashMap<String, Object>();
				cxMap.put("schoolId", schoolId);
				cxMap.put("placementId", placementId);
				cxMap.put("termInfo", wfTermInfo);
				plaObj = placementTaskDao.queryPlacementTaskById(cxMap );
			}
			//增加中走班逻辑，剔除固定开班人数
			boolean isMedium  = false;
			List<String> fixedSubjectGroupList = new ArrayList<String>();
			int fixedStuNum = 0;
			if(plaObj!=null&&plaObj.getPlacementType()==2){
				isMedium = true;
				JSONObject params = new JSONObject();
				params.put("placementType", 2);
				params.put("schoolId", schoolId);
				params.put("placementId", placementId);
				params.put("type", 1); // 中走班的三科组合；
				params.put("termInfo", plaObj.getTermInfo());
				List<OpenClassInfo> openClassInfos = placementTaskDao
						.queryOpenClassInfo(params);
				if (CollectionUtils.isNotEmpty(openClassInfos)) {
					for (OpenClassInfo openClassInfo : openClassInfos) {
						String subjectIdsStr = openClassInfo.getSubjectIdsStr();
						if (StringUtils.isNotEmpty(subjectIdsStr)) {
							fixedSubjectGroupList.add(subjectIdsStr);
						}
					}
				}
			}
			Map<String, Integer> stuInSub = new HashMap<String, Integer>();
			// 学考人数
			Map<String, Integer> stuInSubOfPro = new HashMap<String, Integer>();
			Map<String, String> subNameMap = getSubjectNameMap(obj, false);

			for (JSONObject wishing : zhData) {
				
				int stuInWishing = wishing.getIntValue("studentNum");
				String subjectIds = wishing.getString("subjectIds");
				if(fixedSubjectGroupList.size()>0&&fixedSubjectGroupList.contains(subjectIds)){
					fixedStuNum += stuInWishing;
					continue;
				}
				
				numOfWfStuds += stuInWishing;
				
				if (!StringUtils.isEmpty(subjectIds)) {
					List<String> curSubIds = new ArrayList<String>();
					curSubIds = Arrays.asList(subjectIds.split(","));
					// 选考人数映射
					for (String subjectId : curSubIds) {
						int totalStuInSub = (stuInSub.get(subjectId) == null) ? 0 : stuInSub.get(subjectId);
						totalStuInSub += stuInWishing;
						stuInSub.put(subjectId, totalStuInSub);
					}
					// 学考人数映射
					for (String subjectId : subNameMap.keySet()) {
						if (curSubIds.contains(subjectId)) {
							continue;
						}
						stuInSubOfPro.put(subjectId, (stuInSubOfPro.containsKey(subjectId))
								? (stuInSubOfPro.get(subjectId) + stuInWishing) : stuInWishing);
					}
				}
			}

			List<JSONObject> advancedOptions = new ArrayList<>();
			String techId = getTechId(obj);
			boolean wishingContainsTech = false;
			for (String subjectId : stuInSub.keySet()) {
				JSONObject subOptions = new JSONObject();
				if (subjectId.equals(techId) && !wishingContainsTech) {
					// continue; 前定二走一设置,不要技术
					wishingContainsTech = true;
				}
				subOptions.put("subjectId", subjectId);
				subOptions.put("subjectName", subNameMap.get(subjectId));
				subOptions.put("stuNum", stuInSub.get(subjectId));
				subOptions.put("stuNumOfPro", stuInSubOfPro.get(subjectId));
				advancedOptions.add(subOptions);
			}

			//科目排序
			Collections.sort(advancedOptions , new Comparator<JSONObject>(){
	            @Override
	            public int compare(JSONObject o1, JSONObject o2) {
	            	Integer v1 = Integer.parseInt(o1.getString("subjectId"));
	            	Integer v2 = Integer.parseInt(o2.getString("subjectId"));
	                return  v1.compareTo(v2);
	            }}
	        );
		      
			result.put("advancedOptions", advancedOptions);
			result.put("gradeName", gradeName);
			result.put("stuInGradeNum", numOfGradeStuds-fixedStuNum);
			result.put("stuInPartitionNum", numOfWfStuds);
			result.put("notInNum", numOfGradeStuds - numOfWfStuds-fixedStuNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * schoolId: 学校代码 termInfoId：学年学期 placementId：分班代码
	 * subjectId:科目代码（如果是行政班级代码为‘-999’，多个科目以‘，’分隔） tclassId:
	 * 班级代码(optional多个班级以‘，’分隔，不指定时，查找全部) stuName: 学生姓名（optional）
	 * 注：行政班会有冗余（两个科目组）
	 */
	@Override
	public List<JSONObject> getStuInClassDetail(JSONObject obj) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		String querySubjectId = obj.getString("subjectId");
		String queryTclassId = obj.getString("tclassId");
		String queryStuName = obj.getString("stuName").trim();

		try {
			Long schoolId = obj.getLong("schoolId");
			String termInfoId = obj.getString("termInfo");
			// 获取科目名称映射
			Map<String, String> subjectNameMap = getSubjectNameMap(obj);

			// 学生志愿
			List<TPlDezySubjectcompStudent> stuSubCompList = placementTaskDao.getSubjectcompStuList(obj);
			// 行政班+教学班
			List<TPlDezyClass> dezyClassList = placementTaskDao.getDezyClassList(obj);
			Map<String, TPlDezyClass> classIdMap = new HashMap<String, TPlDezyClass>();
			for (TPlDezyClass cls : dezyClassList) {
				// for(int i=start; i<end; i++){
				// TPlDezyClass cls = dezyClassList.get(i);
				classIdMap.put(cls.getTclassId(), cls);
				// }
			}

			// 获取学生账号列表及志愿下的学生
			Map<String, List<String>> stuInWishingMap = new HashMap<String, List<String>>();
			List<Long> accountIds = new ArrayList<Long>();
			for (TPlDezySubjectcompStudent stuSubComp : stuSubCompList) {
				String wishingId = stuSubComp.getSubjectCompId();
				String stuId = stuSubComp.getStudentId();
				accountIds.add(Long.parseLong(stuId));

				List<String> stuInWishingList = null;
				if (stuInWishingMap.containsKey(wishingId)) {
					stuInWishingList = stuInWishingMap.get(wishingId);
				} else {
					stuInWishingList = new ArrayList<String>();
					stuInWishingMap.put(wishingId, stuInWishingList);
				}
				stuInWishingList.add(stuId);
			}

			// 批量获取学生账户
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", schoolId);
			map.put("termInfoId", termInfoId);
			// stuAccounts = commonDataService.getAccountBatch(schoolId,
			// accountIds, termInfoId);
			List<Account> stuAccounts = commonDataService.getStudentList(schoolId, termInfoId, "");
			// stuAccounts = commonDataService.getStudentList(map);
			// School school = commonDataService.getSchoolById(schoolId,
			// termInfoId);
			// List<Account> stuAccounts =
			// commonDataService.getAllStudent(school, termInfoId);

			Map<Long, String> stuIdNameMap = new HashMap<Long, String>();
			for (Account account : stuAccounts) {
				stuIdNameMap.put(account.getId(), account.getName());
			}

			// 获取行政班下的志愿列表映射（ 及志愿Id与志愿名称映射）
			List<TPlDezySubjectcomp> subCompList = placementTaskDao.getSubjectcompList(obj);
			Map<String, List<TPlDezySubjectcomp>> subCompInClassMap = new HashMap<String, List<TPlDezySubjectcomp>>();
			Map<String, String> subCompIdNameMap = new HashMap<String, String>();
			for (TPlDezySubjectcomp subComp : subCompList) {
				String classId = subComp.getClassId();

				List<TPlDezySubjectcomp> subCompInClass = null;
				if (subCompInClassMap.containsKey(classId)) {
					subCompInClass = subCompInClassMap.get(classId);
				} else {
					subCompInClass = new ArrayList<TPlDezySubjectcomp>();
					subCompInClassMap.put(classId, subCompInClass);
				}
				subCompInClass.add(subComp);

				subCompIdNameMap.put(subComp.getSubjectCompId(), subComp.getCompName());
			}

			// 补全行政班下的学生信息(已过滤名字和班级查询条件)
			for (String classId : subCompInClassMap.keySet()) {
				TPlDezyClass dezyClass = classIdMap.get(classId);

				// 过滤查询科目
				if (querySubjectId != null && querySubjectId.split(",").length == 1 && !"-999".equals(querySubjectId)) {
					continue;
				}

				// 过滤教学班
				if (6 != dezyClass.getTclassType()) {
					continue;
				}

				// 刷选班级
				if (queryTclassId != null && queryTclassId.split(",").length == 1 && !queryTclassId.equals(classId)) {
					continue;
				}
				
				//循环行政班志愿信息
				List<TPlDezySubjectcomp> subCompLs = subCompInClassMap.get(classId);
				for (TPlDezySubjectcomp subComp : subCompLs) {
					String subCompId = subComp.getSubjectCompId();
					List<String> stuIds = stuInWishingMap.get(subCompId);

					for (String stuId : stuIds) {
						JSONObject cls = new JSONObject();
						cls.put("subjectGroupId", subCompId);
						cls.put("subjectGroupName", subCompIdNameMap.get(subCompId));

						String stuName = stuIdNameMap.get(Long.valueOf(stuId));

						// 过滤查询条件
						if (!StringUtils.isEmpty(queryStuName)) {
							if (null == stuName || !stuName.contains(queryStuName)) {
								continue;
							}
						}
						cls.put("stuName", stuName);

						cls.put("subjectName", "行政班");
						cls.put("tclassId", classId);
						cls.put("tclassName", dezyClass.getTclassName());
						cls.put("groundName", dezyClass.getGroundName());

						result.add(cls);
					}
				}
			}

			// 获取教学班下的志愿列表映射
			List<TPlDezyTclassSubcomp> tClassSubCompList = placementTaskDao.getTclassSubcompList(obj);
			Map<String, List<TPlDezyTclassSubcomp>> subCompInTClassMap = new HashMap<String, List<TPlDezyTclassSubcomp>>();
			for (TPlDezyTclassSubcomp subComp : tClassSubCompList) {
				String tclassId = subComp.getTclassId();
				// 过滤行政班
				if (subCompInClassMap.containsKey(tclassId)) {
					continue;
				}
				List<TPlDezyTclassSubcomp> tclassSubCompInClass = null;
				if (subCompInTClassMap.containsKey(tclassId)) {
					tclassSubCompInClass = subCompInTClassMap.get(tclassId);
				} else {
					tclassSubCompInClass = new ArrayList<TPlDezyTclassSubcomp>();
					subCompInTClassMap.put(tclassId, tclassSubCompInClass);
				}
				tclassSubCompInClass.add(subComp);
			}

			// 补全教学班下的学生信息(已过滤名字和班级查询条件)
			for (String tclassId : subCompInTClassMap.keySet()) { // 教学班
				List<TPlDezyTclassSubcomp> wishingList = subCompInTClassMap.get(tclassId);
				TPlDezyClass tclass = classIdMap.get(tclassId);
				String subjectId = tclass.getSubjectId();

				// 过滤科目
				if (querySubjectId != null && querySubjectId.split(",").length == 1 && !querySubjectId.equals(subjectId)
						&& !querySubjectId.equals("-999")) {
					continue;
				}

				// 刷选班级
				if (queryTclassId != null && queryTclassId.split(",").length == 1 && !queryTclassId.equals(tclassId)) {
					continue;
				}

				for (TPlDezyTclassSubcomp subComp : wishingList) { // 教学班-志愿
					String subjectCompId = subComp.getSubjectCompId();
					// 学生
					List<String> stuIdList = stuInWishingMap.get(subjectCompId);

					for (String stuId : stuIdList) { // 志愿-学生
						JSONObject cls = new JSONObject();
						cls.put("subjectGroupId", subjectCompId);
						cls.put("subjectGroupName", subCompIdNameMap.get(subjectCompId));

						String stuName = stuIdNameMap.get(Long.valueOf(stuId));

						// 过滤查询条件
						if (!StringUtils.isEmpty(queryStuName)) {
							if (null == stuName || !stuName.contains(queryStuName)) {
								continue;
							}
						}
						cls.put("stuName", stuName);

						cls.put("subjectName", subjectNameMap.get(subjectId));
						cls.put("tclassId", tclassId);
						cls.put("tclassName", tclass.getTclassName());
						cls.put("groundName", tclass.getGroundName());
						result.add(cls);
					}
				}
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取科目Id与科目名称映射
	 * 
	 * @param obj{keys:schoolId},
	 *            simple:全名
	 * @return
	 */
	private Map<String, String> getSubjectNameMap(JSONObject obj, boolean... simple) {
		Long schoolId = obj.getLong("schoolId");

		String termInfoId = obj.getString("termInfo");
		if (null == termInfoId) {
			termInfoId = commonDataService.getCurTermInfoId(schoolId);
		}
		School school = commonDataService.getSchoolById(schoolId, termInfoId);
		List<LessonInfo> lessonInfoList = commonDataService.getLessonInfoList(school, termInfoId);
		Map<String, String> courseMap = new HashMap<String, String>();
		for (LessonInfo lessonInfo : lessonInfoList) {
			String subjectName = (simple.length > 0 && true == simple[0]) ? lessonInfo.getSimpleName()
					: lessonInfo.getName();

			courseMap.put(String.valueOf(lessonInfo.getId()), subjectName);
		}
		return courseMap;
	}

	// 获取技术课Id
	private String getTechId(JSONObject obj) {
		Map<String, String> subMap = getSubjectNameMap(obj, true);
		for (String key : subMap.keySet()) {
			if (subMap.get(key).equals("技")) {
				return key;
			}
		}
		return "";
	}

	@Override
	public List<JSONObject> getWfListToThird(JSONObject obj) {
		// TODO Auto-generated method stub
		String gradeId = obj.getString("gradeId");

		if (StringUtils.isEmpty(gradeId)) {
			PlacementTask pl = placementTaskDao.queryPlacementTaskById(obj);
			gradeId = pl.getUsedGrade();
		}

		String termInfo = obj.getString("termInfo");
		try {
			List<JSONObject> oriWfList = wishFillingThirdService.getWfListToThird(gradeId, obj.getString("type"),
					obj.getLong("schoolId"));
			// 剔除不同学年学期选课
			if (CollectionUtils.isNotEmpty(oriWfList)) {
				Iterator<JSONObject> wfIter = oriWfList.iterator();
				while (wfIter.hasNext()) {
					JSONObject wf = wfIter.next();
					String wfTermInfo = wf.getString("wfTermInfo");
					if (termInfo.equals(null)) {
						break;
					}
					if (termInfo.equals(wfTermInfo)) {
						continue;
					}
					wfIter.remove();
				}
			}

			return oriWfList;
		} catch (Exception e) {
			logger.info("获取志愿出错！！");
		}
		return null;
	}

	@Override
	public int insertDzbPreSettings(final JSONObject obj) {
		// TODO Auto-generated method stub

		try {
			String termInfo = obj.getString("termInfo");
			String placementId = obj.getString("placementId");
			
			if(StringUtils.isNotEmpty(placementId)){
				String oldPlacementId = scheduleCommonDao.selectPlacementIdInSchedule(placementId);
				if(!StringUtils.isEmpty(oldPlacementId)){
					return 2;
				}
			}
			
			String gradeId = obj.getString("usedGrade");
			String wfId = obj.getString("wfId");
			String schoolId = obj.getString("schoolId");
			//obj.put("classNum", obj.get("avgClassNum"));
			obj.put("maxClassNum", obj.get("maxStuInClass"));
			//obj.put("classNum", obj.get("avgStuInClass"));

			
			Map<String, Object> dzbPreSetting = new HashMap<String, Object>();
			dzbPreSetting.putAll(obj);

			dzbPreSetting.put("classNum", obj.get("classNum"));
			dzbPreSetting.put("avgClassNum", obj.getIntValue("avgStuInClass"/*"avgStuInClass"*/));
			dzbPreSetting.put("maxClassNum", obj.getIntValue("maxStuInClass"));
			dzbPreSetting.put("placementId", placementId);
			dzbPreSetting.put("gradeId", gradeId);
			dzbPreSetting.put("wfId", wfId);
			dzbPreSetting.put("schoolId", schoolId);
			dzbPreSetting.put("placementAlg", 0);
			obj.put("classNum", obj.get("avgStuInClass"));
			// 平均/最大班额
			JSONObject subjectGroup = obj.getJSONObject("subjectGroup");
			int fixSumLesson = subjectGroup.getIntValue("fixedSumLesson");
			int gradeSumLesson = subjectGroup.getIntValue("totalWeekLessons");
			dzbPreSetting.put("fixedSumLesson", fixSumLesson);
			dzbPreSetting.put("gradeSumLesson", gradeSumLesson);

			// 传入分班参数
			obj.put("fixedSumLesson", fixSumLesson);
			obj.put("gradeSumLesson", gradeSumLesson);

			// 兼容数据库
			JSONObject techSettings = new JSONObject();
			techSettings.put("goClass", 0);
			dzbPreSetting.put("techSettings", techSettings);

			// 设置主表入库
			try {
				dezyPlacementTaskDao.insertDezyPreSetting(dzbPreSetting);
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("插入设置主表出错！");
				return -1;
			}

			// 设置科目课时
			List<TPlDezySubjectSet> subjectSetList = new ArrayList<TPlDezySubjectSet>();
			JSONArray subjects = subjectGroup.getJSONArray("subject");
			for (Object object : subjects) {
				JSONObject subject = (JSONObject) object;
				TPlDezySubjectSet subjectSet = new TPlDezySubjectSet();
				subjectSet.setPlacementId(placementId);
				subjectSet.setSchoolId(schoolId);
				subjectSet.setUsedGrade(gradeId);

				subjectSet.setSubjectGroupId("-999");
				subjectSet.setSubjectId(subject.getString("subjectId"));
				if (null != subject.getInteger("optLesson")) {
					subjectSet.setOptLesson(subject.getIntValue("optLesson"));
				}
				if (null != subject.getInteger("proLesson")) {
					subjectSet.setProLesson(subject.getIntValue("proLesson"));
					subjectSet.setIsProExist((subject.getIntValue("proLesson") > 0) ? 1 : -1);
				}

				subjectSetList.add(subjectSet);
			}

			// 设置科目高级设置(班级限额、班级分层)
			JSONObject advancedOptions = obj.getJSONObject("advancedOptions");
			JSONArray subMaxLessons = advancedOptions.getJSONArray("subMaxLessons");
			// 设置科目下班级限额
			next: for (TPlDezySubjectSet subjectSet : subjectSetList) {
				String subjectId = subjectSet.getSubjectId();
				for (Object object : subMaxLessons) {
					JSONObject subMaxLes = (JSONObject) object;
					if (subMaxLes.getString("subjectId").equals(subjectId)) {
						if (null != subMaxLes.getInteger("classNum")) {
							subjectSet.setClassLimit(subMaxLes.getIntValue("classNum"));
						}
						if (null != subMaxLes.getInteger("proClassNum")) {
							subjectSet.setProClassLimit(subMaxLes.getIntValue("proClassNum"));
						}
						if (null != subMaxLes.getInteger("stuNumOfPro")) {
							subjectSet.setStuNumOfPro(subMaxLes.getIntValue("stuNumOfPro"));
						}
						if (null != subMaxLes.getInteger("stuNum")) {
							subjectSet.setStuNum(subMaxLes.getIntValue("stuNum"));
						}
						if (null != subMaxLes.getInteger("teacherNum")) {
							subjectSet.setTeacherNum(subMaxLes.getIntValue("teacherNum"));
						}

						continue next;
					}
				}
			}

			// 传入分班参数
			obj.put("subsetList", subjectSetList);

			// 科目表入库
			try {
				dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, subjectSetList);
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("批量插入科目表出错！");
				return -1;
			}

			List<TPlDzbClassLevel> classLevelList = new ArrayList<TPlDzbClassLevel>();
			JSONArray classLevelSettings = advancedOptions.getJSONArray("classLevelSettings");
			for (Object object : classLevelSettings) {
				JSONObject classLevel = (JSONObject) object;
				TPlDzbClassLevel dzbClassLevel = new TPlDzbClassLevel();
				dzbClassLevel.setPlacementId(placementId);
				dzbClassLevel.setSchoolId(schoolId);
				dzbClassLevel.setClassId(classLevel.getString("classId"));
				if (null != classLevel.getInteger("classLevel")) {
					String classLevelName = "";
					switch (classLevel.getInteger("classLevel")) {
					case 1:
						classLevelName = "A层";
						break;
					case 2:
						classLevelName = "B层";
						break;
					case 3:
						classLevelName = "C层";
						break;
					default:
						break;
					}
					dzbClassLevel.setSubLevel(classLevel.getIntValue("classLevel"));
					dzbClassLevel.setSubLevelName(classLevelName);
				}

				classLevelList.add(dzbClassLevel);
			}
			// 传入分班参数
			obj.put("classLevelList", classLevelList);

			try {
				dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, classLevelList);
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("批量插入班级等级失败！");
				return -1;
			}

			setDivProc(placementId, "10", "插入设置成功->开始分班", 0);
			// 启用大走班分班算法
			new Thread(new Runnable() {

				@Override
				public void run() {
					//placementTaskService.startLargeDisClass(obj);
					placementTaskService.startLargeDivClass(obj);
				}
			}).start();

			// setDivProc(placementId, "100", "分班完成！", 0,"测试完成！！");
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("获取前端参数失败！");
			return -1;
		}

		return 0;
	}

	@Override
	public JSONObject getDzbPreSettings(JSONObject obj) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		
		String msg = "返回成功！";
		int retCode = 0;
		JSONObject advancedOptions = null;
		String type = obj.getString("type");

		try {
			long schoolId = obj.getLongValue("schoolId");
			String termInfoId = obj.getString("termInfo");
			String usedGrade = obj.getString("usedGrade");

			Map<String, String> subjectNameMap = getSubjectNameMap(obj, false);

			// 获取普通设置
			try {
				result = dezyPlacementTaskDao.getDzbPreSettings(obj);
				
				// 第一次进来,没设置
				if (null == result && type==null) {
					result = new JSONObject();
					retCode = 1;
					try {
						List<JSONObject> wishings = wishFillingThirdService.getWfListToThird(usedGrade, "2",
								obj.getLong("schoolId"));
						if (!CollectionUtils.isEmpty(wishings)) {
							retCode = 2;
							/*
							for (JSONObject wishing : wishings) {
								if (termInfoId.equals(wishing.get("wfTermInfo"))) {
									retCode = 2;
									break;
								}
							}*/
						}
					} catch (Exception e) {
						retCode = 1;
					}
					// 没有选课
					if (retCode == 1) {
						result.put("code", retCode);
						result.put("msg", "查询设置为空！");
						return result;
					}
				}else if(result==null&&type!=null&&type.equals("medium")){
					result = new JSONObject();
					retCode = 2;
					result.put("code", 2);
					result.put("msg", "查询设置为空！");
//					return result;
				}
				result.put("hasTech", "0");
				String wfId = result.getString("wfId");
				List<JSONObject> zhSubList = wishFillingThirdService.getZhSubjectListToThird(wfId, termInfoId, schoolId);
				if(CollectionUtils.isNotEmpty(zhSubList)){
					for(JSONObject zhSub : zhSubList){
						String zhName = zhSub.getString("zhName");
						if(null!=zhName && zhName.contains("技")){
							result.put("hasTech", "1");
							break;
						}
					}
				}
				
				result.put("classNum", result.get("totalClsNum"));
				// 设置普通设置选项的科目名称
				JSONObject subjectGroup = result.getJSONObject("subjectGroup") == null ? new JSONObject()
						: result.getJSONObject("subjectGroup");
				JSONArray subject = subjectGroup.getJSONArray("subject") == null ? new JSONArray()
						: subjectGroup.getJSONArray("subject");
				for (Object object : subject) {
					JSONObject sub = (JSONObject) object;
					sub.put("subjectName", subjectNameMap.get(sub.get("subjectId")));
				}

				// 设置高级选项的科目名称
				if (result.getJSONObject("advancedOptions") == null) {
					result.put("advancedOptions", new JSONObject());
				}
				advancedOptions = result.getJSONObject("advancedOptions");
				JSONArray subMaxLessons = advancedOptions.getJSONArray("subMaxLessons") == null ? new JSONArray()
						: advancedOptions.getJSONArray("subMaxLessons");
				for (Object object : subMaxLessons) {
					JSONObject subMaxLesson = (JSONObject) object;
					String subjectId = subMaxLesson.getString("subjectId");
					subMaxLesson.put("subjectName", subjectNameMap.get(subjectId));
				}

			} catch (Exception e) {
				e.printStackTrace();

				msg = "查询设置主表错误！";
				retCode = -1;
				logger.info(msg);
				throw new Exception();
			}

			// 获取高级设置
			try {
				JSONArray classLevelSettings = new JSONArray();
				List<TPlDzbClassLevel> clsLevelLs = dezyPlacementTaskDao.getDzbClassLevel(obj);
				Map<String, Integer> levelMap = new HashMap<String, Integer>();
				Map<String, String> clsLevelNameMap = new HashMap<String, String>();
				List<Long> ids = new ArrayList<Long>();
				boolean useDefault = false;

				// 第一次设置（采用默认设置）
				if (CollectionUtils.isEmpty(clsLevelLs)) {
					int gradeLevel = Integer
							.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, termInfoId.substring(0, 4)));
					Grade grade = commonDataService.getGradeByGradeLevel(schoolId, T_GradeLevel.findByValue(gradeLevel),
							termInfoId);
					ids = grade.getClassIds();

					useDefault = true;
				} else {// 非首次设定
					for (TPlDzbClassLevel clsLevel : clsLevelLs) {
						ids.add(Long.parseLong(clsLevel.getClassId()));
						levelMap.put(clsLevel.getClassId(), clsLevel.getSubLevel());
						clsLevelNameMap.put(clsLevel.getClassId(), clsLevel.getSubLevelName());
					}
				}

				// List<Classroom> crList =
				// commonDataService.getSimpleClassBatch(schoolId, ids,
				// termInfoId);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("schoolId", schoolId);
				map.put("termInfoId", termInfoId);
				map.put("usedGradeId", usedGrade);
				List<Classroom> crList = commonDataService.getClassList(map);

				// 设定班级层次
				for (Classroom cr : crList) {
					JSONObject cls = new JSONObject();
					cls.put("classId", cr.getId());
					cls.put("className", cr.getClassName());
					cls.put("classLevel", (useDefault) ? "1" : levelMap.get(String.valueOf(cr.getId())));
					cls.put("classLevelName", clsLevelNameMap.get(String.valueOf(cr.getId())));

					classLevelSettings.add(cls);
				}

				advancedOptions.put("classLevelSettings", classLevelSettings);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			logger.info("获取大走班设置出错！");
		}

		result.put("msg", msg);
		result.put("code", retCode);
		return result;
	}

	@Override
	public List<JSONObject> getDzbDivResult(JSONObject obj) {
		// TODO Auto-generated method stub
		List<JSONObject> divResult = null;
		List<JSONObject> result = new ArrayList<JSONObject>();

		String querySubs = obj.getString("subjectId");
		String classLevel = obj.getString("classLevel");

		// 查询科目
		List<String> querySubList = null;
		if (StringUtils.isNotEmpty(querySubs)) {
			querySubList = Arrays.asList(querySubs.split(","));
			obj.put("subjectIds", querySubList);
		}

		// 查询层次
		List<String> queryLevelList = null;
		if (StringUtils.isNotEmpty(classLevel)) {
			queryLevelList = Arrays.asList(classLevel.split(","));
		}

		try {
			divResult = dezyPlacementTaskDao.getDzbDivResult(obj);
			if (!CollectionUtils.isEmpty(divResult)) {
				Map<String, String> subNameMap = getSubjectNameMap(obj, false);
				for (JSONObject singleData : divResult) {
					JSONArray subject = singleData.getJSONArray("subject");
					JSONObject queryLevelData = new JSONObject();
					if (null == singleData.get("classLevel")) {
						singleData.put("classLevel", "1");
					}

					// 没有科目
					if (CollectionUtils.isEmpty(subject)) {
						continue;
					}

					// 剔选查询层次
					String clsLevel = singleData.getString("classLevel");
					if (CollectionUtils.isNotEmpty(queryLevelList) && !queryLevelList.contains(clsLevel)) {
						continue;
					}
					JSONArray subData = new JSONArray();
					queryLevelData.put("classLevel", clsLevel);
					queryLevelData.put("subject", subData);
					result.add(queryLevelData);

					Iterator<Object> subIter = subject.iterator();
					// 补全科目名称及班级数
					while (subIter.hasNext()) {
						Object object = subIter.next();

						JSONObject sub = (JSONObject) object;
						String subId = sub.getString("subjectId");
						if (CollectionUtils.isNotEmpty(querySubList) && !querySubList.contains(subId)) {
							subIter.remove();
							continue;
						}

						sub.put("subjectName", subNameMap.get(sub.get("subjectId")));

						// 补全班级数
						JSONArray classArray = sub.getJSONArray("classArray");
						if (!CollectionUtils.isEmpty(classArray)) {
							for (Object tmp : classArray) {
								JSONObject cls = (JSONObject) tmp;
								JSONArray tclassArray = cls.getJSONArray("tclassArray");
								if (null != tclassArray) {
									cls.put("classNum", tclassArray.size());
								}
							}
						}
						subData.add(object);
					}

				}
			}
		} catch (Exception e) {
			logger.info("查询分班数据库主表出错！");
		}

		return result;
	}

	@Override
	public List<JSONObject> getDzbStuClassInfoDetail(JSONObject obj) {
		// TODO Auto-generated method stub
		List<JSONObject> result = null;
		try {

			String schoolId = obj.getString("schoolId");
			String termInfo = obj.getString("termInfo");
			String usedGrade = obj.getString("usedGrade");

			// 查询参数
			String classType = obj.getString("classType");
			if("全部".equals(classType)){
				classType = "";
			}
			String tclassIds = obj.getString("tclassId");
			String stuName = obj.getString("stuName");
			PlacementType placementType = PlacementType.getEnumByCode(obj.getInteger("placementType"));

			List<String> tclassIdList = null;

			try {
				if (StringUtils.isNotEmpty(tclassIds)) {
					tclassIdList = Arrays.asList(tclassIds.split(","));
				}
				// 加入查询参数
				if (StringUtils.isNotEmpty(classType) && !"1".equals(classType) && !"0".equals(classType)
						&& !"-999".equals(classType)) {
					obj.put("tclassIdList", tclassIdList);
					// 查询科目
					if (Integer.parseInt(classType) >= 4) {
						obj.put("querySubjectId", classType);
					}
				}

				result = dezyPlacementTaskDao.queryDzbStuClassInfoDetail(obj);
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("查询（大走班）学生分班明细数据库失败！");
				throw new Exception();
			}

			// 补全学生姓名、班级和科目名称
			if (CollectionUtils.isNotEmpty(result)) {
				Map<String, String> subNameMap = getSubjectNameMap(obj, false);
				List<Long> stuIds = new ArrayList<Long>();

				// 查询年级下的班级
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("schoolId", schoolId);
				map.put("classTypeId ", 0);
				map.put("termInfoId", termInfo);
				map.put("usedGradeId", /* grade.getId() */usedGrade);
				List<Classroom> clsList = commonDataService.getClassList(map);
				Map<Long, Long> stuIdClsIdMap = new HashMap<Long, Long>();
				Map<Long, String> clsNameMap = new HashMap<Long, String>();
				for (Classroom cr : clsList) {
					List<Long> stuIdList = /* cr.getStudentIds() */cr.getStudentAccountIds();
					if (CollectionUtils.isNotEmpty(stuIdList)) {
						stuIds.addAll(stuIdList);
					} else {
						continue;
					}
					try {

						for (Long stuId : stuIdList) {
							stuIdClsIdMap.put(stuId, cr.getId());
						}

						clsNameMap.put(cr.getId(), cr.getClassName());
					} catch (Exception e) {
						logger.info("基础数据库中没有该班级");
					}
				}
				List<Account> stuList = commonDataService.getAccountBatch(Long.parseLong(schoolId), stuIds, termInfo);

				// 姓名映射
				Map<Long, String> stuNameMap = new HashMap<Long, String>();
				for (Account account : stuList) {
					stuNameMap.put(account.getId(), account.getName());
				}

				// 定二走一,获取行政班
				List<TPlDezyTclassfrom> tclassFromList = placementTaskDao.getTclassFromList(obj);
				Map<String, TPlDezyTclassfrom> tclassFromMap = new HashMap<String, TPlDezyTclassfrom>();
				for (TPlDezyTclassfrom tclass : tclassFromList) {
					tclassFromMap.put(tclass.getTclassId(), tclass);
				}
				List<TPlDezyClass> dezyClsList = placementTaskDao.getDezyClassList(obj);
				Map<String, String> dezyClsNameMap = new HashMap<String, String>();
				for (TPlDezyClass dezyClass : dezyClsList) {
					if (6 == dezyClass.getTclassType()) {
						dezyClsNameMap.put(dezyClass.getTclassId(), dezyClass.getTclassName());
					}
				}

				// 补全学生姓名、班级及科目名称
				Iterator<JSONObject> resultIter = result.iterator();
				while (resultIter.hasNext()) {
					JSONObject singleResult = resultIter.next();
					String studentId = singleResult.getString("studentId");
					String adminClassId = singleResult.getString("classId");
					
					// 补全班级信息
					Long clsId = stuIdClsIdMap.get(Long.parseLong(studentId));
					singleResult.put("oriClassId", clsId);
					singleResult.put("oriClassName", clsNameMap.get(clsId));

					// 剔选行政班
					if (CollectionUtils.isNotEmpty(tclassIdList) && null != classType && "1".equals(classType)
							&& !tclassIdList.contains(String.valueOf(clsId))) {
						resultIter.remove();
						continue;
					}
					
					// 补全学生姓名/剔选学生
					String optStuName = stuNameMap.get(Long.parseLong(studentId));
					if(StringUtils.isBlank(optStuName) || (!StringUtils.isBlank(stuName)&& !optStuName.contains(stuName))){
						resultIter.remove();
						continue;
					}
					singleResult.put("stuName", optStuName);
					
					//行政班级信息
					if(placementType == PlacementType.BIG_GO_CLASS){
						singleResult.put("className", singleResult.get("oriClassName"));
					}else{
						singleResult.put("className", dezyClsNameMap.get(adminClassId));
					}

					// 补全科目名称
					JSONArray tclassGroup = singleResult.getJSONArray("tclassGroup");
					if (CollectionUtils.isNotEmpty(tclassGroup)) {
						String searchTclassId = null;
						for (Object object : tclassGroup) {
							JSONObject tclass = (JSONObject) object;
							tclass.put("subjectName", subNameMap.get(tclass.get("subjectId")));
							searchTclassId = tclass.getString("tclassId");
						}
						// 定二走一行政班
						TPlDezyTclassfrom tclassFrom = tclassFromMap.get(searchTclassId);
						if (null != tclassFrom) {
							String oriClassId = tclassFrom.getClassId();

							// 剔选定二走一行政班
							if (("-999".equals(classType) || "6".equals(classType))
									&& CollectionUtils.isNotEmpty(tclassIdList) && !tclassIdList.contains(oriClassId)
									&& dezyClsNameMap.keySet().containsAll(tclassIdList)) {
								resultIter.remove();
								continue;
							}
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("获取（大走班）学生分班明细失败！");
		}
		return result;
	}

	@Override
	public List<JSONObject> getDzbStuQueryParams(JSONObject obj) {
		List<JSONObject> data = new ArrayList<JSONObject>();
		String msg = "";
		Long schoolId = null;
		String termInfo = null;
		// String placementId = null;
		String usedGrade = null;

		try {
			// 获取前台参数
			try {
				schoolId = obj.getLong("schoolId");
				termInfo = obj.getString("termInfo");
				// placementId = obj.getString("placementId");
				usedGrade = obj.getString("usedGrade");
			} catch (Exception e) {
				logger.info("获取前台参数错误！！");
				throw new Exception();
			}

			// 查询教学班
			List<TPlDezyClass> clsList = null;
			try {
				clsList = placementTaskDao.getDezyClassList(obj);
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("查询班级出错！");
				throw new Exception();
			}

			// 查询行政班
			try {
				List<Grade> gids = new ArrayList<Grade>();
				String gLevel = commonDataService.ConvertSYNJ2NJDM(usedGrade, termInfo.substring(0, 4));
				Grade useGrade = commonDataService.getGradeByGradeLevel(obj.getLong("schoolId"),
						T_GradeLevel.findByValue(Integer.parseInt(gLevel)), termInfo);
				School sch = commonDataService.getSchoolById(schoolId, termInfo);
				gids.add(useGrade);

				// 加入行政班
				List<Classroom> crList = commonDataService.getSimpleClassList(sch, gids, termInfo);
				if (CollectionUtils.isNotEmpty(crList)) {
					JSONObject clsOptions = new JSONObject();
					List<JSONObject> clsOptionsList = new ArrayList<JSONObject>();
					clsOptions.put("classType", 1);
					clsOptions.put("class", clsOptionsList);

					for (Classroom cr : crList) {
						JSONObject cls = new JSONObject();
						cls.put("classId", cr.getId());
						cls.put("className", cr.getClassName());
						clsOptionsList.add(cls);
					}
					data.add(clsOptions);
				}

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("查询基础数据库出错！！！");
				throw new Exception();
			}

			// 处理返回教学班
			if (CollectionUtils.isNotEmpty(clsList)) {
				JSONObject optCls = new JSONObject();
				List<JSONObject> optClsList = new ArrayList<JSONObject>();
				optCls.put("class", optClsList);
				optCls.put("classType", 2);

				JSONObject proCls = new JSONObject();
				List<JSONObject> proClsList = new ArrayList<JSONObject>();
				proCls.put("class", proClsList);
				proCls.put("classType", 3);

				for (TPlDezyClass cls : clsList) {
					JSONObject clsMap = new JSONObject();
					clsMap.put("classId", cls.getTclassId());
					clsMap.put("className", cls.getTclassName());
					switch (cls.getTclassLevel()) {
					case 0:// 行政班

						break;
					case 1:// 选考班
						optClsList.add(clsMap);
						break;
					case 2:// 学考班
						proClsList.add(clsMap);
						break;
					default:
						break;
					}
				}
				data.add(proCls);
				data.add(optCls);
			}

		} catch (Exception e) {
			logger.info(msg);
		}

		return data;
	}

	@Override
	public List<JSONObject> getDzbDivQueryParams(Map<String, Object> obj) {
		try {
			List<JSONObject> clsList = dezyPlacementTaskDao.getDzbDivQueryParams(obj);

			if (CollectionUtils.isNotEmpty(clsList)) {

				for (JSONObject cls : clsList) {
					try {
						String subLevelName = null;
						switch (cls.getIntValue("subLevel")) {
						case 1:
							subLevelName = "A层";
							break;
						case 2:
							subLevelName = "B层";
							break;
						case 3:
							subLevelName = "C层";
							break;
						}
						cls.put("classLevelName", subLevelName);
					} catch (Exception e) {
						logger.info("班级等级转换失败！！");
					}

					// 补充科目名称
					JSONArray subjects = cls.getJSONArray("subject");
					if (CollectionUtils.isNotEmpty(subjects)) {
						JSONObject queryParams = new JSONObject();
						queryParams.put("schoolId", obj.get("schoolId"));
						queryParams.put("termInfo", obj.get("termInfo"));

						Map<String, String> subNameMap = getSubjectNameMap(queryParams, false);
						for (Object object : subjects) {
							JSONObject subject = (JSONObject) object;
							String subjectId = subject.getString("subjectId");
							subject.put("subjectName", subNameMap.get(subjectId));
						}
					}

				}
			}
			return clsList;
		} catch (Exception e) {
			logger.info("查询分班预览查询参数出错！！");
		}
		return null;
	}

	@Override
	public List<JSONObject> getDzbTeachingResource(JSONObject obj) {
		// TODO Auto-generated method stub
		List<JSONObject> data = null;
		try {
			try {
				data = dezyPlacementTaskDao.queryDzbTeachingResource(obj);
				
			} catch (Exception e) {
				logger.info("查询师资配比表（数据库）失败！！");
				throw new Exception();
			}

			try {
				if (CollectionUtils.isNotEmpty(data)) {
					Map<String, String> subNameMap = getSubjectNameMap(obj, false);
					obj.put("queryClassType", 6);
					//List<TPlDezyClass>clsList = placementTaskDao.getDezyClassList(obj);
					String termInfo = obj.getString("termInfo");
					School school = commonDataService.getSchoolById(obj.getLongValue("schoolId"), termInfo);
					List<Grade> gradeList =  commonDataService.getGradeList(school, termInfo);
					Grade useGrade = null;
					for(Grade grade : gradeList){
						String synj = commonDataService.ConvertNJDM2SYNJ(String.valueOf(grade.getCurrentLevel().getValue()), termInfo.substring(0,4));
						//synj = commonDataService.ConvertNJDM2RXND(String.valueOf(grade.getCurrentLevel().getValue()), termInfo.substring(0,4));
						//commonDataService.ConvertSYNJ2NJDM(obj.getString("usedGrade"), termInfo.substring(0,4));
						if(synj!=null && synj.equals(obj.getString("usedGrade"))){
							useGrade = grade;
							break;
						}
					}
					
					//计算场地使用情况（班级Id和场地Id一致）
					int clsNum = useGrade.getClassIdsSize();
					List<Grade> queryGradeList = new ArrayList<Grade>();
					queryGradeList.add(useGrade);
					List<Classroom> crList = commonDataService.getSimpleClassList(school, queryGradeList, termInfo);
					
					List<String> crIds = new ArrayList<String>();
					for(Classroom cr : crList) {
						crIds.add(String.valueOf(cr.getId()));
					}
					
					Map<String,Integer> subjectIdAddedGroundMap = null;
					try {
						List<JSONObject> subjectIdGroundIdMap = dezyPlacementTaskDao.selectGroundIdBysubjectIds(obj);
						if(!CollectionUtils.isEmpty(subjectIdGroundIdMap)) {
							subjectIdAddedGroundMap = new HashMap<String,Integer>();
							
							//新增教学场地
							Map<String,List<String>> subExtraGroundIdsMap = new HashMap<String,List<String>>();
							for(JSONObject map : subjectIdGroundIdMap) {
								String subjectId = map.getString("subjectId");
								String groundId = map.getString("groundId");
								
								List<String> extraGroundsInSub = subExtraGroundIdsMap.get(subjectId);
								if(null==extraGroundsInSub){
									extraGroundsInSub = new ArrayList<String>();
									subExtraGroundIdsMap.put(subjectId, extraGroundsInSub);
								}								
								
								//使用的是原有的教学场所
								if(crIds.contains(groundId) ||
										extraGroundsInSub.contains(groundId)) {
									continue;
								}
								extraGroundsInSub.add(groundId);
								
								//额外增加的教学场所
								Integer addGroundNum = subjectIdAddedGroundMap.get(subjectId);
								if(null==addGroundNum) {
									addGroundNum = 0;
								}
								subjectIdAddedGroundMap.put(subjectId, ++addGroundNum);
							}
						}
					
					} catch (Exception e) {
						logger.info(e.getMessage());
						logger.info("查询科目-场地信息错误！");
					}
					
					
					for (JSONObject subject : data) {
						// 补充科目名称
						String subjectId = subject.getString("subjectId");
						subject.put("subjectName", subNameMap.get(subjectId));

						// 补充同时最大开班数、科目开班数
						JSONArray classOptions = subject.getJSONArray("classOptions");
						if (CollectionUtils.isNotEmpty(classOptions)) {
							int upCls = 0, botCls = Integer.MAX_VALUE, grdNum = 0;
							for (Object object : classOptions) {
								JSONObject classOption = (JSONObject) object;

								try {
									Object upClassLimit = classOption.remove("upClassLimit");
									Object bottomClassLimit = classOption.remove("bottomClassLimit");
									Object groundNum = classOption.remove("groundNum");

									if (null != upClassLimit) {
										upCls = Math.max(upCls, (Integer) upClassLimit);
									}
									if (null != bottomClassLimit) {
										botCls = Math.min(botCls, (Integer) bottomClassLimit);
									}
									if (null != groundNum) {
										grdNum += (Long) groundNum;
									}
									
								} catch (Exception e) {
									logger.info("获取班级人数出错！！");
								}

								Integer type = null;
								try {
									type = classOption.getInteger("type");
									if (null == type) {
										continue;
									}
								} catch (Exception e) {
									logger.info("获取当前的学选科目类型出错！！");
								}

								// 补充（科目开班数）字段
								JSONObject subClassOptions = subject.getJSONObject("subClassOptions");
								if (subClassOptions == null) {
									subClassOptions = new JSONObject();
									subject.put("subClassOptions", subClassOptions);
								}

								// 补充（同时最大开班数）字段
								JSONObject maxClassOptions = subject.getJSONObject("maxClassOptions");
								if (maxClassOptions == null) {
									maxClassOptions = new JSONObject();
									subject.put("maxClassOptions", maxClassOptions);
								}								
								
								Integer classNum = classOption.getInteger("classNum");
								Integer levelClassNum = classOption.getInteger("levelClassNum");

								String optionType = null;
								String otherType = null;
								int subSum = 0, maxSum = 0;
								switch (type) {
								case 0: // 行政班
									break;
								case 1: // 选考
									optionType = "optClassNum";
									otherType = "proClassNum";
									break;
								case 2: // 学考
									optionType = "proClassNum";
									otherType = "optClassNum";									
									default:
										break;	
								}

								subSum = (null == classNum) ? 0 : classNum;
								maxSum = (null == levelClassNum) ? 0 : levelClassNum;

								if (null != maxClassOptions.get(otherType)) {
									maxSum = Math.max(maxSum, maxClassOptions.getIntValue(otherType));
								}
								if (null != subClassOptions.get(otherType)) {
									subSum += subClassOptions.getIntValue(otherType);
								}

								maxClassOptions.put(optionType, levelClassNum);
								maxClassOptions.put("totalClassNum", maxSum);
								maxClassOptions.remove(null);
								
								subClassOptions.remove(null);
								subClassOptions.put(optionType, classNum);
								subClassOptions.put("totalClassNum", subSum);
							}

							subject.put("upClassLimit", upCls);
							subject.put("bottomClassLimit", botCls);
							subject.put("groundNum", grdNum);
							Integer addedGroundNum = subjectIdAddedGroundMap.get(subjectId);//= grdNum -  clsNum;
							if (addedGroundNum == null || 0==addedGroundNum) {
								subject.put("addedGround", "无");
							}else{
								subject.put("addedGround", addedGroundNum);
							}
						}
						subject.remove("classOptions");
					}
				}
			} catch (Exception e) {
				logger.info("补充师资配比表数据出错！！");
			}
		} catch (Exception e) {
			logger.info("获取师资配比表出错！！");
		}
		return data;
	}

	@Override
	public List<JSONObject> getDzbMainTable(JSONObject obj) {
		// TODO Auto-generated method stub
		List<JSONObject> data = null;
		try {
			try {
				String querySubId = obj.getString("subjectId");
				if (StringUtils.isNotEmpty(querySubId) && querySubId.split(",").length == 1) {
					obj.put("querySubjectId", querySubId);
				}
				String queryClassLevel = obj.getString("classLevel");
				if (StringUtils.isNotEmpty(queryClassLevel)) {
					obj.put("queryClassLevel", queryClassLevel);
				}
				
				data = dezyPlacementTaskDao.queryDzbPreview(obj);
				
				//data = dezyPlacementTaskDao.queryDzbMainTable(obj);
			} catch (Exception e) {
				logger.info("查询班级主表失败！！！");
				e.printStackTrace();
				throw new Exception();
			}
		
			
			//这个if内的参数做个参考
			if (CollectionUtils.isNotEmpty(data)) {
				Map<String, String> courseMap = getSubjectNameMap(obj, false);
				for (JSONObject classLevel : data) {
					JSONArray subList = classLevel.getJSONArray("subject");
					// 补全科目名称
					for (Object object : subList) {
						JSONObject subject = (JSONObject) object;
						String subjectId = subject.getString("subjectId");
						subject.put("subjectName", courseMap.get(subjectId));

						// 补全班级人数
						JSONArray tclss = subject.getJSONArray("class");
						for (Object tmp : tclss) {
							JSONObject cls = (JSONObject) tmp;
							if (cls.get("groundName") == null) {
								cls.put("groundName", "");
							}
							// JSONArray subComps =
							// cls.getJSONArray("subComps");
							// Integer totalNum = 0;
							// for(Object comp : subComps){
							// JSONObject subComp = (JSONObject)comp;
							// totalNum +=
							// subComp.getInteger("stuInTclassComp");
							// }

							// cls.put("totalNum", totalNum);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.info("内部错误，获取分班主表失败！！");
			e.printStackTrace();
		}

		return data;
	}

}
