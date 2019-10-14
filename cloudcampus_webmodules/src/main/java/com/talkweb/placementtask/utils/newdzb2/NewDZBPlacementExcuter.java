package com.talkweb.placementtask.utils.newdzb2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.utils.newdzb2.ConflictComputeResult.SubjectCombinationStatus;
import com.talkweb.placementtask.utils.newdzb2.SubSubjectCombination.SubSubjectCombinationType;
import com.talkweb.placementtask.utils.newdzb2.SubjectCombination.ConflictType;

/**
 * 大走班分班执行器
 * @author yzw
 * 
 */
public class NewDZBPlacementExcuter{
//	static Logger logger = LoggerFactory.getLogger(NewDZBPlacementExcuter.class);
	protected final static Integer CD_OPT = 1;
	protected final static Integer CD_PRO = 0;
	/**
	 * 学课类型
	 * @author lenovo
	 *
	 */
	public enum SelectType{
		ThreeFromSix,
		ThreeFromSeven;
		public int getGoodScore(){
			if(this == SelectType.ThreeFromSeven){
				return 36;
			}else if(this == SelectType.ThreeFromSix){
				return 49;
			}
			return 0;
		}
	}
	public static class PlacementTaskConfig{
		/**
		 * 
		 * @param avgClassStudentCount 班平均人数
		 * @param optSubjectIdLessonMap 选考科目和课时映射Map
		 * @param proSubjectIdLessonMap 学考科目和课时映射Map
		 */
		public PlacementTaskConfig(Integer avgClassStudentCount,
				Integer maxClassStudentCount,
				Map<Integer,Integer> optSubjectIdLessonMap,
				Map<Integer,Integer> proSubjectIdLessonMap,
				SelectType selectType
				){
			this.avgTclassStudentCount = avgClassStudentCount;
			this.maxTclassStudentCount = maxClassStudentCount;
			this.optSubjectIdLessonMap = optSubjectIdLessonMap;
			this.proSubjectIdLessonMap = proSubjectIdLessonMap;
			this.selectType = selectType;
			if(selectType==SelectType.ThreeFromSeven){
				seqCounts[CD_OPT] = 3;
				seqCounts[CD_PRO] = 4;
			}else if(selectType==SelectType.ThreeFromSix){
				seqCounts[CD_OPT] = 3;
				seqCounts[CD_PRO] = 3;
			}
		}
		public int[] seqCounts = new int[2];
		/**
		 * 班级最大人数
		 */
		Integer maxTclassStudentCount = null;
		/**
		 * 班级平均人数
		 */
		Integer avgTclassStudentCount = null;
		/**
		 * 选考科目和课时映射Map
		 */
		Map<Integer,Integer> optSubjectIdLessonMap = null;
		/**
		 * 学考科目和课时映射Map
		 */
		Map<Integer,Integer> proSubjectIdLessonMap = null;
		public SelectType selectType;
		
	

	}

	
	/**
	 * 分班结果
	 */
	public static class PlacementResult{
		/**
		 * 执行结果Code
		 * 1 执行成功
		 * -1 执行失败
		 */
		public Integer resultCode = null;
		public List<Tclass> tclassList = null;
		public List<Conflict> conflictList = null;
		public List<TclassEqualSubjectCombination> tclassEqualSubjectCombinations = null;
		public PlacementResult(Integer resultCode){
			this.resultCode = resultCode;
		}
		public PlacementResult(Integer resultCode,List<Tclass> tclassList,List<Conflict> conflictList){
			this.resultCode = resultCode;
			this.tclassList = tclassList;
			this.conflictList = conflictList;
		}
		public ResultScore score = null;
		/**
		 * 所有辅助教室ID
		 */
		public List<String> allExtGroundIds = null;
		public Collection<String> subSubjectCominationStrs = null;
	}

	/**
	 * 优化冲突科目
	 * 
	 */
	public static void conflictProSubjectOptimize(Subject subject,Map<Integer,
			Subject>[] subjectIdMaps,List<Conflict> conflicts,PlacementTaskConfig config){
		for(int seq=1;seq< 3;seq++){
			if(subject.seqSubjects[seq-1].getStudentCount() - subject.avgTclassStudentCount*subject.seqTclassCount[seq-1]>subject.seqTclassCount[seq-1]){
				//不合理，开始调换人数
				int needswitchCount =  subject.seqSubjects[seq-1].getStudentCount() - subject.avgTclassStudentCount*subject.seqTclassCount[seq-1] -subject.seqTclassCount[seq-1];
				for(SubjectCombination subjectCombination:subject.seqSubjects[seq-1].fromSubjectCombinationMap.keySet()){
					SubSubjectCombination subSubjectCombination = (SubSubjectCombination)subjectCombination;
					if(subSubjectCombination.getConfictType() != ConflictType.TYPE_1 && subSubjectCombination.getConfictType() != ConflictType.TYPE_2){
						continue;
					}
					for(int otherSeq=1;otherSeq<3;otherSeq++){
						if(needswitchCount<=0){
							break;
						}
						if(seq==otherSeq){
							continue;
						}
						if(subject.seqSubjects[otherSeq-1].getStudentCount() - subject.avgTclassStudentCount*subject.seqTclassCount[seq-1]<subject.seqTclassCount[seq-1]){
							for(int otherSubjectId:subSubjectCombination.parent.subjectIds[CD_PRO]){
								if(needswitchCount<=0){
									break;
								}
								Subject otherSubject = subjectIdMaps[CD_PRO].get(otherSubjectId);
								int switchCount =otherSubject.avgTclassStudentCount*otherSubject.seqTclassCount[seq-1]- otherSubject.seqSubjects[seq-1].getStudentCount();
								int count = 0;
								SubSubjectCombination otherSubSubjectCombination =null;
								for(SubjectCombination one:otherSubject.seqSubjects[seq-1].fromSubjectCombinationMap.keySet()){
									SubSubjectCombination oneSub = (SubSubjectCombination)one;
									if(oneSub.parent.equals(subSubjectCombination.parent)){
										count = oneSub.getTotalStudentCount();
										otherSubSubjectCombination = oneSub;
										break;
									}
								}
								switchCount  = Math.min(switchCount, count);
								switchCount = Math.min(needswitchCount, switchCount);
								
								if(switchCount > 0){
									SubjectCombinationSwap subjectCombinationSwap = new SubjectCombinationSwap(subSubjectCombination.parent);
									subjectCombinationSwap.swapSeqs[0]=subSubjectCombination.seqIndex+1;
									subjectCombinationSwap.swapSeqs[1]=otherSubSubjectCombination.seqIndex+1;
									subjectCombinationSwap.swapSubject[0]=subjectIdMaps[CD_OPT].get(subSubjectCombination.subjectId);
									subjectCombinationSwap.swapSubject[1]=subjectIdMaps[CD_OPT].get(otherSubSubjectCombination.subjectId);
									subjectCombinationSwap.swapStudenCount = switchCount;
									needswitchCount-=switchCount;
									subjectCombinationSwap.doSwap();
								}
							}
						}

					}
					
				}
			}
		}
		
		return;
	}
	
	/**
	 * 统计当前科目班级序列分布
	 * @param subject
	 * @param config
	 * @return
	 */
	public static Integer[]  statsSeqTclassCount(Subject subject,PlacementTaskConfig config){
		Map<Integer,Float> floatMap = new HashMap();
		Map<Integer,Integer> intMap = new HashMap();
		for(int seq=1;seq<=3;seq++){
			int seqStudent = subject.seqSubjects[seq-1].getStudentCount();
			intMap.put(seq, (int) Math.floor((float)seqStudent/subject.avgTclassStudentCount));
			floatMap.put(seq, (float) seqStudent/subject.avgTclassStudentCount);
		}
		List<Entry<Integer, Float>> entryList = new ArrayList(floatMap.entrySet());
		Collections.sort(entryList,new Comparator<Entry<Integer, Float>>(){

			@Override
			public int compare(Entry<Integer, Float> arg0,
					Entry<Integer, Float> arg1) {
				// TODO Auto-generated method stub
				return arg1.getValue().compareTo(arg0.getValue());
			}});
		int currentTotalCount = CommonUtils.sum(intMap.values());
		for(int i=0;i<subject.tclassCount - currentTotalCount;i++){
			intMap.put(entryList.get(i).getKey(), intMap.get(entryList.get(i).getKey())+1);
		}
		Integer[] seqTclassCount = new Integer[3];
		for(int seq=1;seq<=3;seq++){
			seqTclassCount[seq-1]=intMap.get(seq);
		}
		return seqTclassCount;
	}
	
	enum OptimizeType{
		TYPE_FREE, //自由调整模式
		TYPE_CLASSCOUNT //强制按照班级数分布调整
	}
	
	public static void doubleSubjectOptimize(Map<Integer,Subject>[] subjectIdMaps,List<Conflict> conflicts,PlacementTaskConfig config,int isOpt){
		List<Subject> needOptimizeSubjects = new ArrayList();
		for(Subject subject:subjectIdMaps[isOpt].values()){
			if(subject.isNeedOptimize(config,OptimizeType.TYPE_CLASSCOUNT)){
				needOptimizeSubjects.add(subject);
			}
		}
		List<Subject> sortedSubjects = new ArrayList(needOptimizeSubjects);
		List<Subject> goodSubjects = new ArrayList(subjectIdMaps[isOpt].values());
		goodSubjects.removeAll(sortedSubjects);
		sortedSubjects.addAll(goodSubjects);
		for(Subject subject:needOptimizeSubjects){
			System.out.println("--------当前优化科目subject:"+subject.subjectId+"  isOpt:"+subject.isOpt);
			int num[] = new int[3];
			Integer[] currentSeqClassCount = Arrays.copyOf(subject.seqTclassCount, subject.seqTclassCount.length);
			boolean isContinue = false;
			if(subject.tclassCount%3==0){
				int first = currentSeqClassCount[0];
				for(int one : currentSeqClassCount){
					if(first != one){
						isContinue = true;
						break;
					}
				}
			}
			if(isContinue){
				continue;
			}
			//找出不合格的两个序列
			List<Integer> badSeqs = new ArrayList();
			for(SeqSubject seqSubject:subject.seqSubjects){
				int seqStudentCount = seqSubject.getStudentCount();
				if(seqStudentCount > config.maxTclassStudentCount * subject.seqTclassCount[seqSubject.seq-1] || seqStudentCount < (subject.avgTclassStudentCount-5)* subject.seqTclassCount[seqSubject.seq-1]){
					badSeqs.add(seqSubject.seq);
				}
			}
			List<Integer> sortedSeqs = new ArrayList(badSeqs);
			for(int seq = 1;seq<=config.seqCounts[isOpt];seq++){
				if(!sortedSeqs.contains(seq)){
					sortedSeqs.add(seq);
				}
			}
			//找出所有班级分布变动组合
			List<int[]> allChangeNumsList = new ArrayList();
			int tclassCount = CommonUtils.sum(subject.seqTclassCount);
			int avgTclassCount = tclassCount/config.seqCounts[isOpt];
			for(Integer oneSeq:sortedSeqs){
				for(Integer otherSeq:sortedSeqs){
					if(oneSeq==otherSeq){
						continue;
					}
					if((subject.seqTclassCount[oneSeq-1]+1) <= (avgTclassCount+1) && (subject.seqTclassCount[otherSeq-1]-1) <= (avgTclassCount + 1) ){
						int[] oneChangeNums = new int[config.seqCounts[isOpt]];
						oneChangeNums[oneSeq-1] = 1;
						oneChangeNums[otherSeq-1] = -1;
						allChangeNumsList.add(oneChangeNums);
					}else if((subject.seqTclassCount[oneSeq-1]-1) <= (avgTclassCount+1) && (subject.seqTclassCount[otherSeq-1]+1) <= (avgTclassCount + 1) ){
						int[] oneChangeNums = new int[config.seqCounts[isOpt]];
						oneChangeNums[oneSeq-1] = -1;
						oneChangeNums[otherSeq-1] = 1;
						allChangeNumsList.add(oneChangeNums);
					}
				}
			}
			if(allChangeNumsList.size()==0){
				continue;
			}
			Map<ResultScore,SeqTclassCountChangeBean> resultMap = new HashMap();
			for(int[] oneChangeNums:allChangeNumsList){
				//跟其他科目进行对应班级调整
				for(Subject otherSubject:subjectIdMaps[isOpt].values()){
					if(subject.equals(otherSubject)){
						continue;
					}
					boolean isDoContinue = false;
					for(int oneSeq=1;oneSeq<=config.seqCounts[isOpt];oneSeq++){
						if((otherSubject.seqTclassCount[oneSeq-1]-oneChangeNums[oneSeq-1]) > (avgTclassCount+1) ){
							isDoContinue = true;
							break;
						}	
					}
					if(isDoContinue){
						continue;
					}
					SeqTclassCountChangeBean seqTclassCountChangeBean  = new SeqTclassCountChangeBean(subject,otherSubject,isOpt,oneChangeNums);
					ResultScore currentScore = ResultScore.scoreResult(config, OptimizeType.TYPE_CLASSCOUNT, new Subject[]{subject,otherSubject});
					//备份OtherSubject当前的班级分布和各序列资源组合人数
					Subject.backupSubject(subjectIdMaps[isOpt].values());
					//变更序列班级分布
					for(int oneSeq=1;oneSeq<=config.seqCounts[isOpt];oneSeq++){
						subject.seqTclassCount[oneSeq-1]+=oneChangeNums[oneSeq-1];
						otherSubject.seqTclassCount[oneSeq-1]-=oneChangeNums[oneSeq-1];
					}
					System.out.println("-----------当前优化科目subject:"+subject.subjectId+"  isOpt:"+subject.isOpt+"  class:"+JSONObject.toJSONString(subject.seqTclassCount));
					System.out.println("-----------交换科目otherSubject:"+otherSubject.subjectId+"  isOpt:"+otherSubject.isOpt+"  class:"+JSONObject.toJSONString(otherSubject.seqTclassCount));
					List<SubjectCombinationSwap> swaps = new ArrayList();
					seqTclassCountChangeBean.swapList = swaps;
					swaps.addAll(subjectOptimize(subject,subjectIdMaps,conflicts,config,OptimizeType.TYPE_CLASSCOUNT));
					swaps.addAll(subjectOptimize(otherSubject,subjectIdMaps,conflicts,config,OptimizeType.TYPE_CLASSCOUNT));
					ResultScore score = ResultScore.scoreResult(config, OptimizeType.TYPE_CLASSCOUNT, new Subject[]{subject,otherSubject});
					if(score.compareTo(currentScore)>0){
						resultMap.put(score, seqTclassCountChangeBean);
					}
					System.out.println("-----------结果当前优化科目subject:"+subject.subjectId+"  isOpt:"+subject.isOpt+"  class:"+JSONObject.toJSONString(subject.seqTclassCount)+"  seqStudent:");
					for(int i=0;i<config.seqCounts[isOpt];i++){
						System.out.print(subject.seqSubjects[i].getStudentCount()+",");
					}
					System.out.println("");
					System.out.println("-----------结果交换科目otherSubject:"+otherSubject.subjectId+"  isOpt:"+otherSubject.isOpt+"  class:"+JSONObject.toJSONString(otherSubject.seqTclassCount)+"  seqStudent:");
					for(int i=0;i<config.seqCounts[isOpt];i++){
						System.out.print(otherSubject.seqSubjects[i].getStudentCount()+",");
					}
					System.out.println("");
					//还原OtherSubject当前的班级分布和各序列资源组合人数
					Subject.restoreSubject(subjectIdMaps[isOpt].values());
				}
			}
			List<Map.Entry<ResultScore, SeqTclassCountChangeBean>> entrys = new ArrayList(resultMap.entrySet());
			Collections.sort(entrys, new Comparator<Map.Entry<ResultScore, SeqTclassCountChangeBean>>(){
				@Override
				public int compare(
						Entry<ResultScore, SeqTclassCountChangeBean> arg0,
						Entry<ResultScore, SeqTclassCountChangeBean> arg1) {
					// TODO Auto-generated method stub
					return arg1.getKey().compareTo(arg0.getKey());
				}
			});
			if(entrys.size()!=0){
				SeqTclassCountChangeBean bean = entrys.get(0).getValue();
				for(int oneSeq=1;oneSeq<=config.seqCounts[isOpt];oneSeq++){
					bean.subject.seqTclassCount[oneSeq-1]+=bean.changeNums[oneSeq-1];
					bean.otherSubject.seqTclassCount[oneSeq-1]-=bean.changeNums[oneSeq-1];
				}
				for(SubjectCombinationSwap scSwap:bean.swapList){
					scSwap.doSwap();
				}
				//
			}
	}
		
	}
	/**
	 * 制定班级数分布优化
	 * @param subject
	 * @param subjectIdMaps
	 * @param conflicts
	 * @param config
	 * @return
	 */
	private static List<SubjectCombinationSwap>  subjectOptimize(Subject subject,Map<Integer,Subject>[] subjectIdMaps,List<Conflict> conflicts,PlacementTaskConfig config,OptimizeType optimizeType){
	//	System.out.println("------isOpt:"+subject.isOpt+"  subject:"+subject.subjectId+"开始优化--------"
	//			+subject.seqSubjects[0].getStudentCount()+","+subject.seqSubjects[1].getStudentCount()+","+subject.seqSubjects[2].getStudentCount());
		List<SubjectCombinationSwap> subjectCombinationSwaps = new ArrayList();
		if(!subject.isNeedOptimize(config,OptimizeType.TYPE_FREE)){
//			System.out.println("------isOpt:"+subject.isOpt+"  subject:"+subject.subjectId+"不需要优化-----------------");
			return subjectCombinationSwaps ;
		}
		boolean isOK = false;
		//排两轮第一轮寻找最优人数方案，第一轮排不好，第二轮只要满足人数不超过最大人数即可
		for(int turn = 1 ;turn <=2 ;turn++){
			int avgTclassStudentCount = subject.avgTclassStudentCount;
			if(turn==2){
				avgTclassStudentCount = config.maxTclassStudentCount -1;
			}
			List<SubjectCombinationSwap> oneSwaps = doubleSeqSwap(subject,avgTclassStudentCount, subjectIdMaps, conflicts,config,optimizeType,turn);
			subjectCombinationSwaps.addAll(oneSwaps);
			if(subject.isAvailable(config)&&turn==2){
				break;
			}else if(!subject.isNeedOptimize(config, optimizeType)){
				break;
			}
					
			List<SubjectCombinationSwap> twoSwaps = threeSeqSwap(subject,  avgTclassStudentCount, subjectIdMaps,conflicts,config,optimizeType, subject.isOpt,turn);
			subjectCombinationSwaps.addAll(twoSwaps);
			if(subject.isAvailable(config)){
				break;
			}
		}
		return subjectCombinationSwaps;
	}
	
	/**
	 * 两两序列人数调整
	 * @param subject
	 * @return
	 */
	public static List<SubjectCombinationSwap> doubleSeqSwap(Subject subject, 
			Integer avgTclassStudentCount,
			Map<Integer,Subject>[] subjectIdMaps,
			List<Conflict> conflicts,
			PlacementTaskConfig config,
			OptimizeType optimizeType,
			int turn){
		List<SubjectCombinationSwap> subjectCombinationSwaps = new ArrayList();
		Map<Integer,Integer> needAddSeqs = new HashMap<Integer,Integer>();
		Map<Integer,Integer> needMinusSeqs =  new HashMap<Integer,Integer>();
		Map<Integer,Integer> okSeqs =  new HashMap<Integer,Integer>();
		Integer[] oneTclassCounts = subject.seqTclassCount;
		boolean isOK  = false;
		for(int seq = 1;seq<=config.seqCounts[subject.isOpt];seq++){
			if(subject.seqSubjects[seq-1].getStudentCount()-avgTclassStudentCount*oneTclassCounts[seq-1] > 0){
				needMinusSeqs.put(seq,Math.abs(subject.seqSubjects[seq-1].getStudentCount()-avgTclassStudentCount*oneTclassCounts[seq-1]));
			}else if(subject.seqSubjects[seq-1].getStudentCount()-avgTclassStudentCount*oneTclassCounts[seq-1]  == 0){
				okSeqs.put(seq,0);
			}else{
				needAddSeqs.put(seq,Math.abs(subject.seqSubjects[seq-1].getStudentCount()-avgTclassStudentCount*oneTclassCounts[seq-1]));
			}
		}
//		System.out.println("-----------尝试isOpt"+subject.isOpt+"    "+JSONObject.toJSONString(oneTclassCounts)+"的班级分配安排----------------");
//		System.out.println("---------------needAddSeqs:"+JSONObject.toJSONString(needAddSeqs)
//							+"   needMinusSeqs:"+JSONObject.toJSONString(needMinusSeqs)
//							+"   okSeqs:"+JSONObject.toJSONString(okSeqs)+"-------------");
		List<Integer> allAddSeq = new ArrayList(needAddSeqs.keySet());
		allAddSeq.addAll(okSeqs.keySet());
		
		//两两序列移动
		for(Integer needAddSeq:allAddSeq){
			if(isOK){
				break;
			}
			List<Integer> allMinusSeq = new ArrayList(needMinusSeqs.keySet());
			allMinusSeq.addAll(okSeqs.keySet());
			for(Integer needMinusSeq:allMinusSeq){
				int fromSeq = needMinusSeq;
				int toSeq = needAddSeq;
				if(fromSeq==toSeq){
					continue;
				}
				Integer needAddStudentCount = needAddSeqs.get(needAddSeq);
				if(needAddStudentCount==null){
					needAddStudentCount = 0;
				}
				Integer needMinusStudentCount = needMinusSeqs.get(needMinusSeq);
				if(needMinusStudentCount==null){
					needMinusStudentCount = 0;
				}
				if(needAddStudentCount<=0&&needMinusStudentCount<=0){
					continue;
				}
				if(isOK){
					break;
				}
				List<SubjectCombination> subjectCombinations = new ArrayList(subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.keySet());
				Collections.sort(subjectCombinations, new Comparator<SubjectCombination>(){
					@Override
					public int compare(SubjectCombination sc1,
							SubjectCombination sc2) {
						// TODO Auto-generated method stub
						ConflictType c1 =  sc1.getConfictType();
						if(sc1 instanceof SubSubjectCombination){
							c1 = ((SubSubjectCombination) sc1).getConfictType();
						}
						ConflictType c2 =  sc1.getConfictType();
						if(sc2 instanceof SubSubjectCombination){
							c2 = ((SubSubjectCombination) sc2).getConfictType();
						}
						return c1.compareTo(c2);
					}
					
				});
				int swapStudenCount = Math.min(needMinusStudentCount, needAddStudentCount);
				List<SubjectCombinationSwap> oneSwaps = new ArrayList();
				int moveCount = computeSwaps(subject,fromSeq,toSeq,swapStudenCount,subjectIdMaps,conflicts,config,optimizeType,oneSwaps);
				needAddStudentCount -=moveCount;
				needMinusStudentCount -=moveCount;
				needAddSeqs.put(needAddSeq,needAddStudentCount);
				needMinusSeqs.put(needMinusSeq,needMinusStudentCount);
				subjectCombinationSwaps.addAll(oneSwaps);
				if(turn==1){
					if(ResultScore.scoreResult(config, optimizeType, subject).goodCount == config.seqCounts[subject.isOpt]){
						isOK = true;
						break;
					}
				}else if(turn==2){
					if(subject.isAvailable(config)){
						isOK = true;
						break;
					}
				}
			}
		}
	
		return subjectCombinationSwaps;
		
	}
	
	/**
	 * 三个序列人数调整
	 * @param subject
	 * @return
	 */
	public static List<SubjectCombinationSwap> threeSeqSwap(Subject subject, 
			Integer avgTclassStudentCount,
			Map<Integer,Subject>[] subjectIdMaps,
			List<Conflict> conflicts,
			PlacementTaskConfig config,
			OptimizeType optimizeType,
			int isOpt,
			int turn){
		List<SubjectCombinationSwap> subjectCombinationSwaps = new ArrayList();
		Map<Integer,Integer> needAddSeqs = new HashMap<Integer,Integer>();
		Map<Integer,Integer> needMinusSeqs =  new HashMap<Integer,Integer>();
		Map<Integer,Integer> okSeqs =  new HashMap<Integer,Integer>();
		Integer[] oneTclassCounts = subject.seqTclassCount;
		boolean isOK  = false;
		List<Integer> allSeqList = new ArrayList();
		for(int seq = 1;seq<=config.seqCounts[subject.isOpt];seq++){
			allSeqList.add(seq);
			if(subject.seqSubjects[seq-1].getStudentCount()-avgTclassStudentCount*oneTclassCounts[seq-1] > 0){
				needMinusSeqs.put(seq,Math.abs(subject.seqSubjects[seq-1].getStudentCount()-avgTclassStudentCount*oneTclassCounts[seq-1]));
			}else if(subject.seqSubjects[seq-1].getStudentCount()-avgTclassStudentCount*oneTclassCounts[seq-1]  == 0){
				okSeqs.put(seq,0);
			}else{
				needAddSeqs.put(seq,Math.abs(subject.seqSubjects[seq-1].getStudentCount()-avgTclassStudentCount*oneTclassCounts[seq-1]));
			}
		}
		
		List<Integer> allMinusSeq = new ArrayList(needMinusSeqs.keySet());
		allMinusSeq.addAll(okSeqs.keySet());
		
		//两两序列移动
		for(Integer needMinusSeq:allMinusSeq){
			if(isOK){
				break;
			}
			List<Integer> allAddSeq = new ArrayList(needAddSeqs.keySet());
			allAddSeq.addAll(okSeqs.keySet());
			for(Integer needAddSeq:allAddSeq){
				int fromSeq = needMinusSeq;
				int toSeq = needAddSeq;
				if(fromSeq==toSeq){
					continue;
				}
				List<Integer> tempAllSeqList = new ArrayList(allSeqList);
				tempAllSeqList.remove(tempAllSeqList.indexOf(needMinusSeq));
				tempAllSeqList.remove(tempAllSeqList.indexOf(needAddSeq));
				int centerSeq = tempAllSeqList.get(0);
				
				Integer needAddStudentCount = needAddSeqs.get(needAddSeq);
				if(needAddStudentCount==null){
					needAddStudentCount = 0;
				}
				Integer needMinusStudentCount = needMinusSeqs.get(needMinusSeq);
				if(needMinusStudentCount==null){
					needMinusStudentCount = 0;
				}
				if(needAddStudentCount<=0&&needMinusStudentCount<=0){
					continue;
				}
				if(isOK){
					break;
				}
				List<SubjectCombination> subjectCombinations = new ArrayList(subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.keySet());
				Collections.sort(subjectCombinations, new Comparator<SubjectCombination>(){
					@Override
					public int compare(SubjectCombination sc1,
							SubjectCombination sc2) {
						// TODO Auto-generated method stub
						ConflictType c1 =  sc1.getConfictType();
						if(sc1 instanceof SubSubjectCombination){
							c1 = ((SubSubjectCombination) sc1).getConfictType();
						}
						ConflictType c2 =  sc1.getConfictType();
						if(sc2 instanceof SubSubjectCombination){
							c2 = ((SubSubjectCombination) sc2).getConfictType();
						}
						return c1.compareTo(c2);
					}
				});
				int swapStudenCount = Math.max(needAddStudentCount,needMinusStudentCount);
				//直接从from序列移到目标序列
//				List<SubjectCombinationSwap> oneSubjectCombinationSwaps = new ArrayList();
//				int moveCount = computeSwaps(subject,fromSeq,toSeq,swapStudenCount,subjectIdMaps,conflicts,config,optimizeType,oneSubjectCombinationSwaps);
//				subjectCombinationSwaps.addAll(oneSubjectCombinationSwaps);
//				needAddStudentCount -=moveCount;
//				needMinusStudentCount -=moveCount;
				
				//尝试先移动到中间序列，再从中间序列移到目标序列
				for(int i = 0; i<2;i++){
					if(swapStudenCount==0){
						break;
					}
					//subject从fromSeq移动到centerSeq
					Subject.backupSubject(subjectIdMaps[isOpt].values());
					List<SubjectCombinationSwap> firstSubjectCombinationSwaps = new ArrayList();
					int firstMoveCount = computeSwaps(subject,fromSeq,centerSeq,swapStudenCount,subjectIdMaps,conflicts,config,optimizeType,firstSubjectCombinationSwaps);
					//subject从centerSeq移动到toSeq
					List<SubjectCombinationSwap> secendSubjectCombinationSwaps = new ArrayList();
					int secendMoveCount = computeSwaps(subject,fromSeq,centerSeq,firstMoveCount,subjectIdMaps,conflicts,config,optimizeType,secendSubjectCombinationSwaps);
					if(firstMoveCount!=secendMoveCount){
						swapStudenCount = Math.min(firstMoveCount,secendMoveCount);
						Subject.restoreSubject(subjectIdMaps[isOpt].values());
					}else{
						subjectCombinationSwaps.addAll(firstSubjectCombinationSwaps);
						subjectCombinationSwaps.addAll(secendSubjectCombinationSwaps);
						Subject.clearBackupSubject(subjectIdMaps[isOpt].values());
						needAddStudentCount -=secendMoveCount;
						needMinusStudentCount -=secendMoveCount;
						needAddSeqs.put(needAddSeq,needAddStudentCount);
						needMinusSeqs.put(needMinusSeq,needMinusStudentCount);
						break;
					}	
				}
				if(turn==1){
					if(ResultScore.scoreResult(config, optimizeType, subject).goodCount == config.seqCounts[subject.isOpt]){
						isOK = true;
						break;
					}
				}else if(turn==2){
						isOK = true;
					if(subject.isAvailable(config)){
						break;
					}
				}
			}
		}
		return subjectCombinationSwaps;
	}
	
	/**
	 * 
	 * @param subject
	 * @param fromSeq
	 * @param toSeq
	 * @param totalSwapStudentCount
	 * @param subjectIdMaps
	 * @param conflicts
	 * @param config
	 * @param optimizeType
	 * @param subjectCombinationSwaps
	 * @param subjectCombinations
	 * @return
	 */
	public static int computeSwaps(Subject subject,
			int fromSeq,
			int toSeq,
			int totalSwapStudentCount,
			Map<Integer,Subject>[] subjectIdMaps,
			List<Conflict> conflicts,
			PlacementTaskConfig config,
			OptimizeType optimizeType,
			List<SubjectCombinationSwap> subjectCombinationSwaps,
			SubjectCombination... subjectCombinations){
		boolean isOK = false;
		int orginTotalSwapStudentCount = 0;
		if(subjectCombinations==null||subjectCombinations.length==0){
			subjectCombinations = new SubjectCombination[subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.keySet().size()];
			subjectCombinations = subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.keySet().toArray(subjectCombinations);
		}
		for(SubjectCombination subjectCombination:subjectCombinations){
			if(subjectCombination.getConfictType() == ConflictType.TYPE_3
					|| subjectCombination.getConfictType() == ConflictType.TYPE_4 
					||subjectCombination.getConfictType() == ConflictType.TYPE_5
					|| subjectCombination.getConfictType() == ConflictType.TYPE_6 ){
				continue;
			}
			if(subject.isOpt==CD_PRO && subjectCombination instanceof SubSubjectCombination){
				SubSubjectCombination subSubjectCombination = (SubSubjectCombination) subjectCombination;
				if(subSubjectCombination.getConfictType()!=  ConflictType.TYPE_1 ){
					continue;
				}
			}
			//判断此志愿组合是否可以移到这个少的序列中（学选交叉)
			//判断从fromSeq 到 toSeq
			if(checkConflict(subject.subjectId, toSeq, subjectCombination,conflicts, subject.isOpt,subjectIdMaps)){
		//		System.out.println("------尝试将"+subjectCombination.subjectIdsStr+"    subjectId:"+subject.subjectId+" fromSeq:"+fromSeq+"  ->toSeq:"+toSeq);
				for(Integer oneSubjectId:subjectCombination.subjectIds[subject.isOpt]){
					if(isOK){
						break;
					}
					if(oneSubjectId.equals(subject.subjectId)){
						continue;
					}
					Subject oneSubject = subjectIdMaps[subject.isOpt].get(oneSubjectId);
					//小的序列里面有这个志愿组合
		//			System.out.println("------尝试将"+subjectCombination.subjectIdsStr+"    subjectId:"+oneSubjectId+" toSeq:"+toSeq+"  ->fromSeq:"+fromSeq);			
					if(oneSubject.seqSubjects[toSeq-1].fromSubjectCombinationMap.containsKey(subjectCombination)){
						if(checkConflict(oneSubjectId,fromSeq,subjectCombination,conflicts,subject.isOpt,subjectIdMaps)){
							//移动学生数为组合人数，fromSeq多余的人数，toSeq缺少的人数 中的最小值
						//	int swapStudenCount = Math.max(needAddStudentCount,needMinusStudentCount);
							if(subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.get(subjectCombination) ==null){
								continue;
							}
							if(oneSubject.seqSubjects[toSeq-1].fromSubjectCombinationMap.get(subjectCombination) ==null){
								continue;
							}
							//from科目可以移动的人数  to对应科目可以移动的人数
							int swapStudenCount = Math.min(oneSubject.seqSubjects[toSeq-1].fromSubjectCombinationMap.get(subjectCombination), subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.get(subjectCombination));
							swapStudenCount = Math.min(swapStudenCount, totalSwapStudentCount);
							
							//移动后from序列 中 oneSubject人数是否合理
							int oneSubjectAvgTclassStudentCount = oneSubject.avgTclassStudentCount;
							int fromOneSubjectClassCount = 0;
							if(optimizeType == optimizeType.TYPE_FREE){
								fromOneSubjectClassCount = Math.round((float)(oneSubject.seqSubjects[fromSeq-1].getStudentCount()+swapStudenCount)/oneSubjectAvgTclassStudentCount);
							}else if(optimizeType == optimizeType.TYPE_CLASSCOUNT){
								fromOneSubjectClassCount = oneSubject.seqTclassCount[fromSeq-1];
							}
							
							if(oneSubject.seqSubjects[fromSeq-1].getStudentCount()==0){
								swapStudenCount = 0;
							}
							if((oneSubject.seqSubjects[fromSeq-1].getStudentCount()+swapStudenCount)< (oneSubjectAvgTclassStudentCount-5)*fromOneSubjectClassCount 
									|| (oneSubject.seqSubjects[fromSeq-1].getStudentCount()+swapStudenCount) > config.maxTclassStudentCount*fromOneSubjectClassCount){
								fromOneSubjectClassCount =  Math.round((float)oneSubject.seqSubjects[fromSeq-1].getStudentCount()/oneSubjectAvgTclassStudentCount);
								if(oneSubject.seqSubjects[fromSeq-1].getStudentCount()>(oneSubjectAvgTclassStudentCount-5)*fromOneSubjectClassCount 
									&& oneSubject.seqSubjects[fromSeq-1].getStudentCount() <=  config.maxTclassStudentCount*fromOneSubjectClassCount){
									swapStudenCount = Math.min(swapStudenCount, config.maxTclassStudentCount*fromOneSubjectClassCount-oneSubject.seqSubjects[fromSeq-1].getStudentCount());
								}
							}
							// System.out.println("-------------------移动后from序列 中 oneSubject人数 fromOneSubjectClassCount："+fromOneSubjectClassCount+"  ")
							//移动后to序列 中 oneSubject人数是否合理
							int toOneSubjectSeqClassCount = 0;
							if(optimizeType == optimizeType.TYPE_FREE){
								toOneSubjectSeqClassCount = Math.round((float)(oneSubject.seqSubjects[toSeq-1].getStudentCount()-swapStudenCount)/oneSubjectAvgTclassStudentCount);
							}else if(optimizeType == optimizeType.TYPE_CLASSCOUNT){
								toOneSubjectSeqClassCount = oneSubject.seqTclassCount[toSeq-1];
							}
							if((oneSubject.seqSubjects[toSeq-1].getStudentCount()-swapStudenCount)< (oneSubjectAvgTclassStudentCount-5)*toOneSubjectSeqClassCount 
									|| (oneSubject.seqSubjects[toSeq-1].getStudentCount()-swapStudenCount) > config.maxTclassStudentCount*toOneSubjectSeqClassCount){
								toOneSubjectSeqClassCount =  Math.round((float)oneSubject.seqSubjects[toSeq-1].getStudentCount()/oneSubjectAvgTclassStudentCount);
								if(oneSubject.seqSubjects[toSeq-1].getStudentCount()>(oneSubjectAvgTclassStudentCount-5)*toOneSubjectSeqClassCount 
									&& oneSubject.seqSubjects[toSeq-1].getStudentCount() <  config.maxTclassStudentCount*toOneSubjectSeqClassCount){
									swapStudenCount = Math.min(swapStudenCount,oneSubject.seqSubjects[toSeq-1].getStudentCount() - (oneSubjectAvgTclassStudentCount-5)*toOneSubjectSeqClassCount);
									}
							}
							
							if(swapStudenCount<=0){
								continue;
							}
				//			System.out.println("------可以将"+subjectCombination.subjectIdsStr+"    subjectId:"+subject.subjectId+" fromSeq:"+fromSeq+"  ->toSeq:"+toSeq+"  |     subjectId:"+oneSubjectId+" toSeq:"+toSeq+"  ->fromSeq:"+fromSeq+"  ("+swapStudenCount+"人)");			

							SubjectCombinationSwap subjectCombinationSwap = new SubjectCombinationSwap(subjectCombination);
							subjectCombinationSwap.swapSeqs[0]=fromSeq;
							subjectCombinationSwap.swapSeqs[1]=toSeq;
							subjectCombinationSwap.swapSubject[0]=subject;
							subjectCombinationSwap.swapSubject[1]=oneSubject;
							subjectCombinationSwap.swapStudenCount = swapStudenCount;
							subjectCombinationSwap.order = 1;
							if(subjectCombination.getTotalStudentCount() == swapStudenCount ){
								subjectCombinationSwap.order = 3;
							}
							totalSwapStudentCount-=swapStudenCount;
							subjectCombinationSwaps.add(subjectCombinationSwap);
							subjectCombinationSwap.doSwap();
							while(totalSwapStudentCount<=0){
								break;
							}
						}
					}
					
					//采用三个序列调换方式
			//		System.out.print("最终序列人数:(");
			//		for(int varSeq =0;varSeq<3;varSeq++){
				//		System.out.print(varSeq+":"+subject.seqSubjects[varSeq].getStudentCount()+",");
			//		}
			//		System.out.print(")");
			//		System.out.println("");
				}
			}
		}
		return orginTotalSwapStudentCount - totalSwapStudentCount;
	}
	/**
	 * 初步优化
	 * @param subject
	 * @param subjectIdMaps
	 * @param conflicts
	 * @param config
	 * @return
	 */
	public static ResultScore singleSubjectOptimize(Subject subject,Map<Integer,Subject>[] subjectIdMaps,List<Conflict> conflicts,PlacementTaskConfig config){
	//	System.out.println("------isOpt:"+subject.isOpt+"  subject:"+subject.subjectId+"开始优化--------"
	//			+subject.seqSubjects[0].getStudentCount()+","+subject.seqSubjects[1].getStudentCount()+","+subject.seqSubjects[2].getStudentCount());
	
		ResultScore orginResultScore  = ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,subject);
		if(!subject.isNeedOptimize(config,OptimizeType.TYPE_FREE)){
	//		System.out.println("------isOpt:"+subject.isOpt+"  subject:"+subject.subjectId+"不需要优化-----------------");
			return orginResultScore ;
		}

		Set<Integer[]> tclassCountsSet = new TreeSet(new Comparator<Integer[]>(){
			@Override
			public int compare(Integer[] o1, Integer[] o2) {
				// TODO Auto-generated method stub
				return JSONObject.toJSONString(o2).compareTo(JSONObject.toJSONString(o1));
			}
			
		});
		Map<Integer,Integer[]> subjectSeqStudentMap = new HashMap();
		List<Integer> baseArray = new ArrayList();
		for(int i=1;i<=config.seqCounts[subject.isOpt];i++){
			baseArray.add(i);
		}
		Set<List<Integer>> seqsList =CommonUtils.getPermutation(baseArray);
		Integer[] tclassCounts = new Integer[config.seqCounts[subject.isOpt]];
		for(int seq = 1;seq<=config.seqCounts[subject.isOpt];seq++){
			tclassCounts[seq-1] = subject.seqSubjects[seq-1].getStudentCount() / subject.avgTclassStudentCount;
			if(subject.seqSubjects[seq-1].getStudentCount()%subject.avgTclassStudentCount > subject.avgTclassStudentCount-5){
				tclassCounts[seq-1] += 1;
			}
		}
		int diff = subject.tclassCount - CommonUtils.sum(tclassCounts) ;
		if(diff > 0){
			for(List<Integer> seqs:seqsList){
				int diffTemp = diff;
				Integer[] oneTclassCounts= Arrays.copyOf(tclassCounts, tclassCounts.length);
				for(int seq:seqs){
					if(diffTemp <= 0){
						break;
					}
					oneTclassCounts[seq-1] +=1;
					diffTemp-=1;
				}
				tclassCountsSet.add(oneTclassCounts);
			}
		}else{
			tclassCountsSet.add(tclassCounts);
		}
		Iterator<Integer[]> it = tclassCountsSet.iterator();
		int canMaxAvailableClassDiscreteCount = (int) Math.ceil((float)subject.tclassCount/config.seqCounts[subject.isOpt]);
		while(it.hasNext()){
			Integer[] oneList = it.next();
			boolean isOk = true;
			for(int i= 0;i<oneList.length;i++){
				if(oneList[i]>canMaxAvailableClassDiscreteCount||CommonUtils.sum(oneList)>subject.tclassCount){
					isOk = false;
					it.remove();
					break;
				}
			}
		}
		Map<Integer,ResultScore> scoreResultMap  = new HashMap();
		List<List<SubjectCombinationSwap>> swapsList = new ArrayList();
	//	System.out.println("所有组合情况:"+JSONObject.toJSONString(tclassCountsSet));
		List<Integer[]> tclassCountsList = new ArrayList();
		tclassCountsList.add(subject.seqTclassCount);
		tclassCountsList.addAll(tclassCountsSet);
		ResultScore orginScore = ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,subject);
		for(int i=0;i<tclassCountsList.size();i++){
			Integer[] oneTclassCounts = tclassCountsList.get(i);
			Subject.backupSubject(subjectIdMaps[subject.isOpt].values());
			boolean isOK = false;
			List<SubjectCombinationSwap> subjectCombinationSwaps =  subjectOptimize(subject,subjectIdMaps, conflicts,config,OptimizeType.TYPE_FREE);
			ResultScore resultScore = ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,subject);
			if(resultScore.compareTo(orginScore)>0){
				scoreResultMap.put(i,resultScore);
			}else{
				scoreResultMap.put(i,new ResultScore());
			}
			swapsList.add(subjectCombinationSwaps);
			Subject.restoreSubject(subjectIdMaps[subject.isOpt].values());
		}
		List<Map.Entry<Integer, ResultScore>> entrys = new ArrayList(scoreResultMap.entrySet());
		Collections.sort(entrys, new Comparator<Map.Entry<Integer, ResultScore>>(){
			@Override
			public int compare(Entry<Integer, ResultScore> o1,
					Entry<Integer, ResultScore> o2) {
				// TODO Auto-generated method stub
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		//System.out.println("----------scoreResultMap.size():"+JSONObject.toJSONString(entrys));
		//如果优化
		if(entrys.get(0).getValue().compareTo(orginResultScore) >= 1){
		//	System.out.println("---------------------------进行实际优化-----------------------------");
			List<SubjectCombinationSwap> swaps = swapsList.get(entrys.get(0).getKey());
			subject.seqTclassCount = Arrays.copyOf(tclassCountsList.get(entrys.get(0).getKey()), config.seqCounts[subject.isOpt]);
			for(SubjectCombinationSwap swap:swaps){
				swap.doSwap();
			}
	//		System.out.println("---------------------------实际优化完毕----------------------------");
		}
		Collection<Subject>  subjects = subjectIdMaps[subject.isOpt].values();
		Subject[] subjectArray = new Subject[subjects.size()];
		subjects.toArray(subjectArray);
		orginResultScore  = ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,subjectArray);
		return orginResultScore;
	}
	
	/**
	 * 保证每个班人数不会超过最大人数
	 * @param subject
	 * @param subjectIdMaps
	 * @param conflicts
	 * @param config
	 * @return
	 */
	public static void availableOptimize(Collection<Subject> subjects,Map<Integer,Subject>[] subjectIdMaps,List<Conflict> conflicts,PlacementTaskConfig config){
		for(Subject subject:subjects){
			System.out.println("--------检查subject:"+subject.subjectId+" isOpt:"+subject.isOpt+"------");
			for(int seq=1;seq <=config.seqCounts[subject.isOpt];seq++){
				System.out.println("--------"+subject.seqSubjects[seq-1].getStudentCount()+" -----"+subject.seqTclassCount[seq-1]);
				if(subject.seqTclassCount[seq-1]==0){
					if(subject.seqSubjects[seq-1].getStudentCount()!=0){
						subject.seqTclassCount[seq-1]+=1;
						subject.tclassCount =  CommonUtils.sum(subject.seqTclassCount);
						subject.avgTclassStudentCount = subject.studentCount / subject.tclassCount;
					}else{
						continue;
					}
				}
				if(Math.ceil((float)subject.seqSubjects[seq-1].getStudentCount() /subject.seqTclassCount[seq-1]) > config.maxTclassStudentCount){
					subject.seqTclassCount[seq-1]+=1;
					subject.tclassCount =  CommonUtils.sum(subject.seqTclassCount);
					subject.avgTclassStudentCount = subject.studentCount / subject.tclassCount;
				}
			}
			System.out.println("--------结果检查subject:"+subject.subjectId+" isOpt:"+subject.isOpt+"------"+JSONObject.toJSONString(subject.seqTclassCount));
			subjectOptimize(subject,subjectIdMaps, conflicts,config,OptimizeType.TYPE_CLASSCOUNT);
		}
	}
	/**
	 * 深度优化
	 * @param subject
	 * @param subjectIdMaps
	 * @param conflicts
	 * @param config
	 * @return
	 */
	public static ResultScore subjectDeepOptimize(Subject subject,Map<Integer,Subject>[] subjectIdMaps,List<Conflict> conflicts,PlacementTaskConfig config){
	//	System.out.println("----------subjectId:"+subject.subjectId+" isOpt:"+subject.isOpt+" 深度优化-----------------------");
		Map<SeqSubject,Float> avgTclassStudentMap = new HashMap();
		for(SeqSubject seqSubject:subject.seqSubjects){
			int tclassCount = Math.round((float)seqSubject.getStudentCount() / subject.avgTclassStudentCount);
			subject.seqTclassCount[seqSubject.seq-1]=tclassCount;
			avgTclassStudentMap.put(seqSubject,(float)seqSubject.getStudentCount()/tclassCount);
		}
		List<Map.Entry<SeqSubject,Float>> avgTclassStudentMapEntrys = new ArrayList(avgTclassStudentMap.entrySet());
		Collections.sort(avgTclassStudentMapEntrys , new Comparator<Map.Entry<SeqSubject,Float>>(){
			@Override
			public int compare(
					Map.Entry<SeqSubject,Float> o1,
					Map.Entry<SeqSubject,Float> o2) {
				// TODO Auto-generated method stub
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		List<SubjectCombinationSwap> subjectCombinationSwaps = new ArrayList();
		//优化前科目打分
		ResultScore orginResultScore = ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,subject);
		Subject.backupSubject(subjectIdMaps[subject.isOpt].values());
		for(int i=0;i<avgTclassStudentMapEntrys.size();i++){
			for(int j=avgTclassStudentMapEntrys.size()-1;j>=0;j--){
				if(i>=j){
					continue;
				}
				SeqSubject oneSeqSubject =  avgTclassStudentMapEntrys.get(i).getKey();
				SeqSubject twoSeqSubject =  avgTclassStudentMapEntrys.get(j).getKey();
				if(subject.seqTclassCount[oneSeqSubject.seq-1]==0||subject.seqTclassCount[twoSeqSubject.seq-1]==0){
					continue;
				}
				float curAvgTclassStudentCount = 
						(oneSeqSubject.getStudentCount()+twoSeqSubject.getStudentCount())/(subject.seqTclassCount[oneSeqSubject.seq-1]+subject.seqTclassCount[twoSeqSubject.seq-1]);
				SeqSubject addSeqSubject = null;
				SeqSubject minusSeqSubject = null;
				if((float)oneSeqSubject.getStudentCount()/subject.seqTclassCount[oneSeqSubject.seq-1]>curAvgTclassStudentCount){
					 minusSeqSubject= oneSeqSubject;
					 addSeqSubject = twoSeqSubject;
				}else{
					minusSeqSubject = twoSeqSubject;
					addSeqSubject = oneSeqSubject;
				}
				
				int minusStudentCount = (int) (minusSeqSubject.getStudentCount() - curAvgTclassStudentCount*subject.seqTclassCount[minusSeqSubject.seq-1]) ;
				int addStudentCount =(int) ( curAvgTclassStudentCount*subject.seqTclassCount[addSeqSubject.seq-1]- addSeqSubject.getStudentCount()) ;
				if(minusStudentCount<=1||addStudentCount<=1){
					continue;
				}
				int fromSeq = minusSeqSubject.seq;
				int toSeq = addSeqSubject.seq;
				List<SubjectCombination> subjectCombinations = new ArrayList(subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.keySet());
				boolean isOK = false;
				for(SubjectCombination subjectCombination:subjectCombinations){
					if(subjectCombination.getConfictType() == ConflictType.TYPE_4 
							||subjectCombination.getConfictType() == ConflictType.TYPE_5
							|| subjectCombination.getConfictType() == ConflictType.TYPE_6 ){
						continue;
					}
					if(checkConflict(subject.subjectId, toSeq, subjectCombination,conflicts, subject.isOpt,subjectIdMaps)){
						for(Integer oneSubjectId:subjectCombination.subjectIds[subject.isOpt]){
							if(!checkConflict(oneSubjectId, fromSeq, subjectCombination,conflicts, subject.isOpt,subjectIdMaps)){
								continue;
							}
							int swapStudenCount = Math.min(addStudentCount,minusStudentCount);
							Subject oneSubject = subjectIdMaps[subject.isOpt].get(oneSubjectId);
							if(subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.get(subjectCombination) ==null){
								continue;
							}
							if(oneSubject.seqSubjects[toSeq-1].fromSubjectCombinationMap.get(subjectCombination) ==null){
								continue;
							}
							//from多余的人数
							swapStudenCount = Math.min(swapStudenCount, subject.seqSubjects[fromSeq-1].fromSubjectCombinationMap.get(subjectCombination));
							//to对应科目可以移动的人数
							swapStudenCount = Math.min(swapStudenCount, oneSubject.seqSubjects[toSeq-1].fromSubjectCombinationMap.get(subjectCombination));	
							//移动后from序列 中 subject人数是否合理
							int subjectOffSet = subject.getMaxOffset();
							int fromSubjectSeqClassCount = Math.round((float)(subject.seqSubjects[fromSeq-1].getStudentCount()-swapStudenCount)/subject.avgTclassStudentCount);
							if((subject.seqSubjects[fromSeq-1].getStudentCount()-swapStudenCount)< (subject.avgTclassStudentCount-subjectOffSet)*fromSubjectSeqClassCount 
									|| (subject.seqSubjects[fromSeq-1].getStudentCount()-swapStudenCount) > config.maxTclassStudentCount*fromSubjectSeqClassCount){
								fromSubjectSeqClassCount =  Math.round((float)subject.seqSubjects[fromSeq-1].getStudentCount()/subject.avgTclassStudentCount);
								if(subject.seqSubjects[fromSeq-1].getStudentCount()>(subject.avgTclassStudentCount-subjectOffSet)*fromSubjectSeqClassCount 
									&& subject.seqSubjects[fromSeq-1].getStudentCount() <  config.maxTclassStudentCount*fromSubjectSeqClassCount){
									//调整必须满足基本规则 班级人数>班级平均人数-5 &&班级人数 <最大班级人数
									swapStudenCount = Math.min(swapStudenCount,subject.seqSubjects[fromSeq-1].getStudentCount() - (subject.avgTclassStudentCount-subjectOffSet)*fromSubjectSeqClassCount);
									}
							}
							//均衡两个序列人数
							swapStudenCount = (int) Math.min(swapStudenCount,subject.seqSubjects[fromSeq-1].getStudentCount() - curAvgTclassStudentCount*fromSubjectSeqClassCount);
							// System.out.println("-------------------移动后from序列 中 oneSubject人数 fromOneSubjectClassCount："+fromOneSubjectClassCount+"  ")
							//oneSubject的to序列的班级平均人数必须>from序列的班级平均分数
							int toOneSubjectSeqClassCountOrgin = Math.round((float)(oneSubject.seqSubjects[toSeq-1].getStudentCount())/oneSubject.avgTclassStudentCount);
							int fromOneSubjectSeqClassCountOrgin = Math.round((float)(oneSubject.seqSubjects[fromSeq-1].getStudentCount())/oneSubject.avgTclassStudentCount);
							if(toOneSubjectSeqClassCountOrgin==0||fromOneSubjectSeqClassCountOrgin==0){
								continue;
							}
							if(oneSubject.seqSubjects[toSeq-1].getStudentCount()/toOneSubjectSeqClassCountOrgin 
									> oneSubject.seqSubjects[fromSeq-1].getStudentCount()/fromOneSubjectSeqClassCountOrgin){
								int avgOffset = Math.max(5, (subject.getMaxOffset()+oneSubject.getMaxOffset())/2);
								//移动后to序列 中 oneSubject人数是否合理
								int toOneSubjectSeqClassCount = Math.round((float)(oneSubject.seqSubjects[toSeq-1].getStudentCount()-swapStudenCount)/oneSubject.avgTclassStudentCount);
								if((oneSubject.seqSubjects[toSeq-1].getStudentCount()-swapStudenCount)< (oneSubject.avgTclassStudentCount-avgOffset)*toOneSubjectSeqClassCount 
										|| (oneSubject.seqSubjects[toSeq-1].getStudentCount()-swapStudenCount) > config.maxTclassStudentCount*toOneSubjectSeqClassCount){
									toOneSubjectSeqClassCount =  Math.round((float)oneSubject.seqSubjects[toSeq-1].getStudentCount()/oneSubject.avgTclassStudentCount);
									if(oneSubject.seqSubjects[toSeq-1].getStudentCount()>(oneSubject.avgTclassStudentCount-avgOffset)*toOneSubjectSeqClassCount 
										&& oneSubject.seqSubjects[toSeq-1].getStudentCount() <  config.maxTclassStudentCount*toOneSubjectSeqClassCount){
										//调整必须满足基本规则 班级人数>班级平均人数-两个科目平均班级人数偏差值 &&班级人数 <最大班级人数
										swapStudenCount = Math.min(swapStudenCount,oneSubject.seqSubjects[toSeq-1].getStudentCount() - (oneSubject.avgTclassStudentCount-avgOffset)*toOneSubjectSeqClassCount);
									}
								}
							}	
							float currentAvgTclassStudentCount = (float)(oneSubject.seqSubjects[toSeq-1].getStudentCount()+oneSubject.seqSubjects[fromSeq-1].getStudentCount())/(toOneSubjectSeqClassCountOrgin+fromOneSubjectSeqClassCountOrgin);
							swapStudenCount = (int) Math.min(swapStudenCount,oneSubject.seqSubjects[toSeq-1].getStudentCount()-currentAvgTclassStudentCount*toOneSubjectSeqClassCountOrgin);
							if(swapStudenCount<=0){
								continue;
							}
							SubjectCombinationSwap subjectCombinationSwap = new SubjectCombinationSwap(subjectCombination);
							subjectCombinationSwap.swapSeqs[0]=fromSeq;
							subjectCombinationSwap.swapSeqs[1]=toSeq;
							subjectCombinationSwap.swapSubject[0]=subject;
							subjectCombinationSwap.swapSubject[1]=oneSubject;
							subjectCombinationSwap.swapStudenCount = swapStudenCount;
							subjectCombinationSwap.order = 1;
							if(subjectCombination.getTotalStudentCount() == swapStudenCount ){
								subjectCombinationSwap.order = 3;
							}
							subjectCombinationSwaps.add(subjectCombinationSwap);
							subjectCombinationSwap.doSwap();
							addStudentCount -=swapStudenCount;
							minusStudentCount -=swapStudenCount;
						}
					}
				}
			}
		}
		//优化后科目打分
		if(ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,subject).compareTo(orginResultScore)<1){
			Subject.restoreSubject(subjectIdMaps[subject.isOpt].values());
		}
		return ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,subject);
	}

	/**
	 * 执行分班
	 * @param subjectCombinationsMap
	 * @param config
	 * @return
	 */
	public static PlacementResult executePlacement(Map<String,Integer> subjectCombinationsMap,PlacementTaskConfig config){
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
					currentSubjectBean.subjectCombinationListBak.add(subjectCombination);
				}
			}
		}
		//计算学选交叉
	//	List<List<Conflict>> allConflicts = Conflict.conflictStats(config, subjectIdMaps);
//		if(allConflicts.size()==0){
//			allConflicts.add(new ArrayList());
//		}
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
		
		
		List<PlacementResult> results = new ArrayList();
		int tryTimes =50;
		boolean isOK = false;
		List<ConflictComputeResult>  conflictComputeResults = ConflictComputer.compute(subjectIdMaps,subjectCombinations,config);
		do{
			Map<Integer,Integer[]>[] seqTclassCountMaps = null;
			int times = 1000;
			do{
				seqTclassCountMaps = new Map[2];
				//计算选考学考开班数
				for(Integer intCd:new Integer[]{CD_OPT,CD_PRO}){
					seqTclassCountMaps[intCd] = statsSeqTclassCount(new ArrayList(subjectIdMaps[intCd].values()),config);
				}
				times --;
			}while(seqTclassCountMapsSet.contains(seqTclassCountMaps)&&times>0);
			if(seqTclassCountMapsSet.contains(seqTclassCountMaps)&&isOK){
				break;
			}
			seqTclassCountMapsSet.add(seqTclassCountMaps);
			//有学选交叉的情况下
			if(conflictComputeResults!=null&&conflictComputeResults.size()!=0){
				for(ConflictComputeResult conflictComputeResult:conflictComputeResults){
					System.out.println("------------------打印学选交叉--------------------");
					for(Conflict conflict:conflictComputeResult.conflicts){
						System.out.println("-------------------");
						System.out.println("opt:"+JSONObject.toJSONString(conflict.subjectIds[1])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[1]));
						System.out.println("pro:"+JSONObject.toJSONString(conflict.subjectIds[0])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[0]));
						System.out.println("-------------------");
					}
					
					PlacementResult result = executePlacement( subjectIdMaps,
							config,
							seqTclassCountMaps,
							subjectCombinations,
							conflictComputeResult);
					
					if(result==null){
						continue;
					}
					if(result.resultCode > 0){
						results.add(result);
						if(result.score.isGood(config)){
							isOK = true;
							break;
						}
					}
				}
				if(isOK){
					break;
				}
			}else{
			//无学选交叉的情况下
				PlacementResult result = executePlacement( subjectIdMaps,
						config,
						seqTclassCountMaps,
						subjectCombinations,
						null);
				if(result.resultCode > 0){
					results.add(result);
					if(result.score.isGood(config)){
						isOK = true;
						break;
					}
				}
			}
			tryTimes --;
		}while(tryTimes>0||results.size()==0);
		if(results.size()==0){
			return new PlacementResult(-1);
		}

		//找出最优结果
		Collections.sort(results,new Comparator<PlacementResult>(){
			@Override
			public int compare(PlacementResult o1, PlacementResult o2) {
				// TODO Auto-generated method stub
				return o2.score.compareTo(o1.score);
			}
		});
		//PlacementResult result = results.stream().max((a,b)->b.score.compareTo(a.score)).get();
		return results.get(0);
	}
	
	
	/**
	 * 执行分班
	 * @param subjectCombinations 所有科目组合
	 * @param config 分班配置
	 * @return
	 */
	private static PlacementResult executePlacement(
			Map<Integer,Subject>[] subjectIdMaps,
			PlacementTaskConfig config,
			Map<Integer,Integer[]>[] tclassCountMap,
			List<SubjectCombination> subjectCombinations,
			ConflictComputeResult conflictComputeResult
			){
		List<Conflict>  conflicts  = null;
		Map<SubjectCombination,SubSubjectCombination[][]> subSubjectCombinationsMap =null;
		if(conflictComputeResult !=null){
			conflicts = conflictComputeResult.conflicts;
			subSubjectCombinationsMap = conflictComputeResult.subSubjectCombinationMap;
		}else{
			conflicts = new ArrayList();
			subSubjectCombinationsMap = new HashMap();
		}
//		System.out.println("------------------打印学选交叉--------------------");
//		for(Conflict conflict:conflictComputeResult.conflicts){
//			System.out.println("-------------------");
//			System.out.println("opt:"+JSONObject.toJSONString(conflict.subjectIds[1])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[1]));
//			System.out.println("pro:"+JSONObject.toJSONString(conflict.subjectIds[0])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[0]));
//			System.out.println("-------------------");
//		}
//		System.out.println("------------------打印志愿情况--------------------");
//		for(SubjectCombination sc1:conflictComputeResult.subjectCombinationStatusMap.keySet()){
//			System.out.println(sc1.getSubjectIdsStr()+"   "+sc1.getConfictType());
//		}
		//初始化科目数据
		for(Integer intCd:new Integer[]{CD_OPT,CD_PRO}){
			for(Subject subject:subjectIdMaps[intCd].values()){
				subject.seqSubjects = new SeqSubject[config.seqCounts[subject.isOpt]];
				subject.subjectCombinationList = new ArrayList(subject.subjectCombinationListBak);
				for(int i=0;i<config.seqCounts[subject.isOpt];i++){
					subject.seqSubjects[i]=new SeqSubject(i+1,subject);
				}
				subject.remainStudentCount = subject.studentCount;
				subject.seqMustArrangeStudents = new int[config.seqCounts[CD_PRO]];
			}
		}
		//初始化志愿组合数据
		for(SubjectCombination subjectCombination:subjectCombinations){
			for(Integer optSubjectId:subjectCombination.subjectIds[CD_OPT]){
				subjectCombination.remainSubjectStudentCountMaps[1].put(optSubjectId, subjectCombination.getTotalStudentCount());
			}
			for(Integer proSubjectId:subjectCombination.subjectIds[CD_PRO]){
				subjectCombination.remainSubjectStudentCountMaps[0].put(proSubjectId, subjectCombination.getTotalStudentCount());
			}
			for(int seq=1;seq<=config.seqCounts[CD_OPT];seq++){
				subjectCombination.remainSeqStudentCounts[CD_OPT][seq-1]= subjectCombination.getTotalStudentCount();
			}
			for(int seq=1;seq<=config.seqCounts[CD_PRO];seq++){
				subjectCombination.remainSeqStudentCounts[CD_PRO][seq-1]= subjectCombination.getTotalStudentCount();
			}
			if(conflictComputeResult!=null){
				SubjectCombinationStatus stats = conflictComputeResult.subjectCombinationStatusMap.get(subjectCombination);
				if(stats!=null){
					stats.setStatus(subjectCombination);
				}
			}
		}
		
		//科目Id和科目实体Bean映射Map
		List<Tclass> allTclassList = new ArrayList();
		for(Integer intCd:new Integer[]{CD_OPT,CD_PRO}){
			for(Subject subjectBean:subjectIdMaps[intCd].values()){
				Integer[] tclassCounts = tclassCountMap[intCd].get(subjectBean.subjectId);
				subjectBean.tclassCount = CommonUtils.sum(tclassCounts);
				subjectBean.avgTclassStudentCount = subjectBean.studentCount / subjectBean.tclassCount;
				subjectBean.seqTclassCount = Arrays.copyOf(tclassCounts, config.seqCounts[subjectBean.isOpt]);
				subjectBean.remainStudentCount = subjectBean.studentCount;
				System.out.println(subjectBean.subjectId+":"+JSONObject.toJSONString(tclassCounts));
			}
		}
		
		//将科目序列格子加入到冲突中
		for(Conflict conflict:conflicts){
			conflict.clear();
			for(int cd = 0 ;cd < 2;cd++){
				for(Integer subjectId:conflict.subjectIds[cd]){
					conflict.addSeqSubject(subjectIdMaps[cd].get(subjectId).seqSubjects);
				}
			}
		}
		
		List<Subject>[] subjectLists =  new List[]{new ArrayList(subjectIdMaps[CD_OPT].values()),new ArrayList(subjectIdMaps[CD_PRO].values())};
		//学课加入学选交叉
		//为每个序列科目(非学选交叉科目)划分人数
		for(List<Subject> subjectList :subjectLists){
	//		System.out.println("-----------------------------------------------------------------");
			//轮次分班
			int isOpt = subjectList.get(0).isOpt;
			//根据选考的人数安排，排学考的必排志愿组合
			if(isOpt==CD_PRO){
				for(SubjectCombination subjectCombination:subjectCombinations){
					if(subjectCombination.getConfictType() == ConflictType.TYPE_0 || subjectCombination.getConfictType() == ConflictType.TYPE_1 ){
						continue;
					}
					SubSubjectCombination[][] subSubjectCombinations = subSubjectCombinationsMap.get(subjectCombination);		
					Subject subject0 = subjectIdMaps[isOpt].get(subjectCombination.getOrderProSubjectIds().get(0));
					Subject subject1 = subjectIdMaps[isOpt].get(subjectCombination.getOrderProSubjectIds().get(1));
					Subject subject2 = subjectIdMaps[isOpt].get(subjectCombination.getOrderProSubjectIds().get(2));
					for(int subjectIndex =0 ;subjectIndex < config.seqCounts[CD_PRO]; subjectIndex ++){
						Subject subject = subjectIdMaps[isOpt].get(subjectCombination.getOrderProSubjectIds().get(subjectIndex));
						for(int seq = 0;seq<config.seqCounts[CD_PRO];seq++){
							SubSubjectCombination  subSubjectCombination = subSubjectCombinations[seq][subjectIndex];
							if(subSubjectCombination==null){
								continue;
							}
							int studentCount = 0;
							if(subSubjectCombination.subType == SubSubjectCombinationType.ALL){
								studentCount = subSubjectCombination.parent.getTotalStudentCount();
							}else{
								subSubjectCombination.subject = subjectIdMaps[CD_OPT].get(subSubjectCombination.subjectId);
								studentCount = subSubjectCombination.getTotalStudentCount();
							}
							 if(studentCount!=0){
								subject.seqSubjects[seq].fromSubjectCombinationMap.put(subSubjectCombination, studentCount);
							}
							subjectCombination.remainSeqStudentCounts[isOpt][seq] = 0;
						}
						subject.remainStudentCount -=subjectCombination.getTotalStudentCount();
						subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject.subjectId,0);
						
					}
//					subject0.remainStudentCount -=subjectCombination.getTotalStudentCount();
//					subject1.remainStudentCount -=subjectCombination.getTotalStudentCount();
//					subject2.remainStudentCount -=subjectCombination.getTotalStudentCount();
//					subjectCombination.remainSeqStudentCounts[isOpt][0] = 0;
//					subjectCombination.remainSeqStudentCounts[isOpt][1] = 0;
//					subjectCombination.remainSeqStudentCounts[isOpt][2] = 0;
//					subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subjectCombination.subjectIds[isOpt].get(0), 0);
//					subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subjectCombination.subjectIds[isOpt].get(1), 0);
//					subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subjectCombination.subjectIds[isOpt].get(2), 0);
				}
				
				//将所有type1的志愿组合分拆
				for(Subject subject:subjectList){ 
					Iterator<SubjectCombination> it = subject.subjectCombinationList.iterator();
					List<SubjectCombination> type1CombList = new ArrayList();
					while(it.hasNext()){
						SubjectCombination subjectCombination= it.next();
						if(subjectCombination.getConfictType() == ConflictType.TYPE_1){
							SubSubjectCombination[][] subSubjectCombinations = subSubjectCombinationsMap.get(subjectCombination);
							for(int seq=0;seq<config.seqCounts[CD_PRO];seq++){
								SubSubjectCombination  subSubjectCombination = subSubjectCombinations[seq][0];
								if(subSubjectCombination==null){
									continue;
								}
								subSubjectCombination.subject = subjectIdMaps[CD_OPT].get(subSubjectCombination.subjectId);
								int studentCount = subSubjectCombination.getTotalStudentCount();
								if(studentCount!=0){
									type1CombList.add(subSubjectCombination);
									subSubjectCombination.remainSeqStudentCounts = new int[2][config.seqCounts[CD_PRO]];
									for(int oneSeq = 0;oneSeq<config.seqCounts[CD_PRO];oneSeq++){
										subSubjectCombination.remainSeqStudentCounts[CD_PRO][oneSeq]=studentCount;
									}
									for(int proSubjectId:subSubjectCombination.parent.subjectIds[CD_PRO]){
										subSubjectCombination.remainSubjectStudentCountMaps[CD_PRO].put(proSubjectId, studentCount);
									}
								}
							}
							it.remove();
						}
					}
					subject.subjectCombinationList.addAll(type1CombList);
				}
			
			}
			for(int vSeq=1;vSeq<=config.seqCounts[isOpt];vSeq++){	
				Collections.shuffle(subjectList);
//				Collections.sort(subjectList, new Comparator<Subject>(){
//					@Override
//					public int compare(Subject o1, Subject o2) {
//						// TODO Auto-generated method stub
//						Integer o1IsConflictSubject  = 0;
//						Integer o2IsConflictSubject  = 0;
//						o1IsConflictSubject = o1.isConflictSubject(conflicts);
//						o2IsConflictSubject = o2.isConflictSubject(conflicts);
//						return o2IsConflictSubject.compareTo(o1IsConflictSubject)==0 ? 
//								o1.remainStudentCount.compareTo(o2.remainStudentCount)
//								:o2IsConflictSubject.compareTo(o1IsConflictSubject);
//					}
//				});
				//必须排学选交叉规则组合
				for(SubjectCombination subjectCombination:subjectCombinations){
					//String[][] type3Model = new String[][]{{"x","y","z"},{"y","y1","z1"},{"z","z1","z2"}};
					//String[][] type4Model = new String[][]{{"x","y","z"},{"y","z","x"},{"z","x","y"}};
					//String[][] type5Model = new String[][]{{"x","y","0"},{"y","x","0"},{"0","0","z"}};
					//String[][] type6Model = new String[][]{{"x","0","0"},{"0","y","0"},{"0","0","z"}};
					if(isOpt==CD_OPT){
						if(vSeq==1){
							 if(subjectCombination.getConfictType() == ConflictType.TYPE_6){
									Subject subject0 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(0));
									Subject subject1 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(1));
									Subject subject2 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(2));
									subject0.seqSubjects[0].fromSubjectCombinationMap.put(subjectCombination,subjectCombination.getTotalStudentCount() );
									subject1.seqSubjects[1].fromSubjectCombinationMap.put(subjectCombination,subjectCombination.getTotalStudentCount() );
									subject2.seqSubjects[2].fromSubjectCombinationMap.put(subjectCombination,subjectCombination.getTotalStudentCount() );
									subject0.remainStudentCount -=subjectCombination.getTotalStudentCount();
									subject1.remainStudentCount -=subjectCombination.getTotalStudentCount();
									subject2.remainStudentCount -=subjectCombination.getTotalStudentCount();
									subjectCombination.remainSeqStudentCounts[isOpt][0] = 0;
									subjectCombination.remainSeqStudentCounts[isOpt][1] = 0;
									subjectCombination.remainSeqStudentCounts[isOpt][2] = 0;
									subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject0.subjectId, 0);
									subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject1.subjectId, 0);
									subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject2.subjectId, 0);
							 }else if(subjectCombination.getConfictType() == ConflictType.TYPE_5){
									Subject subject2 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(2));
									subject2.seqSubjects[2].fromSubjectCombinationMap.put(subjectCombination,subjectCombination.getTotalStudentCount());
									subject2.remainStudentCount -=subjectCombination.getTotalStudentCount();
									subjectCombination.remainSeqStudentCounts[isOpt][2] = 0;
									subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject2.subjectId, 0);
							 }
						}else if(vSeq==2){
							if(subjectCombination.getConfictType() == ConflictType.TYPE_4){
								Subject subject0 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(0));
								Subject subject1 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(1));
								Subject subject2 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(2));
								Integer studentCount00 = subject0.seqSubjects[0].fromSubjectCombinationMap.get(subjectCombination);
								if(studentCount00!=null){
									subject2.seqSubjects[1].fromSubjectCombinationMap.put(subjectCombination, studentCount00);
									subject1.seqSubjects[2].fromSubjectCombinationMap.put(subjectCombination, studentCount00);
									subject0.remainStudentCount+=studentCount00;
								}
								Integer studentCount10 = subject1.seqSubjects[0].fromSubjectCombinationMap.get(subjectCombination);
								if(studentCount10!=null){
									subject0.seqSubjects[1].fromSubjectCombinationMap.put(subjectCombination, studentCount10);
									subject2.seqSubjects[2].fromSubjectCombinationMap.put(subjectCombination, studentCount10);
									subject1.remainStudentCount+=studentCount10;
								}
								Integer studentCount20 = subject2.seqSubjects[0].fromSubjectCombinationMap.get(subjectCombination);
								if(studentCount20!=null){
									subject1.seqSubjects[1].fromSubjectCombinationMap.put(subjectCombination, studentCount20);
									subject0.seqSubjects[2].fromSubjectCombinationMap.put(subjectCombination, studentCount20);
									subject2.remainStudentCount+=studentCount20;
								}
								subject0.remainStudentCount -=subjectCombination.getTotalStudentCount();
								subject1.remainStudentCount -=subjectCombination.getTotalStudentCount();
								subject2.remainStudentCount -=subjectCombination.getTotalStudentCount();
								subjectCombination.remainSeqStudentCounts[isOpt][0] = 0;
								subjectCombination.remainSeqStudentCounts[isOpt][1] = 0;
								subjectCombination.remainSeqStudentCounts[isOpt][2] = 0;
								subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject0.subjectId, 0);
								subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject1.subjectId, 0);
								subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject2.subjectId, 0);
							}else if(subjectCombination.getConfictType() == ConflictType.TYPE_5){
								Subject subject0 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(0));
								Subject subject1 = subjectIdMaps[isOpt].get(subjectCombination.getOrderOptSubjectIds().get(1));
								Integer studentCount00 = subject0.seqSubjects[0].fromSubjectCombinationMap.get(subjectCombination);
								if(studentCount00!=null){
									subject1.seqSubjects[1].fromSubjectCombinationMap.put(subjectCombination, studentCount00);
									subject0.remainStudentCount+=studentCount00;
								}
								Integer studentCount10 = subject1.seqSubjects[0].fromSubjectCombinationMap.get(subjectCombination);
								if(studentCount10!=null){
									subject0.seqSubjects[1].fromSubjectCombinationMap.put(subjectCombination, studentCount10);
									subject1.remainStudentCount+=studentCount10;
								}
								subject0.remainStudentCount -=subjectCombination.getTotalStudentCount();
								subject1.remainStudentCount -=subjectCombination.getTotalStudentCount();
								subjectCombination.remainSeqStudentCounts[isOpt][0] = 0;
								subjectCombination.remainSeqStudentCounts[isOpt][1] = 0;
								subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject0.subjectId, 0);
								subjectCombination.remainSubjectStudentCountMaps[isOpt].put(subject1.subjectId, 0);
							}
						}
					}
				}
				//已经拍完的科目
				final List<Integer> oneSeqFinishSubjects = new ArrayList();
				//科目分班
				for(Subject subjectBean:subjectList){
					//为每个序列科目分配人数
					SeqSubject seqSubject =subjectBean.seqSubjects[vSeq-1];
					seqSubject.addStudent(subjectIdMaps[subjectBean.isOpt],
							oneSeqFinishSubjects,
							conflicts,
							config,
							subjectIdMaps,
							subSubjectCombinationsMap);
					oneSeqFinishSubjects.add(subjectBean.subjectId);
				}
			}
//			if(isOpt==CD_OPT){
//				int times = 10;
//				do{
//					for(Subject subject:subjectIdMaps[CD_PRO].values()){
//						conflictProSubjectOptimize(subject,subjectIdMaps,conflicts,config);
//					}
//					times--;
//				}while(times>0);
//			}
			int times = 3;
			ResultScore score = new ResultScore();
			do{
				for(Subject subject:subjectIdMaps[isOpt].values()){
					score = singleSubjectOptimize(subject,subjectIdMaps,conflicts,config);
				}
				times --;
			}while(score.goodCount<18&&times>0);
			Subject[] subjectArray = new Subject[subjectIdMaps[isOpt].values().size()];
			subjectIdMaps[isOpt].values().toArray(subjectArray);
			if(isOpt==CD_PRO){
				doubleSubjectOptimize(subjectIdMaps,conflicts,config,isOpt);
				ResultScore rs1 = ResultScore.scoreResult(config, OptimizeType.TYPE_FREE, subjectArray);
				if(!rs1.isAvailable(config,isOpt)){
					availableOptimize(subjectIdMaps[isOpt].values(),subjectIdMaps, conflicts,config);
					doubleSubjectOptimize(subjectIdMaps,conflicts,config,isOpt);
				}
			}
			times = 1;
			while(times>0){
				for(Subject subject:subjectIdMaps[isOpt].values()){
					subjectDeepOptimize(subject,subjectIdMaps,conflicts,config);
				}
				times--;
			}
			
			//选考排的结果不合格的情况下，就不要往下排了
			if(isOpt==CD_OPT){
				ResultScore rs = ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,subjectArray);
				if(!rs.isAvailable(config,isOpt)){
					return new PlacementResult(-1);
				}
			}
		

		}
		List<Conflict> conflictList = new ArrayList();
		PlacementResult result = new PlacementResult(1,allTclassList,conflictList);
		result.resultCode = 1;
		List<Subject> optSubjectList = new ArrayList(subjectIdMaps[CD_OPT].values());
		Subject[] optSubjects = new Subject[optSubjectList.size()];
		optSubjectList.toArray(optSubjects);
		List<Subject> proSubjectList = new ArrayList(subjectIdMaps[CD_PRO].values());
		Subject[] proSubjects = new Subject[optSubjectList.size()];
		proSubjectList.toArray(proSubjects);
		ResultScore currentScore = ResultScore.add(ResultScore.scoreResult(config,OptimizeType.TYPE_FREE, optSubjects),ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,proSubjects));
	//	if(currentScore.availableCount>=36){
			//深度优化
//		for(int intCd:new int[]{CD_OPT,CD_PRO}){
//			int times = 1;
//			for(Subject subject:subjectIdMaps[intCd].values()){
//				subjectDeepOptimize(subject,subjectIdMaps,conflicts,config);
//			}
//				
//		}
//		}
		
		//创建班级
		result.score = ResultScore.add(ResultScore.scoreResult(config,OptimizeType.TYPE_FREE, optSubjects),ResultScore.scoreResult(config,OptimizeType.TYPE_FREE,proSubjects));

		System.out.println("-----------------------------------------------");
		for(Integer intCd:new Integer[]{CD_OPT,CD_PRO}){
			showResult(new ArrayList(subjectIdMaps[intCd].values()),subjectCombinations,subjectIdMaps[intCd],config,conflicts);
		}
		System.out.println("--------------------------------------"+JSONObject.toJSONString(result.score));
		List<Tclass> tclassList = new ArrayList();
		result.tclassList = tclassList;
		//保存各个子志愿集合名
		Set<String> subSubjectCominationStrs = new HashSet();
		result.subSubjectCominationStrs = subSubjectCominationStrs;
		for(Subject subject:subjectIdMaps[CD_PRO].values()){
			for(SeqSubject seqSubject:subject.seqSubjects){
				for(SubjectCombination sc :seqSubject.fromSubjectCombinationMap.keySet()){
					if(sc instanceof SubjectCombination){
						subSubjectCominationStrs.add(sc.getSubjectIdsStr());
					}
				}
				
			}
		}
		
		//根据序列科目格子人数分班
		for(Integer intCd:new Integer[]{CD_OPT,CD_PRO}){
			for(Subject subject:subjectIdMaps[intCd].values()){
				for(int seq = 1;seq<=config.seqCounts[intCd];seq++){
					SeqSubject seqSubject = subject.seqSubjects[seq-1];
					if(seqSubject.getStudentCount()==0){
						continue;
					}
					Collection<Map.Entry<SubjectCombination,Integer>> entrys = new ArrayList(seqSubject.fromSubjectCombinationMap.entrySet());
					int classCount = Math.round((float)seqSubject.getStudentCount()/subject.avgTclassStudentCount);
					//int classCount = subject.seqTclassCount[seq-1];
					if(classCount==0&&seqSubject.getStudentCount()>0){
						classCount = 1;
					}		
					int avgTclassStudentCount = seqSubject.getStudentCount() / classCount;
					Integer[] tclassStudentCounts = CommonUtils.getRandomData(seqSubject.getStudentCount(),classCount,classCount);
					System.out.println("-------班级划分人数--isOPT:"+subject.isOpt+"  seq:"+seq+" subjectId:"+subject.subjectId+" classCount:"+classCount+" studentCount:"+ seqSubject.getStudentCount());
					for(int i=0;i<classCount;i++){
						Iterator<Map.Entry<SubjectCombination,Integer>> it = entrys.iterator();
						int remainStudentCount = tclassStudentCounts[i];
						Tclass tclass = new Tclass(subject, tclassStudentCounts[i],seq);
						tclassList.add(tclass);
						while(it.hasNext()&&remainStudentCount>0){
							Map.Entry<SubjectCombination,Integer> entry = it.next();
							int addStudentCount = Math.min(entry.getValue(), remainStudentCount);
							tclass.fromSubjectCombinationsMap.put(entry.getKey().getSubjectIdsStr(),addStudentCount);
							remainStudentCount-=addStudentCount;
							if(entry.getValue()-addStudentCount==0){
								it.remove();
							}else{
								entry.setValue(entry.getValue()-addStudentCount);
							}
						}
					}
				}
			}
		}
		//保存各个科目

		//加入学选交叉结果
		for(Conflict conflict:conflicts){
			conflictList.add(conflict.getConflictBean());
		}
		
		return result;
	}
	
	

	/**
	 * 将学生安排到各个教学班中
	 * @param result
	 * @param studentsMap <志愿名，学生ID>
	 * @param allSubjectIds
	 * @param classIds
	 * @param config
	 * @return
	 */
	public static PlacementResult arrangeStudent(PlacementResult result,
			Map<String,List<Student>> studentsMap,
			List<Integer> allSubjectIds,
			Collection<String> classIds,
			PlacementTaskConfig config
			){
		//计算子组合
		List<TclassEqualSubjectCombination> tclassEqualSubjectCombinations = new ArrayList();
		Map<Student,List<Tclass>> stuIdTclassIdsMap = new HashMap();
		Map<String,List<Student>> optStudentsMap  = new HashMap(studentsMap);
		Map<String,List<Student>> proStudentsMap  = new HashMap(studentsMap);
		for(int isOpt:new Integer[]{CD_OPT,CD_PRO}){
			Map<String,List<Student>> currentStudentsMap = isOpt == CD_OPT?optStudentsMap:proStudentsMap;
			//System.out.println("-----------------currentStudentsMap------------isOpt:"+isOpt);
			for(Map.Entry<String,List<Student>> one:currentStudentsMap.entrySet()){
				System.out.println(one.getKey()+"  studentCount:"+one.getValue().size());
			}
			//System.out.println("-----------");
			for(Map.Entry<String, List<Student>> entry:currentStudentsMap.entrySet()){
				String subjectIdsStr = entry.getKey();
				List<Integer> optSubjectIds =new ArrayList();
				for(String str:subjectIdsStr.split("-")[0].split(",")){
					optSubjectIds.add(Integer.parseInt(str));
				}
				List<Integer> proSubjectIds = new ArrayList(allSubjectIds);
				proSubjectIds.removeAll(optSubjectIds);
				List<Integer>[] subjectIdsCds = new List[]{proSubjectIds,optSubjectIds};
			//System.out.println("----------------"+entry.getKey());
				//第一次进来	
				List<Student> students = entry.getValue();
				if(isOpt==CD_OPT){
					for(Student student:students){
						stuIdTclassIdsMap.put(student, new ArrayList());
					}
				}
		
				Map<Integer,List<Student>> subjectStudentIdsMap = new HashMap();
			
				
				for(int seq=1;seq<=config.seqCounts[isOpt];seq++){
				//	System.out.println("-----"+subjectCombination.subjectIdsStr+"----seq:"+seq+"-------");
					//查询是否要分子志愿组合，仅选考的时候进行
					Map<String,List<Student>> seqCombStudentMap = null;
					if(isOpt==CD_OPT){
						seqCombStudentMap = new HashMap();
						for(Integer subjectId:subjectIdsCds[isOpt]){
							String subCombStr = subjectIdsStr+"-"+subjectId+"-"+(seq-1);
							if(result.subSubjectCominationStrs.contains(subCombStr)){
								seqCombStudentMap.put(subCombStr, null);
							}
						}
					}
					Set<List<Integer>> subjectIdsOrders = CommonUtils.getPermutation(subjectIdsCds[isOpt]);
					boolean isOk = true;
					Map<Tclass,List<Student>> tclassStudentIdsMap = null;
					Map<Integer,List<Student>> currentSubjectStudentIdsMap = null;
					for(List<Integer> subjectIds:subjectIdsOrders){
						Map<String,List<Student>> tempSeqCombStudentMap = null;
						if(isOpt==CD_OPT && seqCombStudentMap!=null && seqCombStudentMap.size()!=0) {
							tempSeqCombStudentMap = new HashMap(seqCombStudentMap);
						}
						List<Student> sortStudents = new ArrayList(students);
						int[] gene = new int[]{1,3,5,7,11};
						for(Student student:sortStudents){
							student.order = 0;
							for(int i=subjectIds.size()-1;i>=0;i--){
								List<Student> subjectStudents = subjectStudentIdsMap.get(subjectIds.get(i));
								if(subjectStudents==null||subjectStudents.size()==0){
									continue;
								}else if(subjectStudents.contains(student)){
									student.order+=gene[i];
								}
							}
						}
						//学生排序，后面科目已排的学生的优先
						Collections.sort(sortStudents,new Comparator<Student>(){
							@Override
							public int compare(Student o1, Student o2) {
								// TODO Auto-generated method stub
								return o2.order.compareTo(o1.order);
							}
						});
						tclassStudentIdsMap = new HashMap();
						isOk = true;
						currentSubjectStudentIdsMap = new HashMap();
						List<Student> seqStudentIds = new ArrayList();
						
						for(Map.Entry<Integer,List<Student>> subjectStudentIds:subjectStudentIdsMap.entrySet()){
							currentSubjectStudentIdsMap.put(subjectStudentIds.getKey(), new ArrayList(subjectStudentIds.getValue()));
						}
						
						for(Integer subjectId:subjectIds){
							List<Tclass> tclassList = new ArrayList();
							for(Tclass one:result.tclassList){
						//		System.out.println("tclass.seq:"+one.seq+"   tclass.subjectId:"+one.subjectId+"  tclass.isOpt:"+isOpt+"  tclass.fromSubjectCombinationsMap:"+JSONObject.toJSONString(one.fromSubjectCombinationsMap));
						//		System.out.println("this.seq:"+seq+"  this.subjectId:"+subjectId+"  this.")
								if(one.seq==seq&&one.subjectId==subjectId&&one.isOpt==isOpt){
									if(one.fromSubjectCombinationsMap.get(subjectIdsStr)!=null){
										tclassList.add(one);
									}
								}
							}
							if(tclassList.size()==0){
								continue;
							}
							String currentSubCombStr = subjectIdsStr+"-"+subjectId+"-"+(seq-1);
						//	System.out.println("----当前志愿组合名"+currentSubCombStr+"  tempSeqCombStudentMap:"+JSONObject.toJSONString(tempSeqCombStudentMap));
							//子集合List
							List<Student> subSubCombStudentList = null;
							if(isOpt==CD_OPT){
								if(tempSeqCombStudentMap!=null && tempSeqCombStudentMap.containsKey(currentSubCombStr)){
									subSubCombStudentList = new ArrayList();
									tempSeqCombStudentMap.put(currentSubCombStr, subSubCombStudentList);
								}
							}
							List<Student> subjectStudentIds = currentSubjectStudentIdsMap.get(subjectId);
							if(subjectStudentIds==null){
								subjectStudentIds = new ArrayList();
								currentSubjectStudentIdsMap.put(subjectId, subjectStudentIds);
							}
							for(Tclass one:tclassList){
								Integer studentCount = one.fromSubjectCombinationsMap.get(subjectIdsStr);
								List<Student> canArrangeStudentIds = new ArrayList(sortStudents);
								canArrangeStudentIds.removeAll(seqStudentIds);
								canArrangeStudentIds.removeAll(subjectStudentIds);
						//		System.out.println("subjectId:"+subjectId+"   studentCount："+studentCount+"   seqStudentIds:"+seqStudentIds.size()+" subjectStudentIds:"+subjectStudentIds.size()+"  canArrangeStudentIds:"+canArrangeStudentIds.size());
								if(canArrangeStudentIds.size()<studentCount){
									isOk = false;
									break;
								}
								List<Student> tclassStudentIds = new ArrayList(canArrangeStudentIds.subList(0, studentCount));
								tclassStudentIdsMap.put(one, tclassStudentIds);
								seqStudentIds.addAll(tclassStudentIds);
								subjectStudentIds.addAll(tclassStudentIds);
								if(subSubCombStudentList!=null){
									subSubCombStudentList.addAll(tclassStudentIds);
								}
							}
						}
						if(isOk){
							if(isOpt== CD_OPT && tempSeqCombStudentMap!=null  && tempSeqCombStudentMap.size()!=0){
								proStudentsMap.remove(subjectIdsStr);
								Iterator<Map.Entry<String,List<Student>>> it = tempSeqCombStudentMap.entrySet().iterator();
								while(it.hasNext()){
									Map.Entry<String,List<Student>> one = it.next();
									if(one.getValue() == null){
										it.remove();
									}
								}
								proStudentsMap.putAll(tempSeqCombStudentMap);
							}
							break;
						}
					}
					if(isOk){
						subjectStudentIdsMap = currentSubjectStudentIdsMap;
						for(Entry<Tclass, List<Student>> tclassStudentIds: tclassStudentIdsMap.entrySet()){
							//System.out.println("entry.key:"+entry.getKey()+" entry.value:"+entry.getValue());
							if(tclassStudentIds.getKey().students==null){
								tclassStudentIds.getKey().students = new ArrayList();
							}
							tclassStudentIds.getKey().students.addAll(tclassStudentIds.getValue());
							for(Student student:tclassStudentIds.getValue()){
								List<Tclass> stuTclassList = stuIdTclassIdsMap.get(student);
								stuTclassList.add(tclassStudentIds.getKey());
							}
						}
					}else{
							System.out.println("-------------------计算出错----------");
						
					}
				}
			}
		}
			
		//将行政班和教学班都一样的学生进行分组
		for(Map.Entry<Student,List<Tclass>> stuIdTclassIds:stuIdTclassIdsMap.entrySet()){
			if(stuIdTclassIds.getValue().size()>6){
				System.out.println("安排出错---"+stuIdTclassIds.getKey().accountId+"   班级数："+stuIdTclassIds.getValue().size());
			}
			TclassEqualSubjectCombination sc = new TclassEqualSubjectCombination(stuIdTclassIds.getKey().classId,stuIdTclassIds.getKey().subjectIds,stuIdTclassIds.getValue());
			if(tclassEqualSubjectCombinations.contains(sc)){
				sc = tclassEqualSubjectCombinations.get(tclassEqualSubjectCombinations.indexOf(sc));
			}else{
				tclassEqualSubjectCombinations.add(sc);
			}
			sc.students.add(stuIdTclassIds.getKey());
		}
		
		//分配场地
		List<String> allExtGroundIds = new ArrayList<String>();
		List<Tclass> needArrangeTclasses = new ArrayList(result.tclassList);
		List<Tclass> hadArrangeTclasses = new ArrayList();
		//有学选交叉冲突的科目
		Set<Integer>[] conflictSubjects = new Set[]{new HashSet(),new HashSet()};
		//没有学选交叉冲突的科目
		Set<Integer>[] unConflictSubjects = new Set[]{new HashSet(),new HashSet()};
		for(Conflict conflict:result.conflictList){
			conflictSubjects[CD_PRO].addAll(conflict.subjectIds[CD_PRO]);
			conflictSubjects[CD_OPT].addAll(conflict.subjectIds[CD_OPT]);
		}
		unConflictSubjects[CD_OPT].addAll(allSubjectIds);
		unConflictSubjects[CD_OPT].removeAll(conflictSubjects[CD_OPT]);
		unConflictSubjects[CD_PRO].addAll(allSubjectIds);
		unConflictSubjects[CD_PRO].removeAll(conflictSubjects[CD_PRO]);

		//各序列已经安排的教室
		List<String>[][] cdSeqArrangedGroundIds = new List[2][config.seqCounts[CD_PRO]];
		for(Set<Integer>[] cdSubjectIds:new Set[][]{conflictSubjects,unConflictSubjects}){
			for(int isOpt:new Integer[]{CD_PRO,CD_OPT}){
				for(int seq=1;seq<=config.seqCounts[isOpt];seq++){
					Set<Integer> subjectIds = cdSubjectIds[isOpt];
					List<String> seqGroundIds = cdSeqArrangedGroundIds[isOpt][seq-1];
					if(seqGroundIds==null){
						seqGroundIds =	new ArrayList();
						cdSeqArrangedGroundIds[isOpt][seq-1]=seqGroundIds;
					}
					List<String> seqCanArrangeGroundIds = new ArrayList(classIds);
					seqCanArrangeGroundIds.addAll(allExtGroundIds);
					seqCanArrangeGroundIds.removeAll(seqGroundIds);
					Iterator<Tclass> iterator = needArrangeTclasses.iterator();
					while(iterator.hasNext()){
						Tclass tclass = iterator.next();
						if(!subjectIds.contains(tclass.subjectId)||tclass.seq!=seq || tclass.isOpt != isOpt){
							continue;
						}
						//行政班号和人数Map
						Map<String,Integer> classIdStudentCountMap = new HashMap();
						//统计教学班中，各行政班的人数
						for(Student student:tclass.students){
							Integer studentCount = classIdStudentCountMap.get(student.classId);
							if(studentCount==null){
								studentCount = 0;
							}
							studentCount += 1;
							classIdStudentCountMap.put(student.classId,studentCount);
						}
						//按照人数从大到小排序
						List<Map.Entry<String,Integer>> entrys = new ArrayList(classIdStudentCountMap.entrySet());
						Collections.sort(entrys, new Comparator<Map.Entry<String,Integer>>(){
							@Override
							public int compare(Entry<String, Integer> arg0,
									Entry<String, Integer> arg1) {
								// TODO Auto-generated method stub
								return arg1.getValue().compareTo(arg0.getValue());
							}
						});
						//优先考虑的行政班场地
						List<String> sortedClassIds = new ArrayList();
						for(Entry<String,Integer> entry:entrys){
							sortedClassIds.add(entry.getKey());
						}
						sortedClassIds.removeAll(seqGroundIds);
						
						List<String> canArrangeNormalGroundIds = new ArrayList(seqCanArrangeGroundIds);
						canArrangeNormalGroundIds.removeAll(sortedClassIds);
						List<String> canArrangeGroundIds = new ArrayList();
						canArrangeGroundIds.addAll(sortedClassIds);
						canArrangeGroundIds.addAll(canArrangeNormalGroundIds);
						System.out.println("isOpt:"+isOpt+"  seq:"+seq+" canArrangeGroundIds:"+JSONObject.toJSONString(canArrangeGroundIds));
						//分配场地
						boolean isOk = false;
						String okGroundId = null;
						for(String groundId:canArrangeGroundIds){
							isOk = true;
							okGroundId = groundId;
							//检查学选交叉冲突
							for(Conflict conflict:result.conflictList){
								if(!conflict.subjectIds[isOpt].contains(tclass.subjectId)){
									continue;
								}
								int seqIndex = conflict.seqs[isOpt].indexOf(tclass.seq);
								int otherIsOpt = isOpt==CD_OPT?CD_PRO:CD_OPT;
								int otherSeq = conflict.seqs[otherIsOpt].get(seqIndex);
								for(Tclass one:hadArrangeTclasses){
									if(one.isOpt == otherIsOpt
											&& one.seq == otherSeq
											&& conflict.subjectIds[otherIsOpt].contains(one.subjectId)
											&& groundId.equals(one.groundId)){
										isOk = false;
										System.out.println("------tclassId:"+one.tclassId+"isOpt:"+one.isOpt+"  seq:"+one.seq+ "subjectId:"+one.subjectId+" groundId:"+one.groundId+"冲突");
										break;
									}
								}
								if(!isOk){
									break;
								}
							}
							if(isOk){
								break;
							}
						}
						//全部不行的情况下，添加辅助教室
						if(!isOk){
							System.out.println("isOpt:"+isOpt+"  seq:"+seq+ "subjectId:"+tclass.subjectId+" tclassId:"+tclass.tclassId+"所有场地冲突");
							okGroundId = UUID.randomUUID().toString();
							allExtGroundIds.add(okGroundId);
						}
						tclass.groundId = okGroundId;
						seqCanArrangeGroundIds.remove(okGroundId);
						seqGroundIds.add(okGroundId);
						hadArrangeTclasses.add(tclass);
						iterator.remove();
					}
				}
			}
		}
		result.allExtGroundIds = allExtGroundIds;
		result.tclassEqualSubjectCombinations = tclassEqualSubjectCombinations;
		System.out.println("-------tclassEqualSubjectCombinations-----"+tclassEqualSubjectCombinations.size());
		return result;
	}
	
	/**
	 * 统计计算各科目每个序列班级数量
	 */
	private static Map<Integer, Integer[]> statsSeqTclassCount(List<Subject> subjectList,PlacementTaskConfig config){
		Map<Integer,Integer[]> seqClassCountMap = new HashMap();
		int totalTclassCount = 0;
		int isOpt = subjectList.get(0).isOpt;
		List<Integer> seqAllTclassCount = new ArrayList();
		for(int i=0;i<config.seqCounts[isOpt];i++){
			seqAllTclassCount.add(0);
		}
		Map<Subject,Integer[]> subjectSeqTclassCountMap = new HashMap();
		//计算选考学考开班数
		for(Subject oneSubject:subjectList){
			oneSubject.statsTclassCount(config);
			totalTclassCount+=oneSubject.tclassCount;
		}
		Collections.sort(subjectList,new Comparator<Subject>(){
			@Override
			public int compare(Subject o1, Subject o2) {
				// TODO Auto-generated method stub
				return o2.avgTclassStudentCount.compareTo(o1.avgTclassStudentCount);
			}
			
		});
		
//		if(totalTclassCount%config.seqCounts[isOpt]==2){
//			Subject oneSubject = subjectList.get(0);
//			oneSubject.tclassCount +=1;
//			oneSubject.avgTclassStudentCount = oneSubject.studentCount / oneSubject.tclassCount;
//		}else if(totalTclassCount%config.seqCounts[isOpt]==1){
//			Subject oneSubject = subjectList.get(subjectList.size()-1);
//			int tclassCount = oneSubject.tclassCount - 1;
//			int avgTclassStudentCount = oneSubject.studentCount / tclassCount;
//			if(Math.ceil((float)oneSubject.studentCount/tclassCount) < config.maxTclassStudentCount){
//				 oneSubject.tclassCount = tclassCount;
//				 oneSubject.avgTclassStudentCount = avgTclassStudentCount;
//			}else{
//				for(int i=0;i<2;i++){
//					Subject twoSubject = subjectList.get(i);
//					twoSubject.tclassCount +=1;
//					twoSubject.avgTclassStudentCount = twoSubject.studentCount / twoSubject.tclassCount;
//				}
//			}
//			
//		}
		
		Collections.shuffle(subjectList);
		for(Subject oneSubject:subjectList){
			int minSeqTclassCount = oneSubject.tclassCount/config.seqCounts[oneSubject.isOpt];
			int surplusTclassCount  = oneSubject.tclassCount%config.seqCounts[oneSubject.isOpt];
			oneSubject.seqTclassCount = new Integer[config.seqCounts[oneSubject.isOpt]];
			List<Integer> seqList = new ArrayList();
			for(int i=1;i<=config.seqCounts[oneSubject.isOpt];i++){
				seqList.add(i);
			}
			//打乱顺序
			Collections.shuffle(seqList);
			for(int seq:seqList){
				if(surplusTclassCount>0){
					oneSubject.seqTclassCount[seq-1] = minSeqTclassCount+1;
					surplusTclassCount -=1;
				}else{
					oneSubject.seqTclassCount[seq-1] = minSeqTclassCount;
				}	
			}
			
			List<Integer> seqTclassCountCopy = new ArrayList<Integer>(seqAllTclassCount);
			for(int i=0;i<seqTclassCountCopy.size();i++){
				seqTclassCountCopy.set(i,seqTclassCountCopy.get(i)+oneSubject.seqTclassCount[i]);
			}
			int maxAllSeqTclassCount = Collections.max(seqTclassCountCopy);
			int minAllSeqTclassCount= Collections.min(seqTclassCountCopy);
			//如果序列之间最大开班数小于2
			if(maxAllSeqTclassCount-minAllSeqTclassCount<2){
				seqAllTclassCount = seqTclassCountCopy;
			}else{
			//如果序列之间最大开班数大于2，则把当前科目对应序列的开班数对换
				int maxSeq = seqTclassCountCopy.indexOf(maxAllSeqTclassCount);
				int minSeq = seqTclassCountCopy.indexOf(minAllSeqTclassCount);
				int tmpSubjectSeqTclassCount = oneSubject.seqTclassCount[maxSeq];
				oneSubject.seqTclassCount[maxSeq] = oneSubject.seqTclassCount[minSeq];
				oneSubject.seqTclassCount[minSeq] = tmpSubjectSeqTclassCount;
				for(int i=0;i<seqAllTclassCount.size();i++){
					seqAllTclassCount.set(i,seqAllTclassCount.get(i)+oneSubject.seqTclassCount[i]);
				}
			}
		}
		for(Subject oneSubject:subjectList){
			seqClassCountMap.put(oneSubject.subjectId,oneSubject.seqTclassCount);
			oneSubject.seqTclassCount = null;
			oneSubject.tclassCount = null;
			oneSubject.avgTclassStudentCount = null;
		}
		
		return seqClassCountMap;
	}
	/**
	 * 检查人数是否排满
	 * @param subjects
	 * @param subjectCombinations
	 * @param subjectMap
	 * @return
	 */
	public static boolean checkResult(List<Subject> subjects,
			List<SubjectCombination> subjectCombinations,
			Map<Integer,Subject> subjectMap,
			PlacementTaskConfig config){
		int isOpt = subjects.get(0).isOpt;
		int[] seqStudentCounts = new int[config.seqCounts[isOpt]];
		for(Subject subject :subjects){
		//	System.out.print(subject.subjectId+" : ");
			int subjectStudentCount = 0;
			for(int seq=1;seq<=config.seqCounts[isOpt];seq++){
				seqStudentCounts[seq-1] += subject.seqSubjects[seq-1].getStudentCount();
			}
		}
		for(int i=0;i<config.seqCounts[isOpt];i++){
			for(int j=0;j<config.seqCounts[isOpt];j++){
				if(i==j){
					continue;
				}
				if(seqStudentCounts[i] != seqStudentCounts[j]){
					return false;
				}
			}
			
		}
		return true;
	}

	public static boolean showResult(List<Subject> subjects,List<SubjectCombination> subjectCombinations,Map<Integer,Subject> subjectMap,PlacementTaskConfig config,List<Conflict> conflictList){
		int isOpt = 0;
		int[][] seqStudentCounts = new int[2][config.seqCounts[isOpt]];
		for(Subject subject :subjects){
			isOpt = subject.isOpt;
			System.out.print(subject.subjectId+" : ");
			int subjectStudentCount = 0;
			for(int seq=1;seq<=config.seqCounts[isOpt];seq++){
				System.out.print("["+seq+":"+subject.seqSubjects[seq-1].getStudentCount()+" ] ");
				seqStudentCounts[subject.isOpt][seq-1] += subject.seqSubjects[seq-1].getStudentCount();
				subjectStudentCount += subject.seqSubjects[seq-1].getStudentCount(); 
			}
			System.out.print("  "+subject.studentCount+ "  "+subjectStudentCount+ " avg:"+subject.avgTclassStudentCount+" classCount:"+JSONObject.toJSONString(subject.seqTclassCount));
			System.out.println("");
			for(int seq=1;seq<=config.seqCounts[isOpt];seq++){
			    System.out.print("  seq:"+seq);
			    for(Map.Entry<SubjectCombination, Integer> entry :subject.seqSubjects[seq-1].fromSubjectCombinationMap.entrySet()){
			    	System.out.print("("+entry.getKey().getSubjectIdsStr()+":"+entry.getValue()+")  ");
			    }
			    System.out.println("");
			}
		}
		System.out.println("--------------------------------");
		for(SubjectCombination subjectCombination:subjectCombinations){
			System.out.print("    "+subjectCombination.getSubjectIdsStr()+"[");
			boolean isOk = true;
			for(int seq = 1;seq<=config.seqCounts[isOpt];seq++ ){
				if(subjects.get(0).isOpt==CD_PRO&&subjectCombination.getConfictType() == ConflictType.TYPE_1){
					int remainSeqStudent = subjectCombination.getTotalStudentCount();
					for(Integer subjectId:subjectCombination.subjectIds[CD_PRO]){
						for(Entry<SubjectCombination,Integer> entry:subjectMap.get(subjectId).seqSubjects[seq-1].fromSubjectCombinationMap.entrySet()){
							if(entry.getKey() instanceof SubSubjectCombination){
								if(((SubSubjectCombination)entry.getKey()).parent==subjectCombination){
									remainSeqStudent -=entry.getValue();
								}
							}
						}
					}
					System.out.print(seq+":"+remainSeqStudent+", ");
					if(remainSeqStudent!=0){
						isOk = false;
					}
				}else{
					System.out.print(seq+":"+subjectCombination.remainSeqStudentCounts[isOpt][seq-1]+", ");

				}
			}
			System.out.print("   (");
			for(int seq = 1;seq<=config.seqCounts[isOpt];seq++ ){
				System.out.print(seq+"|");
				for(Integer subjectId :subjectCombination.subjectIds[isOpt]){
					Integer studentCount = subjectMap.get(subjectId).seqSubjects[seq-1].fromSubjectCombinationMap.get(subjectCombination);
					if(studentCount==null){
						studentCount = 0;
						for(Map.Entry<SubjectCombination, Integer> entry:subjectMap.get(subjectId).seqSubjects[seq-1].fromSubjectCombinationMap.entrySet()){
							if(entry.getKey() instanceof SubSubjectCombination){
								SubSubjectCombination subSubjectCombination = (SubSubjectCombination) entry.getKey();
								if(subSubjectCombination.parent == subjectCombination){
									studentCount += entry.getValue();
								}
							}
						}
					}
					System.out.print(subjectId+": "+studentCount+"  ");
				}
			}
			System.out.print("   )");
			System.out.print("  total:  "+subjectCombination.getTotalStudentCount()+"  conflictType:"+subjectCombination.getConfictType());
			System.out.println("]");
		}
		
		System.out.println("--------------------------------");
		int num = seqStudentCounts[isOpt][0];
		for(int seq=1 ;seq<=seqStudentCounts[isOpt].length;seq++){
			System.out.print(seq+":"+seqStudentCounts[isOpt][seq-1]+", ");
			if(num != seqStudentCounts[isOpt][seq-1]){
				System.out.print("   结果不合格     ");
			}
		}
		System.out.println("");
		System.out.println("--------------------------------");
		System.out.println("------------------打印学选交叉--------------------");
		for(Conflict conflict:conflictList){
			System.out.println("-------------------");
			System.out.println("opt:"+JSONObject.toJSONString(conflict.subjectIds[1])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[1]));
			System.out.println("pro:"+JSONObject.toJSONString(conflict.subjectIds[0])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[0]));
			System.out.println("-------------------");
		}
		return false;
	}
	
	
	
	public static boolean printResult(PlacementResult result){
		System.out.println("score:"+JSONObject.toJSONString(result.score));
		int totalStudent   = 0;
		Collections.sort(result.tclassList, new Comparator<Tclass>(){
			@Override
			public int compare(Tclass o1, Tclass o2) {
				// TODO Auto-generated method stub
				return o2.studentCount.compareTo(o1.studentCount);
			}
		} );
		
		for(Tclass tclass:result.tclassList){
			totalStudent +=tclass.studentCount;
			System.out.println("subjectId:"+tclass.subjectId +"  studentCount:"+tclass.studentCount+" seq:"+tclass.seq+" "+(tclass.isOpt==1?"选考":"学考"));
		}
		System.out.println("共"+result.tclassList.size()+"个班,平均每班"+totalStudent/result.tclassList.size()+"人");
		System.out.println("------------------打印学选交叉--------------------");
		for(Conflict conflict:result.conflictList){
			System.out.println("-------------------");
			System.out.println("opt:"+JSONObject.toJSONString(conflict.subjectIds[1])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[1]));
			System.out.println("pro:"+JSONObject.toJSONString(conflict.subjectIds[0])+"  seqs:"+JSONObject.toJSONString(conflict.seqs[0]));
			System.out.println("-------------------");
		}
		return false;
	
	
	}
	
	public static boolean checkConflict(Integer subjectId, Integer seq,SubjectCombination oneSubjectCombination,List<Conflict> conflicts,int isOpt,Map<Integer,Subject>[] subjectIdsMap){
		boolean result = true;
	//	System.out.println(" ---检查冲突 ---subjectId:"+subjectId+"  seq:"+seq+"   ("+oneSubjectCombination.subjectIdsStr+")  isOpt:"+isOpt);
		if(isOpt==CD_PRO&&oneSubjectCombination instanceof SubSubjectCombination){
			SubSubjectCombination subSc = (SubSubjectCombination) oneSubjectCombination;
			ConflictType conflictType = subSc.getConfictType();
			if(conflictType != ConflictType.TYPE_1){
				result = false;
			}
			//检查是否为学选交叉科目
			for(Conflict conflict:conflicts){
				if(conflict.subjectIds[CD_PRO].contains(subjectId)){
					List<Integer> conflictOptSubjectIds = new ArrayList(conflict.subjectIds[CD_OPT]);
					conflictOptSubjectIds.retainAll(oneSubjectCombination.subjectIds[CD_OPT]);
					//如果是
					if(conflict.seqs[CD_PRO].indexOf(seq)==-1){
						continue;
					}
					int optSeq =conflict.seqs[CD_OPT].get(conflict.seqs[CD_PRO].indexOf(seq));
					for(int optSubjectId : conflictOptSubjectIds){
					//	System.out.println("------pro:("+(subSc.seq+1)+","+subSc.subjectId+")    opt:( "+seq+" ,"+optSubjectId+")");
						if((subSc.seqIndex+1)==optSeq&&subSc.subjectId==optSubjectId){
							result = false;
							break;
						}else if((subSc.seqIndex+1)!=optSeq&&subSc.subjectId!=optSubjectId){
							Integer optStuCount = subjectIdsMap[CD_PRO].get(optSubjectId).seqSubjects[optSeq-1].fromSubjectCombinationMap.get(subSc.parent);
							if(optStuCount != null && optStuCount != 0){
								result = false;
								break;
							}
						}
					}
					if(!result){
						break;
					}
				}
			}
			
		}

		return result;
	}
	
	
	public static void main3(String[] args){
	//	List<Integer> datas = new ArrayList(Arrays.asList(new Integer[]{1,1,0}));
	//	System.out.println(JSONObject.toJSONString(getPermutation(datas)));
		List<Integer> datas  = new ArrayList(3);
		datas.add(3);
		System.out.println(datas.indexOf(3));
//		datas.set(2,1);
//		System.out.println(JSONObject.toJSONString(datas));
//		Map<String,Integer> testMap1 = new HashMap();
//		testMap1.put("name1", 1);
//		testMap1.put("name2", 2);
//		testMap1.put("name3", 3);
//		Map<String,Integer> testMap2 = new HashMap(testMap1);
//		for(Map.Entry<String,Integer> entry :testMap2.entrySet()){
//			entry.setValue(6);
//		}
//		System.out.println(JSONObject.toJSONString(testMap1));
//		System.out.println(JSONObject.toJSONString(testMap2));
		//BasicConfigurator.configure();
		//logger.debug("13423");
	}
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
		subjectIdMap.put(19, "技");
//		Map<Integer,Integer> optSubjectLessonsMap = new HashMap();
//		optSubjectLessonsMap.put(4, 4);
//		optSubjectLessonsMap.put(5, 4);
//		optSubjectLessonsMap.put(6, 4);
//		optSubjectLessonsMap.put(7, 4);
//		optSubjectLessonsMap.put(8, 4);
//		optSubjectLessonsMap.put(9, 4);
//		optSubjectLessonsMap.put(19, 4);
//		
//		Map<Integer,Integer> proSubjectLessonsMap = new HashMap();
//		proSubjectLessonsMap.put(4, 2);
//		proSubjectLessonsMap.put(5, 2);
//		proSubjectLessonsMap.put(6, 2);
//		proSubjectLessonsMap.put(7, 2);
//		proSubjectLessonsMap.put(8, 2);
//		proSubjectLessonsMap.put(9, 2);
//		proSubjectLessonsMap.put(19,2);
//		
		Map<Integer,Integer> optSubjectLessonsMap = new HashMap();
		optSubjectLessonsMap.put(4, 3);
		optSubjectLessonsMap.put(5, 5);
		optSubjectLessonsMap.put(6, 3);
		optSubjectLessonsMap.put(7, 4);
		optSubjectLessonsMap.put(8, 5);
		optSubjectLessonsMap.put(9, 3);
		
		Map<Integer,Integer> proSubjectLessonsMap = new HashMap();
		proSubjectLessonsMap.put(4, 1);
		proSubjectLessonsMap.put(5, 2);
		proSubjectLessonsMap.put(6, 2);
		proSubjectLessonsMap.put(7, 2);
		proSubjectLessonsMap.put(8, 1);
		proSubjectLessonsMap.put(9, 1);
		
		Object[][] orginSC12345=  {{"地化生",28},
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
				{"史物生",17},
				{"物化生",76},
				{"政地化",14},
				{"政地生",32},
				{"政地物",14},
				{"政化生",16},
				{"政史地",49},
				{"政史化",27},
				{"政史生",27},
				{"政史物",9},
				{"政物化",13},
				{"政物生",12}};
				
	 			
	Object[][] orginSC111=  {{"地化生",0},
				{"地物化",0},
				{"地物生",32},
				{"史地化",32},
				{"史地生",34},
				{"史地物",32},
				{"史化生",0},
				{"史物化",113},
				{"史物生",0},
				{"物化生",139},
				{"政地化",0},
				{"政地生",0},
				{"政地物",0},
				{"政化生",0},
				{"政史地",111},
				{"政史化",0},
				{"政史生",0},
				{"政史物",32},
				{"政物化",0},
				{"政物生",3}};
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

		Map<String,List<Student>> studentsMap = new HashMap();
		List<String> classIds  = new ArrayList();
		int classCount = 10;
		for(int  i = 0;i<classCount;i++){
			classIds.add(i+"");
		}
		for(Map.Entry<String,Integer> entry:subjectCombinationsMap.entrySet()){
			int studentCount = entry.getValue();
			Random random = new Random();
			List<Student> studentList = new ArrayList();
			studentsMap.put(entry.getKey(),studentList);
			for(int i=1;i<=studentCount;i++){
				Student student = new Student(entry.getKey()+"-"+i, classIds.get(random.nextInt(classCount)%classCount), "1",entry.getKey());
				studentList.add(student);
			}
		}
		Date start = new Date();
		PlacementResult result = NewDZBPlacementExcuter.executePlacement(subjectCombinationsMap, config);
		Date end = new Date();
		System.out.println("cost time:"+(end.getTime()-start.getTime())/1000+"s");
		printResult(result);
//		PlacementResult result = NewDZBPlacementExcuter.divClass(50, 55, optSubjectLessonsMap, proSubjectLessonsMap, studentsMap, classIds, SelectType.ThreeFromSix);
//		System.out.println(JSONObject.toJSON(result.allExtGroundIds));
//		result.tclassList.sort((a,b)->Integer.compare(b.students.size(), a.students.size()));
//		for(Tclass tclass:result.tclassList){
//			System.out.println("subjectId:"+tclass.subjectId+" seq:"+tclass.seq+" isOpt:"+tclass.isOpt+"  groundId:"+tclass.groundId+"  studentCount:"+tclass.studentCount+"   realStudentCount:"+tclass.students.size()+"  "+JSONObject.toJSONString(tclass.fromSubjectCombinationsMap));
//		}
	}
	
	/**
	 * 分班对外调用接口
	 * @param avgClassStudentCount	平均班额
	 * @param maxClassStudentCount	班级最大人数
	 * @param optSubjectIdLessonMap	选考科目-教学任务map
	 * @param proSubjectIdLessonMap	学考科目-教学任务map
	 * @param subjectCombinationsMap 学生志愿的科目组合
	 * @param classIds 所有行政班ID
	 * @return
	 */
	public static PlacementResult divClass(Integer avgClassStudentCount, 
			Integer maxClassStudentCount, 
			Map<Integer, Integer> optSubjectIdLessonMap,
			Map<Integer, Integer> proSubjectIdLessonMap,
			Map<String,List<Student>> studentsMap,
			Collection<String> classIds,
			SelectType selectType){
	
		//配置分班参数
		PlacementTaskConfig config = new PlacementTaskConfig(avgClassStudentCount,
				maxClassStudentCount,
				optSubjectIdLessonMap,
				proSubjectIdLessonMap, selectType);
		
		Map<String,Integer> subjectCombinationsMap = new HashMap();
		for(Entry<String,List<Student>> entry:studentsMap.entrySet()){
			subjectCombinationsMap.put(entry.getKey(),entry.getValue().size());
		}
		//分班
		PlacementResult result =
				NewDZBPlacementExcuter.executePlacement(subjectCombinationsMap, config);
		NewDZBPlacementExcuter.arrangeStudent(result,
				studentsMap, 
				new ArrayList(optSubjectIdLessonMap.keySet()),
				classIds,
				config
		);
		
		for(Tclass tclass:result.tclassList){
			System.out.println("subjectId:"+tclass.subjectId+" seq:"+tclass.seq+" isOpt:"+tclass.isOpt+"  groundId:"+tclass.groundId+"  studentCount:"+tclass.studentCount+"   realStudentCount:"+tclass.students.size()+"  "+JSONObject.toJSONString(tclass.fromSubjectCombinationsMap));
		}
		return result;
	}
	
	
}


	
