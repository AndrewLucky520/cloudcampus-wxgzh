package com.talkweb.wishFilling.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.dao.WishFillingTeacherDao;
import com.talkweb.wishFilling.service.WishFillingService;
import com.talkweb.wishFilling.service.WishFillingTeacherService;
import com.talkweb.wishFilling.util.Util;

/** 
 * 志愿填报-老师serviceImpl
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Service
public class WishFillingTeacherServiceImpl implements WishFillingTeacherService {

	@Autowired
	private WishFillingTeacherDao wishFillingTeacherDao;
	@Autowired
	private WishFillingSetDao wishFillingSetDao;
	@Autowired
	private WishFillingService wishFillingService;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(WishFillingTeacherServiceImpl.class);
	@Override
	public List<JSONObject> getTbList(JSONObject param) throws Exception {
		return  wishFillingTeacherDao.getTbListTeacher(param);
	}
	@Override
	public JSONObject getStaticListByStudent(JSONObject param) throws Exception {
		String  classId = param.getString("classId");
		param.remove("classId");
		List<Long> classIdList = StringUtil.toListFromString(classId);
		/*if(!(classIdList.size()>1)){ //传全部时，将classIdList去除，可以获取已删除的班级的学生填报信息
			param.put("classIdList", classIdList);
		}*/
		param.put("classIdList", classIdList);
		
		String  name = param.getString("name");
		String  schoolYear = param.getString("schoolYear");
		String  termInfo = param.getString("termInfo");
		Long  schoolId = param.getLong("schoolId");
		
		
		JSONObject json = new JSONObject();
		json.put("wfId", param.getString("wfId"));
		json.put("schoolId", param.getString("schoolId"));
		json.put("termInfo", param.getString("termInfo"));
		json.put("schoolYear", param.getString("schoolYear"));
		json.put("termInfoId", param.getString("termInfoId"));
		json.put("classIdList", param.get("allClassIdList"));
		json.put("classId", param.getString("allClassIds"));
		json.put("areaCode", param.getString("areaCode"));
		JSONObject returnObj = this.getProgressTbByTeacher(json);//获取总统计人数
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
		List<Classroom> cList = new ArrayList<Classroom>();
		if(classIdList!=null && classIdList.size()>0){
		    cList = allCommonDataService.getClassroomBatch(schoolId, classIdList, termInfo);
		}
		logger.info("wishFilling: cList:"+cList +"  classIdList: "+classIdList);
		Map<Long,Classroom> cMap = new HashMap<Long,Classroom>();
		for(Classroom c:cList){
			gradeId = c.getGradeId();
			cMap.put(c.getId(), c);
		}
		//获取年级对象
		Grade g =allCommonDataService.getGradeById(schoolId, gradeId, termInfo);
		//所有的科目。已排序
		logger.info("wishFilling:"+g+"入参 schoolId:"+schoolId+" gradeId: "+gradeId+" termInfo: "+termInfo);
		List<JSONObject> subjectList = wishFillingSetDao.getDicSubjectList(schoolId+"", param.getString("areaCode"),Util.getPycc(g.getCurrentLevel().getValue()),"0");//表示所有科目
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
		return returnObj;
	}
	
	@Override
	public JSONObject getProgressTbByTeacher(JSONObject param) throws Exception {
		JSONObject returnObj = new JSONObject();
		Long schoolId = param.getLong("schoolId");
		JSONObject wfObj = wishFillingSetDao.getTb(param);
		String subjectIds = wfObj.getString("subjectIds");
		List<Long> subList = StringUtil.toListFromString(subjectIds);
		String isByElection = wfObj.getString("isByElection");
		String termInfo =param.getString("termInfo");
		String schoolYear = termInfo.substring(0, 4);
		//使用年级
		String useGrade = wfObj.getString("wfGradeId");
		//获取年级名字
		String currentLevel = allCommonDataService.ConvertSYNJ2NJDM(useGrade, schoolYear);
		Grade g=allCommonDataService.getGradeByGradeLevel(schoolId, T_GradeLevel.findByValue(Integer.parseInt(currentLevel)), termInfo);
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		//获取总选课人数
		List<Long> gClassIds = new ArrayList<Long>();
		if(g.isGraduate){
			gClassIds.add(-1L);
		}else{
			gClassIds = g.getClassIds();
		}
		//List<Long> gClassIds = g.getClassIds(); //获取这个年级下的所有班级
		List<Long> ids = (List<Long>) param.get("classIdList");
		//除去任教关系下的不属于设置年级的班级
		List<Long> classIdList = new ArrayList<Long>();
		for(Long cId:ids){
			if(gClassIds!=null && gClassIds.contains(cId)){//判断任教关系下的班级 是否为开设年级选课的班级
				classIdList.add(cId);
			}
		}
		if(classIdList.size()>0){
			param.put("classIdList", classIdList);
		}else{
			Long cId = -1l;
			classIdList.add(cId);
			param.put("classIdList", classIdList);
		}
		int totalSelectedNum=0;
		List<Classroom> cList = new ArrayList<Classroom>();
		if(classIdList.size()>0){
		   cList = allCommonDataService.getClassroomBatch(schoolId, classIdList, termInfo);
		}
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
		JSONObject noSelectObj = wishFillingService.getNoselectedStudentList(param);
		if(noSelectObj!=null){
			noSelectedNum = noSelectObj.getInteger("noSelectedNum");
		}
		returnObj.put("wfWay", wfObj.getString("wfWay"));
		returnObj.put("totalSelectedNum",totalSelectedNum ); //总选课人数
		returnObj.put("hasSelectedNum",hasSelectedNum ); //已选课人数
		returnObj.put("noSelectedNum",noSelectedNum ); //未选课人数
		
		returnObj.put("subList", subList);
		returnObj.put("isByElection", isByElection);
		return returnObj;
	}
}
