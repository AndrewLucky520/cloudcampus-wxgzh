package com.talkweb.placementtask.utils.newdzb2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.talkweb.placementtask.utils.newdzb2.NewDZBPlacementExcuter.PlacementTaskConfig;

/**
 * 科目组合
 * @author lenovo
 *
 */
class SubjectCombination {
	/**
	 * 冲突类型
	 * @author lenovo
	 *
	 */
public static enum ConflictType{
	
		//没有冲突序列
		TYPE_0,
		//有同时上两科的冲突序列，并且可以只有一个选科科目
		TYPE_1,
		//有同时上两科的冲突序列并且所有上三科的冲突序列科目数小于三科
		TYPE_2,
		//有要同时上三科的冲突序列并且所有上三科的冲突序列科目数大于三科
		TYPE_3,
		TYPE_4,
		//2，1
		TYPE_5,
		//
		TYPE_6;
		final static String[][] type3Model = new String[][]{{"x","y","z"},{"y","y1","z1"},{"z","z1","z2"}};
		final static String[][] type4Model = new String[][]{{"x","y","z"},{"y","z","x"},{"z","x","y"}};
		final static String[][] type5Model = new String[][]{{"x","y","0"},{"y","x","0"},{"0","0","z"}};
		final static String[][] type6Model = new String[][]{{"x","0","0"},{"0","y","0"},{"0","0","z"}};
	}
	private ConflictType confictType = ConflictType.TYPE_0;
	private List<Integer> orderOptSubjectIds = null;
	private List<Integer> orderProSubjectIds = null;
	private Set<Integer>[] conflictSubjectIds = new Set[]{new HashSet(),new HashSet()};
	private static int CD_OPT = NewDZBPlacementExcuter.CD_OPT;
	private static int CD_PRO = NewDZBPlacementExcuter.CD_PRO;
	int order = 0;
	protected SubjectCombination(){}
	public SubjectCombination(String subjectIdsStr,Integer totalStudentCount,PlacementTaskConfig config){
		this.subjectIdsStr = subjectIdsStr;
		this.totalStudentCount = totalStudentCount;
		remainSeqStudentCounts = new int[2][config.seqCounts[CD_PRO]];
		//所有科目Id List
		List<Integer> allSubjectIds = new ArrayList(config.optSubjectIdLessonMap.keySet());
		List<Integer> currentOptSubjectIds = new ArrayList();
		for(String oneSubjectIdStr:this.subjectIdsStr.split(",")){
			currentOptSubjectIds.add(Integer.parseInt(oneSubjectIdStr));
		}
		List<Integer> currentProSubjectIds  = new ArrayList(allSubjectIds);
		currentProSubjectIds.removeAll(currentOptSubjectIds);
		this.subjectIds[CD_OPT] = currentOptSubjectIds;
		this.subjectIds[CD_PRO] = currentProSubjectIds;
	}
	
	public ConflictType getConfictType() {
		return confictType;
	}
	public List<Integer> getOrderOptSubjectIds() {
		return orderOptSubjectIds;
	}
	public void setOrderOptSubjectIds(List<Integer> orderOptSubjectIds) {
		this.orderOptSubjectIds = orderOptSubjectIds;
	}
	public List<Integer> getOrderProSubjectIds() {
		return orderProSubjectIds;
	}
	public void setOrderProSubjectIds(List<Integer> orderProSubjectIds) {
		this.orderProSubjectIds = orderProSubjectIds;
	}
	public Set<Integer>[] getConflictSubjectIds() {
		return conflictSubjectIds;
	}
	public void setConflictSubjectIds(Set<Integer>[] conflictSubjectIds) {
		this.conflictSubjectIds = conflictSubjectIds;
	}
	public void setConfictType(ConflictType confictType) {
		this.confictType = confictType;
	}

	/**选考科目Id数组字符串
	 * 1,2,3
	 * **/
	private String subjectIdsStr = null;
	/** 科目Id数组  0学考，1选考**/
	List<Integer>[] subjectIds = new List[]{new ArrayList(),new ArrayList()};
	/**总学生人数**/
	private Integer totalStudentCount = null;
	/** 选考学考各科目剩余人数Map **/
	Map<Integer,Integer>[] remainSubjectStudentCountMaps = new Map[]{new HashMap(),new HashMap()};
	/** 选考学考各序列剩余人数数组 **/
	int remainSeqStudentCounts[][] ;
	/** 选考各序列各科目人数 Map<SujectId,studentCount>[seq-1]**/
	Map<Integer,Integer>[] optSeqSubjectStudentCountMaps =  new Map[]{new HashMap(),new HashMap(),new HashMap()};
	
	
	public Integer getTotalStudentCount() {
		return totalStudentCount;
	}
	public void setTotalStudentCount(Integer totalStudentCount) {
		this.totalStudentCount = totalStudentCount;
	}
	public String getSubjectIdsStr() {
		return subjectIdsStr;
	}
	/**
	 * 获取科目实体Bean
	 */
	public List<Subject> getSubjectList(int isOpt,Map<Integer, Subject> subjectIdMap){
		List<Subject> subjects = new ArrayList();
		for(Integer subjectId:subjectIds[isOpt]){
			subjects.add(subjectIdMap.get(subjectId));
		}
		return subjects;
	}
	/**
	 * 获取剩余未排科目
	 * @param isOpt
	 * @return
	 */
	public List<Integer> getUnfinishedSubjects(int isOpt){
		List<Integer> unfinishedSubjects = new ArrayList();
		for(Map.Entry<Integer,Integer> entry:remainSubjectStudentCountMaps[isOpt].entrySet()){
			if(!entry.getValue().equals(0)){
				unfinishedSubjects.add(entry.getKey());
			}
		}
		return unfinishedSubjects;
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		SubjectCombination one = (SubjectCombination)obj;
		return this.subjectIdsStr.equals(one.subjectIdsStr);
	}
	
}
