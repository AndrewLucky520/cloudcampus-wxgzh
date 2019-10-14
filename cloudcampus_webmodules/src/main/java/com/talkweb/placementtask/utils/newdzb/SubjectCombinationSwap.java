package com.talkweb.placementtask.utils.newdzb;

import java.util.Map;
/**
 * 志愿科目人数上下序列调整
 * @author lenovo
 *
 */
final class SubjectCombinationSwap{
	SubjectCombination subjectCombination;
	Integer swapStudenCount;
	Integer swapSeqs[] = new Integer[2];
	Subject swapSubject[] = new Subject[2];
	Integer order = 0;
	
	Map<Integer,Subject>[] subjectIdMaps ;
	public SubjectCombinationSwap(SubjectCombination subjectCombination){
		this.subjectCombination = subjectCombination;
	}
	public void doSwap(){
		Subject subject1 = swapSubject[0];
		Subject subject2 = swapSubject[1];
//		String outputStr = subjectCombination.subjectIdsStr+": ("
//				+swapSubject[0].subjectId+"|"
//				+swapSeqs[0]+"("+subject1.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.get(subjectCombination)+"人)->" +swapSeqs[1] 
//				+"("+subject1.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.get(subjectCombination)+"人)"
//				+"|"+ swapStudenCount+")  ";
//		outputStr = outputStr + "("+swapSubject[1].subjectId+"|"+swapSeqs[1]
//				+"("+subject2.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.get(subjectCombination)+"人)->"
//				+swapSeqs[0] +"(" +subject2.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.get(subjectCombination)+"人)|"+ swapStudenCount+")  ";
//		System.out.println(outputStr);
	//	 1->2 2->1  
//		System.out.println("变化前： subjectId:"+ subject1.subjectId 
//				+"   seq:"+swapSeqs[0]+"("+subject1.seqSubjects[swapSeqs[0]-1].getStudentCount()+"人) "
//				+"   seq:"+swapSeqs[1]+"("+subject1.seqSubjects[swapSeqs[1]-1].getStudentCount()+"人) ");
//	
//		System.out.println("变化前： subjectId:"+ subject2.subjectId 
//				+"   seq:"+swapSeqs[1]+"("+subject2.seqSubjects[swapSeqs[1]-1].getStudentCount()+"人) "
//				+"   seq:"+swapSeqs[0]+"("+subject2.seqSubjects[swapSeqs[0]-1].getStudentCount()+"人) ");
//		
		// 志愿组合的subject1在第一序列的人数
		int subject1Seq1CombStdCount = subject1.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.get(subjectCombination);
		// 志愿组合的subject1在第二序列的人数
		int subject1Seq2CombStdCount = 0;
		if(subject1.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.get(subjectCombination)!=null){
			subject1Seq2CombStdCount = subject1.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.get(subjectCombination);
		}
		//把subject1人数从第一序列移到第二序列
		if(subject1Seq1CombStdCount-swapStudenCount>0){
			 subject1.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.put(subjectCombination,subject1Seq1CombStdCount-swapStudenCount);
			 subject1.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.put(subjectCombination,subject1Seq2CombStdCount+swapStudenCount);		 
		}else if(subject1Seq1CombStdCount-swapStudenCount==0){
			 subject1.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.remove(subjectCombination);
			 subject1.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.put(subjectCombination,subject1Seq2CombStdCount+swapStudenCount);
		}else{
			//System.out.println("计算错误");
		}
		//把subject2从第二序列移到第一序列
		int subject2Seq2CombStdCount = subject2.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.get(subjectCombination);
		int subject2Seq1CombStdCount = 0;
		//subject2的第一序列人数
		if(subject2.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.get(subjectCombination)!=null){
			subject2Seq1CombStdCount =subject2.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.get(subjectCombination);
		}
	
		
		if(subject2Seq2CombStdCount-swapStudenCount > 0){
			 subject2.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.put(subjectCombination,subject2Seq2CombStdCount-swapStudenCount);
			 subject2.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.put(subjectCombination,subject2Seq1CombStdCount+swapStudenCount);
		}else if(subject2Seq2CombStdCount-swapStudenCount == 0){
			 subject2.seqSubjects[swapSeqs[1]-1].fromSubjectCombinationMap.remove(subjectCombination);
			 subject2.seqSubjects[swapSeqs[0]-1].fromSubjectCombinationMap.put(subjectCombination,subject2Seq1CombStdCount+swapStudenCount);
		}else{
			//System.out.println("计算错误");
		}
//		System.out.println("变化后： subjectId:"+ subject1.subjectId 
//				+"   seq:"+swapSeqs[0]+"("+subject1.seqSubjects[swapSeqs[0]-1].getStudentCount()+"人) "
//				+"   seq:"+swapSeqs[1]+"("+subject1.seqSubjects[swapSeqs[1]-1].getStudentCount()+"人) ");
//		
//		System.out.println("变化后： subjectId:"+ subject2.subjectId 
//				+"   seq:"+swapSeqs[1]+"("+subject2.seqSubjects[swapSeqs[1]-1].getStudentCount()+"人) "
//				+"   seq:"+swapSeqs[0]+"("+subject2.seqSubjects[swapSeqs[0]-1].getStudentCount()+"人) ");
	}
}
