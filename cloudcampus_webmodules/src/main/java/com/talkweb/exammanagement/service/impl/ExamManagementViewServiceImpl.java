package com.talkweb.exammanagement.service.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.dao.ExamManagementDao;
import com.talkweb.exammanagement.dao.ExamManagementViewDao;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.domain.ExamPlan;
import com.talkweb.exammanagement.service.ExamManagementViewService;
import com.talkweb.schedule.entity.TSchTClassInfoExternal;
import com.talkweb.schedule.service.ScheduleExternalService;

@Service
public class ExamManagementViewServiceImpl implements ExamManagementViewService {
	Logger logger = LoggerFactory
			.getLogger(ExamManagementViewServiceImpl.class);

	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private ExamManagementDao examManagementDao;

	@Autowired
	private ExamManagementViewDao examManagementViewDao;

	@Autowired
	private ScheduleExternalService scheduleExternalService;

	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;

	@Override
	public List<JSONObject> getExamPlaceInfo(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		List<JSONObject> placelist = examManagementViewDao
				.getExamPlaceInfo(param);
		
		Map<String,String> armap=this.getsubjectDis(param);//重名处理
			for (JSONObject o : placelist) {
				if (armap.containsKey(o.getString("examSubjectGroupId"))) {
					o.put("subjectNames",
							armap.get(o.getString("examSubjectGroupId")));
				}
			}
		return placelist;
	}
	
	/**
	 * 重名处理
	 * @param param
	 * @return
	 */
	
	public Map<String,String> getsubjectDis(Map<String,Object> param){
		
		List<JSONObject> subjectDist = examManagementViewDao.getExamSubjectDist(param);
		// 排序处理
		Collections.sort(subjectDist, new Comparator<JSONObject>() {
		@Override
		public int compare(JSONObject o1, JSONObject o2) {
			int result = 0;

			List<String> subjKeys1 = Arrays.asList(o1.getString(
					"subjectSort").split(","));
			List<String> subjKeys2 = Arrays.asList(o2.getString(
					"subjectSort").split(","));
			int len = Math.min(subjKeys1.size(), subjKeys2.size());
			for (int i = 0; i < len; i++) {
				String[] item1 = ((String) subjKeys1.get(i)).split("_");
				String[] item2 = ((String) subjKeys2.get(i)).split("_");
				result = Long.compare(Long.parseLong(item1[0]),
						Long.parseLong(item2[0]));
				if (result != 0) {
					return result;
				}
				result = Integer.compare(Integer.parseInt(item1[1]),
						Integer.parseInt(item2[1]));
				if (result != 0) {
					return result;
				}
				result = Long.compare(o1.getDate("createDateTime").getTime(),
						o2.getDate("createDateTime").getTime());
				if (result != 0) {
					return result;
				}
			}
			return Integer.compare(subjKeys1.size(), subjKeys2.size());
		}
	});


		Map<String, LinkedHashMap<String,String>> plamap = new LinkedHashMap<String,  LinkedHashMap<String,String>>();

		for (JSONObject o : subjectDist) {
			if (plamap.containsKey(o.getString("examSubjectNames"))) {
				LinkedHashMap<String,String> list = plamap.get(o.getString("examSubjectNames"));
				list.put(o.getString("examSubjectGroupId"),o.getString("examSubjectGroupId"));
			} else {
				LinkedHashMap<String,String> list=new LinkedHashMap<String,String>();
				list.put(o.getString("examSubjectGroupId"),o.getString("examSubjectGroupId"));
				plamap.put(o.getString("examSubjectNames"), list);
			}
		}
		// 重名处理
		Map<String, String> armap = new LinkedHashMap<String, String>();
		Iterator<Entry<String, LinkedHashMap<String,String>>> it1 = plamap.entrySet()
				.iterator();
		while (it1.hasNext()) {
			Entry<String, LinkedHashMap<String, String>> entry1 = it1.next();
			String gname = entry1.getKey();
			LinkedHashMap<String, String> list = entry1.getValue();
			Iterator<Entry<String, String>> it = list.entrySet()
					.iterator();
			if (list.size() > 1) {
				int i = 1;
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					String s=entry.getKey();
					armap.put(s, gname + i);
					i++;
				}
			} else {
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					String s=entry.getKey();
					armap.put(s, gname);
				}

			}
		}
		return armap;
 }
	@Override
	public JSONObject getTableCornerList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		JSONObject res = new JSONObject();
		int curPageCount = Integer
				.valueOf(param.get("curPageCount").toString());
		List<JSONObject> dalist = new ArrayList<JSONObject>();
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(param,
				param.get("termInfo").toString(), autoIncr);
		if (examPlanList.size() == 0) {
			throw new CommonRunException(0, "获取不到对应年级的考试计划，请前往排考中设置");
		}
		List<JSONObject> seatlist = examManagementViewDao
				.getTableCornerSeatList(param);
		//List<JSONObject> subjectDist = examManagementViewDao.getExamSubjectDist(param);
		
		Map<String, List<JSONObject>> seam = new LinkedHashMap<String, List<JSONObject>>();
		Map<String, JSONObject> seamap = new LinkedHashMap<String, JSONObject>();
		if (seatlist.isEmpty()) {
			throw new CommonRunException(0, "未查询到相关的考场信息，请重新选择条件");
		}

		for (JSONObject json : seatlist) {
			if (seam.containsKey(json.getString("examPlaceId"))) {
				List<JSONObject> sdlist = seam.get(json
						.getString("examPlaceId"));
				sdlist.add(json);
			} else {
				List<JSONObject> sdlist = new ArrayList<JSONObject>();
				sdlist.add(json);
				seam.put(json.getString("examPlaceId"), sdlist);
			}
		}

		Iterator<Entry<String, List<JSONObject>>> it2 = seam.entrySet()
				.iterator();
		while (it2.hasNext()) {
			Entry<String, List<JSONObject>> entry = it2.next();
			String placeid = entry.getKey();
			int min = 100;
			int max = 0;
			JSONObject seatda = new JSONObject();
			for (JSONObject json : entry.getValue()) {
				if (json.getIntValue("seatNumber") < min) {
					min = json.getIntValue("seatNumber");
				}
				if (json.getIntValue("seatNumber") > max) {
					max = json.getIntValue("seatNumber");
				}
			}
			seatda.put("min", min);
			seatda.put("max", max);
			seamap.put(placeid, seatda);
		}
		List<JSONObject> tlist = new ArrayList<JSONObject>();

		for (Entry<String, JSONObject> entry : seamap.entrySet()) {
			JSONObject seatda = entry.getValue();
			param.put("examPlaceId", entry.getKey());
			param.put("minSeat", seatda.getIntValue("min"));
			param.put("maxSeat", seatda.getIntValue("max"));
			tlist.addAll(examManagementViewDao.getTableCornerList(param));
		}

		ExamPlan ep = examPlanList.get(0);
		String scheduleId = ep.getScheduleId();
		String usedGrade = ep.getUsedGrade();
		List<Long> accids = new ArrayList<Long>();
		if (scheduleId.isEmpty()) {
			List<Long> ids = new ArrayList<Long>();
			for (JSONObject es : tlist) {
				accids.add(es.getLongValue("accountId"));
				ids.add(Long.valueOf(es.getString("tClassId")));
			}
			List<Classroom> classrooms = commonDataService.getClassroomBatch(
					Long.valueOf(param.get("schoolId").toString()), ids, param
							.get("termInfo").toString());
			Map<String, Classroom> classmap = new HashMap<String, Classroom>();
			for (Classroom c : classrooms) {
				classmap.put(c.getId() + "", c);
			}
			for (JSONObject se : tlist) {
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
			for (JSONObject se : tlist) {
				accids.add(se.getLongValue("accountId"));
				if (classmap.containsKey(se.getString("tClassId"))) {
					se.put("tClassName", classmap.get(se.getString("tClassId"))
							.getTclassName());
				}
			}
		}

		HashMap<String, Account> studentMap = new HashMap<String, Account>();
		List<Account> allStu = commonDataService.getAccountBatch(Long
				.valueOf(param.get("schoolId").toString()), accids,
				param.get("termInfo").toString());

		for (Account a : allStu) {
			if (a != null && a.getName() != null) {
				studentMap.put(String.valueOf(a.getId()), a);
			}
		}

		if (!param.containsKey("export")) {// 给导出用

			// int totalCount = examManagementViewDao
			// .getTableCornerListcount(param);
			int totalCount = examManagementViewDao
					.getTableCornerSeatListCount(param);
			int totalPage = (totalCount + curPageCount - 1) / curPageCount;

			res.put("totalCount", totalCount);
			res.put("totalPage", totalPage);
		}

		Map<String, LinkedHashMap<String, List<JSONObject>>> datamap = new LinkedHashMap<String, LinkedHashMap<String, List<JSONObject>>>();

		for (JSONObject data : tlist) {
			if (datamap.containsKey(data.getString("examPlaceId"))) {
				LinkedHashMap<String, List<JSONObject>> dmap = datamap.get(data
						.getString("examPlaceId"));
				if (dmap.containsKey(data.getString("seatNumber"))) {
					List<JSONObject> dlist = dmap.get(data
							.getString("seatNumber"));
					dlist.add(data);
					dmap.put(data.getString("seatNumber"), dlist);
				} else {
					List<JSONObject> dlist = new ArrayList<JSONObject>();

					dlist.add(data);

					dmap.put(data.getString("seatNumber"), dlist);
				}
				datamap.put(data.getString("examPlaceId"), dmap);
			} else {

				LinkedHashMap<String, List<JSONObject>> dmap = new LinkedHashMap<String, List<JSONObject>>();

				List<JSONObject> dlist = new ArrayList<JSONObject>();

				dlist.add(data);

				dmap.put(data.getString("seatNumber"), dlist);

				datamap.put(data.getString("examPlaceId"), dmap);
			}
		}

		Iterator<Entry<String, LinkedHashMap<String, List<JSONObject>>>> it = datamap
				.entrySet().iterator();

		while (it.hasNext()) {

			String examPlaceName = "";

			String seatNumber = "";

			Entry<String, LinkedHashMap<String, List<JSONObject>>> entry = it
					.next();

			LinkedHashMap<String, List<JSONObject>> dmap = entry.getValue();

			Iterator<Entry<String, List<JSONObject>>> it1 = dmap.entrySet()
					.iterator();

			while (it1.hasNext()) {

				Entry<String, List<JSONObject>> entry1 = it1.next();

				String stNumber = entry1.getKey();

				seatNumber = stNumber;

				List<JSONObject> dlist = entry1.getValue();
				
				List<JSONObject> datalist=new ArrayList<JSONObject>();
				JSONObject da = new JSONObject();

				//先排序 为合并科目做准备
				HashMap<String,List<JSONObject>> usermap=new LinkedHashMap<String, List<JSONObject>>();
				for (JSONObject data : dlist) {
					String tclassid=data.getString("tClassId");
					String accountId=data.getString("accountId");
					String subGroupId=data.getString("examSubjectGroupId");
					String key=subGroupId+"_"+accountId+"_"+tclassid;
					if(usermap.containsKey(key)){
						List<JSONObject> list=usermap.get(key);
						list.add(data);
//						Collections.sort(list, new Comparator<JSONObject>() {
//							@Override
//							public int compare(JSONObject arg0, JSONObject arg1) {
//								// TODO Auto-generated method stub
//									int result=0;
//									result = Long.compare(arg0.getDate("startTime").getTime(),
//											arg1.getDate("startTime").getTime());
//									if (result != 0) {
//										return result;
//									}
//									result = Integer.compare(arg0.getInteger("subjectId"),
//										arg1.getInteger("subjectId"));
//									if (result != 0) {
//										return result;
//									}
//									result = Integer.compare(arg0.getInteger("subjectLevel"),
//											arg1.getInteger("subjectLevel"));
//									if (result != 0) {
//										return result;
//									}
//								return result;
//							}
//							
//						});
					}else{
						List<JSONObject> list=new ArrayList<JSONObject>();
						list.add(data);
						usermap.put(key, list);
					}
				}
				
				Iterator<Entry<String, List<JSONObject>>> it3 = usermap.entrySet()
						.iterator();
				
				//循环合并科目
				while (it3.hasNext()) {
					Entry<String, List<JSONObject>> en=it3.next();
					List<JSONObject> list=en.getValue();
					Map<String,List<JSONObject>> map=this.getMergeSubject(list);//按时间合并分组
					
					Iterator<Entry<String, List<JSONObject>>> it4 = map.entrySet()
							.iterator();
					int i = 1;
					while (it4.hasNext()) {
						Entry<String, List<JSONObject>> en1=it4.next();
						List<JSONObject> list1=en1.getValue();
						JSONObject d=new JSONObject();
						if(!CollectionUtils.isEmpty(list1)&&list1.size()>1){
							Collections.sort(list1, new Comparator<JSONObject>() {
								@Override
								public int compare(JSONObject arg0, JSONObject arg1) {
									// TODO Auto-generated method stub
										int result=0;
										result = Integer.compare(arg0.getInteger("subjectId"),
											arg1.getInteger("subjectId"));
										if (result != 0) {
											return result;
										}
										result = Integer.compare(arg0.getInteger("subjectLevel"),
												arg1.getInteger("subjectLevel"));
										if (result != 0) {
											return result;
										}
									return result;
								}
								
							});
							for(JSONObject o:list1){
								String examSubjName = o.getString("examSubjSimpleName");
								String subjectNames = d.getString("subjectName");
								if (subjectNames == null) {
									o.put("subjectName", examSubjName);
									d=o;
								} else {
									subjectNames += "+" + examSubjName;
									o.put("subjectName", subjectNames);
									d=o;
								}
							}
						}else if(!CollectionUtils.isEmpty(list1)&&list1.size()==1){
							String examSubjName = list1.get(0).getString("examSubjName");
							list1.get(0).put("subjectName", examSubjName);
							d=list1.get(0);
						}
							
						examPlaceName = d.getString("examPlaceName");
						d.put(
								"studName",
								studentMap.containsKey(d.getString("accountId")) ? studentMap
										.get(d.getString("accountId")).getName()
										: "");
						d.put("xh", i);
						i++;
						datalist.add(d);
					}
					
				}
				
				Collections.sort(datalist, new Comparator<JSONObject>() {//按时间顺序排序
					@Override
					public int compare(JSONObject arg0, JSONObject arg1) {
						// TODO Auto-generated method stub
						int result=0;
						result = Long.compare(arg0.getDate("startTime").getTime(),
								arg1.getDate("startTime").getTime());
						if (result != 0) {
							return result;
						}
						return result;
					}
					
				});
				

				da.put("examPlaceName", examPlaceName);
				da.put("seatNumber", seatNumber);
				da.put("list", datalist);
				dalist.add(da);
			}
		}
		res.put("data", dalist);

		return res;
	}

	@Override
	public int getTableCornerListCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		return examManagementViewDao.getTableCornerListcount(param);
	}

	@Override
	public JSONObject getTClassAndExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		String name = param.get("name").toString();
		int curPageCount = Integer
				.valueOf(param.get("curPageCount").toString());
		List<String> accountIds = new ArrayList<String>();
		JSONObject res = new JSONObject();
		HashMap<String, Account> stumap = new HashMap<String, Account>();

		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);

		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(param,
				param.get("termInfo").toString(), autoIncr);
		if (examPlanList.size() == 0) {
			throw new CommonRunException(0, "获取不到对应年级的考试计划，请前往排考中设置");
		}
		List<String> gralist = new ArrayList<String>();
		for (ExamPlan ep : examPlanList) {
			gralist.add(ep.getUsedGrade());
		}
		List<JSONObject> tlist = new ArrayList<JSONObject>();
		List<Account> al = new ArrayList<Account>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		// sdk速度太慢，所以查名字 和不查名字 用不同的sdk调用
		param.put("pageEnd", "");
		if (!name.toString().isEmpty()) {
			map.put("schoolId", param.get("schoolId"));
			map.put("termInfoId", param.get("termInfo"));
			map.put("usedGradeId", StringUtils.join(gralist, ","));
			map.put("keyword", name);
			al = commonDataService.getStudentList(map);
			for (Account a : al) {
				stumap.put(a.getId() + "", a);
				accountIds.add(a.getId() + "");
			}
			if (!name.isEmpty() && accountIds.isEmpty()) {
				throw new CommonRunException(0, "未查到学生相关信息");
			}
			param.put("accountIds", accountIds.isEmpty() ? null : accountIds);

			tlist = examManagementViewDao.getTClassAndExamPlace(param);
		} else {
			tlist = examManagementViewDao.getTClassAndExamPlace(param);
			List<Long> accids = new ArrayList<Long>();
			for (JSONObject es : tlist) {
				accids.add(Long.valueOf(es.getString("accountId")));
			}
			al = commonDataService.getAccountBatch(
					Long.valueOf(param.get("schoolId").toString()), accids,
					param.get("termInfo").toString());
			for (Account a : al) {
				stumap.put(a.getId() + "", a);
			}
		}
		final List<String> classnamel = new ArrayList<String>();
		ExamPlan ep = examPlanList.get(0);
		String scheduleId = ep.getScheduleId();
		String usedGrade = ep.getUsedGrade();
		if (scheduleId.isEmpty()) {
			List<Long> ids = new ArrayList<Long>();
			for (JSONObject es : tlist) {
				ids.add(Long.valueOf(es.getString("tClassId")));
			}
			List<Classroom> classrooms = commonDataService.getClassroomBatch(
					Long.valueOf(param.get("schoolId").toString()), ids, param
							.get("termInfo").toString());
			Map<String, Classroom> classmap = new HashMap<String, Classroom>();
			for (Classroom c : classrooms) {
				classmap.put(c.getId() + "", c);
			}
			for (JSONObject se : tlist) {
				if (classmap.containsKey(se.getString("tClassId"))) {
					se.put("tClassName", classmap.get(se.getString("tClassId"))
							.getClassName());
					classnamel.add(classmap.get(se.getString("tClassId"))
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
			for (JSONObject se : tlist) {
				if (classmap.containsKey(se.getString("tClassId"))) {
					se.put("tClassName", classmap.get(se.getString("tClassId"))
							.getTclassName());
					classnamel.add(classmap.get(se.getString("tClassId"))
							.getTclassName());
				}

			}
		}

		if (!param.containsKey("export")) {// 给导出用

			int totalCount = examManagementViewDao
					.getTClassAndExamPlaceCount(param);

			int totalPage = (totalCount + curPageCount - 1) / curPageCount;

			res.put("totalCount", totalCount);
			res.put("totalPage", totalPage);
		}

		Collections.sort(classnamel,
				Collator.getInstance(java.util.Locale.CHINA));
		final LinkedHashMap<String, String> clasnamep = new LinkedHashMap<String, String>();
		for (String cname : classnamel) {
			clasnamep.put(cname, cname);
		}

		Collections.sort(tlist, new Comparator<JSONObject>() {
			public int compare(JSONObject arg0, JSONObject arg1) {
				// 第一次比较
				int onesort = Integer.valueOf(arg0.getString("subjectSort")
						.substring(0, 1));
				int twosort = Integer.valueOf(arg1.getString("subjectSort")
						.substring(0, 1));
				int i = onesort == twosort ? 0 : (onesort < twosort ? -1 : 1);
				// logger.info(arg0.getString("subjectSort")+" VS "+arg1.getString("subjectSort"));
				// logger.info("i-----------------"+i);
				// 如果相同则进行第二次比较
				if (i == 0) {
					
					int result = arg0.getString("createDateTime").compareTo(
							arg1.getString("createDateTime"));
					if (result == 0) {
						// 第二次比较
						int onekey = 0, twokey = 0, order1 = 0, order2 = 0;
						for (Entry<String, String> entry : clasnamep.entrySet()) {
							String namse = entry.getKey();
							if (namse.equals(arg0.getString("tClassName"))) {
							// System.out.println("onekey===="+onekey);
								onekey = order1;
							}

							if (namse.equals(arg1.getString("tClassName"))) {
								// System.out.println("twokey===="+twokey);
								twokey = order2;
							}
							order1++;
							order2++;
						}
						int j = onekey == twokey ? 0 : onekey < twokey ? -1 : 1;
						if (j == 0) {// 第三次比较
							long accountId1 = arg0.getLongValue("accountId");
							long accountId2 = arg1.getLongValue("accountId");
							return accountId1 == accountId2 ? 0
									: accountId1 < accountId2 ? -1 : 1;
						}
						return j;
					}
					return result;
				}
				return i;
			}
		});
		
		Map<String,String> armap=this.getsubjectDis(param);//重名处理
			for (JSONObject o : tlist) {
				if (armap.containsKey(o.getString("examSubjectGroupId"))) {
					o.put("examSubjectNames",
							armap.get(o.getString("examSubjectGroupId")));
				}
			}
		
		if (!param.containsKey("export")) {
			int curPage = Integer.valueOf(param.get("curPage").toString());
			int start = (curPage - 1) * curPageCount;
			int end = curPage * curPageCount;
			if (tlist.size() < end) {
				tlist = tlist.subList(start, tlist.size());
			} else {
				tlist = tlist.subList(start, end);
			}

		}

		int i = 1;
		for (JSONObject data : tlist) {

			String stuname = "";
			if (stumap.containsKey(data.getString("accountId"))) {
				stuname = stumap.get(data.getString("accountId")).getName();
			}
			data.put("studName", stuname);
			data.put("xh", i);
			i++;
		}
		
		res.put("data", tlist);
		return res;
	}

	@Override
	public List<ExamPlan> getExamPlanList(Map<String, Object> param) {
		// TODO Auto-generated method stub

		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		return examManagementDao.getExamPlanList(param, param.get("termInfo")
				.toString(), autoIncr);
	}

	@Override
	public int getTClassAndExamPlaceCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		return examManagementViewDao.getTClassAndExamPlaceCount(param);
	}

	@Override
	public JSONObject getStudsAndExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub

		JSONObject res = new JSONObject();
		String name = param.get("name").toString();
		int curPageCount = Integer
				.valueOf(param.get("curPageCount").toString());
		List<String> accountIds = new ArrayList<String>();

		HashMap<String, Account> stumap = new HashMap<String, Account>();

		List<Account> al = new ArrayList<Account>();

		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);

		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(param,
				param.get("termInfo").toString(), autoIncr);
		if (examPlanList.size() == 0) {
			throw new CommonRunException(0, "获取不到对应年级的考试计划，请前往排考中设置");
		}
		List<JSONObject> tlist = new ArrayList<JSONObject>();
		ExamPlan ep = examPlanList.get(0);
		String scheduleId = ep.getScheduleId();
		String usedGrade = ep.getUsedGrade();
		List<String> gralist = new ArrayList<String>();
		for (ExamPlan ep1 : examPlanList) {
			gralist.add(ep1.getUsedGrade());
		}

		if (!name.toString().isEmpty()) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", param.get("schoolId"));
			map.put("termInfoId", param.get("termInfo"));
			map.put("usedGradeId", StringUtils.join(gralist, ","));
			map.put("keyword", name);
			al = commonDataService.getStudentList(map);

			for (Account a : al) {
				stumap.put(a.getId() + "", a);
				accountIds.add(a.getId() + "");
			}
			if (!name.isEmpty() && accountIds.isEmpty()) {
				throw new CommonRunException(0, "未查到学生相关信息");
			}
			param.put("accountId", accountIds.isEmpty() ? null : accountIds);

			tlist = examManagementViewDao.getStudsAndExamPlace(param);
		} else {
			tlist = examManagementViewDao.getStudsAndExamPlace(param);
			List<Long> accids = new ArrayList<Long>();
			for (JSONObject es : tlist) {
				accids.add(Long.valueOf(es.getString("accountId")));
			}
			al = commonDataService.getAccountBatch(
					Long.valueOf(param.get("schoolId").toString()), accids,
					param.get("termInfo").toString());
			for (Account a : al) {
				stumap.put(a.getId() + "", a);
			}

		}

		if (scheduleId.isEmpty()) {
			List<Long> ids = new ArrayList<Long>();
			for (JSONObject es : tlist) {
				ids.add(Long.valueOf(es.getString("tClassId")));
			}
			List<Classroom> classrooms = commonDataService.getClassroomBatch(
					Long.valueOf(param.get("schoolId").toString()), ids, param
							.get("termInfo").toString());
			Map<String, Classroom> classmap = new HashMap<String, Classroom>();
			for (Classroom c : classrooms) {
				classmap.put(c.getId() + "", c);
			}
			for (JSONObject se : tlist) {
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
			for (JSONObject se : tlist) {
				if (classmap.containsKey(se.getString("tClassId"))) {
					se.put("tClassName", classmap.get(se.getString("tClassId"))
							.getTclassName());
				}
			}
		}

		

		Map<String, String> grademap = new HashMap<String, String>();
		final List<String> classnamel = new ArrayList<String>();
		int i = 1;
		for (JSONObject data : tlist) {
			String stuname = "";
			data.put("xh", i);
			classnamel.add(data.getString("tClassName"));
			i++;
			if (stumap.containsKey(data.getString("accountId"))) {
				stuname = stumap.get(data.getString("accountId")).getName();
			}
			data.put("studName", stuname);
			if (!grademap.containsKey(data.getString("usedGrade"))) {
				T_GradeLevel gl = T_GradeLevel.findByValue(Integer
						.parseInt(commonDataService.ConvertSYNJ2NJDM(
								data.getString("usedGrade"),
								param.get("termInfo").toString()
										.substring(0, 4))));
				grademap.put(data.getString("usedGrade"), njName.get(gl));
				data.put("gradeName", njName.get(gl));
				continue;
			} else {
				data.put("gradeName", grademap.get(data.getString("usedGrade")));
			}

		}
		
		
		if (!param.containsKey("export")) {// 给导出用

			int totalCount = examManagementViewDao
					.getStudsAndExamPlaceCount(param);

			int totalPage = (totalCount + curPageCount - 1) / curPageCount;

			res.put("totalCount", totalCount);
			res.put("totalPage", totalPage);
		}
		
		Map<String,String> armap=this.getsubjectDis(param);//重名处理
			for (JSONObject o : tlist) {
				if (armap.containsKey(o.getString("examSubjectGroupId"))) {
					o.put("examSubjectNames",
							armap.get(o.getString("examSubjectGroupId")));
				}
			}
		res.put("data", tlist);
		return res;
	}

	@Override
	public int getStudsAndExamPlaceCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(0, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		return examManagementViewDao.getStudsAndExamPlaceCount(param);
	}
	
	/**
	 * 合并科目
	 * @param list
	 * @return
	 */
	public Map<String,List<JSONObject>> getMergeSubject(List<JSONObject> list){
		
		Iterator<JSONObject> iterator=list.iterator();
		long start1=list.get(0).getDate("startTime").getTime();
		long end1=list.get(0).getDate("endTime").getTime();
		Map<String,List<JSONObject>> ss=new HashMap<String, List<JSONObject>>();
		int i=1;
		do {
			while(iterator.hasNext()){
				JSONObject gg=iterator.next();
				long start2=gg.getDate("startTime").getTime();
				long end2=gg.getDate("endTime").getTime();
				if(!ss.containsKey(i+"")){
					List<JSONObject> sl=new ArrayList<JSONObject>();
					ss.put(i+"",sl);
				}
				if(start1<=end2&&end2<=end1){
					List<JSONObject> sl=ss.get(i+"");
					sl.add(gg);
					ss.put(i+"", sl);
					iterator.remove();
				}
				else if(start1<=start2&&start2<=end1){
					List<JSONObject> sl=ss.get(i+"");
					sl.add(gg);
					ss.put(i+"",sl);
					iterator.remove();
				}
			}
			if(list.size()>0){
				iterator=list.iterator();
				 start1=list.get(0).getDate("startTime").getTime();
				 end1=list.get(0).getDate("endTime").getTime();
				i++;
			}else{
				break;
			}
		}
		while(true);
		return ss;
		
	}
	
	
	public static void main(String[] args) {
		JSONObject aJsonObject = new JSONObject();
		aJsonObject.put("accountId", "100003972359");
		System.out.println( aJsonObject.getLongValue("accountId") );
		
		 
		
	}
}
