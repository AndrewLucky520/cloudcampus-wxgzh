package com.talkweb.placementtask.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.talkweb.common.tools.ListSortUtil;

 /**
 * @author 武洋
 *	日期：2017年7月3日
 *	用途：收纳定二走一中教学班分班传递的参数
 */
public class DezyDisTclassParams implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5874866214805771806L;

	/**
	 * 分班方案 1：跨组；2：不跨组--根据课时自动选择，课时不足选取1；课时充足选取2
	 */
	private int placeAlgMethod;
	/**
	 * 技术是否存在 1:存在，7选3；-1不存在，6选3；
	 */
	private int isTechExist;
	
	/**
	 * 0：行政班，1：走班
	 */
	private int formOfTech;
	/**
	*1:是；-1否
	*/
	private int isLearnExist;
	/**
	 * 班级平均人数
	 */
	private int classMidNum;
	/**
	 * 班级浮动人数
	 */
	private int classRoundNum;
	/**
	 * 班级最大人数
	 */
	private int classMaxNum;
	/**
	 * 班级最小人数
	 */
	private int classMinNum;
	/**
	 * 学选分离还是科目组分离 1:舟山 2：浒山
	 */
	private int combinedSubNum;
	/**
	 * 科目id-科目名称映射
	 */
	private   Map<String, String> kmMcMap ;
	/**
	 * 组合人数排序类
	 */
	ListSortUtil<TPlDezySubjectcomp> compSort = new ListSortUtil<TPlDezySubjectcomp>();

	/**
	 * 科目id-科目设置映射 
	 */
	private HashMap<String,TPlDezySubjectSet> 	subjectSetMap = new HashMap<String, TPlDezySubjectSet>();
	
	/**
	 * 志愿组合-id-对应的组合实例
	 * @return
	 */
	private HashMap<String,TPlDezySubjectcomp> subjectCompIdMap = new HashMap<String, TPlDezySubjectcomp>();

	public int getPlaceAlgMethod() {
		return placeAlgMethod;
	}
	public void setPlaceAlgMethod(int placeAlgMethod) {
		this.placeAlgMethod = placeAlgMethod;
	}
	public int getIsTechExist() {
		return isTechExist;
	}
	public void setIsTechExist(int isTechExist) {
		this.isTechExist = isTechExist;
	}
	public int getFormOfTech() {
		return formOfTech;
	}
	public void setFormOfTech(int formOfTech) {
		this.formOfTech = formOfTech;
	}
	public int getIsLearnExist() {
		return isLearnExist;
	}
	/**
	 * 技术学考是否存在
	 * @return
	 */
	public boolean isTechLearnExist() {
		return isLearnExist==1;
	}
	public void setIsLearnExist(int isLearnExist) {
		this.isLearnExist = isLearnExist;
	}
	public int getClassMidNum() {
		return classMidNum;
	}
	public void setClassMidNum(int classMidNum) {
		this.classMidNum = classMidNum;
	}
	public int getClassRoundNum() {
		return classRoundNum;
	}
	public void setClassRoundNum(int classRoundNum) {
		this.classRoundNum = classRoundNum;
	}
	public int getClassMaxNum() {
		return classMaxNum;
	}
	public void setClassMaxNum(int classMaxNum) {
		this.classMaxNum = classMaxNum;
	}
	public int getClassMinNum() {
		return classMinNum;
	}
	public void setClassMinNum(int classMinNum) {
		this.classMinNum = classMinNum;
	}
	public Map<String, String> getKmMcMap() {
		return kmMcMap;
	}
	public void setKmMcMap(Map<String, String> kmMcMap) {
		this.kmMcMap = kmMcMap;
	}
	public HashMap<String, TPlDezySubjectSet> getSubjectSetMap() {
		return subjectSetMap;
	}
	public void setSubjectSetMap(List<TPlDezySubjectSet> ssList) {
		for(TPlDezySubjectSet ss:ssList){
			subjectSetMap.put(ss.getSubjectId(), ss);
		}
	}
	
	public TPlDezySubjectSet getSubsetBySubId(String subId){
		return subjectSetMap.get(subId);
	}
	public int getCombinedSubNum() {
		return combinedSubNum;
	}
	public void setCombinedSubNum(int combinedSubNum) {
		this.combinedSubNum = combinedSubNum;
	}
	/**
	 * 分班算法是否跨组 true则跨组
	 * @return
	 */
	public boolean isCrossGroupAlg(){
		return this.placeAlgMethod==2?true:false;
	}
	/**
	 * 获取分班类型 1：不跨组-科目组分离 2：跨组-学选分离（浒山版本）3：跨组-科目组分离（舟山版本）
	 * @return
	 */
	public int getDisType(){
		int disType = 1;
		
		if(combinedSubNum==2&&placeAlgMethod==2){
			disType = 2;
		}else if(combinedSubNum==1&&placeAlgMethod==2){
			disType = 3;
		}if(combinedSubNum==2&&placeAlgMethod==1){
			disType = 1;
		}
		
		return disType;
	}
	public ListSortUtil<TPlDezySubjectcomp> getCompSort() {
		return compSort;
	}
	public HashMap<String, TPlDezySubjectcomp> getSubjectCompIdMap() {
		return subjectCompIdMap;
	}
	public void setSubjectCompIdMap(List<TPlDezySubjectcomp> subjectCompList) {
		for(TPlDezySubjectcomp sp:subjectCompList){
			subjectCompIdMap.put(sp.getSubjectCompId(), sp);
		}
	}
	/**
	 * 根据志愿组合id获取志愿组合
	 * @param subcompId
	 * @return
	 */
	public TPlDezySubjectcomp getSubjectcompById(String subcompId){
		return subjectCompIdMap.get(subcompId);
	}
	/**
	 * 根据志愿组合id获取行政班id
	 * @param subcompId
	 * @return
	 */
	public String getXzclassIdBySubcompId(String subcompId){
		if(subjectCompIdMap.containsKey(subcompId)){
			TPlDezySubjectcomp scp = subjectCompIdMap.get(subcompId);
			return scp.getClassId();
		}else{
			return null;
		}
	}
}
