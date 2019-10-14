package com.talkweb.exammanagement.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.SortUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.dao.ExamManagementDao;
import com.talkweb.exammanagement.dao.ExamManagementSetDao;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.domain.ExamPlan;
import com.talkweb.exammanagement.service.ExamManagementExamStudsService;
import com.talkweb.schedule.entity.TSchTClassInfoExternal;
import com.talkweb.schedule.entity.TSchTClassInfoExternal.TSchSubjectInfo;
import com.talkweb.schedule.service.ScheduleExternalService;

@Service
public class ExamManagementExamStudsServiceImpl implements
		ExamManagementExamStudsService {
	Logger logger = LoggerFactory
			.getLogger(ExamManagementExamStudsServiceImpl.class);

	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private ExamManagementDao examManagementDao;

	@Autowired
	private ExamManagementSetDao examManagementSetDao;

	@Autowired
	private ScheduleExternalService scheduleExternalService;

	@Override
	public JSONObject getNonparticipationExamList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		Set<String> classIdSet = new HashSet<String>();
		String cid = param.get("tClassId") != null ? param.get("tClassId")
				.toString() : "";
		JSONObject result = new JSONObject();
		School sch = (School) param.get("school");
		param.put("tClassIds", cid.isEmpty()?"":Arrays.asList(cid.split(",")));
		if (!cid.isEmpty()) {
			classIdSet.addAll(Arrays.asList(cid.split(",")));
		}
		
		List<JSONObject> instulist = new ArrayList<JSONObject>();
		List<JSONObject> nostulist = new ArrayList<JSONObject>();
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);

		List<ExamPlan> eplist = examManagementDao.getExamPlanList(param, param
				.get("termInfo").toString(), autoIncr);
		List<String> gralist = new ArrayList<String>();
		for (ExamPlan ep : eplist) {
			gralist.add(ep.getUsedGrade());
		}
		List<Account> al = new ArrayList<Account>();
		// al=commonDataService.getStudentList(Long.valueOf(param.get("schoolId").toString()),
		// param.get("termInfo").toString(), param.get("studName").toString());
		List<String> noclass=new ArrayList<String>();
		List<String> inclass=new ArrayList<String>();
		for (ExamPlan ep : eplist) {

			param.put("examPlanId", ep.getExamPlanId());
			List<JSONObject> subjectAndLe = examManagementSetDao
					.getExplantSubject(param);// 查询该次考试计划设置的科目级层次
			Map<String, Account> usermap = new HashMap<String, Account>();

			if (!ep.getScheduleId().isEmpty()) { // 从新高考取数据
				List<Long> aidlist = new ArrayList<Long>();
				List<TSchTClassInfoExternal> stulist = scheduleExternalService
						.getTClassInfoExternal(ep.getScheduleId(), Long
								.valueOf(param.get("schoolId").toString()),
								param.get("termInfo").toString(), ep
										.getUsedGrade(),
								classIdSet.isEmpty() ? null : classIdSet);

				if (!param.get("studName").toString().isEmpty()) {
					al = commonDataService.getStudentList(
							Long.valueOf(param.get("schoolId").toString()),
							param.get("termInfo").toString(),
							param.get("studName").toString());
					for (Account ac : al) {
						aidlist.add(ac.getId());
					}
				} else {
					for (TSchTClassInfoExternal tc : stulist) {
						List<Long> tcsidlist = tc.getStudentIdList();
						aidlist.addAll(tcsidlist);
					}
					al = commonDataService.getAccountBatch(
							Long.valueOf(param.get("schoolId").toString()),
							aidlist, param.get("termInfo").toString());
				}
				if(!param.get("studName").toString().isEmpty()&&classIdSet.size()<=1){
					param.put("accountId", aidlist.isEmpty() ? "" : aidlist);
				}

				if (al.isEmpty()) {
					throw new CommonRunException(0, "没有任何相关的学生信息");
				}
				List<JSONObject> nolist = examManagementSetDao
						.getNonparticipationExamList(param);// 未拍考的

				for (Account a : al) {
					usermap.put(a.getId() + "", a);
				}

				Map<String, Map<String, String>> nomap = new HashMap<String, Map<String, String>>();
				for (JSONObject j : nolist) {
					if (nomap.containsKey(j.getString("accountId"))) {
						Map<String, String> smap = nomap.get(j
								.getString("accountId"));
						smap.put(j.getString("subjectId"),
								j.getString("subjectId"));
					} else {
						Map<String, String> smap = new HashMap<String, String>();
						smap.put(j.getString("subjectId"),
								j.getString("subjectId"));
						nomap.put(j.getString("accountId"), smap);
					}
				}

				List<TSchTClassInfoExternal> tClassInfoList = scheduleExternalService
						.getTClassInfoExternalNoAccount(ep.getScheduleId(),
								Long.valueOf(param.get("schoolId").toString()),
								param.get("termInfo").toString(),
								ep.getUsedGrade(), classIdSet);
				Map<String, TSchTClassInfoExternal> tclassMap = new HashMap<String, TSchTClassInfoExternal>();
				if (CollectionUtils.isNotEmpty(tClassInfoList)) {
					for (TSchTClassInfoExternal tce : tClassInfoList) {
						tclassMap.put(tce.getTclassId(), tce);
					}
				}
				for (JSONObject json : nolist) {
					if (usermap.containsKey(json.getString("accountId"))) {
						String tcname = tclassMap.containsKey(json
								.getString("tClassId")) ? tclassMap.get(
								json.getString("tClassId")).getTclassName()
								: "";
						json.put("studName",
								usermap.get(json.getString("accountId"))
										.getName());
						json.put("tClassName", tcname);
						json.put("examPlanId", ep.getExamPlanId());
						noclass.add(tcname);
					}
				}
				nostulist.addAll(nolist);

				for (TSchTClassInfoExternal tc : stulist) {
					Map<String, JSONObject> sujmap = new HashMap<String, JSONObject>();// 存放考试需要的科目和层次
					String tid = tc.getTclassId();
					String tclaname = tclassMap.containsKey(tid) ? tclassMap
							.get(tid).getTclassName() : "";
					List<Long> tcsidlist = tc.getStudentIdList();
					List<TSchSubjectInfo> tslist = tc.getSubjectList();
					for (TSchSubjectInfo ts : tslist) {
						for (JSONObject sl : subjectAndLe) {
							if (sl.getString("subjectId").equals(
									String.valueOf(ts.getSubjectId()))) {
								if (sl.getString("subjectLevel").equals("0")) {
									sujmap.put(
											String.valueOf(ts.getSubjectId()),
											sl);
								} else if (sl.getString("subjectLevel").equals(
										String.valueOf(ts.getSubjectLevel()))) {
									sujmap.put(
											String.valueOf(ts.getSubjectId()),
											sl);
								}
							}
						}
					}
					// if(param.get("name")!=null&&!param.get("name").toString().isEmpty()){
					// Map<String,List<JSONObject>> nomap=new HashMap<String,
					// List<JSONObject>>();
					// for(JSONObject no:nolist){
					// if(nomap.containsKey(no.getString(""))){
					//
					// }
					// }
					for (Long tsid : tcsidlist) {// 学生列表
						if (usermap.containsKey(String.valueOf(tsid))) {// 和基础数据比较
							for (Map.Entry<String, JSONObject> entry : sujmap
									.entrySet()) {
								String sujid = entry.getKey();
								JSONObject sujle = entry.getValue();
								String examSubjName = sujle
										.getString("examSubjName");
//								if (sujle.getInteger("subjectLevel") != null
//										& !sujle.getInteger("subjectLevel")
//												.equals("0")) {
//									examSubjName += EnumSubjectLevel
//											.findNameByValueWithBrackets(sujle
//													.getInteger("subjectLevel"));
//								}
								if (!nolist.isEmpty()) {
									if (nomap.containsKey(String.valueOf(tsid))) {// 和不参考学生比较
										Map<String, String> sumap = nomap
												.get(String.valueOf(tsid));
										if (!sumap.containsKey(sujid)) {
											JSONObject json = new JSONObject();
											json.put("accountId", tsid);
											json.put(
													"studName",
													usermap.get(
															String.valueOf(tsid))
															.getName());
											json.put("examSubjectId", sujle
													.getString("examSubjectId"));
											json.put("examSubjectName",
													examSubjName);
											json.put("tClassId", tid);
											json.put("tClassName", tclaname);
											json.put("examPlanId",
													ep.getExamPlanId());
											instulist.add(json);
											inclass.add(tclaname);
										}
									} else {
										JSONObject json = new JSONObject();
										json.put("accountId", tsid);
										json.put(
												"studName",
												usermap.get(
														String.valueOf(tsid))
														.getName());
										json.put("examSubjectId", sujle
												.getString("examSubjectId"));
										json.put("examSubjectName",
												examSubjName);
										json.put("tClassId", tid);
										json.put("tClassName", tclaname);
										json.put("examPlanId",
												ep.getExamPlanId());
										instulist.add(json);
										inclass.add(tclaname);
									}
								} else {
									JSONObject json = new JSONObject();
									json.put("accountId", tsid);
									json.put("studName",
											usermap.get(String.valueOf(tsid))
													.getName());
									json.put("examSubjectId",
											sujle.getString("examSubjectId"));
									json.put("examSubjectName", examSubjName);
									json.put("tClassId", tid);
									json.put("tClassName", tclaname);
									json.put("examPlanId", ep.getExamPlanId());
									instulist.add(json);
									inclass.add(tclaname);
								}
							}
						}
					}
					// }

				}
			} else if (ep.getScheduleId().isEmpty()) {
				// 没有选择课表 从基础数据取数据
				List<String> aidlist = new ArrayList<String>();
				if (!param.get("studName").toString().isEmpty()) {
					al = commonDataService.getStudentList(
							Long.valueOf(param.get("schoolId").toString()),
							param.get("termInfo").toString(),
							param.get("studName").toString());
				} else {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("schoolId", param.get("schoolId"));
					map.put("termInfoId", param.get("termInfo"));
					map.put("usedGradeId", StringUtils.join(gralist, ","));
					if (!cid.isEmpty()) {
						map.put("classId", cid);
					}
					al = commonDataService.getStudentList(map);
				}
				if (al.isEmpty()) {
					throw new CommonRunException(0, "没有任何相关的学生信息");
				}
				for (Account a : al) {
					usermap.put(a.getId() + "", a);
					aidlist.add(a.getId() + "");
				}
				if(!param.get("studName").toString().isEmpty()&&classIdSet.size()<=1){
					param.put("accountId", aidlist.isEmpty() ? "" : aidlist);
				}
				//param.put("accountId", aidlist);

				List<JSONObject> nolist = examManagementSetDao
						.getNonparticipationExamList(param);// 未拍考的
				Map<String, Map<String, String>> nomap = new HashMap<String, Map<String, String>>();
				for (JSONObject j : nolist) {
					if (nomap.containsKey(j.getString("accountId"))) {
						Map<String, String> smap = nomap.get(j
								.getString("accountId"));
						smap.put(j.getString("subjectId"),
								j.getString("subjectId"));
					} else {
						Map<String, String> smap = new HashMap<String, String>();
						smap.put(j.getString("subjectId"),
								j.getString("subjectId"));
						nomap.put(j.getString("accountId"), smap);
					}
				}

				Map<String, JSONObject> sujmap = new HashMap<String, JSONObject>();// 存放考试需要的科目和层次

				HashMap<String, Classroom> classMap = new HashMap<String, Classroom>();
				List<Classroom> classes = commonDataService.getSimpleClassList(
						sch, null, param.get("termInfo").toString());
				for (Classroom clr : classes) {
					if (clr.getId() != 0 && clr.getClassName() != null) {
						classMap.put(clr.getId() + "", clr);
					}
				}
				for (JSONObject sl : subjectAndLe) {
					sujmap.put(sl.getString("subjectId"), sl);
				}
				String claid = "";
				String clasname = "";

				for (JSONObject json : nolist) {// 不参考的
					if (usermap.containsKey(json.getString("accountId"))) {
						String tcname = classMap.containsKey(json
								.getString("tClassId")) ? classMap.get(
								json.getString("tClassId")).getClassName() : "";
						json.put("studName",
								usermap.get(json.getString("accountId"))
										.getName());
						json.put("tClassName", tcname);
						json.put("examPlanId", ep.getExamPlanId());
						noclass.add(tcname);
					}
				}
				nostulist.addAll(nolist);
				for (Account a : al) {
					String tsid = a.getId() + "";
					List<User> users = a.getUsers();
					for (User u : users) {
						if (u.getStudentPart() != null) {
							claid = u.getStudentPart().getClassId() + "";
							clasname = classMap.get(claid) != null ? classMap
									.get(claid).getClassName() : "";

						}
					}
					for (Map.Entry<String, JSONObject> entry : sujmap
							.entrySet()) {
						String sujid = entry.getKey();
						JSONObject sujle = entry.getValue();
						String examSubjName = sujle.getString("examSubjName");
//						if (sujle.getInteger("subjectLevel") != null
//								& !sujle.getInteger("subjectLevel").equals("0")) {
//							examSubjName += EnumSubjectLevel
//									.findNameByValueWithBrackets(sujle
//											.getInteger("subjectLevel"));
//						}
						if (!nolist.isEmpty()) {
							if (nomap.containsKey(String.valueOf(tsid))) {// 和不参考学生比较
								Map<String, String> sumap = nomap.get(String
										.valueOf(tsid));
								if (!sumap.containsKey(sujid)) {
									JSONObject json = new JSONObject();
									json.put("accountId", tsid);
									json.put("studName",
											usermap.get(String.valueOf(tsid))
													.getName());
									json.put("examSubjectId",
											sujle.getString("examSubjectId"));
									json.put("examSubjectName", examSubjName);
									json.put("tClassId", claid);
									json.put("tClassName", clasname);
									json.put("examPlanId", ep.getExamPlanId());
									instulist.add(json);
									inclass.add(clasname);
								}
							} else {
								JSONObject json = new JSONObject();
								json.put("accountId", tsid);
								json.put("studName",
										usermap.get(String.valueOf(tsid))
												.getName());
								json.put("examSubjectId",
										sujle.getString("examSubjectId"));
								json.put("examSubjectName", examSubjName);
								json.put("tClassId", claid);
								json.put("tClassName", clasname);
								json.put("examPlanId", ep.getExamPlanId());
								instulist.add(json);
								inclass.add(clasname);
							}
						} else {
							JSONObject json = new JSONObject();
							json.put("accountId", tsid);
							json.put("studName",
									usermap.get(String.valueOf(tsid)).getName());
							json.put("examSubjectId",
									sujle.getString("examSubjectId"));
							json.put("examSubjectName", examSubjName);
							json.put("tClassId", claid);
							json.put("tClassName", clasname);
							json.put("examPlanId", ep.getExamPlanId());
							instulist.add(json);
							inclass.add(clasname);
						}
					}
				}
			}
		}
		
		nostulist = SortUtil.sortJsonListByTclassName(nostulist, 0, noclass, "tClassName");
		instulist = SortUtil.sortJsonListByTclassName(instulist, 0, inclass, "tClassName");
		result.put("studs", instulist);
		result.put("studsNotTakingExams", nostulist);
		return result;
	}

	@Override
	public void saveNonparticipationExamList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		examManagementSetDao.saveNonparticipationExamList(param);
		if(param.containsKey("list")&&param.get("list")!=null){
			List<Map<String, Object>> d=(List<Map<String, Object>>) param.get("list");
			for(Map<String, Object> da:d){
				List<String> tClassIds=new ArrayList<String>();
				List<String> accountIds=new ArrayList<String>();
				accountIds.add(da.get("accountId").toString());
				tClassIds.add(da.get("tClassId").toString());
				da.put("autoIncr", autoIncr);
				da.put("tClassIds", tClassIds);
				da.put("accountId", accountIds);
				List<JSONObject> subgrouplist=examManagementSetDao.getSubjectGbyResult(da);//学生所在班的科目组合
				Map<String,List<String>> subm=new HashMap<String, List<String>>();
				for(JSONObject sub:subgrouplist){
					if(subm.containsKey(sub.getString("examSubjectGroupId"))){
						List<String> sublist=subm.get(sub.getString("examSubjectGroupId"));
						sublist.add(sub.getString("examSubjectId"));
					}else{
						List<String> sublist=new ArrayList<String>();
						sublist.add(sub.getString("examSubjectId"));
						subm.put(sub.getString("examSubjectGroupId"), sublist);
					}
				}
				da.put("examSubjectId", "");//因为sql查询条件不需要这个
				List<JSONObject> nolist=examManagementSetDao.getNonparticipationExamList(da);//改学生不参考的科目
				List<String> nosublist=new ArrayList<String>();
				for(JSONObject nosub:nolist){
					nosublist.add(nosub.getString("examSubjectId"));
				}
				Iterator<Entry<String, List<String>>> it1 = subm.entrySet().iterator();
				Collections.sort(nosublist);
				while (it1.hasNext()) {
					Entry<String, List<String>> entry = it1.next();
					List<String> sublist=entry.getValue();
					String subgrouid=entry.getKey();
					Collections.sort(sublist);
					nosublist.retainAll(sublist);
					String userj=StringUtils.join(nosublist, ",");
					String targetj=StringUtils.join(sublist, ",");
					if(userj.contains(targetj)){//只有不参考科目 和 组合科目里面的单科全部匹配上才删除
						da.put("examSubjectGroupId",subgrouid);
						examManagementSetDao.deleteStudsWaiting(da);//删除等待排考信息
						examManagementSetDao.deleteExamResult(da);//删除相应的排考信息
					}
				}
//				if(subgrouplist!=null&&subgrouplist.size()>0){
//					for(JSONObject json:subgrouplist){
//						param.put("examSubjectGroupId", json.getString("examSubjectGroupId"));
//						examManagementSetDao.deleteStudsWaiting(da);//删除等待排考信息
//					}
//				}
				
			}
		}
		
	}

	@Override
	public void serializabdeleteNonparticipation(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		String accountId=param.get("accountId").toString();
		String examSubjectId=param.get("examSubjectId").toString();
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		examManagementSetDao.deleteNonparticipationExamList(param);
		List<ExamPlan> eplist = examManagementDao.getExamPlanList(param, param
				.get("termInfo").toString(), autoIncr);
		String userGrade=eplist.get(0).getUsedGrade();
		List<JSONObject> dlist=examManagementSetDao.getMaxtestNumber(param);
		
		List<String> tClassIds=new ArrayList<String>();
		List<String> accountIds=new ArrayList<String>();
		accountIds.add(accountId);
		tClassIds.add(param.get("tClassId").toString());
		param.put("tClassIds", tClassIds);
		param.put("accountId", accountIds);
		List<JSONObject> subgrouplist=examManagementSetDao.getSubjectGbyResult(param);
		param.put("examSubjectId", "");
		List<JSONObject> nolist=examManagementSetDao.getNonparticipationExamList(param);//改学生不参考的科目
		param.put("examSubjectId", examSubjectId);
		List<String> nosublist=new ArrayList<String>();//存放该生不参考科目
		for(JSONObject nosub:nolist){
			nosublist.add(nosub.getString("examSubjectId"));
		}
		Map<String,List<String>> subm=new HashMap<String, List<String>>();
		for(JSONObject sub:subgrouplist){
			if(subm.containsKey(sub.getString("examSubjectGroupId"))){
				List<String> sublist=subm.get(sub.getString("examSubjectGroupId"));
				sublist.add(sub.getString("examSubjectId"));
			}else{
				List<String> sublist=new ArrayList<String>();
				sublist.add(sub.getString("examSubjectId"));
				subm.put(sub.getString("examSubjectGroupId"), sublist);
			}
		}
		
		List<Map<String, Object>> f=new ArrayList<Map<String,Object>>();
		if(dlist!=null&&dlist.size()>0&&subgrouplist!=null&&subgrouplist.size()>0){
			String testNumber=dlist.get(0).getString("testNumber");
			String nubst=testNumber.substring(0, StringUtil.getNumberIndex(testNumber));
			String numbers=testNumber.substring(StringUtil.getNumberIndex(testNumber), testNumber.length());
			int maxnum=Integer.valueOf(numbers)+1;
			param.put("usedGrade", userGrade);
			param.put("testNumber", nubst+maxnum);
			Iterator<Entry<String, List<String>>> it1 = subm.entrySet().iterator();
			Collections.sort(nosublist);
			while (it1.hasNext()) {
				Entry<String, List<String>> entry = it1.next();
				List<String> sublist=entry.getValue();
				String subgrouid=entry.getKey();
				Collections.sort(sublist);
				param.put("examSubjectGroupId", subgrouid);
				List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
				Map<String,Object> p=new HashMap<String, Object>();
				p.put("autoIncr", autoIncr);
				p.put("termInfo", param.get("termInfo"));
				if(CollectionUtils.isEmpty(nolist)){//该学生没有任何不参考的 插入等待排考
					param.put("accountId", accountId);
					list.add(param);
					p.put("list", list);
					examManagementSetDao.saveStudsWaiting(p);//插入等待排考信息
				}else{
					nosublist.retainAll(sublist);
					String userj=StringUtils.join(nosublist, ",");
					String targetj=StringUtils.join(sublist, ",");
					if(userj.contains(targetj)){//只有不参考科目 和 组合科目里面的单科全部匹
						param.put("accountId", accountId);
						list.add(param);
						p.put("list", list);
						examManagementSetDao.saveStudsWaiting(param);//插入等待排考信息
					}
				}
			}
			param.put("accountId", accountId);
			f.add(param);
			// 批量插入结果
			examManagementDao.insertTestNumberInfoBatch(f,
					param.get("termInfo").toString(), autoIncr);//插入考号表
		}
		
	}

	@Override
	public JSONObject getUserExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		JSONObject re = new JSONObject();
		String name = param.containsKey("name")
				&& !param.get("name").toString().isEmpty() ? param.get("name")
				.toString() : "";
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);

		List<ExamPlan> eplist = examManagementDao.getExamPlanList(param, param
				.get("termInfo").toString(), autoIncr);
		List<String> gralist = new ArrayList<String>();
		for (ExamPlan ep : eplist) {
			gralist.add(ep.getUsedGrade());
		}

		String substring = "";

		HashMap<String, Account> stumap = new HashMap<String, Account>();

		if (!name.isEmpty()) {

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", param.get("schoolId"));
			map.put("termInfoId", param.get("termInfo"));
			map.put("usedGradeId", StringUtils.join(gralist, ","));
			Long t1 = new Date().getTime();
			List<Account> allStu = commonDataService.getStudentList(map);
			Long t2 = new Date().getTime() - t1;
			System.out.println("time---" + t2);

			for (Account a : allStu) {
				stumap.put(a.getId() + "", a);
			}

			Iterator<Entry<String, Account>> it1 = stumap.entrySet().iterator();

			List<String> accountIds = new ArrayList<String>();
			List<String> accountId = new ArrayList<String>();
			while (it1.hasNext()) {

				Entry<String, Account> entry = it1.next();

				Account ac = entry.getValue();

				if (ac.getName().contains(name)) {
					String sub = "a.accountId='" + ac.getId() + "'";
					accountIds.add(sub);
					accountId.add(ac.getId() + "");
				}
			}
			if (accountIds.isEmpty()) {
				throw new CommonRunException(0, "没有查询到相应的学生！");
			}
			substring = StringUtils.join(accountIds, " or ");

			param.put("substring", substring);
			param.put("accountId", accountId.isEmpty() ? null : accountId);
		}
		List<JSONObject> dlist = examManagementSetDao.getUserExamPlace(param);
		Map<String, JSONObject> Dmap = new LinkedHashMap<String, JSONObject>();
		for (JSONObject d : dlist) {
			if (Dmap.containsKey(d.getString("examPlaceId"))) {
				if (d.getString("highLight").equals("1")) {
					Dmap.put(d.getString("examPlaceId"), d);
				}
			} else {
				Dmap.put(d.getString("examPlaceId"), d);
			}
		}
		List<JSONObject> datalist = new ArrayList<JSONObject>();
		for (Map.Entry<String, JSONObject> entry : Dmap.entrySet()) {
			datalist.add(entry.getValue());
		}
		List<JSONObject> stulist = examManagementSetDao.studsWaiting(param);
		if (name.isEmpty()) {
			List<Long> accountIds = new ArrayList<Long>();
			for (JSONObject json : stulist) {
				accountIds.add(json.getLongValue("accountId"));
			}
			List<Account> allStu = commonDataService.getAccountBatch(
					Long.valueOf(param.get("schoolId").toString()), accountIds,
					param.get("termInfo").toString());

			for (Account a : allStu) {
				stumap.put(a.getId() + "", a);
			}
		}
		String scheduleId = eplist.get(0).getScheduleId();
		String usedGrade = eplist.get(0).getUsedGrade();
		List<Long> ids = new ArrayList<Long>();
		if (!stulist.isEmpty()) {
			if (scheduleId.isEmpty()) {

				for (JSONObject es : stulist) {
					if (stumap.containsKey(es.getString("accountId"))) {
						es.put("studName", stumap
								.get(es.getString("accountId")).getName());
					}
					ids.add(Long.valueOf(es.getString("tClassId")));
				}
				List<Classroom> classrooms = commonDataService
						.getClassroomBatch(
								Long.valueOf(param.get("schoolId").toString()),
								ids, param.get("termInfo").toString());
				Map<String, Classroom> classmap = new HashMap<String, Classroom>();
				for (Classroom c : classrooms) {
					classmap.put(c.getId() + "", c);
				}
				for (JSONObject se : stulist) {
					if (classmap.containsKey(se.getString("tClassId"))) {
						se.put("tClassName",
								classmap.get(se.getString("tClassId"))
										.getClassName());
					}
				}
			} else {
				List<TSchTClassInfoExternal> tClassInfoList = scheduleExternalService
						.getTClassInfoExternalNoAccount(scheduleId,
								Long.valueOf(param.get("schoolId").toString()),
								param.get("termInfo").toString(), usedGrade,
								null);
				Map<String, TSchTClassInfoExternal> classmap = new HashMap<String, TSchTClassInfoExternal>();
				if (CollectionUtils.isNotEmpty(tClassInfoList)) {
					for (TSchTClassInfoExternal classInfo : tClassInfoList) {
						classmap.put(classInfo.getTclassId(), classInfo);
					}
				}
				for (JSONObject se : stulist) {
					if (stumap.containsKey(se.getString("accountId"))) {
						se.put("studName", stumap
								.get(se.getString("accountId")).getName());
					}
					if (classmap.containsKey(se.getString("tClassId"))) {
						se.put("tClassName",
								classmap.get(se.getString("tClassId"))
										.getTclassName());
					}
				}
			}
		}
		re.put("placelist", datalist);
		re.put("studswait", stulist);
		return re;
	}

	@Override
	public List<JSONObject> studsWaiting(Map<String, Object> param) {
		// TODO Auto-generated method stub
		String name = param.containsKey("name") && param.get("name") != null
				&& !param.get("name").toString().isEmpty() ? param.get("name")
				.toString() : "";
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		List<ExamPlan> eplist = examManagementDao.getExamPlanList(param, param
				.get("termInfo").toString(), autoIncr);
		List<String> gralist = new ArrayList<String>();
		for (ExamPlan ep : eplist) {
			gralist.add(ep.getUsedGrade());
		}

		HashMap<String, Account> stumap = new HashMap<String, Account>();

		if (!name.isEmpty()) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", param.get("schoolId"));
			map.put("termInfoId", param.get("termInfo"));
			map.put("usedGradeId", StringUtils.join(gralist, ","));
			List<Account> allStu = commonDataService.getStudentList(map);

			for (Account a : allStu) {
				stumap.put(a.getId() + "", a);
			}
			Iterator<Entry<String, Account>> it1 = stumap.entrySet().iterator();
			List<String> accountId = new ArrayList<String>();
			while (it1.hasNext()) {

				Entry<String, Account> entry = it1.next();

				Account ac = entry.getValue();

				if (ac.getName().contains(name)) {
					accountId.add(ac.getId() + "");
				}
			}

			param.put("accountId", accountId.isEmpty() ? null : accountId);
		}
		List<JSONObject> stulist = examManagementSetDao.studsWaiting(param);

		if (name.isEmpty()) {
			List<Long> accountIds = new ArrayList<Long>();
			for (JSONObject json : stulist) {
				accountIds.add(json.getLongValue("accountId"));
			}
			List<Account> allStu = commonDataService.getAccountBatch(
					Long.valueOf(param.get("schoolId").toString()), accountIds,
					param.get("termInfo").toString());

			for (Account a : allStu) {
				stumap.put(a.getId() + "", a);
			}
		}
		String scheduleId = eplist.get(0).getScheduleId();
		String usedGrade = eplist.get(0).getUsedGrade();
		List<Long> ids = new ArrayList<Long>();

		if (scheduleId.isEmpty()) {
			for (JSONObject es : stulist) {
				if (stumap.containsKey(es.getString("accountId"))) {
					es.put("studName", stumap.get(es.getString("accountId"))
							.getName());
				}
				ids.add(Long.valueOf(es.getString("tClassId")));
			}
			List<Classroom> classrooms = commonDataService.getClassroomBatch(
					Long.valueOf(param.get("schoolId").toString()), ids, param
							.get("termInfo").toString());
			Map<String, Classroom> classmap = new HashMap<String, Classroom>();
			for (Classroom c : classrooms) {
				classmap.put(c.getId() + "", c);
			}
			for (JSONObject se : stulist) {
				if (classmap.containsKey(se.getString("tClassId"))) {
					se.put("tClassName", classmap.get(se.getString("tClassId"))
							.getClassName());
				}
			}
		} else {
			List<TSchTClassInfoExternal> tClassInfoList = scheduleExternalService
					.getTClassInfoExternalNoAccount(scheduleId,
							Long.valueOf(param.get("schoolId").toString()),
							param.get("termInfo").toString(), usedGrade, null);
			Map<String, TSchTClassInfoExternal> classmap = new HashMap<String, TSchTClassInfoExternal>();
			if (CollectionUtils.isNotEmpty(tClassInfoList)) {
				for (TSchTClassInfoExternal classInfo : tClassInfoList) {
					classmap.put(classInfo.getTclassId(), classInfo);
				}
			}
			for (JSONObject se : stulist) {
				if (stumap.containsKey(se.getString("accountId"))) {
					se.put("studName", stumap.get(se.getString("accountId"))
							.getName());
				}
				if (classmap.containsKey(se.getString("tClassId"))) {
					se.put("tClassName", classmap.get(se.getString("tClassId"))
							.getTclassName());
				}
			}
		}
		return stulist;
	}

	@Override
	public List<JSONObject> getStudsInExamPlaceByAccountId(
			Map<String, Object> param) {
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		return examManagementSetDao.getStudsInExamPlace(param);
	}

	@Override
	public JSONObject getStudsInExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		//int isKeepContinuous = 1;// 连续的
		List<JSONObject> datalist = new ArrayList<JSONObject>();
		JSONObject re = new JSONObject();
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		List<ExamPlan> eplist = examManagementDao.getExamPlanList(param, param
				.get("termInfo").toString(), autoIncr);
		List<String> gralist = new ArrayList<String>();
		for (ExamPlan ep : eplist) {
			gralist.add(ep.getUsedGrade());
		}
		// HashMap<String, Object> map = new HashMap<String, Object>();
		// map.put("schoolId", param.get("schoolId"));
		// map.put("termInfoId", param.get("termInfo"));
		// map.put("usedGradeId", StringUtils.join(gralist, ","));
		// List<Account> allStu = commonDataService.getStudentList(map);
		Map<String, Account> usermap = new HashMap<String, Account>();
		List<JSONObject> list = examManagementSetDao.getStudsInExamPlace(param);
		List<Long> accountIds = new ArrayList<Long>();
		for (JSONObject json : list) {
			accountIds.add(Long.valueOf(json.getString("accountId")));
		}
		List<Account> allStu = commonDataService.getAccountBatch(
				Long.valueOf(param.get("schoolId").toString()), accountIds,
				param.get("termInfo").toString());

		for (Account a : allStu) {
			usermap.put(a.getId() + "", a);
		}

		String scheduleId = eplist.get(0).getScheduleId();
		String usedGrade = eplist.get(0).getUsedGrade();
		List<Long> ids = new ArrayList<Long>();

		if (!list.isEmpty()) {
			if (scheduleId.isEmpty()) {
				for (JSONObject es : list) {
					if (usermap.containsKey(es.getString("accountId"))) {
						es.put("studName",
								usermap.get(es.getString("accountId"))
										.getName());
					}
					ids.add(Long.valueOf(es.getString("tClassId")));
				}
				List<Classroom> classrooms = commonDataService
						.getClassroomBatch(
								Long.valueOf(param.get("schoolId").toString()),
								ids, param.get("termInfo").toString());
				Map<String, Classroom> classmap = new HashMap<String, Classroom>();
				for (Classroom c : classrooms) {
					classmap.put(c.getId() + "", c);
				}
				for (JSONObject se : list) {
					if (classmap.containsKey(se.getString("tClassId"))) {
						se.put("tClassName",
								classmap.get(se.getString("tClassId"))
										.getClassName());
					}
				}
			} else {
				List<TSchTClassInfoExternal> tClassInfoList = scheduleExternalService
						.getTClassInfoExternalNoAccount(scheduleId,
								Long.valueOf(param.get("schoolId").toString()),
								param.get("termInfo").toString(), usedGrade,
								null);
				Map<String, TSchTClassInfoExternal> classmap = new HashMap<String, TSchTClassInfoExternal>();
				if (CollectionUtils.isNotEmpty(tClassInfoList)) {
					for (TSchTClassInfoExternal classInfo : tClassInfoList) {
						classmap.put(classInfo.getTclassId(), classInfo);
					}
				}
				for (JSONObject se : list) {
					if (usermap.containsKey(se.getString("accountId"))) {
						se.put("studName",
								usermap.get(se.getString("accountId"))
										.getName());
					}
					if (classmap.containsKey(se.getString("tClassId"))) {
						se.put("tClassName",
								classmap.get(se.getString("tClassId"))
										.getTclassName());
					}
				}
			}
			Map<String, JSONObject> damap = new HashMap<String, JSONObject>();
			// 如果坐号中间出现空的情况 填补进去
			for (JSONObject da : list) {
				damap.put(da.getString("seatNumber"), da);
			}
			Map<String, JSONObject> sortmap = new LinkedHashMap<String, JSONObject>();
			int maxSeatN = list.get(list.size() - 1).getIntValue("seatNumber");
			for (int i = 0; i < maxSeatN; i++) {
				String num = String.valueOf(i + 1);
				if (!damap.containsKey(num)) {
					JSONObject d = new JSONObject();
					d.put("accountId", "");
					sortmap.put(num, d);
					//isKeepContinuous = 0;
				} else {
					sortmap.put(num, damap.get(num));
				}

			}

			Iterator<Entry<String, JSONObject>> it1 = sortmap.entrySet()
					.iterator();
			while (it1.hasNext()) {
				datalist.add(it1.next().getValue());
			}
		}

		int counts = list != null && !list.isEmpty() ? list.size() : 0;
		re.put("counts", counts);
		//re.put("isKeepContinuous", isKeepContinuous);
		re.put("data", datalist);
		return re;
	}

	@Override
	public void deleteStudToExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		examManagementSetDao.deleteStudsWaiting(param);
		String acccount = param.get("acc").toString();
		param.put("accountId", "");
		List<JSONObject> dlist = examManagementSetDao
				.getStudsInExamPlace(param);
		List<Map<String, Object>> inlist = new ArrayList<Map<String, Object>>();
		Map<String, Object> ob = new HashMap<String, Object>();
		if (dlist != null && dlist.size() > 0) {
			int maxSeatN = dlist.get(dlist.size() - 1)
					.getIntValue("seatNumber");
			List<Integer> num = new ArrayList<Integer>();
			for (int i = 0; i < dlist.size(); i++) {
				int seatNumber = dlist.get(i).getIntValue("seatNumber");
				if (seatNumber != i + 1) {
					num.add(i + 1);
				}
			}

			if (num.isEmpty()) {
				param.put("seatNumber", maxSeatN + 1);
			} else {
				param.put("seatNumber", num.get(0));
			}
		} else {
			param.put("seatNumber", 1);
		}
		param.put("accountId", acccount);
		inlist.add(param);
		ob.put("autoIncr", autoIncr);
		ob.put("termInfo", param.get("termInfo"));
		ob.put("list", inlist);
		examManagementSetDao.saveArrangeExamResult(ob);
		if (param.containsKey("isKeepContinuous")
				&& param.get("isKeepContinuous").toString().equals("1")) {// 进行排序
			this.sortExamResult(param);
		}
	}

	@Override
	public void deleteStudToWait(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		examManagementSetDao.deleteArrangeExamResult(param);
		List<Map<String, Object>> inlist = new ArrayList<Map<String, Object>>();
		inlist.add(param);
		Map<String, Object> ob = new HashMap<String, Object>();
		ob.put("autoIncr", autoIncr);
		ob.put("termInfo", param.get("termInfo"));
		ob.put("list", inlist);
		examManagementSetDao.saveStudsWaiting(ob);
		if (param.containsKey("isKeepContinuous")
				&& param.get("isKeepContinuous").toString().equals("1")) {// 进行排序
			this.sortExamResult(param);
		}
	}

	@Override
	public void saveArrangeExamResult(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		if (param.containsKey("list")) {
			List<JSONObject> inse = (List<JSONObject>) param.get("list");
			for (JSONObject j : inse) {
				Map<String, Object> par = new HashMap<String, Object>();
				par.put("schoolId", j.getString("schoolId"));
				par.put("examManagementId", j.getString("examManagementId"));
				par.put("termInfo", j.getString("termInfo"));
				par.put("accountId", j.getString("accountId"));
				par.put("examSubjectGroupId", j.getString("examSubjectGroupId"));
				par.put("autoIncr", autoIncr);
				examManagementSetDao.deleteArrangeExamResult(par);
			}
		}
		examManagementSetDao.saveArrangeExamResult(param);
		if (param.containsKey("isKeepContinuous")
				&& param.get("isKeepContinuous").toString().equals("1")) {// 进行排序
			this.sortExamResult(param);
		}
	}

	// 连号排序
	@Override
	public void sortExamResult(Map<String, Object> param) {
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		param.put("accountId", "");
		List<JSONObject> list = examManagementSetDao.getStudsInExamPlace(param);// 考场所有学生
		if (!list.isEmpty()) {
			// 排序
			for (int i = 0; i < list.size(); i++) {
				JSONObject data = list.get(i);
				data.put("seatNumber", i + 1);
			}
			examManagementSetDao.deleteArrangeExamResult(param);
			param.put("list", list);
			examManagementSetDao.saveArrangeExamResult(param);
		}
	}
	
}
