package com.talkweb.placementtask.utils.newdzb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.utils.newdzb.NewDZBPlacementExcuter.PlacementTaskConfig;
import com.talkweb.placementtask.utils.newdzb.NewDZBPlacementExcuter.SelectType;
import com.talkweb.placementtask.utils.newdzb.SubSubjectCombination.SubSubjectCombinationType;
import com.talkweb.placementtask.utils.newdzb.SubjectCombination.ConflictType;

public class ConflictComputer {
	private static int CD_OPT = NewDZBPlacementExcuter.CD_OPT;
	private static int CD_PRO = NewDZBPlacementExcuter.CD_PRO;
	public static void main(String[] args){
//		List<Integer> list1 = Arrays.asList(new Integer[]{1,2,3,4,5,6});
//		List<Integer> list2 = Arrays.asList(new Integer[]{1,2,3});
//		System.out.println(JSONObject.toJSONString(CollectionUtils.removeAll(list1, list2)));
//		System.out.println(JSONObject.toJSONString(list1));
		Map<Integer,String> subjectIdMap = new HashMap();
		subjectIdMap.put(4, "政");
		subjectIdMap.put(5, "史");
		subjectIdMap.put(6, "地");
		subjectIdMap.put(7, "物");
		subjectIdMap.put(8, "化");
		subjectIdMap.put(9, "生");
		//subjectIdMap.put(19, "技");
		Map<Integer,Integer> optSubjectLessonsMap = new HashMap();
		optSubjectLessonsMap.put(4, 3);
		optSubjectLessonsMap.put(5, 4);
		optSubjectLessonsMap.put(6, 3);
		optSubjectLessonsMap.put(7, 5);
		optSubjectLessonsMap.put(8, 4);
		optSubjectLessonsMap.put(9,3);
		//optSubjectLessonsMap.put(19, 4);
		
		Map<Integer,Integer> proSubjectLessonsMap = new HashMap();
		proSubjectLessonsMap.put(4, 1);
		proSubjectLessonsMap.put(5, 1);
		proSubjectLessonsMap.put(6, 1);
		proSubjectLessonsMap.put(7, 2);
		proSubjectLessonsMap.put(8, 2);
		proSubjectLessonsMap.put(9, 3);
	//	proSubjectLessonsMap.put(19,2);
		
//		Map<Integer,Integer> optSubjectLessonsMap = new HashMap();
//		optSubjectLessonsMap.put(4, 3);
//		optSubjectLessonsMap.put(5, 3);
//		optSubjectLessonsMap.put(6, 3);
//		optSubjectLessonsMap.put(7, 3);
//		optSubjectLessonsMap.put(8, 3);
//		optSubjectLessonsMap.put(9, 3);
//		
//		Map<Integer,Integer> proSubjectLessonsMap = new HashMap();
//		proSubjectLessonsMap.put(4, 1);
//		proSubjectLessonsMap.put(5, 1);
//		proSubjectLessonsMap.put(6, 1);
//		proSubjectLessonsMap.put(7, 1);
//		proSubjectLessonsMap.put(8, 1);
//		proSubjectLessonsMap.put(9, 3);
//		
		Object[][] orginSC111=  {{"地化生",28},
				{"地物化",26},
				{"地物生",9},
				{"史地化",11},
				{"史地生",26},
				{"史地物",37},
				{"史化生",16},
				{"史物化",13},
				{"史物生",181},
				{"物化生",35},
				{"政地化",9},
				{"政地生",17},
				{"政地物",57},
				{"政化生",35},
				{"政史地",33},
				{"政史化",24},
				{"政史生",14},
				{"政史物",4},
				{"政物化",10},
				{"政物生",11},
				{"政史技",4},
				{"政物技",10},
				{"政化技",35},
				{"政地技",7},
				{"物化技",35},
				{"史化技",16},
				{"史物技",13},
				{"史地技",28},
				{"地物技",9},
				{"地化技",28}
				
				};
	 Object[][] orginSC=  {{"地化生",10},
				{"地物化",14},
				{"地物生",16},
				{"史地化",6},
				{"史地生",20},
				{"史地物",16},
				{"史化生",16},
				{"史物化",26},
				{"史物生",27},
				{"物化生",106},
				{"政地化",1},
				{"政地生",9},
				{"政地物",4},
				{"政化生",6},
				{"政史地",49},
				{"政史化",7},
				{"政史生",27},
				{"政史物",9},
				{"政物化",13},
				{"政物生",12}};
//				
//	 			
//	Object[][] orginSC11=  {{"地化生",0},
//				{"地物化",0},
//				{"地物生",32},
//				{"史地化",32},
//				{"史地生",34},
//				{"史地物",32},
//				{"史化生",0},
//				{"史物化",113},
//				{"史物生",0},
//				{"物化生",139},
//				{"政地化",0},
//				{"政地生",0},
//				{"政地物",0},
//				{"政化生",0},
//				{"政史地",111},
//				{"政史化",0},
//				{"政史生",0},
//				{"政史物",32},
//				{"政物化",0},
//				{"政物生",3}};
		PlacementTaskConfig config = new PlacementTaskConfig(50,55,optSubjectLessonsMap,proSubjectLessonsMap,SelectType.ThreeFromSix);
		Map<String,Integer> subjectCombinationsMap = new HashMap();
		for(Object[] one:orginSC){
			String scName = one[0].toString();
			for(Map.Entry<Integer, String> entry : subjectIdMap.entrySet()){
				scName = scName.replace(entry.getValue(), entry.getKey()+",");
			}
			scName = scName.substring(0, scName.length()-1);
			subjectCombinationsMap.put(scName,(Integer)one[1]);
		}

		test(subjectCombinationsMap, config);
	}
	
	public static void test(Map<String,Integer> subjectCombinationsMap,PlacementTaskConfig config){
		Map<Integer,Subject>[] subjectIdMaps = new Map[]{new HashMap(),new HashMap()};
		List<Tclass> allTclassList = new ArrayList();
		//统计各科人数及科目和组合Map映射
		List<SubjectCombination> subjectCombinations = new ArrayList();
		for(Map.Entry<String,Integer> entry:subjectCombinationsMap.entrySet()){
			SubjectCombination subjectCombination =  new SubjectCombination(entry.getKey(),entry.getValue(),config);
			subjectCombinations.add(subjectCombination);
			for(Integer intCd:new Integer[]{CD_OPT,CD_PRO}){
				//统计学考选考科目人数
				for(Integer optSubjectId:subjectCombination.subjectIds[intCd]){
					Subject currentSubjectBean = subjectIdMaps[intCd].get(optSubjectId);
					if(currentSubjectBean==null){
						currentSubjectBean = new Subject(optSubjectId,intCd,config);
						subjectIdMaps[intCd].put(optSubjectId,currentSubjectBean);
					}
					currentSubjectBean.studentCount+=subjectCombination.getTotalStudentCount();
					currentSubjectBean.subjectCombinationList.add(subjectCombination);
				}
			}
		}
		//计算学选交叉
		List<List<Conflict>> allConflicts = Conflict.conflictStats(config, subjectIdMaps);
		if(allConflicts.size()==0){
			allConflicts.add(new ArrayList());
		}
		Set<Map<Integer,Integer[]>[]> seqTclassCountMapsSet = new TreeSet(new Comparator<Map<Integer,Integer[]>[]>(){
			@Override
			public int compare(Map<Integer, Integer[]>[] maps1,
					Map<Integer, Integer[]>[] maps2) {
				// TODO Auto-generated method stub
				int result = 0;
				for(Integer intCd:new Integer[]{CD_OPT,CD_PRO}){
					for(Map.Entry<Integer, Integer[]> entry1:maps1[intCd].entrySet()){
						if(!JSONObject.toJSONString(entry1.getValue()).equals(JSONObject.toJSONString(maps2[intCd].get(entry1.getKey())))){
							return new Integer(maps1.hashCode()).compareTo(maps2.hashCode());
						}
					}
				}
				return result;
		}});
		

	    Map<SubjectCombination,SubSubjectCombination[][]> computeMap =  new HashMap();
	    allConflicts.get(1).get(1).seqs[0]= new ArrayList(Arrays.asList(new Integer[]{3,2,1}));
	    compute(subjectCombinations,allConflicts.get(1),computeMap,config);
		for(SubjectCombination subjectCombination:subjectCombinations){
			System.out.println("--------------------------------");
			System.out.println(subjectCombination.getSubjectIdsStr()+"  "+subjectCombination.getConfictType());
			System.out.println(JSONObject.toJSONString(subjectCombination.getOrderOptSubjectIds())+"  "
							+JSONObject.toJSONString(subjectCombination.getOrderProSubjectIds()));
			SubSubjectCombination[][] subComs = computeMap.get(subjectCombination);
			//System.out.println("");
			if(subComs!=null){
				for(int seqIndex = 0;seqIndex < config.seqCounts[CD_PRO];seqIndex++){
					for(int subjectIndex = 0;subjectIndex < config.seqCounts[CD_PRO];subjectIndex++){
						if(subComs[seqIndex][subjectIndex]==null){
							System.out.print("(null),");
						}else{
							System.out.print(subComs[seqIndex][subjectIndex]+",");
						}
						
					}
					System.out.println("");
				}
			}
			System.out.println("---INDEX--");
			if(subComs!=null){
				for(int seqIndex = 0;seqIndex < config.seqCounts[CD_PRO];seqIndex++){
					for(int subjectIndex = 0;subjectIndex < config.seqCounts[CD_PRO];subjectIndex++){
						if(subComs[seqIndex][subjectIndex]==null){
							System.out.print("(null),");
						}else{
							System.out.print("("+subComs[seqIndex][subjectIndex].subjectIndex+","+subComs[seqIndex][subjectIndex].seqIndex+")");
						}
						
					}
					System.out.println("");
				}
			}
			
		}
		
		System.out.println("------------------打印学选交叉--------------------");
		for(Conflict conflict:allConflicts.get(1)){
			System.out.println("-------------------");
			System.out.println("opt:"+JSONObject.toJSONString(conflict.subjectIds[1])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[1]));
			System.out.println("pro:"+JSONObject.toJSONString(conflict.subjectIds[0])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[0]));
			System.out.println("-------------------");
		}
		
	}
	
	/**
	 * 计算所有的学选交叉及冲突志愿的学考各课眼的排列
	 * @param subjectIdMaps
	 * @param subjectCombinations
	 * @param config
	 * @return
	 */
	public static  List<ConflictComputeResult> compute(Map<Integer,Subject>[] subjectIdMaps,List<SubjectCombination> subjectCombinations,PlacementTaskConfig config){
		List<List<Conflict>> allConflicts = Conflict.conflictStats(config, subjectIdMaps);
		List<ConflictComputeResult> conflictComputeResults = new ArrayList();
		for(List<Conflict> conflicts : allConflicts){
			ConflictComputeResult ccResult = null;
			int i = 0;
			List<List<Integer>> perms = new ArrayList(CommonUtils.getPermutation(Arrays.asList(new Integer[]{1,2,3})));
			if(config.selectType == SelectType.ThreeFromSeven){
				perms.addAll(CommonUtils.getPermutation(Arrays.asList(new Integer[]{1,3,4})));
			}
			do{
				Map<SubjectCombination,SubSubjectCombination[][]>  result = new HashMap();
				boolean isOk = ConflictComputer.compute(subjectCombinations,conflicts,result,config);
				if(isOk){
					ccResult = new ConflictComputeResult(conflicts,result,subjectCombinations);
					conflictComputeResults.add(ccResult);
					break;
				}else{
					if(conflicts.size()>1){
						Collections.shuffle(perms);
						for(int index = 0;index<conflicts.size()-1;index++){
							conflicts.get(index+1).seqs[CD_PRO]=new ArrayList(perms.get(index));
						}
					}else{
						break;
					}
					
				}
			}while(i<18);
		}
		return conflictComputeResults;
	}
	
	/**
	 * 
	 * @param subjectCombinations
	 * @param conflicts
	 * @return Map<SubjectCombination,SubSubjectCombination[seqIndex][subjectIndex]>
	 */
	public static boolean compute(List<SubjectCombination> subjectCombinations,
			List<Conflict> conflicts,
			Map<SubjectCombination,
			SubSubjectCombination[][]> result,
			PlacementTaskConfig config){
		if(conflicts.size()==0){
			return true;
		}
		SubSubjectCombination.subScMaps.get().clear();
		Conflict.statsConflictType(subjectCombinations, conflicts);
		//conflicts.get(1).seqs =  new List[]{Arrays.asList(new Integer[]{3,1,2}),Arrays.asList(new Integer[]{1,2,3})};
		boolean isAllOk = true;
		for(SubjectCombination subjectCombination:subjectCombinations){
			if(subjectCombination.getConfictType() == SubjectCombination.ConflictType.TYPE_0){
				continue;
			}
			Set<List<Integer>>  orderProSubjectsList = CommonUtils.getPermutation(subjectCombination.subjectIds[CD_PRO]);
			Set<List<Integer>>  orderOptSubjectsList = CommonUtils.getPermutation(subjectCombination.subjectIds[CD_OPT]);
			boolean isOk = false;
			boolean isType1Good = false;
			List<Integer> orderOptSubjectIds  = new ArrayList(subjectCombination.getConflictSubjectIds()[CD_OPT]);
			List<Integer> tmpSubjectIds = new ArrayList(subjectCombination.subjectIds[CD_OPT]);
			tmpSubjectIds.removeAll(orderOptSubjectIds);
			orderOptSubjectIds.addAll(tmpSubjectIds);
			subjectCombination.setOrderOptSubjectIds(orderOptSubjectIds);
			SubSubjectCombination[][] resultSubComs = null;
			List<Integer> resultOrderProSubjects = null;
			for(List<Integer> orderProSubjects : orderProSubjectsList){	
				subjectCombination.setOrderProSubjectIds(orderProSubjects);
			//	System.out.println("subjectCombination:"+subjectCombination.getSubjectIdsStr()
			//			+"   orderProSubjectIds:"+JSONObject.toJSONString(subjectCombination.orderProSubjectIds)
			//			+"	orderOptSubjectIds"+JSONObject.toJSONString(subjectCombination.orderOptSubjectIds));
			//	System.out.println("orderProSubjectIds:"+JSONObject.toJSONString(subjectCombination.orderProSubjectIds));
			//	System.out.println("orderOptSubjectIds:"+JSONObject.toJSONString(subjectCombination.orderOptSubjectIds));
			//	if(subjectCombination.getConfictType() != SubjectCombination.ConflictType.TYPE_3){
					//尝试所有科目都采用选考一种科目的序列
				subjectCombination.setConfictType(SubjectCombination.ConflictType.TYPE_1);
				for(Integer optSubjectId:orderOptSubjectIds){
					Integer subjectIndex = subjectCombination.getOrderOptSubjectIds().indexOf(optSubjectId);
					List<SubSubjectCombination[][]> subComsList = getInitOneSubjectMode(subjectIndex,subjectCombination,config.selectType);
					for(SubSubjectCombination[][] subComs:subComsList){
//						System.out.println("---------------------"+subjectCombination.getSubjectIdsStr()+"----type:"+" TYPE_1");
//						System.out.println("---------------------"+JSONObject.toJSONString(subjectCombination.getOrderOptSubjectIds())+"----"+JSONObject.toJSONString(subjectCombination.getOrderProSubjectIds()));
//						
//						for(int i=0;i<config.seqCounts[CD_PRO];i++){
//							for(int j=0;j<config.seqCounts[CD_PRO];j++){
//								if(subComs[i][j] == null){
//									System.out.print("(null) ");
//								}else{
//									System.out.print("("+subComs[i][j].subjectIndex+","+subComs[i][j].seqIndex+") ");
//								}
//							}
//							System.out.println("");
//						}
//						System.out.println("-----------------------------------------");
						if(checkConflict(subComs,subjectCombination,conflicts,config.seqCounts[CD_OPT])){
//							System.out.println("---------------------"+subjectCombination.getSubjectIdsStr()+"----type1 is ok");
							isOk = true;
							resultSubComs = subComs;
							resultOrderProSubjects = orderProSubjects;
						//	if(orderProSubjects.size()>subjectCombination.orderOptSubjectIds.size()){
							if(config.selectType==SelectType.ThreeFromSeven){
								for(int oneSeqIndex = 0;oneSeqIndex<config.seqCounts[CD_PRO];oneSeqIndex++){
									if(subComs[0][oneSeqIndex]==null){
										continue;
									}
									if(subComs[3][oneSeqIndex]!=null){
										continue;
									}
									if(checkConflict(subComs[0][oneSeqIndex],orderProSubjects.get(orderProSubjects.size()-1),oneSeqIndex,conflicts)){
										isType1Good = true;
										break;
									}
								}
								if(isType1Good){
									break;
								}
							}else{
								isType1Good = true;
								break;
							}
						};
					}
					if(isType1Good){
						break;
					}
				}
				if(isType1Good){
					break;
				}
			}
			if(isOk){
				result.put(subjectCombination, resultSubComs);
				subjectCombination.setConfictType(ConflictType.TYPE_1);
				subjectCombination.setOrderProSubjectIds(resultOrderProSubjects);
				continue;
			}
			
			//其他情况的
		    List<ConflictType> conflictTypes = Arrays.asList(new ConflictType[]{ConflictType.TYPE_2,ConflictType.TYPE_3,ConflictType.TYPE_4,ConflictType.TYPE_5,ConflictType.TYPE_6});    
		    for(int i=1;i<conflictTypes.size();i++){
				subjectCombination.setConfictType(conflictTypes.get(i));
				for(List<Integer> orderOptSubjects : orderOptSubjectsList){
					subjectCombination.setOrderOptSubjectIds(orderOptSubjects);
			    	for(List<Integer> orderProSubjects : orderProSubjectsList){
			    		subjectCombination.setOrderProSubjectIds(orderProSubjects);
			    		List<SubSubjectCombination[][]> subComsList = getInitMultiSubjectMode(subjectCombination,config.selectType);
						for(SubSubjectCombination[][] subComs:subComsList){
//							System.out.println("-------------------"+subjectCombination.getSubjectIdsStr()+"----type:"+subjectCombination.getConfictType()+"--"+JSONObject.toJSONString(subjectCombination.getOrderOptSubjectIds())+"----"+JSONObject.toJSONString(subjectCombination.getOrderProSubjectIds()));
//							for(int k=0;k<config.seqCounts[CD_PRO];k++){
//								for(int j=0;j<config.seqCounts[CD_PRO];j++){
//									if(subComs[k][j] == null){
//										System.out.print("(null) ");
//									}else{
//										System.out.print("("+subComs[k][j].subjectIndex+","+subComs[k][j].seqIndex+") ");
//									}
//								}
//								System.out.println("");
//							}
							if(checkConflict(subComs,subjectCombination,conflicts,config.seqCounts[CD_OPT])){
								isOk = true;
								result.put(subjectCombination, subComs);
								break;
							}
						}
						if(isOk){
							break;
						}
					}
					if(isOk){
						break;
					}
				}
				if(isOk){
					break;
				}
			}
			if(!isOk){
				System.out.println(subjectCombination.getSubjectIdsStr());
	//			subjectCombination.confictType = ConflictType.TYPE_5;
				isAllOk = false;
				break;
			}
		}
//		for(Conflict conflict :conflicts){
//			System.out.println("-------------------");
//			System.out.println("opt:"+JSONObject.toJSONString(conflict.subjectIds[1])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[1]));
//			System.out.println("pro:"+JSONObject.toJSONString(conflict.subjectIds[0])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[0]));
//			System.out.println("-------------------");
//		}
		System.out.println("isAllOk:"+isAllOk);
		return isAllOk;
	}
	
	/**
	 * 检查单个子志愿是否可以放入学考指定科目，指定序列
	 * @param subCom
	 * @param proSubjectId
	 * @param proSeqIndex
	 * @param conflicts
	 * @return
	 */
	public static boolean checkConflict(SubSubjectCombination subCom,Integer proSubjectId,Integer proSeqIndex, List<Conflict> conflicts){
		if(subCom ==null){
			return true;
		}
		SubjectCombination oneSubjectCombination = subCom.parent;
		for(Conflict conflict:conflicts){
			if(!conflict.subjectIds[CD_PRO].contains(proSubjectId)){
				continue;
			}
			if(!conflict.seqs[CD_PRO].contains(proSeqIndex+1)){
				continue;
			}
			int optSeqIndex = conflict.seqs[CD_OPT].get(conflict.seqs[CD_PRO].indexOf(proSeqIndex+1))-1;
			List<Integer> optSubjectIds = new ArrayList(conflict.subjectIds[CD_OPT]);
			optSubjectIds.retainAll(oneSubjectCombination.subjectIds[CD_OPT]);
			for(Integer optSubjectId:optSubjectIds){
				Integer	optSubjectIdIndex = oneSubjectCombination.getOrderOptSubjectIds().indexOf(optSubjectId);
			//	System.out.println("pro:("+subCom.subjectIndex+" ,"+subCom.seqIndex+" ), opt:("+optSubjectIdIndex+" ,"+optSeqIndex+" )");
				if((subCom.seqIndex != optSeqIndex && subCom.subjectIndex != optSubjectIdIndex)
						||(subCom.seqIndex == optSeqIndex && subCom.subjectIndex == optSubjectIdIndex)){
					if(oneSubjectCombination.getConfictType() == ConflictType.TYPE_3 ){
						if(subCom.subType == SubSubjectCombinationType.ALL){
							return false;
						}
						String proFlag = ConflictType.type3Model[subCom.subjectIndex][subCom.seqIndex];
						String optFlag = ConflictType.type3Model[optSubjectIdIndex][optSeqIndex];
						if(proFlag.equals(optFlag)){
							return false;
						}else{
							boolean isOK = false;
							for(int i=0;i<3;i++){
								if(ArrayUtils.contains(ConflictType.type3Model[i], proFlag)&&ArrayUtils.contains(ConflictType.type3Model[i], optFlag)){
									isOK = true;
									break;
								}
							}
							if(!isOK){
								return false;
							}
						}		
					}else if(oneSubjectCombination.getConfictType() == ConflictType.TYPE_4){
							if(subCom.subType == SubSubjectCombinationType.ALL){
								return false;
							}
							String proFlag = ConflictType.type4Model[subCom.subjectIndex][subCom.seqIndex];
							String optFlag = ConflictType.type4Model[optSubjectIdIndex][optSeqIndex];
							if(proFlag.equals(optFlag)){
								return false;
							}else{
								boolean isOK = false;
								if(ArrayUtils.contains(ConflictType.type4Model[0], proFlag)&&ArrayUtils.contains(ConflictType.type3Model[0], optFlag)){
									isOK = true;
								}
								if(!isOK){
									return false;
								}
							}
					}else if(oneSubjectCombination.getConfictType() == ConflictType.TYPE_5){
						boolean isOK = false;
						String optFlag = ConflictType.type5Model[optSubjectIdIndex][optSeqIndex];
						if(optFlag.equals("0")){
							isOK=true;
							continue;
						}
						if(!isOK && subCom.subType == SubSubjectCombinationType.ALL){
							return false;
						}
						String proFlag = ConflictType.type5Model[subCom.subjectIndex][subCom.seqIndex];
						if(proFlag.equals("0")){
							isOK=true;
						}else if((proFlag.equals("x")&&proFlag.equals("y"))
								||(proFlag.equals("y")&&proFlag.equals("x"))){
							isOK = true;
						}
						if(!isOK){
							return false;
						}
					}else if(oneSubjectCombination.getConfictType() == ConflictType.TYPE_6){
						String optFlag = ConflictType.type6Model[optSubjectIdIndex][optSeqIndex];
						//System.out.println("optFlag:"+optFlag);
						boolean isOK = false;
						if(optFlag.equals("0")){
							isOK=true;
							continue;
						}
						if(!isOK && subCom.subType == SubSubjectCombinationType.ALL){
							return false;
						}
						String proFlag = ConflictType.type6Model[subCom.subjectIndex][subCom.seqIndex];
						//System.out.println("proFlag:"+proFlag);
						if(proFlag.equals("0")){
							isOK=true;
						}
						if(!isOK){
						//	System.out.print("optSeq:"+optSeq+"  optSubjectIdIndex:"+optSubjectIdIndex);
						//	System.out.print("proSeq:"+proSeq+"  proSubjectIdIndex:"+proSubjectIdIndex+"   proFlag："+proFlag+ " optFlag:"+optFlag);
							return false;
						}
					}else{
						//	System.out.println("pro:("+subCom.subjectIndex+" ,"+subCom.seqIndex+" ), opt:("+optSubjectIdIndex+" ,"+optSeqIndex+" )冲突冲突");
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 检查学考子志愿组合是否可行
	 * @param subCom
	 * @param proSubjectId
	 * @param seqIndex
	 * @param conflicts
	 * @return
	 */
	public static boolean checkConflict(SubSubjectCombination[][] subComs,
			SubjectCombination oneSubjectCombination,
			List<Conflict> conflicts,
			final int OPT_SUBJECT_NUM){
		boolean result = true;
		//检查学选交叉	
		for(Integer proSubjectId:oneSubjectCombination.getConflictSubjectIds()[CD_PRO]){
			for(int proSeqIndex = 0;proSeqIndex < OPT_SUBJECT_NUM;proSeqIndex++){
				Integer proSubjectIdIndex = oneSubjectCombination.getOrderProSubjectIds().indexOf(proSubjectId);
				if(!checkConflict(subComs[proSeqIndex][proSubjectIdIndex],proSubjectId,proSeqIndex,conflicts)){
					return false;
				}
			}
		}
		return result;
	}
	

	/*
	 * 获取单选考所有序列的子志愿组合在选考科目中所有的排列
	 */
	public static List<SubSubjectCombination[][]> getInitOneSubjectMode(Integer subjectIndex,SubjectCombination subjectCombination,SelectType selectType){
		List<SubSubjectCombination[][]> results = new ArrayList();
		Integer[][] rows = new Integer[][]{{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
		for(Integer[] firstRow:rows){
			for(Integer[] secondRow:rows){
				if(firstRow[0]==secondRow[0]||firstRow[1]==secondRow[1]||firstRow[2]==secondRow[2]){
					continue;
				}
				for(Integer[] thirdRow:rows){
					if(firstRow[0]==thirdRow[0]||firstRow[1]==thirdRow[1]||firstRow[2]==thirdRow[2]
							||secondRow[0]==thirdRow[0]||secondRow[1]==thirdRow[1]||secondRow[2]==thirdRow[2]){
						continue;
					}
					SubSubjectCombination[][] subComs = new SubSubjectCombination[3][3];
					Integer[][] currentRows = new Integer[][]{firstRow,secondRow,thirdRow};
					for(int i=0;i<3;i++){
						for(int j=0;j<3;j++){
							SubSubjectCombination subCom = SubSubjectCombination.getInstances(subjectCombination,subjectIndex, currentRows[i][j],SubSubjectCombinationType.NORMAL);
							subComs[i][j]=subCom;
						}
					}
					results.add(subComs);
					break;
				}
			}
		}
		if(selectType==SelectType.ThreeFromSeven){
			List<SubSubjectCombination[][]> results7X3 = new ArrayList();
			for(SubSubjectCombination[][] subComs:results){
				for(Integer splusSeq=0;splusSeq<4;splusSeq++){
					SubSubjectCombination[][] newSubComs = new SubSubjectCombination[4][4];
					int i =0;
					for(int seq =0;seq<4;seq++){
						if(splusSeq==seq){
							newSubComs[seq][3] =  SubSubjectCombination.getInstances(subjectCombination,-1, -1,SubSubjectCombinationType.ALL);
							continue;
						}
						for(int index = 0;index<3;index++){
							newSubComs[seq][index] = subComs[i][index];
						}
						i++;
					}
					results7X3.add(newSubComs);
				}
			}
			results = results7X3;
		}
		return results;
	}
	
	/*
	 * 获取单选考所有序列的子志愿组合在选考科目中所有的排列
	 */
	public static List<SubSubjectCombination[][]> getInitMultiSubjectMode(SubjectCombination subjectCombination,SelectType selectType){
		List<SubSubjectCombination[][]> results = new ArrayList();
		Set<List<Integer>> perms = CommonUtils.getPermutation(subjectCombination.subjectIds[CD_OPT]);
		for(List<Integer> perm:perms){
			Set<List<Integer>> perms2 = CommonUtils.getPermutation(Arrays.asList(new Integer[]{0,1,2}));
			for(List<Integer> perm2:perms2){
				SubSubjectCombination[][] subComs = new SubSubjectCombination[3][3];
				for(int seqIndex=0;seqIndex<3;seqIndex++){
					for(int subjectIndex=0;subjectIndex<3;subjectIndex++){
						SubSubjectCombination sc = SubSubjectCombination.getInstances(subjectCombination, subjectCombination.getOrderOptSubjectIds().indexOf(perm.get(subjectIndex)),perm2.get(seqIndex),SubSubjectCombinationType.NORMAL);
						subComs[seqIndex][subjectIndex] = sc;
					}	
				}
				results.add(subComs);
//	
//					System.out.println("-----------------------------------------");
//					System.out.println("-----orderOptSubjectIds:"+JSONObject.toJSONString(subjectCombination.orderOptSubjectIds));
//					System.out.println("-----orderProSubjectIds:"+JSONObject.toJSONString(subjectCombination.orderProSubjectIds));
//					for(int k=0;k<3;k++){
//						System.out.println("("+subComs[k][0].subjectIndex+","+subComs[k][0].seqIndex+"),"
//								+"("+subComs[k][1].subjectIndex+","+subComs[k][1].seqIndex+"),"
//								+"("+subComs[k][2].subjectIndex+","+subComs[k][2].seqIndex+")");
//					}
//					System.out.println("-----------------------------------------");
			}
		}
		if(selectType==SelectType.ThreeFromSeven){
			List<SubSubjectCombination[][]> results7X3 = new ArrayList();
			for(SubSubjectCombination[][] subComs:results){
				for(Integer splusSeq=0;splusSeq<4;splusSeq++){
					SubSubjectCombination[][] newSubComs = new SubSubjectCombination[4][4];
					int i =0;
					for(int seq =0;seq<4;seq++){
						if(splusSeq==seq){
							newSubComs[seq][3] =  SubSubjectCombination.getInstances(subjectCombination,-1, -1,SubSubjectCombinationType.ALL);
							continue;
						}
						for(int index = 0;index<3;index++){
							newSubComs[seq][index] = subComs[i][index];
						}
						i++;
					}
					results7X3.add(newSubComs);
				}
			}
			results = results7X3;
		}
		return results;
	}
	
}
