package com.talkweb.placementtask.utils.newdzb2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import com.talkweb.placementtask.utils.newdzb2.NewDZBPlacementExcuter.PlacementTaskConfig;
import com.talkweb.placementtask.utils.newdzb2.SubSubjectCombination.SubSubjectCombinationType;
import com.talkweb.placementtask.utils.newdzb2.SubjectCombination.ConflictType;

public class SeqSubject{
	private static int CD_OPT = NewDZBPlacementExcuter.CD_OPT;
	private static int CD_PRO = NewDZBPlacementExcuter.CD_PRO;
	Integer seq = null;
	Subject subject = null;
	Map<SubjectCombination,Integer> fromSubjectCombinationMap = new HashMap();
	private Stack<Map<SubjectCombination,Integer>> bakFromSubjectCombinationMap = new Stack();
	public SeqSubject(Integer seq,Subject subject){
		this.seq = seq;
		this.subject = subject;
	}
	
	public void backupFromSubjectCombinationMap(){
	//	bakFromSubjectCombinationMap = fromSubjectCombinationMap;
		bakFromSubjectCombinationMap.push(new HashMap(fromSubjectCombinationMap));
	}
	
	public void restoreFromSubjectCombinationMap(){
		fromSubjectCombinationMap = bakFromSubjectCombinationMap.pop();
	}

	public void clearBackUpFromSubjectCombinationMap(){
		bakFromSubjectCombinationMap.pop();
	}
	/**
	 * 设置加入志愿组合
	 * @param subjectCombination
	 * @return
	 */
	public Integer getFromSubjectCombinationStuCount(SubjectCombination subjectCombination){
		Integer studentCount = null;
		if(subjectCombination instanceof SubSubjectCombination){
			SubSubjectCombination subSc = (SubSubjectCombination) subjectCombination;
			for(Entry<SubjectCombination, Integer> entry:this.fromSubjectCombinationMap.entrySet()){
				if(entry.getKey() instanceof SubSubjectCombination){
					SubSubjectCombination oneSub = (SubSubjectCombination) entry.getKey();
					if(oneSub.subjectId == subSc.subjectId && oneSub.seqIndex == subSc.seqIndex){
						studentCount  = entry.getValue();
						break;
					}
				}
			}
		}else{
			studentCount = this.getFromSubjectCombinationStuCount(subjectCombination);
		}
		return studentCount;
	}
	
	public void setFromSubjectCombinationStuCount(SubjectCombination subjectCombination,Integer studentCount){
		if(subjectCombination instanceof SubSubjectCombination){
			SubSubjectCombination subSc = (SubSubjectCombination) subjectCombination;
			Entry<SubjectCombination, Integer> targetEntry = null;
			for(Entry<SubjectCombination, Integer> entry:this.fromSubjectCombinationMap.entrySet()){
				if(entry.getKey() instanceof SubSubjectCombination){
					SubSubjectCombination oneSub = (SubSubjectCombination) entry.getKey();
					if(oneSub.subjectId == subSc.subjectId && oneSub.seqIndex == subSc.seqIndex){
						targetEntry = entry;
						break;
					}
				}
			}
			if(targetEntry==null){
				this.fromSubjectCombinationMap.put(subjectCombination,studentCount);
			}else{
				targetEntry.setValue(studentCount);
			}
		}else{
			this.fromSubjectCombinationMap.put(subjectCombination,studentCount);
		}
	}
	
	/**
	 * 
	 * @param subjectCombination
	 * @param conflicts
	 * @param isOpt
	 * @return 0.非学选交叉志愿组合  2-n 需要同时上课的科目数量
	 */
	private static Map<Conflict,List[]> getConflictSubjects(SubjectCombination subjectCombination,List<Conflict> conflicts,Subject subject){
		Map<Conflict,List[]> conflictSubjectMaps = new HashMap();
		Set<Integer> conflictSubjects = new HashSet();
		for(Conflict conflict:conflicts){
			List<Integer> optRetains = new ArrayList<Integer>(conflict.subjectIds[CD_OPT]);
			List<Integer> proRetains = new ArrayList<Integer>(conflict.subjectIds[CD_PRO]);
			
			optRetains.retainAll(subjectCombination.subjectIds[CD_OPT]);
			proRetains.retainAll(subjectCombination.subjectIds[CD_PRO]);
			if(optRetains.size()>0&&proRetains.size()>0&&conflict.subjectIds[subject.isOpt].contains(subject.subjectId)){
				List[] confictSubjects = new List[2];
				confictSubjects[CD_OPT] = optRetains;
				confictSubjects[CD_PRO] = proRetains;
				conflictSubjectMaps.put(conflict, confictSubjects);
			}	
		}
		return conflictSubjectMaps;
	}
	

	/**
	 * 
	 * @param subjectCombination
	 * @param conflicts
	 * @param isOpt
	 * @return 0.非学选交叉志愿组合  2-n 需要同时上课的科目数量
	 */
	private static int isConflictSubjectCombination(SubjectCombination subjectCombination,List<Conflict> conflicts){
		int result = 0;
		Set<Integer> conflictSubjects = new HashSet();
		for(Conflict conflict:conflicts){
			List<Integer> optRetains = new ArrayList<Integer>(conflict.subjectIds[CD_OPT]);
			List<Integer> proRetains = new ArrayList<Integer>(conflict.subjectIds[CD_PRO]);
			optRetains.retainAll(subjectCombination.subjectIds[CD_OPT]);
			proRetains.retainAll(subjectCombination.subjectIds[CD_PRO]);
			if(optRetains.size()>0&&proRetains.size()>0){
				result = 1;
				conflictSubjects.addAll(optRetains);
				conflictSubjects.addAll(proRetains);
			}	
		}
		if(result==1){
			result = conflictSubjects.size();
		}
		return result;
	}

	public static List<Integer> getConflictSubjects(SubjectCombination subjectCombination,List<Conflict> conflicts,int isOpt){
		Set<Integer> conflictSubjects = new HashSet();
		for(Conflict conflict:conflicts){
			conflictSubjects.addAll(conflict.subjectIds[isOpt]);
		}
		List<Integer> subjectList = subjectCombination.subjectIds[isOpt];
		subjectList.retainAll(conflictSubjects);
		return subjectList;
	}
	/**
	 * 检查志愿组合是否可以排下去
	 * @param subjectCombination
	 * @param conflicts
	 * @return
	 */
	public static boolean canArrangeProSubjectCombination(SubjectCombination subjectCombination,List<Conflict> conflicts,Map<Integer,Subject>[] subjectIdMaps){
		Integer[][] seqs ={{1,2,3},{1,3,2},{2,1,3},{2,3,1},{3,2,1},{3,1,2}};
		boolean result = false;
		for(int i=0;i<seqs.length;i++){
			boolean tempResult = true;
			for(int j=0;j<subjectCombination.subjectIds[CD_PRO].size();j++){
				if(!NewDZBPlacementExcuter.checkConflict(subjectCombination.subjectIds[CD_PRO].get(j), seqs[i][j], subjectCombination, conflicts, CD_PRO,subjectIdMaps)){
					tempResult = false ;
					break;
				}
			}
			if(tempResult){
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 根据学选交叉计算出最大添加人数
	 * @param subjectCombination
	 * @param seq
	 * @param subjectId
	 * @param currentCanAddCount
	 * @return
	 */
	public int getProCanAddCount(SubjectCombination subjectCombination,
			int seq,
			int subjectId,
			int currentCanAddCount,
			Map<Integer,Subject>[] subjectIdMaps,
			Map<SubjectCombination,SubSubjectCombination[][]>  subSubjectCombinationsMap,
			PlacementTaskConfig config){
		if(subjectCombination.getConfictType() == ConflictType.TYPE_0){
			return currentCanAddCount;
		}
		List<Integer> proSubjectIds  = subjectCombination.subjectIds[CD_PRO];
		SubSubjectCombination[][] subSubjectCombinations = subSubjectCombinationsMap.get(subjectCombination);
		int canAddCount = currentCanAddCount;
		for(Integer proSubjectId:proSubjectIds){
			Subject subject = subjectIdMaps[CD_PRO].get(proSubjectId);
			int subjectIndex = subjectCombination.getOrderProSubjectIds().indexOf(proSubjectId);
			for(int seqIndex = 0;seqIndex<config.seqCounts[CD_PRO];seqIndex++){
				SubSubjectCombination subSubjectCombination = subSubjectCombinations[seqIndex][subjectIndex];
				if(subSubjectCombination==null ||subSubjectCombination.subType == SubSubjectCombinationType.ALL ){
					continue;
				}
				if(subSubjectCombination.subjectId.equals(subjectId)&&
						subSubjectCombination.seqIndex.equals(seq)){
					canAddCount = Math.min(canAddCount, subject.avgTclassStudentCount*subject.seqTclassCount[seqIndex]-subject.seqMustArrangeStudents[seqIndex]);
				}
			}
		}
		return canAddCount;
	}
	
	/**
	 * 
	 * @param subjectCombination
	 * @param seq
	 * @param subjectId
	 * @param currentCanAddCount
	 * @param subjectIdMaps
	 * @param subSubjectCombinationsMap
	 * @return
	 */
	public static int getProMustAddCount(SubjectCombination subjectCombination,
			int seq,
			int subjectId,
			int mustCount,
			Map<Integer,Subject>[] subjectIdMaps,
			Map<SubjectCombination,SubSubjectCombination[][]>  subSubjectCombinationsMap,
			List<Integer> finishedSubjectIds,
			PlacementTaskConfig config){
		if(subjectCombination.getConfictType()==ConflictType.TYPE_0){
			return mustCount;
		}
		List<Integer> unfinishedSubjectIds = new ArrayList(subjectCombination.subjectIds[CD_OPT]);
		unfinishedSubjectIds.removeAll(finishedSubjectIds);
		unfinishedSubjectIds.remove(unfinishedSubjectIds.indexOf(subjectId));
		SubSubjectCombination[][] subSubjectCombinations = subSubjectCombinationsMap.get(subjectCombination);
		if(subSubjectCombinations==null){
			System.out.println(subjectCombination.getSubjectIdsStr() + " is null");
		}
		if(unfinishedSubjectIds.size()==2){
			Integer optSubjectId = unfinishedSubjectIds.get(0);
			for(int subjectIndex =0 ;subjectIndex <config.seqCounts[CD_PRO] ;subjectIndex ++){
				for(int seqIndex = 0;seqIndex<config.seqCounts[CD_PRO];seqIndex++){
					SubSubjectCombination subSubjectCombination = subSubjectCombinations[seqIndex][subjectIndex];
					if(subSubjectCombination == null || subSubjectCombination.subType == SubSubjectCombinationType.ALL){
						continue;
					}
					if(subSubjectCombination.subjectId.equals(optSubjectId)&&
							subSubjectCombination.seqIndex.equals(seq)){
						Subject proSubject = subjectIdMaps[CD_PRO].get(subjectCombination.getOrderProSubjectIds().get(subjectIndex));
				//		System.out.println("proSubject:"+proSubject.subjectId+" seq: "+(seqIndex+1)+" seqMustArrangeStudents:"+proSubject.seqMustArrangeStudents[seqIndex]);
						mustCount = Math.max(mustCount, proSubject.seqMustArrangeStudents[seqIndex]+subjectCombination.remainSeqStudentCounts[CD_OPT][seq-1]-proSubject.avgTclassStudentCount*proSubject.seqTclassCount[seqIndex]);
					}
				}
			}
		}
		if(seq==2){
			Integer optSubjectId = subjectId;
			for(int subjectIndex =0 ;subjectIndex < config.seqCounts[CD_PRO];subjectIndex ++){
				for(int seqIndex = 0;seqIndex<config.seqCounts[CD_PRO];seqIndex++){
					SubSubjectCombination subSubjectCombination = subSubjectCombinations[seqIndex][subjectIndex];
					if(subSubjectCombination == null || subSubjectCombination.subType == SubSubjectCombinationType.ALL){
						continue;
					}
					if(subSubjectCombination.subjectId.equals(optSubjectId)&&
							subSubjectCombination.seqIndex.equals(3)){
						Subject proSubject = subjectIdMaps[CD_PRO].get(subSubjectCombination.getOrderProSubjectIds().get(subjectIndex));
						mustCount = Math.max(mustCount, proSubject.seqMustArrangeStudents[seqIndex]+subjectCombination.remainSeqStudentCounts[CD_OPT][seq-1]-proSubject.avgTclassStudentCount*proSubject.seqTclassCount[seqIndex]);
					}
				}
			}
		}
		return mustCount;
	}
	
	/**
	 * 
	 */
	public void setProMustAddCount(SubjectCombination subjectCombination,
			int seq,
			int subjectId,
			int currentCanAddCount,
			Map<Integer,Subject>[] subjectIdMaps,
			Map<SubjectCombination,SubSubjectCombination[][]>  subSubjectCombinationsMap,
			PlacementTaskConfig config){
		if(subjectCombination.getConfictType() == ConflictType.TYPE_0){
			return;
		}
		List<Integer> proSubjectIds  = subjectCombination.subjectIds[CD_PRO];
		SubSubjectCombination[][] subSubjectCombinations = subSubjectCombinationsMap.get(subjectCombination);
		int canAddCount = currentCanAddCount;
		for(Integer proSubjectId:proSubjectIds){
			Subject subject = subjectIdMaps[CD_PRO].get(proSubjectId);
			int subjectIndex = subjectCombination.getOrderProSubjectIds().indexOf(proSubjectId);
			for(int seqIndex = 0;seqIndex<config.seqCounts[CD_PRO];seqIndex++){
				SubSubjectCombination subSubjectCombination = subSubjectCombinations[seqIndex][subjectIndex];
				if(subSubjectCombination == null || subSubjectCombination.subType == SubSubjectCombinationType.ALL){
					continue;
				}
				if(subSubjectCombination.subjectId.equals(subjectId)&&
						subSubjectCombination.seqIndex.equals(seq)){
					subject.seqMustArrangeStudents[seq-1] += canAddCount;
				}
			}
	
		}
		return ;
	}
	/**
	 * 
	 * @param subjectCombinations 所有组合
	 * @param mustSubjectCombinations 必须安排的组合
	 */
	public void addStudent(Map<Integer,Subject> subjectIdMap,
			List<Integer> seqFinishedSubjects,
			List<Conflict> conflicts,
			PlacementTaskConfig config,
			Map<Integer,Subject>[] subjectIdMaps,
			Map<SubjectCombination,SubSubjectCombination[][]>  subSubjectCombinationsMap){
		final int isOpt = this.subject.isOpt;
		//必须分配两个科目的志愿组合及人数
		Map<SubjectCombination,Integer> mustSubjectCombinations = new HashMap<SubjectCombination,Integer>();
		Map<SubjectCombination,Integer> canSubjectCombinations = new HashMap<SubjectCombination,Integer>();
		List<SubjectCombination> subjectCombinationList = new ArrayList(this.subject.subjectCombinationList);
		//System.out.println("排 seq:"+this.seq+" subjectId:"+this.subject.subjectId+"----------------------科目剩余人数:"+this.subject.remainStudentCount);
		Iterator<SubjectCombination> it = subjectCombinationList.iterator();
		while(it.hasNext()){
			SubjectCombination oneSubjectCombination = it.next();
			//System.out.println("-----   本序列下志愿组合["+oneSubjectCombination.subjectIdsStr+"("+JSONObject.toJSONString(oneSubjectCombination.subjectIds[0])+")]");
			if(!NewDZBPlacementExcuter.checkConflict(this.subject.subjectId,this.seq,oneSubjectCombination, conflicts,isOpt,subjectIdMaps)){
					it.remove();
				//	System.out.println("-----   本序列下志愿组合["+oneSubjectCombination.subjectIdsStr+"("+JSONObject.toJSONString(oneSubjectCombination.subjectIds[0])+")]学选交叉冲突");
					continue;
			}
			if(oneSubjectCombination.remainSubjectStudentCountMaps[this.subject.isOpt].get(this.subject.subjectId).equals(0)){
				it.remove();
				continue;
			}
			//取得该志愿组合未排完的科目
			List<Integer> unfinishedSubjects = oneSubjectCombination.getUnfinishedSubjects(isOpt);
			//本序列下该志愿组合可排的科目
			unfinishedSubjects.removeAll(seqFinishedSubjects);
		//	System.out.println("-----   本序列下志愿组合["+oneSubjectCombination.subjectIdsStr+"("+JSONObject.toJSONString(oneSubjectCombination.subjectIds[0])+")]可排"+oneSubjectCombination.remainSeqStudentCounts[this.subject.isOpt][this.seq-1]);
			if(oneSubjectCombination.remainSeqStudentCounts[isOpt][this.seq-1]>0){
				//学选交叉检查
				//如果除去此科目，这个志愿组合在本序列下人数不能排满的话，则为必选组合
				if(unfinishedSubjects.size() > 0 &&unfinishedSubjects.contains(this.subject.subjectId)){
					unfinishedSubjects.remove(this.subject.subjectId);
					int canArrangeStudentCount = 0;
					for(Integer subjectId:unfinishedSubjects){
						if(NewDZBPlacementExcuter.checkConflict(subjectId,this.seq,oneSubjectCombination, conflicts,isOpt,subjectIdMaps)){
							canArrangeStudentCount += oneSubjectCombination.remainSubjectStudentCountMaps[isOpt].get(subjectId);
						}
					}
					//该序列该科目，这个志愿组合必须安排的人数
					int mustArrangeStudentCount = oneSubjectCombination.remainSeqStudentCounts[isOpt][this.seq-1] - canArrangeStudentCount;
					if(isOpt==CD_OPT){
						mustArrangeStudentCount =  this.getProMustAddCount(oneSubjectCombination, seq, subject.subjectId, mustArrangeStudentCount, subjectIdMaps, subSubjectCombinationsMap,seqFinishedSubjects,config);
					}
					
//					System.out.println("-------"+oneSubjectCombination.subjectIdsStr+"("
//							+JSONObject.toJSONString(oneSubjectCombination.subjectIds[this.subject.isOpt])+")"
//							+ " mustArrangeStudentCount:"+mustArrangeStudentCount);
					if(mustArrangeStudentCount > 0){
						oneSubjectCombination.order = 2;
						mustSubjectCombinations.put(oneSubjectCombination,mustArrangeStudentCount);
						if(isOpt==CD_OPT){
							this.setProMustAddCount(oneSubjectCombination, seq, subject.subjectId, mustArrangeStudentCount, subjectIdMaps, subSubjectCombinationsMap,config);
							continue;
						}
					}else{
						oneSubjectCombination.order = 1;
					}
				}else{
					oneSubjectCombination.order = 0;
				}
				
				//学考的情况下
				if(this.subject.isOpt == CD_PRO && oneSubjectCombination instanceof SubSubjectCombination){
					//检查学选交叉是否为必选科目
					boolean isMustArrangeStudentAll = true;
					for(int oneSeq = this.seq+1; oneSeq <=config.seqCounts[CD_PRO] ;oneSeq++){
						if(NewDZBPlacementExcuter.checkConflict(this.subject.subjectId,oneSeq,oneSubjectCombination, conflicts,isOpt,subjectIdMaps)){
							isMustArrangeStudentAll = false;
						}
					}
					if(isMustArrangeStudentAll){
					//	System.out.println("-----   本序列下志愿组合["+oneSubjectCombination.subjectIdsStr+"("+JSONObject.toJSONString(oneSubjectCombination.subjectIds[0])+")]必排"+oneSubjectCombination.remainSeqStudentCounts[isOpt][this.seq-1]);
						mustSubjectCombinations.put(oneSubjectCombination,oneSubjectCombination.remainSeqStudentCounts[isOpt][this.seq-1]);
						oneSubjectCombination.order = 2;
						continue;
					}
					//检查其他科目是否可以在其他序列拍
					int canArrangeStudentCount = oneSubjectCombination.remainSeqStudentCounts[isOpt][this.seq-1];
					List<Integer> noCheckSubjectIds = new ArrayList(seqFinishedSubjects);
					noCheckSubjectIds.add(this.subject.subjectId);
				//	subjectIds.removeAll(new ArrayList(Arrays.asList(new Integer[]{this.subject.subjectId})));
					
				//	System.out.println("学课检查其他科目是否可排"+oneSubjectCombination.subjectIdsStr);
					// turn = 1 如果排了，是否其他的可排    ture=2 如果未排，是否其他序列可排
					for(int turn = 1;turn <=2 ;turn++){
						List<Integer> unConflictSubjectIds =new ArrayList(oneSubjectCombination.subjectIds[CD_PRO]);
						unConflictSubjectIds.removeAll(oneSubjectCombination.getConflictSubjectIds()[CD_PRO]);
						List<List<Integer>> conflictSortedSubjectIdsPerms =  new ArrayList(oneSubjectCombination.getConflictSubjectIds()[CD_PRO]);
						int remainCount =canArrangeStudentCount;
						for(int permIndex = 0;permIndex<conflictSortedSubjectIdsPerms.size();permIndex++){
							Map<Integer,Integer> tmpRemainStudentCount = new HashMap();
							Integer[] canArrangeStudentCounts  =new Integer[config.seqCounts[CD_PRO]];
							List<Integer> conflictSortedSubjectIds  = new ArrayList(oneSubjectCombination.getConflictSubjectIds()[CD_PRO]);
							conflictSortedSubjectIds.addAll(unConflictSubjectIds);
							for(Integer oneSubjectId:conflictSortedSubjectIds){
								if(oneSubjectId==this.subject.subjectId&&turn==1){
									tmpRemainStudentCount.put(oneSubjectId, oneSubjectCombination.remainSubjectStudentCountMaps[CD_PRO].get(oneSubjectId)-canArrangeStudentCount);
								}else{
									tmpRemainStudentCount.put(oneSubjectId, oneSubjectCombination.remainSubjectStudentCountMaps[CD_PRO].get(oneSubjectId));
								}
							}
							for(int oneSeq = 1; oneSeq <=config.seqCounts[CD_PRO] ;oneSeq++){
								if(oneSeq == this.seq&&turn==1){
									canArrangeStudentCounts[oneSeq-1]=oneSubjectCombination.remainSeqStudentCounts[CD_PRO][oneSeq-1]-canArrangeStudentCount;
								}else if(oneSeq < this.seq){
									canArrangeStudentCounts[oneSeq-1]= 0;
								}else{
									canArrangeStudentCounts[oneSeq-1]=oneSubjectCombination.remainSeqStudentCounts[CD_PRO][oneSeq-1];
								}
							}
						//	System.out.println("-------学课检查顺序："+JSONObject.toJSONString(conflictSortedSubjectIds)+"  冲突的科目："+JSONObject.toJSONString(oneSubjectCombination.conflictSubjectIds[CD_PRO]));
							for(Integer oneSubjectId:conflictSortedSubjectIds){
								isMustArrangeStudentAll = true;
								if(tmpRemainStudentCount.get(oneSubjectId)==0){
									continue;
								}
								for(int oneSeq = this.seq; oneSeq <=config.seqCounts[CD_PRO] ;oneSeq++){
									if( oneSeq == this.seq && noCheckSubjectIds.contains(oneSubjectId)){
										continue;
									}
									if(NewDZBPlacementExcuter.checkConflict(oneSubjectId,oneSeq,oneSubjectCombination, conflicts,isOpt,subjectIdMaps)){
										int studentCount= Math.min(canArrangeStudentCounts[oneSeq-1], tmpRemainStudentCount.get(oneSubjectId));
										canArrangeStudentCounts[oneSeq-1]-=studentCount;
										tmpRemainStudentCount.put(oneSubjectId, tmpRemainStudentCount.get(oneSubjectId)-studentCount);
							//			System.out.println("-----------subjectId:"+oneSubjectId+"  seq:"+oneSeq+"  排："+studentCount+"人 科目剩下："+ tmpRemainStudentCount.get(oneSubjectId)+"人");
									}else{
							//			System.out.println("-----------subjectId:"+oneSubjectId+"  seq:"+oneSeq+"冲突不能排");
									}
								}
		//						if(isMustArrangeStudentAll){
		//							canArrangeStudentCount -=oneSubjectCombination.remainSubjectStudentCountMaps[CD_PRO].get(oneSubjectId);
		//						}
							}
							
							int tmpRemainCount = 0;
							for(Integer oneSubjectRemain:tmpRemainStudentCount.values()){
								if(oneSubjectRemain>0){
									tmpRemainCount+=oneSubjectRemain;
								}
							}
							remainCount = Math.min(remainCount, tmpRemainCount);
							if(remainCount<=0){
								break;
							}
					//		System.out.println(oneSubjectCombination.subjectIdsStr+"   subjectId:"+this.subject.subjectId+"  seq:"+this.seq+"   isMustArrangeStudentAll:"+isMustArrangeStudentAll+" canArrangeStudentCount:"+canArrangeStudentCount+"   remainCount:"+remainCount+"  结果:"+JSONObject.toJSONString(tmpRemainStudentCount));
						}
						
						if(turn ==1){
							if(remainCount>0){
								canArrangeStudentCount = Math.max(canArrangeStudentCount-remainCount,0);
							}
							if(canArrangeStudentCount<=0){
							//	System.out.println("移除志愿组合"+oneSubjectCombination.subjectIdsStr);
								it.remove();
								break;
							}else{
								canSubjectCombinations.put(oneSubjectCombination, canArrangeStudentCount);
							}
						}else if(turn ==2){
							if(remainCount>0){
								int mustCount = Math.min(oneSubjectCombination.remainSeqStudentCounts[CD_PRO][this.seq-1],Math.abs(remainCount));
								mustSubjectCombinations.put(oneSubjectCombination,mustCount);
								oneSubjectCombination.order = 2;
								canSubjectCombinations.put(oneSubjectCombination, mustCount);
								break;
							}
					
						}
					}
				}
			}else{
				it.remove();
			}
		}
		final int currentSeq = this.seq;
		final List<Conflict> finalConflicts = conflicts;
		final int finalIsOpt = isOpt;
		//按已排科目数排序
		Collections.sort(subjectCombinationList, new Comparator<SubjectCombination>(){
			@Override
			public int compare(SubjectCombination sc1,
					SubjectCombination sc2) {
				// TODO Auto-generated method stub
				
				int result = Integer.compare(sc2.order,sc1.order);
				if(result==0){
					result =  sc2.getConfictType().compareTo(sc1.getConfictType());
					if(result==0){
						result = Integer.compare(sc2.remainSeqStudentCounts[isOpt][currentSeq-1], sc1.remainSeqStudentCounts[isOpt][currentSeq-1]);
					}
				}
				return result;
			}	
		});
		
		int mustStudentCount = CommonUtils.sum(mustSubjectCombinations.values());
		int studentCount = 0;
		if(this.seq == config.seqCounts[subject.isOpt]){
			studentCount = this.subject.remainStudentCount;
		}else{
			if(this.seq == 1){
				 studentCount = CommonUtils.sum(CommonUtils.getRandomData(subject.studentCount,subject.tclassCount,this.subject.seqTclassCount[this.seq-1]));
				 studentCount -= this.getStudentCount();
			}else if(this.seq ==2 ){
				studentCount = CommonUtils.sum(CommonUtils.getRandomData(subject.studentCount-subject.seqSubjects[0].getStudentCount(),subject.tclassCount-this.subject.seqTclassCount[0],this.subject.seqTclassCount[this.seq-1]));
				studentCount -= this.getStudentCount();
			}else if(this.seq ==3){
				studentCount = CommonUtils.sum(CommonUtils.getRandomData(subject.studentCount-(subject.seqSubjects[0].getStudentCount()+subject.seqSubjects[1].getStudentCount()),subject.tclassCount-(this.subject.seqTclassCount[0]+this.subject.seqTclassCount[1]),this.subject.seqTclassCount[this.seq-1]));
				studentCount -= this.getStudentCount();
			}
			studentCount = Math.max(studentCount, mustStudentCount);
		}
		
//	System.out.println("---seq:"+this.seq+"  subjectId:"+this.subject.subjectId+"  mustStudentCount:"+mustStudentCount+" studentCount:"+studentCount+"   this.subject.remainStudentCount:"+ this.subject.remainStudentCount);
//	System.out.print("所有必排志愿组合   ");
//	for(Map.Entry<SubjectCombination, Integer> one:mustSubjectCombinations.entrySet()){
//		System.out.print("  "+one.getKey().getSubjectIdsStr()+":"+one.getValue());
//	}
//	System.out.println("");
	//		studentCount = Math.max(studentCount, mustStudentCount);
		for(SubjectCombination subjectCombination:subjectCombinationList){
				int addStudentCount = 0;
				Map<Integer,Integer> remainSubjectStudentCountMap = subjectCombination.remainSubjectStudentCountMaps[this.subject.isOpt];
				if(this.seq!=config.seqCounts[subject.isOpt]){
					//志愿组合在本科目本序列下可以拿出的人数
					int canAddStudentCount = Math.min(subjectCombination.remainSeqStudentCounts[this.subject.isOpt][this.seq-1], remainSubjectStudentCountMap.get(this.subject.subjectId));

					addStudentCount = Math.min(canAddStudentCount, studentCount);
					if(addStudentCount==0){
						continue;
					}
					if(mustSubjectCombinations.containsKey(subjectCombination)){
				//		System.out.println("-------   本序列下志愿组合["+subjectCombination.subjectIdsStr+"("+JSONObject.toJSONString(subjectCombination.subjectIds[0])+")]必排："+mustSubjectCombinations.get(subjectCombination)+"  addStudentCount:"+addStudentCount);
						addStudentCount = Math.min(mustSubjectCombinations.get(subjectCombination), addStudentCount);
					}else if(seqFinishedSubjects.size()<=5){
						//检查本序列剩下本志愿组合科目是否能排满
						for(Integer oneSubjectId:subjectCombination.subjectIds[this.subject.isOpt]){
							//选课的情况下，检查学课的学选交叉科目对应序列能够排满
							Subject oneSubject = subjectIdMap.get(oneSubjectId);
							//不是本科目并且不是已完成的科目
							if(!oneSubjectId.equals(this.subject.subjectId)&&!seqFinishedSubjects.contains(oneSubjectId)){
								int minSubjectSeqStudentCount = oneSubject.getRemainSeqStudentCount(this.seq,conflicts,subjectIdMaps)-oneSubject.avgTclassStudentCount * oneSubject.seqTclassCount[this.seq-1];
								addStudentCount  = Math.min(addStudentCount, minSubjectSeqStudentCount);
							}
						}
						if(isOpt == CD_OPT){
							addStudentCount = this.getProCanAddCount(subjectCombination, seq, subject.subjectId, addStudentCount, subjectIdMaps, subSubjectCombinationsMap,config);
						}
					}
					//学选交叉检查可以安排最多人数，避免影响同志愿的其他科目
					if(isOpt == CD_PRO){
						Integer canArrangeStudentCount = canSubjectCombinations.get(subjectCombination);
						if(canArrangeStudentCount!=null){
							addStudentCount  = Math.min(addStudentCount, canArrangeStudentCount);
						}
					}
				}else{
					addStudentCount =  Math.min(remainSubjectStudentCountMap.get(this.subject.subjectId), subjectCombination.remainSeqStudentCounts[this.subject.isOpt][this.seq-1]);
				}
			//	System.out.println("----------"+subjectCombination.subjectIdsStr+"   addStudentCount:"+addStudentCount);
				if(addStudentCount> 0){
					this.fromSubjectCombinationMap.put(subjectCombination, addStudentCount);
					subjectCombination.remainSeqStudentCounts[this.subject.isOpt][this.seq-1] -= addStudentCount;
					remainSubjectStudentCountMap.put(this.subject.subjectId, remainSubjectStudentCountMap.get(this.subject.subjectId)-addStudentCount);
					studentCount -= addStudentCount;
					subject.remainStudentCount -= addStudentCount;
					if(isOpt==CD_OPT){
						subjectCombination.optSeqSubjectStudentCountMaps[seq-1].put(subject.subjectId, addStudentCount);
					}
				}
			//	System.out.println("----------"+subjectCombination.subjectIdsStr+"   addStudentCount:"+addStudentCount+" 拍完以后科目剩余人数："+subject.remainStudentCount+"   studentCount:"+studentCount);

				if(studentCount<=0){
					break;
				}
			}
		}

	public int getStudentCount(){
		int allCount = 0;
//		for(Map.Entry<SubjectCombination,Integer> entry:fromSubjectCombinationMap.entrySet()){
//			SubjectCombination  subjectCombination  = entry.getKey();
//			int count = entry.getValue();
//			if(subjectCombination instanceof SubSubjectCombination){
//				count = ((SubSubjectCombination)subjectCombination).getStudentCount();
//			}
//			allCount += count;
//		}
		allCount = CommonUtils.sum(fromSubjectCombinationMap.values());
		return allCount;
	}
}