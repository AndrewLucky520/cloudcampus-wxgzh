package com.talkweb.placementtask.utils.newdzb2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.talkweb.placementtask.utils.newdzb2.SubjectCombination.ConflictType;

/**
 * 学课课眼学选冲突计算结果
 * @author lenovo
 *
 */
public class ConflictComputeResult {
	public Map<SubjectCombination,SubSubjectCombination[][]> subSubjectCombinationMap;
	public Map<SubjectCombination,SubjectCombinationStatus> subjectCombinationStatusMap;
	public List<Conflict> conflicts;
	public ConflictComputeResult(
			List<Conflict> conflicts,
			Map<SubjectCombination,SubSubjectCombination[][]> subSubjectCombinationMap,
			List<SubjectCombination> subjectCombinations){
		this.conflicts = conflicts;
		this.subSubjectCombinationMap = subSubjectCombinationMap;
		this.subjectCombinationStatusMap = new HashMap();
		for(SubjectCombination sc:subjectCombinations){
			this.subjectCombinationStatusMap.put(sc,new SubjectCombinationStatus(sc));
		}
	}
	public class SubjectCombinationStatus{
		public Set<Integer>[] conflictSubjectIds;
		public List<Integer> orderOptSubjectIds = null;
		public List<Integer> orderProSubjectIds = null;
		public ConflictType conflictType = null;
		public SubjectCombinationStatus(SubjectCombination sc){
			this.orderOptSubjectIds = sc.getOrderOptSubjectIds();
			this.orderProSubjectIds  = sc.getOrderProSubjectIds();
			this.conflictSubjectIds = sc.getConflictSubjectIds();
			this.conflictType = sc.getConfictType();
			if(sc.getConfictType()==null){
				System.out.println(sc.subjectIds);
			}
		}
		
		public void setStatus(SubjectCombination sc){
			sc.setOrderOptSubjectIds(this.orderOptSubjectIds);
			sc.setOrderProSubjectIds(this.orderProSubjectIds);
			sc.setConflictSubjectIds(this.conflictSubjectIds);
			sc.setConfictType(this.conflictType); 
		}
	}
	
}
