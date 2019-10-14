package com.talkweb.placementtask.utils.newdzb2;

import com.talkweb.placementtask.utils.newdzb2.NewDZBPlacementExcuter.OptimizeType;
import com.talkweb.placementtask.utils.newdzb2.NewDZBPlacementExcuter.PlacementTaskConfig;
import com.talkweb.placementtask.utils.newdzb2.NewDZBPlacementExcuter.SelectType;

public class ResultScore implements Comparable<ResultScore>{
	private static int CD_OPT = NewDZBPlacementExcuter.CD_OPT;
	private static int CD_PRO = NewDZBPlacementExcuter.CD_PRO;
	/**
	 * 离散值
	 */
	public int discreteValue = 0;
	/**
	 * 优势格子数
	 */
	public int goodCount = 0;
	/**
	 * 能用格子数
	 */
	public int availableCount = 0;
	/**
	 *各序列分布合格科目数
	 */
	public int availableClassDiscreteCount = 0;
	
	/**
	 * 各序列班级分布系数
	 */
	public int classDiscreteValue = 0;
	/**
	 * 总班级数
	 */
	public int tclassCount = 0;
	/**
	 * 
	 * @param subjects
	 * @param config
	 * @return
	 */
	public static ResultScore scoreResult(PlacementTaskConfig config,OptimizeType optimizeType,Subject... subjects){
		ResultScore score = new ResultScore();
		for(Subject subject:subjects){
			int avgTclassCount =  Math.round((float)subject.studentCount/subject.avgTclassStudentCount/3);
			for(SeqSubject seqSubject:subject.seqSubjects){
				int tclassCount = 0;
				if(optimizeType==OptimizeType.TYPE_CLASSCOUNT){
					tclassCount = subject.seqTclassCount[seqSubject.seq-1];
				}else{
					tclassCount = Math.round((float)seqSubject.getStudentCount()/subject.avgTclassStudentCount);
				}
				score.tclassCount += tclassCount;
				score.classDiscreteValue += Math.pow(tclassCount-avgTclassCount, 2);
				if(tclassCount == 0 ){
					if(seqSubject.getStudentCount() == 0){
						score.goodCount += 1;
						score.availableCount += 1;
					}else{
				//		System.out.println("seqSubject.getStudentCount():"+seqSubject.getStudentCount()+"   subject.avgTclassStudentCount:"+subject.avgTclassStudentCount);
					}
					continue;
				}
				int realAvgTclassStudentCount = (int) Math.ceil((float)seqSubject.getStudentCount()/tclassCount);
				if(realAvgTclassStudentCount  <= config.maxTclassStudentCount){
					score.availableCount += 1;
					if(realAvgTclassStudentCount >  subject.avgTclassStudentCount- 5){
						score.goodCount +=1;
					}
				}else{
			//		System.out.println("seqSubject.getStudentCount():"+seqSubject.getStudentCount()+"   subject.avgTclassStudentCount:"+subject.avgTclassStudentCount);
				}
				score.discreteValue += Math.pow(realAvgTclassStudentCount,2);
			}
			int oneAvailableClassDiscreteCount = 1;
			int canMaxAvailableClassDiscreteCount = (int) Math.ceil((float)subject.tclassCount/config.seqCounts[subject.isOpt]);
			for(int i = 0;i<config.seqCounts[subject.isOpt];i++){
				if(subject.seqTclassCount[i]>canMaxAvailableClassDiscreteCount){
					oneAvailableClassDiscreteCount = 0;
					break;
				}
			}
			score.availableClassDiscreteCount +=oneAvailableClassDiscreteCount;
		}
		return score;
	}

	@Override
	public int compareTo(ResultScore rs) {
		// TODO Auto-generated method stub
		int result = 0;
		
		result =  Integer.compare(this.availableClassDiscreteCount,rs.availableClassDiscreteCount);
		if(result != 0){
			return result;
		}
		
		result =  Integer.compare(this.availableCount, rs.availableCount);
		if(result !=0){
			return result;
		}
		
		result =  Integer.compare(this.goodCount, rs.goodCount);
		if(result !=0 ){
			return result;
		}
		
		result =  Integer.compare(rs.tclassCount,this.tclassCount);
		if(result !=0){
			return result;
		}
		
		result =  Integer.compare(rs.classDiscreteValue,this.classDiscreteValue);
		if(result !=0){
			return result;
		}
		result = Integer.compare(rs.discreteValue,this.discreteValue);
		return result;
	}
	
	/**
	 * 求和
	 * @param rs1
	 * @param rs2
	 * @return
	 */
	public static ResultScore add(ResultScore rs1,ResultScore rs2){
		ResultScore result = new ResultScore();
		result.discreteValue =rs1.discreteValue + rs2.discreteValue;
		result.goodCount = rs1.goodCount + rs2.goodCount;
		result.availableCount =  rs1.availableCount + rs2.availableCount;
		result.classDiscreteValue =rs1.classDiscreteValue + rs2.classDiscreteValue;
		result.availableClassDiscreteCount =rs1.availableClassDiscreteCount + rs2.availableClassDiscreteCount;
		result.tclassCount =rs1.tclassCount + rs2.tclassCount;
		return result;
	}
	
	
	public  boolean isGood(PlacementTaskConfig config){
		boolean result= true;
		if(config.selectType == SelectType.ThreeFromSix){
			if (this.goodCount >=36 && this.availableClassDiscreteCount >=12){
				result = true;
			}else{
				result = false;
			}
		}else if(config.selectType == SelectType.ThreeFromSeven){
			if (this.goodCount >=49 && this.availableClassDiscreteCount >=14){
				result = true;
			}else{
				result = false;
			}
		}
		return result;
	}
	
	
	public  boolean isAvailable(PlacementTaskConfig config,int isOpt){
		boolean result= true;
		if(isOpt==CD_PRO){
			if(config.selectType == SelectType.ThreeFromSix){
				if (this.availableCount >=18){
					result = true;
				}else{
					result = false;
				}
			}else if(config.selectType == SelectType.ThreeFromSeven){
				if (this.availableCount >=28){
					result = true;
				}else{
					result = false;
				}
			}
		}else{
			if(config.selectType == SelectType.ThreeFromSix){
				if (this.availableCount >=18){
					result = true;
				}else{
					result = false;
				}
			}else if(config.selectType == SelectType.ThreeFromSeven){
				if (this.availableCount >=21){
					result = true;
				}else{
					result = false;
				}
			}
		}
		return result;
	}
}
