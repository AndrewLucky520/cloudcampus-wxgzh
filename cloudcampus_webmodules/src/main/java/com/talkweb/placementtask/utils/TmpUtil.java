package com.talkweb.placementtask.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.ListSortUtil;
import com.talkweb.common.tools.SortUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.placementtask.domain.DezyDisTclassParams;
import com.talkweb.placementtask.domain.DezyDisTclassRunParams;
import com.talkweb.placementtask.domain.TPlConfIndexSubs;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezyClassgroup;
import com.talkweb.placementtask.domain.TPlDezySettings;
import com.talkweb.placementtask.domain.TPlDezySubjectSet;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompGroup;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezySubjectgroup;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.domain.TPlStudentinfo;

public class TmpUtil {

	public static boolean isStrInArr(String str, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(str)) {
				return true;
			}
		}
		return false;
	}

	public static int genTclassByUnfinishComps(
			List<TPlDezyTclassfrom> tclassFromList, int classNumMax,
			int classNumMin,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			ListSortUtil<TPlDezySubjectcomp> compSort,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList,
			HashMap<String, TPlDezyClass> xzclassMap,
			TPlDezyClassgroup classGroup, TPlDezySubjectgroup subGroup,
			int seq, String subId, List<TPlDezySubjectcomp> notFinishSubcomps,
			List<TPlDezyClass> dezyClassList, int score,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubcompList,
			Map<String, String> kmMcMap, int alg,
			HashMap<String, List<TPlDezyClass>> optionTclassMap, int gkType) {
		// 按行政班开完单科之后 凑班
		// 对未完成的志愿组合进行排序
		ListSortUtil<TPlDezySubjectcompGroup> compgSort = new ListSortUtil<TPlDezySubjectcompGroup>();
		List<String> finishSubcompIds = new ArrayList<String>();
		List<List<TPlDezySubjectcompGroup>> notFinishCompGroupList = convertToGroupList(
				notFinishSubcomps, subId);

		// 结束未完成的志愿组合凑班循环
		int loop = 0;
		int cz = (classNumMax - classNumMin) / 2;
		int avg = (classNumMax - classNumMin) / 2;

		int tmpClassNumMax = classNumMax;
		int tmpClassNumMin = classNumMin;
		while (finishSubcompIds.size() != notFinishSubcomps.size() && loop < 20) {
			loop++;

			if (gkType == 1) {

				genTclassByUnfinishCompGroupChild(tclassFromList,
						tmpClassNumMax, tmpClassNumMin, subjectCompMap,
						compgSort, xzgroudMap, fzgroudMap, fzgroudList,
						xzclassMap, classGroup, subGroup, seq, subId,
						notFinishCompGroupList.get(0), dezyClassList,
						finishSubcompIds, tclassSubcompList, kmMcMap, alg,
						true, optionTclassMap);
				genTclassByUnfinishCompGroupChild(tclassFromList,
						tmpClassNumMax, tmpClassNumMin, subjectCompMap,
						compgSort, xzgroudMap, fzgroudMap, fzgroudList,
						xzclassMap, classGroup, subGroup, seq, subId,
						notFinishCompGroupList.get(1), dezyClassList,
						finishSubcompIds, tclassSubcompList, kmMcMap, alg,
						false, optionTclassMap);
			} else if (gkType == 2) {
				genTclassByUnfinishCompGroupChild(tclassFromList,
						tmpClassNumMax, tmpClassNumMin, subjectCompMap,
						compgSort, xzgroudMap, fzgroudMap, fzgroudList,
						xzclassMap, classGroup, subGroup, seq, subId,
						notFinishCompGroupList.get(0), dezyClassList,
						finishSubcompIds, tclassSubcompList, kmMcMap, alg,
						true, optionTclassMap);
			} else if (gkType == 3) {
				genTclassByUnfinishCompGroupChild(tclassFromList,
						tmpClassNumMax, tmpClassNumMin, subjectCompMap,
						compgSort, xzgroudMap, fzgroudMap, fzgroudList,
						xzclassMap, classGroup, subGroup, seq, subId,
						notFinishCompGroupList.get(1), dezyClassList,
						finishSubcompIds, tclassSubcompList, kmMcMap, alg,
						false, optionTclassMap);
			}
		
			if (loop % 3 == 0) {
				tmpClassNumMin -= cz;
			}
			if (loop % 3 == 0) {
				tmpClassNumMax += cz;
			}
		}
		return (notFinishSubcomps.size() - finishSubcompIds.size());
	}

	public static List<String> genTclassByUnfinishCompGroupChild(
			List<TPlDezyTclassfrom> tclassFromList, int classNumMax,
			int classNumMin,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			ListSortUtil<TPlDezySubjectcompGroup> compSort,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList,
			HashMap<String, TPlDezyClass> xzclassMap,
			TPlDezyClassgroup classGroup, TPlDezySubjectgroup subGroup,
			int seq, String subId,
			List<TPlDezySubjectcompGroup> notFinishSubcomps,
			List<TPlDezyClass> dezyClassList, List<String> finishSubcompIds,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubcompList,
			Map<String, String> kmMcMap, int alg, boolean isGk,
			HashMap<String, List<TPlDezyClass>> optionTclassMap) {
		// 按行政班开完单科之后 凑班
		// 对未完成的志愿组合进行排序
		compSort.sort(notFinishSubcomps, "groupNum", "asc");
//		Collections.shuffle(notFinishSubcomps);
		for (int m = 0; m < notFinishSubcomps.size(); m++) {
			TPlDezySubjectcompGroup lsubComp = notFinishSubcomps.get(m);
			List<String> lsubCompIds = lsubComp.getSubjectCompId();
			// lsubCompIds = rmUnfitComps
			// (lsubCompIds,isGk,subjectCompMap,seq,subId);
			String maxRsClassId = lsubComp.getClassId();
			int maxRs = lsubComp.getGroupNum();
			int subNum = lsubComp.getGroupNum();
			if (lsubCompIds.size() == 0) {
				continue;
			}
			if (!finishSubcompIds.contains(lsubCompIds.get(0))) {
				int maxLenth = notFinishSubcomps.size();
				for (int l = 0; l < maxLenth; l++) {
					boolean isFit = false;
					List<String> tmpAddArr = new ArrayList<String>();
					for (int n = l; n > m; n--) {
						TPlDezySubjectcompGroup rsubComp = notFinishSubcomps
								.get(n);
						List<String> rsubCompIds = rsubComp.getSubjectCompId();
						// rsubCompIds= rmUnfitComps
						// (rsubCompIds,isGk,subjectCompMap,seq,subId);
						if (rsubCompIds.size() == 0) {
							continue;
						}
						if (!finishSubcompIds.contains(rsubCompIds.get(0))) {
							tmpAddArr.addAll(rsubCompIds);
							subNum += rsubComp.getGroupNum();
							if (rsubComp.getGroupNum() > maxRs) {
								maxRs = rsubComp.getGroupNum();
								maxRsClassId = rsubComp.getClassId();
							}
							// 人数符合开班条件
							if (isRsFullfill(subNum, classNumMin, classNumMax)) {
								isFit = true;
								break;
							}
						}
					}
					if (!isFit
							&& !finishSubcompIds.contains(lsubCompIds.get(0))) {
						if (isRsFullfill(subNum, classNumMin, classNumMax)) {
							isFit = true;
						}
					}
					// 人数符合开班条件
					if (isFit) {
						tmpAddArr.addAll(lsubCompIds);
						TPlDezyClass mxrsClass = xzclassMap.get(maxRsClassId);
						List<TPlDezyClass> relatedClasses = TmpUtil
								.getRelatedClassesBySubcomId(xzclassMap,
										subjectCompMap, tmpAddArr);
						// 生成教学班
						TPlDezyClass newTclass = genNZtTclass(classGroup,
								subGroup, seq, subId, mxrsClass, subNum,
								isGk ? 1 : 2, xzgroudMap, fzgroudMap,
								fzgroudList, kmMcMap, alg, relatedClasses);
						finishSubcompIds.addAll(tmpAddArr);
						// genRelationOfTclassBySubcomps(newTclass, xzclassMap,
						// tmpAddArr, tclassFromList, subjectCompMap);
						genRelationOfTclassSubCompByIds(tmpAddArr, newTclass,
								tclassSubcompList);
						genFinishSubCompsExtend(tmpAddArr, subjectCompMap,
								subId, seq, newTclass.getTclassId());

						dezyClassList.add(newTclass);

						addTclassToOptMap(optionTclassMap, newTclass);

						break;
					} else {
						// subNum = lsubComp.getCompNum();
						TPlDezyClass fitTcl = null;
						int tlvl = isGk ? 1 : 2;
						if (subNum < (classNumMax + classNumMin) / 2) {
							String optKey = classGroup.getClasstGroupId() + "_"
									+ subId + "_" + seq + "_" + tlvl;
							List<TPlDezyClass> tclList = optionTclassMap
									.get(optKey);
							if (tclList != null) {
								for (TPlDezyClass tcl : tclList) {
									if (isRsFullfill(
											subNum + tcl.getTclassNum(),
											classNumMin, classNumMax)
											&& tcl.getIsExpClass() == 0) {
										isFit = true;
										fitTcl = tcl;
										break;
									}
								}
							}
						}
						// 与已有教学班凑班成功
						if (isFit && fitTcl != null) {
							tmpAddArr.addAll(lsubCompIds);
							finishSubcompIds.addAll(tmpAddArr);
							fitTcl.setTclassNum(subNum + fitTcl.getTclassNum());
							genRelationOfTclassSubCompByIds(tmpAddArr, fitTcl,
									tclassSubcompList);
							genFinishSubCompsExtend(tmpAddArr, subjectCompMap,
									subId, seq,fitTcl.getTclassId());
							break;
						} else {
							// isFit = false;
							// boolean isOver = (subNum > (classNumMax +
							// classNumMin) / 2);
							// //无法凑班则拆原有教学班
							// String optKey =
							// classGroup.getClasstGroupId()+"_"+subId+"_"+seq
							// +"_"+tlvl;
							// List<TPlDezyClass> tclList =
							// optionTclassMap.get(optKey);
							// if(tclList!=null){
							// for (TPlDezyClass tcl : tclList) {
							// String tcid = tcl.getTclassId();
							// List<TPlDezyTclassSubcomp> spList =
							// tclassSubcompList.get(tcid);
							// int orTclassNum = tcl.getTclassNum();
							// if(!isOver){
							// for(TPlDezyTclassSubcomp sp:spList){
							// int spNum = subjectCompMap.get(
							// sp.getSubjectCompId()).getCompNum();
							//
							// if(isRsFullfill(subNum+spNum,classNumMin,classNumMax)
							// &&isRsFullfill(orTclassNum-spNum,classNumMin,classNumMax)
							// &&!lsubCompIds.contains(sp.getSubjectCompId())){
							//
							// tmpAddArr.addAll(lsubCompIds);
							// tmpAddArr.add(sp.getSubjectCompId());
							// TPlDezyClass mxrsClass =
							// xzclassMap.get(maxRsClassId);
							// List<TPlDezyClass> relatedClasses =
							// TmpUtil.getRelatedClassesBySubcomId(xzclassMap,subjectCompMap,tmpAddArr);
							// // 生成教学班
							// TPlDezyClass newTclass = genNZtTclass(classGroup,
							// subGroup, seq, subId, mxrsClass, subNum+spNum,
							// isGk ? 1 : 2, xzgroudMap, fzgroudMap,
							// fzgroudList, kmMcMap, alg,relatedClasses);
							//
							// finishSubcompIds.addAll(lsubCompIds);
							//
							// genRelationOfTclassSubCompByIds(tmpAddArr,
							// newTclass,
							// tclassSubcompList);
							// tcl.setTclassNum(orTclassNum-spNum);
							// removeOrTclassSp(sp,tcl,tclassSubcompList);
							// addTclassToOptMap(optionTclassMap, newTclass);
							// dezyClassList.add(newTclass);
							//
							// isFit = true;
							// break;
							// }
							// }
							// }
							// if(isFit){
							// break;
							// }
							// }
							// }
							if (!isFit) {
								subNum = lsubComp.getGroupNum();
							} else {
								break;
							}
						}
					}
				}
			}
		}
		// 结束未完成的志愿组合凑班循环
		return finishSubcompIds;
	}

	private static List<String> rmUnfitComps(List<String> lsubCompIds,
			boolean isGk, HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			int seq, String subId) {
		// TODO Auto-generated method stub
		List<String> rs = new ArrayList<String>();
		for (String lsub : lsubCompIds) {
			TPlDezySubjectcomp sp = subjectCompMap.get(lsub);
			if (sp.getFinishSeqs().contains((Integer) seq)) {
				boolean fit = true;
				if (isGk && sp.getUnfinishProSubTasks().contains(subId)) {
					fit = false;
				}
				if (!isGk && sp.getUnfinishOptSubTasks().contains(subId)) {
					fit = false;
				}
				if (fit) {
					rs.add(lsub);
				}
			}
		}
		return rs;
	}

	public static void removeOrTclassSp(TPlDezyTclassSubcomp sp,
			TPlDezyClass tcl,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubcompList) {
		// TODO Auto-generated method stub
		List<TPlDezyTclassSubcomp> tspList = tclassSubcompList.get(tcl
				.getTclassId());
		List<TPlDezyTclassSubcomp> rmList = new ArrayList<TPlDezyTclassSubcomp>();
		if(tspList==null){
			System.out.println("fdsf");
			return;
		}
		for (TPlDezyTclassSubcomp tsp : tspList) {
			if ( sp.getSubjectCompId().equals(tsp.getSubjectCompId())) {
				rmList.add(tsp);
			}
		}

		tspList.removeAll(rmList);
		tclassSubcompList.put(tcl.getTclassId(), tspList);
	}

	public static void removeOrTclassMutilSp(List<String> spIds,
			TPlDezyClass tcl,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubcompList) {
		// TODO Auto-generated method stub
		List<TPlDezyTclassSubcomp> tspList = tclassSubcompList.get(tcl
				.getTclassId());
		List<TPlDezyTclassSubcomp> rmList = new ArrayList<TPlDezyTclassSubcomp>();
		for (TPlDezyTclassSubcomp tsp : tspList) {
			if (tsp.getTclassId().equals(tcl.getTclassId())
					&& spIds.contains(tsp.getSubjectCompId())) {
				rmList.add(tsp);
			}
		}

		tspList.removeAll(rmList);
		tclassSubcompList.put(tcl.getTclassId(), tspList);
	}

	public static void addTclassToOptMap(
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			TPlDezyClass newTclass) {
		String optKey = newTclass.getClassGroupId() + "_"
				+ newTclass.getSubjectId() + "_" + newTclass.getClassSeq()
				+ "_" + newTclass.getTclassLevel();
		List<TPlDezyClass> tclList = null;
		if (optionTclassMap.containsKey(optKey)) {
			tclList = optionTclassMap.get(optKey);
		} else {
			tclList = new ArrayList<TPlDezyClass>();
		}
		tclList.add(newTclass);
		optionTclassMap.put(optKey, tclList);
	}

	public static void removeTclassFromOptMap(
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			TPlDezyClass newTclass,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap) {
		String optKey = newTclass.getClassGroupId() + "_"
				+ newTclass.getSubjectId() + "_" + newTclass.getClassSeq()
				+ "_" + newTclass.getTclassLevel();
		List<TPlDezyClass> tclList = null;
		if (optionTclassMap.containsKey(optKey)) {
			tclList = optionTclassMap.get(optKey);
		} else {
			tclList = new ArrayList<TPlDezyClass>();
		}
		tclList.remove(newTclass);
		tclassSubMap.remove(newTclass.getTclassId());
		optionTclassMap.put(optKey, tclList);
	}

	/**
	 * 根据志愿组合生成的教学班生成教学班、行政班关联关系
	 * 
	 * @param newTclass
	 * @param xzclassMap
	 * @param tmpAddArr
	 * @param tclassFromList
	 * @param subjectCompMap
	 */
	public static void genRelationOfTclassBySubcomps(TPlDezyClass newTclass,
			HashMap<String, TPlDezyClass> xzclassMap, List<String> tmpAddArr,
			List<TPlDezyTclassfrom> tclassFromList,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap) {
		// TODO Auto-generated method stub
		for (String subcomp : tmpAddArr) {
			TPlDezySubjectcomp subjectComp = subjectCompMap.get(subcomp);
			TPlDezyClass xzclass = xzclassMap.get(subjectComp.getClassId());
			TPlDezyTclassfrom re = new TPlDezyTclassfrom();
			re.setClassGroupId(newTclass.getClassGroupId());
			re.setClassId(xzclass.getTclassId());
			re.setPlacementId(newTclass.getPlacementId());
			re.setSchoolId(newTclass.getSchoolId());
			re.setSubjectGroupId(newTclass.getSubjectGroupId());
			re.setTclassId(newTclass.getTclassId());
			re.setUsedGrade(newTclass.getUsedGrade());

			boolean contain = false;
			for (TPlDezyTclassfrom rre : tclassFromList) {
				if (rre.getTclassId().equals(re.getTclassId())
						&& rre.getClassId().equals(re.getClassId())
						&& rre.getClassGroupId().equals(re.getClassGroupId())) {
					contain = true;
					break;
				}
			}
			if (!contain) {

				tclassFromList.add(re);
			}
		}
	}

	/**
	 * 根据单志愿组合构成的教学班生成教学班行政班关联关系
	 * 
	 * @param xzclass
	 * @param newTclass
	 * @param tclassFromList
	 */
	public static void genRelationOfTclass(TPlDezyClass xzclass,
			TPlDezyClass newTclass, List<TPlDezyTclassfrom> tclassFromList) {
		// TODO Auto-generated method stub
		TPlDezyTclassfrom re = new TPlDezyTclassfrom();
		re.setClassGroupId(newTclass.getClassGroupId());
		re.setClassId(xzclass.getTclassId());
		re.setPlacementId(newTclass.getPlacementId());
		re.setSchoolId(newTclass.getSchoolId());
		re.setSubjectGroupId(newTclass.getSubjectGroupId());
		re.setTclassId(newTclass.getTclassId());
		re.setUsedGrade(newTclass.getUsedGrade());
		tclassFromList.add(re);
	}

	public static boolean isRsFullfill(int subNum, int classNumMin,
			int classNumMax) {
		// TODO Auto-generated method stub
		if (subNum >= classNumMin && subNum <= classNumMax) {
			return true;
		}
		return false;
	}

	public static TPlDezyClass genZtTclass(TPlDezyClassgroup classGroup,
			TPlDezySubjectgroup subGroup, int seq, String subId,
			TPlDezyClass xzclass, int sumrs, int classLevel,
			ConcurrentHashMap<String, String> xzgroudMap,
			Map<String, String> kmMcMap, int alg) {
		TPlDezyClass xkclass = new TPlDezyClass();
		xkclass.setClassGroupId(classGroup.getClasstGroupId());
		xkclass.setClassSeq(seq);
		xkclass.setGroundId(xzclass.getGroundId());
		xkclass.setGroundName(xzclass.getGroundName());
		xkclass.setPlacementId(classGroup.getPlacementId());
		xkclass.setSchoolId(classGroup.getSchoolId());
		if (subGroup != null) {
			xkclass.setSubjectGroupId(subGroup.getSubjectGroupId());
		}else{
			xkclass.setSubjectGroupId("-999");
		}
		xkclass.setSubjectId(subId);
		xkclass.setTclassId(UUIDUtil.getUUID());
		xkclass.setTclassLevel(classLevel);
		xkclass.setTclassNum(sumrs);
		xkclass.setTclassType(7);
		xkclass.setUsedGrade(classGroup.getUsedGrade());
		StringBuffer tclassName = new StringBuffer();
		tclassName.append(kmMcMap.get(subId)).append("（")
				.append(classLevel == 1 ? "选" : "学").append("）").append("（")
				.append(xkclass.getGroundName().replaceAll("教室", ""))
				.append("）");
		xkclass.setTclassName(tclassName.toString());
		xkclass.setOriClassName(tclassName.toString());
//		if (alg == 2) {
//			xkclass.setTclassName(tclassName.toString() + seq);
//
//		}
		String subgId = "";
		if (subGroup != null && subGroup.getSubjectGroupId() != null
				&& alg == 1) {
			subgId = subGroup.getSubjectGroupId();
		}
		xzgroudMap.put(subgId + "_" + xzclass.getTclassId() + "_" + seq,
				"exist");
		// 设置实验班、平行班组合不进入实验班
		xkclass.setIsExpClass(xzclass.getIsExpClass());
		return xkclass;
	}

	public static TPlDezyClass genNZtTclass(TPlDezyClassgroup classGroup,
			TPlDezySubjectgroup subGroup, int seq, String subId,
			TPlDezyClass xzclass, int sumrs, int classLevel,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList, Map<String, String> kmMcMap,
			int alg, List<TPlDezyClass> relatedClasses) {
		TPlDezyClass xkclass = new TPlDezyClass();
		xkclass.setClassGroupId(classGroup.getClasstGroupId());
		xkclass.setClassSeq(seq);
		String subgId = "";
		if (subGroup != null && subGroup.getSubjectGroupId() != null
				&& alg == 1) {
			subgId = subGroup.getSubjectGroupId();
		}
		String substr = "";
		if (seq == 8) {
			substr = "B";
		}
//		if (!xzgroudMap.containsKey(subgId + "_" + xzclass.getTclassId() + "_"
//				+ seq)) {
//
//			xkclass.setGroundId(xzclass.getGroundId());
//			xkclass.setGroundName(xzclass.getGroundName());
//
//			xzgroudMap.put(subgId + "_" + xzclass.getTclassId() + "_" + seq,
//					"exist");
//			if (seq == 8) {
//				xzgroudMap.put(subgId + "_" + xzclass.getTclassId() + "_" + 3,
//						"exist");
//			}
//		} else {
//			String gid = null;
//			String gname = null;
//			boolean canArr = true;
//			for (int i = 0; i < fzgroudList.size(); i++) {
//				String gskey = subgId + "_" + i;
//				if (fzgroudMap.containsKey(gskey)) {
//					List<Integer> seqList = fzgroudMap.get(gskey);
//					if (seqList.contains((Integer) seq)) {
//						canArr = false;
//					} else {
//						canArr = true;
//					}
//				} else {
//					List<Integer> seqList = new ArrayList<Integer>();
//					seqList.add((Integer) seq);
//					fzgroudMap.put(gskey, seqList);
//					canArr = true;
//				}
//				if (canArr) {
//
//					JSONObject ground = fzgroudList.get(i);
//					gid = ground.getString("groundId");
//					gname = ground.getString("groundName");
//					fzgroudMap.get(gskey).add(seq);
//					break;
//				}
//			}
//			// 优先其它组内行政班
//			for (TPlDezyClass tcl : relatedClasses) {
//
//				if (!xzgroudMap.containsKey(subgId + "_" + tcl.getTclassId()
//						+ "_" + seq)) {
//
//					gid = (tcl.getGroundId());
//					gname = (tcl.getGroundName() + substr);
//					xzgroudMap.put(
//							subgId + "_" + tcl.getTclassId() + "_" + seq,
//							"exist");
//					if (seq == 8) {
//						xzgroudMap.put(subgId + "_" + tcl.getTclassId() + "_"
//								+ 3, "exist");
//					}
//					break;
//				}
//			}
//			// 无行政班教室可用
//			if (gid == null) {
//				JSONObject ground = new JSONObject();
//				ground.put("groundId", UUIDUtil.getUUID());
//				ground.put("groundName", "辅助教室" + (fzgroudList.size() + 1)
//						+ substr);
//				gid = ground.getString("groundId");
//				gname = ground.getString("groundName");
//				fzgroudList.add(ground);
//
//				List<Integer> seqList = new ArrayList<Integer>();
//				seqList.add(seq);
//				if (seq == 8) {
//					seqList.add(3);
//				}
//				fzgroudMap
//						.put(subgId + "_" + (fzgroudList.size() - 1), seqList);
//			}
//			xkclass.setGroundId(gid);
//			xkclass.setGroundName(gname);
//		}
		xkclass.setPlacementId(classGroup.getPlacementId());
		xkclass.setSchoolId(classGroup.getSchoolId());
		if (subGroup == null) {

			xkclass.setSubjectGroupId("-999");
		} else {

			xkclass.setSubjectGroupId(subGroup.getSubjectGroupId());
		}
		xkclass.setSubjectId(subId);
		xkclass.setTclassId(UUIDUtil.getUUID());
		xkclass.setTclassLevel(classLevel);
		xkclass.setTclassNum(sumrs);
		xkclass.setTclassType(7);
		xkclass.setUsedGrade(classGroup.getUsedGrade());

//		StringBuffer tclassName = new StringBuffer();
//		tclassName.append(kmMcMap.get(subId)).append("（")
//				.append(classLevel == 1 ? "选" : "学").append("）").append("（")
//				.append(xkclass.getGroundName().replaceAll("教室", ""))
//				.append("）");
//		xkclass.setTclassName(tclassName.toString());

		return xkclass;
	}

	public static TPlDezyClass genNZtTclassByTclass(TPlDezyClass lclass,
			int seq, String subId, TPlDezyClass xzclass, int sumrs,
			int classLevel, ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList, Map<String, String> kmMcMap,
			int alg, List<TPlDezyClass> relatedClasses) {
		TPlDezyClass xkclass = new TPlDezyClass();
		xkclass.setClassGroupId(lclass.getClassGroupId());
		xkclass.setClassSeq(seq);
		// String subgId = lclass.getSubjectGroupId();
//		String subgId = "";
//		if (lclass.getSubjectGroupId() != null && alg == 1) {
//			subgId = lclass.getSubjectGroupId();
//		}
//		if (!xzgroudMap.containsKey(subgId + "_" + xzclass.getTclassId() + "_"
//				+ seq)) {
//
//			xkclass.setGroundId(xzclass.getGroundId());
//			xkclass.setGroundName(xzclass.getGroundName());
//
//			xzgroudMap.put(subgId + "_" + xzclass.getTclassId() + "_" + seq,
//					"exist");
//		} else {
//			String gid = null;
//			String gname = null;
//			boolean canArr = true;
//			for (int i = 0; i < fzgroudList.size(); i++) {
//				String gskey = subgId + "_" + i;
//				if (fzgroudMap.containsKey(gskey)) {
//					List<Integer> seqList = fzgroudMap.get(gskey);
//					if (seqList.contains(seq)) {
//						canArr = false;
//					} else {
//						canArr = true;
//					}
//				} else {
//					List<Integer> seqList = new ArrayList<Integer>();
//					seqList.add(seq);
//					fzgroudMap.put(gskey, seqList);
//					canArr = true;
//				}
//				if (canArr) {
//
//					JSONObject ground = fzgroudList.get(i);
//					gid = ground.getString("groundId");
//					gname = ground.getString("groundName");
//					fzgroudMap.get(gskey).add(seq);
//					break;
//				}
//			}
//			// 优先其它组内行政班
//			for (TPlDezyClass tcl : relatedClasses) {
//
//				if (!xzgroudMap.containsKey(subgId + "_" + tcl.getTclassId()
//						+ "_" + seq)) {
//
//					gid = (tcl.getGroundId());
//					gname = (tcl.getGroundName());
//					xzgroudMap.put(
//							subgId + "_" + tcl.getTclassId() + "_" + seq,
//							"exist");
//					break;
//				}
//			}
//			// 无行政班教室可用
//			if (gid == null) {
//				JSONObject ground = new JSONObject();
//				ground.put("groundId", UUIDUtil.getUUID());
//				ground.put("groundName", "辅助教室" + (fzgroudList.size() + 1));
//				gid = ground.getString("groundId");
//				gname = ground.getString("groundName");
//				fzgroudList.add(ground);
//
//				List<Integer> seqList = new ArrayList<Integer>();
//				seqList.add(seq);
//				fzgroudMap
//						.put(subgId + "_" + (fzgroudList.size() - 1), seqList);
//			}
//			xkclass.setGroundId(gid);
//			xkclass.setGroundName(gname);
//		}
		xkclass.setPlacementId(lclass.getPlacementId());
		xkclass.setSchoolId(lclass.getSchoolId());

		xkclass.setSubjectGroupId(lclass.getSubjectGroupId());
		xkclass.setSubjectId(subId);
		xkclass.setTclassId(UUIDUtil.getUUID());
		xkclass.setTclassLevel(classLevel);
		xkclass.setTclassNum(sumrs);
		xkclass.setTclassType(7);
		xkclass.setUsedGrade(lclass.getUsedGrade());

//		StringBuffer tclassName = new StringBuffer();
//		tclassName.append(kmMcMap.get(subId)).append("（")
//				.append(classLevel == 1 ? "选" : "学").append("）").append("（")
//				.append(xkclass.getGroundName().replaceAll("教室", ""))
//				.append("）");
//		xkclass.setTclassName(tclassName.toString());

		return xkclass;
	}

	/**
	 * 生成教学班与教学班构成的关联关系
	 * 
	 * @param inArrSubcomps
	 * @param newTclass
	 * @param tclassSubcompList
	 * @param tclassSubMap
	 */
	public static void genRelationOfTclassSubComp(
			List<TPlDezySubjectcomp> inArrSubcomps, TPlDezyClass newTclass,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap) {
		// TODO Auto-generated method stub
		List<TPlDezyTclassSubcomp> tclSpList = null;
		if (tclassSubMap.containsKey(newTclass.getTclassId())) {
			tclSpList = tclassSubMap.get(newTclass.getTclassId());
		} else {
			tclSpList = new ArrayList<TPlDezyTclassSubcomp>();
		}

		for (TPlDezySubjectcomp subcomp : inArrSubcomps) {
			TPlDezyTclassSubcomp tc = new TPlDezyTclassSubcomp();
			tc.setPlacementId(newTclass.getPlacementId());
			tc.setSchoolId(newTclass.getSchoolId());
			tc.setSubjectCompId(subcomp.getSubjectCompId());
			tc.setTclassId(newTclass.getTclassId());
			tc.setUsedGrade(newTclass.getUsedGrade());

			// tclassSubcompList.add(tc);
			tclSpList.add(tc);
		}
		tclassSubMap.put(newTclass.getTclassId(), tclSpList);
	}

	/**
	 * 生成教学班与教学班构成的关联关系
	 * 
	 * @param inArrSubcomps
	 * @param newTclass
	 * @param subjectCompMap
	 * @param tclassSubcompList
	 */
	public static void genRelationOfTclassSubCompByIds(List<String> subcompIds,
			TPlDezyClass newTclass,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap) {
		// TODO Auto-generated method stub

		List<TPlDezyTclassSubcomp> tclSpList = null;
		if (tclassSubMap.containsKey(newTclass.getTclassId())) {
			tclSpList = tclassSubMap.get(newTclass.getTclassId());
		} else {
			tclSpList = new ArrayList<TPlDezyTclassSubcomp>();
		}

		for (String subcomp : subcompIds) {
			TPlDezyTclassSubcomp tc = new TPlDezyTclassSubcomp();
			tc.setPlacementId(newTclass.getPlacementId());
			tc.setSchoolId(newTclass.getSchoolId());
			tc.setSubjectCompId(subcomp);
			tc.setTclassId(newTclass.getTclassId());
			tc.setUsedGrade(newTclass.getUsedGrade());

			// tclassSubcompList.add(tc);
			tclSpList.add(tc);
		}
		tclassSubMap.put(newTclass.getTclassId(), tclSpList);
	}

	public static Map<String, Object> deepCopy(Map<String, Object> clsGroup) {
		// TODO Auto-generated method stub
		Map<String, Object> rs = new HashMap<String, Object>();

		// 设置部分
		// 由分班生成 可能会增加教学班的列表
		List<TPlDezyClassgroup> classGroupList = (List<TPlDezyClassgroup>) clsGroup
				.get("classGroupList");
		List<TPlDezyClass> dezyClassList = (List<TPlDezyClass>) clsGroup
				.get("dezyClassList");
		List<TPlDezySubjectcomp> subjectCompList = (List<TPlDezySubjectcomp>) clsGroup
				.get("subjectCompList");
		List<TPlDezyTclassSubcomp> tclassSubcompList = (List<TPlDezyTclassSubcomp>) clsGroup
				.get("tclassSubcompList");

		// 深复制
		List<TPlDezyClassgroup> classGroupListCopy = new ArrayList<TPlDezyClassgroup>();
		List<TPlDezyClass> dezyClassListCopy = new ArrayList<TPlDezyClass>();
		List<TPlDezySubjectcomp> subjectCompListCopy = new ArrayList<TPlDezySubjectcomp>();
		List<TPlDezyTclassSubcomp> tclassSubcompListCopy = new ArrayList<TPlDezyTclassSubcomp>();

		for (TPlDezyClassgroup cg : classGroupList) {
			classGroupListCopy.add(cg.deepCopy());
		}
		for (TPlDezyClass tcl : dezyClassList) {
			dezyClassListCopy.add(tcl.deepCopy());
		}
		if (subjectCompList != null) {

			for (TPlDezySubjectcomp ss : subjectCompList) {
				subjectCompListCopy.add(ss.deepCopy());
			}
		}
		if (tclassSubcompList != null) {

			for (TPlDezyTclassSubcomp ts : tclassSubcompList) {
				tclassSubcompListCopy.add(ts.deepCopy());
			}
		}
		rs.putAll(clsGroup);
		rs.put("classGroupList", classGroupListCopy);
		rs.put("dezyClassList", dezyClassListCopy);
		if (subjectCompListCopy.size() > 0) {

			rs.put("subjectCompList", subjectCompListCopy);
		}
		if (tclassSubcompListCopy.size() > 0) {

			rs.put("tclassSubcompList", tclassSubcompListCopy);
		}
		return rs;
	}

	public static List<List<TPlDezySubjectcompGroup>> convertToGroupList(
			List<TPlDezySubjectcomp> notFinishSubcomps, String subId) {
		// TODO Auto-generated method stub
		HashMap<String, TPlDezySubjectcompGroup> proMap = new HashMap<String, TPlDezySubjectcompGroup>();
		HashMap<String, TPlDezySubjectcompGroup> optMap = new HashMap<String, TPlDezySubjectcompGroup>();
		List<TPlDezySubjectcompGroup> proList = new ArrayList<TPlDezySubjectcompGroup>();
		List<TPlDezySubjectcompGroup> optList = new ArrayList<TPlDezySubjectcompGroup>();

		for (TPlDezySubjectcomp cp : notFinishSubcomps) {
			String[] subFrom = cp.getCompFrom().split(",");
			String classId = cp.getClassId();
			TPlDezySubjectcompGroup g = null;
			boolean isgk = false;
			if (isStrInArr(subId, subFrom)) {
				isgk = true;
			}
			if (isgk) {
				if (optMap.containsKey(classId)) {
					g = optMap.get(classId);
				} else {
					g = new TPlDezySubjectcompGroup();
					g.setClassId(classId);
				}
			} else {
				if (proMap.containsKey(classId)) {
					g = proMap.get(classId);
				} else {
					g = new TPlDezySubjectcompGroup();
					g.setClassId(classId);
				}
			}
			g.addChildComp(cp);
			if (isgk) {
				optMap.put(classId, g);
			} else {
				proMap.put(classId, g);
			}
		}
		optList.addAll(optMap.values());
		proList.addAll(proMap.values());
		List<List<TPlDezySubjectcompGroup>> rs = new ArrayList<List<TPlDezySubjectcompGroup>>();
		rs.add(optList);
		rs.add(proList);
		return rs;
	}

	public static List<TPlDezyClass> getRelatedClassesBySubcomId(
			HashMap<String, TPlDezyClass> xzclassMap,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			List<String> tmpAddArr) {
		// TODO Auto-generated method stub
		List<TPlDezyClass> rs = new ArrayList<TPlDezyClass>();
		for (String cid : tmpAddArr) {
			if (subjectCompMap.containsKey(cid)) {
				TPlDezySubjectcomp scp = subjectCompMap.get(cid);
				String tid = scp.getClassId();
				if (xzclassMap.containsKey(tid)) {
					TPlDezyClass dclass = xzclassMap.get(tid);
					if (!rs.contains(dclass)) {
						rs.add(dclass);
					}
				}
			}
		}
		return rs;
	}

	/**
	 * 
	 * @param dezyClassList
	 * @param tclassSubMap
	 * @param optionTclassMap
	 * @param classMidNum
	 * @param xs
	 *            最大最小系数
	 * @param classNumMin
	 * @param classNumMax
	 * @param subjectCompMap
	 * @param xzclassMap
	 * @param fzgroudList
	 * @param fzgroudMap
	 * @param xzgroudMap
	 * @param kmMcMap
	 * @param subjectSetMap
	 */
	public static int adjustTclassResult(List<TPlDezyClass> dezyClassList,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			int classMidNum, double xs,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			HashMap<String, TPlDezyClass> xzclassMap,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList, Map<String, String> kmMcMap,
			HashMap<String, TPlDezySubjectSet> subjectSetMap, int alg,int xzMaxRs) {

		// TODO Auto-generated method stub
		int classNumMax = (int) Math.ceil(classMidNum * (1 + 0.25));
		
		int classNumMin = (int) Math.ceil(classMidNum * (1 - xs));
		ListSortUtil<TPlDezySubjectcomp> subcompSort = new ListSortUtil<TPlDezySubjectcomp>();

		for (TPlDezyClass tcl : dezyClassList) {
			if (!tclassSubMap.containsKey(tcl.getTclassId())) {
				continue;
			}
			int tclNum = tcl.getTclassNum();
			int tclassNumMax = classNumMax;
			int tclassNumMin = classNumMin;
			int tclassMidNum = classMidNum;
			if(xzMaxRs!=0){
				tclassNumMax = xzMaxRs;
			}
			if (tcl.getTclassLevel() == 1) {
				TPlDezySubjectSet ss = subjectSetMap.get(tcl.getSubjectId());
				if (ss != null && ss.getClassLimit() > 0) {
					tclassMidNum = (int) Math.ceil(ss.getStuNum()
							/ ss.getClassLimit());
					tclassNumMax = (int) Math.ceil(tclassMidNum * (1 + 0.3));
					tclassNumMin = (int) Math.ceil(tclassMidNum * (1 - xs));
				}
			}

			if (tclNum < tclassNumMin) {
				// 凑班
				boolean isFit = false;
				// 无法凑班则拆原有教学班
				String optKey = tcl.getClassGroupId() + "_"
						+ tcl.getSubjectId() + "_" + tcl.getClassSeq() + "_"
						+ tcl.getTclassLevel();
				List<TPlDezyClass> tclList = optionTclassMap.get(optKey);

				if (tclList != null) {
					for (TPlDezyClass rtcl : tclList) {
						if (rtcl.getTclassId().equals(tcl.getTclassId())
								|| rtcl.getIsExpClass() != tcl.getIsExpClass()) {
							continue;
						}
						String tcid = rtcl.getTclassId();
						List<TPlDezyTclassSubcomp> spList = tclassSubMap
								.get(tcid);
						int orTclassNum = rtcl.getTclassNum();

						if (isRsFullfill(tclNum + orTclassNum, tclassNumMin,
								tclassNumMax)) {
							List<TPlDezyTclassSubcomp> lsubs = tclassSubMap
									.get(tcl.getTclassId());
							List<String> lspIds = new ArrayList<String>();
							for (TPlDezyTclassSubcomp lsub : lsubs) {
								lspIds.add(lsub.getSubjectCompId());
							}
							removeOrTclassMutilSp(lspIds, tcl, tclassSubMap);
							genRelationOfTclassSubCompByIds(lspIds, rtcl,
									tclassSubMap);
							removeTclassFromOptMap(optionTclassMap, tcl,
									tclassSubMap);
							rtcl.setTclassNum(orTclassNum + tcl.getTclassNum());
							tcl.setTclassNum(0);
							isFit = true;
							return 0;

						} else {

							for (TPlDezyTclassSubcomp sp : spList) {
								int spNum = subjectCompMap.get(
										sp.getSubjectCompId()).getCompNum();
								if (isRsFullfill(tclNum + spNum, tclassNumMin,
										tclassNumMax)
										&& isRsFullfill(orTclassNum - spNum,
												classNumMin, classNumMax)) {
									List<String> tmpAddArr = new ArrayList<String>();
									tmpAddArr.add(sp.getSubjectCompId());
									// 生成教学班
									genRelationOfTclassSubCompByIds(tmpAddArr,
											tcl, tclassSubMap);
									rtcl.setTclassNum(orTclassNum - spNum);
									tcl.setTclassNum(tcl.getTclassNum() + spNum);
									removeOrTclassSp(sp, rtcl, tclassSubMap);
									isFit = true;
									return 0;
								}
							}

							if (isFit) {
								break;
							}
						}
					}
				}

				//
				if (isFit) {
					break;
				}
			} else if (tclNum > tclassNumMax) {
				// //班额超大 需拆分
				List<TPlDezyTclassSubcomp> tspList = tclassSubMap.get(tcl
						.getTclassId());
				List<TPlDezySubjectcomp> spList = new ArrayList<TPlDezySubjectcomp>();
				if (tspList == null) {
					continue;
				}
				for (TPlDezyTclassSubcomp tsp : tspList) {
					TPlDezySubjectcomp comp = subjectCompMap.get(tsp
							.getSubjectCompId());
					spList.add(comp);
				}
				List<TPlDezySubjectcomp> lspList = new ArrayList<TPlDezySubjectcomp>();
				List<TPlDezySubjectcomp> rspList = new ArrayList<TPlDezySubjectcomp>();
				subcompSort.sort(spList, "compNum", "desc");

				boolean isFit = false;

				int lsum = spList.get(0).getCompNum(), tryTimes = 0;
				lspList.add(spList.get(0));
				int rsum = getRightSpCompNum(spList, lspList, rspList);
				while ((!isRsFullfill(lsum, tclassNumMin, tclassNumMax) || !isRsFullfill(
						rsum, tclassNumMin, tclassNumMax))
						&& tryTimes < spList.size() - 2) {

					tryTimes++;
					TPlDezySubjectcomp addComp = spList.get(spList.size() - 1
							- tryTimes);
					lspList.add(addComp);
					lsum += addComp.getCompNum();
					rsum = getRightSpCompNum(spList, lspList, rspList);
				}
				if (isRsFullfill(lsum, tclassNumMin, tclassNumMax)
						&& isRsFullfill(rsum, tclassNumMin, tclassNumMax)) {
					isFit = true;
					String optKey = tcl.getClassGroupId() + "_"
							+ tcl.getSubjectId() + "_" + tcl.getClassSeq()
							+ "_" + tcl.getTclassLevel();
					List<TPlDezyClass> tclList = optionTclassMap.get(optKey);

					List<String> rspIds = new ArrayList<String>();
					for (TPlDezySubjectcomp rsp : rspList) {
						rspIds.add(rsp.getSubjectCompId());
					}

					// 将切除的组合从班级移除
					removeOrTclassMutilSp(rspIds, tcl, tclassSubMap);
					tcl.setTclassNum(lsum);

					subcompSort.sort(rspList, "compNum", "desc");
					String xzclassId = rspList.get(0).getClassId();
					TPlDezyClass xzclass = xzclassMap.get(xzclassId);
					List<TPlDezyClass> relatedClassIds = new ArrayList<TPlDezyClass>();
					for (TPlDezySubjectcomp rsp : rspList) {
						TPlDezyClass rxzclass = xzclassMap
								.get(rsp.getClassId());
						if (!relatedClassIds.contains(rxzclass)) {
							relatedClassIds.add(rxzclass);
						}
					}
					TPlDezyClass newTclass = genNZtTclassByTclass(tcl,
							tcl.getClassSeq(), tcl.getSubjectId(), xzclass,
							rsum, tcl.getTclassLevel(), xzgroudMap, fzgroudMap,
							fzgroudList, kmMcMap, alg, relatedClassIds);
					tclList.add(newTclass);

					genRelationOfTclassSubCompByIds(rspIds, newTclass,
							tclassSubMap);
					dezyClassList.add(newTclass);
				}
				if (isFit) {
					break;
				}
			}
		}
		// if(true){
		// return;
		// }
		// 优化教学班-选考数量
		// 科目下的选考班级映射
		HashMap<String, List<TPlDezyClass>> subjectOptTclListMap = new HashMap<String, List<TPlDezyClass>>();
		HashMap<String,TPlDezyClass> dezyTclassMap = new HashMap<String, TPlDezyClass>();
		for (TPlDezyClass tcl : dezyClassList) {
			dezyTclassMap.put(tcl.getTclassId(), tcl);
			if (!tclassSubMap.containsKey(tcl.getTclassId())) {
				continue;
			}
			if (tcl.getTclassNum() > 0) {
				List<TPlDezyClass> optlist = null;
				String subLvlKey = tcl.getSubjectId() + "_"
						+ tcl.getTclassLevel();
				if (subjectOptTclListMap.containsKey(subLvlKey)) {
					optlist = subjectOptTclListMap.get(subLvlKey);
				} else {
					optlist = new ArrayList<TPlDezyClass>();
				}
				optlist.add(tcl);
				subjectOptTclListMap.put(subLvlKey, optlist);
			}
		}
		//
		ListSortUtil<TPlDezySubjectcomp> compare = new ListSortUtil<TPlDezySubjectcomp>();
		ListSortUtil<TPlDezyClass> compareTcl = new ListSortUtil<TPlDezyClass>();
		//物理
//		TPlDezySubjectSet ss3 = subjectSetMap.get("7");
//		ss3.setClassLimit(6);
//		ss3.setStuNum(242);
		
		TPlDezySubjectSet ss1 = subjectSetMap.get("5");
		ss1.setClassLimit(6);
		ss1.setStuNum(256);
		

		TPlDezySubjectSet ss2 = new TPlDezySubjectSet();
		ss2.setClassLimit(8);
		ss2.setStuNum(333);
		ss2.setSubjectId("19");
		subjectSetMap.put("19", ss2);

		int rs = 0;
		for (String subKey : subjectOptTclListMap.keySet()) {
			String subId = subKey.split("_")[0];
			int lvl = Integer.parseInt(subKey.split("_")[1]);
			TPlDezySubjectSet ss = subjectSetMap.get(subId);
			if (ss != null && ss.getClassLimit() != 0) {
				List<TPlDezyClass> optList = subjectOptTclListMap.get(subId
						+ "_" + lvl);
				compareTcl.sort(optList, "tclassNum", "asc");
				if (ss.getClassLimit() > 0
						&& ss.getClassLimit() < optList.size()) {
					
					rs -= 100;
					
					int tclassMidNum = (int) Math.ceil(ss.getStuNum()
							/ ss.getClassLimit());
					int tclassNumMax = (int) Math
							.ceil(tclassMidNum * (1 + 0.45));
					if(xzMaxRs!=0){
						tclassNumMax = xzMaxRs;
					}
					int tclassNumMin = (int) Math.ceil(tclassMidNum * (1 - xs));

					int nowBs = optList.size();
					int tryTimes = 0;
					while (nowBs > ss.getClassLimit() && tryTimes < 10) {
						tryTimes++;
						List<TPlDezyClass> needKillTclass = new ArrayList<TPlDezyClass>();
						for (TPlDezyClass dezy : optList) {
							HashMap<String, TPlDezyClass> subcomp2TclassMapTmp = new HashMap<String, TPlDezyClass>();
							List<TPlDezyTclassSubcomp> tspList = tclassSubMap
									.get(dezy.getTclassId());
							List<TPlDezySubjectcomp> spList = new ArrayList<TPlDezySubjectcomp>();
							if (tspList == null) {
								continue;
							}
							
							HashMap<String,TPlDezyTclassSubcomp> tspMap = new HashMap<String, TPlDezyTclassSubcomp>();
							for (TPlDezyTclassSubcomp tsp : tspList) {
								TPlDezySubjectcomp comp = subjectCompMap
										.get(tsp.getSubjectCompId());
								spList.add(comp);
								tspMap.put(tsp.getSubjectCompId(), tsp);
							}
							compare.sort(spList, "compNum", "desc");

							HashMap<String,JSONObject> canSwapComp = new HashMap<String, JSONObject>();
							for (TPlDezyClass oTcl : optList) {
								if (!oTcl.equals(dezy)  ) {
									int orNum = oTcl.getTclassNum();
									for (TPlDezySubjectcomp sp : spList) {
										if (!subcomp2TclassMapTmp.containsKey(sp.getSubjectCompId())) {
											if (isRsFullfill(sp.getCompNum() + orNum, tclassNumMin, tclassNumMax)) {
												if(oTcl.getClassSeq() == dezy.getClassSeq()){
													orNum += sp.getCompNum();
													subcomp2TclassMapTmp.put(sp.getSubjectCompId(),oTcl);
												}else{
//													if(true){
//														continue;
//													}
													//需要跨序列凑班 找到当前已使用序列，看该科目是否可以安排到当前序列
													int nowSeq = dezy.getClassSeq();
													int usedSeq = oTcl.getClassSeq();
													String usedSubId = "";
													for(String ll :sp.getFinishSubTseqMap().keySet()){
														if(sp.getFinishSubTseqMap().get(ll) ==usedSeq){
															usedSubId = ll;
														}
													}
													int ulvl = 1;
													if(!isStrInArr(subId, sp.getCompFrom().split(","))){
														ulvl =2;
													}
													String optKey = oTcl.getClassGroupId() + "_"
															+ usedSubId + "_" + nowSeq + "_" + ulvl;
													List<TPlDezyClass> tclList = optionTclassMap.get(optKey);
													TPlDezyClass ttTclass = null;
													if(tclList==null){
														continue;
													}
													for(TPlDezyClass uTclass:tclList){
														if (isRsFullfill(sp.getCompNum() 
																+ uTclass.getTclassNum(), 
																tclassNumMin, tclassNumMax))
															ttTclass = uTclass;
															break;
													}
													if(ttTclass!=null  ){
														String orTclassId = sp.getFinishSubTclassMap().get(usedSubId);
														TPlDezyClass orTclass = dezyTclassMap.get(orTclassId);
														if(orTclass==null){
															continue;
														}
														//找到可以去别的序列的班级
														JSONObject compObj = new JSONObject();
														canSwapComp.put(sp.getSubjectCompId(), compObj);
														compObj.put("swapSubId", usedSubId);
														compObj.put("fromSeq", usedSeq);
														compObj.put("toSeq", nowSeq);
														compObj.put("fromTclass", orTclass);
														compObj.put("toTclass", ttTclass);
														
														orNum += sp.getCompNum();
														subcomp2TclassMapTmp.put(sp.getSubjectCompId(),oTcl);
//														//转换被使用科目的序列及所在序列的教学班- 
//														 
													}
												}
											} else {
												// 本班人数已满，下一班
												continue;
											}
										}
									}
								}
								// 所有组合已完成
								if (spList.size() == subcomp2TclassMapTmp.size()) {
									break;
								}
							}
							// 本轮循环拆分一个班级
							if (spList.size() == subcomp2TclassMapTmp.size()) {
								needKillTclass.add(dezy);
								// 删除dezy班级
								dezy.setTclassNum(0);
								List<String> lspIds = new ArrayList<String>();
								for (TPlDezySubjectcomp lsub : spList) {
									lspIds.add(lsub.getSubjectCompId());
								}
								removeOrTclassMutilSp(lspIds, dezy,
										tclassSubMap);
								removeTclassFromOptMap(optionTclassMap, dezy,
										tclassSubMap);
								
								for(String compId:canSwapComp.keySet()){
									TPlDezySubjectcomp sp = subjectCompMap.get(compId);
									JSONObject swapj = canSwapComp.get(compId);
									String swapSubId = swapj.getString("swapSubId");
									int fromSeq = swapj.getIntValue("fromSeq");
									int toSeq = swapj.getIntValue("toSeq");
									TPlDezyClass fromTclass = (TPlDezyClass) swapj.get("fromTclass");
									TPlDezyClass toTclass = (TPlDezyClass) swapj.get("toTclass");
									TPlDezyTclassSubcomp tsp = tspMap.get(sp.getSubjectCompId());
									removeOrTclassSp(tsp , fromTclass, tclassSubMap);
									fromTclass.setTclassNum( fromTclass.getTclassNum()- sp.getCompNum() );
									toTclass.setTclassNum(toTclass.getTclassNum() + sp.getCompNum());
									List<String> tmpAddArr = new ArrayList<String>();
									tmpAddArr.add(sp.getSubjectCompId());
//									// 生成教学班
									genRelationOfTclassSubCompByIds(tmpAddArr,toTclass, tclassSubMap);
									sp.getFinishSeqs().remove((Integer) fromSeq);
									genFinishSubCompsExtend(tmpAddArr, subjectCompMap,swapSubId, toSeq, toTclass.getTclassId());
									if(!subcomp2TclassMapTmp.containsKey(compId) ){
										System.out.println("finishOnece");
									}
								}
								
								for (String compId : subcomp2TclassMapTmp
										.keySet()) {
									lspIds = new ArrayList<String>();
									lspIds.add(compId);
									TPlDezyClass rtcl = subcomp2TclassMapTmp
											.get(compId);
									TPlDezySubjectcomp comp = subjectCompMap
											.get(compId);
									if(comp.getCompNum()==1){
										System.out.println("finishOnece");
										
									}
									rtcl.setTclassNum(comp.getCompNum()
											+ rtcl.getTclassNum());
									genRelationOfTclassSubCompByIds(lspIds,
											rtcl, tclassSubMap);
									genFinishSubCompsExtend(lspIds, subjectCompMap,subId, rtcl.getClassSeq(), rtcl.getTclassId());
								}
								
								break;
							}
						}
						dezyClassList.removeAll(needKillTclass);
						optList.removeAll(needKillTclass);
						if (needKillTclass.size() > 0) {

							nowBs--;
							return 0;
						}
					}

				}
			}
		}
		
		return rs;
	}

	private static int getRightSpCompNum(List<TPlDezySubjectcomp> spList,
			List<TPlDezySubjectcomp> lspList, List<TPlDezySubjectcomp> rspList) {
		// TODO Auto-generated method stub
		int rs = 0;
		for (TPlDezySubjectcomp sp : spList) {
			if (!lspList.contains(sp)) {
				if (!rspList.contains(sp)) {

					rspList.add(sp);
					rs += sp.getCompNum();
				}
			}
		}
		return rs;
	}

	public static int getScoreByDezyClassGroup(
			Map<String, Object> settingParams,
			HashMap<String, List<TPlDezySubjectcomp>> classSubcompMap,
			int classMidNum, double xs) {
		// TODO Auto-generated method stub
		List<TPlDezyClass> dezyClassList = (List<TPlDezyClass>) settingParams
				.get("dezyClassList");
		HashMap<String, List<TPlDezyClass>> groupTclassMap = new HashMap<String, List<TPlDezyClass>>();
		for (TPlDezyClass tcl : dezyClassList) {
			String gpid = tcl.getClassGroupId();
			List<TPlDezyClass> tclList = null;
			if (groupTclassMap.containsKey(gpid)) {
				tclList = groupTclassMap.get(gpid);
			} else {
				tclList = new ArrayList<TPlDezyClass>();
			}
			tclList.add(tcl);
			groupTclassMap.put(gpid, tclList);
		}
		List<TPlDezySubjectSet> subjectSetList = (List<TPlDezySubjectSet>) settingParams
				.get("subjectSetList");
		HashMap<String, Integer> subjectSetMap = new HashMap<String, Integer>();
		for (TPlDezySubjectSet ss : subjectSetList) {
			String subId = ss.getSubjectId();
			if (ss.getIsProExist() == 1) {
				subjectSetMap.put(subId, 2);
			} else {
				subjectSetMap.put(subId, 1);
			}
		}
		int score = 100;
		for (List<TPlDezyClass> tclList : groupTclassMap.values()) {

			for (String subId : subjectSetMap.keySet()) {
				boolean isProExist = subjectSetMap.get(subId) == 2;
				int sum = 0;
				int fsum = 0;

				for (TPlDezyClass tcl : tclList) {
					List<TPlDezySubjectcomp> spList = classSubcompMap.get(tcl
							.getTclassId());
					if (spList == null) {
						continue;
					}
					for (TPlDezySubjectcomp sp : spList) {
						if (sp.getCompFrom() == null) {
							continue;
						}
						String[] spSubs = sp.getCompFrom().split(",");
						int num = sp.getCompNum();
						if (isStrInArr(subId, spSubs)) {
							sum += num;
						} else {
							fsum += num;
						}
					}

					if (sum < classMidNum * xs) {
						score -= 1;
					}
					if (sum < 10) {
						score -= 5;
					}
					if (isProExist) {
						if (fsum < classMidNum * xs) {
							score -= 1;
						}
						if (fsum < 10) {
							score -= 5;
						}
					}
				}

			}
		}

		return score;
	}

	public static HashMap<String, List<TPlDezySubjectcomp>> convertToClassSpMap(
			List<TPlDezyTclassSubcomp> tclassSubcompList,
			List<TPlDezySubjectcomp> subjectCompList) {
		// TODO Auto-generated method stub

		// 志愿组合map
		HashMap<String, TPlDezySubjectcomp> subjectCompMap = new HashMap<String, TPlDezySubjectcomp>();
		// 志愿的行政班map
		for (TPlDezySubjectcomp subcomp : subjectCompList) {
			subjectCompMap.put(subcomp.getSubjectCompId(), subcomp);
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
		return classSubcompMap;
	}

	public static int divTclassNoneCross(
			List<TPlDezySubjectgroup> subjectGroupList,
			List<TPlDezyClassgroup> classGroupList,
			List<TPlDezyClass> dezyClassList,
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			List<TPlDezyTclassfrom> tclassFromList, int classNumMax,
			int classNumMin, int placeAlgMethod, Map<String, String> kmMcMap,
			HashMap<String, TPlDezySubjectSet> subjectSetMap,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			HashMap<String, List<TPlDezySubjectcomp>> classSubcompMap,
			ListSortUtil<TPlDezySubjectcomp> compSort,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList,
			HashMap<String, TPlDezyClass> xzclassMap) {
		int unfinishSubs = 0;
		int maxRepeat = (int) Math.ceil((classGroupList.size() / 3));

		// 开教学班
		HashMap<String, TPlDezyClassgroup> cgMap = new HashMap<String, TPlDezyClassgroup>();
		for (TPlDezyClassgroup classGroup : classGroupList) {
			cgMap.put(classGroup.getClasstGroupId(), classGroup);
		}
		HashMap<String, String> isSubCompFinish = new HashMap<String, String>();
		int loops = 0;
		for (TPlDezyClassgroup classGroup : cgMap.values()) {
			String classIds = classGroup.getClassIds();
			String[] classArr = classIds.split(",");
			// 获取所有志愿组合
			List<TPlDezySubjectcomp> groupSubcompList = new ArrayList<TPlDezySubjectcomp>();
			for (int i = 0; i < classArr.length; i++) {
				String cid = classArr[i];
				if (classSubcompMap.containsKey(cid)) {
					groupSubcompList.addAll(classSubcompMap.get(cid));
				}
			}
			// 开文班或理班
			for (TPlDezySubjectgroup subGroup : subjectGroupList) {
				String[] subIdArr = subGroup.getSubjectIds().split(",");
				if (subIdArr.length < 3) {
					throw new CommonRunException(-2, "科目组内科目数不足，需重新检查科目组设置！");
				}
				// 有序科目顺序
				List<String> subIds = new ArrayList<String>();
				if (loops % 3 == 0) {
					// 1,2,3
					subIds.add(subIdArr[0]);
					subIds.add(subIdArr[1]);
					subIds.add(subIdArr[2]);
				} else if (loops % 3 == 1) {
					// 3,1,2
					subIds.add(subIdArr[2]);
					subIds.add(subIdArr[0]);
					subIds.add(subIdArr[1]);
				} else if (loops % 3 == 2) {
					// 2,3,1
					subIds.add(subIdArr[1]);
					subIds.add(subIdArr[2]);
					subIds.add(subIdArr[0]);
				}
				// 行政班组内学选同时开
				for (int seq = 1; seq < 4; seq++) {
					String subId = subIds.get((seq - 1));
					TPlDezySubjectSet subset = subjectSetMap.get(subId);
					boolean isLearnExist = subset.getIsProExist() > 0;

					// 组内单序列未完成的组合---单独算法进行凑班
					List<TPlDezySubjectcomp> notFinishSubcomps = new ArrayList<TPlDezySubjectcomp>();

					for (int k = 0; k < classArr.length; k++) {
						String tcId = classArr[k];
						TPlDezyClass xzclass = xzclassMap.get(tcId);
						List<TPlDezySubjectcomp> tcSubcomps = classSubcompMap
								.get(tcId);
						List<TPlDezySubjectcomp> inArrSubcomps = new ArrayList<TPlDezySubjectcomp>();
						List<TPlDezySubjectcomp> notInArrSubcomps = new ArrayList<TPlDezySubjectcomp>();
						int sumrs = 0;
						int fsum = 0;

						for (TPlDezySubjectcomp tcSubcomp : tcSubcomps) {
							String[] subarr = tcSubcomp.getCompFrom()
									.split(",");
							if (TmpUtil.isStrInArr(subId, subarr)) {
								sumrs += tcSubcomp.getCompNum();
								inArrSubcomps.add(tcSubcomp);
							} else {
								fsum += tcSubcomp.getCompNum();
								notInArrSubcomps.add(tcSubcomp);
							}
						}
						if (sumrs >= classNumMin && sumrs <= classNumMax) {
							// 符合开教学班条件 则开选考班
							TPlDezyClass newTclass = TmpUtil.genZtTclass(
									classGroup, subGroup, seq, subId, xzclass,
									sumrs, 1, xzgroudMap, kmMcMap,
									placeAlgMethod);
							TmpUtil.genRelationOfTclassSubComp(inArrSubcomps,
									newTclass, tclassSubMap);
							notFinishSubcomps.addAll(notInArrSubcomps);
							dezyClassList.add(newTclass);
							TmpUtil.addTclassToOptMap(optionTclassMap,
									newTclass);

						} else if (fsum >= classNumMin && fsum <= classNumMax
								&& isLearnExist) {
							// 符合开教学班条件 则开学考班
							TPlDezyClass newTclass = TmpUtil.genZtTclass(
									classGroup, subGroup, seq, subId, xzclass,
									fsum, 2, xzgroudMap, kmMcMap,
									placeAlgMethod);

							TmpUtil.genRelationOfTclassSubComp(
									notInArrSubcomps, newTclass, tclassSubMap);

							notFinishSubcomps.addAll(inArrSubcomps);
							dezyClassList.add(newTclass);
							TmpUtil.addTclassToOptMap(optionTclassMap,
									newTclass);
						} else {
							notFinishSubcomps.addAll(inArrSubcomps);
							if (isLearnExist) {
								notFinishSubcomps.addAll(notInArrSubcomps);
							}
						}
					}

					unfinishSubs += TmpUtil.genTclassByUnfinishComps(
							tclassFromList, classNumMax, classNumMin,
							subjectCompMap, compSort, xzgroudMap, fzgroudMap,
							fzgroudList, xzclassMap, classGroup, subGroup, seq,
							subId, notFinishSubcomps, dezyClassList,
							unfinishSubs, tclassSubMap, kmMcMap,
							placeAlgMethod, optionTclassMap, 1);
				}
			}

			loops++;
		}
		return unfinishSubs;
	}

	public static int divTclassCrossGroup(TPlDezySettings dezySetting,
			List<TPlDezySubjectSet> subjectSetList,
			List<TPlDezyClassgroup> classGroupList,
			List<TPlDezyClass> dezyClassList,
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			List<TPlDezyTclassfrom> tclassFromList, int classNumMax,
			int classNumMin, int placeAlgMethod, Map<String, String> kmMcMap,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			HashMap<String, List<TPlDezySubjectcomp>> classSubcompMap,
			ListSortUtil<TPlDezySubjectcomp> compSort,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList,
			HashMap<String, TPlDezyClass> xzclassMap,
			HashMap<String, TPlDezySubjectgroup> subject2GroupMap,
			int placementAlgType, int isTechExist) {
		int classMidNum = (classNumMax + classNumMin) / 2;
		int unfinishSubs = 0;
		int minTeaNum = dezySetting.getMinTeacherNum();
		// 跨组生成教学班
		TPlDezyClassgroup classGroup = classGroupList.get(0);
		HashMap<String, Integer> subSeqNum = new HashMap<String, Integer>();
		// 未完成的选考志愿组合
		HashMap<String, List<TPlDezySubjectcomp>> unfinishOptCompsMap = new HashMap<String, List<TPlDezySubjectcomp>>();
		// 未完成的学考志愿组合--全部志愿组合
		HashMap<String, List<TPlDezySubjectcomp>> unfinishProCompsMap = new HashMap<String, List<TPlDezySubjectcomp>>();
		// 记录学考班科目
		List<String> learnSubs = new ArrayList<String>();
		List<String> selSubs = new ArrayList<String>();
		// 记录学选考科目
		for (TPlDezySubjectSet subSet : subjectSetList) {
			String subId = subSet.getSubjectId();
			boolean isLearnExist = subSet.getIsProExist() == 1;
			if (isLearnExist) {
				learnSubs.add(subId);
			}
			selSubs.add(subId);
		}
		//学考科目排序--难排的在前面
		Collections.shuffle(learnSubs);
		boolean isTechOptExist = selSubs.contains("19");
		boolean isTechProExist = learnSubs.contains("19");
		// 初始化各组合配置
		for (TPlDezyClass xzclass : xzclassMap.values()) {
			if (xzclass.getTclassType() == 6) {
				// 获取最多的两门科目 开出选考班，并记录未完成的选考志愿组合
				List<TPlDezySubjectcomp> allSubcomps = classSubcompMap
						.get(xzclass.getTclassId());

				for (TPlDezySubjectcomp subcomp : allSubcomps) {
					String[] optSubArr = subcomp.getCompFrom().split(",");
					// 设置未完成的选考科目/学考科目
					List<String> unfinishOptSubs = new ArrayList<String>();
					List<String> unfinishProSubs = new ArrayList<String>();
					for (String sub : optSubArr) {
						unfinishOptSubs.add(sub);
					}

					for (String lsub : learnSubs) {
						if (!unfinishOptSubs.contains(lsub)) {
							unfinishProSubs.add(lsub);
						}
					}
					subcomp.setUnfinishOptSubTasks(unfinishOptSubs);
					subcomp.setUnfinishProSubTasks(unfinishProSubs);
					subcomp.getFinishSubTseqMap().clear();
					subcomp.getFinishSeqs().clear();
				}
			}
		}
		if (placementAlgType == 1) {
			// 浒山版本
			genFixTwoTclass(dezyClassList, optionTclassMap, tclassSubMap,
					placeAlgMethod, kmMcMap, subjectCompMap, classSubcompMap,
					xzgroudMap, fzgroudMap, fzgroudList, xzclassMap,
					subject2GroupMap, minTeaNum, classGroup, subSeqNum,
					unfinishOptCompsMap, unfinishProCompsMap, learnSubs, 1,
					isTechExist, isTechOptExist);
			// 处理剩余选考
			// 补充定二部分中未完成的组合
			dealWithFixTwoUnfinishSubComps(optionTclassMap, tclassSubMap,
					classMidNum, classGroup, unfinishOptCompsMap, 1, 1, true,
					false, subjectCompMap);
			// 处理走一部分班级
			for (String subId : unfinishOptCompsMap.keySet()) {
				List<TPlDezySubjectcomp> notFinishSubcomps = unfinishOptCompsMap
						.get(subId);
				if (notFinishSubcomps.size() > 0) {

					unfinishSubs += TmpUtil.genTclassByUnfinishComps(
							tclassFromList, classNumMax, classNumMin,
							subjectCompMap, compSort, xzgroudMap, fzgroudMap,
							fzgroudList, xzclassMap, classGroup,
							subject2GroupMap.get(subId), 3, subId,
							notFinishSubcomps, dezyClassList, unfinishSubs,
							tclassSubMap, kmMcMap, placeAlgMethod,
							optionTclassMap, 1);
				}
			}
			// 处理剩余学考
			for (String subId : unfinishProCompsMap.keySet()) {
				List<TPlDezySubjectcomp> notFinishSubcomps = unfinishProCompsMap
						.get(subId);
				unfinishSubs += TmpUtil.genTclassByUnfinishComps(
						tclassFromList, classNumMax, classNumMin,
						subjectCompMap, compSort, xzgroudMap, fzgroudMap,
						fzgroudList, xzclassMap, classGroup,
						subject2GroupMap.get(subId), 4, subId,
						notFinishSubcomps, dezyClassList, unfinishSubs,
						tclassSubMap, kmMcMap, placeAlgMethod, optionTclassMap,
						1);
			}
		} else if (placementAlgType == 2) {
			// 先处理选考
			genFixTwoTclass(dezyClassList, optionTclassMap, tclassSubMap,
					placeAlgMethod, kmMcMap, subjectCompMap, classSubcompMap,
					xzgroudMap, fzgroudMap, fzgroudList, xzclassMap,
					subject2GroupMap, minTeaNum, classGroup, subSeqNum,
					unfinishOptCompsMap, unfinishProCompsMap, learnSubs, 1,
					isTechExist, isTechOptExist);
			// 补充定二部分中未完成的组合---选考无技术时 选了技术的组合应保证在1,2序列上选考课
			dealWithFixTwoUnfinishSubComps(optionTclassMap, tclassSubMap,
					classMidNum, classGroup, unfinishOptCompsMap, isTechExist,
					1, isTechOptExist, isTechProExist, subjectCompMap);
			// 七选三时 单独处理技术
			boolean qxs = false;
			if (learnSubs.size() > 3 && isTechExist == 1 && isTechOptExist) {
				qxs = true;
			}
			// 处理走一部分班级 选考
			for (String subId : unfinishOptCompsMap.keySet()) {
				List<TPlDezySubjectcomp> notFinishSubcomps = unfinishOptCompsMap
						.get(subId);
				List<TPlDezySubjectcomp> needRemove = new ArrayList<TPlDezySubjectcomp>();
				for (TPlDezySubjectcomp sp : notFinishSubcomps) {
					if (!sp.getUnfinishOptSubTasks().contains(subId)) {
						needRemove.add(sp);
					}
				}
				if (notFinishSubcomps.size() > 0) {

					unfinishSubs += TmpUtil.genTclassByUnfinishComps(
							tclassFromList, classNumMax, classNumMin,
							subjectCompMap, compSort, xzgroudMap, fzgroudMap,
							fzgroudList, xzclassMap, classGroup,
							subject2GroupMap.get(subId), 3, subId,
							notFinishSubcomps, dezyClassList, unfinishSubs,
							tclassSubMap, kmMcMap, placeAlgMethod,
							optionTclassMap, 2);
				}
				if (unfinishSubs > 0) {
					// 处理剩余学考人
				}
			}
			// 处理舟山版本学考
			// 七选三 且 技术不参与走班 第四科单独开班
			if (isTechExist == 1 && !isTechProExist
					&& unfinishProCompsMap.keySet().size() > 3) {
				genFourthLernTclass(unfinishProCompsMap, learnSubs,
						tclassFromList, classNumMax, classNumMin,
						subjectCompMap, compSort, xzgroudMap, fzgroudMap,
						fzgroudList, xzclassMap, classGroup, dezyClassList,
						unfinishSubs, tclassSubMap, kmMcMap, placeAlgMethod,
						optionTclassMap, subject2GroupMap);
			}
			// 开始定二部分分班
			genFixTwoTclassPro(dezyClassList, optionTclassMap, tclassSubMap,
					placeAlgMethod, kmMcMap, subjectCompMap, classSubcompMap,
					xzgroudMap, fzgroudMap, fzgroudList, xzclassMap,
					subject2GroupMap, minTeaNum, classGroup, subSeqNum,
					unfinishOptCompsMap, unfinishProCompsMap, learnSubs, 2,
					isTechExist, isTechProExist);
			// 补充定二部分中未完成的学考组合--- 选考有技术时 选了技术的组合应保证在4,5序列上学考课8序列上第四科的学考
			dealWithFixTwoUnfinishSubCompsPro(optionTclassMap, tclassSubMap,
					classMidNum, classGroup, unfinishProCompsMap, isTechExist,
					2, isTechOptExist, isTechProExist, learnSubs,
					subjectCompMap);
			// 剩余走一的学考
			int zclaSeq = 6;
			if (learnSubs.size() > 3 && isTechExist == 1 && isTechOptExist) {
				zclaSeq = 7;
			}
			for (String subId : unfinishProCompsMap.keySet()) {
				List<TPlDezySubjectcomp> notFinishSubcomps = unfinishProCompsMap
						.get(subId);
				List<TPlDezySubjectcomp> needRemove = new ArrayList<TPlDezySubjectcomp>();
				for (TPlDezySubjectcomp sp : notFinishSubcomps) {
					if (!sp.getUnfinishProSubTasks().contains(subId)
							|| sp.getFinishSubTseqMap().containsKey(subId)) {
						needRemove.add(sp);
					}
				}
				// notFinishSubcomps.removeAll(needRemove);
				List<TPlDezySubjectcomp> copy = new ArrayList<TPlDezySubjectcomp>();
				for (TPlDezySubjectcomp not : notFinishSubcomps) {
					if (!copy.contains(not)
							&& !not.getFinishSeqs().contains((Integer) zclaSeq)
							&& !needRemove.contains(not)) {
						copy.add(not);
					}
				}
				if (copy.size() > 0) {
					unfinishSubs += TmpUtil.genTclassByUnfinishComps(
							tclassFromList, classNumMax, classNumMin,
							subjectCompMap, compSort, xzgroudMap, fzgroudMap,
							fzgroudList, xzclassMap, classGroup,
							subject2GroupMap.get(subId), zclaSeq, subId, copy,
							dezyClassList, unfinishSubs, tclassSubMap, kmMcMap,
							placeAlgMethod, optionTclassMap, 3);
				}
			}
			for (int seq = 4; seq < zclaSeq; seq++) {
				for (String subId : unfinishProCompsMap.keySet()) {
					List<TPlDezySubjectcomp> notFinishSubcomps = unfinishProCompsMap
							.get(subId);
					List<TPlDezySubjectcomp> needRemove = new ArrayList<TPlDezySubjectcomp>();
					for (TPlDezySubjectcomp sp : notFinishSubcomps) {
						if (!sp.getUnfinishProSubTasks().contains(subId)
								|| sp.getFinishSubTseqMap().containsKey(subId)) {
							needRemove.add(sp);
						}
					}
					List<TPlDezySubjectcomp> copy = new ArrayList<TPlDezySubjectcomp>();
					for (TPlDezySubjectcomp not : notFinishSubcomps) {
						if (!copy.contains(not)
								&& !not.getFinishSeqs().contains((Integer) seq)
								&& !needRemove.contains(not)) {
							copy.add(not);
						}
					}
					if (copy.size() > 0) {
						unfinishSubs += TmpUtil.genTclassByUnfinishComps(
								tclassFromList, classNumMax, classNumMin,
								subjectCompMap, compSort, xzgroudMap,
								fzgroudMap, fzgroudList, xzclassMap,
								classGroup, subject2GroupMap.get(subId), seq,
								subId, copy, dezyClassList, unfinishSubs,
								tclassSubMap, kmMcMap, placeAlgMethod,
								optionTclassMap, 3);
					}
				}
			}
			// //交换序列
			for (int seq = 4; seq <= zclaSeq; seq++) {
				for (String subId : unfinishProCompsMap.keySet()) {
					List<TPlDezySubjectcomp> notFinishSubcomps = unfinishProCompsMap
							.get(subId);
					List<TPlDezySubjectcomp> needRemove = new ArrayList<TPlDezySubjectcomp>();
					for (TPlDezySubjectcomp sp : notFinishSubcomps) {
						if (!sp.getUnfinishProSubTasks().contains(subId)
								|| sp.getFinishSubTseqMap().containsKey(subId)) {
							needRemove.add(sp);
						}
					}
					List<TPlDezySubjectcomp> copy = new ArrayList<TPlDezySubjectcomp>();
					for (TPlDezySubjectcomp not : notFinishSubcomps) {
						if (!copy.contains(not)
								&& !not.getFinishSeqs().contains((Integer) seq)
								&& !needRemove.contains(not)) {
							copy.add(not);
						}
					}
					if (copy.size() > 0) {
						for (TPlDezySubjectcomp comp : copy) {
							// 找不到该序列相应学考班级 则寻找其它序列
							boolean finish = false;
							TPlDezyClass addTclNow = null;
							TPlDezyClass chaTclNow = null;
							TPlDezyClass rmclNow = null;
							for (String loopsub : comp.getFinishSubTseqMap()
									.keySet()) {
								int oseq = comp.getFinishSubTseqMap().get(
										loopsub);
								if (oseq > 3 && oseq != seq && oseq != 8) {
									String nowOptKey = classGroup
											.getClasstGroupId()
											+ "_"
											+ loopsub
											+ "_" + seq + "_" + 2;
									String rmOptKey = classGroup
											.getClasstGroupId()
											+ "_"
											+ loopsub
											+ "_" + oseq + "_" + 2;
									List<TPlDezyClass> rmTclList = optionTclassMap
											.get(rmOptKey);
									boolean find = false;
									if (rmTclList == null) {
										continue;
									}
									for (TPlDezyClass rm : rmTclList) {
										List<TPlDezyTclassSubcomp> tclsubs = tclassSubMap
												.get(rm.getTclassId());
										for (TPlDezyTclassSubcomp tcsp : tclsubs) {
											if (tcsp.getSubjectCompId().equals(
													comp.getSubjectCompId())) {
												rmclNow = rm;
												find = true;
												break;
											}
											if (find) {
												break;
											}
										}
									}
									List<TPlDezyClass> nowTclList = optionTclassMap
											.get(nowOptKey);
									if (nowTclList != null) {

										for (TPlDezyClass tcl : nowTclList) {
											if (isRsFullfill(tcl.getTclassNum()
													+ comp.getCompNum(),
													classMidNum / 2,
													classMidNum * 2)) {

												String fOptKey = classGroup
														.getClasstGroupId()
														+ "_"
														+ subId
														+ "_"
														+ oseq + "_" + 2;
												List<TPlDezyClass> fTclList = optionTclassMap
														.get(fOptKey);
												if (fTclList != null) {

													for (TPlDezyClass otcl : fTclList) {
														if (isRsFullfill(
																otcl.getTclassNum()
																		+ comp.getCompNum(),
																classMidNum / 2,
																classMidNum * 2)) {
															addTclNow = otcl;
															chaTclNow = tcl;
															finish = true;
															break;
														}
													}
												}
											}
											if (finish) {
												break;
											}
										}
									}
								}
								if (finish) {
									// 将完成的班级加好
									String compId = comp.getSubjectCompId();
									int compRs = comp.getCompNum();
									List<String> lspIds = new ArrayList<String>();
									lspIds.add(compId);
									// rm被移除班级移除组合
									rmclNow.setTclassNum(rmclNow.getTclassNum()
											- compRs);
									removeOrTclassMutilSp(lspIds, rmclNow,
											tclassSubMap);
									// 调换序列增加
									genRelationOfTclassSubCompByIds(lspIds,
											addTclNow, tclassSubMap);
									genFinishSubCompsExtend(lspIds,
											subjectCompMap, loopsub, seq,addTclNow.getTclassId());
									addTclNow.setTclassNum(addTclNow
											.getTclassNum() + compRs);
									// 被调换序列增加
									genRelationOfTclassSubCompByIds(lspIds,
											chaTclNow, tclassSubMap);
									genFinishSubCompsExtend(lspIds,
											subjectCompMap, subId, oseq,chaTclNow.getTclassId());
									chaTclNow.setTclassNum(chaTclNow
											.getTclassNum() + compRs);
									unfinishSubs--;
									break;
								}
							}
						}
					}
				}
			}
		}
		unfinishSubs = 0;
		for (String subId : unfinishProCompsMap.keySet()) {
			List<TPlDezySubjectcomp> notFinishSubcomps = unfinishProCompsMap
					.get(subId);
			for (TPlDezySubjectcomp comp : notFinishSubcomps) {
				if (comp.getUnfinishProSubTasks().contains(subId)) {
					unfinishSubs++;
				}
			}
		}
		for (String subId : unfinishOptCompsMap.keySet()) {
			List<TPlDezySubjectcomp> notFinishSubcomps = unfinishOptCompsMap
					.get(subId);
			for (TPlDezySubjectcomp comp : notFinishSubcomps) {
				if (comp.getUnfinishOptSubTasks().contains(subId)) {
					unfinishSubs++;
				}
			}
		}
		return unfinishSubs;
	}

	public static void dealWithFixTwoUnfinishSubComps(
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			int classMidNum, TPlDezyClassgroup classGroup,
			HashMap<String, List<TPlDezySubjectcomp>> unfinishOptCompsMap,
			int genType, int isTechExist, boolean isTechOptExist,
			boolean isTechProExist,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap) {
		List<String> finishTasks = new ArrayList<String>();
		for (String subId : unfinishOptCompsMap.keySet()) {
			List<TPlDezySubjectcomp> notFinishSubcomps = unfinishOptCompsMap
					.get(subId);
			for (TPlDezySubjectcomp subcomp : notFinishSubcomps) {
				List<String> finishSubs = new ArrayList<String>();
				int loop = 0;
				List<String> tplist = new ArrayList<String>();
				tplist.addAll(subcomp.getUnfinishOptSubTasks());
				int cz = subcomp.getUnfinishOptSubTasks().size()
						- finishSubs.size();
				while (cz > 1 && loop < 10) {
					loop++;
					for (String usub : tplist) {
						if (finishSubs.contains(usub)) {
							continue;
						}
						boolean sarr = false;
						if (!subcomp.getFinishSeqs().contains((Integer) 2)) {
							sarr = isAddToExistSuc(optionTclassMap,
									tclassSubMap, classMidNum, classGroup,
									subcomp, usub, 2, 1, loop, subjectCompMap);
							if (sarr) {
								subcomp.getFinishSeqs().add(2);
								finishSubs.add(usub);

								finishTasks.add(usub + ","
										+ subcomp.getSubjectCompId());
							}
						}
						if (!sarr) {
							if (!subcomp.getFinishSeqs().contains((Integer) 1)) {
								sarr = isAddToExistSuc(optionTclassMap,
										tclassSubMap, classMidNum, classGroup,
										subcomp, usub, 1, 1, loop,
										subjectCompMap);
								if (sarr) {
									subcomp.getFinishSeqs().add(1);
									finishSubs.add(usub);
									finishTasks.add(usub + ","
											+ subcomp.getSubjectCompId());
								}
							}
						}
					}
				}
				subcomp.getUnfinishOptSubTasks().removeAll(finishSubs);
			}
		}
		for (String subId : unfinishOptCompsMap.keySet()) {
			List<TPlDezySubjectcomp> notFinishSubcomps = unfinishOptCompsMap
					.get(subId);
			List<TPlDezySubjectcomp> needRemove = new ArrayList<TPlDezySubjectcomp>();
			for (TPlDezySubjectcomp subcomp : notFinishSubcomps) {
				String taskKey = subId + "," + subcomp.getSubjectCompId();
				if (finishTasks.contains(taskKey)) {
					needRemove.add(subcomp);
				}
				if (!subcomp.getUnfinishOptSubTasks().contains(subId)) {
					needRemove.add(subcomp);
				}
			}
			notFinishSubcomps.removeAll(needRemove);
		}
	}

	// 学考
	// 七选三 且 技术不参与走班时 选考了技术的需要上第四门学考课
	// 七选三 且 技术参与走班时 全员走四门学考
	public static void dealWithFixTwoUnfinishSubCompsPro(
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			int classMidNum, TPlDezyClassgroup classGroup,
			HashMap<String, List<TPlDezySubjectcomp>> unfinishProCompsMap,
			int genType, int isTechExist, boolean isTechOptExist,
			boolean isTechProExist, List<String> learnSubs,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap) {
		List<String> finishTasks = new ArrayList<String>();

		for (String subId : unfinishProCompsMap.keySet()) {
			List<TPlDezySubjectcomp> notFinishSubcomps = unfinishProCompsMap
					.get(subId);
			for (TPlDezySubjectcomp subcomp : notFinishSubcomps) {
				List<String> finishSubs = new ArrayList<String>();
				int loop = 0;

				if (subcomp.getFinishSubTseqMap().containsKey(subId)) {
					continue;
				}
				boolean sarr = false;
				if (!subcomp.getFinishSeqs().contains((Integer) 4)) {
					sarr = isAddToExistSuc(optionTclassMap, tclassSubMap,
							classMidNum, classGroup, subcomp, subId, 4, 2,
							loop, subjectCompMap);
					if (sarr) {
						subcomp.getFinishSeqs().add((Integer) 4);
						finishSubs.add(subId);
						subcomp.getFinishSubTseqMap().put(subId, 4);
						finishTasks.add(subId + ","
								+ subcomp.getSubjectCompId());
					}
				}
				if (!sarr) {
					if (!subcomp.getFinishSeqs().contains((Integer) 5)) {
						sarr = isAddToExistSuc(optionTclassMap, tclassSubMap,
								classMidNum, classGroup, subcomp, subId, 5, 2,
								loop, subjectCompMap);
						if (sarr) {
							subcomp.getFinishSeqs().add(5);
							subcomp.getFinishSubTseqMap().put(subId, 5);
							finishSubs.add(subId);
							finishTasks.add(subId + ","
									+ subcomp.getSubjectCompId());
						}
					}
				}
				if (!sarr && learnSubs.size() > 3 && isTechExist == 1
						&& isTechOptExist) {
					if (!subcomp.getFinishSeqs().contains((Integer) 6)) {
						sarr = isAddToExistSuc(optionTclassMap, tclassSubMap,
								classMidNum, classGroup, subcomp, subId, 6, 2,
								loop, subjectCompMap);
						if (sarr) {
							subcomp.getFinishSeqs().add(6);
							subcomp.getFinishSubTseqMap().put(subId, 6);
							finishSubs.add(subId);
							finishTasks.add(subId + ","
									+ subcomp.getSubjectCompId());
						}
					}
				}
				subcomp.getUnfinishProSubTasks().removeAll(finishSubs);
			}
		}
		for (String subId : unfinishProCompsMap.keySet()) {
			List<TPlDezySubjectcomp> notFinishSubcomps = unfinishProCompsMap
					.get(subId);
			List<TPlDezySubjectcomp> needRemove = new ArrayList<TPlDezySubjectcomp>();
			for (TPlDezySubjectcomp subcomp : notFinishSubcomps) {
				String taskKey = subId + "," + subcomp.getSubjectCompId();
				if (finishTasks.contains(taskKey)) {
					needRemove.add(subcomp);
				}
			}
			notFinishSubcomps.removeAll(needRemove);
		}
	}

	// 生成序列为8的班级
	public static void genFourthLernTclass(
			HashMap<String, List<TPlDezySubjectcomp>> unfinishProCompsMap,
			List<String> learnSubs, List<TPlDezyTclassfrom> tclassFromList,
			int classNumMax, int classNumMin,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			ListSortUtil<TPlDezySubjectcomp> compSort,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList,
			HashMap<String, TPlDezyClass> xzclassMap,
			TPlDezyClassgroup classGroup, List<TPlDezyClass> dezyClassList,
			int unfinishSubs,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			Map<String, String> kmMcMap, int placeAlgMethod,
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			HashMap<String, TPlDezySubjectgroup> subject2GroupMap) {
		int sum = 0;
		List<TPlDezySubjectcomp> fourthProSubcomps = new ArrayList<TPlDezySubjectcomp>();
		List<TPlDezySubjectcomp> finishFourthProSubcomps = new ArrayList<TPlDezySubjectcomp>();
		TPlDezySubjectcomp leastRsComp = null;
		for (String subId : unfinishProCompsMap.keySet()) {
			List<TPlDezySubjectcomp> notFinishSubcomps = unfinishProCompsMap
					.get(subId);
			for (TPlDezySubjectcomp subcomp : notFinishSubcomps) {
				String[] optSubs = subcomp.getCompFrom().split(",");
				if (isStrInArr("19", optSubs)) {
					if (!fourthProSubcomps.contains(subcomp)) {
						fourthProSubcomps.add(subcomp);
						sum += subcomp.getCompNum();
					}
					if (leastRsComp != null) {
						if (leastRsComp.getCompNum() > subcomp.getCompNum()) {
							leastRsComp = subcomp;
						}
					} else {
						leastRsComp = subcomp;
					}
				}
			}
		}
		int finishSum = 0;
		int loopTimes = 0;
		HashMap<String, List<TPlDezySubjectcomp>> subComps = new HashMap<String, List<TPlDezySubjectcomp>>();
		while (finishSum < sum && loopTimes < 8) {
			loopTimes++;
			List<TPlDezySubjectcomp> tempFinish = new ArrayList<TPlDezySubjectcomp>();
			int max = 0;
			String bsub = null;
			for (String lsub : leastRsComp.getUnfinishProSubTasks()) {
				// for(String lsub:learnSubs){
				if (subComps.containsKey(lsub)) {
					continue;
				}
				int tmp = 0;
				List<TPlDezySubjectcomp> templist = new ArrayList<TPlDezySubjectcomp>();
				for (TPlDezySubjectcomp sp : fourthProSubcomps) {
					if (sp.getUnfinishProSubTasks().contains(lsub)
							&& !finishFourthProSubcomps.contains(sp)) {
						tmp += sp.getCompNum();
						templist.add(sp);
					}
				}
				if (tmp > max) {
					max = tmp;
					tempFinish = templist;

					bsub = lsub;
				}
			}
			subComps.put(bsub, tempFinish);
			finishFourthProSubcomps.addAll(tempFinish);
			finishSum += max;
		}
		subComps = getBestDistrute(leastRsComp, learnSubs, fourthProSubcomps,
				classNumMax, classNumMin, subComps);
		for (String subId : subComps.keySet()) {
			List<TPlDezySubjectcomp> unfinishTemp = subComps.get(subId);
			genTclassByUnfinishComps(tclassFromList, classNumMax, classNumMin,
					subjectCompMap, compSort, xzgroudMap, fzgroudMap,
					fzgroudList, xzclassMap, classGroup,
					subject2GroupMap.get(subId), 8, subId, unfinishTemp,
					dezyClassList, 0, tclassSubMap, kmMcMap, 2,
					optionTclassMap, 3);

		}
	}

	private static HashMap<String, List<TPlDezySubjectcomp>> getBestDistrute(
			TPlDezySubjectcomp leastRsComp, List<String> learnSubs,
			List<TPlDezySubjectcomp> fourthProSubcomps, int classNumMax,
			int classNumMin, HashMap<String, List<TPlDezySubjectcomp>> subComps) {
		// TODO Auto-generated method stub
		HashMap<String, List<TPlDezySubjectcomp>> rs = new HashMap<String, List<TPlDezySubjectcomp>>();
		ListSortUtil<TPlDezySubjectcomp> compare = new ListSortUtil<TPlDezySubjectcomp>();
		compare.sort(fourthProSubcomps, "compNum", "asc");
		List<JSONObject> subjectList = new ArrayList<JSONObject>();
		HashMap<String, Integer> subRs = new HashMap<String, Integer>();
		for (String subId : subComps.keySet()) {
			JSONObject subObj = new JSONObject();
			subObj.put("subId", subId);
			List<TPlDezySubjectcomp> spList = subComps.get(subId);
			subObj.put("spList", spList);
			int zrs = 0;
			for (TPlDezySubjectcomp sp : spList) {
				zrs += sp.getCompNum();
			}
			subObj.put("rs", zrs);
			subRs.put(subId, zrs);
			subjectList.add(subObj);
		}
		SortUtil.sortListByTime(subjectList, "rs", "asc", "", "");

		for (int i = 0; i < subjectList.size(); i++) {
			JSONObject lsub = subjectList.get(i);
			String lsubId = lsub.getString("subId");
			int lrs = subRs.get(lsubId);
			List<TPlDezySubjectcomp> lspList = subComps.get(lsubId);
			boolean lfit = lrs > classNumMin / 2 ? true : false;
			for (int j = subjectList.size() - 1; j > i; j--) {
				JSONObject rsub = subjectList.get(j);
				String rsubId = rsub.getString("subId");
				int rrs = subRs.get(rsubId);
				if (rrs < classNumMin / 2) {
					continue;
				}
				List<TPlDezySubjectcomp> rsplist = subComps.get(rsubId);
				List<TPlDezySubjectcomp> rRemoveList = new ArrayList<TPlDezySubjectcomp>();
				compare.sort(rsplist, "compNum", "desc");

				for (TPlDezySubjectcomp rsp : rsplist) {
					if (rsp.getUnfinishProSubTasks().contains(lsubId)) {
						if (lrs > classNumMin / 2 || rrs < classNumMin / 2) {
							lfit = true;
							break;
						}
						lrs += rsp.getCompNum();
						rrs -= rsp.getCompNum();
						lspList.add(rsp);
						rRemoveList.add(rsp);
					}
				}
				rsplist.removeAll(rRemoveList);
				subRs.put(lsubId, lrs);
				subRs.put(rsubId, rrs);
				if (lfit) {
					break;
				}
			}
		}

		return subComps;
	}

	/**
	 * @param optionTclassMap
	 * @param tclassSubMap
	 * @param classMidNum
	 * @param classGroup
	 * @param subcomp
	 * @param usub
	 * @param classSeq
	 * @param classLev
	 * @param loop
	 * @param subjectCompMap
	 * @return
	 */
	public static boolean isAddToExistSuc(
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			int classMidNum, TPlDezyClassgroup classGroup,
			TPlDezySubjectcomp subcomp, String usub, int classSeq,
			int classLev, int loop,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap) {
		boolean sarr = false;
		String optKey = classGroup.getClasstGroupId() + "_" + usub + "_"
				+ classSeq + "_" + classLev;
		List<TPlDezyClass> tclList = optionTclassMap.get(optKey);

		if (tclList != null && tclList.size() > 0) {
			for (TPlDezyClass tarTcl : tclList) {
				boolean normalb = tarTcl.getTclassNum() <= classMidNum * 1.5
						&& (tarTcl.getTclassNum() + subcomp.getCompNum()) <= classMidNum * 1.5;
				if (normalb || loop > 7) {

					List<String> lspIds = new ArrayList<String>();
					lspIds.add(subcomp.getSubjectCompId());
					genRelationOfTclassSubCompByIds(lspIds, tarTcl,
							tclassSubMap);
					genFinishSubCompsExtend(lspIds, subjectCompMap, usub,
							classSeq,tarTcl.getTclassId());
					tarTcl.setTclassNum(tarTcl.getTclassNum()
							+ subcomp.getCompNum());
					sarr = true;
					break;
				}
			}
		}

		return sarr;
	}

	/**
	 * 定二部分班级开班
	 * 
	 * @param dezyClassList
	 * @param optionTclassMap
	 * @param tclassSubMap
	 * @param placeAlgMethod
	 * @param kmMcMap
	 * @param subjectCompMap
	 * @param classSubcompMap
	 * @param xzgroudMap
	 * @param fzgroudMap
	 * @param fzgroudList
	 * @param xzclassMap
	 * @param subject2GroupMap
	 * @param minTeaNum
	 * @param classGroup
	 * @param subSeqNum
	 * @param unfinishOptCompsMap
	 * @param unfinishProCompsMap
	 * @param learnSubs
	 * @param placementAlgType
	 */
	public static void genFixTwoTclass(List<TPlDezyClass> dezyClassList,
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			int placeAlgMethod, Map<String, String> kmMcMap,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			HashMap<String, List<TPlDezySubjectcomp>> classSubcompMap,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList,
			HashMap<String, TPlDezyClass> xzclassMap,
			HashMap<String, TPlDezySubjectgroup> subject2GroupMap,
			int minTeaNum, TPlDezyClassgroup classGroup,
			HashMap<String, Integer> subSeqNum,
			HashMap<String, List<TPlDezySubjectcomp>> unfinishOptCompsMap,
			HashMap<String, List<TPlDezySubjectcomp>> unfinishProCompsMap,
			List<String> learnSubs, int genType, int isTechExist,
			boolean isTechOptExist) {
		// 开选考班 7xuan3
		boolean qxs = false;
		if (learnSubs.size() > 3 && isTechExist == 1 && isTechOptExist) {
			qxs = true;
		}
		// 科目_实验班等级-班级序列map
		HashMap<String, Integer> subLevel2SeqMap = new HashMap<String, Integer>();
		
		Collection<TPlDezyClass> xzcoll = xzclassMap.values();
		List<TPlDezyClass> xzclList = new ArrayList<TPlDezyClass>();
		xzclList.addAll(xzcoll);
		Collections.shuffle(xzclList);
		for (TPlDezyClass xzclass : xzclList) {
			if (xzclass.getTclassType() == 6) {
				// 获取最多的两门科目 开出选考班，并记录未完成的选考志愿组合
				List<TPlDezySubjectcomp> allSubcomps = classSubcompMap
						.get(xzclass.getTclassId());
				// unfinishProComps.addAll(allSubcomps);
				// 科目下的志愿组合
				HashMap<String, List<String>> subCompsMap = new HashMap<String, List<String>>();
				// 各科目人数之和
				HashMap<String, Integer> subRs = new HashMap<String, Integer>();
				for (TPlDezySubjectcomp subcomp : allSubcomps) {
					String[] compSubs = subcomp.getCompFrom().split(",");
					for (int i = 0; i < compSubs.length; i++) {
						String csub = compSubs[i];
						String compId = subcomp.getSubjectCompId();
						TPlDezySubjectcomp subComp = subjectCompMap.get(compId);
						List<String> singleSubList = null;
						int srs = 0;
						if (subCompsMap.containsKey(csub)) {
							singleSubList = subCompsMap.get(csub);
							srs = subRs.get(csub);
						} else {
							singleSubList = new ArrayList<String>();
						}
						srs += subcomp.getCompNum();
						subRs.put(csub, srs);
						singleSubList.add(subComp.getSubjectCompId());
						subCompsMap.put(csub, singleSubList);
					}
					if (genType == 1) {

						for (String lsubId : learnSubs) {
							if (!TmpUtil.isStrInArr(lsubId, compSubs)) {
								List<TPlDezySubjectcomp> unfinishProComps = null;
								if (unfinishProCompsMap.containsKey(lsubId)) {
									unfinishProComps = unfinishProCompsMap
											.get(lsubId);
								} else {
									unfinishProComps = new ArrayList<TPlDezySubjectcomp>();
								}
								if (!unfinishProComps.contains(subcomp)) {
									unfinishProComps.add(subcomp);
								}
								unfinishProCompsMap.put(lsubId,
										unfinishProComps);
							}
						}
					}
				}
				// 取人数最高的前两科开选考班
				int maxOne = 0;
				int maxTwo = 0;
				String subOne = null;
				String subTwo = null;
				List<JSONObject> csubList = new ArrayList<JSONObject>();
				for (String csub : subRs.keySet()) {
					JSONObject csubo = new JSONObject();
					csubo.put("subId", csub);
					if (!isTechOptExist && isTechExist == 1
							&& csub.equals("19")) {

						csubo.put("rs", 0);
					} else {

						csubo.put("rs", subRs.get(csub));
					}
					csubList.add(csubo);
				}
				SortUtil.sortListByTime(csubList, "rs", "desc", "", "");
				maxOne = csubList.get(0).getIntValue("rs");
				subOne = csubList.get(0).getString("subId");
				maxTwo = csubList.get(1).getIntValue("rs");
				subTwo = csubList.get(1).getString("subId");
				for (String unsubId : subCompsMap.keySet()) {
					if (!unsubId.equals(subOne) && !unsubId.equals(subTwo)) {
						List<String> unfinishSubcompIds = subCompsMap
								.get(unsubId);
						List<TPlDezySubjectcomp> unfinishOptComps = null;
						if (unfinishOptCompsMap.containsKey(unsubId)) {
							unfinishOptComps = unfinishOptCompsMap.get(unsubId);
						} else {
							unfinishOptComps = new ArrayList<TPlDezySubjectcomp>();
						}
						for (String unfinishSubcompId : unfinishSubcompIds) {
							unfinishOptComps.add(subjectCompMap
									.get(unfinishSubcompId));
						}
						unfinishOptCompsMap.put(unsubId, unfinishOptComps);
					}
				}
				boolean seqOne = true;
				String slkey = subOne + "_" + xzclass.getIsExpClass();
				if (subLevel2SeqMap.containsKey(slkey)
						&& subLevel2SeqMap.get(slkey) == 1) {
					seqOne = false;
					subLevel2SeqMap.put(slkey, 2);
				} else {
					subLevel2SeqMap.put(slkey, 1);
				}
				int nseq = seqOne ? 1 : 2;

				List<TPlDezyClass> relatedClasses = new ArrayList<TPlDezyClass>();
				TPlDezyClass newTclass1 = TmpUtil.genZtTclass(classGroup,
						subject2GroupMap.get(subOne), nseq, subOne, xzclass,
						maxOne, 1, xzgroudMap, kmMcMap, placeAlgMethod);

				TmpUtil.genRelationOfTclassSubCompByIds(
						subCompsMap.get(subOne), newTclass1, tclassSubMap);

				genFinishSubCompsExtend(subCompsMap.get(subOne),
						subjectCompMap, subOne, nseq,  newTclass1.getTclassId());

				dezyClassList.add(newTclass1);

				nseq = seqOne ? 2 : 1;
				TPlDezyClass newTclass2 = TmpUtil.genZtTclass(classGroup,
						subject2GroupMap.get(subTwo), nseq, subTwo, xzclass,
						maxTwo, 1, xzgroudMap, kmMcMap, placeAlgMethod);

				TmpUtil.genRelationOfTclassSubCompByIds(
						subCompsMap.get(subTwo), newTclass2, tclassSubMap);

				genFinishSubCompsExtend(subCompsMap.get(subTwo),
						subjectCompMap, subTwo, nseq, newTclass2.getTclassId());

				dezyClassList.add(newTclass2);

				TmpUtil.addTclassToOptMap(optionTclassMap, newTclass1);
				TmpUtil.addTclassToOptMap(optionTclassMap, newTclass2);
			}
		}
	}

	/**
	 * 固定 学考定二
	 * 
	 * @param dezyClassList
	 * @param optionTclassMap
	 * @param tclassSubMap
	 * @param placeAlgMethod
	 * @param kmMcMap
	 * @param subjectCompMap
	 * @param classSubcompMap
	 * @param xzgroudMap
	 * @param fzgroudMap
	 * @param fzgroudList
	 * @param xzclassMap
	 * @param subject2GroupMap
	 * @param minTeaNum
	 * @param classGroup
	 * @param subSeqNum
	 * @param unfinishOptCompsMap
	 * @param unfinishProCompsMap
	 * @param learnSubs
	 * @param genType
	 * @param isTechExist
	 * @param isTechOptExist
	 */
	public static void genFixTwoTclassPro(List<TPlDezyClass> dezyClassList,
			HashMap<String, List<TPlDezyClass>> optionTclassMap,
			Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap,
			int placeAlgMethod, Map<String, String> kmMcMap,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap,
			HashMap<String, List<TPlDezySubjectcomp>> classSubcompMap,
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList,
			HashMap<String, TPlDezyClass> xzclassMap,
			HashMap<String, TPlDezySubjectgroup> subject2GroupMap,
			int minTeaNum, TPlDezyClassgroup classGroup,
			HashMap<String, Integer> subSeqNum,
			HashMap<String, List<TPlDezySubjectcomp>> unfinishOptCompsMap,
			HashMap<String, List<TPlDezySubjectcomp>> unfinishProCompsMap,
			List<String> learnSubs, int genType, int isTechExist,
			boolean isTechProExist) {

		HashMap<String, Integer> subLevel2SeqMap = new HashMap<String, Integer>();
		HashMap<String, Integer> subLevelSeq2NumMap = new HashMap<String, Integer>();

		// 开学考班
		for (TPlDezyClass xzclass : xzclassMap.values()) {
			if (xzclass.getTclassType() == 6) {
				// 获取最多的两门科目 开出学考班，并记录未完成的选考志愿组合
				List<TPlDezySubjectcomp> allSubcomps = classSubcompMap
						.get(xzclass.getTclassId());
				// unfinishProComps.addAll(allSubcomps);
				// 科目下的志愿组合
				HashMap<String, List<String>> subCompsMap = new HashMap<String, List<String>>();
				// 各科目人数之和
				HashMap<String, Integer> subRs = new HashMap<String, Integer>();
				for (TPlDezySubjectcomp subcomp : allSubcomps) {
					List<String> compSubs = subcomp.getUnfinishProSubTasks();
					for (int i = 0; i < compSubs.size(); i++) {
						String csub = compSubs.get(i);
						String compId = subcomp.getSubjectCompId();
						TPlDezySubjectcomp subComp = subjectCompMap.get(compId);
						List<String> singleSubList = null;
						int srs = 0;
						if (subCompsMap.containsKey(csub)) {
							singleSubList = subCompsMap.get(csub);
							srs = subRs.get(csub);
						} else {
							singleSubList = new ArrayList<String>();
						}
						srs += subcomp.getCompNum();
						subRs.put(csub, srs);
						singleSubList.add(subComp.getSubjectCompId());
						subCompsMap.put(csub, singleSubList);
					}

				}
				// 取人数最高的前两科开选考班
				int maxOne = 0;
				int maxTwo = 0;
				int maxThree = 0;
				String subOne = null;
				String subTwo = null;
				String subThree = null;
				List<JSONObject> csubList = new ArrayList<JSONObject>();
				for (String csub : subRs.keySet()) {
					JSONObject csubo = new JSONObject();
					csubo.put("subId", csub);
					csubo.put("rs", subRs.get(csub));
					// 不开的学考科目略过
					if (!learnSubs.contains(csub)) {
						continue;
					}
					csubList.add(csubo);
				}
				if (csubList.size() == 0) {
					continue;
				}
				SortUtil.sortListByTime(csubList, "rs", "desc", "", "");
				maxOne = csubList.get(0).getIntValue("rs");
				subOne = csubList.get(0).getString("subId");
				// 学考足以开第二定科目
				if (learnSubs.size() > 2 && csubList.size() > 1) {
					maxTwo = csubList.get(1).getIntValue("rs");
					subTwo = csubList.get(1).getString("subId");
				}
				// 学考定三走一
				if (learnSubs.size() > 3 && isTechExist == 1 && isTechProExist
						&& csubList.size() > 2) {
					maxThree = csubList.get(2).getIntValue("rs");
					subThree = csubList.get(2).getString("subId");
				}
				for (String unsubId : subCompsMap.keySet()) {
					if (!unsubId.equals(subOne) && !unsubId.equals(subTwo)) {
						List<String> unfinishSubcompIds = subCompsMap
								.get(unsubId);
						List<TPlDezySubjectcomp> unfinishProComps = null;
						if (unfinishProCompsMap.containsKey(unsubId)) {
							unfinishProComps = unfinishProCompsMap.get(unsubId);
						} else {
							unfinishProComps = new ArrayList<TPlDezySubjectcomp>();
						}
						for (String unfinishSubcompId : unfinishSubcompIds) {
							unfinishProComps.add(subjectCompMap
									.get(unfinishSubcompId));
						}
						unfinishProCompsMap.put(unsubId, unfinishProComps);
					}
				}

				boolean seqOne = true;
				String slkey = subOne + "_" + xzclass.getIsExpClass();
				int seq1 = 4;
				int seq2 = 5;
				int seq3 = 6;
				if (subLevel2SeqMap.containsKey(slkey)
						&& subLevel2SeqMap.get(slkey) == 4 && subTwo != null) {
					seqOne = false;
					subLevel2SeqMap.put(slkey, 5);
					seq1 = 5;
					seq2 = 4;
				} else {
					subLevel2SeqMap.put(slkey, 4);
				}
				subLevel2SeqMap.put(subTwo + "_" + xzclass.getIsExpClass(),
						seq2);
				if (subThree != null) {
					List<Integer> seqList = new ArrayList<Integer>();
					seqList.add(4);
					seqList.add(5);
					seqList.add(6);

					int[] seqArr = getBestSeqResult(subLevelSeq2NumMap,
							seqList, subOne, subTwo, subThree);
					seq1 = seqArr[0];
					seq2 = seqArr[1];
					seq3 = seqArr[2];
				}
				List<TPlDezyClass> relatedClasses = new ArrayList<TPlDezyClass>();
				TPlDezyClass newTclass1 = TmpUtil.genZtTclass(classGroup,
						subject2GroupMap.get(subOne), seq1, subOne, xzclass,
						maxOne, 2, xzgroudMap, kmMcMap, placeAlgMethod);

				TmpUtil.genRelationOfTclassSubCompByIds(
						subCompsMap.get(subOne), newTclass1, tclassSubMap);

				genFinishSubCompsExtend(subCompsMap.get(subOne),
						subjectCompMap, subOne, seq1, newTclass1.getTclassId());
				dezyClassList.add(newTclass1);
				TmpUtil.addTclassToOptMap(optionTclassMap, newTclass1);
				if (subTwo != null) {

					TPlDezyClass newTclass2 = TmpUtil.genZtTclass(classGroup,
							subject2GroupMap.get(subTwo), seq2, subTwo,
							xzclass, maxTwo, 2, xzgroudMap, kmMcMap, placeAlgMethod);

					TmpUtil.genRelationOfTclassSubCompByIds(
							subCompsMap.get(subTwo), newTclass2, tclassSubMap);

					genFinishSubCompsExtend(subCompsMap.get(subTwo),
							subjectCompMap, subTwo, seq2, newTclass2.getTclassId());

					dezyClassList.add(newTclass2);
					TmpUtil.addTclassToOptMap(optionTclassMap, newTclass2);
				}
				if (subThree != null) {

					TPlDezyClass newTclass3 = TmpUtil.genZtTclass(classGroup,
							subject2GroupMap.get(subThree), seq3, subThree,
							xzclass, maxThree, 2, xzgroudMap, kmMcMap, placeAlgMethod);

					TmpUtil.genRelationOfTclassSubCompByIds(
							subCompsMap.get(subThree), newTclass3, tclassSubMap);

					genFinishSubCompsExtend(subCompsMap.get(subThree),
							subjectCompMap, subThree, seq3, newTclass3.getTclassId());

					dezyClassList.add(newTclass3);
					TmpUtil.addTclassToOptMap(optionTclassMap, newTclass3);
				}

			}
		}
	}

	private static int[] getBestSeqResult(
			HashMap<String, Integer> subLevelSeq2NumMap, List<Integer> seqList,
			String subOne, String subTwo, String subThree) {
		// TODO Auto-generated method stub
		int[] rs = new int[3];
		List<JSONObject> seqScoreList = new ArrayList<JSONObject>();
		for (int i = 0; i < seqList.size(); i++) {

			int seq1 = seqList.get(i);
			for (int j = 0; j < seqList.size(); j++) {
				if (j == i) {
					continue;
				}
				int seq2 = seqList.get(j);

				for (int s : seqList) {
					if (s != seq1 && s != seq2) {
						int seq3 = s;

						boolean isFit1 = isSingleSubFit(seq1, subOne,
								subLevelSeq2NumMap, seqList);
						boolean isFit2 = isSingleSubFit(seq2, subTwo,
								subLevelSeq2NumMap, seqList);
						boolean isFit3 = isSingleSubFit(seq3, subThree,
								subLevelSeq2NumMap, seqList);
						int score = 0;
						if (isFit1) {
							score++;
						}
						if (isFit2) {
							score++;
						}
						if (isFit3) {
							score++;
						}

//						if (isFit1 && isFit2 && isFit3) {
//							rs[0] = seq1;
//							rs[1] = seq2;
//							rs[2] = seq3;
//
//							String key1 = subOne + "_" + seq1;
//							if (subLevelSeq2NumMap.containsKey(key1)) {
//								int num = subLevelSeq2NumMap.get(key1);
//								subLevelSeq2NumMap.put(key1, num++);
//							} else {
//								subLevelSeq2NumMap.put(key1, 1);
//							}
//							String key2 = subTwo + "_" + seq2;
//							if (subLevelSeq2NumMap.containsKey(key2)) {
//								int num = subLevelSeq2NumMap.get(key2);
//								subLevelSeq2NumMap.put(key2, num++);
//							} else {
//								subLevelSeq2NumMap.put(key2, 1);
//							}
//							String key3 = subThree + "_" + seq3;
//							if (subLevelSeq2NumMap.containsKey(key3)) {
//								int num = subLevelSeq2NumMap.get(key3);
//								subLevelSeq2NumMap.put(key3, num++);
//							} else {
//								subLevelSeq2NumMap.put(key3, 1);
//							}
//							return rs;
//						}

						JSONObject seqScore = new JSONObject();
						seqScore.put("score", score);
						seqScore.put("seqs", seq1 + "," + seq2 + "," + seq3);
						seqScoreList.add(seqScore);
					}
				}
			}
		}
		SortUtil.sortListByTime(seqScoreList, "score", "desc", "", "");
		JSONObject bseq = seqScoreList.get(0);
		List<JSONObject> sameRsResult = new ArrayList<JSONObject>();
		sameRsResult.add(bseq);
		for (JSONObject json : seqScoreList) {
			if (json.getIntValue("score") == bseq.getIntValue("score")) {
				sameRsResult.add(json);
			} else {
				break;
			}
		}
		bseq = sameRsResult.get((int) (Math.random() * sameRsResult.size()));
		String[] seqs = bseq.getString("seqs").split(",");
		rs[0] = Integer.parseInt(seqs[0]);
		rs[1] = Integer.parseInt(seqs[1]);
		rs[2] = Integer.parseInt(seqs[2]);
		String key1 = subOne + "_" + rs[0];
		if (subLevelSeq2NumMap.containsKey(key1)) {
			int num = subLevelSeq2NumMap.get(key1);
			subLevelSeq2NumMap.put(key1, num++);
		} else {
			subLevelSeq2NumMap.put(key1, 1);
		}
		String key2 = subTwo + "_" + rs[1];
		if (subLevelSeq2NumMap.containsKey(key2)) {
			int num = subLevelSeq2NumMap.get(key2);
			subLevelSeq2NumMap.put(key2, num++);
		} else {
			subLevelSeq2NumMap.put(key2, 1);
		}
		String key3 = subThree + "_" + rs[2];
		if (subLevelSeq2NumMap.containsKey(key3)) {
			int num = subLevelSeq2NumMap.get(key3);
			subLevelSeq2NumMap.put(key3, num++);
		} else {
			subLevelSeq2NumMap.put(key3, 1);
		}
		return rs;
	}

	private static boolean isSingleSubFit(int seq1, String subOne,
			HashMap<String, Integer> subLevelSeq2NumMap, List<Integer> seqList) {
		// TODO Auto-generated method stub
		boolean rs = true;
		String skey = subOne + "_" + seq1;
		if (!subLevelSeq2NumMap.containsKey(skey)) {
			return true;
		} else {
			for (Integer seq : seqList) {
				if (seq != seq1
						&& !subLevelSeq2NumMap.containsKey(subOne + "_" + seq)) {
					return false;
				}
			}
			int nowNum = subLevelSeq2NumMap.get(skey);
			for (Integer seq : seqList) {
				if (seq != seq1
						&& nowNum > subLevelSeq2NumMap.get(subOne + "_" + seq)) {
					return false;
				}
			}
		}
		return rs;
	}

	private static void genFinishSubCompsExtend(List<String> subcomps,
			HashMap<String, TPlDezySubjectcomp> subjectCompMap, String subId,
			int classSeq,String tclassId) {
		// TODO Auto-generated method stub
		for (String subcompId : subcomps) {
			TPlDezySubjectcomp subcomp = subjectCompMap.get(subcompId);
			if (subcomp.getCompFrom().equals("7,8,19") && subId.equals("6")
					&& subcomp.getCompNum() == 12) {
				System.out.println("fdsfs");
			}
			if (!subcomp.getFinishSeqs().contains((Integer) classSeq)) {
				subcomp.getFinishSeqs().add((Integer) classSeq);
			} else {
				System.out.println("fds");
			}
			subcomp.getFinishSubTseqMap().put(subId, classSeq);
			subcomp.getFinishSubTclassMap().put(subId, tclassId);
			subcomp.getUnfinishOptSubTasks().remove(subId);
			subcomp.getUnfinishProSubTasks().remove(subId);
		}
	}

	/**
	 * 初始化定二走一分班参数
	 * 
	 * @param dezySetting
	 * @param subjectSetList
	 * @param classNumMax
	 * @param classNumMin
	 * @param classMidNum
	 * @param placeAlgMethod
	 * @param subjectCompList
	 * @param kmMcMap
	 * @return
	 */
	public static DezyDisTclassParams initDezySettingParams(
			TPlDezySettings dezySetting,
			List<TPlDezySubjectSet> subjectSetList, int classNumMax,
			int classNumMin, int classMidNum, int placeAlgMethod,
			List<TPlDezySubjectcomp> subjectCompList,
			Map<String, String> kmMcMap) {
		DezyDisTclassParams disTclassParams = new DezyDisTclassParams();
		disTclassParams.setClassMaxNum(classNumMax);
		disTclassParams.setClassMidNum(classMidNum);
		disTclassParams.setClassMinNum(classNumMin);
		disTclassParams.setClassRoundNum(dezySetting.getClassRoundNum());
		disTclassParams.setCombinedSubNum(dezySetting.getCombinedSubNum());
		disTclassParams.setFormOfTech(dezySetting.getFormOfTech());
		disTclassParams.setIsLearnExist(dezySetting.getIsLearnExist());
		disTclassParams.setIsTechExist(dezySetting.getIsTechExist());
		disTclassParams.setPlaceAlgMethod(placeAlgMethod);
		disTclassParams.setSubjectSetMap(subjectSetList);
		disTclassParams.setSubjectCompIdMap(subjectCompList);
		disTclassParams.setKmMcMap(kmMcMap);
		return disTclassParams;
	}

	/**
	 * 定二走一分教学班班总入口
	 * 
	 * @param disTclassParams
	 * @param disTclassRunParams
	 */
	public static int divTclassCommon(DezyDisTclassParams disTclassParams,
			DezyDisTclassRunParams disTclassRunParams) {
		// TODO Auto-generated method stub
		// 获取分班类型 1：不跨组-科目组分离 2：跨组-学选分离（浒山版本）3：跨组-科目组分离（舟山版本）
		int disType = disTclassParams.getDisType();
		// 未完成的志愿组合
		int unfinishSubs = 0;
		switch (disType) {
		case 1:
			//
			unfinishSubs = divTclassNoCrossGroup(disTclassParams,
					disTclassRunParams);
			break;
		case 2:
			break;
		case 3:
			break;
		default:
			break;
		}
		return unfinishSubs;
	}

	/**
	 * 教学班分班核心算法--不跨组
	 * 
	 * @param disTclassParams
	 * @param disTclassRunParams
	 * @return
	 */
	private static int divTclassNoCrossGroup(
			DezyDisTclassParams disTclassParams,
			DezyDisTclassRunParams disTclassRunParams) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 优化减少辅助教室
	 * 
	 * @param xzgroudMap
	 * @param fzgroudMap
	 * @param fzgroudList
	 * @param dezyClassList
	 * @param xzgroud2xzClassIdMap
	 * @param kmMcMap
	 * @param tclass2classIdMap 
	 * @param tclass2MaxRsClassMap 
	 * @param xzclassMap2
	 */
	public static void adjustGrounds(
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList, List<TPlDezyClass> dezyClassList,
			HashMap<String, String> xzgroud2xzClassIdMap,
			HashMap<String, TPlDezyClass> xzclassMap,
			Map<String, String> kmMcMap, int alg,
			Map<String, String> tclass2MaxRsClassMap,
			Map<String, List<String>> tclass2classIdMap,int xzMaxRs) {

		// TODO Auto-generated method stub
 
		HashMap<String,Integer> tclassNameMap = new HashMap<String, Integer>();
		for(TPlDezyClass xkclass:dezyClassList){
			if(xkclass.getGroundId()==null){
				//未安排教室的安排教室
				String mxRsClass = tclass2MaxRsClassMap.get(xkclass.getTclassId());
				List<String> relatedTclass = tclass2classIdMap.get(xkclass.getTclassId());
				TPlDezyClass xzclass = xzclassMap.get(mxRsClass);
						
				List<TPlDezyClass> relatedXzclass = new ArrayList<TPlDezyClass>();
				for(String rcid:relatedTclass){
					relatedXzclass.add(xzclassMap.get(rcid));
				}
				int seq = xkclass.getClassSeq();
				String subgId = "";
				if (xkclass.getSubjectGroupId() != null&&!xkclass.getSubjectGroupId().equals("-999")
						&& alg == 1) {
					subgId = xkclass.getSubjectGroupId();
				}
				String substr = "";
				if (seq == 8) {
					substr = "B";
				}
				if (!xzgroudMap.containsKey(subgId + "_" + xzclass.getTclassId() + "_"
						+ seq) && (xzMaxRs!=0&&xkclass.getTclassNum()<=xzMaxRs)) {
		
					xkclass.setGroundId(xzclass.getGroundId());
					xkclass.setGroundName(xzclass.getGroundName());
		
					xzgroudMap.put(subgId + "_" + xzclass.getTclassId() + "_" + seq,
							"exist");
					if (seq == 8) {
						xzgroudMap.put(subgId + "_" + xzclass.getTclassId() + "_" + 3,
								"exist");
					}
				} else {
					String gid = null;
					String gname = null;
					boolean canArr = true;
					for (int i = 0; i < fzgroudList.size(); i++) {
						String gskey = subgId + "_" + i;
						if (fzgroudMap.containsKey(gskey)) {
							List<Integer> seqList = fzgroudMap.get(gskey);
							if (seqList.contains((Integer) seq)) {
								canArr = false;
							} else {
								canArr = true;
							}
						} else {
							List<Integer> seqList = new ArrayList<Integer>();
							seqList.add((Integer) seq);
							fzgroudMap.put(gskey, seqList);
							canArr = true;
						}
						if (canArr) {
		
							JSONObject ground = fzgroudList.get(i);
							gid = ground.getString("groundId");
							gname = ground.getString("groundName");
							fzgroudMap.get(gskey).add(seq);
							break;
						}
					}
					if(  (xzMaxRs!=0&&xkclass.getTclassNum()<=xzMaxRs)&&gid==null){
						
						// 优先其它组内行政班
						for (TPlDezyClass tcl : relatedXzclass) {
							
							if (!xzgroudMap.containsKey(subgId + "_" + tcl.getTclassId()
									+ "_" + seq)) {
								
								gid = (tcl.getGroundId());
								gname = (tcl.getGroundName() + substr);
								xzgroudMap.put(
										subgId + "_" + tcl.getTclassId() + "_" + seq,
										"exist");
								if (seq == 8) {
									xzgroudMap.put(subgId + "_" + tcl.getTclassId() + "_"
											+ 3, "exist");
								}
								break;
							}
						}
						if(gid==null){
							//查找其它行政班可用教室
							for (TPlDezyClass tcl : xzclassMap.values()) {
								
								if (!xzgroudMap.containsKey(subgId + "_" + tcl.getTclassId()
										+ "_" + seq)) {
									
									gid = (tcl.getGroundId());
									gname = (tcl.getGroundName() + substr);
									xzgroudMap.put(
											subgId + "_" + tcl.getTclassId() + "_" + seq,
											"exist");
									if (seq == 8) {
										xzgroudMap.put(subgId + "_" + tcl.getTclassId() + "_"
												+ 3, "exist");
									}
									break;
								}
							}
						}
					}
					// 无行政班教室可用
					if (gid == null) {
						JSONObject ground = new JSONObject();
						ground.put("groundId", UUIDUtil.getUUID());
						ground.put("groundName", "辅助教室" + (fzgroudList.size() + 1)
								+ substr);
						gid = ground.getString("groundId");
						gname = ground.getString("groundName");
						fzgroudList.add(ground);
		
						List<Integer> seqList = new ArrayList<Integer>();
						seqList.add(seq);
						if (seq == 8) {
							seqList.add(3);
						}
						fzgroudMap
								.put(subgId + "_" + (fzgroudList.size() - 1), seqList);
					}
					xkclass.setGroundId(gid);
					xkclass.setGroundName(gname);
					
				}
			} 
			
			if(xkclass.getTclassName()==null){
				
				StringBuffer tclassName = new StringBuffer();
				tclassName.append(kmMcMap.get(xkclass.getSubjectId())).append("（")
				.append(xkclass.getTclassLevel() == 1 ? "选" : "学").append("）").append("（")
				.append(xkclass.getGroundName().replaceAll("教室", ""))
				.append("）");
				xkclass.setTclassName(tclassName.toString());
				xkclass.setOriClassName(tclassName.toString());
			}
			//重名的改名
			String tName = xkclass.getTclassName();
			if(tName==null){
				System.out.println("fds");
			}
			if(tclassNameMap.containsKey(tName)){
				int num = tclassNameMap.get(tName);
				String appFix = "";
				if(num==1){
					appFix = "B";
				}else if(num==2){
					appFix = "C";
				}
				tclassNameMap.put(tName, num++);
				tName = tName+appFix;
				xkclass.setTclassName(tName);
				xkclass.setOriClassName(tName);
			}else{
				tclassNameMap.put(tName, 1);
			}
		}
	}
	
	public static void adjustGrounds2(
			ConcurrentHashMap<String, String> xzgroudMap,
			ConcurrentHashMap<String, List<Integer>> fzgroudMap,
			Vector<JSONObject> fzgroudList, List<TPlDezyClass> dezyClassList,
			HashMap<String, String> xzgroud2xzClassIdMap,
			HashMap<String, TPlDezyClass> xzclassMap,
			Map<String, String> kmMcMap, int placeAlgMethod) {

		// TODO Auto-generated method stub

		HashMap<String, List<TPlDezyClass>> ground2TclassList = new HashMap<String, List<TPlDezyClass>>();
		for (TPlDezyClass tclass : dezyClassList) {
			String gid = tclass.getGroundId();

			if (tclass.getGroundName().startsWith("辅助")) {
				List<TPlDezyClass> tclList = null;
				if (ground2TclassList.containsKey(gid)) {
					tclList = ground2TclassList.get(gid);
				} else {
					tclList = new ArrayList<TPlDezyClass>();
				}
				tclList.add(tclass);
				ground2TclassList.put(gid, tclList);
			}
		}
		List<JSONObject> needremove = new ArrayList<JSONObject>();
		for (int i = fzgroudList.size() - 1; i >= 0; i--) {
			JSONObject fzground = fzgroudList.get(i);
			String fzgid = fzground.getString("groundId");

			List<TPlDezyClass> tclList = ground2TclassList.get(fzgid);
			if (tclList == null) {
				needremove.add(fzground);
				continue;
			}
			boolean isAllArr = true;

			for (TPlDezyClass tcl : tclList) {
				String subgId = "";
				if (!tcl.getSubjectGroupId().equals("-999")
						&& placeAlgMethod != 2) {
					subgId = tcl.getSubjectGroupId();
				}
				int lvl = tcl.getClassSeq();
				int tlvl = tcl.getTclassLevel();
				boolean sar = false;
				for (String gid : xzgroud2xzClassIdMap.keySet()) {
					String xzclassId = xzgroud2xzClassIdMap.get(gid);
					TPlDezyClass xzclass = xzclassMap.get(xzclassId);
					String gkey = subgId + "_" + xzclassId + "_" + lvl;
					if (!xzgroudMap.containsKey(gkey)) {
						xzgroudMap.put(gkey, "exist");
						tcl.setGroundName(xzclass.getGroundName());
						tcl.setGroundId(gid);
						StringBuffer tclassName = new StringBuffer();
						tclassName
								.append(kmMcMap.get(tcl.getSubjectId()))
								.append("（")
								.append(tlvl == 1 ? "选" : "学")
								.append("）")
								.append("（")
								.append(xzclass.getGroundName().replaceAll(
										"教室", "")).append("）");
						tcl.setTclassName(tclassName.toString());
						tcl.setOriClassName(tclassName.toString());
						sar = true;
						break;
					} else {
						for (int j = 0; j < i; j++) {
							String fzkey = subgId + "_" + j;
							if (!fzgroudMap.containsKey(fzkey)
									|| !fzgroudMap.get(fzkey).contains(
											(Integer) lvl)) {
								List<Integer> seqList = null;
								if (fzgroudMap.contains(fzkey)) {
									seqList = fzgroudMap.get(fzkey);
								} else {
									seqList = new ArrayList<Integer>();
								}
								seqList.add(lvl);
								fzgroudMap.put(fzkey, seqList);
								JSONObject dfzground = fzgroudList.get(j);
								String dgid = dfzground.getString("groundId");
								String dgName = dfzground
										.getString("groundName");
								StringBuffer tclassName = new StringBuffer();
								tclassName
										.append(kmMcMap.get(tcl.getSubjectId()))
										.append("（")
										.append(tlvl == 1 ? "选" : "学")
										.append("）").append("（")
										.append(dgName.replaceAll("教室", ""))
										.append("）");
								tcl.setGroundId(dgid);
								tcl.setGroundName(dgName);
								tcl.setTclassName(tclassName.toString());
								tcl.setOriClassName(tclassName.toString());
								sar = true;
								break;
							}

						}
					}
				}
				if (!sar) {
					isAllArr = false;
				}
			}
			if (isAllArr) {
				needremove.add(fzground);
				fzgroudList.removeAll(needremove);
				adjustGrounds2(xzgroudMap, fzgroudMap, fzgroudList, tclList,
						xzgroud2xzClassIdMap, xzclassMap, kmMcMap,
						placeAlgMethod);
			}
		}

	}

	/**
	 * 
	 * @param disRs
	 * @param stuentZh
	 * @param req
	 * @param kmMcMap
	 * @param allExtGroundIds 辅助教室ID
	 * @return
	 */
	public static HashMap<String, Object> genLargeDivTclassResult(
			HashMap<String, Object> disRs,
			List<JSONObject> stuentZh, 
			JSONObject req, 
			Map<String, String> kmMcMap) {
		// TODO Auto-generated method stub
		HashMap<String, Object> rs = new HashMap<String, Object>();
		List<JSONObject> allTclasses = (List<JSONObject>) disRs.get("allTclasses");
		List<Classroom> allXzclassEntites = (List<Classroom>) disRs.get("allXzclassEntites");
		//int crossCz = (int) disRs.get("crossCz");
		//LargeDisParams largeDisParams = (LargeDisParams) disRs.get("largeDisParams");
		List<TPlConfIndexSubs> confIndexSubsList = (List<TPlConfIndexSubs>) disRs.get("confIndexSubsList");
		List<TPlDezyClass> tclassList = new ArrayList<TPlDezyClass>();
		List<TPlDezySubjectcomp> subCompList = new  ArrayList<TPlDezySubjectcomp>();
		List<TPlDezyTclassSubcomp> tclassSubCompList = new ArrayList<TPlDezyTclassSubcomp>();
		List<TPlDezySubjectcompStudent> subcompFromList = (List<TPlDezySubjectcompStudent>)disRs.get("subcompFromList");;
		List<TPlStudentinfo> lstStuList = new ArrayList<TPlStudentinfo>();
		String placementId = req.getString("placementId");
		String usedGrade = req.getString("usedGrade");
		String schoolId = req.getString("schoolId");
		String termInfo = req.getString("termInfo");
	
		//生成班级Id-班级映射
		Map<String,Classroom> classIdRoomMap = new HashMap<String,Classroom>();
		for(Classroom cr : allXzclassEntites){
			if(null == cr){	//去空
				continue;
			}
			classIdRoomMap.put(String.valueOf(cr.getId()), cr);
		}

		
		//生成学生志愿 TPlDezySubjectcompStudent 表
		Map<String,List<String>> subjectIdsStudentIdListMap = new HashMap<String,List<String>>();
		for(JSONObject stuZh : stuentZh){
			String studentId = stuZh.getString("accountId");
			String subjectIds = stuZh.getString("subjectIds");
			List<String> studentIdList = subjectIdsStudentIdListMap.get(subjectIds);
			if(null == studentIdList){
				studentIdList = new ArrayList<String>();
				subjectIdsStudentIdListMap.put(subjectIds, studentIdList);
			}
			studentIdList.add(studentId);
		}		
		
		//生成教学班信息 & 志愿信息
		List<String> allocatedWishingIds = new ArrayList<String>();
		//某个科目,序列已经分配的班级映射map（key: sub_seq_opt-1/pro-2）
		Map<String,List<String>> subSeqClassIdMap = new HashMap<String,List<String>>();
		Map<String,String> addedExtraGroundIdNameMap = new HashMap<String,String>();	//分配班级场地时,额外增加的场地
		Map<String,List<TPlDezyClass>> tclassnameMap = new HashMap();
		for(JSONObject oriTclass : allTclasses){

			TPlDezyClass tclass = new TPlDezyClass();
			tclass.setSchoolId(schoolId);
			tclass.setUsedGrade(usedGrade);
			tclass.setPlacementId(placementId);
			
			JSONArray tclassSubFrom = oriTclass.getJSONArray("tclassSubFrom");
			//生成 TPlDezySubjectcomp 表数据  & TPlDezyTclassSubcomp
			for(Object object : tclassSubFrom){	
				JSONObject oriSubjectComp = (JSONObject)object;
				TPlDezySubjectcomp subjectComp = new TPlDezySubjectcomp();
				
				//生成 TPlDezySubjectcomp 表数据
				int stuInZh = oriSubjectComp.getInteger("studentNum");
				String subjectIds = oriSubjectComp.getString("subjectIds");
				String wishingId = oriSubjectComp.getString("zhUUID");
				subjectComp.setClassId("-999");
				subjectComp.setCompFrom(subjectIds);
				subjectComp.setCompName(oriSubjectComp.getString("zhName"));
				subjectComp.setCompNum(stuInZh);//studentNum
				subjectComp.setSchoolId(schoolId);
				subjectComp.setSubjectCompId(wishingId);
				subjectComp.setPlacementId(placementId);
				subjectComp.setUsedGrade(usedGrade);
				
				subCompList.add(subjectComp);												
				
				//组合 TPlDezyTclassSubcomp 表数据
				TPlDezyTclassSubcomp tclassSubComp = new TPlDezyTclassSubcomp();
				tclassSubComp.setPlacementId(placementId);
				tclassSubComp.setSchoolId(schoolId);
				tclassSubComp.setUsedGrade(usedGrade);
				tclassSubComp.setSubjectCompId(oriSubjectComp.getString("zhUUID"));
				tclassSubComp.setTclassId(oriTclass.getString("tclassId"));				
				
				tclassSubCompList.add(tclassSubComp);
				
			}
			
			//组装教学班所属科目及序列（包括学选区分类别）
			Integer seq = oriTclass.getInteger("classSeq");
			String subjectId = oriTclass.getString("subjectId");
			Integer isOpt = oriTclass.getInteger("tclassLevel");
			
			//分配教学班上课地点
			String toBeAllocateGroundId = null;
			StringBuffer toBeAllocateGroundName = new StringBuffer();
			toBeAllocateGroundId = oriTclass.getString("groundId");
			String name = oriTclass.getString("groundName");
			if(name == null){
				name = classIdRoomMap.get(toBeAllocateGroundId).className;
			}
			toBeAllocateGroundName.append(name);
			
			//命名班级名称
			StringBuffer tclassName = new StringBuffer();
			tclassName.append(oriTclass.get("subjectName"));
			if(1==isOpt){
				tclassName.append("(选)");
			}else {
				tclassName.append("(学)");
			}
			tclassName.append(toBeAllocateGroundName.toString());
			
			tclass.setClassGroupId("-999");
			tclass.setClassSeq(seq);
			tclass.setGroundId(toBeAllocateGroundId);
			tclass.setGroundName(toBeAllocateGroundName.toString());
			tclass.setIsExpClass(0);
			tclass.setOriClassName(tclassName.toString());
			
			tclass.setTclassName(tclassName.toString());
			tclass.setSubjectGroupId("-999");
			tclass.setSubjectId(subjectId);
			tclass.setSubLevel(oriTclass.getInteger("subLevel"));
			tclass.setTclassId(oriTclass.getString("tclassId"));
			tclass.setTclassType(7);
			tclass.setTclassNum(oriTclass.getInteger("tclassNum"));
			tclass.setTclassLevel(isOpt);
			
			tclassList.add(tclass);
			List<TPlDezyClass> tclassNameList = tclassnameMap.get(tclass.getTclassName());
			if(tclassNameList==null){
				tclassNameList = new ArrayList();
				tclassnameMap.put(tclass.getTclassName(), tclassNameList);
			}
			tclassNameList.add(tclass);
		}
		//班级名去重
		for(Map.Entry<String,List<TPlDezyClass>> entry:tclassnameMap.entrySet()){
			if(entry.getValue().size()>1){
				for(int index = 0;index<entry.getValue().size();index++){
					TPlDezyClass tclass = entry.getValue().get(index);
					tclass.setTclassName(tclass.getTclassName()+(index+1));
				}
			}
		}
		
		//补全冲突索表信息
		for(TPlConfIndexSubs confIndexSub : confIndexSubsList){
			confIndexSub.setSchoolId(schoolId);
			confIndexSub.setPlacementId(placementId);
			confIndexSub.setUsedGrade(usedGrade);			
		}
		
		//补全学生志愿表信息
		for(TPlDezySubjectcompStudent subCompStu : subcompFromList){
			subCompStu.setPlacementId(placementId);
			subCompStu.setSchoolId(schoolId);
			subCompStu.setUsedGrade(usedGrade);
		}
		
		// 保存结果     
		rs.put("tclassList", tclassList);
		rs.put("tclassSubCompList", tclassSubCompList);
		rs.put("subcompFromList", subcompFromList);
		rs.put("subCompList", subCompList);
		rs.put("lstStuList", lstStuList);
		rs.put("confIndexSubsList", confIndexSubsList);
		return rs;
	}
	
}
