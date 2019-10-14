package com.talkweb.placementtask.utils.newdzb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.talkweb.placementtask.utils.newdzb.NewDZBPlacementExcuter.OptimizeType;
import com.talkweb.placementtask.utils.newdzb.NewDZBPlacementExcuter.PlacementTaskConfig;

/**
 * 科目
 * @author lenovo
 *
 */
final class Subject{
	private static int CD_OPT = NewDZBPlacementExcuter.CD_OPT;
	private static int CD_PRO = NewDZBPlacementExcuter.CD_PRO;
	Integer subjectId;
	/**
	 * 是否是选考
	 * 1.选考 0.学考
	 */
	public Integer isOpt = null;
	/**需要上课的所有科目组合 **/
	List<SubjectCombination> subjectCombinationList = new ArrayList();	
	/**需要上课的所有科目组合 **/
	List<SubjectCombination> subjectCombinationListBak = new ArrayList();	
	Stack<Integer[]> seqTclassCountBak = new Stack() ;
	
	/** 学生人数 **/
	Integer studentCount = 0;
	/** 平均班人数 **/
	Integer avgTclassStudentCount = 0;
	/** 开班数 **/
	Integer tclassCount = 0;

	/** 剩余未排学生数 **/
	Integer remainStudentCount  = 0 ;
	/** 序列班级数 **/
	Integer[] seqTclassCount ;
	/** 序列课程格子Map**/
	SeqSubject[] seqSubjects = null;
	/** 采用 的小志愿组合 **/

	int[] seqMustArrangeStudents = null;
	
	/** 课时数 **/
	Integer lessonCount;
	public Subject(Integer subjectId,Integer isOpt,PlacementTaskConfig config){
		this.seqMustArrangeStudents = new int[config.seqCounts[CD_PRO]];
		this.subjectId = subjectId;
		this.isOpt = isOpt;
		this.lessonCount =isOpt.equals(1)?config.optSubjectIdLessonMap.get(subjectId):config.proSubjectIdLessonMap.get(subjectId);
	}

	/**
	 * 获取班级人数最大偏差值
	 * @return
	 */
	public int getMaxOffset(){
		int offset = 5;
		int seq = 0;
		for(SeqSubject seqSubject:this.seqSubjects){
			offset = (int) Math.max(offset, Math.ceil((float)seqSubject.getStudentCount()/this.seqTclassCount[seq]));
			seq++;
		}
		return offset;
	}
	/**
	 * 获取序列下剩余可排学生数
	 * @param seq
	 */
	public int getRemainSeqStudentCount(int seq,List<Conflict> conflicts,Map<Integer,Subject>[] subjectIdsMap ){
		int count = 0;
		for(SubjectCombination subjectCombination:subjectCombinationList){
			if(this.isOpt==CD_PRO){
				if(NewDZBPlacementExcuter.checkConflict(this.subjectId,seq,subjectCombination,conflicts,isOpt,subjectIdsMap)){
					count += subjectCombination.remainSeqStudentCounts[this.isOpt][seq-1];
				}
			}else{
				count += subjectCombination.remainSeqStudentCounts[this.isOpt][seq-1];
			}
		}
		return count;
	}
	

	/**
	 * 统计科目开班数
	 */
	public void statsTclassCount(PlacementTaskConfig config){
		this.tclassCount = Math.round((float)studentCount/config.avgTclassStudentCount);
		if(Math.ceil((float)studentCount/tclassCount)>config.maxTclassStudentCount){
			this.tclassCount+=1;
		}
		this.avgTclassStudentCount = studentCount/tclassCount;
		this.remainStudentCount = this.studentCount;
	}
	
	
	/**
	 * 备份序列科目的志愿组合
	 * @param subjects
	 */
	public static void backupSubject(Collection<Subject> subjects){
		for(Subject subject:subjects){
			for(SeqSubject seqSubject:subject.seqSubjects){
				seqSubject.backupFromSubjectCombinationMap();
			}
			subject.seqTclassCountBak.push(Arrays.copyOf(subject.seqTclassCount, subject.seqTclassCount.length));
		}
		
	}
	
	/**
	 * 清空上次备份序列科目的志愿组合
	 * @param subjects
	 */
	public static void clearBackupSubject(Collection<Subject> subjects){
		for(Subject subject:subjects){
			for(SeqSubject seqSubject:subject.seqSubjects){
				seqSubject.clearBackUpFromSubjectCombinationMap();
			}
			subject.seqTclassCountBak.pop();
		}
		
	}

	/**
	 * 还原序列科目的志愿组合
	 * @param subjects
	 */
	public static void restoreSubject(Collection<Subject> subjects){
		for(Subject subject:subjects){
			for(SeqSubject seqSubject:subject.seqSubjects){
				seqSubject.restoreFromSubjectCombinationMap();
			}
			subject.seqTclassCount = subject.seqTclassCountBak.pop();
		}
	}
	
	/**
	 * 是否需要优化
	 * @param config
	 * @param optimizeType
	 * @return
	 */
	public boolean isNeedOptimize(PlacementTaskConfig config,OptimizeType optimizeType){
		boolean isOk = false;
		boolean isAvailable = true;
		for(SeqSubject seqSubject:this.seqSubjects){
			int seqSubjectStudentCount = seqSubject.getStudentCount() ;
			int classCount = 0;
			if(optimizeType == OptimizeType.TYPE_CLASSCOUNT){
				classCount = this.seqTclassCount[seqSubject.seq-1];
			}else{
				classCount =Math.round((float)seqSubjectStudentCount/this.avgTclassStudentCount);
			}
			//System.out.println("===subjectId:"+this.subjectId+"  isOpt:"+this.isOpt+"   seqSubjectStuCount:"+seqSubjectStudentCount+"   classCount:"+classCount);
			if( seqSubjectStudentCount < classCount*(this.avgTclassStudentCount-5) || seqSubjectStudentCount >= config.maxTclassStudentCount*classCount){
				isOk = true;
				break;
			}
		}
		return isOk;
	}

	public boolean isAvailable(PlacementTaskConfig config){
		boolean isOk = true;
		for(SeqSubject seqSubject:this.seqSubjects){
			int seqSubjectStudentCount = seqSubject.getStudentCount() ;
			int classCount =Math.round((float)seqSubjectStudentCount/this.avgTclassStudentCount);
			if(seqSubjectStudentCount >= config.maxTclassStudentCount*classCount){
				isOk = false;
				break;
			}
		}
		return isOk;
	}

}


