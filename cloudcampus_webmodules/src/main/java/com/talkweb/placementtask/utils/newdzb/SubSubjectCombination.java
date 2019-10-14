package com.talkweb.placementtask.utils.newdzb;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class  SubSubjectCombination extends SubjectCombination {
	/**
	 * 
	 * @author lenovo
	 *
	 */
	enum SubSubjectCombinationType{
		NORMAL, //一般情况，就是对应的选考一个科目序列
		ALL //整个科目所有人数
	}
	static ThreadLocal<HashMap<String,SubSubjectCombination>> subScMaps = new ThreadLocal<HashMap<String,SubSubjectCombination>>(){
		protected HashMap<String,SubSubjectCombination> initialValue()
        {
            return new HashMap();
        }
	};
	public static SubSubjectCombination getInstances(SubjectCombination parent,Integer subjectIndex,Integer seq,SubSubjectCombinationType subType){
		String key = "";
		if(subType==SubSubjectCombinationType.NORMAL){
			key = parent.getSubjectIdsStr()+"-"+subjectIndex+"-"+seq;
		}else{
			key = parent.getSubjectIdsStr()+"-ALL-ALL";
		}
		SubSubjectCombination instance = subScMaps.get().get(key);
		if(instance==null){
			instance = new SubSubjectCombination(parent,subjectIndex,seq,subType);
			subScMaps.get().put(key, instance);
		}
		return instance;
	}

	private SubSubjectCombination(SubjectCombination parent,Integer subjectIndex,Integer seqIndex,SubSubjectCombinationType subType){
		this.parent = parent;
		this.subjectIndex = subjectIndex;
		this.seqIndex = seqIndex;
		this.subjectIds = parent.subjectIds;
		this.subType = subType;
	}
	
	@Override
	public List<Integer> getOrderOptSubjectIds() {
		// TODO Auto-generated method stub
		return parent.getOrderOptSubjectIds();
	}

	@Override
	public List<Integer> getOrderProSubjectIds() {
		// TODO Auto-generated method stub
		return parent.getOrderProSubjectIds();
	}

	@Override
	public Set<Integer>[] getConflictSubjectIds() {
		// TODO Auto-generated method stub
		return parent.getConflictSubjectIds();
	}

	@Override
	public ConflictType getConfictType() {
		// TODO Auto-generated method stub
		return parent.getConfictType();
	}



	SubjectCombination parent;
	SubSubjectCombinationType subType;
	//所在格子选考序列
	Integer seqIndex;
	Integer subjectIndex;

	//
	Subject subject;
	//需要拍的科目
	List<Integer> proSubjectIds;
	//需要排的序列
	List<Integer> proSeqs;
	
	//获取所在格子选考科目
	public Integer getSubjectId(){
		if(subType == SubSubjectCombinationType.NORMAL){
			return parent.getOrderOptSubjectIds().get(subjectIndex);
		}else{
			return -1;
		}
	}
	
	public String getSubjectIdsStr() {
		String subjectIdsStr = null;
		if(this.subType == SubSubjectCombinationType.NORMAL){
			subjectIdsStr =  parent.getSubjectIdsStr()+"-"+this.getSubjectId()+"-"+this.seqIndex;
		}else{
			subjectIdsStr = parent.getSubjectIdsStr()+"-ALL-ALL";
		}
		return  subjectIdsStr;
	}
	
	
	public String toString(){
		return "("+this.getSubjectId()+","+seqIndex+")";
		
	}

	@Override
	public Integer getTotalStudentCount() {
		// TODO Auto-generated method stub
		Integer result = subject.seqSubjects[seqIndex].fromSubjectCombinationMap.get(parent);
		if(result == null){
			result = 0;
		}
		return result;
	}

	
}
