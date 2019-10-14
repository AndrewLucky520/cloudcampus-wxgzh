package com.talkweb.placementtask.utils.newdzb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.utils.newdzb.NewDZBPlacementExcuter.PlacementTaskConfig;
import com.talkweb.placementtask.utils.newdzb.NewDZBPlacementExcuter.SelectType;
import com.talkweb.placementtask.utils.newdzb.SubjectCombination.ConflictType;

/**
 * 学选交叉
 */
public class Conflict{
	private static int CD_OPT = NewDZBPlacementExcuter.CD_OPT;
	private static int CD_PRO = NewDZBPlacementExcuter.CD_PRO;
	//同时上课的科目 subjectIds[CD_PRO]学考科目  subjectIds[CD_OPT]选考科目 
	List<Integer>[] subjectIds = new List[2];
	List<Integer>[] seqs =  new List[2];
	//[学选][序列]<subjectId，SeqSubject>
	Map<Integer,SeqSubject>[][] seqSubjects = new Map[2][4];
	private Conflict(){}
	public Conflict(List[] turn,List[] seqs){
		this.subjectIds = turn;
		this.seqs = seqs;
		for(int i = 0;i<2;i++){
			for(int j=0;j<3;j++){
				seqSubjects[i][j] = new HashMap();
			}
		}
	}
	public void clear(){
		for(int i = 0;i<2;i++){
			for(int j=0;j<3;j++){
				seqSubjects[i][j].clear();
			}
		}
	}
	public Conflict getConflictBean(){
		Conflict copy = new Conflict();
		copy.subjectIds = this.subjectIds;
		copy.seqs = this.seqs;
		return copy;
	}
	
	/**
	 * 计算冲突类型
	 * @param subjectCombinations
	 * @param conflicts
	 */
	public static void  statsConflictType(List<SubjectCombination> subjectCombinations,List<Conflict> conflicts){
		for(SubjectCombination sc:subjectCombinations){
			sc.setConflictSubjectIds(new Set[]{new HashSet(),new HashSet()});
			ConflictType conflictType = ConflictType.TYPE_0;
			Set<Integer> allOptSubjectSet  = new HashSet();
			Set<Integer> allProSubjectSet  = new HashSet();
			Set<Integer> threeSubjectIdSet = new HashSet();
			for(Conflict conflict:conflicts){
				List<Integer> optSubjectList = new ArrayList(conflict.subjectIds[CD_OPT]);
				List<Integer> proSubjectList = new ArrayList(conflict.subjectIds[CD_PRO]);
				optSubjectList.retainAll(sc.subjectIds[CD_OPT]);
				proSubjectList.retainAll(sc.subjectIds[CD_PRO]);
				if(optSubjectList.size()>0 &&proSubjectList.size() >0 ){
					if((optSubjectList.size()+proSubjectList.size())>=3){
						threeSubjectIdSet.addAll(optSubjectList);
						threeSubjectIdSet.addAll(proSubjectList);
					}else{
						conflictType = ConflictType.TYPE_1;
					}
					allOptSubjectSet.addAll(optSubjectList);
					allProSubjectSet.addAll(proSubjectList);
				}
			}
			if(threeSubjectIdSet.size()==3){
				conflictType = ConflictType.TYPE_2;
			}else if(threeSubjectIdSet.size()>3){
				conflictType = ConflictType.TYPE_3;
			}
			sc.setConfictType(conflictType);
			sc.getConflictSubjectIds()[CD_OPT].addAll(allOptSubjectSet);
			sc.getConflictSubjectIds()[CD_PRO].addAll(allProSubjectSet);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Conflict){
			Conflict target = (Conflict) obj;
			if(Arrays.deepEquals(target.subjectIds[0].toArray(), this.subjectIds[0].toArray())
					&& Arrays.deepEquals(target.subjectIds[1].toArray(), this.subjectIds[1].toArray())
					){
				return true;
			}
		}
		return super.equals(obj);
	}


	public List<SubjectCombination>  getFromSubjectCombinations(int cd,int seq){
		List<SubjectCombination> fromSubjectCombinations = new ArrayList();
		for(SeqSubject one:seqSubjects[cd][seq-1].values()){
			fromSubjectCombinations.addAll(one.fromSubjectCombinationMap.keySet());
		}
		return fromSubjectCombinations;
	}
	
	/**
	 * 是否可以加入序列组
	 * @param mainControl
	 * @param seqSubject
	 * @return
	 */
	public boolean canAdd(SubSubjectCombination subSubjectCombination,Integer subjectId,Integer seq,int isOpt){
		// System.out.println(JSONObject.toJSONString(seqSubjects[isOpt][seq-1])+"    |subjectId:"+subjectId);
		if(!subjectIds[isOpt].contains(subjectId)){
			return true;
		}
		int otherCd;
		if (isOpt == NewDZBPlacementExcuter.CD_PRO)
			otherCd = NewDZBPlacementExcuter.CD_OPT;
		else
			otherCd = NewDZBPlacementExcuter.CD_PRO;
		List<Integer> otherCdSubjects = new ArrayList(subSubjectCombination.parent.subjectIds[otherCd]);
		otherCdSubjects.retainAll(subjectIds[otherCd]);
		int seqIndex = seqs[isOpt].indexOf(seq);
		if(seqIndex==-1){
			return true;
		}
		int otherSeq =seqs[otherCd].get(seqIndex);
		if(otherSeq==-1){
			return true;
		}
//		System.out.println("----------------------------seqs:"+JSONObject.toJSONString(seqs[isOpt])+
//				"   otherSeqs:"+JSONObject.toJSONString(seqs[otherCd])
//				+"    seq:"+seq+"    otherSeq:"+otherSeq+ " otherCdSubjects:"+JSONObject.toJSONString(otherCdSubjects)+
//				"   this.getFromSubjectCombinations:"+this.getFromSubjectCombinations(otherCd, otherSeq));
		if(otherCdSubjects.size()==0){
			return true;
		}
//		StringBuffer sb = new StringBuffer();
//		for(SubjectCombination sc:this.getFromSubjectCombinations(otherCd, otherSeq)){
//			sb.append("("+sc.subjectIdsStr+"),  ");
//		}
	//	System.out.println("------------------------------"+sb.toString());
		if(!this.getFromSubjectCombinations(otherCd, otherSeq).contains(subSubjectCombination.parent)){
			return true;
		}
		for(SeqSubject one:seqSubjects[otherCd][otherSeq-1].values()){
			if((subSubjectCombination.getSubjectId().equals(subjectId)&&subSubjectCombination.seqIndex.equals(otherSeq))
					||(!subSubjectCombination.getSubjectId().equals(subjectId)&&!subSubjectCombination.seqIndex.equals(otherSeq))){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 加入序列科目
	 * @param seqSubject
	 */
	public void addSeqSubject(SeqSubject... addSeqSubjects){
		for(SeqSubject seqSubject:addSeqSubjects){
			if(this.seqSubjects[seqSubject.subject.isOpt][seqSubject.seq-1]==null){
				this.seqSubjects[seqSubject.subject.isOpt][seqSubject.seq-1] = new HashMap();
			}
			if(subjectIds[seqSubject.subject.isOpt].contains(seqSubject.subject.subjectId)){
				this.seqSubjects[seqSubject.subject.isOpt][seqSubject.seq-1].put(seqSubject.subject.subjectId, seqSubject);
			}
		}
	}
	
	
	public static void main2(String[] args){
		Map<Integer,String> subjectIdMap = new HashMap();
		subjectIdMap.put(4, "政");
		subjectIdMap.put(5, "史");
		subjectIdMap.put(6, "地");
		subjectIdMap.put(7, "物");
		subjectIdMap.put(8, "化");
		subjectIdMap.put(9, "生");
		subjectIdMap.put(19, "技");
		Map<Integer,Integer> optSubjectLessonsMap = new HashMap();
		optSubjectLessonsMap.put(4, 3);
		optSubjectLessonsMap.put(5, 5);
		optSubjectLessonsMap.put(6, 5);
		optSubjectLessonsMap.put(7, 3);
		optSubjectLessonsMap.put(8, 3);
		optSubjectLessonsMap.put(9, 3);
		
		Map<Integer,Integer> proSubjectLessonsMap = new HashMap();
		proSubjectLessonsMap.put(4, 2);
		proSubjectLessonsMap.put(5, 1);
		proSubjectLessonsMap.put(6, 1);
		proSubjectLessonsMap.put(7, 1);
		proSubjectLessonsMap.put(8, 1);
		proSubjectLessonsMap.put(9, 2);
		proSubjectLessonsMap.put(19, 2);
		PlacementTaskConfig config = new PlacementTaskConfig(50,55,optSubjectLessonsMap,proSubjectLessonsMap,SelectType.ThreeFromSix);
		Map<Integer,Subject>[] subjectIdMaps = new Map[]{new HashMap(),new HashMap()};
		List<Tclass> allTclassList = new ArrayList();
		Object[][] orginSC=  {{"地化生",28},
				{"地物化",26},
				{"地物生",9},
				{"史地化",11},
				{"史地生",26},
				{"史地物",7},
				{"史化生",16},
				{"史物化",13},
				{"史物生",181},
				{"物化生",35},
				{"政地化",29},
				{"政地生",17},
				{"政地物",7},
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
				{"史物",13},
				{"地物技",9},
				{"地化技",28}

				};
		
		//统计各科人数及科目和组合Map映射
		List<SubjectCombination> subjectCombinations = new ArrayList();
		Map<String,Integer> subjectCombinationsMap = new HashMap();
		for(Object[] one:orginSC){
			String scName = one[0].toString();
			for(Map.Entry<Integer, String> entry : subjectIdMap.entrySet()){
				scName = scName.replace(entry.getValue(), entry.getKey()+",");
			}
			scName = scName.substring(0, scName.length()-1);
			subjectCombinationsMap.put(scName,(Integer)one[1]);
		}
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
		
		List<List<Conflict>> allConflicts = conflictStats(config,subjectIdMaps);
		int i=0;
		for(List<Conflict> conflicts : allConflicts){
			System.out.println("--------------------------"+(i++)+"-----------------------");
			for(Conflict conflict :conflicts){
				System.out.println("-------------------");
				System.out.println("opt:"+JSONObject.toJSONString(conflict.subjectIds[1])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[1]));
				System.out.println("pro:"+JSONObject.toJSONString(conflict.subjectIds[0])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[0]));
				System.out.println("-------------------");
			}
		}
		System.out.println(allConflicts.size());
		
		
	}
	/**
	 * 计算学选交叉
	 * @param config
	 */
	public static List<List<Conflict>> conflictStats(PlacementTaskConfig config,Map<Integer,Subject>[] subjectIdMaps){
		//所有冲突情况
		List<List<Conflict>> allConflictsList = new ArrayList();
		//计算学选交叉
		//计算最小课时数
		int minLessons = 0;
		Map<Integer,Integer> tmpDiffMap = new HashMap();
		for(Map.Entry<Integer,Integer> entry:config.optSubjectIdLessonMap.entrySet()){
			tmpDiffMap.put(entry.getKey(), entry.getValue() - config.proSubjectIdLessonMap.get(entry.getKey()));
		}
		List<Map.Entry<Integer,Integer>> tmpDiffMapEntrys = new ArrayList(tmpDiffMap.entrySet());
		Collections.sort(tmpDiffMapEntrys, new Comparator<Map.Entry<Integer,Integer>>(){
			@Override
			public int compare(Entry<Integer, Integer> o1,
					Entry<Integer, Integer> o2) {
				// TODO Auto-generated method stub
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		for(int i=0;i<tmpDiffMapEntrys.size();i++){
			if(i<3){
				minLessons+=config.optSubjectIdLessonMap.get(tmpDiffMapEntrys.get(i).getKey());
			}else{
				minLessons+=config.proSubjectIdLessonMap.get(tmpDiffMapEntrys.get(i).getKey());
			}
		}
		System.out.println("--------minLessons:"+minLessons);
		//计算是否有交叉
		int[][] lessons = new int[2][2];
		lessons[CD_PRO][0]=Collections.max(config.proSubjectIdLessonMap.values());
		lessons[CD_PRO][1]=Collections.min(config.proSubjectIdLessonMap.values());
		lessons[CD_OPT][0]=Collections.max(config.optSubjectIdLessonMap.values());
		lessons[CD_OPT][1]=Collections.min(config.optSubjectIdLessonMap.values());
		int times = Math.max(lessons[CD_OPT][0]-lessons[CD_OPT][1] ,lessons[CD_PRO][0]-lessons[CD_PRO][1]);
		int minTimes = Math.min(lessons[CD_OPT][0]-lessons[CD_OPT][1] ,lessons[CD_PRO][0]-lessons[CD_PRO][1]);
		int confictLessons = lessons[CD_PRO][0]*config.seqCounts[CD_PRO] + lessons[CD_OPT][0]*config.seqCounts[CD_OPT]- minLessons;
		Map<Subject,List<List<Integer>>>[] subjectPermutationMaps = new Map[]{new HashMap(),new HashMap()};
		Set<Integer> subjectIdSet = new HashSet();
		if(lessons[CD_PRO][0]==lessons[CD_PRO][1]||lessons[CD_OPT][0]==lessons[CD_OPT][1]){
			return allConflictsList;
		}
		if(confictLessons>0){
			//计算是否有三科以上的科目要同时上课的学选交叉
			boolean isNeedOver3Subject = false;
			int splus = minLessons%config.seqCounts[CD_OPT];
			if(splus!=0&minTimes>=splus){
				isNeedOver3Subject = true;
			}
			List<Map<Integer,Integer>> subjectIdLessonMaps = new ArrayList();
			subjectIdLessonMaps.add(config.proSubjectIdLessonMap);
			subjectIdLessonMaps.add(config.optSubjectIdLessonMap);
			List<Map<Integer,Integer>> conflictSubjectIdLessonMaps = new ArrayList();
			conflictSubjectIdLessonMaps.add(new HashMap());
			conflictSubjectIdLessonMaps.add(new HashMap());
			List<Subject> allSubjects = new ArrayList();
			List<Integer> subjectPermutationSizes = new ArrayList();
			for(int cd = 0 ;cd < 2;cd++){
				Map<Integer,Integer> subjectIdLessonMap = subjectIdLessonMaps.get(cd);
				for(Map.Entry<Integer,Integer> entry: subjectIdLessonMap.entrySet()){
					int subjectSurplusLesson = entry.getValue() - lessons[cd][1];
					if(subjectSurplusLesson>0){
						List<Integer> onePermutation = new ArrayList(times);
						for(int i=0;i<times;i++){
							onePermutation.add(subjectSurplusLesson>0?entry.getKey():0);
							subjectSurplusLesson--;
						}
						Set<List<Integer>> onePermutations = CommonUtils.getPermutation(onePermutation);
						subjectPermutationMaps[cd].put(subjectIdMaps[cd].get(entry.getKey()), new ArrayList(onePermutations));
						allSubjects.add(subjectIdMaps[cd].get(entry.getKey()));
						subjectIdSet.add(entry.getKey());
						subjectPermutationSizes.add(onePermutations.size());
					}
				}
			}
		
			//序列所有的排列
		   List<List<Integer>> allSeqPerm = new ArrayList(CommonUtils.getPermutation(Arrays.asList(new Integer[]{1,2,3})));
		   //序列是否需要打乱 如果科目总数大于3 则需要打乱
		   boolean isNeedSeqDisorder = subjectIdSet.size()>config.seqCounts[CD_PRO];
		   //循环所有分布组合
		   for(Integer[] oneComb :CommonUtils.getAllComb(subjectPermutationSizes,subjectPermutationSizes.size())){
			  //每个学选交叉的科目Id集合
			   List<Set<Integer>> subjectIdsSetList = new ArrayList();
			   for(int time =0;time<times;time++){
				   subjectIdsSetList.add(new HashSet());
			   }
			   boolean isOk = true;
			   List<Integer>[][] subjectIds = new List[times][2];
			   for(int i=0;i<oneComb.length;i++){
				  Subject currentSubject = allSubjects.get(i);
				  List<Integer> currentSubjectDistribute =  subjectPermutationMaps[currentSubject.isOpt].get(currentSubject).get(oneComb[i]);
				   for(int time = 0;time < times; time++){
					   //如果分布因子不为0则添加进去
					   if(currentSubjectDistribute.get(time)!=0){
						   subjectIdsSetList.get(time).add(currentSubjectDistribute.get(time));
					   }
					   List<Integer> oneSubjectIdsList =  subjectIds[time][currentSubject.isOpt];
					   if(oneSubjectIdsList==null){
						   oneSubjectIdsList = new ArrayList();
						   subjectIds[time][currentSubject.isOpt] = oneSubjectIdsList;
					   }
					   
					   if(!currentSubjectDistribute.get(time).equals(0)){
						   oneSubjectIdsList.add(currentSubjectDistribute.get(time));
					   }
					   
					   //在一个学选交叉里面科目数不能超过3
					   if(subjectIdsSetList.get(time).size()  > config.seqCounts[CD_PRO]
							   && !(CollectionUtils.isEmpty(subjectIds[time][CD_OPT])||CollectionUtils.isEmpty(subjectIds[time][CD_PRO]))
							   && !(isNeedOver3Subject&&time==times-1)){
						   isOk = false; 
						   break;	  
						}
					   
				   }
				   if(!isOk){
					   break;
				   }
			   }
			   if(isOk){
				   List<Conflict> conflicts = new ArrayList();
				   for(int time = 0;time < times; time++){
					   //如果选课和选课任意一方科目数为0
					   if(subjectIds[time][CD_OPT].size()==0||subjectIds[time][CD_PRO].size()==0){
						   continue;
					   }
					   List[] seqs = null;
					   //如果需要乱序的情况下
//					   if(isNeedSeqDisorder){
//						  // seqs =  new List[]{new ArrayList(allSeqPerm.get(conflicts.size()%allSeqPerm.size())),Arrays.asList(new Integer[]{1,2,3})};
//						 //  seqs =  new List[]{Arrays.asList(new Integer[]{3,1,2}),Arrays.asList(new Integer[]{1,2,3})};
//					   }else{
						   seqs =  new List[]{Arrays.asList(new Integer[]{1,2,3}),Arrays.asList(new Integer[]{1,2,3})};
//					   }
					   //超过3科情况下
					   int splusSujectCount = subjectIdsSetList.get(time).size()-config.seqCounts[CD_PRO];
					   //如果超过的课数比剩下能排的课时数多，则忽略
					   if(splusSujectCount>splus){
						   isOk = false;
						   break;
					   }else if(splusSujectCount== 1){
						   seqs[CD_PRO].set(0, -1);
						}else if(splusSujectCount== 2){
						   seqs[CD_PRO].set(0, -1);
						   seqs[CD_PRO].set(1, -1);
					   }
					   Conflict conflict = new Conflict(subjectIds[time],seqs);
					   conflicts.add(conflict);
				   }
				   if(isOk){
					   allConflictsList.add(conflicts);
				   }
			   }
		   }	
		}

		
		//过滤掉所有重复的冲突组合
		List<List<Conflict>> distinctConflicts = new ArrayList();
		for(List<Conflict> conflicts:allConflictsList){
			boolean isHad = false;
			for(List<Conflict> distinctConflict:distinctConflicts){
				if(distinctConflict.containsAll(conflicts)){
					isHad = true;
				}
			}
			if(!isHad){
				distinctConflicts.add(conflicts);
			}
		}
		//过滤学选交叉次数过多的组合
		List<List<Conflict>> minConflicts = new ArrayList();
		for(List<Conflict> conflicts:distinctConflicts){
			if(conflicts.size()>minTimes){
				continue;
			}
			minConflicts.add(conflicts);
		}
		
		return minConflicts.size()!=0?minConflicts:distinctConflicts;
	}
	public List<Integer>[] getSeqs() {
		// TODO Auto-generated method stub
		return this.seqs;
	}
	public List<Integer>[] getSubjectIds() {
		// TODO Auto-generated method stub
		return this.subjectIds;
	}	
	
	

}