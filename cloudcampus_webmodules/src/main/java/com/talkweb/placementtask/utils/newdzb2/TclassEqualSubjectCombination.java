package com.talkweb.placementtask.utils.newdzb2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TclassEqualSubjectCombination {
	public String subjectIdsStr;
	public String subjectCombinationId = null;
	public String classId ;
	public List<Tclass> tclassList = new ArrayList();
	public List<Student> students = new ArrayList();
	public TclassEqualSubjectCombination(String classId,String subjectIdsStr,List<Tclass> tclassList){
		this.subjectIdsStr = subjectIdsStr.split("-")[0];
		this.tclassList = tclassList;
		this.classId = classId;
		this.subjectCombinationId = UUID.randomUUID().toString();
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof TclassEqualSubjectCombination ){
			TclassEqualSubjectCombination sc = (TclassEqualSubjectCombination) obj;
			if(!this.classId.equals(sc.classId)){
				return false;
			}
			if(Arrays.deepEquals(this.tclassList.toArray(), sc.tclassList.toArray())){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}


}
