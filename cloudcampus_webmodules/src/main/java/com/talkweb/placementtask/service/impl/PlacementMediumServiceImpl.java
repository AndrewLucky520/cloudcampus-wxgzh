package com.talkweb.placementtask.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.dao.PlacementImportDao;
import com.talkweb.placementtask.dao.PlacementTaskDao;
import com.talkweb.placementtask.domain.ClassInfo;
import com.talkweb.placementtask.domain.OpenClassInfo;
import com.talkweb.placementtask.domain.OpenClassTask;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.StudentInfo;
import com.talkweb.placementtask.domain.StudentbaseInfo;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlMediumClasslevel;
import com.talkweb.placementtask.domain.TPlMediumSettings;
import com.talkweb.placementtask.domain.TPlMediumSubjectSet;
import com.talkweb.placementtask.domain.TPlMediumZhSet;
import com.talkweb.placementtask.domain.TeachingClassInfo;
import com.talkweb.placementtask.service.PlacementMediumService;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.medium.MediumDivOperation;
import com.talkweb.placementtask.utils.div.medium.dto.MediumClassData;
import com.talkweb.placementtask.utils.div.medium.dto.MediumFixedClassInfo;
import com.talkweb.placementtask.utils.div.medium.dto.MediumWishSubjectGroup;
import com.talkweb.scoreManage.service.impl.ScoreManageAPIServiceImpl;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.mapper.IWfInfoMapper;
import com.talkweb.wishFilling.service.impl.WishFillingThirdServiceImpl;
import com.talkweb.wishFilling.vo.WfInfoVo;

@Service
public class PlacementMediumServiceImpl implements PlacementMediumService{
	Logger logger = LoggerFactory.getLogger(PlacementMediumServiceImpl.class);
	
	@Autowired
	private PlacementTaskDao placementTaskDao;
	
	@Autowired
	private PlacementImportDao placementImportDao;
	
	@Autowired
	private ScoreManageAPIServiceImpl scoreManageAPIService;
	
	@Autowired
	private WishFillingSetDao wishFillingSetDao;
	
	@Autowired
	private WishFillingThirdServiceImpl wishFillingThirdService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	
	@Autowired
	private IWfInfoMapper  iWfInfoMapper;
	
	@Override
	public JSONObject newqueryScoreList(String schoolId,String termInfo,String placementId) {
		JSONObject data = new JSONObject();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("placementId", placementId);
		map.put("termInfo", termInfo);
		map.put("schoolId",schoolId);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(map);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		Integer placemrntType = pl.getPlacementType();
		if(placemrntType!=2){
			throw new CommonRunException(-1, "分班类型不是中走班");
		}
		String usedGrade = pl.getUsedGrade();
		List<String> classIds=new ArrayList<String>();
		List<String> subjectIds=new ArrayList<String>();
		List<JSONObject> list = scoreManageAPIService.getScoreIdAndNameList(termInfo, schoolId, usedGrade, classIds, subjectIds);
		JSONObject nullScore = new JSONObject();
		nullScore.put("examId", "");
		nullScore.put("examName", "");
		nullScore.put("examTermInfo", "");
		list.add(0, nullScore);
		data.put("list", list);
		List<TPlMediumSettings> tPlMediumSettingsList = placementTaskDao.getTPlMediumSettings(map);
		Integer ruleCode =1;
		String examId = "";
		String examTermInfo="";
		if(tPlMediumSettingsList.size()>0){
			TPlMediumSettings tPlMediumSettings = tPlMediumSettingsList.get(0);
			ruleCode = tPlMediumSettings.getRuleCode();
			examId = tPlMediumSettings.getExamId();
			examTermInfo = tPlMediumSettings.getExamTermInfo();
		}
		data.put("examId", examId);
		data.put("examTermInfo", examTermInfo);
		data.put("ruleCode", ruleCode);
		
		return data;
	}
	
	
	@Override
	public List<JSONObject> QueryZhInfoByWfId(String schoolId,String termInfo,String placementId,String wfId){
		List<JSONObject> zhSetList = new ArrayList<JSONObject>();
		Map<String,JSONObject> zhSetMap = new HashMap<String,JSONObject>();
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("wfId", wfId);
		String schoolYear = termInfo.substring(0, termInfo.length()-1);
		param.put("schoolYear", schoolYear);
		param.put("termInfo", termInfo);
		int totalNum = 0;
		JSONObject wfObj = wishFillingSetDao.getTWfWfinfo(param);
		String wfWay = wfObj.getString("wfWay");
		String isByElection = wfObj.getString("isByElection");
		List<JSONObject> staticZhs = new ArrayList<JSONObject>();
		if("0".equals(wfWay)&& "1".equals(isByElection)){
			param.put("zhWay", 1);
			staticZhs = wishFillingSetDao.getByStaticListByZh(param);
			totalNum = wishFillingSetDao.getByZhTotalStudentNum(param);
		}else{
			staticZhs = wishFillingSetDao.getStaticListByZh(param);
			totalNum = wishFillingSetDao.getZhTotalStudentNum(param);
		}
		for(JSONObject staticZh:staticZhs){
			String zhName = staticZh.get("zhName").toString();
			Long studentNum = (Long) staticZh.get("studentNum");
			JSONObject zhInfo = new JSONObject();
			zhInfo.put("compName", zhName);
			zhInfo.put("compNum", studentNum);
			zhInfo.put("numOfOpenClasses", 0);
			zhInfo.put("numOfStuds", 0);
			String compFrom = "";
			for(int i = 0;i<zhName.length();i++){
				String subjectId = "";
				String subjectName = zhName.substring(i, i+1);
				switch(subjectName){
				case "政":
					subjectId = "4";
					break;
				case "史":
					subjectId = "5";
					break;
				case "地":
					subjectId = "6";
					break;
				case "物":
					subjectId = "7";
					break;
				case "化":
					subjectId = "8";
					break;
				case "生":
					subjectId = "9";
					break;
				case "技":
					subjectId = "19";
					break;
				default:
					throw new CommonRunException(-1, "科目名不正确");
				}
				compFrom = compFrom+subjectId+",";
			}
			compFrom= compFrom.substring(0, compFrom.length()-1);
			zhInfo.put("compFrom", compFrom);
			zhSetMap.put(zhName, zhInfo);
		}

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("termInfo", termInfo);
		map.put("placementId", placementId);
		map.put("schoolId", schoolId);
		List<TPlMediumZhSet> tPlMediumZhSetList = placementTaskDao.getTPlMediumZhSet(map);
		for(TPlMediumZhSet tPlMediumZhSet:tPlMediumZhSetList){
			String compName = tPlMediumZhSet.getCompName();
			Integer numOfOpenClasses = tPlMediumZhSet.getNumOfOpenClasses();
			Integer numOfStuds = tPlMediumZhSet.getNumOfStuds();
			if(zhSetMap.containsKey(compName)){
				JSONObject zhSetInfo = zhSetMap.get(compName);
				zhSetInfo.put("numOfOpenClasses", numOfOpenClasses);
				zhSetInfo.put("numOfStuds", numOfStuds);
				zhSetMap.put(compName, zhSetInfo);
			}else{
				//throw new CommonRunException(-1, "选科组合信息有误");
			}
		}
		for(Map.Entry<String,JSONObject> entry:zhSetMap.entrySet()){
			JSONObject zhSet = entry.getValue();
			zhSetList.add(zhSet);
		}
		return zhSetList;
	}
	
	@Override
	public JSONObject GetMediumPreSetting(String schoolId,String termInfo,String placementId,String wfId){
		JSONObject data = new JSONObject();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("placementId", placementId);
		map.put("termInfo", termInfo);
		map.put("schoolId",schoolId);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(map);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		Integer placemrntType = pl.getPlacementType();
		if(placemrntType!=2){
			throw new CommonRunException(-1, "分班类型不是中走班");
		}
		String placementName = pl.getPlacementName();
		String usedGrade = pl.getUsedGrade();
		List<TPlMediumSettings> tPlMediumSettingsList = placementTaskDao.getTPlMediumSettings(map);
		Integer maxClassNum = 0;
		Integer gradeSumLesson = 0;
		Integer fixedSumLesson = 0;
		if(tPlMediumSettingsList.size()>0){
			TPlMediumSettings tPlMediumSettings = tPlMediumSettingsList.get(0);
			maxClassNum =  tPlMediumSettings.getMaxClassNum();
			gradeSumLesson = tPlMediumSettings.getGradeSumLesson();
			fixedSumLesson = tPlMediumSettings.getFixedSumLesson();
		}
		data.put("maxClassNum", maxClassNum);
		data.put("gradeSumLesson", gradeSumLesson);
		data.put("fixedSumLesson", fixedSumLesson);
		data.put("usedGrade", usedGrade);
		data.put("placementId", placementId);
		data.put("placementName", placementName);
		
		
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("wfId", wfId);
		param.put("termInfo", termInfo);
		List<JSONObject> subjectInfoList = wishFillingSetDao.getSubjectListByTb(param);
		JSONObject wfObj = wishFillingSetDao.getTWfWfinfo(param);
		String isByElection = wfObj.getString("isByElection");
		
		List<JSONObject> classlevelList = new ArrayList<JSONObject>();
		Map<String,JSONObject> classlevelTemporary = new HashMap<String,JSONObject>();
		List<TPlMediumClasslevel> tPlMediumClasslevelList = placementTaskDao.getTPlMediumClasslevel(map);
		//TODO 需要再加一个条件，只有组合下面有人才显示出来，通过getZhSubject查询是查询所有可能的组合
		//第一次进来才会出现这种情况，的时候，将所有组合放到一个默认组里面返回给前端
		if(tPlMediumClasslevelList.size()==0||tPlMediumClasslevelList ==null){
			List<JSONObject> ZhSubjectList= wishFillingSetDao.getZhSubject(param);
			for(JSONObject ZhSubject:ZhSubjectList){
				String zhWay = ZhSubject.get("zhWay").toString();
				if(("1".equals(isByElection)&&"1".equals(zhWay))||"0".equals(zhWay)){
					String subLevelName = "A层";
					Integer subLevel = 1;
					JSONObject classlevel = new JSONObject();
					classlevel.put("subLevelName", subLevelName);
					classlevel.put("subLevel", subLevel);
					String compFrom = ZhSubject.get("subjectIds").toString();
					String compName = ZhSubject.get("zhName").toString();
					classlevel.put("compFrom", compFrom);
					classlevel.put("compName", compName);
					classlevelList.add(classlevel);
				}
			}
		}else{
			//如果已经有设置项就取设置中的信息
			for(TPlMediumClasslevel tPlMediumClasslevel:tPlMediumClasslevelList){
				String subLevelName = tPlMediumClasslevel.getSubLevelName();
				Integer subLevel = tPlMediumClasslevel.getSubLevel();
				String compFrom = tPlMediumClasslevel.getCompFrom();
				String compName ="";
				List<String> subjectIdList = Arrays.asList(compFrom.split(","));
				for(String subjectId:subjectIdList){
					switch(subjectId){
					case "4":
						compName +="政";
						break;
					case "5":
						compName +="史";
						break;
					case "6":
						compName +="地";
						break;
					case "7":
						compName +="物";
						break;
					case "8":
						compName +="化";
						break;
					case "9":
						compName +="生";
						break;
					case "19":
						compName +="技";
						break;
					default:
						break;
					}
				}	
				if(!classlevelTemporary.containsKey(compFrom)){
					JSONObject classlevel = new JSONObject();
					classlevel.put("subLevelName", subLevelName);
					classlevel.put("subLevel",subLevel);
					classlevel.put("compFrom", compFrom);
					classlevel.put("compName", compName);
					classlevelTemporary.put(compFrom, classlevel);
				}else{
					JSONObject classlevel = classlevelTemporary.get(compFrom);
					classlevel.put("subLevelName", subLevelName);
					classlevel.put("subLevel",subLevel);
					classlevel.put("compFrom", compFrom);
					classlevel.put("compName", compName);
					classlevelTemporary.put(compFrom, classlevel);
				}
				
			}
			for(Map.Entry<String,JSONObject> entry:classlevelTemporary.entrySet()){
				JSONObject classlevel = entry.getValue();
				classlevelList.add(classlevel);
			}
		}
		data.put("classlevelList", classlevelList);
		
		
		List<JSONObject> subjectSetList = new ArrayList<JSONObject>();
		List<TPlMediumSubjectSet> tPlMediumSubjectSetList = placementTaskDao.getTPlMediumSubjectSet(map);
		for(JSONObject subjectInfo:subjectInfoList){
			String subjectId = subjectInfo.get("subjectId").toString();
			String subjectName = "";
			Integer optLesson = 0;
			Integer proLesson = 0;
			for(TPlMediumSubjectSet tPlMediumSubjectSet:tPlMediumSubjectSetList){
				String subjectId2 = tPlMediumSubjectSet.getSubjectId();
				if(subjectId.equals(subjectId2)){
					optLesson = tPlMediumSubjectSet.getOptLesson();
					proLesson = tPlMediumSubjectSet.getProLesson();
					break;
				}
			}
			switch(subjectId){
			case "4":
				subjectName ="政治";
				break;
			case "5":
				subjectName ="历史";
				break;
			case "6":
				subjectName ="地理";
				break;
			case "7":
				subjectName ="物理";
				break;
			case "8":
				subjectName ="化学";
				break;
			case "9":
				subjectName ="生物";
				break;
			case "19":
				subjectName ="技术";
				break;
			default:
				break;
			}
			JSONObject subjectSet = new JSONObject();
			subjectSet.put("subjectId", subjectId);
			subjectSet.put("subjectName", subjectName);
			subjectSet.put("optLesson", optLesson);
			subjectSet.put("proLesson", proLesson);
			subjectSetList.add(subjectSet);
		}
		data.put("subjectSetList", subjectSetList);
		return data;
	}
	
	@Override
	@Transactional
	public void SetMediumPreSetting(String schoolId,String termInfo,String placementId,String examTermInfo,String wfId,
			String examId,Integer ruleCode,Integer maxClassNum,Integer gradeSumLesson,Integer fixedSumLesson,
			List<JSONObject> zhSetList,List<JSONObject> classlevelList,List<JSONObject> subjectSetList){
		try{
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("placementId", placementId);
			map.put("termInfo", termInfo);
			map.put("schoolId",schoolId);
			PlacementTask pl = placementTaskDao.queryPlacementTaskById(map);
			if (pl == null) {
				throw new RuntimeException("查询不到对应的分班任务，请联系管理员！");
			}
			Integer placementType = pl.getPlacementType();
			if(placementType!=2){
				throw new CommonRunException(-1, "分班类型不是中走班");
			}
			
			if(StringUtils.isBlank(schoolId)||StringUtils.isBlank(termInfo)||StringUtils.isBlank(placementId)||
					zhSetList.size()<=0||classlevelList.size()<=0||subjectSetList.size()<=0){
				throw new RuntimeException("参数异常，请检查传入参数");
			}
			
			for(JSONObject zhSet:zhSetList){
				Integer compNum = Integer.parseInt(zhSet.get("compNum").toString());
				Integer numOfOpenClasses = Integer.parseInt(zhSet.get("numOfOpenClasses").toString());
				Integer numOfStuds = Integer.parseInt(zhSet.get("numOfStuds").toString());
				if(numOfOpenClasses<0||numOfStuds<0||numOfStuds<numOfOpenClasses||numOfStuds>compNum||
						(numOfOpenClasses>0&&numOfStuds<=0)||(numOfStuds>0&&numOfOpenClasses<=0)){
					throw new RuntimeException("开班数和固定班额设置有误");
				}
			}
			//TODO 验证组合分层信息准确性暂时未想好怎么处理，可以最后再验证
			
			for(JSONObject subjectSet:subjectSetList){
				Integer optLesson = Integer.parseInt(subjectSet.get("optLesson").toString());
				Integer proLesson = Integer.parseInt(subjectSet.get("proLesson").toString());
				if(optLesson<0||proLesson<0){
					throw new RuntimeException("走班科目学时设置有误");
				}
			}
			if(maxClassNum<0){
				throw new RuntimeException("最大班额设置有误");
			}
			if(gradeSumLesson<0){
				throw new RuntimeException("周总课时设置有误");
			}
			if(fixedSumLesson<0||fixedSumLesson>gradeSumLesson){
				throw new RuntimeException("非走班课时设置有误");
			}
			

			//开始保存数据
			pl.setWfId(wfId);
			
			WfInfoVo wfInfo = iWfInfoMapper.selectById(wfId);
			if(null == wfInfo ) throw new RuntimeException("找不到选科数据："+wfId);
			pl.setWfTermInfo(wfInfo.getSchoolyear()+wfInfo.getTerminfoid());
			placementTaskDao.updatePlacementTaskById(pl);
			List<TPlMediumSettings> TPlMediumSettingsList = placementTaskDao.getTPlMediumSettings(map);
			String ruleId = "";
			TPlMediumSettings tPlMediumSettings = new TPlMediumSettings();
			tPlMediumSettings.setPlacementId(placementId);
			tPlMediumSettings.setSchoolId(schoolId);
			tPlMediumSettings.setTermInfo(termInfo);
			tPlMediumSettings.setRuleCode(ruleCode);
			tPlMediumSettings.setExamId(examId);
			tPlMediumSettings.setExamTermInfo(examTermInfo);
			tPlMediumSettings.setMaxClassNum(maxClassNum);
			tPlMediumSettings.setGradeSumLesson(gradeSumLesson);
			tPlMediumSettings.setFixedSumLesson(fixedSumLesson);
			if(TPlMediumSettingsList.size()<=0||TPlMediumSettingsList ==null){
				ruleId = UUIDUtil.getUUID();
				tPlMediumSettings.setRuleId(ruleId);
				placementTaskDao.insertTPlMediumSettings(tPlMediumSettings);
			}else{
				ruleId = TPlMediumSettingsList.get(0).getRuleId();
				tPlMediumSettings.setRuleId(ruleId);
				placementTaskDao.updateTPlMediumSettings(tPlMediumSettings);
			}
			
			//placementId、schoolId、placementType
			map.put("placemrntType", placementType);
			placementTaskDao.deleteOpenClassInfo(map);
			placementTaskDao.deleteOpenClassTask(map);
			placementTaskDao.deleteTPlMediumZhSet(map);
			List<TPlMediumZhSet> TPlMediumZhSetList = new ArrayList<TPlMediumZhSet>();
			for(JSONObject zhSet:zhSetList){
				String compFrom = zhSet.get("compFrom").toString();
				String compName = zhSet.get("compName").toString();
				Integer compNum = Integer.parseInt(zhSet.get("compNum").toString()) ;
				Integer numOfOpenClasses = Integer.parseInt(zhSet.get("numOfOpenClasses").toString());
				Integer numOfStuds = Integer.parseInt(zhSet.get("numOfStuds").toString());
				String openClassInfoId = "";
				if(numOfOpenClasses>0){
					//更新t_pl_openclassinfo表
					OpenClassInfo openClassInfo = new OpenClassInfo();
					openClassInfoId = UUIDUtil.getUUID();
					openClassInfo.setPlacementId(placementId);
					openClassInfo.setSchoolId(Long.parseLong(schoolId));
					openClassInfo.setTermInfo(termInfo);
					openClassInfo.setOpenClassInfoId(openClassInfoId);
					openClassInfo.setPlacementType(placementType);
					Integer type = 1;//班级类型为组合班级:1
					openClassInfo.setType(type);
					openClassInfo.setZhName(compName);
					openClassInfo.setSubjectIdsStr(compFrom);
					openClassInfo.setRuleId(ruleId);
					openClassInfo.setClassIdsStr("");
					Float scoreLimit = 0F;
					openClassInfo.setScoreUpLimit(scoreLimit);
					openClassInfo.setScoreDownLimit(scoreLimit);
					placementTaskDao.insertOpenClassInfo(openClassInfo);
					
					
					OpenClassTask openClassTask = new OpenClassTask();
					String openClassTaskId = UUIDUtil.getUUID();
					openClassTask.setOpenClassTaskId(openClassTaskId);
					openClassTask.setSchoolId(Long.parseLong(schoolId));
					openClassTask.setPlacementId(placementId);
					openClassTask.setOpenClassInfoId(openClassInfoId);
					openClassTask.setStatus(1);
					openClassTask.setSubjectLevel(0);
					openClassTask.setLayName("");
					openClassTask.setLayValue(0);
					openClassTask.setNumOfStuds(numOfStuds);
					openClassTask.setNumOfOpenClasses(numOfOpenClasses);
					Integer classSize = numOfStuds/numOfOpenClasses;
					if(numOfStuds%numOfOpenClasses !=0){
						classSize++;
					}
					openClassTask.setClassSize(classSize);
					openClassTask.setTermInfo(termInfo);
					placementTaskDao.insertOpenClassTask(openClassTask);
				}
				TPlMediumZhSet tPlMediumZhSet = new TPlMediumZhSet();
				tPlMediumZhSet.setPlacementId(placementId);
				tPlMediumZhSet.setSchoolId(schoolId);
				tPlMediumZhSet.setTermInfo(termInfo);
				tPlMediumZhSet.setOpenClassInfoId(openClassInfoId);
				tPlMediumZhSet.setCompFrom(compFrom);
				tPlMediumZhSet.setCompName(compName);
				tPlMediumZhSet.setCompNum(compNum);
				tPlMediumZhSet.setNumOfOpenClasses(numOfOpenClasses);
				tPlMediumZhSet.setNumOfStuds(numOfStuds);
				TPlMediumZhSetList.add(tPlMediumZhSet);
			}
			placementTaskDao.insertTPlMediumZhSet(termInfo,TPlMediumZhSetList);
			
		
			placementTaskDao.deleteTPlMediumClasslevel(map);
			List<TPlMediumClasslevel> tPlMediumClasslevelList = new ArrayList<TPlMediumClasslevel>();
			for(JSONObject classlevel:classlevelList){
				String subLevelName = classlevel.get("subLevelName").toString();
				Integer subLevel = Integer.parseInt(classlevel.get("subLevel").toString());
				String compFrom = classlevel.get("compFrom").toString();
				TPlMediumClasslevel tPlMediumClasslevel = new TPlMediumClasslevel();
				tPlMediumClasslevel.setPlacementId(placementId);
				tPlMediumClasslevel.setSchoolId(schoolId);
				tPlMediumClasslevel.setTermInfo(termInfo);
				tPlMediumClasslevel.setCompFrom(compFrom);
				tPlMediumClasslevel.setSubLevel(subLevel);
				tPlMediumClasslevel.setSubLevelName(subLevelName);
				tPlMediumClasslevelList.add(tPlMediumClasslevel);
				
			}
			placementTaskDao.insertTPlMediumClasslevel(termInfo,tPlMediumClasslevelList);
			
			
			placementTaskDao.deleteTPlMediumSubjectSet(map);
			List<TPlMediumSubjectSet> tPlMediumSubjectSetList = new ArrayList<TPlMediumSubjectSet>();
			for(JSONObject subjectSet:subjectSetList){
				String subjectId = subjectSet.get("subjectId").toString();
				Integer optLesson = Integer.parseInt(subjectSet.get("optLesson").toString());
				Integer proLesson = Integer.parseInt(subjectSet.get("proLesson").toString());
				TPlMediumSubjectSet tPlMediumSubjectSet = new TPlMediumSubjectSet();
				tPlMediumSubjectSet.setPlacementId(placementId);
				tPlMediumSubjectSet.setSchoolId(schoolId);
				tPlMediumSubjectSet.setTermInfo(termInfo);
				tPlMediumSubjectSet.setSubjectId(subjectId);
				tPlMediumSubjectSet.setOptLesson(optLesson);
				tPlMediumSubjectSet.setProLesson(proLesson);
				tPlMediumSubjectSetList.add(tPlMediumSubjectSet);
			}
			placementTaskDao.insertTPlMediumSubjectSet(termInfo,tPlMediumSubjectSetList);
			
			//先获取学生的组合信息(subjectIds,accountId,classId,gradeId,zhName)
			List<JSONObject> wishStudList = wishFillingThirdService.getZhStudentListToThird(wfId,termInfo,Long.parseLong(schoolId));
			List<Account> accountList = commonDataService.getStudentList(Long.parseLong(schoolId), termInfo,"");
			Map<String,String> accountIdToName = new HashMap<String,String>();
			for(Account account:accountList){
				String accountId = account.getId()+"";
				String name = account.getName();
				accountIdToName.put(accountId, name);
			}
			for(JSONObject wishStud:wishStudList){
				String accountId = wishStud.get("accountId").toString();
				String name = accountIdToName.get(accountId);
				wishStud.put("name",name);
			}
				//开始调用分班接口开始分班
			List<MediumWishSubjectGroup> MediumWishSubjectGroupList = new ArrayList<MediumWishSubjectGroup>();
			for(TPlMediumClasslevel tPlMediumClasslevel:tPlMediumClasslevelList){
				String compFrom = tPlMediumClasslevel.getCompFrom();
				for(TPlMediumZhSet TPlMediumZhSet:TPlMediumZhSetList){
					String compFrom2 = TPlMediumZhSet.getCompFrom();
					String compName = TPlMediumZhSet.getCompName();
					if(compFrom.equals(compFrom2)){
						MediumWishSubjectGroup mediumWishSubjectGroup = new MediumWishSubjectGroup();
						List<String> subjectIds = Arrays.asList(compFrom.split(","));
						String zhid = "";
						for(String subjectId:subjectIds){
							zhid = zhid+subjectId;
						}
						mediumWishSubjectGroup.setId(zhid);
						mediumWishSubjectGroup.setIds(subjectIds);
						mediumWishSubjectGroup.setName(compName);
						mediumWishSubjectGroup.setStudentCount(TPlMediumZhSet.getCompNum());
						mediumWishSubjectGroup.setFixedClassCount(TPlMediumZhSet.getNumOfOpenClasses());
						mediumWishSubjectGroup.setFixedStudentCount(TPlMediumZhSet.getNumOfStuds());
						mediumWishSubjectGroup.setLayId(tPlMediumClasslevel.getSubLevel());
						mediumWishSubjectGroup.setLayName(tPlMediumClasslevel.getSubLevelName());
						List<Student> students = new ArrayList<Student>();
						for(JSONObject wishStud:wishStudList){
							String zhIds = wishStud.get("subjectIds").toString();
							if(zhIds.equals(compFrom)){
								String accountId = wishStud.get("accountId").toString();
								String name = wishStud.get("name").toString();
								Student student = new Student(accountId,name,compFrom,compName);
								students.add(student);
							}
						}
						mediumWishSubjectGroup.setStudents(students);
						MediumWishSubjectGroupList.add(mediumWishSubjectGroup);
					}
				}
			}
			
			Integer maxClassSize = tPlMediumSettings.getMaxClassNum();
			Long seqTimeoutSecond = 1L;
			MediumClassData mediumClassData = MediumDivOperation.excuteDiv(maxClassSize,MediumWishSubjectGroupList,seqTimeoutSecond);
			//开始保存数据
			List<MediumFixedClassInfo> mediumFixedClassInfoList = mediumClassData.getFixedClassList();
			List<ClassInfo> adClassList = mediumClassData.getAdClassList();
			List<ClassInfo> teachClassList = mediumClassData.getTeachClassList();
			List<ClassInfo> walkClassInfoList = new ArrayList<ClassInfo>();
			walkClassInfoList.addAll(adClassList);
			walkClassInfoList.addAll(teachClassList);
			UpdateMediumResult(schoolId,termInfo,placementId,mediumFixedClassInfoList,walkClassInfoList);
		}catch(Exception e){
		logger.error("数据转换失败！",e);
		throw new RuntimeException("数据转换失败！",e);
		}
	}
	
	@Override
	public JSONObject GetQuerySubList(String schoolId,String termInfo,String placementId,String usedGrade){
		JSONObject data = new JSONObject();

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("placementId", placementId);
		map.put("termInfo", termInfo);
		map.put("schoolId",schoolId);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(map);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		/*
		Integer placemrntType = pl.getPlacementType();
		if(placemrntType!=2){
			throw new CommonRunException(-1, "分班类型不是中走班");
		}*/
		List<TPlDezyClass> tPlDezyClassList = placementTaskDao.getDezyClassList(map);
		Map<String,String> subjectInfo = new HashMap<String,String>();
		for(TPlDezyClass tPlDezyClass:tPlDezyClassList){
			String subjectId = tPlDezyClass.getSubjectId();
			String subjectName="";
			if(!subjectInfo.containsKey(subjectId)){
				switch(subjectId){
				case "4":
					subjectName ="政治";
					break;
				case "5":
					subjectName ="历史";
					break;
				case "6":
					subjectName ="地理";
					break;
				case "7":
					subjectName ="物理";
					break;
				case "8":
					subjectName ="化学";
					break;
				case "9":
					subjectName ="生物";
					break;
				case "19":
					subjectName ="技术";
					break;
				case "-999":
					subjectName ="行政班";
					break;
				default:
					break;
				}
				subjectInfo.put(subjectId, subjectName);
			}
		}
		List<JSONObject> subject = new ArrayList<JSONObject>();
		for(Map.Entry<String,String> entry:subjectInfo.entrySet()){
			JSONObject json = new JSONObject();
			String subjectId = entry.getKey();
			String subjectName = entry.getValue();
			json.put("subjectId", subjectId);
			json.put("subjectName", subjectName);
			subject.add(json);
		}
		
		Collections.sort(subject, new Comparator<JSONObject>() {
		      @Override
		      public int compare(JSONObject o1, JSONObject o2) {
		       String str1=o1.get("subjectId").toString();
		       String str2=o2.get("subjectId").toString();
		             if (str1.compareToIgnoreCase(str2)<0){  
		                 return -1;  
		             }  
		             return 1;  
		      }
		       });
		
		data.put("classLevelId",1);
		data.put("subject",subject);
		return data;
	}
	
	/*
	@Override
	public List<JSONObject> GetWatchQuerySubList(String schoolId,String termInfo,String placementId){
		List<JSONObject> data = new ArrayList<JSONObject>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("placementId", placementId);
		map.put("termInfo", termInfo);
		map.put("schoolId",schoolId);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(map);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		Integer placemrntType = pl.getPlacementType();
		if(placemrntType!=2){
			throw new CommonRunException(-1, "分班类型不是中走班");
		}
		List<TPlDezyClass> tPlDezyClassList = placementTaskDao.getDezyClassList(map);
		Map<String,String> subjectInfo = new HashMap<String,String>();
		for(TPlDezyClass tPlDezyClass:tPlDezyClassList){
			String subjectId = tPlDezyClass.getSubjectId();
			if(subjectId.equals("-999")){
				continue;
			}
			String subjectName="";
			if(!subjectInfo.containsKey(subjectId)){
				switch(subjectId){
				case "4":
					subjectName ="政治";
					break;
				case "5":
					subjectName ="历史";
					break;
				case "6":
					subjectName ="地理";
					break;
				case "7":
					subjectName ="物理";
					break;
				case "8":
					subjectName ="化学";
					break;
				case "9":
					subjectName ="生物";
					break;
				case "19":
					subjectName ="技术";
					break;
				default:
					break;
				}
				subjectInfo.put(subjectId, subjectName);
			}
		}
		for(Map.Entry<String,String> entry:subjectInfo.entrySet()){
			String subjectId = entry.getKey();
			String subjectName = entry.getValue();
			JSONObject json = new JSONObject();
			json.put("id", subjectId);
			json.put("text", subjectName);
			json.put("type", "1");
			data.add(json);
		}
		return data;
	}
	*/
	
	@Override
	public void UpdateMediumResult(String schoolId,String termInfo,String placementId,
			List<MediumFixedClassInfo> mediumFixedClassInfoList,List<ClassInfo> walkClassInfoList){

		if(StringUtils.isBlank(schoolId)||StringUtils.isBlank(termInfo)||StringUtils.isBlank(placementId)){
			throw new RuntimeException("传入参数有误");
		}
		//打印看分班结果
		System.out.println("固定上课班级信息");
		for(MediumFixedClassInfo mediumFixedClassInfo:mediumFixedClassInfoList){
			String wishId = mediumFixedClassInfo.getWishId();
			String wishName = mediumFixedClassInfo.getWishName();
			String tClassId = mediumFixedClassInfo.getTclassId();
			List<StudentbaseInfo> studentLists = mediumFixedClassInfo.getStudentLists();
			String studentIds = "";
			for(StudentbaseInfo studentInfo:studentLists){
				String accountId = studentInfo.getId();
				String name = studentInfo.getName();
				studentIds = studentIds + accountId+","+ name+";";
			}
			System.out.println("wishId:"+wishId+"   ;wishName:"+wishName+"   ;tClassId:"+tClassId+"   ;studentIds:"+studentIds);
		}
		System.out.println("走班上课班级信息");
		for(ClassInfo walkClassInfo:walkClassInfoList){
			String tClassId = walkClassInfo.getTclassId();
			Integer classSeq = walkClassInfo.getClassSeq();
			Integer tclassLevel = walkClassInfo.getTclassLevel();
			Integer classInfo = walkClassInfo.getClassInfo();
			String subjectId = walkClassInfo.getSubjectId();
			List<StudentbaseInfo> studentLists = walkClassInfo.getStudentLists();
			String studentIds = "";
			for(StudentbaseInfo studentInfo:studentLists){
				String accountId = studentInfo.getId();
				String name = studentInfo.getName();
				studentIds = studentIds + accountId+","+ name+";";
			}
			System.out.println("tClassId:"+tClassId+"   ;classSeq:"+classSeq+"   ;tclassLevel:"+tclassLevel+
					"   ;classInfo:"+classInfo+"   ;subjectId:"+subjectId+"   ;studentIds:"+studentIds);
		}
		
		
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("termInfo", termInfo);
		map.put("placementId", placementId);
		PlacementTask placementTask = placementTaskDao.queryPlacementTaskById(map);
		if (placementTask == null) {
			throw new RuntimeException("查询不到对应的分班任务，请联系管理员！");
		}
		String wfId = placementTask.getWfId();
		String usedGrade = placementTask.getUsedGrade();
		Integer placementType = placementTask.getPlacementType();
		if(placementType!=2){
			throw new RuntimeException("分班类型不是中走班");
		}
		
		if(mediumFixedClassInfoList.size()>0){
			List<OpenClassTask> OpenClassTaskList = placementTaskDao.queryOpenClassTask(map);
			map.put("placementType", placementType);
			List<OpenClassInfo> OpenClassInfoList = placementTaskDao.queryOpenClassInfo(map);
			if(OpenClassTaskList==null||OpenClassInfoList==null){
				throw new RuntimeException("微走班分班info和分班task信息有误");
			}
			//生成一个Map，Key为志愿id，Value保存志愿id对应的分班info和task的id，方便后面直接使用而不用每次遍历
			Map<String,Map<String,String>> OpenClass = new HashMap<String,Map<String,String>>();
			for(OpenClassInfo OpenClassInfo:OpenClassInfoList){
				String subjectIdsStr = OpenClassInfo.getSubjectIdsStr();
				List<String> subjectIds = Arrays.asList(subjectIdsStr.split(","));
				String zhSubjectIds = "";
				for(String subjectId:subjectIds){
					zhSubjectIds = zhSubjectIds+subjectId;
				}
				
				String openClassInfoId = OpenClassInfo.getOpenClassInfoId();
				for(OpenClassTask OpenClassTask:OpenClassTaskList){
					String openClassInfoId2 = OpenClassTask.getOpenClassInfoId();
					if(openClassInfoId.equals(openClassInfoId2)){
						String openClassTaskId = OpenClassTask.getOpenClassTaskId();
						Map<String,String> TaskInfo = new HashMap<String,String>();
						TaskInfo.put("openClassInfoId", openClassInfoId);
						TaskInfo.put("openClassTaskId", openClassTaskId);
						OpenClass.put(zhSubjectIds, TaskInfo);
					}
				}
			}
			List<JSONObject> studentClassInfoList = placementTaskDao.getStudentClassInfo(map);
			Map<String,String> stuToClassId = new HashMap<String,String>();
			for(JSONObject studentClassInfo:studentClassInfoList){
				String accountId = studentClassInfo.get("accountId").toString();
				String classId = studentClassInfo.get("classId").toString();
				stuToClassId.put(accountId, classId);
			}
			//微走班已分班学生表t_pl_studentinfo实体类
			List<StudentInfo> StudentInfoList = new ArrayList<StudentInfo>();
			//微走班教学班级表t_pl_teachingclassinfo实体类，其中Key为t_pl_teachingclassinfo
			List<TeachingClassInfo> TeachingClassInfoList = new ArrayList<TeachingClassInfo>();
			for(MediumFixedClassInfo mediumFixedClassInfo:mediumFixedClassInfoList){
				String wishId = mediumFixedClassInfo.getWishId();
				String wishName = mediumFixedClassInfo.getWishName();
				String tclassId = mediumFixedClassInfo.getTclassId();
				List<StudentbaseInfo> studentLists = mediumFixedClassInfo.getStudentLists();
				String openClassInfoId = OpenClass.get(wishId).get("openClassInfoId");
				String openClassTaskId = OpenClass.get(wishId).get("openClassTaskId"); 
				Integer numOfStuds = studentLists.size();
				Integer numOfBoys = 0;
				Integer numOfGirls = 0;
				for(StudentbaseInfo studentbaseInfo:studentLists){
					String accountId = studentbaseInfo.getId();
					String classId = stuToClassId.get(accountId);
					Map<String,Object> stuMap = new HashMap<String,Object>();
					stuMap.put("termInfo", termInfo);
					stuMap.put("accountId",accountId);
					String gender = placementTaskDao.queryGender(stuMap);
					if("1".equals(gender)){
						numOfBoys++;
					}else if("2".equals(gender)){
						numOfGirls++;
					}
					StudentInfo StudentInfo = new StudentInfo();
					StudentInfo.setPlacementId(placementId);
					StudentInfo.setSchoolId(Long.parseLong(schoolId));
					StudentInfo.setTeachingClassId(tclassId);
					StudentInfo.setAccountId(Long.parseLong(accountId));
					StudentInfo.setClassId(Long.parseLong(classId));
					StudentInfo.setOpenClassInfoId(openClassInfoId);
					StudentInfo.setOpenClassTaskId(openClassTaskId);
					StudentInfo.setType(1);
					StudentInfo.setTermInfo(termInfo);
					StudentInfoList.add(StudentInfo);
				}

				TeachingClassInfo teachingClassInfo = new TeachingClassInfo();
				teachingClassInfo.setTeachingClassId(tclassId);
				teachingClassInfo.setSchoolId(Long.parseLong(schoolId));
				teachingClassInfo.setUsedGrade(usedGrade);
				String teachingClassName = "";
				//命名规则：政史地1班
				for(int i =1;i<=99;i++){
					String temporaryName = wishName+i+"班";
					Boolean ifExist = false;
					for(TeachingClassInfo TeachingClassInfo:TeachingClassInfoList){
						String temporaryName2 = TeachingClassInfo.getTeachingClassName();
						if(temporaryName.equals(temporaryName2)){
							ifExist = true;
							break;
						}
					}
					if(!ifExist){
						teachingClassName = temporaryName;
						break;
					}
				}
				teachingClassInfo.setTeachingClassName(teachingClassName);
				teachingClassInfo.setPlacementId(placementId);
				teachingClassInfo.setOpenClassInfoId(openClassInfoId);
				teachingClassInfo.setOpenClassTaskId(openClassTaskId);
				teachingClassInfo.setNumOfBoys(numOfBoys);
				teachingClassInfo.setNumOfGirls(numOfGirls);
				teachingClassInfo.setNumOfStuds(numOfStuds);
				teachingClassInfo.setTermInfo(termInfo);
				TeachingClassInfoList.add(teachingClassInfo);
			
			}
			//先将两张表的值删掉，再以新数据保存
			placementTaskDao.deleteTeachingClassInfo(map);
			placementTaskDao.deleteStudentInfo(map);
			placementTaskDao.insertTeachingClassInfoBatch(TeachingClassInfoList,termInfo);
			placementTaskDao.insertStudentInfoBatch(StudentInfoList,termInfo);
		}
		//t_pl_dezy_class、t_pl_dezy_subjectcomp、t_pl_dezy_tclass_subcomp、t_pl_dezy_subjectcomp_student
		if(walkClassInfoList.size()>0){
			//保存每个序列的班级信息（Key为序列，考虑到兼容大走班，用tclassLevel+classSeq的组合字符串作为标识)
			Map<String,List<ClassInfo>> sequenceClassInfos = new HashMap<String,List<ClassInfo>>();
			for(ClassInfo classInfo:walkClassInfoList){
				Integer tclassLevel = classInfo.getTclassLevel();
				Integer classSeq = classInfo.getClassSeq();
				String Key = tclassLevel.toString() + classSeq.toString();
				if(!sequenceClassInfos.containsKey(Key)){
					sequenceClassInfos.put(Key, new ArrayList<ClassInfo>());
				}
				List<ClassInfo> sequenceClassInfo = sequenceClassInfos.get(Key);
				sequenceClassInfo.add(classInfo);
				sequenceClassInfos.put(Key,sequenceClassInfo);
			}
			
			//计算出所有时间点的最大开班数，作为需要的场地数量
			Integer maxClassNum = 0;
			for(Map.Entry<String,List<ClassInfo>> entrys : sequenceClassInfos.entrySet()){
				Integer classNum = entrys.getValue().size();
				if(classNum > maxClassNum){
					maxClassNum = classNum;
				}
			}
			
			//根据数量生成指定个场地信息
			List<Map<String,String>> groundInfoList = new  ArrayList<Map<String,String>>();
			for(int i = 1;i<=maxClassNum;i++){
				Map<String,String> groundInfo = new HashMap<String,String>();
				groundInfo.put("groundId", UUIDUtil.getUUID());
				groundInfo.put("groundName", "教学"+i+"班教室");
				//使用这个场地的班级命名参考
				groundInfo.put("className", "教学"+i+"班");
				groundInfoList.add(groundInfo);
			}
			
			//行政班id对应行政班实体类
			Map<String,ClassInfo> classIdToAdminClassInfo = new HashMap<String,ClassInfo>();
			List<ClassInfo> AdminClassInfos = sequenceClassInfos.get("00");
			int i=0;
			for(ClassInfo AdminClassInfo:AdminClassInfos){
				String groundId = groundInfoList.get(i).get("groundId");
				String groundName = groundInfoList.get(i).get("groundName");
				String className = groundInfoList.get(i).get("className");
				String classId = AdminClassInfo.getTclassId();
				AdminClassInfo.setTclassName(className);
				AdminClassInfo.setGroundId(groundId);
				AdminClassInfo.setTclassNum(AdminClassInfo.getStudentLists().size());
				AdminClassInfo.setGroundName(groundName);
				classIdToAdminClassInfo.put(classId, AdminClassInfo);
				i++;
			}
			
			//学生accountId对应行政班id
			Map<String,String> studentAdiminClassInfos = new HashMap<String,String>();
			for(ClassInfo AdminClassInfo:AdminClassInfos){
				String AdiminClassId = AdminClassInfo.getTclassId();
				List<StudentbaseInfo> studentLists = AdminClassInfo.getStudentLists();
				for(StudentbaseInfo studentBaseInfo:studentLists){
					String accountId = studentBaseInfo.getId();
					studentAdiminClassInfos.put(accountId, AdiminClassId);
				}
			}
			
			//教学班名对应教学班信息，为了后面做查重方便
			Map<String,ClassInfo> tclassNameToClassInfo = new HashMap<String,ClassInfo>();
			for(Map.Entry<String,List<ClassInfo>> entrys:sequenceClassInfos.entrySet()){
				if("00".equals(entrys.getKey())){
					continue;
				}
				//Key为行政班+教学班的唯一主键，Value中分别为AdminClassId，teachClassId，AdminClassNum，teachClassNum，Percentage百分比可以最后算
				Map<String,Map<String,Object>> stuDistributes = new HashMap<String,Map<String,Object>>();
				List<ClassInfo> teachClassInfos = entrys.getValue();
				for(ClassInfo teachClassInfo:teachClassInfos){
					String teachClassId = teachClassInfo.getTclassId();
					List<StudentbaseInfo> studentLists = teachClassInfo.getStudentLists();
					Integer teachClassNum =  studentLists.size();
					for(Map.Entry<String,ClassInfo> entry:classIdToAdminClassInfo.entrySet()){
						String AdminClassId = entry.getKey();
						String Key = AdminClassId+","+teachClassId;
						Map<String,Object> stuDistribute = new HashMap<String,Object>();
						stuDistribute.put("AdminClassId",AdminClassId);
						stuDistribute.put("teachClassId", teachClassId);
						stuDistribute.put("AdminClassNum", 0);
						stuDistribute.put("teachClassNum", teachClassNum);
						stuDistribute.put("ClassInfo",teachClassInfo);
						stuDistributes.put(Key, stuDistribute);
					}					 
				}
				//添加所属行政班人数
				for(ClassInfo teachClassInfo:teachClassInfos){
					String teachClassId = teachClassInfo.getTclassId();
					List<StudentbaseInfo> studentLists = teachClassInfo.getStudentLists();
					for(StudentbaseInfo studentInfo:studentLists){
						String accountId = studentInfo.getId();
						String AdminClassId = studentAdiminClassInfos.get(accountId);
						String Key = AdminClassId+","+teachClassId;
						Map<String,Object> stuDistribute = stuDistributes.get(Key);
						Integer AdminClassNum = (Integer) stuDistribute.get("AdminClassNum");
						AdminClassNum++;
						stuDistribute.put(Key,stuDistribute);
					}
				}
				
				List<Map<String,Object>> adminTeachInfoList = new ArrayList<Map<String,Object>>();
				for(Map.Entry<String,Map<String,Object>> entry:stuDistributes.entrySet()){
					Map<String,Object> stuDistribute = entry.getValue();
					Integer AdminClassNum = (Integer) stuDistribute.get("AdminClassNum");
					Integer teachClassNum = (Integer) stuDistribute.get("teachClassNum");
					float proportion = 0.000f;
					if(AdminClassNum >0){
						proportion = AdminClassNum*10000/teachClassNum;
					}
					stuDistribute.put("proportion",proportion);
					adminTeachInfoList.add(stuDistribute);
				}
				
				//对adminTeachInfoList排降序，以Map中的proportion排序
				Collections.sort(adminTeachInfoList, new Comparator<Map<String, Object>>() {
		            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
		            	Float proportion1 = (float)o1.get("proportion");
		            	Float proportion2 = (float)o2.get("proportion");
		                return proportion2.compareTo(proportion1);
		            }
		        });
				
				//用来保存本序列已经使用了的行政班和教学班，Key为班级id
				Map<String,String> adminClass = new HashMap<String,String>();
				Map<String,String> teachClass = new HashMap<String,String>();
				//已安排教学班场地的数量，如果等于本序列的教学班数量，那么break
				int completeTeachClassNum = 0;
				for(Map<String,Object> adminTeachInfo :adminTeachInfoList){
					if(completeTeachClassNum == teachClassInfos.size()){
						break;
					}
					String teachClassId = adminTeachInfo.get("teachClassId").toString();
					String adminClassId = adminTeachInfo.get("AdminClassId").toString();
					//如果行政班或者教学班已经被使用的话，continue
					if(adminClass.containsKey(adminClassId)||teachClass.containsKey(teachClassId)){
						continue;
					}
					ClassInfo classInfo = (ClassInfo) adminTeachInfo.get("ClassInfo");
					String subjectId = classInfo.getSubjectId();
					String subjectName = "";
					switch (subjectId){
					case "4":
						subjectName="政";
						break;
					case "5":
						subjectName="史";
						break;
					case "6":
						subjectName="地";
						break;
					case "7":
						subjectName="物";
						break;
					case "8":
						subjectName="化";
						break;
					case "9":
						subjectName="生";
						break;
					case "19":
						subjectName="技";
						break;
					default:
						throw new RuntimeException("subjectName信息有误！");
					}
					String classType ="";
					switch (classInfo.getTclassLevel()){
					case 1:
						classType="选";
						break;
					case 2:
						classType="学";
						break;
					default :
						throw new RuntimeException("classType信息有误");
					}
					String className = subjectName + "(" + classType + ")" + classIdToAdminClassInfo.get(adminClassId).getTclassName();
					
					//tclassNameToClassInfo
					//判断班级名是否已经存在，如果已经存在的话，流水号+1(认为不会有10个同样类型的班用到同一个场地)
					String classNameNew ="";
					for(int order =0;order<=99;order++){
						Boolean enable = true;
						String suffix ="";
						if(order>0){
							suffix=order+"";
						}
						if(tclassNameToClassInfo.containsKey(className+suffix)){
							enable = false;
						}
						if(enable){
							classNameNew = className+suffix;
							break;
						}
					}
					
					classInfo.setTclassName(classNameNew);
					System.out.println(classInfo.getTclassId()+","+classNameNew);
					classInfo.setGroundId(classIdToAdminClassInfo.get(adminClassId).getGroundId());
					classInfo.setGroundName(classIdToAdminClassInfo.get(adminClassId).getGroundName());
					//已经使用的教学班或者行政班不能再使用，另外就是如果班级的场地都已经安排了就不需要再遍历
					adminClass.put(adminClassId, adminClassId);
					teachClass.put(teachClassId, teachClassId);
					completeTeachClassNum++;
					tclassNameToClassInfo.put(classNameNew, classInfo);
				}
			}
			//开始更新t_pl_dezy_class表
			List<TPlDezyClass> tPlDezyClassList = new ArrayList<TPlDezyClass>();
			for(Map.Entry<String,ClassInfo> entry:tclassNameToClassInfo.entrySet()){
				ClassInfo classInfo = entry.getValue();
				TPlDezyClass tPlDezyClass = new TPlDezyClass();
				tPlDezyClass.setPlacementId(placementId);
				tPlDezyClass.setSchoolId(schoolId);
				tPlDezyClass.setUsedGrade(usedGrade);
				tPlDezyClass.setSubjectGroupId("-999");
				tPlDezyClass.setSubjectId(classInfo.getSubjectId());
				tPlDezyClass.setClassGroupId("-999");
				tPlDezyClass.setTclassId(classInfo.getTclassId());
				tPlDezyClass.setTclassType(classInfo.getClassInfo());
				Integer tclassLevel = classInfo.getTclassLevel();
				tPlDezyClass.setTclassLevel(tclassLevel);
				tPlDezyClass.setTclassNum(classInfo.getStudentLists().size());
				tPlDezyClass.setGroundId(classInfo.getGroundId());
				tPlDezyClass.setGroundName(classInfo.getGroundName());
				Integer classSeq = classInfo.getClassSeq();
				if(tclassLevel==2){
					classSeq = classSeq-3;
				}
				tPlDezyClass.setClassSeq(classSeq);
				tPlDezyClass.setTclassName(classInfo.getTclassName());
				tPlDezyClass.setOriClassName(classInfo.getTclassName());
				tPlDezyClass.setSubLevel(0);
				tPlDezyClassList.add(tPlDezyClass);
			}
			
			for(Map.Entry<String,ClassInfo> entry:classIdToAdminClassInfo.entrySet()){
				ClassInfo classInfo = entry.getValue();	
				TPlDezyClass tPlDezyClass = new TPlDezyClass();
				tPlDezyClass.setPlacementId(placementId);
				tPlDezyClass.setSchoolId(schoolId);
				tPlDezyClass.setUsedGrade(usedGrade);
				tPlDezyClass.setSubjectGroupId("-999");
				tPlDezyClass.setSubjectId("-999");
				tPlDezyClass.setClassGroupId("-999");
				tPlDezyClass.setTclassId(classInfo.getTclassId());
				tPlDezyClass.setTclassType(classInfo.getClassInfo());
				Integer tclassLevel = classInfo.getTclassLevel();
				tPlDezyClass.setTclassLevel(tclassLevel);
				tPlDezyClass.setTclassNum(classInfo.getStudentLists().size());
				tPlDezyClass.setGroundId(classInfo.getGroundId());
				tPlDezyClass.setGroundName(classInfo.getGroundName());
				tPlDezyClass.setClassSeq(0);
				tPlDezyClass.setTclassName(classInfo.getTclassName());
				tPlDezyClass.setOriClassName(classInfo.getTclassName());
				tPlDezyClass.setSubLevel(0);
				tPlDezyClassList.add(tPlDezyClass);
			}
			if(tPlDezyClassList.size()>0){
				placementImportDao.deleteTPlDezyClass(termInfo,placementId,schoolId);
				placementImportDao.batchInsertTPlDezyClassList(termInfo, tPlDezyClassList);
			}

			//再把数据转换成classKey,得到每个classKey对应的行政班id、list<account>、志愿id和志愿名和教学班ids,
			Map<String,List<ClassInfo>> stuToClassInfos = new HashMap<String,List<ClassInfo>>();
			for(ClassInfo classInfo:walkClassInfoList){
				List<StudentbaseInfo> StudentbaseInfoList = classInfo.getStudentLists();
				for(StudentbaseInfo studentbaseInfo:StudentbaseInfoList){
					String accountId = studentbaseInfo.getId();
					if(stuToClassInfos.containsKey(accountId)){
						List<ClassInfo> classInfoList = stuToClassInfos.get(accountId);
						classInfoList.add(classInfo);
						stuToClassInfos.put(accountId, classInfoList);
					}else{
						List<ClassInfo> classInfoList = new ArrayList<ClassInfo>();
						classInfoList.add(classInfo);
						stuToClassInfos.put(accountId, classInfoList);
					}
				}
			}
			//Key为班级classKey,Value中分别用AdminClass、TeachClassList、studentList、compName、compFrom代表行政班、教学班、学生accountId
			Map<String,Map<String,Object>> classKeyToAllInfos = new HashMap<String,Map<String,Object>>();
			for(Map.Entry<String,List<ClassInfo>> entry:stuToClassInfos.entrySet()){
				String accountId = entry.getKey();
				List<ClassInfo> classInfoList = entry.getValue();
				Collections.sort(classInfoList, new Comparator<ClassInfo>() {
				      @Override
				      public int compare(ClassInfo o1, ClassInfo o2) {
				       String str1=o1.getSubjectId();
				       String str2=o2.getSubjectId();
				             if (str1.compareToIgnoreCase(str2)<0){
				                 return -1;  
				             }  
				             return 1;  
				      }
				});
				String classKey ="";
				ClassInfo AdminClass = new ClassInfo();
				List<ClassInfo> TeachClassList = new ArrayList<ClassInfo>();
				for(ClassInfo classInfo:classInfoList){
					String tclassId = classInfo.getTclassId();
					classKey = classKey +","+ tclassId;
					Integer classType = classInfo.getClassInfo();
					if(classType ==6){
						AdminClass = classInfo;
					}else if(classType ==7){
						TeachClassList.add(classInfo);
					}
					
				}
				if(!classKeyToAllInfos.containsKey(classKey)){
					Map<String,Object> classKeyToAllInfo = new HashMap<String,Object>();
					classKeyToAllInfo.put("AdminClass", AdminClass);
					classKeyToAllInfo.put("TeachClassList", TeachClassList);
					List<String> studentList = new ArrayList<String>();
					studentList.add(accountId);
					classKeyToAllInfo.put("studentList", studentList);
					JSONObject param = new JSONObject();
					param.put("schoolId", schoolId);
					param.put("wfId", wfId);
					param.put("termInfoId", termInfo);
					String schoolYear = termInfo.substring(0, termInfo.length()-1);
					param.put("schoolYear", schoolYear);
					param.put("termInfo", termInfo);
					JSONObject wfObj = wishFillingSetDao.getTWfWfinfo(param);
					String isByElection = wfObj.getString("isByElection");
					JSONObject param2 = new JSONObject();
					param2.put("termInfo", termInfo);
					param2.put("schoolId", schoolId);
					param2.put("wfId", wfId);
					param2.put("useGrade", usedGrade);
					param2.put("accountId", accountId);
					List<JSONObject> subjectList = new ArrayList<JSONObject>();
					if("1".equals(isByElection)){
						subjectList = wishFillingSetDao.getStudentTbBy(param2);
					}else{
						subjectList = wishFillingSetDao.getStudentTb(param2);
					}
					Collections.sort(subjectList, new Comparator<JSONObject>() {
					      @Override
					      public int compare(JSONObject o1, JSONObject o2) {
					       String str1=o1.get("subjectId").toString();
					       String str2=o2.get("subjectId").toString();
					             if (str1.compareToIgnoreCase(str2)<0){  
					                 return -1;  
					             }  
					             return 1;  
					      }
					 });
					String compName = "";
					String compFrom = "";
					for(JSONObject subjectInfo:subjectList){
						String subjectId = subjectInfo.get("subjectId").toString();
						String subjectName = "";
						switch(subjectId){
						case "4":
							subjectName="政";
							break;
						case "5":
							subjectName="史";
							break;
						case "6":
							subjectName="地";
							break;
						case "7":
							subjectName="物";
							break;
						case "8":
							subjectName="化";
							break;
						case "9":
							subjectName="生";
							break;
						case "19":
							subjectName="技";
							break;
						default:
							throw new RuntimeException("subjectName信息有误！");
						}
						compName = compName+subjectName;
						compFrom = compFrom+subjectId+",";
					}
					compFrom = compFrom.substring(0, compFrom.length()-1);
					classKeyToAllInfo.put("compName",compName);
					classKeyToAllInfo.put("compFrom",compFrom);
					classKeyToAllInfos.put(classKey, classKeyToAllInfo);
				}else{
					Map<String,Object> classKeyToAllInfo = classKeyToAllInfos.get(classKey);
					List<String> studentList = (List<String>) classKeyToAllInfo.get("studentList");
					studentList.add(accountId);
					classKeyToAllInfo.put("studentList",studentList);
					classKeyToAllInfos.put(classKey, classKeyToAllInfo);
				}
			}
			placementImportDao.deleteTPlDezySubjectcompStudent(termInfo,placementId,schoolId);
			placementImportDao.deleteTPlDezyTclassSubcomp(termInfo,placementId,schoolId);
			placementImportDao.deleteTPlDezySubjectcomp(termInfo,placementId,schoolId);
			List<TPlDezySubjectcomp> tPlDezySubjectcompList = new ArrayList<TPlDezySubjectcomp>();
			List<TPlDezyTclassSubcomp> tPlDezyTclassSubcompList = new ArrayList<TPlDezyTclassSubcomp>();
			List<TPlDezySubjectcompStudent> tPlDezySubjectcompStudentList = new ArrayList<TPlDezySubjectcompStudent>();
			for(Map.Entry<String,Map<String,Object>> entry:classKeyToAllInfos.entrySet()){
				String subjectCompId = UUIDUtil.getUUID();
				Map<String,Object> classKeyToAllInfo = entry.getValue();
				String compName = classKeyToAllInfo.get("compName").toString();
				String compFrom = classKeyToAllInfo.get("compFrom").toString();
				List<String> studentList = (List<String>) classKeyToAllInfo.get("studentList");
				ClassInfo AdminClass = (ClassInfo) classKeyToAllInfo.get("AdminClass");
				List<ClassInfo> TeachClassList = (List<ClassInfo>) classKeyToAllInfo.get("TeachClassList");
				//行政班实体表
				TPlDezySubjectcomp tPlDezySubjectcomp = new TPlDezySubjectcomp();
				tPlDezySubjectcomp.setPlacementId(placementId);
				tPlDezySubjectcomp.setSchoolId(schoolId);
				tPlDezySubjectcomp.setUsedGrade(usedGrade);
				tPlDezySubjectcomp.setClassId(AdminClass.getTclassId());
				tPlDezySubjectcomp.setSubjectCompId(subjectCompId);
				tPlDezySubjectcomp.setCompName(compName);
				tPlDezySubjectcomp.setCompNum(studentList.size());
				tPlDezySubjectcomp.setCompFrom(compFrom);
				tPlDezySubjectcompList.add(tPlDezySubjectcomp);
				
				for(ClassInfo TeachClass:TeachClassList){
					TPlDezyTclassSubcomp tPlDezyTclassSubcomp = new TPlDezyTclassSubcomp();
					tPlDezyTclassSubcomp.setPlacementId(placementId);
					tPlDezyTclassSubcomp.setSchoolId(schoolId);
					tPlDezyTclassSubcomp.setUsedGrade(usedGrade);
					tPlDezyTclassSubcomp.setSubjectCompId(subjectCompId);
					tPlDezyTclassSubcomp.setTclassId(TeachClass.getTclassId());
					tPlDezyTclassSubcompList.add(tPlDezyTclassSubcomp);
				}
				
				for(String studentInfo:studentList){
					TPlDezySubjectcompStudent tPlDezySubjectcompStudent = new TPlDezySubjectcompStudent();
					tPlDezySubjectcompStudent.setPlacementId(placementId);
					tPlDezySubjectcompStudent.setSchoolId(schoolId);
					tPlDezySubjectcompStudent.setUsedGrade(usedGrade);
					tPlDezySubjectcompStudent.setSubjectCompId(subjectCompId);
					tPlDezySubjectcompStudent.setStudentId(studentInfo);
					tPlDezySubjectcompStudentList.add(tPlDezySubjectcompStudent);
				}
			}
			placementImportDao.batchInsertTPlDezySubjectcompList(termInfo,tPlDezySubjectcompList);
			placementImportDao.batchInsertTPlDezyTclassSubcompList(termInfo,tPlDezyTclassSubcompList);
			placementImportDao.batchInsertTPlDezySubjectcompStudentList(termInfo,tPlDezySubjectcompStudentList);
		}
	
		//更新status的状态
		PlacementTask placementTask2 = new PlacementTask();
		placementTask2.setSchoolId(Long.parseLong(schoolId));
		placementTask2.setTermInfo(termInfo);
		placementTask2.setPlacementId(placementId);
		placementTask2.setStatus(100);
		placementTaskDao.updatePlacementTaskById(placementTask2);
		
	}
}
