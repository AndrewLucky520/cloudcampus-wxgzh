package com.talkweb.placementtask.utils.newdzb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.talkweb.placementtask.utils.newdzb.SubjectCombination.ConflictType;

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
			if(sc.getConfictType() != ConflictType.TYPE_0){
				this.orderOptSubjectIds = new ArrayList(sc.getOrderOptSubjectIds());
				this.orderProSubjectIds  = new ArrayList(sc.getOrderProSubjectIds());
				this.conflictSubjectIds =  Arrays.copyOf(sc.getConflictSubjectIds(), 2);
			}
			this.conflictType = sc.getConfictType();
		}
		
		public void setStatus(SubjectCombination sc){
			if(this.conflictType!= ConflictType.TYPE_0){
				sc.setOrderOptSubjectIds(new ArrayList(this.orderOptSubjectIds));
				sc.setOrderProSubjectIds(new ArrayList(this.orderProSubjectIds));
				sc.setConflictSubjectIds(Arrays.copyOf(sc.getConflictSubjectIds(), 2));
			}
			sc.setConfictType(this.conflictType); 
		}
	}
	
}
