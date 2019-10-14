package com.talkweb.wishFilling.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.service.PlacementTaskService;
import com.talkweb.utils.KafkaUtils;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.service.WishFillingService;
import com.talkweb.wishFilling.util.Combination;
import com.talkweb.wishFilling.util.Util;

/** 
 * 志愿填报-管理员设置serviceImpl
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Service
public class WishFillingServiceImpl implements WishFillingService {
	@Value("#{settings['clientId']}")
	private String clientId;

	@Value("#{settings['clientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
	@Value("#{settings['wishFilling.msgUrlApp']}")
	private String msgUrlApp;
	
	@Value("#{settings['wishFilling.msgUrlPc']}")
	private String msgUrlPc;
	@Autowired
	private WishFillingSetDao wishFillingSetDao;
	 
	@Autowired
	private PlacementTaskService placementTaskService;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(WishFillingServiceImpl.class);
	@Override
	public List<JSONObject> getTbList(JSONObject param) throws Exception {
		String termInfo = param.getString("termInfo");
		String schoolYear = termInfo.substring(0, 4);
		String schoolId = param.getString("schoolId");
		 //获取所有轮次的组合列表
		List<JSONObject> zhList = wishFillingSetDao.getZhListByTb(param);
		Map<String,Integer> wfZhFixedNumMap = new HashMap<String,Integer>(); // wfId,totalFixedHasNum  固定的组合id和totalHasnum
		List<String> zhIds = new ArrayList<String>();  //固定zhId
		for(JSONObject zh:zhList){
			String wfId = zh.getString("wfId");
			String zhId = zh.getString("zhId");
			String zhWay = zh.getString("zhWay");
			if("0".equals(zhWay)){continue;} //不固定
			int fixedHasNum = zh.getInteger("fixedHasNum");
			if(fixedHasNum>0){
				if(wfZhFixedNumMap.containsKey(wfId)){
					int num = wfZhFixedNumMap.get(wfId);
					num += fixedHasNum;
					wfZhFixedNumMap.put(wfId, num);
				}else{
					wfZhFixedNumMap.put(wfId,fixedHasNum);
				}
			}
			zhIds.add(zhId);
		}
		//获取所有轮次的固定的组合的学生补选情况
		param.put("zhIds", zhIds);
		param.put("isFixedZh", "2");
		List<JSONObject> list = wishFillingSetDao.getByZhStudentNum(param);
		Map<String,Integer> byNumMap = new HashMap<String,Integer>(); 
		for(JSONObject obj:list){
			String wfId = obj.getString("wfId");
			int hasSelectedNumBy = obj.getInteger("hasSelectedNumBy");
			byNumMap.put(wfId, hasSelectedNumBy);
		}
		//准备数据
		List<T_GradeLevel> gradeLevels = new ArrayList<T_GradeLevel> ();
		gradeLevels.add(T_GradeLevel.T_JuniorOne);
		gradeLevels.add(T_GradeLevel.T_JuniorTwo);
		gradeLevels.add(T_GradeLevel.T_JuniorThree);
		gradeLevels.add(T_GradeLevel.T_HighOne);
		gradeLevels.add(T_GradeLevel.T_HighTwo);
		gradeLevels.add(T_GradeLevel.T_HighThree);
		List<Grade> gList = allCommonDataService.getGradeByGradeLevelBatch(Long.parseLong(schoolId), gradeLevels, termInfo);
		 List<Long> classIdsAll = new ArrayList<Long>();
	     for(Grade g:gList){
	    	 Long gId = g.getId();
	    	 List<Long> classIds = g.getClassIds();
	    	 if(classIds!=null){
	    		 classIdsAll.addAll(classIds);
	    	 }
	     }
	     Set<Long> classSetAll = new HashSet<Long>(classIdsAll); //去重
	     List<Classroom> cList = allCommonDataService.getClassroomBatch(Long.parseLong(schoolId), new ArrayList<Long>(classSetAll), termInfo);
	     Map<Long,Classroom> cMap = new HashMap<Long,Classroom>();
	     for(Classroom c :cList){
	    	  cMap.put(c.getId(), c);
	     }
	     //年级班级对应关系 人数  
	     HashMap<T_GradeLevel,Integer> gcMap = new HashMap<T_GradeLevel,Integer>();
	     for(Grade g:gList){
	    	 List<Long> classIds = g.getClassIds();
	    	 int count=0;
	    	 for(Long cId :classIds){
	    		 Classroom c = cMap.get(cId);
	    		 if(c!=null){
		    		 if(null!=c.getStudentAccountIds()){
		    			 count+=c.getStudentAccountIdsSize();
		    		 }
	    		 }
	    	 }
	    	 gcMap.put(g.getCurrentLevel(), count);
	     }
	    //得到填报列表
		List<JSONObject> returnList=wishFillingSetDao.getTbList(param);
		//返回拼接
		for(JSONObject obj:returnList){
			String wfId = obj.getString("wfId");
			String wfStartTime = obj.getString("wfStartTime");
			String wfEndTime = obj.getString("wfEndTime");
			String byStartTime = obj.getString("byStartTime");
			String byEndTime = obj.getString("byEndTime");
			String isShowQuery = obj.getString("isShowQuery");
			String wfWay = obj.getString("wfWay");
			String isByElection = obj.getString("isByElection"); //是否补选开始
			Date d = new Date();
			Date wfStartDate = null;
			Date wfEndDate = null;
			Date byStartDate = null;
			Date byEndDate = null;
			if(StringUtils.isNotBlank(wfStartTime)){
				wfStartDate = DateUtil.parseDateSecondFormat(wfStartTime+":00");
			}
			if(StringUtils.isNotBlank(wfEndTime)){
			    wfEndDate = DateUtil.parseDateSecondFormat(wfEndTime+":59");
			}
			if(StringUtils.isNotBlank(byStartTime)){
			    byStartDate = DateUtil.parseDateSecondFormat(byStartTime+":00");
			}
			if(StringUtils.isNotBlank(byEndTime)){
			     byEndDate = DateUtil.parseDateSecondFormat(byEndTime+":59");
			}
		   //删除按钮是否变灰
			obj.put("isDelete", 1); //不变灰
			if(wfStartTime!=null && wfEndTime!=null && "1".equals(isShowQuery)&& "0".equals(isByElection) && byStartDate!=null && byEndDate!=null && d.after(byStartDate)&& d.before(byEndDate)){ //编辑了,补选未开始，并且正选进行中
				obj.put("isDelete", 0);
			}
			if(byStartDate!=null && byEndDate !=null && "1".equals(isByElection) && d.after(byStartDate)&& d.before(byEndDate)){ //补选开始并且补选进行中
				obj.put("isDelete", 0);
			}
			//是否显示补选按钮
			obj.put("isShowByElection", 0); //不显示补选
			if("0".equals(isByElection) && wfEndDate!=null && wfEndDate.before(d)){ //补选未开始并且正选时间已结束
				obj.put("isShowByElection",1);
			}
			//进度条
			obj.put("isShowProgress", 0); //新建选课，未编辑
			obj.put("isShowSendBtn", 0);
			if(wfStartDate!=null && "0".equals(isByElection)  &&d.before(wfStartDate) ){
				obj.put("isShowProgress", 1); //新建选课，已编辑，未到开放时间
			}else if(wfStartDate!=null && wfEndDate!=null && "0".equals(isByElection) && d.after(wfStartDate) && d.before(wfEndDate)){
				obj.put("isShowProgress", 2); //新建选课，已编辑，正选进行中
				obj.put("isShowSendBtn", 1);//发送模板消息
			}else if(wfEndDate!=null && "0".equals(wfWay) &&"0".equals(isByElection) && d.after(wfEndDate)){ //正选结束，补选未编辑
				obj.put("isShowProgress", 3); 
			}else if(byStartDate!=null &&"1".equals(isByElection) && d.before(byStartDate)){  //正选结束，补选已编辑，未到开放时间
				obj.put("isShowProgress", 4); 
			}else if(byStartDate!=null && byEndDate!=null && "1".equals(isByElection) && d.after(byStartDate) && d.before(byEndDate)){ //正选结束，补选已编辑，补选进行中
				obj.put("isShowProgress", 5); 
				obj.put("isShowSendBtn", 1);//发送模板消息
			}else if(byEndDate!=null && "1".equals(isByElection) && d.after(byEndDate) ){ //补选结束
				obj.put("isShowProgress", 6); 
			}else if (wfEndDate!=null && "1".equals(wfWay) && d.after(wfEndDate) ){ //组合选结束
				obj.put("isShowProgress", 7); 
			}
				 
			
			int hasStudentNum = obj.getIntValue("hasStudentNum");
			/*if(hasStudentNum>0){
				obj.put("hasStudentTb", 1);
			}else{
				obj.put("hasStudentTb", 0 );
			}*/
			 //某个轮次的得到已选人数（正选）,wfProcess进度计算
			int totalFixedNum = 0;
			if("1".equals(isByElection)){ 
				//如果是补选，则计算固定的人数
				if(wfZhFixedNumMap.get(wfId)!=null){
					totalFixedNum = wfZhFixedNumMap.get(wfId);
				}
				if(byNumMap.get(wfId)!=null){
					hasStudentNum = byNumMap.get(wfId);
				}else{
					hasStudentNum=0;
				}
			}
			String useGrade = obj.getString("gradeId"); //使用年级，通过年级获取该年级下所有总选人数
			if(StringUtils.isNotBlank(useGrade)){
				T_GradeLevel currentLevel = T_GradeLevel.findByValue(Integer.parseInt(allCommonDataService.ConvertSYNJ2NJDM(useGrade, schoolYear)));
				Integer totalNum = gcMap.get(currentLevel);//获取总选课人数
				if( totalNum==null || totalNum==0 ){
					obj.put("wfProcess", "0%");
				}else{
					totalNum -= totalFixedNum;  //如果是补选，减去固定组合那部分人数
					if((float)hasStudentNum/totalNum*100>100.0){
						obj.put("wfProcess", "100%");
					}else{
						obj.put("wfProcess", StringUtil.formatNumber((float)hasStudentNum/totalNum*100,2)+"%");
					}
				}
			}else{
				obj.put("wfProcess", "0%");
			}
			//如果是补选 则将 byStartTime和byEndTime 放入wfXXTime传给前端
			if("1".equals(isByElection)){
				obj.put("wfStartTime",byStartTime);
				obj.put("wfEndTime", byEndTime);
			}
		} //end of returnList
		return returnList;
	}
	@Override
	public void createTb(JSONObject param) throws Exception {
		//将字典表中的科目代码插入subjecttb表中
		String schoolId = param.getString("schoolId");
		String schoolYear = param.getString("xn");
		String termInfoId = param.getString("xqm");
		String wfId = param.getString("wfId");
		List<JSONObject> dicSubList = new ArrayList<JSONObject>();
		if("1".equals(param.getString("selectWayToJunior"))){
			dicSubList = wishFillingSetDao.getDicSubjectList(schoolId+"",param.getString("areaCode"),"3","0");
		}else{
			dicSubList = wishFillingSetDao.getDicSubjectList(schoolId+"",param.getString("areaCode"),"4","0");
		}
		if(dicSubList!=null){
			for(JSONObject obj:dicSubList){
				obj.put("schoolId", schoolId);
				obj.put("schoolYear", schoolYear);
				obj.put("termInfoId", termInfoId);
				obj.put("wfId", wfId);
			}
		}
		//删除所有填报科目 
		wishFillingSetDao.deleteSubjectTb(param);
		//创建填报科目
	    wishFillingSetDao.createSubjectTbBatch(dicSubList);
	    //创建填报轮次
		wishFillingSetDao.createTb(param);
	}
	@Override
	public void updateTbName(JSONObject param) throws Exception {
		wishFillingSetDao.updateTbName(param);
	}

	@Override
	public JSONObject getTb(JSONObject param) throws Exception {
		JSONObject wf = wishFillingSetDao.getTb(param);
		String wfWay = wf.getString("wfWay");
		String subjectIds=wf.getString("subjectIds");
		String subjectNames=wf.getString("subjectNames");
		logger.info("wishFilling :   param 4:" +param);
		List<JSONObject> subList= wishFillingSetDao.getDicSubjectList(wf.getString("schoolId"),param.getString("areaCode"),"4","0");// new ArrayList<JSONObject>();
		/*if(StringUtils.isNotBlank(subjectIds) && StringUtils.isNotBlank(subjectNames)){
			List<Long> ids=StringUtil.toListFromString(subjectIds);
			List<String> names = Arrays.asList(subjectNames.split(","));
			for(int i=0;i<ids.size();i++){
				JSONObject obj = new JSONObject();
				Long id = ids.get(i);
				String name = names.get(i);
				obj.put("subjectId", id);
				obj.put("subjectName", name);
				//第二版因为不能删除科目所以标志位去掉
				//param.put("subjectId", id);
				int hasSubjectStudentTb=wishFillingSetDao.getStudentSubjectCount(param);
				if(hasSubjectStudentTb>0){
					obj.put("hasSubjectStudentTb", 1 );
				}else{
					obj.put("hasSubjectStudentTb", 0 );
				}
				subList.add(obj);
			}
			
			subList = Util.lessonSort(subList, "subjectId"); //科目按subject Id排序
		}*/
		logger.info("wishFilling :   param 5:" +param);
		if(subList!=null){
			wf.put("subjects", subList);
		}else{
			wf.put("subjects", new ArrayList<JSONObject>());
		}
		List<JSONObject> subListToJunior= wishFillingSetDao.getDicSubjectList(wf.getString("schoolId"),param.getString("areaCode"),"3","0");
		logger.info("wishFilling :   param 6:" +param);
		if(subList!=null){
			wf.put("subjectsToJunior", subListToJunior);
		}else{
			wf.put("subjectsToJunior", new ArrayList<JSONObject>());
		}
		//获取zhs
		List<JSONObject> zhList =new ArrayList<JSONObject>();
		if("1".equals(wfWay)){
		    zhList = wishFillingSetDao.getZhListByTb(param);
			List<JSONObject> dicList = wishFillingSetDao.getDicSubjectList(wf.getString("schoolId"),param.getString("areaCode"),param.getString("pycc"),"0");
			JSONObject paramObj = (JSONObject) param.clone();
			List<JSONObject> zhNumList = getStaticListByZh(paramObj);
			logger.info("wishFilling :   param 7:" +param);
			Map<String,String> subIdNameMap= new HashMap<String,String>();
			if(dicList!=null){
				for(JSONObject dic:dicList){
					String subjectId = dic.getString("subjectId");
					String subjectName = dic.getString("subjectName");
					subIdNameMap.put(subjectId, subjectName);
				}
			}
			
			Map<String,Integer> zhIdNumMap = new HashMap<String,Integer>();
			for(JSONObject zh:zhNumList){
				String zhId = zh.getString("zhId");
				Integer studentNum = zh.getInteger("studentNum");
				zhIdNumMap.put(zhId, studentNum);
			}
			for(JSONObject zh:zhList){
				String zhId = zh.getString("zhId");
				List<JSONObject> subjects = new ArrayList<JSONObject>();
				String zhSubjectIds = zh.getString("subjectIds");
				if(StringUtils.isNotBlank(zhSubjectIds)){
					String [] zhSubjectIdsStr = zhSubjectIds.split(",");
					for(int i =0;i<zhSubjectIdsStr.length;i++){
						JSONObject obj = new JSONObject();
						String id = zhSubjectIdsStr[i];
						if(StringUtils.isNotBlank(id)){
							obj.put("subjectId",id);
							if(subIdNameMap.get(id)!=null){
								obj.put("subjectName",subIdNameMap.get(id));
							}else{
								obj.put("subjectName", "");
							}
							subjects.add(obj);
						}
					}
				}
				zh.put("subjects", subjects);
				Integer hasStudentTbZh = zhIdNumMap.get(zhId);
				if(hasStudentTbZh==null){
					zh.put("hasStudentTbZh", 0);
				}else{
					zh.put("hasStudentTbZh", hasStudentTbZh);
				}
			}
		}
		wf.put("zhs", zhList);
		
		//获取轮次下的已选人数
		/*Integer wfNum = wf.getInteger("wfNum");
		param.put("wfNum", wfNum);*/
		int hasStudentTb = wishFillingSetDao.getTotalStudentCount(param);
		logger.info("wishFilling :   param 8:" +param);
		wf.put("hasStudentTb", hasStudentTb);
		param.remove("subjectId"); //移除 ，以免影响其他方法的sql
		param.remove("wfNum");//移除 ，以免影响其他方法的sql
		return wf;
	}
	@Override
	public void deleteTb(JSONObject param) throws Exception {
		//删除填报主表
		wishFillingSetDao.deleteTb(param);
		//删除填报科目 
		wishFillingSetDao.deleteSubjectTb(param);
		//删除学生填报
		wishFillingSetDao.deleteStudentTb(param);
		//删除组合科目
		wishFillingSetDao.deleteZhSubject(param);
		//删除学生组合
		wishFillingSetDao.deleteZhStudent(param);
		//删除补选学生组合
		wishFillingSetDao.deleteByZhStudent(param);
		//删除补选学生填报
		wishFillingSetDao.deleteByStudentTb(param);
	}
	@Override
	public int updateTb(JSONObject param) throws Exception {
		String xn = param.getString("xn");
		String xqm = param.getString("xqm");
		String schoolId = param.getString("schoolId");
		String termInfo = param.getString("termInfo");
		String wfId = param.getString("wfId");
		List<Long> subList = (List<Long>) param.get("subList");
		List<JSONObject> zhs = (List<JSONObject>) param.get("zhs");
		String wfWay = param.getString("wfWay");
		Integer wfNum = param.getInteger("wfNum");
		String wfGradeId = param.getString("wfGradeId");
		List<JSONObject> zuheSubjectList =  new ArrayList<JSONObject>();
		
		JSONObject oldWf = wishFillingSetDao.getTb(param);
		String isByEletion = oldWf.getString("isByElection");
		if("0".equals(wfWay) && "1".equals(isByEletion)){ //补选已开始
			return -2;
		}
		String gLevel = allCommonDataService.ConvertSYNJ2NJDM(wfGradeId, xn);
		String newPycc = Util.getPycc(Integer.parseInt(gLevel));
		List<String> zhDelList= new ArrayList<String>(); //当wfWay为1按组合选择的时候，需要删除组合的id
		if(oldWf!=null){
			String oldUseGrade = oldWf.getString("wfGradeId");
			String oldWfWay = oldWf.getString("wfWay");
			
			if( !wfGradeId.equals(oldUseGrade)|| !oldWfWay.equals(wfWay)){ //修改填报年级
				//判断是否已有分班数据，若有则不允许修改年级或者选科方式
				logger.info("wishFillingPlacementtask:updateTb !wfGradeId.equals(oldUseGrade)|| !oldWfWay.equals(wfWay)" );
				List<String> usedIds = placementTaskService.getAllUsedWfId(termInfo);
				if(usedIds!=null && usedIds.contains(wfId)){
					return -3;
				}
			}
			//获取设置过的组合Ids
			if("1".equals(wfWay)){
				List<JSONObject> oldZhSubjects = wishFillingSetDao.getZhSubject(param);
				for(JSONObject oldZhSub:oldZhSubjects){
					String oldZhId = oldZhSub.getString("zhId");
					boolean isDelete = true;
					for(JSONObject newZh: zhs){
						String zhId = newZh.getString("zhId");
						if(oldZhId.equals(zhId)){
							isDelete = false;
						}
					}
					if(isDelete){
						zhDelList.add(oldZhId);
					}
				}//end of for oldZhSubjects
				if(zhDelList.size()>0){
					logger.info("wishFillingPlacementtask:updateTb zhDelList.size()>0" );
					//判断是否已有分班数据，若有则不允许修改组合
					List<String> usedIds = placementTaskService.getAllUsedWfId(termInfo);
					if(usedIds!=null && usedIds.contains(wfId)){
						return -3;
					}
				}
			}
			
			if( !wfGradeId.equals(oldUseGrade)|| !oldWfWay.equals(wfWay)){ //修改填报年级
				 
				//删除所有该old年级的选课记录 zhStudent和studenttb表
				JSONObject json = new JSONObject();
				json.put("schoolId", schoolId);
				json.put("wfId", wfId);
				json.put("termInfo", termInfo);
				if(!wfGradeId.equals(oldUseGrade)){
					json.put("wfGradeId", oldUseGrade);
				}
				wishFillingSetDao.deleteStudentTb(json);
				wishFillingSetDao.deleteZhStudent(json); //删除所有的编辑选课方式的情况下
				
			}
		
			//从news中找到old中没有的Id视为要删除的组合代码
			if("1".equals(wfWay)){ //选组合才能删除设置过的组合
				
				//删除选择该组合的人  studenttb和zhSubject表
				if(zhDelList.size()>0){
					JSONObject json = new JSONObject();
					json.put("wfId", wfId);
					json.put("schoolId", schoolId);
					json.put("zhIds", zhDelList);
					json.put("termInfo", termInfo);
					
					//zhDelList得到accountIds然后删除对应的studenttb表中的数据
					List<String> accountIdList = new ArrayList<String>();
					List<JSONObject> zhStudents = wishFillingSetDao.getAllStudentZh(param);
					for(JSONObject zhStu:zhStudents){
						String zhId = zhStu.getString("zhId");
						String accountIdStr = zhStu.getString("accountIds");
						List<String> accountIds = Arrays.asList(accountIdStr.split(","));
						if(zhDelList.contains(zhId)){
							accountIdList.addAll(accountIds);
						}
					}
					wishFillingSetDao.deleteZhStudent(json);
					json.remove("zhIds");
					if(accountIdList.size()>0){
						json.put("accountIds", accountIdList);
						wishFillingSetDao.deleteStudentTb(json);
					}
				}
			}
		}//end of oldWf!=null
		//更新填报详情
		param.put("isByElection", 0);//未补选（正选）
		param.put("byStartTime", "");
		param.put("byEndTime", "");
		param.put("pycc", newPycc);
		wishFillingSetDao.updateTb(param);
		if(oldWf!=null){
			String oldUseGrade = oldWf.getString("wfGradeId");
			String oldWfWay = oldWf.getString("wfWay");
			//根据培养层次更改基础科目
			//根据新的年级重新添加
			String oldPycc = "";
			if(StringUtils.isNotBlank(oldUseGrade)){
				String oldGLevel = allCommonDataService.ConvertSYNJ2NJDM(oldUseGrade, xn);
				oldPycc = Util.getPycc(Integer.parseInt(oldGLevel));
			}
			if(!newPycc.equals(oldPycc)){
				List<JSONObject> dicSubList = wishFillingSetDao.getDicSubjectList(schoolId+"",param.getString("areaCode"),newPycc,"0");
				if(dicSubList!=null){
					for(JSONObject obj:dicSubList){
						obj.put("schoolId", schoolId);
						obj.put("schoolYear", xn);
						obj.put("termInfoId", xqm);
						obj.put("wfId", wfId);
					}
				}
				//删除所有填报科目 
				wishFillingSetDao.deleteSubjectTb(param);
				//创建填报科目
			    wishFillingSetDao.createSubjectTbBatch(dicSubList);
			}
		}
		//填报科目
		List<JSONObject> subjects = new ArrayList<JSONObject>();
		List<JSONObject> lList = wishFillingSetDao.getDicSubjectList(schoolId,param.getString("areaCode"),newPycc,"0");
		Map<String,JSONObject> lessonMap = new HashMap<String,JSONObject>();
		if(lList!=null){
			for(JSONObject l:lList){
				lessonMap.put(l.getString("subjectId"), l);
			}
		}
		//不同的选课方式，得到subjects
		if("0".equals(wfWay)){
			for(long subjectId:subList){
				JSONObject subObj = new JSONObject();
				subObj.put("schoolId", schoolId);
				subObj.put("wfId", wfId);
				subObj.put("schoolYear", xn);
				subObj.put("termInfoId", xqm);
				subObj.put("subjectId", subjectId);
				subObj.put("subjectType", lessonMap.get(subjectId+"").getString("subjectType"));
				subObj.put("subjectName", lessonMap.get(subjectId+"").getString("subjectName"));
				subjects.add(subObj);
			}
		}else{ 
			//防止重名
			Map<String,String> subIdMap  = new HashMap<String,String>();
			for(JSONObject zh:zhs){
				String subjectIds = zh.getString("subjectIds");
				String zhName = zh.getString("zhName");
				String zhId = zh.getString("zhId");
				List<String> subjectIdList = Arrays.asList(subjectIds.split(","));
				for(String subjectId:subjectIdList){
					if(!subIdMap.containsKey(subjectId)){
						JSONObject subObj = new JSONObject();
						subObj.put("schoolId", schoolId);
						subObj.put("wfId", wfId);
						subObj.put("schoolYear", xn);
						subObj.put("termInfoId", xqm);
						subObj.put("subjectId", subjectId);
						subObj.put("subjectType", lessonMap.get(subjectId).getString("subjectType"));
						subObj.put("subjectName", lessonMap.get(subjectId).getString("subjectName"));
						subjects.add(subObj);
						subIdMap.put(subjectId, subjectId);
					}
				}
				
				JSONObject zuheObj = new JSONObject();
				  zuheObj.put("schoolId", schoolId);
		          zuheObj.put("schoolYear", xn);
		          zuheObj.put("termInfoId", xqm);
		          zuheObj.put("wfId", wfId);
		          if(StringUtils.isBlank(zhId)){
		        	  zuheObj.put("zhId", UUIDUtil.getUUID());
		          }else{
		        	  zuheObj.put("zhId",zhId);
		          }
		          zuheObj.put("subjectIds",subjectIds);
		          zuheObj.put("zhName",zhName);
		          zuheObj.put("zhWay", 0); //不固定
		          zuheObj.put("fixedHasNum", -1); //人数 （到补选设置后才有值）
		          zuheSubjectList.add(zuheObj);
			}//end of for zhs
		}

		//删除所有填报科目 
		wishFillingSetDao.deleteSubjectTb(param);
		//创建填报科目
		wishFillingSetDao.createSubjectTbBatch(subjects);
		//合成组合科目列表
		if("0".equals(wfWay)){ //单科则由后端生成组合，插入zhsubject中
			  //排序
			subjects=Util.lessonSort(subjects, "subjectId");
			int [] a=new int[subjects.size()];
			for(int i=0;i<subjects.size();i++){
			/*	Integer subjectId=subjects.get(i).getInteger("subjectId");
				a[i]=subjectId;*/
				a[i]=i;
			}
			
			//result为获取的组合下标列表
			List result =  Combination.combine(a , wfNum);
			 for(int i=0;i<result.size();i++){   
		          int[] zuhe = (int[])result.get(i);  
		          JSONObject zuheObj = new JSONObject(); 
		          zuheObj.put("schoolId", schoolId);
		          zuheObj.put("schoolYear", xn);
		          zuheObj.put("termInfoId", xqm);
		          zuheObj.put("wfId", wfId);
		          zuheObj.put("zhId", UUIDUtil.getUUID());
		          zuheObj.put("zhWay", 0); //不固定
		          zuheObj.put("fixedHasNum", -1); //人数 （到补选设置后才有值）
		          
		          String sIds = "";
		          String simpleNames = "";
		          //组合的下标
		          for(int j=0;j<zuhe.length;j++){  
		        	  JSONObject obj = subjects.get(zuhe[j]);
		        	  if(obj!=null){
		        		  sIds+=obj.getString("subjectId")+",";
		        		  JSONObject l = lessonMap.get(obj.getString("subjectId"));
		        		  simpleNames+=l.getString("subjectSimpleName");
		        	  }
		          }
		          if(StringUtils.isNotBlank(sIds)){
		        	  sIds = sIds.substring(0, sIds.length()-1);
		        	  
		          }
		          zuheObj.put("subjectIds",sIds);
		          zuheObj.put("zhName",simpleNames);
		          zuheSubjectList.add(zuheObj);
		          if(a.length==wfNum){
		            break;
		          }
			 }
			 //获取以前的所有的zhSubject
			   //判断以前是否有设置  一模一样的subjectIds则将zhId拿过来
			 List<JSONObject> list = wishFillingSetDao.getZhSubject(param);
			 for(JSONObject insertObj:zuheSubjectList){
				 insertObj.put("zhWay", 0); //默认为0 不固定
				 String InsertSubjectIds = insertObj.getString("subjectIds");
				 for(JSONObject obj:list){
					 String subjectIds=obj.getString("subjectIds");
					 if(InsertSubjectIds!=null && InsertSubjectIds.equals(subjectIds)){
						 insertObj.put("zhId", obj.getString("zhId"));
					 }
				 }
			 }
		}else{
		   //这里的逻辑放在上面了
		}
		
		 wishFillingSetDao.deleteZhSubject(param);
		 wishFillingSetDao.createZhSubjectBatch(zuheSubjectList);
		 return 1;
	}
	@Override
	public JSONObject getProgressTb(JSONObject param) throws Exception {
		JSONObject returnObj = new JSONObject();
		Long schoolId = param.getLong("schoolId");
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String termInfo =param.getString("termInfo");
		String schoolYear = termInfo.substring(0, 4);
		//使用年级
		String useGrade = wfObj.getString("wfGradeId");
		/*String schoolYear = wfObj.getString("schoolYear");
		param.put("schoolYear", schoolYear);
		String termInfoId = wfObj.getString("termInfoId");
		param.put("termInfoId", termInfoId);*/
		String subjectIds = wfObj.getString("subjectIds");
		String subjectNames = wfObj.getString("subjectNames");
		String wfNum = wfObj.getString("wfNum");
		
		List<Long> subList = StringUtil.toListFromString(subjectIds);
		List<String> subjectNameList =Arrays.asList(subjectNames.split(","));
		/*List<JSONObject> lessonList = wishFillingSetDao.getDicSubjectList();
		Map<Long,String> lessonNameMap = new HashMap<Long,String>();
		for(JSONObject l:lessonList){
			lessonNameMap.put(l.getLong("subjectId"),l.getString("subjectName"));
		}*/
		//获取年级名字
		String currentLevel = allCommonDataService.ConvertSYNJ2NJDM(useGrade, schoolYear);
		Grade g=allCommonDataService.getGradeByGradeLevel(schoolId, T_GradeLevel.findByValue(Integer.parseInt(currentLevel)), termInfo);
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		
		//获取总选课人数
		List<Long> ids= new ArrayList<Long>();
		if(!g.isGraduate){
			ids = g.getClassIds();
		}else{
			ids.add(-1L);
		}
		String allClassIds = "";
		for(long id :ids ){
			allClassIds += id+",";
		}
		if(StringUtils.isNotBlank(allClassIds)){
			allClassIds=allClassIds.substring(0,allClassIds.length()-1);
		}else{
			allClassIds="-1";
		}
		int totalSelectedNum=0;
		List<Classroom> cList = allCommonDataService.getClassroomBatch(schoolId, ids, termInfo);
		for(Classroom c:cList){
			if(c.getStudentAccountIds()!=null){
				totalSelectedNum+=c.getStudentAccountIdsSize();
			}
		}
		//获取已选课人数
		//param.put("wfNum", wfNum);
		int hasSelectedNum = wishFillingSetDao.getTotalStudentCount(param);
		//获取所有组合科目
		List<JSONObject> subjectHasSelected = new ArrayList<JSONObject>();
		for(int i=0;i< subList.size();i++){
			JSONObject subObj = new JSONObject();
			if(StringUtils.isBlank(subjectNameList.get(i))){
				subObj.put("subjectName","");
			}else{
				subObj.put("subjectName",subjectNameList.get(i));
			}
			param.put("subjectId", subList.get(i));
			int count=wishFillingSetDao.getStudentSubjectCount(param);
			subObj.put("hasSelectedNum", count);
			subObj.put("subjectId",subList.get(i));
			subjectHasSelected.add(subObj);
		}
		//科目Id排序
		subjectHasSelected = Util.lessonSort(subjectHasSelected, "subjectId");
		
		//获得	noSelectedNum
		int noSelectedNum = 0;
		JSONObject json = new JSONObject();
		json.put("wfId", param.getString("wfId"));
		json.put("schoolId", param.getString("schoolId"));
		json.put("termInfo", param.getString("termInfo"));
		json.put("termInfoId", param.getString("termInfoId"));
		json.put("schoolYear", param.getString("schoolYear"));
		json.put("classId", allClassIds);
		json.put("cList", cList);
		json.put("areaCode", param.getString("areaCode"));
		JSONObject noSelectObj = this.getNoselectedStudentList(json);
		if(noSelectObj!=null){
			noSelectedNum = noSelectObj.getInteger("noSelectedNum");
		}
				
		returnObj.put("gradeName", njName.get(T_GradeLevel.findByValue(Integer.parseInt(currentLevel))));//年级名称
		returnObj.put("totalSelectedNum",totalSelectedNum ); //总选课人数
		returnObj.put("hasSelectedNum",hasSelectedNum ); //已选课人数
		returnObj.put("noSelectedNum",noSelectedNum); //未选课人数
		returnObj.put("subjectHasSelected",subjectHasSelected); //科目已选人数
		return returnObj;
	}
	
	@Override
	public JSONObject getProgressTbByZh(JSONObject param) throws Exception {
		JSONObject returnObj = new JSONObject();
		Long schoolId = param.getLong("schoolId");
		String termInfo = param.getString("termInfo");
		String schoolYear = termInfo.substring(0, 4);
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String wfWay = wfObj.getString("wfWay");
		String isByElection = wfObj.getString("isByElection");
		String useGrade = wfObj.getString("wfGradeId");
		//获取年级名字
		String currentLevel = allCommonDataService.ConvertSYNJ2NJDM(useGrade, schoolYear);
		Grade g=allCommonDataService.getGradeByGradeLevel(schoolId, T_GradeLevel.findByValue(Integer.parseInt(currentLevel)), termInfo);
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		
		//获取总选课人数 
		List<Long> ids = new ArrayList<Long>();
		if(g.isGraduate){
			ids.add(-1L);
		}else{
			ids = g.getClassIds();
		}
		String allClassIds = "";
		for(long id :ids ){
			allClassIds += id+",";
		}
		if(StringUtils.isNotBlank(allClassIds)){
			allClassIds=allClassIds.substring(0,allClassIds.length()-1);
		}else{
			allClassIds="-1";
		}
		int totalSelectedNum=0;
		List<Classroom> cList = allCommonDataService.getClassroomBatch(schoolId, ids, termInfo);
		for(Classroom c:cList){
			if(c.getStudentAccountIds()!=null){
				totalSelectedNum+=c.getStudentAccountIdsSize();
			}
		}
		int totalFixedHasNum =0; //总的固定人数（固定的那些组合的 从正选中来的那些已选人）
		List<String> zhIds = new ArrayList<String>();  //固定zhId
		List<JSONObject> zhList = wishFillingSetDao.getZhListByTb(param);
		if("1".equals(isByElection)){ //如果是补选的话 ，总人数还要减去固定的总人数
			//Map<String,Integer> wfZhFixedNumMap = new HashMap<String,Integer>(); // wfId,totalFixedHasNum  固定的组合id和totalHasnum
			for(JSONObject zh:zhList){
				String zhId = zh.getString("zhId");
				String zhWay = zh.getString("zhWay");
				if("0".equals(zhWay)){continue;} //不固定
				int fixedHasNum = zh.getInteger("fixedHasNum");
				if(fixedHasNum>0){
					totalFixedHasNum+=fixedHasNum;
				}
				zhIds.add(zhId);
			}
			totalSelectedNum-=totalFixedHasNum;
		}
		
		//获取已选课人数
		//param.put("wfNum", wfNum);
		int hasSelectedNum = 0;
		if("0".equals(wfWay) && "1".equals(isByElection)){   //补选
			param.put("zhWay", "1"); //固定组合中的
			param.put("isFixedZh", "2");//补选人数
		    hasSelectedNum = wishFillingSetDao.getByTotalStudentCount(param);
		}else if("1".equals(wfWay)){
			//普通组合
			hasSelectedNum = wishFillingSetDao.getTotalStudentCount(param);
		}else{
			return null;
		}
		//获取各个组合的人员选择情况
		List<JSONObject> zhHasSelected = new ArrayList<JSONObject>();
		if("1".equals(isByElection) && "0".equals(wfWay)){ //补选的出固定的组合ids
			param.put("zhIdList", zhIds);
			param.put("isFixedZh", "2");//补选人数
			zhHasSelected = wishFillingSetDao.getByZhStudentCount(param);
		}else{
			zhHasSelected = wishFillingSetDao.getZhStudentCount(param);
		}
		
		Map<String,Integer> zhHasSelectedMap = new HashMap<String,Integer>();
		for(JSONObject zhHasObj:zhHasSelected){
			zhHasSelectedMap.put(zhHasObj.getString("zhId"), zhHasObj.getInteger("hasSelectedNum"));
		}
		//默认
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(JSONObject zh:zhList){
			String zhWay = zh.getString("zhWay");
			if("1".equals(isByElection)){//补选
				if("0".equals(zhWay)){continue;} //不固定跳过
			}
			String zhId = zh.getString("zhId");
			Integer num = zhHasSelectedMap.get(zhId);
			if(num!=null){
				zh.put("hasSelectedNum", num);
			}else{
				zh.put("hasSelectedNum", 0);
			}
			returnList.add(zh);
		}
		//获得	noSelectedNum
		int noSelectedNum = 0;
		JSONObject json = new JSONObject();
		json.put("wfId", param.getString("wfId"));
		json.put("schoolId", param.getString("schoolId"));
		json.put("termInfo", param.getString("termInfo"));
		json.put("termInfoId", param.getString("termInfoId"));
		json.put("schoolYear", param.getString("schoolYear"));
		json.put("classId", allClassIds);
		json.put("cList",cList);
		json.put("areaCode", param.getString("areaCode"));
		JSONObject noSelectObj = this.getNoselectedStudentList(json);
		if(noSelectObj!=null){
			noSelectedNum = noSelectObj.getInteger("noSelectedNum");
		}
		returnObj.put("isByElection", isByElection);
		returnObj.put("gradeName", njName.get(T_GradeLevel.findByValue(Integer.parseInt(currentLevel))));//年级名称
		returnObj.put("totalSelectedNum", totalSelectedNum);
		returnObj.put("hasSelectedNum", hasSelectedNum);
		returnObj.put("noSelectedNum",noSelectedNum);
		returnObj.put("zhHasSelected",returnList);
		return returnObj;
	}
	@Override
	public int updateByElection(JSONObject param) throws Exception {
		String termInfo = param.getString("termInfo");
		String wfId = param.getString("wfId");
		String schoolId = param.getString("schoolId");
		String isByElection = param.getString("isByElection");
		List<JSONObject> zhs = (List<JSONObject>) param.get("zhs");
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String useGrade = wfObj.getString("wfGradeId");
		String oldIsByElection = wfObj.getString("isByElection");
		String wfWay = wfObj.getString("wfWay");
		String wfEndTime = wfObj.getString("wfEndTime")+":59";
		Date wfEndDate = DateUtil.parseDateSecondFormat(wfEndTime);
		Date d = new Date();
		//组合的情况下，不能设置补选
		if("1".equals(wfWay)){
			return -3;
		}
		if(d.before(wfEndDate) && "0".equals(wfWay)){ //如果单科选且正选还未结束
			return -2;
		}
		//已有分班数据，不允许修改补选组合
		Boolean isUpdate = false; //是否修改组合信息（包括新增和删除）
		List<JSONObject> oldSetedZhList = wishFillingSetDao.getZhListByTb(param);
		List<String> toAddfixedZhIds = new ArrayList<String>(); //固定组合zhIds
		for(JSONObject zh:zhs){
			String zhId = zh.getString("zhId");
			toAddfixedZhIds.add(zhId);
		}
		 
		List<String> oldFixedZhIds = new ArrayList<String>();
		for(JSONObject zh:oldSetedZhList){
			String zhId = zh.getString("zhId");
			oldFixedZhIds.add(zhId);
			if(!toAddfixedZhIds.contains(zhId)){ //是否有需要删除的
				isUpdate = true;
				break;
			}
		}
		for(JSONObject zh:zhs){
			String zhId = zh.getString("zhId");
			if(!oldFixedZhIds.contains(zhId)){  // 是否有需要新增的
				isUpdate = true;
				break;
			}
		}
		if(isUpdate){
			logger.info("wishFillingPlacementtask:updateByElection  判断是否已有分班数据，若有则不允许修改补选" );
			//判断是否已有分班数据，若有则不允许修改补选
			List<String> usedIds = placementTaskService.getAllUsedWfId(termInfo);
			if(usedIds!=null && usedIds.contains(wfId)){
				return -4;
			}
			
		} 
		
		
		//更新填报主表的是否为补选等字段
		wishFillingSetDao.updateByTb(param);
		
		
		//获取所有组合及组合下的选择人数
		List<JSONObject> zhList = new ArrayList<JSONObject>();
		List<JSONObject> selectedStudentList = new ArrayList<JSONObject>();
		param.put("useGrade", useGrade);
		if("0".equals(oldIsByElection)){
			zhList = wishFillingSetDao.getStaticListByZh(param);
			selectedStudentList = wishFillingSetDao.getStudentZh(param);
		}else{
			zhList = wishFillingSetDao.getByStaticListByZh(param);
			List<String> zhIdList = new ArrayList<String>();
			for(JSONObject zh:zhs){
				String zhId = zh.getString("zhId");
				zhIdList.add(zhId);
			}
			param.put("zhIdList", zhIdList);
			selectedStudentList = wishFillingSetDao.getByStudentZh(param);
			param.remove("zhIdList");
		}
		 //某个zh下的对应的人员id
		List<Long> hasSelectedStudentIds = new ArrayList<Long>();
		Map<String,List<Long>> zhStudentMap = new HashMap<String,List<Long>>();
		for(JSONObject obj:selectedStudentList){
			Long accountId = obj.getLong("accountId");
			String zhId = obj.getString("zhId");
			hasSelectedStudentIds.add(accountId);
			if(zhStudentMap.containsKey(zhId)){
				List<Long> studentIds = zhStudentMap.get(zhId);
				studentIds.add(accountId);
				zhStudentMap.put(zhId, studentIds);
			}else{
				List<Long> studentIds = new ArrayList<Long>();
				studentIds.add(accountId);
				zhStudentMap.put(zhId, studentIds);
			}
		}
		List<Account> aList = allCommonDataService.getAccountBatch(Long.parseLong(schoolId), hasSelectedStudentIds, termInfo);
		Map<Long,Account> aMap = new HashMap<Long,Account>();
		for(Account a: aList){
			aMap.put(a.getId(), a);
		}
		
		
		Map<String,Integer> zhStudentNumMap= new HashMap<String,Integer>();
		for(JSONObject zh:zhList){
			String zhId = zh.getString("zhId");
			List<Long> accountList=zhStudentMap.get(zhId);
			int delStudentNum = 0;
			if(accountList!=null){
				for(Long id :accountList){
					if(aMap.get(id)==null){
						delStudentNum++;
					}
				}
			}
			Integer studentNum = zh.getInteger("studentNum");
			if(studentNum!=null){
				zhStudentNumMap.put(zhId, studentNum-delStudentNum); //要去除已删除的人
			}else{
				zhStudentNumMap.put(zhId, 0);
			}
			
		}
		
		//更新组合是否为固定组合等字段
		
		Set<Long> subjectIdList = new HashSet<Long>();
		for(JSONObject zh:zhs){
			String zhId = zh.getString("zhId");
			Integer studentNum = zhStudentNumMap.get(zhId);
			if(studentNum!=null){
				zh.put("fixedHasNum", studentNum); //这里不能由前端传入，因为可能页面没有刷新 而学生端又在提交数据
			}else{
				zh.put("fixedHasNum", 0);
			}
			zh.put("wfId", wfId);
			zh.put("schoolId", schoolId);
			zh.put("zhWay", "1");
			zh.put("termInfo", termInfo);
		}
		for(JSONObject zh:oldSetedZhList){
			String zhId = zh.getString("zhId");
			String subjectIds = zh.getString("subjectIds");
			if(!toAddfixedZhIds.contains(zhId)){ //不包含 则将zhWay等字段更新回 默认值
				JSONObject zhObj = new JSONObject();
				zhObj.put("zhId", zhId);
				zhObj.put("termInfo", termInfo);
				zhObj.put("wfId", wfId);
				zhObj.put("schoolId", schoolId);
				zhObj.put("zhWay", 0);
				zhObj.put("fixedHasNum", -1);
				zhs.add(zhObj);//zhs已改变，变成了待更新的全部zhList
			}else{ //包含则记录这些zhId的subjectId
				subjectIdList.addAll(StringUtil.toListFromString(subjectIds));
			}
		}
		for(JSONObject zh:zhs){
			wishFillingSetDao.updateZhSubject(zh);
		}
		
	    //更新补选科目 (选择的 subjectIsBy为1 没选择为0)
		JSONObject json0 = new JSONObject();
		json0.put("wfId", wfId);
		json0.put("schoolId", schoolId);
		json0.put("termInfo", termInfo);
		json0.put("subjectIdList", new ArrayList<Long>(subjectIdList));
	    wishFillingSetDao.updateSubjectTbByBatch(json0);
	    wishFillingSetDao.updateSubjectTbNoByBatch(json0);
		
		//如果取消补选
		if("1".equals(oldIsByElection) && "0".equals(isByElection)){
			//删除所有的补选数据 
			JSONObject json = new JSONObject();
			json.put("wfId", wfId);
			json.put("schoolId", schoolId);
			json.put("termInfo", termInfo);
 			wishFillingSetDao.deleteByStudentTb(param);
			wishFillingSetDao.deleteByZhStudent(param);
			return 1;
		}
		//如果第一次补选
		if("0".equals(oldIsByElection) && "1".equals(isByElection)){
			JSONObject json = new JSONObject();
			json.put("wfId", wfId);
			json.put("schoolId", schoolId);
			json.put("termInfo", termInfo);
			json.put("zhIdList", toAddfixedZhIds);
			wishFillingSetDao.insertByZhStudentBatch(json);	
			//获取选择该组合的所有人
			List<Long> addAccountIdList = wishFillingSetDao.getStudentIds(json);
			if(addAccountIdList.size()>0){
				json.put("accountIdList", addAccountIdList);
				wishFillingSetDao.insertByStudentTbBatchForSet(json);
			}else{
				wishFillingSetDao.insertByStudentTbBatchForSetNoPerson(json);
			}
			return 1;
		}
		
		//不是取消补选
		//先删除
		  //设置了的zh列表
		List<String> oldSetedFiexedZhId = new ArrayList<String>(); 
		Map<String,String> allOldZhSubMap = new HashMap<String,String>(); //subjectIds - zhId
		for(JSONObject zh:oldSetedZhList){
			String zhId = zh.getString("zhId");
			String zhWay = zh.getString("zhWay");
			String subjectIds = zh.getString("subjectIds");
			allOldZhSubMap.put(zhId, subjectIds);
			if("1".equals(zhWay)){ //固定的组合
				oldSetedFiexedZhId.add(zhId);
			}
		}
		//获取真正要新增的zhId，待删除的zhId，待删除的subjectIds
		List<String> trueAddFixedZhIdList = new ArrayList<String>();
		List<String> toBeDelFixedZhIdList = new ArrayList<String>();
		List<String> toBeDelSubjectIdsList = new ArrayList<String>();
		List<String> unChangeFixedSubjectIdsList = new ArrayList<String>();
		//查询新增的zh
		for(String toAddzhId:toAddfixedZhIds){
			if(!oldSetedFiexedZhId.contains(toAddzhId)){ //旧的里面没有新的  要新增的zh
				trueAddFixedZhIdList.add(toAddzhId);
			}
		}
		//查询要删的zh
		for(String oldZhId:oldSetedFiexedZhId){ //从旧的中寻找 新的  ，如果都找不到，则该旧zh要删除
			boolean isDel = true; 
			for(String zhId:toAddfixedZhIds){
				if(oldZhId.equals(zhId)){ //如果旧的在新的中，则不删除
					isDel=false;
				}
			}
			if(isDel){
				toBeDelFixedZhIdList.add(oldZhId);
				//获取要删除的 subjectIds
				String subjectIds = allOldZhSubMap.get(oldZhId);
				toBeDelSubjectIdsList.add(subjectIds);
			}
		}
		//查询不变的 zhId
		unChangeFixedSubjectIdsList = new ArrayList<String>(oldSetedFiexedZhId);
		unChangeFixedSubjectIdsList.removeAll(toBeDelFixedZhIdList);
		
		JSONObject json = new JSONObject();
		json.put("wfId", wfId);
		json.put("schoolId",schoolId);
		json.put("termInfo",termInfo);
		if(toBeDelFixedZhIdList.size()>0){
			json.put("zhIdList", toBeDelFixedZhIdList);
			json.put("isFixedZh", "0");
			wishFillingSetDao.updateByZhStudentBatch(json);//把以前设置过的，现在要删的组合下的学生的isFixedZh改为0
			//wishFillingSetDao.deleteByZhStudent(json);
		}
		
		JSONObject json2 = new JSONObject();
		json2.put("wfId", wfId);
		json2.put("schoolId",schoolId);
		json2.put("termInfo",termInfo);
		//json2.put("subjectIdsList", toBeDelSubjectIdsList);
		//获取以前设置过，且补选过，现在要删的某个科目组合下的学生id拿到
		 //获取所有studenttbby数据
		List<String> toBeDelAccountIdList = new ArrayList<String>();
		if(toBeDelSubjectIdsList.size()>0){
			List<JSONObject> accountObjList = wishFillingSetDao.getByStudentByFixedZhIds(json2); 
			for(JSONObject obj:accountObjList){
				String accountId=obj.getString("accountId");
				String subjectIds = obj.getString("subjectIds");
				if(toBeDelSubjectIdsList.contains(subjectIds)){
					toBeDelAccountIdList.add(accountId); //找到该要删组合科目的人
				}
			}
		}
		if(toBeDelAccountIdList.size()>0){
			json2.put("accountIdList", toBeDelAccountIdList);
			json2.put("isFixedZh", "0");
			//wishFillingSetDao.deleteByStudentTb(json2); 
			wishFillingSetDao.updateByStudentTbBatchForSet(json2);//找到某个学生 该学生选了要删的该组合下的subjectIds的isFixedZh改为0
		}
		
		//将固定组合下的所有人的填报信息插入至 studenttbby和 zhstudentby表
		if(trueAddFixedZhIdList.size()+unChangeFixedSubjectIdsList.size()>0){
			//插入zhstudentby
			List<String> addAndUnChangeFixedZhId= new ArrayList<String>();
			addAndUnChangeFixedZhId.addAll(unChangeFixedSubjectIdsList);
			addAndUnChangeFixedZhId.addAll(trueAddFixedZhIdList);
			json.put("zhIdList", addAndUnChangeFixedZhId);
			json.put("isFixedZh", "1");
			wishFillingSetDao.updateByZhStudentBatch(json); //把新增的组合和不变的组合的isFixedZh设置成 1
			
			//插入studenttbby
			//根据trueAddFixedZhIdList 获取要添加的accountId人员
			json.put("zhIdList", addAndUnChangeFixedZhId);
			List<Long> addAccountIdList = wishFillingSetDao.getStudentIds(json);
			json2.put("accountIdList", addAccountIdList);
			json2.put("isFixedZh", "1");
			wishFillingSetDao.updateByStudentTbBatchForSet(json2);
	    }
		return 1;
	}
/*	@Override
	public int updateByElection(JSONObject param) throws Exception {
		String termInfo = param.getString("termInfo");
		String wfId = param.getString("wfId");
		String schoolId = param.getString("schoolId");
		String isByElection = param.getString("isByElection");
		List<JSONObject> zhs = (List<JSONObject>) param.get("zhs");
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String wfWay = wfObj.getString("wfWay");
		String wfEndTime = wfObj.getString("wfEndTime")+":59";
		Date wfEndDate = DateUtil.parseDateSecondFormat(wfEndTime);
		Date d = new Date();
		//组合的情况下，不能设置补选
		if("1".equals(wfWay)){
			return -3;
		}
		if(d.before(wfEndDate) && "0".equals(wfWay)){ //如果单科选且正选还未结束
			return -2;
		}
		//更新填报主表的是否为补选等字段
		wishFillingSetDao.updateByTb(param);
		//更新组合是否为固定组合等字段
		List<String> toAddfixedZhIds = new ArrayList<String>(); //固定组合zhIds
		List<JSONObject> oldSetedZhList = wishFillingSetDao.getZhListByTb(param);
		for(JSONObject zh:zhs){
			String zhId = zh.getString("zhId");
			toAddfixedZhIds.add(zhId);
			zh.put("wfId", wfId);
			zh.put("schoolId", schoolId);
			zh.put("zhWay", 1);
			zh.put("termInfo", termInfo);
		}
		for(JSONObject zh:oldSetedZhList){
			String zhId = zh.getString("zhId");
			if(!toAddfixedZhIds.contains(zhId)){ //不包含 则将zhWay等字段更新回 默认值
				JSONObject zhObj = new JSONObject();
				zhObj.put("zhId", zhId);
				zhObj.put("termInfo", termInfo);
				zhObj.put("wfId", wfId);
				zhObj.put("schoolId", schoolId);
				zhObj.put("zhWay", 0);
				zhObj.put("fixedHasNum", -1);
				zhs.add(zhObj);//zhs已改变，变成了待更新的全部zhList
			}
		}
		for(JSONObject zh:zhs){
			wishFillingSetDao.updateZhSubject(zh);
		}
		//先删除
		  //设置了的zh列表
		List<String> oldSetedFiexedZhId = new ArrayList<String>(); 
		Map<String,String> allOldZhSubMap = new HashMap<String,String>(); //subjectIds - zhId
		for(JSONObject zh:oldSetedZhList){
			String zhId = zh.getString("zhId");
			String zhWay = zh.getString("zhWay");
			String subjectIds = zh.getString("subjectIds");
			allOldZhSubMap.put(zhId, subjectIds);
			if("1".equals(zhWay)){ //固定的组合
				oldSetedFiexedZhId.add(zhId);
			}
		}
		//fixedZhIds 剔除上一次有的，这次又传进来的（即没改变的组合id），不再重复删除（有学生填报数据了）和插入
		List<String> trueAddFixedZhIdList = new ArrayList<String>();
		List<String> toBeDelFixedZhIdList = new ArrayList<String>();
		List<String> unChangFixedZhIdList = new ArrayList<String>();
		List<String> toBeDelSubjectIdsList = new ArrayList<String>();
		//查询新增的zh
		for(String toAddzhId:toAddfixedZhIds){
			if(!oldSetedFiexedZhId.contains(toAddzhId)){ //旧的里面没有新的  要新增的zh
				trueAddFixedZhIdList.add(toAddzhId);
			}
		}
		//查询要删的zh
		for(String oldZhId:oldSetedFiexedZhId){ //从旧的中寻找 新的  ，如果都找不到，则该旧zh要删除
			boolean isDel = true; 
			for(String zhId:toAddfixedZhIds){
				if(oldZhId.equals(zhId)){ //如果旧的在新的中，则不删除
					isDel=false;
				}
			}
			if(isDel){
				toBeDelFixedZhIdList.add(oldZhId);
				//获取要删除的 subjectIds
				String subjectIds = allOldZhSubMap.get(oldZhId);
				toBeDelSubjectIdsList.add(subjectIds);
			}
		}
	    //获取不变的 zhIdList
		unChangFixedZhIdList.addAll(oldSetedFiexedZhId);
		unChangFixedZhIdList.removeAll(toBeDelFixedZhIdList);
		
		JSONObject json = new JSONObject();
		json.put("wfId", wfId);
		json.put("schoolId",schoolId);
		json.put("termInfo",termInfo);
		if(toBeDelFixedZhIdList.size()>0){
			json.put("zhIdList", toBeDelFixedZhIdList);
			wishFillingSetDao.deleteByZhStudent(json); //把以前设置过的，现在要删的组合删掉
		}
		
		JSONObject json2 = new JSONObject();
		json2.put("wfId", wfId);
		json2.put("schoolId",schoolId);
		json2.put("termInfo",termInfo);
		//json2.put("subjectIdsList", toBeDelSubjectIdsList);
		//获取以前设置过，且补选过，现在要删的某个科目组合下的学生id拿到
		 //获取所有studenttbby数据
		List<String> toBeDelAccountIdList = new ArrayList<String>();
		if(toBeDelSubjectIdsList.size()>0){
			List<JSONObject> accountObjList = wishFillingSetDao.getByStudentByFixedZhIds(json2); 
			for(JSONObject obj:accountObjList){
				String accountId=obj.getString("accountId");
				String subjectIds = obj.getString("subjectIds");
				if(toBeDelSubjectIdsList.contains(subjectIds)){
					toBeDelAccountIdList.add(accountId); //找到该要删组合科目的人
				}
			}
		}
		if(toBeDelAccountIdList.size()>0){
			json2.put("accountIdList", toBeDelAccountIdList);
			wishFillingSetDao.deleteByStudentTb(json2); //找到某个学生 该学生选了要删的该组合下的subjectIds
		}
		
		//在不变的zhIdList中判断被删除的那些学生填报数据并插入
		if(unChangFixedZhIdList.size()>0){
			List<Long> toAddAccountIds = new ArrayList<Long>();
			json.put("zhIdList",unChangFixedZhIdList);
			List<JSONObject> zxStudentZhList = wishFillingSetDao.getAllStudentZh(json);
			for(JSONObject zxStudentZh:zxStudentZhList){
				String accountIds = zxStudentZh.getString("accountIds");
				if(StringUtils.isNotBlank(accountIds)){
					toAddAccountIds.addAll(StringUtil.toListFromString((accountIds)));
				}
			}
				List<Long> hasAccountIdList = wishFillingSetDao.getByStudentIds(json);
				toAddAccountIds.removeAll(hasAccountIdList);
				if(toAddAccountIds.size()>0){
					json2.put("accountIdList", toAddAccountIds);
					wishFillingSetDao.insertByZhStudentBatch(json2); //插入studenttbby表在下面
					json.put("accountIdList", toAddAccountIds);
					wishFillingSetDao.insertByStudentTbBatchForSet(json2);
				}
			
		}
		
		
		//将固定组合下的所有人的填报信息插入至 studenttbby和 zhstudentby表
		if(trueAddFixedZhIdList.size()>0){
			//查询之前判断这个 新增的组合下的人，在正选的时候，选择了这个组合  且 这个人在补选又选了数据，则将该人的补选数据删除
			//如果不清除 则这个人的补选数据将有2条 或报错
			json.put("zhIdList", trueAddFixedZhIdList);
			List<String> toDelAccountIds = new ArrayList<String>();
			List<JSONObject>  zxStudentZhList = wishFillingSetDao.getAllStudentZh(json);
			for(JSONObject zxStudentZh:zxStudentZhList){
				String accountIds = zxStudentZh.getString("accountIds");
				if(StringUtils.isNotBlank(accountIds)){
					toDelAccountIds.addAll(Arrays.asList(accountIds.split(",")));
				}
			}
			json2.put("accountIdList", toDelAccountIds);
			wishFillingSetDao.deleteByStudentTb(json2);
			wishFillingSetDao.deleteByZhStudent(json2); 
			
			
			//插入zhstudentby
			json.put("zhIdList", trueAddFixedZhIdList);
			wishFillingSetDao.insertByZhStudentBatch(json);
			
			//插入studenttbby
			//根据trueAddFixedZhIdList 获取要添加的accountId人员
			json.put("zhIdList", trueAddFixedZhIdList);
			List<Long> addAccountIdList = wishFillingSetDao.getStudentIds(json);
			json2.put("accountIdList", addAccountIdList);
			wishFillingSetDao.insertByStudentTbBatchForSet(json2);
	    }
		return 1;
	}*/
	@Override
	public JSONObject getByElection(JSONObject param) throws Exception {
		JSONObject returnObj = new JSONObject();
		String termInfo = param.getString("termInfo");
		Long schoolId = param.getLong("schoolId");
		String schoolYear = termInfo.substring(0, 4);
		JSONObject wf = wishFillingSetDao.getTb(param);
		String isByElection = wf.getString("isByElection");
		String wfName = wf.getString("wfName");
		String byStartTime = wf.getString("byStartTime");
		String byEndTime = wf.getString("byEndTime");
		String useGrade = wf.getString("wfGradeId");
		
		//获取所有组合及组合下的选择人数
		List<JSONObject> zhList = new ArrayList<JSONObject>();
		List<JSONObject> selectedStudentList = new ArrayList<JSONObject>(); 
		param.put("useGrade", useGrade);
		if("0".equals(isByElection)){
			selectedStudentList = wishFillingSetDao.getStudentZh(param);
			zhList = wishFillingSetDao.getStaticListByZh(param);
		}else{
			selectedStudentList = wishFillingSetDao.getByStudentZh(param);
			zhList = wishFillingSetDao.getByStaticListByZh(param);
		}
		
		if(useGrade!=null){
			//获取总人数
			  //获取年级名字
			String currentLevel = allCommonDataService.ConvertSYNJ2NJDM(useGrade, schoolYear);
			Grade g=allCommonDataService.getGradeByGradeLevel(schoolId, T_GradeLevel.findByValue(Integer.parseInt(currentLevel)), termInfo);
			Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
					
			//获取总选课人数 
			if(g!=null){
				//获取总人数
				List<Long> ids = new ArrayList<Long>();
				if(g.isGraduate){
					ids.add(-1L);
				}else{
					ids = g.getClassIds();
				}
				int totalSelectedNum=0;
				List<Classroom> cList = allCommonDataService.getClassroomBatch(schoolId, ids, termInfo);
				Set<Long> allAccountIds = new HashSet<Long>();
				for(Classroom c:cList){
					if(c.getStudentAccountIds()!=null){
						allAccountIds.addAll(c.getStudentAccountIds());
						totalSelectedNum+=c.getStudentAccountIdsSize();
					}
				}
				
				//获取已删除学生人数
				  //所有组合下的已填报人员
				Set<Long> hasSelectedStudentIds = new HashSet<Long>(); 
				  //某个zh下的对应的人员id
				Map<String,List<Long>> zhStudentMap = new HashMap<String,List<Long>>();
				for(JSONObject obj:selectedStudentList){
					Long accountId = obj.getLong("accountId");
					String zhId = obj.getString("zhId");
					String zhWay = obj.getString("zhWay");
					if("1".equals(zhWay)){
						hasSelectedStudentIds.add(accountId);
					}
					if(zhStudentMap.containsKey(zhId)){
						List<Long> studentIds = zhStudentMap.get(zhId);
						studentIds.add(accountId);
						zhStudentMap.put(zhId, studentIds);
					}else{
						List<Long> studentIds = new ArrayList<Long>();
						studentIds.add(accountId);
						zhStudentMap.put(zhId, studentIds);
					}
				}
				
				List<Account> aList = allCommonDataService.getAccountBatch(schoolId, new ArrayList<Long>(allAccountIds), termInfo);
				Map<Long,Account> aMap = new HashMap<Long,Account>();
				for(Account a: aList){
					aMap.put(a.getId(), a);
				}
				int delNum = 0;
				for(Long id:hasSelectedStudentIds){
					if(aMap.get(id)==null){
						delNum++;
					}
				}
				
				int totalNum = 0;
				List<String> hasNumZhList = new ArrayList<String>(); 
				for(JSONObject zh:zhList)
				{
					String zhId = zh.getString("zhId");
					String zhWay = zh.getString("zhWay");
					Integer studentNum = zh.getInteger("studentNum");
					zh.put("hasByStudentTb", studentNum);
					if("1".equals(zhWay)){
						totalNum+= studentNum;
					}
					hasNumZhList.add(zhId);
					List<Long> accountList=zhStudentMap.get(zhId);
					int delStudentNum = 0;
					if(accountList!=null){
						for(Long id :accountList){
							if(aMap.get(id)==null){
								delStudentNum++;
							}
						}
					}
					zh.put("delStudentNum", delStudentNum);
				}
				//为0的也显示出来
				List<JSONObject> allZhList = wishFillingSetDao.getZhListByTb(param);
				for(JSONObject zh:allZhList){
					String zhId = zh.getString("zhId");
					if(!hasNumZhList.contains(zhId)){
						zh.put("hasByStudentTb", 0);
						zh.put("studentNum", 0);
						zh.put("delStudentNum", 0);
						zhList.add(zh);
					}
				}
				//添加返回值
				if(null==byStartTime){
					returnObj.put("byStartTime", "");
				}else{
					returnObj.put("byStartTime", byStartTime);
				}
				if(null==byEndTime){
					returnObj.put("byEndTime", "");
				}else{
					returnObj.put("byEndTime", byEndTime);
				}
				returnObj.put("zhs", zhList);
				returnObj.put("totalSelectedNum", totalSelectedNum);
				returnObj.put("hasNoStudentTbZh", totalSelectedNum+delNum-totalNum);
				returnObj.put("wfName", wfName);
			}//end of g!=null
		}//end of useGrade！=null
		return returnObj;
	}
	@Override
	public List<JSONObject> getZhSubject(JSONObject param) throws Exception {
		return wishFillingSetDao.getZhSubject(param);
	}
	@Override
	public JSONObject getStaticTotalByStudent(JSONObject param) throws Exception {
	    Long schoolId = param.getLong("schoolId");
		JSONObject returnObj = new JSONObject();
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String subjectIds = wfObj.getString("subjectIds");
		List<Long> subList = StringUtil.toListFromString(subjectIds);
		String isByElection = wfObj.getString("isByElection");
		/*if("1".equals(isByElection)){ //如果是补选则只取补选的数据
			param.put("isGetBySubject", 1);
			JSONObject wfObj1 = wishFillingSetDao.getTb(param);
			subjectIds = wfObj1.getString("subjectIds");
			subList = StringUtil.toListFromString(subjectIds);
			param.remove("isGetBySubject");
		}*/
		String termInfo =param.getString("termInfo");
		String schoolYear = termInfo.substring(0, 4);
		//使用年级
		String useGrade = wfObj.getString("wfGradeId");
		//获取年级名字
		String currentLevel = allCommonDataService.ConvertSYNJ2NJDM(useGrade, schoolYear);
		Grade g=allCommonDataService.getGradeByGradeLevel(schoolId, T_GradeLevel.findByValue(Integer.parseInt(currentLevel)), termInfo);
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		//获取总人数
		List<Long> ids = new ArrayList<Long>();
		if(!g.isGraduate){
			ids = g.getClassIds();
		}else{
			ids.add(-1L);
		}
		int totalSelectedNum=0;
		List<Classroom> cList = allCommonDataService.getClassroomBatch(schoolId, ids, termInfo);
		for(Classroom c:cList){
			if(c.getStudentAccountIds()!=null){
				totalSelectedNum+=c.getStudentAccountIdsSize();
			}
		}
	/*	//获取所有轮次的组合列表
		int fixedTotalSelectedNum =0 ; //固定组合的总人数
		if("1".equals(isByElection)){
			List<JSONObject> zhList = wishFillingSetDao.getZhListByTb(param);
			List<String> zhIds = new ArrayList<String>();  //固定zhId
			for(JSONObject zh:zhList){
				String zhId = zh.getString("zhId");
				String zhWay = zh.getString("zhWay");
				if("0".equals(zhWay)){continue;} //不固定
				int fixedHasNum = zh.getInteger("fixedHasNum");
				if(fixedHasNum>0){
						fixedTotalSelectedNum += fixedHasNum;
				}
			}
		}
		totalSelectedNum-=fixedTotalSelectedNum;*/
		
		//获取已选课人数
		int hasSelectedNum = 0;
		if("0".equals(isByElection)){ //未补选
		     hasSelectedNum = wishFillingSetDao.getTotalStudentCount(param);
		}else{
			//从zhstudent表中查已选人（正选固定+补选）
			param.put("zhWay", 1);
			 hasSelectedNum  = wishFillingSetDao.getByTotalStudentCount(param);
		}
		//获得	noSelectedNum
		int noSelectedNum = 0;
		param.put("cList", cList);
		JSONObject noSelectObj = this.getNoselectedStudentList(param);
		if(noSelectObj!=null){
			noSelectedNum = noSelectObj.getInteger("noSelectedNum");
		}
		returnObj.put("wfWay", wfObj.getString("wfWay"));
		returnObj.put("totalSelectedNum", totalSelectedNum);
		returnObj.put("hasSelectedNum", hasSelectedNum);
		//通过调用getNoSelectList获得 （防止已删除学生的负数现象）
		returnObj.put("noSelectedNum",noSelectedNum);
		
		returnObj.put("subList", subList);
		returnObj.put("isByElection", isByElection);
		returnObj.put("cList", cList);
		return returnObj;
 	}
	public JSONObject getStaticListByStudent(JSONObject param) throws Exception {
		String  classId = param.getString("classId");
		param.remove("classId");
		List<Long> classIdList = StringUtil.toListFromString(classId);
		if(classIdList!=null && !(classIdList.size()>1)){ //传全部时，将classIdList去除，可以获取已删除的班级的学生填报信息
			param.put("classIdList", classIdList);
		}
		String  name = param.getString("name");
		String  schoolYear = param.getString("schoolYear");
		String  termInfo = param.getString("termInfo");
		Long  schoolId = param.getLong("schoolId");
		
		
		JSONObject json = new JSONObject();
		json.put("wfId", param.getString("wfId"));
		json.put("termInfo", param.getString("termInfo") );
		json.put("schoolId", param.getString("schoolId") );
		json.put("schoolYear", param.getString("schoolYear"));
		json.put("termInfoId", param.getString("termInfoId"));
		json.put("classId", param.getString("allClassIds"));
		json.put("areaCode", param.getString("areaCode"));
		JSONObject returnObj = this.getStaticTotalByStudent(json);//获取总统计人数
		//List<Long> subList = (List<Long>) returnObj.get("subList"); //表示设置的科目
		String isByElection = returnObj.getString("isByElection");
		
		
		//获取学生ids填报信息
		List<Long>  studentIds = new ArrayList<Long>();
		List<JSONObject> studentZhAndSubjects = new ArrayList<JSONObject>();
		if("0".equals(isByElection)){ //不是补选，则取zhstudent表的已填学生ids
			studentIds = wishFillingSetDao.getStudentIds(param); //去重
			studentZhAndSubjects = wishFillingSetDao.getStudentZhAndSubject(param);
		}else{
			//补选开始了，则取zhstudentby表中的已填学生ids
			param.put("zhWay", 1);
			studentIds = wishFillingSetDao.getByStudentIds(param); //去重
			studentZhAndSubjects = wishFillingSetDao.getByStudentZhAndSubject(param);
		}
		
		//获取班级对象列表
		Long gradeId = -1L;
		List<Classroom> cList = (List<Classroom>) returnObj.get("cList");//allCommonDataService.getClassroomBatch(schoolId, classIdList, termInfo);
		Map<Long,Classroom> cMap = new HashMap<Long,Classroom>();
		for(Classroom c:cList){
			gradeId = c.getGradeId();
			cMap.put(c.getId(), c);
		}
		//获取年级对象
		Grade g =allCommonDataService.getGradeById(schoolId, gradeId, termInfo);
		if(g==null){
			return null;
		}
		//所有的科目。已排序
		String pycc = Util.getPycc(g.getCurrentLevel().getValue());
		List<JSONObject> subjectList = wishFillingSetDao.getDicSubjectList(schoolId+"",param.getString("areaCode"),pycc,"0");//表示所有科目
		
		Map<Long,String> idNameMap = new HashMap<Long,String>();
		if(subjectList!=null){
			for(JSONObject sub:subjectList){
				Long subjectId = sub.getLong("subjectId");
				idNameMap.put(subjectId, sub.getString("subjectName"));
			}
		}
		//拼接columns（不包括固定列）
		List<List<EasyUIDatagridHead>> columns = new ArrayList<List<EasyUIDatagridHead>>();
		List<EasyUIDatagridHead> subjectHeadList = new ArrayList<EasyUIDatagridHead>();
		for(JSONObject obj:subjectList){
			Long id = obj.getLong("subjectId");
			String subName = idNameMap.get(id);
			subjectHeadList.add(new EasyUIDatagridHead(id+"",subName, "center", 90, 1, 1, false));
		}
		columns.add(subjectHeadList);
		subjectHeadList.add(new EasyUIDatagridHead("operator","操作", "center", 90, 1, 1, false));
		returnObj.put("subjectHeadList", columns);
		//获取学生对象列表
		Map<Long,Account> aMap = new HashMap<Long,Account>();
		List<Account> aList = allCommonDataService.getAccountBatch(schoolId, studentIds, termInfo);
		for(Account a:aList){
			aMap.put(a.getId(), a);
		}
		//返回row
		List<JSONObject> staticList = new ArrayList<JSONObject>();
		List<JSONObject> hasDelStaticList = new ArrayList<JSONObject>();
		Set<Long> classIdSet = new HashSet<Long>();
		Set<Long> accountIdSet = new HashSet<Long>();
		Map<Long,Integer> subMap = new HashMap<Long,Integer>();
		for(JSONObject obj:subjectList){
			Long ss = obj.getLong("subjectId");
			subMap.put(ss,0);
		}
		for(JSONObject obj:studentZhAndSubjects){
			int isDeleted = 0 ;
			Long accountId = obj.getLong("accountId");
			Long cId = obj.getLong("classId");
			
			JSONObject row = new JSONObject();
			boolean isShow = true;
			row.put("accountId",accountId);
			
			row.put("gradeId",allCommonDataService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", schoolYear));
			Account a = aMap.get(accountId);
			Classroom c = cMap.get(cId);
			if(a!=null){
				row.put("accountName", a.getName());
				if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(a.getName()) && !a.getName().contains(name))
				{
					isShow=false;
				}
				
			}else{
				isDeleted=1;
				row.put("accountName", "[已删除]");
			}
			if(c!=null){
				cId=c.getId();
				row.put("classId", c.getId());
				row.put("className", c.getClassName());
			}else{
				row.put("classId", "[已删除]");
				row.put("className", "[已删除]");
			}
			if(isShow&&StringUtils.isBlank(name)   ||  isShow && StringUtils.isNotBlank(name) && isDeleted!=1){
				String subjectIds = obj.getString("subjectIds");
				List<Long> selectedSubs = StringUtil.toListFromString(subjectIds);
				for(JSONObject obj1:subjectList){
					Long s = obj1.getLong("subjectId");
					if(selectedSubs.contains(s)){
						row.put(s+"", 1);
						Integer subCount = subMap.get(s);
						subMap.put(s, ++subCount); //计算选择的科目总个数
					}else{
						row.put(s+"", 0);
					}
				}
				classIdSet.add(cId);//计算选择班级总个数
				accountIdSet.add(accountId);//计算选择账户总个数
				if(isDeleted==0){
					row.put("operator", "调整");
				}else{
					row.put("operator", "删除");
				}
				if(isDeleted==0){
					staticList.add(row);
				}else{
					hasDelStaticList.add(row);
				}
			}
		}
		//按班级名称排序
		staticList = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, staticList, "className");
		staticList.addAll(0, hasDelStaticList);
		//得到序号
		int count=1;
		for(JSONObject obj : staticList){
			obj.put("index", count++);
		}
		//处理最后一列
		//List<JSONObject> subjectHasSelected = (List<JSONObject>) returnObj.get("subjectHasSelected");
		JSONObject row = new JSONObject();
		row.put("index", "总计");
		row.put("className", classIdSet.size());
		row.put("accountName", accountIdSet.size());
		row.put("operator", "-");
		row.put("classId", "");
		row.put("gradeId", "");
		row.put("accountId", "");
		//存放最后一行的科目选择总数
		for (Map.Entry<Long, Integer> entry : subMap.entrySet()) 
		{
			row.put(entry.getKey()+"", entry.getValue());
		}
		staticList.add(row);
		returnObj.put("staticList", staticList);
		if("3".equals(pycc)){ //初中部
			returnObj.put("selectWay", 3); //默认5选3
		}else{
			returnObj.put("selectWay", 1); //默认6选3
			if(subjectList.size()==7){
				returnObj.put("selectWay", 2); //7选3
			}
		}
		return returnObj;
	}
	
	@Override
	public JSONObject getNoselectedStudentList(JSONObject param) throws Exception {
		JSONObject returnObj = new JSONObject();
		String wfId = param.getString("wfId");
		Long schoolId = param.getLong("schoolId");
		String termInfo = param.getString("termInfo");
		String classId = param.getString("classId");
		List<Classroom> cList = (List<Classroom>) param.get("cList");
		List<Long> classIdList = StringUtil.toListFromString(classId);
	    JSONObject wfObj = wishFillingSetDao.getTb(param);
	    String isByElection = wfObj.getString("isByElection");
	    if(cList==null){
	    	cList = allCommonDataService.getClassroomBatch(schoolId, classIdList, termInfo);
	    }
		Set<Long> shouldAccountIdSet = new LinkedHashSet<Long>();//accountIds
		Map<Long,String> scMap = new HashMap<Long,String>(); //accountId - className
		for(Classroom c:cList){
		  String cName = c.getClassName();
		  List<Long> accountIds=c.getStudentAccountIds();
		  if(accountIds!=null){
		    for(Long a:accountIds){
			  scMap.put(a, cName);
		  	}
		  	shouldAccountIdSet.addAll(accountIds); 
		  }
		}
		
		List<Long> noStudentIds = new ArrayList<Long>( shouldAccountIdSet ) ;
		List<Long> hasSelectedStudentIds = new ArrayList<Long>();
		if("0".equals(isByElection)){
			hasSelectedStudentIds = wishFillingSetDao.getHasSelectedStudentIds(param);
		}else{
			param.put("zhWay", 1);
		    hasSelectedStudentIds = wishFillingSetDao.getByHasSelectedStudentIds(param);
		}
		noStudentIds.removeAll(hasSelectedStudentIds); //得到未选人
		
		List<Account> aList = allCommonDataService.getAccountBatch(schoolId, noStudentIds, termInfo);
		List<JSONObject> noSelectedList = new ArrayList<JSONObject>();
		for(Account a: aList){
			JSONObject noSelectedObj = new JSONObject();
			noSelectedObj.put("accountName", a.getName());
			noSelectedObj.put("className", scMap.get(a.getId()));
			noSelectedList.add(noSelectedObj);
		}
		returnObj.put("noSelectedList", noSelectedList);
		returnObj.put("noSelectedNum", noSelectedList.size());
		return returnObj;
	}
	@Override
	public JSONObject getStudentTb(JSONObject param) throws Exception {
		JSONObject returnObj = new JSONObject();
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		Integer wfNum = wfObj.getInteger("wfNum");
		String wfWay = wfObj.getString("wfWay");
		String isByElection = wfObj.getString("isByElection");
		List<JSONObject> subjects = new ArrayList<JSONObject>();
		List<JSONObject> zhs = new ArrayList<JSONObject>();
		if("0".equals(isByElection) && "0".equals(wfWay)){ // 补选未开始 && 单科选择的情况 
			List<JSONObject> dicSubjectList = wishFillingSetDao.getDicSubjectList(wfObj.getString("schoolId"),param.getString("areaCode"),wfObj.getString("pycc"),"0");
			Map<Long,JSONObject> subMap = new HashMap<Long,JSONObject>(); 
			if(dicSubjectList!=null){
				for(JSONObject dicSub:dicSubjectList){
					Long subjectId = dicSub.getLong("subjectId");
					subMap.put(subjectId, dicSub);
				}
			}
			String subjectIds = wfObj.getString("subjectIds");
			List<Long> subjectList = StringUtil.toListFromString(subjectIds); //选择的科目
			List<JSONObject> studentTbList =  wishFillingSetDao.getStudentTb(param);
			for(Long id:subjectList){
				JSONObject subObj = subMap.get(id);
				if(subObj!=null){
					JSONObject obj = new JSONObject();
					Long subjectId = subObj.getLong("subjectId");
					String subjectName = subObj.getString("subjectName");
					String selectRule = subObj.getString("selectRule");
					obj.put("subjectId", subjectId);
					obj.put("subjectName", subjectName);
					obj.put("isSelected", 0);
					obj.put("selectRule", selectRule);
					for(JSONObject studentTb:studentTbList){
						long subjectIdTb = studentTb.getLongValue("subjectId");
						if(subjectId==subjectIdTb){
							obj.put("isSelected", 1);
							break;
						}
					}
					subjects.add(obj);
				}
			}
		}else{
			List<JSONObject> zhList = wishFillingSetDao.getZhListByTb(param);
			//查询组合
			JSONObject studentZhTb  = new JSONObject();
			if("0".equals(isByElection)){
				studentZhTb = wishFillingSetDao.getZhStudent(param);
			}else{
				studentZhTb = wishFillingSetDao.getByZhStudent(param);
			}
			String studentZhId = studentZhTb.getString("zhId");
			for(JSONObject zh:zhList){
				String zhWay = zh.getString("zhWay");
				if("0".equals(zhWay)&& "0".equals(wfWay) &&"1".equals(isByElection)){ //补选的情况下，不固定则不出
					continue;
				}
				JSONObject obj = new JSONObject();
				String zhId = zh.getString("zhId");
				String zhName = zh.getString("zhName");
				obj.put("zhId", zhId);
				obj.put("zhName", zhName);
				obj.put("isSelected", 0);
				if(zhId.equals(studentZhId)){
					obj.put("isSelected", 1);
				}
				zhs.add(obj);
			}
		}
		returnObj.put("wfNum", wfNum);
		returnObj.put("wfWay", wfWay);
		returnObj.put("isByElection", isByElection);
		returnObj.put("subjects", subjects);
		returnObj.put("zhs", zhs);
		return returnObj;
	}
	@Override
	public List<JSONObject> getStaticListBySubject(JSONObject param) throws Exception {
		Long schoolId = param.getLong("schoolId");
		String termInfo = param.getString("termInfo");
		String wfId = param.getString("wfId");
		String sIds = param.getString("subjectId");
		List<Long> sIdList = StringUtil.toListFromString(sIds);
		
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String pycc = wfObj.getString("pycc");
		List<JSONObject> dicSubjectList = new ArrayList<JSONObject>();
		if("3".equals(pycc)){
			dicSubjectList = wishFillingSetDao.getDicSubjectList(schoolId+"", param.getString("areaCode"), pycc, "0");
		}
		Map<String,JSONObject> isDividedMap = new HashMap<String,JSONObject>(); 
		if(dicSubjectList!=null){
			for(JSONObject sub :dicSubjectList){
				String isDivided = sub.getString("isDivided");
				if("1".equals(isDivided)){
					isDividedMap.put(sub.getString("subjectId"), sub);
				}
			}
		}
		String wfWay = wfObj.getString("wfWay");
		String isByElection = wfObj.getString("isByElection");
		String subjectSortIds = wfObj.getString("subjectIds");
		List<Long> subjectSortList = StringUtil.toListFromString(subjectSortIds);
		//如果按组合选，则前端下拉框也是出的所有科目，传入后端的应该剔除不在组合中的科目
		if("1".equals(wfWay)&& sIdList.size()>1){
			sIds = wfObj.getString("subjectIds");
			sIdList = StringUtil.toListFromString(sIds);
		}
		if(sIdList!=null && sIdList.size()==1){//下拉框勾选“政治” ，而设置组合又不包含政治时
			String ss = wfObj.getString("subjectIds"); 
			List<Long> ssList = StringUtil.toListFromString(ss);
			if(!ssList.contains(sIdList.get(0))){
				return new ArrayList<JSONObject>();
			}
		}
		param.put("sIdList", sIdList);
		/*
		 * Collections.sort(sIdList);
		 * Map<Long,Boolean> isUsedMap = new LinkedHashMap<Long,Boolean>();
		for(Long s:sIdList){
			isUsedMap.put(s, false);//初始化
		}*/
		param.remove("subjectId");
		List<JSONObject> studentTbsGroupSub = new ArrayList<JSONObject>();
		if("0".equals(wfWay) && "1".equals(isByElection)){
			List<JSONObject> zhList = wishFillingSetDao.getZhListByTb(param);
			List<String> zhIdList = new ArrayList<String>();
			for(JSONObject zh:zhList){
				String zhWay = zh.getString("zhWay");
				if("0".equals(zhWay)){continue;}
				zhIdList.add(zh.getString("zhId"));
			}
			//获取该组合下的accountIds 
			JSONObject json = new JSONObject();
			json.put("wfId", wfId);
			json.put("schoolId", schoolId);
			json.put("termInfo", termInfo);
			json.put("zhIdList", zhIdList);
			List<JSONObject> AccountList = wishFillingSetDao.getByStudentByFixedZhIds(json);
			List<String> accountIdList = new ArrayList<String>();
			for(JSONObject account:AccountList){
				accountIdList.add(account.getString("accountId"));
			}
			if(accountIdList.size()>0){
				param.put("accountIdList", accountIdList);
				studentTbsGroupSub = wishFillingSetDao.getByStaticListBySubject(param);
			}
		}else{
			studentTbsGroupSub = wishFillingSetDao.getStaticListBySubject(param);
		}
		//List<Long> subjectIds = new ArrayList<Long>();
		Set<Long> accountIdSet =  new HashSet<Long>();
		Set<Long> classIdSet =  new HashSet<Long>();
		for(JSONObject ss:studentTbsGroupSub){
			String accountIds = ss.getString("accountIds");
			List<Long> aIds = StringUtil.toListFromString(accountIds);
			accountIdSet.addAll(aIds);
			Long classId = ss.getLong("classId");
			classIdSet.add(classId);
			//Long subjectId = ss.getLong("subjectId");
			//subjectIds.add(subjectId);
			//isUsedMap.put(subjectId, true);
		}
		Map<Long,Account> aMap = new HashMap<Long,Account>(); 
		List<Account> aList = allCommonDataService.getAccountBatch(schoolId,new ArrayList<Long>( accountIdSet), termInfo);
		for(Account a: aList){
			aMap.put(a.getId(), a);
		}
		Map<Long,Classroom> cMap = new HashMap<Long,Classroom>(); 
		List<Classroom> cList = allCommonDataService.getSimpleClassBatch(schoolId, new ArrayList<Long>(classIdSet), termInfo);
		for(Classroom c:cList){
			cMap.put(c.getId(), c);
		}
		List<LessonInfo> lList = allCommonDataService.getLessonInfoBatch(schoolId, sIdList, termInfo);
		Map<Long,LessonInfo> lMap = new HashMap<Long,LessonInfo>(); 
		for(LessonInfo l:lList){
			lMap.put(l.getId(), l);
		}
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		int count=1;
		//for(long ssId:subjectSortList){
		for(JSONObject ss:studentTbsGroupSub){
			
			String accountIds = ss.getString("accountIds");
			List<Long> aIds = StringUtil.toListFromString(accountIds);
			long classId = ss.getLongValue("classId");
			long subjectId = ss.getLongValue("subjectId");
			//if(subjectId!=ssId){continue;}
			LessonInfo l = lMap.get(subjectId);
			for(int i =0;i<aIds.size();i++){
				JSONObject returnObj = new JSONObject();
				if(l!=null){
					returnObj.put("subjectName", l.getName());
				}else{
					if(isDividedMap.get(subjectId+"")!=null){
						returnObj.put("subjectName", isDividedMap.get(subjectId+"").getString("subjectName"));
					}else{
					returnObj.put("subjectName", "[已删除]");
					}
				}
				Long accountId = aIds.get(i);
				Account a = aMap.get(accountId);
				Classroom c = cMap.get(classId);
				if(a!=null){
					returnObj.put("accountName", a.getName());
				}else{
					returnObj.put("accountName", "[已删除]");
				}
				if(c!=null){
					returnObj.put("className", c.getClassName());
				}else{
					returnObj.put("className", "[已删除]");
				}
				returnObj.put("index", count++);
				returnList.add(returnObj);
			}
		}
		//}
		
		//没选的出默认效果
		/*for (Map.Entry<Long, Boolean> entry : isUsedMap.entrySet()) 
		{
			 if(false==entry.getValue()){
				 JSONObject returnObj = new JSONObject();
				 returnObj.put("index", count++);
				 returnObj.put("className", "-");
				 returnObj.put("accountName", "-");
				 LessonInfo l = lMap.get(entry.getKey());
				 if(l!=null){
						returnObj.put("subjectName", l.getName());
					}else{
						returnObj.put("subjectName", "[已删除]");
				 }
				 returnList.add(returnObj);
			 }
		
		}*/
		return returnList;
	}
	@Override
	public List<JSONObject> getStaticListByZh(JSONObject param) throws Exception {
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String wfWay = wfObj.getString("wfWay");
		String isByElection = wfObj.getString("isByElection");
		List<JSONObject> staticZhs = new ArrayList<JSONObject>();
		int totalNum = 0;
		String classId = param.getString("classId");
		param.remove("classId");
		List<Long> classIdList = StringUtil.toListFromString(classId);
		if(classIdList!=null && !(classIdList.size()>1)){
			param.put("classIdList", classIdList);
		}
		if("0".equals(wfWay)&& "1".equals(isByElection)){
			param.put("zhWay", 1);
			staticZhs = wishFillingSetDao.getByStaticListByZh(param);
			totalNum = wishFillingSetDao.getByZhTotalStudentNum(param);
		}else{
			staticZhs = wishFillingSetDao.getStaticListByZh(param);
			totalNum = wishFillingSetDao.getZhTotalStudentNum(param);
		}
		int count=1;
		//List<String> zhIds = new ArrayList<String>();
		for(JSONObject zhObj:staticZhs){
			JSONObject returnObj = new JSONObject();
			String zhName = zhObj.getString("zhName");
			String zhId = zhObj.getString("zhId");
			Integer studentNum = zhObj.getInteger("studentNum");
			String rate = StringUtil.formatNumber((float)studentNum/totalNum*100, 2);
			returnObj.put("zhId", zhId);
			returnObj.put("zhName", zhName);
			returnObj.put("studentNum", studentNum);
			returnObj.put("index", count++);
			returnObj.put("rate", rate+"%");
			returnList.add(returnObj);
			//zhIds.add(zhId);
		}
		/*List<JSONObject> list = wishFillingSetDao.getZhSubject(param);
		for(JSONObject obj:list){
			String zhId = obj.getString("zhId");
			String zhName = obj.getString("zhName");
			if(!zhIds.contains(zhId)){
				JSONObject returnObj = new JSONObject();
				returnObj.put("zhId", zhId);
				returnObj.put("zhName", zhName);
				returnObj.put("studentNum", 0);
				returnObj.put("index", count++);
				returnObj.put("rate", "-");
				returnList.add(returnObj);
			}
		}*/
		return returnList;
	}
	@Override
	public List<JSONObject> getStudentZh(JSONObject param) throws Exception {
		String termInfo = param.getString("termInfo");
		Long schoolId = param.getLong("schoolId");
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		String classId = param.getString("classId");
		param.remove("classId");
		List<Long> classIdList = StringUtil.toListFromString(classId);
		
		List<Classroom> cList = allCommonDataService.getSimpleClassBatch(schoolId, classIdList, termInfo);
		Map<Long,Classroom> cMap = new HashMap<Long,Classroom>();
		for(Classroom c:cList){
			cMap.put(c.getId(), c);
		}
		if(!(classIdList.size()>1)){
			param.put("classIdList", classIdList);
		}
		
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String wfWay = wfObj.getString("wfWay");
		String isByElection = wfObj.getString("isByElection");
		List<JSONObject> zhStudents = new ArrayList<JSONObject>();
		if("0".equals(wfWay) && "1".equals(isByElection)){
			zhStudents = wishFillingSetDao.getByStudentZh(param);
		}else{
			zhStudents = wishFillingSetDao.getStudentZh(param);
		}
		Set<Long> accountSet = new HashSet<Long>();
		for(JSONObject obj: zhStudents){
			Long accountId = obj.getLong("accountId");
			accountSet.add(accountId);
		}
		Map<Long,Account> aMap = new HashMap<Long,Account>();
		List<Account> aList = allCommonDataService.getAccountBatch(schoolId, new ArrayList<Long>(accountSet), termInfo);
		for(Account a: aList){
			aMap.put(a.getId(), a);
		}
		
		for(JSONObject obj: zhStudents){
			JSONObject returnObj = new JSONObject();
			String zhName = obj.getString("zhName");
			Long accountId = obj.getLong("accountId");
			Long cId = obj.getLong("classId");
			Classroom c = cMap.get(cId);
			Account a = aMap.get(accountId);
			returnObj.put("zhName", zhName);
			if(c!=null){
				returnObj.put("className", c.getClassName());
			}else{
				returnObj.put("className", "[已删除]");
			}
			if(a!=null){
				returnObj.put("accountName", a.getName());
			}else{
				returnObj.put("accountName", "[已删除]");
			}
			returnList.add(returnObj);
		}
		return returnList;
	}
	@Override
	public JSONArray exportStudentZh(JSONObject param) throws Exception {
		Long schoolId = param.getLong("schoolId");
		String termInfo = param.getString("termInfo");
		JSONArray exportList = new JSONArray();
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String wfWay = wfObj.getString("wfWay");
		String isByElection = wfObj.getString("isByElection");
		List<JSONObject> allStudentZhs = new ArrayList<JSONObject>();
		List<JSONObject> allStudentZhsNoGroupBy = new ArrayList<JSONObject>();
		//需要根据填报的学生数排序（组合与组合之间）
		if("0".equals(wfWay) && "1".equals(isByElection)){
			 param.put("zhWay", "1");
	         allStudentZhs = wishFillingSetDao.getByAllStudentZh(param);
	         allStudentZhsNoGroupBy=wishFillingSetDao.getByAllStudentZhNoGroupBy(param);
		}else{
			 allStudentZhs = wishFillingSetDao.getAllStudentZh(param);
			 allStudentZhsNoGroupBy=wishFillingSetDao.getAllStudentZhNoGroupBy(param);
		}
		
		Set<Long> classIdSet = new HashSet<Long>();
		Set<Long> accountIdSet = new HashSet<Long>();
		Set<String> zhIdSets = new LinkedHashSet<String>();
		for(JSONObject studentZh: allStudentZhs)
		{
			String zhId = studentZh.getString("zhId");
			if(!zhIdSets.contains(zhId)){
				zhIdSets.add(zhId);
			}
			String accountIds = studentZh.getString("accountIds");
			String classIds = studentZh.getString("classIds");
			List<Long> accountIdList = StringUtil.toListFromString(accountIds);
			List<Long> classIdList = StringUtil.toListFromString(classIds);
			classIdSet.addAll(classIdList);
			accountIdSet.addAll(accountIdList);
		}
		List<Account> aList = allCommonDataService.getAccountBatch(schoolId, new ArrayList<Long>(accountIdSet), termInfo);
		Map<Long,Account> aMap = new HashMap<Long,Account>();
		for(Account a: aList){
			aMap.put(a.getId(), a);
		}
		List<Classroom> cList = allCommonDataService.getSimpleClassBatch(schoolId, new ArrayList<Long>(classIdSet), termInfo);
		Map<Long,Classroom> cMap = new HashMap<Long,Classroom>();
		for(Classroom c:cList){
			cMap.put(c.getId(), c);
		}
		
		/*for(JSONObject studentZh: allStudentZhs)
		{
			String zhName = studentZh.getString("zhName");
			String accountIds = studentZh.getString("accountIds");
			String classIds = studentZh.getString("classIds");
			List<Long> accountIdList = StringUtil.toListFromString(accountIds);
			List<Long> classIdList = StringUtil.toListFromString(classIds);
			for(int i=0;i<accountIdList.size();i++){
				JSONObject returnObj = new JSONObject();
				returnObj.put("zhName", zhName);
				Long accountId = accountIdList.get(i);
				returnObj.put("accountId", accountId);
				Long classId = classIdList.get(i);
				Account a = aMap.get(accountId);
				Classroom c = cMap.get(classId);
				if(a!=null){
					returnObj.put("accountName", a.getName());
				}else{
					returnObj.put("accountName", "[已删除]");
				}
				
				if(c!=null){
					returnObj.put("className", c.getClassName());
				}else{
					returnObj.put("className", "[已删除]");
				}
				exportList.add(returnObj);
			}
		}*/
		for(String sortedZhId:zhIdSets){
			for(JSONObject studentZh:allStudentZhsNoGroupBy){
				String zhId = studentZh.getString("zhId");
				if(!sortedZhId.equals(zhId)){continue;}
				JSONObject returnObj = new JSONObject();
				String zhName = studentZh.getString("zhName");
				Long accountId = studentZh.getLong("accountId");
				Long classId = studentZh.getLong("classId");
				returnObj.put("zhName", zhName);
				Account a = aMap.get(accountId);
				Classroom c = cMap.get(classId);
				if(a!=null){
					returnObj.put("accountName", a.getName());
				}else{
					returnObj.put("accountName", "[已删除]");
				}
				
				if(c!=null){
					returnObj.put("className", c.getClassName());
				}else{
					returnObj.put("className", "[已删除]");
				}
				exportList.add(returnObj);
			}
		}
		
		return exportList;
	}
	@Override
	public int getTotalStudentCount(JSONObject param) throws Exception {
		return wishFillingSetDao.getTotalStudentCount(param);
	}
	@Override
	public List<JSONObject> getDicSubjectList(String sId,String areaCode,String pycc ,String isDivided) throws Exception {
		return wishFillingSetDao.getDicSubjectList(sId,areaCode,pycc, isDivided);
	}
	@Override
	public List<JSONObject> getTbNameList(JSONObject param) throws Exception {
		return wishFillingSetDao.getTbNameList(param);
	}
	@Override
	public List<JSONObject> getZhListByTb(JSONObject param) throws Exception {
		return wishFillingSetDao.getZhListByTb(param);
	}
	@Override
	public List<JSONObject> getByAllZhStudent(JSONObject param) throws Exception {
		return wishFillingSetDao.getByAllZhStudent(param);
	}
	@Override
	public int deleteStudentTb(JSONObject param) throws Exception {
		List<JSONObject> zhList = wishFillingSetDao.getZhListByTb(param);
		Map<String,JSONObject> zhMap = new HashMap<String,JSONObject>();
		for(JSONObject zh:zhList){
			zhMap.put( zh.getString("zhId"), zh);
		}
		JSONObject zhStudentObj = wishFillingSetDao.getZhStudent(param);
		if(zhStudentObj!=null){
			String zhId = zhStudentObj.getString("zhId");
			param.put("zhId", zhId);
			JSONObject zhObj=zhMap.get(zhId);
			if(zhObj!=null){
					String zhWay = zhObj.getString("zhWay");
					if("1".equals(zhWay) ){//固定
						//获取学生补选信息 看该学生是正选就选了 还是补选选的
						JSONObject zhStudent = wishFillingSetDao.getByZhStudent(param);
						if(zhStudent!=null){
							String isFixedZh = zhStudent.getString("isFixedZh");
							if("1".equals(isFixedZh)){//该学生是正选就选了的
								//修改fixedHasNum -1 
								wishFillingSetDao.updateZhSubjectFixedHasNum(param);
								
							}
						}
			    }
			}
			param.remove("zhId");
		}
	
		
		//删除学生填报
		wishFillingSetDao.deleteStudentTb(param);
	    wishFillingSetDao.deleteByStudentTb(param);
	    //删除学生组合
	    wishFillingSetDao.deleteZhStudent(param);
	    wishFillingSetDao.deleteByZhStudent(param);
		return 1;
	}
	@Override
	public int getByTotalStudentCount(JSONObject param) throws Exception {
		return wishFillingSetDao.getByTotalStudentCount(param);
	}
	@Override
	public int sendWx(JSONObject param) {
		String gradeName = param.getString("gradeName");
		String wfStartTime = param.getString("wfStartTime" );
		String  wfEndTime = param.getString("wfEndTime" );
		String byStartTime = param.getString("byStartTime"  );
		String byEndTime= param.getString("byEndTime" );
		String wfName = param.getString("wfName"  );
		String schoolExtId = param.getString("schoolExtId" );
		String schoolName = param.getString("schoolName");
		JSONObject returnObj = new JSONObject();
		String wfId = param.getString("wfId");
		Long schoolId = param.getLong("schoolId");
		String termInfo = param.getString("termInfo");
		String classId = param.getString("classId");
		List<Classroom> cList = (List<Classroom>) param.get("cList");
		List<Long> classIdList = StringUtil.toListFromString(classId);
	    JSONObject wfObj = wishFillingSetDao.getTb(param);
	    String isByElection = wfObj.getString("isByElection");
	    if(cList==null){
	    	cList = allCommonDataService.getClassroomBatch(schoolId, classIdList, termInfo);
	    }
		Set<Long> shouldAccountIdSet = new LinkedHashSet<Long>();//accountIds
		Map<Long,String> scMap = new HashMap<Long,String>(); //accountId - className
		for(Classroom c:cList){
		  String cName = c.getClassName();
		  List<Long> accountIds=c.getStudentAccountIds();
		  if(accountIds!=null){
		    for(Long a:accountIds){
			  scMap.put(a, cName);
		  	}
		  	shouldAccountIdSet.addAll(accountIds); 
		  }
		}
		
		List<Long> noStudentIds = new ArrayList<Long>( shouldAccountIdSet ) ;
		List<Long> hasSelectedStudentIds = new ArrayList<Long>();
		if("0".equals(isByElection)){
			hasSelectedStudentIds = wishFillingSetDao.getHasSelectedStudentIds(param);
		}else{
			param.put("zhWay", 1);
		    hasSelectedStudentIds = wishFillingSetDao.getByHasSelectedStudentIds(param);
		}
		noStudentIds.removeAll(hasSelectedStudentIds); //得到未选人
		
		List<Account> studentAccountList = allCommonDataService.getAccountBatch(schoolId, noStudentIds, termInfo);
		List<JSONObject> listParent = allCommonDataService.getSimpleParentByStuMsg(noStudentIds, termInfo, schoolId);
		//一、封装接收人列表
		//学生接收人列表
		List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>();
		for (int i = 0; i < studentAccountList.size(); i++) {
			Account account = studentAccountList.get(i);
			//1.填学生接收者
			JSONObject student = new JSONObject();
			student.put("userId", account.getExtId());
			student.put("userName", account.getName());
			student.put("userType", 3);		//  kafka中3为学生
			msgCenterReceiversArray.add(student);
		}
		// 家长接收人列表
		List<String> parentNoRepeat = new ArrayList<String>();
		for(JSONObject parentObj:listParent) {
			JSONObject parent = new JSONObject();
			String studentName = parentObj.getString("studentName");
			String parentName = parentObj.getString("name");
			String parentUserId= parentObj.getString("extUserId");
				if(!parentNoRepeat.contains(parentUserId)){
					parent.put("userId", parentUserId );
					parent.put("userName", parentName);
					parent.put("userType", 4);			//  kafka中4为家长
					parent.put("userStudentName", studentName );
					msgCenterReceiversArray.add(parent);
					parentNoRepeat.add(parentUserId);
				}
		}
		//二、填消息体
		 if(msgCenterReceiversArray.size()>0) {
			JSONObject msg = new JSONObject();
		    String msgId = UUIDUtil.getUUID().replace("-", "");
			msg.put("msgId", msgId);
			msg.put("msgTemplateType", "XSXK");
			msg.put("msgTitle",wfName);
			msg.put("msgContent",  wfName);
			msg.put("msgUrlPc", msgUrlPc+"?id="+wfId );
			msg.put("msgUrlApp",  msgUrlApp+"&id="+wfId );
			msg.put("msgOrigin", "3+1+2选科"); //业务模块名称
			msg.put("msgTypeCode", "XK312");
			msg.put("schoolId", schoolExtId);
			msg.put("creatorName", schoolName);
			
			JSONObject first = new JSONObject();
			first.put("value", "你好，学校已经创建了新高考选科任务，请学生在规定时间内在电脑/微信完成选科操作。");
			JSONObject keyword1 = new JSONObject();
			JSONObject keyword2 = new JSONObject();
			if("0".equals(isByElection)){
				if(StringUtils.isNotBlank(wfStartTime)&& StringUtils.isNotBlank(wfEndTime)) {
					keyword1.put("value", wfStartTime );
					keyword2.put("value", wfEndTime);
			   }
			}else {
				if(StringUtils.isNotBlank(byStartTime)&& StringUtils.isNotBlank(byEndTime)) {
					keyword1.put("value", byStartTime );
					keyword2.put("value", byEndTime);
				}
			}
			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", gradeName);
			
			
			JSONObject remark = new JSONObject();
			remark.put("value", "请尽快完成选科，如有疑问请联系老师。");
			
			JSONObject data = new JSONObject();
			data.put("first", first);
			data.put("keyword1", keyword1);
			data.put("keyword2", keyword2);
			data.put("keyword3", keyword3);
			data.put("url", msgUrlApp+"&id="+wfId);
			data.put("remark", remark);
			msg.put("msgWxJson", data);
			
			JSONObject msgCenterPayLoad = new JSONObject();
			msgCenterPayLoad.put("msg", msg);
			msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
			logger.info("msg=====>" + msg.toString());
			logger.info("msgCenterReceiversArray=====>" + msgCenterReceiversArray.toString());
			logger.info("msgCenterReceiversArray size=====>" + msgCenterReceiversArray.size());
			logger.info("msgCenterPayLoad=====>" + msgCenterPayLoad.toString());
			// 发送消息操作
			logger.info("msg kfk=====>"+"kafkaUrl"+" clientId:"+clientId+" clientSecret:"+clientSecret );
			KafkaUtils.sendAppMsg(kafkaUrl,wfId, msgCenterPayLoad , "XK312", clientId,clientSecret);
		 }
		return 1;
	}
}
